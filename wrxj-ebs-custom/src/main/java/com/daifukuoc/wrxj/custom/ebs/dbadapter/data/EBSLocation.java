package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSRouteServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;

public class EBSLocation extends Location {
    /* ======================================================================== */
    /* Pre-built SQL */
    /* ======================================================================== */
    protected static String SQL_EMPTY_LOCATION_BASE = "SELECT " + LocationData.WAREHOUSE_NAME + ", "
            + LocationData.ADDRESS_NAME + " FROM Location WHERE " + LocationData.DEVICEID_NAME + " = ? AND "
            + LocationData.EMPTYFLAG_NAME + " = " + DBConstants.UNOCCUPIED + " AND " + LocationData.LOCATIONSTATUS_NAME
            + " = " + DBConstants.LCAVAIL + " AND " + "(" + LocationData.LOCATIONTYPE_NAME + " = " + DBConstants.LCASRS
            + " OR " + LocationData.LOCATIONTYPE_NAME + " = " + DBConstants.LCCONVSTORAGE + ") AND "
            + LocationData.HEIGHT_NAME + " >= ? ";

    protected static String SQL_PRIMARY_EMPTY_ORDERING = " ORDER BY " + LocationData.HEIGHT_NAME + ","
            + EBSLocationData.PRIMARYSEARCHORDER_NAME + "," + LocationData.ADDRESS_NAME;

    protected static String SQL_SECONDARY_EMPTY_ORDERING = " ORDER BY " + LocationData.HEIGHT_NAME + ","
            + EBSLocationData.SECONDARYSEARCHORDER_NAME + "," + LocationData.ADDRESS_NAME;

    protected static String SQL_EMPTY_LOCATION_PRIMARY = SQL_EMPTY_LOCATION_BASE + SQL_PRIMARY_EMPTY_ORDERING;

    protected static String SQL_EMPTY_LOCATION_SECONDARY = SQL_EMPTY_LOCATION_BASE + SQL_SECONDARY_EMPTY_ORDERING;

    protected EBSLocationData mpEBSLocationData = Factory.create(EBSLocationData.class);
    protected EBSLoadServer mpEBSLoadServer = Factory.create(EBSLoadServer.class);
    protected EBSSysConfig mpEBSSysConfig = Factory.create(EBSSysConfig.class);

