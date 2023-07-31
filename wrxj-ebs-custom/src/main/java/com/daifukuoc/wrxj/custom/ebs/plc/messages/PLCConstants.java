package com.daifukuoc.wrxj.custom.ebs.plc.messages;

/**
 * Constants file for PLC message modules.
 * 
 * @author Administrator
 *
 */
public class PLCConstants {

    public static final short KEEPALIVE_RESP = 1;// BCS KeepAlive Response Value
    public static final short MESSAGE_VER = 1;// KR: configure this in db and get it in startup
    public static final short EBSC_EQUIPID = 111; // BCS EBSC Equipment ID (this is SAC id header :TODO)
    public static final int MAX_MSG_LEN = 250; // Max Msg length
    public static final int SEQNO_FIELD_LEN = 2; // msg Sequence # Field length
    public static final int MSG_BYTE_LEN = 1; // byte size
    public static final int MSG_DWORD_LEN = 4; // byte size
    public static final int MSG_WORD_LEN = 2; // byte size
    public static final int MSG_HEADER_LEN = 16;// Header length
    public static final int MSG_HEADER_LEN_WITH_STX = 17;// Header length with STX
    public static final int BAG_BARCODE_LEN = 12; // it is Char[12] BagID/IATA Code
    public static final int FLIGHT_NUMBER_LEN = 8; // it is Char[9] Flight number
    public static final int FLIGHT_SCHEDULED_DATE_LEN = 14; // it is Char[14] Flight scheduled date time
    // KeepAlive message
    public static final short KEEPALIVE_MSG_TYPE = 1; // PLC <-> WCS
    public static final short KEEPALIVE_MSG_ACTIVE_VAL = 1; // 1= Active;0=NotActive
    public static final short KEEPALIVE_MSG_BODY_LEN = 2;

    public static final String PLC_LINK_STARTUP_MSG_TYPE = "99"; // No message body PLC <-> WCS
    public static final short PLC_LINK_STARTUP_MSG_TYPE_INT = 99;
    public static final short PLC_LINK_STARTUP_MSG_BODY_LEN = 0;
    
    public static final String PLC_LINK_STARTUP_ACK_MSG_TYPE = "199";
    public static final short PLC_LINK_STARTUP_ACK_MSG_TYPE_INT = 199;
    public static final short PLC_LINK_STARTUP_MSG_ACK_BODY_LEN = 2;
    public static final short PLC_LINK_STARTUP_MSG_ACK_STATUS = 0;
    

    // Storage Complete / Stored msg send by PLC to WCS to notify that the bag is stored in ASRS
    public static final String PLC_ITEM_STORED_MSG_TYPE = "52"; // PLC -> WCS
    public static final short PLC_ITEM_STORED_MSG_TYPE_INT = 52;
    public static final short PLC_ITEM_STORAGE_COMPLETE_MSG_BODY_LEN = 30;
    public static final String PLC_ITEM_STORED_ACK_MSG_TYPE = "152"; // WCS -> PLC
    public static final short PLC_ITEM_STORED_ACK_MSG_TYPE_INT = 152; // WCS -> PLC
    public static final short PLC_ITEM_STORED_ACK_BODY_LEN = 2;
    public static final int PLC_ITEM_STORED_BIN_FULL_ERROR = 3;
    public static final int PLC_ITEM_STORED_ERROR = 2;

    // Flush message used by Airflow WCS to request PLC to flush the a lane
    public static final String PLC_FLUSH_REQUEST_MSG_TYPE = "3";// WCS -> PLC
    public static final short PLC_FLUSH_REQUEST_MSG_TYPE_INT = 3;
    public static final short PLC_FLUSH_REQUEST_MSG_BODY_LEN = 12;
    public static final short PLC_FLUSH_REQUEST_MSG_BODY_QTY = 0;
    public static final short PLC_FLUSH_REQUEST_MSG_BODY_RELEASE_INTVL = 0;

    // Flush Ack message
    public static final String PLC_FLUSH_REQUEST_ACK_MSG_TYPE = "103";// PLC -> WCS
    public static final short PLC_FLUSH_REQUEST_ACK_MSG_TYPE_INT = 103;
    public static final short PLC_FLUSH_REQUEST_ACK_MSG_BODY_LEN = 2;

