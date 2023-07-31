package com.daifukuoc.wrxj.custom.ebs.scheduler.plc;

import java.util.HashMap;
import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.scheduler.agc.AGCScheduler;
import com.daifukuoc.wrxj.custom.ebs.plc.PLCDevice;

/**
 * The Scheduler is the object that schedules all load movement in the WRXJ. 
 * @author KR
 *
 */
public class EBSPLCScheduler extends AGCScheduler
{
	protected PLCDevice mpPLCDevice = Factory.create(PLCDevice.class);
	protected HashMap<String, String> mapStationToFlush = new HashMap<String, String>();// Creating HashMap
	/**
	   * Public constructor for Factory
	   *
	   * @param isName
	   */
	  public EBSPLCScheduler(String isName)
	  {
	    super(isName);
	  }

	@Override
	protected void processStationEvent(String receivedString) {
		// TODO Auto-generated method stub
		System.out.print("KR processStationEvent:"+ receivedString);
	}

	/**
	 * Handles the Load Event
	 */
	@Override
	protected void processLoadEvent(String receivedString) {
		// TODO Auto-generated method stub
		System.out.print("KR processLoadEvent:"+ receivedString);
	}

	/**
	 * Handles the Schedule Event call
	 */
	@Override
	protected void processSchedulerEvent(String receivedString) {
		// TODO Auto-generated method stub
		System.out.print("KR processSchedulerEvent:"+ receivedString);
		
		AllocationMessageDataFormat allocData = new AllocationMessageDataFormat();
		allocData.decodeReceivedString(receivedString);
		String to = allocData.getOutputStation();
		String from = allocData.getFromAddress();
		String w = allocData.getFromWarehouse();
		String data =  allocData.getDataString();
		
		String load =Integer.toString( allocData.getMessageType());
		
		
		String vsDevice = mpLoadServer.getLoadDeviceID(load);
		//getLoadDeviceID();
		// Send the PLC flush Messages
		mpPLCDevice.sendFlushMessagetoPlc(vsDevice,to);	
	}
	
	
}
