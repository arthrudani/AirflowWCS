package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class GenericSimulator extends StationSimulator
{
  public GenericSimulator(StationData ipSD)
  {
    super(ipSD);
  }

  @Override
  public String simulate(String isLoad) throws DBException
  {
    logDebug("Load: " + isLoad +  " has arrived at Generic Station: " + msName);
    
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
      logDebug("Sending load to next station: " + vsStn);
      return vsStn;
    }
    return null;
  }

}
