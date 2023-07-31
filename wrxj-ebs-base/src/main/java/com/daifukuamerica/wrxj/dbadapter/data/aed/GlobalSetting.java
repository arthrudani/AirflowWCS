/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2015 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data.aed;

import com.daifukuamerica.wrxj.dbadapter.WynsoftDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.wynright.wrxj.app.Wynsoft;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the AES_SYS_GLOBAL_SETTINGS table
 * 
 * @author mandrus
 */
public class GlobalSetting extends WynsoftDBInterface
{
  private static final Integer GS_PRODUCT_ID = Wynsoft.getProductId();
  private static final Integer GS_INSTANCE_ID = Wynsoft.getInstanceId();

  /*====================================================================*/
  /* Match the .NET global settings list                                */
  /*====================================================================*/
  private static final String GS_LIST_SQL_SELECT = "SELECT * FROM ";
  private static final String GS_LIST_SQL_WHERE  = " WHERE ((InstanceId = ?) OR (InstanceId is NULL AND ProductId = ?) OR (InstanceId is NULL AND ProductId is NULL))";
  private static final String GS_LIST_SQL_NAME   = " AND Name like ?";
  private static final String GS_LIST_SQL_ORDER  = " ORDER BY Area, Name";

  /**
   * Constructor
   */
  public GlobalSetting() throws DBException
  {
    super("AED", "AES_SYS_GLOBAL_SETTINGS", "AES_SYS_GLOBAL_SETTINGS",
        Factory.create(GlobalSettingData.class));
  }

  /**
   * Get a particular GlobalSetting record
   * 
   * @param isArea
   * @param isName
   * @return
   * @throws DBException
   */
  public GlobalSettingData getData(String isArea, String isName) throws DBException
  {
    if (isArea == null)
    {
      return getData(isName);
    }
    
    /*
     * ProductId = null AND InstanceId = null signify global settings
     * ProductId = XXXX AND InstanceId = null signify product settings
     * ProductId = XXXX AND InstanceId = YYYY signify instance settings
     */
    
    /*
      select * from AES_SYS_GLOBAL_SETTINGS
        where (
                  (ProductId = ? and InstanceId = ?) or 
                  (ProductId = ? and InstanceId is null) or 
                  (ProductId is null and InstanceId is null)
              )
          and Area=?
          and Name=?
          order by ProductId desc, InstanceId desc
     */
    StringBuilder sql = new StringBuilder();
    sql.append("select * from ").append(getReadTableName())
       .append(" where (")
       .append("           (ProductId = ? and InstanceId = ?) or") 
       .append("           (ProductId = ? and InstanceId is null) or") 
       .append("           (ProductId is null and InstanceId is null)")
       .append("       )")
       .append(" and Area=?")
       .append(" and Name=?")
       .append(" order by ProductId desc, InstanceId desc");
    List<GlobalSettingData> vpGsList = fetchData(new GlobalSettingData(),
        sql.toString(), GS_PRODUCT_ID, GS_INSTANCE_ID, GS_PRODUCT_ID, isArea,
        isName);
    if (vpGsList.size() > 0)
    {
      return vpGsList.get(0);
    }
    return null;
  }

  /**
   * Get a particular GlobalSetting record (without an Area)
   * 
   * @param isArea
   * @param isName
   * @return
   * @throws DBException
   */
  public GlobalSettingData getData(String isName) throws DBException
  {
    /*
     * ProductId = null AND InstanceId = null signify global settings
     * ProductId = XXXX AND InstanceId = null signify product settings
     * ProductId = XXXX AND InstanceId = YYYY signify instance settings
     */
    
    /*
      select * from AES_SYS_GLOBAL_SETTINGS
        where (
                  (ProductId = ? and InstanceId = ?) or 
                  (ProductId = ? and InstanceId is null) or 
                  (ProductId is null and InstanceId is null)
              )
          and Area is null
          and Name=?
          order by ProductId desc, InstanceId desc
     */
    StringBuilder sql = new StringBuilder();
    sql.append("select * from ").append(getReadTableName())
       .append(" where (")
       .append("           (ProductId = ? and InstanceId = ?) or") 
       .append("           (ProductId = ? and InstanceId is null) or") 
       .append("           (ProductId is null and InstanceId is null)")
       .append("       )")
       .append(" and Area is null")
       .append(" and Name=?")
       .append(" order by ProductId desc, InstanceId desc");
    List<GlobalSettingData> vpGsList = fetchData(new GlobalSettingData(),
        sql.toString(), GS_PRODUCT_ID, GS_INSTANCE_ID, GS_PRODUCT_ID, isName);
    if (vpGsList.size() > 0)
    {
      return vpGsList.get(0);
    }
    return null;
  }

  /**
   * Get a list of Global Setting in the system
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList() throws DBException
  {
    return getList(null);
  }

  /**
   * Get a list of Global Setting in the system
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList(String isName) throws DBException
  {
    if (isName != null && isName.trim().length() > 0) {
      return fetchRecords(
          GS_LIST_SQL_SELECT + getReadTableName() + GS_LIST_SQL_WHERE
              + GS_LIST_SQL_NAME + GS_LIST_SQL_ORDER,
          GS_INSTANCE_ID, GS_PRODUCT_ID, "%" + isName + "%");
    }
    else
    {
      return fetchRecords(GS_LIST_SQL_SELECT + getReadTableName()
          + GS_LIST_SQL_WHERE + GS_LIST_SQL_ORDER, GS_INSTANCE_ID,
          GS_PRODUCT_ID);
    }
  }

  /**
   * Get a list of Global Settings in the system
   * 
   * @return
   * @throws DBException
   */
  public List<GlobalSettingData> getDataList() throws DBException
  {
    return fetchData(
        new GlobalSettingData(), GS_LIST_SQL_SELECT + getReadTableName()
            + GS_LIST_SQL_WHERE + GS_LIST_SQL_ORDER,
        GS_INSTANCE_ID, GS_PRODUCT_ID);
  }

  /**
   * Delete a Global Setting
   * 
   * @param isArea
   * @param isName
   * @throws DBException
   */
  public void delete(String isArea, String isName) throws DBException
  {
    GlobalSettingData vpGSData = getData(isArea, isName);
    if (vpGSData != null)
    {
      vpGSData.setKey(GlobalSettingData.ID_NAME, vpGSData.getId());
      deleteElement(vpGSData);
    }
  }

  /**
   * Determines whether or not a global setting exists
   * 
   * @param isArea
   * @param isName
   * @return
   * @throws DBException
   */
  public boolean exists(String isArea, String isName) throws DBException
  {
    return getData(isArea, isName) != null;
  }

  /**
   * Update global setting data
   * 
   * @param ipGSD
   * @throws DBException
   */
  public void update(GlobalSettingData ipGSD)
      throws DBException
  {
    ipGSD.setKey(GlobalSettingData.ID_NAME, ipGSD.getId());
    modifyElement(ipGSD);
  }
}
