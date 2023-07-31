package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

/*
                     Daifuku America Corporation
                         International Center
                     5202 Douglas Corrigan Way
                  Salt Lake City, Utah  84116-3192
                          (801) 359-9900

   This software is furnished under a license and may be used and copied only in
   accordance with the terms of such license.  This software or any other copies
   thereof in any form, may not be provided or otherwise made available, to any
   other person or company without written consent from SKDC Corporation.

   Daifuku America Corporation assumes no responsibility for the use or
   reliability of software which has been modified without approval.
*/


/**
 *  Transaction server for Station based changes to the database.
 *
 * <p><b>Details:</b> No details available.</p>
 */
public class StandardStationServer extends StandardServer
{
  protected boolean stagedStations = true;
  protected String keyName;

  protected StandardDeviceServer mpDevServer = null;
  protected StandardConfigurationServer mpConfigServ = null;

  protected Station mpStation = Factory.create(Station.class);
  protected StationData mpStnData = Factory.create(StationData.class);
  private Device      mpDevice = Factory.create(Device.class);
  private Load        mpLoad   = Factory.create(Load.class);

  protected TableJoin mpTJ = Factory.create(TableJoin.class);

  /**
   * Constructor
   */
  public StandardStationServer()
  {
    this(null);
  }

  /**
   * Constructor
   *
   * @param isKeyName
   */
  public StandardStationServer(String isKeyName)
  {
    super(isKeyName);
    keyName = isKeyName;
  }

  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardStationServer(String isKeyName, DBObject dbo)
  {
	  super(isKeyName, dbo);
	  keyName = isKeyName;
  }

  /**
   * Clean up
   *
   * @see com.daifukuamerica.wrxj.dataserver.standard.StandardServer#cleanUp()
   */
  @Override
  public void cleanUp()
  {
    if (mpDevServer != null)
    {
      mpDevServer.cleanUp();
      mpDevServer = null;
    }

    super.cleanUp();
  }

  /**
   * Add a station to the database
   * @param stationData to add
   * @throws DBException
   */
  public void addStation(StationData stationData) throws DBException
  {

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      LocationData lcdata = Factory.create(LocationData.class);
      lcdata.setWarehouse(stationData.getWarehouse());
      lcdata.setAddress(stationData.getStationName());
      lcdata.setDeviceID(stationData.getDeviceID());
      lcdata.setAllowDeletion(DBConstants.NO);
      lcdata.setLocationType(DBConstants.LCSTATION);
      lcdata.setAisleGroup(mpDevice.getDeviceAisleGroup(stationData.getDeviceID()));
      lcdata.setKey(LocationData.WAREHOUSE_NAME, lcdata.getWarehouse());
      lcdata.setKey(LocationData.ADDRESS_NAME, lcdata.getAddress());
      Location location = Factory.create(Location.class);
      if (location.exists(lcdata))
      {
        DBHelper.dbThrow("Location already exists for this station");
      }
      location.addElement(lcdata);
      mpStation.createStation(stationData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Returns the reject route for a station
   * @param stationName
   * @return rejectRoutre
   */
  public String getRejectStation(String stationName)
  {
    try
    {
      StationData stationData = getStation(stationName);
      if(stationData != null)
      {
        StandardRouteServer vpRouteServ = Factory.create(StandardRouteServer.class);
        return vpRouteServ.getNextRouteDest(stationData.getRejectRoute(), stationName);
      }
      else
      {
        return null;
      }
    }
    catch (DBException ex)
    {
      logException("Error getting reject for station " + stationName, ex);
      return null;
    }
  }

  /**
   * Get the station data for a given station
   * @param stationName to get the
   * @return StationData or null if the station doesn't exist.
   */
  public StationData getStation(String stationName)
  {
    try
    {
      return mpStation.getStationData(stationName);
    }
    catch (Exception e)
    {
      logException(e, "Exception getting Station  \"" + stationName
          + "\"  - StandardStationServer.getStation");
      return null;
    }
  }

  /**
   * Get a list of station records that are for a certain device
   * @param deviceName to get output stations for
   * @return List of output station records
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getStationByDeviceList(String deviceID) throws DBException
  {
    return mpStation.getDeviceStationData(deviceID);
  }

  /**
   * Get a list of station records by aisle group
   * @param <code>StationData</code> whose aisle group they should be on
   * @return stationRecord
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  @UnusedMethod
  public List<Map> getListOfStationsByAisleGroup(StationData ipSD) throws DBException
  {
    int vnAisleGroup = getStationAisleGroup(ipSD.getStationName());
    return mpStation.getListOfStationsByAislegroup(vnAisleGroup);
  }

  /**
   * Gets a list of station records like stationName
   * @param stationName <code>String</code>to get a list of stations like
   * @return List of stations records
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getStationDataListByStation(String stationName) throws DBException
  {
    return mpStation.getStationDataListByStation(stationName);
  }

  /**
   * Get a list of stations
   * @return list of stations <code>String</code>
   * @throws DBException
   */
  public List<String> getStationNameList() throws DBException
  {
    return getStationNameList("");
  }

  /**
   * Get a list of stations like the srch.
   * @param srch to find stations like
   * @return list of stations <code>String</code>
   * @throws DBException
   */
  public List<String> getStationNameList(String srch) throws DBException
  {
    return mpStation.getStationNameList(srch);
  }

  /**
   * Get a list of stations by there type
   * @param stationTypes types of stations to get
   * @return <code>String[]</code> of all the stations that have the types
   * @throws DBException
   */
  @UnusedMethod
  public String[] getStationNameListByStationType(int[] stationTypes) throws DBException
  {
    return mpStation.getStationNameListByStationType(stationTypes);
  }

  /**
   * Get a list of station records by types
   * @param stationTypes <code>int</code> of stations to get list of.
   * If null or empty default to non-input type stations.
   * @return Map of station records
   * @throws DBException
   */
  public Map<String,String> getStationsByStationType(int[] stationTypes) throws DBException
  {
    return getStationsByStationType(stationTypes, SKDCConstants.NO_PREPENDER);
  }

 /**
  * Get a list of station records by types
  * @param stationTypes <code>int</code> of stations to get list of.
  * If null or empty default to non-input type stations.
  * @param isFirstElement One of the following constants:<ul>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#ALL_STRING
  *              SKDCConstants.ALL_STRING}
  *            which prepends the string "ALL" to the array.</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NONE_STRING
  *              SKDCConstants.NONE_STRING}
  *            which prepends the string "NONE" to the array</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#EMPTY_VALUE
  *              SKDCConstants.EMPTY_VALUE}
  *            which prepends a blank string to the array.</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NO_PREPENDER
  *              SKDCConstants.NO_PREPENDER}
  *            which means there is no prepender (no pre-defined first element)
  *            to the list.</li></ul>
  * @return Map&lt;&quot;Station Desc.&quot;, &nbsp;&quot;Station Name&quot;&gt;
  * @throws DBException if there is a DB access error.
  */
  public Map<String, String> getStationsByStationType(int[] stationTypes,
                                                    final String isFirstElement)
         throws DBException
  {
    initializeConfigServer();
    if (mpConfigServ.isSplitSystem())
    {
      String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
      return mpTJ.getStationsByStationType(stationTypes, vsJVMId, isFirstElement);
    }
    else
    {
      return mpStation.getStationsByStationType(stationTypes, isFirstElement);
    }
  }

 /**
  * Gets all stations by station type but ignores if using multiple JVMs.  This
  * can be useful for screens that want to show a full system view regardless of JVMs.
  * @param stationTypes <code>int</code> of stations to get list of.
  * If null or empty default to non-input type stations.
  * @param isFirstElement One of the following constants:<ul>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#ALL_STRING
  *              SKDCConstants.ALL_STRING}
  *            which prepends the string "ALL" to the array.</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NONE_STRING
  *              SKDCConstants.NONE_STRING}
  *            which prepends the string "NONE" to the array</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#EMPTY_VALUE
  *              SKDCConstants.EMPTY_VALUE}
  *            which prepends a blank string to the array.</li>
  *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NO_PREPENDER
  *              SKDCConstants.NO_PREPENDER}
  *            which means there is no prepender (no pre-defined first element)
  *            to the list.</li></ul>
  * @return Map&lt;&quot;Station Desc.&quot;, &nbsp;&quot;Station Name&quot;&gt;
  * @throws DBException if there is a DB access error.
  */
  public Map<String, String> getAllSystemStations(int[] stationTypes,
                                                  final String isFirstElement)
         throws DBException
  {
    return mpStation.getStationsByStationType(stationTypes, isFirstElement);
  }

  /**
   * validate that a station exists
   * @param name <code>String</code> station to check
   * @return True if the station exist
   */
  public boolean exists(String name)
  {
    try
    {
      return mpStation.doesStationExist(name);
    }
    catch (DBException e)
    {
      return false;
    }
  }

  /**
   * Updates the station data in the database without any checking for station
   * warehouse changes.
   *
   * @param sd StationData to update record with
   * @throws DBException
   */
  public void modifyStationRecord(StationData ipNewData) throws DBException
  {
    StationData vpOldData = getStation(ipNewData.getStationName());
    modifyStationRecord(vpOldData, ipNewData);
  }

  /**
   * Modify a station
   *
   * @param ipOldData
   * @param ipNewData
   * @throws DBException
   */
  private void modifyStationRecord(StationData ipOldData, StationData ipNewData)
      throws DBException
  {
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
      logModifyTransaction(ipOldData, ipNewData);
      mpStation.updateStation(ipNewData);
      commitTransaction(ttok);
    }
    catch(NoSuchElementException noElem)
    {
      KeyObject stnKey = ipNewData.getKeyObject(StationData.STATIONNAME_NAME);
      throw new DBException("Station " + (String)stnKey.getColumnValue() +
                            " not updated!", noElem);
    }
    finally
    {
      endTransaction(ttok);
    }
  }

  /**
   * Update all of the station data to the sd data
   * @param sd <code>StationData</code> to update to
   * @throws DBException
   */
  public void updateStation(StationData sd) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      Location location = Factory.create(Location.class);
      tt = startTransaction();
      // if warehouse has changed then delete the matching location and re-add it
      // in the new warehouse
      LocationData lcdata = Factory.create(LocationData.class);
      lcdata.setWarehouse(sd.getWarehouse());
      lcdata.setAddress(sd.getStationName());
      lcdata.setDeviceID(sd.getDeviceID());
      lcdata.setKey(LocationData.WAREHOUSE_NAME, lcdata.getWarehouse());
      lcdata.setKey(LocationData.ADDRESS_NAME, lcdata.getAddress());
      lcdata.setKey(LocationData.DEVICEID_NAME, lcdata.getDeviceID());
      //
      // ONLY update the Station's Location if the Warehouse, Location, or
      // DeviceID has changed.
      //
      if (! location.exists(lcdata))
      {
         // get the old station data.
        StationData tmpSD = getStation(sd.getStationName());
        lcdata.clear();
        lcdata.setWarehouse(tmpSD.getWarehouse());
        lcdata.setAddress(tmpSD.getStationName());
        lcdata.setKey(LocationData.WAREHOUSE_NAME, lcdata.getWarehouse());
        lcdata.setKey(LocationData.ADDRESS_NAME, lcdata.getAddress());
        //
        // Delete the Station's previous Location.
        //
        try
        {
          location.deleteElement(lcdata);
        }
        catch (NoSuchElementException nsee)
        {
          logError("Old location for station [" + sd.getStationName()
              + "] not found!");
        }
        //
        lcdata.setWarehouse(sd.getWarehouse());
        lcdata.setAddress(sd.getStationName());
        lcdata.setAllowDeletion(DBConstants.NO);
        lcdata.setLocationType(DBConstants.LCSTATION);
        lcdata.setAisleGroup(getStationAisleGroup(sd.getStationName()));
        lcdata.setDeviceID(sd.getDeviceID());
        lcdata.setKey(LocationData.WAREHOUSE_NAME, lcdata.getWarehouse());
        lcdata.setKey(LocationData.ADDRESS_NAME, lcdata.getAddress());
        if (location.exists(lcdata))
        {
          DBHelper.dbThrow("New Location already exists for this station");
        }
        location.addElement(lcdata);
      }
      modifyStationRecord(sd);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Deletes the station and the location station in the location table
   * @param stationName <code>String</code> to delete
   * @throws DBException if can't delete it
   */
  public void deleteStation(String stationName) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      // delete the matching location then the station
      StationData stationData = getStation(stationName);
      LocationData lcdata = Factory.create(LocationData.class);
      lcdata.setWarehouse(stationData.getWarehouse());
      lcdata.setAddress(stationData.getStationName());
      lcdata.setKey(LocationData.WAREHOUSE_NAME, lcdata.getWarehouse());
      lcdata.setKey(LocationData.ADDRESS_NAME, lcdata.getAddress());
      Location location = Factory.create(Location.class);
      location.deleteElement(lcdata);
      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.DELETE);
      tnData.setLocation(stationName);
      logTransaction(tnData);

