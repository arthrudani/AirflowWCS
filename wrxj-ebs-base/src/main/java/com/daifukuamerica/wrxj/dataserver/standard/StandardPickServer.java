package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wms.printer.barcode.LabelGeneratorException;
import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderProcessor;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
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
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.printer.barcode.PickLabel;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A server that provides methods and transactions for use in picking
 * operations. Methods used to release loads from End of Aisle stations,
 * to complete item picks, load picks, and empty container pick requests
 * are provided. Transactions are wrapped around calls to the lower
 * level data base objects.
 *
 *
 * Title:        StandardPickServer
 * Description:
 * Copyright:    Copyright (c) 2003-2008
 * Company:      Daifuku America Corporation
 *
 * @author avt
 * @version 1.0
 */
public class StandardPickServer extends StandardServer
{
  private final MoveData mpMVData = Factory.create(MoveData.class);
  private final Move mpMV = Factory.create(Move.class);
  protected ShortOrderProcessor mpShortOrderProcess = Factory.create(ShortOrderProcessor.class);
  
  // Pick Label re-print support
  PickLabelInfo mpLastPickInfo = null;
  
  // Commonly used servers
  protected StandardOrderServer      mpOrderServer = null;
  protected StandardAllocationServer mpAllocServ   = null;
  protected StandardHostServer       mpHostServ    = null;
  
  // Public Methods for Pick Server
  public StandardPickServer()
  {
    this(null);
  }

  public  StandardPickServer(String keyName)
  {
    super(keyName);
  }
  
  /**
   *  Shuts down this controller by cancelling any timers and shutting down the
   *  Equipment.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
      
    if (mpOrderServer != null)
    {
      mpOrderServer.cleanUp();
      mpOrderServer = null;
    }
  }

  /**
   *  Method to complete a move for a Load pick.
   *
   *  @param userID User ID.
   *  @param moveData Move data object to be completed.
   *  @param captive Specifies if pick was performed at a Captive location.
   *  @param deleteInvt Specifies if pick was performed at a location where
   *  inventory is to be deleted.
   *  @exception DBException
   */
  public void completeLoadPick(String userID, MoveData moveData,
      boolean captive, boolean deleteInvt) throws DBException
  {
    completeLoadPick(userID, moveData, captive, deleteInvt, null);
  }

