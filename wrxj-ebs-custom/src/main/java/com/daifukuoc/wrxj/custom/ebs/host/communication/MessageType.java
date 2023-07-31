package com.daifukuoc.wrxj.custom.ebs.host.communication;

import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
/**
 * Enms for the HostToSAC messages
 * @author KR
 *
 */
public enum MessageType {

	EXPECTEDRECEIPTMESSAGE(SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE),
	FLIGHTDATAUPDATEMESSAGE(SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE),
	RETRIEVALORDERMESSAGE(SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE),
	INVENTORYREQUESTBYWAREHOUSEMESSAGE(SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE),
	RETRIEVALITEMMESSAGE(SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE),
    STOREDCOMPLETEACKMESSAGE(SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE),
    INVENTORYUPDATEMESSAGE(SACControlMessage.INVENTORY_UPDATE_MSG_TYPE),
	INVENTORYREQUESTMESSAGE(SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE);

	MessageType(int i) {
		itype = i;
	}
	private int itype;
	public int getMessageType()
    {
        return this.itype;
    }		
}
