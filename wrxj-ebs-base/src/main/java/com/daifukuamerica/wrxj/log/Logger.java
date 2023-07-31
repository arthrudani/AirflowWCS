package com.daifukuamerica.wrxj.log;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.log.database.DatabaseLoggerImpl;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Logger class is used to record events and messages for the executing
 * application. Several instances of static logger types are automatically
 * provided for system use. These are:
 * 
 * <p>
 * System Logger - The SYSTEM logger records all inter-process-communication
 * message events, debug text, errors, exceptions, and operations in the
 * executing application. There is only ONE system logger running in an
 * executing application.
 * </p>
 * 
 * <p>
 * Error Logger - The ERROR logger records all faults, failures, anomalies, and
 * exceptions in the executing application. There is only ONE error logger
 * running in an executing application.
 * </p>
 * 
 * <p>
 * Operation Logger - The OPERATION logger records all (normal, not error)
 * critical events such as user login, device startup/shutdown, load movement,
 * etc. in the executing application. There is only ONE operation logger running
 * in an executing application.
 * </p>
 * 
 * <p>
 * All Objects/Threads/Controllers that are in the application log to these
 * central loggers. This allows all important system activity and interaction to
 * be kept in one coherent and organized place. Communication logs are created
 * and used by a single {@link com.daifukuamerica.wrxj.common.comport.ComPort
 * ComPort} implementation. So, each ComPort has its own, separate Communication
 * log.
 * </p>
 * 
 * <p>
 * Communication Logger - The COMMUNICATION logger records all RAW BYTE DATA
 * received and transmitted from any type communication port, at the lowest evel
 * possible. There is one communication logger PER PORT running in an executing
 * application, so there can be many communication loggers active.
 * </p>
 * 
 * @author Stephen Kendorski
 * @version 1.0
 */
public final class Logger
{
  private static final String START_LOG_ENTRY = "=============== Start log ===============";
  
  private static Log      gpSystemLog = null;
  private static Log      gpErrorLog = null;
  private static Log      gpOperationLog = null;

  private static ThreadLocal<String> gpDefaultKeys = new ThreadLocal<>();
  private static String uniqueSystemKey = null;
  private static Map<String,Log> gpLogs = new HashMap<>();
  private static List<String> gpLogNames = new ArrayList<>();

  private String          loggerInstanceName = null;
  private int             loggerInstanceKey = 0;

  private Log             mpCommLog = null;
  private Log             mpEquipmentLog = null;

  /**
   * Maps log names to their loggers.
   * 
   * <p><b>Details:</b> This member maps loggable entities ({@link Object}s) to 
   * loggers ({@link Logger}s).</p>
   */
  private static final Map<String, Logger> gpLoggers = new HashMap<>();

  /**
   * Initialize the logging system
   */
  static
  {
    int logSize;
    // error log
    logSize = Application.getInt("ErrorLogSize", LogConsts.MAX_ERROR_LOGS);
    gpErrorLog = getLoggerInstance(logSize);
    gpErrorLog.setLogSyncNeeded(true);
    gpErrorLog.setLogType(LogConsts.ERROR_LOG_NAME);
    gpLogs.put(LogConsts.ERROR_LOG_NAME, gpErrorLog);
    gpLogNames.add(LogConsts.ERROR_LOG_NAME + "\tError Logs");
    // operation log
    logSize = Application.getInt("OperationLogSize", LogConsts.MAX_OPERATION_LOGS);
    gpOperationLog = getLoggerInstance(logSize);
    gpOperationLog.setLogSyncNeeded(true);
    gpOperationLog.setLogType(LogConsts.OPERATION_LOG_NAME);
    gpLogs.put(LogConsts.OPERATION_LOG_NAME, gpOperationLog);
    gpLogNames.add(LogConsts.OPERATION_LOG_NAME + "\tOperation Logs");
    // system log
    logSize = Application.getInt("SystemLogSize", LogConsts.MAX_LOGS);
    gpSystemLog = getLoggerInstance(logSize);
    gpSystemLog.setLogSyncNeeded(true);
    gpSystemLog.setLogType(LogConsts.SYSTEM_LOG_NAME);
    gpLogs.put(LogConsts.SYSTEM_LOG_NAME, gpSystemLog);
    gpLogNames.add(LogConsts.SYSTEM_LOG_NAME + "\tSystem Logs");
    //
    uniqueSystemKey = Application.getString(WarehouseRx.RUN_MODE);
    uniqueSystemKey = uniqueSystemKey != null ? uniqueSystemKey : "Client";
  }
  
