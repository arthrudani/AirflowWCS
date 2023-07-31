package com.daifukuamerica.wrxj.swing;
/*
 * Created on Jan 9, 2004
 * Title:        WRx-J
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company:      SKDC Corp.
 * @author       Stephen Kendorski
 */

import com.daifukuamerica.wrxj.log.Logger;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * A Table view for displaying status of the Controllers in the application.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public class MonitorTableModel extends AbstractTableModel
                               implements MonitorDataModel
{
  private static final long serialVersionUID = 0L;

  /**
   * The Logging implementation for this named subsystem to use.
   */
  protected Logger logger = Logger.getLogger();

  /**
   * An array containing the column numbers of displayed columns mapped to the
   * entries' data field indexes in the internal data Model.
   */
  protected int[] dataMap = null;

  protected List<String[]> dataList = new ArrayList<String[]>();

  /*------------------------------------------------------------------------*/
  //
  // The following 7 methods are defined by the AbstractTableModel
  // interface; all of those methods forward to the real model.
  /*
   * JTable uses this method to determine the default renderer/
   * editor for each cell.  If we didn't implement this method,
   * then the last column would contain text ("true"/"false"),
   * rather than a check box.
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
    return dataList.size();   
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
    try
    {
      String[] entryStatus = dataList.get(rowIndex) ;
      return entryStatus[columnIndex];
    }
    catch (IndexOutOfBoundsException e)
    {
      return "";
    }
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
   * Perform any final clean-up before closing.
   */
  public void cleanUpOnClose()
  {
    dataList.clear();
  }
}