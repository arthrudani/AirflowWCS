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
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *   Title:  Class to handle Station Object.
 *   Description : Handles all reading and writing database for station
 * @author       REA
 * @version      1.0     02/25/02
 */
public class Station extends BaseDBInterface
{
  protected DBResultSet mpDBResultSet;
  protected StationData mpStnData;

  public Station()
  {
    super("Station");
    mpStnData = Factory.create(StationData.class);
  }

  /**
   * Add a station if it doesn't already exist
   * 
   * @param newStation
   * @return
   * @throws DBException
   */
  public boolean createStation(StationData newStation) throws DBException
  {
    if (doesStationExist(newStation.getStationName()))
    {
      return false;
    }
    else
    {
      addElement(newStation);
      return true;
    }
  }

  /**
   * Retrieves one column value from the OrderHeader table.
   * 
   * @param isStationID the unique key to use in the search.
   * @param isColumnName the name of the column whose value is returned.
   * @return value of column specified by isColumnName as an <code>Object</code>.
   *         The caller is assumed to know what data type is actually in
   *         <code>Object</code>. <i>A</i> <code>null</code> <i>object is
   *         returned for no matching data</i>
   * @throws DBException when database access errors occur.
   */
  public Object getSingleColumnValue(String isStationID, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName).append(" FROM Station WHERE ")
             .append("sStationName = '").append(isStationID).append("'");
    List<Map> vpData = fetchRecords(vpSql.toString());

    return((!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null);
  }
  
  /**
   * Method retrieves a Station record using Station Name as key.
   * 
   * @param stationName <code>String</code> containing Station to search for.
   * @return <code>StationData</code> object. <code>null</code> if no record
   *         found.
   */
  public StationData getStationData(String stationName) throws DBException
  {
    mpStnData.clear();
    mpStnData.setKey(StationData.STATIONNAME_NAME, stationName);

    return getElement(mpStnData, DBConstants.NOWRITELOCK);
  }

  /**
   * Returns station records based on exact match of device IDs. Requesting
   * stations for an AGC or ARC device returns all stations controlled by the
   * AGC or ARC as well as all stations controlled by any of that AGC's or
   * ARC's devices.
   * 
   * @param isDeviceID
   * @return List of Maps containing station data
   * @throws DBException
   */
  public List<Map> getDeviceStationData(String isDeviceID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ST.* FROM STATION ST, DEVICE DV")
               .append(" WHERE ST.").append(StationData.DEVICEID_NAME)
               .append("=DV.").append(DeviceData.DEVICEID_NAME)
               .append(" AND (ST.").append(StationData.DEVICEID_NAME)
               .append("=? OR DV.").append(DeviceData.COMMDEVICE_NAME)
               .append("=?) ORDER BY ST.").append(StationData.STATIONNAME_NAME);
    
    return fetchRecords(vpSql.toString(), isDeviceID, isDeviceID);
  }

