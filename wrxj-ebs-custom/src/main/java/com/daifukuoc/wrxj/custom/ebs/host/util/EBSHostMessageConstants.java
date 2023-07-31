package com.daifukuoc.wrxj.custom.ebs.host.util;

public interface EBSHostMessageConstants {
	// TODO @DK we can delete this class as other class can be used for this purpose
	// this is staying for sample purpose
	// DK:30075 - Creating timeslot for the expected receipt.
	public static final class ExpectedReceiptsRequest {

		public static final class REQUEST_TYPE {
			/** 1:New */
			public static final int NEW = 1;

			/** 2:Update */
			public static final int UPDATE = 2;

			/** 3:Cancel */
			public static final int CANCEL = 3;
		}
	}

	// DK:30294 - Send expected receipt response msg to host
	public static final class ExpectedReceiptsResponse {

		public static final class LOCATION {
			/** 0:Not found */
			public static final int NOT_FOUND = 0;
		}

		public static final class STATUS {
			/** 0:Not available */
			public static final int NOT_AVAILABLE = 0;

			/** 1:Success */
			public static final int SUCCESS = 1;

			/** 2:Error */
			public static final int ERROR = 2;
		}
	}
	
	public static final String WAREHOUSE_NAME = "EBS"; // the default warehouse ID
}
