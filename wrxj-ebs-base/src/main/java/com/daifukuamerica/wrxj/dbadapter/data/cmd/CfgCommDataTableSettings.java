package com.daifukuamerica.wrxj.dbadapter.data.cmd;

import com.daifukuamerica.wrxj.dbadapter.WynsoftDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Class to support database operations on the CME_CFG_COMM_DATA_TABLE_SETTINGS table
 */
public class CfgCommDataTableSettings extends WynsoftDBInterface
{
  
  private CfgCommDataTableSettingsData mpCommData;

  public CfgCommDataTableSettings()
  {
    super("CMD", "CME_CFG_COMM_DATA_TABLE_SETTINGS", "CME_CFG_COMM_DATA_TABLE_SETTINGS",
        Factory.create(CfgCommDataTableSettingsData.class));
    mpCommData = Factory.create(CfgCommDataTableSettingsData.class);
  }

  /**
   * Get a particular CfgCommDataTableSettings record
   * 
   * @param isConnectionName
   * @param inDirection
   * @return
   * @throws DBException
   */
  public CfgCommDataTableSettingsData getData(String isConnectionName,
      int inDirection) throws DBException
  {
    mpCommData.clear();
    mpCommData.setKey(CfgCommDataTableSettingsData.CLIENTCONNECTIONNAME_NAME,
        isConnectionName);
    mpCommData.setKey(CfgCommDataTableSettingsData.DIRECTIONID_NAME,
        inDirection);

    return getElement(mpCommData, DBConstants.NOWRITELOCK);
  }
  
  /**
   * Get a CfgCommDataTableSettings list
   * 
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public List<Map> getList() throws DBException
  {
    mpCommData.clear();
    mpCommData.addOrderByColumn(CfgCommDataTableSettingsData.COMMUNICATIONID_NAME);
    mpCommData.addOrderByColumn(CfgCommDataTableSettingsData.CLIENTCONNECTIONNAME_NAME);
    mpCommData.addOrderByColumn(CfgCommDataTableSettingsData.DIRECTIONID_NAME);

    return getAllElements(mpCommData);
  }

  /**
   * Determines whether or not a CfgCommDataTableSettings record exists
   * 
   * @param isConnectionName
   * @param inDirection
   * @return
   * @throws DBException
   */
  public boolean exists(String isConnectionName, int inDirection)
      throws DBException
  {
    return getData(isConnectionName, inDirection) != null;
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpCommData = null;
  }
}
