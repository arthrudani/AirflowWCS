package com.daifukuamerica.wrxj.web.model.json.wrx;

public class StoreModel
{

	private String station; 
	private String containerType; 
	private String loadId; 
	private String expectedReceipt; 
	private String amountFull; 
	
	public StoreModel()
	{

	}

	public StoreModel(String station, String containerType, String loadId, String expectedReceipt)
	{
		this.station = station;
		this.containerType = containerType;
		this.loadId = loadId;
		this.expectedReceipt = expectedReceipt;
	}
	
	public StoreModel(String station, String containerType, String loadId, String expectedReceipt, int amountFull)
	{
		this.station = station;
		this.containerType = containerType;
		this.loadId = loadId;
		this.expectedReceipt = expectedReceipt;
		this.amountFull = Integer.toString(amountFull); 
	}
	
	public StoreModel(String station, String containerType, String loadId, String expectedReceipt, String amountFull)
	{
		this.station = station;
		this.containerType = containerType;
		this.loadId = loadId;
		this.expectedReceipt = expectedReceipt;
		this.amountFull = amountFull; 
	}

	public String getStation()
	{
		return station;
	}

	public void setStation(String station)
	{
		this.station = station;
	}

	public String getContainerType()
	{
		return containerType;
	}

	public void setContainerType(String containerType)
	{
		this.containerType = containerType;
	}

	public String getLoadId()
	{
		return loadId;
	}

	public void setLoadId(String loadId)
	{
		this.loadId = loadId;
	}

	public String getExpectedReceipt()
	{
		return expectedReceipt;
	}

	public void setExpectedReceipt(String expectedReceipt)
	{
		this.expectedReceipt = expectedReceipt;
	}

	public String getAmountFull()
	{
		return amountFull;
	}

	public void setAmountFull(String amountFull)
	{
		this.amountFull = amountFull;
	}

	
	
}
