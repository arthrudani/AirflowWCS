package com.daifukuamerica.wrxj.emulation.station;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.agc.AGCMCMessage;
import com.daifukuamerica.wrxj.emulation.station.simulator.AGCTransferSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.GenericSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.InputSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.OutputSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.PDStandSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.StationSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.TransferSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.UShapeInSimulator;
import com.daifukuamerica.wrxj.emulation.station.simulator.UShapeOutSimulator;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.timer.RestartableTimer;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Special testing-only controller for simulating stations.  
 * <p>This controller simulates human interaction as well as machine behavior.  
 * Theoretically you should be able to simulate the normal operation of an entire system
 * without any need for user interaction.  The chief purpose of this controller is for throughput testing.
 * It may also be used to facilitate basic testing or to perform stress testing.  It may also prove useful
 * in on site testing by combining live equipment with simulated equipment to test the entire system, but that
 * remains to be seen.</p>
 * <p> To enable this controller you must have the application property "SimulationEnabled=YES" defined.</p> 
 * @author karmstrong
 * @since March 2006
 * @see StationSimulator, {@link SimUtilities}
 *
 */
public class SimulationController extends Controller
{
  Map<String, StationSimulator> mpSimStations = Collections.synchronizedMap(new HashMap<String, StationSimulator>());
  DeviceData mpDD;
  Map<String, StoreTask> mpStoreTasks = Collections.synchronizedMap(new HashMap<String, StoreTask>());
  RestartableTimer mpTimer;

  private SimulationController(String isControllerName)
  {
    StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
    mpDD = vpDevServ.getDeviceData(isControllerName);
  }
  
  /**
   * Turn on simulation for a station by creating a new <code>StationSimulator</code>.
   * 
   * @param ipSD <code>StationData</code> object containing info on station
   * to be simulated.  The station type field is used to determine which kind
   * of simulator to add.
   */
  private void startupSimulator(StationData ipSD)
  {
    String vsName = ipSD.getStationName();
    int vnType = ipSD.getStationType();
    StationSimulator vpSS = null;
    
    switch(vnType)
    {
      case DBConstants.USHAPE_IN : 
        vpSS = Factory.create(UShapeInSimulator.class, ipSD);     break;
      case DBConstants.USHAPE_OUT :
        vpSS = Factory.create(UShapeOutSimulator.class, ipSD);    break;
      case DBConstants.INPUT :
        vpSS = Factory.create(InputSimulator.class, ipSD);        break;
      case DBConstants.OUTPUT :        
        vpSS = Factory.create(OutputSimulator.class, ipSD);       break;
      case DBConstants.PDSTAND :
        vpSS = Factory.create(PDStandSimulator.class, ipSD);      break;
      case DBConstants.REVERSIBLE :
        vpSS = Factory.create(PDStandSimulator.class, ipSD);      break;
      case DBConstants.TRANSFER_STATION :
        vpSS = Factory.create(TransferSimulator.class, ipSD);     break;
      case DBConstants.AGC_TRANSFER :
        vpSS = Factory.create(AGCTransferSimulator.class, ipSD);  break;
      // ADD NEW STATION TYPES HERE
      default:
        vpSS = Factory.create(GenericSimulator.class, ipSD);      break;
    }
    mpSimStations.put(vsName, vpSS);
    // create a store timer if necessary
    if(vpSS.isStoringStation())
    {
      createStoreTask(ipSD);
    }
  }
  
  /**
   * Called when the controller first starts up, this method searches to find
   * all stations with the simulation flag turned on and starts a <code>
   * StationSimulator</code> controller for each one.
   */
  private void initializeSimulators()
  {   
    try
    {
      StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
      List<StationData> vpSimStations = vpStnServ.getSimStationsForDevice(mpDD.getDeviceID());
      for(StationData vpSD : vpSimStations)
      {
        startupSimulator(vpSD); 
      }
    }
    catch(DBException ex)
    {
      System.err.println("Error obtaining simulated station data.");
    }
  }
  
