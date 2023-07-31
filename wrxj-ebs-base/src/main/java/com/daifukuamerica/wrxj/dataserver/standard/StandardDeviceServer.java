package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.JVMConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Port;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleMove;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleMoveData;
import com.daifukuamerica.wrxj.dbadapter.data.VehiclePaths;
import com.daifukuamerica.wrxj.dbadapter.data.VehiclePathsData;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleSystemCmd;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleSystemCmdData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxSequencer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Ed Askew
 * @author Steven Kendorski
 * @version 1.0
 */
public class StandardDeviceServer extends StandardServer
{
  protected StandardConfigurationServer mpConfigServer = null;
  protected StandardHostServer mpHostServer = null;
  protected TableJoin mpTJ = Factory.create(TableJoin.class);
  protected Station mpStation = Factory.create(Station.class);
  protected Device mpDevice = Factory.create(Device.class);
  protected Port mpPort = Factory.create(Port.class);
  protected final DeviceData mpDevData = Factory.create(DeviceData.class);
  protected final WrxSequencer mpSequencer = Factory.create(WrxSequencer.class);
  protected final VehiclePaths mpVPath = Factory.create(VehiclePaths.class);
  protected final VehicleMove mpVehicleMove = Factory.create(VehicleMove.class);
  protected final VehicleMoveData mpVMData = Factory.create(VehicleMoveData.class);
  protected final VehicleSystemCmd mpVSCmd = Factory.create(VehicleSystemCmd.class);
  protected final VehicleSystemCmdData mpVSCmdData = Factory.create(VehicleSystemCmdData.class);

  public StandardDeviceServer()
  {
    this(null);
  }

