package com.daifukuoc.wrxj.custom.ebs.allocator;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.allocator.AllocationException;
import com.daifukuamerica.wrxj.allocator.AllocationMessageDataFormat;
import com.daifukuamerica.wrxj.allocator.PieceAllocation;
import com.daifukuamerica.wrxj.allocator.shortorder.ShortOrderAllocationException;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSAllocationServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSOrderServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;

public class ConveyorPieceAllocation extends PieceAllocation{
	 private EBSHostServer  mpEBSHostServer;
	 private EBSOrderServer  mpEBSOrderServer;
	 private EBSAllocationServer mpEBSAllocationServer;
	 private ConveyorTableJoin mpTableJoin = null;
	  
	public ConveyorPieceAllocation()
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
		 
		 /* Create a unique list of Land Id ( Load.sAddress) that need to be flushed
		 	The Load.sShelfPosition is used for position of the tray/bag on the CONVEYOR lane from entrance side. 
		 	SO we can use Load.sShelfPosition of the bag to calculate the number of bags (Quantity) that need to be
			released from each lane instead of flushing whole lane.  
		  */
	
		 // Only need to find the sAddress ( station ID ) of the Conveyor which need to be flushed! 
		 initializeTableJoin();
		 
		 for(Map vpOLMap : mpOLList)
		 {
			 mpOLData.dataToSKDCData(vpOLMap);
			 LoadData vpLoadData = mpLoadServer.getLoad(mpOLData.getLoadID());
	
			 if (vpLoadData == null)
			 {
	            // throw new DBException("Critical Error! Load " + mpOLData.getLoadID() +" deleted by some other process during allocation!");
				 mpLogger.logError("Critical Error! Load " + mpOLData.getLoadID() +" deleted by some other process during allocation!");
				 //Create a short list!!
				 continue; 
			 }
			 //this is position of the tray/bag on the conveyor 
			 Integer itemPosition = ( vpLoadData.getShelfPosition() == null || vpLoadData.getShelfPosition().isBlank()) 
						? 0 : Integer.parseInt(vpLoadData.getShelfPosition());  
			 
			 AllocationMessageDataFormat allocData = mpAllocatedDataList.stream()
					 .filter(c -> c.getOutputStation().equals(vpLoadData.getAddress())).findFirst().orElse(null);
			 
			 if( allocData != null)
			 {
				 Integer currentPosition =  ( allocData.getFromAddress() != null )? Integer.parseInt( allocData.getFromAddress()): 0 ;
				 
				 //just check the position 
				 if(itemPosition <  currentPosition ) 
				 {
					 //update it
					 allocData.setFromAddress(String.valueOf( itemPosition) );
				 }
			 }else
			 {
				//create a new one 
				 AllocationMessageDataFormat newAllocData = new AllocationMessageDataFormat();
				//newAllocData.setOutBoundLoad(mpOLData.getLoadID());   
				 newAllocData.setOutBoundLoad(mpOLData.getOrderLot());  //Put flight number (sLot) to this field
     	         //newAllocData.setFromWarehouse(vpLoadData.getWarehouse());
				 newAllocData.setFromWarehouse(vpLoadData.getDeviceID()); //set Device ID in this field
     	         newAllocData.setFromAddress(String.valueOf( itemPosition)); //Put the position in this 
				 newAllocData.setOutputStation(vpLoadData.getAddress()); //Conveyor ID which need to be flushed
				 newAllocData.setOrderID(mpOLData.getOrderID());
				 newAllocData.createDataString();

     	         mpAllocatedDataList.add(newAllocData); 
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
	  protected void initializeTableJoin()
	  {
	    if (mpTableJoin == null)
	    {
	    	mpTableJoin = Factory.create(ConveyorTableJoin.class);
	    }
	  }
	  @Override	
	  protected void processMove(OrderLineData olData, LoadLineItemData idData,
             double pickQuantity)throws DBException, AllocationException
	  {
		
		//mpAllocServer.buildReturnData(idData.getLoadID(), msFirstOutputStation,mpAllocatedDataList);
	  }
}
