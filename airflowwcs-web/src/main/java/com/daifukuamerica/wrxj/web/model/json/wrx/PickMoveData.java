package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.factory.Factory;

public class PickMoveData extends MoveData
{
	private Integer confirmPickQuantity; 
	private String itemDescription; 
	private String carrier; 
	private Double allocated; 
	private Double remaining; 
	private Double picked; 
	private Integer recMaxPickQty;
	
	public PickMoveData()
	{
		super(); 
	}
	
	public PickMoveData(MoveData moveData)
	{
		super(); 
		this.setPickLot(moveData.getPickLot());
		this.setParentLoad(moveData.getParentLoad()); //
		this.setLoadID(moveData.getLoadID()); //
		this.setPickToLoadID(moveData.getPickToLoad()); //
		this.setItem(moveData.getItem()); //
		this.setOrderLot(moveData.getOrderLot()); // 
		this.setSchedulerName(moveData.getSchedulerName()); //
		this.setRouteID(moveData.getRouteID()); //
		this.setDeviceID(moveData.getDeviceID()); // 
		this.setReleaseToCode(moveData.getReleaseToCode()); 
		this.setLineID(moveData.getLineID()); //
		this.setDestWarehouse(moveData.getDestWarehouse()); //
		this.setNextWarehouse(moveData.getNextWarehouse()); //
		this.setNextAddress(moveData.getNextAddress()); //
		this.setWarehouse(moveData.getWarehouse()); //
		this.setAddress(moveData.getAddress()); //
		this.setDisplayMessage(moveData.getDisplayMessage());
		this.setPositionID(moveData.getPositionID());
		this.setMoveDate(moveData.getMoveDate());
		this.setMoveID(moveData.getMoveID());
		this.setAisleGroup(moveData.getAisleGroup());
		this.setMoveSequence(moveData.getMoveSequence());
		this.setPriority(moveData.getPriority());
		this.setMoveType(moveData.getMoveType());
		this.setMoveCategory(moveData.getMoveCategory());
		this.setMoveStatus(moveData.getMoveStatus());
		this.setPickQuantity(moveData.getPickQuantity());
		this.setOrderID(moveData.getOrderID()); //
	}
	
	public PickMoveData(MoveData moveData, String itemDescription,Double allocated, Double picked)
	{
		this(moveData); 
		this.itemDescription = itemDescription; 
	//	this.carrier = carrier; 
		this.picked = picked; 
		this.remaining = allocated-picked; 
		this.allocated = allocated; 
	}
	
	public PickMoveData(MoveData moveData, String itemDescription, int recMaxPickQty)
	{
		this(moveData); 
		this.itemDescription = itemDescription; 
		this.recMaxPickQty = recMaxPickQty; 
	}
	public PickMoveData(Integer confirmPickQuantity)
	{
		this.confirmPickQuantity = confirmPickQuantity; 
	}

	public Integer getConfirmPickQuantity()
	{
		return confirmPickQuantity;
	}

	public void setConfirmPickQuantity(Integer confirmPickQuantity)
	{
		this.confirmPickQuantity = confirmPickQuantity;
	}

	public String getItemDescription()
	{
		return itemDescription;
	}

	public void setItemDescription(String itemDescription)
	{
		this.itemDescription = itemDescription;
	}

	public String getCarrier()
	{
		return carrier;
	}

	public void setCarrier(String carrier)
	{
		this.carrier = carrier;
	}

	public Double getAllocated()
	{
		return allocated;
	}

	public void setAllocated(Double allocated)
	{
		this.allocated = allocated;
	}

	public Double getRemaining()
	{
		return remaining;
	}

	public void setRemaining(Double remaining)
	{
		this.remaining = remaining;
	}

	public Double getPicked()
	{
		return picked;
	}

	public void setPicked(Double picked)
	{
		this.picked = picked;
	}

	public Integer getRecMaxPickQty() 
	{
		return recMaxPickQty;
	}

	public void setRecMaxPickQty(Integer recMaxPickQty) 
	{
		this.recMaxPickQty = recMaxPickQty;
	}

}
