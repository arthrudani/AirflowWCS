package com.daifukuoc.wrxj.custom.ebs.scheduler.event;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.scheduler.event.TimedEventTask;
import com.daifukuoc.wrxj.custom.ebs.dataserver.ACPMoveCommandServer;
import com.daifukuoc.wrxj.custom.ebs.plc.commandallocation.ACPCommandAllocator;
import com.daifukuoc.wrxj.custom.ebs.plc.commandallocation.CommandAllocator;
import com.daifukuoc.wrxj.custom.ebs.plc.commandallocation.StandardCommandAllocator;


public class CmdAllocationTask extends TimedEventTask {

	StandardLocationServer mpStandardLocationServer = null ;
	ACPMoveCommandServer mpACPMoveCommandServer = null;
	public CmdAllocationTask(String isName) {
		super(isName);		
	}

	@Override
	public String initTask() {

		int vnSecs = getConfigValue(INTERVAL);
		msIntervalString = vnSecs + " seconds ";
		mnInterval = vnSecs * 1000;
		if (vnSecs < 1)
			return "INVALID CmdAllocationTask interval - " + vnSecs + " CmdAllocationTask will not be started.";

		return null;
	}

	@Override
	public void run() {
		mpLogger.logDebug("CmdAllocationTask- run() start");
		
		//Load all the devices from the device table (Only Id ie: 9001)
		//For each device create a thread to handle the Allocation in its own thread 
		String[] sDevices = null;
		setAllocationServer();
		setACPMoveCommandServer();
		try {
			sDevices = mpStandardLocationServer.getDeviceIDList(false);	
			//archive CMDs with status = DELETED ( NOTE: this is going to be archived
			//mpACPMoveCommandServer.archiveCmds();
			
		} catch (DBException e) {
			mpLogger.logError("Failed to load devices. Error:"+ e.getMessage());
		}
		
		if( sDevices != null && sDevices.length > 0)
		{
			mpLogger.logDebug("CmdAllocationTask- run() for "+  sDevices.length  + "  Devices");
			for(String sDevice: sDevices )
			{
				//new Thread(() -> {
				    CommandAllocator  cmdAllocator = Factory.create(StandardCommandAllocator.class,sDevice);
					cmdAllocator.allocateAll();
				//}).start();
			}
		}
		mpLogger.logDebug("CmdAllocationTask- run() End");		
	}

	
	private void setAllocationServer()
	{
		if( mpStandardLocationServer == null)
		{
			mpStandardLocationServer = Factory.create(StandardLocationServer.class);		
		}
	}
	private void setACPMoveCommandServer()
	{
		if( mpACPMoveCommandServer == null)
		{
			mpACPMoveCommandServer = Factory.create(ACPMoveCommandServer.class);		
		}
	}
	
}
