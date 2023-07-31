package com.daifukuoc.wrxj.custom.ebs.jdbc;

/**
 * General constants.
 *
 * @author A.D.
 * @since 08-May-2017
 */
public interface EBSConstants {
	/*
	 * JVM split Key for Controller Config.
	 */
	String ASSIGNEDTOJVM = "AssignedToJVM";

	public static final String EMPTY_TRAY_STACK = "Empty_Tray_Stack";
	//TODO: check following and remove unwanted 
	// BCS <-> WRx Message ID Definitions
	   public static final int BCS_KEEPALIVE_MSG				= 1;	// BCS Keepalive
	   public static final int BCS_TR_REQ_MSG					= 10;	// BCS Tray Release Request
	   public static final int BCS_TR_RESP_MSG					= 10;	// BCS Tray Release Request Response
	   public static final int BCS_TR_ARV_CONF_MSG				= 16;	// BCS Tray Arrival Confirmation
	   public static final int BCS_SSSU_MSG						= 17;	// BCS Storage Station Status Update
	   public static final int BCS_A_REQ_MSG					= 18;	// BCS Aisle Request
	   public static final int BCS_A_RESP_MSG					= 18;	// BCS Aisle Request Response
	   public static final int BCS_TR_DEST_MSG					= 20;	// BCS Tray Destination
	   
	   public static final int BCS_OSS_REQ_MSG					= 55;	// BCS OutputStationStatus Request
	   public static final int BCS_OSS_RESP_MSG					= 55;	// BCS OutputStationStatus Response
	   public static final String BCS_OSS_ENABLED				= "1";	// Enabled
	   public static final String BCS_OSS_DISABLED				= "2";	// Enabled
	   
	   
	   // BCS tray status values
	   public static final int BCS_TRAY_EMPTY					= 0;	// BCS tray empty
	   public static final int BCS_TRAY_OCCUPIED				= 1;	// BCS tray occupied
	   public static final int BCS_TRAY_STACK					= 13;	// BCS tray stack
	   public static final int BCS_TRAY_UNKNOWN 				= 9;	// BCS tray unknown
	   public static final int BCS_TRAY_REJECT	 				= 5;	// BCS tray lower reject

	   // BCS deviceid to receive tray request response
	   public static final String BCS_TRAY_EMPTY_DEST					= "2";	// BCS tray empty destination
	   
	   // BCS deviceid to receive tray request response
	   public static final String BCS_DEVICEID					= "BCSSF";
	   public static final String BCS_SMARTFLOW_ALLOCATOR		= "SFAllocator";
	
	// END OF TODO
	   
	   
	public static final int EMPTY_TRAY_STACK_QTY = 3;
	public static final String EBS_UNKNOWN_ITEM = "Unknown BagId";
	public static final String EBS_UNKNOWN_LOT = "Unknown Flight";
	public static final String EBS_BADREAD_ITEM = "BadRead Item";
	public static final String EBS_DUPLICATE_TRAY = "Duplicate Tray";
	public static final String EBS_DUPLICATE_BAGID = "Duplicate BagId";
	public static final String EMPTY_TRAY_STACK_DESC = "SmartFlow Empty Tray Order";
	public static final String BAGSTAGE_BAG_DESC = "BagStage Bag Order";
	public static final String ANY_BAG_IN_FN = "ANY_BAG_IN_FN";
	public static final int EMPTY_TRAY_STACK_PRIORITY = 5;
	public static final int EMPTY_TRAY_STACK_DEST = 101;
	public static final int EMPTY_TRAY_STACK_HEIGHT = 2;
	public static final int UNKNOWN_ITEM_HEIGHT = 2;

	public static final int BAGSTAGE_DEFAULT_ER_QTY = 1;
	public static final int TRAY_STATUS_UNKNOWN = 2;
	public static final int SMARTFLOW_DEFAULT_ER_QTY = 3;
	public static final int TRAY_STATUS_REJECT_TO_LOWER = 5;
	public static final int TRAY_STATUS_EMPTYTRAYSTACK = 13;

	public static final String EBS_SYSTEM = "EBSC";
	public static final String BAGSTAGE_HOSTNAME = "SAC";

	public static final String LOAD_ARRIVAL_BR = "BR";

	public static final int FLUSH_PRIORITY = 5;
	public static final String FLUSH_DESTINATION = "PURG";

	public static final String REJECT_STATION = "4";
	public static final String LOWER_REJECT_STATION = "1";

	public static final int MAX_ER_TO_DELETE_ATATIME = 9;

	// DK:30148 - Fetch location for the given retrieval date time of expected
	// System config parameter values

	public static final class SYSCONFIG_CONSTANTS {

		public static final class LOCATION_GROUP {

			public static final String GROUP_NAME = "LocationGroup";

			public static final class PARAM_NAME {
				public static final String STANDARD = "LOC_COUNT_STANDARD";

				public static final String OOG = "LOC_COUNT_OOG";
			}

			public static final class DEFAULT_LOC_COUNT {
				public static final int STANDARD = 25;

				public static final int OOG = 25;
			}
		}

		public static final class TIME_SLICE_GROUP {

			public static final String GROUP_NAME = "TimeSliceGroup";

			public static final class TIME_SLICE {
				public static final class PARAM_NAME {
					public static final String HOUR = "TIME_SLICE_HOUR";

					public static final String MIN = "TIME_SLICE_MIN";
				}

				public static final class DEFAULT {
					public static final String HOUR = "01";

					public static final String MIN = "00";
				}
			}
		}

	}

	public static String TIME_SEPARATOR = ":";

	public static String LOG_FILE_NAME_SEPARATOR = TIME_SEPARATOR;
}
