package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadDataAndLLIData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

/**
 * @author dystout
 *
 * Encapsulation of LoadData for use in Spring Databinding. Naming of variable
 * names to match <form/> 'path' variable names in the view is required to data
 * bind easily when posting.
 *
 * Eventually, when custom objects are required we can map the database with hibernate
 * and do validation entirely from JSR 303.
 *
 */

public class loadAndLLIDataModel
{
	/**
	 * When converting this object to JSON using GSON the variable
	 * names will be formatted to reflect exactly the variable names
	 * defined in the class.
	 *
	 * TODO JSR 303 Validation for form fields.
	 */

	private String loadId;
	private String containerType;
	private String shelfPosition;
	private String moveStatus;
	private String warehouse;
	private String address;
	private String parentLoad;
	private String barcode;
	private String message;
	private String mcKey;
	private String nextWarehouse;
	private String nextAddress;
	private String nextShelfPosition;
	private String finalWarehouse;
	private String finalAddress;
	private String routeId;
	private String recZone;
	private String deviceId;
	private Date loadDate;
	private String lpCheck;
	private int height;
	private Integer amountFull;
	private String sAmountFull;
	private double weight;
	private String item;
	private String currentAddress;
	
	public String lot; 
	public String positionId; 
	public String holdReasone; 
	public String orderId; 
	public String expectedReceipt; 
	public String orderLot; 
	public String globalId;
	public String lineId; 
	public Date lastCCIDate = new Date(); 
	public Date agingDate = new Date(); 
	public Date expirationDate; 
	public Date expectedDate; 
	public double currentQuantity = 0; 
	public double allocatedQuantity = 0;
	public double priorityAllocation  = 0;
	public int holdType;

	/**
	 * Encapsulation of wrxj LoadData
	 */
	private WebLoadData loadData = null;
	private WebLoadLineItemData LLIData = null;

	public loadAndLLIDataModel()
	{

	}

	/**
	 * Construct the outer class with LoadData information.
	 *
	 * @param ld
	 * @throws NoSuchFieldException
	 */
	public WebLoadData loadAndLLIDataModel(LoadData ld) throws NoSuchFieldException
	{
		this.loadId = ld.getLoadID();
		this.parentLoad = ld.getParentLoadID();
		this.mcKey = ld.getMCKey();
		this.containerType = ld.getContainerType();
		this.warehouse = ld.getWarehouse();
		this.address = ld.getAddress();
		this.shelfPosition = ld.getShelfPosition();
		this.nextWarehouse = ld.getNextWarehouse();
		this.nextAddress = ld.getNextAddress();
		this.nextShelfPosition = ld.getNextShelfPosition();
		this.finalWarehouse = ld.getFinalWarehouse();
		this.finalAddress = ld.getFinalAddress();
		this.routeId = ld.getRouteID();
		this.recZone = ld.getRecommendedZone();
		this.message = ld.getLoadMessage();
		this.deviceId = ld.getDeviceID();
		this.height = ld.getHeight();
		this.weight = ld.getWeight();
		this.amountFull = ld.getAmountFull();
		this.barcode = ld.getBCRData();
		this.moveStatus = DBTrans.getStringValue(LoadData.LOADMOVESTATUS_NAME, ld.getLoadMoveStatus());
		this.lpCheck = DBTrans.getStringValue(LoadData.LOADPRESENCECHECK_NAME, ld.getLoadPresenceCheck());
		this.loadDate = ld.getMoveDate();
		this.currentAddress = ld.getCurrentAddress();
		return loadData;
	}
	
	public WebLoadLineItemData LoadLineItemDataModel(LoadLineItemData ld) throws NoSuchFieldException
	{
		LLIData.setLot(ld.getLot());
		this.positionId=ld.getPositionID(); 
		this.holdReasone=ld.getHoldReason(); 
		this.orderId=ld.getOrderID(); 
		this.expectedReceipt=ld.getExpectedReceipt(); 
		this.orderLot=ld.getOrderLot(); 
		this.globalId=ld.getGlobalID();
		this.lineId=ld.getLineID(); 
		this.lastCCIDate =ld.getLastCCIDate(); 
		this.agingDate = ld.getAgingDate(); 
		this.expirationDate = ld.getExpirationDate(); 
		this.expectedDate = ld.getExpectedDate(); 
		this.currentQuantity = ld.getCurrentQuantity(); 
		this.allocatedQuantity = ld.getAllocatedQuantity();
		this.priorityAllocation  = ld.getPriorityAllocation();
		this.holdType=ld.getHoldType();
		return LLIData;
	}

