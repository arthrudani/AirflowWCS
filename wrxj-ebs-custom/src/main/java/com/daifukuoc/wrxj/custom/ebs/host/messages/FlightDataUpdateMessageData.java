package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class FlightDataUpdateMessageData extends AbstractSKDCData {
    // The received whole message
    private String receivedMessage = "";
    // Header
    SACMessageHeader header = new SACMessageHeader();
    // Fields in the body
    private String flightNumber = "";
    private String flightScheduledDateTime = "";
    private String defaultRetrievalDateTime = "";
    private String finalSortLocation = "";

    public boolean parse(String receivedMessage) {
        this.receivedMessage = receivedMessage;

        if (receivedMessage != null && !receivedMessage.isEmpty()) {
            String[] splitedMsg = receivedMessage.split(",");
            if (splitedMsg != null && splitedMsg.length == SACControlMessage.FLIGHT_DATA_UPDATE_SPLIT_LEN) {

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
                flightNumber = splitedMsg[8];
                flightScheduledDateTime = splitedMsg[9];
                defaultRetrievalDateTime = splitedMsg[10];
                finalSortLocation = splitedMsg[11];

                return isValid();
            }
        }

        return false;
    }
   
    /**
     * Validate the parsed flight data update message
     * 
     * @return true if valid, false if not
     */
    public boolean isValid() {

        if (flightNumber.trim().isEmpty()) {
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
        if (defaultRetrievalDateTime.trim().isEmpty()) {
            return false;
        } else {
            try {
                LocalDateTime.parse(defaultRetrievalDateTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            } catch (Exception e) {
                return false;
            }
        }
        if (finalSortLocation.trim().isEmpty()) {
            return false;
        } else {
            try {
                Integer parsed = Integer.parseInt(finalSortLocation);
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

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getFlightScheduledDateTime() {
        return flightScheduledDateTime;
    }

    public String getDefaultRetrievalDateTime() {
        return defaultRetrievalDateTime;
    }

    public String getFinalSortLocation() {
        return finalSortLocation;
    }
    
    @Override
    public String toString() {
        return "FlightDataUpdateMessageData [receivedMessage=" + receivedMessage + ", header=" + header
                + ", flightNumber=" + flightNumber + ", flightScheduledDateTime=" + flightScheduledDateTime
                + ", defaultRetrievalDateTime=" + defaultRetrievalDateTime + ", finalSortLocation=" + finalSortLocation
                + "]";
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
        FlightDataUpdateMessageData other = (FlightDataUpdateMessageData) obj;
        return Objects.equals(defaultRetrievalDateTime, other.defaultRetrievalDateTime)
                && Objects.equals(finalSortLocation, other.finalSortLocation)
                && Objects.equals(flightNumber, other.flightNumber)
                && Objects.equals(flightScheduledDateTime, other.flightScheduledDateTime)
                && Objects.equals(header, other.header) && Objects.equals(receivedMessage, other.receivedMessage);
    }
}
