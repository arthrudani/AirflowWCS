package com.daifukuamerica.wrxj.device.agc;

//                 Daifuku America Corporation
//                     International Center
//                 5202 Douglas Corrigan Way
//              Salt Lake City, Utah  84116-3192
//                      (801) 359-9900
//
// This software is furnished under a license and may be used and copied only in
// accordance with the terms of such license.  This software or any other copies
// thereof in any form, may not be provided or otherwise made available, to any
// other person or company without written consent from Daifuku America 
// Corporation.
//
// Daifuku America Corporation assumes no responsibility for the use or 
// reliability of software which has been modified without approval.

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.station.StationDevice;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.stationevent.StationEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import java.util.List;
import java.util.Map;

/**
 * This class does the specific messaging of messages for the AGC and SRC
 * devices. This class will take the generic LoadEvent messages and make
 * specific messages for the SRC or AGC.
 * 
 * @author Ed Askew
 * @version 1.0
 */
public class AGCStationDevice extends StationDevice
{
  // TODO: Localize this.
  protected AGCMCMessage mpAgcMessage;
  
  private DeviceData mpDeviceData;

  /**
   * The AGCStationDevice Constructor will pass the deviceID to the super class
   * to name this device. It will only be set at initialization.
   *
   * @param isDeviceID no information available
   */
  public AGCStationDevice(String isDeviceID)
  {
    super(isDeviceID);
  }

