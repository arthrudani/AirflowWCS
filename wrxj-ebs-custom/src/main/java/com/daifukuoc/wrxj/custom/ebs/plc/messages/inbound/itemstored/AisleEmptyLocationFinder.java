package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * Level Based Empty Location Finder Interface
 * @author MT
 *
 */
public interface AisleEmptyLocationFinder {
	public static final String NAME = "AisleEmptyLocationFinder";

	/**
	 * 
	 * @param loadData get the load data
	 * @return available address for that load
	 * @throws DBException 
	 */
	public String find(LoadData loadData) throws DBException;
}
