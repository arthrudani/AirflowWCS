package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class UShapeInSimulator extends StationSimulator
{
  String msEmulator;

  public UShapeInSimulator(StationData ipSD)
  {
    super(ipSD);
    
    msEmulator = SimUtilities.getStationsEmulator(ipSD);
  }

  /**
   * Sends a command to the AGC scheduler to store this load
   * 
   * @param isLoad Load to be stored.
   */
  @Override
  public String simulate(String isLoad) throws DBException
  {

    logDebug("Load: " + isLoad + " has arrived at U-Shape In Station: " + msName);

    // delay the required interval
    try
    {
      Thread.sleep(mpSD.getSimInterval());
    }
    catch(InterruptedException ex)
    {
      throw new DBException(ex);
    }
    // simulate load store
    SimUtilities.storeLoad(isLoad, msName, msEmulator);
    
    return null;
  }

}
