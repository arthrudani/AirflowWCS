package com.daifukuamerica.wrxj.device.agc;

/**
 * Title:        WRx 8.xx (Java)
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corporation
 * @author
 * @version 1.0
 */

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.time.SkDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AGCMOSMessage is the
 * "<b>M</b>onitoring and <b>O</b>perating Support <b>S</b>ystem"
 * message set used to communicate with a Daifuku AGC.
 *
 * @author       Stephen Kendorski
 * @version 1.0
 */
public class AGCMOSMessage extends AGCMessage
{
  private static final int MOS_NUMBER = 0;
  private static final int HEADER_LENGTH = 16;

  /*
   * Message ID's
   */
  public static final int LATCH_CLEAR = 49;

  /**
   * Use this ID to clear ALL latches in the Latch Clear message
   */
  public static final String LATCH_CLEAR_ALL_MOSID = "121000";

  /**
   *  Text classifications.
   */
  //private static final String AGC_AGC = "1";
  protected static final String AGC_MOS = "2";
  private static final String[] TEXT_CLASSIFICATIONS = {
    "*UNKNOWN-0",
    "AGC-AGC",
    "AGC-MOS"
    };
  /**
   * Division codes.
   */
  //private static final String DAIFUKU_COMMON = "0";
  private static final String AUTOMATED_WAREHOUSE = "1";
  //private static final String DISTRIBUTION_RELATED = "2";
  //private static final String CLEAN_FA_RELATED = "3";
  //private static final String AUTOMOBILE_PRODUCTION_RELATED = "4";
  //private static final String SHELF_AND_OTHER_EQUIPMENT_RELATED = "5";
  //private static final String COMPOUND = "6";
  static final String[] DIVISION_CODES = {
    "Daifuku-Common",
    "AS/RS", //"Automated-Warehouse-Related-(AS/RS)",
    "Distribution-Related-(Flow)",
    "Clean-FA-Related-(CFA)",
    "Automobile-Production-Related-(AFA)",
    "Shelf-&-Other-Equipment-Related-(Equipment)",
    "Compound-(Software)"
  };
  /**
   *  Model Codes (of destination RECEIVER).
   */
  //private static final String MC_MODEL_CODE  = "01";
  //private static final String MOS_MODEL_CODE = "02";
  public static final String AGC_MODEL_CODE = "03";
  private static final String SRM_MODEL_CODE = "11";
  public static final String CO_MODEL_CODE  = "21";
  public static final String SV_MODEL_CODE  = "99";
  /**
   * Connection Information constants.
   */
  public static final int EQUIPMENT_STATUS_NOT_NEEDED = 101;
  public static final int MESSAGE_DATA_NOT_NEEDED = 147;
  public static final int WARNING_REPORT_NOT_NEEDED = 150;
  public static final int PARK_POSITION_CHANGE_NOT_NEEDED = 153;
  public static final int TRANSFER_HISTORY_NOT_NEEDED = 184;
  /**
   * Equipment status request constants.
   */
  //private static final String ALL_EQUIPMENT_STATUS_REQUEST = AGCMOSMessage.AGC_MODEL_CODE + "000";
  private static final int EQUIPMENT_LENGTH = 6;
  /**
   * Equipment status report constants.
   */
  private static final int EQUIPMENT_TRANSPORT_DATA_REPORT_LENGTH = 139;
  private static final int EQUIPMENT_TRANSPORT_DATA_QUANTITY_REPORT_LENGTH = 9;
  private static final int EQUIPMENT_STATUS_REPORT_LENGTH = 21;
  //private static final String SYSTEM_OFFLINE_MODE = "0";
  public static final String SYSTEM_ONLINE_MODE = "1";
  //private static final String SYSTEM_SYSTEM_REMOTE_MODE = "2";
  //private static final String SYSTEM_AGC_MODE = "3";
  //private static final String SYSTEM_SYSTEM_RECOVERY_MODE = "4";
  private static final String SYSTEM_REPORT_MODE = "9";
  static final String[] SYSTEM_MODES = {
      StatusEventDataFormat.STATUS_OFFLINE,
      StatusEventDataFormat.STATUS_ONLINE,
      "Remote",
      "AGC",
      "System-Recovery",
      "*UNKNOWN*",
      "*UNKNOWN-6*",
      "*UNKNOWN-7*",
      "*UNKNOWN-8*",
      "Equipment-Status-Report"
    };

  private static final String[] RESPONSE_FLAGS = {
      "OK",
      "*NG*"
    };

  static final String[] KEY_SWITCH_MODES = {
      StatusEventDataFormat.STATUS_OFFLINE,
      StatusEventDataFormat.STATUS_ONLINE,
      "Remote",
      "Manual"
    };
  static final String[] OPERATION_STATUSES = {
      StatusEventDataFormat.STATUS_UNKNOWN,
      StatusEventDataFormat.STATUS_RUNNING,
      StatusEventDataFormat.STATUS_STOPPED,
      StatusEventDataFormat.STATUS_DISCONNECT,
      "*UNKNOWN-4*",
      "*UNKNOWN-5*",
      StatusEventDataFormat.STATUS_ERROR,
      "Power-OFF"
  };
  static final String[] COMMUNICATION_STATUSES = {
      "Not-Ready",
      "Ready"
  };
  static final String[] LEARNING_STATUSES = {
    "N/A",
    "NOT Completed",
    "Completed"
  };
  static final String[] SHUTTLE_FORK_STATUSES = {
    "Normal",
    "Fork 1 Disconnected",
    "Fork 2 Disconnected",
    "Fork 1 & 2 Disconnected"
  };
  // These get placed in status messages
  static final String[] TRANSPORT_MODES = {
    StatusEventDataFormat.STATUS_NOT_APPLICABLE,
    StatusEventDataFormat.STATUS_STORE,
    StatusEventDataFormat.STATUS_RETRIEVE,
  };
  static final String[] STARTUP_NG_CODES = {
    "N/A",
    "Communication-Error-Between-Cell-and-Equipment-(Including-Power-OFF)",
    "Main-Unit-Error",
    "Safety-Fence-Open",
    "Cell-Emergency-Stop",
    "Cell-Key-Switch-Mode",
    "Equipment-Key-Switch-Mode",
    "Fire-Shutter-Open-Limit-OFF",
    "Protusion-Error",
    "Fork-Off-Center",
    "Command-Time-Out",
    "Operation-Suspended",
    "TRV Loading",
    "**-13-**",
    "**-14-**",
    "**-15-**",
    "**-16-**",
    "**-17-**",
    "**-18-**",
    "**-19-**",
    "**-20-**",
    "**-21-**",
    "**-22-**",
    "**-23-**",
    "**-24-**",
    "**-25-**",
    "**-26-**",
    "**-27-**",
    "**-28-**",
    "**-29-**",
    "**-30-**",
    "**-31-**",
    "**-32-**",
    "**-33-**",
    "**-34-**",
    "**-35-**",
    "**-36-**",
    "**-37-**",
    "**-38-**",
    "**-39-**",
    "**-40-**",
    "**-41-**",
    "**-42-**",
    "**-43-**",
    "**-44-**",
    "**-45-**",
    "**-46-**",
    "**-47-**",
    "**-48-**",
    "**-49-**",
    "**-50-**",
    "System-Mode-Error(Offline)",
    "System-Mode-Error(Reconnecting)",
    "Equipment-Disconnected",
    "Equipment-Out-Of-Order(Abnormal)",
    "Cell-Key-Switch-Out-Of-Order(Abnormal)",
    "Requesting-Alternate-Location",
    "Alternate-Location-Not-Found",
    "**-58-**",
    "**-59-**"
  };

  public static final int DATE_TIME_REQUEST = 1;
  public static final int SET_DATE_TIME = 2;
  private static final String[] DATE_TIME_CORRECTION_CLASSIFICATIONS = {
    "*UNKNOWN-0*",
    "Date/Time-Request",
    "Set-Date/Time",
    "Date/Time-Report",
  };

  private static final int STARTUP_EQUIPMENT = 1;
  private static final int STOP_EQUIPMENT = 2;
  private static final String[] EQUIPMENT_ACTIONS = {
    "*UNKNOWN-0*",
    "Startup",
    "Stop"
  };

  private static final String[] TRANSPORT_CYCLES = {
    "No-Cycle",
    "HP-Return",
    "Pickup",
    "Unload",
    "Move",
    "Charging",
    "Learning",
    "7-*UNKNOWN*",
    "8-*UNKNOWN*",
    "Not-In-Use"
  };

  //private static final char NO_TRANSPORT_CLASSIFICATION = '0';
  private static final char STORE_TRANSPORT_CLASSIFICATION = '1';
  private static final char RETRIEVE_TRANSPORT_CLASSIFICATION = '2';
  private static final char TRANSFER_TRANSPORT_CLASSIFICATION = '3';
  private static final char LOC_LOC_TRANSPORT_CLASSIFICATION = '4';
  private static final char NEW_LOC_LOC_TRANSPORT_CLASSIFICATION = '5';     // Kang 08-21-2012

  // These get placed in status messages
  static final String[] TRANSPORT_CLASSIFICATIONS = {
    StatusEventDataFormat.STATUS_NOT_APPLICABLE,
    StatusEventDataFormat.STATUS_STORE,
    StatusEventDataFormat.STATUS_RETRIEVE,
    StatusEventDataFormat.STATUS_TRANSFER,
    StatusEventDataFormat.STATUS_LOCTOLOC,      // Kang 08-21-2012
    StatusEventDataFormat.STATUS_LOCTOLOC
  };

/*  private static final String[] TRANSPORT_TYPES = {
    "N/A",
    "Emergency-Retrieval",
    "Planned-Retrieval",
    "*UNKNOWN-3*",
    "*UNKNOWN-4*",
    "*UNKNOWN-5*",
    "*UNKNOWN-6*",
    "*UNKNOWN-7*",
    "*UNKNOWN-8*",
    "Empty-Location-Check"
  };
*/
  public static final int DISCONNECTION_CLASSIFICATION = 1;
  public static final int RECOVERY_CLASSIFICATION = 2;
  private static final String[] DISCONNECTION_RECOVERY_CLASSIFICATIONS = {
    "N/A",
    "Disconnect",
    "Recover"
  };

  public static final String DISCONNECTION_PROCESS_CLASSIFICATION_MAIN_UNIT = "0";
  //private static final String DISCONNECTION_PROCESS_CLASSIFICATION_FORK = "1";
  public static final String DISCONNECTION_PROCESS_CLASSIFICATION_NO_FORK = "2";
  private static final String[] DISCONNECTION_PROCESS_CLASSIFICATIONS = {
    "Main-Unit",
    "Fork",
    "No-Fork"
  };

  public static final String DISCONNECTION_DATA_PROCESS_CLASSIFICATION_NA = "0";
  //private static final String DISCONNECTION_DATA_PROCESS_CLASSIFICATION_SUSPEND = "1";
  //private static final String DISCONNECTION_DATA_PROCESS_CLASSIFICATION_COMPLETE = "2";
  public static final String DISCONNECTION_DATA_PROCESS_CLASSIFICATION_CANCEL = "3";
  public static final String DISCONNECTION_DATA_PROCESS_CLASSIFICATION_DELETE = "4";
  private static final String[] DISCONNECTION_DATA_PROCESS_CLASSIFICATIONS = {
    "N/A",
    "Suspend",
    "Complete",
    "Cancel",
    "Delete"
  };
  private static final String[] DISCONNECTION_RESPONSE_DATA_PROCESS_CLASSIFICATIONS = {
    "Selectable",
    "Not-Selectable",
    "N/A",
    "N/A",
    "N/A",
    "N/A",
    "N/A",
    "N/A",
    "N/A",
    "Not-In-Use"
  };

