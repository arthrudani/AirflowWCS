package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuoc.wrxj.custom.ebs.dataserver.location.MultiDeepVacantManager;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfigData;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc.TimeSlotLinkLocationsEnum;
import com.daifukuoc.wrxj.custom.ebs.entity.location.LocationEntity;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;

import io.netty.util.internal.StringUtil;

public class EBSTimeLocationManager extends StandardServer {

	protected EBSTimeServer mpEBSTimeServer = Factory.create(EBSTimeServer.class);
	protected EBSTimeSlotLocationServer mpEBSTimeSlotLocationServer = Factory.create(EBSTimeSlotLocationServer.class);
	protected EBSLocationServer mpEBSLocationServer = Factory.create(EBSLocationServer.class);
	protected EBSRouteServer mpEBSRouteServer = Factory.create(EBSRouteServer.class);

	/**
	 * Constructor w/o key name
	 */
	public EBSTimeLocationManager() {
		this(null);
		mpLogger.setDefaultLoggerKey("EBSTimeLocationManager");
	}

	/**
	 * Constructor with key name
	 *
	 * @param isKeyName
	 */
	public EBSTimeLocationManager(String isKeyName) {
		super(isKeyName);
		logDebug("Creating " + getClass().getSimpleName());
	}

	/**
	 * Web application constructor for per user connection pooling
	 * 
	 * @param keyName
	 * @param dbo
	 */
	public EBSTimeLocationManager(String keyName, DBObject dbo) {
		super(keyName, dbo);
		logDebug("Creating " + getClass().getSimpleName());

	}

	/**
	 * Shuts down this controller by canceling any timers and shutting down the
	 * Equipment.
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	public void addTimeSlot(String startTime, String schemaId) throws DBException {
		mpEBSTimeServer.addTimeSlot(startTime, schemaId);
	}

	/**
	 * Method to delete a Time Slot.
	 *
	 * @param schemaId.
	 * @param timeSlot.
	 * @exception DBException
	 */
	public void deleteTimeSlot(String startTime, String schemaId) throws DBException {
		mpEBSTimeServer.deleteTimeSlot(startTime, schemaId);
	}

	/**
	 * Method to fetch the a Time Slot by date return List of time slot that comes
	 * under
	 * 
	 * @param timeSlot.
	 */
	@SuppressWarnings("rawtypes")
	public String getWarehouseByFinalSortLoction(String finalSortLoction) throws DBException {
		return mpEBSTimeSlotLocationServer.getWarehouseByFinalSortLoction(finalSortLoction);
	}

	// DK:30148 - When the expected receipts is new to register. Fetch location for
	// the given retrieval date time of expected
	/**
	 * This method fetch location for the given retrieval date time 1. Returns the
	 * location if the location found for the given retrieval date time. 2. If
	 * location not found, then find the full empty location and associate to the
	 * time slot 2.1 then return the location address
	 * 
	 * @param mpROMData
	 * @param wh
	 * @return
	 * @throws DBException
	 * @throws ParseException
	 */
	public String findLocationForExpectedReceipt(ExpectedReceiptMessageData mpROMData, String wh)
			throws DBException, ParseException {
		String locationId = "";
		String entranceId = "";
		// Find any location already associated to time and have space to store.
		locationId = getLocationsByTime(mpROMData.getDefaultRetrievalDateTime(), wh, mpROMData.isOOGBag());
		mpLogger.logDebug("The location :" + locationId + " found for the given warehouse: " + wh
				+ " and the time slot:" + mpROMData.getDefaultRetrievalDateTime());
		// If the location is not found
		// DK:30075 - Find full empty location and link to the time slot
		if (locationId.isEmpty()) {
			mpLogger.logDebug("There is no existing location found for the given warehouse:" + wh
					+ " and the time slot:" + mpROMData.getDefaultRetrievalDateTime());
			// 1. Find location with space(full location empty) to store bags .
			// 2. Associate the location with this time slot.
			// 3. Return the location.
			locationId = allocateVacantLocationForBag(mpROMData.getDefaultRetrievalDateTime(), wh, mpROMData.isOOGBag());
		}
		// DK:30294 - Send expected receipt response msg to host
		entranceId = mpEBSLocationServer.getGateIdForLocationAddress(locationId);
		mpLogger.logDebug("Entrance id:" + entranceId + " found for this expected receipt.");
		return entranceId;
	}

