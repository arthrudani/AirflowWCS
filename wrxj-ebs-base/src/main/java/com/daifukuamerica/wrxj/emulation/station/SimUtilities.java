package com.daifukuamerica.wrxj.emulation.station;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author karmstrong
 *
 * A utility class for common routines performed by more than one simulator.
 * 
 * Since all methods are static, this class should not be instantiated.
 */

public class SimUtilities
{

  private SimUtilities()
  {
    throw new InstantiationError(); 
  }
  
  /**
   * Simulate a user operation of the pick screen, picking a load clean.
   * @param isLoad
   * @param ipSD
   * @throws DBException
   */
  public static void pickItemsFromLoad(String isLoad, StationData ipSD) throws DBException
  {
    MoveData vpMD = Factory.create(MoveData.class);
    OrderHeaderData vpOHD = Factory.create(OrderHeaderData.class);
    
    // delay required interval
    try
    {
      Thread.sleep(ipSD.getSimInterval());
    }
    catch(InterruptedException ex)
    {
      throw new DBException(ex);
    }

    StandardMoveServer vpMoveServ = Factory.create(StandardMoveServer.class);
    StandardOrderServer vpOrdServ = Factory.create(StandardOrderServer.class);
    vpMD = vpMoveServ.getNextMoveRecord(isLoad, DBConstants.ITEMMOVE);
    
    while(vpMD != null) 
    {
      vpOHD = vpOrdServ.getOrderHeaderRecord(vpMD.getOrderID());
      if (vpOHD.getDestAddress().equals(ipSD.getStationName()))
      {
        String vsItem = vpMD.getItem();
        double vfQty = vpMD.getPickQuantity();
        System.out.println("Picking " + vfQty + " of " + vsItem);
  
        StandardPickServer vpPickServ = Factory.create(StandardPickServer.class);
        vpPickServ.completeItemPick("Simulator", vpMD, null,
            ipSD.getDeleteInventory() == DBConstants.YES,
            vpMD.getPickQuantity(), "");
        
        // delay required interval
        try
        {
          Thread.sleep(ipSD.getSimInterval());
        }
        catch(InterruptedException ex)
        {
          throw new DBException(ex);
        }
      } 
      vpMD = vpMoveServ.getNextMoveRecord(isLoad, DBConstants.ITEMMOVE);
    }
  }
  
  /**
   * Autopick a load - it is believed that this method may soon become obsolete.
   * @param isLoad
   * @param ipSD
   * @throws DBException
   */
  public static void autoPickLoad(String isLoad, StationData ipSD) throws DBException
  {
    // delay a small interval in case another station needs to simulate after
    try
    {
      Thread.sleep(2000);
    }
    catch(InterruptedException ex)
    {
      throw new DBException(ex);
    }
  }
  
  /**
   * Simulate the release of a load from a store screen.
   * Autostore stations should not need to use this method.
   * @param isLoad 
   * @param ipSD
   * @throws DBException
   */
  public static void releaseLoad(String isLoad, StationData ipSD) throws DBException
  {
    StandardPickServer vpPickServ = Factory.create(StandardPickServer.class);
    if(vpPickServ.checkLoadForRelease(isLoad) == null)
      vpPickServ.releaseLoad(isLoad, ipSD);
  }
  
  /**
   * Based on the type of auto load movement of a station, create a load ID and
   * any secondary data (expected receipt, etc.) needed for a load to be stored.
   * storing at that station.  The One or more items from the item list may be
   * added as well, depending on the type of store to be performed.  The load will
   * not be created yet for autostore stations.
   * 
   * @param ipSD Station where load will be stored from.
   * @param ipItems List of possible items that can be stored in the load.
   * @return The load ID of the load to be stored.
   * @throws DBException
   */
  public static String createLoadToStore(StationData ipSD, List<ItemMasterData> ipItems) throws DBException
  {
    switch(ipSD.getAutoLoadMovementType())
    {
      case DBConstants.AUTORECEIVE_ER:
      case DBConstants.BOTH:
        return autoReceiveFromER(ipSD, ipItems);
      case DBConstants.AUTORECEIVE_ITEM:
        return autoReceiveStationItem(ipSD);
      case DBConstants.AUTORECEIVE_LOAD:
        return autoReceiveEmptyLoad(ipSD);
      default:
        return simulateStoreScreen(ipSD, ipItems);
    }
  }

  /**
   * Create a random Expected Receipt with random items on it to store against.
   * @param ipSD Station where load will be stored.
   * @param ipReceivableItems List of items that may be stored in the load
   * @return ER ID and load ID that will be stored.
   */
  private static String autoReceiveFromER(StationData ipSD, List<ItemMasterData> ipReceivableItems)
  {
    RandomLoadGenerator vpLoadGen = new RandomLoadGenerator(ipSD);
    System.out.println("Autostoring a load from E.R. ...");
    return vpLoadGen.createERLoad(ipReceivableItems);
  }
  
  /**
   * Create a random load id for storing at autostore with item station.
   */
  private static String autoReceiveStationItem(StationData ipSD)
  {
    RandomLoadGenerator vpLoadGen = new RandomLoadGenerator(ipSD);
    System.out.println("Autostoring a load with items ...");
    return vpLoadGen.createItemLoad();
  }

