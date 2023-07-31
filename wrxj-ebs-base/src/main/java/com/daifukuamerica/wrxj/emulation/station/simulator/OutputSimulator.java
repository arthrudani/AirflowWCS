package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class OutputSimulator extends StationSimulator
{

  public OutputSimulator(StationData ipSD)
  {
    super(ipSD);
  }

  /**
   * Simulate the processing of a load arrival
   * There are three possibilities:
   * 1) Autopick - the load will be autopicked from the system
   * 2) No Autopick/Delete Inventory - check for item move records 
   *    and simulate pick screen if necessary - then delete it
   * 3) No Autopick/No Delete Inv - pick as above - however instead of
   *    being deleted this load will need to go to another station - 
   *    figure out where it needs to go and send it on it's way.
   */
  @Override
  public String simulate(String isLoad) throws DBException
  {

    logDebug("Load: " + isLoad + " has arrived at Output Station: " + msName);

    StandardInventoryServer vpLoadServ = Factory.create(StandardInventoryServer.class);
    
    // Check to see if we will need to simulate another station after this one
    String vsNext = SimUtilities.getNextStation(mpSD);
  
    // Case 1: Autopick
    if (mpSD.getAutoLoadMovementType() == DBConstants.AUTOPICK)
    {
      SimUtilities.autoPickLoad(isLoad, mpSD);
      
    }
    else
    {
      // Check for item picks
      SimUtilities.pickItemsFromLoad(isLoad, mpSD);
      
      // Case 2: DeleteInventory
      if (mpSD.getDeleteInventory() == DBConstants.YES)
      {
        vpLoadServ.deleteLoad(isLoad, "");
      }
      else // Case 3: send the load to the next station
      {
        if (vsNext == null)
        {
          throw new DBException("Unable to find next station from " + msName + 
                          " for " + isLoad);
        }
        else // delay slighty so that the next arrival doesn't happen too soon
        {
          try
          {
            Thread.sleep(1000);
          }
          catch(InterruptedException ex) {}
        }
      }
    }
    return vsNext;
  }
}
