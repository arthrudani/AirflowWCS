package com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * @author Administrator
 *
 */
public class TimeSlotLinkLocations extends BaseDBInterface {

	public static final String tableName = "TimeSlotLinkLocations";
	protected TimeSlotLinkLocationsData mpEBSTimeSlotLocationData;

	public TimeSlotLinkLocations() {
		super(tableName);
		mpEBSTimeSlotLocationData = Factory.create(TimeSlotLinkLocationsData.class);
	}

	/**
	 * Sets Objects for garbage collection.
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		mpEBSTimeSlotLocationData = null;
	}

	@SuppressWarnings("rawtypes")
	public List<Map> getLocationsAssociatedWithThisTimeSlot(int sTimeSlotId) throws DBException {
		mpEBSTimeSlotLocationData.clear();
		mpEBSTimeSlotLocationData.setKey(TimeSlotLinkLocationsData.TIMESLOTID, sTimeSlotId);
		return getAllElements(mpEBSTimeSlotLocationData);
	}

	// Additional method to fetch the data from WAREHOUSE_FINALSORTLOCATION table
	@SuppressWarnings("rawtypes")
	public String getWarehouseByFinalSortLoction(String finalSortLoction) throws DBException {
		String warehouse = "";
		StringBuilder vpSql = new StringBuilder(" SELECT * FROM [WAREHOUSE_FINALSORTLOCATION] WHERE [sLocationID] ='")
				.append(finalSortLoction).append("'");
		List<Map> vpList = fetchRecords(vpSql.toString());

		if (vpList.isEmpty())
			return warehouse;

		Map vpMap = vpList.get(0);
		warehouse = DBHelper.getStringField(vpMap, "SWAREHOUSE");
		return warehouse;
	}

	// DK:30075 - Find full empty location and link to the time slot
	/**
	 * This method add entry to the TimeSlotLinkLocation table. This method attachs
	 * the location to the time slot.
	 * 
	 * @param mpEBSTimeSlotData
	 * @throws DBException
	 */
	public void assiocateLocationToTimeSlot(TimeSlotLinkLocationsData mpEBSTimeSlotData) throws DBException {
		addElement(mpEBSTimeSlotData);
	}
}
