package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.dbadapter.data.Warehouse;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.dbadapter.data.Zone;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneData;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroup;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.InvalidDataException;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.UnusedMethod;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardLocationServer extends StandardServer
{
  protected StandardDeviceServer mpDeviceServer = null;

  protected Location mpLocation = Factory.create(Location.class);
  protected Warehouse mpWHS = Factory.create(Warehouse.class);

  public static boolean BARCODE_TO_LOCATION_ENCODE = false;
  protected static boolean BARCODE_TO_LOCATION_ENCODE_ALL_AISLEGROUPS = false;
  protected static String gsPatternForAllAisles = null;
  protected static Map<Integer,String> barCodeIsLocationAisleGroups = new HashMap<Integer,String>();
  protected static String aisleGroupBarCodePatterns = "";

  static
  {
    String vsBarCodeisLocationAisleGroups = Application.getString("BarCodeIsLocation");
    if (vsBarCodeisLocationAisleGroups != null)
    {
      StringTokenizer vpAisleGroupsIterator = new StringTokenizer(vsBarCodeisLocationAisleGroups, ",");
      while (vpAisleGroupsIterator.hasMoreTokens())
      {
        String vsAisleGroup = vpAisleGroupsIterator.nextToken();
        String vsPattern = vpAisleGroupsIterator.nextToken();
        aisleGroupBarCodePatterns = aisleGroupBarCodePatterns + "\nAisleGroup "
            + vsAisleGroup + " Pattern " + vsPattern + " - BarCodeIsLocation";
        Integer vnAisleGroup = null;
        if (vsAisleGroup.equalsIgnoreCase("ALL"))
        {
          gsPatternForAllAisles = vsPattern;
          BARCODE_TO_LOCATION_ENCODE_ALL_AISLEGROUPS = true;
        }
        else
        {
          vnAisleGroup = Integer.valueOf(vsAisleGroup);
        }
        barCodeIsLocationAisleGroups.put(vnAisleGroup, vsPattern);
        BARCODE_TO_LOCATION_ENCODE = true;
      }
    }
  }

  public StandardLocationServer()
  {
    this(null);
  }

  public StandardLocationServer(String keyName)
  {
    super(keyName);
    if (aisleGroupBarCodePatterns.length() > 0)
    {
      logDebug(aisleGroupBarCodePatterns);
    }
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardLocationServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo); 
	  if (aisleGroupBarCodePatterns.length() > 0)
	    {
	      logDebug(aisleGroupBarCodePatterns);
	    }
  }

  /**
   *  Method helps Garbage Collector by unreserving any object references in
   *  this object.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpLocation = null;
    if (mpDeviceServer != null)
    {
      mpDeviceServer.cleanUp();
      mpDeviceServer = null;
    }
  }

  /**
   *  Gets a list of Location data based on keys passed in lcdata.
   *  @param lcdata <code>LocationData</code> object containing search
   *         information.
   *  @return <code>List</code> of Location data.
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getLocationData(LocationData lcdata) throws DBException
  {
    return mpLocation.getAllElements(lcdata);
  }

  /**
   * Get a location
   * @param location
   * @return
   * @throws DBException
   */
  public LocationData getLocationRecord(String location)
         throws DBException
  {
    String[] locn = Location.parseLocation(location);
    return getLocationRecord(locn[0], locn[1]);
  }

  /**
   *  Method retrieves Location record based on warehouse address string.
   *  No locks are done.
   *
   * @param warehouse <code>String</code> containing Warehouse of location.
   * @param address <code>String</code>  containing Address of location.
   *  @return <code>LocationData</code> containing location record.
   * @throws DBException ?
   */
  public LocationData getLocationRecord(String warehouse, String address)
      throws DBException
  {
    LocationData lcdata = Factory.create(LocationData.class);
    lcdata.setKey(LocationData.WAREHOUSE_NAME, warehouse);
    lcdata.setKey(LocationData.ADDRESS_NAME, address);

    return getLocationRecord(lcdata, DBConstants.NOWRITELOCK);
  }

  /**
   * Reads a Location record with read-lock.
   *
   * @param lcdata ?
   * @param lockFlag ?
   * @return ?
   * @throws DBException ?
   */
  public LocationData getLocationRecord(LocationData lcdata, int lockFlag)
         throws DBException
  {
    return mpLocation.getElement(lcdata, lockFlag);
  }

  /**
   * Convenience method.  Get one location record with no locking.
   * @param lcdata ?
   * @return ?
   * @throws DBException ?
   */
  public LocationData getLocationRecord(LocationData lcdata)
         throws DBException
  {
    return getLocationRecord(lcdata, DBConstants.NOWRITELOCK);
  }

  /**
   * Gets an array of all valid location heights
   * @return
   */
  public Integer[] getLocationHeights() throws DBException
  {
    return mpLocation.getLocationHeights();
  }

  /**
   * Method to compute the rack-location address range given the beginning and
   * ending address range.
   *
   * @param warhse ?
   * @param begAddress <code>String</code> containing beginning numeric
   *            location address.
   * @param endAddress <code>String</code> containing ending numeric location
   *            address.
   * @return Returns <code>String[]</code> containing address range.
   *         <code>null</code> if there is some type of error generating the
   *         range.
   */
  public String[] getRackAddressRange(String warhse, String begAddress,
                                            String endAddress)
  {
    String[] addrRange = null;
    try
    {
      addrRange = mpLocation.getAddressChoices(warhse, begAddress, endAddress, "");
    }
    catch(DBException e)
    {
      logException(e, "Error getting Rack Location range");
    }

    return(addrRange);
  }

  /**
   * Method gets the Station attached to this Warehouse.
   *
   * @param sWarehouse the warehouse station is attached to.
   * @param sAddress the address station is attached to.
   *
   * @return <code>String</code> containing station name. An empty string is
   *         returned when nothing is found.
   */
  public String getLocationsStation(String sWarehouse, String sAddress)
         throws DBException
  {
    LocationData lcdata = Factory.create(LocationData.class);
    lcdata.setKey(LocationData.WAREHOUSE_NAME, sWarehouse);
    lcdata.setKey(LocationData.ADDRESS_NAME, sAddress);
    lcdata.setKey(LocationData.LOCATIONTYPE_NAME, Integer.valueOf(DBConstants.LCSTATION));
    LocationData locData = getLocationRecord(lcdata);

    String sStationName = "";
    if (locData != null)
    {
      sStationName = locData.getAddress();
    }
    else
    {
      lcdata.clearKeysColumns();
      lcdata.setKey(LocationData.WAREHOUSE_NAME, sWarehouse);
      lcdata.setKey(LocationData.LOCATIONTYPE_NAME, Integer.valueOf(DBConstants.LCSTATION));
      locData = getLocationRecord(lcdata);
      if (locData != null)
      {
        sStationName = locData.getAddress();
      }
    }

    return(sStationName);
  }

  /**
   * Method to Add Rack locations to the system.
   *
   * @param lcData ?
   * @return ?
   * @throws ArrayIndexOutOfBoundsException ?
   * @throws IOException ?
   * @throws NumberFormatException ?
   * @throws DBException ?
   */
  public String addRackLocations(LocationData lcData)
      throws ArrayIndexOutOfBoundsException, IOException,
      NumberFormatException, DBException
  {
    // prevent locations from being added to a super warehouse
    // Note: Several other methods work on the assumption that a super warehouse
    //      will not have locations - so we need to enforce that here
    if(doesSuperWarehouseExist(lcData.getWarehouse()))
    {
      throw new DBException("Cannot add locations to a super warehouse.");
    }

                                       // Get all sequential addresses in the
                                       // range of banks, bays, and tiers.
    String[] vasAddresses = mpLocation.getRackAddressRange(lcData.getAddress(),
        lcData.getEndingAddress());

    int add_count = 0;
    int total_count = vasAddresses.length;
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      for(int lcidx = 0; lcidx < total_count; lcidx++)
      {
        /*
         * All of the column data is the same with the exception of the address,
         * so over-write the address value(s), and add the row to the database.
         */
        if (lcidx != 0)
        {
          lcData.modifyColumnObject(LocationData.ADDRESS_NAME,
              vasAddresses[lcidx]);
        }

        try
        {
          mpLocation.addElement(lcData);
          add_count++;
        }
        catch(DBException e)
        {
          System.out.println(e);
          logException(e, "Error adding Location");
        }
      }

      if (add_count > 0)
      {
        commitTransaction(tt);
      }
    }
    finally
    {
      endTransaction(tt);
    }

    String s = "Added " + add_count + " of " + total_count + " Locations.";

    vasAddresses = null;

    return(s);
  }

  /**
   * Method to Add non-Rack locations to the system.
   *
   * @param lcData ?
   * @return ?
   * @throws ArrayIndexOutOfBoundsException ?
   * @throws IOException ?
   * @throws NumberFormatException ?
   * @throws DBException ?
   */
  public String addLocations(LocationData lcData)
      throws ArrayIndexOutOfBoundsException, IOException,
      NumberFormatException, DBException
  {
    // prevent locations from being added to a super warehouse
    // Note: Several other methods work on the assumption that a super warehouse
    //      will not have locations - so we need to enforce that here
    if(doesSuperWarehouseExist(lcData.getWarehouse()))
    {
      throw new DBException("Cannot add locations to a super warehouse.");
    }

    String[] addr_list;
                                       // Get all sequential addresses in the
                                       // range of banks, bays, and tiers.
    addr_list = mpLocation.getAddressRange(lcData.getAddress(),
                                        lcData.getEndingAddress());
    int add_count = 0;
    int total_count = addr_list.length;
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      for(int lcidx = 0; lcidx < total_count; lcidx++)
      {                                  // All of the column data is the same
                                         // with the exception of the address.
        if (lcidx != 0)                  // So over-write the address value, and
        {                                // add the row to the database.
          lcData.modifyColumnObject(LocationData.ADDRESS_NAME,
                                    addr_list[lcidx].toString());
        }

        try
        {
          mpLocation.addElement(lcData);
          add_count++;
        }
        catch(DBException e)
        {
          System.out.println(e);
          logException(e, "Error adding Location");
        }
      }

      if (add_count > 0)
      {
        commitTransaction(tt);
      }
    }
    finally
    {
      endTransaction(tt);
    }

    String s = "Added " + add_count + " of " + total_count + " Locations.";

    addr_list = null;

    return(s);
  }

  /**
   * Modify Location method based on the <code>KeyObject</code>s and fields
   * set in the <code>LocationData</code> object.  If no keys are set,  or
   * no fields are set, an exception will be thrown.
   *
   * @param lcData ?
   * @return ?
   * @throws DBException ?
   */
  public String modifyLocation(LocationData lcData) throws DBException
  {
    LocationData oldLCData = getLocationRecord(lcData.getWarehouse(), lcData.getAddress());
    return modifyLocation(oldLCData, lcData);
  }

  /**
   * Modify Location method based on the <code>KeyObject</code>s and fields
   * set in the <code>LocationData</code> object.  If no keys are set,  or
   * no fields are set, an exception will be thrown.
   *
   * @param oldLCData Old <code>LocationData</code> object
   * @param lcData    New <Code>LocationData</code> object
   * @return ?
   * @throws DBException ?
   */
  public String modifyLocation(LocationData oldLCData, LocationData lcData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      logModifyTransaction(oldLCData, lcData);
      mpLocation.modifyElement(lcData);
      commitTransaction(tt);
    }
    catch(DBException exc)
    {
      logException(exc, "modifyLocation");
      throw exc;
    }
    finally
    {
      endTransaction(tt);
    }

    String s = "Location " + lcData.getWarehouse() + "-" + lcData.getAddress()
        + " modified successfully.";

    return(s);
  }

  /**
   * Deletes a location record after verifying it is okay to delete.
   *
   * @param sWarehouse
   * @param sAddress
   * @param sPosition
   *
   * @exception DBException
   */
  public void deleteLocation(String sWarehouse, String sAddress,
      String sPosition) throws DBException
  {
    StandardDedicationServer vpDedServer = Factory.create(StandardDedicationServer.class);
    Load load   = Factory.create(Load.class);
    String vsLocation = "Location " + sWarehouse + "-" + sAddress;

                                       // Get the current location's load
                                       // count.
    if (load.getLoadCountAtCurrentLoc(sWarehouse, sAddress, sPosition) > 0)
    {
      throw new DBException(vsLocation + " has loads present\nand won't be deleted!");
    }
    else if (load.getLoadCountAtNextLoc(sWarehouse, sAddress, sPosition) > 0)
    {
      throw new DBException(vsLocation + " has loads enroute\nand won't be deleted!");
    }
    else if (load.getLoadCountAtFinalLoc(sWarehouse, sAddress) > 0)
    {
      throw new DBException(vsLocation + " has loads enroute\nand won't be deleted!");
    }

    /*
     * Make sure the location has no dedications
     */
    if (vpDedServer.isLocationDedicated(sWarehouse, sAddress))
    {
      throw new DBException(vsLocation + " has dedications\nand won't be deleted!");
    }

    TransactionToken tt = startTransaction();
    LocationData lcdata = Factory.create(LocationData.class);
    lcdata.setKey(LocationData.WAREHOUSE_NAME, sWarehouse);
    lcdata.setKey(LocationData.ADDRESS_NAME, sAddress);
    try
    {
      LocationData locn_rec = this.getLocationRecord(lcdata, DBConstants.WRITELOCK);
      int lcstat = locn_rec.getLocationStatus();
      int empflg = locn_rec.getEmptyFlag();
      int lctype = locn_rec.getLocationType();
      int iAllowDeletion = locn_rec.getAllowDeletion();

                                       // See if location is allowed to be
                                       // deleted.
      if (iAllowDeletion == DBConstants.NO && !SKDCUserData.isSuperUser())
      {
        throw new DBException(vsLocation + "\nis not allowed to be deleted.");
      }

  /*---------------------------------------------------------------------------
    If it's LCUNAVAIL or LCPROHIBIT don't allow deletion.  At this decision
    point, if the location is LCRESERVED but there is really no moves headed here,
    it is because it's a captive system where the load is on it's way out to a
    station; the other case is that it's a manual location, and the location
    is unnecessarily marked LCRESERVED in which case we allow the delete to take
    place.
    ---------------------------------------------------------------------------*/
      if (lcstat == DBConstants.LCUNAVAIL || lcstat == DBConstants.LCPROHIBIT)
      {
        throw new DBException(vsLocation + " has wrong status to be deleted!");
      }
      else if ((lctype == DBConstants.LCDEVICE) && (lctype == DBConstants.LCSTATION))
      {
        String vsLocType;

        try
        {
          vsLocType = DBTrans.getStringValue("iLocationType", lctype);
        }
        catch (NoSuchFieldException vpNSFE)
        {
          vsLocType = "Unknown";
        }

        String excString = vsLocation +
              " is a\n " + vsLocType + " Location and\nwill not be\ndeleted!";
        throw new DBException(excString);
      }
      else if (empflg == DBConstants.LCRESERVED)
      {
        int devtype = Factory.create(Device.class).getDeviceType(locn_rec.getDeviceID());

        StationData stnData = Factory.create(StationData.class);
        stnData.setKey(StationData.DEVICEID_NAME, locn_rec.getDeviceID());
        int stationCaptiveValue = Factory.create(Station.class).getCaptiveTypeValue(stnData);

        if (devtype != DBConstants.CONV_DEVICE &&
            stationCaptiveValue == DBConstants.CAPTIVE)
        {                              // It's not a manual location.
          throw new DBException(vsLocation +
                                " is LCRESERVED and CAPTIVE and therefore " +
                                "can't be deleted!");
        }
      }

      mpLocation.deleteElement(lcdata);       // Submit for deletion.

      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.DELETE);
      tnData.setLocation(sWarehouse, sAddress);
      logTransaction(tnData);

      commitTransaction(tt);

      locn_rec = null;

    }
    catch(DBException exc)
    {
      System.out.println(vsLocation + " not deleted!");
      logException(exc, "deleteLocation");
      throw exc;
    }
    finally
    {
      endTransaction(tt);
    }

    return;
  }

  /**
   *  Checks for the existence of a warehouse.
   *
   *  @param warehouse <code>String</code>
   */
  public boolean exists(String warehouse)
  {
    if (warehouse == null || warehouse.trim().length() == 0)
    {
      return(false);
    }

    return mpLocation.warehouseExists(warehouse);
  }

  /**
   *  Checks for the existence of a location.
   */
  public boolean exists(String warehouse, String address)
  {
    if (warehouse == null || warehouse.trim().length() == 0)
    {
      return(false);
    }
    else if (address == null || address.trim().length() == 0)
    {
      return(false);
    }

    LocationData lcdata = Factory.create(LocationData.class);

    lcdata.setKey(LocationData.WAREHOUSE_NAME, warehouse);
    lcdata.setKey(LocationData.ADDRESS_NAME, address);

    return mpLocation.exists(lcdata);
  }

  /**
   *  Checks for the existence of a Regular location.
   */
  public boolean exists(String[] location)
  {
    try
    {
      String warehouse = location[0];
      String address = location[1];

      return exists(warehouse, address);
    }
    catch (NullPointerException npe)
    {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean existsWithHeight(String warehouse, String address, int height)
  {
    if (warehouse == null || warehouse.trim().length() == 0)
    {
      return(false);
    }
    else if (address == null || address.trim().length() == 0)
    {
      return(false);
    }
    else if (isLocationAStation(warehouse, address))
    {                                  // Don't care about height check at
      return true;                     //  a station location!
    }

    LocationData lcdata = Factory.create(LocationData.class);

    lcdata.setKey(LocationData.WAREHOUSE_NAME, warehouse);
    lcdata.setKey(LocationData.ADDRESS_NAME, address);
    if (mpLocation.exists(lcdata))
    {
      try
      {
        LocationData lcData = getLocationRecord(lcdata);
        if(lcData.getHeight() >= height)
        {
          return true;
        }
        else
        {
          return false;
        }
      }
      catch(DBException exc)
      {
        logException(exc, "Location " + warehouse + "-" + address
            + " not found!");
        return false;
      }
    }
    else
    {
      return false;
    }
  }

  /**
   * Method figures out if two addresses for a location are next to each other
   * (provided that the location are in the same warehouse).
   *
   * @param sWarehouse <code>String</code> containing location warehouse.
   * @param sFirstAddress <code>String</code> containing first location
   *            address.
   * @param sSecondAddress <code>String</code> containing second location
   *            address
   *
   * @return <code>boolean</code> of true if the locations are paired.
   */
  @UnusedMethod
  public boolean isPairedRackLocation(String sWarehouse, String sFirstAddress,
                                      String sSecondAddress) throws DBException
  {
    return mpLocation.isPairedRackLocation(sWarehouse, sFirstAddress,
        sSecondAddress);
  }

  /**
   * Is this load in a conventional location?
   * @return boolean
   * @throws DBException
   */
  public boolean isConventionalLocation(String sWarehouse, String sAddress)
      throws DBException
  {
    TableJoin tj = Factory.create(TableJoin.class);
    return tj.isConventionalLocation(sWarehouse, sAddress);
  }

 /**
  * Checks if a Location is a Station.
  * @param isWarehouse Location warehouse
  * @param isAddress Location address.
  * @return <code>true</code> if location is of station type.
  */
  public boolean isLocationAStation(String isWarehouse, String isAddress)
  {
    int vnLocnType;

    try
    {
      vnLocnType = getLocationTypeValue(isWarehouse, isAddress);
    }
    catch(DBException exc)
    {
      vnLocnType = -1;
      logError("Database error finding Location Type for warehouse \'" +
               isWarehouse + "\' " + "address \'" + isAddress + "\'");
    }

    return(vnLocnType == DBConstants.LCSTATION);
  }

 /**
  * Checks if a Location is of type ASRS.
  * @param isWarehouse Location warehouse
  * @param isAddress Location address.
  * @return <code>true</code> if location is of ASRS type.
  */
  public boolean isASRSLocation(String isWarehouse, String isAddress)
  {
    int vnLocnType;

    try
    {
      vnLocnType = getLocationTypeValue(isWarehouse, isAddress);
    }
    catch(DBException exc)
    {
      vnLocnType = -1;
      logError("Database error finding Location Type for warehouse \'" +
               isWarehouse + "\' " + "address \'" + isAddress + "\'");
    }

    return(vnLocnType == DBConstants.LCASRS);
  }

  /**
   * Gets the Empty Flag for a particular location.
   *
   * @param warehouse
   * @param address
   * @param position - unused in baseline implementation
   * @throws DBException ?
   */
  public int getEmptyFlagValue(String warehouse, String address,
      String position) throws DBException
  {
    return mpLocation.getEmptyFlagValue(warehouse, address);
  }

  /**
   *  Gets the Location Type for a particular location.
   *  Returns -1 if the location does not exist.
   *
   * @throws DBException if database error.
   */
  public int getLocationTypeValue(String warehouse, String address)
         throws DBException
  {
    return mpLocation.getLocationTypeValue(warehouse, address);
  }

  /**
   * Method gets location device.
   *
   * @param warehouse <code>String</code> containing location warehouse.
   * @param address <code>String</code> containing location address.
   *
   * @return <code>String</code> containing Location device.
   */
  public String getLocationDeviceId(String warehouse, String address)
         throws DBException
  {
    return mpLocation.getLocationDevice(warehouse, address);
  }

  /**
   * Method gets location status.
   *
   * @param warehouse <code>String</code> containing location warehouse.
   * @param address <code>String</code> containing location address.
   * @param position - unused in baseline implementation
   *
   * @return <code>int</code> containing Location status, -1 if the location
   *         does not exist.
   */
  public int getLocationStatusValue(String warehouse, String address,
      String position) throws DBException
  {
    return mpLocation.getLocationStatusValue(warehouse, address);
  }

  /**
   * This method creates a location address from a load id. when the location is
   * assumed to be embedded in the load string.  If the <b>BarCodeIsLocation</b>
   * parameter is set in the wrxj.properties file, that pattern is assumed to be
   * in the load.
   *
   * @param isLoadID  the load string which is converted to a location address.
   *
   * @return Location address.  An empty string is returned if there is an error
   *         performing the conversion.
   */
  public static String getAddressFromLoadID(String isLoadID, int inAisleGroup) throws InvalidDataException
  {
    String vsRtnAddress = "";
    String vsBarCodeLoad = isLoadID.trim();

    if (!vsBarCodeLoad.isEmpty() && BARCODE_TO_LOCATION_ENCODE)
    {
/*----------------------------------------------------------------------------
   There should be only one pattern specified if it is applicable to all aisle
   groups (pattern uses the "ALL" string in the specification).
  ----------------------------------------------------------------------------*/
      String vsPattern, vsAisleGroup;
      if (BARCODE_TO_LOCATION_ENCODE_ALL_AISLEGROUPS)
      {
        vsPattern = gsPatternForAllAisles;
        vsAisleGroup = "ALL";
      }
      else
      {
        vsPattern = barCodeIsLocationAisleGroups.get(Integer.valueOf(inAisleGroup));
        vsAisleGroup = Integer.toString(inAisleGroup);
      }
      if (vsPattern == null) {
        //throw new InvalidDataException("Barcode pattern not defined for aisle group " + vsAisleGroup);
        // Allow some aisles to be captive while others are not
        return "";
      }

      Pattern vpPattern = Pattern.compile(vsPattern);
      Matcher vpMatcher = vpPattern.matcher(vsBarCodeLoad);
      if (vsPattern != null && vpMatcher.matches())
      {
        vsRtnAddress = SKDCUtility.preZeroFill(vpMatcher.group(1), DBConstants.LNBANK) +
                       SKDCUtility.preZeroFill(vpMatcher.group(2), DBConstants.LNBAY)  +
                       SKDCUtility.preZeroFill(vpMatcher.group(3), DBConstants.LNTIER);
      }
      else
      {
        throw new InvalidDataException("Barcoded load \'" + isLoadID + "\' does " +
            "not match any enabled pattern for aisle group " + vsAisleGroup);
      }
    }

    return(vsRtnAddress);
  }

  /**
   * Finds and Reserves an Empty Location for storage. This could be either a
   * storage location or a station.
   *
   * TODO: Throw exceptions when errors occur instead of just logging an error
   * and returning null
   *
   * @param ipStationData
   * @param ipLD
   * @return String[2] { Warehouse, Address } or <code>null</code>
   */
  public String[] findLocationForStoring(StationData ipStationData,
      LoadData ipLD) throws InvalidDataException
  {
    int vnHeight = ipLD.getHeight();
    String vsLoadID = ipLD.getLoadID();

    initializeDeviceServer();

    String[] vasEmptyLoc = null;
    TransactionToken tt = null;

    try
    {
      tt = startTransaction();
      Device device = Factory.create(Device.class);
      int vnAisleGroup = device.getDeviceAisleGroup(ipStationData.getDeviceID());
      if (vnAisleGroup == -1)
      {
        logError("LoadId \"" + vsLoadID + "\": NO empty location found - " +
                 "Invalid device.");
        return null;
      }

      vasEmptyLoc = getEmptyLocationUsingLoadId(ipStationData.getWarehouse(),
                                           vnAisleGroup, vnHeight, vsLoadID);
      if (vasEmptyLoc == null)
      {
        /* If Station AllowRoundRobin is false we need to determine which device
         * to use
         */
        if (ipStationData.getAllowRoundRobin() == DBConstants.NO)
        {
          vasEmptyLoc = reserveEmptyLocationForRoute(ipStationData, vnHeight,
              ipLD.getLength(), ipLD.getWidth(), ipLD.getRecommendedZone());
        }
        else
        {
          vasEmptyLoc = reserveEmptyLocationForRoundRobin(ipStationData, ipLD,
                                                          vnAisleGroup);
        }
      }
      if (vasEmptyLoc == null)
      {
        logError("LoadId \"" + vsLoadID + "\": NO empty location found for Device " +
            ipStationData.getDeviceID());
      }
      else
      {                                // Mark the Location as RESERVED.
        mpLocation.setEmptyFlagValue(vasEmptyLoc[0], vasEmptyLoc[1],
            DBConstants.LCRESERVED);
        commitTransaction(tt);
      }
    }
    catch(DBException e)
    {
      logError("DB error finding empty location for load \"" + vsLoadID
          + "\"! " + e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

    return vasEmptyLoc;
  }

  /*========================================================================*/
  /* Location reservation methods                                           */
  /*========================================================================*/

  /**
   * Finds and Reserves an Empty Location for storage when we need a new
   * location due to a bin-full or height-mismatch error.
   *
   * For baseline, this is the same as reserveEmptyLocationForDevice(). Certain
   * projects (like Cox) may have screwy requirements and may thus need to
   * override this method.
   *
   * @param isWarehouse The desired location's warehouse (unused in baseline)
   * @param isDeviceID The current location's device
   * @param inHeight The current location's height
   * @param inLength Load length - ignored in baseline implementation
   * @param inWidth Load width - ignored in baseline implementation
   * @param isRecommendedZone The recommended zone for the load
   * @param izIsForBinfull Flag to indicate if this method is called for an
   *            alternate location search due to a bin full error.
   * @return String[2] { Warehouse, Address } or <code>null</code>
   * @throws DBException if there is one
   */
  public String[] reserveAlternateLocationForDevice(String isWarehouse,
      String isDeviceID, int inHeight, int inLength, int inWidth,
      String isRecommendedZone) throws DBException
  {
    boolean vzIsForBinfull = true; // This declaration is for readability only.
    return reserveEmptyLocationForDevice(isWarehouse, isDeviceID, inHeight,
        inLength, inWidth, isRecommendedZone, vzIsForBinfull);
  }

  /**
   * Finds and Reserves an Empty Location for storage.
   *
   * @param isWarehouse The desired location's warehouse (unused in baseline).
   * @param isDeviceID The current location's device
   * @param inHeight The Current location's height
   * @param inLength Load length - ignored in baseline implementation
   * @param inWidth Load width - ignored in baseline implementation
   * @param izIsForBinfull Flag to indicate if this method is called for an
   *            alternate location search due to a bin full error.
   * @return String[2] { Warehouse, Address } or <code>null</code>
   * @throws DBException if there is one
   */
  @SuppressWarnings("rawtypes")
  protected String[] reserveEmptyLocationForDevice(String isWarehouse,
      String isDeviceID, int inHeight, int inLength, int inWidth,
      String isRecommendedZone, boolean izIsForBinfull) throws DBException
  {
    String[] vasEmptyLoc = null;
    initializeDeviceServer();

    // Check the device.  Ignore offline status for bin full
    if (isDeviceID.length() == 0)
      return null;
    if (!izIsForBinfull)
    {
      if (!isDeviceAvailable(isDeviceID))
      {
        logError("Device " + isDeviceID + " is not Physically or Operationally online!!");
        return null;
      }
    }

    // Find and reserve a location
    TransactionToken vpTranTok = null;
    try
    {
      vpTranTok = startTransaction();
      if(isRecommendedZone == null || isRecommendedZone.length() < 1)
      {
        vasEmptyLoc = mpLocation.findEmptyLocationByZone(isDeviceID, inHeight,
            isRecommendedZone);
      }
      else
      {
        List<Map> vpZoneGroup = null;
        vpZoneGroup = getZoneGroupList(isRecommendedZone);
        if(vpZoneGroup == null || vpZoneGroup.size() == 0)
        {
          vasEmptyLoc = mpLocation.findEmptyLocationByZone(isDeviceID,
              inHeight, isRecommendedZone);
        }
        else
        {
          String vsCurrentZone = "";
          for(Map vpMap : vpZoneGroup)
          {
            vsCurrentZone = vpMap.get(ZoneGroupData.ZONE_NAME).toString();
            vasEmptyLoc = mpLocation.findEmptyLocationByZone(isDeviceID,
                inHeight, vsCurrentZone);
            if(vasEmptyLoc != null)
            {
              break;
            }
          }
        }
      }
      if (vasEmptyLoc == null)
      {
        logError("NO empty location found for DeviceId " + isDeviceID);
      }
      else
      {
        // Mark the Location as RESERVED.
        mpLocation.setEmptyFlagValue(vasEmptyLoc[0], vasEmptyLoc[1],
                                     DBConstants.LCRESERVED);
        commitTransaction(vpTranTok);
      }
    }
    finally
    {
      endTransaction(vpTranTok);
    }

    return(vasEmptyLoc);
  }
  /**
   * Reserve Empty Location (round robin devices)
   *
   * @param ipSD - the station storing the load
   * @param ipLD - the load we are storing
   * @param inAisleGroup
   * @return String[2] { Warehouse, Address } or <code>null</code>
   * @throws DBException if there is one
   */
  @SuppressWarnings("rawtypes")
  protected String[] reserveEmptyLocationForRoundRobin(StationData ipSD,
      LoadData ipLD, int inAisleGroup) throws DBException
  {
    initializeDeviceServer();
    String[] vasEmptyLoc = null;
    String vsInitialDevice = null;
    String vsDeviceID;
    boolean vzLookingForLoc = true;

    List<Map> vpZoneGroup = null;
    int vnCurrentZoneInGroup = 0;
    int vnZonesToCheck = 0;
    String vsCurrentZone = "";

    /*
     * Figure out whether or not we have to worry about zoning
     */
    String vsZoneGroup = ipLD.getRecommendedZone();
    if (vsZoneGroup.trim().length() == 0)
    {
      vsZoneGroup = ipSD.getRecommendedZone();
    }
    if (vsZoneGroup.trim().length() > 0)
    {
      vpZoneGroup = getZoneGroupList(vsZoneGroup);
      vnZonesToCheck = vpZoneGroup.size();
      vsCurrentZone = vpZoneGroup.get(vnCurrentZoneInGroup).get(ZoneGroupData.ZONE_NAME).toString();
      vnCurrentZoneInGroup++;
    }

    /*
     * Actually find & reserve a location
     */
    while (vzLookingForLoc)
    {
      vsDeviceID = mpDeviceServer.getAndUpdateDeviceToken(inAisleGroup);
      if (vsInitialDevice == null)
      {
        vsInitialDevice = vsDeviceID;
      }
      else if (vsDeviceID.equals(vsInitialDevice))
      {
        if (vnCurrentZoneInGroup < vnZonesToCheck && vpZoneGroup != null)
        {
          vsCurrentZone = vpZoneGroup.get(vnCurrentZoneInGroup).get(ZoneGroupData.ZONE_NAME).toString();
          vnCurrentZoneInGroup++;
        }
        else
        {
          vzLookingForLoc = false;
        }
      }

      if (vzLookingForLoc)
      {
        /*
         * In theory, reserveEmptyLocationForZone() should always work, since it
         * accepts blank zones and we don't really care about the warehouse as
         * long as the location is accessible from the device.
         */
        vasEmptyLoc = reserveEmptyLocationForZone(vsDeviceID, ipLD.getHeight(),
            vsCurrentZone);
        if ( vasEmptyLoc != null )
        {
          vzLookingForLoc = false;
        }
      }
    }

    return vasEmptyLoc;
  }

  /**
   * Reserve empty location on a route
   *
   * @param ipStationData
   * @param inHeight
   * @param isRecommendedZone
   * @return String[2] { Warehouse, Address } or <code>null</code>
   * @throws DBException if there is one or if no default route is defined
   */
  protected String[] reserveEmptyLocationForRoute(StationData ipStationData,
      int inHeight, int inLength, int inWidth, String isRecommendedZone)
      throws DBException
  {
    // Get the route
    StandardRouteServer vpRouteServer = Factory.create(StandardRouteServer.class);
    RouteData vpRouteData = vpRouteServer.getNextRouteData(
                                         ipStationData.getDefaultRoute(),
                                         ipStationData.getStationName());

    if (vpRouteData == null)
    {
      String vsLogText = "ReserveEmptyLocation: Round Robin is NO for "
          + ipStationData.getStationName()
          + " a route must be defined in Stations Default Route";
      throw new DBException(vsLogText);
    }

    // Get the next destination
    if (vpRouteData.getDestType() == DBConstants.EQUIPMENT)
    {
      // Equipment route - next is a storage location
      String vsDeviceID = vpRouteData.getDestID();
      return reserveEmptyLocationForDevice(ipStationData.getWarehouse(),
          vsDeviceID, inHeight, inLength, inWidth, isRecommendedZone, false);
    }
    else
    {
      // Station route - next is the station
      StandardStationServer vpStnServ = Factory.create(StandardStationServer.class);
      StationData vpSD = vpStnServ.getStation(vpRouteData.getDestID());
      return new String[] {vpSD.getWarehouse(), vpSD.getStationName()};
    }
  }

  /**
   * Finds and Reserves an Empty Location for storage by zone.
   *
   * @param isDevice
   * @param inHeight
   * @param isZone
   * @return String[2] { Warehouse, Address } or <code>null</code>
   * @throws DBException if there is one
   */
  public String[] reserveEmptyLocationForZone(String isDevice, int inHeight,
      String isZone) throws DBException
  {
    // Make sure the device is available
    if (!isDeviceAvailable(isDevice))
    {
      logError(isDevice + " Not ONLINE; NO empty location found");
      return null;
    }

    // Find & Reserve the location
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      String[] vasEmptyLoc = mpLocation.findEmptyLocationByZone(isDevice,
          inHeight, isZone);
      if (vasEmptyLoc == null)
      {
        logDebug("NO empty location found. DeviceId='" + isDevice
            + "'; Height=" + inHeight + "; Zone='" + isZone + "'");
      }
      else
      {
        // Mark the Location as RESERVED.
        mpLocation.setEmptyFlagValue(vasEmptyLoc[0], vasEmptyLoc[1],
            DBConstants.LCRESERVED);
        commitTransaction(tt);
      }
      return vasEmptyLoc;
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Is the device available for use?
   * <BR>Helper method for location reservation methods
   *
   * @param isDevice
   * @return
   */
  protected boolean isDeviceAvailable(String isDevice)
  {
    initializeDeviceServer();

    return (isDevice.length() != 0 &&
        mpDeviceServer.getPhysicalStatus(isDevice) == DBConstants.ONLINE &&
        mpDeviceServer.getDeviceOperationalStatus(isDevice) == DBConstants.APPONLINE);
  }

  /*========================================================================*/
  /* End location reservation methods                                       */
  /*========================================================================*/

  /**
   * Method to find an empty location in a system that uses the load Id passed
   * in. <b>This method is meant to be used in conjunction with the process of
   * initializing a system with slave pallets.</b> In particular, the load id
   * will be comprised of a location address that must match a property pattern,
   * "BarCodeIsLocation". A sample property is:
   *
   * 01001001: BarCodeIsLocation=ALL,([0-9]{2})([0-9]{3})([0-9]{3})
   * P0100302  BarCodeIsLocation=ALL,.([0-9]{2})([0-9]{3})([0-9]{2})
   * 020301    BarCodeIsLocation=ALL,([0-9]{2}).([0-9]{2}).([0-9]{2})
   *
   * @param isCurrentWhs <code>String</code> containing current warehouse.
   * @param isCurrentAisleGroup station's aisle group
   * @param inHeight height of load
   * @param isLoadIdAsLocation <code>String</code> containing current load id to
   *            store.
   * @param isPattern <code>String</code> containing pattern to parse for
   *            location
   *
   * @return <code>null</code> if no location is found or an error has
   *         occurred. Otherwise <code>String[]</code> containing warehouse
   *         and address of empty location.
   */
  protected String[] getEmptyLocationUsingLoadId(String isCurrentWhs,
      int inCurrentAisleGroup, int inHeight, String isLoadIdAsLocation)
      throws InvalidDataException
  {
    String vsAddress = StandardLocationServer.getAddressFromLoadID(isLoadIdAsLocation,
                                                                   inCurrentAisleGroup);
    if (vsAddress.isEmpty()) return(null);

/*---------------------------------------------------------------------------
   If the location is RESERVED, or OCCUPIED, it means that (a) this load's
   barcode is a duplicate of an existing load, or (b) the location has been
   erroneously left in this state.  If case (a) proves true, return error.
   If case (b) proves true, take the location as valid and allow the load to
   store there.
  ---------------------------------------------------------------------------*/
    try
    {
      int vnOccupiedStatus = mpLocation.getEmptyFlagValue(isCurrentWhs, vsAddress);
      if (vnOccupiedStatus == DBConstants.LCRESERVED ||
          vnOccupiedStatus == DBConstants.OCCUPIED)
      {
        if (mpLocation.locationIsNotEmpty(isCurrentWhs, vsAddress))
        {
          logError("LoadId \"" + isLoadIdAsLocation + "\" AsLocation " +
                   isCurrentWhs + "-" + vsAddress + " is Not empty, or IS reserved");
          return(null);
        }
      }
      else if (vnOccupiedStatus == -1) // This means location does not exist!
      {
        logError("LoadId \"" + isLoadIdAsLocation + "\" AsLocation " +
                   isCurrentWhs + "-" + vsAddress + " Location does NOT exist");
        return(null);
      }
                                       // Set it to RESERVED if we need to.
      if (vnOccupiedStatus != DBConstants.LCRESERVED)
      {
        if (inHeight > 0)
        {
          int vnHeight = (Integer)mpLocation.getSingleColumnValue(isCurrentWhs,
                                                                  vsAddress,
                                                                  LocationData.HEIGHT_NAME);
          if (inHeight > vnHeight)
          {
            logError("Load \"" + isLoadIdAsLocation + "\" has height " + inHeight +
                     " and Location has height " + vnHeight + ".  Location " +
                     "embedded in barcode is incorrect for this height.");
            return(null);
          }
        }
        setLocationEmptyFlag(isCurrentWhs, vsAddress,
            LoadData.DEFAULT_POSITION_VALUE, DBConstants.LCRESERVED);
      }
    }
    catch(DBException dbExc)
    {
      logException(dbExc, "LoadId \"" + isLoadIdAsLocation + "\" AsLocation Location status validation");
      return(null);
    }

    return(new String[] {isCurrentWhs, vsAddress});
  }

  /**
   *  Counts the number of Locations attached to this device.
   */
  public int getLocationDeviceCount(String deviceID)
  {
    int count = 0;
    LocationData lcdata = Factory.create(LocationData.class);
    lcdata.setKey(LocationData.DEVICEID_NAME, deviceID);
    try
    {
      count = mpLocation.getCount(lcdata);
    }
    catch(DBException e)
    {
      logException(e, "Counting Locations. getLocationDeviceCount()");
      count = -1;
    }

    return(count);
  }

  /**
   * Counts the number of Locations attached to this device
   * @param isDeviceID the aisle device.
   * @param inLocType the location type
   * @param inLocStatus the location status (AVAILABLE/UNAVAILABLE/PROHIBITED). 0
   *        means all location status' should be counted.
   * @return 
   */
  public int getLocationDeviceCount(String isDeviceID, int inLocType, int inLocStatus)
  {
    int vnCount = 0;

    LocationData vpLCData = Factory.create(LocationData.class);
    vpLCData.setKey(LocationData.DEVICEID_NAME, isDeviceID);
    vpLCData.setKey(LocationData.LOCATIONTYPE_NAME, Integer.valueOf(inLocType));
    if (inLocStatus != 0)
      vpLCData.setKey(LocationData.LOCATIONSTATUS_NAME, Integer.valueOf(inLocStatus));

    try
    {
      vnCount = mpLocation.getCount(vpLCData);
    }
    catch(DBException e)
    {
      logException(e, "Counting Locations. getLocationDeviceCount()");
      vnCount = -1;
    }

    return(vnCount);
  }

  /**
   *  Gets list of all device IDs in the system.
   *
   * @throws DBException ?
   */
  public String[] getDeviceIDList(boolean insert_all) throws DBException
  {
    String[] dev_list = null;
    Device device = Factory.create(Device.class);

    try
    {
      if (insert_all)
        dev_list = device.getDeviceChoices(SKDCConstants.ALL_STRING);
      else
        dev_list = device.getDeviceChoices("");
    }
    catch(DBException exc)
    {
      logException(exc, "getDeviceIDList");
      throw exc;
    }

    return(dev_list);
  }

 /**
  * Method to set Location Empty flag.
  *
  * @param isWarehouse <code>String</code> containing location warehouse.
  * @param isAddress <code>String</code> containing location address.
  * @param isPosition <code>String</code> containing location position.
  * @param emptyFlag <code>int</code> containing location empty flag.
  *
  * @exception DBException when there is a database update error.
  */
  public void setLocationEmptyFlag(String isWarehouse, String isAddress,
      String isPosition, int emptyFlag) throws DBException
  {
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
      mpLocation.setEmptyFlagValue(isWarehouse, isAddress, emptyFlag);
      commitTransaction(ttok);
    }
    catch(NoSuchElementException e)
    {
      throw new DBException("Updating location: " + isWarehouse + "-" + isAddress, e);
    }
    finally
    {
      endTransaction(ttok);
    }
  }

  /**
   *  Method sets the location status.
   *
   *  @param warehouse <code>String</code> containing Location Warehouse
   *  @param address <code>String</code> containing Location Address
   *  @param statusFlag <code>String</code> containing New Location Status
   *  @param doTransaction <code>boolean</code> indicating if this method should
   *                       start a transction.
   *
   *  @return <code></code>
   */
  public void setLocationStatus(String warehouse, String address,
      String isPosition, int statusFlag, boolean doTransaction)
      throws DBException
  {
    TransactionToken tranTok = null;
    try
    {
      if (doTransaction) tranTok = startTransaction();
      mpLocation.setLocationStatusValue(warehouse, address, statusFlag);
      if (doTransaction) commitTransaction(tranTok);
    }
    catch(DBException e)
    {
      throw e;
    }
    finally
    {
      if (doTransaction) endTransaction(tranTok);
    }
  }

  /**
   * Get the min and max locations for a given warehouse
   *
   * @param isWarehouse
   * @return String[2] with min (0) and max (1) or null if none
   */
  public String[] getAddressMinMaxByWarehouse(String isWarehouse)
      throws DBException
  {
    return mpLocation.getAddressMinMaxByWarehouse(isWarehouse);
  }

  /**
   * Get the next empty flag
   *
   * @param ipSD
   * @param ipLD
   * @return
   */
  public int getEmptyFlagAfterRetrieve(StationData ipSD, LoadData ipLD)
  {
    int vnNextEmptyFlag = (ipSD.getCaptive() == DBConstants.CAPTIVE) ?
        DBConstants.LCRESERVED : DBConstants.UNOCCUPIED;
    return vnNextEmptyFlag;
  }


/*==========================================================================
                    WAREHOUSE RELATED METHODS GO HERE
  ==========================================================================*/
  /**
   * Reads a set of Warehouse records based on the <code>KeyObject</code>s
   * set in the <code>WarehouseData</code> object.  If no keys are set, a
   * list of all warehouses is returned.
   *
   * @return <code>List<Map></code> where each <code>Map</code> contains
   * field name-value pairs.
   *
   * @throws DBException ?
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getWarehouseData(WarehouseData wtdata) throws DBException
  {
    Warehouse whs = Factory.create(Warehouse.class);
    return whs.getAllElements(wtdata);
  }

  /**
   *  Convenience method, since the caller shouldn't have to call setKey()
   *
   * @param isWarehouse The warehouse whose data we want
   * @return The warehouse data for the warehouse
   * @throws DBException
   */
  public WarehouseData getRegularWarehouseElement(String isWarehouse)
      throws DBException
  {
    WarehouseData vpWH = new WarehouseData();
    vpWH.setKey(WarehouseData.WAREHOUSE_NAME, isWarehouse);

    Warehouse whs = Factory.create(Warehouse.class);
    return whs.getElement(vpWH, DBConstants.NOWRITELOCK);
  }

  /**
   * Adds a Warehouse record.
   *
   * @param  wtdata Warehouse Data to add.  It is assumed that the
   *         WarehouseType is filled in in this structure.
   * @return String containing message of success or failure to add.
   * @exception DBException
  */
  public String addWarehouse(WarehouseData wtdata) throws DBException
  {
    Warehouse whs = Factory.create(Warehouse.class);

    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      whs.addElement(wtdata);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      logException("AddWarehouse()", e);
      throw new DBException("Transaction Error! " + e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

    String s = "Warehouse Record for " + wtdata.getWarehouse()
        + " added successfully.";

    return(s);
  }

  /**
   * Modifies a Warehouse record based on the <code>KeyObject</code>s and fields
   * set in the <code>LocationData</code> object.  If no keys are set,  or
   * no fields are set, an exception will be thrown.
   *
   * @param  wtdata Warehouse Data to Modify.
   * @return String containing message of success or failure to add.
   *
   * @exception DBException
   */
  public String modifyWarehouse(WarehouseData wtdata) throws DBException
  {
    TransactionToken tt = null;
    Warehouse whs = Factory.create(Warehouse.class);
    try
    {                                  // Submit for modification.
      tt = startTransaction();
      whs.modifyElement(wtdata);
      commitTransaction(tt);
    }
    catch(DBException exc)
    {
      logException(exc, "modifyWarehouse");
      throw exc;
    }
    finally
    {
      endTransaction(tt);
    }

    String s = "Warehouse " + wtdata.getWarehouse() + " modified successfully.";

    return(s);
  }

  /**
   * Deletes a Warehouse record after verifying it is okay to delete.  Method
   * checks for the non-existence of Locations before deleting.  If the Super
   * warehouse is provided, then the unique key will be super_whs|regular_whs.
   *
   * @param  whsData
   * @throws DBException ?
   */
  public String deleteWarehouse(WarehouseData whsData) throws DBException
  {
    whsData.setKey(WarehouseData.SUPERWAREHOUSE_NAME, whsData.getSuperWarehouse());
    whsData.setKey(WarehouseData.WAREHOUSE_NAME, whsData.getWarehouse());
    WarehouseData wtdata  = null;
    TransactionToken tt = null;
    Warehouse whs = Factory.create(Warehouse.class);

    String regular_whs = whsData.getWarehouse();
    if (regular_whs.length() == 0)
    {
      logDebug("Blank warehouse passed!");
      throw new DBException("Blank warehouse passed!");
    }
                                       // See if there are any Locations
                                       // attached to this warehouse.
    LocationData lcdata = Factory.create(LocationData.class);
    lcdata.setWarehouse(regular_whs);
    lcdata.setKey(LocationData.WAREHOUSE_NAME, regular_whs);
    if (mpLocation.getCount(lcdata) > 0)
    {
      logError("Warehouse " + regular_whs + " has locations.");
      lcdata.clear();
      lcdata = null;
      throw new DBException("Warehouse " + regular_whs +
                            " has locations");
    }
    tt = startTransaction();

    try
    {
      // Fetch Warehouse record to see if
      // it's on HOLD.
      wtdata = whs.getElement(whsData, DBConstants.WRITELOCK);
      wtdata.setKey(WarehouseData.WAREHOUSE_NAME, wtdata.getWarehouse());
      if (wtdata.getWarehouseStatus() == DBConstants.WARHOLD)
      {
        String err = "HOLD status warehouses can't be deleted!";
        logError(err);
        throw new DBException(err);
      }
      else if (wtdata.getWarehouseType() == DBConstants.SUPER)
      {
        /*---------------------------------------------------------------------------
         If it's a Super Warehouse, delete all super warehouse references.  Recall
         that for a super warehouse the Warehouse Type is SUPER, the Super Warehouse
         field is null, and the regular warehouse field is filled in.
         ---------------------------------------------------------------------------*/
        if (whsData.getSuperWarehouse().length() != 0)
        {
          throw new DBException("Warehouse " + whsData.getWarehouse() + " is a Super\nwarehouse also and must" + " be unlinked first!");
        }
      }
      if (wtdata.getWarehouseType() == DBConstants.SUPER)
      {
        deleteAllLinks(wtdata);
      }
      else
      {
        whs.deleteElement(wtdata);
      }
      commitTransaction(tt);
    }
    catch(DBException exc)
    {
      logException(exc, "deleteWarehouse");
      throw exc;
    }
    finally
    {
      endTransaction(tt);
      if (wtdata != null)
      {
        wtdata.clear();
        wtdata = null;
      }
    }

    return("Warehouse " + regular_whs + " deleted successfully.");
  }

  /**
   * Deletes linkage between Super warehouse and a regular warehouse.
   * @param  orig_super_whs String containing Super Warehouse.  This string must
   *         be filled in.
   * @param  regular_whs String containing Regular Warehouse.  This string must
   *         be filled in.
   * @param  new_super_whs String containing new Super Warehouse.  This string must
   *         be filled in.
   *
   * @exception DBException
   */
  public String modifySuperLink(String orig_super_whs, String regular_whs,
                                String new_super_whs) throws DBException
  {
    Warehouse whs = Factory.create(Warehouse.class);
    if (regular_whs.trim().length() == 0 && orig_super_whs.trim().length() == 0)
    {
      throw new DBException("Blank Super, and Regular Warehouse passed!");
    }
                                       // If they are blanking out the Super
                                       // reference, make sure there is no
                                       // conflict with an existing record.
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      if (new_super_whs.trim().length() == 0)
      {
        modifySuperWarehouse(orig_super_whs, regular_whs, "");
        this.deleteSuperWarehouseIfPossible(orig_super_whs);
      }
      else
      {                                  // If a super warehouse doesn't already
                                         // exist, add it before making requested
                                         // link with the regular warehouse.
        if (whs.isNewLinkCircular(new_super_whs, regular_whs))
        {
          throw new DBException("Invalid link given!");
        }
                                         // Add the super warehouse record if it
                                         // doesn't exist.
        if (this.doesSuperWarehouseExist(new_super_whs) == false)
        {
          addSuperWarehouse(new_super_whs, regular_whs);
        }
                                         // Link the selected child warehouse to
                                         // the super warehouse.
        modifySuperWarehouse(orig_super_whs, regular_whs, new_super_whs);
      }

      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }

    return("Link modified successfully.");
  }

  /**
   *  Fetches a list of Super warehouses for a combo-box type display.
   */
  public String[] getSuperWarehouseChoices(boolean insert_all)
         throws DBException
  {
    String[] whs_list = null;
    Warehouse whs = Factory.create(Warehouse.class);

    try
    {
      if (insert_all)
        whs_list = whs.getSuperWarehouseChoices(SKDCConstants.ALL_STRING);
      else
        whs_list = whs.getSuperWarehouseChoices("");
    }
    catch(DBException exc)
    {
      logException(exc, "getSuperWarehouseChoices");
      throw exc;
    }

    return(whs_list);
  }

  /**
   *  Fetches a list of warehouses for a combo-box type display.
   *
   * @throws DBException ?
   */
  public String[] getWarehouseChoices(boolean insert_all)
         throws DBException
  {
    String[] whs_list = null;
    Warehouse whs = Factory.create(Warehouse.class);

    try
    {
      if (insert_all)
        whs_list = whs.getWarehouseChoices(SKDCConstants.ALL_STRING);
      else
        whs_list = whs.getWarehouseChoices("");
    }
    catch(DBException exc)
    {
      logException(exc, "getWarehouseChoices");
      throw exc;
    }

    return(whs_list);
  }

  /**
   * Does this warehouse exist?
   * @param whsData
   * @return
   */
  public boolean exists(WarehouseData whsData)
  {
    Warehouse whs = Factory.create(Warehouse.class);
    return whs.exists(whsData);
  }

  /**
   * Gets a count of warehouse records based on the <code>KeyObject</code>s
   * set in the <code>WarehouseData</code> object.  If no keys are set, a
   * count of all warehouses is returned.
   */
  public int getWarehouseCount(WarehouseData whsData) throws DBException
  {
    Warehouse whs = Factory.create(Warehouse.class);
    return(whs.getCount(whsData));
  }

  /**
   * Get the equipment warehouse code for a given warehouse
   *
   * @param isWarehouse The warehouse whose data we want
   * @return The warehouse data for the warehouse (default is "0")
   */
  public String getEquipWarehouse(String isWarehouse)
  {
    try
    {
      WarehouseData vpWH = getRegularWarehouseElement(isWarehouse);
      if (vpWH != null)
      {
        return vpWH.getEquipWarehouse();
      }
      else
      {
        logError("Warehouse \"" + isWarehouse
            + "\" not found, using default value.");
      }
    }
    catch (DBException exc)
    {
      // TODO: throw the exception instead of catching it and logging an error.
      logException(exc, "Error getting warehouse \"" + isWarehouse + "\"");
    }
    return "0";
  }

  /**
   * Fetches a list of warehouses for a combo-box type display.
   *
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
   * @return Array of warehouses.
   * @throws DBException if there is a DB access error.
   */
  public String[] getRegularWarehouseChoices(final String isFirstElement)
         throws DBException
  {
    return mpWHS.getRegularWarehouseChoices(isFirstElement);
  }

  /*===========================================================================
     All Private methods go in this section.
   ===========================================================================*/
  /**
   *  Adds a super warehouse record.
   *
   * @throws DBException ?
   */
  private void addSuperWarehouse(String superwhs, String regwhs)
          throws DBException
  {
    Warehouse whs = Factory.create(Warehouse.class);
                                   // Add a super warehouse record.
    WarehouseData whs_rec = Factory.create(WarehouseData.class);
    whs_rec.setWarehouse(superwhs);
    whs_rec.setWarehouseType(DBConstants.SUPER);

    try
    {
      whs.addElement(whs_rec);
    }
    catch(DBException e)
    {
      logException(e, "addSuperWarehouse");
      throw e;
    }
    finally
    {
      whs_rec.clear();
      whs_rec = null;
    }
  }

  /**
   * Modify super warehouse
   *
   * @param superwhs
   * @param regwhs
   * @param new_super
   * @throws DBException
   */
  private void modifySuperWarehouse(String superwhs, String regwhs,
                                    String new_super) throws DBException
  {
    Warehouse whs = Factory.create(Warehouse.class);
    WarehouseData whsData = Factory.create(WarehouseData.class);

    try
    {
        whsData.setSuperWarehouse(new_super);
        whsData.setKey(WarehouseData.SUPERWAREHOUSE_NAME, superwhs);
        whsData.setKey(WarehouseData.WAREHOUSE_NAME, regwhs);
      whs.modifyElement(whsData);
    }
    catch(DBException e)
    {
      logException(e, "modifySuperWarehouse");
      throw e;
    }
    finally
    {
      whsData.clear();
      whsData = null;
    }

    return;
  }

  /**
   *  Deletes a super Warehouse if possible.
   *  This method is called when a warehouse is unlinked from it's super warehouse.
   *  If unlinking this warehouse means there are no more warehouses linked to
   *  the super warehouse it can be deleted.
   *
   *  Note: deleting the super warehouse results in blanking out the super
   *  field in the regular warehouse, hence the need to only modify if the
   *  super is not deleted.
   * @throws DBException ?
   */
  private void deleteSuperWarehouseIfPossible(String superwhs)
          throws DBException
  {
    Warehouse whs = Factory.create(Warehouse.class);
    WarehouseData wtdata = null;

    try
    {
      if (this.doesSuperWarehouseExist(superwhs))
      {
        /*
         * If no one is using this warehouse anymore, then we can delete it
         */
        wtdata = Factory.create(WarehouseData.class);
        wtdata.setKey(WarehouseData.SUPERWAREHOUSE_NAME, superwhs);
        if (getWarehouseCount(wtdata) == 0)
        {
          wtdata.clear();
          wtdata.setKey(WarehouseData.WAREHOUSE_NAME, superwhs);
          whs.deleteElement(wtdata);
        }
      }
    }
    catch(DBException e)
    {
      logException(e, "deleteSuperWarehouseIfPossible");
      throw e;
    }

    return;
  }

  /**
   *  Deletes all Super Warehouse associations. A DBException is thrown when
   *  one of the sub-warehouses is also a super warehouse; in this case the
   *  user needs to unlink this sub-warehouse from any of its sub-warehouses
   *  first.  The reason for not making the unlink function recursive is
   *  it may provide the user with more destructive power than they want!
   *
   *  @param whs_rec <code>WarehouseData</code> containing Super warehouse
   *  and sub-warehouse info.
   *
   * @throws DBException ?
   */
  private void deleteAllLinks(WarehouseData whs_rec) throws DBException
  {
/*---------------------------------------------------------------------------
   First check for potential conflicts that may arise because of this request.
   The conflict will arise if any one of the child warehouses happens to be
   a super warehouse itself, since in this case there will be a stand-alone
   record that has iWarehouseType=SUPER, sSuperWarehouse = "", and
   sWarehouse = "something".
   --------------------------------------------------------------------------*/
    Warehouse whs = Factory.create(Warehouse.class);
    String sWarehouse = whs_rec.getWarehouse();
                                       // sWarehouse contains the super warehouse
                                       // we are trying to unlink from all its
                                       // sub-warehouses here.
    if (whs.isSubWarehouseParent(sWarehouse))
    {
      throw new DBException("Warehouse " + sWarehouse +
      " contains one or more\nSub-Warehouse(s) that are Super-Warehouses.\nUnlink them first!");
    }
    whs_rec.clear();
    whs_rec.setSuperWarehouse("");
    whs_rec.setKey(WarehouseData.SUPERWAREHOUSE_NAME, sWarehouse);

    try
    {                                  // Unlink everything.
      try
      {
        whs.modifyElement(whs_rec);
      }
      catch (NoSuchElementException nsee)
      {
        // This isn't really a problem, but we'll log it just in case
        logException(nsee, "deleteAllLinks");
      }

                                       // Delete any remaining references.
      whs_rec.clear();
      whs_rec.setKey(WarehouseData.WAREHOUSE_NAME, sWarehouse);
      whs.deleteElement(whs_rec);
    }
    catch(DBException exc)
    {
      logException(exc, "deleteAllLinks");
      throw exc;
    }
    finally
    {
      whs_rec.clear();
      whs_rec = null;
    }
    return;
  }

  /**
   * Does a super warehouse exist
   *
   * @param superWarehouse
   * @return
   */
  protected boolean doesSuperWarehouseExist(String superWarehouse)
  {
    Warehouse whs = Factory.create(Warehouse.class);
    WarehouseData whsData = Factory.create(WarehouseData.class);
    whsData.setWarehouse(superWarehouse);
    whsData.setWarehouseType(DBConstants.SUPER);
    whsData.setKey(WarehouseData.WAREHOUSE_NAME, superWarehouse);
    whsData.setKey(WarehouseData.WAREHOUSETYPE_NAME, DBConstants.SUPER);
    return(whs.exists(whsData));
  }

 /**
  * Tests for warehouse existence.
  * @param isWarehouse the warehouse
  * @return
  */
  public boolean doesWarehouseExist(String isWarehouse)
  {
    Warehouse vpWhs = Factory.create(Warehouse.class);
    WarehouseData vpWhsData = Factory.create(WarehouseData.class);
    vpWhsData.setKey(WarehouseData.WAREHOUSE_NAME, isWarehouse);

    return(vpWhs.exists(vpWhsData));
  }

  /**
   * Update location data for a range of locations
   *
   * @param locationData
   * @param izBankBayTier
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
      boolean izBankBayTier, int zoneModifier, int deviceModifier,
      int aisleModifier, int statusModifier, int occupiedModifier,
      int heightModifier, int deleteModifier, int typeModifier,
      int inSwapModifier) throws DBException
  {
    TransactionToken tt = null;
    int rowCount;
    try
    {
      tt = startTransaction();
      if (izBankBayTier)
      {
        // do all locations in the range, bounded by bank-bay-tier
        rowCount = mpLocation.updateLocationsDataBankBayTier(locationData,
            zoneModifier, deviceModifier, aisleModifier, statusModifier,
            occupiedModifier, heightModifier, deleteModifier, typeModifier,
            inSwapModifier);
      }
      else
      {
          // do all locations in the range
        rowCount = mpLocation.updateLocationsData(locationData, zoneModifier,
            deviceModifier, aisleModifier, statusModifier, occupiedModifier,
            heightModifier, deleteModifier, typeModifier, inSwapModifier);
      }
      commitTransaction(tt);
    }
    catch (DBException exc)
    {
      logException(exc, "Error Updating Locations");
      throw exc;
    }
    finally
    {
      endTransaction(tt);
    }
    return rowCount;
  }

  /**
   * Changes the iSearchOrder for a range of locations
   * @param iasLocations is an array of strings containing the location names in
   *        the desired order
   * @param ipLocationData has the warehouse and starting search order.
   * @return String of how many locations were changed.
   *
   * @exception DBException
   */
   public String changeSearchOrder(String[] iasLocations,
      LocationData ipLocationData) throws DBException
  {
    String infoMessage = "";
    boolean vzIncrement = true;
    if (iasLocations[0] == "")
    {
      vzIncrement = false;
      try
      {
        iasLocations = mpLocation.getRackAddressRange(
            ipLocationData.getAddress(), ipLocationData.getEndingAddress());
      }
      catch (ArrayIndexOutOfBoundsException aiobe)
      {
        iasLocations = new String[0];
        infoMessage = "Invalid ending Bank. No Locations Modified ";
        return (infoMessage);
      }
      catch (IOException ioe)
      {
        iasLocations = new String[0];
        infoMessage = "Bank, Bay, or Tier must be non-zero! No Locations Modified ";
        return (infoMessage);
      }
      catch (NumberFormatException nfe)
      {
        iasLocations = new String[0];
        infoMessage = "Number Format Error. No Locations Modified ";
        return (infoMessage);
      }
    }

    int changeCount = 0;
    int totalCount = iasLocations.length;
    int searchOrder = ipLocationData.getSearchOrder();
    String address = "";
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      for (int lcidx = 0; lcidx < totalCount; lcidx++)
      { // All of the column data is the same
        // with the exception of the address.
        try
        {
          address = iasLocations[lcidx].toString();
          mpLocation.changeSearchOrder(ipLocationData.getWarehouse(), address,
              searchOrder);
          changeCount++;
          if (vzIncrement)
            searchOrder++;
        }
        catch (NoSuchElementException e)
        {
          // We get this if no location matches the warehouse and address.
          // Occurs regularly if specifying a range of locations.
          // do not increment the add count but do increment the searchOrder so
          // we go to next location
          if (vzIncrement)
            searchOrder++;
        }
        catch (DBException e)
        {
          System.out.println(e);
          logException(e, "Error updating Search order for Location " + address);
        }
      }

      if (changeCount > 0)
      {
        commitTransaction(tt);
      }
    }
    finally
    {
      endTransaction(tt);
    }

    infoMessage = infoMessage + "Modified Search order for " + changeCount
        + " of " + totalCount + " Locations.";

    return (infoMessage);
  }

  /**
   * Gets the count of matching locations.
   *
   * @param isWarehouse <code>String</code> containing location warehouse (null
   *          if all warehouses).
   * @param isDevice <code>String</code> containing location device (null if all
   *          devices).
   * @param inAisleGroup <code>int</code> containing aisle group (0 if all
   *          groups).
   * @param inType <code>int</code> containing location type (0 if all types).
   * @param inStatus <code>int</code> containing location status (0 if all
   *          statuses).
   * @param inEmpfyFlag <code>int</code> containing empty flag (0 if all
   *          states).
   * @param inHeight <code>int</code> containing location height (-1 if all
   *          heights).
   */
  public int getLocationCount(String isWarehouse, String isDevice,
      int inAisleGroup, int inType, int inStatus, int inEmpfyFlag, int inHeight)
      throws DBException
  {
    LocationData lcdata = Factory.create(LocationData.class);

    if (isWarehouse != null && isWarehouse.trim().length() > 0)
    {
      lcdata.setKey(LocationData.WAREHOUSE_NAME, isWarehouse);
    }

    if (isDevice != null && isDevice.trim().length() > 0)
    {
      lcdata.setKey(LocationData.DEVICEID_NAME, isDevice);
    }

    if (inAisleGroup > 0)
    {
      lcdata.setKey(LocationData.AISLEGROUP_NAME, inAisleGroup);
    }

    if (inType > 0)
    {
      lcdata.setKey(LocationData.LOCATIONTYPE_NAME, inType);
    }

    if (inStatus > 0)
    {
      lcdata.setKey(LocationData.LOCATIONSTATUS_NAME, inStatus);
    }

    if (inEmpfyFlag > 0)
    {
      lcdata.setKey(LocationData.EMPTYFLAG_NAME, inEmpfyFlag);
    }

    if (inHeight != -1)
    {
      lcdata.setKey(LocationData.HEIGHT_NAME, inHeight);
    }

    return mpLocation.getCount(lcdata);
  }

  /*========================================================================*/
  /*  Zone-related methods go here                                          */
  /*========================================================================*/
  /**
   * Add Zone
   *
   * @param ipZoneData - ZoneData - the zone to add
   * @throws DBException
   */
  public void addZone(ZoneData ipZoneData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Zone.class).addZone(ipZoneData);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      throw new DBException("Error adding zone: " + e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Modify Zone
   *
   * NOTE: does not require key objects to be set - that will
   * be done automatically.
   *
   * @param ipZoneData - ZoneData - the zone to modify
   * @throws DBException
   */
  public void modifyZone(ZoneData ipZoneData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Zone.class).modifyZone(ipZoneData);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      throw new DBException("Error modifying zone: " + e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Determine whether or not a zone is safe to delete (ie. not used)
   *
   * @param isZone
   * @return
   * @throws DBException
   */
  public boolean isSafeToDeleteZone(String isZone) throws DBException
  {
    return Factory.create(Zone.class).isSafeToDeleteZone(isZone);
  }

  /**
   * Delete Zone
   *
   * @param isZone - String - the Zone to delete
   * @throws DBException
   */
  public void deleteZone(String isZone) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      mpLocation.deleteZone(isZone);
      Factory.create(ZoneGroup.class).deleteZone(isZone);
      Factory.create(Zone.class).deleteZone(isZone);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      throw new DBException("Error deleting zone: " + e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Get a list of all zones
   * @return - List
   * @throws DBException
   */
  public ZoneData getZone(String isZone) throws DBException
  {
    return Factory.create(Zone.class).getZone(isZone);
  }

  /**
   * Get a list of zones
   * @param isZone - Search string
   * @return - List
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getZones(String isZone) throws DBException
  {
    return Factory.create(Zone.class).getZones(isZone);
  }

  /**
   * Get a list of all zones
   *
   * @param isPrepender - prepender for the list (null if none)
   * @return - List
   * @throws DBException
   */
  public List<String> getZoneChoiceList(String isPrepender) throws DBException
  {
    return Factory.create(Zone.class).getZoneChoiceList(isPrepender);
  }

  /**
   * Add Zone Group
   *
   * @param ipZoneGroupData - ZoneGroupData - the zone to add
   * @throws DBException
   */
  public void addZoneGroupMember(ZoneGroupData ipZoneGroupData)
    throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(ZoneGroup.class).addZoneGroupMember(ipZoneGroupData);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      throw new DBException("Error adding " +
          ZoneGroup.describeZoneGroupMember(ipZoneGroupData) + ": " +
          e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Modify Zone Group
   *
   * @param ipZoneGroupData - ZoneGroupData - the zone to modify
   * @throws DBException
   */
  public void modifyZoneGroupMember(ZoneGroupData ipZoneGroupData)
    throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(ZoneGroup.class).modifyZoneGroupMember(ipZoneGroupData);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      throw new DBException("Error modifying " +
          ZoneGroup.describeZoneGroupMember(ipZoneGroupData) + ": " +
          e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Delete Zone Group
   *
   * @param isZoneGroup - String - the ZoneGroup to delete
   * @param inPriority
   * @throws DBException
   */
  public void deleteZoneGroupMember(String isZoneGroup, int inPriority)
    throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(ZoneGroup.class).deleteZoneGroupMember(isZoneGroup, inPriority);
      commitTransaction(tt);
    }
    catch(DBException e)
    {
      throw new DBException("Error deleting " +
          ZoneGroup.describeZoneGroupMember(isZoneGroup, inPriority) + ": " +
          e.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   * Get a zone group record
   *
   * @param isZoneGroup
   * @param inPriority
   * @return
   * @throws DBException
   */
  public ZoneGroupData getZoneGroupMember(String isZoneGroup, int inPriority)
    throws DBException
  {
    return Factory.create(ZoneGroup.class).getZoneGroupMember(isZoneGroup, inPriority);
  }

  /**
   * Get a list of all possible recommended zones, including a blank
   *
   * @return - List
   * @throws DBException
   */
  public List<String> getRecommendedZones() throws DBException
  {
    return Factory.create(ZoneGroup.class).getRecommendedZones();
  }

  /**
   * Get a list of zone group records
   *
   * @param isZoneGroup
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getZoneGroupList(String isZoneGroup) throws DBException
  {
    return Factory.create(ZoneGroup.class).getZoneGroupList(isZoneGroup);
  }

  /**
   * Get a list of zones not assigned to a particular group
   *
   * @param isZoneGroup
   * @return
   * @throws DBException
   */
  public String[] getZonesNotInGroup(String isZoneGroup) throws DBException
  {
    return Factory.create(ZoneGroup.class).getZonesNotInGroup(isZoneGroup);
  }

  /**
   * Get the next zone group priority for a zone group
   *
   * @param isZoneGroup
   * @return
   * @throws DBException
   */
  public int getNextZoneGroupPriority(String isZoneGroup) throws DBException
  {
    return Factory.create(ZoneGroup.class).getNextZoneGroupPriority(isZoneGroup);
  }

  /**
   * Are there zones defined?
   * @return
   */
  public boolean hasLocationZonesDefined()
  {
    boolean vzZoneEnabled = false;

    try
    {
      vzZoneEnabled = getZones("").size() > 0;
    }
    catch (DBException dbe)
    {
      logException("hasLocationZonesDefined", dbe);
    }

    return vzZoneEnabled;
  }

  /**
   * Are there zones defined?
   * @return
   */
  public boolean hasRecommendedZonesDefined()
  {
    boolean vzZoneEnabled = false;

    try
    {
      vzZoneEnabled = getZoneGroupList("").size() > 0;
    }
    catch (DBException dbe)
    {
      logException("hasRecommendedZonesDefined", dbe);
    }

    return vzZoneEnabled;
  }

  /*========================================================================*/
  /*  The following initialize other servers if/when we need them.  This    */
  /*  is cheaper than both constantly creating/destroying them and          */
  /*  needlessly creating them when we create the server.                   */
  /*========================================================================*/
  protected void initializeDeviceServer()
  {
    if (mpDeviceServer == null)
    {
      mpDeviceServer = Factory.create(StandardDeviceServer.class,
          getClass().getSimpleName());
    }
  }

  /**
   *  Log a Modify Transaction History record
   *
   *  @param ipOldData <code>String</code> containing data of old Location record.
   *  @param ipNewData <code>String</code> containing data of new Location record.
   *
   *  @throws <code>DBException</code> if a database add error.
   */
  private void logModifyTransaction(LocationData ipOldData, LocationData ipNewData) throws DBException
  {
    // We only log the transaction if there is any change
    if (ipNewData.getColumnCount() > 0)
    {
      tnData.clear();
      tnData.setTranCategory(DBConstants.SYSTEM_TRAN);
      tnData.setTranType(DBConstants.MODIFY);
      tnData.setLocation(ipNewData.getWarehouse(), ipNewData.getAddress());
      if (logDataChanged(ipOldData, ipNewData) == true)
      {
        logTransaction(tnData);
      }
    }
  }
}

