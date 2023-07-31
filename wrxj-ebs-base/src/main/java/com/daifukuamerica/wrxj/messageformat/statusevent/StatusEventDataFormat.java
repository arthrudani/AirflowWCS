package com.daifukuamerica.wrxj.messageformat.statusevent;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.messageformat.MessageDataFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StatusEventDataFormat  extends MessageDataFormat
{
  public static final String NO_DATA = "---";
  public static final String NONE = "*NONE*";

  // Primary status options
  public static final String STATUS_DISCONNECT = "Disconnect";
  public static final String STATUS_ERROR = "Error";
  public static final String STATUS_OFFLINE = "Offline";
  public static final String STATUS_ONLINE = "Online";
  public static final String STATUS_RUNNING = "Running";
  public static final String STATUS_STOPPED = "Stopped";
  public static final String STATUS_UNKNOWN = "Unknown";
  public static final String STATUS_NO_LOG = "NoLogging"; // Error, but don't log 
  
  // Secondary status options
  public static final String STATUS_NOT_APPLICABLE = "N/A";
  public static final String STATUS_LOCTOLOC = "Loc-Loc";
  public static final String STATUS_RETRIEVE = "Retrieve";
  public static final String STATUS_STORE = "Store";
  public static final String STATUS_TRANSFER = "Direct-Transfer";
  
  private char msType = ControllerConsts.CONTROLLER_STATUS;
  
  private static final String DELIMITER = "\t"; 
  private List<StatusInfo> mpStatusList = new ArrayList<StatusInfo>(); 

  /*========================================================================*/
  /*  Constructors                                                          */
  /*========================================================================*/
  
  public StatusEventDataFormat(String name)
  {
    super(MessageEventConsts.STATUS_EVENT_TYPE, name);
  }

  /*========================================================================*/
  /*  Helper methods                                                        */
  /*========================================================================*/

  /**
   * Clears the values of all data in this class
   */
  @Override
  public void clearAllData()
  {
    super.clearAllData();
    mpStatusList.clear();
  }

  /**
   * Puts all data of this class into a string with delimeters of ";"
   * 
   * @return myString suppers.tosting and all of this classes data appended to it
   */
  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer();
    myString.append("Type: ").append(msType).append(" Message: ");
    for (StatusInfo s : mpStatusList)
    {
      myString.append(s.toString()).append("; ");
    }
    
    return myString.toString();
  }

  /**
   * Formats the data string to be published
   * @param myString String buffer to append this classes data to
   * @return myString the orginal string with this classes appended data
   */
  public StringBuffer formatDataString(StringBuffer myString)
  {
    myString.append(msType).append(DELIMITER);
    for (StatusInfo s : mpStatusList)
    {
      myString.append(s.toString());
    }
  	return myString;
  }
  
  /**
   * This method takes all data values and make them into one string with delimiters
   * of ";" and stores that string into the base class dataString
   */
  @Override
  public void createDataString()
  {
    StringBuffer myString = new StringBuffer();
    myString = formatDataString(myString);
    setDataString(myString.toString());
  }

  /**
   * parses the current string and stores the data 
   * @param parser string to continue parsing
   * @return parser string that I just parsed what data I needed
   * @throws Exception error parsing the data
   */
  public StringTokenizer parseDataString(StringTokenizer parser) throws Exception 
  {
    return parser;
  }
  
  /**
   * This method takes the get data string of the base class and decodes it into the
   * individule data members of this class.
   * @return True is a valid message was decoded else returns false
   */
  @Override
  public boolean decodeDataString()
  {
    StringTokenizer parser = new StringTokenizer(getDataString(), ";");
    boolean validMessage = true;
    try
    {
      clearAllData();
      parseDataString(parser);
    }
    catch (Exception e)
    {
      validMessage = false;
    }
    return validMessage;
  }

  /**
   * Set the type of StatusMessage
   * @param isType from <code>ControllerConsts</code>
   */
  public void setType(char isType)
  {
    msType = isType;
  }

  /**
   * Get the type of StatusMessage
   * @return
   */
  public char getType()
  {
    return msType;
  }

  /**
   * Add a Bidirectional status update.
   * @param isStationName
   * @param isStatus
   */
  public void addBidirectionalStatus(String isStationName, String isStatus)
  {
    mpStatusList.add(new StatusInfo(isStationName, isStatus));
  }
  
  /**
   * Controller Status messages
   * @param isName
   * @param icType
   * @param isStatus
   * @param isError
   * @param isUpdateTime
   */
  public void addStatusMessage(String isName, char icType, String isStatus, 
      String isError, String isUpdateTime)
  {
    mpStatusList.add(new StatusInfo(isName, ""+icType, isStatus, isError, isUpdateTime));
  }

  /**
   * Controller Status messages
   * @param isName
   * @param isType
   * @param isStatus
   * @param isError
   * @param isUpdateTime
   */
  public void addStatusMessage(String isName, String isType, String isStatus, 
      String isError, String isUpdateTime)
  {
    mpStatusList.add(new StatusInfo(isName, isType, isStatus, isError, isUpdateTime));
  }

  /**
   * Equipment Status Messages (from MOS Port)
   * @param isID
   * @param isType
   * @param isNo
   * @param isStat
   * @param isDesc
   * @param isError
   * @param isUpdateTime
   */
  public void addEquipmentStatus(String isID, String isType, String isNo, 
      String isStat, String isDesc, String isError, String isUpdateTime)
  {
    mpStatusList.add(new StatusInfo(isID, isType, isNo, isStat, isDesc, isError, 
        isUpdateTime));
  }

  /**
   * Machine Status Messages (from MC Port)
   * @param isID
   * @param isType
   * @param isNo
   * @param isStat
   * @param isDesc
   * @param isError
   */
  public void addMachineStatus(String isID, String isType, String isNo, 
      Integer isStat, String isDesc, String isError)
  {
    mpStatusList.add(new StatusInfo(isID, isType, isNo, isStat.toString(), 
        isDesc, isError));
  }
  
  /**
   * Tracking status messages
   * @param isName
   * @param isMachine
   * @param isKey
   * @param isBCR
   * @param isType
   * @param isSrc
   * @param isDst
   * @param isSize
   * @param isUpdateTime
   */
  public void addTrackingMessage(String isName, String isMachine, String isKey,
      String isBCR, String isType, String isSrc, String isDst, String isSize,
      String isUpdateTime)
  {
    mpStatusList.add(new StatusInfo(isName, isMachine, isKey, isBCR, isType,
        isSrc, isDst, isSize, isUpdateTime));
  }
  
  public void addStatusMessage(String[] iasValues)
  {
    mpStatusList.add(new StatusInfo(iasValues));
  }
  
  public void setMessage(String isMessage)
  {
    clearAllData();
    setType(isMessage.charAt(0));
    if (isMessage.length() > 2)
    {
      String vsMessageData = isMessage.substring(isMessage.indexOf('\t')+1);
      StringTokenizer vpST = new StringTokenizer(vsMessageData, "\n");
      while (vpST.hasMoreTokens())
      {
        mpStatusList.add(new StatusInfo(vpST.nextToken()));
      }
    }
  }
  
  public List<StatusInfo> getStatusList()
  {
    return mpStatusList;
  }
  
  /**
   * For use with the StatusModel dealing with tracking
   * @return
   */
  public String getMessageWithoutHeader()
  {
    StringBuffer vsMessage = new StringBuffer();
    
    for (StatusInfo s : mpStatusList)
    {
      vsMessage.append(s.toString());
    }
//    if (vsMessage.length() == 0)
//    {
//      return null;
//    }
    return vsMessage.toString();
  }
  
  @Override
  public String createStringToSend()
  {
    if (msType != ControllerConsts.NUDGE_EQUIPMENT_MONITOR && 
        msType != ControllerConsts.TRACKING_STATUS && 
        mpStatusList.size() == 0)
    {
      return null;
    }
    else
    {
      createDataString();
      return getDataString();
    }
  }
}