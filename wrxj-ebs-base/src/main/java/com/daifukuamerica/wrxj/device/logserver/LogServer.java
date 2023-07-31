package com.daifukuamerica.wrxj.device.logserver;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.LoggingDataAccess;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import com.daifukuamerica.wrxj.log.io.LogReaderWriter;
import java.util.StringTokenizer;

/**
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      SK Daifuku
 * @author
 * @version 1.0
 */

/**
 * LogServer is a Controller that publishes logs to remote log viewers that
 * need to observe this running instance of WRx-J logs. This device publishes
 * the necessary "Log" message responses to the remote viewer requests (right
 * now the LogViewerFrame display).
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public class LogServer extends Controller
{
  public LogServer()
  {
  }

  @Override
  public void initialize(String isControllerKeyName)
  {
    super.initialize(isControllerKeyName);
    logger.logDebug("LogServer.initialize() - Start");
    subscribeControlEvent();
    subscribeForLogEvent();
    logger.logDebug("LogServer.initialize() - End");
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("LogServer.startup() - Start");
    setControllerStatus(ControllerConsts.STATUS_RUNNING);
    logger.logDebug("LogServer.startup() - End");
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Device/Equipment.
   */
  /*--------------------------------------------------------------------------*/
  @Override
  public void shutdown()
  {
    logger.logDebug("LogServer.shutdown() -- Start");
    logger.logDebug("LogServer.shutdown() -- End");
    super.shutdown();
  }

  /*--------------------------------------------------------------------------*/
  // Process System Inter-Process-Communication Message.
  /*--------------------------------------------------------------------------*/
  @Override
  protected void processIPCReceivedMessage()
  {
    //
    // (Decide how to) Process message here
    //
    // receivedText = receivedMessage.getMessageText();
    // receivedData = receivedMessage.getMessageData();
    // receivedEventType = receivedMessage.getEventType();
    // receivedEvent = receivedMessage.getEvent();
    //
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.LOG_EVENT_TYPE:
          processLogEvent();
          break;
        default:
        receivedMessageProcessed = false;
        break;
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Process a LOG EVENT (From a Remote Log Viewer Frame).
   */
  /*--------------------------------------------------------------------------*/
  protected void processLogEvent()
  {
    if (controllerStatus != ControllerConsts.STATUS_RUNNING)
    {
      logger.logDebug("Controller NOT Running - Event DISCARDED - processLogEvent()");
      return;
    }
    try
    {
      switch (receivedData)
      {
        case LogConsts.SEND_LOG_LIST:
          sendLogList();
          break;
        case LogConsts.SEND_LOG_ENTRY_COUNT:
          sendLogEntryCount();
          break;
        case LogConsts.SEND_LOG_ENTRIES:
          sendLogEntries();
          break;
        case LogConsts.SEND_FILTERED_LOG_ENTRY_COUNT:
          sendFilteredLogEntryCount();
          break;
        case LogConsts.SEND_FILTERED_LOG_ENTRIES:
          sendFilteredLogEntries();
          break;
        case LogConsts.SAVE_LOGS:
          saveLogs();
          break;
        case LogConsts.ADD_LOG_ENTRY:
          addLogEntry();
          break;
        case LogConsts.FIND_TEXT:
          findText();
          break;
        default:
        {
          logger.logError("UNKNOWN Event Type \"" + receivedData + "\" -- LogServer.processLogEvent()");
        }
      }
    }
    catch(StringIndexOutOfBoundsException e)
    {
      logger.logSparseException(e, "LogServer.processLogEvent() - \"" + receivedText + "\"");
    }
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Send a string containing the Controller Group Name and a tab delimited
   * list of log names and descriptions
   */
  /*-------------------------------------------------------------------------*/
  private void sendLogList()
  {
    String s = Logger.getDelimitedLogList();
    publishLogEvent(s, LogConsts.SEND_LOG_LIST, receivedCKN);
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Send the number of available log entries.  The received message is a
   * tab-delimited string specifying the name of the log, and a message description.
   * The returned log event contains the log name, entry count, and current log
   * sink entry number.
   */
  private void sendLogEntryCount()
  {
    int idx = receivedText.indexOf("\t");
    String vsReceivedLogDescription = receivedText.substring(0, idx); // Name of Log
    String vsLogDescription = vsReceivedLogDescription;
    //
    // If requesting Communication or Equipment logs strip off the constant log
    // type text to get the log description that we need to find the logs.
    //
    if (vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME));
    }
    else if (vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME));
    }

    LoggingDataAccess vlLog = (LoggingDataAccess)Logger.getLog(vsLogDescription);
    if (vlLog != null)
    {
      int logEntryCount = vlLog.getEntryCount();
      int logEntrySink = vlLog.getLogSinkEntryNumber();
      int latestLogEntryNumber = vlLog.getLatestLogEntryNumber();
      publishLogEvent(vsReceivedLogDescription + "\t" + String.valueOf(logEntryCount) +
                                         "\t" + String.valueOf(logEntrySink) +
                                         "\t" + String.valueOf(latestLogEntryNumber),
                                         LogConsts.SEND_LOG_ENTRY_COUNT, receivedCKN);
    }
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Send the requested range of log entries.  The received message is a
   * tab-delimited string specifying the name of the log, start entry number,
   * end entry number, and a message description.
   * The returned log event contains the log name, entry count, current log
   * sink entry number, and the log entries.
   */
  private void sendLogEntries()
  {
    StringTokenizer stringTokenizer = new StringTokenizer(receivedText, "\t");
    String vsReceivedLogDescription = stringTokenizer.nextToken();
    String vsLogDescription = vsReceivedLogDescription;
    //
    // If requesting Communication or Equipment logs strip off the constant log
    // type text to get the log description that we need to find the logs.
    //
    if (vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME));
    }
    else if (vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME));
    }
    String vsLogEntryNumberStart = stringTokenizer.nextToken();
    Integer logEntryNumber = Integer.valueOf(vsLogEntryNumberStart);
    int viLogFirstEntryNumber = logEntryNumber.intValue();
    //
    String vsLogEntryNumber = stringTokenizer.nextToken();
    logEntryNumber = Integer.valueOf(vsLogEntryNumber);
    int viLogLastEntryNumber = logEntryNumber.intValue();
    //
    // Get the actual logger whose entries we need.
    //
    LoggingDataAccess vlLog = (LoggingDataAccess)Logger.getLog(vsLogDescription);
    //
    int logEntryCount = vlLog.getEntryCount();
    int logEntrySink = vlLog.getLogSinkEntryNumber();
    int latestLogEntryNumber = vlLog.getLatestLogEntryNumber();
    if (viLogLastEntryNumber >= logEntryCount)
    {
      viLogLastEntryNumber = logEntryCount - 1;
    }
    //
    int[] indexArray = null;
    //
    // Get the correct translation array to get the correct log entry fields
    // for the required log type.
    //
    if (vsReceivedLogDescription.indexOf(LogConsts.SYSTEM_LOG_NAME) != -1)
    {
      indexArray = LogConsts.SYS_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.OPERATION_LOG_NAME) != -1)
    {
      indexArray = LogConsts.OPR_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.ERROR_LOG_NAME) != -1)
    {
      indexArray = LogConsts.ERR_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.COMM_LOG_NAME) != -1)
    {
      indexArray = LogConsts.COM_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
    {
      indexArray = LogConsts.COM_LOG_COLUMN_FIELDS;
    }
    StringBuffer sbLogEntries = new StringBuffer(10000);
    sbLogEntries.append(vsReceivedLogDescription).append("\t");
    sbLogEntries.append(String.valueOf(logEntryCount)).append("\t");
    sbLogEntries.append(String.valueOf(logEntrySink)).append("\t");
    sbLogEntries.append(String.valueOf(latestLogEntryNumber)).append("\n");
    sbLogEntries.append(vsLogEntryNumberStart).append("\n");
    //
    StringBuffer sbLine = new StringBuffer(1000);
    StringBuffer sbField = new StringBuffer(1000);
    for (int i = viLogFirstEntryNumber; i <= viLogLastEntryNumber; i ++)
    {
      sbLine.setLength(0);
      int viIndexArrayLength = indexArray.length;
      for (int cc = 0; cc < viIndexArrayLength; cc++)
      {
        sbField.setLength(0);
        sbField.append(vlLog.getValue(i, indexArray[cc], true));
        if (sbField.length() == 0)
        {
          sbField.append(" ");
        }
        else
        {
          //
          // Convert any tabs and cr-lf's to "escape" characters so these
          // characters don't affect our actual delimiting characters.
          //
          int viSBFieldLength = sbField.length();
          for (int j = 0; j < viSBFieldLength; j++)
          {
            char c = sbField.charAt(j);
            if (c < (char)0x20)
            {
              if (c == (char)0x0d)
              {
                sbField.setCharAt(j, (char)0x01);
              }
              else if (c == (char)0x0a)
              {
                sbField.setCharAt(j, (char)0x02);
              }
              else if (c == (char)0x09)
              {
                sbField.setCharAt(j, (char)0x03);
              }
            }
          }
        }
        sbLine.append(sbField).append("\t");
      }
      sbLine.setLength(sbLine.length() - 1);
      sbLogEntries.append(sbLine).append("\n");
    }
    publishLogEvent(sbLogEntries.toString(), LogConsts.SEND_LOG_ENTRIES, receivedCKN);
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Send the number of available log entries.  The received message is a
   * tab-delimited string specifying the name of the log, and a message description.
   * The returned log event contains the log name, entry count, and current log
   * sink entry number.
   */
  private void sendFilteredLogEntryCount()
  {
    StringTokenizer stringTokenizer = new StringTokenizer(receivedText, "\t");
    String vsReceivedLogDescription = stringTokenizer.nextToken();
    String vsLogDescription = vsReceivedLogDescription;
    //
    // If requesting Communication or Equipment logs strip off the constant log
    // type text to get the log description that we need to find the logs.
    //
    if (vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME));
    }
    else if (vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME));
    }
    String filterText = stringTokenizer.nextToken();
    //
    String vsFilterIndex = stringTokenizer.nextToken();
    Integer iFilterIndex = Integer.valueOf(vsFilterIndex);
    int filterIndex = iFilterIndex.intValue();
    //
    LoggingDataAccess vlLog = (LoggingDataAccess)Logger.getLog(vsLogDescription);
    int logEntryCount = vlLog.getEntryCount(filterText, filterIndex);
    int logEntrySink = vlLog.getLogSinkEntryNumber(filterText, filterIndex);
    int latestLogEntryNumber = vlLog.getLatestLogEntryNumber(filterText, filterIndex);
    publishLogEvent(vsReceivedLogDescription + "\t" + String.valueOf(logEntryCount) +
                                       "\t" + String.valueOf(logEntrySink) +
                                       "\t" + String.valueOf(latestLogEntryNumber) +
                                       "\t" + filterText + 
                                       "\t" + vsFilterIndex,
                                       LogConsts.SEND_FILTERED_LOG_ENTRY_COUNT,
                                       receivedCKN);
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Send the requested range of log entries.  The received message is a
   * tab-delimited string specifying the name of the log, start entry number,
   * end entry number, and a message description.
   * The returned log event contains the log name, entry count, current log
   * sink entry number, and the log entries.
   */
  private void sendFilteredLogEntries()
  {
    StringTokenizer stringTokenizer = new StringTokenizer(receivedText, "\t");
    String vsReceivedLogDescription = stringTokenizer.nextToken();
    String vsLogDescription = vsReceivedLogDescription;
    //
    // If requesting Communication or Equipment logs strip off the constant log
    // type text to get the log description that we need to find the logs.
    //
    if (vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME));
    }
    else if (vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME));
    }
    String vsLogEntryNumberStart = stringTokenizer.nextToken();
    Integer logEntryNumber = Integer.valueOf(vsLogEntryNumberStart);
    int viLogFirstEntryNumber = logEntryNumber.intValue();
    //
    String vsLogEntryNumber = stringTokenizer.nextToken();
    logEntryNumber = Integer.valueOf(vsLogEntryNumber);
    int viLogLastEntryNumber = logEntryNumber.intValue();
    String filterText = stringTokenizer.nextToken();
    //
    String vsFilterIndex = stringTokenizer.nextToken();
    Integer iFilterIndex = Integer.valueOf(vsFilterIndex);
    int filterIndex = iFilterIndex.intValue();
    //
    // Get the actual logger whose entries we need.
    //
    LoggingDataAccess vlLog = (LoggingDataAccess)Logger.getLog(vsLogDescription);
    //
    int logEntryCount = vlLog.getEntryCount(filterText, filterIndex);
    int logEntrySink = vlLog.getLogSinkEntryNumber(filterText, filterIndex);
    int latestLogEntryNumber = vlLog.getLatestLogEntryNumber(filterText, filterIndex);
    if (viLogLastEntryNumber >= logEntryCount)
    {
      viLogLastEntryNumber = logEntryCount - 1;
    }
    //
    int[] indexArray = null;
    //
    // Get the correct translation array to get the correct log entry fields
    // for the required log type.
    //
    if (vsReceivedLogDescription.indexOf(LogConsts.SYSTEM_LOG_NAME) != -1)
    {
      indexArray = LogConsts.SYS_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.OPERATION_LOG_NAME) != -1)
    {
      indexArray = LogConsts.OPR_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.ERROR_LOG_NAME) != -1)
    {
      indexArray = LogConsts.ERR_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.COMM_LOG_NAME) != -1)
    {
      indexArray = LogConsts.COM_LOG_COLUMN_FIELDS;
    }
    else if (vsReceivedLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
    {
      indexArray = LogConsts.COM_LOG_COLUMN_FIELDS;
    }
    StringBuffer sbLogEntries = new StringBuffer(10000);
    sbLogEntries.append(vsReceivedLogDescription).append("\t");
    sbLogEntries.append(String.valueOf(logEntryCount)).append("\t");
    sbLogEntries.append(String.valueOf(logEntrySink)).append("\t");
    sbLogEntries.append(String.valueOf(latestLogEntryNumber)).append("\t");
    sbLogEntries.append(filterText).append("\t");
    sbLogEntries.append(vsFilterIndex).append("\n");
    sbLogEntries.append(vsLogEntryNumberStart).append("\n");
    //
    StringBuffer sbLine = new StringBuffer(1000);
    StringBuffer sbField = new StringBuffer(1000);
    for (int i = viLogFirstEntryNumber; i <= viLogLastEntryNumber; i ++)
    {
      sbLine.setLength(0);
      int viIndexArrayLength = indexArray.length;
      for (int cc = 0; cc < viIndexArrayLength; cc++)
      {
        sbField.setLength(0);
        sbField.append(vlLog.getValue(i, indexArray[cc], true, filterText, filterIndex));
        if (sbField.length() == 0)
        {
          sbField.append(" ");
        }
        else
        {
          //
          // Convert any tabs and cr-lf's to "escape" characters so these
          // characters don't affect our actual delimiting characters.
          //
          int viSBFieldLength = sbField.length();
          for (int j = 0; j < viSBFieldLength; j++)
          {
            char c = sbField.charAt(j);
            if (c < (char)0x20)
            {
              if (c == (char)0x0d)
              {
                sbField.setCharAt(j, (char)0x01);
              }
              else if (c == (char)0x0a)
              {
                sbField.setCharAt(j, (char)0x02);
              }
              else if (c == (char)0x09)
              {
                sbField.setCharAt(j, (char)0x03);
              }
            }
          }
        }
        sbLine.append(sbField).append("\t");
      }
      sbLine.setLength(sbLine.length() - 1);
      sbLogEntries.append(sbLine).append("\n");
    }
    publishLogEvent(sbLogEntries.toString(), LogConsts.SEND_FILTERED_LOG_ENTRIES, receivedCKN);
  }
  
  /*-------------------------------------------------------------------------*/
  private void saveLogs()
  {
    new Thread("MonitorFrame-SaveLogs")
    {
      @Override
      public void run()
      {
        saveLogsRunThread();
      }
    }.start();
  }
  
  void saveLogsRunThread()
  {
    LogReaderWriter vpLogReaderWriter = new LogFileReaderWriter();
    vpLogReaderWriter.writeLogs();
    String vsSavedPath = vpLogReaderWriter.writeLogs();
    logger.logOperation(LogConsts.OPR_DEVICE, "Logs Saved to: " + vsSavedPath);
  }

  /*-------------------------------------------------------------------------*/
  private void addLogEntry()
  {
    logger.logDebug(" ");
    logger.logOperation(LogConsts.OPR_USER, receivedText);
    logger.logDebug(" ");
  }
  
  /*-------------------------------------------------------------------------*/
  /**
   * Send the number of available log entries.  The received message is a
   * tab-delimited string specifying the name of the log, the text to find,
   * start entry number, entry field, direction of search, and a message description.
   * The returned log event contains the log name, entry count, and current log
   * sink entry number.
   */
  private void findText()
  {
    StringTokenizer stringTokenizer = new StringTokenizer(receivedText, "\t");
    String vsReceivedLogDescription = stringTokenizer.nextToken();
    String vsLogDescription = vsReceivedLogDescription;
    //
    // If requesting Communication or Equipment logs strip off the constant log
    // type text to get the log description that we need to find the logs.
    //
    if (vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.COMM_LOG_NAME));
    }
    else if (vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
    {
      vsLogDescription = vsLogDescription.substring(0, vsLogDescription.indexOf(LogConsts.EQUIPMENT_LOG_NAME));
    }
    String vsFindText = stringTokenizer.nextToken();
    //
    String vsLogEntryNumberStart = stringTokenizer.nextToken();
    Integer logEntryNumber = Integer.valueOf(vsLogEntryNumberStart);
    int viLogFirstEntryNumber = logEntryNumber.intValue();
    //
    String vsLogEntryField = stringTokenizer.nextToken();
    logEntryNumber = Integer.valueOf(vsLogEntryField);
    int viLogEntryField = logEntryNumber.intValue();
    //
    String vsDirection = stringTokenizer.nextToken();
    boolean down = true;
    if (vsDirection.equalsIgnoreCase("UP"))
    {
      down = false;
    }
    else if (vsDirection.equalsIgnoreCase("DOWN"))
    {
      down = true;
    }
    else
    {
      logger.logError("UNKNOWN Direction \"" + vsDirection + "\" findText()");
    }
    //
    LoggingDataAccess vlLog = (LoggingDataAccess)Logger.getLog(vsLogDescription);
    int viFoundEntry = vlLog.findText(vsFindText, viLogFirstEntryNumber,viLogEntryField, down);
    publishLogEvent(vsReceivedLogDescription + "\t" + String.valueOf(viFoundEntry),
                                       LogConsts.FIND_TEXT, receivedCKN);
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Factory for ControllerImplFactory.
   *
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>.  Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object.  If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   *
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    Controller vpController = new LogServer();
    return vpController;
  }
}