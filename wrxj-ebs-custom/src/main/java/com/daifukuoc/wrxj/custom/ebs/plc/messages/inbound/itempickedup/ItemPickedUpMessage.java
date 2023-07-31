/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardInboundMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemPickedupTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.util.PLCMessageUtil;

import io.netty.util.internal.StringUtil;

public class ItemPickedUpMessage extends StandardInboundMessage {

	protected Logger logger = Logger.getLogger();
	protected StandardStationServer stnServer = Factory.create(StandardStationServer.class);
	protected Load mpLoad = Factory.create(Load.class);
	protected EBSLoadServer mpEBSLoadServer = Factory.create(EBSLoadServer.class);
	int sStatus; // 1 = succeed, 2 = Error, 3 = Unexpected Bin Empty Error
	String sOrderId = "";// Order Id - Move order request ID
	String sLoadId = ""; // Tray,Container id
	String sGlobalId = "";// Global I
	String sLineId = ""; // sBarcode
	String sFromLocationId = ""; // Storage location/inbound/outbound station ID where the item has been picked
									// up
	String sToLocationId = ""; // Lifter/shuttle location of where the item is now if succeed. 0 if pickup
								// failed.

	public ItemPickedUpMessage() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * Setters section
	 */
	public void setFromLocationId(String vsStationID) {
		sFromLocationId = vsStationID;
	}

	public void setToLocationId(String vsStationID) {
		sToLocationId = vsStationID;
	}

	public void setStatus(int vsStatus) {
		sStatus = vsStatus;
	}

	public void setOrderId(String vsOrderID) {
		sOrderId = vsOrderID;
	}

	public void setLoadId(String vsLoadId) {
		sLoadId = vsLoadId;
	}

	public void setLineId(String vsLineId) {
		sLineId = vsLineId;
	}

	public void setGlobalId(String globalId) {
		sGlobalId = globalId;
	}

	/*
	 * Getters section
	 */
	public String getFromLocationID() {
		return (sFromLocationId);
	}

	public String getToLocationID() {
		return (sToLocationId);
	}

	public int getStatus() {
		return (sStatus);
	}

	public String getOrderID() {
		return (sOrderId);
	}

	public String getLoadId() {
		return (sLoadId);
	}

	public String getLineId() {
		return (sLineId);
	}

	public String getGlogalId() {
		return sGlobalId;
	}

	/*
	 * Validate the PLC response message based on the message type the validation
	 * would be different need to add pass the parse manager object and the msg type
	 */
	@Override
	public boolean isValid() {
		boolean result = true;
		// TODO:
		// - Update the Load with cureent location of the item ( toLocation)
		// - in Stage 2 Update Location status EmpytFlag here and set to Empty if item
		// picked up from Location to
		// Shuttle

		/*
		 * StationData stdata = Factory.create(StationData.class);
		 * 
		 * try {
		 * 
		 * } catch (DBException ex) {
		 * logger.logError("Error while validating the PLC Item Release Message, Error"
		 * + ex.getMessage()); return result; }
		 */
		return result;
	}

	@Override
	public boolean parse(String sMsg) {
		logger.logDebug("Parse PLC Item Relase Response Message : " + sMsg);
		if (!StringUtil.isNullOrEmpty(sMsg)) {
			String[] splitedMsg = sMsg.split(PLCConstants.DELIM_COMMA);
			if (splitedMsg != null) {
				setSerialNumber(splitedMsg[1]);
				setOrderId(splitedMsg[2]);
				setLoadId(splitedMsg[3]); // Tray,Container id
				setGlobalId(splitedMsg[4]); // Global Id
				setLineId(splitedMsg[5]); // Barcode ID
				setFromLocationId(String.format("%010d", Integer.parseInt(splitedMsg[6])));// Lane ID / Station ID
				setToLocationId(String.format("%010d", Integer.parseInt(splitedMsg[7])));// Lane ID / Unique Device ID /
																							// Out-bound station ID
				int iStatus = (PLCMessageUtil.isNumeric(splitedMsg[8]) ? Integer.parseInt(splitedMsg[8]) : 1);
				setStatus(iStatus);
				logger.logDebug("PLC Item Relase Response message Parsed sucessfully");
				return true;
			} else {
				logger.logDebug("Invalid PLC Item Relase Response message from PLC for parsing");
				return false;
			}
		} else {
			logger.logDebug("PLC Item Relase Response message from PLC for parsing Parse is Empty");
			return false;
		}
	}

	@Override
	public EBSTransactionContext process() {
		ItemPickedUpContext plcItemPickedUpContext = null;
		try {
			plcItemPickedUpContext = new ItemPickedUpContext(getLoadId(), getFromLocationID(), getToLocationID());
			Factory.create(StandardItemPickedupTransaction.class).execute(plcItemPickedUpContext);

		} catch (DBException ex) {
			logger.logException(ex);
		}
		return plcItemPickedUpContext;
	}

	@Override
	public void processAck() {
		PLCStandardAckMessage itemAckMsg = Factory.create(PLCStandardAckMessage.class);
		itemAckMsg.setDeviceId(getDeviceId());
		itemAckMsg.setSerialNum(getSerialNumber());
		itemAckMsg.setMessageType(PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE);
		itemAckMsg.setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		itemAckMsg.sendMessageToPlc();

	}

	@Override
	public void outputTransactionLog(EBSTransactionContext ebsTransactionContext) {
		ItemPickedUpContext plcItemPickedUpContext = (ItemPickedUpContext) ebsTransactionContext;
		try {
			LoadData loadData = mpLoad.getLoadData(getLoadId());
			LoadLineItemData loadLineData = mpEBSLoadServer.getLoadLineByLoadId(getLoadId());

			mpEBSLoadServer.logLoadItemPickedUpTransaction(loadData, loadLineData,
					plcItemPickedUpContext.getFromLocation(), getDeviceId(), getLineId(), getOrderID());
		} catch (DBException ex) {
			logger.logException(ex);
		}
	}

}
