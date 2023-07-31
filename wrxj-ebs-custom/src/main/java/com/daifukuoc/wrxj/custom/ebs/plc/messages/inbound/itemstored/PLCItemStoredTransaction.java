package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored;

import java.util.Date;
import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommand;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSSchedulerServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSStoreCompletionNotifyMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerFailureException;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemStoredTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived.ItemArrivedContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionWrapper;
import com.daifukuoc.wrxj.custom.ebs.plc.queue.CommandQueueManagerImpl;

/**
 * Item Stored message transaction implementation.
 * 
 * @author ST
 *
 */
public class PLCItemStoredTransaction extends StandardItemStoredTransaction {
    protected Load mpLoad = Factory.create(Load.class);
    protected EBSLocation mpEBSLocation = Factory.create(EBSLocation.class);

    public PLCItemStoredTransaction() {
        super(true);
    }

    @Override
    protected void 	executeBody(ItemStoredContext plcItemStoredContext) throws DBException {
      
    	EBSInventoryServer mpEBSInventoryServer = Factory.create(EBSInventoryServer.class);

        EBSLoadServer mpEBSLoadServer = Factory.create(EBSLoadServer.class);
        MoveCommandData moveCommandData = null;
        try {
        	
        	if(plcItemStoredContext.getStatus() == PLCConstants.PLC_ITEM_STORED_BIN_FULL_ERROR || 
        			plcItemStoredContext.getStatus() == PLCConstants.PLC_ITEM_STORED_ERROR) {
        		LoadData loadData = mpEBSLoadServer.getLoad(plcItemStoredContext.getLoadId());
        		plcItemStoredContext.setLoadData(loadData);
        		LoadLineItemData lineItemData = mpEBSLoadServer.getLoadLineByLoadId(loadData.getLoadID());
        		plcItemStoredContext.setLoadLineItemData(lineItemData);
        		
        		AisleEmptyLocationFinderImpl emptyLocationFinderImpl = Factory.create(AisleEmptyLocationFinderImpl.class);
        		String availableAddress = emptyLocationFinderImpl.find(plcItemStoredContext.getLoadData());
        		if(plcItemStoredContext.getStatus() == PLCConstants.PLC_ITEM_STORED_BIN_FULL_ERROR)
        		{
        			// Update the old location to occupied
        			mpEBSLocation.setEmptyFlagValue(loadData.getWarehouse(), loadData.getAddress(), DBConstants.OCCUPIED);
        		
        			//set the location to prohibited
        			mpEBSLocation.setLocationStatusValue(loadData.getWarehouse(), loadData.getAddress(), DBConstants.LCPROHIBIT);
        		}
        		
        		// Mark the previous command to deleted
        		MoveCommand moveCommand = Factory.create(MoveCommand.class);
        		moveCommand.updateMoveCommandStatusByLoadId(plcItemStoredContext.getLoadId(), DBConstants.CMD_DELETED);

        		CommandQueueManagerImpl cmdQueueManager = Factory.create(CommandQueueManagerImpl.class);
        		// If not able to find any empty location on same level
        		if (Objects.isNull(availableAddress)) {
        			// Update load data with new address to database
            		loadData.setLoadMoveStatus(DBConstants.RETRIEVING);
            		mpLoad.updateLoadInfo(loadData);	
        			
        			// release the bag and send to outbound station.
        			RouteManagerImpl routeManagerImpl = Factory.create(RouteManagerImpl.class);
        			String nextAddress = routeManagerImpl.findNextDestination(plcItemStoredContext.getLoadData(), plcItemStoredContext.getLoadData().getAddress());
        			 moveCommandData = constructMoveCommandData(plcItemStoredContext, nextAddress, 
        					 routeManagerImpl.getMoveType(), DBConstants.CMD_RETRIEVAL);
        		} else {

        			// Update new address to database
        			loadData.setAddress(availableAddress);
        			mpLoad.updateLoadInfo(loadData);
        			
        			// Update the new location to occupied
        			mpEBSLocation.setEmptyFlagValue(loadData.getWarehouse(), availableAddress, DBConstants.LCRESERVED);
        			// Create new move command 
        			moveCommandData = constructMoveCommandData(plcItemStoredContext, loadData.getCurrentAddress(), 
       					 String.valueOf(DBConstants.CMD_DIRECT_LOC), DBConstants.CMD_RACK);
        		}
        		// send move command to queue manager
        		cmdQueueManager.enqueue(moveCommandData);
        		
        	}else
        	{
	            // In normal situation Update the load and location information for the store completion.
	        	mpEBSInventoryServer.processStoreComplete(plcItemStoredContext);
	        	//publish to host
	        	if( mpEBSInventoryServer.mzHasHostSystem &&  plcItemStoredContext.isSuccess())
	        	{
	        		publishItemStoredToHost(plcItemStoredContext);
	        	}
        	}
        } catch (DBException e) {
            logger.logError("Error while Processing PLC Store completion message, Error:" + e.getMessage());
            logger.logException(e);
        } catch (RouteManagerFailureException e) {
        	logger.logError("Error while Processing PLC Store completion message, Error:" + e.getMessage());
            logger.logException(e);
		}
    }
    
