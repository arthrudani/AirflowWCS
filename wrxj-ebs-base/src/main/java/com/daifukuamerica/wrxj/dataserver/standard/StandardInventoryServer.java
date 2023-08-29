package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Alerts;
import com.daifukuamerica.wrxj.dbadapter.data.ContainerType;
import com.daifukuamerica.wrxj.dbadapter.data.ContainerTypeData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistoryData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCodeData;
import com.daifukuamerica.wrxj.dbadapter.data.Synonym;
import com.daifukuamerica.wrxj.dbadapter.data.SynonymData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.MessageNameEnum;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A server that provides methods and transactions for use in inventory
 * management. Methods used to add, modify and delete containers, loads, item
 * masters and details are provided. Transactions are wrapped around calls to
 * the lower level data base objects.
 *
 * @author avt
 * @version 1.0
 */
public class StandardInventoryServer extends StandardServer
{
  private final int MAX_RANDOM_TRIES = 20;

  protected final String TEMP_LOAD_ITEM = "TEMPORARY_ITEM";
  protected final String BIN_FULL_ITEM = "BIN_FULL_ITEM";
  protected final String BIN_EMPTY_ITEM = "BIN_EMPTY_ITEM";
  protected final String BIN_HEIGHT_ITEM = "BIN_HEIGHT_ITEM";

  protected LoadLineItem mpLLI = Factory.create(LoadLineItem.class);
  protected ItemMaster mpIM = Factory.create(ItemMaster.class);
  protected Load mpLoad = Factory.create(Load.class);
  
  protected StandardHostServer mpHostServ = null;

  /**
   * Constructor
   */
  public StandardInventoryServer()
  {
    this(null);
  }

  /**
   * Constructor
   *
   * @param keyName
   */
  public StandardInventoryServer(String keyName)
  {
    super(keyName);
    logDebug("Creating " + getClass().getSimpleName());
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardInventoryServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  logDebug("Creating " + getClass().getSimpleName());
	  
  }

  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Equipment.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();

