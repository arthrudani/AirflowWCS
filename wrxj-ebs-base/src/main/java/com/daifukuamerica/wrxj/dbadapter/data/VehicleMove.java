package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 * Class to handle Vehicle table operations.  <b>Note:</b> This table exists
 * primarily for portability of persistent data between Warehouse Rx and some
 * other system.
 * 
 * @author A.D.
 * @since  13-May-2009
 */
public class VehicleMove extends BaseDBInterface
{
 /** The name of the Inbound AGV sequencer. */
  public static final String INBOUND_SEQUENCER = "AGVInboundSeq";
 /** The name of the Outbound AGV sequencer. */
  public static final String OUTBOUND_SEQUENCER = "AGVOutboundSeq";
 /** The name of the HeartBeat sequencer. */
  public static final String HEARTBEAT_SEQUENCER = "HeartBeatSeq";
  private VehicleMoveData mpVHData;

  public VehicleMove()
  {
    super("VehicleMove");
    mpVHData = Factory.create(VehicleMoveData.class);
  }

 /**
  * Retrieves one column value from the Device table.
  * @param isLoadID the unique key to use in the search.
  * @param isColumnName the name of the column whose value is returned.
  * @return value of column specified by isColumnName as an <code>Object</code>.
  *         The caller is assumed to know what data type is actually in 
  *         <code>Object</code>.  <i>A</i> <code>null</code> <i>object is 
  *         returned for no matching data</i>
  * @throws DBException when database access errors occur.
  */
  public Object getSingleColumnValue(String isLoadID, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName).append(" FROM VehicleMove WHERE ")
               .append("sLoadID = '").append(isLoadID).append("'");
    List<Map> vpData = fetchRecords(vpSql.toString());

    return((!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null);
  }

 /**
  * Method to get the oldest Load at an AGV station.
  * @param inMoveStatus
  * @return {@code null} if no records found, else reference to
  *         {@link VehicleMoveData VehicleMoveData}
  * @throws DBException if there is a DB access error.
  */
  public VehicleMoveData getOldestStationaryLoad(int inMoveStatus) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM Vehicle WHERE ")
               .append("dStatusChangeTime IN (")
               .append("SELECT MIN(dStatusChangeTime) FROM VehicleMove WHERE ")
               .append("iAGVLoadStatus = ").append(inMoveStatus).append(")");

    List vpList = fetchRecords(vpSql.toString());
    if (vpList.isEmpty())
    {
      return(null);
    }
    mpVHData.clear();
    mpVHData.dataToSKDCData((Map)vpList.get(0));
    
    return(mpVHData.clone());
  }

 /**
  * Set flag to notify host when this Vehicle Move is complete. This method
  * <b>must</b> be called from within a transaction.
  * 
  * @param isLoadID The load ID. of the vehicle move.
  * @param izNotifyHost {@code true} means host will be notified when
  *        this vehicle move is complete. The default is {@code false}.
  * @throws DBException if there is a database update error.
  */
  public void setHostNotifyFlag(String isLoadID, boolean izNotifyHost)
         throws DBException
  {
    mpVHData.setKey(VehicleMoveData.LOADID_NAME, isLoadID);
    mpVHData.setNotifyHost((izNotifyHost) ? DBConstants.YES : DBConstants.NO);
    modifyElement(mpVHData);
  }

 /**
  *  Sets Objects for garbage collection.
  */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpVHData = null;
  }
}
