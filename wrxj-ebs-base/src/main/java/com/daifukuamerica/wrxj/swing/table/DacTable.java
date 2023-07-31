package com.daifukuamerica.wrxj.swing.table;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.swing.DacParagraphToolTip;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

/**
 *   Class to build SK data display tables using Lists of data.
 *   This class handles the SK look and feel for tables.
 *
 * @author       A.D.
 * @version      1.0    10/24/02
 * @version      1.5    04/26/05  Added support for localised mouse events as
 *                                as opposed to AbstractMouseFrame.
 */
@SuppressWarnings("serial")
public class DacTable extends JTable
{
  protected int[]              manColumnWidths  = null;
  protected MouseListener[]    mpMouseListeners = null;
  protected JScrollPane        mpScrollPane     = null;
  protected DacTableDecorator  mpDecoratorModel;
  protected URL                mpUpArrowURL;
  protected URL                mpDownArrowURL;

 /**
  *  Set up table using DacModel.
  *
  *  @param model {@link DacModel} object representing data.
  */
  public DacTable(DacModel model)
  {
    super(new DacTableDecorator(model));
    initTable();
    if(model.getRowCount() == 1)
      selectFirstRow();
                                       // Show tool tips for 8 seconds if the
                                       // mouse is stationary.
    ToolTipManager.sharedInstance().setDismissDelay(8000);
  }

 /**
  *  Default constructor.
  */
  public DacTable()
  {
    this(new DacModel());
  }

  /**
   *  Over-loaded method from JTable.
   *
   *  @param model {@link DacModel} object representing data.
   */
  public void setModel(DacModel model)
  {
    super.setModel(new DacTableDecorator(model));
    initTable();
  }

  /**
   * Set the foreground color chooser
   * @param ipDRFCC
   */
  public void setForegroundColorChooser(DacRowForegroundColorChooser ipDRFCC)
  {
    DacTableCellRenderer vpDateRenderer = new DacDateTimeRenderer(SKDCConstants.DATETIME_FORMAT2);
    vpDateRenderer.setForeGroundColorChooser(ipDRFCC);
    
    DacTableCellRenderer vpContentAlign = new DacCellContentAlignmentRenderer(SwingConstants.CENTER);
    vpContentAlign.setForeGroundColorChooser(ipDRFCC);

    DacBooleanRenderer vpBooleanRenderer = new DacBooleanRenderer();
    vpBooleanRenderer.setForeGroundColorChooser(ipDRFCC);

    setDefaultRenderer(Date.class, vpDateRenderer);
    setDefaultRenderer(Integer.class, vpContentAlign);
    setDefaultRenderer(Double.class, vpContentAlign);
    setDefaultRenderer(String.class, vpContentAlign);
    setDefaultRenderer(Boolean.class, vpBooleanRenderer);
    setDefaultRenderer(Long.class, vpContentAlign);
  }
  
  /**
   *  Returns reference to scroll pane that the JTable was added to.
   */
  public JScrollPane getScrollPane()
  {
    return mpScrollPane;
  }

  /**
   *  Clears all rows of this table.
   */
  public void clearTable()
  {
    ((DacTableDecorator)getModel()).clearTable();
    clearSelection();
  }

  /**
   *  Method to set the date format for the model.<br>
   *  <Strong>Note:</Strong> this method will only work if the column class is
   *  of type date.
   *
   *  dateFormat <code>String</code> constant which is one of the values<br>
   *             SKDCConstants.DateFormatString = HH:mm:ss MM-dd-yyyy<br>
   *             SKDCConstants.DATETIME_FORMAT1 = MM-dd-yyyy HH:mm:ss<br>
   *             SKDCConstants.DATETIME_FORMAT2 = dd-MMM-yyyy HH:mm:ss<br>
   *             SKDCConstants.DATE_FORMAT1     = MM-dd-yyyy<br>
   *             SKDCConstants.DATE_FORMAT2     = dd-MMM-yyyy<br>
   *             SKDCconstants.TIME_FORMAT      = HH:mm:ss<br>
   *
   *  {@link com.daifukuamerica.wrxj.common.util.SKDCConstants SKDCConstants}
   */
  public void setDateDisplayFormat(String dateFormat)
  {
    setDefaultRenderer(Date.class, new DacDateTimeRenderer(dateFormat));
    updateUI();
  }

