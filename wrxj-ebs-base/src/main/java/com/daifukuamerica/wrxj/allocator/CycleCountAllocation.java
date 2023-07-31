package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:<p>
 *  Class to find loads for a Cycle-Count request.  This class will build
 *  moves to bring out a load.</p>
 *
 * @author   A.D.
 * @version  1.0
 * @since:   20-Feb-03
 */
public class CycleCountAllocation extends AbstractAllocationStrategy implements AllocationStrategy
{
  protected String   msRouteID;
  protected List<AllocationMessageDataFormat> mpAllocatedData;
  private   TableJoin                mpTableJoin;
  protected StandardInventoryServer  mpInvServer;
  protected   StandardOrderServer      mpOrderServer;
  protected   StandardDeviceServer     mpDeviceServer;
  protected   StandardLoadServer       mpLoadServer;
  protected   StandardRouteServer      mpRouteServer;
  protected   LoadLineItemData         mpIDData;
  protected   Move                     mpMove;
  protected   MoveData                 mpMVData;
  private   DBObject                 mpDBObj;

  public CycleCountAllocation()
  {
    super();
    mpAllocServer  = Factory.create(StandardAllocationServer.class);
    mpInvServer    = Factory.create(StandardInventoryServer.class);
    mpOrderServer  = Factory.create(StandardOrderServer.class);
    mpDeviceServer = Factory.create(StandardDeviceServer.class);
    mpLoadServer   = Factory.create(StandardLoadServer.class);
    mpRouteServer  = Factory.create(StandardRouteServer.class);
    mpTableJoin    = Factory.create(TableJoin.class);
    mpIDData       = Factory.create(LoadLineItemData.class);
    mpMove         = Factory.create(Move.class);
    mpMVData       = Factory.create(MoveData.class);    
    mpDBObj        = new DBObjectTL().getDBObject();
    mpAllocatedData = new ArrayList<AllocationMessageDataFormat>();
  }
  
 /**
  * Method to find all loads pertaining to the Cycle Count request.
  *
  * @return <code>List</code> of AllocatedData class.
  * @throws DBException if there is an allocation problem.
  */
  public List<AllocationMessageDataFormat> allocate()
         throws DBException, AllocationException
  {
    mpAllocatedData.clear();           // Reserve order for allocation.
    mpAllocServer.reserveOrder(mpOHD.getOrderID());

    TransactionToken vpTranTok = null;
    try
    {
      vpTranTok = mpDBObj.startTransaction();
      if (mpOrderServer.orderHeaderExists(msOrder) &&
          mpOrderServer.getOrderStatusValue(msOrder) == DBConstants.ALLOCATING)
      {
        processOrderLines();
                                       // processOrderLines may put order on HOLD
                                       // no loads are found.
        if (mpOrderServer.getOrderStatusValue(msOrder) != DBConstants.HOLD)
          mpOrderServer.setOrderStatusValue(mpOHD.getOrderID(), DBConstants.SCHEDULED);
      }
      mpDBObj.commitTransaction(vpTranTok);
    }
    catch(DBException exc)
    {
      mpDBObj.endTransaction(vpTranTok);
      
      mpAllocServer.revertOrderStatus(mpOHD.getOrderID(), "", iOriginalOrderStatus);
      throw exc;
    }
    catch(AllocationException mesg)
    {
      mpDBObj.endTransaction(vpTranTok);// Handle next order status by caller so
                                       // that we can effectively determine if 
                                       // it's a short order or some other prob.
      String vsOrdMsg = "Application Error in allocator. Exception of type " + 
                         mesg.getClass().getCanonicalName() + " caught!";
      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw mesg;
    }
    catch(Exception exc)  
    {
      mpDBObj.endTransaction(vpTranTok);
      
      String vsOrdMsg = "Application Error in allocator. Exception of type " + 
                         exc.getClass().getCanonicalName() + " caught!";
      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw new DBException("Order " + msOrder + " Not Allocated...", exc);
    }
    finally
    {
      mpDBObj.endTransaction(vpTranTok);
    }

    return(mpAllocatedData);
  }

