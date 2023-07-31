package com.daifukuamerica.wrxj.swingui.item;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacLargeListFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.itemdetail.ItemDetailListFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of items.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ItemMasterListFrame extends DacLargeListFrame
{
  protected static final String QUANTITY_BTN = "QUANTITIES";
  
  protected StandardInventoryServer invtServ = null;
  protected ColumnObject[] filterData = null;

  protected SKDCButton mpBtnQuantities = null;
  protected ItemMaster mpItemMaster = Factory.create(ItemMaster.class);
  
  /**
   *  Create Item list frame.
   */
  public ItemMasterListFrame()
  {
    super("ItemMaster");
    setDisplaySearchCount(true, "Item");
    addExtraButtons();
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    invtServ.cleanUp();
    invtServ = null;
  }

  /**
   * Sets screen permissions.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> augments the
   * supermethod by setting the screen permissions.</p>
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    if (ePerms == null)
      return;
    invtServ = Factory.create(StandardInventoryServer.class);
    setSearchData("Item ID", DBInfo.getFieldLength(ItemMasterData.ITEM_NAME));
    searchButtonPressed();
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
        popupMenu.add("Add", ADD_BTN, getDefaultListener());
        popupMenu.add("Modify", MODIFY_BTN, getDefaultListener());
        popupMenu.add("Delete", true, DELETE_BTN, getDefaultListener());
        popupMenu.add("Show Details", VIEW_BTN, getDefaultListener());
        popupMenu.add("Show Quantities", QUANTITY_BTN, new buttonListener());
        
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
      *  Display the screen.
      */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
    });
  }

  /**
   * 
   */
  protected void addExtraButtons()
  {
    viewButton.setText("Detail");
    viewButton.setToolTipText("Show Item Details for the selected Item");
    viewButton.setMnemonic('l');
    setViewButtonVisible(true);
    
    mpBtnQuantities = new SKDCButton("Show Quantities", "Show item quantities", 'Q');
    mpBtnQuantities.addEvent(QUANTITY_BTN, new buttonListener());
    buttonPanel.add(mpBtnQuantities);
    buttonPanel.add(closeButton);
  }

  /**
   *  Button Listener class.
   */
  private class buttonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(QUANTITY_BTN))
      {
        quantityButtonPressed();
      }
    }
  }

  /**
   *  Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    refreshTable(searchField.getText());
  }

  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateIM updateIM = Factory.create(UpdateIM.class, "Add");
    addSKDCInternalFrameModal(updateIM, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (prop.equals(FRAME_CHANGE))
        {
          refreshTable(searchField.getText());
        }

      }
    });
  }

  /**
   *  Action method to handle Modify button. Brings up screen to do the update.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be Modified at a time", "Selection Error");
      return;
    }
    Object cObj = sktable.getCurrentRowDataField(ItemMasterData.ITEM_NAME);
    // only allow modify if we have selected an item
    if (cObj != null)  //we have one
    {
      UpdateIM updateIM = Factory.create(UpdateIM.class, "Modify");
      updateIM.setModify(cObj.toString());

      addSKDCInternalFrameModal(updateIM, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshTable(searchField.getText());
            }
        }
      });

    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  }

  /**
   *  Action method to handle Delete button.
   */
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to delete", "Selection Error");
      return;
    }

    if (displayYesNoPrompt("Do you really want to delete\n" +
                                      "all selected Items", "Delete Confirmation "))
    {
      String[] delItemList = null;
                                       // Get selected list of Item IDs
      delItemList = sktable.getSelectedColumnData(ItemMasterData.ITEM_NAME);

      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();
      for(int row = 0; row < totalSelected; row++)
      {
        try
        {
          invtServ.deleteItemMaster(delItemList[row]);
          delCount++;
        }
        catch(DBException exc)
        {
          displayError(exc.getMessage(), "Delete Error");
                                       // De-Select the troubling row!
          sktable.deselectRow(deleteIndices[row]);
        }
      }
      if (delCount != totalSelected)
      {
        displayInfo("Deleted " +  delCount + " of " + totalSelected +
                    " selected rows",
                    "Delete Result");
      }
      else
      {
        displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                               " selected rows", "Delete Result");
      }
      sktable.deleteSelectedRows();    // Update the display.
    }
  }

  /**
   * Display the item/lot quantities screen
   */
  private void quantityButtonPressed()
  {
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be viewed at a time", "Selection Error");
      return;
    }
    Object cObj = sktable.getCurrentRowDataField(ItemMasterData.ITEM_NAME);
    // only allow modify if we have selected an item
    if (cObj != null)  //we have one
    {
      setInfo(" ");
      
      ItemLotQuantityFrame vpIMQty = Factory.create(ItemLotQuantityFrame.class, 
          cObj.toString(), invtServ, ePerms.iModifyAllowed);

      addSKDCInternalFrameModal(vpIMQty, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshTable(searchField.getText());
            }
        }
      });

    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  }
  
  /**
   *  Action method to handle View button. Brings up screen to show
   *  the load line items for this item master.
   */
  @Override
  protected void viewButtonPressed()
  {
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be Viewed at a time", "Selection Error");
      return;
    }
    Object cObj = sktable.getCurrentRowDataField(ItemMasterData.ITEM_NAME);
    ItemDetailListFrame vpViewItemDetails = Factory.create(ItemDetailListFrame.class);
    vpViewItemDetails.setCategoryAndOption(getCategory(), "Item Details");
    if (cObj != null)  //we have one
    {
      List searchData = new LinkedList();
      searchData.add(new ColumnObject(ItemMasterData.ITEM_NAME,cObj.toString()));

      vpViewItemDetails.setItemFilter(cObj.toString());
      vpViewItemDetails.setAllowDuplicateScreens(true);
      addSKDCInternalFrameModal(vpViewItemDetails, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshTable(searchField.getText());
            }
        }
      });
    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  }

  /**
   *  Method to filter by name. Refreshes display.
   *
   *  @param isPartialItem Item to search for.
   */
  protected void refreshTable(String isPartialItem)
  {
    ItemMasterData vpIMKey = Factory.create(ItemMasterData.class);
    if (isPartialItem.trim().length() > 0)
    {
      KeyObject vpKey = new KeyObject(ItemMasterData.ITEM_NAME, isPartialItem);
      vpKey.setComparison(KeyObject.LIKE);
      vpIMKey.addKeyObject(vpKey);
    }
    vpIMKey.addOrderByColumn(ItemMasterData.ITEM_NAME);
    
    startSearch(mpItemMaster, vpIMKey);
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
    return ItemMasterListFrame.class;
  }
}