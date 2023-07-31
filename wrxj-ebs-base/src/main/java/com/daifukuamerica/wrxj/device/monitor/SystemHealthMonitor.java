package com.daifukuamerica.wrxj.device.monitor;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.controller.DaemonController;
import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>SystemHealthMonitor is a Controller that observes all Controllers in the 
 * system and watches their condition.  This device publishes "Heartbeat" 
 * messages to all Controllers in the system and subscribes for the response.  
 * The "Heartbeat" responses are then given to an observer for analysis (right 
 * now the MonitorFrame display).</p>
 *
 * <p><b>HEALTH To-Do</b>: Monitor Controllers to ensure they are not starved, 
 * deadlocked, or spinning. Ensure OS is healthy (by using OS calls?).  Check 
 * that interrupts are being serviced.</p>
 *
 * <p><b>STATE To-Do</b>: Provide global state of system to all sub-systems.</p>
 *
 * @author Stephen Kendorski
 */
public class SystemHealthMonitor extends DaemonController
{
  protected static final String HEARTBEAT_INTERVAL_PROP = "HeartbeatInterval";
  protected static final int DEFAULT_HEARTBEAT_INTERVAL = 30000;
  protected static final int MINIMUM_HEARTBEAT_INTERVAL = 1000;

  protected static final String STATUS_CHANGE_INTERVAL_PROP = "SystemMonitorChangeInterval";
  protected static final int DEFAULT_STATUS_CHANGE_INTERVAL = 200;
  protected static final int MINIMUM_STATUS_CHANGE_INTERVAL = 200;
  
  /**
   * A tool for computing and formatting the time taken to receive a heartbeat
   * request.  The elapsed time is then sent back as part of the heartbeat 
   * response.
   */
  protected HeartbeatIntervalTimeout heartbeatIntervalTimeout = new HeartbeatIntervalTimeout();

  protected MonitorStatusChangesIntervalTimeout monitorStatusChangesIntervalTimeout = new MonitorStatusChangesIntervalTimeout();

  protected StatusModel equipmentStatusModel = null;

  protected Map<String, StatusModel> controllerStatusModels = new TreeMap<String, StatusModel>();

  /**
   * Construct an instance of SystemHealthNonitor.
   */
  protected SystemHealthMonitor()
  {
  }

