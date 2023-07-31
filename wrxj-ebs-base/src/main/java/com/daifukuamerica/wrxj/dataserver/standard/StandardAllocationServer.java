package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.allocator.AllocationException;
import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.allocator.AllocationProbe;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

/**
 * Description:<BR>
 *   Server to handle Allocation Specific operations.
 *
 * @author       A.D.
 * @version      1.0     11/18/04
 */
public class StandardAllocationServer extends StandardServer
{
/*---------------------------------------------------------------------------
   Our coding "standard" discourages the creation of servers in this manner.
   However this server should not be called from other servers and therefore
   it is safe to have objects created and used like this.
  ---------------------------------------------------------------------------*/
  protected String           gpShortOrderProcess;
  private AllocationProbe  gpAllocationProbe;
  protected OrderHeaderData  mpOHData = Factory.create(OrderHeaderData.class);
  private OrderLineData    oldata = Factory.create(OrderLineData.class);
  private Station          station = Factory.create(Station.class);
  protected Load           load = Factory.create(Load.class);
  private LoadData         lddata = Factory.create(LoadData.class);
  protected LoadLineItem     loadLine = Factory.create(LoadLineItem.class);
  private LoadLineItemData iddata = Factory.create(LoadLineItemData.class);
  protected Location         mpLocation = Factory.create(Location.class);
  protected OrderLine      orderLine = Factory.create(OrderLine.class);
  protected TableJoin      mpTJ = Factory.create(TableJoin.class);
  protected MoveData       mpMVData = Factory.create(MoveData.class);
  protected Move           mpMove = Factory.create(Move.class);

  protected StandardDedicationServer mpDedServer     = null;
  protected StandardLoadServer       mpLoadServer    = null;
  protected StandardLocationServer   mpLocServer     = null;
  protected StandardOrderServer      mpOrderServer   = null;
  protected StandardStationServer    mpStationServer = null;
  protected StandardRouteServer      mpRouteServer   = null;
  protected StandardMoveServer       mpMoveServer    = null;

  public StandardAllocationServer()
  {
    this(null);
  }

  public StandardAllocationServer(String keyName)
  {
    super(keyName);
    gpShortOrderProcess = Application.getString("ShortOrderProcessing");
    logDebug("StandardAllocationServer()");
  }
  
  public StandardAllocationServer(String keyName, DBObject dbo)
  {
    super(keyName, dbo);
    gpShortOrderProcess = Application.getString("ShortOrderProcessing");
    logDebug("StandardAllocationServer()");
  }

 /**
  *  Attaches the allocation diagnostic probe that will report allocation issues.
  *  @param ipAllocationProbe the Allocation Probe to use for diagnostics.
  */
  public void setAllocationProbe(AllocationProbe ipAllocationProbe)
  {
    gpAllocationProbe = ipAllocationProbe;
    mpTJ.attachAllocationProbe(gpAllocationProbe);
  }

 /**
  *  Method does preallocation processing on a load order.  This method reserves
  *  an order, and checks order lines for shortages. This method does a
  *  preliminary check to see if the load still exists for the order. It
  *  may be that Loads are deleted off the system, but the load order tied to
  *  that load is not!  If this is the case we delete all violating lines and
  *  and potentially the order itself.
  *  @param ordData <code>OrderHeaderData</code> containing order info.
  *  @return List containing order lines for this order.
  *  @throws DBException when there is a database error,
  *          or if the Order Header is deleted due to no more lines being left.
  */
  public List<Map> loadOrderPreallocation(OrderHeaderData ordData)
         throws DBException
  {
    String sOrderID = ordData.getOrderID();
    initializeOrderServer();
    List<Map> orderLineList = null;
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
                                       // Lock order for allocation.
      OrderHeaderData myOrderData = mpOrderServer.getOrderHeaderRecord(sOrderID,
                                                                   DBConstants.WRITELOCK);
      allowOrderReserve(myOrderData);
                                       // Reserve the order for allocation.
      mpOrderServer.setOrderStatusValue(sOrderID, DBConstants.ALLOCATING);
                                       // Inspect the order lines for loads that
                                       // may be missing.
      orderLineList = mpOrderServer.getOrderLineData(sOrderID);
      if (orderLineList.size() == 0)   // Serious! No order lines for an order??
      {
        logError("No Order Lines found for Order " + sOrderID +
                 "\n StandardAllocationServer-->loadOrderPreallocation");
        throw new DBException("No order lines found for Order " + sOrderID);
      }

      int listSize = orderLineList.size();
      for(int ordLineIdx = 0; ordLineIdx < listSize; ordLineIdx++)
      {
        Map currObj = orderLineList.get(ordLineIdx);
        String sLoadID = DBHelper.getStringField(currObj, OrderLineData.LOADID_NAME);
        if (!load.exists(sLoadID))
        {
          try
          {
            mpOrderServer.deleteLoadOrderLine(sOrderID, sLoadID);
          }
          catch(DBException e)
          {
            // This is okay.  Look for the next order line if there is one.
          }
                                         // If order is gone now, throw exception
                                         // to leave, and notify controller.
          mpOHData.clear();
          mpOHData.setKey(OrderHeaderData.ORDERID_NAME, sOrderID);
          if (!mpOrderServer.OrderHeaderExists(mpOHData))
          {
            logDiagnostic("StandardAllocationServer.loadOrderPreallocation",
                          "Load Order " + sOrderID +
                          "deleted due to missing load(s)!  No allocations performed.");
            throw new DBException("Load Order " + sOrderID +
                                  "deleted due to missing load(s)!  No " +
                                  "allocations performed.");
          }
        }
      }  /* End for-loop */
      commitTransaction(ttok);
    }
    finally
    {
      endTransaction(ttok);
    }

