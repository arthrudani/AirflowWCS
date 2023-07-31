package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDedicationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacLargeListFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.load.LoadListFrame;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    Primary frame for Location operations.
 *
 * @author       A.D.<br>
 *               12-12-02 Added double click and popup menu features.
 * @version      1.0
 * <BR>Created: 20-Apr-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class LocationMain extends DacLargeListFrame
{
  // Custom search panel
  protected LocationPanel mpLocPanel;
  
  // Custom buttons
  protected SKDCButton mpBtnMaint;
  protected SKDCButton mpBtnOrder;
  protected SKDCButton mpBtnLocnDet;
  protected LCButtonListener mpLocButtonListener;

  // Custom pop-up menu
  protected SKDCPopupMenu mpPopupMenu = new SKDCPopupMenu();
  
  // Lists to populate once to avoid repetitive database hits
  protected String[] masWarehouses;
  protected String[] masWarehousesWithAll;
  protected String[] masDevices;
  protected String[] masDevicesWithAll;
  
  // Data Servers
  protected StandardLocationServer      mpLocServer;
  protected StandardStationServer       mpStnServer;
  protected StandardDedicationServer    mpDedServer;
  
  // DB Interface Object
  protected Location mpLocation = Factory.create(Location.class);

  // Data objects
  protected LocationData mpLocData = Factory.create(LocationData.class);
  protected LocationData mpSearchCriteria;
  
  /**
   * Public constructor for Factory
   */
  public LocationMain()
  {
    super("Location");

    // Our servers
    mpDedServer = Factory.create(StandardDedicationServer.class);
    mpLocServer = Factory.create(StandardLocationServer.class);
    mpStnServer = Factory.create(StandardStationServer.class);
    
    // Custom button listener
    mpLocButtonListener = new LCButtonListener();

    // Fill in the device lists once only.
    fillLists();
    
    // Set up the custom search panel
    mpLocPanel = Factory.create(LocationPanel.class);
    mpLocPanel.setWarehouseList(masWarehouses);
    mpLocPanel.addEvent(SEARCH_BTN, mpLocButtonListener);
    setSearchData("Location", mpLocPanel);
    setDetailSearchVisible(true);

    // Define the custom buttons
    buildButtonPanel();
  }

  /**
   * Constructor with initial location
   * 
   * @param isWarehouse
   * @param isAddress
   * @param isShelfPosition
   */
  public LocationMain(String isWarehouse, String isAddress, String isShelfPosition)
  {
    this();
    mpLocPanel.reset(isWarehouse, isAddress);
  }
  /**
   * Constructor with initial location
   * 
   * @param isWarehouse
   * @param isAddress
   * @param isShelfPosition
   */
  public LocationMain(String isWarehouse, String isAddress)
  {
    this();
    mpLocPanel.reset(isWarehouse, isAddress);
  }
  
  /**
   * Overridden method so we can get our screen permissions.
   * 
   * @param ipEvent <code>InternalFrameEvent</code> that is generated when frame
   *          is opened.
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    mpBtnMaint.setAuthorization(ePerms.iModifyAllowed);
    mpBtnOrder.setAuthorization(ePerms.iModifyAllowed);
    
    /*
     * Don't list all 47 million locations when you start
     */
    String vsAddress = "";
    try
    {
      vsAddress = mpLocPanel.getAddressString();
    }
    catch (DBException e)
    {
      logAndDisplayException(e);
      return;
    }
    if (vsAddress.trim().length() > 0)
    {
      searchButtonPressed();
    }
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#cleanUpOnClose()
   */
  @Override
  public void cleanUpOnClose()
  {
    mpLocServer.cleanUp();
    mpLocServer = null;
  }
  
  
  /*========================================================================*/
  /* Action methods go here                                                 */
  /*========================================================================*/
  
  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#searchButtonPressed()
   */
  @Override
  protected void searchButtonPressed()
  {
    String vsWarehouse = mpLocPanel.getWarehouseString();
    String vsAddress = "";
    try
    {
      vsAddress = mpLocPanel.getAddressString();
    }
    catch (DBException e)
    {
      logAndDisplayException(e);
      return;
    }
    if (mpSearchCriteria == null)
    {
      mpSearchCriteria = (LocationData)mpLocData.clone();
    }

    mpSearchCriteria.clear();
    mpSearchCriteria.setKey(LocationData.WAREHOUSE_NAME, vsWarehouse);

    if (vsAddress.length() > 0)
    {
      mpSearchCriteria.setKey(LocationData.ADDRESS_NAME, vsAddress, KeyObject.LIKE);
    }
    refreshButtonPressed();
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#detailedSearchButtonPressed()
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    LocationSearchFrame vpLocSearchFrame = Factory.create(
        LocationSearchFrame.class, mpLocServer, masWarehousesWithAll,
        masDevicesWithAll);
    addSKDCInternalFrameModal(vpLocSearchFrame, searchPanel,
        new LocationDetailSearchFrameHandler());
  }

  /**
   * Handles Refresh button.
   */
  @Override
  protected void refreshButtonPressed()
  {
    if (mpSearchCriteria != null)
    {
      // Make sure we always have a default sort
      mpSearchCriteria.clearOrderByColumns();
      mpSearchCriteria.addOrderByColumn(LocationData.WAREHOUSE_NAME);
      mpSearchCriteria.addOrderByColumn(LocationData.ADDRESS_NAME);

      startSearch(mpLocation, mpSearchCriteria);
    }
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCListFrame#addButtonPressed()
   */
  @Override
  protected void addButtonPressed()
  {
    AddLocationFrame vpLocAddFrame = Factory.create(AddLocationFrame.class,
        mpLocServer, masWarehouses, masDevices);
    addSKDCInternalFrameModal(vpLocAddFrame, buttonPanel,
        new LocationAddFrameHandler());
  }

  /**
   * This method is called after the modify Location button is pressed to fill
   * in the LocationData structure based on the current selected row on the
   * Location table display.
   */
  @Override
  protected void modifyButtonPressed()
  {
    if (!isSelectionValidForModify(false))
    {
      return;
    }

    mpLocData.dataToSKDCData(sktable.getSelectedRowData());
    if (mpLocData.getLocationType() == DBConstants.LCSTATION)
    {
      if (!SKDCUserData.isSuperUser())  //only super user can modify station locations
      {
        displayInfoAutoTimeOut("Station Locations may not be Modified", "Insufficient User Credentials");
        return;
      }
    }
    
    ModifyLocationFrame vpLocModifyFrame = Factory.create(
        ModifyLocationFrame.class, mpLocServer, masDevices);
    vpLocModifyFrame.setCurrentData(mpLocData);

    /*
     * Add the frame and put the buttonPanel on the toggle on-off list for
     * SKDCInternalFrame. When the frame first comes up, the buttonPanel is
     * disabled. When there is a internal frame close event for the Modify frame
     * (handled inside SKDCInternalFrame) toggle the button buttonPanel to
     * enabled.
     */
    addSKDCInternalFrameModal(vpLocModifyFrame, buttonPanel,
        new LocationModifyFrameHandler());
  }

  /**
   * Handles Delete button for Location.
   */
  @Override
  protected void deleteButtonPressed()
  {
    if (!isSelectionValidForDelete(true))
    {
      return;
    }
    
    int vnTotalSelected = sktable.getSelectedRowCount();

    boolean vzDeleteLocn = false;
    vzDeleteLocn = displayYesNoPrompt(
        "Do you really want to Delete\nall selected Locations",
        "Delete Confirmation");
    if (vzDeleteLocn)
    {
      // Convert all selected rows to lcdata list
      List<LocationData> vpDelList = DBHelper.convertData(
                         sktable.getSelectedRowDataArray(), LocationData.class);

      int vnDelCount = 0;
      int[] vanDeleteIndices = sktable.getSelectedRows();

      for (int row = 0; row < vnTotalSelected; row++)
      {
        try
        {
          LocationData vpLocData = vpDelList.get(row);
          if (mpStnServer.exists(vpLocData.getAddress()))
          {
            displayError("Can NOT delete a Station-Location ("
                + vpLocData.getAddress()
                + ").\n Use Station screen to delete a station.");
            sktable.deselectRow(vanDeleteIndices[row]);
          }
          else if (mpDedServer.isLocationDedicated(vpLocData.getWarehouse(),
              vpLocData.getAddress()))
          {
            displayError("Can NOT delete a Dedicated-Location ("
                + vpLocData.getAddress()
                + ").\n Use Dedications screen to remove all dedicated items first.");
            sktable.deselectRow(vanDeleteIndices[row]);
          }
          else
          {
            mpLocServer.deleteLocation(vpLocData.getWarehouse(),
                vpLocData.getAddress(), vpLocData.getShelfPosition());
            vnDelCount++;
          }
        }
        catch(DBException exc)
        {
          logAndDisplayException(exc);
                                       // De-Select the troubling row!
          sktable.deselectRow(vanDeleteIndices[row]);
        }
      }
      if (vnDelCount != vnTotalSelected)
      {
        displayInfo("Deleted " + vnDelCount + " of " + vnTotalSelected
            + " selected rows", "Delete Result");
      }
      else
      {
        displayInfoAutoTimeOut("Deleted " + vnDelCount + " of "
            + vnTotalSelected + " selected rows", "Delete Result");
      }
      sktable.deleteSelectedRows();    // Update the display.
    }
  }

  /**
   *  Handles Load Display button.
   */
  protected void displayLocnDetail()
  {
    int selectedRowIndex = sktable.getSelectedRow();
    if (selectedRowIndex == -1)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    mpLocData.dataToSKDCData(sktable.getRowData(selectedRowIndex));
    try
    {
      LoadListFrame vpViewLoad = Factory.create(LoadListFrame.class,
          SKDCConstants.ALL_INT, "", "", mpLocData.getWarehouse(), 
          mpLocData.getAddress(), "", "");
      vpViewLoad.setAllowDuplicateScreens(true);
      addSKDCInternalFrameModal(vpViewLoad);
    }
    catch (Exception e)
    {
      logAndDisplayException(e);
    }
  }

  /**
   *  Handles Maintenance button.
   */
  protected void maintButtonPressed()
  {
    LocationMaintFrame vpLocMaintFrame = Factory.create(
        LocationMaintFrame.class, mpLocServer, masWarehouses, masDevices,
        mpLocPanel.getWarehouseString());
    addSKDCInternalFrameModal(vpLocMaintFrame, buttonPanel,
        new LocationMaintFrameHandler());
  }

  /**
   *  Handles Location Order button.
   */
  protected void orderButtonPressed()
  {
    LocationOrderFrame vpLocOrderFrame = Factory.create(
        LocationOrderFrame.class, mpLocServer, masWarehouses,
        mpLocPanel.getWarehouseString());
    addSKDCInternalFrameModal(vpLocOrderFrame, buttonPanel,
        new LocationMaintFrameHandler());
  }

  
  /*========================================================================*/
  /* All other methods go here                                              */
  /*========================================================================*/
  
  /**
   * Mouse listener for the table 
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
      @Override
      public SKDCPopupMenu definePopup()
      {
        mpPopupMenu.add("Add", ADD_BTN, getDefaultListener());
        mpPopupMenu.add("Modify", MODIFY_BTN, getDefaultListener());
        mpPopupMenu.add("Delete", true, DELETE_BTN, getDefaultListener());
        mpPopupMenu.add("Show Details", SHOWDETAIL_BTN, mpLocButtonListener);
        mpPopupMenu.add("Maintenance", MAINT_BTN, mpLocButtonListener);
        mpPopupMenu.add("Location Order", ORDER_BTN, mpLocButtonListener);
        
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        if (ePerms != null)
        {
          mpPopupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          mpPopupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
          mpPopupMenu.setAuthorization("Maintenance", ePerms.iModifyAllowed);
          mpPopupMenu.setAuthorization("Location Order", ePerms.iModifyAllowed);
        }
        
        return(mpPopupMenu);
      }
      
      @Override
      public void displayDetail()
      {
        displayLocnDetail();
      }
    });
  }
  
  /**
   * Add custom buttons
   */
  protected void buildButtonPanel()
  {
    mpBtnLocnDet = new SKDCButton("Detail", "Show All Loads At This Location", 'l');
    mpBtnMaint   = new SKDCButton("Maintenance", "Location Maintenance", 'i');
    mpBtnOrder   = new SKDCButton("Location Order ", "Set Load Storage Location Ordering", 'o');

    mpBtnLocnDet.addEvent(SHOWDETAIL_BTN, mpLocButtonListener);
    mpBtnMaint.addEvent(MAINT_BTN, mpLocButtonListener);
    mpBtnOrder.addEvent(ORDER_BTN, mpLocButtonListener);
    
    buttonPanel.add(mpBtnLocnDet);
    if (SKDCUserData.isSuperUser())
    {
      buttonPanel.add(mpBtnMaint);
    }
    if (SKDCUserData.isSuperUser() || SKDCUserData.isAdministrator())
    {
      buttonPanel.add(mpBtnOrder);
    }
  }

  /**
   * Fills in lists so that they may be passed around to various dialogs to
   * minimize database hits.
   */
  private void fillLists()
  {
    try
    {
      masWarehouses = mpLocServer.getRegularWarehouseChoices(SKDCConstants.NO_PREPENDER);
      masWarehousesWithAll = mpLocServer.getRegularWarehouseChoices(SKDCConstants.ALL_STRING);
      masDevices = mpLocServer.getDeviceIDList(false);
      masDevicesWithAll = mpLocServer.getDeviceIDList(true);
    }
    catch (DBException e)
    {
      logAndDisplayException(e);
    }
  }

  
  /*========================================================================*/
  /* All Listener classes go here                                           */
  /*========================================================================*/
  
  /**
   *   Property Change event listener for Add frame.
   */
  private class LocationAddFrameHandler implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        mpSearchCriteria = (LocationData)pcevt.getNewValue();
        mpLocPanel.reset(mpSearchCriteria.getWarehouse());
        mpSearchCriteria.setKey(LocationData.WAREHOUSE_NAME,
            mpSearchCriteria.getWarehouse());
        mpSearchCriteria.setBetweenKey(LocationData.ADDRESS_NAME,
            mpSearchCriteria.getAddress(), mpSearchCriteria.getEndingAddress());
        mpSearchCriteria.addOrderByColumn(LocationData.WAREHOUSE_NAME);
        mpSearchCriteria.addOrderByColumn(LocationData.ADDRESS_NAME);
        refreshButtonPressed();
      }
    }
  }

  /**
   * Property Change event listener for Maint frame.
   */
  private class LocationMaintFrameHandler implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        refreshButtonPressed();
      }
    }
  }

  /**
   * Property Change event listener for Modify frame.
   */
  private class LocationModifyFrameHandler implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        mpLocData = (LocationData)pcevt.getNewValue();
        sktable.modifySelectedRow(mpLocData);
        refreshButtonPressed();
      }
    }
  }

  /**
   * Property Change event listener for Search frame.
   */
  private class LocationDetailSearchFrameHandler implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        mpSearchCriteria = (LocationData)pcevt.getNewValue();
        refreshButtonPressed();
      }
    }
  }

  /**
   * Button Listener class.
   */
  private class LCButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(SHOWDETAIL_BTN))
      {
        displayLocnDetail();
      }
      else if (which_button.equals(MAINT_BTN))
      {
        maintButtonPressed();
      }
      else if (which_button.equals(ORDER_BTN))
      {
        orderButtonPressed();
      }
    }
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
    return LocationMain.class;
  }
}
