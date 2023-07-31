package com.daifukuoc.wrxj.custom.ebs.allocator;

import com.daifukuamerica.wrxj.allocator.AllocationController;
import com.daifukuamerica.wrxj.allocator.AllocationStrategy;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;


public class EBSAllocationController extends AllocationController 
{
	

	public EBSAllocationController()
	  {
		super();
		
	  }
	
	
	  /**
	   * Processes Order Event Messages from the Order Server.  Note: It is assumed
	   * that Full-Out orders will be for immediate processing.  The Order Server
	   * should mark all Full-Out load orders as ALLOCATENOW if they aren't marked
	   * that way already.  It is also assumed that for Full-Out orders the
	   * destination station in the order will be the output station for the load
	   * (since they know what aisle the load is on).
	   *
	   * @param orderID no information available
	   * @throws DBException no information available
	   */
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

	/*---------------------------------------------------------------------------
	           Don't do allocation for stations that don't allow it.
	  ---------------------------------------------------------------------------*/
	    if (!destStationIsEmpty(ohdata.getDestinationStation()) &&
	        !mpStationServer.isStationAllocationEnabled(ohdata.getDestinationStation()))
	    {
	      String vsMsg = "Station " + ohdata.getDestinationStation() +
	                     " does not have allocation enabled.  Received request but " +
	                     "no orders allocated...";
	      logger.logDebug(vsMsg);
	      logProbe("AllocationController.processOrderEvent",vsMsg);
	      return;
	    }

	    if (mzSplitSystem && (ohdata.getOrderType() == DBConstants.FULLLOADOUT ||
	        ohdata.getOrderType() == DBConstants.EMPTY_CONTAINER_ORDER))
	    {
	      handleSplitSystemOrder(ohdata);
	    }
	    else
	    {
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

	      if (ordData.getOrderType() == DBConstants.CYCLECOUNT ||
	          ordData.getOrderType() == DBConstants.REPLENISHMENT)
	      {
	        try
	        {
	          AllocationStrategy allocStrategy = getAllocationStrategy("", ordData);
	          execOrderAllocStrategy(allocStrategy, ordData, "");
	        }
	        catch(DBException exc)
	        {
	          logProbe("AllocationController.processOrderEvent", exc.getMessage());
	          logger.logDebug(exc.getMessage() +
	                          " IN:AllocationController.processOrderEvent()");
	        }
	      }
	      else if (sDestinationStation.trim().length() == 0)
	      {
	        try
	        {
	          AllocationStrategy allocStrategy = getAllocationStrategy(sDestinationStation,
	                                                                   ordData);
	          if (allocStrategy != null)
	            execLineAllocStrategy(allocStrategy, ordData);
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
	      else
	      {
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
	    }
	  }
	  
	  /**
	   * Mark all allocated loads as RETRIEVEPENDING and notify Scheduler(s).
	   */
	  @Override
	  synchronized protected void markLoadsRetrievePending()
	  {		
		 
	    if (allocatedLoadList == null || allocatedLoadList.isEmpty())
	    {
	      return;
	    }

	    int loadListLength = allocatedLoadList.size();
	    for(int row = 0; row < loadListLength; row++)
	    {
	      mpIPCAllocData = allocatedLoadList.get(row);

	      // Take this station off the list of hungry stations.
	      if (hungryStations.containsKey(mpIPCAllocData.getOutputStation()))
	      {
	        hungryStations.remove(mpIPCAllocData.getOutputStation());
	      }

	      try
	      {
	        mpAllocServer.changeLoadToRetrievePending(mpIPCAllocData.getOutBoundLoad());	        
	       
	      }
	      catch(DBException e)
	      {
	        logProbe("AllocationController.markLoadsRetrievePending", e.getMessage());
	        logger.logException(e, "AllocationController-->markLoadsRetrievePending");
	      }
	    }	   
	    // *** End for loop ***
	  	//KR: publishing Scheduler Notification to EBSPLCScheduler
	    publishSchedulerNotifications();
	  }
	  
	 
}
