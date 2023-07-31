package com.daifukuamerica.wrxj.swingui.warehouse; 

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacMapViewer;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    Primary frame for Warehouse operations.
 *
 * @author       A.D.    04/03/02
 * @version      1.0
 */
@SuppressWarnings("serial")
public class WarehouseMain extends SKDCInternalFrame
       implements SKDCGUIConstants
{
  private String             LINKSUPER_BTN = "SUPERWARHSE_LINK";
  private String             UNLINKSUPER_BTN = "SUPERWARHSE_UNLINK";
  private String[]     swhs_list;
  private String[]     cwhs_list;
  private JPanel             ipanel;
  private JPanel             btnpanel;
  private List          alist       = new ArrayList();
  private WarehouseAdd       addFrame;
  private WarehouseModify    modifyFrame;
  private LinkSuperWarehouse linkFrame;
  private SKDCComboBox      swhs_combo;
  private SKDCComboBox      whs_combo;
  private SKDCButton        btnLink;
  private SKDCButton        btnAdd;
  private SKDCButton        btnModify;
  private SKDCButton        btnUnlink;
  private SKDCButton        btnDelete;
  private SKDCButton        btnRefresh;
  private SKDCButton        btnSearch;
  private DacTable         sktable;
  private SKDCScreenPermissions ePerms;
  private SKDCUserData      userData;
  private SKDCPopupMenu     popupMenu    = new SKDCPopupMenu();
  private WarehouseData     wtdata       = Factory.create(WarehouseData.class);
  private StandardLocationServer    loc_server;

  public WarehouseMain() throws Exception
  {
    super("Warehouses - Warehouse Search");

    userData = new SKDCUserData();

    loc_server = Factory.create(StandardLocationServer.class);
                                       // Get list of super warehouses.
    swhs_list = loc_server.getSuperWarehouseChoices(true);
                                       // Get list of warehouses.
    cwhs_list = loc_server.getWarehouseChoices(true);

    sktable = new DacTable(new DacModel(alist, "Warehouse"));
    sktable.allowOneRowSelection(false);
    defineButtons();
                                       // Get content pane of this Internal
                                       // Frame, and add the panels, and table
    Container cp = getContentPane();
    cp.add(buildInputPanel(), BorderLayout.NORTH);
    cp.add(sktable.getScrollPane(), BorderLayout.CENTER);
    cp.add(buildButtonPanel(), BorderLayout.SOUTH);
  }

 /**
  *  Overridden method so we can get our screen permissions.
  *
  *  @param ipEvent <code>InternalFrameEvent</code> that is generated when
  *         frame is opened.
  *
  */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    setTableMouseListener();

    if (ePerms == null)
    {
      ePerms = userData.getOptionPermissions(getCategory(), getOption());
    }
    if (ePerms != null)
    {
      btnAdd.setAuthorization(ePerms.iAddAllowed);
      btnModify.setAuthorization(ePerms.iModifyAllowed);
      btnDelete.setAuthorization(ePerms.iDeleteAllowed);
      btnUnlink.setAuthorization(ePerms.iModifyAllowed);
      btnLink.setAuthorization(ePerms.iModifyAllowed);
    }
    searchButtonPressed();
  }

  @Override
  public Dimension getPreferredSize()
  {                                    // Search frame Width and Height.
    return(new Dimension(800, 375));
  }

  @Override
  public void cleanUpOnClose()
  {
    loc_server.cleanUp();
    loc_server = null;
  }

