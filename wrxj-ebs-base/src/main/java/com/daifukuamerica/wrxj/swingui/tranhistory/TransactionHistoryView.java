package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.swing.DoubleClickFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCHeaderLabel;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * Description:<BR>
 *    Base class for all transaction views.
 *
 * @author       A.D.
 * @version      1.0
 * @since: 07-May-03
 */
@SuppressWarnings("serial")
public abstract class TransactionHistoryView extends JPanel
{
  private   GridBagConstraints gbconst;
  private   List               mpList;
  protected int                interfaceType;
  protected String             msViewHeader;
  protected DacTable           mpDacTable;
  protected SKDCButton         mpSearchButton;
  protected SKDCButton         mpDetSearchButton;
  protected SKDCDateField      mpBeginDateField;
  protected SKDCDateField      mpEndDateField;
  protected SKDCTranComboBox   mpActionTypeCombo;
  protected DBObject           dbobj = new DBObjectTL().getDBObject();
  protected TransactionHistory mpTranHist = Factory.create(TransactionHistory.class);
  protected static TransactionHistoryData mpTNData = Factory.create(TransactionHistoryData.class);
  protected SKDCLabel          mpInfoLabel;
  protected SKDCPopupMenu      mpPopupMenu = new SKDCPopupMenu();
  protected SKDCInternalFrame  parentFrame = null;

  public TransactionHistoryView(int inInterfaceType, ActionListener listener, String isDataViewName)
  {
    super(new BorderLayout());
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    interfaceType = inInterfaceType;
    
    mpDacTable = new DacTable(new DacModel(new ArrayList(), isDataViewName, " "));
    defineButtons(listener);
    
    JPanel vpScrollPanel = new JPanel(new BorderLayout());
    vpScrollPanel.add(mpDacTable.getScrollPane(), BorderLayout.CENTER);
    vpScrollPanel.add(buildInfoPanel(), BorderLayout.SOUTH);
    
    add(buildSearchPanel(), BorderLayout.NORTH);
    add(vpScrollPanel, BorderLayout.CENTER);
    dbConnect();
  }

  private JPanel buildInfoPanel()
  {
    JPanel vpInfoPanel = new JPanel();
    vpInfoPanel.setBorder(BorderFactory.createEtchedBorder());
    
    mpInfoLabel = new SKDCLabel(" ");
    vpInfoPanel.add(mpInfoLabel);
    
    setTableMouseListener();
    
    return vpInfoPanel;
  }
  