 /**
  *  Method to carry out the allocation.
  * @throws DBException if there is a DB error.
  * @throws AllocationException if any station device is inoperable.
  */
  protected void processOrderLines() throws DBException, AllocationException
  {
    List<Map> vpIDList = null;
    List<Map> vpLDList = null;
    OrderLineData olKey = Factory.create(OrderLineData.class);
    olKey.setKey(OrderHeaderData.ORDERID_NAME, mpOHD.getOrderID());
    OrderLineData vpOLData = mpOrderServer.getOrderLineRecord(olKey);

    switch(OrderLine.getCycleCountOrderCategory(vpOLData))
    {
      case OrderLineData.CC_ITEM:
        mpIDData.clear();
        mpIDData.setKey(LoadLineItemData.ITEM_NAME, vpOLData.getItem());
        vpIDList = mpInvServer.getLoadLineItemDataList(mpIDData);
        break;

      case OrderLineData.CC_ITEM_LOT:
        mpIDData.clear();
        mpIDData.setKey(LoadLineItemData.ITEM_NAME, vpOLData.getItem());
        mpIDData.setKey(LoadLineItemData.LOT_NAME, vpOLData.getOrderLot());
        vpIDList = mpInvServer.getLoadLineItemDataList(mpIDData);
        break;
        
      case OrderLineData.CC_ITEM_WHS:
      case OrderLineData.CC_ITEM_LOT_WHS:
        vpIDList = mpTableJoin.getItemDetailsInWarehouse(vpOLData.getItem(), 
                             vpOLData.getOrderLot(), vpOLData.getWarehouse());
        break;

      case OrderLineData.CC_LOCATION:
        vpLDList = mpTableJoin.getLoadsInLocationRange(vpOLData.getBeginWarehouse(), 
                       vpOLData.getBeginAddress(), vpOLData.getEndingAddress());
        break;

      default:
        throw new DBException("Unknown Cycle-Count Allocation data...");
    }

    if ((vpIDList == null || vpIDList.isEmpty()) && 
        (vpLDList == null || vpLDList.isEmpty()))
    {                                // Set Cycle-Count orders to Hold for
                                     // no product found case.
      mpOrderServer.setOrderStatusValue(mpOHD.getOrderID(), DBConstants.HOLD);
      mpOHD.setOrderStatus(DBConstants.HOLD);
      mpLogger.logOperation("Cycle-Count Order " + mpOHD.getOrderID() +
                            "not allocated. No item details found!");
      return;
    }

    if (OrderLine.getCycleCountOrderCategory(vpOLData) == OrderLineData.CC_LOCATION)
    {
      buildLoadCycleCounts(vpOLData, vpLDList);
    }
    else
    {
      buildItemDetailCycleCounts(vpOLData, vpIDList);
    }
  }

  protected void buildLoadCycleCounts(OrderLineData ipOLData, List<Map> ipLDList)
            throws AllocationException
  {
    LoadData vpLDData = Factory.create(LoadData.class);
    
    for(Map vpLDMap : ipLDList)
    {
      vpLDData.clear();
      vpLDData.dataToSKDCData(vpLDMap);
      try
      {
        if (mpLoadServer.isLoadEmpty(vpLDData.getLoadID()))
        {
          try
          {
            msRouteID = mpRouteServer.getCCIRoute(vpLDData.getLoadID());
            mpIDData.clear();
            mpIDData.setLoadID(vpLDData.getLoadID());
            processItemMove(ipOLData, mpIDData);
          }
          catch(DBException e)
          {
            mpLogger.logDebug("Cycle-Count mpMove not built for Order " +
                              ipOLData.getOrderID() + "." + e.getMessage());
          }
        }
        else
        {
          mpIDData.clear();
          mpIDData.setKey(LoadLineItemData.LOADID_NAME, vpLDData.getLoadID());
          List<Map> vpIDList = mpInvServer.getLoadLineItemDataList(mpIDData);
          buildItemDetailCycleCounts(ipOLData, vpIDList);
        }
      }
      catch(DBException e)
      {
        mpLogger.logDebug("Cycle-Count mpMove not built for Order " +
                          mpOHD.getOrderID() + "." + e.getMessage());
      }
      finally
      {                              // If nothing was allocated, mark order
        checkForNoAllocation();      // on HOLD.
      }
    }
  }
  