    // TODO: Remove this PLC_ITEM_RELEASED_MSG_TYPE
    // Item Released message sends by PLC to WCS to notify that an item/tray/bag is released
    public static final String PLC_ITEM_PICKEDUP_MSG_TYPE = "53"; // PLC -> WCS
    public static final short PLC_ITEM_PICKEDUP_MSG_TYPE_INT = 53;
    public static final short PLC_ITEM_PICKEDUP_MSG_BODY_LEN = 34;
    public static final String PLC_ITEM_PICKEDUP_ACK_MSG_TYPE = "153"; // WCS -> PLC
    public static final short PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT = 153; // WCS -> PLC

    // Location Status message sends by PLC to WCS to notify
    public static final String PLC_LOCATION_STATUS_MSG_TYPE = "60"; // PLC -> WCS
    public static final short PLC_LOCATION_STATUS_MSG_TYPE_INT = 60;
    public static final short PLC_LOCATION_STATUS_MSG_BODY_LEN = 255; // KR: TODO: Read dynamically from the header
    public static final String PLC_LOCATION_STATUS_ACK_MSG_TYPE = "160"; // KR: TODO: Read dynamically from the header
    public static final short PLC_LOCATION_STATUS_ACK_MSG_TYPE_INT = 160; // KR: TODO: Read dynamically from the header

    // Item arrival message sends by PLC to WCS to notify that item arrived at the station
    public static final String PLC_ITEM_ARRIVED_MSG_TYPE = "51"; // PLC -> WCS
    public static final short PLC_ITEM_ARRIVED_MSG_TYPE_INT = 51;
    public static final short PLC_ITEM_ARRIVED_MSG_BODY_LEN = 28;

    public static final String PLC_ITEM_ARRIVED_ACK_MSG_TYPE = "151"; // WCS -> PLC
    public static final short PLC_ITEM_ARRIVED_ACK_MSG_TYPE_INT = 151;
    public static final short PLC_ITEM_ARRIVED_ACK_BODY_LEN = 2;

    // Standard ACK body len
    public static final short PLC_STANDARD_ACK_BODY_LEN = 2;

    // Move Order request
    public static final String PLC_MOVE_ORDER_REQUEST_MSG_TYPE = "2"; // WCS -> PLC
    public static final short PLC_MOVE_ORDER_REQUEST_MSG_TYPE_INT = 2;
    public static final short PLC_MOVE_ORDER_REQUEST_MSG_BODY_LEN = 60;

    public static final String PLC_MOVE_ORDER_ACK_MSG_TYPE = "102"; // WCS -> PLC
    public static final short PLC_MOVE_ORDER_ACK_MSG_TYPE_INT = 102;
    public static final short PLC_MOVE_ORDER_ACK_MSG_BODY_LEN = 2;

    // Flight Data Update
    public static final String PLC_FLIGHT_DATA_UPDATE_MSG_TYPE = "9"; // WCS -> PLC
    public static final short PLC_FLIGHT_DATA_UPDATE_MSG_TYPE_INT = 9;
    public static final short PLC_FLIGHT_DATA_UPDATE_MSG_BODY_LEN = 12;
    public static final int PLC_FLIGHT_DATA_UPDATE_MSG_SPLIT = 4;
    
    public static final String PLC_FLIGHT_DATA_UPDATE_ACK_MSG_TYPE = "109"; // PLC ->  WCS
    public static final short PLC_FLIGHT_DATA_UPDATE_ACK_MSG_TYPE_INT = 109;
    public static final short PLC_FLIGHT_DATA_UPDATE_ACK_MSG_BODY_LEN = 2;