  /**
   * Method retrieves a List of Station records matching a station name pattern.
   * 
   * @param srchStation <code>String</code> containing Station pattern to
   *            search for.
   * @return <code>List</code> containing station records. Empty list if no
   *         record found.
   */
  public List<Map> getStationDataListByStation(String srchStation)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM Station WHERE sStationName LIKE '")
             .append(srchStation).append("%' ORDER BY sStationName");
    return fetchRecords(vpSql.toString());
  }

  /**
   *  Method to update an Station record based on Station key.
   *
   *  @param sd Filled in Station data object.
   *  @exception DBException
   */
  public void updateStation(StationData sd) throws DBException
  {
    if (sd.getKeyObject(StationData.STATIONNAME_NAME) == null)
    {                                  // Must atleast have station key set.
      sd.setKey(StationData.STATIONNAME_NAME, sd.getStationName());
    }
    modifyElement(sd);
  }

  /**
   * Method returns a list of legal Replenishment sources for dedicated
   * locations controlled by this station.
   * 
   * @param sStationName The name of the station in question.
   * @return integer array of legal replenishment sources. If none are found, an
   *         empty array is returned.
   * @throws DBException if there is a serious database access error.
   */
  public int[] getReplenishmentSources(String sStationName) throws DBException
  {
    int[] iRplSrc;
    StringBuilder vpSql = new StringBuilder("SELECT sReplenishSources FROM Station WHERE ")
             .append("sStationName = '").append(sStationName).append("'");
             
    String sRplSrc = getStringColumn(StationData.REPLENISHSOURCES_NAME, vpSql.toString());
    if (sRplSrc != null && sRplSrc.trim().length() > 0)
    {
      String[] src = sRplSrc.split(",");
      iRplSrc = new int[src.length];
      for(int idx = 0; idx < src.length; idx++)
      {
        iRplSrc[idx] = Integer.parseInt(src[idx].trim());
      }
    }
    else
    {
      iRplSrc = new int[0];
    }
    
    return(iRplSrc);
  }
 
  /**
   * Method returns a list of legal Replenishment sources for dedicated
   * locations controlled by this station.
   * 
   * @param sStationName The name of the station in question.
   * @return integer array of legal replenishment sources. If none are found, an
   *         empty array is returned.
   * @throws DBException if there is a serious database access error.
   */
  public String[] getReplenishmentSourcesAsStrings(String sStationName)
      throws DBException
  {
    String[] iRplSrc;
    StringBuilder vpSql = new StringBuilder("SELECT sReplenishSources FROM Station WHERE ")
             .append("sStationName = '").append(sStationName).append("'");
             
    String sRplSrc = getStringColumn(StationData.REPLENISHSOURCES_NAME, vpSql.toString());
    if (sRplSrc != null && sRplSrc.trim().length() > 0)
    {
      String[] src = sRplSrc.split(",");
      iRplSrc = new String[src.length];
      for(int idx = 0; idx < src.length; idx++)
      {
        try
        {
          iRplSrc[idx] = DBTrans.getStringValue("iLocationType", Integer.parseInt(src[idx].trim()));
        }
        catch (NoSuchFieldException e)
        {
          iRplSrc[idx] = "ERROR";
        }
      }
    }
    else
    {
      iRplSrc = new String[0];
    }
    
    return(iRplSrc);
  }

  /**
   * Turn a String Array into a comma-delimited list
   * 
   * @param vasSources
   * @return
   */
  public String getReplenishmentSourcesString(String[] vasSources)
  {
    String vsReplenishSource = "";
    for (String vsSource : vasSources)
    {
      if (vsReplenishSource != "")
      {
        vsReplenishSource = vsReplenishSource + ",";
      }
      try
      {
        vsReplenishSource = vsReplenishSource + DBTrans.getIntegerValue("iLocationType", vsSource);
      }
      catch (NoSuchFieldException e)
      {
        if (vsReplenishSource != "")
        {
          vsReplenishSource = vsReplenishSource.substring(0, vsReplenishSource.length()-1);
        }
      }
    }
    return vsReplenishSource;
  }
  
  /**
   * Deletes a station
   * 
   * @param isStationName
   * @throws DBException
   */
  public void deleteStation(String isStationName) throws DBException
  {
    if (doesStationExist(isStationName))
    {
      mpStnData.clear();
      mpStnData.setKey(StationData.STATIONNAME_NAME, isStationName);
      deleteElement(mpStnData);
    }
  }

  /**
   * Checks to see if a station exists
   * 
   * @param stationName
   * @return
   * @throws DBException
   */
  public boolean doesStationExist(String stationName) throws DBException
  {
    mpStnData.clear();
    mpStnData.setKey(StationData.STATIONNAME_NAME, stationName);
    return(getCount(mpStnData) > 0);
  }

  /**
   * Description:<BR>
   * Title:  getDeviceType
   * Description : Counts the number of stations attached to a device.
   * @param  deviceID String containing station.
   *
   * @return integer containing the station count.
   */
  public int getDeviceStationCount(String deviceID) throws DBException
  {
    mpStnData.clear();
    mpStnData.setKey(StationData.DEVICEID_NAME, deviceID);
    return(getCount(mpStnData));
  }

  /**
   * Get a list of station names
   * @param srch
   * @return
   * @throws DBException
   */
  public List<String> getStationNameList(String srch) throws DBException
  {
    List<String> stationList = new ArrayList<String>();

    mpDBResultSet = execute("SELECT sStationName FROM station WHERE "
        + "sStationName LIKE ? ORDER BY sStationName", srch + "%");
    Map row;
    while (mpDBResultSet.hasNext()) // may be multiple rows
    {
      row = (Map) mpDBResultSet.next();
      String nameStr = new String(DBHelper.getStringField(row, "sStationName"));
      stationList.add(nameStr);
    }
    return (stationList);
  }

  /**
   * Title:  getStationNameListByStationType
   * Description : Gets list of Station Names based on the station types passed
   *               in.
   * @param stationTypes <code>Integer array</code> containing station types.
   * @return String array containing Station Names
   */
  public String[] getStationNameListByStationType(int[] stationTypes)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sStationName FROM Station ");

    if (stationTypes.length != 0)
    {                                // Build the WHERE clause.
      vpSql.append("WHERE ");
      for(int idx = 0; idx < stationTypes.length; idx++)
      {
        vpSql.append("iStationType = ").append(stationTypes[idx]);
        if (idx < stationTypes.length - 1)
        {
          vpSql.append(" OR ");
        }
      }
    }
    vpSql.append(" ORDER BY sStationName ");

    return getList(vpSql.toString(), StationData.STATIONNAME_NAME, 
                   SKDCConstants.NO_PREPENDER);
  }

  /**
   *  Method gets the station type.
   *
   *  @param sStationName <code>String</code> containing name of this station.
   *
   *  @return <code>int</code> value for station type.
   */
  public int getStationTypeValue(String sStationName) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iStationType FROM Station WHERE sStationName = '")
             .append(sStationName).append("'");

    return getIntegerColumn(StationData.STATIONTYPE_NAME, vpSql.toString());
  }

  /**
   *  Method gets a station's Captive type value based on a device ID lookup.
   *
   *  @param stnData <code>StationData</code> containing key to do searches by.
   *
   *  @return <code>int</code> value for captive type.
   */
  public int getCaptiveTypeValue(StationData stnData) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iCaptive FROM Station")
             .append(DBHelper.buildWhereClause(stnData.getKeyArray()));

    return getIntegerColumn(StationData.CAPTIVE_NAME, vpSql.toString());
  }
  
 /**
  * Gets map of Station Names/Descriptions based on the station types passed
  * in.
  * 
  * @param iapStationTypes <code>Integer array</code> containing station types.
  *            If empty or null assume non-input type stations.
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
  public Map<String, String> getStationsByStationType(int[] iapStationTypes,
                                                      final String isFirstElement)
         throws DBException
  {
    List<Map> vpList = null; 
    mpStnData.clear();
    
    if (iapStationTypes != null && iapStationTypes.length != 0)
    {
      mpStnData.setStationName("");
      mpStnData.setDescription("");
      Object[] vapTypes = SKDCUtility.getVarArgIntegerArray(iapStationTypes);
      mpStnData.setInKey(StationData.STATIONTYPE_NAME, KeyObject.AND, vapTypes);
      mpStnData.setOrderByColumns(StationData.STATIONNAME_NAME);
      vpList = getSelectedColumnElements(mpStnData);
    }
    else
    {
      mpStnData.setStationName("");
      mpStnData.setDescription("");
      mpStnData.setKey(StationData.STATIONTYPE_NAME, DBConstants.INPUT, 
                       KeyObject.NOT_EQUAL);
      mpStnData.setKey(StationData.STATIONTYPE_NAME, DBConstants.USHAPE_IN, 
                       KeyObject.NOT_EQUAL);
      mpStnData.setOrderByColumns(StationData.STATIONNAME_NAME);
      vpList = getSelectedColumnElements(mpStnData);
    }
    
    Map<String, String> vpStationMap = new LinkedHashMap<String, String>();
    if (isFirstElement.equals(SKDCConstants.ALL_STRING) ||
        isFirstElement.equals(SKDCConstants.NONE_STRING))
    {
      vpStationMap.put(isFirstElement, isFirstElement);
    }
    else if (isFirstElement.equals(SKDCConstants.EMPTY_VALUE))
    {
      vpStationMap.put("", "");
    }
    
    if (vpList.isEmpty())
    {
      if (vpStationMap.isEmpty())
        vpStationMap.put(SKDCConstants.NONE_STRING, SKDCConstants.NONE_STRING);
    }
    else
    {
      if (isFirstElement.equals(SKDCConstants.EMPTY_VALUE))
      {
        vpStationMap.put("", "");
      }

      for(Map vpRowMap : vpList)
      {
        String vsStation = DBHelper.getStringField(vpRowMap,
                                                   StationData.STATIONNAME_NAME);
        String vsDesc = DBHelper.getStringField(vpRowMap,
                                                StationData.DESCRIPTION_NAME);
        vpStationMap.put(vsStation + " " + SKDCConstants.DESCRIPTION_SEPARATOR +
                         " " + vsDesc, vsStation);
      }
    }
    
    return(vpStationMap);
  }

  /**
   *  Method retrieves a list of Stations for a given aisle group.
   *
   *  @param aisleGroup <code>int</code> containing aisle-group to search by.
   */
  public List<Map> getListOfStationsByAislegroup(int aisleGroup) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT st.sStationName FROM Station st, Device dv WHERE ")
             .append("st.sDeviceID = dv.sDeviceID AND ")
             .append("dv.iAisleGroup = ?");
    List<Map> aList = fetchRecords(vpSql.toString(), aisleGroup);


    return(aList);
  }
  
  /**
   *  Method retrieves a list of output Stations for a given aisle group.
   *
   *  @param inAisleGroup <code>int</code> containing aisle-group to search by.
   */
  public String[] getRetvStationsByAislegroup(int inAisleGroup) throws DBException
  {
    String[] stnList = new String[0];
    StringBuilder vpSql = new StringBuilder("SELECT st.sStationName FROM Station st, Device dv WHERE ")
               .append("st.sDeviceID = dv.sDeviceID AND ")
               .append("dv.iAisleGroup = ?").append(" AND ")
               .append("st.iStationType IN (?,?,?,?,?)");
    
    Object[] vapParams = { inAisleGroup, DBConstants.USHAPE_OUT,
        DBConstants.OUTPUT, DBConstants.REVERSIBLE, DBConstants.PDSTAND,
        DBConstants.TRANSFER_STATION };

    List<Map> aList = fetchRecords(vpSql.toString(), vapParams);
    if (aList.size() > 0)
    {
      stnList = SKDCUtility.toStringArray(aList, StationData.STATIONNAME_NAME);
    }

    return(stnList);
  }

  /**
   *  Method retrieves a list of Stations for a given warehouse.
   *
   *  @param sWarehouse <code>String</code> containing warehouse to search by.
   *  @return array of station names.  Empty array if no matches found.
   */
  public String[] getStationsByWarehouse(String sWarehouse) throws DBException
  {
    String[] stnList = new String[0];
    StringBuilder vpSql = new StringBuilder("SELECT sStationName FROM Station WHERE ")
             .append("sWarehouse = '").append(sWarehouse).append("'");
    List<Map> aList = fetchRecords(vpSql.toString());
    if (!aList.isEmpty())
    {
      stnList = SKDCUtility.toStringArray(aList, StationData.STATIONNAME_NAME);
    }

    return(stnList);
  }
 
  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpStnData = null;
    mpDBResultSet = null;
  }

  /**
   * Get a station's warehouse
   * @param sStationName
   * @return
   * @throws DBException
   */
  public String getStationWarehouse(String sStationName) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sWarehouse FROM Station WHERE ")
             .append("sStationName = '").append(sStationName).append("'");

    List<Map> alist = fetchRecords(vpSql.toString());
    if (alist == null || alist.isEmpty())
      throw new DBException("Station not found!");

    String stnWarehouse = DBHelper.getStringField(alist.get(0),
                                                  StationData.WAREHOUSE_NAME);
    return(stnWarehouse);
  }

  /**
   *  Method checks if a given scheduler is attached to a particular station.
   *
   *  @param isSchedulerName <code>String</code> containing scheduler name.
   *  @param isStationName <code>String</code> containing station name to check
   *         against for scheduler.
   *
   *  @return <code>boolean</code> of <code>true</code> if scheduler is attached
   *          to the station, <code>false</code> otherwise.
   */
  public boolean isStationScheduler(String isSchedulerName, String isStationName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(st.sStationName) AS \"rowCount\" FROM ")
             .append("Device dv, Station st WHERE ")
             .append("dv.sDeviceID = st.sDeviceID AND ")
             .append("dv.sSchedulerName = '").append(isSchedulerName).append("' AND ")
             .append("st.sStationName = '").append(isStationName).append("' ");
    return getRecordCount(vpSql.toString(), "rowCount") > 0;
  }

  /**
   * Method gets a list of stations attached to a scheduler that are configured
   * as one of USHAPE_OUT, PDSTAND, REVERSIBLE, or OUTPUT, and is in RETRIEVE
   * status.
   * 
   * @param schedulerName <code>String</code> containing name of scheduler
   *            attached to some station.
   * @param izStagedOnly If this flag is set, only stations that need to stage
   *            loads will be returned.
   * @return <code>String</code> array of station names. An empty string array
   *         is returned when no matches are found.
   */
  public List<StationData> getRetvStationsBySchedulerName(String schedulerName,
      boolean izStagedOnly) throws DBException
  {
    String vsStationTypes = DBConstants.USHAPE_OUT       + ", " +
                            DBConstants.PDSTAND          + ", " +
                            DBConstants.TRANSFER_STATION + ", " +
                            DBConstants.REVERSIBLE       + ", " +
                            DBConstants.OUTPUT;
    
    StringBuilder vpSql = new StringBuilder("SELECT st.* FROM Device dv, Station st WHERE ")
             .append("dv.sDeviceID = st.sDeviceID AND ")
             .append("iStatus = ").append(DBConstants.STORERETRIEVE).append(" AND ")
             .append("dv.iPhysicalStatus = ").append(DBConstants.ONLINE).append(" AND ")
             .append("iStationType IN (").append(vsStationTypes).append(") AND ")
             .append("dv.sSchedulerName = '").append(schedulerName).append("'");
    if (izStagedOnly)
      vpSql.append(" AND ").append("iAllocationEnabled = ").append(DBConstants.YES);
    
    List<Map> vpStns = fetchRecords(vpSql.toString());
    return DBHelper.convertData(vpStns, StationData.class);
  }

  /**
   *  Method retrieves a list of stations attached to a scheduler that are
   *  configured as one of REVERSIBLE, USHAPE_IN, PDSTAND, or INPUT.
   *
   *  @param schedulerName <code>String</code> containing name of scheduler
   *         attached to some station.
   *  @return <code>String</code> array of station names.  An empty string array
   *          is returned when no matches are found.
   */
  public List<StationData> getStoreStationsBySchedulerName(String schedulerName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT st.* FROM Device dv, Station st WHERE ")
             .append("dv.sDeviceID = st.sDeviceID AND ")
             .append("iStatus IN (?, ?) AND ")
             .append("dv.iPhysicalStatus = ? AND ")
             .append("iStationType IN (?, ?, ?, ?, ?, ?) AND ")
             .append("dv.sSchedulerName = ?");
    Object[] vapArgs = {DBConstants.CAPTIVEINSERT, DBConstants.STORERETRIEVE,
                        DBConstants.ONLINE, DBConstants.USHAPE_IN,
                        DBConstants.PDSTAND, DBConstants.REVERSIBLE,
                        DBConstants.AGC_TRANSFER, DBConstants.TRANSFER_STATION,
                        DBConstants.INPUT, schedulerName};
    List<Map> vpStnList = fetchRecords(vpSql.toString(), vapArgs);
    
    return(DBHelper.convertData(vpStnList,StationData.class));
  }
  
  /**
   * Fetch the real status of the specified station.
   *
   * @param  stationId String containing Station ID.
   * @return integer containing the status (translation).
   * @throws DBException
   */
  public int getPhysicalStatus(String stationId) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iPhysicalStatus FROM Station WHERE sStationName='")
             .append(stationId).append("'");

    return getIntegerColumn(StationData.PHYSICALSTATUS_NAME, vpSql.toString());
  }

  /**
   * Specify the real status of the specified station.
   *
   * @param  stationId String containing Station ID.
   * @param status the status (translation).
   * @throws DBException
   */
  public void setPhysicalStatus(String stationId, int status) throws DBException
  {
    mpStnData.clear();
    mpStnData.setKey(StationData.STATIONNAME_NAME, stationId);
    mpStnData.setPhysicalStatus(status);
    modifyElement(mpStnData);
  }
  
  /**
   * Change the Bidirectional Status of a PD or Reversible Station
   * @param stationId
   * @param status the new status to change to
   * @throws DBException
   */
  public void setBidirectionalStatus(String stationId, int status) throws DBException
  {
    mpStnData.clear();
    mpStnData.setKey(StationData.STATIONNAME_NAME, stationId);
    mpStnData.setBidirectionalStatus(status);
    modifyElement(mpStnData);
  }
  
  
  /**
   * Get a list of valid location types for the Replenish Sources field
   * 
   * @return Integer[]
   */
  public String[] getListOfValidReplenishSources()
  {
    try
    {
      String[] vasValidSources = 
        {DBTrans.getStringValue("iLocationType", DBConstants.LCASRS),
         DBTrans.getStringValue("iLocationType", DBConstants.LCCONVSTORAGE)};
      return vasValidSources;
    }
    catch (NoSuchFieldException e)
    {
      String[] vasErrorSources = {"Error reading sources"}; 
      return vasErrorSources;
    }
  }

  /**
   * Method retrieves a List of Station records with simulate flag set.
   * 
   * @param isDevice
   * 
   * @return <code>List</code> of <code>Map</code> objects containing station
   *         records. Empty list if no records are found.
   */
  public List<StationData> getSimStationsForDevice(String isDevice) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT st.* FROM Station st, Device dv ")
             .append("WHERE st.isimulate = ?")
             .append("AND st.sdeviceid = dv.sdeviceid ")
             .append("AND (dv.scommdevice = ? OR dv.sdeviceid LIKE ?) ")
             .append("ORDER BY st.sStationName");
    List<Map> vpStnList = fetchRecords(vpSql.toString(),  DBConstants.ON,
                                       isDevice, isDevice+"%");
    
    return DBHelper.convertData(vpStnList, StationData.class);
  }

  /**
   * 
   * @param isScheduler
   * @return
   * @throws DBException
   */
  public String[] getStationsWithRetrievePendingLoads(String isScheduler) throws DBException
  {
    StringBuffer vpBaseSQL = new StringBuffer();
    vpBaseSQL.append("SELECT DISTINCT st.sstationname FROM Device dv, ")
             .append("Station st, Load ld, Move mv, Route rt ")
             .append("WHERE (st.sDeviceID = dv.sDeviceID OR st.sDeviceID = dv.sCommDevice) ") 
             .append("AND iStatus = " + DBConstants.STORERETRIEVE + " ")
             .append("AND st.iPhysicalStatus = " + DBConstants.ONLINE + " ")
             .append("AND dv.iPhysicalStatus = " + DBConstants.ONLINE + " ")
             .append("AND dv.sSchedulerName = ? ")
             .append("AND mv.sRouteID = rt.sRouteID ")
             .append("AND rt.iFromType = " + DBConstants.EQUIPMENT + " ")
             .append("AND rt.sDestID = st.sstationname ")
             .append("AND rt.iRouteOnOff = " + DBConstants.ON + " ")
             .append("AND ld.sParentLoad = mv.sParentLoad ");
    StringBuilder vpSql = new StringBuilder(vpBaseSQL.toString())
             .append("AND ld.iloadmovestatus = ").append(DBConstants.RETRIEVEPENDING)
             .append(" AND st.sstationname NOT IN (")
             .append(vpBaseSQL.toString())
             .append(" AND ld.sNextAddress = st.sStationName ")
             .append("AND ld.iloadmovestatus IN (")
             .append(DBConstants.RETRIEVESENT).append(",")
             .append(DBConstants.RETRIEVING).append(",")
             .append(DBConstants.MOVING).append("))");
    
    return getList(vpSql.toString(), StationData.STATIONNAME_NAME, 
                   SKDCConstants.NO_PREPENDER, isScheduler, isScheduler);
  }

  /**
   * Get all online stations with store pending loads, but no storing or store
   * sent loads.
   * 
   * @param isScheduler
   * @return
   * @throws DBException
   */
  public String[] getStationsWithStorePendingLoads(String isScheduler) throws DBException
  {
    StringBuffer vpBaseSQL = new StringBuffer();
    vpBaseSQL.append("SELECT DISTINCT st.sstationname FROM Device dv, Station st, Load ld ") 
             .append("WHERE (st.sDeviceID = dv.sDeviceID OR st.sDeviceID = dv.sCommDevice) ") 
             .append("AND st.iStatus IN (").append(DBConstants.STORERETRIEVE)
             .append(",").append(DBConstants.CAPTIVEINSERT)
             .append(") AND st.iPhysicalStatus = ").append(DBConstants.ONLINE).append(" ")
             .append("AND dv.iPhysicalStatus = ").append(DBConstants.ONLINE)
             .append(" AND dv.sSchedulerName = ? ")
             .append("AND ld.saddress = st.sstationname ");
    StringBuilder vpSql = new StringBuilder(vpBaseSQL.toString())
             .append("AND ld.iloadmovestatus IN (").append(DBConstants.STOREPENDING)
             .append(", ").append(DBConstants.MOVEPENDING).append(") ")
             .append("AND st.sstationname NOT IN (").append(vpBaseSQL.toString())
             .append("AND ld.iloadmovestatus IN (").append(DBConstants.STORESENT)
             .append(",").append(DBConstants.MOVESENT).append("))");
    
    return getList(vpSql.toString(), StationData.STATIONNAME_NAME,
                   SKDCConstants.NO_PREPENDER, isScheduler, isScheduler);
  }

  /**
   * Method retrieves a list of Stations for a given RepresentativeStation
   * group.
   * 
   * @param srchStation <code>String</code> containing Representative Station
   *            to search by.
   * @return array of station names. Empty array if no matches found.
   */
  public String[] getStationNameListByReprStation(String srchStation)
      throws DBException
  {
    String[] stnList = new String[0];
    StringBuilder vpSql = new StringBuilder("SELECT sStationName FROM Station WHERE ")
             .append("sReprStationName = '").append(srchStation).append("'");
    List<Map> aList = fetchRecords(vpSql.toString());
    if (!aList.isEmpty())
    {
      stnList = SKDCUtility.toStringArray(aList, StationData.STATIONNAME_NAME);
    }

    return(stnList);
  } 
 
  /**
   * Get a station's representative station name
   * @param sStationName
   * @return
   * @throws DBException
   */
  public String getReprStationName(String sStationName) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sReprStationName FROM Station WHERE ")
             .append("sStationName = '").append(sStationName).append("'");

    List<Map> alist = fetchRecords(vpSql.toString());
    if (alist == null || alist.isEmpty())
      throw new DBException("Station not found!");

    String stnReprStationName = DBHelper.getStringField(alist.get(0),
        StationData.REPRSTATIONNAME_NAME);
    return(stnReprStationName);
  }
}
