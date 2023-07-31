package com.daifukuoc.wrxj.custom.ebs.dataserver;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc.TimeSlotLinkLocations;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc.TimeSlotLinkLocationsData;

public class EBSTimeSlotLocationServer extends StandardServer {

	protected String msMyClass = null;
	protected StandardUserServer mpUserServer = null;
	protected TimeSlotLinkLocations mpEBSTimeSlotLocation = Factory.create(TimeSlotLinkLocations.class);
	protected TimeSlotLinkLocationsData timeSlotLocationData = Factory.create(TimeSlotLinkLocationsData.class);

	/**
	 * Constructor w/o key name
	 */
	public EBSTimeSlotLocationServer() {
		this(null);
	}

	/**
	 * Constructor with key name
	 *
	 * @param isKeyName
	 */
	public EBSTimeSlotLocationServer(String isKeyName) {
		super(isKeyName);
		logDebug("Creating " + getClass().getSimpleName());

	}

	/**
	 * Web application constructor for per user connection pooling
	 * 
	 * @param keyName
	 * @param dbo
	 */
	public EBSTimeSlotLocationServer(String keyName, DBObject dbo) {
		super(keyName, dbo);
		logDebug("Creating " + getClass().getSimpleName());

	}

	protected void initializeUserServer() {
		if (mpUserServer == null) {
			mpUserServer = Factory.create(StandardUserServer.class, msMyClass);
		}
	}

	/**
	 * Shuts down this controller by canceling any timers and shutting down the
	 * Equipment.
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
	}

	/**
	 * Method to fetch the a Time Slot by date return List of time slot that comes
	 * under
	 * 
	 * @param timeSlot.
	 */
	@SuppressWarnings("rawtypes")
	public String getWarehouseByFinalSortLoction(String finalSortLoction) throws DBException {
		return mpEBSTimeSlotLocation.getWarehouseByFinalSortLoction(finalSortLoction);
	}

	// DK:30075 - Creating timeslot for the expected receipt.
	/**
	 * Gets the locations associated with the specified timeslot
	 * 
	 * @param sTimeSlotId
	 * @return
	 * @throws DBException
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> getLocationsAssociatedWithThisTimeSlot(int sTimeSlotId) throws DBException {
		return mpEBSTimeSlotLocation.getLocationsAssociatedWithThisTimeSlot(sTimeSlotId);
	}

	// DK:30075 - Find full empty location and link to the time slot
	/**
	 * This method creates an association between the location and time slot
	 * 
	 * @param timeSlotId
	 * @param location
	 * @param warehouse
	 * @return
	 */
	public String allocateVacantLocationToTimeSlot(int timeSlotId, String location, String warehouse) {
		String newLocationToStore = "";
		TimeSlotLinkLocationsData mpEBSTimeSlotLocationData = Factory.create(TimeSlotLinkLocationsData.class);

		TransactionToken tt = null;
		try {
			tt = startTransaction();
			mpEBSTimeSlotLocationData.setWarehouse(warehouse);
			mpEBSTimeSlotLocationData.setLocation(location);
			mpEBSTimeSlotLocationData.setiTimeSlotID(timeSlotId);
			mpEBSTimeSlotLocation.assiocateLocationToTimeSlot(mpEBSTimeSlotLocationData);
			commitTransaction(tt);
			newLocationToStore = location;

		} catch (Exception e) {
			logException("Error adding in TimeSlotLocation: TimeSlotId:" + timeSlotId + ", location: " + location
					+ ",warehouse:" + warehouse, e);
		} finally {
			endTransaction(tt);
		}

		return newLocationToStore;
	}
}
