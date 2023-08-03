package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:<BR>
 *   Title:  Class to handle Load Object.
 *   Description : Handles all reading and writing database for load
 * @author       REA
 * @version      1.0
 * <BR>Created: 4-Feb-25<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class Load extends BaseDBInterface
{
  private DBResultSet mpDBResultSet;
  protected LoadData mpLoadData;

  public Load()
  {
    super("Load");
    mpLoadData = Factory.create(LoadData.class);
  }

  /**
   * Retrieves one column value from the Load table.
   *
   * @param isLoadID the unique key to use in the search.
   * @param isColumnName the name of the column whose value is returned.
   * @return value of column specified by isColumnName as an <code>Object</code>.
   *         The caller is assumed to know what data type is actually in
   *         <code>Object</code>. <i>A</i> <code>null</code> <i>object is
   *         returned for no matching data</i>
   * @throws DBException when database access errors occur.
   */
  public Object getSingleColumnValue(String isLoadID, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName).append(" FROM Load WHERE ")
             .append("sLoadID = '").append(isLoadID).append("'");
    List<Map> vpData = fetchRecords(vpSql.toString());

    return((!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null);
  }

  /**
   * This method replaces several sections of similar code.  Since changing
   * to millisecond precision in the DB, this will probably never change a Date.
   *
   * @param isLoadID
   * @param inMoveStatus
   * @param ipDate
   * @return
   */
  protected Date getUniqueDate(String isLoadID, int inMoveStatus, Date ipDate)
  {
    for(int i = 0; i < 20; i++)
    {
      if (existsLoadWithSameMoveDate(isLoadID, inMoveStatus, ipDate))
      {
        ipDate.setTime(ipDate.getTime() + 1);
      }
      else
      {
        break;
      }
    }
    return ipDate;
  }


  /**
   * use data that is sent to create load
   */
  public boolean createLoad(LoadData newLoad) throws DBException
  {
    if (exists(newLoad.getLoadID()))
    {
       return false;
    }
    else
    {
      if (newLoad.getLoadMoveStatus() != DBConstants.NOMOVE)
      {
        newLoad.setMoveDate(getUniqueDate(newLoad.getLoadID(),
          newLoad.getLoadMoveStatus(), newLoad.getMoveDate()));
      }
      addLoad(newLoad);
      return true;
    }
  }
  
  public boolean createLoadLineItem(LoadLineItemData newLoadLine) throws DBException
  {
      addLoadLineItem(newLoadLine);
      return true;
  }

  /**
   *  Method retrieves a load record using load as key.
   *
   *  @param loadID <code>String</code> containing load to search for.
   *  @param withLock <code>int</code> flag indicating if record should be locked.
   *
   *  @return <code>LoadData</code> object. <code>null</code> if no record found.
   */
  public LoadData getLoadData(String loadID, int withLock) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.LOADID_NAME, loadID);

    return getElement(mpLoadData, withLock);
  }

  /**
   *  Method retrieves a parent load record using load as key.
   *
   *  @param loadID <code>String</code> containing load get the parent load for.
   *  @param withLock <code>int</code> flag indicating if record should be locked.
   *
   *  @return <code>LoadData</code> object. <code>null</code> if no record found.
   */
  public LoadData getParentLoadData(String isLoadID, int withLock)
         throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.LOADID_NAME, getParentLoadID(isLoadID));

    return getElement(mpLoadData, withLock);
  }

  /**
   * Convenience method.  This gets a Load record with no lock.
   */
  public LoadData getLoadData(String loadID) throws DBException
  {
    return getLoadData(loadID, DBConstants.NOWRITELOCK);
  }

  /**
   * Convenience method.  This gets a ParentLoad ID
   */
  public String getParentLoadID(String isLoadID) throws DBException
  {
    LoadData vploadData = getLoadData(isLoadID);
    if(vploadData == null)
    {
      throw new DBException("Error Reading Load Record for Load: '" + isLoadID
          + "' - Load not found.");
    }
    else
    {
      return(vploadData.getParentLoadID());
    }
  }

  /**
   * Convenience method.  This gets a ParentLoad record with no lock.
   */
  public LoadData getParentLoadData(String isLoadID) throws DBException
  {
    return(this.getParentLoadData(isLoadID, DBConstants.NOWRITELOCK));
  }

  /**
   *
   * @param warehouse
   * @param address
   * @param status
   * @param withLock
   * @return
   * @throws DBException
   */
  public LoadData getOldestLoadData(String warehouse, String address,
                                    int status, int withLock) throws DBException
  {
    LoadData myData = null;
    List<LoadData> arrList = getOldestLoadDataList(warehouse, address, status, withLock);

    if (arrList.isEmpty())
    {
      return(null);
    }
    else
    {                                  // we only want the first row
      myData = arrList.get(0);
    }

    return(myData);
  }

  /**
   *
   * @param warehouse
   * @param address
   * @param status
   * @param withLock
   * @return
   * @throws DBException
   */
  protected List<LoadData> getOldestLoadDataList(String warehouse, String address,
                                    int status, int withLock) throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if (warehouse.trim().length() == 0)
    {
      vpSql.append("SELECT * FROM Load WHERE ");
    }
    else
    {
      vpSql.append("SELECT * FROM Load WHERE sWarehouse = '")
               .append(warehouse).append("' AND ");
    }

    vpSql.append("sAddress = '").append(address).append("' ");
    if (status > 0)
    {
      vpSql.append("AND iLoadMoveStatus = ").append(status).append(" ");
    }
    if (withLock == DBConstants.WRITELOCK)
    {
      vpSql.append("FOR UPDATE ");
    }

    vpSql.append("ORDER BY dMoveDate ");
    List<Map> mapList = fetchRecords(vpSql.toString());
    List<LoadData> loadDataList = new ArrayList<LoadData>();

    for (int i = 0; i < mapList.size(); i++)
    {
      mpLoadData.dataToSKDCData(mapList.get(i));
      loadDataList.add(mpLoadData.clone());
    }

    return loadDataList;
  }

  /**
   * Get the oldest load data
   *
   * @param address
   * @param status
   * @param withLock
   * @return
   * @throws DBException
   */
  public LoadData getOldestLoadData(String address, int status, int withLock) throws DBException
  {
    return getOldestLoadData("", address, status, withLock);
  }

  /**
   * Method to check to see if there is a load in the system with the same move status
   *    and the same move date, if there is it returns true, if not it returns false
   *
   * @param sLoadid - The load we are checking against...is there a load other than this one
   * @param loadStatus - the load status we are checking for
   * @param moveDate - the move date we are checking for
   * @return boolean
   */
  public boolean existsLoadWithSameMoveDate( String sLoadid, int loadStatus, Date moveDate)
  {
    try
    {
      String tmpString = "SELECT sLoadID FROM Load WHERE sLoadID != ?" +
                         " AND iLoadMoveStatus = ?" +
                         " AND dMoveDate = ?";
      mpDBResultSet = execute(tmpString, sLoadid, loadStatus, moveDate);
      if( mpDBResultSet.getRowCount() > 0)
      {
          return true;
      }
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println("Error " + e + " checking for load with same move date");
    }

    return false;
  }

  /**
   * Convenience method.  This gets a Load record with no lock.
   */
  public LoadData getOldestLoadData(String warehouse, String address, int status) throws DBException
  {
    return(this.getOldestLoadData(warehouse, address, status, DBConstants.NOWRITELOCK));
  }

  /**
   * Convenience method.  This gets a Load record with no lock.
   */
  public LoadData getOldestLoadData(String address, int status) throws DBException
  {
    return getOldestLoadData(address, status, DBConstants.NOWRITELOCK);
  }

  /**
   * Method gets next load in Load ID order
   * @param isLoadID
   *
   * @return a Load Record
   *
   * @throws DBException when a database access error occurs
   *
   */
  public LoadData getNextLoadData(String isLoadID) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.LOADID_NAME, isLoadID, KeyObject.GREATER_THAN);
    mpLoadData.addOrderByColumn(LoadData.LOADID_NAME, true);
    setMaxRows(1);
    List<Map> vpLoadList = getAllElements(mpLoadData);
    setMaxRows();

    if (vpLoadList.size() > 0)
    {
      Map vpLoadMap = vpLoadList.get(0);

      mpLoadData.clear();
      mpLoadData.dataToSKDCData(vpLoadMap);

      return mpLoadData.clone();
    }
    else
    {
      return null;
    }
  }

  /**
   * Method gets previous load in Load ID order
   * @param isLoadID
   *
   * @return a Load Record
   *
   * @throws DBException when a database access error occurs
   *
   */
  public LoadData getPreviousLoadData(String isLoadID) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.LOADID_NAME, isLoadID, KeyObject.LESS_THAN);
    mpLoadData.addOrderByColumn(LoadData.LOADID_NAME, true);
    setMaxRows(1);
    List<Map> vpLoadList = getAllElements(mpLoadData);
    setMaxRows();

    if (vpLoadList.size() > 0)
    {
      Map vpLoadMap = vpLoadList.get(0);

      mpLoadData.clear();
      mpLoadData.dataToSKDCData(vpLoadMap);

      return mpLoadData.clone();
    }
    else
    {
      return null;
    }
  }

  /**
   *  Method gets a load's location.
   *  @param isLoadID -- the load id for which a location is being fetched.
   *
   *  @return a string of the form WWW-AAAAAAAAA where W=warehouse, and A=address.
   *          <b>Note:</b> the warehouse is <i>not</i> blank padded if it is less
   *          than 3 characters.
   *  @throws DBException when a database access error occurs.
   */
  public String getLoadLocation(String isLoadID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT CONCAT(CONCAT(sWarehouse, '-'), sAddress) AS \"Location\" FROM Load ")
             .append("WHERE sLoadID = '").append(isLoadID).append("'");

    return(getStringColumn("Location", vpSql.toString()));
  }

  /**
   * Add a load
   *
   * @param newLoad
   * @throws DBException
   */
  public void addLoad(LoadData newLoad) throws DBException
  {
    addElement(newLoad);
  }
  
  public void addLoadLineItem(LoadLineItemData newLoad) throws DBException
  {
    addElement(newLoad);
  }

  /**
   * Delete a load
   *
   * @param loadID
   * @throws DBException
   */
  public void deleteLoad(String loadID) throws DBException
  {
    if (exists(loadID))
    {
      mpLoadData.clear();
      mpLoadData.setKey(LoadData.LOADID_NAME, loadID);
      deleteElement(mpLoadData);
    }
  }

  /**
   * Does this load ID exist?
   *
   * @param loadID
   * @return <code>true</code>
   */
  public boolean exists(String loadID)
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.LOADID_NAME, loadID);

    return(exists(mpLoadData));
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpLoadData = null;
    mpDBResultSet = null;
  }

  /**
   * Get the number of loads at a location
   *
   * @param warehouse
   * @param address
   * @return
   * @throws DBException
   */
  public int getLoadCountAtCurrentLoc(String warehouse, String address)
      throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.WAREHOUSE_NAME, warehouse);
    mpLoadData.setKey(LoadData.ADDRESS_NAME, address);
    return(getCount(mpLoadData));
  }

  /**
   * Get the number of loads at a location + shelf position
   *
   * @param isWarehouse
   * @param isAddress
   * @param isShelfPosition
   * @return
   * @throws DBException
   */
  public int getLoadCountAtCurrentLoc(String isWarehouse, String isAddress,
      String isShelfPosition) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.WAREHOUSE_NAME, isWarehouse);
    mpLoadData.setKey(LoadData.ADDRESS_NAME, isAddress);
    mpLoadData.setKey(LoadData.SHELFPOSITION_NAME, isShelfPosition);
    return getCount(mpLoadData);
  }

  /**
   * Get the number of loads destined for this location.
   *
   * @param warehouse
   * @param address
   * @return
   * @throws DBException
   */
  public int getLoadCountAtNextLoc(String isWarehouse, String isAddress,
      String isShelfPosition) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.NEXTWAREHOUSE_NAME, isWarehouse);
    mpLoadData.setKey(LoadData.NEXTADDRESS_NAME, isAddress);
    mpLoadData.setKey(LoadData.NEXTSHELFPOSITION_NAME, isShelfPosition);
    return getCount(mpLoadData);
  }

  /**
   * Get the number of loads destined for this location.
   *
   * @param warehouse
   * @param address
   * @return
   * @throws DBException
   */
  public int getLoadCountAtFinalLoc(String warehouse, String address)
         throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.FINALWAREHOUSE_NAME, warehouse);
    mpLoadData.setKey(LoadData.FINALADDRESS_NAME, address);
    return(getCount(mpLoadData));
  }

  /**
   * Get a list of load data
   *
   * @param isSearchLoad Load ID or partial load string.  If a partial string is
   *        passed, the partial match will only be done from the end of the string.
   * @return List&lt;Map&gt; representing list of load data.
   * @throws DBException database access error.
   */
  public List<Map> getLoadDataList(String isSearchLoad) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setWildcardKey(LoadData.LOADID_NAME, isSearchLoad, false);

    return(getAllElements(mpLoadData));
  }

  /**
   * Get a list of load data
   *
   * @param iapKeys search keys.
   * @return List&lt;Map&gt; representing list of load data.
   * @throws DBException database access error.
   */
  public List<Map> getLoadDataList(KeyObject[] iapKeys) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKeys(iapKeys);
    mpLoadData.setOrderByColumns(LoadData.LOADID_NAME);

    return(getAllElements(mpLoadData));
  }

  /**
   * Do any loads have this container type?
   *
   * @param containerType
   * @return
   * @throws DBException
   */
  public boolean doesContainerTypeExist(String containerType) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.CONTAINERTYPE_NAME, containerType);
    return(getCount(mpLoadData) > 0);
  }

  /**
   *  Method updates various fields in the Load record matching on the Load ID
   *  field.
   *
   *  @param ld <code>LoadData</code> containing reference to Load Data to modify.
   *  @exception DBException
   */
  public void updateLoadInfo(LoadData ld) throws DBException
  {
                                       // Must at least have Load as key.
    if (ld.getKeyObject(LoadData.LOADID_NAME) == null)
    {
      ld.setKey(LoadData.LOADID_NAME, ld.getLoadID());
    }
    if (ld.getLoadMoveStatus() != DBConstants.NOMOVE)
    {
      ld.setMoveDate(getUniqueDate(ld.getLoadID(),
          ld.getLoadMoveStatus(), ld.getMoveDate()));
    }
    modifyElement(ld);
  }

  /**
   *  Method to figure out the top-most level load if the parameter loadID
   *  happens to be a sub-load in a load heirarchy.
   *
   *  @return top level load if found. Returns null if the load passed in has no
   *          parent.
   */
  public String getTopLevelLoad(String loadID)
  {
    boolean done    = false;
    String  superLoad = null;
    String  possibleSuperLoad = loadID;
    List<Map> arrList = null;

    while(!done)
    {
      StringBuilder vpSql = new StringBuilder("SELECT sParentLoad, sLoadID FROM Load WHERE ")
               .append("sLoadID = '").append(possibleSuperLoad).append("'");

      try
      {
        arrList = fetchRecords(vpSql.toString());
        if ((arrList == null) || (arrList.size() == 0))
        {                              // Record not found!
          done = true;
        }
        else
        {
          possibleSuperLoad = DBHelper.getStringField(arrList.get(0),
                                                 "sParentLoad");
          if (possibleSuperLoad == null || possibleSuperLoad.trim().length() == 0)
          {                            // Have reached top-most load.
            done = true;
          }
          else
          {
            superLoad = possibleSuperLoad;
            if (superLoad.equals(loadID))
            {
              done = true;
            }
          }
        }
      }
      catch(DBException e)
      {
        e.printStackTrace(System.out);
        System.out.println("Error " + e + " checking Load existence");
        done = true;
        possibleSuperLoad = null;
      }
    }

    return(superLoad);
  }

  /**
   *  Gets the loads current move status.
   *
   *  @param parentLoad <code>String</code> containing parent load ID.
   *  @return <code>int</code> containing the load status.
   */
  public int getLoadMoveStatusValue(String parentLoad) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT iLoadMoveStatus FROM Load WHERE ")
             .append("sParentLoad = '").append(parentLoad)
             .append("' OR sLoadID = '").append(parentLoad).append("'");
    return getIntegerColumn(LoadData.LOADMOVESTATUS_NAME, vpSql.toString());
  }

  /**
   * Method to set the Load move Status.
   * @param isParentLoad <code>String</code> containing parent load id.
   * @param inNewStatus <code>int</code> containing load move status.
   * @param isMessage
   * @exception DBException
   */
  public void setLoadMoveStatusValue(String isParentLoad, int inNewStatus,
      String isMessage) throws DBException
  {
    mpLoadData.clear();
    if (inNewStatus != DBConstants.NOMOVE)
    {
      StringBuilder vpSql = new StringBuilder("SELECT dMoveDate FROM Load WHERE ")
               .append("sParentLoad = '").append(isParentLoad).append("' OR ")
               .append("sLoadID = '").append(isParentLoad).append("'");
      List<Map> arrList = fetchRecords(vpSql.toString());
      if (!arrList.isEmpty())
      {
        Date dMoveDate = DBHelper.getDateField(arrList.get(0),
                                               LoadData.MOVEDATE_NAME);
        mpLoadData.setMoveDate(getUniqueDate(isParentLoad, inNewStatus, dMoveDate));
      }
    }

    mpLoadData.setLoadMoveStatus(inNewStatus);
    mpLoadData.setLoadMessage(isMessage);
    mpLoadData.setKey(LoadData.PARENTLOAD_NAME, isParentLoad);
    modifyElement(mpLoadData);
  }

  /**
   *  Method updates the Amount Full flag of Load record.
   *
   *  @param loadID <code>String</code> containing load id to be used as search key.
   *  @param newAmountFull <code>int</code> containing Amount Full Flag.
   *  @exception DBException
   */
  public void updateLoadAmountFull(String loadID, int newAmountFull)
         throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setAmountFull(newAmountFull);
    mpLoadData.setKey(LoadData.LOADID_NAME, loadID);
    try
    {
      modifyElement(mpLoadData);
    }
    catch(java.util.NoSuchElementException e)
    {
      throw new DBException("Cannot update Load: " + loadID
          + ", does not exist", e);
    }
  }

  /**
   * Method to update Load status once it arrives at a station (sets load to
   * ARRIVED). This method also updates the load's Move Date, current location.
   * If the station is a CAPTIVE station the load Next Location field is updated
   * to the location the load came from. If this is not a CAPTIVE station, the
   * Next Location is updated to blanks.
   *
   * @param mpLoadData <code>LoadData</code> containing Load data fields to
   *            update.
   * @param newStatus <code>int</code> containing new status of load (this
   *            will almost always be ARRIVED.
   * @param stationCaptive <code>int</code> containing indicator if Load
   *            should be treated as a captive load. If the location this load
   *            came from is Reserved, then treat this load as if it were
   *            captive.
   *
   * @exception DBException if there is a database error.
   */
  public void setLoadForRetrieve(LoadData ipLoadData, int newStatus,
      int stationCaptive, String isDeviceID) throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setLoadMoveStatus(newStatus);
    mpLoadData.setWarehouse(ipLoadData.getNextWarehouse());
    mpLoadData.setAddress(ipLoadData.getNextAddress());
    mpLoadData.setShelfPosition(ipLoadData.getNextShelfPosition());
    mpLoadData.setMoveDate(new Date());
    mpLoadData.setDeviceID(isDeviceID);

    if (stationCaptive == DBConstants.LCRESERVED)
    {
      //
      // Load IS Captive.
      //

      mpLoadData.setNextWarehouse(ipLoadData.getWarehouse());
      mpLoadData.setNextAddress(ipLoadData.getAddress());
      mpLoadData.setNextShelfPosition(ipLoadData.getShelfPosition());
      mpLoadData.setFinalWarehouse(ipLoadData.getWarehouse());
      mpLoadData.setFinalAddress(ipLoadData.getAddress());
    }
    else
    {
      mpLoadData.setNextWarehouse(null);
      mpLoadData.setNextAddress(null);
      mpLoadData.setNextShelfPosition(null);
      mpLoadData.setFinalWarehouse(null);
      mpLoadData.setFinalAddress(null);
    }

    //
    mpLoadData.setKey(LoadData.PARENTLOAD_NAME, ipLoadData.getParentLoadID());
    modifyElement(mpLoadData);
  }

  /**
   * Set up data for load about to be retrieved to a captive station.  This code
   * makes the logical assumption that if you have a Captive system, the load
   * will have its home location encoded in its load id and the encoding will follow
   * one of the the patterns given in the wrxj.properties file.
   *
   * @param ipLoadData
   * @param inLoadMoveStatus
   * @param isDeviceID
   * @throws DBException if there is a Load Modify Error.
   */
  public void setLoadForCaptiveRetrieve(LoadData ipLoadData, String isHomeAddress,
                                        int inLoadMoveStatus, String isDeviceID) throws DBException
  {
    if (isHomeAddress == null || isHomeAddress.isEmpty())
    {
      throw new DBException("Invalid load home address for a captive system!");
    }

    mpLoadData.clear();
    mpLoadData.setLoadMoveStatus(inLoadMoveStatus);
    mpLoadData.setWarehouse(ipLoadData.getNextWarehouse());
    mpLoadData.setAddress(ipLoadData.getNextAddress());
    mpLoadData.setShelfPosition(ipLoadData.getNextShelfPosition());
    mpLoadData.setMoveDate(new Date());
    mpLoadData.setDeviceID(isDeviceID);

    mpLoadData.setNextWarehouse(ipLoadData.getWarehouse());
    mpLoadData.setNextAddress(isHomeAddress);
    mpLoadData.setNextShelfPosition(ipLoadData.getShelfPosition());
    mpLoadData.setFinalWarehouse(ipLoadData.getWarehouse());
    mpLoadData.setFinalAddress(isHomeAddress);
    mpLoadData.setKey(LoadData.PARENTLOAD_NAME, ipLoadData.getParentLoadID());
    modifyElement(mpLoadData);
  }

  /**
   *  Method gets list of Retrieve pending loads that are in the rack.  This
   *  list may contain duplicates if there are multiple moves for a given load.
   *
   *  @param isStation The station loads should be retrieved to.
   *  @param isScheduler The scheduler that should send the retrieve commands.
   *  @return <code>List</code> containing Maps of Load data columns +
   *    Move.sRouteID as "MOVEROUTE"
   */
  public List<Map> getRetrievePendingLoads(String isStation,
      String isScheduler) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT load.*, Move.sRouteID AS \"MOVEROUTE\"")
             .append(" FROM Load, Move, Route, Device ")
             .append(" WHERE Load.iLoadMoveStatus = " )
             .append(DBConstants.RETRIEVEPENDING)
             .append(" AND Load.sParentLoad = Move.sParentLoad ")
             .append(" AND (Load.sDeviceID = Device.sDeviceID ")
             .append(" OR LOAD.sDeviceID = DEVICE.sCommDevice)")
             .append(" AND Device.sSchedulerName = ? ")
             .append(" AND Move.sRouteID = Route.sRouteID ")
             .append(" AND Route.iFromType = ").append(DBConstants.EQUIPMENT)
             .append(" AND Route.sFromID = Load.sDeviceID ")
             .append(" AND Route.sDestID = ?")
             .append(" AND Route.iRouteOnOff = ").append(DBConstants.ON)
             .append(" ORDER BY iPriority, Move.dMoveDate");

    Object[] vapParams = {isScheduler, isStation};
    return fetchRecords(vpSql.toString(), vapParams);
  }

  /**
   * Find and return Retrieve Pending loads awaiting a location-to-location
   * move. If any exist, it is likely caused by recovery on a double-deep
   * system, but it may come up if we have to do recovery on a system where we
   * agree to shuffle loads.
   * <P>
   * <I>Note: This method assumes that both the to- and from-location are
   * controlled by the same scheduler.</I>
   * </P>
   *
   * @param isScheduler
   * @return
   * @throws DBException
   */
  public String[] getLocToLocRetrievePendingLoads(String isScheduler)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ld.sLoadID FROM LOAD ld, LOCATION lc, DEVICE dv ");
    // Retrieve pending load
    vpSql.append("WHERE ld.iLoadMoveStatus=").append(DBConstants.RETRIEVEPENDING);
    // Going to a rack location
    vpSql.append(" AND ld.sNextWarehouse=lc.sWarehouse ");
    vpSql.append(" AND ld.sNextAddress=lc.sAddress ");
    vpSql.append(" AND lc.ILOCATIONTYPE=").append(DBConstants.LCASRS);
    // Controlled by this scheduler
    vpSql.append(" AND lc.sDeviceID=dv.sDeviceID ");
    vpSql.append(" AND dv.SSCHEDULERNAME=?");

    return getList(vpSql.toString(), LoadData.LOADID_NAME,
        SKDCConstants.NO_PREPENDER, isScheduler);
  }

  /**
   * Get the highest priority route for a retrieving load
   * @param isLoadID
   * @param isStation
   * @param isScheduler
   * @return
   * @throws DBException
   */
  public String getRetrieveRoute(String isLoadID, String isStation,
      String isScheduler) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ROUTE.srouteid AS \"SROUTEID\" FROM Load, Move, Route, Device ")
             .append(" WHERE Load.iLoadMoveStatus = " ).append(DBConstants.RETRIEVEPENDING)
             .append(" AND Load.sLoadID = ? ")
             .append(" AND Load.sParentLoad = Move.sParentLoad ")
             .append(" AND (Load.sDeviceID = Device.sDeviceID ")
             .append(" OR LOAD.sDeviceID = DEVICE.sCommDevice)")
             .append(" AND Device.sSchedulerName = ? ")
             .append(" AND Move.sRouteID = Route.sRouteID ")
             .append(" AND Route.iFromType = ").append(DBConstants.EQUIPMENT)
             .append(" AND Route.sDestID = ?")
             .append(" AND Route.iRouteOnOff = ").append(DBConstants.ON)
             .append(" ORDER BY iPriority, Move.dMoveDate");

    Object[] vapParams = {isLoadID, isScheduler, isStation};
    List<Map> vpList = fetchRecords(vpSql.toString(), vapParams);

    if (vpList.size() > 0)
    {
      String vsRouteID = vpList.get(0).get("SROUTEID").toString();
      return vsRouteID;
    }

    return "";
  }

  /**
   * Method gets count of RETRIEVEPENDING status loads. This method implicitly
   * verifies that a move record exists for the load to come out also in case
   * the load is erroneously marked as RETRIEVEPENDING for some reason!
   *
   * @param isStationName <code>String</code> containing station to check for
   *          RETRIEVEPENDING loads. The station must have a corresponding
   *          route.
   * @param isRepresentativeStation <code>String</code> containing the
   *          representative station to check for RETRIEVEPENDING loads. The
   *          station must have a corresponding route.
   *
   * @return <code>int</code> containing row count matching criteria.
   * @throws DBException if there is a DB access error.
   */
  public int getRetrievePendingLoadCount(String isStationName,
      String isRepresentativeStation) throws DBException
  {
    StringBuilder vpSql = new StringBuilder();

    if (isRepresentativeStation != null)
    {
      // If this has a representative, check the station and its representative
      vpSql.append("SELECT COUNT(DISTINCT Load.sLoadID) ")
                 .append("AS \"").append(ROW_COUNT_NAME).append("\" ")
                 .append("FROM Load, Move, Route WHERE ")
                 .append("Load.").append(LoadData.LOADMOVESTATUS_NAME).append("=").append(DBConstants.RETRIEVEPENDING)
                 .append(" AND Load.").append(LoadData.PARENTLOAD_NAME).append(" = Move.").append(MoveData.PARENTLOAD_NAME)
                 .append(" AND Move.").append(MoveData.ROUTEID_NAME).append(" = Route.").append(RouteData.ROUTEID_NAME)
                 .append(" AND Route.").append(RouteData.ROUTEONOFF_NAME).append(" = ").append(DBConstants.ON)
                 .append(" AND (Route.").append(RouteData.ROUTEID_NAME).append(" = ? ")
                 .append(" OR Route.").append(RouteData.ROUTEID_NAME).append(" = ?)");

      return getRecordCount(vpSql.toString(), ROW_COUNT_NAME,
          isStationName, isRepresentativeStation);
    }
    else
    {
      // This station doesn't have a representative, but it may BE one.  We
      // don't want to over-stage if all of it's children are staged.
      vpSql.append("SELECT COUNT(DISTINCT Load.sLoadID) ")
                 .append("AS \"").append(ROW_COUNT_NAME).append("\" ")
                 .append("FROM Load, Move, Route, Station WHERE ")
                 .append("Load.").append(LoadData.LOADMOVESTATUS_NAME).append("=").append(DBConstants.RETRIEVEPENDING)
                 .append(" AND Load.").append(LoadData.PARENTLOAD_NAME).append(" = Move.").append(MoveData.PARENTLOAD_NAME)
                 .append(" AND Move.").append(MoveData.ROUTEID_NAME).append(" = Route.").append(RouteData.ROUTEID_NAME)
                 .append(" AND Route.").append(RouteData.ROUTEONOFF_NAME).append(" = ").append(DBConstants.ON)
                 .append(" AND (Route.").append(RouteData.ROUTEID_NAME).append(" = ? ")
                 .append(" OR (Station.").append(StationData.REPRSTATIONNAME_NAME)
                 .append("=? AND Route.").append(RouteData.ROUTEID_NAME).append("=Station.")
                 .append(StationData.STATIONNAME_NAME).append(")))");

      return getRecordCount(vpSql.toString(), ROW_COUNT_NAME,
          isStationName, isStationName);
    }
  }


  /**
   *  Method sets the next address and warehouse in the Load record.
   *
   *  @param loadID <code>String</code> containing load id to be used as search key.
   *  @param warehouse <code>String</code> containing next warehouse.
   *  @param address <code>String</code> containing next address.
   *  @exception DBException
   */
  public void setNextAddress(String loadID, String warehouse, String address)
         throws DBException
  {
    LoadData vpLoadData = Factory.create(LoadData.class);

    vpLoadData.setNextWarehouse(warehouse);
    vpLoadData.setNextAddress(address);
    vpLoadData.setKey(LoadData.LOADID_NAME, loadID);
    try
    {
      modifyElement(vpLoadData);
    }
    catch(java.util.NoSuchElementException e)
    {
      throw new DBException("Cannot update Load: " + loadID
          + ", does not exist", e);
    }
  }

  /**
   * Gets a count of store sent/error and move sent/error loads at a station
   *
   * @return
   */
  public int getStoreWaitingLoadCount(String isStationName) throws DBException
  {
    int storePendLoadsCount = -1;
    String vsINSQL = DBConstants.STORESENT  + ", " +
                     DBConstants.STOREERROR + ", " +
                     DBConstants.MOVESENT   + ", " +
                     DBConstants.MOVEERROR;

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"rowCount\" FROM Load WHERE ")
               .append("sAddress = ? AND ")
               .append("iLoadMoveStatus IN (").append(vsINSQL).append(") ");

    storePendLoadsCount = getRecordCount(vpSql.toString(), "rowCount",
                                         isStationName);

    return storePendLoadsCount;
  }

  /**
   * Gets a count of store pending and move pending loads at a station
   * @return
   */
  public int getStorePendingLoadCount(String isStationName) throws DBException
  {
    int storePendLoadsCount = -1;
    String vsINSQL = DBConstants.STOREPENDING + ", " + DBConstants.MOVEPENDING;

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"rowCount\" FROM Load WHERE ")
               .append("sAddress = ? AND ")
               .append("iLoadMoveStatus IN (").append(vsINSQL).append(") ");

    storePendLoadsCount = getRecordCount(vpSql.toString(), "rowCount",
                                         isStationName);

    return storePendLoadsCount;
  }

  /**
   * Method gets list of store pending and move pending loads that are a station
   * with oldest load first.
   *
   * @param isStationName <code>String</code> containing input station.
   * @return <code>List</code> containing load list. All columns of the load
   *         record are published by this method.
   */
  public List<Map> getStorePendingLoads(String isStationName) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM Load WHERE ")
             .append(LoadData.ADDRESS_NAME).append("=? AND ")
             .append(LoadData.LOADMOVESTATUS_NAME)
             .append(" IN (").append(DBConstants.STOREPENDING)
             .append(", ").append(DBConstants.MOVEPENDING)
             .append(") ORDER BY ").append(LoadData.MOVEDATE_NAME);

    return fetchRecords(vpSql.toString(), isStationName);
  }

  /**
   * Get the count of loads destined for (but not arrived at) a station.  The
   * count for a representative station includes all of the stations that it
   * represents.
   *
   * @param isStation
   * @return
   * @throws DBException
   */
  public int getEnrouteCount(String isStation) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"rowCount\" FROM LOAD, STATION ")
               .append("WHERE (")
               .append(StationData.STATIONNAME_NAME).append("=? OR ")
               .append(StationData.REPRSTATIONNAME_NAME).append("=?) AND (")
               .append(LoadData.NEXTADDRESS_NAME).append("=").append(StationData.STATIONNAME_NAME)
               .append(" OR ")
               .append(LoadData.FINALADDRESS_NAME).append("=").append(StationData.STATIONNAME_NAME)
               .append(") AND ")
               .append(LoadData.LOADMOVESTATUS_NAME).append(" != ").append(DBConstants.RETRIEVEPENDING);

    return getRecordCount(vpSql.toString(), "rowCount", isStation,
        isStation);
  }

  /**
   * Get the count of loads destined for and at a station.  The count for a
   * representative station includes all of the stations that it represents.
   *
   * @param isStation
   * @return
   * @throws DBException
   */
  public int getEnrouteCountPlusAtStation(String isStation) throws DBException
  {
    int vnCount = getEnrouteCount(isStation);

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"rowCount\" FROM LOAD, STATION WHERE ")
               .append(LoadData.ADDRESS_NAME).append("=").append(StationData.STATIONNAME_NAME)
               .append(" AND (").append(StationData.STATIONNAME_NAME).append("=? OR ")
               .append(StationData.REPRSTATIONNAME_NAME).append("=?) AND ")
               .append(LoadData.LOADMOVESTATUS_NAME).append(" = ").append(DBConstants.ARRIVED);

    vnCount += getRecordCount(vpSql.toString(), "rowCount", isStation, isStation);

    return vnCount;
  }

  /**
   *  Method to get any child loads for this parent.
   *
   *  @param parentLoad Load ID.
   *
   *  @return List containing child loads.
   *  @exception DBException
   */
  public List<Map> getChildLoads(String parentLoad) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM Load WHERE sParentLoad = '")
             .append(parentLoad).append("' AND ")
             .append("sLoadID != '").append(parentLoad).append("'");
    return fetchRecords(vpSql.toString());
  }

  /**
   *  Method to get any child loads for this parent in
   *  descending order of last moved date.
   *
   *  @param parentLoad Load ID.
   *
   *  @return List containing child loads.
   *  @exception DBException
   */
  public List<Map> getChildLoadsOrderedByDate(String parentLoad)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM Load WHERE sParentLoad = '")
             .append(parentLoad).append("' AND ")
             .append("sLoadID != '").append(parentLoad).append("'")
             .append("ORDER BY dMoveDate DESC");
    return fetchRecords(vpSql.toString());
  }

  /**
   *  Method to get any child loads for this parent.
   *
   *  @param parentLoad Load ID to be deleted.
   *  @param isItemID - The String to place in the ItemID field
   *  @return List containg child loads.
   *  @exception DBException
   */
  public List<Map> getChildLoadsAsLLI(String parentLoad, String isItemID)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sLoadid, ' ' AS \"SORDERID\", '")
             .append(isItemID)
             .append("' AS \"SITEM\", ")
             .append(" ' ' AS \"SLOT\", ' ' AS \"SORDERLOT\", ")
             .append(" ' ' AS \"SLINEID\", 0.0 AS \"FCURRENTQUANTITY\" FROM Load WHERE sParentLoad = '")
             .append(parentLoad).append("' OR sLoadid = '").append(parentLoad).append("'");
    return fetchRecords(vpSql.toString());
  }

  /**
   * Method retrieves a load record using barcode (actually TrackingId) as key.
   *
   * @param isBarcode <code>String</code> containing barcode (tracking id) to
   *            search for.
   * @param withLock <code>int</code> flag indicating if record should be
   *            locked.
   *
   * @return <code>LoadData</code> object. <code>null</code> if no record
   *         found.
   */
  public LoadData getLoadDataFromTrackingId(String isTrackingId, int withLock)
      throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.MCKEY_NAME, isTrackingId);

    return getElement(mpLoadData, withLock);
  }

  /**
   * See how many load retrieves have been sent to station
   *
   * @param isDestination station to check
   * @return int number of loads to be retrieved.
   */
  public int getRetrievesSentLoadCount(String isDestination)
  {
    mpLoadData.clear();
    //
    int count = 0;
    mpLoadData.setInKey(LoadData.LOADMOVESTATUS_NAME, KeyObject.AND,
        new Object[] {DBConstants.RETRIEVESENT, DBConstants.RETRIEVEERROR});

    mpLoadData.setKey(LoadData.NEXTADDRESS_NAME,isDestination);
    try
    {
      count = getCount(mpLoadData);
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println(e);
      count = 0;
    }
    return count;
  }

  /**
   * Fetch a value that if true, the Storing Equipment will only accept
   * the Store command if a load is present at the Storing Station when
   * the command is received. If false, the Storing Equipment will
   * accept the Store command even if there is not a load at the Storing
   * Station when the command is received (the Store command will be
   * applied to the next arriving load).  The default value is TRUE
   * (only accept the command if a Load is present).
   *
   *  @param loadID load identifier
   *  @return true if Load Presence is Yes (Load must be at Store Station)
   */
  public boolean getLoadPresenceRequired(String isLoadID)
  {
    LoadData vpLoadData = null;
    try
    {
      vpLoadData = getParentLoadData(isLoadID);
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println(e);
    }
    if (vpLoadData == null)
    {
      return false;
    }
    if (vpLoadData.getLoadPresenceCheck() == DBConstants.YES)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Return a Load Id associated with a Load Tracking Id.
   *
   * @param isTrackingId load id that needs a tracking id
   * @return the load id associated with the load tracking id
   * @throws DBException
   */
  public String getLoadIdFromTrackingId(String isTrackingId) throws DBException
  {
    String isLoadId = isTrackingId;
    LoadData vpLoadData = getLoadDataFromTrackingId(isTrackingId,
        DBConstants.NOWRITELOCK);
    if (vpLoadData != null)
    {
      isLoadId = vpLoadData.getLoadID();
    }
    return isLoadId;
  }

  /**
   * Get a list of picked loads on an order
   *
   * @return String of load IDs
   * @throws DBException
   */
  public String[] getPickedLoadsOnOrder(String isOrderId) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sLoadID FROM LoadLineItem WHERE sOrderID = '")
      .append(isOrderId)
      .append("'");

    return getList(vpSql.toString(), LoadData.LOADID_NAME,
                   SKDCConstants.NO_PREPENDER);
  }

  /**
   * Get a list of orders for unpicking
   *
   * @return String of order IDs
   * @throws DBException
   */
  public String[] getUnpickLoadList() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sLoadID FROM LoadLineItem WHERE iHoldType = ")
      .append(DBConstants.SHIPHOLD);

    return getList(vpSql.toString(), LoadData.LOADID_NAME,
                   SKDCConstants.NO_PREPENDER);
  }

  /**
   *  Method to get any child loads for this parent.
   *
   *  @param parentLoad Load ID to be deleted.
   *
   *  @return List containing child loads.
   *  @exception DBException
   */
  public int getChildCount(String parentLoad) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"rowCount\" FROM Load WHERE sParentLoad = '")
             .append(parentLoad).append("' AND ")
             .append("sLoadID != '").append(parentLoad).append("'");
    return getRecordCount(vpSql.toString(), "rowCount");
  }

  /**
   * Get the newest load for a device
   *
   * @param isDeviceID
   * @param status
   * @param withLock
   * @return
   * @throws DBException
   */
  public LoadData getNewestLoadDataByDevice(String isDeviceID, int status,
      int withLock) throws DBException
  {
    List<LoadData> arrList = getNewestLoadDataListByDevice(isDeviceID, status,
        withLock);

    if (arrList.isEmpty())
    {
      return null;
    }

    return arrList.get(0);
  }

  /**
   * Get a list of loads for a device in descending move date order.
   *
   * @param isDeviceID
   * @param status
   * @param withLock
   * @return
   * @throws DBException
   */
  private List<LoadData> getNewestLoadDataListByDevice(String isDeviceID,
      int status, int withLock) throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if (isDeviceID.trim().length() == 0)
    {
      throw new DBException(
          "Device Cannot Be Blank for Search For Loads on Device - getNewestLoadDataListByDevice");
    }
    else
    {
      vpSql.append("SELECT * FROM Load WHERE sDeviceID = '")
               .append(isDeviceID).append("' ");
    }

    if (status > 0)
    {
      vpSql.append(" AND iLoadMoveStatus = ").append(status).append(" ");
    }
    if (withLock == DBConstants.WRITELOCK)
    {
      vpSql.append("FOR UPDATE ");
    }

    vpSql.append("ORDER BY dMoveDate DESC");
    List<Map> mapList = fetchRecords(vpSql.toString());
    List<LoadData> loadDataList = new ArrayList<LoadData>();

    for (int i = 0; i < mapList.size(); i++)
    {
      mpLoadData.dataToSKDCData(mapList.get(i));
      loadDataList.add(mpLoadData.clone());
    }

    return loadDataList;
  }

  /**
   * Stage a Load.
   *
   * @param loadID load to be staged.
   * @param location new location of load
   */
  public void stageLoad(String loadID, String location) throws DBException
  {
    String[] lcArray = Location.parseLocation(location);
    if (!(getLoadLocation(loadID).equalsIgnoreCase(location)))
    {
      LoadData vpLoadData = getLoadData(loadID, DBConstants.NOWRITELOCK);
      vpLoadData.setWarehouse(lcArray[0]);
      vpLoadData.setAddress(lcArray[1]);
      vpLoadData.setKey(LoadData.LOADID_NAME, loadID);
      modifyElement(vpLoadData);
    }
    setLoadMoveStatusValue(loadID, DBConstants.STAGED, null);
  }

  /**
   * Ship a Load.
   *
   * @param loadID load to be shipped.
   * @param location new location of load
   */
  public void shipLoad(String loadID, String location) throws DBException
  {
    String[] lcArray = Location.parseLocation(location);
    if (!(getLoadLocation(loadID).equalsIgnoreCase(location)))
    {
      LoadData vpLoadData = getLoadData(loadID, DBConstants.NOWRITELOCK);
      vpLoadData.setWarehouse(lcArray[0]);
      vpLoadData.setAddress(lcArray[1]);
      vpLoadData.setKey(LoadData.LOADID_NAME, loadID);
      modifyElement(vpLoadData);
    }
    setLoadMoveStatusValue(loadID, DBConstants.SHIPWAIT, null);
  }

  /**
   * Retrieves a String[] list of all Loads with
   * a sDeviceID not matching there locations sDeviceID
   *
   * @return reference to an String[] of PurchaseOrder Numbers
   *
   * @exception DBException
   */
  public List<Map> getLoadsWithIncorrectDeviceID() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT Load.sLoadID, Load.sWarehouse, Load.sAddress, Load.sDeviceID")
             .append(" FROM Load, Location")
             .append(" WHERE Load.sWarehouse = Location.sWarehouse and")
             .append(" Load.sAddress = Location.sAddress and")
             .append(" Load.sDeviceID != Location.sDeviceID");

    return fetchRecords(vpSql.toString());
  }

  /**
   * Retrieves a String[] list of all Loads with
   * a sDeviceID not matching there locations sDeviceID
   *
   * @return reference to an String[] of PurchaseOrder Numbers
   *
   * @exception DBException
   */
  public List<Map> getLoadsNoMoveWithNextLoc() throws DBException
  {
    mpLoadData.clear();
    mpLoadData.setKey(LoadData.LOADMOVESTATUS_NAME, DBConstants.NOMOVE);
    mpLoadData.setKey(LoadData.NEXTADDRESS_NAME, null, KeyObject.NOT_EQUAL);
    return getAllElements(mpLoadData);
  }

  /**
   * Retrieves a String[] list of all Loads with
   * that are in a moving, storing, xxxing, etc...status for too long
   *
   * @return
   *
   * @exception DBException
   */
  public List<Map> getLoadsMovingTooLong(Date moveDate) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sLoadID, sWarehouse, sAddress, iLoadMoveStatus, ")
             .append("dMoveDate From Load ")
             .append("Where (iLoadMoveStatus = ")
             .append(DBConstants.RETRIEVING)
             .append(" or iLoadMoveStatus = ")
             .append(DBConstants.BUILDING)
             .append(" or iLoadMoveStatus = ")
             .append(DBConstants.STORING)
             .append(" or iLoadMoveStatus = ")
             .append(DBConstants.MOVING)
             .append(" or iLoadMoveStatus = ")
             .append(DBConstants.STOREERROR)
             .append(" or iLoadMoveStatus = ")
             .append(DBConstants.RETRIEVEERROR)
             .append(" or iLoadMoveStatus = ")
             .append(DBConstants.MOVEERROR)
             .append(" ) and dMoveDate < ")
             .append(DBHelper.convertDateToDBString(moveDate))
             .append(" order by iLoadMoveStatus, dMoveDate ");

    return fetchRecords(vpSql.toString());
  }

 /**
  * Method to get the total fullness present in all loads in the rack for a
  * given device.
  * @param isDevice the device
  * @return double value representing total amount full.
  * @throws DBException if there is a database access error.
  */
  public double getTotalLoadFullnessByDevice(String isDevice) throws DBException
  {
    final String TOTALS_COLUMN_NAME = "IAMTFULLTOTAL";
    double vdTotalAmtFullness = 0;

    StringBuilder vpSql = new StringBuilder("SELECT iAmountFull, COUNT(iAmountFull) AS \"").append(TOTALS_COLUMN_NAME)
             .append("\" FROM Load WHERE sDeviceID = ? AND ")
             .append("iAmountFull != ").append(DBConstants.EMPTY).append(" AND ")
             .append("iLoadMoveStatus = ").append(DBConstants.NOMOVE)
             .append(" GROUP BY iAmountFull");

    List<Map> vpGroupedList = fetchRecords(vpSql.toString(), isDevice);
    if (vpGroupedList.isEmpty()) return(0);

    Set<Map.Entry<String, AmountFullTransMapper>> vpMapSet =
                                  LoadData.getAmountFullDecimalMap().entrySet();

    double vdFractionalFullness = 0, vdFullnessCount = 0;
    int vnCandidateTrans, vnAmtFullTran;

    for(Map vpDataMap : vpGroupedList)
    {
      vnAmtFullTran = DBHelper.getIntegerField(vpDataMap, LoadData.AMOUNTFULL_NAME);
      vdFullnessCount = DBHelper.getIntegerField(vpDataMap, TOTALS_COLUMN_NAME);

      if (vnAmtFullTran == mpLoadData.getAmountFullnessTrans())
      {
        vdTotalAmtFullness += vdFullnessCount;
      }
      else
      {
        for(Map.Entry<String, AmountFullTransMapper> vpMap : vpMapSet)
        {
          vnCandidateTrans = vpMap.getValue().getPartialAmtFullTranVal();
          vdFractionalFullness = vpMap.getValue().getPartialAmtFullDecimal();
          if (vnCandidateTrans == vnAmtFullTran)
          {
            vdTotalAmtFullness += vdFullnessCount*vdFractionalFullness;
            break;
          }
        }
      }
    }

    return(vdTotalAmtFullness);
  }

  /**
   * Method to get a count of all the loads in the rack that are not moving
   * for a device
   * @param isDevice
   * @return
   * @throws DBException
   */
  public int getLoadCountByDevice(String isDevice) throws DBException
  {
    int vnCount = 0;
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(ld.SLOADID) ").append("AS \"")
               .append(ROW_COUNT_NAME).append("\" ").append("FROM LOAD ld, ")
               .append("LOCATION lc WHERE ld.SDEVICEID = ? ")
               .append("AND ld.SADDRESS = lc.SADDRESS AND ld.SDEVICEID ")
               .append("= lc.SDEVICEID AND lc.ILOCATIONTYPE = ")
               .append(DBConstants.LCASRS).append(" AND lc.ILOCATIONSTATUS = ")
               .append(DBConstants.LCAVAIL).append(" AND ld.ILOADMOVESTATUS = ")
               .append(DBConstants.NOMOVE);
    vnCount = getRecordCount(vpSql.toString(), ROW_COUNT_NAME, isDevice);
    return vnCount;

  }
}