  /**
   * Left justify strings 
   */
  public void leftJustifyStrings()
  {
    setDefaultRenderer(String.class, new DacCellContentAlignmentRenderer(SwingConstants.LEFT));
  }

  /**
   *  Appends a row of data to the model.  This method provides a convenient
   *  front-end to the Model method called "addRow".
   *  @param skData <code>AbstractSKDCData</code> representation of row to be
   *         added.
   *
   *  {@link com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData AbstractSKDCData}
   */
  public void appendRow(AbstractSKDCData skData)
  {
    ((DacTableDecorator)getModel()).addRow(skData.getColumnArray());
    if (getRowCount() == 1)            // First Row added. Update the column
    {                                  // appearances.
      resizeColumns();
    }
  }

  /**
   *  Appends a row of data to the model.  This method provides a convenient
   *  front-end to the Model method called "addRow".
   *  @param columnArray <code>ColumnObject[]</code> representation of row to be
   *         added.
   *
   *  @see com.daifukuamerica.wrxj.jdbc.ColumnObject ColumnObject
   */
  public void appendRow(ColumnObject[] columnArray)
  {
    ((DacTableDecorator)getModel()).addRow(columnArray);
    if (getRowCount() == 1)            // First Row added. Update the column
    {                                  // appearances.
      resizeColumns();
    }
  }

 /**
  *  Appends a row of data to the model.  This method provides a convenient
  *  front-end to the Model method called "addRow".  This method should be used
  *  when there is no AbstractSKDCData representation of the data we wish to
  *  display (for example data retrieved using a Table Join).
  *  @param tmap <code>Map</code> representation of row to be
  *         added.
  */
  public void appendRow(Map tmap)
  {
    ((DacTableDecorator)getModel()).addRow(tmap);
    if (getRowCount() == 1)            // First Row added. Update the column
    {                                  // appearances.
      resizeColumns();
    }
  }

 /**
  *  Modifies a selected row of data in the model.
  *
  *  @param skData <code>AbstractSKDCData</code> representation of current
  *         selected row.
  *
  *  @see com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData AbstractSKDCData
  */
  public void modifySelectedRow(AbstractSKDCData skData)
  {
    int selection = -1;
    if ((selection = getSelectedRow()) != -1)
    {
      ((DacTableDecorator)getModel()).setModelRow(skData.getColumnArray(),
          selection);
    }
  }

  /**
   *  Modifies a selected row of data in the model.
   *
   *  @param columnArray <code>ColumnObject[]</code> representation of current
   *         selected row.
   *
   *  {@link com.daifukuamerica.wrxj.common.jdbc.ColumnObject ColumnObject}
   */
  public void modifySelectedRow(ColumnObject[] columnArray)
  {
    int selection = -1;
    if ((selection = getSelectedRow()) != -1)
    {
      ((DacTableDecorator)getModel()).setModelRow(columnArray, selection);
    }
  }

  /**
   *  Modifies a selected row of data in the model.
   *
   *  @param rowMap <code>Map</code> representation of current
   *         selected row.
   *
   *  {@link java.util.Map Map}
   */
  public void modifySelectedRow(Map rowMap)
  {
    int selection = -1;
    if ((selection = getSelectedRow()) != -1)
    {
      ((DacTableDecorator)getModel()).setModelRow(rowMap, selection);
    }
  }

  /**
   *  Modifies a specified row of data in the model.
   *
   *  @param rowNum <code>int</code> containing zero based Row index of the
   *         row to modify.
   *  @param skData <code>AbstractSKDCData</code> representation of current
   *         selected row.
   *
   *  {@link com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData AbstractSKDCData}
   */
  public void modifyRow(int rowNum, AbstractSKDCData skData)
  {
    ((DacTableDecorator)getModel()).setModelRow(skData.getColumnArray(), rowNum);
  }

