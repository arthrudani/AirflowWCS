package com.daifukuoc.wrxj.custom.ebs.scheduler.event.hostcleanuptask;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.scheduler.event.TimedEventTask;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSPurchaseOrderHeader;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

/**
 * Deletes load and loadline if PO is not completed for more then 5 hours based
 * on status.
 * 
 * @author BT
 */
@SuppressWarnings("unused")
public class HostCleanupTask extends TimedEventTask {
	private TransactionHistoryData tnData = Factory.create(TransactionHistoryData.class);

	private EBSTableJoin mpTableJoin;
	int mnCutoff;

	public HostCleanupTask(String isName) {
		super(isName);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {

		mpLogger.logDebug("HOSTcleanup- run() start");
		ensureDBConnection();
		TransactionToken ttok = null;
		try {
			ttok = mpDBObject.startTransaction();
			// Get the not completed purchase order header
			List<Map> allNotCompletedPO = getNotCompletedPOList();
			for (Map vpLoadMap : allNotCompletedPO) {
				
				String sOrderId=(String) vpLoadMap.get("SORDERID");
				
				List<Map> allNotCompletedPOL = getNotCompletedPOL(sOrderId);
				String sLoadID=(String) allNotCompletedPOL.get(0).get("SLOADID");
				
				List<Map> LOAD = getLOAD(sLoadID);
				List<Map> LOADLINEITEM = getLOADLINEITEM(sLoadID);
				String sAddress=(String) LOAD.get(0).get("SADDRESS");
				
				List<Map> locationBySAddress = getlocationBySAddress(sAddress);
				if(locationBySAddress.get(0).get("ILOCATIONSTATUS")==(Integer)30) {
					changeStatusOfLocation(locationBySAddress.get(0));
				}
				
				//addToTransactionHistory(allNotCompletedPOL.get(0),LOAD.get(0),locationBySAddress.get(0),LOADLINEITEM.get(0));
				setPOtoComplete(vpLoadMap);
				deleteLLItemandLOAD(sLoadID);
				
				mpDBObject.commitTransaction(ttok);
			}
		} catch (DBException e) {
			e.printStackTrace();
			mpLogger.logError("HOSTcleanup Task Error msg:" + e.getMessage());
		}
		mpLogger.logDebug("HOSTcleanup- run() ended");
		mpDBObject.endTransaction(ttok);
	}


	

	@Override
	public String initTask() {
		int vnSecs = getConfigValue(INTERVAL);
		msIntervalString = vnSecs + " seconds ";
		mnInterval = vnSecs*1000;
		if (vnSecs < 1)
			return "INVALID HostAllocationTask interval - " + vnSecs + " HostAllocationTask will not be started.";

		return null;
	}
	
	//add record to TRANSACTIONHISTORY
	private void addToTransactionHistory(Map POL, Map LOAD, Map LOCATION, Map LOADLINEITEM) {
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		System.out.println("calling");
		vpTJLoadHandler.addToTransactionHistory(POL,LOAD,LOCATION,LOADLINEITEM);
		mpLogger.logDebug("add to TRANSACTIONHISTORY");	
	}
	
	//set PURCHASEORDER to completed state
	private void setPOtoComplete(Map vpLoadMap) {
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		vpTJLoadHandler.setPOtoComplete(vpLoadMap);
		mpLogger.logDebug("set PURCHASEORDER to completed state");	
	}

	//change status of LOCATION to available from arrival pending
	private void changeStatusOfLocation(Map map) {
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		vpTJLoadHandler.changeStatusOfLocation(map);
		mpLogger.logDebug("change status of LOCATION to AVAILABLE from arrival pending");
	}

	//get LOCATION by sAddress
	private List<Map> getlocationBySAddress(String sAddress) throws DBException {
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> Location = vpTJLoadHandler.getlocationBySAddress(sAddress);
		mpLogger.logDebug("get LOCATION by sAddress");
		return Location;
	}

	//get LOAD according to sLoadId of not completed PURCHASEORDERLINE 
	private List<Map> getLOAD(String sLoadID) throws DBException {
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> LOAD = vpTJLoadHandler.getLOAD(sLoadID);
		mpLogger.logDebug("get LOAD according to sLoadId of not completed PURCHASEORDERLINE");
		return LOAD;
	}
	
	//get LOAD according to sLoadId of not completed PURCHASEORDERLINE 
		private List<Map> getLOADLINEITEM(String sLoadID) throws DBException {
			EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
			List<Map> LOADLINEITEM = vpTJLoadHandler.getLOADLINEITEM(sLoadID);
			mpLogger.logDebug("get LOAD according to sLoadId of not completed PURCHASEORDERLINE");
			return LOADLINEITEM;
		}

	//delete LOAD and LOADLINE
	private void deleteLLItemandLOAD(String sLoadID) throws DBException {
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		vpTJLoadHandler.deleteLLIandLOAD(sLoadID);
		mpLogger.logDebug("delete LOAD and LOADLINE");
	}

	
	//get not completed PURCHASEORDERLINE list
	private List<Map> getNotCompletedPOL(String sOrderId) throws DBException {
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> purchaseOrderLine = vpTJLoadHandler.getNotCompletedPOL(sOrderId);
		mpLogger.logDebug("get not completed PURCHASEORDERLINE list");
		return purchaseOrderLine;
	}

	//get not completed PURCHASEORDER list
	private List<Map> getNotCompletedPOList() throws DBException{
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> purchaseOrder = vpTJLoadHandler.getNotCompletedPO();
		mpLogger.logDebug("get not completed PURCHASEORDER list");
		return purchaseOrder;
	}
	
	
}
