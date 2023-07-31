package com.daifukuamerica.wrxj.log.view;

import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.LogTableModel;
import com.daifukuamerica.wrxj.swing.DoubleClickFrame;
import com.daifukuamerica.wrxj.swing.SKDCColorChooserFrame;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.table.DacTableHeaderRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * AbstractLogScrollPane is a JScrollPane wrapper for a JTable (which is a
 * user-interface component that presents data in a two-dimensional table
 * format) whose TableModel is a LogTableModel.
 */
@SuppressWarnings("serial")
public abstract class AbstractLogScrollPane extends LogTableModel
                           implements LogScrollPane
{
  static final Color[] colorSelections = {
    new Color(222, 200, 172),
    new Color(203, 246, 204),
    new Color(151, 173, 224),
    new Color(211, 243,  95),
    new Color(255, 246, 204),
    new Color(228, 178, 249),
    new Color(255, 246, 46),
    new Color(255, 133, 138),
    new Color(114, 230, 197),
    new Color(209, 139, 76)
  };

  private SKDCInternalFrame parentFrame = null;
  private JScrollPane logScrollPane = null;
  //
  private JTable logTable = null;
  private AbstractLogScrollPane logTableModel = this;
  private int tableWidth = 0;
  protected String[] columnNames = null;
  protected String[] columnNamesDefaultWidth = null;
  //
  private Timer tableUpdateTimer = null;
  private Timer tableScrollTimer = null;
  //
  int lastSelectedRow = -1;
  int lastSelectedColumn = -1;
  private Color[] selectedKeys = new Color[50];
  Color backgroundColor = null;
  Color foregroundColor = null;
  private int initialColorIndex = 0;
  private String findText = null;
  private int findRow = -1;
  private int findColumn = -1;
  boolean findDown = true;
  boolean mzNoKeyPressed = true;
  

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  public AbstractLogScrollPane()
  {
    super();
    Random random = new Random();
    initialColorIndex = random.nextInt(colorSelections.length);
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
   * Returns the value for the cell at columnIndex and rowIndex.
   * 
   * @param rowIndex the row whose value is to be queried
   * @param columnIndex the column whose value is to be queried
   * @return the value Object at the specified cell
   */ 
  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if (rowIndex != lastSelectedRow)
    {
     backgroundColor = Color.white;
     foregroundColor = Color.black;
    }
    else
    {
      backgroundColor = logTable.getSelectionBackground();
      foregroundColor = logTable.getSelectionForeground();
    }
    if ((selectedKeys != null) &&
        (dataMap != LogConsts.COM_LOG_COLUMN_FIELDS))
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
          if (rowIndex != lastSelectedRow)
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
   * Return the width of the underlying JTable Table Model.
   *
   * @return the width
   */
  public int getTableWidth()
  {
    return tableWidth;
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
    int viKey = -1;
    try
    {
      Integer iKey = (Integer)getFieldValue(entryNumber, dataMap.length - 1, false);
      if (iKey != null)
      {
        viKey = iKey.intValue();
      }
    }
    catch (ClassCastException e)
    {
    }
    return viKey;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Determines whether component is showing on screen. This means
   * that the component is visible (it's frame is not iconified).  The
   * component may be hidden behind another frame, but is still "showing".
   *
   * @return true if the component is showing; false otherwise.
   */
  public boolean isShowing()
  {
    return (logScrollPane.isShowing());
  }

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  /**
   * Specify the Model Data (in the Model/View/Controller design pattern)
   * to view, whether its width should be re-sized, and this component's
   * parent Frame.
   *
   * @param ipLogData the log data model
   * @param izResize if true, re-sise the underlying Jtable width
   * @param ipParentFrame parent Component
   * @return the JScrollPane this object decorates
   */
  public JScrollPane initialize(Object ipLogData, boolean izResize, 
      SKDCInternalFrame ipParentFrame)
  {
    super.initialize(ipLogData);
    //
    logTable = new JTable();
    logTable.setModel(logTableModel);
    parentFrame = ipParentFrame;
    //
    // Set logTable Column widths.
    //
    setTableColumnWidths(izResize);
    //
    // Show no entry keys selected.
    //
    for (int i = 0; i < selectedKeys.length; i++)
    {
      selectedKeys[i] = null;
    }
    //
    //
    // Create the scroll pane and add the table to it.
    //
    logScrollPane = new JScrollPane(logTable);
    //
    tableUpdateTimer = new Timer(500, updateTableAction);
    tableUpdateTimer.start();
    tableScrollTimer = new Timer(50, scrollTableAction);
    tableScrollTimer.stop();
    //
    //--------------------------------------------------------------------
    logTable.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        processMouseClick(e);
      }
    });
    //--------------------------------------------------------------------
    logTable.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (lastSelectedColumn >= 0 && lastSelectedRow >= 0 && mzNoKeyPressed)
        {
          logTable.setRowSelectionInterval(lastSelectedRow, lastSelectedRow);
          logTable.setColumnSelectionInterval(lastSelectedColumn, lastSelectedColumn);
        }
        mzNoKeyPressed = false;
      }
      
      @Override
      public void keyReleased(KeyEvent e)
      {
        mzNoKeyPressed = true;
        processKeyReleased();
      }
    });
    //--------------------------------------------------------------------
    JTableHeader tableHeader = logTable.getTableHeader();
    tableHeader.setToolTipText("Entry Text Filter: Right-Mouse-Click on Desired Column Header");
    tableHeader.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (SwingUtilities.isRightMouseButton(e))
        {
          processTableHeaderMouseRightClick(e);
        }
      }
    });
    //--------------------------------------------------------------------
    //
    // Text search actions.
    //
    InputMap vpInputMap = logTable.getInputMap(JComponent.WHEN_FOCUSED);
    KeyStroke vpSearch = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK);
    vpInputMap.put(vpSearch, "textSearch");
    logTable.getActionMap().put("textSearch", new AbstractAction()
    {
      private static final long serialVersionUID = 0L;

      public void actionPerformed(ActionEvent e)
      {
        //
        // First param: repeat, Second param: Down
        //
        textSearch(false, true);
      }
    });
    //
    vpSearch = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK);
    vpInputMap.put(vpSearch, "textSearchUp");
    logTable.getActionMap().put("textSearchUp", new AbstractAction()
    {
      private static final long serialVersionUID = 0L;

      public void actionPerformed(ActionEvent e)
      {
        //
        // First param: repeat, Second param: Down
        //
        textSearch(false, false);
      }
    });
    //
    vpSearch = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
    vpInputMap.put(vpSearch, "repeatTextSearch");
    logTable.getActionMap().put("repeatTextSearch", new AbstractAction()
    {
      private static final long serialVersionUID = 0L;

      public void actionPerformed(ActionEvent e)
      {
        //
        // First param: repeat, Second param: Down
        //
        textSearch(true, findDown);
      }
    });
    //----------------------------------------- LogScrollPane
    return logScrollPane;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Perform any final clean-up before closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    logTableModel = null;
    logTable = null;
    logScrollPane = null;
    parentFrame = null;
    tableUpdateTimer.stop();
    tableScrollTimer.stop();
    super.cleanUpOnClose();
  }

  /*------------------------------------------------------------------------*/
  /**
   * Assign a foreground color to all rows associated with the specified key.
   *
   * @param key the selection
   * @param color the Color for the foreground
   */
  void addSelectedKey(int key, Color color)
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
//    int entryKey = getEntryKey(lastSelectedRow);
  }

  /*------------------------------------------------------------------------*/
  /**
   * Set all columns' displayed widths.  The widths do not have to be the same
   * as the displyed column names.
   */
  private void setTableColumnWidths(boolean resize)
  {
    TableCellRenderer coloredTableCellRenderer = new ColoredTableCellRenderer();
    Dimension paneSize = new Dimension();
    //
    // Make sure we setAutoResizeMode to OFF, otherwise all of the column width
    // setting gets undone before it's displayed.
    //
    logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JTableHeader header = logTable.getTableHeader();

    TableCellRenderer defaultHeaderRenderer = null;
    if (header != null)
    {
      defaultHeaderRenderer = new DacTableHeaderRenderer();
      header.setDefaultRenderer(defaultHeaderRenderer);
    }

    TableColumnModel columns    = logTable.getColumnModel();
    int              totalWidth = 0;

    int iColCount = logTable.getColumnCount();
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
        Component c = h.getTableCellRendererComponent(logTable,
                          column.getHeaderValue(), false, false, -1, i);
        width = c.getPreferredSize().width;
      }
      column.setCellRenderer(coloredTableCellRenderer);
      Component c = coloredTableCellRenderer.getTableCellRendererComponent(logTable,
                          columnNamesDefaultWidth[columnIndex],
                          false, false, 0, i);
      width = Math.max(width, c.getPreferredSize().width);
      if (width > SKDCGUIConstants.COLUMN_WIDTH_LIMIT)
      {
        width = SKDCGUIConstants.DEFAULT_COLUMN_WIDTH;
      }
      if (width >= 0)
      {
        column.setPreferredWidth(width);
      }
      else
      {
        ;             // ???
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
    logTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
  }

  /*------------------------------------------------------------------------*/
  public class ColoredTableCellRenderer extends DefaultTableCellRenderer
  {
    private static final long serialVersionUID = 0L;

    @Override
    public void setValue(Object value)
    {
      String vsText = "<html>Text Search - Select Column Cell then: Forward: Ctrl-F, Back: Ctrl-B, Repeat: F3<br>" +
                      "Mark/Highlight a Specific Controller: Right-Mouse-Click</html>";
      setToolTipText(vsText);
      setBackground(backgroundColor);
      setForeground(foregroundColor);
      super.setValue(value);
    }
  }

  void setCellColor()
  {
  }
  void processTableHeaderMouseRightClick(MouseEvent e)
  {
    // Don't allow filtering on Number column
    int vnIndex = logTable.getColumnModel().getColumnIndexAtX(e.getX());
    int vnColumn = logTable.getColumnModel().getColumn(vnIndex).getModelIndex();
    if (((String)logTable.getColumnModel().getColumn(vnColumn).getHeaderValue()).equalsIgnoreCase("Number"))
      return;

    //
    // Right mouse click on the table header says display dialog for
    // choosing a text filter.
    //
    String newFilterText = (String)JOptionPane.showInputDialog(parentFrame,
                                    "Enter Filter Text",
                                    "Filter Text Selection",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    null,
                                    null);
    if (newFilterText != null)
    {
      filterText = newFilterText;
      TableColumnModel colModel = logTable.getColumnModel();
      int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
      int newFilterColumn = colModel.getColumn(columnModelIndex).getModelIndex();
      String columnName = (String)colModel.getColumn(newFilterColumn).getHeaderValue();
      if (columnName.indexOf(" [") != -1)
      {
        columnName = columnName.substring(0,columnName.indexOf(" ["));
      }
      if (filterText.length() > 0)
      {
        columnName = columnName + " [" + filterText + "]";
      }
      colModel.getColumn(newFilterColumn).setHeaderValue(columnName);
      if (newFilterColumn != filterColumn)
      {
        columnName = (String)colModel.getColumn(filterColumn).getHeaderValue();
        if (columnName.indexOf(" [") != -1)
        {
          columnName = columnName.substring(0,columnName.indexOf(" ["));
          colModel.getColumn(filterColumn).setHeaderValue(columnName);
        }
        filterColumn = newFilterColumn;
      }
      // Force the header to resize and repaint itself
      logTable.getTableHeader().resizeAndRepaint();
      fireTableDataChanged();
    }
  }
  
  void processKeyReleased()
  {
    int col = logTable.getSelectedColumn();
    if (col != -1)
    {
      lastSelectedColumn = col;
    }
    int row = logTable.getSelectedRow();
    if (row != -1)
    {
      setLastSelectedRow(row);
      fireTableDataChanged();
    }
  }

  private void setLastSelectedRow(int iiRow)
  {
    lastSelectedRow = iiRow;
    if (lastEntryAtBottom)
    {
      int bottomRow = logTableModel.getRowCount() - 1;
      keepLatestRowVisible = (iiRow == bottomRow);
    }
    else
    {
      keepLatestRowVisible = (iiRow == 0);
    }
    if (userHandlingNeeded)
    {
      setKeepLatestEntryVisible(keepLatestRowVisible);
    }
  }

  /*------------------------------------------------------------------------*/
  //
  // Ctrl-F Text Search, F3 Repeat Text Search
  //
  void textSearch(boolean ibRepeat, boolean down)
  {
    findDown = down;
    if (lastSelectedRow != -1)
    {
      if (! ibRepeat)
      {
        findText = (String)JOptionPane.showInputDialog(parentFrame,
                                        "Enter Search Text",
                                        "Search Text Selection",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        null,
                                        null);
        findColumn = lastSelectedColumn;
      }
      if (findText != null)
      {
        if ((ibRepeat) && (findRow == lastSelectedRow))
        {
          if (down)
          {
            findRow++;
          }
          else
          {
            findRow--;
            if (findRow < 0)
            {
              return;
            }
          }
        }
        else
        {
          findRow = lastSelectedRow;
        }
        if ((! down) && (findRow == 0))
        {
          return;
        }
        int foundEntry = logData.findText(findText, findRow, dataMap[findColumn], down);
        setFoundEntry(foundEntry);
      }
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * During a text seach through the entries, set "foundEntry" index to
   * the parameter.  If -1, do NOT set the found entry, but display a
   * "not Found" dialog.  If param is >= 0, scroll the view to the entry.
   * If param is -2 do nothing.
   * 
   * @param viFoundEntry index of found entry, -1 if not found, -2 do nothing
   */
  public void setFoundEntry(int viFoundEntry)
  {
    if (viFoundEntry >= 0)
    {
      findRow = viFoundEntry;
      Rectangle cellRect = logTable.getCellRect(viFoundEntry, 0, true);
      if(cellRect != null)
      {
        logTable.scrollRectToVisible(cellRect);
        setLastSelectedRow(viFoundEntry);
        fireTableDataChanged();
      }
    }
    else if (viFoundEntry == -1)
    {
      JOptionPane.showMessageDialog(null, "Search Text \"" + findText + "\" Not Found", "Search Text Selection", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  void processMouseClick(MouseEvent e)
  {
    Point origin = e.getPoint();
    int row = logTable.rowAtPoint(origin);
    setLastSelectedRow(row);
    //
    int col = logTable.columnAtPoint(origin);
    lastSelectedColumn = col;
    //
    // Check for mouse double-click.
    //
    if (e.getClickCount() == 2)
    {
      String vsTitle = "Number: " + logTableModel.getValueAt(row, 0) + " - " +
                      logTableModel.getColumnName(col);
      DoubleClickFrame vpDoubleClickFrame = new DoubleClickFrame(vsTitle);
      String fText = null;
      try
      {
        fText = (String)logTableModel.getValueAt(row, col);
      }
      catch (ClassCastException ec)
      {
        fText = logTableModel.getValueAt(row, col).toString();
      }
      vpDoubleClickFrame.setData(fText);//baseLogTableModel
      Dimension dimension = null;
      if (fText.indexOf('\n') > 0)
        dimension = new Dimension(800, 400);
      else
        dimension = new Dimension(600, 100);
      vpDoubleClickFrame.setPreferredSize(dimension);
      parentFrame.addSKDCInternalFrame(vpDoubleClickFrame);
    }
    else
    {
      if (dataMap != LogConsts.COM_LOG_COLUMN_FIELDS)
      {
        if (SwingUtilities.isRightMouseButton(e))
        {
          //
          // Right mouse click says display color chooser for setting background
          // color for the entry we are on.
          //
          if (parentFrame != null)
          {
            SKDCColorChooserFrame colorChooserFrame = new SKDCColorChooserFrame();
            parentFrame.addSKDCInternalFrameModal(colorChooserFrame);
            Color newColor = colorChooserFrame.getColorChoice("Choose Text Background Color",
                             colorSelections[initialColorIndex]);
            initialColorIndex++;
            initialColorIndex = initialColorIndex % colorSelections.length;
            int key = getEntryKey(lastSelectedRow);
            if (key >= 0)
            {
              addSelectedKey(key, newColor);
            }
            colorChooserFrame.close();
            colorChooserFrame = null;
          }
        }
      }
      fireTableDataChanged();
    }
  }

  /*------------------------------------------------------------------------*/
  private Action updateTableAction = new AbstractAction() {
    private static final long serialVersionUID = 0L;

    public void actionPerformed(ActionEvent e)
    {
      processUpdateTable();
    }
  };

  void processUpdateTable()
  {
    if (mzNoKeyPressed && isShowing() && logData.newLogsAvailable(logSourceIndex))
    {
      updateTableRowCount();
      fireTableDataChanged();
      if (keepLatestRowVisible)
      {
        tableScrollTimer.start();
      }
    }
  }
  
  /*------------------------------------------------------------------------*/
  void processTableScroll()
  {
    tableScrollTimer.stop();
    int row = 1;
    if (lastEntryAtBottom)
    {
      row = logTable.getRowCount();
      if (keepLatestRowVisible)
      {
        lastSelectedRow = row - 1;
      }
    }
    Rectangle rect = logTable.getCellRect(row, 0, true);
    logTable.scrollRectToVisible(rect);
  }

  private Action scrollTableAction = new AbstractAction() {
    private static final long serialVersionUID = 0L;

    public void actionPerformed(ActionEvent e)
    {
      processTableScroll();
    }
  };
}