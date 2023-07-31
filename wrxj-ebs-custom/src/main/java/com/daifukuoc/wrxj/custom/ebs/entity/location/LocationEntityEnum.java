package com.daifukuoc.wrxj.custom.ebs.entity.location;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

public enum LocationEntityEnum implements TableEnum {
	ADDRESS("SADDRESS"), WAREHOUSE("SWAREHOUSE"), LOCATIONTYPE("ILOCATIONTYPE"), OCCUPIEDCOUNT("OCCUPIEDCOUNT");

	private String msColumnName;

	LocationEntityEnum(String isColumnName) {
		msColumnName = isColumnName;
	}

	@Override
	public String getName() {
		return (msColumnName);
	}

}