  public StandardDeviceServer(String keyName)
  {
    super(keyName);
  }

  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardDeviceServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo);
  }


 /**
  * Indicates if this device is part of a split system.
  * @param isDeviceID the device ID.
  * @return <code>true</code> if device is part of a split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isDevicePartOfAnySplitSystem(String isDeviceID) throws DBException
  {
    return(mpTJ.isDevicePartOfAnySplitSystem(isDeviceID));
  }

 /**
  * Indicates if this device is part of a split system.
  * @param isDeviceID the device ID.
  * @return <code>true</code> if device is part of a split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isDevicePartOfThisSplitSystem(String isDeviceID) throws DBException
  {
    return(mpTJ.isDevicePartOfThisSplitSystem(isDeviceID));
  }

 /**
  * Method to count the number of aisles in a primary JVM.
  * @return the count of number of aisles in a primary JVM.
  * @throws DBException
  */
  public int getPrimaryJVMAisleCount() throws DBException
  {
    initConfigServer();
    String vsPrimaryJVM = mpConfigServer.getPrimaryJVMIdentifier();
    DeviceData vpDevData = Factory.create(DeviceData.class);
    vpDevData.setKey(DeviceData.JVMIDENTIFIER_NAME, vsPrimaryJVM);

    return(mpDevice.getCount(vpDevData));
  }

 /**
  * Method gets the count of the number of unique schedulers in a JVM.
  * @param isJVMIdentifier The JVM Identifier.
  * @return scheduler count.
  * @throws DBException if there is a database error.
  */
  public int getSchedulerCountPerJVM(String isJVMIdentifier) throws DBException
  {
    DeviceData vpDevData = Factory.create(DeviceData.class);
    vpDevData.setKey(DeviceData.JVMIDENTIFIER_NAME, isJVMIdentifier);
    String[] vasSched = mpDevice.getSingleColumnValues(DeviceData.SCHEDULERNAME_NAME,
                                                       true, vpDevData,
                                                    SKDCConstants.NO_PREPENDER);
    return(vasSched.length);
  }

  /**
   *  Method checks if a given device is a conventional device.
   *
   *  @param sDeviceID <code>String</code> containing the device id.
   *
   *  @return <code>true</code> if device is conventional.
   */
  public boolean isConventionalDevice(String sDeviceID)
  {
    boolean convDev = false;
    try
    {
      convDev = (mpDevice.getDeviceType(sDeviceID) == DBConstants.CONV_DEVICE);
    }
    catch(DBException e)
    {
      logException(e, getClass().getSimpleName() + ".isConventionalDevice");
    }

    return(convDev);
  }

  /**
   *  Adds a Device.
   *
   *  @param ipDeviceData object containing data collected from GUI screen.
   */
  public String addDevice(DeviceData ipDeviceData) throws DBException
  {
    /*
     * TODO: Conventional devices REQUIRE a location for the device. This
     * requirement, however, can break RTS retro-fits. We need to find a way to
     * keep both happy.
     */

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpDevice.addElement(ipDeviceData);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      throw new DBException("Transaction Error! " + e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

    String vsMesg = "Device Record for " + ipDeviceData.getDeviceID()
        + "\n added successfully.";

    return(vsMesg);
  }

  /*--------------------------------------------------------------------------*/
  /**
   *  Modifies a Device Record based on DeviceData passed in, and the unique device id
   *  key.
   * @param dvdata Data class containing key and column modify settings.
   * @throws com.daifukuamerica.wrxj.jdbc.DBException for database access errors.
   * @return  Message indicating success or failure (useful for GUI mainly).
   */
  public String modifyDevice(DeviceData dvdata) throws DBException
  {
    /*
     * TODO: Conventional devices REQUIRE a location for the device. This
     * requirement, however, can break RTS retro-fits. We need to find a way to
     * keep both happy.
     */

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();

      DeviceData vpOldData = getDeviceData(dvdata.getDeviceID());
      logModifyTransaction(vpOldData, dvdata);

      mpDevice.modifyElement(dvdata);
      if(dvdata.getOperationalStatus() == DBConstants.INOP)
      {
          StandardDeallocationServer dealServ =
            Factory.create(StandardDeallocationServer.class, getClass().getSimpleName());
          dealServ.deallocateMovesForDevice(dvdata.getDeviceID());
          dealServ.cleanUp();
      }
      commitTransaction(tt);
    }
    catch(DBException exc)
    {
      logException(exc, "StandardDeviceServer - modifyDevice");
      throw exc;
    }
    finally
    {
      endTransaction(tt);
    }
    String vsMesg = "Device " + dvdata.getDeviceID() + " modified successfully.";

    return(vsMesg);
  }

  /**
   *  Return true if the specified device can be found.
   *
   * @param devID the name key to lookup
   * @return true if device exists
   */
  public boolean exists(String devID)
  {
    try
    {
      return mpDevice.exists(devID);
    }
    catch(DBException e)
    {
      throw new RuntimeException("Database exception: "+ e, e);
    }
  }

  /**
   * Deletes a device record based on unique device id.
   * @param deviceID <code>String</code> containing device ID of the device
   *        to delete.
   * @throws com.daifukuamerica.wrxj.jdbc.DBException
   */
  public void deleteDevice(String deviceID) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      DeviceData dvdata = Factory.create(DeviceData.class);
      dvdata.setDeviceID(deviceID);
      dvdata.setKey(DeviceData.DEVICEID_NAME, deviceID);
      dvdata = mpDevice.getElement(dvdata, DBConstants.WRITELOCK);

      /*
       * If there is a location, delete it
       */
      LocationData lcdata = Factory.create(LocationData.class);
      lcdata.setWarehouse(dvdata.getWarehouse());
      lcdata.setAddress(dvdata.getDeviceID());
      lcdata.setKey(LocationData.WAREHOUSE_NAME, lcdata.getWarehouse());
      lcdata.setKey(LocationData.ADDRESS_NAME, lcdata.getAddress());
      Location location = Factory.create(Location.class);
      if (location.exists(lcdata))
      {
        location.deleteElement(lcdata);
      }

      /*
       * Check for stations
       */
      if (Factory.create(Station.class).getDeviceStationCount(deviceID) > 0)
      {
        throw new DBException("Stations exist for this Device!\nDevice not deleted...");
      }
      else
      {
        /*
         * Check for other locations
         */
        int lcCount = Factory.create(StandardLocationServer.class, "StandardDeviceServer").getLocationDeviceCount(deviceID);
        if (lcCount > 0)
        {
          throw new DBException("Locations exist for this Device!\nDevice not deleted...");
        }
        else if (lcCount < 0)
        {
          throw new DBException(
                "Error checking for locations\nattached to Device " + deviceID);
        }
      }

      dvdata.setKey(DeviceData.DEVICEID_NAME, deviceID);
      mpDevice.deleteElement(dvdata);

      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.DELETE);
      tnData.setDeviceID(deviceID);
      logTransaction(tnData);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Fetches a list of available Schedulers from the System Configuration.
   *
   * @param isArrayPrepender One of the following constants:<ul>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#ALL_STRING
   *              SKDCConstants.ALL_STRING}
   *            which prepends the string "ALL" to the array.</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NONE_STRING
   *              SKDCConstants.NONE_STRING},
   *            which prepends the string "NONE" to the array</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#EMPTY_VALUE
   *              SKDCConstants.EMPTY_VALUE},
   *            which prepends a blank string to the array.</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NO_PREPENDER
   *              SKDCConstants.NO_PREPENDER},
   *            which means there is no prepender (no pre-defined first element)
   *            to the list.</li></ul>
   * @param izPhysicallyOnlineDeviceOnly if <code>true</code> find only those
   *        schedulers that are associated with physically online devices.
   * @return <code>String[]</code> array with nothing if there is some type of
   *         error, or no data. Scheduler list otherwise.
   */
  public String[] getSchedulerChoices(String isArrayPrepender,
                                      boolean izPhysicallyOnlineDeviceOnly)
  {
    try
    {
      initConfigServer();
      if (mpConfigServer.isSplitSystem())
      {
        String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
        return mpDevice.getSchedulerChoices(vsJVMId, isArrayPrepender,
                                            izPhysicallyOnlineDeviceOnly);
      }
      else
      {
        return mpDevice.getSchedulerChoices(isArrayPrepender,
                                            izPhysicallyOnlineDeviceOnly);
      }
    }
    catch(DBException e)
    {
      String[] clist = new String[1];
      clist[0] = "";
      return clist;
    }
  }

  /**
   * Fetches a list of Allocators from allocator-aware devices.
   *
   * @param allOrNone <code>String</code> specifying if the "ALL" or "NONE"
   *            string should be included in the choice list.
   *
   * @return <code>String[]</code> array with nothing if there is some type of
   *         error, or no data. Allocator list otherwise.
   */
  public String[] getAllocatorChoices(String allOrNone)
  {
    try
    {
      initConfigServer();
      if (mpConfigServer.isSplitSystem())
      {
        String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
        return(mpDevice.getAllocatorChoices(allOrNone, vsJVMId));
      }
      else
      {
        return(mpDevice.getAllocatorChoices(allOrNone));
      }
    }
    catch(DBException e)
    {
      String[] clist = new String[1];
      clist[0] = "";
      return clist;
    }
  }

  /**
   * Fetches a list of available Communication Devices from the System
   * Configuration table.
   *
   * @param allOrNone <code>String</code> specifying if the "ALL" or "NONE"
   *            string should be included in the choice list.
   *
   * @return <code>String[]</code> array with nothing if there is some type of
   *         error, or no data. Comm. Device list otherwise.
   */
  public String[] getCommDeviceChoices(String allOrNone)
  {
    try
    {
      return mpDevice.getCommDeviceChoices(allOrNone);
    }
    catch(DBException e)
    {
      String[] clist = new String[1];
      clist[0] = "";
      return(clist);
    }
  }

  /**
   * Get a list of devices
   * @return list of devices <code>String</code>
   * @throws DBException
   */
  public List<String> getDeviceNameList() throws DBException
  {
    return mpDevice.getDeviceNameList();
  }

 /**
  * Method to get an array of devices under a particular super warehouse.
  * @param isSuperWhs the super warehouse
  * @return array of devices.  Empty array if nothing found.
  * @throws DBException if there is a database error.
  */
  public String[] getDeviceNamesPerSuperWarehouse(String isSuperWarehouse)
         throws DBException
  {
    return(mpDevice.getDeviceNamesPerSuperWarehouse(isSuperWarehouse));
  }

 /**
  * Method to get an array of all names belonging to storage rack devices.
  *
  * @return array of storage device ids.
  * @throws DBException if there are DB access errors.
  */
  public String[] getRackStorageDeviceNames() throws DBException
  {
    DeviceData vpDVKey = Factory.create(DeviceData.class);
    vpDVKey.setInKey(DeviceData.DEVICETYPE_NAME, 0, DBConstants.ARC100,
                     DBConstants.SRC9X, DBConstants.SRC9Y, DBConstants.SRC5,
                     DBConstants.AGC, DBConstants.AGC9X);
    vpDVKey.setOrderByColumns(DeviceData.DEVICEID_NAME);
    return(mpDevice.getSingleColumnValues(DeviceData.DEVICEID_NAME, true, vpDVKey,
                                   SKDCConstants.NO_PREPENDER));
  }

  /**
   * Gets a String[] of devices of the type
   * @param inType <code/>int</code> the station type
   * @param isAllorNoneorBlank <code/>String</code>
   * @return List<String> Devices of inType
   * @throws DBException
   */
  public String[] getDevicesNameListByType(int inType, String isAllorNoneorBlank)
      throws DBException
  {
    return mpDevice.getDevicesNameListByType(inType, isAllorNoneorBlank);
  }

  /**
   * Get matching devices
   *
   * @param srchDevice
   * @return
   * @throws DBException
   */
  public List<Map> getDeviceSearchData(String srchDevice) throws DBException
  {
    return mpDevice.getSearchData(srchDevice);
  }

  /**
   * Get the DeviceData for a device
   * @param isDeviceID
   * @return
   */
  public DeviceData getDeviceData(String isDeviceID)
  {
    DeviceData vpDevKey = Factory.create(DeviceData.class);
    vpDevKey.setKey(DeviceData.DEVICEID_NAME, isDeviceID);
    DeviceData vpDevData = null;
    try
    {
      vpDevData = mpDevice.getElement(vpDevKey, DBConstants.NOWRITELOCK);
    }
    catch (DBException e)
    {
      logException("Error getting device " + isDeviceID, e);
    }
    return(vpDevData);
  }

  /**
   *  Gets all Stations attached to this device from the Station Object.
   */
  public List<Map> getDeviceStationData(String deviceID) throws DBException
  {
    return Factory.create(Station.class).getDeviceStationData(deviceID);
  }

 /**
  * Get all devices that have a particular comm device.  This is useful to
  * determine all physical devices used by an AGC.
  * @param isCommDevice the comm device.
  * @return a string array of device id's.
  * @throws DBException if there is a database access error.
  */
  public String[] getDevicesByCommDeviceName(String isCommDevice)
         throws DBException
  {
    DeviceData vpDevKey = Factory.create(DeviceData.class);
    vpDevKey.setKey(DeviceData.COMMDEVICE_NAME, isCommDevice);
    String[] vasDevices = mpDevice.getSingleColumnValues(DeviceData.DEVICEID_NAME,
                                    true, vpDevKey, SKDCConstants.NO_PREPENDER);
    return(vasDevices);
  }
  
  /**
   * Get all devices from Port entries.  This is used in the web EquipLogView screen
   * @return a string array of device id's.
   * @throws DBException if there is a database access error.
   */
   public String[] getPortDevicesByDeviceNameForWeb(boolean insert_all)
          throws DBException
   {
	 String[] vasDevices = null;
     PortData vpPortKey = Factory.create(PortData.class);
     
     if( insert_all)
         vasDevices = mpPort.getSingleColumnValues(PortData.DEVICEID_NAME,
                                     true, vpPortKey, SKDCConstants.ALL_STRING);
     else
         vasDevices = mpPort.getSingleColumnValues(PortData.DEVICEID_NAME,
                                     true, vpPortKey, SKDCConstants.NO_PREPENDER);
     return(vasDevices);
   }

  /**
   *  Method to get the Operational status of a device attached to a station.
   *  <b>Note:</b> this method is setup with the assumption of one device per
   *  multiple stations.  It may be that in the non-automated area we will have
   *  one station containing multiple devices.
   *
   *  @param stationName <code>String</code> containing the device ID to check.
   *
   *  @return -1 if there is a error of some type.  Otherwise the device status.
   */
  public int getOperationalStatus(String stationName)
  {
    int status = -1;

    if (stationName.length() != 0)
    {
      StationData stData = Factory.create(StationData.class);
      stData.setKey(StationData.STATIONNAME_NAME, stationName);
      StationData stnData = null;

      try
      {
        stnData = Factory.create(Station.class).getElement(stData, DBConstants.NOWRITELOCK);
        if (stnData == null)
        {
          logDebug("Station " + stationName + " has no Device attached!");
          return(status);
        }
        status = mpDevice.getOperationalStatusValue(stnData.getDeviceID());
      }
      catch(DBException e)
      {
        logException(e, "StandardDeviceServer - Device \""
            + stnData.getDeviceID() + "\" - getOperationalStatus()");
      }
    }

    return(status);
  }

  /**
   * Get the physical status of a device
   * @param deviceID
   * @return
   */
  public int getPhysicalStatus(String deviceID)
  {
    int status = 0;
    if (deviceID.length() != 0)
    {
      try
      {
        status = mpDevice.getPhysicalStatus(deviceID);
      }
      catch (Exception e)
      {
        logException(e, "StandardDeviceServer - Device \"" + deviceID
            + "\" - getPhysicalStatus()");
      }
    }
    else
    {
      logError("StandardDeviceServer - Device \"" + deviceID
          + "\" NOT Found - getPhysicalStatus()");
    }

    return status;
  }

 /**
  * Method checks if a station's device is inoperable.  <b>Note:</b> For AGC
  * systems, the station's device is the SRC's comm. device in the device
  * table. For SRC-only systems the station's device is the SRC device itself.
  *
  * @param isStationName <code>String</code> containing device's station.
  *
  * @return <code>boolean</code> of <code>false</code> if device is functioning.
  */
  public boolean isStationDeviceInoperable(String isStationName)
  {
    String vsDevice = "";
    try
    {
      vsDevice = (String)mpStation.getSingleColumnValue(isStationName,
                                                     StationData.DEVICEID_NAME);
    }
    catch(DBException exc)
    {
      logException(exc, "Error finding device for station " +
                   isStationName + "!");
    }

    return(isDeviceInoperable(vsDevice));
  }

 /**
  * Method checks if a device's logical (versus Physical) operational status is
  * INOP.
  * @param isDeviceID the device ID.
  * @return <code>true</code> if the device is inoperable.
  */
  public boolean isDeviceInoperable(String isDeviceID)
  {
    Integer vpOperStat = null;
    boolean vzRtn = true;

    if (isDeviceID != null && !isDeviceID.isEmpty())
    {
      try
      {
        vpOperStat = (Integer)mpDevice.getSingleColumnValue(isDeviceID,
                                             DeviceData.OPERATIONALSTATUS_NAME);
        if (vpOperStat != null)
        {
          vzRtn = (vpOperStat.intValue() == DBConstants.INOP);
        }
      }
      catch(DBException exc)
      {
        logException(exc, "Error finding device " + isDeviceID + "!");
      }
    }

    return(vzRtn);
  }

  /**
   * Set the physical status of a device
   * @param isDeviceID the device whose status os to be changed.
   * @param inStatus the new device status.
   * @throws DBException
   */
   public void setPhysicalStatus(String isDeviceID, int inStatus) throws DBException
   {
     TransactionToken vpTok = null;
     try
     {
       vpTok = startTransaction();
       switch(inStatus)
       {
         case DBConstants.ONLINE:
           mpDevData.clear();
           mpDevData.setKey(DeviceData.DEVICEID_NAME, isDeviceID);
           DeviceData vpDVData = mpDevice.getElement(mpDevData, DBConstants.WRITELOCK);
           if (vpDVData != null)
           {
             if (vpDVData.getPhysicalStatus() == DBConstants.OFFLINE)
             {
               mpDevData.setPhysicalStatus(DBConstants.ONLINE);
               mpDevice.modifyElement(mpDevData);
               sendHostDeviceStatus(isDeviceID, inStatus);
             }

             String vsCommDev = vpDVData.getCommDevice();
             if (vsCommDev != null && vsCommDev.trim().length() > 0)
             {                         // There's a comm device also. If it's
                                       // offline make it online.
               if (getPhysicalStatus(vsCommDev) == DBConstants.OFFLINE)
               {
                 mpDevice.setPhysicalStatus(vsCommDev, DBConstants.ONLINE);
               }
             }
           }
           else
           {
             throw new DBException("Error finding device record for '" +
                                   isDeviceID + "'");
           }
           break;

         case DBConstants.OFFLINE:
           if (getPhysicalStatus(isDeviceID) == DBConstants.ONLINE)
           {
             mpDevice.setPhysicalStatus(isDeviceID, inStatus);
             sendHostDeviceStatus(isDeviceID, inStatus);
           }
           break;

       }
       commitTransaction(vpTok);
     }
     finally
     {
       endTransaction(vpTok);
     }
   }

  /**
   *  Method finds the scheduler name associated with a device.
   *
   *  @param isDeviceID <code>String</code> containing device ID of scheduler we need.
   *
   *  @return <code>String</code> containing scheduler name.
   * @throws DBException
   */
  public String getSchedulerName(String isDeviceID) throws DBException
  {
    Object vpObj = mpDevice.getSingleColumnValue(isDeviceID, DeviceData.SCHEDULERNAME_NAME);
    if (vpObj == null)
    {
      throw new DBException("No device found for Device \"" + isDeviceID + "\"");
    }

    return (String)vpObj;
  }

  /**
   *  Method finds the Allocator name associated with a device.
   *
   *  @param isDeviceID <code>String</code> containing device ID of Allocator we need.
   *
   *  @return <code>String</code> containing scheduler name.
   * @throws DBException
   */
  public String getAllocatorName(String isDeviceID) throws DBException
  {
    Object vpObj = mpDevice.getSingleColumnValue(isDeviceID, DeviceData.ALLOCATORNAME_NAME);
    if (vpObj == null)
    {
      throw new DBException("No device found for Device \"" + isDeviceID + "\"");
    }

    return (String)vpObj;
  }

  /**
   * Method get the device that has the token then changes that devices token to
   * false and sets the next devices token to true.
   *
   * @param aisleGroup Group <code>Integer</code> of the token to get.
   *
   * @return <code>String</code> containing device ID with token that is true.
   */
  public String getAndUpdateDeviceToken(int aisleGroup)
  {
    String currentDevice = null;
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      currentDevice = mpDevice.getAndUpdateDeviceToken(aisleGroup);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception Getting Device Token for Aisle Group \""
          + aisleGroup + "\"  - StandardDeviceServer.getDeviceToken");
      currentDevice = null;
    }
    finally
    {
      endTransaction(tt);
    }

    return(currentDevice);
  }

  /**
   * Get a list of all devices that need a controller associated with them.
   * (SRCs, AGCs, ARCs, etc.)  This method is primarily for system startup.
   *
   * @return <code>List<Map></code> containing device data
   * @throws DBException if there is a database access or update error.
   */
  public List<Map> getCtlrDevices() throws DBException
  {
    List<Map> vpDevices = null;
    DeviceData vpDD = Factory.create(DeviceData.class);

    TransactionToken vpTok = null;

    try
    {
      vpTok = startTransaction();
      initConfigServer();
      if (mpConfigServer.isSplitSystem())
      {
        String vsJVMID = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
        String vsJMSTopic = Application.getString(SKDCConstants.JVM_JMSTOPIC_KEY);
                                       // If all JVM IDs are in use we will
                                       // return an error.
        if (vsJVMID != null && mpConfigServer.isAnyDeviceConfiguredForThisJVM())
        {
                                       // Reserve a JVM to use.
          JVMConfigData vpJVMConfData = mpConfigServer.reserveJVM();
          if (vpJVMConfData != null)
          {
            vpDD.setKey(DeviceData.COMMREADPORT_NAME, "", KeyObject.NOT_EQUAL);
            vpDD.setKey(DeviceData.COMMSENDPORT_NAME, "", KeyObject.NOT_EQUAL, KeyObject.OR);
            vpDD.setKey(DeviceData.JVMIDENTIFIER_NAME, vpJVMConfData.getJVMIdentifier());
            vpDD.addOrderByColumn(DeviceData.DEVICEID_NAME);
            vpDevices = mpDevice.getAllElements(vpDD);
          }
          else
          {
            String vsErr = "Invalid configuration for multiple JVM start up. " +
                           "No JVM available for JMS topic " + vsJMSTopic;
            logError(vsErr);
            throw new DBException(vsErr);
          }
        }
        else
        {
          String vsErr = "Invalid configuration for multiple JVM start up. " +
                         "Either no JVM available for JMS topic " + vsJMSTopic +
                         ", or no devices have been assigned to " + vsJVMID;
          logError(vsErr);
          throw new DBException(vsErr);
        }
      }
      else                             // If no JVM Config records exist, use
      {                                // default behaviour.
        vpDD.setKey(DeviceData.COMMREADPORT_NAME, "", KeyObject.NOT_EQUAL);
        vpDD.setKey(DeviceData.COMMSENDPORT_NAME, "", KeyObject.NOT_EQUAL, KeyObject.OR);
        vpDD.addOrderByColumn(DeviceData.DEVICEID_NAME);
        vpDevices = mpDevice.getAllElements(vpDD);
      }
      commitTransaction(vpTok);
    }
    catch(DBException ex)
    {
      logException(ex, "Error while finding devices for startup.");
      throw ex;
    }
    finally
    {
      endTransaction(vpTok);
    }

    return(vpDevices);
  }

  /**
   * Get the operational status of a device
   * @param deviceID
   * @return
   */
  public int getDeviceOperationalStatus(String deviceID)
  {
    int status = 0;
    if (deviceID.length() != 0)
    {
      try
      {
        status = mpDevice.getOperationalStatusValue(deviceID);
      }
      catch (Exception e)
      {
        logException(e, "StandardDeviceServer - Device \"" +
            deviceID + "\" getDeviceOperationalStatus()");
      }
    }
    else
    {
      logError("StandardDeviceServer - Device \"" + deviceID
          + "\" NOT Found - getDeviceOperationalStatus()");
    }

    return status;
  }

 /**
  * Method to get the comm device for a device.
  * @param isDeviceID the device for which to get comm. device.  If this is an
  *        AGC type device there is no comm. device.  If this is an SRC device
  *        on an AGC system the comm. device is the AGC.
  * @return for agc type devices return either an empty string or the agc device
  *         id. itself (depending what is configured). For SRC devices return
  *         whatever is configured.
  * @throws DBException if there is a DB error.
  */
  public String getCommDevice(String isDeviceID) throws DBException
  {
    String vsCommDev;
    Object vpObj = mpDevice.getSingleColumnValue(isDeviceID,
                                                 DeviceData.COMMDEVICE_NAME);
    if (vpObj != null)
    {
      vsCommDev = (String)vpObj;
      if (vsCommDev.trim().length() == 0)
      {
        vsCommDev = isDeviceID;
        logDebug("Warning: no comm device found for device " + isDeviceID);
      }
    }
    else
    {
      vsCommDev = "";
    }

    return(vsCommDev);
  }

  /**
   * Determine which schedulers must collaborate with a given allocator
   * @param isAllocator The name of the allocator to check
   * @return List of Strings representing schedulers
   */
  public List<String> getSchedulersForAllocator(String isAllocator)
  {
    List<String> vpSchedulers = new ArrayList<String>();
    DeviceData vpDD = Factory.create(DeviceData.class);
    vpDD.setKey(DeviceData.ALLOCATORNAME_NAME, isAllocator);
    try
    {
      List<Map> vpList = mpDevice.getAllElements(vpDD);
      for(Map vpMap : vpList)
      {
        vpDD.dataToSKDCData(vpMap);
        String vsSched = vpDD.getSchedulerName();
        if (!vpSchedulers.contains(vsSched))
          vpSchedulers.add(vsSched);
      }
    }
    catch(DBException ex)
    {
      logException(ex, "StandardDeviceServer.getSchedulersForAllocator");
    }
    return vpSchedulers;
  }

  /**
   * Determine which devices must collaborate with a given scheduler
   * @param isScheduler The name of the scheduler to check
   * @return List of Strings representing devices
   */
  public List<String> getDevicesForScheduler(String isScheduler)
  {
    List<String> vpDevices = new ArrayList<String>();
    DeviceData vpDD = Factory.create(DeviceData.class);
    vpDD.setKey(DeviceData.SCHEDULERNAME_NAME, isScheduler);
    vpDD.setKey(DeviceData.COMMSENDPORT_NAME, "", KeyObject.NOT_EQUAL);
    try
    {
      List<DeviceData> vpList = DBHelper.convertData(mpDevice.getAllElements(vpDD),
                                                     DeviceData.class);
      for(DeviceData tpDD : vpList)
      {
        String vsDev = tpDD.getDeviceID();
        if (!vpDevices.contains(vsDev))
          vpDevices.add(vsDev);
      }
    }
    catch(DBException ex)
    {
      logException(ex, "StandardDeviceServer.getDevicesForScheduler");
    }
    return vpDevices;
  }

  /**
   * Method to get the device aisle group.
   * @param isDeviceID the Device.
   */
  public int getDeviceAisleGroup(String isDeviceID) throws DBException
  {
    return mpDevice.getDeviceAisleGroup(isDeviceID);
  }

  /**
   * Get the name of the emulator for a given device
   */
  public String getEmulatorForDevice(String isDeviceID)
  {
    DeviceData vpDD = getDeviceData(isDeviceID);
    String vsEmulator = null;
    while(vsEmulator == null && vpDD != null)
    {
      if(vpDD.getEmulationMode() == DBConstants.FULLEMU)
        vsEmulator = vpDD.getDeviceID() + Controller.EMULATOR;
      else
        vpDD = getDeviceData(vpDD.getCommDevice());
    }
    return vsEmulator;
  }

  protected void initConfigServer()
  {
    if (mpConfigServer == null)
      mpConfigServer = Factory.create(StandardConfigurationServer.class);
  }

  protected void initHostServer()
  {
    if (mpHostServer == null)
      mpHostServer = Factory.create(StandardHostServer.class);
  }

  /**
   * Log a Modify Transaction History record
   *
   * @param ipOldData <code>String</code> containing data of old Device record.
   * @param ipNewData <code>String</code> containing data of new Device record.
   *
   * @throws <code>DBException</code> if a database add error.
   */
  protected void logModifyTransaction(DeviceData ipOldData, DeviceData ipNewData)
      throws DBException
  {
    // We only log the transaction if there is any change
    if (ipNewData.getColumnCount() > 0)
    {
      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.MODIFY);
      tnData.setDeviceID(ipNewData.getDeviceID());
      if (logDataChanged(ipOldData, ipNewData) == true)
      {
        logTransaction(tnData);
      }
    }
  }

