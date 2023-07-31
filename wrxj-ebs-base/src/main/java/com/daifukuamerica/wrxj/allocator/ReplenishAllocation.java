package com.daifukuamerica.wrxj.allocator;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDedicationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
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
 *  Class implements an Allocation Strategy for Replenishment Allocation.
 *
 *  @author       A.D.
 *  @version      1.0   04/25/05
 */
public class ReplenishAllocation extends AbstractAllocationStrategy implements AllocationStrategy
{
                                       // ******  Protected section ******
  protected String           routeID;
  protected StandardStationServer    stationServer;
  protected StandardOrderServer      orderServer;
  protected StandardInventoryServer  inventoryServer;
  protected StandardDedicationServer dedicatedServer;
  protected OrderLineData    oldata;
  protected LoadLineItemData iddata;
  
                                       // ******  Private section ******
  private   StandardLoadServer       loadServer;
  private   List<Map>             olList;
  private   DBObject         dbobj;
  private   List<AllocationMessageDataFormat> allocatedDataList;

 /**
  *  Default constructor for Replenishment allocation strategy.
  */
  public ReplenishAllocation()
  {
    super();
                                       // ***** Data server objects.  ******
    mpAllocServer = Factory.create(StandardAllocationServer.class, "ReplenishAllocation");
    orderServer = Factory.create(StandardOrderServer.class, "ReplenishAllocation");
    stationServer = Factory.create(StandardStationServer.class, "ReplenishAllocation");
    dedicatedServer = Factory.create(StandardDedicationServer.class, "ReplenishAllocation");
    inventoryServer = Factory.create(StandardInventoryServer.class, "ReplenishAllocation");
    loadServer = Factory.create(StandardLoadServer.class, "ReplenishAllocation");
    
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
  */
  public List<AllocationMessageDataFormat> allocate() throws DBException
  {
    allocatedDataList.clear();
    mpAllocServer.reserveOrder(msOrder);
    
    TransactionToken transTok = null;
    try
    {
      if (!prelimCheckPassed())
        throw new DBException("No allocations performed on order " +
                              msOrder + ". Invalid destination station...");

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
  protected void processOrderLines() throws DBException
  {
    olList = orderServer.getOrderLineData(msOrder);
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

      String itemID   = oldata.getItem();
      double orderQty = oldata.getOrderQuantity();
      int    ppUnit   = inventoryServer.getPiecesPerUnit(itemID);

      try
      {
/*---------------------------------------------------------------------------
    The dedicated location's allocation type tells us to build a load move
    or an item move.  It also tells us whether or not to allocate a full load
    pieces, or full units.  If we allocate by full load, we will only find
    single part number loads.
  ---------------------------------------------------------------------------*/
        int iReplenishType = dedicatedServer.getReplenishType(mpOHD.getDestWarehouse(),
                                                              mpOHD.getDestAddress(),
                                                              oldata.getItem());
      
        List<Map> idList = mpAllocServer.getReplenishments(mpOHD, oldata, mnAisle,
                                                    iReplenishType);
        
        if (!idList.isEmpty())
        {
//          boolean convStation;
          double needQty = orderQty;
          double pickQty = 0.0;
          for(int idLine = 0; idLine < idList.size() && needQty > 0; idLine++)
          {
            iddata.clear();
            iddata.dataToSKDCData(idList.get(idLine));
            LoadLineItemData idTempData = iddata.clone();
            idTempData.clearKeysColumns();
            
//            convStation = loadServer.isLoadInConventionalLocation(idTempData.getLoadID());
                                       // There must always be a route with the same
                                       // name as the order destination.
            routeID = mpOHD.getDestinationStation();

            if (iReplenishType == DBConstants.PIECE)
            {
              if (needQty >= iddata.getCurrentQuantity())
              {
                pickQty = mpAllocServer.reserveFullItemDetail(idTempData.getLoadID());
                needQty -= pickQty;
              }
              else
              {
                pickQty = idTempData.getCurrentQuantity() - idTempData.getAllocatedQuantity();
                mpAllocServer.reserveItemDetail(idTempData, pickQty);
                needQty = orderQty - (pickQty + oldata.getAllocatedQuantity());
              }
            }
            else if (iReplenishType == DBConstants.UNIT)
            {                          // The need quantity in this case should be
                                       // a multiple of pieces-per-unit.  This is
                                       // the contract with whoever creates this order!
              if (ppUnit == 0)
                throw new DBException("Pieces Per Unit must be non-zero for " +
                                      "full unit allocation!!");
              int needQtyCases = (int)Math.floor(needQty/ppUnit);
              pickQty = (needQtyCases*ppUnit);
              mpAllocServer.reserveItemDetail(idTempData, pickQty);
              needQty = orderQty - (pickQty + oldata.getAllocatedQuantity());
            }
            else                       // Full Load case.
            {
              pickQty = mpAllocServer.reserveFullItemDetail(idTempData.getLoadID());
              needQty -= pickQty;
            }
            processMove(oldata, iddata, pickQty, iReplenishType);
            mpAllocServer.updateOrderLine(oldata, (needQty < 0) ? 0 : needQty);
          } // End id. for-loop.
        }
        else
        {
          orderServer.setOrderLineShort(oldata.getOrderID(), oldata.getItem(),
                                        oldata.getOrderLot(), oldata.getLineID(),
                                        DBConstants.YES);
          String errStr = "Order " + oldata.getOrderID() + ", Item " +
                          oldata.getItem() + ", lot '" + oldata.getOrderLot() +
                          " could not be filled!";
          if (mzAllocationDiagnostics)
          {
            mpProbe.addProbeDetails("ReplenishAllocation.processOrderLines",
                                            errStr);
          }
          mpLogger.logDebug(errStr);
          break;
        }
      }
      catch(DBException e)
      {
        mpLogger.logError(e.getMessage());
        if (mzAllocationDiagnostics)
        {
          mpProbe.addProbeDetails("ReplenishAllocation.processOrderLines",
                                          e.getMessage());
        }
        break;
      }
    } // *** End for loop ***
  }

 /**
  *  Method does preliminary check to see a valid destination station can be
  *  found for a dedicated location.
  *  @return <code>false</code> if no item details can be allocated for this
  *          order or else <code>true</code>.
  */
  protected boolean prelimCheckPassed() throws DBException
  {
    boolean rtn = true;
    
    String sDestStation = mpAllocServer.getReplenishDestStation(mpOHD.getDestWarehouse());
    if (sDestStation.trim().length() == 0)
    {
      rtn = false;
      String errStr = "No suitable load delivery station found for " +
                      "dedicated location warehouse " + mpOHD.getDestWarehouse();
      if (mzAllocationDiagnostics)
        mpProbe.addProbeDetails("ReplenishAllocation.prelimCheckPassed",
                                        errStr);
      
    }
    else
    {
      updateDestInfo(sDestStation, msOrder);
    }
    
    return(rtn);
  }
  
 /**
  *  Method to build move for the item detail.
  */
  protected void processMove(OrderLineData olData, LoadLineItemData idData,
                             double pickQuantity, int iReplenishType) throws DBException
  {
    String vsDevice = loadServer.getLoadDeviceID(idData.getLoadID());
    
/*----------------------------------------------------------------------------
   If this load resides in a conventional area don't worry about the route
   for the load.  If on the other hand the load is in an ASRS, we need to choose
   the correct output station for the load.
  ----------------------------------------------------------------------------*/
    if (iReplenishType == DBConstants.LOAD)
      mpAllocServer.buildLoadMove(idData.getLoadID(), mpOHD.getDestinationStation(),
                                  vsDevice, mpOHD);
    else
      mpAllocServer.buildItemMove(olData, idData, pickQuantity, mpOHD, routeID, 
                                  vsDevice);

    loadServer.updateLoadNextLocation(idData.getLoadID(), mpOHD.getDestWarehouse(),
                                      mpOHD.getDestAddress());
    mpAllocServer.buildReturnData(idData.getLoadID(), routeID, allocatedDataList);
  }
  
  protected void updateDestInfo(String sDestinationStation, String sOrderID)
            throws DBException
  {
    mpOHD.setDestinationStation(sDestinationStation);
    setOutputStation(stationServer.getStation(sDestinationStation));
/*----------------------------------------------------------------------------
   For Replenishment requests coming from locations attached to non-conventional
   devices (such as flow racks on the side of an Asrs), find the actual aisle
   group to limit searches for replenishments to that aisle group.  For replenishment
   requests coming from locations with conventional devices set the aisle group
   to -1 (meaning replenishments can come from ASRS or conventional bulk).
  ----------------------------------------------------------------------------*/
    orderServer.setOrderDestinationStation(sOrderID, sDestinationStation);
    setAisleGroup(-1);
  }
}
