package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;

public class PurchaseOrderDataModel
{
	// Purchase Order Header
	public String orderId;
	public Date expectedDate;
	public Integer orderStatus;
	public String storeStation;
	public String vendorId;
	public Date lastActivityTime;
	public Integer hostLineCount;

	public String addMethod;
	public String updateMethod;
	public Date modifyDate;

	// Purchase Order Line
	public String item;
	public String loadId;


	public WebPurchaseOrderHeaderData orderData = null;

	/**
	 * Constructor
	 */
	public PurchaseOrderDataModel()
	{
		//
	}

	/**
	 * Construct the outer class with OrderHeaderData information
	 *
	 * @param ohd
	 * @throws NoSuchFieldException
	 */
	public PurchaseOrderDataModel(PurchaseOrderHeaderData ohd) throws NoSuchFieldException
	{
		if (ohd != null)
		{
			this.orderId = ohd.getOrderID();
			this.expectedDate = ohd.getExpectedDate();
			this.orderStatus = ohd.getOrderStatus();
			this.storeStation = ohd.getStoreStation();
			this.vendorId = ohd.getVendorID();
			this.lastActivityTime = ohd.getLastActivityTime();
			this.hostLineCount = ohd.getHostLineCount();

			this.addMethod = ohd.getAddMethod();
			this.updateMethod = ohd.getUpdateMethod();
			this.modifyDate = ohd.getModifyTime();
		}
	}

	/*
	 * Inner class for constructing OrderHeaderData object from fields
	 */
	protected class WebPurchaseOrderHeaderData extends PurchaseOrderHeaderData
	{
		public WebPurchaseOrderHeaderData(PurchaseOrderDataModel odm) throws NoSuchFieldException
		{
			this.clear();
			this.setOrderID(odm.getOrderId());
			this.setExpectedDate(odm.getExpectedDate());
			this.setOrderStatus(odm.getOrderStatus());
			this.setStoreStation(odm.getStoreStation());
			this.setVendorID(odm.getVendorId());
			this.setLastActivityTime(odm.getLastActivityTime());
			this.setHostLineCount(odm.getHostLineCount());

			this.setAddMethod(odm.getAddMethod());
			this.setUpdMethod(odm.getUpdateMethod());
			this.setModifyTime(odm.getModifyDate());
		}
	}

	public String getOrderId()
	{
		return orderId;
	}

	public void setOrderId(String orderId)
	{
		this.orderId = orderId;
	}

	public Date getExpectedDate()
	{
		return expectedDate;
	}

	public void setExpectedDate(Date expectedDate)
	{
		this.expectedDate = expectedDate;
	}

	public Integer getOrderStatus()
	{
		return orderStatus;
	}

	public void setOrderStatus(Integer orderStatus)
	{
		this.orderStatus = orderStatus;
	}

	public String getStoreStation()
	{
		return storeStation;
	}

	public void setStoreStation(String storeStation)
	{
		this.storeStation = storeStation;
	}

	public String getVendorId()
	{
		return vendorId;
	}

	public void setVendorId(String vendorId)
	{
		this.vendorId = vendorId;
	}

	public Date getLastActivityTime()
	{
		return lastActivityTime;
	}

	public void setLastActivityTime(Date lastActivityTime)
	{
		this.lastActivityTime = lastActivityTime;
	}

	public Integer getHostLineCount()
	{
		return hostLineCount;
	}

	public void setHostLineCount(Integer hostLineCount)
	{
		this.hostLineCount = hostLineCount;
	}

	public String getAddMethod()
	{
		return addMethod;
	}

	public void setAddMethod(String addMethod)
	{
		this.addMethod = addMethod;
	}

	public String getUpdateMethod()
	{
		return updateMethod;
	}

	public void setUpdateMethod(String updateMethod)
	{
		this.updateMethod = updateMethod;
	}

	public Date getModifyDate()
	{
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate)
	{
		this.modifyDate = modifyDate;
	}

	// Purchase Order Line

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getLoadId() {
		return loadId;
	}

	public void setLoadId(String loadId) {
		this.loadId = loadId;
	}
}
