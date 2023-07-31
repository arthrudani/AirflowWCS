package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardSchedulerServer;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Looks for loads that are moving and part of a route that goes through
 * this station.  A store arrival is created and those loads are sent on their
 * merry way to their final destination.
 * @author karmstrong
 *
 */
public class AGCTransferSimulator extends StationSimulator
{
  private String msEmulator;
  
  public AGCTransferSimulator(StationData ipSD)
  {
    super(ipSD);
    msEmulator = SimUtilities.getStationsEmulator(ipSD);
  }

  @Override
  public String simulate(String isLoad) throws DBException
  {
    StandardSchedulerServer vpSchedServer = Factory.create(StandardSchedulerServer.class);

    // find the oldest moving load for this station
    String[] vasLoads = vpSchedServer.getLoadsForAGCTransferStation(msName);
    if (vasLoads.length == 0)
      return null;
    String vsLoad = vasLoads[0];
    logDebug("Load: " + vsLoad + " has arrived at Input Station: " + msName);
    
    // simulate load store
    SimUtilities.storeLoad(vsLoad, msName, msEmulator);
    
    return null;
  }

  @Override
  public boolean isStoringStation()
  {
    return true;
  }
}
