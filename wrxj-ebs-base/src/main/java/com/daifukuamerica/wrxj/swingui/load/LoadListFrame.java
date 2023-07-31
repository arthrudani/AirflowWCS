package com.daifukuamerica.wrxj.swingui.load;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
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
import com.daifukuamerica.wrxj.swingui.itemdetail.ItemDetailListFrame;
import com.daifukuamerica.wrxj.swingui.itemdetail.ReasonCodeFrame;
import com.daifukuamerica.wrxj.swingui.location.LocationMain;
import com.daifukuamerica.wrxj.swingui.move.MoveListFrame;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for displaying a list of loads.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class LoadListFrame extends DacLargeListFrame
{
  public static final String LOCATION_BTN = "LOCATION";
  public static final String OLDEST_BTN = "OLDEST";
  
  protected boolean mzAutoUpdate = false;
  
  protected SKDCButton mpOrderButton;
  protected SKDCButton mpDetailViewButton;
  protected SKDCButton mpLocationButton;
  protected SKDCButton mpOldestButton;
  protected SKDCButton mpMovesButton;

  protected StandardLoadServer mpLoadServer;
  protected Load mpLoad = Factory.create(Load.class);
  protected LoadData mpSearchKey = Factory.create(LoadData.class);

  protected LoadButtonListener mpListener;
  
  /**
   * Create the LoadListFrame.
   */
  public LoadListFrame()
  {
    super("Load");
    ePerms = userData.getOptionPermissionsByClass(LoadListFrame.class);
    setSearchData("Load", DBInfo.getFieldLength(LoadData.LOADID_NAME));
    setDetailSearchVisible(true);
    mpLoadServer = Factory.create(StandardLoadServer.class);
  }

  /**
   * Create the LoadListFrame with an initial Load ID to search on.
   * 
   * <P>Used by ItemDetailListFrame.</P>
   * 
   * @param sLoadid
   */
  public LoadListFrame(String sLoadid)
  {
    this();
    setLoadFilter(sLoadid);
  }

  /**
   * Create the LoadListFrame with an initial criteria to search on.
   * 
   * <P>Used by LocationMain.</P>
   * 
   * @param iMoveStatus
   * @param sContainer
   * @param sZone
   * @param sWarehouse
   * @param sAddress
   * @param sDestWare
   * @param sDestAdd
   */
  public LoadListFrame(int iMoveStatus, String sContainer, String sZone,
      String sWarehouse, String sAddress, String sDestWare, String sDestAdd)
  {
    this();
    
    LoadData vpLoadKey = Factory.create(LoadData.class);
    
    vpLoadKey.addKeyObject(new KeyObject(LoadData.LOADMOVESTATUS_NAME,
      Integer.valueOf(iMoveStatus)));
    if (sZone.length() > 0)
    {
      vpLoadKey.addKeyObject(new KeyObject(LoadData.RECOMMENDEDZONE_NAME, sZone));
    }
    if (sContainer.length() > 0)
    {
      vpLoadKey.addKeyObject(new KeyObject(LoadData.CONTAINERTYPE_NAME,
          sContainer));
    }
    if (sWarehouse.length() > 0)
    {
      vpLoadKey.addKeyObject(new KeyObject(LoadData.WAREHOUSE_NAME, sWarehouse));
      vpLoadKey.addKeyObject(new KeyObject(LoadData.ADDRESS_NAME, sAddress));
    }
    if (sDestWare.length() > 0)
    {
      vpLoadKey.addKeyObject(new KeyObject(LoadData.FINALWAREHOUSE_NAME,
          sDestWare));
      vpLoadKey.addKeyObject(new KeyObject(LoadData.FINALADDRESS_NAME, sDestAdd));
    }
    setFilter(vpLoadKey);
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

    mpOrderButton.setAuthorization(ePerms.iModifyAllowed);
    mpOldestButton.setAuthorization(ePerms.iModifyAllowed);

    if (mzAutoUpdate)
    {
      if (mpSearchKey != null)
      {
        refreshTable();
      }
      else
      {
        searchButtonPressed();
      }
    }
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
  }

  /**
   *  Method to initialize screen components. This adds the extra buttons
   *  to the screen.
   */
  protected void addExtraButtons()
  {
    mpDetailViewButton = new SKDCButton("Details", "Show Item Details in the selected Load", 'l');
    mpLocationButton   = new SKDCButton("Location", "Show Location", 'o');
    mpMovesButton      = new SKDCButton("Moves", "Show Moves for this Load", 'v');
    
    mpOrderButton      = new SKDCButton("Retrieve", "Retrieve this Load", 'e');
    mpOldestButton     = new SKDCButton("Oldest", "Make this Load the oldest at the Station");

    getButtonPanel().add(Box.createHorizontalStrut(10));

    getButtonPanel().add(mpOrderButton);
    /*
     * Mike hasn't seen this used since Merit, and thinks that it might be best
     * to keep this for SuperUser (su) only.
     */
    if (SKDCUserData.isSuperUser())
      getButtonPanel().add(mpOldestButton); 

    getButtonPanel().add(Box.createHorizontalStrut(10));
    
    getButtonPanel().add(mpDetailViewButton);
    getButtonPanel().add(mpLocationButton);
    getButtonPanel().add(mpMovesButton);
    
    getButtonPanel().add(Box.createHorizontalStrut(10));
    
    getButtonPanel().add(closeButton);
  }

  /**
   * Method to set the search criteria filter.
   * 
   * @param ipSearchKey ColumnObject containing criteria to use in search.
   */
  public void setFilter(LoadData ipSearchKey)
  {
    mzAutoUpdate = true;
    mpSearchKey = ipSearchKey.clone();
    
    // Make sure we use the default sort
    mpSearchKey.clearOrderByColumns();
    mpSearchKey.addOrderByColumn(LoadData.LOADID_NAME);
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#addActionListeners()
   */
  @Override
  protected void addActionListeners()
  {
    super.addActionListeners();
    
    addExtraButtons();
    
    mpListener = new LoadButtonListener();
    mpOrderButton.addEvent(ORDER_BTN, mpListener);
    mpOldestButton.addEvent(OLDEST_BTN, mpListener);
    mpDetailViewButton.addEvent(SHOWDETAIL_BTN, mpListener);
    mpLocationButton.addEvent(LOCATION_BTN, mpListener);
    mpMovesButton.addEvent(MOVES_BTN, mpListener);
  }

  /**
   * Button Listener class.
   */
  private class LoadButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(ORDER_BTN))
      {
        orderButtonPressed();
      }
      else if (which_button.equals(OLDEST_BTN))
      {
        oldestButtonPressed();
      }
      else if (which_button.equals(SHOWDETAIL_BTN))
      {
        detailViewButtonPressed();
      }
      else if (which_button.equals(LOCATION_BTN))
      {
        locationButtonPressed();
      }
      else if(which_button.equals(MOVES_BTN))
      {
        movesButtonPressed();
      }
    }
  }

  /**
   * Open the location screen
   */
  protected void locationButtonPressed()
  {
    LoadData vpLoadData = Factory.create(LoadData.class);
    int selectedRowIndex = sktable.getSelectedRow();

    /*
     * Make sure one load is selected
     */
    if (selectedRowIndex == -1)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    vpLoadData.dataToSKDCData(sktable.getRowData(selectedRowIndex));
    try
    {
      LocationMain vpLocMain = Factory.create(LocationMain.class,
          vpLoadData.getWarehouse(), vpLoadData.getAddress(),
          vpLoadData.getShelfPosition());
      vpLocMain.setAllowDuplicateScreens(true);
      addSKDCInternalFrameModal(vpLocMain);
    }
    catch(Exception e)
    {
      displayInfo(e.getMessage());
    }
  }
  
  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#refreshButtonPressed()
   */
  @Override
  protected void refreshButtonPressed()
  {
    refreshTable();
  }
  
  /**
   * Action method to handle search button.
   */
  @Override
  protected void searchButtonPressed()
  {
    setLoadFilter(searchField.getText());
    refreshTable();
  }

  /**
   * Method to set the search criteria filter.
   * 
   * @param isLoadID ColumnObject containing criteria to use in search.
   */
  public void setLoadFilter(String isLoadID)
  {
    searchField.setText(isLoadID);
    LoadData vpLoadKey = Factory.create(LoadData.class);
    if (isLoadID.length() > 0)
    {
      KeyObject vpLoadKeyObject = new KeyObject(LoadData.LOADID_NAME, isLoadID);
      // Accept partial load searches.
      vpLoadKeyObject.setComparison(KeyObject.LIKE);
      vpLoadKey.addKeyObject(vpLoadKeyObject);
    }
    setFilter(vpLoadKey);
  }

  /**
   * Method to filter by extended search. Refreshes display.
   */
  public void refreshTable()
  {
    startSearch(mpLoad, mpSearchKey);
  }

  /**
   * Action method to handle Order load button. Brings up screen to order out
   * this load.
   */
  protected void orderButtonPressed()
  {
    OrderLoad orderLoad = Factory.create(OrderLoad.class);
    if (sktable.getSelectedRowCount() > 1)
    {
      orderLoad.setTitle("Retrieve Loads");
                                       // Get selected list of loads
      String[] retrieveLoadList = sktable.getSelectedColumnData(LoadData.LOADID_NAME);
      orderLoad.setListOfLoads(retrieveLoadList);
    }
    else
    {
      orderLoad.setTitle("Retrieve Load");
      Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
      if (cObj != null)  //we have one
      {
        orderLoad.setLoadID(cObj.toString());
      }
      else
      {
        orderLoad.setLoadID("");
      }
    }

    addSKDCInternalFrameModal(orderLoad, new JPanel[] {buttonPanel, searchPanel},
      new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
            sktable.clearSelection();
          }
          else if (prop.equals(FRAME_CLOSING))
          {
            refreshTable();
          }
      }
    });
  }

  /**
   * Make the load the oldest at the station
   */
  void oldestButtonPressed()
  {
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row can be Modified at a time", "Selection Error");
      return;
    }
    if (sktable.getSelectedRowCount() < 1)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    String vsLoadID = cObj.toString();

    StandardStationServer vpStationServer = Factory.create(StandardStationServer.class);
    
    LoadData vpLoadData = mpLoadServer.getLoad(vsLoadID);
    if (!vpStationServer.exists(vpLoadData.getAddress()))
    {
      displayInfoAutoTimeOut("Load " + vsLoadID + " is not at a station.");
      return;
    }

    if (displayYesNoPrompt("Make load " + vsLoadID + " the oldest load at " + vpLoadData.getAddress() + "?"))
    {
      mpLoadServer.moveLoadToStation(vsLoadID, vpLoadData.getAddress(), vpLoadData.getLoadMoveStatus(), 0);
    }
  }
  
  /**
   *  Action method to handle Add button. Brings up screen to do the add.
   */
  @Override
  protected void addButtonPressed()
  {
    UpdateLoad updateLoad = Factory.create(UpdateLoad.class, "Add Load");
    addSKDCInternalFrameModal(updateLoad, new JPanel[] {buttonPanel, searchPanel},
      new PropertyChangeListener() {
      @Override
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
    Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    // only allow modify if we have selected a load
    if (cObj != null)
    {
      final UpdateLoad updateLoad = Factory.create(UpdateLoad.class, "Modify Load");
      updateLoad.setModify(cObj.toString());

      addSKDCInternalFrameModal(updateLoad, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
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
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
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
                           "all selected Loads", "Delete Confirmation"))
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
              @Override
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
      @Override
      public void run()
      {
        if(reasonCode == null)
        {
          displayError("A valid reason code must be selected");
          return;
        }

        StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);
        int totalSelected = sktable.getSelectedRowCount();

        String[] delLoadList = null;
        // Get selected list of loads
        delLoadList = sktable.getSelectedColumnData(LoadData.LOADID_NAME);

        int delCount = 0;
        int[] deleteIndices = sktable.getSelectedRows();
        for(int row = 0; row < totalSelected; row++)
        {
          try
          {
            invtServ.deleteLoad(delLoadList[row], reasonCode);
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
        refreshTable(); // Update the display.
      }
    });
  }

  /**
   * Action method to handle Detailed Search button. Brings up form with
   * extended search criteria, gets criteria the operator entered, then refreshs
   * list screen.
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    final LoadDetailedSearchFrame searchLoad = Factory.create(LoadDetailedSearchFrame.class);
    searchField.setText("");
    addSKDCInternalFrameModal(searchLoad, new JPanel[] {buttonPanel, searchPanel},
    new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e)
        {
          String prop = e.getPropertyName();
          if (prop.equals(FRAME_CHANGE))
          {
          // get the search criteria
            setFilter(searchLoad.getSearchData());
            try
            {
              searchLoad.setClosed(true);
            }
            catch (PropertyVetoException pve) {}
            refreshTable();
          }
        }
       });
  }
  
  /**
   *  Action method to handle Moves details button. Brings up a screen that 
   *  shows the moves for this load
   */
  protected void movesButtonPressed()
  {
    if(sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row's Moves can be viewed at a time", "Selection Error");
      return;
    }
    Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    if(cObj != null)
    {
      MoveListFrame moveFrame = Factory.create(MoveListFrame.class);
      moveFrame.setLoadFilter(cObj.toString());
      moveFrame.refreshTable();
      moveFrame.setAllowDuplicateScreens(true);
      addSKDCInternalFrameModal(moveFrame);
    }
    else
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  }
  

  /**
   *  Action method to handle View details button. Brings up screen to show
   *  the load line items for this load.
   */
  protected void detailViewButtonPressed()
  {
    if (sktable.getSelectedRowCount() > 1)
    {
      displayInfoAutoTimeOut("Only one row's Details can be Viewed at a time", "Selection Error");
      return;
    }
    Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    ItemDetailListFrame viewLoad = Factory.create(ItemDetailListFrame.class);
    viewLoad.setCategoryAndOption(getCategory(), "Item Details");
    if (cObj != null)  //we have one
    {
      viewLoad.setLoadFilter(cObj.toString());
      viewLoad.setAllowDuplicateScreens(true);
      addSKDCInternalFrameModal(viewLoad, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
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
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
  }

  /**
   *  Action method to handle View details button. Brings up screen to show
   *  all fields for this Load.
   */
  @Override
  protected void viewButtonPressed()
  {
    Object cObj = sktable.getCurrentRowDataField(LoadData.LOADID_NAME);
    // only allow modify if we have selected a load
    if (cObj != null)
    {
      final UpdateLoad updateLoad = Factory.create(UpdateLoad.class, "View Load");
      updateLoad.setView(cObj.toString());

      addSKDCInternalFrameModal(updateLoad, new JPanel[] {buttonPanel, searchPanel},
        new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
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
      displayInfoAutoTimeOut("No row selected", "Selection Error");
    }
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
        popupMenu.add("Add",           ADD_BTN,        getDefaultListener());
        popupMenu.add("Modify",        MODIFY_BTN,     getDefaultListener());
        popupMenu.add("Delete", true,  DELETE_BTN,     getDefaultListener());

        popupMenu.add("Show Details",     SHOWDETAIL_BTN, mpListener);
        popupMenu.add("Show Location",    LOCATION_BTN,   mpListener);
        popupMenu.add("Show Moves", true, MOVES_BTN,      mpListener);
        
        popupMenu.add("Retrieve",      ORDER_BTN,      mpListener);
        /*
         * Mike hasn't seen this used since Merit, and thinks that it might be 
         * best to keep this for SuperUser (su) only.
         */
        if (SKDCUserData.isSuperUser())
          popupMenu.add("Make Oldest",   OLDEST_BTN,     mpListener);
        
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        if (ePerms != null)
        {
          popupMenu.setAuthorization("Add", ePerms.iAddAllowed);
          popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
          
          popupMenu.setAuthorization("Retrieve", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Make Oldest", ePerms.iModifyAllowed);
        }

        return(popupMenu);
      }
      /**
       *  Display the Load Line screen.
       */
      @Override
      public void displayDetail()
      {
        detailViewButtonPressed();
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
  protected Class<?> getRoleOptionsClass()
  {
    return LoadListFrame.class;
  }
}