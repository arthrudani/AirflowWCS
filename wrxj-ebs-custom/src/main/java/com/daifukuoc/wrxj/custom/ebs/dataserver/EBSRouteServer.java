/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.dataserver;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSRoute;

/**
 * @author Administrator
 *
 */
public class EBSRouteServer extends StandardRouteServer {

	/**
	 * 
	 */

	private StandardStationServer mpStationServ = Factory.create(StandardStationServer.class);
	protected EBSRoute mpRoute = Factory.create(EBSRoute.class);

	public EBSRouteServer() {
		super();
	}

	public EBSRouteServer(String keyName) {
		super(keyName);
	}

	public EBSRouteServer(String keyName, DBObject dbo) {
		super(keyName, dbo);
	}

	/**
	 * Get a route that connects the source and destination locations
	 *
	 * @param isFromWarehouse
	 * @param isFromAddress
	 * @param isDestWarehouse
	 * @param isDestAddress
	 * @return <code>String</route> with a route name if one exists, null otherwise
	 * @throws DBException
	 */
	@Override
	public String getFromToRoute(String isFromWarehouse, String isFromAddress, String isDestWarehouse,
			String isDestAddress) throws DBException {
		StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class, "StandardRouteServer");

		// Get LocationData of from warehouse/address
		// - Address is an address in Location table like 001001001
		LocationData locationOfFromWarehouseAndAddress = vpLocServer.getLocationRecord(isFromWarehouse, isFromAddress);
        if (locationOfFromWarehouseAndAddress == null) {
            throw new DBException("Invalid From Location [" + isFromWarehouse + "-" + isFromAddress + "]!");
        }
        LocationData fromLocationInRoute = Factory.create(LocationData.class);
        // We put device id into address instead of original from address, 
        // so that Route.getRouteFromTo() can find the route from device to to station 
        fromLocationInRoute.setAddress(locationOfFromWarehouseAndAddress.getDeviceID());

        // Get LocationData of to warehouse/address
        // - Address is a station id like 6121
		LocationData toLocationInRoute = Factory.create(LocationData.class);
		StationData stationOfToLocationInRoute = mpStationServ.getStation(isDestAddress);
        if (stationOfToLocationInRoute == null) {
            throw new DBException("Invalid Destination Location [" + isDestWarehouse + "-" + isDestAddress + "]!");
        }
        // When location type is DBConstants.LCSTATION, Route.getRouteFromTo() uses device id
		toLocationInRoute.setDeviceID(mpStationServ.getStation(isDestAddress).getDeviceID());
		toLocationInRoute.setLocationType(DBConstants.LCSTATION);

		return mpRoute.getRouteFromTo(fromLocationInRoute, toLocationInRoute);
	}

	// DK:30294 - Send expected receipt response msg to host
	/**
	 * This method returns the from station for the given destination station
	 * Returns the top row
	 * 
	 * @param ipDestLocation
	 * @return
	 * @throws DBException
	 */
	public String getRouteFromTo(String ipDestLocation) throws DBException {
		return mpRoute.getRouteFromTo(ipDestLocation);
	}

}
