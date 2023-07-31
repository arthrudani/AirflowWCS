package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * PLC Move order message class is used to send the move order command to PLC
 * 
 * @author KR
 *
 */
public class PLCMoveOrderMessage extends StandardOutboundMessage {
    public static final int FROM_LOC_DEFAULT_VAL = 0;
    public static final int FINAL_SORT_LOC_DEFAULT_VAL = 0;
    public static final String SCH_DATE_DEFAULT_VAL = "NOSCHEDULEDATE";// MAX it can be 9 character
    public static final String LOT_DEFAULT_VAL = "NOFLIGHT";// MAX it can be 9 character
    public static final String LINE_DEFAULT_VAL = "NOBARCODE";// MAX it can be 12 character
    public static final int ORDER_DEFAULT_VAL = 0;

    private String loadId = ""; // Tray,Container id
    private String orderId = "";
    private String globalId = "";// Global Id
    private String lineId = ""; // sBarcode
    private String lot = ""; // Flight#
    private SimpleDateFormat formatter = new SimpleDateFormat(PLCConstants.DATE_FORMAT);
    private Date flightSchduledDateTime = new Date(); // Flight Scheduled Date Time - STD
    private String finalSortLocation = "0"; // FinalSortLocationId
    private String fromLocation = "0"; // FromLocation
    private String toLocation = "0"; // ToLocation
    private String moveType = ""; // Move type 0=direct (station to station) , 1=direct location (location to location),
                                  // 2= Storage (station to location), 3=Retrieval (location to station)
    
    public PLCMoveOrderMessage() {
    }

    public PLCMoveOrderMessage(String orderId, String loadId, String globalId, String lineId, String lot,
            Date flightSchduledDateTime, String finalSortLocation, String fromLocation, String toLocation,
            String moveType) {
        super();

        this.orderId = orderId;
        this.loadId = loadId;
        this.globalId = globalId;
        this.lineId = lineId;
        this.lot = lot;
        this.flightSchduledDateTime = flightSchduledDateTime;
        this.finalSortLocation = finalSortLocation;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.moveType = moveType;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public String getLoadId() {
        return loadId;
    }

    public void setLoadId(String loadId) {
        this.loadId = loadId;
    }

    public String getOrderId() {
        if (orderId == null || orderId.isBlank()) {
            return String.valueOf(PLCMoveOrderMessage.ORDER_DEFAULT_VAL);
        } else {
            return orderId;
        }
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public Date getFlightSchduledDateTime() {
        return flightSchduledDateTime;
    }

    public String getFlightSchduledDateTimeInString() {
        String expirationDateInMessageformat = PLCMoveOrderMessage.SCH_DATE_DEFAULT_VAL;
        try {
            expirationDateInMessageformat = formatter.format(getFlightSchduledDateTime());
        } catch (Exception e) {

        }
        return expirationDateInMessageformat;
    }

    public void setFlightSchduledDateTime(Date flightSchduledDateTime) {
        this.flightSchduledDateTime = flightSchduledDateTime;
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

    public String getFromLocation() {
        if (fromLocation == null || fromLocation.isBlank()) {
            return String.valueOf(PLCMoveOrderMessage.FROM_LOC_DEFAULT_VAL);
        } else {
            return fromLocation;
        }
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    public String getMoveType() {
        return moveType;
    }

    public void setMoveType(String moveType) {
        this.moveType = moveType;
    }

    @Override
    public String constructSendMessagetoPlc() {
        StringBuilder sendMessageBuilder = new StringBuilder();

        sendMessageBuilder.append(PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE);
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        // adding received serial number from the header
        sendMessageBuilder.append(getSerialNum());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getOrderId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLoadId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getGlobalId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLineId());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getLot());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getFlightSchduledDateTimeInString());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getFinalSortLocation());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getFromLocation());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getToLocation());
        sendMessageBuilder.append(PLCConstants.PLC_MESSAGE_DELIM);
        sendMessageBuilder.append(getMoveType());

        return sendMessageBuilder.toString();
    }
}
