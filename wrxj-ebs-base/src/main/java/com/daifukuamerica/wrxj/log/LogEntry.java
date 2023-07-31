package com.daifukuamerica.wrxj.log;

 /**
 * @author Stephen Kendorski
 * @version 1.0
 */
public interface LogEntry
{
  void addDebugLogEntry(String entryType, String name, int key, String text);
  void addErrorLogEntry(String name, int key, String text, int errorNumber);
  void addSystemLogEntry(String entryType, String name, int key, String text, boolean transmission);
  void addCommByteLogEntry(byte[] byteArray, int offset, int count, boolean transmission);
  void addEquipmentLogEntry(String text, String clarifier, boolean transmission);
  void addOperationLogEntry(String entryType, String name, int key, String text);
  void addOperationLogEntry(String entryType, String name, int key, String entryKey, String text);
  void addLogEntry(Object o);

  void setLogSyncNeeded(boolean needed);
  void setMaxLogEntries(int iMaxEntries);
  /**
   * Return the number of log entries currently available.
   *
   * @return number of log entries
   */
  int getEntryCount();
  /**
   * Return the number of log entries currently available.
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return number of log entries
   */
  int getEntryCount(String filterText, int filterIndex);
  /**
   * Fetch the number of the earliest entry that is still in the logger.  This
   * number can be greater than  the logger buffer capacity.
   *
   * @return first available log sequence number.
   */
  int getEarliestLogEntryNumber();
  /**
   * Return the sequence number of the most recent available log.
   *
   * @return most recent available log sequence number.
   */
  int getLatestLogEntryNumber();
  /**
   * Return the sequence number of the most recent available log.
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return most recent available log sequence number.
   */
  int getLatestLogEntryNumber(String filterText, int filterIndex);
  int getMaxLogEntries();
  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @return the offset
   */
  int getLogSinkEntryNumber();
  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @param filterText pattern match to apply to records
   * @param filterIndex the offset to the field within the record
   * @return the offset
   */
  int getLogSinkEntryNumber(String filterText, int filterIndex);

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
  Object getValue(int entryNumber, int index, boolean bAll);
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
  Object getValue(int entryNumber, int index, boolean bAll, String filterText, int filterIndex);
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
  int findText(String findText, int startEntry, int findIndex, boolean down);
  /**
   * Set the data display format
   */
  void setDataDisplayFormat(int displayFormat);
}