	/**
	 * Gets the locations associated with the specified timeslot
	 * 
	 * @param retrvDateTime
	 * @param wareHouse
	 * @param isOOGBag
	 * @return
	 * @throws DBException
	 * @throws ParseException
	 */
	@SuppressWarnings("rawtypes")
	public String getLocationsByTime(String retrvDateTime, String wareHouse, boolean isOOGBag)
			throws DBException, ParseException {

		int timeSlotId = 0;
		String timeSlot = "";
		String locationId = "";
		String timeSlotWareHouse = "";
		List<Map> timeSlotLocationMap = null;

		logDebug("Searching location for the retrieval date time:" + retrvDateTime + ", warehouse:" + wareHouse);
		// Get the time slot for the given retrieval date time.
		TimeSlotConfigData timeSlotData = mpEBSTimeServer.getTopRowTimeSlotListbyTime(retrvDateTime);

		if (timeSlotData != null) {
			timeSlotId = timeSlotData.getTimeslotID();
			timeSlot = timeSlotData.getStartTime();
			if (timeSlotId > 0) {
				// Fetch all locations for the identified time slot
				timeSlotLocationMap = mpEBSTimeSlotLocationServer.getLocationsAssociatedWithThisTimeSlot(timeSlotId);

				for (Map timeSlotLocationData : timeSlotLocationMap) {
					locationId = DBHelper.getStringField(timeSlotLocationData,
							TimeSlotLinkLocationsEnum.LOCATION.getName());
					timeSlotWareHouse = DBHelper.getStringField(timeSlotLocationData,
							TimeSlotLinkLocationsEnum.WAREHOUSE.getName());
					// Check whether there is any empty space to handle the bag.
					if (!StringUtil.isNullOrEmpty(locationId) && !StringUtil.isNullOrEmpty(timeSlotWareHouse)
							&& timeSlotWareHouse.equalsIgnoreCase(wareHouse)) {
						logDebug("The location:" + locationId + " for the time slot:" + timeSlot + " and warehouse:"
								+ timeSlotWareHouse);
						if (mpEBSLocationServer.isLocationEmptyToStoreBag(locationId, isOOGBag)) {
							// Find the empty space
							// Check whether it can hold the OOG bag is if it true.
							break;
						}
					}
					locationId = "";// reset the locationId
				}
			}
		}

		return locationId;
	}

	/**
	 * Gets the locations associated with the specified timeslot
	 * 
	 * @param retrvDateTime
	 * @param isOOGBag
	 * @return
	 * @throws DBException
	 * @throws ParseException
	 */
	@SuppressWarnings("rawtypes")
	public String getLocationsByTime(String retrvDateTime, boolean isOOGBag) throws DBException, ParseException {

		int timeSlotId = 0;
		String timeSlot = "";
		String locationId = "";
		List<Map> timeSlotLocationMap = null;

		logDebug("Searching location for the retrieval date time:" + retrvDateTime);
		// Get the time slot for the given retrieval date time.
		TimeSlotConfigData timeSlotData = mpEBSTimeServer.getTopRowTimeSlotListbyTime(retrvDateTime);

		if (timeSlotData != null) {
			timeSlotId = timeSlotData.getTimeslotID();
			timeSlot = timeSlotData.getStartTime();
			if (timeSlotId > 0) {
				// Fetch all locations for the identified time slot
				timeSlotLocationMap = mpEBSTimeSlotLocationServer.getLocationsAssociatedWithThisTimeSlot(timeSlotId);

				for (Map timeSlotLocationData : timeSlotLocationMap) {
					locationId = DBHelper.getStringField(timeSlotLocationData,
							TimeSlotLinkLocationsEnum.LOCATION.getName());
					// Check whether there is any empty space to handle the bag.
					if (!StringUtil.isNullOrEmpty(locationId)) {
						logDebug("The location:" + locationId + " for the time slot:" + timeSlot);
						if (mpEBSLocationServer.isLocationEmptyToStoreBag(locationId, isOOGBag)) {
							// Find the empty space
							// Check whether it can hold the OOG bag is if it true.
							break;
						}
					}
					locationId = "";
				}
			}
		}

		return locationId;

	}

