package com.daifukuamerica.wrxj.emulation.arc9y;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.device.arc9y.Arc9yMessage;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Arc9yDeviceEmulator2 extends Arc9yDeviceEmulator
{
  protected int mnSpeedFactor = 5;

  protected StandardSchedulerServer mpSchedServer;
  protected StandardLocationServer mpLocServer;
  protected StandardLoadServer mpLoadServer;

  /**
   * Constructor
   */
  public Arc9yDeviceEmulator2()
  {
    // Public constructor for Factory
  }

  /**
   * Queue up the crane work and send the responses in sequence.
   * 
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   * 
   * @author mandrus
   * @version 1.0
   */
  protected class CraneWork
  {
    String msCrane;
     Queue<Arc9yMessage> mpCraneResponses = new ConcurrentLinkedQueue<Arc9yMessage>();
    Queue<Integer> mpDelays = new ConcurrentLinkedQueue<Integer>();
    Date mpLastSentTime = new Date();
    Integer mnLock = 1;
    
    CraneWork(String isCrane)
    {
      msCrane = isCrane;
      mpTimer.schedule(new SendNextCraneResponse(msCrane), 15000);
    }
    
    public void add(Arc9yMessage ipMessage, int inDelay)
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
  public Map<String, CraneWork> mpCranes = new TreeMap<String, CraneWork>();
  

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
    //
    // First is responseToTransportCommand (25).
    //
    Arc9yMessage vpArc9yResponse_25 = Factory.create(Arc9yMessage.class, true);
    vpArc9yResponse_25.setMCKey(mpArc9yMessage.getMCKey());
    vpArc9yResponse_25.setResponseClassification(0); // Normal
    vpArc9yResponse_25.responseToTransportCommandToString();
    
    setSSTimerEvent(new Arc9yMCResponse(vpArc9yResponse_25, 60), mnSpeedFactor);


    /*
     * The rest of the messages are dependent upon what the crane already has
     * scheduled.
     */
    CraneWork vpCrane = null;
    try
    {
      String vsLoadID = mpSchedServer.getLoadIdFromTrackingId(
          mpArc9yMessage.getMCKey());
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

   
    //
    // Next is a operationCompletionReport (33).
    //
    String vsDestStation = mpArc9yMessage.getDestinationStationNumber();

    Arc9yMessage vpArc9yResponse_33 = Factory.create(Arc9yMessage.class, true);
    vpArc9yResponse_33.setRetrievalDataMCKey(0, mpArc9yMessage.getMCKey());
    vpArc9yResponse_33.setRetrievalDataTransportDivision(0, mpArc9yMessage.getTransportDivision());
    vpArc9yResponse_33.setRetrievalDataType(0, 2);
    vpArc9yResponse_33.setOperationCompleteRMNumber(msArc9yDeviceId);
    vpArc9yResponse_33.setRetrievalDataCompletionClassification(0, 0); // 0 = Normal
    vpArc9yResponse_33.setRetrievalDataSourceStationNumber(0, mpArc9yMessage.getSourceStationNumber());
    vpArc9yResponse_33.setRetrievalDataDestinationStationNumber(0, mpArc9yMessage.getDestinationStationNumber());
    vpArc9yResponse_33.setRetrievalDataLocationNumber(0, mpArc9yMessage.getLocationNumber(),mpArc9yMessage.getWarehouse());
//      vpArc9yResponse_33.setRetrievalDataShelfToShelfLocationNumber(0, mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(0));
    vpArc9yResponse_33.setRetrievalDataBCData(0, mpArc9yMessage.getBCData());
    vpArc9yResponse_33.setRetrievalDataWorkNumber(0, mpArc9yMessage.getWorkNumber());
    vpArc9yResponse_33.setRetrievalDataType(1, 0);
    vpArc9yResponse_33.operationCompletionReportToString();
    
    setSSTimerEvent(new Arc9yMCResponse(vpArc9yResponse_33, 60), mnSpeedFactor);
   
  if (!vsDestStation.equals(Arc9yMessage.RACK_STATION))
  {
      /*
       * Only send the arrival if arrivals are required
       */
      StandardStationServer vpStnServ =  Factory.create(StandardStationServer.class);
      boolean arrival = vpStnServ.doesStationGetRetrieveArrival(
          mpArc9yMessage.getDestinationStationNumber());
      if (arrival)
      {
        // check to see if this is a group station
        String vsChildStn = vpStnServ.getReprStationChild(vsDestStation);
        if (vsChildStn.trim().length() > 0)
          vsDestStation = vsChildStn;
        //
        // Next is arrivalReport (26)
        //
        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
        vpArc9yResponse_26.setMCKey(mpArc9yMessage.getMCKey());
        vpArc9yResponse_26.setArrivalStationNumber(vsDestStation);
        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
        vpArc9yResponse_26.setLoadInformation(1);
        vpArc9yResponse_26.setBCData(mpArc9yMessage.getBCData());
        vpArc9yResponse_26.arrivalReportToString();

        int vnArrivalDelay;
        if(mpStationServer.isSimulationOn(vsDestStation))
          vnArrivalDelay = mpStationServer.getStation(vsDestStation).getSimInterval();
        else
          vnArrivalDelay = storeCompletionDelay;
        vpCrane.add(vpArc9yResponse_26, vnArrivalDelay);
      }
  }  
//      /*
//       * Auto-press the pick complete button
//       */
//      else if ((vpStationData != null) && (vpStationData.getStationType() == DBConstants.PDSTAND))
//      {
//        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
//        vpArc9yResponse_26.setMCKey("99999999");
//        vpArc9yResponse_26.setArrivalStationNumber(mpArc9yMessage.getDestinationStationNumber());
//        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
//        vpArc9yResponse_26.setLoadInformation(1);
//        vpArc9yResponse_26.setBCData(mpArc9yMessage.getBCData());
//        vpArc9yResponse_26.setControlInformation(mpArc9yMessage.getControlInformation());
//        vpArc9yResponse_26.arrivalReportToString();
//        
//        vpDelays.add(storeCompletionDelay);
//        vpResponses.add(vpArc9yResponse_26);
//      }
    
  }

  /*------------------------------------------------------------------------*/
  /**
   * MC ID 12
   * <BR>Need to reply with a 32, 33, 68, 26
   */
  @Override
  protected void processRetrievalCmd()
  {
    //
    // First is responseToRetrievalCmd (32).
    //
    Arc9yMessage vpArc9yResponse_32 = Factory.create(Arc9yMessage.class, true);
    int vnRetDataTransClass = mpArc9yMessage.getRetrievalDataTransportDivision(0);
    vpArc9yResponse_32.setMCKey(mpArc9yMessage.getRetrievalDataMCKey(0));
    vpArc9yResponse_32.setResponseClassification(0); // Normal
    vpArc9yResponse_32.setMCKey2(mpArc9yMessage.getRetrievalDataMCKey(1));
    vpArc9yResponse_32.setResponseClassification2(0); // Normal
    vpArc9yResponse_32.setRetrievalDataTransportDivision(0, vnRetDataTransClass);
    vpArc9yResponse_32.responseToRetrievalCmdToString();
    
    setSSTimerEvent(new Arc9yMCResponse(vpArc9yResponse_32, 60), mnSpeedFactor);
    //
    // The rest of the messages are dependent upon what the crane already has
    // scheduled.
    //
    CraneWork vpCrane = null;
    try
    {
      String vsLoadID = mpSchedServer.getLoadIdFromTrackingId(
          mpArc9yMessage.getRetrievalDataMCKey(0));
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
    Arc9yMessage vpArc9yResponse_33 = Factory.create(Arc9yMessage.class, true);
    // Completion: 0 = Normal, 1 = Bin Full, 2 = Bin Empty, 9 = Cancel
    int vnCompletion = 0;
    for (int i = 0; i < 2; i++)
    {
      vpArc9yResponse_33.setRetrievalDataMCKey(i, mpArc9yMessage.getRetrievalDataMCKey(i));
      vpArc9yResponse_33.setRetrievalDataTransportDivision(i, mpArc9yMessage.getRetrievalDataTransportDivision(i));
      vpArc9yResponse_33.setRetrievalDataType(i, mpArc9yMessage.getRetrievalDataType(i));
      vpArc9yResponse_33.setRetrievalDataCompletionClassification(i, vnCompletion);
      vpArc9yResponse_33.setRetrievalDataSourceStationNumber(i, mpArc9yMessage.getRetrievalDataSourceStationNumber(i));
      vpArc9yResponse_33.setRetrievalDataDestinationStationNumber(i, mpArc9yMessage.getRetrievalDataDestinationStationNumber(i));
      vpArc9yResponse_33.setRetrievalDataLocationNumber(i, mpArc9yMessage.getRetrievalDataLocationNumber(i),mpArc9yMessage.getWarehouse());
      vpArc9yResponse_33.setRetrievalDataShelfToShelfLocationNumber(i, mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(i));
      vpArc9yResponse_33.setRetrievalDataBCData(i, mpArc9yMessage.getRetrievalDataBCData(i));
      vpArc9yResponse_33.setRetrievalDataWorkNumber(i, mpArc9yMessage.getRetrievalDataWorkNumber(i));
      vpArc9yResponse_33.setRetrievalDataMCData(i, mpArc9yMessage.getRetrievalDataMCData(i));
    }
    //
    // Bin-to-Bin move - Need Bin-to-Bin Retrieval (4)
    //
    int vn33Delay = retrieveCompletionDepositDelay;
    if (vnRetDataTransClass == 5)
    {
      vpArc9yResponse_33.setRetrievalDataTransportDivision(0, 4);
      vn33Delay = retrieveCompletionPickupDelay;
    }
    vpArc9yResponse_33.operationCompletionReportToString();
    
    vpCrane.add(vpArc9yResponse_33, vn33Delay);

    //
    // If the completion code is not 0, we're done here
    //
    if (vnCompletion != 0)
    {
         return;
    }

    if (vnRetDataTransClass == 2)
    {
      //
      // Next is arrivalReport (26)
      //
      // Only send if arrival is required
      //
      StandardStationServer vpStnServ =  Factory.create(StandardStationServer.class);
      boolean arrival = needToSendArrival(mpArc9yMessage.getRetrievalDataDestinationStationNumber(0));
      if (arrival)
      {
        // check to see if this is a group station
        String vsDestStn = mpArc9yMessage.getRetrievalDataDestinationStationNumber(0);
        String vsChildStn = vpStnServ.getReprStationChild(vsDestStn);
        if (vsChildStn.trim().length() > 0)
          vsDestStn = vsChildStn;
        
        Arc9yMessage vpArc9yResponse_26 = Factory.create(Arc9yMessage.class, true);
        vpArc9yResponse_26.setMCKey(mpArc9yMessage.getRetrievalDataMCKey(0));
        vpArc9yResponse_26.setArrivalStationNumber(vsDestStn);
        vpArc9yResponse_26.setDimensionInformation(mpArc9yMessage.getDimensionInformation());
        vpArc9yResponse_26.setLoadInformation(1);
        vpArc9yResponse_26.setBCData(mpArc9yMessage.getRetrievalDataBCData(0));
        vpArc9yResponse_26.arrivalReportToString();
  
        int vnArrivalDelay;
        if (mpStationServer.isSimulationOn(vsDestStn))
          vnArrivalDelay = mpStationServer.getStation(vsDestStn).getSimInterval();
        else
          vnArrivalDelay = retrieveArrivalReportDelay;
        vpCrane.add(vpArc9yResponse_26, vnArrivalDelay);
      }
    }
    else
    {
      //
      // Next is a operationCompletionReport (33) for Retrieval store.
      //
      Arc9yMessage vpArc9yResponse_33b = Factory.create(Arc9yMessage.class, true);
      for (int i = 0; i < 2; i++)
      {
        vpArc9yResponse_33b.setRetrievalDataMCKey(i, mpArc9yMessage.getRetrievalDataMCKey(i));
        vpArc9yResponse_33b.setRetrievalDataTransportDivision(i, mpArc9yMessage.getRetrievalDataTransportDivision(i));
        vpArc9yResponse_33b.setRetrievalDataType(i, mpArc9yMessage.getRetrievalDataType(i));
        vpArc9yResponse_33b.setRetrievalDataCompletionClassification(i, 0); // 0 = Normal
        vpArc9yResponse_33b.setRetrievalDataSourceStationNumber(i, mpArc9yMessage.getRetrievalDataSourceStationNumber(i));
        vpArc9yResponse_33b.setRetrievalDataDestinationStationNumber(i, mpArc9yMessage.getRetrievalDataDestinationStationNumber(i));
        vpArc9yResponse_33b.setRetrievalDataLocationNumber(i, mpArc9yMessage.getRetrievalDataLocationNumber(i), mpArc9yMessage.getWarehouse());
        vpArc9yResponse_33b.setRetrievalDataShelfToShelfLocationNumber(i, mpArc9yMessage.getRetrievalDataShelfToShelfLocationNumber(i),
                                                                          mpArc9yMessage.getWarehouse());
        vpArc9yResponse_33b.setRetrievalDataBCData(i, mpArc9yMessage.getRetrievalDataBCData(i));
        vpArc9yResponse_33b.setRetrievalDataWorkNumber(i, mpArc9yMessage.getRetrievalDataWorkNumber(i));
      }
      //
      // Bin-to-Bin move - Need Bin-to-Bin Store (5)
      //
      vpArc9yResponse_33b.setRetrievalDataTransportDivision(0, 5);
      vpArc9yResponse_33b.operationCompletionReportToString();

      vpCrane.add(vpArc9yResponse_33b, retrieveCompletionDepositDelay);
    }
   }
}