    // Location Status Values
    public static final short PLC_LOCATION_STATUS_HEALTHY = 0; // Healthy: no mechanical / controls issues
    public static final short PLC_LOCATION_STATUS_FULL = 1; // Full : all trays have indexed up to the entry point:
                                                            // there is no more room to insert more trays
    public static final short PLC_LOCATION_STATUS_EMPTY = 2; // Empty: PLC has flushed the lane and have run the
                                                             // conveyors long enough to be sure that the lane is empty
    public static final short PLC_LOCATION_STATUS_NO_ENTRY_STORAGE = 3; // Entry Not Available for a storage : Issue
                                                                        // with the entry diverter
    public static final short PLC_LOCATION_STATUS_NO_EXIT_STORAGE = 4; // Exit Not Available for a storage : Issue with
                                                                       // the exit diverter
    public static final short PLC_LOCATION_STATUS_FLUSH = 5; // Flushing : Lane currently flushing
    public static final short PLC_LOCATION_STATUS_SCADA_OVERRIDE = 6; // SCADA Override : Set by SCADA to manual mode
    public static final short PLC_LOCATION_STATUS_DISABLED = 7; // Disabled : Disable by SCADA (due to mode change
                                                                // half/full)

    public static final short PLC_LOCATION_STATUS_VALUE_COUNT = 7; // Total Count Status
    
    // Bag Data Update msg
    public static final String PLC_BAG_DATA_UPDATE_MSG_TYPE = "10"; // WCS -> ACP
    public static final short PLC_BAG_DATA_UPDATE_MSG_TYPE_INT = 10;
    public static final short PLC_BAG_DATA_UPDATE_MSG_BODY_LEN = 38;
    
    // Bag Data Update Ack msg
    public static final String PLC_BAG_DATA_UPDATE_ACK_MSG_TYPE = "110"; // ACP -> WCS
    public static final short PLC_BAG_DATA_UPDATE_ACK_MSG_TYPE_INT = 110;
    public static final short PLC_BAG_DATA_UPDATE_ACK_MSG_BODY_LEN = 2;
    
    // DK:31176:Storing PLC message in the table.
    public static final String PLC_MESSAGE_DELIM = ",";
    public static final String DELIM_COMMA = ",";

    // Item Released message sends by PLC to WCS to notify that item arrived at the station
    public static final String PLC_ITEM_RELEASED_MSG_TYPE = "54"; // PLC -> WCS
    public static final short PLC_ITEM_RELEASED_MSG_TYPE_INT = 54;
    public static final short PLC_ITEM_RELEASED_MSG_BODY_LEN = 28;

    public static final String PLC_ITEM_RELEASED_ACK_MSG_TYPE = "154"; // WCS -> PLC
    public static final short PLC_ITEM_RELEASED_ACK_MSG_TYPE_INT = 154;
    public static final short PLC_ITEM_RELEASED_ACK_BODY_LEN = 2;
    
    public static final String DATE_FORMAT = "yyyyMMddHHmmss";

    public enum AckStatus {
        OK((short) 0), MESSAGE_ERROR((short) 1), SEQUENCE_NUMBER_ERROR((short) 2);

        private short value;
        
        AckStatus(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }
    }

    public static final class MoveOrder {
        /**
         * - Manages whether it is the key that must be registered in the system operation.
         */
        public static final class MOVE_TYPE {
            /** 0:station to station */
            public static final int DIRECT = 0; // station to station
            /** 1:location to location */
            public static final int INTER_LOCATION = 1; // location to location
            /** 2:station to location */
            public static final int STORAGE = 2; // station to location
            /** 3:location to station */
            public static final int RETRIEVAL = 3; // location to location
        }
    }

    public static final int PLC_HEADER_OFFSET_MESSAGE_LENGTH = 0;
    public static final int PLC_HEADER_OFFSET_SEQUENCE_NUMBER = 2;
    public static final int PLC_HEADER_OFFSET_MESSAGE_TYPE = 4;
    public static final int PLC_HEADER_OFFSET_DEVICE_ID = 6;
    public static final int PLC_HEADER_OFFSET_HOURS = 10;
    public static final int PLC_HEADER_OFFSET_MINUTES = 11;
    public static final int PLC_HEADER_OFFSET_MILLISECONDS = 12;
    public static final int PLC_HEADER_OFFSET_MESSAGE_VERSION = 14;
    
    public static final String UNKNOWN_BARCODE = "UNKOWNBC";
    public static final String UNKNOWN_ITEM = "Unknown_Item";
    
}
