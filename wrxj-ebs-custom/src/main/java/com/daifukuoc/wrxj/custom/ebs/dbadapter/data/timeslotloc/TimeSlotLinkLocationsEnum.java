package com.daifukuoc.wrxj.custom.ebs.dbadapter.data.timeslotloc;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

public enum TimeSlotLinkLocationsEnum implements TableEnum {

	TIMESLOT_ID("ITIMESLOTID"), 
	WAREHOUSE("SWAREHOUSE"), 
	LOCATION("SLOCATIONID");

	private String msColumnName;

	TimeSlotLinkLocationsEnum(String isColumnName) {
		msColumnName = isColumnName;
	}
	
	@Override
	public String getName() {
		return (msColumnName);
	}

}
