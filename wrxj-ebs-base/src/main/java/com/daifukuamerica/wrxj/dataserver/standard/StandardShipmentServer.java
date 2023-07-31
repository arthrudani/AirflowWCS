package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCode;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.host.messages.ShipComplete;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.util.SkdRtException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description: <BR>
 * Server to handle Shipping Specific operations.  This class has been stripped
 * down to only support shipping a load.
 * 
 * @author R.G.
 * @version 1.0 <BR>
 *          Created: 20-Dec-04 <BR>
 *          Copyright (c) 2004 <BR>
 *          Company: Daifuku America Corporation
 */
public class StandardShipmentServer extends StandardServer
{
  private StandardHostServer mpHostServ = null;

  /**
   * Constructor
   */
  public StandardShipmentServer()
  {
    this(null);
  }

  /**
   * Constructor
   * 
   * @param keyName
   */
  public StandardShipmentServer(String keyName)
  {
    super(keyName);
    logDebug("Create StandardShipmentServer");
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardShipmentServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  logDebug("Create StandardShipmentServer");
  }

  /**
   * Get the highest level load for a shipping load
   * 
   * @param isLoadID - the load we want to ship
   * @return
   */
  private String getHighestLevelLoadToShip(String isLoadID)
  {
    StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);

    LoadData vpShipLoadData = vpLoadServer.getLoad(isLoadID);
    if (vpShipLoadData == null)
    {
      /*
       * This load already shipped.
       */
      return null;
    }
    LoadData vpParentLoadData = vpShipLoadData;

    /*
     * Get the highest level SHIPWAIT load
     */
    while ((!vpParentLoadData.getLoadID().equals(
        vpParentLoadData.getParentLoadID()))
        && (vpParentLoadData.getLoadMoveStatus() == DBConstants.SHIPWAIT))
    {
      vpShipLoadData = vpParentLoadData;
      vpParentLoadData = vpLoadServer.getParentLoad(vpShipLoadData.getLoadID());
    }

    /*
     * If the parent is SHIPWAIT, ship it
     */
    if (vpParentLoadData.getLoadMoveStatus() == DBConstants.SHIPWAIT)
    {
      vpShipLoadData = vpParentLoadData;
    }

