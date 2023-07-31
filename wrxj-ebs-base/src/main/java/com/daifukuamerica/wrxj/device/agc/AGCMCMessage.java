package com.daifukuamerica.wrxj.device.agc;

import com.daifukuamerica.wrxj.application.Application;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceEnum;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.MessageConstants;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCUtility;

/**
 * AgcMcMessage is the "<b>M</b>aterial Handling <b>C</b>omputer" message set
 * used to communicate with a Daifuku AGC or SRC.
 *
 * @author       Stephen Kendorski
 * @version 1.0
 */
public class AGCMCMessage extends AGCMessage
{
  private static final int HEADER_LENGTH = 16;

  private static final String DEFAULT_AGCDATA = "      ";
  private static final String DEFAULT_BARCODE = "                              ";
  private static final String DEFAULT_CONTROL_INFO = "                              ";
  private static final String DEFAULT_LOCATION = "000000000";
  private static final String DEFAULT_MACHINEID = "000000";
  private static final String DEFAULT_MACHINENO = "0000";
  private static final String DEFAULT_MCKEY = "        ";
  private static final String DEFAULT_MODELCODE = "00";
  private static final String DEFAULT_STATION = "0000";
  private static final String DEFAULT_TIME = "000000";
  private static final String DEFAULT_WORKNO = "        ";

  // Command classifications for alternative location command
  public static final int CC_BINFULL_NEWLOC  =  1;
  public static final int CC_BINFULL_NEWSTN  =  2;
  public  static final int CC_BINFULL_CANCEL  =  3;
  public static final int CC_BINEMPTY_NEWLOC = 11;
  public  static final int CC_BINEMPTY_CANCEL = 12;
  public static final int CC_SIZEMIS_NEWLOC  = 21;
  public static final int CC_SIZEMIS_NEWSTN  = 22;
  public  static final int CC_SIZEMIS_CANCEL  = 23;

  // AS-21 Error Codes for bin full and bin empty
  public static final String ERROR_BIN_EMPTY_AT_LOCATION = "1114002";
  public static final String ERROR_BIN_FULL_FOR_RETRIEVE = "1114102";
  public static final String ERROR_BIN_FULL_AT_LOCATION = "1114001";
  public static final String ERROR_BIN_FULL_AT_FRONT_LOCATION = "1114101";
  public static final String ERROR_SIZE_MISMATCH = "1114003";

  private int mnIdClassification = 0;
  private String msAgcTransmissionTime = DEFAULT_TIME;
  protected boolean mzAddMiniParse = true;
  protected boolean mzAddShelfToMiniParse = Application.getBoolean(Application.SYSCFG_DOMAIN + "ShelfPosition.AddShelfToMiniParse", false);
  //
  // Data Values for:  3: RequestToTerminateOperation
  //
  /**
   * 0: Regular termination (No operation data returned by AGC)
   * 1: Unconditional termination WITH data retention
   * 2: Unconditional termination WITHOUT data retention
   */
  //
  // Data Values for 22: dateTimeRequest
  //
  // 0: Time modified.
  // 1: Operation start.
  //
  private int mnRequestClassification = 0;
  //
  // Data Values for:  4: TransportDataCancel
  //                   5: TransportCommand
  //
  private String msSourceStationNumber = DEFAULT_STATION;
  private String msDestinationStationNumber = DEFAULT_STATION;
  /**
   * TransportCommand: Alternate store location (all zeros = Reject Station)
   */
  private String msLocationNumber = DEFAULT_LOCATION;
  /**
   * This is the shelf position (also known as the "address" in the MC Comm. Spec.)
   * that every rack location can have.
   */
  private String msShelfPosition = LoadData.DEFAULT_POSITION_VALUE;
  //
  // Data Values for:  5: TransportCommand
  //
  /**
   * 1: Storage
   * 2: Direct Transfer
   */
  private int mnTransportClassification = 1;
  /**
   * 1: Setting in advance
   * 2: Load check setting
   */
  private int mnSettingClassification = 1;
  private int mnGroupNumber = 0;

  private int mnDimensionInformation = 0;
  private String msBCData = DEFAULT_BARCODE;
  private String msWorkNumber = DEFAULT_WORKNO;
  private String msControlInformation = DEFAULT_CONTROL_INFO;
  private String mcKey = DEFAULT_MCKEY;
  /**
   * 1: Destination station changes directions
   * 2: Destination station change is impossible (no alternative location
   *    and reject station)
   */
  private int mnCommandClassification = 1;
  /**
   * Station number for Reject (all zeros = Alternate location)
   */
  private String msRejectStation = DEFAULT_STATION;
  protected String msTerminalOperationCompleteStationNumber;
  protected int mnTerminalOperationCompleteTxfrClassification;
  /**
   * Stores the intact item of receiver change request
   */
  private String msAgcData = DEFAULT_AGCDATA;
  //
  // Data Values for: 21: responseToStartOperationRequest
  //
  /**
   *  0: Normal
   *  3: AGC Status Error
   * 99: Data Error
   */
  private int mnResponseClassification = 0;
  /**
   * Effective only when the responseClassification is "3".
   * The number of AGC who has status error (impossible to start work) is stored.
   */
  private String msErrorDetails = "0";
  /**
   * 0: No reports.
   * 1: System Recovery conducted (transport data eliminated)
   */
  private int mnSystemRecoveryReport = 0;
  //
  // Data Values for: 23: responseToTerminateOperationRequest
  //
  // Effective only when the responseClassification is 1 (termination impossible).
  //
  private String msResponseDetailsMachineId = DEFAULT_MACHINEID;
  private String msResponseDetailsModelCode = DEFAULT_MODELCODE;
  private String msResponseDetailsMachineNumber = DEFAULT_MACHINENO;
  //
  // 0: Normal Completion.
  // 1: Cancellation impossible since the applicable data has already been on
  //    the way.
  // 2: No applicable transport data.
  //
  private int mnCancellationResults = 0;
  //
  private String msArrivalStationNumber = DEFAULT_STATION;
  protected int mnLoadInformation = 0;
  //
  private int mnContinuationClassification = 0;
  private int mnNumberOfReports = 0;
  private String msMachineStatusString = "";
  private class MachineStatus
  {
    String machineId = "";
    int machineTypeCode = 0;
    String machineNumber = "";
    int status = 0;
    String errorCode = "";
  }
  private MachineStatus[] mapMachineStatuses;
  //
  private String msImpossibleLocationString = "";
  private class ImpossibleLocation
  {
    int status = 0;
    String storageClassification = "A";
    String bankNumber = "";
    String startBayNumber = "";
    String startLevelNumber = "";
    String endBayNumber = "";
    String endLevelNumber = "";
  }

  public static final int MAX_IMPOSSIBLE_REPORTS = 30;
  private ImpossibleLocation[] mapImpossibleLocations = new ImpossibleLocation[MAX_IMPOSSIBLE_REPORTS];

  //
  protected String msRetrievalDataString = "";
  private class RetrievalData
  {
    String mmsMcKey = AGCDeviceConstants.EMPTYMCKEY;
    int transportationClassification = 0;
    int category = 0;
    int completionClassification = 0;
    //
    int reInputtingFlag = 0;
    int retrievalCommandDetail = 0;
    int mmnGroupNumber = 0;
    //
    String mmsSourceStationNumber = DEFAULT_STATION;
    String mmsDestinationStationNumber = DEFAULT_STATION;
    String mmsLocationNumber = DEFAULT_LOCATION;
    String mmsShelfPosition = LoadData.DEFAULT_POSITION_VALUE;
    String mmsShelfToShelfLocationNumber = DEFAULT_LOCATION;
    String mmsShelfToShelfPosition = LoadData.DEFAULT_POSITION_VALUE;
    int dimension = 0;
    String mmsBcData = DEFAULT_BARCODE;
    String mmsWorkNumber = DEFAULT_WORKNO;
    String mmsControlInformation = DEFAULT_CONTROL_INFO;
  }
  private RetrievalData[] mapRetrievalDatas = new RetrievalData[2];
  /**
    1: Start.
    2: Stop.
  */
  private int mnStartStopClassification = 0;
  private String msRetrievalStationNumber = "";
  private int mnInabiltyToStartReason = 0;
  private String msOperationModeChangeStation = DEFAULT_STATION;
  private int mnRequestResponse = 0;
  private int mnOperationModeChangeCmd = 0;
  private String msAlternateLocation = DEFAULT_LOCATION + LoadData.DEFAULT_POSITION_VALUE;
  private int mnLoadSizeInformation = 0;
  private String msAlternateStationNumber = DEFAULT_STATION;
  //
  private int mnResponseClassification2 = 0;
  protected String msMCKey2 = DEFAULT_MCKEY;
  //
  private int mnCompletionMode = 0;
  private String msResponseDetails = null;


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public AGCMCMessage()
  {
    for (int i = 0; i < 2; i++)
    {
      mapRetrievalDatas[i] = new RetrievalData();
    }
    setMachineStatusSize(100);
    for (int i = 0; i < 30; i++)
    {
      mapImpossibleLocations[i] = new ImpossibleLocation();
    }
    clearAgcMessageData();
  }

  /**
   * Set the size of the Machine Status array.  This method creates a new
   * array, so any information in the old array is lost.
   *
   * @param inSize
   */
  public void setMachineStatusSize(int inSize)
  {
    mapMachineStatuses = new MachineStatus[inSize];
    for (int i = 0; i < mapMachineStatuses.length; i++)
    {
      mapMachineStatuses[i] = new MachineStatus();
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void toDataValues(String isMessageString)
  {
    setValidMessage(true);
    try
    {
        setID(getIntFromTwoAsciiDigits(isMessageString, 0));
        setIDClassification(getIntFromTwoAsciiDigits(isMessageString, 2));
        setTransmissionTime(isMessageString.substring(4, 4+6));
        setAGCTransmissionTime(isMessageString.substring(10, 10+6));
        setMessageData(isMessageString.substring(AGCMCMessage.HEADER_LENGTH));
    }
    catch (Exception e)
    {
      if (getValidMessage())
      {
        setValidMessage(false);
        setInvalidMessageDescription("#####  IndexOutOfBoundsException -- Header \""
            + isMessageString + "\"  #####");
      }
      return;
    }
    //
    // Extract the data values from the child message's data based on the "id".
    //
    messageToDataValues(getID());
  }

  /*--------------------------------------------------------------------------*/
  protected void messageToDataValues(int messageId)
  {
    switch (messageId)
    {
      case  1: requestToStartOperationToDataValues(); break;
      case  2: dateTimeDataToDataValues(); break;
      case  3: requestToTerminateOperationToDataValues(); break;
      case  4: transportDataCancelToDataValues(); break;
      case  5: transportCommandToDataValues(); break;
      case  8: destinationStationChangeCmdToDataValues(); break;
      case 10: machineStatusInquiryToDataValues(); break;
      case 11: alternativeLocationCmdToDataValues(); break;
      case 12: retrievalCmdToDataValues(); break;
      case 16: simultaneousStartStopCmdToDataValues(); break;
      case 19: communicationTestRequestToDataValues(); break;
      case 20: responseToCommunicationTestRequestToDataValues(); break; // ID 20
      case 21: responseToOperationStartRequestToDataValues(); break;
      case 22: dateTimeRequestToDataValues(); break;
      case 23: responseToOperationTerminationRequestToDataValues(); break;
      case 24: responseToTransportDataCancelToDataValues(); break;
      case 25: responseToTransportCommandToDataValues(); break;
      case 26: arrivalReportToDataValues(); break;
      case 27: requestDestinationStationChangeToDataValues(); break;
      case 28: responseToDestinationStationChangeCmdToDataValues(); break;
      case 30: machineStatusReportToDataValues(); break;
      case 31: responseToAlternativeLocationCmdToDataValues(); break;
      case 32: responseToRetrievalCmdToDataValues(); break;
      case 33: operationCompletionReportToDataValues(); break;
      case 35: transportDataDeletionReportToDataValues(); break;
      case 36: simultaneousStartImproperReportToDataValues(); break;
      case 39: responseToCommunicationTestRequestToDataValues(); break; // ID 39
      case 40: communicationTestRequestFromAgcToDataValues(); break;
      case 41: responseToOperationModeChangeRequestToDataValues(); break;
      case 42: operationModeChangeCmdToDataValues(); break;
      case 45: terminalOperationCompletionReportToDataValues(); break;
      case 46: responseToRetrievalTriggerToDataValues(); break;
      case 47: requestRetrievalTriggerRepetitionToDataValues(); break;
      case 50: dataMessageToDataValues(); break; // ID 50
      case 51: requestAccessImpossibleLocationsToDataValues(); break;
      case 54: DOOutputInstructionToDataValues(); break;
      case 58: requestToStartSystemRecoveryToDataValues(); break;
      case 59: requestToTerminateSystemRecoveryToDataValues(); break;
      case 61: operationModeChangeRequestFromAgcToDataValues(); break;
      case 62: responseToOperationModeChangeCmdToDataValues(); break;
      case 63: operationModeChangeCompletionReportToDataValues(); break;
      case 64: pickupCompletionReportToDataValues(); break;
      case 66: retrievalTriggerToDataValues(); break;
      case 68: triggerOfOperationIndicationToDataValues(); break;
      case 70: agcStationLPStatusReportToDataValues(); break;
      case 71: accessImpossibleLocationsReportToDataValues(); break;
      case 78: responseToRequestToStartSystemRecoveryToDataValues(); break;
      case 79: responseToRequestToTerminateSystemRecoveryToDataValues(); break;
      default: break;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Message Instances.
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Messages sent to AGC From RTS
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  public String requestToStartOperation()  //ID 01
  {
    setID(1);
    setResponseMessage(false);
    setDateTimeString();
    setMessageAsString(headerToString() + getDateTimeString());
    requestToStartOperationGetParsed();
    return getMessageAsString();
  }

  private void requestToStartOperationToDataValues()
  {
    setDateTimeString(getSubString(getMessageData(), 0, 14));
    requestToStartOperationGetParsed();
  }

  private void requestToStartOperationGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - RequestToStartOperation - " +
                          getSubString(getDateTimeString(), 4, 2) + "/" +
                          getSubString(getDateTimeString(), 6, 2) + "/" +
                          getSubString(getDateTimeString(), 0, 4) + "  " +

                          getSubString(getDateTimeString(), 8, 2) + ":" +
                          getSubString(getDateTimeString(), 10, 2) + ":" +
                          getSubString(getDateTimeString(), 12, 2) +
                          "  hdr: " + headerGetParsed());
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  public void dateAndTime()
  {
    dateTimeDataToString();
  }

  private String dateTimeDataToString() // ID 02
  {
    setID(2);
    setResponseMessage(false);
    setDateTimeString();
    setMessageAsString(headerToString() + getDateTimeString());
    dateTimeDataGetParsed();
    return getMessageAsString();
  }

  private void dateTimeDataToDataValues()
  {
    setDateTimeString(getSubString(getMessageData(), 0,14));
    dateTimeDataGetParsed();
  }

  private void dateTimeDataGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - DateTimeData - " +
                          getSubString(getDateTimeString(), 4, 2) + "/" +
                          getSubString(getDateTimeString(), 6, 2) + "/" +
                          getSubString(getDateTimeString(), 0, 4) + "  " +

                          getSubString(getDateTimeString(), 8, 2) + ":" +
                          getSubString(getDateTimeString(), 10, 2) + ":" +
                          getSubString(getDateTimeString(), 12, 2) +
                          "  hdr: " + headerGetParsed());
  }
  /*--------------------------------------------------------------------------*/
  public String requestToTerminateOperation(int inRequestClassification) // ID 03
  {
    setID(3);
    setRequestClassification(inRequestClassification);
    setResponseMessage(false);
    setMessageAsString(headerToString()
        + ASCII_DIGITS[getRequestClassification()]);
    requestToTerminateOperationGetParsed();
    return getMessageAsString();
  }

  private void requestToTerminateOperationToDataValues()
  {
    setRequestClassification(getIntDigit(getMessageData(), 0));
    requestToTerminateOperationGetParsed();
  }

  private void requestToTerminateOperationGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - RequestToTerminateOperation - ";
    switch (getRequestClassification())
    {
      case 0: vsParsed = vsParsed + "Regular Termination"; break;
      case 1: vsParsed = vsParsed + "Unconditional Termination With Data RETENTION"; break;
      case 2: vsParsed = vsParsed + "Unconditional Termination With Data DELETION"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getRequestClassification();
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public String transportDataCancelToString() // ID 04
  {
    setID(4);
    setResponseMessage(false);
    transportDataCancelGetParsed();
    setMessageAsString(headerToString() +
                       parsedMcKey(getMCKey()) +
                       getSourceStationNumber() +
                       getDestinationStationNumber() +
                       getSubString(getLocationNumber(), 0, 9) +
                       getShelfPosition());
      return getMessageAsString();
  }

  private void  transportDataCancelToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setSourceStationNumber(getSubString(getMessageData(), 8, 4));
    setDestinationStationNumber(getSubString(getMessageData(), 12, 4));
    setLocationNumber(getSubString(getMessageData(), 16, 9));
    transportDataCancelGetParsed();
  }

  private void transportDataCancelGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID())
        + " - TransportDataCancel - mcKey \"" + getMCKey() + "\""
        + " - SourceStation#: " + getSourceStationNumber()
        + "  DestinationStation#: " + getDestinationStationNumber()
        + "  Location# \"" + getLocationNumber() + "\"";
    setParsedMessageString(vsParsed);
  }
  /*--------------------------------------------------------------------------*/

  /**
   * Transfer a Load from a Station to a Station.
   *
   * @param loadID the load Id or Tracking Id
   * @param loadPresence if true, only accept cmd if load is present at source
   *            station; if false, accept cmd even if there is not a load at the
   *            source station.
   * @param sourceStation where load to be moved is currently
   * @param destStation where load is to be moved to
   */
  public void moveLoadStationStation(String loadID, boolean loadPresence,
      String sourceStation, String destStation, String isBarCode,
      String isControlInfo, int inDimension )
  {
      setMCKey(loadID);
      setTransportClassification(3);  // Direct Transfer
      if(loadPresence)
      {
        setSettingClassification(2); // Load Check Setting
      }
      else
      {
        setSettingClassification(1); // Setting in Advance.
      }
      setSourceStationNumber(sourceStation);
      setDestinationStationNumber(destStation);
      setBCData(isBarCode);
      setControlInformation(isControlInfo);
      setDimensionInformation(inDimension);
      transportCommandToString();
  }

  /**
   * Store a Load from a Station to a Location the Rack.
   *
   * @param isLoadID the load Id or Tracking Id
   * @param izLoadPresence if true, only accept cmd if load is present at source
   *            station; if false, accept cmd even if there is not a load at the
   *            source station.
   * @param isSourceStation where load to be moved is currently
   * @param isLocation where load is to be moved to
   * @param
   * @param isBarCode
   * @param isControlInformation
   * @param inDimension
   */
  public void storeLoad(String isLoadID, boolean izLoadPresence,
                        String isSourceStation, String isLocation,
                        String isShelfPosition, String isBarCode,
                        String isControlInformation, int inDimension)
  {
    setMCKey(isLoadID);
    setTransportClassification(1) ;  // Transport Classification
    if(izLoadPresence )
    {
      setSettingClassification(2);
    }
    else
    {
      setSettingClassification(1);
    }
    setSourceStationNumber(isSourceStation);
    setDestinationStationNumber(AGCDeviceConstants.RACKSTATION);
    setLocationNumber(isLocation);
    setShelfPosition(isShelfPosition);
    setBCData(isBarCode);
    setControlInformation(isControlInformation);
    setDimensionInformation(inDimension);
    transportCommandToString();
  }

  protected String transportCommandToString() // ID 05
  {
    setID(5);
    setResponseMessage(false);
    transportCommandGetParsed();
    setMessageAsString(headerToString() +
             parsedMcKey(getMCKey()) +
             ASCII_DIGITS[getTransportClassification()] +
             ASCII_DIGITS[getSettingClassification()] +
             getThreeAsciiDigits(getGroupNumber()) +
             getSubString(getSourceStationNumber(), 0, 4) +
             getSubString(getDestinationStationNumber(), 0, 4) +
             getSubString(getLocationNumber(), 0, 9) + getShelfPosition() +
             getTwoAsciiDigits(getDimensionInformation()) +
             getSubString(getBCData(), 0, 30) +
             getSubString(getWorkNumber(), 0, 8) +
             getSubString(getControlInformation(), 0, 30));
    return getMessageAsString();
  }


  protected void  transportCommandToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setTransportClassification(getIntDigit(getMessageData(), 8));
    setSettingClassification(getIntDigit(getMessageData(), 9));
    setGroupNumber(getIntFromThreeAsciiDigits(getMessageData(), 10));

    setSourceStationNumber(getSubString(getMessageData(), 13, 4));
    setDestinationStationNumber(getSubString(getMessageData(), 17, 4));
    setLocationNumber(getSubString(getMessageData(), 21, 9));
    setShelfPosition(getSubString(getMessageData(), 30, 3));

    setDimensionInformation(getIntFromTwoAsciiDigits(getMessageData(), 33));

    setBCData(getSubString(getMessageData(), 35, 30));
    setWorkNumber(getSubString(getMessageData(), 65, 8));
    setControlInformation(getSubString(getMessageData(), 73, 30));
    transportCommandGetParsed();
  }

  protected void  transportCommandGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed += getTwoAsciiDigits(getID()) + " - Transport " + getMCKey()
           + " from " + getSourceStationNumber() + " to ";
      if (getDestinationStationNumber().equals(AGCDeviceConstants.RACKSTATION))
      {
        vsParsed += getMiniParseLocation(getLocationNumber(), getShelfPosition());
      }
      else
      {
        vsParsed += getDestinationStationNumber();
      }
      vsParsed += MINI_PARSE_DIVIDER;
    }
    vsParsed += getTwoAsciiDigits(getID()) + " - Transport - mcKey \""
        + getMCKey() + "\"" + " - ";
    switch (getTransportClassification())
    {
      case 1: vsParsed = vsParsed + "Store"; break;
      case 3: vsParsed = vsParsed + "Direct Transfer"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getTransportClassification();
    }

    vsParsed = vsParsed + "  Source: " + getSourceStationNumber()
        + "  Destination: " + getDestinationStationNumber()
        + "  Location: \"" + getLocationNumber() + "\""
        + "  Shelf Position: \"" + getShelfPosition() + "\""
        + "  Dimension: " + getDimensionInformation()
        + "  Control: \"" + getControlInformation() + "\""
        /*
         *  Right now, the following are not needed.  Feel free to uncomment
         *  them if this changes.
         */
