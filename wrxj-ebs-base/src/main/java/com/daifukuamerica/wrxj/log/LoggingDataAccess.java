package com.daifukuamerica.wrxj.log;

/**
 * LoggingDataAccess is a View into the Logger Model (in the Model/View/Controller
 * design pattern).  Multiple simultaneous Views can be attached to the same
 * Logger Model without causing conflict or interaction.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public interface LoggingDataAccess
{
  /**
   * Return the number of log entries currently available.
   *
   * @return number of log entries
   */
  public int getEntryCount();
  /**
   * Return the number of log entries currently available.
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return number of log entries
   */
  public int getEntryCount(String filterText, int filterIndex);
  /**
   * Fetch the number of the earliest entry that is still in the logger.  This
   * number can be greater than  the logger buffer capacity.
   *
   * @return first available log sequence number.
   */
  public int getEarliestLogEntryNumber();
  /**
   * Return the sequence number of the most recent available log.
   *
   * @return most recent available log sequence number.
   */
  public int getLatestLogEntryNumber();
  /**
   * Return the sequence number of the most recent available log.
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return most recent available log sequence number.
   */
  public int getLatestLogEntryNumber(String filterText, int filterIndex);
  /**
   * Fetch the greatest number of records the logger will keep in RAM.  When
   * the maximum is reached the earliest entries are be over-written by the
   * newest entries.
   *
   * @return the maximum number of entries
   */
  public int getMaxLogEntries();
  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @return the offset
   */
  public int getLogSinkEntryNumber();
  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return the offset
   */
  public int getLogSinkEntryNumber(String filterText, int filterIndex);
  /**
   * Return <i>true</i> if additional logs exist.  If the current logging
   * sequence number is different than that in the caller's passed in parameter
   * return <i>true</i>, otherwise return <i>false</i>.
   *
   * @param callersLatestLogEntryNumber most recent log sequence number known
   * @return result of additional logs
   */
  public boolean newLogsAvailable(int callersLatestLogEntryNumber);
  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset.  If the
   * caller's parameter "bAll" is false, return a truncated String if the String
   * length exceeds a pre-determined length.
   *
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @return the Object at entryNumber[field]
   */
  public Object getValue(int entryNumber, int index, boolean bAll);
  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset.  If the
   * caller's parameter "bAll" is false, return a truncated String if the String
   * length exceeds a pre-determined length.
   *
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return the Object at entryNumber[field]
   */
  public Object getValue(int entryNumber, int index, boolean bAll, String filterText, int filterIndex);
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
  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset.  If the
   * caller's parameter "bAll" is false, return a truncated String if the String
   * length exceeds a pre-determined length.
   *
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @param displayFormat, values LogConsts.ASCII_DISPLAY_FORMAT, LogConsts.HEX_DISPLAY_FORMAT, LogConsts.ASCII_CONTROL_DISPLAY_FORMAT
   * @return the Object at entryNumber[field]
   */
  public Object getValueSetDisplayFormat(int entryNumber, int index, boolean bAll, int displayFormat);
  public int findText(String findText, int startEntry, int findIndex, boolean down);
  public String getLogName();
}