/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  void searchButtonPressed()
  {
    wtdata.clear();

    if (swhs_combo != null && swhs_combo.getItemCount() != 0)
    {
      Object selectedItem = swhs_combo.getSelectedItem();
      if (selectedItem.toString().trim().equals(SKDCConstants.ALL_STRING) == false)
      {
        wtdata.setSuperWarehouse(selectedItem.toString());
        wtdata.setKey(WarehouseData.SUPERWAREHOUSE_NAME, selectedItem,
                      KeyObject.EQUALITY);
      }
    }

    Object selectedItem = whs_combo.getSelectedItem();
    if (selectedItem.toString().trim().equals(SKDCConstants.ALL_STRING) == false)
    {
      wtdata.setWarehouse(selectedItem.toString());
      wtdata.setKey(WarehouseData.WAREHOUSE_NAME, selectedItem,
                    KeyObject.EQUALITY, KeyObject.OR);
    }
    wtdata.addOrderByColumn(WarehouseData.SUPERWAREHOUSE_NAME);
    wtdata.addOrderByColumn(WarehouseData.WAREHOUSE_NAME);

    // Get data from the warehouse server.
    refreshData();
  }

  void linkSuperButtonPressed()
  {
    if (checkOneSelection("link") == 0) return;

    wtdata.dataToSKDCData(sktable.getSelectedRowData());
    linkFrame = Factory.create(LinkSuperWarehouse.class, loc_server, wtdata);
    addSKDCInternalFrameModal(linkFrame, new JPanel[] { ipanel, btnpanel },
                               new WarehouseLinkFrameHandler());
  }

  /**
   * Unlink a warehouse from its super-warehouse
   */
  void unlinkSuperButtonPressed()
  {
    if (checkOneSelection("unlink") == 0) return;
    Map tmap = sktable.getSelectedRowData();
    WarehouseData vpWHData = Factory.create(WarehouseData.class);
    vpWHData.dataToSKDCData(tmap);

    try
    {
      String confirm_mesg = "";

      confirm_mesg = loc_server.modifySuperLink(vpWHData.getSuperWarehouse(),
                                                vpWHData.getWarehouse(), "");
      displayInfo(confirm_mesg, "Confirmation");
      
      refreshData();
      
    }
    catch(Exception e)
    {
      displayError(e.getMessage(), "DB Error");
    }

    return;
  }

  void addButtonPressed()
  {
    addFrame = Factory.create(WarehouseAdd.class, loc_server);
    addSKDCInternalFrameModal(addFrame, new JPanel[] { ipanel, btnpanel },
                              new WarehouseAddFrameHandler());
  }

  /**
   *  This method is called after the modify warehouse button is pressed to
   *  fill in the wtdata structure based on the current selected row on the
   *  Warehouse table display.
   */
  void modifyButtonPressed()
  {
    if (checkOneSelection("modify") == 0) return;

    wtdata.dataToSKDCData(sktable.getSelectedRowData());
    modifyFrame = Factory.create(WarehouseModify.class, loc_server, wtdata);
    addSKDCInternalFrameModal(modifyFrame, new JPanel[] { ipanel, btnpanel },
                              new WarehouseModifyFrameHandler());

    return;
  }

  /**
   *  Handles Delete button for Regular warehouse.
   */
  void deleteButtonPressed()
  {
    int vnselectedRows = sktable.getSelectedRowCount();
    if(vnselectedRows == 0)
    {
      displayInfoAutoTimeOut("No rows selected to Delete", "Selection Error");
      return;
    }
    boolean vzdeleteWarehouse = false;
    vzdeleteWarehouse = displayYesNoPrompt("Do you really want to Delete\nall selected Warehouses",
        "Delete Confirmation");
    if(vzdeleteWarehouse)
    {
      List<WarehouseData> delList = null;
      delList = DBHelper.convertData(sktable.getSelectedRowDataArray(),
                                     WarehouseData.class);
      int vndelCount = 0;
      int[] vandeleteIndices = sktable.getSelectedRows();
      for(int vnrow = 0; vnrow < vnselectedRows; vnrow++)
      {
        try
        {
          WarehouseData vpData = delList.get(vnrow);
          String vsmsg = loc_server.deleteWarehouse(vpData);
          displayInfoAutoTimeOut(vsmsg);
          vndelCount++;
        }
        catch(DBException e)
        {
          displayError(e.getMessage(), "Search Information");
          sktable.deselectRow(vandeleteIndices[vnrow]);
        }
      }
      displayInfo("Deleted " + vndelCount + " of " + vnselectedRows +
            " selected rows", "Delete Result");
      sktable.deleteSelectedRows();
    }
  }
    

  @Override
  protected void clearButtonPressed()
  {
    try
    {
      cwhs_list = loc_server.getWarehouseChoices(true);
      whs_combo.setComboBoxData(cwhs_list);
      swhs_list = loc_server.getSuperWarehouseChoices(true);
      swhs_combo.setComboBoxData(swhs_list);
    }
    catch(Exception e)
    {
      displayError(e.getMessage(), "DB Error");
    }

    if (swhs_combo != null)
    {
      swhs_combo.setSelectedIndex(0);
    }
    whs_combo.setSelectedIndex(0);
    sktable.clearTable();

    return;
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/
  private JPanel buildInputPanel()
  {
    ipanel = getEmptyButtonPanel();

    JPanel super_combo_panel = superComboPanel();
    ipanel.add(super_combo_panel);
    ipanel.add(warhseComboPanel());

    btnSearch.addEvent(SEARCH_BTN, new WHSButtonListener());
    btnRefresh.addEvent(REFRESH_BTN, new WHSButtonListener());

    ipanel.add(btnSearch);
    ipanel.add(btnRefresh);

    return(ipanel);
  }

  private JPanel buildButtonPanel()
  {
    btnpanel = getEmptyButtonPanel();

    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpSouthPanel.add(btnpanel, BorderLayout.SOUTH);

    btnpanel.add(btnAdd);              // Disable these buttons initially.
    btnpanel.add(btnModify);           // Add the buttons to the panel
    btnpanel.add(btnDelete);
    btnpanel.add(btnLink);
    btnpanel.add(btnUnlink);

    return(vpSouthPanel);
  }

  private void defineButtons()
  {
    btnSearch = new SKDCButton(" Search ", "Search Warehouse.", 'S');
    btnLink   = new SKDCButton(" Link Super ",
                                "Associate Super Warehouse.", 'L');
    btnUnlink = new SKDCButton("Unlink Super", "Delete Super Warehouse.",
                                'U');
    btnAdd    = new SKDCButton("    Add     ", "Add Warehouse.", 'A');
    btnModify = new SKDCButton("   Modify   ", "Modify Warehouse.", 'M');
    btnDelete = new SKDCButton("   Delete   ", "Delete Warehouse.", 'D');
    btnRefresh= new SKDCButton("  Refresh   ", "Refresh List.", 'R');

                                       // Attach listeners.
    btnLink.addEvent(LINKSUPER_BTN, new WHSButtonListener());
    btnUnlink.addEvent(UNLINKSUPER_BTN, new WHSButtonListener());
    btnAdd.addEvent(ADD_BTN, new WHSButtonListener());
    btnModify.addEvent(MODIFY_BTN, new WHSButtonListener());
    btnDelete.addEvent(DELETE_BTN, new WHSButtonListener());
    btnRefresh.addEvent(REFRESH_BTN, new WHSButtonListener());
  }

  private JPanel superComboPanel()
  {
    JPanel super_combo_panel = null;

    if (swhs_list != null)
    {
      swhs_combo = new SKDCComboBox(swhs_list);

      super_combo_panel = new JPanel();
      super_combo_panel.add(new SKDCLabel("Super Warehouse:"));
      super_combo_panel.add(swhs_combo);
    }

    return(super_combo_panel);
  }

  private JPanel warhseComboPanel()
  {
    JPanel warhse_combo_panel = new JPanel();

    whs_combo = new SKDCComboBox(cwhs_list);

    warhse_combo_panel.add(new SKDCLabel("Warehouse:"));
    warhse_combo_panel.add(whs_combo);

    return(warhse_combo_panel);
  }

  void refreshData()
  {
    try
    {
      alist = loc_server.getWarehouseData(wtdata);
      if (alist != null && alist.size() >= 0)
      {
        sktable.refreshData(alist);
        displayInfoAutoTimeOut(alist.size() + " warehouse" +
            (alist.size() == 1 ? "" : "s") + " found");
      }
      else
      {
        displayInfoAutoTimeOut("No data found", "Search Information");
      }
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "Search Information");
    }
  }

  private int checkOneSelection(String context)
  {
    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected to " + context, "Selection Error");
    }
    else if (totalSelected > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to " + context + " at a time", "Selection Error");
      totalSelected = 0;
    }

    return(totalSelected);
  }

  /**
   *   Property Change event listener for Super Warehouse link frame.
   */
  private class WarehouseLinkFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();

      if (prop_name.equals(FRAME_CHANGE))
      {
        String swhs = pcevt.getNewValue().toString();
//        String super_col_name = WarehouseData.SUPERWAREHOUSE_NAME;
        int rowSelect = sktable.getSelectedRow();

        wtdata.dataToSKDCData(sktable.getRowData(rowSelect));
        wtdata.setSuperWarehouse(swhs);
        sktable.modifyRow(rowSelect, wtdata);
      }
      else if (prop_name.equals(REFRESH_NOTIFY))
      {
        refreshData();                 // Refresh whole data set.
      }
    }
  }

  /**
   *   Property Change event listener for Add frame.
   */
  private class WarehouseAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();

      if (prop_name.equals(FRAME_CHANGE))
      {
        wtdata = (WarehouseData)pcevt.getNewValue();
//System.out.println("wtdata = " + wtdata);
        sktable.appendRow(wtdata);
      }
      else if (prop_name.equals(FRAME_CLOSING))
      {
        if (sktable.getRowCount() == 0)
        {
          enablePanelComponents(btnpanel, false);
        }
      }
    }
  }

  /**
   *   Property Change event listener for Modify frame.
   */
  private class WarehouseModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CLOSING))
      {
        //enablePanelComponents(btnpanel, true);
        //modifyFrame.removePropertyChangeListener(this);
      }
      else if (prop_name.equals(FRAME_CHANGE))
      {
        try
        {
          wtdata = (WarehouseData)pcevt.getNewValue();
// System.out.println("wtdata: " + wtdata);
          sktable.modifyRow(sktable.getSelectedRow(), wtdata);
        }
        catch(Exception exc)
        {
          displayError(exc.getMessage(), "Modify Information");
        }
      }
    }
  }

  private void setTableMouseListener()
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
        popupMenu.add("Add", ADD_BTN, new WHSButtonListener());
        popupMenu.add("Modify", MODIFY_BTN, new WHSButtonListener());
        popupMenu.add("Delete", true, DELETE_BTN, new WHSButtonListener());
        popupMenu.add("Link", LINKSUPER_BTN, new WHSButtonListener());
        popupMenu.add("UnLink", UNLINKSUPER_BTN, new WHSButtonListener());
