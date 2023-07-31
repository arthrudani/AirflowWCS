package com.daifukuamerica.wrxj.device.scale;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;

public class ScaleDevice extends Controller
{
  private int mnEquipmentPortStatus = ControllerConsts.STATUS_UNKNOWN;
  private boolean mzProcessAgain = false;
  protected ScaleMessage mpScaleMessage;
  protected String msScaleStation;
  protected LoadEventDataFormat mpLoadEventData;
  protected StandardLoadServer mpLoadServer;
  protected DeviceData mpDeviceData = null;

  /**
   * Constructor
   */
  public ScaleDevice()
  {
    this(null);
  }

  /**
   * Constructor
   * 
   * @param deviceID
   */
  public ScaleDevice(String deviceID)
  {

  }

  /**
   * This method changes the controller status to wait port
   */
  @Override
  public void startup()
  {
    super.startup();
    mpLoadEventData = Factory.create(LoadEventDataFormat.class, msName);
    mpScaleMessage = Factory.create(ScaleMessage.class);
    mpLoadServer = Factory.create(StandardLoadServer.class);
    StandardDeviceServer vpDeviceServer = Factory.create(StandardDeviceServer.class);
    mpDeviceData =  vpDeviceServer.getDeviceData(msName);
    logger.logDebug("ScaleDevice.startup() - Start");
    //
    // Request a status update from our port in case the were already up and
    // we missed its status reports.
    //
    publishRequestEvent(equipmentPortCKN);
    setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
    logger.logDebug("ScaleDevice.startup() - End");
  }
  
  /**
   * This method Shuts down this controller by cancelling any timers
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("ScaleDevice.shutdown() -- Start");
    if(mpLoadServer != null)
    {
      mpLoadServer.cleanUp();
      mpLoadServer = null; 
    }
    logger.logDebug("ScaleDevice.shutdown() -- End");
    super.shutdown();
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
    logger.logDebug("ScaleDevice.initialize() - Start");
    logger.addEquipmentLogger();
    subscribePortStatusEvent();
    subscribeEquipmentEvent();
    subscribeControlEvent();
    subscribeLoadEvent();
    logger.logDebug("ScaleDevice.initialize() - End");
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
        case MessageEventConsts.LOAD_EVENT_TYPE:
        	processLoadEvent();
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
   * Process a control event (usually from a user screen)
   */
  @Override
  protected void processControlEvent()
  {
    try
    {
      int vnType = receivedData;
      switch (vnType)
      {
      	case ControlEventDataFormat.TEXT_MESSAGE:
      		processWeight();
      		break;
      	default:
          logger.logError("ScaleDevice.processControlEvent() -- UNKNOWN Event Type \"" 
              + vnType + "\" -- processControlEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logException(e, "AGCStationDevice.processControlEvent() - \"" + receivedText + "\"");
    }
  }
  
  /**
   * Process an equipment event
   * 
   * @param messageId
   */
  protected void processEquipmentEvent()
  {
  	logger.logRxEquipmentMessage(receivedText, "Equipment");
  	int vnMessageType = mpScaleMessage.getMessageType(receivedText);
  	switch(vnMessageType)
  	{
  		case ScaleMessage.WEIGHT:
  			processWeight();
  			break;
  		case ScaleMessage.MOVEMENT:
  			sendWeightCommand();
  			break;
  		case ScaleMessage.TRANSMISSION_ERROR:
  			sendWeightCommand();
  			break;
  		case ScaleMessage.LOGICAL_ERROR:
  			break;
  		case ScaleMessage.SYNTAX_ERROR:
  			//Using this as a keep alive
  			break;
  	}
  }
  
  /**
   * Process a load event
   */
  protected void processLoadEvent()
  {
  	mpLoadEventData.decodeReceivedString(receivedText);
  	sendWeightCommand();
  }
  
  /**
   * Method sends the weight command to the scale
   */
  protected void sendWeightCommand()
  {
  	transmitEquipmentEvent(mpScaleMessage.getWeightCommand(), "Check Weight");
  }
  
  /**
   * Method stub to be overwritten in custom Scale
   */
  protected void sendOverWeightMessage()
  {
  	
  }
  
  /**
   * Method processes the weight message
   */
  protected void processWeight()
  {
    LoadData vpLoadData = Factory.create(LoadData.class);
    double vdweight = mpScaleMessage.getWeight(receivedText);
    if (vdweight > Application.getDouble("ScaleDeviceMaxWeight"))
    {
      sendOverWeightMessage();
    }
    else
    {
      vpLoadData = mpLoadServer.getOldestLoadData(
          mpDeviceData.getStationName(), DBConstants.ARRIVEPENDING);
      if (vpLoadData != null)
      {
        vpLoadData.setWeight(vdweight);
        vpLoadData.setAmountFull(DBConstants.FULL);
        vpLoadData.setLoadMoveStatus(DBConstants.STOREPENDING);
        try
        {
          mpLoadServer.updateLoadInfo(vpLoadData);
          LoadEventDataFormat mpLEDF = Factory.create(
              LoadEventDataFormat.class, msName);
          String msStoreCmd = mpLEDF.screenLoadRelease(vpLoadData.getLoadID(),
              vpLoadData.getAddress());
          publishLoadEvent(msStoreCmd, 0, mpDeviceData.getSchedulerName());

        }
        catch (DBException e)
        {
          logger.logException("Error updating weight of load--processWeight()",
              e);
        }
      }
      else
      {
        logger.logError("No load to add weight at station:"
            + mpDeviceData.getStationName());
      }
    }
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
    String vsDeviceId = ipConfig.getString(DEVICE_ID);
    if (vsDeviceId == null)
      throw new ControllerCreationException(
          "unable to create ScaleDevice: DeviceID undefined");
    Controller vpController = Factory.create(ScaleDevice.class, vsDeviceId);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }
}
