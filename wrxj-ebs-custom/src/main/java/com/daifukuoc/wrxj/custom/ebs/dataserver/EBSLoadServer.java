package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLoad;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;

public class EBSLoadServer extends StandardLoadServer {
	protected EBSLoad mpEBSLoad = Factory.create(EBSLoad.class);
	protected EBSSchedulerServer mpEBSSchedulerServer = Factory.create(EBSSchedulerServer.class);
	protected LoadLineItem mpLLI = Factory.create(LoadLineItem.class);
	protected StandardRouteServer mpRouteServer = Factory.create(StandardRouteServer.class);
	protected OrderHeader mpOrderHeader = Factory.create(OrderHeader.class);
	
	protected LoadLineItemData mpLoadLineItemData = Factory.create(LoadLineItemData.class);
	/**
	 * Constructor for load with no parameters
	 */
	public EBSLoadServer() {
		super();
	}

	/**
	 * Constructor for load with name of who is creating it and the scheduler name
	 *
	 * @param isKeyName name of creator
	 */
	public EBSLoadServer(String isKeyName) {
		super(isKeyName);
	}

	/**
	 * Web application constructor for per user connection pooling
	 * 
	 * @param keyName
	 * @param dbo
	 */
	public EBSLoadServer(String keyName, DBObject dbo) {
		super(keyName, dbo);
	}

	/**
	 * Gets the count of matching load. =
	 */
	public int getLoadCount(String isAddress, int moveStatus) throws DBException {
		LoadData lddata = Factory.create(LoadData.class);

		if (isAddress != null && isAddress.trim().length() > 0) {
			lddata.setKey(LoadData.ADDRESS_NAME, isAddress);
		}

		if (moveStatus > 0) {
			lddata.setKey(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(moveStatus));
		}

		return mpLoad.getCount(lddata);
	}

	//flushing the lane by it`s address (location)
	//from the Web UI side (Location web page) 
	//this method only create a the order details and call the allocateOrder
	public boolean flushAisle(String sAddressID) throws DBException {
		EBSOrderServer orderServ = Factory.create(EBSOrderServer.class);
		logDebug("flushAisle: " + sAddressID);
		// get list of loads in aisle address Where there is no orders created 
		// get load with ILOADMOVESTATUS = 224(#NOMOVE) AND SADDRESS = PARAM value 
		List<Map> vpLoadDataList = getLoadsByAddressWithoutOrders(sAddressID);
		logDebug("flush Aisle for Loads, Load count for flush: " + vpLoadDataList.size());
		
		OrderHeaderData oh = buildOrderHeader(EBSConstants.FLUSH_PRIORITY, null);
		OrderLineData[] lineList = new OrderLineData[vpLoadDataList.size()];

		int i = 0;
		String destinationStation = null, wareHouse = null, route = null;

		for (Map vpLoadMap : vpLoadDataList) {
			String vsLoadID = DBHelper.getStringField(vpLoadMap, LoadData.LOADID_NAME);
			wareHouse = DBHelper.getStringField(vpLoadMap, LoadData.WAREHOUSE_NAME);
			route = DBHelper.getStringField(vpLoadMap, LoadData.ROUTEID_NAME);
			destinationStation = mpRouteServer
					.getNextRouteDest(DBHelper.getStringField(vpLoadMap, LoadData.ROUTEID_NAME), sAddressID);

			// get the load line item associate with LoadID
			String sItemID = null;
			List<Map> loadLineItems = mpLLI.getLoadLineItemDataListByLoadID(vsLoadID);

			for (Iterator<Map> it = loadLineItems.iterator(); it.hasNext();) {
				Map tMap = it.next();
				sItemID = DBHelper.getStringField(tMap, LoadLineItemData.ITEM_NAME);
			}

			logDebug("flush Aisle for OrderID created, OrderID: " + oh.getOrderID()+", LoadId: " + vsLoadID);
			lineList[i] = buildLoadOrderLine(oh.getOrderID(), vsLoadID, sItemID, wareHouse, route);
			i++;
			//updateLoadMoveStatus(vsLoadID);
		}
		if(vpLoadDataList.size()>0) {
			oh.setDestinationStation(destinationStation);
			String rslt = orderServ.buildOrder(oh, lineList);
			logDebug("build Order Stauts : " + rslt);
			logDebug("build Order for flush location "+sAddressID+ ", Order Id: "+oh.getOrderID());
			return true;
		}
		else {
			System.out.println("build Order status : failed, There is a order is pending Order for Flush");
			return false;	
		}
	}
	
