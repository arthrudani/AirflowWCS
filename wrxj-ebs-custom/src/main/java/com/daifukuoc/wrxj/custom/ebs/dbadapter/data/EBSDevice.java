package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class EBSDevice extends Device
{
	public EBSDevice()
	{
	    super();
	}

	
	
	/**
	   * Change the Device Token.
	   *
	   * @param inAisleGroup - The aisle group for the device
	   * @return String - The device ID that is next
	   * @throws DBException
	   */
	  public String getAndUpdateDeviceToken(String isWarhse ) throws DBException
	  {
	    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(getWriteTableName())
	             .append(" WHERE ").append(DeviceData.WAREHOUSE_NAME)
	             .append("=? AND ").append(DeviceData.DEVICETOKEN_NAME)
	             .append("=").append(DBConstants.TRUE)
	             .append(" AND ").append(DeviceData.PHYSICALSTATUS_NAME)
	             .append("=").append(DBConstants.ONLINE)
	             .append(" AND ").append(DeviceData.OPERATIONALSTATUS_NAME)
	             .append("=").append(DBConstants.APPONLINE)
	             .append(" AND ").append(DeviceData.DEVICETYPE_NAME)
	             .append("=").append(DBConstants.SRC5);
	    List<Map> vpResults = fetchRecords(vpSql.toString(), isWarhse);
	    if (vpResults.size() == 0)
	    {
	      /*
	       * Somehow, there is NO device token.  Pick a device at random to restart.
	       */
	    	
	      getLogger().logDebug("WARNING: No ONLINE devices with device token set for warehouse " 
	          + isWarhse);
	      vpSql.setLength(0);
	      vpSql.append("SELECT * FROM ").append(getWriteTableName())
	               .append(" WHERE ").append(DeviceData.WAREHOUSE_NAME)
	               .append("=? AND ").append(DeviceData.DEVICETOKEN_NAME)
	               .append("=").append(DBConstants.FALSE).append(" AND ")
	               .append(DeviceData.NEXTDEVICE_NAME)
	               .append(" is not null")
		             .append(" AND ").append(DeviceData.PHYSICALSTATUS_NAME)
		             .append("=").append(DBConstants.ONLINE)
		             .append(" AND ").append(DeviceData.OPERATIONALSTATUS_NAME)
		             .append("=").append(DBConstants.APPONLINE)
		             .append(" AND ").append(DeviceData.DEVICETYPE_NAME)
		             .append("=").append(DBConstants.SRC5);  // The "not null" portion is to exclude AGC devices 
	      vpResults = fetchRecords(vpSql.toString(), isWarhse);
	      
	      /*
	       * There aren't any devices configured properly.  This is bad, and beyond
	       * the scope of automatic recovery.
	       */
	      if (vpResults.size() == 0)
	      {
	    	  getLogger().logDebug("WARNING: No ONLINE devices with device token set for warehouse " 
	    	          + isWarhse);
	    	  return "";
	      }
	    }
	    else if (vpResults.size() > 1)
	    {
	      /*
	       * Somehow, more than one device had the token set.  Pick one at random.
	       */
	      getLogger().logDebug("WARNING: Multiple devices with device token set for Warehouse " 
	          + isWarhse);
	    }
	    mpDevData.dataToSKDCData(vpResults.get(0));
	    String currentDevice = mpDevData.getDeviceID();
	    String nextDevice = mpDevData.getNextDevice();
	    setDeviceToken(currentDevice, DBConstants.FALSE);
	    setDeviceToken(nextDevice, DBConstants.TRUE);
	    
	    return currentDevice;
	  }
	  
	  
	  /**
	    * Method to return a the Secondary DeviceID for an  aisle
	    *
	    * @throws DBException for DB access errors.
	    */
	   public String getSecondaryDeviceID( String isDeviceID )
	       throws DBException
	   {
	     StringBuilder vpSql = new StringBuilder("SELECT sSecondaryDeviceID FROM STATION  ")
	                .append( " WHERE iStationType = ").append( DBConstants.OUTPUT ).append( " " )
	                .append( " AND sDeviceID = '").append( isDeviceID ).append( "' " );

	      String vsSecondaryDeviceID = "";
	      List<Map> vpList = fetchRecords(vpSql.toString());
	      if (!vpList.isEmpty())
	      {
	    	  vsSecondaryDeviceID = DBHelper.getStringField(vpList.get(0), EBSStationData.SECONDARYDEVICEID_NAME);
	      }

	      return vsSecondaryDeviceID;
	   }
	   
	   /**
	    * Method to return a the location seequence method
	    *
	    * @throws DBException for DB access errors.
	    */
	   public int getLocSeqMethodForDeviceID( String isDeviceID )
	       throws DBException
	   {
	                                            // Clear out SQL String buffer.
	         StringBuilder vpSql = new StringBuilder("SELECT iLocSeqMethod FROM Device WHERE sDeviceID='")
	                  .append(isDeviceID).append("'");
	         return getIntegerColumn(EBSDeviceData.LOC_SEQ_METHOD_NAME, vpSql.toString());
	       }
}
