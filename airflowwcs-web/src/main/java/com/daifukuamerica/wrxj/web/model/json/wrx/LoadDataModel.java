package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;

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

public class LoadDataModel
{


	/**
	 * When converting this object to JSON using GSON the variable
	 * names will be formatted to reflect exactly the variable names
	 * defined in the class.
	 *
	 * TODO JSR 303 Validation for form fields.
	 */

	private String loadId;
	private String parentLoad;
	private String mcKey;
	private String containerType;
	private String warehouse;
	private String address;
	private String shelfPosition;
	private String nextWarehouse;
	private String nextAddress;
	private String nextShelfPosition;
	private String finalWarehouse;
	private String finalAddress;
	private String routeId;
	private String recZone;
	private String moveStatus;
	private String message;
	private String deviceId;
	private Date loadDate;
	private String lpCheck;
	private Integer height;
	private Integer amountFull;
	private String sAmountFull;
	private String barcode;
	private Double weight;
	private String item;
	private String currentAddress;

	/**
	 * Encapsulation of wrxj LoadData
	 */
	private WebLoadData loadData = null;

	public LoadDataModel()
	{

	}

	/**
	 * Construct the outer class with LoadData information.
	 *
	 * @param ld
	 * @throws NoSuchFieldException
	 */
	public LoadDataModel(LoadData ld) throws NoSuchFieldException
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
		 * @param ldm
		 * @throws NoSuchFieldException
		 */
		public WebLoadData(LoadDataModel ldm) throws NoSuchFieldException
		{
			this.clear();
			this.setLoadID(ldm.getLoadId());
			this.setParentLoadID(ldm.getLoadId());
			//this.setMCKey(ldm.getMcKey());
			this.setContainerType(ldm.getContainerType());
			this.setWarehouse(ldm.getWarehouse());
			this.setAddress(ldm.getAddress());
			this.setShelfPosition(ldm.getShelfPosition());
			this.setNextWarehouse(ldm.getNextWarehouse());
			this.setNextAddress(ldm.getNextAddress());
			this.setNextShelfPosition(ldm.getNextShelfPosition());
			this.setFinalWarehouse(ldm.getFinalWarehouse());
			this.setFinalAddress(ldm.getFinalAddress());
			this.setRouteID(ldm.getRouteId());
			this.setRecommendedZone(ldm.getRecZone());
			this.setLoadMessage(ldm.getMessage());
			this.setDeviceID(ldm.getDeviceId());
			this.setLoadMoveStatus(DBTrans.getIntegerValue(LoadData.LOADMOVESTATUS_NAME, ldm.getMoveStatus()));
			this.setWeight(ldm.getWeight());
			this.setHeight(ldm.getHeight());
			this.setAmountFull(DBTrans.getIntegerValue(LoadData.AMOUNTFULL_NAME, ldm.getsAmountFull()));
			this.setBCRData(ldm.getBarcode());
			this.setLoadPresenceCheck(DBTrans.getIntegerValue(LoadData.LOADPRESENCECHECK_NAME, ldm.getLpCheck()));
			this.setMoveDate(ldm.getLoadDate());
			this.setCurrentAddress(ldm.getCurrentAddress());
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
	public Integer getHeight()
	{
		return height;
	}
	public void setHeight(Integer height)
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
	public Double getWeight()
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

	/**
	 * If we dont already have an instance of the encapsulated
	 * load data, construct one using the current state of the
	 * outer class.
	 *
	 * @return
	 * @throws NoSuchFieldException
	 */
	public LoadData getLoadData() throws NoSuchFieldException
	{
		WebLoadData wld = null;
		if(this.loadData==null)
		{
			wld = new WebLoadData(this);
		}else{
			wld = this.loadData;
		}
		return wld;
	}

	public void setLoadData(WebLoadData wld)
	{
		this.loadData = wld;
	}


}