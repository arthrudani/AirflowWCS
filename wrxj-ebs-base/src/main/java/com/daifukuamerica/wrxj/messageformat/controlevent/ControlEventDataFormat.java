/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.messageformat.controlevent;

import com.daifukuamerica.wrxj.io.PropertyFileReader;
import com.daifukuamerica.wrxj.io.PropertyReader;
import com.daifukuamerica.wrxj.messageformat.MessageDataFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class defines IPC "Control" messages, primarily between user screens
 * and Controllers.  It is a conversion of boatloads of magic integer, 
 * character, and String values that already existed.  
 * 
 * <P>Someday, it would be nice if these messages were made more consistent.</P>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
public class ControlEventDataFormat extends MessageDataFormat
{
  /*==========================================================================*/
  /*  Message Types                                                           */
  /*==========================================================================*/
  
  /**
   * This message type basically says "Look at the text message instead"
   */
  public static final int TEXT_MESSAGE = 0;
  public static final int SEND_ARRIVAL_TO_EMULATOR = 1;
  public static final int SEND_70_MESSAGE_TO_EMULATOR = 2;
  public static final int SEND_TRANSPORT_DATA_DELETE_TO_EMULATOR = 3;
  
  /*
   * LogObserver -> SystemHealthMonitor
   */
  public static final int SHM_STATUS_REQUEST = 1;  // Status Request
  public static final int SHM_REQUEST_GROUPS = 2;  // Request Controller Groups
  
  /*
   * Equipment Monitor -> Controls Device (PLC)
   */
  public static final int PLC_STATUS_REQUEST = 1;  // Status Request
  public static final int PLC_COMM_TEST      = 2;  // Test Communications
  
  /*
   * Equipment Monitor -> MOS Device
   */
  public static final int MOS_STOP_POLLING   = 1;  // Stop Status Polling (MOS)
  public static final int MOS_START_POLLING  = 2;  // Start Status Polling (MOS)
  public static final int MOS_COMM_TEST      = 3;  // Test Communications (MOS)
  public static final int MOS_START_AISLE    = 4;  // Start Aisle (MOS)
  public static final int MOS_STOP_AISLE     = 5;  // Stop Aisle (MOS)
  public static final int MOS_RESET_ERROR    = 6;  // Reset Error (MOS)
  public static final int MOS_DELETE_TRACK   = 7;  // Delete Tracking Data (MOS)
  public static final int MOS_DISCONNECT     = 8;  // Disconnect (MOS)
  public static final int MOS_RECOVER_DATA   = 9;  // Recover Data (MOS)
  public static final int MOS_SAVE_ALL_LOGS  = 10; // Save Logs (MOS)
  public static final int MOS_SILENCE_ERROR  = 11; // Silence Error (MOS)
  public static final int MOS_SEND_BAR_CODE  = 12; // Send BCR data (MOS)
  public static final int MOS_STATUS_REQUEST = 13; // Status Request (MOS)
  public static final int MOS_START_EQUIP    = 14; // Start [MOS ID] (MOS)
  public static final int MOS_STOP_EQUIP     = 15; // Stop [MOS ID] (MOS)
  public static final int MOS_LATCH_CLEAR    = 16; // Stop [MOS ID] (MOS)

  /*
   * Host Emulation screen -> Host Emulator
   */
  public static final int HOST_LOAD_MOVER_MSG  = 0; // Manual host message
  public static final int HOST_LOAD_MOVER_AUTO = 1; // Automatic host
  public static final int HOST_LOAD_MOVER_MAN  = 2; // Manual Host
  
  /*
   * Recovery -> Scheduler
   */
  public static final int RECOVERY_ALTLOC = 1;      // Recovery.  Really a text message.
  
  /*==========================================================================*/
  /* Message Text Constants                                                   */
  /*==========================================================================*/

  /*
   * Equipment Monitor -> AGCStationDevice & Arc9yDevice
   */
  public static final char CHAR_START_DEVICE    = '1';  // Start AGC (MC)
  public static final char CHAR_STOP_DEVICE     = '3';  // Stop AGC (MC)
  public static final char CHAR_REQUEST_STATUS  = 'A';  // Status Request (MC)
  public static final char CHAR_COMM_TEST       = 'C';  // Comm test (MC)
  public static final char CHAR_START_EQUIPMENT = 'G';  // Start equipment (MC)
  public static final char CHAR_STOP_EQUIPMENT  = 'H';  // Stop equipment (MC)
  public static final char CHAR_RETRIEVE_MODE   = 'R';  // Retrieve Mode (AS21)
  public static final char CHAR_STORE_MODE      = 'S';  // Store Mode (AS21)
  public static final char CHAR_SEND_WEIGHT     = 'W';  // Send weight command