  /*--------------------------------------------------------------------------*/
  /* static methods                                                           */
  /*--------------------------------------------------------------------------*/
  
  /**
   * Get the LoggerImpl instance.
   * 
   * <p>This cannot be done by the Factory because it may need to be called 
   * before the Factory is initialized.</p>
   * 
   * @param inLogSize
   * @return
   */
  public static LoggerImpl getLoggerInstance(int inLogSize)
  {
    if (Application.getBoolean(DatabaseLoggerImpl.DBLOG_ENABLED))
    {
      return new DatabaseLoggerImpl(inLogSize);
    }
    else
    {
      return new LoggerImpl(inLogSize);
    }
  }
  
  /**
   * Determines current loggable entity.
   * 
   * <p><b>Details:</b> This method determines the default loggable entity for 
   * the current thread/<w
   * br>event context.  This is either the current thread 
   * or, if the current thread is an AWT event dispatch thread, a loggable UI 
   * component that contains the source of the current UI event.  If the current 
   * UI event's source is not contained by a loggable component, then the 
   * dispatch thread itself is taken as the effective loggable entity.</p>
   * 
   * @return the loggable entity
   */
  private static String getLoggerKey()
  {
    String vsKey = getDefaultLoggerKey();
    if (vsKey != null)
      return vsKey;
    
    /*
     *  Mike removed a bunch of code here that didn't actually do anything 
     *  except return the following:
     */
    return Thread.currentThread().getName();
  }

  /**
   * Set the default logger key name
   * 
   * @param isKey
   */
  public static void setDefaultLoggerKey(String isKey)
  {
    gpDefaultKeys.set(isKey);
  }
  
  /**
   * Get the default logger key name
   * 
   * @return
   */
  public static String getDefaultLoggerKey()
  {
    return gpDefaultKeys.get();
  }
  
  /**
   * Returns logger for current loggable entity.
   * 
   * <p><b>Details:</b> This method determines the current loggable entity and
   * returns a logger bound to it.  If the logger doesn't yet exist, it will be
   * created.</p>
   * 
   * @return the logger
   */
  public static synchronized Logger getLogger()
  {
    String vpKey = getLoggerKey();
    return getLogger(vpKey);
  }

  /**
   * Returns logger for current loggable entity.
   * 
   * <p><b>Details:</b> This method determines the current loggable entity and
   * returns a logger bound to it.  If the logger doesn't yet exist, it will be
   * created.</p>
   * 
   * @param ipKey key for the loggable entity
   * @return the logger
   */
  public static synchronized Logger getLogger(String ipKey)
  {
    Logger vpLogger = gpLoggers.get(ipKey);
    if (vpLogger == null)
    {
      // No logger has been created for this key.  Let's do it now.
      vpLogger = createLogger(ipKey);
      gpLoggers.put(ipKey, vpLogger);
    }
    return vpLogger;
  }