	private List<Map> getLoadsByAddressWithoutOrders(String sAddressID) throws DBException {
		return mpEBSLoad.getLoadsByAddressWithoutOrders(DBConstants.NOMOVE,sAddressID);
	}

	public OrderHeaderData buildOrderHeader(int inPriority, String station) throws DBException {

		StandardOrderServer vpOrdServ = Factory.create(StandardOrderServer.class);

		// fill in what is needed for a load order
		OrderHeaderData oh = Factory.create(OrderHeaderData.class);
		oh.clear();

		String loadOrderID = new String(vpOrdServ.createRandomOrderID());
		oh.setOrderID(loadOrderID);
		oh.setOrderStatus(DBConstants.ALLOCATENOW);
		oh.setOrderType(DBConstants.FULLLOADOUT);
		oh.setPriority(inPriority);
		oh.setDescription("Order for flush lane");
		oh.setDestinationStation(station);

		return oh;
	}

	/**
	 * Method to add a Load Order with the specified priority to retrieve the
	 * specified load to the specified destination
	 * 
	 * @param isLoadToRetrieve <code>String</code> to be retrieved
	 * @param inPriority       <code>int</code> of the order
	 * @param isDestination    <code>String</code>
	 * @return <code>OrderHeaderData</code> Object of created order header
	 * @throws DBException
	 */
	public OrderLineData buildLoadOrderLine(String loadOrderID, String isLoadToRetrieve, String sItem, String wareHouse,
			String route) throws DBException {

		OrderLineData ol = Factory.create(OrderLineData.class);
		ol.clear();
		ol.setOrderID(loadOrderID);
		ol.setLoadID(isLoadToRetrieve);
		ol.setOrderQuantity(1.0);
		ol.setAllocatedQuantity(0.0);
		ol.setPickQuantity(0.0);
		ol.setLineShy(DBConstants.NO);
		ol.setItem(sItem);
		ol.setWarehouse(wareHouse);
		ol.setRouteID(route);
		return ol;
	}

	public void updateLoadMoveStatus(String loadID) throws DBException {
		TransactionToken vpTok = null;
		try {
			vpTok = startTransaction();
			mpLoadData.clear();
			mpLoadData.setLoadMoveStatus(DBConstants.MOVEPENDING);
			mpLoadData.setKey(LoadData.LOADID_NAME, loadID);
			mpLoad.modifyElement(mpLoadData);
			commitTransaction(vpTok);
		} catch (java.util.NoSuchElementException e) {
			throw new DBException("Cannot update Load move Status: " + loadID + ", does not exist", e);
		} finally {
			endTransaction(vpTok);
		}
	}

	/**
	 * Method to clean up stranded IDPending loads.
	 *
	 * @param iDaysOld <code>int</code> Number of minutes past.
	 */
	public void cleanupIDPendingAtInput(int dMins) {

		TransactionToken tt = null;

		try {
			String[] ldList = Factory.create(EBSLoad.class).getOldIDPendingListByMins(dMins);

			for (int i = 0; i < ldList.length; i++) {

				try {
					tt = startTransaction();

					rejectIDPendingLoad(ldList[i]);

					commitTransaction(tt);
				} catch (DBException e) {
					logError(e.getMessage());
				} finally {
					endTransaction(tt);
				}
			}

		} catch (DBException e) {
			logError(e.getMessage());
		}
	}

