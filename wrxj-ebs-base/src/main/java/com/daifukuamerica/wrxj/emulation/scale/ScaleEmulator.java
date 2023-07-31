package com.daifukuamerica.wrxj.emulation.scale;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.device.scale.ScaleMessage;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.log.LogConsts;

public class ScaleEmulator extends Controller
{
  private int mnEquipmentPortStatus = ControllerConsts.STATUS_UNKNOWN;
  private boolean mzProcessAgain = false;
  private String defaultWeight = "1000";
  protected ScaleMessage mpScaleMessage;
  
  /**
   * This method changes the controller status to wait port
   */
  @Override
  public void startup()
  {
    super.startup();
    mpScaleMessage = Factory.create(ScaleMessage.class);
    logger.logDebug("ScaleEmulator.startup() - Start");
    //
    // Request a status update from our port in case the were already up and
    // we missed its status reports.
    //
    publishRequestEvent(equipmentPortCKN);
    setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
    logger.logDebug("ScaleEmulator.startup() - End");
  }
  /*--------------------------------------------------------------------------*/
  /**
   * The Base Controller initialized the controller by setting the controller's
   * KeyName, attaching a logger, starting up the topic subscribers and
   * publishers for the message service, and starting the Thread where messages
   * will be processed.
   *
   * The <tt>Scheduler</tt> finds its collaborators (but doesn't use them),
   * subscribe to <i>LoadEvent, SchedulerEvent, StationEvent</i>.
   *
   * @param aControllerKeyName the unique name that identifies this instance of Controller
   */
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("ScaleEmulator.initialize() - Start");
    logger.addEquipmentLogger();
    subscribePortStatusEvent();
    subscribeEquipmentEvent();
    logger.logDebug("ScaleEmulator.initialize() - End");
  }
  
  /**
   * We come here when our Port notifies this Transporter that it has
   * changed status (Connecting, Running, etc.).
   */
  @Override
  protected void processStatusEvent()
  {
    super.processStatusEvent();  // Make sure we let the base class process this.
    try
    {
      char chr0 = receivedText.charAt(0);
      switch (chr0)
      {
        case ControllerConsts.CONTROLLER_STATUS:
          if (mnEquipmentPortStatus == receivedData)
          {
            break;
          }
          //
          // Status of our Port has changed.
          //
          logger.logDebug("StatusChange (from Port) - WAS "
                + ControllerConsts.STATUS_TEXT[mnEquipmentPortStatus] + "  NOW "
                + ControllerConsts.STATUS_TEXT[receivedData]);
          mnEquipmentPortStatus = receivedData;
          switch (receivedData)
          {
            case ControllerConsts.STATUS_RUNNING:
              //
              // Our Port is running - we can go online.
              //
                logger.logOperation(LogConsts.OPR_DEVICE, "New Status: Running - Was: " +
                  ControllerConsts.STATUS_TEXT[controllerStatus]);
              setControllerStatus(ControllerConsts.STATUS_RUNNING);
              break;
              
            case ControllerConsts.STATUS_ERROR:
                logger.logOperation(LogConsts.OPR_DEVICE, "New Status: **Port Error** - Was: " +
                  ControllerConsts.STATUS_TEXT[controllerStatus]);
              setControllerStatus(ControllerConsts.STATUS_ERROR_PORT);
              break;
            case ControllerConsts.STATUS_SHUT_DOWN:
                logger.logOperation(LogConsts.OPR_DEVICE, "New Status: **Port Shut Down** - Was: " +
                  ControllerConsts.STATUS_TEXT[controllerStatus]);
              setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
            default:
              if (controllerStatus == ControllerConsts.STATUS_ERROR_PORT)
              {
                logger.logOperation(LogConsts.OPR_DEVICE, "New Status: Waiting for Port - Was: " +
                    ControllerConsts.STATUS_TEXT[controllerStatus]);
                setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
              }
              break;
          }
          break;
        default:
          logger.logError("processStatusEvent() -- UNKNOWN Event Type \""
              + chr0 + "\" -- processStatusEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "processStatusEvent() - \""
          + receivedText + "\"");
    }
  }

  /**
   * Process System Inter-Process-Communication Message.
   */
  @Override
  protected void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.EQUIPMENT_EVENT_TYPE:
          processEquipmentEvent();
          break;
        default: receivedMessageProcessed = false;
      }
    }
    do
    {
      mzProcessAgain = false; // Some methods may set this to true.
    }
    while (mzProcessAgain);
  }
  
  /**
   * Process an equipment event
   * 
   * @param messageId
   */
  protected void processEquipmentEvent()
  {
    logger.logRxEquipmentMessage(receivedText, "Equipment");
    int vnMessageType = mpScaleMessage.getEmulatorMessageType(receivedText);
    switch(vnMessageType)
    {
        case ScaleMessage.BUFFER_CLEAR:
            sendBufferClearResponse();
            break;
        case ScaleMessage.WEIGHT:
            sendWeight();
            break;
    }
  }
  
  /**
   * method sends the buffer cleared response
   */
  protected void sendBufferClearResponse()
  {
    transmitEquipmentEvent(mpScaleMessage.getBufferClearResponse(), "Buffer Clear Response");
  }
  
  protected void sendWeight()
  {
    String vsMessage = mpScaleMessage.getWeightDataMessage(defaultWeight);
    transmitEquipmentEvent(vsMessage, "Stable Weight");
  }
  
  /**
   * Give the data to be transmitted to the Device/Equipment without logging
   */
  protected void transmitEquipmentEvent(String isEquipmentEventString)
  {
    publishEquipmentEvent(isEquipmentEventString, 0);
  }
  
  /**
   * Give the data to be transmitted to the Device/Equipment to the Port.
   */
  protected void transmitEquipmentEvent(String equipmentEventString, String isClarifier)
  {
    logger.logTxEquipmentMessage(equipmentEventString, isClarifier);
    publishEquipmentEvent(equipmentEventString, 0);
  }
  
  /**
   * Factory for ControllerImplFactory.
   *
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>.  Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object.  If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   *
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) 
      throws ControllerCreationException
  {
    Controller vpController = Factory.create(ScaleEmulator.class);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }

}