	/**
	 * Inner class for encapsulating a LoadData Object with a direct-access constructor.
	 *
	 * May be a better way to extend these classes but currently am stuck at formatting a json
	 * from gson or jackson in a way that's easy to work with the JSON variable naming that results
	 * from just using a vanilla wrxj object. Since I don't want to mess around with base code at all
	 * this may seems like a viable solution?
	 *
	 *
	 * Author: dystout
	 * Created : May 5, 2017
	 *
	 */
	protected class WebLoadData extends LoadData
	{
		/**
		 * Additional constructor to LoadData for direct access to class variables.
		 *
		 * @param loadAndLLIDataModel
		 * @throws NoSuchFieldException
		 */
		public WebLoadData WebLoadData(loadAndLLIDataModel loadAndLLIDataModel) throws NoSuchFieldException
		{
			loadData.clear();
			loadData.setLoadID(loadAndLLIDataModel.getLoadId());
			System.out.println("load id:"+loadData.getLoadID());
			loadData.setParentLoadID(loadAndLLIDataModel.getLoadId());
			System.out.println("load id:"+loadData.getParentLoadID());	
			//this.setMCKey(ldm.getMcKey());
			loadData.setContainerType(loadAndLLIDataModel.getContainerType());
			loadData.setWarehouse(loadAndLLIDataModel.getWarehouse());
			loadData.setAddress(loadAndLLIDataModel.getAddress());
			loadData.setShelfPosition(loadAndLLIDataModel.getShelfPosition());
			loadData.setNextWarehouse(loadAndLLIDataModel.getNextWarehouse());
			loadData.setNextAddress(loadAndLLIDataModel.getNextAddress());
			loadData.setNextShelfPosition(loadAndLLIDataModel.getNextShelfPosition());
			loadData.setFinalWarehouse(loadAndLLIDataModel.getFinalWarehouse());
			loadData.setFinalAddress(loadAndLLIDataModel.getFinalAddress());
			loadData.setRouteID(loadAndLLIDataModel.getRouteId());
			loadData.setRecommendedZone(loadAndLLIDataModel.getRecZone());
			loadData.setLoadMessage(loadAndLLIDataModel.getMessage());
			loadData.setDeviceID(loadAndLLIDataModel.getDeviceId());
			loadData.setLoadMoveStatus(DBTrans.getIntegerValue(LoadDataAndLLIData.LOADMOVESTATUS_NAME, loadAndLLIDataModel.getMoveStatus()));
			loadData.setWeight(loadAndLLIDataModel.getWeight());
			loadData.setHeight(loadAndLLIDataModel.getHeight());
			//this.setAmountFull(DBTrans.getIntegerValue(LoadDataAndLLIData.AMOUNTFULL_NAME, ldm.getsAmountFull()));
			loadData.setBCRData(loadAndLLIDataModel.getBarcode());
			//this.setLoadPresenceCheck(DBTrans.getIntegerValue(LoadDataAndLLIData.LOADPRESENCECHECK_NAME, ldm.getLpCheck()));
			loadData.setMoveDate(loadAndLLIDataModel.getLoadDate());
			loadData.setCurrentAddress(loadAndLLIDataModel.getCurrentAddress());
			return loadData;
		}

	}
	
	
	protected class WebLoadLineItemData extends LoadLineItemData
	{
		public WebLoadLineItemData WebLoadLineItemData(loadAndLLIDataModel loadAndLLIDataModel) throws NoSuchFieldException
		{
			LLIData.clear();
			LLIData.setLot(loadAndLLIDataModel.getLot());
			LLIData.setExpectedReceipt(loadAndLLIDataModel.getExpectedReceipt());;
			LLIData.setOrderLot(loadAndLLIDataModel.getOrderLot());
			LLIData.setLastCCIDate(loadAndLLIDataModel.getLastCCIDate());
			LLIData.setAgingDate(loadAndLLIDataModel.getAgingDate());
			LLIData.setExpirationDate(loadAndLLIDataModel.getExpirationDate());
			LLIData.setExpectedDate(loadAndLLIDataModel.getExpectedDate());
			LLIData.setCurrentQuantity(loadAndLLIDataModel.getCurrentQuantity());
			LLIData.setAllocatedQuantity(loadAndLLIDataModel.getAllocatedQuantity());
			LLIData.setPriorityAllocation(loadAndLLIDataModel.getPriorityAllocation());
			LLIData.setHoldType(loadAndLLIDataModel.getHoldType());
			return LLIData;
		}

	}

	public String getLoadId()
	{
		return loadId;
	}
	public void setLoadId(String loadId)
	{
		this.loadId = loadId;
	}