  protected void buildItemDetailCycleCounts(OrderLineData ipOLData, List<Map> ipIDList)
            throws AllocationException
  {
    LoadLineItemData vpIDData = Factory.create(LoadLineItemData.class);
    for(Map vpIDMap : ipIDList)
    {
      vpIDData.clear();
      vpIDData.dataToSKDCData(vpIDMap);
      if (vpIDData.getAllocatedQuantity() == 0.0)
      {
        try
        {
          msRouteID = mpRouteServer.getCCIRoute(vpIDData.getLoadID());
          processItemMove(ipOLData, vpIDData);
        }
        catch(DBException e)
        {
          mpLogger.logDebug("Cycle-Count mpMove not built for Order " +
                            ipOLData.getOrderID() + "." + e.getMessage());
        }
        finally
        {                              // If nothing was allocated, mark order
          checkForNoAllocation();      // on HOLD.
        }
      }
    }
  }
  
  protected void checkForNoAllocation()
  {
    mpMVData.clear();
    mpMVData.setKey(MoveData.ORDERID_NAME, mpOHD.getOrderID());
    try
    {
      if (mpMove.getCount(mpMVData) == 0)
      {
        String vsWarn = "Cycle-Count Order " + mpOHD.getOrderID() +
                        "not allocated since all item(s) found were " +
                        "allocated for orders or no item details found.";
        mpAllocServer.revertOrderStatus(mpOHD.getOrderID(), vsWarn, 
                                        DBConstants.HOLD);
        mpOHD.setOrderStatus(DBConstants.HOLD);
        mpLogger.logOperation(vsWarn);
      }
    }
    catch(DBException e)
    {
      mpLogger.logException(e);
    }
  }

  protected void processItemMove(OrderLineData olData, LoadLineItemData idData)
          throws DBException, AllocationException
  {
    String vsDevice = mpLoadServer.getLoadDeviceID(idData.getLoadID());
    
    if (mpDeviceServer.isStationDeviceInoperable(msRouteID) || 
        mpDeviceServer.isDeviceInoperable(vsDevice))
    {
      String vsErr = "Station " + mpOutStation.getStationName() + " has inoperable " +
                     "Device! Allocation not fully performed for Cycle " +
                     "Count Order " + mpOHD.getOrderID();
      throw new AllocationException(vsErr, AllocationException.DEVICE_INOP);
    }
    mpMVData.clear();
    mpMVData.setKey(MoveData.PARENTLOAD_NAME, idData.getLoadID());
    mpMVData.setKey(MoveData.LOADID_NAME, idData.getLoadID());
    mpMVData.setKey(MoveData.MOVETYPE_NAME, Integer.valueOf(DBConstants.CYCLECOUNTMOVE));
    mpMVData.setKey(MoveData.ITEM_NAME, idData.getItem());
    mpMVData.setKey(MoveData.ORDERLOT_NAME, idData.getLot());
    mpMVData.setKey(MoveData.PICKLOT_NAME, idData.getLot());
    if (mpMove.getCount(mpMVData) == 0)
    {
      mpAllocServer.buildItemMove(olData, idData, 0,  mpOHD, msRouteID, vsDevice);
      mpAllocServer.buildReturnData(idData.getLoadID(), msRouteID,
                                  mpAllocatedData);
    }
  }
}

