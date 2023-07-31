package com.daifukuamerica.wrxj.device.agc;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
// Messages from the AGC To RTS

public interface AGCDeviceConstants
{
  static final int  AGCRTSWORKSTARTED = 21;
  static final int  AGCRTSDATATIME = 22;
  static final int  AGCRTSWORKSTOP = 23;
  static final int  AGCRTSDATACANCEL = 24;
  static final int  AGCRTSTRANSPORTRESPONSE = 25;
  static final int  AGCRTSARRIVAL = 26;
  static final int  AGCRTSREQDESTSTATCHG = 27;
  static final int  AGCRTSRESPDESTSTATCHG = 28;
  static final int  AGCRTSSTATUSREPORT = 30;
  static final int  AGCRTSLOCATIONRETRYRESP = 31;
  static final int  AGCRTSRETIEVALRESP = 32;
  static final int  AGCRTSWORKCOMP = 33;
  static final int  AGCRTSTRACKINGDELETE = 35;
  static final int  AGCRTSSIMULTSTARTIM = 36;
  static final int  AGCRTSCOMMRESP = 39;
  static final int  AGCRTSCOMMTESTREQ = 40;
  static final int  AGCRTS_DATA_MESSAGE = 50;
  static final int  AGCRTSMODECHGREQ = 61;
  static final int  AGCRTSRESPMODECHG = 62;
  static final int  AGCRTSMODECHGCOMP = 63;
  static final int  AGCRTSPICKUPCOMP = 64;
  static final int  AGCRTSRETVTRIG = 66;
  static final int  AGCRTSTRIGOPER = 68;
  static final int  AGCRTSAGCMESSAGEDATA = 70;
  static final int  AGCRTSACCESSIMPLOC = 71;
  static final int  AGCRTSSYSTEMRECRESP = 78;
  static final int  AGCRTSSYSTEMTERMREQ = 79;
  static final int  AGCRTSDUALARRIAL = 29;


// Message ID sent From RTS to AGC

  static final int  RTSAGCON = 1;
  static final int  RTSAGCDATETIME = 2;
  static final int  RTSAGCWORKSTOP = 3;
  static final int  RTSAGCCANCEL = 4;
  static final int  RTSAGCMOVE = 5;
  static final int  RTSAGCDESTSTATCHG = 8;
  static final int  RTSAGCMACHINESTATREQ = 10;
  static final int  RTSAGCLOCATIONRETRY = 11;
  static final int  RTSAGCRETRIEVAL = 12;
  static final int  RTSAGCONOFF = 16;
  static final int  RTSAGCHOSTTEST = 19;
  static final int  RTSAGCHOSTTESTRESP = 20;
  static final int  RTSAGCMODECHGRESP = 41;
  static final int  RTSAGCMODECHANGE = 42;
  static final int  RTSAGCOPERCOMP = 45;
  static final int  RTSAGCRETVTRIG = 46;
  static final int  RTSAGCREQTRIGREP = 47;
  static final int  RTSAGCREQACCESSIMPLOC = 51;
  static final int  RTSAGCREQSYSTEMRECOV = 58;
  static final int  RTSAGCREQTERMSYSTEMRECOV = 59;

// Misc

  static final String RACKSTATION = "9000";
  static final int LNWORKNUMBER = 8;
  static final int LNCONTROLINFORMATION = 30;
  static final int LNAGCLOADID = 8;
  static final int LNAGCSTATION = 4;
  static final int LNAGCLOCATION = 9;
  static final int LNAGCEQUIPWAREHOUSE = 1;
  static final int AGCMAXRETVMSG = 2;
  static final String AGCDUMMYLOAD = "99999999";
  static final int LOADIDMAXVALUE = 99999998;        // Can't have a load 99999999 because that is a dummy load
  static final String EMPTYBCRFIELD = "        ";
  static final String ZEROBCRFIELD =  "00000000";
  static final String EMPTYMCKEY = "00000000";
  static final String EMPTYLOCATION = "000000000";
  static final String EMPTYARCLOCATION = "00000000";
  static final int GENERALLOADARRIVALATSTATION = 998;         // used to notify a load has arrived at station
  static final int GENERALSTORELOAD = 999;         // used to wake up scheduler to store a load
  static final String BR_BARCODE = "BR";
  static final String NOREAD_BARCODE = "NOREAD";
  static final String NR_BARCODE = "NR";
  static final int COMPLETIONMODESTORE = 1;
  static final int COMPLETIONMODERETRIEVE = 2;
  static final int GENERALCHANGESTATIONMODE = 888;
  static final int STOREMODEREQ = 1;
  static final int RETRIEVALMODEREQ = 2;
  static final int CHANGEMODERESPNORMAL = 0;
  static final int CHANGEMODERESPERROR = 1;
  static final int CHANGEMODERESPERRORSTATION = 2;
  static final int STORAGEREQUESTCANCEL = 3;
  static final int RETRIEVALREQUESTCANCEL = 4;
  /**
   * Message Identifier to signal load arrival at an AGV station.
   */
  static final int AGV_LOAD_ARRIVAL = 326;
 /**
  * Message Identifier to signal load pickup Complete on AGV.
  */
  static final int AGV_LOAD_PICKUP_COMPLETE = 327;

// AGCScheduler Messages sent to AGCStationDevice

