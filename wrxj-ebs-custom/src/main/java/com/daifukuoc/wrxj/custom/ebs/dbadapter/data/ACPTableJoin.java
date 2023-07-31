package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerConstants;

/*
 * Implements all ACP related queries
 */
public class ACPTableJoin extends BaseDBInterface{

	public ACPTableJoin() {
        super();
    }
	/* CMD Allocations */
	
	/*
	 * Returns counts when status = Ready and count for status = processing
	 * Output stations type =03 
	 */	
	public List<Map> getCMDCountForOutboundStations(String deviceID)throws DBException
	{
		return getCMDCountFor( deviceID, RouteManagerConstants.LCLIFTER_AISLE_DEPOSIT_STATION_TYPE,null); 
	}
	
	public List<Map> getCMDCountForInputASRSLocations(String deviceID)throws DBException
	{
		return getCMDCountFor( deviceID, RouteManagerConstants.LCSTORAGE_LOCATION_TYPE,null); 
	}
	public List<Map> getCMDCountForInputTransferStations(String deviceID)throws DBException
	{
		return getCMDCountFor( deviceID, RouteManagerConstants.LCSHUTTLE_PICKUP_STATION_TYPE,RouteManagerConstants.LCSHUTTLE_BIDIRECTIONAL_STATION_TYPE); 
	}

	public List<Map> getCMDForOutboundStations(String deviceID, Integer iNumOfCmd)throws DBException
	{		
		StringBuffer tmpString = new StringBuffer();
		tmpString.append(" SELECT top("+iNumOfCmd+") mc.* ")	
		.append(" FROM asrs.MoveCOMMAND mc ")
		.append(" WHERE mc.sDeviceID ='").append(deviceID).append("'" )
		.append(" AND mc.sToDest like '").append(RouteManagerConstants.LCLIFTER_AISLE_DEPOSIT_STATION_TYPE).append("%'" )
		.append(" Order by mc.dCreatedDate");
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
	    return (idList);
	}
	
	/*
	 * Returns counts when status = Ready and count for status = processing
	 * Lift Transfer Stations type =05 and 07
	 */	
	public List<Map> getCMDCountForLiftPickupTransferStations(String deviceID)throws DBException
	{
		return getCMDCountFor( deviceID, RouteManagerConstants.LCLIFTER_LAYER_PICKUP_STATION_TYPE,RouteManagerConstants.LCLIFTER_LAYER_BIDIRECTIONAL_STATION_TYPE);
	}

	
	private List<Map> getCMDCountFor(String deviceID,String stationType,String secondStationType)throws DBException
	{
		
		StringBuffer tmpString = new StringBuffer();
		tmpString.append(" SELECT ")
		.append(" sum(case when iCmdStatus = ").append(DBConstants.CMD_READY).append(" then 1 else 0 end ) as CmdReady, ")
		//0 = Unknown, 1 = Ready, 2 = Commanded, 3 = Processing, 4 = Completed, 5 = Deleted, 6 = Error 
		.append(" sum(case when iCmdStatus = ").append(DBConstants.CMD_COMMANDED).append( " or iCmdStatus = ").append(DBConstants.CMD_PROCCESSING).append(" then 1 else 0 end ) as CmdWorking, " )			
		.append(" sToDest as DESTSTATION ")
		.append(" FROM asrs.MoveCOMMAND ")
		.append(" WHERE sDeviceID ='").append(deviceID).append("'" )
		.append(" AND sToDest like '").append(stationType).append("%'" );
		if ( secondStationType != null )
		{
			tmpString.append(" OR sToDest like '").append(secondStationType).append("%'" );
		}
		
		tmpString.append(" GROUP BY sToDest");
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
	    return (idList);
	}
	
	public List<Map> getCMDFor(String deviceID, Integer iNumOfCmd,String destinationID)throws DBException
	{
		StringBuffer tmpString = new StringBuffer();
		tmpString.append(" SELECT top("+iNumOfCmd+") mc.* ")	
		.append(" FROM asrs.MoveCOMMAND mc ")
		.append(" WHERE mc.sDeviceID ='").append(deviceID).append("'" )
		.append(" AND mc.sToDest = '").append(destinationID).append("'" )
		.append(" AND mc.iCmdStatus = ").append(DBConstants.CMD_READY) //status = ready (1)
		.append(" Order by mc.dCreatedDate");
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
	    return (idList);
	}
	
	public void updateMoveCmdStatusById(String sDeviceId,String sLoadId,String from,String toDest,int iStatus) 
	 {
		try {
			this.execute("update MOVECOMMAND set iCMDStatus = ? WHERE sDeviceID = ? and sLoadId = ? and sFrom = ? and sToDest =?   ",iStatus, sDeviceId,sLoadId,from,toDest);
		} catch (Exception e) {
			String er = e.getMessage();
		     e.printStackTrace();
		}
	 }
	
