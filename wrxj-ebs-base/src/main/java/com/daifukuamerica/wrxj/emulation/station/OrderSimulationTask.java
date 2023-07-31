package com.daifukuamerica.wrxj.emulation.station;

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderSimulationTask extends RestartableTimerTask
{
  int mnInterval;
  boolean mzFull, mzItemLot, mzMult;
  String msRoute;
  
  public OrderSimulationTask(String isRoute, int inInterval, boolean izFull, boolean izItemLot, boolean izMult)
  {
    msRoute = isRoute;
    updateValues(inInterval, izFull, izItemLot, izMult);
  }
  
  public void run()
  {
    String vsOrder = null;
    try
    {
      vsOrder = createOrder();
    }
    catch(DBException ex)
    {
      // TODO: do something better 
      KipsSKDCSimulationLoggerErrorLoggingImpl.println("Order simulation - error generating order for route: " + msRoute);
      KipsSKDCSimulationLoggerErrorLoggingImpl.println(ex.getMessage());
    }
    
    if (vsOrder == null)
      System.out.println("Nothing to order for station: " + msRoute);
    else
      System.out.println("Order: " + vsOrder + " for station: " + msRoute);
  }
  
  public int getInterval()
  {
    return mnInterval;
  }
  
  public void updateValues(int inInterval, boolean izFull, boolean izItemLot, boolean izMult)
  {
    mnInterval = inInterval;
    mzFull = izFull;
    mzItemLot = izItemLot;
    mzMult = izMult;
  }
  
  /**
   * This is the chief method of the order simulator - it will be called on the
   * given interval to create a new order for a station.  The order is created
   * and set to ready for immediate allocation.  What the order will be for depends
   * on the <code>mzItemLot</code>, <code>mzFull</code>, and <code>mzMult</code>
   * flags.  There are five possible cases:
   *  1) mzItemLot = true - The item, lot, and order quantity fields from the station
   *      record will be used to create a one line order.
   *  2) mzFull, mzMult = true - The full quantity of all items from one or more
   *  loads will be ordered out.
   *  3) mzFull = true - The full quantity of an item on a load with only one item
   *      detail will be ordered out.
   *  4) mzMult = true - A partial quantity of one or more items from one or more
   *      loads will be ordered out.
   *  5) all false - A partial quantity of a single item on a load with only one
   *      item detail will be ordered out.
   * @param isStation Station to order load to.
   * @return String representation of the order's id.
   */
  private String createOrder() throws DBException
  {
    StandardOrderServer vpOrdServ = Factory.create(StandardOrderServer.class);
    StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
    String vsOrderID = vpOrdServ.createRandomOrderID("SIM","");
    
    if (mzItemLot) // Case 1
    {
      // use item/lot combination from station table
      StationData vpSD = vpStnServ.getStation(msRoute);
      if (vpSD == null)
        throw new DBException(msRoute + "is not a station - cannot use station item/lot combination.");
      vpOrdServ.createItemOrder(vsOrderID, msRoute, "Station Sim Order - "
                                + vsOrderID, DBConstants.READY, vpSD.getItem(),
                                vpSD.getLot(), vpSD.getOrderQuantity());
    }
    else
    {
      // order random items
      List<LoadLineItemData> vpItemsForOrder = new ArrayList<LoadLineItemData>();
      
      List<LoadLineItemData> vpList = mzMult ? 
          vpOrdServ.getOrderableItemDetails(msRoute, true) : // Cases 3 & 5
          vpOrdServ.getSingleOrderableItemDetails(msRoute, true); // Cases 2 & 4           
      
      int vnSize = vpList.size();
      if (vnSize == 0)
      {
        // try again without requiring routes to be set in item masters
        vpList = mzMult ? 
            vpOrdServ.getOrderableItemDetails(msRoute, false) : // Cases 3 & 5
            vpOrdServ.getSingleOrderableItemDetails(msRoute, false); // Cases 2 & 4 
      }
      
      vnSize = vpList.size();
      if (vnSize == 0) // There just isn't anything to order
        return null;
      
      int vnAmount = mzMult ? getOrderSize() : 1;
      
      if (vnAmount > vnSize)
        vnAmount = vnSize;
      
      int vnCount = 0;
      while (vnCount < vnAmount && !vpList.isEmpty())
      {
        int vnRand = new Random().nextInt(vpList.size());
        LoadLineItemData vpLLD = vpList.remove(vnRand);
        
        // Cases 4 & 5 - only partial quantity
        if (!mzFull)
        {
          vpLLD.setCurrentQuantity(new Random().nextInt((int)vpLLD.getCurrentQuantity())+1);
        }
        
        // merge any LLIs for the identical item and lot
        boolean vzNeedToAdd = true;
        for (LoadLineItemData tpLLD : vpItemsForOrder)
        {
          if (tpLLD.getItem().equals(vpLLD.getItem()))
          {
            vnCount++;
            tpLLD.setCurrentQuantity(tpLLD.getCurrentQuantity() +
                                     vpLLD.getCurrentQuantity());
            vzNeedToAdd = false;
          }
        }
        if (vzNeedToAdd && okToAddItem(vpItemsForOrder, vpLLD))
        {
          vpItemsForOrder.add(vpLLD);
          vnCount++;
        }
      }
      
      OrderHeaderData vpOHD = Factory.create(OrderHeaderData.class);
      vpOHD.setOrderID(vsOrderID);
      vpOHD.setDestinationStation(msRoute);
      vpOHD.setDescription("Sim Order - " + vsOrderID);
      vpOHD.setPriority(5);
      vpOHD.setOrderType(DBConstants.ITEMORDER);
      vpOHD.setOrderStatus(DBConstants.READY);
      
      OrderLineData[] vapLines = new OrderLineData[vpItemsForOrder.size()];
      int i = 0;
      for (LoadLineItemData vpItem : vpItemsForOrder)
      {
        OrderLineData vpOLD = Factory.create(OrderLineData.class);
        vpOLD.setOrderID(vsOrderID);
        vpOLD.setItem(vpItem.getItem());
        vpOLD.setOrderQuantity(vpItem.getCurrentQuantity());
        vapLines[i] = vpOLD;
        i++;
      }

      // now create the order
      buildOrderData(vpOHD, vapLines);      
    }

    return vsOrderID;
  }
  
  protected void buildOrderData(OrderHeaderData ipOHD, OrderLineData[] iapOLines) throws DBException
  {
    StandardOrderServer vpOrdServ = Factory.create(StandardOrderServer.class);
    vpOrdServ.buildOrder(ipOHD, iapOLines);
  }
  
  /**
   * For use by overriding classes.
   * @param ipItems
   * @param ipItem
   * @return
   */
  protected boolean okToAddItem(List<LoadLineItemData> ipItems, LoadLineItemData ipItem)
  {
    return true;
  }
  
  /**
   * Determine how many lines to generate for an order.
   * @see SimUtilities.getWeightedRandomInt
   * @return
   */
  protected int getOrderSize()
  {
    return SimUtilities.getWeightedRandomInt(1, new int[] {50,30,20});
  }
}