  /**
   *  Modifies a specified row of data in the model.
   *
   *  @param rowNum <code>int</code> containing zero based Row index of the
   *         row to modify.
   *  @param columnArray <code>ColumnObject[]</code> representation of current
   *         selected row.
   *
   *  {@link com.daifukuamerica.wrxj.common.jdbc.ColumnObject ColumnObject}
   */
  public void modifyRow(int rowNum, ColumnObject[] columnArray)
  {
    ((DacTableDecorator)getModel()).setModelRow(columnArray, rowNum);
  }

  /**
   *  Modifies a selected row of data in the model.
   *
   *  @param rowNum <code>int</code> containing zero based Row index of the
   *         row to modify.
   *  @param rowMap <code>Map</code> representation of passed in row.
   *
   *  {@link java.util.Map Map}
   */
  public void modifyRow(int rowNum, Map rowMap)
  {
    ((DacTableDecorator)getModel()).setModelRow(rowMap, rowNum);
  }

  /**
   * Removes one or more selected rows from this table (these rows are actually
   * removed from the model also).  Method displays error if no rows are selected.
   */
  public void deleteSelectedRows()
  {
    int[] delRows = getSelectedRows();

    for(int i = 0; i < delRows.length; i++)
    {
      ((DacTableDecorator)getModel()).removeRow(delRows[i]);
      for(int k = i + 1; k < delRows.length; k++)
      {
        delRows[k] -= 1;
      }
    }
  }
  
  /**
   *  Deselects the specified row.
   *  @param rownum <code>int</code> index of the row to deselect.
   */
  public void deselectRow(int rownum)
  {
    DefaultListSelectionModel defSelection = (DefaultListSelectionModel)getSelectionModel();
    defSelection.removeSelectionInterval(rownum, rownum);
  }

  /**
   *  Selects the first row of the table. 
   */
  public void selectFirstRow()
  {
    DefaultListSelectionModel defSelection = (DefaultListSelectionModel)getSelectionModel();
    defSelection.addSelectionInterval(0,0);
  }

  /**
   *  Returns selected or highlighted rows of data in an List.  The format
   *  of the data will be the same as that of data returned from the database.
   *
   *  @return <code>List</code> of <code>Map'ed</code> data of selected rows.
   */
  public List<Map> getSelectedRowDataArray()
  {
    int[] selectedRows = getSelectedRows();
    if (selectedRows.length == 0)
    {
      JOptionPane.showMessageDialog(null, "No row selected", "Selection Error",
          JOptionPane.ERROR_MESSAGE);
      return(new ArrayList<Map>());         // Return empty ArrayList.
    }

    List<Map> arrList = new ArrayList<Map>(selectedRows.length);
    for(int k = 0; k < selectedRows.length; k++)
    {
      arrList.add(((DacTableDecorator)getModel()).getRowData(selectedRows[k]));
    }

    return(arrList);
  }

  /**
   *  Method to retrieve one selected row of data.
   *
   *  @return <code>Map</code> of selected Row.
   */
  public Map getSelectedRowData()
  {
    int selection = -1;

    if ((selection = getSelectedRow()) == -1)
    {
      JOptionPane.showMessageDialog(null, "No row selected", "Selection Error",
          JOptionPane.ERROR_MESSAGE);
      return(new TreeMap());
    }

    return(((DacTableDecorator)getModel()).getRowData(selection));
  }

  /**
   * Get a Map representing the row data
   * @param rownum
   * @return
   */
  public Map<String,Object> getRowData(int rownum)
  {
    return(((DacTableDecorator)getModel()).getRowData(rownum));
  }

 /**
  * Returns all data in this model.  This is useful in case the model has been 
  * updated but the database has not, and we need the model data for local changes.
  * @return List&lt;Map&gt; of model data.
  */
  public List<Map> getTableData()
  {
    return(((DacTableDecorator)getModel()).getTableData());
  }

 /**
  * Method returns the column name as it appears in the database.
  * @param inViewColumn the column number as it appears in the view.
  * @return the model column name.
  */
  public String getDBColumnName(int inViewColumn)
  {
    return ((DacTableDecorator)getModel()).getDBColumnName(convertColumnIndexToModel(inViewColumn));
  }

