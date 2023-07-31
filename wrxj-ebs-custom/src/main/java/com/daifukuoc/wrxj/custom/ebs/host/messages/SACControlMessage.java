package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.device.controls.ControlsMessageInterface;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusInfo;

public class SACControlMessage implements ControlsMessageInterface {
	public static final String HOST_PORT_EVENT = "HostPort";
	public static final String COMMA =",";
	public static final String DEFAULT_WAREHOUSE_ID = "WHS"; // EBS ASRS South Warehouse
	public static final String TIME_FORMAT = "HH:mm";
	public static final String DATE_FORMAT = "yyyyMMddHHmmss";
	public static final int MSG_BYTE_LEN = 1; // byte size
	public static final int MSG_DWORD_LEN = 4; // byte size
	public static final int MSG_WORD_LEN = 2; // byte size
	public static final int MSG_HEADER_LEN = 16;// Header length
	public static final int MAX_SEQNO = 32767; // short range -32,768 to 32,767
	public static final int BAG_BARCODE_LEN = 12; // it is Char[12] BagID/IATA Code
	public static final int FLIGHT_NUM_LEN = 8; // it is Char[8] Flight Number (example: QFA 1234A)
	public static final int WAREHOUSE_NAME_LEN = 3; // it is Char[3] Warehouse Name (example : EBS)
	public static final int FLIGHT_SCHEDULED_DATETIEM_LEN = 14; // it is Char[14] Flight Scheduled Date Time - STD
																// (YYYYMMDDHHMMSS-20221201134500)
	public static final int DEFAULT_RETRIEVAL_DATETIEM_LEN = 14; // it is Char[14] Default Retrieval Date Time
																	// (YYYYMMDDHHMMSS-20221201134500)
	public static final short TEST_MSG_TYPE = 0; // SAC <-> WCS
	public static final short KEEPALIVE_MSG_TYPE = 1; // SAC <-> WCS
	public static final short KEEPALIVE_MSG_ACTIVE_VAL = 1; // 1= Active;0=NotActive
	public static final short KEEPALIVE_MSG_BODY_LEN = 2;
	public static final short KEEPALIVE_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + KEEPALIVE_MSG_BODY_LEN;
	
	public static final int DEFAULT_ARRAY_OBJECT_LEN = 18; // it is an object array with Tray ID,Global ID, Item ID, Final Sort Location ID,
	
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

	// expected Receipt
	public static final short EXPECTED_RECIEPT_MSG_TYPE = 52; // SAC -> WCS
	public static final short EXPECTED_RECIEPT_MSG_LEN = 84; // header(16) + body(68 = 4 + 4 + 4 + 12 + 8 + 14 + 14 + 4 + 2 + 2)
	public static final short EXPECTED_RECIEPT_SPLETED_LEN = 18;
	
	// expected Receipt ack
    public static final short EXPECTED_RECIEPT_ACK_MSG_TYPE = 152; // WCS -> SAC
    public static final short EXPECTED_RECIEPT_ACK_MSG_LEN = 18; // header(16) + body(2)

	// expected Receipt Response
	public static final short EXPECTED_RECIEPT_RESPONSE_MSG_TYPE = 2; // WCS -> SAC
	public static final short EXPECTED_RECIEPT_RESPONSE_MSG_BODY_LEN = 30; // Body(30)
	public static final short EXPECTED_RECIEPT_RESPONSE_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + EXPECTED_RECIEPT_RESPONSE_MSG_BODY_LEN ; // header(16) + body(30) 
	public static final short EXPECTED_RECIEPT_RESPONSE_SPLETED_LEN = 14;
	
	// expected Receipt Response Ack
    public static final short EXPECTED_RECIEPT_RESPONSE_ACK_MSG_TYPE = 102; // SAC -> WCS
    public static final short EXPECTED_RECIEPT_RESPONSE_ACK_MSG_LEN = 18; // header(16) + body(2)
	
    // flight data update
    public static final short FLIGHT_DATA_UPDATE_MSG_TYPE = 57; // SAC -> WCS
    public static final short FLIGHT_DATA_UPDATE_MSG_LEN = 57; // header(16) + body(41 = 9 + 14 + 14 + 4)
    public static final short FLIGHT_DATA_UPDATE_SPLIT_LEN = 12; // header(8) + body(4)

    // flight data update ack
    public static final short FLIGHT_DATA_UPDATE_ACK_MSG_TYPE = 157; // WCS -> SAC
    public static final short FLIGHT_DATA_UPDATE_ACK_MSG_BODY_LEN = 2;
    public static final short FLIGHT_DATA_UPDATE_ACK_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + FLIGHT_DATA_UPDATE_ACK_MSG_BODY_LEN; // header(16) + body(2)
    public static final short FLIGHT_DATA_UPDATE_ACK_SPLIT_NUM = 4;
	
