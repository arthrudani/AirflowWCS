package com.daifukuamerica.wrxj.scheduler.agc;

/*
 *                 Daifuku America Corporation
 *                     International Center
 *                 5202 Douglas Corrigan Way
 *              Salt Lake City, Utah  84116-3192
 *                      (801) 359-9900
 *
 * This software is furnished under a license and may be used and copied only in
 * accordance with the terms of such license.  This software or any other copies
 * thereof in any form, may not be provided or otherwise made available, to any
 * other person or company without written consent from Daifuku America
 * Corporation.
 *
 * Daifuku America Corporation assumes no responsibility for the use or
 * reliability of software which has been modified without approval.
 */

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.stationevent.StationEventDataFormat;
import com.daifukuamerica.wrxj.scheduler.Scheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The AGCScheduler is the object that schedules all load movement between
 * SRC's, AGCs, and WRXJ. The AGCScheduler determines if a station needs more
 * loads staged for it. If it does a scheduler event is published to tell the
 * allocator to allocate an order for this station. If a station needs a load
 * moved from it to a storage location or if a load needs to be moved from a
 * storage location to the station based on priority. A load event is published
 * to a station device to move the load. The AGCScheduler updates the load move
 * status based on messages received from the station device through a load
 * event.
 *
 * @author Ed Askew
 * @version 1.0
 */

public class AGCScheduler extends Scheduler
{
  public static final String MSG_TRACKING_DELETE = "Tracking data deleted";

  protected StandardLoadServer mpLoadServer;
  protected StandardLocationServer mpLocServer;
  protected StandardRouteServer mpRouteServer;
  protected StandardSchedulerServer mpSchedServer;
  protected StandardStationServer mpStationServer;

  /**
   * Public constructor for Factory
   *
   * @param isName
   */
  public AGCScheduler(String isName)
  {
    super(isName);
  }

  /**
   * Method to Initialize everything need to run the AGCSCHEDULER.
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug(getClass().getSimpleName() + ".startup() - Start "
        + getSchedulerName());

    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpLocServer = Factory.create(StandardLocationServer.class);
    mpRouteServer = Factory.create(StandardRouteServer.class);
    mpSchedServer = Factory.create(StandardSchedulerServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);

    logger.logDebug(getClass().getSimpleName() + ".startup() - End");
  }

  /**
   *  Shuts down this controller by canceling any timers and shutting down the
   *  Equipment.
   */
  @Override
  public void shutdown()
  {
    logger.logDebug(getClass().getSimpleName() + ".shutdown() -- Start");

//    timers.cancel(mpWorkTask);
//    mpWorkTask = null;

    mpLoadServer.cleanUp();
    mpLoadServer = null;
    mpLocServer.cleanUp();
    mpLocServer = null;
    mpRouteServer.cleanUp();
    mpRouteServer = null;
    mpSchedServer.cleanUp();
    mpSchedServer = null;
    mpStationServer.cleanUp();
    mpStationServer = null;

    logger.logDebug(getClass().getSimpleName() + ".shutdown() -- End");
    super.shutdown();
  }

  /**
   * We have received an Station EVENT
   *
   * @param isReceivedEventString the received data
   */
  @Override
  protected void processStationEvent(String isReceivedEventString)
  {
    StationEventDataFormat vpSEDF = Factory.create(
        StationEventDataFormat.class, getSchedulerName());
    vpSEDF.decodeReceivedString(isReceivedEventString);
    decodeStationEvent(vpSEDF);
  }

  /**
   * Take the MessageId in the stationEventMessage and call the correct method
   * to process that message.
   *
   * @param ipSEDF <code>StationEventDataFormat</code>
   */
  protected void decodeStationEvent(StationEventDataFormat ipSEDF)
  {
    logger.logDebug(getClass().getSimpleName() + ".decodeStationEvent() - "
        + ipSEDF.getMessageID() + " - " + ipSEDF.getClass().getName());
    switch (ipSEDF.getMessageID())
    {
      case AGCDeviceConstants.AGCDEVICEOPERMODECHGREQ:  // ID 61
        logger.logDebug(getClass().getSimpleName()
            + ".decodeStationEvent () - Change Station Mode Request");
        processOperationModeChgReq(ipSEDF);
        break;
      case AGCDeviceConstants.AGCDEVICERESPOPERMODECHGCMD: // ID 62
        logger.logDebug(getClass().getSimpleName()
            + ".decodeStationEvent() - Change Station Mode Response");
        processOperationModeChgResp(ipSEDF);
        break;

      case AGCDeviceConstants.AGCDEVICEOPERMODECHGCOMPREPORT: // ID 63
        logger.logDebug(getClass().getSimpleName()
            + ".decodeStationEvent() - Change Station Mode Complete");
        processOperationModeChgComplete(ipSEDF);
        break;

      default:
        logger.logError(getClass().getSimpleName()
            + ".decodeStationEvent() - Unknown Message ID "
            + ipSEDF.getMessageID());
    }
  }