  /**
   * This method connects to the database and instantiates the loadEventData,
   * stationEventData, StatusEventData, loadServer, stationData.
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("AGCStationDevice.startup() - Start");

    StandardDeviceServer vpDevServer = Factory.create(StandardDeviceServer.class);
    mpDeviceData = vpDevServer.getDeviceData(getCommDeviceID());
//    vpDevServer.cleanUp(); Don't... this disconnects the other servers.
    
    mpAgcMessage = Factory.create(AGCMCMessage.class);

    logger.logDebug("AGCStationDevice.startup() - End");
  }

  /**
   * This method disconnects from the database and  the loadEventData,
   * stationEventData, StatusEventData, loadServer, stationData.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug("AGCStationDevice.shutdown() -- Start");
    mpAgcMessage = null;
    logger.logDebug("AGCStationDevice.shutdown() -- End");
    super.shutdown();
  }
  
  /**
   * Process a ControlEvent (probably sent from a User's form)
   */
  @Override
  protected void processControlEvent()
  {
    String vsStationId;
    
    if (controllerStatus != ControllerConsts.STATUS_RUNNING)
    {
      logger.logDebug("Controller NOT Running - Control Event DISCARDED - processControlEvent()");
      return;
    }
    
    // All of this controller's messages are text messages for now
    if (receivedData != 0)
    {
      logger.logError(getClass().getSimpleName()
          + ".processControlEvent() -- UNKNOWN Event Type: " + receivedData);
      return;
    }
    
    // Process the text message
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
          setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_GO_OFFLINE_1);
          mpAgcMessage.requestToTerminateOperation(0);
          transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), mpAgcMessage.getParsedMessageString());
          break;
          
        case ControlEventDataFormat.CHAR_REQUEST_STATUS:
          logDebug("MachineStatusInquiry - 10 - processControlEvent()");
          setDetailedControllerStatus(AGCDeviceConstants.DS_REQ_MACH_STATUS);
          mpAgcMessage.setStartStopClassification(1); // Start
          mpAgcMessage.machineStatusRequest();
          transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), mpAgcMessage.getParsedMessageString());
          break;
          
        case ControlEventDataFormat.CHAR_COMM_TEST:
          logDebug("CommunicationTestRequest - 19 - processControlEvent()");
          setDetailedControllerStatus("Communication Test Request (19)");
          logger.logOperation(LogConsts.OPR_DEVICE, "Communication Test Request (19)");
          mpAgcMessage.setCommunicationTestRandomTextRequest();
          mpAgcMessage.StartCommTest();
          transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), mpAgcMessage.getParsedMessageString());
          break;
          
        case ControlEventDataFormat.CHAR_START_EQUIPMENT:
          logDebug("SimultaneousStartStopCmd - 16 - processControlEvent()");
          setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_GO_ONLINE);
          mpAgcMessage.startDevice();
          transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), mpAgcMessage.getParsedMessageString());
          break;
          
        case ControlEventDataFormat.CHAR_STOP_EQUIPMENT:
          logDebug("SimultaneousStartStopCmd - 16 - processControlEvent()");
          setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_GO_OFFLINE);
          mpAgcMessage.stopDevice();
          transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), mpAgcMessage.getParsedMessageString());
          break;
          
        case ControlEventDataFormat.CHAR_RETRIEVE_MODE:
          logger.logDebug("RetrieveModeCmd - 42 - processControlEvent()");
          vsStationId = receivedText.substring(1);
          setStationToRetrieveMode(vsStationId);
          transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), mpAgcMessage.getParsedMessageString());
          break;
          
        case ControlEventDataFormat.CHAR_STORE_MODE:
          logger.logDebug("StoreModeCmd - 42 - processControlEvent()");
          vsStationId = receivedText.substring(1);
          setStationToStoreMode(vsStationId);
          transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), mpAgcMessage.getParsedMessageString());
          break;
          
        default:
          logger.logError("AGCStationDevice.processControlEvent() -- UNKNOWN Event Type \"" 
              + chr0 + "\" -- processControlEvent()");
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logException(e, "AGCStationDevice.processControlEvent() - \"" + receivedText + "\"");
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Start up the device
   */
  @Override
  protected void startupDevice()
  {
    setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_GO_ONLINE_1);
    mpAgcMessage.requestToStartOperation();
    transmitEquipmentEvent(mpAgcMessage.getMessageAsString(),
        mpAgcMessage.getParsedMessageString());
  }

  /**
   * The SRC has a tendency to forget bidirectional station modes when it is
   * rebooted.  Remind it.
   */
  protected void refreshBidirectionalStatuses()
  {
    try
    {
      // I don't like doing it this way, since there's no guarantee that the 
      // controllersKeyName will always be the device ID.  Unfortunately, I 
      // don't see the actual device stored anywhere.  TODO: Use device.
      List<Map> vasBidirectionalStations = 
          mpStationServer.getStationByDeviceList(controllersKeyName);
      StationData vpSD = Factory.create(StationData.class);
      for (Map m : vasBidirectionalStations)
      {
        vpSD.dataToSKDCData(m);
        if (vpSD.getStationType() == DBConstants.REVERSIBLE)
        {
          char vcMode = ControlEventDataFormat.CHAR_RETRIEVE_MODE;
          if (vpSD.getBidirectionalStatus() == DBConstants.STOREMODE)
          {
            vcMode = ControlEventDataFormat.CHAR_STORE_MODE;
          }
          publishControlEvent(ControlEventDataFormat.getModeChangeCommand(
              vcMode, vpSD.getStationName()),
              ControlEventDataFormat.TEXT_MESSAGE, controllersKeyName);
        }
      }
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
  }

  /**
   * These stationEvent Messages are unformatted using stationEventFormat
   * and then the method that handles each message type is called
   * @param isReceivedText of the stationEvent
   * @return true if the message could be sent, false if the message was not valid
   */
  @Override
  public boolean processStationEvent(String isReceivedText)
  {
    StationEventDataFormat vpSEDF = new StationEventDataFormat(getCommDeviceID());
    if (vpSEDF.decodeReceivedString(isReceivedText))
    {
      logger.logDebug("StationEventData IS Valid \"" + vpSEDF.toString() + "\"");
      mpAgcMessage.clearAgcMessageData();
      processStationEventMessage(vpSEDF);
      if (mpAgcMessage.getValidMessage())
      {
        transmitEquipmentEvent(mpAgcMessage.getMessageAsString(),
            mpAgcMessage.getParsedMessageString());
        return true;
      }
      else
      {
        logger.logError("AGCDevice.processStationEvent() -- "
            + mpAgcMessage.getInvalidMessageDescription() + " -- \""
            + mpAgcMessage.getMessageAsString() + "\"");
        return false;
      }
    }
    else
    {
        logger.logDebug("INVALID Message \"" + isReceivedText
          + "\" - AGCStationDevice.processStationEvent()");
        return false;
    }
  }
  
  /**
   * Determine the type of message and then call the correct method to process it.
   * @param messageId the type of message this is.
   */
  protected void processStationEventMessage(StationEventDataFormat ipSEDF)
  {
    switch (ipSEDF.getMessageID())
    {
      case AGCDeviceConstants.RTSAGCMODECHGRESP: // ID 41
        responseToOperationModeChangeRequest(ipSEDF);
        break;
      case AGCDeviceConstants.STOREMODE:
        setStationToStoreMode(ipSEDF.getStation()); // ID 42
        break;
        
      case AGCDeviceConstants.RETRIEVEMODE:
        setStationToRetrieveMode(ipSEDF.getStation()); // ID 42
        break;
      default:
        logger.logDebug("INVALID MessageID: " + ipSEDF.getMessageID()
            + " - processDataFromScheduler()");
    }
  }

  /**
   * These loadEvent Messages are unformatted using loadEventFormat and then the
   * method that handles each message type is called. They come from WRX-J and
   * are messages sent to the equipment.
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
      mpAgcMessage.clearAgcMessageData();
      processLoadEventMessage(vpLEDF);
      if (mpAgcMessage.getValidMessage())
      {
        transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), 
            mpAgcMessage.getParsedMessageString());
        return true;
      }
      else
      {
        logger.logError(getClass().getSimpleName() + ".processLoadEvent() -- "
            + mpAgcMessage.getInvalidMessageDescription() + " -- \""
            + mpAgcMessage.getMessageAsString() + "\"");
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
      case AGCDeviceConstants.DESTSTATIONCHANGE:
        destinationStationChange(ipLEDF); // ID 08
        break;
      case AGCDeviceConstants.DEVICESTATUS:
        machineStatusRequest(); // ID 10
        break;
      case AGCDeviceConstants.BINFULLNEWLOC:
        binFullNewLocation(ipLEDF); // ID 11
        break;
      case AGCDeviceConstants.BINFULLNONEWLOC:
          binFullNoNewLocation(ipLEDF);
          break;
      case AGCDeviceConstants.BINFULLMOVESTATION:
        binFullMoveStation(ipLEDF); // ID 11
        break;
      case AGCDeviceConstants.DIMENSIONWRONGNEWLOC:
        heightMisMatchNewLocation(ipLEDF); // ID 11
        break;
      case AGCDeviceConstants.DIMENSIONWRONGMOVESTATION:
        heigthMisMatchMoveStation(ipLEDF); // ID 11
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
        startCommTest(); // ID 19
        break;
      case AGCDeviceConstants.COMMTESTRESPONSE:
        responseToCommTestDevice(); // ID 20
        break;
      case AGCDeviceConstants.RTSAGCOPERCOMP:
        operationCompleteRequest(ipLEDF); // ID 45
        break;
      case AGCDeviceConstants.RESPONSETORETRIEVALTRIGGER:
    	  sendResponseToRetrivalTrigger(ipLEDF); // ID 46
          break;
      case AGCDeviceConstants.SEND_DATA_MESSAGE:
        sendDataMessage(ipLEDF); // ID 50
        break;
      case AGCDeviceConstants.DOOUTPUTINSTRUCTION:
        sendDOOutputInstruction(ipLEDF);
        break;
      default:
        logger.logDebug("INVALID MessageID: " + ipLEDF.getMessageID()
            + " - processDataFromScheduler()");
    }
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to turn it
   * on. This is ID 01.
   */
  private void turnOnDevice()
  {
    logDebug("Operation Start - 01 - turnOnDevice()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_GO_ONLINE_1);
    mpAgcMessage.requestToStartOperation();
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * turn it off.  This is ID 01.
   */
  private void turnOffDevice()
  {
    logDebug("RequestToTerminateOperation - 03/0 - turnOffDevice()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_GO_OFFLINE_0);
    // Request Classification 0 = regular termination
    mpAgcMessage.requestToTerminateOperation(0);
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * turn force it off.  This is ID 01.
   */
  private void forceDeviceOff()
  {
    logDebug("RequestToTerminateOperation - 03/1 - forceDeviceOff()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_GO_OFFLINE_1);
    // Request Classification 1 = unconditional termination with data retention
    mpAgcMessage.requestToTerminateOperation(1);
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * turn force it off and delete data.  This is ID 01.
   */
  private void forceDeviceOffDeleteData()
  {
    logDebug("RequestToTerminateOperation - 03/2 - forceDeviceOffDeleteData()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_GO_OFFLINE_2);
    // Request Classification 2 = unconditional termination with no data retention
    mpAgcMessage.requestToTerminateOperation(2);
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * store a load from a station to a rack location.  This is ID 05.
   */
  protected void storeLoad(LoadEventDataFormat ipLEDF)
  {
    logDebug("TransportCommand - 05 - storeLoad()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId + "\" - Store TransportCommand(05) Sent - SrcStn: " +
    ipLEDF.getSourceStation() + " DstLoc: " + ipLEDF.getDestinationLocation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpAgcMessage.storeLoad(vsTrackingId,
                           mpLoadServer.getLoadPresenceRequired(loadId),
                           ipLEDF.getSourceStation(),
                           ipLEDF.getDestinationLocation(),
                           ipLEDF.getDestinationLocnShelfPosition(),
                           ipLEDF.getFullBarCode(),
                           ipLEDF.getInformation(),
                           ipLEDF.getDimensionInfo());
  }
  
  /**
   * This method send the destination station change command
   * to the AGC.  It is the ID 08 COMMAND.
   * @param ipLEDF
   */
  protected void destinationStationChange(LoadEventDataFormat ipLEDF)
  {
    logDebug("Destination Change Command - 08 - destinationStationChange()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId + "\" - Destination change Cmd(08) Sent - DstLoc: " +
               ipLEDF.getDestinationLocation() + " DstStn: " + ipLEDF.getDestinationStation();
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    setDetailedControllerStatus(s);
    mpAgcMessage.rejectLoad(vsTrackingId, ipLEDF.getDestinationLocation(), 
        ipLEDF.getDestinationStation(), ipLEDF.getInformation());
    
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * store a load from a station to another station.  This is ID 05.
   */
  private void moveLoadStationStation(LoadEventDataFormat ipLEDF)
  {
    logDebug("TransportCommand - 05 - moveLoadStationStation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId + "\" - Transfer TransportCmd(05) Sent - SrcStn: " +
               ipLEDF.getSourceStation() + " DstStn: " + ipLEDF.getDestinationStation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpAgcMessage.moveLoadStationStation(vsTrackingId, mpLoadServer.getLoadPresenceRequired(loadId),
          ipLEDF.getSourceStation(), ipLEDF.getDestinationStation(),
          ipLEDF.getFullBarCode(), ipLEDF.getInformation(), ipLEDF.getDimensionInfo());
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * retrieve a load from a rack location to a station.  This is ID 12.
   *
   * @param inCommandNumber
   * @param ipLEDF
   */
  protected void retrieveLoadLocationStation(int inCommandNumber,
                                             LoadEventDataFormat ipLEDF)
  {
    logDebug("RetrievalCommand - 12 - retrieveLoadLocationStation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId  + "\" - RetrieveLoadCmd(12) Sent - SrcLoc: " +
    ipLEDF.getSourceLocation() + " DstStn: " + ipLEDF.getDestinationStation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);

//    int vnStationType = mpStationServer.getStationType(ipLEDF.getDestinationStation());
    mpAgcMessage.retrieveLoadLocationStation(inCommandNumber, vsTrackingId,
//                                             vnStationType,
                                             ipLEDF);
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * retrieve a load from a rack location and store it into another location
   * in the same aisle.  This is ID 12.
   *
   * @param inCommandNumber
   * @param ipLEDF
   */
  protected void moveLoadLocationLocation(int inCommandNumber,
                                        LoadEventDataFormat ipLEDF)
  {
    logDebug("RetrievalCommand - 12 - moveLoadLocationLocation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + loadId  + "\" - Loc-Loc RetrieveLoadCmd(12) Sent - SrcLoc: " +
    ipLEDF.getSourceLocation() + " DstLoc: " + ipLEDF.getDestinationLocation();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpAgcMessage.moveLoadLocationLocation(inCommandNumber, vsTrackingId,
                                          ipLEDF.getSourceLocation(),
                                          ipLEDF.getSourceLocnShelfPosition(),
                                          ipLEDF.getDestinationLocation(),
                                          ipLEDF.getDestinationLocnShelfPosition(),
                                          ipLEDF.getDimensionInfo());
  }

  /**
   * Send the Date/Time to the AGC
   */
  private void dateAndTime()
  {
    logDebug("DateTimeDataResponse - 02 - dateAndTime()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_REQ_MACH_STATUS);
    mpAgcMessage.dateAndTime();
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * request a machine status report from the SRC/AGC.  This is ID 10.
   */
  private void machineStatusRequest()
  {
    logDebug("MachineStatusInquiry - 10 - machineStatusRequest()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_REQ_MACH_STATUS);
    mpAgcMessage.machineStatusRequest();
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * move a load that was storing to a location that already has a load in it
   * that we don't know about to a different location.  This is ID 11.
   */
  protected void binFullNewLocation(LoadEventDataFormat ipLEDF)
  {
    logDebug("AlternativeLocationCmd - 11 - binFullNewLocation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    setDetailedControllerStatus(AGCDeviceConstants.DS_ALT_LOC_BIN_REQ);
    logger.logDebug(" Destination Station for bin full = " 
        + ipLEDF.getDestinationLocation());
    mpAgcMessage.binFullNewLocation(vsTrackingId,
                                    ipLEDF.getDestinationLocation(),
                                    ipLEDF.getDestinationLocnShelfPosition(),
                                    ipLEDF.getDimensionInfo(),
                                    ipLEDF.getSourceStation());
  }
  
  /**
   * This method uses AgcMessage to format the message to the SRC/AGC when an
   * alternate location is not needed in a captive system. This is ID 11.
   */
  protected void binFullNoNewLocation(LoadEventDataFormat ipLEDF)
  {
      logDebug("AlternativeLocationCmd - 11 - binFullNoNewLocation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    setDetailedControllerStatus(AGCDeviceConstants.DS_ALT_LOC_BIN_REQ);
    mpAgcMessage.binFullNoNewLocation(vsTrackingId);
  }
  
  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to move a
   * load that was storing to a location that already has a load in it that we
   * don't know about to a station. This is ID 11.
   */
  protected void binFullMoveStation(LoadEventDataFormat ipLEDF)
  {
    logDebug("AlternativeLocationCmd - 11 - binFullNewStation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    setDetailedControllerStatus(AGCDeviceConstants.DS_ALT_LOC_STN_REQ);
    mpAgcMessage.binFullMoveStation(vsTrackingId,
        ipLEDF.getDestinationStation(), ipLEDF.getDimensionInfo());
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to move a
   * load that was storing to a location that already has a load in it that we
   * don't know about to a different location. This is ID 11.
   */
  protected void heightMisMatchNewLocation(LoadEventDataFormat ipLEDF)
  {
    logDebug("AlternativeLocationCmd - 11 - heightMisMatchNewLocation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    setDetailedControllerStatus(AGCDeviceConstants.DS_ALT_LOC_BIN_REQ);
    logger.logDebug(" Destination Station for Height MisMatch = " +
                    ipLEDF.getDestinationLocation());
    mpAgcMessage.heightMisMatchNewLocation(vsTrackingId,
                                       ipLEDF.getDestinationLocation(),
                                       ipLEDF.getDestinationLocnShelfPosition(),
                                       ipLEDF.getDimensionInfo(),
                                       ipLEDF.getSourceStation());
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to move a
   * load that was storing to a location that already has a load in it that we
   * don't know about to a station. This is ID 11.
   */
  private void heigthMisMatchMoveStation(LoadEventDataFormat ipLEDF)
  {
    logDebug("AlternativeLocationCmd - 11 - heightMisMatchNewStation()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    setDetailedControllerStatus(AGCDeviceConstants.DS_ALT_LOC_STN_REQ);
    mpAgcMessage.heightMisMatchMoveStation(vsTrackingId,
                                           ipLEDF.getDestinationStation(),
                                           ipLEDF.getDimensionInfo());
  }
   
  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to cancel
   * a load retrieval from a location that doesn't have a load in it but we
   * think it does. This is ID 11.
   */
  private void binEmptyCancel(LoadEventDataFormat ipLEDF)
  {
    logDebug("AlternativeLocationCmd - 11 - binEmptyCancel()");
    String loadId = ipLEDF.getLoadID();
    String vsTrackingId = getLoadsTrackingId(loadId);
    String s = "LoadId \"" + (loadId + "\" Location: " 
        + ipLEDF.getDestinationLocation() + " - BinEmptyDataCancel(11) Sent");
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpAgcMessage.binEmptyCancel(vsTrackingId, ipLEDF.getDestinationLocation(),
                                ipLEDF.getDestinationLocnShelfPosition(),
                                ipLEDF.getDimensionInfo(), ipLEDF.getSourceStation());
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * turn the device online.   This is ID 16.
   */
  protected void startDevice()
  {
    logDebug("SimultaneousStartCmd - 16 - startDevice()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_GO_ONLINE);
    mpAgcMessage.startDevice();
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * turn the device offline.   This is ID 16.
   */
  private void stopDevice()
  {
    logDebug("SimultaneousStopCmd - 16 - stopDevice()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_GO_OFFLINE);
    mpAgcMessage.stopDevice();
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * start a communications test.   This is ID 19.
   */
  private void startCommTest()
  {
    logDebug("StartCommTest - 19 - startCommTest()");
    mpAgcMessage.StartCommTest();
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * respond to a communications test the the SRC/AGC started.   This is ID 20.
   */
  private void responseToCommTestDevice()
  {
    logDebug("ResponseToCommTest - 20 - responseToCommTestDevice()");
    mpAgcMessage.responseToCommTestDevice();
  }
  
  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * notify the SRC/AGC that it is okay to put the station
   * into store mode.   This is ID 41.
   */
  private void responseToOperationModeChangeRequest(StationEventDataFormat ipSEDF)
  {
    logDebug("ResponseToOperationModeChangeRequest - 41 -"
        + " responseToOperationModeChangeRequest()");
    String vsResult = "Error";
    if(ipSEDF.getResults() == 0)
    {
      vsResult = "Normal";
    }
    String vsStatus = "Station: " + ipSEDF.getStation() + " \"Mode Change: "
        + vsResult + " \" - ResponseToOperationModeChangeRequest Sent";
    setDetailedControllerStatus(vsStatus);
    mpAgcMessage.setOperationModeChangeStation(ipSEDF.getStation());
    mpAgcMessage.setRequestResponse(ipSEDF.getResults());
    mpAgcMessage.responseToOperationModeChangeRequestToString();
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * command the SRC/AGC to put the station into store mode.   This is ID 42.
   */
  private void setStationToStoreMode(String isStationId)
  {
    logDebug("OperationModeChangeCmd - 42 - setStationToStoreMode()");
    String s = "Station: " + isStationId
        + " \"Store Mode (Normal)\" - OpModeChangeCmd(42) Sent";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpAgcMessage.setStationToStoreMode(isStationId);
    mpStationServer.setBidirectionalMode(isStationId, DBConstants.STOREMODE_SENT);
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * command the SRC/AGC to put the station into retrieve mode.   This is ID 42.
   */
  private void setStationToRetrieveMode(String isStationId)
  {
    logDebug("OperationModeChangeCmd - 42 - setStationToRetrieveMode()");
    String s = "Station: " + isStationId
        + " \"Retrieve Mode (Normal)\" - OpModeChangeCmd(42) Sent";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpAgcMessage.setStationToRetrieveMode(isStationId);
    mpStationServer.setBidirectionalMode(isStationId, DBConstants.RETRIEVEMODE_SENT);
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * send an operation complete message   This is ID 45.
   */
  protected  void operationCompleteRequest(LoadEventDataFormat ipLEDF)
  {
    logDebug("TerminalOperationComplete - 45 - operationCompleteRequest()");
    mpAgcMessage.terminalOperationComplete(ipLEDF.getLoadID(), 
        ipLEDF.getSourceStation());
  }

  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * send a data message.   This is ID 50.
   */
  protected void sendDataMessage(LoadEventDataFormat ipLEDF)
  {
    logDebug("DataMessage - 50 - sendDataMessage()");
    String vsMsgData = ipLEDF.getMsgData();

    String s = "\"MessageData(50) Sent\" - MsgData: " + vsMsgData;
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    
    mpAgcMessage.dataMessageToString(vsMsgData);
  }
  
  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * send a data message.   This is ID 50.
   */
  protected void sendResponseToRetrivalTrigger(LoadEventDataFormat ipLEDF)
  {
    logDebug("DataMessage - 46 - sendDataMessage()");
    String vsStation = ipLEDF.getSourceStation();
    int dResult = ipLEDF.getResults();

    String s = "\"MessageData(46) Sent\" - Station: " + vsStation;
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    
    mpAgcMessage.responseToRetrievalTriggerToString(vsStation, dResult);
  }
  
  /**
   * This method uses AgcMessage to format the message to the SRC/AGC to
   * send DO Output Instruction message. This is ID 54. It is used to control
   * the light tower
   */
  protected void sendDOOutputInstruction(LoadEventDataFormat ipLEDF)
  {
    logDebug("DO Output Instruction- 54 - sendDOOutputInstruction()");
    String vsStation = ipLEDF.getSourceStation();
    String vsLamp = ipLEDF.getMsgData();
    int vnStatus = ipLEDF.getStatus();
    String s = "\"DO Output Instruction(54) Sent\" -Station: " + vsStation +
        " Lamp: " + vsLamp + " Status: " + vnStatus;
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    mpAgcMessage.doOutputInstructionToString(vsStation, vsLamp, vnStatus);
  }
  
  /**
   * We have received a message from the PORT that is connected to the actual
   * Device/Equipment that this AGCStationDevice is controlling. This method
   * decodes the received text string and calls the correct method for the
   * message received. A stationEvent, LoadEvent or StatusEvent will be
   * published depending on which message is received.
   * 
   * @param isReceivedText - The received data (String)
   */
  @Override
  public void processEquipmentEvent(String isReceivedText)
  {
    mpAgcMessage.toDataValues(isReceivedText);
    if ((mpAgcMessage != null) && (!mpAgcMessage.getValidMessage()))
    {
      String s = mpAgcMessage.getInvalidMessageDescription();
      logger.logRxEquipmentMessage(isReceivedText, s);
      logger.logError("AGCStationDevice.processEquipmentEvent() -- " + s);
    }
    else
    {
      String s = mpAgcMessage.getParsedMessageString();
      logger.logRxEquipmentMessage(isReceivedText, s);
      logger.logDebug("Msg ID: " + s);
      processEquipmentEventMessage(mpAgcMessage.getID());
    }
  }

  /**
   * 
   * @param messageId
   */
  protected void processEquipmentEventMessage(int messageId)
  {
    switch (messageId)
    {
      case AGCDeviceConstants.AGCRTSWORKSTARTED:
        processStartResponse(); // ID 21
        break;
      case AGCDeviceConstants.AGCRTSDATATIME:
        processDateTimeRequest(); // ID 22
        break;
      case AGCDeviceConstants.AGCRTSWORKSTOP:
        processTerminateResponse(); // ID 23
        break;
      case AGCDeviceConstants.AGCRTSDATACANCEL:
        processResponseTransportDataCancel(); // ID 24
        break;
      case AGCDeviceConstants.AGCRTSTRANSPORTRESPONSE:
        processResponseToTransportCommand(); // ID 25
        break;
      case AGCDeviceConstants.AGCRTSARRIVAL:
        processArrivalReport(); // ID 26
        break;
      case AGCDeviceConstants.AGCRTSREQDESTSTATCHG:
        processRequestDestinationStationChg(); // ID 27
        break;
      case AGCDeviceConstants.AGCRTSRESPDESTSTATCHG:
        processResponseDestinationStationChg(); // ID 28
        break;
      case AGCDeviceConstants.AGCRTSSTATUSREPORT:
        processMachineStatusReport(); // ID 30
        break;
      case AGCDeviceConstants.AGCRTSLOCATIONRETRYRESP:
        processAlternateLocation(); // ID 31
        break;
      case AGCDeviceConstants.AGCRTSRETIEVALRESP:
        processRetrieveCommandResponse(); // ID 32
        break;
      case AGCDeviceConstants.AGCRTSWORKCOMP:
        processOperationCompletionReport(); // ID 33
        break;
      case AGCDeviceConstants.AGCRTSTRACKINGDELETE:
        processTransportDeletion(); // ID 35
        break;
      case AGCDeviceConstants.AGCRTSSIMULTSTARTIM:
        processSimultaneousStartImproperReport(); // ID 36
        break;
      case AGCDeviceConstants.AGCRTSCOMMRESP:
        processResponseCommTestRequest(); // ID 39
        break;
      case AGCDeviceConstants.AGCRTSCOMMTESTREQ:
        processCommTestRequest(); // ID 40
        break;
      case AGCDeviceConstants.AGCRTSMODECHGREQ:
        processOperationModeChangeRequest(); // ID 61
        break;
      case AGCDeviceConstants.AGCRTSRESPMODECHG:
        processOperationModeChangeCmdResponse(); // ID 62
        break;
      case AGCDeviceConstants.AGCRTSMODECHGCOMP:
        processOperationModeChgCompletionReport(); // ID 63
        break;
      case AGCDeviceConstants.AGCRTSPICKUPCOMP:
        processPickupCompletionReport(); // ID 64
        break;
      case AGCDeviceConstants.AGCRTSRETVTRIG:
        processID66Mesg(); // ID 66
        break;
      case AGCDeviceConstants.AGCRTSTRIGOPER:
        processTriggerOfOperationIndication(); // ID 68
        break;
      case AGCDeviceConstants.AGCRTSAGCMESSAGEDATA:
        processMessageData(); // ID 70
        break;
      case AGCDeviceConstants.AGCRTSACCESSIMPLOC:
        processAccessImpossibleLocationsReport(); // ID 71
        break;
      //
      default:
        logger.logError("UNKNOWN AGC Message Type: " + messageId + " -- \""
            + getCommDeviceID()
            + "\" - AGCStationDevice.processEquipmentEventMessage()");
    }
  }

  /**
   * This method uses AgcMessage to decode the StartResponse message from the
   * SRC/AGC and then creates a StatusEvent using StatusEventFormat and
   * publishes the response. ID 21
   */
  protected void processStartResponse()       // ID 21
  {
    logDebug("(21) AGCDevice.processStartResponse()");
    String s = mpAgcMessage.getResponseDetails();
//    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    if(mpAgcMessage.getResponseClassification() != 0) // if 00 normal, 03 AGC error, 99 data error
    {
      if(mpAgcMessage.getResponseClassification() == 3)
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
      setDetailedControllerStatus(AGCDeviceConstants.DS_AGC_ONLINE);
    }

    // Start up
    startDevice();
    transmitEquipmentEvent(mpAgcMessage.getMessageAsString(),
        mpAgcMessage.getParsedMessageString());

    // Remind SRC about statuses
    refreshBidirectionalStatuses();
  }

  /**
   * This method calls dateAndTime to send the data and time to the SRC/AGC
   * ID 22
   */
  private void processDateTimeRequest()     // ID 22
  {
    logDebug("(22) AGCDevice.processDateTimeRequest()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_RCV_DATETIME_REQ);
    dateAndTime();
  }

  /**
   * This method uses AgcMessage to decode the Terminate Response message from
   * the SRC/AGC and then creates a StatusEvent using StatusEventFormat and 
   * publishes the response.
   * ID 23
   */
  protected void processTerminateResponse()   // ID 23
  {
    logDebug("(23) AGCDevice.processTerminateResponse()");
    if(mpAgcMessage.getResponseClassification() > 0) // if 00 normal, 03 AGC error, 99 data error
    {
      if(mpAgcMessage.getResponseClassification() == 1)
      { // AGC Termination impossible
        publishStatusEvent(mpAgcMessage.getStatusForResponseToOperationTerminationRequest());
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
   * This method uses AgcMessage to decode the Response Transport Data Cancel
   * message from the SRC/AGC and then creates a loadEvent using ipLEDF and
   * publishes the response.
   * ID 24
   */
  private void processResponseTransportDataCancel()
  {
    logDebug("(24) AGCDevice.processResponseTransportDataCancel()");
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    setDetailedControllerStatus("LoadId \"" + vsLoadId + "\"  "
        + AGCDeviceConstants.DS_LD_XFR_STN_STN_RQ);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.responseTransportDataCancel(vsLoadId,
        mpAgcMessage.getResponseClassification()));
  }

  /**
   * This method uses AgcMessage to decode the Response To Transport Command
   * message from the SRC/AGC and then creates a loadEvent using ipLEDF and 
   * publishes the response.
   * ID 25
   */
  private void processResponseToTransportCommand()
  {
    logDebug("(25) AGCDevice.processResponseToTransportCommand()");
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = "LoadId \"" + vsLoadId + "\" - \""
        + mpAgcMessage.getStoreResponse()
        + "\" TransportCmdResponse(25) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.responseToTransportCommand(vsLoadId,
        mpAgcMessage.getResponseClassification()));
  }

  /**
   * This method uses AgcMessage to decode the Arrival Report message from the 
   * SRC/AGC and then creates a loadEvent using loadEventFormat and publishes 
   * the response.
   * ID 26
   */
  private void processArrivalReport()   // ID 26
  {
    logDebug("(26) AGCDevice.processArrivalReport() -  Height: "
        + mpAgcMessage.getDimensionInformation());
    String s = null;
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = mcKey;
    String vsBarcode = mpAgcMessage.getBCData().trim();
    if (mcKey.equals(AGCDeviceConstants.AGCDUMMYLOAD))
    {
      //
      // A bar code has been read at Store Station.  See if a child device may
      // need to massage the raw bar code.
      //
      vsBarcode = preProcessBarcode(vsBarcode);
      s = "LoadId \"" + vsBarcode + "\" - "
          + mpAgcMessage.getArrivalStationNumber()
          + " Store Arrival Report (26) Received";
    }
    else
    {
      vsLoadId = getTrackingsLoadId(mcKey);
      s = "LoadId \"" + vsLoadId + "\" - "
          + mpAgcMessage.getArrivalStationNumber()
          + " Retrieve Arrival Report (26) Received";
    }
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    // AGCScheduler will get this msg and call processStoreArrival or processFinalArrival
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());

    String vsCmd = vpLEDF.processArrivalReport(vsLoadId,
                                        mpAgcMessage.getArrivalStationNumber(),
                                        mpAgcMessage.getDimensionInformation(),
                                        mpAgcMessage.getLoadInformation(),
                                        vsBarcode,
                                        mpAgcMessage.getControlInformation());
    transmitLoadEvent(vsCmd);
    logger.logDebug("- AGCDevice.processArrivalReport() data " + vpLEDF.toString());
  }

  /**
   * Allows easy extensibility for customized bar code handling
   */
  protected String preProcessBarcode(String isBarcode)
  {
    return (isBarcode);
  }
  
  /**
   * This method uses AgcMessage to decode the Request Destination Station Chg
   * message from the SRC/AGC and then creates a LoadEvent using 
   * LoadEventFormat and publishes the response.
   * ID 27
   */
  private void processRequestDestinationStationChg()   // ID 27
  {
    logDebug("(27) AGCDevice.processRequestDestinationStationChg()");
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    setDetailedControllerStatus("LoadId \"" + vsLoadId + "\"  "
        + AGCDeviceConstants.DS_LD_XFR_STN_STN_RQ);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
                                                getCommDeviceID());
    String vsPublishStr = vpLEDF.requestDestinationStationChg(vsLoadId,
        mpAgcMessage.getLocationNumber(), mpAgcMessage.getShelfPosition(),
        mpAgcMessage.getDestinationStationNumber(), mpAgcMessage.getAgcData());
    transmitLoadEvent(vsPublishStr);
  }

  /**
   * This method uses AgcMessage to decode the Response Destination Station Chg
   * message from the SRC/AGC and then
   * creates a loadEvent using loadEventFormat and publishes the response.
   * ID 28
   */
  private void processResponseDestinationStationChg()   // ID 28
  {
    logDebug("(28) AGCDevice.processResponseDestinationStationChg() ==========");
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = mpAgcMessage.getResponseDetails();
    setDetailedControllerStatus("LoadId \"" + vsLoadId + "\"  "
        + AGCDeviceConstants.DS_LD_XFR_STN_STN_RQ);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
                                                getCommDeviceID());
    String vsPublisStr = vpLEDF.responseDestinationStationChg(vsLoadId,
                                       mpAgcMessage.getResponseClassification(),
                                       mpAgcMessage.getLocationNumber(),
                                       mpAgcMessage.getShelfPosition(),
                                       mpAgcMessage.getDestinationStationNumber(),
                                       mpAgcMessage.getCommandClassification());
    transmitLoadEvent(vsPublisStr);
  }

  /**
   * This method uses AgcMessage to decode the Machine Status Report message
   * from the SRC/AGC and then creates a StatusEvent using StatusEventFormat 
   * and publishes the response.
   * ID 30
   */
  protected void processMachineStatusReport()   // ID 30
  {
    logDebug("(30) AGCDevice.processMachineStatusReport()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_RCV_MACH_STATUS);
    publishStatusEvent(mpAgcMessage.getMachineStatusMessage());
  }

  /**
   * This method uses AgcMessage to decode the Alternate Location message from
   * the SRC/AGC and then creates a loadEvent using loadEventFormat and 
   * publishes the response.
   * ID 31
   */
  private void processAlternateLocation()   // ID 31
  {
    logDebug("(31) AGCDevice.processAlternateLocation()");
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = mpAgcMessage.getResponseDetails();
    setDetailedControllerStatus("LoadId \"" + vsLoadId + "\"  "
        + AGCDeviceConstants.DS_ALT_LOC_BIN_REQ);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processAlternateLocation(vsLoadId,
        mpAgcMessage.getResponseClassification()));
  }

  /**
   * This method uses AgcMessage to decode the Retrieve Command Response message
   * from the SRC/AGC and then creates a loadEvent using loadEventFormat and 
   * publishes the response.
   * ID 32
   */
  private void processRetrieveCommandResponse()   // ID 32
  {
    logDebug("(32) AGCDevice.processRetrieveCommandResponse()");
    for(int i=0;i<2;i++)
    {
      /*
       * If the MCKey is all zeros then it is the second command which we don't
       * send 2 retrieves at a time
       */
      if(!mpAgcMessage.getRetrievalDataMCKey(i).equals("00000000"))
      {
        String mcKey = mpAgcMessage.getRetrievalDataMCKey(i);
        String vsLoadId = getTrackingsLoadId(mcKey);
        String s = "LoadId \"" + vsLoadId + "\" - \""
            + mpAgcMessage.getRetrievalCommandResponseString(i)
            + "\" RetrieveLoadResponse(32) Received";
        setDetailedControllerStatus(s);
        logger.logOperation(LogConsts.OPR_DEVICE, s);
        LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
            getCommDeviceID());
        transmitLoadEvent(vpLEDF.processRetrieveCommandResponse(
            vsLoadId, mpAgcMessage.getRetrievalDataCompletionClassification(i)));
      }
    }
  }

  /**
   * This method uses AgcMessage to decode the Operation Completion Report
   * message from the SRC/AGC and then creates a loadEvent using loadEventFormat 
   * and publishes the response.
   * ID 33
   */
  private void processOperationCompletionReport()     // ID 33
  {
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());

    logDebug("(33) AGCDevice.processOperationCompletionReport()");
    String mcKey = mpAgcMessage.getRetrievalDataMCKey(0);
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = "LoadId \"" + vsLoadId + "\" - \""
        + mpAgcMessage.getRetrievalResponse(0)
        + "\" OperationCompleteReport(33) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);

    String vsPublishStr = vpLEDF.processOperationCompletion(vsLoadId,
                   mpAgcMessage.getRetrievalDataTransportationClassification(0),
                   mpAgcMessage.getRetrievalDataCompletionClassification(0),
                   mpAgcMessage.getRetrievalDataSourceStationNumber(0),
                   mpAgcMessage.getRetrievalDataDestinationStationNumber(0),
                   mpAgcMessage.getRetrievalDataLocationNumber(0),
                   mpAgcMessage.getRetrievalDataShelfPosition(0),
                   mpAgcMessage.getRetrievalDataShelfToShelfLocationNumber(0),
                   mpAgcMessage.getRetrievalDataShelfToShelfPosition(0),
                   mpAgcMessage.getRetrievalDataDimension(0),
                   mpAgcMessage.getRetrievalDataBCData(0),
                   mpAgcMessage.getRetrievalDataWorkNumber(0),
                   mpAgcMessage.getRetrievalDataControlInformation(0));
    transmitLoadEvent(vsPublishStr);

    if (!mpAgcMessage.getRetrievalDataMCKey(1).equals(AGCDeviceConstants.EMPTYMCKEY))
    {
      //
      // We do have a valid second completion report.
      //
      mcKey = mpAgcMessage.getRetrievalDataMCKey(1);
      vsLoadId = getTrackingsLoadId(mcKey);
      s = "LoadId \"" + vsLoadId + "\" - \""
          + mpAgcMessage.getRetrievalResponse(1)
          + "\" OperationCompleteReport(33) Received";
      setDetailedControllerStatus(s);
      logger.logOperation(LogConsts.OPR_DEVICE, s);

      vsPublishStr = vpLEDF.processOperationCompletion(vsLoadId,
                   mpAgcMessage.getRetrievalDataTransportationClassification(1),
                   mpAgcMessage.getRetrievalDataCompletionClassification(1),
                   mpAgcMessage.getRetrievalDataSourceStationNumber(1),
                   mpAgcMessage.getRetrievalDataDestinationStationNumber(1),
                   mpAgcMessage.getRetrievalDataLocationNumber(1),
                   mpAgcMessage.getRetrievalDataShelfPosition(1),
                   mpAgcMessage.getRetrievalDataShelfToShelfLocationNumber(1),
                   mpAgcMessage.getRetrievalDataShelfToShelfPosition(1),
                   mpAgcMessage.getRetrievalDataDimension(1),
                   mpAgcMessage.getRetrievalDataBCData(1),
                   mpAgcMessage.getRetrievalDataWorkNumber(1),
                   mpAgcMessage.getRetrievalDataControlInformation(1));
      transmitLoadEvent(vsPublishStr);
    }
  }

  /**
   * This method uses AgcMessage to decode the Transport Deletion message from
   * the SRC/AGC and then creates a loadEvent using loadEventFormat and 
   * publishes the response.
   * ID 35
   */
  private void processTransportDeletion()   //ID 35
  {
    logDebug("(35) AGCDevice.processTransportDeletion()");
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    setDetailedControllerStatus(AGCDeviceConstants.DS_DATA_DELETE_RCV);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processTransportDeletion(vsLoadId,
        mpAgcMessage.getInabiltyToStartReason(), 
        mpAgcMessage.getDestinationStationNumber(),
        mpAgcMessage.getControlInformation()));
  }

  /**
   * This method uses AgcMessage to decode the Simultaneous Start Improper Report
   * message from the SRC/AGC and then creates a StatusEvent using 
   * StatusEventFormat and publishes the response.
   * ID 36
   */
  private void processSimultaneousStartImproperReport()   //ID 36
  {
    logDebug("(36) AGCDevice.processSimultaneousStartImproperReport()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_EQPMT_ONLINE_UNA);
  }

  /**
   * This method uses AgcMessage to decode the Response Comm Test Request message
   * from the SRC/AGC and then creates a StatusEvent using StatusEventFormat 
   * and publishes the response.
   * ID 39
   */
  private void processResponseCommTestRequest()   //ID 39
  {
    logDebug("(39) AGCDevice.processResponseCommTestRequest()");
    if (mpAgcMessage.getCommunicationTestResult())
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
   * This method uses AgcMessage to decode the Comm Test Request message from
   * the SRC/AGC and then creates an EquipmentEvent using EquipmentEventFormat
   * and publishes the response.
   * ID 40
   */
  private void processCommTestRequest()   //ID 40
  {
    logDebug("(40) AGCDevice.processCommTestRequest()");
    mpAgcMessage.setCommunicationTestTextResponse(
        mpAgcMessage.getCommunicationTestTextRequest());
    responseToCommTestDevice();
    transmitEquipmentEvent(mpAgcMessage.getMessageAsString(), 
        mpAgcMessage.getParsedMessageString());
  }
  
  /**
   * This method uses AgcMessage to decode the Operation Mode Change Request 
   * message from the SRC/AGC and then creates a StationEvent using 
   * stationEventFormat and publishes the message.
   * ID 61
   */
  private void processOperationModeChangeRequest() // ID 61
  {
    logDebug("(61) AGCDevice.processOperationModeChangeRequest()");
    String vsStatus = "Station - " + mpAgcMessage.getOperationModeChangeStation()
        + " Mode Change Requested  ID 61";
    setDetailedControllerStatus(vsStatus);
    StationEventDataFormat vpSEDF = new StationEventDataFormat(
        getCommDeviceID());
    transmitStationEvent(vpSEDF.processOperationModeChangeRequest(
        mpAgcMessage.getOperationModeChangeStation(),
        mpAgcMessage.getRequestClassification()));

  }
  

  /**
   * This method uses AgcMessage to decode the Operation Mode Change Cmd 
   * message from the SRC/AGC and then creates a StationEvent using 
   * stationEventFormat and publishes the response.
   * ID 62
   */
  private void processOperationModeChangeCmdResponse()              // ID 62
  {
    logDebug("(62) AGCDevice.processOperationModeChangeCmdResponse()");
    String s = mpAgcMessage.getResponseDetails();
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    StationEventDataFormat vpSEDF = new StationEventDataFormat(getCommDeviceID());
    transmitStationEvent(vpSEDF.processOperationModeChangeCmd(
        mpAgcMessage.getOperationModeChangeStation(),
        mpAgcMessage.getResponseClassification()));
  }

  /**
   * This method uses AgcMessage to decode the Operation Mode Change Report 
   * message from the SRC/AGC and then creates a StationEvent using 
   * stationEventFormat and publishes the response.
   * ID 63
   */
  private void processOperationModeChgCompletionReport()    // ID 63
  {
    logDebug("(63) AGCDevice.processOperationModeChgCompletionReport()");
    String cplMode = null;
    String vsBidirStatus = StatusEventDataFormat.STATUS_NOT_APPLICABLE;
    switch (mpAgcMessage.getCompletionMode())
    {
      case 1:
        cplMode = "Store"; 
        vsBidirStatus = StatusEventDataFormat.STATUS_STORE;
        break;
      case 2:
        cplMode = "Retrieve";
        vsBidirStatus = StatusEventDataFormat.STATUS_RETRIEVE;
        break;
      default: cplMode = "*UNKNOWN*: " + mpAgcMessage.getCompletionMode();
    }
    String s = "Station \"" + mpAgcMessage.getOperationModeChangeStation()
        + "\" Mode Changed To " + cplMode
        + " - Change Station Op Mode Completion Report (63) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    StationEventDataFormat vpStnEDF = new StationEventDataFormat(getCommDeviceID());
    transmitStationEvent(vpStnEDF.processOperationModeChgReport(
        mpAgcMessage.getOperationModeChangeStation(), 
        mpAgcMessage.getCompletionMode()));
  
    /*
     * Due to the wonky way the StatusModel works, ask the MOS device to update
     * the status of the station.
     * 
     * TODO: If Mike gets really bored, he should re-write the StatusModel.
     */
    if (mpDeviceData.getCommDevice().trim().length() > 0)
    {
      publishControlEvent(mpAgcMessage.getOperationModeChangeStation(),
          ControlEventDataFormat.MOS_STATUS_REQUEST,
          mpDeviceData.getCommDevice());
    }
    else
    {
      StatusEventDataFormat vpStatusEDF = new StatusEventDataFormat(getClass().getSimpleName());
      vpStatusEDF.setType(ControllerConsts.BIDIRECTIONAL_STATUS);
      vpStatusEDF.addBidirectionalStatus(
          mpAgcMessage.getOperationModeChangeStation(), vsBidirStatus);
      publishStatusEvent(vpStatusEDF.createStringToSend());
    }
  }
  
  /**
   * This method uses AgcMessage to decode the Pickup Completion Report message
   * from the SRC/AGC and then creates a loadEvent using loadEventFormat and 
   * publishes the response.
   * ID 64
   */
  private void processPickupCompletionReport()              // ID 64
  {
    logDebug("(64) AGCDevice.processPickupCompletionReport()");

    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = "LoadId \"" + vsLoadId + "\" - "
        + mpAgcMessage.getSourceStationNumber()
        + " Pickup Completion Report (64) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    // the AGC scheduler get this message
    transmitLoadEvent(vpLEDF.processPickupCompletionReport(vsLoadId,
        mpAgcMessage.getSourceStationNumber()));
    if(mpAgcMessage.getNumberOfReports() == 2)
    {
      mcKey = mpAgcMessage.getMCKey2();
      vsLoadId = getTrackingsLoadId(mcKey);
      s = "LoadId \"" + vsLoadId + "\" - "
          + mpAgcMessage.getSourceStationNumber()
          + " Pickup Completion Report (64) Received";
      setDetailedControllerStatus(s);
      logger.logOperation(LogConsts.OPR_DEVICE, s);
      transmitLoadEvent(vpLEDF.processPickupCompletionReport(vsLoadId,
          mpAgcMessage.getSourceStationNumber()));
    }
  }

  /**
   * This method uses AgcMessage to decode the Retrieval Trigger message from the
   * SRC/AGC and then
   * creates a loadEvent using loadEventFormat and publishes the response.
   * ID 66
   */
  private void processID66Mesg()              // ID 66
  {
    logDebug("(66) AGCDevice.processRetrievalTrigger()");
    String s = "Station " + mpAgcMessage.getRetrievalStationNumber()
        + " - Retrieval Trigger(66) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processmsg66Report(mpAgcMessage.getRetrievalStationNumber()));
  }

  /**
   * This method uses AgcMessage to decode the Trigger Of Operation Indication
   * message from the SRC/AGC and then
   * creates a loadEvent using loadEventFormat and publishes the response.
   * ID 68
   */
  private void processTriggerOfOperationIndication()      // ID 68
  {
    logDebug("(68) AGCDevice.processTriggerOfOperationIndication()");
    String mcKey = mpAgcMessage.getMCKey();
    String vsLoadId = getTrackingsLoadId(mcKey);
    String s = "LoadId \"" + vsLoadId + "\" - "
        + mpAgcMessage.getDestinationStationNumber()
        + " Operation Trigger(68) Received";
    setDetailedControllerStatus(s);
    logger.logOperation(LogConsts.OPR_DEVICE, s);
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getCommDeviceID());
    transmitLoadEvent(vpLEDF.processTriggerOfOperationIndication(vsLoadId,
          mpAgcMessage.getDestinationStationNumber(), 
          mpAgcMessage.getControlInformation()));
  }

  /**
   * ID 70 custom method stub to be implemented in extended class
   */
  protected void processMessageData()   // ID 70
  {
    
  }

  /**
   * This method uses AgcMessage to decode the Access Impossible Locations
   * Report message from the SRC/AGC and then does nothing.
   * TODO: Actually process this message
   * ID 71
   */
  private void processAccessImpossibleLocationsReport()   // ID 71
  {
    logDebug("(71) AGCDevice.processAccessImpossibleLocationsReport()");
    setDetailedControllerStatus(AGCDeviceConstants.DS_RCV_IMPOS_LOC);
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
          "unable to create AGCStationDevice: DeviceID undefined");
    Controller vpController = Factory.create(AGCStationDevice.class, vsDeviceId);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }
}
