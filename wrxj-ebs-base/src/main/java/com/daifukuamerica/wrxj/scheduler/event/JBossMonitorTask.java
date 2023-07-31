package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swingui.utility.JBossMonitorFrame;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * Class to take periodic statistics of the JBoss database for debug purposes.
 * 
 * <P>Required ControllerConfig properties:
 * <UL>
 * <LI><tt>interval</tt> - in seconds</LI>
 * <LI><tt>LogType</tt> - must be Debug, Error, or Operation</LI>
 * <LI><tt>limit</tt> - Log to Error log if the count exceeds this number</LI>
 * <LI><tt>driver</tt> - JDBC driver</LI>
 * <LI><tt>url</tt> - JDBC url</LI>
 * <LI><tt>user</tt> - JDBC user</LI>
 * <LI><tt>password</tt> - JDBC password</LI>
 * </UL>
 * 
 * @author mandrus
 */
public class JBossMonitorTask extends TimedEventTask
{
  private static final String PROP_DRIVER = "driver";
  private static final String PROP_URL = "url";
  private static final String PROP_USER = "user";
  private static final String PROP_PASSWORD = "password";
  
  private static final String PROP_LOGTYPE = "LogType";
  private static final String LOGTYPE_DEBUG = "Debug";
  private static final String LOGTYPE_ERROR = "Error";
  private static final String LOGTYPE_OPERATION = "Operation";
  private static final String PROP_LIMIT = "limit";
  
  private DBObject mpJBossDBObj;
  
  private int mnMaxCount = 0;
  private int mnMinCount = Integer.MAX_VALUE;
  
  private int mnLimit;
  private String msLogType;
  
  /**
   * Constructor
   * 
   * @param isName
   */
  public JBossMonitorTask(String isName)
  {
    super(isName);
  }

  /**
   * Initialize the task
   */
  @Override
  public String initTask()
  {
    // JBoss database information (Oracle only!)
    String vsDriver = getConfigString(PROP_DRIVER);
    if (vsDriver == null)
    {
      return getPropertyError(PROP_DRIVER, vsDriver);
    }
    String vsURL = getConfigString(PROP_URL);
    if (vsURL == null)
    {
      return getPropertyError(PROP_URL, vsURL);
    }
    String vsUser = getConfigString(PROP_USER);
    if (vsUser == null)
    {
      return getPropertyError(PROP_USER, vsUser);
    }
    String vsPassword = getConfigString(PROP_PASSWORD);
    if (vsPassword == null)
    {
      return getPropertyError(PROP_PASSWORD, vsPassword);
    }
    Application.setString(JBossMonitorFrame.JBOSS_DB + ".driver", vsDriver);
    Application.setString(JBossMonitorFrame.JBOSS_DB + ".url", vsURL);
    Application.setString(JBossMonitorFrame.JBOSS_DB + ".user", vsUser);
    Application.setString(JBossMonitorFrame.JBOSS_DB + ".password", vsPassword);
    Application.setString(JBossMonitorFrame.JBOSS_DB + ".maximum", "3");
    mpJBossDBObj = null;
      
    // Logger
    msLogType = getConfigString(PROP_LOGTYPE);
    if (msLogType == null ||
        !(msLogType.equals(LOGTYPE_DEBUG) || 
          msLogType.equals(LOGTYPE_ERROR) || 
          msLogType.equals(LOGTYPE_OPERATION)))
    {
      return getPropertyError(PROP_LOGTYPE, msLogType);
    }
    mpLogger = Logger.getLogger();
    mnLimit = getConfigValue(PROP_LIMIT);
    if (mnLimit < 1)
    {
      return getPropertyError(PROP_LIMIT, "" + mnLimit);
    }
    
    // Interval
    mnInitialInterval = 60000;
    int vnSecs = getConfigValue(INTERVAL);
    msIntervalString = vnSecs + " seconds ";
    mnInterval = vnSecs * 1000;
    if (vnSecs < 1)
    {
      return getPropertyError(INTERVAL, msIntervalString);
    }
    return null;
  }
  

  /**
   * Record JVM stats
   */
  @Override
  public void run()
  {
    if (mpJBossDBObj == null)
    {
      try
      {
        mpJBossDBObj = new DBObjectTL().getDBObject(JBossMonitorFrame.JBOSS_DB);
        mpJBossDBObj.connect();
      }
      catch (DBException dbe)
      {
        mpJBossDBObj = null;
        mpLogger.logException(dbe);
      }
    }
    
    try
    {
      StringBuffer vpLogMessage = new StringBuffer();
      List<Map> vpResults = mpJBossDBObj.execute(
          JBossMonitorFrame.JBOSS_QUERY_DEST).getRows();

      int vnTotalCount = 0;
      for (Map m : vpResults)
      {
        String vsDest = m.get(JBossMonitorFrame.DESTINATION_NAME).toString();
        int vnDCount = Integer.parseInt(m.get(JBossMonitorFrame.RESULT_NAME).toString());
        vnTotalCount += vnDCount;
        vpLogMessage.append(" " + vsDest + ": " + vnDCount
            + SKDCConstants.EOL_CHAR);
      }
      mnMinCount = Math.min(mnMinCount, vnTotalCount);
      mnMaxCount = Math.max(mnMaxCount, vnTotalCount);
      vpLogMessage.insert(0, "JBoss message count: " + mnMinCount + " < "
          + vnTotalCount + " < " + mnMaxCount + SKDCConstants.EOL_CHAR);
      
      // Log
      if (msLogType.equals(LOGTYPE_ERROR) || vnTotalCount >= mnLimit)
      {
        mpLogger.logError(vpLogMessage.toString());
      }
      else if (msLogType.equals(LOGTYPE_DEBUG))
      {
        mpLogger.logDebug(vpLogMessage.toString());
      }
      else if (msLogType.equals(LOGTYPE_OPERATION))
      {
        mpLogger.logOperation(vpLogMessage.toString());
      }
    }
    catch (DBException dbe)
    {
      mpLogger.logException(dbe);
    }
  }
  
  /**
   * Clean up JBoss database connection
   * 
   * @see com.daifukuamerica.wrxj.scheduler.event.TimedEventTask#removeDBConnection()
   */
  @Override
  protected void removeDBConnection()
  {
    super.removeDBConnection();
    
    boolean threadCheckingOn = true;
    if(mpJBossDBObj != null)
    {
      if(mpJBossDBObj.checkConnected())
      {
        try
        {
          mpJBossDBObj.disconnect(threadCheckingOn);
          if ((!threadCheckingOn) ||
            (!SwingUtilities.isEventDispatchThread()))
          {
            mpJBossDBObj = null;
          }
        }
        catch (DBException e)
        {
          mpLogger.logException(e, "Error closing Database Connection");
        }
      }
    }
  }
}