package com.daifukuoc.wrxj.custom.ebs.plc.acp.route;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBException;
/**
 * RouteManger class for getting next address
 * @author MT
 *
 */
public interface RouteManager {

	/**
	 * This method is used while getting item arrived message to obtain the next destination address of the load.
	 * @param load
	 * @param currentAddress
	 * @return nextDestination address from load status and current address
	 * @throws RouteManagerFailureException if load data or current address is null
	 * @throws DBException 
	 */
	public String findNextDestination(LoadData load, String currentAddress) throws RouteManagerFailureException, DBException;
}
