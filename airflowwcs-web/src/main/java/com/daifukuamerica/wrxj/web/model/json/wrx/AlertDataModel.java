package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.AlertData;

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

public class AlertDataModel
{


	/**
	 * When converting this object to JSON using GSON the variable
	 * names will be formatted to reflect exactly the variable names
	 * defined in the class.
	 *
	 * TODO JSR 303 Validation for form fields.
	 */

	private String alertId;
	private Date timeStamp;
	private String eventCode;
	private String description;
	private String activeFlag;

	/**
	 * Encapsulation of wrxj LoadData
	 */
	private WebAlertData alertData = null;

	public AlertDataModel()
	{

	}

	/**
	 * Construct the outer class with AlertData information.
	 *
	 * @param ld
	 * @throws NoSuchFieldException
	 */
	public AlertDataModel(AlertData ad) throws NoSuchFieldException
	{	
		this.alertId = ad.getAlertId();
		this.timeStamp = ad.getTimeStamp();
		this.eventCode = ad.getEventCode();
		this.description = ad.getDescription();
		this.activeFlag = ad.getActiveFlag();
	}

	/**
	 * Inner class for encapsulating a AlertData Object with a direct-access constructor.
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
	protected class WebAlertData extends AlertData
	{
		/**
		 * Additional constructor to LoadData for direct access to class variables.
		 *
		 * @param ldm
		 * @throws NoSuchFieldException
		 */
		public WebAlertData(AlertDataModel adm) throws NoSuchFieldException
		{
			
			this.clear();
			this.setAlertId(adm.getAlertId());
			this.setTimeStamp(adm.getTimeStamp());
			this.setEventCode(adm.getEventCode());
			this.setDescription(adm.getDescription());
			this.setActiveFlag(adm.getActiveFlag());
		}

	}

	public String getAlertId()
	{
		return alertId;
	}
	
	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}

	public Date getTimeStamp()
	{
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public String getEventCode()
	{
		return eventCode;
	}
	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}
	
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	
	public String getActiveFlag()
	{
		return activeFlag;
	}
	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}
	
	

	/**
	 * If we dont already have an instance of the encapsulated
	 * load data, construct one using the current state of the
	 * outer class.
	 *
	 * @return
	 * @throws NoSuchFieldException
	 */
	public WebAlertData getLoadData() throws NoSuchFieldException
	{
		WebAlertData wad = null;
		if(this.alertData==null)
		{
			wad = new WebAlertData(this);
		}else{
			wad = this.alertData;
		}
		return wad;
	}

	public void setAlertData(WebAlertData wad)
	{
		this.alertData = wad;
	}


}