 /**
  * Retrieves all data in a specified column from a selected set of rows.
  * @param  dbColumnName <code>String</code> containing database name of the
  *         column
  *
  * @return <code>String[]</code> of column values. <code>null</code> if no
  *         matching column name found.
  */
  public String[] getSelectedColumnData(String dbColumnName)
  {
    int[] selectedRows = getSelectedRows();
    if (selectedRows.length == 0)
    {
      JOptionPane.showMessageDialog(null, "No row selected", "Selection Error",
          JOptionPane.ERROR_MESSAGE);
      return(null);         // Return empty List.
    }

    String[] columnData = new String[selectedRows.length];
    for(int row = 0; row < selectedRows.length; row++)
    {
      Map<?, ?> tmap = ((DacTableDecorator)getModel()).getRowData(selectedRows[row]);

      if (tmap.containsKey(dbColumnName))
        columnData[row] = tmap.get(dbColumnName).toString();
      else if (tmap.containsKey(dbColumnName.toUpperCase()))
        columnData[row] = tmap.get(dbColumnName.toUpperCase()).toString();
    }

    return(columnData);
  }

 /**
  * Method to enable tool tips for user selected column(s).
  * @param isDBColumnNames variable arg list of column names such as "SLOADID",
  *        "SITEM" etc.
  */
  public void setToolTipColumns(String... isDBColumnNames)
  {
    for(int vnIdx = 0; vnIdx < isDBColumnNames.length; vnIdx++)
    {
      int vnViewIdx = ((DacTableDecorator)getModel()).getViewColumnIndex(isDBColumnNames[vnIdx]);
      if (vnViewIdx == -1) continue;
      
      TableColumn vpTabCol = getColumn(getColumnName(convertColumnIndexToModel(vnViewIdx)));
      vpTabCol.setCellRenderer(new DacToolTipRenderer());
    }
  }

 /**
  * Method used for JTable tool tips.  <b>Note:</b> If a cell rendering tool tip
  * is set up using {@link #setToolTipColumns setToolTipColumns}, this method
  * will be called by the ToolTip manager on behalf of JTable, and the cell's
  * text will be used. Otherwise the ToolTip Manager looks for tool tip text
  * from the over all JTable component.
  * @return reference to a {@link com.daifukuamerica.wrxj.swing.DacParagraphToolTip DacParagraphToolTip}
  */
  @Override
  public JToolTip createToolTip()
  {
    DacParagraphToolTip vpTTip = new DacParagraphToolTip();
    vpTTip.setComponent(this);

    return(vpTTip);
  }

 /**
  * Method to exclude certain rows of this table from being editable using a
  * pattern or patterns of strings from a displayed column. (All rows containing these patterns
  * will not be editable).
  * @param isDBColumnName the column name of the column containing exclusion
  *        patterns.  The name of the column is the database column name.
  * @param isPattern the string pattern or patterns to use to exclude rows from being editable.
  */
  public void setNonEditableRowsByColumnPattern(String isDBColumnName, String... isPattern)
  {
    List<Integer> vpExcludedRows = new ArrayList<Integer>();

    int vnTotalRows = getRowCount();
    for (String vsPattern : isPattern)
    {
      for (int vnRow = 0; vnRow < vnTotalRows; vnRow++)
      {
        Map<String, Object> vpRowMap = ((DacTableDecorator) getModel()).getRowData(vnRow);
        if (vpRowMap.containsKey(isDBColumnName))
        {
          String vsPatternColVal = vpRowMap.get(isDBColumnName).toString();
          if (vsPatternColVal.contains(vsPattern))
          {
            vpExcludedRows.add(Integer.valueOf(vnRow));
          }
        }
      }
    }
    ((DacTableDecorator)getModel()).setNonEditableRows(vpExcludedRows);
  }
  
 /**
  * Allows user to specify columns that need to be edited.
  * @param isDBColumnNames variable arg list of column names such as "SLOADID",
  *        "SITEM" etc.
  */
  public void setEditableColumn(String... isDBColumnNames)
  {
    getTableHeader().setReorderingAllowed(false);
    List<Integer> vpEditableColumns = new ArrayList<Integer>(isDBColumnNames.length);
    for(int vnIdx = 0; vnIdx < isDBColumnNames.length; vnIdx++)
    {
      vpEditableColumns.add(((DacTableDecorator)getModel()).getViewColumnIndex(isDBColumnNames[vnIdx]));
    }
    ((DacTableDecorator)getModel()).setEditableColumns(vpEditableColumns);
    setSurrendersFocusOnKeystroke(true);
  }