  /**
   * Update the data associated with a station's simulator.
   * 
   * @param ipSD <code>StationData</code> to associate with the simulator.
   * @throws DBException
   */
  public void updateSimulatorStation(StationData ipSD)
  {
    boolean vzOn = (ipSD.getSimulate() == DBConstants.ON);
    
    StationSimulator vpSim = mpSimStations.get(ipSD.getStationName());
    
    // startup if necessary
    if (vpSim == null)
    {
      if (vzOn)
        startupSimulator(ipSD);
    }
    else
    {
      if (!vzOn) // shutdown simulator
      {
        cancelStoreTask(ipSD.getStationName());
        mpSimStations.remove(ipSD.getStationName());
      }
      else       // just changing parameters
      {
        boolean vzOrig = vpSim.isStoringStation();
        boolean vzIntervalChanged = vpSim.intervalChanged(ipSD.getSimInterval());
        vpSim.setStationData(ipSD);
        boolean vzNew = vpSim.isStoringStation();
        // make sure it hasn't become (or ceased to be) a storing station
        if (vzOrig != vzNew)
        {
          if (vzNew == true) // turning on
            createStoreTask(ipSD);
          else
            cancelStoreTask(ipSD.getStationName());
        }
        // See if the store interval has changed
        else if (vzNew == true && vzIntervalChanged)
        {
          cancelStoreTask(ipSD.getStationName());
          createStoreTask(ipSD);
        }
      }
    }
  }
  