//        popupMenu.add("Clear", CLEAR_BTN, new WHSButtonListener());
        
        if (ePerms == null)
        {
          ePerms = userData.getOptionPermissions(getCategory(), getOption());
        }
        if (ePerms != null)
        {
          popupMenu.setAuthorization("Link", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("UnLink", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Add", ePerms.iAddAllowed);
          popupMenu.setAuthorization("Modify", ePerms.iModifyAllowed);
          popupMenu.setAuthorization("Delete", ePerms.iDeleteAllowed);
        }
        
        return(popupMenu);
      }
     /**
      *  Display the detail screen.
      */
      @Override
      public void displayDetail()
      {
        viewButtonPressed();
      }
    });
  }

  /**
   *  Button Listener class.
   */
  private class WHSButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();

      if (which_button.equals(SEARCH_BTN))
      {
        searchButtonPressed();
      }
      else if (which_button.equals(REFRESH_BTN))
      {
        refreshData();
      }
      else if (which_button.equals(LINKSUPER_BTN))
      {
        linkSuperButtonPressed();
      }
      else if (which_button.equals(ADD_BTN))
      {
        addButtonPressed();
      }
      else if (which_button.equals(MODIFY_BTN))
      {
        modifyButtonPressed();
      }
      else if (which_button.equals(UNLINKSUPER_BTN))
      {
        unlinkSuperButtonPressed();
      }
      else if (which_button.equals(DELETE_BTN))
      {
        deleteButtonPressed();
      }
      else if (which_button.equals(CLEAR_BTN))
      {
        clearButtonPressed();
      }
    }
  }
  
  /**
   * Default viewer (for double-click)
   */
  private void viewButtonPressed()
  {
    int vnSelectedRows = sktable.getSelectedRowCount();
    if (vnSelectedRows == 0)
    {
      displayInfoAutoTimeOut("No row selected to view", "Selection Error");
      return;
    }
    if (vnSelectedRows > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to view at a time", 
          "Selection Error");
      return;
    }
    
    Map m = sktable.getSelectedRowDataArray().get(0);
    DacMapViewer vpDMV = new DacMapViewer("Display Warehouse", 
        "Warehouse Information", m, "Warehouse");
    addSKDCInternalFrameModal(vpDMV, new JPanel[] {ipanel, btnpanel });
  }
}
