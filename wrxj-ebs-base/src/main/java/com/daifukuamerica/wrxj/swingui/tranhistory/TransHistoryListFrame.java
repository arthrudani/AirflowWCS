package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacLargeListFrame;
import com.daifukuamerica.wrxj.swing.DacMapViewer;
import com.daifukuamerica.wrxj.swing.DoubleClickFrame;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import javax.swing.JPanel;

/**
 * <B>Description:</B> Transaction History Frame that uses the same code base as
 * the rest of the Warehouse Rx list screens so it doesn't have to re-implement
 * all of the standard features.
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class TransHistoryListFrame extends DacLargeListFrame
{
  int mnCurrentCategory = 0;
  String msViewName;
  
  SKDCTranComboBox mpCategoryCombo;
  SKDCTranComboBox mpTranTypeCombo;
  SKDCDateField mpStartDate;
  SKDCDateField mpEndDate;
  
  // Actions for each category
  int[] manAllActions = DBTrans.getIntegerList(TransactionHistoryData.TRANTYPE_NAME);
  
  int[] manInventoryActions = new int[] { DBConstants.ADD,
      DBConstants.ADD_ITEM,           DBConstants.ADD_LOAD, 
      DBConstants.CYCLE_COUNT,        DBConstants.COUNT,
      DBConstants.DELETE,             DBConstants.DELETE_ITEM,
      DBConstants.DELETE_LOAD,        DBConstants.ITEM_PICK,
      DBConstants.ITEM_RECEIPT,       DBConstants.ITEM_SHIP, 
      DBConstants.MODIFY,             DBConstants.MODIFY_ITEM,  
      DBConstants.MODIFY_LOAD,        DBConstants.TRANSFER ,
      DBConstants.ADD_ITEM_MASTER,    DBConstants.MODIFY_ITEM_MASTER,
      DBConstants.DELETE_ITEM_MASTER};
  
  int[] manLoadActions = new int[] {  DBConstants.ADD_LOAD,
      DBConstants.COMPLETION,         DBConstants.DELETE_LOAD, 
      DBConstants.LOAD_SCHED,        DBConstants.MODIFY_LOAD,
      DBConstants.TRANSFER};

  int[] manOrderActions = new int[] { DBConstants.ADD_ORDER,
      DBConstants.MODIFY_ORDER,       DBConstants.DELETE_ORDER,
      DBConstants.COMPLETION,         DBConstants.ADD_ORDER_LINE,
      DBConstants.MODIFY_ORDER_LINE,  DBConstants.DELETE_ORDER_LINE };
  
  int[] manSystemActions = new int[] { DBConstants.DELETE, DBConstants.MODIFY };
  
  int[] manUserActions = new int[] { DBConstants.LOGIN, DBConstants.LOGOUT };
  
  // Objects for large list
  TransactionHistory mpTH = Factory.create(TransactionHistory.class); 
  TransactionHistoryData mpTHKey = Factory.create(TransactionHistoryData.class);
  
  /**
   * Constructor
   */
  public TransHistoryListFrame()
  {
    this("Transaction_All");
    msViewName = "Transaction_All";
    setDisplaySearchCount(true, "transaction", false);
    buildSearchPanel();
    buildButtonPanel();
  }

  /**
   * Constructor
   * @param isNameOfData
   */
  public TransHistoryListFrame(String isNameOfData)
  {
    super(isNameOfData);
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#getRoleOptionsClass()
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return TransHistoryListFrame.class;
  }

  /**
   * Rebuild the search panel
   */
  private void buildSearchPanel()
  {
    try
    {
      mpTranTypeCombo = new SKDCTranComboBox(TransactionHistoryData.TRANTYPE_NAME);
      mpTranTypeCombo.setDisplayAllEnabled(true);
      
      mpCategoryCombo = new SKDCTranComboBox(
          TransactionHistoryData.TRANCATEGORY_NAME, true);
      mpCategoryCombo.addItemListener(new CategoryListener());
      categoryChanged();
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
      return;
    }
    mpStartDate = new SKDCDateField();
    mpEndDate = new SKDCDateField();
    initDateFieldValues();
    
    searchButton.setVisible(true);
    detailedSearchButton.setVisible(true);
    
    searchPanel = getEmptyInputPanel("");
    GridBagConstraints vpGBC = new GridBagConstraints();
    setLabelColumnGridBagConstraints(vpGBC);
    searchPanel.add(new SKDCLabel("Category:"), vpGBC);
    searchPanel.add(new SKDCLabel("Beginning Date:"), vpGBC);
    searchPanel.add(new SKDCLabel("Ending Date:"), vpGBC);
    
    setInputColumnGridBagConstraints(vpGBC);
    vpGBC.insets = new Insets(4, 2, 4, 2);
    vpGBC.weightx = 0;
    searchPanel.add(mpCategoryCombo, vpGBC);
    vpGBC.gridwidth = 2;
    searchPanel.add(mpStartDate, vpGBC);
    searchPanel.add(mpEndDate, vpGBC);
    
    vpGBC.gridx = 2;
    vpGBC.gridy = 0;
    vpGBC.gridwidth = 1;
    vpGBC.anchor = GridBagConstraints.EAST;
    searchPanel.add(new SKDCLabel("Option:"), vpGBC);
    
    vpGBC.gridx = 3;
    vpGBC.gridy = 0;
    vpGBC.gridwidth = 2;
    vpGBC.anchor = GridBagConstraints.WEST;
    searchPanel.add(mpTranTypeCombo, vpGBC);
    vpGBC.gridwidth = 1;
    
    vpGBC.gridx = 3;
    vpGBC.gridy = 2;
    vpGBC.insets = new Insets(4, 3, 4, 3);
    searchPanel.add(searchButton, vpGBC);
    
    vpGBC.gridx = 4;
    vpGBC.weightx = 0.2;
    vpGBC.insets = getInputColumnInsets();
    searchPanel.add(detailedSearchButton, vpGBC);
    
    getContentPane().add(searchPanel, BorderLayout.NORTH);
  }

  /**
   * Configure the button panel
   */
  private void buildButtonPanel()
  {
    addButton.setVisible(false);
    modifyButton.setText("Archive");
    deleteButton.setVisible(false);
  }

  /**
   * Remove Add, Modify, Delete from pop-up menu
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#setTableMouseListener()
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
      /**
       * Defines popup menu items for <code>SKDCTable</code>, and adds listeners
       * to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {
        return(new SKDCPopupMenu());
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
       * Display the Order Line screen.
       */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
      
      /**
       *  Display the Order Line screen.
       */
      @Override
      public void mouseClicked(MouseEvent e)
      {
        Point vpOrigin = e.getPoint();
        int row = sktable.rowAtPoint(vpOrigin);
        int col = sktable.columnAtPoint(vpOrigin);
        // Pop-up a new frame for a double-click on the Action Description
        String vsDBName = sktable.getDBColumnName(col);
        if (vsDBName.equals(TransactionHistoryData.ACTIONDESCRIPTION_NAME)
            && e.getClickCount() == 2)
        {
          String fText = (String)sktable.getValueAt(row, col);
          DoubleClickFrame doubleClickFrame = new DoubleClickFrame(sktable.getColumnName(col));
          doubleClickFrame.setData(fText);
          Dimension dimension = null;
          dimension = new Dimension(600, 100);
          doubleClickFrame.setPreferredSize(dimension);
          addSKDCInternalFrame(doubleClickFrame);
        }
        else
        {
          super.mouseClicked(e);
        }
      }
    });
  }
  
  /**
   * Change the Action list depending on which Category is selected.
   */
  private void categoryChanged()
  {
    try
    {
      if (mnCurrentCategory == mpCategoryCombo.getIntegerValue())
      {
        return;
      }
      mnCurrentCategory = mpCategoryCombo.getIntegerValue();
      
      switch (mnCurrentCategory)
      {
        case DBConstants.INVENTORY_TRAN:
          mpTranTypeCombo.setComboBoxData(TransactionHistoryData.TRANTYPE_NAME,
              manInventoryActions);
          msViewName = "Transaction_Inventory";
          break;
        case DBConstants.LOAD_TRAN:
          mpTranTypeCombo.setComboBoxData(TransactionHistoryData.TRANTYPE_NAME,
              manLoadActions);
          msViewName = "Transaction_Load";
          break;
        case DBConstants.ORDER_TRAN:
          mpTranTypeCombo.setComboBoxData(TransactionHistoryData.TRANTYPE_NAME,
              manOrderActions);
          msViewName = "Transaction_Order";
          break;
        case DBConstants.SYSTEM_TRAN:
          mpTranTypeCombo.setComboBoxData(TransactionHistoryData.TRANTYPE_NAME,
              manSystemActions);
          msViewName = "Transaction_System";
          break;
        case DBConstants.USER_TRAN:
          mpTranTypeCombo.setComboBoxData(TransactionHistoryData.TRANTYPE_NAME,
              manUserActions);
          msViewName = "Transaction_User";
          break;
        default:
          mpTranTypeCombo.setComboBoxData(TransactionHistoryData.TRANTYPE_NAME,
              manAllActions);
          msViewName = "Transaction_All";
      }
      sktable.clearTable();
      getContentPane().remove(sktable.getScrollPane());
      sktable = null;
      sktable = new DacTable(new DacModel(new ArrayList<Map>(), msViewName, " "));
      if (isVisible())
      {
        setTableMouseListener();
      }
      getContentPane().add(sktable.getScrollPane(), BorderLayout.CENTER);
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }
  }
  
  /**
   * Initialize the starting and ending dates
   */
  private void initDateFieldValues()
  {
    Calendar vpStartCal = Calendar.getInstance();
    Calendar vpEndCal = (Calendar)vpStartCal.clone();
    vpStartCal.add(Calendar.DATE, -1);
    vpEndCal.add(Calendar.DATE, 1);
    mpStartDate.setDate(vpStartCal.getTime());
    mpEndDate.setDate(vpEndCal.getTime());
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#detailedSearchButtonPressed()
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    TransactionSearch vpDetailedSearchFrame;
    switch (mnCurrentCategory)
    {
      case DBConstants.INVENTORY_TRAN:
        vpDetailedSearchFrame = new InventorySearchImpl(
            SKDCConstants.DBINTERFACE, manInventoryActions);
        break;
      case DBConstants.LOAD_TRAN:
        vpDetailedSearchFrame = new LoadSearchImpl(
            SKDCConstants.DBINTERFACE, manLoadActions);
        break;
      case DBConstants.ORDER_TRAN:
        vpDetailedSearchFrame = new OrderSearchImpl(
            SKDCConstants.DBINTERFACE, manOrderActions);
        break;
      case DBConstants.SYSTEM_TRAN:
        vpDetailedSearchFrame = new SystemSearchImpl(
            SKDCConstants.DBINTERFACE, manSystemActions);
        break;
      case DBConstants.USER_TRAN:
        vpDetailedSearchFrame = new UserSearchImpl(
            SKDCConstants.DBINTERFACE, manUserActions);
        break;
      default:
        vpDetailedSearchFrame = new AllSearchImpl(
            SKDCConstants.DBINTERFACE, manAllActions);
    }
    addSKDCInternalFrameModal(vpDetailedSearchFrame, new JPanel[] {
        searchPanel, buttonPanel }, new SearchFrameReturnListener());
  }
  
  /**
   * Archive the transaction history
   * 
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#modifyButtonPressed()
   */
  @Override
  protected void modifyButtonPressed()
  {
    int[] vanTranCategories = DBTrans.getIntegerList(TransactionHistoryData.TRANCATEGORY_NAME);
    addSKDCInternalFrameModal(new ArchiveFrame(SKDCConstants.XMLINTERFACE,
        vanTranCategories), new JPanel[] { searchPanel, buttonPanel },
        new SearchFrameReturnListener());
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#refreshButtonPressed()
   */
  @Override
  protected void refreshButtonPressed()
  {
    startSearch(mpTH, mpTHKey);
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#searchButtonPressed()
   */
  @Override
  protected void searchButtonPressed()
  {
    mpTHKey.clear();
    try
    {
      if (mpTranTypeCombo.getSelectedIndex() > 0)
      {
        mpTHKey.setTranTypeKey(mpTranTypeCombo.getIntegerValue());
      }
      if (mpCategoryCombo.getSelectedIndex() > 0)
      {
        mpTHKey.setTranCategoryKey(mpCategoryCombo.getIntegerValue());
      }
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
      return;
    }
    mpTHKey.setDateRangeKey(mpStartDate.getDate(), mpEndDate.getDate());
    mpTHKey.addOrderByColumn(TransactionHistoryData.TRANSDATETIME_NAME);
    mpTHKey.addOrderByColumn(AbstractSKDCDataEnum.ID.getName());

    refreshButtonPressed();
  }

  
  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#viewButtonPressed()
   */
  @Override
  protected void viewButtonPressed()
  {
    if (SKDCUserData.isSuperUser())
    {
      if (isSelectionValid("View", false))
      {
        String vsDisplayThis = 
          msSingularRowDescription.substring(0,1).toUpperCase()
          + msSingularRowDescription.substring(1);
        
        Map m = sktable.getSelectedRowDataArray().get(0);
        DacMapViewer vpDMV = new DacMapViewer("Display " + vsDisplayThis, 
            vsDisplayThis + " Information", m, msViewName);
        addSKDCInternalFrameModal(vpDMV, new JPanel[] { searchPanel,
            buttonPanel });
      }
    }
  }
  
  /**
   * <B>Description:</B> Change the Action list depending on which Category is
   * selected.
   *
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  private class CategoryListener implements ItemListener
  {
    @Override
    public void itemStateChanged(ItemEvent e)
    {
      categoryChanged();
    }
  }
  
  /**
   * <B>Description:</B> Refresh the list after a change
   *
   * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
   *
   * @author       mandrus
   * @version      1.0
   */
  private class SearchFrameReturnListener implements PropertyChangeListener
  {
    public void propertyChange(java.beans.PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(SKDCGUIConstants.FRAME_CHANGE))
      {
        mpTHKey = (TransactionHistoryData)
            ((TransactionHistoryData)pcevt.getNewValue()).clone();
        refreshButtonPressed();
      }
    }
  }
}
