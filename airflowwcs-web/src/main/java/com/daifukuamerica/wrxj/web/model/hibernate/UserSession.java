package com.daifukuamerica.wrxj.web.model.hibernate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.gson.annotations.SerializedName;

/**
 * WRX LOGIN which holds active sessions for 
 * users and from where they are originating.
 * 
 * Author: dystout
 * Created : Jun 2, 2018
 *
 */

@Entity
@IdClass(UserSessionKey.class)
@Table(name="V_USER_SESSION")
public class UserSession
{

	@Id
	@Column(name="SUSERID")
	@SerializedName("User Session")
	private String userId; 
	
	@SerializedName("Login Time")
	@Column(name="DLOGINTIME")
	private Date loginTime; 
	
	@Id
	@Column(name="SMACHINENAME")
	@SerializedName("Machine Name")
	private String machineName; 
	
	@Id
	@Column(name="SIPADDRESS")
	@SerializedName("IP")
	private String ipAddress;
	
	@SerializedName("Granted User Access")
	@Column(name="GROUPNAMES")
	private String grantedUserAccess; 

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public Date getLoginTime()
	{
		return loginTime;
	}

	public void setLoginTime(Date loginTime)
	{
		this.loginTime = loginTime;
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

	public void setIpAdress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}

	public String getGrantedUserAccess()
	{
		return grantedUserAccess;
	}

	public void setGrantedUserAccess(String grantedUserAccess)
	{
		this.grantedUserAccess = grantedUserAccess;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	} 
	
	
	

}
