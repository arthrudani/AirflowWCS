package com.daifukuamerica.wrxj.emulation.agc;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.agc.AGCMCMessage;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A Device Controller that emulates a Daifuku AGC.  The AGC monitors the 
 * status of the AS/RS and its peripheral equipment, and schedules transfers
 * operations requested by the AGC Device Controller.
 *
 * <P>This is another intermediate step in accurate equipment simulation. This
 * queues up crane work so that multiple retrieves/stores are performed 
 * sequentially rather than concurrently.  Ideally, this would alternate between 
 * retrieves and stores, but it currently does not.</P>
 * 
 * <P>Please note that this emulator is *much* slower than the original, since
 * the original performs the work concurrently.</P>
 *
 * @author Michael Andrus
 */
public class AGCDeviceEmulator2 extends AGCDeviceEmulator
{
  private StandardSchedulerServer mpSchedServer;

  /**
   * Queue up the crane work and send the responses in sequence.
   * 
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   * 
   * @author mandrus
   * @version 1.0
   */
  private class CraneWork
  {
    String msCrane;
    Queue<AGCMCMessage> mpCraneResponses = new ConcurrentLinkedQueue<AGCMCMessage>();
    Queue<Integer> mpDelays = new ConcurrentLinkedQueue<Integer>();
    Date mpLastSentTime = new Date();
    Integer mnLock = 1;
    
    CraneWork(String isCrane)
    {
      msCrane = isCrane;
      mpTimer.schedule(new SendNextCraneResponse(msCrane), 15000);
    }
    
    void add(AGCMCMessage ipMessage, int inDelay)
    {
      inDelay = inDelay / mnSpeedFactor;
      synchronized (mnLock)
      {
        mnLock++;
        if (mpCraneResponses.size() == 0)
        {
          mpLastSentTime = new Date();
        }
        mpCraneResponses.add(ipMessage);
        mpDelays.add(inDelay);
        mnLock--;
      }
    }
    
    void sendNextResponse()
    {
      synchronized (mnLock)
      {
        mnLock++;
        Date vpRightNow = new Date();
        if (mpCraneResponses.size() > 0)
        {
          // TODO: Don't send if the crane is not online
          if (mpLastSentTime.getTime() + mpDelays.element() <= vpRightNow.getTime())
          {
            transmitMessageToMCDevice(mpCraneResponses.remove());
            mpDelays.remove();
            mpLastSentTime = vpRightNow;
          }
        }
        
        long vlNext = 2500;
        if (mpDelays.size() > 0)
        {
          vlNext = mpLastSentTime.getTime() + mpDelays.element() - vpRightNow.getTime();
          if (vlNext < 0) vlNext = 1;
        }
        mpTimer.schedule(new SendNextCraneResponse(msCrane), vlNext);
      }
      mnLock--;
    }
  }

  /**
   * Timer task to send queued responses
   */
  private class SendNextCraneResponse extends TimerTask
  {
    String msCrane;
    SendNextCraneResponse(String isCrane)
    {
      msCrane = isCrane;
    }
    
    @Override
    public void run()
    {
      mpCranes.get(msCrane).sendNextResponse();
    }
  }
  Map<String, CraneWork> mpCranes = new TreeMap<String, CraneWork>();
  
  /*========================================================================*/
  /*  Constructor                                                           */
  /*========================================================================*/
  
  /**
   * Public constructor for Factory
   */
  public AGCDeviceEmulator2()
  {
    super();
  }

  /*========================================================================*/
  /*  Controller methods                                                    */
  /*========================================================================*/
  
