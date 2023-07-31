/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;

/**
 * @author Administrator
 *
 */
public class TimeSlotConfig extends BaseDBInterface {

	protected TimeSlotConfigData mpEBSTimeSlotData;

	public TimeSlotConfig() {
		super("TimeSlotConfig");
		mpEBSTimeSlotData = Factory.create(TimeSlotConfigData.class);
	}

	/**
	 * Sets Objects for garbage collection.
	 */
	@Override
	public void cleanUp() {
		super.cleanUp();
		mpEBSTimeSlotData = null;
	}

	// Get the Time slot by schema ID
	public List<Map> getTimeSlotBySchemaId(String schemaId) throws DBException {

		StringBuilder vpSql = new StringBuilder(" SELECT convert(char(5), sStartTime, 108) [sStartTime] ")
				.append(" FROM TIMESLOTCONFIG ").append(" WHERE [iSchemaID] ='").append(schemaId).append("' ");

		List<Map> vpList = fetchRecords(vpSql.toString());
		return vpList;
	}

	// Get the next Time slot ID
	public List<Map> getNextTimeSlotId() throws DBException {

		StringBuilder vpSql = new StringBuilder(" SELECT ISNULL(MAX(iTimeslotID) + 1, 1) FROM TIMESLOTCONFIG  ");
		return fetchRecords(vpSql.toString());
	}

	// Get the Time slot by schema ID
	public List<Map> getTimeSlotAndSchemaId(String schemaId, String startTime) throws DBException {

		StringBuilder vpSql = new StringBuilder(" SELECT IID, ITimeslotID ").append(" FROM TIMESLOTCONFIG ")
				.append(" WHERE [iSchemaID] ='").append(schemaId).append("' AND ").append(" [sStartTime] = '")
				.append(startTime).append("' ");

		List<Map> vpList = fetchRecords(vpSql.toString());
		return vpList;
	}

	// Insert the new Time slot
	public void addTimeSlot(TimeSlotConfigData mpEBSTimeSlotData) throws DBException {
		addElement(mpEBSTimeSlotData);
	}

	// Delete the Time slot
	public void deleteTimeSlot(TimeSlotConfigData mpEBSTimeSlotData) throws DBException {
		deleteElement(mpEBSTimeSlotData);
	}

	// Get the Time slot by startTime that fit with the provided time slot
	@SuppressWarnings("rawtypes")
	public List<Map> getTimeSlotbyStartTime(String startTime, String endTime) throws DBException {
		StringBuilder vpSql = new StringBuilder(" SELECT * FROM TIMESLOTCONFIG WHERE [sStartTime] BETWEEN ")
				.append(" '").append(startTime).append("' AND '").append(endTime).append("' ");
		return fetchRecords(vpSql.toString());
	}

	public List<Map> getTimeSlotbyStartTime(String startTime) throws DBException {

		mpEBSTimeSlotData.clear();
		mpEBSTimeSlotData.setKey(TimeSlotConfigData.TIMESLOTSTARTTIME, startTime, KeyObject.LESS_THAN_INCLUSIVE);
		mpEBSTimeSlotData.addOrderByColumn(TimeSlotConfigData.TIMESLOTSTARTTIME, true);

		return getAllElements(mpEBSTimeSlotData);
//		StringBuilder vpSql = new StringBuilder(" SELECT * FROM TIMESLOTCONFIG WHERE [sStartTime] = ").append(" '")
//				.append(startTime).append("' Order by [sStartTime] desc ");
	}

	/**
	 * This method gets the timeslot for the given time and fetchs the first row
	 * from the list
	 * 
	 * @param startTime
	 * @return
	 * @throws DBException
	 */
	public TimeSlotConfigData getFirstRowTimeSlotbyStartTime(String startTime) throws DBException {

		TimeSlotConfigData timeSlotConfigData = null;

		StringBuilder vpSql = new StringBuilder(" SELECT TOP 1 * FROM TIMESLOTCONFIG WHERE [sStartTime] <= ")
				.append(" '").append(startTime).append("' Order by [sStartTime] desc ");

		List<Map> timeSlotConfigMap = fetchRecords(vpSql.toString());

		for (Map timeSlotLocationData : timeSlotConfigMap) {
			timeSlotConfigData = new TimeSlotConfigData();
			timeSlotConfigData.setSchemaID(DBHelper.getStringField(timeSlotLocationData, TimeSlotConfigData.SCHEMAID));
			timeSlotConfigData
					.setStartTime(DBHelper.getStringField(timeSlotLocationData, TimeSlotConfigData.TIMESLOTSTARTTIME));
			timeSlotConfigData
					.setTimeslotID(DBHelper.getStringField(timeSlotLocationData, TimeSlotConfigData.TIMESLOTID));
			break;
		}

		return timeSlotConfigData;
	}

}
