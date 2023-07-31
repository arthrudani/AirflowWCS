package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2009 Daifuku America Corporation  All Rights Reserved.

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
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Description:<BR>
 *   Class to handle Location specific operations.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 24-Oct-01<BR>
 *     Copyright (c) 2001<BR>
 *     Company:  Daifuku America Corporation
 */
public class Location extends BaseDBInterface
{
  /*========================================================================*/
  /*  Pre-built SQL                                                         */
  /*========================================================================*/
  protected static String SQL_EMPTY_LOCATION_BASE =
    "SELECT " + LocationData.WAREHOUSE_NAME + ", " + LocationData.ADDRESS_NAME
      + " FROM Location WHERE "
      + LocationData.DEVICEID_NAME + " = ? AND "
      + LocationData.EMPTYFLAG_NAME + " = " + DBConstants.UNOCCUPIED + " AND "
      + LocationData.LOCATIONSTATUS_NAME + " = " + DBConstants.LCAVAIL + " AND "
      + "(" + LocationData.LOCATIONTYPE_NAME + " = " + DBConstants.LCASRS + " OR "
            + LocationData.LOCATIONTYPE_NAME + " = " + DBConstants.LCCONVSTORAGE + ") AND "
      + LocationData.HEIGHT_NAME + " >= ? ";

  protected static String SQL_EMPTY_ORDERING = " ORDER BY "
      + LocationData.HEIGHT_NAME + "," + LocationData.SEARCHORDER_NAME + ","
      + LocationData.ADDRESS_NAME;

  protected static String SQL_EMPTY_LOCATION = SQL_EMPTY_LOCATION_BASE
      + SQL_EMPTY_ORDERING;

  protected static String SQL_EMPTY_LOCATION_BY_ZONE = SQL_EMPTY_LOCATION_BASE
      + " AND " + LocationData.ZONE_NAME + " = ? " + SQL_EMPTY_ORDERING;

  /**
   * Constructor
   */
  public Location()
  {
    super("Location");
  }

  /**
   * Get a location record
   * 
   * @param isWarehouse
   * @param isAddress
   * @return
   * @throws DBException
   */
  public LocationData getLocation(String isWarehouse, String isAddress)
      throws DBException
  {
    LocationData vpKey = Factory.create(LocationData.class);
    vpKey.setKey(LocationData.WAREHOUSE_NAME, isWarehouse);
    vpKey.setKey(LocationData.ADDRESS_NAME, isAddress);
    return getElement(vpKey, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Retrieves one column value from the Location table.
   *
   * @param isWarehouse the Location Warehouse.
   * @param isAddress the Location Address.
   * @param isColumnName the name of the column whose value is returned.
   * @return value of column specified by isColumnName as an <code>Object</code>.
   *         The caller is assumed to know what data type is actually in
   *         <code>Object</code>. <i>A</i> <code>null</code> <i>object is
   *         returned for no matching data</i>
   * @throws DBException when database access errors occur.
   */
  @SuppressWarnings("rawtypes")
  public Object getSingleColumnValue(String isWarehouse, String isAddress,
                                     String isColumnName) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName)
        .append(" FROM ").append(getReadTableName())
        .append(" WHERE ").append("sWarehouse = ?")
        .append("   AND ").append("sAddress = ?");
    List<Map> vpData = fetchRecords(vpSql.toString(), isWarehouse, isAddress);

    return (!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null;
  }

  /**
   * Method to get List of valid Location Addresses given range to check.
   *
   * @param isWarehouse <code>String</code> containing Warehouse of the location
   * @param isBegAddress <code>String</code> containing Beginning address
   * @param isEndAddress <code>String</code> containing Ending address
   * @param isAllOrNone <code>String</code> containing string to start list
   *            with. The values are SKDCConstants.ALL_STRING or
   *            SKDCConstants.NONE_STRING
   *
   * @return StringBuffer Addresses.
   */
  public String[] getAddressChoices(String isWarehouse, String isBegAddress,
                                    String isEndAddress, String isAllOrNone)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT " + LocationData.ADDRESS_NAME + " FROM ").append(getReadTableName())
             .append(" WHERE sWarehouse = '").append(isWarehouse).append("' AND ")
             .append("sAddress BETWEEN '").append(isBegAddress).append("' AND '")
             .append(isEndAddress).append("'");

    return getList(vpSql.toString(), LocationData.ADDRESS_NAME,
                   isAllOrNone);
  }

  /**
   * Method to set Location Empty flag.
   */
  public void setEmptyFlagValue(String isWarehouse, String isAddress,
                                int inEmptyFlag) throws DBException
  {
    LocationData vpLocData = Factory.create(LocationData.class);
    vpLocData.setKey(LocationData.WAREHOUSE_NAME, isWarehouse);
    vpLocData.setKey(LocationData.ADDRESS_NAME, isAddress);
    vpLocData.setEmptyFlag(inEmptyFlag);
    modifyElement(vpLocData);
  }
  
  /**
   * Method to set Location Shelf Position.
   * 
   * @param isWarehouse - warehouse name
   * @param isAddress - location address
   * @param sShelfPos - new shelf position
   * @throws DBException - If anything goes wrong
   */
  public void setShelfPositionValue(String isWarehouse, String isAddress,
                                String sShelfPos) throws DBException
  {
    LocationData vpLocData = Factory.create(LocationData.class);
    vpLocData.setKey(LocationData.WAREHOUSE_NAME, isWarehouse);
    vpLocData.setKey(LocationData.ADDRESS_NAME, isAddress);
    vpLocData.setShelfPosition(sShelfPos);
    modifyElement(vpLocData);
  }
  