  /**
   *  Method to complete a move for a Load pick.
   *
   *  @param userID User ID.
   *  @param moveData Move data object to be completed.
   *  @param captive Specifies if pick was performed at a Captive location.
   *  @param deleteInvt Specifies if pick was performed at a location where
   *  inventory is to be deleted.
   *  @param toLoad Load ID the inventory is being transferred to.
   *  @exception DBException
   */
  public void completeLoadPick(String userID, MoveData moveData,
      boolean captive, boolean deleteInvt, String toLoad) throws DBException
  {
    boolean conventionalPick = false;
    // delete/move all items on this load, add transaction history for each item
    // if toLoad is blank then we delete the items
    logDebug("LoadId \"" + moveData.getParentLoad() + "\" - User: " + userID + " Completing MoveID: " + moveData.getMoveID());
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      // have fun here
      MoveData mvdataSearch = Factory.create(MoveData.class);
      mvdataSearch.setKey(MoveData.MOVEID_NAME, Integer.valueOf(moveData.getMoveID()));
      MoveData mvdata = Factory.create(Move.class).getElement(mvdataSearch, DBConstants.WRITELOCK);

      if (deleteInvt)
      {
        pickLoadToDeletion (mvdata.getLoadID(), mvdata.getOrderID(), captive, toLoad);
        if (mvdata.getMoveType() == DBConstants.LOADMOVE)
        {
          adjustOrderLineForLoad(mvdata);
        }
        else
        {
          adjustOrderLine(mvdata, mvdata.getPickQuantity());
        }
      }
      else if (captive && !SKDCUtility.isFilledIn(toLoad))
      {
        DBHelper.dbThrow("To load is required");
      }
      else if (!captive && !SKDCUtility.isFilledIn(toLoad))
      {
        // get the next location in this moves route
        LoadData lddataFrom = getLoad(mvdata.getLoadID(), DBConstants.WRITELOCK);
        String address = getNextAddressOnRouteForMove(mvdata, lddataFrom.getAddress());
        if (!SKDCUtility.isFilledIn(address))
        {
          // no route when we need it
          DBHelper.dbThrow("No routing for this move");
        }
        String warehouse = getStationsWarehouse(address);
        pickLoad(mvdata.getLoadID(), mvdata, warehouse, address, null);
      }
      else
      {
        // get the next location in this moves route
        LoadData lddataFrom = getLoad(mvdata.getLoadID(), DBConstants.WRITELOCK);
        String address = getNextAddressOnRouteForMove(mvdata, lddataFrom.getAddress());
        if (!SKDCUtility.isFilledIn(address))
        {
          // no route when we need it
          DBHelper.dbThrow("No routing for this move");
        }
        String warehouse = getStationsWarehouse(address);
        if (!SKDCUtility.isFilledIn(warehouse))
        {
          // no route when we need it
          DBHelper.dbThrow("Invalid routing for this move");
        }

        // check the to load
        // if it exists it better be at the next location
        // if it does not exist then add it at the next location
        LoadData lddataTo = getLoad(toLoad, DBConstants.WRITELOCK);
        if (lddataTo != null)
        {
          // is it in the right place?
          if ((!lddataTo.getWarehouse().equals(warehouse)) ||
              (!lddataTo.getAddress().equals(address)))
          {
            // some wrong place
            DBHelper.dbThrow("To load is in the wrong location");
          }
          else if (!lddataTo.getRouteID().equals(mvdata.getRouteID()))
          {
            // load on wrong route
            DBHelper.dbThrow("To load is on a different route");
          }
        }
        else
        {
          // add the load
          addLoad(mvdata, toLoad, warehouse, address, conventionalPick);
        }

        if (!SKDCUtility.isFilledIn(toLoad))
        {
          pickLoad(mvdata.getLoadID(), mvdata, warehouse, address, toLoad);
        }
        else
        {
          pickInvt (mvdata, toLoad);
        }
        MoveData orderMove = adjustMove(moveData, 1);
        adjustOrderLineForLoad(orderMove);
      }
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      DBHelper.dbThrow(e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to actually complete a move for an Item pick.
   * 
   * @param isUserID User ID.
   * @param ipMoveData Move data object to be completed.
   * @param isToLoad Load ID the inventory is being transferred to.
   * @param izDeleteInvt Specifies if pick was performed at a location where
   *            inventory is to be deleted.
   * @param ifQtyPicked Specifies the quantity that was picked.
   * @exception DBException
   */
  public void completeItemPick(String isUserID, MoveData ipMoveData,
      String isToLoad, boolean izDeleteInvt, double ifQtyPicked,
      String isReasonCode) throws DBException
  {
    logDebug("User: " + isUserID + " completing move ID: " +
            ipMoveData.getMoveID() + " on load: " + ipMoveData.getParentLoad());
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      if (ifQtyPicked > 0)
      {
        MoveData itemMove = adjustMove(ipMoveData, ifQtyPicked);
        adjustLoadLineItems(itemMove, ifQtyPicked, izDeleteInvt, isToLoad,
                            isReasonCode);  
        if(mzHasHostSystem)
        {
          initializeHostServer();
          mpHostServ.sendPickComplete(ipMoveData, ifQtyPicked);
        }
        adjustOrderLine(itemMove, ifQtyPicked);
        
        commitTransaction(vpTok);
      }
    }
    catch(DBException e)
    {
      logException("Error Completing Pick for Item: " + ipMoveData.getItem() +
                   " To Load: " + isToLoad, e);
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
  
  /**
   * Method to complete a move for an Item pick.
   * 
   * @param isUserID User ID.
   * @param ipMoveData Move data object to be completed.
   * @param isToLoad Load ID the inventory is being transferred to.
   * @param izDeleteInv Specifies if pick was performed at a location where
   *            inventory is to be deleted.
   * @param idQtyPicked Specifies the quantity that was picked.
   * @param izZeroQtyRemaining true if load line item quantity is zero
   * @param izReallocate true if reallocation is desired
   * @param isPrinterName
   * @exception DBException
   */
  public void completeItemPick(String isUserID, MoveData ipMoveData,
      String isToLoad, boolean izDeleteInv, double idQtyPicked,
      boolean izZeroQtyRemaining, boolean izReallocate, String isPrinterName)
      throws DBException
  {
    boolean vzAllocate = false;
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      LoadLineItemData vpLLIData = null;
            
      if (izZeroQtyRemaining)
      {
        LoadLineItem vpLoadLineItem = Factory.create(LoadLineItem.class);
        vpLLIData = vpLoadLineItem.getLoadLineItemData(ipMoveData.getItem(),
            ipMoveData.getPickLot(), ipMoveData.getLoadID(), null, null, null,
            ipMoveData.getPositionID());
        if (!izReallocate)
        {    
          // back off ordered and allocated qty
          OrderLine vpOrderLine = Factory.create(OrderLine.class);
          OrderLineData vpOLKey = Factory.create(OrderLineData.class);
          // get the order line so we can back off ordered qty
          vpOLKey.setKey(OrderLineData.ORDERID_NAME, ipMoveData.getOrderID());
          vpOLKey.setKey(OrderLineData.ITEM_NAME, ipMoveData.getItem());
          vpOLKey.setKey(OrderLineData.ORDERLOT_NAME, ipMoveData.getOrderLot());
          vpOLKey.setKey(OrderLineData.LINEID_NAME, ipMoveData.getLineID());
          OrderLineData vpOLData = vpOrderLine.getElement(vpOLKey, DBConstants.WRITELOCK);
          if (vpOLData == null)
          {
            // can't find line item record
            DBHelper.dbThrow("Unable to find Order Line record for order: "
              + ipMoveData.getOrderID() + 
              ", item: " + ipMoveData.getItem() + 
              ", lot: " + ipMoveData.getOrderLot() +
              ", LineID "  + ipMoveData.getLineID());
          }
          vpOLData.setOrderQuantity(vpOLData.getOrderQuantity()
              - (ipMoveData.getPickQuantity() - idQtyPicked));
          vpOLData.setKey(OrderLineData.ORDERID_NAME, ipMoveData.getOrderID());
          vpOLData.setKey(OrderLineData.ITEM_NAME, ipMoveData.getItem());
          vpOLData.setKey(OrderLineData.ORDERLOT_NAME, ipMoveData.getOrderLot());
          vpOLData.setKey(OrderLineData.LINEID_NAME, ipMoveData.getLineID());
          vpOrderLine.modifyElement(vpOLData);
        }
      }
      
      completeItemPick(isUserID, ipMoveData, isToLoad, izDeleteInv, idQtyPicked, "");
      
      initializeOrderServer();
      if (izZeroQtyRemaining)
      {
        OrderHeaderData vpOHData = mpOrderServer.getOrderHeaderRecord(ipMoveData.getOrderID());
        
        // we need to adjust the load line item
        StandardInventoryAdjustServer vpInvAdjServer =
            Factory.create(StandardInventoryAdjustServer.class);
        vpInvAdjServer.adjustLoadLineItemIfNeeded(isUserID,
            vpLLIData.getLoadID(), vpLLIData.getItem(), vpLLIData.getLot(),
            null, null, null, vpLLIData.getPositionID(), 0.0,
            ReasonCode.getCycleCountReasonCode());
  
        // adjustLoadLineItemIfNeeded may have changed the order status
        // If we are to reallocate, set order to REALLOC and notify the Allocator
  
        vpOHData = mpOrderServer.getOrderHeaderRecord(ipMoveData.getOrderID());
        if (vpOHData != null)
        {
          if (!izReallocate && (vpOHData.getOrderStatus() == DBConstants.REALLOC))
          {
            // order can go to REALLOC because the remaining move was deleted
            // if the realloc flag is not set, recheck the order status
            checkOrderHeader(vpOHData.getOrderID());
          }
          
          if (izReallocate && ((vpOHData.getOrderStatus() == DBConstants.SCHEDULED) ||
              (vpOHData.getOrderStatus() == DBConstants.SHORT)))
          {
            mpOrderServer.setOrderStatusValue(vpOHData.getOrderID(), DBConstants.REALLOC);
            vpOHData.setOrderStatus(DBConstants.REALLOC);
            vzAllocate = true;
          }
        }
      }
      
      // Print the pick label
      printPickLabel(isPrinterName, ipMoveData, isToLoad, idQtyPicked);
      
      commitTransaction(tt);
      if (vzAllocate)
      {
        mpOrderServer.allocateOrder(ipMoveData.getOrderID());
      }
    }
    catch (Exception e)
    {
      logException("Error Confirming Pick: ", e);
      throw new DBException(e);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Print a pick label
   * 
   * @param isPrinterName
   * @param ipMoveData
   * @param isToLoad
   * @param idQtyPicked
   * @throws LabelGeneratorException
   */
  public void printPickLabel(String isPrinterName, MoveData ipMoveData,
      String isToLoad, double idQtyPicked) throws LabelGeneratorException
  {
    if (isPrinterName.trim().length() > 0)
    {
      PickLabel vpPickLabel = Factory.create(PickLabel.class);
      Date vpPrintDate = new Date();
      vpPickLabel.printPickLabel(isPrinterName, ipMoveData, isToLoad,
          idQtyPicked, vpPrintDate, false);
      mpLastPickInfo = new PickLabelInfo(ipMoveData, isToLoad,
          idQtyPicked, vpPrintDate);
    }
  }

  /**
   * Print a pick label
   * 
   * @param isPrinterName
   * @param ipMoveData
   * @param isToLoad
   * @param idQtyPicked
   * @throws LabelGeneratorException
   */
  public void reprintLastPickLabel(String isPrinterName) throws LabelGeneratorException
  {
    if (isPrinterName.trim().length() == 0)
    {
      throw new LabelGeneratorException("No printer name supplied.");
    }
    
    if (mpLastPickInfo == null)
    {
      throw new LabelGeneratorException("There is no data for reprint.");
    }
    
    PickLabel vpPickLabel = Factory.create(PickLabel.class);
    vpPickLabel.printPickLabel(isPrinterName, mpLastPickInfo.mpLabelMove, 
        mpLastPickInfo.msLabelToLoad, mpLastPickInfo.mdLabelQuantity,
        mpLastPickInfo.mpLabelDate, true);
  }

  /**
   * Method to pick this load to deletion. Deletes all inventory, moves and
   * loads on the specified load and its child loads.
   * 
   * @param isLoadID Load ID being picked.
   * @param isOrderID the order this load belongs to.
   * @param captive Specifies if pick was performed at a Captive location.
   * @param toLoad Load ID the inventory is being transferred to.
   * @exception DBException for database transaction errors.
   */
  protected void pickLoadToDeletion(String isLoadID, String isOrderID,
      boolean captive, String toLoad) throws DBException
  {
    LoadLineItem vpLoadLineItem = Factory.create(LoadLineItem.class);
    
    List<Map> vpItemDetList = vpLoadLineItem.getLoadLineItemDataListByLoadID(isLoadID);
    List<LoadLineItemData> vpLoadLineList = DBHelper.convertData(vpItemDetList,
                                                       LoadLineItemData.class);
    
    for (LoadLineItemData vpLoadLine : vpLoadLineList)
    {
      String pickedItem = vpLoadLine.getItem();
      String pickedLot = vpLoadLine.getLot();
      String pickedOrder = vpLoadLine.getOrderID();
      double pickedQuantity = vpLoadLine.getCurrentQuantity();
      
      vpLoadLineItem.deleteLoadLineItem(pickedItem, pickedLot, vpLoadLine.getLoadID(),
                                        pickedOrder, vpLoadLine.getOrderLot(),
                                        vpLoadLine.getLineID(),vpLoadLine.getPositionID());
      MoveData vpMoveData = deletePickedMove(isLoadID, isOrderID, vpLoadLine);
      if (vpMoveData == null || vpMoveData.getMoveType() == DBConstants.LOADMOVE)
      {
        vpMoveData = Factory.create(MoveData.class);
        vpMoveData.setLoadID(isLoadID);
        vpMoveData.setOrderID(isOrderID);
        vpMoveData.setItem(pickedItem);
        vpMoveData.setPickLot(pickedLot);
        
        LoadData vpLoadData = Factory.create(Load.class).getLoadData(isLoadID);
        vpMoveData.setWarehouse(vpLoadData.getWarehouse());
        vpMoveData.setAddress(vpLoadData.getAddress());
      }
      
      // Log Inventory Deletion history
      // This whole method assumes there is at most one move per item detail
      // TODO: Fix the to-load location if it ever matters
      logItemPickTransaction(isLoadID, vpMoveData.getWarehouse()
          + vpMoveData.getAddress(), toLoad, null, pickedItem, pickedLot,
          isOrderID, vpMoveData.getLineID(), vpMoveData.getOrderLot(),
          pickedQuantity);
      
      // Send a pick complete
      if(mzHasHostSystem)
      {
        initializeHostServer();
        mpHostServ.sendPickComplete(vpMoveData, pickedQuantity);
      }
    }
    
    Load load = Factory.create(Load.class);
    List<Map> childLoads = load.getChildLoads(isLoadID);
    for (Map vpMap : childLoads)
    {
      pickLoadToDeletion(DBHelper.getStringField(vpMap, LoadData.LOADID_NAME),
          isOrderID, captive, toLoad);
    }

    Move vpMove = Factory.create(Move.class);

                                       // All moves should be gone at this point.
                                       // This is simply a check to make sure.
    List<Map> vpMoveList = vpMove.getMovesByLoadID(isLoadID);
    for(Map vpMap : vpMoveList)
    {
      vpMove.deleteByMoveID(DBHelper.getIntegerField(vpMap, "iMoveID"));
    }

    LoadData lddata = getLoad(isLoadID, DBConstants.WRITELOCK);
    if (!captive) //delete the load
    {
      load.deleteLoad(lddata.getLoadID());
      tnData.clear();
      tnData.setTranCategory(DBConstants.LOAD_TRAN);
      tnData.setTranType(DBConstants.DELETE_LOAD);
      tnData.setLoadID(lddata.getLoadID());
      tnData.setLocation(lddata.getWarehouse(), lddata.getAddress());
      logTransaction(tnData);
    }
    else  // set the load empty
    {
      load.updateLoadAmountFull(lddata.getLoadID(), DBConstants.EMPTY);
    }
  }

 /**
  *  Method to pick inventory from one load to another. Transfer all inventory
  *  on the specified load and its child loads.
  *
  *  @param moveData Move data object to be completed.
  *  @param toLoad Load ID the inventory is being transferred to.
  *  @exception DBException
  */
  protected void pickInvt(MoveData moveData, String toLoad) throws DBException
  {
    Load load = Factory.create(Load.class);
    LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
    List<Map> loadLineItems = loadLineItem.getLoadLineItemDataListByLoadID(moveData.getLoadID());
    for(Iterator<Map> it = loadLineItems.iterator(); it.hasNext();)
    {
      LoadLineItemData vpFromLLIData = Factory.create(LoadLineItemData.class);
      vpFromLLIData.dataToSKDCData(it.next());
      
      // now see if this load line item already exists on the load
      LoadLineItemData llidataTo = loadLineItem.getLoadLineItemData(moveData.getItem(),
          moveData.getPickLot(), toLoad, moveData.getOrderID(), moveData.getOrderLot(), moveData.getLineID(),
          moveData.getPositionID());
      if (llidataTo == null)  // need to just modify the from load line item
      {
        llidataSearch.setKey(LoadLineItemData.LOADID_NAME, vpFromLLIData.getLoadID());
        llidataSearch.setKey(LoadLineItemData.ITEM_NAME, vpFromLLIData.getItem());
        llidataSearch.setKey(LoadLineItemData.LOT_NAME, vpFromLLIData.getLot());
        llidataSearch.setKey(LoadLineItemData.POSITIONID_NAME, vpFromLLIData.getPositionID());
        llidataSearch.setLoadID(toLoad);
        llidataSearch.setOrderID(moveData.getOrderID());
        llidataSearch.setLineID(moveData.getLineID());
        loadLineItem.modifyElement(llidataSearch);

        logOperation(LogConsts.OPR_DSVR, 
            "Move Load Line Item from load " + vpFromLLIData.getLoadID() +
            " to load " + toLoad +
            " item " + vpFromLLIData.getItem() +
            " lot " + vpFromLLIData.getLot() +
            " qty " + vpFromLLIData.getCurrentQuantity() +
            " for order " + moveData.getOrderID());
        if (mzHasHostSystem)
        {
          initializeHostServer();
          mpHostServ.sendPickComplete(moveData, llidataSearch.getCurrentQuantity());
        }
      }
      else  // need to merge into the to and delete the from
      {
        llidataTo.setKey(LoadLineItemData.LOADID_NAME, llidataTo.getLoadID());
        llidataTo.setKey(LoadLineItemData.ITEM_NAME, llidataTo.getItem());
        llidataTo.setKey(LoadLineItemData.LOT_NAME, llidataTo.getLot());
        llidataTo.setKey(LoadLineItemData.ORDERID_NAME, llidataTo.getOrderID());
        llidataTo.setKey(LoadLineItemData.ORDERLOT_NAME, llidataTo.getOrderLot());
        llidataTo.setKey(LoadLineItemData.LINEID_NAME, llidataTo.getLineID());
        llidataTo.setCurrentQuantity(llidataTo.getCurrentQuantity() +
            vpFromLLIData.getCurrentQuantity());
        llidataTo.setAllocatedQuantity(llidataTo.getAllocatedQuantity() +
            vpFromLLIData.getAllocatedQuantity());
        loadLineItem.modifyElement(llidataTo);
        llidataSearch.setLoadID(toLoad);
        llidataSearch.setOrderID(moveData.getOrderID());
        loadLineItem.modifyElement(llidataSearch);
        loadLineItem.deleteLoadLineItem(vpFromLLIData.getItem(),
            vpFromLLIData.getLot(), vpFromLLIData.getLoadID(),
            vpFromLLIData.getOrderID(), vpFromLLIData.getOrderLot(),
            vpFromLLIData.getLineID(), vpFromLLIData.getPositionID());

        logOperation(LogConsts.OPR_DSVR, 
            "Merge Load Line Item from load " + vpFromLLIData.getLoadID() + 
            " onto load " + toLoad + 
            " item " + vpFromLLIData.getItem() + 
            " lot " + vpFromLLIData.getLot()
            + " Line ID " + vpFromLLIData.getLineID() + 
            " quantity " + vpFromLLIData.getCurrentQuantity() + 
            " for order " + moveData.getOrderID());
      }
      tnData.clear();
      tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
      tnData.setTranType(DBConstants.ITEM_PICK);
      tnData.setLoadID(vpFromLLIData.getLoadID());
      tnData.setToLoadID(toLoad);
      tnData.setItem(vpFromLLIData.getItem());
      tnData.setLot(vpFromLLIData.getLot());
      tnData.setOrderID(moveData.getOrderID());
      tnData.setLineID(moveData.getLineID());
      tnData.setPickQuantity(vpFromLLIData.getCurrentQuantity());
      logTransaction(tnData);
    }

    LoadData lddataTo = getLoad(toLoad, DBConstants.NOWRITELOCK);

    List<Map> childLoads = load.getChildLoads(moveData.getLoadID());
    for(Iterator<Map> it = childLoads.iterator(); it.hasNext();)
    {
      pickLoad(
          DBHelper.getStringField(it.next(), LoadLineItemData.LOADID_NAME),
          moveData, lddataTo.getWarehouse(), lddataTo.getAddress(), toLoad);
    }

  }

  /**
   * Method to pick this load. Picks all inventory on the specified load and its
   * child loads.
   * 
   * @param loadID Load ID being picked.
   * @param moveData Move data object to be completed.
   * @param warehouse Specifies warehouse of location where pick was performed.
   * @param address Specifies address of location where pick was performed.
   * @param toLoad Load ID the inventory is being transferred to.
   * @exception DBException
   */
  protected void pickLoad(String loadID, MoveData moveData, String warehouse,
                        String address, String toLoad) throws DBException
  {
    Load load = Factory.create(Load.class);
    Move move = Factory.create(Move.class);
    LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
    List<Map> loadLineItems = loadLineItem.getLoadLineItemDataListByLoadID(loadID);
    for(Iterator<Map> it = loadLineItems.iterator(); it.hasNext();)
    {
      Map tMap = it.next();
      LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
      llidataSearch.setOrderID(moveData.getOrderID());
      llidataSearch.setKey(LoadLineItemData.LOADID_NAME,
          DBHelper.getStringField(tMap, LoadLineItemData.LOADID_NAME));
      llidataSearch.setKey(LoadLineItemData.ITEM_NAME, 
          DBHelper.getStringField(tMap, LoadLineItemData.ITEM_NAME));
      llidataSearch.setKey(LoadLineItemData.LOT_NAME, 
          DBHelper.getStringField(tMap, LoadLineItemData.LOT_NAME));
      loadLineItem.modifyElement(llidataSearch);
      if(mzHasHostSystem)
      {
        initializeHostServer();
        mpHostServ.sendPickComplete(moveData, llidataSearch.getCurrentQuantity());
      }
    }

    List<Map> childLoads = load.getChildLoads(loadID);
    for(Iterator<Map> it = childLoads.iterator(); it.hasNext();)
    {
      pickLoad(DBHelper.getStringField(it.next(), LoadData.LOADID_NAME), moveData, warehouse, address, null);
    }

    List<Map> moves = move.getMovesByLoadID(loadID);
    for(Iterator<Map> it = moves.iterator(); it.hasNext();)
    {
      MoveData mvdataSearch = Factory.create(MoveData.class);
      mvdataSearch.setKey(MoveData.MOVEID_NAME, Integer.valueOf(DBHelper.getIntegerField(it.next(), "iMoveID")));
      mvdataSearch.setMoveStatus(DBConstants.AVAILABLE);
      move.modifyElement(mvdataSearch);
    }

    LoadData lddata = getLoad(loadID, DBConstants.WRITELOCK);
    lddata.setKey(LoadData.LOADID_NAME, lddata.getLoadID());
    String tranHist = "Move load " + loadID +
      " from " + lddata.getWarehouse() +
      "-" + lddata.getAddress() +
      " to " + warehouse +
      "-" + address +
      " for order " + moveData.getOrderID();

    lddata.setWarehouse(warehouse);
    lddata.setAddress(address);
    lddata.setRouteID(moveData.getRouteID());
    if (!SKDCUtility.isFilledIn(toLoad))
    {
      lddata.setParentLoadID(null);
    }
    else
    {
      lddata.setParentLoadID(toLoad);
    }
    load.modifyElement(lddata);

    logOperation(LogConsts.OPR_DSVR, tranHist);
                                       // Log Inventory Transfer History
    tnData.clear();
    tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
    tnData.setTranType(DBConstants.TRANSFER);
    tnData.setLoadID(loadID);
    if (SKDCUtility.isFilledIn(toLoad)) tnData.setToLoadID(toLoad);
    tnData.setLocation(lddata.getWarehouse() + lddata.getAddress());
    tnData.setOrderID(moveData.getOrderID());
    logTransaction(tnData);
  }

  /**
   * Method to adjust the quantity for this move. Delete the move if quantity to
   * be picked is zero
   * 
   * @param moveID Load ID being picked.
   * @param qtyPicked Quantity that is to be adjusted.
   * @return MoveData object containing modified move data.
   * @exception DBException
   */
  protected MoveData adjustMove(MoveData ipMoveData, double qtyPicked) throws DBException
  {
    int vnMoveID = ipMoveData.getMoveID();
    
    Move vpMove = Factory.create(Move.class);
    MoveData vpMoveKey = Factory.create(MoveData.class);
    vpMoveKey.setKey(MoveData.MOVEID_NAME, vnMoveID);
    MoveData vpMoveData = vpMove.getElement(vpMoveKey, DBConstants.WRITELOCK);
    if (vpMoveData == null)
    {
      DBHelper.dbThrow("Cannot find move record");
    }
    if (vpMoveData.getPickQuantity() < qtyPicked)
    {
      throw new DBException("Pick Qty: " + qtyPicked
          + " Cannot be more than requested Pick Qty: "
          + vpMoveData.getPickQuantity() + " for item: " + vpMoveData.getItem()
          + " MoveID:" + vpMoveData.getMoveID());
    }
    vpMoveData.setPickQuantity(vpMoveData.getPickQuantity() - qtyPicked);
    vpMoveData.setKey(MoveData.MOVEID_NAME, Integer.valueOf(vpMoveData.getMoveID()));
    if (vpMoveData.getPickQuantity() == 0)
    {
      // We picked it all, so delete the move
      vpMove.deleteByMoveID(vpMoveData.getMoveID());
    }
    else
    {
      vpMove.modifyElement(vpMoveData);
    }
    
    double originalPickQuantity = vpMoveData.getPickQuantity() + qtyPicked;
    logOperation(LogConsts.OPR_DSVR, "Pick " + vnMoveID +
      " order " + vpMoveData.getOrderID() +
      " load " + vpMoveData.getLoadID() +
      " item " + vpMoveData.getItem() +
      " lot " + vpMoveData.getPickLot() +
      " quantity " + qtyPicked +
      " of " + originalPickQuantity);
    
    return (vpMoveData);
  }

  /**
   * Method to adjust the load line items that correspond to the move. If the
   * amount of the load line item is zero after picking, it will be deleted. If
   * a to load is specified and a matching load line item exists on that load
   * then the picked inventory will be merged into the existing inventory, if it
   * doesn't exist a new load line item will be created on the to load.
   * 
   * @param ipMoveData Move data object to be completed.
   * @param idQtyPicked Quantity that was picked.
   * @param izDeleteInventory Specifies if pick was performed at a location
   *            where inventory is to be deleted.
   * @param isToLoad Load that inventory is being moved to.
   * @param isReasonCode reason code for adjustment (if any).
   * @exception DBException
   */
  protected void adjustLoadLineItems(MoveData ipMoveData, double idQtyPicked,
                                     boolean izDeleteInventory, String isToLoad,
                                     String isReasonCode) throws DBException
  {
    Load load = Factory.create(Load.class);
    LoadData lddataTo = null;
    LoadData lddataFrom = null;
    StationData vpStnData = null;
    StandardStationServer stationServer = Factory.create(StandardStationServer.class);
    LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
    LoadLineItem toLoadLineItem = Factory.create(LoadLineItem.class);
    
    // get the load line item on the from load so we decrement the qty
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
    llidataSearch.setKey(LoadData.LOADID_NAME, ipMoveData.getLoadID());
    llidataSearch.setKey(LoadLineItemData.ITEM_NAME, ipMoveData.getItem());
    llidataSearch.setKey(LoadLineItemData.LOT_NAME, ipMoveData.getPickLot());
    llidataSearch.setKey(LoadLineItemData.POSITIONID_NAME,ipMoveData.getPositionID());
    LoadLineItemData llidataFrom = loadLineItem.getElement(llidataSearch, 
                                                         DBConstants.WRITELOCK);
    if (llidataFrom == null)
    {
      // no load line item when we need it
      DBHelper.dbThrow("No load line item for this move");
    }
    
    // Lock the load
    lddataFrom = getLoad(ipMoveData.getLoadID(), DBConstants.WRITELOCK);

    // if we don't delete the inventory, we need to merge/add load line item
    // for the to load
    if (!izDeleteInventory)
    {
      /*
       * If the ToLoad is filled in get it's information here
       */
      if (isToLoad.trim().length() > 0)
      {
        lddataTo = getLoad(isToLoad, DBConstants.WRITELOCK);
      }
      
      /*
       * If the from load is not at a station but is in a location we must be
       * picking with a conventional device, so put the load on our terminal and
       * get the the location we need to deposit to next.
       */
      vpStnData = stationServer.getControllingStationFromLocation(
          lddataFrom.getWarehouse(), lddataFrom.getAddress());
      
      String vsAddress = getNextAddressOnRouteForMove(ipMoveData, vpStnData.getStationName());
      String vsWarehouse = getStationsWarehouse(vsAddress);
      if (!SKDCUtility.isFilledIn(vsAddress))
      {
        // no route when we need it
        String tmpstring = "No Address for Next Location for move of Load: " + 
                           ipMoveData.getLoadID() + 
                           " From Station/Route: " + 
                           vpStnData.getStationName() + "/" + 
                           ipMoveData.getRouteID();
        logError(tmpstring);
        DBHelper.dbThrow(tmpstring);
      }

      if (!SKDCUtility.isFilledIn(vsWarehouse))
      {
        // no route when we need it
        String tmpstring = "No Warehouse for Next Location for move of Load: " + 
                           ipMoveData.getLoadID() + 
                           " From Station/Route: " + 
                           vpStnData.getStationName() + "/" + 
                           ipMoveData.getRouteID();
        logError(tmpstring);
        DBHelper.dbThrow(tmpstring);
      }

      // check the to load
      // if it exists it better be at the next location
      // if it does not exist then add it at the next location
      if (lddataTo == null)
      {
        // add the load
        addLoad(ipMoveData, isToLoad, vsWarehouse, vsAddress, false);
        lddataTo = getLoad(isToLoad, DBConstants.WRITELOCK);
      }

      // now see if this load line item already exists on the load
      llidataSearch.clear();
      llidataSearch.setKey(LoadData.LOADID_NAME, isToLoad);
      llidataSearch.setKey(LoadLineItemData.ITEM_NAME, ipMoveData.getItem());
      llidataSearch.setKey(LoadLineItemData.LOT_NAME, ipMoveData.getPickLot());
      llidataSearch.setKey(LoadLineItemData.ORDERID_NAME,ipMoveData.getOrderID());
      llidataSearch.setKey(LoadLineItemData.ORDERLOT_NAME,ipMoveData.getOrderLot());
      llidataSearch.setKey(LoadLineItemData.LINEID_NAME,ipMoveData.getLineID());
      LoadLineItemData llidataTo = toLoadLineItem.getElement(llidataSearch, DBConstants.WRITELOCK);
      if (llidataTo == null)  // need to add load line item
      {
        llidataTo = llidataFrom.clone();
        llidataTo.clear();

        llidataTo.setLoadID(isToLoad);
        llidataTo.setItem(ipMoveData.getItem());
        llidataTo.setLot(ipMoveData.getPickLot());
        llidataTo.setOrderID(ipMoveData.getOrderID());
        llidataTo.setOrderLot(ipMoveData.getOrderLot());
        llidataTo.setLineID(ipMoveData.getLineID());
        llidataTo.setCurrentQuantity(idQtyPicked);
        llidataTo.setAllocatedQuantity(idQtyPicked);
        llidataTo.setHoldType(DBConstants.SHIPHOLD);
        llidataTo.setExpirationDate(llidataFrom.getExpirationDate());
        toLoadLineItem.addLoadLineItem(llidataTo);

        logOperation(LogConsts.OPR_DSVR, "Add Load Line Item from load " + 
                     ipMoveData.getLoadID() +
                     " to load " + isToLoad +
                     " item " + ipMoveData.getItem() +
                     " picked lot " + ipMoveData.getPickLot() +
                     " quantity " + idQtyPicked +
                     " for order " + ipMoveData.getOrderID() +
                     " order lot " + ipMoveData.getOrderLot() +
                     " Line ID " + ipMoveData.getLineID());
      }
      else
      {
        llidataTo.setKey(LoadLineItemData.LOADID_NAME, llidataTo.getLoadID());
        llidataTo.setKey(LoadLineItemData.ITEM_NAME, llidataTo.getItem());
        llidataTo.setKey(LoadLineItemData.LOT_NAME, llidataTo.getLot());
        llidataTo.setKey(LoadLineItemData.ORDERID_NAME, llidataTo.getOrderID());
        llidataTo.setKey(LoadLineItemData.ORDERLOT_NAME, llidataTo.getOrderLot());
        llidataTo.setKey(LoadLineItemData.LINEID_NAME, llidataTo.getLineID());
        llidataTo.setCurrentQuantity(llidataTo.getCurrentQuantity() + idQtyPicked);
        llidataTo.setAllocatedQuantity(llidataTo.getAllocatedQuantity() + idQtyPicked);
        llidataTo.setHoldType(DBConstants.SHIPHOLD);
        toLoadLineItem.modifyElement(llidataTo);

        logOperation(LogConsts.OPR_DSVR, "Merge Load Line Item from load " + ipMoveData.getLoadID() +
          " to load " + isToLoad +
          " item " + ipMoveData.getItem() +
          " picked lot " + ipMoveData.getPickLot() +
          " quantity " + idQtyPicked +
          " for order " + ipMoveData.getOrderID() +
          " order lot " + ipMoveData.getOrderLot() +
          " Line ID " + ipMoveData.getLineID());
      }

      logItemPickTransaction(ipMoveData, lddataTo, idQtyPicked);
    }
    else
    {
      //we need to log the pick
      logItemPickTransaction(ipMoveData, null, idQtyPicked);
    }
    // now modify the from load line item
    llidataFrom.setCurrentQuantity(llidataFrom.getCurrentQuantity() - idQtyPicked);
    llidataFrom.setAllocatedQuantity(llidataFrom.getAllocatedQuantity() - idQtyPicked);
    llidataFrom.setKey(LoadLineItemData.LOADID_NAME, llidataFrom.getLoadID());
    llidataFrom.setKey(LoadLineItemData.ITEM_NAME, llidataFrom.getItem());
    llidataFrom.setKey(LoadLineItemData.LOT_NAME, llidataFrom.getLot());
    llidataFrom.setKey(LoadLineItemData.POSITIONID_NAME, llidataFrom.getPositionID());
    
    if (llidataFrom.getCurrentQuantity() == 0)
    {
      loadLineItem.deleteElement(llidataFrom);
      // if this is last item on load, set load to Empty
      llidataSearch.clear();
      llidataSearch.setKey(LoadData.LOADID_NAME, llidataFrom.getLoadID());
      if (loadLineItem.getCount(llidataSearch) == 0)
      {
        load.updateLoadAmountFull(llidataFrom.getLoadID(), DBConstants.EMPTY);
      }
    }
    else
    {
      loadLineItem.modifyElement(llidataFrom);
    }
    
    checkForReplenishment(ipMoveData);
  }

  /**
   * Check to see if this pick caused a need for replenishment
   * 
   * @param moveData
   * @throws DBException
   */
  public void checkForReplenishment(MoveData moveData) throws DBException
  {
    // Now check to see if this was a pick from a dedicated location - if so ,
    // send the replenishment request.
    StandardDedicationServer dedServer = Factory.create(StandardDedicationServer.class);
    dedServer.replenishDedication(moveData.getItem(), moveData.getWarehouse(), 
                                                      moveData.getAddress());
  }
 /**
  *  Method to adjust the order line item for this Load move.
  *
  *  @param moveData Move data object to be completed.
  *  @exception DBException
  */
  public void adjustOrderLineForLoad(MoveData moveData) throws DBException
  {
    OrderLine orderLine = Factory.create(OrderLine.class);
    OrderLineData oldataSearch = Factory.create(OrderLineData.class);
    // get the order line so we increment the qty
    oldataSearch.setKey(OrderLineData.ORDERID_NAME, moveData.getOrderID());
    oldataSearch.setKey(OrderLineData.LOADID_NAME, moveData.getLoadID());
    OrderLineData oldata = orderLine.getElement(oldataSearch, DBConstants.WRITELOCK);
    if (oldata == null)
    {
      // can't find line item record
      DBHelper.dbThrow("Unable to find Order Line record for order: "
        + moveData.getOrderID() + ", load: " + moveData.getLoadID());
    }
    oldata.setPickQuantity(oldata.getPickQuantity() + 1.0);
    oldata.setKey(OrderLineData.ORDERID_NAME, oldata.getOrderID());
    oldata.setKey(OrderLineData.LOADID_NAME, oldata.getLoadID());
    orderLine.modifyElement(oldata);
    checkOrderHeader(oldata.getOrderID());
  }

 /**
  *  Method to adjust the order line item for this item move. After the
  *  pick, the order line item will be marked Shy if the amount of the pick
  *  quantity is equal to the allocated quantity and the allocated quantity
  *  is less than the order quantity. If the pick quantity equals the allocated
  *  quantity then we will recheck the order status.
  *
  *  @param moveData Move data object to be completed.
  *  @param qtyPicked Quantity that was picked.
  *  @exception DBException
  */
  protected String adjustOrderLine(MoveData moveData, double qtyPicked) throws DBException
  {
    OrderLine orderLine = Factory.create(OrderLine.class);
    OrderLineData oldataSearch = Factory.create(OrderLineData.class);
    // get the order line so we increment the qty
    oldataSearch.setKey(OrderLineData.ORDERID_NAME, moveData.getOrderID());
    oldataSearch.setKey(OrderLineData.ITEM_NAME, moveData.getItem());
    oldataSearch.setKey(OrderLineData.ORDERLOT_NAME, moveData.getOrderLot());
    oldataSearch.setKey(OrderLineData.LINEID_NAME, moveData.getLineID());
    OrderLineData oldata = orderLine.getElement(oldataSearch, DBConstants.WRITELOCK);
    oldata.setPickQuantity(oldata.getPickQuantity() + qtyPicked);
    if (oldata.getAllocatedQuantity() < oldata.getOrderQuantity())
    {
      oldata.setLineShy(DBConstants.YES);
    }
    else
    {
      oldata.setLineShy(DBConstants.NO);
    }
    oldata.setKey(OrderLineData.ORDERID_NAME, oldata.getOrderID());
    oldata.setKey(OrderLineData.ITEM_NAME, oldata.getItem());
    oldata.setKey(OrderLineData.ORDERLOT_NAME, oldata.getOrderLot());
    oldata.setKey(OrderLineData.LINEID_NAME, oldata.getLineID());
    orderLine.modifyElement(oldata);
    checkOrderHeader(oldata.getOrderID());

    return (oldata.getLineID());
  }

 /**
  * Method to check this order to see if we are done picking it. Order is DONE
  * if no moves exist for it, and there are no load line items for it. Order is
  * PICKCOMP if no moves exist but there are still load line items. Orders that
  * are DONE are currently deleted here.  PICKCOMP orders are normally deleted 
  * by a shipping system.
  * 
  * @param isOrderID Order ID being picked.
  * @exception DBException
  */
  public void checkOrderHeader(String isOrderID) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      initializeOrderServer();
      
      if (!mpOrderServer.orderHasMoves(isOrderID))
      {
        OrderHeaderData vpOHData = mpOrderServer.getOrderHeaderRecord(isOrderID, 
                                                         DBConstants.WRITELOCK);
        if (vpOHData != null)
        {
          mpShortOrderProcess.setNextOrderState(isOrderID,
                                                vpOHData.getDestinationStation());
          if (vpOHData.getOrderType() == DBConstants.ITEMORDER)
          {
            if (mpOrderServer.loadLinesHaveOrders(isOrderID))
            {                          // There must be pick-to loads still on
                                       // the system for this order.
              // make sure the order is in proper status
              if (vpOHData.getOrderStatus() == DBConstants.SCHEDULED)
              {
                mpOrderServer.setOrderStatusValue(isOrderID, DBConstants.PICKCOMP);
              }
            }
            else
            {
              deleteAutoPickOrderIfNecessary(vpOHData);
            }
          }
          else
          {
            deleteAutoPickOrderIfNecessary(vpOHData);
          }
        }
      }
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      DBHelper.dbThrow(e.getMessage());
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  *  Method to adjust the order line item for this empty container move. Since
  *  one store can complete multiple empty container request, we find
  *  all matching moves and complete each one. We delete each move and adjust
  *  its corresponding order line item. If the order line item pick quantity
  *  equals the allocated quantity then we will recheck the order status.
  *
  *  @param moveData Move data object to be completed.
  *  @exception DBException
  */
  public void completeEmptyContainerMove(MoveData moveData) throws DBException
  {
    OrderLine orderLine = Factory.create(OrderLine.class);
    OrderLineData oldataSearch = Factory.create(OrderLineData.class);
    Move move = Factory.create(Move.class);
    // make sure this is an empty container request
    if (moveData.getMoveType() != DBConstants.EMPTYMOVE)
    {
      // not an empty contaainer request
      DBHelper.dbThrow("Can not complete non empty container moves here");
    }
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      
      // there can be multiple MT requests satisfied by 1 store
      // find all matching moves then complete each one
      // moves can be for different orders
      List<Map> moves = move.getMTMovesByLoadID(moveData.getLoadID(),
          moveData.getItem(), moveData.getOrderLot());
      for(Iterator<Map> it = moves.iterator(); it.hasNext();)
      {
        Map tMap = it.next();
        // delete this move record
        move.deleteByMoveID(DBHelper.getIntegerField(tMap,"iMoveID"));

        // get the order line so we increment the qty
        oldataSearch.clear();
        oldataSearch.setKey(OrderLineData.ORDERID_NAME, 
            DBHelper.getStringField(tMap, OrderLineData.ORDERID_NAME));
        oldataSearch.setKey(OrderLineData.ITEM_NAME, 
            DBHelper.getStringField(tMap, OrderLineData.ITEM_NAME));
        oldataSearch.setKey(OrderLineData.ORDERLOT_NAME, 
            DBHelper.getStringField(tMap, OrderLineData.ORDERLOT_NAME));
        OrderLineData oldata = orderLine.getElement(oldataSearch, DBConstants.WRITELOCK);
        if (oldata == null)
        {
          DBHelper.dbThrow("Cannot find Empty Container order line");
        }
        oldata.setPickQuantity(oldata.getPickQuantity() + 
            DBHelper.getDoubleField(tMap, OrderLineData.PICKQUANTITY_NAME));
        oldata.setKey(OrderLineData.ORDERID_NAME, oldata.getOrderID());
        oldata.setKey(OrderLineData.ITEM_NAME, oldata.getItem());
        oldata.setKey(OrderLineData.ORDERLOT_NAME, oldata.getOrderLot());
        orderLine.modifyElement(oldata);
        if (oldata.getOrderQuantity() <= oldata.getPickQuantity())
        {
          checkOrderHeader(oldata.getOrderID());
        }
      }

      commitTransaction(tt);
    }
    catch (DBException e)
    {
      DBHelper.dbThrow(e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

  }

  /**
   *  Method to Delete the load move and order associated with a load retrieval
   *  if the station is a P&D or a USHAPE OUT and there are no item moves...the station
   *  checking must be done before here.
   * 
   *  @param moveData Move data object to be completed.
   *  @exception DBException
   */
  public void completeLoadRetrieveMove(MoveData moveData) throws DBException
  {
    Move move = Factory.create(Move.class);

    StandardMoveServer moveServer = Factory.create(StandardMoveServer.class);
    // make sure this is a Load Retrieve request -- No Item Moves

    if ((moveData.getMoveType() != DBConstants.LOADMOVE) || 
        (moveServer.getNextMoveRecord(moveData.getLoadID(), DBConstants.ITEMMOVE) != null))
    {
      // not a Load Move request
      DBHelper.dbThrow("Can not complete non Load moves here");
    }
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();

      move.deleteByMoveID(moveData.getMoveID());
      checkOrderHeader(moveData.getOrderID());

      commitTransaction(tt);
    }
    catch (DBException e)
    {
      DBHelper.dbThrow(e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }
   
  /**
   * Method to complete a move for an Cycle Count move.
   * 
   * @param isUserID User ID.
   * @param ipMoveData Move data object to be completed.
   * @param idQtyCounted Specifies the quantity that was counted.
   * @exception DBException
   */
  public void completeCycleCountMove(String isUserID, MoveData ipMoveData, 
      double idQtyCounted) throws DBException
  {
    logDebug("User: " + isUserID + " completing cycle count move ID: "
        + ipMoveData.getMoveID());
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      
      if (ipMoveData.getItem().trim().length() == 0)
      {
        // Confirm an empty load
        tnData.clear();
        tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
        tnData.setTranType(DBConstants.CYCLE_COUNT);
        tnData.setOrderID(ipMoveData.getOrderID());
        tnData.setLoadID(ipMoveData.getLoadID());
        tnData.setLocation(ipMoveData.getWarehouse(), ipMoveData.getAddress());
        tnData.setReasonCode(ReasonCode.getCycleCountReasonCode());
        logTransaction(tnData);
      }
      else
      {
        // Make the adjustment
        StandardInventoryAdjustServer vpInvAdjServer =
          Factory.create(StandardInventoryAdjustServer.class);
        vpInvAdjServer.adjustLoadLineItemIfNeeded(isUserID,
            ipMoveData.getLoadID(), ipMoveData.getItem(),
            ipMoveData.getPickLot(), ipMoveData.getOrderID(),
            ipMoveData.getOrderLot(), ipMoveData.getLineID(),
            ipMoveData.getPositionID(), idQtyCounted, ipMoveData.getMoveID(),
            ReasonCode.getCycleCountReasonCode());
        
        // Update the Item Master Last CCI Date
        ItemMaster vpIM = Factory.create(ItemMaster.class);
        vpIM.updateLastCCIDate(ipMoveData.getItem());
      }
      if (ipMoveData.getMoveID() > 0)
      {
        deleteCycleCountMove(ipMoveData.getMoveID());
      }
      
      // Reset the load status in conventional locations
      resetParentLoadStatusIfNecessary(ipMoveData);
      
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      DBHelper.dbThrow(e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to delete cycle count moves.
   *
   *  @param moveID Load ID being picked.
   *  @return MoveData object containing modifuied move data.
   *  @exception DBException
   */
  protected void deleteCycleCountMove(int moveID) throws DBException
  {
//    Move move = Factory.create(Move.class);
//    MoveData mvdataSearch = Factory.create(MoveData.class);
//    mvdataSearch.setKey(mvdataSearch.getMoveIDName(), new Integer(moveID));
//    MoveData mv = (MoveData)move.getElement(mvdataSearch, DBConstants.WRITELOCK);
//    if (mv == null)
//    {
//      new DBHelper().dbThrow("Cannot find move record");
//    }
//    move.deleteByMoveID(mv.getMoveID());
//
//      // determine if all moves for this order are gone
//    mvdataSearch.clear();
//    mvdataSearch.setKey(mvdataSearch.getOrderIDName(), mv.getOrderID());
//    if (move.getCount(mvdataSearch) == 0)
//    {
//        // delete the corresponding cycle count record
//      CycleCountData ccSearch = Factory.create(CycleCountData.class);
//      ccSearch.setKey(ccSearch.getOrderIDName(), mv.getOrderID());
//      CycleCount mntOrder = Factory.create(CycleCount.class);
//      mntOrder.deleteElement(ccSearch);
//   }
    StandardMaintenanceOrderServer maintOrderServer = Factory.create(StandardMaintenanceOrderServer.class);
    maintOrderServer.deleteMaintenanceOrderMove(moveID);
  }

 /**
  *  Method to add a pick-to load at a location. The new load will be try
  *  to get is container type from the to station. If the to location is a
  *  regular location, the location will be set OCCUPIED.
  *
  *  @param MoveData mvData - move information for this load with the Route this load should be on.
  *  @param loadID Load ID.
  *  @param warehouse Warehouse load is in.
  *  @param address Address load is at.
  *  @exception DBException
  */
  protected void addLoad(MoveData mvData, String loadID, String warehouse, 
                         String address, boolean conventionalPick) throws DBException
  {
    LoadData lddataTo = Factory.create(LoadData.class);
    Location location = Factory.create(Location.class);

    StandardStationServer stnServer = Factory.create(StandardStationServer.class);
    StationData stdata = Factory.create(StationData.class);
    if(conventionalPick)
    {
      
      lddataTo.setLoadMoveStatus(DBConstants.MOVING);
      lddataTo.setDeviceID(mvData.getDeviceID());
      lddataTo.setNextAddress(mvData.getNextAddress());
      lddataTo.setNextWarehouse(mvData.getNextWarehouse());
      lddataTo.setFinalWarehouse(mvData.getDestWarehouse());
      lddataTo.setFinalAddress(mvData.getDestAddress());
      stdata = stnServer.getControllingStationFromLocation(mvData.getWarehouse(), mvData.getAddress());
    }
    else
    {
      // address is the station, get the containertype from the station
      stdata = stnServer.getStation(address);
 
    }
    
    if (stdata == null)
    {
      lddataTo.setContainerType("UNKNOWN");
    }
    else
    {
      lddataTo.setContainerType(stdata.getContainerType());
      if(!conventionalPick)
      {
        lddataTo.setDeviceID(stdata.getDeviceID());
      }
    }

//    if(mvData.getCubePickAction() == DBConstants.FULLCASE)
//    {
//      lddataTo.setContainerType("FULLCASE");
//    }
    lddataTo.setLoadID(loadID);
    lddataTo.setParentLoadID(loadID);
    lddataTo.setWarehouse(warehouse);
    lddataTo.setAddress(address);
    lddataTo.setRouteID(mvData.getRouteID());
    lddataTo.setAmountFull(DBConstants.HALF);
    Factory.create(Load.class).addLoad(lddataTo);

                                       // Record Load Add Transaction
    tnData.clear();
    tnData.setTranCategory(DBConstants.LOAD_TRAN);
    tnData.setTranType(DBConstants.ADD_LOAD);
    tnData.setLoadID(lddataTo.getLoadID());
    tnData.setLocation(lddataTo.getWarehouse() + lddataTo.getAddress());
    tnData.setRouteID(lddataTo.getRouteID());
    logTransaction(tnData);

    LocationData lcdataSearch = Factory.create(LocationData.class);
    lcdataSearch.setKey(LocationData.WAREHOUSE_NAME, warehouse);
    lcdataSearch.setKey(LocationData.ADDRESS_NAME, address);
    LocationData lcdata = location.getElement(lcdataSearch, DBConstants.WRITELOCK);
    if (lcdata != null)
    {
      if(lcdata.getEmptyFlag() == DBConstants.UNOCCUPIED)
      {
                // may need to set location occupied
        lcdata.setKey(LocationData.WAREHOUSE_NAME, warehouse);
        lcdata.setKey(LocationData.ADDRESS_NAME, address);
        lcdata.setEmptyFlag(DBConstants.OCCUPIED);
        location.modifyElement(lcdata);
      }
    }
  }

 /**
  *  Method to check if a load can be released from a station. Method does not
  *  actually release the load, just evaluates it to see if can be released. The
  *  operator will decide if they still want to release load or not.
  *
  *  @param loadID Load ID to check for release.
  *  @return empty String if load can be released or a message containing
  *          reason load might not be released.
  *  @exception DBException if there is a database access error.
  */
  public String checkLoadForRelease(String isLoadID) throws DBException
  {
    String vsMesg = "";
   
    StandardMoveServer vpMoveServ = Factory.create(StandardMoveServer.class);
    MoveData vpMoveData = vpMoveServ.getNextMoveRecord(isLoadID);
    if (vpMoveData != null)
    {                                  // Pick requests exist for items on this
                                       // load.
      vsMesg = "Load has Pick request ";
      if (vpMoveData.getItem().trim().length() > 0)
      {
        vsMesg += ("for item: " + vpMoveData.getItem());
      }
      
      if (vpMoveData.getOrderLot().trim().length() > 0)
      {
        vsMesg += (", lot: " + vpMoveData.getOrderLot());
      }
      vsMesg += ".\nRelease load";
    }
    return vsMesg;
  }

  /**
   *  Method to release a load from a station. For all non U-Shaped stations
   *  the load will be set to ARRIVEPENDING and a release message sent to the
   *  stations scheduler. For U-Shaped stations the load will not be released
   *  if the next location in its route can not be determined. If we determine
   *  the next location the load will be moved to that location, set to
   *  ARRIVEPENDING and a release message sent to the stations scheduler.
   *  Before being released, all empty container moves for the load will be
   *  completed.
   *
   *  @param loadID Load ID to check for release.
   *  @param stationData Station data object for station the load is at.
   *  @return String containing null if load was released, or a message
   *  containing error message for why load cannot be released.
   *  @exception DBException
   */
  public String releaseLoad(String loadID, StationData stationData) throws DBException
  {
    Load load = Factory.create(Load.class);
    TransactionToken tt = null;
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
    StationData stdata = null;
    StandardStationServer stationServer = Factory.create(StandardStationServer.class);
    
    try
    {
      tt = startTransaction();
      LoadData lddataFrom = getLoad(loadID, DBConstants.WRITELOCK);
      if (lddataFrom == null)
      {
        return("Load not found");
      }

      /*
       * If this is a reversible station and arrivals are required, put the in 
       * store mode.  (If arrivals are not required, we don't need to change 
       * modes).
       */
      if (stationData.getStationType() == DBConstants.REVERSIBLE &&
          stationData.getArrivalRequired() == DBConstants.YES)
      {
        stationData.setBidirectionalStatus(DBConstants.STOREMODE);
        stationServer.sendBiDirectionalChangeCommand(stationData);
      }
      
      String address;
      if ((stationData.getStationType() == DBConstants.USHAPE_IN) ||
         (stationData.getStationType() == DBConstants.USHAPE_OUT))
      {
        StandardRouteServer routeServer = Factory.create(StandardRouteServer.class);
        if (!SKDCUtility.isFilledIn(lddataFrom.getRouteID()))
        {
          address = routeServer.getNextRouteDest(stationData.getLinkRoute(), 
              lddataFrom.getAddress());
        }
        else
        {
          address = routeServer.getNextRouteDest(lddataFrom.getRouteID(), 
              lddataFrom.getAddress());
          
        }
        if (!SKDCUtility.isFilledIn(address))
        {
          // no route when we need it
          return("No routing for this load");
        }

        String warehouse = getStationsWarehouse(address);
        if (!SKDCUtility.isFilledIn(warehouse))
        {
          // no route when we need it
          return("No routing for this load");
        }
        lddataFrom.setWarehouse(warehouse);
        lddataFrom.setAddress(address);
      }
      else
      {
        address = stationData.getStationName();
      }

      // get information on the next station on the route
      stdata = stationServer.getStation(address);
      if (stdata == null)
      {
        return("No routing for this load");
      }

      lddataFrom.setKey(LoadData.LOADID_NAME, lddataFrom.getLoadID());
      lddataFrom.setLoadMoveStatus(DBConstants.ARRIVEPENDING);
      lddataFrom.setMoveDate();
      if (stationData.getCaptive() == DBConstants.CAPTIVE)
      {
        lddataFrom.setNextWarehouse(lddataFrom.getFinalWarehouse());
        lddataFrom.setNextAddress(lddataFrom.getFinalAddress());
      }
      else if (address.equals(lddataFrom.getNextAddress()))
      {
        lddataFrom.setNextWarehouse("");
        lddataFrom.setNextAddress("");
      }
      lddataFrom.setLoadMessage("");
      load.modifyElement(lddataFrom);

      // if there are no items on the load, set load to Empty
      // if its Empty but has invt, set it to one quarter just so we can
      // tell there is something in the load
      llidataSearch.clear();
      llidataSearch.setKey(LoadData.LOADID_NAME, loadID);
      if (Factory.create(LoadLineItem.class).getCount(llidataSearch) == 0)
      {
        load.updateLoadAmountFull(loadID, DBConstants.EMPTY);
      }
      else if (lddataFrom.getAmountFull() == DBConstants.EMPTY)
      {
        load.updateLoadAmountFull(loadID, DBConstants.ONEQUARTER);
      }

      commitTransaction(tt);
    }
    catch (DBException e)
    {
      DBHelper.dbThrow(e.getMessage());
    }
//    catch( NullPointerException ne)
//    {
//      System.out.println("Check Multiple Null for standard pick server - release Load");
//      ne.printStackTrace();
//      logException(ne, "Check Multiple Null for standard pick server - release Load");
//    }
    finally
    {
      endTransaction(tt);
    }
    
    /*
     * Note: This used to be before the commit, and that's bad, since it opens 
     * up the possibility of the message being processed before we've committed
     * the transaction.  If that occurs, the marry-up wont happen.
     * 
     * If we get to here, then we need to inform the station's scheduler.
     * Get the scheduler and send it a ScreenLoadRelease message.
     */
    String scheduler = stationServer.getStationsScheduler(stdata.getStationName());
    String cmdstr = Factory.create(LoadEventDataFormat.class, getClass().getSimpleName())
        .screenLoadRelease(loadID, stdata.getStationName());
    getSystemGateway().publishLoadEvent(cmdstr, 0, scheduler);

    return null;
  }

  /**
   *  Method to reset the parentload status to nomove if this was the last pick or cycle count
   *  for the load in a conventional Location
   *
   *  @param moveData MoveData object for move the load is in.
   *  @exception DBException
   */
   public void resetParentLoadStatusIfNecessary(MoveData moveData) throws DBException
   {
     TransactionToken tt = null;
     
     try
     {
       tt = startTransaction();
       StandardStationServer stationServer = Factory.create(StandardStationServer.class);
       StandardMoveServer moveServ = Factory.create(StandardMoveServer.class);
       StationData stdata = stationServer.getControllingStationFromLocation(moveData.getWarehouse(), moveData.getAddress());
       if (stdata == null)
       {
         // If no more moves exist for this load in the conventional location
         // set the load to NOMOVE so that we know there are no more picks there
         MoveData mvdat = Factory.create(MoveData.class);
         mvdat.setKey(MoveData.LOADID_NAME, moveData.getLoadID());
                  
         if(moveServ.getMoveCount(mvdat) < 1)
         {
           StandardLoadServer loadServer = Factory.create(StandardLoadServer.class);
           loadServer.setParentLoadMoveStatus(moveData.getParentLoad(),
              DBConstants.NOMOVE, "");
         }
       }
       commitTransaction(tt);
     }
     catch (DBException e)
     {
       DBHelper.dbThrow(e.getMessage());
     }
     finally
     {
       endTransaction(tt);
     }
     return;
   }

 /**
  *  Method to get a stations warehouse.
  *
  *  @param aStation Station.
  *  @return String containing warehouse.
  *  @exception DBException
  */
  public String getStationsWarehouse(String aStation) throws DBException
  {
    StationData stdataSearch = Factory.create(StationData.class);
    stdataSearch.setKey(StationData.STATIONNAME_NAME, aStation);
    StationData stdata = Factory.create(Station.class).getElement(stdataSearch, DBConstants.NOWRITELOCK);
    if (stdata == null)
    {
      return null;
    }
    return(stdata.getWarehouse());
  }

  /**
   * Method to autopick a load from a autopick station
   *
   * @param loadID no information available
   * @return true if deleted else false
   */
  public boolean autoPickLoadFromStation(String loadID)
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      boolean captive = false;
      boolean delInventory = false;
      StandardLoadServer loadServ = Factory.create(StandardLoadServer.class);
      LoadData loadData = loadServ.getLoad1(loadID);
      StationData stationData = Factory.create(StationData.class);
      stationData.setKey(StationData.STATIONNAME_NAME, loadData.getAddress());
      stationData = Factory.create(Station.class).getElement(stationData, DBConstants.NOWRITELOCK);
      StandardMoveServer moveServer = Factory.create(StandardMoveServer.class);
      MoveData moveData = moveServer.getNextMoveRecord(loadID);

      if((stationData.getCaptive() == DBConstants.CAPTIVE) ||
                (stationData.getCaptive() == DBConstants.SEMICAPTIVE))
      {
        logDebug("StandardPickServer.autoPickLoadFromStation() - captive");
        captive = true;
      }
      if(stationData.getDeleteInventory() == DBConstants.YES)
      {
        delInventory = true;
      }
      if (moveData != null)
      {
        completeLoadPick("ST" + loadData.getAddress(), moveData, captive, delInventory);
      }
      else 
      {
        StandardInventoryServer vpInvServ = Factory.create(StandardInventoryServer.class);
        if(!captive) // no moves, just delete the load and it's inventory
          vpInvServ.deleteLoad(loadID, ReasonCode.getAutoPickReasonCode());
        else // just delete the load's inventory
          vpInvServ.deleteAllItemsOnLoad(loadID, ReasonCode.getAutoPickReasonCode());
      }
      commitTransaction(tt);
      return true;
    }
    catch (DBException e)
    {
      logError("Pick server exception: " + e.getMessage());
      return false;
    }
    catch (Exception e)
    {
      logError("Pick server exception: " + e.getMessage());
      return false;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method retrieves a load record load id as key.
   *
   *  @param isLoadID <code>String</code> containing load to search for.
   *  @param inLock <code>int</code> flag indicating if record should be locked.
   *
   *  @return <code>LoadData</code> object. <code>null</code> if no record found.
   */
  protected LoadData getLoad(String isLoadID, int inLock) throws DBException
  {

    LoadData vpKey = Factory.create(LoadData.class);
    vpKey.setKey(LoadData.LOADID_NAME, isLoadID);
    LoadData myLoadData = Factory.create(Load.class).getElement(vpKey, inLock);

    return myLoadData;
  }

  /**
   * Method to unpick a load
   *
   * @param isLoadID containing ID of load to unpick
   * @param isReceivingStation
   * @param isOrderID
   * @param izPlaceOrderOnHold
   * @throws DBException
   */
  public void unpickLoad(String isLoadID, String isReceivingStation,
      String isOrderID, boolean izPlaceOrderOnHold) throws DBException
  {
    StandardOrderServer orderServ = Factory.create(StandardOrderServer.class);
    
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Load load = Factory.create(Load.class);
      LoadData lddata = getLoad(isLoadID, DBConstants.WRITELOCK);
      
      // unpick child loads
      List<Map> childLoads = load.getChildLoads(isLoadID);
      for(Iterator<Map> it = childLoads.iterator(); it.hasNext();)
      {
        unpickLoad(DBHelper.getStringField(it.next(), LoadData.LOADID_NAME),
            isReceivingStation, isOrderID, izPlaceOrderOnHold);
      }
  
      LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
      List<Map> loadLineItems = loadLineItem.getLoadLineItemDataListByLoadID(isLoadID);
      for(Iterator<Map> it = loadLineItems.iterator(); it.hasNext();)
      {
        LoadLineItemData lli = Factory.create(LoadLineItemData.class);
        lli.dataToSKDCData(it.next());
        unpickLoadLineItem(lli, isReceivingStation);
      }
      
      // If the user requested this order go back on hold, do so.
      if (izPlaceOrderOnHold && orderServ.getOrderStatusValue(isOrderID) != DBConstants.HOLD)
        orderServ.setOrderStatusValue(isOrderID, DBConstants.KILLED, DBConstants.HOLD);
      // Otherwise, delete the order.
      else
        orderServ.deleteOrder(isOrderID);
  
      // if this load has no child loads and has no line items then delete it
      LoadLineItemData lliSearch = Factory.create(LoadLineItemData.class);
      lliSearch.setKey(LoadLineItemData.LOADID_NAME, isLoadID);
      LoadData ldSearch = Factory.create(LoadData.class);
      ldSearch.setKey(LoadData.PARENTLOAD_NAME, isLoadID);

      if (!load.exists(ldSearch) && !loadLineItem.exists(lliSearch))
      {
        load.deleteLoad(isLoadID);
      }
     
      lddata.setKey(LoadData.LOADID_NAME, lddata.getLoadID());
      String stationWarehouse = Factory.create(Station.class).getStationWarehouse(isReceivingStation);
      String currentWarehouse = lddata.getWarehouse();
      String currentAddress = lddata.getAddress();
      
      String tranHist = "Move unpicked load " + isLoadID +
        " from " + currentWarehouse +
        "-" + currentAddress +
        " to " + stationWarehouse +
        "-" + isReceivingStation;
      logOperation(LogConsts.OPR_DSVR, tranHist);
  
      String parentLoad = LoadData.PARENTLOAD_NAME;

      lddata.setWarehouse(stationWarehouse);
      lddata.setAddress(isReceivingStation);
      lddata.setRouteID("");
      // Take the load off of any super-load
      lddata.setParentLoadID(lddata.getLoadID());
      // Change the status of the load to received.
      lddata.setLoadMoveStatus(DBConstants.RECEIVED);
      load.modifyElement(lddata);
      
      // if this loads has a parent load that has no child loads and has no line items
      // then delete the parent load
      if (parentLoad != null && parentLoad.length() > 0)
      {
        lliSearch.clearKeysColumns();
        lliSearch.setKey(LoadLineItemData.LOADID_NAME, parentLoad);
        ldSearch.clearKeysColumns();
        ldSearch.setKey(LoadData.LOADID_NAME, parentLoad);
        

        if (!load.exists(ldSearch) && !loadLineItem.exists(lliSearch))
        {
          load.deleteLoad(parentLoad);
        }
      }
      
//      Factory.create(StandardInventoryServer.class).setLocationEmptyStatus(currentWarehouse, currentAddress);
  
                                         // Log Inventory Transfer History
      tnData.clear();
      tnData.setTranCategory(DBConstants.LOAD_TRAN);
      tnData.setTranType(DBConstants.TRANSFER);
      tnData.setLoadID(isLoadID);
      tnData.setLocation(currentWarehouse + currentAddress);
      tnData.setToStation(isReceivingStation);
      tnData.setOrderID(isOrderID);
      logTransaction(tnData);
      commitTransaction(tt);
      return;
    }
    catch (DBException e)
    {
      logError("Pick server exception: " + e.getMessage());
      return;
    }
    catch (Exception e)
    {
      logError("Pick server exception: " + e.getMessage());
      return;
    }
    finally
    {
      endTransaction(tt);
    }
  }
  
  /**
  *  Method to unpick a load line item.
  *
  *  @param loadLineItemData LoadLineItem data object to be unpicked.
  *  @exception DBException
  */
  private void unpickLoadLineItem(LoadLineItemData loadLineItemData, String receivingStation) throws DBException
  {
    StandardDeallocationServer deallocServ = Factory.create(StandardDeallocationServer.class);
    
    // deallocate the load line item
    deallocServ.deallocateLoadLineItem(loadLineItemData);
    
    loadLineItemData.setHoldType(DBConstants.AVAILABLE);
    
    // add an item transfer transaction
    LoadData lddata = getLoad(loadLineItemData.getLoadID(), DBConstants.WRITELOCK);
    
    String tranHist = "Unpicked load line item " + loadLineItemData.getLoadID() + 
      ", " + loadLineItemData.getItem() +
      ", " + loadLineItemData.getLot() +
      " at " + lddata.getWarehouse() +
      "-" + lddata.getAddress() +
      " to " + receivingStation +
      " for order " + loadLineItemData.getOrderID();
    
    logOperation(LogConsts.OPR_DSVR, tranHist);
    
                                       // Log Inventory Transfer History
    tnData.clear();
    tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
    tnData.setTranType(DBConstants.TRANSFER);
    tnData.setLoadID(lddata.getLoadID());
    tnData.setLocation(lddata.getWarehouse() + lddata.getAddress());
    tnData.setToStation(receivingStation);
    tnData.setOrderID(loadLineItemData.getOrderID());
    tnData.setItem(loadLineItemData.getItem());
    tnData.setLot(loadLineItemData.getLot());
    tnData.setLineID(loadLineItemData.getLineID());
    tnData.setPickQuantity(loadLineItemData.getCurrentQuantity());
    logTransaction(tnData);
    
    // reset load line item or combine with a matching item

    LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
      // now see if this load line item already exists on the load
    LoadLineItemData llidataTo = loadLineItem.getLoadLineItemData(loadLineItemData.getItem(),
        loadLineItemData.getLot(), loadLineItemData.getLoadID(), "", "", null,loadLineItemData.getPositionID());
    if (llidataTo == null)  // need to just modify the this load line item
    {
      llidataSearch.setKey(LoadLineItemData.LOADID_NAME, loadLineItemData.getLoadID());
      llidataSearch.setKey(LoadLineItemData.ITEM_NAME, loadLineItemData.getItem());
      llidataSearch.setKey(LoadLineItemData.LOT_NAME, loadLineItemData.getLot());
      llidataSearch.setKey(LoadLineItemData.ORDERID_NAME, loadLineItemData.getOrderID());
      llidataSearch.setKey(LoadLineItemData.ORDERLOT_NAME, loadLineItemData.getOrderLot());
      llidataSearch.setKey(LoadLineItemData.LINEID_NAME, loadLineItemData.getLineID());
      llidataSearch.setOrderID("");
      llidataSearch.setOrderLot("");
      llidataSearch.setLineID("");
      llidataSearch.setAllocatedQuantity(0.0);
      loadLineItem.modifyElement(llidataSearch);
    }
    else
    {
      llidataTo.setKey(LoadLineItemData.LOADID_NAME, llidataTo.getLoadID());
      llidataTo.setKey(LoadLineItemData.ITEM_NAME, llidataTo.getItem());
      llidataTo.setKey(LoadLineItemData.LOT_NAME, llidataTo.getLot());
      llidataTo.setKey(LoadLineItemData.ORDERID_NAME, llidataTo.getOrderID());
      llidataTo.setKey(LoadLineItemData.ORDERLOT_NAME, llidataTo.getOrderLot());
      llidataTo.setKey(LoadLineItemData.LINEID_NAME, llidataTo.getLineID());
      llidataTo.setCurrentQuantity(llidataTo.getCurrentQuantity() +
      loadLineItemData.getCurrentQuantity());
      loadLineItem.modifyElement(llidataTo);
      loadLineItem.modifyElement(llidataSearch);
      loadLineItem.deleteLoadLineItem(loadLineItemData.getItem(),
      loadLineItemData.getLot(), loadLineItemData.getLoadID(),
      loadLineItemData.getOrderID(), loadLineItemData.getOrderLot(),
      loadLineItemData.getLineID(),loadLineItemData.getPositionID());
    }
  }

  public MoveData getNextItemPick(String isLoadID, Date ipSearchStart)
         throws DBException
  {
    mpMVData.clear();
    mpMVData.setKey(MoveData.PARENTLOAD_NAME, isLoadID);
    mpMVData.setKey(MoveData.MOVETYPE_NAME, DBConstants.ITEMMOVE);
    mpMVData.setKey(MoveData.MOVEDATE_NAME, ipSearchStart, KeyObject.GREATER_THAN,
                    KeyObject.AND);
    List<Map> vpMVList = mpMV.getAllElements(mpMVData);
    
    MoveData vpRtnData = null;
    if (!vpMVList.isEmpty())
    {
      mpMVData.dataToSKDCData(vpMVList.get(0));
      vpRtnData = mpMVData.clone();
    }

    return(vpRtnData);
  }
  
  public MoveData getPrevItemPick(String isLoadID, Date ipSearchStart)
         throws DBException
  {
    mpMVData.clear();
    mpMVData.setKey(MoveData.PARENTLOAD_NAME, isLoadID);
    mpMVData.setKey(MoveData.MOVETYPE_NAME, DBConstants.ITEMMOVE);
    mpMVData.setKey(MoveData.MOVEDATE_NAME, ipSearchStart, 
                    KeyObject.LESS_THAN_INCLUSIVE, KeyObject.AND);
    List<Map> vpMVList = mpMV.getAllElements(mpMVData);
    
    MoveData vpRtnData = null;
    if (!vpMVList.isEmpty())
    {
      mpMVData.dataToSKDCData(vpMVList.get(0));
      vpRtnData = mpMVData.clone();
    }

    return(vpRtnData);
  }
  
  /**
   * 
   * @param ipMoveData
   * @param isCurrentStation
   * @return
   * @throws DBException
   */
  protected String getNextAddressOnRouteForMove(MoveData ipMoveData, String isCurrentLoc)
    throws DBException
  {
    StandardRouteServer routeServer = Factory.create(StandardRouteServer.class);

    String nextAddress = "";

    /*
     * TODO: Make sure the MoveData's destination is populated.  Right now,
     * it isn't.  (Honestly, shouldn't this be part of the view rather than 
     * its own field?)
     * 
     * See if the load is at the final destination.
     */
    initializeOrderServer();
    OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
    ohdata = mpOrderServer.getOrderHeaderRecord(ipMoveData.getOrderID());
    if (ohdata != null)
    {
      if (ohdata.getDestAddress().equals(isCurrentLoc))
      {
        return isCurrentLoc;
      }
    }
    
    nextAddress = routeServer.getNextRouteDest(ipMoveData.getRouteID(),
        isCurrentLoc);
    
    return nextAddress;
  }
  
  protected MoveData deletePickedMove(String isParentLoad, String isOrderID, 
                                    LoadLineItemData ipItemDetail) throws DBException
  {
    Move vpMove = Factory.create(Move.class);
    MoveData vpMoveSrch = Factory.create(MoveData.class);
    
    vpMoveSrch.setKey(MoveData.PARENTLOAD_NAME, isParentLoad);
    vpMoveSrch.setKey(MoveData.LOADID_NAME,     ipItemDetail.getLoadID());
    vpMoveSrch.setKey(MoveData.ORDERID_NAME,    isOrderID);
    
    if (vpMove.getCount(vpMoveSrch) > 1)
    {                                  // It might be an Item move. Provide
                                       // additional keys to get unique rec.
      vpMoveSrch.setKey(MoveData.ITEM_NAME, ipItemDetail.getItem());
      vpMoveSrch.setKey(MoveData.PICKLOT_NAME, ipItemDetail.getLot());
    }
    MoveData vpMoveRecord = vpMove.getElement(vpMoveSrch, DBConstants.WRITELOCK);
    if (vpMoveRecord != null)
      vpMove.deleteElement(vpMoveSrch);
    
    return(vpMoveRecord);
  }

 /**
  * Method to do some final checks before deleting Auto-Pick Orders.
  * @param ipOHData Order header data.
  * @throws DBException if there is a DB error.
  */
  protected void deleteAutoPickOrderIfNecessary(OrderHeaderData ipOHData)
            throws DBException
  {
    initializeOrderServer();
    String vsOrderId = ipOHData.getOrderID();
    int vnOrdStat = mpOrderServer.getOrderStatusValue(vsOrderId);

    if ((vnOrdStat == DBConstants.KILLED ||vnOrdStat == DBConstants.SCHEDULED ||
         vnOrdStat == DBConstants.DONE) && !mpOrderServer.orderHasMoves(vsOrderId))
    {
      mpOrderServer.executeDeletion(ipOHData);
      if (mzHasHostSystem)
      {                                // Generate an order completion message.
        initializeHostServer();
        mpHostServ.sendOrderComplete(ipOHData);
      }
    }
  }
  
  /*========================================================================*/
  /*  Below is the start of a possibly vain attempt to gain some            */
  /* consistency and completeness in transaction history.                   */
  /*========================================================================*/
  
  /**
   * Log a pick to transaction history
   * @param ipMoveData
   * @param isToLoad
   * @param idQtyPicked
   */
  protected void logItemPickTransaction(MoveData ipMoveData, LoadData ipToLoadData, 
      double idQtyPicked)
  {
    String vsToLoad = "";
    String vsToLoc  = "";
    
    if (ipToLoadData != null)
    {
      vsToLoad = ipToLoadData.getLoadID();
      vsToLoc = ipToLoadData.getWarehouse() + ipToLoadData.getAddress();
    }
    
    logItemPickTransaction(ipMoveData.getLoadID(), 
        ipMoveData.getWarehouse() + ipMoveData.getAddress(), vsToLoad, vsToLoc,
        ipMoveData.getItem(), ipMoveData.getPickLot(), ipMoveData.getOrderID(), 
        ipMoveData.getLineID(), ipMoveData.getOrderLot(), idQtyPicked);
  }
  
  /**
   * Log a complete Item Pick transaction
   * 
   * @param isLoadID
   * @param isLocation Warehouse+Address
   * @param isToLoadID
   * @param isToLocation Warehouse+Address
   * @param isItem
   * @param isLot
   * @param isOrderID
   * @param isLineID
   * @param isOrderLot
   * @param idQtyPicked
   */
  protected void logItemPickTransaction(String isLoadID, String isLocation,
      String isToLoadID, String isToLocation, String isItem, String isLot,
      String isOrderID, String isLineID, String isOrderLot, double idQtyPicked)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
    tnData.setTranType(DBConstants.ITEM_PICK);
    tnData.setLoadID(isLoadID);
    tnData.setLocation(isLocation);
    tnData.setToLoadID(isToLoadID);
    tnData.setToLocation(isToLocation);
    tnData.setItem(isItem);
    tnData.setLot(isLot);
    tnData.setOrderID(isOrderID);
    tnData.setOrderLot(isOrderLot);
    tnData.setLineID(isLineID);
    tnData.setPickQuantity(idQtyPicked);
    logTransaction(tnData);
  }
  
  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/
  protected void initializeOrderServer()
  {
    if (mpOrderServer == null)
    {
      mpOrderServer = Factory.create(StandardOrderServer.class, getClass().getSimpleName());
    }
  }
  
  protected void initializeAllocationServer()
  {
    if (mpAllocServ == null)
    {
      mpAllocServ = Factory.create(StandardAllocationServer.class);
    }
  }

  protected void initializeHostServer()
  {
    if (mpHostServ == null)
    {
      mpHostServ = Factory.create(StandardHostServer.class);
    }
  }
  
  /**
   * PickLabelInfo
   * <B>Description:</B> Class to support reprinting labels
   *
   * @author       mandrus<BR>
   * @version      1.0
   * 
   * <BR>Copyright (c) 2008 by Daifuku America Corporation
   */
  private class PickLabelInfo
  {
    MoveData mpLabelMove = null;
    String msLabelToLoad = null;
    double mdLabelQuantity = 0;
    Date mpLabelDate = null;

    /**
     * Constructor
     * 
     * @param ipMoveData
     * @param isToLoad
     * @param idQtyPicked
     * @param ipDate
     */
    public PickLabelInfo(MoveData ipMoveData, String isToLoad,
        double idQtyPicked, Date ipDate)
    {
      mpLabelMove = ipMoveData.clone();
      msLabelToLoad = isToLoad;
      mdLabelQuantity = idQtyPicked;
      mpLabelDate = (Date)ipDate.clone();
    }
  }
}