    return(orderLineList);
  }

 /**
  * Method does preallocation processing on an order.  This method reserves an
  * order, and checks order lines for shortages.
  * @param ordData <code>OrderHeaderData</code> containing order info.
  * @return List containing order lines for this order.
  * @throws DBException when there is a database error.
 * @throws AllocationException
  */
  public List<Map> itemOrderPreallocation(OrderHeaderData ordData) throws DBException, AllocationException
  {
    initializeOrderServer();

    String sOrderID = ordData.getOrderID();

    List<Map> vpOLList = null;
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
                                       // Lock order for allocation.
      OrderHeaderData myOrderData = mpOrderServer.getOrderHeaderRecord(sOrderID,
                                                                   DBConstants.WRITELOCK);
      allowOrderReserve(myOrderData);
                                       // Reserve the order for allocation.
      mpOrderServer.setOrderStatusValue(sOrderID, DBConstants.ALLOCATING);
                                       // First check if order is short, and if
                                       // short orders are allowed.
      vpOLList = getAllocatableItemOrderLines(sOrderID);
      if (vpOLList.isEmpty())          // Serious! No order lines for an order??
      {
        logError("No Order Lines found for Order " + sOrderID +
                 "\n StandardAllocationServer-->itemOrderPreallocation");
        throw new DBException("No order lines found for Order " + sOrderID);
      }

      double ordQty, allocQty, vdRemainingUnAllocQty;
      OrderLineData vpOLData = Factory.create(OrderLineData.class);

      for(Map vpOLMap : vpOLList)
      {
        vpOLData.clear();
        vpOLData.dataToSKDCData(vpOLMap);
        String sItem = vpOLData.getItem();
        String sOrderLot = vpOLData.getOrderLot();
        String sLineID = vpOLData.getLineID();
        ordQty = vpOLData.getOrderQuantity();
        allocQty = vpOLData.getAllocatedQuantity();
        vdRemainingUnAllocQty = ordQty - allocQty;

        LoadLineItem vpLoadLine = Factory.create(LoadLineItem.class);
        double avlqty = vpLoadLine.getTotalAvailableQuantity(sItem, true);
                                       // Mark the order line as short.
        if (avlqty == 0 || (vdRemainingUnAllocQty > 0 && avlqty < vdRemainingUnAllocQty))
        {
          mpOrderServer.setOrderLineShort(sOrderID, sItem, sOrderLot, sLineID,
                                          DBConstants.YES);
        }
      }
      commitTransaction(ttok);
    }
    finally
    {
      endTransaction(ttok);
    }

    return(vpOLList);
  }

 /**
  *  Method marks an order as allocating.
  * @param sOrderID <code>String</code> containing order ID.
  * @throws DBException
  */
  public void reserveOrder(String sOrderID) throws DBException
  {
    initializeOrderServer();

    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
      OrderHeaderData myOrderData = mpOrderServer.getOrderHeaderRecord(sOrderID,
                                                                 DBConstants.WRITELOCK);
      if (myOrderData == null)
      {
        throw new DBException("Order " + sOrderID + " is no longer on the system...");
      }
      allowOrderReserve(myOrderData);
                                     // Reserve the order for allocation.
      mpOrderServer.setOrderStatusValue(sOrderID, DBConstants.ALLOCATING);
      commitTransaction(ttok);
    }
    finally
    {
      endTransaction(ttok);
    }
  }

 /**
  * Gets the controlling station for a dedicated location.  <b>Note:</b> for a
  * Flow Rack dedicated location, this method should be over-ridden to get the
  * correct dedicated station.
  * @param isDestWarehouse the warehouse of the dedicated location.
  * @return controlling station for this dedicated location (could potentially
  *         be an empty string).
  */
  public String getReplenishDestStation(String isDestWarehouse) throws DBException
  {
    initializeStationServer();

    String[] vpStations = mpStationServer.getStationsByWarehouse(isDestWarehouse);

    return((vpStations.length > 0) ? vpStations[0] : "");
  }

 /**
  *  Finds Load Line Items to fill a replenishment order. All loads should be single
  *  part number loads, and have no previous allocations. The algorithm starts
  *  by trying to find replenishments in a particular aisle group if it is provided
  *  (something other than -1).  If enough quantities are not found from this
  *  initial search, find inventory from alternate location sources and any
  *  aisle group.
  *  @param ohData  <code>OrderHeaderData</code> containing order header info.
  *  @param olData  <code>OrderLineData</code> containing order line info.
  *  @param iAisleGroup  the aislegroup in which to search for replenishment data.
  *         If this is set to -1, it means any bulk area is open for a search.
  *  @return  <code>List</code> of oldest item detail records ordered by
  *           Allocation priority, and scheduled date.
  */
  public List<Map> getReplenishments(OrderHeaderData ohData, OrderLineData olData,
                                int iAisleGroup, int iReplenishAllocType) throws DBException
  {
    int[] replenSources = station.getReplenishmentSources(ohData.getDestinationStation());
    if (replenSources.length == 0)
    {
      String errStr = "No Replenishment sources configured for station " +
                      ohData.getDestinationStation();
      logDiagnostic("StandardAllocationServer.getReplenishments", errStr);
      throw new DBException(errStr);
    }
    List<Map> replenList = new ArrayList<Map>();
    double fOrderQty = olData.getOrderQuantity() - olData.getAllocatedQuantity();
    double amountFillable = mpTJ.getReplenItemDetails(olData.getItem(),
                                                           iReplenishAllocType, iAisleGroup,
                                                           replenSources[0], fOrderQty,
                                                           replenList);
/*---------------------------------------------------------------------------
   If order line wasn't filled completely subtract off what was filled, and
   try to fill remaining amount from alternate replenishment location if one
   exists.
  ---------------------------------------------------------------------------*/
    if (amountFillable < fOrderQty)
    {
      fOrderQty -= amountFillable;
      if (replenSources.length > 1)
      {
        for(int idx = 1; idx < replenSources.length; idx++)
        {
          amountFillable = mpTJ.getReplenItemDetails(olData.getItem(), iReplenishAllocType,
                                                          -1, replenSources[idx],
                                                          fOrderQty, replenList);
          if (amountFillable >= fOrderQty) break;
          else                             fOrderQty -= amountFillable;
        }
      }
    }

    return(replenList);
  }

 /**
  *  Method finds oldest item details in the system for the item, order lot
  *  combination. First preference is given to Dedicated Locations, and then
  *  other locations.
  *  @param sItem  <code>String</code> containing order line item.
  *  @param sOrderLot  <code>String</code> containing ordered lot.
  *  @param neededAmount  <code>double</code> value indicating amount needed to
  *         fill the order line.
  *  @param ipCustomObj Object containing custom data that may be used in projects.
  * @return  <code>List</code> of oldest item detail records ordered by
  *           Allocation priority, and scheduled date.
  *  @throws DBException when a database error occurs.
  */
  public List<Map> getOldestItemDetails(String sItem, String sOrderLot,
                                        double neededAmount, Object...ipCustomObj)
         throws DBException
  {
    initializeDedicationServer();

    List<Map> rtnList = null;

    if (mpDedServer.isDedicatedItem(sItem))
    {
      List<Map> dedicatedIDList = mpTJ.getOldestDedicatedItemDetails(sItem, sOrderLot);
      if (dedicatedIDList.isEmpty())
      {
        rtnList = mpTJ.getOldestItemDetails(sItem, sOrderLot, false);
      }
      else if (!isSufficientAmountAvail(dedicatedIDList, neededAmount))
      {
        List<Map> idList = mpTJ.getOldestItemDetails(sItem, sOrderLot, false, ipCustomObj);
        if (idList.isEmpty())
        {
          rtnList = dedicatedIDList;
        }
        else
        {
          rtnList = new ArrayList<Map>(dedicatedIDList.size() + idList.size());
          int dedListLen = dedicatedIDList.size();
          for(int dedIdx = 0; dedIdx < dedListLen; dedIdx++)
          {
            rtnList.add(dedicatedIDList.get(dedIdx));
            String vsDedLoad = DBHelper.getStringField(dedicatedIDList.get(dedIdx),
                                                       LoadLineItemData.LOADID_NAME);
            int idListLen = idList.size();
            for(int regLocIdx = 0; regLocIdx < idListLen; regLocIdx++)
            {
              if (idList.get(regLocIdx) == null) continue;
              String vsNormLoad = DBHelper.getStringField(idList.get(regLocIdx),
                                                     LoadLineItemData.LOADID_NAME);
              if (vsDedLoad.equals(vsNormLoad))
              {
                idList.set(regLocIdx, null);
              }
            }
          }

          int idListLen = idList.size();
          for(int regIdx = 0; regIdx < idListLen; regIdx++)
          {
            if (idList.get(regIdx) != null) rtnList.add(idList.get(regIdx));
          }
        }
      }
      else
      {
        rtnList = dedicatedIDList;
      }
    }
    else
    {
      rtnList = mpTJ.getOldestItemDetails(sItem, sOrderLot, false, ipCustomObj);
    }

    return(rtnList);
  }

  /**
   * Method finds totally empty containers in the system.
   *
   * @param isZoneGroup
   * @param inAisleGroup <code>int</code> containing requesting station's
   *            aisle group.
   * @param isContainerType <code>String</code> containing load's
   * @param inHeight <code>int</code> the location height
   * @param isDestination <code>String</code> destination station
   * @return <code>List</code> of Load's that match the order line container
   *         type.
   * @throws DBException
   */
  public List<Map> getCompleteEmpties(String isZoneGroup, int inAisleGroup,
      String isContainerType, int inHeight, String isDestination)
      throws DBException
  {
    return mpTJ.getCompleteEmpties(isZoneGroup, inAisleGroup,
        isContainerType, inHeight, isDestination);
  }

  /**
   * Method finds partially empty containers that most closely match the amount
   * full value.
   *
   * @param isZoneGroup <code>String</code> containing requesting station's
   *            Recommended Zone
   * @param inAisleGroup <code>int</code> containing requesting station's aisle
   *            group.
   * @param ipOLData <code>OrderLineData</code> containing Order Line Info.
   * @param inAmtFullValue the amount full value, as opposed to the amount empty
   *            value.
   * @param izWithLike <code>boolean</code> if set to <code>true</code> we
   *            find partially empty containers by matching aisle group and
   *            item.
   * @param isDestination <code>String</code> destination station
   * @return <code>List</code> of partially empty loads.
   * @throws DBException
   */
  public List<Map> getPartialEmpties(String isZoneGroup, int inAisleGroup,
      OrderLineData ipOLData, int inAmtFullValue, boolean izWithLike,
      String isDestination) throws DBException
  {
    return mpTJ.getPartialEmpties(isZoneGroup, inAisleGroup, ipOLData,
        inAmtFullValue, izWithLike, isDestination);
  }

 /**
  *  Find all item details that belong to a particular Aisle Group and find
  *  the quantity that best fits our order line quantity.
  *
  *  @param aisleGroup <code>int</code> Aisle group for which we need to find
  *                                    an item detail.
  *  @param itemID <code>String</code> containing item.
  *  @param ordLot <code>String</code> containing lot.
  *  @param orderQty <code>double</code> Order Line quantity we're trying to
  *                                       match.
  *
  *  @return LoadLineItemData object containing Item Detail info. matching our
  *          search criteria.
  */
  public LoadLineItemData getBestFitItemDetail(int aisleGroup, String itemID,
                                               String ordLot, double orderQty)
         throws DBException
  {
    return(mpTJ.getBestFitItemDetail(aisleGroup, itemID, ordLot,
                                          orderQty));
  }

 /**
  * Method checks if an order is in the correct state to be allocated.
  * @param ordData <code>OrderHeaderData</code> containing order info.
  * @throws DBException if the order is of the wrong
  *         status for allocation.
  */
  public void allowOrderReserve(OrderHeaderData ordData) throws DBException
  {
    if (ordData == null)
    {
      String mesg = "No Order Header record for Order!";
      logError(mesg);
      throw new DBException(mesg);
    }
                                       // Make sure nothing has changed on us.
    int ordStatus = ordData.getOrderStatus();
    if (ordStatus != DBConstants.READY && ordStatus != DBConstants.ALLOCATENOW &&
        ordStatus != DBConstants.SHORT && ordStatus != DBConstants.REALLOC &&
        ordStatus != DBConstants.HOLD)
    {
      String s = "";
      try {s = DBTrans.getStringValue("iOrderStatus", ordStatus);}
      catch (Exception e)
      {
        logError("StandardAllocationServer - DBTrans CANNOT find iOrderStatus - " +
                 "allowOrderReserve()");
      }
      String mesg = "Order " + ordData.getOrderID() + " is in " + s +
                    " state. Can't retry allocation.";
      throw new DBException(mesg);
    }
  }

 /**
  *  Method to check if a load already has a Load Move.
  *  @param superLoad  <code> String</code> containing super load id.
  *  @param loadid <code>String</code> containing Load id.
  *  @return   <code>true</code> if there are <b>no</b> moves in the system,
  *            <code>false</code> otherwise.
  */
  public boolean loadNeedsMove(String superLoad, String loadid)
  {
    boolean rtn = false;
    try
    {
      rtn = mpMove.loadNeedsMove(superLoad, loadid);
    }
    catch(DBException e)
    {
      logException(e, "In AllocationServer.loadNeedsMove()");
    }

    return(rtn);
  }

 /**
  *  Method checks if a load already has a move using load id. and move type
  *  in the check.
  *  @param sLoadID <code>String</code> containing Load id.
  *  @param iMoveType <code>int</code> containing move type to check for.
  *  @return   <code>true</code> if there are <b>no</b> moves in the system,
  *            <code>false</code> otherwise.
  */
  public boolean loadNeedsMove(String sLoadID, int iLoadMoveType)
  {
    boolean rtn = false;
    try
    {
      rtn = mpMove.loadNeedsMove(sLoadID, iLoadMoveType);
    }
    catch(DBException e)
    {
      logException(e, "In AllocationServer.loadNeedsMove()");
    }

    return(rtn);
  }

 /**
  *  Method to check if a load already has a move for this Order, item, and lot.
  *
  *  @param superLoad  <code> String</code> containing super load id.
  *  @param idData     <code>LoadLineItemData</code> containing LoadLineItem info.
  *  @param olData <code>OrderLineData</code> containing Order line data.
  *  @return   <code>true</code> if there are <b>no</b> moves in the system,
  *            <code>false</code> otherwise.
  */
  public boolean loadNeedsMove(String superLoad, LoadLineItemData idData,
                               OrderLineData olData)
  {
    boolean rtn = false;
    try
    {
      rtn = mpMove.loadNeedsMove(olData.getOrderID(), superLoad, idData.getLoadID(),
                               idData.getItem(), idData.getLot(),
                               olData.getOrderLot(), olData.getLineID());
    }
    catch(DBException e)
    {
      logException(e, "In loadNeedsMove");
    }

    return(rtn);
  }

 /**
  *  Method to reserve the total current item detail quantity for an allocation
  *  request.  <b>Note: This method is used for load allocations, with the
  *  assumption that no item detail on the load has been allocated.  If an item
  *  detail has been previously allocated, it will be left alone.</b>
  *
  *  @param sLoadID <code>String</code> containing Item detail load id.
  *  @return amountReserved double value of the total amount that was reserved.
  */
  public double reserveFullItemDetail(String sLoadID) throws DBException
  {
    double amountReserved = 0;
                                       // Update item detail allocated qty.
    List<Map> idList = loadLine.getLoadLineItemDataListByLoadID(sLoadID);
    int listLength = idList.size();
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
      for(int idx = 0; idx < listLength; idx++)
      {
        Map currObj = idList.get(idx);
        String sItem = DBHelper.getStringField(currObj, LoadLineItemData.ITEM_NAME);
        String sLot = DBHelper.getStringField(currObj, LoadLineItemData.LOT_NAME);
        double fCurrentQuantity = DBHelper.getDoubleField(currObj, LoadLineItemData.CURRENTQUANTITY_NAME);
        double fAllocatedQuantity = DBHelper.getDoubleField(currObj, LoadLineItemData.ALLOCATEDQUANTITY_NAME);
        if (fAllocatedQuantity == 0)
        {
          iddata.clear();
          iddata.setKey(LoadLineItemData.LOADID_NAME, sLoadID);
          iddata.setKey(LoadLineItemData.ITEM_NAME, sItem);
          iddata.setKey(LoadLineItemData.LOT_NAME, sLot);

          iddata.setAllocatedQuantity(fCurrentQuantity);
          loadLine.modifyElement(iddata);
          amountReserved += fCurrentQuantity;
        }
      } /* end for-loop */
      commitTransaction(ttok);
    }
    finally
    {
      endTransaction(ttok);
    }

    return(amountReserved);
  }

  /**
   * Method to reserve the item detail quantity for an allocation request.
   *
   * @param ipLLIData <code>LoadLineItemData</code> containing Item detail data.
   * @param idReserveQty <code>double</code> containing the quantity to reserve.
   */
  public void reserveItemDetail(LoadLineItemData ipLLIData, double idReserveQty)
        throws DBException
  {
    // Read with write-lock to avoid currency issues
    ipLLIData.setKey(LoadLineItemData.LOADID_NAME, ipLLIData.getLoadID());
    ipLLIData.setKey(LoadLineItemData.ITEM_NAME, ipLLIData.getItem());
    ipLLIData.setKey(LoadLineItemData.LOT_NAME, ipLLIData.getLot());
    ipLLIData.setKey(LoadLineItemData.LINEID_NAME, ipLLIData.getLineID());
    ipLLIData.setKey(LoadLineItemData.POSITIONID_NAME, ipLLIData.getPositionID());
    LoadLineItemData vpLLIData = loadLine.getElement(ipLLIData, DBConstants.WRITELOCK);
    try
    {
      if (vpLLIData != null)
      {
        if (vpLLIData.getAllocatedQuantity() + idReserveQty > vpLLIData.getCurrentQuantity())
        {
          throw new DBException(
              "Allocated quantity changed unexpectedly for item "
                  + ipLLIData.getItem() + " lot " + ipLLIData.getLot()
                  + " on load " + ipLLIData.getLoadID());
        }

      // Update item detail allocated qty.
        ipLLIData.setAllocatedQuantity(vpLLIData.getAllocatedQuantity() + idReserveQty);
        loadLine.modifyElement(ipLLIData);
      }
    }
    catch(NoSuchElementException e)
    {
      throw new DBException(e.getMessage());
    }
  }

  /**
   * Method to convert a decimal amount representing the amount empty to the
   * amount full translation value.
   *
   * @param idAmountEmptyDecimal The amount empty in decimal form. Normally this
   *          is what is ordered on an empty container order.
   * @return the translation value of the amount full.
   */
  public int amountEmptyToAmountFullTrans(double idAmountEmptyDecimal)
  {
    int vnAmtFullTran = -1;
    Map<String, AmountFullTransMapper> vpAmtFullMapperMap = LoadData.getAmountFullDecimalMap();

    double vdAmountFull = SKDCUtility.getTruncatedDouble(1.0 - idAmountEmptyDecimal);
                                       // Get Set of keys which in this case are
                                       // the fractions stored as strings.
    Set<String> vpFractionKeySet = vpAmtFullMapperMap.keySet();
    for(String vsFraction : vpFractionKeySet)
    {
      AmountFullTransMapper vpTempMapper = vpAmtFullMapperMap.get(vsFraction);
      if (vpTempMapper.getPartialAmtFullDecimal() == vdAmountFull)
      {
        vnAmtFullTran = vpTempMapper.getPartialAmtFullTranVal();
        break;
      }
    }

    return(vnAmtFullTran);
  }

  /**
   * Builds move records associated with load lines for an order.  For loads
   *  with multiple levels of sub-loads, this method will insert the top-most
   *  load into the "sParentLoad" field of the Move record, and the load to
   *  be picked into the "sLoadID" field of the move record.
   *
   *  Once the pick screen sees that the load is a Full Load Allocation
   *  it knows
   *  that all inventory on the Load that was ordered in the OrderLine (or the
   *  sub-Load in the move record), is to be decremented.  It then deletes
   *  the associated move record using the unique "sMoveID" key.
   *  <i>NOTE: This method should be called from within a transaction</i>
   *
   * @param isAllocatedLoad Load ordered out
   * @param isRouteID Route for the load
   * @param isLoadDevice The load's device.
   * @param ipOHData OrderHeaderData to provide order and priority
   * @throws DBException for database update errors.
   */
  public void buildLoadMove(String isAllocatedLoad, String isRouteID,
                           String isLoadDevice, OrderHeaderData ipOHData)
         throws DBException
  {
    String superLoad = isAllocatedLoad;  // Here for future extensibility.
    if (loadNeedsMove(isAllocatedLoad, isAllocatedLoad))
    {
      mpMVData.clear();
      mpMVData.setMoveID(Math.abs(new Random().nextInt()));
      mpMVData.setParentLoad(isAllocatedLoad);
      mpMVData.setLoadID(isAllocatedLoad);
      mpMVData.setOrderID(ipOHData.getOrderID());
      mpMVData.setRouteID(isRouteID);
      mpMVData.setDeviceID(isLoadDevice);
      mpMVData.setMoveType(DBConstants.LOADMOVE);
      int ordType = ipOHData.getOrderType();

      if (ordType == DBConstants.REPLENISHMENT)
        mpMVData.setMoveCategory(DBConstants.REPLENISHMENT_REQUEST);
      else if (ordType == DBConstants.CYCLECOUNT)
        mpMVData.setMoveCategory(DBConstants.CYCLECOUNT_REQUEST);
      else
        mpMVData.setMoveCategory(DBConstants.PICK_REQUEST);

      mpMVData.setMoveStatus(DBConstants.AVAILABLE);
                                       // Set the move priority to the order
                                       // priority.
      mpMVData.setPriority(ipOHData.getPriority());
      mpMVData.setMoveDate(new Date());
      mpMove.addElement(mpMVData);         // Add the move record.
    }
    else
    {
      String mesg = "";
      if (superLoad.equals(isAllocatedLoad))
      {
        mesg = "Load " + isAllocatedLoad + " is already being moved!";
      }
      else
      {
        mesg = "Parent Load " + superLoad + " of Load " + isAllocatedLoad;
        mesg += " is already being moved!";
      }
      throw new DBException(mesg);
    }
  }

 /**
  * Method builds move for an empty container request.
  * <i>NOTE: This method should be called from within a transaction</i>
  * @param ipOLData <code>OrderLineData</code> containing Order Line data.
  *                item and lot can be filled in for partially empty containers with a
  *                particular item or item/lot combination
  * @param isLoadID <code>String</code> containing id of allocated load.
  * @param inOrderPriority Order priority.
  * @param isRouteID <code>String</code> containing the routeID.
  * @param isLoadDevice The Load's device.
  * @param idEmptyAmount <code>double</code> specifying the amount of empty space on
  *         the container.
  *  @throws DBException if there is a database error
  *          adding the move.
  */
  public void buildEmptyContainerMove(OrderLineData ipOLData, String isLoadID,
                                      int inOrderPriority, String isRouteID,
                                      String isLoadDevice, double idEmptyAmount)
         throws DBException
  {
                                       // See if the parent load needs a move.
    if (loadNeedsMove(isLoadID, DBConstants.EMPTYMOVE))
    {
      mpMVData.clear();
      mpMVData.setMoveID(Math.abs(new Random().nextInt()));
      mpMVData.setParentLoad(isLoadID);
      mpMVData.setLoadID(isLoadID);
      mpMVData.setItem(ipOLData.getItem());         //partially empty container item.
      mpMVData.setPickLot(ipOLData.getOrderLot());  //partially empty container item lot.
      mpMVData.setOrderLot(ipOLData.getOrderLot()); //partially empty container item lot.
      mpMVData.setPickQuantity(idEmptyAmount);
      mpMVData.setPriority(inOrderPriority);
      mpMVData.setOrderID(ipOLData.getOrderID());
      mpMVData.setMoveType(DBConstants.EMPTYMOVE);
      mpMVData.setMoveCategory(DBConstants.PICK_REQUEST);
      mpMVData.setMoveStatus(DBConstants.AVAILABLE);
      mpMVData.setMoveDate(new Date());
      mpMVData.setRouteID(isRouteID);
      mpMVData.setDeviceID(isLoadDevice);

      mpMove.addElement(mpMVData);         // Add the move record.
    }
  }

  /**
   *  Builds Item move for each item detail allocated.  If there is still a
   *  move out there from a previous allocation, it's because it has not been
   *  picked.  In this case the move record is updated.
   *  <i>NOTE: This method should be called from within a transaction</i>
   *
   * @param ipOLData <code>OrderLineData</code> containing Order Line data.
   * @param ipIDData <code>LoadLineItemData</code> containing Item Detail data.
   * @param idPickQty <code>double</code> containing the pick quantity.
   * @param ipOHData <code>OrderHeaderData</code> containing Order Header data.
   * @param isRouteID <code>String</code> containing the routeID.
   * @param isLoadDeviceID The device ID of the Load.
   * @return -1 if the move was not added, and 0 if it was added successfully.
   * @throws DBException if there is a DB update/access error.
   */
  public int buildItemMove(OrderLineData ipOLData, LoadLineItemData ipIDData,
                           double idPickQty, OrderHeaderData ipOHData,
                           String isRouteID, String isLoadDeviceID) throws DBException
  {
    int rtn = -1;
    int ordType = ipOHData.getOrderType();

    if (loadNeedsMove(ipIDData.getLoadID(), ipIDData, ipOLData))
    {
      addItemMove(ipOHData, ipOLData, ipIDData, idPickQty, isRouteID, isLoadDeviceID);
      rtn = 0;
    }
    else if (ordType == DBConstants.ITEMORDER)
    {                                  // For Item orders we will update the move.
      mpMVData.clear();
      mpMVData.setKey(MoveData.ORDERID_NAME, ipOHData.getOrderID());
      mpMVData.setKey(MoveData.PARENTLOAD_NAME, ipIDData.getLoadID());
      mpMVData.setKey(MoveData.LOADID_NAME, ipIDData.getLoadID());
      mpMVData.setKey(MoveData.ITEM_NAME, ipIDData.getItem());
      mpMVData.setKey(MoveData.PICKLOT_NAME, ipIDData.getLot());
      mpMVData.setKey(MoveData.ORDERLOT_NAME, ipOLData.getOrderLot());
      mpMVData.setKey(MoveData.LINEID_NAME, ipOLData.getLineID());
      mpMVData.setKey(MoveData.POSITIONID_NAME,ipIDData.getPositionID());
      mpMVData.setKey(MoveData.MOVETYPE_NAME, Integer.valueOf(DBConstants.ITEMMOVE));
      MoveData lockedMove = mpMove.getElement(mpMVData, DBConstants.WRITELOCK);
      if (lockedMove != null)
      {
        mpMVData.setPickQuantity(lockedMove.getPickQuantity() + idPickQty);
        mpMove.modifyElement(mpMVData);
      }
      else                             // This should never happen, but it appears
      {                                // the move has disappeared on us!
        addItemMove(ipOHData, ipOLData, ipIDData, idPickQty, isRouteID,
                    isLoadDeviceID);
      }
      rtn = 0;
    }

    return(rtn);
  }

  /**
   *  <BR><B>TODO: Move this to StandardRouteServer
   *  </B><BR><BR>
   *  Method determines if a load can reach a particular output station, and
   *  determines the route a load can take based on either the Order Header
   *  destination (which should be the same name as a route name), or the order
   *  line route.
   *  <p>This method fills in the following pieces of data into the returning
   *  <code>Map</code>.&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
   *  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
   *  <ul><li>ParameterNameConstants.STATION -- The output station this load should go to.</li>
   *      <li>ParameterNameConstants.LOCATION -- The Loads current Location.</li>
   *      <li>ParameterNameConstants.ROUTEID  -- The route this load needs to be on.</li>
   *      <li>ParameterNameConstants.DEVICEID -- The Device this load belongs to.</li>
   *  </ul>
   *  </p>
   *  @param isLoadID <code>String</code> containing load for which to get routing
   *         information.
   *  @param inDestAisleGroup Aisle group of the station requesting work.
   *  @return <code>null</code> if data can't be filled in due to some type of error.
   *          <code>Map</code> of data otherwise.
   */
  public Map<String, String> getLoadOutputStation(String isLoadID,
      String isRequestingStation, String isOrderDestStation,
      int inDestAisleGroup) throws DBException
  {
    initializeLoadServer();
    initializeLocationServer();
    initializeRouteServer();
    initializeStationServer();

    String vsLoadLocation = mpLoadServer.getLoadLocation(isLoadID);
    LocationData vpLoadLocData = mpLocServer.getLocationRecord(vsLoadLocation);
    String vsLoadDevice = vpLoadLocData.getDeviceID();

    /*
     * Get the route (probably same name as sOrderDestStation)
     */
    String vsRoute = mpRouteServer.getFromToRoute(vpLoadLocData.getWarehouse(),
        vpLoadLocData.getAddress(), mpStationServer.getStationWarehouse(isOrderDestStation),
        isOrderDestStation);

    if (vsRoute == null)
    {
      throw new DBException("No route from " + vpLoadLocData.getWarehouse()
          + "-" + vpLoadLocData.getAddress() + " to " + isOrderDestStation
          + " for Load "+isLoadID );
    }

    /*
     * If the aisle group of the current load location doesn't match the aisle
     * group of the requesting station, find the the output station this load
     * can go to.
     */
    String vsFirstOutputStation;
    if (vpLoadLocData.getAisleGroup() == inDestAisleGroup)
    {
      vsFirstOutputStation = isRequestingStation;
    }
    else
    {
      /*
       * Get the stations by aisle group.  If there are multiple stations in
       * this aisle group, take the first output station on the route.
       */
//      vsFirstOutputStation = "";
//      Station vpStation = Factory.create(Station.class);
//      String[] vasStations = vpStation.getRetvStationsByAislegroup(vpLoadLocData.getAisleGroup());
//      if (vasStations.length == 0)
//      {
//        return(null);
//      }
//
//      String vsNextRouteStation = mpRouteServer.getNextRouteDest(vsRoute, vsLoadDevice);
//      for (String s : vasStations)
//      {
//        if (s.equals(vsNextRouteStation))
//        {
//          vsFirstOutputStation = s;
//          break;
//        }
//      }
      vsFirstOutputStation = mpRouteServer.getNextRouteDest(vsRoute, vsLoadDevice);
    }

    Map<String, String> hMap = new HashMap<String, String>();
    hMap.put(ParameterNameConstants.ROUTEID, vsRoute);
    hMap.put(ParameterNameConstants.LOCATION, vsLoadLocation);
    hMap.put(ParameterNameConstants.STATIONNAME, vsFirstOutputStation);
    hMap.put(ParameterNameConstants.DEVICEID, vsLoadDevice);

    return(hMap);
  }

  /**
   * Checks if a load is stored in the rack and can reach a destination.
   *
   * @param isLoadID <code>String</code> identifier of load being checked.
   * @param isFinalStation indicates requesting station's aisle group.
   * @param izCheckRetrievePend checks Retrieve Pending loads also if set to
   *          <code>true</code>.
   * @return <code>boolean</code> of <code>true</code> if load is in this aisle
   *         group.
   * @throws DBException if there is a DB access error.
   */
  public boolean isLoadAllocatable(String isLoadID, String isFinalStation,
      boolean izCheckRetrievePend) throws DBException
  {
    return (mpTJ.isLoadAllocatable(isLoadID, isFinalStation,
                                        izCheckRetrievePend));
  }

 /**
  * Method does a quick check to see if there is sufficient inventory in the
  * warehouse that can fill an order. <b>Note:</b> this is not dependent on the
  * Location of the load since we want the ability to allocate from a load at a
  * station also.
  * @param isOrderID the order id.
  * @param isOrdDest the order destination station.
  * @return <code>true</code> if all order lines can be filled.
  * @throws DBException if there is a DB access error.
  */
  public boolean itemOrderHasSufficientInventory(String isOrderID, String isOrdDest)
         throws DBException
  {
    boolean vzRtn = false;

    List<Map> vpList = orderLine.getOutstandingOrderLinesByOrderId(isOrderID);
    for(Map vpRowMap : vpList)
    {
      String vsItem = (String)vpRowMap.get(OrderLineData.ITEM_NAME);
      String vsOrdLot = (String)vpRowMap.get(OrderLineData.ORDERLOT_NAME);
      vzRtn = mpTJ.orderLineHasSufficientInventory(isOrderID, vsItem, vsOrdLot,
                                                   isOrdDest);
      if (!vzRtn) break;
    }

    return(vzRtn);
  }

  /**
   * Checks if a load can be retrieved.
   *
   * @param isLoadID <code>String</code> Load being checked in the rack.
   * @param isOrderID the Load Order ID.
   * @param izCheckRetrievePend <code>boolean</code> to indicate if a load that
   *          has been marked for retrieval should be counted also. A value of
   *          <code>true</code> means it will be counted.
   * @return <code>null</code> if load is in the rack.  Non-null message
   *         otherwise.
   */
  public String isOrderedLoadInRack(String isLoadID, String isOrderID,
                                       boolean izCheckRetrievePend)
  {
    initializeLocationServer();
    
    String vsMessage;
    int vnLoadStat;

    try
    {
      /*
       * Make sure that the load exists
       */
      LoadData vpLoadData = load.getLoadData(isLoadID);
      if (vpLoadData == null)
      {
        vsMessage = "Load " + isLoadID + " for Order " + isOrderID
            + " does not exist. Product not allocated.";
        logError(vsMessage);
        return vsMessage;
      }

      /*
       * We need to make sure that the load is in an AVAILABLE location.
       */
      if (mpLocServer.getLocationStatusValue(vpLoadData.getWarehouse(),
          vpLoadData.getAddress(), vpLoadData.getShelfPosition()) != DBConstants.LCAVAIL)
      {
        vsMessage = "Load " + isLoadID + " for Order " + isOrderID
            + " is in an unavailable location ("
            + mpLocation.describeLocation(vpLoadData.getWarehouse(),
                vpLoadData.getAddress()) + "). Product not allocated.";
        logError(vsMessage);
        return vsMessage;
      }

      vnLoadStat = vpLoadData.getLoadMoveStatus();
/*===========================================================================
    This is the load with the oldest product. Regardless of whether it's in
    our aisle group, we will have to move onto the next order until the load
    either comes back into the rack or is deleted from the system (if it's
    deleted from the system, we will presumably find the next oldest item detail
    in another load on the next try).
  ===========================================================================*/
      if (izCheckRetrievePend)
      {
        if (vnLoadStat != DBConstants.NOMOVE && vnLoadStat != DBConstants.RETRIEVEPENDING)
        {
          vsMessage = "Load " + isLoadID + " for Order " + isOrderID
              + " is at " + "a station. Product not allocated.";
          logOperation(vsMessage);
          return vsMessage;
        }
      }
      else
      {
        if (vnLoadStat != DBConstants.NOMOVE)
        {
          vsMessage = "Load " + isLoadID + " for Order " + isOrderID + " is at "
              + "a station or already scheduled for a station. Product "
              + "not allocated.";
          logOperation(vsMessage);
          return vsMessage;
        }
      }
    }
    catch (DBException exc)
    {
      vsMessage = "Exception getting load status for load " + isLoadID;
      logException(exc, vsMessage);
      return vsMessage;
    }

    return null;
  }

 /**
  * Method to check if any Load Order Lines need to be allocated for this JVM.
  * @param isOrderId the order id.
  * @return <code>true</code> if this JVM has any Load Order Lines that still need
  *         to be allocated.
  * @throws DBException if there is a database access error.
  */
  public boolean anyLoadOrderLinesToAllocate(String isOrderId) throws DBException
  {
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
    return(mpTJ.anyLoadOrderLinesToAllocate(isOrderId, vsJVMId));
  }

 /**
  * Method gets all order lines for a given Order for which this JVM is
  * responsible.
  * @param isOrderID the order id.
  * @return List of order lines that can be allocaed for this JVM.
  * @throws DBException if there is a database access error.
  */
  public List<Map> getOrderLinesUnderThisJVM(String isOrderID) throws DBException
  {
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
    return(mpTJ.getOrderLinesUnderThisJVM(isOrderID, vsJVMId));
  }

 /**
  * Changes the order status of an order that was being allocated back to its
  * original status.  This method is normally called when no allocations could
  * occur, or else there was an error.
  * @param isOrderID <code>String</code> containing order id.
  * @param isOptionalMessage an optional explanation string of why order status
  *        is being reversed.
  * @param inOriginalOrderStatus  <code>int</code> containing the original order
  *        status prior to allocation.
  */
  public void revertOrderStatus(String isOrderID, String isOptionalMessage,
                                int inOriginalOrderStatus)
  {
    initializeOrderServer();

    try
    {
      if (isOptionalMessage == null || isOptionalMessage.isEmpty())
      {
        mpOrderServer.setOrderStatusValue(isOrderID, inOriginalOrderStatus);
      }
      else
      {
        mpOHData.clear();
        mpOHData.setKey(OrderHeaderData.ORDERID_NAME, isOrderID);
        mpOHData.setOrderStatus(inOriginalOrderStatus);
        mpOHData.setOrderMessage(isOptionalMessage);
        mpOrderServer.modifyOrderHeader(mpOHData);
      }
    }
    catch(DBException e)
    {
      logError(e.getMessage() + "Setting order status. Order " + isOrderID +
               " not allocated.");
    }
  }

  /**
   *  Updates the order line allocated quantity.  <i>This method should be
   *  called from within a transaction</i>
   *
   *  @param olData <code>OrderLineData</code> containing order line data.
   *  @param amountShy <code>double</code> containing the amount this order line
   *         still needs to be fulfilled.
   */
  public void updateOrderLine(OrderLineData olData, double amountShy)
          throws DBException
  {
    /*
     * This olData is not read with a writelock.
     * Changes were made 18-Feb-2007 to not clobber other values, but it is
     * still possible to screw up if this line is short-picked while it is
     * being reallocated.  Ideally, the caller should read the order line
     * with a writelock to ensure that its values are not changed while it
     * is within the transaction.
     * TODO: Fix the problem described above.
     */
    OrderLineData vpOLD = Factory.create(OrderLineData.class);

    double newAllocQty = olData.getOrderQuantity() - amountShy;
                                       // Update the Order Line allocated
    vpOLD.setKey(OrderLineData.ORDERID_NAME, olData.getOrderID());
    vpOLD.setKey(OrderLineData.ITEM_NAME, olData.getItem());
    vpOLD.setKey(OrderLineData.ORDERLOT_NAME, olData.getOrderLot());
    vpOLD.setKey(OrderLineData.LINEID_NAME, olData.getLineID());
    vpOLD.setAllocatedQuantity(newAllocQty);
    if (amountShy == 0.0) vpOLD.setLineShy(DBConstants.NO);

    try
    {
      orderLine.modifyElement(vpOLD);
    }
    catch(NoSuchElementException e)
    {
      throw new DBException(e.getMessage());
    }
  }

 /**
  *  Updates the order line allocated quantity to 1 for a load order.  <i>This
  *  method should be called from within a transaction</i>
  *  @param sOrderID <code>String</code> containing order id.
  *  @param sLoadID <code>String</code> containing load being ordered out.
  *  @throws DBException
  */
  public void updateOrderLine(String sOrderID, String sLoadID)
         throws DBException
  {
    oldata.clear();
    oldata.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    oldata.setKey(OrderLineData.LOADID_NAME, sLoadID);
    try
    {
      oldata.setAllocatedQuantity(1);
      orderLine.modifyElement(oldata);
    }
    catch(NoSuchElementException e)
    {
      throw new DBException(e.getMessage());
    }
  }

 /**
  * Checks and marks a short order as SHORT, or HOLD, or deletes it.
  * @param sOrderID the order id. of the order to check.
  * @return <code>boolean</code> of true if the order status ends up being changed
  *         here. false otherwise.
  * @throws DBException for database update or connection errors.
  */
  public boolean checkOrderForShortage(String sOrderID) throws DBException
  {
    initializeOrderServer();

    boolean orderStatusChanged = false;

    if (!mpOrderServer.isOrderFullyAllocated(sOrderID))
    {
      if (gpShortOrderProcess != null)
      {
        if (gpShortOrderProcess.equals("SHORT"))
        {
          mpOrderServer.setOrderStatusValue(sOrderID, DBConstants.SHORT);
          orderStatusChanged = true;
        }
        else if (gpShortOrderProcess.equals("HOLD"))
        {
          mpOrderServer.holdOrder(sOrderID);
          orderStatusChanged = true;
          String str = "Holding Order-->'" + sOrderID +
                       "' due to ShortOrderProcess flag setting...";
          logDebug(str);
          logDiagnostic("StandardAllocationServer.checkOrderForShortage", str);
        }
        else if (gpShortOrderProcess.equals("DELETE"))
        {
          mpOrderServer.deleteOrder(sOrderID);
          orderStatusChanged = true;
          String str = "Deleting Order-->'" + sOrderID + "' due to ShortOrderProcess flag setting...";
          logDebug(str);
          logDiagnostic("StandardAllocationServer.checkOrderForShortage", str);
        }
      }
      else
      {
        mpOrderServer.setOrderStatusValue(sOrderID, DBConstants.SHORT);
        orderStatusChanged = true;
      }
    }

    return(orderStatusChanged);
  }

 /**
  * Build info. for scheduler so that it can take the super-load to the desti-
  * nation station only if it has never received an allocate load message
  * before for this load.
  *
  * @param loadID <code>String</code> containing load that has allocations.
  * @param outputStation <code>String</code> containing station that is load's
  *        first stop.
  * @param allocatedDataList <code>List</code> reference to allocated load list.
  * @throws DBException when there is a load lookup
  *         error.
  */
  public void buildReturnData(String loadID, String outputStation,
                              List<AllocationMessageDataFormat> allocatedDataList) throws DBException
  {
    LoadData ldData = load.getLoadData(loadID);
    if (ldData.getLoadMoveStatus() == DBConstants.NOMOVE)
    {
      if (!inLoadList(loadID, allocatedDataList))
      {
        AllocationMessageDataFormat allocData = new AllocationMessageDataFormat();
        allocData.setOutBoundLoad(loadID);
        allocData.setFromWarehouse(ldData.getWarehouse());
        allocData.setFromAddress(ldData.getAddress());
        allocData.setOutputStation(outputStation);
        allocData.createDataString();

        allocatedDataList.add(allocData);
      }
    }
  }

  /**
   * Remove a load from the return data
   *
   * @param isLoadID
   * @param ipAllocatedDataList
   */
  public void cancelReturnData(String isLoadID, List<AllocationMessageDataFormat> ipAllocatedDataList)
  {
    AllocationMessageDataFormat allocList = null;

    for(int idx = 0; idx < ipAllocatedDataList.size(); idx++)
    {
      allocList = ipAllocatedDataList.get(idx);
      if (isLoadID.equals(allocList.getOutBoundLoad()))
      {
        ipAllocatedDataList.remove(idx);
        break;
      }
    }
  }


 /**
  *  Method updates the load's next location using the output station as the
  *  address.
  *  @param loadID <code>String</code> containing id. of load being updated.
  *  @param outputStation <code>String</code> containing output station name.
  *  @throws DBException if there is an update error.
  */
  public void updateLoadNextLocation(String loadID, String outputStation)
         throws DBException
  {
    initializeLoadServer();

    String nextDestWarehouse = station.getStationWarehouse(outputStation);
    mpLoadServer.updateLoadNextLocation(loadID, nextDestWarehouse, outputStation);
  }

 /**
  *  Method updates the next location field for a load on a load order.
  *  @param sLoadid <code>String</code> containing id. of load being updated.
  *  @param sOutputStation <code>String</code> containing output station name.
  *  @throws DBException if there is an update error.
  */
  public void updateOrderLoadNextLocation(String sLoadid, String sOutputStation)
         throws DBException
  {
    initializeLoadServer();

    String nextDestWarehouse = station.getStationWarehouse(sOutputStation);
    lddata.setKey(LoadData.LOADID_NAME, sLoadid);
    lddata.setNextWarehouse(nextDestWarehouse);
    lddata.setNextAddress(sOutputStation);
    mpLoadServer.updateLoadData(lddata, true);
  }

  @Override
  public void cleanUp()
  {
    if (mpDedServer != null)
    {
      mpDedServer.cleanUp();
      mpDedServer = null;
    }
    if (mpLoadServer != null)
    {
      mpLoadServer.cleanUp();
      mpLoadServer = null;
    }
    if (mpLocServer != null)
    {
      mpLocServer.cleanUp();
      mpLocServer = null;
    }
    if (mpOrderServer != null)
    {
      mpOrderServer.cleanUp();
      mpOrderServer = null;
    }
    if (mpRouteServer != null)
    {
      mpRouteServer.cleanUp();
      mpRouteServer = null;
    }
    if (mpStationServer != null)
    {
      mpStationServer.cleanUp();
      mpStationServer = null;
    }
  }

  protected void addItemMove(OrderHeaderData ipOHData, OrderLineData ipOLData,
                             LoadLineItemData ipIDData, double idPickQty,
                             String sRouteID, String isLoadDevice) throws DBException
  {
    mpMVData.clear();

    mpMVData.setMoveID(Math.abs(new Random().nextInt()));
    mpMVData.setParentLoad(ipIDData.getLoadID());
    mpMVData.setLoadID(ipIDData.getLoadID());
    mpMVData.setItem(ipIDData.getItem());
    mpMVData.setPickLot(ipIDData.getLot());
    mpMVData.setOrderLot(ipOLData.getOrderLot());
    mpMVData.setLineID(ipOLData.getLineID());
    mpMVData.setPositionID(ipIDData.getPositionID());
    if (ipOHData.getOrderType() != DBConstants.CYCLECOUNT)
    {
      mpMVData.setPickQuantity(idPickQty);
    }
    mpMVData.setPriority(ipOHData.getPriority());
    mpMVData.setOrderID(ipOLData.getOrderID());
    mpMVData.setMoveType(DBConstants.ITEMMOVE);

    if (ipOHData.getOrderType() == DBConstants.REPLENISHMENT)
    {
      mpMVData.setMoveCategory(DBConstants.REPLENISHMENT_REQUEST);
      mpMVData.setMoveType(DBConstants.REPLENISHMENTMOVE);
      mpMVData.setPickQuantity(idPickQty);
    }
    else if (ipOHData.getOrderType() == DBConstants.CYCLECOUNT)
    {
      mpMVData.setMoveCategory(DBConstants.CYCLECOUNT_REQUEST);
      mpMVData.setMoveType(DBConstants.CYCLECOUNTMOVE);
    }
    else
    {
      mpMVData.setMoveCategory(DBConstants.PICK_REQUEST);
      mpMVData.setMoveType(DBConstants.ITEMMOVE);
      mpMVData.setPickQuantity(idPickQty);
    }

    mpMVData.setMoveStatus(DBConstants.AVAILABLE);
    mpMVData.setRouteID(sRouteID);
    mpMVData.setDeviceID(isLoadDevice);

    /* The following assures that there will be a move date difference
     * *always*.  This is critical since we sort by this date when finding
     * Retrieve Pending loads. (The Move Date is defaulted to the record add time
     * in the database).
     */
    try
    {
      Thread.sleep(10);
    }
    catch(InterruptedException ex)
    {
    }

    mpMove.addElement(mpMVData);
  }

  /**
   * Method to update Load to RETRIEVEPENDING after allocation.
   * @param isLoadID the load to mark RETIEVEPENDING
   * @throws DBException if there is a Load modify error.
   */
  public void changeLoadToRetrievePending(String isLoadID) throws DBException
  {
    initializeLoadServer();

    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      LoadData vpLoadData = mpLoadServer.getLoad(isLoadID, true);
      if (vpLoadData.getLoadMoveStatus() == DBConstants.NOMOVE)
      {
        vpLoadData.clear();
        vpLoadData.setKey(LoadData.LOADID_NAME, isLoadID);
        vpLoadData.setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
        vpLoadData.setMoveDate();
        load.modifyElement(vpLoadData);

        logOperation(LogConsts.OPR_DEVICE, "LoadId \"" + isLoadID
            + "\" - Allocated: Status: Retrieve Pending");
      }
      commitTransaction(vpToken);
    }
    catch (Exception e)
    {
      throw new DBException("Error updating Load " + isLoadID
          + " to RETRIEVEPENDING...", e);
    }
    finally
    {
      endTransaction(vpToken);
    }
  }


