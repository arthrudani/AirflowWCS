package com.daifukuamerica.wrxj.swingui.device;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.Warehouse;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;

/**
 * Description:<BR>
 *    Sets up the Device add internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 13-May-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class AddDeviceFrame extends DacInputFrame
{
  protected DeviceData mpBaseDeviceData = Factory.create(DeviceData.class);
  
  protected StandardDeviceServer mpDevServer;
  protected StandardStationServer mpStationServer;
  
  protected SKDCTextField    mpTxtDeviceID;
  protected SKDCComboBox     mpCmbWarehouse;
  protected SKDCTranComboBox mpCmbDeviceType;
  protected SKDCTranComboBox mpCmbOperStatus;
  protected SKDCIntegerField mpTxtAisleGroup;
  protected SKDCComboBox     mpCmbScheduler;
  protected SKDCComboBox     mpCmbAllocator;
  protected SKDCComboBox     mpCmbCommDevice;
  protected SKDCTextField    mpTxtCommReadPort;
  protected SKDCTextField    mpTxtCommSendPort;
  protected SKDCTextField    mpTxtNextDevice;
  protected JCheckBox        mpCBxDeviceToken;
  protected SKDCTextField    mpTxtPrinter;
  protected SKDCTranComboBox mpCmbEmulationMode;

  boolean mzIsSuperUser = SKDCUserData.isSuperUser();

  /**
   * Constructor
   * 
   * @param ipDevServer
   * @param ipStationServer
   */
  public AddDeviceFrame(StandardDeviceServer ipDevServer,
      StandardStationServer ipStationServer) 
  {
    super("Add Device", "Device Information");

    mpDevServer = ipDevServer;
    mpStationServer = ipStationServer;

    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException err)
    {
      displayWarning(err.getMessage(), "Translation Warning");
    }
  }

  /**
   * Method builds listing of Schedulers defined in the Device table.
   */
  protected void fillSchedulerCombo()
  {
    String[] vasSchedulers = mpDevServer.getSchedulerChoices(SKDCConstants.NONE_STRING, false);
    mpCmbScheduler.setComboBoxData(vasSchedulers);
  }

  /**
   * Method builds listing of Allocators defined in the Device table.
   */
  protected void fillAllocatorCombo()
  {
    String[] vasAllocators = mpDevServer.getAllocatorChoices(SKDCConstants.NONE_STRING);
    mpCmbAllocator.setComboBoxData(vasAllocators);
  }

  /**
   * Method builds listing of devices the AGCStationDevice controller talks to
   * as defined in the Device table.
   */
  protected void fillCommDeviceCombo()
  {
    String[] vasCommDevices = mpDevServer.getCommDeviceChoices(SKDCConstants.NONE_STRING);
    mpCmbCommDevice.setComboBoxData(vasCommDevices);
  }

  /**
   * Method to populate the warehouse combo box.
   */
  protected void fillWarehouseCombo()
  {
    try
    {
      Warehouse vpWarehouse = Factory.create(Warehouse.class);
      String[] vasWarehouses = vpWarehouse.getRegularWarehouseChoices("");
      mpCmbWarehouse.setComboBoxData(vasWarehouses);
    }
    catch (Exception e)
    {
      displayError(e.getMessage(), "DB Error");
    }
  }
    
  /*=========================================================================*/
  /*  Methods for display formatting go in this section.                     */
  /*=========================================================================*/
  /**
   * Build the screen
   */
  private void buildScreen() throws NoSuchFieldException
  {
    /*
     * Initialize the input fields
     */
    mpTxtDeviceID = new SKDCTextField(DeviceData.DEVICEID_NAME);
    mpCmbWarehouse = new SKDCComboBox();
    fillWarehouseCombo();
    mpCmbDeviceType = new SKDCTranComboBox(DeviceData.DEVICETYPE_NAME);
    mpCmbDeviceType.addActionListener(new DeviceTypeListener());
    mpCmbOperStatus = new SKDCTranComboBox(DeviceData.OPERATIONALSTATUS_NAME);
    mpTxtAisleGroup = new SKDCIntegerField(DeviceData.AISLEGROUP_NAME);
    mpTxtAisleGroup.setText("0");
    mpCmbScheduler = new SKDCComboBox();
    mpCmbScheduler.setEditable(true);
    fillSchedulerCombo();
    mpCmbAllocator = new SKDCComboBox();
    mpCmbAllocator.setEditable(true);
    fillAllocatorCombo();
    mpCmbCommDevice = new SKDCComboBox();
    mpCmbCommDevice.setEditable(true);
    fillCommDeviceCombo();
    mpTxtCommReadPort = new SKDCTextField(DeviceData.COMMREADPORT_NAME);
    mpTxtCommSendPort = new SKDCTextField(DeviceData.COMMSENDPORT_NAME);
    mpTxtNextDevice = new SKDCTextField(DeviceData.NEXTDEVICE_NAME);
    mpCBxDeviceToken = new JCheckBox();
    mpTxtPrinter = new SKDCTextField(DeviceData.PRINTER_NAME);
    mpCmbEmulationMode = new SKDCTranComboBox(DeviceData.EMULATIONMODE_NAME, DBConstants.NOEMU);

    /*
     * Add the input fields to the screen
     */
    addInput("Device:", mpTxtDeviceID);
    addInput("Warehouse:", mpCmbWarehouse);
    addInput("Device Type:", mpCmbDeviceType);
    addInput("Operational Status:", mpCmbOperStatus);
    addInput("Aisle Group:", mpTxtAisleGroup);
    addInput("Assigned Scheduler:", mpCmbScheduler);
    addInput("Assigned Allocator:", mpCmbAllocator);
    addInput("Communication Device (if different):", mpCmbCommDevice);
    addInput("Read Port:", mpTxtCommReadPort);
    addInput("Bi-directional or Write Port:", mpTxtCommSendPort);
    addInput("Next Device:", mpTxtNextDevice);
    addInput("Device Token:", mpCBxDeviceToken);
//    addInput("Printer:", txtPrinter);  // Nothing uses this now
    addInput("Emulation Mode:", mpCmbEmulationMode);

    useAddButtons();
    
    /*
     * Only super-user can add automated equipment
     */
    if (!SKDCUserData.isSuperUser())
    {
      mpBaseDeviceData.setDeviceType(DBConstants.CONV_DEVICE);
    }
    setData(mpBaseDeviceData);
    
    reorganizeScreen();
  }

