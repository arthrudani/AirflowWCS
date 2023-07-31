package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class ExpectedReceiptMessageData extends AbstractSKDCData {
    public static final int INVALID_REQUEST_TYPE = -1;

    SACMessageHeader header = new SACMessageHeader();

    String message = ""; // Whole message
    String orderId = ""; // Order Id
    String loadId = ""; // Tray Id
    String globalId = ""; // Global Id
    String lineId = ""; // Item Id, Bag Id, Barcode
    String lot = ""; // Flight number
    String flightScheduledDateTime = "";
    String defaultRetrievalDateTime = "";
    String finalSortLocation = "";
    String itemType = ""; // item (Bag_On_Tray or OOG_Bag_On_Tray)
    int requestType = INVALID_REQUEST_TYPE; // Request Type (Add = 1, Update = 2, Cancel = 3)

    public boolean parse(String sMsg) {
        // Example: 84,1000,52,2222,3,4,5,0,999,1002,10021002,BAG1002,FL100,20221213000000,20221213000000,3600,1,1
        // - Message length in bytes including header: 84
        // - Serial Number: 1000
        // - Message type: 52
        // - Equipment ID: 2222
        // - Timestamp, hours: 3
        // - Timestamp, minutes: 
        // - Timestamp, milliseconds: 5
        // - Message version number: 0
        // - Order ID: 999
        // - Tray ID: 1002
        // - Global ID: 10021002
        // - Bag ID: BAG1002
        // - Flight Number: FL100
        // - Flight Scheduled Date Time: 20221213000000
        // - Default Retrieval Date Time: 20221213000000
        // - Final Sort Location: 3600
        // - Item Type: 1
        // - Request Type:1
        message = sMsg;

        if (!StringUtils.isBlank(sMsg)) {
            String[] splitedMsg = sMsg.split(",");
            if (splitedMsg != null && splitedMsg.length == SACControlMessage.EXPECTED_RECIEPT_SPLETED_LEN) {
                header.setMsgLength(Integer.parseInt(splitedMsg[0]));
                header.setSeqNo(Integer.parseInt(splitedMsg[1]));
                header.setMsgType(Short.parseShort(splitedMsg[2]));
                header.setEquipmentId(splitedMsg[3]);
                header.setHours(Integer.parseInt(splitedMsg[4]));
                header.setMinutes(Integer.parseInt(splitedMsg[5]));
                header.setMilliSeconds(Integer.parseInt(splitedMsg[6]));
                header.setMsgVersion(Integer.parseInt(splitedMsg[7]));

                orderId = splitedMsg[8]; // Order Id
                loadId = splitedMsg[9]; // tray-ContainerId
                globalId = splitedMsg[10]; // Global Id
                lineId = splitedMsg[11]; // sBarcode
                lot = splitedMsg[12]; // FlightNo
                flightScheduledDateTime = splitedMsg[13]; // sFlightSchedule
                defaultRetrievalDateTime = splitedMsg[14]; // sDefaultRetrievalDateTime
                finalSortLocation = splitedMsg[15]; // sFinalSortLocationId
                itemType = (splitedMsg[16].equals("2") ? SACControlMessage.OOG_Bag_On_Tray
                        : SACControlMessage.Bag_On_Tray);
                try {
                    requestType = Integer.parseInt(splitedMsg[17]);
                } catch (NumberFormatException e) {
                }

                return isValid();
            }
        }

        return false;
    }

    // DK:30075 - Creating timeslot for the expected receipt.
    // Return whether the bag in the expected receipt is OverSize bag.
    public boolean isOOGBag() {
        boolean isOOGBag = false;

        if (itemType != null && itemType.equals(SACControlMessage.OOG_Bag_On_Tray)) {
            isOOGBag = true;
        }

        return isOOGBag;
    }

    /**
     * Validate the parsed expected receipt message
     * 
     * @return true if valid, false if not
     */
    public boolean isValid() {
        if (orderId.trim().isEmpty()) {
            return false;
        } else {
            try {
                Integer parsed = Integer.parseInt(orderId);
                if (parsed < 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
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
        if (itemType.trim().isEmpty()) {
            return false;
        } else if (!itemType.trim().equals(SACControlMessage.Bag_On_Tray)
                && !itemType.trim().equals(SACControlMessage.OOG_Bag_On_Tray)) {
            return false;
        }
        if (requestType != SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.ADD
                && requestType != SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.UPDATE
                && requestType != SACControlMessage.ExpectedReceiptsRequest.REQUEST_TYPE.CANCEL) {
            return false;
        }

        return true;
    }

    public SACMessageHeader getHeader() {
        return header;
    }

    public String getMessage() {
        return message;
    }

    public String getOrderId() {
        return orderId;
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

    public String getLot() {
        return lot;
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

    public String getItemType() {
        return itemType;
    }

    public int getRequestType() {
        return requestType;
    }

    @Override
    public String toString() {
        return "ExpectedReceiptMessageData [header=" + header + ", message=" + message + ", orderId=" + orderId
                + ", loadId=" + loadId + ", globalId=" + globalId + ", lineId=" + lineId + ", lot=" + lot
                + ", flightScheduledDateTime=" + flightScheduledDateTime + ", defaultRetrievalDateTime="
                + defaultRetrievalDateTime + ", finalSortLocation=" + finalSortLocation + ", itemType=" + itemType
                + ", requestType=" + requestType + "]";
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
        ExpectedReceiptMessageData other = (ExpectedReceiptMessageData) obj;
        return Objects.equals(defaultRetrievalDateTime, other.defaultRetrievalDateTime)
                && Objects.equals(finalSortLocation, other.finalSortLocation)
                && Objects.equals(flightScheduledDateTime, other.flightScheduledDateTime)
                && Objects.equals(globalId, other.globalId) && Objects.equals(header, other.header)
                && Objects.equals(itemType, other.itemType) && Objects.equals(lineId, other.lineId)
                && Objects.equals(loadId, other.loadId) && Objects.equals(lot, other.lot)
                && Objects.equals(message, other.message) && Objects.equals(orderId, other.orderId)
                && requestType == other.requestType;
    }
}
