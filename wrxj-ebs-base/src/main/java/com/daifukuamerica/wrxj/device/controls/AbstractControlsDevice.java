/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.device.controls;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;

public abstract class AbstractControlsDevice extends Controller
{
  protected int mnEquipmentPortStatus = ControllerConsts.STATUS_UNKNOWN;
  protected boolean mzProcessAgain = false;

  protected ControlsMessageInterface mpControlsMessage = null;
  
  /**
   * Initialize the controller
   */
  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.addEquipmentLogger();
    subscribeStatusEvent(equipmentPortCKN);
    subscribeEquipmentEvent(equipmentPortCKN);
    subscribeControlEvent();
  }

  /**
   * Start up the controller
   */
  @Override
  public void startup()
  {
    super.startup();
    //
    // Request a status update from our ports in case they were already up and
    // we missed their status reports.
    //
    publishRequestEvent(equipmentPortCKN);
    setEquipmentStatus(StatusEventDataFormat.STATUS_UNKNOWN);
    setControllerStatus(ControllerConsts.STATUS_WAIT_PORT_AND_DEVICE);
  }

  /**
   * Shuts down this controller by canceling any timers and shutting down the
   * Device/Equipment.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("shutdown() -- Start");
    setEquipmentStatus(StatusEventDataFormat.STATUS_UNKNOWN);
    logger.logDebug("shutdown() -- End");
    super.shutdown();
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
              setControllerStatus(ControllerConsts.STATUS_RUNNING);
              sendCommunicationTestRequest();  // Just to give us warm fuzzies
              setEquipmentStatus(StatusEventDataFormat.STATUS_ONLINE);
              break;
              
            case ControllerConsts.STATUS_ERROR:
              setEquipmentStatus(StatusEventDataFormat.STATUS_UNKNOWN);
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
              setEquipmentStatus(StatusEventDataFormat.STATUS_DISCONNECT);
              break;

            default:
              if (controllerStatus == ControllerConsts.STATUS_ERROR_PORT)
              {
                setControllerStatus(ControllerConsts.STATUS_WAIT_PORT);
                setEquipmentStatus(StatusEventDataFormat.STATUS_UNKNOWN);
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
   * We have received a CONTROL EVENT (probably from a User's Form)
   */
  @Override
  protected void processControlEvent()
  {
    try
    {
      switch (receivedData)
      {
        case ControlEventDataFormat.TEXT_MESSAGE:
          logger.logDebug(receivedData + " -- processControlEvent()");
          sendEquipmentStatusChange();
          break;
        case ControlEventDataFormat.PLC_STATUS_REQUEST:
          logger.logDebug(receivedData + " -- processControlEvent()");
          sendEquipmentStatusReport();
          break;
        case ControlEventDataFormat.PLC_COMM_TEST:
          logger.logDebug(receivedData + " -- processControlEvent()");
          sendCommunicationTestRequest();
          break;
        default:
          logger.logError("UNKNOWN Event Type \"" + receivedData
              + "\" -- processControlEvent()");
       }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "processControlEvent() - \""
          + receivedText + "\"");
    }
  }

  /**
   * Set equipment status information
   */
  protected void setEquipmentStatus(String equipmentStatus)
  {
    mpControlsMessage.setEquipmentStatus(equipmentStatus);
    publishStatusEvent(mpControlsMessage.getEquipmentStatusReport());
  }

  /**
   * Give the data to be transmitted to the Device/Equipment to the Port.
   */
  protected void transmitMessageToDevice()
  {
    String s = mpControlsMessage.getParsedMessageString();
    logger.logDebug(s);
    logger.logTxEquipmentMessage(mpControlsMessage.getMessageAsString(), s);
    publishEquipmentEvent(mpControlsMessage.getMessageAsString(), 0);
  }
  
  /*========================================================================*/
  /*  The following methods NEED to be implemented by a child class         */
  /*========================================================================*/

  /**
   * We have received a message from the PORT that is connected to the actual
   * Device/Equipment that this Transporter is controlling.
   * 
   * <BR>The received data (String) is in global field "receivedText".
   */
  public abstract void processEquipmentEvent();

  /*========================================================================*/
  /*  The following methods are normally called in response to user input   */
  /*  on the Equipment Monitor frame.                                       */
  /*========================================================================*/

  /**
   * Send Communication Test Request to the device
   * <BR>This is initiated from the Equipment Monitor
   * <BR>This is also called upon connecting to the device
   */
  protected void sendCommunicationTestRequest() {}

  /**
   * Send Equipment Status Change to the device
   * <BR>This is initiated from the Equipment Monitor
   */
  protected void sendEquipmentStatusChange() {}

  /**
   * Send Equipment Status Report to the device
   * <BR>This is initiated from the Equipment Monitor
   */
  protected void sendEquipmentStatusReport() {}
  
  /*========================================================================*/
  /* ControllerImplFactory                                                  */
  /*========================================================================*/
  
  /**
   * Someday we'll convert fully to Factory.  In the meantime, we need this 
   * method for ControllerImplFactory
   * 
   * <BR><BR>For a typical child class, this method should have 3 lines:
   * <CODE>
   * <BR> &nbsp; Controller vpController = new ChildClassName();
   * <BR> &nbsp; vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
   * <BR> &nbsp; return vpController;
   * </CODE>
   */
  public static Controller create(ReadOnlyProperties ipConfig)
    throws ControllerCreationException
  {
    throw new ControllerCreationException("Can not instantiate abstract class");
  }
}
