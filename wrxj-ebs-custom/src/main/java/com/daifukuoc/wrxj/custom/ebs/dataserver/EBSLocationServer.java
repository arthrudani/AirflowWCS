package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;
import com.daifukuoc.wrxj.custom.ebs.entity.location.LocationEntity;
import com.daifukuoc.wrxj.custom.ebs.entity.location.LocationEntityEnum;

public class EBSLocationServer extends StandardLocationServer {

	protected EBSLocation mpEBSLocation = Factory.create(EBSLocation.class);
	protected EBSDeviceServer mpEBSDeviceServer;

	public EBSLocationServer() {
		super();
	}

	public EBSLocationServer(String keyName) {
		super(keyName);
	}

	/**
	 * Web application constructor for per user connection pooling
	 * 
	 * @param keyName
	 * @param dbo
	 */
	public EBSLocationServer(String keyName, DBObject dbo) {
		super(keyName, dbo);
	}

	/**
	 * Finds and Reserves an Empty Location for storage.
	 *
	 * @param isWarehouse    The desired location's warehouse (unused in baseline).
	 * @param isDeviceID     The current location's device
	 * @param inHeight       The Current location's height
	 * @param inLength       Load length - ignored in baseline implementation
	 * @param inWidth        Load width - ignored in baseline implementation
	 * @param izIsForBinfull Flag to indicate if this method is called for an
	 *                       alternate location search due to a bin full error.
	 * @return String[2] { Warehouse, Address } or <code>null</code>
	 * @throws DBException if there is one
	 */
	@SuppressWarnings("rawtypes")
	protected String[] reserveEmptyLocationForDevice(String isWarehouse, String isDeviceID, int inHeight, int inLength,
			int inWidth, String isRecommendedZone, boolean izIsForBinfull) throws DBException {
		String[] vasEmptyLoc = null;
		initializeEBSDeviceServer();

		// Check the device. Ignore offline status for bin full
		if (isDeviceID.length() == 0)
			return null;
		if (!izIsForBinfull) {
			if (!isDeviceAvailable(isDeviceID)) {
				logError("Device " + isDeviceID + " is not Physically or Operationally online!!");
				return null;
			}
		}

		int dLocSeqMethodForDeviceID = mpEBSDeviceServer.getLocSeqMethodForDeviceID(isDeviceID);

		// Find and reserve a location
		TransactionToken vpTranTok = null;
		try {
			vpTranTok = startTransaction();
			if (isRecommendedZone == null || isRecommendedZone.length() < 1) {
				vasEmptyLoc = mpEBSLocation.findEmptyLocationByZone(isDeviceID, dLocSeqMethodForDeviceID, inHeight,
						isRecommendedZone);
			} else {
				List<Map> vpZoneGroup = null;
				vpZoneGroup = getZoneGroupList(isRecommendedZone);
				if (vpZoneGroup == null || vpZoneGroup.size() == 0) {
					vasEmptyLoc = mpEBSLocation.findEmptyLocationByZone(isDeviceID, dLocSeqMethodForDeviceID, inHeight,
							isRecommendedZone);
				} else {
					String vsCurrentZone = "";
					for (Map vpMap : vpZoneGroup) {
						vsCurrentZone = vpMap.get(ZoneGroupData.ZONE_NAME).toString();
						vasEmptyLoc = mpEBSLocation.findEmptyLocationByZone(isDeviceID, dLocSeqMethodForDeviceID,
								inHeight, vsCurrentZone);
						if (vasEmptyLoc != null) {
							break;
						}
					}
				}
			}
			if (vasEmptyLoc == null) {

				// if secondary device is inop, check its locations
				if (mpEBSDeviceServer.isDeviceInoperable(mpEBSDeviceServer.getSecondaryDeviceID(isDeviceID))) {
					vasEmptyLoc = reserveEmptyLocationForSecondaryDevice(isWarehouse, isDeviceID,
							dLocSeqMethodForDeviceID, mpEBSDeviceServer.getSecondaryDeviceID(isDeviceID), inHeight,
							inLength, inWidth, isRecommendedZone, izIsForBinfull);
				}
				if (vasEmptyLoc == null) {
					logError("NO empty location found for DeviceId " + isDeviceID);
				}
			} else {
				// Mark the Location as RESERVED.
				mpLocation.setEmptyFlagValue(vasEmptyLoc[0], vasEmptyLoc[1], DBConstants.LCRESERVED);
				commitTransaction(vpTranTok);
			}
		} finally {
			endTransaction(vpTranTok);
		}

		return (vasEmptyLoc);
	}

