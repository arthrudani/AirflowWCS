package com.daifukuamerica.wrxj.jdbc;

/**
 * Database constants and translation values.
 * <P>
 * This file is automatically generated.  Please do not edit!
 * To change this file, modify jwmsadd.sql and run "ant db-tran" to regenerate
 * this file.
 * </P>
 */
public interface DBConstants
{
  /*------------------------------------------*
   *          Non-DDL constants.              *
   *------------------------------------------*/
  final int WRITELOCK               = 666;
  final int NOWRITELOCK             = 667;
  final int LNBANK                  = 3;
  final int LNBAY                   = 3;
  final int LNTIER                  = 3;
  final int LNHEIGHT                = 2;
  final int LNAISLEGROUP            = 2;


  /*------------------------------------------*
   *          Translation constants.          *
   *------------------------------------------*/
  final int ADD                           = 880;
  final int ADDING_RETRIEVAL              = 3;
  final int ADD_EXPECTED_RECEIPT          = 908;
  final int ADD_EXPECTED_RECEIPT_LINE     = 911;
  final int ADD_ITEM                      = 894;
  final int ADD_ITEM_MASTER               = 905;
  final int ADD_LOAD                      = 893;
  final int ADD_ORDER                     = 892;
  final int ADD_ORDER_LINE                = 902;
  final int AGC                           = 186;
  final int AGC9X                         = 188;
  final int AGC_TRANSFER                  = 230;
  final int AGV                           = 190;
  final int AGV_MOVECANCELED              = 267;
  final int AGV_MOVECANCELERROR           = 269;
  final int AGV_MOVECANCELPENDING         = 270;
  final int AGV_MOVECANCELREQUEST         = 268;
  final int AGV_MOVECANCELSENT            = 266;
  final int AGV_MOVECOMPLETE              = 265;
  final int AGV_MOVEERROR                 = 263;
  final int AGV_MOVEPENDING               = 262;
  final int AGV_MOVESENT                  = 261;
  final int AGV_MOVING                    = 264;
  final int AGV_NOMOVE                    = 260;
  final int AGV_RECOVERABLE               = 271;
  final int AGV_STATION                   = 231;
  final int AGV_SYSCMD_COMPLETE           = 143;
  final int AGV_SYSCMD_ERROR              = 144;
  final int AGV_SYSCMD_PENDING            = 142;
  final int AGV_SYSCMD_REQUEST            = 140;
  final int AGV_SYSCMD_SENT               = 141;
  final int ALLOCATENOW                   = 234;
  final int ALLOCATING                    = 235;
  final int ANY_REQUEST                   = 129;
  final int APPOFFLINE                    = 187;
  final int APPONLINE                     = 186;
  final int ARC100                        = 179;
  final int ARRIVED                       = 231;
  final int ARRIVEPENDING                 = 220;
  final int ASSIGNED                      = 33;
  final int AUTOPICK                      = 179;
  final int AUTORECEIVE_BCR               = 180;
  final int AUTORECEIVE_ER                = 176;
  final int AUTORECEIVE_EXPECTED_LOAD     = 181;
  final int AUTORECEIVE_ITEM              = 177;
  final int AUTORECEIVE_LOAD              = 178;
  final int AUTO_MOVE_OFF                 = 174;
  final int AUTO_ORDER_OFF                = 340;
  final int AVAILABLE                     = 32;
  final int BACK_TO_FRONT                 = 303;
  final int BIDIRECT                      = 103;
  final int BINFULL_ERROR                 = 245;
  final int BOTH                          = 175;
  final int BUILDING                      = 241;
  final int CAPTIVE                       = 181;
  final int CAPTIVEINSERT                 = 201;
  final int CARTPICK_REQUEST              = 130;
  final int CA_CUSTOM1                    = 1;
  final int CA_CUSTOM2                    = 2;
  final int CA_CUSTOM3                    = 3;
  final int CA_CUSTOM4                    = 4;
  final int CA_CUSTOM5                    = 5;
  final int CA_CUSTOM6                    = 6;
  final int CA_NORMAL                     = 0;
  final int COMPLETION                    = 881;
  final int CONSOLIDATED                  = 238;
  final int CONSOLIDATING                 = 237;
  final int CONSOLIDATION                 = 226;
  final int CONTAINER                     = 4;
  final int CONVEYOR                      = 227;
  final int CONV_DEVICE                   = 184;
  final int COUNT                         = 882;
  final int CREATOR                       = 56;
  final int CUSTOM                        = 233;
  final int CYCLECOUNT                    = 16;
  final int CYCLECOUNTMOVE                = 63;
  final int CYCLECOUNT_REQUEST            = 127;
  final int CYCLE_COUNT                   = 883;
  final int DELETE                        = 884;
  final int DELETE_EXPECTED_RECEIPT       = 910;
  final int DELETE_EXPECTED_RECEIPT_LINE  = 913;
  final int DELETE_ITEM                   = 900;
  final int DELETE_ITEM_MASTER            = 907;
  final int DELETE_LOAD                   = 899;
  final int DELETE_ORDER                  = 898;
  final int DELETE_ORDER_LINE             = 904;
  final int DELIMITED                     = 430;
  final int DEVICE_SEQ                    = 371;
  final int DISCONNECTED                  = 235;
  final int DLACTIVE                      = 11;
  final int DLINACTIVE                    = 13;
  final int DLUNREPLEN                    = 14;
  final int DLWAIT                        = 12;
  final int DONE                          = 241;
  final int EMPLOYEESTAT                  = 102;
  final int EMPTY                         = 234;
  final int EMPTYMOVE                     = 62;
  final int EMPTY_CONTAINER_ORDER         = 341;
  final int EMPTY_LOCATION_CHECK          = 9;
  final int EQUALS                        = 556;
  final int EQUIPMENT                     = 231;
  final int ERBUILDING                    = 24;
  final int ERCOMPLETE                    = 26;
  final int EREXPECTED                    = 27;
  final int ERFORCED                      = 28;
  final int ERHISTORY                     = 25;
  final int ERPENDING                     = 30;
  final int ERRECEIVING                   = 31;
  final int ERROR                         = 236;
  final int FALSE                         = 236;
  final int FIXED                         = 246;
  final int FIXEDLENGTH                   = 431;
  final int FRONT_TO_BACK                 = 302;
  final int FULL                          = 238;
  final int FULLEMU                       = 198;
  final int FULLLOADOUT                   = 8;
  final int FULLPICK                      = 89;
  final int GS_BOOLEAN                    = 4;
  final int GS_DATETIME                   = 3;
  final int GS_DOUBLE                     = 5;
  final int GS_INTEGER                    = 1;
  final int GS_SELECTION                  = 0;
  final int GS_STRING                     = 2;
  final int HALF                          = 236;
  final int HOLD                          = 230;
  final int HOST_SEQ                      = 370;
  final int IDPENDING                     = 219;
  final int IGNORE                        = 555;
  final int INBOUND                       = 101;
  final int INNER_PACK                    = 90;
  final int INOP                          = 188;
  final int INPUT                         = 224;
  final int INVENTORY_CHECK               = 0;
  final int INVENTORY_TRAN                = 773;
  final int ITEMMOVE                      = 61;
  final int ITEMORDER                     = 1;
  final int ITEM_ORDER                    = 342;
  final int ITEM_PICK                     = 885;
  final int ITEM_RECEIPT                  = 886;
  final int ITEM_SHIP                     = 887;
  final int ITMAVAIL                      = 168;
  final int ITMHOLD                       = 169;
  final int ITMREJECT                     = 170;
  final int JVM_DISABLED                  = 462;
  final int JVM_INUSE                     = 461;
  final int JVM_UNUSED                    = 460;
  final int KILLED                        = 240;
  final int LCASRS                        = 10;
  final int LCAVAIL                       = 29;
  final int LCCONSOLIDATION               = 15;
  final int LCCONVSTORAGE                 = 19;
  final int LCDEDICATED                   = 17;
  final int LCDEVICE                      = 18;
  final int LCFLOW                        = 12;
  final int LCPROHIBIT                    = 31;
  final int LCRECEIVING                   = 14;
  final int LCRESERVED                    = 23;
  final int LCSHIPPING                    = 13;
  final int LCSTAGING                     = 16;
  final int LCSTATION                     = 11;
  final int LCUNAVAIL                     = 30;
  final int LC_BACK                       = 2;
  final int LC_DDMOVE                     = 25;
  final int LC_FRONT                      = 3;
  final int LC_SINGLE                     = 1;
  final int LC_SWAP                       = 24;
  final int LOAD                          = 86;
  final int LOADMOVE                      = 60;
  final int LOAD_SCHED                    = 901;
  final int LOAD_TRAN                     = 770;
  final int LOCWAIT                       = 231;
  final int LOGIN                         = 888;
  final int LOGOUT                        = 889;
  final int LOOP                          = 197;
  final int MASTER                        = 182;
  final int MIXALL                        = 242;
  final int MIXLOTS_ONEITEM               = 244;
  final int MODIFY                        = 890;
  final int MODIFY_EXPECTED_RECEIPT       = 909;
  final int MODIFY_EXPECTED_RECEIPT_LINE  = 912;
  final int MODIFY_ITEM                   = 897;
  final int MODIFY_ITEM_MASTER            = 906;
  final int MODIFY_LOAD                   = 896;
  final int MODIFY_ORDER                  = 895;
  final int MODIFY_ORDER_LINE             = 903;
  final int MOS_DEVICE                    = 185;
  final int MOVEERROR                     = 243;
  final int MOVEPENDING                   = 242;
  final int MOVESENT                      = 244;
  final int MOVING                        = 228;
  final int NO                            = 2;
  final int NOEMU                         = 196;
  final int NOMOVE                        = 224;
  final int NONCAPTIVE                    = 179;
  final int NONEXIST                      = 29;
  final int NOP                           = 3;
  final int NO_REINPUT                    = 0;
  final int OCCUPIED                      = 22;
  final int OFF                           = 36;
  final int OFFLINE                       = 234;
  final int ON                            = 35;
  final int ONELOT_ONEITEM                = 245;
  final int ONELOT_PERITEM                = 243;
  final int ONEQUARTER                    = 235;
  final int ONLINE                        = 233;
  final int ORBUILDING                    = 229;
  final int ORDER_TRAN                    = 771;
  final int ORERROR                       = 242;
  final int OTHER_SEQ                     = 372;
  final int OUTBOUND                      = 102;
  final int OUTPUT                        = 223;
  final int PDSTAND                       = 222;
  final int PICKCOMP                      = 239;
  final int PICKED                        = 235;
  final int PICKING_RETRIEVAL             = 2;
  final int PICK_REQUEST                  = 126;
  final int PIECE                         = 87;
  final int PIECEPICK                     = 88;
  final int PLANNED_RETRIEVAL             = 2;
  final int PLC                           = 182;
  final int PRIMARY_JVM                   = 560;
  final int PROC_ERROR                    = 3;
  final int PTL_INPUT                     = 232;
  final int PTL_OUTPUT                    = 233;
  final int QCHOLD                        = 172;
  final int RANDOM                        = 245;
  final int READY                         = 233;
  final int REALLOC                       = 237;
  final int REASONADJUST                  = 11;
  final int REASONCUSTOM                  = 12;
  final int REASONHOLD                    = 10;
  final int RECEIVECHECKED                = 240;
  final int RECEIVED                      = 239;
  final int REGULAR                       = 64;
  final int REINPUT_SAME_LOC              = 1;
  final int REPICK                        = 232;
  final int REPLENISHMENT                 = 2;
  final int REPLENISHMENTMOVE             = 64;
  final int REPLENISHMENT_REQUEST         = 128;
  final int RETRIEVEERROR                 = 229;
  final int RETRIEVEMODE                  = 404;
  final int RETRIEVEMODE_PENDING          = 407;
  final int RETRIEVEMODE_REQUESTED        = 405;
  final int RETRIEVEMODE_SENT             = 406;
  final int RETRIEVEPENDING               = 222;
  final int RETRIEVESENT                  = 223;
  final int RETRIEVING                    = 225;
  final int REVERSIBLE                    = 225;
  final int SCALE                         = 189;
  final int SCHEDULED                     = 236;
  final int SECONDARY_JVM                 = 561;
  final int SEMICAPTIVE                   = 180;
  final int SET                           = 557;
  final int SHIPHOLD                      = 171;
  final int SHIPMENTBUILD                 = 158;
  final int SHIPMENTCLOSED                = 161;
  final int SHIPMENTHOLD                  = 159;
  final int SHIPMENTOPEN                  = 160;
  final int SHIPPING                      = 228;
  final int SHIPWAIT                      = 233;
  final int SHORT                         = 238;
  final int SHORTLOCWAIT                  = 232;
  final int SIZE_ERROR                    = 246;
  final int SLAVE                         = 183;
  final int SRC5                          = 183;
  final int SRC9X                         = 187;
  final int SRC9Y                         = 180;
  final int SRMACHINE                     = 181;
  final int STAGED                        = 232;
  final int STATION                       = 232;
  final int STNOFFLINE                    = 200;
  final int STORAGE                       = 231;
  final int STOREERROR                    = 230;
  final int STOREMODE                     = 400;
  final int STOREMODE_PENDING             = 403;
  final int STOREMODE_REQUESTED           = 401;
  final int STOREMODE_SENT                = 402;
  final int STOREPENDING                  = 221;
  final int STORERETRIEVE                 = 202;
  final int STORESENT                     = 227;
  final int STORE_REQUEST                 = 125;
  final int STORING                       = 226;
  final int SUPER                         = 63;
  final int SUPER_USER                    = 57;
  final int SWAP_BROKEN                   = 1;
  final int SWAP_HEALTHY                  = 2;
  final int SWAP_SWAP_BACK                = 4;
  final int SWAP_SWAP_FRONT               = 3;
  final int SYSTEM_TRAN                   = 774;
  final int TERMINALSTAT                  = 101;
  final int THREEQUARTER                  = 237;
  final int TRANSFER                      = 891;
  final int TRANSFER_STATION              = 229;
  final int TRUE                          = 235;
  final int UNIT                          = 85;
  final int UNIT_RETRIEVAL                = 1;
  final int UNKNOWN                       = 243;
  final int UNOCCUPIED                    = 21;
  final int URGENT_RETRIEVAL              = 1;
  final int USER_TRAN                     = 772;
  final int USHAPE_IN                     = 220;
  final int USHAPE_OUT                    = 221;
  final int WARAVAIL                      = 240;
  final int WARHOLD                       = 241;
  final int WCS4_CONTROL                  = 191;
  final int WCS4_LIFTER                   = 193;
  final int WCS4_VEHICLE                  = 192;
  final int WORKER                        = 55;
  final int XML                           = 432;
  final int YES                           = 1;

