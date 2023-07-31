package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSPurchaseOrderHeader;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSPurchaseOrderLine;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSPurchaseOrderLineData;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;

public class EBSPoReceivingServer extends StandardPoReceivingServer {

	protected ItemMaster mpIM = Factory.create(ItemMaster.class);
	public static final String DATE_FORMAT = "yyyyMMddHHmmss";
	protected SimpleDateFormat dft = new SimpleDateFormat(DATE_FORMAT);
	protected EBSTableJoin mpTableJoin = new EBSTableJoin(); 
	/**
	 * Constructor
	 */
	public EBSPoReceivingServer() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param isKeyName
	 */
	public EBSPoReceivingServer(String isKeyName) {
		super(isKeyName);
	}

	/**
	 * Web application constructor for per user connection pooling
	 * 
	 * @param keyName
	 * @param dbo
	 */
	public EBSPoReceivingServer(String keyName, DBObject dbo) {
		super(keyName, dbo);
	}

	/**
	 * Method to Get a list of Purchase Order Lines for BCS emulator.
	 */
	public List<Map> getTrayArrivalPurchaseOrderListForEmulation() throws DBException {
		EBSPurchaseOrderLine pol = Factory.create(EBSPurchaseOrderLine.class);
		return pol.getTrayArrivalPurchaseOrderListForEmulation();
	}

	/**
	 * Method to Get a list of Purchase Order Lines for BCS emulator.
	 */
	public List<Map> getAisleRequestPurchaseOrderListForEmulation() throws DBException {
		EBSPurchaseOrderLine pol = Factory.create(EBSPurchaseOrderLine.class);
		return pol.getAisleRequestPurchaseOrderListForEmulation();
	}

	public void addSmartFlowExpectedReceipt(String vsOrderID, String vsItem, String vsLoadID) {

		initializeInventoryServer();
		PurchaseOrderHeaderData mpPOHData = Factory.create(PurchaseOrderHeaderData.class);

		try { // Get count of orders going to destination
				// station.
			mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, vsOrderID);
			if (mpPOH.getCount(mpPOHData) >= 1) { // A Purchase order header already exists with this ID?
				logError("Error Adding ExpectedReceipt, OrderID " + vsOrderID + " already exists!");
				return;
			}

			// Is there an ER for this trayID already in system?
			if (expectedLoadExists(vsLoadID)) {
				// An ER already exists with this loadID?
				logError("Error Adding ExpectedReceipt, TrayID " + vsLoadID + " already exists!");

				// delete the old ER, so that we can add new ER
				deleteERByLoadID(vsLoadID);
			}
		} catch (DBException e) {
			System.out.println("Error " + e + " getting order count for OrderID");
			return; // Set so order will not be created
		}
		/*
		 * Build and Add the purchase order
		 */
		PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
		vpPOHData.setOrderID(vsOrderID);
		vpPOHData.setOrderStatus(DBConstants.EREXPECTED);
		vpPOHData.setExpectedDate(new Date());
		vpPOHData.setLastActivityTime(new Date());

		PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
		vpPOLData.clear();
		vpPOLData.setOrderID(vsOrderID);
		vpPOLData.setLineID("1");
		vpPOLData.setItem(vsItem);
		vpPOLData.setExpectedQuantity(EBSConstants.SMARTFLOW_DEFAULT_ER_QTY);

