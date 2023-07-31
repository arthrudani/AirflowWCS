package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfigEnum.SCHEMA_ID;
import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfigEnum.TIMESLOT_ID;
import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfigEnum.TIMESLOT_NAME;
import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfigEnum.TIMESLOT_STARTTIME;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

public class TimeSlotConfigData extends AbstractSKDCData {

//	public static final String TSID = ID.getName();
	public static final String TIMESLOTID = TIMESLOT_ID.getName();
	public static final String SCHEMAID = SCHEMA_ID.getName();
	public static final String TIMESLOTNAME = TIMESLOT_NAME.getName();
	public static final String TIMESLOTSTARTTIME = TIMESLOT_STARTTIME.getName();

	private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<>();

	/*---------------------------------------------------------------------------
	Database fields for TimeSlot table.
	---------------------------------------------------------------------------*/
	private int timeSlotID;
	private int schemaID;
	private String name;
	private String startTime;

	public TimeSlotConfigData() {
		super();
		clear();
		initColumnMap(mpColumnMap, TimeSlotConfigEnum.class);
	}

	/**
	 * This helps in debugging when we want to print the whole structure.
	 */
	@Override
	public String toString() {
		StringBuffer myString = new StringBuffer(getClass().getCanonicalName()).append("\n");
		String[] vasKeys = mpColumnMap.keySet().toArray(new String[0]);
		Arrays.sort(vasKeys);
		for (String sKey : vasKeys) {
			ColumnObject vpVal = getColumnObject(sKey);
			String vsVal = vpVal == null ? null
					: vpVal.getColumnValue() == null ? null : vpVal.getColumnValue().toString();
			myString.append(" * ").append(sKey).append(" = ").append(vsVal).append(";\n");
		}
		return myString.toString();
	}

	/**
	 * Resets the data in this class to the default.
	 */
	public void clear() {
		super.clear(); // Pull in default behaviour.
	}

	/*---------------------------------------------------------------------------
	Column value get methods go here.
	---------------------------------------------------------------------------*/

	/**
	 * Fetches Timeslot ID
	 * 
	 * @return TimeslotID as int
	 */
	public int getTimeslotID() {
		return (timeSlotID);
	}

	/**
	 * Fetches SchemaID
	 * 
	 * @return SchemaID as int
	 */
	public int getSchemaID() {
		return (schemaID);
	}

	/**
	 * Fetches Name
	 * 
	 * @return Name as string
	 */
	public String getName() {
		return (name);
	}

	/**
	 * Fetches StartTime.
	 * 
	 * @return StartTime as string
	 */

	public String getStartTime() {
		return (startTime);
	}

	/*---------------------------------------------------------------------------
	 ******** Column Setting methods go here. ********
	---------------------------------------------------------------------------*/

	/**
	 * Sets TIMESLOT ID value.
	 */
	public void setTimeslotID(int timeslotID) {
		this.timeSlotID = timeslotID;
		addColumnObject(new ColumnObject(TIMESLOTID, timeslotID));
	}

	public void setTimeslotID(String timeslotID) {
		this.timeSlotID = Integer.parseInt(timeslotID);
		addColumnObject(new ColumnObject(TIMESLOTID, timeslotID));
	}

	/**
	 * Sets schemaID value.
	 */
	public void setSchemaID(int schemaID) {
		this.schemaID = schemaID;
		addColumnObject(new ColumnObject(SCHEMAID, schemaID));
	}

	public void setSchemaID(String schemaID) {
		this.schemaID = Integer.parseInt(schemaID);
		addColumnObject(new ColumnObject(SCHEMAID, schemaID));
	}

	/**
	 * Sets Name value.
	 */
	public void setName(String isName) {
		name = checkForNull(isName);
		addColumnObject(new ColumnObject(TIMESLOTNAME, isName));
	}

	/**
	 * Sets Carrier Phone value.
	 */
	public void setStartTime(String isStartTime) {
		startTime = checkForNull(isStartTime);
		addColumnObject(new ColumnObject(TIMESLOTSTARTTIME, isStartTime));
	}

	/**
	 * Required set field method. This method figures out what column was passed to
	 * it and sets the value. This allows us to have a generic method for all DB
	 * interfaces.
	 */
	@Override
	public int setField(String isColName, Object ipColValue) {
		TableEnum vpEnum = mpColumnMap.get(isColName);
		if (vpEnum == null) {
			return super.setField(isColName, ipColValue);
		}

		switch ((TimeSlotConfigEnum) vpEnum) {
		case TIMESLOT_ID:
			setTimeslotID((Integer) ipColValue);
			break;
		case SCHEMA_ID:
			setSchemaID((Integer) ipColValue);
			break;
		case TIMESLOT_NAME:
			setName((String) ipColValue);
			break;
		case TIMESLOT_STARTTIME:
			setStartTime((String) ipColValue);
			break;
		}
		return 0;
	}

	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		// TODO Auto-generated method stub
		return false;
	}

}
