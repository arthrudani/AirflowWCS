package com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc;

import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc.TimeSlotLinkLocationsEnum.LOCATION;
import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc.TimeSlotLinkLocationsEnum.TIMESLOT_ID;
import static com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc.TimeSlotLinkLocationsEnum.WAREHOUSE;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

public class TimeSlotLinkLocationsData extends AbstractSKDCData {

	public static final String TIMESLOTID = TIMESLOT_ID.getName();
	public static final String SWAREHOUSE = WAREHOUSE.getName();
	public static final String SLOCATION = LOCATION.getName();

	private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<>();

	/*---------------------------------------------------------------------------
	Database fields for TimeSlot table.
	---------------------------------------------------------------------------*/
	private int iTimeSlotID;
	private String sWarehouse;
	private String sLocation;

	public TimeSlotLinkLocationsData() {
		super();
		clear();
		initColumnMap(mpColumnMap, TimeSlotLinkLocationsEnum.class);
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
	public String getTimeslotID() {
		return String.valueOf(iTimeSlotID);
	}

	/**
	 * Fetches SchemaID
	 * 
	 * @return SchemaID as int
	 */
	public String getWarehouse() {
		return (sWarehouse);
	}

	/**
	 * Fetches Name
	 * 
	 * @return Name as string
	 */
	public String getLocation() {
		return (sLocation);
	}

	/*---------------------------------------------------------------------------
	 ******** Column Setting methods go here. ********
	---------------------------------------------------------------------------*/

	/**
	 * Sets TIMESLOT ID value.
	 */
	public void setiTimeSlotID(int iTimeSlotID) {
		this.iTimeSlotID = iTimeSlotID;
		addColumnObject(new ColumnObject(TIMESLOTID, this.iTimeSlotID));
	}

	/**
	 * Sets schemaID value.
	 */
	public void setWarehouse(String inWarehouse) {
		sWarehouse = checkForNull(inWarehouse);
		addColumnObject(new ColumnObject(SWAREHOUSE, sWarehouse));
	}

	/**
	 * Sets Name value.
	 */
	public void setLocation(String inLocation) {
		sLocation = checkForNull(inLocation);
		addColumnObject(new ColumnObject(SLOCATION, sLocation));
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

		switch ((TimeSlotLinkLocationsEnum) vpEnum) {
		case TIMESLOT_ID:
			setiTimeSlotID((Integer) ipColValue);
			break;
		case LOCATION:
			setLocation((String) ipColValue);
			break;
		case WAREHOUSE:
			setWarehouse((String) ipColValue);
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