	public String getMcKey() {
		return mcKey;
	}

	public void setMcKey(String mcKey) {
		this.mcKey = mcKey;
	}

	public String getsAmountFull() {
		return sAmountFull;
	}

	public void setsAmountFull(String sAmountFull) {
		this.sAmountFull = sAmountFull;
	}

	public String getWarehouse()
	{
		return warehouse;
	}
	public void setWarehouse(String warehouse)
	{
		this.warehouse = warehouse;
	}
	public String getAddress()
	{
		return address;
	}
	public void setAddress(String address)
	{
		this.address = address;
	}
	public String getShelfPosition()
	{
		return shelfPosition;
	}
	public void setShelfPosition(String shelfPosition)
	{
		this.shelfPosition = shelfPosition;
	}
	public String getRouteId()
	{
		return routeId;
	}
	public void setRouteId(String routeId)
	{
		this.routeId = routeId;
	}
	public String getDeviceId()
	{
		return deviceId;
	}
	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}
	public String getNextWarehouse()
	{
		return nextWarehouse;
	}
	public void setNextWarehouse(String nextWarehouse)
	{
		this.nextWarehouse = nextWarehouse;
	}
	public String getNextAddress()
	{
		return nextAddress;
	}
	public void setNextAddress(String nextAddress)
	{
		this.nextAddress = nextAddress;
	}
	public String getNextShelfPosition()
	{
		return nextShelfPosition;
	}
	public void setNextShelfPosition(String nextShelfPosition)
	{
		this.nextShelfPosition = nextShelfPosition;
	}
	public String getFinalAddress()
	{
		return finalAddress;
	}
	public void setFinalAddress(String finalAddress)
	{
		this.finalAddress = finalAddress;
	}
	public String getParentLoad()
	{
		return parentLoad;
	}
	public void setParentLoad(String parentLoad)
	{
		this.parentLoad = parentLoad;
	}
	public Date getLoadDate()
	{
		return loadDate;
	}
	public void setLoadDate(Date loadDate)
	{
		this.loadDate = loadDate;
	}
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public int getHeight()
	{
		return height;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}
	public String getZone()
	{
		return recZone;
	}
	public void setZone(String zone)
	{
		this.recZone = zone;
	}
	public Integer getAmountFull()
	{
		return amountFull;
	}
	public void setAmountFull(Integer amountFull)
	{
		this.amountFull = amountFull;
		try
		{
			this.sAmountFull = DBTrans.getStringValue(LoadData.AMOUNTFULL_NAME, amountFull);
		} catch (NoSuchFieldException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * When we have to set string <-> integer values for the amount full descriptions.
	 * @param amountFull
	 */
	public void setSAmountFull(String amountFull)
	{
		Integer amtFullTrans = null;
		try
		{
			amtFullTrans = DBTrans.getIntegerValue(LoadData.AMOUNTFULL_NAME, amountFull);
		} catch (NoSuchFieldException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.amountFull = amtFullTrans;
	}
	public String getSAmountFull()
	{
		return sAmountFull;
	}
	public double getWeight()
	{
		return weight;
	}
	public void setWeight(Double weight)
	{
		this.weight = weight;
	}
	public String getContainerType()
	{
		return containerType;
	}
	public void setContainerType(String containerType)
	{
		this.containerType = containerType;
	}
	public String getFinalWarehouse()
	{
		return finalWarehouse;
	}
	public void setFinalWarehouse(String finalWarehouse)
	{
		this.finalWarehouse = finalWarehouse;
	}
	public String getRecZone()
	{
		return recZone;
	}
	public void setRecZone(String recZone)
	{
		this.recZone = recZone;
	}
	public String getBarcode()
	{
		return barcode;
	}
	public void setBarcode(String barcode)
	{
		this.barcode = barcode;
	}
	public String getMoveStatus()
	{
		return moveStatus;
	}
	public void setMoveStatus(String moveStatus)
	{
		this.moveStatus = moveStatus;
	}
	public String getLpCheck()
	{
		return lpCheck;
	}
	public void setLpCheck(String lpCheck)
	{
		this.lpCheck = lpCheck;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	
	public String getCurrentAddress() {
		return currentAddress;
	}

	public void setCurrentAddress(String currentAddress) {
		this.currentAddress = currentAddress;
	}
	
	

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public String getPositionId() {
		return positionId;
	}

	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}

	public String getHoldReasone() {
		return holdReasone;
	}

	public void setHoldReasone(String holdReasone) {
		this.holdReasone = holdReasone;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getExpectedReceipt() {
		return expectedReceipt;
	}

	public void setExpectedReceipt(String expectedReceipt) {
		this.expectedReceipt = expectedReceipt;
	}

	public String getOrderLot() {
		return orderLot;
	}

	public void setOrderLot(String orderLot) {
		this.orderLot = orderLot;
	}

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}

	public Date getLastCCIDate() {
		return lastCCIDate;
	}

	public void setLastCCIDate(Date lastCCIDate) {
		this.lastCCIDate = lastCCIDate;
	}

	public Date getAgingDate() {
		return agingDate;
	}

	public void setAgingDate(Date agingDate) {
		this.agingDate = agingDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Date getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(Date expectedDate) {
		this.expectedDate = expectedDate;
	}

	public double getCurrentQuantity() {
		return currentQuantity;
	}

	public void setCurrentQuantity(double currentQuantity) {
		this.currentQuantity = currentQuantity;
	}

	public double getAllocatedQuantity() {
		return allocatedQuantity;
	}

	public void setAllocatedQuantity(double allocatedQuantity) {
		this.allocatedQuantity = allocatedQuantity;
	}

	public double getPriorityAllocation() {
		return priorityAllocation;
	}

	public void setPriorityAllocation(double priorityAllocation) {
		this.priorityAllocation = priorityAllocation;
	}

	public int getHoldType() {
		return holdType;
	}

	public void setHoldType(int holdType) {
		this.holdType = holdType;
	}

	/**
	 * If we dont already have an instance of the encapsulated
	 * load data, construct one using the current state of the
	 * outer class.
	 *
	 * @return
	 * @throws NoSuchFieldException
	 * @throws DBException 
	 */
	public LoadData getLoadData() throws NoSuchFieldException, DBException
	{
		LoadData ld = new LoadData();
		EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> location = vpTJLoadHandler.getlocationBySAddress(this.getAddress());
		
		ld.clear();
		ld.setLoadID(this.getLoadId());
		ld.setParentLoadID(this.getLoadId());	
		//this.setMCKey(ldm.getMcKey());
		ld.setContainerType("1");
		ld.setWarehouse("EBS");
		ld.setAddress(this.getAddress());
		ld.setShelfPosition(this.getShelfPosition());
		ld.setNextWarehouse(this.getNextWarehouse());
		ld.setNextAddress(this.getNextAddress());
		ld.setNextShelfPosition(this.getNextShelfPosition());
		ld.setFinalWarehouse(this.getFinalWarehouse());
		ld.setFinalAddress(this.getFinalAddress());
		ld.setRouteID(this.getRouteId());
		ld.setRecommendedZone(this.getRecZone());
		ld.setLoadMessage(this.getMessage());
		ld.setDeviceID((String) location.get(0).get("SDEVICEID"));
		//ld.setLoadMoveStatus(DBTrans.getIntegerValue(ldAndLLIData.LOADMOVESTATUS_NAME, loadAndLLIDataModel.getMoveStatus()));
		ld.setWeight(this.getWeight());
		//ld.setHeight(this.getHeight());
		//this.setAmountFull(DBTrans.getIntegerValue(ldAndLLIData.AMOUNTFULL_NAME, ldm.getsAmountFull()));
		ld.setBCRData(this.getBarcode());
		//this.setLoadPresenceCheck(DBTrans.getIntegerValue(ldAndLLIData.LOADPRESENCECHECK_NAME, ldm.getLpCheck()));
		ld.setMoveDate(this.getLoadDate());
		ld.setCurrentAddress(this.getAddress());
		return ld;
	}
	public LoadLineItemData getLoadLineItemData() throws NoSuchFieldException
	{
		Date date = new Date();  
        Timestamp ts=new Timestamp(date.getTime());
        System.out.println(ts);
        
		LoadLineItemData lineItemData=new LoadLineItemData();
		lineItemData.clear();
		//lineItemData.setLot(this.getLot());
		//lineItemData.setExpectedReceipt(this.getExpectedReceipt());;
		//lineItemData.setOrderLot(this.getOrderLot());
		//lineItemData.setLastCCIDate(new Date());
		//lineItemData.setAgingDate(new Date());
		//lineItemData.setItem("Bag_On_Tray");
		//lineItemData.setExpirationDate(new Date());
		
		//lineItemData.setGlobalID(this.getGlobalId());
		//lineItemData.setExpectedDate(ts);
		//lineItemData.setCurrentQuantity(6);
		//lineItemData.setAllocatedQuantity(5.000);
		//lineItemData.setPriorityAllocation(5);
		//lineItemData.setHoldType(168);
		return lineItemData;
	}

}