	/**
	 * Method to clean up stranded IDPending loads.
	 *
	 * @param iDaysOld <code>int</code> Number of minutes past.
	 */
	public void cleanupArrivedAtOutput(int dSecs) {

		TransactionToken tt = null;

		try {
			String[] ldList = Factory.create(EBSLoad.class).getOldArrivedListByMins(dSecs);

			for (int i = 0; i < ldList.length; i++) {

				try {
					tt = startTransaction();

					LoadData vpLoadData = getLoad(ldList[i]);
					mpEBSSchedulerServer.autoPickArrivedLoad(vpLoadData);

					commitTransaction(tt);
				} catch (DBException e) {
					logError(e.getMessage());
				} finally {
					endTransaction(tt);
				}
			}

		} catch (DBException e) {
			logError(e.getMessage());
		}
	}

	public void rejectIDPendingLoad(String sLoadid) {

		LoadData ld = null;
		StationData stationData = null;

		initializeStationServer();

		// add the load before we add any items or store it
		try {
			ld = getLoad(sLoadid);
			stationData = mpStationServer.getStation(ld.getAddress());
			if (stationData == null) {

			}
			logError("Missing PLC Arrival at Input " + ld.getAddress() + ", Rejecting UnIdentifed Tray " + sLoadid);

			ld.setNextWarehouse(stationData.getWarehouse());
			ld.setNextAddress(stationData.getRejectRoute());

			ld.setLoadMoveStatus(DBConstants.MOVEPENDING);
			updateLoadInfo(ld);
		} catch (DBException e2) {
			logException(e2, "Error adding Load " + sLoadid + " - " + e2.getMessage());
		}
	}

	public List<LoadData> getLoadAtAddressList(String warehouse, String address, int loadStatus) throws DBException {
		return mpEBSLoad.getLoadAtAddressList(warehouse, address, loadStatus, DBConstants.NOWRITELOCK);
	}

	//DK:30400
	/**
	   * Log a load move transaction
	   *
	   * @param isLoadID
	   * @param isFromWarehouse
	   * @param isFromAddress
	   * @param isFromPosition
	   * @param isToWarehouse
	   * @param isToAddress
	   * @param isToPosition
	   * @param isDeviceID
	   */
	public void logLoadStorageCompleteTransaction(String isLoadID, String isToAddress, String isDeviceID) {
		tnData.clear();
		tnData.setTranCategory(EBSDBConstants.TransactionHistory.CATEGORY.LOAD_TRAN);
		tnData.setTranType(EBSDBConstants.TransactionHistory.TRANSACTION_TYPE.STORAGE_COMPLETION);
		tnData.setLoadID(isLoadID);
		tnData.setLocation("");
		tnData.setToLoadID(isLoadID);
		tnData.setToStation(isToAddress);
		tnData.setDeviceID(isDeviceID);
		tnData.setReasonCode(PLCConstants.PLC_ITEM_STORED_MSG_TYPE);
		tnData.setActionDescription("Item Stored");
		logTransaction(tnData);
	}
	
	
	/**
	 * Log item arrived transaction
	 * 
	 * @param loadData
	 * @param loadLineData
	 * @param tranCategory
	 * @param tranType
	 * @param stationId
	 * @param deviceId
	 * @param lineId
	 * @param orderId
	 * @return
	 */
	public void logLoadItemArrivedTransaction(LoadData loadData, LoadLineItemData loadLineData, String stationId,
			String deviceId, String lineId, String orderId) {
		tnData.clear();
		tnData.setTranCategory(DBTrans.LOAD_TRAN);
		tnData.setTranType(DBTrans.ITEM_SHIP);
		tnData.setLoadID(loadData.getLoadID());
		tnData.setLot(loadLineData.getLot());
		tnData.setItem(loadLineData.getItem());
		tnData.setToLoadID(loadData.getLoadID());
		tnData.setLocation(loadData.getWarehouse(),loadData.getAddress());
		tnData.setToStation(stationId);
		tnData.setDeviceID(deviceId);
		tnData.setLineID(lineId);
		tnData.setOrderID(orderId);
		tnData.setCurrentQuantity(loadLineData.getCurrentQuantity());
		tnData.setAgingDate(loadLineData.getAgingDate());
		tnData.setExpirationDate(loadLineData.getExpirationDate());
		tnData.setLastCCIDate(loadLineData.getLastCCIDate());
		tnData.setHoldType(loadLineData.getHoldType());
		tnData.setModifyTime(loadData.getModifyTime());
		tnData.setOrderLot(loadLineData.getOrderLot());
		tnData.setReasonCode(PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE);
		tnData.setActionDescription("Item Arrived");
		logTransaction(tnData);
	}
	