	// stored complete ack
	public static final short STORED_COMPLETE_ACK_MSG_TYPE = 103;
	public static final short STORED_COMPLETE_ACK_MSG_LEN = 18; // header(16) + body(2)
	public static final short STORED_COMPLETE_ACK_MSG_BODY_LEN = 2;
	
	// Item Release : WCS -> SAC
	public static final short ITEM_RELEASE_MSG_TYPE = 6; // WCS -> SAC
	public static final short ITEM_RELEASE_MSG_LEN = 46; // Body + header
	public static final short ITEM_RELEASE_MSG_BODY_LEN = 30; // Body
	
	// Item Release ack: SAC -> WCS
	public static final short ITEM_RELEASE_ACK_MSG_TYPE = 106; // WCS -> SAC
	public static final short ITEM_RELEASE_ACK_MSG_LEN = 18; // header(16) + body(2)
	public static final short ITEM_RELEASE_ACK_MSG_BODY_LEN = 2; // Body
	
	// Retrieval Order Request  :  SAC -> WCS
	public static final short RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE = 55; // SAC -> WCS
	public static final short RETRIEVAL_FLIGHT_REQUEST_MSG_BODY_LEN = 28; // Body
	public static final short RETRIEVAL_FLIGHT_WITHOUT_LIST_REQUEST_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + RETRIEVAL_FLIGHT_REQUEST_MSG_BODY_LEN; // Body + header
	public static final short RETRIEVAL_FLIGHT_REQUEST_SPLIT_NUM = 12; // header(8) + body(4)
		
	// Retrieval Order ack
    public static final short RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_TYPE = 155; // WCS -> SAC
    public static final short RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_LEN = 18; // header(16) + body(2)
	
	// Retrieval Order Response  :   WCS --> SAC
	public static final short RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE = 5; //  WCS --> SAC
	public static final short RETRIEVAL_FLIGHT_RESPONSE_MSG_BODY_LEN = 8; // body(4 + 2 + 2)
	public static final short RETRIEVAL_FLIGHT_RESPONSE_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + RETRIEVAL_FLIGHT_RESPONSE_MSG_BODY_LEN; // header(16 bytes) + body(2 + 2 + 2)
	
	// Retrieval Order Response Ack
    public static final short RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_TYPE = 105; // SAC -> WCS
    public static final short RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_LEN = 18; // header(16) + body(2)
	
	// Retrieval Order Request Item :  SAC -> WCS
	public static final short RETRIEVAL_ITEM_REQUEST_MSG_TYPE = 54; // SAC -> WCS
	public static final short RETRIEVAL_ITEM_REQUEST_MSG_BODY_LEN = 94; // Body + header
	public static final short RETRIEVAL_ITEM_REQUEST_SPLIT_NUM = 22; //header(8) + body(14)
	//public static final short RETRIEVAL_ORDER_LIST_REQUEST_MSG_BODY_LEN = 27; // Body
	
	// Retrieval Item ack
    public static final short RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE = 154; // WCS -> SAC
    public static final short RETRIEVAL_ITEM_REQUEST_ACK_MSG_LEN = 18; // header(16) + body(2)
    
	
    // Inventory Request By Warehouse
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE = 59; // SAC -> WCS
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_MSG_BODY_LEN = 7; // body(4 + 3)
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_MSG_LEN = 23; // header(8) + body(4)
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_MSG_SPLIT_NUM = 10; // header(8) + body(4)

    // Inventory Request By Warehouse Ack
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE = 159; // WCS -> SAC
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_BODY_LEN = 2;
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_BODY_LEN; // header(16) + body(2)
    public static final short INVENTORY_REQUEST_BY_WAREHOUSE_ACK_SPLIT_NUM = 4;
    
    
	// Retrieval Item Response Item :   WCS --> SAC
	public static final short RETRIEVAL_ITEM_RESPONSE_MSG_TYPE = 4; //  WCS --> SAC
	public static final short RETRIEVAL_ITEM_RESPONSE_MSG_BODY_LEN = 8; // body(4 + 2 + 2)
 	public static final short RETRIEVAL_ITEM_RESPONSE_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + RETRIEVAL_ITEM_RESPONSE_MSG_BODY_LEN; // header(16 bytes) + body(2 + 2 + 2)
 	
 	// Retrieval Item Response Ack
    public static final short RETRIEVAL_ITEM_RESPONSE_ACK_MSG_TYPE = 104; // SAC -> WCS
    public static final short RETRIEVAL_ITEM_RESPONSE_ACK_MSG_LEN = 18; // header(16) + body(2)
    