  static final int TURNONDEVICE = 200;
  static final int TURNOFFDEVICE = 201;
  static final int FORCEDEVICEOFF = 202;
  static final int FORCEDEVICEOFFDELDATA = 203;
  static final int STORELOADSTATIONLOCATION = 204;
  static final int MOVELOADSTATIONSTATION = 205;
  static final int RETRIEVELOADLOCATIONSTATION = 206;
  static final int MOVELOADLOCATIONLOCATION = 207;
  static final int SENDDATEANDTIME = 208;
  static final int DEVICESTATUS = 209;
  static final int BINFULLNEWLOC = 210;
  static final int BINFULLMOVESTATION = 211;
  static final int BINEMPTYNEWLOC  = 212;
  static final int BINEMPTYCANCEL = 213;
  static final int DIMENSIONWRONGNEWLOC = 214;
  static final int DIMENSIONWRONGMOVESTATION = 215;
  static final int STARTDEVICE = 216;
  static final int STOPDEVICE = 217;
  static final int COMMTESTDEVICE = 218;
  static final int COMMTESTRESPONSE = 219;
  static final int STOREMODE = 220;
  static final int RETRIEVEMODE = 221;
  static final int STOREDUALLOADSTATIONLOCATION = 222;
  static final int RETRIEVEDUALLOADSTATIONLOCATION = 223;
  static final int SEND_DATA_MESSAGE = 224;
  static final int BINFULLNONEWLOC = 225;
  static final int DOOUTPUTINSTRUCTION = 226;
  static final int RESPONSETORETRIEVALTRIGGER = 227;
  static final int DESTSTATIONCHANGE = 228;

// AGCStationDevice Messages sent to AGCScheduler

  static final int AGCDEVICESTARTRESPONSE = 421;                 // ID 21
  static final int AGCDEVICEDATTIME = 422;                       // ID 22
  static final int AGCDEVICERESPOPERTERMREQ = 423;          // ID 23
  static final int AGCDEVICERESPTRANSPORTDATACANCEL = 424;           // ID 24
  static final int AGCDEVICERESPTRANSPORTCOMMAND = 425;          // ID 25
  static final int AGCDEVICEARRIVALREPORT = 426;                 // ID 26
  static final int AGCDEVICEREQDESTSTATCHG = 427;            // ID 27
  static final int AGCDEVICERESPDESTSTATCHG = 428;               // ID 28
  static final int AGCDEVICEDUALARRIVALREPORT = 429;				// ID 29
  static final int AGCDEVICEMACHINESTATUSREPORT = 430;                 // ID 30
  static final int AGCDEVICEALTLOCCOMMANDRESP = 431;             // ID 31
  static final int AGCDEVICERESPRETRIEVECMD = 432;                  // ID 32
  static final int AGCDEVICEOPERATIONCOMPLETION = 433;           // ID 33
  static final int AGCDEVICETRANDATADELREPORT = 435;             // ID 35
  static final int AGCDEVICESIMUSTATEIMPORPER = 436;             // ID 36
  static final int AGCDEVICERESPCOMMTESTREQ = 439;               // ID 39
  static final int AGCDEVICECOMMTESTREQ = 440;                   // ID 40
  static final int AGCRETRIEVALTIGGERRESPONSE = 446;             // ID 46
  static final int AGCDEVICEOPERMODECHGREQ = 461;                // ID 61
  static final int AGCDEVICERESPOPERMODECHGCMD = 462;            // ID 62
  static final int AGCDEVICEOPERMODECHGCOMPREPORT = 463;         // ID 63
  static final int AGCDEVICEPICKCOMPREPORT = 464;                // ID 64
  static final int AGCDEVICERETRIEVALTRIGGER = 466;              // ID 65
  static final int AGCDEVICETRIGGEROPERINDICAT = 468;            // ID 68
  static final int AGCDEVICEMESSAGEDATA = 470;                   // ID 70
  static final int AGCDEVICEACCESSIMPOSSIBLELOC = 471;           // ID 71
  static final int AGCDEVICERESPSYSTEMRECSTARTREQ = 478;         // ID 78
  static final int AGCDEVICERESPSYSTEMRECTERMREQ = 479;          // ID 79

