package com.daifukuamerica.wrxj.dbadapter.data;
/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *   Class to handle Warehouse specific operations.
 *
 * @author       A.D.
 * @version      1.0    02/20/02
 * @version      2.0    10/14/04
 */
public class Warehouse extends BaseDBInterface
{
  protected WarehouseData mpWarehouseData;

  public Warehouse()
  {
    super("Warehouse");
    mpWarehouseData = Factory.create(WarehouseData.class);
  }

  /**
   * Retrieves List of all Super warehouses on the system.  The data returned
   * from here is primarily meant to be used in choice lists.
   *
   *  @param isAllOrNone <code>String</code> containing string to start list
   *         with.  The values are SKDCConstants.ALL_STRING or
   *         SKDCConstants.NONE_STRING
   *
   * @return String array of Super warehouses.
   */
  public String[] getSuperWarehouseChoices(String isAllOrNone)
         throws DBException
  {
    mpWarehouseData.clear();
    mpWarehouseData.setKey(WarehouseData.WAREHOUSETYPE_NAME, DBConstants.SUPER);
    mpWarehouseData.addOrderByColumn(WarehouseData.WAREHOUSE_NAME);
    
    return getSingleColumnValues(WarehouseData.WAREHOUSE_NAME, true,
        mpWarehouseData, isAllOrNone);
  }

  /**
   * Retrieves List of all regular (child) warehouses on the system.  The data
   * returned from here is primarily meant to be used in choice lists.
   *
   * @param isFirstElement One of the following constants:<ul>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#ALL_STRING
   *              SKDCConstants.ALL_STRING}
   *            which prepends the string "ALL" to the array.</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NONE_STRING
   *              SKDCConstants.NONE_STRING}
   *            which prepends the string "NONE" to the array</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#EMPTY_VALUE 
   *              SKDCConstants.EMPTY_VALUE}
   *            which prepends a blank string to the array.</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NO_PREPENDER 
   *              SKDCConstants.NO_PREPENDER}
   *            which means there is no prepender (no pre-defined first element)
   *            to the list.</li></ul>
   *
   * @return String array of regular warehouses.
   * @throws DBException if there is a database access error.
   */
  public String[] getRegularWarehouseChoices(final String isFirstElement)
      throws DBException
  {
    mpWarehouseData.clear();
    mpWarehouseData.setKey(WarehouseData.WAREHOUSETYPE_NAME, DBConstants.REGULAR);
    mpWarehouseData.addOrderByColumn(WarehouseData.WAREHOUSE_NAME);
    
    return getSingleColumnValues(WarehouseData.WAREHOUSE_NAME, true,
                                 mpWarehouseData, isFirstElement);
  }

  /**
   * Retrieves List of all warehouses on the system.  The data returned
   * from here is primarily meant to be used in choice lists.
   *
   *  @param isAllOrNone <code>String</code> containing string to start list
   *         with.  The values are SKDCConstants.ALL_STRING or
   *         SKDCConstants.NONE_STRING
   * @return String array of regular warehouses.
   */
  public String[] getWarehouseChoices(String isAllOrNone) throws DBException
  {
    return getDistinctColumnValues(WarehouseData.WAREHOUSE_NAME, isAllOrNone);
  }

  /**
   * Retrieves a list of lowest level warehouses given a super warehouse.
   *
   * @param  superWarehouse <code>string</code>
   *
   * @return List of lowest level warehouse.
   */
  public List<Map> getLowestLevelWarehouse(String superWarehouse)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ")
        .append(getWriteTableName()).append(" WHERE ")
        .append(WarehouseData.WAREHOUSETYPE_NAME).append(" = ")
        .append(DBConstants.REGULAR).append(" START WITH sWarehouse = ? ")
        .append("CONNECT BY PRIOR sWarehouse = sSuperWarehouse");

    return fetchRecords(vpSql.toString(), superWarehouse);
  }

  /**
   * Checks if Super Warehouse and regular warehouse combination would exist in
   * a circular manner. If a new link is formed (eg. if P1 <--> C1 already
   * exists, and if someone tries to form C1 <--> P1 then this method returns
   * true).
   */
  public boolean isNewLinkCircular(String isSuper, String isRegular)
         throws DBException
  {
    mpWarehouseData.clear();
    mpWarehouseData.setKey(WarehouseData.SUPERWAREHOUSE_NAME, isSuper);
    mpWarehouseData.setKey(WarehouseData.WAREHOUSE_NAME, isRegular);
    if (getCount(mpWarehouseData) == 0)
    {
      mpWarehouseData.clear();
      mpWarehouseData.setKey(WarehouseData.SUPERWAREHOUSE_NAME, isRegular);
      mpWarehouseData.setKey(WarehouseData.WAREHOUSE_NAME, isSuper);
      return getCount(mpWarehouseData) > 0;
    }

    return true;
  }

  /**
   * Method checks if a super warehouse contains sub-warehouses that are
   * themselves super warehouses.
   * @param superWarehouse warehouse checked to see if sub-warehouse it contains
   *        are super warehouses also.
   * @return <code>true</code> if this super warehouse has sub-warehouses that
   *         ar super warehouses also.
   * @throws DBException
   */
  public boolean isSubWarehouseParent(String superWarehouse) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT COUNT(*) AS \"rowCount\" FROM Warehouse WHERE ")
        .append("sSuperWarehouse = '").append(superWarehouse).append("' ")
        .append("AND sWarehouse IN (SELECT wt.sWarehouse FROM warehouse wt ")
        .append("WHERE wt.iWarehouseType = ").append(DBConstants.SUPER).append(")");

    return getRecordCount(vpSql.toString(), "rowCount") > 0;
  }

 /**
  * Method checks if a warehouse is a super warehouse.
  *
  * @param isWarehouse the warehouse being checked.
  * @return <code>true</code> if this is a super warehouse.
  */
  public boolean isSuperWarehouse(String isWarehouse)
  {
    mpWarehouseData.clear();
    mpWarehouseData.setKey(WarehouseData.WAREHOUSE_NAME, isWarehouse);
    mpWarehouseData.setKey(WarehouseData.WAREHOUSETYPE_NAME, DBConstants.SUPER);

    return(exists(mpWarehouseData));
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpWarehouseData = null;
  }
  
  public String getWarehouseType(String warehouse) throws DBException
  {
	StringBuilder vpSql = new StringBuilder("SELECT iWarehouseType FROM Warehouse WHERE ")
             .append("sWarehouse = '").append(warehouse).append("'");

    return(getStringColumn(WarehouseData.WAREHOUSETYPE_NAME, vpSql.toString()));
  }
}