	/**
	 * Finds and Reserves an Empty Location for storage.
	 *
	 * @param isWarehouse    The desired location's warehouse (unused in baseline).
	 * @param isDeviceID     The current location's device
	 * @param inHeight       The Current location's height
	 * @param inLength       Load length - ignored in baseline implementation
	 * @param inWidth        Load width - ignored in baseline implementation
	 * @param izIsForBinfull Flag to indicate if this method is called for an
	 *                       alternate location search due to a bin full error.
	 * @return String[2] { Warehouse, Address } or <code>null</code>
	 * @throws DBException if there is one
	 */
	@SuppressWarnings("rawtypes")
	protected String[] reserveEmptyLocationForSecondaryDevice(String isWarehouse, String isDeviceID,
			int inLocSeqMethodForDeviceID, String isSecondaryDeviceID, int inHeight, int inLength, int inWidth,
			String isRecommendedZone, boolean izIsForBinfull) throws DBException {
		String[] vasEmptyLoc = null;
		initializeEBSDeviceServer();

		// Check the device. Ignore offline status for bin full
		if (isDeviceID.length() == 0)
			return null;

		// Find and reserve a location
		TransactionToken vpTranTok = null;
		try {
			vpTranTok = startTransaction();
			if (isRecommendedZone == null || isRecommendedZone.length() < 1) {
				vasEmptyLoc = mpEBSLocation.findEmptyLocationByZoneSecondaryDevice(isDeviceID,
						inLocSeqMethodForDeviceID, isSecondaryDeviceID, inHeight, isRecommendedZone);
			} else {
				List<Map> vpZoneGroup = null;
				vpZoneGroup = getZoneGroupList(isRecommendedZone);
				if (vpZoneGroup == null || vpZoneGroup.size() == 0) {
					vasEmptyLoc = mpEBSLocation.findEmptyLocationByZoneSecondaryDevice(isDeviceID,
							inLocSeqMethodForDeviceID, isSecondaryDeviceID, inHeight, isRecommendedZone);
				} else {
					String vsCurrentZone = "";
					for (Map vpMap : vpZoneGroup) {
						vsCurrentZone = vpMap.get(ZoneGroupData.ZONE_NAME).toString();
						vasEmptyLoc = mpEBSLocation.findEmptyLocationByZoneSecondaryDevice(isDeviceID,
								inLocSeqMethodForDeviceID, isSecondaryDeviceID, inHeight, vsCurrentZone);
						if (vasEmptyLoc != null) {
							break;
						}
					}
				}
			}
			if (vasEmptyLoc == null) {
				logError("NO empty location found for DeviceId " + isDeviceID);
			} else {
				// Mark the Location as RESERVED.
				mpLocation.setEmptyFlagValue(vasEmptyLoc[0], vasEmptyLoc[1], DBConstants.LCRESERVED);
				commitTransaction(vpTranTok);
			}
		} finally {
			endTransaction(vpTranTok);
		}

		return (vasEmptyLoc);
	}
	
	
	public LocationData reserveUnoccupiedLocationOfLevelIfAvailable(String warehouse, String level, String deviceID) throws DBException {
	    LocationData locationData = null;
	    TransactionToken vpTranTok = null;
	    
	    try {
	        // Start a new transaction
            vpTranTok = startTransaction();
            
            // Find an unoccupied location of the given device
            String locationAddress = mpEBSLocation.findUnoccupiedLocationOfLevel(warehouse, level, deviceID);
            if (locationAddress != null && !locationAddress.isEmpty()) {            
                // Mark the found location as reserved
                mpLocation.setEmptyFlagValue(warehouse, locationAddress, DBConstants.LCRESERVED);
             
                // Read in the modified location data to return the found & reserved location
                locationData = getLocationRecordByAddress(locationAddress);    
            }
            
            // Commit the change made to the empty flag
            commitTransaction(vpTranTok);
        } finally {
            endTransaction(vpTranTok);
        }
     
	    return locationData;
	}
	/**
	 * Find one unoccupied location  in the given bank and mark it as reserved
	 * 
	 * @param warehouse warehouse name
	 * @param bank bank, for example, 001 in an address 001001001
	 * @return The found/reserved location
	 * @throws DBException When anything goes wrong
	 */
	public LocationData reserveUnoccupiedLocationOfBankIfAvailable(String warehouse, String bank) throws DBException {
	    LocationData locationData = null;
	    TransactionToken vpTranTok = null;
	    
	    try {
	        // Start a new transaction
            vpTranTok = startTransaction();
            
            // Find an unoccupied location of the given device
            String locationAddress = mpEBSLocation.findUnoccupiedLocationOfBank(warehouse, bank);
            if (locationAddress != null && !locationAddress.isEmpty()) {            
                // Mark the found location as reserved
                mpLocation.setEmptyFlagValue(warehouse, locationAddress, DBConstants.LCRESERVED);
             
                // Read in the modified location data to return the found & reserved location
                locationData = getLocationRecordByAddress(locationAddress);    
            }
            
            // Commit the change made to the empty flag
            commitTransaction(vpTranTok);
        } finally {
            endTransaction(vpTranTok);
        }
     
	    return locationData;
	}
	
