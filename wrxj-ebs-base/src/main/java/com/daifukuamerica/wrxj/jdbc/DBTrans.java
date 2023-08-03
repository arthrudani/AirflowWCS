package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.log.Logger;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Description:<BR>
 *   Data class that initializes the translation objects, and provides methods 
 *   to retrieve them.
 *
 * <P><I>This is a generated class.  Do not edit directly!  If you need to add
 * a translation, add it to jwmsadd.sql and run "ant db-tran".</I></P>
 *
 * @author       A.D.
 * @version      2.0
 * <BR>Created:  30-Aug-01    Original version<BR>
 * <BR>Modified: 11-Dec-01    Converted to static methods.
 * <BR>Copyright (c) 2001-2009
 * <BR>Company:  Daifuku America Corporation
 */
public class DBTrans implements DBConstants
{
  protected static Map<String, DBTrandef[]> tm = new TreeMap<String, DBTrandef[]>();

/*----------------------------------------------------------------------------
   The code inside the init method is generated and overwritten by the
   ParseDatabase utility when it parses the data definition file.  Please
   do not edit this method.
  ----------------------------------------------------------------------------*/
 /**
  *  Initializes Translation objects, and stores them for retrieval later.
  */
  public static void init()
  {
    if (tm.size() == 0)
    {


                                      // Object array for "boldnt"
      DBTrandef[] boldnt = new DBTrandef[2];
      boldnt[0] = new DBTrandef(NO, "No");
      boldnt[1] = new DBTrandef(YES, "Yes");

      tm.put("BOLDNT", boldnt);
    

                                      // Object array for "iActiveConfig"
      DBTrandef[] iActiveConfig = new DBTrandef[2];
      iActiveConfig[0] = new DBTrandef(NO, "No");
      iActiveConfig[1] = new DBTrandef(YES, "Yes");

      tm.put("IACTIVECONFIG", iActiveConfig);
    

                                      // Object array for "iAddAllowed"
      DBTrandef[] iAddAllowed = new DBTrandef[2];
      iAddAllowed[0] = new DBTrandef(NO, "No");
      iAddAllowed[1] = new DBTrandef(YES, "Yes");

      tm.put("IADDALLOWED", iAddAllowed);
    

                                      // Object array for "iAGVLoadStatus"
      DBTrandef[] iAGVLoadStatus = new DBTrandef[12];
      iAGVLoadStatus[0] = new DBTrandef(AGV_MOVECANCELED, "AGV Move Canceled");
      iAGVLoadStatus[1] = new DBTrandef(AGV_MOVECANCELERROR, "AGV Move Cancel Error");
      iAGVLoadStatus[2] = new DBTrandef(AGV_MOVECANCELPENDING, "AGV Move Cancel Pending");
      iAGVLoadStatus[3] = new DBTrandef(AGV_MOVECANCELREQUEST, "AGV Move Cancel Request");
      iAGVLoadStatus[4] = new DBTrandef(AGV_MOVECANCELSENT, "AGV Move cancel sent");
      iAGVLoadStatus[5] = new DBTrandef(AGV_MOVECOMPLETE, "AGV Move Completed");
      iAGVLoadStatus[6] = new DBTrandef(AGV_MOVEERROR, "AGV move Error");
      iAGVLoadStatus[7] = new DBTrandef(AGV_MOVEPENDING, "AGV move pending");
      iAGVLoadStatus[8] = new DBTrandef(AGV_MOVESENT, "AGV Move sent");
      iAGVLoadStatus[9] = new DBTrandef(AGV_MOVING, "AGV moving");
      iAGVLoadStatus[10] = new DBTrandef(AGV_NOMOVE, "AGV Move Unsent");
      iAGVLoadStatus[11] = new DBTrandef(AGV_RECOVERABLE, "AGV Move recoverable.");

      tm.put("IAGVLOADSTATUS", iAGVLoadStatus);
    

                                      // Object array for "iAllocationEnabled"
      DBTrandef[] iAllocationEnabled = new DBTrandef[2];
      iAllocationEnabled[0] = new DBTrandef(NO, "No");
      iAllocationEnabled[1] = new DBTrandef(YES, "Yes");

      tm.put("IALLOCATIONENABLED", iAllocationEnabled);
    

                                      // Object array for "iAllowDeletion"
      DBTrandef[] iAllowDeletion = new DBTrandef[2];
      iAllowDeletion[0] = new DBTrandef(NO, "No");
      iAllowDeletion[1] = new DBTrandef(YES, "Yes");

      tm.put("IALLOWDELETION", iAllowDeletion);
    

                                      // Object array for "iAllowRoundRobin"
      DBTrandef[] iAllowRoundRobin = new DBTrandef[2];
      iAllowRoundRobin[0] = new DBTrandef(NO, "No (Use Route)");
      iAllowRoundRobin[1] = new DBTrandef(YES, "Yes (Use Device Token)");

      tm.put("IALLOWROUNDROBIN", iAllowRoundRobin);
    

                                      // Object array for "iAmountFull"
      DBTrandef[] iAmountFull = new DBTrandef[5];
      iAmountFull[0] = new DBTrandef(EMPTY, "Empty");
      iAmountFull[1] = new DBTrandef(FULL, "Full");
      iAmountFull[2] = new DBTrandef(HALF, "1/2 Full");
      iAmountFull[3] = new DBTrandef(ONEQUARTER, "1/4 Full");
      iAmountFull[4] = new DBTrandef(THREEQUARTER, "3/4 Full");

      tm.put("IAMOUNTFULL", iAmountFull);
    

                                      // Object array for "iArrivalRequired"
      DBTrandef[] iArrivalRequired = new DBTrandef[2];
      iArrivalRequired[0] = new DBTrandef(NO, "No");
      iArrivalRequired[1] = new DBTrandef(YES, "Yes");

      tm.put("IARRIVALREQUIRED", iArrivalRequired);
    

                                      // Object array for "iAutoLoadMovementType"
      DBTrandef[] iAutoLoadMovementType = new DBTrandef[8];
      iAutoLoadMovementType[0] = new DBTrandef(AUTOPICK, "Auto-Pick");
      iAutoLoadMovementType[1] = new DBTrandef(AUTORECEIVE_BCR, "Auto-Receive BCR as Item");
      iAutoLoadMovementType[2] = new DBTrandef(AUTORECEIVE_ER, "Auto-Receive With E.R.");
      iAutoLoadMovementType[3] = new DBTrandef(AUTORECEIVE_EXPECTED_LOAD, "Auto-Receive Load with E.R. Validation");
      iAutoLoadMovementType[4] = new DBTrandef(AUTORECEIVE_ITEM, "Auto-Receive With Item Only");
      iAutoLoadMovementType[5] = new DBTrandef(AUTORECEIVE_LOAD, "Auto-Receive Load");
      iAutoLoadMovementType[6] = new DBTrandef(AUTO_MOVE_OFF, "Auto-Load Movement Off");
      iAutoLoadMovementType[7] = new DBTrandef(BOTH, "Both Pick & Receiving");

      tm.put("IAUTOLOADMOVEMENTTYPE", iAutoLoadMovementType);
    

                                      // Object array for "iAutoOrderType"
      DBTrandef[] iAutoOrderType = new DBTrandef[3];
      iAutoOrderType[0] = new DBTrandef(AUTO_ORDER_OFF, "Auto-Order off.");
      iAutoOrderType[1] = new DBTrandef(EMPTY_CONTAINER_ORDER, "Auto-Order Empty Container.");
      iAutoOrderType[2] = new DBTrandef(ITEM_ORDER, "Auto-Order Item.");

      tm.put("IAUTOORDERTYPE", iAutoOrderType);
    

                                      // Object array for "iBidirectionalStatus"
      DBTrandef[] iBidirectionalStatus = new DBTrandef[8];
      iBidirectionalStatus[0] = new DBTrandef(RETRIEVEMODE, "Retrieve Mode");
      iBidirectionalStatus[1] = new DBTrandef(RETRIEVEMODE_PENDING, "Retrieve Mode Pending");
      iBidirectionalStatus[2] = new DBTrandef(RETRIEVEMODE_REQUESTED, "Retrieve Mode Requested");
      iBidirectionalStatus[3] = new DBTrandef(RETRIEVEMODE_SENT, "Retrieve Mode Sent");
      iBidirectionalStatus[4] = new DBTrandef(STOREMODE, "Store Mode");
      iBidirectionalStatus[5] = new DBTrandef(STOREMODE_PENDING, "Store Mode Pending");
      iBidirectionalStatus[6] = new DBTrandef(STOREMODE_REQUESTED, "Store Mode Requested");
      iBidirectionalStatus[7] = new DBTrandef(STOREMODE_SENT, "Store Mode Sent");

      tm.put("IBIDIRECTIONALSTATUS", iBidirectionalStatus);
    

                                      // Object array for "iBoldNote"
      DBTrandef[] iBoldNote = new DBTrandef[3];
      iBoldNote[0] = new DBTrandef(NO, "No");
      iBoldNote[1] = new DBTrandef(NOP, "NOP");
      iBoldNote[2] = new DBTrandef(YES, "Yes");

      tm.put("IBOLDNOTE", iBoldNote);
    

                                      // Object array for "iButtonBar"
      DBTrandef[] iButtonBar = new DBTrandef[2];
      iButtonBar[0] = new DBTrandef(NO, "No");
      iButtonBar[1] = new DBTrandef(YES, "Yes");

      tm.put("IBUTTONBAR", iButtonBar);
    

                                      // Object array for "iCaptive"
      DBTrandef[] iCaptive = new DBTrandef[3];
      iCaptive[0] = new DBTrandef(CAPTIVE, "Captive");
      iCaptive[1] = new DBTrandef(NONCAPTIVE, "Not Captive");
      iCaptive[2] = new DBTrandef(SEMICAPTIVE, "Semi Captive");

      tm.put("ICAPTIVE", iCaptive);
    

                                      // Object array for "iCategory"
      DBTrandef[] iCategory = new DBTrandef[2];
      iCategory[0] = new DBTrandef(EMPLOYEESTAT, "Employee");
      iCategory[1] = new DBTrandef(TERMINALSTAT, "Terminal");

      tm.put("ICATEGORY", iCategory);
    

                                      // Object array for "iCCIAllowed"
      DBTrandef[] iCCIAllowed = new DBTrandef[2];
      iCCIAllowed[0] = new DBTrandef(NO, "No");
      iCCIAllowed[1] = new DBTrandef(YES, "Yes");

      tm.put("ICCIALLOWED", iCCIAllowed);
    

                                      // Object array for "iCommandStatus"
      DBTrandef[] iCommandStatus = new DBTrandef[5];
      iCommandStatus[0] = new DBTrandef(AGV_SYSCMD_COMPLETE, "AGV System command Completed");
      iCommandStatus[1] = new DBTrandef(AGV_SYSCMD_ERROR, "AGV System command Error");
      iCommandStatus[2] = new DBTrandef(AGV_SYSCMD_PENDING, "AGV System command Pending");
      iCommandStatus[3] = new DBTrandef(AGV_SYSCMD_REQUEST, "AGV System change Requested");
      iCommandStatus[4] = new DBTrandef(AGV_SYSCMD_SENT, "AGV System command Sent");

      tm.put("ICOMMANDSTATUS", iCommandStatus);
    

                                      // Object array for "iCommunicationMode"
      DBTrandef[] iCommunicationMode = new DBTrandef[2];
      iCommunicationMode[0] = new DBTrandef(MASTER, "Server");
      iCommunicationMode[1] = new DBTrandef(SLAVE, "Client");

      tm.put("ICOMMUNICATIONMODE", iCommunicationMode);
    

                                      // Object array for "iConfirmItem"
      DBTrandef[] iConfirmItem = new DBTrandef[2];
      iConfirmItem[0] = new DBTrandef(NO, "No");
      iConfirmItem[1] = new DBTrandef(YES, "Yes");

      tm.put("ICONFIRMITEM", iConfirmItem);
    

                                      // Object array for "iConfirmLoad"
      DBTrandef[] iConfirmLoad = new DBTrandef[2];
      iConfirmLoad[0] = new DBTrandef(NO, "No");
      iConfirmLoad[1] = new DBTrandef(YES, "Yes");

      tm.put("ICONFIRMLOAD", iConfirmLoad);
    

                                      // Object array for "iConfirmLocation"
      DBTrandef[] iConfirmLocation = new DBTrandef[2];
      iConfirmLocation[0] = new DBTrandef(NO, "No");
      iConfirmLocation[1] = new DBTrandef(YES, "Yes");

      tm.put("ICONFIRMLOCATION", iConfirmLocation);
    

                                      // Object array for "iConfirmLot"
      DBTrandef[] iConfirmLot = new DBTrandef[2];
      iConfirmLot[0] = new DBTrandef(NO, "No");
      iConfirmLot[1] = new DBTrandef(YES, "Yes");

      tm.put("ICONFIRMLOT", iConfirmLot);
    

                                      // Object array for "iConfirmQty"
      DBTrandef[] iConfirmQty = new DBTrandef[2];
      iConfirmQty[0] = new DBTrandef(NO, "No");
      iConfirmQty[1] = new DBTrandef(YES, "Yes");

      tm.put("ICONFIRMQTY", iConfirmQty);
    

                                      // Object array for "iContainerUse"
      DBTrandef[] iContainerUse = new DBTrandef[4];
      iContainerUse[0] = new DBTrandef(CUSTOM, "Custom");
      iContainerUse[1] = new DBTrandef(REPICK, "Repick");
      iContainerUse[2] = new DBTrandef(SHIPPING, "Shipping");
      iContainerUse[3] = new DBTrandef(STORAGE, "Storage");

      tm.put("ICONTAINERUSE", iContainerUse);
    

                                      // Object array for "iCustomAction"
      DBTrandef[] iCustomAction = new DBTrandef[7];
      iCustomAction[0] = new DBTrandef(CA_CUSTOM1, "Custom Action 1");
      iCustomAction[1] = new DBTrandef(CA_CUSTOM2, "Custom Action 2");
      iCustomAction[2] = new DBTrandef(CA_CUSTOM3, "Custom Action 3");
      iCustomAction[3] = new DBTrandef(CA_CUSTOM4, "Custom Action 4");
      iCustomAction[4] = new DBTrandef(CA_CUSTOM5, "Custom Action 5");
      iCustomAction[5] = new DBTrandef(CA_CUSTOM6, "Custom Action 6");
      iCustomAction[6] = new DBTrandef(CA_NORMAL, "None");

      tm.put("ICUSTOMACTION", iCustomAction);
    

                                      // Object array for "iDedicatedType"
      DBTrandef[] iDedicatedType = new DBTrandef[3];
      iDedicatedType[0] = new DBTrandef(FULLPICK, "Full Unit");
      iDedicatedType[1] = new DBTrandef(INNER_PACK, "Inner Pack");
      iDedicatedType[2] = new DBTrandef(PIECEPICK, "Piece");

      tm.put("IDEDICATEDTYPE", iDedicatedType);
    

                                      // Object array for "iDeleteAllowed"
      DBTrandef[] iDeleteAllowed = new DBTrandef[2];
      iDeleteAllowed[0] = new DBTrandef(NO, "No");
      iDeleteAllowed[1] = new DBTrandef(YES, "Yes");

      tm.put("IDELETEALLOWED", iDeleteAllowed);
    

                                      // Object array for "iDeleteAtZeroQuantity"
      DBTrandef[] iDeleteAtZeroQuantity = new DBTrandef[2];
      iDeleteAtZeroQuantity[0] = new DBTrandef(NO, "No");
      iDeleteAtZeroQuantity[1] = new DBTrandef(YES, "Yes");

      tm.put("IDELETEATZEROQUANTITY", iDeleteAtZeroQuantity);
    

                                      // Object array for "iDeleteInventory"
      DBTrandef[] iDeleteInventory = new DBTrandef[2];
      iDeleteInventory[0] = new DBTrandef(NO, "No");
      iDeleteInventory[1] = new DBTrandef(YES, "Yes");

      tm.put("IDELETEINVENTORY", iDeleteInventory);
    

                                      // Object array for "iDeleteOnUse"
      DBTrandef[] iDeleteOnUse = new DBTrandef[2];
      iDeleteOnUse[0] = new DBTrandef(NO, "No");
      iDeleteOnUse[1] = new DBTrandef(YES, "Yes");

      tm.put("IDELETEONUSE", iDeleteOnUse);
    

                                      // Object array for "iDestType"
      DBTrandef[] iDestType = new DBTrandef[2];
      iDestType[0] = new DBTrandef(EQUIPMENT, "Equipment Route");
      iDestType[1] = new DBTrandef(STATION, "Station Route");

      tm.put("IDESTTYPE", iDestType);
    

                                      // Object array for "iDeviceToken"
      DBTrandef[] iDeviceToken = new DBTrandef[2];
      iDeviceToken[0] = new DBTrandef(FALSE, "FALSE");
      iDeviceToken[1] = new DBTrandef(TRUE, "TRUE");

      tm.put("IDEVICETOKEN", iDeviceToken);
    

                                      // Object array for "iDeviceType"
      DBTrandef[] iDeviceType = new DBTrandef[15];
      iDeviceType[0] = new DBTrandef(AGC, "AGC Device");
      iDeviceType[1] = new DBTrandef(AGC9X, "AGC9X Machine");
      iDeviceType[2] = new DBTrandef(AGV, "AGV Device");
      iDeviceType[3] = new DBTrandef(ARC100, "ARC100");
      iDeviceType[4] = new DBTrandef(CONV_DEVICE, "Conventional Device");
      iDeviceType[5] = new DBTrandef(MOS_DEVICE, "MOS Device - do not use");
      iDeviceType[6] = new DBTrandef(PLC, "PLC Device");
      iDeviceType[7] = new DBTrandef(SCALE, "SCALE Device");
      iDeviceType[8] = new DBTrandef(SRC5, "SRC5 Machine");
      iDeviceType[9] = new DBTrandef(SRC9X, "SRC9X Machine");
      iDeviceType[10] = new DBTrandef(SRC9Y, "SRC9Y Machine");
      iDeviceType[11] = new DBTrandef(SRMACHINE, "SR Machine");
      iDeviceType[12] = new DBTrandef(WCS4_CONTROL, "WCS4 Controller");
      iDeviceType[13] = new DBTrandef(WCS4_LIFTER, "WCS4 Lifter");
      iDeviceType[14] = new DBTrandef(WCS4_VEHICLE, "WCS4 Shuttle");

      tm.put("IDEVICETYPE", iDeviceType);
    

                                      // Object array for "iDirection"
      DBTrandef[] iDirection = new DBTrandef[3];
      iDirection[0] = new DBTrandef(BIDIRECT, "BiDirectional");
      iDirection[1] = new DBTrandef(INBOUND, "Inbound");
      iDirection[2] = new DBTrandef(OUTBOUND, "Outbound");

      tm.put("IDIRECTION", iDirection);
    

                                      // Object array for "iEmulationMode"
      DBTrandef[] iEmulationMode = new DBTrandef[3];
      iEmulationMode[0] = new DBTrandef(FULLEMU, "Full Emulation");
      iEmulationMode[1] = new DBTrandef(LOOP, "Loopback");
      iEmulationMode[2] = new DBTrandef(NOEMU, "No Emulation");

      tm.put("IEMULATIONMODE", iEmulationMode);
    

                                      // Object array for "iEmptyFlag"
      DBTrandef[] iEmptyFlag = new DBTrandef[6];
      iEmptyFlag[0] = new DBTrandef(LCRESERVED, "Reserved");
      iEmptyFlag[1] = new DBTrandef(LC_DDMOVE, "Double-Deep Movement");
      iEmptyFlag[2] = new DBTrandef(LC_SWAP, "Swap");
      iEmptyFlag[3] = new DBTrandef(OCCUPIED, "Occupied");//In Conveyor system a Lane can be occupied but not full!
      iEmptyFlag[4] = new DBTrandef(UNOCCUPIED, "Empty");
      iEmptyFlag[5] = new DBTrandef(FULL_LOCATION, "Full"); //This is a flag is used in Conveyor system to indicate a Lane is full 

      tm.put("IEMPTYFLAG", iEmptyFlag);
    

                                      // Object array for "iOccupiedStatus"
      DBTrandef[] iOccupiedStatus = new DBTrandef[5];
      iOccupiedStatus[0] = new DBTrandef(LCRESERVED, "Reserved");
      iOccupiedStatus[1] = new DBTrandef(LC_DDMOVE, "Double-Deep Movement");
      iOccupiedStatus[2] = new DBTrandef(LC_SWAP, "Swap");
      iOccupiedStatus[3] = new DBTrandef(OCCUPIED, "Occupied");
      iOccupiedStatus[4] = new DBTrandef(UNOCCUPIED, "Empty");

      tm.put("IOCCUPIEDSTATUS", iOccupiedStatus);
    

                                      // Object array for "iEnabled"
      DBTrandef[] iEnabled = new DBTrandef[2];
      iEnabled[0] = new DBTrandef(NO, "No");
      iEnabled[1] = new DBTrandef(YES, "Yes");

      tm.put("IENABLED", iEnabled);
    

                                      // Object array for "iExpirationRequired"
      DBTrandef[] iExpirationRequired = new DBTrandef[2];
      iExpirationRequired[0] = new DBTrandef(NO, "No");
      iExpirationRequired[1] = new DBTrandef(YES, "Yes");

      tm.put("IEXPIRATIONREQUIRED", iExpirationRequired);
    

                                      // Object array for "iFromType"
      DBTrandef[] iFromType = new DBTrandef[2];
      iFromType[0] = new DBTrandef(EQUIPMENT, "Equipment Route");
      iFromType[1] = new DBTrandef(STATION, "Station Route");

      tm.put("IFROMTYPE", iFromType);
    

                                      // Object array for "iHoldType"
      DBTrandef[] iHoldType = new DBTrandef[5];
      iHoldType[0] = new DBTrandef(ITMAVAIL, "Available");
      iHoldType[1] = new DBTrandef(ITMHOLD, "Hold");
      iHoldType[2] = new DBTrandef(ITMREJECT, "Reject Load Marker Item");
      iHoldType[3] = new DBTrandef(QCHOLD, "QCHold");
      iHoldType[4] = new DBTrandef(SHIPHOLD, "Shipping");

      tm.put("IHOLDTYPE", iHoldType);
    

                                      // Object array for "iHostDataFormat"
      DBTrandef[] iHostDataFormat = new DBTrandef[3];
      iHostDataFormat[0] = new DBTrandef(DELIMITED, "Delimited");
      iHostDataFormat[1] = new DBTrandef(FIXEDLENGTH, "FixedLength");
      iHostDataFormat[2] = new DBTrandef(XML, "XML");

      tm.put("IHOSTDATAFORMAT", iHostDataFormat);
    

                                      // Object array for "iJVMStatus"
      DBTrandef[] iJVMStatus = new DBTrandef[3];
      iJVMStatus[0] = new DBTrandef(JVM_DISABLED, "JVM usage is temporarily disabled");
      iJVMStatus[1] = new DBTrandef(JVM_INUSE, "JVM is in use.");
      iJVMStatus[2] = new DBTrandef(JVM_UNUSED, "JVM is not being used.");

      tm.put("IJVMSTATUS", iJVMStatus);
    

                                      // Object array for "iJVMType"
      DBTrandef[] iJVMType = new DBTrandef[2];
      iJVMType[0] = new DBTrandef(PRIMARY_JVM, "Primary JVM");
      iJVMType[1] = new DBTrandef(SECONDARY_JVM, "Secondary JVM");

      tm.put("IJVMTYPE", iJVMType);
    

                                      // Object array for "iMessageProcessed"
      DBTrandef[] iMessageProcessed = new DBTrandef[3];
      iMessageProcessed[0] = new DBTrandef(NO, "No");
      iMessageProcessed[1] = new DBTrandef(PROC_ERROR, "Processing Error");
      iMessageProcessed[2] = new DBTrandef(YES, "Yes");

      tm.put("IMESSAGEPROCESSED", iMessageProcessed);
    

                                      // Object array for "iInspection"
      DBTrandef[] iInspection = new DBTrandef[2];
      iInspection[0] = new DBTrandef(NO, "No");
      iInspection[1] = new DBTrandef(YES, "Yes");

      tm.put("IINSPECTION", iInspection);
    

                                      // Object array for "iLineShy"
      DBTrandef[] iLineShy = new DBTrandef[2];
      iLineShy[0] = new DBTrandef(NO, "No");
      iLineShy[1] = new DBTrandef(YES, "Yes");

      tm.put("ILINESHY", iLineShy);
    

                                      // Object array for "iLoadPresenceCheck"
      DBTrandef[] iLoadPresenceCheck = new DBTrandef[2];
      iLoadPresenceCheck[0] = new DBTrandef(NO, "No");
      iLoadPresenceCheck[1] = new DBTrandef(YES, "Yes");

      tm.put("ILOADPRESENCECHECK", iLoadPresenceCheck);
    

                                      // Object array for "iLoadMoveStatus"
      DBTrandef[] iLoadMoveStatus = new DBTrandef[29];
      iLoadMoveStatus[0] = new DBTrandef(ARRIVED, "Arrived");
      iLoadMoveStatus[1] = new DBTrandef(ARRIVEPENDING, "Arrival Pending");
      iLoadMoveStatus[2] = new DBTrandef(BINFULL_ERROR, "Bin Full Error");
      iLoadMoveStatus[3] = new DBTrandef(BUILDING, "Building");
      iLoadMoveStatus[4] = new DBTrandef(CONSOLIDATED, "Consolidated");
      iLoadMoveStatus[5] = new DBTrandef(CONSOLIDATING, "Consolidating");
      iLoadMoveStatus[6] = new DBTrandef(ERROR, "Error");
      iLoadMoveStatus[7] = new DBTrandef(IDPENDING, "ID Pending");
      iLoadMoveStatus[8] = new DBTrandef(MOVEERROR, "Move Error");
      iLoadMoveStatus[9] = new DBTrandef(MOVEPENDING, "Move Pending");
      iLoadMoveStatus[10] = new DBTrandef(MOVESENT, "Move Sent");
      iLoadMoveStatus[11] = new DBTrandef(MOVING, "Moving");
      iLoadMoveStatus[12] = new DBTrandef(NOMOVE, "Stored");
      iLoadMoveStatus[13] = new DBTrandef(PICKED, "Picked");
      iLoadMoveStatus[14] = new DBTrandef(RECEIVECHECKED, "Received and Checked");
      iLoadMoveStatus[15] = new DBTrandef(RECEIVED, "Received");
      iLoadMoveStatus[16] = new DBTrandef(RETRIEVEERROR, "Retrieve Error");
      iLoadMoveStatus[17] = new DBTrandef(RETRIEVEPENDING, "Retrieve Pending");
      iLoadMoveStatus[18] = new DBTrandef(RETRIEVESENT, "Retrieve Message Sent");
      iLoadMoveStatus[19] = new DBTrandef(RETRIEVING, "Retrieving");
      iLoadMoveStatus[20] = new DBTrandef(SHIPPING, "Shipping");
      iLoadMoveStatus[21] = new DBTrandef(SHIPWAIT, "ShipWait");
      iLoadMoveStatus[22] = new DBTrandef(SIZE_ERROR, "Size Mismatch Error");
      iLoadMoveStatus[23] = new DBTrandef(STAGED, "Staged");
      iLoadMoveStatus[24] = new DBTrandef(STOREERROR, "Store Error");
      iLoadMoveStatus[25] = new DBTrandef(STOREPENDING, "Store Pending");
      iLoadMoveStatus[26] = new DBTrandef(STORESENT, "Store Sent");
      iLoadMoveStatus[27] = new DBTrandef(STORING, "Storing");
      iLoadMoveStatus[28] = new DBTrandef(TRANSFERRING_OUT, "Transferring out");

      tm.put("ILOADMOVESTATUS", iLoadMoveStatus);
    

                                      // Object array for "iLocationDepth"
      DBTrandef[] iLocationDepth = new DBTrandef[3];
      iLocationDepth[0] = new DBTrandef(LC_BACK, "Outer");
      iLocationDepth[1] = new DBTrandef(LC_FRONT, "Inner");
      iLocationDepth[2] = new DBTrandef(LC_SINGLE, "Single");

      tm.put("ILOCATIONDEPTH", iLocationDepth);
    

                                      // Object array for "iLocationMaint"
      DBTrandef[] iLocationMaint = new DBTrandef[3];
      iLocationMaint[0] = new DBTrandef(EQUALS, "Equals");
      iLocationMaint[1] = new DBTrandef(IGNORE, "Ignore");
      iLocationMaint[2] = new DBTrandef(SET, "Set");

      tm.put("ILOCATIONMAINT", iLocationMaint);
    

                                      // Object array for "iLocationOrder"
      DBTrandef[] iLocationOrder = new DBTrandef[2];
      iLocationOrder[0] = new DBTrandef(BACK_TO_FRONT, "Order Back To Front");
      iLocationOrder[1] = new DBTrandef(FRONT_TO_BACK, "Order Front To Back");

      tm.put("ILOCATIONORDER", iLocationOrder);
    

                                      // Object array for "iLocationStatus"
      DBTrandef[] iLocationStatus = new DBTrandef[3];
      iLocationStatus[0] = new DBTrandef(LCAVAIL, "Available");
      iLocationStatus[1] = new DBTrandef(LCPROHIBIT, "Prohibited");
      iLocationStatus[2] = new DBTrandef(LCUNAVAIL, "Unavailable");

      tm.put("ILOCATIONSTATUS", iLocationStatus);
    

                                      // Object array for "iLocationType"
      DBTrandef[] iLocationType = new DBTrandef[13];
      iLocationType[0] = new DBTrandef(LCASRS, "ASRS Location");						//10
      iLocationType[1] = new DBTrandef(LCCONSOLIDATION, "Consolidation Location");		//15
      iLocationType[2] = new DBTrandef(LCCONVSTORAGE, "Conventional Storage");			//19
      iLocationType[3] = new DBTrandef(LCDEDICATED, "Dedicated Location");				//17
      iLocationType[4] = new DBTrandef(LCDEVICE, "Device Location");					//18
      iLocationType[5] = new DBTrandef(LCFLOW, "Flow Rack");							//12
      iLocationType[6] = new DBTrandef(LCRECEIVING, "Receiving Location");				//14
      iLocationType[7] = new DBTrandef(LCSHIPPING, "Shipping Location");				//13
      iLocationType[8] = new DBTrandef(LCSTAGING, "Staging Location");					//16
      iLocationType[9] = new DBTrandef(LCSTATION, "Station Location");					//11
      iLocationType[10] = new DBTrandef(LCLIFT, "Lift Location");						//20
      iLocationType[11] = new DBTrandef(LCSHUTTLE, "Shuttle Location");					//21
      iLocationType[12] = new DBTrandef(LCOUTOFGAUGE, "OOG Location");					//22
      tm.put("ILOCATIONTYPE", iLocationType);
    

                                      // Object array for "iLocnOrderStrategy"
      DBTrandef[] iLocnOrderStrategy = new DBTrandef[2];
      iLocnOrderStrategy[0] = new DBTrandef(FIXED, "Fixed Location Order");
      iLocnOrderStrategy[1] = new DBTrandef(RANDOM, "Random Location Ordering");

      tm.put("ILOCNORDERSTRATEGY", iLocnOrderStrategy);
    

                                      // Object array for "iModifyAllowed"
      DBTrandef[] iModifyAllowed = new DBTrandef[2];
      iModifyAllowed[0] = new DBTrandef(NO, "No");
      iModifyAllowed[1] = new DBTrandef(YES, "Yes");

      tm.put("IMODIFYALLOWED", iModifyAllowed);
    

                                      // Object array for "iMoveCategory"
      DBTrandef[] iMoveCategory = new DBTrandef[6];
      iMoveCategory[0] = new DBTrandef(ANY_REQUEST, "Any Work Request");
      iMoveCategory[1] = new DBTrandef(CARTPICK_REQUEST, "Cart-Pick Request");
      iMoveCategory[2] = new DBTrandef(CYCLECOUNT_REQUEST, "Cycle-Count Request");
      iMoveCategory[3] = new DBTrandef(PICK_REQUEST, "Pick Request");
      iMoveCategory[4] = new DBTrandef(REPLENISHMENT_REQUEST, "Replenishment Request");
      iMoveCategory[5] = new DBTrandef(STORE_REQUEST, "Store Request");

      tm.put("IMOVECATEGORY", iMoveCategory);
    
      DBTrandef[] iCmdStatus = new DBTrandef[7];
      
      iCmdStatus[0] = new DBTrandef(CMD_UNKNOWN, "Unknown");
      iCmdStatus[1] = new DBTrandef(CMD_READY, "Ready");
      iCmdStatus[2] = new DBTrandef(CMD_COMPLETED, "Completed");
      iCmdStatus[3] = new DBTrandef(CMD_DELETED, "Deleted");
      iCmdStatus[4] = new DBTrandef(CMD_PROCCESSING, "Processing");
      iCmdStatus[5] = new DBTrandef(CMD_COMMANDED, "Commanded");
      iCmdStatus[6] = new DBTrandef(CMD_ERROR, "Error");

      tm.put("ICMDSTATUS", iCmdStatus);
      
      DBTrandef[] iCmdOrderType = new DBTrandef[3];
      
      iCmdOrderType[0] = new DBTrandef(CMD_STORAGE, "Storage");
      iCmdOrderType[1] = new DBTrandef(CMD_RETRIEVAL, "Retrieval");
      iCmdOrderType[2] = new DBTrandef(CMD_RACK, "Rack To Rack");

      tm.put("ICMDORDERTYPE", iCmdOrderType);
      
      DBTrandef[] iCmdMoveType = new DBTrandef[4];
      
      iCmdMoveType[0] = new DBTrandef(CMD_DIRECT, "Station to Station");
      iCmdMoveType[1] = new DBTrandef(CMD_DIRECT_LOC, "Location to Location");
      iCmdMoveType[2] = new DBTrandef(CMD_STOREAGE_LOC, "Station to Location");
      iCmdMoveType[3] = new DBTrandef(CMD_LOC_RETRIEVAL, "Location to Station");

      tm.put("ICMDMOVETYPE", iCmdMoveType);


                                      // Object array for "iMoveStatus"
      DBTrandef[] iMoveStatus = new DBTrandef[2];
      iMoveStatus[0] = new DBTrandef(ASSIGNED, "Assigned");
      iMoveStatus[1] = new DBTrandef(AVAILABLE, "Available");

      tm.put("IMOVESTATUS", iMoveStatus);
    

                                      // Object array for "iMoveType"
      DBTrandef[] iMoveType = new DBTrandef[5];
      iMoveType[0] = new DBTrandef(CYCLECOUNTMOVE, "Cycle-Count Move");
      iMoveType[1] = new DBTrandef(EMPTYMOVE, "Empty Cont. Move");
      iMoveType[2] = new DBTrandef(ITEMMOVE, "Item Move");
      iMoveType[3] = new DBTrandef(LOADMOVE, "Load Move");
      iMoveType[4] = new DBTrandef(REPLENISHMENTMOVE, "Replenishment Move");

      tm.put("IMOVETYPE", iMoveType);
    

                                      // Object array for "iMultipleLoad"
      DBTrandef[] iMultipleLoad = new DBTrandef[2];
      iMultipleLoad[0] = new DBTrandef(NO, "No");
      iMultipleLoad[1] = new DBTrandef(YES, "Yes");

      tm.put("IMULTIPLELOAD", iMultipleLoad);
    

                                      // Object array for "iOneLoadPerLoc"
      DBTrandef[] iOneLoadPerLoc = new DBTrandef[2];
      iOneLoadPerLoc[0] = new DBTrandef(NO, "No");
      iOneLoadPerLoc[1] = new DBTrandef(YES, "Yes");

      tm.put("IONELOADPERLOC", iOneLoadPerLoc);
    

                                      // Object array for "iOperationalStatus"
      DBTrandef[] iOperationalStatus = new DBTrandef[3];
      iOperationalStatus[0] = new DBTrandef(APPOFFLINE, "Offline");
      iOperationalStatus[1] = new DBTrandef(APPONLINE, "Online");
      iOperationalStatus[2] = new DBTrandef(INOP, "Inoperable");

      tm.put("IOPERATIONALSTATUS", iOperationalStatus);
    

                                      // Object array for "iOrderType"
      DBTrandef[] iOrderType = new DBTrandef[5];
      iOrderType[0] = new DBTrandef(CONTAINER, "Container Order");
      iOrderType[1] = new DBTrandef(CYCLECOUNT, "Cycle Count Order");
      iOrderType[2] = new DBTrandef(FULLLOADOUT, "Full Load Order");
      iOrderType[3] = new DBTrandef(ITEMORDER, "Item Order");
      iOrderType[4] = new DBTrandef(REPLENISHMENT, "Replenishment Order");

      tm.put("IORDERTYPE", iOrderType);
    

                                      // Object array for "iOrderStatus"
      DBTrandef[] iOrderStatus = new DBTrandef[15];
      iOrderStatus[0] = new DBTrandef(ALLOCATENOW, "Allocate Immediately");
      iOrderStatus[1] = new DBTrandef(ALLOCATING, "Allocating");
      iOrderStatus[2] = new DBTrandef(DONE, "Done");
      iOrderStatus[3] = new DBTrandef(HOLD, "Hold");
      iOrderStatus[4] = new DBTrandef(KILLED, "Killed");
      iOrderStatus[5] = new DBTrandef(LOCWAIT, "Waiting For Location");
      iOrderStatus[6] = new DBTrandef(ORBUILDING, "Building");
      iOrderStatus[7] = new DBTrandef(ORERROR, "In Error");
      iOrderStatus[8] = new DBTrandef(PICKCOMP, "Pick Complete");
      iOrderStatus[9] = new DBTrandef(READY, "Ready");
      iOrderStatus[10] = new DBTrandef(REALLOC, "Reallocate");
      iOrderStatus[11] = new DBTrandef(SCHEDULED, "Scheduled");
      iOrderStatus[12] = new DBTrandef(SHORT, "Short");
      iOrderStatus[13] = new DBTrandef(SHORTLOCWAIT, "Short (Waiting for Location)");
      iOrderStatus[14] = new DBTrandef(UNKNOWN, "Unknown");

      tm.put("IORDERSTATUS", iOrderStatus);
    

                                      // Object array for "iNextStatus"
      DBTrandef[] iNextStatus = new DBTrandef[15];
      iNextStatus[0] = new DBTrandef(ALLOCATENOW, "Allocate Immediately");
      iNextStatus[1] = new DBTrandef(ALLOCATING, "Allocating");
      iNextStatus[2] = new DBTrandef(DONE, "Done");
      iNextStatus[3] = new DBTrandef(HOLD, "Hold");
      iNextStatus[4] = new DBTrandef(KILLED, "Killed");
      iNextStatus[5] = new DBTrandef(LOCWAIT, "Waiting For Location");
      iNextStatus[6] = new DBTrandef(ORBUILDING, "Building");
      iNextStatus[7] = new DBTrandef(ORERROR, "In Error");
      iNextStatus[8] = new DBTrandef(PICKCOMP, "Pick Complete");
      iNextStatus[9] = new DBTrandef(READY, "Ready");
      iNextStatus[10] = new DBTrandef(REALLOC, "Reallocate");
      iNextStatus[11] = new DBTrandef(SCHEDULED, "Scheduled");
      iNextStatus[12] = new DBTrandef(SHORT, "Short");
      iNextStatus[13] = new DBTrandef(SHORTLOCWAIT, "Short (Waiting for Location)");
      iNextStatus[14] = new DBTrandef(UNKNOWN, "Unknown");

      tm.put("INEXTSTATUS", iNextStatus);
    

                                      // Object array for "iNotifyHost"
      DBTrandef[] iNotifyHost = new DBTrandef[2];
      iNotifyHost[0] = new DBTrandef(NO, "No");
      iNotifyHost[1] = new DBTrandef(YES, "Yes");

      tm.put("INOTIFYHOST", iNotifyHost);
    

                                      // Object array for "iPhysicalStatus"
      DBTrandef[] iPhysicalStatus = new DBTrandef[4];
      iPhysicalStatus[0] = new DBTrandef(DISCONNECTED, "Disconnected");
      iPhysicalStatus[1] = new DBTrandef(ERROR, "Error");
      iPhysicalStatus[2] = new DBTrandef(OFFLINE, "Offline");
      iPhysicalStatus[3] = new DBTrandef(ONLINE, "Online");

      tm.put("IPHYSICALSTATUS", iPhysicalStatus);
    

                                      // Object array for "iPoReceiveAll"
      DBTrandef[] iPoReceiveAll = new DBTrandef[2];
      iPoReceiveAll[0] = new DBTrandef(NO, "No");
      iPoReceiveAll[1] = new DBTrandef(YES, "Yes");

      tm.put("IPORECEIVEALL", iPoReceiveAll);
    

                                      // Object array for "iPriorityAllocation"
      DBTrandef[] iPriorityAllocation = new DBTrandef[2];
      iPriorityAllocation[0] = new DBTrandef(NO, "No");
      iPriorityAllocation[1] = new DBTrandef(YES, "Yes");

      tm.put("IPRIORITYALLOCATION", iPriorityAllocation);
    

                                      // Object array for "iPriorityCategory"
      DBTrandef[] iPriorityCategory = new DBTrandef[3];
      iPriorityCategory[0] = new DBTrandef(EMPTY_LOCATION_CHECK, "Empty Location Check");
      iPriorityCategory[1] = new DBTrandef(PLANNED_RETRIEVAL, "Planned Retrieval");
      iPriorityCategory[2] = new DBTrandef(URGENT_RETRIEVAL, "Urgent Retrieval");

      tm.put("IPRIORITYCATEGORY", iPriorityCategory);
    

                                      // Object array for "iPurchaseOrderStatus"
      DBTrandef[] iPurchaseOrderStatus = new DBTrandef[8];
      iPurchaseOrderStatus[0] = new DBTrandef(ERBUILDING, "Building");
      iPurchaseOrderStatus[1] = new DBTrandef(ERCOMPLETE, "Complete");
      iPurchaseOrderStatus[2] = new DBTrandef(EREXPECTED, "Expected");
      iPurchaseOrderStatus[3] = new DBTrandef(ERFORCED, "Forced Close");
      iPurchaseOrderStatus[4] = new DBTrandef(ERHISTORY, "History");
      iPurchaseOrderStatus[5] = new DBTrandef(ERPENDING, "Pending");
      iPurchaseOrderStatus[6] = new DBTrandef(ERRECEIVING, "Receiving");
      iPurchaseOrderStatus[7] = new DBTrandef(NONEXIST, "Non-Existent");

      tm.put("IPURCHASEORDERSTATUS", iPurchaseOrderStatus);
    

                                      // Object array for "iReasonCategory"
      DBTrandef[] iReasonCategory = new DBTrandef[3];
      iReasonCategory[0] = new DBTrandef(REASONADJUST, "Adjust Inventory");
      iReasonCategory[1] = new DBTrandef(REASONCUSTOM, "Custom Reason");
      iReasonCategory[2] = new DBTrandef(REASONHOLD, "Hold Inventory");

      tm.put("IREASONCATEGORY", iReasonCategory);
    

                                      // Object array for "iReInputFlag"
      DBTrandef[] iReInputFlag = new DBTrandef[2];
      iReInputFlag[0] = new DBTrandef(NO_REINPUT, "No Reinputting");
      iReInputFlag[1] = new DBTrandef(REINPUT_SAME_LOC, "Reinput To Same Bin");

      tm.put("IREINPUTFLAG", iReInputFlag);
    

                                      // Object array for "iRememberLastLogin"
      DBTrandef[] iRememberLastLogin = new DBTrandef[2];
      iRememberLastLogin[0] = new DBTrandef(NO, "No");
      iRememberLastLogin[1] = new DBTrandef(YES, "Yes");

      tm.put("IREMEMBERLASTLOGIN", iRememberLastLogin);
    

                                      // Object array for "iReplenishNow"
      DBTrandef[] iReplenishNow = new DBTrandef[4];
      iReplenishNow[0] = new DBTrandef(DLACTIVE, "Active");
      iReplenishNow[1] = new DBTrandef(DLINACTIVE, "Inactive");
      iReplenishNow[2] = new DBTrandef(DLUNREPLEN, "Unreplenishing");
      iReplenishNow[3] = new DBTrandef(DLWAIT, "Waiting");

      tm.put("IREPLENISHNOW", iReplenishNow);
    

                                      // Object array for "iReplenishType"
      DBTrandef[] iReplenishType = new DBTrandef[4];
      iReplenishType[0] = new DBTrandef(INNER_PACK, "Inner Pack");
      iReplenishType[1] = new DBTrandef(LOAD, "Load");
      iReplenishType[2] = new DBTrandef(PIECE, "Piece");
      iReplenishType[3] = new DBTrandef(UNIT, "Unit");

      tm.put("IREPLENISHTYPE", iReplenishType);
    

                                      // Object array for "iRetrieveCommandDetail"
      DBTrandef[] iRetrieveCommandDetail = new DBTrandef[4];
      iRetrieveCommandDetail[0] = new DBTrandef(ADDING_RETRIEVAL, "Adding Retrieval");
      iRetrieveCommandDetail[1] = new DBTrandef(INVENTORY_CHECK, "Inventory Check");
      iRetrieveCommandDetail[2] = new DBTrandef(PICKING_RETRIEVAL, "Picking Retrieval");
      iRetrieveCommandDetail[3] = new DBTrandef(UNIT_RETRIEVAL, "Unit Retrieval");

      tm.put("IRETRIEVECOMMANDDETAIL", iRetrieveCommandDetail);
    

                                      // Object array for "iRoleType"
      DBTrandef[] iRoleType = new DBTrandef[3];
      iRoleType[0] = new DBTrandef(CREATOR, "Creator");
      iRoleType[1] = new DBTrandef(SUPER_USER, "Super User");
      iRoleType[2] = new DBTrandef(WORKER, "Worker");

      tm.put("IROLETYPE", iRoleType);
    

                                      // Object array for "iRouteOnOff"
      DBTrandef[] iRouteOnOff = new DBTrandef[2];
      iRouteOnOff[0] = new DBTrandef(OFF, "Off");
      iRouteOnOff[1] = new DBTrandef(ON, "On");

      tm.put("IROUTEONOFF", iRouteOnOff);
    

                                      // Object array for "iScreenChangeAllowed"
      DBTrandef[] iScreenChangeAllowed = new DBTrandef[2];
      iScreenChangeAllowed[0] = new DBTrandef(NO, "No");
      iScreenChangeAllowed[1] = new DBTrandef(YES, "Yes");

      tm.put("ISCREENCHANGEALLOWED", iScreenChangeAllowed);
    

                                      // Object array for "iSequenceType"
      DBTrandef[] iSequenceType = new DBTrandef[3];
      iSequenceType[0] = new DBTrandef(DEVICE_SEQ, "Device Message Sequencer");
      iSequenceType[1] = new DBTrandef(HOST_SEQ, "Host Message Sequencer");
      iSequenceType[2] = new DBTrandef(OTHER_SEQ, "Other Sequencer");

      tm.put("ISEQUENCETYPE", iSequenceType);
    

                                      // Object array for "iShipmentStatus"
      DBTrandef[] iShipmentStatus = new DBTrandef[4];
      iShipmentStatus[0] = new DBTrandef(SHIPMENTBUILD, "Building");
      iShipmentStatus[1] = new DBTrandef(SHIPMENTCLOSED, "Closed");
      iShipmentStatus[2] = new DBTrandef(SHIPMENTHOLD, "Hold");
      iShipmentStatus[3] = new DBTrandef(SHIPMENTOPEN, "Open");

      tm.put("ISHIPMENTSTATUS", iShipmentStatus);
    

                                      // Object array for "iSimulate"
      DBTrandef[] iSimulate = new DBTrandef[2];
      iSimulate[0] = new DBTrandef(OFF, "Off");
      iSimulate[1] = new DBTrandef(ON, "On");

      tm.put("ISIMULATE", iSimulate);
    

                                      // Object array for "iStationType"
      DBTrandef[] iStationType = new DBTrandef[20];
      iStationType[0] = new DBTrandef(AGC_TRANSFER, "AGC Transfer Station"); 				// 	230
      iStationType[1] = new DBTrandef(AGV_STATION, "AGV Station");							//	231
      iStationType[2] = new DBTrandef(CONSOLIDATION, "Consolidation");						//	226
      iStationType[3] = new DBTrandef(CONVEYOR, "Conveyor Station");						//	227
      iStationType[4] = new DBTrandef(INPUT, "Input");										//	224
      iStationType[5] = new DBTrandef(OUTPUT, "Output");									//	223
      iStationType[6] = new DBTrandef(PDSTAND, "P&D Stand");								//	222
      iStationType[7] = new DBTrandef(PTL_INPUT, "PTL Input");								//	232
      iStationType[8] = new DBTrandef(PTL_OUTPUT, "PTL Output");							//	233
      iStationType[9] = new DBTrandef(REVERSIBLE, "Reversible");							//	225
      iStationType[10] = new DBTrandef(SHIPPING, "Shipping");								//	228
      iStationType[11] = new DBTrandef(TRANSFER_STATION, "Transfer Station");				//	229
      iStationType[12] = new DBTrandef(USHAPE_IN, "U-Shape In");							//	220
      iStationType[13] = new DBTrandef(USHAPE_OUT, "U-Shape Out");							//	221
      iStationType[14] = new DBTrandef(LIFT_TRANSFER_INPUT, "Lift Transfer Input");			//	234
      iStationType[15] = new DBTrandef(LIFT_TRANSFER_OUT, "Lift Transfer Output");			//	235
      iStationType[16] = new DBTrandef(SHUTTLE_TRANSFER_INPUT, "Shuttle Transfer Input");	//	236
      iStationType[17] = new DBTrandef(SHUTTLE_TRANSFER_OUT, "Shuttle Transfer Output");	//	237
      iStationType[18] = new DBTrandef(LIFT_TRANSFER_REVERSIBLE, "Lift Transfer Reversible");	//	238
      iStationType[19] = new DBTrandef(SHUTTLE_TRANSFER_REVERSIBLE, "Shuttle Transfer Reversible");	//	239
      
      tm.put("ISTATIONTYPE", iStationType);
    

                                      // Object array for "iStatus"
      DBTrandef[] iStatus = new DBTrandef[3];
      iStatus[0] = new DBTrandef(CAPTIVEINSERT, "Captive Insert");
      iStatus[1] = new DBTrandef(STNOFFLINE, "Offline");
      iStatus[2] = new DBTrandef(STORERETRIEVE, "Store+Retrieve");

      tm.put("ISTATUS", iStatus);
    

                                      // Object array for "iStorageFlag"
      DBTrandef[] iStorageFlag = new DBTrandef[4];
      iStorageFlag[0] = new DBTrandef(MIXALL, "Any Item - Any Lot");
      iStorageFlag[1] = new DBTrandef(MIXLOTS_ONEITEM, "One Item-Mixed Lots");
      iStorageFlag[2] = new DBTrandef(ONELOT_ONEITEM, "One Item-One Lot");
      iStorageFlag[3] = new DBTrandef(ONELOT_PERITEM, "One Lot Per Item");

      tm.put("ISTORAGEFLAG", iStorageFlag);
    

                                      // Object array for "iSwapZoneStatus"
      DBTrandef[] iSwapZoneStatus = new DBTrandef[4];
      iSwapZoneStatus[0] = new DBTrandef(SWAP_BROKEN, "Broken");
      iSwapZoneStatus[1] = new DBTrandef(SWAP_HEALTHY, "Healthy");
      iSwapZoneStatus[2] = new DBTrandef(SWAP_SWAP_BACK, "Retrieving Back Load");
      iSwapZoneStatus[3] = new DBTrandef(SWAP_SWAP_FRONT, "Swapping Front Load");

      tm.put("ISWAPZONESTATUS", iSwapZoneStatus);
    

                                      // Object array for "iTranCategory"
      DBTrandef[] iTranCategory = new DBTrandef[5];
      iTranCategory[0] = new DBTrandef(INVENTORY_TRAN, "Inventory");
      iTranCategory[1] = new DBTrandef(LOAD_TRAN, "Load Movement");
      iTranCategory[2] = new DBTrandef(ORDER_TRAN, "Ordering");
      iTranCategory[3] = new DBTrandef(SYSTEM_TRAN, "System");
      iTranCategory[4] = new DBTrandef(USER_TRAN, "User");

      tm.put("ITRANCATEGORY", iTranCategory);
    

                                      // Object array for "iTranType"
      DBTrandef[] iTranType = new DBTrandef[35];
      iTranType[0] = new DBTrandef(ADD, "Generic Add Data");
      iTranType[1] = new DBTrandef(ADD_EXPECTED_RECEIPT, "Add Expected Receipt");
      iTranType[2] = new DBTrandef(ADD_EXPECTED_RECEIPT_LINE, "Add Expected Receipt Line");
      iTranType[3] = new DBTrandef(ADD_ITEM, "Add Inventory");
      iTranType[4] = new DBTrandef(ADD_ITEM_MASTER, "Add Item Master");
      iTranType[5] = new DBTrandef(ADD_LOAD, "Add Load");
      iTranType[6] = new DBTrandef(ADD_ORDER, "Add Order");
      iTranType[7] = new DBTrandef(ADD_ORDER_LINE, "Add Order Line");
      iTranType[8] = new DBTrandef(COMPLETION, "Completion");
      iTranType[9] = new DBTrandef(COUNT, "Counting");
      iTranType[10] = new DBTrandef(CYCLE_COUNT, "Cycle Counting");
      iTranType[11] = new DBTrandef(DELETE, "Generic Delete Data");
      iTranType[12] = new DBTrandef(DELETE_EXPECTED_RECEIPT, "Delete Expected Receipt");
      iTranType[13] = new DBTrandef(DELETE_EXPECTED_RECEIPT_LINE, "Delete Expected Receipt Line");
      iTranType[14] = new DBTrandef(DELETE_ITEM, "Delete Inventory");
      iTranType[15] = new DBTrandef(DELETE_ITEM_MASTER, "Delete Item Master");
      iTranType[16] = new DBTrandef(DELETE_LOAD, "Delete Load");
      iTranType[17] = new DBTrandef(DELETE_ORDER, "Delete Order");
      iTranType[18] = new DBTrandef(DELETE_ORDER_LINE, "Delete Order Line");
      iTranType[19] = new DBTrandef(ITEM_PICK, "Item Picking");
      iTranType[20] = new DBTrandef(ITEM_RECEIPT, "Item Receipt");
      iTranType[21] = new DBTrandef(ITEM_SHIP, "Item Ship");
      iTranType[22] = new DBTrandef(LOAD_SCHED, "Load Schedule");
      iTranType[23] = new DBTrandef(LOGIN, "User Login");
      iTranType[24] = new DBTrandef(LOGOUT, "User Logout");
      iTranType[25] = new DBTrandef(MODIFY, "Generic Modify Data");
      iTranType[26] = new DBTrandef(MODIFY_EXPECTED_RECEIPT, "Modify Expected Receipt");
      iTranType[27] = new DBTrandef(MODIFY_EXPECTED_RECEIPT_LINE, "Modify Expected Receipt Line");
      iTranType[28] = new DBTrandef(MODIFY_ITEM, "Modify Inventory");
      iTranType[29] = new DBTrandef(MODIFY_ITEM_MASTER, "Modify Item Master");
      iTranType[30] = new DBTrandef(MODIFY_LOAD, "Modify Load");
      iTranType[31] = new DBTrandef(MODIFY_ORDER, "Modify Order");
      iTranType[32] = new DBTrandef(MODIFY_ORDER_LINE, "Modify Order Line");
      iTranType[33] = new DBTrandef(TRANSFER, "Transfer Inventory/Load");
      iTranType[34] = new DBTrandef(DELETE_MOVE_COMMAND, "Delete Move Command");
      tm.put("ITRANTYPE", iTranType);
    

                                      // Object array for "typeid"
      DBTrandef[] typeid = new DBTrandef[6];
      typeid[0] = new DBTrandef(GS_BOOLEAN, "Boolean");
      typeid[1] = new DBTrandef(GS_DATETIME, "DateTime");
      typeid[2] = new DBTrandef(GS_DOUBLE, "Double");
      typeid[3] = new DBTrandef(GS_INTEGER, "Integer");
      typeid[4] = new DBTrandef(GS_SELECTION, "Selection");
      typeid[5] = new DBTrandef(GS_STRING, "String");

      tm.put("TYPEID", typeid);
    

                                      // Object array for "iViewAllowed"
      DBTrandef[] iViewAllowed = new DBTrandef[2];
      iViewAllowed[0] = new DBTrandef(NO, "No");
      iViewAllowed[1] = new DBTrandef(YES, "Yes");

      tm.put("IVIEWALLOWED", iViewAllowed);
    

                                      // Object array for "iWarehouseStatus"
      DBTrandef[] iWarehouseStatus = new DBTrandef[2];
      iWarehouseStatus[0] = new DBTrandef(WARAVAIL, "Available");
      iWarehouseStatus[1] = new DBTrandef(WARHOLD, "Hold");

      tm.put("IWAREHOUSESTATUS", iWarehouseStatus);
    

                                      // Object array for "iWarehouseType"
      DBTrandef[] iWarehouseType = new DBTrandef[4];      
      iWarehouseType[0] = new DBTrandef(REGULAR, "Crane Warehouse");
      iWarehouseType[1] = new DBTrandef(CONVEYOR_WR, "Conveyor Warehouse");
      iWarehouseType[2] = new DBTrandef(SHUTTLE_WR, "Shuttle Warehouse");
      iWarehouseType[3] = new DBTrandef(SUPER, "Super Warehouse");

      tm.put("IWAREHOUSETYPE", iWarehouseType);
      
      }
  }

