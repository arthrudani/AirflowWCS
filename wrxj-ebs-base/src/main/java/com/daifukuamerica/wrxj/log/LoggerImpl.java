package com.daifukuamerica.wrxj.log;

/**
 * An implementation of a logging sub-system (a logger). This class reads and writes
 * data to a LogEntry object which actually keeps the log record data. The
 * LoggerImpl saves one category of logs, such as System, Operation, etc..
 *
 * @author       Stephen Kendorski
 * @version 1.0
 */
public class LoggerImpl implements Log, LoggingDataAccess
{
  //
  // logs -- Our Log Entries.
  //
  private LogEntry logs = null;
  
  private String logName;
  private String logType;

  /*--------------------------------------------------------------------------*/
  /**
   * Create an instance of a logging sub-system.
   */
  public LoggerImpl()
  {
    // This cannot be done by the Factory because it may need to be called 
    // before the Factory is initialized.  If we ever have need to override
    // LogEntryImpl it will need to be done via a protected getter method.
    logs = new LogEntryImpl();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Create an instance of a logging sub-system with a specified maximum number
   * of logs.
   *
   * @param maxLogs the maximum entry count
   */
  public LoggerImpl(int maxLogs)
  {
    // This cannot be done by the Factory because it may need to be called 
    // before the Factory is initialized.  If we ever have need to override
    // LogEntryImpl it will need to be done via a protected getter method.
    logs = new LogEntryImpl(maxLogs);
  }

  /*--------------------------------------------------------------------------*/
  // LoggingDataAccess interface implementation.
  /*--------------------------------------------------------------------------*/
  /**
   * Return the number of log entries currently available.
   *
   * @return number of log entries
   */
  @Override
  public int getEntryCount()
  {
    return logs.getEntryCount();
  }
  /**
   * Return the number of log entries currently available.
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return number of log entries
   */
  @Override
  public int getEntryCount(String filterText, int filterIndex)
  {
    return logs.getEntryCount(filterText, filterIndex);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset.  If the caller's parameter "bAll" is false,
   * return a truncated String if the String length exceeds a pre-determined length.
   *
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @return the Object at entryNumber[field]
   */
  @Override
  public Object getValue(int entryNumber, int index, boolean bAll)
  {
    return logs.getValue(entryNumber, index, bAll);
  }
  
  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset.  If the caller's parameter "bAll" is false,
   * return a truncated String if the String length exceeds a pre-determined length.
   *
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @return the Object at entryNumber[field]
   */
 


@Override
public Object getValueSetDisplayFormat(int entryNumber, int index,
		boolean bAll, int displayFormat) {
	
		logs.setDataDisplayFormat(displayFormat);
	    return logs.getValue(entryNumber, index, bAll);
}
  
  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset.  If the caller's parameter "bAll" is false,
   * return a truncated String if the String length exceeds a pre-determined length.
   *
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return the Object at entryNumber[field]
   */
  @Override
  public Object getValue(int entryNumber, int index, boolean bAll, String filterText, int filterIndex)
  {
    return logs.getValue(entryNumber, index, bAll, filterText, filterIndex);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the number of the earliest entry that is still in the logger.  This
   * number can be greater than  the logger buffer capacity.
   *
   * @return first available log sequence number.
   */
  @Override
  public int getEarliestLogEntryNumber()
  {
    return logs.getEarliestLogEntryNumber();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the sequence number of the most recent available log.
   *
   * @return most recent available log sequence number.
   */
  @Override
  public int getLatestLogEntryNumber()
  {
    return logs.getLatestLogEntryNumber();
  }

  /**
   * Return the sequence number of the most recent available log.
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return most recent available log sequence number.
   */
  @Override
  public int getLatestLogEntryNumber(String filterText, int filterIndex)
  {
    return logs.getLatestLogEntryNumber(filterText, filterIndex);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the greatest number of records the logger will keep in RAM.  When
   * the maximum is reached the earliest entries are be over-written by the
   * newest entries.
   *
   * @return the maximum number of entries
   */
  @Override
  public int getMaxLogEntries()
  {
    return logs.getMaxLogEntries();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @return the offset
   */
  @Override
  public int getLogSinkEntryNumber()
  {
    return logs.getLogSinkEntryNumber();
  }

  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return the offset
   */
  @Override
  public int getLogSinkEntryNumber(String filterText, int filterIndex)
  {
    return logs.getLogSinkEntryNumber(filterText, filterIndex);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return <i>true</i> if additional logs exist.  If the current logging
   * sequence number is different than that in the caller's passed in parameter
   * return <i>true</i>, otherwise return <i>false</i>.
   *
   * @param callersLatestLogEntryNumber most recent log sequence number known
   * @return result of additional logs
   */
  @Override
  public boolean newLogsAvailable(int callersLatestLogEntryNumber)
  {
    return (callersLatestLogEntryNumber != logs.getLatestLogEntryNumber());
  }

  /*--------------------------------------------------------------------------*/
  // Log interface implementation.
  /*--------------------------------------------------------------------------*/
  /**
   * Specify if logger use needs to be <i>synchronized</i>.
   *
   * @param needed if true, synchronize
   */
  @Override
  public void setLogSyncNeeded(boolean needed)
  {
    logs.setLogSyncNeeded(needed);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify the greatest number of records the logger will keep in RAM.  When
   * the maximum is reached the earliest entries will be over-written by the
   * newest entries.
   *
   * @param maxEntries the maximum number of entries
   */
  @Override
  public void setMaxLogEntries(int maxEntries)
  {
    logs.setMaxLogEntries(maxEntries);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save a Debug text message.
   *
   * @param logText the text
   */
  @Override
  public void logDebug(String logText)
  {
    logs.addDebugLogEntry("Debug", null, 0, logText);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save a Debug text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param logText the text message
   */
  @Override
  public void logDebug(String name, int key, String logText)
  {
    logs.addDebugLogEntry("Debug", name, key, logText);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save Error text as a Debug text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param logText the Error text
   */
  @Override
  public void logDebugError(String name, int key, String logText)
  {
    logs.addDebugLogEntry("Error", name, key, " ##### " + logText + " #####");
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save Operation text as a Debug text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param logText the Operation text
   * @param type the Operation category
   */
  @Override
  public void logDebugOperation(String name, int key,
                         String logText, int type)
  {
    logs.addDebugLogEntry(LogConsts.OPR_LOG_TYPE_NAMES[type],
        name, key, logText);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a Error text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param logText the text message
   */
  @Override
  public void logError(String name, int key, String logText)
  {
    logs.addErrorLogEntry(name, key, logText, 0);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save an Error text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param logText the Error text message
   * @param errorNumber the error classification
   */
  @Override
  public void logError(String name, int key, String logText, int errorNumber)
  {
    logs.addErrorLogEntry(name, key, logText, errorNumber);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save an inter-process-communication in-coming message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param logText the text message
   * @param msgType the message classification
   */
  @Override
  public void logReceivedMessage(String name, int key, String logText, String msgType)
  {
    logs.addSystemLogEntry(msgType, name, key, logText,
                        false);//LogConsts.COM_LOG_RX_DATA);// " --->" + name);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save an inter-process-communication out-going message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param logText the text message
   * @param msgType the message classification
   */
  @Override
  public void logTransmittedMessage(String name, int key, String logText, String msgType)
  {
    logs.addSystemLogEntry(msgType, name, key, logText,
                        true);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a data packet received by a communication port.
   *
   * @param byteArray the data bytes
   * @param offset the index to the first data byte to record
   * @param count the number of data bytes to record
   */
  @Override
  public void logRxByteCommunication(byte[] byteArray, int offset, int count)
  {
    logs.addCommByteLogEntry(byteArray, offset, count, false);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save a data packet transmitted by a communication port.
   *
   * @param byteArray the data bytes
   * @param offset the index to the first data byte to record
   * @param count the number of data bytes to record
   */
  @Override
  public void logTxByteCommunication(byte[] byteArray, int offset, int count)
  {
    logs.addCommByteLogEntry(byteArray, offset, count, true);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a data message received by an equipment's device.
   *
   * @param entryData the data message
   * @param clarifier the interpreted data
   */
  @Override
  public void logRxEquipmentMessage(String entryData, String clarifier)
  {
    logs.addEquipmentLogEntry(entryData, clarifier, false);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save a data message transmitted by an equipment's device.
   *
   * @param entryData the data message
   * @param clarifier the interpreted data
   */
  @Override
  public void logTxEquipmentMessage(String entryData, String clarifier)
  {
    logs.addEquipmentLogEntry(entryData, clarifier, true);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a Operation text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param inLogType the category
   * @param isLogText the string message
   */
  @Override
  public void logOperation(String name, int key, String isLogText, int inLogType)
  {
    logs.addOperationLogEntry(LogConsts.OPR_LOG_TYPE_NAMES[inLogType],
        name, key, null, isLogText);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save a Operation text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param inLogType the category
   * @param logKey the entry search key
   * @param logText the string message
   */
  @Override
  public void logOperation(String name, int key, String logKey, String logText, int inLogType)
  {
    if (logKey == null)
    {
      logKey = " ";
    }
    logs.addOperationLogEntry(LogConsts.OPR_LOG_TYPE_NAMES[inLogType],
        name, key, logKey, logText);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Save a complete pre-existing log record.  This is used when logs are read
   * from a file.
   *
   * @param o the log entry
   */
  @Override
  public void addLogEntry(Object o)
  {
    logs.addLogEntry(o);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the number of the log entry that contains the search text,
   * -1 if search text not found, -2 if do nothing..
   *
   * @param findText pattern match to apply to records
   * @param startEntry number of entry where search begins
   * @param findIndex the offset to the field within the record
   * @param down if true, search by descending log entries
   * @return number of log entry that has data match
   */
  @Override
  public int findText(String findText, int startEntry, int findIndex, boolean down)
  {
    return logs.findText(findText, startEntry, findIndex, down);
  }

  @Override
  public void setLogName(String isName)
  {
    logName = isName;
  }
  @Override
  public String getLogName()
  {
    return logName;
  }
  
  @Override
  public void setLogType(String isType)
  {
    logType = isType;
  }
  @Override
  public String getLogType()
  {
    return logType;
  }

}