package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.zone.LocationZoneComboBox;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Description:<BR>
 *    Sets up the Location add internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author      Ed Askew.
 * @version      1.0
 * <BR>Created: 11-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class LocationMaintFrame extends DacInputFrame
{
  protected static final String LOCATION_MAINT = "iLocationMaint";
  
  private StandardLocationServer    mpLocationServer = null;

  private AddressPanel     mpBegAddressPanel;
  private AddressPanel     mpEndAddressPanel;
  private SKDCRadioButton  mpRackFormat = new SKDCRadioButton();

  private SKDCTranComboBox lcstatCombo = null;
  private SKDCTranComboBox empflgCombo = null;
  private SKDCTranComboBox mpTypeCombo = null;
  private SKDCComboBox warhseCombo = null;
  private SKDCComboBox deviceCombo = null;
  private SKDCTranComboBox zoneFuncCombo = null;
  private SKDCTranComboBox devFuncCombo = null;
  private SKDCTranComboBox aisleFuncCombo = null;
  private SKDCTranComboBox statusFuncCombo = null;
  private SKDCTranComboBox occupFuncCombo = null;
  private SKDCTranComboBox heightFuncCombo = null;
  private SKDCTranComboBox delFuncCombo = null;
  private SKDCTranComboBox mpTypeFuncCombo = null;

  private LocationZoneComboBox mpZoneCombo = null;
  private SKDCIntegerField txtAisleGroup = new SKDCIntegerField(DBConstants.LNAISLEGROUP);
  private SKDCIntegerField txtHeight     = new SKDCIntegerField(DBConstants.LNHEIGHT);
  private JCheckBox cboxAllowDel = new JCheckBox();
  private JCheckBox cboxBankBayTier = new JCheckBox();
  private String[] mpWarehouseList = null;
  private String[] mpDeviceList = null;
  
  private int zoneModifier = DBConstants.IGNORE;
  private int deviceModifier = DBConstants.IGNORE;
  private int aisleModifier = DBConstants.IGNORE;
  private int statusModifier = DBConstants.IGNORE;
  private int occupiedModifier = DBConstants.IGNORE;
  private int heightModifier = DBConstants.IGNORE;
  private int deleteModifier = DBConstants.IGNORE;
  private int typeModifier = DBConstants.IGNORE;
  protected int mnSwapModifier = DBConstants.IGNORE; // For double deep

  private SKDCButton mpBtnRange;
  
  public LocationMaintFrame(StandardLocationServer ipLocServer,
      String[] iasWarehouseList, String[] iasDeviceList, String isWarehouse)
      throws NumberFormatException
  {
    super("Location Maintenance", "Location Information");

    mpLocationServer = ipLocServer;
    mpWarehouseList = iasWarehouseList;
    mpDeviceList = iasDeviceList;

    try
    {
      buildScreen();        // Set up all elements of Label column
    }
    catch (NoSuchFieldException err)
    {
      logAndDisplayException(err);
    }
    
    warhseCombo.selectItemBy(isWarehouse);
  }

/*===========================================================================
              Methods for display formatting go in this section.
===========================================================================*/
  /**
   * Build the Screen
   * @return
   * @throws NoSuchFieldException
   */
  private void buildScreen() throws NoSuchFieldException
  {
    mpBegAddressPanel = new AddressPanel();
    mpRackFormat.setSelected(true);
    mpRackFormat.setToolTipText("Switch location format");
    mpRackFormat.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if ((e.getStateChange() == ItemEvent.SELECTED) ||
            (e.getStateChange() == ItemEvent.DESELECTED))
        {
          setRackFormat(e.getStateChange() == ItemEvent.SELECTED);
        }
      }
    });
    mpBegAddressPanel.add(mpRackFormat);

    mpEndAddressPanel = new AddressPanel();
    mpBtnRange = new SKDCButton("Get Range");
    mpBtnRange.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          getLocationRange();
        }
      });
    mpEndAddressPanel.add(Box.createHorizontalStrut(10));
    mpEndAddressPanel.add(mpBtnRange);
    warhseCombo = new SKDCComboBox(mpWarehouseList);
    cboxBankBayTier.setSelected(true);

    addInput("Warehouse:", warhseCombo);
    addInput("Beginning Address:", mpBegAddressPanel);
    addInput("Ending Address:", mpEndAddressPanel);
    addInput("Bound by Bank Bay Tier:", cboxBankBayTier);
    addInput("", zonePanel());
    addInput("", devicePanel());
    addInput("", aisleGroupPanel());
    addInput("", locationStatusPanel());
    addInput("", emptyFlagStatusPanel());
    addInput("", heightPanel());
    addInput("", locationTypePanel());
    addInput("", allowDeletionPanel());
  }

  /**
   * Get the range of locations for a given warehouse
   */
  private void getLocationRange()
  {
    try
    {
      String[] vasMinMax = mpLocationServer.getAddressMinMaxByWarehouse(
          warhseCombo.getText());
      if (vasMinMax != null)
      {
        mpBegAddressPanel.setAddress(vasMinMax[0]);
        mpEndAddressPanel.setAddress(vasMinMax[1]);
      }
      else
      {
        displayWarning("No locations found for warehouse " 
            + warhseCombo.getText());
      }
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
    }
  }

  /**
   * Build the Zone panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel zonePanel() throws NoSuchFieldException
  {
    JPanel vpZonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    zoneFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    vpZonePanel.add(zoneFuncCombo);
    vpZonePanel.add(new SKDCLabel(" Zone = "));
    mpZoneCombo = new LocationZoneComboBox("");
    vpZonePanel.add(mpZoneCombo);
    vpZonePanel.setVisible(mpLocationServer.hasLocationZonesDefined());
    return(vpZonePanel);
  }

  /**
   * Build the Device panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel devicePanel() throws NoSuchFieldException
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    devFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    addrPanel.add(devFuncCombo);
    addrPanel.add(new SKDCLabel(" Device = "));
    deviceCombo = new SKDCComboBox(mpDeviceList);
    addrPanel.add(deviceCombo);
    return(addrPanel);
  }

  /**
   * Build the Aisle Group panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel aisleGroupPanel() throws NoSuchFieldException
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    aisleFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    addrPanel.add(aisleFuncCombo);
    addrPanel.add(new SKDCLabel(" Aisle Group = "));
    addrPanel.add(txtAisleGroup);
    return(addrPanel);
  }

  /**
   * Build the Location Status panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel locationStatusPanel() throws NoSuchFieldException
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    statusFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    addrPanel.add(statusFuncCombo);
    addrPanel.add(new SKDCLabel(" Status = "));
    lcstatCombo = new SKDCTranComboBox(LocationData.LOCATIONSTATUS_NAME);
    addrPanel.add(lcstatCombo);
    return(addrPanel);
  }

  /**
   * Build the Location Type panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel locationTypePanel() throws NoSuchFieldException
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    mpTypeFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    addrPanel.add(mpTypeFuncCombo);
    addrPanel.add(new SKDCLabel(" Type = "));
    mpTypeCombo = new SKDCTranComboBox(LocationData.LOCATIONTYPE_NAME);
    addrPanel.add(mpTypeCombo);
    return(addrPanel);
  }

  /**
   * Build the Empty Flag panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel emptyFlagStatusPanel() throws NoSuchFieldException
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    occupFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    addrPanel.add(occupFuncCombo);
    addrPanel.add(new SKDCLabel(" Occupied Status = "));
    empflgCombo = new SKDCTranComboBox(LocationData.EMPTYFLAG_NAME,
                                         DBConstants.UNOCCUPIED);
    addrPanel.add(empflgCombo);
    return(addrPanel);
  }

  /**
   * Build the Device panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel heightPanel() throws NoSuchFieldException
  {
    JPanel addrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    heightFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    addrPanel.add(heightFuncCombo);
    addrPanel.add(new SKDCLabel(" Height = "));
    addrPanel.add(txtHeight);     // Put in Height Field.
    return(addrPanel);
  }

  /**
   * Build the Allow Deletion panel (Equals/Ignore/Set)
   * @return
   * @throws NoSuchFieldException
   */
  private JPanel allowDeletionPanel() throws NoSuchFieldException
  {
    JPanel vpAllowDeletePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
    delFuncCombo = new SKDCTranComboBox(LOCATION_MAINT, DBConstants.IGNORE);
    vpAllowDeletePanel.add(delFuncCombo);
    vpAllowDeletePanel.add(new SKDCLabel(" Allow Deletion = "));
    vpAllowDeletePanel.add(cboxAllowDel);  // Put in Allow Deletion Check Box.
    return(vpAllowDeletePanel);
  }

  /**
   * Are we setting anything?
   * @return
   */
  protected boolean getAndCheckModifiers()
  {
    try
    {
      zoneModifier = zoneFuncCombo.getIntegerValue();
      deviceModifier = devFuncCombo.getIntegerValue();
      aisleModifier = aisleFuncCombo.getIntegerValue();
      statusModifier = statusFuncCombo.getIntegerValue();
      occupiedModifier = occupFuncCombo.getIntegerValue();
      heightModifier = heightFuncCombo.getIntegerValue();
      deleteModifier = delFuncCombo.getIntegerValue();
      typeModifier = mpTypeFuncCombo.getIntegerValue();
    }
    catch (NoSuchFieldException e)
    {
      logAndDisplayException(e);
    }

    return (zoneModifier == DBConstants.SET ||
            deviceModifier == DBConstants.SET ||
            aisleModifier == DBConstants.SET ||
            statusModifier == DBConstants.SET ||
            occupiedModifier == DBConstants.SET ||
            heightModifier == DBConstants.SET ||
            deleteModifier == DBConstants.SET ||
            typeModifier == DBConstants.SET);
  }

  /**
   * Is the location range valid?
   * @return
   */
  private boolean checkForValidLocRange()
  {
    String vsBegAddress = mpBegAddressPanel.getAddress();
    String vsEndAddress = mpEndAddressPanel.getAddress();
    
    if (!mpRackFormat.isSelected())
    {
      int i = 0;
      char[] vpBegAddress = vsBegAddress.toCharArray();
      char[] vpEndAddress = vsEndAddress.toCharArray();
      int vnBegAddress = 0;
      int vnEndAddress = 0;

      if (vsBegAddress.equals(vsEndAddress))
      {
        return(true);
      }

      while (i < vpBegAddress.length && i < vpEndAddress.length &&
          vpBegAddress[i] == vpEndAddress[i])
      {
        i++;
      }
      while (i>0 && Character.isDigit(vpBegAddress[i-1]))
      {
        i--;
      }
      
      if (vsBegAddress.trim().length() == 0)
      {
        displayInfoAutoTimeOut("Beginning address cannot be blank.");
        return false;
      }
      
      if (vsEndAddress.trim().length() == 0)
      {
        displayInfoAutoTimeOut("Ending address cannot be blank.");
        return false;
      }
      
      try
      {
        vnBegAddress = Integer.parseInt(vsBegAddress.substring(i));
        vnEndAddress = Integer.parseInt(vsEndAddress.substring(i));
      }
      catch (NumberFormatException nfe) {}
      
      if (vnBegAddress == 0 || vnEndAddress == 0)
      {
        displayError("Location Range is Invalid - Beginning and Ending Differing parts must be non-zero!");
        return false;
      }
      else if (vnEndAddress < vnBegAddress)
      {
        displayError("Location Range is Invalid - Beginning Address Larger than Ending Address.");
        return false;
      }
      return true;
    }
    else if(cboxBankBayTier.isSelected())
    {     // were doing bay bank tier
      String vsBegBank    = mpBegAddressPanel.getBank();
      String vsBegBay     = mpBegAddressPanel.getBay();
      String vsBegTier    = mpBegAddressPanel.getTier();
      String vsEndBank    = mpEndAddressPanel.getBank();
      String vsEndBay     = mpEndAddressPanel.getBay();
      String vsEndTier    = mpEndAddressPanel.getTier();
      
      int compareValue;
      compareValue = vsEndBank.compareTo(vsBegBank);
      if(compareValue < 0)
      {   // the ending bank is less than the Beginning bank error
        displayError( "Location Range is Invalid - Beginning Bank Larger than Ending Bank");
        return false;
      }
      else if(compareValue > 0)
      {   // the ending bank is greater than the Beginning bank so rest don't matter
        return true;
      }
      else
      {
        compareValue = vsEndBay.compareTo(vsBegBay);
        if(compareValue < 0)
        {   // the ending bay is less than the Beginning bay error
          displayError( "Location Range is Invalid - Beginning Bay Larger than Ending Bay");
          return false;
        }
        else if(compareValue > 0)
        {   // the ending bay is greater than the Beginning bay so rest don't matter
          return true;
        }
        else
        {
          compareValue = vsEndTier.compareTo(vsBegTier);
          if(compareValue < 0)
          {   // the ending tier is less than the Beginning tier error
            displayError( "Location Range is Invalid - Beginning Tier Larger than Ending Tier");
             return false;
          }
          else
          {   // the ending tier is greater than the Beginning tier so rest don't matter
            return true;
          }
        }
      }
    }
    else
    {       // only modify the tier
      String vsBegBank    = mpBegAddressPanel.getBank();
      String vsBegBay     = mpBegAddressPanel.getBay();
      String vsBegTier    = mpBegAddressPanel.getTier();
      String vsEndBank    = mpEndAddressPanel.getBank();
      String vsEndBay     = mpEndAddressPanel.getBay();
      String vsEndTier    = mpEndAddressPanel.getTier();

      int compareValue;
      compareValue = vsEndTier.compareTo(vsBegTier);
      if( compareValue != 0)
      {     // the tiers must be the same
        displayError( "Location Range is Invalid - Tiers must be the same if not interpreted as Bank-Bay-Tier");
        return false;
      }
      compareValue = vsEndBank.compareTo(vsBegBank);
      if(compareValue < 0)
      {   // the ending bank is less than the Beginning bank error
        displayError( "Location Range is Invalid - Beginning Bank Larger than Ending Bank");
        return false;
      }
      else if(compareValue > 0)
      {   // the ending bank is greater than the Beginning bank so rest don't matter
        return true;
      }
      else
      {
        compareValue = vsEndBay.compareTo(vsBegBay);
        if(compareValue < 0)
        {   // the ending bay is less than the Beginning bay error
          displayError( "Location Range is Invalid - Beginning Bay Larger than Ending Bay");
          return false;
        }
        else if(compareValue > 0)
        {   // the ending bay is greater than the Beginning bay so rest don't matter
          return true;
        }
        else
        { // we are changing only one location
            return true;
        }
      }
    }
  }