  /*
   * Controller Detailed Status strings
   */
  static final String DS_AGC_GO_ONLINE_1   = "Request AGC Online (01)";
  static final String DS_AGC_ONLINE        = "AGC Online (21)";
  static final String DS_EQPMT_GO_ONLINE   = "Request Equipment Online (16)";
  static final String DS_EQPMT_ONLINE_UNA  = "*Equipment Unable To Go Online (36)";
  static final String DS_AGC_GO_OFFLINE_0  = "Request AGC Offline (03)";
  static final String DS_AGC_GO_OFFLINE_1  = "Request AGC Offline - Unconditional (03)";
  static final String DS_AGC_GO_OFFLINE_2  = "Request AGC Offline - Uncond & Del Data (03)";
  static final String DS_AGC_OFFLINE       = "AGC Offline (23)";
  static final String DS_AGC_OFFLINE_SE    = "*AGC Offline Status ERROR (23)*";
  static final String DS_AGC_OFFLINE_DE    = "*AGC Offline Data ERROR (23)*";
  static final String DS_EQPMT_GO_OFFLINE  = "Request Equipment Offline (16)";
  static final String DS_EQPMT_OFFLINE     = "Equipment Offline (23)";
  static final String DS_EQPMT_OFFLINE_NO  = "*CANNOT Turn Equipment Offline (23)*";
  static final String DS_EQPMT_OFFLINE_DE  = "*Equipment Offline Data ERROR (23)*";
  static final String DS_REQ_MACH_STATUS   = "Request Machine Status (10)";
  static final String DS_RCV_MACH_STATUS   = "Received Machine Status (30)";
  static final String DS_RCV_IMPOS_LOC     = "Received Impossible Locations Report (71)";
  static final String DS_RCV_DATETIME_REQ  = "Received Date/Time Request";
  static final String DS_LD_RETRIEVE_REQ   = "Load Retrieval Request (12)";
  static final String DS_LD_RTRVE_RSP_RCV  = "Load Retrieval Response (32) Received";
  static final String DS_LD_RTRVE_OP_CPL   = "Operation Complete (33) Received";
  static final String DS_LD_RTRVE_TRG_RCV  = "Operation Indication Trigger (68) Received";
  static final String DS_LD_RTRVE_ARV_RPT  = "Load Retrieval Arrival Report (26) Received";
  static final String DS_LD_STORE_REQ      = "Load Store Request (05)";
  static final String DS_LD_STORE_RSP_RCV  = "Load Store Response Received (25)";
  static final String DS_LD_STORE_PU_CPL   = "Load Store Pickup Complete (64) Received";
  static final String DS_LD_STORE_TRG_RCV  = "Operation Indication Trigger (68) Received";
  static final String DS_LD_STORE_ARR_RPT  = "Load Store Arrival Report Received (26)";
  static final String DS_LD_XFR_STN_STN_RQ = "Load Station-To-Station Transfer Request (05)";
  static final String DS_LD_XFR_LOC_LOC_RQ = "Load Bin-To-Bin Transfer Request (05)";
  static final String DS_DATETIME_DATA_RPT = "Data-Time Data Report (02)";
  static final String DS_ALT_LOC_BIN_REQ   = "Alternate Location Request (for Bin-Full) (11)";
  static final String DS_ALT_LOC_STN_REQ   = "Alternate Station Request (for Bin-Full) (11)";
  static final String DS_ALT_DATA_CNCL_REQ = "Alternate Data Cancel Request (for Bin-Empty) (11)";
  static final String DS_OP_MODE_STR_N_REQ = "Op Mode Change Request (To Store Normal) (42)";
  static final String DS_OP_MODE_RTV_N_REQ = "Op Mode Change Request (To Retrieve Normal) (42)";
  static final String DS_DATA_DELETE_RCV   = "Transport Data Deletion Report (35)";
}
