/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.util.SKDCUtility;

/**
 * Encapsulation of LoadData for use in Spring Databinding. Naming of variable
 * names to match <form/> 'path' variable names in the view is required to data
 * bind easily when posting.
 *
 * Eventually, when custom objects are required we can map the database with hibernate
 * and do validation entirely from JSR 303.
 *
 * @author mandrus
 */

public class LocationDataModel
{
	/*
	 * When converting this object to JSON using GSON the variable names will be
	 * formatted to reflect exactly the variable names defined in the class.
	 *
	 * TODO JSR 303 Validation for form fields.
	 */
	private String warehouse;
	private String address;
	private String shelfPosition;
	private String deviceId;
	private String zone;
	private Integer locationStatus;
	private Integer emptyFlag;
	private Integer type;

	public LocationDataModel()
	{
	}

	public LocationDataModel(LocationData ipLocData)
	{
		setWarehouse(ipLocData.getWarehouse());
		setAddress(ipLocData.getAddress());
		setShelfPosition(ipLocData.getShelfPosition());
		setDeviceId(ipLocData.getDeviceID());
		setZone(ipLocData.getZone());
		setLocationStatus(ipLocData.getLocationStatus());
		setEmptyFlag(ipLocData.getEmptyFlag());
		setType(ipLocData.getLocationType());
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
	public String getDeviceId()
	{
		return deviceId;
	}
	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}
	public String getZone()
	{
		return zone;
	}
	public void setZone(String zone)
	{
		this.zone = zone;
	}
	public Integer getLocationStatus()
	{
		return locationStatus;
	}
	public void setLocationStatus(Integer locationStatus)
	{
		this.locationStatus = locationStatus;
	}
	public Integer getEmptyFlag()
	{
		return emptyFlag;
	}
	public void setEmptyFlag(Integer emptyFlag)
	{
		this.emptyFlag = emptyFlag;
	}
	
	
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String describeLocation()
	{
		StringBuilder sb = new StringBuilder(String.valueOf(warehouse)).append("-").append(String.valueOf(address));
		if (SKDCUtility.isNotBlank(shelfPosition))
		{
			sb.append(shelfPosition);
		}
		return sb.toString();
	}
}