 /**
  *  Returns string array of translations for a translation name
  *
  * @param  tran_name  String containing translation name
  * @return String array of translations.
  * @exception NoSuchFieldException
  */
  public static String[] getStringList(String tran_name)
         throws NoSuchFieldException
  {
    DBTrandef[] td = null;
                                    // If the Translation name is unknown,
                                    // then throw an exception.
    if ((td = tm.get(tran_name.toUpperCase())) == null)
    {
      throw new NoSuchFieldException("Invalid key value: " + tran_name);
    }
    else
    {                                  // Loop over each Translation object
                                       // and get its string representation.
      int      idx;
      String[] sList = new String[td.length];

      for(idx = 0; idx < td.length; idx++)
      {
        sList[idx] = td[idx].getTranString();
      }

      return(sList);
    }
  }

 /**
  *  Returns string array of translations for a set of translation values.
  *
  * @param tranName  String containing translation name
  * @param tranValues array of integer translations.
  * @return String array of translations.
  * @exception NoSuchFieldException if any of the user specified translation
  *            values are not found.
  */
  public static String[] getStringList(String tranName, int[] tranValues)
          throws NoSuchFieldException
  {
    String[] strTranslations = new String[tranValues.length];
    for(int idx = 0; idx < tranValues.length; idx++)
    {
      strTranslations[idx] = DBTrans.getStringValue(tranName, tranValues[idx]);
    }
    
    return(strTranslations);
  }

