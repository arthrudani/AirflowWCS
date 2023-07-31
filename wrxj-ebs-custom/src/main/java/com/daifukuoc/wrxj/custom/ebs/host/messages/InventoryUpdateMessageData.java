package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class InventoryUpdateMessageData extends AbstractSKDCData {

    // The received whole message
    private String receivedMessage = "";
    // Header
    SACMessageHeader header = new SACMessageHeader();
    // Fields in the body
    private String loadId = ""; // Tray,Container id
    private String globalId = "";// Global Id
    private String lineId = ""; // sBarcode
    private String storageLocationID = "";
    private int status; // 0=Automatically removed from storage location, 
    					// 1=Manually removed from a storage location, 
    					// 2=manual update added to a location, 3=pickup failed – nothing to pick up.
	
    public boolean parse(String receivedMessage) {
        this.receivedMessage = receivedMessage;

        if (receivedMessage != null && !receivedMessage.isEmpty()) {
            String[] splitedMsg = receivedMessage.split(",");
            if (splitedMsg != null && splitedMsg.length == SACControlMessage.INVENTORY_UPDATE_SPLIT_LEN) {

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
                loadId = splitedMsg[9];
                globalId = splitedMsg[10];
                lineId= splitedMsg[11];
                storageLocationID = splitedMsg[12];
                try {
                	status = Integer.parseInt(splitedMsg[13]);
                } catch (NumberFormatException e) {
				}
                return isValid();
            }
        }

        return false;
    }
    
    /**
     * Validate the parsed inventory update message
     * 
     * @return true if valid, false if not
     */
    public boolean isValid() {

    	if (loadId.trim().isEmpty()) {
            return false;
        } else {
            try {
                Integer parsed = Integer.parseInt(loadId);
                if (parsed < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (globalId.trim().isEmpty()) {
            return false;
        } else {
            try {
                Integer parsed = Integer.parseInt(globalId);
                if (parsed < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (lineId.trim().isEmpty()) {
            return false;
        }
        if (storageLocationID.trim().isEmpty()) {
            return false;
        } else {
            try {
                Integer parsed = Integer.parseInt(storageLocationID);
                if (parsed < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        if (status != SACControlMessage.InventoryUpdate.STATUS.AUTO_REMOVE
                && status != SACControlMessage.InventoryUpdate.STATUS.MANUAL_REMOVE
                && status != SACControlMessage.InventoryUpdate.STATUS.PICKUP_FAILED
                && status != SACControlMessage.InventoryUpdate.STATUS.MANUAL_UPDATE) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "InventoryUpdateMessageData [receivedMessage=" + receivedMessage + ", header=" + header
                + ", loadId=" + loadId + ", globalId=" + globalId
                + ", lineId=" + lineId + ", storageLocationID=" + storageLocationID
                + "]";
    }
    
    @Override
    public boolean equals(AbstractSKDCData eskdata) {
        return equals((Object) eskdata);
    }

    public String getReceivedMessage() {
		return receivedMessage;
	}

	public SACMessageHeader getHeader() {
		return header;
	}

	public String getLoadId() {
		return loadId;
	}

	public String getGlobalId() {
		return globalId;
	}

	public String getLineId() {
		return lineId;
	}

	public String getStorageLocationID() {
		return storageLocationID;
	}

	public int getStatus() {
		return status;
	}

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InventoryUpdateMessageData other = (InventoryUpdateMessageData) obj;
        return Objects.equals(loadId, other.loadId)
                && Objects.equals(globalId, other.globalId)
                && Objects.equals(lineId, other.lineId)
                && Objects.equals(storageLocationID, other.storageLocationID)
                && Objects.equals(header, other.header) && Objects.equals(receivedMessage, other.receivedMessage);
    }
}
