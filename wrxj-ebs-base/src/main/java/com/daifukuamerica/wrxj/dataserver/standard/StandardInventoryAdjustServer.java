package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import java.util.Date;

/**
 * A server that provides methods and transactions for use in inventory
 * management. Methods used to add, modify and delete containers, loads, item
 * masters and details are provided. Transactions are wrapped around calls to
 * the lower level data base objects.
 * 
 * @author avt
 * @version 1.0
 */
public class StandardInventoryAdjustServer extends StandardServer
{
  protected StandardHostServer mpHostServ = null;
  
    //   Public Methods for Inventory Adjust Server
  public StandardInventoryAdjustServer()
  {
    this(null);
  }

  public StandardInventoryAdjustServer(String keyName)
  {
    super(keyName);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardInventoryAdjustServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
  }

 
  /**
   *  Method to adjust load line item quantity if needed.
   *
   *  @param userID User ID.
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param order Order number.
   *  @param lineID Ordered line number.
   *  @param orderLot Ordered lot number.
   *  @param newQuantity Quantity remaining.
   *  @param reasonCode Reason for adjusting this line item.
   *  @exception DBException
   */
  public void adjustLoadLineItemIfNeeded(String userID, String loadID, String item, String lot, 
      String order, String orderLot, String lineid, String positionID, double newQuantity, String reasonCode) throws DBException
  {
    adjustLoadLineItemIfNeeded(userID, loadID, item, lot, order, orderLot, lineid, positionID, newQuantity, 0, reasonCode);
  }

  /**
   *  Method to adjust load line item quantity if needed.
   *
   *  @param userID User ID.
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param order Order number.
   *  @param orderLot Ordered lot number.
   *  @param lineID Ordered line number.
   *  @param newQuantity Quantity remaining.
   *  @param cciMoveID Cycle count move ID.
   *  @param reasonCode Reason for adjusting this line item.
   *  @exception DBException
   */
  public void adjustLoadLineItemIfNeeded(String userID, String loadID, String item,
          String lot, String order, String orderLot, String lineID, String positionID, double newQuantity, 
          int cciMoveID, String reasonCode) throws DBException
  {
    adjustLoadLineItemIfNeeded(userID, loadID, item, lot, order, orderLot, lineID, positionID, newQuantity,
        cciMoveID, reasonCode, true, true);
  }

