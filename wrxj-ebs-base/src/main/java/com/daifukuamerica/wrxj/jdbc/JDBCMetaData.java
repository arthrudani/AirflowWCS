package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.application.Application;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Description:<BR>
 *  Class to present database meta-data in a more readily accessible form.
 * @author       A.D.
 * @version      1.0
 *     Copyright (c) 2004<BR>
 *     Company:  DaifukuAmerica Corporation
 */
public class JDBCMetaData
{
//  private Logging  logger = SkdContext.getLogger();
  private DBObject mpDBObj;
  private static Map<String,String> mpTableNameMap = new TreeMap<String,String>();
  private static Map<String, Map<String,String>> mpTableColumnMap = new TreeMap<String, Map<String,String>>();
  private static DatabaseMetaData mpDBMetaData;

  private JDBCMetaData() throws DBRuntimeException
  {
    super();
    mpDBObj = new DBObjectTL().getDBObject();
    try
    {
      if (!mpDBObj.checkConnected())
      {
        mpDBObj.connect();               // Get connection from connection pool.
      }
      mpDBMetaData = mpDBObj.getDataBaseMetaData();
    }
    catch (DBException e)
    {
      throw new DBRuntimeException("Error opening Database Connection", e);
    }

    initTableNamesList();
  }

  public static void init()
  {
    new JDBCMetaData();
  }

  public static Map<String,String> getTableNames()
  {
    return(mpTableNameMap);
  }

 /**
  * Gets a map of all column names for a given table.
  * @param sTableName the table for which to get the column names.
  * @return Map of column names.
  * @throws DBRuntimeException for DB access errors.
  */
  public static Map<String,String> getColumnNames(String sTableName) throws DBRuntimeException
  {
    String tableNameKey = sTableName.toUpperCase();
    if (!mpTableColumnMap.containsKey(tableNameKey))
    {                                  // Cache the data for future reference.
      JDBCMetaData.addToColumnMapCache(tableNameKey);
    }

    return(mpTableColumnMap.get(tableNameKey));
  }

 /**
  * Method to get the Database Vendor Name.
  * @return the DB vendor name.  Return the string "Unknown" if there is a
  *         problem getting string.
  */
  public static String getDatabaseVendorName()
  {
    String vsVendorName;

    try
    {
      vsVendorName = mpDBMetaData.getDatabaseProductName();
    }
    catch(Exception ex)
    {
      vsVendorName = "Unknown";
    }

    return(vsVendorName);
  }

 /**
  *  Method initialises list of table names.
  */
  private void initTableNamesList() throws DBRuntimeException
  {
    try
    {
      String vsSchemaName = Application.getString("database.schema","ASRS");
      ResultSet rsltSet = mpDBMetaData.getTables(null, vsSchemaName, null, null);
      while(rsltSet.next())
      {
        mpTableNameMap.put(rsltSet.getString("TABLE_NAME"),
                         rsltSet.getString("TABLE_NAME"));
      }
    }
    catch(SQLException sqlExc)
    {
      throw new DBRuntimeException("Error retrieving JDBC ResultSet data.", sqlExc);
    }
  }

  private static void addToColumnMapCache(String isTableName) throws DBRuntimeException
  {
    try
    {
      String vsSchemaName = Application.getString("database.schema","ASRS");
      ResultSet rsltSet = mpDBMetaData.getColumns(null, vsSchemaName, isTableName, null);
      try { Thread.sleep(600); } catch(InterruptedException ie) {}
      Map<String,String> tempColumnMap = new TreeMap<String,String>();
      while(rsltSet.next())
      {
        tempColumnMap.put(rsltSet.getString("COLUMN_NAME"),
                          rsltSet.getString("COLUMN_NAME"));
      }
      mpTableColumnMap.put(isTableName, tempColumnMap);
    }
    catch(SQLException sqlExc)
    {
      throw new DBRuntimeException("Error retrieving JDBC ResultSet data.", sqlExc);
    }
  }
}
