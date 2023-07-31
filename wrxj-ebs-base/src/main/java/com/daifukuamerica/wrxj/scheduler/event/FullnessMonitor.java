package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * <B>Description:</B> Timed event to update the status of the fullness for 
 * the Equipment Monitor.  This class prevents 15 individual Equipment Monitors
 * from beating the hell out of our poor database.
 * 
 * <P>Sample equipment configuration (must remove parenthetical notes):
 * <pre>
 *  EquipmentName   SRC-5:Fullness
 *  Description     SwapZoneMonitor
 *  Category        Equipment           (pretend this is monitoring equipment)
 *  ErrorSet        AS21
 *  MCController    Monitor:Fullness    (FULLNESS_MONITOR_NAME)
 *  MOSController   *NONE*
 *  AisleGroup      *NONE*
 *  HostID          *NONE*
 *  DeviceID        *NONE*
 *  StationID       *NONE*
 *  MCID            SRC5                (device)
 *  MOSID           SRC5                (device)
 *  GraphicClass    FullnessMonitorButton
 * </pre></P>
 * 
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class FullnessMonitor extends TimedEventTask
{
  /* 
    -- SQL to enable the FullnessMonitor
    INSERT [asrs].[CONTROLLERCONFIG] ([sController], [sPropertyName], [sPropertyValue], [sPropertyDesc], [iScreenChangeAllowed], [iEnabled])
     VALUES (N'TimedEventScheduler', N'Task.FullnessMonitor.class', N'FullnessMonitor', N'Task to monitor fullness and publish statuses for them for the Equipment Monitor FullnessMonitorButtons.', 1, 1);
    INSERT [asrs].[CONTROLLERCONFIG] ([sController], [sPropertyName], [sPropertyValue], [sPropertyDesc], [iScreenChangeAllowed], [iEnabled])
     VALUES (N'TimedEventScheduler', N'Task.FullnessMonitor.devices', N'SV01,SV02,SV03,SV04,SV05,SV06,SV07,SV08,SV09,SV10,SV11,SV12,SV13,SV14,SV15', N'Devices to to monitor', 1, 1);
    INSERT [asrs].[CONTROLLERCONFIG] ([sController], [sPropertyName], [sPropertyValue], [sPropertyDesc], [iScreenChangeAllowed], [iEnabled])
     VALUES (N'TimedEventScheduler', N'Task.FullnessMonitor.errorPercent', N'96', N'Percent at which the monitor button turns red', 1, 1);
    INSERT [asrs].[CONTROLLERCONFIG] ([sController], [sPropertyName], [sPropertyValue], [sPropertyDesc], [iScreenChangeAllowed], [iEnabled])
     VALUES (N'TimedEventScheduler', N'Task.FullnessMonitor.interval', N'1', N'The interval at which to refresh the FullnessMonitor.  Default is 1 which means run every minute. (2 means run every two minutes etc.)', 1, 1);
    INSERT [asrs].[CONTROLLERCONFIG] ([sController], [sPropertyName], [sPropertyValue], [sPropertyDesc], [iScreenChangeAllowed], [iEnabled])
     VALUES (N'TimedEventScheduler', N'Task.FullnessMonitor.warningPercent', N'91', N'Percent at which the monitor button turns yellow', 1, 1);
   */
  
  protected static final int DEFAULT_WARNING_LEVEL = 91;
  protected static final int DEFAULT_ERROR_LEVEL = 96;

  // The devices for which we'll be calculating fullness (comma separated list)
  private static final String DEVICES = "devices";
  private static final String WARNING_LEVEL = "warningPercent"; 
  private static final String ERROR_LEVEL = "errorPercent"; 

  // Messaging
  private static final String FULLNESS_MONITOR_NAME = "Monitor:Fullness";
  private SystemGateway mpSystemGateway;
  
  protected StandardLocationServer mpLocServer = null;
  protected String[] masDevices = null;
  protected Map<String,Integer> mpLocationTotals = new TreeMap<String, Integer>();
  protected int mnWarningLevel;
  protected int mnErrorLevel;

  /**
   * Constructor
   * 
   * @param isName
   */
  public FullnessMonitor(String isName)
  {
    super(isName);
    mpSystemGateway = SystemGateway.create(FULLNESS_MONITOR_NAME, mpLogger);
  }

  /*========================================================================*/
  /* Overridden methods                                                     */
  /*========================================================================*/
  
  /**
   * @see com.daifukuamerica.wrxj.scheduler.event.TimedEventTask#initTask()
   */
  @Override
  public String initTask()
  {
    String vsMyName = getClass().getSimpleName();
    
    mnInitialInterval = 6000;
    int vnMinutes = getConfigValue(INTERVAL);
    msIntervalString = vnMinutes + " seconds ";
    mnInterval = vnMinutes * 60000;
    if (vnMinutes < 1)
      return vsMyName + ": INVALID interval (" + vnMinutes + "). " + vsMyName
          + " will not be started.";
    
    String vsDevices = getConfigString(DEVICES);
    if (vsDevices == null)
    {
      return vsMyName + ": devices not specified. " + vsMyName
          + " will not be started.";
    }
    masDevices = vsDevices.split(",");
    if (masDevices.length == 1 && masDevices[0].trim().length() == 0)
    {
      return vsMyName + ": devices not specified. " + vsMyName
          + " will not be started.";
    }
    
    mnWarningLevel = getConfigValue(WARNING_LEVEL);
    if (mnWarningLevel <= 0)
    {
      mnWarningLevel = DEFAULT_WARNING_LEVEL;
    }

    mnErrorLevel = getConfigValue(ERROR_LEVEL);
    if (mnErrorLevel <= 0)
    {
      mnErrorLevel = DEFAULT_ERROR_LEVEL;
    }

    return null;
  }

  /**
   * @see com.daifukuamerica.wrxj.scheduler.event.TimedEventTask#run()
   */
  @Override
  public void run()
  {
    if (mpLocServer == null)  // Reconfigure on first time through
    {
      mpLocServer = Factory.create(StandardLocationServer.class);
      initializeLocationTotals();
    }
    
    try
    {
      // For each monitored device
      for (String vsDevice : masDevices)
      {
        int vnTotalLocations = mpLocationTotals.get(vsDevice);
        if (vnTotalLocations == 0)
        {
          mpLogger.logError("Device \"" + vsDevice + "\" has no locations!");
          sendStatus(vsDevice, Integer.MAX_VALUE);
        }
        else
        {
          // Figure out the fullness
          int vnEmpties = mpLocServer.getLocationCount(null, vsDevice, 0,
              DBConstants.LCASRS, DBConstants.LCAVAIL, DBConstants.UNOCCUPIED, -1);
          int vnPercentFull = (vnTotalLocations - vnEmpties) * 100
              / vnTotalLocations;
  
          // Send the message
          sendStatus(vsDevice, vnPercentFull);
        }
      }
    }
    catch (DBException dbe)
    {
      mpLogger.logException(dbe);
    }
  }

  /*========================================================================*/
  /* New methods                                                            */
  /*========================================================================*/

  /**
   * Initialize the location total availability
   */
  protected void initializeLocationTotals()
  {
    try
    {
      // For each monitored device
      for (String vsDevice : masDevices)
      {
        // Get the total possible locations only once.
        Integer vnTotal = mpLocationTotals.get(vsDevice);
        if (vnTotal == null)
        {
          int vnTotalLoc = mpLocServer.getLocationCount(null, vsDevice, 0,
              DBConstants.LCASRS, 0, 0, -1);
          int vnProhibitedLoc = mpLocServer.getLocationCount(null, vsDevice, 0,
              DBConstants.LCASRS, DBConstants.LCPROHIBIT, 0, -1);
          vnTotal = vnTotalLoc - vnProhibitedLoc;
          mpLocationTotals.put(vsDevice, vnTotal);
        }
      }
    }
    catch (DBException dbe)
    {
      mpLogger.logException(dbe);
    }
  }
  
  /**
   * Send a status message for a given swap zone
   * @param isDevice
   * @param inPercentFull
   */
  public void sendStatus(String isDevice, int inPercentFull)
  {
    String vsStatus;
    if (inPercentFull >= mnErrorLevel)
      vsStatus = StatusEventDataFormat.STATUS_ERROR;
    else if (inPercentFull >= mnWarningLevel)
      vsStatus = StatusEventDataFormat.STATUS_DISCONNECT;
    else
      vsStatus = StatusEventDataFormat.STATUS_ONLINE;
    
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(FULLNESS_MONITOR_NAME);
    vpSEDF.setType(ControllerConsts.MACHINE_STATUS);
    vpSEDF.addMachineStatus(isDevice, "Fullness", isDevice, 0,
        vsStatus + "|" + inPercentFull + "%", StatusEventDataFormat.NONE);
    
    mpSystemGateway.publishStatusEvent(vpSEDF.createStringToSend());
  }
  
  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable
  {
    if (mpSystemGateway != null)
    {
      SystemGateway.destroy(mpSystemGateway);
    }
  }
}
