package com.daifukuamerica.wrxj.log.io;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SKDC Corp.
 */

/**
 * @author Stephen Kendorski
 * @version 1.0
 */
public interface LogReaderWriter
{
  /**
   * Retrieve log records from a persistence mechanism.
   *
   * @param logName a path and file name
   * @param dataMap read log column to data model field map
   */
  Object readLogs(String logName, int[] dataMap, Object logger);
  /**
   * Initiate automatic saving of log entries as they are detected.  If the
   * caller's parameter <i>appendLogger</i> is true, new log entries are added
   * to logs already saved.  The save method is timer activated, so new log
   * entries are added in batch mode.  A shutdown hook is also created to
   * save any new log entries that exits at application shutdown.
   *
   * @param logTableModel {@link com.daifukuamerica.wrxj.common.log.view.LogTableModel com.daifukuamerica.wrxj.common.swing.LogTableModel} of the logs to save
   * @param logsFile directory where logs are to be saved
   * @param loggerToLoad if <i>!null</i> load logger from saved logs
   */
  void startAutoLogSaver(Object logTableModel, String logsFile, Object loggerToLoad);
  /**
   * Fetch the directory path to where this log implementation saves logs.
   *
   * @return the directory path
   */
  String getLogPath();
  /**
   * Save all Logs to files. Create a unique directory when
   * manually saving the logs to files (for example, by using
   * the MonitorFrame "Save Logs" button).
   * 
   * @return the directory path of the saved files
   */
  String writeLogs();
}