 /**
  * Required method for editable columns.
  * @param ipNewValue the edited value of a cell.
  * @param inRow the row number of the edited cell.
  * @param inColumn the column number of the edited cell.
  */
  @Override
  public void setValueAt(Object ipNewValue, int inRow, int inColumn)
  {
    if (isCellEditable(inRow, inColumn))
    {
      boolean vzError = false;
      Class<?> vpClass = getColumnClass(inColumn);
      
      if (vpClass == Double.class)
        vzError = (((Double)ipNewValue).doubleValue() < 0);
      else if (vpClass == Integer.class)
        vzError = (((Integer)ipNewValue).intValue() < 0);
      
      if (vzError)
      {
        JOptionPane.showMessageDialog(null, "Negative values not allowed.",
                                      "Data Entry error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      ((DacTableDecorator)getModel()).setValueAt(ipNewValue, inRow, inColumn);
    }
  }

 /**
  * Makes sure that all edits to JTable take effect when a button is pressed.
  * @param vnRow  Current row where edit is occurring.
  * @param isDBColumnName Column name of column where edit is occurring. <b>Note:</b>
  *        this is the database column name such as SLOADID, or SITEM and not the
  *        expanded column name as displayed in JTable.
  */
  public void commitEdit(int vnRow, String isDBColumnName)
  {
    int vnViewColIndex = ((DacTableDecorator)getModel()).getViewColumnIndex(isDBColumnName);
    
    if (isCellEditable(vnRow, vnViewColIndex))
    {
      TableCellEditor vpEditor = getCellEditor(vnRow, vnViewColIndex);
      if (!isCellSelected(vnRow, vnViewColIndex))
      {
        Object vpData = vpEditor.getCellEditorValue();
        if (vpData == null)
        {
          Object vpObj = getValueAt(vnRow, vnViewColIndex);
          setValueAt(vpObj, vnRow, vnViewColIndex);
        }
      }
      else if (vpEditor != null)
      {
        try
        {
          vpEditor.stopCellEditing();
        }
        catch(Exception exc)
        {
          exc.printStackTrace();
        }
      }
    }
  }
  
  /**
   * Retrieves the value of a column on the currently selected row.
   * @param  dbColumnName Name of the column to fetch the value for.
   *
   * @return value of the column as an object.
   */
  public Object getCurrentRowDataField(String dbColumnName)
  {
    Object ob = null;

    int selection = -1;
    if ((selection = getSelectedRow()) != -1)
    {
      Map<?,?> tm = ((DacTableDecorator)getModel()).getRowData(selection);
      if (tm.containsKey(dbColumnName))
      {
        ob = tm.get(dbColumnName);
      }
      else if (tm.containsKey(dbColumnName.toUpperCase()))
      {
        ob = tm.get(dbColumnName.toUpperCase());
      }
    }

    return(ob);
  }

  /**
   * Refresh the table with a new list
   * @param alist
   */
  public void refreshData(List alist)
  {
    ((DacTableDecorator)getModel()).resetData(alist);
    initTableHeaderWidths();
    if (mpScrollPane.getWidth() > getPreferredSize().width)
    {
      resizeColumns();
    }
    if (getRowCount() == 1)
      selectFirstRow();
  }

  public void refreshTable()
  {
    ((DacTableDecorator)getModel()).refreshData(0, getRowCount());
  }
  
  /**
   *  Method expands all columns by n pixels where
   *  <code>n = (totalScrollPaneSize - totalColumnSize)/totalNumberOfColumns</code>
   */
  public void resizeColumns()
  {
    int growthAmount = 0;
    int columnCount = getColumnCount();

    if (columnCount > 0)
    {
      growthAmount = ((mpScrollPane.getWidth() - getPreferredSize().width)/columnCount);
      if (growthAmount > 1)
      {
        for(int col = 0; col < columnCount; col++)
        {
          TableColumn tc = getColumn(getColumnName(col));
          int colWidth = tc.getPreferredWidth();
          tc.setPreferredWidth(colWidth + growthAmount);
        }
      }
    }
  }

  /**
   *  Center vertical scrollbar on row.
   *  @param rownum <code>int</code> index of the row on which to center the scroll bar.
   *
   *  Use the position of rownum relative to the total number of rows in the table
   *  to determine the slider position relative to the scroll bar maximum.
   *  This will place the selected row at the TOP of the view.  To center the view
   *  on the selected row, move the slider up by half of a view height.
   * 
   *   */
  public void moveScrollToRow(int rownum)
  {
    int rowCount = getRowCount();
    int scrollBarMax = getScrollPane().getVerticalScrollBar().getMaximum();
    int sliderPos = scrollBarMax;

    if (rownum < rowCount) //slider is already at max, can't move it further
    {
      //set slider position relative to the scroll bar maximum
      sliderPos = ((rownum*scrollBarMax)/rowCount);


      //fudge the slider to center the selected row in the view
      int viewHeight = getScrollPane().getVerticalScrollBar().getVisibleAmount();
      int sliderFudge = (viewHeight/2);
      //don't fudge if slider is already within half a view height of the table end
      if ((sliderPos + sliderFudge < scrollBarMax) &
          (sliderPos - sliderFudge > 0))
      {
        sliderPos = sliderPos - sliderFudge;
      }
    }

    getScrollPane().getVerticalScrollBar().setValue(sliderPos);
  }

  /**
   * Enable or disable the table and its associated listeners
   * @param enabled
   */
  public void setTableEnabled(boolean enabled)
  {
    super.setEnabled(enabled);

    if (enabled)
    {
      if (mpMouseListeners != null)
      {
        for (int i = 0; i < mpMouseListeners.length; i++)
        {
          addMouseListener(mpMouseListeners[i]);
        }
        mpMouseListeners = null;
      }
    }
    else
    {
      mpMouseListeners = getListeners(MouseListener.class);
      for (int i = 0; i < mpMouseListeners.length; i++)
      {
        removeMouseListener(mpMouseListeners[i]);
      }
    }
  }

 /**
  *  Method make sureonly one row selection is allowed.  The default is multiple
  *  row selection.
  *
  *  @param oneRowSelectAllowed
  */
  public void allowOneRowSelection(boolean oneRowSelectAllowed)
  {
    setSelectionMode((oneRowSelectAllowed) ? ListSelectionModel.SINGLE_SELECTION
                                           : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }
  
  /*==========================================================================
           ******** All private methods go in this section. ********
  ==========================================================================*/
  /**
   *  Builds table model based on display data from the database, and meta-data
   *  information such as column ordering.  This method assumes that a database
   *  connection has already been made so that it can retrieve meta-data info.
   *  from the database.
   */
  protected void initTable()
  {
    mpUpArrowURL= DacTable.class.getResource("/graphics/uparrow.png");
    mpDownArrowURL = DacTable.class.getResource("/graphics/downarrow.png");
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setColumnSelectionAllowed(false);
    setRowSelectionAllowed(true);
    setShowHorizontalLines(false);
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    
    DefaultTableCellRenderer vpContentAlign = new DacCellContentAlignmentRenderer(SwingConstants.CENTER);
    setDefaultRenderer(Date.class, new DacDateTimeRenderer(SKDCConstants.DATETIME_FORMAT2));
    setDefaultRenderer(Integer.class, vpContentAlign);
    setDefaultRenderer(Double.class, vpContentAlign);
    setDefaultRenderer(String.class, vpContentAlign);
    setDefaultRenderer(Boolean.class, new DacBooleanRenderer());
    setDefaultRenderer(Long.class, vpContentAlign);

    JTableHeader vpTableHeader = getTableHeader();

    initTableHeaderWidths();
    vpTableHeader.addMouseListener(getHeaderMouseListener());

    // Create the scroll pane container
    // and add the table to it.
    mpScrollPane = new JScrollPane(this);
    mpScrollPane.addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent ce)
      {
        resizeColumns();
      }
    });
  }

