package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

public enum OccupancyDataEnum implements TableEnum{
	
	LASTMOVEMENTTIME("DLASTMOVEMENTTIME"),
	AVAILABLECOUNT("IAVAILABLECOUNT"),
	OCCUPIEDCOUNT("IOCCUPIEDCOUNT"),
	UNAVAILABLECOUNT("IUNAVAILABLECOUNT"),
	STDEMPTYTRAYCOUNT("ISTDEMPTYTRAYCOUNT"),
	OOGEMPTYTRAYCOUNT("IOOGEMPTYTRAYCOUNT"),
	STDBAGONTRAYCOUNT("ISTDBAGONTRAYCOUNT"),
	OOGBAGONTRAYCOUNT("IOOGBAGONTRAYCOUNT"),
	STDTRAYSTACKCOUNT("ISTDTRAYSTACKCOUNT"),
	OOGTRAYSTACKCOUNT("IOOGTRAYSTACKCOUNT"),
	OTHERCONTAINERTYPECOUNT("IOTHERCONTAINERTYPECOUNT");
	
	private String msMessageName;
    OccupancyDataEnum(String isMessageName)
	 {
	    msMessageName = isMessageName;
	 }
    
	@Override
	public String getName() {
		 return(msMessageName);
	}

}
