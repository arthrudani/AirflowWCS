package com.daifukuamerica.wrxj.swingui.recovery;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacTranslator;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.itemdetail.ItemDetailListFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of loads for recovery.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RecoveryListFrame extends SKDCListFrame
{
  protected LoadRecovery loadRecovery = null;
  protected SKDCButton detailViewButton;
  protected SKDCButton recoverButton;
  protected SKDCButton samButton;
  protected KeyObject[] mapFilterData = null;

  protected RecoveryButtonListener mpListener;
  
  protected final String SAM_BTN  = "Send_Arrival";
  
  /**
   *  Create recovery list frame.
   *
   */
  public RecoveryListFrame()
  {
    super("Recovery");
    setSearchData("Load", DBInfo.getFieldLength(LoadData.LOADID_NAME));
    setAutoRefreshVisible(true);
    setAutoRefreshSelected(true);
    setAutoRefreshInterval(5);
    sktable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    addButton.setVisible(false);
    modifyButton.setVisible(false);
    setDetailSearchVisible(true);
    setDisplaySearchCount(true, "Moving Load");
    setAutoRefreshInterval(4);
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

    recoverButton.setAuthorization(ePerms.iModifyAllowed);
    samButton.setAuthorization(ePerms.iModifyAllowed);

    createLoadRecovery();
    loadRecovery.setParentFrame(this);
    loadRecovery.initialize();

    if (mapFilterData != null)
    {
      refreshButtonPressed();
    }
    searchButtonPressed();
  }

  /**
   * Set the Load Recovery class to use.
   * 
   */
  protected void createLoadRecovery()
  {
    loadRecovery = Factory.create(LoadRecovery.class);
  }
  
  /**
   *  Method to set the search criteria filter.
   *
   *  @param iapSearchData ColumnObject containing criteria to use in search.
   */
  protected void setFilter(KeyObject[] iapSearchData)
  {
    mapFilterData = iapSearchData;
  }

  /**
   *  Method to intialize screen components. This adds the extra buttons
   *  to the screen.
   *
   */
  protected void addExtraButtons()
  {
    detailViewButton = new SKDCButton("Details", "Show Item Details in this Load", 'l');
    recoverButton = new SKDCButton("Recover", "Recover this Load", 'v');
    samButton = new SKDCButton("Send Arrival", "Send an Arrival message", 'S');

    detailViewButton.addEvent(SHOWDETAIL_BTN, mpListener);
    recoverButton.addEvent(ORDER_BTN, mpListener);
    samButton.addEvent(SAM_BTN, mpListener);

    getButtonPanel().add(detailViewButton);
    getButtonPanel().add(recoverButton);
    getButtonPanel().add(samButton);
   
    getButtonPanel().add(closeButton);
  }

  /**
   * 
   */
  @Override
  protected void addActionListeners()
  {
    super.addActionListeners();
    
    mpListener = new RecoveryButtonListener();
    addExtraButtons();
    
    deleteButton.removeActionListener(getDefaultListener());
    deleteButton.addEvent(DELETE_BTN, mpListener);
  }

  /**
   *  Button Listener class.
   */
  public class RecoveryButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(SHOWDETAIL_BTN))
      {
        displayRecoveryDetail();
      }
      else if (which_button.equals(SAM_BTN))
      {
        SAMButtonPressed();
      }
      else
      {
        pauseAutoRefresh();
        if (sktable.getSelectedRow() == -1)
        {
          displayInfoAutoTimeOut("No row selected", "Selection Error");
          resumeAutoRefresh();
          return;
        }
        
        if (which_button.equals(DELETE_BTN))
        {
          deleteButtonPressed();
        }
        else if (which_button.equals(ORDER_BTN))
        {
          recoverButtonPressed();
        }
        resumeAutoRefresh();
      }
    }
  }

  /**
   * Used for double-click, Detail button and Show Detail pop-up
   */
  protected void displayRecoveryDetail()
  {
    int vnSelectedRowIndex = sktable.getSelectedRow();
    
    if (vnSelectedRowIndex == -1)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row's Details can be Viewed at a time", "Selection Error");
      return;
    }
    
    Object mpObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    
    if (mpObj != null)  //we have one
    {
      ItemDetailListFrame vpViewItemDetails = Factory.create(ItemDetailListFrame.class);
      vpViewItemDetails.setLoadFilter(mpObj.toString());
      vpViewItemDetails.setAllowDuplicateScreens(true);
      addSKDCInternalFrameModal(vpViewItemDetails, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshButtonPressed();
            }
        }
      });
    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  };

  
  /**
   *  Action method to handle search button.
   *
   */
  @Override
  protected void searchButtonPressed()
  {
    List<KeyObject> vpSearchDataList = new LinkedList<KeyObject>();
    String vsLoadID = searchField.getText().trim();

    /*
     * Load ID
     */
    if (vsLoadID.length() > 0)
    {
      KeyObject vpLoadKey = new KeyObject(LoadData.LOADID_NAME, vsLoadID);
      vpLoadKey.setComparison(KeyObject.LIKE);
      vpLoadKey.setConjunction(KeyObject.AND);
      vpSearchDataList.add(vpLoadKey);
    }
    
    /*
     * Load Move Status
     */
    KeyObject vpStatusKey = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
      Integer.valueOf(DBConstants.NOMOVE));
    vpStatusKey.setComparison(KeyObject.NOT_EQUAL);
    vpSearchDataList.add(vpStatusKey);

    KeyObject vpStatusKey2 = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
      Integer.valueOf(DBConstants.PICKED));
    vpStatusKey2.setComparison(KeyObject.NOT_EQUAL);
    vpStatusKey2.setConjunction(KeyObject.AND);
    vpSearchDataList.add(vpStatusKey2);
    
    KeyObject vpStatusKey3 = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
      Integer.valueOf(DBConstants.STAGED));
    vpStatusKey3.setComparison(KeyObject.NOT_EQUAL);
    vpStatusKey3.setConjunction(KeyObject.AND);
    vpSearchDataList.add(vpStatusKey3);

    KeyObject vpStatusKey4 = new KeyObject(LoadData.LOADMOVESTATUS_NAME,
      Integer.valueOf(DBConstants.RECEIVED));
    vpStatusKey4.setComparison(KeyObject.NOT_EQUAL);
    vpStatusKey4.setConjunction(KeyObject.AND);
    vpSearchDataList.add(vpStatusKey4);

    /*
     * Refresh
     */
    setFilter(KeyObject.toKeyArray(vpSearchDataList));
    refreshButtonPressed();
  }

  /**
   *  Method to filter by extended search. Refreshes display.
   */
  @Override
  protected void refreshButtonPressed()
  {
    /*
     * If a row is selected, remember which one it is
     */
    String vsSelectedLoad = null;
    Object vpLoadID = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    if (vpLoadID != null)
    {
      vsSelectedLoad = vpLoadID.toString();
    }
      
    List vpList = loadRecovery.getLoadList(mapFilterData);
    if (vpList != null)
    {
      refreshTable(vpList);
      
      /*
       * Re-select the previously selected row
       */
      if (vsSelectedLoad != null)
      {
        for (int i = 0; i < sktable.getRowCount(); i++)
        {
          String vsLoadID = sktable.getRowData(i).get(LoadData.LOADID_NAME).toString();
          if (vsLoadID.equals(vsSelectedLoad))
          {
            sktable.addRowSelectionInterval(i,i);
            break;
          }
        }
      }
    }
  }

  /**
   *  Method to auto-refreshes the display.
   */
  @Override
  protected void executeAutoRefreshTable()
  {
    if (loadRecovery != null && mapFilterData != null)
    {
      refreshButtonPressed();
    }
  }

 /**
  *  Action method to handle Recover button. Method recovers the load.
  *  Verifies that load can be recovered. Updates the load and sends messages
  *  needed to recover.
  */
  /*--------------------------------------------------------------------------*/
  protected void recoverButtonPressed()
  {
    Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    // only allow modify if we have selected a load
    if (cObj == null)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    String loadId = cObj.toString();
    loadRecovery.recoverLoad(loadId);
    refreshButtonPressed();
  }

 /**
  *  Action method to handle Delete button.
  *
  */
  @Override
  protected void deleteButtonPressed()
  {
    Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    if (cObj != null)
    {
      String vsLoadId = cObj.toString();
      
      /*
       * Confirm the deletion!
       */
      if (displayWarningYesNoPrompt("Deleting load \"" + vsLoadId
          + "\" will remove it and any items\n"
          + "it may contain from the Warehouse Rx database!",
          "Are you sure you want to DELETE load \"" + vsLoadId + "\"",
          "Delete Confirmation"))
      {
        loadRecovery.deleteLoad(vsLoadId);
        displayInfoAutoTimeOut("Load \"" + vsLoadId + "\" deleted",
            "Delete Result");
      }
    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
    refreshButtonPressed();
  }
  
  /**
   * Method to display an Informational message in an option pane.
   * 
   * @param isWarning
   * @param isPrompt Text to be displayed.
   * @param isTitle Text to be displayed as title.
   * @return true if yes
   */
  protected boolean displayWarningYesNoPrompt(String isWarning,
      String isPrompt, String isTitle)
  {
    String[] vasOptions = new String[] { DacTranslator.getTranslation("Yes"),
        DacTranslator.getTranslation("No") };

    String vsDisplayText = "<html><font color=RED>"
        + isWarning.replace("\n", "<br>") + "</font><br><br>" + isPrompt;
    if (!vsDisplayText.endsWith("?"))
    {
      vsDisplayText += "?";
    }
    vsDisplayText += "</html>";

    int vnResponse = JOptionPane.showOptionDialog(this, vsDisplayText, isTitle,
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
        vasOptions, vasOptions[1]);
    return vnResponse == 0;
  }

  /**
   * Action method to handle Send Arrival Button.
   */
  protected void SAMButtonPressed()
  {
    SendArrivalFrame SendArrivalFrame = Factory.create(SendArrivalFrame.class);
    addSKDCInternalFrameModal(SendArrivalFrame, new JPanel[] {buttonPanel, searchPanel},
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            refreshButtonPressed();
          }
      }
    });
  }

  /**
   *  Action method to handle Detailed Search button. Brings up form with
   *  extended search criteria, gets criteria the operator entered, then
   *  refreshs list screen.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    final RecoveryDetailedSearchFrame vpSearchLoad = 
      Factory.create(RecoveryDetailedSearchFrame.class);
    vpSearchLoad.setTitle("Recovery Search");
    searchField.setText("");
//    searchLoad.setExternalControl(true);
    addSKDCInternalFrameModal(vpSearchLoad, new JPanel[] {buttonPanel, searchPanel},
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
          // get the search criteria
            setFilter(vpSearchLoad.getSearchKeyData());
            try
            {
              vpSearchLoad.setClosed(true);
            }
            catch (PropertyVetoException pve) {}
            refreshButtonPressed();
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
        popupMenu.add("Delete", DELETE_BTN, mpListener);
        popupMenu.add("Recover", ORDER_BTN, mpListener);
        popupMenu.add("View Detail", SHOWDETAIL_BTN, mpListener);

        if (ePerms != null)
        {
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
          popupMenu.setAuthorization("Recover", ePerms.iModifyAllowed);
        }
        
        return(popupMenu);
      }
     /**
      *  Display detail screen.
      */
      @Override
      public void displayDetail()
      {
        displayRecoveryDetail();
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
    return RecoveryListFrame.class;
  }
}
