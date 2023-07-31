package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.swing.table.DacTableHeaderRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * AbstractMonitorScrollPane is a JScrollPane wrapper for a JTable (which is a
 * user-interface component that presents data in a two-dimensional table
 * format) whose TableModel is Status MonitorData.
 */
@SuppressWarnings("serial")
public abstract class AbstractMonitorScrollPane extends MonitorTableModel
    implements MonitorScrollPane
{
  SKDCInternalFrame parentFrame = null;
  private JScrollPane monitorScrollPane = null;
  //
  protected JTable dataTable = null;
  protected AbstractMonitorScrollPane monitorTableModel = this;
  private int tableWidth = 0;
  protected String[] columnNames = null;
  protected String[] columnNamesDefaultWidth = null;

  private Color[] selectedKeys = new Color[50];
  Color backgroundColor = null;
  Color foregroundColor = null;
  protected String groupName = null;
  protected int updatedIndex = -1;

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  public AbstractMonitorScrollPane()
  {
    super();
    groupName = Application.getString(WarehouseRx.RUN_MODE);
  }

  /**
   * Returns the value for the cell at columnIndex and rowIndex.
   * 
   * @param rowIndex the row whose value is to be queried
   * @param columnIndex the column whose value is to be queried
   * @return the value Object at the specified cell
   */ 
  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if (!dataTable.isRowSelected(rowIndex))
    {
     backgroundColor = Color.white;
     foregroundColor = Color.black;
    }
    else
    {
      backgroundColor = dataTable.getSelectionBackground();
      foregroundColor = dataTable.getSelectionForeground();
    }
    if (selectedKeys != null)
    {
      int entryKey = getEntryKey(rowIndex);
      if (entryKey > -1)
      {
        if (entryKey >= selectedKeys.length)
        {
          //
          // Need to grow keys array.
          //
          Color[] newArray = new Color[entryKey + 50];
          System.arraycopy(selectedKeys, 0, newArray, 0, selectedKeys.length);
          selectedKeys = newArray;
        }
        if (selectedKeys[entryKey] != null)
        {
          if (!dataTable.isRowSelected(rowIndex))
          {
            backgroundColor = selectedKeys[entryKey];
          }
        }
      }
    }
    return super.getValueAt(rowIndex, columnIndex);
  }

  /*------------------------------------------------------------------------*/
  /**
   * Assign a foreground color to all rows associated with the specified key.
   *
   * @param key the selection
   * @param color the Color for the foreground
   */
  public void addSelectedKey(int key, Color color)
  {
    if (key >= selectedKeys.length)
    {
      //
      // Need to grow keys array.
      //
      Color[] newArray = new Color[key + 50];
      System.arraycopy(selectedKeys, 0, newArray, 0, selectedKeys.length);
      selectedKeys = newArray;
    }
    if (color == null)
    {
      //
      // This entry key is going away - remove it from the array.
      //
      int moveCount = selectedKeys.length - (key + 1);
      if (moveCount > 0)
      {
        System.arraycopy(selectedKeys, key + 1, selectedKeys, key, moveCount);
      }
      selectedKeys[selectedKeys.length-1] = null; // set new last element to null
    }
    else
    {
      selectedKeys[key] = color;
    }
  }

  public void setGroupName(String isGroupName)
  {
    groupName = ":" + isGroupName;
    dataList.clear();
    fireTableDataChanged();
  }
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the identifier for a specified record.  The identifier is the unique
   * key associated with the named sub-system that logged the specified entry.
   *
   * @param entryNumber the record of interest
   *
   * @return the identifier
   */
  public int getEntryKey(int entryNumber)
  {
    return entryNumber;
  }

  /**
   * Returns the number of columns in the model. A JTable uses this method
   * to determine how many columns it should create and display by default.
   * 
   * @return the number of columns in the model
   */
  @Override
  public int getColumnCount()
  {
    return columnNames.length;   
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
    return columnNames[columnIndex];
  }
 
  /**
   * Fetch the list index of the last item to updated.
   * 
   * @return the index, -1 if not in list.
   */
  public int getUpdatedIndex()
  {
    return updatedIndex;
  }

  /**
   * Notifies all listeners that all cell values in the table's rows may have
   * changed. The number of rows may also have changed and the JTable should
   * redraw the table from scratch. The structure of the table (as in the
   * order of the columns) is assumed to be the same.
   */
  public void fireDataChanged()
  {
    fireTableDataChanged();
  }
  /*------------------------------------------------------------------------*/
  /**
   * Return the underlying JTable Table Model.
   *
   * @return the table Model
   */
  public JTable getTable()
  {
    return dataTable;
  }

  /**
   * Return the width of the underlying JTable Table Model.
   *
   * @return the width
   */
  public int getTableWidth()
  {
    return tableWidth;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Specify the logger and this component's parent Frame.
   *
   * @param logger the logger to use
   * @param parent parent Component
   * @return the JScrollPane this object decorates
   */
  public JScrollPane initialize(SKDCInternalFrame parent)
  {
    dataTable = new JTable();
    dataTable.setModel(monitorTableModel);
    parentFrame = parent;
    //
    //
    // Create the scroll pane and add the table to it.
    //
    monitorScrollPane = new JScrollPane(dataTable);
    //
    // Set myTable Column widths.
    //
    setTableColumnWidths(false);
    for (int i = 0; i < selectedKeys.length; i++)
    {
      selectedKeys[i] = null;
    }
    //--------------------------------------------------------------------
    dataTable.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        Point origin = e.getPoint();
        int row = dataTable.rowAtPoint(origin);
        //
        // Check for mouse double-click.
        //
        if (e.getClickCount() == 2)
        {
          int col = dataTable.columnAtPoint(origin);
          String vsTitle = "Number: " + monitorTableModel.getValueAt(row, 0) + " - " +
                                       monitorTableModel.getColumnName(col);
          DoubleClickFrame vpDoubleClickFrame = new DoubleClickFrame(vsTitle);
          String fText = (String)dataTable.getValueAt(row, col);
          vpDoubleClickFrame.setData(fText);//baseLogTableModel
          Dimension dimension = null;
          if (fText.indexOf('\n') > 0)
            dimension = new Dimension(800, 400);
          else
            dimension = new Dimension(600, 100);
          vpDoubleClickFrame.setPreferredSize(dimension);
          parentFrame.addSKDCInternalFrame(vpDoubleClickFrame);
        }
      }
    });
    return monitorScrollPane;
  }
  
  /*------------------------------------------------------------------------*/
  /**
   * Perform any final clean-up before closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    monitorTableModel = null;
    dataTable = null;
    monitorScrollPane = null;
    parentFrame = null;
    monitorTableModel = null;
    columnNames = null;
    columnNamesDefaultWidth = null;
    selectedKeys = null;
    backgroundColor = null;
    foregroundColor = null;
    groupName = null;
    super.cleanUpOnClose();
  }

  /*------------------------------------------------------------------------*/
  /**
   * Set all columns' displayed widths.  The widths do not have to be the same
   * as the displayed column names.
   */
  private void setTableColumnWidths(boolean resize)
  {
    TableCellRenderer coloredTableCellRenderer = new ColoredTableCellRenderer();
    Dimension paneSize = new Dimension();
    //
    // Make sure we setAutoResizeMode to OFF, otherwise all of the column width
    // setting gets undone before it's displayed.
    //
    dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JTableHeader header = dataTable.getTableHeader();

    TableCellRenderer defaultHeaderRenderer = null;
    if (header != null)
    {
      defaultHeaderRenderer = new DacTableHeaderRenderer();
      header.setDefaultRenderer(defaultHeaderRenderer);
    }

    TableColumnModel columns    = dataTable.getColumnModel();
    int              totalWidth = 0;

    int iColCount = dataTable.getColumnCount();
    for (int i = 0; i < iColCount; i++)
    {
      TableColumn       column      = columns.getColumn(i);
      int               columnIndex = column.getModelIndex();
      int               width       = -1;
      TableCellRenderer h           = column.getHeaderRenderer();
      if (h == null)
      {
          h = defaultHeaderRenderer;
      }
      if (h != null)    // Not explicitly impossible
      {
        Component c = h.getTableCellRendererComponent(dataTable,
                          column.getHeaderValue(), false, false, -1, i);
        width = c.getPreferredSize().width;
      }
      column.setCellRenderer(coloredTableCellRenderer);
      Component c = coloredTableCellRenderer.getTableCellRendererComponent(dataTable,
                          columnNamesDefaultWidth[columnIndex],
                          false, false, 0, i);
      
      width = Math.max(width, c.getPreferredSize().width);
      // Hopefully this will keep the system monitor from going too crazy
      if (width > SKDCGUIConstants.COLUMN_WIDTH_LIMIT)
      {
        width = SKDCGUIConstants.DEFAULT_COLUMN_WIDTH;
      }
      if (width >= 0)
      {
        column.setPreferredWidth(width);
      }
      width = column.getPreferredWidth();
      totalWidth += width;
    }
    totalWidth += columns.getColumnCount() * columns.getColumnMargin();

    paneSize.width = totalWidth;

    paneSize.width += 15;
    tableWidth = paneSize.width;
    if (resize)
    {
      int parentWidth = parentFrame.getWidth();
      if (parentWidth < tableWidth)
      {
        parentFrame.setSize(paneSize.width, parentFrame.getHeight());
      }
    }
    dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
  }

  /*------------------------------------------------------------------------*/
  class ColoredTableCellRenderer extends DefaultTableCellRenderer
  {
    @Override
    public void setValue(Object value)
    {
      setBackground(backgroundColor);
      setForeground(foregroundColor);
      super.setValue(value);
    }
  }
}