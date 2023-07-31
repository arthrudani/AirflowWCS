package com.daifukuoc.wrxj.custom.ebs.dataserver.location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.dataserver.standard.StandardServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.entity.location.LocationEntity;
import com.daifukuoc.wrxj.custom.ebs.entity.location.LocationEntityComparator;

/**
 * MultiDeepVacantManager class is used to handle the vacant location list
 * 
 * @author Administrator
 *
 *         DK:30075 - Find full empty location and link to the time slot
 */
public class MultiDeepVacantManager extends StandardServer {
	// Main variable contains all the vacant location in a map location address as
	// key, Location entity as data .
	protected LinkedHashMap<String, LocationEntity> mapVacantLocation = new LinkedHashMap<>();
	// Main variable contains all the vacant location list in a map organised by
	// warehouse warehouse as key, all Location entity belong to the warehouse as
	// data .
	protected LinkedHashMap<String, List<LocationEntity>> groupVacantLocationList = new LinkedHashMap<>();

	EBSLocationServer mpEBSLocationServer = Factory.create(EBSLocationServer.class);

	public MultiDeepVacantManager() throws DBException {
		super();
		// Build the vacant list
		populateVacantList();
		// Build the group vacant list
		populateGroupVacantLocationList();
	}

	/**
	 * This method is used to generate the vacant list
	 * 
	 * @throws DBException
	 */
	private void populateVacantList() throws DBException {
		setMapVacantLocation(mpEBSLocationServer.getFullVacantLocationEmptyData());
	}

	/**
	 * This method build the group vacant list
	 * 
	 * @throws DBException
	 */
	private void populateGroupVacantLocationList() throws DBException {
		// Get the vacant list
		if (getMapVacantLocation() != null && getMapVacantLocation().size() > 0) {
			List<LocationEntity> locationEntityList = null;
			Iterator<String> keyVacantLoc = getMapVacantLocation().keySet().iterator();
			// iterator the vacant list items
			while (keyVacantLoc.hasNext()) {
				LocationEntity locationEntity = getMapVacantLocation().get(keyVacantLoc.next());
				// get the warehouse data
				if (locationEntity != null && locationEntity.getWarehouse() != null
						&& locationEntity.getWarehouse().trim().length() > 0) {
					String groupKey = locationEntity.getWarehouse();
					// if warehouse is in the group vacant list
					if (getGroupVacantLocationList().containsKey(groupKey)) {
						// get the value (List of LocationEntity)
						locationEntityList = groupVacantLocationList.get(groupKey);
					} else {
						locationEntityList = new ArrayList<>();
					}
					// Add the current location to the list
					locationEntityList.add(locationEntity);
					groupVacantLocationList.put(groupKey, locationEntityList);
				}

			}
		}
	}

	public LinkedHashMap<String, LocationEntity> getMapVacantLocation() {
		return mapVacantLocation;
	}

	public void setMapVacantLocation(LinkedHashMap<String, LocationEntity> mapVacantLocation) {
		this.mapVacantLocation = mapVacantLocation;
	}

	public LinkedHashMap<String, List<LocationEntity>> getGroupVacantLocationList() {
		return groupVacantLocationList;
	}

	public void setGroupVacantLocationList(LinkedHashMap<String, List<LocationEntity>> groupVacantLocationList) {
		this.groupVacantLocationList = groupVacantLocationList;
	}

	/**
	 * This method get all the vacant location list for the given warehouse
	 * 
	 * @param warehouse
	 * @return
	 */
	public List<LocationEntity> getAllVacantLocationForWarehouse(String warehouse) {
		if (getGroupVacantLocationList() != null && !getGroupVacantLocationList().isEmpty()) {
			return getGroupVacantLocationList().get(warehouse);
		}
		return null;
	}

	/**
	 * this method gets all the vacant location list for the given warehouse and the
	 * bag size
	 * 
	 * @param warehouse
	 * @param OOGBag
	 * @return
	 */
	public List<LocationEntity> getAllVacantLocationForWhsBagSize(String warehouse, boolean OOGBag) {
		if (getGroupVacantLocationList() != null && !getGroupVacantLocationList().isEmpty()) {
			return getGroupVacantLocationList().get(warehouse).stream()
					.filter(locationEntity -> (locationEntity.isOOGBag() == OOGBag)).collect(Collectors.toList());
		}
		return null;
	}

	/**
	 * This method is used to get all the vcant location for the given bag size.
	 * Also this method brings the given warehouse to the top of the list
	 * 
	 * @param OOGBag
	 * @param priorityWarehouse
	 * @return
	 */
	public List<LocationEntity> getAllVacantLocationForBagSize(boolean OOGBag, String priorityWarehouse) {
		if (getMapVacantLocation() != null && !getMapVacantLocation().isEmpty()) {
			return getMapVacantLocation().values().stream()
					.filter(locationEntity -> (locationEntity.isOOGBag() == OOGBag))
					.sorted(new LocationEntityComparator().priortiseByWarehouse(priorityWarehouse))
					.collect(Collectors.toList());
		}
		return null;
	}

	/**
	 * This method return the best location id from the vacant list
	 * 
	 * @param OOGBag
	 * @param priorityWarehouse
	 * @return
	 */
	public String getVacantLocationIdForBagSize(boolean OOGBag, String priorityWarehouse) {
		String locationId = "";
		List<LocationEntity> locationList = getAllVacantLocationForBagSize(OOGBag, priorityWarehouse);
		if (locationList != null && locationList.size() > 0) {
			locationId = locationList.get(0).getAddress();
		}
		return locationId;
	}

	/**
	 * This method is used to find the vacant location from the map using bag size
	 * and warehouse
	 * 
	 * @param OOGBag
	 * @param priorityWarehouse
	 * @return
	 */
	public LocationEntity getVacantLocationForBagSize(boolean OOGBag, String priorityWarehouse) {
		LocationEntity locationEntity = new LocationEntity();
		// Get the vacant list for the filter by bag size and warehouse.
		List<LocationEntity> locationList = getAllVacantLocationForBagSize(OOGBag, priorityWarehouse);
		if (locationList != null && locationList.size() > 0) {
			locationEntity = locationList.get(0);
			mpLogger.logDebug("The location identified which is empty to store the bag:" + locationEntity.toString());
		}

		if (locationEntity != null && locationEntity.getAddress() != null
				&& locationEntity.getAddress().trim().length() <= 0) {
			mpLogger.logDebug(
					"Location not fetched for search bag type:isOOGBag:" + OOGBag + ",Warehouse:" + priorityWarehouse);
		}
		return locationEntity;
	}

}
