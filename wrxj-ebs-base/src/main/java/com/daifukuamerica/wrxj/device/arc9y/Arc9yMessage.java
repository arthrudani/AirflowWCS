/*
 * Created on Jun 8, 2005
 *
 */
package com.daifukuamerica.wrxj.device.arc9y;

/**
 * Title:        Wrx
 * Description:
 * Copyright:    Copyright (c) 2005
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceEnum;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.agc.AGCMessage;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;

/**
 * Arc9yMessage is the "<b>M</b>aterial Handling <b>C</b>omputer" message set used to
 * communicate with a Daifuku ARC.
 *
 * @author       Stephen Kendorski
 * @version 1.0
 */
public class Arc9yMessage extends AGCMessage
{
  protected boolean mzAddMiniParse = true;

  public static final String RACK_STATION = "9000";
  private static final int HEADER_LENGTH = 16;
  public static final String EMPTY_MCKEY = "00000000";
  public static final String EMPTY_LOCATION = "00000000";
  private int idClassification = 0;
  private String msArcTransmissionTime = "000000";
  private final int ARCLOADLENGTH = 8;
  private int COMMUNICATION_TEST_LENGTH = 232;
  protected static final String NO_ARC_ERROR = "0000";
  
  // Command classifications for alternative location command
  public static final int CC_DATA_CANCEL = 4;

  //
  // Data Values for 22: dateTimeRequest
  //
  // 0: Time modified.
  // 1: Operation start.
  //
  private int requestClassification = 0;
  //
  // Data Values for:  4: TransportDataCancel
  //                   5: TransportCommand
  //
  private String sourceStationNumber = "0000";
  private String destinationStationNumber = "0000";
  private String locationNumber = "        ";
  //
  // Data Values for:  5: TransportCommand
  //
  /**
   * 1: Storage
   * 2: Direct Transfer
   */
  private int transportDivision = 1;
  /**
   * 1: Setting in advance
   * 2: Load check setting
   */
  private int settingClassification = 1;
  private int groupNumber = 0;

  private int dimensionInformation = 0;
  private String bcData = "        ";                                   //  8
  private String workNumber = "        ";                               //  8
  private String MCData = "                              "; // 30
  private String mcKey = "        ";
  /**
   * 1: Destination station changes directions
   * 2: Destination station change is impossible (no alternative location
   *    and reject station)
   */
  private int commandClassification = 1;
  /**
   * Station number for Reject (all zeros = Alternate location)
   */
  private int rejectStationNumber = 0;
  private String terminalOperationCompleteStationNumber;
  /**
   * Stores the intact item of receiver change request
   */
  private String msArcData = "      ";
  //
  // Data Values for: 21: responseToStartOperationRequest
  //
  /**
   *  0: Normal
   *  3: ARC Status Error
   * 99: Data Error
   */
  private int responseClassification = 0;
  /**
   * Effective only when the responseClassification is "3".
   * The number of ARC who has status error (impossible to start work) is stored.
   */
  private String msErrorDetails = "0";
  //
  // Data Values for: 23: responseToTerminateOperationRequest
  //
  // Effective only when the responseClassification is 1 (termination impossible).
  //
  private String responseDetailsMachineId = "000000";
  private String responseDetailsModelCode = "00";
  private String responseDetailsMachineNumber = "0000";
  //
  // 0: Normal Completion.
  // 1: Cancellation impossible since the applicable data has already been on
  //    the way.
  // 2: No applicable transport data.
  //
  private int cancellationResults = 0;
  //
  private String arrivalStationNumber = "0000";
  protected int loadInformation = 0;
  //
  private int continuationClassification = 0;
  protected int mnNumberOfReports = 0;
  protected String msMachineStatusString = "";
  private class MachineStatus
  {
    String machineId = "000000";
    int machineTypeCode = 0;
    String machineNumber = "0000";
    int status = 0;
    String errorCode = "";
  }
  private MachineStatus[] machineStatuses = new MachineStatus[50];
  //
  private String retrievalDataString = "";
  private class RetrievalData
  {
    String mmsMcKey = Arc9yMessage.EMPTY_MCKEY;
    int mmnTransportDivision = 0;
    int mmnType = 0;
    int mmzReStorageFlag = 0;
    int retrieveCommandDetail = 0;
    int mmnGroupNumber = 0;
    //
    String mmsSourceStationNumber = "0000";
    String mmsDestinationStationNumber = "0000";
    String mmsLocationNumber = EMPTY_LOCATION;
    String mmsShelfToShelfLocationNumber = EMPTY_LOCATION;
    String mmsBcData = "        ";             // 8
    String mmsWorkNumber = "00000000";         //  8
    String mmsMCData = "                              "; // 30
    //
    int completionClassification = 0;
   }
  private RetrievalData[] retrievalDatas = new RetrievalData[2];
  //
  // 1: Start.
  // 2: Stop.
  //
  private int startStopClassification = 0;
  //
  private String retrievalStationNumber = "";
  //
  private int inabiltyToStartReason = 0;
  //
  private String operationModeChangeStation = "0000";
  //
  private int operationModeChangeCmd = 0;
  /**
   * TransportCommand: Alternate store location (all zeros = Reject Station)
   */
  private String alternateLocation = EMPTY_LOCATION;
  private int loadSizeInformation = 0;
  private String alternateStationNumber = "0000";
  //
  private int responseClassification2 = 0;
  protected String mcKey2 = "        ";
  //
  private int completionMode = 0;
  private String responseDetails = null;
  private String opCompleteRMNumber = "0000";

