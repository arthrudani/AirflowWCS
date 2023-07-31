package com.daifukuamerica.wrxj.swing.table;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.DacTranslator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;

/**
 * Class to refine data for display from the database.
 *
 * @author       A.D.
 * @version      1.0
 * @since: 24-Oct-02
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public final class DacModel extends AbstractTableModel
{
  private List<Integer>    mpNonEditableRows;
  private List<Integer>    mpEditableColumns;
  private List<Map>        mpMetaData;
  private List<Map>        mpDBData;
  private String[]         mpColumnOrdering;
  private Object[]         dbDataColumns;
  private Logger           logger = null;
  private DBObject         dbobj;
  private int[]            maxColumnSize;
  private AsrsMetaData     amd    = Factory.create(AsrsMetaData.class);
  private AsrsMetaDataData mddata = Factory.create(AsrsMetaDataData.class);
  private String msNoTranslation = "Undefined Translation";

  public DacModel()
  {
    super();
    mpDBData = new ArrayList<>();
  }

  /**
   *  @param ipDBData <code>List</code> Data cache.  This may be an empty
   *         list but not <Strong><code>null</code></Strong>.
   *  @param isMetaDataName <code>String</code> containing AsrsMetaData record
   *         name (sDataViewName).
   */
  public DacModel(List<Map> ipDBData, String isMetaDataName, String isNoTranslation)
  {
    this(ipDBData, isMetaDataName);
    msNoTranslation = isNoTranslation;
  }

  /**
   *  @param ipDBData <code>List</code> Data cache.  This may be an empty
   *         list but not <Strong><code>null</code></Strong>.
   *  @param isMetaDataName <code>String</code> containing AsrsMetaData record
   *         name (sDataViewName).
   */
  public DacModel(List<Map> ipDBData, String isMetaDataName)
  {
    dbobj = new DBObjectTL().getDBObject();
    if (!dbobj.checkConnected())
    {
      try { dbobj.connect(); } catch(DBException e) { return; }
    }

    mpDBData = ipDBData;

    try                                // Get the Meta Data for formatting this
    {                                  // Table.  Start by getting the desired
                                       // column ordering.
      mpColumnOrdering = amd.getOrderedColumns(isMetaDataName);
      mddata.clear();
      mddata.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, isMetaDataName);
      mpMetaData = amd.getAllElements(mddata);
      setDBDataColumnArray();          // The following sets data to be used later
    }
    catch(DBException exc)
    {
      if (logger == null)
      {
        logger = Logger.getLogger();
      }
      logger.logException(exc, "DacModel");
    }
  }

  /**
   *  @param ipDBData <code>List</code> Data cache.  This may be an empty
   *         list but not <Strong><code>null</code></Strong>.
   *  @param iasColumnOrdering
   *  @param iapMetaData
   */
  public DacModel(List<Map> ipDBData, List<String> ipColumnNames)
  {
    mpDBData = ipDBData;
    mpColumnOrdering = new String[ipColumnNames.size()];
    for (int i = 0; i < ipColumnNames.size(); i++)
    {
      mpColumnOrdering[i] = ipColumnNames.get(i).toUpperCase();
    }
    mpMetaData = new ArrayList<>(ipColumnNames.size());
    for (String s : ipColumnNames)
    {
      Map m = new HashMap<>();
      m.put(AsrsMetaDataData.FULLNAME_NAME, s);
      m.put(AsrsMetaDataData.COLUMNNAME_NAME, s.toUpperCase());
      mpMetaData.add(m);
    }
    setDBDataColumnArray();
  }

  /**
   *  Method to remove all of the model data.  Method will notify view of the
   *  change.
   */
  public void clearTable()
  {
    mpDBData.clear();
    fireTableRowsDeleted(0, 0);
  }

 /**
  * Allows the caller to mark a group of rows as non-editable.  The use of this
  * method is meaningful only when a set of columns have been marked as editable
  * using {@link #setEditableColumns(List) setEditableColumns}
  * 
  * @param ipNonEditableRows List&lt;Integer&gt; of rows to mark as non-editable.
  */
  public void setNonEditableRows(List<Integer> ipNonEditableRows)
  {
    mpNonEditableRows = ipNonEditableRows;
//    for(int vnIdx = 0; vnIdx < ianNonEditableRows.length; vnIdx++)
//    {
//      mpNonEditableRows.add(ianNonEditableRows[vnIdx]);
//    }
  }

 /**
  * Allows the caller to mark a group of columns as editable.
  * @param ipEditableColumns List&lt;Integer&gt; of columns to mark as editable.
  */
  public void setEditableColumns(List<Integer> ipEditableColumns)
  {
    mpEditableColumns = ipEditableColumns;
//    for(int vnIdx = 0; vnIdx < ianEditableColumns.length; vnIdx++)
//    {
//      mpEditableColumns.add(ianEditableColumns[vnIdx]);
//    }
  }
  
  /**
   * Set the value to display for a bad translation
   * 
   * @param isNoTranslation
   */
  public void setNoTranslationDisplay(String isNoTranslation)
  {
    msNoTranslation = isNoTranslation;
  }
  
  /**
   *  Required method of the <code>AbstractTableModel</code> interface.
   *  <code>JTable</code> determines the number of rows to display using this
   *  method.
   *
   *  @return <code>int</code> containing number of rows in the model.
   */
  @Override
  public int getRowCount()
  {
    return(mpDBData.size());
  }

  /**
   * Required method of the <code>AbstractTableModel</code> interface.
   * <code>JTable</code> determines the number of columns it is to display
   * using this method.
   * 
   * @return <code>int</code> containing column count. This count is
   *         determined by the number of displayable columns (which is all
   *         columns in the data set if the corresponding meta-data is
   *         undefined).
   */
  @Override
  public int getColumnCount()
  {
    int colCount = 0;

    if (mpDBData.size() > 0)
    {
      int numQueryColumns = (mpDBData.get(0)).size();
      int numDisplayableColumns = mpColumnOrdering.length;

      if (numDisplayableColumns == 0)
        colCount = numQueryColumns;
      else
        colCount = Math.min(numQueryColumns, numDisplayableColumns);
    }
    else if (mpColumnOrdering != null && mpColumnOrdering.length > 0)
    {
      colCount = mpColumnOrdering.length;
    }

    return(colCount);
  }

 /**
  * Method returns the column name as it appears in the database.
  * @param isViewName the column name as it appears in the view.
  * @return the model column name.
  */
  public String getDBColumnName(int inColumn)
  {
    String vsViewName = getColumnName(inColumn);
    for (Map m : mpMetaData)
    {
      if (vsViewName.equals(DBHelper.getStringField(m, AsrsMetaDataData.FULLNAME_NAME)))
      {
        return DBHelper.getStringField(m, AsrsMetaDataData.COLUMNNAME_NAME);
      }
    }

    return "";
  }

  /**
   *  <code>JTable</code> determines what column header names to display using
   *  this method.  If this method were not implemented, the default displayed
   *  column names would be A, B, C, ....
   *
   *  @param col <code>int</code> index of the column.
   *
   *  @return <code>String</code> containing meta-data column name. If meta-data
   *          is not specified for this table, the database column name is
   *          returned.
   */
  @Override
  public String getColumnName(int col)
  {
    String rtn = "";

    if (!mpDBData.isEmpty())
    {
      if (mpColumnOrdering.length > 0)
      {
        if (isMetaDataColumn(mpColumnOrdering[col]))
        {
          rtn = mddata.getFullName();
        }
      }
      else
      {
        rtn = dbDataColumns[col].toString();
      }
    }
    else if (mpColumnOrdering != null && mpColumnOrdering.length > 0)
    {
      if (isMetaDataColumn(mpColumnOrdering[col]))
      {
        rtn = mddata.getFullName();
      }
    }

    rtn = DacTranslator.getTranslation(rtn);
    
    return(rtn);
  }

  /**
   *  Method used by JTable to accurately determine what renderer to use for
   *  a given column.
   *
   *  @param columnIndex <code>int</code> containing view column index.
   *
   *  @return <code>Class</code> object containing runtime class of this
   *          column.
   */
  @Override
  public Class getColumnClass(int columnIndex)
  {
    Object colObject = null;
                                       // Try and find a valid column class.
    int totalRows = getRowCount();
    if (totalRows > 0)
    {
      for(int row = 0; row < totalRows; row++)
      {
        Object newObject = null;
        try
        {
          newObject = getValueAt(row, columnIndex);
        }
        catch (IndexOutOfBoundsException e)
        {
          newObject = null;
        }
        if (newObject != null)
        {
          colObject = newObject;
          break;
        }
      }
    }
    return((colObject == null) ? Object.class : colObject.getClass());
  }

  /**
   *  Required method of the <code>AbstractTableModel</code> interface.
   *  <code>JTable</code> queries the model for its data via this method.  It
   *  does this for each cell of the table that it will display.  This method
   *  will convert translation values into strings using the static DBTrans
   *  object.
   *
   *  @param row <code>int</code> containing row index of the cell
   *      <code>JTable</code> will get the value for.
   *  @param col <code>int</code> containing view column index of the cell
   *      <code>JTable</code> will get the value for.
   *
   *  @return <code>Object</code> containing data value in the equivalent row,
   *         column mapping of the model.
   */
  @Override
  public Object getValueAt(int row, int col)
  {
    Object rtn = null;
    Object keyColumnName = null;
    Map dbDataMap = null;

    int viRowCount = getRowCount();
    if (viRowCount > 0)
    {
      if (row <= viRowCount  && mpDBData.size() > 0)
      {
        if (mpColumnOrdering.length > 0)
        {
          try
          {
            keyColumnName = mpColumnOrdering[col];
          }
          catch (IndexOutOfBoundsException e)
          {
            return "";
          }
        }
        else                             // No Meta-Data given.
        {
          keyColumnName = dbDataColumns[col];
        }
      }
    }

    if (keyColumnName != null)
    {
      dbDataMap = mpDBData.get(row);
      rtn = dbDataMap.get(keyColumnName);
      if (rtn == null)
      {
        return "";
      }
                                       // If the column is a translation convert
                                       // it to its string representation before
                                       // giving it back to requester.
      String sKey = keyColumnName.toString();
      if (DBTrans.isTranslation(sKey))
      {
        // Tiny hack to handle integers stored as strings.
        // TODO: Handle alpha keys
        if (rtn instanceof Integer || rtn instanceof String)
        {
          try
          {
            if (rtn instanceof Integer)
              rtn = DBTrans.getStringValue(sKey, ((Integer)rtn).intValue());
            else {
              if (((String)rtn).trim().length() > 0) {
                rtn = DBTrans.getStringValue(sKey, Integer.valueOf((String)rtn).intValue());
              }
            }
          }
          catch(NoSuchFieldException e)
          {
            rtn = msNoTranslation;
          }
        }
      }
    }
    return rtn;
  }

 /**
  * Method to change the model value at a particular row and column.  JTable will
  * call this method only if the cell is editable.
  * @param ipNewValue the replacement value
  * @param inRow the cell row to replace.
  * @param inColumn the cell column to replace.
  */
  @Override
  public void setValueAt(Object ipNewValue, int inRow, int inColumn)
  {
    Map vpCurrentRow = mpDBData.get(inRow);
    vpCurrentRow.put(mpColumnOrdering[inColumn], ipNewValue);
      
    fireTableCellUpdated(inRow, inColumn);
  }
  
 /**
  * Method used by JTable to query this model if a given row and/or column is editable.
  * If a group of columns have been marked as editable using
  * {@link #setEditableColumns(List) setEditableColumns}, all rows in those
  * columns will be editable by default. This holds true if no rows in
  * particular have been specified as non-editable using
  * {@link #setNonEditableRows(List) setNonEditableRows}.
  * <b>Note:</b> it is left up to the programmer that a Date or translation
  * column not be editable!
  * @param inRowIndex Index of Model row.
  * @param inColumnIndex index of Model column.
  * @return <code>true</code> if cell is editable, <code>false</code> otherwise.
  */  
  @Override
  public boolean isCellEditable(int inRowIndex, int inColumnIndex)
  {
    boolean vzRtn = false;
    
    if (mpEditableColumns != null)
    {
      vzRtn = (!mpEditableColumns.isEmpty() &&
                mpEditableColumns.contains(Integer.valueOf(inColumnIndex)));
    
      if (mpNonEditableRows != null)
      {
        if (vzRtn && !mpNonEditableRows.isEmpty())
          vzRtn = !mpNonEditableRows.contains(inRowIndex);
      }
    }
      
    return(vzRtn);
  }

  /**
   *  Adds a row of data to the model.  Method will notify view of the
   *  change.
   *
   *  @param tmap <code>Map</code> containing the name value pairs of the
   *         row to add.
   */
  public void addRow(Map tmap)
  {
    mpDBData.add(tmap);
    int endRow = -1;

    if ((endRow = getRowCount()) == 1) // We just added a row to an empty table.
    {
      setDBDataColumnArray();
    }
    fireTableRowsInserted(endRow, endRow);
  }

  /**
   *  Adds a row of data to the model.  Method will notify view of the
   *  change.
   *
   *  @param newData <code>ColumnObject</code> array containing the name value
   *         pairs of the row to add.
   */
  public void addRow(ColumnObject[] newData)
  {
    Map newRowMap = new TreeMap();
    if (mpDBData != null && mpDBData.size() > 0)
    {
      for(int dbCol = 0; dbCol < dbDataColumns.length; dbCol++)
      {
        String dbColName = dbDataColumns[dbCol].toString();
        int newCol = 0;

        for(newCol = 0; newCol < newData.length; newCol++)
        {
          String newColumnName = newData[newCol].getColumnName();
          if (dbColName.equalsIgnoreCase(newColumnName))
          {
            break;
          }
        }

        if (newCol < newData.length)
        {                              // We found the column in the DB columns
                                       // array.
          newRowMap.put(newData[newCol].getColumnName(),
                        newData[newCol].getColumnValue());
        }
      }
    }
    else
    {
      if (mpDBData == null)
      {
        mpDBData = new ArrayList();
      }

      for(int idx = 0; idx < newData.length; idx++)
      {
        newRowMap.put(newData[idx].getColumnName(), newData[idx].getColumnValue());
      }
    }

    mpDBData.add(newRowMap);
    int endRow = -1;
    if ((endRow = getRowCount()) == 1) // We just added a row to an empty table.
    {
      setDBDataColumnArray();
    }
    fireTableRowsInserted(endRow, endRow);
  }

  /**
   *  Modifies a row of data in the model.  Method will notify view of the
   *  change.
   *
   *  @param newData <code>ColumnObject</code> containing row data that is changed
   *         to a Map to reflect the internal data structure of this extension
   *         of TableModel.
   *  @param row <code>int</code> containing index of the row to change.
   */
  public void setModelRow(ColumnObject[] newData, int row)
  {
    Map rowMap = mpDBData.get(row);

    for (int newCol = 0; newCol < newData.length; newCol++)
    {
      String newDataColumnName = newData[newCol].getColumnName();
      if (newDataColumnName != null)
      {
        rowMap.put(newDataColumnName, newData[newCol].getColumnValue());
      }
    }
    fireTableRowsUpdated(row, row);
  }

  /**
   *  Modifies a row of data in the model.  Method will notify view of the
   *  change.
   *
   *  @param newData <code>Map</code> containing changed row data.
   *  @param row <code>int</code> containing index of the row to change.
   */
  public void setModelRow(Map newData, int row)
  {
    mpDBData.set(row, newData);
    fireTableRowsUpdated(row, row);
  }

  /**
   *  Removes a row of data from the model.  Method will notify view of the
   *  change.
   *
   *  @param row <code>int</code> containing index of the row to remove.
   */
  public void removeRow(int row)
  {
    mpDBData.remove(row);
    fireTableRowsDeleted(row, row);
  }

  /**
   * Get an array containing the width of each column
   * @return
   */
  public int[] getColumnWidths()
  {
    return(maxColumnSize);
  }

  /**
   *  Method retrieves the data in the current row of the model as a
   *  <code>Map</code>
   *
   *  @param row <code>int</code> containing index of the row to fetch.
   * @return Map of the current row.
   */
  public Map<String,Object> getRowData(int row)
  {
    return(mpDBData.get(row));
  }

 /**
  * Returns all data in this model.  This is useful in case the model has been 
  * updated but the database has not, and we need the model data for local changes.
  * @return List&lt;Map&gt; of model data.
  */
  public List<Map> getTableData()
  {
    return(mpDBData);
  }

  /**
   * Adds data to an existing model.
   *
   * <p><b>Details:</b> <code>resetData</code> adds data to an existing model.
   * This method is used by SKDCTable for a specific purpose and should not
   * otherwise be called.</p>
   *
   * @param newData <code>List</code> containing new data.
   */
  void resetData(List newData)
  {
    if(mpDBData != newData)
    {
      mpDBData.clear();
    }
    mpDBData = newData;
    if (mpDBData.size() > 0) setDBDataColumnArray();

    fireTableDataChanged();
  }
  
  public void refreshData(int inBeginRow, int inEndRow)
  {
    fireTableRowsUpdated(inBeginRow, inEndRow);
  }

  /**
   *  Method to map the database column name to view's column index.
   *
   *  @param dbColName <code>String</code> containing column name to map to
   *         the view column index.  The column name is <strong>not</strong> the
   *         expanded column name but rather the actual database column name.
   *
   *  @return -1 if name is not found, else the view index.
   */
  public int dbColumnNameToViewIndex(String dbColName)
  {
    int viewColumnIndex = -1;

    if (mpColumnOrdering.length > 0)     // Meta-Data exists.
    {
      for(int idx = 0; idx < mpColumnOrdering.length; idx++)
      {
        String colName = mpColumnOrdering[idx];
        if (colName.equalsIgnoreCase(dbColName))
        {
          viewColumnIndex = idx;
          break;
        }
      }
    }
    else
    {
      for(int idx = 0; idx < dbDataColumns.length; idx++)
      {
        String colName = dbDataColumns[idx].toString();
        if (colName.equalsIgnoreCase(dbColName))
        {
          viewColumnIndex = idx;
          break;
        }
      }
    }

    return(viewColumnIndex);
  }

