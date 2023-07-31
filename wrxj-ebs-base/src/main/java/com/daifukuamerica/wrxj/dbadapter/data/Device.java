package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Title: Device Class
 * Description: Provides all functionality to the device object
 * Copyright:    Copyright (c) 2002
 * Company: SK Daifuku
 * @author  Ed Askew
 *          A.D.  Refactored to fit ModelInterface, and BaseDBInterface.
 * @version 1.0
 */
public class Device extends BaseDBInterface
{
  private DBResultSet  myDBResultSet;
  protected  DeviceData mpDevData;

  public Device()
  {
    super("Device");
    mpDevData = Factory.create(DeviceData.class);
  }

  /**
   * Gets list of Device IDs.
   *  @param isAllOrNone <code>String</code> containing string to start list
   *         with.  The values are SKDCConstants.ALL_STRING or
   *         SKDCConstants.NONE_STRING
   * @return String array containing all device IDs in the system.
   * @throws DBException
   */
  public String[] getDeviceChoices(String isAllOrNone) throws DBException
  {
    return getDistinctColumnValues(DeviceData.DEVICEID_NAME, isAllOrNone);
  }
  
  /**
   * Gets a list of Devices by Type.
   * 
   * @param inType <code>int</code> the device type we are looking for.
   * @param isAllOrNoneOrBlank determines whether we include the words all or
   *            none or blank in the list.
   * @return String[] containing all of the devices that match the type.
   * @throws DBException
   */
  public String[] getDevicesNameListByType(int inType, String isAllOrNoneOrBlank)
      throws DBException
  {
    mpDevData.clear();
    mpDevData.setKey(DeviceData.DEVICETYPE_NAME, inType);
    mpDevData.addOrderByColumn(DeviceData.DEVICEID_NAME);
    
    return getSingleColumnValues(DeviceData.DEVICEID_NAME, false, mpDevData,
        isAllOrNoneOrBlank);
  }

  /**
   * Get a list of devices like the isSearch.
   * @param isSearch to find stations like
   * @return List of stations <code>List</code>
   * @throws DBException
   */
  public List<String> getDeviceNameList() throws DBException
  {
    String[] vasDevices = getDistinctColumnValues(DeviceData.DEVICEID_NAME, "");
    List<String> vpReturn = new ArrayList<String>();
    for (String s : vasDevices)
    {
      vpReturn.add(s);
    }
    return vpReturn;
  }

 /**
  * Method to get an array of devices under a particular super warehouse.
  * @param isSuperWhs the super warehouse
  * @return array of devices.  Empty array if nothing found.
  * @throws DBException if there is a database error.
  */
  public String[] getDeviceNamesPerSuperWarehouse(String isSuperWhs)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT dv.sDeviceID FROM Device dv, Warehouse wh WHERE ")
               .append("wh.iWarehouseType = ").append(DBConstants.REGULAR).append(" AND ")
               .append("wh.sSuperWarehouse = ? AND ")
               .append("wh.sWarehouse = dv.sWarehouse ORDER BY dv.sDeviceID");
    List<Map> vpList = fetchRecords(vpSql.toString(), isSuperWhs);

