package com.daifukuamerica.wrxj.swingui.station;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.Warehouse;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import com.daifukuamerica.wrxj.swingui.zone.RecommendedZoneComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating ports. Ports meaning stations that is.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateStation extends DacInputFrame
{
  protected String stationName = "";
  protected Map<String, AmountFullTransMapper> mpPartialQtyMap;
  protected StandardStationServer stationServ = Factory.create(StandardStationServer.class);
  protected StandardInventoryServer invtServ = Factory.create(StandardInventoryServer.class);
  protected StandardRouteServer routeServ = Factory.create(StandardRouteServer.class);
  protected StandardDeviceServer mpDeviceServer = Factory.create(StandardDeviceServer.class);
  protected StandardLocationServer locServ;
  protected Station   mpStation = Factory.create(Station.class);
  protected Warehouse warInterface = Factory.create(Warehouse.class);

  protected SKDCTranComboBox stationType = new SKDCTranComboBox();
  protected SKDCTranComboBox orderStatus = new SKDCTranComboBox();
  protected SKDCTranComboBox bidirectionalModeCombo = new SKDCTranComboBox();
  protected SKDCTextField descriptionText;
  protected SKDCTranComboBox confirmLoad = new SKDCTranComboBox();
  protected SKDCIntegerField height = new SKDCIntegerField(2);
  protected SKDCIntegerField maxEnroute = new SKDCIntegerField(4);
  protected SKDCTranComboBox confirmLot = new SKDCTranComboBox();
  protected SKDCIntegerField maxStaged = new SKDCIntegerField(4);
  protected SKDCTranComboBox stationStatus = new SKDCTranComboBox();
  protected SKDCTranComboBox confirmLocation = new SKDCTranComboBox();
  protected SKDCTranComboBox confirmQty = new SKDCTranComboBox();
  protected SKDCComboBox mpAmountEmptyCombo = new SKDCComboBox();
  protected SKDCDoubleField weight = new SKDCDoubleField(4);
  protected SKDCTranComboBox arrivalRequired = new SKDCTranComboBox();
  protected SKDCTranComboBox captive = new SKDCTranComboBox();
  protected SKDCTranComboBox confirmItem = new SKDCTranComboBox();
  protected SKDCTextField printerText;
  protected SKDCTextField mpLoadPrefixText = new SKDCTextField(DBInfo.getFieldLength(StationData.LOADPREFIX_NAME));
  protected SKDCTextField mpOrderPrefix = new SKDCTextField(DBInfo.getFieldLength(StationData.ORDERPREFIX_NAME));
  protected SKDCTextField stationNameText = new SKDCTextField(DBInfo.getFieldLength(StationData.STATIONNAME_NAME));
  protected SKDCTranComboBox deleteInventory = new SKDCTranComboBox();
  protected SKDCTranComboBox allowRoundRobin = new SKDCTranComboBox();
  protected SKDCTranComboBox poReceiveAllcombo = new SKDCTranComboBox();
  protected SKDCTranComboBox autoLoadMovementTypeCombo = new SKDCTranComboBox();
  protected SKDCTranComboBox mpCustomActionCombo = new SKDCTranComboBox();
  protected SKDCTranComboBox mpRetrievePriorityCombo = new SKDCTranComboBox();
  protected SKDCTranComboBox mpRetrieveCommandDetailCombo = new SKDCTranComboBox();
  protected ItemNumberInput mpItemInput;
  protected SKDCComboBox allocationType = new SKDCComboBox();
  protected SKDCComboBox defaultRoute = new SKDCComboBox();
  protected SKDCComboBox rejectRoute = new SKDCComboBox();
  protected SKDCComboBox linkRoute = new SKDCComboBox();
  protected SKDCComboBox containerType = new SKDCComboBox();
  protected SKDCComboBox deviceID = new SKDCComboBox();
  protected SKDCComboBox mpscale = new SKDCComboBox();
  protected SKDCComboBox warehouse = new SKDCComboBox();
  protected SKDCTranComboBox mpAutoOrderTypeCombo = new SKDCTranComboBox();
  protected RecommendedZoneComboBox mpRecZoneCombo = new RecommendedZoneComboBox();
  protected StationData defaultStationData = null; // will have default values
  protected StationData currentStationData = null;
  protected boolean readOnly = false;
  protected SKDCDoubleField   orderQty    = new SKDCDoubleField(DBInfo.getFieldLength(StationData.ORDERQUANTITY_NAME));
  protected SKDCTranComboBox allocationEnabled = new SKDCTranComboBox();
  protected SKDCTranComboBox mpCCIAllowed = new SKDCTranComboBox();
  protected JList       mpListReplenishSources = new JList();
  protected JScrollPane mpScrollReplenishSources;
  protected SKDCComboBox mpReprStationCombo = new SKDCComboBox();
  
  protected boolean mzAdding = true;
  
 /**
  *  Create station screen class.
  *
  *  @param isTitle Title to be displayed.
  */
  public UpdateStation(String isTitle)
  {
    super(isTitle, "Station Information");
    try
    {
      mpPartialQtyMap = LoadData.getAmountFullDecimalMap();
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

 /**
  *  Create default station screen class.
  *
  */
  public UpdateStation()
  {
    this("");
  }

  /**
   *  Method to set screen for modifing.
   *
   *  @param station Station to be modified.
   */
  public void setModify(String station)
  {
    stationName = station;
    readOnly = false;
    mzAdding = false;
    useModifyButtons();
  }

 /**
  *  Method to set screen for viewing.
  *
  *  @param station Station to be viewed.
  */
  public void setView(String station)
  {
    stationName = station;
    mzAdding = false;
    readOnly = true;
    useReadOnlyButtons();
  }

  /**
   * Overridden method so we can set up frame for either an add or modify
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    if (!mzAdding)
    {
      stationNameText.setText(stationName);
      stationNameText.setEnabled(false);

      defaultStationData = stationServ.getStation(stationName);
      
      stationType.setEnabled(SKDCUserData.isSuperUser());
      mpscale.setEnabled(SKDCUserData.isSuperUser());
      allocationEnabled.setEnabled(SKDCUserData.isSuperUser());
      mpCCIAllowed.setEnabled(SKDCUserData.isSuperUser());
      orderQty.setEnabled(SKDCUserData.isSuperUser());
      mpCustomActionCombo.setEnabled(SKDCUserData.isSuperUser());
      deviceID.requestFocus();

      if (readOnly)
      {
        mpBtnSubmit.setVisible(false);
        mpBtnClear.setVisible(false);
        // set all fields to readonly
        enablePanelComponents(mpInputPanel, false);
        
        /*
         * Since, for the life of me, I can't get this to work otherwise...
         */
        mpListReplenishSources.setEnabled(false);
      }
    }
    setData(defaultStationData);
  }

 /**
  *  Method to intialize screen components. This adds the components to the
  *  screen and adds listeners as needed.
  *
  *  @exception Exception
  */
  protected void jbInit() throws Exception
  {
    deviceID.setSelectedIndex(-1);

    locServ = Factory.create(StandardLocationServer.class);
    defaultStationData = Factory.create(StationData.class);

    initInputFields();
    buildInputPanel();

    linkRouteFill("");
    defaultRouteFill("");
    rejectRouteFill("");
    deviceIDFill();
    scaleFill();
    containerFill();
    warehouseFill();
    allocationTypeFill();
    reprStationFill();
  }

  protected void initInputFields()
  {
    /*
     * Make these display a bit smaller
     */
    descriptionText = new SKDCTextField(15);
    descriptionText.setMaxColumns(DBInfo.getFieldLength(StationData.DESCRIPTION_NAME));
    printerText = new SKDCTextField(15);
    printerText.setMaxColumns(DBInfo.getFieldLength(StationData.PRINTER_NAME));

    try
    {
      mpItemInput = new ItemNumberInput(invtServ, true, true);

      arrivalRequired = new SKDCTranComboBox(StationData.ARRIVALREQUIRED_NAME);

      stationType = new SKDCTranComboBox(StationData.STATIONTYPE_NAME);
      stationType.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          showBidirectionalModeFields();
          showDefaultRetvCmdDetail();
        }
      });
      bidirectionalModeCombo = new SKDCTranComboBox(StationData.BIDIRECTIONALSTATUS_NAME,
          new int[] { DBConstants.RETRIEVEMODE, DBConstants.STOREMODE }, false);
      bidirectionalModeCombo.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          showBidirectionalModeFields();
        }
      });
      stationStatus = new SKDCTranComboBox(StationData.STATUS_NAME);
      stationStatus.addItemListener(new ItemListener()
      {
        public void itemStateChanged(ItemEvent e)
        {
          try
          {
            if (stationStatus.getIntegerValue() == DBConstants.CAPTIVEINSERT)
            {
              bidirectionalModeCombo.setSelectedElement(DBConstants.STOREMODE);
            }
          }
          catch (NoSuchFieldException nsfe) {}
        }
      });
      
      captive = new SKDCTranComboBox(StationData.CAPTIVE_NAME);
      mpAmountEmptyCombo.setComboBoxData(buildAmountEmptyComboList());
      orderStatus = new SKDCTranComboBox(StationData.ORDERSTATUS_NAME,
          new int[] { DBConstants.READY, DBConstants.HOLD }, false);
      autoLoadMovementTypeCombo = new SKDCTranComboBox(StationData.AUTOLOADMOVEMENTTYPE_NAME);
      deleteInventory = new SKDCTranComboBox(StationData.DELETEINVENTORY_NAME);
      allowRoundRobin = new SKDCTranComboBox(StationData.ALLOWROUNDROBIN_NAME);
      poReceiveAllcombo = new SKDCTranComboBox(StationData.PORECEIVEALL_NAME);
      confirmLoad = new SKDCTranComboBox(StationData.CONFIRMLOAD_NAME);
      confirmLocation = new SKDCTranComboBox(StationData.CONFIRMLOCATION_NAME);
      confirmItem = new SKDCTranComboBox(StationData.CONFIRMITEM_NAME);
      confirmLot = new SKDCTranComboBox(StationData.CONFIRMLOT_NAME);
      confirmQty = new SKDCTranComboBox(StationData.CONFIRMQTY_NAME);
      allocationEnabled = new SKDCTranComboBox(StationData.ALLOCATIONENABLED_NAME);
      mpCCIAllowed = new SKDCTranComboBox(StationData.CCIALLOWED_NAME);
      mpCustomActionCombo = new SKDCTranComboBox(StationData.CUSTOMACTION_NAME);
      mpAutoOrderTypeCombo = new SKDCTranComboBox(StationData.AUTOORDERTYPE_NAME);
      mpRetrievePriorityCombo = new SKDCTranComboBox(StationData.PRIORITYCATEGORY_NAME);
      mpRetrieveCommandDetailCombo = new SKDCTranComboBox(StationData.RETRIEVECOMMANDDETAIL_NAME);
      
      String[] vasReplenSources = mpStation.getListOfValidReplenishSources();
      mpListReplenishSources.setListData(vasReplenSources);
      mpListReplenishSources.setVisibleRowCount(2);
      mpScrollReplenishSources = new JScrollPane(mpListReplenishSources);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }
  }

  protected void buildInputPanel()
  {
    boolean vzUseZones = locServ.hasRecommendedZonesDefined();
    /*
     * Add inputs
     */
    // Equipment-related configuration
    addInput("Station ID:", stationNameText);
    addInput("Description:", descriptionText);
    addInput("Type:", stationType);
    addInput("Bi-Directional Mode:", bidirectionalModeCombo);
    addInput("Mode:", stationStatus);
    addInput("Arrival Required:", arrivalRequired);
    addInput("Warehouse:", warehouse);
    addInput("Device ID:", deviceID);
    addInput("Scale:", mpscale);
    addInput("Representative Stn:", mpReprStationCombo);
    addInput("Captive:", captive);
    addInput("Default Route:", defaultRoute);
    addInput("Linked Route:", linkRoute);
    addInput("Reject Route:", rejectRoute);
    addInput("Allow Round Robin:", allowRoundRobin);
    addInput("Max Staged:", maxStaged);
    addInput("Max Enroute:", maxEnroute);
    
    // Storage-related configuration
    addSectionHeader("Storage");
//    addInput("Weight:", weight);                     TODO: Implement weight
    addInput("Auto Load Movement:", autoLoadMovementTypeCombo); // TODO: Split auto-store / auto-pick
    addInput("Item*:", mpItemInput);                            // TODO: Split auto-store item / auto-order item
    addInput("Order Quantity*:", orderQty);                     // TODO: Split auto-store qty / auto-order qty
    addInput("Load Prefix*:", mpLoadPrefixText);
    addInput("Height*:", height);                               // TODO: Split auto-store height / auto-order height
    if (vzUseZones)
    {
      addInput("Recommended Zone*:", mpRecZoneCombo);          // TODO: Split store location search / empty container allocation
    }
    addInput("Container Type*:", containerType);                // TODO: Split auto-store container / auto-order container
    addInput("", new SKDCLabel("* Also used for auto-ordering"));
    
    // Allocation
    addSectionHeader("Allocation");
    addInput("Allocation Enabled:", allocationEnabled);
    addInput("Allocation Type:", allocationType);
    
    if (vzUseZones)
    {
      addInput("", new SKDCLabel(""));
    }
    
    // Auto-ordering
    addSectionHeader("Auto-ordering");
    addInput("Auto-Order Type:", mpAutoOrderTypeCombo);
    addInput("Order Status:", orderStatus);
    addInput("Order Prefix:", mpOrderPrefix);
    // TODO: Add split item
    // TODO: Add split qty
    addInput("Amount Empty:", mpAmountEmptyCombo);
    // TODO: Add split height
    // TODO: Add split zone
    
    // Picking
    addSectionHeader("Picking");
    addInput("Cycle Count Allowed:", mpCCIAllowed);
    addInput("Delete Inventory:", deleteInventory);
    addInput("Retrieval Priority:", mpRetrievePriorityCombo);
    addInput("Retrieval Cmd. Detail:", mpRetrieveCommandDetailCombo);
    // TODO: Add split AutoPick
    addInput("Confirm Location:", confirmLocation);
    addInput("Confirm Load:", confirmLoad);
    addInput("Confirm Item:", confirmItem);
    addInput("Confirm Lot:", confirmLot);
    addInput("Confirm Qty:", confirmQty);
    
    addInput("Printer:", printerText);
    
    // Custom
    addSectionHeader("Custom");
    addInput("Custom Action:", mpCustomActionCombo);

    // These do nothing and should probably be purged from WRx
//    addSectionHeader("Useless");
//    addInput("Process Short:", processShort);
//    addInput("Receive Entire PO:", poReceiveAllcombo);
//    addInput("Replenish Sources:",  mpScrollReplenishSources, 2);
    
    setInputColumns(3);
    
    useAddButtons();
  }

  /**
   * Break up the columns a bit
   * @param isHeader
   */
  protected void addSectionHeader(String isHeader)
  {
    addInput("", new SKDCLabel("<HTML><u>" + isHeader + "</u></HTML>"));
  }
  
  /**
   *  Method to clean up as needed at closing.
   *
   */
  @Override
  public void cleanUpOnClose()
  {
    stationServ.cleanUp();
    invtServ.cleanUp();
    routeServ.cleanUp();
    locServ.cleanUp();
    mpDeviceServer.cleanUp();
  }

  /**
   *  Method to populate the container type combo box.
   */
  protected void containerFill()
  {
    try
    {
      List containerList = invtServ.getContainerTypeList();
      containerType.setComboBoxData(containerList);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   *  Method to populate the warehouse combo box.
   */
  protected void warehouseFill()
  {
    try
    {
      String[] warehouseList = warInterface.getRegularWarehouseChoices("");
      warehouse.setComboBoxData(warehouseList);

    }
    catch(Exception e)
    {
      displayError(e.getMessage(), "DB Error");
    }
  }
  
  /**
   * Method to populate the allocation type combo box.
   */
  protected void allocationTypeFill()
  {
    try
    {
      String[] vasAllocationTypes = stationServ.getAllocationTypes();
      allocationType.setComboBoxData(vasAllocationTypes);
    }
    catch (DBException ex)
    {
      displayError("Database Error: " + ex);
    }
  }

  /**
   * Method to populate the representative station combo box.
   */
  protected void reprStationFill()
  {
    try
    {
      List reprStns = stationServ.getStationNameList();
      //
      // Make sure we have a selection of "None"
      //
      if (!reprStns.contains(""))
      {
        reprStns.add(0, "");
      }
      mpReprStationCombo.setComboBoxData(reprStns);
    }
    catch (DBException ex)
    {
      displayError("Database Error: " + ex);
    }
  }

  /**
   *  Method to populate the device combo box.
   */
  protected void deviceIDFill()
  {
    try
    {
      String[] deviceIDList = locServ.getDeviceIDList(false);
      deviceID.setComboBoxData(deviceIDList);
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }
  
  /**
   * Method to populate the Scale combo box
   */
  protected void scaleFill()
  {
  	try
  	{
  		String[] vpScaleList = mpDeviceServer.getDevicesNameListByType(DBConstants.SCALE, SKDCConstants.EMPTY_VALUE);
  		mpscale.setComboBoxData(vpScaleList);
   	}
  	catch (DBException e)
  	{
  		logger.logException(e);
  		displayError("Database Error: " + e);
  	}
  }

  /**
   *  Method to populate the default route combo box.
   *
   *  @param srch Name to match.
   */
  protected void defaultRouteFill(String srch)
   {
     try
     {
       List routeList = routeServ.getRouteNameList(srch);
       //
       // Make sure we have a selection of "None"
       //
       if (!routeList.contains(""))
       {
         routeList.add(0, "");
       }
       defaultRoute.setComboBoxData(routeList);
     }
     catch (DBException e)
     {
       e.printStackTrace(System.out);
       displayError("Database Error: " + e);
     }
   }
  
 /**
  *  Method to populate the reject route combo box.
  *
  *  @param srch Name to match.
  */
  protected void rejectRouteFill(String srch)
  {
    try
    {
      List routeList = routeServ.getRouteNameList(srch);
      //
      // Make sure we have a selection of "None"
      //
      if (!routeList.contains(""))
      {
        routeList.add(0, "");
      }
      rejectRoute.setComboBoxData(routeList);
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

 /**
  *  Method to populate the link route combo box.
  *
  *  @param srch Name to match.
  */
  protected void linkRouteFill(String srch)
  {
    try
    {
      List routeList = routeServ.getRouteNameList(srch);
      //
      // Make sure we have a selection of "None"
      //
      if (!routeList.contains(""))
      {
        routeList.add(0, "");
      }
      linkRoute.setComboBoxData(routeList);
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
   *  Action method to handle Clear button.
   *
   */
  @Override
  public void clearButtonPressed()
  {
    setData(defaultStationData);
  }

 /**
  *  Method to refresh screen fields.
  *
  *  @param sd Station data to use in refreshing.
  *
  */
  protected void setData(StationData sd)
  {
    printerText.setText(sd.getPrinter());
    mpLoadPrefixText.setText(sd.getLoadPrefix());
    mpOrderPrefix.setText(sd.getOrderPrefix());
    descriptionText.setText(sd.getDescription());
    containerType.setSelectedItem(sd.getContainerType());
    warehouse.setSelectedItem(sd.getWarehouse());
    mpRecZoneCombo.setSelectedItem(sd.getRecommendedZone());
    deviceID.setSelectedItem(sd.getDeviceID());
    defaultRoute.setSelectedItem(sd.getDefaultRoute());
    linkRoute.setSelectedItem(sd.getLinkRoute());
    rejectRoute.setSelectedItem(sd.getRejectRoute());
    mpItemInput.setSelectedItem(sd.getItem());
    allocationType.setSelectedItem(sd.getAllocationType());
    mpReprStationCombo.setSelectedItem(sd.getReprStationName());
    mpscale.setSelectedItem(sd.getStationScale());

    try
    {
      arrivalRequired.setSelectedElement(sd.getArrivalRequired());
      stationStatus.setSelectedElement(sd.getStatus());
      stationType.setSelectedElement(sd.getStationType());
      captive.setSelectedElement(sd.getCaptive());
      mpAmountEmptyCombo.setSelectedItem(LoadData.convAmountFullToFractionString(sd.getAmountFull()));
      orderStatus.setSelectedElement(sd.getOrderStatus());
      autoLoadMovementTypeCombo.setSelectedElement(sd.getAutoLoadMovementType());
      deleteInventory.setSelectedElement(sd.getDeleteInventory());
      allowRoundRobin.setSelectedElement(sd.getAllowRoundRobin());
      poReceiveAllcombo.setSelectedElement(sd.getPoReceiveAll());
      confirmLoad.setSelectedElement(sd.getConfirmLoad());
      confirmLocation.setSelectedElement(sd.getConfirmLocation());
      confirmItem.setSelectedElement(sd.getConfirmItem());
      confirmLot.setSelectedElement(sd.getConfirmLot());
      confirmQty.setSelectedElement(sd.getConfirmQty());      
      allocationEnabled.setSelectedElement(sd.getAllocationEnabled());
      mpCCIAllowed.setSelectedElement(sd.getCCIAllowed());
      bidirectionalModeCombo.setSelectedElement(sd.getBidirectionalStatus());
      mpCustomActionCombo.setSelectedElement(sd.getCustomAction());
      mpAutoOrderTypeCombo.setSelectedElement(sd.getAutoOrderType());
      mpRetrievePriorityCombo.setSelectedElement(sd.getRetrievalPriority());
      mpRetrieveCommandDetailCombo.setSelectedElement(sd.getRetrieveCommandDetail());
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
    }
    
    try
    {
      String[] vasReplenishSources = mpStation.getReplenishmentSourcesAsStrings(sd.getStationName());
      int[] vanSelection = new int[vasReplenishSources.length];
      mpListReplenishSources.clearSelection();
      for (int i=0; i < vasReplenishSources.length; i++)
      {
        mpListReplenishSources.setSelectedValue(vasReplenishSources[i], false);
        vanSelection[i] = mpListReplenishSources.getSelectedIndex();
      }
      mpListReplenishSources.setSelectedIndices(vanSelection);
    }
    catch (DBException e3)
    {
      e3.printStackTrace(System.out);
      displayError("DB Error: " + e3);
    }
    maxEnroute.setValue(sd.getMaxAllowedEnroute());
    maxStaged.setValue(sd.getMaxAllowedStaged());
    weight.setValue(sd.getWeight());
    height.setValue(sd.getHeight());
    orderQty.setValue(sd.getOrderQuantity());
    stationNameText.setText(sd.getStationName());
    
    currentStationData = (StationData)sd.clone();

    showBidirectionalModeFields();
  }

  /**
   *  Action method to handle Close button.
   *
   */
  @Override
  public void closeButtonPressed()
  {
    close();
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new station to the database.
   *
   *  @param e Action event.
   *
   */
  @Override
  public void okButtonPressed()
  {
    boolean alreadyExists;
    alreadyExists = stationServ.exists(stationNameText.getText().trim());

    if (mzAdding && alreadyExists)
    {
      displayInfoAutoTimeOut("Station " + stationNameText.getText().trim() + " already exists");
      return;
    }

    if (!mzAdding && !alreadyExists)
    {
      displayInfoAutoTimeOut("Station " + stationNameText.getText().trim() + " does not exist");
      return;
    }
    
    StationData stationData = Factory.create(StationData.class);
    // fill in Station data

    stationData.setStationName(stationNameText.getText().trim());
    if (stationData.getStationName().trim().length() <= 0)  // required
    {
      displayInfoAutoTimeOut("Station name is required");
      return;
    }

    if (warehouse.getSelectedIndex() < 0)  // required
    {
      displayInfoAutoTimeOut("Warehouse is required");
      return;
    }

    if ((orderQty.getValue() > 0) && (mpItemInput.getText().length() <= 0))
    {
      displayInfo("Item cannot be blank for order quantity greater than zero");
      return;
    }

    stationData.setPrinter(printerText.getText().trim());
    stationData.setLoadPrefix(mpLoadPrefixText.getText().trim());
    stationData.setOrderPrefix(mpOrderPrefix.getText().trim());
    stationData.setDescription(descriptionText.getText());

    stationData.setMaxAllowedEnroute(maxEnroute.getValue());
    stationData.setMaxAllowedStaged(maxStaged.getValue());
    stationData.setWeight(weight.getValue());
    stationData.setHeight(height.getValue());
    stationData.setOrderQuantity(orderQty.getValue());

    stationData.setItem(mpItemInput.getText());
    stationData.setContainerType( containerType.getText().trim());
    stationData.setWarehouse( warehouse.getText().trim());
    stationData.setRecommendedZone(mpRecZoneCombo.getText().trim());
    stationData.setDefaultRoute(defaultRoute.getText().trim());
    stationData.setLinkRoute(linkRoute.getText().trim());
    stationData.setRejectRoute(rejectRoute.getText().trim());
    stationData.setDeviceID(deviceID.getText().trim());
    stationData.setStationScale(mpscale.getText().trim());
    stationData.setAllocationType(allocationType.getText().trim());
    stationData.setReprStationName(mpReprStationCombo.getText().trim());

    try
    {
      stationData.setArrivalRequired(arrivalRequired.getIntegerValue());
      stationData.setStatus(stationStatus.getIntegerValue());
/*---------------------------------------------------------------------------
   This is really the amount empty they want to keep on order.  We order by
   amount empty and the allocator tries to fill the order in terms of amount full
   since that's what the unit of measure is for "emptiness" of a load (iAmountFull).
  ---------------------------------------------------------------------------*/
      int vnAmtFullTranVal;
      if (((String)mpAmountEmptyCombo.getSelectedItem()).equalsIgnoreCase("Empty"))
      {
        vnAmtFullTranVal = DBConstants.EMPTY;
      }
      else
      {
        AmountFullTransMapper vpTranMapper = mpPartialQtyMap.get(mpAmountEmptyCombo.getSelectedItem());
        vnAmtFullTranVal = vpTranMapper.getPartialAmtFullTranVal();
      }
      stationData.setAmountFull(vnAmtFullTranVal);
      
      stationData.setOrderStatus(orderStatus.getIntegerValue());
      stationData.setStationType(stationType.getIntegerValue());
      stationData.setAllocationEnabled(allocationEnabled.getIntegerValue());
      stationData.setCCIAllowed(mpCCIAllowed.getIntegerValue());
      stationData.setAutoLoadMovementType(autoLoadMovementTypeCombo.getIntegerValue());
      stationData.setDeleteInventory(deleteInventory.getIntegerValue());
      stationData.setAllowRoundRobin(allowRoundRobin.getIntegerValue());
      stationData.setPoReceiveAll(poReceiveAllcombo.getIntegerValue());
      stationData.setCaptive(captive.getIntegerValue());
      stationData.setConfirmItem(confirmItem.getIntegerValue());
      stationData.setConfirmLot(confirmLot.getIntegerValue());
      stationData.setConfirmQty(confirmQty.getIntegerValue());
      stationData.setConfirmLocation(confirmLocation.getIntegerValue());
      stationData.setConfirmLoad(confirmLoad.getIntegerValue());
      stationData.setReplenishSources(getReplenSourceString());
      stationData.setCustomAction(mpCustomActionCombo.getIntegerValue());
      stationData.setAutoOrderType(mpAutoOrderTypeCombo.getIntegerValue());
      stationData.setRetrievalPriority(mpRetrievePriorityCombo.getIntegerValue());
      stationData.setRetrieveCommandDetail(mpRetrieveCommandDetailCombo.getIntegerValue());
      
      if (stationData.getStationType() == DBConstants.REVERSIBLE ||
          stationData.getStationType() == DBConstants.PDSTAND)
      {
        stationData.setBidirectionalStatus(bidirectionalModeCombo.getIntegerValue());
      }
      else
      {
        stationData.deleteColumnObject(StationData.BIDIRECTIONALSTATUS_NAME);
      }
      
      if (stationData.getAutoLoadMovementType() == DBConstants.AUTORECEIVE_ITEM)
      {
        boolean vzError = false;
        if (stationData.getItem().trim().length() == 0)
        {
          displayInfo("Item cannot be blank for \"Auto Receive With Item\"");
          mpItemInput.requestFocus();
          vzError = true;
        }
        else if (stationData.getOrderQuantity() <= 0)
        {
          displayInfo("Auto-Receipt quantity (Order Qty.) must " + SKDCConstants.EOL_CHAR +
                      "be greater than zero for \"Auto Receive With Item\"!");
          orderQty.requestFocus();
          vzError = true;
        }
        
        if (vzError) return;
      }
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
      return;
    }

    try
    {
      if (mzAdding)
      {
        stationServ.addStation(stationData);
        this.changed();
        displayInfoAutoTimeOut("Station " + stationNameText.getText().trim() + " added");
      }
      else
      {
        stationServ.updateStation(stationData);
        this.changed();
        displayInfoAutoTimeOut("Station " + stationNameText.getText().trim() + " updated");
        
        /*
         * If this is a reversible station, update the SRC.
         * 
         * Actually, due to weird logic, the SRC reports retrieve mode when the
         * station has a captive load to store after a retrieval.  So if there 
         * is a Store Pending or Move Pending load at the station, just leave it
         * in store mode.
         * 
         * Also, functionally in Warehouse Rx, only REVERSIBLE stations use 
         * mode change regardless of what it actually physically is.  P&D stands
         * in Warehouse Rx do NOT use mode change.  If a P&D stand requires mode
         * change, call it a reversible station instead.
         */
        if (stationData.getStationType() == DBConstants.REVERSIBLE)
        {
          StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
          LoadData vpSPLoad = vpLoadServer.getOldestLoadData(
              defaultStationData.getStationName(), DBConstants.STOREPENDING);
          LoadData vpMPLoad = vpLoadServer.getOldestLoadData(
              defaultStationData.getStationName(), DBConstants.MOVEPENDING);
          
          if (stationData.getBidirectionalStatus() != DBConstants.STOREMODE ||
              (vpSPLoad == null && vpMPLoad == null))
          {
            stationServ.sendBiDirectionalChangeCommand(stationData);
          }
        }
      }
    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      if (mzAdding)
      {
        displayError("Error adding station " + stationNameText.getText().trim()
          + " - " + e2.getMessage());
      }
      else
      {
        displayError("Error updating station " + stationNameText.getText().trim()
          + " - " + e2.getMessage());
      }
    }
    if (!mzAdding)
    {
      close();
    }
  }

  /**
   * Get the ReplenishSources string
   * @return
   */
  protected String getReplenSourceString()
  {
    Object[] vapSelected = mpListReplenishSources.getSelectedValues();
    String[] vasSelected = new String[vapSelected.length];
    
    for (int i=0; i < vapSelected.length; i++)
    {
      vasSelected[i] = (String)vapSelected[i];
    }
    
    return mpStation.getReplenishmentSourcesString(vasSelected);
  }
  
  /**
   *
   */
  protected void showBidirectionalModeFields()
  {
    if (!readOnly) //don't override view-only mode
    {
      try
      {
        int vnType = stationType.getIntegerValue();
        int vnMode = bidirectionalModeCombo.getIntegerValue();
        
        boolean vzBidirectional = (vnType == DBConstants.REVERSIBLE);
        bidirectionalModeCombo.setEnabled(vzBidirectional);
    
        boolean vbOrderable = ((vnType != DBConstants.INPUT) &&
                               !((vnMode == DBConstants.STOREMODE) && vzBidirectional)); //disable OrderType for bidirectional CO's in store mode
        mpAutoOrderTypeCombo.setEnabled(vbOrderable);
      }
      catch (NoSuchFieldException e2)
      {
        e2.printStackTrace(System.out);
        displayError("No Such Field: " + e2);
        return;
      }
    }
  }

  protected void showDefaultRetvCmdDetail()
  {
    try
    {
      switch (stationType.getIntegerValue())
      {
        case DBConstants.PDSTAND:
        case DBConstants.REVERSIBLE:
          mpRetrieveCommandDetailCombo.setSelectedElement(DBConstants.PICKING_RETRIEVAL);
          break;

        default:
          mpRetrieveCommandDetailCombo.setSelectedElement(DBConstants.UNIT_RETRIEVAL);
      }
    }
    catch(NoSuchFieldException ex)
    {
      displayError("Translation error for " +
                   StationData.RETRIEVECOMMANDDETAIL_NAME + ex.getMessage());
    }
  }

  protected Object[] buildAmountEmptyComboList()
  {
    Object[] vpEmptyAmts = mpPartialQtyMap.keySet().toArray();
    Object[] vpNewList = new Object[vpEmptyAmts.length + 1];
    
    try
    {
      vpNewList[0] = DBTrans.getStringValue(StationData.AMOUNTFULL_NAME, DBConstants.EMPTY);
      for(int vnIdx = 0; vnIdx < vpEmptyAmts.length; vnIdx++)
      {
        vpNewList[vnIdx+1] = vpEmptyAmts[vnIdx];
      }
    }
    catch(NoSuchFieldException nsf)
    {
    }
    
    return(vpNewList);
  }

  /**
   * Set the Zone Enabled flag based upon the system configuration and 
   * fill the zone list if necessary
   */
  public boolean hasRecommendedZonesDefined()
  {
    boolean vzZoneEnabled = false;
    
    try
    {
      vzZoneEnabled = Factory.create(StandardLocationServer.class).getZoneGroupList("").size() > 0;
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
    
    return vzZoneEnabled;
  }

}
