package com.daifukuamerica.wrxj.device.arc9y;
//
//                 Daifuku America Corporation
//                     International Center
//                 5202 Douglas Corrigan Way
//              Salt Lake City, Utah  84116-3192
//                      (801) 359-9900
//
// This software is furnished under a license and may be used and copied only 
// in accordance with the terms of such license.  This software or any other 
// copies thereof in any form, may not be provided or otherwise made available, 
// to any other person or company without written consent from Daifuku
// America Corporation.
//
// Daifuku America Corporation assumes no responsibility for the use or 
// reliability of software which has been modified without approval.
//

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.station.StationDevice;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;

/**
 * This class does the specific control of messages for the ARC and SRC devices.
 * This class will take the generic LoadEvent messages and make specific
 * messages for the SRC or ARC.
 * 
 * @author S Kendorski
 * @version 1.0
 */
public class Arc9yDevice extends StationDevice
{
  private static final String DEFAULT_SHELF_POSITION = "000";
  
  protected Arc9yMessage mpArc9yMessage;
  protected boolean publishMachineStatuses = true;
  private boolean autoPublishDateAndTime = true;

  protected int mcKeyLength = 0;
  
  /**
   * The unique <i>String</i> key that identifies the second communication port
   * in a system which uses two communication ports for one device.
   */
  protected String equipmentPort2CKN = null;
  
  /**
   * The Arc9yDevice Constructor will pass the deviceID to the super class
   * to name this device. It will only be set at initialization.
   *
   * @param isDeviceID no information available
   */
  public Arc9yDevice(String isDeviceID)
  {
    super(isDeviceID);
  }

  @Override
  public void initialize(String aControllerKeyName)
  {
    super.initialize(aControllerKeyName);
    logger.logDebug("Arc9yDevice.initialize() - Start");
    equipmentPort2CKN = getConfigProperty(DEVICE_PORT2);
    if (equipmentPort2CKN != null)
    {
      if (equipmentPort2CKN.equals(equipmentPortCKN))
      {
        logger.logDebug("Device Port 1 equals Port 2; only subscribing to one port");
      }
      else
      {
        //
        // We have a 2nd comm port which we will use as our RECEIVE Port.
        //
        logger.logDebug("DevicePort2 \"" + equipmentPort2CKN + "\"");
        equipmentPort2Status = ControllerConsts.STATUS_UNKNOWN;
        subscribeStatusEvent(equipmentPort2CKN);
        subscribeEquipmentEvent(equipmentPort2CKN);
        //
        // Request a status update from our port in case the were already up and
        // we missed its status reports.
        //
        publishRequestEvent(equipmentPort2CKN);
      }
    }
    else
    {
      logger.logDebug("Missing DevicePort2 (Device May Not Need One)");
    }
    
//    System.out.println(msName + " Out-Port = " + equipmentPortCKN);
//    System.out.println(msName + "  In-Port = " + equipmentPort2CKN);

    logger.logDebug("Arc9yDevice.initialize() - End");
  }
  
