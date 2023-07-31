/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.log.database;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.log.LoggerImpl;
import com.daifukuamerica.wrxj.util.SKDCUtility;
//import org.apache.log4j.Level;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.apache.log4j.MDC;

/**
 * Logger that also logs to the Wynsoft AED database.  Wynsoft separates logs 
 * into two tables, so this does as well.  This may also be used without a
 * Wynsoft AED database by updating the synonyms as needed.
 * 
 * <p>
 * To enable, add the following to wrxj.properties.  This cannot be configured
 * via factory.properties because it is needed before the factory is 
 * initialized.
 * <pre>
# true for file+database logging, false (default) for file only 
DbLog.Enabled=true
# DEBUG, INFO, WARN (default), OFF
DbLog.Level=WARN
 * </pre></p>
 * 
 * Synonyms allow the tables to be located in AED for integrated Wynsoft 
 * installations or in WRx for standalone installations.  Required synonyms:
 * <ul>
 * <li>logError logs to S_AED_WRX_LOG_ERR -> AED.dbo.WRX_LOG_ERR or WRXLOG</li>
 * <li>everything else logs to S_AED_WRX_LOG_INF -> AED.dbo.WRX_LOG_ERR or WRXLOG</li>
 * </ul>
 * 
 * Column mapping:
 * <ul>
 *   <li>[IDENTITY_NAME] - "WRx"</li>
 *   <li>[DATE_TIME]     - SYSDATETIME()</li>
 *   <li>[SOURCE]        - Log Name</li>
 *   <li>[AREA]          - Thread Name</li>
 *   <li>[POSITION]      - key</li>
 *   <li>[DESCRIPTION]   - "Timestamp - Message"</li>
 *   <li>[SUBJECT]       - First 60 characters of Description</li>
 *   <li>[INFOWARNFATAL] - First letter of level (Debug, Info, Warn)</li>
 * </ul>
 * 
 * Warn is used instead of Error to more closely match other Wynsoft products
 * 
 * @author mandrus
 */
public class DatabaseLoggerImpl extends LoggerImpl
{
  /*========================================================================*/
  /* Log4J log via JDBC                                                     */
  /*========================================================================*/
  private static final Logger LOG_ERR;
  private static final Logger LOG_INF;
  private static final Logger LOG_EQUIP;
  
  // Configurable via wrxj.properties
  public static final String DBLOG_ENABLED = "DbLog.Enabled";
  private static final String CONFIG_LEVEL = "DbLog.Level";
  private static final String CONFIG_SQL_ERR = "DbLog.Err.Sql";
  private static final String CONFIG_SQL_INF = "DbLog.Inf.Sql";
  private static final String CONFIG_SQL_EQUIP = "DbLog.Equip.Sql";
  
  private static final String DEFAULT_LEVEL = Level.WARN.toString();
  private static final String DEFAULT_SQL_ERR = "INSERT INTO S_AED_WRX_LOG_ERR (IDENTITY_NAME,DATE_TIME,SOURCE,AREA,POSITION,DESCRIPTION,SUBJECT,INFOWARNFATAL)"
      + " VALUES ('WRx',SYSDATETIME(),'%X{LogSource}','%X{LogArea}',%X{LogPosition},'%d{yyyy/MM/dd HH:mm:ss.SSS XXX} - %m','%X{LogSubject}',left('%p',1))";
  private static final String DEFAULT_SQL_INF = "INSERT INTO S_AED_WRX_LOG_INF (IDENTITY_NAME,DATE_TIME,SOURCE,AREA,POSITION,DESCRIPTION,SUBJECT,INFOWARNFATAL)"
      + " VALUES ('WRx',SYSDATETIME(),'%X{LogSource}','%X{LogArea}',%X{LogPosition},'%d{yyyy/MM/dd HH:mm:ss.SSS XXX} - %m','%X{LogSubject}',left('%p',1))";
  private static final String DEFAULT_SQL_EQUIP = "INSERT INTO WRXEQUIPLOG (IDENTITY_NAME,DATE_TIME,DEVICEID,IDIRECTION,COUNT,DATA)"
	      + " VALUES ('WRx',SYSDATETIME(),'%X{LogDeviceid}','%X{LogDirection}','%X{LogCount}','%X{LogData}')";

