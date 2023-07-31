package com.daifukuamerica.wrxj.emulation.station.simulator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.emulation.station.SimUtilities;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;

public class InputSimulator extends StationSimulator
{
  List<ItemMasterData> mpReceivableItems;
  String msEmulator;

  public InputSimulator(StationData ipSD)
  {
    super(ipSD);
    msEmulator = SimUtilities.getStationsEmulator(ipSD);
    
    // create a list of receivable items for random storing
    mpReceivableItems = SimUtilities.getStorableItems(mpSD);
  }

  /**
   * Simulate the appearance of a load at a store station.  
   * Causes an AGC store arrival to take place.
   */
  @Override
  public String simulate(String isLoad) throws DBException
  {
    StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);
    String vsLoad;
    if (!vpLoadServ.loadExists(isLoad))
    {
      vsLoad = createAutoStoreLoad();
      if (vsLoad == null || vsLoad.equals(""))
        return null;
    }
    else
    {
      vsLoad = isLoad;
    }
    
    logDebug("Load: " + vsLoad + " has arrived at Input Station: " + msName);
    
    // simulate load store
    SimUtilities.storeLoad(vsLoad, msName, msEmulator);
    
    return null;
  }
  
  @Override
  public boolean isStoringStation()
  {
    return(mpSD.getAutoLoadMovementType() == DBConstants.AUTORECEIVE_ER   ||
           mpSD.getAutoLoadMovementType() == DBConstants.AUTORECEIVE_ITEM ||
           mpSD.getAutoLoadMovementType() == DBConstants.AUTORECEIVE_LOAD ||
           mpSD.getAutoLoadMovementType() == DBConstants.BOTH);
  }
  
  protected String createAutoStoreLoad() throws DBException
  {
    return SimUtilities.createLoadToStore(mpSD, mpReceivableItems);
  }

}
