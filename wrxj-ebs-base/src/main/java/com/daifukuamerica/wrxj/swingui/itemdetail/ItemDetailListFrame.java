package com.daifukuamerica.wrxj.swingui.itemdetail;

import com.daifukuamerica.wrxj.util.SKDCUtility;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacLargeListFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.load.LoadListFrame;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A screen class for displaying a list of load line items.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ItemDetailListFrame extends DacLargeListFrame
{
  protected boolean mzAutoUpdate = false;

  protected LoadLineItemData idSearchData = Factory.create(LoadLineItemData.class);
  protected SKDCButton showLoadButton;
  protected boolean runFromAnotherScreen = false;
  protected boolean mzSearchByItem = true;
  protected ButtonListener mpListener;
  protected boolean mzThisReplacesLabelTextUsedAsABoolean = true;
  
  /**
   *  Create load line item list frame.
   */
  public ItemDetailListFrame()
  {
    super("ItemDetail");
    userData = new SKDCUserData();
    setSearchData("Item", DBInfo.getFieldLength(LoadLineItemData.ITEM_NAME));
    setDetailSearchVisible(true);
    setDisplaySearchCount(true, "Item Detail");
  }

  /**
   * Search on open if the screen is set to auto-update
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    if (mzAutoUpdate)
    {
      searchButtonPressed();
    }
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param searchData ColumnObject containing criteria to use in search.
   */
  public void setFilter(KeyObject[] searchData)
  {
    mzAutoUpdate = true;
    idSearchData.clear();
    for(int keyIdx = 0; keyIdx < searchData.length; keyIdx++)
    {
      idSearchData.addKeyObject(searchData[keyIdx]);
    }
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param loadID ColumnObject containing criteria to use in search.
   */
  public void setLoadFilter(String loadID)
  {
    mzAutoUpdate = true;
    setSearchData("Load", DBInfo.getFieldLength(LoadLineItemData.LOADID_NAME));
    searchField.setText(loadID);
    idSearchData.clear();

    if (loadID.length() > 0)
    {
      idSearchData.setKey(LoadLineItemData.LOADID_NAME, loadID);
      runFromAnotherScreen = true;
    }
    mzSearchByItem = false;
    mzThisReplacesLabelTextUsedAsABoolean = false;
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param sItem ColumnObject containing criteria to use in search.
   */
  public void setItemFilter(String sItem)
  {
    mzAutoUpdate = true;
    setSearchData("Item", DBInfo.getFieldLength(LoadLineItemData.ITEM_NAME));
    searchField.setText(sItem);
    idSearchData.clear();

    if (sItem.length() > 0)
    {
      idSearchData.setKey(LoadLineItemData.ITEM_NAME, sItem, KeyObject.LIKE);
      runFromAnotherScreen = true;
    }
    mzSearchByItem = true;
    mzThisReplacesLabelTextUsedAsABoolean = true;
  }

  /**
   *  Method to reset the search filter.
   *
   */
  public void clearFilter()
  {
    idSearchData.clear();
  }

  /**
   * Method to filter by extended search. Refreshes display.
   */
  public void refreshTable()
  {
    // Make sure we use the default sort
    idSearchData.clearOrderByColumns();
    idSearchData.addOrderByColumn(LoadLineItemData.ITEM_NAME);
    idSearchData.addOrderByColumn(LoadLineItemData.LOT_NAME);
    idSearchData.addOrderByColumn(LoadLineItemData.LOADID_NAME);

    startSearch(new LoadLineItem(), idSearchData);
  }

  /**
   *  Adds buttons on the main Item Detail Panel.
   */
  protected void addExtraButtons()
  {
    showLoadButton = new SKDCButton("Load", "Show Parent Load", 'L');
    getButtonPanel().add(showLoadButton);
    getButtonPanel().add(closeButton);
  }

  /**
   * 
   */
  @Override
  protected void addActionListeners()
  {
    super.addActionListeners();
    
    addExtraButtons();
    mpListener = new ButtonListener();
    showLoadButton.addEvent(SHOWDETAIL_BTN, mpListener);
    
    sktable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        if (sktable.getSelectedRowCount() > 1)
          calculateSums();
        else
          setInfo("");
      }
    });
  }

  /**
   *  Button Listener class.
   */
  public class ButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(SHOWDETAIL_BTN))
      {
        displayLoadButtonPressed();
      }
    }
  }

  /**
   * Sum the quantities on the selected lines 
   */
  protected void calculateSums()
  {
    int vnSelectedLines = sktable.getSelectedRowCount();
    if (vnSelectedLines == 0)
    {
      displayInfoAutoTimeOut("No rows selected to Sum", "Selection Error");
      return;
    }
    
    double vnCurrentQty = 0.0;
    double vnAllocQty = 0.0;

    String[] vpCurrentQtyList = null;
    String[] vpAllocQtyList = null;

    vpCurrentQtyList = sktable.getSelectedColumnData("fCurrentQuantity");
    vpAllocQtyList = sktable.getSelectedColumnData("fAllocatedQuantity");

    for (String s : vpCurrentQtyList)
    {
      vnCurrentQty += Double.parseDouble(s);
    }

    for (String s : vpAllocQtyList)
    {
      vnAllocQty += Double.parseDouble(s);
    }
    String vsCurrentQty = SKDCUtility.formatDouble("###,###,###.##", vnCurrentQty);
    String vsAllocQty = SKDCUtility.formatDouble("###,###,###.##", vnAllocQty);
    String vsOutput = "<HTML><B>Selected lines:</B> " + vnSelectedLines + 
        " &nbsp;  &nbsp; - &nbsp; &nbsp; <B>Current:</B> " + vsCurrentQty + 
        " &nbsp;  &nbsp; - &nbsp; &nbsp; <B>Allocated:</B> " + vsAllocQty + "</HTML>";
    displayInfoAutoTimeOut(vsOutput);
  }
  
  /**
   *  Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    if (mzThisReplacesLabelTextUsedAsABoolean)
    {
      setItemFilter(searchField.getText());
    }
    else
    {
      setLoadFilter(searchField.getText());
    } 
    runFromAnotherScreen = false;
    refreshTable();
  }

  /**
   * 
   */
  @Override
  protected void refreshButtonPressed()
  {
    refreshTable();
  }
  
  /**
   * Action method to handle the Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    String vsInitialItem = null;
    String vsInitialLoad = null;
    if (mzSearchByItem)
    {
      vsInitialItem = searchField.getText();
    }
    else
    {
      vsInitialLoad = searchField.getText();
    }

    AddLoadLineItem updateItemDetail = Factory.create(AddLoadLineItem.class,
        "Add Item Detail", vsInitialLoad, vsInitialItem);
    addSKDCInternalFrameModal(updateItemDetail, buttonPanel,
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
              refreshTable();
          }
      }
    });
  }

  /**
   *  Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected = sktable.getSelectedRowCount();
    if (totalSelected == 0)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }

    if (displayYesNoPrompt("Do you really want to Delete\n" +
                                      "all selected Item Details", "Delete Confirmation"))
    {
      // Get the reason code for deletion.
      ReasonCodeFrame reasonCodeFrame = new ReasonCodeFrame(DBConstants.REASONADJUST);
      String[] vsChoices = reasonCodeFrame.getMsChoices();
      if (vsChoices == null || vsChoices.length == 0)
      {
        deleteSelectedItems("");
      }
      else if (vsChoices.length == 1)
      {
        int vnColon = vsChoices[0].indexOf(':');
        deleteSelectedItems((vnColon == -1) ? vsChoices[0] : vsChoices[0].substring(0, vnColon));
      }
      else
      {
        addSKDCInternalFrameModal(reasonCodeFrame, new PropertyChangeListener() 
            {
              public void propertyChange(PropertyChangeEvent event) 
              {
                String prop = event.getPropertyName();
                if(prop.equals(FRAME_CHANGE))
                {
                  String reasonCode = (String) event.getNewValue();
                  int vnColon = reasonCode.indexOf(':');
                  deleteSelectedItems((vnColon == -1) ? reasonCode : reasonCode.substring(0, vnColon));
                }
              }
            });
      }
    
    }
  }
    
  /**
   * deleteSelectedItems - 
   * 
   * Desc: Deletes the selected lines and updates the window. 
   * 
   * Thread saftey: Because this can be called by another thread 
   * (a property fire event), and it updates the current screen, we must 
   * ensure that it is thread safe. That is, that if another thread updates 
   * this window, it should wait until the regular swing thread is done.
   * Adding it to the swing event queue will take care of this for us...
   */
  protected void deleteSelectedItems(final String reasonCode)
  {
    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);