  @Override
  public void startup()
  {
    super.startup();
    
    mpLoadServer = Factory.create(StandardLoadServer.class);
    mpLocServer  = Factory.create(StandardLocationServer.class);
    mpSchedServer  = Factory.create(StandardSchedulerServer.class);

    // Queue up moves
    try
    {
      String vsDeviceID = getName().substring(0,4);
      mpCranes.put(vsDeviceID, new CraneWork(vsDeviceID));
      
      // If we're an AGC
      StandardDeviceServer vpDevServer = Factory.create(StandardDeviceServer.class);
      String[] vasCranes = vpDevServer.getDevicesByCommDeviceName(vsDeviceID);
      for (String vsCrane : vasCranes)
      {
        mpCranes.put(vsCrane, new CraneWork(vsCrane));
      }
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
  }

  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Device/Equipment.
   */
  @Override
  public void shutdown()
  {
    mpSchedServer.cleanUp();
    mpSchedServer = null;
    super.shutdown();
  }
  
  /*========================================================================*/
  /*  MC Messages                                                          */
  /*========================================================================*/

  /**
   * MC ID 05
   * <BR>Need to reply with a 25, 64, 33 (station to rack)
   * <BR>Need to reply with a 25, 64, 68, 26 (if required) (station to station)
   */
  @Override
  protected void processTransportCommand()
  {
    /*
     * First is responseToTransportCommand (25). This response is more or less
     * immediate, regardless of how much work is queued up for the crane.
     */
    AGCMCMessage vpAGCMCResponse_25 = new AGCMCMessage();
    vpAGCMCResponse_25.setMCKey(mpAGCMCMessage.getMCKey());
    vpAGCMCResponse_25.setResponseClassification(0); // Normal
    vpAGCMCResponse_25.responseToTransportCommandToString();
    
    setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_25), 60);

    /*
     * The rest of the messages are dependent upon what the crane already has
     * scheduled.
     */
    CraneWork vpCrane = null;
    try
    {
      String vsLoadID = mpSchedServer.getLoadIdFromTrackingId(
          mpAGCMCMessage.getMCKey());
      LoadData vpLData = mpLoadServer.getLoad(vsLoadID);
      LocationData vpLocData = mpLocServer.getLocationRecord(
          vpLData.getNextWarehouse(), vpLData.getNextAddress());
      vpCrane = mpCranes.get(vpLocData.getDeviceID());
    }
    catch (Exception e)
    {
      logger.logException(e, "Unable to send responses.");
      return;
    }

    if (mzUsePickupCompletion)
    {
      //
      // Next is a pickupCompletionReport (64).
      //
      AGCMCMessage vpAGCMCResponse_64 = new AGCMCMessage();
      vpAGCMCResponse_64.setMCKey(mpAGCMCMessage.getMCKey());
      vpAGCMCResponse_64.setSourceStationNumber(mpAGCMCMessage.getSourceStationNumber());
      vpAGCMCResponse_64.setNumberOfReports (1);
      vpAGCMCResponse_64.pickupCompletionReportToString();
  
      vpCrane.add(vpAGCMCResponse_64, storePickupCompletionDelay);
    }
    