/*============================================================================
                      All private methods go in this section.
  ============================================================================*/
 /**
  *  Method checks if a particular load is contained in the list of already
  *  allocated loads.  This list is sent back to the allocation controller so
  *  that it can send the scheduler a set of events stating that these loads
  *  have been allocated.
  *  @param loadid <code>String</code> containing load that is being checked.
  *  @param allocatedDataList <code>List</code> reference to allocated load list.
  *
  *  @return <code>boolean</code> value of <code>true</code> if the current load
  *          is found in the list.
  */
  protected boolean inLoadList(String loadid, List<AllocationMessageDataFormat> allocatedDataList)
  {
    boolean rtn = false;
    AllocationMessageDataFormat allocList = null;

    for(int idx = 0; idx < allocatedDataList.size(); idx++)
    {
      allocList = allocatedDataList.get(idx);
      if (loadid.equals(allocList.getOutBoundLoad()))
      {
        rtn = true;
        break;
      }
    }
    return(rtn);
  }

 /**
  * This method exists for allowing any project specific customisation of order
  * line selection by the allocator.  This method is only for item orders.
  * @param isOrderID
  * @return
  * @throws DBException
  */
  public List<Map> getAllocatableItemOrderLines(String isOrderID)
         throws DBException
  {
    return(mpOrderServer.getOrderLineData(isOrderID));
  }

 /**
  *  Method checks if there is sufficient amount in a list of item details that
  *  can be used to fill the order line.
  *
  *  @param itemDetailList <code>List</code> containing a set of item details.
  *  @param neededQuantity <code>String</code> containing the needed amount to
  *         fill the order line.
  *
  *  @return <code>boolean</code> of true if the there is sufficient quantity
  *          in the item detail list. false otherwise.
  */
  protected boolean isSufficientAmountAvail(List<Map> itemDetailList, double neededQuantity)
          throws DBException
  {
    double availAmount = 0, tmpqty = 0;
    int listLength = itemDetailList.size();

    for(int idx = 0; idx < listLength; idx++)
    {
      tmpqty = DBHelper.getDoubleField(itemDetailList.get(idx), "FCURRENTQUANTITY") -
               DBHelper.getDoubleField(itemDetailList.get(idx), "FALLOCATEDQUANTITY");
      availAmount += tmpqty;
      if (availAmount >= neededQuantity) break;
    }

    return(neededQuantity <= availAmount);
  }

  protected void logDiagnostic(String sMethodName, String sDiagMessage)
  {
    if (gpAllocationProbe != null)
      gpAllocationProbe.addProbeDetails(sMethodName, sDiagMessage);
  }

  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/
  protected void initializeDedicationServer()
  {
    if (mpDedServer == null)
    {
      mpDedServer = Factory.create(StandardDedicationServer.class,
                                   getClass().getSimpleName());
    }
  }

  protected void initializeLoadServer()
  {
    if (mpLoadServer == null)
    {
      mpLoadServer = Factory.create(StandardLoadServer.class,
                                    getClass().getSimpleName());
    }
  }

  protected void initializeLocationServer()
  {
    if (mpLocServer == null)
    {
      mpLocServer = Factory.create(StandardLocationServer.class,
                                   getClass().getSimpleName());
    }
  }

  protected void initializeOrderServer()
  {
    if (mpOrderServer == null)
    {
      mpOrderServer = Factory.create(StandardOrderServer.class,
                                     getClass().getSimpleName());
    }
  }

  protected void initializeRouteServer()
  {
    if (mpRouteServer == null)
    {
      mpRouteServer = Factory.create(StandardRouteServer.class,
                                     getClass().getSimpleName());
    }
  }

  protected void initializeStationServer()
  {
    if (mpStationServer == null)
    {
      mpStationServer = Factory.create(StandardStationServer.class,
                                       getClass().getSimpleName());
    }
  }

  protected void initializeMoveServer()
  {
    if (mpMoveServer == null)
    {
      mpMoveServer = Factory.create(StandardMoveServer.class,
                                    getClass().getSimpleName());
    }
  }

  /**
   * Method to evaluate the LoadMoveStatus of the Parent load associated with
   * the given load and return the new Move Status should it be changed.
   *
   * @param isLoadId <code>String</code> the Load ID
   * @param isOrderId <code>String</code> the Order ID
   * @param inStatus <code>Int</code> the preferred Move Status
   * @throws DBException if there is a serious DB error.
   */
  protected int evalMoveStatus(String isLoadId, String isOrderId, int inStatus) throws DBException
  {
    int vnNewStatus = inStatus;

    Load vpLoad = Factory.create(Load.class);
    String vsParentLoadId = vpLoad.getParentLoadID(isLoadId);
    LoadData vpParentLoadData = vpLoad.getLoadData(vsParentLoadId);

    if (vpParentLoadData != null)
    {
      String vsWarehouse = vpParentLoadData.getWarehouse();
      String vsAddress = vpParentLoadData.getAddress();

      int vnLoadMoveStatus = vpParentLoadData.getLoadMoveStatus();

      // Base on Load Move Status of Parent Load --
      switch (vnLoadMoveStatus)
      {
      case DBConstants.RETRIEVING:
      case DBConstants.RETRIEVESENT:
        vnNewStatus = DBConstants.ASSIGNED;
        break;
      case DBConstants.MOVING:
          // Parent load is moving, check where it is going.
        int vnFromLocType =
              mpLocServer.getLocationTypeValue(vsWarehouse, vsAddress);
        int vnToLocType =
              mpLocServer.getLocationTypeValue(vpParentLoadData.getNextWarehouse(),
                                               vpParentLoadData.getNextAddress());
          // Load is moving from a ASRS location to a non-ASRS location.
          // It is an out-bound load.
        if (vnFromLocType == DBConstants.LCASRS && vnToLocType != DBConstants.LCASRS)
        {
          vnNewStatus = DBConstants.ASSIGNED;
        }
        break;
      case DBConstants.ARRIVED:
        // Parent load has arrived at the station.
        // If it is at the destination of the Item order, set Move Status to
        // be ASSIGNED. Otherwise, leave it along.
        initializeOrderServer();
        OrderHeaderData vpOHData = mpOrderServer.getOrderHeaderRecord(isOrderId);
        if (vpOHData.getOrderType() == DBConstants.ITEMORDER)
        {
          // Get the destination station from Order Header record
          initializeStationServer();
          String vsDestStation = vpOHData.getDestinationStation();
          String vsDestWarehouse = mpStationServer.getStationWarehouse(vsDestStation);

          // Check if the load is at the destination of the order
          if (vsWarehouse.equals(vsDestWarehouse) && vsAddress.equals(vsDestStation))
          {
            vnNewStatus = DBConstants.ASSIGNED;
          }
        }
        break;
      }
    }
    return vnNewStatus;
  }
}