/*===========================================================================
              Methods for event handling go in this section.
===========================================================================*/
  /**
   * Processes Location maintenance request.
   */
  @Override
  protected void okButtonPressed()
  {
    /*
     * Build the location data
     */
    LocationData vpLocChanges = getLocationDataFromScreenData();
    if (vpLocChanges == null)
    {
      return;
    }

    /*
     * Set modifier fields
     */
    boolean vzBankBayTier = cboxBankBayTier.isSelected()
        && mpRackFormat.isSelected();
    if (getAndCheckModifiers() == false)
    {
      displayWarning("Nothing set to modify");
      return;
    }
    
    // Call on the location server to modify set of locations
    try
    {
      int info = mpLocationServer.updateLocationsData(vpLocChanges,
          vzBankBayTier, zoneModifier, deviceModifier, aisleModifier,
          statusModifier, occupiedModifier, heightModifier, deleteModifier,
          typeModifier, mnSwapModifier);
      displayInfoAutoTimeOut(info + " Locations Changed");
      changed();                       // Fire frame (data) change event.
      close();
    }
    catch (Exception exc)
    {
      logAndDisplayException(exc);
    }
  }

  /**
   * Build a LocationData based upon screen input
   * @return
   */
  protected LocationData getLocationDataFromScreenData()
  {
    if (checkForValidLocRange() == false)
    {
      return null;
    }
    
    LocationData vpLocChanges = Factory.create(LocationData.class);

    vpLocChanges.setWarehouse(warhseCombo.getSelectedItem().toString());
    vpLocChanges.setAddress(mpBegAddressPanel.getAddress());
    vpLocChanges.setEndingAddress(mpEndAddressPanel.getAddress());

    try
    {
      vpLocChanges.setLocationStatus(lcstatCombo.getIntegerValue());
      vpLocChanges.setEmptyFlag(empflgCombo.getIntegerValue());
      vpLocChanges.setLocationType(mpTypeCombo.getIntegerValue());
    }
    catch (NoSuchFieldException e)
    {
      logAndDisplayException(e);
      return null;
    }
    vpLocChanges.setZone(mpZoneCombo.getSelectedItem().toString());
    vpLocChanges.setDeviceID(deviceCombo.getSelectedItem().toString());
    if (txtAisleGroup.getText().trim().length() != 0)
    {
      vpLocChanges.setAisleGroup(txtAisleGroup.getValue());
    }
    if (txtHeight.getText().trim().length() != 0)
    {
      vpLocChanges.setHeight(txtHeight.getValue());
    }

    vpLocChanges.setAllowDeletion(cboxAllowDel.isSelected() ? 
        DBConstants.YES : DBConstants.NO);

    return vpLocChanges;
  }
  
  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Location dialog.
   */
  @Override
  protected void clearButtonPressed()
  {                                    // Clear out the text box.
    warhseCombo.setSelectedIndex(0);
    mpBegAddressPanel.reset();
    mpEndAddressPanel.reset();
    mpZoneCombo.setSelectedIndex(0);
    txtHeight.setText("");
    deviceCombo.setSelectedIndex(0);
    txtAisleGroup.setText("");
                                       // Uncheck the check box.
    cboxAllowDel.setSelected(false);
    cboxBankBayTier.setSelected(true);
    warhseCombo.requestFocus();
                                       // Reset Combo-Boxes to orig. selection
    lcstatCombo.resetDefaultSelection();
    empflgCombo.resetDefaultSelection();
    devFuncCombo.resetDefaultSelection();
    zoneFuncCombo.resetDefaultSelection();
    aisleFuncCombo.resetDefaultSelection();
    statusFuncCombo.resetDefaultSelection();
    occupFuncCombo.resetDefaultSelection();
    heightFuncCombo.resetDefaultSelection();
    delFuncCombo.resetDefaultSelection();
    mpTypeFuncCombo.resetDefaultSelection();
    mpTypeCombo.resetDefaultSelection();
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Method to switch between rack and non-rack location format.
   *
   *  @param izRack True if setting to rack format.
   */
  void setRackFormat(boolean izRack)
  {
    mpRackFormat.setSelected(izRack);
    mpBegAddressPanel.setRackMode(izRack);
    mpEndAddressPanel.setRackMode(izRack);
  }
}