/*============================================================================
 *                       AGV specific transactions
 *============================================================================*/
 /**
  * Method to get the Formatting sequence number for a new message.
  * @param isSequencerName The sequence number name.  Correct values are
  * VehicleMove.INBOUND_SEQUENCER, VehicleMove.OUTBOUND_SEQUENCER or
  * VehicleMove.
  * @return
  * @throws DBException if there is a database access error.
  */
  public int generateAGVMessageSequence(String isSequencerName) throws DBException
  {
    int vnNewSeq;
    TransactionToken vpTok = null;

    try
    {
      vpTok = startTransaction();
      vnNewSeq = mpSequencer.changeSequenceNumber(isSequencerName,
                                                  "AGVController",
                                                  DBConstants.DEVICE_SEQ);
      commitTransaction(vpTok);
    }
    catch(DBException exc)
    {
      throw new DBException("Error generating sequence number.");
    }
    finally
    {
      endTransaction(vpTok);
    }

    return(vnNewSeq);
  }

 /**
  * Method to generate a random number based request ID. for an AGV move.
  * @return the request ID. the request ID.
  */
  public String generateRequestID()
  {
    Random vpRand = new Random();
    String vsRequestID = "";

    do
    {
      vsRequestID = "TRAN-" + vpRand.nextInt(99999);
      mpVMData.clear();
      mpVMData.setKey(VehicleMoveData.REQUESTID_NAME, mpVMData);
    } while(mpVehicleMove.exists(mpVMData));

    return(vsRequestID);
  }

 /**
  * Method to get a AGV move record using the unique sequence number.
  * @param inSequenceNumber the sequence number.
  * @return {@code null} if no records found, otherwise a reference to a
  * VehicleMoveData object.
  * @throws DBException if there is a DB access error.
  */
  public VehicleMoveData getAGVMoveRecord(int inSequenceNumber)
         throws DBException
  {
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.SEQUENCENUMBER_NAME, inSequenceNumber);

    return(mpVehicleMove.getElement(mpVMData, DBConstants.NOWRITELOCK));
  }

 /**
  * Method to get a AGV move record using the unique sequence number.
  * @param isLoad the Load ID of agv move.
  * @return {@code null} if no records found, otherwise a reference to a
  * VehicleMoveData object.
  * @throws DBException if there is a DB access error.
  */
  public VehicleMoveData getAGVMoveRecord(String isLoad)
         throws DBException
  {
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.LOADID_NAME, isLoad);

    return(mpVehicleMove.getElement(mpVMData, DBConstants.NOWRITELOCK));
  }

 /**
  * Method fetches current AGV Load status.
  * @param isAGVLoadID the AGV load id.
  * @return integer containing load status.
  * @throws DBException  if there is a database access error.
  */
  public int getAGVMoveStatus(String isAGVLoadID) throws DBException
  {
    Object vpLoadStat = mpVehicleMove.getSingleColumnValue(isAGVLoadID,
                                                VehicleMoveData.AGVLOADSTATUS_NAME);
    return(vpLoadStat != null ? (Integer)vpLoadStat : 0);
  }

 /**
  * Method to get back a List of VehicleMove records.  List will be in the oldest
  * to the newest record order.
  * @param ianStatus the move status by which to search.  <b>If no status is
  * provided all move records are returned</b>
  * @return
  * @throws DBException
  */
  public List<Map> getAllAGVMoveRecordsByStatus(int... ianStatus) throws DBException
  {
    mpVMData.clear();
    if (ianStatus.length > 1)
    {
      Integer[] vapStatuses = SKDCUtility.getVarArgIntegerArray(ianStatus);
      mpVMData.setInKey(VehicleMoveData.AGVLOADSTATUS_NAME, 0, (Object[])vapStatuses);
    }
    else if (ianStatus.length == 1)
    {
      mpVMData.setKey(VehicleMoveData.AGVLOADSTATUS_NAME, ianStatus[0]);
    }

    mpVMData.setOrderByColumns(VehicleMoveData.STATUSCHANGETIME_NAME);
    return(mpVehicleMove.getAllElements(mpVMData));
  }

    /**
   * Gets all defined vehicle paths for a given path number.
   * @param idPathNumber if set to -1 get all paths ordered by path number.
   * @return
   * @throws DBException
   */
  public List<VehiclePathsData> getVehiclePaths(double idPathNumber) throws DBException
  {
    return mpVPath.getVehiclePaths(idPathNumber);
  }

  /**
   * Gets all paths with same originating station.
   *
   * @param isFromStation
   * @return List&lt;VehiclePathsData&gt;
   * @throws DBException
   */
  public List<VehiclePathsData> getVehiclePaths(String isFromStation) throws DBException
  {
    return mpVPath.getVehiclePaths(isFromStation);
  }

  /**
   * Get all paths with common destination station.
   *
   * @param isToStation dest. station
   * @return List&lt;VehiclePathsData&gt;
   * @throws com.daifukuamerica.wrxj.jdbc.DBException
   */
  public List<VehiclePathsData> getVehiclePathsCommonDest(String isToStation) throws DBException
  {
    return mpVPath.getVehiclePathsCommonDest(isToStation);
  }

  /**
   * Method to display AGV path segments.
   * @return array of path segments with a hyphen in between from and to-stations.
   * @throws DBException
   */
  public String[] getAllAGVPathsForDisplay() throws DBException
  {
    List<VehiclePathsData> vpDataList = mpVPath.getVehiclePaths(-1.0);
    String[] vasFromStn = DBHelper.getArray(vpDataList, String.class,
                                            VehiclePathsData.FROM_STATION_NAME);
    String[] vasToStn = DBHelper.getArray(vpDataList, String.class,
                                            VehiclePathsData.TO_STATION_NAME);

    String[] vasDisplay = new String[vasFromStn.length];
    for(int vnIdx = 0; vnIdx < vasFromStn.length; vnIdx++)
    {
      vasDisplay[vnIdx] = vasFromStn[vnIdx] + " - " + vasToStn[vnIdx];
    }

    return vasDisplay;
  }

 /**
  * Method to update an AGV Move record's sequence numner to the latest value.
  * This helps us avoid out of sequence errors from CMS.
  * @param isLoadID the load ID of the move.
  * @return the sequence number to which this move will be updated.
  * @throws DBException if there is a database access error.
  */
  public int updateAGVMoveSequence(String isLoadID) throws DBException
  {
    int vnNewSeq = 0;
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpVMData.clear();
      mpVMData.setKey(VehicleMoveData.LOADID_NAME, isLoadID);

      vnNewSeq = generateAGVMessageSequence(VehicleMove.OUTBOUND_SEQUENCER);
      mpVMData.setSequenceNumber(vnNewSeq);
      mpVehicleMove.modifyElement(mpVMData);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }

    return(vnNewSeq);
  }

