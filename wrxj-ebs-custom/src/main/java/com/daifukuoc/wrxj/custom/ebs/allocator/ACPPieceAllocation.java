package com.daifukuoc.wrxj.custom.ebs.allocator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.allocator.AllocationException;
import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.allocator.PieceAllocation;
import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderAllocationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.ParameterNameConstants;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSAllocationServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSOrderServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerFailureException;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerImpl;

public class ACPPieceAllocation extends PieceAllocation{

	  private EBSHostServer  mpEBSHostServer;
	  private EBSOrderServer  mpEBSOrderServer;
	  private EBSAllocationServer mpEBSAllocationServer;
	public ACPPieceAllocation()
	{
	    super();
	}
	
	/**
	  *  Method does Piece-wise allocation of items.  FIFO rules are followed
	  *  for the item details.
	  *
	  * @return <code>List</code> of AllocationMessageDataFormat objects.
	  * @throws DBException for DB access and modify errors.
	  * @throws AllocationException if the station's device is INOP.
	  */
	  @Override
	  public List<AllocationMessageDataFormat> allocate()
	         throws DBException, AllocationException
     {
          // Reserve the order by marking it ALLOCATING
		  mpOLList = mpAllocServer.itemOrderPreallocation(mpOHD);
		  mpAllocatedDataList.clear();
		
		    TransactionToken vpTranTok = null;
		    try
		    {
		      vpTranTok = mpDBObj.startTransaction();
			/*===========================================================================
			   If we made it to here we can try to allocate everything on the OL list
			   provided order status hasn't changed, or the order hasn't been deleted by
			   Short order configuration.
			  ===========================================================================*/
		      if (mpOrderServer.orderHeaderExists(msOrder) &&
		          mpOrderServer.getOrderStatusValue(msOrder) == DBConstants.ALLOCATING)
		      {
		        processOrderLines();
		       // mpShortOrderProcess.setNextOrderStateWithNotification(msOrder, mpOHD.getDestinationStation());
		      }
		      mpDBObj.commitTransaction(vpTranTok);
		      
		      
		    }
		    catch(ShortOrderAllocationException ae)
		    {
		      mpDBObj.endTransaction(vpTranTok);
		      mpAllocatedDataList.clear();
		      mpShortOrderProcess.auxiliaryOrderHandling(msOrder);
		    }
		    catch(DBException exc)
		    {
		      mpDBObj.endTransaction(vpTranTok);
		                                       // Set Order status back to what it was
		                                       // so that we can retry later.
		      mpAllocServer.revertOrderStatus(msOrder, "", iOriginalOrderStatus);
		      throw exc;
		    }
		    catch(AllocationException mesg)
		    {
		      mpDBObj.endTransaction(vpTranTok);
		      String vsOrdMsg = "Application Error in allocator. Exception of type " +
		                         mesg.getClass().getCanonicalName() + " caught!";
		      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
		      throw mesg;                      // Handle next order status by caller so
		                                       // that we can effectively determine if
		                                       // it's a short order or some other prob.
		    }
		    catch(Exception exc)
		    {
		      mpDBObj.endTransaction(vpTranTok);

		      String vsOrdMsg = "Application Error in allocator. Exception of type " +
		                         exc.getClass().getCanonicalName() + " caught!";
		      mpAllocServer.revertOrderStatus(msOrder, vsOrdMsg, DBConstants.ORERROR);
		      throw new DBException("Order " + msOrder + " Not Allocated...", exc);
		    }
		    finally
		    {
		      mpDBObj.endTransaction(vpTranTok);
		    }
		  
		  
		  return(mpAllocatedDataList);
     }
	 protected void processOrderLines() throws DBException, AllocationException
	 {
		//To make sure it is defined
		 if( mpOLList == null )
		 {
			   return;
		 }
		 for(Map vpOLMap : mpOLList)
		 {
			 String vsLoadLocation = "";
			 mpOLData.dataToSKDCData(vpOLMap);
			 LoadData vpLoadData = mpLoadServer.getLoad(mpOLData.getLoadID());
			 if (vpLoadData == null)
			 {
	             throw new DBException("Critical Error! Load " + mpOLData.getLoadID() +
	                                   " deleted by some other process during allocation!");
			 }
			 RouteManagerImpl routeManager = Factory.create(RouteManagerImpl.class);
	         
	         msFirstOutputStation = msRouteID = "";
	         vsLoadLocation = vpLoadData.getAddress();
	         try 
	         {
	        	 msFirstOutputStation = routeManager.findNextDestination(vpLoadData,vpLoadData.getAddress());
	        	 msRouteID = msFirstOutputStation;	
	        	 
	        	 if (msRouteID != null && msRouteID.trim().length() > 0)
		         {
	        		 if (!inLoadList(vpLoadData.getLoadID(), mpAllocatedDataList))
	        	      {
	        	        AllocationMessageDataFormat allocData = new AllocationMessageDataFormat();
	        	        allocData.setOutBoundLoad(vpLoadData.getLoadID());
	        	        allocData.setFromWarehouse(vpLoadData.getWarehouse());
	        	        allocData.setFromAddress(vpLoadData.getAddress());
	        	        allocData.setOutputStation(msFirstOutputStation);
	        	        allocData.setOrderID(mpOLData.getOrderID());
	        	        allocData.createDataString();

	        	        mpAllocatedDataList.add(allocData);
	        	      } 
		        	   
		         }else
		         {
		             mpOrderServer.setOrderLineShort(mpOLData.getOrderID(), mpOLData.getItem(),
		                                             mpOLData.getOrderLot(), mpOLData.getLineID(),
		                                             DBConstants.YES);
		             String vsErrStr = "Order " + mpOLData.getOrderID() + ", Item " +
		                               mpOLData.getItem() + ", lot '" +
		                               mpOLData.getOrderLot() + "' has oldest product in load " +
		                               mpOLData.getLoadID() + " at location " + vsLoadLocation +
		                             ". Output station not on valid route " + msRouteID;
		             mpLogger.logDebug(vsErrStr);
		             if (mzAllocationDiagnostics)
		               mpProbe.addProbeDetails("PieceAllocation.processOrderLines",
		                                       "No route info. or output station found " +
		                                       "for load " + mpOLData.getLoadID() +
		                                       " and order item " + mpOLData.getItem() +
		                                       ". " + vsErrStr);
		         }
	        	    
	         }catch (RouteManagerFailureException ex) {
	        	 mpLogger.logError("Could not find the next station for " +vsLoadLocation);
	   		 }catch(DBException exc)
	         {                            // Move onto the next Order Line.
		           mpLogger.logError(exc.getMessage());
		           if (mzAllocationDiagnostics)
		             mpProbe.addProbeDetails("PieceAllocation.processOrderLines",
		                                     "Error allocating line OrderID " +
		                                      mpOLData.getOrderID() + " Item " +
		                                      mpOLData.getItem() + ", lot " +
		                                      mpOLData.getOrderLot() + "... " +
		                                      exc.getMessage());
		     }
			 
		 }
	 }
	 protected boolean inLoadList(String loadid, List<AllocationMessageDataFormat> allocatedDataList)
	  {
	    boolean rtn = false;
	    AllocationMessageDataFormat allocList = null;

	    for(int idx = 0; idx < allocatedDataList.size(); idx++)
	    {
	      allocList = allocatedDataList.get(idx);
	      if (loadid.equals(allocList.getOutBoundLoad()))
	      {
	        rtn = true;
	        break;
	      }
	    }
	    return(rtn);
	  }
	  /**
	   *  Method to carry out the allocation.
	   */
	   protected void processOrderLinesOld() throws DBException, AllocationException
	   {
		   //To make sure it is defined
		   if( mpOLList == null )
		   {
			   return;
		   }
	 /*===========================================================================
	        Step through each Order Line and find the Item detail(s) for it.
	   ===========================================================================*/
	     for(Map vpOLMap : mpOLList)
	     {
	       mpOLData.dataToSKDCData(vpOLMap);
	      
	       
	       /* KR
	        double vdNeedQty = mpOLData.getOrderQuantity() - mpOLData.getAllocatedQuantity();

	       if (vdNeedQty == 0)
	       {
	         if (mpOLData.getLineShy() == DBConstants.YES)
	         {
	           mpOrderServer.setOrderLineShort(mpOLData.getOrderID(), mpOLData.getItem(),
	                                           mpOLData.getOrderLot(), mpOLData.getLineID(),
	                                           DBConstants.NO);
	         }
	         continue;
	       }
	        	*/
	       double vdNeedQty = 1;
	       List<Map> vpIDList = null;
	       vpIDList = mpAllocServer.getOldestItemDetails( mpOLData.getLineID(),
	    		   mpOLData.getOrderLot(),
                   vdNeedQty,
                   mpOLData.getLoadID());
	       
	       
	       
	       if (vpIDList == null || vpIDList.isEmpty())
	       {
	         continue;
	       }

	       for(Iterator<Map> vpIter = vpIDList.iterator(); vpIter.hasNext(); ) // && vdNeedQty > 0; )
	       {
	         mpIDData.dataToSKDCData(vpIter.next());
	         Map<String,String> hMap = new HashMap<String,String>();
	        // double vdAvailQty = mpIDData.getCurrentQuantity() - mpIDData.getAllocatedQuantity();
	         LoadLineItemData idTempData = mpIDData.clone();
	         idTempData.clearKeysColumns();

	         String vsLoadLocation = "";
	
	           LoadData  vpLoadData = mpLoadServer.getLoad(idTempData.getLoadID());
	           if (vpLoadData == null)
	             throw new DBException("Critical Error! Load " + idTempData.getLoadID() +
	                                   " deleted by some other process during allocation!");

	           RouteManagerImpl routeManager = Factory.create(RouteManagerImpl.class);
	         
	           msFirstOutputStation = msRouteID = "";
	           vsLoadLocation = vpLoadData.getAddress();

	           try {
	           
	        	   msFirstOutputStation = routeManager.findNextDestination(vpLoadData,vpLoadData.getAddress());
	        	   msRouteID = msFirstOutputStation;
	        	   
	        		   
	           }catch (RouteManagerFailureException ex) {
	        	   mpLogger.logError("Could not find the next station for " +vsLoadLocation);
	   		   }	  
	          

	         try
	         {
	           if (msRouteID != null && msRouteID.trim().length() > 0)
	           {
	        	   //KR: just add to the allocated list
	        	   mpAllocServer.buildReturnData(mpIDData.getLoadID(), msFirstOutputStation,
			                 mpAllocatedDataList);
	        	   
	        	  /* processMove(mpOLData, mpIDData, vdNeedQty);
	        	   
	             if (vdNeedQty >= vdAvailQty)
	             {
	               mpAllocServer.reserveItemDetail(idTempData, vdAvailQty);
	               processMove(mpOLData, mpIDData, vdAvailQty);
	               vdNeedQty -= vdAvailQty;
	             }
	             else
	             {
	               mpAllocServer.reserveItemDetail(idTempData, vdNeedQty);
	               processMove(mpOLData, mpIDData, vdNeedQty);
	               vdNeedQty = 0;
	             }
	             mpAllocServer.updateOrderLine(mpOLData, vdNeedQty);
	             */
	           }
	           else
	           {
	             mpOrderServer.setOrderLineShort(mpOLData.getOrderID(), mpOLData.getItem(),
	                                             mpOLData.getOrderLot(), mpOLData.getLineID(),
	                                             DBConstants.YES);
	             String vsErrStr = "Order " + mpOLData.getOrderID() + ", Item " +
	                               mpOLData.getItem() + ", lot '" +
	                               mpOLData.getOrderLot() + "' has oldest product in load " +
	                              mpIDData.getLoadID() + " at location " + vsLoadLocation +
	                             ". Output station not on valid route " + msRouteID;
	             mpLogger.logDebug(vsErrStr);
	             if (mzAllocationDiagnostics)
	               mpProbe.addProbeDetails("PieceAllocation.processOrderLines",
	                                       "No route info. or output station found " +
	                                       "for load " + mpIDData.getLoadID() +
	                                       " and order item " + mpIDData.getItem() +
	                                       ". " + vsErrStr);
	           }
	         }
	         catch(DBException exc)
	         {                            // Move onto the next Order Line.
	           mpLogger.logError(exc.getMessage());
	           if (mzAllocationDiagnostics)
	             mpProbe.addProbeDetails("PieceAllocation.processOrderLines",
	                                     "Error allocating line OrderID " +
	                                      mpOLData.getOrderID() + " Item " +
	                                      mpOLData.getItem() + ", lot " +
	                                      mpOLData.getOrderLot() + "... " +
	                                      exc.getMessage());
	         }
	       } // *** End LoadLineItem for-loop ***
	     } // **** End OrderLine for-loop ****
	   }
	  
