package com.daifukuamerica.wrxj.device.controls.conveyor;

/**
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      SK Daifuku
 * @author       Stephen Kendorski
 * @version 1.0
 */

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.device.controls.ControlsMessageInterface;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import com.daifukuamerica.wrxj.time.SkDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class ConveyorMessage implements ControlsMessageInterface
{
  private String text = null;
  private String type = null;
  
  /**
   * Message data field - normally the LoadId, or Error Code for a Status Report.
   */
  private String data = null;
  private String station = null;
  private String status = null;
  private String invalidMessageDescription = null;
  private String communicationTestTextRequest = null;
  private Map<String,String> equipmentStatuses = new TreeMap<String,String>();
  private boolean equipmentStatusChangesExist = false;
  protected static final String STATUS_TIME_FORMAT = "HH:mm:ss";
  private SkDateTime statusDateTime = new SkDateTime(ConveyorMessage.STATUS_TIME_FORMAT);
  
  /**
   * Semaphore to show if a decoded message is correct.
   */
  private boolean validMessage = false;

  public static final String NO_READ_BCR = "NR";
  public static final String NO_READ_STRING = "NO_READ";

  // Status field values
  public static final String STATUS_ALARM = "AL";
  public static final String STATUS_ERROR = "ER";
  public static final String STATUS_OFFLINE = "OF";
  public static final String STATUS_ONLINE = "ON";
  public static final String STATUS_RESET = "RE";

  public ConveyorMessage()
  {
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Parse and interpret the passed-in message text into fields defined for that
   * message type.
   *
   * @param isMessageString the message to decode
   */
  public void toDataValues(String isMessageString)
  {
    try
    {
      validMessage = true;
      text = isMessageString;
      type = isMessageString.substring(0,2);
      data = isMessageString.substring(2,10);
      data = data.trim();
      data = data.replaceAll(" ", "_");            // Don't allow spaces in the data
      station = isMessageString.substring(10,14);
      station = station.trim();
      status = isMessageString.substring(14,16);
      //
      if (type.equals("ST"))
      {
        // This is a STatus message, we need to keep track of status changes.
        processStatus("Conveyor");
      }
    }
    catch (Exception e)
    {
      if (validMessage)
      {
        validMessage = false;
        invalidMessageDescription = "#####  IndexOutOfBoundsException -- \"" +
                                     isMessageString + "\"  #####";
      }
      return;
    }
  }
  
  /**
   * Process a status message
   */
  protected void processStatus(String machineDescription)
  {
    String machineId = station;
    String machineNumber = station;
    String statusBytes = "-";
    String vsStatus = null;
    if (status.equalsIgnoreCase(STATUS_ONLINE))
      vsStatus = StatusEventDataFormat.STATUS_ONLINE;
    else if (status.equalsIgnoreCase(STATUS_OFFLINE))
      vsStatus = StatusEventDataFormat.STATUS_OFFLINE;
    else if (status.equalsIgnoreCase(STATUS_ALARM))
      vsStatus = StatusEventDataFormat.STATUS_ERROR;
    else if (status.equalsIgnoreCase(STATUS_ERROR))
      vsStatus = StatusEventDataFormat.STATUS_ERROR;
    else
    {
      vsStatus = StatusEventDataFormat.STATUS_UNKNOWN + status;
    }
    if (data.equals("00000000"))
    {
      data = StatusEventDataFormat.NONE;
    }
    else
    {
      vsStatus = vsStatus + "|" + data;
      data = StatusEventDataFormat.NONE;
    }
    String errorCode = data;
    String reportTime = statusDateTime.getCurrentDateTimeAsString();
    String previousStatus = StatusEventDataFormat.NONE;
    // TODO: Convert to StatusEventDataFormat
    String equipmentStatus = machineId +
                             "\t" + machineDescription +
                             "\t" + machineNumber +
                             "\t" + statusBytes +
                             "\t" + vsStatus +
                             "\t" + errorCode;
    
    //
    // Now, put the newly arrived equipment status into our global list of
    // ALL equipment statuses that we have ever received.
    //
    if (equipmentStatuses.containsKey(station))
    {
      previousStatus = equipmentStatuses.get(station);
      int previousStatusLength = previousStatus.length()
          - ConveyorMessage.STATUS_TIME_FORMAT.length();
      previousStatus = previousStatus.substring(0, previousStatusLength);
    }
    if (! equipmentStatus.equals(previousStatus))
    {
      equipmentStatus = equipmentStatus + "\t" + reportTime;
      equipmentStatuses.put(station, equipmentStatus);
      equipmentStatusChangesExist = true;
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Build a message from the given arguments (see below).
   * @param Type
   * @param Data
   * @param Station
   * @param Status
   */
  public String commandToString(String isType, String isData, String isStation, 
      String isStatus)
  {
    type = isType;
    if (isData.length() != 8)
    {
      isData = isData + "        ";
      isData = isData.substring(0, 8);
    }
    data = isData;
    if (isStation.length() != 4)
    {
      isStation = isStation + "    ";
      isStation = isStation.substring(0, 4);
    }
    station = isStation;
    status = isStatus;
    text = isType + isData + isStation + isStatus;
    return text;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Convert the current command to a string for transmission
   */
  public String getMessageAsString()
  {

    text = type + data + station + status;
    return text;
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   * Get the parsed message string
   */
  public String getParsedMessageString()
  {
    String s = "Msg Type \"" + type + "\" ";
    if (type.equals("AR"))       // Arrival Report
    {
      s = s + "Arrival Report";
    }
    else if (type.equals("ST"))  // Status
    {
      s = s + "Status Report";
    }
    else if (type.equals("DV"))  // Divert Command
    {
      s = s + "Divert Command";
    }
    else if (type.equals("DC"))  // Divert Complete
    {
      s = s + "Divert Complete";
    }
    else if (type.equals("TM"))  // Test Message Request
    {
      s = s + "Test Message Request";
    }
    else if (type.equals("TR"))  // Test Message Response
    {
      s = s + "Test Message Response";
    }
    else
    {
      s = s + "** UNKNOWN**";
    }
    s = s + " - Data \"" + data + "\" - Stn: " + station + "  Status: " + status;
    return s;
  }

  /**
   * Is this a valid message?
   * @return
   */
  public boolean getValidMessage()
  {
    return(validMessage);
  }
  
  /**
   * Get the description of why the message is invalid
   * @return String if message is invalid, null if message is valid
   */
  public String getInvalidMessageDescription()
  {
    return(invalidMessageDescription);
  }
  
  // Getters/Setters for message fields
  public String getType()
  {
    return(type);
  }
  public void setType(String isType)
  {
    type = isType;
  }
  public String getData()
  {
    return(data);
  }
  public String getStation()
  {
    return(station);
  }
  public String getStatus()
  {
    return(status);
  }
  public int getIntStatus()
  {
    Integer iStatus = Integer.valueOf(status);
    return(iStatus.intValue());
  }

  /**
   * Set the communication test message text
   * @param isType message type
   */
  public void setCommunicationTestText(String isType)
  {
    communicationTestTextRequest = commandToString(isType, "CommTest", "0000", "00");
  }
  
  /**
   * Did the communications test succeed?
   * @return boolean
   */
  public boolean getCommunicationTestResult()
  {
    //
    // We're comparing Test Response with Test Request, so drop message type.
    //
    String s1 = text.substring(2,16);
    String s2 = communicationTestTextRequest.substring(2,16);
    return (s1.equals(s2));
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Are there any status changes?
   * @return boolean
   */
  public boolean equipmentStatusChanges()
  {
    return equipmentStatusChangesExist;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Set the equipment status
   * @param isStatus
   */
  public void setEquipmentStatus(String isStatus)
  {
    equipmentStatusChangesExist = false;
    String reportTime = statusDateTime.getCurrentDateTimeAsString();
    Iterator<String> statusIterator = equipmentStatuses.values().iterator();
    while (statusIterator.hasNext())
    {
      String previousEquipmentStatus = statusIterator.next();
      StringTokenizer st = new StringTokenizer(previousEquipmentStatus);
      String machineId = st.nextToken();
      String machineDescription = st.nextToken();
      String machineNumber = st.nextToken();
      String statusBytes = st.nextToken();
      String statusDescription = st.nextToken();
      String errorCode = st.nextToken();
      if (! isStatus.equals(statusDescription))
      {
        equipmentStatusChangesExist = true;
        // TODO: Convert to StatusEventDataFormat
        String equipmentStatus = machineId +
                                 "\t" + machineDescription +
                                 "\t" + machineNumber +
                                 "\t" + statusBytes +
                                 "\t" + isStatus +
                                 "\t" + errorCode +
                                 "\t" + reportTime;
        equipmentStatuses.put(machineId, equipmentStatus);
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Set the equipment status of a station
   * @param isStation
   * @param isStatus
   */
  public void setEquipmentStatus(String isStation, String isStatus)
  {
    try
    {
      String previousEquipmentStatus = equipmentStatuses.get(isStation);
      if (previousEquipmentStatus == null)
      {
        // TODO: Convert to StatusEventDataFormat
        String equipmentStatus = isStation +
                                 "\t" + "Conveyor" +
                                 "\t" + isStation +
                                 "\t" + "-" +
                                 "\t" + isStatus +
                                 "\t" + StatusEventDataFormat.NONE +
                                 "\t" + statusDateTime.getCurrentDateTimeAsString();
        equipmentStatuses.put(isStation, equipmentStatus);
      }
      else
      {
        StringTokenizer st = new StringTokenizer(previousEquipmentStatus);
        String machineId = st.nextToken();
        String machineDescription = st.nextToken();
        String machineNumber = st.nextToken();
        String statusBytes = st.nextToken();
        String statusDescription = st.nextToken();
        String errorCode = st.nextToken();
        String reportTime = statusDateTime.getCurrentDateTimeAsString();
        //
        int vsIndex = statusDescription.indexOf('|');
        if (vsIndex != -1)
        {
          statusDescription = statusDescription.substring(0, vsIndex);
        }
        if (!isStatus.equals(statusDescription))
        {
          equipmentStatusChangesExist = true;
          // TODO: Convert to StatusEventDataFormat
          String equipmentStatus = machineId +
                                   "\t" + machineDescription +
                                   "\t" + machineNumber +
                                   "\t" + statusBytes +
                                   "\t" + isStatus +
                                   "\t" + errorCode +
                                   "\t" + reportTime;
          equipmentStatuses.put(machineId, equipmentStatus);
        }
      }
    }
    catch (Exception e)
    {
      /*
       * TODO: It would probably be good to log something here, but there is no
       * logger currently available to ControlsMessage.
       */
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Set the load ID at a station
   * @param isStation
   * @param isLoadId
   */
  public void setEquipmentLoadId(String isStation, String isLoadId)
  {
    try
    {
      String previousEquipmentStatus = equipmentStatuses.get(isStation);
      StringTokenizer st = new StringTokenizer(previousEquipmentStatus);
      String machineId = st.nextToken();
      String machineDescription = st.nextToken();
      String machineNumber = st.nextToken();
      String statusBytes = st.nextToken();
      String statusDescription = st.nextToken();
      String errorCode = st.nextToken();
      String reportTime = statusDateTime.getCurrentDateTimeAsString();
      //
      int vsIndex = statusDescription.indexOf('|');
      if (vsIndex != -1)
      {
        statusDescription = statusDescription.substring(0, vsIndex);
      }
      statusDescription = statusDescription + "|" + isLoadId;
      equipmentStatusChangesExist = true;
      String equipmentStatus = machineId +
                               "\t" + machineDescription +
                               "\t" + machineNumber +
                               "\t" + statusBytes +
                               "\t" + statusDescription +
                               "\t" + errorCode +
                               "\t" + reportTime;
      equipmentStatuses.put(machineId, equipmentStatus);
    }
    catch (Exception e)
    {
      Logger.getLogger().logException(e);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Get the status report
   * @return String
   */
  public String getEquipmentStatusReport()
  {
    /*
     * TODO: Convert to StatusEventDataFormat 
     */
    equipmentStatusChangesExist = false;
    String strng = "";
    Iterator<String> statusIterator = equipmentStatuses.values().iterator();
    while (statusIterator.hasNext())
    {
      String equipmentStatusReport = statusIterator.next();
      strng = strng + equipmentStatusReport + "\n";
    }
    return ControllerConsts.EQUIPMENT_STATUS + "\t" + strng;
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   * Is this a no-read?
   * @param isBCR
   * @return boolean
   */
  public boolean isNoRead(String isBCR)
  {
    return (isBCR.trim().equals(NO_READ_BCR)); 
  }
}
