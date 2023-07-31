package com.daifukuamerica.wrxj.swingui.location;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCRadioButton;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.zone.LocationZoneComboBox;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;

/**
 * Description:<BR>
 * Sets up the Location add internal frame. It fills in the contents of a JPanel
 * before adding it to the Internal frame. This method then returns the Internal
 * Frame reference to the caller so that it can be added to the desktop when
 * appropriate.
 *
 * @author A.D.
 * @version 1.0 <BR>
 *          Created: 11-Feb-02<BR>
 *          Copyright (c) 2002<BR>
 *          Company: SKDC Corporation
 */
@SuppressWarnings("serial")
public class AddLocationFrame extends DacInputFrame
{
  protected StandardLocationServer mpLocServer;

  protected SKDCComboBox mpComboWarehouse;
  protected AddressPanel mpBegAddressPanel;
  protected AddressPanel mpEndAddressPanel;
  protected SKDCRadioButton mpRackFormat;
  protected SKDCTranComboBox mpComboType;
  protected LocationZoneComboBox mpComboZone;
  protected SKDCComboBox mpComboDevice;
  protected SKDCIntegerField mpTxtAisleGroup;
  protected SKDCTranComboBox mpComboStatus;
  protected SKDCTranComboBox mpComboEmptyFlag;
  protected SKDCIntegerField mpTxtHeight;
  protected JCheckBox mpCheckAllowDel;

  protected String[] masWarehouseList;
  protected String[] masDeviceList;

  public AddLocationFrame(StandardLocationServer ipLocServer, String[] iasWarehouses,
                          String[] iasDevices) throws NumberFormatException
  {
    super("Add Location", "Location Information");

    mpLocServer = ipLocServer;
    masWarehouseList = iasWarehouses;
    masDeviceList = iasDevices;

    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }
  }


/*===========================================================================
              Methods for display formatting go in this section.
===========================================================================*/
  private void buildScreen() throws NoSuchFieldException
  {
    mpComboWarehouse = new SKDCComboBox(masWarehouseList);

    mpBegAddressPanel = new AddressPanel();
    mpRackFormat = new SKDCRadioButton();
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
//    mpBegAddressPanel.add(mpRackFormat);

    mpEndAddressPanel = new AddressPanel();
    mpBegAddressPanel.setRackMode(false);
    mpEndAddressPanel.setRackMode(false);
    mpComboType  = new SKDCTranComboBox(LocationData.LOCATIONTYPE_NAME);
    mpComboZone  = new LocationZoneComboBox("");
    mpComboDevice = new SKDCComboBox(masDeviceList);
    mpTxtAisleGroup = new SKDCIntegerField(DBInfo.getFieldLength(LocationData.AISLEGROUP_NAME));
    mpComboStatus = new SKDCTranComboBox(LocationData.LOCATIONSTATUS_NAME);
    mpComboEmptyFlag = new SKDCTranComboBox(LocationData.EMPTYFLAG_NAME, DBConstants.UNOCCUPIED);
    mpTxtHeight = new SKDCIntegerField(DBInfo.getFieldLength(LocationData.HEIGHT_NAME));
    mpCheckAllowDel = new JCheckBox();

    /*
     * Put the fields on the screen
     */
    addInput("Warehouse:", mpComboWarehouse);
    addInput("Beginning Address:", mpBegAddressPanel);
    addInput("Ending Address:", mpEndAddressPanel);
    addInput("Type:", mpComboType);
    addInput("Zone:", mpComboZone);
    addInput("Device:", mpComboDevice);
    addInput("Aisle Group:", mpTxtAisleGroup);
    addInput("Status:", mpComboStatus);
    addInput("Availability:", mpComboEmptyFlag);
    addInput("Height:", mpTxtHeight);
    addInput("Allow Deletion:", mpCheckAllowDel);

    // Hide the zone combo if there aren't any zones
    setInputVisible(mpComboZone, mpLocServer.hasLocationZonesDefined());

    useAddButtons();
  }