  // Change Classification for Transport Data Change Command/Response ID 41/141
  private static final int CHANGE_CLASS_DELETE = 1;
  private static final int CHANGE_CLASS_MOVE = 2;
  private static final int CHANGE_CLASS_ADD = 3;


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class Equipment
  {
    String mmsDivisionCode = AGCMOSMessage.AUTOMATED_WAREHOUSE;
    String mmsModelCode = AGCMOSMessage.AGC_MODEL_CODE;
    String machineNumber = "000";
    String mmsMachineId = mmsDivisionCode + mmsModelCode + machineNumber;

    void setFromString(String s, int offset)
    {
      mmsDivisionCode =        s.substring(offset + 0, offset + 1);
      mmsModelCode =           s.substring(offset + 1, offset + 3);
      if (s.length() == 6)
      {
        machineNumber =       s.substring(offset + 3);
        mmsMachineId =           s.substring(offset + 0);
      }
      else
      {
        machineNumber =       s.substring(offset + 3, offset + 6);
        mmsMachineId =           s.substring(offset + 0, offset + 6);
      }
    }
    String getAsString()
    {
      return (mmsDivisionCode + mmsModelCode + machineNumber);
    }
    String getParsed()
    {
      return ("ID: " + mmsMachineId +
//              "  divisionCode: " + AGCMOSMessage.DIVISION_CODES[getIntFromOneAsciiDigit(divisionCode, 0)] +
              "  Model: " + machineTypes[getIntFromTwoAsciiDigits(mmsModelCode,0)] +
              "  Machine#: " + machineNumber);
    }
    String getModelCode()
    {
      return (mmsModelCode);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class EquipmentStatusReport
  {
    String mmsMachineId = null;
    String mmsDivisionCode = null;
    String mmsModelCode = null;
    String machineNumber = null;
    String keySwitchMode = null;
    String operationStatus = "0";
    String communicationStatus = null;
    String learningStatus = null;
    String shuttleForkStatus = null;
    String transportMode = null;
    String startupNGCode = null;
    String mmsErrorCode = AGCMessage.NO_ERROR;
    String equipmentSystemMode = "5";
    int transportDataQuantity = 0;
    String reportTime = null;

  /*--------------------------------------------------------------------------*/
    void setEquipmentStatusFromString(String s, int offset)
    {
      mmsMachineId =        s.substring(offset + 0, offset + 6);
      mmsDivisionCode =     s.substring(offset + 0, offset + 1);
      mmsModelCode =        s.substring(offset + 1, offset + 3);
      machineNumber =       s.substring(offset + 3, offset + 6);
      keySwitchMode =       s.substring(offset + 6, offset + 7);
      operationStatus =     s.substring(offset + 7, offset + 8);
      communicationStatus = s.substring(offset + 8, offset + 9);
      learningStatus =      s.substring(offset + 9, offset + 10);
      shuttleForkStatus =   s.substring(offset + 10, offset + 11);
      transportMode =       s.substring(offset + 11, offset + 12);
      startupNGCode =       s.substring(offset + 12, offset + 14);
      mmsErrorCode =        s.substring(offset + 14, offset + 21);
    }

    void setSystemMode(String s)
    {
      equipmentSystemMode = s;
    }

    String getEquipmentStatusAsString()
    {
      return (mmsDivisionCode + mmsModelCode + machineNumber +  keySwitchMode +
              operationStatus + communicationStatus + learningStatus +
              shuttleForkStatus + transportMode + startupNGCode + mmsErrorCode);
    }

    String getEquipmentStatusParsed()
    {
      return ("  machineId: " + mmsMachineId +
//              "  Division: " + AGCMOSMessage.DIVISION_CODES[getIntFromOneAsciiDigit(mmsDivisionCode, 0)] +
              "  Model: " + machineTypes[getIntFromTwoAsciiDigits(mmsModelCode, 0)] +
              "  Machine: " + machineNumber +
              "  KeySwitch: " +  AGCMOSMessage.KEY_SWITCH_MODES[getIntFromOneAsciiDigit(keySwitchMode, 0)] +
              "  Operation: " + AGCMOSMessage.OPERATION_STATUSES[getIntFromOneAsciiDigit(operationStatus, 0)] +
              "  Communication: " + AGCMOSMessage.COMMUNICATION_STATUSES[getIntFromOneAsciiDigit(communicationStatus, 0)] +
              "  Learning: " + AGCMOSMessage.LEARNING_STATUSES[getIntFromOneAsciiDigit(learningStatus, 0)] +
              "  ShuttleFork: " + AGCMOSMessage.SHUTTLE_FORK_STATUSES[getIntFromOneAsciiDigit(shuttleForkStatus, 0)] +
              "  TransportMode: " + AGCMOSMessage.TRANSPORT_MODES[getIntFromOneAsciiDigit(transportMode, 0)] +
              "  StartupNGCode: " + getStartupNGCode() +
              "  ErrorCode: " + mmsErrorCode);
    }

    /**
     * Get an array of values representing the equipment status
     * @return
     */
    String[] getEquipmentStatusArray()
    {
      String statusBytes = getEquipmentStatusAsString();
      statusBytes = statusBytes.substring(6, 14);
      String vsStatusDescription = null;
      if (equipmentSystemMode.equals(AGCMOSMessage.SYSTEM_REPORT_MODE)) // "9"
      {
        //
        // This is a report of equipment (conveyors, etc).
        //
        vsStatusDescription = AGCMOSMessage.OPERATION_STATUSES[getIntFromOneAsciiDigit(operationStatus, 0)];
      }
      else
      {
        //
        // This is a report of non-equipment (software processes).
        //
        vsStatusDescription = AGCMOSMessage.SYSTEM_MODES[getIntFromOneAsciiDigit(equipmentSystemMode, 0)];
      }
      if (mmsModelCode.equals(AGCMOSMessage.SRM_MODEL_CODE))
      {
        vsStatusDescription = vsStatusDescription + "|"
            + AGCMOSMessage.KEY_SWITCH_MODES[getIntFromOneAsciiDigit(keySwitchMode, 0)];
      }
      else if ((mmsModelCode.equals(AGCMOSMessage.CO_MODEL_CODE)) &&
               (getIntFromOneAsciiDigit(transportMode, 0) != 0))
      {
        vsStatusDescription = vsStatusDescription + "|"
            + AGCMOSMessage.TRANSPORT_MODES[getIntFromOneAsciiDigit(transportMode, 0)];
      }

      if (mmsErrorCode.equals(AGCMessage.NO_ERROR))
      {
        mmsErrorCode = StatusEventDataFormat.NONE;
      }

      return new String[] { mmsMachineId,
          machineTypes[getIntFromTwoAsciiDigits(mmsModelCode, 0)],
          machineNumber, statusBytes, vsStatusDescription, mmsErrorCode,
          reportTime };
    }

    private String getStartupNGCode()
    {
      return (AGCMOSMessage.STARTUP_NG_CODES[getIntFromTwoAsciiDigits(startupNGCode, 0)]);
    }

    /*--------------------------------------------------------------------------*/
    void setEquipmentTransportDataQuantityFromString(String s, int offset)
    {
      mmsMachineId =            s.substring(offset + 0, offset + 6);
      mmsDivisionCode =         s.substring(offset + 0, offset + 1);
      mmsModelCode =            s.substring(offset + 1, offset + 3);
      machineNumber =        s.substring(offset + 3, offset + 6);
      transportDataQuantity = getIntFromThreeAsciiDigits(s, offset + 6);
    }

    String getEquipmentTransportDataQuantityAsString()
    {
      return mmsDivisionCode + mmsModelCode + machineNumber
          + getThreeAsciiDigits(transportDataQuantity);
    }

    String getEquipmentTransportDataQuantityParsed()
    {
      return "Machine: " + mmsMachineId +
             "  Quantity: " +  transportDataQuantity +
             "  Model: " + machineTypes[getIntFromTwoAsciiDigits(mmsModelCode, 0)];
    }

    int getEquipmentTransportDataQuantity()
    {
      return (transportDataQuantity);
    }

    void resetEquipmentTransportDataQuantity()
    {
      transportDataQuantity = -1;
    }

    void setTransmissionTime(String time)
    {
      reportTime = getSubString(time, 0, 2) + ":" +
                   getSubString(time, 2, 2) + ":" +
                   getSubString(time, 4, 2);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class TransportDataReport
  {
    String mmsMachineId = null;
    String dataString = null;
    String controlAgc = null;
    String statusClassification = null;
    String conveyorStatus = null;
    String cycle = null;
    String receivingConveyorNumber = null;
    String mmsMcKey = null;
    String transportClassification = null;
    String type = null;
    String restoringFlag = null;
    String instructionDetails1 = null;
    String instructionDetails2 = null;
    String instructionDetails3 = null;
    String groupNumber = null;
    String sendingStationNumber = null;
    String receivingStationNumber = null;
    String receivingStationNumber2 = null;
    String transportStationNumber = null;
    String locationNumber = null;
    String locationNumberLocToLoc = null;
    String loadSizeInformation = null;
    String bcrData = null;
    String operationNumber = null;
    String controlInformation = null;

  /*--------------------------------------------------------------------------*/
    void setTransportDataReportFromString(String s, int offset)
    {
      if (s.length() >= 139)
      {
        if (s.length() == 139)
        {
          dataString =              s.substring(offset + 0);
        }
        else
        {
          dataString =              s.substring(offset + 0, offset + 139);
        }
        controlAgc =              s.substring(offset + 0, offset + 3);
        statusClassification =    s.substring(offset + 3, offset + 4);
        conveyorStatus =          s.substring(offset + 4, offset + 8);
        cycle =                   s.substring(offset + 8, offset + 9);
        receivingConveyorNumber = s.substring(offset + 9, offset + 12);
        mmsMcKey =                   s.substring(offset + 12, offset + 20);
        transportClassification = s.substring(offset + 20, offset + 21);
        type =                    s.substring(offset + 21, offset + 22);
        restoringFlag =           s.substring(offset + 22, offset + 23);
        instructionDetails1 =     s.substring(offset + 23, offset + 24);
        instructionDetails2 =     s.substring(offset + 24, offset + 25);
        instructionDetails3 =     s.substring(offset + 25, offset + 26);
        groupNumber         =     s.substring(offset + 26, offset + 29);
        sendingStationNumber =    s.substring(offset + 29, offset + 33);
        receivingStationNumber =  s.substring(offset + 33, offset + 37);
        receivingStationNumber2 = s.substring(offset + 37, offset + 41);
        transportStationNumber =  s.substring(offset + 41, offset + 45);
        locationNumber =          s.substring(offset + 45, offset + 57);
        locationNumberLocToLoc =  s.substring(offset + 57, offset + 69);
        loadSizeInformation =     s.substring(offset + 69, offset + 71);
        bcrData =                 s.substring(offset + 71, offset + 101);
        operationNumber =         s.substring(offset + 101, offset + 109);
        if (s.length() == 139)
        {
          controlInformation =      s.substring(offset + 109);
        }
        else
        {
          controlInformation =      s.substring(offset + 109, offset + 139);
        }
      }
      else
      {
        dataString = "";
        mmsMcKey = "-";
        bcrData = "-";
        transportClassification = "0";
        loadSizeInformation = "-";
      }
    }

    String getTransportDataReportAsString()
    {
      String s = null;
      if (dataString.length() > 0)
      {
        s = controlAgc + statusClassification + conveyorStatus + cycle
            + receivingConveyorNumber + mmsMcKey + transportClassification
            + type + restoringFlag + instructionDetails1 + instructionDetails2
            + instructionDetails3 + groupNumber + sendingStationNumber
            + receivingStationNumber + receivingStationNumber2
            + transportStationNumber + locationNumber + locationNumberLocToLoc
            + loadSizeInformation + bcrData + operationNumber
            + controlInformation;
      }
      else
      {
        s = dataString;
      }
      return (s);
    }

    String getTransportDataReportParsed()
    {
      return "MCKey:" + mmsMcKey;
    }

    String[] getTransportDataReportArray()
    {
      String src = null;
      String dst = null;
      switch (transportClassification.charAt(0))
      {
        case  AGCMOSMessage.STORE_TRANSPORT_CLASSIFICATION:
          src = sendingStationNumber;
          if (!locationNumber.equals(AGCDeviceConstants.EMPTYLOCATION))
          {
            dst = locationNumber.substring(0,1) + "-" +
                  locationNumber.substring(1,3) + "-" +
                  locationNumber.substring(3,6) + "-" +
                  locationNumber.substring(6,9) + "  (Bin)";
          }
          else
          {
            dst = StatusEventDataFormat.NONE;
          }
          break;
        case  AGCMOSMessage.RETRIEVE_TRANSPORT_CLASSIFICATION:
          if (!locationNumber.equals(AGCDeviceConstants.EMPTYLOCATION))
          {
            src = locationNumber.substring(0,1) + "-" +
                  locationNumber.substring(1,3) + "-" +
                  locationNumber.substring(3,6) + "-" +
                  locationNumber.substring(6,9) + "  (Bin)";
          }
          else
          {
            src = StatusEventDataFormat.NONE;
          }
          dst = receivingStationNumber;
          break;
        case  AGCMOSMessage.TRANSFER_TRANSPORT_CLASSIFICATION:
          src = sendingStationNumber;
          dst = receivingStationNumber;
          break;
        case  AGCMOSMessage.LOC_LOC_TRANSPORT_CLASSIFICATION:
        case  AGCMOSMessage.NEW_LOC_LOC_TRANSPORT_CLASSIFICATION:       // Kang 08-21-2012
          if (!locationNumber.equals(AGCDeviceConstants.EMPTYLOCATION))
          {
            src = locationNumber.substring(0,1) + "-" +
                  locationNumber.substring(1,3) + "-" +
                  locationNumber.substring(3,6) + "-" +
                  locationNumber.substring(6,9) + "  (Bin)";
          }
          else
          {
            src = StatusEventDataFormat.NONE;
          }
          if (!locationNumberLocToLoc.equals(AGCDeviceConstants.EMPTYLOCATION))
          {
            dst = locationNumberLocToLoc.substring(0,1) + "-" +
                  locationNumberLocToLoc.substring(1,3) + "-" +
                  locationNumberLocToLoc.substring(3,6) + "-" +
                  locationNumberLocToLoc.substring(6,9) + " (Bin)";
          }
          else
          {
            dst = StatusEventDataFormat.NONE;
          }
          break;
        default:
          src = StatusEventDataFormat.STATUS_UNKNOWN;
          dst = StatusEventDataFormat.STATUS_UNKNOWN;
          break;
      }
      return new String[] {StatusEventDataFormat.NONE, mmsMachineId, mmsMcKey, bcrData,
          AGCMOSMessage.TRANSPORT_CLASSIFICATIONS[getIntFromOneAsciiDigit(transportClassification, 0)],
          src, dst, loadSizeInformation, StatusEventDataFormat.NONE };
    }

    void setMachineId(String isMachineId)
    {
      mmsMachineId = isMachineId;
    }
    String getTransportDataReportString()
    {
      return (dataString);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public class DisconnectRecoverDataReport
  {
    String mmsMachineId = null;
    String forkNumber = null;
    String suspend = null;
    String forcedCompletion = null;
    public String cancel = null;
    public String delete = null;
    String cycle = null;
    String locationNumber = null;
    String mmsMcKey = null;
    String transportClassification = null;
    String sendingStationNumber = null;
    String receivingStationNumber = null;
    String controlInformation = null;

    /*------------------------------------------------------------------------*/
    void setDisconnectRecoverDataReportFromString(String s, int offset)
    {
      if (s.length() >= 65)
      {
        forkNumber =              s.substring(offset + 0, offset + 1);
        suspend =                 s.substring(offset + 1, offset + 2);
        forcedCompletion =        s.substring(offset + 2, offset + 3);
        cancel =                  s.substring(offset + 3, offset + 4);
        delete =                  s.substring(offset + 4, offset + 5);
        cycle =                   s.substring(offset + 5, offset + 6);
        locationNumber =          s.substring(offset + 6, offset + 18);
        mmsMcKey =                s.substring(offset + 18, offset + 26);
        transportClassification = s.substring(offset + 26, offset + 27);
        sendingStationNumber =    s.substring(offset + 27, offset + 31);
        receivingStationNumber =  s.substring(offset + 31, offset + 35);
        if (s.length() == 65)
        {
          controlInformation =      s.substring(offset + 35);
        }
        else
        {
          controlInformation =      s.substring(offset + 35, offset + 65);
        }
      }
      else
      {
        mmsMcKey = "-";
        transportClassification = "0";
      }
    }

    String getDisconnectRecoverDataReportParsed()
    {
      String strng = "\nMachine " + mmsMachineId + " Fork " + forkNumber + ":";
      if (suspend.equals("0"))
        strng = strng + " Suspend Allowed,";
      else
        strng = strng + " Suspend N/A,    ";
      if (forcedCompletion.equals("0"))
        strng = strng + " ForcedCompletion Allowed,";
      else
        strng = strng + " ForcedCompletion N/A,    ";
      if (cancel.equals("0"))
        strng = strng + " Cancel Allowed,";
      else
        strng = strng + " Cancel N/A,    ";
      if (delete.equals("0"))
        strng = strng + " Delete Allowed,";
      else
        strng = strng + " Delete N/A,    ";
      int viCycle = cycle.charAt(0) - '0';
      strng = strng + " Cycle " + TRANSPORT_CYCLES[viCycle];
      strng = strng + ", MCKey \"" + mmsMcKey;
      strng = strng + "\", Locn " + locationNumber;
      strng = strng + ", Sending Stn " + sendingStationNumber;
      strng = strng + ", Dest Stn " + receivingStationNumber;
      strng = strng + ", Transport Class " + transportClassification;
      strng = strng + ", Control " + controlInformation;

      return (strng);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private class DisconnectRecoverCmdResponse
  {
    String forkNumber = null;
    String discClassification = null;
    String dataClassification = null;
    String mmsResponseFlag = null;

  /*--------------------------------------------------------------------------*/
    void setDisconnectRecoverCmdResponseFromString(String s, int offset)
    {
      if (s.length() >= 4)
      {
        forkNumber =         s.substring(offset + 0, offset + 1);
        discClassification = s.substring(offset + 1, offset + 2);
        dataClassification = s.substring(offset + 2, offset + 3);
        mmsResponseFlag =    s.substring(offset + 3, offset + 4);
      }
    }

    String getDisconnectRecoverCmdResponseParsed()
    {
      String strng = "\nFork " + forkNumber +
           " " + DISCONNECTION_RECOVERY_CLASSIFICATIONS[getIntFromOneAsciiDigit(discClassification, 0)] +
           " " + DISCONNECTION_DATA_PROCESS_CLASSIFICATIONS[getIntFromOneAsciiDigit(dataClassification, 0)] +
           " " + AGCMOSMessage.RESPONSE_FLAGS[getIntFromOneAsciiDigit(mmsResponseFlag, 0)];
      return (strng);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private static String emptyBarCode = null;

  protected String textClassification = AGCMOSMessage.AGC_MOS;
  private String divisionCode = AGCMOSMessage.AUTOMATED_WAREHOUSE;
  protected String modelCode = AGCMOSMessage.AGC_MODEL_CODE;
  protected String msgModelCode = AGCMOSMessage.AGC_MODEL_CODE;
  protected int mosNumber = AGCMOSMessage.MOS_NUMBER;
  protected int msgMosNumber = AGCMOSMessage.MOS_NUMBER;
  private String systemMode = null;
  private int responseFlag = 0;
  private List<String> unneededConnectionInformations = new ArrayList<>();
  private int communicationTestResponse = -1;
  private int dateTimeCorrectionClassification = AGCMOSMessage.SET_DATE_TIME;
  private boolean equipmentStatusChangesExist = false;
  private boolean mzTransportDataChangesExist = false;
  private int startupEquipment = 0;
  private String mcKey = null;
  private int transferTime = 0;
  private String sourceStation = null;
  private String destinationStation = null;
  private Equipment transportDataEquipment = new Equipment();

  private Map<String,EquipmentStatusReport> equipmentStatuses = new HashMap<>();
  //
  // Our Map of ALL equipment that currently has tracking (a List of TransportDataReports).
  //
  private Map<String,List<TransportDataReport>> equipmentsLoadTrackingLists = new HashMap<>();

  private List<Equipment> equipmentList = new ArrayList<>();
  private List<EquipmentStatusReport> equipmentStatusReports = new ArrayList<>();
  private List<TransportDataReport> transportDataReports = new ArrayList<>();
  protected String machineId = null;

  // For Transport Data Change Command (ID 41)
  private int changeClassification = 0;
  private int moveClassification = 0;
  private int additionalPosition = 0;

  private String errorCode = null;

  protected int disconnectionRecoveryClassification = 0;
  private int processClassification = 0;
//  private String disconnectionProcessClassification
  private String dataProcessClassification = null;
//  private String disconnectionForkNumber = null;
  private String logName;
  static final String STATUS_TIME_FORMAT = "HH:mm:ss";
  private SkDateTime statusDateTime = new SkDateTime(AGCMOSMessage.STATUS_TIME_FORMAT);
  protected List<DisconnectRecoverDataReport> disconnectRecoverDataReports = new ArrayList<>();
  private List<DisconnectRecoverCmdResponse> disconnectRecoverCmdResponses = new ArrayList<>();
  private List<String> mpDisconnectRecoverCmds = new ArrayList<>();
  private String barCode = null;
  private int impossibleReason = 0;

  static
  {
    StringBuffer vpEmptyBarCode = new StringBuffer(991);
    for (int i = 0; i < 991; i++)
    {
      vpEmptyBarCode.append(' ');
    }
    emptyBarCode = new String(vpEmptyBarCode);
  }
  /*--------------------------------------------------------------------------*/
  public AGCMOSMessage()
  {
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void setMosNumber(int inMosNumber)
  {
    mosNumber = inMosNumber;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String getModelCode()
  {
    return (msgModelCode);
  }

  public void setModelCode(String isModelCode)
  {
    modelCode = isModelCode;
  }

  public void setMsgModelCode(String isMsgModelCode)
  {
	  msgModelCode = isMsgModelCode;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public void setMCKey(String isMCKey)
  {
    if (isMCKey.length() != 8)
    {
      isMCKey = isMCKey + "         ";
      isMCKey = isMCKey.substring(0,8);
    }
    mcKey = isMCKey;
  }

  public String getMCKey()
  {
    return mcKey;
  }

  public void setDateTimeCorrectionClassification(int value)
  {
    dateTimeCorrectionClassification = value;
  }

  public void setSystemMode(String s)
  {
    systemMode = s;
  }

  public String getSystemMode()
  {
    return systemMode;
  }

  public void setMachineId(String isMachineId)
  {
    machineId = isMachineId;
  }

  public void setDisconnectionRecoveryClassification(int value)
  {
    disconnectionRecoveryClassification = value;
  }

  public int getDisconnectionRecoveryClassification()
  {
    return disconnectionRecoveryClassification;
  }

  public String getDisconnectionRecoveryClassificationParsed(int inClassification)
  {
    try
    {
      return DISCONNECTION_RECOVERY_CLASSIFICATIONS[inClassification];
    }
    catch (ArrayIndexOutOfBoundsException aioobe)
    {
      return "Unknown (" + inClassification + ") ";
    }
  }

  public void setProcessClassification(int value)
  {
    processClassification = value;
  }

  public void setDataProcessClassification(String value)
  {
    dataProcessClassification = value;
  }

//  public void setDisconnectionForkNumber(String value)
//  {
//    disconnectionForkNumber = value;
//  }

  public String getMachineId()
  {
    return machineId;
  }
  public String getErrorCode()
  {
    return errorCode;
  }

  public void setLogName(String isLogName)
  {
    logName = isLogName;
  }

  public void setBarCode(String isBarCode)
  {
    barCode = isBarCode + emptyBarCode;
    if (isBarCode.length() > 0)
    {
      barCode = barCode.substring(0, 991);
    }
  }

  public String getBarCode()
  {
    return barCode;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Parse and interpret the passed-in message text into fields defined for that
   * message type.
   *
   * @param sMessageString the message to decode
   */
  public void toDataValues(String sMessageString)
  {
    validMessage = true;
    try
    {
      textClassification = sMessageString.substring(0, 1);
      id = getIntFromThreeAsciiDigits(sMessageString, 1);
      divisionCode = sMessageString.substring(4, 4+1);
      msgModelCode = sMessageString.substring(5, 5+2);
      msgMosNumber = getIntFromThreeAsciiDigits(sMessageString, 7);
      transmissionTime = sMessageString.substring(10, 10+6);
      messageData = sMessageString.substring(AGCMOSMessage.HEADER_LENGTH);
    }
      catch (Exception e)
    {
      if (getValidMessage())
      {
        validMessage = false;
        invalidMessageDescription = "#####  IndexOutOfBoundsException -- Header \""
            + sMessageString + "\"  #####";
      }
      return;
    }
    //
    // Extract the data values from the child message's data based on the "id".
    //
    switch (id)
    {
      case   1: equipmentStatusRequestToDataValues(); break;
      case   2: transportDataQuantityRequestToDataValues(); break;
      case   3: transportDataRequestToDataValues(); break;
      case  19: connectionInformationToDataValues(); break;
      case  21: startupStopCommandToDataValues(); break;
      case  22: alarmResetCommandToDataValues(); break;
      case  23: errorResetCommandToDataValues(); break;
      case  24: disconnectionRecoveryDataRequestToDataValues(); break;
      case  25: disconnectionRecoveryCommandToDataValues(); break;
      case  34: dateAndTimeCorrectionRequestToDataValues(); break;
      case  41: transportDataChangeCommandToDataValues(); break;
      case  42: cancelDataRequestToDataValues(); break;
      case LATCH_CLEAR: latchClearCommandToDataValues(); break;
      case  51: barCodeDataCommandToDataValues(); break;
      case  61: systemModeChangeRequestToDataValues(); break;
      case  66: logDataSaveCommandToDataValues(); break;
      case  80: communicationTestRequestToDataValues(); break;
      case 101: equipmentStatusReportToDataValues(); break;
      case 102: transportDataQuantityReportToDataValues(); break;
      case 103: transportDataReportToDataValues(); break;
      case 109: detailedErrorReportToDataValues(); break;
      case 121: startupStopResponseToDataValues(); break;
      case 124: disconnectionRecoveryDataReportToDataValues(); break;
      case 125: disconnectionRecoveryCommandResponseToDataValues(); break;
      case 134: dateAndTimeReportToDataValues(); break;
      case 150: warningReportToDataValues(); break;
      case 151: barCodeDataResponseToDataValues(); break;
      case 141: transportDataChangeCommandResponseToDataValues(); break;
      case 161: systemModeChangeResponseToDataValues(); break;
      case 166: logDataSaveCommandResponseToDataValues(); break;
      case 184: transferHistoryReportToDataValues(); break;
      case 186: loadArrivalReportToDataValues(); break;
      case 180: communicationTestResponseToDataValues(); break;
      default:
        setParsedMessageString(getThreeAsciiDigits(id) + " - ** NOT Decoded **");
        break;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  protected String headerToString()
  {
    validMessage = true;
    String sid = getThreeAsciiDigits(id);
    String sMosNumber = getThreeAsciiDigits(msgMosNumber);
    String sTxTime = setTransmissionTime();
    return textClassification +
           sid +
           divisionCode +
           msgModelCode +
           sMosNumber +
           sTxTime;
  }

  private String headerGetParsed()
  {
    return "Header - textClassification: " +
          AGCMOSMessage.TEXT_CLASSIFICATIONS[getIntFromOneAsciiDigit(textClassification, 0)] +
          " MsgID: " + getThreeAsciiDigits(id) +
          "  Division: " + AGCMOSMessage.DIVISION_CODES[getIntFromOneAsciiDigit(divisionCode, 0)] +
          "  Model: " + machineTypes[getIntFromTwoAsciiDigits(msgModelCode,0)] +
          "  MOS: " + msgMosNumber +
          "  TxTime: " +
          getSubString(transmissionTime, 0, 2) + ":" +
          getSubString(transmissionTime, 2, 2) + ":" +
          getSubString(transmissionTime, 4, 2);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Message Instances.
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  // Messages sent to AGC From MOS (MOS is in this application)
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String equipmentStatusRequestToString() // ID 001
  {
    id = 1;
    textClassification = AGCMOSMessage.AGC_MOS;
//    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    Iterator<Equipment> statusRequests = equipmentList.iterator();
    while (statusRequests.hasNext())
    {
      Equipment equipment = statusRequests.next();
      messageAsString = messageAsString + equipment.getAsString();
    }
    equipmentStatusRequestGetParsed();
    return messageAsString;
  }

  private void equipmentStatusRequestToDataValues()
  {
    int requestCount = messageData.length() / AGCMOSMessage.EQUIPMENT_LENGTH;
    setEquipment(requestCount, 0);
    equipmentStatusRequestGetParsed();
  }

  private void equipmentStatusRequestGetParsed()
  {
    String strng = getThreeAsciiDigits(id)
        + " - Status Request - Request Count: " + equipmentList.size();
    Iterator<Equipment> statusRequests = equipmentList.iterator();
    while (statusRequests.hasNext())
    {
      Equipment equipment = statusRequests.next();
      strng = strng + " - " + equipment.getParsed();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  public void addEquipment(String isDivisionCode,
                           String isModelCode,
                           String isMachineNumber)
  {
    Equipment equipment = new Equipment();
    equipment.mmsDivisionCode =  isDivisionCode;
    equipment.mmsModelCode = isModelCode;
    if (isMachineNumber.length() != 3)
    {
      isMachineNumber = "000" + isMachineNumber;
      isMachineNumber = isMachineNumber.substring(0,3);
    }
    equipment.machineNumber = isMachineNumber;
    equipment.mmsMachineId = equipment.getAsString();
    equipmentList.add(equipment);
    //
    msgModelCode = isModelCode;
  }

  /*--------------------------------------------------------------------------*/
  public void addEquipment(String isModelCode, String isMachineNumber)
  {
    Equipment equipment = new Equipment();
    equipment.mmsDivisionCode =  divisionCode;
    equipment.mmsModelCode = isModelCode;
    if (isMachineNumber.length() != 3)
    {
      isMachineNumber = "000" + isMachineNumber;
      isMachineNumber = isMachineNumber.substring(0,3);
    }
    equipment.mmsMachineId = isModelCode + isMachineNumber;
    equipment.machineNumber = isMachineNumber;
    equipmentList.add(equipment);
    //
    msgModelCode = isModelCode;
  }

  /*--------------------------------------------------------------------------*/
  public void setEquipment(String isModelCode, String isMachineNumber)
  {
    equipmentList.clear();
    Equipment equipment = new Equipment();
    equipment.mmsDivisionCode =  divisionCode;
    equipment.mmsModelCode = isModelCode;
    if (isMachineNumber.length() != 3)
    {
      isMachineNumber = "000" + isMachineNumber;
      isMachineNumber = isMachineNumber.substring(0,3);
    }
    equipment.machineNumber = isMachineNumber;
    equipment.mmsMachineId = equipment.getAsString();
    equipmentList.add(equipment);
    //
    msgModelCode = isModelCode;
  }

  /*--------------------------------------------------------------------------*/
  public void setAllEquipment()
  {
    equipmentList.clear();
    Equipment equipment = new Equipment();
    equipment.mmsDivisionCode = divisionCode;
    equipment.mmsModelCode = AGCMOSMessage.AGC_MODEL_CODE;
    equipment.machineNumber = "000";
    equipmentList.add(equipment);
    //
    msgModelCode = AGCMOSMessage.AGC_MODEL_CODE;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String equipmentStatusReportToString() // ID 101
  {
    id = 101;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    messageAsString = headerToString() + systemMode;
    Iterator<EquipmentStatusReport> statusReports = equipmentStatusReports.iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport equipmentStatusReport = statusReports.next();
      messageAsString = messageAsString + equipmentStatusReport.getEquipmentStatusAsString();
    }
    equipmentStatusReportGetParsed();
    return messageAsString;
  }

  private void equipmentStatusReportToDataValues()
  {
    systemMode = messageData.substring(0, 1);
    int viReportCount = (messageData.length() - 1) / AGCMOSMessage.EQUIPMENT_STATUS_REPORT_LENGTH;
    int offset = 1;
    //
    // Clear the list where we put the newly arrived statuses.
    //
    equipmentStatusReports.clear();
    for (int i = 0; i < viReportCount; i++)
    {
      EquipmentStatusReport vpEquipmentStatusReport = new EquipmentStatusReport();
      vpEquipmentStatusReport.setEquipmentStatusFromString(messageData, offset);
      vpEquipmentStatusReport.setTransmissionTime(transmissionTime);
      vpEquipmentStatusReport.setSystemMode(systemMode);
      equipmentStatusReports.add(vpEquipmentStatusReport);
      String eKey = messageData.substring(offset, offset+6);
      String eStat = messageData.substring(offset + 6, offset + 21);
      String vsPreviousStatus = "";
      //
      EquipmentStatusReport vpEquipmentStatus = null;
      //
      // Now, put the newly arrived equipment status into our global list of
      // ALL equipment statuses that we have ever received.
      //
      if (equipmentStatuses.containsKey(eKey))
      {
        vpEquipmentStatus = equipmentStatuses.get(eKey);
        vsPreviousStatus = vpEquipmentStatus.getEquipmentStatusAsString();
        vsPreviousStatus = vsPreviousStatus.substring(6);
      }
      else
      {
        vpEquipmentStatus = new EquipmentStatusReport();
        equipmentStatuses.put(eKey, vpEquipmentStatus);
      }
      if (!vsPreviousStatus.equals(eStat))
      {
        //
        // Status has changed
        //
        vpEquipmentStatus.setEquipmentStatusFromString(messageData, offset);
        vpEquipmentStatusReport.setTransmissionTime(transmissionTime);
        equipmentStatusChangesExist = true;
      }
      offset += AGCMOSMessage.EQUIPMENT_STATUS_REPORT_LENGTH;
    }
    equipmentStatusReportGetParsed();
  }

  private void equipmentStatusReportGetParsed()
  {
    String sSysMode = null;
    int sysMd = getIntFromOneAsciiDigit(systemMode, 0);
    if (sysMd < AGCMOSMessage.SYSTEM_MODES.length)
    {
      sSysMode = AGCMOSMessage.SYSTEM_MODES[sysMd];
    }
    else
    {
      sSysMode = "**-" + systemMode + "-**";
    }
    String strng = getThreeAsciiDigits(id) + " - Status Report - System Mode: "
        + sSysMode + "  Report Count: " + equipmentStatusReports.size();
    Iterator<EquipmentStatusReport> statusReports = equipmentStatusReports.iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport equipmentStatusReport = statusReports.next();
      strng = strng + "\n" + equipmentStatusReport.getEquipmentStatusParsed();
    }
    setParsedMessageString(strng);
  }

  public String getEquipmentStatusReportMessage()
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.EQUIPMENT_STATUS);
    Iterator<EquipmentStatusReport> statusReports = equipmentStatusReports.iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport vpEquipmentStatusReport = statusReports.next();
      vpSEDF.addStatusMessage(vpEquipmentStatusReport.getEquipmentStatusArray());
    }
    return vpSEDF.createStringToSend();
  }

  public String getEquipmentStatusMessage()
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.EQUIPMENT_STATUS);
    Iterator<EquipmentStatusReport> statusReports = equipmentStatuses.values().iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport vpEquipmentStatusReport = statusReports.next();
      vpSEDF.addStatusMessage(vpEquipmentStatusReport.getEquipmentStatusArray());
    }
    return vpSEDF.createStringToSend();
  }

  /*--------------------------------------------------------------------------*/
  public void clearEquipmentStatusReports()
  {
    equipmentStatusReports.clear();
  }

  /*--------------------------------------------------------------------------*/
  public void addEquipmentStatusReport(String isDivisionCode, String isModelCode,
                                        String isMachineNumber)
  {
    EquipmentStatusReport equipmentStatusReport = new EquipmentStatusReport();
    equipmentStatusReport.mmsDivisionCode =  isDivisionCode;
    equipmentStatusReport.mmsModelCode = isModelCode;
    if (isMachineNumber.length() != 3)
    {
      isMachineNumber = "000" + isMachineNumber;
      isMachineNumber = isMachineNumber.substring(0,3);
    }
    equipmentStatusReport.mmsMachineId = isModelCode + isMachineNumber;
    equipmentStatusReport.machineNumber = isMachineNumber;
    equipmentStatusReports.add(equipmentStatusReport);
  }
  /*--------------------------------------------------------------------------*/
  public void addEquipmentStatusReport(String isModelCode, String isMachineNumber)
  {
    EquipmentStatusReport equipmentStatusReport = new EquipmentStatusReport();
    equipmentStatusReport.mmsDivisionCode =  divisionCode;
    equipmentStatusReport.mmsModelCode = isModelCode;
    if (isMachineNumber.length() != 3)
    {
      isMachineNumber = "000" + isMachineNumber;
      isMachineNumber = isMachineNumber.substring(0,3);
    }
    equipmentStatusReport.mmsMachineId = isModelCode + isMachineNumber;
    equipmentStatusReport.machineNumber = isMachineNumber;
    equipmentStatusReports.add(equipmentStatusReport);
  }

  /*--------------------------------------------------------------------------*/
  public void addEquipmentStatusReport(String statusReport)
  {
    EquipmentStatusReport equipmentStatusReport = new EquipmentStatusReport();
    equipmentStatusReport.setEquipmentStatusFromString(statusReport, 0);
    equipmentStatusReports.add(equipmentStatusReport);
  }

  public boolean equipmentStatusChanges()
  {
    boolean result = equipmentStatusChangesExist;
    equipmentStatusChangesExist = false;
    return (result);
  }


  /*--------------------------------------------------------------------------*/
  public void setEquipmentStatus()
  {
    String reportTime = statusDateTime.getCurrentDateTimeAsString();
    Iterator<EquipmentStatusReport> statusIterator = equipmentStatuses.values().iterator();
    while (statusIterator.hasNext())
    {
      EquipmentStatusReport vpEquipmentStatusReport = statusIterator.next();
      if (vpEquipmentStatusReport.equipmentSystemMode.equals(AGCMOSMessage.SYSTEM_REPORT_MODE)) // "9"
      {
        //
        // This is a report of equipment (conveyors, etc).
        //
        if (! vpEquipmentStatusReport.operationStatus.equals("0"))
        {
          equipmentStatusChangesExist = true;
          vpEquipmentStatusReport.operationStatus = "0";
          vpEquipmentStatusReport.reportTime = reportTime;
        }
      }
      else
      {
        //
        // This is a report of non-equipment (software processes).
        //
        if (! vpEquipmentStatusReport.operationStatus.equals("5"))
        {
          equipmentStatusChangesExist = true;
          vpEquipmentStatusReport.operationStatus = "5";
          vpEquipmentStatusReport.reportTime = reportTime;
        }
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void detailedErrorReportToDataValues() // ID 109
  {
    machineId = getSubString(messageData, 0, 6);
    errorCode = getSubString(messageData, 6, 7);
    detailedErrorReportGetParsed();
  }

  private void detailedErrorReportGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - Detailed Error Report - " +
                   machineId + "  Error Code: " + errorCode + "  Time: " +
                   getSubString(transmissionTime, 0, 2) + ":" +
                   getSubString(transmissionTime, 2, 2) + ":" +
                   getSubString(transmissionTime, 4, 2);
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void loadArrivalReportToDataValues() // ID 186
  {
    machineId = getSubString(messageData, 0, 6);
    errorCode = getSubString(messageData, 6, 7);
    loadArrivalReportGetParsed();
  }

  /**
   * This isn't in the spec, so I'm making some guesses based upon observations
   * at Otis.
   */
  private void  loadArrivalReportGetParsed()
  {
    String vsParsed = getThreeAsciiDigits(id) + " - Load Arrival Report";

    String vsCraneMachine = messageData.substring(0, 6);
    int vnReports = Integer.parseInt(messageData.substring(6,8));
    String vsReports = "";
    for (int i = 0; i < vnReports; i++)
    {
      String vsReportData = messageData.substring(8 + (i*21), 29 + (i*21));
      vsReports += " Fork:" + vsReportData.substring(0, 1);
      String vsMCKey = vsReportData.substring(2, 10);
      switch (vsReportData.charAt(1))
      {
        case '2':
          vsReports += " Pickup " + vsMCKey + " from ";
          break;

        case '3':
          vsReports += " Deposit " + vsMCKey + " to ";
          break;

        default:
          vsReports += " Unknown:" + vsReportData.charAt(1) + vsMCKey + " at ";
      }
      vsReports += vsReportData.substring(10, 18) + "\n";
      // Last three characters are status--seem to always be 000
    }
    vsParsed += " Machine:" + vsCraneMachine + " Reports:" + vnReports + "\n"
        + vsReports;
    setParsedMessageString(vsParsed);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String disconnectionRecoveryDataReportToString() // ID 124
  {
    id = 124;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = "02";
    msgMosNumber = 1;
    messageAsString = headerToString();
    //
    // Just used by emulator - plug in test values.
    //
//    messageAsString = messageAsString + "2" + machineId
//        + disconnectionRecoveryClassification + "99990";

//    messageAsString = messageAsString + "2" + machineId
//        + disconnectionRecoveryClassification + "99990"
//        + "11101000000000000000000000000000000                              ";

    messageAsString = messageAsString + "2" + machineId
        + disconnectionRecoveryClassification + "99990"
        + "100003013010003000B0000057290001207                              "
        + "100103014010003000B0000058290001207                              ";
    disconnectionRecoveryDataReportGetParsed();
    return messageAsString;
  }

  private void disconnectionRecoveryDataReportToDataValues()
  {
    // Don't need "Continuous Flag" - Next is machineID
    machineId = getSubString(messageData, 1, 6);
    disconnectionRecoveryClassification = getIntFromOneAsciiDigit(messageData, 7);
    dataProcessClassification = getSubString(messageData, 8, 4);
    responseFlag = getIntDigit(messageData, 12);
    disconnectRecoverDataReports.clear();
    int offset = 13;
    int disconnectRecoverDataReportLength = messageData.length() - offset;
    while (disconnectRecoverDataReportLength > 0)
    {
      DisconnectRecoverDataReport disconnectRecoverDataReport = new DisconnectRecoverDataReport();
      disconnectRecoverDataReport.setDisconnectRecoverDataReportFromString(messageData, offset);
      disconnectRecoverDataReports.add(disconnectRecoverDataReport);
      disconnectRecoverDataReportLength -= 65;
      offset += 65;
    }
    disconnectionRecoveryDataReportGetParsed();
  }

  private void  disconnectionRecoveryDataReportGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - " +
      DISCONNECTION_RECOVERY_CLASSIFICATIONS[disconnectionRecoveryClassification] +
      "DataReport " + machineId +
      "  " + DISCONNECTION_RESPONSE_DATA_PROCESS_CLASSIFICATIONS[getIntFromOneAsciiDigit(dataProcessClassification, 3)] +
      "  " + getResponseFlag();
    Iterator<DisconnectRecoverDataReport> vpDisconnectRecoverDataReports = disconnectRecoverDataReports.iterator();
    while (vpDisconnectRecoverDataReports.hasNext())
    {
      DisconnectRecoverDataReport vpDisconnectRecoverDataReport = vpDisconnectRecoverDataReports.next();
      strng = strng + vpDisconnectRecoverDataReport.getDisconnectRecoverDataReportParsed();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String disconnectionRecoveryCommandResponseToString(
      String[] iasDataItems) // ID 125
  {
    id = 125;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    //
    // Just used by emulator - plug in test values.
    //
//    messageAsString = messageAsString + machineId + "0"
//        + disconnectionRecoveryClassification + "00" + "1130";
    messageAsString = messageAsString + machineId + "0"
        + disconnectionRecoveryClassification + "00";
    disconnectRecoverCmdResponses.clear();
    for (String vsData : iasDataItems)
    {
      messageAsString += vsData + "0";
      DisconnectRecoverCmdResponse vpDRCResp = new DisconnectRecoverCmdResponse();
      vpDRCResp.setDisconnectRecoverCmdResponseFromString(vsData + "0", 0);
      disconnectRecoverCmdResponses.add(vpDRCResp);
    }
    disconnectionRecoveryCommandResponseGetParsed();
    return messageAsString;
  }

  private void disconnectionRecoveryCommandResponseToDataValues()
  {
    machineId = getSubString(messageData, 0, 6);
    processClassification = getIntFromOneAsciiDigit(messageData, 6);
    disconnectionRecoveryClassification = getIntFromOneAsciiDigit(messageData, 7);
    dataProcessClassification = getSubString(messageData, 8, 1);
    responseFlag = getIntDigit(messageData, 9);
    disconnectRecoverCmdResponses.clear();
    int offset = 10;
    int disconnectRecoverCmdResponseLength = messageData.length() - offset;
    while (disconnectRecoverCmdResponseLength > 0)
    {
      DisconnectRecoverCmdResponse vpDisconnectRecoverCmdResponse = new DisconnectRecoverCmdResponse();
      vpDisconnectRecoverCmdResponse.setDisconnectRecoverCmdResponseFromString(messageData, offset);
      disconnectRecoverCmdResponses.add(vpDisconnectRecoverCmdResponse);
      disconnectRecoverCmdResponseLength -= 4;
      offset += 4;
    }
    disconnectionRecoveryCommandResponseGetParsed();
  }

  private void  disconnectionRecoveryCommandResponseGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - " +
      DISCONNECTION_RECOVERY_CLASSIFICATIONS[disconnectionRecoveryClassification] +
      "Response " + machineId +
      "  " + DISCONNECTION_PROCESS_CLASSIFICATIONS[processClassification] +
      "  " + DISCONNECTION_DATA_PROCESS_CLASSIFICATIONS[getIntFromOneAsciiDigit(dataProcessClassification, 0)] +
      "  " + getResponseFlag();
    Iterator<DisconnectRecoverCmdResponse> vpDisconnectRecoverCmdResponses = disconnectRecoverCmdResponses.iterator();
    while (vpDisconnectRecoverCmdResponses.hasNext())
    {
      DisconnectRecoverCmdResponse vpDisconnectRecoverCmdResponse = vpDisconnectRecoverCmdResponses.next();
      strng = strng + vpDisconnectRecoverCmdResponse.getDisconnectRecoverCmdResponseParsed();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String transportDataQuantityRequestToString() // ID 002
  {
    id = 2;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    Iterator<Equipment> statusRequests = equipmentList.iterator();
    while (statusRequests.hasNext())
    {
      Equipment equipment = statusRequests.next();
      messageAsString = messageAsString + equipment.getAsString();
    }
    transportDataQuantityRequestGetParsed();
    return messageAsString;
  }

  private void transportDataQuantityRequestToDataValues()
  {
    int requestCount = messageData.length() / AGCMOSMessage.EQUIPMENT_LENGTH;
    setEquipment(requestCount, 0);
    transportDataQuantityRequestGetParsed();
  }

  private void transportDataQuantityRequestGetParsed()
  {
    String strng = getThreeAsciiDigits(id)
        + " - Transport Data Quantity Request - Request Count: "
        + equipmentList.size();
    Iterator<Equipment> statusRequests = equipmentList.iterator();
    while (statusRequests.hasNext())
    {
      Equipment equipment = statusRequests.next();
      strng = strng + " - " + equipment.getParsed();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String transportDataQuantityReportToString() // ID 102
  {
    id = 102;
    textClassification = AGCMOSMessage.AGC_MOS;
    messageAsString = headerToString();
    Iterator<EquipmentStatusReport> statusReports = equipmentStatusReports.iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport equipmentStatusReport = statusReports.next();
      messageAsString = messageAsString + equipmentStatusReport.getEquipmentTransportDataQuantityAsString();
    }
    transportDataQuantityGetParsed();
    return messageAsString;
  }

  private void transportDataQuantityReportToDataValues()
  {
    int reportCount = (messageData.length()) / AGCMOSMessage.EQUIPMENT_TRANSPORT_DATA_QUANTITY_REPORT_LENGTH;
    int offset = 0;
    equipmentStatusReports.clear();
    for (int i = 0; i < reportCount; i++)
    {
      EquipmentStatusReport equipmentStatusReport = new EquipmentStatusReport();
      equipmentStatusReport.setEquipmentTransportDataQuantityFromString(messageData, offset);
      equipmentStatusReports.add(equipmentStatusReport);
      //
      String eKey = messageData.substring(offset, offset+6);
      int currentCount = equipmentStatusReport.getEquipmentTransportDataQuantity();
      int previousCount = -1;
      EquipmentStatusReport equipmentStatus = null;
      if (equipmentStatuses.containsKey(eKey))
      {
        equipmentStatus = equipmentStatuses.get(eKey);
        previousCount = equipmentStatus.getEquipmentTransportDataQuantity();
      }
      else
      {
        equipmentStatus = new EquipmentStatusReport();
        equipmentStatuses.put(eKey, equipmentStatus);
      }
      if (previousCount != currentCount)
      {
        //
        // Transport Data/Tracking Count has changed.
        //
        equipmentStatus.setEquipmentTransportDataQuantityFromString(messageData, offset);
        if ((currentCount == 0) && (previousCount == 0))
        {
          //
          // Transport count was zero and is still zero, delete it from our
          // Map of ALL equipment that currently has tracking.
          //
          equipmentsLoadTrackingLists.remove(eKey);
        }
        else
        {
          if (!equipmentsLoadTrackingLists.containsKey(eKey))
          {
            //
            // This key is NOT in our Map of ALL equipment that currently has
            // tracking - add key with a null value List of TransportDataReports.
            //
            equipmentsLoadTrackingLists.put(eKey, null);
          }
          else
          {
            if (currentCount == 0)
            {
              //
              // This key IS in our Map of ALL equipment that currently has
              // tracking and its tracking list count is now zero - set a null
              // value List of TransportDataReports.
              //
              equipmentsLoadTrackingLists.put(eKey, null);
              equipmentStatusChangesExist = true;
              mzTransportDataChangesExist = true;
            }
          }
        }
      }
      offset += AGCMOSMessage.EQUIPMENT_TRANSPORT_DATA_QUANTITY_REPORT_LENGTH;
    }
    transportDataQuantityGetParsed();
  }

  private void transportDataQuantityGetParsed()
  {
    String vsParsed = getThreeAsciiDigits(id) + " - Transport Data Quantity - "
        + equipmentStatusReports.size() + ": ";
    Iterator<EquipmentStatusReport> statusReports = equipmentStatusReports.iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport equipmentStatusReport = statusReports.next();
      vsParsed += "\n" + equipmentStatusReport.getEquipmentTransportDataQuantityParsed();
    }
    setParsedMessageString(vsParsed);
  }

  public boolean transportDataChanges()
  {
    boolean result = mzTransportDataChangesExist;
    mzTransportDataChangesExist = false;
    return (result);
  }

  public Iterator<String> getEquipmentTransportKeys()
  {
    return (equipmentsLoadTrackingLists.keySet().iterator());
  }

  public void clearEquipmentsLoadTrackingLists()
  {
    equipmentsLoadTrackingLists.clear();
    mzTransportDataChangesExist = true;
  }

  public Iterator<TransportDataReport> getEquipmentTransportData()
  {
    return (transportDataReports.iterator());
  }

  public void resetEquipmentTransportDataQuantities()
  {
    Iterator<EquipmentStatusReport> statusReports = equipmentStatusReports.iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport equipmentStatusReport = statusReports.next();
      equipmentStatusReport.resetEquipmentTransportDataQuantity();
    }
    statusReports = equipmentStatuses.values().iterator();
    while (statusReports.hasNext())
    {
      EquipmentStatusReport equipmentStatusReport = statusReports.next();
      equipmentStatusReport.resetEquipmentTransportDataQuantity();
    }
  }

  /**
   * Helper method for the emulator
   *
   * @param isEquipment
   * @param inTransport
   */
  public void addEquipmentTransportDataQuantities(String isEquipment,
      int inTransport)
  {
    EquipmentStatusReport equipmentStatusReport = new EquipmentStatusReport();
    equipmentStatusReport.setEquipmentTransportDataQuantityFromString(
        isEquipment + getThreeAsciiDigits(inTransport), 0);
    equipmentStatusReports.add(equipmentStatusReport);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String transportDataRequestToString() // ID 003
  {
    id = 3;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = transportDataEquipment.getModelCode();
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + transportDataEquipment.getAsString();
    transportDataRequestGetParsed();
    return messageAsString;
  }

  private void transportDataRequestToDataValues()
  {
    transportDataEquipment.setFromString(messageData, 0);
    transportDataRequestGetParsed();
  }

  private void transportDataRequestGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id)
        + " - Transport Data Request - " + transportDataEquipment.getParsed());
  }

  public void setTransportDataEquipment(String s)
  {
    transportDataEquipment.setFromString(s, 0);
  }

  public String getTransportDataEquipment()
  {
    return transportDataEquipment.getAsString();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String transportDataReportToString() // ID 103
  {
    id = 103;
    textClassification = AGCMOSMessage.AGC_MOS;
    messageAsString = headerToString();
    messageAsString += transportDataEquipment.getAsString();
    messageAsString += getTwoAsciiDigits(transportDataReports.size());
    Iterator<TransportDataReport> transportReports = transportDataReports.iterator();
    while (transportReports.hasNext())
    {
      TransportDataReport transportDataReport = transportReports.next();
      messageAsString = messageAsString + transportDataReport.getTransportDataReportAsString();
    }
    transportDataReportGetParsed();
    return messageAsString;
  }

  private void transportDataReportToDataValues()
  {
    transportDataEquipment.setFromString(messageData, 0);
    String eKey = messageData.substring(0, 6);
    //
    List<TransportDataReport> currentTransportStatuses = null;
    List<String> currentTransportStatusStrings = new ArrayList<>();
    List<TransportDataReport> newTransportStatuses = new ArrayList<>();
    List<String> newTransportStatusStrings = new ArrayList<>();
    //
    if (equipmentsLoadTrackingLists.containsKey(eKey))
    {
      //
      // Our Map of ALL equipment that currently has tracking DOES contain
      // this equipment.
      //
      if (equipmentsLoadTrackingLists.get(eKey) != null)
      {
        //
        // This Equipment's Load Tracking ALREADY has a List of TransportDataReports.
        // Get it.
        //
        currentTransportStatuses = equipmentsLoadTrackingLists.get(eKey);
      }
      else
      {
        //
        // This Equipment's Load Tracking does NOT contain a List of
        // TransportDataReports. Add it.
        //
        currentTransportStatuses = new ArrayList<>();
        equipmentsLoadTrackingLists.put(eKey, currentTransportStatuses);
      }
    }
    else
    {
      //
      // Our Map of ALL equipment that currently has tracking does NOT contain
      // this equipment. Add the key with a List of TransportDataReports.
      //
      currentTransportStatuses = new ArrayList<>();
      equipmentsLoadTrackingLists.put(eKey, currentTransportStatuses);
    }
    //
    int reportCount = getIntFromTwoAsciiDigits(messageData, 6);
    int offset = 8;
    //
    // Clear the list where we put the newly arrived load tracking.
    //
    transportDataReports.clear();
    int i = 1;
    //
    // Now, add all load tracking for the reporting piece of equipment.  We may
    // have a reportCount of zero, but we still generate a transportDataReport
    // that will contain a move type of "N/A".  This will clear any displayed
    // tracking if there are no valid transportDataReports.
    //
    do
    {
      TransportDataReport transportDataReport = new TransportDataReport();
      transportDataReport.setTransportDataReportFromString(messageData, offset);
      transportDataReport.setMachineId(eKey);
      transportDataReports.add(transportDataReport);
      newTransportStatuses.add(transportDataReport);
      newTransportStatusStrings.add(transportDataReport.getTransportDataReportString());
      offset += AGCMOSMessage.EQUIPMENT_TRANSPORT_DATA_REPORT_LENGTH;
      i++;
    } while (i <= reportCount);
    //
    // Now, put the newly arrived tracking (List of TransportDataReports)
    // from this piece of equipment into our global list of ALL equipment
    // tracking that we have ever received since we started status polling.
    //
    equipmentsLoadTrackingLists.put(eKey, newTransportStatuses);
    //
    Iterator<TransportDataReport> currentTransportStatusesIterator = currentTransportStatuses.iterator();
    while (currentTransportStatusesIterator.hasNext())
    {
      TransportDataReport currentTransportDataReport = currentTransportStatusesIterator.next();
      currentTransportStatusStrings.add(currentTransportDataReport.getTransportDataReportString());
    }
    //
    // Now compare the newly arrived tracking list for this piece of equipment
    // with the previous tracking to see if there are any changes that we need
    // to report to our device.
    //
    boolean changesExist = !((newTransportStatusStrings.containsAll(currentTransportStatusStrings)) &&
                                (currentTransportStatusStrings.containsAll(newTransportStatusStrings)));
    mzTransportDataChangesExist = (changesExist || mzTransportDataChangesExist);

    // Every so often, publish anyway, just in case
    mnTransportCount++;
    if (mnTransportCount == 10)
    {
      mnTransportCount = 0;
      mzTransportDataChangesExist = true;
    }

    transportDataReportGetParsed();
  }

  int mnTransportCount = 0;

  private void transportDataReportGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - transportDataReport - " +
                        transportDataReports.size();
    Iterator<TransportDataReport> statusReports = transportDataReports.iterator();
    while (statusReports.hasNext())
    {
      TransportDataReport transportDataReport = statusReports.next();
      strng = strng + "\n" + transportDataReport.getTransportDataReportParsed();
    }
    setParsedMessageString(strng);
  }

  /**
   * Get the status report for the tracking messages we have received
   *
   * @return
   */
  public String getStatusMessageForTransportDataReport()
  {
    StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
    vpSEDF.setType(ControllerConsts.TRACKING_STATUS);

    String[] vasReport = null;
    Iterator<List<TransportDataReport>> equipmentTrackingIterator = equipmentsLoadTrackingLists.values().iterator();
    while (equipmentTrackingIterator.hasNext())
    {
      List<TransportDataReport> trackingList = equipmentTrackingIterator.next();
      if (trackingList != null)
      {
        Iterator<TransportDataReport> trackingListIterator = trackingList.iterator();
        while (trackingListIterator.hasNext())
        {
          boolean vzAddMe = true;
          TransportDataReport transportDataReport = trackingListIterator.next();
          vasReport = transportDataReport.getTransportDataReportArray();
          //
          // Count up the number of "N/A" entries which show that a tracking
          // report count has reported twice as zero (no more changes or records).
          //
          for (String s : vasReport)
          {
            if (s.equals(StatusEventDataFormat.STATUS_NOT_APPLICABLE))
            {
              vzAddMe = false;
              break;
            }
          }
          if (vzAddMe)
          {
            vpSEDF.addTrackingMessage(vasReport[0], vasReport[1], vasReport[2],
                vasReport[3], vasReport[4], vasReport[5], vasReport[6],
                vasReport[7], vasReport[8]);
          }
        }
      }
    }

    return vpSEDF.createStringToSend();
  }

  /**
   * Helper method for the emulator
   */
  public void addTransportDataReport(String isControlAgc,
      String statusClassification, String conveyorStatus, String cycle,
      String receivingConveyorNumber, String mmsMcKey,
      String transportClassification, String type, String restoringFlag,
      String instructionDetails1, String instructionDetails2,
      String instructionDetails3, String groupNumber,
      String sendingStationNumber, String receivingStationNumber,
      String receivingStationNumber2, String transportStationNumber,
      String locationNumber, String locationNumberLocToLoc,
      String loadSizeInformation, String bcrData, String operationNumber,
      String controlInformation)
  {
    TransportDataReport vpTDR = new TransportDataReport();
    vpTDR.setTransportDataReportFromString(isControlAgc + statusClassification
        + conveyorStatus + cycle + receivingConveyorNumber + mmsMcKey
        + transportClassification + type + restoringFlag + instructionDetails1
        + instructionDetails2 + instructionDetails3 + groupNumber
        + sendingStationNumber + receivingStationNumber
        + receivingStationNumber2 + transportStationNumber + locationNumber
        + locationNumberLocToLoc + loadSizeInformation + bcrData
        + operationNumber + controlInformation, 0);
    transportDataReports.add(vpTDR);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String connectionInformationToString() // ID 019
  {
    id = 19;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    int count = unneededConnectionInformations.size();
    String sCount = getTwoAsciiDigits(count);
    messageAsString = messageAsString + sCount;
    //
    Iterator<String> unneededConnectionInformation = unneededConnectionInformations.iterator();
    while (unneededConnectionInformation.hasNext())
    {
      String s = unneededConnectionInformation.next();
      messageAsString = messageAsString +  s;
    }
    connectionInformationGetParsed();
    return messageAsString;
  }

  private void connectionInformationToDataValues()
  {
    unneededConnectionInformations.clear();
    if (messageData.length() > 0)
    {
      int count = getIntFromTwoAsciiDigits(messageData, 0);
      int offset = 2;
      for (int i = 0; i < count; i++)
      {
        unneededConnectionInformations.add(messageData.substring(offset, offset + 3));
        offset += 3;
      }
    }
    connectionInformationGetParsed();
  }

  private void connectionInformationGetParsed()
  {
    String strng = getThreeAsciiDigits(id)
        + " - Connection Information - count: "
        + unneededConnectionInformations.size();
    Iterator<String> unneededConnectionInformation = unneededConnectionInformations.iterator();
    while (unneededConnectionInformation.hasNext())
    {
      String s = unneededConnectionInformation.next();
      int unneeded = getIntFromThreeAsciiDigits(s, 0);
      switch (unneeded)
      {
        case AGCMOSMessage.EQUIPMENT_STATUS_NOT_NEEDED: s = "  Equipment-Status/101"; break;
        case AGCMOSMessage.MESSAGE_DATA_NOT_NEEDED: s = "  Message-Status/147"; break;
        case AGCMOSMessage.WARNING_REPORT_NOT_NEEDED: s = "  Warning-Report/150"; break;
        case AGCMOSMessage.PARK_POSITION_CHANGE_NOT_NEEDED: s = "  Park-Position-Change/153"; break;
        case AGCMOSMessage.TRANSFER_HISTORY_NOT_NEEDED: s = "  Transfer-History/184"; break;
        default: s = "  *UNKNOWN-" + s + "*"; break;
      }
      strng = strng + s;
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  public void addUnneededConnectionInformation(int unneededCode)
  {
    String s = getThreeAsciiDigits(unneededCode);
    unneededConnectionInformations.add(s);
  }
  /*--------------------------------------------------------------------------*/
  public void setNeedAllConnectionInformation()
  {
    unneededConnectionInformations.clear();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String startupStopCommandToString() // ID 021
  {
    id = 21;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    String sStartup = AGCMessage.ASCII_DIGITS[startupEquipment];
    messageAsString = messageAsString + sStartup;
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    while (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      messageAsString = messageAsString + equipment.getAsString();
    }
    startupStopCommandGetParsed();
    return messageAsString;
  }

  /**
   * Start/Stop command for one piece of equipment
   *
   * @param isMOSID
   * @return
   */
  public String startupStopCommandToString(String isMOSID) // ID 021
  {
    id = 21;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    String sStartup = AGCMessage.ASCII_DIGITS[startupEquipment];
    messageAsString += sStartup;
    messageAsString += isMOSID;
    startupStopCommandGetParsed(isMOSID);
    return messageAsString;
  }

  private void startupStopCommandToDataValues()
  {
    startupEquipment = getIntFromOneAsciiDigit(messageData, 0);
    int count = (messageData.length() - 1) / AGCMOSMessage.EQUIPMENT_LENGTH;
    setEquipment(count, 1);
    startupStopCommandGetParsed();
  }

  private void setEquipment(int inCount, int inOffset)
  {
    equipmentList.clear();
    for (int i = 0; i < inCount; i++)
    {
      Equipment equipment = new Equipment();
      equipment.setFromString(messageData, inOffset);
      equipmentList.add(equipment);
      inOffset += AGCMOSMessage.EQUIPMENT_LENGTH;
    }
  }

  /**
   * Get the specified equipment foror the emulator.
   *
   * @return
   */
  public String[] getEquipment()
  {
    String[] vasEq = new String[equipmentList.size()];
    for (int i = 0; i < vasEq.length; i++)
    {
      vasEq[i] = equipmentList.get(i).getAsString();
    }
    return vasEq;
  }

  private void startupStopCommandGetParsed()
  {
    String sStartup = AGCMOSMessage.EQUIPMENT_ACTIONS[startupEquipment];
    String vsParsed = getThreeAsciiDigits(id) + " - " + sStartup
        + MINI_PARSE_DIVIDER;

    vsParsed += getThreeAsciiDigits(id) + " - Startup/Stop Command: "
        + sStartup + "  Count: " + equipmentList.size();
    for (Equipment e : equipmentList)
    {
      String vsEq = e.getParsed();
      vsParsed += " - " + vsEq;
    }
    setParsedMessageString(vsParsed);
  }

  /**
   * I wouldn't have to do this if we actually parsed the message
   * @param isMOSID
   */
  private void startupStopCommandGetParsed(String isMOSID)
  {
    String sStartup = AGCMOSMessage.EQUIPMENT_ACTIONS[startupEquipment];

    Equipment e = new Equipment();
    e.setFromString(isMOSID, 0);
    String vsEquipment = e.getParsed();

    String vsParsed = getThreeAsciiDigits(id) + " - " + sStartup + " " + isMOSID
        + MINI_PARSE_DIVIDER;

    vsParsed += getThreeAsciiDigits(id) + " - Startup/Stop Command: "
        + sStartup + "  Count: 1 - " + vsEquipment;
    setParsedMessageString(vsParsed);
  }

  public boolean isStartCommand()
  {
    return startupEquipment == STARTUP_EQUIPMENT;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String alarmResetCommandToString() // ID 022
  {
    id = 22;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    while (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      messageAsString = messageAsString + equipment.getAsString();
    }
    alarmResetCommandGetParsed();
    return messageAsString;
  }

  private void alarmResetCommandToDataValues()
  {
    int count = messageData.length() / AGCMOSMessage.EQUIPMENT_LENGTH;
    setEquipment(count, 0);
    alarmResetCommandGetParsed();
  }

  private void alarmResetCommandGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - alarmResetCommand - count: " + equipmentList.size();
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    while (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      strng = strng + " - " + equipment.getParsed();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String disconnectionRecoveryDataRequestToString() // ID 024
  {
    id = 24;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + machineId + disconnectionRecoveryClassification;
    disconnectionRecoveryDataRequestGetParsed();
    return messageAsString;
  }

  private void disconnectionRecoveryDataRequestToDataValues()
  {
    machineId = getSubString(messageData, 0, 6);
    disconnectionRecoveryClassification = getIntFromOneAsciiDigit(messageData, 6);
    //
    equipmentList.clear();
    Equipment equipment = new Equipment();
    equipment.setFromString(messageData, 0);
    equipmentList.add(equipment);
    disconnectionRecoveryDataRequestGetParsed();
  }

  private void disconnectionRecoveryDataRequestGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id) + " - "
        + DISCONNECTION_RECOVERY_CLASSIFICATIONS[disconnectionRecoveryClassification]
        + "DataRequest - " + machineId);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String disconnectionRecoveryCommandToString() // ID 025
  {
    id = 25;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + machineId + "0" +
                      disconnectionRecoveryClassification + "0";
    //
    // Use Disconnect/Recover Data Reports (124) to fill our message.
    //
    boolean noData = true;
    for (DisconnectRecoverDataReport vpDRDReport : disconnectRecoverDataReports)
    {
      if (vpDRDReport.cancel.equals("0"))
      {
        //
        // This data can be canceled.
        //
        messageAsString = messageAsString + vpDRDReport.forkNumber + "13";
        noData = false;
      }
      else if (vpDRDReport.delete.equals("0"))
      {
        //
        // This data can be deleted.
        //
        messageAsString = messageAsString + vpDRDReport.forkNumber + "14";
        noData = false;
      }
    }
    if (noData)
    {
      messageAsString = messageAsString + "000";
    }
    disconnectionRecoveryCommandGetParsed(messageAsString.substring(25).trim());
    return messageAsString;
  }

  private void disconnectionRecoveryCommandToDataValues()
  {
    equipmentList.clear();
    Equipment equipment = new Equipment();
    equipment.setFromString(messageData, 0);
    equipmentList.add(equipment);
    //
    processClassification = getIntFromOneAsciiDigit(messageData, 6);
    disconnectionRecoveryClassification = getIntFromOneAsciiDigit(messageData, 7);
    dataProcessClassification = getSubString(messageData, 8, 1);
    disconnectionRecoveryCommandGetParsed(messageData.substring(9).trim());
  }

  /**
   * Parse the Disconnection / Recovery Command (025)
   * @param vs25Data - Data items
   */
  protected void disconnectionRecoveryCommandGetParsed(String vs25Data)
  {
    String strng = getThreeAsciiDigits(id) + " - " +
      DISCONNECTION_RECOVERY_CLASSIFICATIONS[disconnectionRecoveryClassification] +
      "Cmd - " +
      DISCONNECTION_PROCESS_CLASSIFICATIONS[processClassification] +
      "  " + DISCONNECTION_RECOVERY_CLASSIFICATIONS[disconnectionRecoveryClassification] +
      "  " + DISCONNECTION_DATA_PROCESS_CLASSIFICATIONS[getIntFromOneAsciiDigit(dataProcessClassification, 0)];

    mpDisconnectRecoverCmds.clear();
//    boolean noData = true;
    while (vs25Data.length() >= 3)
    {
      mpDisconnectRecoverCmds.add(vs25Data.substring(0, 3));

      // Each data item is 3 bytes: fork, disconnection/recovery classification,
      // and data processing classification
      int vnFork   = getIntFromOneAsciiDigit(vs25Data, 0);
      int vnClass1 = getIntFromOneAsciiDigit(vs25Data, 1);
      int vnClass2 = getIntFromOneAsciiDigit(vs25Data, 2);

      // Get the parsed data
      strng = strng + " \nFork " + vnFork + " "
          + DISCONNECTION_RECOVERY_CLASSIFICATIONS[vnClass1] + " "
          + DISCONNECTION_DATA_PROCESS_CLASSIFICATIONS[vnClass2];
//      noData = false;

      // If there are more data items, get ready to process them
      if (vs25Data.length() > 3)
      {
        vs25Data = vs25Data.substring(3);
      }
      else
      {
        vs25Data = "";
      }
    }
//    if (noData)
//    {
//      messageAsString = messageAsString + "Main N/A N/A";
//    }
    setParsedMessageString(strng);
  }

  /**
   * Get the Disconnect / Recover Commands from the 025 message.  Only valid
   * AFTER parsing the 025 message.  Someday I'll re-write this whole class so
   * it makes more sense.
   *
   * @return
   */
  public String[] getDisconnectRecoverCmds()
  {
    return mpDisconnectRecoverCmds.toArray(new String[0]);
  }

  /*------------------------------------------------------------------------*/
  /* Latch clear command (ID=49)                                            */
  /*------------------------------------------------------------------------*/
  public String latchClearCommandToString() // ID 049
  {
    id = LATCH_CLEAR;
    textClassification = AGCMOSMessage.AGC_MOS;
    setModelCode("21"); // Always "21"
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString() + machineId;
    latchClearCommandGetParsed();
    return messageAsString;
  }

  private void latchClearCommandToDataValues()
  {
    latchClearCommandGetParsed();
  }

  private void latchClearCommandGetParsed()
  {
    String vsMachineID = machineId;
    if (vsMachineID.equals(LATCH_CLEAR_ALL_MOSID))
    {
      vsMachineID = "ALL";
    }
    setParsedMessageString(getThreeAsciiDigits(id) + " - " +
      "Latch Clear Command - " + vsMachineID);
  }

  /*------------------------------------------------------------------------*/
  /* External input data command (ID=51)                                    */
  /*------------------------------------------------------------------------*/
  public String barCodeDataCommandToString() // ID 051
  {
    id = 51;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    String vsBarCodeLength = "" + barCode.trim().length();
    while (vsBarCodeLength.length() < 3)
    {
      vsBarCodeLength = "0" + vsBarCodeLength;
    }
    messageAsString = messageAsString + machineId + vsBarCodeLength + barCode;
    barCodeDataCommandGetParsed();
    return messageAsString;
  }

  private void barCodeDataCommandToDataValues()
  {
    machineId = getSubString(messageData, 0, 6);
    int vnBarCodeLength = getIntFromTwoAsciiDigits(messageData, 6);
    barCode = getSubString(messageData, 9, 9 + vnBarCodeLength);
    barCode = barCode.trim();
    barCodeDataCommandGetParsed();
  }

  private void barCodeDataCommandGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id) + " - " +
      "BarCodeDataCmd - " + barCode + " - " + machineId);
  }

  /*------------------------------------------------------------------------*/
  /*  Bar code data response (151)                                          */
  /*------------------------------------------------------------------------*/
  public String barCodeDataResponseToString() // ID 151
  {
    id = 151;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString() + getMachineId() + "000"; // Just plug-in OK values for emulator
    barCodeDataResponseGetParsed();
    return messageAsString;
  }

  private void barCodeDataResponseToDataValues()
  {
    machineId = getSubString(messageData, 0, 6);
    //
    responseFlag = getIntDigit(messageData, 6);
    impossibleReason = getIntFromTwoAsciiDigits(messageData, 7);
    barCodeDataResponseGetParsed();
  }

  private void barCodeDataResponseGetParsed()
  {
    String strng = getThreeAsciiDigits(id) +
      " - BarCodeDataResponse - " + RESPONSE_FLAGS[responseFlag];
    if (responseFlag != 0)
    {
      switch (responseFlag)
      {
        case 1:
          strng = strng + " Impossible";
          break;
        case 2:
          strng = strng + " Figure-Number-Mismatch";
          break;
        default:
          strng = strng + " UNKNOWN-" + responseFlag;
          break;
      }
    }
    setParsedMessageString(strng + " (Reason: " + impossibleReason + ")");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String startupStopResponseToString() // ID 121
  {
    id = 121;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    String sStartup = AGCMessage.ASCII_DIGITS[startupEquipment];
    messageAsString = messageAsString + sStartup;
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    while (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      messageAsString = messageAsString + equipment.getAsString();
    }
    startupStopCommandGetParsed();
    return messageAsString;
  }

  private void startupStopResponseToDataValues()
  {
    startupEquipment = getIntFromOneAsciiDigit(messageData, 0);
    int count = (messageData.length() - 1) / AGCMOSMessage.EQUIPMENT_LENGTH;
    setEquipment(count, 1);
    startupStopCommandGetParsed();
  }

  /*--------------------------------------------------------------------------*/
  public void clearEquipmentList()
  {
    equipmentList.clear();
  }
  /*--------------------------------------------------------------------------*/
  public void addEquipmentStartupStop(String isDivisionCode,
                                        String isModelCode,
                                        String isMachineNumber)
  {
    Equipment equipment = new Equipment();
    equipment.mmsDivisionCode =  isDivisionCode;
    equipment.mmsModelCode = isModelCode;
    if (isMachineNumber.length() != 3)
    {
      isMachineNumber = "000" + isMachineNumber;
      isMachineNumber = isMachineNumber.substring(0,3);
    }
    equipment.mmsMachineId = isModelCode + isMachineNumber;
    equipment.machineNumber = isMachineNumber;
    equipmentList.add(equipment);
  }
  /*--------------------------------------------------------------------------*/
  public void addEquipmentStartupStop(String isModelCode, String isMachineNumber)
  {
    Equipment equipment = new Equipment();
    equipment.mmsDivisionCode =  divisionCode;
    equipment.mmsModelCode = isModelCode;
    if (isMachineNumber.length() != 3)
    {
      isMachineNumber = "000" + isMachineNumber;
      isMachineNumber = isMachineNumber.substring(0,3);
    }
    equipment.mmsMachineId = isModelCode + isMachineNumber;
    equipment.machineNumber = isMachineNumber;
    equipmentList.add(equipment);
  }
  /*--------------------------------------------------------------------------*/
  public void setStartupStopAllEquipment(boolean ibStart)
  {
    if (ibStart)
    {
      startupEquipment = STARTUP_EQUIPMENT;
    }
    else
    {
      startupEquipment = STOP_EQUIPMENT;
    }
    equipmentList.clear();
    Equipment equipment = new Equipment();
    equipment.mmsDivisionCode = divisionCode;
    equipment.mmsModelCode = AGCMOSMessage.SV_MODEL_CODE; //AGC_MODEL_CODE;
    equipment.machineNumber = "000";
    equipment.mmsMachineId = divisionCode + AGCMOSMessage.SV_MODEL_CODE
        + equipment.machineNumber;
    equipmentList.add(equipment);
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String errorResetCommandToString() // ID 023
  {
    id = 23;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    while (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      messageAsString = messageAsString + equipment.getAsString();
    }
    errorResetCommandGetParsed();
    return messageAsString;
  }

  private void errorResetCommandToDataValues()
  {
    int count = messageData.length() / AGCMOSMessage.EQUIPMENT_LENGTH;
    setEquipment(count, 0);
    errorResetCommandGetParsed();
  }

  private void errorResetCommandGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - errorResetCommand - count: " + equipmentList.size();
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    while (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      strng = strng + " - " + equipment.getParsed();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/

  /**
   * Transport Data Change Command
   *
   * <P>Currently the only supported command is Delete.</P>
   *
   * @return
   */
  public String transportDataChangeCommandToString() // ID 041
  {
    id = 41;

    // Deletion only
    changeClassification = 1;
    moveClassification = 0;
    additionalPosition = 0;

    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + machineId + changeClassification
        + mcKey + moveClassification + getTwoAsciiDigits(additionalPosition);
    transportDataChangeCommandGetParsed();
    return messageAsString;
  }

  private void transportDataChangeCommandToDataValues()
  {
    machineId = messageData.substring(0, 6);
    changeClassification = getIntDigit(messageData, 6);
    mcKey = messageData.substring(7, 15);
    moveClassification = getIntDigit(messageData, 15);
    additionalPosition = getIntFromTwoAsciiDigits(messageData, 16);

    transportDataChangeCommandGetParsed();
  }

  private void transportDataChangeCommandGetParsed()
  {
    String vsParsed = getThreeAsciiDigits(id)
        + " - Transport Data Change Command - ";
    vsParsed += describeTransportDataChangeCommand();

    setParsedMessageString(vsParsed);
  }

  private String describeTransportDataChangeCommand()
  {
    String vsParsed = "";
    switch (changeClassification)
    {
      case CHANGE_CLASS_DELETE:
        vsParsed += "\nDelete " + mcKey + " from " + machineId;
        break;

      case CHANGE_CLASS_MOVE:
        vsParsed += "\nMove " + mcKey + " "
            + getMoveClassificationParsed(moveClassification) + " at "
            + machineId;
        break;

      case CHANGE_CLASS_ADD:
        vsParsed += "\nAdd " + mcKey + " to " + machineId + " at position "
            + additionalPosition;
        break;

      default:
        vsParsed += "\nUnknown command.";
    }
    return vsParsed;
  }

  private String getMoveClassificationParsed(int inMoveClassification)
  {
    switch (inMoveClassification)
    {
      case 1:
        return "Forward";
      case 2:
        return "Backward";
      default:
        return "N/A";
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Build a 141 message (for the emulator only)
   *
   * @return
   */
  public String transportDataChangeCommandResponseToString() // ID 141
  {
    // Temporary hard-code for emulator
    changeClassification = 1;
    moveClassification = 0;
    additionalPosition = 0;
    responseFlag = 0;

    id = 141;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + machineId + changeClassification
        + mcKey + moveClassification + getTwoAsciiDigits(additionalPosition)
        + responseFlag;
    transportDataChangeCommandResponseGetParsed();
    return messageAsString;
  }

  private void transportDataChangeCommandResponseToDataValues()
  {
    machineId = messageData.substring(0, 6);
    changeClassification = getIntDigit(messageData, 6);
    mcKey = messageData.substring(7, 15);
    moveClassification = getIntDigit(messageData, 15);
    additionalPosition = getIntFromTwoAsciiDigits(messageData, 16);
    responseFlag = getIntDigit(messageData, 18);

    transportDataChangeCommandResponseGetParsed();
  }

  private void transportDataChangeCommandResponseGetParsed()
  {
    String strng = getThreeAsciiDigits(id)
        + " - Transport Data Change Command Response - " + getResponseFlag()
        + " - ";
    strng += describeTransportDataChangeCommand();

    setParsedMessageString(strng);
  }

  public String getResponseFlag()
  {
    return (AGCMOSMessage.RESPONSE_FLAGS[responseFlag]);
  }
  public void setResponseFlag(int iiResponseFlag)
  {
    responseFlag = iiResponseFlag;
  }
  public boolean responseOk()
  {
    return (responseFlag == 0);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String cancelDataRequestToString() // ID 042
  {
    id = 42;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    if (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      messageAsString = messageAsString + equipment.getAsString();
    }
    cancelDataRequestGetParsed();
    return messageAsString;
  }

  private void cancelDataRequestToDataValues()
  {
    equipmentList.clear();
    Equipment equipment = new Equipment();
    equipment.setFromString(messageData, 0);
    equipmentList.add(equipment);
    cancelDataRequestGetParsed();
  }

  private void cancelDataRequestGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - cancelDataRequest - ";
    Iterator<Equipment> equipmentIterator = equipmentList.iterator();
    if (equipmentIterator.hasNext())
    {
      Equipment equipment = equipmentIterator.next();
      strng = strng + " - " + equipment.getParsed();
    }
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String systemModeChangeRequestToString() // ID 061
  {
    id = 61;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + systemMode;
    systemModeChangeRequestGetParsed();
    return messageAsString;
  }

  private void systemModeChangeRequestToDataValues()
  {
    systemMode = messageData.substring(0, 1);
    systemModeChangeRequestGetParsed();
  }

  private void systemModeChangeRequestGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id)
        + " - System Mode Change Request - "
        + SYSTEM_MODES[getIntFromOneAsciiDigit(systemMode, 0)]);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String logDataSaveCommandToString() // ID 066
  {
    id = 66;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = AGCMOSMessage.AGC_MODEL_CODE;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + "11" + AGCMOSMessage.AGC_MODEL_CODE +
                      getThreeAsciiDigits(msgMosNumber) + "1001;" + logName + ";";
    logDataSaveCommandGetParsed();
    return messageAsString;
  }

  private void logDataSaveCommandToDataValues()
  {
    machineId = getSubString(messageData, 1, 6);
    logName = messageData.substring(10);
    logDataSaveCommandGetParsed();
  }

  private void logDataSaveCommandGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - logDataSaveCommand - " +
        AGCMOSMessage.AGC_MODEL_CODE + getThreeAsciiDigits(msgMosNumber) +
        " logName \"" + logName + "\"";
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String logDataSaveCommandResponseToString() // ID 166
  {
    id = 166;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = AGCMOSMessage.AGC_MODEL_CODE;
    msgMosNumber = mosNumber;
    messageAsString = headerToString();
    messageAsString = messageAsString + "11" + AGCMOSMessage.AGC_MODEL_CODE +
                      getThreeAsciiDigits(msgMosNumber) + "001;" + logName + ";0";
    logDataSaveCommandResponseGetParsed();
    return messageAsString;
  }

  private void logDataSaveCommandResponseToDataValues()
  {
    machineId = getSubString(messageData, 1, 6);
    logName = messageData.substring(10);
    logDataSaveCommandResponseGetParsed();
  }

  private void logDataSaveCommandResponseGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - logDataSaveResponse - " + machineId +
                   " logs: " + logName;
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String communicationTestRequestToString() //ID 080
  {
    id = 80;
    textClassification = AGCMOSMessage.AGC_MOS;
    msgModelCode = modelCode;
    msgMosNumber = mosNumber;
    messageAsString = headerToString() + communicationTestTextRequest;
    communicationTestRequestGetParsed();
    return messageAsString;
  }

  private void communicationTestRequestToDataValues()
  {
    communicationTestTextRequest = messageData;
    communicationTestRequestGetParsed();
  }

  private void communicationTestRequestGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id)
        + " - Communication Test Request - \n  Test Text: \""
        + communicationTestTextRequest + "\"");
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String dateAndTimeCorrectionRequestToString() //ID 034
  {
    id = 34;
    textClassification = AGCMOSMessage.AGC_MOS;
//    dateTimeCorrectionClassification = AGCMOSMessage.SET_DATE_TIME;
    String sCorr = ASCII_DIGITS[dateTimeCorrectionClassification];
    setDateTimeString();
    messageAsString = headerToString() + sCorr + dataDateTimeString;
    dateAndTimeCorrectionRequestGetParsed();
    return messageAsString;
  }

  private void dateAndTimeCorrectionRequestToDataValues()
  {
    dateTimeCorrectionClassification = getIntFromOneAsciiDigit(messageData, 0);
    dataDateTimeString = messageData.substring(1);
    dateAndTimeCorrectionRequestGetParsed();
  }

  private void dateAndTimeCorrectionRequestGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id) + " - Date/Time Correction Request - Classification: " +
        AGCMOSMessage.DATE_TIME_CORRECTION_CLASSIFICATIONS[dateTimeCorrectionClassification] + " - " +
        getSubString(dataDateTimeString, 4, 2) + "/" +
        getSubString(dataDateTimeString, 6, 2) + "/" +
        getSubString(dataDateTimeString, 0, 4) + "  " +

        getSubString(dataDateTimeString, 8, 2) + ":" +
        getSubString(dataDateTimeString, 10, 2) + ":" +
        getSubString(dataDateTimeString, 12, 2) +
        "  hdr: " + headerGetParsed());
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String dateAndTimeReportToString() //ID 134
  {
    id = 134;
    textClassification = AGCMOSMessage.AGC_MOS;
    String sCorr = ASCII_DIGITS[dateTimeCorrectionClassification];
    setDateTimeString();
    messageAsString = headerToString() + sCorr + dataDateTimeString;
    dateAndTimeReportGetParsed();
    return messageAsString;
  }

  private void dateAndTimeReportToDataValues()
  {
    dateTimeCorrectionClassification = getIntFromOneAsciiDigit(messageData, 0);
    dataDateTimeString = messageData.substring(1);
    dateAndTimeReportGetParsed();
  }

  private void dateAndTimeReportGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id) + " - Date/Time Report - " +
        AGCMOSMessage.DATE_TIME_CORRECTION_CLASSIFICATIONS[dateTimeCorrectionClassification] + " - " +
        getSubString(dataDateTimeString, 4, 2) + "/" +
        getSubString(dataDateTimeString, 6, 2) + "/" +
        getSubString(dataDateTimeString, 0, 4) + "  " +

        getSubString(dataDateTimeString, 8, 2) + ":" +
        getSubString(dataDateTimeString, 10, 2) + ":" +
        getSubString(dataDateTimeString, 12, 2) +
        "  hdr: " + headerGetParsed());
  }


  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void warningReportToDataValues() //ID 150
  {
    machineId = getSubString(messageData, 0, 6);
    errorCode = getSubString(messageData, 6, 7);
    warningReportGetParsed();
  }

  private void  warningReportGetParsed()
  {
    String strng = getThreeAsciiDigits(id) + " - Warning Report - " +
                   machineId + "  Error Code: " +
                   errorCode + "  Time: " +
                   getSubString(transmissionTime, 0, 2) + ":" +
                   getSubString(transmissionTime, 2, 2) + ":" +
                   getSubString(transmissionTime, 4, 2);
    setParsedMessageString(strng);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String systemModeChangeResponseToString() //ID 161
  {
    id = 161;
    textClassification = AGCMOSMessage.AGC_MOS;
    String sResponseFlag = ASCII_DIGITS[responseFlag];
    messageAsString = headerToString() + systemMode + sResponseFlag;
    systemModeChangeResponseGetParsed();
    return messageAsString;
  }

  private void systemModeChangeResponseToDataValues()
  {
    systemMode = messageData.substring(0, 1);
    responseFlag = getIntFromOneAsciiDigit(messageData, 1);
    systemModeChangeResponseGetParsed();
  }

  private void systemModeChangeResponseGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id)
        + " - System Mode Change Response - "
        + SYSTEM_MODES[getIntFromOneAsciiDigit(systemMode, 0)] + "  "
        + RESPONSE_FLAGS[responseFlag]);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String transferHistoryReportToString() //ID 184
  {
    id = 184;
    textClassification = AGCMOSMessage.AGC_MOS;
    String sTransferTime = getFourAsciiDigits(transferTime);
    messageAsString = headerToString() + mcKey + sTransferTime + sourceStation + destinationStation;
    transferHistoryReportGetParsed();
    return messageAsString;
  }

  private void transferHistoryReportToDataValues()
  {
    mcKey = messageData.substring(0, 8);
    transferTime = getIntFromFourAsciiDigits(messageData, 8);
    sourceStation = messageData.substring(8, 12);
    destinationStation = messageData.substring(12, 16);
    transferHistoryReportGetParsed();
  }

  private void transferHistoryReportGetParsed()
  {
    setParsedMessageString(getThreeAsciiDigits(id)
        + " - transferHistoryReport - mcKey: \"" + mcKey + "\"  src: "
        + sourceStation + "  dst: " + destinationStation + " - transferTime: "
        + transferTime);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public String communicationTestResponseToString() //ID 180
  {
    id = 180;
    textClassification = AGCMOSMessage.AGC_MOS;
    String sResponse = ASCII_DIGITS[communicationTestResponse];
    messageAsString = headerToString() + sResponse + communicationTestTextResponse;
    communicationTestResponseGetParsed();
    return messageAsString;
  }

  /**
   * Set the communicationTestResponse (for the emulator)
   * <BR>0=Good
   * <BR>1=Bad
   */
  public void setCommunicationTestResponse(int inCommunicationTestResponse)
  {
    communicationTestResponse = inCommunicationTestResponse;
  }

  private void communicationTestResponseToDataValues()
  {
    communicationTestResponse = getIntFromOneAsciiDigit(messageData, 0);
    communicationTestTextResponse = messageData.substring(1);
    communicationTestResponseGetParsed();
  }

  private void communicationTestResponseGetParsed()
  {
    String sResponse = null;
    switch (communicationTestResponse)
    {
      case 0: sResponse = "OK"; break;
      case 1: sResponse = "NG"; break;
      default: sResponse = "*UNKNOWN-" + communicationTestResponse + "*"; break;
    }
    setParsedMessageString(getThreeAsciiDigits(id)
        + " - Communication Test Response - " + sResponse + "  Text Length: "
        + communicationTestTextResponse.length());
  }

  /**
   * In my experience, this conversion has always been true.
   *
   * @param isMCID
   * @return
   */
  public static String getMOSIDfromMCID(String isMCID)
  {
    String vsMOSID = "1" + isMCID.substring(0,2) + isMCID.substring(3);
    return vsMOSID;
  }

  /**
   * Since the MC status and the MOS status numbers are different...
   * @param inMCStatus
   * @return
   */
  public static int getMOSStatusFromMCStatus(int inMCStatus)
  {
    return MC2MOSSTATUS[inMCStatus];
  }
  static final int[] MC2MOSSTATUS = {1, 2, 6, 3, 7, 0, 0, 0, 0};
}