    // Inventory Request By Flight Request  :  SAC -> WCS
 	public static final short INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE = 58; // SAC -> WCS
 	public static final short INVENTORY_REQUEST_BY_FLIGHT_MSG_BODY_LEN = 26; // Body
 	public static final short INVENTORY_REQUEST_BY_FLIGHT_MSG_LEN = 42; // Body+header
 	public static final short INVENTORY_REQUEST_BY_FLIGHT_SPLIT_NUM = 11; // header(8) + body(3)
 		
 	// Inventory Request By Flight ack
     public static final short INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE = 158; // WCS -> SAC
     public static final short INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_LEN = 18; // header(16) + body(2)
     
 	// Inventory Response Item :   WCS --> SAC
 	public static final short INVENTORY_RESPONSE_MSG_TYPE = 8; //  WCS --> SAC
 	public static final short INVENTORY_RESPONSE_MSG_BODY_LEN = 8; // body(4 + 2 + 2)
  	public static final short INVENTORY_RESPONSE_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + INVENTORY_RESPONSE_MSG_BODY_LEN; // header(16 bytes) + body(4 + 2 + 2)
  	public static final short INVENTORY_RESPONSE_ARRAY_ITEM_LEN = 49; // length of any item in the array
  	
  	// Inventory Response Ack
    public static final short INVENTORY_RESPONSE_ACK_MSG_TYPE = 108; // SAC -> WCS
    public static final short INVENTORY_RESPONSE_ACK_MSG_LEN = 18; // header(16) + body(2)
 	
	
	// Location type, 2 digits, used as a prefix in address value
	public static final String LOCATION_STORAGE_TYPE = "20";
	
	// Item Type
	public static final String Bag_On_Tray = "Bag_On_Tray";
	public static final String OOG_Bag_On_Tray = "OOG_Bag_On_Tray";
	
	public static final byte STX = (byte) 0x02;
	public static final byte ETX = (byte) 0x03;
	
	
	// Inventory update message
    public static final short INVENTORY_UPDATE_MSG_TYPE = 7; // SAC -> WCS
    public static final short INVENTORY_UPDATE_MSG_LEN = 42; // header(16) + body(26 = 4 + 4 + 12 + 4 + 2)
    public static final short INVENTORY_UPDATE_SPLIT_LEN = 14; // header(8) + body(4)

    // Inventory update ack message
    public static final short INVENTORY_UPDATE_ACK_MSG_TYPE = 107; // WCS -> SAC
    public static final short INVENTORY_UPDATE_ACK_MSG_BODY_LEN = 2;
    public static final short INVENTORY_UPDATE_ACK_MSG_LEN = SACControlMessage.MSG_HEADER_LEN + INVENTORY_UPDATE_ACK_MSG_BODY_LEN; // header(16) + body(2)
    public static final short INVENTORY_UPDATE_ACK_SPLIT_NUM = 4;

    
    public static final short STATUS_SUCCESS = 1;
    public static final short STATUS_FAILED = 2;
    public static final short STATUS_COMPLETED_WITH_SHORTAGE = 3;
    
    public static final String LINK_STARTUP_MSG_TYPE = "99"; // No message body PLC <-> WCS
    public static final short  LINK_STARTUP_MSG_TYPE_INT = 99;
    public static final short  LINK_STARTUP_MSG_BODY_LEN = 0;
    
    public static final String LINK_STARTUP_ACK_MSG_TYPE = "199";
    public static final short LINK_STARTUP_ACK_MSG_TYPE_INT = 199;
    public static final short LINK_STARTUP_MSG_ACK_BODY_LEN = 2;
    public static final short LINK_STARTUP_MSG_ACK_STATUS = 0;
    
	public static final class ExpectedReceiptsRequest {

		public static final class REQUEST_TYPE {
			public static final int ADD = 1;
			public static final int UPDATE = 2;
			public static final int CANCEL = 3;
		}
	}
	public static final class ITEM_TYPE {
		public static final int STANDARD = 1;
		public static final int OVERSIZE = 2;
	}

	public static final class ExpectedReceiptsResponse {

		public static final class LOCATION {
			public static final int NOT_FOUND = 0;
		}

		public static final class STATUS {
			public static final int NOT_AVAILABLE = 0;
			public static final int SUCCESS = 1;
			public static final int ERROR = 2;
		}
	}
	
	//DK:28372
	public static final class StoreCompletionNotify {
		
		public static final short MSG_TYPE = 3; // WCS -> SAC
		public static final short MSG_LEN = 48; // Body + header
		public static final short SPLITED_LEN = 13;

		public static final class STATUS {
			/** 1:Success */
			public static final int SUCCESS = 1;

			/** 2:Error */
			public static final int ERROR = 2;
		}
	}
	
	//NHC:28373
	public static final class RetrievalOrderNotify {

			public static final class STATUS {
				/** 1:Success */
				public static final int SUCCESS = 1;

				/** 2:Error */
				public static final int ERROR = 2;
				
