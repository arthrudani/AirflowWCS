package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;

public class TransactionHistoryDataModel
{
	public Date startingDate;
	public Date endingDate;
	public String loadId;
	public String lot;
	public String lineId;

	public WebTransactionHistoryData historyData = null;

	/**
	 * Constructor
	 */
	public TransactionHistoryDataModel()
	{
	}

	/**
	 * Construct the outer class with TransactionHistoryData information
	 *
	 * @param ohd
	 * @throws NoSuchFieldException
	 */
	public TransactionHistoryDataModel(TransactionHistoryData dbdata) throws NoSuchFieldException
	{
		setStartingDate(dbdata.getStartingDate());
		setEndingDate(dbdata.getEndingDate());
	}

	/**
	 * Inner class for constructing TransactionHistoryData object from fields
	 */
	protected class WebTransactionHistoryData extends TransactionHistoryData
	{
		public WebTransactionHistoryData(TransactionHistoryDataModel dm) throws NoSuchFieldException
		{
			setStartingDate(dm.getStartingDate());
			setEndingDate(dm.getEndingDate());
			setLoadID(dm.getLoadId());
			setLot(dm.getLot());
			setLineID(dm.getLineId());
		}
	}

	public Date getStartingDate()
	{
		return startingDate;
	}

	public void setStartingDate(Date startingDate)
	{
		this.startingDate = startingDate;
	}

	public Date getEndingDate()
	{
		return endingDate;
	}

	public void setEndingDate(Date endingDate)
	{
		this.endingDate = endingDate;
	}

	public String getLoadId()
	{
		return loadId;
	}

	public void setLoadId(String loadId)
	{
		this.loadId = loadId;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}
	
	
}
