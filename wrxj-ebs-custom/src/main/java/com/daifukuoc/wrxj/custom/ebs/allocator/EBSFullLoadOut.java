package com.daifukuoc.wrxj.custom.ebs.allocator;

import java.util.ArrayList;
import java.util.Map;

import com.daifukuamerica.wrxj.allocator.AllocationException;
import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.allocator.FullLoadOut;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAllocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;

public class EBSFullLoadOut extends FullLoadOut
{

	/**
	  *  Default constructor for Load allocation strategy.
	  */
	  public EBSFullLoadOut()
	  {
	    super();
	  }
	  /* KR: don't  need to override so delete this file
	  @Override
	  protected void processMove() throws DBException, AllocationException
	  {
	    String vsDevice = mpLoadServ.getLoadDeviceID(mpOLData.getLoadID());
	    //MCM, EBS
	    // Don't check for Inoperable Devcie, the secondary crane can always retrieve it
//	    if (mpDeviceServ.isStationDeviceInoperable(msFirstOutputStn) || 
//	        mpDeviceServ.isDeviceInoperable(vsDevice))
//	    {
//	      String ordLoad = "Order '" + msOrder + "' and Load '" + 
//	                       mpOLData.getLoadID() + "'";
//	      throw new AllocationException("Station's Device or Load Device is not " + 
//	                                    "operational! Allocation not performed " +
//	                                    "for " + ordLoad, AllocationException.DEVICE_INOP);
//	    }
	      
	    mpAllocServer.buildLoadMove(mpOLData.getLoadID(), msRouteID, vsDevice, mpOHD);
	    mpAllocServer.buildReturnData(mpOLData.getLoadID(), msFirstOutputStn,
	                                  mpAllocatedDataList);
	  }
	
	 */
}