  /*
   * From where do these originate? (Received by PortController)
   */
  public static final char CHAR_START_PORT = 'C';  // Start Comm Port
  public static final char CHAR_STOP_PORT  = 'D';  // Stop Comm Port
  public static final char CHAR_PORT_TEST  = 'T';  // Test Comm Port

  /*
   * Strings used for starting up or shutting down controllers
   */
  public static final String CONTROLLER_STARTUP = "Startup";
  public static final String CONTROLLER_SHUTDOWN = "Shutdown";

  /*
   * Strings that are placed into the message, probably to make them human-
   * readable, but are never actually used.  These are usually, but not always,
   * associated with non-zero message types.
   */
  public static final String TEXT_ALLOC_HUNGRY_STATION = "CheckHungryStationsTimer";
  public static final String TEXT_LOG_REQUEST_GROUPS_STR = "Request Controller Groups";
  public static final String TEXT_MOS_COMM_TEST = "MOS Comm Test";
  public static final String TEXT_MOS_DELETE_TRACKING = "Delete-Tracking";
  public static final String TEXT_MOS_RESET_ERROR = "Reset Error";
  public static final String TEXT_MOS_SAVE_LOGS = "Save-Logs all";
  public static final String TEXT_MOS_SEND_BAR_CODE = "Send-Bar-Code";
  public static final String TEXT_MOS_SILENCE_ALARM = "Silence Alarm";
  public static final String TEXT_MOS_START_EQUIPMENT = "Start Equipment";
  public static final String TEXT_MOS_START_POLLING = "START Load/Status Polling";
  public static final String TEXT_MOS_STOP_EQUIPMENT = "Stop Equipment";
  public static final String TEXT_MOS_STOP_POLLING = "STOP Status Polling";
  public static final String TEXT_PLC_COMM_TEST = "PLC Comm Test";
  public static final String TEXT_SHM_ALL_STATUSES = "All Statuses Update Request";

  /*
   * Internal fields for building/parsing the command&target-list messages
   */
  public static final String LOCTOLOC = "LOCTOLOC";
  public static final String RETRIEVE = "RETRIEVE";
  public static final String STAGED = "STAGED";
  public static final String STORE = "STORE";
  private static final String DELIMITER = "|"; 

  private String msMessageCommand = "";
  private List<String> mpMessageTargets = null;
  
  /*
   * Internal fields for building/parsing the Emulation Arrival command and
   * MOS Barcode command
   */
  protected static final String ARR_STATION = "station";
  protected static final String ARR_MCKEY = "mcKey";
  protected static final String ARR_BARCODE = "barcode";
  protected static final String ARR_DIMENSION = "dimension";
  protected static final String ARR_CONTROL = "control";
  protected static final String ARR_LOADINFO = "loadinfo";
  protected static final String ARR_NULL = "null";

  protected String msMachineID = "";
  protected String msStation = "";
  protected String msBarCode = "";
  protected String msControlInfo = "";
  protected String msMCKey = "";
  protected int mnDimension = 0;
  protected int mnLoadInfo = 1;

  /**
   * Simple constructor
   * 
   * @param senderName
   */
  public ControlEventDataFormat(String senderName)
  {
    super(-1, senderName);
  }
  
  /**
   * Required constructor
   * 
   * @param messageType
   * @param senderName
   */
  public ControlEventDataFormat(int messageType, String senderName)
  {
    super(messageType, senderName);
  }

  /**
   * This class is slowly replacing hard-coded int, char, and String values.
   * Someday this method will work.
   */
  @Override
  public void createDataString() {}

  /**
   * This class is slowly replacing hard-coded int, char, and String values.
   * Someday this method will work.
   */
  @Override
  public boolean decodeDataString()
  {
    return false;
  }

  /**
   * Get the equipment start/stop command
   * 
   * @param icChange
   * @param isMOSID
   * @return
   */
  public static String getEquipCommand(char icChange, String isMOSID)
  {
    return icChange + isMOSID;
  }

  /**
   * Get the station mode-change command
   * 
   * @param icModeChange
   * @param isStation
   * @return
   */
  public static String getModeChangeCommand(char icModeChange, String isStation)
  {
    return icModeChange + isStation;
  }
  
  /**
   * Get a MOS machine command
   * 
   * @param isMachineID
   * @return
   */
  public static String getMosMachineCommand(String isMachineID)
  {
    return isMachineID;
  }
  