  private boolean emulatorMessage = false;


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public Arc9yMessage()
  {
    this(false);
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public Arc9yMessage(boolean izCreatedInEmulator)
  {
    for (int i = 0; i < 2; i++)
    {
      retrievalDatas[i] = new RetrievalData();
    }
    for (int i = 0; i < machineStatuses.length; i++)
    {
      machineStatuses[i] = new MachineStatus();
    }
    setCommTestMessageLength(COMMUNICATION_TEST_LENGTH);
    emulatorMessage = izCreatedInEmulator;
    clearMessageData();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected static void setMachineTypes()
  {
    for (int i = 0; i < machineTypes.length; i++)
    {
      machineTypes[i] = "*UNKNOWN-" + i + "*";
    }
    machineTypes[11] = "S/R-Crane";
    machineTypes[21] = "Conveyor";
    machineTypes[24] = "STV-Shuttle";
    machineTypes[25] = "STV-Loop";
    machineTypes[27] = "BCR";
    machineTypes[31] = "AGV";


    for (int i = 0; i < 10; i++)
    {
      machineTypesFL[i] = "*UNKNOWN-0" + i + "* ";
    }
    for (int i = 10; i < machineTypesFL.length; i++)
    {
      machineTypesFL[i] = "*UNKNOWN-" + i + "* ";
    }
    machineTypesFL[11] = "S/R-Crane    ";
    machineTypesFL[21] = "Conveyor     ";
    machineTypesFL[24] = "STV-Shuttle  ";
    machineTypesFL[25] = "STV-Loop     ";
    machineTypesFL[27] = "BCR          ";
    machineTypesFL[31] = "AGV          ";
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void toDataValues(String sMessageString)
  {
    setValidMessage(true);
    try
    {
      setID(getIntFromTwoAsciiDigits(sMessageString, 0));
      setIDClassification(getIntFromTwoAsciiDigits(sMessageString, 2));
      setTransmissionTime(sMessageString.substring(4, 4+6));
      setArcTransmissionTime(sMessageString.substring(10, 10+6));
      setMessageData(sMessageString.substring(Arc9yMessage.HEADER_LENGTH));
    }
      catch (Exception e)
    {
      if (getValidMessage())
      {
        setValidMessage(false);
        setInvalidMessageDescription("#####  IndexOutOfBoundsException -- Header \"" +
                                     sMessageString + "\"  #####");
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
      case 7: responseToDestinationStationChangeToDataValues(); break;
      //case 9: memoryMaintenanceToDataValues(); break;
      case 10: machineStatusInquiryToDataValues(); break;
      case 11: alternativeLocationCmdToDataValues(); break;
      case 12: retrievalCmdToDataValues(); break;
      case 16: simultaneousStartStopCmdToDataValues(); break;
      case 19: communicationTestRequestToDataValues(); break;
      case 20: responseToCommunicationTestRequestFromArcToDataValues(); break;
      case 21: responseToOperationStartRequestToDataValues(); break;
      case 22: dateTimeRequestToDataValues(); break;
      case 23: responseToOperationTerminationRequestToDataValues(); break;
      case 24: responseToTransportDataCancelToDataValues(); break;
      case 25: responseToTransportCommandToDataValues(); break;
      case 26: arrivalReportToDataValues(); break;
      case 27: requestDestinationStationChangeToDataValues(); break;
      case 30: machineStatusReportToDataValues(); break;
      case 31: responseToAlternativeLocationCmdToDataValues(); break;
      case 32: responseToRetrievalCmdToDataValues(); break;
      case 33: operationCompletionReportToDataValues(); break;
      case 35: trackingDataDeletionReportToDataValues(); break;
      case 37: deviceStatusReportToDataValues(); break;
      case 39: responseToCommunicationTestRequestToDataValues(); break;
      case 40: communicationTestRequestFromArcToDataValues(); break;
      case 50: dataMessageToDataValues(); break; // ID 50
      case 70: message70ToDataValues(); break;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Message Instances.
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Messages sent to ARC From WRx
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  public String requestToStartOperation()  //ID 01
  {
    setID(1);
    setResponseMessage(false);
    setDateTimeString();
    setMessageAsString(headerToString() +
           getDateTimeString());
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
  public void dateAndTime()
  {
    dateTimeDataToString();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private String dateTimeDataToString() // ID 02
  {
    setID(2);
    setResponseMessage(false);
    setDateTimeString();
    setMessageAsString(headerToString() +
           getDateTimeString());
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
  /*--------------------------------------------------------------------------*/
  public String requestToTerminateOperation(int inRequestClassification) // ID 03
  {
    setID(3);
    setRequestClassification(inRequestClassification);
    setResponseMessage(false);
    setMessageAsString(headerToString() +
             ASCII_DIGITS[getRequestClassification()]);
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
    String strng = getTwoAsciiDigits(getID()) + " - RequestToTerminateOperation - ";
    switch (getRequestClassification())
    {
      case 0: strng = strng + "Regular Termination"; break;
      case 1: strng = strng + "Unconditional Termination With Data RETENTION"; break;
      case 2: strng = strng + "Unconditional Termination With Data DELETION"; break;
      default: strng = strng + " *UNKNOWN*: " + getRequestClassification();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
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
             getSubString(getArc9yLocationNumber(), 0, 8));
      return getMessageAsString();
  }

  private void  transportDataCancelToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setSourceStationNumber(getSubString(getMessageData(), 8, 4));
    setDestinationStationNumber(getSubString(getMessageData(), 12, 4));
    setArc9yLocationNumber(getSubString(getMessageData(), 16, 8));
    transportDataCancelGetParsed();
  }

  private void transportDataCancelGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) + " - TransportDataCancel - mcKey \"" + getMCKey() + "\"" +
                   " - SourceStation#: " + getSourceStationNumber() +
                   "  DestinationStation#: " + getDestinationStationNumber() +
                   "  Location# \"" + getArc9yLocationNumber() + "\"";
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Transfer a Load from a Station to a Station.
   * 
   * @param loadID the load Id or Tracking Id
   * @param loadPresence if true, only accept cmd if load is present at source station;
   *                     if false, accept cmd even if there is not a load at the source station.
   * @param sourceStation where load to be moved is currently
   * @param destStation where load is to be moved to
   */
  public void moveLoadStationStation(String loadID, boolean loadPresence, String sourceStation,
            String destStation)
  {
    setMCKey(loadID);
    setTransportDivision(3);  // Direct Transfer
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
    transportCommandToString();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Store a load (station -> location)
   * 
   * @param isLoadID the load Id or Tracking Id
   * @param izLoadPresence if true, only accept cmd if load is present at source station;
   *                     if false, accept cmd even if there is not a load at the source station.
   * @param isSourceStation where load to be moved is currently
   * @param isLocation where load is to be moved to
   * @param isEquipWarehouse
   */
  public void storeLoad(String isLoadID, boolean izLoadPresence,
      String isSourceStation, String isLocation, String isEquipWarehouse)
  {
    setMCKey(isLoadID);
    setTransportDivision(1) ;  // Transport Classification
    setSettingClassification(izLoadPresence ? 2 : 1);
    setSourceStationNumber(isSourceStation);
    setDestinationStationNumber(Arc9yMessage.RACK_STATION);
    setLocationNumber(isLocation, isEquipWarehouse);
    transportCommandToString();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected String transportCommandToString() // ID 05
  {
    setID(5);
    setResponseMessage(false);
    transportCommandGetParsed();
    setMessageAsString(headerToString() +
             parsedMcKey(getMCKey()) +
             ASCII_DIGITS[getTransportDivision()] +
             ASCII_DIGITS[getSettingClassification()] +
             "00000" +
             getSubString(getSourceStationNumber(), 0, 4) +
             getSubString(getDestinationStationNumber(), 0, 4) + 
             LoadData.DEFAULT_POSITION_VALUE +
             getSubString(getArc9yLocationNumber(), 0, 8) +
             getSubString(getBCData(), 0, 8) +
             getSubString(getWorkNumber(), 0, 8) +
             getSubString(getMCData(), 0, 30));
    return getMessageAsString();
  }


  protected void  transportCommandToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setTransportDivision(getIntDigit(getMessageData(), 8));
    setSettingClassification(getIntDigit(getMessageData(), 9));
    // Skip 5 zeros.
    setSourceStationNumber(getSubString(getMessageData(), 15, 4));
    setDestinationStationNumber(getSubString(getMessageData(), 19, 4));
    // Skip 3 zeros.
    setArc9yLocationNumber(getSubString(getMessageData(), 26, 8));

    setBCData(getSubString(getMessageData(), 34, 8));
    setWorkNumber(getSubString(getMessageData(), 42, 8));
    setMCData(getSubString(getMessageData(), 50, 30));
    transportCommandGetParsed();
  }

  protected void  transportCommandGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed += getTwoAsciiDigits(getID()) + " - Transport " + getMCKey() 
           + " from " + getSourceStationNumber() + " to ";
      if (getDestinationStationNumber().equals(RACK_STATION))
      {
        vsParsed += getMiniParseLocation(getLocationNumber());
      }
      else
      {
        vsParsed += getDestinationStationNumber();
      }
      vsParsed += MINI_PARSE_DIVIDER;
    }

    vsParsed += getTwoAsciiDigits(getID()) + " - Transport - mcKey \""
        + getMCKey() + "\"" + " - ";
    switch (getTransportDivision())
    {
      case 1: vsParsed = vsParsed + "Store"; break;
      case 3: vsParsed = vsParsed + "Direct Transfer"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getTransportDivision();
    }
    vsParsed = vsParsed + "  Source: " + getSourceStationNumber()
        + "  Destination: " + getDestinationStationNumber() 
        + "  Location: \"" + getArc9yLocationNumber() 
        + "  Dimension: \"" + getDimensionInformation() 
        + "\"  Barcode: \"" + getBCData() 
        + "\"  Work#: \"" + getWorkNumber() 
        + "\"  MCData: \"" + getMCData() + "\"";
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
  //  2-3-2 Location???...sk
  /*--------------------------------------------------------------------------*/
  public String responseToDestinationStationChangeToString() // ID 07
  {
    setID(7);
    setResponseMessage(false);
    responseToDestinationStationChangeGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getSubString(getArc9yLocationNumber(), 0, 8) +
           getSubString(getDestinationStationNumber(), 0, 4) +
           getSubString(getArcData(), 0, 6));
    return getMessageAsString();
  }

  private void  responseToDestinationStationChangeToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setArc9yLocationNumber(getSubString(getMessageData(), 8, 8));
    setDestinationStationNumber(getSubString(getMessageData(), 20, 4));
    setArcData(getSubString(getMessageData(), 24, 6));
    responseToDestinationStationChangeGetParsed();
  }

  private void responseToDestinationStationChangeGetParsed()
  {
    String strng = getTwoAsciiDigits(getID())
        + " - ResponseToDestStnChange - mcKey \"" + getMCKey() + "\""
        + " - Location# \"" + getArc9yLocationNumber() 
        + "\"  DestStation#: " + getDestinationStationNumber() 
        + "  arcData \"" + getArcData() + "\"";
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  public void machineStatusRequest()
  {
      machineStatusInquiryToString();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
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
  /*--------------------------------------------------------------------------*/
  public void binFullNewLocation(String LoadID, String loc,  String isEquipWarehouse,
                                 int height, String station)
  {
      setCommandClassification(1);  // New Location (for Bin-Full)
      setMCKey(LoadID);
      setAlternateLocation(loc, isEquipWarehouse);
      setLoadSizeInformation(height);
      setAlternateStationNumber(station);
      alternativeLocationCmdToString();
  }

  /*--------------------------------------------------------------------------*/
  public void binFullMoveStation(String LoadID, String station, int height)
  {
      setCommandClassification(2); // New Station (for Bin-Full)
      setMCKey(LoadID);
      setAlternateStationNumber(station);
      setLoadSizeInformation(height);
      alternativeLocationCmdToString();
  }

  /*--------------------------------------------------------------------------*/
  public void binEmptyCancel(String LoadID, String location, String isEquipWarehouse, int height, String station)
  {
      setCommandClassification(4); // New Station (for Bin-Full)
      setMCKey(LoadID);
      setAlternateLocation(location, isEquipWarehouse);
      setAlternateStationNumber(station);
      setLoadSizeInformation(height);
      alternativeLocationCmdToString();
  }

  /*--------------------------------------------------------------------------*/
  // For Bin-Full/Bin-Empty.
  /*--------------------------------------------------------------------------*/
  private String alternativeLocationCmdToString() // ID 11
  {
    setID(11);
    setResponseMessage( false);
    alternativeLocationCmdGetParsed();
    setMessageAsString(headerToString() +
             parsedMcKey(getMCKey()) +
             getSubString(getAlternateArc9yLocation(), 0, 8) +
             getSubString(getAlternateStationNumber(), 0, 4) +
             getSubString(bcData, 0, 8) +
             getSubString(getWorkNumber(), 0, 8) +
             getSubString(getMCData(), 0, 30) +
             ASCII_DIGITS[getCommandClassification()]);
      return getMessageAsString();
  }

  private void  alternativeLocationCmdToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setAlternateArc9yLocation(getSubString(getMessageData(), 8, 8),
                              getSubString(getMessageData(), 8, 1));
    setAlternateStationNumber(getSubString(getMessageData(), 16, 4));
    setBCData(getSubString(getMessageData(), 20, 8));
    setWorkNumber(getSubString(getMessageData(), 28, 8));
    setMCData(getSubString(getMessageData(), 36, 30));
    setCommandClassification(getIntDigit(getMessageData(), 66));
    alternativeLocationCmdGetParsed();
  }

  private void alternativeLocationCmdGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) + " - AlternateLocationCmd - mcKey \"" + getMCKey() +
                   "\" - ";
    strng = strng + " - AlternateLocation# \"" + getAlternateArc9yLocation() +
                    "\"  AlternateStation#: " + getAlternateStationNumber() +
                    "  bcData \"" +  getBCData() +
                    "\"  workNumber \"" +  getWorkNumber() +
                    "\"  MCData \"" +  getMCData() + "\" - ";
    switch (getCommandClassification())
    {
      case  1: strng = strng + "New Location (for BIN-FULL)"; break;
      case  2: strng = strng + "Sending to Station (for BIN-FULL)"; break;
      case  3: strng = strng + "New Location (for BIN-FULL)"; break;
      case 4: strng = strng + "Data Cancel (for BIN-EMPTY)"; break;
       default: strng = strng + " *UNKNOWN*: " + getCommandClassification();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void moveLoadLocationLocation(int command, String loadID, 
                                       String sourceLocation, String isSourceEquipWarehouse,
                                       String destinationLocation, String isDestinationEquipWarehouse)
  {
    setRetrievalDataMCKey(command, loadID);
    setRetrievalDataTransportDivision(command, 5 ); // Bin-To-Bin Move
    setRetrievalDataType(command, 2); // Planned Retrieval
    setRetrievalDataReStorageFlag(command, 0); // No Re-Inputting
    setRetrievalDataRetrieveCommandDetail(command, 1); // Unit Retrieval
    setRetrievalDataSourceStationNumber(command, Arc9yMessage.RACK_STATION);
    setRetrievalDataDestinationStationNumber(command, Arc9yMessage.RACK_STATION);
    setRetrievalDataLocationNumber(command, sourceLocation, isSourceEquipWarehouse);
    setRetrievalDataShelfToShelfLocationNumber(command, destinationLocation, isDestinationEquipWarehouse);
    clearRetrievalDataBCData(0);
    clearRetrievalDataWorkNumber(0);
    clearRetrievalDataMCData(0);
    retrievalCmdToString();
  }


  /**
   * Retrieve a load (location -> station)
   * 
   * @param inCmd
   * @param isLoadID
   * @param inDestStationType
   * @param isDestinationStation
   * @param isLocation
   * @param isEquipWarehouse
   */
  public void retrieveLoadLocationStation(int inCmd, String isLoadID,
        int inDestStationType, String isDestinationStation, String isLocation, 
        String isEquipWarehouse)
  {
    setRetrievalDataMCKey(inCmd, isLoadID);
    setRetrievalDataTransportDivision(inCmd, 2); // Retrieve
    setRetrievalDataType(inCmd, 2); // Planned Retrieval
    setRetrievalDataReStorageFlag(inCmd, 0); // NO Re-Inputting
    switch (inDestStationType)
    {
      case DBConstants.PDSTAND:
        setRetrievalDataRetrieveCommandDetail(inCmd, 2); // Picking Retrieval
        break;
      case DBConstants.REVERSIBLE:
        setRetrievalDataRetrieveCommandDetail(inCmd, 2); // Picking Retrieval
        break;
      default:
        setRetrievalDataRetrieveCommandDetail(inCmd, 1); // Unit Retrieval
        break;
    }
    setRetrievalDataSourceStationNumber(inCmd, Arc9yMessage.RACK_STATION);
    setRetrievalDataDestinationStationNumber(inCmd, isDestinationStation);
    setRetrievalDataLocationNumber(inCmd, isLocation, isEquipWarehouse);
    setRetrievalDataShelfToShelfArc9yLocationNumber(inCmd, EMPTY_LOCATION, "0");
    clearRetrievalDataBCData(0);
    clearRetrievalDataWorkNumber(0);
    clearRetrievalDataMCData(0);
    retrievalCmdToString();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected String retrievalCmdToString() // ID 12
  {
    setID(12);
    setResponseMessage( true);
    retrievalCmdGetParsed();
    String sResult = headerToString();

    for (int i = 0; i < 2; i++)
    {
      sResult = sResult + parsedMcKey(getRetrievalDataMCKey(i)) +
                          ASCII_DIGITS[getRetrievalDataTransportDivision(i)] +
                          ASCII_DIGITS[getRetrievalDataType(i)] +
                          ASCII_DIGITS[getRetrievalDataReStorageFlag(i)] +
                          ASCII_DIGITS[getRetrievalDataRetrieveCommandDetail(i)] +
                          getThreeAsciiDigits(getRetrievalDataGroupNumber(i)) +
                          getSubString(getRetrievalDataSourceStationNumber(i), 0, 4) +
                          getSubString(getRetrievalDataDestinationStationNumber(i), 0, 4) +
                          "000" +
                          getSubString(getRetrievalDataArc9yLocationNumber(i), 0, 8) +
                          getSubString(getRetrievalDataShelfToShelfArc9yLocationNumber(i), 0, 8) +
                          getSubString(getRetrievalDataBCData(i), 0, 8) +
                          getSubString(getRetrievalDataWorkNumber(i), 0, 8) +
                          getSubString(getRetrievalDataMCData(i), 0, 30);
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  private void  retrievalCmdToDataValues()
  {
    retrievalDataString = getSubString(getMessageData(), 0, 2*88);
    for (int i = 0; i < 2; i++)
    {
      int idx = i * 88;
      setRetrievalDataMCKey(i, getSubString(retrievalDataString, idx + 0, 8).trim());
      setRetrievalDataTransportDivision(i, getIntDigit(retrievalDataString, idx + 8));
      setRetrievalDataType(i, getIntDigit(retrievalDataString, idx + 9));
      setRetrievalDataReStorageFlag(i, getIntDigit(retrievalDataString, idx + 10));
      setRetrievalDataRetrieveCommandDetail(i, getIntDigit(retrievalDataString, idx + 11));
      setRetrievalDataGroupNumber(i, getIntFromThreeAsciiDigits(retrievalDataString, idx + 12));
      setRetrievalDataSourceStationNumber(i, getSubString(retrievalDataString, idx + 15, 4));
      setRetrievalDataDestinationStationNumber(i, getSubString(retrievalDataString, idx + 19, 4));
      // Skip 3 bytes for ARC.
      if (emulatorMessage)
      {
        setRetrievalDataArc9yLocationNumber(i, getSubString(retrievalDataString, idx + 26, 8));
      }
      else
      {
        setRetrievalDataLocationNumber(i, getSubString(retrievalDataString, idx + 26, 8),
                                          getSubString(retrievalDataString, idx + 26, 1));
      }
      setRetrievalDataShelfToShelfArc9yLocationNumber(i, getSubString(retrievalDataString, idx + 34, 8),
                                                         getSubString(retrievalDataString, idx + 34, 1));
      setRetrievalDataBCData(i, getSubString(retrievalDataString, idx + 42, 8));
      setRetrievalDataWorkNumber(i, getSubString(retrievalDataString, idx + 50, 8));
      setRetrievalDataMCData(i, getSubString(retrievalDataString, idx + 58, 30));
    }
    retrievalCmdGetParsed();
  }

  protected void retrievalCmdGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed = getTwoAsciiDigits(getID()) + " - Retrieve ";
      for (int i = 0; i < 2; i++)
      {
        String vsMCKey = getRetrievalDataMCKey(i);
        if (vsMCKey.equals(EMPTY_MCKEY))
        {
          continue;
        }
        if (i > 0)
        {
          vsParsed += " | ";
        }
        vsParsed += vsMCKey + " from ";
        
        vsParsed += getMiniParseLocation(getRetrievalDataLocationNumber(i)) + " to ";
        if (getRetrievalDataDestinationStationNumber(i).equals(RACK_STATION))
        {
          vsParsed += getMiniParseLocation(getRetrievalDataShelfToShelfLocationNumber(i));
        }
        else
        {
          vsParsed += getRetrievalDataDestinationStationNumber(i);
        }
      }
      vsParsed += MINI_PARSE_DIVIDER;
    }

    vsParsed += getTwoAsciiDigits(getID()) + " - Retrieval - ";
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

      switch (getRetrievalDataTransportDivision(i))
      {
        case 2: vsParsed = vsParsed + "Retrieve-to-Station"; break;
        case 5: vsParsed = vsParsed + "Bin-to-Bin-Move"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataTransportDivision(i);
      }
      
      vsParsed = vsParsed + "  Group: " + getRetrievalDataGroupNumber(i)
          + "  Source: " + getRetrievalDataSourceStationNumber(i)
          + "  Location: \"" + getRetrievalDataArc9yLocationNumber(i) 
          + "\"  Destination: " + getRetrievalDataDestinationStationNumber(i)
          + "  ShelfToShelf: \"" + getRetrievalDataShelfToShelfArc9yLocationNumber(i) 
          + "\"  Barcode: \"" + getRetrievalDataBCData(i) 
          + "\"  Work#: \"" + getRetrievalDataWorkNumber(i) 
          + "\"  MCData: \"" + getRetrievalDataMCData(i) + "\"";

      vsParsed = vsParsed + "  Category: ";
      switch (getRetrievalDataType(i))
      {
        case 1: vsParsed = vsParsed + "Urgent Retrieval"; break;
        case 2: vsParsed = vsParsed + "Planned Retrieval"; break;
        case 9: vsParsed = vsParsed + "Empty Location Check"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataType(i);
      }
      vsParsed = vsParsed + "  ReStorageFlag: ";
      switch (getRetrievalDataReStorageFlag(i))
      {
        case 0: vsParsed = vsParsed + "Retrieval Only"; break;
        case 1: vsParsed = vsParsed + "Re-Storing to Same Location"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataReStorageFlag(i);
      }
      vsParsed = vsParsed + "  RetrievalDetail: ";
      switch (getRetrievalDataRetrieveCommandDetail(i))
      {
        case 0: vsParsed = vsParsed + "Inventory Check"; break;
        case 1: vsParsed = vsParsed + "Unit Retrieval"; break;
        case 2: vsParsed = vsParsed + "Picking Retrieval"; break;
        case 3: vsParsed = vsParsed + "Adding (Pick+Store) Retrieval"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataRetrieveCommandDetail(i);
      }
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

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private String simultaneousStartStopCmdToString() // ID 16
  {
    setID(16);
    setResponseMessage(false);
    simultaneousStartStopCmdGetParsed();
    setMessageAsString(headerToString() +
             ASCII_DIGITS[getStartStopClassification()]);
    return getMessageAsString();
  }

  private void simultaneousStartStopCmdToDataValues()
  {
    setStartStopClassification(getIntDigit(getMessageData(), 0));
    simultaneousStartStopCmdGetParsed();
  }

  private void simultaneousStartStopCmdGetParsed()
  {
    String strng = getTwoAsciiDigits(getID());
    switch (getStartStopClassification())
    {
      case 1: strng = strng + " - SimultaneousStartCmd"; break;
      case 2: strng = strng + " - SimultaneousStopCmd"; break;
      default: strng = strng + " - SimultaneousStartStopCmd *UNKNOWN*: " + getStartStopClassification();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  public void StartCommTest()
  {
      communicationTestRequestToString();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private String communicationTestRequestToString() //ID 19
  {
    setID(19);
    setResponseMessage( false);
    communicationTestRequestGetParsed();
    messageAsString = headerToString() +
           getSubString(getCommunicationTestTextRequest(), 0, 232);
    return getMessageAsString();
  }

  private void communicationTestRequestToDataValues()
  {
    setCommunicationTestTextRequest(getSubString(getMessageData(), 0, 232));
    communicationTestRequestGetParsed();
  }

  private void communicationTestRequestGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - CommunicationTestRequest");
  }

  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Messages sent to WRx From ARC
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private String responseToCommunicationTestRequestFromArcToString() // ID 20
  {
    setID(20);
    setResponseMessage( false);
    responseToCommunicationTestRequestFromArcGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getCommunicationTestTextResponse(), 0, 232));
    return getMessageAsString();
  }

  private void responseToCommunicationTestRequestFromArcToDataValues()
  {
    setCommunicationTestTextResponse(getSubString(getMessageData(), 0, 232));
    responseToCommunicationTestRequestFromArcGetParsed();
  }

  private void responseToCommunicationTestRequestFromArcGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - CommunicationTestResponse");
  }

  /*--------------------------------------------------------------------------*/
  /**
   * This is an ID 20 - WRx-J's response to a Comm Test REQUEST from the ARC.
   */
  public void responseToCommTestDevice()
  {
      responseToCommunicationTestRequestFromArcToString();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String responseToOperationStartRequestToString() // ID 21
  {
    setID(21);
    setResponseMessage( true);
    responseToOperationStartRequestGetParsed();
    setMessageAsString(headerToString() +
           getTwoAsciiDigits(getResponseClassification()) +
           getSubString(msErrorDetails, 0, 2));
    return getMessageAsString();
  }

  private void responseToOperationStartRequestToDataValues()
  {
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 0));
    setErrorDetails(getSubString(getMessageData(), 2, 2));
    responseToOperationStartRequestGetParsed();
  }

  private void responseToOperationStartRequestGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) +
                   " - OperationStartResponse - ";
    String response = null;
    switch (getResponseClassification())
    {
      case 0: response = "Normal"; break;
      case 3: response = "** ARC Status Error - ErrorDetails: " + msErrorDetails + " **"; break;
      case 99: response = "** Data Error **"; break;
      default: response = "** *UNKNOWN*: " + getResponseClassification() + " **";
    }
    strng = strng + response + "  ErrorDetails: " + msErrorDetails;
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String dateTimeRequestToString() // ID 22
  {
    setID(22);
    setResponseMessage( true);
    dateTimeRequestGetParsed();
    setMessageAsString(headerToString() +
        ASCII_DIGITS[getRequestClassification()]);
    return getMessageAsString();
  }

  private void dateTimeRequestToDataValues()
  {
    setRequestClassification(getIntDigit(getMessageData(), 0));
    dateTimeRequestGetParsed();
  }

  private void dateTimeRequestGetParsed()
  {
    String vsParsed = getTwoAsciiDigits(getID()) + " - DateTimeRequest - ";
    switch (getRequestClassification())
    {
      case 0: vsParsed += "Time Modified"; break;
      case 1: vsParsed += "Operation Start"; break;
      default: vsParsed += " *UNKNOWN*: " + getRequestClassification();
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
           getSubString(getErrorDetails(), 0, 2));
    return getMessageAsString();
  }

   private void responseToOperationTerminationRequestToDataValues()
  {
    setResponseClassification(getIntFromTwoAsciiDigits(getMessageData(), 0));
    setErrorDetails(getSubString(getMessageData(), 2, 2));
    responseToOperationTerminationRequestGetParsed();
  }

  private void responseToOperationTerminationRequestGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) + " - OperationTerminationResponse - ";
    String vsResponse = null;
    switch (getResponseClassification())
    {
      case 0: vsResponse = "Normal Termination"; break;
      case 1: vsResponse = "RM Operating"; break;
      case 2: vsResponse = "CO  Operating"; break;
      case 3: vsResponse = "STV  Operating"; break;
      case 4: vsResponse = "AGV  Operating"; break;
      case 99: vsResponse = "Data ERROR"; break;
      default: vsResponse = " *UNKNOWN*: " + getResponseClassification();
    }
    strng = strng + vsResponse + "  ErrorDetails: " + getErrorDetails();
    setParsedMessageString(strng);
  }

  public String getStatusForResponseToOperationTerminationRequest()
  {
    //...sk NOT the same???
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
           ASCII_DIGITS[getCancellationResults()]);
    return getMessageAsString();
  }

  private void   responseToTransportDataCancelToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setCancellationResults(getIntFromOneAsciiDigit(getMessageData(), 8));
    responseToTransportDataCancelGetParsed();
  }

  private void responseToTransportDataCancelGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) + " - TransportDataCancelResponse - mcKey \"" + getMCKey() + "\"" +
                   " - ";
    switch (getCancellationResults())
    {
      case 0: strng = strng + "Normal Completion"; break;
      case 1: strng = strng + "Cancellation IMPOSSIBLE (Data Executing)"; break;
      case 2: strng = strng + "NO Applicable Transport Data"; break;
      default: strng = strng + " *UNKNOWN*: " + getCancellationResults();
    }
    setParsedMessageString(strng);
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
      case 3: vsParsed = vsParsed + "** Duplicate Command (The SAME mcKey is in the ARC) **"; break;
      case 4: vsParsed = vsParsed + "** (Source Designated) is OUT-OF-ORDER **"; break;
      case 5: vsParsed = vsParsed + "** (Source Designated) is DISCONNECTED **"; break;
      case 6: vsParsed = vsParsed + "** ARC is OFF-LINE **"; break;
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
           ASCII_DIGITS[getDimensionInformation()] +
           getSubString(getBCData(), 0, 8) +
           ASCII_DIGITS[getLoadInformation()] +
           getSubString(getMCData(), 0, 30));
    return getMessageAsString();
  }


  public void  arrivalReportToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setArrivalStationNumber(getSubString(getMessageData(), 8, 4));
    setDimensionInformation(getIntDigit(getMessageData(), 12));
    setBCData(getSubString(getMessageData(), 13, 8));
    setLoadInformation(getIntDigit(getMessageData(), 21));
    setMCData(getSubString(getMessageData(), 22, 30));
    arrivalReportGetParsed();
  }

  private void arrivalReportGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed = getTwoAsciiDigits(getID()) 
          + " - Arrival " + getMCKey() + " at " + getArrivalStationNumber() 
          + "  H:" + getDimensionInformation() + "  BC:\"" + getBCData() + "\"";
      vsParsed += MINI_PARSE_DIVIDER;
    }

    vsParsed += getTwoAsciiDigits(getID()) 
        + " - ArrivalReport - mcKey \"" + getMCKey() + "\"" 
        + " - Station#: " + getArrivalStationNumber()
        + "  Dimension: " + getDimensionInformation()
        + "  Barcode: \"" + getBCData() 
        + "\"  LoadPresence: ";
    switch (loadInformation)
    {
      case 0: vsParsed = vsParsed + "None (For Picking Cancel)"; break;
      case 1: vsParsed = vsParsed + "Present (For Conventional Storing/Re-Storing)"; break;
      default: vsParsed = vsParsed + " *UNKNOWN*: " + getLoadInformation();
    }
    vsParsed = vsParsed + "  MCData \"" +  getMCData() + "\"";
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  //  2-3-2 Location???...sk
  /*--------------------------------------------------------------------------*/
  public String requestDestinationStationChangeToString() // ID 27
  {
    setID(27);
    setResponseMessage( true);
    requestDestinationStationChangeGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getSubString(getArc9yLocationNumber(), 0, 8) +
           getSubString(getDestinationStationNumber(), 0, 4) +
           getSubString(getArcData(), 0, 6));
    return getMessageAsString();
  }

  private void  requestDestinationStationChangeToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setArc9yLocationNumber(getSubString(getMessageData(), 8, 8));
    setDestinationStationNumber(getSubString(getMessageData(), 20, 4));
    setArcData(getSubString(getMessageData(), 24, 6));
    requestDestinationStationChangeGetParsed();
  }

  private void requestDestinationStationChangeGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) 
        + " - RequestDestStationChange - mcKey \"" + getMCKey() + "\"" 
        + " - Location: \"" + getArc9yLocationNumber() 
        + "\"  Dest: " + getDestinationStationNumber() 
        + "  arcData \"" +  getArcData() + "\"";
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String machineStatusReportToString()  // ID 30
  {
    setID(30);
    setResponseMessage(true);
    machineStatusReportGetParsed();
    String sResult = headerToString() +
                     ASCII_DIGITS[getContinuationClassification()];
    for (int i = 0; i < mnNumberOfReports; i++)
    {
      sResult = sResult + getTwoAsciiDigits(getMachineStatusMachineTypeCode(i)) +
                          getSubString(getMachineStatusMachineNumber(i), 0, 4) +
                          ASCII_DIGITS[getMachineStatusStatus(i)] +
                          getSubString(getMachineStatusErrorCode(i), 0, 4);
    }
    // Arc returns 9 statuses per message
    for (int i = mnNumberOfReports; i < 9; i++)
    {
      sResult = sResult + "00000000000";
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  /**
   * Convert the status report to status messages
   */
  protected void  machineStatusReportToDataValues()
  {
    try
    {
      setContinuationClassification(getIntDigit(getMessageData(), 0));
      setMachineStatusString(getSubString(getMessageData(), 1, 99));
      clearMachineStatuses();
      for (int i = 0; i < 9; i++, mnNumberOfReports++)
      {
        int idx = i * 11;
        int vnType = getIntFromTwoAsciiDigits(msMachineStatusString, idx + 0);
        if (vnType == 0)
        {
          break;
        }
        setMachineStatusMachineId(i, getSubString(msMachineStatusString, idx + 0, 6));
        setMachineStatusMachineTypeCode(i, vnType);
        setMachineStatusMachineNumber(i, getSubString(msMachineStatusString, idx + 2, 4));
        setMachineStatusStatus(i, getIntDigit(msMachineStatusString, idx + 6));
        setMachineStatusErrorCode(i, getSubString(msMachineStatusString, idx + 7, 4));
      }
    }
    catch (Exception e)
    {
      if (getValidMessage())
      {
        setValidMessage(false);
        setInvalidMessageDescription(("#####  Exception  #####"));
      }
    }
    machineStatusReportGetParsed();
  }

  protected void machineStatusReportGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) + " - MachineStatusReport - ";
    switch (getContinuationClassification())
    {
      case 1: strng = strng + "Multiple Reports"; break;
      case 0: strng = strng + "Final Report"; break;
      default: strng = strng + " *UNKNOWN*: " + getContinuationClassification();
    }
    strng = strng + " Machine Reports (TypeCode-Nmbr-Stts-ErrCd):";
    for (int i = 0; i < 9; i++)
    {
      int vnType = getMachineStatusMachineTypeCode(i);
      if (vnType == 0)
      {
        break;
      }
      strng = strng + "   ";
      strng = strng + vnType + "-";
      strng = strng + getMachineStatusMachineNumber(i) + "-";
      switch (getMachineStatusStatus(i))
      {
        case 0: strng = strng + "On-"; break;
        case 1: strng = strng + "Off-"; break;
        case 2: strng = strng + "Error-"; break;
        case 3: strng = strng + "Disconnected-"; break;
        default: strng = strng + "*UNKNOWN: " + getMachineStatusStatus(i) + "*-";
      }
      strng = strng + getMachineStatusErrorCode(i);
    }
    setParsedMessageString(strng);
  }

  public String getMachineStatusMessage()
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.MACHINE_STATUS);

    for (int i = 0; i < mnNumberOfReports; i++)
    {
      int vnType = getMachineStatusMachineTypeCode(i);
      if (vnType == 0)
      {
        break;
      }
      int vnMCStatus = getMachineStatusStatus(i);
      String vsErrorCode = getMachineStatusErrorCode(i);
      if ((vnMCStatus != 0) && (!vsErrorCode.equals(Arc9yMessage.NO_ARC_ERROR)))
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
      if (vsErrorCode.equals(Arc9yMessage.NO_ARC_ERROR))
      {
        vsErrorCode = StatusEventDataFormat.NONE;
      }
      vpSEDF.addMachineStatus(getMachineStatusMachineId(i), 
          machineTypes[getMachineStatusMachineTypeCode(i)],
          getMachineStatusMachineNumber(i), vnDBStatus, vsDesc, vsErrorCode);
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
    String strng = getTwoAsciiDigits(getID())
        + " - AlternateLocationResponse - mcKey \"" + getMCKey() + "\" - ";
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
    strng = strng + response;
    responseDetails = "LoadId \"" + getMCKey() + "\" \"" + response
        + "\" - ResponseToAlternateLocationCmd(31) Received";
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
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

  private void   responseToRetrievalCmdToDataValues()
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
      if (!getMCKey().equals(EMPTY_MCKEY))
      {
        vsParsed += getMCKey() + " ";
        
        vsParsed += getRetrievalCommandResponseString(0);
      }

      if (!getMCKey2().equals(EMPTY_MCKEY))
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
    responseDetails = "LoadId \"" + getMCKey() + "\"";
    setParsedMessageString(vsParsed);
  }

  public String getRetrievalCommandResponseString(int num)
  {
    String result;
    int vnResponse = 0;
    
    if (num == 0) vnResponse = getResponseClassification();
    else          vnResponse = getResponseClassification2();
      
    switch (vnResponse)
    {
      case 0: result = "OK"; break;
      case 3: result = "** Duplicate **"; break;
      case 6: result = "** ARC OFF-LINE **"; break;
      case 11: result = "** Buffer FULL **"; break;
      case 99: result = "** Data ERROR **"; break;
      default: result = "** UNKNOWN" + vnResponse + " **";
    }
    return result;
  }
  
  /*--------------------------------------------------------------------------*/
  // opCompleteRMNumber ???...sk
  /*--------------------------------------------------------------------------*/
  public String operationCompletionReportToString() // ID 33
  {
    setID(33);
    setResponseMessage(true);
    operationCompletionReportGetParsed();
    String sResult = headerToString() + opCompleteRMNumber;
    for (int i = 0; i < 2; i++)
    {
      sResult = sResult + parsedMcKey(getRetrievalDataMCKey(i)) +
                          ASCII_DIGITS[getRetrievalDataTransportDivision(i)] +
                          ASCII_DIGITS[getRetrievalDataType(i)] +
                          ASCII_DIGITS[getRetrievalDataCompletionClassification(i)] +
                          getSubString(getRetrievalDataSourceStationNumber(i), 0, 4) +
                          getSubString(getRetrievalDataDestinationStationNumber(i), 0, 4) +
                          getSubString(getRetrievalDataArc9yLocationNumber(i), 0, 8) +
                          getSubString(getRetrievalDataShelfToShelfArc9yLocationNumber(i), 0, 8) +
                          getSubString(getRetrievalDataBCData(i), 0, 8) +
                          getSubString(getRetrievalDataWorkNumber(i), 0, 8) +
                          getSubString(getRetrievalDataMCData(i), 0, 30);
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  private void  operationCompletionReportToDataValues()
  {
    opCompleteRMNumber = getSubString(getMessageData(), 0, 4);
    setRetrievalDataString(getSubString(getMessageData(), 4, 2*81));
    for (int i = 0; i < 2; i++)
    {
      int idx = i * 81;
      setRetrievalDataMCKey(i, getSubString(retrievalDataString, idx + 0, 8).trim());
      setRetrievalDataTransportDivision(i, getIntDigit(retrievalDataString, idx + 8));
      setRetrievalDataType(i, getIntDigit(retrievalDataString, idx + 9));
      setRetrievalDataCompletionClassification(i, getIntDigit(retrievalDataString, idx + 10));
      setRetrievalDataSourceStationNumber(i, getSubString(retrievalDataString, idx + 11, 4));
      setRetrievalDataDestinationStationNumber(i, getSubString(retrievalDataString, idx + 15, 4));
      setRetrievalDataArc9yLocationNumber(i, getSubString(retrievalDataString, idx + 19, 8));
      setRetrievalDataShelfToShelfLocationNumber(i, getSubString(retrievalDataString, idx + 27, 8));
      setRetrievalDataBCData(i, getSubString(retrievalDataString, idx + 35, 8));
      setRetrievalDataWorkNumber(i, getSubString(retrievalDataString, idx + 43, 8));
      setRetrievalDataMCData(i, getSubString(retrievalDataString, idx + 51, 30));
    }
    operationCompletionReportGetParsed();
  }

  private void   operationCompletionReportGetParsed()
  {
    String vsParsed = "";
    if (mzAddMiniParse)
    {
      vsParsed += getTwoAsciiDigits(getID()) + " - OperationComplete ";

      for (int i = 0; i < 2; i++)
      {
        String vsMCKey = getRetrievalDataMCKey(i);
        if (vsMCKey.equals(EMPTY_MCKEY))
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
            vsParsed += ((getRetrievalDataTransportDivision(i) == 4) 
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
        if (getRetrievalDataSourceStationNumber(i).equals(RACK_STATION))
        {
          vsParsed += getMiniParseLocation(getRetrievalDataLocationNumber(i));
        }
        else
        {
          vsParsed += getRetrievalDataSourceStationNumber(i);
        }
        vsParsed += " to ";
        if (getRetrievalDataDestinationStationNumber(i).equals(RACK_STATION))
        {
          if (getRetrievalDataShelfToShelfArc9yLocationNumber(i).equals(EMPTY_LOCATION))
          {
            vsParsed += getMiniParseLocation(getRetrievalDataLocationNumber(i));  
          }
          else
          {
            vsParsed += getMiniParseLocation(getRetrievalDataShelfToShelfLocationNumber(i));  
          }
        }
        else
        {
          vsParsed += getRetrievalDataDestinationStationNumber(i);
        }
      }

      vsParsed += MINI_PARSE_DIVIDER;
    }

    vsParsed += getTwoAsciiDigits(getID()) + " - OperationCompletionReport - "
        + "RM#: " + opCompleteRMNumber + " - ";
    for (int i = 0; i < 2; i++)
    {
      vsParsed += " Data-" + (i + 1) + " mcKey \"" + getRetrievalDataMCKey(i)
          + "\"";
      
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
      vsParsed += "  Source: " + getRetrievalDataSourceStationNumber(i)
          + "  Location: \"" + getRetrievalDataArc9yLocationNumber(i) + "\""
          + "  Destination: " + getRetrievalDataDestinationStationNumber(i)
          + "  ShelfToShelf: \"" + getRetrievalDataShelfToShelfArc9yLocationNumber(i) + "\"";
      
      vsParsed += " - Transport: ";
      switch (getRetrievalDataTransportDivision(i))
      {
        case 1: vsParsed = vsParsed + "Storage"; break;
        case 2: vsParsed = vsParsed + "Retrieve"; break;
        case 4: vsParsed = vsParsed + "Shelf-To-Shelf Move (Retrieval)"; break;
        case 5: vsParsed = vsParsed + "Shelf-To-Shelf Move (Storage)"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataTransportDivision(i);
      }

      vsParsed = vsParsed + "  Type: ";
      switch (getRetrievalDataType(i))
      {
        case 1: vsParsed = vsParsed + "Urgent Retrieval"; break;
        case 2: vsParsed = vsParsed + "Planned Retrieval"; break;
        case 9: vsParsed = vsParsed + "Empty Location Check"; break;
        default: vsParsed = vsParsed + " *UNKNOWN*: " + getRetrievalDataType(i);
      }

      vsParsed += "  Barcode: \"" + getRetrievalDataBCData(i) 
          + "\"  Work#: \"" + getRetrievalDataWorkNumber(i) 
          + "\" MCData: \"" + getRetrievalDataMCData(i) + "\"";

      vsParsed += " | ";
    }

    setParsedMessageString(vsParsed);
  }

  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String trackingDataDeletionReportToString() // ID 35
  {
    setID(35);
    setResponseMessage( true);
    trackingDataDeletionReportGetParsed();
    setMessageAsString(headerToString() +
           parsedMcKey(getMCKey()) +
           getSubString(getDestinationStationNumber(), 0, 4) +
           getSubString(getMCData(), 0, 30));
    return getMessageAsString();
  }

  private void   trackingDataDeletionReportToDataValues()
  {
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    setDestinationStationNumber(getSubString(getMessageData(), 8, 4));
    setMCData(getSubString(getMessageData(), 12, 30));
    trackingDataDeletionReportGetParsed();
  }

  private void trackingDataDeletionReportGetParsed()
  {
    String strng = getTwoAsciiDigits(getID())
        + " - TrackingDataDeletionReport - mcKey \"" + getMCKey()
        + "\"  DestStation#: " + getDestinationStationNumber() 
        + "  MCData \"" + getMCData() + "\"";
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String deviceStatusReportToString()  // ID 37
  {
    setID(37);
    setResponseMessage(true);
    deviceStatusReportGetParsed();
    String sResult = headerToString();
    for (int i = 0; i < 30; i++)
    {
      int vnMachineType = getMachineStatusMachineTypeCode(i);
      if (vnMachineType == 0)
      {
        break;
      }
      sResult = sResult + getTwoAsciiDigits(vnMachineType) +
                          getSubString(getMachineStatusMachineNumber(i), 0, 4) +
                          ASCII_DIGITS[getMachineStatusStatus(i)];
    }
    setMessageAsString(sResult);
    return getMessageAsString();
  }

  private void  deviceStatusReportToDataValues()
  {
    try
    {
      setContinuationClassification(getIntDigit(getMessageData(), 0));
      setMachineStatusString(getSubString(getMessageData(), 1, 210));
      clearMachineStatuses();
      for (int i = 0; i < 30; i++, mnNumberOfReports++)
      {
        int idx = i * 7;
        int vnType = getIntFromTwoAsciiDigits(msMachineStatusString, idx + 0);
        if (vnType == 0)
        {
          break;
        }
        String vsMachineType = getSubString(msMachineStatusString, idx + 0, 6);
        if (vsMachineType.equals("000000"))
        {
          break;
        }
        setMachineStatusMachineId(i, vsMachineType);
        setMachineStatusMachineTypeCode(i, vnType);
        setMachineStatusMachineNumber(i, getSubString(msMachineStatusString, idx + 2, 4));
        setMachineStatusStatus(i, getIntDigit(msMachineStatusString, idx + 6));
      }
    }
    catch (Exception e)
    {
      if (getValidMessage())
      {
        setValidMessage(false);
        setInvalidMessageDescription(("#####  Exception  #####"));
      }
    }
    deviceStatusReportGetParsed();
  }

  private void deviceStatusReportGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) + " - MachineStatusReport - ";
    strng = strng + " Machine Reports (TypeCode-Nmbr-Stts-ErrCd):";
    for (int i = 0; i < 30; i++)
    {
      int vnType = getMachineStatusMachineTypeCode(i);
      if (vnType == 0)
      {
        break;
      }
      strng = strng + "   ";
      strng = strng + vnType + "-";
      strng = strng + getMachineStatusMachineNumber(i) + "-";
      switch (getMachineStatusStatus(i))
      {
        case 0: strng = strng + "On-"; break;
        case 1: strng = strng + "Off-"; break;
        case 2: strng = strng + "Error-"; break;
        case 3: strng = strng + "Disconnected-"; break;
        default: strng = strng + "*UNKNOWN: " + getMachineStatusStatus(i) + "*-";
      }
      strng = strng + getMachineStatusErrorCode(i);
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * This is an ID 39 - ARC's response to a Comm Test REQUEST from WRx-J.
   */
  public String responseToCommunicationTestRequestToString() // ID 39
  {
    setID(39);
    setResponseMessage( true);
    responseToCommunicationTestRequestGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getCommunicationTestTextResponse(), 0, 232));
    return getMessageAsString();
  }

  private void responseToCommunicationTestRequestToDataValues()
  {
    setCommunicationTestTextResponse(getSubString(getMessageData(), 0, 232));
    responseToCommunicationTestRequestGetParsed();
  }

  private void responseToCommunicationTestRequestGetParsed()
  {
    String s = "OK";
    if (! getCommunicationTestResult())
    {
      s = "*FAIL*";
    }
    String strng = getTwoAsciiDigits(getID()) + " - CommunicationTestResponse "
        + s + "  TextLength: " + communicationTestTextResponse.length();
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String communicationTestRequestFromArcToString() // ID 40
  {
    setID(40);
    setResponseMessage( true);
    communicationTestRequestFromArcGetParsed();
    setMessageAsString(headerToString() +
           getSubString(getCommunicationTestTextRequest(), 0, 232));
    return getMessageAsString();
  }

  private void communicationTestRequestFromArcToDataValues()
  {
    setCommunicationTestTextRequest(getSubString(getMessageData(), 0, 232));
    communicationTestRequestFromArcGetParsed();
  }

  private void communicationTestRequestFromArcGetParsed()
  {
    setParsedMessageString(getTwoAsciiDigits(getID()) + " - CommunicationTestRequest");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected String parsedMcKey(String s)
  {
    String vsResult;
    if (s.length() == 8)
    {
      vsResult = getSubString(s, 0, 8);
    }
    else
    {
      s = s + "        ";
      s = s.substring(0, 8);
      vsResult = s;
    }
    return vsResult;
  }

  protected String headerToString()
  {
    setValidMessage(true);
    String sid = getTwoAsciiDigits(getID());
    String sidClassification = getTwoAsciiDigits(idClassification);
    if (getResponseMessage())
    {
      setArcTransmissionTime( msgDateTime.getCurrentDateTimeAsString());
    }
    else
    {
      setTransmissionTime( msgDateTime.getCurrentDateTimeAsString());
      setArcTransmissionTime( "000000");
    }
    return sid +
           sidClassification +
           getTransmissionTime() +
           getArcTransmissionTime();
  }

  private String headerGetParsed()
  {
    return getTwoAsciiDigits(getID()) + " - Header - idClassification" +
          getTwoAsciiDigits(getIDClassification()) +
          "  mcTxTime: " +
          getSubString(getTransmissionTime(), 0, 2) + ":" +
          getSubString(getTransmissionTime(), 2, 2) + ":" +
          getSubString(getTransmissionTime(), 4, 2) +
          "  arcTxTime: " +
          getSubString(getTransmissionTime(), 0, 2) + ":" +
          getSubString(getTransmissionTime(), 2, 2) + ":" +
          getSubString(getTransmissionTime(), 4, 2);
  }

  public void clearMessageData()
  {
    int i;
    setValidMessage(false);
    setInvalidMessageDescription("");
    setMessageAsString(null);
    setParsedMessageString("");
    setMessageData("");
    setID(0);
    setIDClassification(0);
    setTransmissionTime("000000");
    setArcTransmissionTime("000000");
    setResponseMessage(false);
    setDateTimeString();
    setRequestClassification(0);
    setSourceStationNumber("    ");
    setDestinationStationNumber("    ");
    setArc9yLocationNumber(EMPTY_LOCATION);
    setTransportDivision(1);
    setSettingClassification(1);
    setGroupNumber(0);
    setDimensionInformation(0);
    setBCData("        ");             //  8
    setWorkNumber("        ");         //  8
    setMCData("                              "); // 30
    setMCKey("        ");
    setCommandClassification(1);
    setRejectStationNumber(0);
    setArcData("      ");
    setResponseClassification(0);
    setErrorDetails("0");
    setResponseDetailsMachineId("000000");
    setResponseDetailsModelCode("00");
    setResponseDetailsMachineNumber("0000");
    setCancellationResults(0);
    setArrivalStationNumber("0000");
    setLoadInformation(0);
    setContinuationClassification(0);
    setNumberOfReports(0);
    setMachineStatusString("");
    setRetrievalDataString("");
    for(i=0;i<2;i++)
    {
      setRetrievalDataMCKey(i,EMPTY_MCKEY);
      setRetrievalDataTransportDivision(i,0);
      setRetrievalDataType(i,0);
      setRetrievalDataCompletionClassification(i,0);
      setRetrievalDataReStorageFlag(i,0);
      setRetrievalDataRetrieveCommandDetail(i,0);
      setRetrievalDataGroupNumber(i,0);
      setRetrievalDataSourceStationNumber(i,"0000");
      setRetrievalDataDestinationStationNumber(i,"0000");
      setRetrievalDataArc9yLocationNumber(i,EMPTY_LOCATION);
      setRetrievalDataShelfToShelfArc9yLocationNumber(i,EMPTY_LOCATION, "0");
      clearRetrievalDataBCData(i);             //  8
      clearRetrievalDataWorkNumber(i);         //  8
      clearRetrievalDataMCData(i);
    }
    setStartStopClassification(0);
    setRetrievalStationNumber(null);
    setInabiltyToStartReason(0);
    setCommunicationTestTextResponse("this is a test ");
    setCommunicationTestTextRequest(null);
    setOperationModeChangeStation("0000");
    setOperationModeChangeCmd(0);
    setAlternateArc9yLocation(EMPTY_LOCATION, "0");
    setLoadSizeInformation(0);
    setAlternateStationNumber("0000");
    setResponseClassification2(0);
    setMCKey2("        ");
    setCompletionMode(0);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String dataMessageToString() // ID 50
  {
    setID(50);
    setResponseMessage( false);
    dataMessageGetParsed();
    setMessageAsString(headerToString() +
             getSubString(getDestinationStationNumber(), 0, 4) +
             parsedMcKey(getMCKey()));
    return getMessageAsString();
  }
  protected void dataMessageToDataValues()
  {
    setDestinationStationNumber(getSubString(getMessageData(), 0, 4));
    setMCKey(getSubString(getMessageData(), 0, 8).trim());
    dataMessageGetParsed();
  }
  
  protected void dataMessageGetParsed()
  {
    String strng = getTwoAsciiDigits(getID()) + " - dataMessage - Station# " +
                    getDestinationStationNumber() + " mcKey: " + getMCKey();
    setParsedMessageString(strng);
  }
  
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected void message70ToDataValues()
  {
  }

/*---------------------------------------------------------------------------
   Methods to return values.
  ---------------------------------------------------------------------------*/
  private String getTransmissionTime()     { return(transmissionTime);  }
  private String getArcTransmissionTime()    { return(msArcTransmissionTime); }
  private String getDateTimeString()         { return(dataDateTimeString);  }
  public String getMCKey()                  { return(mcKey);               }
  public String getMCKey2()                 { return(mcKey2);              }
  public int getResponseClassification()    { return(responseClassification);}
  private int getResponseClassification2()    { return(responseClassification2);}
  public String getResponseDetailsModelCode() { return(responseDetailsModelCode);               }
  public String getErrorDetails()            { return(msErrorDetails);        }
  private int getRequestClassification()     { return(requestClassification); }
  public int getTransportDivision()   { return(transportDivision); }
  protected int getSettingClassification()     { return(settingClassification); }
  public int getGroupNumber()               { return(groupNumber);            }
  public String getSourceStationNumber()    { return(sourceStationNumber.substring(0,4));    }
  public String getDestinationStationNumber() { return(destinationStationNumber.substring(0,4)); }
  public int getDimensionInformation()   { return(dimensionInformation);   }
  public String getBCData()                 { return(bcData);                 }
  public String getWorkNumber()             { return(workNumber);             }
  public String getMCData()     { return(MCData);     }
  public String getMessageData()            { return(messageData);            }
  public int getCommandClassification()     { return(commandClassification);  }
  public int getRejectStationNumber()       { return(rejectStationNumber);    }
  public String getArcData()                { return(msArcData);                }
  public String getRetrievalStationNumber()    { return(retrievalStationNumber); }
  public int getLoadSizeInformation()       { return(loadSizeInformation);    }
  public String getAlternateStationNumber()    { return(alternateStationNumber); }
  public int getStartStopClassification()    { return(startStopClassification); }
  public String getOperationModeChangeStation() { return(operationModeChangeStation);      }
  public int getOperationModeChangeCmd()    { return(operationModeChangeCmd); }
  public int getCancellationResults()    { return(cancellationResults); }
  public String getResponseDetailsMachineId() { return(responseDetailsMachineId);      }
  public String getResponseDetailsMachineNumber() { return(responseDetailsMachineNumber);      }
  public String getArrivalStationNumber()    { return(arrivalStationNumber); }
  public String getTerminalOperationCompleteStationNumber()    { return(terminalOperationCompleteStationNumber); }
  public int getLoadInformation()         {   return(loadInformation);      }
  public int getContinuationClassification() { return(continuationClassification);      }
  public int getNumberOfReports()             { return(mnNumberOfReports);      }
  public int getInabiltyToStartReason()             { return(inabiltyToStartReason);      }
  public int getCompletionMode()              { return(completionMode);      }
  private boolean getResponseMessage()              { return(responseMessage);      }
  private int getIDClassification()              { return(idClassification);      }
  public String getResponseDetails() { return(responseDetails); }



  /* 
   * Get from retrieval data's class
   */

  public String getRetrievalDataMCKey(int num)
  {
    return retrievalDatas[num].mmsMcKey;
  }

  public int getRetrievalDataTransportDivision(int num)
  {
    return retrievalDatas[num].mmnTransportDivision;
  }

  public int getRetrievalDataType(int num)
  {
    return retrievalDatas[num].mmnType;
  }

  public int getRetrievalDataCompletionClassification(int num)
  {
    return retrievalDatas[num].completionClassification;
  }

  public String getRetrievalResponse(int num)
  {
    String result = null;
    switch (getRetrievalDataCompletionClassification(num))
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
      default: result = "** *UNKNOWN*: " + getRetrievalDataCompletionClassification(num) + " **";
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
      case 3: result = "** Duplicate Command (The SAME mcKey is in the ARC) **"; break;
      case 4: result = "** (Source Designated) is OUT-OF-ORDER **"; break;
      case 5: result = "** (Source Designated) is DISCONNECTED **"; break;
      case 6: result = "** ARC is OFF-LINE **"; break;
      case 7: result = "** Condition ERROR (Transport Commanded is IMPOSSIBLE to Implement) **"; break;
      case 11: result = "** Buffer FULL **"; break;
      case 99: result = "** Data ERROR **"; break;
      default: result = "** *UNKNOWN*: " + getResponseClassification() + " **";
    }
    return result;
  }

  protected int getRetrievalDataReStorageFlag(int num)
  {
    return retrievalDatas[num].mmzReStorageFlag;
  }

  protected int getRetrievalDataRetrieveCommandDetail(int num)
  {
    return retrievalDatas[num].retrieveCommandDetail;
  }

  protected int getRetrievalDataGroupNumber(int num)
  {
    return retrievalDatas[num].mmnGroupNumber;
  }

  public String getRetrievalDataSourceStationNumber(int num)
  {
    return retrievalDatas[num].mmsSourceStationNumber;
  }

  public String getRetrievalDataDestinationStationNumber(int num)
  {
    return retrievalDatas[num].mmsDestinationStationNumber;
  }

  public String getRetrievalDataBCData(int num)
  {
    return retrievalDatas[num].mmsBcData;
  }

  public String getRetrievalDataWorkNumber(int num)
  {
    return retrievalDatas[num].mmsWorkNumber;
  }

  public String getRetrievalDataMCData(int num)
  {
    return retrievalDatas[num].mmsMCData;
  }

/*
* gets for machine status
*/

  public MachineStatus getMachineStatusItem(int num)
  {
    return machineStatuses[num];
  }

  public String getMachineStatusMachineId(int num)
  {
    return machineStatuses[num].machineId;
  }

  public int getMachineStatusMachineTypeCode(int num)
  {
    return machineStatuses[num].machineTypeCode;
  }

  public String getMachineStatusMachineNumber(int num)
  {
    return machineStatuses[num].machineNumber;
  }

  public int getMachineStatusStatus(int num)
  {
    return machineStatuses[num].status;
  }

  public String getMachineStatusErrorCode(int num)
  {
    return machineStatuses[num].errorCode;
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

  protected void setMessageData(String isMessageData)
  {
    messageData = isMessageData;
  }

  private void setIDClassification (int inIDClassification)
  {
    idClassification = inIDClassification;
  }

  protected void setResponseMessage(boolean izResponseMessage)
  {
    responseMessage = izResponseMessage;
  }

  private void setArcData(String isArcData)
  {
    msArcData = isArcData;
  }

  private void setRejectStationNumber(int inRejectStationNumber)
  {
    rejectStationNumber = inRejectStationNumber;
  }

  public void setCompletionMode(int inCompletionMode)
  {
    completionMode = inCompletionMode;
  }

  protected void setMessageAsString(String isMessageAsString)
  {
    messageAsString = isMessageAsString;
  }

  private void setLoadSizeInformation(int inLoadSizeInformation)
  {
    loadSizeInformation = inLoadSizeInformation;
  }

  private void setAlternateStationNumber(String isAlternateStationNumber)
  {
    alternateStationNumber = isAlternateStationNumber;
  }

  public void setResponseClassification2(int inResponseClassification2)
  {
    responseClassification2 = inResponseClassification2;
  }

  private void setRetrievalStationNumber(String isRetrievalStationNumber)
  {
    retrievalStationNumber = isRetrievalStationNumber;
  }

  public void setInabiltyToStartReason(int inInabiltyToStartReason)
  {
    inabiltyToStartReason = inInabiltyToStartReason;
  }

  public void setResponseDetailsMachineId(String isResponseDetailsMachineId)
  {
    responseDetailsMachineId = isResponseDetailsMachineId;
  }

  public void setResponseDetailsModelCode(String isResponseDetailsModelCode)
  {
    responseDetailsModelCode = isResponseDetailsModelCode;
  }

  public void setResponseDetailsMachineNumber(String isResponseDetailsMachineNumber)
  {
    responseDetailsMachineNumber = isResponseDetailsMachineNumber;
  }

  private void setRetrievalDataString(String isRetrievalDataString)
  {
    retrievalDataString = isRetrievalDataString;
  }

  private void setCancellationResults(int inCancellationResults)
  {
    cancellationResults = inCancellationResults;
  }

  public void setArrivalStationNumber(String isArrivalStationNumber)
  {
    arrivalStationNumber = isArrivalStationNumber;
  }

  public void setLoadInformation(int inLoadInformation)
  {
    loadInformation = inLoadInformation;
  }

  public void setContinuationClassification(int inContinuationClassification)
  {
    continuationClassification = inContinuationClassification;
  }

  public void setNumberOfReports(int inNumberOfReports)
  {
    mnNumberOfReports = inNumberOfReports;
  }

  protected void setMachineStatusString(String isMachineStatusString)
  {
    msMachineStatusString = isMachineStatusString;
  }

  public void setResponseClassification(int inResponseClassification)
  {
    responseClassification = inResponseClassification;
  }

  public void setErrorDetails(String isErrorDetails)
  {
    msErrorDetails = isErrorDetails;
  }

  private void setArcTransmissionTime(String isArcTransmissionTime)
  {
    msArcTransmissionTime = isArcTransmissionTime;
  }

  public void setStartStopClassification(int inStartStopClassification)
  {
    startStopClassification = inStartStopClassification;
  }

  public void setRequestClassification(int inRequestClassification)
  {
    requestClassification = inRequestClassification;
  }

  public void setMCKey(String isMCKey)
  {
    mcKey = blankFilledString(isMCKey, ARCLOADLENGTH);
  }

  public void setMCKey2(String isMCKey2)
  {
    mcKey2 = blankFilledString(isMCKey2, ARCLOADLENGTH);
  }

  public void setOperationModeChangeCmd(int inOperationModeChangeCmd)
  {
    operationModeChangeCmd = inOperationModeChangeCmd;
  }

  public void setOperationModeChangeStation(String isOperationModeChangeStation)
  {
    operationModeChangeStation = isOperationModeChangeStation;
  }

  public void setCommandClassification(int inCommandClassification)
  {
    commandClassification = inCommandClassification;
  }

  public void setDestinationStationNumber(String isDestinationStationNumber)
  {
    destinationStationNumber = isDestinationStationNumber;
  }

  /*--------------------------------------------------------------------------*/
  // Location methods
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch Location from ARC/9Y Format WarehouseBankBayTier(1-2-3-2) 
   * to Wrx Format BankBayTier(3-3-3). 
   */
  public String getLocationNumber()
  {
    return convertLocationFromArcFormat(locationNumber);
  }
  /*--------------------------------------------------------------------------*/
  // Location methods
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch Warehouse ARC/9Y Format WarehouseBankBayTier(1-2-3-2) 
   * 
   */
  public String getWarehouse()
  {
    return convertWarehouseFromArcFormat(locationNumber);
  }
  
  
  /*--------------------------------------------------------------------------*/
  /**
   * Update Location from Wrx Format BankBayTier(3-3-3) 
   * to ARC/9Y Format WarehouseBankBayTier(1-2-3-2).
   */
  public void setLocationNumber(String isLocationNumber, String isEquipWarehouse)
  {
    locationNumber = convertLocationToArcFormat(isLocationNumber, isEquipWarehouse);
  }
  public String getArc9yLocationNumber()
  {
    return(locationNumber);
  }
  private void setArc9yLocationNumber(String isLocationNumber)
  {
    locationNumber = isLocationNumber;
  }

  /**
   * Convert a Warehouse Rx address to a 9x/9y address
   * 
   * @param isLocationNumber
   * @param isEquipWarehouse
   * @return
   */
  private String convertLocationToArcFormat(String isLocationNumber,
      String isEquipWarehouse)
  {
    // Don't change EMPTY_LOCATION
    if (isLocationNumber.equalsIgnoreCase(EMPTY_LOCATION))
    {
      return isLocationNumber;
    }
    
    if (isEquipWarehouse == null)
    {
      throw new IllegalArgumentException("Equipment warehouse can not be null");
    }
    
    String vsReturnLocation = isLocationNumber;
    if (!emulatorMessage)
    {
      vsReturnLocation = isLocationNumber.substring(0, 6)
          + isLocationNumber.substring(7);
      vsReturnLocation = isEquipWarehouse + isLocationNumber.substring(1, 6)
          + isLocationNumber.substring(7);
    }
    return vsReturnLocation;
  }

  private String convertLocationFromArcFormat(String isLocationNumber)
  {
    String vsReturnLocation = isLocationNumber;
    if (!emulatorMessage)
    {
      vsReturnLocation = "0"  + isLocationNumber.substring(1, 6) + "0" 
                       + isLocationNumber.substring(6);
    }
    return vsReturnLocation;
  }
  
  private String convertWarehouseFromArcFormat(String isLocationNumber)
  {
    return(isLocationNumber.substring(0, 1));
  }
  
  /*--------------------------------------------------------------------------*/
  // ALTERNATE Location methods
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch ALTERNATE Location from ARC/9Y Format BnkByTir to Wrx Format BnkBayTir. 
   */
  public String getAlternateLocation()
  {
    return convertLocationFromArcFormat(alternateLocation);
  }
  
 /*--------------------------------------------------------------------------*/
  /**
   * Update ALTERNATE Location from Wrx Format BnkBayTir to ARC/9Y Format BnkByTir.
   */
  private void setAlternateLocation(String isAlternateLocation, String isEquipWarehouse)
  {
    alternateLocation = convertLocationToArcFormat(isAlternateLocation, isEquipWarehouse);
  }

  public String getAlternateArc9yLocation()
  {
    return(alternateLocation);
  }
  
  private void setAlternateArc9yLocation(String isAlternateLocation, String isEquipWarehouse)
  {
    alternateLocation = convertLocationToArcFormat(isAlternateLocation, isEquipWarehouse);
  }

  public void setOperationCompleteRMNumber(String rackmasterID)
  {
    opCompleteRMNumber = rackmasterID;
  }

  /*--------------------------------------------------------------------------*/
  // RETRIEVAL Data Location methods
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch Location from ARC/9Y Format BnkByTir to Wrx Format BnkBayTir. 
   */
  public String getRetrievalDataLocationNumber(int num)
  {
    String vsLocn = retrievalDatas[num].mmsLocationNumber;
    return convertLocationFromArcFormat(vsLocn);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Update Location from Wrx Format BnkBayTir to ARC/9Y Format BnkByTir.
   */
  public void setRetrievalDataLocationNumber(int num, String isLocation, String isEquipWarehouse)
  {
    retrievalDatas[num].mmsLocationNumber = convertLocationToArcFormat(isLocation, isEquipWarehouse);
  }

  public String getRetrievalDataArc9yLocationNumber(int num)
  {
    return retrievalDatas[num].mmsLocationNumber;
  }

  public void setRetrievalDataArc9yLocationNumber(int num, String isLocation)
  {
     retrievalDatas[num].mmsLocationNumber = isLocation;
  }

  /*--------------------------------------------------------------------------*/
  // RETRIEVAL Data Shelf-To-Shelf Location methods
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch Location from ARC/9Y Format BnkByTir to Wrx Format BnkBayTir. 
   */
  public String getRetrievalDataShelfToShelfLocationNumber(int num)
  {
    String vsSToSLocation = retrievalDatas[num].mmsShelfToShelfLocationNumber;
    return convertLocationFromArcFormat(vsSToSLocation);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Update Location from Wrx Format BnkBayTir to ARC/9Y Format BnkByTir.
   */
  public void setRetrievalDataShelfToShelfLocationNumber(int num, String isLocation, String isEquipWarehouse)
  {
    retrievalDatas[num].mmsShelfToShelfLocationNumber = convertLocationToArcFormat(isLocation, isEquipWarehouse);
  }

  /**
   * Set the Shelf-To-Shelf Location Number
   *  
   * @param num
   * @param isLocation in ARC format
   */
  public void setRetrievalDataShelfToShelfLocationNumber(int num,
      String isLocation)
  {
    retrievalDatas[num].mmsShelfToShelfLocationNumber = isLocation;
  }

  public String getRetrievalDataShelfToShelfArc9yLocationNumber(int num)
  {
    return retrievalDatas[num].mmsShelfToShelfLocationNumber;
  }

  public void setRetrievalDataShelfToShelfArc9yLocationNumber(int num, String isLocation, String isEquipWarehouse)
  {
    retrievalDatas[num].mmsShelfToShelfLocationNumber = convertLocationToArcFormat(isLocation, isEquipWarehouse);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void setSourceStationNumber(String isSourceStationNumber)
  {
    sourceStationNumber = isSourceStationNumber;
  }

  public void setSettingClassification(int inSettingClassification)
  {
    settingClassification = inSettingClassification;
  }

  public void setTransportDivision(int inTransportDivision)
  {
    transportDivision = inTransportDivision;
  }

  public void setGroupNumber(int inGroupNumber)
  {
    groupNumber = inGroupNumber;
  }

  public void setDimensionInformation(int inDimensionInformation)
  {
    dimensionInformation = inDimensionInformation;
  }

  public void setBCData(String isBCData)
  {
    bcData = blankFilledString(isBCData, 8);
  }

  public void setWorkNumber(String isWorkNumber)
  {
    workNumber = isWorkNumber;
  }

  public void setMCData(String isMCData)
  {
    MCData = blankFilledString(isMCData, 30);
  }

  /* sets for the Retrieval data class can send 2 commands
  * num = which command 1 or 2 */

  public void setRetrievalDataMCKey(int num, String isMCKey)
  {
    retrievalDatas[num].mmsMcKey = blankFilledString(isMCKey, ARCLOADLENGTH);
  }

  public void setRetrievalDataTransportDivision(int num, int inTransportDivision)
  {
    retrievalDatas[num].mmnTransportDivision = inTransportDivision;
  }

  public void setRetrievalDataType(int num, int category)
  {
    retrievalDatas[num].mmnType = category;
  }

  public void setRetrievalDataCompletionClassification(int num, int completionClassification)
  {
    retrievalDatas[num].completionClassification = completionClassification;
  }

  public void setRetrievalDataReStorageFlag(int num, int reStorageFlag)
  {
    retrievalDatas[num].mmzReStorageFlag = reStorageFlag;
  }

  public void setRetrievalDataRetrieveCommandDetail(int num, int retrieveCommandDetail)
  {
    retrievalDatas[num].retrieveCommandDetail = retrieveCommandDetail;
  }

  public void setRetrievalDataGroupNumber(int num, int inGroupNumber)
  {
    retrievalDatas[num].mmnGroupNumber = inGroupNumber;
  }

  public void setRetrievalDataSourceStationNumber(int num, String isSourceStationNumber)
  {
    retrievalDatas[num].mmsSourceStationNumber = isSourceStationNumber;
  }

  public void setRetrievalDataDestinationStationNumber(int num, String isDestinationStationNumber)
  {
    retrievalDatas[num].mmsDestinationStationNumber = isDestinationStationNumber;
  }

  public void setRetrievalDataBCData(int num, String isBCData)
  {
    retrievalDatas[num].mmsBcData = blankFilledString(isBCData, 8);
  }

  public void clearRetrievalDataBCData(int num)
  {
    retrievalDatas[num].mmsBcData = "00000000"; // 8
  }

  public void setRetrievalDataWorkNumber(int num, String isWorkNumber)
  {
    retrievalDatas[num].mmsWorkNumber = isWorkNumber;
  }

  public void setRetrievalDataMCData(int num, String isMCData)
  {
    retrievalDatas[num].mmsMCData = blankFilledString(isMCData, 30);
  }

  public void setRetrievalDataMCData(int num)
  {
    retrievalDatas[num].mmsMCData = "000000000000000000000000000000";
  }
  
  public void clearRetrievalDataWorkNumber(int num)
  {
    retrievalDatas[num].mmsWorkNumber = "00000000";
  }

  public void clearRetrievalDataMCData(int num)
  {
    retrievalDatas[num].mmsMCData = "000000000000000000000000000000";
  }

  public int getMCKeyLength()
  {
    return Arc9yMessage.EMPTY_MCKEY.length();
  }

/*********************************************
 *  Sets for machine messages
 */

  public void clearMachineStatuses()
  {
    mnNumberOfReports = 0;
    for (int i = 0; i < machineStatuses.length; i++)
    {
      machineStatuses[i].machineId = "000000";
      machineStatuses[i].machineTypeCode = 0;
      machineStatuses[i].machineNumber = "0000";
      machineStatuses[i].status = 0;
      machineStatuses[i].errorCode = "0000";
    }
  }

  public void setMachineStatusesOffline()
  {
    for (int i = 0; i < mnNumberOfReports; i++)
    {
      machineStatuses[i].status = 1;
    }
  }
  public void setMachineStatusItem(int num, MachineStatus machineStatus)
  {
    machineStatuses[num].machineId = machineStatus.machineId;
    machineStatuses[num].machineTypeCode = machineStatus.machineTypeCode;
    machineStatuses[num].machineNumber = machineStatus.machineNumber;
    machineStatuses[num].status = machineStatus.status;
    machineStatuses[num].errorCode = machineStatus.errorCode;
  }

  public void setMachineStatus(int num, String machineId,
      int machineTypeCode,
      String machineNumber,
      int status,
      String errorCode)
  {
    machineStatuses[num].machineId = machineId;
    machineStatuses[num].machineTypeCode = machineTypeCode;
    machineStatuses[num].machineNumber = machineNumber;
    machineStatuses[num].status = status;
    machineStatuses[num].errorCode = errorCode;
  }

  protected void setMachineStatusMachineId(int num, String machineId)
  {
    machineStatuses[num].machineId = machineId;
  }

  protected void setMachineStatusMachineTypeCode(int num, int machineTypeCode)
  {
    machineStatuses[num].machineTypeCode = machineTypeCode;
  }

  protected void setMachineStatusMachineNumber(int num, String machineNumber)
  {
    machineStatuses[num].machineNumber = machineNumber;
  }

  protected void setMachineStatusStatus(int num, int status)
  {
    machineStatuses[num].status = status;
  }

  protected void setMachineStatusErrorCode(int num, String errorCode)
  {
    machineStatuses[num].errorCode = errorCode;
  }

  private String blankFilledString( String s, int length)
  {
    return new String(s + "                              ").substring(0, length);
  }
  
  /**
   * Parse the location for the mini-parse (short, easy human-readable text)
   * 
   * @param isAddress
   * @return
   */
  protected String getMiniParseLocation(String isAddress)
  {
    String vsBank = isAddress.substring(0, 3);
    String vsBay = isAddress.substring(3, 6);
    String vsTier = isAddress.substring(6);
    
    return vsBank + "-" + vsBay + "-" + vsTier;
  }
}