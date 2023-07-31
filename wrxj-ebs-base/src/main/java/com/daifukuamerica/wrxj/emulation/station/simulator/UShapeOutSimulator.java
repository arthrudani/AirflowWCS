package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class UShapeOutSimulator extends StationSimulator
{

  public UShapeOutSimulator(StationData ipSD)
  {
    super(ipSD);
  }

  /**
   * Simulate the processing of a load arrival
   * There are two possibilities:
   * 1) Autopick - the load will be autopicked
   * 2) No Autopick/Captive - this load isn't going anywhere - check for
   *    item move records and simulate pick screen if necessary
   */
  @Override
  public String simulate(String isLoad) throws DBException
  {
    
    logDebug("Load: " + isLoad + " has arrived at U-shape Out Station: " + msName);
   
    // Case 1: autopicking
    if (mpSD.getAutoLoadMovementType() == DBConstants.AUTOPICK)
    {
      SimUtilities.autoPickLoad(isLoad, mpSD);
    }
    // Case 2: Simulate the pick screen and return the load
    else
    {
      SimUtilities.pickItemsFromLoad(isLoad, mpSD);
      
      // simulate work complete button push and release load
      SimUtilities.releaseLoad(isLoad, mpSD);
    }
    // done picking
    
    return null;
  }

}
