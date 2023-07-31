package com.daifukuamerica.wrxj.log;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

 /**
 * @author Stephen Kendorski
 * @version 1.0
 */
public interface Log
{
  /**
   * Specify if logger use needs to be <i>synchronized</i>.
   *
   * @param needed if true, synchronize
   */
  void setLogSyncNeeded(boolean needed);
  /**
   * Specify the greatest number of records the logger will keep in RAM.  When
   * the maximum is reached the earliest entries will be over-written by the
   * newest entries.
   *
   * @param maxLogs the maximum number of entries
   */
  void setMaxLogEntries(int maxLogs);

  void logDebug(String entryData);
  void logDebug(String name, int key, String entryData);
  void logDebugError(String name, int key, String entryData);
  void logDebugOperation(String name, int key,
                         String logText, int type);

  void logError(String name, int key, String entryData);
  void logError(String name, int key, String entryData, int errorNumber);

  void logReceivedMessage(String name, int key, String entryData, String msgType);
  void logTransmittedMessage(String name, int key, String entryData, String msgType);

  void logRxByteCommunication(byte[] byteArray, int offset, int count);
  void logTxByteCommunication(byte[] byteArray, int offset, int count);

  void logRxEquipmentMessage(String entryData, String clarifier);
  void logTxEquipmentMessage(String entryData, String clarifier);

  void logOperation(String name, int key, String entryData, int entryType);
  void logOperation(String name, int key, String entryKey, String entryData, int entryType);

  void addLogEntry(Object o);
  
  void setLogName(String isName);
  String getLogName();
  
  void setLogType(String isType);
  String getLogType();
}
