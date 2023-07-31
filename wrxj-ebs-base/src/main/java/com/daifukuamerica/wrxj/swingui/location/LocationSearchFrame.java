package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.zone.LocationZoneComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 *    Sets up the Location Search internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 31-Jan-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class LocationSearchFrame extends DacInputFrame
{
  protected StandardLocationServer mpLocServer;
  
  protected LocationPanel        mpLocationPanel;
  protected SKDCComboBox         mpDeviceCombo;
  protected SKDCTranComboBox     mpTypeCombo;
  protected SKDCTranComboBox     mpStatusCombo;
  protected SKDCTranComboBox     mpEmptyFlagCombo;
  protected SKDCComboBox         mpHeightCombo;
  protected LocationZoneComboBox mpZoneCombo;

  /**
   * Constructor
   * 
   * @param ipLocServer
   * @param iasWarehouses
   * @param iasDevices
   */
  public LocationSearchFrame(StandardLocationServer ipLocServer,
      String[] iasWarehouses, String[] iasDevices)
  {
    super("Location Search", "Location Information");
    mpLocServer = ipLocServer;
    
    try
    {
      buildScreen(iasWarehouses, iasDevices);
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
  }

  /**
   * Builds the screen
   * 
   * @param iasWarehouses
   * @param iasDevices
   * @throws NoSuchFieldException
   */
  protected void buildScreen(String[] iasWarehouses, String[] iasDevices)
      throws NoSuchFieldException
  {
    mpLocationPanel = Factory.create(LocationPanel.class);
    mpLocationPanel.setWarehouseList(iasWarehouses);
    mpDeviceCombo = new SKDCComboBox(iasDevices);
    // Limit this list to the types eWareNavi will let us have
    mpTypeCombo = new SKDCTranComboBox(LocationData.LOCATIONTYPE_NAME, 
        new int[] {DBConstants.LCASRS, DBConstants.LCDEVICE, DBConstants.LCSTATION},
        true);
    mpStatusCombo = new SKDCTranComboBox(LocationData.LOCATIONSTATUS_NAME, true);
    // Limit this list the types single-deep supports
    mpEmptyFlagCombo = new SKDCTranComboBox(LocationData.EMPTYFLAG_NAME, 
        new int[] {DBConstants.UNOCCUPIED, DBConstants.OCCUPIED, DBConstants.LCRESERVED},
        true);
    // Limit this list to heights that are actually in the system
    Integer[] vapHeights = { 0, 1, 2, 3 };
    try
    {
      vapHeights = Factory.create(StandardLocationServer.class).getLocationHeights();
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
    mpHeightCombo = new SKDCComboBox(vapHeights, true);
    mpZoneCombo = new LocationZoneComboBox(SKDCConstants.ALL_STRING);

    addInput("Search Location:", mpLocationPanel);
    addInput("Device ID:", mpDeviceCombo);
    addInput("Location Type:", mpTypeCombo);
    addInput("Location Status:", mpStatusCombo);
    addInput("Availability:", mpEmptyFlagCombo);
    addInput("Height:", mpHeightCombo);
    addInput("Zone:", mpZoneCombo);
    
    // Hide the zone combo if there aren't any zones
    setInputVisible(mpZoneCombo, mpLocServer.hasLocationZonesDefined());

    useSearchButtons();
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpLocationPanel.reset();
    mpTypeCombo.resetDefaultSelection();
    mpStatusCombo.resetDefaultSelection();
    mpEmptyFlagCombo.resetDefaultSelection();
    mpHeightCombo.resetDefaultSelection();
  }
  
  /**
   * Processes Search Location request.
   */
  @Override
  protected void okButtonPressed()
  {
    LocationData vpSearchKey = Factory.create(LocationData.class);

    if (!mpLocationPanel.getWarehouseString().equals(SKDCConstants.ALL_STRING))
    {
      // Store Warehouse entry. Warehouse is a mandatory field.
      vpSearchKey.setKey(LocationData.WAREHOUSE_NAME, mpLocationPanel.getWarehouseString());
    }

    // Store Address entry from Bank, Bay, and Tier.
    String sAddress = "";              
    try
    {
      sAddress = mpLocationPanel.getAddressString();
    }
    catch(DBException e)
    {
      displayError("Error retrieving LocationAddres string", "Data Error");
      return;
    }
    if (sAddress.trim().length() != 0)
    {
      vpSearchKey.setKey(LocationData.ADDRESS_NAME, sAddress, KeyObject.LIKE);
    }

    // Device
    if (!mpDeviceCombo.getSelectedItem().toString().equals(SKDCConstants.ALL_STRING))
    {
      vpSearchKey.setKey(LocationData.DEVICEID_NAME,
          mpDeviceCombo.getSelectedItem().toString());
    }

    try
    {
      // Set up Location Type key.
      vpSearchKey.setKey(LocationData.LOCATIONTYPE_NAME,
          mpTypeCombo.getIntegerObject());
      
      // Set up Location Status key.
      vpSearchKey.setKey(LocationData.LOCATIONSTATUS_NAME,
          mpStatusCombo.getIntegerObject());

      // Store Empty Flag entry.
      vpSearchKey.setKey(LocationData.EMPTYFLAG_NAME,
          mpEmptyFlagCombo.getIntegerObject());
    }
    catch(NoSuchFieldException e)
    {
      displayWarning(e.getMessage(), "Translation Error");
      return;
    }
    
    // Location Height entry.
    if (!mpHeightCombo.getSelectedItem().equals(SKDCConstants.ALL_STRING))
    {
      vpSearchKey.setKey(LocationData.HEIGHT_NAME, mpHeightCombo.getSelectedItem());
    }
    
    // Location Zone entry
    if (!mpZoneCombo.getSelectedItem().equals(SKDCConstants.ALL_STRING))
    {
      vpSearchKey.setKey(LocationData.ZONE_NAME, mpZoneCombo.getSelectedItem());
    }
    
    // Order by
    vpSearchKey.addOrderByColumn(LocationData.WAREHOUSE_NAME);
    vpSearchKey.addOrderByColumn(LocationData.ADDRESS_NAME);

    changed(null, vpSearchKey);
    close();
  }
}
