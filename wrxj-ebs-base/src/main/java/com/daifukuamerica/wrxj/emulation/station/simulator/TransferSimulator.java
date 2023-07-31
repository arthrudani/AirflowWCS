package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class TransferSimulator extends StationSimulator
{
  public TransferSimulator(StationData ipSD)
  {
    super(ipSD);
  }

  @Override
  public String simulate(String isLoad) throws DBException
  {
    // wait for the load to get transferred and then send store arrival at next station.
    logDebug("Load: " + isLoad +  " has arrived at Transfer Station: " + msName);
    
    // Delay required interval
    try
    {
      Thread.sleep(mpSD.getSimInterval());
    }
    catch (InterruptedException ex) 
    {
      throw new DBException(ex);
    }
    
    // Send it to the next station if possible
    String vsStn = SimUtilities.getNextStation(mpSD);
    if (vsStn != null)
    {
      logDebug("Transfering load to next station: " + vsStn);
      
      // We need a store arrival from a different AGC, so we'll send it here
      // TODO: try to figure out how to do this on the other simulator
      StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
      StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
      StationData vpNext = vpStnServ.getStation(vsStn);
      String vsEmulator = vpDevServ.getEmulatorForDevice(vpNext.getDeviceID());
      if (vsEmulator == null)
        throw new DBException("At " + mpSD.getStationName() + ": No emulator for next station " + vsStn);
      
//      SimUtilities.storeLoad(isLoad, vsStn, vsEmulator);
//      LoadEventDataFormat vpLEDF = new LoadEventDataFormat("Simulation");
//      vpLEDF.setHeight(1);
//      String vsCmd = vpLEDF.processArrivalReport(AGCDeviceConstants.AGCDUMMYLOAD, vsStn,
//          vpLEDF.getHeight(), 1, isLoad, "");
//      ThreadSystemGateway.get().publishLoadEvent(vsCmd, 0, vpStnServ.getStationsScheduler(vsStn));
    }
    return null;
  }

}
