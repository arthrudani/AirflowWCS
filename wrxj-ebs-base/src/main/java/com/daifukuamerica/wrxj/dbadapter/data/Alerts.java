package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.AmountFullTransMapper;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:<BR>
 *   Title:  Class to handle Load Object.
 *   Description : Handles all reading and writing database for load
 * @author       REA
 * @version      1.0
 * <BR>Created: 4-Feb-25<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class Alerts extends BaseDBInterface
{
  private DBResultSet mpDBResultSet;
  protected AlertData mpAlertData;

  public Alerts()
  {
    super("Alerts");
    mpAlertData = Factory.create(AlertData.class);
  }

  /**
   * Retrieves one column value from the Load table.
   *
   * @param isAlertID the unique key to use in the search.
   * @param isColumnName the name of the column whose value is returned.
   * @return value of column specified by isColumnName as an <code>Object</code>.
   *         The caller is assumed to know what data type is actually in
   *         <code>Object</code>. <i>A</i> <code>null</code> <i>object is
   *         returned for no matching data</i>
   * @throws DBException when database access errors occur.
   */
  public Object getSingleColumnValue(String isAlertID, String isColumnName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(isColumnName).append(" FROM Load WHERE ")
             .append("iID = '").append(isAlertID).append("'");
    List<Map> vpData = fetchRecords(vpSql.toString());

    return((!vpData.isEmpty()) ? (vpData.get(0)).get(isColumnName.toUpperCase()) : null);
  }




  /**
   * use data that is sent to create load
   */
  public boolean createAlert(AlertData newAlert) throws DBException
  {
    if (exists(newAlert.getAlertId()))
    {
       return false;
    }
    else
    {
      addLoad(newAlert);
      return true;
    }
  }

  /**
   *  Method retrieves a load record using load as key.
   *
   *  @param loadID <code>String</code> containing load to search for.
   *  @param withLock <code>int</code> flag indicating if record should be locked.
   *
   *  @return <code>AlertData</code> object. <code>null</code> if no record found.
   */
  public AlertData getAlertData(String alertID, int withLock) throws DBException
  {
    mpAlertData.clear();
    mpAlertData.setKey(AlertData.ALERTID_NAME, alertID);

    return getElement(mpAlertData, withLock);
  }


  /**
   * Convenience method.  This gets a Load record with no lock.
   */
  public AlertData getAlertData(String alertID) throws DBException
  {
    return getAlertData(alertID, DBConstants.NOWRITELOCK);
  }

 
  /**
   * Add a load
   *
   * @param newAlert
   * @throws DBException
   */
  public void addLoad(AlertData newAlert) throws DBException
  {
    addElement(newAlert);
  }

  /**
   * Delete a load
   *
   * @param loadID
   * @throws DBException
   */
  public void deleteAlert(String alertID) throws DBException
  {
    if (exists(alertID))
    {
      mpAlertData.clear();
      mpAlertData.setKey(AlertData.ALERTID_NAME, alertID);
      deleteElement(mpAlertData);
    }
  }

  /**
   * Does this load ID exist?
   *
   * @param loadID
   * @return <code>true</code>
   */
  public boolean exists(String alertID)
  {
    mpAlertData.clear();
    mpAlertData.setKey(AlertData.ALERTID_NAME, alertID);

    return(exists(mpAlertData));
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpAlertData = null;
    mpDBResultSet = null;
  }

  /**
   * Get a list of load data
   *
   * @param isSearchLoad Load ID or partial load string.  If a partial string is
   *        passed, the partial match will only be done from the end of the string.
   * @return List&lt;Map&gt; representing list of load data.
   * @throws DBException database access error.
   */
  public List<Map> getAlertDataList(String isSearchLoad) throws DBException
  {
    mpAlertData.clear();
    mpAlertData.setWildcardKey(AlertData.ALERTID_NAME, isSearchLoad, false);

    return(getAllElements(mpAlertData));
  }

  /**
   * Get a list of load data
   *
   * @param iapKeys search keys.
   * @return List&lt;Map&gt; representing list of load data.
   * @throws DBException database access error.
   */
  public List<Map> getAlertDataList(KeyObject[] iapKeys) throws DBException
  {
    mpAlertData.clear();
    mpAlertData.setKeys(iapKeys);
    mpAlertData.setOrderByColumns(AlertData.ALERTID_NAME);

    return(getAllElements(mpAlertData));
  }
}

