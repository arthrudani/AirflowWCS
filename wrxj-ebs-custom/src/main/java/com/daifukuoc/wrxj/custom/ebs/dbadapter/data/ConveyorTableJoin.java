package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;

/*
 * Implements all ACP related queries
 */
public class ConveyorTableJoin extends BaseDBInterface{

	public ConveyorTableJoin() {
        super();
    }
	 public String findLocationOnAnyLaneBy(ExpectedReceiptMessageData expectedReceiptMessageData,String sWarehouseID)throws DBException
	 {
		StringBuffer tmpString = new StringBuffer();
		tmpString.append("SELECT top(1) lc.sAddress, count(ld.sAddress) as LoadCount, ")
		.append(" lc.iLocationDepth as Capacity, ")
        .append("( lc.iLocationDepth - count(ld.sAddress)) as Available ")
        .append(" FROM LOCATION lc ").append(" inner join Load ld on lc.sAddress = ld.sAddress ")
        .append(" inner join LOADLINEITEM ldl on ld.sLoadID = ldl.sLoadID ")
        .append(" WHERE lc.iEmptyFlag <> ").append(DBConstants.FULL_LOCATION)
        .append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
        .append(" AND lc.iLocationType = ").append( ( expectedReceiptMessageData.getItemType().equals(String.valueOf(SACControlMessage.ITEM_TYPE.OVERSIZE))) ? DBConstants.LCOUTOFGAUGE :DBConstants.LCASRS ) ;
        if( sWarehouseID != null && !sWarehouseID.isBlank() && !sWarehouseID.isEmpty())
        {
        	tmpString.append(" AND lc.sWarehouse = '").append(sWarehouseID).append("'");	
        }
        tmpString.append(" group by lc.sAddress,lc.iLocationDepth ")
        .append(" having ( count(ld.sAddress) <  lc.iLocationDepth )")
		.append(" ORDER BY LoadCount DESC ");
	 	
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
		if( idList != null && idList.size() > 0)
		{
			String s = (String) idList.get(0).get(LocationData.ADDRESS_NAME);
			return s;
		}
			
		return null; 
	 }
	/**
	 * Finds a lane (location) which has closest release time 
	 * @param expectedReceiptMessageData
	 * @param configMinutes
	 * @param sWarehouseID
	 * @return
	 * @throws DBException
	 */
	public String findEmptyLocationWhichHasClosestReleaseWindowBy(ExpectedReceiptMessageData expectedReceiptMessageData,String sWarehouseID)throws DBException 
	 {
		Date expectedDate = ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getDefaultRetrievalDateTime());
		
		StringBuffer tmpString = new StringBuffer();
		tmpString.append("SELECT top(1) lc.sAddress, count(ld.sAddress) as LoadCount, ")
				.append(" lc.iLocationDepth as Capacity, ")
		        .append("( lc.iLocationDepth - count(ld.sAddress)) as Available ")
		        .append(" FROM LOCATION lc ").append(" inner join Load ld on lc.sAddress = ld.sAddress ")
		        .append(" inner join LOADLINEITEM ldl on ld.sLoadID = ldl.sLoadID ")
		        .append(" WHERE lc.iEmptyFlag <> ").append(DBConstants.FULL_LOCATION)
		        .append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
		        .append(" AND lc.iLocationType = ").append( ( expectedReceiptMessageData.getItemType().equals(String.valueOf(SACControlMessage.ITEM_TYPE.OVERSIZE))) ? DBConstants.LCOUTOFGAUGE :DBConstants.LCASRS ) ;
		        if( sWarehouseID != null && !sWarehouseID.isBlank() && !sWarehouseID.isEmpty())
		        {
		        	tmpString.append(" AND lc.sWarehouse = '").append(sWarehouseID).append("'");	
		        }
		        tmpString.append(" group by lc.sAddress,lc.iLocationDepth,lc.iSearchOrder ")
		        .append(" having ( count(ld.sAddress) <  lc.iLocationDepth AND ("
		        + DBHelper.convertDateToDBString(expectedDate)
		        		+"   >  min(ldl.dExpirationDate  ) ")
		        .append(" OR "+ DBHelper.convertDateToDBString(expectedDate)+"   <  min(ldl.dExpirationDate  ))) ")
		        .append(" order by lc.iSearchOrder, LoadCount");
		
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
		if( idList != null && idList.size() > 0)
		{
			String s = (String) idList.get(0).get(LocationData.ADDRESS_NAME);
			return s;
		}
		
