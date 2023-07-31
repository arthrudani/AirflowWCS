package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.zone.LocationZoneComboBox;
import javax.swing.JCheckBox;

/**
 * Description:<BR>
 *    Sets up the Location Modify internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 11-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class ModifyLocationFrame extends DacInputFrame
{
  protected LocationData mpCurrentLocData;
  protected StandardLocationServer mpLocServer;
  
  protected LocationPanel mpLocation;
  protected SKDCTranComboBox mpLocStatusCombo;
  protected SKDCTranComboBox mpEmptyFlagCombo;
  protected SKDCTranComboBox mpTypeCombo;
  protected LocationZoneComboBox mpZoneCombo;
  protected SKDCComboBox mpDeviceCombo;
  protected SKDCIntegerField txtAisleGroup;
  protected SKDCIntegerField txtHeight;
  protected JCheckBox cboxAllowDel;
  
  protected String[] mpDeviceList;
  
  public ModifyLocationFrame(StandardLocationServer ipLocServer, String[] ipDeviceList)
  {
    super("Modify Location", "Location Information");
    mpLocServer = ipLocServer;
    mpDeviceList = ipDeviceList;
    
    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }
  }

  /**
   * Get the valid status option (different for single-deep vs double-deep)
   * @return
   */
  protected int[] getEmptyFlagOptions()
  {
    return new int[] { DBConstants.UNOCCUPIED, DBConstants.OCCUPIED,
        DBConstants.LCRESERVED };
  }
  
  /**
   * Load the modify screen with the current selected row of data from the
   * table. It is assumed that this frame has already been built when this
   * method is called.
   */
  public void setCurrentData(LocationData lcdata)
  {
    mpCurrentLocData = (LocationData)lcdata.clone();
    try
    {
      mpLocStatusCombo.setSelectedElement(lcdata.getLocationStatus());
      if (lcdata.getLocationStatus() == DBConstants.LCPROHIBIT)
      {
        if (mpLocStatusCombo.getIntegerValue() != lcdata.getLocationStatus())
        {
          mpLocStatusCombo.addItem(DBTrans.getStringValue(
              LocationData.LOCATIONSTATUS_NAME, DBConstants.LCPROHIBIT));
          mpLocStatusCombo.setSelectedElement(lcdata.getLocationStatus());
        }
        mpLocStatusCombo.setEnabled(SKDCUserData.isSuperUser()
            || SKDCUserData.isAdministrator());
      }
      else
      {
        mpLocStatusCombo.setEnabled(true);
      }
      mpEmptyFlagCombo.setSelectedElement(lcdata.getEmptyFlag());
      mpTypeCombo.setSelectedElement(lcdata.getLocationType());
    }
    catch(NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }
    // Insert the data to the appropriate fields
    mpLocation.reset(mpCurrentLocData.getWarehouse(),
        mpCurrentLocData.getAddress());
    mpZoneCombo.setSelectedItem(mpCurrentLocData.getZone());
    mpDeviceCombo.setSelectedItem(mpCurrentLocData.getDeviceID());
    txtAisleGroup.setValue(mpCurrentLocData.getAisleGroup());
    txtHeight.setValue(mpCurrentLocData.getHeight());
    
    // Check or Uncheck the check box.
    cboxAllowDel.setSelected(mpCurrentLocData.getAllowDeletion() == DBConstants.YES);
  }

  /**
   * Build the screen
   * @throws NoSuchFieldException
   */
  protected void buildScreen() throws NoSuchFieldException
  {
    mpLocation = Factory.create(LocationPanel.class);
//    mpLocation.setFormatVisible(false);
    mpLocation.setEnabled(true);
    mpZoneCombo = new LocationZoneComboBox("");
    mpDeviceCombo = new SKDCComboBox(mpDeviceList);
    if (SKDCUserData.isSuperUser() || SKDCUserData.isAdministrator())
    {
      mpLocStatusCombo = new SKDCTranComboBox(LocationData.LOCATIONSTATUS_NAME);
    }
    else
    {
      // Don't let non-super-users set locations to PROHIBITED, because they
      // wont be able to change them back.
      mpLocStatusCombo = new SKDCTranComboBox(LocationData.LOCATIONSTATUS_NAME,
          new int[] { DBConstants.LCAVAIL, DBConstants.LCUNAVAIL }, false);
    }
    mpEmptyFlagCombo = new SKDCTranComboBox(LocationData.EMPTYFLAG_NAME,
        getEmptyFlagOptions(), false);
    mpTypeCombo = new SKDCTranComboBox(LocationData.LOCATIONTYPE_NAME);
    txtAisleGroup = new SKDCIntegerField(DBConstants.LNAISLEGROUP);
    txtHeight = new SKDCIntegerField(DBConstants.LNHEIGHT);
    cboxAllowDel = new JCheckBox();

    addInput("Warehouse-Address:", mpLocation);
    addInput("Status:", mpLocStatusCombo);
    addInput("Availability:", mpEmptyFlagCombo);
    addInput("Type:", mpTypeCombo);
    addInput("Zone:", mpZoneCombo);
    addInput("Device:", mpDeviceCombo);
    addInput("Aisle Group:", txtAisleGroup);
    addInput("Height:", txtHeight);
    addInput("Allow Deletion:", cboxAllowDel);

    setInputVisible(mpZoneCombo, mpLocServer.hasLocationZonesDefined());
    
    useModifyButtons();
  }
  
  
