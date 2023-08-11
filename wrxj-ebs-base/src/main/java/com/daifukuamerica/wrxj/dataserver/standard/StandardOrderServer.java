package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderProcessor;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.Customer;
import com.daifukuamerica.wrxj.dbadapter.data.CustomerData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;


/**
 * Description:<p>
 *   Server to handle Order Specific operations.  This class is actually working
 *   with two interdependent data classes: OrderHeader and OrderLine.</p>
 *
 * @author     A.D.
 * @version    1.0
 * @since      20-May-02
 */
public class StandardOrderServer extends StandardServer
{
  protected StandardStationServer   mpStationServer = null;
  protected StandardHostServer      mpHostServ      = null;
  protected StandardInventoryServer mpInvServ       = null;
  
  protected OrderHeader mpOrderHeader = Factory.create(OrderHeader.class);
  protected OrderHeaderData mpOHData = Factory.create(OrderHeaderData.class);
  protected OrderLine mpOrderLine = Factory.create(OrderLine.class);
  protected final OrderLineData mpOLData = Factory.create(OrderLineData.class);
  protected ShortOrderProcessor mpShortOrderProcessor = null;
  
  protected Random rand = new Random();

  public StandardOrderServer()
  {
    this(null);
  }

  public StandardOrderServer(String keyName)
  {
    super(keyName);
    logDebug("StandardOrderServer.createOrderServer()");
  }

  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardOrderServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  logDebug("StandardOrderServer.createOrderServer()");
  }

  /**
   *  Method adds Order Header to the database.
   *  
   *  @param ohdata <code>OrderHeaderData</code> containing data to add.
   *  
   *  @throws <code>DBException</code> if dependency records don't exist, 
   *                or a database add error.
   */
  public void addOrderHeader(OrderHeaderData ohdata) throws DBException
  { 
/*---------------------------------------------------------------------------
      If the Order already exists, reject order.  If the carrier doesn't
      exist reject order.
  ---------------------------------------------------------------------------*/
    if (mpOrderHeader.exists(ohdata.getOrderID()))
    {
      throw new DBException("Duplicate data add error! Order '" +
                            ohdata.getOrderID() + "' already exists!", true);
    }
    else if (ohdata.getCarrierID().trim().length() != 0)
    {
      StandardCarrierServer carrierServer = Factory.create(StandardCarrierServer.class);
      if (!carrierServer.exists(ohdata.getCarrierID()))
      {
        throw new DBException("Carrier " + ohdata.getCarrierID() +
                              " does not exist!  Order " + ohdata.getOrderID() +
                              " not added...");
      }
    }

/*---------------------------------------------------------------------------
          Make sure the customer exists before adding order header.
  ---------------------------------------------------------------------------*/
    if (ohdata.getShipCustomer().trim().length() != 0)
    {
      if (!customerExists(ohdata.getShipCustomer()))
      {
        throw new DBException("Customer " + ohdata.getShipCustomer() +
                              " does not exist!  Order " + ohdata.getOrderID() +
                              " not added...");
      }
    }
    
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
                                       // Default this to the scheduled date
                                       // when the order is first added.
      ohdata.setShortOrderCheckTime(ohdata.getScheduledDate());
      mpOrderHeader.addElement(ohdata);
                                       // Log Order Add history
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.ADD_ORDER);
      tnData.setOrderID(ohdata.getOrderID());
      tnData.setOrderType(ohdata.getOrderType());
      tnData.setToStation(ohdata.getDestinationStation());
      logTransaction(tnData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside addOrderHeader");
      throw new DBException("StandardOrderServer.addOrderHeader " + e.getMessage(), e);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Method modifies Order Header in the database.  The order must be in
   *  READY, or HOLD state to be modified.
   *
   *  @param ohdata <code>OrderHeaderData</code> containing Key and
   *         data to modify.
   *
   *  @throws <code>DBException</code>
   */
  public String modifyOrderHeader(OrderHeaderData ohdata) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      if(ohdata.getKeyCount() == 0)
        ohdata.setKey(OrderHeaderData.ORDERID_NAME, ohdata.getOrderID());
      logModifyTransaction(ohdata);
      mpOrderHeader.modifyElement(ohdata);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside modifyOrderHeader");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }

    return ("Order modified successfully");
  }

  /**
   *  Modify order, including priority changes and manifest info if necessary
   *  (This was extracted from OrderSearchFrame.)
   * 
   *  @param ipNewOHData - the new order header data
   *  @param izUpdatePriority - change move priority
   *  @return
   *  @throws DBException
   */
  public String modifyOrder(OrderHeaderData ipNewOHData, boolean izUpdatePriority)
    throws DBException
  {
    String vsReturnMessage = "Aw, crap.";
    String vsOrderID = ipNewOHData.getOrderID();

    /*
     * Modify the order header
     */
    ipNewOHData.setKey(OrderHeaderData.ORDERID_NAME, vsOrderID);
    vsReturnMessage = modifyOrderHeader(ipNewOHData);

    /*
     * Update any moves if the priority changed
     */
    StandardMoveServer vpMoveServer = Factory.create(StandardMoveServer.class);
    if (izUpdatePriority)
    {
      int mvcount = 0;
      MoveData vpMoveData = Factory.create(MoveData.class);
      vpMoveData.setKey(OrderHeaderData.ORDERID_NAME, vsOrderID);
      mvcount = vpMoveServer.getMoveCount(vpMoveData);
      if(mvcount > 0)
      {
        vpMoveData.setKey(OrderHeaderData.ORDERID_NAME, vsOrderID);
        vpMoveData.setPriority(ipNewOHData.getPriority());
        vpMoveServer.modifyMove(vpMoveData);
      }
    }

    return vsReturnMessage;
  }

  /**
   *  Method adds Order Line to the database. This method by default checks the
   *  order line for validity against the item master.
   * 
   *  @param oldata <code>OrderLineData</code> containing data to add.
   *  @throws <code>DBException</code> if there is a duplicate record.
   */
  public String addOrderLine(OrderLineData oldata) throws DBException
  {
    addOrderLine(oldata, true);
    return ("Order Line Added Successfully");
  }
  
  /**
   *  Method adds Order Line to the database.
   * 
   *  @param oldata <code>OrderLineData</code> containing data to add.
   *  @param checkItemMaster <code>boolean</code> if set to <code>true</code>
   *            this method validates item exists in Item Master.
   *  @throws <code>DBException</code> if there is a duplicate record.
   */
  public String addOrderLine(OrderLineData oldata, boolean checkItemMaster)
         throws DBException
  {
    if (checkItemMaster)
    {
      if (oldata.getItem().trim().length() == 0)
      {
        throw new DBException("Item not specified for Order " + oldata.getOrderID() +
                               " Line " + oldata.getLineID());
      }
      else if (!itemExists(oldata.getItem()))
      {
                                         // Add default item master if we're
                                         // configured that way.
        if (Application.getBoolean("AddDefaultOrderItem", false))
        {
          initializeInventoryServer();
          mpInvServ.addDefaultItem(oldata.getItem());
        }
        else
        {
          throw new DBException("Item " + oldata.getItem() + " doesn't exist for " +
                                "Order " + oldata.getOrderID());
        }
      }
    }
    else if (oldata.getLoadID().trim().length() != 0)
    {                                  // If the load is filled in, assume this
                                       // is a Load Order. Make sure the load exists.
      Load vpLoad = Factory.create(Load.class);
      if (!vpLoad.exists(oldata.getLoadID()))
      {
        throw new DBException("Load " + oldata.getLoadID() +
                              " doesn't exist for Order " + oldata.getOrderID(),HostError.NO_DATA_FOUND);
      }
    }

                                       // Check for duplicate record.
    oldata.clearKeys();
    oldata.setKey(OrderLineData.ORDERID_NAME, oldata.getOrderID());
    oldata.setKey(OrderLineData.ITEM_NAME, oldata.getItem());
    oldata.setKey(OrderLineData.ORDERLOT_NAME, oldata.getOrderLot());
    oldata.setKey(OrderLineData.LINEID_NAME, oldata.getLineID());
    oldata.setKey(OrderLineData.LOADID_NAME, oldata.getLoadID());

    if (mpOrderLine.exists(oldata))
    {
      throw new DBException("Order Line " + oldata.getOrderID() + ", " +
                            oldata.getItem() + ", " + oldata.getOrderLot() + ", " +
                            oldata.getLineID() + " already Exists!", true);
    }
    else if (oldata.getOrderQuantity() <= 0)
    {
      throw new DBException("Order Quantity may not be negative or zero! " +
                            "Order: " + oldata.getOrderID()  +
                            ", Item: " + oldata.getItem()    + 
                            ", Lot: " + oldata.getOrderLot() + 
                            ", Line: " + oldata.getLineID());
    }
    else if (oldata.getRouteID().trim().length() != 0)
    {                                  // Validate route if it is provided.
      StandardRouteServer routeServer = Factory.create(StandardRouteServer.class);
      if (!routeServer.exists(oldata.getRouteID().trim()))
      {
        throw new DBException("Invalid Route \'" + oldata.getRouteID() +
                              "\' specified in Order Line. " +
                              "Order: " + oldata.getOrderID() +
                              ", Item: " + oldata.getItem() + 
                              ", Lot: " + oldata.getOrderLot() + 
                              ", Line: " + oldata.getLineID());
      }
    }

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOrderLine.addElement(oldata);
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.ADD_ORDER_LINE);
      tnData.setOrderID(oldata.getOrderID());
      tnData.setLineID(oldata.getLineID());
      tnData.setItem(oldata.getItem());
      tnData.setLot(oldata.getOrderLot());
      tnData.setLoadID(oldata.getLoadID());
      tnData.setCurrentQuantity(oldata.getOrderQuantity());
      logTransaction(tnData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside addOrderLine");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }

    return ("Order Line Added Successfully");
  }

 /**
  *  Method modifies an Order Line.  This method only allows a modify if the
  *  Order is on HOLD, or ORBUILDING.  It is assumed that a unique key is
  *  provided in the passed in oldata class.
  *
  *  @param oldata <code>OrderLineData</code> containing Key and
  *         data to modify.
  *  @param izCheckOrderStatus flag indicating if order status should be checked
  *        before modification is allowed.
  *  @throws DBException if the various types of validation fail.
  */
  public String modifyOrderLine(OrderLineData oldata, boolean izCheckOrderStatus)
         throws DBException
  {
    String orderID = oldata.getOrderID();
    oldata.setKey(OrderLineData.ORDERID_NAME, orderID);
    oldata.setKey(OrderLineData.LINEID_NAME, oldata.getLineID());

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      OrderHeaderData ohdata = getOrderHeaderRecord(orderID);
      OrderLineData currentOrderLine = getOrderLineRecord(oldata, true);
      
      if (currentOrderLine == null)    // There is a problem with the modification
      {                                // data!  Not enough supporting keys to find data...
        throw new DBException("Order Line not found for order! " +
                              "Order: " + oldata.getOrderID()  +
                              ", Item: " + oldata.getItem()    + 
                              ", Lot: " + oldata.getOrderLot() + 
                              ", Line: " + oldata.getLineID());
      }
      
      if (izCheckOrderStatus && ohdata.getOrderStatus() != DBConstants.HOLD &&
          ohdata.getOrderStatus() != DBConstants.ORBUILDING)
      {
        String sOrderStatus = "";
        try {sOrderStatus = DBTrans.getStringValue(OrderHeaderData.ORDERSTATUS_NAME, ohdata.getOrderStatus()); }
        catch (NoSuchFieldException nf) {}
        
        throw new DBException("Order '" + ohdata.getOrderID() + "' is in " +
                              sOrderStatus + " status and may not be modified!");
      }
      else if (ohdata.getOrderType() == DBConstants.CONTAINER)
      {
        throw new DBException("CONTAINER orders may not be modified!");
      }
      
      ColumnObject vpObj = oldata.getColumnObject(OrderLineData.ORDERQUANTITY_NAME);
/*============================================================================
   An order qty. was given for modification. Make sure it is positive and
   greater than the picked quantity.
  ============================================================================*/
      if (vpObj != null)
      {
        double vdOrderQty = (Double)vpObj.getColumnValue();
        if(vdOrderQty != currentOrderLine.getOrderQuantity())
        {
          if (vdOrderQty <= 0.0)
          {
            throw new DBException("Order quantity received for modification is " + 
                                  "not a positive value! " +
                                  "Order: " + oldata.getOrderID()  +
                                  ", Item: " + oldata.getItem()    + 
                                  ", Lot: " + oldata.getOrderLot() + 
                                  ", Line: " + oldata.getLineID());
          }
          else if (currentOrderLine.getPickQuantity() > oldata.getOrderQuantity() ||
                   currentOrderLine.getAllocatedQuantity() > oldata.getOrderQuantity())
          {
            throw new DBException("Order quantity received for modification is " + 
                                  "not allowed to be less than what has already been " +
                                  "picked, or allocated..." +
                                  "Order: " + oldata.getOrderID()  +
                                  ", Item: " + oldata.getItem()    + 
                                  ", Lot: " + oldata.getOrderLot() + 
                                  ", Line: " + oldata.getLineID());
          }
        }
      }
      logModifyTransaction(currentOrderLine, oldata);
      mpOrderLine.modifyElement(oldata);
      commitTransaction(vpTok);
    }
    catch(DBException e)
    {
      logException(e, "Inside modifyOrderLine");
      throw e;
    }
    catch (NoSuchElementException ne)
    {
      throw new DBException("Modifying OL for order '" + oldata.getOrderID() + "'");
    }
    finally
    {
      endTransaction(vpTok);
    }

    return ("Order Line modified successfully");
  }

 /**
  *  A no holds barred Order line update method. This method is meant to be
  *  called only by another server with the assumption that the calling server
  *  has taken the necessary precautions for the update.
  *  @param ipOLKey Order line modify key.
  *  @throws DBException if there is a database update/access error.
  */
  public void modifyOrderLine(OrderLineData ipOLKey) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      logModifyTransaction(ipOLKey);
      mpOrderLine.modifyElement(ipOLKey);
      commitTransaction(vpTok);
    }
    catch(NoSuchElementException nse)
    {
      String vsOrderID = (String)ipOLKey.getKeyObject(OrderLineData.ORDERID_NAME).getColumnValue();
      throw new DBException("Error updating OL record for Order: " + vsOrderID,
                            nse);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
  
  /**
   * Method to delete Orders.
   * 
   * @param isOrderID <code>String</code> Order ID. to delete.
   * 
   * @return <code>int</code> value set to DBConstants.DONE, or
   *         DBConstants.KILLED depending on what the next order status is. -1
   *         if the order doesn't exist.
   */
  public int deleteOrder(String isOrderID) throws DBException
  {
    int ordStatus = 0;
    OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
    OrderHeaderData ordData = null;

    ohdata.setKey(OrderHeaderData.ORDERID_NAME, isOrderID);
    ordData = mpOrderHeader.getElement(ohdata, DBConstants.NOWRITELOCK);
    
    if (ordData == null)               // If the order doesn't even exist.
    {
      return (-1);
    }
                                       // See if the order is in the right
                                       // status to be deleted.
    ordStatus = ordData.getOrderStatus();
    if (ordStatus == DBConstants.ALLOCATING)
    {
      throw new DBException("Order is being allocated\nand can't be deleted!");
    }
    
    TransactionToken vpTok = null;
    int nextStatus = DBConstants.DONE;
    try
    {
      if (loadLinesHaveOrders(isOrderID) || orderHasMovingLoads(isOrderID))
      {
        nextStatus = DBConstants.KILLED;
      }

      vpTok = startTransaction();
      
      // Deallocate moves
      backOffInventory(isOrderID);
      
      // If there are ANY moves left, mark the order as KILLED.
      MoveData mvdata = Factory.create(MoveData.class);
      mvdata.setKey(MoveData.ORDERID_NAME, isOrderID);
      if (Factory.create(Move.class).getCount(mvdata) > 0)
        nextStatus = DBConstants.KILLED;
      mvdata.clear();
      mvdata = null;
      
      // At this point nextStatus should be set to either DONE or KILLED
      if (nextStatus == DBConstants.DONE)
      {
        // Carry out the deletion of headerData and lines.
        executeDeletion(ordData);
        if (mzHasHostSystem)
        {
          initializeHostServer();
          mpHostServ.sendOrderComplete(ordData);
        }
      }
      else
      {
        setOrderStatusValue(isOrderID, ordStatus, nextStatus);
      }
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "deleteOrder");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }

    return nextStatus;
  }

  /**
   *  Method to delete line.
   *
   *  @param sOrderID <code>String</code> containing Order ID.
   *  @param sLineId <code>String</code> containing line ID for OrderId to be deleted.
   */
  public void deleteOrderLine(String sOrderID, String sLineId) throws DBException
  {
    OrderLineData lookupKey = Factory.create(OrderLineData.class);

    if (sOrderID.trim().length() == 0)
    {
      throw new DBException("Order ID required for Order Line deletion!");
    }
    lookupKey.setKey(OrderLineData.ORDERID_NAME, sOrderID);

    if (sLineId.trim().length() == 0)
    {
      throw new DBException("Line ID required for Order Line deletion!");
    }
    lookupKey.setKey(OrderLineData.LINEID_NAME, sLineId);

    TransactionToken vpTok = null;
    try
    {
                                      // Read the record for transaction logging only
      OrderLineData tmpLineData = getOrderLineRecord(lookupKey);
      vpTok = startTransaction();
                                      // Log the delete transaction
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.DELETE_ORDER_LINE);
      tnData.setOrderID(sOrderID);
      tnData.setLineID(tmpLineData.getLineID());
      tnData.setItem(tmpLineData.getItem());
      tnData.setLot(tmpLineData.getOrderLot());
      tnData.setCurrentQuantity(tmpLineData.getOrderQuantity());
      logTransaction(tnData);
      
      mpOrderLine.deleteElement(lookupKey);      
                                       // See if the order header should be
                                       // deleted too.
      switch (getOrderLineCount(sOrderID, "", ""))
      {
        case 0 :
                                       // Read the record for transaction logging only
          OrderHeaderData ohdata = getOrderHeaderRecord(sOrderID);
          ohdata.setKey(OrderHeaderData.ORDERID_NAME, sOrderID);
          mpOrderHeader.deleteElement(ohdata);
                                       // Log the delete transaction
          tnData.clear();
          tnData.setTranCategory(DBConstants.ORDER_TRAN);
          tnData.setTranType(DBConstants.DELETE_ORDER);
          tnData.setOrderID(sOrderID);
          tnData.setOrderType(ohdata.getOrderType());
          tnData.setToStation(ohdata.getDestinationStation());
          logTransaction(tnData);
          if (mzHasHostSystem)
          {
            initializeHostServer();
            mpHostServ.sendOrderComplete(ohdata);
          }

          commitTransaction(vpTok);
          break;

        case -1 :
          throw new DBException("Error counting order line.");

        default :
          commitTransaction(vpTok);
      }
    }
    catch (DBException e)
    {
      logException(e, "deleteLoadOrderLine");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
  /**
   *  Method to delete line.
   *
   *  @param sOrderID <code>String</code> containing Order ID.
   *  @param sItem <code>String</code> containing Item OrderId to be deleted.
   *  @param sLot <code>String</code> containing Lot for OrderId to be deleted.
   *  @param sLineId <code>String</code> containing line ID for OrderId to be deleted.
   */
  public void deleteOrderLine(String sOrderID, String sItem, String sLot,
                              String sLineId) throws DBException
  {
    OrderLineData lookupKey = Factory.create(OrderLineData.class);

    if (sOrderID.trim().length() == 0)
    {
      throw new DBException("Order ID required for Order Line deletion!");
    }
    lookupKey.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    lookupKey.setKey(OrderLineData.ITEM_NAME, sItem);
    lookupKey.setKey(OrderLineData.ORDERLOT_NAME, sLot);   
    lookupKey.setKey(OrderLineData.LINEID_NAME, sLineId);

    TransactionToken vpTok = null;
    try
    {
                                      // Read the record for transaction logging only
      OrderLineData tmpLineData = getOrderLineRecord(lookupKey);
      vpTok = startTransaction();
                                      // Log the delete transaction
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.DELETE_ORDER_LINE);
      tnData.setOrderID(sOrderID);
      tnData.setLineID(tmpLineData.getLineID());
      tnData.setItem(tmpLineData.getItem());
      tnData.setLot(tmpLineData.getOrderLot());
      tnData.setCurrentQuantity(tmpLineData.getOrderQuantity());
      logTransaction(tnData);
      
      mpOrderLine.deleteElement(lookupKey);      
                                       // See if the order header should be
                                       // deleted too.
      switch (getOrderLineCount(sOrderID, "", ""))
      {
        case 0 :
                                       // Read the record for transaction logging only
          OrderHeaderData ohdata = getOrderHeaderRecord(sOrderID);
          ohdata.setKey(OrderHeaderData.ORDERID_NAME, sOrderID);
          mpOrderHeader.deleteElement(ohdata);
                                       // Log the delete transaction
          tnData.clear();
          tnData.setTranCategory(DBConstants.ORDER_TRAN);
          tnData.setTranType(DBConstants.DELETE_ORDER);
          tnData.setOrderID(sOrderID);
          tnData.setOrderType(ohdata.getOrderType());
          tnData.setToStation(ohdata.getDestinationStation());
          logTransaction(tnData);
          if (mzHasHostSystem)
          {
            initializeHostServer();
            mpHostServ.sendOrderComplete(ohdata);
          }

          commitTransaction(vpTok);
          break;

        case -1 :
          throw new DBException("Error counting order line.");

        default :
          commitTransaction(vpTok);
      }
    }
    catch (DBException e)
    {
      logException(e, "deleteLoadOrderLine");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Method to delete Order Lines.  This method will delete the OrderHeader
   *  if this order Line happens to be the last one.  <br><strong>Note:</strong>
   *  it is assumed that the load line item will have the order number it is
   *  being picked for when the pick is completed.
   *
   *  @param sOrderID <code>String</code> containing Order identifier to search
   *         by for the deletion.  "" if it should be excluded in the search.
   *  @param sItem <code>String</code> containing Item identifier to search
   *         by for the deletion.  "" if it should be excluded in the search.
   *  @param sOrderLot <code>String</code> containing Order Lot to search
   *         by for the deletion.  "" if it should be excluded in the search.
   *  @param sLineID <code>String</code> containing Order Line ID to search
   *         by for the deletion.  "" if it should be excluded in the search.
   */
  public void deleteItemOrderLine(String sOrderID, String sItem, String sOrderLot, 
                                  String sLineID) throws DBException
  {
    int ordStatus = getOrderStatusValue(sOrderID);
    if (ordStatus != DBConstants.HOLD && ordStatus != DBConstants.KILLED)
    {
      throw new DBException("Order is not on HOLD or KILLED!\nNo line items deleted!");
    }
                                        // If something has already been picked
                                        // against this order line don't allow
                                        // it to be deleted!
    if (hasPickedQuantities(sOrderID, sItem, sOrderLot, sLineID))
    {
      throw new DBException("Order line has allocated loads\nand may not be deleted");
    }
                                       // See if there are any loads out there
                                       // for this order.
    StandardInventoryServer invServ = Factory.create(StandardInventoryServer.class);

    int idCount = invServ.getLoadLineCount("", sItem, "", sOrderID, sLineID);
    if (idCount != -1 && idCount > 0)
    {
      throw new DBException("Order line has allocated loads\nand may not be deleted");
    }
    //
    // If there are ANY moves left, for this line don't let it be deleted
    //
    MoveData mvdata = Factory.create(MoveData.class);
    mvdata.setKey(MoveData.ORDERID_NAME, sOrderID);
    mvdata.setKey(MoveData.ITEM_NAME, sItem);
    mvdata.setKey(MoveData.ORDERLOT_NAME, sOrderLot);
    mvdata.setKey(MoveData.LINEID_NAME, sLineID);

    if (Factory.create(Move.class).getCount(mvdata) > 0)
    {
      throw new DBException("Order line has assigned Moves\nand may not be deleted");
    }

    deleteOrderLine(sOrderID, sItem, sOrderLot, sLineID);
  }
  
  /**
   *  Method to delete line item for a load order.
   *
   *  @param sOrderID <code>String</code> containing Order ID.
   *  @param sLoadID <code>String</code> containing load ID. of load tied to order.
   */
  public void deleteLoadOrderLine(String sOrderID, String sLoadID)
         throws DBException
  {
    OrderLineData tmpLineData = Factory.create(OrderLineData.class);

    if (sOrderID.trim().length() == 0)
    {
      throw new DBException("Order ID required for Order Line deletion!");
    }
    tmpLineData.setKey(OrderLineData.ORDERID_NAME, sOrderID);

    if (sLoadID.trim().length() == 0)
    {
      throw new DBException("Load ID required for Order Line deletion!");
    }
    tmpLineData.setKey(OrderLineData.LOADID_NAME, sLoadID);

    StandardLoadServer loadServ = Factory.create(StandardLoadServer.class);

    if (!loadServ.isLoadInRack(sLoadID))
    {
      throw new DBException("Order line has moving load!");
    }

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
               // Log the Delete Transaction
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.DELETE_ORDER_LINE);
      tnData.setOrderID(sOrderID);
      tnData.setLoadID(sLoadID);
      logTransaction(tnData);
      
      mpOrderLine.deleteElement(tmpLineData);

                                       // See if the order header should be
                                       // deleted too.
      switch (getOrderLineCount(sOrderID, "", ""))
      {
        case 0 :
          OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
                                       // Read the record for transaction
                                       // logging only.
          ohdata = getOrderHeaderRecord(sOrderID);
          ohdata.setKey(OrderHeaderData.ORDERID_NAME, sOrderID);
          mpOrderHeader.deleteElement(ohdata);
                                       // Log the delete transaction
          tnData.clear();
          tnData.setTranCategory(DBConstants.ORDER_TRAN);
          tnData.setTranType(DBConstants.DELETE_ORDER);
          tnData.setOrderID(sOrderID);
          tnData.setOrderType(ohdata.getOrderType());
          tnData.setToStation(ohdata.getDestinationStation());
          logTransaction(tnData);
          commitTransaction(vpTok);
          break;

        case -1 :
          throw new DBException("Error counting order line.");

        default :
          commitTransaction(vpTok);
      }
    }
    catch (DBException e)
    {
      logException(e, "deleteLoadOrderLine");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
      tmpLineData.clear();
      tmpLineData = null;
    }
  }

  /**
   *  Method gets a list of order id's based on order status, and last time order
   *  was checked for short allocation. The list is ordered by the oldest such
   *  short order.
   * 
   *  @param ipOrderStatuses translation value denoting order status.
   *  @param inOrderType translation value denoting order type. <i>This is an
   *            optional parameter that provides additional filtering. <b>This
   *            parameter is ignored if it is passed as 0</b></i>
   *  @return String array of order id's. ordered by the oldest to newest
   *         scheduled date.
   */
  public String[] getCheckedShortOrderList(int[] ipOrderStatuses,
      int inOrderType)
  {
    try
    {
      return mpOrderHeader.getCheckedShortOrderList(ipOrderStatuses,
          inOrderType);
    }
    catch (DBException dbe)
    {
      logException("getCheckedShortOrderList", dbe);
      return new String[0];
    }
  }

  /**
   *  Gets a list of order header data based on field name and value.
   * 
   *  @param fieldName <code>String</code> containing DB Column name.
   *  @param fieldValue <code>int</code> containing Column value.
   *  @return <code>List</code> of Order data.
   */
  public List<Map> getOrderHeaderData(String fieldName, int fieldValue)
    throws DBException
  {
    OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
    ohdata.setKey(fieldName, Integer.valueOf(fieldValue));

    return(getOrderHeaderData(ohdata));
  }

  /**
   *  Gets a list of order header data based on field name and value.  It is
   *  assumed that the fieldValue is not necessarily exact but rather a pattern.
   *
   *  @param fieldName <code>String</code> containing DB Column name.
   *  @param fieldValue <code>String</code> containing Column value.
   *
   *  @return <code>List</code> of Order data.
   */
  public List<Map> getOrderHeaderData(String fieldName, String fieldValue)
    throws DBException
  {
    OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
    if (fieldValue != null && fieldValue.trim().length() != 0)
    {
      ohdata.setKey(fieldName, fieldValue, KeyObject.LIKE);
    }
    else
    {
      ohdata.setKey(fieldName, fieldValue);
    }

    return (getOrderHeaderData(ohdata));
  }

  /**
   *  Gets a list of order header data based on keys passed in ohdata.
   *  @param ohdata <code>OrderHeaderData</code> object containing search
   *         information.
   *  @return <code>List</code> of Order data.
   */
  public List<Map> getOrderHeaderData(OrderHeaderData ohdata) throws DBException
  {
    return mpOrderHeader.getAllElements(ohdata);
  }

  /**
   * Method retrieves the expected Line count of order lines from the host.
   * 
   * @param isOrderID the order id. to search by.
   * @return the count of the expected Order Lines
   * @throws DBException if there is as a database error.
   */
  public int getOrderHostLineCount(String isOrderID) throws DBException
  {
    return mpOrderHeader.getHostLineCount(isOrderID);
  }

  /**
   *  Gets a list of order header data based on keys passed in ohdata and an
   *  Item, Lot combo.
   * 
   *  @param ohdata
   *  @param sItem
   *  @param sLot
   *  @return
   *  @throws DBException
   */
  public List<Map> getOrderSearchList(OrderHeaderData ohdata,
      OrderLineData oldata) throws DBException
  {
    TableJoin tj = Factory.create(TableJoin.class);
    return (tj.getOrderSearchList(ohdata, oldata));
  }

  /**
   * Convenience methods.
   */
  public List<Map> getOrderLineData(String sOrderID) throws DBException
  {
    return (getOrderLineData(sOrderID, "", "", "", ""));
  }

  /**
   * Convenience methods.
   */
  public List<Map> getLoadOrderLineData(String sLoadID) throws DBException
  {
    return(getOrderLineData("", sLoadID, "", "", ""));
  }

  /**
   * Convenience methods.
   */
  public List<Map> getOrderLineData(String sOrderID, String sLoadID)
    throws DBException
  {
    return(getOrderLineData(sOrderID, sLoadID, "", "", ""));
  }

  /**
   * Convenience methods.
   */
  public List<Map> getOrderLineData(String sOrderID, String sLoadID, String sItem)
    throws DBException
  {
    return(getOrderLineData(sOrderID, sLoadID, sItem, "", ""));
  }

  /**
   * Convenience methods.
   */
  public List<Map> getOrderLineData(String sOrderID, String sLoadID, String sItem,
                               String sOrderLot) throws DBException
  {
    return(getOrderLineData(sOrderID, sLoadID, sItem, sOrderLot, ""));
  }

  /**
   *  Reads a set of Location records matching criteria provided by the
   *  arguments.  <strong>Note:</strong> if a particular piece of data is
   *  to be excluded from the search, then it is assumed to be a blank zero
   *  length string or else a -1 for integers.
   *
   *  @param sOrderID <code>String</code> containing Order identifier to search by.
   *         "" if it should be excluded in the search.
   *  @param sLoadID <code>String</code> containing Order load ID. to search
   *         by. "" if it should be excluded in the search.
   *  @param sItem <code>String</code> containing Order Item to search by.
   *         "" if it should be excluded in the search.
   *  @param sOrderLot <code>String</code> containing Order lot to search by.
   *         "" if it should be excluded in the search.
   *  @param sRouteID <code>String</code> containing order Route to search by.
   *         "" if it should be excluded in the search.
   */
  public List<Map> getOrderLineData(String sOrderID, String sLoadID, String sItem,
                               String sOrderLot, String sRouteID)
         throws DBException
  {
    OrderLineData oldata = Factory.create(OrderLineData.class);
    if (sOrderID.trim().length() != 0)
    {
      oldata.setOrderID(sOrderID);
      oldata.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    }

    if (sLoadID.trim().length() != 0)
    {
      oldata.setLoadID(sLoadID);
      oldata.setKey(OrderLineData.LOADID_NAME, sLoadID);
    }

    if (sItem.trim().length() != 0)
    {
      oldata.setItem(sItem);
      oldata.setKey(OrderLineData.ITEM_NAME, sItem);
    }

    if (sOrderLot.trim().length() != 0)
    {
      oldata.setOrderLot(sOrderLot);
      oldata.setKey(OrderLineData.ORDERLOT_NAME, sOrderLot);
    }

    if (sRouteID.trim().length() != 0)
    {
      oldata.setRouteID(sRouteID);
      oldata.setKey(OrderLineData.ROUTEID_NAME, sRouteID);
    }

    return mpOrderLine.getAllElements(oldata);
  }

  /**
   *  Reads a set of orderline data matching criteria provided by the
   *  arguments.  <strong>Note:</strong> if a particular piece of data is
   *  to be excluded from the search, then it is assumed to be a blank zero
   *  length string or else a -1 for integers.
   *
   *  @param LoadLineItemData vpLLI - loadlineitem from load picked for order
   */
  public OrderLineData getOrderLineData(LoadLineItemData ipLLI)
         throws DBException
  {
    String vpsOrderID = ipLLI.getOrderID();
    String vpsItem = ipLLI.getItem();
    String vpsOrderLot = ipLLI.getOrderLot();
    String vpsLineID = ipLLI.getLineID();
    
    OrderLineData oldata = Factory.create(OrderLineData.class);
    if (vpsOrderID != null && vpsOrderID.trim().length() != 0)
    {
      oldata.setOrderID(vpsOrderID);
      oldata.setKey(OrderLineData.ORDERID_NAME, vpsOrderID);
    }

    if (vpsItem != null && vpsItem.trim().length() != 0)
    {
      oldata.setItem(vpsItem);
      oldata.setKey(OrderLineData.ITEM_NAME, vpsItem);
    }

    if (vpsOrderLot != null && vpsOrderLot.trim().length() != 0)
    {
      oldata.setOrderLot(vpsOrderLot);
      oldata.setKey(OrderLineData.ORDERLOT_NAME, vpsOrderLot);
    }

    if (vpsLineID != null && vpsLineID.trim().length() != 0)
    {
      oldata.setLineID(vpsLineID);
      oldata.setKey(OrderLineData.LINEID_NAME, vpsLineID);
    }

    return mpOrderLine.getElement(oldata, DBConstants.NOWRITELOCK);
  }

  /**
   *  Gets a Order Header record.
   */
  public OrderHeaderData getOrderHeaderRecord(OrderHeaderData ordData,
      int lockFlag) throws DBException
  {
    return mpOrderHeader.getElement(ordData, lockFlag);
  }

  /**
   *  Convenience method. Gets a Order Header with no Lock.
   */
  public OrderHeaderData getOrderHeaderRecord(OrderHeaderData ordData)
    throws DBException
  {
    return getOrderHeaderRecord(ordData, DBConstants.NOWRITELOCK);
  }

  /**
   *  Convenience method. Gets a Order Header with no Lock, and only the
   *  order ID.
   *  @param orderID <code>String</code> containing order id of record being
   *         read.
   */
  public OrderHeaderData getOrderHeaderRecord(String orderID)
    throws DBException
  {
    OrderHeaderData ordData = Factory.create(OrderHeaderData.class);
    ordData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    return (getOrderHeaderRecord(ordData));
  }

  /**
   *  Convenience method. Gets a Order Header with lock flag specified.
   *  @param orderID <code>String</code> containing order id of record being
   *         read.
   *  @param writeLockFlag <code>int</code> specifying if record is locked.
   */
  public OrderHeaderData getOrderHeaderRecord(String orderID, int writeLockFlag)
         throws DBException
  {
    OrderHeaderData ordData = Factory.create(OrderHeaderData.class);
    ordData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    return (getOrderHeaderRecord(ordData, writeLockFlag));
  }

 /**
  *  Gets unique Order Line record based on specified key.
  *  @param ipOLKey the criteria to find unique Order Line record.
  *  @param izWithLock <code>true</code> indicates read with lock. <b>Note:</b>
  *        <u>This method must be called from within a transaction if this flag
  *        is set to <code>true</code>.</u>
  *  @return null if no records are found.
  *  @throws DBException If a unique record can't be found based on specified
  *         key (but something was still found).  Also if DB access errors.
  */
  public OrderLineData getOrderLineRecord(OrderLineData ipOLKey, 
                                          boolean izWithLock) 
         throws DBException
  {
    int vnWithLock = (izWithLock) ? DBConstants.WRITELOCK 
                                  : DBConstants.NOWRITELOCK;
    return mpOrderLine.getElement(ipOLKey, vnWithLock);
  }

  /**
   *  Convenience method. Gets a Order Line with no Lock.
   */
  public OrderLineData getOrderLineRecord(OrderLineData ordData)
         throws DBException
  {
    return getOrderLineRecord(ordData, false);
  }

 /**
  *  Method to retrieve a unique Order Line.  This method does some basic checks
  *  before accessing the database to make sure enough data is provided to try
  *  and find a unique record.  This is in an attempt to provide a more 
  *  meaningful error when bad data is provided in the search criteria 
  *  instead of a cryptic "no record found" type message.
  * 
  *  @param isOrderID the order id.  <u>This is a required parameter.</u>
  *  @param isItem The Order Line Item.  <u>If this is not provided, the Load ID.
  *               will be required.</u>
  *  @param isOrderLot The order lot. Can be passed as an empty string if you
  *               know there is a unique record matching an Order and Item or 
  *               Order and Load combination.
  *  @param isLoadID The order line load.  <u>This is a required parameter only
  *                 if isItem is not provided.</u>  If this parameter is filled
  *                 in, this method assumes this is a Load order to begin with.
  *  @param izWithLock <code>true</code> indicates read with lock. <b>Note:</b>
  *        <u>This method must be called from within a transaction if this flag
  *        is set to <code>true</code>.</u>
  *  @return null if no records are found.
  *  @throws DBException If a unique record can't be found based on specified
  *         key (but something was still found).  Also if DB access errors.
  */
  public OrderLineData getOrderLineRecord(String isOrderID, String isItem,
                                          String isOrderLot, String isLoadID, 
                                          boolean izWithLock) throws DBException
  {
    mpOLData.clear();
    if (isOrderID.trim().length() == 0)
      throw new DBException("Order ID must be provided for search!");
    
    mpOLData.setKey(OrderLineData.ORDERID_NAME, isOrderID);

    if (isItem.trim().length() != 0 && isLoadID.trim().length() != 0)
      throw new DBException("Invalid search criteria!  Both the Item and " +
                            "Load were filled in for the search key.  What " +
                            "type of Order is this?");
    
    if (isLoadID.trim().length() != 0)
    {           // Assume a Load Order since that's the only type that has the 
                // load filled in on the order line.
      mpOLData.setKey(OrderLineData.LOADID_NAME, isLoadID);
    }
    else
    {
      if (isItem.trim().length() == 0)
        throw new DBException("Item ID must be provided for search!");
      mpOLData.setKey(OrderLineData.ITEM_NAME, isItem);
      
      if (isOrderLot.trim().length() != 0)
        mpOLData.setKey(OrderLineData.ORDERLOT_NAME, isOrderLot);
    }
    
    return getOrderLineRecord(mpOLData, izWithLock);
  }

 /**
  *  Method to determine if an order is a Maintenance order.
  *  @param isOrderID the order id.
  *  @return <code>true</code> if this is a maintenance order.
  */
  public boolean isMaintenanceOrder(String isOrderID)
  {
    int vnOrderType = -1;
    try
    {
      vnOrderType = getOrderTypeValue(isOrderID);
    }
    catch(DBException exc)
    {
      logError("Error accessing database! :: " + exc.getMessage());
    }

    return(vnOrderType == DBConstants.REPLENISHMENT ||
           vnOrderType == DBConstants.CYCLECOUNT);
  }
  
  /**
   *  Retrieves Order Type of a given order.
   *  @param orderID The order id. to search by.
   *  @return translation of the order type.
   *  @throws DBException if there is a database access error.
   */
  public int getOrderTypeValue(String orderID) throws DBException
  {
    OrderHeaderData ordData = Factory.create(OrderHeaderData.class);
    ordData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    return mpOrderHeader.getOrderTypeValue(ordData);
  }

  /**
   * {@inheritDoc}
   * @param isOrderID {@inheritDoc}
   * @throws DBException {@inheritDoc}
   */
  public void setHostOrderStatus(String isOrderID) throws DBException
  {
    OrderHeaderData vpOHData = getOrderHeaderRecord(isOrderID);
    if (vpOHData == null)
    {
      throw new DBException("StandardOrderServer-->setHostOrderStatus():: " +
                            "Order Header record for order " + isOrderID +
                            " should exist but doesn't!");
    }
    
    switch(vpOHData.getNextStatus())
    {
      case DBConstants.UNKNOWN:        // Get it from the dest. station
        initializeStationServer();
        int vnOrdStat = mpStationServer.getOrderStatusForStation(vpOHData.getDestinationStation());
        setOrderStatusValue(isOrderID, vnOrdStat);
        break;
    
      case DBConstants.ALLOCATENOW:
        allocateOrder(isOrderID);
        break;
    
      default:
        setOrderStatusValue(isOrderID, vpOHData.getNextStatus());
    }
  }
  
  /**
   *  Retrieves Order Status of a given order.
   *  @param orderID The order id. to search by.
   *  @return translation of the order status.
   *  @throws DBException if there is a database access error.
   */
  public int getOrderStatusValue(String orderID) throws DBException
  {
    OrderHeaderData ordData = Factory.create(OrderHeaderData.class);
    ordData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    return mpOrderHeader.getOrderStatusValue(ordData);
  }

  /**
   *  Sets the order status to the newer Order Status.
   *  
   *  @param orderID <code>String</code> containing order id. of order record.
   *  @param newStatus <code>int</code> value for what the new order will be.
   */
  public void setOrderStatusValue(String orderID, int newStatus)
         throws DBException
  {
    int vnCurStatus = this.getOrderStatusValue(orderID);
    setOrderStatusValue(orderID, vnCurStatus, newStatus);
  }

  /**
   *  Sets the order status to the newer Order Status.  This method checks to make
   *  sure the current order status is what the user is expecting it to be before
   *  updating to the newer status.
   *  
   *  @param orderID <code>String</code> containing order id. of order record.
   *  @param origStatus <code>int</code> value for what current order status should
   *         be in the database.
   *  @param newStatus <code>int</code> value for what the new order will be.
   *  
   *  @exception DBException if current order status is not what the user specifies
   *  it to be.
   */
  public void setOrderStatusValue(String orderID, int origStatus, int newStatus)
         throws DBException
  {
    if (origStatus == newStatus)
      return;

    OrderHeaderData ordData = Factory.create(OrderHeaderData.class);
    ordData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    TransactionToken vpTok = startTransaction();

    try
    {
                                       // Lock the record.
      OrderHeaderData rtnData =
        getOrderHeaderRecord(ordData, DBConstants.WRITELOCK);
                                       // Make sure nothing has changed on us
                                       // in the database.
      int curStatus = rtnData.getOrderStatus();
      if (curStatus != origStatus)
      {
        String mesg =
          "Order Status in the\ndatabase is not the same\nas the original status given!";
        throw new DBException(mesg);
      }
                                       // Leave the order alone in certain
                                       // circumstances
      if ((newStatus == DBConstants.REALLOC)
        && (curStatus == DBConstants.HOLD
          || curStatus == DBConstants.KILLED
          || curStatus == DBConstants.DONE
          || curStatus == DBConstants.ALLOCATENOW))
      {
        // Don't do anything for now
        ;
      }
      else
      {
        logModifyTransaction(orderID, 
                             OrderHeaderData.ORDERSTATUS_NAME,
          Integer.valueOf(origStatus),
                               Integer.valueOf(newStatus)
                             );
        mpOrderHeader.setOrderStatusValue(orderID, newStatus);
        if (mzHasHostSystem)
        {
          initializeHostServer();
          mpHostServ.sendOrderStatus(orderID, newStatus);
        }
      }
      commitTransaction(vpTok);
    }
    catch (DBException exc)
    {
      logException(exc, "Setting Order Status");
      throw exc;
    }
    finally
    {
      endTransaction(vpTok);
    }

    ordData = null;

    return;
  }

 /**
   * Method gets a list of order id's based on order status(s), and/or order
   * type. The returned list is ordered by the oldest checked short order to the
   * newest.
   * 
   * @param ipOrderStatuses translation values denoting order status.
   * @param inOrderType translation value denoting order type. <i>This is an
   *            optional parameter that provides additional filtering. <b>This
   *            parameter is ignored if it is passed as 0</b></i>
   * @return String array of order id's. ordered by the oldest to newest
   *         scheduled date.
   * @throws DBException
   */
  public String[] getOrderListByStatuses(int[] ianOrderStatuses, int inOrderType)
         throws DBException
  {
    return(mpOrderHeader.getOrderListByStatuses(ianOrderStatuses, inOrderType));
  }

  /**
   *  Method to update short order check time (which indicates the last time a
   *  short order was considered for allocation)
   *  @param vsOrderID the order id.
   *  @param ipDateValue the date time stamp.
   *  TODO: make this a more generic method to update any date time field in the
   *  order header table.
   */
  public void updateShortOrderCheckTime(String vsOrderID, Date ipDateValue)
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
      ohdata.setKey(OrderHeaderData.ORDERID_NAME, vsOrderID);
      ohdata.setShortOrderCheckTime(ipDateValue);
      mpOrderHeader.modifyElement(ohdata);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "StandardOrderServer.updateShortOrderCheckTime");
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
  
  /**
   *  Method checks if the order header exists.
   *  @param ordData The order data to use in the search.
   *  @return  true if order exists, false otherwise.
   *  @throws DBException if there is a database access error.
   */
  public boolean OrderHeaderExists(OrderHeaderData ordData) throws DBException
  {
    return mpOrderHeader.exists(ordData);
  }

  /**
   *  Method checks if the order header exists given an order id.
   * @param ordData The order id. to use in the search.
   * @return  true if order exists, false otherwise.
   * @throws DBException if there is a database access error.
   */
  public boolean orderHeaderExists(String isOrderID) throws DBException
  {
    return mpOrderHeader.exists(isOrderID);
  }

  /**
   *  Method checks if the order line exists.
   *  @param ordData The order line data to use in the search.
   *  @return  true if order line exists, false otherwise.
   *  @throws DBException if there is a database access error.
   */
  public boolean OrderLineExists(OrderLineData ordData) throws DBException
  {
    return mpOrderLine.exists(ordData);
  }

  /**
   *  Convenient Order existence checker.
   *  @param sOrderID the order id.
   *  @param sItem  the item
   *  @param sOrderLot  the order lot number.
   *  @param sLineID  unique line id. for this P.O. line.
   *  @return true if line exists, false otherwise.
   */
  public boolean orderLineExists(String sOrderID, String sItem, String sOrderLot, String sLineID)
  {
    OrderLineData oldata = Factory.create(OrderLineData.class);
    oldata.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    oldata.setKey(OrderLineData.ITEM_NAME, sItem);
    oldata.setKey(OrderLineData.ORDERLOT_NAME, sOrderLot);
    oldata.setKey(OrderLineData.LINEID_NAME, sLineID);
    
    return mpOrderLine.exists(oldata);
  }
  
  /**
   *  Checks if there are any assigned moves for this order.
   *  @return true if there are ASSIGNED moves, false otherwise.
   */
  public boolean hasAssignedMoves(String orderID)
  {
    boolean rtn = false;

    MoveData mvdata = Factory.create(MoveData.class);

    mvdata.setKey(MoveData.ORDERID_NAME, orderID);
    mvdata.setKey(MoveData.MOVESTATUS_NAME, Integer.valueOf(DBConstants.ASSIGNED));
    try
    {
      if (Factory.create(Move.class).getCount(mvdata) != 0)
      {                                // There are assigned moves.
        rtn = true;
      }
    }
    catch (DBException exc)
    {
      logException(exc, "Checking ASSIGNED Moves for order " + orderID);
    }
    finally
    {
      mvdata.clear();
      mvdata = null;
    }

    return (rtn);
  }

  /**
   *  Method to mark the order as ready.  This enables the allocator to use
   *  this order as a candidate for allocation for the appropriate station when
   *  it gets a message from the scheduler for finding work for a station.
   *
   *  @param orderID <code>String</code> containing order ID of order to
   *         mark READY.
   */
  public void readyOrder(String orderID) throws DBException
  {
                                       // Only HOLD status orders can be activated.
    int ordStatus = this.getOrderStatusValue(orderID);

    if (ordStatus == DBConstants.READY)
    {
      return;
    }
    else if (ordStatus != DBConstants.HOLD && ordStatus != DBConstants.ORERROR)
    {
      throw new DBException("Order " + orderID + " must be on HOLD\nto activate!");
    }
    // Update the status
    setOrderStatusValue(orderID, DBConstants.READY);
  }

  /**
   * Send a message to the allocator to wake it up to process this order.
   * 
   * It is assumed that orders coming through this method are currently in READY
   * or HOLD state.
   * 
   * @param orderID <code>String</code> containing order ID of order to
   *          allocate.
   */
  public void allocateOrder(String orderID) throws DBException
  {
    if (getSystemGateway() == null) // Make sure the Gatway is really there.
    {
      logError("DefaultOrderServer-->allocateOrder: ERROR! System Gateway not defined.");
      return;
    }

    int ordStatus = getOrderStatusValue(orderID);
    if (ordStatus == DBConstants.HOLD || ordStatus == DBConstants.READY ||
        ordStatus == DBConstants.ORBUILDING)
    {
      setOrderStatusValue(orderID, DBConstants.ALLOCATENOW);
    }

                                       // Ask the allocator to look at this.
    logDebug("Publishing Order Event for Order: " + orderID);
    String vsAllocator = getAllocatorForOrder(orderID);
    getSystemGateway().publishOrderEvent(orderID, AllocationMessageDataFormat.NORMAL_ORDER,
                                         vsAllocator);
  }

  /**
   *  Allocate Oldest Held Order for Station
   *  If mustNotHaveOtherActive is true it will only allocate the next order if there
   *  are no other active orders for the station
   *  Method handles Orders that are on HOLD for a specific station
   *
   *  @param stationID <code>String</code> containing station ID for which 
   *         to allocate Order.
   *  @param mustNotHaveOtherActive <code>boolean</code> indicating if any
   *         active order is allowed to be destined for this station.
   */
  public void allocateOldestHeldOrderForStation(String stationID, boolean mustNotHaveOtherActive)
         throws DBException
  {
    if (mustNotHaveOtherActive)
    { // Are there any active orders? If so
      // just return since there can be no active
      // orders in this block.
      if (anyActiveOrdersForStation(stationID))
        return;
    }

    String sOrderID = getOldestOrderForStation(stationID, DBConstants.HOLD);
    if (sOrderID != null && sOrderID.trim().length() > 0)
    {
      TransactionToken vpTok = null;
      try
      {
        vpTok = startTransaction();
        allocateOrder(sOrderID);
        commitTransaction(vpTok);
      }
      finally
      {
        endTransaction(vpTok);
      }
    }
  }

  /**
   *  Method checks if there are any active orders slated for a station.
   *
   *  @param stationID <code>String</code> containing station being checked against.
   *  @return <code>boolean</code> of true if there are active orders.
   */
  public boolean anyActiveOrdersForStation(String stationID) throws DBException
  {
    OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);

    ohdata.setKey(OrderHeaderData.DESTINATIONSTATION_NAME, stationID);
    ohdata.setKey(OrderHeaderData.ORDERSTATUS_NAME, DBConstants.HOLD,
        KeyObject.NOT_EQUAL);
    return (mpOrderHeader.getCount(ohdata) > 0);
  }

  /**
   *  Get the Oldest Order by status for Station 
   *
   *  @param stationID <code>String</code> containing station ID for which to
   *         find oldest order.
   * 
   *  returns the oldest order by status as a String if found or and empty String 
   *  if not found
   */
  public String getOldestOrderForStation(String stationID, int inOrderStatus)
         throws DBException
  {
    return mpOrderHeader.getOldestOrderForStation(stationID, inOrderStatus);
  }

  /**
   *  Get the Oldest Order on HOLD.
   * 
   *  @returns the oldest orderID of held order.
   */
  public String getOldestHeldOrder() throws DBException
  {
    return mpOrderHeader.getOldestHeldOrder();
  }

  /*==========================================================================
            All types of Data gathering methods go in this section.
   ==========================================================================*/
  /**
   *  Gets list of all Order IDs in the system.
   */
  public String[] getOrderChoices(boolean insertAll) throws DBException
  {
    String[] ordlist = null;

    try
    {
      if (insertAll)
        ordlist = mpOrderHeader.getOrderChoices(SKDCConstants.ALL_STRING);
      else
        ordlist = mpOrderHeader.getOrderChoices("");
    }
    catch (DBException exc)
    {
      logException(exc, "getOrderChoices");
      throw exc;
    }

    return (ordlist);
  }

  /**
   *  Gets list of all Order IDs that fit the criteria.
   */
  public String[] getOrderChoices(OrderHeaderData ohdata, OrderLineData oldata, 
                                  boolean insertAll) throws DBException
  {
    String[] ordlist = null;
    TableJoin tableJoin = Factory.create(TableJoin.class);

    try
    {
      if (insertAll)
        ordlist = tableJoin.getOrderChoices(ohdata, oldata, SKDCConstants.ALL_STRING);
      else
        ordlist = tableJoin.getOrderChoices(ohdata, oldata, "");
    }
    catch (DBException exc)
    {
      logException(exc, "getOrderChoices");
      throw exc;
    }

    return (ordlist);
  }

  /**
   *  Check for Item existence.
   */
  public boolean itemExists(String item)
  {
    boolean rtnval;
    try
    {
      StandardInventoryServer invServ = Factory.create(StandardInventoryServer.class);
      rtnval = invServ.itemMasterExists(item);
    }
    catch (DBException exc)
    {
      rtnval = false;
    }

    return (rtnval);
  }

  /**
   *  Method counts Order Line records.
   *
   *  @param orderID <code>String</code> containing Order identifier to count by.
   *         "" if it should be excluded in the search.
   *  @param item <code>String</code> containing Order item to count by.
   *         "" if it should be excluded in the search.
   *  @param orderLot <code>String</code> containing Order lot to count by.
   *         "" if it should be excluded in the search.
   */
  public int getOrderLineCount(String orderID, String item, String orderLot)
  {
    int olCount = -1;
    OrderLineData olData = Factory.create(OrderLineData.class);

    if (orderID.trim().length() != 0)
    {
      olData.setKey(OrderLineData.ORDERID_NAME, orderID);
    }

    if (item.trim().length() != 0)
    {
      olData.setKey(OrderLineData.ITEM_NAME, item);
    }

    if (orderLot.trim().length() != 0)
    {
      olData.setKey(OrderLineData.ORDERLOT_NAME, orderLot);
    }

    if (olData.getKeyCount() != 0)
    {
      try
      {
        olCount = mpOrderLine.getCount(olData);
      }
      catch (DBException exc)
      {
        logException(exc, "DefaultOrderServer-->getOrderLineCount()");
        olCount = -1;
      }
    }

    return (olCount);
  }

  /**
   *  Sets the order line short for a load order line.  This method should only
   *  be called from within a transaction.
   *
   *  @param sOrderID <code>String</code> containing Order identifier.
   *  @param sLoadID <code>String</code> containing load being ordered out.
   */
  public void setOrderLineShort(String sOrderID, String sLoadID)
  {
    OrderLineData olData = Factory.create(OrderLineData.class);
    olData.setLineShy(DBConstants.YES);
    olData.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    olData.setKey(OrderLineData.LOADID_NAME, sLoadID);

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOrderLine.modifyElement(olData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside setOrderLineShort");
    }
    catch (NoSuchElementException ne)
    {
      logException(ne, "StandardOrderServer-->setOrderLineShort()");
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Sets the order line short for an item order line.
   *
   *  @param sOrderID <code>String</code> containing Order identifier.
   *  @param sItem <code>String</code> containing item being ordered out.
   *  @param sOrderLot <code>String</code> containing order lot being ordered out.
   *  @param iLineShyFlag value of flag to set order line to. Valid flags are
   *         DBConstants.YES, or DBConstants.NO.
   *  @param sLineID <code>String</code> containing line id. of the order line.
   */
  public void setOrderLineShort(String sOrderID, String sItem, String sOrderLot,
                                String sLineID, int iLineShyFlag)
  {
    OrderLineData olData = Factory.create(OrderLineData.class);
    olData.setLineShy(iLineShyFlag);
    olData.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    olData.setKey(OrderLineData.ITEM_NAME, sItem);
    olData.setKey(OrderLineData.ORDERLOT_NAME, sOrderLot);
    olData.setKey(OrderLineData.LINEID_NAME, sLineID);

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOrderLine.modifyElement(olData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside setOrderLineShort");
    }
    catch (NoSuchElementException ne)
    {
      logException(ne, "StandardOrderServer-->setOrderLineShort()");
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Sets the order line ship quantity for an item order line.
   *
   *  @param sOrderID <code>String</code> containing Order identifier.
   *  @param sItem <code>String</code> containing item being ordered out.
   *  @param sOrderLot <code>String</code> containing order lot being ordered out.
   *  @param iShipQty value of the ship quantity.
   */
  public void setOrderLineShipQuantity(String sOrderID, String sItem, 
                String sOrderLot, String sLineID, double iShipQty)
  {
    OrderLineData olData = Factory.create(OrderLineData.class);
    olData.setShipQuantity(iShipQty);
    olData.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    olData.setKey(OrderLineData.ITEM_NAME, sItem);
    olData.setKey(OrderLineData.ORDERLOT_NAME, sOrderLot);
    olData.setKey(OrderLineData.LINEID_NAME, sLineID);

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOrderLine.modifyElement(olData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside setOrderLineShipQuantity");
    }
    catch (NoSuchElementException ne)
    {
      logException(ne, "StandardOrderServer-->setOrderLineShipQuantity()");
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
   
  /**
   *  Sets the order header destination station.
   *
   *  @param sOrderID <code>String</code> containing Order identifier.
   *  @param sDestinationStation <code>String</code> the destination station.
   */
  public void setOrderDestinationStation(String sOrderID, String sDestinationStation)
          throws DBException
  {
    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    ohData.setDestinationStation(sDestinationStation);
    ohData.setKey(OrderHeaderData.ORDERID_NAME, sOrderID);
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOrderHeader.modifyElement(ohData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside setOrderDestinationStation");
    }
    catch (NoSuchElementException ne)
    {
      logException(ne, "StandardOrderServer-->setOrderDestinationStation()");
    }
    finally
    {
      endTransaction(vpTok);
    }      
  }
    
  /**
   *  Sets the order header carrier id.
   *
   *  @param sOrderID <code>String</code> containing Order identifier.
   *  @param sCarrierID <code>String</code> Carrier ID
   */
  public void setOrderCarrierID(String sOrderID, String sCarrierID)
  {
    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    ohData.setCarrierID(sCarrierID);
    ohData.setKey(OrderHeaderData.ORDERID_NAME, sOrderID);

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOrderHeader.modifyElement(ohData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside setOrderCarrierID");
    }
    catch (NoSuchElementException ne)
    {
      logException(ne, "StandardOrderServer-->setOrderCarrierID()");
    }
    finally
    {
      endTransaction(vpTok);
    }      
  }

  /**
   *  Sets the order message in the order header.
   *  
   *  @param sOrderMessage <code>String</code> containing the new order message.
   */
  public void setOrderMessage(String sOrderID, String sOrderMessage) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOrderHeader.setOrderMessage(sOrderID, sOrderMessage);
      commitTransaction(vpTok);
    }
    catch (DBException exc)
    {
      logException(exc, "StandardOrderServer-->setOrderStatusMessage()");
      throw exc;
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Method checks if there are order lines with picked quantities.
   *
   *  @param sOrderID <code>String</code> containing Order identifier.
   *  @param sItem <code>String</code> containing Order Item identifier.
   *  @param sOrderLot <code>String</code> containing Order Lot identifier.
   *  @param isLineID  <code>String</code> containing Order Line ID identifier.
   *
   *  @return <code>boolean</code> of <strong>false</strong> if picked quantity
   *          is 0. <strong>true</strong> otherwise.
   */
  public boolean hasPickedQuantities(String sOrderID, String sItem, String sOrderLot, 
      String isLineID) throws DBException
  {
    OrderLineData olData = Factory.create(OrderLineData.class);

    if (sOrderID.trim().length() == 0 || sItem.trim().length() == 0)
    {
      throw new DBException("Invalid arguments passed to\nDefaultOrderServer-->hasPickedQuantities");
    }
    olData.setKey(OrderLineData.ORDERID_NAME, sOrderID);
    olData.setKey(OrderLineData.ITEM_NAME, sItem);
    olData.setKey(OrderLineData.ORDERLOT_NAME, sOrderLot);
    olData.setKey(OrderLineData.LINEID_NAME, isLineID);
    olData.setKey(OrderLineData.PICKQUANTITY_NAME, Double.valueOf(0.0),
                  KeyObject.GREATER_THAN);

    return mpOrderLine.getCount(olData) > 0;
  }
  
  public OrderHeaderData buildLoadOrderForThisLoad(String isLoadToRetrieve, int inPriority) throws DBException
  {
    // fill in what is needed for a load order
    OrderHeaderData oh = Factory.create(OrderHeaderData.class);
    oh.clear();

    // make sure order doesn't already exist
    // if it does try another orderID

    StringBuffer orderID = new StringBuffer("55");
    orderID.append(isLoadToRetrieve);
    oh.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    while (OrderHeaderExists(oh))
    {
      orderID.setLength(2);
      orderID.append(rand.nextInt(Integer.MAX_VALUE));
      oh.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    }

    String loadOrderID = new String(orderID);

    oh.setOrderID(loadOrderID);
    oh.setOrderStatus(DBConstants.ALLOCATENOW);
    oh.setOrderType(DBConstants.ITEM_ORDER);
    oh.setPriority(inPriority);
    oh.setDescription("Order Load: " + isLoadToRetrieve);
   //oh.setDestinationStation(isDestination);

    OrderLineData ol = Factory.create(OrderLineData.class);
    ol.clear();
    ol.setOrderID(loadOrderID);
    ol.setLoadID(isLoadToRetrieve);
    ol.setOrderQuantity(1.0);
    ol.setAllocatedQuantity(0.0);
    ol.setPickQuantity(0.0);
    ol.setLineShy(DBConstants.NO);

    OrderLineData[] lineList = {ol};
    buildOrder(oh, lineList);
    
    return oh;
  }
  /**
   * Method to add a Load Order with the specified priority to retrieve the 
   * specified load to the specified destination
   * @param isLoadToRetrieve <code>String</code> to be retrieved
   * @param inPriority <code>int</code> of the order
   * @param isDestination <code>String</code> 
   * @return <code>OrderHeaderData</code> Object of created order header
   * @throws DBException
   */
  public OrderHeaderData buildLoadOrder(String isLoadToRetrieve, int inPriority, 
          String isDestination) throws DBException
  {
    // fill in what is needed for a load order
    OrderHeaderData oh = Factory.create(OrderHeaderData.class);
    oh.clear();

    // make sure order doesn't already exist
    // if it does try another orderID

    StringBuffer orderID = new StringBuffer("LD");
    orderID.append(isLoadToRetrieve);
    oh.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    System.out.println("HELLO:"+oh.getOrderID());
    while (OrderHeaderExists(oh))
    {
      orderID.setLength(2);
      orderID.append(rand.nextInt(Integer.MAX_VALUE));
      oh.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    }

    String loadOrderID = new String(orderID);

    oh.setOrderID(loadOrderID);
    oh.setOrderStatus(DBConstants.ALLOCATENOW);
    oh.setOrderType(DBConstants.FULLLOADOUT);
    oh.setPriority(inPriority);
    oh.setDescription("Order Load: " + isLoadToRetrieve);
    oh.setDestinationStation(isDestination);

    OrderLineData ol = Factory.create(OrderLineData.class);
    ol.clear();
    ol.setOrderID(loadOrderID);
    ol.setLoadID(isLoadToRetrieve);
    ol.setOrderQuantity(1.0);
    ol.setAllocatedQuantity(0.0);
    ol.setPickQuantity(0.0);
    ol.setLineShy(DBConstants.NO);

    OrderLineData[] lineList = {ol};
    buildOrder(oh, lineList);
    
    return oh;
  }
  
  /**
   *  Method to add orders into the system.
   *
   *  @param ipHeaderData The order header.
   *  @param ipLines Array of Order Lines.
   *  @return String with message indicating success of operation.  The
   *          intent is that this string could be used by a GUI.
   * @throws DBException  if there is any type of database error.
   */
  public String buildOrder(OrderHeaderData ipHeaderData, OrderLineData[] ipLines)
         throws DBException
  {
    if (ipHeaderData.getOrderStatus() == DBConstants.ORBUILDING)
      ipHeaderData.setOrderStatus(DBConstants.READY);
      
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      addOrderHeader(ipHeaderData);
                                       // Add the order lines.
      for(OrderLineData vpLine : ipLines)
      {
        int vnOrdType = ipHeaderData.getOrderType();
        if (vnOrdType == DBConstants.CONTAINER || vnOrdType == DBConstants.FULLLOADOUT)
          addOrderLine(vpLine, false);
        else
          addOrderLine(vpLine);
      }
      commitTransaction(vpTok);

      if (ipHeaderData.getOrderStatus() == DBConstants.ALLOCATENOW)
      {                                // Send message to allocator.
        allocateOrder(ipHeaderData.getOrderID());
      }
      else if (ipHeaderData.getOrderStatus() == DBConstants.READY)
      {
        initializeStationServer();
        String vsStation = ipHeaderData.getDestinationStation();
        String vsScheduler = mpStationServer.getStationsScheduler(vsStation);
        getSystemGateway().publishControlEvent(
            ControlEventDataFormat.getCommandTargetListMessage(
                ControlEventDataFormat.STAGED, new String[] {vsStation}), 
                ControlEventDataFormat.TEXT_MESSAGE, vsScheduler);
      }
    }
    catch(DBException vpExc)
    {
      logError("Error adding order " + ipHeaderData.getOrderID() + "::: " + vpExc.getMessage());
      throw new DBException("Error adding order " + ipHeaderData.getOrderID(), vpExc);
    }
    finally
    {
      endTransaction(vpTok);
    }

    return("Order is added successfully.");
  }

 /**
  * Puts an order on hold.  This method starts and ends its own transaction.
  * 
  * @param isOrderID the order id of order being put on hold.
  * @throws DBException if there is a DB error putting order on hold.
  */
  public void holdOrder(String isOrderID) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpOHData.clear();
      mpOHData.setKey(OrderHeaderData.ORDERID_NAME, isOrderID);
      OrderHeaderData vpOrdData = getOrderHeaderRecord(mpOHData, DBConstants.WRITELOCK);

      if (vpOrdData == null)
      {
        throw new DBException("Order " + isOrderID + " not found!");
      }
      else if (vpOrdData.getOrderStatus() == DBConstants.HOLD)
      {
        return;
      }
      else if (vpOrdData.getOrderStatus() == DBConstants.ORBUILDING)
      {
        throw new DBException("Order cannot be in 'Building' status.");
      }
      else
      {
        backOffInventory(isOrderID);
        setOrderStatusValue(isOrderID, DBConstants.HOLD);
      }
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /*==========================================================================
                           Customer related methods go here.
    ==========================================================================*/
  /**
   *  Gets a list of Customer records based on keys passed in cidata.
   *  @param cidata <code>CustomerData</code> object containing search
   *         information.
   *  @return <code>List</code> of <code>Map</code> containing customer
   *          information.
   */
  public List<Map> getCustomerData(CustomerData cidata) throws DBException
  {
    return Factory.create(Customer.class).getAllElements(cidata);
  }

  /**
   *  Method to get a Customer record.
   *
   *  @param customerID <code>String</code> containing customer ID. for unique
   *         lookup.
   *  @param lockFlag <code>boolean</code> of <code>true</code> indicates
   *         record should be read with a lock. <code>false</code> to read
   *         without lock.
   *
   *  @return <code>CustomerData</code> object with accessor methods.
   */
  public CustomerData getCustomerRecord(String customerID, boolean lockBoolean)
    throws DBException
  {
    int lockFlag = (lockBoolean) ? DBConstants.WRITELOCK : DBConstants.NOWRITELOCK;
    CustomerData cidata = Factory.create(CustomerData.class);
    cidata.setKey(CustomerData.CUSTOMER_NAME, customerID);
    return Factory.create(Customer.class).getElement(cidata, lockFlag);
  }

  /**
   * Gets list of all customer IDs in the system.
   * 
   * @param izInsertAll <code>boolean</code> flag indicating if the 'ALL'
   *            option needs to appear in the choice list.
   * 
   * @return <code>String[]</code> array of Customer ID's.
   */
  public String[] getCustomerChoices(boolean izInsertAll) throws DBException
  {
    Customer vpCustomer = Factory.create(Customer.class);

    return vpCustomer.getCustomerChoices(izInsertAll);
  }

  /**
   *  Method checks for customer record existence.
   *
   *  @param sCustomerID <code>String</code> containing customer ID.
   *
   *  @return <code>boolean</code> of <code>true</code> if customer record
   *          exists, <code>false</code> otherwise.
   */
  public boolean customerExists(String sCustomerID)
  {
    CustomerData cidata = Factory.create(CustomerData.class);
    cidata.setKey(CustomerData.CUSTOMER_NAME, sCustomerID);

    return Factory.create(Customer.class).exists(cidata);
  }

  /**
   *  Method adds a customer record.
   *
   *  @param custData <code>CustomerData</code> object containing data to add.
   *
   *  @throws DBException if there is a database error.
   */
  public void addCustomer(CustomerData custData) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      Factory.create(Customer.class).addElement(custData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException("StandardOrderServer.addCustomer " + getExceptionString(e), e);
      throw new DBException("Customer add error! " + e.getMessage());
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /**
   *  Method to modify a customer record.
   *
   *  @param custData <code>CustomerData</code> object containing key
   *         specifications for the modify.
   *  @throws DBException if there is a database error, or if the record to
   *          modify was not found using specified key.
   */
  public void modifyCustomer(CustomerData custData) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      Factory.create(Customer.class).modifyElement(custData);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Inside modifyCustomer");
      throw e;
    }
    catch (NoSuchElementException ne)
    {
      throw new DBException(ne);
    }
  }

  /**
   *  Deletes a Customer record.  This method makes sure that no orders exist
   *  on the system with this Customer ID before deleting the record.
   *
   *  @param customerID <code>String</code> containing unique customer ID to delete.
   *  @throws DBException if there is a database error.
   */
  public void deleteCustomer(String customerID) throws DBException
  {
    OrderHeaderData ohkey = Factory.create(OrderHeaderData.class);

    ohkey.setKey(OrderHeaderData.SHIPCUSTOMER_NAME, customerID);
    if (mpOrderHeader.exists(ohkey))
    {
      logOperation("Cannot Delete Ship Customer: " + customerID
          + " because Order(s) still exist for the customer.");
      return;
    }

    TransactionToken vpTok = null;
    try
    {
      CustomerData custData = Factory.create(CustomerData.class);
      custData.setKey(CustomerData.CUSTOMER_NAME, customerID);
      vpTok = startTransaction();
      Factory.create(Customer.class).deleteElement(custData);
      logOperation("Deleted Customer: " + customerID
          + " With 'Delete After Use' - All Orders Complete.");
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "Error Deleting Customser - orderserver.deleteCustomer");
      throw e;
    }
    catch (NoSuchElementException ne)
    {
      logException(ne, "Error Deleting Customser - orderserver.deleteCustomer");
      throw new DBException(ne);
    }
  }

  /**
   *  Method checks if Loads for this order are moving.  If any loads for
   *  this order are not in NOMOVE and not in RETRIEVEPENDING state, the
   *  load is considered moving.
   *
   *  @return <code>boolean</code> value of true
   */
  public boolean orderHasMovingLoads(String sOrderID)
  {
    boolean rtn = true;

    try
    {
      if (Factory.create(TableJoin.class).countMovingLoads(sOrderID) == 0)
      {
        rtn = false; // Everything is ok. Loads on this order
      } // are not moving.
    }
    catch (DBException e)
    {
      logDebug("orderHasMovingLoads-->" + e.getMessage());
    }

    return (rtn);
  }

  /**
   *  Method checks if this order hase any Moves.  
   *
   *  @return <code>boolean</code> value of true
   */
  public boolean orderHasMoves(String sOrderID)
  {
    boolean rtn = true;
    MoveData mvData = Factory.create(MoveData.class);

    mvData.setKey(MoveData.ORDERID_NAME, sOrderID);

    try
    {
      if (Factory.create(Move.class).getCount(mvData) == 0)
      {
        rtn = false; // Everything is ok. Order Has No Moves
      }
    }
    catch (DBException e)
    {
      logDebug("orderHasMoves-->" + e.getMessage());
    }

    return (rtn);
  }

  /**
   *  Method checks if this order has any Moves on loads other than the one passed in.  
   *
   *  @return <code>boolean</code> value of true
   */
  public boolean orderHasMovesOnOtherLoads(String sOrderID, String sLoadID)
  {
    MoveData mvData = Factory.create(MoveData.class);
    Move move = Factory.create(Move.class);

    mvData.setKey(MoveData.ORDERID_NAME, sOrderID);

    try
    {
      if (move.getCount(mvData) == 0)
      {
        return (false); // Everything is ok. Order Has No Moves
      }
      else
      {
        List<Map> mvlist = move.getAllElements(mvData);
        for (int idx = 0; idx < mvlist.size(); idx++)
        {
          mvData.dataToSKDCData(mvlist.get(idx));
          if (!mvData.getLoadID().equals(sLoadID))
          {
            return (true);
          }
        }
        return (false);
      }
    }
    catch (DBException e)
    {
      logDebug("orderHasMoves-->" + e.getMessage());
    }

    return (true);
  }

  /**
   *  Method checks if this order hase any order Lines that still have picks
   *  that have not been completed.
   *
   *  @return <code>boolean</code> value of <code>true</code> if there are
   *  picks remaining, <code>false</code> otherwise.
   */
  public boolean orderHasRemainingPicks(String sOrderID)
  {
    boolean rtn = true;

    try
    {
      if (getOrderTypeValue(sOrderID) != DBConstants.CYCLECOUNT)
      {
        rtn = mpOrderLine.hasRemainingPicks(sOrderID);
      }
    }
    catch (DBException e)
    {
      logDebug("orderHasRemainingPicks-->" + e.getMessage());
    }

    return(rtn);
  }

  /**
   *  Method checks if this order hase any order Lines that still have picks that
   *  have not been completed  or any moves outstanding...if either of these is true
   *  the order is not complete  
   *
   *  @return <code>boolean</code> value of true if order is complete.
   */
  public boolean orderIsComplete(String sOrderID)
  {
    return(!orderHasRemainingPicks(sOrderID) && !orderHasMoves(sOrderID));
  }

 /**
  * Method to check if any load lines have an order attached.  This method's
  * result is meaningful only for systems that track the pick-to load.
  * @param sOrderID the order id.
  * @return <code>true</code> if load lines have been picked for an order.
  */
  public boolean loadLinesHaveOrders(String sOrderID)
  {
    boolean vzRtn = false;

    StandardInventoryServer invServ = Factory.create(StandardInventoryServer.class);

    int idCount = invServ.getLoadLineCount("", "", "", sOrderID, "");
    if (idCount == -1)
    {
      logError("OrderServer-->loadLinesHaveOrders::Error Counting Load Line Items...");
      vzRtn = true;
    }
    else if (idCount > 0)
    {
      vzRtn = true;
    }

    return(vzRtn);
  }

  /**
   *  {@inheritDoc}
   *  @param isOrderID the Order id.
   *  @return {@inheritDoc}
   */
  public boolean isOrderFullyAllocated(String isOrderID)
  {
    boolean vzFullyAllocated = false;
    try
    {
      vzFullyAllocated = mpOrderLine.isOrderFullyAllocated(isOrderID);
    }
    catch(DBException e)
    {
      logException(e, "StandardOrderServer-->isOrderFullyAllocated");
    }
    
    return(vzFullyAllocated);
  }
  
  /**
   * Does this order have short lines?
   * 
   * @param the order id.
   * @param the order's destination station
   * @return boolean
   */
  @UnusedMethod
  public boolean orderHasShortLines(String isOrderID, String isDestStation)
  {
    boolean vzOrderIsShort = false;
    
    TableJoin vpTJ = Factory.create(TableJoin.class);
    OrderLineData vpOLData = Factory.create(OrderLineData.class);
    try
    {
      initializeStationServer();
      String vsWarhse = mpStationServer.getStationWarehouse(isDestStation);
      int vnAisleGroup = mpStationServer.getStationAisleGroup(isDestStation);
      List<Map> vpList = getOrderLineData(isOrderID);
      for(Map vpMap : vpList)
      {
        vpOLData.dataToSKDCData(vpMap);
        double vdAvailQty = vpTJ.getTotalAvailableQuantity(vpOLData.getItem(),
            vpOLData.getOrderLot(), vsWarhse, vnAisleGroup);
        if (vpOLData.getAllocatedQuantity() < vpOLData.getOrderQuantity() &&
            vdAvailQty < vpOLData.getOrderQuantity())
        {
          vzOrderIsShort = true;
          break;
        }
      }
    }
    catch (DBException e)
    {
      logException(e, "StandardOrderServer-->orderHasShortLines");
    }

    return(vzOrderIsShort);
  }

  /**
   *  Method checks if any order line has allocated quantity.
   *
   *  @param orderID <code>String</code> containing order id of order.
   *
   *  @return <code>boolean</code> of <code>true</code> if any  order line has
   *          allocated quantities.
   */
  public boolean orderHasNothingAllocated(String sOrderID)
  {
    boolean rtn = false;

    try
    {
      OrderLineData oldata = Factory.create(OrderLineData.class);
      oldata.setKey(OrderLineData.ORDERID_NAME, sOrderID);
      oldata.setKey(OrderLineData.ALLOCATEDQUANTITY_NAME, Double.valueOf(0.0),
                    KeyObject.GREATER_THAN);
      rtn = (mpOrderLine.getCount(oldata) == 0);
    }
    catch (DBException e)
    {
      logException(e, "StandardOrderServer-->orderHasNothingAllocated");
    }

    return (rtn);
  }

  /**
   *  Backs-Off Order Line for an Item de-allocation.
   *
   *  @param orderID <code>String</code> containing order id of order.
   *  @param loadID <code>String</code> containing load id. This may be passed as
   *                a null or empty string if the order is an item order.
   *  @param item <code>String</code> containing item id.  This may be passed as
   *              a null or empty string if the order is a load order.
   *  @param lot <code>String</code> containing order lot.  This may be passed as
   *              a null or empty string if the order is a load order.
   *  @param sLineID  <code>String</code> containing the order line's line id.
   *  @param deAllocateQty <code>double</code> value indicating allocation amount to
   *                       back-off from order line.
   *
   *  @exception throws <code>DBException</code> when there is a database error.
   */
  public void backoffOrderLineQty(String orderID, String loadID, String item,
      String lot, String lineID, double deAllocateQty) throws DBException
  {
    boolean noLineInfoPassed = true;
    OrderLineData oldata = Factory.create(OrderLineData.class);

    oldata.setKey(OrderLineData.ORDERID_NAME, orderID);
    if (item != null && item.trim().length() != 0)
    {
      oldata.setKey(OrderLineData.ITEM_NAME, item);
      oldata.setKey(OrderLineData.ORDERLOT_NAME, lot);
      oldata.setKey(OrderLineData.LINEID_NAME, lineID);
      noLineInfoPassed = false;
    }
    else if (loadID != null && loadID.trim().length() != 0)
    {
      oldata.setKey(OrderLineData.LOADID_NAME, loadID);
      noLineInfoPassed = false;
    }

    if (noLineInfoPassed)
    {
      // There is one case where it is ok to not pass item/lot or Load in...that is an order
      // where empties were requested because we do not put any line info in the order line
      // so just add a warning for us to be aware of what is happening
      logDebug(
        " WARNING: No Order Line Item/Lot/Load information passed for order: "
          + orderID
          + " to OrderServer-->backOffOrderLine() - OK IF REQUEST FOR EMPTY LOADS");
    }

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      OrderLineData currOrderLine = mpOrderLine.getElement(oldata, DBConstants.NOWRITELOCK);
      if (currOrderLine == null)
      {
        logError(
          " Error Deallocating for move..NO ORDER LINES FOUND for Order: "
            + orderID
            + " in OrderServer-->backOffOrderLine()");
        return;
      }
      double olAllocQty = currOrderLine.getAllocatedQuantity();
      olAllocQty -= deAllocateQty;

      if (olAllocQty < 0)
      {
        olAllocQty = 0;
      }
      oldata.setAllocatedQuantity(olAllocQty);
      mpOrderLine.modifyElement(oldata);
      commitTransaction(vpTok);
    }
    catch (DBException dbe)
    {
      logError(
        dbe.getMessage()
          + " Error Deallocating for move. OrderServer-->backOffOrderLine()");
    }
    finally
    {
      endTransaction(vpTok);
    }

    return;
  }

  /**
   *  Backs-Off Order Line for an Item de-allocation.
   *
   *  @param orderID <code>String</code> containing order id of order.
   *  @param loadID <code>String</code> containing load id. This may be passed as
   *                a null or empty string if the order is an item order.
   *  @param item <code>String</code> containing item id.  This may be passed as
   *              a null or empty string if the order is a load order.
   *  @param lot <code>String</code> containing order lot.  This may be passed as
   *              a null or empty string if the order is a load order.
   *  @param lineID <code>String</code> containing order line ID. This may be passed as
   *              a null or empty string if the order is a load order.
   *  @param deAllocateQty <code>double</code> value indicating allocation amount to
   *                       back-off from order line.
   *
   *  @exception throws <code>DBException</code> when there is a database error.
   */
  public void backoffOrderLineQtyAfterPick(String orderID, String loadID, String item,
                                           String lot, String lineID, double deAllocateQty)
         throws DBException
  {
    OrderLineData oldata = Factory.create(OrderLineData.class);
    oldata.setKey(OrderLineData.ORDERID_NAME, orderID);
    if (item != null && item.trim().length() != 0)
    {
      oldata.setKey(OrderLineData.ITEM_NAME, item);
      oldata.setKey(OrderLineData.ORDERLOT_NAME, lot);
      oldata.setKey(OrderLineData.LINEID_NAME, lineID);
    }
    else if (loadID != null && loadID.trim().length() != 0)
    {
      oldata.setKey(OrderLineData.LOADID_NAME, loadID);
    }
    else
    {
      logError(
        " Insufficient Order Line Item/Lot/Load information passed for order: "
          + orderID
          + " to OrderServer-->backoffOrderLineQtyAfterPick()");
      return;
    }

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      OrderLineData currOrderLine = mpOrderLine.getElement(oldata, DBConstants.NOWRITELOCK);
      if (currOrderLine == null)
      {
        logError(
          " Error Deallocating for move..NO ORDER LINES FOUND for Order: "
            + orderID
            + " in OrderServer-->backoffOrderLineQtyAfterPick()");
        return;
      }
      double olAllocQty = currOrderLine.getAllocatedQuantity();
      double olPickQty = currOrderLine.getPickQuantity();
      olAllocQty -= deAllocateQty;
      olPickQty -= deAllocateQty;

      if (olAllocQty < 0)
      {
        olAllocQty = 0;
      }
      oldata.setAllocatedQuantity(olAllocQty);
      
      if (olPickQty < 0)
      {
        olPickQty = 0;
      }
      oldata.setPickQuantity(olPickQty);
      mpOrderLine.modifyElement(oldata);
      
      // now try to change the order status to REALLOCATE if needed
      int iOrderStatus = getOrderStatusValue(orderID);
      if (iOrderStatus != DBConstants.HOLD        &&
          iOrderStatus != DBConstants.REALLOC     &&
          iOrderStatus != DBConstants.ALLOCATING)
      {    
        setOrderStatusValue(orderID, getOrderStatusValue(orderID), DBConstants.REALLOC);
      }
      commitTransaction(vpTok);
    }
    catch (DBException dbe)
    {
      logError(dbe.getMessage() +
               " Error Deallocating for move. OrderServer-->backoffOrderLineQtyAfterPick()");
    }
    finally
    {
      endTransaction(vpTok);
    }

    return;
  }

  /**
   *  Worker method to carry out the deletion of Order Header and associated
   *  lines.
   */
  public void executeDeletion(OrderHeaderData ordData) throws DBException
  {
    String orderID = ordData.getOrderID();
    OrderLineData oldata = Factory.create(OrderLineData.class);
    try
    {
      List<Map> ollist = getOrderLineData(orderID);
      for (int i = 0; i < ollist.size(); i++)
      {
        oldata.dataToSKDCData(ollist.get(i));
        tnData.clear();
        tnData.setTranCategory(DBConstants.ORDER_TRAN);
        tnData.setTranType(DBConstants.DELETE_ORDER_LINE);
        tnData.setOrderID(orderID);
        tnData.setLineID(oldata.getLineID());
        tnData.setItem(oldata.getItem());
        tnData.setLot(oldata.getOrderLot());
        tnData.setLoadID(oldata.getLoadID());
        logTransaction(tnData);
      }
      oldata.clear();
      oldata.setKey(OrderLineData.ORDERID_NAME, orderID);
      mpOrderLine.deleteElement(oldata);
    }
    catch (NoSuchElementException e)
    {
      // it's ok to swallow this exception - log the fact that this order has no order lines
      logDebug(orderID + " being deleted and it had no order lines");
    }

    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    ohData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    mpOrderHeader.deleteElement(ohData);

                                       // Log Delete history (including where
                                       // Order was going)
    tnData.clear();
    tnData.setTranCategory(DBConstants.ORDER_TRAN);
    tnData.setTranType(DBConstants.DELETE_ORDER);
    tnData.setOrderID(orderID);
    tnData.setOrderType(ordData.getOrderType());
    tnData.setToStation(ordData.getDestinationStation());
    logTransaction(tnData);

    // Delete related records such as order notes, order line notes, and
    // customers.
    deleteAuxiliaryRecords(ordData);

    oldata.clear();
    oldata = null;
    ohData.clear();
    ohData = null;
  }

  /**
   *  Method deletes Order related records.
   *
   *  @param sOrderID <code>String</code> containing order to delete.
   *  @throws DBException when there are database errors.
   */
  protected void deleteAuxiliaryRecords(OrderHeaderData ohdata) throws DBException
  {
    Customer customer = Factory.create(Customer.class);
    CustomerData cidata = Factory.create(CustomerData.class);

    cidata.setKey(CustomerData.CUSTOMER_NAME, ohdata.getShipCustomer());
    if (customer.getCount(cidata) > 0)
    {
      cidata = customer.getElement(cidata, DBConstants.NOWRITELOCK);
      if (cidata != null && cidata.getDeleteOnUse() == DBConstants.YES)
      {
        deleteCustomer(ohdata.getShipCustomer());
      }
    }
  }
  
  /**
   *  Method to decrement all necessary data related to an allocated order.<BR><BR>
   *  <p> This method first finds UNASSIGNED moves for each
   *  order line and decrements the order allocated quantity and item detail
   *  allocated quantity by the move pick quantity and then deletes the move(s).
   *
   *  @param orderID <code>String</code> containing the order to back off.
   */
  protected void backOffInventory(String orderID) throws DBException
  {
    MoveData mvdata = Factory.create(MoveData.class);
    Move move = Factory.create(Move.class);
    StandardLoadServer loadServ = Factory.create(StandardLoadServer.class);
    
    mvdata.setKey(MoveData.ORDERID_NAME, orderID);
    List<Map> mvList = move.getAllElements(mvdata);
    for (int mvIdx = 0; mvIdx < mvList.size(); mvIdx++)
    {
      mvdata.dataToSKDCData(mvList.get(mvIdx));
      int moveID = mvdata.getMoveID();
                                       // Leave everything alone if it's been
                                       // ASSIGNED.
      if (mvdata.getMoveStatus() == DBConstants.ASSIGNED)
      {
        continue;
      }
      
                                       // Lock the Load record.
      LoadData lddata = loadServ.getLoad(mvdata.getLoadID(), true);
      if (lddata == null)
      {
        throw new DBException("Load Record for Load " + mvdata.getLoadID() +
                              " not found!");
      }
      else if (lddata.getLoadMoveStatus() != DBConstants.NOMOVE &&
               lddata.getLoadMoveStatus() != DBConstants.RETRIEVEPENDING)
      {
        continue;
      }
      else
      {
        /*
         * If there are no other moves for this load, make this load stationary
         */
        MoveData vpMVData = Factory.create(MoveData.class);
        
        vpMVData.setKey(MoveData.LOADID_NAME, mvdata.getLoadID());
        vpMVData.setKey(MoveData.MOVEID_NAME, mvdata.getMoveID(), KeyObject.NOT_EQUAL);
        List<Map> vpListOtherMovesForLoad = move.getAllElements(vpMVData);

        if (vpListOtherMovesForLoad.size() <= 0)
        {
          lddata.clear();
          lddata.setKey(LoadData.LOADID_NAME, mvdata.getLoadID());

          setLoadDataMoveStatus(lddata, DBConstants.NOMOVE);
          lddata.setNextWarehouse("");
          lddata.setFinalWarehouse("");
          lddata.setNextAddress("");
          lddata.setFinalAddress("");
          Factory.create(Load.class).modifyElement(lddata);
        }
      }

      try
      {
        if (mvdata.getMoveType() == DBConstants.LOADMOVE ||
            mvdata.getMoveType() == DBConstants.EMPTYMOVE)
        {
          fixLoadInventory(mvdata);
        }
        else
        {
          fixItemInventory(mvdata);
        }
        
                                       // Delete the move
        mvdata.clear();
        mvdata.setKey(MoveData.MOVEID_NAME, Integer.valueOf(moveID));
        move.deleteElement(mvdata);
      }
      catch (DBException e)
      {
        logException(e, "In DefaultOrderServer-->backOffInventory()");
      }
    } // End for-loop
  }

  /**
   *  Backs-Off Item Detail and Order Line for an Item allocation.  This method
   *  should be called from Order hold or Order deletion methods.
   */
  protected void fixItemInventory(MoveData mvData) throws DBException
  {
    // A load cycle count move has no item details
    if (mvData.getItem().trim().length() == 0 &&
        mvData.getPickLot().trim().length() == 0)
    {
      return;
    }
    
    LoadLineItem loadLine = Factory.create(LoadLineItem.class);
    LoadLineItemData iddata = Factory.create(LoadLineItemData.class);
                                       // Note if we have a move the pick from
                                       // Load Line item can be found uniquely
                                       // using the three key values.
    iddata.setKey(LoadLineItemData.LOADID_NAME, mvData.getLoadID());
    iddata.setKey(LoadLineItemData.ITEM_NAME, mvData.getItem());
    iddata.setKey(LoadLineItemData.LOT_NAME, mvData.getPickLot());
    LoadLineItemData currItemDet = loadLine.getElement(iddata, DBConstants.NOWRITELOCK);

    if (currItemDet == null)
      throw new DBException("Item detail record not found for load: " +
                            mvData.getLoadID() +
                            "\n item: " + mvData.getItem() + " Lot : " +
                            mvData.getPickLot() +
                            "\n::::: DefaultOrderServer-->fixItemInventory() :::::");

    double idAllocQty = currItemDet.getAllocatedQuantity();
    double mvPickQty = mvData.getPickQuantity();
    idAllocQty = (idAllocQty >= mvPickQty) ? idAllocQty - mvPickQty : 0;

    iddata.setAllocatedQuantity(idAllocQty);
    loadLine.modifyElement(iddata);

    OrderLineData oldata = Factory.create(OrderLineData.class);
    oldata.setKey(OrderLineData.ORDERID_NAME, mvData.getOrderID());
    oldata.setKey(OrderLineData.ITEM_NAME, mvData.getItem());
    oldata.setKey(OrderLineData.ORDERLOT_NAME, mvData.getOrderLot());
    oldata.setKey(OrderLineData.LINEID_NAME, mvData.getLineID());
    OrderLineData currentOL = getOrderLineRecord(oldata);

    if (currentOL != null)
    {
      double olAllocQty = currentOL.getAllocatedQuantity();
      if (olAllocQty > 0)
      {
        olAllocQty -= mvPickQty;
        if (olAllocQty < 0) olAllocQty = 0;
        oldata.setAllocatedQuantity(olAllocQty);

        mpOrderLine.modifyElement(oldata);
      }
    }
  }

  /**
   *  Backs-off Item detail, and Order Line for a Load Allocation.
   */
  protected void fixLoadInventory(MoveData mvData) throws DBException
  {
    LoadLineItem loadLine = Factory.create(LoadLineItem.class);
    LoadLineItemData iddata = Factory.create(LoadLineItemData.class);
    
    iddata.setKey(LoadLineItemData.LOADID_NAME, mvData.getLoadID());
    if (mvData.getItem().trim().length() != 0)
    {
      iddata.setKey(LoadLineItemData.ITEM_NAME, mvData.getItem());
      iddata.setKey(LoadLineItemData.LOT_NAME, mvData.getPickLot());
    }
    
    List<Map> vpLoadLineList = loadLine.getAllElements(iddata);
    
                                       // Get Order Type we are dealing with.
    int orderType = getOrderTypeValue(mvData.getOrderID());

    if (vpLoadLineList.size() == 0)
    {                                  // It must be a completely empty container.
      if (orderType == DBConstants.CONTAINER || orderType == DBConstants.FULLLOADOUT)
      {
        fixEmptyLoadInventory(orderType, mvData);
        return;
      }
      throw new DBException("Item detail record not found for load: " +
                            mvData.getLoadID() +
                            "\n::::: DefaultOrderServer-->fixLoadInventory() :::::");
    }

    double idAllocQty = 0, olAllocQty = 0;
    OrderLineData currentOL = null;
    OrderLineData oldata = Factory.create(OrderLineData.class);
    
    if (orderType == DBConstants.CONTAINER || orderType == DBConstants.FULLLOADOUT)
    {
                                       // Order lines have order and allocated qty.
                                       // of 1. Item details are NOT allocated.
      
      oldata.setKey(OrderLineData.ORDERID_NAME, mvData.getOrderID());
      oldata.setKey(OrderLineData.LOADID_NAME, mvData.getLoadID());
      olAllocQty = idAllocQty = 0;
    }
    else if (orderType == DBConstants.REPLENISHMENT || orderType == DBConstants.ITEMORDER)
    {
      if (vpLoadLineList.size() > 1)
      {
        throw new DBException("Too many items on load " + mvData.getLoadID()
            + " for Replenishment/Item order to deallocate load move");
      }
      
      LoadLineItemData currItemDet = Factory.create(LoadLineItemData.class);
      currItemDet.dataToSKDCData(vpLoadLineList.get(0));
      
      if (orderType == DBConstants.REPLENISHMENT)
      {
        oldata.setKey(OrderLineData.ORDERID_NAME, mvData.getOrderID());
        oldata.setKey(OrderLineData.ITEM_NAME, currItemDet.getItem());
      }
      else
      {
        oldata.setKey(OrderLineData.ORDERID_NAME, mvData.getOrderID());
        oldata.setKey(OrderLineData.LOADID_NAME, mvData.getLoadID());
      }
      currentOL = getOrderLineRecord(oldata);
      idAllocQty = currItemDet.getAllocatedQuantity();
      olAllocQty = currentOL.getAllocatedQuantity();
      olAllocQty -= idAllocQty;
      
      if (olAllocQty < 0) olAllocQty = 0;
      idAllocQty = 0;
      
      iddata.clear();
      iddata.setKey(LoadLineItemData.LOADID_NAME, currItemDet.getLoadID());
      iddata.setKey(LoadLineItemData.ITEM_NAME, currItemDet.getItem());
      iddata.setKey(LoadLineItemData.LOT_NAME, currItemDet.getLot());
      iddata.setAllocatedQuantity(idAllocQty);
      loadLine.modifyElement(iddata);
    }
    else
    {
      throw new DBException("Unknown Order Type " + orderType + 
                            " in ::::: DefaultOrderServer-->fixLoadInventory() :::::");
    }

    oldata.setAllocatedQuantity(olAllocQty);
    mpOrderLine.modifyElement(oldata);
  }

  /**
   *  Fixes Order for a completely empty load.  <b>This method should be called
   *  only when we know there is no inventory in the load.</b>
   *
   *  @param  mvData contains a seleted move record.
   */
  protected void fixEmptyLoadInventory(int inOrderType, MoveData mvData) throws DBException
  {
    OrderLineData oldata = Factory.create(OrderLineData.class);
    oldata.setKey(OrderLineData.ORDERID_NAME, mvData.getOrderID());
    OrderLineData currentOL = getOrderLineRecord(oldata);

    if (currentOL == null)
    {
      throw new DBException("Order Line record not found for order: " +
                            mvData.getOrderID() +
                            "\n::::: StandardtOrderServer-->fixEmptyLoadInventory() :::::");
    }
    double olAllocQty = currentOL.getAllocatedQuantity() - mvData.getPickQuantity();
    if (Double.compare(olAllocQty, 0.0) < 0) olAllocQty = 0.0;
    if (inOrderType == DBConstants.FULLLOADOUT)
      oldata.setAllocatedQuantity(0);
    else
      oldata.setAllocatedQuantity(olAllocQty);
    mpOrderLine.modifyElement(oldata);
  }

  protected void setLoadDataMoveStatus(LoadData loadData, int status)
  {
    String s = "" + status;
    String field = LoadData.LOADMOVESTATUS_NAME;
    try
    {
      s = DBTrans.getStringValue(field, status);
    }
    catch (Exception e)
    {
      logError("StandardOrderServer - DBTrans CANNOT find " + field +
               " - setLoadDataMoveStatus()");
    }
    logDebug("LoadId \"" + loadData.getLoadID() + "\" - New Status: " + s +
             " - StandardOrderServer");
    loadData.setLoadMoveStatus(status);
  }

  /**
   *  Method to generate Order info. for testing.
   */
  public void autoOrderSKTestItem(String destination, int orderStatus)
  {
    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    OrderLineData olData = Factory.create(OrderLineData.class);
    Random rand = new Random();
    ohData.setOrderID("AUTO" + Math.abs(rand.nextInt()));
    ohData.setDescription("SK Test Order");
    ohData.setDestinationStation(destination);
    ohData.setPriority(5);
    ohData.setOrderStatus(orderStatus);
    ohData.setOrderType(DBConstants.ITEMORDER);
    olData.setOrderID(ohData.getOrderID());
    olData.setItem("SK_TEST_ITEM");
    olData.setOrderQuantity(1);
    olData.setHeight(1);
    olData.setLineShy(DBConstants.YES);
    OrderLineData[] olArray = { olData };
    try
    {
      buildOrder(ohData, olArray);
    }
    catch (DBException exc)
    {
      this.logDebug(
        "DefaultOrderServer.autoOrderSKTestItem - Error creating auto order");
    }
  }

  /**
   *  createItemOrder - used to to the building of an item order just from 
   *                   the order basic components
   *  @param destination - where the order needs to be sent
   *  @param orderStatus - valid order status (HOLD, READY, ALLOCATENOW)
   *  @param itemID  - the item ordered
   *  @param lotID - the lot Ordered
   *  @param itemQty - the qty ordered
   *  @throws DBException 
   */
  public void createItemOrder(String destination, int orderStatus, String itemID,
                              String lotID, double itemQty) throws DBException
  {
    Random rand = new Random();
    //System.out.println("inside createitemorder");
    StringBuffer orderID = new StringBuffer("Item");
    orderID.append(rand.nextInt(Integer.MAX_VALUE));
    // Call our method that uses the order ID    
    createItemOrder(new String(orderID), destination, "Item Order", orderStatus,
                    itemID, lotID, itemQty);
  }

  /**
   *  createItemOrder - used to to the building of an item order just from 
   *                    the order basic components
   *  @param sOrderID - the order number
   *  @param destination - where the order needs to be sent
   *  @param sDescription - Order Description
   *  @param orderStatus - valid order status (HOLD, READY, ALLOCATENOW)
   *  @param itemID  - the item ordered
   *  @param lotID - the lot Ordered
   *  @param itemQty - the qty ordered
   *  @throws DBException 
   */
  public void createItemOrder(String sOrderID, String destination,
                              String sDescription, int orderStatus, String itemID,
                              String lotID, double itemQty)
         throws DBException
  {
    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    ohData.clear();
    ohData.setOrderID(sOrderID);
    OrderLineData olData = Factory.create(OrderLineData.class);
    olData.clear();
    Random rand = new Random();
    StringBuffer orderID = new StringBuffer(sOrderID);

    ohData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
    int i = 0;

    int vnOrderIDLength = DBInfo.getFieldLength(OrderHeaderData.ORDERID_NAME);
    while (OrderHeaderExists(ohData))
    {
      orderID.setLength(sOrderID.length());
      if (sOrderID.length() < vnOrderIDLength)
      {
        orderID.setLength(sOrderID.length());
      }
      else
      {
        orderID.setLength(4);
      }
      orderID.append(rand.nextInt(Integer.MAX_VALUE / 100));
      if (orderID.length() > vnOrderIDLength)
      {
        orderID.setLength(vnOrderIDLength);
      }
      ohData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
      i++;
      if (i > 20)
      {
        logError(
          "OrderServer.createItemOrder-Error finding unique Order # for Item: "
            + itemID
            + " for station: "
            + destination);
        throw new DBException(
          "OrderServer.createItemOrder-Error finding unique Order # for Item: "
            + itemID
            + " for station: "
            + destination);
      }
    }

    //System.out.println("new order id: " + orderID);

    ohData.setOrderID(new String(orderID));
    ohData.setDescription(sDescription);
    ohData.setDestinationStation(destination);
    ohData.setPriority(5);
    if (orderStatus != DBConstants.HOLD
      && orderStatus != DBConstants.ALLOCATENOW
      && orderStatus != DBConstants.READY)
    {
      logDebug("Invalid Order Status: " + orderStatus + " for Item: " + itemID
          + " for station: " + destination);
      orderStatus = DBConstants.READY;
    }
    ohData.setOrderStatus(orderStatus);
    ohData.setOrderType(DBConstants.ITEMORDER);
    olData.setOrderID(ohData.getOrderID());
    olData.setItem(itemID);
    olData.setOrderLot(lotID);
    olData.setOrderQuantity(itemQty);
    olData.setLineShy(DBConstants.NO);
    olData.setLineID("1");
    OrderLineData[] olArray = { olData };

    buildOrder(ohData, olArray);
  }

  /**
   *  Method to generate Order info. for testing.
   */
  public void orderEmptyLoad(String destination, int orderStatus,
                             String containerType, double loadQty, int loadHeight,
                             String stationOrderPrefix )
  {
    try
    {
      OrderHeaderData oh = Factory.create(OrderHeaderData.class);
      oh.setOrderID(createRandomOrderID(stationOrderPrefix,"MT"));
      oh.setOrderStatus(DBConstants.ALLOCATENOW);
      oh.setOrderType(DBConstants.CONTAINER);
      oh.setPriority(7);
      oh.setDestinationStation(destination);
      oh.setDescription("Empty Container Request");

      OrderLineData vpOrderLineData = Factory.create(OrderLineData.class);
      vpOrderLineData.clear();
      vpOrderLineData.setOrderID(oh.getOrderID());
      vpOrderLineData.setContainerType(containerType);
      vpOrderLineData.setOrderQuantity(loadQty);
      vpOrderLineData.setHeight(loadHeight);
      vpOrderLineData.setAllocatedQuantity(0.0);
      vpOrderLineData.setPickQuantity(0.0);
      vpOrderLineData.setLineShy(DBConstants.NO);
      vpOrderLineData.setItem("");
      vpOrderLineData.setOrderLot("");
      vpOrderLineData.setDescription("Empty Container Request");

      OrderLineData[] lineList = { vpOrderLineData };

      String mesg = buildOrder(oh, lineList);
      logDebug("Results for Order: " + oh.getOrderID() + ", " + mesg);

    }
    catch (DBException e2)
    {
      logDebug("Unable to Order Empty Load");
    }
  }

  /**
   *  Create an automatic empty container order for a station. This is used when
   *  a station should always have an empty container present so that product 
   *  can be received into it.
   *  @param inTotalEmptyStationOrders the sum of enroute and staged loads for  
   *                      stationthat auto-order is being created for.
   *  @param isDestStation the auto-order station.
   *  @param inOrderStatus the default status that the order should be created with.
   *  @param isContainerType Container type assigned to station.
   *  @param inAllowableLoadHeight the allowable load heights for station.
   *  @param idOrderAmount the default order amount. This decimal value represents
   *                      the number of empty containers that will be ordered to this
   *                      station.  So for example 1.25 represents an empty 
   *                      container plus 1/4 empty container.
   *  @param isStnOrderPrefix Special prefix for any station order.
   */
  public void autoOrderEmpty(int inTotalEmptyStationOrders, String isDestStation,
                             int inOrderStatus, String isContainerType, 
                             int inAllowableLoadHeight, double idOrderAmount, 
                             String isStnOrderPrefix)
  {
    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    try
    {
      ohData.setKey(OrderHeaderData.DESTINATIONSTATION_NAME, isDestStation);
      ohData.setKey(OrderHeaderData.ORDERTYPE_NAME, DBConstants.CONTAINER);
      if (mpOrderHeader.getCount(ohData) >= inTotalEmptyStationOrders)
      { // The total count of enroute and staged orders is equal to stations needs
        return;
      }
    }
    catch (DBException e)
    {
      System.out.println(
        "Error " + e + " getting order count going to station");
      return; // Set so order will not be created
    }
    
    orderEmptyLoad(isDestStation, inOrderStatus, isContainerType, 
                   idOrderAmount, inAllowableLoadHeight, isStnOrderPrefix);
  }
  
  /**
   * autoOrderItem - Order an item to a station based on the stations 
   *                 enroute qty's etc.
   */
  public void autoOrderItem(int totalItemStationOrders, String destinationStation,
                            int orderStatus, String item, double itemQuantity,
                            int loadHeight, String stationOrderPrefix )
  {
    OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
    try
    { // Get count of orders going to destination
      // station.
      ohData.setKey(OrderHeaderData.DESTINATIONSTATION_NAME, destinationStation);
      if (mpOrderHeader.getCount(ohData) >= totalItemStationOrders)
      { // The total count of enroute and staged orders is equal to stations needs
        return;
      }
    }
    catch (DBException e)
    {
      System.out.println(
        "Error " + e + " getting order count going to station");
      return; // Set so order will not be created
    }
    ohData.clear();
    OrderLineData olData = Factory.create(OrderLineData.class);
    olData.clear();
    ohData.setOrderID(createRandomOrderID(stationOrderPrefix, "AUTO"));
    ohData.setDescription("AutoOrder at Station " + destinationStation);
    ohData.setDestinationStation(destinationStation);
    ohData.setPriority(5);
    ohData.setOrderStatus(orderStatus);
    ohData.setOrderType(DBConstants.ITEMORDER);
    olData.setOrderID(ohData.getOrderID());
    olData.setItem(item);
    olData.setOrderQuantity(itemQuantity);
    olData.setHeight(1);
    olData.setLineShy(DBConstants.YES);
    OrderLineData[] olArray = { olData };

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      buildOrder(ohData, olArray);
      commitTransaction(vpTok);
    }
    catch (DBException exc)
    {
      System.out.println(
        "OrderServer.autoOrderSKTestItem - Error "
          + exc
          + " creating auto order for station");
      this.logDebug(
        "DefaultOrderServer.autoOrderSKTestItem - "
          + "Error creating auto order for station "
          + itemQuantity);
      return;
    }
    finally
    {
      endTransaction(vpTok);
    }

  }

  /**
   * Get a random unique order id
   * 
   */
  public String createRandomOrderID()
  {
    Random rand = new Random();
    String orderID = "";
    boolean duplicateID = true;
    do
    {
      orderID = "OR" + rand.nextInt(Integer.MAX_VALUE);
      if (!mpOrderHeader.exists(orderID))
      {
        duplicateID = false;
      }
    }
    while (duplicateID);

    return (orderID);
  }
  
  /**
   *  Get a random unique order id using a passed in order
   *  prefix
   * 
   */
  public String createRandomOrderID(String prefix, String defaultPrefix)
  {
    Random rand = new Random();
    String orderID = "";
    boolean duplicateID = true;
   
    /* if passed in prefix is blank, use passed in defaultPrefix */
    if(prefix.trim().length() == 0)
    {
      if(defaultPrefix.trim().length() == 0)
      {
        prefix = "OR";
      }
      else
      {
        prefix = defaultPrefix;
      }
    }
    
    do
    {
      orderID = prefix + rand.nextInt(Integer.MAX_VALUE);
      if (!mpOrderHeader.exists(orderID))
      {
        duplicateID = false;
      }
    }
    while (duplicateID);

    return (orderID);
  }
  
  /**
   *  Checks if there are any load line items for this order.
   *  @return true if there are load line items, false otherwise.
   */
  public boolean hasLoadLineItems(String orderID)
  {
    boolean rtn = false;

    LoadLineItemData iddata = Factory.create(LoadLineItemData.class);

    iddata.setKey(LoadLineItemData.ORDERID_NAME, orderID);
    try
    {
      if (Factory.create(LoadLineItem.class).getCount(iddata) > 0)
      {                                // There are load line items.
        rtn = true;
      }
    }
    catch (DBException exc)
    {
      logException(exc, "Checking load line items for order " + orderID);
    }
    finally
    {
      iddata.clear();
      iddata = null;
    }

    return (rtn);
  }

  /**
   *  @inheritDoc
   */
  public List<LoadLineItemData> getOrderableItemDetails(String isRoute, 
          boolean izRouteSet) throws DBException
  {
    List<Map> vpMapList = null;
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      LoadLineItem vpLLI = Factory.create(LoadLineItem.class);
      vpMapList = vpLLI.getOrderableItemDetails(isRoute, izRouteSet);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "StandardOrderServer.getOrderableItemDetails");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }
    
    // convert to data objects
    List<LoadLineItemData> vpDataList = new ArrayList<LoadLineItemData>();
    for (Map vpMap : vpMapList)
    {
      LoadLineItemData vpLLD = Factory.create(LoadLineItemData.class);
      vpLLD.dataToSKDCData(vpMap);
      vpDataList.add(vpLLD);
    }
    return vpDataList;
  }

  /**
   *  @inheritDoc
   */
  public List<LoadLineItemData> getSingleOrderableItemDetails(String isRoute, 
          boolean izRouteSet) throws DBException
  {
    List<Map> vpMapList = null;
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      LoadLineItem vpLLI = Factory.create(LoadLineItem.class);
      vpMapList = vpLLI.getOrderableItemDetails(isRoute, izRouteSet);
      commitTransaction(vpTok);
    }
    catch (DBException e)
    {
      logException(e, "StandardOrderServer.getSingleOrderableItemDetails");
      throw e;
    }
    finally
    {
      endTransaction(vpTok);
    }
    
    // convert to data objects - and in the process filter out any item
    // details on the same load as each other
    List<LoadLineItemData> vpDataList = new ArrayList<LoadLineItemData>();
    List<String> vpDupLoads = new ArrayList<String>();
    List<String> vpLoadIds = new ArrayList<String>();
    for (Map<String,String> vpMap : vpMapList)
    {
      String vsLoad = vpMap.get(LoadLineItemData.LOADID_NAME);
      if (vpLoadIds.contains(vsLoad))
        vpDupLoads.add(vsLoad);
      else
        vpLoadIds.add(vsLoad);
    }
    for (Map vpMap : vpMapList)
    {
      LoadLineItemData vpLLD = Factory.create(LoadLineItemData.class);
      vpLLD.dataToSKDCData(vpMap);
      if (!vpDupLoads.contains(vpLLD.getLoadID()))
      {
        vpDataList.add(vpLLD);
      }
    }
    return vpDataList;
  }

  /**
   *  {@inheritDoc} Find the order's station's device's allocator!
   *  @param isOrder the order id.
   *  @throws DBException if there is a database access error.
   */
  public String getAllocatorForOrder(String isOrder) throws DBException
  {
    initializeStationServer();
    String vsDestStn = (String)mpOrderHeader.getSingleColumnValue(isOrder, 
                                       OrderHeaderData.DESTINATIONSTATION_NAME);
    if (vsDestStn == null || vsDestStn.isEmpty())
    {                               // In case we are using Order line routing
                                    // check the order lines for a station
      List<Map> vpList = getOrderLineData(isOrder);
      if (vpList.size() > 0)
      {
        OrderLineData vpOLD = Factory.create(OrderLineData.class);
        vpOLD.dataToSKDCData(vpList.get(0));
        vsDestStn = vpOLD.getRouteID();
      }
                                     // No station was found in order lines either
      if (vsDestStn == null || vsDestStn.isEmpty())
        throw new DBException("Allocator could not be determined for Order "
                              + isOrder + ".  Undetermined destination  "
                              + "station for order...");

    }
    else
    {
      if (!mpStationServer.exists(vsDestStn))
        throw new DBException("Allocator could not be determined for Order "
                              + isOrder + ".  Undetermined destination  "
                              + "station for order...");
    }
    
    return mpStationServer.getAllocatorForStation(vsDestStn);
  }
  
  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/

  protected void initializeStationServer()
  {
    if (mpStationServer == null)
    {
      mpStationServer = Factory.create(StandardStationServer.class,
          getClass().getSimpleName());
    }
  }
  
  protected void initializeShortOrderProcessor()
  {
    if (mpShortOrderProcessor == null)
      mpShortOrderProcessor = Factory.create(ShortOrderProcessor.class);
  }
  
  protected void initializeHostServer()
  {
    if (mpHostServ == null)
    {
      mpHostServ = Factory.create(StandardHostServer.class);
    }
  }  

  protected void initializeInventoryServer()
  {
    if (mpInvServ == null)
    {
      mpInvServ = Factory.create(StandardInventoryServer.class);
    }
  }

  /*========================================================================*/

  /**
   *  Shuts down this controller by canceling any timers and shutting down the
   *  Equipment.
   */
  @Override
  public void cleanUp()
  {
    if (mpStationServer != null)
    {
      mpStationServer.cleanUp();
      mpStationServer = null;
    }
    super.cleanUp();
  }

  /*=========================================================================
        *** Methods for Logging Transaction History record go here ***
   =========================================================================*/

  /**
   *  Log a Modify Transaction History record for an OH Data column whose
   *  value is changed.
   *  
   *  @param orderID <code>String</code> containing order id. of order record.
   *  @param colName <code>String</code> column name whose value is changed.
   *  @param oldObj <code>Object</code> contains current value of the column.
   *  @param newObj <code>Object</code> contains new value of the column.
   */
  protected void logModifyTransaction(String isOrderID, String isColName, 
          Object ipOldValue, Object ipNewValue) throws DBException
  {
    if (!ipOldValue.equals(ipNewValue))
    {
      String actDesc = "";
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.MODIFY_ORDER);
      tnData.setOrderID(isOrderID);
      if (tnData.setField(isColName, ipNewValue) == -1)
      {
        actDesc = mpOHData.getActionDesc(isColName, ipOldValue, ipNewValue);
        tnData.setActionDescription(actDesc);
      }
      logTransaction(tnData);
    }
  }

  /**
   *  Log a Modify Transaction History record
   *  
   *  @param ipNewData <code>String</code> containing data of new OrderHeader record.
   *  
   *  @throws <code>DBException</code> if a database add error.
   */
  private void logModifyTransaction(OrderHeaderData ipNewData) throws DBException
  {
    OrderHeaderData vpCurOH = getOrderHeaderRecord(ipNewData);
    logModifyTransaction(vpCurOH, ipNewData);
  }

  /**
   *  Log a Modify Transaction History record
   *  
   *  @param ipOldData <code>String</code> containing data of old OrderHeader record.
   *  @param ipNewData <code>String</code> containing data of new OrderHeader record.
   *  
   *  @throws <code>DBException</code> if a database add error.
   */
  private void logModifyTransaction(OrderHeaderData ipOldData, OrderHeaderData ipNewData) throws DBException
  {
    // We only log the transaction if there is any change
    if (ipNewData.getColumnCount() > 0)
    {
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.MODIFY_ORDER);
      // Make sure we log the orderid correctly
      if( ipNewData.getOrderID().isEmpty())
    	tnData.setOrderID(ipOldData.getOrderID());
      else
        tnData.setOrderID(ipNewData.getOrderID());
      
      if (logDataChanged(ipOldData, ipNewData) == true)
      {
        logTransaction(tnData);
      }
    }
  }

  /**
   *  Log a Modify Transaction History record
   *  
   *  @param ipNewData <code>String</code> containing data of new OrderHeader record.
   *  
   *  @throws <code>DBException</code> if a database add error.
   */
  private void logModifyTransaction(OrderLineData ipNewData) throws DBException
  {
    OrderLineData vpCurOL = getOrderLineRecord(ipNewData);
    logModifyTransaction(vpCurOL, ipNewData);
  }

  /**
   *  Log a Modify Transaction History record
   *  
   *  @param ipOldData <code>String</code> containing data of old OrderLine record.
   *  @param ipNewData <code>String</code> containing data of new OrderLine record.
   *  
   *  @throws <code>DBException</code> if a database add error.
   */
  protected void logModifyTransaction(OrderLineData ipOldData, OrderLineData ipNewData) throws DBException
  {
    // We only log the transaction if there is any change
    if (ipNewData.getColumnCount() > 0)
    {
      tnData.clear();
      tnData.setTranCategory(DBConstants.ORDER_TRAN);
      tnData.setTranType(DBConstants.MODIFY_ORDER_LINE);
      tnData.setOrderID(ipNewData.getOrderID());
      tnData.setLineID(ipNewData.getLineID());
      if (logDataChanged(ipOldData, ipNewData) == true)
      {
        logTransaction(tnData);
      }
    }
  }
}