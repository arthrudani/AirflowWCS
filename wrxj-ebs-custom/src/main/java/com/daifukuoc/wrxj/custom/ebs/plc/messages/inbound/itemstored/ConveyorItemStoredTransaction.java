package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.ConveyorTableJoin;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSStoreCompletionNotifyMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemStoredTransaction;

/**
 * Item Stored message transaction implementation for Conveyor system.
 * 
 * @author KR
 *
 */
public class ConveyorItemStoredTransaction extends StandardItemStoredTransaction {
    protected Load mpLoad = Factory.create(Load.class);
    protected EBSLocation mpEBSLocation = Factory.create(EBSLocation.class);

    public ConveyorItemStoredTransaction() {
        super(true);
    }

    @Override
    protected void 	executeBody(ItemStoredContext plcItemStoredContext) throws DBException {
    	// Format the address to remove leading zeros
    	plcItemStoredContext.setAddressId(ConversionUtil.formatAddressForConveyor(plcItemStoredContext.getAddressId()));
    	
    	EBSInventoryServer mpEBSInventoryServer = Factory.create(EBSInventoryServer.class);
    	 EBSPoReceivingServer mpEBSPoSever = Factory.create(EBSPoReceivingServer.class);
    	 ConveyorTableJoin convTJ = Factory.create(ConveyorTableJoin.class);

        try {
        	
        	if(plcItemStoredContext.getStatus() == PLCConstants.PLC_ITEM_STORED_BIN_FULL_ERROR || 
        			plcItemStoredContext.getStatus() == PLCConstants.PLC_ITEM_STORED_ERROR) {
        		
        		// Fetch POL with load Id
        		PurchaseOrderLineData pol = mpEBSPoSever.getPurchaseOrderLineByLoadId(plcItemStoredContext.getLoadId());
        		
        		// Delete POL
        		convTJ.deletePurchaseOrderLineByOrderId(pol.getOrderID());
        		
        		// Delete POH
        		convTJ.deletePurchaseOrderHeaderByOrderId(pol.getOrderID());
        		
        		// Delete LoadLine
        		convTJ.deleteLoadlineByLoadId(plcItemStoredContext.getLoadId());
        		
        		// Delete Load
        		convTJ.deleteLoadByLoadId(plcItemStoredContext.getLoadId());
        	
        		 logger.logDebug("Bin full error for item: "+ plcItemStoredContext.getLoadId());
        		 
        	}else
        	{
	            // In normal situation Update the load and location information for the store completion.
	        	mpEBSInventoryServer.processStoreCompleteForConveyor(plcItemStoredContext);
	        	//publish to host
	        	if( mpEBSInventoryServer.mzHasHostSystem &&  plcItemStoredContext.isSuccess())
	        	{
	        		publishItemStoredToHost(plcItemStoredContext);
	        	}
        	}
        } catch (DBException e) {
            logger.logError("Error while Processing PLC Store completion message, Error:" + e.getMessage());
            logger.logException(e);
        }
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