  /**
   * This method will process the change Mode request and create a response
   * to the SRC/AGC
   * @param ipSEDF
   */
  @SuppressWarnings("rawtypes")
  protected void processOperationModeChgReq(StationEventDataFormat ipSEDF)
  {

    String vsStation = ipSEDF.getStation();
    StationEventDataFormat vpOutSEDF = new StationEventDataFormat(
        getClass().getSimpleName());
    if (mpStationServer.exists(vsStation))
    {
      int vnResponse;
      Load vpLoad = Factory.create(Load.class);
      int vnClassification = ipSEDF.getResults();
      if (vnClassification == AGCDeviceConstants.STOREMODEREQ)
      {
        // default set to error, so we will respond even if there is an exception
        int vnEnrouteCount = 1;
        try
        {
          vnEnrouteCount = vpLoad.getEnrouteCountPlusAtStation(vsStation);
        }
        catch (DBException e)
        {
          logger.logError("Could not retrieve enroute count for Station: "
              + vsStation);
        }
        if (vnEnrouteCount == 0)
        {
          vnResponse = AGCDeviceConstants.CHANGEMODERESPNORMAL;
        }
        else
        {
          vnResponse = AGCDeviceConstants.CHANGEMODERESPERROR;
          logger.logError("******** Station " + vsStation + " has enroute loads still. " +
                          "STORE Mode change request failed! Clear retrieval data and try again! *******");
        }

      }
      else if (vnClassification == AGCDeviceConstants.RETRIEVALMODEREQ)
      {
         LoadData vpLoadData = Factory.create(LoadData.class);
         List<Map> vpLoadList = null;
         vpLoadData.setKey(LoadData.ADDRESS_NAME, vsStation);
         try
         {
           vpLoadList = mpLoadServer.getLoadDataList(vpLoadData);
         }
         catch (DBException e)
         {
           logger.logError("Could not retrieve Loads information at Station: "
               + vsStation);
         }
         if (vpLoadList != null && vpLoadList.size() == 0)
         {
           vnResponse = AGCDeviceConstants.CHANGEMODERESPNORMAL;
         }
         else
         {
           vnResponse = AGCDeviceConstants.CHANGEMODERESPERROR;
           logger.logError("******** Station " + vsStation + " has load(s) to store still. " +
                           "RETRIEVE Mode change request failed! Clear store data and try again! *******");
         }
      }
      else
      {
        vnResponse = AGCDeviceConstants.CHANGEMODERESPNORMAL;
      }
      try
      {
        String vsDevice = mpStationServer.getStationsDevice(vsStation);
        String vsCollaborator = getCollaboratorFromDevice(vsDevice);
        publishStationEvent(vpOutSEDF.processOperationModeChangeReqResponse(vsStation, vnResponse),0, vsCollaborator);
      }
      catch (DBException e)
      {
        logger.logException(e, "Error getting device for Station: " + vsStation);
      }
    }
    else //Station does not exist in system
    {
      logger.logError("Mode change station does not exist. Station: "
          + vsStation);
    }
  }

  /**
   * This process will send the command response to the station server to
   * process command.
   *
   * @param ipSEDF <code>StationEventDataFormat</code>
   */
  protected void processOperationModeChgResp(StationEventDataFormat ipSEDF)
  {
    String vsStation = ipSEDF.getStation();
    String vsError = "";
    int vnResults = ipSEDF.getResults();

    if (vnResults == 0)
    {
      /*
       * Update the station status
       */
      StationData vpSD = mpStationServer.getStation(vsStation);
      switch (vpSD.getBidirectionalStatus())
      {
        case DBConstants.RETRIEVEMODE_SENT:
          mpStationServer.setBidirectionalMode(vsStation, DBConstants.RETRIEVEMODE_PENDING);
          break;
        case DBConstants.STOREMODE_SENT:
          mpStationServer.setBidirectionalMode(vsStation, DBConstants.STOREMODE_PENDING);
          break;
        default:
          String vsStatus = "UNKNOWN";
          try
          {
            vsStatus = DBTrans.getStringValue(
                StationData.BIDIRECTIONALSTATUS_NAME,
                vpSD.getBidirectionalStatus());
          }
          catch (NoSuchFieldException nsfe)
          {
            vsStatus += "-" + vpSD.getBidirectionalStatus();
          }
          logger.logError("Station " + vsStation
              + " in unexpected state for mode change response: " + vsStatus);
      }
    }
    else
    {
      /*
       * Something bad happened
       */
      switch (vnResults)
      {
        case 1:    vsError = "Mode being Changed";                    break;
        case 2:    vsError = "Invalid Station Number";                break;
        case 3:    vsError = "Invalid Mode Commanded";                break;
        case 4:    vsError = "Transport Data/Yes";
          //  This happens when someone tries to put the station in Store Mode
          //  when it is picking.  In this case, just put the station in Store
          //  Mode.  For all intents and purposes, it is already.
          mpStationServer.setBidirectionalMode(vsStation, DBConstants.STOREMODE);
          break;
        default:   vsError = "Unknown Result (" + vnResults + ")";    break;
      }
      logger.logError("Error changing mode for " + vsStation + ": " + vsError);
    }
  }


  /**
   * Process the Operation Mode Change Complete
   *
   * @param ipSEDF <code>StationEventDataFormat</code>
   */
  protected void processOperationModeChgComplete(StationEventDataFormat ipSEDF)
  {
    String vsModalStation = ipSEDF.getStation();
    int vnBiMode = 0;
    boolean vzWriteIt = false;

    switch (ipSEDF.getResults())
    {
      case AGCDeviceConstants.COMPLETIONMODESTORE:           // Store
        vnBiMode = DBConstants.STOREMODE;
        vzWriteIt = true;
        break;

      case AGCDeviceConstants.COMPLETIONMODERETRIEVE:        // Retrieve
        vnBiMode = DBConstants.RETRIEVEMODE;
        vzWriteIt = true;
        break;

      default:
         break;
    }

    if (vzWriteIt)
    {
      mpStationServer.setBidirectionalMode(vsModalStation, vnBiMode);

      /*
       * Now schedule
       */
      if (vnBiMode == DBConstants.RETRIEVEMODE)
        checkIfStationHasLoadToRetrieve(vsModalStation);
      else
        checkIfStationHasLoadToStore(vsModalStation);
    }
  }


