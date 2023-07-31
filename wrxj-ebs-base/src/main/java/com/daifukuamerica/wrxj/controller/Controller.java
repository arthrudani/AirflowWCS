package com.daifukuamerica.wrxj.controller;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.ipc.AbstractIPCMessenger;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;

/**
 * A Threaded inter-process-communication event processor. This is the main
 * class in the application where synchronized, event-driven, threaded work can
 * be performed. Any events processed by a Controller implementation are
 * guaranteed to be performed asynchronously and sequentially (one event will be
 * processed to completion without interruption by another event).
 * 
 * @author Stephen Kendorski
 */
public abstract class Controller extends AbstractIPCMessenger
{
  /**
   * Property identifier for equipment port CKN.
   * 
   * <p>
   * <b>Details:</b> <code>DEVICE_PORT</code> is set to "<tt>DevicePort</tt>",
   * the name of the property whose value is the controller's equipment port
   * CKN. This constant is defined here for convenience and to promote safe
   * coding by discouraging the use of string literals in related code.
   * </p>
   * 
   * <p>
   * Not all device definitions include a property by this name.
   * </p>
   * 
   * @see #setEquipmentPortCKN(String)
   */
  public static final String DEVICE_PORT = "DevicePort";
  public static final String DEVICE_PORT2 = "DevicePort2";

  /**
   * Property identifier for collaborator CKN.
   * 
   * <p>
   * <b>Details:</b> <code>COLLABORATOR</code> is set to "<tt>Collaborator</tt>",
   * the name of the property whose value is the controller's collaborator CKN.
   * This constant is defined here for convenience and to promote safe coding by
   * discouraging the use of string literals in related code.
   * </p>
   * 
   * <p>
   * Not all device definitions include a property by this name.
   * </p>
   * 
   * @see #setEquipmentPortCKN(String)
   */
  public static final String COLLABORATOR = "Collaborator";

  /**
   * Property identifier for controller's device.
   * 
   * <p>
   * <b>Details:</b> The device id property is used to associate a device
   * controller with it's device record in the database.
   */
  public static final String DEVICE_ID = "DeviceID";

  /**
   * Property identifier for MOS device associated with a controller.
   * 
   * <p>
   * <b>Details:</b> This property only applies to controllers that may have an
   * associated mos device such as an emulator.
   */
  public static final String MOS_DEVICE = "MOSDevice";

  public static final String SIMULATOR = "StationSimulator";
  public static final String EMULATOR = "-Emulator";

  /**
   * Controller configuration properties.
   * 
   * <p>
   * <b>Details:</b> <code>mpProperties</code> contains the configuration
   * details for this controller. These parameters were obtained from a
   * controller configuration source before this controller was instantiated.
   * </p>
   * 
   * @see #setProperties(ReadOnlyProperties)
   */
  protected ReadOnlyProperties mpProperties;

  /**
   * The unique <i>String</i> key that identifies an instantiated Controller's
   * partner.
   */
  protected String collaboratorCKN = null;

  /**
   * The unique <i>String</i> key that identifies an instantiated Controller's
   * communication port.
   */
  protected String equipmentPortCKN = null;

  /**
   * The current general condition of the Controller.
   */
  protected int controllerStatus = ControllerConsts.STATUS_UNKNOWN;

  /**
   * These strings are for PUBLISHING/SUBSCRIBING.
   */
  private String commEventTypeText;

  
  /*========================================================================*/
  /*  Getter & Setter Methods                                               */
  /*========================================================================*/

  /**
   * Specify the Controller's associate (another Controller). The Controller
   * uses the associate Controller to help it accomplish work that is tightly
   * coupled between the two Controllers. The associate is identified by its
   * ControllerKeyName.
   * 
   * @param s the associate's ControllerKeyName
   */
  public final void setCollaboratorCKN(String s)
  {
    collaboratorCKN = s;
  }

  /**
   * Specify the Controller's communication port (another Controller) for
   * controlling and monitoring a piece of external hardware. The Equipment Port
   * handles (amd encapsulates) all communication protocol and physical layers.
   * 
   * @param s the
   *            {@link com.daifukuamerica.wrxj.common.device.port.PortController PortController}'s
   *            ControllerKeyName.
   */
  public final void setEquipmentPortCKN(String s)
  {
    equipmentPortCKN = s;
  }


