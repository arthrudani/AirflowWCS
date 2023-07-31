package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Zone extends BaseDBInterface
{
  private ZoneData mpZoneData;
  
  public Zone()
  {
    super("Zone");
    mpZoneData = Factory.create(ZoneData.class);
  }

  /*========================================================================*/
  /*  Add/Modify/Delete/Get                                                 */
  /*========================================================================*/

  /**
   * Add Zone
   * 
   * @param ipZoneData - ZoneData - the zone to add
   * @throws DBException
   */
  public void addZone(ZoneData ipZoneData) throws DBException
  {
    addElement(ipZoneData);
  }
  
  /**
   * Modify Zone
   * 
   * @param ipZoneData - ZoneData - the zone to modify
   * @throws DBException
   */
  public void modifyZone(ZoneData ipZoneData) throws DBException
  {
    mpZoneData.clear();
    mpZoneData.addKeyObject(new KeyObject(ZoneData.ZONE_NAME, ipZoneData.getZone()));
    mpZoneData.setDescription(ipZoneData.getDescription());
    modifyElement(mpZoneData);
  }
  
  /**
   * Determine whether or not a zone is safe to delete (ie. not used)
   * 
   * @param isZone
   * @return
   * @throws DBException
   */
  public boolean isSafeToDeleteZone(String isZone) throws DBException
  {
    /*
     * Check locations
     */
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"rowCount\" FROM LOCATION ")
             .append(" WHERE sZone=?");
    boolean vzLocationSafe = (getRecordCount(vpSql.toString(), "rowCount",
                                             isZone) == 0);

    /*
     * Check zone groups
     */
    vpSql.setLength(0);
    vpSql.append("SELECT COUNT(*) AS \"rowCount\" FROM ZONEGROUP ")
             .append(" WHERE sZone=?");
    boolean vzZoneGroupSafe = (getRecordCount(vpSql.toString(), "rowCount",
                                              isZone) == 0);
    
    return (vzLocationSafe && vzZoneGroupSafe);
  }

  /**
   * Delete Zone
   * 
   * @param isZone - String - the Zone to delete
   * @throws DBException
   */
  public void deleteZone(String isZone) throws DBException
  {
    mpZoneData.clear();
    mpZoneData.addKeyObject(new KeyObject(ZoneData.ZONE_NAME, isZone));
    deleteElement(mpZoneData);
  }

  /**
   * Get a Zone
   * 
   * @param isZone - String - the Zone to get
   * @throws DBException
   */
  public ZoneData getZone(String isZone) throws DBException
  {
    mpZoneData.clear();
    mpZoneData.addKeyObject(new KeyObject(ZoneData.ZONE_NAME, isZone));
    
    return getElement(mpZoneData, DBConstants.NOWRITELOCK);
  }

  /**
   * Get a list of zones
   * @param isZone - Search string
   * @return - List
   * @throws DBException
   */
  public List<Map> getZones(String isZone) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ZONE");
    if (isZone.length() > 0)
    {
      vpSql.append(" WHERE sZone LIKE '" + isZone + "%'");
    }
    vpSql.append(" ORDER BY sZone");
    
    return fetchRecords(vpSql.toString());
  }
  
  /**
   * Get a list of all zones
   * 
   * @param isPrepender - prepender for the list (null if none)
   * @return - List
   * @throws DBException
   */
  public List<String> getZoneChoiceList(String isPrepender) throws DBException
  {
    List<String> vpZoneList = new ArrayList<String>();
    if (isPrepender != null)
    {
      vpZoneList.add(isPrepender);
    }
    try
    {
      StringBuilder vpSql = new StringBuilder("SELECT " + ZoneData.ZONE_NAME)
               .append(" FROM " + getWriteTableName())
               .append(" ORDER BY " + ZoneData.ZONE_NAME);
      
      DBResultSet myDBResultSet = execute(vpSql.toString(), (Object[])null);
      Map row;
      while (myDBResultSet.hasNext())  // may be multiple rows
      {
        row = (Map) myDBResultSet.next();
        String nameStr = DBHelper.getStringField(row, ZoneData.ZONE_NAME);
        if (nameStr.trim().length() > 0)
        {
          vpZoneList.add(nameStr);
        }
      }
    }
    catch(DBException e)
    {
      e.printStackTrace(System.out);
      System.out.println("Error \"" + e + "\" Getting Zone Choice List");
      return(null);
    }
    return(vpZoneList);
  }
}