  /**
   * We have received a Load EVENT.  This method will decode the message and
   * determine which message was received.  The correct method will then be
   * called to handle that message.
   *
   * <P>The messages that are implemented are:
   * <LI>Transport Command ID-25
   * <LI>Arrival Report ID-26
   * <LI>Alternate Location Response ID-31
   * <LI>Retrieve Response ID-32
   * <LI>Work Complete ID-33
   * <LI>Transport Data Deletion ID-35
   * <LI>General Store - From a Store Screen
   * </P>
   *
   * @param receiveEventString the received data (String) is in "receivedText".
   */
  @Override
  protected void processLoadEvent(String receiveEventString)
  {
    LoadEventDataFormat vpLEDF = Factory.create(
        LoadEventDataFormat.class, getSchedulerName());
    vpLEDF.decodeReceivedString(receiveEventString);
    decodeLoadEvent(vpLEDF);
  }

  /**
   * Take the MessageId in the ipLEDF and call the correct method to process
   * that message.
   */
  protected void decodeLoadEvent(LoadEventDataFormat ipLEDF)
  {
    switch (ipLEDF.getMessageID())
    {
      case AGCDeviceConstants.AGCDEVICERESPTRANSPORTCOMMAND: // ID 25
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Store Response");
        processStoreResponse(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICEARRIVALREPORT: // ID 26
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Arrival Response");
        processArrivalReport(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICEREQDESTSTATCHG: // ID 27
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Request Dest Change");
        processReqDestChange(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICERESPDESTSTATCHG: // ID 28
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Request Dest Change Response");
        processStoreResponse(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICEALTLOCCOMMANDRESP :  // ID 31
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Alternate Location Response");
        processAltLocResponse(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICERESPRETRIEVECMD: // ID 32
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Retrieve Response");
        processRetrieveResponse(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICEOPERATIONCOMPLETION: // ID 33
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Work Complete Response");
        processWorkComplete(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICETRANDATADELREPORT: // ID 35
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Store Data Delete Response");
        processTransportDataDeletion(ipLEDF);
        break;
      case AGCDeviceConstants.GENERALSTORELOAD :    // store load from a screen
        processScreenRelease(ipLEDF);
        break;
      case AGCDeviceConstants.RTSAGCOPERCOMP :    // screen Release Or Operation complete  ID 45
                                                  // Message sent to the SRC so we don't need to process it
        break;
      case AGCDeviceConstants.AGCDEVICEPICKCOMPREPORT: // Pickup Completion
                                                       // Report ID 64
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Pickup Completion Report");
        processPickupCompletionReport(ipLEDF);
        break;
      case AGCDeviceConstants.AGCDEVICERETRIEVALTRIGGER: // Retrieval Trigger
          // Report ID 66
    	  logger.logDebug(getClass().getSimpleName()
    			  + ".decodeLoadEvent() - Retrieval Trigger Report");
    	  processRetrievalTriggerReport(ipLEDF);
    	  break;
      case AGCDeviceConstants.AGCDEVICETRIGGEROPERINDICAT: // Trigger command ID 68
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Trigger of Operation Report");
        processTriggerOperation(ipLEDF);
        break;
      case AGCDeviceConstants.MOVELOADSTATIONSTATION: // Trigger for station to station
        logger.logDebug(getClass().getSimpleName()
            + ".decodeLoadEvent() - Trigger for station to station");
        checkIfStationHasLoadToStore(ipLEDF.getSourceStation());
        break;
      case AGCDeviceConstants.AGCRTSAGCMESSAGEDATA: // Custom Message ID 70
        process70Message(ipLEDF);
        break;
      case AGCDeviceConstants.SEND_DATA_MESSAGE:
        // Message sent to the SRC so we don't need to process it.
        break;
      case AGCDeviceConstants.DOOUTPUTINSTRUCTION:
        //Rack status message sent to SRC so we don't need to process it
        break;
      case AGCDeviceConstants.MOVELOADLOCATIONLOCATION:
        // Location-to-location re-schedule.
        // Message sent to the SRC so we don't need to process it.
        break;
      default:
        logger.logError(getClass().getSimpleName()
            + ".decodeLoadEvent() - Unknown Message ID "
            + ipLEDF.getMessageID());
    }
  }

  /**
   * We have received an scheduler EVENT. This is a load that is retrieve
   * pending for one of this schedulers stations and I need to schedule it.
   * There may be higher priority or older loads waiting before it so check to
   * see if I can move any loads. If I can publish the loadEvent.
   *
   * @param receiveEventString the received data (String) is in "receivedText".
   */
  @Override
  protected void processSchedulerEvent(String receiveEventString)
  {
    AllocationMessageDataFormat vpAMDF = Factory.create(
        AllocationMessageDataFormat.class, getSchedulerName());
    vpAMDF.decodeReceivedString(receiveEventString);
    logger.logDebug("LoadId \"" + vpAMDF.getOutBoundLoad()
        + "\" Retrieve Pending to Station " + vpAMDF.getOutputStation()
        + " - " + getClass().getSimpleName() + ".processSchedulerEvent()");
    if (vpAMDF.validOutputStation())
    {
      try
      {
        String vsFromDevice = mpLocServer.getLocationDeviceId(
            vpAMDF.getFromWarehouse(), vpAMDF.getFromAddress());
        List<String> vpStns = mpRouteServer.getFirstStationsList(
            vpAMDF.getOutputStation(), vsFromDevice);
        for (String s : vpStns)
          checkIfStationHasLoadToRetrieve(s);
      }
      catch (DBException ex)
      {
        logger.logException(getClass().getSimpleName()
            + ".processSchedulerEvent", ex);
      }
    }
  }

  /**
   * See if there is a load to retrieve to the station. If there is take the
   * formatted message and publish it to the device.
   *
   * @param isStation of station to see if there is a load needed to be
   *            retrieved
   */
  protected void checkIfStationHasLoadToRetrieve(String isStation)
  {
    try
    {
      StationData vpSD = mpStationServer.getStation(isStation);
      if (vpSD != null)
      {
        retrieveAndStageForStation(vpSD);
  
        /*
         * Check to see if this station has representative station that should be
         * checked.
         */
        if (vpSD.getReprStationName().length() > 0)
        {
          vpSD = mpStationServer.getStation(vpSD.getReprStationName());
          if (vpSD != null)
          {
            retrieveAndStageForStation(vpSD);
          }
        }
      }
    }
    catch (DBException ex)
    {
      logger.logException(ex);
    }
  }

  /**
   * Look for retrieve pending loads for a station and send messages to get
   * those loads to retrieve.  Also send a message to the allocator to check to
   * see if we need more loads staged.
   *
   * @param ipSD
   * @throws DBException
   */
  protected void retrieveAndStageForStation(StationData ipSD)
    throws DBException
  {
    List<LoadEventDataFormat> vpList = getLoadToRetrieve(ipSD);
    for (LoadEventDataFormat vpLEDF : vpList)
    {
      String vsLocation = mpLoadServer.getLoadLocation(vpLEDF.getLoadID());
      String[] vasLoc = Location.parseLocation(vsLocation);
      String vsDevice = mpLocServer.getLocationDeviceId(vasLoc[0], vasLoc[1]);
      String loadRetrieveCommand = vpLEDF.createStringToSend();
      if( loadRetrieveCommand.length() > 0)
      {
        String vsCollaborator = getCollaboratorFromDevice(vsDevice);
        publishLoadEventMove(loadRetrieveCommand, vsCollaborator);
      }
    }

    // just took a staged load do we need another
    checkIfStationNeedsMoreStagedLoads(ipSD.getStationName());
  }

  /**
   * Resend a location to location retrieve command for a load that was
   * recovered.
   *
   * @param isLoadID
   */
  protected void resendLocToLocRetrieveCommand(String isLoadID)
  {
    try
    {
      LoadEventDataFormat vpLEDF = mpSchedServer.getLocToLocRetrieve(isLoadID);
      if (vpLEDF != null)
      {
        String vsLocation = mpLoadServer.getLoadLocation(vpLEDF.getLoadID());
        String[] vasLoc = Location.parseLocation(vsLocation);
        String vsDevice = mpLocServer.getLocationDeviceId(vasLoc[0], vasLoc[1]);
        String vsRetrieveCommand = vpLEDF.createStringToSend();
        if (vsRetrieveCommand.length() > 0)
        {
          String vsCollaborator = getCollaboratorFromDevice(vsDevice);
          publishLoadEventMove(vsRetrieveCommand, vsCollaborator);
        }
      }
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
  }

  /**
   * Send an alternate location command for a stuck load
   *
   * @param isMessage
   */
  protected void sendAltLocCommand(String isMessage)
  {
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        getSchedulerName());
    vpLEDF.decodeReceivedString(isMessage);

    StationData vpSD = mpStationServer.getStation(vpLEDF.getDestinationStation());
    String vsCollaborator = getCollaboratorFromDevice(vpSD.getDeviceID());

    publishLoadEventMove(isMessage, vsCollaborator);
  }

  /**
   * Checks certain stations for work.
   *
   * We have received a notification that this scheduler may have stations
   * who have lost triggers for scheduling events so they will be checked here.
   * The message received is in the format of
   *                        "TYPEXstn1Xstn2Xst3..."
   * where TYPE is the type of work to check for (staged, store, retrieve, etc.),
   * X is the delimiting character, and stn1, stn2, etc. are the stations to check.
   */
  @Override
  protected void processControlEvent()
  {
    ControlEventDataFormat vpCEDF = Factory.create(
        ControlEventDataFormat.class, getSchedulerName());

    if (receivedData == ControlEventDataFormat.TEXT_MESSAGE)
    {
      vpCEDF.parseCommandTargetListCommand(receivedText);
      String vsType = vpCEDF.getMessageCommand();
      List<String> vpStations = vpCEDF.getMessageTargets();

      if(vsType.equals(ControlEventDataFormat.STAGED))
      {
        for(String vsStation : vpStations)
        {
          checkIfStationNeedsMoreStagedLoads(vsStation);
        }
      }
      else if(vsType.equals(ControlEventDataFormat.RETRIEVE))
      {
        for(String vsStation : vpStations)
        {
          checkIfStationHasLoadToRetrieve(vsStation);
        }
      }
      else if(vsType.equals(ControlEventDataFormat.STORE))
      {
        for(String vsStation : vpStations)
        {
          checkIfStationHasLoadToStore(vsStation);
        }
      }
      else if (vsType.equals(ControlEventDataFormat.LOCTOLOC))
      {
        for(String vsLoadID : vpStations)
        {
          resendLocToLocRetrieveCommand(vsLoadID);
        }
      }
    }
    else if (receivedData == ControlEventDataFormat.RECOVERY_ALTLOC)
    {
      sendAltLocCommand(receivedText);
    }
  }

  /**
   * Process a transport data delete message
   *
   * @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processTransportDataDeletion(LoadEventDataFormat ipLEDF)
  {
    logger.logError("Ignoring Transport Data Delete for " + ipLEDF.getLoadID()
        + " at " + ipLEDF.getDestinationStation()
        + "; manually delete if necessary");

    LoadData vpLoadData = mpLoadServer.getLoad(ipLEDF.getLoadID());
    if (vpLoadData != null)
    {
      vpLoadData.setLoadMessage(MSG_TRACKING_DELETE);
      mpLoadServer.updateLoadData(vpLoadData, false);
    }
  }

  /**
   *  Determines which work complete message was sent and calls the correct method
   *  to process it.
   *  Normal work complete
   *    From a store of a load - processWorkCompleteStore()
   *    From a retrieve of a load - processWorkCompleteRetrieve()
   *  Error work complete
   *    From a store Bin Full - processBinFull()
   *    from a retrieve Bin Empty - processBinEmpty()
   *
   *  @param ipLEDF The decoded loadEvent String.
   */
  protected void processWorkComplete(LoadEventDataFormat ipLEDF)
  {
    switch(ipLEDF.getResults())
    {
      case  0 :  // Normal Completion
        switch(ipLEDF.getStatus())
        {
          case 1:   // Storage
            processWorkCompleteStore(ipLEDF);
            break;
          case 2:   // Retrieval
            logger.logDebug("LoadId \"" + ipLEDF.getLoadID() + "\" Normal Retrieval Work Complete");
            processWorkCompleteRetrieve(ipLEDF);
            break;
          case 4:   // Shelf-To-Shelf Retrieval
            logger.logDebug("LoadId \"" + ipLEDF.getLoadID() + "\" Normal Shelf-To-Shelf Retrieval Work Complete");
            processWorkCompleteRetrieve(ipLEDF);
            break;
          case 5:   // Shelf-To-Shelf Store
            logger.logDebug("LoadId \"" + ipLEDF.getLoadID() + "\" Normal Shelf-To-Shelf Store Work Complete");
            processShelfToShelfWorkCompleteStore(ipLEDF);
            //
            //... Do we need to do something here...??? <<=====================<<<
            //
            break;
          default:
            logger.logError("LoadId \"" + ipLEDF.getLoadID() + "\" Unknown Transaction Classification: " +
                            ipLEDF.getStatus() + "");
        }
        break;
      case 1:  // Bin Full
        processBinFull(ipLEDF);
        break;
      case 2:  // Bin Empty
        processBinEmpty(ipLEDF);
        break;
      case 3:   // Load/Location Size Mismatch
        processHeightMismatch(ipLEDF);
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID() + "\" Load/Location Size (" +
                        ipLEDF.getDimensionInfo() + ") Mismatch Work Complete");
        break;
      case 7:   // Empty Location
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID() + "\" Empty Location Work Complete");
        break;
      case 8:   // Stored Location
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID() + "\" Stored Location Work Complete");
        break;
      case 9:  // Cancel
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID() + "\" Cancel Work Complete");
        processWorkCompleteCancel(ipLEDF);
        break;
      default :
        logger.logError("Unknown WorkComplete Classification: "
            + ipLEDF.getResults() + " - " + getClass().getSimpleName()
            + ".processWorkComplete()");
    }
  }

  /**
   * Method stub to handle the custom ID 70 message from the station device
   * @param ipLEDF
   */
  protected void process70Message(LoadEventDataFormat ipLEDF)
  {

  }

  /**
   *  Notify the loadServer that there is not a load at this location.
   *  Cancel the command to the crane and log an error.
   *
   *  @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processBinEmpty(LoadEventDataFormat ipLEDF)
  {
    logger.logError("LoadId \"" + ipLEDF.getLoadID()
        + "\" Bin Empty at Location: " + ipLEDF.getSourceLocation());
    String vsEquipmentWarehouse = " ";
    try
    {
      LoadData vpLoadData = mpLoadServer.getLoad(ipLEDF.getLoadID());
      if (vpLoadData != null)
      {
        vsEquipmentWarehouse = mpLocServer.getEquipWarehouse(vpLoadData.getWarehouse());
      }
      mpSchedServer.createBinEmptyLoad(ipLEDF.getLoadID());
    }
    catch (Exception e)
    {
      logger.logException("processBinEmpty()", e);
    }
    finally
    {
      /*
       * ALWAYS send the BinEmptyCancel, no matter what happened above
       */
      String vsMessage = ipLEDF.binEmptyCancel(ipLEDF.getLoadID(),
          vsEquipmentWarehouse, ipLEDF.getSourceLocation(),
          ipLEDF.getSourceLocnShelfPosition(), ipLEDF.getDimensionInfo());
      transmitLoadEvent(vsMessage, ipLEDF.getSendersName());
    }
  }

  /**
   *  Notify the loadServer that there is already a load at this location.
   *  Send command for new location of load and log an error.
   *
   *  @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processBinFull(LoadEventDataFormat ipLEDF)
  {
    String vsMessage = mpSchedServer.createBinFullLoadFindNewLocation(ipLEDF.getLoadID(), getSchedulerName());
    if (vsMessage == null) return;

    transmitLoadEvent(vsMessage, ipLEDF.getSendersName());
    /*
     * This a stupid hack required because, for some unfathomable reason, we
     * store the destination location for a transport command (05) in
     * getSourceLocation() instead of getDestinationLocation().
     */
    String vsBinFullLoc = ipLEDF.getDestinationLocation();
    if (vsBinFullLoc.contains(AGCDeviceConstants.EMPTYARCLOCATION))
    {
      vsBinFullLoc = ipLEDF.getSourceLocation();
    }
    logger.logError("LoadId \"" + ipLEDF.getLoadID()
        + "\" Bin Full at Location " + vsBinFullLoc);
  }