  /**
   *  Method to space pad each header cell's text before displaying.
   */
  protected void initTableHeaderWidths()
  {
    int numColumns = getColumnCount();

    int fontSize = getFont().getSize();

    manColumnWidths = ((DacTableDecorator)getModel()).getColumnWidths();
    for(int col = 0; col < numColumns; col++)
    {
      int colLength = fontSize*(manColumnWidths[col] + 2);
      TableColumn tc = getColumn(getColumnName(convertColumnIndexToModel(col)));

      tc.setHeaderRenderer(new DacSorterRenderer());
      if (colLength > SKDCGUIConstants.COLUMN_WIDTH_LIMIT)
      {
        colLength = SKDCGUIConstants.DEFAULT_COLUMN_WIDTH;
      }
      tc.setPreferredWidth(colLength);
    }
  }
  
  /**
   * Method to return HeaderMouseListener object.
   * @return <code>HeaderMouseListener</code> the object.
   */
  protected HeaderMouseListener getHeaderMouseListener()
  {
    return new HeaderMouseListener();
  }

  /**
   *  Class to render SKDCTable Header.
   */
  private final class DacSorterRenderer extends DacTableHeaderRenderer
  {
    private URL arrowType;

    @Override
    public Component getTableCellRendererComponent(JTable ipTable, Object ipValue,
        boolean izSelected, boolean izFocused, int inRow, int inColumn)
    {
      Component vpComponent = super.getTableCellRendererComponent(ipTable, ipValue,
                                                                  izSelected, izFocused, 
                                                                  inRow, inColumn);
      if (ipValue != null)
      {
        JLabel vpLabel = (JLabel)vpComponent;
        if (arrowType != null)
          vpLabel.setIcon(new ImageIcon(arrowType));
        else
          vpLabel.setIcon(new ImageIcon(""));
      }

      return(vpComponent);
    }