  /**
   *  Defines all buttons on the Inventory Transaction Panel, and adds
   *  listeners to them.
   */
  private void defineButtons(ActionListener btnListener)
  {
    mpSearchButton    = new SKDCButton(" Search ", "Search", 'S');
    mpDetSearchButton = new SKDCButton(" Detailed Search ", "Detailed Search", 'D');
    mpSearchButton.addEvent(SKDCGUIConstants.SEARCH_BTN, btnListener);
    mpDetSearchButton.addEvent(SKDCGUIConstants.DETSEARCH_BTN, btnListener);
  }

  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(675, 410));
  }

 /**
  *  Method to reset screen values to defaults.
  */
  public void resetScreen()
  {
    mpDacTable.clearTable();
    mpActionTypeCombo.setSelectedIndex(0);
    initDateFieldValues();
  }

 /**
  *  Gets record count from model using a specific criteria.
  */
  public int getCount(int tranCategory, int actionType)
  {
    int rowCount = 0;
    try
    {
      rowCount = mpTranHist.getCount(tranCategory, actionType,
                                     mpBeginDateField.getDate(),
                                     mpEndDateField.getDate());
    }
    catch(DBException exc)
    {
      exc.printStackTrace(System.out);
      JOptionPane.showMessageDialog(null, exc.getMessage(), "DB Error",
                                    JOptionPane.ERROR_MESSAGE);
      return(rowCount);
    }

    return(rowCount);
  }

  protected void dbConnect()
  {
    if (dbobj == null || !dbobj.checkConnected())
    {
      System.out.println("DB connection object invalid! dbobj = " + dbobj);
      try { dbobj.connect(); } catch(DBException e){ return; }
    }
  }

 /**
  *  Method to handle search button press from all views.
  *
  *  @return <code>int</code> containing status.  -1 if some type of error, or
  *          no data found. 0 otherwise.
  */
  protected int searchButtonPressed(int tranCategory)
  {
    mpTNData.clear();
    try
    {
      mpTNData.setTranTypeKey(mpActionTypeCombo.getIntegerValue());
    }
    catch (NoSuchFieldException e)
    {
      JOptionPane.showMessageDialog(null, e.getMessage(), "Translation",
                                    JOptionPane.INFORMATION_MESSAGE);
      return(-1);
    }
    if (tranCategory > 0)
    {
      mpTNData.setTranCategoryKey(tranCategory);
    }
    mpTNData.setDateRangeKey(mpBeginDateField.getDate(), mpEndDateField.getDate());

    mpTNData.addOrderByColumn(TransactionHistoryData.TRANSDATETIME_NAME);
    mpTNData.addOrderByColumn(AbstractSKDCDataEnum.ID.getName());

    return refreshData(mpTNData);
  }

  protected JPanel buildSearchPanel()
  {
    JPanel ipanel = new JPanel(new GridBagLayout());
    gbconst = new GridBagConstraints();
    gbconst.insets = new Insets(4, 4, 10, 4);
    ipanel.setBorder(BorderFactory.createEtchedBorder());

    buildLabelColumn(ipanel);
    buildInputColumn(ipanel);

    return(ipanel);
  }

  private void buildLabelColumn(JPanel panel)
  {
    gbconst.gridx = 0;
    gbconst.gridy = 0;
    gbconst.gridwidth = GridBagConstraints.REMAINDER;
    gbconst.anchor = GridBagConstraints.CENTER;
    panel.add(new SKDCHeaderLabel(msViewHeader), gbconst);

    gbconst.gridy = GridBagConstraints.RELATIVE;
    gbconst.gridwidth = 1;
    gbconst.anchor = GridBagConstraints.EAST;
    gbconst.weightx = 0.2;
    gbconst.weighty = 0.8;

    panel.add(new SKDCLabel("Action Type:"), gbconst);
    panel.add(new SKDCLabel("Beginning Date:"), gbconst);
    panel.add(new SKDCLabel("Ending Date:"), gbconst);
  }

  private void buildInputColumn(JPanel panel)
  {
    gbconst.gridwidth = 2;
    gbconst.gridx = 1;
    gbconst.gridy = 1;
    gbconst.anchor = GridBagConstraints.WEST;

    mpBeginDateField = new SKDCDateField();
    mpEndDateField = new SKDCDateField();
    initDateFieldValues();

                                       // Note: mpActionTypeCombo is defined
                                       // inside the differing views.
    panel.add(mpActionTypeCombo, gbconst);
    gbconst.gridy = 2;
    panel.add(mpBeginDateField, gbconst);
    gbconst.gridy = 3;
    panel.add(mpEndDateField, gbconst);

    gbconst.gridx = GridBagConstraints.RELATIVE;
    JPanel btnPanel = new JPanel();
    btnPanel.add(mpSearchButton);
    btnPanel.add(javax.swing.Box.createHorizontalStrut(3));
    btnPanel.add(mpDetSearchButton);

    panel.add(btnPanel, gbconst);
  }

  private void initDateFieldValues()
  {
    Calendar cal = Calendar.getInstance();
    Calendar calOrig = (Calendar)cal.clone();
    cal.add(Calendar.DATE, -1);
    calOrig.add(Calendar.DATE, 1);
    mpBeginDateField.setDate(cal.getTime());
    mpEndDateField.setDate(calOrig.getTime());
  }

  /**
   * Default mouse listener.  Should be overridden in most cases. 
   */
  protected void setTableMouseListener()
  {
    mpDacTable.addMouseListener(new DacTableMouseListener(mpDacTable)
    {
      /**
       *  Defines popup menu items for <code>SKDCTable</code>, and adds 
       *  listeners to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {
        return(mpPopupMenu);
      }

      /**
       * @see com.daifukuamerica.wrxj.swing.table.DacTableMouseListener#hasMoreMenuItems()
       */
      @Override
      protected boolean hasMoreMenuItems()
      {
        return false;
      }
      
      /**
       *  Display the Order Line screen.
       */
      @Override
      public void displayDetail()
      {
      }

      /**
       *  Display the Order Line screen.
       */
      @Override
      public void mouseClicked(MouseEvent e)
      {
        // Make sure it has been double clicked and parent frame is not null
        if (e.getClickCount() == 2 && parentFrame != null)
        {
          Point origin = e.getPoint();
          int row = mpDacTable.rowAtPoint(origin);
          //
          // Check for mouse double-click.
          //
          int col = mpDacTable.columnAtPoint(origin);
          // Make sure the mouse is in column Change Description filed
          if (mpDacTable.getDBColumnName(col).equals(TransactionHistoryData.ACTIONDESCRIPTION_NAME))
          {
            String fText = (String)mpDacTable.getValueAt(row, col);
            DoubleClickFrame doubleClickFrame = new DoubleClickFrame(mpDacTable.getColumnName(col));
            doubleClickFrame.setData(fText);
            Dimension dimension = null;
            dimension = new Dimension(600, 100);
            doubleClickFrame.setPreferredSize(dimension);
            parentFrame.addSKDCInternalFrame(doubleClickFrame);
          }
        }
      }
    });
  }
  
  /**
   * Refresh the list based upon search criteria
   * 
   * @param ipKey
   * @return
   */
  protected int refreshData(TransactionHistoryData ipKey)
  {
    mpInfoLabel.setText(" ");
    try
    {
      mpList = mpTranHist.getAllElements(ipKey);
      if (mpList.isEmpty())
      {
        mpInfoLabel.setText("No data found");
        resetScreen();
        return(-1);
      }
    }
    catch(DBException exc)
    {
      exc.printStackTrace(System.out);
      JOptionPane.showMessageDialog(null, exc.getMessage(), "DB Error",
                                    JOptionPane.ERROR_MESSAGE);
      return(-1);
    }
    mpDacTable.refreshData(mpList);

    return(0);
  }
  
  public abstract void searchButtonPressed();
  public abstract TransactionSearch getSearchFrameInstance();
  public abstract DacTable getViewTableInstance();
  
  public void setParentFrame(SKDCInternalFrame ipFrame)
  {
    parentFrame = ipFrame;
  }
}