/*===========================================================================
              Methods for event handling go in this section.
===========================================================================*/
  /**
   *  Processes Location add request.  This method stuffs column objects into
   *  a LocationData instance container.
   */
  @Override
  protected void okButtonPressed()
  {
    LocationData lcdata = getLocationDataFromScreenData();
    if (lcdata == null)
    {
      return;
    }

    // Save off Location Add data.
    LocationData addData = (LocationData)lcdata.clone();

    // Call on the location server to add set of locations
    try
    {
      String info = "";
      if (mpRackFormat.isSelected())
      {
        info = mpLocServer.addRackLocations(lcdata);
      }
      else
      {
        info = mpLocServer.addLocations(lcdata);
      }
      displayInfoAutoTimeOut(info, "Rows Added");
      changed(null, addData);
      addCompleted();
    }
    catch(Exception exc)
    {
      logAndDisplayException(exc);
    }
  }

  /**
   * Do this after the add is complete
   */
  protected void addCompleted()
  {
    try
    {
      // If this isn't here, sometimes the data returned from changed() is lost.
      Thread.sleep(30);
    }
    catch (InterruptedException e) {}
//    close();
  }

  /**
   * Build a LocationData based upon screen input
   * @return
   */
  protected LocationData getLocationDataFromScreenData()
  {
    LocationData vpAddLocData = Factory.create(LocationData.class);

                                       // Get the beginning Address.
    String vsBeginAddr = mpBegAddressPanel.getAddress();
    String vsEndAddr   = mpEndAddressPanel.getAddress();
    if(vsBeginAddr.length() < 1 || vsEndAddr.length() < 1)
    {
      displayWarning("The Address Cannot be blank.");
      mpBegAddressPanel.requestFocus();
      return null;
    }

    vpAddLocData.setWarehouse(mpComboWarehouse.getSelectedItem().toString());
    vpAddLocData.setAddress(vsBeginAddr);
    vpAddLocData.setEndingAddress(vsEndAddr);

    try
    {
      vpAddLocData.setLocationStatus(mpComboStatus.getIntegerValue());
      vpAddLocData.setLocationType(mpComboType.getIntegerValue());
      vpAddLocData.setEmptyFlag(mpComboEmptyFlag.getIntegerValue());
    }
    catch(NoSuchFieldException e)
    {
      logAndDisplayException(e);
      return null;
    }

    // Set the zone.
    vpAddLocData.setZone(mpComboZone.getSelectedItem().toString().trim());

    // Put in the Device ID selection
    vpAddLocData.setDeviceID(mpComboDevice.getSelectedItem().toString());

    // Set the Aisle Group
    if (mpTxtAisleGroup.getText().trim().length() != 0)
    {
      vpAddLocData.setAisleGroup(mpTxtAisleGroup.getValue());
    }
    // Set the Height.
    if (mpTxtHeight.getText().trim().length() != 0)
    {
      vpAddLocData.setHeight(mpTxtHeight.getValue());
    }

    vpAddLocData.setAllowDeletion(mpCheckAllowDel.isSelected() ?
        DBConstants.YES : DBConstants.NO);

    return vpAddLocData;
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Location dialog.
   */
  @Override
  protected void clearButtonPressed()
  {                                    // Clear out the text box.
    mpComboWarehouse.setSelectedIndex(0);
    mpBegAddressPanel.reset();
    mpEndAddressPanel.reset();
    mpComboZone.setSelectedItem("");
    mpTxtHeight.setText("");
    mpComboDevice.setSelectedIndex(0);
    mpTxtAisleGroup.setText("");
                                       // Reset Combo-Boxes to orig. selection
    mpComboStatus.resetDefaultSelection();
    mpComboEmptyFlag.resetDefaultSelection();
    mpComboType.resetDefaultSelection();

                                       // Uncheck the check box.
    mpCheckAllowDel.setSelected(false);

    mpComboWarehouse.requestFocus();
  }

  /**
   *  Method to switch between rack and non-rack location format.
   *
   *  @param izRack True if setting to rack format.
   */
  protected void setRackFormat(boolean izRack)
  {
    int[] vanTranList;
    if (izRack)
    {
      vanTranList = new int[] { DBConstants.LCASRS, DBConstants.LCSTATION,
          DBConstants.LCFLOW, DBConstants.LCSHIPPING, DBConstants.LCRECEIVING,
          DBConstants.LCCONSOLIDATION, DBConstants.LCSTAGING,
          DBConstants.LCDEDICATED, DBConstants.LCDEVICE,
          DBConstants.LCCONVSTORAGE };
    }
    else
    {
      vanTranList = new int[] { DBConstants.LCSTATION, DBConstants.LCFLOW,
          DBConstants.LCSHIPPING, DBConstants.LCRECEIVING,
          DBConstants.LCCONSOLIDATION, DBConstants.LCSTAGING,
          DBConstants.LCDEDICATED, DBConstants.LCDEVICE,
          DBConstants.LCCONVSTORAGE };
    }
    try
    {
      mpComboType.setComboBoxData(LocationData.LOCATIONTYPE_NAME, vanTranList);
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
    }
    mpRackFormat.setSelected(izRack);
    mpBegAddressPanel.setRackMode(izRack);
    mpEndAddressPanel.setRackMode(izRack);
  }
}
