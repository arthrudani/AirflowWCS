package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;

/**
 * Simulator for P&D stand station types.  Simulates retrieving/picking as well as storing.
 * @author karmstrong
 *
 */
public class PDStandSimulator extends StationSimulator
{
  protected List<ItemMasterData> mpReceivableItems;
  boolean mzArrivePending = false;
  String msEmulator;

  public PDStandSimulator(StationData ipSD)
  {
    super(ipSD);
    msEmulator = SimUtilities.getStationsEmulator(ipSD);
    
    // create a list of receivable items for random storing
    mpReceivableItems = SimUtilities.getStorableItems(mpSD);
  }

  /**
   * If the store mode is Store/Retrieve, the station acts like a
   * U-shape out / U-shape in combo
   * 
   * If the store mode is Captive Insert, the station acts like an
   * input station 
   */
  @Override
  public String simulate(String isLoad) throws DBException
  {
    StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);
    String vsLoad;
    if (!vpLoadServ.loadExists(isLoad) && isStoringStation())
    {
      // Must be a request from a timer to create a new load to store
      vsLoad = SimUtilities.createLoadToStore(mpSD, mpReceivableItems);
    }
    else
    {
      vsLoad = isLoad;
    }
    
    if (vpLoadServ.getLoadMoveStatus(isLoad) == DBConstants.ARRIVEPENDING)
    {
      SimUtilities.storeLoad(vsLoad, msName, msEmulator);
      return null;
    }
    
    logDebug("Load: " + vsLoad + " has arrived at P&D Station: " + msName);
   
    int vnMode = mpSD.getBidirectionalStatus();
    
    if (vnMode == DBConstants.RETRIEVEMODE)
    {
      // Either autopick the load or simulate the pick screen
      if (mpSD.getAutoLoadMovementType() == DBConstants.AUTOPICK)
      {
        SimUtilities.autoPickLoad(vsLoad, mpSD);
      }
      else 
      { 
        //      Simulate pick screen
        return simPickScreen(vsLoad, mpSD);
        
      }
    }
    
    else if (vnMode == DBConstants.STOREMODE)
    {
      // simulate load store
      SimUtilities.storeLoad(vsLoad, msName, msEmulator);
    }
    
    return null;
  }
  
  /**
   * Return true if the P&D station is in store mode.
   */

  @Override
  public boolean isStoringStation()
  {
    return (mpSD.getBidirectionalStatus() == DBConstants.STOREMODE);
  }
  
  protected String simPickScreen(String isLoad, StationData ipSD) throws DBException
  {
    SimUtilities.pickItemsFromLoad(isLoad, ipSD);

    if (mpSD.getCaptive() == DBConstants.CAPTIVE || mpSD.getCaptive() == DBConstants.SEMICAPTIVE)
    {  // Load needs to go back
      if (mpSD.getAutoLoadMovementType() == DBConstants.AUTO_MOVE_OFF)
      {        
        // simulate work complete button push and release load
        // This actually gets done in two stages:
        //  The first time through the load is released.
        //  The second time it will get stored.
        SimUtilities.releaseLoad(isLoad, mpSD);
      }
      else
      {
        try 
        {
          Thread.sleep(mpSD.getSimInterval());
        }
        catch (InterruptedException ex) 
        {
          throw new DBException(ex);
        }
        SimUtilities.storeLoad(isLoad, msName, msEmulator);
      }
    }
    else // try to send load to another station
    {
      String vsStn = SimUtilities.getNextStation(mpSD);
      if (vsStn != null)
      {
        return vsStn;
      }
      else
      {
        throw new DBException("Unable to find next station from " + msName + 
                        " for " + isLoad);
      }
    }
    return null;
  }
}
