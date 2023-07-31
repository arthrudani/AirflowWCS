package com.daifukuamerica.wrxj.swingui.dedication;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDedicationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocation;
import com.daifukuamerica.wrxj.dbadapter.data.DedicatedLocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    This is a GUI for managing item dedications (dedicated locations,
 *    dedicated warehouses, and dedicated recommendations from Data Dr.).
 *
 * @author       mandrus<BR>
 * @version      1.0
 * <BR>Created: Feb 17, 2005<BR>
 *     Copyright (c) 2005<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class DedicationMain extends SKDCListFrame
{
  private static final String REPLEN_BTN = "REPLENISH";
  
  private DedicationSearchFrame searchFrame;
  
  private LocationPanel panelDedLC;
  private SKDCButton btnReplenish;
  private DedButtonListener dedButtonListener; 

  private String[] sWarehouseList, sAllWarehouseList;
  private StandardLocationServer locServer;
  private StandardDedicationServer dedServer;
  
  private DedicatedLocationData mpDLData;
  private DedicatedLocationData mpDLSearchCriteria;
  private DedicatedLocation mpDL;
  
  /**
   * Default constructor
   */
  public DedicationMain() throws Exception
  {
    this(false);
  }

  /**
   * 
   * @param izDebugMode
   * @throws Exception
   */
  public DedicationMain(boolean izDebugMode) throws Exception
  {
    super("DedicatedLocation");

    mpDLData = Factory.create(DedicatedLocationData.class);
    locServer = Factory.create(StandardLocationServer.class);
    dedServer = Factory.create(StandardDedicationServer.class);
    
    sWarehouseList = locServer.getRegularWarehouseChoices(SKDCConstants.NO_PREPENDER);
    sAllWarehouseList = locServer.getRegularWarehouseChoices(SKDCConstants.ALL_STRING);
    
    // Build the GUI
    dedButtonListener = new DedButtonListener();
    addExtraButtons();
    
    panelDedLC = Factory.create(LocationPanel.class);
    panelDedLC.setWarehouseList(sAllWarehouseList);
    setSearchData("Location", panelDedLC);
    setDetailSearchVisible(true);
    setDisplaySearchCount(true, "Dedication");
  }

  /**
   * Overridden method so we can get our screen permissions.
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    searchButtonPressed();
  }

  @Override
  public void cleanUpOnClose()
  {
    locServer.cleanUp();
    locServer = null;
    
    dedServer.cleanUp();
    dedServer = null;
  }

  /*========================================================================*/
  /*  GUI controls builders and handlers                                    */
  /*========================================================================*/
  /**
   *  Defines all buttons on the main Location Panels, and adds listeners
   *  to them.
   */
  private void addExtraButtons()
  {
    btnReplenish = new SKDCButton("  Replenish  ", "Replenish Location", 'R');
    btnReplenish.addEvent(REPLEN_BTN, dedButtonListener);
    buttonPanel.add(btnReplenish);
  }

  /*========================================================================*/
  /*  Action methods for Dedications tab                                    */
  /*========================================================================*/
  /**
   * Search for dedicated locations based upon new critera
   */
  @Override
  protected void searchButtonPressed()
  {
    // Gather the search criteria 
    String sWarehouse = panelDedLC.getWarehouseString();
    String sAddress = "";
    try
    {
      sAddress = panelDedLC.getAddressString();
    }
    catch(DBException e)
    {
      return;
    }
//    String sItem = dedTxtItem.getText();
    
    // If searchCriteria is null, instantiate it
    if (mpDLSearchCriteria == null)
    {
      mpDLSearchCriteria = (DedicatedLocationData)mpDLData.clone();
    }

    // Populate searchCriteria
    mpDLSearchCriteria.clear();

//    if (sItem.length() > 0)
//    {
//      mpDLSearchCriteria.setKey(mpDLData.getItemName(), sItem, KeyObject.LIKE);
//    }

    mpDLSearchCriteria.setKey(DedicatedLocationData.WAREHOUSE_NAME, sWarehouse);
    
    if (sAddress.length() > 0)
    {
      mpDLSearchCriteria.setKey(DedicatedLocationData.ADDRESS_NAME, sAddress, KeyObject.LIKE);
    }

    // Rebuild the list
    refreshButtonPressed();
  }

  /**
   * Open detailed Search Pop-up
   */
  @Override
  protected void detailedSearchButtonPressed()
  {
    searchFrame = Factory.create(DedicationSearchFrame.class, dedServer, sAllWarehouseList);
    addSKDCInternalFrameModal(searchFrame, searchPanel, new DedicationDetailSearchFrameHandler());
  }

  /**
   *  Handles Refresh button.
   */
  @Override
  protected void refreshButtonPressed()
  {
    if (mpDLSearchCriteria != null)
    {
      try
      {
        refreshTable(dedServer.getDedications(mpDLSearchCriteria));
      }
      catch (DBException exc)
      {
        exc.printStackTrace(System.out);
        displayError(exc.getMessage(), "DB Error");
      }
    }
  }

  /**
   * Add a new Dedication 
   */
  @Override
  protected void addButtonPressed()
  {
    DedicationUpdateFrame addFrame = Factory.create(DedicationUpdateFrame.class, dedServer, sWarehouseList);
    addSKDCInternalFrameModal(addFrame, buttonPanel, new DedicationUpdateFrameHandler());
  }

  /**
   *  This method is called after the modify Location button is pressed to
   *  fill in the lcdata structure based on the current selected row on the
   *  Warehouse table display.
   */
  @Override
  protected void modifyButtonPressed()
  {
    int[] selection = sktable.getSelectedRows();
    if (selection.length == 0)
    {
      displayInfoAutoTimeOut("No row selected to Modify", "Selection Error");
      return;
    }
    else if (selection.length > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to Modify at a time", "Selection Error");
      return;
    }

    mpDLData.dataToSKDCData(sktable.getSelectedRowData());

    try
    {
      mpDL = new DedicatedLocation();
      mpDLData.setCurrentQuantity(mpDL.getCurrentQuantity(mpDLData));
    }
    catch (DBException e)
    {
      displayInfoAutoTimeOut("AAIIIIEEEEE", "Selection Error");
      return;
    }
    
    DedicationUpdateFrame modifyFrame = 
      Factory.create(DedicationUpdateFrame.class, dedServer, sWarehouseList, mpDLData);
    
    /*
     * Add the frame and put the btnpanel on the toggle on-off list for
     * SKDCInternalFrame. When the frame first comes up, the btnpanel is 
     * disabled.  When there is a internal frame close event for the Modify 
     * frame (handled inside SKDCInternalFrame) toggle the button banel to 
     * enabled.
     */
    addSKDCInternalFrameModal(modifyFrame, buttonPanel, new DedicationUpdateFrameHandler());
  }

  /**
   *  Handles Delete button for Dedications.
   */
  @Override
  protected void deleteButtonPressed()
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Delete", "Selection Error");
      return;
    }

    boolean deleteDed = false;
    deleteDed = displayYesNoPrompt("Do you really want to Delete\nall selected Dedications",
        "Delete Confirmation");
    if (deleteDed)
    {
      boolean bUnreplenish = false;
      /*
       * Unreplenishments have not been implemented
       */
//      bUnreplenish = displayYesNoPrompt("Do you want to immediately\ndeplete these Dedications",
//          "Delete Confirmation");
      
      List<DedicatedLocationData> delList = null;
                            // Convert all selected rows to dldata list
      delList = DBHelper.convertData(sktable.getSelectedRowDataArray(), 
                                     DedicatedLocationData.class);

      int delCount = 0;
      int[] deleteIndices = sktable.getSelectedRows();

      for(int row = 0; row < totalSelected; row++)
      {
        try
        {
          DedicatedLocationData theData = delList.get(row);
          dedServer.deleteDedication(theData, bUnreplenish);
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
      refreshButtonPressed();
      //skDedTable.deleteSelectedRows();    // Update the display.
    }
  }

  /**
   *  Handles Replenish button for Dedications.
   */
  void replenButtonPressed()
  {
    int vnTotalSelected;
    if ((vnTotalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to Replenish", "Selection Error");
      return;
    }

    boolean vzReplenDed = false;
    vzReplenDed = displayYesNoPrompt("Do you really want to Replenish\nall selected Dedications",
        "Replenish Confirmation");
    if (vzReplenDed)
    {
      List<DedicatedLocationData> delList = null;
                             // Convert all selected rows to dldata list
      delList = DBHelper.convertData(sktable.getSelectedRowDataArray(),
                                     DedicatedLocationData.class);

      int vnReplenCount = 0;
      int[] vanReplenIndices = sktable.getSelectedRows();

      for (int row = 0; row < vnTotalSelected; row++)
      {
        try
        {
          DedicatedLocationData theData = delList.get(row);
          dedServer.replenishDedication(theData);
          vnReplenCount++;
        }
        catch(DBException exc)
        {
          displayError(exc.getMessage(), "Replenish Error");
                                       // De-Select the troubling row!
          sktable.deselectRow(vanReplenIndices[row]);
        }
      }
      if (vnReplenCount != vnTotalSelected)
      {
        displayInfo("Replenishment request sent for " +  vnReplenCount + " of " + 
            vnTotalSelected + " selected rows", "Replenishment Result");
      }
      else
      {
        displayInfoAutoTimeOut("Replenishment request sent for " +  vnReplenCount + 
            " of " + vnTotalSelected + " selected rows", "Replenishment Result");
      }
      refreshButtonPressed();
    }
  }


  /*========================================================================*/
  /*  Listener Classes                                                      */
  /*========================================================================*/

  /**
   * Default mouse listener.  Should be overridden in most cases. 
   */
  @Override
  protected void setTableMouseListener()
  {
    sktable.addMouseListener(new DacTableMouseListener(sktable)
    {
      /**
       *  Defines popup menu items for <code>SKDCTable</code>, and adds 
       *  listeners to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {
        popupMenu.add("Add"   ,       ADD_BTN   , getDefaultListener());
        popupMenu.add("Modify",       MODIFY_BTN, getDefaultListener());
        popupMenu.add("Delete", true, DELETE_BTN, getDefaultListener());
        popupMenu.add("Replenish",    REPLEN_BTN, dedButtonListener);
        
        if (ePerms == null)
        {
          ePerms = getPermissions();
        }
        if (ePerms != null)
        {
          popupMenu.setAuthorization("Add"   , ePerms.iAddAllowed);
          popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
        }

        return(popupMenu);
      }

      /**
       *  Display the Order Line screen.
       */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
    });
  }

  /**
   *  Button Listener class - Dedications tab.
   */
  private class DedButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(REPLEN_BTN))
      {
        replenButtonPressed();
      }
    }
  }

  /**
   *   Property Change event listener for Search frame.
   */
  private class DedicationDetailSearchFrameHandler implements PropertyChangeListener
  {
    @Override
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        sktable.refreshData((List)pcevt.getNewValue());
        mpDLSearchCriteria = searchFrame.getSearchCriteria();
      }
    }
  }
 
  /**
   *   Property Change event listener for Update frame.
   */
  private class DedicationUpdateFrameHandler implements PropertyChangeListener
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
    return DedicationMain.class;
  }
}