  // Special parameters for the logs
  public static final String LOGGER_SOURCE_KEY = "LogSource";
  public static final String LOGGER_AREA_KEY = "LogArea";
  public static final String LOGGER_POSITION_KEY = "LogPosition";
  public static final String LOGGER_SUBJECT_KEY = "LogSubject";
  public static final String LOGGER_DEVICEID_KEY = "LogDeviceid";
  public static final String LOGGER_DIRECTION_KEY = "LogDirection";
  public static final String LOGGER_COUNT_KEY = "LogCount";
  public static final String LOGGER_DATA_KEY = "LogData";
  static
  {
    LOG_ERR = LogManager.getLogger("WRxDbLoggerErr");
    LOG_INF = LogManager.getLogger("WRxDbLoggerInf");
    LOG_EQUIP = LogManager.getLogger("WRxDbLoggerEquip");
  }

  /*========================================================================*/
  /* Constructors                                                           */
  /*========================================================================*/

  /**
   * Constructor
   */
  public DatabaseLoggerImpl()
  {
    super();
  }
  
  /**
   * Constructor
   * @param maxLogs
   */
  public DatabaseLoggerImpl(int maxLogs)
  {
    super(maxLogs);
  }

  /*========================================================================*/
  /* New methods                                                            */
  /*========================================================================*/

  /**
   * Extra info for DB log
   */
  private void setLogName(String isName, int inKey, String isSubject)
  {
    String vsSource;
    if (SKDCUtility.isNotBlank(isName))
    {
      vsSource = isName;
    }
    else if (SKDCUtility.isNotBlank(getLogName()))
    {
      vsSource = getLogName();
    }
    else
    {
      vsSource = com.daifukuamerica.wrxj.log.Logger.getLogger().getLoggerInstanceName();
    }
    ThreadContext.put(LOGGER_SOURCE_KEY, SKDCUtility.substring(vsSource,0,60));
    ThreadContext.put(LOGGER_POSITION_KEY, String.valueOf(inKey));
    if (SKDCUtility.isNotBlank(isSubject))
    {
        ThreadContext.put(LOGGER_SUBJECT_KEY, isSubject);
    }
  }
  
  /**
   * Clear the log name for the DB log
   */
  private void clearLogName()
  {
      ThreadContext.clearAll();
  }
  
  /**
   * Extra info for DB log
   */
  private void setEquipLogName(String isName, int inDirection, int inCount, String isData)
  {
    String vsSource;
    if (SKDCUtility.isNotBlank(isName))
    {
      vsSource = isName;
    }
    else if (SKDCUtility.isNotBlank(getLogName()))
    {
      vsSource = getLogName();
    }
    else
    {
      vsSource = com.daifukuamerica.wrxj.log.Logger.getLogger().getLoggerInstanceName();
    }
    String [] sVars = SKDCUtility.getTokens(vsSource, ":");
	  
    ThreadContext.put(LOGGER_DEVICEID_KEY, sVars[0]);
    ThreadContext.put(LOGGER_DIRECTION_KEY, String.valueOf(inDirection));
    ThreadContext.put(LOGGER_COUNT_KEY, String.valueOf(inCount));
    if (SKDCUtility.isNotBlank(isData))
    {
        ThreadContext.put(LOGGER_DATA_KEY, isData);
    }
  }
  
  /**
   * Clear the log name for the DB log
   */
  private void clearEuipLogName()
  {
      ThreadContext.clearAll();
  }
  
  /**
   * Planning Logger - implementation
   * 
   * @param t
   * @param l
   * @param isFormat
   */
  private void logErr(Level l, String isMessage)
  {
    LOG_ERR.log(l, trimMessage(isMessage));
  }

  /**
   * Planning Logger - implementation
   * 
   * @param t
   * @param l
   * @param isFormat
   */
  private void logInf(Level l, String isMessage)
  {
    LOG_INF.log(l, trimMessage(isMessage));
  }
  
  /**
   * Planning Logger - implementation
   * 
   * @param t
   * @param l
   * @param isFormat
   */
  private void logEquip(Level l, String isMessage)
  {
    LOG_EQUIP.log(l, trimMessage(isMessage));
  }

  /**
   * Get Subject
   * 
   * @param isLogType
   * @param inLogType
   * @return
   */
  private String getSubject(String isLogType, int inLogType)
  {
    return inLogType > 0 ? isLogType + " [" + inLogType + "]" : isLogType;
  }
  
