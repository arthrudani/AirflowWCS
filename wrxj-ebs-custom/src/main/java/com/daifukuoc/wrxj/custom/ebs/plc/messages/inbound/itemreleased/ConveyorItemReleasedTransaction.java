package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemreleased;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.EBSItemReleaseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemReleasedTransaction;


/**
 * Item Released Transaction does process body of Item Arrived message [54]
 * 
 * @author KR
 *
 */
public class ConveyorItemReleasedTransaction extends StandardItemReleasedTransaction {

	protected EBSInventoryServer mpEBSInventory = Factory.create(EBSInventoryServer.class);
	protected Load load = Factory.create(Load.class);

	/**
	 * Constructor.
	 */
	public ConveyorItemReleasedTransaction() {
		super(true);
	}

	@Override
	protected void executeBody(ItemReleasedContext plcItemReleasedContext) throws DBException {
		doTerminalProcess(plcItemReleasedContext);
		plcItemReleasedContext.setSuccess(true);
	}

	void doTerminalProcess(ItemReleasedContext plcItemReleasedContext) {
		try {
			mpEBSInventory.processReleasedLoadDataForConveyor(plcItemReleasedContext);
			publishItemReleasedToHost(plcItemReleasedContext);

		} catch (DBException e) {
			logger.logError("DB Exception item arrived: " + plcItemReleasedContext.getStationId() + ". Error:"
					+ e.getMessage());
		}
	}
	
	private void publishItemReleasedToHost(ItemReleasedContext plcItemReleasedContext) {
    	EBSItemReleaseMessage mpResponseMsg = Factory.create(EBSItemReleaseMessage.class);
        mpResponseMsg.setOrderID(plcItemReleasedContext.getOrderId());
        mpResponseMsg.setLoadID(plcItemReleasedContext.getLoadId());
        mpResponseMsg.setGlobalID(plcItemReleasedContext.getGlobalId());
        mpResponseMsg.setLineID(plcItemReleasedContext.getLineId());
        mpResponseMsg.setArrivalStation(plcItemReleasedContext.getStationId());
        mpResponseMsg.setStatus(SACControlMessage.ItemRelease.STATUS.NORMAL_RETRIEVAL);

        mpResponseMsg.format();
        String messageToSend = new String(mpResponseMsg.prepareMessageToSend((int) 0));
        //publish this to host
        ThreadSystemGateway.get().publishHostMesgSendEvent(messageToSend, 0, "HostPort");
    }

}