	public void logLoadItemReleasedTransaction(LoadData loadData, LoadLineItemData loadLineData, String stationId,
			String deviceId, String lineId, String orderId) {
		tnData.clear();
		tnData.setTranCategory(DBTrans.LOAD_TRAN);
		tnData.setTranType(DBTrans.ITEM_SHIP);
		tnData.setLoadID(loadData.getLoadID());
		tnData.setLot(loadLineData.getLot());
		tnData.setItem(loadLineData.getItem());
		tnData.setToLoadID(loadData.getLoadID());
		tnData.setLocation(loadData.getWarehouse() , loadData.getAddress());
		tnData.setStation(loadData.getCurrentAddress());
		tnData.setToStation(stationId);
		tnData.setDeviceID(deviceId);
		tnData.setLineID(lineId);
		tnData.setOrderID(orderId);
		tnData.setCurrentQuantity(loadLineData.getCurrentQuantity());
		tnData.setAgingDate(loadLineData.getAgingDate());
		tnData.setExpirationDate(loadLineData.getExpirationDate());
		tnData.setLastCCIDate(loadLineData.getLastCCIDate());
		tnData.setHoldType(loadLineData.getHoldType());
		tnData.setModifyTime(loadData.getModifyTime());
		tnData.setOrderLot(loadLineData.getOrderLot());
		tnData.setReasonCode(PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE);
		tnData.setActionDescription("Item Released");
		logTransaction(tnData);
	}
	
	/**
	 * Log item arrived transaction
	 * 
	 * @param loadData
	 * @param loadLineData
	 * @param tranCategory
	 * @param tranType
	 * @param stationId
	 * @param deviceId
	 * @param lineId
	 * @param orderId
	 * @return
	 */
	public void logLoadItemPickedUpTransaction(LoadData loadData, LoadLineItemData loadLineData, String stationId,
			String deviceId, String lineId, String orderId) {
		tnData.clear();
		tnData.setTranCategory(DBTrans.LOAD_TRAN);
		tnData.setTranType(DBTrans.ITEM_PICK);
		tnData.setLoadID(loadData.getLoadID());
		tnData.setLot(loadLineData.getLot());
		tnData.setItem(loadLineData.getItem());
		tnData.setToLoadID(loadData.getLoadID());
		//tnData.setToLocation(loadData.getWarehouse() + loadData.getAddress());
		tnData.setLocation(loadData.getWarehouse() , loadData.getAddress());
		tnData.setStation(loadData.getCurrentAddress());
		tnData.setToStation(stationId);
		tnData.setDeviceID(deviceId);
		tnData.setLineID(lineId);
		tnData.setOrderID(orderId);
		tnData.setCurrentQuantity(loadLineData.getCurrentQuantity());
		tnData.setAgingDate(loadLineData.getAgingDate());
		tnData.setExpirationDate(loadLineData.getExpirationDate());
		tnData.setLastCCIDate(loadLineData.getLastCCIDate());
		tnData.setHoldType(loadLineData.getHoldType());
		tnData.setModifyTime(loadData.getModifyTime());
		tnData.setOrderLot(loadLineData.getOrderLot());
		tnData.setReasonCode(PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE);
		tnData.setActionDescription("Item Picked Up");
		logTransaction(tnData);
	}
	