  /*========================================================================*/
  /* Controller Status Methods                                              */
  /*========================================================================*/
  /**
   * Get the status text for controller. Currently only used by
   * <code>SystemHealthMonitor.shutdown()</code>.
   * 
   * @return
   */
  protected final String getControllerStatusText()
  {
    return (ControllerConsts.CONTROLLER_STATUS + " " + ControllerConsts.STATUS_TEXT[controllerStatus]);
  }

  /**
   * Specify the general condition of the Controller. Also, log and publish the
   * status change.
   * 
   * @param value the new state
   */
  protected final void setControllerStatus(int value)
  {
    controllerStatus = value;
    publishControllerStatus();
  }

  /**
   * Specify the elaborated/exact state of the Controller. Also, publish the
   * detailed status.
   * 
   * @param s the new elaborated state
   */
  protected final void setDetailedControllerStatus(String s)
  {
    publishDetailedStatusEvent(ControllerConsts.DETAILED_STATUS + " " + s, 0);
  }

  /**
   * Specify the general condition of the Controller using
   * <code>synchronized</code>. This method is provided so that an external
   * sub-system can change the state of the Controller.
   * 
   * @param value the new state
   */
  final void setStatus(int value)
  {
    synchronized (this)
    {
      setControllerStatus(value);
    }
  }
  
  
  /*========================================================================*/
  /*  Specific Event Publishing                                             */
  /*========================================================================*/
  /**
   * Publish a "Comm" (Communication) Message to the Inter-Process-Communication
   * message service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  protected final void publishCommEvent(String messageText, int messageData)
  {
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.COMM_EVENT_TYPE, commEventTypeText);
  }

  /**
   * Publish the general condition of the Controller.
   */
  private void publishControllerStatus()
  {
    publishStatusEvent(ControllerConsts.CONTROLLER_STATUS + " "
        + ControllerConsts.STATUS_TEXT[controllerStatus], controllerStatus);
  }

  /**
   * Publish a "Detailed Status" Message to the Inter-Process-Communication
   * message service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  private void publishDetailedStatusEvent(String messageText, int messageData)
  {
    publishEvent(msName, messageText, messageData,
        MessageEventConsts.STATUS_EVENT_TYPE,
        MessageEventConsts.CONTROLLER_DETAILED_STATUS_EVENT_TYPE_TEXT + msName);
  }

  /**
   * Publish an "Equipment" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  protected final void publishEquipmentEvent(String messageText, int messageData)
  {
    publishEquipmentEvent(messageText, messageData, equipmentPortCKN);
  }
  /**
   * Publish an "Custom Equipment" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   * 
   * @param messageText the String data content to be sent
   * @param messageData the int data content to be sent
   */
  protected final void publishCustomEquipmentEvent(String messageText, int messageData)
  { 
	publishEvent(msName, messageText, messageData, MessageEventConsts.CUSTOM_ALLOCATION_EVENT_TYPE, MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + equipmentPortCKN);  
  }

  
  
  /**
   * Publish a "Controller Request" Message to the Inter-Process-Communication
   * message service <i>Server</i>.
   * 
   * @param sCKN
   */
  protected final void publishRequestEvent(String sCKN)
  {
    if (sCKN != null)
    {
      publishEvent(msName, "Controller Request", 0,
          MessageEventConsts.CONTROLLER_REQUEST_EVENT_TYPE,
          MessageEventConsts.REQUEST_EVENT_TYPE_TEXT + sCKN);
    }
    else
    {
      logger.logError("publishRequestEvent() -- sCKN == null");
    }
  }
  

  /*========================================================================*/
  /* Subscriptions                                                          */
  /*========================================================================*/
  /**
   * Subscribe for Allocation request events
   * 
   * @param sCKN the Controller key name
   * @param noLocal boolean used to inhibit or allow subscriber to receive this
   *            same event if it's published by the message subscriber.
   */
  protected final void subscribeAllocateEvent(String sCKN, boolean noLocal)
  {
    startupTopicSubscriber(MessageEventConsts.ALLOCATE_EVENT_TYPE_TEXT + sCKN,
        noLocal);
  }

  /**
   * Subscribe for Allocation Probe response events
   * 
   * @param sCKN the Controller key name
   */
  protected final void subscribeAllocationProbeEvent(String sCKN)
  {
    startupTopicSubscriber(MessageEventConsts.ALLOCATION_PROBE_EVENT_TYPE_TEXT
        + sCKN, true);
  }