 /**
  * Returns a string given a Translation name and value (which 
  * is really an integer from the DBConstants file).
  *               
  * @param  tran_name  String containing Translation name.
  * @param  iPriorityAllocation Integer value for this translation.
  * @return A particular translation string related to <B>tranval</B>
  *         argument.
  * @exception NoSuchFieldException
  */
  public static String getStringValue(String tran_name, double iPriorityAllocation)
         throws NoSuchFieldException
  {
    DBTrandef[] td = null;
    String errString = "";
                                       // If the hash key is unknown, then
                                       // throw an exception.
    if ((td = tm.get(tran_name.toUpperCase())) == null)
    {
      throw new NoSuchFieldException("Invalid key value: " + tran_name);
    }
    else
    {                                  // Loop over each Translation object
                                       // and get its string representation.
      for(int idx = 0; idx < td.length; idx++)
      {
        if (iPriorityAllocation == td[idx].getTranInteger())
        {
            return(td[idx].getTranString());
        }
      }
                                       // Didn't find it. Throw exception.
      errString = "No string value for " + Double.toString(iPriorityAllocation);
      errString += " and translation named \"" + tran_name + "\"";
      throw new NoSuchFieldException(errString);
    }
  }

  /**
   * Returns a string given a Translation name and value (which is really an
   * integer from the DBConstants file).
   * <p>The method logs an error and returns "Unknown ([tranval])" rather than
   * throw an exception.</p>
   * 
   * @param tran_name String containing Translation name.
   * @param tranval Integer value for this translation.
   * @return A particular translation string related to <B>tranval</B> argument.
   */
  public static String getStringValueNoExc(String tran_name, int tranval)
  {
    try
    {
      return getStringValue(tran_name, tranval);
    }
    catch (NoSuchFieldException e)
    {
      Logger.getLogger().logError(e.getMessage());
      return "Unknown (" + tranval + ")";
    }
  }

