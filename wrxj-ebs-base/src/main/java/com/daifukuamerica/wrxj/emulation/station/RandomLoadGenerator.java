package com.daifukuamerica.wrxj.emulation.station;

/**
 * @author karmstrong
 * 
 * Auxiliary class used by input station simulator to create new loads
 * for storing.
 */
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomLoadGenerator
{
  StationData mpSD;
  StandardPoReceivingServer mpPOServ = Factory.create(StandardPoReceivingServer.class);
  int mnLoadLength;
  Random mpRand = new Random(System.currentTimeMillis());

  /**
   * 
   * @param ipSD <code>StationData</code> for the station where generated loads will be stored
   * @param ipList List of <code>ItemMasterData</code> objects representing all possible items
   *  that might be stored at this location.
   */
  public RandomLoadGenerator(StationData ipSD)
  {
    mpSD = ipSD;
    mnLoadLength = DBInfo.getFieldLength(LoadData.LOADID_NAME);
  }
  
  /**
   * Initialize a new load and add it to the database
   * 
   * @param isLoadId <code>String</code> representing load's ID
   * @return <code>LoadData</code> for the newly created load
   */
  private LoadData initializeLoadData(String isLoadId) throws DBException
  {
    LoadData vpLD = Factory.create(LoadData.class);
    vpLD.setLoadID(isLoadId);
    vpLD.setParentLoadID(isLoadId);
    vpLD.setAddress(mpSD.getStationName());
    vpLD.setWarehouse(mpSD.getWarehouse());
    vpLD.setContainerType(mpSD.getContainerType());
    vpLD.setHeight(SimUtilities.getWeightedRandomInt(0, 80, 15, 5));
    vpLD.setLoadMoveStatus(DBConstants.ARRIVEPENDING);
    Factory.create(StandardLoadServer.class).addLoad(vpLD);
    return vpLD;
  }
  
  /**
   * Return a unique loadid to be used in an arrival at an autostore empties station.
   * @return <code>String</code> representing the load to be created.
   */
  public String createEmptyLoad()
  {
    return generateLoadID(mpSD.getLoadPrefix());
  }
  
  /**
   * Return a unique loadid to be used in an arrival at an autostore with item station.
   * @return <code>String</code> representing the load to be created.
   */
  public String createItemLoad()
  {
    return generateLoadID(mpSD.getLoadPrefix());
  }
  
  /**
   * Simulate the use of the store screen.
   * 
   * A new load is created and one or more items is/are added to it.
   * @return
   */
  public String simStoreScreen(List<ItemMasterData> ipList) throws DBException
  {
    String vsLoadID = generateLoadID(mpSD.getLoadPrefix());
    LoadData vpLD = initializeLoadData(vsLoadID);
    
    // Add some random item details
    
    LoadLineItemData vpLLD = Factory.create(LoadLineItemData.class);
    //int vnLines = SimUtilities.getWeightedRandomInt(false, 75,20,5);
    int vnLines = 1;
    for(int i=0; i<vnLines; i++)
    {
      ItemMasterData vpIMD = getRandomItem(ipList);
      vpLLD.clear();
      vpLLD.setItem(vpIMD.getItem());
      vpLLD.setLoadID(vsLoadID);
      double vfQty = vpIMD.getDefaultLoadQuantity();
      if (vfQty == 0.0)
        vfQty = mpSD.getOrderQuantity();
      if (vfQty == 0.0)
        vfQty = 1.0;
      vpLLD.setCurrentQuantity(vfQty);
      vpLLD.setLot("simIT" + vsLoadID.substring(3) + i);
      try
      {
        Factory.create(StandardInventoryServer.class).addLoadLI(vpLLD);
      }
      catch (DBException ex)
      {
        System.err.println("Autostore: Error adding load line item: " + vpLLD.getItem() +
                          " to load: " + vpLLD.getLoadID());
      }
    }
    return vpLD.getLoadID();
  }
  
  /**
   * Create a new expected receipt and use that as the id of a load that will be stored.
   * When the station is simulated, the ER will be received into the load.
   * 
   * @return <code>String</code> representing the load that will be created.
   */
  public String createERLoad(List<ItemMasterData> ipList)
  {
    // create the ER to receive against
    PurchaseOrderHeaderData vpPOH = generateER(ipList);
    // the id of the load will be the id of the ER
    return vpPOH.getOrderID();
  }
  
  private String generateLoadID(String isPrefix)
  {
    return new StandardLoadServer().createRandomLoadID(isPrefix);
  }
  
  /**
   * Create a new PO/ER to be received into a new load
   * 
   * @return <code>PurchaseOrderHeaderData</code> representing the created ER.
   */
  private PurchaseOrderHeaderData generateER(List<ItemMasterData> ipList)
  {
    String vsID = mpPOServ.createRandomPurchaseOrderID();
    if (vsID.length() > mnLoadLength)
      vsID = vsID.substring(0, mnLoadLength);
    PurchaseOrderHeaderData vpPOH = Factory.create(PurchaseOrderHeaderData.class);
    vpPOH.setOrderID(vsID);
    
    try
    {
      List<PurchaseOrderLineData> vpList = new ArrayList<PurchaseOrderLineData>();
      
      // If the station does not have a dedicated item set, add some random item details
      if (mpSD.getItem() == null || mpSD.getItem().equals(""))
      {
//        int vnLines = SimUtilities.getWeightedRandomInt(1, 70, 20, 10);
        int vnLines =1;
        for(int i=0; i<vnLines; i++)
        {
          PurchaseOrderLineData vpPOL = Factory.create(PurchaseOrderLineData.class);
          vpPOL.setOrderID(vsID);
          vpPOL.setLineID(i+"");
          ItemMasterData vpIMD = getRandomItem(ipList);
          vpPOL.setItem(vpIMD.getItem());
          vpPOL.setLot("simlotER" + i + vpIMD.getItem().substring(0,1));
          double vfQty = vpIMD.getDefaultLoadQuantity();
          if (vfQty == 0.0)
            vfQty = mpSD.getOrderQuantity();
          if (vfQty == 0.0)
            vfQty = 1.0;
          vpPOL.setExpectedQuantity(vfQty);
          vpList.add(vpPOL);
        }
      }
      else // use the station's item
      {
        PurchaseOrderLineData vpPOL = Factory.create(PurchaseOrderLineData.class);
        vpPOL.setOrderID(vsID);
        vpPOL.setLineID(1+"");
        vpPOL.setItem(mpSD.getItem());
        vpPOL.setLot("simlotERDed" + mpSD.getItem().substring(0,1));
        double vfQty = mpSD.getOrderQuantity();
        if (vfQty == 0.0)
          vfQty = 1.0;
        vpPOL.setExpectedQuantity(vfQty);
        vpList.add(vpPOL);
      }
      
      // Create the ER
      mpPOServ.buildPO(vpPOH, vpList);
    }
    catch(DBException ex)
    {
      System.err.println("Error generating PO" + ex.getMessage());
    }
    return vpPOH;
  }
  
  /**
   * Randomly choose an item from the list of possible item masters.
   * 
   * @return <code>ItemMasterData</code> representing item chosen.
   */
  private ItemMasterData getRandomItem(List<ItemMasterData> ipList)
  {
    int vnIndex = mpRand.nextInt(ipList.size());
    return ipList.get(vnIndex);
  }

}