	  protected void initializeEBSHostServer()
	  {
	    if (mpEBSHostServer == null)
	    {
	    	mpEBSHostServer = Factory.create(EBSHostServer.class);
	    }
	  }  
	  
	  protected void initializeEBSOrderServer()
	  {
	    if (mpEBSOrderServer == null)
	    {
	    	mpEBSOrderServer = Factory.create(EBSOrderServer.class);
	    }
	  }  
	  
	  protected void initializeEBSAllocationServer()
	  {
	    if (mpEBSAllocationServer == null)
	    {
	    	mpEBSAllocationServer = Factory.create(EBSAllocationServer.class);
	    }
	  } 
	  @Override	
	  protected void processMove(OrderLineData olData, LoadLineItemData idData,
              double pickQuantity)throws DBException, AllocationException
	  {
		//String vsDevice = mpLoadServer.getLoadDeviceID(idData.getLoadID());
		
		/* KR: to look at 
		if (mpDeviceServer.isStationDeviceInoperable(msFirstOutputStation) ||
				mpDeviceServer.isDeviceInoperable(vsDevice))
		{
		String vsErr = "Station's Device or Load Device is not operational! "  +
		      "Allocation Order '" + olData.getOrderID() + "' Item '" +
		      olData.getItem() + "' and Lot '" + idData.getLot() + "' ";
		throw new AllocationException(vsErr, AllocationException.DEVICE_INOP);
		}
		                        // Use the order line's route if we can.
		String defaultRouteID = msRouteID;
		if (olData.getRouteID().trim().length() != 0) defaultRouteID = olData.getRouteID();
		
		if (mpAllocServer.buildItemMove(olData, idData, pickQuantity, mpOHD,
		                     defaultRouteID, vsDevice) == -1)
		{
			throw new DBException("Item move not generated for order line: Order id. " +
		             olData.getOrderID() + ", item " + olData.getItem() +
		             ", Order lot " + olData.getOrderLot() +
		             ", Line id. " + olData.getLineID());
		}
		*/
		mpAllocServer.buildReturnData(idData.getLoadID(), msFirstOutputStation,
		                 mpAllocatedDataList);
	}
}
