
package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Baseline class for line based allocation.  This is used for orders that
 * don't specify an output station but do specify routes on each order line.
 * 
 * @author A.D.
 * @since 18-Jun-2010
 */
public class LineAllocation extends PieceAllocation
{
  public LineAllocation()
  {
    super();
  }

  @Override
  protected void processOrderLines() throws DBException, AllocationException
  {
    OrderLineData vpOLData = Factory.create(OrderLineData.class);

/*===========================================================================
       Step through each Order Line and find the Item detail(s) for it.
  ===========================================================================*/
    for(Map vpOLMap : mpOLList)
    {
      vpOLData.dataToSKDCData(vpOLMap);
      double vdNeedQty = vpOLData.getOrderQuantity() - vpOLData.getAllocatedQuantity();
      if (vdNeedQty == 0)
      {
        if (vpOLData.getLineShy() == DBConstants.YES)
        {
          mpOrderServer.setOrderLineShort(vpOLData.getOrderID(), vpOLData.getItem(),
                                          vpOLData.getOrderLot(), vpOLData.getLineID(),
                                          DBConstants.NO);
        }
        continue;
      }

      List<Map> vpIDList = mpAllocServer.getOldestItemDetails(vpOLData.getItem(),
                                                              vpOLData.getOrderLot(),
                                                              vdNeedQty);
      if (vpIDList.isEmpty())
      {
        continue;
      }

      for(Iterator<Map> vpIter = vpIDList.iterator(); vpIter.hasNext() && vdNeedQty > 0; )
      {
        mpIDData.dataToSKDCData(vpIter.next());
        double vdAvailQty = mpIDData.getCurrentQuantity() - mpIDData.getAllocatedQuantity();
        LoadLineItemData idTempData = mpIDData.clone();
        idTempData.clearKeysColumns();

        String vsLoadLocation = "";
        if (mpLoadServer.isLoadInRack(mpIDData.getLoadID()))
        {
          msFirstOutputStation = vpOLData.getRouteID();
          vsLoadLocation = mpLoadServer.getLoadLocation(mpIDData.getLoadID());
          msRouteID = vpOLData.getRouteID();
        }
        else
        {
          LoadData vpLoadData = mpLoadServer.getLoad(idTempData.getLoadID());
          if (vpLoadData == null)
            throw new DBException("Critical Error! Load " + idTempData.getLoadID() +
                                  " deleted by some other process during allocation!");

          int vnLoadStatus = vpLoadData.getLoadMoveStatus();

          msFirstOutputStation = msRouteID = "";
          vsLoadLocation = vpLoadData.getAddress();

/*============================================================================
      If the load's final location is a station don't allocate from it.
============================================================================*/
          if (mpLocationServer.isLocationAStation(vpLoadData.getFinalWarehouse(),
                                                  vpLoadData.getFinalAddress()))
          {
            break;
          }

/*============================================================================
 Don't allocate from inbound loads since we need a valid output route in
 the move record for everything to work.
============================================================================*/
          if ((vnLoadStatus == DBConstants.MOVING ||
               vnLoadStatus == DBConstants.RETRIEVING ||
               vnLoadStatus == DBConstants.RETRIEVESENT)&&
              mpMoveServer.moveExists(idTempData.getLoadID()))
          {                            // Must be an outbound load.
            msFirstOutputStation = vpLoadData.getNextAddress();
            msRouteID = vpOLData.getRouteID();
            vsLoadLocation = "";
          }
          else if (vnLoadStatus != DBConstants.ARRIVEPENDING &&
                   vnLoadStatus != DBConstants.MOVEPENDING   &&
                   vnLoadStatus != DBConstants.STOREPENDING  &&
                   vnLoadStatus != DBConstants.STORESENT     &&
                   vnLoadStatus != DBConstants.STORING       &&
                   vnLoadStatus != DBConstants.STOREERROR    &&
                   vnLoadStatus != DBConstants.MOVING        &&
                   mpLocationServer.isLocationAStation(vpLoadData.getWarehouse(), vpLoadData.getAddress()))
          {
            msFirstOutputStation = vpLoadData.getAddress();
            vsLoadLocation = vpLoadData.getAddress();
            msRouteID = vpOLData.getRouteID();
          }
          else
          {                            // The oldest product is likely on an input
            break;                     // type station. Stop allocation until
                                       // it comes back in.
          }
        }

        try
        {
          if (msRouteID != null && msRouteID.trim().length() > 0)
          {
            if (vdNeedQty >= vdAvailQty)
            {
              mpAllocServer.reserveItemDetail(idTempData, vdAvailQty);
              processMove(vpOLData, mpIDData, vdAvailQty);
              vdNeedQty -= vdAvailQty;
            }
            else
            {
              mpAllocServer.reserveItemDetail(idTempData, vdNeedQty);
              processMove(vpOLData, mpIDData, vdNeedQty);
              vdNeedQty = 0;
            }
            mpAllocServer.updateOrderLine(vpOLData, vdNeedQty);
          }
          else
          {
            mpOrderServer.setOrderLineShort(vpOLData.getOrderID(), vpOLData.getItem(),
                                            vpOLData.getOrderLot(), vpOLData.getLineID(),
                                            DBConstants.YES);
            String vsErrStr = "Order " + vpOLData.getOrderID() + ", Item " +
                              vpOLData.getItem() + ", lot '" +
                              vpOLData.getOrderLot() + "' has oldest product in load " +
                             mpIDData.getLoadID() + " at location " + vsLoadLocation +
                            ". Output station not on valid route " + msRouteID;
            mpLogger.logDebug(vsErrStr);
            if (mzAllocationDiagnostics)
              mpProbe.addProbeDetails("LineAllocation.processOrderLines",
                                      "No route info. or output station found " +
                                      "for load " + mpIDData.getLoadID() +
                                      " and order item " + mpIDData.getItem() +
                                      ". " + vsErrStr);
          }
        }
        catch(DBException exc)
        {                            // Move onto the next Order Line.
          mpLogger.logError(exc.getMessage());
          if (mzAllocationDiagnostics)
            mpProbe.addProbeDetails("LineAllocation.processOrderLines",
                                    "Error allocating line OrderID " +
                                     vpOLData.getOrderID() + " Item " +
                                     vpOLData.getItem() + ", lot " +
                                     vpOLData.getOrderLot() + "... " +
                                     exc.getMessage());
        }
      } // *** End LoadLineItem for-loop ***
    } // **** End OrderLine for-loop ****
  }
}
