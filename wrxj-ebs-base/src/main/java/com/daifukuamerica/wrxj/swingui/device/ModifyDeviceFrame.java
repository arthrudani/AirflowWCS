package com.daifukuamerica.wrxj.swingui.device;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Description:<BR>
 *    Sets up the Device Modify internal frame.  It fills in the contents of
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
public class ModifyDeviceFrame extends AddDeviceFrame
{
  protected DeviceData mpCurrentDeviceData = Factory.create(DeviceData.class);

  /**
   * Constructor
   * 
   * @param ipDevServer
   * @param ipStationServer
   */
  public ModifyDeviceFrame(StandardDeviceServer ipDevServer,
      StandardStationServer ipStationServer)
  {
    super(ipDevServer, ipStationServer);
    
    setTitle("Modify Device");
    mpTxtDeviceID.setEnabled(false);
    useModifyButtons();
  }

  /**
   * Load the modify screen with the current selected row of data from the
   * table.
   */
  public void setCurrentData(DeviceData ipDeviceData)
  {
    mpCurrentDeviceData = (DeviceData) ipDeviceData.clone();
    mpBaseDeviceData = (DeviceData)ipDeviceData.clone();

    setData(mpCurrentDeviceData);
  }


/*===========================================================================
              Methods for event handling go in this section.
  ===========================================================================*/
  /**
   *  Gathers data off the screen and builds Device Data structure.
   */
  @Override
  protected void okButtonPressed()
  {
    // Check the fields
    if (!checkInputs())
    {
      return;
    }

    // Convert screen data to DeviceData
    try
    {
      mpBaseDeviceData.clear();
      screenToDeviceData(mpBaseDeviceData);
    }
    catch(NoSuchFieldException e)
    {
      displayError(e.getMessage(), "Translation Error");
      return;
    }

   try
    {                                  // Set the key for the modify
      mpBaseDeviceData.setDeviceID(mpTxtDeviceID.getText().trim());
      mpBaseDeviceData.setKey(DeviceData.DEVICEID_NAME, mpTxtDeviceID.getText().trim());

      mpDevServer.modifyDevice(mpBaseDeviceData);

      displayInfoAutoTimeOut("Device " + mpTxtDeviceID.getText() + " Modified");
      changed(null, mpBaseDeviceData);
      Thread.sleep(30);
      close();
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "Modify Error!");
    }
    catch(InterruptedException e)
    {
    }
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.device.AddDeviceFrame#checkInputs()
   */
  @Override
  protected boolean checkInputs()
  {
    // Make sure the Communication Device is different
    if (mpCmbCommDevice.getSelectedItem().equals(mpTxtDeviceID.getText()))
    {
      displayInfoAutoTimeOut(
          "Only enter a communication device if it is a different device (ie MOS or AGC).",
          "Entry Error");
      mpCmbCommDevice.requestFocus();
      return false;
    }
    
    return true;
  }
  
  /**
   *  Reset button event handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpCurrentDeviceData);
    mpCmbDeviceType.requestFocus();
  }
}