	public LocationData reserveUnoccupiedLocationForDeviceIfAvailable(String warehouse, String deviceID) throws DBException {
	    LocationData locationData = null;
	    TransactionToken vpTranTok = null;
	    
	    try {
	        // Start a new transaction
            vpTranTok = startTransaction();
            
            // Find an unoccupied location of the given device
            String locationAddress = mpEBSLocation.findUnoccupiedLocationForDevice(warehouse, deviceID);
            if (locationAddress != null && !locationAddress.isEmpty()) {            
                // Mark the found location as reserved
                mpLocation.setEmptyFlagValue(warehouse, locationAddress, DBConstants.LCRESERVED);
             
                // Read in the modified location data to return the found & reserved location
                locationData = getLocationRecordByAddress(locationAddress);    
            }
            
            // Commit the change made to the empty flag
            commitTransaction(vpTranTok);
        } finally {
            endTransaction(vpTranTok);
        }
     
	    return locationData;
	}

	protected void initializeEBSDeviceServer() {
		if (mpEBSDeviceServer == null) {
			mpEBSDeviceServer = Factory.create(EBSDeviceServer.class);
		}
	}

	// DK:30148 - Fetch location for the given retrieval date time of expected
	/**
	 * This method checks whether the given location is empty to store the bag.
	 * 
	 * @param sAddress
	 * @param overSizeBag
	 * @return
	 * @throws DBException
	 */
	public boolean isLocationEmptyToStoreBag(String sAddress, boolean overSizeBag) throws DBException {
		return mpEBSLocation.isLocationEmptyToStoreBag(sAddress, overSizeBag);
	}

	// DK:30075 - Find full empty location and link to the time slot
	/**
	 * This method get the all location data from the database(Location table)
	 * 
	 * @param onlyVacantLocation
	 * @return
	 * @throws DBException
	 */
	private LinkedHashMap<String, LocationEntity> getAvailableLocationData(boolean loadFullEmptyLocation)
			throws DBException {
		// List<LocationEntity> locationDataList = new ArrayList();
		LinkedHashMap<String, LocationEntity> maplocationDataList = new LinkedHashMap<>();
		List<Map> vpLocList = null;
		String address = "";
		String warehouse = "";
		int locationType = 0;
		int vacantLimit = 0;
		int occupiedCount = 0;

		LocationEntity locationEntity = null;
		// Get all location with occupied count for the location status = available
		vpLocList = mpEBSLocation.getLocationOccupiedCount();

		if (vpLocList != null && vpLocList.size() > 0) {
			// Iterate the list and create the locationEntity object for the each row.
			for (Map dbRec : vpLocList) {
				address = DBHelper.getStringField(dbRec, LocationEntityEnum.ADDRESS.getName());
				warehouse = DBHelper.getStringField(dbRec, LocationEntityEnum.WAREHOUSE.getName());
				locationType = DBHelper.getIntegerField(dbRec, LocationEntityEnum.LOCATIONTYPE.getName());
				// Get the vacant limit which is configured in the database by the location type
				vacantLimit = mpEBSLocation.getConfiguedCountByStorageType(locationType);
				occupiedCount = DBHelper.getIntegerField(dbRec, LocationEntityEnum.OCCUPIEDCOUNT.getName());

				if (address != null) {
					locationEntity = new LocationEntity();
					locationEntity.setAddress(address);
					locationEntity.setWarehouse(warehouse);
					locationEntity.setLocationType(locationType);
					locationEntity.setVacantLimit(vacantLimit);
					locationEntity.setOccupiedQty(occupiedCount);
					// Load only full empty location
					if (loadFullEmptyLocation) {
						if (locationEntity.isLocationFullEmpty()) {
							maplocationDataList.put(address, locationEntity);
						}
					} else {
						maplocationDataList.put(address, locationEntity);
					}
				}
			}

		}
		mpLogger.logDebug("Vacant location data is fetched for Search key: loadOnlyFullLocationEmpty:"
				+ loadFullEmptyLocation + ", location loaded count:"
				+ (maplocationDataList != null ? maplocationDataList.size() : "[location data list empty]"));
		return maplocationDataList;
	}