		List<PurchaseOrderLineData> vpPOLList = new ArrayList();
		vpPOLList.add(vpPOLData);
		try {
			buildPO(vpPOHData, vpPOLList);
		} catch (DBException dbe) {
			logException(dbe, "Error Adding in addSmartFlowExpectedReceipt ...");

		}
	}

	public void addBagStageExpectedReceipt(String vsOrderID, String vsItem, String vsLot) {

		initializeInventoryServer();
		PurchaseOrderHeaderData mpPOHData = Factory.create(PurchaseOrderHeaderData.class);

		try { // Get count of orders going to destination
				// station.
			mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, vsOrderID);
			if (mpPOH.getCount(mpPOHData) >= 1) { // A Purchase order header already exists with this ID?
				logError("Error Adding ExpectedReceipt, OrderID " + vsOrderID + " already exists!");
				return;
			}
		} catch (DBException e) {
			System.out.println("Error " + e + " getting order count for OrderID");
			return; // Set so order will not be created
		}
		/*
		 * Build and Add the purchase order
		 */
		PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
		vpPOHData.setOrderID(vsOrderID);
		vpPOHData.setOrderStatus(DBConstants.EREXPECTED);
		vpPOHData.setExpectedDate(new Date());
		vpPOHData.setLastActivityTime(new Date());

		PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
		vpPOLData.clear();
		vpPOLData.setOrderID(vsOrderID);
		vpPOLData.setLineID("1");
		if (!mpIM.exists(vsItem)) {
			mpInvServer.addItemMasterFromString(vsItem, "Auto Store Item", true);
		}
		vpPOLData.setItem(vsItem);
		vpPOLData.setLot(vsLot);
		vpPOLData.setExpectedQuantity(EBSConstants.BAGSTAGE_DEFAULT_ER_QTY);

		List<PurchaseOrderLineData> vpPOLList = new ArrayList();
		vpPOLList.add(vpPOLData);
		try {
			buildPO(vpPOHData, vpPOLList);
		} catch (DBException dbe) {
			logException(dbe, "Error Adding in addSmartFlowExpectedReceipt ...");

		}
	}

	public void addUnknownBagStageExpectedReceipt(String vsOrderID, String vsLoadID, String vsItem, String vsLot) {

		initializeInventoryServer();
		PurchaseOrderHeaderData mpPOHData = Factory.create(PurchaseOrderHeaderData.class);

		try {
			// Get count of orders with this requestID
			mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, vsOrderID);
			if (mpPOH.getCount(mpPOHData) >= 1) { // A Purchase order header already exists with this ID?
				logError("Error Adding ExpectedReceipt, OrderID " + vsOrderID + " already exists!");
				return;
			}

			// Is there an ER for this trayID already in system?
			if (expectedLoadExists(vsLoadID)) {
				// An ER already exists with this loadID?
				logError("Error Adding ExpectedReceipt, TrayID " + vsLoadID + " already exists!");

				// delete the old ER, so that we can add new ER
				deleteERByLoadID(vsLoadID);
			}
		} catch (DBException e) {
			System.out.println("Error " + e + " getting order count for OrderID");
			return; // Set so order will not be created
		}
		/*
		 * Build and Add the purchase order
		 */
		PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
		vpPOHData.setOrderID(vsOrderID);
		vpPOHData.setOrderStatus(DBConstants.EREXPECTED);
		vpPOHData.setExpectedDate(new Date());
		vpPOHData.setLastActivityTime(new Date());

		PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
		vpPOLData.clear();
		vpPOLData.setOrderID(vsOrderID);
		vpPOLData.setLoadID(vsLoadID);
		vpPOLData.setLineID("1");
		if (!mpIM.exists(vsItem)) {
			mpInvServer.addItemMasterFromString(vsItem, "Auto Store Item", true);
		}
		vpPOLData.setItem(vsItem);
		vpPOLData.setLot(vsLot);
		vpPOLData.setExpectedQuantity(EBSConstants.BAGSTAGE_DEFAULT_ER_QTY);

		List<PurchaseOrderLineData> vpPOLList = new ArrayList();
		vpPOLList.add(vpPOLData);
		try {
			buildPO(vpPOHData, vpPOLList);
		} catch (DBException dbe) {
			logException(dbe, "Error Adding in addSmartFlowExpectedReceipt ...");

		}
	}

	/**
	 * Create the order
	 */
	public void UpdatePOForAisleRequest(String sOrderID, String sTrayID, String sStorageInputId, int iHeight) {

		try {
			PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
			vpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, sOrderID);
			vpPOHData.setStoreStation(sStorageInputId);

			modifyPOHead(vpPOHData);

			EBSPurchaseOrderLineData vpPOLData = Factory.create(EBSPurchaseOrderLineData.class);
			vpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, sOrderID);
			vpPOLData.setLoadID(sTrayID);
			vpPOLData.setHeight(iHeight);

			modifyPOLine(vpPOLData);
		} catch (DBException dbe) {
			logException("UpdatePOForAisleRequest() - Error updating Expected Receipt", dbe);
		}
	}

	/**
	 * Deletes a purchase order.
	 *
	 * @param sPONum <code>String</code> object.
	 * @return TRUE if Successful or FALSE if not
	 * @exception DBException
	 */
	public void deleteERByLoadID(String vsLoadID) throws DBException {

		String sPONum[] = getExpectdIDByLoadID(vsLoadID);
		// An ER already exists with this loadID?
		logError("Deleting OLD ExpectedReceipt, RequestID " + sPONum[0] + ", for TrayID " + vsLoadID);

		// Now Delete the PO
		PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
		PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);

		// Delete the lines
		poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, sPONum[0]);
		List<Map> vapPOLData = pol.getAllElements(poldata);
		for (Map m : vapPOLData) {
			TransactionToken tt = null;
			try {
				tt = startTransaction();
				poldata.dataToSKDCData(m);
				poldata.setKey(PurchaseOrderLineData.ORDERID_NAME, poldata.getOrderID());
				poldata.setKey(PurchaseOrderLineData.LOADID_NAME, poldata.getLoadID());
				poldata.setKey(PurchaseOrderLineData.LINEID_NAME, poldata.getLineID());
				pol.deleteElement(poldata);
				logDeleteExpectedReceiptLine(poldata, null);
				commitTransaction(tt);

			} finally {
				endTransaction(tt);
			}
		}
		// Delete the header

		deletePOHeaderIfNecessary(sPONum[0], null, true);

		return;
	}

	/**
	 * Method to return a list of expected receipt ids. of E.R.s with a given store
	 * station.
	 * 
	 * @param isStoreStation the store station
	 * @return Array of expected receipt ids.
	 * @throws DBException if there is a DB access error.
	 */
	public String[] getExpectdIDByLoadID(String isLoadID) throws DBException {
		mpPOLData.clear();
		mpPOLData.setKey(EBSPurchaseOrderLineData.LOADID_NAME, isLoadID);
		return (mpPOL.getSingleColumnValues(EBSPurchaseOrderLineData.ORDERID_NAME, false, mpPOLData,
				SKDCConstants.NO_PREPENDER));
	}

	/**
	 * Get PO lines for a given item
	 * 
	 * @param sItem
	 * @return empty list if there is an error or no data found.
	 */
	public List<Map> getPurchaseOrderLinesByItem(String sItem) throws DBException {
		PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
		PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
		poldata.setKey(PurchaseOrderLineData.ITEM_NAME, sItem);
		return pol.getAllElements(poldata);
	}

	/**
	 * See if Purchase Order Exists For Item
	 *
	 * @param sItem
	 * @return
	 * @throws DBException
	 */
	public boolean purchaseOrderExistsByItem(String sItem) throws DBException {
		PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
		PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
		poldata.setKey(PurchaseOrderLineData.ITEM_NAME, sItem);
		return pol.exists(poldata);
	}

	/**
	 * Method to clean up old expected receipts.
	 *
	 * @param iDaysOld <code>int</code> Number of hours past.
	 */
	public void cleanupOldExpectedReceiptsByHours(int iHoursOld) {

		TransactionToken tt = null;

		try {
			String[] poList = Factory.create(EBSPurchaseOrderHeader.class).getOldPOStringListByHours(iHoursOld);

			for (int i = 0; i < poList.length && i <= EBSConstants.MAX_ER_TO_DELETE_ATATIME; i++) {
				logDebug("Cleaning up Expecteed Receipt Order: " + poList[i]);

				try {
					tt = startTransaction();

					deletePO(poList[i]);

					// Add the transaction history for the deletion
					tnData.clear();
					tnData.setTranCategory(DBConstants.ORDER_TRAN);
					tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT);
					tnData.setOrderID(poList[i]);
					logTransaction(tnData);
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
	 * Method to clean up old expected receipts.
	 *
	 * @param iDaysOld <code>int</code> Number of hours past.
	 */
	public void deleteOlderEmptyTrayPOForTray(String sRequestid, String sLoadid) {

		TransactionToken tt = null;

		try {
			String[] poList = Factory.create(EBSPurchaseOrderHeader.class).getOlderEmptyTrayPOForTray(sRequestid,
					sLoadid);

			for (int i = 0; i < poList.length; i++) {
				logDebug("Deleting older PO:" + poList[i] + ", for TrayID: " + sLoadid);

				try {
					tt = startTransaction();

					deletePO(poList[i]);

					// Add the transaction history for the deletion
					tnData.clear();
					tnData.setTranCategory(DBConstants.ORDER_TRAN);
					tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT);
					tnData.setOrderID(poList[i]);
					logTransaction(tnData);
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
	public void deletePOForTrayItem(String sLoadid,String sGlobalId, String sItem) {

		TransactionToken tt = null;

		try {
			String[] poList = Factory.create(EBSPurchaseOrderHeader.class).getPOForTrayItem(sLoadid,sGlobalId,sItem);

			for (int i = 0; i < poList.length; i++) {
				logDebug("Deleting older PO:" + poList[i] + ", for TrayID: " + sLoadid);

				try {
					tt = startTransaction();
					//KR: there is issue calling deletePO db deadlock which i try to avoid
					//deletePO(poList[i]);
					mpTableJoin.deletePurchaseOrderLineByOrderId(poList[i]);
					mpTableJoin.deletePurchaseOrderHeaderByOrderId(poList[i]);
					// Add the transaction history for the deletion
					tnData.clear();
					tnData.setTranCategory(DBConstants.ORDER_TRAN);
					tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT);
					tnData.setOrderID(poList[i]);
					tnData.setLoadID(sLoadid);
					tnData.setItem(sItem);
					logTransaction(tnData);
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
	 * Method to clean up old expected receipts.
	 *
	 * @param iDaysOld <code>int</code> Number of hours past.
	 */
	public void deleteOlderPOForTrayItem(String sRequestid, String sLoadid, String sItem) {

		TransactionToken tt = null;

		try {
			String[] poList = Factory.create(EBSPurchaseOrderHeader.class).getOlderPOForTrayItem(sRequestid, sLoadid,
					sItem);

			for (int i = 0; i < poList.length; i++) {
				logDebug("Deleting older PO:" + poList[i] + ", for TrayID: " + sLoadid);

				try {
					tt = startTransaction();

					deletePO(poList[i]);

					// Add the transaction history for the deletion
					tnData.clear();
					tnData.setTranCategory(DBConstants.ORDER_TRAN);
					tnData.setTranType(DBConstants.DELETE_EXPECTED_RECEIPT);
					tnData.setOrderID(poList[i]);
					logTransaction(tnData);
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

	public StationData getAssignedStoreStationForPO(String sOrderID) {
		initializeStationServer();

		String vsStoreStation = null;

		try {
			vsStoreStation = (String) mpPOH.getSingleColumnValue(sOrderID, PurchaseOrderHeaderData.STORESTATION_NAME);

			if ((vsStoreStation != null) && !vsStoreStation.isEmpty()) {
				return (mpStationServ.getStation(vsStoreStation));
			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Adding new Method
	 * Created by Nalin Hudson / Date : 2022-03-25
	 * this would be the replacement of the 'addBagStageExpectedReceipt' methodAdds PurchaseOrder record to database.
	 *
	 * @param vsOrderID <code>Order ID</code> String.
	 * @param vsItem <code>Item</code> String.
	 * @param vsLot <code>sLot</code> String.
	 */
	public void addBagPOExpectedReceipt(String vsOrderID, String vsItem, String vsLot) {

		initializeInventoryServer();
		PurchaseOrderHeaderData mpPOHData = Factory.create(PurchaseOrderHeaderData.class);

		try { 
			// Get count of orders going to destination station.
			mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, vsOrderID);
			if (mpPOH.getCount(mpPOHData) >= 1) {
				// A Purchase order header already exists with this ID?
				logError("Error Adding ExpectedReceipt, OrderID " + vsOrderID + " already exists!");
				return;
			}
		} catch (DBException e) {
			System.out.println("Error " + e + " getting order count for OrderID");
			return; // Set so order will not be created
		}

		/*
		 * Build and Add the purchase order
		 */
		PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
		vpPOHData.setOrderID(vsOrderID);
		vpPOHData.setOrderStatus(DBConstants.EREXPECTED);
		vpPOHData.setExpectedDate(new Date());
		vpPOHData.setLastActivityTime(new Date());

		PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
		vpPOLData.clear();
		vpPOLData.setOrderID(vsOrderID);
		vpPOLData.setLineID("1");
		vpPOLData.setLot(vsLot);
		vpPOLData.setExpectedQuantity(EBSConstants.BAGSTAGE_DEFAULT_ER_QTY);

		// this section need to clarify
		if (!mpIM.exists(vsItem)) {
			mpInvServer.addItemMasterFromString(vsItem, "Auto Store Item", true);
		}
		// This should be Bag_On_Tray, OOG_Bag_On_Tray, for now
		vpPOLData.setItem(vsItem);

		List<PurchaseOrderLineData> vpPOLList = new ArrayList<PurchaseOrderLineData>();

		try {
			// to avoid the superclass adding data to PurchaseOrder Line record using unique key (sPONum).
			// send a empty list of Purchase Order LineData
			buildPO(vpPOHData, vpPOLList);

			// over ride the superclass addPOLine method and add the Purchase Order LineData
			vpPOLList.add(vpPOLData);
			for (PurchaseOrderLineData vpELData : vpPOLList) {
				addPOLine(vpELData);
			}
		} catch (DBException dbe) {
			logException(dbe, "Error Adding in addBagPOExpectedReceipt ...");
		}
	}

	/**
	 * Adds one PurchaseOrder Line record using unique key (sPONum).
	 *
	 * @param addpol <code>PurchaseOrder</code> object.
	 * @return TRUE if Successful or FALSE if not
	 * @exception DBException
	 */
	public boolean addPOLine(PurchaseOrderLineData addpol) throws DBException {

		if (!purchaseOrderExists(addpol.getOrderID())) {
			throw new DBException(
					"Attempt to add P.O. Line failed!  No Header " + "info. found!  P.O: " + addpol.getOrderID());
		}

		boolean rtn = false;
		TransactionToken tt = null;
		try {
			tt = startTransaction();
			ItemMaster itemMast = Factory.create(ItemMaster.class);

			// check the item is in the DB like : Bag_On_Tray, OOG_Bag_On_Tray, Bag for now
			if (!itemMast.exists(addpol.getItem())) {
				throw new DBException(
						"Attempt to add P.O. Line failed! No Item Found the Item Master " + addpol.getItem());
			} else {
				// Make sure expiration date is checked if existing item master says to check
				// it.
				ItemMasterData vpImdata = itemMast.getItemMasterData(addpol.getItem());
				if (vpImdata.getExpirationRequired() == DBConstants.YES
						&& !isValidExpirationDate(addpol.getExpirationDate())) {
					throw new DBException("Invalid expiration date provided for P.O. " + "line. Order: "
							+ addpol.getOrderID() + " Item: " + addpol.getItem() + " Lot: " + addpol.getLot()
							+ ". Item " + addpol.getItem() + " does not exist!");
				}
			}

			mpPOL.addElement(addpol);
			logAddExpectedReceiptLine(addpol);

			commitTransaction(tt);
			rtn = true;
		} catch (DBException exc) {
			throw new DBException("Expected Line not added for " + "PO: " + addpol.getOrderID() + " item: "
					+ addpol.getItem() + " Line ID: " + addpol.getLineID(), exc);
		} finally {
			endTransaction(tt);
		}

		return rtn;
	}
	
	/**
	 * Adds PurchaseOrder record.
	 * Created by Nalin Hudson / Date : 2022-05-17
	 *
	 * @param mpROMData <code>ExpectedReceiptMessageData</code> object.
	 * @throws ParseException 
	 * @exception DBException
	 */ 
	public void addPOExpectedReceipt(ExpectedReceiptMessageData mpROMData) throws ParseException {
		initializeInventoryServer();
		PurchaseOrderHeaderData mpPOHData = Factory.create(PurchaseOrderHeaderData.class);
		String vsOrderID = mpROMData.getOrderId();

		try { 
			// Get count of orders going to destination station.
			mpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, vsOrderID);
			if (mpPOH.getCount(mpPOHData) >= 1) {
				// A Purchase order header already exists with this ID?
				logError("Error Adding ExpectedReceipt, OrderID " + vsOrderID + " already exists!");
				return;
			}
		} catch (DBException e) {
			System.out.println("Error " + e + " getting order count for OrderID");
			return; 
		}
		
		/*
		 * Build and Add the purchase order
		 */
		PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
		vpPOHData.setOrderID(vsOrderID);
		vpPOHData.setOrderStatus(DBConstants.EREXPECTED);
		
		Date scheduleDate = new Date();
		if(!StringUtils.isBlank(mpROMData.getFlightScheduledDateTime()))
		{
			scheduleDate = dft.parse(String.valueOf(mpROMData.getFlightScheduledDateTime()));
		}
		vpPOHData.setExpectedDate(scheduleDate);
		vpPOHData.setLastActivityTime(new Date());
		vpPOHData.setFinalSortLocationId(mpROMData.getFinalSortLocation());

		PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
		vpPOLData.clear();
		vpPOLData.setOrderID(vsOrderID);
		vpPOLData.setLineID(mpROMData.getLineId());
		vpPOLData.setLot(mpROMData.getLot());
		vpPOLData.setItem(mpROMData.getItemType());
		vpPOLData.setLoadID(mpROMData.getLoadId());
		vpPOLData.setGlobalID(mpROMData.getGlobalId());
		
		Date defaultRetrievalDate = new Date();
		if(!StringUtils.isBlank(mpROMData.getDefaultRetrievalDateTime()))
		{
			defaultRetrievalDate = dft.parse(String.valueOf(mpROMData.getDefaultRetrievalDateTime()));
		}		
		vpPOLData.setExpirationDate(defaultRetrievalDate);
		vpPOLData.setExpectedQuantity(EBSConstants.BAGSTAGE_DEFAULT_ER_QTY);
		
		List<PurchaseOrderLineData> vpPOLList = new ArrayList<PurchaseOrderLineData>();

		try {
			// to avoid the superclass adding data to PurchaseOrder Line record using 			
			// unique key (sPONum), send an empty list of Purchase Order LineData
			buildPO(vpPOHData, vpPOLList);

			// over ride the superclass addPOLine method and add the Purchase Order LineData
			vpPOLList.add(vpPOLData);
			for (PurchaseOrderLineData vpELData : vpPOLList) {
				addPOLine(vpELData);
			}
		} catch (DBException dbe) {
			logException(dbe, "Error Adding in addPOExpectedReceipt ...");
		}
	}

	/**
	 * update PurchaseOrder record in the database.
	 * Created by Nalin Hudson / Date : 2022-05-17
	 *
	 * @param exReceiptMsgData <code>ExpectedReceiptMessageData</code> object.
	 * @throws ParseException 
	 * @exception DBException
	 */ 
	public void updatePOExpectedReceipt(ExpectedReceiptMessageData exReceiptMsgData) throws ParseException {
		String vsOrderID = exReceiptMsgData.getOrderId();
		String vsLoardID = exReceiptMsgData.getLoadId();
		Date expectedDate = new Date();
		Date expirationDate= new Date();
		
		if(!StringUtils.isBlank(exReceiptMsgData.getFlightScheduledDateTime()))
		{
			expectedDate = dft.parse(String.valueOf(exReceiptMsgData.getFlightScheduledDateTime()));
		}	
		if(!StringUtils.isBlank(exReceiptMsgData.getDefaultRetrievalDateTime()))
		{
			expirationDate = dft.parse(String.valueOf(exReceiptMsgData.getDefaultRetrievalDateTime()));
		}	
		
		try {
			PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);			
			vpPOHData.setKey(PurchaseOrderHeaderData.ORDERID_NAME, vsOrderID);			
		//	vpPOHData.setStoreStation(exReceiptMsgData.getStoreStation());
			vpPOHData.setExpectedDate(expectedDate); 
			vpPOHData.setLastActivityTime(new Date());			
			//update the Purchase Order header details 
			modifyPOHead(vpPOHData);

			EBSPurchaseOrderLineData vpPOLData = Factory.create(EBSPurchaseOrderLineData.class);
			vpPOLData.setKey(PurchaseOrderLineData.ORDERID_NAME, vsOrderID);
			vpPOLData.setKey(PurchaseOrderLineData.LOADID_NAME, vsLoardID);
			vpPOLData.setExpirationDate(expirationDate);
			//update the Purchase Order Line details 
			modifyPOLine(vpPOLData);
			
		} catch (DBException dbe) {
			logException("updatePOExpectedReceipt() - Error updating Expected Receipt", dbe);
		}
		
	}
	
	//US31492 - Getting the purchase order line for the given load id
	public PurchaseOrderLineData getPurchaseOrderLineByLoadId(String loadId) throws DBException {
		EBSPurchaseOrderLine pol = Factory.create(EBSPurchaseOrderLine.class);
		return pol.getPurchaseOrderLineByLoadId(loadId);
	}

	/**
	 * Returns the list of all purchase order line by the given lot
	 * 
	 * @param lot lot/flight number
	 * @return the list of all purchase order line by the given lot
	 * @throws DBException when anything goes wrong
	 */
    public List<PurchaseOrderLineData> getPurchaseOrderLinesByLot(String lot) throws DBException {
        PurchaseOrderLine pol = Factory.create(PurchaseOrderLine.class);
        PurchaseOrderLineData poldata = Factory.create(PurchaseOrderLineData.class);
        poldata.setKey(PurchaseOrderLineData.LOT_NAME, lot);
        return pol.getAllElements(poldata).stream().map(entry -> {
            PurchaseOrderLineData line = Factory.create(PurchaseOrderLineData.class);
            line.dataToSKDCData(entry);
            return line;
        }).collect(Collectors.toList());
    }
}
