package com.daifukuoc.wrxj.custom.ebs.plc.commandallocation;

import com.daifukuamerica.wrxj.log.Logger;

/**
 * Base command allocation class
 * @author KR
 *
 */
public abstract class StandardCommandAllocator implements CommandAllocator{

	protected final String CMD_READY ="CMDREADY"; 
	protected final String CMD_WORKING ="CMDWORKING";
	protected final String CMD_DEST_STATION = "DESTSTATION";
	protected String sDeviceID;	
	protected Logger mpLogger = Logger.getLogger();
	
	
	public StandardCommandAllocator(String deviceID)
	{
		sDeviceID = deviceID;
		mpLogger.logDebug("StandardCommandAllocator is instantiated for device: "+ deviceID );
	}
	@Override
	public abstract void allocate(String stationID);

	@Override
	public abstract void allocateAll();

}
