package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

public enum MoveCommandDataEnum implements TableEnum{
	
	DEVICEID("SDEVICEID"),
	LOADID("SLOADID"),
	FROM("SFROM"),
	TODEST("STODEST"),
	COMMAND("SCOMMAND"),
	STATUS("ICMDSTATUS"),
	MOVETYPE("ICMDMOVETYPE"),
	ORDERID("SORDERID"),
	GLOBALID("SGLOBALID"),
	ITEMID("SITEMID"),
	FLIGHTNUM("SFLIGHTNUM"),
	FLIGHTSTD("DFLIGHTSTD"),
	FINALSORTLOCATION("SFINALSORTLOCATIONID"),
	ORDERTYPE("ICMDORDERTYPE"),
	CREATEDDATE("DCREATEDDATE"),
	LASTMODIFYDATE("DLASTMODIFYDATE");
	
	private String msMessageName;
	
	MoveCommandDataEnum(String isMessageName)
	 {
	    msMessageName = isMessageName;
	 }
	 @Override
	 public String getName()
	 {
	   return(msMessageName);
	 }
}
