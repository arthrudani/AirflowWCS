package com.daifukuamerica.wrxj.device.station;

//
//                  Daifuku America Corporation
//                     International Center
//                 5202 Douglas Corrigan Way
//              Salt Lake City, Utah  84116-3192
//                       (801) 359-9900
//
// This software is furnished under a license and may be used and copied only 
// in accordance with the terms of such license.  This software or any other 
// copies thereof in any form, may not be provided or otherwise made available, 
// to any other person or company without written consent from Daifuku
// America Corporation.
//
// Daifuku America Corporation assumes no responsibility for the use or 
// reliability of software which has been modified without approval.

import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.LogConsts;

/**
 * Base Class for all types of stations.
 * This base class will do all the subscribing and publishing for all station devices
 *
 * The <tt>stationDevice</tt> will publish the following events.
 * <i>LoadEvent, EquipmentEvent, StationEvent, EquipmentStatus </i>.
 *
 * The <tt>stationDevice</tt> subscribes to the <i>LoadEvent, 
 * EquipmentEvent</i>.  After the event happens the received string is sent in 
 * the Abstract method for that event.  Each child class can then do what it 
 * needs for that event.
 *
 * @author Ed Askew
 * @version 1.0
 */
public abstract class StationDevice extends Controller
{
  protected int equipmentPortStatus = ControllerConsts.STATUS_UNKNOWN;
  private boolean bProcessAgain = false;
  private String sCommDevice = "";
  protected int equipmentPort2Status = ControllerConsts.STATUS_RUNNING;

  protected StandardLoadServer mpLoadServer;
  protected StandardSchedulerServer mpSchedServer;
  protected StandardStationServer mpStationServer;

  /**
   * The stationDevice Constructor will initialize the sCommDevice String. This is what
   * this StationDevice will be called.  It will only be set at initailization.
   * @param newComDevice
   */
  public StationDevice(String newComDevice)  // Device ID
  {
    sCommDevice = newComDevice;
  }

  /**
   * Abstract Class used to process events from the equipments.
   */
  public abstract void processEquipmentEvent(String isReceivedText); // receiveEquipmentEvent Message

  /**
   * Abstract Class used to process events from the loadEvents.
   */
  public abstract boolean processLoadEvent(String isReceivedText);   // receiveLoadEvent Message