				/** 3:Processed (Shortage) */
				public static final int PROCEED = 3;
			}
		}
	
	public static final class ItemRelease {

		public static final class STATUS {
			public static final int NORMAL_RETRIEVAL = 1;
			public static final int OPERATOR_RETRIEVAL = 2;
			public static final int NO_ROOM_TO_STORE = 3;
			public static final int UNKNOWN_TRAY = 4;
			public static final int ERROR_RECOVERY = 5;
		}
	}

	public static int getMessageType(String isReceivedData) {
		if (StringUtils.isBlank(isReceivedData)) {
			return 0;
		}
		if (!isReceivedData.contains(",")) {
			return 0;
		}

		String sMsgType = isReceivedData.substring(0, isReceivedData.indexOf(","));

		if (StringUtils.isBlank(sMsgType)) {
			return 0;
		}
		return Integer.parseInt(sMsgType);
	}

	public static final class InventoryUpdate {

		public static final class STATUS {
			public static final int AUTO_REMOVE = 0;
			public static final int MANUAL_REMOVE = 1;
			public static final int MANUAL_UPDATE = 2;
			public static final int PICKUP_FAILED = 3;
		}
	}
	
	/* KR: existing fileds which need to be REVIEWED ..... */

	// Internal fields
	private String msMessageText = null;
	private boolean mzIsValid = false;
	private String msInvalidReason = null;

	// Message header data fields
	private String msTelegramNumber = " ";
	private String msLength = " ";
	private String msSource = " ";
	private String msDestination = " ";

	// Message body data fields
	private String msData = " "; // Message Body
	private String msMsgTyp = " "; // Message ID

	// Fake equipment status
	private StatusEventDataFormat mpSEDF = new StatusEventDataFormat(getClass().getSimpleName());

	/**
	 * public constructor for Factory
	 */
	public SACControlMessage() {
	}

	/**
	 * Parse and interpret the passed-in message text into fields defined for that
	 * message type.
	 *
	 * @param isMessageString the message to decode
	 */
//  @Override
	public void toDataValues(String isMessageString) {
		try {
			mzIsValid = true;
			msInvalidReason = null;
			msMessageText = isMessageString;

			// Parse the message
			msMsgTyp = isMessageString.substring(0, 2);
		} catch (Exception e) {
			mzIsValid = false;
			msInvalidReason = e.getMessage() + " parsing " + isMessageString;
		}
	}

	/**
	 * Convert the current command to a string for transmission
	 */
//  @Override
	public String getMessageAsString() {
		msMessageText = msTelegramNumber + msLength + msSource + msDestination + msMsgTyp + msData;
		return msMessageText;
	}

	/**
	 * Get the parsed message string
	 */
//  @Override
	public String getParsedMessageString() {
		String s = "";

		return s;
	}

	/**
	 * Is this a valid message?
	 * 
	 * @return
	 */
//  @Override
	public boolean getValidMessage() {
		return mzIsValid;
	}

	/**
	 * Get the description of why the message is invalid
	 * 
	 * @return String if message is invalid, null if message is valid
	 */
//  @Override
	public String getInvalidMessageDescription() {
		return msInvalidReason;
	}

	/**
	 * Initialize Equipment Statuses
	 * 
	 * @param iasStations
	 */
	public void initializeEquipmentStatus(String isDevice) {
		mpSEDF.addEquipmentStatus(isDevice, "Conveyor", isDevice, "Unknown", "Unknown", "*NONE*", "Now");
	}

	/**
	 * Set the equipment status
	 * 
	 * @param isStatus
	 */
//  @Override
	public void setEquipmentStatus(String isStatus) {
		StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
		List<StatusInfo> vpStatuses = mpSEDF.getStatusList();
		for (StatusInfo vpSI : vpStatuses) {
			vpSEDF.addEquipmentStatus(vpSI.getMachineID(), vpSI.getMachineType(), vpSI.getMachineNo(),
					vpSI.getMachineStat(), isStatus, vpSI.getMachineError(), "NOW");
		}
		mpSEDF = vpSEDF;
	}

	/**
	 * Get the equipment status report
	 * 
	 * @return
	 */
	public String getEquipmentStatusReport() {
		mpSEDF.setType(ControllerConsts.EQUIPMENT_STATUS);
		return mpSEDF.createStringToSend();
	}

	/* ======================================================================== */
	/* Message Field Getters */
	/* ======================================================================== */
	public String getMessageID() {
		return msMsgTyp;
	}

	public String getTelegramNumber() {
		return msTelegramNumber;
	}

	public String getLength() {
		return msLength;
	}

	public String getSource() {
		return msSource;
	}

	public String getDestination() {
		return msDestination;
	}

	public String getMsgTyp() {
		return msMsgTyp;
	}

	public String getData() {
		return msData;
	}

	// -----------------------

}