  /**
   * Parse Weight message
   * @param isMessage the message to be parsed
   * @return mdWeight 
   */
  public static Double parseWeightDataMessage(String isMessage)throws NumberFormatException
  {
  	StringTokenizer vpST = new StringTokenizer(isMessage, DELIMITER);
    vpST.nextToken();
    Double vdWeight = Double.parseDouble(vpST.nextToken());
    return vdWeight;
   
  }

  /**
   * Get a bar code command
   * 
   * @param isMachineID
   * @param isBCRData
   * @return
   */
  public static String getMosBcrDataCommand(String isMachineID, String isBCRData)
  {
    return TEXT_MOS_SEND_BAR_CODE + DELIMITER + isMachineID + DELIMITER + isBCRData;
  }

  /**
   * Parse a MOS barcode command
   * 
   * @param isMessage
   */
  public void parseMosBcrCommand(String isMessage)
  {
    StringTokenizer vpST = new StringTokenizer(isMessage, DELIMITER);
    vpST.nextToken();
    msMachineID = vpST.nextToken();
    msBarCode = vpST.nextToken();
  }
  
  /**
   * Get a data delete command
   * 
   * @param isMachineID
   * @param isMCKey
   * @return
   */
  public static String getMosTrackingDeleteCommand(String isMachineID,
      String isMCKey)
  {
    return TEXT_MOS_DELETE_TRACKING + " " + isMCKey + " " + isMachineID;
  }
  
  /**
   * Status request
   * 
   * @param isGroupName
   * @return
   */
  public static String getStatusMessage(String isGroupName)
  {
    return isGroupName + DELIMITER + TEXT_SHM_ALL_STATUSES;
  }

  /**
   * Get the group from the status message
   * 
   * <P>Note that, given the way this currently works, if no group is specified
   * this method returns the first word in TEXT_SHM_ALL_STATUSES, which happens
   * to be "All" and is an invalid group.  SystemHealthMonitor handles this.</P>
   * 
   * @param isKeyName
   * @return
   */
  public static String getGroupFromStatusMessage(String isMessage)
  {
    StringTokenizer vpTokenizer = new StringTokenizer(isMessage, DELIMITER);
    
    return vpTokenizer.nextToken();
  }

  /*==========================================================================*/
  /*  Command+TargetList messages                                             */
  /*==========================================================================*/

  /**
   * Create a message with a command and a list of targets
   *  
   * @param isCommand
   * @param iasTargets
   * @return
   */
  public static String getCommandTargetListMessage(String isCommand, 
      String... iasTargets)
  {
    StringBuffer vsMessage = new StringBuffer();
    vsMessage.append(isCommand);
    for(String vsStation : iasTargets)
    {
      vsMessage.append(DELIMITER).append(vsStation);
    }
    return vsMessage.toString();
  }

  /**
   * Parse out a scheduler wake-up command 
   * 
   * @param isMessage
   */
  public void parseCommandTargetListCommand(String isMessage)
  {
    StringTokenizer vpTokenizer = new StringTokenizer(isMessage, DELIMITER);
    msMessageCommand = vpTokenizer.nextToken();
    mpMessageTargets = new ArrayList<String>();
    while(vpTokenizer.hasMoreTokens())
    {
      mpMessageTargets.add(vpTokenizer.nextToken());
    }
  }
  
  /**
   * Get the scheduler wake-up command type
   * 
   * @return
   */
  public String getMessageCommand()
  {
    return msMessageCommand;
  }
  
  /**
   * Get the scheduler wake-up command list of stations
   *  
   * @return
   */
  public List<String> getMessageTargets()
  {
    return mpMessageTargets;
  }

  /*==========================================================================*/

  /**
   * Get an alternate location command to send to the scheduler when a load gets
   * stuck.
   * 
   * <P><I>Note: This method just echoes back the original string.  It is 
   * provided for documentation purposes.</I></P>
   * 
   * @param isCommand - The alternate location LoadEventMessage
   * @return
   */
  public static String getAlternateLocationCommand(String isCommand)
  {
    return isCommand;
  }
  
  /**
   * method stub
   * Parse an emulator 70 message
   * @param isMessage
   */
  public void parse70Message(String isMessage)
  {
  }
  
  /*==========================================================================*/

