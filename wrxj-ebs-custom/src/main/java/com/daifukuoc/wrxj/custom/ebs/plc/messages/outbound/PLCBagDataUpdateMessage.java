package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * Outbound Message For BagDataUpdateMsg
 * @author MT
 *
 */
public class PLCBagDataUpdateMessage extends StandardOutboundMessage {
	public static final int LOCATION_ID_DEFAULT_VAL = 0;
    public static final int FINAL_SORT_LOC_DEFAULT_VAL = 0;
    public static final String LOT_DEFAULT_VAL = "NOFLIGHT";// MAX it can be 9 character
    public static final String LINE_DEFAULT_VAL = "NOBARCODE";// MAX it can be 12 character

	private String loadId = ""; // Tray,Container id
    private String globalId = "";// Global Id
    private String lineId = ""; // sBarcode
    private String lot = ""; // Flight#
    private String finalSortLocation = "0"; // FinalSortLocationId
    private String locationID = ""; //Location ID
    private int updateType = 0; // 1=Placed in storage location, 
    							// 2=Removed from storage location, 3=Sort destination update
	
    // Default constructor
    public PLCBagDataUpdateMessage() {
    	
    }
    
    // All arguments constructor
	public PLCBagDataUpdateMessage(String loadId, String globalId, String lineId, String lot, String finalSortLocation,
			String locationID, int updateType) {
		super();
		this.loadId = loadId;
		this.globalId = globalId;
		this.lineId = lineId;
		this.lot = lot;
		this.finalSortLocation = finalSortLocation;
		this.locationID = locationID;
		this.updateType = updateType;
	}

	public String getLoadId() {
		return loadId;
	}

	public void setLoadId(String loadId) {
		this.loadId = loadId;
	}

	public String getGlobalId() {
		return globalId;
	}

	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}

	public String getLineId() {
        if (lineId == null || lineId.isBlank()) {
            return String.valueOf(PLCMoveOrderMessage.LINE_DEFAULT_VAL);
        } else {
            return lineId;
        }
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getLot() {
        if (lot == null || lot.isBlank()) {
            return PLCMoveOrderMessage.LOT_DEFAULT_VAL;
        } else {
            return lot;
        }
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

	public String getFinalSortLocation() {
        if (finalSortLocation == null || finalSortLocation.isBlank()) {
            return String.valueOf(PLCMoveOrderMessage.FINAL_SORT_LOC_DEFAULT_VAL);
        } else {
            return finalSortLocation;
        }
    }

    public void setFinalSortLocation(String finalSortLocation) {
        this.finalSortLocation = finalSortLocation;
    }

	public String getLocationID() {
		return locationID;
	}

	public void setLocationID(String locationID) {
		this.locationID = locationID;
	}

	public int getUpdateType() {
		return updateType;
	}

	public void setUpdateType(int updateType) {
		this.updateType = updateType;
	}

	@Override
	public String constructSendMessagetoPlc() {
		StringBuilder sendMessageBuilder = new StringBuilder();

        sendMessageBuilder.append(PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE);
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        // adding received serial number from the header
        sendMessageBuilder.append(getSerialNum());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLoadId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getGlobalId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLineId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLot());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getFinalSortLocation());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLocationID());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getUpdateType());
		
        return sendMessageBuilder.toString();
	}

}
