package com.daifukuamerica.wrxj.swingui.route;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.table.DacTableHeaderRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * <B>Description:</B> Do you have 1,247 routes to add?  Have no fear, 
 * RouteBuilder is here!
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class RouteBuilder extends SKDCInternalFrame
{
  private SKDCTextField mpRouteName;
  private List<String> mpSourceList;
  private List<String> mpDestList;
  private List<Integer> mpTypeList;
  private Boolean[][] mapCheckBoxes;
  
  private StandardDeviceServer mpDeviceServer = Factory.create(StandardDeviceServer.class);
  private StandardRouteServer mpRouteServer = Factory.create(StandardRouteServer.class);
  private StandardStationServer mpStationServer = Factory.create(StandardStationServer.class);

  private RouteTable mpTable;
  
  /**
   * Default constructor 
   */
  public RouteBuilder()
  {
    super();
    
    buildScreen();
    setResizable(true);
    setMaximizable(true);
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize()
  {
    return new Dimension(600,400);
  }
  
  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpDeviceServer.cleanUp();
    mpRouteServer.cleanUp();
    mpStationServer.cleanUp();
  }

  /**
   * 
   */
  public void buildScreen()
  {
    JPanel vpWarning = getEmptyButtonPanel();
    vpWarning.add(new SKDCLabel("This is a developer tool only!"));
    getContentPane().add(vpWarning, BorderLayout.NORTH);

    mpRouteName = new SKDCTextField(RouteData.ROUTEID_NAME);
    fillRouteEndPoints();
    mapCheckBoxes = new Boolean[mpSourceList.size()][mpDestList.size()];
    for (int i = 0; i < mpSourceList.size(); i++)
    {
      for (int j = 0; j < mpDestList.size(); j++)
      {
        mapCheckBoxes[i][j] = Boolean.valueOf(false);
      }
    }

    // Build the table
    mpTable = new RouteTable();

    // Build the screen
    JPanel vpRouteName = new JPanel();
    vpRouteName.add(new SKDCLabel("Route Name:"));
    vpRouteName.add(mpRouteName);
    
    JPanel vpCenterPanel = new JPanel(new BorderLayout());
    vpCenterPanel.setBorder(new TitledBorder(new EtchedBorder(), "Route Information"));
    vpCenterPanel.add(vpRouteName, BorderLayout.NORTH);
    vpCenterPanel.add(mpTable.getScrollPane(), BorderLayout.CENTER);
    
    getContentPane().add(vpCenterPanel, BorderLayout.CENTER);
    
    // Buttons
    SKDCButton vpBtnOkay = new SKDCButton("Submit");
    vpBtnOkay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        okButtonPressed();
      }});

    SKDCButton vpBtnClear = new SKDCButton("Clear");
    vpBtnClear.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        clearButtonPressed();
      }});

    SKDCButton vpBtnClose = new SKDCButton("Close");
    vpBtnClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeButtonPressed();
      }});

    JPanel vpButtonPanel = getEmptyButtonPanel();
    vpButtonPanel.add(vpBtnOkay);
    vpButtonPanel.add(vpBtnClear);
    vpButtonPanel.add(vpBtnClose);
    
    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpSouthPanel.add(vpButtonPanel, BorderLayout.SOUTH);
    
    getContentPane().add(vpSouthPanel, BorderLayout.SOUTH);
  }

  /**
   * Get the lists of possible start and end points.
   */
  private void fillRouteEndPoints()
  {
    mpSourceList = new ArrayList<String>();
    mpDestList = new ArrayList<String>();
    mpTypeList = new ArrayList<Integer>();
    
    try
    {
      List<String> mpStationList = mpStationServer.getStationNameList();
      for (String s : mpStationList)
      {
        mpSourceList.add(s);
        mpDestList.add(s);
        mpTypeList.add(DBConstants.STATION);
      }

      List<String> mpDeviceList = mpDeviceServer.getDeviceNameList();
      for (String s : mpDeviceList)
      {
        mpSourceList.add(s);
        mpDestList.add(s);
        mpTypeList.add(DBConstants.EQUIPMENT);
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }
  
  /**
   * Add all of the selected route segments
   * 
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#okButtonPressed()
   */
  @Override
  protected void okButtonPressed()
  {
    if (mpRouteName.getText().trim().length() == 0)
    {
      displayError("No route ID specified.");
      return;
    }

    int vnAddCounter = 0;
    for (int i = 0; i < mpSourceList.size(); i++)
    {
      for (int j = 0; j < mpDestList.size(); j++)
      {
        if (mapCheckBoxes[i][j])
        {
          try
          {
            RouteData vpRouteData = Factory.create(RouteData.class);
            vpRouteData.setRouteID(mpRouteName.getText());
            vpRouteData.setFromID(mpSourceList.get(i));
            vpRouteData.setFromType(mpTypeList.get(i));
            vpRouteData.setDestID(mpDestList.get(j));
            vpRouteData.setDestType(mpTypeList.get(j));
            vpRouteData.setRouteOnOff(DBConstants.ON);
            mpRouteServer.addRoute(vpRouteData);
            vnAddCounter++;
          }
          catch (DBException dbe)
          {
            logAndDisplayException(dbe);
            if (!displayYesNoPrompt("Add failed for "
                + mpRouteServer.describeRouteSegment(mpRouteName.getText(),
                    mpSourceList.get(i), mpDestList.get(j)) + ". Continue"))
            {
              return;
            }
          }
        }
      }
    }
    displayInfoAutoTimeOut("Added " + vnAddCounter + " route segments.");
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#clearButtonPressed()
   */
  @Override
  protected void clearButtonPressed()
  {
    mpRouteName.setText("");
    for (int i = 0; i < mapCheckBoxes.length; i++)
    {
      for (int j = 0; j < mapCheckBoxes[i].length; j++)
      {
        mapCheckBoxes[i][j] = false;
      }
    }
    mpTable.repaint();
  }
  
  
  /**
   * <B>Description:</B> Route Table
   *
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  private class RouteTable extends JTable
  {
    JScrollPane mpScrollPane;
    
    /**
     * Constructor
     */
    public RouteTable()
    {
      super(new RouteModel());
      setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      getTableHeader().setReorderingAllowed(false);
      getTableHeader().setDefaultRenderer(new DacTableHeaderRenderer());
      setRowHeight(20);
      resizeColumns();
      
      mpScrollPane = new JScrollPane(mpTable,
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      setRowHeaders();
      
      addMouseMotionListener(new RouteMouseListener());
    }
    
    /**
     * @see javax.swing.JTable#getCellRenderer(int, int)
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column)
    {
      if (row == column)
      {
        return new RouteCellRenderer();
      }
      else
      {
        return new BooleanRouteCellRenderer();
      }
    }
    
    /**
     * @see javax.swing.JTable#getPreferredScrollableViewportSize()
     */
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
      return super.getPreferredSize();
    }

    /**
     * Get the scroll pane
     * 
     * @return
     */
    public JScrollPane getScrollPane()
    {
      return mpScrollPane;
    }
    
    /**
     * Add row headers
     * 
     * @param ipTable
     */
    private void setRowHeaders()
    {
      JList<String> vpRowHeaders = new JList<>(new AbstractListModel<String>()
        {
          @Override
          public int getSize()
          {
            return mpSourceList.size();
          }
          @Override
          public String getElementAt(int index)
          {
            return mpSourceList.get(index);
          }
        });
      int vnMaxWidth = 4;
      for (String s : mpSourceList)
      {
        vnMaxWidth = Math.max(s.length(), vnMaxWidth);
      }
      vpRowHeaders.setFixedCellWidth(vnMaxWidth * 10);
      vpRowHeaders.setFixedCellHeight(20);
      vpRowHeaders.setCellRenderer(new RowHeaderRenderer());
      vpRowHeaders.setBackground(UIManager.getLookAndFeel().getDefaults().getColor(
          "TableHeader.background"));
      
      mpScrollPane.setViewportView(this);
      mpScrollPane.setRowHeaderView(vpRowHeaders);
      mpScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, new RowHeaderRenderer("Fr \\ To") );
    }

    /**
     * Row header render class
     */
    private class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> 
    { 
      public RowHeaderRenderer()
      {   
        setOpaque(true);
        setBorder(BorderFactory.createRaisedBevelBorder());
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBackground(UIManager.getLookAndFeel().getDefaults().getColor(
            "TabbedPane.selected"));
      }
      
      public RowHeaderRenderer(String isText)
      {
        this();
        setText(isText);
      }
      
      @Override
      public Component getListCellRendererComponent(JList<? extends String> list,
          String ipValue, int index, boolean izIsSelected, boolean cellHasFocus) 
      {
        setText((ipValue == null) ? "" : ipValue.toString());
        return this;  
      }
    }

    /**
     * Route cell renderer class
     */
    private class RouteCellRenderer extends JPanel implements TableCellRenderer
    {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean izIsSelected, boolean hasFocus, int row, int column)
      {
        if ((row == mpTable.getSelectedRow() &&
             column <= mpTable.getSelectedColumn()) ||
            (column == mpTable.getSelectedColumn() &&
             row <= mpTable.getSelectedRow()))
        {
          setBackground(getSelectionBackground());
        }
        else
        {
          setBackground(Color.LIGHT_GRAY);
        }
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 1));
        add(new SKDCLabel(mpSourceList.get(row)));
        return this;
      }
    }

    /**
     * Route cell renderer class for Boolean
     */
    private class BooleanRouteCellRenderer extends JCheckBox implements
        TableCellRenderer, UIResource
    {
      public BooleanRouteCellRenderer()
      {
        super();
        setHorizontalAlignment(JLabel.CENTER);
        setBorderPainted(true);
      }

      @Override
      public Component getTableCellRendererComponent(JTable table,
          Object value, boolean izIsSelected, boolean hasFocus, int row,
          int column)
      {
        if ((row == mpTable.getSelectedRow() &&
             column <= mpTable.getSelectedColumn()) ||
            (column == mpTable.getSelectedColumn() &&
             row <= mpTable.getSelectedRow()))
        {
          setBackground(getSelectionBackground());
        }
        else
        {
          setBackground(row % 2 == 0 ? Color.WHITE : new Color(230,230,230));
        }
        setSelected((value != null && ((Boolean)value).booleanValue()));

        if (hasFocus)
        {
          setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        }
        else
        {
          setBorder(new EmptyBorder(1, 1, 1, 1));
        }

        return this;
      }
    }

    /**
     * Mouse selection listener
     */
    private class RouteMouseListener extends MouseMotionAdapter
    {
      /**
       * @see java.awt.event.MouseMotionListener#mouseMoved(MouseEvent)
       */
      @Override
      public void mouseMoved(MouseEvent e)
      {
        int vnRow = mpTable.rowAtPoint(e.getPoint());
        int vnCol = mpTable.columnAtPoint(e.getPoint());
        int vnOldCol = mpTable.getSelectedColumn();

        if (vnOldCol != vnCol || mpTable.getSelectedRow() != vnRow)
        {
          mpTable.changeSelection(vnRow, vnCol, false, false);
          
          Rectangle vpR1 = getCellRect(0, Math.min(vnOldCol, vnCol), true);
          vpR1.width *= 2;
          vpR1.height *= vnRow;
          repaint(vpR1);
          displayInfoAutoTimeOut(mpSourceList.get(vnRow) + " to "
              + mpDestList.get(vnCol));
        }
      }
    }
    
    /**
     * By default, all of the columns are squished.  Expand them.
     */
    public void resizeColumns()
    {
      int vnColumnCount = getColumnCount();
      if (vnColumnCount > 0)
      {
        for (int i = 0; i < vnColumnCount; i++)
        {
          TableColumn vpTC = getColumn(getColumnName(i));
          vpTC.setPreferredWidth(getColumnName(i).length() * 10);
          vpTC.setMinWidth(40);
        }
      }
    }
  }
  
  /**
   * <B>Description:</B> Model for RouteBuilder table
   *
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  private class RouteModel extends AbstractTableModel
  {
    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount()
    {
      return mpSourceList.size();
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount()
    {
      return mpDestList.size();
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      return mapCheckBoxes[rowIndex][columnIndex];
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
      return mpDestList.get(column);
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return rowIndex != columnIndex;
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return Boolean.class;
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex)
    {
      mapCheckBoxes[rowIndex][columnIndex] = (Boolean)value;
    }
  }
}