  /**
   * Build an arrival command to send to the 9y/AS21 emulator
   * 
   * @param isStation
   * @param isBarCode
   * @param isMCKey
   * @param isControlInfo
   * @param inDimension
   * @return
   */
  public static String getArrivalCommand(String isStation, String isBarCode,
                                         String isMCKey, String isControlInfo,
                                         int inDimension, int inLoadInfo)
  {
    String vsBarCode = isBarCode;
    if (isBarCode == null || isBarCode.length() == 0)
    {
      vsBarCode = ARR_NULL;
    }

    String vsControlInfo = isControlInfo;
    if (isControlInfo == null || isControlInfo.length() == 0)
    {
      vsControlInfo = ARR_NULL;
    }
    
    String vsMsgText = 
      ARR_STATION + " " + isStation + " " + 
      ARR_BARCODE + " " + vsBarCode + " " + 
      ARR_MCKEY + " " + isMCKey + " " + 
      ARR_CONTROL + " " + vsControlInfo + " " + 
      ARR_DIMENSION + " " + inDimension + " " +
      ARR_LOADINFO + " " + inLoadInfo;
    return vsMsgText;
  }
  
  /**
   * Parse an emulator arrival message
   * 
   * @param isMessage
   */
  public void parseArrivalCommand(String isMessage)
  {
    PropertyReader propertyReader = new PropertyFileReader();
    propertyReader.tokenize(isMessage);
    msStation = propertyReader.getProperty(ARR_STATION);
    msBarCode = propertyReader.getProperty(ARR_BARCODE);
    msControlInfo = propertyReader.getProperty(ARR_CONTROL);
    msMCKey = propertyReader.getProperty(ARR_MCKEY);
    mnDimension = propertyReader.getIntProperty(ARR_DIMENSION);
    mnLoadInfo = propertyReader.getIntProperty(ARR_LOADINFO);

    if (msBarCode.equals(ARR_NULL))
    {
      msBarCode = "                              ";
    }
    else
    {
      msBarCode = msBarCode + "                              ";
      msBarCode = msBarCode.substring(0, 30);
    }

    if (msControlInfo.equals(ARR_NULL))
    {
      msControlInfo = "                              ";
    }
    else
    {
      msControlInfo = msControlInfo + "                              ";
      msControlInfo = msControlInfo.substring(0, 30);
    }
  }

  /**
   * Get the station for the arrival message
   * 
   * @return
   */
  public String getStation()
  {
    return msStation;
  }
  
  /**
   * Get the MC Key for the arrival message
   * 
   * @return
   */
  public String getMCKey()
  {
    return msMCKey;
  }

  /**
   * Get the bar code for the arrival message or the MOS barcode command
   * 
   * @return
   */
  public String getBarCode()
  {
    return msBarCode;
  }

  /**
   * Get the control information for the arrival message
   * 
   * @return
   */
  public String getControlInformation()
  {
    return msControlInfo;
  }

  /**
   * Get the load dimension for the arrival message
   * 
   * @return
   */
  public int getDimensionInfo()
  {
    return mnDimension;
  }

  /**
   * Get the load information for the arrival message
   * 
   * @return
   */
  public int getLoadInfo()
  {
    return mnLoadInfo;
  }

  /**
   * Get the machine ID for the MOS barcode command
   * 
   * @return
   */
  public String getMachineID()
  {
    return msMachineID;
  }

  /**
   * Build a Transport Data Delete command to send to the AS21 emulator
   * 
   * @param isStation
   * @param isMCKey
   * @param isControlInfo
   * @return
   */
  public static String getTransportDataDeleteCommand(String isStation,
      String isMCKey, String isControlInfo)
  {
    String vsControlInfo = isControlInfo;
    if (isControlInfo == null || isControlInfo.length() == 0)
    {
      vsControlInfo = ARR_NULL;
    }
    
    String vsMsgText = 
      ARR_STATION + " " + isStation + " " + 
      ARR_MCKEY + " " + isMCKey + " " +
      ARR_CONTROL + " " + vsControlInfo; 
    return vsMsgText;
  }

  /**
   * Parse an emulator Transport Data Delete message
   * 
   * @param isMessage
   */
  public void parseTransportDataDeleteCommand(String isMessage)
  {
    PropertyReader propertyReader = new PropertyFileReader();
    propertyReader.tokenize(isMessage);
    msStation = propertyReader.getProperty(ARR_STATION);
    msMCKey = propertyReader.getProperty(ARR_MCKEY);
    msControlInfo = propertyReader.getProperty(ARR_CONTROL);

    if (msControlInfo.equals(ARR_NULL))
    {
      msControlInfo = "                              ";
    }
    else
    {
      msControlInfo = msControlInfo + "                              ";
      msControlInfo = msControlInfo.substring(0, 30);
    }
  }
}