//        + "  Group: " + getGroupNumber()
//        + "  Barcode: \"" + getBCData()
//        + "\"  Work#: \"" + getWorkNumber() + "\""
        ;

    vsParsed = vsParsed + "  Setting: ";
    switch (getSettingClassification())
    {
      case 1: vsParsed = vsParsed + "Setting In Advance"; break;
      case 2: vsParsed = vsParsed + "Load Check Setting"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getSettingClassification();
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public void rejectLoad(String isLoadID, String isLocation, String isStation,
      String isInfo)
  {
    int vnCommandClass = 2;
    String vsLocationNumber = AGCDeviceConstants.EMPTYLOCATION;
    if (isStation == null || isStation.trim().length() == 0)
    {
      isStation = DEFAULT_STATION;
    }

    if (isLocation != null && isLocation.trim().length() > 0)
    {
      vsLocationNumber = isLocation;
      vnCommandClass = 1;
    }

    setMCKey(isLoadID);
    setTransportClassification(1) ;  // Transport Classification (Store)
    setCommandClassification(vnCommandClass);
    setSettingClassification(2); // (Station Change Directions)
    setLocationNumber(vsLocationNumber);
    setRejectStationNumber(isStation);

    if (isInfo.trim().length() > 0)
    {
      setAGCData(isInfo);
    }
    else
    {
      setAGCData(DEFAULT_AGCDATA);
    }
    destinationStationChangeCmdToString();
  }

  public String destinationStationChangeCmdToString() // ID 08
  {
    setID(8);
    setResponseMessage(false);
    destinationStationChangeCmdGetParsed();
    setMessageAsString(headerToString() +
                       parsedMcKey(getMCKey()) +
                       ASCII_DIGITS[getCommandClassification()] +
                       getSubString(getLocationNumber(), 0, 9) + getShelfPosition() +
                       getRejectStationNumber() +
                       getSubString(getAgcData(), 0, 6));
      return getMessageAsString();
  }

  private void destinationStationChangeCmdToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setCommandClassification(getIntDigit(getMessageData(), 8));
    setLocationNumber(getSubString(getMessageData(), 9, 9));
    setRejectStationNumber(getSubString(getMessageData(), 21, 4));

    setDestinationStationNumber("    ");

    setAGCData(getSubString(getMessageData(), 25, 6));
    setControlInformation(getSubString(getMessageData(), 25, 6));

    destinationStationChangeCmdGetParsed();
  }

  private void destinationStationChangeCmdGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - DestinationStationChangeCmd - mcKey \"" + getMCKey() +
                   "\" - ";
    switch (getCommandClassification())
    {
      case 1: vsParsed = vsParsed + "Destination Station Change Directions"; break;
      case 2: vsParsed = vsParsed + "Destination Station Change is IMPOSSIBLE"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getCommandClassification();
    }
    vsParsed = vsParsed + " - Location# \"" + getLocationNumber() +
                    "\"  RejectStation#: " + getRejectStationNumber() +
                    "  agcData \"" +  getAgcData() + "\"";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/

  public void machineStatusRequest()
  {
      machineStatusInquiryToString();
  }

  private String machineStatusInquiryToString() // ID 10
  {
    setID(10);
    setResponseMessage( false);
    setMessageAsString(headerToString());
    machineStatusInquiryGetParsed();
    return getMessageAsString();
  }

  private void machineStatusInquiryToDataValues()
  {
    machineStatusInquiryGetParsed();
  }

  private void machineStatusInquiryGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID())
        + " - machineStatusInquiry" + "  hdr: " + headerGetParsed());
  }

  /*--------------------------------------------------------------------------*/
  public void binFullNewLocation(String isLoadID, String isNewLocn,
                                 String isShelfPosition, int inHeight,
                                 String isStation)
  {
    setCommandClassification(1);  // New Location (for Bin-Full)
    setMCKey(isLoadID);
    setAlternateLocation(isNewLocn);
    setShelfPosition(isShelfPosition);
    setLoadSizeInformation(inHeight);
    setAlternateStationNumber(isStation);
    alternativeLocationCmdToString();
  }

  public void binFullNoNewLocation(String LoadID)
  {
    setCommandClassification(3);  // No new Location (for Bin-Full)
    setMCKey(LoadID);
    alternativeLocationCmdToString();
  }

  public void binFullMoveStation(String isLoadID, String isStation, int inHeight)
  {
    setCommandClassification(2); // New Station (for Bin-Full)
    setMCKey(isLoadID);
    setAlternateStationNumber(isStation);
    setLoadSizeInformation(inHeight);
    alternativeLocationCmdToString();
  }

  public void heightMisMatchNewLocation(String isLoadID, String isAltLocn,
                                        String isAltShelfPos, int inHeight,
                                        String isStation)
  {
    setCommandClassification(21);  // New Location (for Bin-Full)
    setMCKey(isLoadID);
    setAlternateLocation(isAltLocn);
    setShelfPosition(isAltShelfPos);
    setLoadSizeInformation(inHeight);
    setAlternateStationNumber(isStation);
    alternativeLocationCmdToString();
  }

  public void heightMisMatchMoveStation(String isLoadID, String isStation, int inHeight)
  {
    setCommandClassification(22); // New Station (for Bin-Full)
    setMCKey(isLoadID);
    setAlternateStationNumber(isStation);
    setLoadSizeInformation(inHeight);
    alternativeLocationCmdToString();
  }

  public void binEmptyCancel(String isLoadID, String isLocation, String isShelfPos,
                             int inHeight, String isStation)
  {
    setCommandClassification(CC_BINEMPTY_CANCEL); // Data Cancel
    setMCKey(isLoadID);
    setAlternateLocation(isLocation);
    setShelfPosition(isShelfPos);
    setAlternateStationNumber(isStation);
    setLoadSizeInformation(inHeight);
    alternativeLocationCmdToString();
  }

  protected String alternativeLocationCmdToString() // ID 11
  {
    setID(11);
    setResponseMessage( false);
    alternativeLocationCmdGetParsed();
    setMessageAsString(headerToString() +
             getTwoAsciiDigits(getCommandClassification()) +
             parsedMcKey(getMCKey()) +
             getSubString(getAlternateLocation(), 0, 9) + getShelfPosition() +
             getSubString(getAlternateStationNumber(), 0, 4) +
             getTwoAsciiDigits(getLoadSizeInformation()) +
             getSubString(msBCData, 0, 30) +
             getSubString(getWorkNumber(), 0, 8) +
             getSubString(getControlInformation(), 0, 30));
      return getMessageAsString();
  }

  protected void  alternativeLocationCmdToDataValues()
  {
    setCommandClassification(getIntFromTwoAsciiDigits(getMessageData(), 0));
    setMCKey(getSubString(getMessageData(), 2, 8).trim());
    setAlternateLocation(getSubString(getMessageData(), 10, 9));
    setShelfPosition(getSubString(getMessageData(), 19, 3));
    setAlternateStationNumber(getSubString(getMessageData(), 22, 4));
    setLoadSizeInformation(getIntFromTwoAsciiDigits(getMessageData(), 26));
    setBCData(getSubString(getMessageData(), 28, 30));
    setWorkNumber(getSubString(getMessageData(), 58, 8));
    setControlInformation(getSubString(getMessageData(), 66, 30));
    alternativeLocationCmdGetParsed();
  }

  protected void alternativeLocationCmdGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - AlternateLocation "
        + getMCKey();

    switch (getCommandClassification())
    {
      case CC_BINFULL_NEWLOC:
        vsParsed += " to "
            + getMiniParseLocation(getAlternateLocation(), getShelfPosition());
        vsParsed += " (for BIN-FULL)";
        break;
      case CC_BINFULL_NEWSTN:
        vsParsed += " to " + getAlternateStationNumber() + " (for BIN-FULL)";
        break;
      case CC_BINFULL_CANCEL:
        // Should never see this in Warehouse Rx
        vsParsed += " NO alternative location (for BIN-FULL)";
        break;
      case CC_BINEMPTY_NEWLOC:
        // Should never see this in Warehouse Rx
        vsParsed += " from "
            + getMiniParseLocation(getAlternateLocation(), getShelfPosition());
        vsParsed += " (for BIN-EMPTY)";
        break;
      case CC_BINEMPTY_CANCEL:
        vsParsed += " Data Cancel (for BIN-EMPTY)";
        break;
      case CC_SIZEMIS_NEWLOC:
        vsParsed += " to "
            + getMiniParseLocation(getAlternateLocation(), getShelfPosition());
        vsParsed += " (for Load/Location Size MIS-MATCH)";
        break;
      case CC_SIZEMIS_NEWSTN:
        vsParsed += " to " + getAlternateStationNumber();
        vsParsed += " (for Load/Location Size MIS-MATCH)";
        break;
      case CC_SIZEMIS_CANCEL:
        // Should never see this in Warehouse Rx
        vsParsed += "NO Alternative Locations (for Load/Location Size MIS-MATCH";
        break;
      default:
        vsParsed += " *UNKNOWN*: " + getCommandClassification();
    }
    vsParsed = vsParsed + " \nAlternateLocation: \"" + getAlternateLocation() +
                    "\"  Shelf Position: \"" + getShelfPosition() +
                    "\"  AlternateStation: " + getAlternateStationNumber() +
                    "  Dimension: " + getLoadSizeInformation() +
                    "  Barcode: \"" +  getBCData() +
                    "\"  Work: \"" +  getWorkNumber() +
                    "\"  Control: \"" +  getControlInformation() + "\"";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/

  public void moveLoadLocationLocation(int inCommand, String isLoadID,
                                       String isSourceLocn, String isSourceShelfPos,
                                       String isDestLocn, String isDestLocnPos,
                                       int inHeight)
  {
    setRetrievalDataMCKey(inCommand, isLoadID);
    setRetrievalDataTransportationClassification(inCommand, MessageConstants.SHELF_TO_SHELF_MOVE);
    setRetrievalDataCategory(inCommand, MessageConstants.PLANNED_RETRIEVAL);
    setRetrievalDataReInputtingFlag(inCommand, MessageConstants.NO_REINPUT);
    setRetrievalDataRetrievalCommandDetail(inCommand, MessageConstants.UNIT_RETRIEVAL);
    setRetrievalDataSourceStationNumber(inCommand, AGCDeviceConstants.RACKSTATION);
    setRetrievalDataDestinationStationNumber(inCommand, AGCDeviceConstants.RACKSTATION);
    setRetrievalDataLocationNumber(inCommand, isSourceLocn);
    setRetrievalDataShelfPosition(inCommand, isSourceShelfPos);
    setRetrievalDataShelfToShelfLocationNumber(inCommand, isDestLocn);
    setRetrievalDataShelfToShelfPosition(inCommand, isDestLocnPos);
    setRetrievalDataDimension(inCommand, inHeight);
    setRetrievalDataBCData(0);
    setRetrievalDataWorkNumber(0);
    setRetrievalDataControlInformation(0);
    retrievalCmdToString();
  }

 /**
  * Build a retrieve command (location -> station)
  * @param inCommand the SRC tracking id assigned to this request.
  * @param isLoadID load to retrieve.
  * @param ipLEDF Load Data Format object carrying Retrieval command data.
  */
  public void retrieveLoadLocationStation(int inCommand, String isLoadID,
                                          LoadEventDataFormat ipLEDF)
  {
    setRetrievalDataMCKey(inCommand, isLoadID);
    setRetrievalDataTransportationClassification(inCommand, MessageConstants.RETRIEVAL_MOVE);
    setRetrievalDataCategory(inCommand, ipLEDF.getPriorityCategory());
    setRetrievalDataReInputtingFlag(inCommand, ipLEDF.getReinputFlag());
    setRetrievalDataRetrievalCommandDetail(inCommand, ipLEDF.getRetrievalCommandDetail());
    setRetrievalDataSourceStationNumber(inCommand, AGCDeviceConstants.RACKSTATION);
    setRetrievalDataDestinationStationNumber(inCommand, ipLEDF.getDestinationStation());
    setRetrievalDataLocationNumber(inCommand, ipLEDF.getSourceLocation());
    setRetrievalDataShelfPosition(inCommand, ipLEDF.getSourceLocnShelfPosition());
    setRetrievalDataDimension(inCommand, ipLEDF.getDimensionInfo());
    setRetrievalDataBCData(inCommand, ipLEDF.getFullBarCode());
    setRetrievalDataWorkNumber(0);
    setRetrievalDataControlInformation(inCommand, ipLEDF.getInformation());
    setRetrievalDataGroupNumber(inCommand, ipLEDF.getGroupNumber());
    retrievalCmdToString();
  }

  protected String retrievalCmdToString() // ID 12
  {
    setID(12);
    setResponseMessage( true);
    retrievalCmdGetParsed();
    String sResult = headerToString();

    for (int i = 0; i < 2; i++)
    {
      sResult = sResult + parsedMcKey(getRetrievalDataMCKey(i)) +
          ASCII_DIGITS[getRetrievalDataTransportationClassification(i)] +
          ASCII_DIGITS[getRetrievalDataCategory(i)] +
          ASCII_DIGITS[getRetrievalDataReInputtingFlag(i)] +
          ASCII_DIGITS[getRetrievalDataRetrievalCommandDetail(i)] +
          getThreeAsciiDigits(getRetrievalDataGroupNumber(i)) +
          getSubString(getRetrievalDataSourceStationNumber(i), 0, 4) +
          getSubString(getRetrievalDataDestinationStationNumber(i), 0, 4) +
          getSubString(getRetrievalDataLocationNumber(i), 0, 9) +
          getSubString(getRetrievalDataShelfPosition(i), 0, 3) +
          getSubString(getRetrievalDataShelfToShelfLocationNumber(i), 0, 9) +
          getSubString(getRetrievalDataShelfToShelfPosition(i), 0, 3) +
          getTwoAsciiDigits(getRetrievalDataDimension(i)) +
          getSubString(getRetrievalDataBCData(i), 0, 30) +
          getSubString(getRetrievalDataWorkNumber(i), 0, 8) +
          getSubString(getRetrievalDataControlInformation(i), 0, 30);
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  protected void retrievalCmdToDataValues()
  {
    msRetrievalDataString = getSubString(getMessageData(), 0, 2*117);
    for (int i = 0; i < 2; i++)
    {
      int idx = i * 117;
      setRetrievalDataMCKey(i, getSubString(msRetrievalDataString, idx + 0, 8).trim());
      setRetrievalDataTransportationClassification(i, getIntDigit(msRetrievalDataString, idx + 8));
      setRetrievalDataCategory(i, getIntDigit(msRetrievalDataString, idx + 9));
      setRetrievalDataReInputtingFlag(i, getIntDigit(msRetrievalDataString, idx + 10));
      setRetrievalDataRetrievalCommandDetail(i, getIntDigit(msRetrievalDataString, idx + 11));
      setRetrievalDataGroupNumber(i, getIntFromThreeAsciiDigits(msRetrievalDataString, idx + 12));
      setRetrievalDataSourceStationNumber(i, getSubString(msRetrievalDataString, idx + 15, 4));
      setRetrievalDataDestinationStationNumber(i, getSubString(msRetrievalDataString, idx + 19, 4));
      setRetrievalDataLocationNumber(i, getSubString(msRetrievalDataString, idx + 23, 9));
      setRetrievalDataShelfPosition(i, getSubString(msRetrievalDataString, idx + 32, 3));
      setRetrievalDataShelfToShelfLocationNumber(i, getSubString(msRetrievalDataString, idx + 35, 9));
      setRetrievalDataShelfToShelfPosition(i, getSubString(msRetrievalDataString, idx + 44, 3));
      setRetrievalDataDimension(i, getIntFromTwoAsciiDigits(msRetrievalDataString, idx + 47));
      setRetrievalDataBCData(i, getSubString(msRetrievalDataString, idx + 49, 30));
      setRetrievalDataWorkNumber(i, getSubString(msRetrievalDataString, idx + 79, 8));
      setRetrievalDataControlInformation(i, getSubString(msRetrievalDataString, idx + 87, 30));
    }
    retrievalCmdGetParsed();
  }

  private void retrievalCmdGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed = getTwoAsciiDigits(getID()) + " - Retrieve ";
      for (int i = 0; i < 2; i++)
      {
        String vsMCKey = getRetrievalDataMCKey(i);
        if (vsMCKey.equals(AGCDeviceConstants.EMPTYMCKEY))
        {
          continue;
        }
        if (i > 0)
        {
          vsParsed += " | ";
        }
        vsParsed += vsMCKey + " from ";
        vsParsed += getMiniParseLocation(getRetrievalDataLocationNumber(i),
            getRetrievalDataShelfPosition(i));
        vsParsed += " to ";
        if (getRetrievalDataDestinationStationNumber(i).equals(AGCDeviceConstants.RACKSTATION))
        {
          vsParsed += getMiniParseLocation(
              getRetrievalDataShelfToShelfLocationNumber(i),
              getRetrievalDataShelfToShelfPosition(i));
        }
        else
        {
          vsParsed += getRetrievalDataDestinationStationNumber(i);
        }
      }
      vsParsed += MINI_PARSE_DIVIDER;
    }
    vsParsed += getTwoAsciiDigits(getID()) + " - Retrieval -";
    for (int i = 0; i < 2; i++)
    {
      /*
       * If the MCKey is "00000000", then this part was not populated
       */
      String vsMCKey = getRetrievalDataMCKey(i);
      vsParsed += " Data-" + (i + 1);
      if (vsMCKey.equals(AGCDeviceConstants.EMPTYMCKEY))
      {
        vsParsed += " (NONE) ";
        continue;
      }
      vsParsed += " mcKey \"" + vsMCKey + "\" - ";

      switch (getRetrievalDataTransportationClassification(i))
      {
        case 2: vsParsed = vsParsed + "Retrieve-to-Station"; break;
        case 5: vsParsed = vsParsed + "Bin-to-Bin-Move"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataTransportationClassification(i);
      }

      vsParsed = vsParsed + "  Source: " + getRetrievalDataSourceStationNumber(i)
          + "  Location: \"" + getRetrievalDataLocationNumber(i) + "\""
          + "  Shelf Position: \"" + getRetrievalDataShelfPosition(i) + "\""
          + "  Destination: " + getRetrievalDataDestinationStationNumber(i)
          + "  ShelfToShelf: \"" + getRetrievalDataShelfToShelfLocationNumber(i) + "\""
          + "  Shelf Position: \"" + getRetrievalDataShelfToShelfPosition(i) + "\""
          + "  Dimension: " + getRetrievalDataDimension(i)
          + "  Control: \"" + getRetrievalDataControlInformation(i) + "\""
          /*
           *  Right now, the following are not needed.  Feel free to uncomment
           *  them if this changes.
           */
          + "  Group: " + getRetrievalDataGroupNumber(i)
//          + "  Barcode: \"" + getRetrievalDataBCData(i)
//          + "\"  Work#: \"" + getRetrievalDataWorkNumber(i) + "\""
          ;

      vsParsed = vsParsed + "  Category: ";
      switch (getRetrievalDataCategory(i))
      {
        case 1: vsParsed = vsParsed + "Urgent Retrieval"; break;
        case 2: vsParsed = vsParsed + "Planned Retrieval"; break;
        case 9: vsParsed = vsParsed + "Empty Location Check"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataCategory(i);
      }
      vsParsed = vsParsed + "  ReInputting: ";
      switch (getRetrievalDataReInputtingFlag(i))
      {
        case 0: vsParsed = vsParsed + "No Re-Inputting"; break;
        case 1: vsParsed = vsParsed + "Re-Inputting to the Same Location"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataReInputtingFlag(i);
      }
      vsParsed = vsParsed + "  RetrievalDetail: ";
      switch (getRetrievalDataRetrievalCommandDetail(i))
      {
        case 0: vsParsed = vsParsed + "Inventory Check"; break;
        case 1: vsParsed = vsParsed + "Unit Retrieval"; break;
        case 2: vsParsed = vsParsed + "Picking Retrieval"; break;
        case 3: vsParsed = vsParsed + "Adding Retrieval"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataRetrievalCommandDetail(i);
      }
      vsParsed += " | ";
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/

  public void startDevice()
  {
    setStartStopClassification(1);
    simultaneousStartStopCmdToString();
  }

  public void stopDevice()
  {
    setStartStopClassification(2);
    simultaneousStartStopCmdToString();
  }

  private String simultaneousStartStopCmdToString() // ID 16
  {
    setID(16);
    setResponseMessage(false);
    simultaneousStartStopCmdGetParsed();
    setMessageAsString(headerToString() + ASCII_DIGITS[getStartStopClassification()]);
    return getMessageAsString();
  }

  private void simultaneousStartStopCmdToDataValues()
  {
    setStartStopClassification(getIntDigit(getMessageData(), 0));
    simultaneousStartStopCmdGetParsed();
  }

  private void simultaneousStartStopCmdGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID());
    switch (getStartStopClassification())
    {
      case 1: vsParsed = vsParsed + " - Simultaneous Start"; break;
      case 2: vsParsed = vsParsed + " - Simultaneous Stop"; break;
      default: vsParsed = vsParsed + " - SimultaneousStartStopCmd *UNKNOWN*: " + getStartStopClassification();
    }
    setParsedMessageString(vsParsed);
  }
  /*--------------------------------------------------------------------------*/

  public void StartCommTest()
  {
    communicationTestRequestToString();
  }

  private String communicationTestRequestToString() //ID 19
  {
    setID(19);
    setResponseMessage( false);
    communicationTestRequestGetParsed();
    messageAsString = headerToString() +
           getSubString(getCommunicationTestTextRequest(), 0, 488);
    return getMessageAsString();
  }

  private void communicationTestRequestToDataValues()
  {
    setCommunicationTestTextRequest(getSubString(getMessageData(), 0, 488));
    communicationTestRequestGetParsed();
  }

  private void communicationTestRequestGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - CommunicationTestRequest");
  }

  /*--------------------------------------------------------------------------*/
  /**
   * This is an ID 20 - WRx-J's response to a Comm Test REQUEST from the AGC.
   */

  public void responseToCommTestDevice()
  {
    responseToCommunicationTestRequestFromAgcToString();
  }

  private String responseToCommunicationTestRequestFromAgcToString() // ID 20
  {
    setID(20);
    setResponseMessage( false);
    responseToCommunicationTestRequestFromAgcGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getCommunicationTestTextResponse(), 0, 488));
    return getMessageAsString();
  }

