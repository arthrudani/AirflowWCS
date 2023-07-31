package com.daifukuamerica.wrxj.web.model.json.wrx;

import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;

public class DeviceDataModel
{
	private int deviceType; 
	private int aisleGroup; 
	private int operationalStatus; 
	private int physicalStatus; 
	private int emulationMode; 
	private int deviceToken; 
	private String deviceId; 
	private String commDevice; 
	private String commSendPort; 
	private String errorCode; 
	private String jvmIdentifier;
	private String nextDevice; 
	private String schedulerName; 
	private String stationName; 
	private String printer; 
	private String userId; 
	private String warehouse; 
	private String allocatorName; 
	
	public DeviceDataModel()
	{
		//
	}
	
	public DeviceDataModel(DeviceData dd)
	{
		this.deviceType = dd.getDeviceType(); 
		this.aisleGroup = dd.getAisleGroup(); 
		this.operationalStatus = dd.getOperationalStatus(); 
		this.physicalStatus = dd.getPhysicalStatus(); 
		this.emulationMode = dd.getEmulationMode(); 
		this.deviceToken = dd.getDeviceToken(); 
		this.deviceId = dd.getDeviceID(); 
		this.commDevice = dd.getDeviceID(); 
		this.commSendPort = dd.getCommSendPort(); 
		this.errorCode = dd.getErrorCode(); 
		this.jvmIdentifier = dd.getJVMIdentifier(); 
		this.nextDevice = dd.getNextDevice(); 
		this.schedulerName = dd.getSchedulerName(); 
		this.stationName = dd.getStationName(); 
		this.printer = dd.getPrinter(); 
		this.userId = dd.getUserID(); 
		this.warehouse = dd.getWarehouse(); 
		this.allocatorName = dd.getAllocatorName(); 
	}
	
	private WebDeviceData deviceData = null; 
	
	protected class WebDeviceData extends DeviceData
	{
		public WebDeviceData(DeviceDataModel ddm)
		{
			super(); 
			this.setDeviceType(ddm.getDeviceType()); 
			this.setAisleGroup(ddm.getAisleGroup()); 
			this.setOperationalStatus(ddm.getOperationalStatus());
			this.setPhysicalStatus(ddm.getPhysicalStatus()); 
			this.setEmulationMode(ddm.getEmulationMode()); 
			this.setDeviceToken(ddm.getDeviceToken()); 
			this.setDeviceID(ddm.getDeviceId());
			this.setCommDevice(ddm.getCommDevice()); 
			this.setCommSendPort(ddm.getCommSendPort()); 
			this.setErrorCode(ddm.getErrorCode()); 
			this.setJVMIdentifier(ddm.getJvmIdentifier());
			this.setNextDevice(ddm.getNextDevice()); 
			this.setSchedulerName(ddm.getSchedulerName()); 
			this.setStationName(ddm.getStationName()); 
			this.setPrinter(ddm.getPrinter()); 
			this.setUserID(ddm.getUserId());
			this.setWarehouse(ddm.getWarehouse()); 
			this.setAllocatorName(ddm.getAllocatorName()); 
			
		}
	}

	/**
	 * If we dont already have an instance of the 
	 * encapsulated container type data, construct one 
	 * using the current state of the outer class
	 * 
	 * @return DeviceData - the encapsulated device data
	 * @throws NoSuchFieldException
	 */
	public DeviceData getDeviceData() throws NoSuchFieldException
	{
		WebDeviceData wdd = null; 
		if(this.deviceData == null)
		{
			wdd  = new WebDeviceData(this); 
		}else{
			wdd = this.deviceData; 
		}
		return wdd; 
	}

	public void setDeviceData(WebDeviceData deviceData)
	{
		this.deviceData = deviceData;
	}

	public int getDeviceType()
	{
		return deviceType;
	}

	public void setDeviceType(int deviceType)
	{
		this.deviceType = deviceType;
	}

	public int getAisleGroup()
	{
		return aisleGroup;
	}

	public void setAisleGroup(int aisleGroup)
	{
		this.aisleGroup = aisleGroup;
	}

	public int getOperationalStatus()
	{
		return operationalStatus;
	}

	public void setOperationalStatus(int operationalStatus)
	{
		this.operationalStatus = operationalStatus;
	}

	public int getPhysicalStatus()
	{
		return physicalStatus;
	}

	public void setPhysicalStatus(int physicalStatus)
	{
		this.physicalStatus = physicalStatus;
	}

	public int getEmulationMode()
	{
		return emulationMode;
	}

	public void setEmulationMode(int emulationMode)
	{
		this.emulationMode = emulationMode;
	}

	public int getDeviceToken()
	{
		return deviceToken;
	}

	public void setDeviceToken(int deviceToken)
	{
		this.deviceToken = deviceToken;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getCommDevice()
	{
		return commDevice;
	}

	public void setCommDevice(String commDevice)
	{
		this.commDevice = commDevice;
	}

	public String getCommSendPort()
	{
		return commSendPort;
	}

	public void setCommSendPort(String commSendPort)
	{
		this.commSendPort = commSendPort;
	}

	public String getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(String errorCode)
	{
		this.errorCode = errorCode;
	}

	public String getJvmIdentifier()
	{
		return jvmIdentifier;
	}

	public void setJvmIdentifier(String jvmIdentifier)
	{
		this.jvmIdentifier = jvmIdentifier;
	}

	public String getNextDevice()
	{
		return nextDevice;
	}

	public void setNextDevice(String nextDevice)
	{
		this.nextDevice = nextDevice;
	}

	public String getSchedulerName()
	{
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName)
	{
		this.schedulerName = schedulerName;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public String getPrinter()
	{
		return printer;
	}

	public void setPrinter(String printer)
	{
		this.printer = printer;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getWarehouse()
	{
		return warehouse;
	}

	public void setWarehouse(String warehouse)
	{
		this.warehouse = warehouse;
	}

	public String getAllocatorName()
	{
		return allocatorName;
	}

	public void setAllocatorName(String allocatorName)
	{
		this.allocatorName = allocatorName;
	}
}