    /*
     * Return the load ID
     */
    return vpShipLoadData.getLoadID();
  }

  /**
   * Method to ship a load.
   * 
   * @param isLoadID containing load to ship.
   * @param izShipSuperLoad true to ship this load's highest-level super load
   * @throws <code>DBException</code>
   */
  public void shipLoad(String isLoadID, boolean izShipSuperLoad)
      throws DBException
  {
    String vsShipLoad = isLoadID;
    if (izShipSuperLoad)
    {
      vsShipLoad = getHighestLevelLoadToShip(isLoadID);
      if (vsShipLoad == null)
      {
        return;
      }
    }

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      shipLoad(vsShipLoad);
      commitTransaction(tt);
    }
    catch (DBException exp)
    {
      exp.printStackTrace();
      logException("StandardShipmentServer.shipLoad", exp);
      throw new SkdRtException("StandardShipmentServer.shipLoad " + isLoadID
          + getExceptionString(exp), exp);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Method to ship a load.
   * 
   * @param String loadID containing load to ship.
   * @throws <code>DBException</code>
   */
  private void shipLoad(String iLoadID) throws DBException
  {
    StandardOrderServer orderServ = Factory.create(StandardOrderServer.class);
    StandardLoadServer loadServ = Factory.create(StandardLoadServer.class);
    StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);

    String orderNumber = "";
    String saveOrderNumber = "";
    OrderHeaderData ohdata = null;
    List<String> orderList = new ArrayList<String>();

    /*
     * If this is a super load, recursively ship all the loads on this load and
     * then ship this load.
     */
    if (loadServ.getChildrenCount(iLoadID) > 0)
    {
      // Get a list of the child loads.
      List<Map> childList = loadServ.getChildrenList(iLoadID);

      for (int i = 0; i < childList.size(); i++)
      {
        shipLoad(
            DBHelper.getStringField(childList.get(i), LoadData.LOADID_NAME),
            false);
      }
    }

    /*
     * Ship the load.
     */
    LoadData lddata = loadServ.getLoad(iLoadID);
    if (lddata == null)
    {
      throw new DBException("Unknown load: " + iLoadID);
    }

    /*
     * For each item detail record on the load, update ship qty in order line
     * item record.
     */
    List<Map> detailList = invtServ.getLoadLineItemDataListByLoadID(iLoadID);
    LoadLineItemData iddata = Factory.create(LoadLineItemData.class);
    for (int i = 0; i < detailList.size(); i++)
    {
      iddata.clear();
      iddata.dataToSKDCData(detailList.get(i));

      // Update the order line record.

      OrderLineData oldata = Factory.create(OrderLineData.class);
      oldata = orderServ.getOrderLineData(iddata);
      if (oldata != null)
      {
        orderNumber = oldata.getOrderID();
        double shipQty = oldata.getShipQuantity() + iddata.getCurrentQuantity();
        orderServ.setOrderLineShipQuantity(oldata.getOrderID(),
            oldata.getItem(), oldata.getOrderLot(), oldata.getLineID(), shipQty);
      }
      
      // Get the current order header and add to our save list.
      if (!orderNumber.equals(saveOrderNumber))
      {
        ohdata = orderServ.getOrderHeaderRecord(orderNumber);
        orderList.add(orderNumber);
      }
      saveOrderNumber = orderNumber;

      // Put this check in as a request to not send ship completes for container
      // or cycle count orders. But this is just a patch, why are load orders
      // and cycle count orders getting set to CLOSING or the loads themselves
      // being set to SHIP_WAIT. This makes no sense...
      // -Ryan
      if (mzHasHostSystem && ohdata.getOrderType() != DBConstants.CONTAINER
          && ohdata.getOrderType() != DBConstants.CYCLECOUNT
          && ohdata.getOrderType() != DBConstants.REPLENISHMENT
          && ohdata.getOrderType() != DBConstants.FULLLOADOUT)
      {
        // Send the ship complete message
        ShipComplete scdata = MessageOutFactory.getInstance(MessageOutNames.SHIP_COMPLETE);
        scdata.setTransactionTime(new Date());
        scdata.setCarrierID(ohdata.getCarrierID());
        scdata.setDestinationStation(ohdata.getDestinationStation());
        scdata.setItem(iddata.getItem());
        scdata.setLot(iddata.getLot());
        scdata.setOrderID(oldata.getOrderID());
        scdata.setOrderLineID(oldata.getLineID());
        scdata.setOrderPriority(ohdata.getPriority());
        scdata.setReleaseToCode(ohdata.getReleaseToCode());
        scdata.setShipLoadID(lddata.getLoadID());
        scdata.setShippingAddress(ohdata.getDestAddress());
        scdata.setShippingWarehouse(ohdata.getDestWarehouse());
        scdata.setShipQuantity(iddata.getCurrentQuantity());
        scdata.setTerminalID("SHIP");
        scdata.setUserID("SHIP");
        scdata.setTrackingNumber("");

        initializeHostServer();
        mpHostServ.sendShipComplete(ohdata.getOrderType(), scdata);
      }
    }

    // Log the transaction.
    tnData.clear();
    tnData.setTranCategory(DBConstants.LOAD_TRAN);
    tnData.setTranType(DBConstants.COMPLETION);
    if (ohdata != null)
    {
      tnData.setCarrierID(ohdata.getCarrierID());
      tnData.setOrderID(ohdata.getOrderID());
      tnData.setOrderType(ohdata.getOrderType());
    }
    tnData.setLoadID(iLoadID);
    tnData.setLocation(lddata.getWarehouse(), lddata.getAddress());
    tnData.setShipDate(new Date());
    tnData.setTransDateTime(new Date());
    logTransaction(tnData);

    // Delete the shipping load
    invtServ.deleteShippingLoad(iLoadID, ReasonCode.getShippingReasonCode());

    /*
     * For each order we processed, check if it's done. If so set it to DONE.
     */
    for (int i = 0; i < orderList.size(); i++)
    {
      String orderID = orderList.get(i);
      /*
       * If there are no more loads and moves for this order, mark it done. Lock
       * the order header record so we know that no one else is doing anything
       * with the order (allocating for example) before we do our check for item
       * details or moves
       */
      OrderHeaderData tmpoh = orderServ.getOrderHeaderRecord(orderID,
          DBConstants.WRITELOCK);
      if (tmpoh != null && (tmpoh.getOrderStatus() != DBConstants.REALLOC)
          && !orderServ.hasLoadLineItems(orderID)
          && !orderServ.orderHasMoves(orderID))
      {
        cleanOrder(orderID);
      }
    }
  }

  /**
   * Mark an order as DONE if it is so
   * 
   * @param isOrderID
   * @return
   */
  protected boolean cleanOrder(String isOrderID)
  {
    StandardOrderServer orderServ = Factory.create(StandardOrderServer.class);
    StandardLocationServer locationServ = Factory.create(StandardLocationServer.class);

    // Get the order header data.
    OrderHeaderData ohdata = null;
    try
    {
      ohdata = orderServ.getOrderHeaderRecord(isOrderID);
    }
    catch (DBException exp)
    {
      logException(exp, "Failed to read Order Header for order: '" + isOrderID
          + "'");
      return false;
    }

    // Make sure the order is complete
    if (!orderServ.orderIsComplete(isOrderID))
    {
      try
      {
        orderServ.setOrderMessage(ohdata.getOrderID(),
            "Order prematurely set to DONE");
        orderServ.setOrderStatusValue(ohdata.getOrderID(), DBConstants.ERROR);
      }
      catch (DBException exp)
      {
        logException(exp, "Failed to update order status for order: '"
            + isOrderID + "'");
      }
      return false;
    }

    // -----------------------------------------------------------//
    // Delete the order (sending the Order Complete) //
    // -----------------------------------------------------------//
    try
    {
      orderServ.deleteOrder(isOrderID);
    }
    catch (DBException exp)
    {
      logException(exp, "Failed to delete order: '" + isOrderID + "'");
      return false;
    }

    // ----------------------------------------------------------//
    // Free up the consolidation / shipping location. //
    // ----------------------------------------------------------//
    try
    {
      if (ohdata.getDestAddress().trim().length() > 0)
      {
        locationServ.setLocationEmptyFlag(ohdata.getDestWarehouse(),
            ohdata.getDestAddress(), LoadData.DEFAULT_POSITION_VALUE,
            DBConstants.EMPTY);
      }
    }
    catch (DBException exp)
    {
      logException(exp, "Failed to free consolidation location for order: '"
          + isOrderID + "'");
    }

    return true;
  }

  /**
   * Initialize the host server
   */
  private void initializeHostServer()
  {
    if (mpHostServ == null)
    {
      mpHostServ = Factory.create(StandardHostServer.class);
    }
  }
}