	/**
	 * This method get all vacant location data which is full empty.
	 * 
	 * @return
	 * @throws DBException
	 */
	public LinkedHashMap<String, LocationEntity> getFullVacantLocationEmptyData() throws DBException {
		boolean onlyFullVacantLocation = true;
		return getAvailableLocationData(onlyFullVacantLocation);
	}

	/**
	 * This method is used to get all the available location data (full empty, half
	 * empty and full).
	 * 
	 * @return
	 * @throws DBException
	 */
	public LinkedHashMap<String, LocationEntity> getAllLocationData() throws DBException {
		// below false represents the only full location empty location. If it is false
		// then all location will be fetched
		return getAvailableLocationData(false);
	}
	
	//DK:30400
	/**
	 * Gets the gate/entrance id for the given location address
	 * @param locationId
	 * @return
	 * @throws DBException
	 */
	public String getGateIdForLocationAddress(String locationId) throws DBException {
		EBSRouteServer mpEBSRouteServer = Factory.create(EBSRouteServer.class);
		return mpEBSRouteServer.getRouteFromTo(locationId);
	}
	
	/**
	 * Gets the location address for the given gate id.
	 * @param gateId
	 * @return
	 * @throws DBException
	 */
	public String getLocationAddressForGateId(String gateId) throws DBException {
		EBSRouteServer mpEBSRouteServer = Factory.create(EBSRouteServer.class);
		return mpEBSRouteServer.getRouteFromTo(gateId);
	}
	
	/**
	 * Gets the location record for the given location address
	 * @param address
	 * @return
	 * @throws DBException
	 */
	public LocationData getLocationRecordByAddress(String address) throws DBException {
		LocationData lcdata = Factory.create(LocationData.class);
		lcdata.setKey(LocationData.ADDRESS_NAME, address);

		return getLocationRecord(lcdata, DBConstants.NOWRITELOCK);
	}
	
	/**
	 * Gets the location record for the given gate/entrance id.
	 * @param gateId
	 * @return
	 * @throws DBException
	 */
	public LocationData getLocationRecordByGateId(String gateId) throws DBException {
		LocationData locationData = null;
		//Get the location address for the given gate id
		String locationAddress = getLocationAddressForGateId(gateId);
		if (locationAddress != null && locationAddress.trim().length() > 0) {
			//Get the location record for the location address
			locationData = getLocationRecordByAddress(locationAddress);
		}
		return locationData;
	}
	
	/**
	 * This method gets the warehouse for the given location address
	 * @param address
	 * @return
	 * @throws DBException
	 */
	public String getWarehouseForLocationAddress(String address) throws DBException {
		String warehouse = "";
		LocationData locationData = getLocationRecordByAddress(address);
		if(locationData!=null && locationData.getWarehouse()!=null) {
			warehouse = locationData.getWarehouse();
		}
		return warehouse;
	}
	
	/**
	 * This method gets the warehouse for the given gate/entrance id
	 * @param address
	 * @return
	 * @throws DBException
	 */
	public String getWarehouseForGateId(String gateId) throws DBException {
		String warehouse = "";
		String locationAddress = getLocationAddressForGateId(gateId);
		if (locationAddress != null && locationAddress.trim().length() > 0) {
			LocationData locationData = getLocationRecordByAddress(locationAddress);
			if (locationData != null && locationData.getWarehouse() != null) {
				warehouse = locationData.getWarehouse();
			}
		}

		return warehouse;
	}
	
	/**
	 * This method checks whether the given location is full or not.
	 * @param warehouse
	 * @param address
	 * @return
	 * @throws DBException
	 */
	public boolean isLocationFull(String warehouse, String address) throws DBException {
		return mpEBSLocation.isLocationFull(warehouse, address);
	}
	
	/**
	 * Method to set Location Empty flag.
	 * 
	 * @throws DBException if anything goes wrong
	 */
	public void updateLocationEmptyFlag(String isWarehouse, String isAddress, int inEmptyFlag) throws DBException {
		mpEBSLocation.setEmptyFlagValue(isWarehouse, isAddress, inEmptyFlag);
	}

}