  /**
   * This is the method that is called any time an Inter-Process Message is received.
   * 
   * The method parses the message and forwards it to an appropriate method to process that message.
   */
  @Override
  protected void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      final int vnReceivedEventType = receivedEventType;
      final String vsReceivedText = receivedText;
      new Thread()
      {
        @Override
        public void run()
        {
          switch (vnReceivedEventType)
          {
            case MessageEventConsts.LOAD_EVENT_TYPE:
              logger.logDebug("StationDevice.processIPCReceivedMessage() -- LoadEvent \"" + receivedText + "\"");
              processLoadEvent(vsReceivedText);
              break;
            case MessageEventConsts.EQUIPMENT_EVENT_TYPE:
              processEquipmentEvent(vsReceivedText);
              break;
            case MessageEventConsts.STATION_EVENT_TYPE:
              logger.logDebug("StationDevice.processIPCReceivedMessage() -- StationEvent");
              processStationEvent(vsReceivedText);
              break;
            default:
              logger.logError("Unrecognized message type " + vnReceivedEventType + "in SimulationController.");
          }
        }
      }.start();
    }
  }
  
  /**
   * This handles simulator update messages.
   * 
   * A station event should be received when the station record associated 
   * with a simulator is modified from the simulation screen. 
   * @param isReceivedText <code>String</code> containing station name
   * of the station that has been modified.
   * @return
   */
  private void processStationEvent(String isReceivedText)
  {
    StationData vpSD = Factory.create(StandardStationServer.class).getStation(isReceivedText);
    if (vpSD != null)
      updateSimulatorStation(vpSD);
  }
  
  /**
   * Generally called for retrieve arrivals
   * @param isReceivedText
   */
  private void processEquipmentEvent(String isReceivedText)
  {
    AGCMCMessage vpMsg = new AGCMCMessage();
    vpMsg.toDataValues(isReceivedText);
    if (vpMsg.getID() == 26)
    {
      String vsStn = vpMsg.getArrivalStationNumber();
      if (vsStn != null)
      {
        String vsLoad = vpMsg.getMCKey();
        if(!vsLoad.equals(AGCDeviceConstants.AGCDUMMYLOAD))
          simulate(vsStn, vsLoad);
      }
    }
  }
  
  /**
   * Generally called for store arrivals or PD stand retrievals that
   * don't get an arrival message.
   * @param isReceivedText
   */
  private void processLoadEvent(String isReceivedText)
  {
    LoadEventDataFormat vpLEDF = Factory.create(LoadEventDataFormat.class,
        "Simulation");
    vpLEDF.clearAllData();
    vpLEDF.decodeReceivedString(isReceivedText);
    // This is a store load arrival
    if (vpLEDF.getMessageID() == 999)
    {
      String vsStn = vpLEDF.getSourceStation();
      if(vsStn != null)
      {
        String vsLoad = vpLEDF.getLoadID();
        
        simulate(vsStn, vsLoad);
      }
    }
    // This is a retrieval for a P&D that has arrival required set to no.
    else if (vpLEDF.getMessageID() == AGCDeviceConstants.AGCDEVICEOPERATIONCOMPLETION)
    {
      StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
      String vsStn = vpLEDF.getDestinationStation();
      if (vsStn != null && vpStnServ.exists(vsStn) && !vpStnServ.doesStationGetRetrieveArrival(vsStn))
      {
        String vsLoad = vpLEDF.getLoadID();
        
        simulate(vsStn, vsLoad);
      }
    }
  }
  
  /**
   * Method for conformity to the {@link ControllerImplFactory} design.
   * @param ipConfig
   * @return
   * @throws ControllerCreationException
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    String vsDeviceId = ipConfig.getString(DEVICE_ID);
    if (vsDeviceId == null)
      throw new ControllerCreationException(
          "unable to create SimulationController: DeviceID undefined");
    Controller vpController = Factory.create(SimulationController.class,
        vsDeviceId);
    vpController.setEquipmentPortCKN(ipConfig.getString(DEVICE_PORT));
    return vpController;
  }
  
  /**
   * Initialization method for conformity to the {@link ControllerImplFactory} design.
   */
  @Override
  public void initialize(String isName)
  {
    super.initialize(isName);
    subscribeEquipmentEvent();
    subscribeLoadEvent(mpDD.getSchedulerName());
    subscribeLoadEvent(mpDD.getDeviceID());
//    subscribeStationEvent(mpDD.getDeviceID()+"-"+SIMULATOR);
    subscribeStationEvent(isName);
    mpTimer = new RestartableTimer(mpDD.getDeviceID() + "-SimStoreTimer");
    initializeSimulators();    
  }

  @Override
  public void startup()
  {
    super.startup();
    setControllerStatus(ControllerConsts.STATUS_RUNNING);
    // TODO: use real logging instead of this piece of garbage.
    KipsSKDCSimulationLoggerErrorLoggingImpl.instantiateThisClass();
  }
  
  @Override
  public void shutdown()
  {
    KipsSKDCSimulationLoggerErrorLoggingImpl.closeEverything();
    mpTimer.cancel();
    mpStoreTasks.clear();
    mpSimStations.clear();
    super.shutdown();
  }
  
  /**
   * Simulate a load's journey to one or more stations.
   * If the simulation of one station requests the load be moved and simulated
   * at another station more than one simulation may occur.
   * @param isStn
   * @param isLoad
   */
  private void simulate(String isStn, String isLoad)
  {
    StationSimulator vpSS = mpSimStations.get(isStn);
    while(vpSS != null)
    {
      String vsStn = null;
      try
      {
        vsStn = vpSS.simulate(isLoad);
        // See if we need to simulate another station as well.
        if (vsStn != null)
        {
          StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);
          if (vpLoadServ.loadExists(isLoad) && !vpLoadServ.getLoadAddress(isLoad).equals(vsStn))
            vpLoadServ.moveLoadToStation(isLoad, vsStn, DBConstants.ARRIVED, 1);
          vpSS = mpSimStations.get(vsStn);
        }
        else
          vpSS = null;
      }
      catch (Exception ex)
      {
        // TODO: use real logging instead of this piece of garbage
        KipsSKDCSimulationLoggerErrorLoggingImpl.println("Unexpected error simulating station " + isStn + ". Check error logs for details.");
        KipsSKDCSimulationLoggerErrorLoggingImpl.println(ex.getMessage());
        KipsSKDCSimulationLoggerErrorLoggingImpl.println(ex);
        logger.logException(ex, "Simulating station " + isStn);
        vpSS = null;
      }
    }
  }
  
  /**
   * Creates a periodic timer task to create a load for storing at autostore stations.
   * @param ipSD
   */
  private void createStoreTask(StationData ipSD)
  {
    String vsStation = ipSD.getStationName();
    StoreTask vpTask = new StoreTask(vsStation);
    mpStoreTasks.put(vsStation, vpTask);
    // create a random delay interval so they all don't start up at once
    // also make sure the system has time to start up before they start going off
    int vnRandDelay = new Random().nextInt(20000) + 60000;
    mpTimer.setPeriodicTimerEvent(vpTask, ipSD.getSimInterval(), vnRandDelay);
  }
  
  /**
   * Cancels periodic store timer for autostore stations
   * @param isStation
   */
  private void cancelStoreTask(String isStation)
  {
    StoreTask vpTask = mpStoreTasks.remove(isStation);
    if (vpTask != null)
      mpTimer.cancel(vpTask);
  }
  
  /**
   * 
   * @author karmstrong
   * Auxiliary class for periodically creating a new load to store
   * 
   * The type of load stored depends on the AutoLoadMovementType flag.
   */
  private class StoreTask extends RestartableTimerTask
  {
    String msStation;
    public StoreTask(String isStation)
    {
      msStation = isStation;
    }
    public void run()
    {
      simulate(msStation,"");
    }
  }
}