      mpStation.deleteStation(stationName);
      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.DELETE);
      tnData.setStation(stationName);
      logTransaction(tnData);

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Return a list of all my stations that this scheduler is storing loads to
   * @param schedulerName
   * @return
   */
  @UnusedMethod
  public List<StationData> getSchedulersStoreStations(String schedulerName)
  {
    try
    {
      return mpStation.getStoreStationsBySchedulerName(schedulerName);
    }
    catch (DBException e)
    {
      logException(e, "StandardStationServer-->getSchedulersStoreStations");
      return new ArrayList<StationData>();
    }
  }

  /**
   * Create a list of all my stations that this scheduler is Retreving for
   * @param schedulerName <code>String</code> Scheduler to check
   * @param izStagedOnly If this flag is set, only stations that need to stage loads will be returned.
   * @return List  <code>String</code> of StationNames
   */
  public List<StationData> getSchedulersRetvStations(String schedulerName, boolean izStagedOnly)
  {
    try
    {
      return mpStation.getRetvStationsBySchedulerName(schedulerName, izStagedOnly);
    }
    catch (DBException e)
    {
      logException(e, "StandardStationServer-->getSchedulersRetvStations");
      return new ArrayList<StationData>();
    }
  }

  /**
   * Create a list of all my stations that this scheduler is staging for
   * @param schedulerName <code>String</code> Scheduler to check
   * @return List  <code>String</code> of StationNames
   */
  public List<StationData> getSchedulersStagedStations(String schedulerName)
  {
    return getSchedulersRetvStations(schedulerName, true);
  }

  /**
   *  Method checks if a given scheduler is attached to a particular station.
   *
   *  @param schedulerName <code>String</code> containing scheduler name.
   *  @param stationName <code>String</code> containing station name to check
   *         against for scheduler.
   *
   *  @return <code>boolean</code> of <code>true</code> if scheduler is attached
   *          to the station, <code>false</code> otherwise.
   */
  public boolean isStationScheduler(String schedulerName, String stationName)
  {
    boolean rtn = false;

    try
    {
      rtn = mpStation.isStationScheduler(schedulerName, stationName);
    }
    catch(DBException e)
    {
      logException(e, "StandardStationServer-->isStationScheduler.");
    }

    return(rtn);
  }

  /**
   * Return true if the specified station is the correct type of station to
   * accept a retrieved load and its device is Online.
   *
   * @return true, if station can accept retrieved load; otherwise, false
   */
  public boolean canStationRetrieveALoad(StationData ipSD)
  {
    initializeDeviceServer();
    boolean result = false;
    if (ipSD != null)
    {
      String vsStation = ipSD.getStationName();
      if (ipSD.getPhysicalStatus() != DBConstants.ONLINE)
      {
        logOperation(LogConsts.OPR_DEVICE, "Station " + vsStation
            + "\" NOT Online - StandardStationServer.canStationRetrieveALoad()");
        return false;
      }
      if (mpDevServer.getPhysicalStatus(ipSD.getDeviceID()) == DBConstants.ONLINE)
      {   // crane online
        int status = ipSD.getStatus();
        if(status == DBConstants.STORERETRIEVE)  // Is station in retrieve mode
        {
          result = isRetrieveStation(ipSD);
        }   // Not in retrieve mode
        else
        {
          String s = "";
          try
          {
            s = DBTrans.getStringValue("iStatus", status);
            logOperation(LogConsts.OPR_DEVICE, "Station " + vsStation
                + "  NOT in retrieve mode - StandardStationServer.canStationRetrieveALoad()");
          }
          catch (Exception e)
          {
            logError("StandardStationServer DBTrans CANNOT find iStatus - canStationRetrieveALoad()");
          }
          logDebug("Station " + vsStation + " Status: " + s
              + " - NOT in Retrieve Mode - StandardStationServer.canStationRetrieveALoad()");
        }
      } // device is offline
      else
      {
        logOperation(LogConsts.OPR_DEVICE, "Station " + vsStation + " Device \""
            + ipSD.getDeviceID()
            + "\" NOT Online - StandardStationServer.canStationRetrieveALoad()");
      }
    }
    else
    { // Didn't get station data from database
      logDebug("stationData == null -- canStationRetrieveALoad()");
    }
    return result;
  }

  /**
   * Is this station capable of accepting a retrieving load?
   * @param ipSD
   * @return
   */
  protected boolean isRetrieveStation(StationData ipSD)
  {
    int type = ipSD.getStationType();
    switch(type)
    {
      case DBConstants.TRANSFER_STATION:
      case DBConstants.USHAPE_OUT:
      case DBConstants.OUTPUT:
      case DBConstants.PDSTAND:
        // Station is a retrieve type station
        return true;
      case DBConstants.REVERSIBLE:
        // Station might be a retrieve type station
        return (ipSD.getBidirectionalStatus() == DBConstants.RETRIEVEMODE);
      default:
        // Station is not a retrieve type station
        String s = "";
        String vsStation = ipSD.getStationName();
        try
        {
          s = DBTrans.getStringValue("iStationType", type);
          logOperation(LogConsts.OPR_DEVICE, "Station " + vsStation
              + " NOT A retrieve station - StandardStationServer.isRetrieveStation()");
        }
        catch (Exception e)
        {
          logError("StandardStationServer DBTrans CANNOT find iStationType - isRetrieveStation()");
        }
        logDebug("Station " + vsStation + " Type: " + s
            + " - NOT a RetrieveType Station - StandardStationServer.isRetrieveStation()");
        return false;
    }
  }

  /**
   * can this station store a load in its current state for this scheduler.
   * Is needs to be online and be either a store station a PD in store mode.
   *
   * @param ipSD The station to check
   * @param isScheduler The scheduler doing the checking
   * @param ipLoadServer
   * @return An error String if the station can't store, an empty String if it can.
   */
  public String canStationStoreALoad(StationData ipSD, String isScheduler,
      StandardLoadServer ipLoadServer)
  {
    initializeDeviceServer();

    if (ipSD == null)
    {
      return "StationData is null!";
    }

    String vsStation = ipSD.getStationName();
    if (!isStationScheduler(isScheduler, vsStation))
    {
      return "Scheduler doesn't schedule for Station " + vsStation;
    }

    String vsMethodInfo = getClass().getSimpleName() + ".canStationStoreALoad()";

    if (mpDevServer.getPhysicalStatus(ipSD.getDeviceID()) != DBConstants.ONLINE)
    {
      String vsMsg = "Device: " + ipSD.getDeviceID() + " NOT OnLine - ";
      logOperation(vsMsg + vsMethodInfo);
    }

    if (ipSD.getPhysicalStatus() != DBConstants.ONLINE)
    {
      String vsMsg = "Station " + vsStation + " NOT Online - ";
      logOperation(LogConsts.OPR_DEVICE, vsMsg + vsMethodInfo);
      return vsMsg;
    }

    if ((ipSD.getStatus() == DBConstants.CAPTIVEINSERT) ||
        (ipSD.getStatus() == DBConstants.STORERETRIEVE))  // Is station in Store or insert mode
    {
      switch(ipSD.getStationType())
      {
      case DBConstants.TRANSFER_STATION:
      case DBConstants.AGC_TRANSFER:
      case DBConstants.USHAPE_IN:
      case DBConstants.INPUT:
      case DBConstants.PDSTAND:
        return "";
      case DBConstants.REVERSIBLE:
        /*
         * Unfortunately, the SRC has 3 modes, but the mode change messages
         * only have two:
         *   1) Store (both)
         *   2) Retrieve (both)
         *   3) Retrieve+Store (SRC only; this is reported as Retrieve)
         * In a probably vain attempt to correlate the SRC status with what is
         * reported, we'll assume that if there is a Store Pending load at the
         * station and the station is in Retrieve mode, then it must really be
         * in Retrieve+Store mode and it is okay to store.
         */
        if (ipSD.getBidirectionalStatus() == DBConstants.STOREMODE)
        {
          return "";
        }
        else
        {
          LoadData vpLD = ipLoadServer.getOldestLoadData(
              ipSD.getStationName(), -1);
          if (vpLD != null &&
              (vpLD.getLoadMoveStatus() == DBConstants.STOREPENDING ||
               vpLD.getLoadMoveStatus() == DBConstants.MOVEPENDING))
          {
            return "";
          }
          return "Station " + vsStation + " not in store mode.";
        }
      default:
        // Not as/rs store station
        return "Station " + vsStation + " not a store station.";
      }
    }

    // OK to store.
    return "";
  }

  /**
   * Does this station both store and retrieve?
   *
   * @param isStationId  <code>String</code>
   * @return True if can store a load
   */
  public boolean stationIsBidirectional(String isStationId)
  {
    int vnStnType = -1;
    try
    {
      vnStnType = mpStation.getStationTypeValue(isStationId);
    }
    catch(DBException ex)
    {
      logError(ex.getMessage());
    }

    return vnStnType == DBConstants.REVERSIBLE || vnStnType == DBConstants.PDSTAND;
  }

  /**
   * Get the captive flag of the station captive, semi or non
   * @param stationName <code>String</code>
   * @return captive type  <code>int</code>
   */
  public int getStationCaptiveType(String stationName)
  {
    int captiveType;
    mpStnData.clear();
    mpStnData.setKey(StationData.STATIONNAME_NAME, stationName);
    try
    {
      captiveType = mpStation.getCaptiveTypeValue(mpStnData);
    }
    catch(DBException e)
    {
      logException(e, "Exception Getting Station data for \"" +
                         stationName + "\"  - StandardStationServer.getStationCaptiveType()");
      captiveType = -1;
    }

    return captiveType;
  }

  /**
   * Get the Aisle Group of the station
   * @param isStnName <code>String</code>
   * @return aisleGroup of staion.  -1 if there is a DB error.
   */
  public int getStationAisleGroup(String isStnName)
  {
    int vnAisleGrp = -1;
    try
    {
      String vsStnDevice = (String)mpStation.getSingleColumnValue(isStnName,
                                                     StationData.DEVICEID_NAME);
      vnAisleGrp = mpDevice.getDeviceAisleGroup(vsStnDevice);
    }
    catch(DBException e)
    {
      logException(e, "Exception Getting Station data for \"" + isStnName +
                   "\"  - StandardStationServer.getStationCaptiveType()");
    }

    return vnAisleGrp;
  }

  /**
   * Get the controlling station data for a given location
   * @param isWarehouse to get the controlling station for.
   * @param isAddress to get the controlling station for.
   * @return StationData
   */
  @SuppressWarnings("rawtypes")
  public StationData getControllingStationFromLocation(String isWarehouse, String isAddress)
  {
    try
    {
      LocationData lcData = Factory.create(LocationData.class);
      StandardLocationServer locationServer = Factory.create(StandardLocationServer.class);
      lcData = locationServer.getLocationRecord(isWarehouse, isAddress);
              // First look to see if this location is a station
      StationData stationData = Factory.create(StationData.class);
      if(lcData.getLocationType() == DBConstants.LCSTATION)
      {
        stationData.setKey(StationData.STATIONNAME_NAME, isAddress);
      }
      else
      {
        stationData.setKey(StationData.DEVICEID_NAME, lcData.getDeviceID());
      }
      List<Map> tmplist = mpStation.getAllElements(stationData);
      if(tmplist != null && tmplist.size() > 0)
      {
        stationData.dataToSKDCData(tmplist.get(0));
        return(stationData);
      }
      return null;
    }
    catch (Exception e)
    {
        logException(e, "Exception Getting Controlling Station data for location\"" +
          isWarehouse + "-" + isAddress + "\"  - StandardStationServer.getConrollingStationFromLocation()");
        return null;
    }
  }

  /**
   *  Method gets all stations belonging to a warehouse.
   * @param sWarehouse the warehouse to search by.
   * @return String array of station names.  Empty array if no matches found.
   */
  public String[] getStationsByWarehouse(String sWarehouse) throws DBException
  {
    return mpStation.getStationsByWarehouse(sWarehouse);
  }

  /**
   * Verifies that this station can store a load to this warehouse and address with this
   * height
   * @param ipSD <code>String</code> station to check
   * @param address  <code>String</code> to store to
   * @param height  <code>int</code> of load to store
   * @return True if station can store to that location with that height
   */
  public boolean canStationStoreToLocation(StationData ipSD, String address, int height)
  {
    try
    {
      logDebug("StandardStationServer.createLocationServer()");
      StandardLocationServer locationServer = Factory.create(StandardLocationServer.class);
      return locationServer.existsWithHeight(ipSD.getWarehouse(), address, height);
    }
    catch (Exception e)
    {
      logException(e, "Exception getting location server  \""
          + "\"  - StandardStationServer.StandardStationServer");
      return false;
    }
  }

  /**
   * Get the type of station this is ( input, output, ushaped in, PD etc)
   * @param stationName <code>String</code>
   * @return station Type  <code>int</code>
   */
  public int getStationType(String stationName)
  {
    Integer vpStationType = null;
    try
    {
      vpStationType = (Integer)mpStation.getSingleColumnValue(stationName,
          StationData.STATIONTYPE_NAME);
    }
    catch(DBException e)
    {
      logException(e, "Error checking Station type for station...");
    }

    return (vpStationType == null) ? 0 : vpStationType;
  }

  /**
   * Gets the warehouse this station is in
   * @param stationName the station's identifier.
   * @return Station's warehouse.  If an error condition occurs, a <code>null</code>
   *         value is returned.
   */
  public String getStationWarehouse(String stationName)
  {
    String vsStationWhs = null;
    try
    {
      vsStationWhs = (String) mpStation.getSingleColumnValue(stationName,
          StationData.WAREHOUSE_NAME);
    }
    catch(DBException e)
    {
      logException(e, "Error checking Warehouse for station...");
    }

    return(vsStationWhs);
  }

 /**
  * Get stations device.
  * @param isStationName the stations name.
  * @return the device to which this station belongs.
  * @throws DBException If there is a DB access error.
  */
  public String getStationsDevice(String isStationName) throws DBException
  {
    String vsDeviceID = (String)mpStation.getSingleColumnValue(isStationName,
                                                     StationData.DEVICEID_NAME);
    return(vsDeviceID);
  }

  /**
   * Get the item to auto order for this station
   * @param stationName
   * @return item
   */
  public String getStationItem(String stationName)
  {
    StationData stationData = getStation(stationName);
    return stationData.getItem();
  }

  /**
   * This is the quanitiy to auto order if this is an auto order station
   * @param stationName <code>String</code>
   * @return order quantity <code>double</code>
   */
  public double getStationOrderQty(String stationName)
  {
    StationData stationData = getStation(stationName);
    return stationData.getOrderQuantity();
  }

  /**
   * Get the default values needed for this station to create a load at it.
   * @param stationName <code>String</code>
   * @return list of warehouse, deviceid, container type, and Amount full
   */
  public List<Object> getStationDefaults(String stationName)
  {
    StationData stationData = getStation(stationName);
    List<Object> commandString = new ArrayList<Object>();
    commandString.add(stationData.getWarehouse());
    commandString.add(stationData.getDeviceID());
    commandString.add(stationData.getContainerType());
    Integer a = Integer.valueOf(stationData.getAmountFull());
    commandString.add(a);
    commandString.add(stationData.getDefaultRoute());

    return commandString;
  }

  /**
   * Returns true if this is an autostore station
   * @param isStationName <code>String</code> Station Name
   * @return True <code>boolean</code> if it is an auto store station
   */
  public boolean isStationAutoStore(String isStationName)
  {
    boolean vzRtn = false;

    try
    {
      Integer vpAutoStoreType = (Integer)mpStation.getSingleColumnValue(isStationName,
                                           StationData.AUTOLOADMOVEMENTTYPE_NAME);
      if (vpAutoStoreType != null)
      {
        int vnAutoStoreType = vpAutoStoreType;

        vzRtn = (vnAutoStoreType == DBConstants.AUTORECEIVE_ER            ||
                 vnAutoStoreType == DBConstants.AUTORECEIVE_EXPECTED_LOAD ||
                 vnAutoStoreType == DBConstants.AUTORECEIVE_ITEM          ||
                 vnAutoStoreType == DBConstants.AUTORECEIVE_LOAD          ||
                 vnAutoStoreType == DBConstants.AUTORECEIVE_BCR           ||
                 vnAutoStoreType == DBConstants.BOTH);
      }
    }
    catch(DBException exc)
    {
      logError("DB Error finding AutoStore Station Type. StandardStationServer.isStationAutoStore");
    }

    return(vzRtn);
  }

  /**
   * Returns true if this is an autostore station using an expected receipt that
   * matches the barcode on the load.  All items in the expected receipt are then
   * stored on the load.
   * @param isStationName <code>String</code> Station name
   * @return True <code>boolean</code> if it is an auto store item station
   */
  public boolean isStationAutoStoreWithER(String isStationName)
  {
    boolean vzRtn = false;
    try
    {
      Integer vpAutoStoreType = (Integer)mpStation.getSingleColumnValue(isStationName,
                                         StationData.AUTOLOADMOVEMENTTYPE_NAME);
      if (vpAutoStoreType != null)
      {
        int vnAutoStoreType = vpAutoStoreType;
        vzRtn = (vnAutoStoreType == DBConstants.AUTORECEIVE_ER ||
                 vnAutoStoreType == DBConstants.BOTH);
      }
    }
    catch (DBException ex)
    {
      logError("DB Error finding AutoStore Station Type. StandardStationServer.isStationAutoStoreWithER");
    }

    return(vzRtn);
  }

 /**
  * Checks if a station is an Auto-Store station that uses an Expected Receipt
  * to validate an input load.
  * @param isStationName the station name.
  * @return <code>true</code> if the station is an AutoStore E.R. Load station.
  */
  public boolean isStationAutoStoreERLoad(String isStationName)
  {
    boolean vzRtn = false;
    try
    {
      Integer vpAutoStoreType = (Integer)mpStation.getSingleColumnValue(isStationName,
                                         StationData.AUTOLOADMOVEMENTTYPE_NAME);
      if (vpAutoStoreType != null)
      {
        int vnAutoStoreType = vpAutoStoreType;
        vzRtn = (vnAutoStoreType == DBConstants.AUTORECEIVE_EXPECTED_LOAD);
      }
    }
    catch (DBException ex)
    {
      logError("DB Error finding AutoStore Station Type. StandardStationServer.isStationAutoStoreERLoad");
    }

    return(vzRtn);
  }

  /**
   * Returns true if this is an autostore item station using sItem in station to
   * store on the load with the fOrderQuantity in the station as quantity.
   * No expected receipt.
   * @param isStationName <code>String</code> Station Name
   * @return True <code>boolean</code> if it is an auto store item station
   */
  public boolean isStationAutoStoreItem(String isStationName)
  {
    boolean vzRtn = false;
    try
    {
      Integer vpAutoStoreType = (Integer)mpStation.getSingleColumnValue(isStationName,
                                         StationData.AUTOLOADMOVEMENTTYPE_NAME);
      if (vpAutoStoreType != null)
      {
        int vnAutoStoreType = vpAutoStoreType;
        vzRtn = (vnAutoStoreType == DBConstants.AUTORECEIVE_ITEM);
      }
    }
    catch (DBException ex)
    {
      logError("DB Error finding AutoStore Station Type. StandardStationServer.isStationAutoStoreItem");
    }
    return(vzRtn);
  }

  /**
   * Returns true if this is an autostore load station using barcode as load id
   * no expected recept or items on load
   * @param isStationName <code>String</code> Station Name
   * @return True <code>boolean</code> if it is an auto store load station
   */
  public boolean isStationAutoStoreLoad(String isStationName)
  {
    boolean vzRtn = false;
    try
    {
      Integer vpAutoStoreType = (Integer)mpStation.getSingleColumnValue(isStationName,
                                         StationData.AUTOLOADMOVEMENTTYPE_NAME);
      if (vpAutoStoreType != null)
      {
        int vnAutoStoreType = vpAutoStoreType;
        vzRtn = (vnAutoStoreType == DBConstants.AUTORECEIVE_LOAD);
      }
    }
    catch (DBException ex)
    {
      logError("DB Error finding AutoStore Station Type. StandardStationServer.isStationAutoStoreLoad");
    }
    return(vzRtn);
  }

  /**
   * Returns true if this is an autostore station using the station load prefix
   * for loadid and the BCR as an Item
   * @param isStationName <code>String</code> Station Name.
   * @return True <code>boolean</code> if it is an auto store LoadPrefix BCR as
   *         Item station
   */
  public boolean isStationAutoStoreBCRAsItem(String isStationName)
  {
    boolean vzRtn = false;
    try
    {
      int vnAutoStoreType = (Integer)mpStation.getSingleColumnValue(isStationName,
                                         StationData.AUTOLOADMOVEMENTTYPE_NAME);
      vzRtn = (vnAutoStoreType == DBConstants.AUTORECEIVE_BCR);
    }
    catch (DBException ex)
    {
      logError("DB Error finding AutoStore Station Type. StandardStationServer.isStationAutoStoreBCRAsItem");
    }
    return(vzRtn);
  }

  /**
   * Return trure if this is an out order station
   * @param isStationName <code>String</code> to check
   * @return True <code>boolean</code> if auto order station
   */
  public boolean isStationAutoOrder(String isStationName)
  {
    boolean vzRtn = false;
    try
    {
      Integer vpAutoStoreType = (Integer) mpStation.getSingleColumnValue(isStationName,
                                         StationData.AUTOLOADMOVEMENTTYPE_NAME);
      if (vpAutoStoreType != null)
      {
        int vnAutoStoreType = vpAutoStoreType;
        vzRtn = (vnAutoStoreType == DBConstants.AUTO_ORDER_OFF);
      }
    }
    catch (DBException ex)
    {
      logError("DB Error finding AutoStore Station Type. StandardStationServer.isStationAutoOrder");
    }
    return(vzRtn);
  }

  /**
   * See if this is a auto order station and if it is then determine if it is auto order
   * empty container or item.  use the order server to create the correct order.
   * @param stationName <code>String</code>
   */
  public void autoOrderForStation(String stationName)
  {
    StationData stationData = getStation(stationName);
    if( stationData.getAutoOrderType() == DBConstants.EMPTY_CONTAINER_ORDER)
    { // This is an auto order empty container station

      logDebug("StandardStationServer -Auto Order Empty Container " + stationName +  " - autoOrderForStation()");
      StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);

      int vnTotalEmptyStationOrders = stationData.getMaxAllowedEnroute() +
                                      stationData.getMaxAllowedStaged() - 1;
      double vdAmtEmptyDecimal = 1;
      if (stationData.getAmountFull() > DBConstants.EMPTY)
      {
        Map<String, AmountFullTransMapper> vpPartialQtyMap = LoadData.getAmountFullDecimalMap();
        String vsKey = LoadData.convAmountFullToFractionString(stationData.getAmountFull());
        AmountFullTransMapper vpTranMapper = vpPartialQtyMap.get(vsKey);
        vdAmtEmptyDecimal = vpTranMapper.getPartialAmtFullDecimal();
      }
      orderServer.autoOrderEmpty(vnTotalEmptyStationOrders, stationName,
                                 stationData.getOrderStatus(),
                                 stationData.getContainerType(),
                                 stationData.getHeight(), vdAmtEmptyDecimal,
                                 stationData.getOrderPrefix());
    }
    else
    if( stationData.getAutoOrderType() == DBConstants.ITEM_ORDER)
    { // This is an auto item order station
      logDebug("StandardStationServer -Auto Order Items " + stationName +  " - autoOrderForStation()");
      StandardOrderServer orderServer = Factory.create(StandardOrderServer.class);
      orderServer.autoOrderItem( stationData.getMaxAllowedEnroute() + stationData.getMaxAllowedStaged(),
             stationName, stationData.getOrderStatus(), stationData.getItem(),
             stationData.getOrderQuantity(), stationData.getHeight(),stationData.getOrderPrefix());
    }
  }

  /**
   * Checks if we know of a load at a station.
   *
   * @param isStation
   * @return  <code>true</code> if station is occupied.
   * @throws DBException
   */
  public boolean isStationOccupied(String isStation) throws DBException
  {
    boolean vzRtn = false;

    String vsWhs = getStationWarehouse(isStation);
    if (!vsWhs.isEmpty())
    {
      vzRtn = mpLoad.getLoadCountAtCurrentLoc(vsWhs, isStation) > 0;
    }
    else
    {
      throw new DBException("Station " + isStation + " does not have a warehouse!");
    }

    return(vzRtn);
  }

  /**
   * Returns true if this is a conventional station
   * @param stationName <code>String</code>
   * @return True <code>boolean</code> if it is a conventional station
   */
  public boolean isStationConventional(String stationName)
  {
    StationData stationData = getStation(stationName);
    return stationData.isConventionalStation();
  }

  /**
   * Returns true if this is an autopick station
   * @param stationName <code>String</code>
   * @return True <code>boolean</code> if it is an auyto pick station
   */
  public boolean isStationAutoPick(String stationName)
  {
    boolean vzRtn = false;

    try
    {
      Integer vpAutoLoadMovement = (Integer)mpStation.getSingleColumnValue(
          stationName, StationData.AUTOLOADMOVEMENTTYPE_NAME);
      if (vpAutoLoadMovement != null)
      {
        int vnAutoLoadMovement = vpAutoLoadMovement;
        vzRtn = (vnAutoLoadMovement == DBConstants.AUTOPICK ||
                 vnAutoLoadMovement == DBConstants.BOTH);
      }
    }
    catch(DBException e)
    {
      logException(e, "Error checking captive type for station...");
    }

    return vzRtn;
  }

  /**
   * Returns true if this is a captive station
   * @param stationName <code>String</code>
   * @return True <code>boolean</code> if it is captive station
   */
  public boolean isStationCaptive(String stationName)
  {
    int vnCaptiveType = 0;
    try
    {
      vnCaptiveType = (Integer)mpStation.getSingleColumnValue(stationName,
          StationData.CAPTIVE_NAME);
    }
    catch(DBException e)
    {
      logException(e, "Error checking captive type for station...");
    }

    return(vnCaptiveType == DBConstants.CAPTIVE);
  }

  /**
   * Get the linked route of this station
   * @param isStationName <code>String</code> the Station in question.
   * @return route <code>String</code>linked route if it exists; <code>null</code>
   *         if not.
   */
  public String getStationLinkedRoute(String isStationName)
  {
    if (isStationName == null || isStationName.trim().length() == 0) return(null);

    String vsLinkRoute = null;
    try
    {
      vsLinkRoute = (String)mpStation.getSingleColumnValue(isStationName,
          StationData.LINKROUTE_NAME);
    }
    catch(DBException e)
    {
      logException(e, "Error checking Link Route for station...");
    }

    return(vsLinkRoute);
  }

 /**
  * Indicates if this station is part of a split system.
  * @param isStationName the station ID.
  * @return <code>true</code> if station is part of a split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isStationPartOfAnySplitSystem(String isStationName) throws DBException
  {
    return(mpTJ.isStationPartOfAnySplitSystem(isStationName));
  }

 /**
  * Indicates if this station belongs to current JVM in a split system.
  * @param isStationName the station ID.
  * @return <code>true</code> if station is part of current split system.
  * @throws DBException if there is a DB access error.
  */
  public boolean isStationPartOfThisSplitSystem(String isStationName) throws DBException
  {
    return(mpTJ.isStationPartOfThisSplitSystem(isStationName));
  }

  /**
   * Checks if a station is an AGV station
   * @param isStation
   * @return
   * @throws DBException
   */
  public boolean isAGVStation(String isStation) throws DBException
  {
    int vnStationType = getStationType(isStation);

    return vnStationType == DBConstants.AGV_STATION;
  }

  /**
   * Does this screen have allocation enabled
   * @param isStatioName the station Name.
   * @return boolean of True if the station can have allocations.
   * @throws DBException if there is a database access error.
   */
  public boolean isStationAllocationEnabled(String isStationName) throws DBException
  {
    int vnAllocationEnabled = (Integer)mpStation.getSingleColumnValue(
        isStationName, StationData.ALLOCATIONENABLED_NAME);

    return(vnAllocationEnabled == DBConstants.YES);
  }

  /**
   * Get the type of allocation to use for this station
   * @param stationName <code>String</code>
   * @return allocation type <code>int</code>
   */
  public String getAllocationType(String stationName)
  {
    StationData stationData = getStation(stationName);
    return stationData.getAllocationType();
  }

  /**
   * Get all the possible types of allocation strategies.
   * @return List of allocation types.
   */
  public String[] getAllocationTypes() throws DBException
  {
    SysConfig vpSC = Factory.create(SysConfig.class);
    return vpSC.getParameterNames(SysConfig.ALLOCATION_STRATEGY);
  }

  /**
   * Does this location get a dummy arrival or do we send the store command
   * after the load is released.
   * @param stationName <code>String</code>
   * @return True <code>boolean</code> is we do get dummy arrivals
   */
  public boolean doesStationGetStoreArrival(String stationName)
  {
    StationData stationData = getStation(stationName);

    return(stationData.getArrivalRequired() == DBConstants.YES);
  }

  /**
   * Does this station get a work complete and then a final arrival.
   * @param stationName <code>String</code>
   * @return True <code>boolean</code> if does get final arrivals
   */
  public boolean doesStationGetRetrieveArrival(String stationName)
  {
    StationData stationData = getStation(stationName);

    return(stationData.getArrivalRequired() == DBConstants.YES);
  }

  public boolean isUnitRetrievalStation(String isStationName) throws DBException
  {
    int vnRetrievalDetail = (Integer)mpStation.getSingleColumnValue(
        isStationName, StationData.RETRIEVECOMMANDDETAIL_NAME);

    return(vnRetrievalDetail == DBConstants.UNIT_RETRIEVAL);
  }

  /**
   * Checks to see if the station name is the rack master.  Daifuku sets this
   * to 9000.
   * @param stationName <code>String</code>
   * @return True <code>boolean</code> if stationName = 9000
   */
  public boolean isStationTheRackMaster(String stationName)
  {
    return(stationName.equals(AGCDeviceConstants.RACKSTATION));
  }

  /**
   * Check to see if this station needs a new location to store a load.
   * If the station is captive the next location will have a valid location
   * that has a location status set to reserved.  This load always goes to that
   * location.  If not captive then a load can go to any empty, avalible location.
   * @param ipSD <code>String</code>
   * @return true <code>boolean</code> if doesn't require new location
   */
  public boolean isStationCaptiveForStore(StationData ipSD)
  {
     // If this is a captive station location is in next address
    return(ipSD.getCaptive() == DBConstants.CAPTIVE &&
           ipSD.getStatus() != DBConstants.CAPTIVEINSERT);
  }

  /**
   * Checks the passed in height to the station height.  If the passed in height is
   * less than or equal to the station height return true
   * @param stationName <code>String</code> to check height
   * @param checkHeight <code>int</code> height to compare
   * @return true <code>boolean</code> if check height is less than or equal to station height
   */
  public boolean checkStationHeight(String stationName, int checkHeight)
  {
    StationData stationData = getStation(stationName);
    return(stationData.getHeight() >= checkHeight);
  }

  /**
   * Get the order status for this station.  This is what the order status is set to
   * when an order is received from the host.
   * @param stationName <code>String</code>
   * @return order status at station.  If there is an error of some kind the
   *         default of READY is returned.
   */
  public int getOrderStatusForStation(String isStationID)
  {
    int vnDefaultOrdStatus = DBConstants.READY;
    if (isStationID == null || isStationID.trim().length() == 0)
    {
      return(vnDefaultOrdStatus);
    }

    try
    {
      Object vpOrdStat = mpStation.getSingleColumnValue(isStationID,
          StationData.ORDERSTATUS_NAME);
      if (vpOrdStat != null)
        vnDefaultOrdStatus = ((Integer)vpOrdStat).intValue();
    }
    catch(DBException e)
    {
      logException("Error getting default Station Order status.:::", e);
    }

    return(vnDefaultOrdStatus);
  }

  /**
   * Get the physical station for this station
   * @param stationId <code>String</code> to get the physical station
   * @return physical status <code>int</code> of station
   */
  public int getPhysicalStatus(String stationId)
  {
    int status = 0;
    if (stationId.length() != 0)
    {
      try
      {
        status = mpStation.getPhysicalStatus(stationId);
        String s = "";
        try
        {
          s = DBTrans.getStringValue("iPhysicalStatus", status);
        }
        catch (Exception e)
        {
          logError("StandardStationServer - DBTrans CANNOT find iPhysicalStatus - getPhysicalStatus()");
        }
        logDebug("Station " + stationId + " Status: " + s
            + " - StandardStationServer.getPhysicalStatus()");
      }
      catch (Exception e)
      {
        String s = "";
        logException(e, "StandardStationServer - Station \"" + stationId
            + "\" PhysicalStatus: " + s
            + " - StandardStationServer.getPhysicalStatus");
      }
    }
    else
    {
      logError("Station \"" + stationId
          + "\" NOT Found - StandardStationServer.getPhysicalStatus()");
    }
    return status;
  }

  /**
   * Set the station to a new physical status
   * @param stationId <code>String</code> to change the status
   * @param status <code>int</code> for the station
   * @throws DBException
   */
  public void setPhysicalStatus(String stationId, int status)
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpStation.setPhysicalStatus(stationId, status);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception Setting Station \"" + stationId
          + "\" PhysicalStatus - StandardStationServer.getPhysicalStatus");
    }
    catch (NoSuchElementException nsee)
    {
      logException(nsee,
          "Station [" + stationId + "] not found for setPhysicalStatus!");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Gets the station type of a load if it is at a station.
   * @param isLoadID the load being checked.
   * @return Station type if load is at a station. -1 if load is not at a station.
   * @throws DBException If there is a database access error.
   */
  @UnusedMethod
  public int getStationTypeByLoadLocn(String isLoadID) throws DBException
  {
    Load vpLoad = Factory.create(Load.class);
    String vsLoadAddress = Location.parseLocation(vpLoad.getLoadLocation(isLoadID))[1];

    return mpStation.getStationTypeValue(vsLoadAddress);
  }

  /**
   * Gets the station of a load if it is at a station.  If the load is not at a
   * station <code>null</code> is returned.
   * @param isLoadID the load being checked.
   * @return Load's station if it's at a station. <code>null</code> otherwise.
   * @throws DBException If there is a database access error.
   */
  @UnusedMethod
  public String getLoadsStation(String isLoadID) throws DBException
  {
    Load vpLoad = Factory.create(Load.class);
    String vsLoadAddress = Location.parseLocation(vpLoad.getLoadLocation(isLoadID))[1];

    int vzStationType = mpStation.getStationTypeValue(vsLoadAddress);

    return ((vzStationType == -1) ? null : vsLoadAddress);
  }

  /**
   * Get the scheduler for the given station
   *
   * @param isStationName <code>String</code> station to get scheduler name
   * @return scheduler <code>String</code>
   * @throws DBException
   */
  public String getStationsScheduler(String isStationName) throws DBException
  {
    String vsSchedName = "";

    Object vpDeviceID = mpStation.getSingleColumnValue(isStationName,
                                                     StationData.DEVICEID_NAME);
    if (vpDeviceID != null)
    {
      initializeDeviceServer();
      vsSchedName = mpDevServer.getSchedulerName((String)vpDeviceID);
    }

    return(vsSchedName);
  }

  /**
   * Method gets the custom action for a station.
   * @param isStationName the station
   * @throws com.daifukuamerica.wrxj.jdbc.DBException for DB access errors.
   * @return the custom action translation.
   */
  public int getStationCustomAction(String isStationName) throws DBException
  {
    int vnRtn = 0;
    try
    {
      Integer vpCustomAction = (Integer)mpStation.getSingleColumnValue(isStationName,
          StationData.CUSTOMACTION_NAME);

      if (vpCustomAction != null)
      {
        vnRtn = vpCustomAction;
      }
    }
    catch(DBException e)
    {
      logException(e, "Error getting Custom Action for station...");
    }

    return(vnRtn);
  }

  /**
   * Get the number of needed staged loads.
   *
   * @param isStationName
   * @return the number of loads to stage
   */
  public int getNumberOfLoadsToStage(String isStationName)
  {
    int vnNeeded = 0;

    try
    {
      // Get the station
      StationData vpSD = getStation(isStationName);

      // How many loads are already staged?
      Load vpLoad = Factory.create(Load.class);
      int vnLoadCount = vpLoad.getRetrievePendingLoadCount(isStationName,
          vpSD.getReprStationName());

      // Does the station need more?
      if (vpSD.getAllocationEnabled() == DBConstants.NO)
      {
        // if set to no don't send message to allocator for this station
        vnNeeded = 0;
      }
      else
      {
        vnNeeded = vpSD.getMaxAllowedStaged() - vnLoadCount;
      }

      if (vnNeeded > 0)
      {
        logDebug("Station " + isStationName + " has " + vnLoadCount
            + " loads staged, needs another staged load");
        autoOrderForStation(isStationName);
      }
      else
      {
        logDebug("Station " + isStationName + " has " + vnLoadCount
            + " loads staged, doesn't need staged loads");
      }
    }
    catch (DBException dbe)
    {
      logException(dbe, "stageLoadToStation: " + isStationName);
    }
    return vnNeeded;
  }

  /**
   * Get a list of all stations that need more loads staged
   *
   * @param isSchedulerName
   * @return
   */
  public List<String> stageLoadsToStations(String isSchedulerName)
  {
    List<StationData> vpStagingStations = getSchedulersStagedStations(isSchedulerName);
    List<String> vpStationList = new ArrayList<String>();
    if (vpStagingStations.isEmpty())
    {
      if (stagedStations)
      {
        logOperation(LogConsts.OPR_DEVICE, isSchedulerName
            + " has NO Staged stations (or they are offline)");
        stagedStations = false;
      }
    }
    else
    {
      if (!stagedStations)
      {
        logOperation(LogConsts.OPR_DEVICE, "Staged Stations are online");
      }
      stagedStations = true;
      logDebug(isSchedulerName + " has " + vpStagingStations.size()
          + " Staged Stations");
      for (int i = 0; i < vpStagingStations.size(); i++)
      {
        String vsStationName = vpStagingStations.get(i).getStationName();
        if (vsStationName.length() > 0)  // TODO: Why do we need this check?
        {
          if (getNumberOfLoadsToStage(vsStationName) > 0)
          {
            vpStationList.add(vsStationName);
          }
        }
      }
    }
    return vpStationList;
  }

  /**
   * Return true if this is a station that stores loads by round robin
   * to different aisles from it.
   * @param stationName <code>String</code> station to check if is a round robin
   * @return true <code>boolean</code> if it is a round robin station
   */
  @UnusedMethod
  public boolean roundRobinStation(String stationName)
  {
    StationData stationData = getStation(stationName);
    if(stationData.getAllowRoundRobin() == DBConstants.YES)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Set the Bi-Directional mode of a P&D or reversible station
   * @param isStationId
   * @param inStoreRetrieveMode
   */
  public void setBidirectionalMode(String isStationId, int inStoreRetrieveMode)
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpStation.setBidirectionalStatus(isStationId, inStoreRetrieveMode);
      commitTransaction(tt);
    }
    catch (DBException e)
    {
      logException(e, "Exception Setting Station \"" + isStationId
          + "\" BidirectionalStatus - StandardStationServer.setBidirectionalStatus");
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Is the station in Store mode
   * @param isStationName station to check is mode
   * @return true if in store mode
   */
  @UnusedMethod
  public boolean isStationInStoreMode(String isStationName)
  {
    int vnStationMode = -1;
    try
    {
      vnStationMode = (Integer)mpStation.getSingleColumnValue(isStationName,
                                                       StationData.BIDIRECTIONALSTATUS_NAME);
    }
    catch(DBException ex)
    {
      logException(ex, "Error finding station status for station " +
                   isStationName + "!");
    }

    return(vnStationMode == DBConstants.STOREMODE);
  }

  /**
   * Is the station in Captive Insert mode
   * @param isStationName station to check is mode
   * @return true if in Captive Insert mode.
   */
  @UnusedMethod
  public boolean isStationInCaptiveInsertMode(String isStationName)
  {
    int vnStationMode = -1;
    try
    {
      vnStationMode = (Integer)mpStation.getSingleColumnValue(isStationName,
                                                       StationData.STATUS_NAME);
    }
    catch(DBException ex)
    {
      logException(ex, "Error finding station status for station " +
                   isStationName + "!");
    }

    return(vnStationMode == DBConstants.CAPTIVEINSERT);
  }

  /**
   * Checks to see if a station is in the right mode to insert a load.
   * Throws an exception if it is not.
   *
   * @param isStationName
   * @throws DBException
   */
  public void canStationInsertALoad(String isStationName) throws DBException
  {
    StationData vpStationData = getStation(isStationName);
    StationData vpLinkStationData = null;

    if (vpStationData == null)
    {
      throw new DBException("Station " + isStationName + " does not exist.");
    }

    if(vpStationData.getCaptive() == DBConstants.CAPTIVE)
    {
      if (vpStationData.getStationType() == DBConstants.PDSTAND ||
          vpStationData.getStationType() == DBConstants.REVERSIBLE)
      {
        if ((vpStationData.getStatus() != DBConstants.CAPTIVEINSERT)
            || (vpStationData.getBidirectionalStatus() != DBConstants.STOREMODE))
        {
          throw new DBException("Store not allowed at Station " + isStationName + ".\n"
              + "\"Captive\" mode must be \"Captive Insert\" and \"P&D Station Mode\" must be \"Store Mode\".\n\n"
              + "NOTE: Station must be stopped and not have any SRC tracking data to change Station Mode.");
        }
      }
      else
      {
        try
        {
          if(vpStationData.getLinkRoute().trim().length() > 0)
          {
            vpLinkStationData = getStation(Factory.create(StandardRouteServer.class)
                .getNextRouteDest(vpStationData.getLinkRoute(), isStationName));
          }
        }
        catch(DBException e2)
        {
          throw new DBException("Unable to get station data - " + e2.getMessage());
        }
        if((vpLinkStationData == null) && (vpStationData.getStatus() != DBConstants.CAPTIVEINSERT))
        {
          throw new DBException("Store not allowed, Station " + isStationName
              + " \"Captive\" mode must be \"Captive Insert\" mode");
        }
        else if((vpLinkStationData != null) && (vpLinkStationData.getStatus() != DBConstants.CAPTIVEINSERT))
        {
          throw new DBException("Insert not allowed, Station " + vpLinkStationData.getStationName()
              + " \"Captive\" mode must be \"Captive Insert\" mode");
        }
      }
    }
    /*
     * If we get here, all is well and we can insert at this station
     */
  }

  /**
   *  Method retrieves a List of Station records with simulate flag set.
   * @param isDevice Controller device for which stations will be simulated.
   *
   *  @return <code>List</code> of <code>StationData</code> objects containing
   *  station records. Empty list if no records are found.  If device is blank, return all
   *  stations that are being simulated.
   */
  public List<StationData> getSimStationsForDevice(String isDevice) throws DBException
  {
    return mpStation.getSimStationsForDevice(isDevice);
  }

  /**
   * @inheritDoc
   */
  public void alertSimulatorOfStationUpdate(StationData ipSD)
  {
    initializeDeviceServer();

    String vsDevice = "";
    DeviceData vpDD = mpDevServer.getDeviceData(ipSD.getDeviceID());

    // Figure out the controlling device for this station
    if (vpDD.getCommDevice() == null || vpDD.getCommDevice().equals(""))
      vsDevice = vpDD.getDeviceID();
    else
      vsDevice = mpDevServer.getDeviceData(vpDD.getCommDevice()).getDeviceID();

    // Now send a message to the device's station simulator
    getSystemGateway().publishStationEvent(ipSD.getStationName(),0,vsDevice+"-"+Controller.SIMULATOR);
  }

  public boolean isSimulationOn(String isStation)
  {
    if (Application.getString("SimulationEnabled", "NO").equals("YES"))
    {
      StationData vpSD = getStation(isStation);
      return (vpSD.getSimulate() == DBConstants.ON);
    }
    return false;
  }

  /**
   * Method gets the allocator that is effectively allocating for a station.  It
   * does this based on the station's device.
   * @param isStationName the station for which an allocator will be found.
   * @return the allocator name. Return empty string if there is a database error
   *         or no matching allocator found for station.
   */
  public String getAllocatorForStation(String isStationName)
  {
    String vsAllocatorName = "";
    String vsStationDevice = "";

    try
    {
      vsStationDevice = (String)mpStation.getSingleColumnValue(isStationName,
                                                    StationData.DEVICEID_NAME);
      if (vsStationDevice == null || vsStationDevice.isEmpty())
      {
        initializeConfigServer();
        if (mpConfigServ.isSplitSystem())
        {
          String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
          DeviceData vpDData = Factory.create(DeviceData.class);
          vpDData.setKey(DeviceData.JVMIDENTIFIER_NAME, vsJVMId);
          String[] vasTmpStr = mpDevice.getSingleColumnValues(DeviceData.ALLOCATORNAME_NAME,
                                                              true, vpDData,
                                                              SKDCConstants.NO_PREPENDER);
                                       // Assume one Allocator per JVM instance.
          if (vasTmpStr.length != 0)
            vsAllocatorName = vasTmpStr[0];
        }
      }
      else
      {
        String vsTmpStr = (String)mpDevice.getSingleColumnValue(vsStationDevice,
                                      DeviceData.ALLOCATORNAME_NAME);
        if (vsTmpStr != null)
          vsAllocatorName = vsTmpStr;
      }
    }
    catch(DBException e)
    {
      logException(e, "Error getting Allocator Name for station... No allocator found for station.");
    }

    if (vsAllocatorName.isEmpty())
    {
      logError("No allocator found for station \'" + isStationName +
               "\' and Device \'" + vsStationDevice + "\'.");
    }

    return(vsAllocatorName);
  }

  /**
   * Gets a random child station of a representative station. Tries to pick one
   * with available enroute count that is not the same as the last one returned.
   *
   * @param isStn
   * @return
   */
  @SuppressWarnings("rawtypes")
  public String getReprStationChild(String isStn)
  {
    String vsStation = "";
    StationData vpSD = Factory.create(StationData.class);
    vpSD.setKey(StationData.REPRSTATIONNAME_NAME, isStn);
    try
    {
      List<Map> vpList = mpStation.getAllElements(vpSD);
      int vnLen = vpList.size();
      if (vnLen > 0)
      {
        // Pick a station with an enroute count less than the max that is not
        // the same as the last one returned, if possible.
        StandardSchedulerServer vpSchedServer = Factory.create(StandardSchedulerServer.class);
        for (Map m : vpList)
        {
          vpSD.dataToSKDCData(m);
          if (vpSchedServer.getEnrouteLoadCount(vpSD.getStationName()) < vpSD.getMaxAllowedEnroute())
          {
            if (!msLastReprStationChild.equals(vpSD.getStationName()))
            {
              msLastReprStationChild = vpSD.getStationName();
              return vpSD.getStationName();
            }
            else
            {
              vsStation = vpSD.getStationName();
            }
          }
        }
        if (vsStation.trim().length() > 0)
        {
          msLastReprStationChild = vsStation;
          return vsStation;
        }

        // Just pick a random one. In theory, we should never get here since
        // we shouldn't be transporting more than the max enroute.
        Map vpMap = vpList.get(new Random().nextInt(vnLen));
        vsStation = DBHelper.getStringField(vpMap, StationData.STATIONNAME_NAME);
      }
    }
    catch (DBException ex)
    {
      logException(ex, "Getting representative station's children.");
    }
    return vsStation;
  }
  private String msLastReprStationChild = "";

  /**
   * Get a list of stations names of stations that
   */
  public String[] getStationsWithRetrievePendingLoads(String isScheduler)
      throws DBException
  {
    return mpStation.getStationsWithRetrievePendingLoads(isScheduler);
  }

  /**
   * {@inheritDoc}
   */
  public String[] getStationsWithStorePendingLoads(String isScheduler)
      throws DBException
  {
    return mpStation.getStationsWithStorePendingLoads(isScheduler);
  }

  /**
   * Uses route table to determine if a load going to this station will pass
   * through an AGC Transfer Station first.
   *
   * @param stationName
   * @return
   */
  @UnusedMethod
  public boolean doesStationNeedAGCTransfer(String isStation)
  {
    try
    {
      return mpTJ.doesStationNeedAGCTransfer(isStation);
    }
    catch(DBException ex)
    {
      logException("Error checking for AGC Transfer station ", ex);
      return false;
    }
  }

  /**
   * Get the representative station ID of a given station
   *
   * @param stationName
   * @return
   */
  public String getReprStationName(String stationName)
  {
    try
    {
      return mpStation.getReprStationName(stationName);
    }
    catch (Exception e)
    {
      logException(e, "Exception getting Station  \"" + stationName
          + "\"  - StandardStationServer.getStation");
      return null;
    }
  }

  /**
   * Method gets all stations belonging to a RepresentativeStation group.
   *
   * @param sStationName the Representative Station to search by.
   * @return String array of station names. Empty array if no matches found.
   */
  public String[] getStationNameListByReprStation(String sStationName)
      throws DBException
  {
    return mpStation.getStationNameListByReprStation(sStationName);
  }

  /**
   * Send a mode change command to the AGC
   *
   * @param ipSD
   */
  public void sendBiDirectionalChangeCommand(StationData ipSD)
  {
    initializeDeviceServer();

    char vcMode = ControlEventDataFormat.CHAR_RETRIEVE_MODE;
    if (ipSD.getBidirectionalStatus() == DBConstants.STOREMODE)
    {
      vcMode = ControlEventDataFormat.CHAR_STORE_MODE;
    }
    DeviceData vpDevData = mpDevServer.getDeviceData(ipSD.getDeviceID());
    String vsCommDevice = vpDevData.getCommDevice().trim().length() > 0 ?
        vpDevData.getCommDevice() : vpDevData.getDeviceID();

    getSystemGateway().publishControlEvent(
        ControlEventDataFormat.getModeChangeCommand(vcMode,
            ipSD.getStationName()),
        ControlEventDataFormat.TEXT_MESSAGE, vsCommDevice);
  }

  /* ======================================================================== */
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/
  protected void initializeDeviceServer()
  {
    if (mpDevServer == null)
    {
      mpDevServer = Factory.create(StandardDeviceServer.class, getClass().getSimpleName());
    }
  }

  protected void initializeConfigServer()
  {
    if (mpConfigServ == null)
    {
      mpConfigServ = Factory.create(StandardConfigurationServer.class);
    }
  }

  /**
   *  Log a Modify Transaction History record
   *
   *  @param ipOldData <code>String</code> containing data of old Station record.
   *  @param ipNewData <code>String</code> containing data of new Station record.
   *
   *  @throws <code>DBException</code> if a database add error.
   */
  private void logModifyTransaction(StationData ipOldData, StationData ipNewData) throws DBException
  {
    // We only log the transaction if there is any change
    if (ipNewData.getColumnCount() > 0)
    {
      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.MODIFY);
      tnData.setStation(ipNewData.getStationName());
      if (logDataChanged(ipOldData, ipNewData) == true)
      {
        logTransaction(tnData);
      }
    }
  }

  /**
   * Get a list of default station names by screen type and machine name,
   * IP Address, and local host if user is a super user.
   * @param isMachineName
   * @param isIPAddress
   * @param isScreenType
   * @param izSuperUser
   * @return Map of default station names
   * @throws DBException
   */
  public Map<String,String> getDefaultStations(String isMachineName, String isIPAddress,
          String isScreenType, boolean izSuperUser) throws DBException
  {
    SysConfig mpSC = Factory.create(SysConfig.class);

    String[] vsStationList = mpSC.getDefaultStationList(isMachineName,
            isIPAddress, isScreenType, izSuperUser);

    Map<String, String> vpStationMap = new LinkedHashMap<String, String>();

    for (String vsStnName : vsStationList)
    {
      StationData vpStnData = getStation(vsStnName);
      if (vpStnData != null)
      {
        String vsStation = vpStnData.getStationName();
        String vsDesc = vpStnData.getDescription();
        vpStationMap.put(vsStation + " " + SKDCConstants.DESCRIPTION_SEPARATOR +
                         " " + vsDesc, vsStation);
      }
      else
      {
        logError("Station " + vsStnName + " does not exist!");
      }
    }

    return vpStationMap;
  }
}