	public List<Map> getLoadbyFlightNo(String flightNo) throws DBException {
		return mpEBSLoad.getLoadsByFlightNo(flightNo);
	}
	
	public List<Map> getLoadbyFlightNo(String flightNo, int noOfBags) throws DBException {
		return mpEBSLoad.getLoadsByFlightNo(flightNo, noOfBags);
	}
	
	public LoadLineItemData getLoadLineByLoadId(String isLoadID) {
		LoadLineItem loadLineItem = Factory.create(LoadLineItem.class);
		LoadLineItemData loadLineItemData = Factory.create(LoadLineItemData.class);

		List<Map> loadLineItems;
		try {
			loadLineItems = loadLineItem.getLoadLineItemDataListByLoadID(isLoadID);
			if (loadLineItems.size() > 0) {
				Map vpLoadMap = loadLineItems.get(0); // Fetch top row only.
				loadLineItemData.dataToSKDCData(vpLoadMap);
			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return loadLineItemData;
	}

	/**
	 * Create a new expected load on the found empty location
	 * 
	 * @param ldData The load data to be persisted
	 * @return true if the given load data is persisted
	 * @throws DBException When anything goes wrong
	 */
	public boolean addExpectedLoad(LoadData ldData) throws DBException
	{
	    TransactionToken transactionToken = null;
	    boolean isAdded = false;
	    
	    try {
            transactionToken = startTransaction();
            
            isAdded = addLD(ldData);
            
            commitTransaction(transactionToken);            
        } finally {
            endTransaction(transactionToken);
        }
        return isAdded;
	}
	
    public void updateReservedLocation(LoadData existingLoadData, String address) throws DBException {
        TransactionToken vpTok = null;
        try {
            vpTok = startTransaction();
            
            mpLoadData.clear();            
            mpLoadData.setAddress(address);
            mpLoadData.setKey(LoadData.LOADID_NAME, existingLoadData.getLoadID());
            mpLoad.modifyElement(mpLoadData);
            
            logTransaction_LoadModify(mpLoadData, existingLoadData);
            
            commitTransaction(vpTok);
        } catch (java.util.NoSuchElementException e) {
            throw new DBException("Cannot update address of a load: " + existingLoadData.getLoadID() + " to " + address + " because it's not found", e);
        } finally {
            endTransaction(vpTok);
        }
    }
    
    public void updatLoadLineItem(LoadLineItemData existingloadLineItemData ) throws DBException
	{
		TransactionToken vpTok = null;
        try {
            vpTok = startTransaction();
          
            mpLoadLineItemData.clear();   
            mpLoadLineItemData.setOrderID(existingloadLineItemData.getOrderID());

            mpLoadLineItemData.setKey(LoadData.LOADID_NAME, existingloadLineItemData.getLoadID());
            mpLLI.modifyElement(mpLoadLineItemData);

            commitTransaction(vpTok);
        } catch (java.util.NoSuchElementException e) {
            throw new DBException("Cannot update LoadLineItem OrderID of a load: " + existingloadLineItemData.getLoadID() + " to " + existingloadLineItemData.getOrderID() + " because it's not found", e);
        } finally {
            endTransaction(vpTok);
        }
	}
    
    /**
     * Method to update a load line item record.
     *
     * @param ipLoadLineItemInfo load line item info. to update.
     * @throws DBException
     */
     public void modifyLoadLineItemData(LoadLineItemData ipLoadLineItemInfo) throws DBException
     {
       TransactionToken vpTok = null;
       try
       {
         vpTok = startTransaction();
         mpLLI.modifyElement(ipLoadLineItemInfo);
         commitTransaction(vpTok);
       }
       catch(NoSuchElementException nse)
       {
         throw new DBException("Unable to modify load!", nse);
       }
       finally
       {
         endTransaction(vpTok);
       }
     }
}
