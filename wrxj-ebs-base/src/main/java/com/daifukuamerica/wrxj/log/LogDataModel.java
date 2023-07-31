package com.daifukuamerica.wrxj.log;

import javax.swing.table.TableModel;

/*
 * Created on Jan 8, 2004
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      SKDC Corp.
 * @author Stephen Kendorski
 * The LogDataModel interface specifies the methods a View will use to
 * interrogate a log data model.
 */
public interface LogDataModel extends TableModel
{
  /**
   * Perform any post-construction setup needed to complete the instantiated
   * object's functionality.
   * 
   * @param logData the log data model
   */
  public void initialize(Object logData);
  /**
   * Perform any post-construction setup needed to complete the instantiated
   * object's functionality.
   * 
   * @param logData the log data model
   * @param dataMap the displayed column to data field mapping
   */
  public void initialize(Object logData, int[]dataMap);
  /**
   * Perform any final clean-up before closing.
   */
  public void cleanUpOnClose();
  /**
   * Fetch an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @return the ordered array
   */
  public int[] getColumnToFieldMap();
  /**
   * Specify an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @param dataMap the ordered array
   */
  public void setColumnToFieldMap(int[] dataMap);
  /**
   * Specify if the last (newest) entry will be displayed at the bottom of
   * the View.
   *
   * @param value if true, last/newest entry will be at bottom
   */
  public void setLastEntryAtBottom(boolean value);
  /**
   * Specify the underlying data Model to use (in the Model/View/Controller
   * design pattern).
   *
   * @param data the data Model
   */
  public void setData(Object data);
  /**
   * Specify the name to use for the viewed data Model.
   *
   * @param s the data name
   */
  public void setDataName(String s);
  /**
   * Fetch the name used for the viewed data Model.
   *
   * @return the data name
   */
  public String getDataName();
  /**
   * Fetch the number of the earliest entry that is still in the logger.  This
   * number can be greater than  the logger buffer capacity.
   *
   * @return first available log sequence number.
   */
  public int getEarliestEntryNumber();
  /**
    * Return the sequence number of the most recent available log.
    *
    * @return most recent available log sequence number.
    */
  public int getLatestEntryNumber();
  /**
    * Return true if new entries are available for the caller.
    *
    * @param callersLatestEntryNumber caller's last available entry
    * @return true, if entries available; false, if none availabe
    */
  public boolean newEntriesAvailable(int callersLatestEntryNumber);
  /**
   * Specify if the data Model should be normalized to the View Table. If false,
   * no conversions are made to adjust the View's offsets into the data Model.
   *
   * @param value if true, normalize
   */
  public void setNormalToTable(boolean value);
  /**
   *  Set the data source index to the latest entry number.
   */
  public void updateTableRowCount();
  /**
   * Set true if we need to adjust the data to keep data visible when the
   * data model handles data entry wrapping.
   * 
   * @param value true, keep data visible
   */
  public void setUserHandlingNeeded(boolean value);
  /**
   * Fetch value that shows a user wants the log update stopped.
   * 
   * @return true, stop data update
   */
  public boolean getUserUsingLogGrid();
}