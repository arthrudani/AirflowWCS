package com.daifukuamerica.wrxj.emulation;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.io.PropertyReader;
import java.util.ArrayList;
import java.util.List;

public class DeviceEmulator extends Controller
{
  /**
   * A list of equipment properties obtained from the equipment properties file.
   */
  List<List<String>> mpEquipPropList;
  
  /**
   * A utility object for reading properties from the equipment list
   */
  PropertyReader mpReader;

  /**
   * Constructor
   */
  protected DeviceEmulator()
  {
    try
    {
      String vsEquipFile = Application.getString(StatusModel.EQUIPMENT_CONFIGURATION_KEY);
      mpReader = PropertyReader.newInstance();
      mpEquipPropList = mpReader.getAllPropertyCollections(vsEquipFile, StatusModel.EQUIPMENT_NAME);
    }
    catch (Exception ex)
    {
      logger.logException("Error creating equipment properties list for emulator",ex);
    }
  }
  
  /**
   * Get the list of equipment that reports status via the MC port
   * @return
   */
  protected List<String> getTransporterList()
  {
    String vsDevice = getConfigProperty(DEVICE_ID);
    List<String> vpTransporterList = new ArrayList<String>();
      
    for (List<String> vpEq : mpEquipPropList)
    {
      String vsMC = mpReader.getProperty(vpEq, StatusModel.MC_CONTROLLER);
      if(vsMC != null && vsMC.equals(vsDevice))
      {
        String vsMCID = mpReader.getProperty(vpEq, StatusModel.MC_ID);
        if (vsMCID != null && !vsMCID.equals("*NONE*") && isTransporter(vsMCID))
          vpTransporterList.add(vsMCID);
      }
    }
    return vpTransporterList;
  }
  
  /**
   * Is this piece of equipment a transporter?  (Do we get MC status messages?)
   * 
   * @param isMCID
   * @return
   */
  protected boolean isTransporter(String isMCID)
  {
    return true;
  }
  
  /**
   * Get the list of equipment that reports status via the MOS port
   * @return
   */
  protected List<String> getEquipmentList()
  {
    String vsMOSDev = getConfigProperty(MOS_DEVICE);
    
    // Make sure we are even emulating a mos device
    if (vsMOSDev == null)
      return null;
    
    List<String> vpEquipmentList =  new ArrayList<String>();
    
    for (List<String> vpEq : mpEquipPropList)
    {
      String vsMOSCtlr = mpReader.getProperty(vpEq, StatusModel.MOS_CONTROLLER);
      if (vsMOSCtlr != null && vsMOSCtlr.equals(vsMOSDev))
      {
        String vsMOSID = mpReader.getProperty(vpEq, StatusModel.MOS_ID);
        if (vsMOSID != null && !vsMOSID.equals("*NONE*"))
          vpEquipmentList.add(vsMOSID);
      }
    }
    return vpEquipmentList;
  }
  
  /**
   * Get the station ID for a given machine/MOS ID
   * @param isMachineID
   * @return String Station ID or null if none
   */
  protected String getStationID(String isMachineID)
  {
    /*
     * Map machine IDs to station IDs for tracking mode change
     */
    String vsMOSDev = getConfigProperty(MOS_DEVICE);
    if (vsMOSDev != null)
    {
      for (List<String> vpEq : mpEquipPropList)
      {
        String vsMOSCtlr = mpReader.getProperty(vpEq, StatusModel.MOS_CONTROLLER);
        if (vsMOSCtlr != null && vsMOSCtlr.equals(vsMOSDev))
        {
          String vsMOSID = mpReader.getProperty(vpEq, StatusModel.MOS_ID);
          String vsStation = mpReader.getProperty(vpEq, StatusModel.STATION_ID);
          String vsDevice = mpReader.getProperty(vpEq,StatusModel.DEVICE_ID);
          if (vsMOSID != null && vsMOSID.equals(isMachineID) && 
              vsStation != null && !vsStation.equals("*NONE*") &&
              !vsStation.startsWith("9"))
            return vsStation;
          else if( vsDevice.startsWith("SR"))
        	return vsDevice;
        }
      }
    }
    return null;
  }
  
  /**
   * Get the MC machine ID for a given device ID
   * @param isDeviceID
   * @return String Machine ID or null if none
   */
  protected String getMOSIDForDevice(String isDeviceID)
  {
    /*
     * Map machine IDs to station IDs for tracking mode change
     */
    for (List<String> vpEq : mpEquipPropList)
    {
      String vsMOSID = mpReader.getProperty(vpEq, StatusModel.MOS_ID);
      String vsDeviceID = mpReader.getProperty(vpEq, StatusModel.DEVICE_ID);
      if (vsDeviceID != null && vsDeviceID.equals(isDeviceID) && 
          vsMOSID != null && !vsMOSID.equals("*NONE*"))
        return vsMOSID;
    }
    return null;
  }
}