 /**
  * Returns all integer values in a translation name.
  *               
  * @param  tran_name  String containing Translation name.
  * @return list of integer values for <B>tran_name</B> name.
  */
  public static int[] getIntegerList(String tran_name)
  {
    DBTrandef[] td = null;
    int[] iList = new int[0];

    if ((td = tm.get(tran_name.toUpperCase())) != null)
    {
      iList = new int[td.length];
      for(int idx = 0; idx < td.length; idx++)
      {
        iList[idx] = td[idx].getTranInteger();
      }
    }

    return(iList);
  }

 /**
  * Returns an integer representation for a given translation name, and
  * translation string.
  *               
  * @param  tran_name  String containing Translation name.
  * @param  transtring String containing full Tranlation String.
  * @return integer value for <B>transtring</B>.
  * @exception NoSuchFieldException
  */
  public static int getIntegerValue(String tran_name, String transtring)
         throws NoSuchFieldException
  {
    DBTrandef[] td = null;
    String errString = "";

                                       // If the hash key is unknown, then
                                       // throw an exception.
    if ((td = tm.get(tran_name.toUpperCase())) == null)
    {
      throw new NoSuchFieldException("Invalid key value: " + tran_name);
    }
    else
    {
      String stringValue = new String();
      for(int idx = 0; idx < td.length; idx++)
      {
        stringValue = td[idx].getTranString();
        if (stringValue.equalsIgnoreCase(transtring.trim()))
        {
          return(td[idx].getTranInteger());
        }
      }
                                       // Didn't find it. Throw exception.
      errString = "No Integer value for " + transtring;
      errString += " and translation name \"" + tran_name + "\"";
      throw new NoSuchFieldException(errString);
    }
  }