    return(SKDCUtility.toStringArray(vpList, DeviceData.DEVICEID_NAME));
  }
  
  /**
   * Gets list of Communication Device IDs.
   *  @param isAllOrNone <code>String</code> containing string to start list
   *         with.  The values are SKDCConstants.ALL_STRING or
   *         SKDCConstants.NONE_STRING
   * @return String array containing all communication device IDs in the system.
   * @throws DBException
   */
  public String[] getCommDeviceChoices(String isAllOrNone) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(DeviceData.DEVICEID_NAME)
             .append(" FROM ").append(getWriteTableName())
             .append(" ORDER BY ").append(DeviceData.DEVICEID_NAME);
    
    return getList(vpSql.toString(), DeviceData.DEVICEID_NAME,
                   isAllOrNone);
  }

  /**
   * Gets list of Schedulers.
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
   * @return String array containing all schedulers in the system.
   * @throws DBException if there is a DB access error.
   */
  public String[] getSchedulerChoices(String isArrayPrepender, 
         boolean izPhysicallyOnlineDeviceOnly) throws DBException
  {
    mpDevData.clear();
    mpDevData.setKey(DeviceData.SCHEDULERNAME_NAME, "", KeyObject.NOT_EQUAL);
    if (izPhysicallyOnlineDeviceOnly)
    {
      mpDevData.setKey(DeviceData.PHYSICALSTATUS_NAME, DBConstants.ONLINE);
    }
    String[] vasSched = getSingleColumnValues(DeviceData.SCHEDULERNAME_NAME,
                                              true, mpDevData, isArrayPrepender);
    return(vasSched);
  }

  /**
   * Gets list of Schedulers.
   * @param isJVMId The JVM IDentifier for split systems.
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
   * @return String array containing all schedulers in the system.
   * @throws DBException if there is a DB access error.
   */
  public String[] getSchedulerChoices(String isJVMId, String isArrayPrepender,
                       boolean izPhysicallyOnlineDeviceOnly) throws DBException
  {
    mpDevData.clear();
    mpDevData.setKey(DeviceData.SCHEDULERNAME_NAME, "", KeyObject.NOT_EQUAL);
    mpDevData.setKey(DeviceData.JVMIDENTIFIER_NAME, isJVMId);
    if (izPhysicallyOnlineDeviceOnly)
    {
      mpDevData.setKey(DeviceData.PHYSICALSTATUS_NAME, DBConstants.ONLINE);
    }
    String[] vasSched = getSingleColumnValues(DeviceData.SCHEDULERNAME_NAME,
                                              true, mpDevData, isArrayPrepender);
    return(vasSched);
  }
  
  /**
   * Gets list of Allocators associated with devices.
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
   *        The second argument, if present, is assumed to be the JVM Identifier
   *        for split systems.
   * @param iasArgs optional argument. This is the JVM ID for split systems.
   * @return String array containing all device related allocators in the system.
   * @throws DBException
   */
  public String[] getAllocatorChoices(String isArrayPrepender, String... iasArgs)
         throws DBException
  {
    mpDevData.clear();
    mpDevData.setKey(DeviceData.ALLOCATORNAME_NAME, "", KeyObject.NOT_EQUAL);

    if (iasArgs.length > 0)
    {
      mpDevData.setKey(DeviceData.JVMIDENTIFIER_NAME, iasArgs[0]);
    }
    String[] vasAlloc = getSingleColumnValues(DeviceData.ALLOCATORNAME_NAME,
                                              true, mpDevData, isArrayPrepender);
    return(vasAlloc);
  }

 /**
  * Tests if a JVM is assigned to any device for split systems.
  * @param isJVMId the JVM Identifier.
  * @return <code>true</code> if at least one device has an assigned JVM.
  * @throws DBException for DB access errors.
  */
  public boolean isAnyDeviceJVMEnabled(String isJVMId) throws DBException
  {
    mpDevData.clear();
    mpDevData.setKey(DeviceData.JVMIDENTIFIER_NAME, isJVMId);
    
    return(getCount(mpDevData) > 0);
  }

  /**
   *  Return true if the specified device can be found.
   *
   * @param isDeviceID the name key to lookup
   * @return true if device exists
   * @throws DBException
   */
  public boolean exists(String isDeviceID) throws DBException
  {
    myDBResultSet = execute("SELECT * FROM Device WHERE sDeviceID = ?",
        isDeviceID);
    boolean result = false;
    switch (myDBResultSet.getRowCount())
    {
      case 0: // Does not exist
        result = false;
        break;
      case 1: // Already exists
        result = true;
        break;
      default: // Should only have one match
        DBHelper.dbThrow("Multiple matches on key: " + isDeviceID);
        result = false;
    }
    return result;
  }

  /**
   *  Return List of Data that is like the specified device String.
   *
   * @param srchDevice the name key to lookup
   * @return List
   * @throws DBException
   */
  public List<Map> getSearchData(String srchDevice) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(getWriteTableName())
             .append(" WHERE ").append(DeviceData.DEVICEID_NAME)
             .append(" LIKE '").append(srchDevice).append("%' ORDER BY ")
             .append(DeviceData.DEVICEID_NAME);
    return fetchRecords(vpSql.toString());
  }
  
  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    myDBResultSet = null;
  }

  /**
   * Description: gets the device type.
   * @param  deviceID String containing Device ID.
   * @return integer containing the device type (translation).
   * @throws DBException
   */
  public int getDeviceType(String deviceID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iDeviceType FROM Device WHERE sDeviceID='")
             .append(deviceID).append("'");

    return getIntegerColumn(DeviceData.DEVICETYPE_NAME, vpSql.toString());
  }

  /**
   * Fetch the real status of the specified device.
   *
   * @param  deviceID String containing Device ID.
   * @return integer containing the status (translation).
   * @throws DBException
   */
  public int getPhysicalStatus(String deviceID) throws DBException
  {
                                       // Clear out SQL String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT iPhysicalStatus FROM Device WHERE sDeviceID='")
             .append(deviceID).append("'");
    return getIntegerColumn(DeviceData.PHYSICALSTATUS_NAME, vpSql.toString());
  }

  /**
   * Fetch the device Aisle Group.
   *
   * @param  deviceID String containing Device ID.
   * @return integer containing the Aisle Group (translation).
   * @throws DBException
   */
  public int getDeviceAisleGroup(String deviceID) throws DBException
  {
                                       // Clear out SQL String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT iAisleGroup FROM Device WHERE sDeviceID='")
             .append(deviceID).append("'");
    return getIntegerColumn(DeviceData.AISLEGROUP_NAME, vpSql.toString());
  }

  /**
   * Fetch the device status for application decision.  For example,
   * the device can be marked as INOPERABLE in the application device status
   * field and still be ONLINE in the physical status field.  In this case
   * the application would not do anything with that device or its associated
   * stations.
   *
   * @param  deviceID String containing Device ID.
   * @return integer containing the status (translation).
   * @throws DBException
   */
  public int getOperationalStatusValue(String deviceID) throws DBException
  {
                                       // Clear out SQL String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT iOperationalStatus FROM Device WHERE sDeviceID='")
             .append(deviceID).append("'");
    return getIntegerColumn(DeviceData.OPERATIONALSTATUS_NAME, vpSql.toString());
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify the real status of the specified device.
   *
   * @param  deviceID String containing Device ID.
   * @param status the status (translation).
   * @throws DBException
   */
  public void setPhysicalStatus(String deviceID, int status) throws DBException
  {
    mpDevData.clear();
    mpDevData.setPhysicalStatus(status);
    mpDevData.setKey(DeviceData.DEVICEID_NAME, deviceID);
    modifyElement(mpDevData);
  }
  /**
   * Change the Device Token.
   *
   * @param  deviceID String containing Device ID.
   * @param deviceToken (translation).
   * @throws DBException
   */
  protected void setDeviceToken(String deviceID, int deviceToken) throws DBException
  {
    mpDevData.clear();
    mpDevData.setDeviceToken(deviceToken);
    mpDevData.setKey(DeviceData.DEVICEID_NAME, deviceID);
    modifyElement(mpDevData);
  }

  /**
   * Change the Device Token.
   *
   * @param inAisleGroup - The aisle group for the device
   * @return String - The device ID that is next
   * @throws DBException
   */
  public String getAndUpdateDeviceToken(int inAisleGroup) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(getWriteTableName())
             .append(" WHERE ").append(DeviceData.AISLEGROUP_NAME)
             .append("=? AND ").append(DeviceData.DEVICETOKEN_NAME)
             .append("=").append(DBConstants.TRUE);
    List<Map> vpResults = fetchRecords(vpSql.toString(), inAisleGroup);
    if (vpResults.size() == 0)
    {
      /*
       * Somehow, there is NO device token.  Pick a device at random to restart.
       */
      getLogger().logDebug("WARNING: No devices with device token set for aisle group " 
          + inAisleGroup);
      vpSql.setLength(0);
      vpSql.append("SELECT * FROM ").append(getWriteTableName())
               .append(" WHERE ").append(DeviceData.AISLEGROUP_NAME)
               .append("=? AND ").append(DeviceData.DEVICETOKEN_NAME)
               .append("=").append(DBConstants.FALSE).append(" AND ")
               .append(DeviceData.NEXTDEVICE_NAME)
               .append(" is not null");  // The "not null" portion is to exclude AGC devices 
      vpResults = fetchRecords(vpSql.toString(), inAisleGroup);
      
      /*
       * There aren't any devices configured properly.  This is bad, and beyond
       * the scope of automatic recovery.
       */
      if (vpResults.size() == 0)
      {
        throw new DBException("There are no properly configured devices for aisle group " 
            + inAisleGroup);
      }
    }
    else if (vpResults.size() > 1)
    {
      /*
       * Somehow, more than one device had the token set.  Pick one at random.
       */
      getLogger().logDebug("WARNING: Multiple devices with device token set for aisle group " 
          + inAisleGroup);
    }
    mpDevData.dataToSKDCData(vpResults.get(0));
    String currentDevice = mpDevData.getDeviceID();
    String nextDevice = mpDevData.getNextDevice();
    setDeviceToken(currentDevice, DBConstants.FALSE);
    setDeviceToken(nextDevice, DBConstants.TRUE);
    
    return currentDevice;
  }
  
 /**
  * Retrieves one column value from the Device table.
  * @param isDeviceID the unique key to use in the search.
  * @param isColumnName the name of the column whose value is returned.
  * @return value of column specified by isColumnName as an <code>Object</code>.
  *         The caller is assumed to know what data type is actually in <code>Object</code>.
  *         <i>A</i> <code>null</code> <i>object is returned for no matching data</i>
  * @throws DBException when database access errors occur.
  */
  public Object getSingleColumnValue(String isDeviceID, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName).append(" FROM Device WHERE ")
             .append("sDeviceID = '").append(isDeviceID).append("'");
    List<Map> vpData = fetchRecords(vpSql.toString());

    return((!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null);
  }
}
