/*
 * Workfile:   wrxj\com.daifukuamerica.wrxj.tool.wrxj.swingui.LoadLocDetail\LoadLocationDetailFrame.java
 * Revision:   1.0.0.0
 * Created on: Apr 7, 2004, 10:19:21 AM
 *
 *  Copyright ? 2004 SK-Daifuku Corporation  All Rights Reserved.
 * 
 *  This software is furnished under a license and may be used
 *  and copied only in accordance with the terms of such license.
 *  This software or any other copies thereof in any form, may not be
 *  provided or otherwise made available, to any other person or Company
 *  without written consent from SK-Daifuku Corporation (SKDC).
 *
 *  SKDC assumes no responsibility for the use or reliability of
 *  software which has been modified without approval.
 * 
 *  SK-Daifuku Corporation
 *  5202 Douglas Corrigan Way
 *  Salt Lake City, Utah  84116-3192
 *  (801) 359-9900
 */
package com.daifukuamerica.wrxj.swingui.loadlocdetail;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTablePanel;
import com.daifukuamerica.wrxj.swingui.itemdetail.AddLoadLineItem;
import com.daifukuamerica.wrxj.swingui.itemdetail.ModifyLoadLineItem;
import com.daifukuamerica.wrxj.swingui.itemdetail.ReasonCodeFrame;
import com.daifukuamerica.wrxj.swingui.load.UpdateLoad;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.PopupMenuEvent;

/**
 * 
 * @author jmarquez
 *
 * Class Description:
 */
@SuppressWarnings("serial")
public class LoadLocDetailFrame extends SKDCInternalFrame implements SKDCGUIConstants
{
  public static final String LOAD_SEARCH_BTN   = "LOAD_SEARCH";
  public static final String LOC_SEARCH_BTN    = "LOC_SEARCH";
    
  public static final String PREVIOUS_LOAD_BTN = "PREVIOUS_LOAD";
  public static final String NEXT_LOAD_BTN     = "NEXT_LOAD";
  public static final String PREVIOUS_LOC_BTN  = "PREVIOUS_LOC";
  public static final String NEXT_LOC_BTN      = "NEXT_LOC";
  public static final String UPDATE_BTN        = "UPDATE";
  public static final String LOAD_VIEW_MOD_BTN = "LOAD_VIEW_MOD";
  public static final String ITEM_VIEW_MOD_BTN = "ITEM_VIEW_MOD";
  public static final String ITEM_DEL_BTN      = "ITEM_DEL";
  
  
  // Search Panel Buttons
  private SKDCButton mpBtnLoadSearch   = new SKDCButton();
  private SKDCButton mpBtnLocSearch    = new SKDCButton();
  
  //private SKDCButton mpBtnSearch   = new SKDCButton();
  private SKDCButton mpBtnPrevLoad = new SKDCButton();
  private SKDCButton mpBtnNextLoad = new SKDCButton();
  private SKDCButton mpBtnPrevLoc  = new SKDCButton();
  private SKDCButton mpBtnNextLoc  = new SKDCButton();
  
  // Load Location Panel Buttons
  private SKDCButton mpBtnUpdate     = new SKDCButton("Update");
  private SKDCButton mpBtnClear      = new SKDCButton();
  private SKDCButton mpBtnLoadView   = new SKDCButton("View/Mod");
  private SKDCButton mpBtnLoadAdd    = new SKDCButton();
  private SKDCButton mpBtnLoadDelete = new SKDCButton();

  // Item Detail Panel Buttons
  private SKDCButton mpBtnItemView = new SKDCButton("View/Mod");
  private SKDCButton mpBtnItemAdd  = new SKDCButton();
  private SKDCButton mpBtnItemDel  = new SKDCButton();

  private SKDCScreenPermissions mpScreenPerms;
  private SKDCUserData mpUserData;
    
  private JPanel mpPnlSearch = new JPanel();  
  private JPanel mpPnlButton = new JPanel();
  
  private StandardLoadServer      mpLoadServer      = Factory.create(StandardLoadServer.class);
  private StandardLocationServer  mpLocationServer  = Factory.create(StandardLocationServer.class);
  private StandardInventoryServer mpInventoryServer = Factory.create(StandardInventoryServer.class);
  private LoadData        mpCurrentLoadData = Factory.create(LoadData.class);
  private LocationData    mpCurrentLocData  = Factory.create(LocationData.class);
  
  // private String[] sWarehouseList; 

  // Search panel Components
  private SKDCComboBox mpCmbLoadID = new SKDCComboBox();
  private String msLoadID;
  private LocationPanel mpPnlRackLoc;

  // Load Location panel Components
  private LocationPanel mpPnlCurrentLoc;
  private LocationPanel mpPnlNextLoc;
  private LocationPanel mpPnlFinalLoc;
  
                           //Set Location Type width to 30 to fit any size String Translation 
                           //and to widen screen so all of the Item Detail data shows on screen
                           //and you won't have to use the scroll bar to view all Item Detail Data
  private SKDCTextField    mpTxtLocType = new SKDCTextField(30); 
  private SKDCTranComboBox mpCmbLocStatus;
  private SKDCTranComboBox mpCmbLocEmptyFlag;
  private SKDCIntegerField mpTxtAisleGroup = new SKDCIntegerField(LocationData.AISLEGROUP_NAME);
  
  private SKDCComboBox     mpCmbContainerType = new SKDCComboBox();
  private SKDCComboBox     mpCmbLoadRoute = new SKDCComboBox();
  private SKDCTranComboBox mpCmbLoadMoveStatus;
  private SKDCTranComboBox mpCmbLoadAmountFull; 
  
  private SKDCIntegerField mpTxtLoadHeight   = new SKDCIntegerField(LoadData.HEIGHT_NAME);
  private SKDCIntegerField mpTxtLocHeight    = new SKDCIntegerField(LocationData.HEIGHT_NAME);
  private SKDCTextField    mpTxtLoadDeviceID = new SKDCTextField(LoadData.DEVICEID_NAME);
  private SKDCTextField    mpTxtLocDeviceID  = new SKDCTextField(LocationData.DEVICEID_NAME);
  private SKDCTextField    mpTxtLoadZone     = new SKDCTextField(LoadData.RECOMMENDEDZONE_NAME);
  private SKDCTextField    mpTxtLocZone      = new SKDCTextField(LocationData.ZONE_NAME);

  private SKDCDateField    mpTxtLoadMoveDate = new SKDCDateField(true); 
  
  private SKDCTextField    mpTxtLoadMsg = new SKDCTextField(LoadData.LOADMESSAGE_NAME);
  private SKDCTextField    mpTxtBCRData = new SKDCTextField(LoadData.BCRDATA_NAME);

  // Item Detail Panel Components
  private DacTable        mpItemDetailTable;
  private DacTablePanel   mpPnlItemDetail     = new DacTablePanel();;
  private SKDCPopupMenu    mpItemDetailPopupMenu = new SKDCPopupMenu();
  private JPanel           mpPnlItemDetailTable = new JPanel(new BorderLayout()); 
  protected LoadLineItemData mpItemSearchData = Factory.create(LoadLineItemData.class);
  protected Object[] masLoads = null;
  
  private TransactionHistoryData mpTranHistData = Factory.create(TransactionHistoryData.class);
  /*
   * glm This app may be up for awhile, so lets pre-load lists for all location addresses in each of the warehouses
   *     to speed up the previous/next buttons.
   */
  private List<String[]> mpAddressList = new ArrayList<String[]>();
    