//  /**
//   * Print the column widths (for debugging)
//   */
//  public void printColumnWidths()
//  {
//    if (maxColumnSize != null)
//    {
//      for(int idx = 0; idx < maxColumnSize.length; idx++)
//      {
//        System.out.println("maxColumnSize[" + idx + "] = " + maxColumnSize[idx]);
//      }
//    }
//  }

/*==========================================================================
          ******** All private methods go in this section. ********
  ==========================================================================*/
  /**
   * 
   */
  private void setDBDataColumnArray()
  {
    if (mpDBData.size() > 0)
    {
      Set tset = (mpDBData.get(0)).keySet();
      dbDataColumns = tset.toArray();
    }
    else
    {
      dbDataColumns = new Object[0];
    }
    calculateColumnWidth();
  }

  /**
   *  Allocate integers to keep track of the maximum column element lengths.
   *  This info. can be used later by the view for formatting.
   */
  private void calculateColumnWidth()
  {
    if (maxColumnSize == null || maxColumnSize.length < mpColumnOrdering.length)
    {
      maxColumnSize = new int[mpColumnOrdering.length];
    }

    for(int col = 0; col < mpColumnOrdering.length; col++)
    {
      String dbColName = mpColumnOrdering[col];
      int viewColumnIdx = dbColumnNameToViewIndex(dbColName);
      int iHeaderNameLength = getColumnName(viewColumnIdx).length();
      maxColumnSize[viewColumnIdx] = iHeaderNameLength;
      if (mpDBData.size() != 0)
      {
        for(int row = 0; row < mpDBData.size(); row++)
        {
          Object cellValue = getValueAt(row, viewColumnIdx);
          if (cellValue != null)
          {
            int iCellValueLength = cellValue.toString().length();
            if (cellValue instanceof Date)
            {
              iCellValueLength -= 6;
            }
            if (maxColumnSize[viewColumnIdx] < iCellValueLength ||
                maxColumnSize[viewColumnIdx] < iHeaderNameLength)
            {
              maxColumnSize[viewColumnIdx] = Math.max(iHeaderNameLength, iCellValueLength);
            }
          }
        } // **** inner for loop
      }
      
      maxColumnSize[viewColumnIdx] *= 0.8;
    } // **** outer for loop
  }

  /**
   *  Method checks if a given column name is part of the meta-data column name
   *  list.  The Meta-data column name list is used to filter which DB columns
   *  can be put into the model.
   *
   *  @param colName <code>Object</code> containing column name to check against
   *         meta-data column list.
   *
   *  @return <code>boolean</code> value of <strong>true</strong> if the
   *          meta-data list contains the passed in column name.
   */
  private boolean isMetaDataColumn(Object colName)
  {
    boolean rtnval = false;
    int row;

    for(row = 0; row < mpMetaData.size(); row++)
    {
      String metaColumn = DBHelper.getStringField(mpMetaData.get(row),
          AsrsMetaDataData.COLUMNNAME_NAME);
      if (metaColumn.equalsIgnoreCase(colName.toString()))
      {
        mddata.clear();
        mddata.dataToSKDCData(mpMetaData.get(row));
        rtnval = true;
        break;
      }
    }
    return(rtnval);
  }
}
