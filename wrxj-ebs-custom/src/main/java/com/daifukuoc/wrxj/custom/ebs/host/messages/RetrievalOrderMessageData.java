/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class RetrievalOrderMessageData extends AbstractSKDCData {
    // The received whole message
    private String receivedMessage = "";
    // Header
    private SACMessageHeader header = new SACMessageHeader();
    // Fields in the body
    String orderId = ""; // Global Id
    String lot = ""; // Flight number
    String flightScheduledDateTime = ""; // Flight scheduled date time
    String numberOfBags = ""; // Number of Bags to retrieve, 0 = All

    public boolean parse(String receivedMessage) {
        this.receivedMessage = receivedMessage;

        if (receivedMessage != null && !receivedMessage.isEmpty()) {
            String[] splitedMsg = receivedMessage.split(",");
            if (splitedMsg != null && splitedMsg.length == SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_SPLIT_NUM) {

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
                orderId = splitedMsg[8];
                lot = splitedMsg[9];
                flightScheduledDateTime = splitedMsg[10];
                numberOfBags = splitedMsg[11];

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
        if (orderId.trim().isEmpty()) {
            return false;
        } else {
            try {
                Short parsed = Short.parseShort(orderId);
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
        if (numberOfBags.trim().isEmpty()) {
            return false;
        } else {
            try {
                Short parsed = Short.parseShort(numberOfBags);
                if (parsed < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
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

    public String getOrderId() {
        return orderId;
    }

    public String getLot() {
        return lot;
    }

    public String getFlightScheduledDateTime() {
        return flightScheduledDateTime;
    }

    public String getNumberOfBags() {
        return numberOfBags;
    }

    @Override
    public String toString() {
        return "RetrievalOrderMessageData [receivedMessage=" + receivedMessage + ", header=" + header + ", orderId="
                + orderId + ", lot=" + lot + ", flightScheduledDateTime=" + flightScheduledDateTime + ", numberOfBags="
                + numberOfBags + "]";
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
        RetrievalOrderMessageData other = (RetrievalOrderMessageData) obj;
        return Objects.equals(flightScheduledDateTime, other.flightScheduledDateTime)
                && Objects.equals(header, other.header) && Objects.equals(lot, other.lot)
                && Objects.equals(numberOfBags, other.numberOfBags) && Objects.equals(orderId, other.orderId)
                && Objects.equals(receivedMessage, other.receivedMessage);
    }

}