 /**
  * Convenience method to return integer object from translation as opposed
  * to just an integer.
  *               
  * @param  tran_name  String containing Translation name.
  * @param  transtring String containing full Tranlation String.
  * @return integer value for <B>transtring</B>.
  * @exception NoSuchFieldException
  */
  public static Integer getIntegerObject(String tran_name, String transtring)
         throws NoSuchFieldException
  {
    int iValue = getIntegerValue(tran_name, transtring);

    return(Integer.valueOf(iValue));
  }

 /**
  * Method to check if a string is a translation type.
  *               
  * @param  column_name  String to verify as translation.
  * @return boolean of true if argument is a translation type, false
  * otherwise
  */
  public static boolean isTranslation(String column_name)
  {
    boolean rtn = false;
    
    Iterator<String> keyItr = tm.keySet().iterator();
    while(keyItr.hasNext())
    {
      String sKey = keyItr.next();
      if (column_name.equalsIgnoreCase(sKey))
      {
        rtn = true;
      }
    }

    return(rtn);
  }

 /**
  *  Method figures out the maximum length of a translation
  * @param tran_name The translation name.
  * @return length of the largest translation string.
  */
  public static int getMaxTranslationLength(String tran_name)
  {
    int maxTranLength = 0;
    try
    {
      String[] tranNames = getStringList(tran_name);
      maxTranLength = tranNames[0].length();
      
      for(int idx = 1; idx < tranNames.length; idx++)
      {
        int iTmpLen = tranNames[idx].length();
        if (maxTranLength < iTmpLen) maxTranLength = iTmpLen;
      }
    }
    catch(NoSuchFieldException exc)
    {
      exc.printStackTrace();
    }
    
    return(maxTranLength);
  }
  
  /**
   * Return a list of all translation fields
   * @return
   */
  public static Set<String> getTranslationFields()
  {
    return tm.keySet();
  }
} /*** End of class DBTrans ****/
