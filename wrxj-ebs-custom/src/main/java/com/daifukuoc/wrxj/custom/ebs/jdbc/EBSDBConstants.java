package com.daifukuoc.wrxj.custom.ebs.jdbc;

import com.daifukuamerica.wrxj.jdbc.DBConstants;

/**
 * DBConstants file for Warsaw EBS project.
 *
 * @author M.M.
 * @since Sep-2019
 */
public interface EBSDBConstants extends DBConstants {


	/** Following is for iLocSeqMethod */
	final int PRIMARYSEQ = 1;
	final int SECONDARYSEQ = 2;
	final String DEFAULT_ALLOCATOR = "Allocator";

// DK:30148 - Fetch location for the given retrieval date time of expected
		public static final class Location {
			/**
			 * - Manages whether it is the key that must be registered in the system
			 * operation.
			 */
			public static final class LOCATION_TYPE {
				/** 19:Standard */
				public static final int STANDARD = 19;

				/** 20:Oversize */
				public static final int OVER_SIZE = 20;
			}
			/**
			 * Location empty status field
			 */
			public static final class EMPTY_FLAG {
				/** 21:Empty */
				public static final int EMPTY = 21;
				/** 22:Full */
				public static final int FULL = 22;
				/** 23:Atleast one occupied or not full but occupied */
				public static final int ATLEAST_ONE_OCCUPIED = 23;
				/** 23:Reserved */
				public static final int RESERVED = 23;
			}
			/**
			 * Location status
			 */
			public static final class LOCATION_STATUS {
				/** 29:Available */
				public static final int AVAILABLE = 29;
			}
		}
		/**
		 * Load table
		 */
		public static final class Load {
			/**
			 * - Manages whether it is the key that must be registered in the system
			 * operation.
			 */
			public static final class MOVE_STATUS {
				/** 222: Retrieval Pending */
				public static final int RETRIEVE_PENDING = 222;

				/** 223:Reserved for storage */
				public static final int RESERVED_FOR_STORAGE = 223;
				
				/** 224:Occupied or Stored */
				public static final int STORED = 224;
			}
		}

		/**
		 * Transaction history table
		 */
		public static final class TransactionHistory {
			/**
			 * Transaction category
			 */
			public static final class CATEGORY {
				/** 770: Load transaction */
				public static final int LOAD_TRAN = 770;
				public static final int ORDER_TRAN = 771;
				public static final int USER_TRAN = 772;
				public static final int INVENTORY_TRAN = 773;
				public static final int SYSTEM_TRAN = 774;
			}
			
			/**
			 * Transaction type
			 */
			public static final class TRANSACTION_TYPE {
				/** 881: Load storage completion */
				public static final int STORAGE_COMPLETION = 881;
			}
		}
		
		//DK:31176
		/**
		 * Transaction history table
		 */
		public static final class WrxToPlc {
			public static final class SEQUENCE {
				public static final int PORT_SEQ                      = 373;
			}
		}
		
		public static final class PlcToWrx {
			public static final String PLC_PORTNAME = "PLC";
		}
		
		public static final class MSG_ACKNOWLEDGEMENT_STATUS
		{
			public static final int  OK = 0; // no issue
			public static final int  ERORR = 1; //Error in message content
			public static final int  SERIAL_ERORR = 2; // out of sequence error
		}
		
		public static final class DELETE_REASON_CODE
		{
			public static final String  RELEASED = "0"; // item released
			public static final String  REJECTED = "1"; // item rejected or removed
 
		}
}
