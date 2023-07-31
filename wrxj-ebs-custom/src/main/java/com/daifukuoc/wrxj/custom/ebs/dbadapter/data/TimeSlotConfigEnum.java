package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

public enum TimeSlotConfigEnum implements TableEnum {


	TIMESLOT_ID("ITIMESLOTID"), 
	SCHEMA_ID("ISCHEMAID"), 
	TIMESLOT_NAME("SNAME"), 
	TIMESLOT_STARTTIME("SSTARTTIME");

	private String msColumnName;

	TimeSlotConfigEnum(String isColumnName) {
		msColumnName = isColumnName;
	}

	@Override
	public String getName() {
		return (msColumnName);
	}

}
