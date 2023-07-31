package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;

public class OrderDataModel
{

	public Date modifyDate; 
	public Date orderDate; 
	public Date scheduleDate; 
	public Date shortOrderCheckDate; 
	public Integer hostLineCount; 
	public Integer nextStatus; 
	public Integer orderStatus; 
	public String sOrderStatus; 
	public String sOrderType; 
	public Integer orderType; 
	public Integer priority; 
	public String addMethod; 
	public String carrierId; 
	public String description; 
	public String destAddress; 
	public String destStation; 
	public String destWarehouse; 
	public String orderId; 
	public String orderMessage; 
	public String releaseToCode; 
	public String shipCustomer; 
	public String updateMethod;
	public String item;
	
	public WebOrderHeaderData orderData = null; 
	
	public OrderDataModel()
	{
		//
	}
	
	/**
	 * Construct the outer class with OrderHeaderData information
	 * 
	 * @param ohd
	 * @throws NoSuchFieldException 
	 */
	public OrderDataModel(OrderHeaderData ohd) throws NoSuchFieldException
	{
		if (ohd != null)
		{
			this.modifyDate = ohd.getModifyTime(); 
			this.orderDate = ohd.getOrderedTime(); 
			this.scheduleDate = ohd.getScheduledDate(); 
			this.shortOrderCheckDate = ohd.getShortOrderCheckTime(); 
			this.hostLineCount = ohd.getHostLineCount(); 
			this.nextStatus = ohd.getNextStatus(); 
			this.orderStatus = ohd.getOrderStatus(); 
			this.sOrderStatus = DBTrans.getStringValue(OrderHeaderData.ORDERSTATUS_NAME, ohd.getOrderStatus()); 
			this.sOrderType = DBTrans.getStringValue(OrderHeaderData.ORDERTYPE_NAME, ohd.getOrderType()); 
			this.orderType = ohd.getOrderType(); 
			this.priority= ohd.getPriority(); 
			this.addMethod = ohd.getAddMethod(); 
			this.carrierId = ohd.getCarrierID(); 
			this.description = ohd.getDescription(); 
			this.destAddress = ohd.getDestAddress(); 
			this.destWarehouse = ohd.getDestWarehouse(); 
			this.destStation = ohd.getDestinationStation();
			this.orderId = ohd.getOrderID(); 
			this.orderMessage = ohd.getOrderMessage(); 
			this.releaseToCode = ohd.getReleaseToCode(); 
			this.shipCustomer = ohd.getShipCustomer(); 
			this.updateMethod = ohd.getUpdateMethod(); 
		}
	}
	
	/*
	 * Inner class for constructing OrderHeaderData object from fields
	 */
	protected class WebOrderHeaderData extends OrderHeaderData
	{
		public WebOrderHeaderData(OrderDataModel odm) throws NoSuchFieldException
		{
			this.clear(); 
			this.setModifyTime(odm.getModifyDate());
			this.setOrderedTime(odm.getOrderDate());
			this.setScheduledDate(odm.getScheduleDate());
			this.setShortOrderCheckTime(odm.getShortOrderCheckDate());
			this.setHostLineCount(odm.getHostLineCount());
			this.setNextStatus(odm.getNextStatus());; 
			this.setOrderStatus(odm.getOrderStatus());
			this.setOrderType(odm.getOrderType());
			this.setPriority(odm.getPriority());
			this.setAddMethod(odm.getAddMethod());
			this.setCarrierID(odm.getCarrierId());
			this.setDescription(odm.getDescription());
			this.setDestAddress(odm.getDestAddress());
			this.setDestWarehouse(odm.getDestWarehouse());
			this.setDestinationStation(odm.getDestStation());
			this.setOrderID(odm.getOrderId());
			this.setOrderMessage(odm.getOrderMessage());
			this.setReleaseToCode(odm.getReleaseToCode());
			this.setShipCustomer(odm.getShipCustomer());
			this.setUpdMethod(odm.getUpdateMethod());
		}
	}
	
