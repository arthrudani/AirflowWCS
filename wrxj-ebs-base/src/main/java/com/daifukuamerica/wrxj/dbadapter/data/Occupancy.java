package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class Occupancy extends BaseDBInterface{

	protected OccupancyData mpOccupancyData;
	
	public Occupancy() {
		super("Occupancy");
		mpOccupancyData = Factory.create(OccupancyData.class);
	}

	/**
	 * Add an occupancy
	 *
	 * @param occupancy
	 * @throws DBException When anything goes wrong
	 */
	public void addOccupancy(OccupancyData occupancyData) throws DBException {
		addElement(occupancyData);
	}

}