    public EBSLocation() {
        super();
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
    public String[] findEmptyLocationByZone(String isDevice, int inLocSeqMethodForDeviceID, int inHeight, String isZone)
            throws DBException {
        List<Map> vpLocList = null;
        String[] vasEmptyLoc = null;

        try {
            setMaxRows(1);

            if (isZone.trim().length() > 0) {
                vpLocList = fetchRecords(SQL_EMPTY_LOCATION_BY_ZONE, isDevice, inHeight, isZone);
            } else {
                if (inLocSeqMethodForDeviceID == EBSDBConstants.PRIMARYSEQ) {
                    vpLocList = fetchRecords(SQL_EMPTY_LOCATION_PRIMARY, isDevice, inHeight);
                } else if (inLocSeqMethodForDeviceID == EBSDBConstants.SECONDARYSEQ) {
                    vpLocList = fetchRecords(SQL_EMPTY_LOCATION_SECONDARY, isDevice, inHeight);
                } else {
                    vpLocList = fetchRecords(SQL_EMPTY_LOCATION, isDevice, inHeight);
                }
            }
            if (vpLocList.size() > 0) {
                vasEmptyLoc = new String[2];
                vasEmptyLoc[0] = DBHelper.getStringField(vpLocList.get(0), LocationData.WAREHOUSE_NAME);
                vasEmptyLoc[1] = DBHelper.getStringField(vpLocList.get(0), LocationData.ADDRESS_NAME);
            } else {
                return null;
            }
        } finally {
            setMaxRows();
        }

        return vasEmptyLoc;
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
    public String[] findEmptyLocationByZoneSecondaryDevice(String isDevice, int inLocSeqMethodForDeviceID,
            String isSecondaryDeviceID, int inHeight, String isZone) throws DBException {
        List<Map> vpLocList = null;
        String[] vasEmptyLoc = null;

        try {
            setMaxRows(1);

            if (isZone.trim().length() > 0) {
                vpLocList = fetchRecords(SQL_EMPTY_LOCATION_BY_ZONE, isDevice, inHeight, isZone);
            } else {
                if (inLocSeqMethodForDeviceID == EBSDBConstants.PRIMARYSEQ) {
                    vpLocList = fetchRecords(SQL_EMPTY_LOCATION_PRIMARY, isSecondaryDeviceID, inHeight);
                } else if (inLocSeqMethodForDeviceID == EBSDBConstants.SECONDARYSEQ) {
                    vpLocList = fetchRecords(SQL_EMPTY_LOCATION_SECONDARY, isSecondaryDeviceID, inHeight);
                } else {
                    vpLocList = fetchRecords(SQL_EMPTY_LOCATION, isDevice, inHeight);
                }
            }
            if (vpLocList.size() > 0) {
                vasEmptyLoc = new String[2];
                vasEmptyLoc[0] = DBHelper.getStringField(vpLocList.get(0), LocationData.WAREHOUSE_NAME);
                vasEmptyLoc[1] = DBHelper.getStringField(vpLocList.get(0), LocationData.ADDRESS_NAME);
            } else {
                return null;
            }
        } finally {
            setMaxRows();
        }

        return vasEmptyLoc;
    }

    // DK:30148 - Fetch location for the given retrieval date time of expected
    // receipt.
    public boolean isLocationEmptyToStoreBag(String sAddress, boolean isOOGBag) throws DBException {
        boolean hasEmptyLocationToStoreBag = false;
        // Checks whether the location can fit the bag size and location has empty space
        // to store the bag.
        if (isLocationFitForBagSize(sAddress, isOOGBag) && getVacantCountByAddress(sAddress, isOOGBag) > 0) {
            hasEmptyLocationToStoreBag = true;
        }
        return hasEmptyLocationToStoreBag;
    }

    /**
     * This method is used to find whether the given location can fit the bag size.
     * 
     * @param sAddress
     * @param isOOGBag
     * @return
     * @throws DBException
     */
    public boolean isLocationFitForBagSize(String sAddress, boolean isOOGBag) throws DBException {
        boolean isLocationFitForBagSize = false;
        mpEBSLocationData.clear();
        mpEBSLocationData.setKey(EBSLocationData.ADDRESS_NAME, sAddress);
        if (isOOGBag) {
            mpEBSLocationData.setKey(EBSLocationData.LOCATIONTYPE_NAME,
                    EBSDBConstants.Location.LOCATION_TYPE.OVER_SIZE);
        } else {
            mpEBSLocationData.setKey(EBSLocationData.LOCATIONTYPE_NAME, EBSDBConstants.Location.LOCATION_TYPE.STANDARD);
        }

        if (getCount(mpEBSLocationData) > 0) {
            isLocationFitForBagSize = true;
        }

        Logger.getLogger().logDebug("EBSLocation:" + "Address=" + sAddress + ",Is oversize bag:" + isOOGBag
                + ". isLocationFitForBagSize=" + isLocationFitForBagSize);
        return isLocationFitForBagSize;
    }

    /**
     * This method returns the vacant count of the given address. Calculates the vacant count by 1. Fetchs the total
     * available count configured for the location type. 2. Occupied count of the given address 3. Reserved count of the
     * given address 4. Allocation count of the given address.
     * 
     * @param sAddress
     * @param isOOGBag
     * @return
     * @throws DBException
     */
    public int getVacantCountByAddress(String sAddress, boolean isOOGBag) throws DBException {
        int totalCount, vacantCount, occupiedCount, reservedCount, allocationCount = 0;
        // get number of spaces available by address.
        totalCount = getConfiguedCountByStorageType(isOOGBag);
        // get occupied count of the address
        occupiedCount = getOccupiedCountByAddress(sAddress);
        // get the reserved count of the address
        reservedCount = getReservedCountByAddress(sAddress);
        // get the allocated count of the address
        allocationCount = getAllocatedCountByAddress(sAddress);

        Logger.getLogger()
                .logDebug("EBSLocation:" + "Address=" + sAddress + ",Is oversize bag:" + isOOGBag + ". totalCount="
                        + totalCount + ", occupiedCount=" + occupiedCount + ", reservedCount=" + reservedCount
                        + ",allocationCount=" + allocationCount);

        vacantCount = totalCount - (occupiedCount + reservedCount + allocationCount);

        Logger.getLogger().logDebug("EBSLocation:" + "Address=" + sAddress + ",Is oversize bag:" + isOOGBag
                + ". VacantCountByAddress=" + vacantCount);

        return vacantCount;
    }

    /**
     * This method gets the configured count by storage type
     * 
     * @param isOOGBag
     * @return
     * @throws DBException
     */
    protected int getConfiguedCountByStorageType(boolean isOOGBag) throws DBException {
        if (isOOGBag) {
            return mpEBSSysConfig.getNoOfLocationForOOGStoreLoc();
        } else {
            return mpEBSSysConfig.getNoOfLocationForStandardStoreLoc();
        }
    }

    /**
     * This method fetches the configured vacant limit for the location type.
     * 
     * @param locationType
     * @return
     * @throws DBException
     */
    public int getConfiguedCountByStorageType(int locationType) throws DBException {
        if (locationType > 0 && locationType == EBSDBConstants.Location.LOCATION_TYPE.OVER_SIZE) {
            return mpEBSSysConfig.getNoOfLocationForOOGStoreLoc();
        } else {
            return mpEBSSysConfig.getNoOfLocationForStandardStoreLoc();
        }
    }

    /**
     * This method fetches the configured vacant limit for the location type.
     * 
     * @param locationType
     * @return
     * @throws DBException
     */
    public int getConfiguedCountByStorageType(String locationType) throws DBException {
        int locType = 0;

        try {
            locType = Integer.parseInt(locationType);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            Logger.getLogger().logDebug(
                    "EBSLocation:getConfiguedCountByStorageType:Converting String locationtype to int failed:LocationType="
                            + locationType);
        }

        return getConfiguedCountByStorageType(locType);
    }

    /**
     * This method fetchs the occupied count by given address
     * 
     * @param sAddress
     * @return
     * @throws DBException
     */
    protected int getOccupiedCountByAddress(String sAddress) throws DBException {
        return mpEBSLoadServer.getLoadCount(sAddress, EBSDBConstants.Load.MOVE_STATUS.STORED);
    }

    /**
     * This method fetchs the reserved count by address
     * 
     * @param sAddress
     * @return
     * @throws DBException
     */
    protected int getReservedCountByAddress(String sAddress) throws DBException {
        return mpEBSLoadServer.getLoadCount(sAddress, EBSDBConstants.Load.MOVE_STATUS.RESERVED_FOR_STORAGE);
    }

    protected int getAllocatedCountByAddress(String sAddress) throws DBException {
        return mpEBSLoadServer.getLoadCount(sAddress, EBSDBConstants.Load.MOVE_STATUS.RESERVED_FOR_STORAGE);
    }

    // DK:30075 - Find full empty location and link to the time slot
    /**
     * Get the all location data from the Location table. This method searches all location which is in available state
     * and count of bags stored those locations also location which is not already associated to any time slot.
     * 
     * @return
     * @throws DBException
     */
    public List<Map> getLocationOccupiedCount() throws DBException {
        StringBuilder vpSql = new StringBuilder(
                "SELECT LOCATION.SADDRESS, LOCATION.SWAREHOUSE, LOCATION.ILOCATIONTYPE, ")
                        .append(" (SELECT COUNT(*) FROM LOAD LOAD ")
                        .append("	  WHERE LOAD.SADDRESS = LOCATION.SADDRESS) AS 'OCCUPIEDCOUNT' ")
                        .append(" FROM LOCATION LOCATION ").append(" WHERE ").append(LocationData.LOCATIONSTATUS_NAME)
                        .append(" = ").append(EBSDBConstants.Location.LOCATION_STATUS.AVAILABLE)
                        .append(" AND not exists (SELECT TIMESLOTLINKLOC.sLocationID FROM TIMESLOTLINKLOCATIONS TIMESLOTLINKLOC WHERE TIMESLOTLINKLOC.sLocationID = LOCATION.SADDRESS)")
                        .append(" ORDER BY SWAREHOUSE, OCCUPIEDCOUNT ASC ");

        return fetchRecords(vpSql.toString());
    }

    // DK:30400
    /**
     * this method checks whether the given location address is full or not.
     * 
     * @param warehouse
     * @param sAddress
     * @return
     * @throws DBException
     */
    public boolean isLocationFull(String warehouse, String sAddress) throws DBException {
        boolean isLocationFull = false;
        // Checks whether the location is full
        if (getVacantCountByAddress(warehouse, sAddress) <= 0) {
            isLocationFull = true;
        }
        return isLocationFull;
    }

    /**
     * This method gets the vacant count of the given location address.
     * 
     * @param warehouse
     * @param sAddress
     * @return
     * @throws DBException
     */
    public int getVacantCountByAddress(String warehouse, String sAddress) throws DBException {
        boolean isOOGBagType = false;
        if (getLocationTypeValue(warehouse, sAddress) == EBSDBConstants.Location.LOCATION_TYPE.OVER_SIZE) {
            isOOGBagType = true;
        }
        return getVacantCountByAddress(sAddress, isOOGBagType);
    }

    /**
     * Find 1 unoccupied location in the given bank
     * - All of unoccupied locations are sorted by address
     * 
     * @param warehouse warehouse name
     * @param bank bank, for example, "001"
     * @return The found address, for example, "001001001"
     * @throws DBException When anything goes wrong
     */
    public String findUnoccupiedLocationOfBank(String warehouse, String bank) throws DBException {
        StringBuilder strBuilder = new StringBuilder()
                .append("   SELECT TOP 1 sAddress")
                .append("     FROM LOCATION")
                .append("    WHERE sWarehouse = '").append(warehouse).append("'")
                .append("      AND SUBSTRING(sAddress, 3, 3) = '").append(bank).append("'")
                .append("      AND LEFT(sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'") // 20 = storage location
                .append("      AND iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append("      AND iEmptyFlag = ").append(DBConstants.UNOCCUPIED)
                .append(" ORDER BY sAddress ASC ");
        List<Map> list = fetchRecords(strBuilder.toString());      
        for(Map map : list) {
            return DBHelper.getStringField(map, LoadData.ADDRESS_NAME);
        }
        return null;
    }
    public String findUnoccupiedLocationOfLevel(String warehouse, String level, String deviceID) throws DBException {
        StringBuilder strBuilder = new StringBuilder()
                .append(" SELECT TOP 1 sAddress")
                .append(" FROM LOCATION")
                .append(" WHERE sWarehouse = '").append(warehouse).append("'")
                .append(" AND sDeviceID = '").append(deviceID).append("'")
                .append(" AND SUBSTRING(sAddress, 9, 2) = '").append(level).append("'")
                .append(" AND LEFT(sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'") // 20 = storage location
                .append(" AND iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append(" AND iEmptyFlag = ").append(DBConstants.UNOCCUPIED)
                .append(" ORDER BY sAddress ASC ");
        List<Map> list = fetchRecords(strBuilder.toString());      
        for(Map map : list) {
            return DBHelper.getStringField(map, LoadData.ADDRESS_NAME);
        }
        return null;
    }
    
    public String findUnoccupiedLocationForDevice(String warehouse, String deviceID) throws DBException {
        StringBuilder strBuilder = new StringBuilder()
                .append(" SELECT TOP 1 sAddress")
                .append(" FROM LOCATION")
                .append(" WHERE sWarehouse = '").append(warehouse).append("'")
                .append(" AND sDeviceID = '").append(deviceID).append("'")
                .append(" AND LEFT(sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'") // 20 = storage location
                .append(" AND iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append(" AND iEmptyFlag = ").append(DBConstants.UNOCCUPIED)
                .append(" ORDER BY sAddress ASC ");
        List<Map> list = fetchRecords(strBuilder.toString());      
        for(Map map : list) {
            return DBHelper.getStringField(map, LoadData.ADDRESS_NAME);
        }
        return null;
    }
    /**
     * Find 1 unoccupied location at given level of that device
     * - All of unoccupied locations are sorted by address
     * 
     * @param warehouse warehouse name
     * @param deviceId deviceId, for example, "9001"
     * @param level level, for example, "01"
     * @return The found address, for example, "2000100101"
     * @throws DBException When anything goes wrong
     */
    
    public String findUnoccupiedLocationOfLevelAndDeviceId(String warehouse,String level, String deviceId) throws DBException {
        StringBuilder strBuilder = new StringBuilder()
                .append("   SELECT TOP 1 sAddress")
                .append("     FROM LOCATION")
                .append("    WHERE sWarehouse = '").append(warehouse).append("'")
                .append("      AND RIGHT(sAddress, 2) = '").append(level).append("'")
                .append("      AND LEFT(sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'") // 20 = storage location
                .append("      AND iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append("      AND sDeviceID = '").append(deviceId).append("'")
                .append("      AND iEmptyFlag = ").append(DBConstants.UNOCCUPIED)
                .append(" ORDER BY sAddress ASC ");
        List<Map> list = fetchRecords(strBuilder.toString());      
        for(Map map : list) {
            return DBHelper.getStringField(map, LoadData.ADDRESS_NAME);
        }
        return null;
    }
}