	public Date getModifyDate()
	{
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate)
	{
		this.modifyDate = modifyDate;
	}
	public Date getOrderDate()
	{
		return orderDate;
	}
	public void setOrderDate(Date orderDate)
	{
		this.orderDate = orderDate;
	}
	public Date getScheduleDate()
	{
		return scheduleDate;
	}
	public void setScheduleDate(Date scheduleDate)
	{
		this.scheduleDate = scheduleDate;
	}
	public Date getShortOrderCheckDate()
	{
		return shortOrderCheckDate;
	}
	public void setShortOrderCheckDate(Date shortOrderCheckDate)
	{
		this.shortOrderCheckDate = shortOrderCheckDate;
	}
	public Integer getHostLineCount()
	{
		return hostLineCount;
	}
	public void setHostLineCount(Integer hostLineCount)
	{
		this.hostLineCount = hostLineCount;
	}
	public Integer getNextStatus()
	{
		return nextStatus;
	}
	public void setNextStatus(Integer nextStatus)
	{
		this.nextStatus = nextStatus;
	}
	public Integer getOrderStatus()
	{
		return orderStatus;
	}
	public void setOrderStatus(Integer orderStatus)
	{
		this.orderStatus = orderStatus;
	}
	public Integer getOrderType()
	{
		return orderType;
	}
	public void setOrderType(Integer orderType)
	{
		this.orderType = orderType;
	}
	public Integer getPriority()
	{
		return priority;
	}
	public void setPriority(Integer priority)
	{
		this.priority = priority;
	}
	public String getAddMethod()
	{
		return addMethod;
	}
	public void setAddMethod(String addMethod)
	{
		this.addMethod = addMethod;
	}
	public String getCarrierId()
	{
		return carrierId;
	}
	public void setCarrierId(String carrierId)
	{
		this.carrierId = carrierId;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public String getDestAddress()
	{
		return destAddress;
	}
	public void setDestAddress(String destAddress)
	{
		this.destAddress = destAddress;
	}
	public String getDestWarehouse()
	{
		return destWarehouse;
	}
	public void setDestWarehouse(String destWarehouse)
	{
		this.destWarehouse = destWarehouse;
	}
	public String getOrderId()
	{
		return orderId;
	}
	public void setOrderId(String orderId)
	{
		this.orderId = orderId;
	}
	public String getOrderMessage()
	{
		return orderMessage;
	}
	public void setOrderMessage(String orderMessage)
	{
		this.orderMessage = orderMessage;
	}
	public String getReleaseToCode()
	{
		return releaseToCode;
	}
	public void setReleaseToCode(String releaseToCode)
	{
		this.releaseToCode = releaseToCode;
	}
	public String getShipCustomer()
	{
		return shipCustomer;
	}
	public void setShipCustomer(String shipCustomer)
	{
		this.shipCustomer = shipCustomer;
	}
	public String getUpdateMethod()
	{
		return updateMethod;
	}
	public void setUpdateMethod(String updateMethod)
	{
		this.updateMethod = updateMethod;
	} 
	
	public OrderHeaderData getOrderHeaderData() throws NoSuchFieldException
	{ 
		WebOrderHeaderData wohd = null; 
		if(this.orderData == null)
		{
			wohd = new WebOrderHeaderData(this); 
		}else{
			wohd = this.orderData; 
		}
		return wohd; 
	}
	
	public void setOrderHeaderData(WebOrderHeaderData wohd)
	{ 
		this.orderData = wohd;
	}

	public String getsOrderStatus()
	{
		return sOrderStatus;
	}

	public void setsOrderStatus(String sOrderStatus)
	{
		this.sOrderStatus = sOrderStatus;
	}

	public String getsOrderType()
	{
		return sOrderType;
	}

	public void setsOrderType(String sOrderType)
	{
		this.sOrderType = sOrderType;
	}

	public WebOrderHeaderData getOrderData()
	{
		return orderData;
	}

	public void setOrderData(WebOrderHeaderData orderData)
	{
		this.orderData = orderData;
	}

	public String getDestStation() {
		return destStation;
	}

	public void setDestStation(String destStation) {
		this.destStation = destStation;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}
	
}
