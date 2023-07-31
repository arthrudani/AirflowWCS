package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

public class LoadLineItemDataModel
{
	public String item = ""; 
	public String lot = ""; 
	public String loadId = ""; 
	public String positionId = ""; 
	public String holdReasone = ""; 
	public String orderId = ""; 
	public String expectedReceipt = ""; 
	public String orderLot = ""; 
	public String lineId = ""; 
	public Date lastCCIDate = new Date(); 
	public Date agingDate = new Date(); 
	public Date expirationDate = new Date(); 
	public double currentQuantity = 0; 
	public double allocatedQuantity = 0; 
	public int holdType;
	
}
