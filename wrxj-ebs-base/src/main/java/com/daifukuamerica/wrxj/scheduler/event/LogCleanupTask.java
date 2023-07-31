/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLogServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * <B>Description:</B> Task to clean up log messages that are persisted to the
 * database.  This task is not required when persisting to the Wynsoft AED 
 * database.
 * 
 * <p>To enable this task:
 * <pre>
    INSERT asrs.CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled, dModifyTime, sAddMethod, sUpdateMethod)
    VALUES ('TimedEventScheduler', 'Task.LogCleanup.class', 'LogCleanupTask', 'Task to clean up old logs', 1, 1, NULL, NULL, NULL)
    INSERT asrs.CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled, dModifyTime, sAddMethod, sUpdateMethod)
    VALUES ('TimedEventScheduler', 'Task.LogCleanup.DaysToKeep', '7', 'Number of days to keep logs', 1, 1, NULL, NULL, NULL)
    INSERT asrs.CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled, dModifyTime, sAddMethod, sUpdateMethod)
    VALUES ('TimedEventScheduler', 'Task.LogCleanup.interval', '1', 'Interval in hours', 1, 1, NULL, NULL, NULL)
   </pre>
 * </p>
 *
 * @author       mandrus
 * @version      1.0
 */
public class LogCleanupTask extends TimedEventTask
{
  private static final String DAYS_TO_KEEP = "DaysToKeep";
  
  private int mnDaysToKeep;
  
  /**
   * Constructor
   * @param isName
   */
  public LogCleanupTask(String isName)
  {
    super(isName);
  }

  /**
   * Initialize
   */
  @Override
  public String initTask()
  {
    // In multi-JVM systems with a shared WRx database, only one task should be started.
    try
    {
      StandardConfigurationServer vpConfigSrvr = Factory.create(StandardConfigurationServer.class);
      if (vpConfigSrvr.isSplitSystem() && vpConfigSrvr.isThisPrimaryJVM() == false)
      {
        mpLogger.logOperation("INVALID JVM (" + 
                Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY) +
                ") - a Secondary JVM - LogCleanupTask will not be started.");
        // Silent non-start
        return " ";
      }
    }
    catch (DBException e)
    {
      return e.getMessage();
    }
    
    mzFixed = true;
    
    // Interval
    mnInitialInterval = javax.management.timer.Timer.ONE_MINUTE;
    int vnInterval = getConfigValue(INTERVAL);
    msIntervalString = vnInterval + " hours ";
    if (vnInterval < 1)
      return configError(INTERVAL, vnInterval);
    // Convert interval to ms
    mnInterval = vnInterval * javax.management.timer.Timer.ONE_HOUR;

    // DaysToKeep
    mnDaysToKeep = getConfigValue(DAYS_TO_KEEP);
    if (mnDaysToKeep < 1)
      return configError(DAYS_TO_KEEP, mnDaysToKeep);

    return null;
  }

  /**
   * Create an error message due to a bad configuration
   * @param isMessage
   * @param inValue
   * @return
   */
  private String configError(String isMessage, int inValue)
  {
    return String.format("INVALID LogCleanupTask.%1$s=[%2$d]! LogCleanupTask will not be started.");
  }
  
  /**
   * Task
   */
  @Override
  public void run()
  {
    StandardLogServer vpLogServer = Factory.create(StandardLogServer.class);
    try
    {
      vpLogServer.purge(mnDaysToKeep);
      vpLogServer.purgeEquipLogs(mnDaysToKeep);
      vpLogServer.cleanUp();
    }
    catch (Exception e)
    {
      mpLogger.logException("Error cleaning up database logs", e);
    }
  }
}