  /**
   *  Create a Load Location frame.
   *
   */
  public LoadLocDetailFrame() throws Exception
  {
    super("");
    mpUserData = new SKDCUserData();
    mpScreenPerms = mpUserData.getOptionPermissionsByClass(getClass());
    
    defineButtons();
        
    JPanel mpPnlLoadLocData   = new JPanel();

    try
    {
      mpPnlSearch = buildSearchPanel();
      mpPnlLoadLocData = buildLoadLocPanel();
      mpPnlButton = buildLoadButtonPanel();
      buildItemDetailPanel();
    }
    catch(NoSuchFieldException err)
    {
      err.printStackTrace(System.out);
      return;
    }

    // Get content pane for this internal
    // frame, and add the panels to it.
    Container cp = getContentPane();

    JPanel vpPnlMain = new JPanel(new BorderLayout());
    JPanel vpPnlMiddle = new JPanel(new BorderLayout());
    
    vpPnlMain.add(mpPnlSearch, BorderLayout.NORTH);
    vpPnlMiddle.add(mpPnlLoadLocData, BorderLayout.NORTH);
    vpPnlMiddle.add(mpPnlButton, BorderLayout.SOUTH);
    vpPnlMain.add(vpPnlMiddle, BorderLayout.CENTER);
    vpPnlMain.add(mpPnlItemDetailTable, BorderLayout.SOUTH);

    cp.add(vpPnlMain, BorderLayout.NORTH);
    

    try
    {
      addSearchPanelActionListeners();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      addLoadLocPanelActionListeners();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      addItemDetailActionListeners();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }       
  }
  
  /**
   *  Defines all buttons on the main Carrier Panels, and adds listeners
   *  to them.
   */
  private void defineButtons()
  {
    // Search Panel Buttons
    URL vpURLPrev     = SKDCButton.class.getResource("/graphics/MoveSelectionLeft.png");
    URL vpURLNext     = SKDCButton.class.getResource("/graphics/MoveSelectionRight.png");

    ImageIcon vpPrevImg = new ImageIcon(vpURLPrev);
    vpPrevImg = new ImageIcon(vpPrevImg.getImage().getScaledInstance(15,15,Image.SCALE_SMOOTH));

    ImageIcon vpNextImg = new ImageIcon(vpURLNext);
    vpNextImg = new ImageIcon(vpNextImg.getImage().getScaledInstance(15,15,Image.SCALE_SMOOTH));

    mpBtnLoadSearch   = new SKDCButton(" Search ", "Search for Load",                       'S');
    mpBtnLocSearch    = new SKDCButton(" Search ", "Search for Location",                   'L');
    
    mpBtnPrevLoad = new SKDCButton(vpPrevImg, "Previous Load");
    mpBtnPrevLoad.setPreferredSize(new Dimension(20,20));
    mpBtnNextLoad = new SKDCButton(vpNextImg, "Next Load");
    mpBtnNextLoad.setPreferredSize(new Dimension(20,20));
    mpBtnPrevLoc  = new SKDCButton(vpPrevImg, "Previous Location");
    mpBtnPrevLoc.setPreferredSize(new Dimension(20,20));
    mpBtnNextLoc  = new SKDCButton(vpNextImg, "Next Location");
    mpBtnNextLoc.setPreferredSize(new Dimension(20,20));
    
    // Load Location Panel Buttons
    mpBtnUpdate     = new SKDCButton(" Update ",   "Update Load/Location Data",            'U');
    mpBtnClear      = new SKDCButton("  Clear ",   "Clear Data from Screen",               'C');
    mpBtnLoadView   = new SKDCButton(" View/Mod ", "View/Modify Load Detail",              'M');
    mpBtnLoadAdd    = new SKDCButton("  Add   ",   "Add New Load in this Location",        'A');
    mpBtnLoadDelete = new SKDCButton(" Delete ",   "Delete Load from Location",            'D');

    // Item Detail Panel Buttons
    mpBtnItemView   = new SKDCButton(" View/Mod ", "View/Modify Detail for Selected Item", 'V');
    mpBtnItemAdd    = new SKDCButton("  Add   ",   "Add Item to this Load",                'I');
    mpBtnItemDel    = new SKDCButton("  Del   ",   "Delete Item from this Load",           'X');
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
    if(mpScreenPerms == null)
    {
      mpScreenPerms = mpUserData.getOptionPermissions(getCategory(), getOption());
      if(mpScreenPerms == null)
      {
        return;
      }
    }
    mpBtnPrevLoad.setAuthorization(true); 
    mpBtnNextLoad.setAuthorization(true);
    mpBtnPrevLoc.setAuthorization(true);
    mpBtnNextLoc.setAuthorization(true);
    mpBtnUpdate.setAuthorization(true);

    //  Make sure SKDCTable columns take up any remaining space.
    mpItemDetailTable.resizeColumns();           
                                       
    mpPnlRackLoc.reset();
  }