  /**
   *  Method to adjust load line item quantity if needed.
   *
   *  @param userID User ID.
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param order Order number.
   *  @param orderLot Ordered lot number.
   *  @param lineID Ordered line number.
   *  @param newQuantity Quantity remaining.
   *  @param cciMoveID Cycle count move ID.
   *  @param reasonCode Reason for adjusting this line item.
   *  @param izSendHost Send adjustment to host.
   *  @param izAddTrans add transaction history.
   *  @exception DBException
   */
  public void adjustLoadLineItemIfNeeded(String userID, String loadID, String item,
          String lot, String order, String orderLot, String lineID, String positionID, double newQuantity, 
          int cciMoveID, String reasonCode, boolean izSendHost, boolean izAddTrans) throws DBException
      {
    String useOrder = null;
    String useOrderLot = null;
    String useLineID = null;
        // If this is NOT a cycle count set the order info so we can 
        // use it for the llidata queries...
        // If it IS a cycle count, leave them blank for the queries but 
        // use them for the transaction
    if(cciMoveID == 0)      // Not a Cycle Count
    {
      useOrder = order;
      useOrderLot = orderLot;
      useLineID = lineID;
    }

    TransactionToken tt = null;
    LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
    LoadLineItemData llidata = null;
    
    try
    {
      tt = startTransaction();
      llidata = loadLineItem.getLoadLineItemData(item, lot, loadID, useOrder, useOrderLot, useLineID, positionID);

      if (llidata != null)
      {
        double oldQuantity = llidata.getCurrentQuantity();
        if (oldQuantity != newQuantity || oldQuantity == 0.0)
        {
          // quantity has changed lets adjust it
          // if quantity has increased just make adjustment
          // if quantity has decreased we may need to back of moves for orders
          // if quantity is now zero we will delete the load line item
          if (llidata.getAllocatedQuantity() > newQuantity)
          {
            // back off moves until we have sufficient quantity to satisfy moves
            logDebug("StandardInventoryAdjustServer.createDeallocationServer()");
            StandardDeallocationServer dealServer = Factory.create(StandardDeallocationServer.class);
            dealServer.deallocateIDMoves(loadID, item, lot,
                         (llidata.getAllocatedQuantity() - newQuantity));
          }

          if (newQuantity > 0.0)
          {
            LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
            llidataSearch.setKey(LoadLineItemData.LOADID_NAME, loadID);
            llidataSearch.setKey(LoadLineItemData.ITEM_NAME, item);
            llidataSearch.setKey(LoadLineItemData.LOT_NAME, lot);
            llidataSearch.setKey(LoadLineItemData.ORDERID_NAME, useOrder);
            llidataSearch.setKey(LoadLineItemData.ORDERLOT_NAME, useOrderLot);
            llidataSearch.setKey(LoadLineItemData.LINEID_NAME, useLineID);
            llidataSearch.setKey(LoadLineItemData.POSITIONID_NAME, positionID);
            llidataSearch.setCurrentQuantity(newQuantity);
            llidataSearch.setLastCCIDate(new Date());
            loadLineItem.modifyElement(llidataSearch);
          }
          else
          {
            loadLineItem.deleteLoadLineItem(item, lot, loadID, useOrder, useOrderLot, useLineID, positionID);
            checkLoadAmountFull(loadID);
          }
        }
        else
        {
          LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
          llidataSearch.setKey(LoadLineItemData.LOADID_NAME, loadID);
          llidataSearch.setKey(LoadLineItemData.ITEM_NAME, item);
          llidataSearch.setKey(LoadLineItemData.LOT_NAME, lot);
          llidataSearch.setKey(LoadLineItemData.ORDERID_NAME, useOrder);
          llidataSearch.setKey(LoadLineItemData.ORDERLOT_NAME, useOrderLot);
          llidataSearch.setKey(LoadLineItemData.LINEID_NAME, useLineID);
          llidataSearch.setKey(LoadLineItemData.POSITIONID_NAME, positionID);
          llidataSearch.setLastCCIDate(new Date());
          loadLineItem.modifyElement(llidataSearch);
        }

        // log the transaction if there is a Move ID or if the quantity changed
        int viTransactionType = -1;
        tnData.clear();
        tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
        tnData.setLoadID(loadID);
        tnData.setItem(item);
        tnData.setLot(lot);
        tnData.setOrderID(order);
        tnData.setReasonCode(reasonCode);
        tnData.setAdjustedQuantity(newQuantity - oldQuantity);
        tnData.setCurrentQuantity(oldQuantity);
        tnData.setUserID(userID);
        
        if (oldQuantity != newQuantity)
        {   
          /*
           * Log the location, too
           */
          LoadData vpLD = Factory.create(StandardLoadServer.class).getLoad(loadID);
          if (vpLD != null)
          {
            tnData.setLocation(vpLD.getWarehouse(), vpLD.getAddress());
          }

          if (cciMoveID != 0)
          {           
            logOperation("LoadId \"" + loadID +
              "\" CCI: item '" + item +
              "' lot '" + lot +
              "' old quantity " + oldQuantity +
              " new quantity " + newQuantity +
              " by '" + userID + "'");
            viTransactionType = DBConstants.CYCLE_COUNT;
              
          }
          else if (newQuantity > 0.0)
          {
            logOperation("Inventory Adjust: item '" + item +
              "' lot '" + lot +
              "' load '" + loadID +
              "' quantity changed from " + oldQuantity +
              " to " + newQuantity);

            viTransactionType = 
              newQuantity > oldQuantity ? DBConstants.ADD_ITEM : DBConstants.DELETE_ITEM;
          }
          else
          {
            logOperation("Inventory Deleted: item '" + item +
              "' lot '" + lot +
              "' load '" + loadID +
              "' quantity " + oldQuantity);
            viTransactionType = DBConstants.DELETE_ITEM;
          }
          
          if (mzHasHostSystem && izSendHost)
          {
            llidata.setCurrentQuantity(newQuantity - oldQuantity);
            initializeHostServer();
            mpHostServ.sendInventoryAdjust(llidata, reasonCode);
          }
        }
        else if (cciMoveID != 0)
        {
          logOperation("LoadId \"" + loadID +
              "\" CCI: item '" + item +
              "' lot '" + lot +
              "' old quantity " + oldQuantity +
              " new quantity " + newQuantity +
              " by '" + userID + "'");
          viTransactionType = DBConstants.CYCLE_COUNT;
        }
        if (viTransactionType != -1 && izAddTrans)
        {
           tnData.setTranType(viTransactionType);
           logTransaction(tnData);
        }
      }
      else if (newQuantity > 0.0)
      {
        // load line item does not currently exist
        // it must be added
        llidata = new LoadLineItemData();
        llidata.setLoadID(loadID);
        llidata.setItem(item);
        llidata.setLot(lot);
        llidata.setOrderID(order);
        llidata.setOrderLot(orderLot);
        llidata.setLineID(lineID);
        llidata.setCurrentQuantity(newQuantity);
        
        StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);
        invtServ.addLoadLIWithValidation(llidata, MessageOutNames.INVENTORY_ADJUST, reasonCode);
        
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
   *  Method to set the loads amount full value.
   *
   *  @param loadid Load ID to be set.
   *  @exception DBException
   */
  public void checkLoadAmountFull(String loadid) throws DBException
  {
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
    LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
    // if no items on load, set load to Empty
    llidataSearch.setKey(LoadLineItemData.LOADID_NAME, loadid);
    if (loadLineItem.getCount(llidataSearch) == 0)
    {
      Load load = Factory.create(Load.class);
      load.updateLoadAmountFull(loadid, DBConstants.EMPTY);
    }
  }

  /**
   *  Method to get a choice list for reason codes.
   *
   *  @param int iReasonCategory - the category for which to get the reason codes.
   *  @exception DBException
   */
  public String[] getReasonCodeChoiceList(int iReasonCategory) throws DBException
  {
    ReasonCode reasonCode = Factory.create(ReasonCode.class);
    return reasonCode.getReasonCodeChoices(iReasonCategory);
  }
 
  protected void initializeHostServer()
  {
    if (mpHostServ == null)
    {
      mpHostServ = Factory.create(StandardHostServer.class);
    }
  }  
}