  /**
   * Abstract Class used to process events from the stationEvents.
   */
  public abstract boolean processStationEvent(String isReceivedText);   // receiveStationEvent Message

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
    logger.logDebug("StationDevice.initialize() - Start");
    logger.addEquipmentLogger();
    subscribePortStatusEvent();
    subscribeEquipmentEvent();
    subscribeControlEvent();
    subscribeLoadEvent();
    subscribeStationEvent();
    logger.logDebug("StationDevice.initialize() - End");
  }

  /**
   * This method changes the controller status to wait port
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("StationDevice.startup() - Start");
    //
    // Request a status update from our port in case the were already up and
    // we missed its status reports.
    //
    publishRequestEvent(equipmentPortCKN);
    setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);

    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpSchedServer = Factory.create(StandardSchedulerServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);

   logger.logDebug("StationDevice.startup() - End");
  }

  /**
   * This method Shuts down this controller by cancelling any timers
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("StationDevice.shutdown() -- Start");
    sCommDevice = null;

    mpLoadServer.cleanUp();
    mpLoadServer = null;
    mpSchedServer.cleanUp();
    mpSchedServer = null;
    mpStationServer.cleanUp();
    mpStationServer = null;

    logger.logDebug("StationDevice.shutdown() -- End");
    super.shutdown();
  }

  /**
   * This method processes the Inter-Process-Communication Message received
   * to the correct message and calls the process method for that particuler message.
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
        case MessageEventConsts.LOAD_EVENT_TYPE:
                  logger.logDebug("StationDevice.processIPCReceivedMessage() -- LoadEvent \"" + receivedText + "\"");
                  processLoadEvent(receivedText);
                  break;
        case MessageEventConsts.EQUIPMENT_EVENT_TYPE:
                  processEquipmentEvent(receivedText);
                  break;
        case MessageEventConsts.STATION_EVENT_TYPE:
                  logger.logDebug("StationDevice.processIPCReceivedMessage() -- StationEvent");
                  processStationEvent(receivedText);
                  break;
        default: receivedMessageProcessed = false;
      }
    }
    do
    {
      bProcessAgain = false; // Some methods may set this to true.
    }
    while (bProcessAgain);
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
        {
          if (receivedCKN.equals(equipmentPortCKN))
          {
            //
            // The message is from the Material handling Computer ("MC") ARC
            // communication Port.
            //
            if (equipmentPortStatus == receivedData)
            {
              break;
            }
            else
            {
              //
              // Status of our Port has changed.
              //
              logger.logDebug("StatusChange (from Port) - WAS " +
                              ControllerConsts.STATUS_TEXT[equipmentPortStatus] + "  NOW " +
                              ControllerConsts.STATUS_TEXT[receivedData]);
              equipmentPortStatus = receivedData;
              
              /*
               * This switch() was removed 2006-01-12 by Al/Steve.
               * Mike replaced it 2006-09-08 to make the MOS systems work
               */
              switch (receivedData)
              {
                case ControllerConsts.STATUS_RUNNING:
                  startupDevice();
                  //
                  // Are we really "Running" yet...?
                  //
                  logger.logOperation(LogConsts.OPR_DEVICE,
                      "New Status: Running - Was: "
                      + ControllerConsts.STATUS_TEXT[controllerStatus]);
                  setControllerStatus(ControllerConsts.STATUS_RUNNING);
                  break;
                  
                case ControllerConsts.STATUS_ERROR:
                  logger.logOperation(LogConsts.OPR_DEVICE,
                      "New Status: **Port Error** - Was: "
                      + ControllerConsts.STATUS_TEXT[controllerStatus]);
                  setControllerStatus(ControllerConsts.STATUS_ERROR_PORT);
                  break;
                  
                case ControllerConsts.STATUS_STOPPING:
                case ControllerConsts.STATUS_STOPPED:
                case ControllerConsts.STATUS_SHUTTING_DOWN:
                case ControllerConsts.STATUS_SHUT_DOWN:
                  logger.logOperation(LogConsts.OPR_DEVICE,
                      "New Status: Waiting for Port - Was: "
                      + ControllerConsts.STATUS_TEXT[controllerStatus]);
                  setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
                  // TODO: Update equipment Monitor
                  break;
                
                default:
                  if (controllerStatus == ControllerConsts.STATUS_ERROR_PORT)
                  {
                    logger.logOperation(LogConsts.OPR_DEVICE,
                        "New Status: Waiting for Port - Was: "
                        + ControllerConsts.STATUS_TEXT[controllerStatus]);
                    setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
                  }
                  break;
              }
            }
          }
          else
          {
            //
            // The message is from the 2nd communication Port (RECEIVE data).
            // If none defined, this "else" will never get executed.
            //
            if (equipmentPort2Status == receivedData)
            {
              break;
            }
            else
            {
              //
              // Status of our Port has changed.
              //
              logger.logDebug("StatusChange (from Port) - WAS " +
                              ControllerConsts.STATUS_TEXT[equipmentPort2Status] + "  NOW " +
                              ControllerConsts.STATUS_TEXT[receivedData]);
              equipmentPort2Status = receivedData;
            }
          }
          if ((equipmentPortStatus == ControllerConsts.STATUS_RUNNING) &&
              (equipmentPort2Status == ControllerConsts.STATUS_RUNNING))
          {
            setControllerStatus(ControllerConsts.STATUS_RUNNING);
          }
          else if ((equipmentPortStatus == ControllerConsts.STATUS_ERROR) ||
                   (equipmentPort2Status == ControllerConsts.STATUS_ERROR))
          {
            setControllerStatus(ControllerConsts.STATUS_ERROR_PORT);
          }
          else if (controllerStatus == ControllerConsts.STATUS_ERROR_PORT)
          {
            setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
          }
        }
        break;
        default:
        {
          logger.logError("UNKNOWN Event Type \"" + chr0 + "\" - StationDevice.processStatusEvent()");
        }
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "\"" + receivedText + "\" - StationDevice.processStatusEvent()");
    }
  }

  /**
   * This method publishes a LoadEvent using the string publishString.
   * @see com.daifukuamerica.wrxj.common.controller.ControllerConsts  publishLoadEvent()
   */
  protected void transmitLoadEvent(String loadEventString)
  {
    publishLoadEvent(loadEventString, 0);
  }

  /**
   * Give the data to be transmitted to the Device/Equipment to the Port.
   */
  protected void transmitEquipmentEvent(String equipmentEventString, String clarifier)
  {
    logger.logTxEquipmentMessage(equipmentEventString, clarifier);
    publishEquipmentEvent(equipmentEventString, 0);
  }

  /**
   * Give the data to be transmitted to station people
   */
  protected void transmitStationEvent(String stationEventString)
  {
    publishStationEvent(stationEventString, 0);
  }

  /**
   * Start the device
   */
  protected void startupDevice()
  {
  }

  /**
   * Get the comm device
   * @return
   */
  public String getCommDeviceID()
  {
      return sCommDevice;
  }

  /**
   * If we're going to surround our debug messages with hordes of =s, we may
   * as well be consistent with it.
   * @param isMessage
   */
  protected void logDebug(String isMessage)
  {
    logger.logDebug("=================== " + isMessage + " ===================");
  }
 
  /*========================================================================*/
  /*  Tracking ID / MC Key methods                                          */
  /*========================================================================*/

  /**
   * Return a Load Id for the caller's Tracking Id.  The caller's
   * Tracking Id may be returned as the Load Id if the Load Id is
   * not long enough to need a shorter Tracking Id.
   * 
   * @param isTrackingId tracking id that may be the loadId
   * @return a load Id (may be TrackingId)
   */
  protected String getTrackingsLoadId(String isTrackingId)
  {
    try
    {
      return mpSchedServer.getLoadIdFromTrackingId(isTrackingId);
    }
    catch (DBException e)
    {
      logger.logException(e, "TrackingId \"" + isTrackingId + "\"");
      return isTrackingId;
    }
  }

  /**
   * Return a Tracking Id if the Load Id is too long to be a Tracking Id.
   * The caller's Load Id may be returned as the Tracking Id if the
   * Load Id's length is not long enough to need a shorter Tracking Id. 
   * 
   * @param isLoadId load ID (barcode) that may need a tracking id
   * @return a tracking Id (may be LoadId)
   */
  protected String getLoadsTrackingId(String isLoadId)
  {
    return mpSchedServer.getTrackingId(isLoadId,
        AGCDeviceConstants.LOADIDMAXVALUE);
  }

}