  /**
   * Return a sort of escaped, trimmed message.  TODO: use parameters
   * 
   * @param isMsg
   * @return
   */
  private String trimMessage(String isMsg)
  {
    return SKDCUtility.substring(isMsg,0,3000);
  }

  /*========================================================================*/
  /* Overridden methods                                                     */
  /*========================================================================*/

  /**
   * logDebug
   */
  @Override
  public void logDebug(String logText)
  {
    logDebug(null, 0, logText);
  }
  
  /**
   * logDebug
   */
  @Override
  public void logDebug(String name, int key, String logText)
  {
    super.logDebug(name, key, logText);

    setLogName(name, key, "Debug");
    logInf(Level.DEBUG, logText);
    clearLogName();
  }
  
  /**
   * logError
   */
  @Override
  public void logError(String name, int key, String logText)
  {
    logError(name, key, logText, 0);
  }

  /**
   * logError
   */
  @Override
  public void logError(String name, int key, String logText, int errorNumber)
  {
    super.logError(name, key, logText, errorNumber);
    
    setLogName(name, key, getSubject("Error", errorNumber));
    if (errorNumber > 0)
    {
      // WARN to match Wynsoft
      logErr(Level.WARN, "[" + errorNumber + "] " + logText);
    }
    else
    {
      // WARN to match Wynsoft
      logErr(Level.WARN, logText);
    }
    clearLogName();
  }
  
  /**
   * logReceivedMessage
   */
  @Override
  public void logReceivedMessage(String name, int key, String logText,
      String msgType)
  {
    super.logReceivedMessage(name, key, logText, msgType);
    
    setLogName(name, key, "Receive IPC Message");
    logInf(Level.DEBUG, "RECV: [" + msgType + "] " + logText);
    clearLogName();
  }

  /**
   * logTransmittedMessage
   */
  @Override
  public void logTransmittedMessage(String name, int key, String logText,
      String msgType)
  {
    super.logTransmittedMessage(name, key, logText, msgType);
    
    setLogName(name, key, "Send IPC Message");
    logInf(Level.DEBUG, "SEND: [" + msgType + "] " + logText);
    clearLogName();
  }
  
  /**
   * logRxEquipmentMessage
   */
  @Override
  public void logRxEquipmentMessage(String entryData, String clarifier)
  {
    super.logRxEquipmentMessage(entryData, clarifier);
    
    setEquipLogName(null, DBConstants.INBOUND, 0, "RECV: " + clarifier + " [" + entryData + "]");
    logEquip(Level.INFO, "RECV: " + clarifier + " [" + entryData + "]");
    clearEuipLogName();
  }
  
  /**
   * logTxEquipmentMessage
   */
  @Override
  public void logTxEquipmentMessage(String entryData, String clarifier)
  {
    super.logTxEquipmentMessage(entryData, clarifier);
    
    if (SKDCUtility.equals(clarifier, "=============== Start log ==============="))
    {
      setEquipLogName(null, DBConstants.OUTBOUND, 0, "Start up");
      logEquip(Level.INFO, "Starting up");
    }
    else
    {
      int dLength = entryData.length();
      setEquipLogName(null, DBConstants.OUTBOUND, dLength, "SEND: " + clarifier + " [" + entryData + "]");
      logEquip(Level.INFO, "SEND: " + clarifier + " [" + entryData + "]");
    }
    clearEuipLogName();
  }
  
  /**
   * logOperation
   */
  @Override
  public void logOperation(String name, int key, String isLogText,
      int inLogType)
  {
    logOperation(name, key, null, isLogText, inLogType);
  }
  
  /**
   * logOperation
   */
  @Override
  public void logOperation(String name, int key, String logKey, String logText,
      int inLogType)
  {
    super.logOperation(name, key, logKey, logText, inLogType);
    
    // Don't log re-logged errors
    if (!SKDCUtility.startsWith(logText, "##### "))
    {
      setLogName(name, key, getSubject("Operation", inLogType));
      if (SKDCUtility.isBlank(logKey))
      {
        logInf(Level.INFO, logText);
      }
      else
      {
        logInf(Level.INFO, "[" + logKey + "] " + logText);
      }
      clearLogName();
    }
  }
}