	 public void archiveCMDsWithStatusDELETED() 
	 {
		 //TODO: we need to archive but delete them for now ....
		try {
			this.execute("delete from asrs.MOVECOMMAND where iCmdStatus = " +DBConstants.CMD_DELETED);
		} catch (Exception e) {
			String er = e.getMessage();
		     e.printStackTrace();
		}
	 }
	 
	 
	 /* Empty Location Search */
	 
	 /**
	     * Return the map of number of load(already stored or to be stored) of the lot per device id
	     * 
	     * @param warehouse warehouse name, for example, "EBS"
	     * @param lotId lot id, for example, flight number like "ANZ 101"
	     * @param flightScheduledDateTime flight scheduled date time
	     * @return the map of number of load of the lot per device id
	     * @throws DBException When anything goes wrong with the join query
	     */
	    public Map<String, Integer> getNumberOfLoadPerDeviceId(String warehouse, String lotId, Date flightScheduledDateTime)
	            throws DBException {
	        Map<String, Integer> resultMap = new LinkedHashMap<String, Integer>();

	        // Get the device id list
	        StringBuilder strBuilder1 = new StringBuilder()
	                .append("SELECT sDeviceID ")
	                .append("  FROM DEVICE ")
	                .append(" WHERE sWarehouse = '").append(warehouse).append("' ")
	                .append("   AND iOperationalStatus != ")
	                .append(DBConstants.INOP).append(" ORDER BY iAisleGroup ASC ");
	        List<Map> list1 = fetchRecords(strBuilder1.toString());

	        for (Map map : list1) {
	            String deviceIdName = DBHelper.getStringField(map, LoadData.DEVICEID_NAME);
	            resultMap.put(deviceIdName, 0);
	        }

	        // Count the number of loads
	        StringBuilder strBuilder2 = new StringBuilder()
	                .append("     SELECT ld.sDeviceID, count(*) AS loadCount")
	                .append("       FROM LOADLINEITEM AS lli")
	                .append(" INNER JOIN LOAD AS ld")
	                .append("         ON ld.sLoadID = lli.sLoadID")
	                .append("        AND ld.sWarehouse = '").append(warehouse).append("'")
	                .append(" INNER JOIN LOCATION AS lc")
	                .append("         ON lc.sAddress = ld.sAddress")
	                .append("        AND lc.sDeviceID = ld.sDeviceID")
	                .append("        AND lc.sWarehouse = ld.sWarehouse")
	                .append("        AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
	                .append("        AND (lc.iEmptyFlag = ").append(DBConstants.OCCUPIED).append(" OR lc.iEmptyFlag = ").append(DBConstants.LCRESERVED).append(") ")
	                .append("        AND LEFT(lc.sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'")
	                .append(" INNER JOIN DEVICE AS dv")
	                .append("         ON dv.sDeviceID = ld.sDeviceID")
	                .append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP)
	                .append("      WHERE lli.sLot = '").append(lotId).append("'")
	                .append("        AND lli.dExpectedDate = ").append(DBHelper.convertDateToDBString(flightScheduledDateTime))
	                .append("   GROUP BY ld.sDeviceID")
	                .append("   ORDER BY count(*) ASC");
	        List<Map> list2 = fetchRecords(strBuilder2.toString());

	        // Apply the count result to the device list
	        for (Map map : list2) {
	            String deviceIdName = DBHelper.getStringField(map, LoadData.DEVICEID_NAME);
	            Integer loadCount = DBHelper.getIntegerField(map, "loadCount");
	            resultMap.put(deviceIdName, loadCount);
	        }

	        return resultMap;
	    }
	    public Map<String, Integer> getNumberOfLoadPerLevel(String warehouse, String lotId, String deviceId)
	            throws DBException {
	        Map<String, Integer> resultMap = new LinkedHashMap<String, Integer>();
	        
	        StringBuilder strBuilder2 = new StringBuilder()
	                .append(" SELECT SUBSTRING(lc.sAddress, 9, 2) AS Level, COUNT(ld.sAddress) AS loadCount")
	                .append(" FROM LOCATION AS lc ")
	                .append(" LEFT JOIN LOAD AS ld ")
	                .append(" ON lc.sAddress = ld.sAddress ")
	                .append(" AND lc.sWarehouse = ld.sWarehouse ")
	                .append(" AND lc.sDeviceID = ld.sDeviceID ")
	                .append(" WHERE lc.sWarehouse = '").append(warehouse).append("'")
	                .append(" AND lc.sDeviceID = '").append(deviceId).append("'")
	                .append(" AND LEFT(lc.sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'")//Location type 20
	                .append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
	                .append("   GROUP BY SUBSTRING(lc.sAddress, 9, 2) ")
	                .append("   ORDER BY loadCount ");
	              
	        List<Map> loadPerLevelList = fetchRecords(strBuilder2.toString());
	        
	        for (Map load : loadPerLevelList) {
	            String levelName = DBHelper.getStringField(load, "level");
	            Integer loadCount = DBHelper.getIntegerField(load, "loadCount");
	            resultMap.put(levelName, loadCount);
	        }

	        return resultMap;
	    }
}
