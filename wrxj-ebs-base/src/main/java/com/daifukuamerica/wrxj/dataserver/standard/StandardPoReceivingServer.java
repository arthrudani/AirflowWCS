package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.InvalidDataException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;


/**
 * Purchase Order Server
 *
 * @author SBW
 * @version 1.0 <BR>
 *          Created: 23-Jan-03<BR>
 *          Copyright (c) 2003<BR>
 *          Company: Daifuku America Corporation
 */
public class StandardPoReceivingServer extends StandardServer
{
  protected StandardInventoryServer mpInvServer = null;
  protected StandardStationServer   mpStationServ = null;
  protected StandardLoadServer      mpLoadServer = null;
  protected StandardSchedulerServer mpSchedServer = null;
  protected StandardHostServer      mpHostServ;

  protected PurchaseOrderHeader     mpPOH;
  protected PurchaseOrderHeaderData mpPOHData;
  protected PurchaseOrderLineData   mpPOLData;
  protected PurchaseOrderLine       mpPOL;
  protected LoadData                mpLoadData;

  /**
   * Constructor
   */
  public StandardPoReceivingServer()
  {
    this(null);
  }

  /**
   * Constructor
   *
   * @param isKeyName
   */
  public StandardPoReceivingServer(String isKeyName)
  {
    super(isKeyName);
    mpPOH = Factory.create(PurchaseOrderHeader.class);
    mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    mpPOL = Factory.create(PurchaseOrderLine.class);
    mpPOLData = Factory.create(PurchaseOrderLineData.class);
    mpLoadData = Factory.create(LoadData.class);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardPoReceivingServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  mpPOH = Factory.create(PurchaseOrderHeader.class);
	  mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
	  mpPOL = Factory.create(PurchaseOrderLine.class);
	  mpPOLData = Factory.create(PurchaseOrderLineData.class);
	  mpLoadData = Factory.create(LoadData.class);
  }

  /**
   *  Method to disconnect from the database.
   */
  @Override
  public void cleanUp()
  {
    if (mpHostServ != null)
    {
      mpHostServ.cleanUp();
    }
    if (mpInvServer != null)
    {
      mpInvServer.cleanUp();
    }
    if (mpLoadServer != null)
    {
      mpLoadServer.cleanUp();
    }
    if (mpSchedServer != null)
    {
      mpSchedServer.cleanUp();
    }
    if (mpStationServ != null)
    {
      mpStationServ.cleanUp();
    }

    super.cleanUp();
  }

  /**
   * Receives one PurchaseOrder record based on a Load or PO passed in as a
   * string With that PO(Load) it reads the PO information and builds the
   * inventory on the loadid which matches the PO passed in. (The load should
   * already exist).
   *
   * @param sPONum <code>String</code> object.
   * @return TRUE if Successful or FALSE if not
   */
  public boolean receiveEntirePO(String sPONum)
  {
    return(receiveEntirePO(sPONum, sPONum));
  }

  /**
   * Receives one PurchaseOrder record based on a PO and Load passed in as a
   * strings It reads the PO information and builds the inventory on the LoadID
   * passed in. (The load should already exist).
   *
   * @param sPONum The purchase Order
   * @param sLoadID The Load into which product will be received.
   * @return TRUE if Successful or FALSE if not
   */
  public boolean receiveEntirePO(String sPONum, String sLoadID)
  {
    if (mpPOH.exists(sPONum) == false)
    {
      return(false);
    }
    Load load = Factory.create(Load.class);
    if (load.exists(sLoadID) == false)
    {
      return(false);
    }

    // Begin Transaction
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpPOLData.clear();
      mpPOLData.setOrderID(sPONum);
      mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum);
      List<Map> vpLineList = mpPOL.getAllElements(mpPOLData);

