package com.daifukuamerica.wrxj.swingui.load;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.swingui.zone.RecommendedZoneComboBox;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating loads.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateLoad extends DacInputFrame
{
  protected SKDCTextField mpTxtLoadID = new SKDCTextField(LoadData.LOADID_NAME);
  protected SKDCTextField mpTxtLoadMsg = new SKDCTextField(LoadData.LOADMESSAGE_NAME);
  protected SKDCTextField mpTxtBCR = new SKDCTextField(LoadData.BCRDATA_NAME);
  protected SKDCDoubleField mpDblWeight = new SKDCDoubleField(LoadData.WEIGHT_NAME);
  protected SKDCComboBox mpHeightCombo;
  protected List<Object> mpHeightList = new ArrayList<Object>();
  protected SKDCComboBox mpComboContainer = new SKDCComboBox();
  protected RecommendedZoneComboBox mpComboZone = new RecommendedZoneComboBox();
  protected SKDCComboBox mpComboRoute = new SKDCComboBox();
  protected SKDCComboBox mpComboDevice = new SKDCComboBox();
  protected SKDCTranComboBox mpComboAmountFull;
  protected SKDCTranComboBox mpComboLoadPresenceCheck;
  protected SKDCTranComboBox mpComboLoadMoveStatus;
  protected LocationPanel mpLocation;
  protected LocationPanel mpNextLocation;
  protected LocationPanel mpFinalLocation;

  protected SKDCUserData userData = new SKDCUserData();

  protected StandardDeviceServer mpDevServer     = Factory.create(StandardDeviceServer.class);
  protected StandardInventoryServer mpInvServer  = Factory.create(StandardInventoryServer.class);
  protected StandardLoadServer mpLoadServer      = Factory.create(StandardLoadServer.class);
  protected StandardLocationServer mpLocServer   = Factory.create(StandardLocationServer.class);
  protected StandardRouteServer mpRouteServer    = Factory.create(StandardRouteServer.class);

  protected LoadData defaultLoadData = Factory.create(LoadData.class);

  protected String msLoad = "";
  protected String msWarehouse = "";
  protected String msAddress = "";
  protected boolean mzReadOnly = false;
  protected boolean mzCloseOnSubmit = false;
  protected boolean mzByLocation = false;

  /**
   * Constructor for insertion of a load at a station
   *
   * @param isTitle
   * @param isWarehouse
   * @param isAddress
   */
  public UpdateLoad(String isTitle, StationData ipStationData)
  {
    this(isTitle);

//    mpLocation.reset(ipStationData.getWarehouse(), ipStationData.getStationName());
    mpLocation.setEnabled(false);

    defaultLoadData.setWarehouse(ipStationData.getWarehouse());
    defaultLoadData.setAddress(ipStationData.getStationName());
    defaultLoadData.setRouteID(ipStationData.getStationName());
    defaultLoadData.setDeviceID(ipStationData.getDeviceID());
    defaultLoadData.setLoadMoveStatus(DBConstants.ARRIVED);
    defaultLoadData.setContainerType("Pallet");

    if (ipStationData.getCaptive() == DBConstants.CAPTIVE)
    {
      mpTxtLoadID.addActionListener(new java.awt.event.ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          loadToLocation();
        }
      });
    }

    mzCloseOnSubmit = true;
  }

  /**
   * Parse a load into a location for a captive rack
   */
  protected void loadToLocation()
  {
    String vsWarehouse = defaultLoadData.getWarehouse();

    mpNextLocation.reset(vsWarehouse, "0" + mpTxtLoadID.getText().trim());
    mpFinalLocation.reset(vsWarehouse, "0" + mpTxtLoadID.getText().trim());
  }

  /**
   *  Create load screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateLoad(String isTitle)
  {
    super(isTitle, "Load Information");
    try
    {
      jbInit();
      pack();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public UpdateLoad(String isFrameTitle, String isInputTitle)
  {
    super(isFrameTitle, isInputTitle);
  }

  /**
   * Create default load screen class.
   */
  public UpdateLoad()
  {
    this("");
  }

  /**
   * Method to set screen for modifying.
   *
   * @param s load to be modified.
   */
  public void setModify(String s)
  {
    msLoad = s;
    mzReadOnly = false;
    useModifyButtons();
  }

  /**
   * Method for adding a load to a given location.
   *
   * @param isWarehouse
   * @param isAddres
   */
  public void setAddToLocation(String isWarehouse, String isAddress )
  {
    mzReadOnly = false;
    mzByLocation = true;
    msWarehouse = isWarehouse;
    msAddress = isAddress;
    useModifyButtons();
  }

  /**
   * Method to set screen for viewing.
   *
   * @param s Load to be viewed.
   */
  public void setView(String s)
  {
    msLoad = s;
    mzReadOnly = true;
    useReadOnlyButtons();
  }

  /**
   * Method to populate the container type combo box.
   *
   * @param srch Name to match.
   */
  void containerFill()
  {
    try
    {
      List containerList = mpInvServer.getContainerTypeList();
      mpComboContainer.setComboBoxData(containerList);
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   *  Method to populate the route combo box.
   *
   *  @param srch Name to match.
   */
  void routeFill(String srch)
  {
    try
    {
      List routeList = mpRouteServer.getRouteNameList(srch);
      routeList.add("");
      mpComboRoute.setComboBoxData(routeList);
    }
    catch (DBException e)
    {
      e.printStackTrace(System.out);
      displayError("Database Error: " + e);
    }
  }

  /**
  *  Method to populate the device combo box.
  *
  */
  void deviceFill()
  {
   try
   {
     String[] deviceList = mpLocServer.getDeviceIDList(false);
     mpComboDevice.setComboBoxData(deviceList);
   }
   catch (DBException e)
   {
     e.printStackTrace(System.out);
     displayError("Database Error: " + e);
   }
  }

 /**
  *  Method to clean up as needed at closing.
  *
  */
  @Override
  public void cleanUpOnClose()
  {
    mpInvServer.cleanUp();
    mpInvServer = null;
    mpRouteServer.cleanUp();
    mpRouteServer = null;
    mpDevServer.cleanUp();
    mpDevServer = null;
    mpLocServer.cleanUp();
    mpLocServer = null;
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

    if (msLoad.length() > 0) // we are modifying
    {  // fill in current data
      mpTxtLoadID.setText(msLoad);
      mpTxtLoadID.setEnabled(false);

      try
      {
        defaultLoadData = mpLoadServer.getLoad1(mpTxtLoadID.getText());
      }
      catch (DBException e2)
      {
        displayError("Unable to get Load data");
        return;
      }
      setData(defaultLoadData);

      if (!mzReadOnly)
      {
        setTimeout(90);
      }
    }
    else
    {
      /*
       * Display Location in Add screen
       */
      if( mzByLocation)
      {
        defaultLoadData.setWarehouse(msWarehouse);
        defaultLoadData.setAddress(msAddress);
      }
      defaultLoadData.setHeight(Integer.parseInt(mpHeightCombo.getSelectedItem().toString()));
      setData(defaultLoadData);
//      mpTxtLoadID.grabFocus();
    }

    if (mzReadOnly)
    {
      mpBtnSubmit.setVisible(false);
      mpBtnClear.setVisible(false);
      // set all fields to readonly
      enablePanelComponents(mpInputPanel, false);
    }

//    mpComboLoadMoveStatus.setEnabled(userData.isSuperUser() && !mzReadOnly);
//    mpComboDevice.setEnabled(userData.isSuperUser());
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  protected void jbInit() throws Exception
  {
    initInputFields();
    buildInputPanel();

    containerFill();
    routeFill("");
    deviceFill();
  }

  /**
   * Initialize the input fields
   */
  protected void initInputFields()
  {
    mpLocation = Factory.create(LocationPanel.class);
    mpLocation.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
    mpLocation.setInputRequired();
    //mpLocation.setAutoSelectFormatOnReset(true);
    mpNextLocation = Factory.create(LocationPanel.class);
    mpNextLocation.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
//    nextLocation.setInputRequired();
    mpNextLocation.setAutoSelectFormatOnReset(true);
    mpFinalLocation = Factory.create(LocationPanel.class);
    mpFinalLocation.setWarehouseList(LocationPanel.WTYPE_REGULAR,false);
//    finalLocation.setInputRequired();
    mpFinalLocation.setAutoSelectFormatOnReset(true);

    try
    {
      mpComboAmountFull = new SKDCTranComboBox(LoadData.AMOUNTFULL_NAME);
      mpComboAmountFull.setSelectedElement(DBConstants.EMPTY);
      mpComboLoadPresenceCheck = new SKDCTranComboBox(LoadData.LOADPRESENCECHECK_NAME);
      mpComboLoadMoveStatus = new SKDCTranComboBox(LoadData.LOADMOVESTATUS_NAME);
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
    }

    Integer[] vapHeights = { 0, 1, 2, 3 };
    try
    {
      vapHeights = Factory.create(StandardLocationServer.class).getLocationHeights();
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
    for (Integer i : vapHeights)
    {
      mpHeightList.add(i);
    }
    mpHeightCombo = new SKDCComboBox(mpHeightList, false);
  }

  /**
   * Build the input panel
   */
  protected void buildInputPanel()
  {
    addInput("Load ID:",          mpTxtLoadID);
    addInput("Container Type:",   mpComboContainer);
    addInput("Location:",         mpLocation);
    addInput("Next Location:",    mpNextLocation);
    addInput("Final Location:",   mpFinalLocation);
    addInput("Route:",            mpComboRoute);
    addInput("Weight:",           mpDblWeight);
    addInput("Height:",           mpHeightCombo);

    addInput("Move Status:",      mpComboLoadMoveStatus);
    addInput("Message:",          mpTxtLoadMsg);
    addInput("Bar Code:",         mpTxtBCR);
    addInput("Amount Full:",      mpComboAmountFull);
    addInput("LP Check:",         mpComboLoadPresenceCheck);
    addInput("Device ID:",        mpComboDevice);
    if (mpLocServer.hasRecommendedZonesDefined())
    {
      addInput("Recommended Zone:", mpComboZone);
    }
    //setInputColumns(2);

    useAddButtons();
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new load to the database.
   *
   *  TODO: This function is awfully similar to LoadLocationDetailFrame.loadSubmitButtonPressed().
   *  The two should probably be combined.
   */
  @Override
  protected void okButtonPressed()
  {
    boolean alreadyExists;
    String vsLoadID = mpTxtLoadID.getText().trim();

    if (mpComboContainer.getText().length() <= 0)  // required
    {
      displayError("Container Type is required");
      return;
    }

    if (vsLoadID.length() == 0)
    {
      displayError("Load ID cannot be blank.");
      return;
    }

    alreadyExists = mpLoadServer.loadExists(vsLoadID);

    boolean adding = (msLoad.length() == 0);

    if (adding && alreadyExists)
    {
      displayError("Load " + vsLoadID + " already exists");
      return;
    }

    if (!adding && !alreadyExists)
    {
      displayError("Load " + vsLoadID + " does not exist");
      return;
    }

    // fill in load data
    LoadData ld;
    if (adding)
    {
      ld = Factory.create(LoadData.class);
    }
    else
    {
      try
      {
        ld = mpLoadServer.getLoad1(vsLoadID);
      }
      catch (DBException e2)
      {
        displayError("Unable to get Load data");
        return;
      }
    }

    LocationData lc = null;
    try
    {
      LocationData lcSearch = Factory.create(LocationData.class);
      lcSearch.setKey(LocationData.WAREHOUSE_NAME,mpLocation.getWarehouseString());
      lcSearch.setKey(LocationData.ADDRESS_NAME,mpLocation.getAddressString());
      lc = mpLocServer.getLocationRecord(lcSearch);
      if (lc == null)
      {
        displayError("Location does not exist");
        return;
      }

      /*
       * Don't allow a load to be added or moved into an occupied ASRS location
       */
      if ((adding ||
        ((!adding && (!ld.getWarehouse().trim().equals(lc.getWarehouse().trim()))
        || (!ld.getAddress().trim().equals(lc.getAddress().trim())))))
        && (lc.getLocationType() == DBConstants.LCASRS)
        && (lc.getEmptyFlag() != DBConstants.UNOCCUPIED))
      {
        if(!displayYesNoPrompt("Location: " + mpLocation.getWarehouseString() +
                               "-" + mpLocation.getAddressString() +
                " is Occupied...are you sure you want to add a load here"))
        {
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
      ld.setAmountFull(mpComboAmountFull.getIntegerValue());
      ld.setLoadPresenceCheck(mpComboLoadPresenceCheck.getIntegerValue());
      ld.setLoadMoveStatus(mpComboLoadMoveStatus.getIntegerValue());

      ld.setLoadID(vsLoadID);
      ld.setParentLoadID(vsLoadID);
      ld.setWarehouse(mpLocation.getWarehouseString());
      ld.setAddress(mpLocation.getAddressString().trim());
//      ld.setAisleGroup(lc.getAisleGroup());
//      if (userData.isSuperUser() && mpComboDevice.getText().length() > 0)
      if (mpComboDevice.getText().length() > 0)
      {
        if(!mpComboDevice.getText().equals(lc.getDeviceID()))
        {
          if(displayYesNoPrompt("Load Device ID \"" + mpComboDevice.getText() +
                 "\" Does not match the Location Device ID \"" + lc.getDeviceID() +
                         "\".\nDo you want to set it to match the Location"))
          {
            ld.setDeviceID(lc.getDeviceID());
          }
          else
          {
            ld.setDeviceID(mpComboDevice.getText());
          }
        }
        else
        {
          ld.setDeviceID(mpComboDevice.getText());
        }
      }
      else
      {
        ld.setDeviceID(lc.getDeviceID());
      }
      ld.setLoadMessage(mpTxtLoadMsg.getText());
      ld.setBCRData(mpTxtBCR.getText());
      ld.setContainerType(mpComboContainer.getText());
      ld.setRecommendedZone(mpComboZone.getText());
      ld.setRouteID(mpComboRoute.getText());
      ld.setFinalWarehouse(mpFinalLocation.getWarehouseString());
      ld.setFinalAddress(mpFinalLocation.getAddressString().trim());
      if(ld.getFinalAddress().trim().length() <=0)
      {
        ld.setFinalWarehouse("");
        ld.setFinalAddress("");
      }
      else
      {
        if (!mpLocServer.exists(ld.getFinalWarehouse(), ld.getFinalAddress()))
        {
          displayInfoAutoTimeOut(Factory.create(Location.class).describeLocation(
              ld.getFinalWarehouse(), ld.getFinalAddress()) + " does not exist!");
          return;
        }
      }
      ld.setNextWarehouse(mpNextLocation.getWarehouseString());
      ld.setNextAddress(mpNextLocation.getAddressString().trim());
      if (ld.getNextAddress().trim().length() <= 0)
      {
        ld.setNextWarehouse("");
        ld.setNextAddress("");
      }
      else if (ld.getLoadMoveStatus() == DBConstants.NOMOVE
          && displayYesNoPrompt("Load move status has been set to "
              + DBTrans.getStringValue(LoadData.LOADMOVESTATUS_NAME, DBConstants.NOMOVE)
              + ". \nDo you want to clear the next location"))
      {
        ld.setNextWarehouse("");
        ld.setNextAddress("");
      }
      else
      {
        if (!mpLocServer.exists(ld.getNextWarehouse(), ld.getNextAddress()))
        {
          displayInfoAutoTimeOut(Factory.create(Location.class).describeLocation(
              ld.getNextWarehouse(), ld.getNextAddress()) + " does not exist!");
          return;
        }
      }
      ld.setWeight(mpDblWeight.getValue());
      int vnHeight = Integer.parseInt(mpHeightCombo.getSelectedItem().toString());
      if (vnHeight != lc.getHeight() &&
          displayYesNoPrompt("Load height \"" + vnHeight
            + "\" does not match the Location height \"" + lc.getHeight()
            + "\".\nDo you want to set it to match the Location"))
      {
        ld.setHeight(lc.getHeight());
      }
      else
      {
        ld.setHeight(vnHeight);
      }

      /*
       * If adding a load, return LoadData
       */
      if (adding)
      {
        mpLoadServer.addLoad(ld);
        changed(null, ld);
        displayInfoAutoTimeOut("Load " + vsLoadID + " added");
      }
      else
      {
        mpLoadServer.updateLoadInfo(ld);
        changed();
        displayInfoAutoTimeOut("Load " + vsLoadID + " updated");
      }

    }
    catch (DBException e2)
    {
      e2.printStackTrace(System.out);
      if (adding)
      {
        displayError("Error adding load " + vsLoadID);
      }
      else
      {
        displayError("Error updating load " + vsLoadID);
      }
    }
    catch (NoSuchFieldException e2)
    {
      displayError("No Such Field: " + e2);
    }

    if ((!adding) || (mzCloseOnSubmit))
    {
      close();
    }
    else
    {
      setData(defaultLoadData);
    }
  }

  /**
   *  Action method to handle Clear button.
   *
   *  @param e Action event.
   *
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(defaultLoadData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param loadData Load data to use in refreshing.
   *
   */
  protected void setData(LoadData loadData)
  {
//    try
//    {
//      if (loadData.getRouteID().trim().length() == 0)
//      {
//        loadData.setRouteID(loadData.getAddress());
//      }
//      if (loadData.getDeviceID().trim().length() == 0)
//      {
//        LocationData vpLocData = locServ.getLocationRecord(loadData.getWarehouse(), loadData.getAddress());
//        if (vpLocData != null)
//        {
//          loadData.setDeviceID(vpLocData.getDeviceID());
//        }
//      }
//    }
//    catch (DBException dbe) {}

    mpTxtLoadID.setText(loadData.getLoadID());
    mpDblWeight.setValue(loadData.getWeight());
    // Height
    mpHeightCombo.setSelectedItem(loadData.getHeight());
    if (loadData.getHeight() != Integer.parseInt(mpHeightCombo.getSelectedItem().toString()))
    {
      mpHeightList.add(loadData.getHeight());
      mpHeightCombo.setComboBoxData(mpHeightList);
      mpHeightCombo.setSelectedItem(loadData.getHeight());
      displayInfoAutoTimeOut("WARNING: Height " + loadData.getHeight()
          + " is not valid!");
    }
    mpComboContainer.setSelectedItem(loadData.getContainerType());
    mpComboZone.setSelectedItem(loadData.getRecommendedZone());
    mpComboRoute.setSelectedItem(loadData.getRouteID());
    mpComboDevice.setSelectedItem(loadData.getDeviceID());
    mpTxtLoadMsg.setText(loadData.getLoadMessage());
    mpTxtBCR.setText(loadData.getBCRData());
    mpLocation.reset(loadData.getWarehouse(),loadData.getAddress(),loadData.getShelfPosition());
    mpNextLocation.reset(loadData.getNextWarehouse(),loadData.getNextAddress(),loadData.getNextShelfPosition());
    mpFinalLocation.reset(loadData.getFinalWarehouse(),loadData.getFinalAddress());
    try
    {
      mpComboAmountFull.setSelectedElement(loadData.getAmountFull());
      mpComboLoadPresenceCheck.setSelectedElement(loadData.getLoadPresenceCheck());
      mpComboLoadMoveStatus.setSelectedElement(loadData.getLoadMoveStatus());
    }
    catch (NoSuchFieldException e2)
    {
      e2.printStackTrace(System.out);
      displayError("No Such Field: " + e2);
    }

  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   * Set the Zone Enabled flag based upon the system configuration and
   * fill the zone list if necesary
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