	// DK:30075 - Find full empty location and link to the time slot
	/**
	 * This method will find a vacant location and assign to the time slot
	 * 
	 * @param retrvDateTime
	 * @param warehouse
	 * @param OOGBag
	 * @return
	 * @throws ParseException
	 * @throws DBException
	 */
	public String allocateVacantLocationForBag(String retrvDateTime, String warehouse, boolean OOGBag)
			throws ParseException, DBException {
		LocationEntity newLocationToStore = new LocationEntity();
		String newLocationId = "";
		int timeSlotId = 0;
		// Find the new location with full empty based on warehouse and bag size
		mpLogger.logDebug("Trying to find vacant location and associate to the time slot. " + "RetrievalDateTime:"
				+ retrvDateTime + "Warehouse:" + warehouse + "OOGBag:" + OOGBag);
		newLocationToStore = findVacantLocationByWarehouse(warehouse, OOGBag);
		// If any location found then
		if (newLocationToStore != null && newLocationToStore.getAddress() != null) {
			// Search the time slot to associate the location to the time slot
			newLocationId = newLocationToStore.getAddress();
			mpLogger.logDebug("Empty location found for the association: " + newLocationId);
			// Fetch the time slot for the given retrieval date time.
			TimeSlotConfigData timeSlotData = mpEBSTimeServer.getTopRowTimeSlotListbyTime(retrvDateTime);
			mpLogger.logDebug("Time slot planning to link to the location:" + timeSlotData);
			if (timeSlotData != null) {
				timeSlotId = timeSlotData.getTimeslotID();
				if (timeSlotId > 0) {
					// Link the time slot and location using the TimeSlotLinkLocation table.
					mpEBSTimeSlotLocationServer.allocateVacantLocationToTimeSlot(timeSlotId,
							newLocationToStore.getAddress(), newLocationToStore.getWarehouse());
					mpLogger.logDebug("New location :" + newLocationId + " in the warehouse: "
							+ newLocationToStore.getWarehouse() + " attached to the time slot:" + timeSlotId);
				}

			}
		}
		return newLocationId;
	}

	/**
	 * This method is used to find the vacant location by warehouse, bag type
	 * 
	 * @param warehouse
	 * @param OOGBag
	 * @return
	 */
	public LocationEntity findVacantLocationByWarehouse(String warehouse, boolean OOGBag) {
		MultiDeepVacantManager vacantManager = Factory.create(MultiDeepVacantManager.class);
		return vacantManager.getVacantLocationForBagSize(OOGBag, warehouse);
	}

	/**
	 * This method return the entrance id for the given location address
	 * 
	 * @param locationId
	 * @return
	 * @throws DBException
	 */
	public String getEntranceIdForGivenDestination(String locationId) throws DBException {
		return mpEBSRouteServer.getRouteFromTo(locationId);
	}

	/**
	 * Method is to check whether if there is a timeslot available for the given
	 * expiration date time of the flight
	 * 
	 * @throws DBException
	 * @throws ParseException
	 */
	@SuppressWarnings("rawtypes")
	public boolean isTimeSlotPresentForGivenTime(String flightTime) throws ParseException, DBException {
		return mpEBSTimeServer.isTimeSlotPresentForGivenTime(flightTime);
	}

	/**
	 * This method is used to customize the logger key
	 * 
	 * @param loggerKey
	 */
	public void setLoggerKey(String loggerKey) {
		mpLogger.setDefaultLoggerKey(
				loggerKey + EBSConstants.LOG_FILE_NAME_SEPARATOR + this.getClass().getSimpleName());
	}

}