  /**
   * Creates logger for entity.
   * 
   * <p><b>Details:</b> This method creates and returns a new logger for the
   * given loggable entity.</p>
   * 
   * @param ipSource
   * @return the new logger
   */
  private static Logger createLogger(String isName)
  {
    Logger vpLogger = new Logger();
    if (isName.indexOf(':') == -1)
      isName = isName + ':' + uniqueSystemKey;
    vpLogger.loggerInstanceName = isName;
    vpLogger.loggerInstanceKey = gpLoggers.size();
    return vpLogger;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return a collection of all instantiated loggers in the application.
   *
   * @return all loggers
   */
  public static synchronized List<Log> getLogInstances()
  {
    return new ArrayList<>(gpLogs.values());
  }

  /**
   * Get all of the logger instances
   * 
   * @return
   */
  public static List<Logger> getLoggerInstances()
  {
    return new ArrayList<>(gpLoggers.values());
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   * Return the SYSTEM logger in the application. The SYSTEM logger records all
   * inter-process-communication message events, debug text, errors, exceptions,
   * and operations in the executing application. There is only ONE system
   * logger running in an executing application.
   * 
   * @return a suitable logger
   */
  public static Object getSystemLogger()
  {
    return gpSystemLog;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the ERROR logger in the application. The ERROR logger records all
   * faults, failures, anomalies, and exceptions in the executing application.
   * There is only ONE error logger running in an executing application.
   * 
   * @return a suitable logger
   */
  public static Object getErrorLogger()
  {
    return gpErrorLog;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the OPERATION logger in the application. The OPERATION logger
   * records all (normal, not error) critical events such as user login, device
   * startup/shutdown, load movement, etc. in the executing application. There
   * is only ONE operation logger running in an executing application.
   * 
   * @return a suitable logger
   */
  public static Object getOperationLogger()
  {
    return gpOperationLog;
  }

  /**
   * Get the system name
   * 
   * @return
   */
  public static String getSystemName()
  {
    return uniqueSystemKey;
  }

  /**
   * Return a String containing a tab delimited list of log names and descriptions.
   *
   * @return a string of log tokens
   */
  public static synchronized String getDelimitedLogList()
  {
    StringBuffer vpBuff = new StringBuffer();
    vpBuff.append(uniqueSystemKey).append("\n"); 
    for (Iterator<String> vpIter = gpLogNames.iterator(); vpIter.hasNext();)
      vpBuff.append(vpIter.next()).append('\n');
    return vpBuff.toString();
  }

  /**
   * Return an individual log (NOT a Logger Logger implementation).
   *
   * @return a log
   */
  public static synchronized Object getLog(String logDescription)
  {
    return gpLogs.get(logDescription);
  }

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  /**
   * Construct a Logger system. Normally other classes that need loggers should
   * use this class's <i>static</i> methods to get the actual loggers. This
   * class would call its own constructor to instantiate instances of loggers,
   * as needed. The Class Logger's {@link #getLogger() getLogger} method is used
   * to instantiate BaseClass objects that tag log records with the caller's
   * name. The logger Object uses the Class's static methods to actually deal
   * with the log records.
   */
  protected Logger()
  {
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the name of the instantiated logger.
   *
   * @return a name
   */
  public String getLoggerInstanceName()
  {
    return loggerInstanceName;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the key of the instantiated logger.
   *
   * @return a key
   */
  public int getLoggerInstanceKey()
  {
    return loggerInstanceKey;
  }

  /*--------------------------------------------------------------------------*/
  /*  Add & get communication and equipment logs                              */
  /*--------------------------------------------------------------------------*/
  
  /**
   * Find or create a COMMUNICATION logger for a named subsystem. If a
   * communication logger has does not already exist for the named subsystem a
   * new communication logger is created. Communication loggers are NOT static,
   * so there can be more than one communication logger object in the system.
   * There is one communication logger PER PORT running in an executing
   * application, so there can be many communication loggers active at once.
   */
  public void addCommLogger()
  {
    synchronized (getClass()) 
    {
      if (mpCommLog == null) 
      {
        int logSize = Application.getInt("CommLogSize", LogConsts.MAX_LOGS);
        mpCommLog = getLoggerInstance(logSize);
        mpCommLog.setLogSyncNeeded(false);
        mpCommLog.setLogName(loggerInstanceName);
        mpCommLog.setLogType(LogConsts.COMM_LOG_NAME);
        gpLogs.put(loggerInstanceName, mpCommLog);
        gpLogNames.add(loggerInstanceName + "\tComm Logs");
        LogFileReaderWriter.addCommLogAutoSaver(this);
        mpCommLog.logTxByteCommunication(START_LOG_ENTRY.getBytes(), 0,
            START_LOG_ENTRY.length());
      }
    }
  }

  /**
   * Return the named subsystem's COMMUNICATION logger object. The COMMUNICATION
   * logger records all RAW BYTE DATA received and transmitted from any type
   * communication port, at the lowest level possible. There is one
   * communication logger PER PORT running in an executing application, so there
   * can be many communication loggers active.
   * 
   * @return a suitable logger
   */
  public Object getCommLogger()
  {
    return mpCommLog;
  }

  /**
   * Find or create an EQUIPMENT logger for a named subsystem. If an equipment
   * logger has does not already exist for the named subsystem a new equipment
   * logger is created. Equipment loggers are NOT static, so there can be more
   * than one equipment logger object in the system. There is one equipment
   * logger PER PIECE OF EQUIPMENT running in an executing application, so there
   * can be many equipment loggers active at once.
   */
  public void addEquipmentLogger()
  {
    synchronized (getClass())
    {
      if (mpEquipmentLog == null)
      {
        int logSize = Application.getInt("EquipmentLogSize", LogConsts.MAX_LOGS);
        mpEquipmentLog = getLoggerInstance(logSize);
        mpEquipmentLog.setLogSyncNeeded(false);
        mpEquipmentLog.setLogName(loggerInstanceName);
        mpEquipmentLog.setLogType(LogConsts.EQUIPMENT_LOG_NAME);
        gpLogs.put(loggerInstanceName, mpEquipmentLog);
        gpLogNames.add(loggerInstanceName + "\tEquipment Logs");
        LogFileReaderWriter.addEquipmentLogAutoSaver(this);
        mpEquipmentLog.logTxEquipmentMessage("", START_LOG_ENTRY);
      }
    }
  }

  /**
   * Return the named subsystem's EQUIPMENT logger object. The EQUIPMENT logger
   * records all VALID MESSAGES received and transmitted from any type equipment
   * port. There is one equipment logger PER PIECE OF EQUIPMENT running in an
   * executing application, so there can be many equipment loggers active.
   * 
   * @return a suitable logger
   */
  public Log getEquipmentLogger()
  {
    return mpEquipmentLog;
  }


  /*--------------------------------------------------------------------------*/
  /* Operation Logs (Operation & System logs)                                 */
  /*--------------------------------------------------------------------------*/

  /**
   * Record, to the OPERATION Logger (and SYSTEM Logger), an event message
   * for the named subsystem.
   *
   * @param logText the string message
   */
  public void logOperation(String logText)
  {
    logOperation(LogConsts.OPR_NONE, " ", logText);
  }

  /**
   * Record, to the OPERATION Logger (and SYSTEM Logger), an event message of
   * the specified category for the named subsystem.
   *
   * @param logType the category
   * @param logText the string message
   */
  public void logOperation(int logType, String logText)
  {
    logOperation(logType, " ", logText);
  }
  
  /**
   * Record, to the OPERATION Logger (and SYSTEM Logger), an event message of
   * the specified category for the named subsystem.
   *
   * @param logType the category
   * @param logKey the search key
   * @param logText the string message
   */
  public void logOperation(int logType, String logKey, String logText)
  {
    if (gpSystemLog != null)
    {
      String sLogText = logText;
      if (logKey != null && logKey.trim().length() > 0)
      {
        sLogText = logKey + ": " + sLogText;
      }
      gpSystemLog.logDebugOperation(loggerInstanceName, loggerInstanceKey, sLogText, logType);
    }
    if (gpOperationLog != null)
    {
      gpOperationLog.logOperation(loggerInstanceName, loggerInstanceKey, logKey, logText, logType);
    }
  }

  /*--------------------------------------------------------------------------*/
  /* Exception Logging (Operation, System, and Error Logs)                    */
  /*--------------------------------------------------------------------------*/
  /**
   * Record, to the ERROR Logger (and SYSTEM Logger), an Exception for the
   * named subsystem.  The log record description text field includes the
   * Exception description and the stack trace.
   *
   * @param e the Exception
   */
  public void logException(Throwable e)
  {
    logException(e, "");
  }

  /**
   * Record, to the ERROR Logger (and SYSTEM Logger), an Exception (with the
   * caller's text) for the named subsystem.  The log record description
   * text field includes the caller's text, the Exception description, and the
   * stack trace.
   *
   * @param e the Exception
   * @param sText the String message
   */
  public void logException(Throwable e, String sText)
  {
    if (gpErrorLog != null)
    {
      StringWriter out = new StringWriter();
      e.printStackTrace(new PrintWriter(out));
      
      String vsMessage = e.getMessage();
      if (vsMessage == null)
      {
        vsMessage = e.getClass().getSimpleName();
      }
      if (vsMessage.indexOf(": ") > 0)
      {
        vsMessage = vsMessage.substring(vsMessage.indexOf(": ") + 2);
      }
      logError("##EXCEPTION## \"" + vsMessage + "\"\n " + sText + "\n\n"
          + "DETAILS:\n" + out.toString());
    }
  }

  /**
   * Record, to the ERROR Logger (and SYSTEM Logger), an Exception (with the
   * caller's text) for the named subsystem.  The log record description
   * text field includes the caller's text, the Exception description, and the
   * stack trace.
   *
   * @param e the Exception
   * @param sText the String message
   */
  public void logException(String sText, Throwable e)
  {
    logException(e, sText);
  }
  
  /**
   * Record, to the ERROR Logger (and SYSTEM Logger), an Exception (with the
   * caller's text) for the named subsystem.  The log record description
   * text field includes the caller's text and the Exception description, but
   * NOT the stack trace.
   *
   * @param e the Exception
   * @param sText the String message
   */
  public void logSparseException(Throwable e, String sText)
  {
    if (gpErrorLog != null)
    {
      logError("##EXCEPTION## \"" + e.toString() + "\" -- \"" + sText + "\"");
    }
  }

  /*--------------------------------------------------------------------------*/
  /* Error Logging (Operation, System, and Error Logs)                        */
  /*--------------------------------------------------------------------------*/
  
  /**
   * Record, to the ERROR Logger (and SYSTEM Logger), a fault description for
   * the named subsystem.
   *
   * @param logText the fault message
   */
  public void logError(String logText)
  {
    logError(logText, 0);
  }

  /**
   * Record, to the ERROR Logger (and SYSTEM Logger), a fault description and
   * code for the named subsystem.
   *
   * @param logText the fault message
   * @param errorNumber the fault code
   */
  public void logError(String logText, int errorNumber)
  {
    if (gpSystemLog != null)
    {
      gpSystemLog.logDebugError(loggerInstanceName, loggerInstanceKey, logText);
    }
    if (gpOperationLog != null)
    {
      gpOperationLog.logOperation(loggerInstanceName, loggerInstanceKey, ""
          + errorNumber, "##### " + logText + " #####", LogConsts.OPR_ERROR);
    }
    if (gpErrorLog != null)
    {
      gpErrorLog.logError(loggerInstanceName, loggerInstanceKey, logText,
          errorNumber);
    }
  }

  
  /*--------------------------------------------------------------------------*/
  /* System Logs                                                              */
  /*--------------------------------------------------------------------------*/
  
  /**
   * Record, to the SYSTEM Logger, a Debug text message for the named subsystem.
   *
   * @param logText the string message
   */
  public void logDebug(String logText)
  {
    if (gpSystemLog != null)
    {
      gpSystemLog.logDebug(loggerInstanceName, loggerInstanceKey, logText);
    }
  }

  /**
   * Record, to the SYSTEM Logger, an INCOMING inter-process-communication
   * message event for the named subsystem.
   *
   * @param logText the message event text
   * @param eventType the message category
   */
  public void logReceivedMessage(String logText, String eventType)
  {
    if (gpSystemLog != null)
    {
      gpSystemLog.logReceivedMessage(loggerInstanceName, loggerInstanceKey,
              logText, eventType);
    }
  }

  /**
   * Record, to the SYSTEM Logger, an OUTGOING inter-process-communication
   * message event for the named subsystem.
   *
   * @param logText the message event text
   * @param msgType the message category
   */
  public void logTransmittedMessage(String logText, String msgType)
  {
    if (gpSystemLog != null)
    {
      gpSystemLog.logTransmittedMessage(loggerInstanceName, loggerInstanceKey,
               logText, msgType);
    }
  }

  
  /*--------------------------------------------------------------------------*/
  /* Communication Logs                                                       */
  /*--------------------------------------------------------------------------*/
  
  /**
   * Record, to the COMMUNICATION Logger, RECEIVED Comport BYTE data for the
   * named subsystem.
   *
   * @param byteArray the data bytes
   * @param iOffset the index to the first data byte to record
   * @param iCount the number of data bytes to record
   */
  public void logRxByteCommunication(byte[] byteArray, int iOffset, int iCount)
  {
    if (mpCommLog != null)
    {
      mpCommLog.logRxByteCommunication(byteArray, iOffset, iCount);
    }
  }

  /**
   * Record, to the COMMUNICATION Logger, TRANSMITTED Comport BYTE data for the
   * named subsystem.
   *
   * @param byteArray the data bytes
   * @param iOffset the index to the first data byte to record
   * @param iCount the number of data bytes to record
   */
  public void logTxByteCommunication(byte[] byteArray, int iOffset, int iCount)
  {
    if (mpCommLog != null)
    {
      mpCommLog.logTxByteCommunication(byteArray, iOffset, iCount);
    }
  }

  
  /*--------------------------------------------------------------------------*/
  /* Equipment Logs                                                           */
  /*--------------------------------------------------------------------------*/
  
  /**
   * Record, to the EQUIPMENT Logger, RECEIVED VALID MESSAGE data for the
   * named subsystem.
   *
   * @param entryData the message data
   * @param clarifier the interpreted data
   */
  public void logRxEquipmentMessage(String entryData, String clarifier)
  {
    if (mpEquipmentLog != null)
    {
      mpEquipmentLog.logRxEquipmentMessage(entryData, clarifier);
    }
  }

  /**
   * Record, to the EQUIPMENT Logger, TRANSMITTED VALID MESSAGE data for the
   * named subsystem.
   *
   * @param entryData the message data
   * @param clarifier the interpreted data
   */
  public void logTxEquipmentMessage(String entryData, String clarifier)
  {
    if (mpEquipmentLog != null)
    {
      mpEquipmentLog.logTxEquipmentMessage(entryData, clarifier);
    }
  }
}

