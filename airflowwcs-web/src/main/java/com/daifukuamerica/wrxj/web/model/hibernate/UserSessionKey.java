package com.daifukuamerica.wrxj.web.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * Since LOGIN table wonderfully does not have an inbuilt PK column we construct
 * one here so we can select 'individual' records in the database using JPA 2.0 @IdClass
 * Author: dystout
 * Created : Jun 2, 2018
 *
 */

public class UserSessionKey implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 13L;

	public UserSessionKey(){
		//noarg
	}
	
	public UserSessionKey(String userId, String machineName, String ipAddress){
		this.userId=userId; 
		this.machineName=machineName; 
		this.ipAddress=ipAddress; 
	}

	private String userId; 
	private String machineName; 
	private String ipAddress; 
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
        if (!(obj instanceof UserSessionKey)) return false;
		if(obj instanceof UserSessionKey){
			UserSessionKey sessionKey =(UserSessionKey)obj;
			return userId.equalsIgnoreCase(sessionKey.userId) 
					&& machineName.equalsIgnoreCase(sessionKey.machineName) 
					&& ipAddress.equalsIgnoreCase(sessionKey.ipAddress);
		}else{
			return false; 
		}
	}
	
	@Override
	public int hashCode(){
		return userId.hashCode()+machineName.hashCode()-ipAddress.hashCode(); 
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	
}