//  private void responseToCommunicationTestRequestFromAgcToDataValues()
//  {
//    setCommunicationTestTextResponse(getSubString(getMessageData(), 0, 488));
//    responseToCommunicationTestRequestFromAgcGetParsed();
//  }

  private void responseToCommunicationTestRequestFromAgcGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - CommunicationTestResponse");
  }

  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  public String responseToOperationModeChangeRequestToString() // ID 41
  {
    setID(41);
    setResponseMessage( false);
    responseToOperationModeChangeRequestGetParsed();
    setMessageAsString(headerToString() +
             getSubString(getOperationModeChangeStation(), 0, 4) +
             getTwoAsciiDigits(mnRequestResponse));
      return getMessageAsString();
  }

  private void responseToOperationModeChangeRequestToDataValues()
  {
    setOperationModeChangeStation(getSubString(getMessageData(), 0, 4));
    setRequestResponse(getIntFromTwoAsciiDigits(getMessageData(), 4));
    responseToOperationModeChangeRequestGetParsed();
  }

  private void responseToOperationModeChangeRequestGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - OperationModeChangeResponse - Station# " +
                   getOperationModeChangeStation() +
                   "  ";
    switch (mnRequestResponse)
    {
      case 0: vsParsed = vsParsed + "Normal"; break;
      case 1: vsParsed = vsParsed + "** ERROR (on Operating) **"; break;
      case 2: vsParsed = vsParsed + "** ERROR (Station Number) **"; break;
      default: vsParsed = vsParsed + "** *UNKNOWN*: " + mnRequestResponse + " **";
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/


  public void setStationToStoreMode(String station)
  {
    setOperationModeChangeStation(station);
    setOperationModeChangeCmd(1); // Store Mode (Normal)
    operationModeChangeCmdToString();
  }

  public void setStationToRetrieveMode(String station)
  {
    setOperationModeChangeStation(station);
    setOperationModeChangeCmd(3); // Retrieve Mode (Normal)
    operationModeChangeCmdToString();
  }

  private String operationModeChangeCmdToString() // ID 42
  {
    setID(42);
    setResponseMessage( false);
    operationModeChangeCmdGetParsed();
    setMessageAsString(headerToString() +
             getSubString(getOperationModeChangeStation(), 0, 4) +
             ASCII_DIGITS[getOperationModeChangeCmd()]);
    return getMessageAsString();
  }

  private void operationModeChangeCmdToDataValues()
  {
    setOperationModeChangeStation(getSubString(getMessageData(), 0, 4));
    setOperationModeChangeCmd(getIntDigit(getMessageData(), 4));
    operationModeChangeCmdGetParsed();
  }

  private void operationModeChangeCmdGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - Mode Change Cmd - " +
                   getOperationModeChangeStation() +
                   " - ";
    switch (getOperationModeChangeCmd())
    {
      case 1: vsParsed = vsParsed + "Store Mode (Conventional)"; break;
      case 2: vsParsed = vsParsed + "Store Mode (URGENT)"; break;
      case 3: vsParsed = vsParsed + "Retrieve Mode (Conventional)"; break;
      case 4: vsParsed = vsParsed + "Retrieve Mode (URGENT)"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getOperationModeChangeCmd();
    }
    setParsedMessageString(vsParsed);
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToRetrievalTriggerToString(String isStation, int idResult)  // ID 46
  {
    setID(46);
    setResponseMessage( false);
    setRetrievalStationNumber(isStation);
    setResponseClassification(idResult);
    responseToRetrievalTriggerGetParsed();
    setMessageAsString(headerToString() +
             getSubString(isStation, 0, 4) +
             getTwoAsciiDigits(idResult));
    return getMessageAsString();
  }

  private void responseToRetrievalTriggerToDataValues()
  {
    setRetrievalStationNumber(getSubString(getMessageData(), 0, 4));
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 4));
    responseToRetrievalTriggerGetParsed();
  }

  private void responseToRetrievalTriggerGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - RetrievalTriggerResponse - # " +
                    getRetrievalStationNumber() + "  ";
    switch (getResponseClassification())
    {
      case 0: vsParsed = vsParsed + "Normal"; break;
      case 99: vsParsed = vsParsed + "** Data ERROR **"; break;
      default: vsParsed = vsParsed + "** *UNKNOWN*: " + getResponseClassification() + " **";
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public String requestRetrievalTriggerRepetitionToString() // ID 47
  {
    setID(47);
    setResponseMessage( false);
    requestRetrievalTriggerRepetitionGetParsed();
    setMessageAsString(headerToString() +
             getSubString(getRetrievalStationNumber(), 0, 4));
    return getMessageAsString();
  }

  private void requestRetrievalTriggerRepetitionToDataValues()
  {
    setRetrievalStationNumber(getSubString(getMessageData(), 0, 4));
  }

  private void requestRetrievalTriggerRepetitionGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - RequestRetrievalTriggerRepetition - Station# " +
                    getRetrievalStationNumber();
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*  Message 50 is a customizable message                                    */
  /*--------------------------------------------------------------------------*/
  public String dataMessageToString(String msgData)          // ID 50
  {
    setID(50);
    setIDClassification(0);
                                       // Create the header part of the message.
                                       // seq. no.+ID+ID Classification+transmit time etc.
    setMessageData(msgData);
    setMessageAsString(headerToString() + msgData);
    dataMessageGetParsed();
    return getMessageAsString();
  }

  /**
   * Method to send a 54 message to the src/agc this message is used for the signal tower lights
   * @param isStation
   * @param isLamp  the light to turn on or off
   * @param inStatus on  1 or off 0
   * @return
   */
  public String doOutputInstructionToString(String isStation, String isLamp, int inStatus)
  {
    setID(54);
    setMessageAsString(headerToString() + "01" + isStation + isLamp + inStatus);
    setParsedMessageString("54 " + " - DO Output Instruction Station: " + isStation + " Lamp: " +
        isLamp + " Instruction: " + inStatus);
    return getMessageAsString();
  }

  protected void DOOutputInstructionToDataValues()
  {
    setSourceStationNumber(getSubString(getMessageData(), 2, 4));
    setLocationNumber(getSubString(getMessageData(), 6, 2));
    setGroupNumber(getIntFromOneAsciiDigit(getMessageData(), 8));
    DOOutputInstructionGetParsed();

  }

  protected void DOOutputInstructionGetParsed()
  {
    String vsLamp = "Unknown";
    if(getLocationNumber().equals("01"))
    {
      vsLamp = "Location full";
    }
    else if(getLocationNumber().equals("02"))
    {
      vsLamp = "Barcode Data Error";
    }
    else if(getLocationNumber().equals("03"))
    {
      vsLamp = "Data Error";
    }
    String vsStatus = "Unknown";
    switch(getGroupNumber())
    {
      case 0: vsStatus = "Off";break;
      case 1: vsStatus = "On"; break;
      case 2: vsStatus = "Blinking"; break;
    }
    String vsParsed = getTwoAsciiDigits(getID()) + " - DO Output Instruct - Station: " + getSourceStationNumber() +
        " - Lamp: " + getLocationNumber() + " - " + vsLamp + " - Instruction : " + getGroupNumber() + " - " + vsStatus;
    setParsedMessageString(vsParsed);
  }

  protected void dataMessageToDataValues()
  {
    dataMessageGetParsed();
  }

  protected void dataMessageGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) +
      " - Custom message - MessageData: " + getMessageData();
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Response to customizable message 50, empty method exists so we can extend it
  protected String message70ToString()
  {
    return null;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String requestAccessImpossibleLocationsToString() // ID 51
  {
    setID(51);
    setResponseMessage( false);
    requestAccessImpossibleLocationsGetParsed();
    setMessageAsString(headerToString());
    return getMessageAsString();
  }

  private void requestAccessImpossibleLocationsToDataValues()
  {
    requestAccessImpossibleLocationsGetParsed();
  }

  private void requestAccessImpossibleLocationsGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - RequestInaccessibleLocationsReport");
  }
  /*--------------------------------------------------------------------------*/
  public String requestToStartSystemRecoveryToString() // ID 58
  {
    setID(58);
    setResponseMessage( false);
    requestToStartSystemRecoveryGetParsed();
    setMessageAsString(headerToString());
    return getMessageAsString();
  }

  private void requestToStartSystemRecoveryToDataValues()
  {
    setDestinationStationNumber(getSubString(getMessageData(), 0, 4));
    requestToStartSystemRecoveryGetParsed();
  }

  private void requestToStartSystemRecoveryGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - RequestToStartSystemRecovery");
  }

  /*--------------------------------------------------------------------------*/
  public String requestToTerminateSystemRecoveryToString() // ID 59
  {
    setID(59);
    setResponseMessage( false);
    requestToTerminateSystemRecoveryGetParsed();
    setMessageAsString( headerToString());
    return getMessageAsString();
  }

  private void requestToTerminateSystemRecoveryToDataValues()
  {
    requestToTerminateSystemRecoveryGetParsed();
  }

  private void requestToTerminateSystemRecoveryGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - RequestToTerminateSystemRecovery");
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Messages sent to RTS From AGC
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToOperationStartRequestToString() // ID 21
  {
    setID(21);
    setResponseMessage( true);
    responseToOperationStartRequestGetParsed();
    setMessageAsString(headerToString() +
           getTwoAsciiDigits(getResponseClassification()) +
           getSubString(msErrorDetails, 0, 2) +
           ASCII_DIGITS[getSystemRecoveryReport()]);
    return getMessageAsString();
  }

  private void responseToOperationStartRequestToDataValues()
  {
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 0));
    setErrorDetails(getSubString(getMessageData(), 2, 2));
    setSystemRecoveryReport(getIntDigit(getMessageData(), 4));
    responseToOperationStartRequestGetParsed();
  }

  private void responseToOperationStartRequestGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID())
        + " - OperationStartResponse - ";
    String vsResponse = null;
    switch (getResponseClassification())
    {
      case 0: vsResponse = "Normal"; break;
      case 3: vsResponse = "** AGC Status Error - ErrorDetails: " + msErrorDetails + " **"; break;
      case 99: vsResponse = "** Data Error **"; break;
      default: vsResponse = "** *UNKNOWN*: " + getResponseClassification() + " **";
    }
    vsParsed += vsResponse + " - ";
    switch (getSystemRecoveryReport())
    {
      case 0: vsResponse = "No Reports"; break;
      case 1: vsResponse = "System Recovery Conducted"; break;
      default: vsResponse = "** *UNKNOWN*: " + getSystemRecoveryReport() + " **";
    }
    vsParsed += vsResponse;
    msResponseDetails = vsResponse + " - ResponseToOperationStartRequest(21) Received";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public String dateTimeRequestToString() // ID 22
  {
    setID(22);
    setResponseMessage( true);
    dateTimeRequestGetParsed();
    setMessageAsString(headerToString() +
           getTwoAsciiDigits(getRequestClassification()) +
           ASCII_DIGITS[getSystemRecoveryReport()]);
    return getMessageAsString();
  }

  private void dateTimeRequestToDataValues()
  {
    setRequestClassification(getIntDigit(getMessageData(), 0));
    if (mnRequestClassification == 1)
    {
      setSystemRecoveryReport(getIntDigit(getMessageData(), 1));
    }
    dateTimeRequestGetParsed();
  }

  private void dateTimeRequestGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - DateTimeRequest - ";
    switch (getRequestClassification())
    {
      case 0: vsParsed = vsParsed + "Time Modified"; break;
      case 1:
      {
        vsParsed = vsParsed + "Operation Start - systemRecoveryReport: ";
        switch (getSystemRecoveryReport())
        {
          case 0: vsParsed = vsParsed + "No Reports"; break;
          case 1: vsParsed = vsParsed + "System Recovery Conducted"; break;
          default: vsParsed = vsParsed + " *UNKNOWN*: " + getSystemRecoveryReport();
        }
      }
      break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getRequestClassification();
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToOperationTerminationRequestToString() // ID 23
  {
    setID(23);
    setResponseMessage( true);
    responseToOperationTerminationRequestGetParsed();
    setMessageAsString(headerToString() +
           getTwoAsciiDigits(getResponseClassification()) +
           getSubString(getResponseDetailsModelCode(), 0, 2) +
           getSubString(getResponseDetailsMachineNumber(), 0, 4));
    return getMessageAsString();
  }

   private void responseToOperationTerminationRequestToDataValues()
  {
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 0));
    setResponseDetailsMachineId(getSubString(getMessageData(), 2, 6));
    setResponseDetailsModelCode(getSubString(getMessageData(), 2, 2));
    setResponseDetailsMachineNumber(getSubString(getMessageData(), 4, 4));
    responseToOperationTerminationRequestGetParsed();
  }

  private void responseToOperationTerminationRequestGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - OperationTerminationResponse - ";
    switch (getResponseClassification())
    {
      case 0: vsParsed = vsParsed + "Normal Termination"; break;
      case 1:
      {
        vsParsed = vsParsed + "Termination IMPOSSIBLE" +
                " - ModelCode: " + getResponseDetailsModelCode() +
                " - Machine#: " + getResponseDetailsMachineNumber();
      }
      break;
      case 99: vsParsed = vsParsed + "Data ERROR"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getResponseClassification();
    }
    setParsedMessageString(vsParsed);
  }

  public String getStatusForResponseToOperationTerminationRequest()
  {
    /*
     *  Did this ever work?  Machine Status Events should be
     *      ID Type Number Status Description Error
     *  Original code had
     *         Type Number        Description Error
     *
     *  I've tried to correct it, but I haven't tested it.
     */
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.MACHINE_STATUS);

    vpSEDF.addMachineStatus(getResponseDetailsModelCode(),
        machineTypes[getIntFromTwoAsciiDigits(getResponseDetailsModelCode(), 0)],
        getResponseDetailsMachineNumber(), -1, "Cannot-Turn-Offline", "0");

    return vpSEDF.createStringToSend();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToTransportDataCancelToString() //  ID 24
  {
    setID(24);
    setResponseMessage( true);
    responseToTransportDataCancelGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getTwoAsciiDigits(getCancellationResults()));
    return getMessageAsString();
  }

  private void   responseToTransportDataCancelToDataValues()
  {
    setMCKey( getSubString(getMessageData(), 0, 8).trim());
    setCancellationResults( getIntFromTwoAsciiDigits(getMessageData(), 8));
    responseToTransportDataCancelGetParsed();
  }

  private void responseToTransportDataCancelGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - TransportDataCancelResponse - mcKey \"" + getMCKey() + "\"" +
                   " - ";
    switch (getCancellationResults())
    {
      case 0: vsParsed = vsParsed + "Normal Completion"; break;
      case 1: vsParsed = vsParsed + "Cancellation IMPOSSIBLE (Data Already On Way)"; break;
      case 2: vsParsed = vsParsed + "NO Applicable Transport Data"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getCancellationResults();
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToTransportCommandToString() // ID 25
  {
    setID(25);
    setResponseMessage(true);
    responseToTransportCommandGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getTwoAsciiDigits(getResponseClassification()));
    return getMessageAsString();
  }

  protected void   responseToTransportCommandToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 8));
    responseToTransportCommandGetParsed();
  }

  protected void responseToTransportCommandGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed = getTwoAsciiDigits(getID()) + " - TransportResponse "
          + getMCKey() + " ";
      switch (getResponseClassification())
      {
        case 0: vsParsed = vsParsed + "OK"; break;
        case 1: vsParsed = vsParsed + "** ERROR **"; break;
        case 3: vsParsed = vsParsed + "** Duplicate **"; break;
        case 4: vsParsed = vsParsed + "** OUT-OF-ORDER **"; break;
        case 5: vsParsed = vsParsed + "** DISCONNECTED **"; break;
        case 6: vsParsed = vsParsed + "** OFF-LINE **"; break;
        case 7: vsParsed = vsParsed + "** ERROR **"; break;
        case 11: vsParsed = vsParsed + "** ERROR **"; break;
        case 99: vsParsed = vsParsed + "** ERROR **"; break;
        default: vsParsed = vsParsed + "** UNKNOWN **";
      }

      vsParsed += MINI_PARSE_DIVIDER;
    }
    vsParsed += getTwoAsciiDigits(getID()) + " - TransportResponse - mcKey \""
        + getMCKey() + "\"" + " - ";
    switch (getResponseClassification())
    {
      case 0: vsParsed = vsParsed + "Normal"; break;
      case 1: vsParsed = vsParsed + "** Load ERROR (NO Load at the Source Designated) **"; break;
      case 3: vsParsed = vsParsed + "** Duplicate Command (The SAME mcKey is in the AGC) **"; break;
      case 4: vsParsed = vsParsed + "** (Source Designated) is OUT-OF-ORDER **"; break;
      case 5: vsParsed = vsParsed + "** (Source Designated) is DISCONNECTED **"; break;
      case 6: vsParsed = vsParsed + "** AGC is OFF-LINE **"; break;
      case 7: vsParsed = vsParsed + "** Condition ERROR (Transport Commanded is IMPOSSIBLE to Implement) **"; break;
      case 11: vsParsed = vsParsed + "** Buffer FULL **"; break;
      case 99: vsParsed = vsParsed + "** Data ERROR **"; break;
      default: vsParsed = vsParsed + "**  *UNKNOWN*: " + getResponseClassification() + " ** **";
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String arrivalReportToString() // ID 26
  {
    setID(26);
    setResponseMessage( true);
    arrivalReportGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getSubString(getArrivalStationNumber(), 0, 4) +
           getTwoAsciiDigits(getDimensionInformation()) +
           ASCII_DIGITS[getLoadInformation()] +
           getSubString(getBCData(), 0, 30) +
           getSubString(getControlInformation(), 0, 30));
    return getMessageAsString();
  }


  public void  arrivalReportToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setArrivalStationNumber(getSubString(getMessageData(), 8, 4));
    setDimensionInformation(getIntFromTwoAsciiDigits(getMessageData(), 12));
    setLoadInformation(getIntDigit(getMessageData(), 14));
    setBCData(getSubString(getMessageData(), 15, 30));
    setControlInformation(getSubString(getMessageData(), 45, 30));
    arrivalReportGetParsed();
  }

  protected void arrivalReportGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed = getTwoAsciiDigits(getID())
          + " - Arrival " + getMCKey() + " at " + getArrivalStationNumber()
          + "  Dimen:" + getDimensionInformation() + "  BC:\"" + getBCData() + "\"";
      vsParsed += MINI_PARSE_DIVIDER;
    }
    vsParsed += getTwoAsciiDigits(getID())
      + " - ArrivalReport - mcKey \"" + getMCKey() + "\""
      + " - Station#: " + getArrivalStationNumber()
      + "  Dimension: " + getDimensionInformation()
      + "  ";
    switch (mnLoadInformation)
    {
      case 0: vsParsed = vsParsed + "Load: None (For Picking Cancel)"; break;
      case 1: vsParsed = vsParsed + "Load: Present (For Conventional Storing/Re-Storing)"; break;
      default: vsParsed = vsParsed + " Load Information: *UNKNOWN*: " + getLoadInformation();
    }
    vsParsed += "  Barcode: \"" + getBCData()
             + "\"  Control: \"" + getControlInformation() + "\"";

    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String requestDestinationStationChangeToString() // ID 27
  {
    setID(27);
    setResponseMessage( true);
    requestDestinationStationChangeGetParsed();
    setMessageAsString(headerToString() +
                       parsedMcKey(getMCKey()) +
                       getSubString(getLocationNumber(), 0, 9) + getShelfPosition() +
                       getSubString(getDestinationStationNumber(), 0, 4) +
                       getSubString(getAgcData(), 0, 6));
    return getMessageAsString();
  }

  private void  requestDestinationStationChangeToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setLocationNumber(getSubString(getMessageData(), 8, 9));
    setDestinationStationNumber(getSubString(getMessageData(), 20, 4));
    setAGCData(getSubString(getMessageData(), 24, 6));
    requestDestinationStationChangeGetParsed();
  }

  private void requestDestinationStationChangeGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - RequestDestStationChange - mcKey \"" + getMCKey() + "\"" +
                   " - Location# \"" + getLocationNumber() +
                   "\"  DestStation#: " + getDestinationStationNumber() +
                    "  agcData \"" +  getAgcData() + "\"";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToDestinationStationChangeCmdToString() // ID 28
  {
    setID(28);
    setResponseMessage( true);
    responseToDestinationStationChangeCmdGetParsed();
    setMessageAsString(headerToString() +
                       parsedMcKey(getMCKey()) +
                       ASCII_DIGITS[getCommandClassification()] +
                       getSubString(getLocationNumber(), 0, 9) + getShelfPosition() +
                       getSubString(getDestinationStationNumber(), 0, 4) +
                       getTwoAsciiDigits(getResponseClassification()));
    return getMessageAsString();
  }

  private void responseToDestinationStationChangeCmdToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setCommandClassification(getIntDigit(getMessageData(), 8));
    setLocationNumber(getSubString(getMessageData(), 9, 12));
    setDestinationStationNumber(getSubString(getMessageData(), 21, 4));
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 25));
    responseToDestinationStationChangeCmdGetParsed();
  }

  private void responseToDestinationStationChangeCmdGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - DestStationChangeResponse - mcKey \"" + getMCKey() +
                   "\" - commandClassification: " + getCommandClassification() +
                    " - Location# \"" + getLocationNumber() +
                    "\"  DestStn#: " + getDestinationStationNumber() +
                    "  ";
    String response = null;
    switch (getResponseClassification())
    {
      case 0: response = "Normal"; break;
      case 1: response = "** NO Applicable Transport Data **"; break;
      case 2: response = "** NO Applicable  Location Number **"; break;
      case 3: response = "** NO Applicable Station Number **"; break;
      case 4: response = "** NO Transport Route to the Applicable Location Number **"; break;
      case 5: response = "** NO Transport Route to the Applicable Station Number **"; break;
      case 6: response = "** Access to the Applicable Location is NOT Allowed **"; break;
      case 99: response = "** Data ERROR **"; break;
      default: response = "** *UNKNOWN*: " + getResponseClassification() + " **";
    }
    vsParsed = vsParsed + response;
    msResponseDetails = "LoadId \"" + getMCKey() + "\" " + response + " - Lctn: " + getLocationNumber() +
                    "  DestStn: " + getDestinationStationNumber() +
                    " - ResponseToDestStnChangeCmd(28) Received";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String machineStatusReportToString()  // ID 30
  {
    setID(30);
    setResponseMessage(true);
    machineStatusReportGetParsed();
    String sResult = headerToString() +
                     ASCII_DIGITS[getContinuationClassification()] +
                     getTwoAsciiDigits(getNumberOfReports());
    for (int i = 0; i < getNumberOfReports(); i++)
    {
      sResult = sResult + getTwoAsciiDigits(getMachineStatusMachineTypeCode(i)) +
                          getSubString(getMachineStatusMachineNumber(i), 0, 4) +
                          ASCII_DIGITS[getMachineStatusStatus(i)] +
                          getSubString(getMachineStatusErrorCode(i), 0, 7);
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  private void  machineStatusReportToDataValues()
  {
    try
    {
      setContinuationClassification(getIntDigit(getMessageData(), 0));
      setNumberOfReports(getIntFromTwoAsciiDigits(getMessageData(), 1));
      if (getNumberOfReports() > 0)
      {
        setMachineStatusString(getSubString(getMessageData(), 3, getNumberOfReports()*14));
        for (int i = 0; i < getNumberOfReports(); i++)
        {
          int idx = i * 14;
          setMachineStatusMachineId(i, getSubString(msMachineStatusString, idx + 0, 6));
          setMachineStatusMachineTypeCode(i, getIntFromTwoAsciiDigits(msMachineStatusString, idx + 0));
          setMachineStatusMachineNumber(i, getSubString(msMachineStatusString, idx + 2, 4));
          setMachineStatusStatus(i, getIntDigit(msMachineStatusString, idx + 6));
          setMachineStatusErrorCode(i, getSubString(msMachineStatusString, idx + 7, 7));
        }
      }
    }
    catch (Exception e)
    {
      if (getValidMessage())
      {
        setValidMessage(false);
        setInvalidMessageDescription(("#####  Exception -- numberOfReports: " +
                                     getNumberOfReports() + "\"  #####"));
      }
    }
    machineStatusReportGetParsed();
  }

  private void machineStatusReportGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - MachineStatusReport - " +
                   getNumberOfReports() + " reports - ";
    switch (getContinuationClassification())
    {
      case 1: vsParsed = vsParsed + "Multiple Reports"; break;
      case 2: vsParsed = vsParsed + "Single Report"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getContinuationClassification();
    }
    if (getNumberOfReports() > 0)
    {
      vsParsed = vsParsed + " Machine Reports (TypeCode-Nmbr-Stts-ErrCd):";
      for (int i = 0; i < getNumberOfReports(); i++)
      {
        vsParsed = vsParsed + "   ";
        vsParsed = vsParsed + getMachineStatusMachineTypeCode(i) + "-";
        vsParsed = vsParsed + getMachineStatusMachineNumber(i) + "-";
        switch (getMachineStatusStatus(i))
        {
          case 0: vsParsed = vsParsed + "On-"; break;
          case 1: vsParsed = vsParsed + "Off-"; break;
          case 2: vsParsed = vsParsed + "Error-"; break;
          case 3: vsParsed = vsParsed + "Disconnected-"; break;
          default: vsParsed = vsParsed + "*UNKNOWN: " + getMachineStatusStatus(i) + "*-";
        }
        vsParsed = vsParsed + getMachineStatusErrorCode(i);
      }
    }
    setParsedMessageString(vsParsed);
  }

  public String getMachineStatusMessage()
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.MACHINE_STATUS);

    if (getNumberOfReports() > 0)
    {
      for (int i = 0; i < getNumberOfReports(); i++)
      {
        int vnMCStatus = getMachineStatusStatus(i);
        String vsErrorCode = getMachineStatusErrorCode(i);
        if ((vnMCStatus != 0) && (!vsErrorCode.equals(AGCMessage.NO_ERROR)))
        {
          vnMCStatus = 2; // error code says we are in error.
        }
        int vnDBStatus = -1;
        String vsDesc = null;
        switch (vnMCStatus)
        {
          case 0: vnDBStatus = DBConstants.ONLINE;       break;
          case 1: vnDBStatus = DBConstants.OFFLINE;      break;
          case 2: vnDBStatus = DBConstants.ERROR;        break;
          case 3: vnDBStatus = DBConstants.DISCONNECTED; break;
        }
        try
        {
          vsDesc = DBTrans.getStringValue(DeviceEnum.PHYSICALSTATUS.getName(), vnDBStatus);
        }
        catch (NoSuchFieldException nsfe)
        {
          vsDesc = "*UNKNOWN-"+vnMCStatus+"*";
        }
        if (vsErrorCode.equals(AGCMessage.NO_ERROR))
        {
          vsErrorCode = StatusEventDataFormat.NONE;
        }

        vpSEDF.addMachineStatus(getMachineStatusMachineId(i),
            machineTypes[getMachineStatusMachineTypeCode(i)],
            getMachineStatusMachineNumber(i), vnDBStatus, vsDesc, vsErrorCode);
      }
    }
    return vpSEDF.createStringToSend();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToAlternativeLocationCmdToString() // ID 31
  {
    setID(31);
    setResponseMessage( true);
    responseToAlternativeLocationCmdGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getTwoAsciiDigits(getResponseClassification()));
    return getMessageAsString();
  }

  private void   responseToAlternativeLocationCmdToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 8));
    responseToAlternativeLocationCmdGetParsed();
  }

  private void responseToAlternativeLocationCmdGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - AlternateLocationResponse - mcKey \"" + getMCKey() +
                   "\" - ";
    String response = null;
    switch (getResponseClassification())
    {
      case 0: response = "Normal"; break;
      case 1: response = "** NO Applicable Transport Data **"; break;
      case 2: response = "** Command Classification ERROR **"; break;
      case 3: response = "** Status ERROR **"; break;
      case 4: response = "** NO Route to the Applicable Location **"; break;
      case 5: response = "** NO Route to the Applicable Station **"; break;
      case 6: response = "** Access IMPOSSIBLE Location **"; break;
      case 99: response = "** Data ERROR **"; break;
      default: response = "**  *UNKNOWN*: " + getResponseClassification() + " **";
    }
    vsParsed = vsParsed + response;
    msResponseDetails = "LoadId \"" + getMCKey() + "\" \"" + response +
                    "\" - ResponseToAlternateLocationCmd(31) Received";
    setParsedMessageString(vsParsed);
  }
  /*--------------------------------------------------------------------------*/
  public String responseToRetrievalCmdToString() // ID 32
  {
    setID(32);
    setResponseMessage( true);
    responseToRetrievalCmdGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getTwoAsciiDigits(getResponseClassification()) +
           parsedMcKey(getMCKey2()) +
           getTwoAsciiDigits(getResponseClassification2()));
    return getMessageAsString();
  }

  protected void   responseToRetrievalCmdToDataValues()
  {
    String mck = getSubString(getMessageData(), 0, 8).trim();
    setMCKey(mck);
    setRetrievalDataMCKey(0, mck);
    int dcc = getIntFromTwoAsciiDigits(getMessageData(), 8);
    setResponseClassification(dcc);
    setRetrievalDataCompletionClassification(0, dcc);
    //
    mck =  getSubString(getMessageData(), 10, 8).trim();
    setMCKey2(mck);
    setRetrievalDataMCKey(1, mck);
    dcc = getIntFromTwoAsciiDigits(getMessageData(), 18);
    setResponseClassification2(dcc);
    setRetrievalDataCompletionClassification(1, dcc);
    //
    responseToRetrievalCmdGetParsed();
  }

  private void responseToRetrievalCmdGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed += getTwoAsciiDigits(getID()) + " - RetrieveResponse ";
      if (!getMCKey().equals(AGCDeviceConstants.EMPTYMCKEY))
      {
        vsParsed += getMCKey() + " ";

        vsParsed += getRetrievalCommandResponseString(0);
      }

      if (!getMCKey2().equals(AGCDeviceConstants.EMPTYMCKEY))
      {
        vsParsed += " | " + getMCKey() + ": ";

        vsParsed += getRetrievalCommandResponseString(1);
      }

      vsParsed += MINI_PARSE_DIVIDER;
    }
    vsParsed += getTwoAsciiDigits(getID())
        + " - RetrievalResponse - mcKey-1 \"" + getMCKey() + "\" - ";
    vsParsed += getRetrievalCommandResponseString(0);
    vsParsed += " - mcKey-2 \"" + getMCKey2() + "\" - ";
    vsParsed += getRetrievalCommandResponseString(1);
    msResponseDetails = "LoadId \"" + getMCKey() + "\"";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public String operationCompletionReportToString() // ID 33
  {
    setID(33);
    setResponseMessage(true);
    operationCompletionReportGetParsed();
    String sResult = headerToString() +
                     getSubString(getRetrievalDataString(), 0, 0);
    for (int i = 0; i < 2; i++)
    {
      sResult = sResult + parsedMcKey(getRetrievalDataMCKey(i)) +
                          ASCII_DIGITS[getRetrievalDataTransportationClassification(i)] +
                          ASCII_DIGITS[getRetrievalDataCategory(i)] +
                          ASCII_DIGITS[getRetrievalDataCompletionClassification(i)] +
                          getSubString(getRetrievalDataSourceStationNumber(i), 0, 4) +
                          getSubString(getRetrievalDataDestinationStationNumber(i), 0, 4) +
                          getSubString(getRetrievalDataLocationNumber(i), 0, 9) +
                          getSubString(getRetrievalDataShelfPosition(i), 0, 3) +
                          getSubString(getRetrievalDataShelfToShelfLocationNumber(i), 0, 9) +
                          getSubString(getRetrievalDataShelfToShelfPosition(i), 0, 3) +
                          getTwoAsciiDigits(getRetrievalDataDimension(i)) +
                          getSubString(getRetrievalDataBCData(i), 0, 30) +
                          getSubString(getRetrievalDataWorkNumber(i), 0, 8) +
                          getSubString(getRetrievalDataControlInformation(i), 0, 30);
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  protected void operationCompletionReportToDataValues()
  {
    setRetrievalDataString(getSubString(getMessageData(), 0, 2*113));
    for (int i = 0; i < 2; i++)
    {
      int idx = i * 113;
      setRetrievalDataMCKey(i, getSubString(msRetrievalDataString, idx + 0, 8).trim());
      setRetrievalDataTransportationClassification(i, getIntDigit(msRetrievalDataString, idx + 8));
      setRetrievalDataCategory(i, getIntDigit(msRetrievalDataString, idx + 9));
      setRetrievalDataCompletionClassification(i, getIntDigit(msRetrievalDataString, idx + 10));
      setRetrievalDataSourceStationNumber(i, getSubString(msRetrievalDataString, idx + 11, 4));
      setRetrievalDataDestinationStationNumber(i, getSubString(msRetrievalDataString, idx + 15, 4));
      setRetrievalDataLocationNumber(i, getSubString(msRetrievalDataString, idx + 19, 9));
      setRetrievalDataShelfPosition(i, getSubString(msRetrievalDataString, idx + 28, 3));
      setRetrievalDataShelfToShelfLocationNumber(i, getSubString(msRetrievalDataString, idx + 31, 9));
      setRetrievalDataShelfToShelfPosition(i, getSubString(msRetrievalDataString, idx + 40, 3));
      if (getRetrievalDataTransportationClassification(i) == 5 &&
          (!getRetrievalDataShelfToShelfLocationNumber(i).equals(AGCDeviceConstants.EMPTYLOCATION)))
      {
        /*
         * For some unfathomable reason, the locations are REVERSED for the
         * shelf to shelf work complete.  It's just the way it is.
         */
        setRetrievalDataShelfToShelfLocationNumber(i, getSubString(msRetrievalDataString, idx + 19, 9));
        setRetrievalDataLocationNumber(i, getSubString(msRetrievalDataString, idx + 31, 9));
      }
      setRetrievalDataDimension(i, getIntFromTwoAsciiDigits(msRetrievalDataString, idx + 43));
      setRetrievalDataBCData(i, getSubString(msRetrievalDataString, idx + 45, 30));
      setRetrievalDataWorkNumber(i, getSubString(msRetrievalDataString, idx + 75, 8));
      setRetrievalDataControlInformation(i, getSubString(msRetrievalDataString, idx + 83, 30));
    }
    operationCompletionReportGetParsed();
  }

  protected void operationCompletionReportGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed += getTwoAsciiDigits(getID()) + " - OperationComplete ";

      for (int i = 0; i < 2; i++)
      {
        String vsMCKey = getRetrievalDataMCKey(i);
        if (vsMCKey.equals(AGCDeviceConstants.EMPTYMCKEY))
        {
          continue;
        }
        if (i > 0)
        {
          vsParsed += " | ";
        }
        vsParsed += vsMCKey + " ";

        switch (getRetrievalDataCompletionClassification(i))
        {
          case 0:
            vsParsed += ((getRetrievalDataTransportationClassification(i) == 4)
                ? "Retrieve " : "") + "OK";
            break;
          case 1: vsParsed += "** BIN-FULL **"; break;
          case 2: vsParsed += "** BIN-EMPTY **"; break;
          case 3: vsParsed += "** SIZE MIS-MATCH **"; break;
          case 4: vsParsed += "Normal"; break;
          case 5: vsParsed += "Normal"; break;
          case 7: vsParsed += "** Empty Location **"; break;
          case 8: vsParsed += "** Stored Location **"; break;
          case 9: vsParsed += "** CANCEL **"; break;
          default: vsParsed += "** UNKNOWN **";
        }

        vsParsed += " from ";
        if (getRetrievalDataSourceStationNumber(i).equals(AGCDeviceConstants.RACKSTATION))
        {
          vsParsed += getMiniParseLocation(getRetrievalDataLocationNumber(i),
              getRetrievalDataShelfPosition(i));
        }
        else
        {
          vsParsed += getRetrievalDataSourceStationNumber(i);
        }
        vsParsed += " to ";
        if (getRetrievalDataDestinationStationNumber(i).equals(AGCDeviceConstants.RACKSTATION))
        {
          if (getRetrievalDataShelfToShelfLocationNumber(i).contains(AGCDeviceConstants.EMPTYARCLOCATION))
          {
            vsParsed += getMiniParseLocation(getRetrievalDataLocationNumber(i),
                getRetrievalDataShelfPosition(i));
          }
          else
          {
            vsParsed += getMiniParseLocation(
                getRetrievalDataShelfToShelfLocationNumber(i),
                getRetrievalDataShelfToShelfPosition(i));
          }
        }
        else
        {
          vsParsed += getRetrievalDataDestinationStationNumber(i);
        }
      }

      vsParsed += MINI_PARSE_DIVIDER;
    }

    vsParsed += getTwoAsciiDigits(getID()) + " - OperationCompletionReport - ";
    for (int i = 0; i < 2; i++)
    {
      /*
       * If the MCKey is "00000000", then this part was not populated
       */
      String vsMCKey = getRetrievalDataMCKey(i);
      vsParsed += " Data-" + (i + 1);
      if (vsMCKey.equals(AGCDeviceConstants.EMPTYMCKEY))
      {
        vsParsed += " (NONE) ";
        continue;
      }
      vsParsed += " mcKey \"" + vsMCKey + "\" - ";


      vsParsed = vsParsed + "  Completion: ";
      switch (getRetrievalDataCompletionClassification(i))
      {
        case 0: vsParsed = vsParsed + "Normal"; break;
        case 1: vsParsed = vsParsed + "** BIN-FULL **"; break;
        case 2: vsParsed = vsParsed + "** BIN-EMPTY **"; break;
        case 3: vsParsed = vsParsed + "** Load/Location Size MIS-MATCH **"; break;
        case 4: vsParsed = vsParsed + "Pickup Normal"; break;
        case 5: vsParsed = vsParsed + "Deposit Normal"; break;
        case 7: vsParsed = vsParsed + "** Empty Location Completion **"; break;
        case 8: vsParsed = vsParsed + "** Stored Location Completion **"; break;
        case 9: vsParsed = vsParsed + "** CANCEL **"; break;
        default: vsParsed = vsParsed + "** *UNKNOWN*: " + getRetrievalDataCompletionClassification(i) + " **";
      }
      vsParsed +=  "  Source: " + getRetrievalDataSourceStationNumber(i)
          + "  Location: \"" + getRetrievalDataLocationNumber(i) + "\""
          + "  Shelf Position: \"" + getRetrievalDataShelfPosition(i) + "\""
          + "  Destination: " + getRetrievalDataDestinationStationNumber(i)
          + "  ShelfToShelf: \"" + getRetrievalDataShelfToShelfLocationNumber(i) + "\""
          + "  Shelf Position: \"" + getRetrievalDataShelfToShelfPosition(i) + "\"";

      vsParsed = vsParsed + " - Transport: ";
      switch (getRetrievalDataTransportationClassification(i))
      {
        case 1: vsParsed = vsParsed + "Storage"; break;
        case 2: vsParsed = vsParsed + "Retrieve"; break;
        case 4: vsParsed = vsParsed + "Shelf-To-Shelf Move (Retrieval)"; break;
        case 5: vsParsed = vsParsed + "Shelf-To-Shelf Move (Storage)"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataTransportationClassification(i);
      }

      vsParsed = vsParsed + "  Category: ";
      switch (getRetrievalDataCategory(i))
      {
        case 1: vsParsed = vsParsed + "Urgent Retrieval"; break;
        case 2: vsParsed = vsParsed + "Planned Retrieval"; break;
        case 9: vsParsed = vsParsed + "Empty Location Check"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataCategory(i);
      }

      vsParsed += "  Dimension: " + getRetrievalDataDimension(i)
          + "  Barcode: " + getRetrievalDataBCData(i)
          + "  Control: \"" + getRetrievalDataControlInformation(i) + "\""
//          + "  Work#: \"" + getRetrievalDataWorkNumber(i) + "\""
          ;
      vsParsed += " | ";
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public String transportDataDeletionReportToString() // ID 35
  {
    setID(35);
    setResponseMessage(true);
    transportDataDeletionReportGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getMessageData(), 0, 1) +
           parsedMcKey(getMCKey()) +
           getSubString(getDestinationStationNumber(), 0, 4) +
           getSubString(getControlInformation(), 0, 30));
    return getMessageAsString();
  }

  private void  transportDataDeletionReportToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 1, 8).trim());
    setDestinationStationNumber(getSubString(getMessageData(), 9, 4));
    setControlInformation(getSubString(getMessageData(), 13, 30));
    transportDataDeletionReportGetParsed();
  }

  protected void transportDataDeletionReportGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - TransportDataDeletionReport - mcKey \"" + getMCKey() +
                   "\"  Station# " + getDestinationStationNumber() +
                   "  Cause:" + getSubString(getMessageData(), 0, 1) +
                   "  Control: \"" + getControlInformation() + "\"";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public String terminalOperationCompletionReportToString() // ID 45
  {
    setID(45);
    setResponseMessage(false);
    terminalOperationCompletionReportGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getTerminalOperationCompleteStationNumber() +
           ASCII_DIGITS[mnTerminalOperationCompleteTxfrClassification]);
    return getMessageAsString();
  }

  private void terminalOperationCompletionReportToDataValues()
  {
    String mck = getSubString(getMessageData(), 0, 8).trim();
    setMCKey(mck);
    setTerminalOperationCompleteStationNumber(getSubString(getMessageData(), 8, 4).trim());
    setTerminalOperationCompleteTxfrClassification(getIntDigit(getMessageData(), 12));
    terminalOperationCompletionReportGetParsed();
  }

  /*--------------------------------------------------------------------------*/
  public void terminalOperationComplete(String ipLoadID, String ipSourceStation)
  {
      setMCKey(ipLoadID);
      setTerminalOperationCompleteStationNumber(ipSourceStation);
      setTerminalOperationCompleteTxfrClassification(1);
      terminalOperationCompletionReportToString();
  }

  protected void terminalOperationCompletionReportGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - Terminal Op Complete - "
        + getMCKey() + " at " + msTerminalOperationCompleteStationNumber
        + " Classification: ";
    switch (mnTerminalOperationCompleteTxfrClassification)
    {
      case 0:
        vsParsed = vsParsed + "Transfer";
        break;
      case 1:
        vsParsed = vsParsed + "Store Mode (Conventional)";
        break;
      default:
        vsParsed = vsParsed + " *UNKNOWN*: "
            + mnTerminalOperationCompleteTxfrClassification;
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String simultaneousStartImproperReportToString(int inabiltyToStart) // ID 36
  {
    setID(36);
    setInabiltyToStartReason(inabiltyToStart);
    setResponseMessage(true);
    simultaneousStartImproperReportGetParsed();
    setMessageAsString(headerToString() +
           getTwoAsciiDigits(getInabiltyToStartReason()));
    return getMessageAsString();
  }

  private void simultaneousStartImproperReportToDataValues()
  {
    setInabiltyToStartReason( getIntFromTwoAsciiDigits(getMessageData(), 0));
    simultaneousStartImproperReportGetParsed();
  }

  private void simultaneousStartImproperReportGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - SimultaneousStartImproperReport - inabiltyToStartReason: ";
    switch (getInabiltyToStartReason())
    {
      case 1: vsParsed = vsParsed + "The System is NOT in the Condition (Online or System Recovery Mode) to Start Operation"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getInabiltyToStartReason();
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * This is an ID 39 - AGC's response to a Comm Test REQUEST from WRx-J.
   */
  public String responseToCommunicationTestRequestToString() // ID 39
  {
    setID(39);
    setResponseMessage( true);
    responseToCommunicationTestRequestGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getCommunicationTestTextResponse(), 0, 488));
    return getMessageAsString();
  }

  private void responseToCommunicationTestRequestToDataValues()
  {
    setCommunicationTestTextResponse(getSubString(getMessageData(), 0, 488));
    responseToCommunicationTestRequestGetParsed();
  }

  private void responseToCommunicationTestRequestGetParsed()
  {
    String s = "OK";
    if (! getCommunicationTestResult())
    {
      s = "*FAIL*";
    }
    String vsParsed = getTwoAsciiDigits(getID()) + " - CommunicationTestResponse " + s +
        "  textLength: " + communicationTestTextResponse.length();
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String communicationTestRequestFromAgcToString() // ID 40
  {
    setID(40);
    setResponseMessage( true);
    communicationTestRequestFromAgcGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getCommunicationTestTextRequest(), 0, 488));
    return getMessageAsString();
  }

  private void communicationTestRequestFromAgcToDataValues()
  {
    setCommunicationTestTextRequest(getSubString(getMessageData(), 0, 488));
    communicationTestRequestFromAgcGetParsed();
  }

  private void communicationTestRequestFromAgcGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - CommunicationTestRequest");
  }

  /*--------------------------------------------------------------------------*/
  public String operationModeChangeRequestFromAgcToString() // ID 61
  {
    setID(61);
    setResponseMessage( true);
    operationModeChangeRequestFromAgcGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getOperationModeChangeStation(), 0, 4) +
           ASCII_DIGITS[getRequestClassification()]);
    return getMessageAsString();
  }

  protected void operationModeChangeRequestFromAgcToDataValues()
  {
    setOperationModeChangeStation(getSubString(getMessageData(), 0, 4));
    setRequestClassification(getIntDigit(getMessageData(), 4));
    operationModeChangeRequestFromAgcGetParsed();
  }

  private void operationModeChangeRequestFromAgcGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - OpModeChngRqst - Stn# " +
                   getOperationModeChangeStation() +
                   "  ";
    switch (getRequestClassification())
    {
      case 1: vsParsed = vsParsed + "Store"; break;
      case 2: vsParsed = vsParsed + "Retrieve"; break;
      case 3: vsParsed = vsParsed + "Store Request Cancel"; break;
      case 4: vsParsed = vsParsed + "Retrieve Request Cancel"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getRequestClassification();
    }
    setParsedMessageString(vsParsed);
  }
  /*--------------------------------------------------------------------------*/
  public String responseToOperationModeChangeCmdToString() // ID 62
  {
    setID(62);
    setResponseMessage( true);
    responseToOperationModeChangeCmdGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getOperationModeChangeStation(), 0, 4) +
           getTwoAsciiDigits(getResponseClassification()));
    return getMessageAsString();
  }

  private void responseToOperationModeChangeCmdToDataValues()
  {
    setOperationModeChangeStation(getSubString(getMessageData(), 0, 4));
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 4));
    responseToOperationModeChangeCmdGetParsed();
  }

  private void responseToOperationModeChangeCmdGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - Mode Change Response - " +
                   getOperationModeChangeStation() +
                   " - ";
    String response = null;
    switch (getResponseClassification())
    {
      case 0: response = "Normal"; break;
      case 1: response = "** ERROR (Mode being Changed) **"; break;
      case 2: response = "** ERROR (Station Number) **"; break;
      case 3: response = "** ERROR (Mode Commanded) **"; break;
      case 4: response = "** ERROR (Transport Data/Yes) **"; break;
      default: response = "** *UNKNOWN*: " + getRequestClassification() + " **";
    }
    vsParsed = vsParsed + response;
    msResponseDetails = "Station: " +  getOperationModeChangeStation() + " \" Response: " + response +
                    "\" - ResponseToOpModeChangeCmd(62) Received";
    setParsedMessageString(vsParsed);
  }

 /*--------------------------------------------------------------------------*/
  public String operationModeChangeCompletionReportToString() // ID 63
  {
    setID(63);
    setResponseMessage( true);
    operationModeChangeCompletionReportGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getOperationModeChangeStation(), 0, 4) +
           ASCII_DIGITS[getCompletionMode()]);
    return getMessageAsString();
  }

  private void operationModeChangeCompletionReportToDataValues()
  {
    setOperationModeChangeStation(getSubString(getMessageData(), 0, 4));
    setCompletionMode(getIntDigit(getMessageData(), 4));
    operationModeChangeCompletionReportGetParsed();
  }

  private void operationModeChangeCompletionReportGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - Mode Change Complete - " +
                   getOperationModeChangeStation() +
                   " - ";
    switch (getCompletionMode())
    {
      case 1: vsParsed = vsParsed + "Store"; break;
      case 2: vsParsed = vsParsed + "Retrieve"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getCompletionMode();
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  public String pickupCompletionReportToString() // ID 64
  {
    setID(64);
    setResponseMessage( true);
    pickupCompletionReportGetParsed();
    String sResult = headerToString() +
           getSubString(getSourceStationNumber(), 0, 4) +
           getTwoAsciiDigits(getNumberOfReports()) +
           parsedMcKey(getMCKey());
    if (getNumberOfReports() == 2)
    {
      sResult = sResult + parsedMcKey(getMCKey2());
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  private void pickupCompletionReportToDataValues()
  {
    setSourceStationNumber(getSubString(getMessageData(), 0, 4));
    setNumberOfReports (getIntFromTwoAsciiDigits(getMessageData(), 4));
    setMCKey( getSubString(getMessageData(), 6, 8).trim());
    if (getNumberOfReports() == 2)
    {
      setMCKey2( getSubString(getMessageData(), 14, 8).trim());
    }
    pickupCompletionReportGetParsed();
  }

  private void pickupCompletionReportGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed += getTwoAsciiDigits(getID()) + " - PickupComplete - ";
      vsParsed += getMCKey();
      if (getNumberOfReports() == 2)
      {
        vsParsed += " and " + getMCKey2();
      }
      vsParsed += " at " + getSourceStationNumber();

      vsParsed += MINI_PARSE_DIVIDER;
    }

    vsParsed += getTwoAsciiDigits(getID())
        + " - PickupCompletionReport - Station:" + getSourceStationNumber()
        + "  Reports: " + getNumberOfReports()
        + "  mcKey-1 \"" + getMCKey() + "\"";
    if (getNumberOfReports() == 2)
    {
      vsParsed = vsParsed + "  mcKey-2 \"" + getMCKey2() + "\"";
    }
    setParsedMessageString(vsParsed);
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String retrievalTriggerToString() // ID 66
  {
    setID(66);
    setResponseMessage( true);
    retrievalTriggerGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getRetrievalStationNumber(), 0, 4));
    return getMessageAsString();
  }

  private void retrievalTriggerToDataValues()
  {
    setRetrievalStationNumber(getSubString(getMessageData(), 0, 4));
    retrievalTriggerGetParsed();
  }

  private void retrievalTriggerGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - RetrievalTrigger - Station# " + getRetrievalStationNumber());
  }


  /*--------------------------------------------------------------------------*/
  public String triggerOfOperationIndicationToString() // ID 68
  {
    setID(68);
    setResponseMessage( true);
    triggerOfOperationIndicationGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getSubString(getDestinationStationNumber(), 0, 4) +
           getSubString(getControlInformation(), 0, 30));
    return getMessageAsString();
  }


  private void  triggerOfOperationIndicationToDataValues()
  {
    setMCKey( getSubString(getMessageData(), 0, 8).trim());
    setDestinationStationNumber(getSubString(getMessageData(), 8, 4));
    setControlInformation(getSubString(getMessageData(), 12, 30));
    triggerOfOperationIndicationGetParsed();
  }

  private void triggerOfOperationIndicationGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - Work Trigger "
        + getMCKey() + " at " + getDestinationStationNumber() + "  Control: \""
        + getControlInformation() + "\"";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String agcStationLPStatusReportToString() // ID 70
  {
    setID(70);
    setResponseMessage( true);
    agcStationLPStatusReportGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getSourceStationNumber(), 0, 4) +
           ASCII_DIGITS[getLoadInformation()]);
    return getMessageAsString();
  }

  public void   agcStationLPStatusReportToDataValues()
  {
//    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 0));
    setSourceStationNumber(getSubString(getMessageData(), 0, 4));
    setLoadInformation(getIntDigit(getMessageData(), 4));
    agcStationLPStatusReportGetParsed();
  }

  protected void agcStationLPStatusReportGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - AgcStationLPStatusReport - Station#: " + getSourceStationNumber();
    vsParsed = vsParsed + "  loadInformation: ";
    switch (mnLoadInformation)
    {
      case 0: vsParsed = vsParsed + "Load: None"; break;
      case 1: vsParsed = vsParsed + "Load: Present"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getLoadInformation();
    }
    setParsedMessageString(vsParsed);

  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String accessImpossibleLocationsReportToString() //ID 71
  {
    setID(71);
    setResponseMessage(true);
    accessImpossibleLocationsReportGetParsed();
    String sResult = headerToString() +
                     ASCII_DIGITS[getContinuationClassification()] +
                     getTwoAsciiDigits(getNumberOfReports());
    for (int i = 0; i < getNumberOfReports(); i++)
    {
      sResult = sResult + ASCII_DIGITS[getImpossibleLocationStatus(i)] +
                          getSubString(getImpossibleLocationStorageClassification(i), 0, 1) +
                          getSubString(getImpossibleLocationBankNumber(i), 1, 2) +
                          getSubString(getImpossibleLocationStartBayNumber(i), 0, 3) +
                          getSubString(getImpossibleLocationStartLevelNumber(i), 0, 3) +
                          getSubString(getImpossibleLocationEndBayNumber(i), 0, 3) +
                          getSubString(getImpossibleLocationEndLevelNumber(i), 0, 3);
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  private void  accessImpossibleLocationsReportToDataValues()
  {
    setContinuationClassification(getIntDigit(getMessageData(), 0));
    setNumberOfReports(getIntFromTwoAsciiDigits(getMessageData(), 1));
    if (getNumberOfReports() > 0)
    {
      setImpossibleLocationString(getSubString(getMessageData(), 3, getNumberOfReports()*16));
      for (int i = 0; i < getNumberOfReports(); i++)
      {
        int idx = i * 16;
        setImpossibleLocationStatus(i, getIntDigit(msImpossibleLocationString, idx + 0));
        setImpossibleLocationStorageClassification(i, getSubString(msImpossibleLocationString, idx + 1, 1));
        setImpossibleLocationBankNumber(i, getSubString(msImpossibleLocationString, idx + 2, 2));
        setImpossibleLocationStartBayNumber(i, getSubString(msImpossibleLocationString, idx + 4, 3));
        setImpossibleLocationStartLevelNumber(i, getSubString(msImpossibleLocationString, idx + 7, 3));
        setImpossibleLocationEndBayNumber(i, getSubString(msImpossibleLocationString, idx + 10, 3));
        setImpossibleLocationEndLevelNumber(i, getSubString(msImpossibleLocationString, idx + 13, 3));
      }
    }
    accessImpossibleLocationsReportGetParsed();
  }

  private void accessImpossibleLocationsReportGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - InaccessibleLocationsReport - " +
                   getNumberOfReports() + " reports - ";
    switch (getContinuationClassification())
    {
      case 1: vsParsed = vsParsed + "Multiple Reports"; break;
      case 2: vsParsed = vsParsed + "Single Report"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getContinuationClassification();
    }
    if (getNumberOfReports() > 0)
    {
      vsParsed = vsParsed + " (Status;Classification;Range): \n";
      for (int i = 0; i < getNumberOfReports(); i++)
      {
        vsParsed = vsParsed + getImpossibleLocationStatus(i) + "; ";
        vsParsed = vsParsed + getImpossibleLocationStorageClassification(i) + "; ";
        vsParsed = vsParsed + getImpossibleLocationBankNumber(i) + "-";
        vsParsed = vsParsed + getImpossibleLocationStartBayNumber(i) + "-";
        vsParsed = vsParsed + getImpossibleLocationStartLevelNumber(i) + " to ";
        vsParsed = vsParsed + getImpossibleLocationBankNumber(i) + "-";
        vsParsed = vsParsed + getImpossibleLocationEndBayNumber(i) + "-";
        vsParsed = vsParsed + getImpossibleLocationEndLevelNumber(i) + "  \n";
      }
    }
    setParsedMessageString(vsParsed);
  }
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToRequestToStartSystemRecoveryToString() // ID 78
  {
    setID(78);
    setResponseMessage( true);
    responseToRequestToStartSystemRecoveryGetParsed();
    setMessageAsString(headerToString() +
           getTwoAsciiDigits(getResponseClassification()) +
           getSubString(getErrorDetails(), 0, 2));
    return getMessageAsString();
  }

  private void responseToRequestToStartSystemRecoveryToDataValues()
  {
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 0));
    setErrorDetails(getSubString(getMessageData(), 2, 2));
    responseToRequestToStartSystemRecoveryGetParsed();
  }

  private void responseToRequestToStartSystemRecoveryGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - StartSystemRecoveryResponse - ";
    switch (getResponseClassification())
    {
      case 0: vsParsed = vsParsed + "Normal"; break;
      case 3: vsParsed = vsParsed + "** AGC Status Error - ErrorDetails: " + getErrorDetails() + " **"; break;
      case 99: vsParsed = vsParsed + "** Data Error **"; break;
      default: vsParsed = vsParsed + "** *UNKNOWN*: " + getResponseClassification() + " **";
    }
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToRequestToTerminateSystemRecoveryToString() // ID 79
  {
    setID(79);
    setResponseMessage( true);
    responseToRequestToTerminateSystemRecoveryGetParsed();
    setMessageAsString( headerToString());
    return getMessageAsString();
  }

  private void responseToRequestToTerminateSystemRecoveryToDataValues()
  {
    responseToRequestToTerminateSystemRecoveryGetParsed();
  }

  private void responseToRequestToTerminateSystemRecoveryGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - TerminateSystemRecoveryResponse");
  }


  protected String parsedMcKey(String s)
  {
    if (s.length() == 8)
    {
      return getSubString(s, 0, 8);
    }
    else
    {
      s = s + DEFAULT_MCKEY;
      s = s.substring(0, 8);
      return s;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected String headerToString()
  {
    setValidMessage(true);
    String sid = getTwoAsciiDigits(getID());
    String sidClassification = getTwoAsciiDigits(mnIdClassification);
    if (getResponseMessage())
    {
      setAGCTransmissionTime(msgDateTime.getCurrentDateTimeAsString());
    }
    else
    {
      setTransmissionTime(msgDateTime.getCurrentDateTimeAsString());
      setAGCTransmissionTime(DEFAULT_TIME);
    }
    return sid +
           sidClassification +
           getTransmissionTime() +
           getAGCTransmissionTime();
  }

  private String headerGetParsed()
  {
    return getTwoAsciiDigits(getID()) + " - Header - idClassification:" +
          getTwoAsciiDigits(getIDClassification()) +
          "  mcTxTime: " +
          getSubString(getTransmissionTime(), 0, 2) + ":" +
          getSubString(getTransmissionTime(), 2, 2) + ":" +
          getSubString(getTransmissionTime(), 4, 2) +
          "  agcTxTime: " +
          getSubString(getTransmissionTime(), 0, 2) + ":" +
          getSubString(getTransmissionTime(), 2, 2) + ":" +
          getSubString(getTransmissionTime(), 4, 2);
  }

  public void clearAgcMessageData()
  {
    int i;
    setValidMessage(false);
    setInvalidMessageDescription("");
    setMessageAsString(null);
    setParsedMessageString("");
    setMessageData("");
    setID(0);
    setIDClassification(0);
    setTransmissionTime(DEFAULT_TIME);
    setAGCTransmissionTime(DEFAULT_TIME);
    setResponseMessage(false);
    setDateTimeString();
    setRequestClassification(0);
    setSourceStationNumber(DEFAULT_STATION);
    setDestinationStationNumber(DEFAULT_STATION);
    setLocationNumber(DEFAULT_LOCATION);
    setShelfPosition(LoadData.DEFAULT_POSITION_VALUE);
    setTransportClassification(1);
    setSettingClassification(1);
    setGroupNumber(0);
    setDimensionInformation(0);
    setBCData(DEFAULT_BARCODE);
    setWorkNumber(DEFAULT_WORKNO);
    setControlInformation(DEFAULT_CONTROL_INFO);
    setMCKey(DEFAULT_MCKEY);
    setCommandClassification(1);
    setRejectStationNumber(DEFAULT_STATION);
    setAGCData(DEFAULT_AGCDATA);
    setResponseClassification(0);
    setErrorDetails("0");
    setSystemRecoveryReport(0);
    setResponseDetailsMachineId(DEFAULT_MACHINEID);
    setResponseDetailsModelCode(DEFAULT_MODELCODE);
    setResponseDetailsMachineNumber(DEFAULT_MACHINENO);
    setCancellationResults(0);
    setArrivalStationNumber(DEFAULT_STATION);
    setLoadInformation(0);
    setContinuationClassification(0);
    setNumberOfReports(0);
    setMachineStatusString("");
    setRetrievalDataString("");
    for(i=0;i<2;i++)
    {
      setRetrievalDataMCKey(i, AGCDeviceConstants.EMPTYMCKEY);
      setRetrievalDataTransportationClassification(i,0);
      setRetrievalDataCategory(i,0);
      setRetrievalDataCompletionClassification(i,0);
      setRetrievalDataReInputtingFlag(i,0);
      setRetrievalDataRetrievalCommandDetail(i,0);
      setRetrievalDataGroupNumber(i,0);
      setRetrievalDataSourceStationNumber(i,DEFAULT_STATION);
      setRetrievalDataDestinationStationNumber(i,DEFAULT_STATION);
      setRetrievalDataLocationNumber(i,DEFAULT_LOCATION);
      setRetrievalDataShelfToShelfLocationNumber(i,DEFAULT_LOCATION);
      setRetrievalDataDimension(i,0);
      setRetrievalDataBCData(i);
      setRetrievalDataWorkNumber(i);
      setRetrievalDataControlInformation(i);
    }
    setStartStopClassification(0);
    setRetrievalStationNumber(null);
    setInabiltyToStartReason(0);
    setCommunicationTestTextResponse("this is a test ");
    setCommunicationTestTextRequest(null);
    setOperationModeChangeStation(DEFAULT_STATION);
    setRequestResponse(0);
    setOperationModeChangeCmd(0);
    setAlternateLocation(DEFAULT_LOCATION + LoadData.DEFAULT_POSITION_VALUE);
    setLoadSizeInformation(0);
    setAlternateStationNumber(DEFAULT_STATION);
    setResponseClassification2(0);
    setMCKey2(DEFAULT_MCKEY);
    setCompletionMode(0);
  }


/*---------------------------------------------------------------------------
   Methods to return values.
  ---------------------------------------------------------------------------*/
  private String getTransmissionTime()     { return(transmissionTime);  }
  private String getAGCTransmissionTime()    { return(msAgcTransmissionTime); }
  private String getDateTimeString()         { return(dataDateTimeString);  }
  public String getMCKey()                  { return(mcKey);               }
  public String getMCKey2()                 { return(msMCKey2);              }
  public int getResponseClassification()    { return(mnResponseClassification);}
  private int getResponseClassification2()    { return(mnResponseClassification2);}
  public String getResponseDetailsModelCode() { return(msResponseDetailsModelCode);               }
  public int getRequestResponse()           { return(mnRequestResponse);      }
  public String getErrorDetails()            { return(msErrorDetails);        }
  public int getRequestClassification()     { return(mnRequestClassification); }
  public String getLocationNumber()         { return(msLocationNumber);}
  public String getShelfPosition()          { return(msShelfPosition);}
  public int getTransportClassification()   { return(mnTransportClassification); }
  protected int getSettingClassification()     { return(mnSettingClassification); }
  protected int getGroupNumber()               { return(mnGroupNumber);            }
  public String getSourceStationNumber()    { return(msSourceStationNumber.substring(0,4));    }
  public String getDestinationStationNumber() { return(msDestinationStationNumber.substring(0,4)); }
  public int getDimensionInformation()   { return(mnDimensionInformation);   }
  public String getBCData()                 { return(msBCData);                 }
  public String getWorkNumber()             { return(msWorkNumber);             }
  public String getControlInformation()     { return(msControlInformation);     }
  public String getMessageData()            { return(messageData);            }
  public int getCommandClassification()     { return(mnCommandClassification);  }
  public String getRejectStationNumber()    { return(msRejectStation);    }
  public String getAgcData()                { return(msAgcData);                }
  public String getRetrievalStationNumber()    { return(msRetrievalStationNumber); }
  public String getAlternateLocation()      { return(msAlternateLocation);      }
  public int getLoadSizeInformation()       { return(mnLoadSizeInformation);    }
  public String getAlternateStationNumber()    { return(msAlternateStationNumber); }
  public int getStartStopClassification()    { return(mnStartStopClassification); }
  public String getOperationModeChangeStation() { return(msOperationModeChangeStation);      }
  public int getOperationModeChangeCmd()    { return(mnOperationModeChangeCmd); }
  public int getSystemRecoveryReport()    { return(mnSystemRecoveryReport); }
  public int getCancellationResults()    { return(mnCancellationResults); }
  public String getResponseDetailsMachineId() { return(msResponseDetailsMachineId);      }
  public String getResponseDetailsMachineNumber() { return(msResponseDetailsMachineNumber);      }
  public String getArrivalStationNumber()    { return(msArrivalStationNumber); }
  public String getTerminalOperationCompleteStationNumber()    { return(msTerminalOperationCompleteStationNumber); }
  public int getTerminalOperationCompleteTxfrClassification() {return mnTerminalOperationCompleteTxfrClassification;}
  public int getLoadInformation()         {   return(mnLoadInformation);      }
  public int getContinuationClassification() { return(mnContinuationClassification);      }
  public int getNumberOfReports()             { return(mnNumberOfReports);      }
  private String getRetrievalDataString() { return(msRetrievalDataString);      }
  public int getInabiltyToStartReason()             { return(mnInabiltyToStartReason);      }
  public int getCompletionMode()              { return(mnCompletionMode);      }
  private boolean getResponseMessage()              { return(responseMessage);      }
  private int getIDClassification()              { return(mnIdClassification);      }
  public String getResponseDetails() { return(msResponseDetails); }

  /*
   * get from retrieval data class
   */
  public String getRetrievalDataMCKey(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsMcKey;
  }

  public int getRetrievalDataTransportationClassification(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].transportationClassification;
  }

  public int getRetrievalDataCategory(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].category;
  }

  public int getRetrievalDataCompletionClassification(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].completionClassification;
  }

  public String getRetrievalResponse(int inCommandNumber)
  {
    String result = null;
    switch (getRetrievalDataCompletionClassification(inCommandNumber))
    {
      case 0: result = "Normal"; break;
      case 1: result = "** BIN-FULL **"; break;
      case 2: result = "** BIN-EMPTY **"; break;
      case 3: result = "** Load/Location Size MIS-MATCH **"; break;
      case 4: result = "Normal Pickup Completion"; break;
      case 5: result = "Normal Deposit Completion"; break;
      case 7: result = "Empty Location Completion"; break;
      case 8: result = "Stored Location Completion"; break;
      case 9: result = "Cancel"; break;
      default: result = "** *UNKNOWN*: "
        + getRetrievalDataCompletionClassification(inCommandNumber) + " **";
    }
    return result;
  }

  public String getRetrievalCommandResponseString(int inCommandNumber)
  {
    String result;
    int vnResponse = 0;

    if (inCommandNumber == 0) vnResponse = getResponseClassification();
    else                      vnResponse = getResponseClassification2();

    switch (vnResponse)
    {
      case 0: result = "OK"; break;
      case 3: result = "** Duplicate **"; break;
      case 6: result = "** AGC OFF-LINE **"; break;
      case 11: result = "** Buffer FULL **"; break;
      case 99: result = "** Data ERROR **"; break;
      default: result = "** UNKNOWN" + vnResponse + " **";
    }
    return result;
  }

  public String getStoreResponse()
  {
    String result = null;
    switch (getResponseClassification())
    {
      case 0: result = "Normal"; break;
      case 1: result = "** Load ERROR (NO Load at the Source Designated) **"; break;
      case 3: result = "** Duplicate Command (The SAME mcKey is in the AGC) **"; break;
      case 4: result = "** (Source Designated) is OUT-OF-ORDER **"; break;
      case 5: result = "** (Source Designated) is DISCONNECTED **"; break;
      case 6: result = "** AGC is OFF-LINE **"; break;
      case 7: result = "** Condition ERROR (Transport Commanded is IMPOSSIBLE to Implement) **"; break;
      case 11: result = "** Buffer FULL **"; break;
      case 99: result = "** Data ERROR **"; break;
      default: result = "** *UNKNOWN*: " + getResponseClassification() + " **";
    }
    return result;
  }

  protected int getRetrievalDataReInputtingFlag(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].reInputtingFlag;
  }

  protected int getRetrievalDataRetrievalCommandDetail(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].retrievalCommandDetail;
  }

  protected int getRetrievalDataGroupNumber(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmnGroupNumber;
  }

  public String getRetrievalDataSourceStationNumber(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsSourceStationNumber;
  }

  public String getRetrievalDataDestinationStationNumber(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsDestinationStationNumber;
  }

  public String getRetrievalDataLocationNumber(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsLocationNumber;
  }

  public String getRetrievalDataShelfPosition(int inCommandNumber)
  {
    return(mapRetrievalDatas[inCommandNumber].mmsShelfPosition);
  }

  public String getRetrievalDataShelfToShelfLocationNumber(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsShelfToShelfLocationNumber;
  }

  public String getRetrievalDataShelfToShelfPosition(int inCommandNumber)
  {
    return(mapRetrievalDatas[inCommandNumber].mmsShelfToShelfPosition);
  }

  public int getRetrievalDataDimension(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].dimension;
  }

  public String getRetrievalDataBCData(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsBcData;
  }

  public String getRetrievalDataWorkNumber(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsWorkNumber;
  }

  public String getRetrievalDataControlInformation(int inCommandNumber)
  {
    return mapRetrievalDatas[inCommandNumber].mmsControlInformation;
  }

