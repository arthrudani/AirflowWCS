package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.util.SKDCConstants;


/**
 * Class to take periodic snapshots of the JVM for debug purposes
 * <P>Required ControllerConfig properties:
 * <UL>
 * <LI><tt>interval</tt> - in seconds</LI>
 * <LI><tt>LogType</tt> - must be Debug, Error, or Operation</LI>
 * </UL> 
 * 
 * @author mandrus
 */
public class JavaMonitorTask extends TimedEventTask
{
  private static final String PROP_LOGTYPE = "LogType";
  private static final String LOGTYPE_DEBUG = "Debug";
  private static final String LOGTYPE_ERROR = "Error";
  private static final String LOGTYPE_OPERATION = "Operation";
  
  private int mnMaxThreads = 0;
  private int mnMinThreads = Integer.MAX_VALUE;
  
  private String msLogType;
  
  /**
   * Constructor
   * 
   * @param isName
   */
  public JavaMonitorTask(String isName)
  {
    super(isName);
  }

  /**
   * Initialize the task
   */
  @Override
  public String initTask()
  {
    // Logger
    msLogType = getConfigString(PROP_LOGTYPE);
    if (msLogType == null ||
        !(msLogType.equals(LOGTYPE_DEBUG) || 
          msLogType.equals(LOGTYPE_ERROR) || 
          msLogType.equals(LOGTYPE_OPERATION)))
    {
      return getPropertyError(PROP_LOGTYPE, msLogType);
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
    // Threads
    int vnThreads = Thread.currentThread().getThreadGroup().activeCount();
    mnMinThreads = Math.min(mnMinThreads, vnThreads);
    mnMaxThreads = Math.max(mnMaxThreads, vnThreads);
    String vsThreads = " Threads: " + mnMinThreads + " < " + vnThreads + " < "
        + mnMaxThreads + SKDCConstants.EOL_CHAR;
    
    // Memory
    Runtime vpRuntime = Runtime.getRuntime();
    long vlFreeMemory = vpRuntime.freeMemory();
    long vlTotalMemory = vpRuntime.totalMemory();
    
    String vsUsedMem = " Used Memory: "
        + formatMemory(vlTotalMemory - vlFreeMemory) + SKDCConstants.EOL_CHAR;
    String vsFreeMem = " Free Memory: " + formatMemory(vlFreeMemory)
        + SKDCConstants.EOL_CHAR;
    String vsTotalMem = " Total Memory: " + formatMemory(vlTotalMemory)
        + SKDCConstants.EOL_CHAR;
    String vsMaxMem = " Max Memory: " + formatMemory(vpRuntime.maxMemory())
        + SKDCConstants.EOL_CHAR;
    
    // Log
    String vsLogMessage = "JVM information: " + SKDCConstants.EOL_CHAR
        + vsThreads + vsUsedMem + vsFreeMem + vsTotalMem + vsMaxMem;
    if (msLogType.equals(LOGTYPE_DEBUG))
    {
      mpLogger.logDebug(vsLogMessage);
    }
    else if (msLogType.equals(LOGTYPE_ERROR))
    {
      mpLogger.logError(vsLogMessage);
    }
    else if (msLogType.equals(LOGTYPE_OPERATION))
    {
      mpLogger.logOperation(vsLogMessage);
    }
  }
  
  /**
   * Formats memory measurement.
   *
   * <p><b>Details:</b> <code>formatMemory</code> formats the supplied long
   * as a count of bytes by inserting commas for thousands separators and
   * appending "&nbsp;bytes" to the resulting string.</p>
   *
   * @param ilMem the number of bytes
   * @return the formatted string
   */
  private String formatMemory(final long ilMem)
  {
    final StringBuffer vpBuff = new StringBuffer();
    vpBuff.append(ilMem);
    for (int vnI = vpBuff.length() - 3; vnI > 0; vnI -= 3)
      vpBuff.insert(vnI, ',');
    vpBuff.append(" bytes");
    return vpBuff.toString();
  }
}
