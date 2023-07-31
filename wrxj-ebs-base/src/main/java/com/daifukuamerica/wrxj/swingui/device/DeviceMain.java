package com.daifukuamerica.wrxj.swingui.device;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCListFrame;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.InternalFrameEvent;

/**
 * Description:<BR>
 *    Primary frame for Device maintenance.
 *
 * @author       A.D.
 *               12-16-02 Added double click and popup menu features.
 * @version      1.0
 * <BR>Created: 13-May-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
@SuppressWarnings("serial")
public class DeviceMain extends SKDCListFrame
{
  protected AddDeviceFrame addFrame;
  protected ModifyDeviceFrame modifyFrame;
  protected StationTableFrame stationFrame;
  protected StandardDeviceServer dvServer;
  protected StandardStationServer stServer;
  protected DeviceData dvdata = Factory.create(DeviceData.class);

  public DeviceMain() throws Exception
  {
    super("Device");

    userData = new SKDCUserData();
    logger.logDebug("DeviceMain.createDeviceServer()");
    dvServer = Factory.create(StandardDeviceServer.class);
    stServer = Factory.create(StandardStationServer.class);

    defineExtraButtons();
    setSearchData("Device", DBInfo.getFieldLength(DeviceData.DEVICEID_NAME));
  }

  /**
   * Overridden method so we can get our screen permissions.
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    searchButtonPressed();
  }

  @Override
  public void cleanUpOnClose()
  {
    dvServer.cleanUp();
    dvServer = null;
    stServer.cleanUp();
    stServer = null;
  }


/*===========================================================================
                  ****** Action methods go here ******
  ===========================================================================*/
  @Override
  protected void searchButtonPressed()
  {
    try
    {
      refreshTable(dvServer.getDeviceSearchData(getEnteredSearchText()));
    }
    catch (DBException exc)
    {
      displayError(exc.getMessage(), "DB Error");
      return;
    }
  }

  @Override
  protected void addButtonPressed()
  {
    addFrame = Factory.create(AddDeviceFrame.class, dvServer, stServer);
    addSKDCInternalFrameModal(addFrame, buttonPanel, new DeviceAddFrameHandler());
  }

  /**
   *  This method is called after the modify Location button is pressed to
   *  fill in the dvdata structure based on the current selected row on the
   *  Warehouse table display.
   */
  @Override
  protected void modifyButtonPressed()
  {
    int[] selection = sktable.getSelectedRows();
    if (selection.length == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    else if (selection.length > 1)
    {
      displayInfoAutoTimeOut("Only one row can be selected to Modify at a time", "Selection Error");
      return;
    }

    dvdata.dataToSKDCData(sktable.getSelectedRowData());
    String schedName = dvdata.getSchedulerName();
    if (schedName.trim().length() == 0)
      dvdata.setSchedulerName(SKDCConstants.NONE_STRING);

    modifyFrame = Factory.create(ModifyDeviceFrame.class, dvServer, stServer);
    modifyFrame.setCurrentData(dvdata);

/*---------------------------------------------------------------------------
   Add the frame and put the btnpanel on the toggle on-off list for
   SKDCInternalFrame. When the frame first comes up, the btnpanel is disabled.
   When there is a internal frame close event for the Modify frame (handled
   inside SKDCInternalFrame) toggle the button banel to enabled.
  ---------------------------------------------------------------------------*/
    addSKDCInternalFrameModal(modifyFrame, buttonPanel,
                              new DeviceModifyFrameHandler());
  }

  /**
   *  Handles Delete button for Location.
   */
  @Override
  protected void deleteButtonPressed()
  {
    if (modifyFrame != null && modifyFrame.isShowing())
    {
      modifyFrame.close();
    }

    int totalSelected;
    if ((totalSelected = sktable.getSelectedRowCount()) == 0)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }
    // Get selected list of Device IDs
    String[] delDeviceList = sktable.getSelectedColumnData(DeviceData.DEVICEID_NAME);

    int delCount = 0;
    int[] deleteIndices = sktable.getSelectedRows();

    for(int row = 0; row < totalSelected; row++)
    {
      String conf_mesg = "Delete Device " + delDeviceList[row] + " ";
      if (displayYesNoPrompt(conf_mesg, "Delete Confirmation"))
      {
        try
        {
          dvServer.deleteDevice(delDeviceList[row]);
          delCount++;
          searchButtonPressed();
          displayInfoAutoTimeOut("Deleted device " + delDeviceList[row], "Delete Result");
        }
        catch(DBException exc)
        {
          displayError(exc.getMessage(), "Delete Error");
                                       // De-Select the troubling row!
          sktable.deselectRow(deleteIndices[row]);
        }
      }
    }
    if ((delCount > 1) && (delCount != totalSelected))
    {
      displayInfoAutoTimeOut("Deleted " +  delCount + " of " + totalSelected +
                             " selected devices", "Delete Result");
    }
  }

  /**
   *  Displays Station data for this device.
   */
  @Override
  protected void viewButtonPressed()
  {                                    // Get DeviceID field from this row.
    int selectedRowIndex = sktable.getSelectedRow();
    if (selectedRowIndex == -1)
    {
      displayInfoAutoTimeOut("No row selected", "Selection Error");
      return;
    }

    dvdata.clear();
    dvdata.dataToSKDCData(sktable.getSelectedRowData());

    stationFrame = new StationTableFrame(dvServer);

    try
    {
      stationFrame.setTableData(dvdata.getDeviceID());
      addSKDCInternalFrameModal(stationFrame, buttonPanel);
    }
    catch(Exception e)
    {
      displayInfoAutoTimeOut("There are no stations for " + dvdata.getDeviceID());
      stationFrame.close();
    }
  }

/*===========================================================================
              ****** All other private methods go here ******
  ===========================================================================*/

  /**
   *  Defines all buttons on the main Location Panels, and adds listeners
   *  to them.
   */
  protected void defineExtraButtons()
  {
    viewButton.setText("Show Stations");
    viewButton.setToolTipText("All stations controlled by this device");
    viewButton.setMnemonic('h');
    setViewButtonVisible(true);
  }

/*===========================================================================
              ****** All Listener classes go here ******
  ===========================================================================*/

  /**
   *   Property Change event listener for Add frame.
   */
  public class DeviceAddFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        searchButtonPressed();
      }
    }
  }

  /**
   *   Property Change event listener for Modify frame.
   */
  public class DeviceModifyFrameHandler implements PropertyChangeListener
  {
    public void propertyChange(PropertyChangeEvent pcevt)
    {
      String prop_name = pcevt.getPropertyName();
      if (prop_name.equals(FRAME_CHANGE))
      {
        try
        {
          searchButtonPressed();
        }
        catch (Exception exc)
        {
          displayError(exc.getMessage(), "Modify Information");
        }
      }
    }
  }
  
  /**
   * Get the class name that will be used in the RoleOptions table.  This 
   * method facilitates the getting of permissions when setCategoryAndOption()
   * is not called and the implemented class is different from the baseline
   * class.
   * 
   * @return <code>Class</code>
   */
  @Override
  protected Class getRoleOptionsClass()
  {
    return DeviceMain.class;
  }
}
