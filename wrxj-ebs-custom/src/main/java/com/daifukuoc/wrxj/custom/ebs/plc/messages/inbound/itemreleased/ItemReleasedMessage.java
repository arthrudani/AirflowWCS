/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemreleased;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardInboundMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardItemReleasedTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

import io.netty.util.internal.StringUtil;

/**
 * @author BT
 * 
 *         this class only specify for the method that need to implement for the
 *         PLC Item Released Message related methods, if its common for all the
 *         PLC message
 *
 */
public class ItemReleasedMessage extends StandardInboundMessage {

	protected StandardStationServer stnServer = Factory.create(StandardStationServer.class);
	protected Load mpLoad = Factory.create(Load.class);
	StationData stationData = null;
	protected EBSLoadServer mpEBSLoadServer = Factory.create(EBSLoadServer.class);
	protected EBSInventoryServer mpEBSInventory = Factory.create(EBSInventoryServer.class);

	int sStatus; // Status Flags
	String orderId = "";// Order Id - Move order request ID
	String loadId = ""; // Tray,Container id
	String globalId = "";// Global I
	String lineId = ""; // sBarcode
	String stationId = ""; // Lane ID / station ID

	public ItemReleasedMessage() {
		// TODO Auto-generated constructor stub
	}

	public String getLoadId() {
		return loadId;
	}

	public void setLoadId(String loadId) {
		this.loadId = loadId;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getGlogalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/*
	 * Validate the PLC response message based on the message type the validation
	 * would be different need to add pass the parse manager object and the msg type
	 */
	@Override
	public boolean isValid() {
		boolean result = true;
		
		/* KR: TODO: what validation we need here?
		StationData stdata = Factory.create(StationData.class);

		try {
			stdata = stnServer.getStation(getStationId());
			LoadData loadData = mpLoad.getLoadData(getLoadId());
			if (loadData == null) {
				result = false;
				logger.logError("No Load Data found for Item Released message, [Load Id]" + getLoadId());
				throw new DBException("Load Record for Load " + getLoadId() + " not found!");
			} else if (stdata == null) {
				result = false;
				logger.logError("Invalid Station Id from Item Released message, [Station Id]" + getStationId());
				throw new DBException("Station Record for Station " + getStationId() + " not found!");
			}
		} catch (DBException ex) {
			logger.logError("Error while validating the PLC Item Released Message, Error" + ex.getMessage());
			return result;
		}*/
		return result;
	}

	/*
	 * Method will read the message from PLC related to PLC Item Arrived get and set
	 * the values that need to be use for the business logic for later process
	 * example ( header + Message body) :
	 * 24,S42-LL1A,1,OR153765478,001122334455,6111 (MsgType + LocationID, LoadID,
	 * OrderID, BarcodeID, StationID) Parse the data from the PLC based on the msg
	 * types return boolean result if the message parse is success
	 */
	@Override
	public boolean parse(String sMsg) {
		logger.logDebug("Parse PLC Item Released  Message : " + sMsg);
		if (!StringUtil.isNullOrEmpty(sMsg)) {
			String[] splitedMsg = sMsg.split(PLCConstants.DELIM_COMMA);
			if (splitedMsg != null) { // && splitedMsg.length == PLCMessages.PLC_ITEM_RELEAZED_MSG_BODY_LEN
				// the first item will be MsgType and second will be the SerialNum
				setSerialNumber(splitedMsg[1]);
				// Body start from here
				setOrderId(splitedMsg[2]);
				setLoadId(splitedMsg[3]); // Tray,Container id
				setGlobalId(splitedMsg[4]); // Global Id
				setLineId(splitedMsg[5]); // Bar-code ID
				setStationId(ConversionUtil.formatAddressForConveyor(splitedMsg[6]));// Lane ID

				logger.logDebug("PLC Item Released message Parsed sucessfully");
				return true;
			} else {
				logger.logDebug("Invalid PLC Item Released message from PLC for parsing");
				return false;
			}
		} else {
			logger.logDebug("PLC Item Released message from PLC for parsing Parse is Empty");
			return false;
		}
	}

	@Override
	public void processAck() {
		// TODO : need to validate

		PLCStandardAckMessage itemAckMsg = Factory.create(PLCStandardAckMessage.class);
		itemAckMsg.setDeviceId(getDeviceId());
		itemAckMsg.setSerialNum(getSerialNumber());
		itemAckMsg.setMessageType(PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE);
		itemAckMsg.setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
		itemAckMsg.sendMessageToPlc();
	}

	@Override
	public EBSTransactionContext process() {
		ItemReleasedContext plcItemReleasedContext = null;
		try {
			findAndSetStation();
			plcItemReleasedContext = new ItemReleasedContext(stationData, getLoadData(), getLoadLineData(),
					getStationId(), getOrderId(), getLoadId(), getGlogalId(), getLineId(), getSerialNumber(),
					getDeviceId());
			Factory.create(StandardItemReleasedTransaction.class).execute(plcItemReleasedContext);

		} catch (DBException ex) {
			logger.logException(ex);
		}
		return plcItemReleasedContext;
	}

	/**
	 * This method is used to fetch the load data
	 * 
	 * @return
	 */
	public LoadData getLoadData() {
		if (mpEBSLoadServer == null) {
			mpEBSLoadServer = Factory.create(EBSLoadServer.class);
		}
		return mpEBSLoadServer.getLoad(getLoadId());
	}

	/**
	 * This method fetch the load line data for the given load id
	 * 
	 * @return
	 */
	public LoadLineItemData getLoadLineData() {
		if (mpEBSLoadServer == null) {
			mpEBSLoadServer = Factory.create(EBSLoadServer.class);
		}
		return mpEBSLoadServer.getLoadLineByLoadId(getLoadId());
	}

	void findAndSetStation() {
		try {
			this.stationData = null;
			this.stationData = stnServer.getStation(getStationId());
			if (stationData == null) {

				logger.logError("Invalid Station Id from Item Released message, [Station Id]" + getStationId());
			}

		} catch (Exception ex) {
			logger.logError("Error while validationg the PLC Item Released Message, Error" + ex.getMessage());
		}

	}

	@Override
	public void outputTransactionLog(EBSTransactionContext ebsTransactionContext) {
		ItemReleasedContext plcItemArrivedContext = (ItemReleasedContext) ebsTransactionContext;
		mpEBSLoadServer.logLoadItemReleasedTransaction(plcItemArrivedContext.getLoadData(),
				plcItemArrivedContext.getLoadLineItemData(), plcItemArrivedContext.getStationId(),
				plcItemArrivedContext.getDeviceId(), plcItemArrivedContext.getLineId(),
				plcItemArrivedContext.getOrderId());
	}

}
