package com.daifukuamerica.wrxj.web.model.hibernate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="JVMCONFIG")
public class JvmConfig
{
	
	public JvmConfig()
	{
		//for hibernate
	}
	
	@Id
	@Column(name="SJVMIDENTIFIER")
	private String id; 
	
	@Column(name="IJVMTYPE")
	private Integer jvmType;
	
	@Column(name="IJVMSTATUS")
	private Integer jvmStatus; 
	
	@Column(name="SSERVERNAME")
	private Integer serverName; 
	
	@Column(name="SJMSTOPIC")
	private String jmsTopic; 
	
	@Column(name="DMODIFYTIME")
	private Date modifyTime; 
	
	@Column(name="SADDMETHOD")
	private String addMethod; 
	
	@Column(name="SUPDATEMETHOD")
	private String updateMethod;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Integer getJvmType()
	{
		return jvmType;
	}

	public void setJvmType(Integer jvmType)
	{
		this.jvmType = jvmType;
	}

	public Integer getJvmStatus()
	{
		return jvmStatus;
	}

	public void setJvmStatus(Integer jvmStatus)
	{
		this.jvmStatus = jvmStatus;
	}

	public Integer getServerName()
	{
		return serverName;
	}

	public void setServerName(Integer serverName)
	{
		this.serverName = serverName;
	}

	public String getJmsTopic()
	{
		return jmsTopic;
	}

	public void setJmsTopic(String jmsTopic)
	{
		this.jmsTopic = jmsTopic;
	}

	public Date getModifyTime()
	{
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime)
	{
		this.modifyTime = modifyTime;
	}

	public String getAddMethod()
	{
		return addMethod;
	}

	public void setAddMethod(String addMethod)
	{
		this.addMethod = addMethod;
	}

	public String getUpdateMethod()
	{
		return updateMethod;
	}

	public void setUpdateMethod(String updateMethod)
	{
		this.updateMethod = updateMethod;
	} 
	

	
}
