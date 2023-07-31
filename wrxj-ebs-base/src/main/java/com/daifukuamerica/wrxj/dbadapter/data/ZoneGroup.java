package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ZoneGroup extends BaseDBInterface
{
  protected ZoneGroupData mpZoneGroupData;
  
  public ZoneGroup()
  {
    super("ZoneGroup");
    mpZoneGroupData = Factory.create(ZoneGroupData.class);
  }

  /**
   * Add Zone Group
   * 
   * @param ipZoneGroupData - ZoneGroupData - the zone to add
   * @throws DBException
   */
  public void addZoneGroupMember(ZoneGroupData ipZoneGroupData) throws DBException
  {
    addElement(ipZoneGroupData);
  }
  
  /**
   * Modify Zone Group
   * 
   * @param ipZoneGroupData - ZoneGroupData - the zone to modify
   * @throws DBException
   */
  public void modifyZoneGroupMember(ZoneGroupData ipZoneGroupData) throws DBException
  {
    mpZoneGroupData.clear();
    mpZoneGroupData.setKey(ZoneGroupData.ZONEGROUP_NAME, ipZoneGroupData.getZoneGroup());
    mpZoneGroupData.setKey(ZoneGroupData.PRIORITY_NAME, ipZoneGroupData.getPriority());
    mpZoneGroupData.setZone(ipZoneGroupData.getZone());
    modifyElement(mpZoneGroupData);
  }

  /**
   * Delete Zone Group
   * 
   * @param isZoneGroup - String - the ZoneGroup to delete
   * @param inPriority
   * @throws DBException
   */
  public void deleteZoneGroupMember(String isZoneGroup, int inPriority) throws DBException
  {
    mpZoneGroupData.clear();
    mpZoneGroupData.addKeyObject(new KeyObject(ZoneGroupData.ZONEGROUP_NAME, isZoneGroup));
    mpZoneGroupData.addKeyObject(new KeyObject(ZoneGroupData.PRIORITY_NAME, inPriority));
    deleteElement(mpZoneGroupData);
  }
  
  /**
   * Delete Zone Group
   * 
   * @param isZoneGroup - String - the ZoneGroup to delete
   * @param inPriority
   * @throws DBException
   */
  public void deleteZone(String isZone) throws DBException
  {
    List<String> vpDeletedZGD = new ArrayList<String>();
    
    /*
     * Delete references to the zone
     */
    mpZoneGroupData.clear();
    mpZoneGroupData.setKey(ZoneGroupData.ZONE_NAME, isZone);
    List<Map> vpZGDList = getAllElements(mpZoneGroupData);
    for (Map m: vpZGDList)
    {
      mpZoneGroupData.dataToSKDCData(m);
      vpDeletedZGD.add(mpZoneGroupData.getZoneGroup());
      deleteZoneGroupMember(mpZoneGroupData.getZoneGroup(), mpZoneGroupData.getPriority());
    }
    
    /*
     * If the above deletions finish off a zone group, delete references to the 
     * group
     */
    for (String s: vpDeletedZGD)
    {
      if (!hasMembers(s))
      {
        deleteZoneGroupReferences(s);
      }
    }
  }
  
  private void deleteZoneGroupReferences(String isZoneGroup) throws DBException
  {
    /*
     * Clear Load references
     */
    try
    {
      StringBuilder vpSql = new StringBuilder("UPDATE LOAD SET ")
               .append(LoadData.RECOMMENDEDZONE_NAME).append("=NULL WHERE ")
               .append(LoadData.RECOMMENDEDZONE_NAME).append("=?");
      execute(vpSql.toString(), isZoneGroup);
    }
    catch (NoSuchElementException nsee) { /* This is okay */ }

    /*
     * Clear Item Master references
     */
    try
    {
      StringBuilder vpSql = new StringBuilder("UPDATE ITEMMASTER SET ")
               .append(ItemMasterData.RECOMMENDEDZONE_NAME).append("=NULL WHERE ")
               .append(ItemMasterData.RECOMMENDEDZONE_NAME).append("=?");
      execute(vpSql.toString(), isZoneGroup);
    }
    catch (NoSuchElementException nsee) { /* This is okay */ }
    
    /*
     * Clear Station references
     */
    try
    {
      StringBuilder vpSql = new StringBuilder("UPDATE STATION SET ")
               .append(StationData.RECOMMENDEDZONE_NAME).append("=NULL WHERE ")
               .append(StationData.RECOMMENDEDZONE_NAME).append("=?");
      execute(vpSql.toString(), isZoneGroup);
    }
    catch (NoSuchElementException nsee) { /* This is okay */ }
  }
  
  private boolean hasMembers(String isZoneGroup) throws DBException
  {
    mpZoneGroupData.clear();
    mpZoneGroupData.setKey(ZoneGroupData.ZONEGROUP_NAME, isZoneGroup);
    return (getAllElements(mpZoneGroupData).size() > 0);
  }
  
  /**
   * Get a zone group record
   * 
   * @param isZoneGroup
   * @param inPriority
   * @return
   * @throws DBException
   */
  public ZoneGroupData getZoneGroupMember(String isZoneGroup, int inPriority) throws DBException
  {
    mpZoneGroupData.clear();
    mpZoneGroupData.setKey(ZoneGroupData.ZONEGROUP_NAME, isZoneGroup);
    mpZoneGroupData.setKey(ZoneGroupData.PRIORITY_NAME, inPriority);
    
    return getElement(mpZoneGroupData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a list of all zones
   * @return - List
   * @throws DBException
   */
  public List<String> getRecommendedZones() throws DBException
  {
    String[] vasZones = getDistinctColumnValues(ZoneGroupData.ZONEGROUP_NAME,
                                                SKDCConstants.EMPTY_VALUE);
    List<String> vpReturn = new ArrayList<String>();
    for (String s : vasZones)
    {
      vpReturn.add(s);
    }
    return vpReturn;
  }

  /**
   * Get a list of zone group records
   * 
   * @param isZoneGroup
   * @return
   * @throws DBException
   */
  public List<Map> getZoneGroupList(String isZoneGroup) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ZONEGROUP ");
    if (isZoneGroup.length() > 0)
    {
      vpSql.append(" WHERE sZoneGroup LIKE '" + isZoneGroup + "%'");
    }
    vpSql.append(" ORDER BY sZoneGroup, iPriority");
    
    return fetchRecords(vpSql.toString());
  }

  /**
   * Get a list of zones mot assigned to a particular group
   * 
   * @param isZoneGroup
   * @return
   * @throws DBException
   */
  public String[] getZonesNotInGroup(String isZoneGroup) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT SZONE FROM ZONE WHERE SZONE NOT IN ")
             .append("(SELECT SZONE FROM ZONEGROUP WHERE SZONEGROUP=?)");
    
    return getList(vpSql.toString(), ZoneData.ZONE_NAME, 
                   SKDCConstants.NO_PREPENDER, isZoneGroup);
  }

  /**
   * Get the next zone group priority for a zone group
   * 
   * @param isZoneGroup
   * @return
   * @throws DBException
   */
  public int getNextZoneGroupPriority(String isZoneGroup) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT MAX(IPRIORITY) AS \"MAXPRIORITY\" FROM ZONEGROUP WHERE SZONEGROUP=?");
    
    int inMaxPriority = getIntegerColumn("MAXPRIORITY", vpSql.toString(), isZoneGroup);
    
    return inMaxPriority+1;
  }
  
  /**
   * Consistent method of describing a Zone Group member
   * 
   * @param isZoneGroup
   * @param inPriority
   * @return
   */
  public static String describeZoneGroupMember(String isZoneGroup, int inPriority)
  {
    return "Zone Group member " + isZoneGroup + ":" + inPriority;
  }

  /**
   * Consistent method of describing a Zone Group member
   * 
   * @param ipZGData
   * @return
   */
  public static String describeZoneGroupMember(ZoneGroupData ipZGData)
  {
    return "Zone Group member " + ipZGData.getZoneGroup() + ":" + ipZGData.getPriority();
  }
}