		return null;
	 }
	/**
	 * finds the lane which is allocated to the same flight
	 * @param expectedReceiptMessageData
	 * @return
	 * @throws DBException
	 */
	 public String findEmptyLocationForTheSameFlight(ExpectedReceiptMessageData expectedReceiptMessageData,String sWarehouseID)throws DBException 
	 {
		StringBuffer tmpString = new StringBuffer();
		tmpString.append("SELECT top(1) lc.sAddress, count(ld.sAddress) as LoadCount, ")
				.append(" lc.iLocationDepth as Capacity, ")
		        .append("( lc.iLocationDepth - count(ld.sAddress)) as Available ")
		        .append(" FROM LOCATION lc ").append(" inner join Load ld on lc.sAddress = ld.sAddress ")
		        .append(" inner join LOADLINEITEM ldl on ld.sLoadID = ldl.sLoadID ")
		        .append(" WHERE lc.iEmptyFlag <> ").append(DBConstants.FULL_LOCATION)
		        .append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
		        .append(" AND lc.iLocationType = ").append( ( expectedReceiptMessageData.getItemType().equals(String.valueOf(SACControlMessage.ITEM_TYPE.OVERSIZE))) ? DBConstants.LCOUTOFGAUGE :DBConstants.LCASRS ) ;
		        if( sWarehouseID != null && !sWarehouseID.isBlank() && !sWarehouseID.isEmpty())
		        {
		        	tmpString.append(" AND lc.sWarehouse = '").append(sWarehouseID).append("'");	
		        }
		        tmpString.append(" AND  ldl.sLot = '").append(expectedReceiptMessageData.getLot()).append("' ")
		        //.append(" AND  ldl.dExpectedDate = '").append(expectedReceiptMessageData.getFlightScheduledDateTime()).append("' ") //TODO: Do I need this ?
		        	
		        .append(" AND lc.sAddress in ( ")// sub-query start
		        	.append(" SELECT  lc1.sAddress FROM LOCATION lc1 left join Load ld1 on lc1.sAddress = ld1.sAddress ")
		        	.append(" WHERE lc1.iEmptyFlag <> ").append(DBConstants.FULL_LOCATION)
				    .append(" AND lc1.iLocationStatus = ").append(DBConstants.LCAVAIL);
				    if( sWarehouseID != null && !sWarehouseID.isBlank() && !sWarehouseID.isEmpty())
			        {
			        	tmpString.append(" AND lc1.sWarehouse = '").append(sWarehouseID).append("'");	
			        }
				    tmpString.append(" GROUP BY lc1.sAddress,lc1.iLocationDepth,lc1.iSearchOrder ")
				    .append(" having(count(ld1.sAddress) <  lc1.iLocationDepth)) ")//end of the sub-query
		        .append(" GROUP BY lc.sAddress,lc.iLocationDepth,lc.iSearchOrder ")
		        .append(" order by lc.iSearchOrder ");
		
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
		if( idList != null && idList.size() > 0)
		{
			String s = (String) idList.get(0).get(LocationData.ADDRESS_NAME);
			return s;
		}
		
		return null;
	 }
	 /**
	  * find the empty location with : If one or more non-empty lanes have a Lane Release Window that overlaps with the bags Flight Release Window, 
	  * then the first one is selected. 
	  * @param expectedReceiptMessageData
	  * @return
	  * @throws DBException
	  */
	 public String findEmptyLocationInOverlappingReleaseWindowBy(ExpectedReceiptMessageData expectedReceiptMessageData,int configMinutes,String sWarehouseID)throws DBException 
	 {
		StringBuffer tmpString = new StringBuffer();
		
		Date expectedDate = ConversionUtil.convertDateStringToDate(expectedReceiptMessageData.getDefaultRetrievalDateTime());
		
		tmpString.append("SELECT top(1) lc.sAddress, count(ld.sAddress) as LoadCount, ")
				.append(" lc.iLocationDepth as Capacity, ")
		        .append("( lc.iLocationDepth - count(ld.sAddress)) as Available ")
		        .append(" FROM LOCATION lc ").append(" inner join Load ld on lc.sAddress = ld.sAddress ")
		        .append(" inner join LOADLINEITEM ldl on ld.sLoadID = ldl.sLoadID ")
		        .append(" WHERE lc.iEmptyFlag <> ").append(DBConstants.FULL_LOCATION)
		        .append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
		        .append(" AND lc.iLocationType = ").append( ( expectedReceiptMessageData.getItemType().equals(String.valueOf(SACControlMessage.ITEM_TYPE.OVERSIZE))) ? DBConstants.LCOUTOFGAUGE :DBConstants.LCASRS ) ;
		        if( sWarehouseID != null && !sWarehouseID.isBlank() && !sWarehouseID.isEmpty())
		        {
		        	tmpString.append(" AND lc.sWarehouse = '").append(sWarehouseID).append("'");	
		        }
		        tmpString.append(" group by lc.sAddress,lc.iLocationDepth,lc.iSearchOrder ")
		        .append(" having (count(ld.sAddress) <  lc.iLocationDepth AND ( "
		        		+ "Convert( datetime, dateadd(MINUTE, -"+configMinutes+", "+  DBHelper.convertDateToDBString(expectedDate) +" ))  >= CONVERT( datetime, dateadd(MINUTE ,-"+configMinutes+",  min(ldl.dExpirationDate) ) ) ")
		        .append(" AND  Convert( datetime, dateadd(MINUTE, -"+configMinutes+","+DBHelper.convertDateToDBString(expectedDate) +") ) <=  min(ldl.dExpirationDate)  )) ")
		        .append(" order by LoadCount asc,lc.iSearchOrder");
		
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
		if( idList != null && idList.size() > 0)
		{
			String s = (String) idList.get(0).get(LocationData.ADDRESS_NAME);
			return s;
		}
		
		return null;
	 }
	 /**
	  * Finds empty land
	  * @param expectedReceiptMessageData
	  * @param sWarehouseID
	  * @return
	  * @throws DBException
	  */
	 public String findEmptyLaneBy(ExpectedReceiptMessageData expectedReceiptMessageData,String sWarehouseID)throws DBException
	 {
		StringBuffer tmpString = new StringBuffer();
		tmpString.append("SELECT top(1) lc.sAddress FROM LOCATION lc ")	
		.append(" WHERE lc.iEmptyFlag = ").append(DBConstants.UNOCCUPIED) //Empty
        .append(" AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)//Available
        .append(" AND lc.sAddress not in (select ld.sAddress from Load ld) ")
        .append(" AND lc.iLocationType = ").append( ( expectedReceiptMessageData.getItemType().equals(String.valueOf(SACControlMessage.ITEM_TYPE.OVERSIZE))) ? DBConstants.LCOUTOFGAUGE :DBConstants.LCASRS ) ;
	 	if( sWarehouseID != null && !sWarehouseID.isBlank() && !sWarehouseID.isEmpty())
        {
        	tmpString.append(" AND lc.sWarehouse = '").append(sWarehouseID).append("'");	
        }
	 	tmpString.append(" order by lc.iSearchOrder");
		StringBuilder vpSql = new StringBuilder(tmpString);
		List<Map> idList = fetchRecords(vpSql.toString());
		if( idList != null && idList.size() > 0)
		{
			String s = (String) idList.get(0).get(LocationData.ADDRESS_NAME);
			return s;
		}
			
		return null; 
	 }

	 public String findWarehouseByFinalSortLocation(String sFindalSortLocation) throws DBException
	 {
		StringBuffer tmpString = new StringBuffer();
        tmpString.append("SELECT sWarehouse FROM WAREHOUSE_FINALSORTLOCATION ")
                .append(" WHERE sLocationID = '").append(sFindalSortLocation).append("'");

        StringBuilder vpSql = new StringBuilder(tmpString);
        List<Map> list = fetchRecords(vpSql.toString());
        if(!list.isEmpty()) {
            return DBHelper.getStringField(list.get(0), "sWarehouse");
        }
        return null;
	 }
	 public String getDefaultWarehouse() throws DBException
	 {
		StringBuffer tmpString = new StringBuffer();
        tmpString.append("select top(1) sWarehouse from WAREHOUSE_FINALSORTLOCATION");

        StringBuilder vpSql = new StringBuilder(tmpString);
        List<Map> list = fetchRecords(vpSql.toString());
        if(!list.isEmpty()) {
            return DBHelper.getStringField(list.get(0), "sWarehouse");
        }
        return null;
	 }
	 public String getMaxPositionByDevice(String deviceId,String address)throws DBException {
	 
		 StringBuffer tmpString = new StringBuffer();
	        tmpString.append("SELECT MAX(ld.sShelfPosition) sShelfPosition FROM ").append("Load ld ")
	                .append(" WHERE ld.sAddress = '").append(address).append("' ")
	                .append(" AND ld.sDeviceID = '").append(deviceId).append("' ");

	        StringBuilder vpSql = new StringBuilder(tmpString);
	        List<Map> list = fetchRecords(vpSql.toString());
	        if(!list.isEmpty()) {
	            return DBHelper.getStringField(list.get(0), LoadData.SHELFPOSITION_NAME);
	        }
	        
	        return null;
	        
	 }
	
	 public int getReleaseWindowPeriodInMin()
	 {
		 int iReleaseWinInMin = 120;//default
		 StringBuffer tmpString = new StringBuffer();
		 tmpString.append("select sParameterValue from [asrs].[SYSCONFIG] where sGroup='ConveyorWH' And sParameterName = 'ReleaseWindowPeriodInMin' ");
		 StringBuilder vpSql = new StringBuilder(tmpString);
		 List<Map> idList = null;
		 try {
			idList = fetchRecords(vpSql.toString());
		 } catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
		 if( idList != null && idList.size() > 0)
		 {
			String value = (String) idList.get(0).get(SysConfigData.PARAMETERVALUE_NAME);
			return ( value != null && !value.isBlank() && !value.isEmpty() )? Integer.valueOf(value):iReleaseWinInMin ;
		 }
			
		 return iReleaseWinInMin;
	 }
	 public boolean shouldStoreNonOverlappingBags()
	 {
		 boolean shouldStore = true;//default
		 StringBuffer tmpString = new StringBuffer();
		 tmpString.append("select sParameterValue from [asrs].[SYSCONFIG] where sGroup='ConveyorWH' And sParameterName = 'StoreNonOverlappingBags' ");
		 StringBuilder vpSql = new StringBuilder(tmpString);
		 List<Map> idList = null;
		 try {
			idList = fetchRecords(vpSql.toString());
		 } catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
		 if( idList != null && idList.size() > 0)
		 {
			String value = (String) idList.get(0).get(SysConfigData.PARAMETERVALUE_NAME);
			return ( value != null && !value.isBlank() && !value.isEmpty() )? Boolean.valueOf(value):shouldStore ;
		 }
			
		 return shouldStore;
	 }
	 public List<LoadLineItemData> getAllRetrievableLoadLineItemsByWarehouseID(String warehouseID) throws DBException {
	        StringBuilder queryBuf = new StringBuilder();
	        queryBuf.append(" SELECT ");
	        queryBuf.append(" lli.*, ld.sAddress,ld.sWarehouse");
	        queryBuf.append(" FROM LOADLINEITEM AS lli");
	        queryBuf.append(" INNER JOIN LOAD AS ld");
	        queryBuf.append("         ON ld.sLoadID = lli.sLoadID");
	        queryBuf.append("        AND ld.sWarehouse = '").append(warehouseID).append("'");
	        
	        List<Map> results = fetchRecords(queryBuf.toString());
	        if (results == null || results.isEmpty()) {
	            return new ArrayList<LoadLineItemData>();
	        }

	        List<LoadLineItemData> loadLineItemDataList = results.stream().map(row -> {
	            LoadLineItemData liToReturn = Factory.create(LoadLineItemData.class);
	            ColumnObject location = new ColumnObject("SADDRESS", row.get("SADDRESS"));
	            ColumnObject WH = new ColumnObject("SWAREHOUSE", row.get("SWAREHOUSE"));
	            row.remove("SADDRESS");
	            row.remove("SWAREHOUSE");
	            liToReturn.dataToSKDCData(row);
	            liToReturn.addColumnObject(location);
	            liToReturn.addColumnObject(WH);
	            return liToReturn;
	        }).collect(Collectors.toList());

	        return loadLineItemDataList;
	    }
	 public List<LoadLineItemData> getAllRetrievableLoadLineItemsForScheduledFlight(String lotId,
	            Date flightScheduledDateTime) throws DBException {
	        StringBuilder queryBuf = new StringBuilder();
	        queryBuf.append(" SELECT ");
	        queryBuf.append(" lli.*, ld.sAddress,ld.sWarehouse");
	        queryBuf.append(" FROM LOADLINEITEM AS lli");
	        queryBuf.append(" INNER JOIN LOAD AS ld");
	        queryBuf.append("         ON ld.sLoadID = lli.sLoadID");
	        queryBuf.append("      AND lli.dExpectedDate = ");
	        queryBuf.append(DBHelper.convertDateToDBString(flightScheduledDateTime));
	        queryBuf.append("        AND lli.sLot = '").append(lotId).append("'");
	        
	        List<Map> results = fetchRecords(queryBuf.toString());
	        if (results == null || results.isEmpty()) {
	            return new ArrayList<LoadLineItemData>();
	        }

	        List<LoadLineItemData> loadLineItemDataList = results.stream().map(row -> {
	            LoadLineItemData liToReturn = Factory.create(LoadLineItemData.class);
	            ColumnObject location = new ColumnObject("SADDRESS", row.get("SADDRESS"));
	            ColumnObject WH = new ColumnObject("SWAREHOUSE", row.get("SWAREHOUSE"));
	            row.remove("SADDRESS");
	            row.remove("SWAREHOUSE");
	            liToReturn.dataToSKDCData(row);
	            liToReturn.addColumnObject(location);
	            liToReturn.addColumnObject(WH);
	            return liToReturn;
	        }).collect(Collectors.toList());

	        return loadLineItemDataList;
	    }
	 public void updateLocationBy(String sAddress,int iEmptyStatus,String sWarehouse )
	 {
		 try {
			 	String sql ="UPDATE asrs.LOCATION set iEmptyFlag="+iEmptyStatus +"  WHERE sAddress = '"+sAddress+"' and sWarehouse ='"+sWarehouse+"'";
				this.execute(sql);
			} catch (Exception e) {
			     e.printStackTrace();
			}
	 }
	 public void deleteLoadByLoadId(String sLoadId) {
        try {
            this.execute("delete from LOAD  WHERE sLoadID = ? ", sLoadId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public void deleteLoadlineByLoadId(String sLoadId) {
        try {
            this.execute("delete from LOADLINEITEM  WHERE sLoadID = ? ", sLoadId);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public void deletePurchaseOrderLineByOrderId(String isOrderId) {
        try {
            this.execute("delete from PURCHASEORDERLINE  WHERE sOrderID = ? ", isOrderId);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public void deletePurchaseOrderHeaderByOrderId(String isOrderId) {
        try {
            this.execute("delete from PURCHASEORDERHEADER  WHERE sOrderID = ? ", isOrderId);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