  /**
   * This method connects to the database and instantiates the loadEventData,
   * StatusEventData, loadServer, stationData.
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("Arc9yDevice.startup() - Start");
    mpArc9yMessage = Factory.create(Arc9yMessage.class);
    mcKeyLength = mpArc9yMessage.getMCKeyLength();
    //
    String sPublishMachineStatuses = getConfigProperty("PublishMachineStatuses");
    if (sPublishMachineStatuses != null)
    {
      sPublishMachineStatuses = sPublishMachineStatuses.substring(0,1);
      if ((sPublishMachineStatuses.equalsIgnoreCase("Y")) ||
          (sPublishMachineStatuses.equalsIgnoreCase("T")))
      {
       publishMachineStatuses = true;
      }
    }
    if (publishMachineStatuses)
    {
      logger.logDebug("DO Publish Machine Statuses - Arc9yDevice.startup()");
    }
    else
    {
      logger.logDebug("Do NOT Publish Machine Statuses - Arc9yDevice.startup()");
    }
    logger.logDebug("Arc9yDevice.startup() - End");
  }

  /**
   * This method disconnects from the database and  the loadEventData,
   * StatusEventData, loadServer, stationData.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("Arc9yDevice.shutdown() -- Start");
    mpArc9yMessage = null;
    equipmentPort2CKN = null;
    logger.logDebug("Arc9yDevice.shutdown() -- End");
    super.shutdown();
  }
  /*--------------------------------------------------------------------------*/
  // We have received a CONTROL EVENT (probably from a User's Form)
  /*--------------------------------------------------------------------------*/
  @Override
  protected void processControlEvent()
  {
    if (controllerStatus != ControllerConsts.STATUS_RUNNING)
    {
      logger.logDebug("Controller NOT Running - Control Event DISCARDED - processControlEvent()");
      return;
    }
    try
    {
      char chr0 = receivedText.charAt(0);
      switch (chr0)
      {
        case ControlEventDataFormat.CHAR_START_DEVICE:
          //
          // Start the Equipment.
          //
          logDebug("Operation Start - 01 - processControlEvent()");
          startupDevice();
          break;
        case ControlEventDataFormat.CHAR_STOP_DEVICE:
          logDebug("RequestToTerminateOperation - 03 - processControlEvent()");
          setDetailedControllerStatus("Request ARC Offline - Unconditional (03)");
          mpArc9yMessage.requestToTerminateOperation(0);
          transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(),
              mpArc9yMessage.getParsedMessageString());
          break;
        case ControlEventDataFormat.CHAR_REQUEST_STATUS:
          logDebug("MachineStatusInquiry - 10 - processControlEvent()");
          setDetailedControllerStatus("Request Machine Status (10)");
          mpArc9yMessage.machineStatusRequest();
          transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(),
              mpArc9yMessage.getParsedMessageString());
          break;
        case ControlEventDataFormat.CHAR_COMM_TEST:
          logDebug("CommunicationTestRequest - 19 - processControlEvent()");
          setDetailedControllerStatus("Communication Test Request (19)");
          logger.logOperation(LogConsts.OPR_DEVICE,
              "Communication Test Request (19)");
          mpArc9yMessage.setCommunicationTestRandomTextRequest();
          mpArc9yMessage.StartCommTest();
          transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(),
              mpArc9yMessage.getParsedMessageString());
          break;
        case ControlEventDataFormat.CHAR_START_EQUIPMENT:
          logDebug("SimultaneousStartStopCmd - 16 - processControlEvent()");
          setDetailedControllerStatus("Request Equipment Online (16)");
          mpArc9yMessage.startDevice();
          transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(),
              mpArc9yMessage.getParsedMessageString());
          break;
        case ControlEventDataFormat.CHAR_STOP_EQUIPMENT:
          logDebug("SimultaneousStartStopCmd - 16 - processControlEvent()");
          setDetailedControllerStatus("Request Equipment Offline (16)");
          mpArc9yMessage.stopDevice();
          transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(),
              mpArc9yMessage.getParsedMessageString());
          break;
        default:
          logger.logError(
              "Arc9yDevice.processControlEvent() -- UNKNOWN Event Type \""
              + chr0 + "\" -- processControlEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logException(e, "Arc9yDevice.processControlEvent() - \"" 
          + receivedText + "\"");
    }
  }

  /*--------------------------------------------------------------------------*/
  @Override
  protected void startupDevice()
  {
    setDetailedControllerStatus("Request ARC Online (01)");
    mpArc9yMessage.requestToStartOperation();
    transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(), mpArc9yMessage.getParsedMessageString());
  }

  /**
   * These stationEvent Messages are unformated using stationEventFormat
   * and then the method that handles each message type is called
   * @param isReceivedText of the stationEvent
   * @return no information available
   */
  @Override
  public boolean processStationEvent(String isReceivedText)
  {
    return true;
  }

  /**
   * These loadEvent Messages are parsed using LoadEventFormatDataFormat and
   * then the method that handles each message type is called. They come from
   * WRX-J and are messages sent to the equipment.
   * 
   * @param isReceivedText of the loadEvent
   * @return no information available
   */
  @Override
  public boolean processLoadEvent(String isReceivedText)
  {
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    if (vpLEDF.decodeReceivedString(isReceivedText))
    {
      logger.logDebug("LoadEventData IS Valid \"" + vpLEDF.toString() + "\"");
      mpArc9yMessage.clearMessageData();
      processLoadEventMessage(vpLEDF);
      if (mpArc9yMessage.getValidMessage())
      {
        transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(), 
            mpArc9yMessage.getParsedMessageString());
        return true;
      }
      else
      {
        logger.logError("Arc9yDevice.processLoadEvent() -- "
            + mpArc9yMessage.getInvalidMessageDescription() + " -- \""
            + mpArc9yMessage.getMessageAsString() + "\"");
        return false;
      }
    }
    else
    {
        logger.logDebug("INVALID Message \"" + isReceivedText
          + "\" - StationDevice.processLoadEvent()");
        return false;
    }
  }
  
  /**
   * Determine the type of message and then call the correct method to process
   * it.
   * 
   * @param ipLEDF - the <code>LoadEventDataFormat</code> message
   */
  protected void processLoadEventMessage(LoadEventDataFormat ipLEDF)
  {
    switch (ipLEDF.getMessageID())
    {
      case AGCDeviceConstants.TURNONDEVICE:
        turnOnDevice(); // ID 01
        break;
      case AGCDeviceConstants.TURNOFFDEVICE:
        turnOffDevice(); // ID 01
        break;
      case AGCDeviceConstants.FORCEDEVICEOFF:
        forceDeviceOff(); // ID 01
        break;
      case AGCDeviceConstants.FORCEDEVICEOFFDELDATA:
        forceDeviceOffDeleteData(); // ID 01
        break;
      case AGCDeviceConstants.SENDDATEANDTIME:
        dateAndTime(); // ID 02
        break;
      case AGCDeviceConstants.STORELOADSTATIONLOCATION:
        storeLoad(ipLEDF); // ID 05
        break;
      case AGCDeviceConstants.MOVELOADSTATIONSTATION:
        moveLoadStationStation(ipLEDF); // ID 05
        break;
      case AGCDeviceConstants.DEVICESTATUS:
        machineStatusRequest(); // ID 10
        break;
      case AGCDeviceConstants.BINFULLNEWLOC:
        binFullNewLocation(ipLEDF); // ID 11
        break;
      case AGCDeviceConstants.BINFULLMOVESTATION:
        binFullMoveStation(ipLEDF); // ID 11
        break;
      case AGCDeviceConstants.BINEMPTYCANCEL:
        binEmptyCancel(ipLEDF); // ID 11
        break;
      case AGCDeviceConstants.RETRIEVELOADLOCATIONSTATION:
        retrieveLoadLocationStation(0, ipLEDF); // ID 12
        break;
      case AGCDeviceConstants.MOVELOADLOCATIONLOCATION:
        moveLoadLocationLocation(0, ipLEDF); // ID 12
        break;
      case AGCDeviceConstants.STARTDEVICE:
        startDevice(); // ID 16
        break;
      case AGCDeviceConstants.STOPDEVICE:
        stopDevice(); // ID 16
        break;
      case AGCDeviceConstants.COMMTESTDEVICE:
        StartCommTest(); // ID 19
        break;
      case AGCDeviceConstants.COMMTESTRESPONSE:
        responseToCommTestDevice(); // ID 20
        break;
      case AGCDeviceConstants.SEND_DATA_MESSAGE:
        sendDataMessage(ipLEDF); // ID 50
        break;
      default:
        logger.logDebug("INVALID MessageID: " + ipLEDF.getMessageID()
            + " - processDataFromScheduler()");
    }
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to turn
   * it on. This is ID 01.
   */
  private void turnOnDevice()
  {
    logDebug("Operation Start - 01 - turnOnDevice()");
    setDetailedControllerStatus("Request ARC Online (01)");
    mpArc9yMessage.requestToStartOperation();
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * turn it off.  This is ID 01.
   */
  private void turnOffDevice()
  {
    logDebug("RequestToTerminateOperation - 03/0 - turnOffDevice()");
    setDetailedControllerStatus("Request ARC Offline (03)");
    // Request Classification 0 = regular termination
    mpArc9yMessage.requestToTerminateOperation(0);      
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * turn force it off.  This is ID 01.
   */
  private void forceDeviceOff()
  {
    logDebug("RequestToTerminateOperation - 03/1 - forceDeviceOff()");
    setDetailedControllerStatus("Request ARC Offline - Unconditional (03)");
    // Request Classification 1 = unconditional termination with data retention
    mpArc9yMessage.requestToTerminateOperation(1);
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * turn force it off and delete data.  This is ID 01.
   */
  private void forceDeviceOffDeleteData()
  {
    logDebug("RequestToTerminateOperation - 03/2 - forceDeviceOffDeleteData()");
    setDetailedControllerStatus("Request ARC Offline - Uncond & Del Data (03)");
    // Request Classification 2 = unconditional termination with no data retention
    mpArc9yMessage.requestToTerminateOperation(2);
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * store a load from a station to a rack location.  This is ID 05.
   */
  protected void storeLoad(LoadEventDataFormat ipLEDF)
  {
    logDebug("TransportCommand - 05 - storeLoad()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId
        + "\" - Store TransportCommand(05) Sent - SrcStn: "
        + ipLEDF.getSourceStation() + " DstLoc: "
        + ipLEDF.getDestinationLocation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpArc9yMessage.storeLoad(vsTrackingId, 
        mpLoadServer.getLoadPresenceRequired(loadId), 
        ipLEDF.getSourceStation(),
        ipLEDF.getDestinationLocation(),
        ipLEDF.getDestinationEquipWarehouse());
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * store a load from a station to another station.  This is ID 05.
   */
  protected void moveLoadStationStation(LoadEventDataFormat ipLEDF)
  {
    logDebug("TransportCommand - 05 - moveLoadStationStation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId
        + "\" - Transfer TransportCmd(05) Sent - SrcStn: "
        + ipLEDF.getSourceStation() + " DstStn: "
        + ipLEDF.getDestinationStation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpArc9yMessage.moveLoadStationStation(vsTrackingId, 
        mpLoadServer.getLoadPresenceRequired(loadId),
        ipLEDF.getSourceStation(), 
        ipLEDF.getDestinationStation());
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * retrieve a load from a rack location to a station.  This is ID 12.
   *
   * @param commandNumber no information available
   */
  protected void retrieveLoadLocationStation(int commandNumber,
      LoadEventDataFormat ipLEDF)
  {
    logDebug("RetrievalCommand - 12 - retrieveLoadLocationStation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId
        + "\" - RetrieveLoadCmd(12) Sent - SrcLoc: "
        + ipLEDF.getSourceLocation() + " DstStn: "
        + ipLEDF.getDestinationStation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpArc9yMessage.retrieveLoadLocationStation(commandNumber, vsTrackingId,
        mpStationServer.getStationType(ipLEDF.getDestinationStation()), 
        ipLEDF.getDestinationStation(),
        ipLEDF.getSourceLocation(),
        ipLEDF.getSourceEquipWarehouse());
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * retrieve a load from a rack location and store it into another location
   * in the same aisle.  This is ID 12.
   *
   * @param commandNumber no information available
   */
  private void moveLoadLocationLocation(int commandNumber,
      LoadEventDataFormat ipLEDF)
  {
    logDebug("RetrievalCommand - 12 - moveLoadLocationLocation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId
        + "\" - Loc-Loc RetrieveLoadCmd(12) Sent - SrcLoc: "
        + ipLEDF.getSourceLocation() + " DstLoc: "
        + ipLEDF.getDestinationLocation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpArc9yMessage.moveLoadLocationLocation(commandNumber, vsTrackingId,
        ipLEDF.getSourceLocation(),
        ipLEDF.getSourceEquipWarehouse(),
        ipLEDF.getDestinationLocation(),
        ipLEDF.getDestinationEquipWarehouse());
  }

  /**
   * Send the Date/Time to the AGC
   */
  private void dateAndTime()
  {
    logDebug("DateTimeDataResponse - 02 - dateAndTime()");
    setDetailedControllerStatus("Date-Time Data Report (02)");
    mpArc9yMessage.dateAndTime();
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * request a machine status report from thwe SRC/ARC.  This is ID 10.
   */
  private void machineStatusRequest()
  {
      logDebug("MachineStatusInquiry - 10 - machineStatusRequest()");
      setDetailedControllerStatus("Request Machine Status (10)");
      mpArc9yMessage.machineStatusRequest();
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * move a load that was storing to a location that already has a load in it
   * that we don't know about to a different location.  This is ID 11.
   */
  private void binFullNewLocation(LoadEventDataFormat mpLoadEventData)
  {
    logDebug("AlternativeLocationCmd - 11 - binFullNewLocation()");
    String loadId = mpLoadEventData.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    setDetailedControllerStatus(AGCDeviceConstants.DS_ALT_LOC_BIN_REQ);
    logger.logDebug(" Destination Station for bin full = " 
        + mpLoadEventData.getDestinationLocation());
    mpArc9yMessage.binFullNewLocation(vsTrackingId,
        mpLoadEventData.getDestinationLocation(),
        mpLoadEventData.getDestinationEquipWarehouse(),
        mpLoadEventData.getDimensionInfo(),
        mpLoadEventData.getSourceStation());
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * move a load that was storing to a location that already has a load in it
   * that we don't know about to a station.  This is ID 11.
   */
  private void binFullMoveStation(LoadEventDataFormat mpLoadEventData)
  {
    logDebug("AlternativeLocationCmd - 11 - binFullNewStation()");
    String loadId = mpLoadEventData.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    setDetailedControllerStatus(AGCDeviceConstants.DS_ALT_LOC_STN_REQ);
    mpArc9yMessage.binFullMoveStation(vsTrackingId,
        mpLoadEventData.getDestinationStation(), mpLoadEventData.getDimensionInfo());
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * cancel a load retrieval from a location that doesn't have a load in it
   * but we think it does.  This is ID 11.
   */

  private void binEmptyCancel(LoadEventDataFormat mpLoadEventData)
  {
    logDebug("AlternativeLocationCmd - 11 - binEmptyCancel()");
    String loadId = mpLoadEventData.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + (loadId + "\" Location: " 
        + mpLoadEventData.getDestinationLocation() 
        + " - BinEmptyDataCancel(11) Sent");
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpArc9yMessage.binEmptyCancel(vsTrackingId, 
        mpLoadEventData.getDestinationLocation(),
        mpLoadEventData.getDestinationEquipWarehouse(),
        mpLoadEventData.getDimensionInfo(), mpLoadEventData.getSourceStation());
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * turn the device online.   This is ID 16.
   */
  private void startDevice()
  {
    logDebug("SimultaneousStartCmd - 16 - startDevice()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_GO_ONLINE);
    mpArc9yMessage.startDevice();
    autoPublishDateAndTime = true;
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * turn the device offline.   This is ID 16.
   */
  private void stopDevice()
  {
    logDebug("SimultaneousStopCmd - 16 - stopDevice()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_GO_OFFLINE);
    mpArc9yMessage.stopDevice();
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * send a data message.   This is ID 50.
   */
  protected void sendDataMessage(LoadEventDataFormat mpLoadEventData)
  {
    String vsStationId = mpLoadEventData.getSourceStation();
    String vsTrackingId = vsStationId + "DATA";
    String s = "Station  " + vsStationId + " - Data Message (50) Sent";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpArc9yMessage.setDestinationStationNumber(vsStationId);
    mpArc9yMessage.setMCKey(vsTrackingId);
    mpArc9yMessage.dataMessageToString();
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * start a communications test.   This is ID 19.
   */
  private void StartCommTest()
  {
    mpArc9yMessage.StartCommTest();
  }

  /**
   * This method uses Arc9yMessage to format the message to the SRC/ARC to
   * respond to a communications test the the SRC/ARC started.   This is ID 20.
   */
  private void responseToCommTestDevice()
  {
    mpArc9yMessage.responseToCommTestDevice();
  }

  /**
   * We have received a message from the PORT that is connected to the actual
   * Device/Equipment that this AGCStationDevice is controlling.  This method
   * decodes the received text string and calls the correct method for the 
   * message received.  A stationEvent, LoadEvent or StatusEvent will be 
   * published depending on which message is received.
   * @param isReceivedText The received data (String)
   */
  @Override
  public void processEquipmentEvent(String isReceivedText)
  {
    mpArc9yMessage.toDataValues(isReceivedText);
    if ((mpArc9yMessage != null) && (!mpArc9yMessage.getValidMessage()))
    {
      String s = mpArc9yMessage.getInvalidMessageDescription();
      logger.logRxEquipmentMessage(isReceivedText, s);
      logger.logError("Arc9yDevice.processEquipmentEvent() -- " + s);
    }
    else
    {
      String s = mpArc9yMessage.getParsedMessageString();
      logger.logRxEquipmentMessage(isReceivedText, s);
      logger.logDebug("Msg ID: " + s);
      processEquipmentEventMessage(mpArc9yMessage.getID());
    }
  }

  protected void processEquipmentEventMessage(int messageId)
  {
    switch (messageId)
    {
      case 21:
        processStartResponse(); // ID 21
        break;
      case 22:
        processDateTimeRequest(); // ID 22
        break;
      case 23:
        processTerminateResponse(); // ID 23
        break;
      case 24:
        processResponseTransportDataCancel(); // ID 24
        break;
      case 25:
        processResponseToTransportCommand(); // ID 25
        break;
      case 26:
        processArrivalReport(); // ID 26
        break;
      case 27:
        processRequestDestinationStationChg(); // ID 27
        break;
      case 30:
        processMachineStatusReport(); // ID 30
        break;
      case 31:
        processAlternateLocation(); // ID 31
        break;
      case 32:
        processRetrieveCommandResponse(); // ID 32
        break;
      case 33:
        processOperationCompletionReport(); // ID 33
        break;
      case 35:
        processTransportDeletion(); // ID 35
        break;
      case 37:
        processDeviceStatusReport(); // ID 37
        break;
      case 39:
        processResponseCommTestRequest(); // ID 39
        break;
      case 40:
        processCommTestRequest(); // ID 40
        break;
      case 70:
        processMessageData(); // ID 70
        break;
      default:
        logger.logError("UNKNOWN AGC Message Type: " + messageId + " -- \""
            + getCommDeviceID()
            + "\" - Arc9yDevice.processEquipmentEventMessage()");
    }
  }

  /**
   * This method uses Arc9yMessage to decode the Start Responsemessage from the
   * SRC/ARC and then creates a StatusEvent using StatusEventFormat and
   * publishes the response. ID 21
   */
  private void processStartResponse()       // ID 21
  {
    logDebug("(21) Arc9yDevice.processStartResponse()");
    if(mpArc9yMessage.getResponseClassification() != 0) // if 00 normal, 03 AGC error, 99 data error
    {
      if(mpArc9yMessage.getResponseClassification() == 3)
      { // AGC Error
        setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_OFFLINE_SE);
      }
      else
      { // data error
        setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_OFFLINE_DE);
      }
    }
    else
    { // 0 normal ok online
      logger.logOperation(LogConsts.OPR_DEVICE, "Online");
      setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_ONLINE);
      setDetailedControllerStatus("Request Equipment Online (16)");
      startDevice();
      transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(), mpArc9yMessage.getParsedMessageString());
    }
  }

  /**
   * This method calls dateAndTime to send the data and time to the SRC/ARC
   * ID 22
   */
  private void processDateTimeRequest()     // ID 22
  {
    logDebug("(22) Arc9yDevice.processDateTimeRequest()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_RCV_DATETIME_REQ);
    dateAndTime();
  }

  /**
   * This method uses Arc9yMessage to decode the Terminate Response message from
   * the SRC/ARC and then creates a StatusEvent using StatusEventFormat and 
   * publishes the response.
   * ID 23
   */
  private void processTerminateResponse()   // ID 23
  {
    logDebug("(23) Arc9yDevice.processTerminateResponse()");
    if(mpArc9yMessage.getResponseClassification() > 0) // if 00 normal, 03 AGC error, 99 data error
    {
      if(mpArc9yMessage.getResponseClassification() == 1)
      { // AGC Termination impossible
        publishStatusEvent(mpArc9yMessage.getStatusForResponseToOperationTerminationRequest());
        setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_OFFLINE_NO);
      }
      else
      { // data error
        setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_OFFLINE_DE);
      }
    }
    else
    { // 0 normal ok online
      setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_OFFLINE);
    }
  }

  /**
   * This method uses Arc9yMessage to decode the Response Transport Data Cancel
   * message from the SRC/ARC and then creates a loadEvent using loadEventFormat
   * and publishes the response.
   * ID 24
   */
  private void processResponseTransportDataCancel()    // ID 24
  {
    logDebug("(24) Arc9yDevice.processResponseTransportDataCancel()");
    String mcKey = mpArc9yMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    setDetailedControllerStatus("LoadId \"" + vsLoadId + "\"  " +
        AGCDeviceConstants.DS_LD_XFR_STN_STN_RQ);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.responseTransportDataCancel(vsLoadId, 
        mpArc9yMessage.getResponseClassification()));
  }

  /**
   * This method uses Arc9yMessage to decode the Response To Transport Command
   * message from the SRC/ARC and then creates a loadEvent using loadEventFormat 
   * and publishes the response.
   * ID 25
   */
  private void processResponseToTransportCommand()    // ID 25
  {
    logDebug("(25) Arc9yDevice.processResponseToTransportCommand()");
    String mcKey = mpArc9yMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = "LoadId \"" + vsLoadId + "\" - \""
        + mpArc9yMessage.getStoreResponse()
        + "\" TransportCmdResponse(25) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.responseToTransportCommand(vsLoadId, 
        mpArc9yMessage.getResponseClassification()));
  }

  /**
   * This method uses Arc9yMessage to decode the Arrival Report message from 
   * the SRC/ARC and then creates a loadEvent using loadEventFormat and 
   * publishes the response.
   * ID 26
   */
  private void processArrivalReport()   // ID 26
  {
    logDebug("(26) Arc9yDevice.processArrivalReport() -  Height: " +
        mpArc9yMessage.getDimensionInformation());
    String s = null;
    String mcKey = mpArc9yMessage.getMCKey();
    String vsLoadId = mcKey;
    String vsBarcode = mpArc9yMessage.getBCData().trim();
    if (mcKey.equals(AGCDeviceConstants.AGCDUMMYLOAD))
    {
      //
      // A Barcode has been read at Store Station.  See if a child device may
      // need to massage the raw barcode.
      //
      vsBarcode = preProcessBarcode(vsBarcode);
      s = "LoadId \"" + vsBarcode + "\" - "
          + mpArc9yMessage.getArrivalStationNumber()
          + " Store Arrival Report (26) Received";
    }
    else
    {
      vsLoadId = getTrackingsLoadId(mcKey);
      s = "LoadId \"" + vsLoadId + "\" - "
          + mpArc9yMessage.getArrivalStationNumber()
          + " Retrieve Arrival Report (26) Received";
    }
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processArrivalReport(vsLoadId,
        mpArc9yMessage.getArrivalStationNumber(), 
        mpArc9yMessage.getDimensionInformation(),
        mpArc9yMessage.getLoadInformation(), vsBarcode, 
        mpArc9yMessage.getMCData()));
    logger.logDebug("- Arc9yDevice.processArrivalReport() data " 
        + vpLEDF.toString());
  }

  /**
   * Allows easy extensibility for customized bar code handling
   */
  protected String preProcessBarcode(String isBarcode)
  {
    return (isBarcode);
  }
  
  /**
   * This method uses Arc9yMessage to decode the Request Destination Station 
   * Change message from the SRC/ARC and then creates a loadEvent using 
   * loadEventFormat and publishes the response.
   * ID 27
   */
  private void processRequestDestinationStationChg()   // ID 27
  {
    logDebug("(27) Arc9yDevice.processRequestDestinationStationChg()");
    String mcKey = mpArc9yMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    setDetailedControllerStatus("LoadId \"" + vsLoadId + "\"  "
        + AGCDeviceConstants.DS_LD_XFR_STN_STN_RQ);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.requestDestinationStationChg(vsLoadId,
        mpArc9yMessage.getLocationNumber(), DEFAULT_SHELF_POSITION,
        mpArc9yMessage.getDestinationStationNumber(),
        mpArc9yMessage.getArcData()));
  }


  /**
   * This method uses Arc9yMessage to decode the Machine Status Report message
   * from the SRC/ARC and then creates a StatusEvent using StatusEventFormat 
   * and publishes the response.
   * ID 30
   */
  private void processMachineStatusReport()   // ID 30
  {
    logDebug("(30) Arc9yDevice.processMachineStatusReport()");
    setDetailedControllerStatus("Received Machine Status (30)");
    if (publishMachineStatuses)
    {
      publishStatusEvent(mpArc9yMessage.getMachineStatusMessage());
    }
    else
    {
      logger.logDebug("processMachineStatusReport -\n"
          + mpArc9yMessage.getMachineStatusMessage());
    }
    if (mpArc9yMessage.getContinuationClassification() == 0)
    {
      if (autoPublishDateAndTime)
      {
        dateAndTime();
        transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(),
            mpArc9yMessage.getParsedMessageString());
        autoPublishDateAndTime = false;
      }
    }
  }

  /**
   * This method uses Arc9yMessage to decode the Machine Status Report message
   * from the SRC/ARC and then creates a StatusEvent using StatusEventFormat 
   * and publishes the response.
   * ID 30
   */
  private void processDeviceStatusReport()   // ID 37
  {
    logDebug("(37) Arc9yDevice.processDeviceStatusReport()");
    setDetailedControllerStatus("Received Device Status (37)");
    if (publishMachineStatuses)
    {
      publishStatusEvent(mpArc9yMessage.getMachineStatusMessage());
    }
    else
    {
      logger.logDebug("processMachineStatusReport -\n"
          + mpArc9yMessage.getMachineStatusMessage());
    }
  }

  /**
   * This method uses Arc9yMessage to decode the Alternate Location message from
   * the SRC/ARC and then creates a loadEvent using loadEventFormat and 
   * publishes the response.
   * ID 31
   */
  private void processAlternateLocation()   // ID 31
  {
    logDebug("(31) Arc9yDevice.processAlternateLocation()");
    String mcKey = mpArc9yMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = mpArc9yMessage.getResponseDetails();
    setDetailedControllerStatus("LoadId \"" + vsLoadId + "\"  "
        + AGCDeviceConstants.DS_ALT_LOC_BIN_REQ);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processAlternateLocation(vsLoadId,
        mpArc9yMessage.getResponseClassification()));
  }

  /**
   * This method uses Arc9yMessage to decode the Retrieve Command Response 
   * message from the SRC/ARC and then creates a loadEvent using loadEventFormat 
   * and publishes the response.
   * ID 32
   */
  private void processRetrieveCommandResponse()   // ID 32
  {
    logDebug("(32) Arc9yDevice.processRetrieveCommandResponse()");
    // if the mc is all zeros then it is the second command which we don't 
    // send 2 retrieves at a time
    for(int i=0;i<2;i++)
    {
      if(!mpArc9yMessage.getRetrievalDataMCKey(i).equals("00000000"))
      {
        String mcKey = mpArc9yMessage.getRetrievalDataMCKey(i);
        String vsLoadId = getTrackingsLoadId(mcKey);
        String s = "LoadId \"" + vsLoadId + "\" - \""
            + mpArc9yMessage.getRetrievalResponse(i)
            + "\" RetrieveLoadResponse(32) Received";
        setDetailedControllerStatus(s);
        logger.logOperation(LogConsts.OPR_DEVICE, s);
        LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
            getCommDeviceID());
        transmitLoadEvent(vpLEDF.processRetrieveCommandResponse(
            vsLoadId,
            mpArc9yMessage.getRetrievalDataCompletionClassification(i)));
      }
    }
  }

  /**
   * This method uses Arc9yMessage to decode the Operation Completion Report
   * message from the SRC/ARC and then
   * creates a loadEvent using loadEventFormat and publishes the response.
   * ID 33
   */
  private void processOperationCompletionReport()     // ID 33
  {
    logDebug("(33) Arc9yDevice.processOperationCompletionReport()");
    String mcKey = mpArc9yMessage.getRetrievalDataMCKey(0);
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = "LoadId \"" + vsLoadId + "\" - \""
        + mpArc9yMessage.getRetrievalResponse(0)
        + "\" OperationCompleteReport(33) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    //...sk size of 0 ?
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processOperationCompletion(vsLoadId,
        mpArc9yMessage.getRetrievalDataTransportDivision(0),
        mpArc9yMessage.getRetrievalDataCompletionClassification(0),
        mpArc9yMessage.getRetrievalDataSourceStationNumber(0),
        mpArc9yMessage.getRetrievalDataDestinationStationNumber(0),
        mpArc9yMessage.getRetrievalDataLocationNumber(0),
        DEFAULT_SHELF_POSITION,
        mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(0),
        DEFAULT_SHELF_POSITION, 0, mpArc9yMessage.getRetrievalDataBCData(0),
        mpArc9yMessage.getRetrievalDataWorkNumber(0),
        "                              "));
    if (!mpArc9yMessage.getRetrievalDataMCKey(1).equals(Arc9yMessage.EMPTY_MCKEY))
    {
      //
      // We do have a valid second completion report.
      //
      mcKey = mpArc9yMessage.getRetrievalDataMCKey(1);
      vsLoadId = getTrackingsLoadId(mcKey);
      s = "LoadId \"" + vsLoadId + "\" - \""
          + mpArc9yMessage.getRetrievalResponse(1)
          + "\" OperationCompleteReport(33) Received";
      setDetailedControllerStatus(s);
      logger.logOperation(LogConsts.OPR_DEVICE, s);
      transmitLoadEvent(vpLEDF.processOperationCompletion(vsLoadId,
          mpArc9yMessage.getRetrievalDataTransportDivision(1),
          mpArc9yMessage.getRetrievalDataCompletionClassification(1),
          mpArc9yMessage.getRetrievalDataSourceStationNumber(1),
          mpArc9yMessage.getRetrievalDataDestinationStationNumber(1),
          mpArc9yMessage.getRetrievalDataLocationNumber(1),
          DEFAULT_SHELF_POSITION,
          mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(1),
          DEFAULT_SHELF_POSITION, 0, mpArc9yMessage.getRetrievalDataBCData(1),
          mpArc9yMessage.getRetrievalDataWorkNumber(1),
          "                              "));
    }
  }

  /**
   * This method uses Arc9yMessage to decode the Transport Deletion message from
   * the SRC/ARC and then
   * creates a loadEvent using loadEventFormat and publishes the response.
   * ID 35
   */
  private void processTransportDeletion()   //ID 35
  {
    logDebug("(35) Arc9yDevice.processTransportDeletion()");
    String mcKey = mpArc9yMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    setDetailedControllerStatus(AGCDeviceConstants.DS_DATA_DELETE_RCV);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processTransportDeletion(vsLoadId,
        mpArc9yMessage.getInabiltyToStartReason(),
        mpArc9yMessage.getRetrievalDataDestinationStationNumber(0),
        "                              "));
  }

  /**
   * This method uses Arc9yMessage to decode the Response Comm Test Request message
   * from the SRC/ARC and then
   * creates a StatusEvent using StatusEventFormat and publishes the response.
   * ID 39
   */
  private void processResponseCommTestRequest()   //ID 39
  {
    logDebug("(39) Arc9yDevice.processResponseCommTestRequest()");
    if (mpArc9yMessage.getCommunicationTestResult())
    {
      logger.logOperation(LogConsts.OPR_DEVICE, "Communication Test (39) - OK");
      setDetailedControllerStatus("Communication Test - OK");
    }
    else
    {
      setDetailedControllerStatus("Communication Test - *FAIL*");
      logger.logOperation(LogConsts.OPR_DEVICE, "Communication Test (39) - *FAIL*");
    }
  }

  /**
   * This method uses Arc9yMessage to decode the Comm Test Request message from
   * the SRC/ARC and then creates a StatusEvent using StatusEventFormat and 
   * publishes the response.
   * ID 40
   */
  private void processCommTestRequest()   //ID 40
  {
    logDebug("(40) Arc9yDevice.processCommTestRequest()");
    mpArc9yMessage.setCommunicationTestTextResponse(
        mpArc9yMessage.getCommunicationTestTextRequest());
    responseToCommTestDevice();
    transmitEquipmentEvent(mpArc9yMessage.getMessageAsString(), 
        mpArc9yMessage.getParsedMessageString());
  }

  /**
   * This method uses AgcMessage to decode the Message Data message from the 
   * SRC/AGC and then creates a StatusEvent using StatusEventFormat and 
   * publishes the response.
   * ID 70
   */
  protected void processMessageData()   // ID 70
  {
    logDebug("(70) AGCDevice.processMessageData()");
    setDetailedControllerStatus("Data Message (70) Rec'd");
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
          "Unable to create Arc9yDevice: DeviceID undefined");
    Controller vpController = Factory.create(Arc9yDevice.class, vsDeviceId);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }
}
