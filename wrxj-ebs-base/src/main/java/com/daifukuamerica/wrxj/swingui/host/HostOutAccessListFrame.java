package com.daifukuamerica.wrxj.swingui.host;

import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccessData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    Host Configuration Operation
 *
 * @author       Y. Kang
 * @version      1.0
 * <BR>Created:  30-Mar-2009<BR>
 *     Copyright (c) 2009<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class HostOutAccessListFrame extends SKDCListFrame
{
  private final String ENABLE_BTN      = "ENABLE";
  private final String DISABLE_BTN     = "DISABLE";

  private SKDCButton      mpBtnEnableButton;
  private SKDCButton      mpBtnDisableButton;
  private ButtonListener  mpButtonListener = new ButtonListener();

  private boolean mzAutoUpdate = false;
  private StandardConfigurationServer mpConfigSrvr;
  private HostOutAccessData           mpHOASearchData;
 
  /**
   *  Create Configuration Screen.
   *
   *  @param isTitle Title to be displayed.
   */
  public HostOutAccessListFrame(String isTitle)
  {
    super("HostOutAccessDetail");
    userData = new SKDCUserData();
    setSearchData("HostName", DBInfo.getFieldLength(HostOutAccessData.HOSTNAME_NAME));
    setDetailSearchVisible(true);
    setDisplaySearchCount(true, "Host Out Access Detail");
    setupButtonPanel();
    
    mpConfigSrvr = Factory.create(StandardConfigurationServer.class);
    mpHOASearchData = Factory.create(HostOutAccessData.class);
  }

  /**
   *  Create Configuration Screen.
   */
  public HostOutAccessListFrame()
  {
    this("Host Configuration");
    setHostNameFilter("");
  }

  /**
   * Set up the Button panel
   */
  private void setupButtonPanel()
  {
    // DIsable Modify button
    modifyButton.setVisible(false);
    modifyButton.setEnabled(false);
    
    // Create Enable and DIsable buttons
    mpBtnEnableButton = new SKDCButton("Enable", "Enable", 'E');
    mpBtnDisableButton = new SKDCButton("Disable", "Disable", 'i');
    
    // Add the event listener
    mpBtnEnableButton.addEvent(ENABLE_BTN, mpButtonListener);
    mpBtnDisableButton.addEvent(DISABLE_BTN, mpButtonListener);

    // Add Enable and Disable buttons to the button panel
    buttonPanel.add(mpBtnEnableButton);
    buttonPanel.add(mpBtnDisableButton);
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

    mpBtnEnableButton.setAuthorization(ePerms.iModifyAllowed);
    mpBtnDisableButton.setAuthorization(ePerms.iModifyAllowed);
    
    if (ePerms.iModifyAllowed)
    {
      mpBtnEnableButton.setVisible(true);
      mpBtnDisableButton.setVisible(true);
    }
    
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
    mpHOASearchData.clear();
    for(int keyIdx = 0; keyIdx < searchData.length; keyIdx++)
    {
      mpHOASearchData.addKeyObject(searchData[keyIdx]);
    }
  }

  /**
   *  Method to set the search criteria filter.
   *
   *  @param sItem ColumnObject containing criteria to use in search.
   */
  public void setHostNameFilter(String sHostName)
  {
    mzAutoUpdate = true;
    setSearchData("HostName", DBInfo.getFieldLength(HostOutAccessData.HOSTNAME_NAME));
    searchField.setText(sHostName);
    mpHOASearchData.clear();

    if (sHostName.length() > 0)
    {
      mpHOASearchData.setKey(HostOutAccessData.HOSTNAME_NAME, sHostName, KeyObject.LIKE);
    }
  }

  /**
   *  Method to reset the search filter.
   *
   */
  public void clearFilter()
  {
    mpHOASearchData.clear();
  }

  /**
   * Method to filter by extended search. Refreshes display.
   */
  public void refreshTable()
  {
    // Make sure we use the default sort
    mpHOASearchData.clearOrderByColumns();
    mpHOASearchData.addOrderByColumn(HostOutAccessData.HOSTNAME_NAME);
    mpHOASearchData.addOrderByColumn(HostOutAccessData.MESSAGEIDENTIFIER_NAME);

    try
    {
      List<Map> vapList = mpConfigSrvr.getHostOutAccessData(mpHOASearchData);
      refreshTable(vapList);
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage());
    }
  }
  
  /**
   *  Button Listener class.
   */
  private class ButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String vsButton = e.getActionCommand();

      if (vsButton.equals(ENABLE_BTN))          enableButtonPressed();
      else if (vsButton.equals(DISABLE_BTN))    disableButtonPressed();
    }
  }
  
  /**
   *  Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    setHostNameFilter(searchField.getText());
    refreshTable();
  }

  /**
   * Method to refresh data
   */
  @Override
  protected void refreshButtonPressed()
  {
    refreshTable();
  }
  
  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateHostOutAccess vpUpdateHostOutAccess = Factory.create(UpdateHostOutAccess.class, "Add Host Out Access Detail");
    vpUpdateHostOutAccess.setAdd(searchField.getText());
    addSKDCInternalFrameModal(vpUpdateHostOutAccess, buttonPanel,
      new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE) || prop.equals(FRAME_CLOSING))
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
    int vnTotalSelected = sktable.getSelectedRowCount();
    if (vnTotalSelected == 0)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
    }
    else if (displayYesNoPrompt("Do you really want to Delete\n" +
                           "all selected rows", "Delete Confirmation"))
    {
      deleteSelectedRows(vnTotalSelected);
    }
  }
    
  /**
   * deleteSelectedItems - 
   * 
   */
  protected void deleteSelectedRows(int inTotalSelected)
  {
    int vnDelCount = 0;
    List<Map> vapList = sktable.getSelectedRowDataArray();
    
    for(int row = 0; row < vapList.size(); row++)
    {
      Map vpObj = vapList.get(row);
      try
      {
        String vsHostName = DBHelper.getStringField(vpObj, HostOutAccessData.HOSTNAME_NAME);
        String vsMsgId = DBHelper.getStringField(vpObj, HostOutAccessData.MESSAGEIDENTIFIER_NAME);
        mpConfigSrvr.deleteHostOutAccess(vsHostName, vsMsgId );
        vnDelCount++;
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Delete Error");
        // De-Select the troubling row!
        sktable.deselectRow(row);
      }
    }
    displayInfo("Deleted " + vnDelCount + " of " + inTotalSelected + " selected rows",
                "Delete Result");
    refreshTable();
  }

  /**
   * Action method to handle Enable button.
   */
  protected void enableButtonPressed()
  {
    int vnTotalSelected = sktable.getSelectedRowCount();
    if (vnTotalSelected == 0)
    {
      displayInfoAutoTimeOut("No row selected to Enable", "Selection Error");
    }
    else if (displayYesNoPrompt("Do you really want to Enable\n" +
                           "all selected rows", "Enable Confirmation"))
    {
      updateSelectedRows(vnTotalSelected, DBConstants.YES);
    }
  }

  /**
   *  Action method to handle Disable button.
   */
  protected void disableButtonPressed()
  {
    int vnTotalSelected = sktable.getSelectedRowCount();
    if (vnTotalSelected == 0)
    {
      displayInfoAutoTimeOut("No row selected to Disable", "Selection Error");
    }
    else if (displayYesNoPrompt("Do you really want to Disable\n" +
                           "all selected rows", "Disable Confirmation"))
    {
      updateSelectedRows(vnTotalSelected, DBConstants.NO);
    }
  }
  
  /**
   * updateSelectedItems - 
   * 
   */
  protected void updateSelectedRows(int inTotalSelected, int inEnabled)
  {
    int vnModCount = 0;
    List<Map> vapList = sktable.getSelectedRowDataArray();
    
    for(int row = 0; row < vapList.size(); row++)
    {
      Map vpObj = vapList.get(row);
      try
      {
        String vsHostName = DBHelper.getStringField(vpObj, HostOutAccessData.HOSTNAME_NAME);
        String vsMsgId = DBHelper.getStringField(vpObj, HostOutAccessData.MESSAGEIDENTIFIER_NAME);
        mpConfigSrvr.modifyEnabledFlag(vsHostName, vsMsgId, inEnabled );
        vnModCount++;
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Update Error");
        // De-Select the troubling row!
        sktable.deselectRow(row);
      }
    }
    displayInfo("Update " + vnModCount + " of " + inTotalSelected + " selected rows",
                "Update Result");
    refreshTable();
  }

  /**
   *  Action method to handle Detailed Search button. Brings up form with
   *  extended search criteria, gets criteria the operator entered, then
   *  refreshes list screen.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    final HostOutAccessSearchFrame searchItem = Factory.create(HostOutAccessSearchFrame.class);
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
            mpHOASearchData = searchItem.getSearchData();
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
        popupMenu.add("Enable",    ENABLE_BTN,     mpButtonListener);
        popupMenu.add("Disable",   DISABLE_BTN,    mpButtonListener);
        popupMenu.add("Delete", true, DELETE_BTN,  getDefaultListener());
        
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        if (ePerms != null)
        {
          popupMenu.setAuthorization("Add",     ePerms.iAddAllowed);
          popupMenu.setAuthorization("Enable",  ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Disable", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete",  ePerms.iDeleteAllowed);
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
    return HostConfigMain.class;
  }
}
