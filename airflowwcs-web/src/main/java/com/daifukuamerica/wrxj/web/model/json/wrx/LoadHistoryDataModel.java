package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.LoadTransactionHistoryData;

public class LoadHistoryDataModel
{
	public Date startingDate;
	public Date endingDate;

	public WebLoadTransactionHistoryData historyData = null;

	/**
	 * Constructor
	 */
	public LoadHistoryDataModel()
	{
		
	}

	/**
	 * Construct the outer class with TransactionHistoryData information
	 *
	 * @param ohd
	 * @throws NoSuchFieldException
	 */
	public LoadHistoryDataModel(LoadTransactionHistoryData dbdata) throws NoSuchFieldException
	{
		setStartingDate(dbdata.getStartingDate());
		setEndingDate(dbdata.getEndingDate());
	}

	/**
	 * Inner class for constructing TransactionHistoryData object from fields
	 */
	protected class WebLoadTransactionHistoryData extends LoadTransactionHistoryData
	{
		public WebLoadTransactionHistoryData(LoadHistoryDataModel dm) throws NoSuchFieldException
		{
			setStartingDate(dm.getStartingDate());
			setEndingDate(dm.getEndingDate());
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
}
