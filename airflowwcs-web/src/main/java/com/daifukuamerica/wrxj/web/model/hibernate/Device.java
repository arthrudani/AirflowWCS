package com.daifukuamerica.wrxj.web.model.hibernate;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="DEVICE")
public class Device
{
	
	public Device()
	{
		//for hibernate
	}
	
	@Id
	@Column(name="SDEVICEID")
	private String id; 
	
	@Column(name="IDEVICETYPE")
	private Integer deviceType; 
	
	@Column(name="IAISLEGROUP")
	private Integer aisleGroup;
	
	@Column(name="SCOMMDEVICE")
	private String commDevice; 
	
	@Column(name="IOPERATIONALSTATUS")
	private Integer operationalStatus; 

	@Column(name="IPHYSICALSTATUS")
	private String physicalStatus; 

	@Column(name="IEMULATIONMODE")
	private Integer emulationMode; 

	@Column(name="SCOMMSENDPORT")
	private String commSendPort; 

	@Column(name="SCOMMREADPORT")
	private String commReadPort; 

	@Column(name="SERRORCODE")
	private String errorCode; 

	@Column(name="SNEXTDEVICE")
	private String nextDevice; 
	
	@Column(name="IDEVICETOKEN")
	private Integer deviceToken; 

	@Column(name="SSCHEDULERNAME")
	private String schedulerName; 

	@Column(name="SALLOCATORNAME")
	private String allocatorName; 
	
	@Column(name="SSTATIONNAME")
	private String stationName; 
	
	@Column(name="SUSERID")
	private String userId; 

	@Column(name="SPRINTER")
	private String printer; 

	@Column(name="SWAREHOUSE")
	private String warehouse; 

	@Column(name="DMODIFYTIME")
	private Date modifyTime; 

	@Column(name="SADDMETHOD")
	private String addMethod; 

	@Column(name="SUPDATEMETHOD")
	private String updateMethod; 

	@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SJVMIDENTIFIER")
	private JvmConfig jvmConfig; 

	
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Integer getDeviceType()
	{
		return deviceType;
	}

	public void setDeviceType(Integer deviceType)
	{
		this.deviceType = deviceType;
	}

	public Integer getAisleGroup()
	{
		return aisleGroup;
	}

	public void setAisleGroup(Integer aisleGroup)
	{
		this.aisleGroup = aisleGroup;
	}

	public String getCommDevice()
	{
		return commDevice;
	}

	public void setCommDevice(String commDevice)
	{
		this.commDevice = commDevice;
	}

	public Integer getOperationalStatus()
	{
		return operationalStatus;
	}

	public void setOperationalStatus(Integer operationalStatus)
	{
		this.operationalStatus = operationalStatus;
	}

	public String getPhysicalStatus()
	{
		return physicalStatus;
	}

	public void setPhysicalStatus(String physicalStatus)
	{
		this.physicalStatus = physicalStatus;
	}

	public Integer getEmulationMode()
	{
		return emulationMode;
	}

	public void setEmulationMode(Integer emulationMode)
	{
		this.emulationMode = emulationMode;
	}

	public String getCommSendPort()
	{
		return commSendPort;
	}

	public void setCommSendPort(String commSendPort)
	{
		this.commSendPort = commSendPort;
	}

	public String getCommReadPort()
	{
		return commReadPort;
	}

	public void setCommReadPort(String commReadPort)
	{
		this.commReadPort = commReadPort;
	}

	public String getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(String errorCode)
	{
		this.errorCode = errorCode;
	}

	public String getNextDevice()
	{
		return nextDevice;
	}

	public void setNextDevice(String nextDevice)
	{
		this.nextDevice = nextDevice;
	}

	public Integer getDeviceToken()
	{
		return deviceToken;
	}

	public void setDeviceToken(Integer deviceToken)
	{
		this.deviceToken = deviceToken;
	}

	public String getSchedulerName()
	{
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName)
	{
		this.schedulerName = schedulerName;
	}

	public String getAllocatorName()
	{
		return allocatorName;
	}

	public void setAllocatorName(String allocatorName)
	{
		this.allocatorName = allocatorName;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getPrinter()
	{
		return printer;
	}

	public void setPrinter(String printer)
	{
		this.printer = printer;
	}

	public String getWarehouse()
	{
		return warehouse;
	}

	public void setWarehouse(String warehouse)
	{
		this.warehouse = warehouse;
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

	public JvmConfig getJvmConfig()
	{
		return jvmConfig;
	}

	public void setJvmConfig(JvmConfig jvmConfig)
	{
		this.jvmConfig = jvmConfig;
	}



}