    //
    // Next is a operationCompletionReport (33).
    //
    String vsDestStation = mpAGCMCMessage.getDestinationStationNumber();
    if (vsDestStation.equals(AGCDeviceConstants.RACKSTATION))
    {
      // Completion: 0 = Normal, 1 = Bin Full, 2 = Bin Empty, 3 = Size Mismatch
      int vnCompletion = 0;
      int vnDimension = mpAGCMCMessage.getDimensionInformation();
      
      AGCMCMessage vpAGCMCResponse_33 = new AGCMCMessage();
      vpAGCMCResponse_33.setRetrievalDataMCKey(0, mpAGCMCMessage.getMCKey());
      vpAGCMCResponse_33.setRetrievalDataTransportationClassification(0, mpAGCMCMessage.getTransportClassification());
      vpAGCMCResponse_33.setRetrievalDataCategory(0, 2);
      vpAGCMCResponse_33.setRetrievalDataCompletionClassification(0, vnCompletion);
      vpAGCMCResponse_33.setRetrievalDataSourceStationNumber(0, mpAGCMCMessage.getSourceStationNumber());
      vpAGCMCResponse_33.setRetrievalDataDestinationStationNumber(0, mpAGCMCMessage.getDestinationStationNumber());
      vpAGCMCResponse_33.setRetrievalDataLocationNumber(0, mpAGCMCMessage.getLocationNumber());
      vpAGCMCResponse_33.setRetrievalDataShelfPosition(0, mpAGCMCMessage.getShelfPosition());
//      vpAGCMCResponse_33.setRetrievalDataShelfToShelfLocationNumber(0, mpAGCMCMessage.getRetrievalDataShelfToShelfLocationNumber(0));
      vpAGCMCResponse_33.setRetrievalDataDimension(0, vnDimension);
      vpAGCMCResponse_33.setRetrievalDataBCData(0, mpAGCMCMessage.getBCData());
      vpAGCMCResponse_33.setRetrievalDataWorkNumber(0, mpAGCMCMessage.getWorkNumber());
      vpAGCMCResponse_33.setRetrievalDataControlInformation(0, mpAGCMCMessage.getControlInformation());
      vpAGCMCResponse_33.setRetrievalDataCategory(1, 0);
      vpAGCMCResponse_33.operationCompletionReportToString();

      vpCrane.add(vpAGCMCResponse_33, storeCompletionDelay);
    }
    else
    {
      // Handle representative stations
      String vsChildStn = mpStationServer.getReprStationChild(vsDestStation);
      if (vsChildStn.trim().length() > 0)
        vsDestStation = vsChildStn;

      // Does this station need an arrival?
      boolean vzSendArrival = needToSendArrival(vsDestStation);

      // Only send Trigger of Operation when arrivals are NOT required
      if (mzUseTriggerOfOperation && !vzSendArrival)
      {
        AGCMCMessage vpAGCMCResponse_68 = new AGCMCMessage();
        vpAGCMCResponse_68.setMCKey(mpAGCMCMessage.getMCKey());
        vpAGCMCResponse_68.setDestinationStationNumber(vsDestStation);
        vpAGCMCResponse_68.setControlInformation(mpAGCMCMessage.getControlInformation());
        vpAGCMCResponse_68.triggerOfOperationIndicationToString();

        vpCrane.add(vpAGCMCResponse_68, storeCompletionDelay);
      }

      /*
       * Only send the arrival if arrivals are required
       */
      if (vzSendArrival)
      {
        //
        // Next is arrivalReport (26)
        //
        AGCMCMessage vpAGCMCResponse_26 = new AGCMCMessage();
        vpAGCMCResponse_26.setMCKey(mpAGCMCMessage.getMCKey());
        vpAGCMCResponse_26.setArrivalStationNumber(vsDestStation);
        vpAGCMCResponse_26.setDimensionInformation(mpAGCMCMessage.getDimensionInformation());
        vpAGCMCResponse_26.setLoadInformation(1);
        vpAGCMCResponse_26.setBCData(mpAGCMCMessage.getBCData());
        vpAGCMCResponse_26.setControlInformation(mpAGCMCMessage.getControlInformation());
        vpAGCMCResponse_26.arrivalReportToString();

        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = storeCompletionDelay;
        vpCrane.add(vpAGCMCResponse_26, vnArrivalDelay);
      }
//      /*
//       * Auto-press the pick complete button
//       */
//      else if ((vpStationData != null) && (vpStationData.getStationType() == DBConstants.PDSTAND))
//      {
//        AGCMCMessage vpAGCMCResponse_26 = new AGCMCMessage();
//        vpAGCMCResponse_26.setMCKey("99999999");
//        vpAGCMCResponse_26.setArrivalStationNumber(mpAGCMCMessage.getDestinationStationNumber());
//        vpAGCMCResponse_26.setDimensionInformation(mpAGCMCMessage.getDimensionInformation());
//        vpAGCMCResponse_26.setLoadInformation(1);
//        vpAGCMCResponse_26.setBCData(mpAGCMCMessage.getBCData());
//        vpAGCMCResponse_26.setControlInformation(mpAGCMCMessage.getControlInformation());
//        vpAGCMCResponse_26.arrivalReportToString();
//        
//        vpCrane.add(vpAGCMCResponse_26, storeCompletionDelay);
//      }
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 12
   * <BR>Need to reply with a 32, 33, 68, 26
   */
  @Override
  protected void processRetrievalCmd()
  {
    /*
     * First is responseToRetrievalCmd (32). This response is more or less
     * immediate, regardless of how much work is queued up for the crane.
     */
    AGCMCMessage vpAGCMCResponse_32 = new AGCMCMessage();
    int vnRetDataTransClass = mpAGCMCMessage.getRetrievalDataTransportationClassification(0);
    vpAGCMCResponse_32.setMCKey(mpAGCMCMessage.getRetrievalDataMCKey(0));
    vpAGCMCResponse_32.setResponseClassification(0); // Normal
    vpAGCMCResponse_32.setMCKey2(mpAGCMCMessage.getRetrievalDataMCKey(1));
    vpAGCMCResponse_32.setResponseClassification2(0); // Normal
    vpAGCMCResponse_32.setRetrievalDataTransportationClassification(0, vnRetDataTransClass);
    vpAGCMCResponse_32.responseToRetrievalCmdToString();

    setSSTimerEvent(new AGCMCResponse(vpAGCMCResponse_32), 50);

    /*
     * The rest of the messages are dependent upon what the crane already has
     * scheduled.
     */
    CraneWork vpCrane = null;
    try
    {
      String vsLoadID = mpSchedServer.getLoadIdFromTrackingId(
          mpAGCMCMessage.getRetrievalDataMCKey(0));
      LoadData vpLData = mpLoadServer.getLoad(vsLoadID);
      LocationData vpLocData = mpLocServer.getLocationRecord(
          vpLData.getWarehouse(), vpLData.getAddress());
      vpCrane = mpCranes.get(vpLocData.getDeviceID());
    }
    catch (Exception e)
    {
      logger.logException(e, "Unable to send responses.");
      return;
    }
    if (vpCrane == null)
    {
      logger.logError("Unable to find crane for device.");
      return;
    }
    
    //
    // Next is a operationCompletionReport (33) for Retrieval pickup.
    //
    // Completion: 0 = Normal, 1 = Bin Full, 2 = Bin Empty, 3 = Size Mismatch
    int vnCompletion = 0;
    AGCMCMessage vpAGCMCResponse_33 = new AGCMCMessage();
    for (int i = 0; i < 2; i++)
    {
      vpAGCMCResponse_33.setRetrievalDataMCKey(i, mpAGCMCMessage.getRetrievalDataMCKey(i));
      vpAGCMCResponse_33.setRetrievalDataTransportationClassification(i, mpAGCMCMessage.getRetrievalDataTransportationClassification(i));
      vpAGCMCResponse_33.setRetrievalDataCategory(i, mpAGCMCMessage.getRetrievalDataCategory(i));
      vpAGCMCResponse_33.setRetrievalDataCompletionClassification(i, vnCompletion);
      vpAGCMCResponse_33.setRetrievalDataSourceStationNumber(i, mpAGCMCMessage.getRetrievalDataSourceStationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataDestinationStationNumber(i, mpAGCMCMessage.getRetrievalDataDestinationStationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataLocationNumber(i, mpAGCMCMessage.getRetrievalDataLocationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfPosition(i));
      vpAGCMCResponse_33.setRetrievalDataShelfToShelfLocationNumber(i, mpAGCMCMessage.getRetrievalDataShelfToShelfLocationNumber(i));
      vpAGCMCResponse_33.setRetrievalDataShelfToShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfToShelfPosition(i));
      vpAGCMCResponse_33.setRetrievalDataDimension(i, mpAGCMCMessage.getRetrievalDataDimension(i));
      vpAGCMCResponse_33.setRetrievalDataBCData(i, mpAGCMCMessage.getRetrievalDataBCData(i));
      vpAGCMCResponse_33.setRetrievalDataWorkNumber(i, mpAGCMCMessage.getRetrievalDataWorkNumber(i));
      vpAGCMCResponse_33.setRetrievalDataControlInformation(i, mpAGCMCMessage.getRetrievalDataControlInformation(i));
    }
    //
    // Bin-to-Bin move - Need Bin-to-Bin Retrieval (4)
    //
    if (vnRetDataTransClass == 5)
    {
      vpAGCMCResponse_33.setRetrievalDataTransportationClassification(0, 4);
    }
    vpAGCMCResponse_33.operationCompletionReportToString();
    
    vpCrane.add(vpAGCMCResponse_33, retrieveCompletionDelay);
    
    if (vnCompletion != 0)
    {
      return;
    }
    
    //
    //
    //
    if (vnRetDataTransClass == 2)
    {
      // Handle representative stations
      String vsDestStation = mpAGCMCMessage.getRetrievalDataDestinationStationNumber(0);
      String vsChildStn = mpStationServer.getReprStationChild(vsDestStation);
      if (vsChildStn.trim().length() > 0)
        vsDestStation = vsChildStn;

      // Does this station need an arrival?
      boolean vzSendArrival = needToSendArrival(vsDestStation);

      // Only send Trigger of Operation when arrivals are NOT required
      if (mzUseTriggerOfOperation && !vzSendArrival)
      {
        AGCMCMessage vpAGCMCResponse_68 = new AGCMCMessage();
        vpAGCMCResponse_68.setMCKey(mpAGCMCMessage.getRetrievalDataMCKey(0));
        vpAGCMCResponse_68.setDestinationStationNumber(vsDestStation);
        vpAGCMCResponse_68.setControlInformation(mpAGCMCMessage.getRetrievalDataControlInformation(0));
        vpAGCMCResponse_68.triggerOfOperationIndicationToString();
      
        vpCrane.add(vpAGCMCResponse_68, retrieveTriggerOfOperationIndicationDelay);
      }

      //
      // Next is arrivalReport (26)
      //
      // Only send if arrival is required
      //
      if (vzSendArrival)
      {
        AGCMCMessage vpAGCMCResponse_26 = new AGCMCMessage();
        vpAGCMCResponse_26.setMCKey(mpAGCMCMessage.getRetrievalDataMCKey(0));
        vpAGCMCResponse_26.setArrivalStationNumber(vsDestStation);
        vpAGCMCResponse_26.setDimensionInformation(mpAGCMCMessage.getRetrievalDataDimension(0));
        vpAGCMCResponse_26.setLoadInformation(1);
        vpAGCMCResponse_26.setBCData(mpAGCMCMessage.getRetrievalDataBCData(0));
        vpAGCMCResponse_26.setControlInformation(mpAGCMCMessage.getRetrievalDataControlInformation(0));
        vpAGCMCResponse_26.arrivalReportToString();
  
        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = retrieveArrivalReportDelay;
        vpCrane.add(vpAGCMCResponse_26, vnArrivalDelay);
      }
    }
    else
    {
      //
      // Next is a operationCompletionReport (33) for Retrieval store.
      //
      // Completion: 0 = Normal, 1 = Bin Full, 2 = Bin Empty, 3 = Size Mismatch
      int vnBinToBinCompletion = 0;
      AGCMCMessage vpAGCMCResponse_33b = new AGCMCMessage();
      for (int i = 0; i < 2; i++)
      {
        vpAGCMCResponse_33b.setRetrievalDataMCKey(i, mpAGCMCMessage.getRetrievalDataMCKey(i));
        vpAGCMCResponse_33b.setRetrievalDataTransportationClassification(i, mpAGCMCMessage.getRetrievalDataTransportationClassification(i));
        vpAGCMCResponse_33b.setRetrievalDataCategory(i, mpAGCMCMessage.getRetrievalDataCategory(i));
        vpAGCMCResponse_33b.setRetrievalDataCompletionClassification(i, vnBinToBinCompletion);
        vpAGCMCResponse_33b.setRetrievalDataSourceStationNumber(i, mpAGCMCMessage.getRetrievalDataSourceStationNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataDestinationStationNumber(i, mpAGCMCMessage.getRetrievalDataDestinationStationNumber(i));
        /*
         * For some unfathomable reason, the locations are REVERSED for the
         * shelf to shelf work complete.  It's just the way it is.
         */
        vpAGCMCResponse_33b.setRetrievalDataLocationNumber(i, mpAGCMCMessage.getRetrievalDataShelfToShelfLocationNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfToShelfPosition(i));
        vpAGCMCResponse_33b.setRetrievalDataShelfToShelfLocationNumber(i, mpAGCMCMessage.getRetrievalDataLocationNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataShelfToShelfPosition(i, mpAGCMCMessage.getRetrievalDataShelfPosition(i));
        vpAGCMCResponse_33b.setRetrievalDataDimension(i, mpAGCMCMessage.getRetrievalDataDimension(i));
        vpAGCMCResponse_33b.setRetrievalDataBCData(i, mpAGCMCMessage.getRetrievalDataBCData(i));
        vpAGCMCResponse_33b.setRetrievalDataWorkNumber(i, mpAGCMCMessage.getRetrievalDataWorkNumber(i));
        vpAGCMCResponse_33b.setRetrievalDataControlInformation(i, mpAGCMCMessage.getRetrievalDataControlInformation(i));
      }
      //
      // Bin-to-Bin move - Need Bin-to-Bin Store (5)
      //
      vpAGCMCResponse_33b.setRetrievalDataTransportationClassification(0, 5);
      vpAGCMCResponse_33b.operationCompletionReportToString();

      vpCrane.add(vpAGCMCResponse_33b, retrieveTriggerOfOperationIndicationDelay);
    }
  }
}