  /**
   *  Notify the loadServer that there is already a load at this location.
   *  Send command for new location of load and log an error.
   *
   *  @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processHeightMismatch(LoadEventDataFormat ipLEDF)
  {
    logger.logError("LoadId \"" + ipLEDF.getLoadID()
        + "\" Height MisMatch at Location " + ipLEDF.getSourceLocation());
    String vsMessage = mpSchedServer.findNewLocationForHeightMismatch(
        ipLEDF.getLoadID(), getSchedulerName(), ipLEDF.getDimensionInfo());
    if (vsMessage != null)
    {
      transmitLoadEvent(vsMessage, ipLEDF.getSendersName());
    }
  }

  /**
   * Process the Destination Change Request message
   */
  protected void processReqDestChange(LoadEventDataFormat ipLEDF)
  {
    logger.logOperation("Load \"" + ipLEDF.getLoadID()
        + "\" IMPOSSIBLE Location (ID27)");

    transmitLoadEvent(mpSchedServer.findRejectLocation(ipLEDF.getLoadID(),
        getSchedulerName(), ipLEDF.getInformation()));
    logger.logError("LoadId \"" + ipLEDF.getLoadID()
        + "\" Alternate Location Request ");
  }

  /**
   * Determine if this is a final arrival for a retrieve of a load or a dummy
   * arrival for store of a store.
   *
   * @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processArrivalReport(LoadEventDataFormat ipLEDF)
  {
    if (ipLEDF.isLoadDummyLoad())
    { // This is dummy arrival for a store
      logger.logDebug("LoadId \"" + ipLEDF.getBarCode() + "\" Store Arrival - "
          + getClass().getSimpleName() + ".processArrivalReport()");
      processStoreArrival(ipLEDF);
    }
    else
    { // This is final arrival for a retrieve
      logger.logDebug("LoadId \"" + ipLEDF.getLoadID()
          + "\" Retrieve Final Arrival - " + getClass().getSimpleName()
          + ".processArrivalReport()");
      processFinalArrival(ipLEDF);
    }
  }

  /**
   * Determine the type of store response ID 25 normal - just update load to
   * next status error - set load to error device error - roll back load status
   * to resent store command
   *
   * @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processStoreResponse(LoadEventDataFormat ipLEDF)
  {
    int responseCode;
    responseCode = ipLEDF.getResults();
    switch(responseCode)
    {
      case 0: // Normal
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID()
            + "\" Store Response OK");
        String stationName = mpSchedServer.updateLoadForStoreResponseOK(ipLEDF.getLoadID());
        if (stationName.length() > 0)
        {
          checkIfStationHasLoadToStore(stationName);
        }
        break;
      case 3: // AGC already has command for load
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\" Duplicate Command - Store Response Error-3");
        mpSchedServer.updateLoadForStoreResponseError(ipLEDF.getLoadID(),
            "Duplicate data");
        break;
      case 6: // crane or station offline
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID()
            + "\" AGC is OFF-LINE - Store Response Error");
        mpSchedServer.updateLoadForStoreResponseDeviceError(ipLEDF.getLoadID(),
            "Device offline");
        break;
      case 7: // Condition Error
        //
        // The Crane is either disconnected or we are telling it do something
        // that is not physically possible for it. Change Load Status to Error
        // and let someone recover it (Otherwise we'll just try again, and
        // fail again.
        //
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\" Condition ERROR (Transport Cmd IMPOSSIBLE to Implement)");
        mpSchedServer.updateLoadForStoreResponseError(ipLEDF.getLoadID(),
            "Transport impossible");
        break;
      case 11: // Buffer Full
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID()
            + "\" Buffer FULL - Store Response Error");
        mpSchedServer.updateLoadForStoreResponseError(ipLEDF.getLoadID(),
            "Buffer full");
        break;
      case 99: // Data Error
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID()
            + "\" Data ERROR - Store Response Error");
        mpSchedServer.updateLoadForStoreResponseError(ipLEDF.getLoadID(),
            "Data error");
        break;
      default:
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\"  - Store Response Error: " + responseCode + " - "
            + getClass().getSimpleName() + ".processTransportResponse()");
        mpSchedServer.updateLoadForStoreResponseError(ipLEDF.getLoadID(),
            "Unknown error - see log");
        break;
    }
  }

  /**
   * Determine the type of retrieve response ID 32
   * <UL>
   * <LI>normal - just update load to next status</LI>
   * <LI>error - set load to error</LI>
   * <LI>device error - roll back load status to resent retrieve command when
   *     device ready</LI>
   * </UL>
   *
   * @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processRetrieveResponse(LoadEventDataFormat ipLEDF)
  {
    int responseCode;
    int viMaxRetrieveSendsAllowed = 1;
    responseCode = ipLEDF.getResults();
    switch(responseCode)
    {
      case 0 :  // Normal
        logger.logDebug("LoadId \"" + ipLEDF.getLoadID()
            + "\" - Retrieve Response OK - " + getClass().getSimpleName()
            + ".processRetrieveResponse()");
        Map<String, String> vpCommandList = mpSchedServer.updateLoadForRetrieveResponseOK(
            ipLEDF.getLoadID(), getSchedulerName(), viMaxRetrieveSendsAllowed);
        for (String vsCommand : vpCommandList.keySet())
        {
          //
          // First char in String is ""R" for Retrieve command, "S" for Stage command.
          //
          char c = vsCommand.charAt(0);
          switch (c)
          {
            case 'R':
              publishLoadEventMove(vsCommand.substring(1), vpCommandList.get(vsCommand));
              break;
            case 'S':
//              publishMessageToAllocator(vsCommand.substring(1));
              break;
          }
        }
        break;
      case 3: // Already have command for load
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\"  - Retrieve Response Error (Duplicate data) - "
            + getClass().getSimpleName() + ".processRetrieveResponse()");
        mpSchedServer.updateLoadForRetrieveResponseError(ipLEDF.getLoadID(),
            "Duplicate data");
        break;
      case 6: // crane or station offline
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\"  - Retrieve Response Error - " + getClass().getSimpleName()
            + ".processRetrieveResponse()");
        mpSchedServer.updateLoadForRetrieveResponseDeviceError(
            ipLEDF.getLoadID(), "Device offline");
        break;
      case 11: // command buffer full
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\"  - Retrieve Response Error - " + getClass().getSimpleName()
            + ".processRetrieveResponse()");
        mpSchedServer.updateLoadForRetrieveResponseError(ipLEDF.getLoadID(),
            "Buffer full");
        break;
      case 99: // Data Error
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\"  - Retrieve Response Error - " + getClass().getSimpleName()
            + ".processRetrieveResponse()");
        mpSchedServer.updateLoadForRetrieveResponseError(ipLEDF.getLoadID(),
            "Data error");
        break;
      default:
        logger.logError("LoadId \"" + ipLEDF.getLoadID()
            + "\"  - Retrieve Response Error - " + getClass().getSimpleName()
            + ".processRetrieveResponse()");
        mpSchedServer.updateLoadForRetrieveResponseError(ipLEDF.getLoadID(),
            "Unknown error - see log");
        break;
    }
  }

  /**
   * The AGC/SRC decided to cancel our movement.
   *
   * @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processWorkCompleteCancel(LoadEventDataFormat ipLEDF)
  {
    mpSchedServer.updateLoadForWorkCompleteCancel(ipLEDF);
  }

  /**
   *  updates the load to be either moving if arrival required or arrived at
   *  the destination
   *
   *  @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processWorkCompleteRetrieve(LoadEventDataFormat ipLEDF)
  {
    String stationName = ipLEDF.getDestinationStation();
    String loadCommand = mpSchedServer.updateLoadForRetrieveComplete(ipLEDF);
    if(loadCommand.length() > 0)
    {// this is a message to tell store screen that a load is available for pick
    	// so this station doesn't get final arrivals
      publishLoadEvent(loadCommand, 0);
      // No final arrival so see in another retrieve pending to retrieve.
	    checkIfStationHasLoadToRetrieve(stationName);
    }
  }

  /**
   *  updates the load to be store to destination
   *
   *  @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processWorkCompleteStore(LoadEventDataFormat ipLEDF)
  {
    logger.logDebug("LoadId \"" + ipLEDF.getLoadID()
        + "\" Normal Store Work Complete");

    mpSchedServer.updateLoadForStoreComplete(ipLEDF);

    /*
     * If this is a P&D stand, we may be able to retrieve to it now.
     */
    checkIfStationHasLoadToRetrieve(ipLEDF.getSourceStation());
  }

  /**
   *  updates the load to be store to destination
   *
   *  @param ipLEDF The decoded loadEvent Sting.
   */
  protected void processShelfToShelfWorkCompleteStore(LoadEventDataFormat ipLEDF)
  {
    mpSchedServer.updateLoadForShelfToShelfStoreComplete(ipLEDF);
  }

  /**
   * Process the PickupCompletionReport (ID=64)
   *
   * If this is at a REVERSIBLE station, send the mode change command to put
   * the station in retrieve mode.
   *
   * @param ipLEDF the decoded loadEvent Sting.
   */
  public void processPickupCompletionReport(LoadEventDataFormat ipLEDF)
  {
    logger.logDebug(getClass().getSimpleName()
        + ".processPickupCompletionReport() - Received");

    /*
     * If this is at a REVERSIBLE station, send the mode change command to put
     * the station in retrieve mode.
     */
    String vsStationId = ipLEDF.getSourceStation();
    StationData vpSD = mpStationServer.getStation(vsStationId);
    if (vpSD.getStationType() == DBConstants.REVERSIBLE)
    {
      /*
       * If we're in captive insert mode, set the station to store so we can
       * store another load.  Otherwise, make sure that we're in retrieve mode
       * so we can retrieve the next load.
       */
      if (vpSD.getStatus() == DBConstants.CAPTIVEINSERT)
      {
        publishControlEvent(ControlEventDataFormat.getModeChangeCommand(
            ControlEventDataFormat.CHAR_STORE_MODE, vsStationId),
            ControlEventDataFormat.TEXT_MESSAGE, vpSD.getDeviceID());
      }
      else
      {
        publishControlEvent(ControlEventDataFormat.getModeChangeCommand(
            ControlEventDataFormat.CHAR_RETRIEVE_MODE, vsStationId),
            ControlEventDataFormat.TEXT_MESSAGE, vpSD.getDeviceID());
      }
    }
  }

  /**
   * Process the PickupCompletionReport (ID=46)
   *
   * This is the response to the retrieval trigger from the AGC
   *
   * @param ipLEDF the decoded loadEvent Sting.
   */
  public void processRetrievalTriggerReport(LoadEventDataFormat ipLEDF)
  {
    logger.logDebug(getClass().getSimpleName()
        + ".processRetrievalTriggerReport() - Received");

//    String vsStationId = ipLEDF.getSourceStation();
//    StationData vpSD = mpStationServer.getStation(vsStationId);

    try
    {
      logger.logDebug(getClass().getSimpleName()
          + ".processRetrievalTriggerReport() - Received");
      String newCommand = mpSchedServer.processRetrievalTrigger(ipLEDF);
      if( newCommand.length() > 0)
      {
        transmitLoadEvent(newCommand);
      }
    }
    catch (DBException dbe)
    {
      // TODO: Better exception handling
      logger.logException(dbe);
    }
  }

  /**
   * Updates nothing for baseline
   *
   * @param ipLEDF the decoded loadEvent Sting.
   * @throws DBException
   */
  public void processTriggerOperation(LoadEventDataFormat ipLEDF)
  {
    try
    {
      logger.logDebug(getClass().getSimpleName()
          + ".processTriggerOperation() - Received");
      String newCommand = mpSchedServer.updateLoadForTriggerOfOperation(ipLEDF);
      if( newCommand.length() > 0)
      {
        publishLoadEvent(newCommand,0);
      }
    }
    catch (DBException dbe)
    {
      // TODO: Better exception handling
      logger.logException(dbe);
    }
  }

  /**
  * load has just arrived at the output station Update the load and publish a
  * message to the pick screen of load arrival.
  *
  * @param ipLEDF decoded LoadEventDataFormat message
  */
  public void processFinalArrival(LoadEventDataFormat ipLEDF)
  {
    try
    {
      String stationName = ipLEDF.getSourceStation();
      String newCommand = mpSchedServer.updateLoadForFinalArrival(ipLEDF);
      if( newCommand.length() > 0)
      {
        publishLoadEvent(newCommand,0);
      }
      checkIfStationHasLoadToRetrieve(stationName);

      int vnStationType = mpStationServer.getStationType(stationName);
      if (vnStationType == DBConstants.PDSTAND ||
          vnStationType == DBConstants.REVERSIBLE)
      {
        checkIfStationHasLoadToStore(stationName);
      }
    }
    catch (DBException dbe)
    {
      // TODO: Better exception handling
      logger.logException(dbe);
    }
  }

  /**
   * The load is arrived at station if ready to store call
   * checkIfStationHasLoadToStore.
   *
   * @param ipLEDF decoded LoadEventDataFormat message
   */
  protected void processStoreArrival(LoadEventDataFormat ipLEDF)
  {
    int vnNextOper = mpSchedServer.updateLoadForStoreArrival(ipLEDF);
    if (vnNextOper == StandardSchedulerServer.STORE_LOAD_OK)
    {
      checkIfStationHasLoadToStore(ipLEDF.getSourceStation());
      if(mpStationServer.stationIsBidirectional(ipLEDF.getSourceStation()))
        checkIfStationHasLoadToRetrieve(ipLEDF.getSourceStation());
    }
  }

  /**
   * Process the Alternate Location Response message
   *
   * @param ipLEDF
   */
  protected void processAltLocResponse(LoadEventDataFormat ipLEDF)
  {
    // If the load is move sent, update it.
    mpSchedServer.updateLoadForAlternateLocationResponse(ipLEDF);
  }

  /**
   * Process a screen release event from a pick or store screen
   *
   * @param ipLEDF
   */
  protected void processScreenRelease(LoadEventDataFormat ipLEDF)
  {
    logger.logDebug(getClass().getSimpleName()
        + ".processScreenRelease() - General Store");
    try
    {
      if(mpSchedServer.joinLoads(ipLEDF.getSourceStation()))
      {
        checkIfStationHasLoadToStore(ipLEDF.getSourceStation());
      }
    }
    catch (DBException dbe)
    {
      // TODO: Better exception handling
      logger.logException(dbe);
    }
  }

  /**
   * See if there are enough retrieve pending loads for this station
   *
   * @param stationName the station to check
   */
  protected void checkIfStationNeedsMoreStagedLoads(String stationName)
  {
    // Let the allocator worry about this
    publishMessageToAllocator(stationName, -1);
  }

  /**
   * Get a list of one retrieve command
   *
   * @param ipSD
   * @return
   */
  protected List<LoadEventDataFormat> getLoadToRetrieve(StationData ipSD)
  {
    int numberCommands = 1;

    return mpSchedServer.anyLoadsToRetrieveToStation(ipSD, getSchedulerName(),
        numberCommands);
  }

  /**
   * Sees if the station has a load that needs storing. Store Pending
   *
   * @param stationName that need to be checked.
   * @return Formated command to publish to device
   */
  protected String getLoadToStore(StationData ipSD)
  {
  	String newCommand = "";
  	ArrayList<LoadEventDataFormat> loadEventList =
        mpSchedServer.anyLoadsToStoreAtStation(ipSD, getSchedulerName());
  	if (!loadEventList.isEmpty())
    {
      LoadEventDataFormat loadEvent = loadEventList.get(0);
      newCommand = loadEvent.createStringToSend();
    }
  	return newCommand;
  }

  /**
   * See if there are any loads to schedule a store from this station
   *
   * @param isStationName the station to check
   */
  protected void checkIfStationHasLoadToStore(String isStationName)
  {
    StationData vpSD = mpStationServer.getStation(isStationName);
    if (vpSD == null)
    {
      logger.logException(new DBException("Station \"" + isStationName
          + "\" not found!"));
    }
    else
    {
      String vsNewCommand = getLoadToStore(vpSD);
      String vsCollaborator = getCollaboratorFromDevice(vpSD.getDeviceID());
      if (vsNewCommand.length() > 0)
      {
        transmitLoadEvent(vsNewCommand, vsCollaborator);
      }
    }
  }

  /**
   * Send a message to the allocator that this station needs more loads staged.
   *
   * @param isStation the name of the station that needs retrieve pending loads
   * @param inNeeded the number of needed loads
   */
  protected void publishMessageToAllocator(String isStation, int inNeeded)
  {
    if(isStation != null)
    {
      AllocationMessageDataFormat vpAMDF = Factory.create(
          AllocationMessageDataFormat.class, getSchedulerName());
      vpAMDF.clear();
      vpAMDF.setOutputStation(isStation);
      vpAMDF.createDataString();
      transmitAllocateEvent(vpAMDF.createStringToSend(), inNeeded);
    }
  }

  /*--------------------------------------------------------------------------*/
  // Publish load to stationdevice and change status to retrievesent
  /*--------------------------------------------------------------------------*/

  /**
  * Send a loadEvent to who is subscribing to my move Commands.
  *
  * @param moveCommand the command to be published
  */
  protected void publishLoadEventMove(String moveCommand, String vsDevice)
  {
    transmitLoadEvent(moveCommand, vsDevice);
  }

  /**
   * Find out which of this scheduler's collaborators this device id refers to.
   * @param ipSD
   * @return
   */
  protected String getCollaboratorFromDevice(String isDevice)
  {
    // see if this device is a collaborator
    if(!mpCollaborators.contains(isDevice))
    {
      // if not, check the device's controlling device
      StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
      DeviceData vpDD = vpDevServ.getDeviceData(isDevice);
      if (vpDD == null)
      {
        logger.logError("Device \"" + isDevice + "\" not found!");
        return isDevice;
      }
      isDevice = vpDD.getCommDevice();
      if(!mpCollaborators.contains(isDevice))
      {
        // Still no luck, we'll just have to guess
        isDevice = mpCollaborators.get(0);
      }
    }
    return isDevice;
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
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    String vsName = ipConfig.getString(ControllerDefinition.CONTROLLER_NAME);
    if (vsName == null)
      throw new ControllerCreationException("Unable to create AGCScheduler: Name undefined");
    Controller vpController = Factory.create(AGCScheduler.class, vsName);
    vpController.setCollaboratorCKN(ipConfig.getString(COLLABORATOR));
    return vpController;
  }

}
