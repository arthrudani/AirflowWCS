package com.daifukuoc.wrxj.custom.ebs.allocator;

import java.util.Arrays;
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

public class EBSPieceAllocation extends PieceAllocation
{

	  private EBSHostServer  mpEBSHostServer;
	  private EBSOrderServer  mpEBSOrderServer;
	  private EBSAllocationServer mpEBSAllocationServer;
	  
	/**
	  *  Default constructor for piece allocation strategy.
	  */
	  public EBSPieceAllocation()
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
	        mpShortOrderProcess.setNextOrderStateWithNotification(msOrder,
	                                                 mpOHD.getDestinationStation());
	      }
	      mpDBObj.commitTransaction(vpTranTok);
	      
	      // MCM, EBS
	      // Send a order response message to host
	      initializeEBSHostServer();
	      mpEBSHostServer.sendOrderResponse(msOrder, 0, "");
	      
	      // MCM, EBS
	      // Delete order if it has no allocation
	      initializeEBSOrderServer();
	      mpEBSOrderServer.checkEBSOrder(msOrder);
	      
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
	
	  
	  
	  /**
	   *  Method to carry out the allocation.
	   */
	   protected void processOrderLines() throws DBException, AllocationException
	   {
	 /*===========================================================================
	        Step through each Order Line and find the Item detail(s) for it.
	   ===========================================================================*/
	     for(Map vpOLMap : mpOLList)
	     {
	       mpOLData.dataToSKDCData(vpOLMap);
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

	       List<Map> vpIDList = null;
	       if( mpOLData.getItem().equals(EBSConstants.EMPTY_TRAY_STACK))
		   {
	    	   initializeEBSAllocationServer();
	    	   vpIDList = mpEBSAllocationServer.getOldestItemDetailsForEmptyTrayStack(mpOLData.getItem(),
                       mpOLData.getOrderLot(),
                       vdNeedQty);
		   }
	       else
	       {
	           vpIDList = mpAllocServer.getOldestItemDetails(mpOLData.getItem(),
	                                                               mpOLData.getOrderLot(),
	                                                               vdNeedQty);
	       }
	       
	       if (vpIDList == null || vpIDList.isEmpty())
	       {
	         continue;
	       }

	       for(Iterator<Map> vpIter = vpIDList.iterator(); vpIter.hasNext() && vdNeedQty > 0; )
	       {
	         mpIDData.dataToSKDCData(vpIter.next());
	         Map<String,String> hMap = new HashMap<String,String>();
	         double vdAvailQty = mpIDData.getCurrentQuantity() - mpIDData.getAllocatedQuantity();
	         LoadLineItemData idTempData = mpIDData.clone();
	         idTempData.clearKeysColumns();

	         String vsLoadLocation = "";
	         if (mpLoadServer.isLoadInRack(mpIDData.getLoadID()))
	         {
	           if (!mpAllocServer.isLoadAllocatable(mpIDData.getLoadID(),
	                                                mpOutStation.getStationName(), true))
	           {
	             String vsInfo = "Load " + mpIDData.getLoadID() + " with oldest item "
	                 + mpIDData.getItem() + " and lot " + mpOLData.getOrderLot()
	                 + " is not located in the rack or cannot reach "
	                 + mpOutStation.getStationName();
	             if (mzAllocationDiagnostics)
	               mpProbe.addProbeDetails("PieceAllocation.processOrderLines", vsInfo);

	             mpLogger.logOperation(vsInfo);
	             mpOrderServer.setOrderLineShort(mpOLData.getOrderID(), mpOLData.getItem(),
	                                           mpOLData.getOrderLot(), mpOLData.getLineID(),
	                                           DBConstants.YES);
	             break;
	           }
	           hMap = mpAllocServer.getLoadOutputStation(idTempData.getLoadID(),
	                                                     mpOutStation.getStationName(),
	                                                     mpOHD.getDestinationStation(),
	                                                     mnAisle);
	           msFirstOutputStation = hMap.get(ParameterNameConstants.STATIONNAME).toString();
	           vsLoadLocation = hMap.get(ParameterNameConstants.LOCATION).toString();
	           msRouteID = hMap.get(ParameterNameConstants.ROUTEID).toString();
	         }
	         else
	         {
	           LoadData vpLoadData = mpLoadServer.getLoad(idTempData.getLoadID());
	           if (vpLoadData == null)
	             throw new DBException("Critical Error! Load " + idTempData.getLoadID() +
	                                   " deleted by some other process during allocation!");

	           int vnLoadStatus = vpLoadData.getLoadMoveStatus();

	           msFirstOutputStation = msRouteID = "";
	           vsLoadLocation = vpLoadData.getAddress();

	 /*============================================================================
	         If the load's final location is a station don't allocate from it.
	   ============================================================================*/
	           if (mpLocationServer.isLocationAStation(vpLoadData.getFinalWarehouse(),
	                                                   vpLoadData.getFinalAddress()))
	           {
	             break;
	           }

	 /*============================================================================
	    Don't allocate from inbound loads since we need a valid output route in
	    the move record for everything to work.
	   ============================================================================*/
	           if ((vnLoadStatus == DBConstants.MOVING ||
	                vnLoadStatus == DBConstants.RETRIEVING ||
	                vnLoadStatus == DBConstants.RETRIEVESENT)&&
	               mpMoveServer.moveExists(idTempData.getLoadID()))
	           {                            // Must be an outbound load.
	             msFirstOutputStation = mpOutStation.getStationName();
	             msRouteID = mpOHD.getDestinationStation();
	             vsLoadLocation = "";
	           }
	           else if (vnLoadStatus != DBConstants.ARRIVEPENDING &&
	                    vnLoadStatus != DBConstants.MOVEPENDING   &&
	                    vnLoadStatus != DBConstants.STOREPENDING  &&
	                    vnLoadStatus != DBConstants.STORESENT     &&
	                    vnLoadStatus != DBConstants.STORING       &&
	                    vnLoadStatus != DBConstants.STOREERROR    &&
	                    vnLoadStatus != DBConstants.MOVING        &&
	                    mpLocationServer.isLocationAStation(vpLoadData.getWarehouse(), vpLoadData.getAddress()))
	           {
	             msFirstOutputStation = vpLoadData.getAddress();
	             vsLoadLocation = vpLoadData.getAddress();
	             msRouteID = mpOHD.getDestinationStation();
	             StandardRouteServer vpRouteServer = Factory.create(StandardRouteServer.class);
	             boolean vzRoute = vpRouteServer.checkPath(msRouteID, vsLoadLocation, msRouteID);
	             if(!vzRoute)
	             {
	               String vsInfo = "Load " + mpIDData.getLoadID()
	                     + " with oldest item " + mpIDData.getItem() + " and lot "
	                     + mpOLData.getOrderLot() + " cannot reach "
	                     + mpOutStation.getStationName();
	               if (mzAllocationDiagnostics)
	               {
	                 mpProbe.addProbeDetails("PieceAllocation.processOrderLines",
	                     vsInfo);
	                 mpOrderServer.setOrderLineShort(mpOLData.getOrderID(),
	                     mpOLData.getItem(), mpOLData.getOrderLot(),
	                     mpOLData.getLineID(), DBConstants.YES);

	               }
	               break;
	             }
	           }
	           else
	           {
	             mpLogger.logOperation("Order " + msOrder + " not allocated since " +
	                                   "oldest item " + mpOLData.getItem() + " is at input station!");
	                                        // The oldest product is likely on an input
	             break;                     // type station. Stop allocation until
	                                        // it comes back in.
	           }
	         }

	         try
	         {
	           if (msRouteID != null && msRouteID.trim().length() > 0)
	           {
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
	   /**
	    *  Method builds item move, updates item detail Load's next location, and
	    *  builds a list move loads to send back to the scheduler.
	    *  @param olData contains order line info.
	    *  @param idData contains item detail info.
	    *  @param pickQuantity amount that can be picked from this item detail.
	    *  @throws DBException if there is a DB update error.
	    *  @throws AllocationException if the station's device or load's device is
	    *          inoperable.
	    */
	    protected void processMove(OrderLineData olData, LoadLineItemData idData,
	                               double pickQuantity)
	              throws DBException, AllocationException
	    {
	    	 //KR:26/01/2022 -> MCM, EBS
	    	/*
	    	 * NOTE: because the order type is set to ORDERITEM =1 (line206 method addSmartFlowOrder in EBSOrderServer class)
	    	 *  the EBSPieceAllocation is get selected to process the order from SMF. (see getAllocationStrategy in AllocationController)
	    	 */
		   
	    	String vsDevice = mpLoadServer.getLoadDeviceID(idData.getLoadID());
	    	List<String> craneList = Arrays.asList("SR11","SR12","SR21", "SR22","SR31","SR32","SR41","SR42");
	    
	    	// Don't check for Inoperable crane, the secondary crane can always retrieve it
	    	if(!craneList.contains(vsDevice)) 
	    	{
		      if (mpDeviceServer.isStationDeviceInoperable(msFirstOutputStation) ||
		          mpDeviceServer.isDeviceInoperable(vsDevice))
		      {
		        String vsErr = "Station's Device or Load Device is not operational! "  +
		                       "Allocation Order '" + olData.getOrderID() + "' Item '" +
		                       olData.getItem() + "' and Lot '" + idData.getLot() + "' ";
		        throw new AllocationException(vsErr, AllocationException.DEVICE_INOP);
		      }
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
	      mpAllocServer.buildReturnData(idData.getLoadID(), msFirstOutputStation,
	                                  mpAllocatedDataList);
	    }
}