  /**
   * <p>The Base Controller initialized the controller by setting the 
   * controller's KeyName, attaching a logger, starting up the topic subscribers 
   * and publishers for the message service, and starting the Thread where 
   * messages will be processed.</p>
   *
   * <p>A child Controller should find its collaborators (but NOT use them),
   * subscribe to any events it needs from its collaborators, and perform any
   * other simple tasks which do NOT require logging.</p>
   *
   * @param aControllerKeyName the unique name that identifies this instance of 
   *   Controller
   */
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("SystemHealthMonitor.initialize() - Start");
    subscribeForAllStatusEvents();
    subscribeForAllExceptionEvents();
    subscribeControlEvent();
    logger.logDebug("SystemHealthMonitor.initialize() - End");
  }

  /**
   * @see com.daifukuamerica.wrxj.controller.Controller#startup()
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("SystemHealthMonitor.startup() - Start");
    
    equipmentStatusModel = Factory.create(StatusModel.class);
    equipmentStatusModel.setEquipmentStatuses("*UNKNOWN*");
    // If we have equipment - add it to the status model from the configuration.
    logger.logDebug("Adding Equipment");
    equipmentStatusModel.initializeStationStatuses(false);
    equipmentStatusModel.addSiteEquipment();

    // Heartbeat
    int vnHeartbeatInterval = getConfigurationWithLogging(
        HEARTBEAT_INTERVAL_PROP, MINIMUM_HEARTBEAT_INTERVAL,
        DEFAULT_HEARTBEAT_INTERVAL);
    timers.setPeriodicTimerEvent(heartbeatIntervalTimeout, vnHeartbeatInterval);

    // SystemMonitorChanges
    int vnMonitorStatusChangesInterval = getConfigurationWithLogging(
        STATUS_CHANGE_INTERVAL_PROP, MINIMUM_STATUS_CHANGE_INTERVAL,
        DEFAULT_STATUS_CHANGE_INTERVAL);
    timers.setPeriodicTimerEvent(monitorStatusChangesIntervalTimeout,
        vnMonitorStatusChangesInterval);
    
    // Get an initial heartbeat.
    publishHeartbeatRequestEvent();
    publishRequestEvent("%");
    setControllerStatus(ControllerConsts.STATUS_RUNNING);
    logger.logDebug("SystemHealthMonitor.startup() - End");
  }

  /**
   * If tracking is published more often than status changes are published, we
   * end up with duplicates.  This method is to provide some visibility for
   * the status change interval to the MOS controllers to prevent that problem.
   * 
   * @return
   */
  public static int getStatusChangesInterval()
  {
    int vnProperty = DEFAULT_STATUS_CHANGE_INTERVAL;
    if (Application.getString(STATUS_CHANGE_INTERVAL_PROP) != null)
    {
      vnProperty = Application.getInt(STATUS_CHANGE_INTERVAL_PROP);
      if (vnProperty < MINIMUM_STATUS_CHANGE_INTERVAL)
      {
        vnProperty = DEFAULT_STATUS_CHANGE_INTERVAL;
      }
    }
    
    return vnProperty;
  }
  
  /**
   * Get an application property with checking and logging
   * 
   * @param isPropertyName
   * @param inMinimum
   * @param inDefault
   * @return
   */
  protected int getConfigurationWithLogging(String isPropertyName, int inMinimum,
      int inDefault)
  {
    int vnProperty = inDefault;
    if (Application.getString(isPropertyName) != null)
    {
      vnProperty = Application.getInt(isPropertyName);
      if (vnProperty < inMinimum)
      {
        logger.logError("Invalid " + isPropertyName + " (" + vnProperty
            + ").  Must be >= " + inMinimum + ".  Using default.");
        vnProperty = inDefault;
      }
    }
    else
    {
      logger.logDebug(isPropertyName + " is undefined.  Using default.");
    }
    logger.logDebug(isPropertyName + " is " + vnProperty + ".");
    
    return vnProperty;
  }
  
  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Device/Equipment.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("SystemHealthMonitor.shutdown() -- Start");
    timers.cancel(monitorStatusChangesIntervalTimeout);
    monitorStatusChangesIntervalTimeout = null;
    timers.cancel(heartbeatIntervalTimeout);
    heartbeatIntervalTimeout = null;
    // Publish a final status that this controller is shutdown.
    setControllerStatus(ControllerConsts.STATUS_SHUT_DOWN);
    StatusModel controllerStatusModel = controllerStatusModels.get(WRX_RUN_MODE);
    if (controllerStatusModel != null)
    {
      controllerStatusModel.setStatusTxTime(new Date().getTime());
      controllerStatusModel.setStatusString(getControllerStatusText());
      controllerStatusModel.setControllerName(getName());
      controllerStatusModel.updateStatus();
    }
    publishStatusChanges();
    Iterator<StatusModel> controllerStatusModelsIterator = controllerStatusModels.values().iterator();
    while (controllerStatusModelsIterator.hasNext())
    {
      StatusModel statusModel = controllerStatusModelsIterator.next();
      if (statusModel != null)
      {
        statusModel.cleanUp();
        statusModel = null;
      }
    }
    equipmentStatusModel.cleanUp();
    equipmentStatusModel = null;
    logger.logDebug("SystemHealthMonitor.shutdown() -- End");
    super.shutdown();
  }

  /**
   * Process System Inter-Process-Communication Message.
   */
  @Override
  protected void processIPCReceivedMessage()
  {
    // (Decide how to) Process message here
    //
    // receivedText = receivedMessage.getMessageText();
    // receivedData = receivedMessage.getMessageData();
    // receivedEventType = receivedMessage.getEventType();
    // receivedEvent = receivedMessage.getEvent();
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE: processStatusEvent(); break;
        default: receivedMessageProcessed = false;
      }
    }
  }
  
  /**
   * @see com.daifukuamerica.wrxj.ipc.AbstractIPCMessenger#processStatusEvent()
   */
  @Override
  protected void processStatusEvent()
  {
    super.processStatusEvent();  // Make sure we let the base class process this.
    StatusModel statusModel = null;
    
    if (receivedText == null)
    {
      logger.logError("receivedText is null!");
      return;
    }
    
    switch (receivedText.charAt(0))
    {
      case ControllerConsts.CONTROLLER_STATUS:
      case ControllerConsts.DETAILED_STATUS:
      case ControllerConsts.HEARTBEAT_STATUS:
      case ControllerConsts.PRODUCTIVITY_STATUS:
        statusModel = controllerStatusModels.get(receivedControllerGroupName);
        if (statusModel == null)
        {
          statusModel = Factory.create(StatusModel.class);
          controllerStatusModels.put(receivedControllerGroupName, statusModel);
        }
        break;
      case ControllerConsts.BIDIRECTIONAL_STATUS:
      case ControllerConsts.EQUIPMENT_STATUS:
      case ControllerConsts.MACHINE_STATUS:
      case ControllerConsts.TRACKING_STATUS:
        statusModel = equipmentStatusModel;
        break;
      default:
        break;
    }
    if (statusModel != null)
    {
      statusModel.setStatusTxTime(receivedTxTime);
      statusModel.setStatusRxTime(receivedRxTime);
      statusModel.setStatusString(receivedText);
      statusModel.setStatusDataLong(receivedDataLong);
      statusModel.setControllerName(receivedCKN);
      statusModel.updateStatus();
    }
    else
    {
      logger.logError("NO StatusModel defined for StatusEvent \nreceivedText \"" + receivedText + "\"\n" +
          " Sender: " + receivedCKN +
          "\n Event Type " + receivedEventType + ":" +
          MessageEventConsts.EVENT_TEXT[receivedEventType] +
          "\n Event " + receivedEvent);
    }
  }

  /**
   * We have received a CONTROL EVENT (probably from a User's Form)
   */
  @Override
  protected void processControlEvent()
  {
    if (controllerStatus != ControllerConsts.STATUS_RUNNING)
    {
      logger.logDebug("Controller NOT Running -Event DISCARDED - processControlEvent()");
      return;
    }
    switch (receivedData)
    {
      case ControlEventDataFormat.SHM_STATUS_REQUEST:
        String vsRequestedControllerGroup = 
          ControlEventDataFormat.getGroupFromStatusMessage(receivedText);
        publishStatusesUpdate(vsRequestedControllerGroup);
        break;
      case ControlEventDataFormat.SHM_REQUEST_GROUPS:
        publishAllControllerGroups(receivedCKN);
        break;
      default:
        logger.logError("SystemHealthMonitor.processControlEvent() -- UNKNOWN Event Type \"" 
            + receivedData + "\" -- processControlEvent()");
      break;
    }
  }

  /**
   * Get the controller groups
   * 
   * @param isSubscriber
   */
  private void publishAllControllerGroups(String isSubscriber)
  {
    Iterator<String> controllerGroupsIterator = controllerStatusModels.keySet().iterator();
    String s = "";
    while (controllerGroupsIterator.hasNext())
    {
      String vsControllerGroup = controllerGroupsIterator.next();
      s = s + vsControllerGroup + "\n";
    }
    publishLogEvent(s, LogConsts.SEND_CONTROLLER_GROUPS, isSubscriber);
  }

  /**
   * Publish a "Heartbeat Request" Message to the Inter-Process-Communication
   * message service <i>Server</i>.
   */
  protected void publishHeartbeatRequestEvent()
  {
    publishEvent(msName, "*Request Heartbeat*", 0,
        MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE,
        MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE_TEXT);
  }

  /**
   * Publish status updates for a given controller group
   * 
   * @param isRequestedControllerGroup
   */
  protected void publishStatusesUpdate(String isRequestedControllerGroup)
  {
    StatusModel statusModel = controllerStatusModels.get(isRequestedControllerGroup);
    if (statusModel != null)
    {
      String updates = statusModel.getAllStatusesUpdate(StatusModel.CONTROLLER_STATUS_UPDATE);
      if (updates != null)
      {
        publishUpdateEvent(receivedControllerGroupName, MessageEventConsts.STATUS_EVENT_TEXT +
          MessageEventConsts.SUB_EVENT_TEXT + ControllerConsts.CONTROLLER_STATUS,
          updates, ControllerConsts.CONTROLLER_STATUS);
      }
    }
    String updates = equipmentStatusModel.getAllStatusesUpdate(StatusModel.EQUIPMENT_STATUS_UPDATE);
    if (updates != null)
    {
      publishUpdateEvent(WRX_RUN_MODE, MessageEventConsts.STATUS_EVENT_TEXT +
        MessageEventConsts.SUB_EVENT_TEXT + ControllerConsts.EQUIPMENT_STATUS,
        updates, ControllerConsts.EQUIPMENT_STATUS);
    }
    updates = equipmentStatusModel.getAllStatusesUpdate(StatusModel.TRACKING_STATUS_UPDATE);
    if (updates != null)
    {
      publishUpdateEvent(WRX_RUN_MODE, MessageEventConsts.STATUS_EVENT_TEXT +
        MessageEventConsts.SUB_EVENT_TEXT + ControllerConsts.TRACKING_STATUS,
        updates, ControllerConsts.TRACKING_STATUS);
    }
  }

  /**
   * Publish all status changes
   */
  protected void publishStatusChanges()
  {
    String statusChanges = null;
    Iterator<String> groupNamesIterator = controllerStatusModels.keySet().iterator();
    Iterator<StatusModel> controllerStatusModelsIterator = controllerStatusModels.values().iterator();
    while (controllerStatusModelsIterator.hasNext())
    {
      StatusModel statusModel = controllerStatusModelsIterator.next();
      String groupName = groupNamesIterator.next();
      if (statusModel != null)
      {
        statusChanges = statusModel.getStatusChanges();
        if (statusChanges != null)
        {
          publishUpdateEvent(groupName,MessageEventConsts.STATUS_EVENT_TEXT +
             MessageEventConsts.SUB_EVENT_TEXT + ControllerConsts.CONTROLLER_STATUS,
             statusChanges, ControllerConsts.CONTROLLER_STATUS);
        }
      }
    }
    statusChanges = equipmentStatusModel.getStatusChanges();
    if (statusChanges != null)
    {
      publishUpdateEvent(WRX_RUN_MODE, MessageEventConsts.STATUS_EVENT_TEXT +
         MessageEventConsts.SUB_EVENT_TEXT + ControllerConsts.EQUIPMENT_STATUS,
         statusChanges, ControllerConsts.EQUIPMENT_STATUS);
    }
    statusChanges = equipmentStatusModel.getTrackingChanges();
    if (statusChanges != null)
    {
      publishUpdateEvent(WRX_RUN_MODE, MessageEventConsts.STATUS_EVENT_TEXT +
         MessageEventConsts.SUB_EVENT_TEXT + ControllerConsts.TRACKING_STATUS,
         statusChanges, ControllerConsts.TRACKING_STATUS);
    }
  }

  /**
   * Publish a status update message
   * 
   * @param groupName
   * @param updateType
   * @param messageText
   * @param messageData
   */
  protected final void publishUpdateEvent(String groupName, String updateType,
      String messageText, int messageData)
  {
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.UPDATE_EVENT_TYPE,
        MessageEventConsts.UPDATE_EVENT_TYPE_TEXT + updateType
            + MessageEventConsts.UPDATE_EVENT_TYPE_TEXT2 + ":" + groupName);
  }

  /**
   * Publish a heart-beat message (RestartableTimerTask)
   */
  private class HeartbeatIntervalTimeout extends RestartableTimerTask
  {
    public void run()
    {
      heartbeatIntervalTimeout_run();
    }
  }

  /**
   * run -- the LocalTimerTask's run() needs to be synchronized on
   * thisController so that any work we do here is not interrupted by any
   * incoming messages or events that we generate here. We want to complete
   * anything we do here without being pre-empted.
   */
  private void heartbeatIntervalTimeout_run()
  {
    synchronized (SystemHealthMonitor.this)
    {
      Iterator<StatusModel> controllerStatusModelsIterator = controllerStatusModels.values().iterator();
      while (controllerStatusModelsIterator.hasNext())
      {
        StatusModel statusModel = controllerStatusModelsIterator.next();
        if (statusModel != null)
        {
          statusModel.clearHeartbeatResponseTimes();
        }
      }
      publishHeartbeatRequestEvent();
    }
  }

  /**
   * Publish any status changes (RestartableTimerTask)
   */
  private class MonitorStatusChangesIntervalTimeout extends
      RestartableTimerTask
  {
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on
     * thisController so that any work we do here is not interrupted by any
     * incoming messages or events that we generate here. We want to complete
     * anything we do here without being pre-empted.
     */
    public void run()
    {
      synchronized (SystemHealthMonitor.this)
      {
        publishStatusChanges();
      }
    }
  }

  /**
   * Factory for ControllerImplFactory.
   * 
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively 
   * by <code>ControllerImplFactory</code>. Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object. If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   * 
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the
   *           controller
   */
  public static Controller create(ReadOnlyProperties ipConfig)
      throws ControllerCreationException
  {
    SystemHealthMonitor vpShm = new SystemHealthMonitor();
    return vpShm;
  }
}

