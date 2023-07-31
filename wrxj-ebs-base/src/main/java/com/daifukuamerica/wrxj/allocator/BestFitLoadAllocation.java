package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Class implements an Allocation Strategy for Best Load fit.  By Best-Fit
 *  Load is meant a single part number load containing inventory quantitity
 *  that most closely matches the order quantity.  Put another way, we find
 *  *one* load that best fits our order quantity ignoring aging date and Item
 *  detail priority.
 *
 *  @author       A.D.
 *  @version      1.0   08/20/04
  * @version      2.0   11/18/04   Moved over to AllocationServer calls.* 
 */
public class BestFitLoadAllocation extends AbstractAllocationStrategy implements AllocationStrategy
{
  protected String               firstOutputStation;
  protected String               loadLocation;
  protected String               routeID;
  private   DBObject             dbobj;
  private   OrderLineData        oldata;
  private   LoadLineItemData     iddata;
  private   StandardOrderServer  orderServer;
  private   StandardDeviceServer deviceServer;
  private   StandardLoadServer   loadServer;
  private   List<Map>            olList;
  private   List<AllocationMessageDataFormat> allocatedDataList;

 /**
  *  Default constructor for Best-Fit allocation strategy.
  */
  public BestFitLoadAllocation()
  {
    super();
                                       // ***** Data server objects.  ******
    mpAllocServer = Factory.create(StandardAllocationServer.class, "BestFitLoadAllocation");
    orderServer = Factory.create(StandardOrderServer.class, "BestFitLoadAllocation");
    deviceServer = Factory.create(StandardDeviceServer.class, "BestFitLoadAllocation");
    loadServer = Factory.create(StandardLoadServer.class, "BestFitLoadAllocation");
    
                                       // ***** DBAdapter objects.  ******
    oldata = Factory.create(OrderLineData.class);
    iddata = Factory.create(LoadLineItemData.class);
    
    dbobj = new DBObjectTL().getDBObject();
    allocatedDataList = new ArrayList<AllocationMessageDataFormat>();
  }

  /**
   *  Method to find the nearest piece quantity for an order line.
   *  The first basic requirement is that there must be one part number per
   *  Load.  This method rounds down the quantity; this means if the order qty.
   *  = 50 and there are item details for a quantity of 45 and 51, we allocate
   *  the quantity of 45.  If there are only two item details for 25 and 26, we
   *  allocate only 26.
   *
   *  @return <code>List</code> of AllocationMessageDataFormat objects.
   * @throws DBException for DB errors.
   * @throws AllocationException if the station's device is inoperable.
   */
  public List<AllocationMessageDataFormat> allocate() throws DBException, AllocationException
  {
    allocatedDataList.clear();
    olList = mpAllocServer.itemOrderPreallocation(mpOHD);
    TransactionToken transTok = null;
    try                                // If we made it to here we can try to
    {                                  // allocate everything on the OL list.
      transTok = dbobj.startTransaction();
      processOrderLines();
      dbobj.commitTransaction(transTok);
    }
    catch(DBException exc)
    {
      dbobj.endTransaction(transTok);
                                      // Set Order status back to what it was
      mpAllocServer.revertOrderStatus(mpOHD.getOrderID(), "", iOriginalOrderStatus);
      throw exc;
    }
    catch(AllocationException mesg)
    {
      dbobj.endTransaction(transTok);
      String vsOrdMsg = "Application Error in allocator. Exception of type " + 
                         mesg.getClass().getCanonicalName() + " caught!";
      
      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw mesg;                      // Handle next order status by caller so
                                       // that we can effectively determine if 
                                       // it's a short order or some other prob.
    }
    catch(Exception exc)
    {
      dbobj.endTransaction(transTok);
      
      String vsOrdMsg = "Application Error in allocator. Exception of type " + 
                         exc.getClass().getCanonicalName() + " caught!";
      
      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
      throw new DBException("Order " + msOrder + " Not Allocated...", exc);
    }
    finally
    {
      dbobj.endTransaction(transTok);
    }

    return(allocatedDataList);
  }

