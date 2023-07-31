/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class InventoryReqByFlightMessageData extends AbstractSKDCData {
    // The received whole message
    private String receivedMessage = "";
    // Header
    private SACMessageHeader header = new SACMessageHeader();
    // Fields in the body
    String requestID = ""; // Request Id
    String lot = ""; // Flight number
    String flightScheduledDateTime = ""; // Flight scheduled date time

    public boolean parse(String receivedMessage) {
        this.receivedMessage = receivedMessage;

        if (receivedMessage != null && !receivedMessage.isEmpty()) {
            String[] splitedMsg = receivedMessage.split(",");
            if (splitedMsg != null && splitedMsg.length == SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_SPLIT_NUM) {

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
                lot = splitedMsg[9];
                flightScheduledDateTime = splitedMsg[10];

                return isValid();
            }
        }

        return false;
    }

    /**
     * Validate the parsed retrieval order message
     * 
     * @return true if valid, false if not
     */
    public boolean isValid() {
        if (requestID.trim().isEmpty()) {
            return false;
        } else {
            try {
                Short parsed = Short.parseShort(requestID);
                if (parsed < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (lot.trim().isEmpty()) {
            return false;
        }
        if (flightScheduledDateTime.trim().isEmpty()) {
            return false;
        } else {
            try {
                LocalDateTime.parse(flightScheduledDateTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    public SACMessageHeader getHeader() {
        return header;
    }

    public String getRequestId() {
        return requestID;
    }

    public String getLot() {
        return lot;
    }

    public String getFlightScheduledDateTime() {
        return flightScheduledDateTime;
    }

   

    @Override
    public String toString() {
        return "RetrievalOrderMessageData [receivedMessage=" + receivedMessage + ", header=" + header + ", orderId="
                + requestID + ", lot=" + lot + ", flightScheduledDateTime=" + flightScheduledDateTime +  "]";
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
        InventoryReqByFlightMessageData other = (InventoryReqByFlightMessageData) obj;
        return Objects.equals(flightScheduledDateTime, other.flightScheduledDateTime)
                && Objects.equals(header, other.header) && Objects.equals(lot, other.lot)
                && Objects.equals(requestID, other.requestID)
                && Objects.equals(receivedMessage, other.receivedMessage);
    }

}
