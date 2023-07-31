package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;

public class EBSLoad extends Load {

	public EBSLoad() {
		super();
	}

	/**
	 * Method gets list of Retrieve pending loads that are in the rack. This list
	 * may contain duplicates if there are multiple moves for a given load.
	 *
	 * @param isStation   The station loads should be retrieved to.
	 * @param isScheduler The scheduler that should send the retrieve commands.
	 * @return <code>List</code> containing Maps of Load data columns +
	 *         Move.sRouteID as "MOVEROUTE"
	 */
	public List<Map> getRetrievePendingLoadsCombinedAisle(String isStation, String isScheduler) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT load.*, Move.sRouteID AS \"MOVEROUTE\"")
				.append(" FROM Load, Move, Route, Device, Station ").append(" WHERE Load.iLoadMoveStatus = ")
				.append(DBConstants.RETRIEVEPENDING).append(" AND Load.sParentLoad = Move.sParentLoad ")
				.append(" AND (Load.sDeviceID = Device.sDeviceID ").append(" OR LOAD.sDeviceID = DEVICE.sCommDevice)")
				.append(" AND Device.sSchedulerName = ? ").append(" AND Move.sRouteID = Route.sRouteID ")
				.append(" AND Route.iFromType = ").append(DBConstants.EQUIPMENT)
				.append(" AND Route.sFromID = Load.sDeviceID ").append(" AND Route.sDestID = ?")
				.append(" AND Route.iRouteOnOff = ").append(DBConstants.ON)
				.append(" AND Route.sDestID = Station.sStationName AND (Station.sDeviceID = Load.sDeviceID OR Station.sSecondaryDeviceID = Load.sDeviceID) ")
				.append(" ORDER BY iPriority, Move.dMoveDate");

		Object[] vapParams = { isScheduler, isStation };
		return fetchRecords(vpSql.toString(), vapParams);
	}

	/**
	 * Method gets list of Retrieve pending loads that are in the rack. This list
	 * may contain duplicates if there are multiple moves for a given load.
	 *
	 * @param isStation   The station loads should be retrieved to.
	 * @param isScheduler The scheduler that should send the retrieve commands.
	 * @return <code>List</code> containing Maps of Load data columns +
	 *         Move.sRouteID as "MOVEROUTE"
	 */
	public List<Map> getRetrievePendingLoads(String isStation, String isScheduler) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT load.*, Move.sRouteID AS \"MOVEROUTE\"")
				.append(" FROM Load, Move, Route, Device, Station ").append(" WHERE Load.iLoadMoveStatus = ")
				.append(DBConstants.RETRIEVEPENDING).append(" AND Load.sParentLoad = Move.sParentLoad ")
				.append(" AND (Load.sDeviceID = Device.sDeviceID ").append(" OR LOAD.sDeviceID = DEVICE.sCommDevice)")
				.append(" AND Device.sSchedulerName = ? ").append(" AND Move.sRouteID = Route.sRouteID ")
				.append(" AND Route.iFromType = ").append(DBConstants.EQUIPMENT)
				.append(" AND Route.sFromID = Load.sDeviceID ").append(" AND Route.sDestID = ?")
				.append(" AND Route.iRouteOnOff = ").append(DBConstants.ON)
				.append(" AND Route.sDestID = Station.sStationName AND Station.sDeviceID = Load.sDeviceID ")
				.append(" ORDER BY iPriority, Move.dMoveDate");

		Object[] vapParams = { isScheduler, isStation };
		return fetchRecords(vpSql.toString(), vapParams);
	}

	public String[] getOldIDPendingListByMins(int iSecsOld) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sLoadid FROM Load").append(
				" WHERE iLoadMoveStatus = 219 AND dMoveDate < dateadd( second, -" + iSecsOld + ", CURRENT_TIMESTAMP )");

		return getList(vpSql.toString(), LoadData.LOADID_NAME, SKDCConstants.NO_PREPENDER);
	}

	public String[] getOldArrivedListByMins(int iSecsOld) throws DBException {

		StringBuilder vpSql = new StringBuilder("SELECT * FROM Load")
				.append(" WHERE iLoadMoveStatus = 231 AND dMoveDate < dateadd( second, -" + iSecsOld
						+ ", CURRENT_TIMESTAMP ) ");

		return getList(vpSql.toString(), LoadData.LOADID_NAME, SKDCConstants.NO_PREPENDER);
	}

	/**
	 *
	 * @param warehouse
	 * @param address
	 * @param status
	 * @param withLock
	 * @return
	 * @throws DBException
	 */
	public List<LoadData> getLoadAtAddressList(String warehouse, String address, int status, int withLock)
			throws DBException {
		return (getOldestLoadDataList(warehouse, address, status, withLock));
	}

	/**
	 * Method gets list of loads that with Flight No. This list may contain
	 * duplicates if there are multiple sAddress for a given load.
	 *
	 * @param flightNo The Flight No (lot no) loads should be retrieved to.
	 * @return <code>List</code> containing Maps of Load data columns
	 */
	public List<Map> getLoadsByFlightNo(String flightNo) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT b.sLot, a.sAddress, a.sDeviceId ")
				.append(" FROM  LOAD a, LOADLINEITEM b WITH (nolock) ").append(" WHERE b.sLot IS NOT NULL ")
				.append(" AND b.sLot = ?").append(" GROUP BY b.sLot, a.sAddress, a.sDeviceId ");

		Object[] vapParams = { flightNo };
		return fetchRecords(vpSql.toString(), vapParams);
	}

	/**
	 * Method gets list of loads that with Flight No. This list may contain
	 * duplicates if there are multiple sAddress for a given load.
	 *
	 * @param flightNo The Flight No (lot no) loads should be retrieved to.
	 * @param noOfBags number of bag need to fetch loads should be retrieved to.
	 * @return <code>List</code> containing Maps of Load data columns
	 */
	public List<Map> getLoadsByFlightNo(String flightNo, int noOfBags) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT TOP ").append(noOfBags)
				.append(" b.sLot, a.sAddress, a.sDeviceId ").append(" FROM  LOAD a, LOADLINEITEM b  WITH (nolock) ")
				.append(" WHERE b.sLot IS NOT NULL ").append(" AND b.sLot = ?")
				.append(" GROUP BY b.sLot, a.sAddress, a.sDeviceId ");

		Object[] vapParams = { flightNo };
		return fetchRecords(vpSql.toString(), vapParams);
	}

	/**
	 * Method gets list of loads that has orders created already for the given
	 * address.
	 *
	 * @param #address is location Id (N41-UL1A)in the load table.
	 * @return <code>List</code> containing Maps of Load data columns
	 */
	public List<Map> getLoadsByAddressWithoutOrders(int iLoadMoveStatus, String address) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ld.* ").append(" FROM  LOAD ld WITH (nolock) ")
				.append(" INNER JOIN asrs.LoadLineItem li WITH (nolock) ON ld.[SLOADID] = li.[SLOADID] ")
				.append(" WHERE  ld.sLoadID NOT IN ")
				.append("  (SELECT ol.sLoadID FROM ORDERHEADER as oh INNER JOIN ORDERLINE as ol ON oh.sOrderId = ol.sOrderId) ")
				.append(" AND ld.iLoadMoveStatus = ").append(iLoadMoveStatus).append(" AND ld.sAddress = ")
				.append("'" + address + "'");

		return fetchRecords(vpSql.toString());
	}
	
	public List<Map> getLoadsByAddress(String address) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT ld.*,li.* ").append(" FROM  LOAD ld WITH (nolock) ")
				.append(" INNER JOIN asrs.LoadLineItem li WITH (nolock) ON ld.[SLOADID] = li.[SLOADID] ")
				.append(" WHERE ld.sAddress = ")
				.append("'" + address + "'");
		
		return fetchRecords(vpSql.toString());
	}
}