  /**
   * Create a random load id for storing at autostore empties station.
   */
  private static String autoReceiveEmptyLoad(StationData ipSD)
  {
    RandomLoadGenerator vpLoadGen = new RandomLoadGenerator(ipSD);
    System.out.println("Autostoring an empty load ...");
    return vpLoadGen.createEmptyLoad();
  }
  
  /**
   * Simulate user operation of the store screen.
   * @param ipSD Station to store at.
   * @param ipReceivableItems Items the user might store.
   * @return Load ID of created load.
   * @throws DBException
   */
  private static String simulateStoreScreen(StationData ipSD, List<ItemMasterData> ipReceivableItems) throws DBException
  {
    RandomLoadGenerator vpLoadGen = new RandomLoadGenerator(ipSD);
    System.out.println("Simulating Store Screen at station " + ipSD.getStationName() + "...");
    
    // delay required interval
    try
    {
      Thread.sleep(ipSD.getSimInterval());
    }
    catch(InterruptedException ex)
    {
      throw new DBException(ex);
    }
    return vpLoadGen.simStoreScreen(ipReceivableItems);
  }
  
  /**
   * Get all the items that can be stored through a station
   * 
   * @param ipSD station used for storing.
   * @return List of Item Master for items that can be stored.
   */
  public static List<ItemMasterData> getStorableItems(StationData ipSD)
  {
    StandardInventoryServer vpInvServ = Factory.create(StandardInventoryServer.class);
    try
    {
      List<ItemMasterData> vpList = new ArrayList<ItemMasterData>();
      vpList.addAll(vpInvServ.getItemMasterByRecWarehouse(ipSD.getWarehouse()));
      if (vpList.size() == 0)
        vpList.add(vpInvServ.getTemporaryItemData());
      return vpList;
    }
    catch(DBException ex)
    {
      System.err.println("Error obtaining storable items for station simulator: " + ipSD.getStationName());
      return new ArrayList<ItemMasterData>();
    }
  }
  
  /**
   * Get the communication port for a given station.
   * 
   * @param ipSD
   * @return String representation of the port.
   */
  public static String getStationsCommPort(StationData ipSD)
  {
    StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
    DeviceData vpDD =  vpDevServ.getDeviceData(ipSD.getDeviceID());
    return vpDevServ.getDeviceData(vpDD.getCommDevice()).getCommSendPort();
  }
  
  /**
   * Get the controlling device for a given station.
   * 
   * @param ipSD
   * @return String representation of the device.
   */
  public static String getStationsCommDevice(StationData ipSD)
  {
    StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
    return vpDevServ.getDeviceData(ipSD.getDeviceID()).getCommDevice();
  }
  
  /**
   * Method for storing a load from any kind of input station.  Causes an AGC store arrival to occur.
   * @param isLoad Load ID to be used as barcode in arrival.
   * @param isStn Station where arrival will occur.
   * @param isEmulator Name of the controller responsible for generating the arrival.
   */
  public static void storeLoad(String isLoad, String isStn, String isEmulator)
  {
    if (isLoad != null && !isLoad.equals(""))
    {
      ThreadSystemGateway.get().publishControlEvent(
          ControlEventDataFormat.getArrivalCommand(isStn, isLoad,
              AGCDeviceConstants.AGCDUMMYLOAD, null, 1, 1), 
          ControlEventDataFormat.TEXT_MESSAGE, isEmulator);
    }
  }
  
  /**
   * Find out what the next station on a route from a station is and
   * send move a load there.
   * 
   * @param isLoad
   * @param ipSD
   * @return String representation of next station, null if it could not be found
   */
  public static String getNextStation(StationData ipSD)
  {
    StandardRouteServer vpRouteServ = Factory.create(StandardRouteServer.class);
    StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
    String vsRoute = ipSD.getLinkRoute();
    try
    {
      String vsStn = vpRouteServ.getNextRouteDest(vsRoute, ipSD.getStationName());
      
      if (vsStn.length() > 0 && vpStnServ.exists(vsStn))
        return vsStn;
      else
        return null;
    }
    catch(DBException ex)
    {
      return null;
    }
  }
  
  /**
   * Generated a random integer based on received weights.
   * Each integer input represents a weight that it's corresponding
   * number will be chosen.
   * For example if 10,20,30 are passed in with a start of 1, there is a 10 in 60 
   * chance that 1 will be chosen, a 20 in 60 chance that 2 will be chosen and a 
   * 30 in 60 chance of 3 being chosen.
   * 
   * @param ianWeights List of weights corresponding to individual integers
   * @param inStart Determines the number to start with.
   * @return a number between inStart and inStart + however many integers are passed in
   */
  public static int getWeightedRandomInt(int inStart, int ... ianWeights)
  {
    if (ianWeights.length == 0)
      return 1;
    
    int vnTotal = ianWeights[0];
    for (int i=1; i<ianWeights.length; i++)
    {
      vnTotal += ianWeights[i];
      ianWeights[i] += ianWeights[i-1];
    }
    int vnChoice = new Random(System.currentTimeMillis()).nextInt(vnTotal);
    for (int i=0; i<ianWeights.length; i++)
    {
      if (vnChoice <= ianWeights[i])
        return i+inStart;
    }
    return 1;
  }
  
  /**
   * Get the emulation controller responsible for a station.
   * @param ipSD Station to check.
   * @return Name of the controller.
   */
  public static String getStationsEmulator(StationData ipSD)
  {
    StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
    return vpDevServ.getEmulatorForDevice(ipSD.getDeviceID());
  }
}