/*        
        if(reasonCode == null)
        {
          displayError("A valid reason code must be selected");
          return;
        }
*/
        int totalSelected = sktable.getSelectedRowCount();

        String[] delItemList = null;
        String[] delLotList = null;
        String[] delLoadList = null;
        String[] delOrderIDList = null;
        String[] delOrderLotList = null;
        String[] delLineIDList = null;
        String[] delPositionIDList = null;
        
        // Get selected list of Item details
        delItemList = sktable.getSelectedColumnData(LoadLineItemData.ITEM_NAME);
        delLotList = sktable.getSelectedColumnData(LoadLineItemData.LOT_NAME);
        delLoadList = sktable.getSelectedColumnData(LoadLineItemData.LOADID_NAME);
        delOrderIDList = sktable.getSelectedColumnData(LoadLineItemData.ORDERID_NAME);
        delOrderLotList = sktable.getSelectedColumnData(LoadLineItemData.ORDERLOT_NAME);
        delLineIDList = sktable.getSelectedColumnData(LoadLineItemData.LINEID_NAME);
        delPositionIDList = sktable.getSelectedColumnData(LoadLineItemData.POSITIONID_NAME);

        int delCount = 0;
        int[] deleteIndices = sktable.getSelectedRows();
        for(int row = 0; row < totalSelected; row++)
        {
          try
          {
            invtServ.deleteLoadLineItem(delLoadList[row], delItemList[row], delLotList[row], delOrderIDList[row],
                delOrderLotList[row], delLineIDList[row],delPositionIDList[row], reasonCode);
            delCount++;
          }
          catch(DBException exc)
          {
            displayError(exc.getMessage(), "Delete Error");
            // De-Select the troubling row!
            sktable.deselectRow(deleteIndices[row]);
          }
        }
        if(delCount != totalSelected)
        {
          displayInfo("Deleted " + delCount + " of " + totalSelected + " selected rows", "Delete Result");
        }
        else
        {
          displayInfoAutoTimeOut("Deleted " + delCount + " of " + totalSelected + " selected rows", "Delete Result");
        }
        refreshTable();
      }
    });
  }

  /**
   * Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (isSelectionValidForModify(false))
    {
      LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
      Map m = new TreeMap(sktable.getSelectedRowData());
      // This map has some joined fields.  Remove them to avoid errors.
      m.remove(LoadData.WAREHOUSE_NAME);
      m.remove(LoadData.ADDRESS_NAME);
      m.remove(ItemMasterData.DESCRIPTION_NAME);
      // Convert the map to a LoadLineItemData
      vpLLIData.dataToSKDCData(m);

      if (vpLLIData.getOrderID().length() == 0)
      {
        ModifyLoadLineItem updateItemDetail = Factory.create(
            ModifyLoadLineItem.class, "Modify Item Detail", vpLLIData);
        
        addSKDCInternalFrameModal(updateItemDetail, buttonPanel,
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent e)
              {
                String prop = e.getPropertyName();
                if (prop.equals(FRAME_CHANGE))
                {
                  refreshTable();
                }
              }
            });
      }
      else
      {
        displayInfo(
            "Item Detail cannot be modified, already picked for an order",
            "Selection Error");
      }
    }
  }

  /**
   *  Handles Load Display button.
   */
  public void displayLoadButtonPressed()
  {
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to show load information",
                             "Selection Error");
      return;
    }
    else if (sktable.getSelectedRowCount() < 1)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    Object cObjLoad = sktable.getCurrentRowDataField(LoadLineItemData.LOADID_NAME);
    try
    {
      LoadListFrame vpLoadFrame = Factory.create(LoadListFrame.class, cObjLoad.toString());
      vpLoadFrame.setAllowDuplicateScreens(true);
      vpLoadFrame.setCategoryAndOption(getCategory(), "Loads");
      addSKDCInternalFrameModal(vpLoadFrame);
    }
    catch(Exception e)
    {
      displayInfo(e.getMessage());
    }
  }

  /**
   *  Action method to handle Detailed Search button. Brings up form with
   *  extended search criteria, gets criteria the operator entered, then
   *  refreshes list screen.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    final ItemDetailSearchFrame searchItem = Factory.create(ItemDetailSearchFrame.class);
    searchField.setText("");
    addSKDCInternalFrameModal(searchItem, new JPanel[] {buttonPanel, searchPanel},
      new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e)
        {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
          // get the search criteria
            idSearchData = searchItem.getSearchData();
            try
            {
              searchItem.setClosed(true);
            }
            catch (PropertyVetoException pve) {}
            refreshTable();
          }
        }
    });
  }

  /**
   * Mouse listener for the table 
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
      /**
       *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
       *  to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {
        popupMenu.add("Add",       ADD_BTN,        getDefaultListener());
        popupMenu.add("Modify",    MODIFY_BTN,     getDefaultListener());
        popupMenu.add("Delete", true, DELETE_BTN,  getDefaultListener());
        popupMenu.add("Show Load", SHOWDETAIL_BTN, mpListener);
        
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        if (ePerms != null)
        {
          popupMenu.setAuthorization("Add", ePerms.iAddAllowed);
          popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
        }

        return(popupMenu);
      }
      
      /**
       *  Display the Load Line screen.
       */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
    });
  }
  
  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return ItemDetailListFrame.class;
  }
}
