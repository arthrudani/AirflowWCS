/**
 *
 */
package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: dystout
 * Created : Apr 8, 2017
 *
 * Gson encapsulation of data for the load modify screen.
 *
 * can construct from LoadData object
 *
 */
public class LoadDataModifyModel
{

	/**
	* Log4j logger: GsonLoadData
	*/
	private static final Logger logger = LoggerFactory.getLogger(LoadDataModifyModel.class);

	@SerializedName("loadIdMod")
	private String loadId;
	@SerializedName("containerTypeOption")
	private String container;
	@SerializedName("locWarehouseOption")
	private String locWarehouse;
	@SerializedName("locAddressMod")
	private String locAddress;
	@SerializedName("locShelfPositionMod")
	private String locShelfPosition;
	@SerializedName("nextWarehouseOption")
	private String nextWarehouse;
	@SerializedName("nextAddressMod")
	private String nextAddress;
	@SerializedName("nextShelfPositionMod")
	private String nextShelfPosition;
	@SerializedName("finalWarehouseOption")
	private String finalWarehouse;
	@SerializedName("finalAddressMod")
	private String finalAddress;
	@SerializedName("routeOption")
	private String route;
	@SerializedName("weightMod")
	private Double weight;
	@SerializedName("heightOption")
	private Integer height;
	@SerializedName("moveStatusOption")
	private String moveStatus;
	@SerializedName("messageMod")
	private String message;
	@SerializedName("barcodeMod")
	private String barCode;
	@SerializedName("amountFullOption")
	private String amountFull;
	@SerializedName("lpCheckOption")
	private String lpCheck;
	@SerializedName("deviceOption")
	private String deviceId;
	@SerializedName("recZoneOption")
	private String recZone;

	public LoadDataModifyModel()
	{

	}

	public LoadDataModifyModel(LoadData loadData)
	{
		this.loadId = loadData.getLoadID();
		this.container = loadData.getContainerType();
		this.locWarehouse = loadData.getWarehouse();
		this.locAddress = loadData.getAddress();
		this.locShelfPosition = loadData.getShelfPosition();
		this.nextWarehouse = loadData.getNextWarehouse();
		this.nextAddress = loadData.getNextAddress();
		this.nextShelfPosition = loadData.getNextShelfPosition();
		this.finalWarehouse = loadData.getFinalWarehouse();
		this.finalAddress = loadData.getFinalAddress();
		this.route = loadData.getRouteID();
		this.weight = loadData.getWeight();
		this.height = loadData.getHeight();
		try
		{
			this.amountFull = DBTrans.getStringValue(LoadData.AMOUNTFULL_NAME, loadData.getAmountFull());
			this.moveStatus = DBTrans.getStringValue(LoadData.LOADMOVESTATUS_NAME, loadData.getLoadMoveStatus());
			this.lpCheck = DBTrans.getStringValue(LoadData.LOADPRESENCECHECK_NAME, loadData.getLoadPresenceCheck());

		} catch (NoSuchFieldException e)
		{
			logger.error("Error Formatting JSON for Load Data: {}", e.getMessage());
			e.printStackTrace();
		}
		this.deviceId = loadData.getDeviceID();
		this.recZone = loadData.getRecommendedZone();
		this.message = loadData.getLoadMessage();
		this.barCode = loadData.getBCRData();

	}

	public String getLoadId()
	{
		return loadId;
	}

	public void setLoadId(String loadId)
	{
		this.loadId = loadId;
	}

	public String getContainer()
	{
		return container;
	}

	public void setContainer(String container)
	{
		this.container = container;
	}

	public String getLocWarehouse()
	{
		return locWarehouse;
	}

	public void setLocWarehouse(String locWarehouse)
	{
		this.locWarehouse = locWarehouse;
	}

	public String getNextWarehouse()
	{
		return nextWarehouse;
	}

	public String getLocAddress()
	{
		return locAddress;
	}

	public void setLocAddress(String locAddress)
	{
		this.locAddress = locAddress;
	}

	public String getLocShelfPosition()
	{
		return locShelfPosition;
	}

	public void setLocShelfPosition(String locShelfPosition)
	{
		this.locShelfPosition = locShelfPosition;
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

	public String getFinalWarehouse()
	{
		return finalWarehouse;
	}

	public void setFinalWarehouse(String finalWarehouse)
	{
		this.finalWarehouse = finalWarehouse;
	}

	public String getFinalAddress()
	{
		return finalAddress;
	}

	public void setFinalAddress(String finalAddress)
	{
		this.finalAddress = finalAddress;
	}

	public String getRoute()
	{
		return route;
	}

	public void setRoute(String route)
	{
		this.route = route;
	}

	public Double getWeight()
	{
		return weight;
	}

	public void setWeight(Double weight)
	{
		this.weight = weight;
	}

	public Integer getHeight()
	{
		return height;
	}

	public void setHeight(Integer height)
	{
		this.height = height;
	}






	public String getMoveStatus()
	{
		return moveStatus;
	}

	public void setMoveStatus(String moveStatus)
	{
		this.moveStatus = moveStatus;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getBarCode()
	{
		return barCode;
	}

	public void setBarCode(String barCode)
	{
		this.barCode = barCode;
	}

	public String getAmountFull()
	{
		return amountFull;
	}

	public void setAmountFull(String amountFull)
	{
		this.amountFull = amountFull;
	}

	public String getLpCheck()
	{
		return lpCheck;
	}

	public void setLpCheck(String lpCheck)
	{
		this.lpCheck = lpCheck;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getRecZone()
	{
		return recZone;
	}

	public void setRecZone(String recZone)
	{
		this.recZone = recZone;
	}


}