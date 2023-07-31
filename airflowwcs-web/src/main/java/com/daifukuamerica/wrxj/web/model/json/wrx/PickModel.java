package com.daifukuamerica.wrxj.web.model.json.wrx;

public class PickModel
{
	private String orderNumber =""; 
	private String location=""; 
	private String pickFromLoad="";  
	private String itemDescription=""; 
	private String item=""; 
	private String lot=""; 
	private String subLocation="";  
	private String pickQuantity=""; 
	private Integer confirmPickQuantity=0; 
	private String ipAddress = ""; 
	
	public String getOrderNumber()
	{
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber)
	{
		this.orderNumber = orderNumber;
	}
	public String getLocation()
	{
		return location;
	}
	public void setLocation(String location)
	{
		this.location = location;
	}
	public String getPickFromLoad()
	{
		return pickFromLoad;
	}
	public void setPickFromLoad(String pickFromLoad)
	{
		this.pickFromLoad = pickFromLoad;
	}
	public String getItemDescription()
	{
		return itemDescription;
	}
	public void setItemDescription(String itemDescription)
	{
		this.itemDescription = itemDescription;
	}
	public String getItem()
	{
		return item;
	}
	public void setItem(String item)
	{
		this.item = item;
	}
	public String getLot()
	{
		return lot;
	}
	public void setLot(String lot)
	{
		this.lot = lot;
	}
	public String getSubLocation()
	{
		return subLocation;
	}
	public void setSubLocation(String subLocation)
	{
		this.subLocation = subLocation;
	}
	public String getPickQuantity()
	{
		return pickQuantity;
	}
	public void setPickQuantity(String pickQuantity)
	{
		this.pickQuantity = pickQuantity;
	}
	public Integer getConfirmPickQuantity()
	{
		return confirmPickQuantity;
	}
	public void setConfirmPickQuantity(Integer confirmPickQuantity)
	{
		this.confirmPickQuantity = confirmPickQuantity;
	}
	public String getIpAddress()
	{
		return ipAddress;
	}
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	} 
	
	
	
}