/*
* gets for machins status
*/

  public String getMachineStatusMachineId(int inCommandNumber)
  {
    return mapMachineStatuses[inCommandNumber].machineId;
  }

  public int getMachineStatusMachineTypeCode(int inCommandNumber)
  {
    return mapMachineStatuses[inCommandNumber].machineTypeCode;
  }

  public String getMachineStatusMachineNumber(int inCommandNumber)
  {
    return mapMachineStatuses[inCommandNumber].machineNumber;
  }

  public int getMachineStatusStatus(int inCommandNumber)
  {
    return mapMachineStatuses[inCommandNumber].status;
  }

  public String getMachineStatusErrorCode(int inCommandNumber)
  {
    return mapMachineStatuses[inCommandNumber].errorCode;
  }

/* get Impossible Locations */


  public int getImpossibleLocationStatus(int inCommandNumber)
  {
    return mapImpossibleLocations[inCommandNumber].status;
  }

  public String getImpossibleLocationStorageClassification(int inCommandNumber)
  {
    return mapImpossibleLocations[inCommandNumber].storageClassification;
  }

  public String getImpossibleLocationBankNumber(int inCommandNumber)
  {
    return mapImpossibleLocations[inCommandNumber].bankNumber;
  }

  public String getImpossibleLocationStartBayNumber(int inCommandNumber)
  {
    return mapImpossibleLocations[inCommandNumber].startBayNumber;
  }

  public String getImpossibleLocationStartLevelNumber(int inCommandNumber)
  {
    return mapImpossibleLocations[inCommandNumber].startLevelNumber;
  }

  public String getImpossibleLocationEndBayNumber(int inCommandNumber)
  {
    return mapImpossibleLocations[inCommandNumber].endBayNumber;
  }

  public String getImpossibleLocationEndLevelNumber(int inCommandNumber)
  {
    return mapImpossibleLocations[inCommandNumber].endLevelNumber;
  }

  /*
   * set methods
   */
 protected void setValidMessage(boolean izValidMessage)
  {
    validMessage = izValidMessage;
  }

  protected void setInvalidMessageDescription(String isInvalidMessageDescription)
  {
    invalidMessageDescription = isInvalidMessageDescription;
  }

  protected void setTransmissionTime(String isTransmissionTime)
  {
    transmissionTime = isTransmissionTime;
  }

  public void setMessageData(String isMessageData)
  {
    messageData = isMessageData;
  }

  protected void setIDClassification (int inIdClassification)
  {
    mnIdClassification = inIdClassification;
  }

  protected void setResponseMessage(boolean izResponseMessage)
  {
    responseMessage = izResponseMessage;
  }

  public void setAGCData(String isAgcData)
  {
    msAgcData = isAgcData;
  }

  private void setRejectStationNumber(String isRejectStationNumber)
  {
    msRejectStation = isRejectStationNumber;
  }

  public void setCompletionMode(int inCompletionMode)
  {
    mnCompletionMode = inCompletionMode;
  }

  public void setRequestResponse(int inRequestResponse)
  {
    mnRequestResponse = inRequestResponse;
  }

  protected void setMessageAsString(String isMessageAsString)
  {
    messageAsString = isMessageAsString;
  }

  protected void setAlternateLocation(String isAlternateLocation)
  {
    msAlternateLocation = isAlternateLocation;
  }

  private void setImpossibleLocationString(String isImpossibleLocationString)
  {
    msImpossibleLocationString = isImpossibleLocationString;
  }

  protected void setLoadSizeInformation(int inLoadSizeInformation)
  {
    mnLoadSizeInformation = inLoadSizeInformation;
  }

  protected void setAlternateStationNumber(String isAlternateStationNumber)
  {
    msAlternateStationNumber = isAlternateStationNumber;
  }

  public void setResponseClassification2(int inResponseClassification2)
  {
    mnResponseClassification2 = inResponseClassification2;
  }

  public void setRetrievalStationNumber(String isRetrievalStationNumber)
  {
    msRetrievalStationNumber = isRetrievalStationNumber;
  }

  public void setInabiltyToStartReason(int inInabiltyToStartReason)
  {
    mnInabiltyToStartReason = inInabiltyToStartReason;
  }

  public void setSystemRecoveryReport(int inSystemRecoveryReport)
  {
    mnSystemRecoveryReport = inSystemRecoveryReport;
  }

  public void setResponseDetailsMachineId(String isResponseDetailsMachineId)
  {
    msResponseDetailsMachineId = isResponseDetailsMachineId;
  }

  public void setResponseDetailsModelCode(String isResponseDetailsModelCode)
  {
    msResponseDetailsModelCode = isResponseDetailsModelCode;
  }

  public void setResponseDetailsMachineNumber(String isResponseDetailsMachineNumber)
  {
    msResponseDetailsMachineNumber = isResponseDetailsMachineNumber;
  }

  private void setRetrievalDataString(String isRetrievalDataString)
  {
    msRetrievalDataString = isRetrievalDataString;
  }

  private void setCancellationResults(int inCancellationResults)
  {
    mnCancellationResults = inCancellationResults;
  }

  public void setArrivalStationNumber(String isArrivalStationNumber)
  {
    msArrivalStationNumber = isArrivalStationNumber;
  }

  public void setLoadInformation(int inLoadInformation)
  {
    mnLoadInformation = inLoadInformation;
  }

  public void setContinuationClassification (int inContinuationClassification)
  {
    mnContinuationClassification = inContinuationClassification;
  }

  public void setNumberOfReports(int inNumberOfReports)
  {
    mnNumberOfReports = inNumberOfReports;
  }

  private void setMachineStatusString(String isMachineStatusString)
  {
    msMachineStatusString = isMachineStatusString;
  }

  public void setResponseClassification(int inResponseClassification)
  {
    mnResponseClassification = inResponseClassification;
  }

  public void setErrorDetails(String isErrorDetails)
  {
    msErrorDetails = isErrorDetails;
  }

  private void setAGCTransmissionTime(String isAgcTransmissionTime)
  {
    msAgcTransmissionTime = isAgcTransmissionTime;
  }

  public void setStartStopClassification(int inStartStopClassification)
  {
    mnStartStopClassification = inStartStopClassification;
  }

  public void setRequestClassification(int inRequestClassification)
  {
    mnRequestClassification = inRequestClassification;
  }

  public void setMCKey(String isMCKey)
  {
    mcKey = isMCKey;
  }

  public void setMCKey2(String isMCKey2)
  {
    msMCKey2 = isMCKey2;
  }

  public void setOperationModeChangeCmd(int inOperationModeChangeCmd)
  {
    mnOperationModeChangeCmd = inOperationModeChangeCmd;
  }

  public void setOperationModeChangeStation(String isOperationModeChangeStation)
  {
    msOperationModeChangeStation = isOperationModeChangeStation;
  }

  public void setCommandClassification(int inCommandClassification)
  {
    mnCommandClassification = inCommandClassification;
  }

  public void setDestinationStationNumber(String isDestinationStationNumber)
  {
    msDestinationStationNumber = isDestinationStationNumber;
  }

  public void setLocationNumber(String isLocationNumber)
  {
    msLocationNumber = isLocationNumber;
  }

  public void setShelfPosition(String isShelfPosition)
  {
    if (isShelfPosition == null || isShelfPosition.isEmpty())
      msShelfPosition = LoadData.DEFAULT_POSITION_VALUE;
    else
      msShelfPosition = isShelfPosition;
  }

  public void setSourceStationNumber(String isSourceStationNumber)
  {
    msSourceStationNumber = isSourceStationNumber;
  }

  public void setSettingClassification(int inSettingClassification)
  {
    mnSettingClassification = inSettingClassification;
  }

  public void setTransportClassification(int inTransportClassification)
  {
    mnTransportClassification = inTransportClassification;
  }

  public void setGroupNumber(int inGroupNumber)
  {
    mnGroupNumber = inGroupNumber;
  }

  public void setDimensionInformation(int inDimensionInformation)
  {
    mnDimensionInformation = inDimensionInformation;
  }

  public void setBCData(String isBarCode)
  {
    int vnBCLength = msBCData.length();
    if (isBarCode.length() < vnBCLength)
    {
      isBarCode = isBarCode + DEFAULT_BARCODE;
      isBarCode = isBarCode.substring(0, vnBCLength);
    }
    msBCData = isBarCode;
  }

  /**
   *
   * @param isWorkNumber
   */
  public void setWorkNumber(String isWorkNumber)
  {
    msWorkNumber = isWorkNumber;
  }

  /**
   * Set the control information
   * @param isControlInformation
   */
  public void setControlInformation(String isControlInformation)
  {
    msControlInformation = SKDCUtility.spaceFillTrailing(isControlInformation,
        AGCDeviceConstants.LNCONTROLINFORMATION);
  }

  /**
   * Sets for the Retrieval data class can send 2 commands
   *
   * @param num = which command (0 or 1)
   * @param isMCKey
   */
  public void setRetrievalDataMCKey(int inCommandNumber, String isMCKey)
  {
    mapRetrievalDatas[inCommandNumber].mmsMcKey = isMCKey;
  }

  public void setRetrievalDataTransportationClassification(int inCommandNumber, int inTransportationClassification)
  {
    mapRetrievalDatas[inCommandNumber].transportationClassification = inTransportationClassification;
  }

  public void setRetrievalDataCategory(int inCommandNumber, int inCategory)
  {
    mapRetrievalDatas[inCommandNumber].category = inCategory;
  }

  public void setRetrievalDataCompletionClassification(int inCommandNumber, int inCompletionClassification)
  {
    mapRetrievalDatas[inCommandNumber].completionClassification = inCompletionClassification;
  }

  public void setRetrievalDataReInputtingFlag(int inCommandNumber, int inReInputtingFlag)
  {
    mapRetrievalDatas[inCommandNumber].reInputtingFlag = inReInputtingFlag;
  }

  public void setRetrievalDataRetrievalCommandDetail(int inCommandNumber, int inRetrievalCommandDetail)
  {
    mapRetrievalDatas[inCommandNumber].retrievalCommandDetail = inRetrievalCommandDetail;
  }

  public void setRetrievalDataGroupNumber(int inCommandNumber, int inGroupNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmnGroupNumber = inGroupNumber;
  }

  public void setRetrievalDataSourceStationNumber(int inCommandNumber, String isSourceStationNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsSourceStationNumber = isSourceStationNumber;
  }

  public void setRetrievalDataDestinationStationNumber(int inCommandNumber, String isDestinationStationNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsDestinationStationNumber = isDestinationStationNumber;
  }

  public void setRetrievalDataLocationNumber(int inCommandNumber, String isLocationNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsLocationNumber = isLocationNumber;
  }

  public void setRetrievalDataShelfPosition(int inCommandNumber, String isShelfPosition)
  {
    mapRetrievalDatas[inCommandNumber].mmsShelfPosition = isShelfPosition;
  }

  public void setRetrievalDataShelfToShelfLocationNumber(int inCommandNumber, String isShelfToShelfLocationNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsShelfToShelfLocationNumber = isShelfToShelfLocationNumber;
  }

  public void setRetrievalDataShelfToShelfPosition(int inCommandNumber, String isShelfToShelfPosition)
  {
    mapRetrievalDatas[inCommandNumber].mmsShelfToShelfPosition = isShelfToShelfPosition;
  }

  public void setRetrievalDataDimension(int inCommandNumber, int inDimension)
  {
    mapRetrievalDatas[inCommandNumber].dimension = inDimension;
  }

  public void setRetrievalDataBCData(int inCommandNumber, String isBCData)
  {
    int vnBCLength = mapRetrievalDatas[inCommandNumber].mmsBcData.length();
    if (isBCData.length() < vnBCLength)
    {
      isBCData = isBCData + DEFAULT_BARCODE;
      isBCData = isBCData.substring(0, vnBCLength);
    }
    mapRetrievalDatas[inCommandNumber].mmsBcData = isBCData;
  }

  public void setRetrievalDataBCData(int inCommandNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsBcData = DEFAULT_BARCODE;
  }

  public void setRetrievalDataWorkNumber(int inCommandNumber, String isWorkNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsWorkNumber = isWorkNumber;
  }

  public void setRetrievalDataWorkNumber(int inCommandNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsWorkNumber = "00000000";
  }

  public void setRetrievalDataControlInformation(int inCommandNumber, String isControlInformation)
  {
    mapRetrievalDatas[inCommandNumber].mmsControlInformation = isControlInformation;
  }

  public void setRetrievalDataControlInformation(int inCommandNumber)
  {
    mapRetrievalDatas[inCommandNumber].mmsControlInformation = DEFAULT_CONTROL_INFO;
  }

  /*
   * Sets for machine messages
   */

  public void setMachineStatusItem(int inCommandNumber, MachineStatus ipMachineStatus)
  {
    mapMachineStatuses[inCommandNumber].machineId = ipMachineStatus.machineId;
    mapMachineStatuses[inCommandNumber].machineTypeCode = ipMachineStatus.machineTypeCode;
    mapMachineStatuses[inCommandNumber].machineNumber = ipMachineStatus.machineNumber;
    mapMachineStatuses[inCommandNumber].status = ipMachineStatus.status;
    mapMachineStatuses[inCommandNumber].errorCode = ipMachineStatus.errorCode;
  }

  public MachineStatus getMachineStatusItem(int inCommandNumber)
  {
    return mapMachineStatuses[inCommandNumber];
  }

  public void setMachineStatus(int inCommandNumber, String isMachineId,
      int inMachineTypeCode, String isMmachineNumber, int inStatus,
      String isErrorCode)
  {
    mapMachineStatuses[inCommandNumber].machineId = isMachineId;
    mapMachineStatuses[inCommandNumber].machineTypeCode = inMachineTypeCode;
    mapMachineStatuses[inCommandNumber].machineNumber = isMmachineNumber;
    mapMachineStatuses[inCommandNumber].status = inStatus;
    mapMachineStatuses[inCommandNumber].errorCode = isErrorCode;
  }

  private void setMachineStatusMachineId(int inCommandNumber, String isMachineId)
  {
    mapMachineStatuses[inCommandNumber].machineId = isMachineId;
  }

  private void setMachineStatusMachineTypeCode(int inCommandNumber, int inMachineTypeCode)
  {
    mapMachineStatuses[inCommandNumber].machineTypeCode = inMachineTypeCode;
  }

  private void setMachineStatusMachineNumber(int inCommandNumber, String isMachineNumber)
  {
    mapMachineStatuses[inCommandNumber].machineNumber = isMachineNumber;
  }

  private void setMachineStatusStatus(int inCommandNumber, int inStatus)
  {
    mapMachineStatuses[inCommandNumber].status = inStatus;
  }

  private void setMachineStatusErrorCode(int inCommandNumber, String isErrorCode)
  {
    mapMachineStatuses[inCommandNumber].errorCode = isErrorCode;
  }

  /* set Impossible Locations */
  public void setImpossibleLocationStatus(int inCommandNumber, int isStatus)
  {
    mapImpossibleLocations[inCommandNumber].status = isStatus;
  }

  public void setImpossibleLocationStorageClassification(int inCommandNumber, String isStorageClassification)
  {
    mapImpossibleLocations[inCommandNumber].storageClassification = isStorageClassification;
  }

  public void setImpossibleLocationBankNumber(int inCommandNumber, String isBankNumber)
  {
    mapImpossibleLocations[inCommandNumber].bankNumber = "0" + isBankNumber;
  }

  public void setImpossibleLocationStartBayNumber(int inCommandNumber, String isStartBayNumber)
  {
    mapImpossibleLocations[inCommandNumber].startBayNumber = isStartBayNumber;
  }

  public void setImpossibleLocationStartLevelNumber(int inCommandNumber, String isStartLevelNumber)
  {
    mapImpossibleLocations[inCommandNumber].startLevelNumber = isStartLevelNumber;
  }

  public void setImpossibleLocationEndBayNumber(int inCommandNumber, String isEndBayNumber)
  {
    mapImpossibleLocations[inCommandNumber].endBayNumber = isEndBayNumber;
  }

  public void setImpossibleLocationEndLevelNumber(int inCommandNumber, String isEndLevelNumber)
  {
    mapImpossibleLocations[inCommandNumber].endLevelNumber = isEndLevelNumber;
  }

  public void setTerminalOperationCompleteStationNumber(String isStationId)
  {
    msTerminalOperationCompleteStationNumber = isStationId;
  }

  public void setTerminalOperationCompleteTxfrClassification(int inXfrCls)
  {
    mnTerminalOperationCompleteTxfrClassification = inXfrCls;
  }

  /**
   * Parse the location for the mini-parse (short, easy human-readable text)
   *
   * @param isAddress
   * @param isPosition
   * @return
   */
  protected String getMiniParseLocation(String isAddress, String isPosition)
  {
    String vsBank = isAddress.substring(0, 3);
    String vsBay = isAddress.substring(3, 6);
    String vsTier = isAddress.substring(6);

    String vsParsed = vsBank + "-" + vsBay + "-" + vsTier;
    if (mzAddShelfToMiniParse)
    {
      vsParsed += " (" + isPosition + ")";
    }
    return vsParsed;
  }
}