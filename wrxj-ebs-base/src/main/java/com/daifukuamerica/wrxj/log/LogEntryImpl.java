package com.daifukuamerica.wrxj.log;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.util.BoyerMoorePatternMatch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Stephen Kendorski
 * @version 1.0
 */
public class LogEntryImpl implements LogEntry
{
  /**
   * LogEntryData is the class defining the data content of a single log entry.
   */
  class LogEntryData
  {
    int logEntryDataType = LogConsts.NOT_COM_LOG_DATA;
    byte[] byteArray = null;
    Object[] objectArray = new Object[LogConsts.LOG_ENTRY_MAX_FIELDS];
  }

  /**
   * logEntries -- Our Collection of Log Entries. We use an array because of
   * speed requirements.  We do not need to endlessly grow the collection as
   * the newest entries can overwrite the oldest entry once the array is full.
   * All log entries should be persisted (to disk) by a mechanism external to
   * this class.
   */
  //  private List LogEntries = new ArrayList(); //initial size is default
  private LogEntryData[] logEntries = null;
  private int            maxLogEntries = LogConsts.MAX_LOGS;
  private int            currentLogCount = 0;
  private int            logSinkIndex = 0;
  private int            logEntryNumber = 0;
  private boolean        LogSyncNeeded = true;
  private final Object   LogLock = new Object();
  private boolean        mzLogDebug = false;

  private SkDateTime     dateTime = new SkDateTime(LogConsts.LOG_ENTRY_DATE_TIME_FORMAT);
  
  private boolean filterActive = false;
  private String filterText = null;
  private int filterIndex = -1;
  private LogEntryData[] filteredLogEntries = null;
  private int filteredLogCount = 0;
  private int filteredLogSinkIndex = 0;
  private int filteredLogEntryNumber = 0;
  private List<BoyerMoorePatternMatch> filterMatchDataList = new ArrayList<BoyerMoorePatternMatch>();

  /*--------------------------------------------------------------------------*/
  public LogEntryImpl()
  {
    init();
  }

