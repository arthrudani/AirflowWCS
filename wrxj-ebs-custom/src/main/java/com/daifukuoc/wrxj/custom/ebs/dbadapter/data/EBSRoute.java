/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Route;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * @author Administrator
 *
 */
public class EBSRoute extends Route {

	/**
	 * Maximum number of hops for a route from the source to the destination.
	 */
	private static final int MAX_HOPS = Application.getInt("MaxHopsOnRoute", 3);
	private RouteData mpRouteData;

	public EBSRoute() {
		mpRouteData = Factory.create(RouteData.class);
	}

	/**
	 * Get a route that exists for a given Location and Destination.
	 *
	 * @param ipFromLocation <code>LocationData</code> of from-location
	 * @param ipDestLocation <code>LocationData</code> of to-location Note : Over
	 *                       ride the original method due change of condition check
	 * @return <code>String</code> The route or <code>null</code> if none exist
	 */
	@Override
	public String getRouteFromTo(LocationData ipFromLocation, LocationData ipDestLocation) throws DBException {
		StringBuffer subquery1 = new StringBuffer();
		String vsStart;
		String vsEnd;

		/*
		 * Get a list of routes that have this destination
		 */
		if (ipDestLocation.getLocationType() == DBConstants.LCSTATION) {
			vsEnd = ipDestLocation.getAddress();
		} else {
			vsEnd = ipDestLocation.getDeviceID();
		}
		mpRouteData.clear();
		mpRouteData.setKey(RouteData.DESTID_NAME, vsEnd);
		mpRouteData.setKey(RouteData.ROUTEONOFF_NAME, DBConstants.ON);
		Route vpRoute = Factory.create(Route.class);

		String[] vasRoutes = vpRoute.getSingleColumnValues(RouteData.ROUTEID_NAME, true, mpRouteData,
				SKDCConstants.NO_PREPENDER);
		if (vasRoutes.length == 0) {
			// No routes have this destination
			return null;
		}

		for (int i = 0; i < vasRoutes.length; i++) {
			subquery1.append("\'" + vasRoutes[i] + "\'");
			if (i + 1 < vasRoutes.length) {
				subquery1.append(", ");
			}
		}
		vsStart = ipFromLocation.getAddress();
		StringBuilder vpSql = new StringBuilder("SELECT DISTINCT rt.sRouteID AS \"SROUTEID\" FROM")
				.append(" Route rt WHERE rt.sFromID=?").append("  AND rt.iRouteOnOff = ").append(DBConstants.ON)
				.append("  AND  rt.sRouteID IN (").append(subquery1).append(")");

		String[] vasPossibleRoutes = getList(vpSql.toString(), RouteData.ROUTEID_NAME, SKDCConstants.NO_PREPENDER,
				vsStart);
		if (vasPossibleRoutes.length == 0) {
			// No routes have this source and destination
			return null;
		}

		/*
		 * Ensure that the entire route from source to destination is active and
		 * actually connects
		 */
		for (String vsRoute : vasPossibleRoutes) {
			if (checkPath(vsRoute, vsStart, vsEnd, 1)) {
				return vsRoute;
			}
		}
		// Something didn't work out
		return null;
	}

	// DK:30294 - Send expected receipt response msg to host
	/**
	 * This method returns the destination station for the given from station
	 * Returns the top row
	 * 
	 * @param ipDestLocation
	 * @return
	 * @throws DBException
	 */
	public String getRouteFromTo(String ipFromLocation) throws DBException {
		String entranceId = "";
		mpRouteData.clear();
		mpRouteData.setKey(RouteData.FROMID_NAME, ipFromLocation);
		mpRouteData.setKey(RouteData.ROUTEONOFF_NAME, DBConstants.ON);
		Route vpRoute = Factory.create(Route.class);

		String[] fromStationArray = vpRoute.getSingleColumnValues(RouteData.DESTID_NAME, true, mpRouteData,
				SKDCConstants.NO_PREPENDER);
		if (fromStationArray != null && fromStationArray.length > 0) {
			// returns only the top row
			return fromStationArray[0];
		}

		return entranceId;
	}
}