/*===========================================================================
              Methods for event handling go in this section.
  ===========================================================================*/
  /**
   *  Gathers data off the screen and builds Location Data structure.
   */
  @Override
  protected void okButtonPressed()
  {
    LocationData newLCData = getLocationDataFromScreenData();
    if (newLCData == null)
    {
      return;
    }
    
    try
    {
      if (newLCData.getEmptyFlag()== DBConstants.UNOCCUPIED)
      {  
        StandardLoadServer vpLoadServer = Factory.create(StandardLoadServer.class);
        if (vpLoadServer.getOldestLoad(newLCData.getWarehouse(),
            newLCData.getAddress()) != null)
        {
          displayError("Location has load; delete load before setting location to empty.");
          return;
        }
      } 
      // Submit request to Location server.
      String mod_mesg = mpLocServer.modifyLocation(newLCData);
      displayInfoAutoTimeOut(mod_mesg, "Row Modified");
      changed(null, newLCData);
      modifyCompleted();
    }
    catch (Exception e)
    {
      logAndDisplayException(e);
    }
  }

  /**
   * Do this after the modify is complete
   */
  protected void modifyCompleted()
  {
    try
    {
      // If this isn't here, sometimes the data returned from changed() is lost.
      Thread.sleep(30);
    }
    catch (InterruptedException e) {}
    close();
  }

  /**
   * Build a LocationData based upon screen input
   * @return
   */
  protected LocationData getLocationDataFromScreenData()
  {
    LocationData vpNewLCData = Factory.create(LocationData.class);
    
    try
    {
      vpNewLCData.setWarehouse(mpLocation.getWarehouseString());  // Set the Warehouse.
      vpNewLCData.setAddress(mpLocation.getAddressString());    // Set the address.
    }
    catch (DBException dbe)
    {
      logAndDisplayException(dbe);
      return null;
    }

    try
    {
      vpNewLCData.setLocationStatus(mpLocStatusCombo.getIntegerValue());
      vpNewLCData.setEmptyFlag(mpEmptyFlagCombo.getIntegerValue());
      vpNewLCData.setLocationType(mpTypeCombo.getIntegerValue());
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }

    vpNewLCData.setZone(mpZoneCombo.getSelectedItem().toString());
    vpNewLCData.setDeviceID(mpDeviceCombo.getSelectedItem().toString());
    vpNewLCData.setAisleGroup(txtAisleGroup.getValue());
    vpNewLCData.setHeight(txtHeight.getValue());

    int allowdel = (cboxAllowDel.isSelected()) ? DBConstants.YES : DBConstants.NO;
    vpNewLCData.setAllowDeletion(allowdel);

    // Key info
    vpNewLCData.setKey(LocationData.WAREHOUSE_NAME, vpNewLCData.getWarehouse());
    vpNewLCData.setKey(LocationData.ADDRESS_NAME, vpNewLCData.getAddress());
    
    return vpNewLCData;
  }
  
  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Location dialog.
   */
  @Override
  protected void clearButtonPressed()
  {
    setCurrentData(mpCurrentLocData);
    mpLocStatusCombo.requestFocus();
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}
