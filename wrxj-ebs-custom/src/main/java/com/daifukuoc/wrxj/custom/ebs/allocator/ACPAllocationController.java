package com.daifukuoc.wrxj.custom.ebs.allocator;

import com.daifukuamerica.wrxj.allocator.AllocationController;
import com.daifukuamerica.wrxj.allocator.AllocationException;
import com.daifukuamerica.wrxj.allocator.AllocationStrategy;
import com.daifukuamerica.wrxj.allocator.PieceAllocation;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

public class ACPAllocationController extends AllocationController  {

	public ACPAllocationController()
	{
		super();
	}
	
	protected void processOrderEvent(String orderID) throws DBException
	{
		OrderHeaderData ohdata = mpOrderServer.getOrderHeaderRecord(orderID);
	    int iCurrentOrdStat;

	    if (ohdata == null)                // Something is really wrong if this
	    {                                  // happens!
	      String msg = "Order \"" + orderID + "\" not found on system!";
	      logger.logDebug("AllocationController-->processOrderEvent():" + msg);
	      //throw new DBException(msg);
	      return;  // MCM, return without throwing exception
	    }
	    else
	    {
	      iCurrentOrdStat = ohdata.getOrderStatus();
	    }
	   
		 //if NOT READY
		 if (iCurrentOrdStat != DBConstants.SHORT       &&
		          iCurrentOrdStat != DBConstants.ALLOCATENOW &&
		          iCurrentOrdStat != DBConstants.READY       &&
		          iCurrentOrdStat != DBConstants.HOLD        &&
		          iCurrentOrdStat != DBConstants.REALLOC)
	      {
	        try
	        {
	          String sOrderStat = DBTrans.getStringValue("iOrderStatus", iCurrentOrdStat);
	          String mesg = "Order " + orderID + " is in " + sOrderStat +
	                        " state. Can't retry allocation.";
	          logProbe("AllocationController.processOrderEvent", mesg);
	          logger.logDebug("AllocationController-->processOrderEvent():" + mesg);
	        }
	        catch(NoSuchFieldException e)
	        {
	          throw new DBException("Order " + orderID + " has an invalid status...", e);
	        }
	        return;
	      }
		 
		 ordData = ohdata.clone();
	     String sDestinationStation = ordData.getDestinationStation();

	     try
	        {
	          AllocationStrategy allocStrategy = getAllocationStrategy(sDestinationStation,
	                                                                   ordData);
	          if (allocStrategy != null)
	            execOrderAllocStrategy(allocStrategy, ordData, sDestinationStation);
	        }
	        catch(DBException exc)
	        {
	          if (exc.getCause() == null)
	          {
	            logger.logException(exc, ":::::: Trying to Allocate order " +
	                                ordData.getOrderID() +
	                                " AllocationController.processOrderEvent()");
	            logProbe("AllocationController.processOrderEvent",
	                     "Order allocation exception for order: " +
	                     ordData.getOrderID() + "::" + exc.getMessage());
	          }
	          else
	          {
	            logger.logException(exc, DBException.toString(exc.getCause()) +
	                                ":::::: Trying to Allocate order " + ordData.getOrderID() +
	                                " AllocationController.processOrderEvent()");
	            logProbe("AllocationController.processOrderEvent",
	                     "Order allocation exception for order: " +
	                     ordData.getOrderID() + "::" + exc.getMessage() + exc.getCause());
	          }
	        }
	    

	}

	@Override
	synchronized protected void markLoadsRetrievePending()
	{	
		/* The Load status is changed to Retrieval Pending during processRetrievalOrder in the AisleBasedFlightLoadRetrieverImpl	
			So don't need to do anything here
		 */
	}

	@Override
	public AllocationStrategy getAllocationStrategy(String outputStation,
	      OrderHeaderData ohData) throws DBException
	{
	    AllocationStrategy vpStrategy = null;

	    initializePieceStrategy();
        vpStrategy = mpPieceStrategy;
        
        //Make sure the strategy has a logger
        vpStrategy.setLogger(logger);

        return (vpStrategy);
  }
	@Override
	public void execOrderAllocStrategy(AllocationStrategy allocStrategy,
            OrderHeaderData ipOrderHeaderData,
            String requestingStation) throws DBException
	{
		if (allocatedLoadList != null)
		{                                  // Clear out allocated load list.
			allocatedLoadList.clear();
			allocatedLoadList = null;
		}

		logger.logDebug("IN AllocationController-->execOrderAllocStrategy(): ohdata = " + ipOrderHeaderData.toString());
		allocStrategy.setAllocationOrder(ipOrderHeaderData);
		if (allocProbe != null) allocStrategy.setAllocationProbe(allocProbe);
		
		try
		{
			allocatedLoadList = allocStrategy.allocate();
		                // Mark order scheduled and mark loads
		                // Retrieve Pending.
			
			if( allocatedLoadList.size() > 0 )
			{				
			  	//publishing Scheduler Notification to EBSPLCScheduler
			    publishSchedulerNotifications();
			}
		}
		catch(AllocationException aexc)
		{
			logger.logError(aexc.getMessage());
		}
	}

}
