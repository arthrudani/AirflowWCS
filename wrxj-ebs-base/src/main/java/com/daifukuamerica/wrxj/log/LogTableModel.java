package com.daifukuamerica.wrxj.log;

/*
 * Created on Jan 7, 2004
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      SKDC Corp.
 * @author       Stephen Kendorski
 */

import javax.swing.table.AbstractTableModel;
public class LogTableModel extends AbstractTableModel
                           implements LogDataModel
{
  private static final long serialVersionUID = 0L;

  private String logDataName = "";
  /**
   * An array containing the column numbers of displayed columns mapped to the
   * entries' data field indexes in the internal data Model.
   */
  protected int[] dataMap = null;
  /**
   * The Logger data Model component (in the Model/View/Controller design pattern).
   */
  protected LoggingDataAccess logData = null;
  //
  protected int logSourceIndex = 0;
  /**
   * If true, the last (newest) entry will be displayed at the bottom of
   * the View.
   */
  protected boolean lastEntryAtBottom = true;
  protected boolean keepLatestRowVisible = true;
  /**
   * Set true if we need to adjust the fetched logger data to keep data visible
   * when the logger data model handles log entry wrapping.
   */
  protected boolean userHandlingNeeded = true;
  private boolean userUsingLogGrid = false;
  private int usersLogIndexBase = 0;
  private boolean normalToTable = true;
  //
  protected String filterText = "";
  protected int filterColumn = 0;

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  public LogTableModel()
  {
    super();
  }

  /*------------------------------------------------------------------------*/
  //
  // The following 7 methods are defined by the AbstractTableModel
  // interface; all of those methods forward to the real model.
  /*
   * JTable uses this method to determine the default renderer/
   * editor for each cell.  For example, if we didn't implement
   * this method, then the last column would contain text
   * "true"/"false", rather than a check box.
   */
  @Override
  public Class<?> getColumnClass(int columnIndex)
  {
    return getValueAt(0, columnIndex).getClass();
  }

  /**
   * Returns the number of columns in the model. A JTable uses this method
   * to determine how many columns it should create and display by default.
   * 
   * @return the number of columns in the model
   */
  public int getColumnCount()
  {
    return dataMap.length;   
  }

  /**
   * Returns the name of the column at columnIndex. This is used to initialize
   * the table's column header name. Note: this name does not need to be unique;
   * two columns in a table can have the same name.
   *
   * @param columnIndex the index of the column
   * @return the name of the column
   */
  @Override
  public String getColumnName(int columnIndex)
  {
    return null;
  }

  /**
   * Returns the number of rows in the model. A JTable uses this method to
   * determine how many rows it should display. This method should be quick,
   * as it is called frequently during rendering.
   * 
   * @return the number of rows in the model
   */ 
  public int getRowCount()
  {
    try
    {
      if (!filterActive())
      {
        return logData.getEntryCount();   
      }
      else
      {
        return logData.getEntryCount(filterText, dataMap[filterColumn]);   
      }
    }
    catch (NullPointerException e)
    {
      return 0;
    }
  }

  /**
   * Returns the value for the cell at columnIndex and rowIndex.
   * 
   * @param rowIndex the row whose value is to be queried
   * @param columnIndex the column whose value is to be queried
   * @return the value Object at the specified cell
   */ 
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    return getFieldValue(rowIndex, columnIndex, true);
  }

  /**
   * Returns true if the cell at rowIndex and columnIndex  is editable.
   * Otherwise, setValueAt on the cell will not change the value of that cell.
   * 
   * @param rowIndex the row whose value to be queried
   * @param columnIndex the column whose value to be queried
   * @return true if the cell is editable
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return false;
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
   * 
   * @param aValue the new value
   * @param rowIndex the row whose value is to be changed
   * @param columnIndex the column whose value is to be changed
   */
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
  }

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  /**
   * Fetch an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @return the ordered array
   */
  public int[] getColumnToFieldMap()
  {
    return dataMap;
  }

  /**
   * Specify an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @param ianDataMap the ordered array
   */
  public void setColumnToFieldMap(int[] ianDataMap)
  {
    dataMap = ianDataMap;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify if the last (newest) entry will be displayed at the bottom of
   * the View.
   *
   * @param value if true, last/newest entry will be at bottom
   */
  public void setLastEntryAtBottom(boolean value)
  {
    lastEntryAtBottom = value;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify the underlying data Model to use (in the Model/View/Controller
   * design pattern).
   *
   * @param data the data Model
   */
  public void setData(Object data)
  {
    logData = (LoggingDataAccess)data;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify the name to use for the viewed data Model.
   *
   * @param s the data name
   */
  public void setDataName(String s)
  {
    logDataName = s;
  }
  /**
   * Fetch the name used for the viewed data Model.
   *
   * @return the data name
   */
  public String getDataName()
  {
    return logDataName;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the number of the earliest entry that is still in the logger.  This
   * number can be greater than  the logger buffer capacity.
   *
   * @return the number of records
   */
  public int getEarliestEntryNumber()
  {
    return logData.getEarliestLogEntryNumber();
  }

  /*--------------------------------------------------------------------------*/
  /**
    * Return the sequence number of the most recent available log.
    *
    * @return most recent available log sequence number.
    */
  public int getLatestEntryNumber()
  {
    if (!filterActive())
    {
      return logData.getLatestLogEntryNumber();
    }
    else
    {
      return logData.getLatestLogEntryNumber(filterText, dataMap[filterColumn]);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
    * Return true if new entries are available for the caller.
    *
    * @param callersLatestEntryNumber caller's last available entry
    * @return true, if entries available; false, if none available
    */
  public boolean newEntriesAvailable(int callersLatestEntryNumber)
  {
    if (logData != null)
    {
      return logData.newLogsAvailable(callersLatestEntryNumber);
    }
    else
    {
      return false;
    }
  }

  /*--------------------------------------------------------------------------*/
  public void setNormalToTable(boolean value)
  {
    normalToTable = value;
  }

  /*------------------------------------------------------------------------*/
  public void updateTableRowCount()
  {
    logSourceIndex = getLatestEntryNumber();
  }

  /*------------------------------------------------------------------------*/
  public void setUserHandlingNeeded(boolean value)
  {
    userHandlingNeeded = value;
  }
  
  public boolean getUserUsingLogGrid()
  {
    return userUsingLogGrid;
  }


  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  /**
   * Specify the Model Data (in the Model/View/Controller design pattern)
   * to view.
   *
   * @param ipLogData the log data model
   */
  public void initialize(Object ipLogData)
  {
   logData = (LoggingDataAccess)ipLogData;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Specify the Model Data (in the Model/View/Controller design pattern)
   * to view, and an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @param ipLogData the log data model
   * @param ianDataMap the ordered array
   */
  public void initialize(Object ipLogData, int[]ianDataMap)
  {
   initialize(ipLogData);
   dataMap = ianDataMap;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Perform any final clean-up before closing.
   */
  public void cleanUpOnClose()
  {
    logData = null;
  }

  /*--------------------------------------------------------------------------*/
  protected void setKeepLatestEntryVisible(boolean value)
  {
    if ((!value) && (!userUsingLogGrid))
    {
      usersLogIndexBase = getLogSinkEntryNumber();
    }
    userUsingLogGrid = !value;
  }

  /*--------------------------------------------------------------------------*/
  protected int normalizeEntryNumber(int entryNumber)
  {
    int logEntryCount =  getRowCount();
    int maxLogs = logData.getMaxLogEntries();
    if (normalToTable)
    {
      if (!lastEntryAtBottom) // Latest entry at TOP.
      {
        if (!userUsingLogGrid)
        {
          entryNumber =  logEntryCount - (entryNumber + 1);
        }
        else
        {
          if ((usersLogIndexBase - (entryNumber + 1)) >=0)
          {
            entryNumber = usersLogIndexBase - (entryNumber + 1);
          }
          else
          {
            entryNumber = logEntryCount + (usersLogIndexBase - (entryNumber + 1));
          }
        }
      }
      if ((entryNumber < logEntryCount) && (entryNumber < maxLogs))
      {
        if (logEntryCount >=maxLogs)
        {
          if (userUsingLogGrid)
          {
            if (lastEntryAtBottom)
            {
              entryNumber = (entryNumber + usersLogIndexBase) % maxLogs;
            }
            else
            {
              entryNumber = entryNumber % maxLogs;
            }
          }
          else
          {
            entryNumber = (entryNumber + getLogSinkEntryNumber()) % maxLogs;
          }
        }
      }
      else
      {
        entryNumber = (entryNumber + getLogSinkEntryNumber()) % maxLogs;
      }
    }
    else
    {
      entryNumber = entryNumber % maxLogs;
    }
    return entryNumber;
  }

  /*--------------------------------------------------------------------------*/
  protected int getEntryNumber(int entryNumber)
  {
    int maxLogs = logData.getMaxLogEntries();
    entryNumber = entryNumber % maxLogs;
    return entryNumber;
  }
  
  protected Object getFieldValue(int rowIndex, int columnIndex, boolean bAll)
  {
    rowIndex = normalizeEntryNumber(rowIndex);
    if (!filterActive())
    {
      return logData.getValue(rowIndex, dataMap[columnIndex], bAll);
    }
    else
    {
      return logData.getValue(rowIndex, dataMap[columnIndex], bAll, filterText, dataMap[filterColumn]);
    }
  }

  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @return the offset
   */
  private int getLogSinkEntryNumber()
  {
    if (!filterActive())
    {
      return logData.getLogSinkEntryNumber();
    }
    else
    {
      return logData.getLogSinkEntryNumber(filterText, dataMap[filterColumn]);
    }
  }
  private boolean filterActive()
  {
    return (filterText.length() > 0);
  }
}