 /**
  *  Method does the work of going through order lines and checking item
  *  details that can be allocated.  All candidate loads will be single
  *  part number loads.
  */
  private void processOrderLines() throws DBException, AllocationException
  {
                                       // Serious problem here! No order lines.
    if (olList == null || olList.size() == 0)
    {
      throw new DBException("No order lines found for order " +
                            mpOHD.getOrderID());
    }

    for(int ordLineIdx = 0; ordLineIdx < olList.size(); ordLineIdx++)
    {
      oldata.clear();
      oldata.dataToSKDCData(olList.get(ordLineIdx));
/*---------------------------------------------------------------------------
   For best-fit if any order line has already been allocated, assume it was the
   best that could've been done for that line item and move onto a line that
   has no allocation.  (Recall that for best fit we allocate a load that best
   fits the order quantity, where the load qty. <= order qty)
  ---------------------------------------------------------------------------*/
      double allocQty  = oldata.getAllocatedQuantity();
      double pickedQty = oldata.getPickQuantity();
      if (allocQty == 0 && pickedQty == 0)
      {
        String itemID   = oldata.getItem();
        String ordLot   = oldata.getOrderLot();
        double orderQty = oldata.getOrderQuantity();

        try
        {
                                       // Find the best-fit item detail
          LoadLineItemData idData = null;
          idData = mpAllocServer.getBestFitItemDetail(mnAisle, itemID, ordLot,
                                                    orderQty);
          if (idData != null)
          {
            Map<String, String> hMap = mpAllocServer.getLoadOutputStation(
                idData.getLoadID(), mpOutStation.getStationName(),
                mpOHD.getDestinationStation(), mnAisle);        
            firstOutputStation = hMap.get(ParameterNameConstants.STATIONNAME);
            loadLocation = hMap.get(ParameterNameConstants.LOCATION);
            routeID = hMap.get(ParameterNameConstants.ROUTEID);

            double needQty;
            if (orderQty >= idData.getCurrentQuantity())
              needQty = orderQty - idData.getCurrentQuantity();
            else
              needQty = 0;

            mpAllocServer.reserveFullItemDetail(idData.getLoadID());            
            processMove(oldata, idData);
            mpAllocServer.updateOrderLine(oldata, needQty);
          }
          else
          {
            orderServer.setOrderLineShort(oldata.getOrderID(), oldata.getItem(),
                                          oldata.getOrderLot(), oldata.getLineID(),
                                          DBConstants.YES);
            mpLogger.logDebug("Order " + oldata.getOrderID() + ", Item " +
                            oldata.getItem() + ", lot '" + oldata.getOrderLot() +
                            "' has oldest product in load " + iddata.getLoadID() +
                            " at location " + loadLocation + ". Output station " +
                            "not on valid route " + routeID);
            break;
          }
        }
        catch(DBException e)
        {
          mpLogger.logDebug(e.getMessage());
          break;
        }
      }
    } // *** End for loop ***
  }

 /**
  *  Method to build move for the item detail.
  */
  private void processMove(OrderLineData olData, LoadLineItemData idData)
          throws DBException, AllocationException
  {
    String vsDevice = loadServer.getLoadDeviceID(idData.getLoadID());
    
    if (deviceServer.isStationDeviceInoperable(firstOutputStation) || 
        deviceServer.isDeviceInoperable(vsDevice))
    {
      String vsErr = "Station's Device or Load Device is not operational! "  +
                     "Allocation Order '" + olData.getOrderID() + "' Item '" +
                    olData.getItem() + "' and Lot '" + idData.getLot() + "' ";
      throw new AllocationException(vsErr, AllocationException.DEVICE_INOP);
    }
    mpAllocServer.buildItemMove(olData, idData, idData.getCurrentQuantity(),
                                mpOHD, routeID, vsDevice);
    mpAllocServer.buildReturnData(idData.getLoadID(), firstOutputStation,
                                  allocatedDataList);
  }
}