  /**
   * Method to get Location Shelf Position.
   * 
   * @param warehouse - warehouse name
   * @param address - location address
   * @return String containing Current Shelf Position.
   * @throws DBException
   */
  public String getShelfPositionValue(String warehouse, String address)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(LocationData.SHELFPOSITION_NAME)
        .append(" FROM ").append(getReadTableName())
        .append(" WHERE sWarehouse = ?")
        .append(" AND sAddress = ?");
    return getStringColumn(LocationData.SHELFPOSITION_NAME, vpSql.toString(), warehouse, address);
  }
  
  /**
   * Method to get Location Empty Flag.
   *
   * @return int containing Empty Flag.
   */
  public int getEmptyFlagValue(String warehouse, String address)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(LocationData.EMPTYFLAG_NAME)
        .append(" FROM ").append(getReadTableName())
        .append(" WHERE sWarehouse = ?")
        .append(" AND sAddress = ?");
    return getIntegerColumn(LocationData.EMPTYFLAG_NAME, vpSql.toString(), warehouse, address);
  }

  /**
   * Method to get Location Type Flag.
   *  @param warehouse <code>String</code> containing location warehouse.
   *  @param address <code>String</code> containing location address.
   * @return int containing Location Type Flag.
   */
  public int getLocationTypeValue(String warehouse, String address)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(LocationData.LOCATIONTYPE_NAME)
        .append(" FROM ").append(getReadTableName())
        .append(" WHERE sWarehouse = ?")
        .append(" AND sAddress = ?");

    return getIntegerColumn(LocationData.LOCATIONTYPE_NAME, vpSql.toString(), warehouse, address);
  }

  /**
   * Method to get Location Device.
   *
   *  @param warehouse <code>String</code> containing location warehouse.
   *  @param address <code>String</code> containing location address.
   *
   * @return String containing Location Device.
   */
  public String getLocationDevice(String warehouse, String address)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(LocationData.DEVICEID_NAME)
        .append(" FROM ").append(getReadTableName())
        .append(" WHERE sWarehouse = ?")
        .append(" AND sAddress = ?");

    return getStringColumn(LocationData.DEVICEID_NAME, vpSql.toString(), warehouse, address);
  }

  /**
   * Method to get Location Type Flag.
   *
   *  @param warehouse <code>String</code> containing location warehouse.
   *  @param address <code>String</code> containing location address.
   *
   * @return int containing Location status Flag.
   */
  public int getLocationStatusValue(String warehouse, String address)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(LocationData.LOCATIONSTATUS_NAME)
        .append(" FROM ").append(getReadTableName())
        .append(" WHERE sWarehouse = ?")
        .append(" AND sAddress = ?");

    return getIntegerColumn(LocationData.LOCATIONSTATUS_NAME, vpSql.toString(), warehouse, address);
  }

  /**
   * Method to set Location Status.
   */
  public void setLocationStatusValue(String warehouse, String address,
                                     int newStatus) throws DBException
  {
    LocationData vpLocData = Factory.create(LocationData.class);
    vpLocData.setLocationStatus(newStatus);
    vpLocData.setKey(LocationData.WAREHOUSE_NAME, warehouse);
    vpLocData.setKey(LocationData.ADDRESS_NAME, address);
    modifyElement(vpLocData);
  }

  /**
   * Builds rack Location addresses.
   * @param  beg_address String containing beginning address.
   * @param  end_address String containing ending address.
   *
   * @return StringBuffer containing addresses.
   */
  public String[] getRackAddressRange(String beg_address, String end_address)
  throws ArrayIndexOutOfBoundsException, IOException, NumberFormatException
  {
    RackLocationParser begaddr = RackLocationParser.parse(beg_address, true);
    RackLocationParser endaddr = RackLocationParser.parse(end_address, true);

    // The beginning bank, bay, and tier.
    int iBegBank = begaddr.getBankInteger();
    int iBegBay  = begaddr.getBayInteger();
    int iBegTier = begaddr.getTierInteger();
    // The ending bank, bay, and tier.
    int iEndBank = endaddr.getBankInteger();
    int iEndBay  = endaddr.getBayInteger();
    int iEndTier = endaddr.getTierInteger();

    if (iBegBank == 0 || iBegBay == 0 || iBegTier == 0)
    {
      throw new IOException("Bank, Bay, or Tier must be non-zero!");
    }
    else if (iEndBank < iBegBank)
    {
      throw new ArrayIndexOutOfBoundsException("Invalid ending Bank.");
    }
    else if (iEndBay < iBegBay)
    {
      throw new ArrayIndexOutOfBoundsException("Invalid ending Bay.");
    }
    else if (iEndTier < iBegTier)
    {
      throw new ArrayIndexOutOfBoundsException("Invalid ending Tier.");
    }

    NumberFormat nf3 = NumberFormat.getInstance();
    nf3.setMinimumIntegerDigits(DBConstants.LNBANK);

    String sBank = "";
    String sBay = "";
    int idx = 0;
    // Figure out how big to make the array
    int total_locn  = (iEndBank - iBegBank + 1);
    total_locn *= (iEndBay - iBegBay + 1);
    total_locn *= (iEndTier - iBegTier + 1);

    String[] loc_addr = new String[total_locn];

    for (int bank = iBegBank; bank <= iEndBank; bank++)
    {
      sBank = nf3.format(bank);
      for (int bay = iBegBay; bay <= iEndBay; bay++)
      {
        sBay = nf3.format(bay);
        for (int tier = iBegTier; tier <= iEndTier; tier++)
        {
          loc_addr[idx] = sBank + sBay + nf3.format(tier);
          idx++;
        }
      }
    }

    begaddr = endaddr = null;

    return loc_addr;
  }

  /**
   * Builds non-rack Location addresses.
   * @param  beg_address String containing beginning address.
   * @param  end_address String containing ending address.
   *
   * @return StringBuffer containing addresses.
   */
  public String[] getAddressRange(String isBegAddress, String isEndAddress)
      throws ArrayIndexOutOfBoundsException, IOException
  {
    int i = 0;
    char[] vpBegAddress = isBegAddress.toCharArray();
    char[] vpEndAddress = isEndAddress.toCharArray();
    int vnBegAddress = 0;
    int vnEndAddress = 0;

    if (isBegAddress.equals(isEndAddress))
    {
      String[] loc_addr = new String[1];
      loc_addr[0] = isBegAddress;
      return loc_addr;
    }

    while (i < vpBegAddress.length && i < vpEndAddress.length &&
        vpBegAddress[i] == vpEndAddress[i])
    {
      i++;
    }
    while (i>0 && Character.isDigit(vpBegAddress[i-1]))
    {
      i--;
    }

    if (i==0)
    {
      // check for beginning zeroes - we don't want them parsed away
      while (i<vpBegAddress.length && i < vpEndAddress.length &&
          (vpBegAddress[i] == vpEndAddress[i]) && (vpEndAddress[i] == '0'))
      {
        i++;
      }
    }

    try
    {
      vnBegAddress = Integer.parseInt(isBegAddress.substring(i));
      vnEndAddress = Integer.parseInt(isEndAddress.substring(i));
    }
    catch (NumberFormatException nfe) {}

    if (vnBegAddress == 0 || vnEndAddress == 0)
    {
      throw new IOException("Beginning and Ending Differing parts must be non-zero!");
    }
    else if (vnEndAddress < vnBegAddress)
    {
      throw new ArrayIndexOutOfBoundsException("Invalid ending Address.");
    }

    int idx = 0;
    // Figure out how big to make the array
    int total_locn  = (vnEndAddress - vnBegAddress + 1);

    String[] loc_addr = new String[total_locn];

    int vnIntLength = isBegAddress.substring(i).length();
    String vsPrefix = isBegAddress.substring(0,i);
    String vsSuffix = "";

    for(int vnAddress = vnBegAddress; vnAddress <= vnEndAddress; vnAddress++)
    {
      vsSuffix = String.valueOf(vnAddress);
      while (vsSuffix.length() < vnIntLength)
      {
        vsSuffix = "0" + vsSuffix;
      }
      loc_addr[idx++] = vsPrefix + vsSuffix;
    }
    return loc_addr;
  }

  /**
   * Gets an array of all valid location heights
   * @return
   */
  @SuppressWarnings("rawtypes")
  public Integer[] getLocationHeights() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(LocationData.HEIGHT_NAME)
             .append(" FROM ").append(getReadTableName())
             .append(" ORDER BY ").append(LocationData.HEIGHT_NAME);
    List<Map> vpHeights = fetchRecords(vpSql.toString());
    Integer[] vanHeights = new Integer[vpHeights.size()];
    for (int i = 0; i < vanHeights.length; i++)
    {
      vanHeights[i] = Integer.parseInt(vpHeights.get(i).get(
          LocationData.HEIGHT_NAME).toString());
    }
    return vanHeights;
  }

  /**
   * Method to check for Location existence.
   *
   * @return boolean of true if it exists, false otherwise.
   */
  @SuppressWarnings("rawtypes")
  public boolean warehouseExists(String warehouse)
  {
    boolean rtn = false;

    StringBuilder vpSql = new StringBuilder("SELECT ")
             .append(LocationData.WAREHOUSE_NAME)
             .append(" FROM ").append(getReadTableName())
             .append(" WHERE ").append(LocationData.WAREHOUSE_NAME).append(" = ?");

    try
    {
      List<Map> arr_list = fetchRecords(vpSql.toString(), warehouse);
      if (arr_list == null) rtn = false;
      else                  rtn = (arr_list.size() > 0);
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println("Error " + e + " checking Location existence");
    }

    return rtn;
  }

  /**
   * Get an empty location by zone
   *
   * @param isDevice
   * @param inHeight
   * @param isZone
   * @return String[2] { Warehouse, Address } or <code>null</code>
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public String[] findEmptyLocationByZone(String isDevice, int inHeight, String isZone)
    throws DBException
  {
    List<Map> vpLocList = null;
    String[] vasEmptyLoc = null;

    try
    {
      setMaxRows(1);

      if (isZone.trim().length() > 0)
      {
        vpLocList = fetchRecords(SQL_EMPTY_LOCATION_BY_ZONE, isDevice, inHeight,
            isZone);
      }
      else
      {
        vpLocList = fetchRecords(SQL_EMPTY_LOCATION, isDevice, inHeight);
      }
      if (vpLocList.size() > 0)
      {
        vasEmptyLoc = new String[2];
        vasEmptyLoc[0] = DBHelper.getStringField(vpLocList.get(0), LocationData.WAREHOUSE_NAME);
        vasEmptyLoc[1] = DBHelper.getStringField(vpLocList.get(0), LocationData.ADDRESS_NAME);
      }
      else
      {
        return null;
      }
    }
    finally
    {
      setMaxRows();
    }

    return vasEmptyLoc;
  }

  /**
   * Parses a Location into a warehouse and an address part. The Warehouse
   * portion will assumed to be up to the dash position. If the dash is not
   * provided, it is assumed that the warehouse is LNWAREHOUSE characters.
   *
   * @param sLocation <code>String</code> containing Location to parse.
   *
   * @return <code>String[]</code> containing the warehouse in
   *         <code>String[0]</code> and address in <code>String[1]</code>
   */
  public static String[] parseLocation(String sLocation)
  {
    String[] parsedString = new String[2];
    if (sLocation.trim().length() == 0 || sLocation.trim().equals(SKDCConstants.DESCRIPTION_SEPARATOR))
    {
      parsedString[0] = "";
      parsedString[1] = "";
    }
    else
    {
      int whsEnd, addrOffset, dashPosition;
      int vnWarehouseLength = DBInfo.getFieldLength(LocationData.WAREHOUSE_NAME);

      if ((dashPosition = sLocation.indexOf('-')) == -1)
      {
        if (sLocation.length() > vnWarehouseLength)
        {
          whsEnd = vnWarehouseLength;
          addrOffset = vnWarehouseLength;
        }
        else
        {
          whsEnd = sLocation.length();
          addrOffset = sLocation.length();
        }
      }
      else
      {
        whsEnd = (dashPosition <= vnWarehouseLength) ? dashPosition
                                                           : vnWarehouseLength;
        addrOffset = dashPosition + 1;
      }

      parsedString[0] = sLocation.substring(0, whsEnd);
      parsedString[1] = sLocation.substring(addrOffset);
    }

    return parsedString;
  }

  /**
   *  Method checks if two locations are next to each other.
   *
   *  @param sWarehouse <code>String</code> containing Location warehouse.
   *  @param sAddress1 <code>String</code> containing first Location Address.
   *  @param sAddress2 <code>String</code> containing second Location Address.
   *
   *  @return <code>boolean</code> value of <code>true</code> if location is
   *          paired. Returns <code>false</code> otherwise.
   */
  public boolean isPairedRackLocation(String sWarehouse, String sAddress1,
                                      String sAddress2) throws DBException
  {
    if (sAddress1.equals(sAddress2)) return false;

    StringBuilder vpSql = new StringBuilder("SELECT COUNT(sWarehouse) AS \"rowCount\" FROM ")
             .append(getReadTableName()).append(" WHERE ")
             .append("sWarehouse = '").append(sWarehouse).append("' AND ")
             .append("saddress = '").append(sAddress1).append("' OR ")
             .append("saddress = '").append(sAddress2).append("'");

    RackLocationParser firstAddress = null;
    RackLocationParser secondAddress = null;
    try
    {
      if (getRecordCount(vpSql.toString(), "rowCount") <= 1)
      {                                  // Atleast one location didn't exist.
        return false;
      }
      firstAddress = RackLocationParser.parse(sAddress1, true);
      secondAddress = RackLocationParser.parse(sAddress2, true);
    }
    catch(DBException cntExc)
    {
      throw new DBException("Error counting rows -->Location.isPairedRackLocation()");
    }
    catch(IOException ioe)
    {
      throw new DBException("Rack Location Parse Error", ioe);
    }

    int iFirstBank = firstAddress.getBankInteger();
    int iFirstBay  = firstAddress.getBayInteger();
    int iFirstTier = firstAddress.getTierInteger();
    int iSecondBank = secondAddress.getBankInteger();
    int iSecondBay  = secondAddress.getBayInteger();
    int iSecondTier = secondAddress.getTierInteger();
    boolean rtn = false;

    if (iFirstBank == iSecondBank && iFirstTier == iSecondTier)
    {
      if (iFirstBay+1 == iSecondBay || iFirstBay == iSecondBay+1)
      {
        rtn = true;
      }
    }

    return rtn;
  }

  /**
   *  Method validates if a location is RESERVED or OCCUPIED.  This method does
   *  <b>not</b> depend on the location's Empty flag to be set correctly for its
   *  validation.
   *
   *  @param sWarehouse <code>String</code> containing location warehouse.
   *  @param sAddress <code>String</code> containing location address.
   *
   *  @return <code>boolean</code> of <code>true</code> if the location is
   *          truely not empty, <code>false</code> otherwise.
   */
  public boolean locationIsNotEmpty(String sWarehouse, String sAddress)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(sAddress) AS \"rowCount\" FROM Load WHERE ")
               .append("(sWarehouse = '").append(sWarehouse).append("' AND ")
               .append("sAddress = '").append(sAddress).append("') OR ")
               .append("(sNextWarehouse = '").append(sWarehouse).append("' AND ")
               .append("sNextAddress = '").append(sAddress).append("')");

    return getRecordCount(vpSql.toString(), "rowCount") > 0;
  }

  /**
   * Populate the local global lcdata with settings from the maintenance screen.
   *
   * @param ipLocationData
   * @param inZoneModifier
   * @param inDeviceModifier
   * @param inAisleModifier
   * @param inStatusModifier
   * @param inOccupiedModifier
   * @param inHeightModifier
   * @param inDeleteModifier
   * @param inTypeModifier
   * @param inSwapModifier
   */
  private LocationData populateLCDataWithMaintData(LocationData ipLocationData,
      int inZoneModifier, int inDeviceModifier, int inAisleModifier,
      int inStatusModifier, int inOccupiedModifier, int inHeightModifier,
      int inDeleteModifier, int inTypeModifier, int inSwapModifier)
  {
    LocationData vpLocData = Factory.create(LocationData.class);
    if (inZoneModifier == DBConstants.SET)
      vpLocData.setZone(ipLocationData.getZone());

    if (inDeviceModifier == DBConstants.SET)
      vpLocData.setDeviceID(ipLocationData.getDeviceID());

    if (inAisleModifier == DBConstants.SET)
      vpLocData.setAisleGroup(ipLocationData.getAisleGroup());

    if (inStatusModifier == DBConstants.SET)
      vpLocData.setLocationStatus(ipLocationData.getLocationStatus());

    if (inOccupiedModifier == DBConstants.SET)
      vpLocData.setEmptyFlag(ipLocationData.getEmptyFlag());

    if (inHeightModifier == DBConstants.SET)
      vpLocData.setHeight(ipLocationData.getHeight());

    if (inDeleteModifier == DBConstants.SET)
      vpLocData.setAllowDeletion(ipLocationData.getAllowDeletion());

    if (inTypeModifier == DBConstants.SET)
      vpLocData.setLocationType(ipLocationData.getLocationType());

    if (inSwapModifier == DBConstants.SET)
      vpLocData.setSwapZone(ipLocationData.getSwapZone());

    /*-------------------------------------------------------------------------
     * Set up the Key object to find data for modification
     *-----------------------------------------------------------------------*/
    if (inZoneModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.ZONE_NAME, ipLocationData.getZone());

    if (inDeviceModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.DEVICEID_NAME, ipLocationData.getDeviceID());

    if (inAisleModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.AISLEGROUP_NAME,
        Integer.valueOf(ipLocationData.getAisleGroup()));

    if (inStatusModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.LOCATIONSTATUS_NAME,
        Integer.valueOf(ipLocationData.getLocationStatus()));

    if (inOccupiedModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.EMPTYFLAG_NAME,
        Integer.valueOf(ipLocationData.getEmptyFlag()));

    if (inHeightModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.HEIGHT_NAME,
        Integer.valueOf(ipLocationData.getHeight()));

    if(inDeleteModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.ALLOWDELETION_NAME,
        Integer.valueOf(ipLocationData.getAllowDeletion()));

    if (inTypeModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.LOCATIONTYPE_NAME,
        Integer.valueOf(ipLocationData.getLocationType()));

    if (inSwapModifier == DBConstants.EQUALS)
      vpLocData.setKey(LocationData.SWAPZONE_NAME,
        Integer.valueOf(ipLocationData.getSwapZone()));
    return vpLocData;
  }

  /**
   * Update all locations between starting and ending address
   *
   * @param locationData
   * @param zoneModifier
   * @param deviceModifier
   * @param aisleModifier
   * @param statusModifier
   * @param occupiedModifier
   * @param heightModifier
   * @param deleteModifier
   * @param typeModifier
   * @param inSwapModifier
   * @return
   * @throws DBException
   */
  public int updateLocationsData(LocationData locationData,
      int zoneModifier, int deviceModifier, int aisleModifier,
      int statusModifier, int occupiedModifier, int heightModifier,
      int deleteModifier, int typeModifier, int inSwapModifier)
      throws DBException
  {
    LocationData vpLocData = populateLCDataWithMaintData(locationData, zoneModifier, deviceModifier,
        aisleModifier, statusModifier, occupiedModifier, heightModifier,
        deleteModifier, typeModifier, inSwapModifier);

    vpLocData.setKey(LocationData.WAREHOUSE_NAME, locationData.getWarehouse());
    vpLocData.setBetweenKey(LocationData.ADDRESS_NAME, locationData.getAddress(),
        locationData.getEndingAddress(), 0);

    int modifiedCount = getCount(vpLocData);
    modifyElement(vpLocData);

    return modifiedCount;
  }

  /**
   * Update all locations between starting and ending address, bounded by
   * bank-bay-tier
   *
   * @param locationData
   * @param zoneModifier
   * @param deviceModifier
   * @param aisleModifier
   * @param statusModifier
   * @param occupiedModifier
   * @param heightModifier
   * @param deleteModifier
   * @param typeModifier
   * @param inSwapModifier
   * @return
   * @throws DBException
   */
  public int updateLocationsDataBankBayTier(LocationData locationData,
      int zoneModifier , int deviceModifier, int aisleModifier,
      int statusModifier, int occupiedModifier, int heightModifier,
      int deleteModifier, int typeModifier, int inSwapModifier)
      throws DBException
  {
    LocationData vpLocData = populateLCDataWithMaintData(locationData, zoneModifier, deviceModifier,
        aisleModifier, statusModifier, occupiedModifier, heightModifier,
        deleteModifier, typeModifier, inSwapModifier);

    try
    {
      RackLocationParser vpStartAddress =
        RackLocationParser.parse(locationData.getAddress(), true);
      RackLocationParser vpEndAddress =
        RackLocationParser.parse(locationData.getEndingAddress(), true);

      // The beginning bank, bay, and tier.
      String vsBegBank = "'" + vpStartAddress.getBankString() + "'";
      String vsBegBay  = "'" + vpStartAddress.getBayString() + "'";
      String vsBegTier = "'" + vpStartAddress.getTierString() + "'";
      // The ending bank, bay, and tier.
      String vsEndBank = "'" + vpEndAddress.getBankString() + "'";
      String vsEndBay  = "'" + vpEndAddress.getBayString() + "'";
      String vsEndTier = "'" + vpEndAddress.getTierString() + "'";

      String vsBankColumn = "";
      String vsBayColumn = "";
      String vsTierColumn = "";

      if (DBInfo.USING_ORACLE_DB)
      {
        vsBankColumn = "SUBSTR(" + LocationData.ADDRESS_NAME + ", 0, " +
                       DBConstants.LNBANK + ")";
        vsBayColumn = "SUBSTR(" + LocationData.ADDRESS_NAME + ", " +
                      (DBConstants.LNBANK + 1) + ", " +
                      DBConstants.LNBAY + ")";
        vsTierColumn = "SUBSTR(" + LocationData.ADDRESS_NAME + ", " +
                       (DBConstants.LNBANK + DBConstants.LNBAY + 1) + ", " +
                       DBConstants.LNTIER + ")";
      }
      else if (DBInfo.USING_SQL_SERVER)
      {
        vsBankColumn = "SUBSTRING(" + LocationData.ADDRESS_NAME + ", 1, " +
                       DBConstants.LNBANK + ")";
        vsBayColumn = "SUBSTRING(" + LocationData.ADDRESS_NAME + ", " +
                      (DBConstants.LNBANK + 1) + ", " +
                      DBConstants.LNBAY + ")";
        vsTierColumn = "SUBSTRING(" + LocationData.ADDRESS_NAME + ", " +
                       (DBConstants.LNBANK + DBConstants.LNBAY + 1) + ", " +
                       DBConstants.LNTIER + ")";
      }
      vpLocData.setKey(LocationData.WAREHOUSE_NAME, locationData.getWarehouse());
      vpLocData.setBetweenKey(vsBankColumn, vsBegBank, vsEndBank, 0);
      vpLocData.setBetweenKey(vsBayColumn , vsBegBay , vsEndBay , 0);
      vpLocData.setBetweenKey(vsTierColumn, vsBegTier, vsEndTier, 0);
    }
    catch (IOException ioe)
    {
      throw new DBException(ioe);
    }

    int modifiedCount = getCount(vpLocData);
    modifyElement(vpLocData);

    return modifiedCount;
  }

  /**
   * Change the search order of a location
   *
   * @param warehouse
   * @param address
   * @param searchOrder
   * @throws DBException
   */
  public void changeSearchOrder(String warehouse, String address, int searchOrder)
         throws DBException
  {
    LocationData vpLocData = Factory.create(LocationData.class);
    vpLocData.setSearchOrder(searchOrder);
    vpLocData.setKey(LocationData.WAREHOUSE_NAME, warehouse);
    vpLocData.setKey(LocationData.ADDRESS_NAME, address);
    modifyElement(vpLocData);
  }

  /**
   * Clear any references to the specified zone
   *
   * @param isZone
   * @throws DBException
   */
  public void deleteZone(String isZone) throws DBException
  {
    try
    {
      StringBuilder vpSql = new StringBuilder("UPDATE ").append(getWriteTableName())
          .append(" SET SZONE=NULL WHERE SZONE=?");
      execute(vpSql.toString(), isZone);
    }
    catch (NoSuchElementException nsee)
    {
      // This is okay
    }
  }

  /**
   * Retrieves a String[] list of all Locations with
   * an incorrect EmptyFlag
   *
   * @return List of Maps containing:
   *   <UL>
   *   <LI>LocationData.WAREHOUSE_NAME
   *   <LI>LocationData.ADDRESS_NAME
   *   <LI>LocationData.EMPTYFLAG_NAME
   *   <LI>LoadData.LOADID_NAME
   *   </UL>
   *
   * @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getAsrsLocationsEmptyWithLoads() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT LC.").append(LocationData.WAREHOUSE_NAME)
               .append(", LC.").append(LocationData.ADDRESS_NAME)
               .append(", LC.").append(LocationData.EMPTYFLAG_NAME)
               .append(", LD.").append(LoadData.LOADID_NAME)
               .append(" FROM ").append(getReadTableName())
               .append(" LC, LOAD LD WHERE LC.").append(LocationData.EMPTYFLAG_NAME)
               .append("=").append(DBConstants.UNOCCUPIED)
               .append(" AND LC.").append(LocationData.LOCATIONTYPE_NAME)
               .append("=").append(DBConstants.LCASRS)
               .append(" AND LC.").append(LocationData.WAREHOUSE_NAME)
               .append("=LD.").append(LoadData.WAREHOUSE_NAME)
               .append(" AND LC.").append(LocationData.ADDRESS_NAME)
               .append("=LD.").append(LoadData.ADDRESS_NAME)
               .append(" ORDER BY LC.").append(LocationData.WAREHOUSE_NAME)
               .append(", LC.").append(LocationData.ADDRESS_NAME);

    return fetchRecords(vpSql.toString());
  }

  /**
   * Retrieves a String[] list of all Locations with an incorrect EmptyFlag,
   * Occupied location without a load
   *
   * @param isWarehouse
   * @return reference to an String[] of PurchaseOrder Numbers
   *
   * @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getAsrsLocationsFullWithoutLoads(String isWarehouse)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(LocationData.ADDRESS_NAME)
               .append(" FROM ").append(getReadTableName())
               .append(" WHERE ").append(LocationData.EMPTYFLAG_NAME)
               .append("=").append(DBConstants.OCCUPIED)
               .append(" AND ").append(LocationData.LOCATIONTYPE_NAME)
               .append("=").append(DBConstants.LCASRS)
               .append(" AND ").append(LocationData.WAREHOUSE_NAME).append("=?")
               .append(" AND ").append(LocationData.ADDRESS_NAME)
               .append(" NOT IN (SELECT LD.").append(LoadData.ADDRESS_NAME)
               .append(" FROM LOAD LD WHERE LD.").append(LoadData.WAREHOUSE_NAME)
               .append("=?) ORDER BY ").append(LocationData.ADDRESS_NAME);

    return fetchRecords(vpSql.toString(), isWarehouse, isWarehouse);
  }

  /**
   * Retrieves a String[] list of all Locations with an incorrect EmptyFlag,
   * Occupied location without a load
   *
   * @param isWarehouse
   * @return reference to an String[] of PurchaseOrder Numbers
   *
   * @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getAsrsLocationsReservedWithoutLoads(String isWarehouse)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(LocationData.ADDRESS_NAME)
               .append(" FROM ").append(getReadTableName())
               .append(" WHERE ").append(LocationData.EMPTYFLAG_NAME)
               .append("=").append(DBConstants.LCRESERVED)
               .append(" AND ").append(LocationData.LOCATIONTYPE_NAME)
               .append("=").append(DBConstants.LCASRS)
               .append(" AND ").append(LocationData.WAREHOUSE_NAME).append("=?")
               .append(" AND ").append(LocationData.ADDRESS_NAME)
               .append(" NOT IN (SELECT LD.").append(LoadData.NEXTADDRESS_NAME)
               .append(" FROM LOAD LD WHERE LD.").append(LoadData.WAREHOUSE_NAME)
               .append("=? AND LD.").append(LoadData.NEXTADDRESS_NAME)
               .append(" IS NOT NULL) ORDER BY ").append(LocationData.ADDRESS_NAME);

    return fetchRecords(vpSql.toString(), isWarehouse, isWarehouse);
  }

  /**
   * Retrieves a String[] list of all Locations with an incorrect Available
   * status without a load
   *
   * @param isWarehouse
   * @return reference to an String[] of locations
   *
   * @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getAsrsLocationsUnavailWithoutLoads(String isWarehouse)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(LocationData.ADDRESS_NAME)
               .append(" FROM ").append(getReadTableName())
               .append(" WHERE ").append(LocationData.LOCATIONSTATUS_NAME)
               .append("=").append(DBConstants.LCUNAVAIL)
               .append(" AND ").append(LocationData.EMPTYFLAG_NAME)
               .append("=").append(DBConstants.UNOCCUPIED)
               .append(" AND ").append(LocationData.LOCATIONTYPE_NAME)
               .append("=").append(DBConstants.LCASRS)
               .append(" AND ").append(LocationData.WAREHOUSE_NAME)
               .append("=? ORDER BY ").append(LocationData.ADDRESS_NAME);

    return fetchRecords(vpSql.toString(), isWarehouse);
  }

  /**
   * Retrieves a String[] list of all Locations with an incorrect EmptyFlag
   *
   * @return reference to an String[] of PurchaseOrder Numbers
   *
   * @exception DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getAsrsLocationsUnavailWithLoads(String isWarehouse)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT LC.").append(LocationData.ADDRESS_NAME)
               .append(", LD.").append(LoadData.LOADID_NAME)
               .append(" FROM ").append(getReadTableName())
               .append(" LC, LOAD LD WHERE LC.").append(LocationData.LOCATIONSTATUS_NAME)
               .append("=").append(DBConstants.LCUNAVAIL)
               .append(" AND LC.").append(LocationData.WAREHOUSE_NAME)
               .append("=? AND LC.").append(LocationData.WAREHOUSE_NAME)
               .append("=LD.").append(LoadData.WAREHOUSE_NAME)
               .append(" AND LC.").append(LocationData.ADDRESS_NAME)
               .append("=LD.").append(LoadData.ADDRESS_NAME)
               .append(" ORDER BY LC.").append(LocationData.ADDRESS_NAME);

    return fetchRecords(vpSql.toString(), isWarehouse);
  }

  /**
   * Get the min and max locations for a given warehouse
   *
   * @param isWarehouse
   * @return String[2] with min (0) and max (1) or null if none
   */
  @SuppressWarnings("rawtypes")
  public String[] getAddressMinMaxByWarehouse(String isWarehouse)
      throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT MIN(saddress) AS \"MIN\", MAX(saddress) AS \"MAX\"")
             .append(" FROM LOCATION WHERE SWAREHOUSE=? AND ILOCATIONTYPE<>?");
    List<Map> dataList = fetchRecords(vpSql.toString(), isWarehouse,
        DBConstants.LCSTATION);
    if (dataList.size() > 0)
    {
      String vsMin = DBHelper.getStringField(dataList.get(0), "MIN");
      String vsMax = DBHelper.getStringField(dataList.get(0), "MAX");
      return new String[] { vsMin, vsMax } ;
    }
    return null;
  }

  /**
   * Standard method to describe a location (user-friendly version)
   *
   * @param ipLocationData
   * @return
   */
  public String describeLocation(LocationData ipLocationData)
  {
    return describeLocation(ipLocationData.getWarehouse(),
        ipLocationData.getAddress(), ipLocationData.getLocationType());
  }

  /**
   * Standard method to describe a location (user-friendly version)
   *
   * @param isWarehouse
   * @param isAddress
   * @param inLocationType
   * @return
   */
  public String describeLocation(String isWarehouse, String isAddress,
      int inLocationType)
  {
    switch (inLocationType)
    {
      case DBConstants.LCSTATION:
        return "Station " + isAddress;

      case DBConstants.LCASRS:
//        return isWarehouse + "-" + isAddress.substring(0, 3) + "-"
//            + isAddress.substring(3, 6) + "-" + isAddress.substring(6);

      default:
        return isWarehouse + SKDCConstants.DESCRIPTION_SEPARATOR + isAddress;
    }
  }

  /**
   * Standard method to describe a location (user-friendly version)
   *
   * @param isWarehouse
   * @param isAddress
   * @return
   * @throws DBException
   */
  public String describeLocation(String isWarehouse, String isAddress)
      throws DBException
  {
    return describeLocation(isWarehouse, isAddress,
        getLocationTypeValue(isWarehouse, isAddress));
  }


  /**
   * Is this a cycle count location
   *
   * @param isLocation
   * @return
   * @throws DBException
   */
  public boolean isCycleCountLocation(String isLocation) throws DBException
  {
    String[] locn = parseLocation(isLocation);
    LocationData vpLocKey = Factory.create(LocationData.class);
    vpLocKey.setKey(LocationData.WAREHOUSE_NAME, locn[0]);
    vpLocKey.setKey(LocationData.ADDRESS_NAME, locn[1]);

    LocationData vpLocData = getElement(vpLocKey, DBConstants.NOWRITELOCK);
    if (vpLocData == null)
    {
      // For now, just return true for an invalid location.  This allows users
      // to add cycle count orders without having to know exact dimensions of
      // the rack.
      return true;
    }

    return (!(vpLocData.getLocationType() == DBConstants.LCRECEIVING) ||
             (vpLocData.getLocationType() == DBConstants.LCCONSOLIDATION) ||
             (vpLocData.getLocationType() == DBConstants.LCSHIPPING) ||
             (vpLocData.getLocationType() == DBConstants.LCSTATION) ||
             (vpLocData.getLocationType() == DBConstants.LCSTAGING));
  }

  /**
   * Get a list of devices that control AS/RS locations
   * @return
   */
  public String[] getARSRLocationDevices() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(LocationData.DEVICEID_NAME)
               .append(" FROM LOCATION WHERE ")
               .append(LocationData.LOCATIONTYPE_NAME).append("=")
               .append(DBConstants.LCASRS).append(" ORDER BY 1");
    return SKDCUtility.toStringArray(fetchRecords(vpSql.toString()),
        LocationData.DEVICEID_NAME);
  }

  /**
   * Get a list of heights that are valid for a given device
   * @return
   */
  public Integer[] getARSRLocationHeightsForDevice(String isDeviceID) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(LocationData.HEIGHT_NAME)
               .append(" FROM LOCATION WHERE ")
               .append(LocationData.DEVICEID_NAME).append("='")
               .append(isDeviceID).append("' AND ")
               .append(LocationData.LOCATIONTYPE_NAME).append("=")
               .append(DBConstants.LCASRS).append(" ORDER BY 1");
    String[] vasHeights = SKDCUtility.toStringArray(
        fetchRecords(vpSql.toString()), LocationData.HEIGHT_NAME);
    Integer[] vanHeights = new Integer[vasHeights.length];
    for (int i = 0; i < vasHeights.length; i++)
    {
      vanHeights[i] = Integer.parseInt(vasHeights[i]);
    }
    return vanHeights;
  }

  /*========================================================================*/
  /*  Methods to support LocationUtilizationFrame                           */
  /*========================================================================*/
  /**
   * Get a list of maps containing location utilization.  Each map contains the
   * following keys:
   * <UL>
   * <LI>LocationData.LOCATIONSTATUS_NAME - Status
   * <LI>LocationData.EMPTYFLAG_NAME - Empty Flag
   * <LI>LoadData.AMOUNTFULL_NAME - Load Fullness
   * <LI>"ICOUNT" - number of matching locations
   * </UL>
   * The results are sorted by status, empty flag, fullness
   *
   * @param isDevice - Device to check, empty string means no key
   * @param inHeight - Height to check, -1 means no key
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getLocationUtilization(String isDevice, int inHeight)
    throws DBException
  {
    /*
     * Below String should equate to this:
     *
     *   SELECT * FROM (SELECT ilocationstatus,
     *                      CASE
     *                         WHEN ilocationstatus = 29
     *                            THEN iemptyflag
     *                         ELSE 0
     *                      END AS "IEMPTYFLAG",
     *                      iamountfull, COUNT(*) AS "ICOUNT"
     *                 FROM LOCATION lc LEFT OUTER JOIN LOAD ld
     *                      ON (lc.swarehouse = ld.swarehouse AND
     *                          lc.saddress = ld.saddress
     *                         )
     *                WHERE lc.ilocationtype = 10 AND lc.sdeviceid = 'SRC1'
     *             GROUP BY ilocationstatus, iemptyflag, iamountfull) VTABLE
     *   ORDER BY  1, 2, 3
     */
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ")
               .append("(SELECT iLocationStatus, CASE WHEN iLocationStatus = ").append(DBConstants.LCAVAIL)
               .append(" THEN iEmptyFlag ")
               .append("ELSE 0 END AS \"IEMPTYFLAG\", ")
               .append("iAmountFull, COUNT(*) AS \"ICOUNT\" FROM ").append(getReadTableName())
               .append(" lc LEFT OUTER JOIN Load ld ON (lc.sWarehouse = ld.sWarehouse AND ")
               .append("lc.sAddress = ld.sAddress) WHERE ")
               .append("lc.iLocationType = ").append(DBConstants.LCASRS);
      if (isDevice.trim().length() > 0)
      {
        vpSql.append(" AND lc.sDeviceID = '").append(isDevice).append("' ");
      }

      if (inHeight != -1)
      {
        vpSql.append(" AND lc.iHeight = ").append(inHeight);
      }

      vpSql.append(" GROUP BY iLocationStatus, iEmptyFlag, iAmountFull) VTABLE ")
                 .append("ORDER BY 1, 2, 3");

    // Return the results
    return fetchRecords(vpSql.toString());
  }

  /*========================================================================*/
  /*  Methods to support RackUsageFrame                                     */
  /*========================================================================*/
  /**
   * Get the choice list display for the RackUsage screen
   *
   * @return String[] in format "[Device]: Banks [Start]-[End]"
   * @throws DBException
   */
  public String[] getRackUsageDeviceList() throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if (DBInfo.USING_ORACLE_DB)
    {
      vpSql.append("SELECT CONCAT(CONCAT(CONCAT(CONCAT(sDeviceID, ': Banks '), ")
                 .append("SUBSTR(MIN(sAddress), 1, 3)), '-'), SUBSTR(MAX(sAddress), 1, 3)) AS \"sRange\" ")
                 .append("FROM LOCATION WHERE iLocationType = ").append(DBConstants.LCASRS).append(" ")
                 .append("GROUP BY sDeviceID ORDER BY 1");
    }
    else if (DBInfo.USING_SQL_SERVER)
    {
      vpSql.append("SELECT CONCAT(CONCAT(CONCAT(CONCAT(sDeviceID, ': Banks '), ")
                 .append("SUBSTRING(MIN(sAddress), 1, 3)), '-'), SUBSTRING(MAX(sAddress), 1, 3)) AS \"sRange\" ")
                 .append("FROM LOCATION WHERE iLocationType = ").append(DBConstants.LCASRS).append(" ")
                 .append("GROUP BY sDeviceID ORDER BY 1");
    }

    return SKDCUtility.toStringArray(fetchRecords(vpSql.toString()), "sRange");
  }

  /**
   * Get the range of AS/RS addresses for a device
   *
   * <P>Note: This method assumes that all of the locations for a given device
   * are in the same warehouse.</P>
   *
   * @param isDevice
   * @return String[3] { warehouse, minimum address, maximum address }
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public String[] getAddressRangeForDevice(String isDevice) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sWarehouse, MIN(sAddress) AS \"SSTART\",")
               .append(" MAX(sAddress) AS \"SEND\" FROM ").append(getReadTableName())
               .append(" WHERE iLocationType=")
               .append(DBConstants.LCASRS)
               .append(" AND sDeviceID=? GROUP BY sWarehouse");

    List<Map> vpResults = fetchRecords(vpSql.toString(), isDevice);
    if (vpResults.size() != 1)
    {
      throw new DBException("Invalid Device \"" + isDevice + "\"");
    }

    String[] vasResults = new String[3];
    vasResults[0] = vpResults.get(0).get("SWAREHOUSE").toString();
    vasResults[1] = vpResults.get(0).get("SSTART").toString();
    vasResults[2] = vpResults.get(0).get("SEND").toString();

    return vasResults;
  }
}