    /**
     * Method to construct MoveCommand with data
     * @param plcItemStoredContext
     * @param nextStation - next station location
     * @param moveType - type of move 
     * @return MoveCommand with all the data
     */
    protected MoveCommandData constructMoveCommandData(ItemStoredContext plcItemStoredContext, String nextStation, String moveType, Integer cmdOrderType) {
    	MoveCommandData moveCommandData = Factory.create(MoveCommandData.class);
        moveCommandData.setCreatedDate(new Date(System.currentTimeMillis()));
        moveCommandData.setLoadID(plcItemStoredContext.getLoadId());
        moveCommandData.setFinalSortLocationID(plcItemStoredContext.getLoadData().getFinalSortLocationID());
        moveCommandData.setCmdStatus(DBConstants.CMD_READY);
        moveCommandData.setGlobalID(plcItemStoredContext.getGlobalId());
        moveCommandData.setOrderid(moveCommandData.getOrderid());
        moveCommandData.setCmdMoveType( (moveType.isEmpty()? 0 : Integer.valueOf(moveType) ));
        moveCommandData.setFrom(plcItemStoredContext.getLoadData().getCurrentAddress());
        moveCommandData.setToDest(nextStation);
        moveCommandData.setCmdOrderType(cmdOrderType);
        moveCommandData.setItemID(plcItemStoredContext.getLineId());
        moveCommandData.setFlightNum(plcItemStoredContext.getLoadLineItemData().getLot());
        moveCommandData.setFlightSTD(plcItemStoredContext.getLoadLineItemData().getExpectedDate());
        moveCommandData.setDeviceID(plcItemStoredContext.getDeviceId());
		return moveCommandData;
	}

    private void publishItemStoredToHost(ItemStoredContext plcItemStoredContext) {
    	EBSStoreCompletionNotifyMessage mpResponseMsg = Factory
                .create(EBSStoreCompletionNotifyMessage.class);
        mpResponseMsg.setOrderID(plcItemStoredContext.getOrderId());
        mpResponseMsg.setLoadID(plcItemStoredContext.getLoadId());
        mpResponseMsg.setGlobalID(plcItemStoredContext.getGlobalId());
        mpResponseMsg.setLineID(plcItemStoredContext.getLineId());
        mpResponseMsg.setZoneID("");
        mpResponseMsg.setLocation(plcItemStoredContext.getAddressId());
        mpResponseMsg.setStatus(plcItemStoredContext.getStatus());

        mpResponseMsg.format();
        String messageToSend = new String(mpResponseMsg.prepareMessageToSend((int) 0));
        //publish this to host
        ThreadSystemGateway.get().publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }
}