  /**
   * Subscribe for Comm events
   */
  protected final void subscribeCommEvent()
  {
    startupTopicSubscriber(commEventTypeText, false);
  }

 /**
  * Custom event to be used in projects.
  * @param sCKN the controller name
  */
  protected final void subscribeCustomEvent(String sCKN)
  {
    startupTopicSubscriber(MessageEventConsts.CUSTOM_EVENT_TYPE_TEXT + sCKN, true);
  }
  
  /**
   * Subscribe for message events filtered by the "Control" selector.
   * 
   * @param sCKN controller name - possibly name:group
   */
  protected final void subscribeControlEvent(String sCKN)
  {
    startupTopicSubscriber(MessageEventConsts.CONTROL_EVENT_TYPE_TEXT + sCKN,
        false);
  }

  /**
   * Subscribe for Control events for self
   */
  protected final void subscribeControlEvent()
  {
    subscribeControlEvent(msName);
  }

  /**
   * Subscribe for message events filtered by the "Equipment" selector.
   */
  protected final void subscribeEquipmentEvent()
  {
    subscribeEquipmentEvent(equipmentPortCKN);
  }

  /**
   * Subscribe for message events filtered by the "Equipment" selector.
   * 
   * @param sCKN the message destination
   */
  protected final void subscribeEquipmentEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT
          + sCKN, true);
    }
    else
    {
      logger.logError("subscribeEquipmentEvent() -- sCKN == null");
    }
  }

  /**
   * Subscribe for message events filtered by the "Exception" selector.
   */
  protected final void subscribeForAllExceptionEvents()
  {
    startupTopicSubscriber(MessageEventConsts.EXCPTN_EVENT_TYPE_TEXT + "%",
        false); // "%" says all
  }

  /**
   * Subscribe for message events filtered by the "Status" selector. Select ALL
   * types of status (Device, Machine, etc.).
   */
  protected final void subscribeForAllStatusEvents()
  {
    startupTopicSubscriber(MessageEventConsts.ALL_STATUSES_EVENT_TYPE_TEXT
        + "%", false); // "%" says all
  }

  /**
   * Subscribe for message events for this Controller filtered by the "Log"
   * selector.
   */
  protected final void subscribeForLogEvent()
  {
    startupTopicSubscriber(MessageEventConsts.LOG_EVENT_TYPE_TEXT + msName,
        true);
  }

  /**
   * Subscribe for message events filtered by the "Heartbeat Request" selector.
   * <b>ALL</b> Controllers subscribe to "Heartbeat Requests" and publish
   * "Heartbeat Response" event messages in reply. The
   * {@link com.daifukuamerica.wrxj.common.device.monitor.SystemHealthMonitor SystemHealthMonitor}
   * uses the "Heartbeat Response" messages to determine the system's condition.
   */
  private void subscribeHeartbeatRequestEvent()
  {
    // We DO want to see Heartbeat Request Event messages from ourself.
    startupTopicSubscriber(
        MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE_TEXT, false);
  }

  /**
   * Subscribe for Host Message Receipt notification.
   * 
   * @param sCKN the subscribing Controller's key name
   */
  protected final void subscribeHostMesgReceiveEvent(String sCKN, boolean nolocal)
  {
    startupTopicSubscriber(MessageEventConsts.HOST_MESG_RECV_TEXT + sCKN, nolocal);
  }

  /**
   * Subscribe for Host Message Send notification
   * 
   * @param sCKN the Controller key name
   * @param noLocal boolean used to inhibit or allow subscriber to receive this
   *            same event if it's published by the message subscriber.
   */
  protected final void subscribeHostMesgSendEvent(String sCKN, boolean noLocal)
  {
    startupTopicSubscriber(MessageEventConsts.HOST_MESG_SEND_TEXT + sCKN,
        noLocal);
  }
  
  /**
   * Subscribe for message events filtered by the "Load" selector.
   */
  protected final void subscribeLoadEvent()
  {
    subscribeLoadEvent(msName);
  }

  /**
   * Subscribe for message events filtered by the "Load" selector.
   */
  protected final void subscribeLoadEvent(String sCKN)
  {
    startupTopicSubscriber(MessageEventConsts.LOAD_EVENT_TYPE_TEXT + sCKN, true);
  }

  /**
   * Subscribe for message events from the specified Controller filtered by the
   * "Order" selector.
   * 
   * @param sCKN the Controller key name
   */
  protected final void subscribeOrderEvent(String sCKN)
  {
    startupTopicSubscriber(MessageEventConsts.ORDER_EVENT_TYPE_TEXT + sCKN,
        true);
  }

  /**
   * Subscribe for message events filtered by the "Status" and equipmentPortCKN
   * selector.
   */
  protected final void subscribePortStatusEvent()
  {
    subscribeStatusEvent(equipmentPortCKN);
  }

  /**
   * Subscribe for message events filtered by the "Scheduler" selector.
   */
  protected final void subscribeSchedulerEvent()
  {
    startupTopicSubscriber(MessageEventConsts.SCHEDULER_EVENT_TYPE_TEXT
        + msName, true);
  }

  /**
   * Subscribe for message events filtered by the "Station" selector.
   */
  protected final void subscribeStationEvent()
  {
    subscribeStationEvent(msName);
  }

  /**
   * Subscribe for message events filtered by the "Station" selector.
   */
  protected final void subscribeStationEvent(String sCKN)
  {
    startupTopicSubscriber(MessageEventConsts.STATION_EVENT_TYPE_TEXT + sCKN,
        true);
  }
  
  /**
   * Subscribe for message events filtered by the "Status" selector.
   * 
   * @param sCKN
   */
  protected final void subscribeStatusEvent(String sCKN)
  {
    if (sCKN != null)
    {
      startupTopicSubscriber(
          MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + sCKN, true);
    }
    else
    {
      logger.logError("subscribeStatusEvent() -- name == null");
    }
  }

  
  /*========================================================================*/
  /* Event / Message Reception                                              */
  /*========================================================================*/

  private boolean firstHeartbeatRequestEvent = true;

  /**
   * Handle a received "Heartbeat Request" message event. The received Date/Time
   * is used to compute the time taken to receive the heartbeat request. The
   * elapsed time is then sent back as part of the heartbeat response.
   */
  @Override
  protected void processHeartbeatRequestEvent()
  {
    if (firstHeartbeatRequestEvent)
    {
      firstHeartbeatRequestEvent = false;
      // Send an update in case this controller was up before the system health
      // monitor.
      publishControllerStatus();
    }
    publishHeartbeatStatusEvent(receivedTxTime);
  }

  /**
   * Handle a received "Controller Request" message event.
   */
  @Override
  protected void processRequestEvent()
  {
    publishControllerStatus();
  }
  
  
  /*========================================================================*/
  /*  Thread Management Methods                                             */
  /*========================================================================*/
  /**
   * Bring the Controller implementation to a minimal working state. The
   * base-Class implementation initializes the instantiated Controller by
   * setting the Controller's KeyName, <i>controllersKeyName</i>, attaching
   * logging, creating event observers, creating message event selectors for the
   * message service, and starting the Thread where messages will be processed.
   * 
   * <p>
   * A child Controller should find its collaborators (but <b>NOT</b> publish
   * to them), subscribe to any events it needs from its collaborators, and
   * perform any other simple tasks which do NOT require logging. Any object
   * creation beyond basic java classes should be done in the
   * {@link #startup() startup()} method where logging and message services are
   * available.
   * 
   * <p>
   * Inter-Process-Communication event processing is <b>NOT</b> yet enabled.
   * 
   * @param isControllerKeyName the unique name that identifies this instance of
   *            Controller
   */
  @Override
  protected void initialize(String isControllerKeyName)
  {
    super.initialize(isControllerKeyName);

    commEventTypeText = MessageEventConsts.COMM_EVENT_TYPE_TEXT
        + isControllerKeyName;

    setControllerStatus(ControllerConsts.STATUS_INITIALIZING);

    subscribeHeartbeatRequestEvent();
    subscribeRequestEvent();

    selfThread = new NamedThread(this);
    selfThread.setName(getName());
    selfThread.start(); // Start the thread-- executes run().

    String vsNameSpace = logger.getLoggerInstanceName();
    logger.logDebug("SystemGateway NameSpace " + vsNameSpace);
  }

  /**
   * Create any application objects the Controller implementation needs.
   * 
   * <p>
   * Inter-Process-Communication event processing is <i>NOT</i> yet enabled.
   * Inter-Process-Communication event processing will be enabled after all
   * Controllers have completed startup.
   */
  @Override
  protected void startup()
  {
    super.startup();
    setControllerStatus(ControllerConsts.STATUS_STARTING);
  }

  /**
   * Disconnect for the database.
   * 
   * Note: Properties reading can create a database connection.
   */
  protected void disconnectDB()
  {
    DBObject vpDBO = new DBObjectTL().getDBObject();
    if (vpDBO != null)
    {
      try
      {
        vpDBO.disconnect();
      }
      catch (DBException dbe)
      {
        logger.logException(dbe);
      }
    }
  }
  
  /**
   * Terminate the Controller implementation's ability to do work. Terminate the
   * controller implementation by cancelling any timers, setting any created
   * objects to <i>null</i>, and notifying the inter-process-communication
   * event processing thread to quit.
   * 
   * <p>
   * After shutdown the
   * {@link com.daifukuamerica.wrxj.common.controller.ControllerImplFactory ControllerImplFactory}
   * will shutdown the Controller's inter-process-communication message service
   * and set the Controller's logger to <i>null</i>.
   */
  @Override
  protected void shutdown()
  {
    setControllerStatus(ControllerConsts.STATUS_SHUTTING_DOWN);
    super.shutdown();
    setProperties(null);
    equipmentPortCKN = null;
    collaboratorCKN = null;
    logger.logDebug(msName + " is now SHUT-DOWN");
    setControllerStatus(ControllerConsts.STATUS_SHUT_DOWN);
  }

  
  /*========================================================================*/
  /* Configuration Properties Methods                                       */
  /*========================================================================*/
  /**
   * Sets generic controller properties.
   * 
   * <p>
   * <b>Details:</b> <code>setProperties</code> sets the collection of name/<wbr>value
   * data pairs used to define the behavior of this <code>Controller</code>.
   * </p>
   * 
   * @param ipProperties the name/value pairs
   */
  protected final void setProperties(ReadOnlyProperties ipProperties)
  {
    mpProperties = ipProperties;
  }

  /**
   * Returns named property value from configuration source.
   * 
   * <p>
   * <b>Details:</b> <code>getConfigProperty</code> returns the value of the
   * named configuration property for this device. Configurations properties are
   * typically defined in the controller list definition file, but they can
   * theoretically originate from any configuration source.
   * </p>
   * 
   * @param isName the name of the property
   * @return the property's value
   */
  protected final String getConfigProperty(String isName)
  {
    return mpProperties.getString(isName);
  }

  /**
   * Returns named property value from configuration source as int.
   * 
   * <p>
   * <b>Details:</b> <code>getConfigPropertyAsInt</code> does the same thing
   * as <code>getConfigProperty</code> but returns the value as an
   * <code>int</code>. If the string property is cannot be read or converted,
   * -1 is returned.
   * </p>
   * 
   * @param isName the name of the property
   * @return the property's value as an int
   */
  protected final int getConfigPropertyAsInt(String isName)
  {
    return getConfigPropertyAsInt(isName, -1);
  }

  /**
   * Returns named property value from configuration source as int.
   * 
   * <p>
   * <b>Details:</b> <code>getConfigPropertyAsInt(String,int)</code> does the
   * same thing as <code>getConfigPropertyAsInt(String)</code>, but instead
   * of returning 0 if the named property cannot be found, returns the supplied
   * default value.
   * </p>
   * 
   * @param isName the name of the property
   * @param inDefault default property value
   * @return the property's value as an int
   */
  protected final int getConfigPropertyAsInt(String isName, int inDefault)
  {
    return mpProperties.getInt(isName, inDefault);
  }

  /**
   * Returns named property value from configuration source as boolean.
   * 
   * <p>
   * <b>Details:</b> <code>getConfigPropertyAsBoolean</code> does the same
   * thing as <code>getConfigProperty</code> but returns the value as an
   * <code>boolean</code>. If the string property is cannot be read or
   * converted, false is returned.
   * </p>
   * 
   * @param isName the name of the property
   * @return the property's value as a boolean
   */
  protected final boolean getConfigPropertyAsBoolean(String isName)
  {
    String vsProp = mpProperties.getString(isName);
    if (vsProp == null)
      return false;
    else return Boolean.parseBoolean(vsProp);
  }
}