    if (mpHostServ != null)
    {
      mpHostServ.cleanUp();
      mpHostServ = null;
    }
  }

  /**
   *  Method to add a load line item without validation.
   *
   *  @param lli Filled in load line item data object.
   *  @exception DBException
   */
  public void addLoadLI(LoadLineItemData lli) throws DBException
  {
    addLoadLI(lli, true);
  }

  /**
   *  Method to add a load line item without validation.
   *
   *  @param lli Filled in load line item data object.
   *  @exception DBException
   */
  public void addLoadLI(LoadLineItemData lli, boolean izLogTransaction)
    throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
                                       // Lock the load record.
      LoadData lddata = getInventoryLoad(lli.getLoadID(), DBConstants.WRITELOCK);
      if (lddata == null)
      {
        throw new DBException("Load "+lli.getLoadID()+" does not exist");
      }

      mpLLI.addLoadLineItem(lli);
      logOperation(LogConsts.OPR_DSVR, "LoadId \"" + lli.getLoadID()
          + "\" - Added Item: " + lli.getItem() + " Lot: " + lli.getLot()
          + ", Qty: " + lli.getCurrentQuantity());

      // update load amountFull if load is empty
      if (lddata.getAmountFull() == DBConstants.EMPTY)
      {
        lddata.setKey(LoadData.LOADID_NAME, lddata.getLoadID());
        lddata.setAmountFull(DBConstants.ONEQUARTER);
        mpLoad.modifyElement(lddata);
      }

      commitTransaction(tt);

      // Record Inventory Add transaction.
      if (izLogTransaction)
      {
        tnData.clear();
        tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
        tnData.setTranType(DBConstants.ADD_ITEM);
        tnData.setLoadID(lli.getLoadID());
        tnData.setLocation(lddata.getWarehouse(), lddata.getAddress());
        tnData.setToLocation(lddata.getNextWarehouse(), lddata.getNextAddress());
        tnData.setItem(lli.getItem());
        tnData.setLot(lli.getLot());
        tnData.setCurrentQuantity(0.0);
        tnData.setAdjustedQuantity(lli.getCurrentQuantity());
        tnData.setAgingDate(lli.getAgingDate());
        tnData.setReasonCode(lli.getHoldReason());
        tnData.setHoldType(lli.getHoldType());
        tnData.setExpirationDate(lli.getExpirationDate());
        tnData.setOrderID(lli.getOrderID());
        tnData.setOrderLot(lli.getOrderLot());
        tnData.setLineID(lli.getLineID());
        logTransaction(tnData);
      }
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Add a load line item as part of the receiving process.  The caller
   * is responsible for logging transaction history as well as any host
   * messaging.
   *
   * @param ipLLI
   * @throws DBException
   */
  public void addReceivingLoadLI(LoadLineItemData ipLLI) throws DBException
  {
    addLoadLIWithValidation(ipLLI, null, null, false);
  }

  /**
   *  Method to add a load line item with validation.
   *
   *  @param lli Filled in load line item data object.
   *  @param messageName the name of the host message to send. This is either
   *         ReceiveComplete, or InventoryAdjust depending on if the load is
   *         being stored into the system or adjusted.
   *  @exception DBException for database access errors.
   */
  public void addLoadLIWithValidation(LoadLineItemData lli,
      MessageNameEnum messageName) throws DBException
  {
    addLoadLIWithValidation(lli, messageName,
        ReasonCode.getDefaultInvAdjustReasonCode(), true);
  }

  /**
   *  Method to add a load line item with validation.
   *
   *  @param lli Filled in load line item data object.
   *  @param messageName the name of the host message to send. This is either
   *         ReceiveComplete, or InventoryAdjust depending on if the load is
   *         being stored into the system or adjusted.
   *  @param vsReason - The reason code to send to the host
   *  @exception DBException for database access errors.
   */
  public void addLoadLIWithValidation(LoadLineItemData lli, MessageNameEnum messageName,
                                      String vsReason) throws DBException
  {
    addLoadLIWithValidation(lli, messageName, vsReason, true);
  }

  /**
   *  Method to add a load line item with validation.
   *
   *  @param lli Filled in load line item data object.
   *  @param messageName the name of the host message to send. This is either
   *         ReceiveComplete, or InventoryAdjust depending on if the load is
   *         being stored into the system or adjusted.
   *  @param vsReason - The reason code to send to the host
   *  @param izLogTransaction - false if the caller will log the transaction
   *  @throws DBException for database access errors.
   */
  protected void addLoadLIWithValidation(LoadLineItemData lli, MessageNameEnum messageName,
                                       String vsReason, boolean izLogTransaction)
          throws DBException
  {
    String errorMsg = "";

      // Validate that the the quantity is greater than zero,
      // that the load exists, the item master exists,
      // that the item/lot combination does not already exist on this load
      // and that this item matches recommended warehouse and zone

    if (!(lli.getCurrentQuantity() > 0))
    {
      errorMsg = "Quantity must be greater than zero";
    }
    else if (!mpLoad.exists(lli.getLoadID()))
    {
      errorMsg = "Load does not exist";
    }
    else if (mpIM.getItemMasterData(lli.getItem()) == null)
    {
      errorMsg = "Item does not exist";
    }
    else
    {
      LoadLineItemData vpIDData = Factory.create(LoadLineItemData.class);
      vpIDData.setKey(LoadLineItemData.LOADID_NAME, lli.getLoadID());
      vpIDData.setKey(LoadLineItemData.ITEM_NAME, lli.getItem());
      vpIDData.setKey(LoadLineItemData.LOT_NAME, lli.getLot());
      vpIDData.setKey(LoadLineItemData.LINEID_NAME, lli.getLineID());
      vpIDData.setKey(LoadLineItemData.POSITIONID_NAME, lli.getPositionID());
      vpIDData.setKey(LoadLineItemData.ORDERID_NAME, lli.getOrderID());
      vpIDData.setKey(LoadLineItemData.ORDERLOT_NAME, lli.getOrderLot());
      if (mpLLI.exists(vpIDData))
      {
        LoadLineItemData vpExistingLLI;
        vpExistingLLI = getLoadLineItem(lli.getLoadID(), lli.getItem(),
                                         lli.getLot(), lli.getLineID(), lli.getOrderID(),
                                         lli.getOrderLot(),
                                         lli.getPositionID());

        vpExistingLLI.setCurrentQuantity(vpExistingLLI.getCurrentQuantity() +
                                         lli.getCurrentQuantity());
        boolean vzSendIAdj = (messageName == null ? true : false);
        updateLoadLineItemInfo(vpExistingLLI,
            ReasonCode.getDefaultInvAdjustReasonCode(), false, vzSendIAdj);
      }
      else
      {
        addLoadLI(lli, izLogTransaction);
      }

/*----------------------------------------------------------------------------
     Send the host a message if there is a host system, and a message name
     is specified.
  ----------------------------------------------------------------------------*/
      if (mzHasHostSystem && messageName != null)
      {
        if (messageName == MessageOutNames.STORE_COMPLETE)
        {
          String vsLoadLocn = mpLoad.getLoadLocation(lli.getLoadID());
          String[] vpLoadLocn = Location.parseLocation(vsLoadLocn);
          if(mzHasHostSystem)
          {
            initializeHostServer();
            mpHostServ.sendStoreComplete(null, vpLoadLocn[1], lli);
          }
        }
        else if (messageName == MessageOutNames.INVENTORY_ADJUST)
        {
          if (mzHasHostSystem)
          {
            initializeHostServer();
            mpHostServ.sendInventoryAdjust(lli, vsReason);
          }
        }
        else
        {
          logError("Unexpected host event type to publish.");
        }
      }
    }

    if (errorMsg.length() > 0)
    {
      DBHelper.dbThrow(errorMsg);
    }
  }

  /**
   *  Method to add a BIN_FULL_ITEM to a load.
   *
   *  @param loadID no information available
   */
  public void addBinFullItem(String loadID)
  {
    TransactionToken tt = null;

    try
    {
      tt = startTransaction();
      final String binFull = getBinFullItemName();
      LoadLineItemData binFullItem = Factory.create(LoadLineItemData.class);
      binFullItem.setLoadID(loadID);
      binFullItem.setItem(binFull);
      binFullItem.setCurrentQuantity(1);
      if (!mpIM.exists(binFull))
      {
        addItemMasterFromString(binFull, "Default Bin Full Item", false);
      }
      addLoadLI(binFullItem);

      if(sendIAForBEBF())
      {
        if (mzHasHostSystem)
        {
          initializeHostServer();
          mpHostServ.sendInventoryAdjust(binFullItem, "");
        }
      }

      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception adding Bin Full Item to Load  \"" +
                     loadID + "\"  - StandardInventoryServer.addBinFullItem");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to add a BIN_HEIGHT_ITEM to a load.
   *
   * @param loadID no information available
   */
  public void addBinHeightItem(String loadID)
  {
    TransactionToken tt = null;

    try
    {
      tt = startTransaction();
      final String binHeight = getBinHeightItemName();
      LoadLineItemData vpBinHeightItem = Factory.create(LoadLineItemData.class);
      vpBinHeightItem.setLoadID(loadID);
      vpBinHeightItem.setItem(binHeight);
      vpBinHeightItem.setCurrentQuantity(1);
      if (mpIM.exists(binHeight) == false)
      {
        addItemMasterFromString(binHeight, "Default Bin Incorrect Height Item", false);
      }
      LoadLineItemData vpLLI = getLoadLineItem(loadID, binHeight, "", "", "","","");
      if (vpLLI == null)
      {
        addLoadLI(vpBinHeightItem);
      }
      else
      {
        vpLLI.setCurrentQuantity(vpLLI.getCurrentQuantity() + 1);
        updateID(vpLLI);
      }
      commitTransaction(tt);
    }
    catch (DBException e)
    {
        logException(e, "Exception adding Bin Full Item to Load  \"" +
                           loadID + "\"  - StandardInventoryServer.addBinFullItem");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Determines if IA messages should be sent for Bin Empty and Bin Full item
   * adds. Baseline default is to always return false;
   *
   * @return false
   */
  protected boolean sendIAForBEBF()
  {
    return false;
  }

  /**
   * Method to add a BIN_EMPTY_ITEM to a load.
   *
   * @param loadID no information available
   */
  public void addBinEmptyItem(String loadID)
  {
    logDebug("StandardInventoryServer.addBinEmptyItem() - For Load " + loadID);
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      final String binEmpty = getBinEmptyItemName();
      LoadLineItemData binEmptyItem = Factory.create(LoadLineItemData.class);
      binEmptyItem.setLoadID(loadID);
      binEmptyItem.setItem(binEmpty);
      binEmptyItem.setCurrentQuantity(1);
      if (mpIM.exists(binEmpty) == false)
      {
        logDebug("StandardInventoryServer.addBinEmptyItem() - BIN_EMPTY_ITEM Doesn't Exist create it ");
        addItemMasterFromString(binEmpty, "Default Bin Empty Item", false);
      }
      LoadLineItemData vpLLI = getLoadLineItem(loadID, binEmpty, "", "", "","","");
      if (vpLLI == null)
      {
        addLoadLI(binEmptyItem);

        if(sendIAForBEBF())
        {
          if (mzHasHostSystem)
          {
            initializeHostServer();
            mpHostServ.sendInventoryAdjust(binEmptyItem, "");
          }
        }
      }
      else
      {
        vpLLI.setCurrentQuantity(vpLLI.getCurrentQuantity() + 1);
        updateID(vpLLI);
      }
      commitTransaction(tt);
    }
    catch (DBException e)
    {
        logDebug("StandardInventoryServer.addBinEmptyItem() - Error adding item ");
        logException(e, "Exception adding Bin Empty Item to Load  \"" +
                           loadID + "\"  - StandardInventoryServer.addBinFullItem");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to add a TEMP_LOAD_ITEM to a load.
   *
   * @param loadID to add the TEMP_LOAD_ITEM to.
   */
  public void addTemporaryItem(String loadID)
  {
    TransactionToken tt = null;

    try
    {
      tt = startTransaction();
      ItemMasterData vpItem = getTemporaryItemData();
      LoadLineItemData tempLoadItem = Factory.create(LoadLineItemData.class);
      tempLoadItem.setLoadID(loadID);
      tempLoadItem.setItem(vpItem.getItem());
      tempLoadItem.setCurrentQuantity(1.0);
      LoadLineItemData vpLLI = getLoadLineItem(vpItem.getItem(), "", loadID, "", "","","");
      if (vpLLI == null)
      {
        addLoadLI(tempLoadItem);
      }
      else
      {
        vpLLI.setCurrentQuantity(vpLLI.getCurrentQuantity() + 1);
        updateID(vpLLI);
      }
      commitTransaction(tt);
    }
    catch (DBException e)
    {
        logException(e, "Exception adding Temporary Load Item to Load  \"" +
                           loadID + "\"  - StandardInventoryServer.addTemporaryItem");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to add default item master record.
   * @param isItem the item to add.
   * @throws DBException if there is a transaction error.
   */
  public void addDefaultItem(String isItem) throws DBException
  {
    TransactionToken vpTT = null;
    try
    {
      vpTT = startTransaction();
      mpIM.addDefaultItemMaster(isItem);
      commitTransaction(vpTT);
    }
    finally
    {
      endTransaction(vpTT);
    }
  }

  /**
   * Fetches item master record related to temporary load Item detail.
   *
   * @return Item master record of temporary load item.
   * @throws DBException if there is a DB access error.
   */
  public ItemMasterData getTemporaryItemData() throws DBException
  {
    final String vsItem = getDefaultItemName();
    if (!mpIM.exists(vsItem))
    {
      addItemMasterFromString(vsItem, "Default Temporary Empty Item", false);
    }
    return getItemMasterData(vsItem);
  }

  /**
   * Method returns temporary/default item master name.
   *
   * @return Item name.
   */
  public String getDefaultItemName()
  {
    return TEMP_LOAD_ITEM;
  }

  /**
   * Method returns Bin_Empty item master name
   *
   * @return Item Name
   */
  public String getBinEmptyItemName()
  {
    return BIN_EMPTY_ITEM;
  }

  /**
   * Method returns Bin_Full item master name
   *
   * @return Item Name
   */
  public String getBinFullItemName()
  {
    return BIN_FULL_ITEM;
  }

  /**
   * Method returns Bin_Height item master name
   *
   * @return Item Name
   */
  public String getBinHeightItemName()
  {
    return BIN_HEIGHT_ITEM;
  }

  /**
   *  Method to add a item to a load.
   *
   *  @param loadID of load to add item to
   *  @param newItem is the item to add to the load
   *  @param itemQty is the quantity to add to the load
   */
  public void addItemToLoad(String loadID, String newItem, double itemQty,
      boolean izDeleteAtZero)
  {
    addItemToLoad(loadID, newItem, itemQty, 0, "", izDeleteAtZero);
  }

  /**
   *  Method to add a item to a load.
   *
   *  @param loadID of load to add item to
   *  @param newItem is the item to add to the load
   *  @param itemQty is the quantity to add to the load
   *  @param allocQty is the quantity already allocated
   *  @param orderID is the order for the allocated quantity
   *  @param izDeleteAtZero set to true if item master should be deleted after
   *        reaching zero qty.
   */
  public void addItemToLoad(String loadID, String newItem, double itemQty,
                            double allocQty, String orderID,
                            boolean izDeleteAtZero)
  {
    TransactionToken tt = null;

    try
    {
      tt = startTransaction();
      LoadLineItemData loadItem = Factory.create(LoadLineItemData.class);
      loadItem.setLoadID(loadID);
      loadItem.setItem(newItem);
      loadItem.setCurrentQuantity(itemQty);
      loadItem.setAllocatedQuantity(allocQty);
      loadItem.setOrderID(orderID);
      if (!mpIM.exists(newItem))
      {
        addItemMasterFromString(newItem, "Auto Store Item", izDeleteAtZero);
      }
      this.addLoadLI(loadItem);
      if (mzHasHostSystem)
      {
        initializeHostServer();
        mpHostServ.sendInventoryAdjust(loadItem,
            ReasonCode.getDefaultInvAdjustReasonCode());
      }
      commitTransaction(tt);
    }
    catch (DBException e)
    {
        logException(e, "Exception adding Auto Store Item to Load  \"" +
                           loadID + "\"  - StandardInventoryServer.addItemToLoad");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to add a DAC_TEST_ITEM to a load.
   *
   * @param loadID
   */
  @UnusedMethod
  public void addTestItem(String loadID)
  {
    TransactionToken tt = null;

    try
    {
      tt = startTransaction();
      String vsTestItem = "DAC_TEST_ITEM";
      LoadLineItemData vpTestLoadItem = Factory.create(LoadLineItemData.class);
      vpTestLoadItem.setLoadID(loadID);
      vpTestLoadItem.setItem(vsTestItem);
      if (!mpIM.exists(vsTestItem))
      {
        addItemMasterFromString(vsTestItem, "Default Testing Item", true);
      }
      addLoadLI(vpTestLoadItem);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception adding Test Item to Load  \"" + loadID
          + "\"  - " + getClass().getSimpleName() + ".addTestItem");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to see if a load can contain the specified item.
   *
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param recommendedWarehouse Recommended warehouse.
   *  @param recommendedZone Recommended zone.
   *  @param storageFlag Storage flag to use in checking.
   *  @return boolean of <code>true</code> if item can be put on load.
   *  @exception DBException for database error
   *  @exception IllegalStateException with reason if item cannot be put on load.
   */
  @UnusedMethod
  public boolean loadCanContainItem(String loadID, String item, String lot,
            String recommendedWarehouse, String recommendedZone, int storageFlag) throws DBException
  {
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
    TableJoin tableJoin = Factory.create(TableJoin.class);
                                       // One Item and ione lot per load.
    if (storageFlag == DBConstants.ONELOT_ONEITEM)
    {
      llidataSearch.setKey(LoadLineItemData.LOADID_NAME, loadID);
      switch (mpLLI.getCount(llidataSearch))
      {
        case 0: // okay
          break;
        case 1: // better be an exact match
          llidataSearch.setKey(LoadLineItemData.ITEM_NAME, item);
          llidataSearch.setKey(LoadLineItemData.LOT_NAME, lot);
          if (!mpLLI.exists(llidataSearch))
          {  // doesnt match, not allowed with other items/lots
            throw new DBException("Other item and/or lot exist on this load", DBException.ITEMCANNOTGOINLOAD);
          }
          break;
        default : // too many load line items
          throw new DBException("Item Storage flag does not allow other items and/or lots to exist on this load", DBException.ITEMCANNOTGOINLOAD);
      }  //switch
    }                                  // One item belonging to multiple lots.
    else if (storageFlag == DBConstants.MIXLOTS_ONEITEM)
    {
      if (mpLLI.existsOtherItems(item, loadID))
      {  // doesnt match item, not allowed with other items
        throw new DBException("Item Storage flag does not allow other items to exist on this load", DBException.ITEMCANNOTGOINLOAD);
      }
    }                                  // All items belong to the same lot.
    else if (storageFlag == DBConstants.ONELOT_PERITEM)
    {
      if (mpLLI.existsOtherLots(item, lot, loadID))
      {
        throw new DBException("Item Storage flag does not allow other lots to exist on this load", DBException.ITEMCANNOTGOINLOAD);
      }
    }
    if (SKDCUtility.isFilledIn(recommendedWarehouse))
    { // must match with any load line items that have it specified
      String rWar = tableJoin.getLoadsRecommendedWarehouse(loadID);
      if (SKDCUtility.isFilledIn(rWar) && (!rWar.equals(recommendedWarehouse)))
      {  // not allowed
        throw new DBException("Cannot mix recommended warehouses in same load", DBException.ITEMCANNOTGOINLOAD);
      }
    }
    if (SKDCUtility.isFilledIn(recommendedZone))
    { // must match with any load line items that have it specified
      String rZone = tableJoin.getLoadsRecommendedZone(loadID);
      if (SKDCUtility.isFilledIn(rZone) && (!rZone.equals(recommendedWarehouse)))
      {  // not allowed
        throw new DBException("Cannot mix recommended zones in same load", DBException.ITEMCANNOTGOINLOAD);
      }
    }

    return true;
  }

  /**
   *  Method to see if an item is below the CCI point.
   *
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @return boolean of <code>true</code> if item quantity is below the CCI level.
   *  @exception DBException
   */
  public double needsCCI(String loadID, String item, String lot,
      String positionID) throws DBException
  {
    double result = -10.0;

    ItemMasterData vpIMData = mpIM.getItemMasterData(item);

    LoadLineItemData llidata;
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
    llidataSearch.setKey(LoadLineItemData.LOADID_NAME, loadID);
    llidataSearch.setKey(LoadLineItemData.ITEM_NAME, item);
    llidataSearch.setKey(LoadLineItemData.LOT_NAME, lot);
    llidataSearch.setKey(LoadLineItemData.POSITIONID_NAME, positionID);
    llidata = mpLLI.getElement(llidataSearch, DBConstants.NOWRITELOCK);
    if (llidata != null)
    {
      if ((vpIMData != null) && (vpIMData.getCCIPointQuantity() > 0) &&
          (llidata.getCurrentQuantity() < vpIMData.getCCIPointQuantity()))
      {
        result = llidata.getCurrentQuantity();
      }
    }
    else
    {                                  // Item detail was picked to zero
                                       // apparently. If IM CCI point is greater
                                       // than 0 have them verify this.
      if (vpIMData.getCCIPointQuantity() > 0) result = 0;
    }

    return result;
  }

  /**
   *  Method to delete load line item.
   *
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param order Order number.
   *  @param orderLot Ordered lot number.
   *  @exception DBException
   */
  public void deleteLoadLineItem(String loadID, String item, String lot,
      String order, String orderLot, String lineid, String positionID,
      String reasonCode)      throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      LoadLineItemData llidata = mpLLI.getLoadLineItemData(item, lot,
                      loadID, order, orderLot, lineid, positionID);
      if (llidata != null)
      {
        StandardInventoryAdjustServer invtAdjServ =
              Factory.create(StandardInventoryAdjustServer.class);
        invtAdjServ.adjustLoadLineItemIfNeeded(" ", loadID, item, lot, order,
                      orderLot, lineid, positionID, 0.0, 0, reasonCode);

        // if order is not blank then adjust order line record
        if ((order != null) && (order.length() > 0))
        {
          double allocatedQuantity = llidata.getAllocatedQuantity();

          StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
          orderServer.backoffOrderLineQtyAfterPick(order, loadID, item, orderLot, lineid, allocatedQuantity);
        }
      }
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to delete load line item.
   *
   *  @param loadID Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param order Order number.
   *  @param orderLot Ordered lot number.
   *  @exception DBException
   */
  public void shipLoadLineItem( String loadID, String item, String lot,
      String order, String orderLot, String lineid, String positionID,
      String reasonCode)       throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      LoadLineItemData llidata = mpLLI.getLoadLineItemData(item, lot,
          loadID, order, orderLot, lineid, positionID);
      mpLLI.deleteLoadLineItem(item, lot,
                      loadID, order, orderLot, lineid, positionID);

      tnData.clear();
      tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
      tnData.setTranType(DBConstants.ITEM_SHIP);
      tnData.setLoadID(loadID);
      tnData.setToLoadID(loadID);
      tnData.setLineID(lineid);
      tnData.setItem(item);
      tnData.setLot(lot);
      tnData.setOrderID(order);
      double shipqty = llidata.getCurrentQuantity();
      tnData.setAdjustedQuantity(shipqty);
      tnData.setCurrentQuantity(shipqty);

      logTransaction(tnData);

      logOperation("Ship Inventory Adjust: item '" + item +
          "' lot '" + lot +
          "' load '" + loadID +
          "' quantity shipped= " + shipqty);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  * Method to get item detail info. tailored to a store screen that will share
  * display of Item Details and Expected lines on the same screen.
  *
  * @param isLoadID the item detail load.
  * @return List&lt;Map&gt; of Item Detail data.
  */
  public List<Map> getStoreScreenDataList(String isLoadID)
  {
    List<Map> vpRtnList;

    try
    {
      vpRtnList = mpLLI.getStoreScreenDataList(isLoadID);
    }
    catch(DBException ex)
    {
      vpRtnList = new ArrayList<Map>();
    }

    return(vpRtnList);
  }

  /**
   *  Method to get load line items for specified load (sorted by load).
   *
   *  @param srch Load ID to search for.
   *  @return List of <code>LoadLineItemData</code> objects.
   *  @exception DBException
   */
  public List<Map> getLoadLineItemDataListByLoadID(String srch) throws DBException
  {
    return mpLLI.getLoadLineItemDataListByLoadID(srch);
  }

  /**
   *  Method to get load line items for specified item (sorted by item, lot).
   *
   *  @param item Item ID to search for.
   *  @return List of <code>LoadLineItemData</code> objects.
   *  @exception DBException
   */
  public List<Map> getLoadLineItemDataListByItemLot(String item) throws DBException
  {
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);

    vpLLIData.setKey(LoadLineItemData.ITEM_NAME, item);
    vpLLIData.addOrderByColumn(LoadLineItemData.ITEM_NAME);
    vpLLIData.addOrderByColumn(LoadLineItemData.LOT_NAME);

    return mpLLI.getAllElements(vpLLIData);
  }

  /**
   *  Method to get load line items for specified item, lot (sorted by item, lot).
   *
   *  @param item Item ID to search for.
   *  @param lot Lot number to search for.
   *  @return List of <code>LoadLineItemData</code> objects.
   *  @exception DBException
   */
  public List<Map> getLoadLineItemDataListByItemLot(String item, String lot) throws DBException
  {
    return mpLLI.getLoadLineItemDataListByItemLot(item, lot);
  }

  /**
   *  Method to get load line item totals for a given warehouse, item, and lot.
   *
   *  @param sWarehouse to search by. This may be passed as an empty String.
   *  @param sItem Item ID to search for. This may be passed as an empty String.
   *  @param sLot lot to search for.  This may be passed as an empty string.
   *  @return List of <code>LoadLineItemData</code> objects.
   *  @exception DBException for database access errors.
  */
  public List<Map> getLoadLineItemTotals(String sWarehouse, String sItem, String sLot)
         throws DBException
  {
    TableJoin theJoin = Factory.create(TableJoin.class);
    return(theJoin.getLoadLineItemTotals(sWarehouse, sItem, sLot));
  }

  /**
   *  Method to get load line item totals greater than search item
   *  @param sItem item
   *  @return List of <code>LoadLineItemData</code> objects.
   *  @exception DBException for database access errors.
  */
  public List<Map> getItemTotalsGreaterThanItem(String sWarehouse,String sItem)
         throws DBException
  {
    TableJoin theJoin = Factory.create(TableJoin.class);
    return(theJoin.getItemTotalsGreaterThanItem(sWarehouse, sItem));
  }

  /**
   * Gets unique Load Line Item record based on specified key with no record
   * lock.
   *
   * @param ipLLIKey the criteria to find unique Load Line Item record.
   * @return null if no records are found.
   * @throws DBException If a unique record can't be found based on specified
   *           key (but something was still found). Also if DB access errors.
   */
  public LoadLineItemData getLoadLineItem(LoadLineItemData ipLLIKey)
         throws DBException
  {
    return getLoadLineItem(ipLLIKey, false);
  }

 /**
  * Gets unique Load Line Item record based on specified key.
  * @param ipLLIKey the criteria to find unique Load Line Item record.
  * @param izWithLock <code>true</code> indicates read with lock. <b>Note:</b>
  *        <u>This method must be called from within a transaction if this flag
  *        is set to <code>true</code>.</u>
  * @return null if no records are found.
  * @throws DBException If a unique record can't be found based on specified
  *         key (but something was still found).  Also if DB access errors.
  */
  public LoadLineItemData getLoadLineItem(LoadLineItemData ipLLIKey,
                                          boolean izWithLock) throws DBException
  {
    return mpLLI.getElement(ipLLIKey, (izWithLock) ? DBConstants.WRITELOCK
                                                   : DBConstants.NOWRITELOCK);
  }

  /**
   *  Method to get a load line item data object.
   *
   *  @param loadid Load ID to check.
   *  @param item Item number.
   *  @param lot Lot number.
   *  @param line Line number.
   *  @param order Order number.
   *  @param orderLot Ordered lot number.
   *  @return LoadLineItemData object containing Item Detail info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public LoadLineItemData getLoadLineItem(String loadID, String item,
      String lot, String lineID, String orderID, String orderLot,
      String positionID) throws DBException
  {
    return mpLLI.getLoadLineItemData(item, lot, loadID, orderID, orderLot,
        lineID, positionID);
  }

  /**
   * Method to see if the specified item and lot exist on a load.
   *
   * @param item Item number.
   * @param lot Lot number.
   * @param loadid Load ID.
   * @param position ID
   * @return boolean of <code>true</code> if it exists.
   * @exception DBException
   */
  public boolean exists(String item, String lot, String loadid, String position)
      throws DBException
  {
    return mpLLI.exists(item, lot, loadid, position);
  }

  /**
   * Method checks for the existence of a particular lot in the system.
   *
   * @param lot <code>String</code> containing lot to check for.
   * @return <code>boolean</code> indicating if lot is found.
   */
  @UnusedMethod
  public boolean lotExists(String lot)
  {
    boolean rtnval;
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);

    llidataSearch.setKey(LoadLineItemData.LOT_NAME, lot);
    try
    {
      rtnval = mpLLI.getCount(llidataSearch) > 0;
    }
    catch(DBException e)
    {
      rtnval = false;
    }

    return(rtnval);
  }

  /**
   *  Method to update a load line item without transaction.
   *
   *  @param id Filled in load line item data object.
   *  @exception DBException no information available
   */
  public void updateID(LoadLineItemData id) throws DBException
  {
    mpLLI.updateLoadLineItemInfo(id);
  }

  /**
   *  Method to update a load line item.
   *
   *  @param lli Filled in load line item data object.
   *  @exception DBException
   */
  @UnusedMethod
  public void updateLoadLineItemInfo(LoadLineItemData lli) throws DBException
  {
    updateLoadLineItemInfo(lli, ReasonCode.getDefaultInvAdjustReasonCode(),
        false, false);
  }

  /**
   *  Method to update a load line item.
   *
   *  @param ipLLI Filled in load line item data object.
   *  @exception DBException
   */
  public void updateLoadLineItemInfo(LoadLineItemData ipLLI, String isReasonCode,
                                     boolean sendIS, boolean sendIA)
         throws DBException
  {
    LoadLineItemData vpOldLLI = null;
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      vpOldLLI = mpLLI.getLoadLineItemData(ipLLI.getItem(), ipLLI.getLot(),
                                         ipLLI.getLoadID(), ipLLI.getOrderID(),
                                         ipLLI.getOrderLot(), ipLLI.getLineID(),
                                         ipLLI.getPositionID());
      if (vpOldLLI != null)
      {
        if (vpOldLLI.getCurrentQuantity() != ipLLI.getCurrentQuantity())
        {
          // quantity has changed lets adjust it through the standard server to
          // take care of adding transaction history anfd host events as needed.
          // if quantity has decreased it may need to back off moves for orders
          StandardInventoryAdjustServer invtAdjServ =
                            Factory.create(StandardInventoryAdjustServer.class);
          invtAdjServ.adjustLoadLineItemIfNeeded(" ", vpOldLLI.getLoadID(),
                                                 vpOldLLI.getItem(),
                                                 vpOldLLI.getLot(),
                                                 vpOldLLI.getOrderID(),
                                                 vpOldLLI.getOrderLot(),
                                                 vpOldLLI.getLineID(),
                                                 ipLLI.getPositionID(),
                                                 ipLLI.getCurrentQuantity(), 0,
                                                 isReasonCode, sendIA, true);
        }
                                       // Send Inventory Status message to host
                                       // if we need to.
        if (sendIS)
        {
          if (mzHasHostSystem)
          {
            initializeHostServer();
            mpHostServ.sendInventoryStatus(ipLLI);
          }
        }

        if (ipLLI.getHoldType() != vpOldLLI.getHoldType())
        {
          tnData.clear();
          tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
          tnData.setTranType(DBConstants.MODIFY_ITEM);
          tnData.setLoadID(vpOldLLI.getLoadID());
          if (!vpOldLLI.getLoadID().equals(ipLLI.getLoadID()))
            tnData.setToLoadID(ipLLI.getLoadID());
          tnData.setOrderID(ipLLI.getOrderID());
          tnData.setItem(ipLLI.getItem());
          tnData.setLot(ipLLI.getLot());
          tnData.setCurrentQuantity(ipLLI.getCurrentQuantity());
          tnData.setAgingDate(ipLLI.getAgingDate());
          tnData.setReasonCode(ipLLI.getHoldReason());
          tnData.setHoldType(ipLLI.getHoldType());
          tnData.setExpirationDate(ipLLI.getExpirationDate());
          tnData.setOrderLot(ipLLI.getOrderLot());
          tnData.setLineID(ipLLI.getLineID());
          logTransaction(tnData);
        }
      }
      this.updateID(ipLLI);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  * Method to update the item hold flag for all items in the rack.  If an Item is
  * out on the floor, intransit, or allocated it will not be updated.
  * @param isItem the item to update.
  * @param isLot the lot to update.  If this lot is passed in as an empty string,
  *        all matching item details with this item will be updated.
  * @param isHoldReason the Hold Reason to update.
  * @param inHoldValue the hold value.
  * @throws DBException when there is a database access or modify error.
  */
  public void setItemHoldValue(String isItem, String isLot, String isHoldReason,
                               int inHoldValue) throws DBException
  {
    boolean vzLotProvided = (isLot.trim().length() != 0);

    List<Map> vpIDList = null;
    TableJoin vpTabJoin = Factory.create(TableJoin.class);

    if (isHoldReason == null || isHoldReason.trim().length() == 0)
    {                                  // Holds release case.
      if (vzLotProvided)
        vpIDList = getLoadLineItemDataListByItemLot(isItem, isLot);
      else
        vpIDList = getLoadLineItemDataListByItemLot(isItem);
    }
    else
    {
      if (vzLotProvided)
        vpIDList = vpTabJoin.getHoldableLoadLineItems(isItem, isLot);
      else
        vpIDList = vpTabJoin.getHoldableLoadLineItems(isItem, null);
    }

    if (vpIDList.isEmpty())
    {
      logDebug("Warning! Load Line Item not found for item: " + isItem +
               ", Lot: " + isLot + " when trying to set Item details on hold.");
      return;
    }

    LoadLineItemData vpIDData = Factory.create(LoadLineItemData.class);

    for (Map vpIDMap : vpIDList)
    {
      String vsLoadID = DBHelper.getStringField(vpIDMap,
                                                LoadLineItemData.LOADID_NAME);
      String vsLineID = DBHelper.getStringField(vpIDMap,
                                                LoadLineItemData.LINEID_NAME);
      String vsPosition = DBHelper.getStringField(vpIDMap,
                                              LoadLineItemData.POSITIONID_NAME);

      vpIDData.setKey(LoadLineItemData.LOADID_NAME, vsLoadID);
      vpIDData.setKey(LoadLineItemData.ITEM_NAME, isItem);
                                       // Don't use lot unless it's provided.
      if (vzLotProvided)
      {
        vpIDData.setKey(LoadLineItemData.LOT_NAME, isLot);
      }
      else
      {
        String vsLot = DBHelper.getStringField(vpIDMap,
                                               LoadLineItemData.LOT_NAME);
        vpIDData.setKey(LoadLineItemData.LOT_NAME, vsLot);
      }

      vpIDData.setKey(LoadLineItemData.POSITIONID_NAME, vsPosition);
      vpIDData.setKey(LoadLineItemData.LINEID_NAME, vsLineID);
      TransactionToken vpTok = null;
      try
      {
        vpTok = startTransaction();
        LoadLineItemData lockedData = mpLLI.getElement(vpIDData, DBConstants.WRITELOCK);
        vpIDData.setHoldType(inHoldValue);
        vpIDData.setHoldReason(isHoldReason);
        mpLLI.modifyElement(vpIDData);
                                     // Send the host a status message.
        lockedData.setHoldType(inHoldValue);
        lockedData.setHoldReason(isHoldReason);
        if (mzHasHostSystem)
        {
          initializeHostServer();
          mpHostServ.sendInventoryStatus(lockedData);
        }
        commitTransaction(vpTok);
      }
      catch(DBException exc)
      {
        logException("Error updating item detail.", exc);
      }
      finally
      {
        endTransaction(vpTok);
        vpIDData.clear();
      }
    }
  }

  /**
   *  Method to get load line items using KeyObject array.
   *
   *  @param ipKey Item ID to search for.
   *  @return List of <code>LoadLineItemData</code> objects.
   *  @exception DBException
   */
  public List<Map> getLoadLineItemDataList(LoadLineItemData ipKey)
      throws DBException
  {
    return mpLLI.getLoadLineItemDataList(ipKey);
  }

  /**
   * Convenience method.
   *
   * @param sLoadID no information available
   * @return no information available
   */
  public int getLoadLineCount(String sLoadID)
  {
    return(getLoadLineCount(sLoadID, "", "", "", ""));
  }

  /**
   * Convenience method.
   *
   * @param sLoadID no information available
   * @param sItem no information available
   * @return no information available
   */
  @UnusedMethod
  public int getLoadLineCount(String sLoadID, String sItem)
  {
    return(getLoadLineCount(sLoadID, sItem, "", "", ""));
  }

 /**
  *  Method to Count Load Line Items.
  *
  *  @param sLoadID <code>String</code> containing Order load ID. to search
  *         by. "" if it should be excluded in the search.
  *  @param sItem <code>String</code> containing Load Item to search by.
  *         "" if it should be excluded in the search.
  *  @param sLot <code>String</code> containing lot to search by.
  *         "" if it should be excluded in the search.
  *  @param sOrderID <code>String</code> containing Order identifier to search by.
  *         "" if it should be excluded in the search.
  *  @return no information available
  */
  public int getLoadLineCount(String sLoadID, String sItem, String sLot,
                              String sOrderID, String isLineID)
  {
    int idCount = -1;
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);

    if (sLoadID.trim().length() != 0)
    {
      llidataSearch.setKey(LoadLineItemData.LOADID_NAME, sLoadID);
    }

    if (sItem.trim().length() != 0)
    {
      llidataSearch.setKey(LoadLineItemData.ITEM_NAME, sItem);
    }

    if (sLot.trim().length() != 0)
    {
      llidataSearch.setKey(LoadLineItemData.LOT_NAME, sLot);
    }

    if (sOrderID.trim().length() != 0)
    {
      llidataSearch.setKey(LoadLineItemData.ORDERID_NAME, sOrderID);
    }

    if (isLineID.trim().length() != 0)
    {
      llidataSearch.setKey(LoadLineItemData.LINEID_NAME, isLineID);
    }

    try
    {
      idCount = mpLLI.getCount(llidataSearch);
    }
    catch(DBException e)
    {
      logException(e, "Counting Load Line Items.");
    }

    return(idCount);
  }

  /**
   *  Method checks if there is any inventory that is truly available for
   *  allocation.
   *
   *  @param sItem <code>String</code> containing item to check against.
   *  @param requestedOrderQty <code>double</code> containing order quantity
   *         that is being checked for "fillability".
   *
   *  @return <code>true</code> if there is allocatable inventory,
   *          <code>false</code> otherwise.
   */
  @UnusedMethod
  public boolean isInventoryAvailable(String sItem, double requestedOrderQty)
  {
    double availQuantity = 0, olFillRequiredQuantity = 0;

    OrderLine orderLine = Factory.create(OrderLine.class);

    try
    {
      availQuantity = mpLLI.getTotalAvailableQuantity(sItem, false);
                                       // Get the total amount we will need to
                                       // fill all orders containing this item.
      olFillRequiredQuantity = orderLine.getTotalLineFillQuantity(sItem);
    }
    catch(DBException e)
    {
      logException(e, "StandardInventoryServer-->isInventoryAvailable(item = " +
                   sItem + ").");
    }

    return((availQuantity - (olFillRequiredQuantity + requestedOrderQty)) >= 0);
  }

  /**
   * Method to delete an item master without transaction.
   *
   * @param siItem Item number to be deleted.
   * @exception DBException
   */
  private void deleteIM(String siItem) throws DBException
  {
    mpIM.deleteItemMaster(siItem);
    logOperation(LogConsts.OPR_DSVR, "Deleted item master " + siItem);
                                       // Log Item Add history
    tnData.clear();
    tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
    tnData.setTranType(DBConstants.DELETE_ITEM_MASTER);
    tnData.setItem(siItem);
    logTransaction(tnData);
  }

  /**
   * Method to delete an item master.
   *
   * @param siItem Item number to be deleted.
   * @exception DBException
   */
  public void deleteItemMaster(String siItem) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      deleteIM(siItem);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to delete an item master if the Item is at Zero Qty.
   *
   *  @param siItem Item number to be deleted if Zero.
   *  @exception DBException
   */
  @UnusedMethod
  public void deleteItemMasterIfZeroQty(String siItem) throws DBException
  {
    TransactionToken tt = null;
    if (mpLLI.getTotalQuantity(siItem, "") <= 0.0)
    {                                  // There is no inventory left!
      try
      {
        tt = startTransaction();
        logDebug("Deleting item master: " + siItem + " due to Zero Qty ");
        deleteIM(siItem);
        commitTransaction(tt);
      }
      finally
      {
        endTransaction(tt);
      }
    }
  }

 /**
  * Method to get random Item name using a prefix.
  *
  * @param isItemPrefix the item prefix.
  * @return the item name. Empty string if no Item name could be generated within
  *         MAX_RANDOM_TRIES.
  * @throws DBRuntimeException if there is a databse access error.
  * @see #MAX_RANDOM_TRIES MAX_RANDOM_TRIES
  */
  @UnusedMethod // Only called from another unused method
  public String getRandomItemName(String isItemPrefix) throws DBRuntimeException
  {
    String vsItem = "";
    Random vpRand = new Random();

    stopLabel:
    for(int vnIdx = 0; vnIdx < MAX_RANDOM_TRIES; vnIdx++)
    {
      if (isItemPrefix.trim().length() == 0)
        vsItem = Integer.toString(vpRand.nextInt(Integer.MAX_VALUE));
      else
        vsItem = isItemPrefix + Integer.toString(vpRand.nextInt(Integer.MAX_VALUE));

      try
      {
        if (!itemMasterExists(vsItem)) break stopLabel;
      }
      catch(DBException exc)
      {
        throw new DBRuntimeException("Database Access error!", exc);
      }
    }

    return(vsItem);
  }

 /**
  * Method to generate a random Item master record.
  * @param isItemPrefix prefix string for the item name.
  * @throws DBRuntimeException if there is a problem adding the item master after
  *         {@link #MAX_RANDOM_TRIES MAX_RANDOM_TRIES} tries.
  */
  @UnusedMethod
  public void createRandomItemMaster(String isItemPrefix) throws DBRuntimeException
  {
    String vsItem = getRandomItemName(isItemPrefix);
    if (!vsItem.isEmpty())
    {
      addItemMasterFromString(vsItem, "Emulation Item " + vsItem, false);
    }
    else
    {
      throw new DBRuntimeException("Failed to generate random Item!");
    }
  }

  /**
   *  Method to add an item master without transaction.
   *
   *  @param imd Filled in item master data object.
   *  @exception DBException
   */
  private void addIM(ItemMasterData imd) throws DBException
  {
    try
    {
      mpIM.addItemMaster(imd);
                                         // Log Item Add history
      tnData.clear();
      tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
      tnData.setTranType(DBConstants.ADD_ITEM_MASTER);
      tnData.setItem(imd.getItem());
      logTransaction(tnData);

      logOperation("Added item master " + imd.getItem());
    }
    catch(DBException exc)
    {
      if (exc.isDuplicate())
        throw new DBException("Attempt to add duplicate Item " + imd.getItem() + "!", true);
      else
        throw exc;
    }
  }

  /**
   *  Method to add a New Item master by String
   *
   * @param ItemID to add
   * @param itemDescription
   * @param izDeleteAtZero
   * @return 0 if the item was successfully added. -1 if there was some type of
   *           error.
   */
  public int addItemMasterFromString(String newItemID, String itemDescription,
                                     boolean izDeleteAtZero)
  {
    int vnRtn = -1;

    if(newItemID != null && newItemID.trim().length() > 0)
    {
      TransactionToken vpTok = null;
      try
      {
        vpTok = startTransaction();

        if (!mpIM.exists(newItemID))
        {
          ItemMasterData vpIMData = Factory.create(ItemMasterData.class);
          vpIMData.setItem(newItemID);
          if (itemDescription.trim().length() > 0)
            vpIMData.setDescription(itemDescription);
          else
            vpIMData.setDescription("Item: " + newItemID);

          vpIMData.setDeleteAtZeroQuantity(izDeleteAtZero ? DBConstants.YES : DBConstants.NO);
          mpIM.addItemMaster(vpIMData);
          vnRtn = 0;
        }
        else
        {
          logDebug("Attemped to add Item: " + newItemID + " that Already Exists");
        }
        commitTransaction(vpTok);
      }
      catch(DBException e)
      {
        logException(e, "Exception adding Item " + newItemID + " Description: " +
                    itemDescription + "\"  - StandardInventoryServer.addItemFromString");
      }
      finally
      {
        endTransaction(vpTok);
      }
    }
    else
    {
      logError("Attemped to add a Blank ItemID - Description: " + itemDescription );
    }

    return vnRtn;
  }

  /**
   *  Method to add an item master.
   *
   *  @param imd Filled in item master data object.
   *  @exception DBException
   */
  public void addItemMaster(ItemMasterData imd) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      this.addIM(imd);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to get a list of matching item names.
   *
   *  @param srchItem Item number.
   *  @return List of item names.
   *  @exception DBException
   */
  public List<String> getItemMasterNameList(String srchItem) throws DBException
  {
    return mpIM.getItemMasterNameList(srchItem);
  }

  /**
   *  Method to get a list of all item names.
   *
   *  @return List of item names.
   *  @exception DBException
   */
  public List<String> getItemMasterNameList() throws DBException
  {
    return(this.getItemMasterNameList (""));
  }

  /**
   *  Method to get an item's description.
   *
   *  @param itemName Item number.
   *  @return String containing the description of the item.
   *  @exception DBException
   */
  public String getItemMasterDescription(String itemName) throws DBException
  {
    return mpIM.getItemMasterDescription (itemName);
  }

  /**
   *  Method to get an item master data for specified item.
   *
   *  @param itemName Item number.
   *  @return ItemMasterData object containing Item Master info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public ItemMasterData getItemMasterData(String itemName) throws DBException
  {
    return mpIM.getItemMasterData(itemName);
  }

  /**
   *  Method to get an item number for specified lot.
   *
   *  NOTE: This assumes that there is a one to one relationship between
   *  the item and lot (such as at Merit).
   *
   *  @param lot Lot of the item to get.
   *  @return String item name matching the lot.
   *  @exception DBException
   */
  @UnusedMethod
  public ItemMasterData getItemMasterFromLot(String lot) throws DBException
  {
    String sItem = null;

    // Look for an item detail with the specified lot.
    LoadLineItemData llidata = Factory.create(LoadLineItemData.class);
    llidata.setKey(LoadLineItemData.LOT_NAME, lot);
    List<Map> list = mpLLI.getAllElements(llidata);
    if(list != null && list.size() > 0)
    {
      llidata.dataToSKDCData(list.get(0));
      sItem = llidata.getItem();
    }

    // If no item detail, look for an order line.
    if(sItem == null)
    {
      OrderLine ol = Factory.create(OrderLine.class);
      OrderLineData oldata = Factory.create(OrderLineData.class);
      oldata.setKey(OrderLineData.ORDERLOT_NAME, lot);
      list = ol.getAllElements(oldata);
      if(list != null && list.size() > 0)
      {
        oldata.dataToSKDCData(list.get(0));
        sItem = oldata.getItem();
      }
    }

    // If still no item, check the PO lines
    if(sItem == null)
    {
      PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
      PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
      poldata.setKey(PurchaseOrderLineData.LOT_NAME, lot);
      list = pol.getAllElements(poldata);
      if(list != null && list.size() > 0)
      {
        poldata.dataToSKDCData(list.get(0));
        sItem = poldata.getItem();
      }
    }

    // If we still don't have an item, return null
    if(sItem == null)
    {
      return null;
    }

    // Get the item master
    return getItemMasterData(sItem);
  }

  /**
   * Method to validate item dimensions.
   *
   * @param item Item to validate.
   * @return true of false.
   */
  @UnusedMethod
  public boolean hasValidDimensions(String item) throws DBException
  {
    ItemMasterData imdata = getItemMasterData(item);

    // Check that the dimensions are greater than 0
    /*
     * Oh, so we're just going to return false if imdata is null? So, even
     * though we haven't actually *seen* the item master data, we're just going
     * to say, "No sir, the dimensions for this item aren't valid," and never
     * admit to the caller we just don't know? Who wrote this code, my ex-wife?
     *
     * Yes, if imdata is null, then there is no item master.  Since the
     * dimensions are in the item master, if there is no item master for this
     * item, then there are no valid dimensions for this item.
     */
    if (imdata != null &&
        imdata.getItemHeight() > 0.0 &&
        imdata.getItemLength() > 0.0 &&
        imdata.getItemWidth()  > 0.0)
    {
      return true;
    }

    return false;
  }

  /**
   *  Method to get list of recommended zones.
   *
   *  @param srch Zone.
   *  @return List of strings containing recommended zones.
   */
  @UnusedMethod
  public List<String> getItemRecZoneList(String srch) throws DBException
  {
    return mpIM.getItemRecZoneList(srch);
  }

  /**
   *  Method to get list of routes.
   *
   *  @param srch Route.
   *  @return List of strings containing routes.
   */
  public List<String> getItemRouteIDList(String srch) throws DBException
  {
    return mpIM.getItemRouteIDList(srch);
  }

  /**
   * Method to get the items pieces per unit
   * @param itemID
   * @return
   */
  public int getPiecesPerUnit(String itemID) throws DBException
  {
    int rtnval = 0;
    ItemMasterData imData = Factory.create(ItemMasterData.class);
    imData.setKey(ItemMasterData.ITEM_NAME, itemID);

    try
    {
      ItemMasterData imDataNew = mpIM.getElement(imData, DBConstants.NOWRITELOCK);
      if (imDataNew != null)
      {
        rtnval = imDataNew.getPiecesPerUnit();
        imDataNew = null;
      }
    }
    catch(DBException e)
    {
      logException(e, "Getting Pieces-Per-Unit");
      throw new DBException(e);
    }

    return(rtnval);
  }

   /**
   *  Method to see if an item master exists.
   *
   *  @param itemName Item number.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean itemMasterExists(String itemName) throws DBException
  {
    return mpIM.exists(itemName);
  }

 /**
  * Method checks if item is expiration date controlled.
  * @param sItem the item being checked.
  * @return <code>true</code> if the item is expiration date required.
  */
  @UnusedMethod
  public boolean isExpirationControlled(String sItem) throws DBRuntimeException
  {
    boolean rtn = false;
    try
    {
      rtn = mpIM.isExpirationRequired(sItem);
    }
    catch (DBException e)
    {
      logError("Error Reading expiration requirement flag from " +
               "item master..." + e.getMessage());
      throw new DBRuntimeException("Error Reading expiration requirement flag from " +
                                    "item master...", e);
    }

    return(rtn);
  }

  /**
   *  Method to update an item master.
   *
   *  @param imd Filled in item master data object.
   *  @exception DBException
   */
  public void updateItemInfo(ItemMasterData imd) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpIM.updateItemInfo(imd);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to get a count of matching items.
   *
   *  @param srch Item number.
   *  @return int count of matching items.
   *  @exception DBException
   */
  @UnusedMethod
  public int getItemMasterCountByName(String srch) throws DBException
  {
    return mpIM.getItemMasterCountByName(srch);
  }

 /**
  * Deallocates Inventory by load, item, lot, qty
  *
  * @param isLoadID - Load containing allocated quantity.
  * @param isItem   - the allocated item
  * @param isLot    - the allocated lot.
  * @param isLineID - this load's line id.
  * @param isPositionID Position of Item Detail in load.
  * @param idDeallocAmt the amount to deallocate.
  * @throws DBException if the item detail that is supposed to be updated can't
  *         be found, or DB access error.
  */
  public void deallocateInventory(String isLoadID, String isItem, String isLot,
                                  String isLineID, String isPositionID,
                                  double idDeallocAmt) throws DBException
  {
    TransactionToken vpTok = null;
    LoadLineItemData iddata = Factory.create(LoadLineItemData.class);
    iddata.setKey(LoadLineItemData.LOADID_NAME, isLoadID);
    iddata.setKey(LoadLineItemData.ITEM_NAME, isItem);
    iddata.setKey(LoadLineItemData.LOT_NAME, isLot);
    iddata.setKey(LoadLineItemData.LINEID_NAME, isLineID);
    iddata.setKey(LoadLineItemData.POSITIONID_NAME, isPositionID);

    vpTok = startTransaction();
    try
    {
      LoadLineItemData vpAllocItemDet = mpLLI.getElement(iddata,
                                                         DBConstants.WRITELOCK);
      if (vpAllocItemDet == null)
      {
          // Try to find it without the load line id
        iddata.clear();
        iddata.setKey(LoadLineItemData.LOADID_NAME, isLoadID);
        iddata.setKey(LoadLineItemData.ITEM_NAME, isItem);
        iddata.setKey(LoadLineItemData.LOT_NAME, isLot);
        iddata.setKey(LoadLineItemData.POSITIONID_NAME, isPositionID);
        vpAllocItemDet = mpLLI.getElement(iddata, DBConstants.WRITELOCK);
        if (vpAllocItemDet == null)
        {
          String tmpstring = "Item detail record not found for load: " +  isLoadID +
                             "\n isItem: " + isItem + " Lot : " + isLot +
                             "\n::::: InventoryServer-->deallocateInventory() :::::";
          logError(tmpstring);
          throw new DBException(tmpstring);
        }
      }

      double idAllocQty = vpAllocItemDet.getAllocatedQuantity();
      if (idAllocQty >= idDeallocAmt)
      {
        idAllocQty -= idDeallocAmt;
      }
      else
      {                            // Quantities don't match! Just make it 0.
        idAllocQty = 0;
      }

      iddata.setAllocatedQuantity(idAllocQty);
      mpLLI.modifyElement(iddata);
      commitTransaction(vpTok);
    }
    catch(DBException dbe)
    {
      logError( "Error Deallocating inventory Inventory Server-->deallocateInventory()");
      logException(dbe, "In Inventory Server-->deallocateInventory()");
      throw dbe;
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Method to delete a ContainerType.
   *
   *  @param containerType ContainerType type to be deleted.
   *  @exception DBException
   */
  public void deleteContainer(String containerType) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      if (mpLoad.doesContainerTypeExist(containerType))
      {
        logError("Cannot delete ContainerType Type: " + containerType + ", details exist - deleteContainer()" );
        DBHelper.dbThrow("Cannot delete ContainerType Type: " + containerType + ", loads exist" );
      }
      tt = startTransaction();
      Factory.create(ContainerType.class).deleteContainer(containerType);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to add a ContainerType.
   *
   *  @param ContainerTypeData no information available
   *  @exception DBException
   */
  public void addContainer(ContainerTypeData ContainerTypeData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(ContainerType.class).addElement(ContainerTypeData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to get Container information.
   *
   *  @param containerType ContainerType to look for.
   *  @return ContainerTypeData object containing ContainerType info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public ContainerTypeData getContainer(String containerType) throws DBException
  {
    return Factory.create(ContainerType.class).getContainerData(containerType);
  }

  /**
   * Method to get list of matching ContainerType types.
   *
   * @param srch ContainerType to match.
   * @return List of strings containing ContainerType types.
   * @throws DBException
   */
  public List<String> getContainerTypeList() throws DBException
  {
    return Factory.create(ContainerType.class).getContainerTypeList();
  }

  /**
   *  Method to get list of all ContainerType types.
   *
   *  @return List of Empty <code>ContainerTypeData</code> objects.
   *  @exception DBException
   */
  public List<Map> getContainerDataList() throws DBException
  {
    return Factory.create(ContainerType.class).getContainerDataList();
  }

  /**
   *  Method to see if the ContainerType type exists.
   *
   *  @param srch ContainerType type.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean containerExists(String srch) throws DBException
  {
    return Factory.create(ContainerType.class).doesContainerExist(srch);
  }

  /**
   *  Method to update a ContainerType.
   *
   *  @param ct Filled in ContainerType data object.
   *  @throws DBException for DB access errors.
   */
  public void updateContainer(ContainerTypeData ct) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(ContainerType.class).updateContainer(ct);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to delete a load. Deletes a load and its load line items as well
   *  as any child loads and load line items.
   *
   *  @param loadID Load ID to be deleted.
   *  @param reasonCode reason for deleting load line item.
   *  @throws DBException for DB access errors.
   */
  public void deleteLoad(String loadID, String reasonCode) throws DBException
  {
    deleteLoadItems(loadID, reasonCode, true);
  }

  /**
   * Method to delete all load line items and child loads on a load, but not the
   * load itself.
   *
   *  @param isLoad the load for which item details are being deleted.
   *  @param isReason the reason code for which the deletion is taking place.
   *  @throws DBException for DB access errors.
   */
  public void deleteAllItemsOnLoad(String isLoad, String isReason)
         throws DBException
  {
    deleteLoadItems(isLoad, isReason, false);
  }

 /**
  *  Method to delete a load that's shipping. The reason for this method is that we don't
  *  want to delete the manifest records here. They are needed to close orders.
  *  The ship controller will handle that.
  *
  *  @param loadID Load ID to be deleted.
  *  @throws DBException
  */
  public void deleteShippingLoad(String loadID, String reasonCode)
         throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      // delete all child loads
      List<Map> childLoads = mpLoad.getChildLoads(loadID);
      for(Iterator<Map> it = childLoads.iterator(); it.hasNext();)
      {
        deleteShippingLoad(DBHelper.getStringField(it.next(), LoadData.LOADID_NAME), reasonCode);
      }

      // delete all of the moves
      StandardMoveServer moveServ = Factory.create(StandardMoveServer.class);
      List<Map> moveList = moveServ.getMoveDataList(loadID);
      for(int i=0; i<moveList.size(); i++)
      {
        MoveData mvdata = Factory.create(MoveData.class);
        mvdata.dataToSKDCData(moveList.get(i));
        mvdata.setKey(MoveData.MOVEID_NAME, mvdata.getMoveID());
        moveServ.deleteMove(mvdata);
      }

      /*
       * Do project-specific clean-up
       */
      doProjectSpecificCleanUpForLoadShip(loadID);

      // delete all load line items
      List<Map> loadLineItems = mpLLI.getLoadLineItemDataListByLoadID(loadID);
      for(Iterator<Map> it = loadLineItems.iterator(); it.hasNext();)
      {
        Map tMap = it.next();
        shipLoadLineItem(
            DBHelper.getStringField(tMap, LoadLineItemData.LOADID_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.ITEM_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.LOT_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.ORDERID_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.ORDERLOT_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.LINEID_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.POSITIONID_NAME),
            reasonCode);
      }

      // delete container equal to the load ID

      ContainerType vpContainerType = Factory.create(ContainerType.class);
      if(vpContainerType.doesContainerExist(loadID))
      {
        vpContainerType.deleteContainer(loadID);
      }

      // Check for an order that is left over after deleting each line and it's moves but not
      // the order..if this is a load order, we need to delete it.

      StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);

      List<Map> olList = orderServer.getOrderLineData("", loadID);
      for(Iterator<Map> it = olList.iterator(); it.hasNext();)
      {
        Map tMap = it.next();
        String orderID = DBHelper.getStringField(tMap, "sOrderID");
        if(orderServer.getOrderTypeValue(orderID) == DBConstants.FULLLOADOUT)
        {
          // For a load order, we have to delete the order because the load that we ordered
          // is now deleted and load orders only have one load on them..if this changes,
          // we will have to change this.
         orderServer.deleteOrder(orderID);
        }
      }

      // Read load and save data before deleting.
      LoadData ldx = getInventoryLoad(loadID, DBConstants.NOWRITELOCK);
      if (ldx != null)
      {
        mpLoad.deleteLoad(loadID);

        // location empty flags may need adjusting
        setLocationEmptyStatus(ldx.getWarehouse(), ldx.getAddress(), ldx.getShelfPosition());
        setLocationEmptyStatus(ldx.getNextWarehouse(), ldx.getNextAddress(), ldx.getNextShelfPosition());
        setLocationEmptyStatus(ldx.getFinalWarehouse(), ldx.getFinalAddress(), LoadData.DEFAULT_POSITION_VALUE);

        tnData.clear();
        tnData.setTranCategory(DBConstants.LOAD_TRAN);
        tnData.setTranType(DBConstants.DELETE_LOAD);
        tnData.setLoadID(loadID);
        tnData.setToLocation(ldx.getNextWarehouse(), ldx.getNextAddress());
        tnData.setLocation(ldx.getWarehouse(), ldx.getAddress());
      }
      else
      {
        tnData.clear();
        tnData.setTranCategory(DBConstants.LOAD_TRAN);
        tnData.setTranType(DBConstants.DELETE_LOAD);
        tnData.setLoadID(loadID);
        tnData.setActionDescription("Load record does not exist");
      }
      logOperation(LogConsts.OPR_DSVR, "LoadId \"" + loadID + "\" - Deleted");
      logTransaction(tnData);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  *  Method to correctly set a storage location's empty status.
  *  Status is determined as follows:  Reserved - No load present,
  *  but load enroute.  Occupied - Load is in location.  Unoccupied - No load
  *  present or enroute.
  *
  *  @param warehouse Warehouse location is in.
  *  @param address Address of the location.
  *  @throws DBException for DB access errors.
  */
  public void setLocationEmptyStatus(String warehouse, String address,
      String isShelfPosition) throws DBException
  {
    StandardLocationServer mpLocServer = Factory.create(StandardLocationServer.class);
    int vnEmptyFlag = DBConstants.OCCUPIED;
    
    LocationData lcdataSearch = Factory.create(LocationData.class);
    LoadData lddataSearch = Factory.create(LoadData.class);
    Location location = Factory.create(Location.class);
    // validate the locations passed in, be sure they are real
    if ((warehouse.trim().length() == 0) ||
        (address.trim().length() == 0))
    {
      return;
    }

    // only deal with locations that are regular locations
    lcdataSearch.setKey(LocationData.WAREHOUSE_NAME, warehouse);
    lcdataSearch.setKey(LocationData.ADDRESS_NAME, address);
    lcdataSearch.setInKey(LocationData.LOCATIONTYPE_NAME, KeyObject.AND,
        DBConstants.LCASRS, DBConstants.LCCONSOLIDATION,
        DBConstants.LCCONVSTORAGE, DBConstants.LCRECEIVING,
        DBConstants.LCSHIPPING, DBConstants.LCSTAGING, DBConstants.LCDEDICATED);
    LocationData lcdata = location.getElement(lcdataSearch, DBConstants.WRITELOCK);
    if (lcdata != null)
    {
      // see if loads exist in the location
      lddataSearch.setKey(LoadData.WAREHOUSE_NAME, warehouse);
      lddataSearch.setKey(LoadData.ADDRESS_NAME, address);
      lddataSearch.setKey(LoadData.SHELFPOSITION_NAME, isShelfPosition);
      boolean loadsExist = mpLoad.exists(lddataSearch);

      // check if any loads enroute to this location
      // need to check both final and next destinations
      lddataSearch.clear();
      lddataSearch.setKey(LoadData.FINALWAREHOUSE_NAME, warehouse);
      lddataSearch.setKey(LoadData.FINALADDRESS_NAME, address);
      boolean loadsEnroute = mpLoad.exists(lddataSearch);

      if (!loadsEnroute)
      {
        lddataSearch.clear();
        lddataSearch.setKey(LoadData.NEXTWAREHOUSE_NAME, warehouse);
        lddataSearch.setKey(LoadData.NEXTADDRESS_NAME, address);
        lddataSearch.setKey(LoadData.NEXTSHELFPOSITION_NAME, isShelfPosition);
        loadsEnroute = mpLoad.exists(lddataSearch);
      }

      lcdata.setKey(LocationData.WAREHOUSE_NAME,warehouse);
      lcdata.setKey(LocationData.ADDRESS_NAME,address);
      if (loadsExist)
      {
        vnEmptyFlag = DBConstants.OCCUPIED;
      }
      else if (loadsEnroute)
      {
        vnEmptyFlag = DBConstants.LCRESERVED;
      }
      else
      {
        vnEmptyFlag = DBConstants.UNOCCUPIED;
      }

      mpLocServer.setLocationEmptyFlag(warehouse, address, isShelfPosition, vnEmptyFlag);
    }
  }

  /**
   *  Method to delete a load with checking. This verifies that there are no
   *  moves for the load, and verifies that there are no allocations for
   *  the load.
   *
   *  @param loadID Load ID to be deleted.
   *  @throws DBException for DB access errors.
   */
  public void deleteLoadWithChecking(String loadID) throws DBException
  {
    // need to validate that there are no moves
    // then need to check LoadLineItems to see if there are any allocated qty
    // also need to check any child loads for allocated items
    MoveData mvdataSearch = Factory.create(MoveData.class);
    mvdataSearch.setKey(MoveData.PARENTLOAD_NAME, loadID);
    if (Factory.create(Move.class).getCount(mvdataSearch) > 0)
    {
      DBHelper.dbThrow("Load has moves for it");
    }
    if (isLoadAllocated(loadID))
    {
      DBHelper.dbThrow("Load has allocated items");
    }

    deleteLoad(loadID, "");
  }

  /**
   *  Method to check if load has any allocations. A load is considered to be
   *  allocated if the allocated quantity is greater than zero for any load
   *  line item on this load or any of its child loads.
   *
   *  @param loadID Load ID to be checked.
   *  @return boolean of <code>true</code> if there are any allocated
   *          quantities on this load.
   *  @throws DBException for DB access errors.
   */
  public boolean isLoadAllocated(String loadID) throws DBException
  {
    boolean hasAllocations = false;
    LoadLineItemData llidataSearch = Factory.create(LoadLineItemData.class);
    llidataSearch.setKey(LoadLineItemData.LOADID_NAME, loadID);
    llidataSearch.setKey(LoadLineItemData.ALLOCATEDQUANTITY_NAME, Double.valueOf(0.0),
                        KeyObject.GREATER_THAN);
    if (mpLLI.getCount(llidataSearch) > 0)
    {
      return true;
    }

    List<Map> childLoads = mpLoad.getChildLoads(loadID);
    for(Iterator<Map> it = childLoads.iterator(); it.hasNext();)
    {
      hasAllocations = isLoadAllocated(
          DBHelper.getStringField(it.next(), LoadData.LOADID_NAME));
    }
    return (hasAllocations);
  }

 /**
  *  Try to change the item name. Changes name in item masters, load line items,
  *  order lines, moves, cycle counts, stations, dedicated locations,
  *  purchase order lines, and any where else the item name appears in the data base.
  *  @param oldItemName Old Item name being changed.
  *  @param newItemName New Item name.
  *  @throws DBException for DB access errors.
  */
  public void changeItemMasterName(String oldItemName, String newItemName, boolean izLogTransaction)
         throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(TableJoin.class).changeItemMasterName(oldItemName, newItemName);
      commitTransaction(tt);
      if (izLogTransaction)
      {
        tnData.clear();
        tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
        tnData.setTranType(DBConstants.MODIFY_ITEM_MASTER);
        tnData.setItem(oldItemName);
        tnData.setActionDescription("Item Master Name Change Old '" +
        oldItemName+ "' New Item Master Name '"+ newItemName +"'");
        logTransaction(tnData);
      }
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  *  Method retrieves a load record load id as key.
  *
  *  @param loadID <code>String</code> containing load to search for.
  *  @param withLock <code>int</code> flag indicating if record should be locked.
  *
  *  @return <code>LoadData</code> object. <code>null</code> if no record found.
  *  @throws DBException for DB access errors.
  */
  public LoadData getInventoryLoad(String loadID, int withLock) throws DBException
  {
    return mpLoad.getLoadData(loadID, withLock);
  }

  /**
   *  Method to get load line items for specified order (sorted by load).
   *
   *  @param srch Order ID to search for.
   *  @return List of <code>LoadLineItemData</code> objects.
   *  @exception DBException
   */
  @UnusedMethod
  public List<Map> getLoadLineItemDataListByOrderID(String srch) throws DBException
  {
    return mpLLI.getLoadLineItemDataListByOrderID(srch);
  }

  /**
   *  Method to transfer a load line item.  Calling routine is responsible
   *  to create a TO load if one does not exist and delete empty FROM loads.
   *  The quantity in the load line item data record reflects the quantity to be transfered.
   *  Be sure the allocated quantity is correct as well.
   *
   *  @param iLLI Filled in load line item data object.
   *  @param iToLoad Load to be transfered into.
   *  @exception DBException
   */
  public void transferLoadLineItem(LoadLineItemData iLLI, String iToLoad,
                                   String isReasonCode) throws DBException
  {
    String vsItem     = iLLI.getItem();
    String vsLot      = iLLI.getLot();
    String vsOrder    = iLLI.getOrderID();
    String vsOrdLot   = iLLI.getOrderLot();
    String vsLineID   = iLLI.getLineID();
    String vsFromLoad = iLLI.getLoadID();
    String vsPositionID = iLLI.getPositionID();

    if (vsFromLoad.equals(iToLoad))
    {
      return;
    }

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();

      //  Lock the from load line item record
      //  If the item/lot/etc exists in the to-load,
      //     modify the old to-load line item record
      //     If the quantity has not changed, delete the old from-load record
      //  If the item/lot/etc does not exist in the to-load, and the quantity has not changed,
      //     modify the load line item record to reflect the new load.
      //  If the item/lot/etc does not exist in the to-load, and the quantity has changed,
      //     add a new load line item record for the to-load and
      //     modify the quantity on the from-load line item record
      //  Make entry in the transaction history

      /*
       *  Read FROM load line item.
       *  Delete it if we're moving everything.
       *  Update it if we're only moving part.
       */
      LoadLineItemData pLLIFromData = mpLLI.getLoadLineItemData(vsItem, vsLot,
          vsFromLoad, vsOrder, vsOrdLot, vsLineID, vsPositionID,
          DBConstants.WRITELOCK);
      if (pLLIFromData == null)
      {
        throw new DBException("Item detail not found:" + vsItem + ";" + vsLot
            + ";" + vsFromLoad + ";" + vsOrder + ";" + vsOrdLot + ";" + vsLineID);
      }

      /*
       * Don't allow the transfer of more than exist
       */
      if (pLLIFromData.getCurrentQuantity() < iLLI.getCurrentQuantity())
      {
        throw new DBException("Unable to transfer " + iLLI.getCurrentQuantity() +
            " of " + vsItem + "/" + vsLot + ": there are only " + pLLIFromData.getCurrentQuantity());
      }
      if (iLLI.getCurrentQuantity() <= 0)
      {
        throw new DBException("Unable to transfer " + iLLI.getCurrentQuantity()
            + " of " + vsItem + "/" + vsLot
            + ": transfer quantity must be greater than zero.");
      }

      double vdAvailQty = pLLIFromData.getCurrentQuantity() - pLLIFromData.getAllocatedQuantity();
      double vdToAllocQty = iLLI.getCurrentQuantity() - vdAvailQty;
      if (vdToAllocQty < 0)
      {
        vdToAllocQty = 0;
      }

      /*
       *  Read TO load line item
       *  Add to it if it exists; add a new one otherwise.
       */

      LoadLineItemData pLLIToData = mpLLI.getLoadLineItemData(vsItem, vsLot,
          iToLoad, vsOrder, vsOrdLot, vsLineID, vsPositionID,
          DBConstants.WRITELOCK);
      if (pLLIToData != null)
      {
        pLLIToData.setCurrentQuantity(iLLI.getCurrentQuantity()+pLLIToData.getCurrentQuantity());
        pLLIToData.setAllocatedQuantity(vdToAllocQty + pLLIToData.getAllocatedQuantity());
        updateID(pLLIToData);
      }
      else
      {
        iLLI.setLoadID(iToLoad);
        iLLI.setAllocatedQuantity(vdToAllocQty);
        addLoadLI(iLLI);
      }

      if (vdAvailQty < iLLI.getCurrentQuantity())
      {
        double vdMoveQty = iLLI.getCurrentQuantity() - vdAvailQty;
        StandardMoveServer vpMoveServer = Factory.create(StandardMoveServer.class);
        MoveData vpMoveDataSearch = Factory.create(MoveData.class);
        vpMoveDataSearch.setKey(MoveData.ITEM_NAME, vsItem);
        vpMoveDataSearch.setKey(MoveData.PICKLOT_NAME, vsLot);
        vpMoveDataSearch.setKey(MoveData.LOADID_NAME, vsFromLoad);
        if (!vsOrder.equals(""))
        {
          vpMoveDataSearch.setKey(MoveData.ORDERID_NAME, vsOrder);
          vpMoveDataSearch.setKey(MoveData.ORDERLOT_NAME, vsOrdLot);
          vpMoveDataSearch.setKey(MoveData.LINEID_NAME, vsLineID);
        }
        List<Map> vpMoveList = vpMoveServer.getMoveDataList(vpMoveDataSearch.getKeyArray());

        MoveData vpMoveData = Factory.create(MoveData.class);

        StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
        StandardLocationServer vpLocationServer = Factory.create(StandardLocationServer.class);
        Move vpMove = Factory.create(Move.class);
        Random vpRand = new Random();
        LoadData vpToLoadData = vpLoadServer.getLoad(iToLoad);
        LocationData vpLocationData = vpLocationServer.getLocationRecord(
            vpToLoadData.getWarehouse(), vpToLoadData.getAddress());
        if (vpLocationData == null)
        {
          throw new DBException("Could not get to-location record");
        }
        double vdAssignedQty = 0.0;
        for (Map vpMap: vpMoveList)
        {
          vpMoveData.dataToSKDCData(vpMap);

          // You can't move assigned picks
          if (vpMoveData.getMoveStatus() == DBConstants.ASSIGNED)
          {
            vdAssignedQty += vpMoveData.getPickQuantity();
            continue;
          }

          if (vpMoveData.getMoveType() == DBConstants.LOADMOVE)
          {
            if (pLLIFromData.getCurrentQuantity() == iLLI.getCurrentQuantity())
            {
              vpMoveServer.deleteMove(vpMoveData);
            }
            continue;
          }

          if (vpMoveData.getPickQuantity() <= vdMoveQty)
          {
            // move entire record
            vpMoveData.setLoadID(iToLoad);
            vpMoveData.setAddress(vpLoadServer.getLoadAddress(iToLoad));
            vpMoveData.setWarehouse(vpToLoadData.getWarehouse());
            vpMoveData.setAisleGroup(vpLocationData.getAisleGroup());
            vpMoveData.setMoveSequence(vpLocationData.getSearchOrder());
            vpMoveData.setKey(MoveData.MOVEID_NAME, vpMoveData.getMoveID());
            vpMove.modifyElement(vpMoveData);
            vdMoveQty = vdMoveQty - vpMoveData.getPickQuantity();
          }
          else
          {
            // split move record
            vpMoveData.setPickQuantity(vpMoveData.getPickQuantity() - vdMoveQty);
            vpMoveData.setKey(MoveData.MOVEID_NAME, vpMoveData.getMoveID());
            vpMove.modifyElement(vpMoveData);
            vpMoveData.setPickQuantity(vdMoveQty);
            vpMoveData.setLoadID(iToLoad);
            vpMoveData.setAddress(vpLoadServer.getLoadAddress(iToLoad));
            vpMoveData.setWarehouse(vpToLoadData.getWarehouse());
            vpMoveData.setAisleGroup(vpLocationData.getAisleGroup());
            vpMoveData.setMoveSequence(vpLocationData.getSearchOrder());
            vpMoveData.setMoveID(Math.abs(vpRand.nextInt()));
            vpMoveServer.addMove(vpMoveData);
          }
        }
        // You can't move assigned picks
        if (vdAssignedQty > 0.0)
        {
          double viMaxQty = pLLIFromData.getCurrentQuantity() - vdAssignedQty;
          throw new DBException("Cannot transfer more than " + viMaxQty
              + ": " + vdAssignedQty + " have already been assigned to pick.");
        }
      }

      if(pLLIFromData.getCurrentQuantity() == iLLI.getCurrentQuantity())
      {
        mpLLI.deleteLoadLineItem(vsItem, vsLot, vsFromLoad,
            vsOrder, vsOrdLot, vsLineID, vsPositionID);
      }
      else
      {
        pLLIFromData.setCurrentQuantity(pLLIFromData.getCurrentQuantity()-iLLI.getCurrentQuantity());
        pLLIFromData.setAllocatedQuantity(pLLIFromData.getAllocatedQuantity()-vdToAllocQty);
        updateID(pLLIFromData);
      }

      /*
       * Get from/to load information for Transaction History
       */
      LoadData vFromLddata = getLoad(vsFromLoad, DBConstants.NOWRITELOCK);
      LoadData vToLddata = getLoad(iToLoad, DBConstants.NOWRITELOCK);

                                   // Log Inventory Transfer History
      tnData.clear();
      tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
      tnData.setTranType(DBConstants.TRANSFER);
      tnData.setLoadID(vsFromLoad);
      tnData.setToLoadID(iToLoad);
      tnData.setLocation(vFromLddata.getWarehouse(), vFromLddata.getAddress());
      tnData.setToLocation(vToLddata.getWarehouse(), vToLddata.getAddress());
      tnData.setOrderID(vsOrder);
      tnData.setItem(vsItem);
      tnData.setLot(vsLot);
      tnData.setPickQuantity(iLLI.getCurrentQuantity());
      tnData.setAgingDate(iLLI.getAgingDate());
      tnData.setReasonCode(isReasonCode);
      tnData.setHoldType(iLLI.getHoldType());
      tnData.setExpirationDate(iLLI.getExpirationDate());
      tnData.setOrderLot(vsOrdLot);
      tnData.setLineID(vsLineID);
      logTransaction(tnData);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to clean up item masters that have no more details, etc.
   */
  public void cleanupAllAutoDeletableItemMasters()
  {
	  if( DBInfo.USING_SQL_SERVER )
	  {
		  cleanupAllAutoDeletableItemMastersSQLServer();
	  }
	  else if( DBInfo.USING_ORACLE_DB )
	  {
		  cleanupAllAutoDeletableItemMastersOracle();
	  }
  }
  
  /**
   *  Method to clean up item masters that have no more details using Oracle DB, etc.
   */
  public void cleanupAllAutoDeletableItemMastersOracle()
  {
	  
	  TransactionToken tt = null;
	  try
	  {
		  tt = startTransaction();
		  new TableJoin().deleteAllAutoDeletableItemMasters();
		  commitTransaction(tt);
	  }
	  catch (DBException e)
	  {
		  logError(e.getMessage());
	  }
	  finally
	  {
		  endTransaction(tt);
	  }
  }

  /**
   *  Method to clean up item masters that have no more details when using SQLServer DB, etc.
   */
  public void cleanupAllAutoDeletableItemMastersSQLServer()
  {
	  Map currRow = null;
	  String  sItem;
	  String  sSynonym;
	  
	  try 
	  {
		  // Get a list of ItemMasters that can be deleted
	   	List<Map> imList = new TableJoin().getAllAutoDeletableItemMasters();
	
	   	for(int ldIdx = 0; ldIdx < imList.size(); ldIdx++)
	   	{
	   		currRow = imList.get(ldIdx);
	   		sItem = DBHelper.getStringField(currRow, ItemMasterData.ITEM_NAME);
	   		deleteItemMaster(sItem);
	   	}
      
	    // Get a list of synonyms that can be deleted
	   	List<Map> synList = new TableJoin().getAllAutoDeletableSynonyms();
	
	   	for(int ldIdx = 0; ldIdx < synList.size(); ldIdx++)
	   	{
	   		currRow = synList.get(ldIdx);
	   		sSynonym = DBHelper.getStringField(currRow, SynonymData.ITEM_NAME);
	   		deleteSynonym(sSynonym);
	   	}
      
	  } 
	  catch (DBException e) 
	  {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
  }
  
  
  
  
  /**
   *  Method to get list of matching item synonyms.
   *
   *  @param ipKey Key info. to look up synonym.
   *  @return List of <code>SynonymData</code> objects.
   *  @throws DBException
   */
  public List<Map> getSynonymDataList(SynonymData ipKey) throws DBException
  {
    return Factory.create(Synonym.class).getAllElements(ipKey);
  }

  /**
   *  Method to add an item synonym.
   *
   *  @param sydata <code>SynonymData</code> object.
   *  @exception DBException
   */
  public void addSynonym(SynonymData sydata) throws DBException
  {
    if(!itemMasterExists(sydata.getItemID()))
    {
      throw new DBException("Item: " + sydata.getItemID() + " does not exist in Item Master");
    }
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Synonym.class).addSynonym(sydata);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Inside addSynonym");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method retrieves a synonym record.
   *
   *  @param isItem <code>String</code> containing item to search for.
   *  @param isSynonym <code>String</code> containing synonym to search for.
   *
   * @throws DBException for DB access errors.
   *  @return <code>SynonymData</code> object. <code>null</code> if no record found.
   */
   public SynonymData getSynonymRecord(String isItem, String isSynonym)
          throws DBException
   {

     SynonymData sydataSearch = Factory.create(SynonymData.class);
     sydataSearch.setKey(SynonymData.ITEM_NAME, isItem);
     sydataSearch.setKey(SynonymData.SYNONYM_NAME, isSynonym);
     SynonymData mySynonymData =
       Factory.create(Synonym.class).getElement(sydataSearch, DBConstants.NOWRITELOCK);

     return(mySynonymData);
   }

  /**
   *  Method modifies a synonym record.
   *
   *  @param isOldSynonym <code>String</code> containing synonym to modify
   *  @param syData data object containing keys and data to modify.
   *  @throws DBException for DB access errors.
   */
  public void modifySynonym( String isOldSynonym, SynonymData syData)
         throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      syData.setKey(SynonymData.ITEM_NAME, syData.getItemID());
      syData.setKey(SynonymData.SYNONYM_NAME, isOldSynonym);

      Factory.create(Synonym.class).modifyElement(syData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Inside modifySynonym");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  *  Method to see if the specified synonym exists.
  *
  *  @param isSynonym synonym name.
  *  @return boolean of <code>true</code> if it exists.
  *  @exception DBException
  */
  public boolean existSynonym(String isSynonym) throws DBException
  {
    return Factory.create(Synonym.class).existSynonym(isSynonym);
  }

 /**
  *  Method to delete a synonym.
  *
  *  @param isSynonym synonym.
  *  @throws DBException
  */
  public void deleteSynonym(String isSynonym) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();

      Factory.create(Synonym.class).deleteSynonym(isSynonym);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Inside deleteSynonym");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  *  Method to replace a synonym with item.
  *
  *  @param isItem Item or maybe a synonym.
  * @return Item synonym if it exists.
  */
  public String swapItemSynonymIfEntered(String isItem)
  {
    try
    {
     SynonymData mySynonymData = Factory.create(Synonym.class).getSynonymData(isItem);
      if (mySynonymData != null)
      {
        return mySynonymData.getItemID();
      }
    }
    catch (DBException e)
    {
      logException(e, "Inside swapItemSynonymIfEntered");
    }

    return isItem;
  }

  /**
   *  Method to get an item's master hold type.
   *
   *  @param itemName Item number.
   *  @return integer containing the hold type of the item.
   *  @exception DBException
   */
  public int getItemMasterHold(String itemName) throws DBException
  {
    return mpIM.getItemMasterHold(itemName);
  }

  /**
   * Get an Order ID that is associated with the contents of a load.
   *
   * @param isLoadID
   * @return String containing an Order ID that is associated with the load
   */
  public String getLoadOrder(String isLoadID)
  {
    String vsOrderID = "";
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    String vsMultiOrder = "Multiple Orders";

    /*
     * Look for Item Details
     */
    try
    {
      List<Map> vpLLIList = getLoadLineItemDataListByLoadID(isLoadID);
      for (Map m : vpLLIList)
      {
        vpLLIData.dataToSKDCData(m);
        if ((vsOrderID.trim().length() > 0) &&
            (!vsOrderID.equals(vpLLIData.getOrderID())))
        {
          vsOrderID = vsMultiOrder;
        }
        else
        {
          vsOrderID = vpLLIData.getOrderID();
        }
      }
    }
    catch (DBException dbe)
    {
      logException(dbe, "Error looking for load contents (details)");
    }

    /*
     * Look for subloads
     */
    try
    {
      StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
      LoadData vpLoadData = Factory.create(LoadData.class);
      List<Map> vpSLList = vpLoadServer.getChildrenList(isLoadID);

      for (Map m : vpSLList)
      {
        vpLoadData.dataToSKDCData(m);
        String vsSubOrder = getLoadOrder(vpLoadData.getLoadID());

        if ((vsOrderID.trim().length() > 0) &&
            (!vsOrderID.equals(vsSubOrder)))
        {
          vsOrderID = vsMultiOrder;
        }
        else
        {
          vsOrderID = vsSubOrder;
        }
      }
    }
    catch (DBException dbe)
    {
      logException(dbe, "Error looking for load contents (subloads)");
    }

    return vsOrderID;
  }

  /**
   * Return the last store Location associated with an Item and Lot.
   *
   * @param isItem item that needs a location
   * @param isLot item lot that needs a location
   * @return the last store Location associated with the item and lot.
   * @throws DBException for DB access errors.
   */
  @UnusedMethod
  public String getLastLocation(String isItem, String isLot) throws DBException
  {
    return mpLLI.getLastLocation(isItem, isLot);
  }

  /**
   * Method modifies a Reason Code Data record.
   *
   * @param iiOldReasonCategory <code>Integer</code> containing Category to
   *            modify
   * @param isOldReasonCode <code>String</code> containing Reason Code to
   *            modify
   * @param rcData <code>ReasonCodeData</code> object with changes.
   * @throws DBException for DB access errors.
   */
  @UnusedMethod
  public void modifyReasonCode(int iiOldReasonCategory, String isOldReasonCode,
                               ReasonCodeData rcData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      rcData.setKey(ReasonCodeData.REASONCATEGORY_NAME, rcData.getReasonCategory());
      rcData.setKey(ReasonCodeData.REASONCODE_NAME, isOldReasonCode);

      Factory.create(ReasonCode.class).modifyElement(rcData);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Inside modifyReasonCode");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to add Reason Code
   *
   * @param iiReasonCategory the category of the reason code.
   * @param isReasonCode the reason code being added.
   * @param isDescription
   * @throws DBException for DB access errors.
   */
  public void addReasonCode(int iiReasonCategory, String isReasonCode,
                            String isDescription) throws DBException
  {
    TransactionToken tt = null;

    try
    {
      tt = startTransaction();
      ReasonCode reasonCode = Factory.create(ReasonCode.class);
      if( reasonCode.existReasonCode(iiReasonCategory, isReasonCode) == false)
      {
        reasonCode.addReasonCode(iiReasonCategory, isReasonCode, isDescription);
      }
      commitTransaction(tt);
    }

    catch (DBException e)
    {
        logException(e, "Exception adding Reason Category/Reason Code  \""
          + iiReasonCategory + "  " + isReasonCode
          + "\"  - StandardInventoryServer.ReasonCode");
        throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to delete a Reason Code
   *
   * @param iiReasonCategory the category of the reason code being deleted.
   * @param isReasonCode the reason code being deleted.
   * @throws DBException for DB access errors.
   */
  public void deleteReasonCode(int iiReasonCategory, String isReasonCode)
         throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      ReasonCode reasonCode = Factory.create(ReasonCode.class);
      if (reasonCode.existReasonCode(iiReasonCategory, isReasonCode) == true)
      {
        reasonCode.deleteReasonCode(iiReasonCategory, isReasonCode);
      }
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to get a Reason Code data list
   *
   * @param iapSearchKey KeyObject array specifying key data.
   * @return
   * @throws DBException for DB access errors.
   */
  public List<Map> getReasonCodeDataList(KeyObject[] iapSearchKey)
      throws DBException
  {
    return Factory.create(ReasonCode.class).getReasonCodeDataList(iapSearchKey);
  }

  /**
   * Method to update Reason Code data
   *
   * @param rcdata data class containing fields to update and keys for finding
   *        the record to update.
   * @throws DBException for DB access errors.
   */
  public void updateReasonCodeInfo(ReasonCodeData rcdata) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      ReasonCode rs = Factory.create(ReasonCode.class);
      rs.updateReasonCodeInfo(rcdata);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Gets Item masters belonging to a particular recommended warehouse.
   * @param isWarehouse
   * @return List of item master records.
   * @throws DBException for DB access errors.
   */
  public List<ItemMasterData> getItemMasterByRecWarehouse(String isWarehouse)
         throws DBException
  {
    List<Map> vpMapList;
    List<ItemMasterData> vpDataList = new ArrayList<ItemMasterData>();
    ItemMasterData vpIMD = Factory.create(ItemMasterData.class);
    vpIMD.setKey(ItemMasterData.RECOMMENDEDWAREHOUSE_NAME, isWarehouse);
    vpMapList = mpIM.getAllElements(vpIMD);
    for(Map vpMap : vpMapList)
    {
      vpIMD = Factory.create(ItemMasterData.class);
      vpIMD.dataToSKDCData(vpMap);
      vpDataList.add(vpIMD);
    }
    return vpDataList;
  }

  /**
   * Set the hold status of all the inventory on a given load.
   * @param isLoadID The load whose inventory will be modified.
   * @param inStatus The new hold status for the inventory.
   * @param isReason The hold reason code.
   * @throws DBException
   *
   * This method was added for the Cox pick screen, but it seemed like a useful
   * baseline method as well.  If you disagree, feel free to remove it.
   */
  public void setLoadInventoryHoldStatus(String isLoadID, int inStatus, String isReason)
          throws DBException
  {
    TransactionToken vpTT = null;
    try
    {
      vpTT = startTransaction();
      List<Map> vpInvList = getLoadLineItemDataListByLoadID(isLoadID);
      List<LoadLineItemData> vpList = DBHelper.convertData(vpInvList,
                                                        LoadLineItemData.class);
      for (LoadLineItemData vpLLD : vpList)
      {
        vpLLD.setHoldType(inStatus);
        vpLLD.setHoldReason(isReason);
        updateID(vpLLD);
        if (mzHasHostSystem)
        {
          initializeHostServer();
          mpHostServ.sendInventoryStatus(vpLLD);
        }
      }
      commitTransaction(vpTT);
    }
    catch(DBException ex)
    {
      throw ex;
    }
    finally
    {
      endTransaction(vpTT);
    }
  }

  /**
   * Get item quantities
   * @param isItemID
   * @return double[] { TotalQuantity, AllocatedQuantity, ExpectedQuantity }
   * @throws DBException
   */
  public double[] getItemQuantities(String isItemID) throws DBException
  {
    return mpIM.getItemQuantities(isItemID);
  }

  /**
   * Get a list of Lot/Total/Allocated/Expected
   * @param isItemID
   * @return List<Map>
   * @throws DBException
   */
  public List<Map> getItemQuantitiesByLot(String isItemID) throws DBException
  {
    return mpIM.getItemQuantitiesByLot(isItemID);
  }

/*===========================================================================
                   PROTECTED METHODS GO IN THIS SECTION
  ===========================================================================*/
  /**
   * Delete all items and child loads on a load and possibly the load itself
   * @param isLoadID
   * @param isReasonCode
   * @param izDeleteLoad True if the load itself should also be deleted.
   */
  protected void deleteLoadItems(String isLoadID, String isReasonCode,
      boolean izDeleteLoad) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      // delete all child loads
      List<Map> vapChildLoads = mpLoad.getChildLoads(isLoadID);
      for(Iterator<Map> it = vapChildLoads.iterator(); it.hasNext();)
      {
        deleteLoad(DBHelper.getStringField(it.next(), LoadData.LOADID_NAME), isReasonCode);
      }

      // delete all load line items
      List<Map> loadLineItems = mpLLI.getLoadLineItemDataListByLoadID(isLoadID);
      StandardPoReceivingServer poServer = Factory.create(StandardPoReceivingServer.class);
      for (Iterator<Map> it = loadLineItems.iterator(); it.hasNext();)
      {
        Map tMap = it.next();
        deleteLoadLineItem(
            DBHelper.getStringField(tMap, LoadLineItemData.LOADID_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.ITEM_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.LOT_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.ORDERID_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.ORDERLOT_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.LINEID_NAME),
            DBHelper.getStringField(tMap, LoadLineItemData.POSITIONID_NAME),
            isReasonCode);
        
        poServer.deletePO(DBHelper.getStringField(tMap, LoadLineItemData.ORDERID_NAME));
        
      }

      // delete container equal to the load ID
      ContainerType vpContainerType = Factory.create(ContainerType.class);
      if (vpContainerType.doesContainerExist(isLoadID))
      {
        vpContainerType.deleteContainer(isLoadID);
      }

      // check if there are any moves left, maybe on load orders for loads
      // without items
      StandardDeallocationServer dealServer = Factory.create(StandardDeallocationServer.class);
      List<Map> moveList = Factory.create(Move.class).getMovesByLoadID(isLoadID);
      for (Iterator<Map> it = moveList.iterator(); it.hasNext();)
      {
        Map tMap = it.next();
        dealServer.deallocateOneMove(DBHelper.getIntegerField(tMap,
            MoveData.MOVEID_NAME));
      }

      // Check for an order that is left over after deleting each line and its
      // moves but not the order..if this is a load order, we need to delete it.
      StandardOrderServer vpOrderServer = Factory.create(StandardOrderServer.class);
      List<Map> vapOLList = vpOrderServer.getOrderLineData("", isLoadID);
      for (Map m : vapOLList)
      {
        String vsOrderID = DBHelper.getStringField(m, OrderLineData.ORDERID_NAME);
        if (vpOrderServer.getOrderTypeValue(vsOrderID) == DBConstants.FULLLOADOUT)
        {
          // For a load order, we have to delete the order because the load that
          // we ordered is now deleted and load orders only have one load on
          // them.  If this changes, we will have to change this.
          vpOrderServer.deleteOrder(vsOrderID);
        }
      }

      // Delete the load if requested
      if (izDeleteLoad)
      {
        // Read load and save data before deleting.
        LoadData vpLoadData = getInventoryLoad(isLoadID, DBConstants.NOWRITELOCK);
        if (vpLoadData != null)
        {
          mpLoad.deleteLoad(isLoadID);

          // location empty flags may need adjusting
          setLocationEmptyStatus(vpLoadData.getWarehouse(),
              vpLoadData.getAddress(), vpLoadData.getShelfPosition());
          setLocationEmptyStatus(vpLoadData.getNextWarehouse(),
              vpLoadData.getNextAddress(), vpLoadData.getNextShelfPosition());
          setLocationEmptyStatus(vpLoadData.getFinalWarehouse(),
              vpLoadData.getFinalAddress(), LoadData.DEFAULT_POSITION_VALUE);

          logOperation(LogConsts.OPR_DSVR, "LoadId \"" + isLoadID + "\" - Deleted");
          tnData.clear();
          tnData.setTranCategory(DBConstants.LOAD_TRAN);
          tnData.setTranType(DBConstants.DELETE_LOAD);
          tnData.setLoadID(isLoadID);
          tnData.setToLocation(vpLoadData.getNextWarehouse(), vpLoadData.getNextAddress());
          tnData.setLocation(vpLoadData.getWarehouse(),  vpLoadData.getAddress());
          tnData.setRouteID((vpLoadData.getRouteID()));
          logTransaction(tnData);
        }
        else
        {
          vpLoadData = Factory.create(LoadData.class);
          vpLoadData.setLoadID(isLoadID);
        }

        /*
         * Do project-specific clean-up
         */
        doProjectSpecificCleanUpForLoadDeletion(vpLoadData);
      }

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

 /**
  *  Method retrieves a load record load id as key.
  *
  *  @param loadID <code>String</code> containing load to search for.
  *  @param withLock <code>int</code> flag indicating if record should be locked.
  *
  *  @return <code>LoadData</code> object. <code>null</code> if no record found.
  */
  protected LoadData getLoad(String loadID, int withLock) throws DBException
  {
     LoadData lddataSearch = Factory.create(LoadData.class);
    lddataSearch.setKey(LoadData.LOADID_NAME, loadID);
    LoadData myLoadData = mpLoad.getElement(lddataSearch, withLock);
     return(myLoadData);
  }

 /**
  * A generic function for inserting project-specific code into a load
  * deletion method inside of the transaction. <b>Note:</b><i>The Load may have
  * already been deleted by the time this method is called. This stub exists for
  * clean up of any related tables that are project specific or otherwise.
  *
  * @param ipLoadData preserved load data regardless of whether load still exists.
  */
  protected void doProjectSpecificCleanUpForLoadDeletion(LoadData ipLoadData)
            throws DBException
  {
    /*
     * For baseline, this should be empty.  Projects should override it.
     */
  }

  /**
   * A generic function for inserting project-specific code into a load
   * shipment method inside of the transaction
   *
   * @param loadID
   */
  protected void doProjectSpecificCleanUpForLoadShip(String isLoadID) throws DBException
  {
    /*
     * For baseline, this should be empty.  Projects should override it.
     */
  }

  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/

  protected void initializeHostServer()
  {
    if (mpHostServ == null)
    {
      mpHostServ = Factory.create(StandardHostServer.class);
    }
  }



  /*========================================================================*/
  /*  End helper-server initialization methods                              */
  /*========================================================================*/
}