    public void setSortMarker(int iArrowType)
    {
      if (iArrowType == DacTableDecorator.ASCENDING)
        arrowType = mpUpArrowURL;
      else if (iArrowType == DacTableDecorator.DESCENDING)
        arrowType = mpDownArrowURL;
      else
        arrowType = null;
    }
  }

  /**
   * Class to listen for header clicks
   */
  private class HeaderMouseListener extends MouseAdapter
  {
    private int    lastColumnNumber = -1;
    private int    ascDesc = DacTableDecorator.ASCENDING;

    @Override
    public void mouseClicked(MouseEvent mEvent)
    {
      int    pressedButton = mEvent.getButton();

      if (mEvent.getClickCount() == 1 && pressedButton != MouseEvent.BUTTON2 &&
          pressedButton != MouseEvent.BUTTON3)
      {
        int columnNum = columnAtPoint(mEvent.getPoint()); //glm The operator may have reordered the columns in the view...
        if (columnNum == lastColumnNumber)
        {
          ascDesc = (ascDesc == DacTableDecorator.ASCENDING)
              ? DacTableDecorator.DESCENDING
              : DacTableDecorator.ASCENDING;
        }
        else
        {
          ascDesc = DacTableDecorator.ASCENDING;
        }
        changeHeader(columnNum, lastColumnNumber, ascDesc);
        lastColumnNumber = columnNum;
        mpDecoratorModel = (DacTableDecorator)getModel();
        int modelsColumnNum = convertColumnIndexToModel(columnNum); //glm ...but the model's column number stays constant
        mpDecoratorModel.sort(modelsColumnNum, ascDesc); //glm TableDecorator.sort uses the model's column number
      }
    }

    private void changeHeader(int currColumnNum, int lastColumnNum,
                              int arrowType)
    {
      if (lastColumnNum != -1)
      {
        TableColumn tcOld = getColumn(getColumnName(lastColumnNum));
        DacSorterRenderer prevColumnRenderer = (DacSorterRenderer)tcOld.getHeaderRenderer();
        prevColumnRenderer.setSortMarker(-1);
        prevColumnRenderer.getTableCellRendererComponent(DacTable.this,
            tcOld.getHeaderValue(), false, false, 0, lastColumnNum);
      }

      TableColumn tcCurrent = getColumn(getColumnName(currColumnNum));
      DacSorterRenderer headerRenderer = (DacSorterRenderer)tcCurrent.getHeaderRenderer();
      headerRenderer.setSortMarker(arrowType);
      headerRenderer.getTableCellRendererComponent(DacTable.this,
          tcCurrent.getHeaderValue(), true, true, 0, currColumnNum);
    }
  }
}
