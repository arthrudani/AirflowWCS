package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemDetailModel 
{
	private static final Logger logger = LoggerFactory.getLogger(ItemDetailModel.class);
	private String item; 
	private String lot; 
	private String loadId; 
	private String positionId; 
	private String holdReason; 
	private String orderId; 
	private String expectedReciept; 
	private String orderLot; 
	private String lineId; 

	private String holdType;
	
	private WebLoadLineItemData loadLineItemData = null; 
	
	public ItemDetailModel()
	{
		//
	}
	
	public ItemDetailModel(LoadLineItemData llid)
	{
		this.item = llid.getItem(); 
		this.lot = llid.getLot(); 
		this.loadId = llid.getLoadID(); 
		this.positionId = llid.getPositionID(); 
		this.holdReason = llid.getHoldReason(); 
		this.orderId = llid.getOrderID(); 
		this.expectedReciept = llid.getExpectedReceipt(); 
		this.orderLot = llid.getOrderLot(); 
		this.lineId = llid.getLineID(); 

		try
		{
		  this.holdType = DBTrans.getStringValue(LoadLineItemData.HOLDTYPE_NAME,llid.getHoldType());
		}
		catch(NoSuchFieldException nsfe)
		{
			logger.error("Error Formatting JSON for Load Line item Data: {}", nsfe.getMessage());
		}
	}

	protected class WebLoadLineItemData extends LoadLineItemData
	{
		public WebLoadLineItemData(ItemDetailModel idm) throws NoSuchFieldException
		{
			this.clear();
			this.setItem(idm.getItem());
			this.setLoadID(idm.getLoadId());
			this.setLot(idm.getLot());
			this.setPositionID(idm.getPositionId());
			this.setHoldReason(idm.getHoldReason());
			this.setOrderID(idm.getOrderId());
			this.setExpectedReceipt(idm.getExpectedReciept());
			this.setOrderLot(idm.getOrderLot());
			this.setLineID(idm.getLineId());
			
			this.setHoldType(DBTrans.getIntegerValue(LoadLineItemData.HOLDTYPE_NAME, idm.getHoldType()));
		}
	}

	public String getItem()
	{
		return item;
	}

	public void setItem(String item)
	{
		this.item = item;
	}
	
	
	public String getHoldType()
	{
		return holdType;
	}
	
	public void setHoldType(String holdType)
	{
		this.holdType = holdType;
	}


	public String getLot()
	{
		return lot;
	}

	public void setLot(String lot)
	{
		this.lot = lot;
	}

	public String getLoadId()
	{
		return loadId;
	}

	public void setLoadId(String loadId)
	{
		this.loadId = loadId;
	}

	public String getPositionId()
	{
		return positionId;
	}

	public void setPositionId(String positionId)
	{
		this.positionId = positionId;
	}

	public String getHoldReason()
	{
		return holdReason;
	}

	public void setHoldReason(String holdReason)
	{
		this.holdReason = holdReason;
	}

	public String getOrderId()
	{
		return orderId;
	}

	public void setOrderId(String orderId)
	{
		this.orderId = orderId;
	}

	public String getExpectedReciept()
	{
		return expectedReciept;
	}

	public void setExpectedReciept(String expectedReciept)
	{
		this.expectedReciept = expectedReciept;
	}

	public String getOrderLot()
	{
		return orderLot;
	}

	public void setOrderLot(String orderLot)
	{
		this.orderLot = orderLot;
	}

	public String getLineId()
	{
		return lineId;
	}

	public void setLineId(String lineId)
	{
		this.lineId = lineId;
	}
	
	/**
	 * If we dont already have an instance of the 
	 * encapsulated load line item data, construct one 
	 * using the current state of the outer class
	 * 
	 * @return LoadLineItemData - the encapsulated load line item data
	 * @throws NoSuchFieldException
	 */
	public LoadLineItemData getLoadLineItemData() throws NoSuchFieldException
	{
		WebLoadLineItemData wllid = null; 
		if(this.loadLineItemData == null)
		{
			wllid  = new WebLoadLineItemData(this); 
		}else{
			wllid = this.loadLineItemData; 
		}
		return wllid; 
	}
	
	public void setLoadLineItemData(WebLoadLineItemData wllid)
	{
		this.loadLineItemData = wllid; 
	}
	
	
}