  final int LIFT_TRANSFER_INPUT 		  = 234;	
  final int LIFT_TRANSFER_OUT 		  	  = 235;
  final int SHUTTLE_TRANSFER_INPUT 		  = 236;	
  final int SHUTTLE_TRANSFER_OUT 		  = 237;
  final int LIFT_TRANSFER_REVERSIBLE	  = 238;
  final int SHUTTLE_TRANSFER_REVERSIBLE	  = 239;
  
  final int LCLIFT						  = 20; //Location Left
  final int LCSHUTTLE					  = 21; //Location Shuttle
  final int LCOUTOFGAUGE				  = 22; //Location Out Of Gauge


  final int NO_ACK_REQUIRED               = 0;
  final int ACKED                         = 1;
  final int PENDING_ACK                   = 2;
  final int ACK_FAILED                    = 3;
  
  final int CMD_UNKNOWN 				   = 0;
  final int CMD_READY				   	   = 1;
  final int CMD_COMMANDED  				   = 2;
  final int CMD_PROCCESSING 			   = 3;
  final int CMD_COMPLETED  				   = 4;
  final int CMD_DELETED  				   = 5;
  final int CMD_ERROR				   	   = 6;

  
  final int CMD_DIRECT					   = 0;	
  final int CMD_DIRECT_LOC			   	   = 1;	
  final int CMD_STOREAGE_LOC		   	   = 2;
  final int CMD_LOC_RETRIEVAL  			   = 3;
  
  final int CMD_STORAGE  				   = 1;
  final int CMD_RETRIEVAL  				   = 2;
  final int CMD_RACK					   = 3;	
  
  final int CONVEYOR_WR                   = 65;
  final int SHUTTLE_WR                    = 66;

  final int DELETE_MOVE_COMMAND			  = 914;
  
  final int TRANSFERRING_OUT 			  = 247;
  
  final int FULL_LOCATION 				  = 25;	//When Conveyor location is full 
  
  final int DEFAULT_SHELF_POS = 0; 
}