  /*--------------------------------------------------------------------------*/
  public LogEntryImpl(int inMaxLogEntries)
  {
    maxLogEntries = inMaxLogEntries;
    init();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify the greatest number of records the logger will keep in RAM.  When
   * the maximum is reached the earliest entries will be over-written by the
   * newest entries.
   *
   * @param inMaxLogEntries the maximum number of entries
   */
  public void setMaxLogEntries(int inMaxLogEntries)
  {
    if (inMaxLogEntries != maxLogEntries)
    {
      LogEntryData[] newLogEntries = new LogEntryData[inMaxLogEntries];
      if (inMaxLogEntries <= maxLogEntries)
      {
        // We are shrinking - just copy what will fit.  Starting from where...?
        System.arraycopy(logEntries, 0, newLogEntries, 0, inMaxLogEntries);
        logEntries = newLogEntries;
      }
      else
      {
        // We are growing - copy what we have.
        System.arraycopy(logEntries, 0, newLogEntries, 0, maxLogEntries);
        logEntries = newLogEntries;
      }
      maxLogEntries = inMaxLogEntries;
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify if log accesses need to be <i>synchronized</i>.
   *
   * @param needed if true, synchronize
   */
  public void setLogSyncNeeded(boolean needed)
  {
    LogSyncNeeded = needed;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a Debug text message for the named sub-system.
   *
   * @param entryType message classification
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param text the text message
   */
  public void addDebugLogEntry(String entryType, String name, int key, String text)
  {
    if (mzLogDebug)
    {
      LogEntryData lLogEntry = new LogEntryData();
      if (name != null)
      {
        lLogEntry.objectArray[LogConsts.LOG_ENTRY_DIRECTION_IDX] =  "       " + name;
      }
      else
      {
        lLogEntry.objectArray[LogConsts.LOG_ENTRY_DIRECTION_IDX] =  "       **UNKNOWN**";
      }
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_CTLRKEY_IDX] = Integer.valueOf(key);
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_MSG_TYPE_IDX] = entryType;
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_MSG_DESC_IDX] = text;
      //
      // Only take the time to synchronize when absolutely necessary.  And then,
      // minimize the amount of work done.
      //
      if (LogSyncNeeded)
      {
        synchronized(LogLock)
        {
          finishAddingLogEntry(lLogEntry);
        }
      }
      else
      {
          finishAddingLogEntry(lLogEntry);
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save an Error text message for the named sub-system.
   *
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param text the Error text message
   * @param errorNumber the error classification
   */
  public void addErrorLogEntry(String name, int key, String text, int errorNumber)
  {
    LogEntryData lLogEntry = new LogEntryData();
    if (name != null)
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_DEVICE_IDX] =  name;
    }
    else
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_DEVICE_IDX] =  "";
    }
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_CTLRKEY_IDX] = Integer.valueOf(key);
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_ERROR_CODE_IDX] = Integer.valueOf(errorNumber);
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_ERROR_DESC_IDX] = text;
    //
    // Only take the time to synchronize when absolutely necessary.  And then,
    // minimize the amount of work done.
    //
    if (LogSyncNeeded)
    {
      synchronized(LogLock)
      {
        finishAddingLogEntry(lLogEntry);
      }
    }
    else
    {
        finishAddingLogEntry(lLogEntry);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save an inter-process-communication in-coming message for the named sub-system.
   *
   * @param entryType message classification
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param text the text message
   * @param transmission if true, out-going message; if false, in-coming message
   */
  public void addSystemLogEntry(String entryType, String name, int key, String text, boolean transmission)
  {
    LogEntryData lLogEntry = new LogEntryData();
    if (transmission)
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_DIRECTION_IDX] = "       " + name + "--->";
    }
    else
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_DIRECTION_IDX] = " --->" + name;
    }
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_CTLRKEY_IDX] = Integer.valueOf(key);
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_MSG_TYPE_IDX] = entryType;
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_MSG_DESC_IDX] = text;
    //
    // Only take the time to synchronize when absolutely necessary.  And then,
    // minimize the amount of work done.
    //
    if (LogSyncNeeded)
    {
      synchronized(LogLock)
      {
        finishAddingLogEntry(lLogEntry);
      }
    }
    else
    {
        finishAddingLogEntry(lLogEntry);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a data packet received by a communication port.
   *
   * @param byteArray the data bytes
   * @param offset the index to the first data byte to record
   * @param count the number of data bytes to record
   * @param transmission if true, out-going message; if false, in-coming message
   */
  public void addCommByteLogEntry(byte[] byteArray, int offset, int count, boolean transmission)
  {
    LogEntryData lLogEntry = new LogEntryData();
    lLogEntry.byteArray = new byte[count];
    System.arraycopy(byteArray, offset, lLogEntry.byteArray, 0, count);
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_DATA_COUNT_IDX] = Integer.valueOf(count);
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_TX_DATA_IDX] = "";
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_RX_DATA_IDX] = "";
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_CTLRKEY_IDX] = Integer.valueOf(0);
    if (transmission)
    {
      lLogEntry.logEntryDataType = LogConsts.COM_LOG_TX_DATA;
    }
    else
    {
      lLogEntry.logEntryDataType = LogConsts.COM_LOG_RX_DATA;
    }
    //
    // Only take the time to synchronize when absolutely necessary.  And then,
    // minimize the amount of work done.
    //
    if (LogSyncNeeded)
    {
      synchronized(LogLock)
      {
        finishAddingLogEntry(lLogEntry);
      }
    }
    else
    {
        finishAddingLogEntry(lLogEntry);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a message received by an equipment device.
   *
   * @param text the data message
   * @param clarifier the interpreted data
   * @param transmission if true, out-going message; if false, in-coming message
   */
  public void addEquipmentLogEntry(String text, String clarifier, boolean transmission)
  {
    LogEntryData lLogEntry = new LogEntryData();
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_DATA_COUNT_IDX] = Integer.valueOf(text.length());
    if (transmission)
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_TX_DATA_IDX] = clarifier + "  -  \n\n\"" + text + "\"";
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_RX_DATA_IDX] = "";
    }
    else
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_TX_DATA_IDX] = "";
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_RX_DATA_IDX] = clarifier + "  -  \n\n\"" + text + "\"";
    }
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_CTLRKEY_IDX] = Integer.valueOf(0);
    //
    // Only take the time to synchronize when absolutely necessary.  And then,
    // minimize the amount of work done.
    //
    if (LogSyncNeeded)
    {
      synchronized(LogLock)
      {
        finishAddingLogEntry(lLogEntry);
      }
    }
    else
    {
        finishAddingLogEntry(lLogEntry);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a Operation text message for the named sub-system.
   *
   * @param entryType the category
   * @param entryKey the search key
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param text the string message
   */
  public void addOperationLogEntry(String entryType, String name, int key, String entryKey, String text)
  {
    LogEntryData lLogEntry = new LogEntryData();
    if (name != null)
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_USER_IDX] =  name;
    }
    else
    {
      lLogEntry.objectArray[LogConsts.LOG_ENTRY_USER_IDX] =  "**UNKNOWN**";
    }
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_CTLRKEY_IDX] = Integer.valueOf(key);
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_OPR_TYPE_IDX] = entryType;
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_OPR_KEY_IDX] = entryKey;
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_OPR_DESC_IDX] = text;
    //
    // Only take the time to synchronize when absolutely necessary.  And then,
    // minimize the amount of work done.
    //
    if (LogSyncNeeded)
    {
      synchronized(LogLock)
      {
        finishAddingLogEntry(lLogEntry);
      }
    }
    else
    {
        finishAddingLogEntry(lLogEntry);
    }
  }

  /**
   * Save a Operation text message for the named sub-system.
   *
   * @param entryType the category
   * @param name the sub-system's name
   * @param key the unique identifier for the named sub-system
   * @param text the string message
   */
  public void addOperationLogEntry(String entryType, String name, int key, String text)
  {
    addOperationLogEntry(entryType, name, key, " ", text);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Save a complete pre-existing log record.  This is used when logs are read
   * from a file.
   *
   * @param o the log entry
   */
  public void addLogEntry(Object o)
  {
    LogEntryData lLogEntry = new LogEntryData();
    lLogEntry.objectArray = (Object[])o;
    //
    if (currentLogCount >= maxLogEntries)
    {
      set(logSinkIndex, lLogEntry);  // Just replace - no free needed.
    }
    else
    {
      add(lLogEntry);
    }
    logSinkIndex++;
    if (logSinkIndex >= maxLogEntries)
    {
      logSinkIndex = 0;
    }
    getNextLogEntryNumber();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the greatest number of records the logger will keep in RAM.  When
   * the maximum is reached the earliest entries are be over-written by the
   * newest entries.
   *
   * @return the maximum number of entries
   */
  public int getMaxLogEntries()
  {
    return maxLogEntries;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the number of log entries currently available.
   *
   * @return number of log entries
   */
  public int getEntryCount()
  {
    return currentLogCount;
  }

  /**
   * Return the number of log entries currently available.
   *
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return number of log entries
   */
  public int getEntryCount(String isFilterText, int inFilterIndex)
  {
    if ((!filterActive) ||
        (!isFilterText.equals(filterText)) ||
        (inFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, inFilterIndex); 
    }
    if (filterActive)
    {
      return filteredLogCount;
    }
    else
    {
      return getEntryCount();
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the number of the earliest entry that is still in the logger.  This
   * number can be greater than  the logger buffer capacity.
   *
   * @return first available log sequence number.
   */
  public int getEarliestLogEntryNumber()
  {
    int result = 0;
    if (logEntryNumber >= maxLogEntries)
    {
      result = (logEntryNumber - maxLogEntries) + 1;
    }
    return result;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the sequence number of the most recent available log.
   *
   * @return most recent available log sequence number.
   */
  public int getLatestLogEntryNumber()
  {
    return logEntryNumber;
  }

  /**
   * Return the sequence number of the most recent available log.
   *
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return most recent available log sequence number.
   */
  public int getLatestLogEntryNumber(String isFilterText, int inFilterIndex)
  {
    if ((!filterActive) ||
        (!isFilterText.equals(filterText)) ||
        (inFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, inFilterIndex); 
    }
    if (filterActive)
    {
      return filteredLogEntryNumber;
    }
    else
    {
      return getLatestLogEntryNumber();
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @return the offset
   */
  public int getLogSinkEntryNumber()
  {
    return logSinkIndex;
  }

  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return the offset
   */
  public int getLogSinkEntryNumber(String isFilterText, int inFilterIndex)
  {
    if ((!filterActive) ||
        (!isFilterText.equals(filterText)) ||
        (inFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, inFilterIndex); 
    }
    if (filterActive)
    {
      return filteredLogSinkIndex;
    }
    else
    {
      return getLogSinkEntryNumber();
    }
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
  public Object getValue(int entryNumber, int index, boolean bAll)
  {
    LogEntryData logEntry = getLogEntryNormalized(entryNumber, false);
    if (logEntry == null)
    {
      return "Problem!";//...?
    }
    else
    {
      return getFieldValue(logEntry, index, bAll);
    }
  }

  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset.  If the caller's parameter "bAll" is false,
   * return a truncated String if the String length exceeds a pre-determined length.
   *
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return the Object at entryNumber[field]
   */
  public Object getValue(int entryNumber, int index, boolean bAll, String isFilterText, int inFilterIndex)
  {
    if ((!filterActive) ||
        (!isFilterText.equals(filterText)) ||
        (inFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, inFilterIndex); 
    }
    if (filterActive)
    {
      LogEntryData logEntry = getLogEntryNormalized(entryNumber, true);
      if (logEntry == null)
      {
        return "Problem!";//...?
      }
      else
      {
        return getFieldValue(logEntry, index, bAll);
      }
    }
    else
    {
      return getValue(entryNumber, index, bAll);
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Return the number of the log entry that contains the search text,
   * -1 if search text not found, -2 if do nothing.
   *
   * @param findText pattern match to apply to records
   * @param startEntry number of entry where search begins
   * @param findIndex the offset to the field within the record
   * @param down if true, search by descending log entries
   * @return number of log entry that has data match
   */
  public int findText(String findText, int startEntry, int findIndex, boolean down)
  {
    int viResult = -1;
    if (findText.length() == 0)
    {
      return viResult;
    }
    List<BoyerMoorePatternMatch> vpFindMatchDataList = new ArrayList<BoyerMoorePatternMatch>();
    if (findText.indexOf('|') != -1)
    {
      //
      // We have an OR-ed search - get the individual find strings.
      //
      StringTokenizer vpStringTokenizer = new StringTokenizer(findText, "|");
      while (vpStringTokenizer.hasMoreElements())
      {
        boolean vbCaseSensitive = false;
        String vsText = vpStringTokenizer.nextToken();
        if (vsText.indexOf('^') == 0)
        {
          //
          // A leading caret (^) says find IS case sensitive.
          // Lose the caret.
          //
          vsText = vsText.substring(1);
          vbCaseSensitive = true;
        }
        BoyerMoorePatternMatch vpMatchData = new BoyerMoorePatternMatch(vsText, vbCaseSensitive);
        vpFindMatchDataList.add(vpMatchData);
      }
    }
    else
    {
      boolean vbCaseSensitive = false;
      if (findText.indexOf('^') == 0)
      {
        //
        // A leading caret (^) says find IS case sensitive.
        // Lose the caret.
        //
        findText = findText.substring(1);
        vbCaseSensitive = true;
      }
      BoyerMoorePatternMatch vpMatchData = new BoyerMoorePatternMatch(findText, vbCaseSensitive);
      vpFindMatchDataList.add(vpMatchData);
    }
    int viLogEntryIndex = startEntry;
    if (filterActive)
    {
      if (viLogEntryIndex >= filteredLogCount)
      {
        viLogEntryIndex = filteredLogCount - 1;
      }
    }
    else
    {
      if (viLogEntryIndex >= currentLogCount)
      {
        viLogEntryIndex = currentLogCount - 1;
      }
    }
    int viLogSinkIndex = logSinkIndex;
    if (filterActive)
    {
      viLogSinkIndex = filteredLogSinkIndex;
      if (viLogEntryIndex == -1)
      {
        if (down)
        {
          if (filteredLogEntryNumber >= maxLogEntries)
          {
            viLogEntryIndex = (filteredLogEntryNumber + 1) % maxLogEntries;
          }
          else
          {
            viLogEntryIndex = 0;
          }
        }
        else
        {
          if (filteredLogEntryNumber >= maxLogEntries)
          {
            viLogEntryIndex = filteredLogEntryNumber % maxLogEntries;
          }
          else
          {
            viLogEntryIndex = filteredLogEntryNumber;
          }
        }
      }
    }
    else
    {
      if (viLogEntryIndex == -1)
      {
        if (down)
        {
          if (logEntryNumber >= maxLogEntries)
          {
            viLogEntryIndex = (logEntryNumber + 1) % maxLogEntries;
          }
          else
          {
            viLogEntryIndex = 0;
          }
        }
        else
        {
          if (logEntryNumber >= maxLogEntries)
          {
            viLogEntryIndex = logEntryNumber % maxLogEntries;
          }
          else
          {
            viLogEntryIndex = logEntryNumber;
          }
        }
      }
    }
    if (down && (viLogEntryIndex == viLogSinkIndex))
    {
      return viResult;
    }
    Object vpFieldObject = null;
    String vsFieldText = null;
    do
    {
      if (filterActive)
      {
        LogEntryData logEntry = getLogEntryNormalized(viLogEntryIndex, true);
        vpFieldObject = getFieldValue(logEntry, findIndex, true);
      }
      else
      {
        vpFieldObject = getValue(viLogEntryIndex, findIndex, true);
      }
      try
      {
        vsFieldText = (String)vpFieldObject;
      }
      catch (ClassCastException e)
      {
        Integer vnFieldInteger = (Integer)vpFieldObject;
        vsFieldText = vnFieldInteger.toString();
      }
      Iterator<BoyerMoorePatternMatch> vpIterator = vpFindMatchDataList.iterator();
      while (vpIterator.hasNext())
      {
        BoyerMoorePatternMatch vpPatternMatch = vpIterator.next();
        if (vpPatternMatch.matches(vsFieldText))
        {
          viResult = viLogEntryIndex;
          break;
        }
      }
      if (viResult != -1)
      {
        break;
      }
      //
      if (down)
      {
        viLogEntryIndex++;
        if (viLogEntryIndex >= maxLogEntries)
        {
          viLogEntryIndex = 0;
        }
      }
      else
      {
        viLogEntryIndex--;
        if (viLogEntryIndex < 0)
        {
          if (logEntryNumber >= maxLogEntries)
          {
            viLogEntryIndex = maxLogEntries - 1;
          }
          else
          {
            break;
          }
        }
      }
    } while (viLogEntryIndex != viLogSinkIndex);
    return viResult;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void finishAddingLogEntry(LogEntryData lLogEntry)
  {
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_TIMESTAMP_IDX] = dateTime.getCurrentDateTimeAsLong();
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_TIMESTAMP_STRING_IDX] = null;
    lLogEntry.objectArray[LogConsts.LOG_ENTRY_NUMBER_IDX] = Integer.valueOf(logEntryNumber + 1);
    if (logEntryNumber >= maxLogEntries)
    {
      set(logSinkIndex, lLogEntry);  // Just replace - no free needed.
    }
    else
    {
      add(lLogEntry);
    }
    logSinkIndex++;
    if (logSinkIndex >= maxLogEntries)
    {
      logSinkIndex = 0;
    }
    getNextLogEntryNumber();
  }

  /*--------------------------------------------------------------------------*/
  private void add(LogEntryData ourLogEntry)
  {
//  logEntries.add(ourlogEntry);  // use for List
    logEntries[currentLogCount] = ourLogEntry;
    if (currentLogCount < maxLogEntries)
    {
      currentLogCount++;
    }
    if (filterActive)
    {
      String fieldText = getFieldValue(ourLogEntry, filterIndex, true);
      if (filterTextMatch(fieldText))
      {
        filteredLogEntries[filteredLogCount] = ourLogEntry;
        if (filteredLogCount < maxLogEntries)
        {
          filteredLogCount++;
        }
        filteredLogSinkIndex++;
        if (filteredLogSinkIndex >= maxLogEntries)
        {
          filteredLogSinkIndex = 0;
        }
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  private void set(int index, LogEntryData ourLogEntry)
  {
//  logEntries.set(index, ourlogEntry);  // use for List Just replace - no free needed.
    logEntries[index % maxLogEntries] = ourLogEntry;
    if (filterActive)
    {
      String fieldText = getFieldValue(ourLogEntry, filterIndex, true);
      if (filterTextMatch(fieldText))
      {
        filteredLogEntries[filteredLogSinkIndex % maxLogEntries] = ourLogEntry;
        filteredLogSinkIndex++;
        if (filteredLogSinkIndex >= maxLogEntries)
        {
          filteredLogSinkIndex = 0;
        }
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  private int getNextLogEntryNumber()
  {
    return ++logEntryNumber; // Starts at 0 - pre-increment.
  }

  private void init()
  {
    logEntries = new LogEntryData[maxLogEntries];
    mzLogDebug = Application.getBoolean("LogDebugMessages", true);
  }

  /*--------------------------------------------------------------------------*/
  private String getFieldValue(LogEntryData logEntryData, int index, boolean bAll)
  {
    Object oResult = logEntryData.objectArray[index];
    //
    // If this is the Timestamp String we only want to format it once.
    //
    if ((index == LogConsts.LOG_ENTRY_TIMESTAMP_STRING_IDX) &&
       (logEntryData.objectArray[LogConsts.LOG_ENTRY_TIMESTAMP_STRING_IDX] == null))
    {
      Long longDateTime = (Long)logEntryData.objectArray[LogConsts.LOG_ENTRY_TIMESTAMP_IDX];
      logEntryData.objectArray[LogConsts.LOG_ENTRY_TIMESTAMP_STRING_IDX] = dateTime.getlongDateTimeAsString(longDateTime.longValue());
      oResult = logEntryData.objectArray[index];
    }
    else
    {
      if (logEntryData.logEntryDataType == LogConsts.NOT_COM_LOG_DATA)
      {
        oResult = logEntryData.objectArray[index];
      }
      else
      {
        //
        // This is a COMM log entry.
        //
        if (index == LogConsts.LOG_ENTRY_TX_DATA_IDX)
        {
          if (logEntryData.logEntryDataType == LogConsts.COM_LOG_TX_DATA)
          {
            oResult = formatDisplayString(logEntryData, bAll);
          }
        }
        else
        {
          if (index == LogConsts.LOG_ENTRY_RX_DATA_IDX)
          {
            if (logEntryData.logEntryDataType == LogConsts.COM_LOG_RX_DATA)
            {
              oResult = formatDisplayString(logEntryData, bAll);
            }
          }
        }
      }
    }
    return ((oResult != null) ? oResult.toString() : null);
  }

  /*--------------------------------------------------------------------------*/
  /* Get a Log Entry by its Index (>=0)
  */
  private LogEntryData getLogEntryNormalized(int index, boolean filter)
  {
    if (filter)
    {
      if (index >= maxLogEntries)
      {
        index = index % maxLogEntries;
      }
      return filteredLogEntries[index];
    }
    else
    {
      if (index >= maxLogEntries)
      {
        index = index % maxLogEntries;
      }
      return logEntries[index];
    }
  }

  /*--------------------------------------------------------------------------*/
    int dataDisplayFormat = 2;
    static final String[] ASCII_HEX_DIGITS = {
                 "0", "1", "2", "3", "4", "5", "6", "7",
                 "8", "9", "A", "B", "C", "D", "E", "F"};
    static final String[] ASCII_CONTROL = {
      "<NUL>", "<SOH>", "<STX>", "<ETX>", "<EOT>", "<ENQ>", "<ACK>", "<BEL>",
      "<BS>",  "<HT>",  "<LF>",  "<VT>",  "<FF>",  "<CR>",  "<SO>",  "<SI>",
      "<DLE>", "<DC2>", "<DC2>", "<DC3>", "<DC4>", "<NAK>", "<SYN>", "<ETB>",
      "<CAN>", "<EM>",  "<SUB>", "<ESC>", "<FS>",  "<GS>",  "<RS>",  "<US>"};
  /*--------------------------------------------------------------------------*/
    
    /*--------------------------------------------------------------------------*/
    /**
     * Specify data Display Format </i>.
     *
     * @param values LogConsts.ASCII_DISPLAY_FORMAT, LogConsts.HEX_DISPLAY_FORMAT, LogConsts.ASCII_CONTROL_DISPLAY_FORMAT
     */
    public void setDataDisplayFormat(int format)
    {
    	switch( format )
    	{
    	  case LogConsts.ASCII_DISPLAY_FORMAT:
    		dataDisplayFormat = format;
    		break;
    	  case LogConsts.HEX_DISPLAY_FORMAT:
      		dataDisplayFormat = format;
    		break;
    	  case LogConsts.ASCII_CONTROL_DISPLAY_FORMAT:
      		dataDisplayFormat = format;
    		break;
      		default:
      			dataDisplayFormat = LogConsts.ASCII_CONTROL_DISPLAY_FORMAT;
    	}
    }
  
  /**
   * Calculates data display format by searching the given binary.
   * This method is a quick fix for STX / ETX consideration.
   * 
   * @param buf byte[] to check
   * @return LogConsts.HEX_DISPLAY_FORMAT if there are unprintable byte, or LogConsts.ASCII_CONTROL_DISPLAY_FORMAT
   */
  private int calculateDataDisplayFormat(byte[] buf) {
      for(byte b : buf) {
          if (b < 0x20 || 0x7e < b ) {
              return LogConsts.HEX_DISPLAY_FORMAT;
          }
      }
      
      return LogConsts.ASCII_CONTROL_DISPLAY_FORMAT;
  }
    
  private String formatDisplayString(LogEntryData logEntryData, boolean bAll)
  {
    setDataDisplayFormat(calculateDataDisplayFormat(logEntryData.byteArray));
    StringBuffer tmpStringBfr;
    Integer iBCount = (Integer)logEntryData.objectArray[LogConsts.LOG_ENTRY_DATA_COUNT_IDX];
    int iByteCount = iBCount.intValue();
    boolean izPrintBytes = false;
    if (iByteCount > 10000)
    {
      iByteCount = 10000;
    }
    if ((!bAll) && (iByteCount > 100))
    {
      iByteCount = 100;
    }
    
    byte byte0 = logEntryData.byteArray[0];
    if( byte0 == 0x00 )
    {
    	izPrintBytes = true;
    }
    
    switch (dataDisplayFormat)
    {
      case LogConsts.ASCII_DISPLAY_FORMAT: //-------------------------------
      {
        tmpStringBfr = new StringBuffer(iByteCount + 2);
        tmpStringBfr.append("\"");
        for (int i = 0; i < iByteCount; i++)
        {
          byte aByte = logEntryData.byteArray[i];
          if ((aByte < 0x20) || (aByte > 0x7e))
          {
            try
            {
              //
              // ">>>" is supposed to zero-fill, but it sign-fills - mask lower 4-bits.
              //
              tmpStringBfr.append("[" + ASCII_HEX_DIGITS[(aByte >>> 4) & 0x0f] +
                               ASCII_HEX_DIGITS[aByte & 0x0f] + "]");
            }
            catch(ArrayIndexOutOfBoundsException e)
            {
              tmpStringBfr.append("[" +
                               ASCII_HEX_DIGITS[(aByte >>> 12) & 0x0f] +
                               ASCII_HEX_DIGITS[(aByte >>> 8) & 0x0f] +
                               ASCII_HEX_DIGITS[(aByte >>> 4) & 0x0f] +
                               ASCII_HEX_DIGITS[aByte & 0x0f] + "]");
            }
          }
          else
          {
            tmpStringBfr.append((char)aByte);
          }
        }
        tmpStringBfr.append("\"");
      }
      break;
      case LogConsts.HEX_DISPLAY_FORMAT: //---------------------------------
      {
        tmpStringBfr = new StringBuffer((iByteCount * 3) + 1);
        for (int i = 0; i < iByteCount; i++)
        {
          byte aByte = logEntryData.byteArray[i];
          tmpStringBfr.append(ASCII_HEX_DIGITS[(aByte >>> 4) & 0x0f] +
                             ASCII_HEX_DIGITS[aByte & 0x0f] + " ");
        }
        tmpStringBfr.setLength((iByteCount * 3) - 1); // lose trailing space
      }
      break;
      case LogConsts.ASCII_CONTROL_DISPLAY_FORMAT: //-----------------------
      {
        tmpStringBfr = new StringBuffer(iByteCount + 2);
        tmpStringBfr.append("\"");
        for (int i = 0; i < iByteCount; i++)
        {
          byte aByte = logEntryData.byteArray[i];
          //
          // Java bytes are signed (-128 to 127)
          //
          if ((aByte >= 0x00) && (aByte < 0x20))
          {
           // MCM BCS EBS test ------------------------------------------------------------------------------------------
        	  
        	 // tmpStringBfr.append(ASCII_CONTROL[aByte]);
        	  if( izPrintBytes )
        	  {
        	  tmpStringBfr.append( String.format("%2d  ," , aByte));
        	  }
        	  else
        	  {
        		  tmpStringBfr.append(ASCII_CONTROL[aByte]);
              }
          }
          else
          {
        	  if( izPrintBytes )
        	  {
        	      tmpStringBfr.append( String.format("%2d  ," , aByte));
        	  }
        	  else
        	  {
            if (aByte == 0x7f)
            {
              tmpStringBfr.append("<DEL>");
            }
            else
            {
              if (aByte < 0x00)
              {
                tmpStringBfr.append("[" + ASCII_HEX_DIGITS[(aByte >>> 4) & 0x0f] +
                             ASCII_HEX_DIGITS[aByte & 0x0f] + "]");
              }
              else
              {
                tmpStringBfr.append((char)aByte);
              }
            }
        	  }
          }
        }
        tmpStringBfr.append("\"");
      }
      break;
      default:
      {
        tmpStringBfr = new StringBuffer("Keeps Compiler Happy");
      }
    }
    return tmpStringBfr.toString();
  }

  /*--------------------------------------------------------------------------*/
  private void initializeFilter(String isFilterText, int inFilterIndex)
  {
    filterText = isFilterText;
    filterIndex = inFilterIndex;
    if (isFilterText.length() == 0)
    {
      filteredLogEntries = null;
      return;
    }
    filterMatchDataList.clear();
    if (isFilterText.indexOf('|') != -1)
    {
      //
      // We have an OR-ed search - get the individual filter strings.
      //
      StringTokenizer vpStringTokenizer = new StringTokenizer(isFilterText, "|");
      while (vpStringTokenizer.hasMoreElements())
      {
        boolean vbCaseSensitive = false;
        String vsText = vpStringTokenizer.nextToken();
        if (vsText.indexOf('^') == 0)
        {
          //
          // A leading caret (^) says filter IS case sensitive.
          // Lose the caret.
          //
          vsText = vsText.substring(1);
          vbCaseSensitive = true;
        }
        BoyerMoorePatternMatch vpMatchData = new BoyerMoorePatternMatch(vsText, vbCaseSensitive);
        filterMatchDataList.add(vpMatchData);
      }
    }
    else
    {
      boolean vbCaseSensitive = false;
      if (isFilterText.indexOf('^') == 0)
      {
        //
        // A leading caret (^) says filter IS case sensitive.
        // Lose the caret.
        //
        isFilterText = isFilterText.substring(1);
        vbCaseSensitive = true;
      }
      BoyerMoorePatternMatch vpMatchData = new BoyerMoorePatternMatch(isFilterText, vbCaseSensitive);
      filterMatchDataList.add(vpMatchData);
    }
    filterActive = true;
    filteredLogEntries = new LogEntryData[logEntries.length];
    int viLogEntryIndex = 0;
    int viLogSinkIndex = logSinkIndex;
    if (logEntryNumber >= maxLogEntries)
    {
      viLogEntryIndex = (logEntryNumber + 1) % maxLogEntries;
    }
    filteredLogCount = 0;
    filteredLogEntryNumber = 0;
    LogEntryData vpLogEntryData = null;
    while (viLogEntryIndex != viLogSinkIndex)
    {
      vpLogEntryData = logEntries[viLogEntryIndex];
      String vsFieldText = getFieldValue(vpLogEntryData, inFilterIndex, true);
      if (filterTextMatch(vsFieldText))
      {
        filteredLogEntries[filteredLogCount] = vpLogEntryData;
        filteredLogCount++;
      }
      //
      viLogEntryIndex++;
      if (viLogEntryIndex >= maxLogEntries)
      {
        viLogEntryIndex = 0;
      }
    }
    filteredLogSinkIndex = filteredLogCount;
    if (vpLogEntryData != null)
    {
      try
      {
        Integer entryNumberInteger = (Integer)vpLogEntryData.objectArray[LogConsts.LOG_ENTRY_NUMBER_IDX];
        filteredLogEntryNumber = entryNumberInteger.intValue();
      }
      catch (ClassCastException e)
      {
        //
        // We can end up here if we're reading logs that have been saved
        // to a file.  This is not really an error since we are reading
        // an Integer that has been converted to a string.
        //
        String sEntryNumber = (String)vpLogEntryData.objectArray[LogConsts.LOG_ENTRY_NUMBER_IDX];
        Integer entryNumberInteger = Integer.valueOf(sEntryNumber);
        filteredLogEntryNumber = entryNumberInteger.intValue();
      }
    }
  }
  /*--------------------------------------------------------------------------*/
  private boolean filterTextMatch(String isText)
  {
    Iterator<BoyerMoorePatternMatch> vpIterator = filterMatchDataList.iterator();
    while (vpIterator.hasNext())
    {
      BoyerMoorePatternMatch vpPatternMatch = vpIterator.next();
      if (vpPatternMatch.matches(isText))
      {
        return true;
      }
    }
    return false;
  }
}