      for(Map vpRecMap : vpLineList) // while(reading polines)
      {
        mpPOLData.clear();
        mpPOLData.dataToSKDCData(vpRecMap);

        boolean vzResult;
        vzResult = receiveALine(sLoadID, mpPOLData.getLot(), mpPOLData.getItem(),
                                "", mpPOLData,
                                mpPOLData.getExpectedQuantity() - mpPOLData.getReceivedQuantity(),
                                mpPOLData.getExpirationDate(),  new Date());
        if (!vzResult) return false;
      }
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Exception adding Item Detail for PO: \"" + sPONum
          + "\" to Load: \"" + sLoadID + "\"  - InventoryServer.addID");
      return false;
    }
    catch (Exception e2)
    {
      //Log the error
      logException("Error receiving PO: " + sPONum  + " to Load: " + sLoadID, e2);
      return false;
    }
    finally
    {
      endTransaction(vpTok);
    }

    try
    {
      mpLoadServer.setLoadAmountFull(sLoadID, mpLoadData.getAmountFullnessTrans());
    }
    catch (DBException e)
    {
      logException(e, "Exception Updating Load Amount Full PO: \"" + sPONum
          + "\" to Load: \"" + sLoadID + "\"  - Standard PO Server.setLoadAmountFull");
    }
    return true;
  }

  /**
   * Receive a PO line
   *
   * @param isLoadID   The Load being received into.
   * @param isLot      The lot being received.
   * @param isItem     The item being received.
   * @param isPosition Position within load for item detail
   * @param ipPOLData  PO Line data object.
   * @param idReceiveQty  The quantity to receive.
   * @param ipExpirationDate  Expiration date of item detail.
   * @param ipAgingDate   Aging date of item detail.
   * @return true if successful, false otherwise.
   * @throws DBException if there is a DB error.
   */
  protected boolean receiveALine(String isLoadID, String isLot, String isItem,
      String isPosition, PurchaseOrderLineData ipPOLData, double idReceiveQty,
      Date ipExpirationDate, Date ipAgingDate) throws DBException
  {
    initializeInventoryServer();
    initializeLoadServer();

    boolean alreadyExists = false;
    String sPONum = "";
    if (ipPOLData != null)
    {
      sPONum = ipPOLData.getOrderID();
    }

    LoadData vpLdData = mpLoadServer.getLoad1(isLoadID);
    LoadLineItemData lli = Factory.create(LoadLineItemData.class);

    alreadyExists = mpInvServer.exists(isItem, isLot, isPosition, isLoadID);

    lli.setItem(isItem);
    if (!alreadyExists)
    {
      if (lli.getItem().length() <= 0)  // required
      {
        throw new DBException("Item name is required");
      }
    }
    if (ipPOLData != null)
    {
      double vdMaximum = ipPOLData.getExpectedQuantity() - ipPOLData.getReceivedQuantity();
      if (vdMaximum < idReceiveQty)
      {
        throw new DBException("Received quantity greater than allowed on PO "+ vdMaximum);
      }
    }

    lli.setLoadID(isLoadID);
    lli.setLot(isLot);
    lli.setPositionID(isPosition);
    lli.setHoldReason("");

    lli.setCurrentQuantity(idReceiveQty);
    lli.setAllocatedQuantity(0.0);

    if (lli.getCurrentQuantity() <= 0) // required
    {
      throw new DBException("Quantity must be greater than zero");
    }

//    if (lli.getHoldType() != DBConstants.ITMAVAIL)
//    {
//      lli.setHoldReason("ITM");
//    }

    if (ipPOLData != null)
    {
      lli.setExpectedReceipt(ipPOLData.getOrderID());
      if (mpInvServer.getItemMasterHold(lli.getItem()) != DBConstants.ITMAVAIL)
      {
        lli.setHoldReason("ITM");
        lli.setHoldType(DBConstants.ITMHOLD);
      }
      else if (ipPOLData.getHoldReason().trim().length() > 0)
      {
        lli.setHoldReason(ipPOLData.getHoldReason());
        lli.setHoldType(DBConstants.ITMHOLD);
      }
    }
    else
    {
      lli.setExpectedReceipt("");
    }
    lli.setPriorityAllocation((DBConstants.NO));
    lli.setAgingDate(ipAgingDate);
    lli.setLastCCIDate(new Date());
    lli.setExpirationDate(ipExpirationDate);

    // add the item detail data
    mpInvServer.addReceivingLoadLI(lli);

    // now check if this satisfied any empty container requests
    StandardPickServer pickServ = Factory.create(StandardPickServer.class);
    StandardMoveServer moveServer = Factory.create(StandardMoveServer.class);
    MoveData moveData = moveServer.getEmptyContainerMove(lli.getLoadID(),
        lli.getItem(), lli.getLot());
    if (moveData != null)
    {
      pickServ.completeEmptyContainerMove(moveData);
    }
                                       // Send the Store Complete message to
                                       // the host.
    if (mzHasHostSystem)
    {
      initializeHostServer();
      mpHostServ.sendStoreComplete(sPONum, vpLdData.getAddress(), lli);
    }

    tnData.clear();
    tnData.setTranCategory(DBConstants.INVENTORY_TRAN);
    tnData.setTranType(DBConstants.ITEM_RECEIPT);
    tnData.setOrderID(sPONum);
    tnData.setLoadID(isLoadID);
    tnData.setToLoadID(isLoadID);
    tnData.setItem(isItem);
    tnData.setLot(isLot);
    tnData.setLocation(vpLdData.getWarehouse(), vpLdData.getAddress());

    if (ipPOLData != null)
    {
      tnData.setExpectedQuantity(ipPOLData.getExpectedQuantity());
    }
    else
    {
      tnData.setExpectedQuantity(0.0);
    }
    tnData.setCurrentQuantity(0.0);
    tnData.setAdjustedQuantity(idReceiveQty);
    tnData.setReceivedQuantity(idReceiveQty);
    logTransaction(tnData);

    // Now update the PO qtys and status.
    if (ipPOLData != null)
    {
      ipPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum);
      ipPOLData.setKey(PurchaseOrderLineData.ITEM_NAME, ipPOLData.getItem());
      ipPOLData.setKey(PurchaseOrderLineData.LOT_NAME, ipPOLData.getLot());
      ipPOLData.setKey(PurchaseOrderLineData.LINEID_NAME, ipPOLData.getLineID());

      ipPOLData.setReceivedQuantity(ipPOLData.getReceivedQuantity() + idReceiveQty);

      if (ipPOLData.getExpectedQuantity() - ipPOLData.getReceivedQuantity() == 0)
      {
        mpPOL.deleteElement(ipPOLData);
        logDeleteExpectedReceiptLine(ipPOLData, vpLdData.getAddress());
      }
      else
      {
        mpPOL.modifyElement(ipPOLData);
      }

      deletePOHeaderIfNecessary(sPONum, null, true);
    }

    return (true);
  }

  /**
   * Receive a PO line
   *
   * @param isPONum
   * @param isRecvLoad  Load being received into.
   * @param isRecvItem the Item being received.
   * @param isRecvLot the Load being received.
   * @param idReceivedQty the quantity being received.
   * @param ipExpDate Expiration date of product.
   * @param ipAgingDate The product aging date.
   * @return
   * @throws DBException
   */
  public boolean receivePOLine(String isPONum, String isRecvLoad, String isRecvItem,
                               String isRecvLot, String isPositionID,
                               double idReceivedQty, Date ipExpDate, Date ipAgingDate)
         throws DBException
  {
    boolean izResults;

    if (!mpPOH.exists(isPONum))
    {
      throw new DBException("PO does not exist");
    }

    Load load = Factory.create(Load.class);
    if (!load.exists(isRecvLoad))
    {
      throw new DBException("To-Load does not exist");
    }

    // Begin Transaction
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpPOLData.clear();
      mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, isPONum);
      mpPOLData.setKey(PurchaseOrderLineData.ITEM_NAME, isRecvItem);
      mpPOLData.setKey(PurchaseOrderLineData.LOT_NAME, isRecvLot);
      mpPOLData = mpPOL.getElement(mpPOLData, DBConstants.WRITELOCK);
      if (mpPOLData == null)
      {
        throw new DBException("PO line does not exist");
      }

      Date vpExpDate = (ipExpDate == null) ? mpPOLData.getExpirationDate() : ipExpDate;

      izResults = receiveALine(isRecvLoad, isRecvLot, isRecvItem, isPositionID, mpPOLData,
                               idReceivedQty, vpExpDate, ipAgingDate);
      if (!izResults)
      {
        return false;
      }
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception adding Item Detail for PO \"" + isPONum
          + "\" to Load: \"" + isRecvLoad + "\"  - InventoryServer.addID");
      throw e;
    }
    catch (Exception e2)
    {
      throw new DBException(e2.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
    return true;
  }

  /**
   * Adds one PurchaseOrder record using unique key (sPONum).
   *
   * @param ipPOHData <code>PurchaseOrder</code> object.
   * @param ipLineList java.util.List of purchase order line objects.
   * @exception DBException
   */
  public <Type extends PurchaseOrderLineData> void buildPO(PurchaseOrderHeaderData ipPOHData,
                                                              List<Type> ipLineList)
         throws DBException
  {
    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      mpPOH.addElement(ipPOHData);
      logAddExpectedReceiptHeader(mpPOHData);

      for(PurchaseOrderLineData vpELData : ipLineList)
      {
        addPOLine(vpELData);
      }
      commitTransaction(vpToken);
/*---------------------------------------------------------------------------
 * Assume that if a station is marked AUTORECEIVE_ER the loads received there
 * have matching ID's with Expected Receipt ID's. Now make the following checks:
 *
 * (1) If this E.R. has a Store Station specified, check if this station is an
 *     auto-store station with AUTORECEIVE_ER load movement. If it is, check for
 *     an ID Pending load there that may match this Expected Receipt ID.
 *
 * (2) If the Store Station is not specified check any input station that is
 *     AUTORECEIVE_ER for a matching ID Pending load.
 *---------------------------------------------------------------------------*/
      initializeStationServer();
      initializeLoadServer();
      String vsEHStoreStation = ipPOHData.getStoreStation().trim();
      if (vsEHStoreStation.isEmpty())
      {
        mpLoadData.clear();
        mpLoadData.setKey(LoadData.LOADMOVESTATUS_NAME, DBConstants.IDPENDING);
        List<Map> vpIDPendingList = mpLoadServer.getLoadDataList(mpLoadData.getKeyArray());
        for(Map vpLoadMap : vpIDPendingList)
        {
          String vsInpStn = DBHelper.getStringField(vpLoadMap,
                                                    LoadData.ADDRESS_NAME);
          if (mpStationServ.isStationAutoStoreWithER(vsInpStn))
          {
            String vsIDPendLoad = DBHelper.getStringField(vpLoadMap,
                                                        LoadData.BCRDATA_NAME);
            String vsInputLoad = ipPOHData.getOrderID().trim();
            if (vsIDPendLoad.trim().equals(vsInputLoad))
            {
              mergePossibleIDPendingLoad(vsInpStn, vsInputLoad);
              break;
            }
          }
        }
      }
      else if (mpStationServ.isStationAutoStoreWithER(vsEHStoreStation))
      {
        LoadData vpLoadData = mpLoadServer.getOldestLoadData(vsEHStoreStation,
                                                         DBConstants.IDPENDING);
        String vsInputLoad = ipPOHData.getOrderID().trim();
        if (vpLoadData != null &&
            vpLoadData.getLoadID().equals(vsInputLoad))
        {
          mergePossibleIDPendingLoad(vsEHStoreStation, vsInputLoad);
        }
      }
    }
    catch(DBException exc)
    {         // Need to catch this so that duplicate data indicator is set and
              // preserved since otherwise the following Exception block will
              // overwrite this flag.
      throw exc;
    }
    catch(Exception exc)
    {
      throw new DBException("Expected Receipt not added! ", exc);
    }
    finally
    {
      endTransaction(vpToken);
    }
  }

 /**
  * Method to build an Expected Load order.
  * @param ipPOH The Purchase Order/
  * @param ipLoadList the list of loads that are to be on this PurchaseOrder;
  *        normally it's one load to one order.
  * @throws DBException if there is a database access or add error.
  */
  public void buildExpectedLoad(PurchaseOrderHeaderData ipPOH,
                                List<PurchaseOrderLineData> ipLoadList)
              throws DBException
  {
    initializeLoadServer();

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      addPoHeader(ipPOH);

      int vnLineID = 1;
      for(PurchaseOrderLineData vpPOLData : ipLoadList)
      {
        if (mpLoadServer.loadExists(vpPOLData.getLoadID()))
        {
          logOperation("WARNING: Expected Receipt " + vpPOLData.getOrderID() +
                       " has load " + vpPOLData.getLoadID() + " even though this " +
                       "load already exists in the system!");
        }
        vpPOLData.setLineID(Integer.toString(vnLineID++));
        mpPOL.addElement(vpPOLData);
        logAddExpectedReceiptLine(vpPOLData);
      }
      mergePossibleIDPendingLoad(ipPOH.getStoreStation(), ipLoadList);
      commitTransaction(vpTok);
    }
    catch(InvalidDataException ive)
    {
      logError("Invalid Data Exception caught! " + ive.getMessage());
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Method to return a list of expected receipt ids. of E.R.s with a given store
  * station.
  * @param isStoreStation the store station
  * @return Array of expected receipt ids.
  * @throws DBException if there is a DB access error.
  */
  public String[] getExpectdIDByStoreStation(String isStoreStation)
         throws DBException
  {
    mpPOHData.clear();
    mpPOHData.setKey(PurchaseOrderHeaderData.STORESTATION_NAME, isStoreStation);
    mpPOHData.setOrderByColumns(PurchaseOrderHeaderData.EXPECTEDDATE_NAME);
    return(mpPOH.getSingleColumnValues(PurchaseOrderHeaderData.ORDERID_NAME,
                                       false, mpPOHData, SKDCConstants.NO_PREPENDER));
  }
  /**
   * Adds one PurchaseOrder Line record using unique key (sPONum).
   *
   * @param addpol <code>PurchaseOrder</code> object.
   * @return TRUE if Successful or FALSE if not
   * @exception DBException
   */
  public boolean addPOLine(PurchaseOrderLineData addpol) throws DBException
  {
    if (!purchaseOrderExists(addpol.getOrderID()))
    {
      throw new DBException("Attempt to add P.O. Line failed!  No Header " +
                            "info. found!  P.O: " + addpol.getOrderID());
    }

    boolean rtn = false;
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      /*
       *  If Item master does not exist, add the default one.
       */
      ItemMaster itemMast = Factory.create(ItemMaster.class);
      if(!itemMast.exists(addpol.getItem()))
      {
        itemMast.addDefaultItemMaster(addpol.getItem());
      }
      else
      {
        /*
         * Make sure expiration date is checked if existing item master says
         * to check it.
         */
        ItemMasterData vpImdata = itemMast.getItemMasterData(addpol.getItem());
        if (vpImdata.getExpirationRequired() == DBConstants.YES &&
            !isValidExpirationDate(addpol.getExpirationDate()))
        {
          throw new DBException("Invalid expiration date provided for P.O. " +
                                "line. Order: " + addpol.getOrderID() +
                                " Item: " + addpol.getItem() +
                                " Lot: " + addpol.getLot() + ". Item " +
                                addpol.getItem() + " does not exist!");
        }
      }

      mpPOL.addElement(addpol);
      logAddExpectedReceiptLine(addpol);

      commitTransaction(tt);
      rtn = true;
    }
    catch(DBException exc)
    {
      throw new DBException("Expected Line not added for " +
                            "PO: " + addpol.getOrderID() +
                            " item: " + addpol.getItem() +
                            " Line ID: " + addpol.getLineID(), exc);
    }
    finally
    {
      endTransaction(tt);
    }

    return rtn;
  }

  /**
   * Modifies one PurchaseOrder record using unique key (sPONum).
   *
   * @param ipModifiedPOData <code>PurchaseOrder</code> object.
   * @return TRUE if Successful or FALSE if not
   * @exception DBException
   */
  public boolean modifyPOHead(PurchaseOrderHeaderData ipModifiedPOData)
      throws DBException
  {
    boolean rtn = false;
    if (ipModifiedPOData.getKeyArray().length == 0)
    {
      ipModifiedPOData.setKey(PurchaseOrderHeaderData.ORDERID_NAME,
          ipModifiedPOData.getOrderID());
    }

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      PurchaseOrderHeaderData vpOldPOHData = getPoHeaderRecord(ipModifiedPOData);
      mpPOH.modifyElement(ipModifiedPOData);
      logModifyExpectedReceiptHeader(vpOldPOHData, ipModifiedPOData);
      commitTransaction(tt);
      rtn = true;
    }
    catch(Exception exc)
    {
      throw new DBException(exc.getMessage() + " -- Expected Receipt not Modified!");
    }
    finally
    {
      endTransaction(tt);
    }

    return rtn;
  }

  /**
   * Modifies one PurchaseOrder Line record using unique key (sPONum, sItem,
   * sLot).
   *
   * @param ipModifiedPOLData <code>PurchaseOrder</code> object.
   * @return TRUE if Successful or FALSE if not
   * @exception DBException
   */
  public boolean modifyPOLine(PurchaseOrderLineData ipModifiedPOLData)
      throws DBException
  {
    boolean rtn = false;

          // Begin Transaction
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      PurchaseOrderLineData vpOldPOLData = getPoLineRecord(ipModifiedPOLData);
      mpPOL.modifyElement(ipModifiedPOLData);
      logModifyExpectedReceiptLine(vpOldPOLData, ipModifiedPOLData);
      commitTransaction(tt);
      rtn = true;
    }
    catch(NoSuchElementException exc)
    {
      throw new DBException(exc);
    }
    finally
    {
      endTransaction(tt);
    }

    return rtn;
  }

  /**
   * Method validates various data integrity constraints before modifying
   * Expected Receipt Line.
   *
   * @param ipModifiedPOLData the data to be submitted for modification.
   * @throws DBException if there are constraint violations or database access
   *             errors.
   */
  public void modifyPOLineWithValidation(PurchaseOrderLineData ipModifiedPOLData)
      throws DBException
  {
    ColumnObject vpColumnObject = ipModifiedPOLData.getColumnObject(
        PurchaseOrderLineData.EXPECTEDQUANTITY_NAME);

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      PurchaseOrderLineData vpCurrentEL = mpPOL.getElement(ipModifiedPOLData, DBConstants.WRITELOCK);
      if (vpCurrentEL == null)         // There is a problem finding current P.O line
      {
        throw new DBException("Expected Line not found for Expected Receipt! " +
                              "Order: " + ipModifiedPOLData.getOrderID()  +
                              ", Item: " + ipModifiedPOLData.getItem()    +
                              ", Lot: " + ipModifiedPOLData.getLot()      +
                              ", Line: " + ipModifiedPOLData.getLineID());
      }

      double vdExpectedQty = (Double)vpColumnObject.getColumnValue();
      if (vdExpectedQty <= 0.0)
      {
        throw new DBException("Given Expected Quantity for modification is not positive!");
      }
      else if (vpCurrentEL.getReceivedQuantity() > vdExpectedQty)
      {
        throw new DBException("Expected quantity received for modification is " +
                              "not allowed to be less than what has already been received! " +
                              "Order: " + ipModifiedPOLData.getOrderID()  +
                              ", Item: " + ipModifiedPOLData.getItem()    +
                              ", Lot: " + ipModifiedPOLData.getLot() +
                              ", Line: " + ipModifiedPOLData.getLineID());
      }
      mpPOL.modifyElement(ipModifiedPOLData);
      logModifyExpectedReceiptLine(vpCurrentEL, ipModifiedPOLData);
      commitTransaction(vpTok);
    }
    catch(NoSuchElementException exc)
    {
      throw new DBException(exc);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   * Deletes a purchase order.
   *
   * @param sPONum <code>String</code> object.
   * @return TRUE if Successful or FALSE if not
   * @exception DBException
   */
  public void deletePO(String sPONum) throws DBException
  {
    // Start a Transaction


      // Now Delete the PO
      PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
      PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);

      // Delete the lines
      poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum);
      List<Map> vapPOLData = pol.getAllElements(poldata);
      for (Map m : vapPOLData)
      {
        TransactionToken tt = null;
        try
        {
           tt = startTransaction();
           poldata.dataToSKDCData(m);
           poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, poldata.getOrderID());
           poldata.setKey(PurchaseOrderLineData.LOADID_NAME, poldata.getLoadID());
           poldata.setKey(PurchaseOrderLineData.LINEID_NAME, poldata.getLineID());
           pol.deleteElement(poldata);
           logDeleteExpectedReceiptLine(poldata, null);
           commitTransaction(tt);

        }
        finally
        {
           endTransaction(tt);
        }
      }
      // Delete the header

      deletePOHeaderIfNecessary(sPONum, null, true);

    return ;
  }

  /**
   * Method to Delete Expected Receipt Line item using po, item, lot
   *
   * @param isPONum the expected receipt id.
   * @param isItem the item
   * @param isLot the lot
   * @param isLineID the line id.
   * @return TRUE if Successful or FALSE if not
   * @throws DBException
   */
  public void deletePOLine(String isPONum, String isItem, String isLot,
      String isLineID) throws DBException
  {
    // Set up the key
    mpPOLData.clear();
    mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, isPONum);
    mpPOLData.setKey(PurchaseOrderLineData.ITEM_NAME,isItem);
    mpPOLData.setKey(PurchaseOrderLineData.LOT_NAME, isLot);
    mpPOLData.setKey(PurchaseOrderLineData.LINEID_NAME, isLineID);

    // Perform the deletion
    deletePOLine((PurchaseOrderLineData)mpPOLData.clone());
  }

  /**
   * Method deletes an Expected Receipt Line that is used for validating input
   * loads (originally created by ExpectedLoad message from the host). If the
   * line to be deleted is the last line of the Expected Receipt then the header
   * is also deleted.
   *
   * @param isLoadID the load id of the line to delete.
   * @return <code>PurchaseOrderLineData</code> if the delete was successful;
   *         <code>null</code> otherwise.
   * @throws DBException if there is a DB access error or transaction error.
   */
  public PurchaseOrderLineData deleteExpectedLoadLine(String isStoreStn,
      String isLoadID) throws DBException
  {
    String vsPONum = "";
    TransactionToken vpTok = null;
    try
    {                                  // Get Store Station for TH purposes.
      vpTok = startTransaction();
/*---------------------------------------------------------------------------
    Look for oldest Expected Load Line in case there are multiple Expected Receipts
    with the same Load (in a normal system there should always be just one). For
    expected loads in the system, there is no such thing as an expiration date.
    So simply use it to determine oldest E.R. (the expiration date is populated
    with the record add date-time unless it is specified).
  ---------------------------------------------------------------------------*/
      mpPOLData.clear();
      mpPOLData.setKey(PurchaseOrderLineData.LOADID_NAME, isLoadID);
      mpPOLData.setOrderByColumns(PurchaseOrderLineData.EXPIRATIONDATE_NAME);
      List<Map> vpPOLChoices = mpPOL.getAllElements(mpPOLData);
      if (vpPOLChoices.isEmpty())
      {
        return null;
      }
      mpPOLData.dataToSKDCData(vpPOLChoices.get(0));
      mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, mpPOLData.getOrderID());
      mpPOLData.setKey(PurchaseOrderLineData.LOADID_NAME, isLoadID);
      PurchaseOrderLineData vpCompletedPOLData = mpPOL.getElement(mpPOLData, DBConstants.WRITELOCK);
      if (vpCompletedPOLData == null)
      {
        // This could happen
        return null;
      }
      String vsStoreStation = (String)mpPOH.getSingleColumnValue(
          vpCompletedPOLData.getOrderID(),
          PurchaseOrderHeaderData.STORESTATION_NAME);

      vsPONum = vpCompletedPOLData.getOrderID();
      mpPOLData.clear();
      mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, vsPONum);
      mpPOLData.setKey(PurchaseOrderLineData.LOADID_NAME, isLoadID);
      mpPOL.deleteElement(mpPOLData);

      logDeleteExpectedReceiptLoadLine(vpCompletedPOLData, vsStoreStation);

      // Delete the header if there are no more lines
      deletePOHeaderIfNecessary(vsPONum, vsStoreStation, false);

      // Send the Store Complete message
      LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
      vpLLIData.setLoadID(isLoadID);
      vpLLIData.setCurrentQuantity(1);
      initializeHostServer();
      mpHostServ.sendStoreComplete(vsPONum, isStoreStn, vpLLIData);

      commitTransaction(vpTok);

      return (PurchaseOrderLineData)vpCompletedPOLData.clone();
    }
    catch (NoSuchElementException nse)
    {
      logOperation("Warning: Deletion of Expected receipt " + vsPONum
          + " failed.  " + nse.getMessage());
    }
    finally
    {
      endTransaction(vpTok);
    }

    return null;
  }

  /**
   * Delete a ER/PO header if there are no more lines.
   *
   * <P><I>NOTE: This method must be called from within a transaction.</I></P>
   *
   * @param isOrderID
   * @param isStation
   * @param izSendReceiptComplete
   * @throws DBException
   * @throws NoSuchElementException
   */
  protected void deletePOHeaderIfNecessary(String isOrderID, String isStation,
      boolean izSendReceiptComplete) throws DBException, NoSuchElementException
  {

    int vnPOLCount = getOrderLineCount(isOrderID, "", "");
    if (vnPOLCount == 0)
    {
      // There are no more lines.  Delete the ER/PO header
      mpPOHData.clear();
      mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, isOrderID);
      PurchaseOrderHeaderData vpPOHData = getPoHeaderRecord(mpPOHData);
      if (vpPOHData != null)
      {
        TransactionToken tt = null;
        tt = startTransaction();
        mpPOH.deleteElement(mpPOHData);
        commitTransaction(tt);
        logDeleteExpectedReceiptHeader(vpPOHData, isStation);

        if (izSendReceiptComplete)
        {
          initializeHostServer();
          mpHostServ.sendExpectedReceiptComplete(vpPOHData);
        }
      }
    }
    else if (vnPOLCount == -1)
    {
      // getOrderLineCount() returns -1 is there is a DBException
      throw new DBException(
          "Error counting expected receipt lines for order \"" + isOrderID
              + "\"");
    }
    // If there is at least one, don't delete the ER/PO header
  }

  /**
   * Method checks expiration date. If the expiration date is less than or equal
   * to current date, the date is considered invalid.
   *
   * @param expireDate the expiration date being checked.
   * @return <code>true</code> if the date is valid, <code>false</code>
   *         otherwise.
   */
  protected boolean isValidExpirationDate(Date expireDate) throws DBException
  {
                                       // If the expiration date is set to the
                                       // current date or less, reject the P.O.
    Calendar calExpireDate = Calendar.getInstance();
    calExpireDate.setTime(expireDate);
    calExpireDate.set(Calendar.SECOND, 0);
    calExpireDate.set(Calendar.MINUTE, 0);
    calExpireDate.set(Calendar.HOUR_OF_DAY, 0);

    Calendar calCurrentDate = Calendar.getInstance();
    calCurrentDate.set(Calendar.SECOND, 0);
    calCurrentDate.set(Calendar.MINUTE, 0);
    calCurrentDate.set(Calendar.HOUR_OF_DAY, 0);

    return(calExpireDate.compareTo(calCurrentDate) > 0);
  }

  /**
   * Method to Get Purchase Order Status
   *
   * @param sPONum
   * @return
   * @throws DBException
   */
  public int getPOStatus(String sPONum)throws DBException
  {
    PurchaseOrderHeader po = Factory.create(PurchaseOrderHeader.class);
    PurchaseOrderHeaderData podata = Factory.create(PurchaseOrderHeaderData.class);
    podata.setOrderID(sPONum);
    try
    {
      return (po.getOrderStatusValue(podata));
    }
    catch(DBException exc)
    {
      exc.printStackTrace(System.out);
      throw new DBException(exc.getMessage()
          + " - Error getting Expected Receipt Status");
    }
  }

  /**
   * Method to Get a Purchase Order Header to update data that was modified.
   * If sPONum, sItem or sLot
   * is null, it will not be used in the search.
   */
  public List<Map> getPurchaseOrderLine(String sPONum, String sItem, String sLot)
  {
        // If at least one parameter is not set we just return a null because
        // we don't want to return the entire list
    if(sPONum == null && sItem == null && sLot == null)
    {
      return null;
    }

    try
    {
      PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
      PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
      if ( sPONum != null)
      {
        poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum);
      }
      if (sItem != null)
      {
        poldata.setKey(PurchaseOrderLineData.ITEM_NAME, sItem);
      }
      if (sLot != null)
      {
        poldata.setKey(PurchaseOrderLineData.LOT_NAME, sLot);
      }

      return (pol.getAllElements(poldata));
    }
    catch(DBException e)
    {
        e.printStackTrace(System.out);
        //System.out.println("Error " + e + " Reading Expected Receipt Line Data");
        logException(e, "Error Reading Data for Expected Receipt Line...");
        return(null);
    }
  }

  /**
   * See if a PO line exists
   *
   * @param sPonum the purchase order id.
   * @param sItem  the item
   * @param sLot  the lot number.
   * @param sLineID  unique line id. for this P.O. line.
   * @return true if line exists, false otherwise.
   */
  public boolean exists(String sPonum, String sItem, String sLot, String sLineID)
  {
    PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
    PurchaseOrderLineData eldata = Factory.create(PurchaseOrderLineData.class);
    eldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPonum);
    eldata.setKey(PurchaseOrderLineData.ITEM_NAME, sItem);
    eldata.setKey(PurchaseOrderLineData.LOT_NAME, sLot);
    eldata.setKey(PurchaseOrderLineData.LINEID_NAME, sLineID);

    return(pol.exists(eldata));
  }

  /**
   * See if a PO exists
   *
   * @param sPonum the purchase order id.
   * @return true if line exists, false otherwise.
   */
  public boolean exists(String sPonum)
  {
    PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
    PurchaseOrderLineData eldata = Factory.create(PurchaseOrderLineData.class);
    eldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPonum);
    return(pol.exists(eldata));
  }

 /**
  * Method checis if a Expected Receipt Line exists with a given Load ID.
  * @param isLoadID the load to check.
  * @return <code>false</code> if Expected Line does not exist with this load.
  */
  public boolean expectedLoadExists(String isLoadID)
  {
    mpPOLData.clear();
    mpPOLData.setKey(PurchaseOrderLineData.LOADID_NAME, isLoadID);

    return(mpPOL.exists(mpPOLData));
  }

  /**
   * Method to Get a list of Purchase Orders if one is passed in, it looks for
   * purchase orders that are like it.
   */
  public String[] getPOStringList(boolean izDisplayAll)
  {
    String[] vasList = new String[0];
    try
    {
      String vsDispAll;
      PurchaseOrderHeader vpPO = Factory.create(PurchaseOrderHeader.class);
      vsDispAll = (izDisplayAll) ? SKDCConstants.ALL_STRING : SKDCConstants.NO_PREPENDER;
      vasList = vpPO.getDistinctColumnValues(PurchaseOrderHeaderData.ORDERID_NAME, vsDispAll);
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      logException(e, "Error Reading Data for Expected Receipt...");
      return null;
    }

    return(vasList);
  }

  /**
   * Method to Get a list of Purchase Orders if one is passed in, it looks for
   * purchase orders that are like it.
   */
  public List<Map> getPurchaseOrderList(String sPONum, int iPOStatus)
      throws DBException
  {
    PurchaseOrderHeader po = Factory.create(PurchaseOrderHeader.class);
    return po.getPOList(sPONum, iPOStatus);
  }

  /**
   * Method to Get a list of Purchase Orders if one is passed in, it looks for
   * purchase orders that are like it.
   */
  public List<Map> getPOSearchList(String sPONum, int iPOStatus, String sItem,
      String sLot)
  {
    try
    {
      PurchaseOrderHeader po = Factory.create(PurchaseOrderHeader.class);
      return po.getPOSearchList(sPONum, iPOStatus, sItem, sLot);
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      logException(e, "Error Reading Data for Expected Receipt...");
      return null;
    }
  }

  /**
   * Method to Get a list of Purchase Orders if one is passed in, it looks
   * for purchase orders that are like it.
   */
  public List<Map> getPOSearchList(PurchaseOrderHeaderData pohdata, PurchaseOrderLineData poldata)
  {
    try
    {
      PurchaseOrderHeader po = Factory.create(PurchaseOrderHeader.class);
      return po.getPOSearchList(pohdata, poldata);
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      logException(e, "Error Reading Data for Expected Receipt...");
      return null;
    }
  }

  /**
   * Get PO lines for a given order number
   * @param sPONum
   * @return empty list if there is an error or no data found.
   */
  public List<Map> getPurchaseOrderLines(String sPONum) throws DBException
  {
    PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
    PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
    poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum);
    return pol.getAllElements(poldata);
  }

  /**
   * Gets a list of Purchase Order Lines for a given Order including pseudo-columns
   * showing amount that can still be received.
   * @param isPONum the order id of the P.O.
   * @return List of rows matching criteria.  Return empty list if no data found.
   */
  public List<Map> getReceivablePurchaseOrderLines(String isPONum)
  {
    PurchaseOrderLine vpPOLine = Factory.create(PurchaseOrderLine.class);
    List<Map> vpRtnList = new ArrayList<Map>();
    try
    {
      vpRtnList = vpPOLine.getReceivablePOLines(isPONum);
    }
    catch(DBException e)
    {
      logError("Error retrieving PO lines.");
    }

    return(vpRtnList);
  }

  /**
   * See if an item exists
   *
   * @param sItem
   * @return
   */
  public boolean itemExists(String sItem)
  {
    ItemMaster itemMast = Factory.create(ItemMaster.class);
    return itemMast.exists(sItem);
  }

  /**
   * See if Purchase Order Exists
   *
   * @param podata
   * @return
   * @throws DBException
   */
  public boolean purchaseOrderExists(PurchaseOrderHeaderData podata) throws DBException
  {
    PurchaseOrderHeader po = Factory.create(PurchaseOrderHeader.class);
    return po.exists(podata);
  }

  /**
   * See if Purchase Order Exists
   *
   * @param sPONum
   * @return
   * @throws DBException
   */
  public boolean purchaseOrderExists(String sPONum) throws DBException
  {
    PurchaseOrderHeader po = Factory.create(PurchaseOrderHeader.class);
    return po.exists(sPONum);
  }

  /**
   * See if Purchase Order - Item combo exists
   *
   * @param poldata
   * @return
   * @throws DBException
   */
  public boolean purchaseOrderItemExists(PurchaseOrderLineData poldata) throws DBException
  {
    PurchaseOrderLine po = Factory.create(PurchaseOrderLine.class);
    return po.exists(poldata);
  }

  /**
   * See if Purchase Order-Item Exists
   *
   * @param sPONum
   * @param sItem
   * @return
   * @throws DBException
   */
  public boolean purchaseOrderExists(String sPONum, String sItem) throws DBException
  {
    PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
    PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
    poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum);
    poldata.setKey(PurchaseOrderLineData.ITEM_NAME, sItem);
    return pol.exists(poldata);
  }

  /**
   * See if Purchase Order - Item - Lot exists
   *
   * @param poldata
   * @return
   * @throws DBException
   */
  public boolean polineExists(PurchaseOrderLineData poldata) throws DBException
  {
    PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
    return pol.exists(poldata);
  }

  /**
   * See if Purchase Order-Item-Lot Exists
   *
   * @param sPONum
   * @param sItem
   * @param sLot
   * @return
   * @throws DBException
   */
  public boolean purchaseOrderExists(String sPONum, String sItem, String sLot) throws DBException
  {
    PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
    PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
    poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum);
    poldata.setKey(PurchaseOrderLineData.ITEM_NAME, sItem);
    poldata.setKey(PurchaseOrderLineData.LOT_NAME, sLot);
    return pol.exists(poldata);
  }


  /**
   * Get a random unique purchase order id
   */
  public String createRandomPurchaseOrderID()
  {
    Random rand = new Random();
    String purchaseOrderID = "";
    boolean duplicateID = true;
    PurchaseOrderHeader po = Factory.create(PurchaseOrderHeader.class);
    do
    {
      purchaseOrderID = "ER" + rand.nextInt(Integer.MAX_VALUE);
      if (!po.exists(purchaseOrderID))
      {
        duplicateID = false;
      }
    } while(duplicateID);

    return(purchaseOrderID);
  }


  /**
   *  Convenience method. Gets a POrder Header with no Lock, and only the
   *  order ID.
   */
  public PurchaseOrderHeaderData getPoHeaderRecord(String isOrderID) throws DBException
  {
    mpPOHData.clear();
    mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, isOrderID);
    return(getPoHeaderRecord(mpPOHData));
  }

  /**
   *  Gets a POrder Header record.
   */
  public PurchaseOrderHeaderData getPoHeaderRecord(PurchaseOrderHeaderData ipPOHData, int inLock)
    throws DBException
  {
    return mpPOH.getElement(ipPOHData, inLock);
  }

  /**
   *  Convenience method. Gets a Order Header with no Lock.
   */
  public PurchaseOrderHeaderData getPoHeaderRecord(PurchaseOrderHeaderData ordData)
    throws DBException
  {
    return(getPoHeaderRecord(ordData, DBConstants.NOWRITELOCK));
  }

  /**
   *  Gets a Order Line record with lock
   */
  public PurchaseOrderLineData getPoLineRecord(PurchaseOrderLineData ipPOLData, int inLock)
    throws DBException
  {
    return mpPOL.getElement(ipPOLData, inLock);
  }

  /**
   *  Convenience method. Gets a Order Line with no Lock.
   */
  public PurchaseOrderLineData getPoLineRecord(PurchaseOrderLineData ipPOLData)
         throws DBException
  {
    return getPoLineRecord(ipPOLData, DBConstants.NOWRITELOCK);
  }

  /**
   *  Method adds POrder Header to the database.
   *
   *  @param ipPOHData object containing data to add.
   *
   *  @throws DBException if there is a DB access or update error.
   */
  public void addPoHeader(PurchaseOrderHeaderData ipPOHData) throws DBException
  {
    String vsStoreStation = ipPOHData.getStoreStation();

    if (mpPOH.exists(ipPOHData.getOrderID()))
    {
      throw new DBException("Duplicate data add error! Order \'" +
                             ipPOHData.getOrderID() + "\' already exists!", true);
    }
    else if (vsStoreStation.trim().length() != 0)
    {
      initializeStationServer();
      if (!mpStationServ.exists(vsStoreStation))
      {
        throw new DBException("Expected Receipt " + ipPOHData.getOrderID() +
                           " specified non-existent station " + vsStoreStation);
      }
    }

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpPOH.addElement(ipPOHData);
      logAddExpectedReceiptHeader(ipPOHData);
      commitTransaction(vpTok);
    }
    catch(DBException e)
    {
      logException(getClass().getSimpleName() + ".addPoHeader", e);
      throw new DBException(getClass().getSimpleName() + ".addPoHeader "
          + getExceptionString(e), e);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   * Delete a purchase order line
   *
   * @param ipPOLKey <code>PurchaseOrderLineData</code> containing key information.
   * @throws DBException
   */
  public void deletePOLine(PurchaseOrderLineData ipPOLKey) throws DBException
  {
    // Make sure keys are present
    if (ipPOLKey.getKeyCount() == 0)
    {
      throw new DBException(
          "Keys must be set for Expected Receipt Line deletion!");
    }

    // Get the records that we will delete
    PurchaseOrderLine vpPOLine = Factory.create(PurchaseOrderLine.class);
    List<Map> vpDeleteList = vpPOLine.getAllElements(ipPOLKey);
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();

      for (Map m : vpDeleteList)
      {
        mpPOLData.clear();
        mpPOLData.setKey(PurchaseOrderLineData.ITEM_NAME, m.get(PurchaseOrderLineData.ITEM_NAME));
        mpPOLData.setKey(PurchaseOrderLineData.LOT_NAME, m.get(PurchaseOrderLineData.LOT_NAME));
        mpPOLData.setKey(PurchaseOrderLineData.LINEID_NAME, m.get(PurchaseOrderLineData.LINEID_NAME));
        mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, m.get(PurchaseOrderLineData.ORDERID_NAME));
        PurchaseOrderLineData vpPOLData = getPoLineRecord(mpPOLData, DBConstants.WRITELOCK);
        if (vpPOLData != null)
        {
          mpPOL.deleteElement(mpPOLData);
          logDeleteExpectedReceiptLine(vpPOLData, null);
        }

        // See if the order header should be deleted too.
        deletePOHeaderIfNecessary(vpPOLData.getOrderID(), null, false);
      }
      commitTransaction(tt);
    }
    catch (NoSuchElementException nse)
    {
      // This should not ever happen due to the read with write lock
      throw new DBException("Data deletion failed", nse);
    }
    catch (DBException e)
    {
      logException(e, "Deleting PO Line");
      throw e;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to delete an ER/PO line.
   *
   * @param isOrderID <code>String</code> containing ER/PO Order ID.
   * @param isLineId <code>String</code> containing line ID for the line to be
   *          deleted.
   * @throws DBException if there is a DB access or update error.
   */
  public void deletePOLine(String isOrderID, String isLineId) throws DBException
  {
    // Check the arguments
    if (isOrderID.trim().length() == 0)
    {
      throw new DBException("Order ID required for Expected Receipt Line deletion!");
    }

    if (isLineId.trim().length() == 0)
    {
      throw new DBException("Line ID required for Expected Receipt Line deletion!");
    }

    // Set up the key
    mpPOLData.clear();
    mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, isOrderID);
    mpPOLData.setKey(PurchaseOrderLineData.LINEID_NAME, isLineId);

    // Perform the deletion
    deletePOLine((PurchaseOrderLineData)mpPOLData.clone());
  }

  /**
   * Method to set the purchase order status to a given value.
   *
   * @param isPurchaseOrderID identifier for Purchase Order
   * @param inNewStatus the new status P.O. will assume
   * @throws DBException if there is a database access or update error.
   */
  public void setPurchaseOrderStatusValue(String isPurchaseOrderID, int inNewStatus)
         throws DBException
  {
    PurchaseOrderHeader vpEH = Factory.create(PurchaseOrderHeader.class);
    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      vpEH.setOrderStatusValue(isPurchaseOrderID, inNewStatus);
      commitTransaction(vpToken);
    }
    finally
    {
      endTransaction(vpToken);
    }
  }

  /**
   *  Method counts POrder Line records.
   *
   *  @param isOrderID <code>String</code> containing Order identifier to count by.
   *         "" if it should be excluded in the search.
   *  @param isItemID <code>String</code> containing Order isItemID to count by.
   *         "" if it should be excluded in the search.
   *  @param isLot <code>String</code> containing Order isLot to count by.
   *         "" if it should be excluded in the search.
   * @return -1 if there is a DB Error counting lines.
   */
  public int getOrderLineCount(String isOrderID, String isItemID, String isLot)
  {
    int vnLineCount = -1;
    mpPOLData.clear();

    if (isOrderID.trim().length() != 0)
    {
      mpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, isOrderID);
    }

    if (isItemID.trim().length() != 0)
    {
      mpPOLData.setKey(PurchaseOrderLineData.ITEM_NAME, isItemID);
    }

    if (isLot.trim().length() != 0)
    {
      mpPOLData.setKey(PurchaseOrderLineData.LOT_NAME, isLot);
    }

    if (mpPOLData.getKeyCount() != 0)
    {
      try
      {
        vnLineCount = mpPOL.getCount(mpPOLData);
      }
      catch(DBException exc)
      {
        logException(exc, getClass().getSimpleName() + ".getOrderLineCount()");
        vnLineCount = -1;
      }
    }

    return(vnLineCount);
  }

  /**
   * Method retrieves the expected Line count of order lines from  the host.
   * @param isOrderID the order id. to search by.
   * @return the count of the expected Order Lines
   * @throws DBException if there is as a database error.
   */
  public int getHostLineCount(String isOrderID) throws DBException
  {
    PurchaseOrderHeader vpOrderHeader = Factory.create(PurchaseOrderHeader.class);
    return(vpOrderHeader.getHostLineCount(isOrderID));
  }

  /**
   *  Method to clean up old expected receipts.
   *
   * @param iDaysOld <code>int</code> Number of days past.
   */
  public void cleanupOldExpectedReceipts(int iDaysOld)
  {
    try
    {
      String[] poList = Factory.create(PurchaseOrderHeader.class).getOldPOStringList(iDaysOld);

      for (int i = 0; i < poList.length; i++)
      {
        logDebug("Cleaning up Expecteed Receipt Order: " + poList[i]);

        TransactionToken tt = null;
        try
        {
          tt = startTransaction();

          deletePO(poList[i]);

          // Add the transaction history for the deletion
          tnData.clear();
          tnData.setTranCategory(DBConstants.ORDER_TRAN);
          tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT);
          tnData.setOrderID(poList[i]);
          logTransaction(tnData);
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

    }
    catch (DBException e)
    {
      logError(e.getMessage());
    }
  }

  /**
   * Merge a new expected receipt with an old ID pending load if possible.
   *
   * @param isStoreStation
   * @param isExpectedReceiptID
   * @throws DBException
   */
  public void mergePossibleIDPendingLoad(String isStoreStation,
      String isExpectedReceiptID) throws DBException
  {
    TransactionToken vpTok = null;
    initializeSchedulerServer();
    try
    {
      vpTok = startTransaction();
      mpSchedServer.createNewLoadAtStation(isExpectedReceiptID, isStoreStation,
          false);
      mpLoadServer.setParentLoadMoveStatus(isExpectedReceiptID,
          DBConstants.ARRIVEPENDING, null);
      if (mpSchedServer.joinLoads(isStoreStation))
      {
        mpSchedServer.handleAutoStoreWithER(isExpectedReceiptID);
      }
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   * Method to identify a possible ID Pending load amongst a list of Expected
   * Loads If any one of the Expected Loads is identified as an ID Pending load
   * at the input station, this method will create an ARRIVE PENDING load which
   * will later be merged
   *
   * @param isStoreStation the station where ID Pending load is physically
   *          located.
   * @param ipELList the list of Expected Loads on an Expected Receipt.
   * @throws DBException if there is a DB access error, or update error.
   */
  protected void mergePossibleIDPendingLoad(String isStoreStation,
                                          List<PurchaseOrderLineData> ipELList)
          throws DBException, InvalidDataException
  {
    String vsIDPendLoad = "";
    initializeLoadServer();
    LoadData vpLoadAtStation = mpLoadServer.getOldestLoadData(isStoreStation,
                                                         DBConstants.IDPENDING);
    if (vpLoadAtStation != null)
    {
      for(PurchaseOrderLineData vpELData : ipELList)
      {
        if (vpELData.getLoadID().equals(vpLoadAtStation.getBCRData().trim()))
        {
          vsIDPendLoad = vpELData.getLoadID();
          break;
        }
      }
    }

    if (!vsIDPendLoad.isEmpty())
    {
      initializeSchedulerServer();
      mpSchedServer.createNewLoadAtStation(vsIDPendLoad, isStoreStation, false);
      mpLoadServer.setParentLoadMoveStatus(vsIDPendLoad,
                                           DBConstants.ARRIVEPENDING, null);
      if (mpSchedServer.joinLoads(isStoreStation))
      {
        mpSchedServer.handleAutoStoreERLoad(vsIDPendLoad, isStoreStation);
      }
    }
  }

  /*========================================================================*/
  /*  The following methods are part of a possibly vain attempt to          */
  /*  standardize transaction history logging.                              */
  /*========================================================================*/

  /**
   * Log the addition of an ER/PO Header
   *
   * @param ipPOHData
   * @param isStation
   */
  public void logAddExpectedReceiptHeader(PurchaseOrderHeaderData ipPOHData)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.ADD_EXPECTED_RECEIPT);
    tnData.setOrderID(ipPOHData.getOrderID());
    tnData.setStation(ipPOHData.getStoreStation());
    logTransaction(tnData);
  }

  /**
   * Log the addition of an ER/PO Line
   *
   * @param ipPOLData
   * @param isStation
   */
  public void logAddExpectedReceiptLine(PurchaseOrderLineData ipPOLData)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.ADD_EXPECTED_RECEIPT_LINE);
    tnData.setOrderID(ipPOLData.getOrderID());
    tnData.setLoadID(ipPOLData.getLoadID());
    tnData.setToLoadID(ipPOLData.getLoadID());
    tnData.setLineID(ipPOLData.getLineID());
    tnData.setItem(ipPOLData.getItem());
    tnData.setLot(ipPOLData.getLot());
    tnData.setExpectedQuantity(ipPOLData.getExpectedQuantity());
    logTransaction(tnData);
  }

  /**
   * Log the deletion of an ER/PO Header
   *
   * @param ipPOHData
   * @param isStation
   */
  public void logDeleteExpectedReceiptHeader(PurchaseOrderHeaderData ipPOHData,
      String isStation)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT);
    tnData.setOrderID(ipPOHData.getOrderID());
    tnData.setStation(isStation);
    logTransaction(tnData);
  }

  /**
   * Log the deletion of an ER/PO Load Line
   *
   * @param ipPOLData
   * @param isStation
   */
  public void logDeleteExpectedReceiptLoadLine(PurchaseOrderLineData ipPOLData,
      String isStation)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT_LINE);
    tnData.setOrderID(ipPOLData.getOrderID());
    tnData.setLoadID(ipPOLData.getLoadID());
    tnData.setToLoadID(ipPOLData.getLoadID());
    tnData.setStation(isStation);
    tnData.setExpectedQuantity(1.0);
    tnData.setCurrentQuantity(1.0);
    tnData.setAdjustedQuantity(1.0);
    tnData.setReceivedQuantity(1.0);
    logTransaction(tnData);
  }

  /**
   * Log the deletion of an ER/PO Line
   *
   * @param ipPOLData
   * @param isStation
   */
  public void logDeleteExpectedReceiptLine(PurchaseOrderLineData ipPOLData,
      String isStation)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT_LINE);
    tnData.setOrderID(ipPOLData.getOrderID());
    tnData.setLoadID(ipPOLData.getLoadID());
    tnData.setToLoadID(ipPOLData.getLoadID());
    tnData.setLineID(ipPOLData.getLineID());
    tnData.setItem(ipPOLData.getItem());
    tnData.setLot(ipPOLData.getLot());
    tnData.setExpectedQuantity(ipPOLData.getExpectedQuantity());
    tnData.setReceivedQuantity(ipPOLData.getReceivedQuantity());
    tnData.setStation(isStation);
    logTransaction(tnData);
  }

  /**
   * Log the modification of an ER/PO Header
   *
   * @param ipPOHData
   * @param isStation
   */
  public void logModifyExpectedReceiptHeader(PurchaseOrderHeaderData ipOldData,
      PurchaseOrderHeaderData ipNewData)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.MODIFY_EXPECTED_RECEIPT);
    tnData.setOrderID(ipOldData.getOrderID());
    if (logDataChanged(ipOldData, ipNewData) == true)
    {
      logTransaction(tnData);
    }
  }

  /**
   * Log the modification of an ER/PO Line
   *
   * @param ipPOHData
   * @param isStation
   */
  public void logModifyExpectedReceiptLine(PurchaseOrderLineData ipOldData,
      PurchaseOrderLineData ipNewData)
  {
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.MODIFY_EXPECTED_RECEIPT_LINE);
    tnData.setOrderID(ipOldData.getOrderID());

    double vdAdjustedQuantity = ipNewData.getExpectedQuantity()
        - ipOldData.getExpectedQuantity();
    if (vdAdjustedQuantity != 0)
    {
      tnData.setAdjustedQuantity(vdAdjustedQuantity);
    }

    if (logDataChanged(ipOldData, ipNewData) == true)
    {
      logTransaction(tnData);
    }
  }

  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/
  protected void initializeInventoryServer()
  {
    if (mpInvServer == null)
    {
      mpInvServer = Factory.create(StandardInventoryServer.class,
                                   getClass().getSimpleName());
    }
  }

  protected void initializeSchedulerServer()
  {
    if (mpSchedServer == null)
    {
      mpSchedServer = Factory.create(StandardSchedulerServer.class,
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

  protected void initializeStationServer()
  {
    if (mpStationServ == null)
    {
      mpStationServ = Factory.create(StandardStationServer.class);
    }
  }

  protected void initializeHostServer()
  {
    if (mpHostServ == null)
    {
      mpHostServ = Factory.create(StandardHostServer.class);
    }
  }
}

