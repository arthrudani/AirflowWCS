package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;
import com.daifukuoc.wrxj.custom.ebs.jdbc.BCSMessage;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;


public class EBSOrderServer extends StandardOrderServer {

	private EBSLoadServer loadServer = Factory.create(EBSLoadServer.class);
    protected EBSHostServer mpEBSHostServ = null;
    protected StandardMoveServer mpMoveServ = null;
    private EBSTableJoin mpTableJoin = new  EBSTableJoin();
    public EBSOrderServer() {
        super();
    }

    public EBSOrderServer(String keyName) {
        super(keyName);
    }

    public EBSOrderServer(String keyName, DBObject dbo) {
        super(keyName, dbo);
    }

    
    @Override
    public void allocateOrder(String orderID) throws DBException
    {
    	//KR: implementing -> allocation order here
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
    @Override
    public String getAllocatorForOrder(String isOrder) throws DBException
    {
    	return EBSDBConstants.DEFAULT_ALLOCATOR;
    }
    @Override
    public void setOrderStatusValue(String orderID, int newStatus)
            throws DBException
     {
       int vnCurStatus = this.getOrderStatusValue(orderID);
       setOrderStatusValue(orderID, vnCurStatus, newStatus);
     }
    
    @Override
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
           getOrderHeaderRecord(ordData, DBConstants.NOWRITELOCK);
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
         /*  if (mzHasHostSystem)
           {
             initializeHostServer();
             mpHostServ.sendOrderStatus(orderID, newStatus);
           }*/
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

    public String getOrderHeaderIdByLoadId(String sLoadId) throws DBException
    {
    	return mpTableJoin.getOrderHeaderIdByLoadId(sLoadId);
    }

    public short buildOrderForFlight(String lot)
    {
    	short numberOfBagsRetrieved = 0;
    	try {

			List<LoadLineItemData> loadLineItems=  mpTableJoin.getAllRetrievableLoadLineItemsForThisFlight(lot);

	        if (loadLineItems != null && !loadLineItems.isEmpty()) {
	            // As this retrieval is per flight and flight scheduled datetime, final sort location of all loads
				// should be same. So, getting final sort location id from the first entry in the list would be ok.
				processRetrievalOrder(loadLineItems);
	            numberOfBagsRetrieved = (short) (loadLineItems.size());
	        }
    	
    	} catch (DBException e) {
			
			logError( "Failed to buildOrderForFlight:"+ lot + " - Error:"+e.getMessage());
			 System.out.println("Failed to buildOrderForFlight:"+ lot + " - Error:"+e.getMessage());
		}
    	
    	return numberOfBagsRetrieved;
    }
    
    public short buildOrderForTray(String trayID)
    {
    	short numberOfBagsRetrieved = 0;
    	try {

			List<LoadLineItemData> loadLineItems=  mpTableJoin.getAllRetrievableLoadLineItemsForThisTray(trayID);

	        if (loadLineItems != null && !loadLineItems.isEmpty()) {
	            // As this retrieval is per flight and flight scheduled datetime, final sort location of all loads
				// should be same. So, getting final sort location id from the first entry in the list would be ok.
				processRetrievalOrder(loadLineItems);
	            numberOfBagsRetrieved = (short) (loadLineItems.size());
	        }
    	
    	} catch (DBException e) {
			
			logError( "Failed to buildOrderForTray:"+ trayID + " - Error:"+e.getMessage());
			 System.out.println("Failed to buildOrderForTray:"+ trayID + " - Error:"+e.getMessage());
		}
    	
    	return numberOfBagsRetrieved;
    }
    private int processRetrievalOrder( List<LoadLineItemData> loadLineItems) throws DBException
    {
    	int OrderId = generatedOrderId();
    	 OrderHeaderData orderHeaderData = prepareOrderHeaderDataToPersist(String.valueOf( OrderId ),  new java.util.Date());
   	 
    	 ArrayList<OrderLineData>  orderLineDataList = new ArrayList<OrderLineData>();
    	 short savedRecord = 0;
    	 for (LoadLineItemData loadLineItemData : loadLineItems) {
      
           LoadData loadData = loadServer.getLoad(loadLineItemData.getLoadID());
           OrderLineData orderLineData = prepareOrderLineDataToPersist(String.valueOf( OrderId ), loadLineItemData, loadData);
           orderLineDataList.add(orderLineData);
           
           updateLoadMoveStatus(loadData);
          
           loadLineItemData.setOrderID(String.valueOf( OrderId ));
           loadServer.updatLoadLineItem(loadLineItemData);

           savedRecord++;
    	 }
    	 if( orderLineDataList.size() > 0 )
    	 {
    		 buildOrder(orderHeaderData, orderLineDataList.toArray( new OrderLineData[orderLineDataList.size()] ));
    	 }

       return savedRecord;
    }
    
    private OrderHeaderData prepareOrderHeaderDataToPersist(String generatedOrderId, Date flightScheduledDateTime) {
        OrderHeaderData orderHeaderData = Factory.create(OrderHeaderData.class);
        orderHeaderData.setOrderID(generatedOrderId);
        orderHeaderData.setScheduledDate(flightScheduledDateTime);
        orderHeaderData.setOrderType(DBConstants.ITEMORDER);
        //orderHeaderData.setDestinationStation(outputStation);
        orderHeaderData.setOrderStatus(DBConstants.READY);
        //orderHeaderData.setDestAddress(finalSortLocation);

        return orderHeaderData;
    }
    private void updateLoadMoveStatus(LoadData loadData) {
        loadData.setLoadMoveStatus(DBConstants.RETRIEVEPENDING);
        loadServer.updateLoadData(loadData, false);
    }
    private OrderLineData prepareOrderLineDataToPersist(String generatedOrderId, LoadLineItemData loadLineItem,
            LoadData loadData) {

        OrderLineData orderLineData = Factory.create(OrderLineData.class);
        orderLineData.setOrderID(generatedOrderId);
        orderLineData.setItem(loadLineItem.getItem());
        orderLineData.setOrderLot(loadLineItem.getLot());
        orderLineData.setLineID(loadLineItem.getLineID());
        orderLineData.setOrderQuantity(loadLineItem.getCurrentQuantity());
        orderLineData.setLoadID(loadLineItem.getLoadID());
        orderLineData.setContainerType(loadData.getContainerType());
        orderLineData.setWarehouse(loadData.getWarehouse());

        return orderLineData;
    }
    private int generatedOrderId()
    {
    	int min = 8800;
    	int max = 8899;
    	return min + (int)(Math.random() * ((max - min) + 1));
    }

    // KR: All below methods are legacy code which might need to be deleted
    /**
     * autoOrderItem - Order an item to a station based on the stations enroute qty's etc.
     */
    public void addBagStageOrder(String OrderID, String destinationStation, int priority, String item, String lot,
            double itemQuantity) {
        OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
        try { // Get count of orders going to destination
              // station.
            ohData.setKey(OrderHeaderData.ORDERID_NAME, OrderID);
            if (mpOrderHeader.getCount(ohData) >= 1) { // An order already exists with this ID?
                logError("Error Adding Order, OrderID " + OrderID + " already exists!");
                return;
            }
        } catch (DBException e) {
            System.out.println("Error " + e + " getting order count for OrderID");
            return; // Set so order will not be created
        }
        ohData.clear();
        OrderLineData olData = Factory.create(OrderLineData.class);
        olData.clear();
        ohData.setOrderID(OrderID);
        ohData.setDestinationStation(destinationStation);
        ohData.setPriority(priority);
        ohData.setOrderStatus(DBConstants.READY);
        ohData.setOrderType(DBConstants.ITEMORDER);
        ohData.setDescription(EBSConstants.BAGSTAGE_BAG_DESC);

        olData.setOrderID(ohData.getOrderID());
        olData.setItem(item);
        olData.setOrderLot(lot);
        olData.setOrderQuantity(itemQuantity);
        olData.setHeight(1);
        olData.setLineShy(DBConstants.YES);
        OrderLineData[] olArray = { olData };

        TransactionToken vpTok = null;
        try {
            vpTok = startTransaction();
            buildBagStageOrder(ohData, olArray);
            commitTransaction(vpTok);
        } catch (DBException exc) {
            this.logException(exc, "EBSOrderServer.addBagStageOrder - " + "Error creating order " + OrderID);
            return;
        } finally {
            endTransaction(vpTok);
        }

    }

    /**
     * autoOrderItem - Order an item to a station based on the stations enroute qty's etc.
     * 
     * @throws DBException
     */
    public void addDuplicateRetrievalOrder(String destinationStation, String isLoadToRetrieve, String sErrorMsg)
            throws DBException {
        OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
        ohData.clear();
        OrderLineData olData = Factory.create(OrderLineData.class);
        olData.clear();

        StringBuffer orderID = new StringBuffer("LD");
        orderID.append(isLoadToRetrieve);
        ohData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
        while (OrderHeaderExists(ohData)) {
            orderID.setLength(2);
            orderID.append(rand.nextInt(Integer.MAX_VALUE));
            ohData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
        }

        String loadOrderID = new String(orderID);

        ohData.setOrderID(loadOrderID);
        ohData.setOrderStatus(DBConstants.ALLOCATENOW);
        ohData.setOrderType(DBConstants.FULLLOADOUT);
        ohData.setDestinationStation(destinationStation);
        ohData.setPriority(5);
        ohData.setDescription(sErrorMsg);

        olData.setOrderID(ohData.getOrderID());
        olData.setLoadID(isLoadToRetrieve);
        olData.setOrderQuantity(1.0);
        olData.setHeight(1);
        olData.setLineShy(DBConstants.NO);
        OrderLineData[] olArray = { olData };

        TransactionToken vpTok = null;
        try {
            vpTok = startTransaction();
            buildBagStageOrder(ohData, olArray);
            commitTransaction(vpTok);
        } catch (DBException exc) {
            this.logException(exc,
                    "EBSOrderServer.addDuplicateRetrievalOrder - " + "Error creating order " + loadOrderID);
            return;
        } finally {
            endTransaction(vpTok);
        }

    }

    /**
     * autoOrderItem - Order an item to a station based on the stations enroute qty's etc.
     */
    public void addSmartFlowOrder(String OrderID, String destinationStation, int priority, int itemQuantity) {
        OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
        try { // Get count of orders going to destination
              // station.
            ohData.setKey(OrderHeaderData.ORDERID_NAME, OrderID);
            if (mpOrderHeader.getCount(ohData) >= 1) { // An order already exists with this ID?
                logError("Error Adding Order, OrderID " + OrderID + " already exists!");
                return;
            }
        } catch (DBException e) {
            System.out.println("Error " + e + " getting order count for OrderID");
            return; // Set so order will not be created
        }
        ohData.clear();
        OrderLineData olData = Factory.create(OrderLineData.class);
        olData.clear();
        ohData.setOrderID(OrderID);
        ohData.setDestinationStation(destinationStation);
        ohData.setPriority(priority);
        ohData.setOrderStatus(DBConstants.ALLOCATENOW);
        // ohData.setOrderStatus(DBConstants.READY);
        ohData.setOrderType(DBConstants.ITEMORDER);
        ohData.setDescription(EBSConstants.EMPTY_TRAY_STACK_DESC);

        olData.setOrderID(ohData.getOrderID());
        olData.setItem(EBSConstants.EMPTY_TRAY_STACK);
        olData.setOrderQuantity(itemQuantity);
        olData.setHeight(1);
        olData.setLineShy(DBConstants.YES);
        OrderLineData[] olArray = { olData };

        TransactionToken vpTok = null;
        try {
            vpTok = startTransaction();
            buildSFOrder(ohData, olArray);

            commitTransaction(vpTok);

            // MCM, Oct2020
            // Moved event to allocator outside of transaction, because on idle system
            // allocator was reading OH before it got committed.
            // Ask the allocator to look at this.
            logDebug("Publishing Order Event for Order: " + ohData.getOrderID());
            String vsAllocator = EBSConstants.BCS_SMARTFLOW_ALLOCATOR;
            getSystemGateway().publishOrderEvent(ohData.getOrderID(), AllocationMessageDataFormat.NORMAL_ORDER,
                    vsAllocator);
        } catch (DBException exc) {
            this.logException(exc, "EBSOrderServer.addSmartFlowOrder - " + "Error creating order " + OrderID);
            return;
        } finally {
            endTransaction(vpTok);
        }

    }

    /**
     * Method to add orders into the system.
     *
     * @param ipHeaderData The order header.
     * @param ipLines Array of Order Lines.
     * @return String with message indicating success of operation. The intent is that this string could be used by a
     *         GUI.
     * @throws DBException if there is any type of database error.
     */
    public String buildSFOrder(OrderHeaderData ipHeaderData, OrderLineData[] ipLines) throws DBException {
        if (ipHeaderData.getOrderStatus() == DBConstants.ORBUILDING)
            ipHeaderData.setOrderStatus(DBConstants.READY);

        TransactionToken vpTok = null;
        try {
            vpTok = startTransaction();
            addOrderHeader(ipHeaderData);
            // Add the order lines.
            for (OrderLineData vpLine : ipLines) {
                int vnOrdType = ipHeaderData.getOrderType();
                if (vnOrdType == DBConstants.CONTAINER || vnOrdType == DBConstants.FULLLOADOUT)
                    addOrderLine(vpLine, false);
                else
                    addOrderLine(vpLine);
            }
            commitTransaction(vpTok);

        } catch (DBException vpExc) {
            logError("Error adding order " + ipHeaderData.getOrderID() + "::: " + vpExc.getMessage());
            throw new DBException("Error adding order " + ipHeaderData.getOrderID(), vpExc);
        } finally {
            endTransaction(vpTok);
        }

        return ("Order is added successfully.");
    }

    public void checkEBSOrder(String isOrderID) {
        initializeMoveServer();

        // If the order has no moves delete it
        try {
            if (mpMoveServ.getMoveCount(isOrderID, "", "") <= 0) {
                deleteOrder(isOrderID);
            } else {
                // if it has moves mark it scheduled
                setOrderStatusValue(isOrderID, DBConstants.SCHEDULED);
            }
        } catch (DBException e)

        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Method to add orders into the system.
     *
     * @param ipHeaderData The order header.
     * @param ipLines Array of Order Lines.
     * @return String with message indicating success of operation. The intent is that this string could be used by a
     *         GUI.
     * @throws DBException if there is any type of database error.
     */
    public String buildBagStageOrder(OrderHeaderData ipHeaderData, OrderLineData[] ipLines) throws DBException {
        if (ipHeaderData.getOrderStatus() == DBConstants.ORBUILDING)
            ipHeaderData.setOrderStatus(DBConstants.READY);

        TransactionToken vpTok = null;
        try {
            vpTok = startTransaction();
            addOrderHeader(ipHeaderData);
            // Add the order lines.
            for (OrderLineData vpLine : ipLines) {
                int vnOrdType = ipHeaderData.getOrderType();
                if (vnOrdType == DBConstants.CONTAINER || vnOrdType == DBConstants.FULLLOADOUT)
                    addOrderLine(vpLine, false);
                else
                    addOrderLine(vpLine);
            }
            commitTransaction(vpTok);

            if (ipHeaderData.getOrderStatus() == DBConstants.ALLOCATENOW) { // Send message to allocator.
                allocateOrder(ipHeaderData.getOrderID());
            } else if (ipHeaderData.getOrderStatus() == DBConstants.READY) {
                initializeStationServer();
                String vsStation = ipHeaderData.getDestinationStation();
                String vsScheduler = mpStationServer.getStationsScheduler(vsStation);
                getSystemGateway().publishControlEvent(ControlEventDataFormat
                        .getCommandTargetListMessage(ControlEventDataFormat.STAGED, new String[] { vsStation }),
                        ControlEventDataFormat.TEXT_MESSAGE, vsScheduler);
            }
        } catch (DBException vpExc) {
            logError("Error adding order " + ipHeaderData.getOrderID() + "::: " + vpExc.getMessage());
            throw new DBException("Error adding order " + ipHeaderData.getOrderID(), vpExc);

        } finally {
            endTransaction(vpTok);
        }

        return ("Order is added successfully.");
    }

    
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
          
          //KR: need to investigate this, but for now it seems we don't need to call this
          // Deallocate moves
          //backOffInventory(isOrderID);
          
          // If there are ANY moves left, mark the order as KILLED.
          MoveData mvdata = Factory.create(MoveData.class);
          mvdata.setKey(MoveData.ORDERID_NAME, isOrderID);
          if (Factory.create(Move.class).getCount(mvdata) > 0)
            nextStatus = DBConstants.KILLED;
          mvdata.clear();
          mvdata = null;
          
          // At this point nextStatus should be set to either DONE or KILLED
          if (nextStatus == DBConstants.DONE || nextStatus ==DBConstants.KILLED)
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
      //KR: don't need this here
      //deleteAuxiliaryRecords(ordData);

      oldata.clear();
      oldata = null;
      ohData.clear();
      ohData = null;
    }

    
   
   
    /**
     * Method adds Order Line to the database.
     * 
     * @param oldata <code>OrderLineData</code> containing data to add.
     * @param checkItemMaster <code>boolean</code> if set to <code>true</code> this method validates item exists in Item
     *        Master.
     * @throws <code>DBException</code> if there is a duplicate record.
     */
    public String addOrderLine(OrderLineData oldata, boolean checkItemMaster) throws DBException {
       
    	if (checkItemMaster) {
            if (oldata.getItem().trim().length() == 0) {
                throw new DBException(
                        "Item not specified for Order " + oldata.getOrderID() + " Line " + oldata.getLineID());
            } else if (!itemExists(oldata.getItem())) {
                // Add default item master if we're
                // configured that way.
                if (Application.getBoolean("AddDefaultOrderItem", false)) {
                    initializeInventoryServer();
                    mpInvServ.addDefaultItem(oldata.getItem());
                } else {
                    logError("Item " + oldata.getItem() + " doesn't exist for " + "Order " + oldata.getOrderID());
                }
            }
        } else if (oldata.getLoadID().trim().length() != 0) { // If the load is filled in, assume this
                                                              // is a Load Order. Make sure the load exists.
            Load vpLoad = Factory.create(Load.class);
            if (!vpLoad.exists(oldata.getLoadID())) {
                throw new DBException("Load " + oldata.getLoadID() + " doesn't exist for Order " + oldata.getOrderID(),
                        HostError.NO_DATA_FOUND);
            }
        }
    	
        // Check for duplicate record.
        oldata.clearKeys();
        oldata.setKey(OrderLineData.ORDERID_NAME, oldata.getOrderID());
        oldata.setKey(OrderLineData.ITEM_NAME, oldata.getItem());
        oldata.setKey(OrderLineData.ORDERLOT_NAME, oldata.getOrderLot());
        oldata.setKey(OrderLineData.LINEID_NAME, oldata.getLineID());
        oldata.setKey(OrderLineData.LOADID_NAME, oldata.getLoadID());

        if (mpOrderLine.exists(oldata)) {
            throw new DBException("Order Line " + oldata.getOrderID() + ", " + oldata.getItem() + ", "
                    + oldata.getOrderLot() + ", " + oldata.getLineID() + " already Exists!", true);
        } else if (oldata.getOrderQuantity() <= 0) {
            throw new DBException(
                    "Order Quantity may not be negative or zero! " + "Order: " + oldata.getOrderID() + ", Item: "
                            + oldata.getItem() + ", Lot: " + oldata.getOrderLot() + ", Line: " + oldata.getLineID());
        } else if (oldata.getRouteID().trim().length() != 0) { // Validate route if it is provided.
            StandardRouteServer routeServer = Factory.create(StandardRouteServer.class);
            if (!routeServer.exists(oldata.getRouteID().trim())) {
                throw new DBException("Invalid Route \'" + oldata.getRouteID() + "\' specified in Order Line. "
                        + "Order: " + oldata.getOrderID() + ", Item: " + oldata.getItem() + ", Lot: "
                        + oldata.getOrderLot() + ", Line: " + oldata.getLineID());
            }
        }

        TransactionToken vpTok = null;
        try {
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
        } catch (DBException e) {
            logException(e, "Inside addOrderLine");
            throw e;
        } finally {
            endTransaction(vpTok);
        }

        return ("Order Line Added Successfully");
    }

    protected void initializeEBSHostServer() {
        if (mpEBSHostServ == null) {
            mpEBSHostServ = Factory.create(EBSHostServer.class);
        }
    }

    protected void initializeMoveServer() {
        if (mpMoveServ == null) {
            mpMoveServ = Factory.create(StandardMoveServer.class);
        }
    }

    public boolean isAlreadyProcessed(String orderId) throws DBException {
        return mpOrderHeader.orderIdPrefixExists(orderId);
    }

    /**
     * Delete the requested order line data 
     * @param orderLineData - order line details
     * @throws DBException
     */
    public void deleteOrderLine(OrderLineData orderLineData) throws DBException
    {
          tnData.clear();
          tnData.setTranCategory(DBConstants.ORDER_TRAN);
          tnData.setTranType(DBConstants.DELETE_ORDER_LINE);
          tnData.setOrderID(orderLineData.getOrderID());
          tnData.setLineID(orderLineData.getLineID());
          tnData.setItem(orderLineData.getItem());
          tnData.setLot(orderLineData.getOrderLot());
          tnData.setLoadID(orderLineData.getLoadID());
          orderLineData.clearKeys();
          orderLineData.setKey(OrderLineData.LOADID_NAME, orderLineData.getLoadID());
          mpOrderLine.deleteElement(orderLineData);
          logTransaction(tnData);
    }

    /**
     * Delete the order header if there is no order line associated to that order.
     * @param orderID - unique identifier of order header
     * @throws DBException - If anything goes wrong
     */
    public void deleteOrderHeaderIfNecessary(String orderID) throws DBException {
		// If there is no orderline then delete the order header
		if (getOrderLineCount(orderID, "", "") < 0) {
			OrderHeaderData ohData = Factory.create(OrderHeaderData.class);
			ohData.setKey(OrderHeaderData.ORDERID_NAME, orderID);
			ohData = mpOrderHeader.getElement(ohData, DBConstants.NOWRITELOCK);
			
			// Log Delete history (including where
			// Order was going)
			tnData.clear();
			tnData.setTranCategory(DBConstants.ORDER_TRAN);
			tnData.setTranType(DBConstants.DELETE_ORDER);
			tnData.setOrderID(orderID);
			tnData.setOrderType(ohData.getOrderType());
			tnData.setToStation(ohData.getDestinationStation());
			logTransaction(tnData);
			mpOrderHeader.deleteElement(ohData);
		} else {
			logError("Order ID: " + orderID + " can not deleted as it has order line.");
		}
	}
}
