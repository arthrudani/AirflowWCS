package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class InventoryReqByWarehouseMessageData extends AbstractSKDCData {

	
	// The received whole message
    private String receivedMessage = "";
    // Header
    SACMessageHeader header = new SACMessageHeader();
    // Fields in the body
    private String requestID = "";
    private String warehouseID = "";
        
	public String getReceivedMessage() {
		return receivedMessage;
	}

	public void setReceivedMessage(String receivedMessage) {
		this.receivedMessage = receivedMessage;
	}

	public SACMessageHeader getHeader() {
		return header;
	}

	public void setHeader(SACMessageHeader header) {
		this.header = header;
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public String getWarehouseID() {
		return warehouseID;
	}

	public void setWarehouseID(String warehouseID) {
		this.warehouseID = warehouseID;
	}

    public boolean parse(String receivedMessage) {
        this.receivedMessage = receivedMessage;

        if (this.receivedMessage != null && !this.receivedMessage.isEmpty()) {
            String[] splitedMsg = this.receivedMessage.split(",");
            if (splitedMsg != null && splitedMsg.length == SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_SPLIT_NUM) {

                // Split the initial 8 fields into the header
                header.setMsgLength(Integer.parseInt(splitedMsg[0]));
                header.setSeqNo(Integer.parseInt(splitedMsg[1]));
                header.setMsgType(Short.parseShort(splitedMsg[2]));
                header.setEquipmentId(splitedMsg[3]);
                header.setHours(Integer.parseInt(splitedMsg[4]));
                header.setMinutes(Integer.parseInt(splitedMsg[5]));
                header.setMilliSeconds(Integer.parseInt(splitedMsg[6]));
                header.setMsgVersion(Integer.parseInt(splitedMsg[7]));

                // Split the remaining 4 fields into the respective variables
                requestID = splitedMsg[8];
                warehouseID = splitedMsg[9];

                return isValid();
            }
        }

        return false;
    }
    
    
    /**
     * Validate the parsed inventory request by warehouse message data
     * 
     * @return true if valid, false if not
     */
    public boolean isValid() {

        if (warehouseID.trim().isEmpty()) {
            return false;
        }
        if (requestID.trim().isEmpty()) {
            return false;
        } else {
            try {
                Integer parsed = Integer.parseInt(requestID);
                if (parsed < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "InventoryReqByWarehouseMessageData [receivedMessage=" + receivedMessage + ", header=" + header
                + ", warehouseID=" + warehouseID + ", requestID=" + requestID + "]";
    }
    
    @Override
    public boolean equals(AbstractSKDCData eskdata) {
        return equals((Object) eskdata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InventoryReqByWarehouseMessageData other = (InventoryReqByWarehouseMessageData) obj;
        return Objects.equals(requestID, other.requestID)
                && Objects.equals(warehouseID, other.warehouseID)
                && Objects.equals(header, other.header) && Objects.equals(receivedMessage, other.receivedMessage);
    }
}
