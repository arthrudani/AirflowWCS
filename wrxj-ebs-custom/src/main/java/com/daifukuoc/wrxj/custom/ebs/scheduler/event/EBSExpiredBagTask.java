package com.daifukuoc.wrxj.custom.ebs.scheduler.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSOrderServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.scheduler.event.TimedEventTask;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * check Expired Bag details on set interval default set interval is 10sec The
 * list of loads will pick based on the Expired date and not move status(224)
 * for those Loads this class will create orders accordingly and Allocation task
 * service will pick those order and process
 * 
 * @author Nalin Hudson: 2020-06-03
 */
public class EBSExpiredBagTask extends TimedEventTask {
	protected StandardRouteServer mpRouteServer = Factory.create(StandardRouteServer.class);
	protected EBSOrderServer orderServ = Factory.create(EBSOrderServer.class);
	protected EBSLoadServer loadServer = Factory.create(EBSLoadServer.class);
	protected OrderHeader mpOrderHeader = Factory.create(OrderHeader.class);
	protected OrderLine mpOrderLine = Factory.create(OrderLine.class);
	protected StandardOrderServer standOrderServer = Factory.create(StandardOrderServer.class);

	public EBSExpiredBagTask(String isName) {
		super(isName);
	}

	@Override
	public String initTask() {
		try {
			StandardConfigurationServer mpConfigSrvr = Factory.create(StandardConfigurationServer.class);
			if (mpConfigSrvr.isSplitSystem() && mpConfigSrvr.isThisPrimaryJVM() == false) {
				mpLogger.logOperation("INVALID JVM (" + Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY)
						+ ") - a Secondary JVM - EBSExpiredBagTask will not be started.");
				return " ";
			}
		} catch (DBException e) {
			return e.getMessage();
		}

		int vnSecs = getConfigValue(INTERVAL);
		msIntervalString = vnSecs + " seconds ";
		mnInterval = vnSecs * 1000;
		if (vnSecs < 1)
			return "INVALID EBSExpiredBagTask interval - " + vnSecs + " EBSExpiredBagTask will not be started.";

		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {

		mpLogger.logDebug("EBSExpiredBagTask- run() start");
		String stationId = null, sItemID = null, route = null, wareHouse = null, loadId = null;
		Hashtable<String, String> my_dict = new Hashtable<String, String>();
		ArrayList<OrderHeaderData> orderHeaderList = new ArrayList<OrderHeaderData>();
		List<OrderLineData> orderLineList = new ArrayList<OrderLineData>();
		OrderHeaderData orderHeader = Factory.create(OrderHeaderData.class);

		try {

			// Get the Expired Bag Loads
			List<Map> allLoadData = getExpiredBagLoadsList();			

			for (Map vpLoadMap : allLoadData) {
				
				route = DBHelper.getStringField(vpLoadMap, LoadData.ROUTEID_NAME);
				sItemID = DBHelper.getStringField(vpLoadMap, LoadLineItemData.ITEM_NAME);
				wareHouse = DBHelper.getStringField(vpLoadMap, LoadData.WAREHOUSE_NAME);
				loadId = DBHelper.getStringField(vpLoadMap, LoadData.LOADID_NAME);
				stationId = mpRouteServer.getNextRouteDest(route, DBHelper.getStringField(vpLoadMap, LoadData.ADDRESS_NAME));

				if (!my_dict.containsKey(stationId)) {

					// check order is already created for the station and with status #ALLOCATENOW
					if (!orderHeaderExists(stationId)) {

						// create the order header for this station
						orderHeader = loadServer.buildOrderHeader(EBSConstants.FLUSH_PRIORITY, stationId);
						orderHeaderList.add(orderHeader);

						OrderLineData olineData = loadServer.buildLoadOrderLine(orderHeader.getOrderID(), loadId,
								sItemID, wareHouse, route);
						orderLineList.add(olineData);

						System.out.println(
								"build Order header and build order Line for Order ID :" + orderHeader.getOrderID());
						mpLogger.logDebug(
								"build Order header and build order Line for Order ID :" + orderHeader.getOrderID());

						my_dict.put(stationId, orderHeader.getOrderID());
						
					} else {
						// If order Header exist get order header data ID
						String exisitingOrderId = getOrderHeaderData(stationId);
						System.out.println("Order header already exisit in database for Order ID :" + exisitingOrderId);

						// check order line is in the database
						if (!orderLineExists(exisitingOrderId, sItemID, route, wareHouse, loadId)) {

							// create the new order line for existing order header
							mpLogger.logDebug("adding new Order Line to exisiting Order ID :" + exisitingOrderId);
							createOrderLineforExisitingOrderHeader(exisitingOrderId, sItemID, route, wareHouse, loadId);
							System.out.println("create a Order line for exisiting order : " + exisitingOrderId);
						}
						// since there is order line existing for with same data no need to anything in
						// else section
					}
				} else {
					// Get the created order header id, then created build order line for the order
					// header
					String createOrdId = my_dict.get(stationId);
					OrderLineData olineData = loadServer.buildLoadOrderLine(createOrdId, loadId, sItemID, wareHouse, route);
					orderLineList.add(olineData);
				}
			}

			// Create the Orders in the database			
			createOrders(orderHeaderList, orderLineList);

		} catch (DBException e) {
			e.printStackTrace();
			mpLogger.logError("EBSExpired Bag Task Error msg:" + e.getMessage());
		}
		mpLogger.logDebug("EBSExpiredBagTask- run() ended");
	}