/**
  * Method to update an AGV Command record's sequence numner to the latest value.
  * This helps us avoid out of sequence errors from CMS.
  * @param inOldSeq the old sequence number for this record.
  * @return the sequence number to which this move will be updated.
  * @throws DBException if there is a database access error.
  */
  public int updateAGVCmdSequence(int inOldSeq) throws DBException
  {
    int vnNewSeq = 0;
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpVSCmdData.clear();
      mpVSCmdData.setKey(VehicleSystemCmdData.SEQUENCENUMBER_NAME, inOldSeq);

      vnNewSeq = generateAGVMessageSequence(VehicleMove.OUTBOUND_SEQUENCER);
      mpVSCmdData.setSequenceNumber(vnNewSeq);
      mpVSCmd.modifyElement(mpVSCmdData);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }

    return(vnNewSeq);
  }

 /**
  * Method to update the AGV move status based on Sequence number.
  * @param inSeqNum the sequence number
  * @param inAGVMoveStatus the new move status
  * @throws DBException
  */
  public void updateAGVMoveStatus(int inSeqNum, int inAGVMoveStatus)
         throws DBException
  {
    mpVMData.clear();

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpVMData.setKey(VehicleMoveData.SEQUENCENUMBER_NAME, inSeqNum);
      VehicleMoveData vpVMData = mpVehicleMove.getElement(mpVMData,
                                                         DBConstants.WRITELOCK);
      if (vpVMData != null)
      {
        if (inAGVMoveStatus == DBConstants.AGV_MOVECANCELREQUEST)
        {
          if (vpVMData.getAGVLoadStatus() == DBConstants.AGV_NOMOVE ||
              vpVMData.getAGVLoadStatus() == DBConstants.AGV_MOVEPENDING ||
              vpVMData.getAGVLoadStatus() == DBConstants.AGV_MOVECANCELREQUEST)
          {                            // Obtain a new sequence number since
                                       // this will be required when submitting the
                                       // cancel request.
            int vnSeq = generateAGVMessageSequence(VehicleMove.OUTBOUND_SEQUENCER);
            mpVMData.setAGVLoadStatus(inAGVMoveStatus);
            mpVMData.setSequenceNumber(vnSeq);
            mpVMData.setStatusChangeTime(new Date());
            mpVehicleMove.modifyElement(mpVMData);
          }
          else
          {                            // This shouldn't happen since the caller
                                       // should've already verified.
            throw new DBException("AGV Move cannot be cancelled at this point!");
          }
        }
        else
        {
          mpVMData.setAGVLoadStatus(inAGVMoveStatus);
          mpVMData.setStatusChangeTime(new Date());
          mpVehicleMove.modifyElement(mpVMData);
        }
      }
      else
      {
        throw new DBException("AGV Move record not found for " +
                              "modification! Sequence " + inSeqNum);
      }
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Updates the load status of the load bound to some vehicle.
  * @param isAGVLoad the load id.
  * @param inAGVMoveStatus the new load status
  * @throws DBException
  */
  public void updateAGVMoveStatus(String isAGVLoad, int inAGVMoveStatus)
         throws DBException
  {
    Object vpObj = mpVehicleMove.getSingleColumnValue(isAGVLoad,
                                          VehicleMoveData.SEQUENCENUMBER_NAME);
    if (vpObj != null)
    {
      int vnSeq = (Integer)vpObj;
      updateAGVMoveStatus(vnSeq, inAGVMoveStatus);
    }
    else
    {
      throw new DBException("AGV Move record not found for " +
                            "modification! Load " + isAGVLoad);
    }
  }

 /**
  * Update the current station/location of the AGV load.
  * @param isAGVLoad the load.
  * @param isAGVStation the current station/location
  * @throws DBException if there is a database error.
  */
  public void updateCurrAGVLoadLocn(String isAGVLoad, String isAGVStation)
         throws DBException
  {
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.LOADID_NAME, isAGVLoad);
    mpVMData.setCurrentStation(isAGVStation);
    mpVMData.setDestStation("");
    mpVMData.setStatusChangeTime(new Date());

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpVehicleMove.modifyElement(mpVMData);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Method checks if there is request ID. bound to a vehicle and load
  * combination.
  * @param isLoadID the load id.
  * @param isRequestID the request id. bound to this load.
  * @return {@code true} if a vehicle and load exists with this request id.
  */
  public boolean doesRequestIDExists(String isLoadID, String isRequestID)
  {
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.LOADID_NAME, isLoadID);
    mpVMData.setKey(VehicleMoveData.REQUESTID_NAME, isRequestID);
    return(mpVehicleMove.exists(mpVMData));
  }

 /**
  * Method checks if a AGV load exists.
  * @param isLoadID the agv load.
  * @return {@code true} if Agv Load exists.
  */
  public boolean agvLoadExists(String isLoadID)
  {
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.LOADID_NAME, isLoadID);

    return(mpVehicleMove.exists(mpVMData));
  }

  public boolean agvMoveSequenceExists(int inSeq)
  {
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.SEQUENCENUMBER_NAME, inSeq);

    return(mpVehicleMove.exists(mpVMData));
  }

  /**
   * Checks if there is an AGV move from specified station.
   * @param isFromStation the current station.
   * @return
   */
  public boolean agvMoveExists(String isFromStation)
  {
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.CURRSTATION_NAME, isFromStation);

    return(mpVehicleMove.exists(mpVMData));
  }

 /**
  * Method to add an AGV record.
  * @param ipVHData the data to add.
  * @throws DBException if there is a record add error.
  */
  public void addAGVRecord(VehicleMoveData ipVHData) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      /**
       * Get sequence number under same transaction and set it for this record.
       */
      ipVHData.setSequenceNumber(generateAGVMessageSequence(VehicleMove.OUTBOUND_SEQUENCER));
      mpVehicleMove.addElement(ipVHData);
      logAGVMoveTransaction(DBConstants.TRANSFER, ipVHData.getLoadID(),
                            ipVHData.getCurrentStation(),
                            ipVHData.getDestStation(), ipVHData.getAGVLoadStatus(),
                            ipVHData.getSequenceNumber());

      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Method to delete all agv move records of a user-specified status.
  * @param ianStatus Status' of AGV Move records to delete.
  * @throws DBException if there is a database error.
  */
  public void deleteAGVMoveRecordsByStatus(int... ianStatus) throws DBException
  {
    if (ianStatus.length == 0) return;

    TransactionToken vpTok = null;
    mpVMData.clear();
    if (ianStatus.length > 1)
    {
      Integer[] vapStatuses = SKDCUtility.getVarArgIntegerArray(ianStatus);
      mpVMData.setInKey(VehicleMoveData.AGVLOADSTATUS_NAME, 0, (Object[])vapStatuses);
    }
    else
    {
      mpVMData.setKey(VehicleMoveData.AGVLOADSTATUS_NAME, ianStatus[0]);
    }

    try
    {
      List<Map> vpList = mpVehicleMove.getAllElements(mpVMData);

      vpTok = startTransaction();
      for(Map vpMap : vpList)
      {
        int vnSeq = DBHelper.getIntegerField(vpMap, VehicleMoveData.SEQUENCENUMBER_NAME);
        String vsLoadID = DBHelper.getStringField(vpMap, VehicleMoveData.LOADID_NAME);
        String vsCurrStn = DBHelper.getStringField(vpMap, VehicleMoveData.CURRSTATION_NAME);
        String vsDestStn = DBHelper.getStringField(vpMap, VehicleMoveData.DESTSTATION_NAME);
        int vnLoadStat = DBHelper.getIntegerField(vpMap, VehicleMoveData.AGVLOADSTATUS_NAME);

        mpVMData.clear();
        mpVMData.setKey(VehicleMoveData.SEQUENCENUMBER_NAME, Integer.valueOf(vnSeq));
        mpVehicleMove.deleteElement(mpVMData);

        logAGVMoveTransaction(DBConstants.DELETE, vsLoadID, vsCurrStn, vsDestStn,
                              vnLoadStat, vnSeq);
      }
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Delete AGV Move record by sequence number.
  * @param inSequence the sequence of the AGV move record.
  * @throws DBException if there is a Database error.
  */
  public void deleteAGVMoveRecord(int inSequence) throws DBException
  {
    mpVMData.clear();
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpVMData.setKey(VehicleMoveData.SEQUENCENUMBER_NAME, inSequence);
      VehicleMoveData vpData = mpVehicleMove.getElement(mpVMData, DBConstants.WRITELOCK);
      mpVehicleMove.deleteElement(mpVMData);

      logAGVMoveTransaction(DBConstants.DELETE, vpData.getLoadID(), vpData.getCurrentStation(),
                 vpData.getDestStation(), vpData.getAGVLoadStatus(), vpData.getSequenceNumber());
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  public void deleteAGVMoveRecordByLoadID(String isLoadID) throws DBException
  {
    TransactionToken vpTok = null;
    mpVMData.clear();
    mpVMData.setKey(VehicleMoveData.LOADID_NAME, isLoadID);
    try
    {
      vpTok = startTransaction();
      VehicleMoveData vpData = mpVehicleMove.getElement(mpVMData, DBConstants.WRITELOCK);
      if (vpData != null)
      {
        mpVehicleMove.deleteElement(mpVMData);
        logAGVMoveTransaction(DBConstants.DELETE, vpData.getLoadID(),
                              vpData.getCurrentStation(), vpData.getDestStation(),
                              vpData.getAGVLoadStatus(), vpData.getSequenceNumber());
        commitTransaction(vpTok);
      }
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  public void updateAGVRecord(VehicleMoveData ipModData) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpVehicleMove.modifyElement(ipModData);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Method marks all incomplete AGV moves as Move Error so that they may be
  * manually recovered at a later time.  This normally happens when there
  * is CMS controller "Cold-Start" and it loses all tracking data.
  *
  * @throws DBException if there is a databse update error.
  */
  public void markAGVMovesForRecovery() throws DBException
  {
    mpVMData.clear();
    mpVMData.setInKey(VehicleMoveData.AGVLOADSTATUS_NAME, 0,
                      DBConstants.AGV_MOVEPENDING,
                      DBConstants.AGV_MOVING,
                      DBConstants.AGV_MOVECANCELREQUEST,
                      DBConstants.AGV_MOVECANCELPENDING);
    if (mpVehicleMove.exists(mpVMData))
    {
      TransactionToken vpTok = null;
      try
      {
        vpTok = startTransaction();
        mpVMData.setAGVLoadStatus(DBConstants.AGV_RECOVERABLE);
        mpVehicleMove.modifyElement(mpVMData);
        commitTransaction(vpTok);
      }
      finally
      {
        endTransaction(vpTok);
      }
    }
  }

 /**
  * Method to get an AGV command record.
  * @param inSequenceNum the sequence number used as key.
  * @return {@code null} if no record found.
  * @throws DBException if there is a database error.
  */
  public VehicleSystemCmdData getAGVCommandRecord(int inSequenceNum)
         throws DBException
  {
    mpVSCmdData.clear();
    mpVSCmdData.setKey(VehicleSystemCmdData.SEQUENCENUMBER_NAME, inSequenceNum);

    return(mpVSCmd.getElement(mpVSCmdData, DBConstants.NOWRITELOCK));
  }

 /**
  * Method to get back a List of VehicleSystemCmd records.  List will be in the oldest
  * to the newest record order.
  * @param ianStatus the command status by which to search.  <b>If no status is
  * provided all command records are returned</b>
  * @return
  * @throws DBException
  */
  public List<Map> getAllAGVCommandRecordsByStatus(int... ianStatus) throws DBException
  {
    mpVSCmdData.clear();

    if (ianStatus.length > 1)
    {
      Integer[] vapStatuses = SKDCUtility.getVarArgIntegerArray(ianStatus);
      mpVSCmdData.setInKey(VehicleSystemCmdData.COMMANDSTATUS_NAME, 0,
                           (Object[])vapStatuses);
    }
    else if (ianStatus.length == 1)
    {
      mpVSCmdData.setKey(VehicleSystemCmdData.COMMANDSTATUS_NAME, ianStatus[0]);
    }

    mpVSCmdData.setOrderByColumns(VehicleSystemCmdData.STATUSCHANGETIME_NAME);
    return(mpVSCmd.getAllElements(mpVSCmdData));
  }


 /**
  * Method to get an AGV command record.
  * @param isMessageID the message id.
  * @param isCommandValue optional argument to specify the command value in
  *        the lookup.
  * @return {@code null} if no record found.
  * @throws DBException if there is a database error.
  */
  public List<VehicleSystemCmdData> getAGVCommandRecords(String isMessageID,
                                                         String isCommandValue)
         throws DBException
  {
    mpVSCmdData.clear();
    mpVSCmdData.setKey(VehicleSystemCmdData.SYSTEMMESSAGEID_NAME, isMessageID);
    if (isCommandValue != null && !isCommandValue.isEmpty())
      mpVSCmdData.setKey(VehicleSystemCmdData.COMMANDVALUE_NAME, isCommandValue);

    List<Map> vpList = mpVSCmd.getAllElements(mpVSCmdData);
    return(DBHelper.convertData(vpList, VehicleSystemCmdData.class));
  }

 /**
  * Method to add an AGV Command Record.
  * @param ipVSData the data to add.
  * @throws DBException if there is a record add error.
  */
  public void addAGVCommandRecord(VehicleSystemCmdData ipVSData) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      /**
       * Get sequence number under same transaction and set it for this record.
       */
      ipVSData.setSequenceNumber(generateAGVMessageSequence(VehicleMove.OUTBOUND_SEQUENCER));
      mpVSCmd.addElement(ipVSData);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Method checks if a agv command exists.
  * @param inSeq the sequence.
  * @return {@code true} if command sequence exists.
  */
  public boolean agvCommandSequenceExists(int inSeq)
  {
    mpVSCmdData.clear();
    mpVSCmdData.setKey(VehicleSystemCmdData.SEQUENCENUMBER_NAME, inSeq);

    return(mpVSCmd.exists(mpVSCmdData));
  }

 /**
  * Method to delete all agv move records of a user-specified status.
  * @param ianStatus Status' of AGV Command records to delete.
  * @throws DBException
  */
  public void deleteAGVCommandRecordsByStatus(int... ianStatus) throws DBException
  {
    if (ianStatus.length == 0) return;

    TransactionToken vpTok = null;
    mpVSCmdData.clear();
    if (ianStatus.length > 1)
    {
      Integer[] vapStatuses = SKDCUtility.getVarArgIntegerArray(ianStatus);
      mpVSCmdData.setInKey(VehicleSystemCmdData.COMMANDSTATUS_NAME, 0,
                           (Object[])vapStatuses);
    }
    else
    {
      mpVSCmdData.setKey(VehicleSystemCmdData.COMMANDSTATUS_NAME, ianStatus[0]);
    }

    try
    {
      vpTok = startTransaction();
      mpVehicleMove.deleteElement(mpVSCmdData);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Updates the load status of the load bound to some vehicle.
  * @param inSeqNum the sequence number.
  * @param inNewStatus the new command status
  * @throws DBException if there is databas error.
  */
  public void updateAGVCommandStatus(int inSeqNum, int inNewStatus)
         throws DBException
  {
    mpVSCmdData.clear();
    mpVSCmdData.setKey(VehicleSystemCmdData.SEQUENCENUMBER_NAME, inSeqNum);
    mpVSCmdData.setCommandStatus(inNewStatus);
    mpVSCmdData.setStatusChangeTime(new Date());

    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      mpVSCmd.modifyElement(mpVSCmdData);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /*========================================================================*/
  /*  TRANSACTION HISTORY                                                   */
  /*========================================================================*/
  /**
   * Log AGV Move
   *
   * @param inTranType
   * @param isLoadID The load being transferred.
   * @param isFromStation the pickup agv station
   * @param isToStation the drop-off agv station.
   * @param inSequenceNumber Sequence Number assigned to the move.
   * @param inAGVLoadStatus
   */
  public void logAGVMoveTransaction(int inTranType, String isLoadID, String isFromStation,
                 String isToStation, int inAGVLoadStatus, int inSequenceNumber)
  {
    String vsAGVLoadStatus = "";
    String vsWarehouse = "";

    try
    {
      vsAGVLoadStatus = DBTrans.getStringValue(VehicleMoveData.AGVLOADSTATUS_NAME,
                                               inAGVLoadStatus);
      vsWarehouse = mpStation.getStationWarehouse(isFromStation);

      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(inTranType);
      tnData.setLoadID(isLoadID);
      tnData.setLocation(vsWarehouse, isFromStation);
      tnData.setToLocation(vsWarehouse, isToStation);
      tnData.setStation(isFromStation);
      tnData.setToStation(isToStation);
      tnData.setActionDescription("Move Status: " + vsAGVLoadStatus);
      logTransaction(tnData);
    }
    catch(NoSuchFieldException nsf)
    {
      logError("Field " + VehicleMoveData.AGVLOADSTATUS_NAME +
               " has invalid translation value! " + nsf.getMessage());
    }
    catch(DBException e)
    {
      logError("Station " + isFromStation + " has no assigned warehouse! " +
               e.getMessage());
    }
  }

  protected void sendHostDeviceStatus(String isDevID, int inStatus)
  {
    try
    {
      initHostServer();
      String vsDevStat = DBTrans.getStringValue(DeviceData.PHYSICALSTATUS_NAME, inStatus);
      mpHostServer.sendDeviceStatus(isDevID, vsDevStat);
    }
    catch(NoSuchFieldException nsf)
    {
      logException("Translation error for field " +
                   DeviceData.PHYSICALSTATUS_NAME +
                   ", Value to be translated: " + inStatus, nsf);
    }
    catch(DBException exc)
    {
      logError("Error sending Host Device Status message!  " + exc.getMessage());
    }
  }
}
