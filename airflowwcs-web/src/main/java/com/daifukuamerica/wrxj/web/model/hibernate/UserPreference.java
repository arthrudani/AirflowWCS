package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity(name="UserPreference")
@Table(name="WEBUSERPREF")
public class UserPreference
{
	
	public UserPreference()
	{
		// for hibernate
	}
	
	public UserPreference(String userId, String prefKey, String prefValue)
	{
		this.userId = userId;
		this.prefKey = prefKey; 
		this.prefValue = prefValue;
	}
	public UserPreference(String userId, String prefKey, String prefValue, String prefDesc)
	{
		this.userId = userId;
		this.prefKey = prefKey; 
		this.prefValue = prefValue;
		this.prefDesc = prefDesc; 
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name="ID")
	private Integer id; 
	
	@Column(name="USER_ID")
	private String userId; 
	
	@Column(name="PREF_KEY")
	private String prefKey; 
	
	@Column(name="PREF_VALUE")
	private String prefValue;
	
	@Column(name="PREF_DESC")
	private String prefDesc;



	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getPrefKey()
	{
		return prefKey;
	}

	public void setPrefKey(String prefKey)
	{
		this.prefKey = prefKey;
	}

	public String getPrefValue()
	{
		return prefValue;
	}

	public void setPrefValue(String prefValue)
	{
		this.prefValue = prefValue;
	}

	public String getPrefDesc()
	{
		return prefDesc;
	}

	public void setPrefDesc(String prefDesc)
	{
		this.prefDesc = prefDesc;
	} 
	
	

}