  /**
   *  Method to intialize screen Components. This adds the Components to the
   *  screen and adds listeners as needed.
   *
   *  @exception NoSuchFieldException
   *  PANEL BUILDERS
   */
  private JPanel buildSearchPanel() throws NoSuchFieldException
  {
    //build search panel

    JPanel vpPnlSearch       = new JPanel(new GridBagLayout());
    JPanel vpPnlSearchBorder = new JPanel(new BorderLayout());
    
//sub-panel for load search navigation
    JPanel vpPnlLoadLoadSrch = new JPanel(new GridBagLayout());
    Border loadSrchBorder;
    GridBagConstraints vpGbcLoadSrch = new GridBagConstraints();
    vpGbcLoadSrch.insets = new Insets(2, 2, 2, 2);

    vpGbcLoadSrch.gridx = 0;
    vpGbcLoadSrch.gridy = 0;

    vpGbcLoadSrch.gridy = GridBagConstraints.RELATIVE;
    vpGbcLoadSrch.gridwidth = 1;
    vpGbcLoadSrch.anchor = GridBagConstraints.EAST;
    vpGbcLoadSrch.weightx = 0.2;
    vpGbcLoadSrch.weighty = 0.8;

    loadSrchBorder = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    vpPnlLoadLoadSrch.setBorder(loadSrchBorder);
//
    
//  sub-panel for location search navigation
    JPanel vpPnlLoadLocSrch = new JPanel(new GridBagLayout());
    Border locSrchBorder;
    GridBagConstraints vpGbcLocSrch = new GridBagConstraints();
    vpGbcLocSrch.insets = new Insets(2, 2, 2, 2);

    vpGbcLocSrch.gridx = 0;
    vpGbcLocSrch.gridy = 0;

    vpGbcLocSrch.gridy = GridBagConstraints.RELATIVE;
    vpGbcLocSrch.gridwidth = 1;
    vpGbcLocSrch.anchor = GridBagConstraints.EAST;
    vpGbcLocSrch.weightx = 0.2;
    vpGbcLocSrch.weighty = 0.8;

    locSrchBorder = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    vpPnlLoadLocSrch.setBorder(locSrchBorder);
//
    setTitle("Search");
    
    GridBagConstraints vpGbc = new GridBagConstraints();
    vpGbc.insets = new Insets(2, 2, 2, 2);

    vpGbc.gridx = 0;
    vpGbc.gridy = 0;
    
    vpGbc.gridy = GridBagConstraints.RELATIVE;
    vpGbc.gridwidth = 1;
    vpGbc.anchor = GridBagConstraints.EAST;
    vpGbc.weightx = 0.2;
    vpGbc.weighty = 0.8;
    
    vpPnlSearch.setBorder(BorderFactory.createEtchedBorder());
    
    vpPnlSearch.add(new SKDCLabel("   Load ID:"), vpGbc);    
    vpPnlSearch.add(new SKDCLabel("OR         "), vpGbc);
    vpPnlSearch.add(new SKDCLabel("   Location:"), vpGbc);    
    
    vpGbc.anchor = GridBagConstraints.CENTER; 
    
    mpCmbLoadID.setEditable(true);
    /*glm listening for the contents of the box to change could cause interesting looping when two boxes(load and location) are listening to each other
     *    Instead, trigger on the collapse of the load popup, so that we know that the operator made the change and not the program.
     */
    mpCmbLoadID.addPopupMenuListener(new javax.swing.event.PopupMenuListener()
    {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent pe)
      {
      }
      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent pe)
      {
        loadSearchButtonPressed();
      }
      @Override
      public void popupMenuCanceled(PopupMenuEvent pe)
      {
      }
    });
    
    //mpCmbLoadID.setPrototypeDisplayValue("WWWWWWWW");
    //mpCmbLoadID.setSelectedIndex(-1);
    vpGbcLoadSrch.gridx = 1;
    vpPnlLoadLoadSrch.add(mpCmbLoadID, vpGbcLoadSrch);
    
    //initialize warehouse choices
    mpPnlRackLoc = Factory.create(LocationPanel.class);
    mpPnlRackLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR, true);
    mpPnlRackLoc.setAutoSelectFormatOnReset(true);
    
    //initialize all of the available addresses in those warehouses
    String[] whseList = mpPnlRackLoc.getWarehouseList();
    for(int whse = 0; whse < whseList.length; whse++)
    {
      if((whseList[whse] != SKDCConstants.ALL_STRING) &&
         (whseList[whse] != ""))
      {
        String[] tempAddresses = mpLocationServer.getRackAddressRange(whseList[whse], "0", "ZZZZZZZZZ"); //get the entire list for this warehouse(0 to ZZZZZZZZZZ)
        Arrays.sort(tempAddresses);
        mpAddressList.add(tempAddresses);
      }
      else  //add a placeholder so that I can get by index from the array list
      {
        String[] tempStr = new String[0];
        mpAddressList.add(tempStr);
      }
    }

    vpGbcLocSrch.gridx = 1;
    vpPnlLoadLocSrch.add(mpPnlRackLoc, vpGbcLocSrch);
    mpPnlRackLoc.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e2)
      {
        locSearchButtonPressed();
      }
    });
       
    vpGbc.gridx = 1;
    vpGbc.gridy = 0;
    vpGbcLoadSrch.gridx = 0;
    vpPnlLoadLoadSrch.add(mpBtnPrevLoad, vpGbcLoadSrch);
    vpGbcLocSrch.gridx = 0;
    vpPnlLoadLocSrch.add(mpBtnPrevLoc, vpGbcLocSrch);
    vpGbcLoadSrch.gridx = 2;
    vpPnlLoadLoadSrch.add(mpBtnNextLoad, vpGbcLoadSrch);
    vpPnlSearch.add(vpPnlLoadLoadSrch, vpGbc);
    vpGbc.gridy = 2;
    vpGbcLocSrch.gridx = 2;
    vpPnlLoadLocSrch.add(mpBtnNextLoc, vpGbcLocSrch);
    vpPnlSearch.add(vpPnlLoadLocSrch, vpGbc);
    
    vpGbc.gridx = 2;
    vpGbc.gridy = 0;
    vpGbc.anchor = GridBagConstraints.WEST;
    vpPnlSearch.add(mpBtnLoadSearch, vpGbc);
    vpGbc.gridy = 2;
    vpPnlSearch.add(mpBtnLocSearch, vpGbc);
    
    vpPnlSearchBorder.add(vpPnlSearch, BorderLayout.SOUTH);
    
    return(vpPnlSearch);
  }
  
  private JPanel buildLoadLocPanel() throws NoSuchFieldException
  {
    JPanel vpPnlLoadLoc = new JPanel(new GridBagLayout());
    TitledBorder loadLocTitledBorder;
        
    GridBagConstraints vpGbc = new GridBagConstraints();
    vpGbc.insets = new Insets(2, 2, 2, 2);
    setTitle("Update Load Location Information");

    vpGbc.gridx = 0;
    vpGbc.gridy = 0;
    
    vpGbc.gridy = GridBagConstraints.RELATIVE;
    vpGbc.gridwidth = 1;
    vpGbc.anchor = GridBagConstraints.EAST;
    vpGbc.weightx = 0.2;
    vpGbc.weighty = 0.8;
 
    //set cosmetics for load location panel
    loadLocTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder
                         (Color.white,new Color(148, 145, 140)),"Load Information");
    vpPnlLoadLoc.setBorder(loadLocTitledBorder);
    
    // Load Location panel Components
    // Add Column 1 labels
    vpPnlLoadLoc.add(new SKDCLabel("Current Location:"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Next    Location:"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Final   Location:"), vpGbc);
   
    vpPnlLoadLoc.add(new SKDCLabel("Container Type:"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Route:"), vpGbc);
    
    vpPnlLoadLoc.add(new SKDCLabel("Load Move Status:"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Amount Full:"), vpGbc);

    vpPnlLoadLoc.add(new SKDCLabel("Load Height:"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Load Rec Zone:"), vpGbc);
    
    // Add Load Loc Fields in second column
    vpGbc.gridx = 1;
    vpGbc.anchor = GridBagConstraints.WEST;

    // Put in Column 1 Fields
    // Put in Load Location panels
    mpPnlCurrentLoc = Factory.create(LocationPanel.class);
    mpPnlCurrentLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    mpPnlCurrentLoc.setAutoSelectFormatOnReset(true);
    vpPnlLoadLoc.add(mpPnlCurrentLoc, vpGbc);

    mpPnlNextLoc = Factory.create(LocationPanel.class);
    mpPnlNextLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    mpPnlNextLoc.setAutoSelectFormatOnReset(true);
    vpPnlLoadLoc.add(mpPnlNextLoc, vpGbc);
    
    mpPnlFinalLoc = Factory.create(LocationPanel.class);
    mpPnlFinalLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    mpPnlFinalLoc.setAutoSelectFormatOnReset(true);
    vpPnlLoadLoc.add(mpPnlFinalLoc, vpGbc);
  
    vpPnlLoadLoc.add(mpCmbContainerType, vpGbc);
    vpPnlLoadLoc.add(mpCmbLoadRoute, vpGbc);
    
    mpCmbLoadMoveStatus = new SKDCTranComboBox(LoadData.LOADMOVESTATUS_NAME);  
    vpPnlLoadLoc.add(mpCmbLoadMoveStatus, vpGbc);
    mpCmbLoadAmountFull = new SKDCTranComboBox(LoadData.AMOUNTFULL_NAME);  
    vpPnlLoadLoc.add(mpCmbLoadAmountFull, vpGbc);
       
    vpPnlLoadLoc.add(mpTxtLoadHeight, vpGbc);
    mpTxtLoadHeight.setEnabled(false);
    vpPnlLoadLoc.add(mpTxtLoadZone, vpGbc);
    mpTxtLoadZone.setEnabled(false);
    
    // Add Column 3 labels
    vpGbc.gridx = 3;
    vpGbc.anchor = GridBagConstraints.EAST;
    
    // Put in blank lines to space down on form
    vpPnlLoadLoc.add(new SKDCLabel(""), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel(""), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Aisle Group:"),vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Location Type"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Location Device:"), vpGbc);
    
    vpPnlLoadLoc.add(new SKDCLabel("Location Status:"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Location Empty Flag:"), vpGbc);  
   
    vpPnlLoadLoc.add(new SKDCLabel("Location Height:"), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel("Location Zone:"), vpGbc);  

    
    // Add Column 4 fields
    vpGbc.gridx = 4;
    vpGbc.anchor = GridBagConstraints.WEST;
    
    // Put in blank lines to space down on form
    vpPnlLoadLoc.add(new SKDCLabel(""), vpGbc);
    vpPnlLoadLoc.add(new SKDCLabel(""), vpGbc);
    vpPnlLoadLoc.add(mpTxtAisleGroup, vpGbc);
    mpTxtAisleGroup.setEnabled(false);
    vpPnlLoadLoc.add(mpTxtLocType, vpGbc);
    mpTxtLocType.setEnabled(false);
    vpPnlLoadLoc.add(mpTxtLocDeviceID, vpGbc);
    mpTxtLocDeviceID.setEnabled(false);
    
    mpCmbLocStatus = new SKDCTranComboBox(LocationData.LOCATIONSTATUS_NAME);  
    vpPnlLoadLoc.add(mpCmbLocStatus, vpGbc);
    mpCmbLocEmptyFlag = new SKDCTranComboBox(LocationData.EMPTYFLAG_NAME);
    vpPnlLoadLoc.add(mpCmbLocEmptyFlag, vpGbc);

    vpPnlLoadLoc.add(mpTxtLocHeight, vpGbc); 
    mpTxtLocHeight.setEnabled(false);
    vpPnlLoadLoc.add(mpTxtLocZone, vpGbc);
    mpTxtLocZone.setEnabled(false);
    
    containerFill();
    routeFill("");
    loadComboFill();
    
    // Make sure it's off for the next time
    // Grid Bag object is used.
    vpGbc.fill = GridBagConstraints.NONE;
    return(vpPnlLoadLoc);
  }
  
  /**
   *  Adds Buttons to load submit panel.
   */
  private JPanel buildLoadButtonPanel()
  {
    JPanel vpPnlButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
    FlowLayout vpFlowLayout = new FlowLayout();
    
    vpPnlButton.setLayout(vpFlowLayout);
    vpPnlButton.setBorder(BorderFactory.createEtchedBorder());

    vpPnlButton.add(mpBtnUpdate);
    vpPnlButton.add(mpBtnClear);           // Add the buttons to the panel
    vpPnlButton.add(mpBtnLoadView);
    vpPnlButton.add(mpBtnLoadAdd);
    vpPnlButton.add(mpBtnLoadDelete);

    return(vpPnlButton);
  }
  
  private void buildItemDetailPanel() throws Exception
  {
    FlowLayout vpFlowLayout = new FlowLayout();
    
    JPanel vpPnlItemButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
    
    List<Map> vpLstItemDetail = new ArrayList<Map>(); 
    DacModel vpSKmodel   = null;
    BorderLayout vpItemDetailBorderLayout = new BorderLayout(); 
    TitledBorder vpItemListTitledBorder;
    
    GridBagConstraints vpGbc = new GridBagConstraints();
    vpGbc.insets = new Insets(2, 2, 2, 2);
    
    setTitle("Load Item Information");

    vpGbc.gridx = 0;
    vpGbc.gridy = 0;
    
    vpGbc.gridy = GridBagConstraints.RELATIVE;
    vpGbc.gridwidth = 1;
    vpGbc.anchor = GridBagConstraints.EAST;
    vpGbc.weightx = 0.2;
    vpGbc.weighty = 0.8;        
    vpItemListTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Item Detail Information");
    mpPnlItemDetail.setLayout(vpItemDetailBorderLayout); //has add-modify menu at bottom
    mpPnlItemDetail.setBorder(vpItemListTitledBorder);


    //add item detail table to lower panel

    vpSKmodel = new DacModel(vpLstItemDetail, "LoadLocItemDetail");
    mpItemDetailTable = new DacTable(vpSKmodel);
    defineItemDetailPopUp();
    mpPnlItemDetail.addMouseEvent(mpItemDetailTable, mpItemDetailPopupMenu);
    mpPnlItemDetail.add(mpItemDetailTable.getScrollPane(),  BorderLayout.CENTER);

    vpGbc.gridx = 0;
    
    vpGbc.gridy = GridBagConstraints.RELATIVE;
    vpGbc.gridwidth = 1;
    vpGbc.anchor = GridBagConstraints.EAST;
    vpGbc.weightx = 0.2;
    vpGbc.weighty = 0.8;
     
    //add item detail buttons to lower panel
    vpPnlItemButton.setLayout(vpFlowLayout);
    vpPnlItemButton.setBorder(BorderFactory.createEtchedBorder());
    vpPnlItemButton.setMinimumSize(new Dimension(500,45));
    mpBtnItemView.setText("View/Modify");
    vpPnlItemButton.add(mpBtnItemView, vpGbc);
    mpBtnItemAdd.setText("Add");
    vpPnlItemButton.add(mpBtnItemAdd, vpGbc);
    mpBtnItemDel.setText("Delete");
    vpPnlItemButton.add(mpBtnItemDel, vpGbc);
    
    mpPnlItemDetailTable.add(mpPnlItemDetail, BorderLayout.NORTH);
    mpPnlItemDetailTable.add(vpPnlItemButton, BorderLayout.SOUTH);
    
    mpPnlItemDetail.setPreferredSize(new Dimension(100, 200));
  }


  /**
   *  Method to populate the Load ID combo box.
   */
  void loadComboFill()
  {
    msLoadID = ""; 
      try
      {
        masLoads = mpLoadServer.getLoadIDList(SKDCConstants.EMPTY_VALUE);
        mpCmbLoadID.setComboBoxData(masLoads);
      }
      catch(Exception ve)
      {
        ve.printStackTrace();
      }
  }
  
   /**
    *  Method to populate the load panel's container type combo box.
    *
    *  @param srch Name to match.
    */
   private void containerFill()
   {
     try
     {
       List vpLstContainerType = mpInventoryServer.getContainerTypeList();
       mpCmbContainerType.setComboBoxData(vpLstContainerType);
     }
     catch (DBException dbe)
     {
       logAndDisplayException(dbe);
     }
   }
   
   /**
    *  Method to populate the load panel's route combo box.
    *
    *  @param srch Name to match.
    */
   void routeFill(String srch)
   {
     StandardRouteServer vpRouteServer = Factory.create(StandardRouteServer.class);
     
     try
     {
       List vpLstRouteIDs = vpRouteServer.getRouteNameList(srch);
       mpCmbLoadRoute.setComboBoxData(vpLstRouteIDs);
     }
     catch (DBException e)
     {
       e.printStackTrace(System.out);
       displayError("Database Error: " + e);
     }
   }

  /**
   * Search Panel Actions
   * 
   */
  private void addSearchPanelActionListeners() throws Exception
  {
    mpBtnLoadSearch.addEvent(LOAD_SEARCH_BTN, new searchEventListener());
    mpBtnLocSearch.addEvent(LOC_SEARCH_BTN, new searchEventListener());
    
    mpBtnPrevLoad.addEvent(PREVIOUS_LOAD_BTN, new searchEventListener());
    mpBtnNextLoad.addEvent(NEXT_LOAD_BTN, new searchEventListener());
    mpBtnPrevLoc.addEvent(PREVIOUS_LOC_BTN, new searchEventListener());
    mpBtnNextLoc.addEvent(NEXT_LOC_BTN, new searchEventListener());
  }

  private class searchEventListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)    
    {
      String vs_which_button = e.getActionCommand();

      if( vs_which_button.equals(LOAD_SEARCH_BTN))
      {
        loadSearchButtonPressed();
      }
      else if( vs_which_button.equals(LOC_SEARCH_BTN))
      {
        locSearchButtonPressed();
      }
      else if( vs_which_button.equals(PREVIOUS_LOAD_BTN))
      {
        previousLoadButtonPressed();
      }
      else if( vs_which_button.equals(NEXT_LOAD_BTN))
      {
        nextLoadButtonPressed();
      }
      else if( vs_which_button.equals(PREVIOUS_LOC_BTN))
      {
        previousLocButtonPressed();
      }
      else if( vs_which_button.equals(NEXT_LOC_BTN))
      {
        nextLocButtonPressed();
      }
      else if( vs_which_button.equals(CLEAR_BTN))
      {
        clearButtonPressed();
      }
    }
  }
  
  void loadSearchButtonPressed()
  {
    LoadData     vpLoadData = Factory.create(LoadData.class);
    LocationData vpLocData = Factory.create(LocationData.class);
    
    msLoadID = mpCmbLoadID.getText().trim();
    if (msLoadID.length() > 0)
    {
      try
      {
        vpLoadData = mpLoadServer.getLoad1(msLoadID);
      }
      catch(DBException e)
      {
        displayInfoAutoTimeOut(e.getMessage());
        clearButtonPressed();
        return;
      }
      
      if( vpLoadData == null )
      {
        displayInfoAutoTimeOut("Load Not Found");
        clearButtonPressed();
        vpLoadData = Factory.create(LoadData.class);
        vpLoadData.clear();              // Make sure everything is defaulted
        mpPnlRackLoc.reset();
        refreshLoadDataDisplay(vpLoadData);
        refreshItemDetailTable(msLoadID);
        return;
      }
            
      // Load found - read location record for load
      try
      {
        vpLocData = mpLocationServer.getLocationRecord(vpLoadData.getWarehouse(), vpLoadData.getAddress());
      }
      catch(DBException e)
      {
        displayError(e.getMessage(), "Search Error");
        clearButtonPressed();
      }
            
      if( vpLocData == null)
      {
        displayInfoAutoTimeOut("Load does not have a Location record");
        vpLocData = Factory.create(LocationData.class);
        vpLocData.clear();
      }
      
      refreshLoadDataDisplay(vpLoadData);
      refreshItemDetailTable(vpLoadData.getLoadID());
      refreshLocationData(vpLocData);
      mpCmbLoadID.setSelectedItem(vpLoadData.getLoadID());
    }
    else
    {
      displayInfoAutoTimeOut("Enter Load ID");
    }
  }
    
  void locSearchButtonPressed()
  {
    LoadData vpLoadData = null;
    LocationData vpLocData = null;
    
    String vsWarehouse;
    String vsAddress;

    vsWarehouse = mpPnlRackLoc.getWarehouseString();
        
    if( vsWarehouse.trim() == SKDCConstants.ALL_STRING )
    {
      displayInfoAutoTimeOut("Enter valid location");
    }
    else
    {
      if( vsWarehouse.trim().length() > 0 ) 
      {
        if( !mpLocationServer.exists(vsWarehouse) )
        {
          displayInfoAutoTimeOut("Warehouse does not exist");
          return;
        }
        try
        {
          vsAddress = mpPnlRackLoc.getAddressString();        
          if( vsAddress.trim().length() == 0 )
          {
            displayInfoAutoTimeOut("Address cannot be blank");
            return; 
          }
        }
        catch(DBException e)
        {
          displayError(e.getMessage());
          return;
        }
      
        try
        {
          vpLocData = mpLocationServer.getLocationRecord( vsWarehouse, vsAddress );
        }
        catch(DBException e)
        {
          displayError(e.getMessage());
          return;
        }
      
        if (vpLocData == null)
        {
          displayInfoAutoTimeOut("Location does not exist");
          vpLocData = Factory.create(LocationData.class);
          vpLocData.clear();  
          vpLoadData = Factory.create(LoadData.class);
          vpLoadData.clear();
          refreshLoadDataDisplay(vpLoadData);
          refreshItemDetailTable(vpLoadData.getLoadID());
          refreshLocationData(vpLocData);
          return;
        }
          
        // Location found
        // Look for loads in location      
        try
        {
          vpLoadData = mpLoadServer.getOldestLoad(vpLocData.getWarehouse(),vpLocData.getAddress());
        }
        catch(DBException e)
        {
          displayInfoAutoTimeOut(e.getMessage());
          return;
        }      
      
        if (vpLoadData == null)
        {
          vpLoadData = Factory.create(LoadData.class);
          vpLoadData.clear();              // Make sure everything is defaulted
        }
        refreshLoadDataDisplay(vpLoadData);
        refreshItemDetailTable(msLoadID);
        refreshLocationData(vpLocData);
        mpCmbLoadID.setSelectedItem(vpLoadData.getLoadID());
      }
    }
  }
  
  public void previousLoadButtonPressed()
  {
    LoadData   vpLoadData     = Factory.create(LoadData.class);

    vpLoadData.clear();              // Make sure everything is defaulted
    msLoadID = mpCmbLoadID.getText().trim();
    if( msLoadID.length() == 0 )
    {
      msLoadID = "0";  
      vpLoadData = mpLoadServer.getNextLoadData(msLoadID);//this will take us to the first of the list
    }
    else
    {
      vpLoadData = mpLoadServer.getPreviousLoadData(msLoadID);
    }
    if( vpLoadData == null)
    {
      displayInfoAutoTimeOut("Load " + msLoadID + " is the first load");
      return;
    }
    refreshLoadDataDisplay(vpLoadData);
    refreshItemDetailTable(vpLoadData.getLoadID());
    
    LocationData vpLocData = Factory.create(LocationData.class);
    try
    {
      vpLocData = mpLocationServer.getLocationRecord(vpLoadData.getWarehouse(), vpLoadData.getAddress());
    }
    catch(DBException e)
    {
      displayError(e.getMessage());
      clearButtonPressed();
    }
    if (vpLocData == null)
    {
      displayInfoAutoTimeOut("ERROR: No Location Data for Load");
      clearButtonPressed();
      return;
    }
    refreshLocationData(vpLocData);
    mpCmbLoadID.setSelectedItem(vpLoadData.getLoadID());
  }
  
  private void nextLoadButtonPressed()
  {
    LoadData vpLoadData = Factory.create(LoadData.class);

    vpLoadData.clear();              // Make sure everything is defaulted
    
    msLoadID = mpCmbLoadID.getText().trim();
    if( msLoadID.length() == 0 )
    {
      msLoadID = "ZZZZZZZZ";
      vpLoadData = mpLoadServer.getPreviousLoadData(msLoadID);//this should take us to the end of the list
    }
    else
    {
      vpLoadData = mpLoadServer.getNextLoadData(msLoadID);
    }
    if( vpLoadData == null)
    {
      displayInfoAutoTimeOut("Load " + msLoadID + " is the last load");
      return;
    }
    refreshLoadDataDisplay(vpLoadData);
    refreshItemDetailTable(vpLoadData.getLoadID()); 
    
    LocationData vpLocData = Factory.create(LocationData.class);
    try
    {
      vpLocData = mpLocationServer.getLocationRecord(vpLoadData.getWarehouse(), vpLoadData.getAddress());
    }
    catch(DBException e)
    {
      displayError(e.getMessage());
      clearButtonPressed();
    }
    if (vpLocData == null)
    {
      displayInfoAutoTimeOut("ERROR: No Location Data for Load");
      clearButtonPressed();
      return;
    }
    refreshLocationData(vpLocData);
    mpCmbLoadID.setSelectedItem(vpLoadData.getLoadID());
  }
  
  public void nextLocButtonPressed()
  {
    LocationData vpLocData  = Factory.create(LocationData.class);
    vpLocData.clear();              // Make sure everything is defaulted
    String vpWarehouse;
    String vpAddress;
    int vpThisIndex;
    
    try
    {
      vpWarehouse = mpPnlRackLoc.getWarehouseString();
      vpAddress = mpPnlRackLoc.getAddressString();
    }
    catch(DBException e)
    {
      displayError(e.getMessage());
      return;
    }

    if (vpWarehouse == SKDCConstants.ALL_STRING)
      vpWarehouse = "ASR";
    
    String[] whseList = mpPnlRackLoc.getWarehouseList();
    int whseIndex = Arrays.binarySearch(whseList, vpWarehouse);
    String[] tempAddresses = mpAddressList.get(whseIndex);
    vpThisIndex = Arrays.binarySearch(tempAddresses, vpAddress);
    if (vpThisIndex < 0) //not found
      vpThisIndex = (tempAddresses.length-1);  //not found, jump to the end of the list
    if (vpThisIndex < (tempAddresses.length-1)) //don't let "next" take us out of bounds
    {
      vpThisIndex++;
    }
    else
    {
      displayInfoAutoTimeOut("Location " + vpWarehouse + "-" +
          vpAddress +  " is the last location");
    }

    try
    {
      vpLocData = mpLocationServer.getLocationRecord(vpWarehouse, tempAddresses[vpThisIndex]);
    }
    catch(DBException e)
    {
      displayError(e.getMessage() + "Unable to get Location data");
      return;
    }

    refreshLocationData(vpLocData);    
    locSearchButtonPressed();
  }

  public void previousLocButtonPressed()
  {
    LocationData vpLocData  = Factory.create(LocationData.class);
    vpLocData.clear();              // Make sure everything is defaulted
    String vpWarehouse;
    String vpAddress;
    int vpThisIndex;
    
    try
    {
      vpWarehouse = mpPnlRackLoc.getWarehouseString();
      vpAddress = mpPnlRackLoc.getAddressString();
    }
    catch(DBException e)
    {
      displayError(e.getMessage());
      return;
    }

    if (vpWarehouse == SKDCConstants.ALL_STRING)
      vpWarehouse = "ASR";
    
    String[] whseList = mpPnlRackLoc.getWarehouseList();
    int whseIndex = Arrays.binarySearch(whseList, vpWarehouse);
    String[] tempAddresses = mpAddressList.get(whseIndex);
    vpThisIndex = Arrays.binarySearch(tempAddresses, vpAddress);
    if (vpThisIndex < 0) //not found
      vpThisIndex = 0;  //not found, jump to the end of the list
    if (vpThisIndex > 0) //don't let "next" take us out of bounds
    {
      vpThisIndex--;
    }
    else
    {
      displayInfoAutoTimeOut("Location " + vpWarehouse + "-" +
          vpAddress +  " is the first location");
    }

    try
    {
      vpLocData = mpLocationServer.getLocationRecord(vpWarehouse, tempAddresses[vpThisIndex]);
    }
    catch(DBException e)
    {
      displayError(e.getMessage() + "Unable to get Location data");
      return;
    }

    refreshLocationData(vpLocData);    
    locSearchButtonPressed();
  }

  /**
   * LoadLoc Panel Actions
   */
  private void addLoadLocPanelActionListeners() throws Exception
  {
    mpBtnUpdate.addEvent(UPDATE_BTN, new loadLocListener());
    mpBtnLoadView.addEvent(VIEW_BTN, new loadLocListener());
    mpBtnLoadAdd.addEvent(ADD_BTN, new loadLocListener());
    mpBtnLoadDelete.addEvent(DELETE_BTN, new loadLocListener());
    mpBtnClear.addEvent(CLEAR_BTN, new loadLocListener());
  }
  
  /**
   *  Button Listener class.
   */
  private class loadLocListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String vs_which_button = e.getActionCommand();

      if (vs_which_button.equals(UPDATE_BTN))
      {
        updateButtonPressed();
      }
      else if (vs_which_button.equals(VIEW_BTN))
      {
        loadModifyButtonPressed();
      }
      else if (vs_which_button.equals(ADD_BTN))
      {
        loadAddButtonPressed();
      }
      else if (vs_which_button.equals(MODIFY_BTN))
      {
        loadModifyButtonPressed();
      }
      else if (vs_which_button.equals(DELETE_BTN))
      {
        loadDeleteButtonPressed();
      }
      else if (vs_which_button.equals(CLEAR_BTN))
      {
        clearButtonPressed();
      }
    }
  }
  
  /**
   *  Action method to handle Update button. Verifies that entered data is valid,
   *  then updates database.
   *  
   *   
   *  @param e Action event.
   *
   *    
   */
  void updateButtonPressed()
  {
    LoadData vpLoadData;
    vpLoadData = Factory.create(LoadData.class);
    
    msLoadID = mpCmbLoadID.getText().trim();
    
    try
    {
        vpLoadData = mpLoadServer.getLoad1(msLoadID);
    }
    catch (DBException e2)
    {
      displayError("Unable to get Load data");
      return;
    }

    LocationData vpLocData;
    vpLocData = Factory.create(LocationData.class);
    try
    {
      vpLocData = mpLocationServer.getLocationRecord(mpPnlCurrentLoc.getWarehouseString(),
                                                     mpPnlCurrentLoc.getAddressString());
      if (vpLocData == null)
      {
        displayInfoAutoTimeOut("Location does not exist");
        return;
      }
        
      /*
       * Don't allow a load to be added or moved into an occupied ASRS location
       */
      if( ( vpLocData.getLocationType() == DBConstants.LCASRS  )  &
    	    ( vpLocData.getEmptyFlag()    != DBConstants.UNOCCUPIED ) )
      {
        if( (!vpLoadData.getWarehouse().trim().equals(vpLocData.getWarehouse().trim()) ) ||
            (!vpLoadData.getAddress().trim().equals(vpLocData.getAddress().trim())   ) ) 
        {
          displayInfoAutoTimeOut("ASRS Location is not empty");
          return;
        } 
      }
    }
    
    catch (DBException e2)
    {
      displayError("Unable to get Location data");
      return;
    }

    try
    {
      vpLoadData.setAmountFull(mpCmbLoadAmountFull.getIntegerValue());
      vpLoadData.setLoadMoveStatus(mpCmbLoadMoveStatus.getIntegerValue());
      vpLoadData.setWarehouse(mpPnlCurrentLoc.getWarehouseString());
      vpLoadData.setAddress(mpPnlCurrentLoc.getAddressString().trim());
      vpLoadData.setLoadMessage(mpTxtLoadMsg.getText());
      vpLoadData.setContainerType(mpCmbContainerType.getText());
      vpLoadData.setRouteID(mpCmbLoadRoute.getText());
      try
      {  
        String vsLocType;
        vsLocType = DBTrans.getStringValue("iLocationType", vpLocData.getLocationType());
        mpTxtLocType.setText(vsLocType);
        mpCmbLocStatus.setSelectedElement(vpLocData.getLocationStatus());
        mpCmbLocEmptyFlag.setSelectedElement(vpLocData.getEmptyFlag());
      }
      catch(NoSuchFieldException e)
      {
        displayWarning(e.getMessage(), "Translation Error");
      }

      mpLoadServer.updateLoadInfo(vpLoadData);

      displayInfoAutoTimeOut("Load " + msLoadID + " updated");
      //jan fix
      mpCurrentLoadData = vpLoadData;  //after submitting we want reset button to disp new info.

                                         // Record Load Change Transaction
      mpTranHistData.clear();
      mpTranHistData.setTranType(DBConstants.MODIFY);
      mpTranHistData.setTranCategory(DBConstants.LOAD_TRAN);
      mpTranHistData.setLoadID(vpLoadData.getLoadID());
      mpTranHistData.setLocation(vpLoadData.getWarehouse() + vpLoadData.getAddress());
      mpTranHistData.setRouteID(vpLoadData.getRouteID());
      mpInventoryServer.logTransaction(mpTranHistData);
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);

      displayError("Error updating load " + msLoadID);
    }

    catch (NoSuchFieldException e2)
    {
      displayError("No Such Field: " + e2);
    }
    refreshLoadDataDisplay(vpLoadData);
    refreshItemDetailTable(vpLoadData.getLoadID());
    refreshLocationData(vpLocData);
    mpCmbLoadID.setSelectedItem(vpLoadData.getLoadID());
  }
    
  public void loadViewButtonPressed()
  {    
    /*
     * The loadViewButton pops up the UpdateLoad frame with View Load option
     */
	  UpdateLoad vpUpdateLoad = new UpdateLoad("View Load");
	  vpUpdateLoad.setView(mpCurrentLoadData.getLoadID());
    addSKDCInternalFrameModal(vpUpdateLoad, new JPanel[] {mpPnlButton, mpPnlSearch});
  }
	
  public void loadAddButtonPressed()
  {
    /*
     * The loadAddButton pops up the UpdateLoad frame
     */
    
    UpdateLoad vpUpdateLoad = Factory.create(UpdateLoad.class, "Add Load");
    vpUpdateLoad.setAddToLocation(mpCurrentLocData.getWarehouse(), mpCurrentLocData.getAddress());
    addSKDCInternalFrameModal(vpUpdateLoad, new JPanel[] {mpPnlButton, mpPnlSearch},
    new PropertyChangeListener() 
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt) 
      {
          String vsProp = evt.getPropertyName();
          if (vsProp.equals(FRAME_CHANGE))
          {
            LoadData vpLoadData;
            vpLoadData = (LoadData)evt.getNewValue();
            refreshLoadDataDisplay(vpLoadData); 
            refreshItemDetailTable(vpLoadData.getLoadID());    
            //loadComboFill();
            mpCmbLoadID.setSelectedItem(vpLoadData.getLoadID());
          }
      }
    });
  }
  
  private void loadModifyButtonPressed()
  {
    /*
     * The loadModifyButton pops up the UpdateLoad frame
     */
    UpdateLoad vpUpdateLoad = Factory.create(UpdateLoad.class, "Modify Load");
    vpUpdateLoad.setModify(mpCurrentLoadData.getLoadID());
     
    addSKDCInternalFrameModal(vpUpdateLoad, new JPanel[] {mpPnlButton, mpPnlSearch},
    new PropertyChangeListener() 
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt) 
      {
        String vsProp = evt.getPropertyName();
        if (vsProp.equals(FRAME_CHANGE))
        {
          LoadData vpLoadData;
          vpLoadData = Factory.create(LoadData.class);
  
          vpLoadData.clear();
          vpLoadData = (LoadData)evt.getNewValue();
                          
          refreshLoadDataDisplay(vpLoadData);;
          refreshItemDetailTable(vpLoadData.getLoadID());
        }
      }
    });
  }
  
  /**
   *  Action method to handle Delete button.
   *
   */
  void loadDeleteButtonPressed()
  {
    msLoadID = mpCmbLoadID.getText().trim();
    if (displayYesNoPrompt("Do you really want to delete\n" +
                             "Load " + msLoadID, "Delete Confirmation"))
    {
      ReasonCodeFrame vpReasonCodeFrame = new ReasonCodeFrame(DBConstants.REASONADJUST);

      String[] vsChoices = vpReasonCodeFrame.getMsChoices();
      if (vsChoices == null || vsChoices.length == 0)
      {
        deleteLoad("");
        LoadData vpLoadData;
        vpLoadData = Factory.create(LoadData.class);
        vpLoadData.clear();
        loadComboFill();
        return;
      }
      else if (vsChoices.length == 1)
      {
        deleteLoad(vsChoices[0]);
        LoadData vpLoadData;
        vpLoadData = Factory.create(LoadData.class);
        vpLoadData.clear();
        loadComboFill();
        return;
      }
      
      addSKDCInternalFrameModal(vpReasonCodeFrame, new PropertyChangeListener() 
      {
        @Override
        public void propertyChange(PropertyChangeEvent event) 
        {
          String vsProp = event.getPropertyName();
          if(vsProp.equals(FRAME_CHANGE))
          {
            String vsReasonCode = (String) event.getNewValue();
            deleteLoad(vsReasonCode);
            
            LoadData vpLoadData;
            vpLoadData = Factory.create(LoadData.class);
            vpLoadData.clear();
            loadComboFill();
          }
        }
      });
    }
  }
  
  /**
   * deleteLoad -
   * 
   * Desc: Deletes the Load Displayed.
   * 
   */
  protected void deleteLoad(final String isReasonCode)
  {
    if(isReasonCode == null)
    {
      displayError("A valid reason code must be selected");
      return;
    }
 
    String vsReasonCode = isReasonCode;
    String vsLoadID = mpCurrentLoadData.getLoadID();
    if(vsLoadID.length() > 0)
    {
      try
      {
        mpInventoryServer.deleteLoad(vsLoadID, vsReasonCode);
      }
      catch(DBException exc)
      {
        displayError(exc.getMessage(), "Delete Error");
      }
    }
    else
    {
      displayError("Load for deletion is blank");
    }
  }
  
  @Override 
  protected void clearButtonPressed()
  { 
    mpPnlRackLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR, true);
    mpPnlRackLoc.reset();
    mpCmbLoadID.getEditor().setItem("");
    mpPnlCurrentLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    mpPnlCurrentLoc.reset();
    mpPnlNextLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    mpPnlNextLoc.reset();
    mpPnlCurrentLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    mpPnlFinalLoc.reset();
    mpTxtAisleGroup.setText("");
    mpTxtLocType.setText("");
    mpCmbLocStatus.getEditor().setItem("");
    mpCmbLocEmptyFlag.getEditor().setItem("");
    mpCmbContainerType.getEditor().setItem("");
    mpCmbLoadRoute.getEditor().setItem("");
    mpCmbLoadMoveStatus.getEditor().setItem("");
    mpCmbLoadAmountFull.getEditor().setItem(""); 
    mpTxtLoadHeight.setText("");
    mpTxtLocHeight.setText("");
    mpTxtLoadDeviceID.setText("");
    mpTxtLocDeviceID.setText("");
    mpTxtLoadZone.setText("");
    mpTxtLocZone.setText("");
    mpTxtLoadMoveDate.setText("");
    mpTxtLoadMsg.setText("");
    mpTxtBCRData.setText("");
    refreshItemDetailTable(msLoadID);
  }

  /**
   *  Method to refresh load panel fields.  Allow some fields to be edited
   *  when adding a new load but not when modifying existing loads
   *
   *  @param loadData Load data to use in refreshing load data.
   *
   */
  void refreshLoadDataDisplay(LoadData loadData)
  {    
    mpCurrentLoadData = loadData.clone();
    
    msLoadID = mpCmbLoadID.getText().trim();
    msLoadID = loadData.getLoadID();
    mpCmbLoadID.getEditor().setItem(loadData.getLoadID());
    
    if( msLoadID.length() > 0 )
    {
      mpPnlCurrentLoc.reset(loadData.getWarehouse(),loadData.getAddress());
      mpPnlNextLoc.reset(loadData.getNextWarehouse(),loadData.getNextAddress());
      mpPnlFinalLoc.reset(loadData.getFinalWarehouse(),loadData.getFinalAddress());
    }
    else
    {
      mpPnlCurrentLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
      mpPnlCurrentLoc.reset();
      mpPnlNextLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
      mpPnlNextLoc.reset();
      mpPnlCurrentLoc.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
      mpPnlFinalLoc.reset();     
    }
    mpCmbContainerType.setSelectedItem(loadData.getContainerType());
    mpCmbLoadRoute.setSelectedItem(loadData.getRouteID());
    try
    {
      mpCmbLoadMoveStatus.setSelectedElement(loadData.getLoadMoveStatus());
      mpCmbLoadAmountFull.setSelectedElement(loadData.getAmountFull());
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
    }
    mpTxtLoadHeight.setValue(loadData.getHeight());
    mpTxtLoadDeviceID.setText(loadData.getDeviceID());
    mpTxtLoadZone.setText(loadData.getRecommendedZone());
    Date tempDate = loadData.getMoveDate();
    mpTxtLoadMoveDate.setDate(tempDate);
    mpTxtLoadMsg.setText(loadData.getLoadMessage());
    mpTxtBCRData.setText(loadData.getBCRData());
  }
  
  /**
   *   Load the location detail panel with the specified data.
   *   It is assumed that this frame has already been built when this
   *   method is called.
   */
  public void refreshLocationData(LocationData locData)
  {
    mpCurrentLocData = (LocationData)locData.clone();
    
    mpPnlRackLoc.reset(locData.getWarehouse(), locData.getAddress());
    
    try
    { 
      String vsLocType;
      vsLocType = DBTrans.getStringValue("iLocationType", locData.getLocationType());
      mpTxtLocType.setText(vsLocType);
      mpCmbLocStatus.setSelectedElement(locData.getLocationStatus());
      mpCmbLocEmptyFlag.setSelectedElement(locData.getEmptyFlag());
    }
    catch(NoSuchFieldException e)
    {
      displayWarning(e.getMessage(), "Translation Error");
    }
    mpTxtAisleGroup.setValue(mpCurrentLocData.getAisleGroup());
    mpTxtLocZone.setText(mpCurrentLocData.getZone());
    mpTxtLocDeviceID.setText(mpCurrentLocData.getDeviceID());
    mpTxtLocHeight.setValue(mpCurrentLocData.getHeight());
  }
  
  
  /*****************************************************************************
   * Item Detail panel stuff starts here
   ****************************************************************************/
  
  /**
   *  Method to intialize detail panel Components. This adds the items to the
   *  pop up menu.
   *
   */
  private void defineItemDetailPopUp()
  {
                                   // Popup menu for a selected row.
    mpItemDetailPopupMenu.add("Detail", VIEW_BTN,   new itemDetailButtonListener());
    mpItemDetailPopupMenu.add("Add",    ADD_BTN, new itemDetailButtonListener());
  }

  private void addItemDetailActionListeners() throws Exception
  {
    mpBtnItemView.addEvent(VIEW_BTN, new itemDetailButtonListener());
    mpBtnItemAdd.addEvent(ADD_BTN,   new itemDetailButtonListener());
    mpBtnItemDel.addEvent(ITEM_DEL_BTN,   new itemDetailButtonListener());
  }

  /**
   *  ItemDetail Button Listener helper class.
   */
  private class itemDetailButtonListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      String vs_which_button = e.getActionCommand();

      if (vs_which_button.equals(VIEW_BTN))
      {
        viewItemDetailButton();
      }
      else if (vs_which_button.equals(ADD_BTN))
      {
        addItemDetailButton();
      }
      else if (vs_which_button.equals(ITEM_DEL_BTN))
      {
        deleteItemDetailButton();
      }
    }
  }

  /**
   * Action method to handle View button. Brings up screen to do the updating.
   */
  protected void viewItemDetailButton()
  {
    int vnTotalSelected = mpItemDetailTable.getSelectedRowCount();
    if (vnTotalSelected == 0)
    {
      displayError("No row selected to View", "Selection Error");
      return;
    }
    
    LoadLineItemData vpLLIData = Factory.create(LoadLineItemData.class);
    Map m = new TreeMap(mpItemDetailTable.getSelectedRowData());
    // This map has some joined fields.  Remove them to avoid errors.
    m.remove(LoadData.WAREHOUSE_NAME);
    m.remove(LoadData.ADDRESS_NAME);
    m.remove(ItemMasterData.DESCRIPTION_NAME);
    // Convert the map to a LoadLineItemData
    vpLLIData.dataToSKDCData(m);
    
    if (vpLLIData.getItem().length() > 0)
    {
      ModifyLoadLineItem updateItemDetail = Factory.create(
          ModifyLoadLineItem.class, "View/Modify Item Detail", vpLLIData);
      
      final String vsLoadID = vpLLIData.getLoadID();
      addSKDCInternalFrameModal(updateItemDetail, mpPnlItemDetail,
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e)
            {
              String vsProp = e.getPropertyName();
              if (vsProp.equals(FRAME_CHANGE))
              {
                refreshItemDetailTable(vsLoadID);
              }
            }
          });
    }
  }

  /**
   * Action method to handle Add ItemDetail button. Brings up screen to do the
   * add.
   */
  void addItemDetailButton()
  { 
    msLoadID = mpCmbLoadID.getText().trim();
    
    AddLoadLineItem vpUpdateItemDetail = Factory.create(AddLoadLineItem.class,
        "Add Item Detail", msLoadID, null);
    addSKDCInternalFrameModal(vpUpdateItemDetail, mpPnlItemDetail,
    new PropertyChangeListener() 
    {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
          String vsProp = e.getPropertyName();
          if (vsProp.equals(FRAME_CHANGE))
          {
           refreshItemDetailTable(msLoadID);
          }
      }
    });
  }

  /**
   *  Action method to handle Delete button.
   *
   */
   void deleteItemDetailButton()
   {
     int vnTotalSelected = mpItemDetailTable.getSelectedRowCount();
     if (vnTotalSelected == 0)
     {
       displayError("No row selected to Delete", "Selection Error");
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
         deleteSelectedItems(vsChoices[0]);
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
               deleteSelectedItems(reasonCode);
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
         StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);
         int totalSelected = mpItemDetailTable.getSelectedRowCount();

         String[] delItemList = null;
         String[] delLotList = null;
         String[] delLoadList = null;
         String[] delOrderIDList = null;
         String[] delOrderLotList = null;
         String[] delLineIDList = null;
         String[] delPositionIDList = null;
         // Get selected list of Item details
         delItemList = mpItemDetailTable.getSelectedColumnData(LoadLineItemData.ITEM_NAME);
         delLotList = mpItemDetailTable.getSelectedColumnData(LoadLineItemData.LOT_NAME);
         delLoadList = mpItemDetailTable.getSelectedColumnData(LoadLineItemData.LOADID_NAME);
         delOrderIDList = mpItemDetailTable.getSelectedColumnData(LoadLineItemData.ORDERID_NAME);
         delOrderLotList = mpItemDetailTable.getSelectedColumnData(LoadLineItemData.ORDERLOT_NAME);
         delLineIDList = mpItemDetailTable.getSelectedColumnData(LoadLineItemData.LINEID_NAME);
         delPositionIDList = mpItemDetailTable.getSelectedColumnData(LoadLineItemData.POSITIONID_NAME);

         int delCount = 0;
         int[] deleteIndices = mpItemDetailTable.getSelectedRows();
         for(int row = 0; row < totalSelected; row++)
         {
           try
           {
             invtServ.deleteLoadLineItem(delLoadList[row], delItemList[row],
                                         delLotList[row], delOrderIDList[row],
                                         delOrderLotList[row], delLineIDList[row],
                                         delPositionIDList[row],reasonCode);
             delCount++;
           }
           catch(DBException exc)
           {
             displayError(exc.getMessage(), "Delete Error");
             // De-Select the troubling row!
             mpItemDetailTable.deselectRow(deleteIndices[row]);
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
         msLoadID = mpCmbLoadID.getText().trim();
         refreshItemDetailTable(msLoadID);
       }
     });
   }

  /**
    *  Method to refresh data in item detail List.
    */
   public void refreshItemDetailTable(String isLoadID)
   {
     try
     {
       List<Map> vaItemDetailList = mpInventoryServer.getLoadLineItemDataListByLoadID(isLoadID);
       try
       {
         mpItemDetailTable.refreshData(mpInventoryServer.getLoadLineItemDataListByLoadID(isLoadID));
         if (vaItemDetailList.isEmpty())
         {
           if( isLoadID.length() <= 0 )
           {
             return;
           }
           //displayInfoAutoTimeOut("No Load Item Details found");         
         }
         mpItemDetailTable.refreshData(vaItemDetailList);
       }
       catch (DBException e)
       {
         e.printStackTrace(System.out);
         displayError("Database Error: " + e);
       }
     }
     catch( DBException e)
     {
       e.printStackTrace(System.out);
       displayError("Database Error: " + e);
       return;
     }
   }
}