/*===========================================================================
                Methods for event handling go in this section.
  ===========================================================================*/
  /**
   *  Processes Location add request.  This method stuffs column objects into
   *  a DeviceData instance container.
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
    DeviceData vpNewDeviceData = Factory.create(DeviceData.class);
    try
    {
      screenToDeviceData(vpNewDeviceData);
    }
    catch(NoSuchFieldException e)
    {
      displayError(e.getMessage(), "Translation Error");
      return;
    }

    // Call on the Device server to add the device.
    try
    {
      mpDevServer.addDevice(vpNewDeviceData);

      displayInfoAutoTimeOut("Device " + vpNewDeviceData.getDeviceID()
          + " added successfully");

      changed(null, vpNewDeviceData);           // Fire frame (data) change event.
    }
    catch(DBException exc)
    {
      exc.printStackTrace(System.out);
      displayError(exc.getMessage(), "DB Error");
      clearButtonPressed();
    }
    
    //
    // Update Combo boxes in case of more entries.
    //
    fillSchedulerCombo();
    fillAllocatorCombo();    
    fillCommDeviceCombo();
  }

  /**
   * Check the inputs.  If an input is bad, request focus for that input.
   *
   * @return true if all is well, false otherwise
   */
  protected boolean checkInputs()
  {
    // Device ID is a required field.
    String vsDeviceId = mpTxtDeviceID.getText();
    if (vsDeviceId.length() == 0)
    {
      displayInfoAutoTimeOut("Device ID is required", "Entry Error");
      mpTxtDeviceID.requestFocus();
      return false;
    }

    // Make sure the Device doesn't already exist
    if (mpDevServer.exists(vsDeviceId))
    {
      displayInfoAutoTimeOut("Device " + vsDeviceId + " already exists",
          "Entry Error");
      mpTxtDeviceID.requestFocus();
      return false;
    }
    
    // Make sure the Communication Device is different
    if (mpCmbCommDevice.getSelectedItem().equals(vsDeviceId))
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
   * Copy the contents of the input fields into a DeviceData
   * <BR>NOTE: Does not copy the Device ID
   * @param ipTargetDeviceData
   * @throws NoSuchFieldException
   */
  protected void screenToDeviceData(DeviceData ipTargetDeviceData) 
    throws NoSuchFieldException
  {
    ipTargetDeviceData.setDeviceID(mpTxtDeviceID.getText());
    ipTargetDeviceData.setWarehouse(mpCmbWarehouse.getText().trim());
    ipTargetDeviceData.setDeviceType(mpCmbDeviceType.getIntegerValue());
    if (mpCmbDeviceType.getIntegerValue() == DBConstants.CONV_DEVICE)
    {
      // Conventional devices are always ONLINE
      ipTargetDeviceData.setOperationalStatus(DBConstants.APPONLINE);
      ipTargetDeviceData.setPhysicalStatus(DBConstants.ONLINE);
    }
    else
    {
      ipTargetDeviceData.setOperationalStatus(mpCmbOperStatus.getIntegerValue());
    }
    ipTargetDeviceData.setAisleGroup(mpTxtAisleGroup.getValue());
    if (mpCmbScheduler.getText().equals(SKDCConstants.NONE_STRING))
      ipTargetDeviceData.setSchedulerName("");
    else
      ipTargetDeviceData.setSchedulerName(mpCmbScheduler.getText().trim());

    if (mpCmbAllocator.getText().equals(SKDCConstants.NONE_STRING))
      ipTargetDeviceData.setAllocatorName("");
    else
      ipTargetDeviceData.setAllocatorName(mpCmbAllocator.getText().trim());

    if (mpCmbCommDevice.getText().equals(SKDCConstants.NONE_STRING))
      ipTargetDeviceData.setCommDevice("");
    else
      ipTargetDeviceData.setCommDevice(mpCmbCommDevice.getText().trim());
    ipTargetDeviceData.setCommReadPort(mpTxtCommReadPort.getText());
    ipTargetDeviceData.setCommSendPort(mpTxtCommSendPort.getText());
    ipTargetDeviceData.setNextDevice(mpTxtNextDevice.getText());
    ipTargetDeviceData.setEmulationMode(mpCmbEmulationMode.getIntegerValue());
    ipTargetDeviceData.setDeviceToken(
        mpCBxDeviceToken.isSelected() ? DBConstants.TRUE : DBConstants.FALSE);
    ipTargetDeviceData.setPrinter(mpTxtPrinter.getText());
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Device Frame.
   */
  @Override
  protected void clearButtonPressed()
  {
    setData(mpBaseDeviceData);
    mpTxtDeviceID.requestFocus();
  }

  /**
   * Load the screen with the current data.
   */
  protected void setData(DeviceData currentData)
  {
    try
    {
      mpTxtDeviceID.setText(currentData.getDeviceID());
      mpCmbWarehouse.setSelectedItem(currentData.getWarehouse());
      mpCmbDeviceType.setSelectedElement(currentData.getDeviceType());
      mpCmbOperStatus.setSelectedElement(currentData.getOperationalStatus());
      mpTxtAisleGroup.setValue(currentData.getAisleGroup());
      mpCmbScheduler.setSelectedItem(currentData.getSchedulerName());
      mpCmbAllocator.setSelectedItem(currentData.getAllocatorName());
      mpCmbCommDevice.setSelectedItem(currentData.getCommDevice());
      mpTxtCommReadPort.setText(currentData.getCommReadPort());
      mpTxtCommSendPort.setText(currentData.getCommSendPort());
      mpTxtNextDevice.setText(currentData.getNextDevice());
      mpCmbEmulationMode.setSelectedElement(currentData.getEmulationMode());
      mpCBxDeviceToken.setSelected(currentData.getDeviceToken() == DBConstants.TRUE);
      mpTxtPrinter.setText(currentData.getPrinter());
    }
    catch(NoSuchFieldException e)
    {
      displayWarning(e.getMessage(), "Translation Error");
    }
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
   * Determines which fields are enabled/viewable based upon device type and 
   * user level
   */
  private void reorganizeScreen()
  {
    int vnDevType = 0;
    try
    {
      vnDevType = mpCmbDeviceType.getIntegerValue();
    }
    catch (NoSuchFieldException e1)
    {
      displayWarning(e1.getMessage(), "Translation Error");
    }
    boolean vzConventionalDevice = (vnDevType == DBConstants.CONV_DEVICE);
    
    /*
     * Automated vs. Conventional devices
     */
    if (vzConventionalDevice)
    {
      setInputEnabled(mpCmbOperStatus, false);
      setInputEnabled(mpTxtAisleGroup, false);
      setInputEnabled(mpCmbScheduler, mzIsSuperUser);
      setInputEnabled(mpCmbAllocator, mzIsSuperUser);
      setInputEnabled(mpCmbCommDevice, false);
      setInputEnabled(mpTxtCommReadPort, false);
      setInputEnabled(mpTxtCommSendPort, false);
      setInputEnabled(mpTxtNextDevice, false);
      setInputEnabled(mpCmbEmulationMode, false);
      setInputEnabled(mpCBxDeviceToken, false);
      
      mpTxtAisleGroup.setText("0");
      mpCmbWarehouse.setEnabled(true);
    }
    else
    {
      setInputEnabled(mpCmbOperStatus, true);
      setInputEnabled(mpTxtAisleGroup, mzIsSuperUser);
      setInputEnabled(mpCmbScheduler, mzIsSuperUser);
      setInputEnabled(mpCmbAllocator, mzIsSuperUser);
      setInputEnabled(mpCmbCommDevice, mzIsSuperUser);
      setInputEnabled(mpTxtCommReadPort, mzIsSuperUser);
      setInputEnabled(mpTxtCommSendPort, mzIsSuperUser);
      setInputEnabled(mpTxtNextDevice, mzIsSuperUser);
      setInputEnabled(mpCmbEmulationMode, mzIsSuperUser);
      setInputEnabled(mpCBxDeviceToken, mzIsSuperUser);
      mpCmbWarehouse.setEnabled(mzIsSuperUser);
    }
    mpCmbDeviceType.setEnabled(mzIsSuperUser);
  }
  
  /**
   *  Inner listener class for button events.
   */
  class DeviceTypeListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      String which_button = e.getActionCommand();
      if (which_button.equals("comboBoxChanged"))
      {
        reorganizeScreen();
      }
    }
  }
}