	private void createOrderLineforExisitingOrderHeader(String orderID, String sItemID, String route, String wareHouse,
			String loadId) {
		try {
			OrderLineData olineData = loadServer.buildLoadOrderLine(orderID, loadId, sItemID, wareHouse, route);
			standOrderServer.addOrderLine(olineData);
			System.out.println("adding new Order Line to exisiting Order ID :" + orderID);
		}
		catch (DBException e) {
			e.printStackTrace();
			mpLogger.logError(
					"adding new Order line Failed for Order Id : " + orderID + ", Error msg:" + e.getMessage());
		}
	}

	/**
	 * create Order header and the order lines in the database
	 * 
	 * @param orderHeaderList List of Order header data
	 * @param lineList        array of Order line data
	 */
	private void createOrders(ArrayList<OrderHeaderData> orderHeaderList, List<OrderLineData> lineList) {

		List<OrderLineData> resultList = new ArrayList<OrderLineData>();
		// filter out the order line data for the particular order header data
		// should not have a order line data without any order header
		for (OrderHeaderData ohDate : orderHeaderList) {
			resultList = lineList.stream().filter(x -> x.getOrderID().equals(ohDate.getOrderID()))
					.collect(Collectors.toList());

			OrderLineData[] orderlineList = resultList.toArray(new OrderLineData[resultList.size()]);

			try {
				if (orderlineList.length > 0) {
					mpLogger.logDebug("Creating new Order in database");
					String rslt = orderServ.buildOrder(ohDate, orderlineList);
					mpLogger.logDebug("Order Created for Order Id : " + ohDate.getOrderID());
					mpLogger.logDebug("Order Lines for Order " + ohDate.getOrderID() + " : " + resultList.size());
					mpLogger.logDebug("Created Order Status : " + rslt);
				}

			} catch (DBException e) {

				e.printStackTrace();
				mpLogger.logError(
						"build Order Failed for Order Id : " + ohDate.getOrderID() + ", Error msg:" + e.getMessage());
			}
		}
	}

	// Get all the records from load and load line that Expired the date
	@SuppressWarnings("rawtypes")
	private List<Map> getExpiredBagLoadsList() throws DBException {

		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> loadData = vpTJLoadHandler.getExpiredBagLoads();
		mpLogger.logDebug("getLoadsList from LoadServer, load count - " + loadData.size());
		return loadData;
	}

	/**
	 * Method checks if the order header exists.
	 * 
	 * @param ordData The order data to use in the search.
	 * @return true if order exists, false otherwise.
	 * @throws DBException if there is a database access error.
	 */
	private boolean orderHeaderExists(String stationId) throws DBException {

		OrderHeaderData oh = Factory.create(OrderHeaderData.class);
		oh.clear();
		oh.setKey(OrderHeaderData.DESTINATIONSTATION_NAME, stationId);
		//oh.setKey(OrderHeaderData.ORDERSTATUS_NAME, DBConstants.ALLOCATENOW);
		oh.setKey(OrderHeaderData.ORDERTYPE_NAME, DBConstants.FULLLOADOUT);
		return mpOrderHeader.exists(oh);
	}

	/**
	 * Method checks if the order header exists.
	 * 
	 * @param ordData The order data to use in the search.
	 * @return true if order exists, false otherwise.
	 * @throws DBException if there is a database access error.
	 */
	private String getOrderHeaderData(String stationId) throws DBException {

		OrderHeaderData oh = Factory.create(OrderHeaderData.class);
		oh.clear();
		oh.setKey(OrderHeaderData.DESTINATIONSTATION_NAME, stationId);
		//oh.setKey(OrderHeaderData.ORDERSTATUS_NAME, DBConstants.ALLOCATENOW);
		oh.setKey(OrderHeaderData.ORDERTYPE_NAME, DBConstants.FULLLOADOUT);

		List<Map> ordHead = standOrderServer.getOrderHeaderData(oh);
		if (ordHead.size() > 0) {
			oh.setOrderID(ordHead.get(0).get(OrderHeaderData.ORDERID_NAME).toString());
		}
		return oh.getOrderID();
	}

	/**
	 * Method checks if the order line exists.
	 * 
	 * @param ordData The order line data to use in the search.
	 * @return true if order line exists, false otherwise.
	 * @throws DBException if there is a database access error.
	 */
	private boolean orderLineExists(String orderID, String sItemID, String route, String wareHouse, String loadId)
			throws DBException {
		OrderLineData oldata = Factory.create(OrderLineData.class);
		oldata.clearKeys();
		oldata.setKey(OrderLineData.ORDERID_NAME, orderID);
		oldata.setKey(OrderLineData.ITEM_NAME, sItemID);
		oldata.setKey(OrderLineData.ROUTEID_NAME, route);
		oldata.setKey(OrderLineData.WAREHOUSE_NAME, wareHouse);
		oldata.setKey(OrderLineData.LOADID_NAME, loadId);
		return mpOrderLine.exists(oldata);
	}

}