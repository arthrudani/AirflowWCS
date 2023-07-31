package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;

public class WorkDataModel {
	
	public String loadId;
	public String lot;
	public String lineId;
	public String deviceId;
	public String from;
	public String toDest;
	public String command;
	public String status;
	public String moveType;
	public String orderType;
	public String orderId;
	public String ItemId;
	public String flightNum;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	public Date flightStd;
	public String finalSortLocationID;
	public String globalId;
	private WebCommandData commandData = null;
	
	public WorkDataModel()
	{
		
	}
	
	public WorkDataModel(MoveCommandData md) throws NoSuchFieldException {
		super();
		this.loadId = md.getLoadID();
		this.lot = md.getFlightNum();
		this.ItemId = md.getItemID();
		this.deviceId = md.getDeviceID();
		this.from = md.getFrom();
		this.toDest = md.getToDest();
		this.command = md.getCommand();
		this.status = DBTrans.getStringValue(MoveCommandData.STATUS_NAME, md.getCmdStatus());
		this.moveType = DBTrans.getStringValue(MoveCommandData.MOVETYPE_NAME, md.getCmdMoveType());;
		this.orderType = DBTrans.getStringValue(MoveCommandData.ORDERTYPE_NAME, md.getCmdOrderType());;
		this.orderId = md.getOrderid();
		this.flightNum = md.getFlightNum();
		this.flightStd = md.getFlightSTD();
		this.finalSortLocationID = md.getFinalSortLocationID();
		this.globalId = md.getGlobalID();
	}

	protected class WebCommandData extends MoveCommandData
	{
		public WebCommandData(WorkDataModel wdm) throws NoSuchFieldException
		{
			this.clear();
			this.setLoadID(wdm.getLoadId());
			this.setDeviceID(wdm.getDeviceId());
			this.setFrom(wdm.getFrom());
			this.setToDest(wdm.getToDest()); 
			this.setCommand(wdm.getCommand());
			this.setGlobalID(wdm.getGlobalId());
			this.setItemID(wdm.getItemId());
			this.setFlightNum(wdm.getFlightNum());
			this.setFlightSTD(wdm.getFlightStd());
			this.setOrderid(wdm.getOrderId());
			this.setCreatedDate(new Date());
			this.setLastModifyDate(new Date());
			this.setFinalSortLocationID(wdm.getFinalSortLocationID());
			this.setCmdOrderType(DBTrans.getIntegerValue(MoveCommandData.ORDERTYPE_NAME, wdm.getOrderType()));
			this.setCmdStatus(DBTrans.getIntegerValue(MoveCommandData.STATUS_NAME, wdm.getStatus()));
			this.setCmdMoveType(DBTrans.getIntegerValue(MoveCommandData.MOVETYPE_NAME, wdm.getMoveType()));
		}
	}
	public String getLoadId() {
		return loadId;
	}
	public void setLoadId(String loadId) {
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
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getToDest() {
		return toDest;
	}
	public void setToDest(String toDest) {
		this.toDest = toDest;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMoveType() {
		return moveType;
	}
	public void setMoveType(String moveType) {
		this.moveType = moveType;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getItemId() {
		return ItemId;
	}
	public void setItemId(String itemId) {
		ItemId = itemId;
	}
	public String getFlightNum() {
		return flightNum;
	}
	public void setFlightNum(String flightNum) {
		this.flightNum = flightNum;
	}
	public Date getFlightStd() {
		return flightStd;
	}
	public void setFlightStd(Date flightStd) {
		this.flightStd = flightStd;
	}
	public String getFinalSortLocationID() {
		return finalSortLocationID;
	}
	public void setFinalSortLocationID(String finalSortLocationID) {
		this.finalSortLocationID = finalSortLocationID;
	}
	
	public String getGlobalId() {
		return globalId;
	}
	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
	public MoveCommandData getMoveCommnadData() throws NoSuchFieldException{
		WebCommandData wcd = null;
		if(this.commandData==null)
		{
			wcd = new WebCommandData(this);
		}else{
			wcd = this.commandData;
		}
		return wcd;
	}
	
	public void setMoveCommandData(WebCommandData wcd)
	{
		this.commandData = wcd;
	}
}
