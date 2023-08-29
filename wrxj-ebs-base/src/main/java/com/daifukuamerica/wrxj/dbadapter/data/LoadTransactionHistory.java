package com.daifukuamerica.wrxj.dbadapter.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Description: <BR>
 * Title: Class to handle Load Transaction History Object. Description : Handles
 * all reading and writing database for load transaction history
 * 
 * @author MT
 * @version 1.0 <BR>
 *          Created: 22-May-23<BR>
 *          Copyright (c) 2002<BR>
 *          Company: Daifuku America Corporation
 */
public class LoadTransactionHistory extends BaseDBInterface {

	protected LoadTransactionHistoryData mpLoadTransactionHistoryData;

	public LoadTransactionHistory() {
		super("LoadTransactionHistory");
		mpLoadTransactionHistoryData = Factory.create(LoadTransactionHistoryData.class);
	}

	/**
	 * Add a load transaction history
	 *
	 * @param loadTransactionHistoryData
	 * @throws DBException When anything goes wrong
	 */
	public void addLoadTransactionHistory(LoadTransactionHistoryData loadTransactionHistoryData) throws DBException {
		addElement(loadTransactionHistoryData);
	}

	/**
	 * Fetch the load transaction history by load id
	 * @param loadID - unique identifier for load
	 * @return LoadTransactionHistory
	 * @throws DBException
	 */
	public LoadTransactionHistoryData findByLoadId(String loadID) throws DBException {
		mpLoadTransactionHistoryData.clear();
		mpLoadTransactionHistoryData.setKey(LoadTransactionHistoryData.LOADID_NAME, loadID);
		mpLoadTransactionHistoryData.setKey(LoadTransactionHistoryData.ISCOMPLETED_NAME, DBConstants.IS_NOT_COMPLETED);
		return getElement(mpLoadTransactionHistoryData, DBConstants.NOWRITELOCK);
	}
	
	/**
	 * Fetch the load transaction history by load id
	 * @param loadID - unique identifier for load
	 * @return LoadTransactionHistory
	 * @throws DBException
	 */
	public LoadTransactionHistoryData findByBarcode(String barcode) throws DBException {
		mpLoadTransactionHistoryData.clear();
		mpLoadTransactionHistoryData.setKey(LoadTransactionHistoryData.BARCODE_NAME, barcode);
		return getElement(mpLoadTransactionHistoryData, DBConstants.NOWRITELOCK);
	}

	/**
	 * Update a load transaction history
	 *
	 * @param loadTransactionHistoryData
	 * @throws DBException When anything goes wrong
	 */
	public void updateLoadTransactionHistory(LoadTransactionHistoryData loadTransactionHistoryData) throws DBException {
		if (loadTransactionHistoryData.getKeyObject(LoadData.LOADID_NAME) == null)
	    {
	      loadTransactionHistoryData.setKey(LoadData.LOADID_NAME, loadTransactionHistoryData.getLoadID());
	    }
		loadTransactionHistoryData.setKey(LoadTransactionHistoryData.ISCOMPLETED_NAME, DBConstants.IS_NOT_COMPLETED);
		modifyElement(loadTransactionHistoryData);
	}
}
