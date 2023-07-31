package com.daifukuamerica.wrxj.swingui.developer;

/*
                       SKDC Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used
   and copied only in accordance with the terms of such license.
   This software or any other copies thereof in any form, may not be
   provided or otherwise made available, to any other person or company
   without written consent from SKDC Corporation.

   SKDC assumes no responsibility for the use or reliability of
   software which has been modified without approval.
*/

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Altered DBTableInfo for use by the DB code generators
 * 
 * @author mandrus
 */
public class DBTableInfo
{
  public static final String ID_NAME = "ID";
  
  private Map<String, Map<String,Integer>> mpSchemaDef = new HashMap<>();
  private Logger  logger = Logger.getLogger();

  /**
   * Constructor
   * 
   * @param isDBConfig
   * @throws DBException
   */
  public DBTableInfo(String isDBConfig, String isSchema) throws DBException
  {
    String vsSchema = SKDCUtility.isBlank(isSchema) ? 
      Application.getString("database.schema", "ASRS") : isSchema;  
    
    loadFieldInfo(isDBConfig, vsSchema);
  }

  /**
   * Get the column name, without Hungarian notation
   * 
   * @param isColumn
   * @param usesHungarian
   * @return
   */
  public static final String getColumnName(String isColumn, boolean usesHungarian, boolean toUpper)
  {
    String columnName = isColumn;
    if (toUpper)
    {
      columnName = columnName.toUpperCase();
    }
    if (usesHungarian && !columnName.equalsIgnoreCase(DBTableInfo.ID_NAME))
    {
      columnName = columnName.substring(1);
    }
    return columnName;
  }

  /**
   * Suggest an object name for a given database table or column name
   * 
   * @param dbObjName
   * @return
   */
  public static String suggestObjName(String dbObjName)
  {
    if (dbObjName.equalsIgnoreCase(DBTableInfo.ID_NAME))
    {
      return "Id";
    }
    // If it is already mixed case with a starting capital, assume it that is correct
    if (!dbObjName.toLowerCase().equals(dbObjName)
        && dbObjName.charAt(0) == Character.toUpperCase(dbObjName.charAt(0)))
    {
      return dbObjName;
    }
    String objName = dbObjName.toLowerCase();
    StringBuilder sb = new StringBuilder();
    boolean needsCap = true;
    for (char c : objName.toCharArray())
    {
      if (c == '_')
      {
        needsCap = true;
        continue;
      }
      if (needsCap)
      {
        c = Character.toUpperCase(c);
        needsCap = false;
      }
      sb.append(c);
    }
    return sb.toString();
  }
  
  /**
   * Retrieves Array List of MetaData data.
   * @param isDBConfig
   * @param isSchema
   */
  private void loadFieldInfo(String isDBConfig, String isSchema) throws DBException
  {
    DBObject dbobj;
    if (isDBConfig == null || isDBConfig.trim().length() == 0
        || isDBConfig.equals(Application.getString("database")))
    {
      dbobj = new DBObjectTL().getDBObject();
    }
    else
    {
      dbobj = new DBObjectTL().getDBObject(isDBConfig);
    }

    try
    {
      dbobj.connect();                 // Get connection from connection pool.
    }
    catch (DBException e)
    {
      throw new DBException("Unable to connect to database.", e);
    }

    logger.logDebug("DBTableInfo.loadFieldInfo() - Reloading Database JDBC MetaData");

    DatabaseMetaData metaData = null;
    if (dbobj != null)
    {
      try
      {
        metaData = dbobj.getDataBaseMetaData();

        if (metaData != null)
        {
          ResultSet columns = metaData.getColumns(null, isSchema, null, null);

          while(columns.next() )
          {
            String tableName = columns.getString("TABLE_NAME");
            String colName = columns.getString("COLUMN_NAME");
            String typeName = columns.getString("TYPE_NAME");
            int decimalDigits = columns.getInt("DECIMAL_DIGITS");
//            int colSize = columns.getInt("COLUMN_SIZE");
            int colType = columns.getInt("DATA_TYPE");
            if (!tableName.equals("PLAN_TABLE") && // Toad adds PLAN_TABLE
                /* PostgreSQL tables */
                !tableName.startsWith("pg_") &&
                !tableName.equals("check_constraints") &&
                !tableName.equals("views") &&
                !tableName.startsWith("view_") &&
                !tableName.equals("routines") &&
                !tableName.equals("schemata") &&
                !tableName.equals("routine_privileges") &&
                !tableName.equals("parameters") &&
                !tableName.equals("key_column_usage") &&
                !tableName.equals("domains") &&
                !tableName.startsWith("domain_") &&
                !tableName.equals("data_type_privileges") &&
                !tableName.equals("usage_privileges") &&
                !tableName.equals("triggers") &&
                !tableName.equals("tables") &&
                !tableName.startsWith("table_") &&
                !tableName.startsWith("sql_") &&
                !tableName.startsWith("role_") &&
                !tableName.equals("referential_constraints") &&
                !tableName.equals("information_schema_catalog_name") &&
                !tableName.startsWith("constraint_") &&
                !tableName.startsWith("element_") &&
                !tableName.equals("columns") &&
                !tableName.startsWith("column_") &&
                !tableName.startsWith("triggered_") &&
                !tableName.startsWith("applicable_") &&
                !tableName.startsWith("enabled_roles") &&
                /* END PostgreSQL tables */
              !tableName.startsWith("RECYCLEBIN10G") && // Oracle 10 recycled tables
              !tableName.startsWith("BIN") && // Oracle 10 Also recyclebin tables
              (!tableName.startsWith("QUEST_"))) // ABC uses a DBA tool that adds QUEST_ tables
            {
              Map<String, Integer> tableDef = mpSchemaDef.get(tableName.toUpperCase());
              if (tableDef == null) {
                tableDef = new TreeMap<>();
                mpSchemaDef.put(tableName.toUpperCase(), tableDef);
              }
              
              //System.out.println(tableName + "." + colName + ": " + typeName + "(" + colSize + ")");
              tableDef.put(colName, determineTypeToUse(colName, colType, decimalDigits, typeName));
            }
          }
        }
      }
      catch (DBException e)
      {
        System.out.println("DBTableInfo - Unable to get database information - " + e.getMessage());
      }
      catch (SQLException e)
      {
        System.out.println("DBTableInfo - Unable to get database information - " + e.getMessage());
      }
    }
  }

  /**
   * Determines what type should be used for the field.
   *
   * @param fldName String containing field name.
   * @param jdbcType Integer containing jdbc type.
   * @param decimalDigits Integer containing number of digits after the decimal.
   * @param typeName String containing jdbc type.
   *
   * @return integer containing field type.
   */
  private static int determineTypeToUse(String fldName, int jdbcType, int decimalDigits, String typeName)
  {
    int type = -1;

    // determine jdbc data type
    switch (jdbcType)
    {
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
        type = Types.INTEGER;
        break;
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.DECIMAL:
        if (decimalDigits == 0)
        {
          type = Types.INTEGER;
        }
        else
        {
          type = Types.FLOAT;
        }
        break;
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
        type = Types.VARCHAR;
        break;
      case Types.BLOB:
      case Types.CLOB:
        type = jdbcType;
        break;
      case Types.TIMESTAMP:
        type = Types.DATE;
        break;
      case Types.OTHER:
        if (typeName.startsWith("TIMESTAMP"))
        {
          type = Types.DATE;
          break;
        }
      case Types.BIT:
        type = Types.INTEGER;
        break;
      case Types.VARBINARY:
        type = Types.VARCHAR;
        break;
      // SQL Server datetimeoffset
      case DBInfo.MSSQL_DATETIMEOFFSET:
        type = DBInfo.MSSQL_DATETIMEOFFSET;
        break;
      case Types.TIME:
      case Types.DATE:
      case Types.REAL:
      case Types.BINARY:
      case Types.LONGVARBINARY:
      case Types.NULL:
      case Types.JAVA_OBJECT:
      case Types.DISTINCT:
      case Types.STRUCT:
      case Types.ARRAY:
      case Types.REF:
      default :
        System.out.println("DBTableInfo - UNSUPPORTED JDBC Type: " + typeName +
          ", field name: " + fldName);
    }  //switch
    if (type < 0 && type != DBInfo.MSSQL_DATETIMEOFFSET)
    {
      System.out.println("DBTableInfo - UNDETERMINED Type for field: " + fldName + " (" + type + "," + typeName + ")");
    }

    return (type);
  }

  /**
   * Get the list of tables
   * @return
   */
  public Set<String> getTables() {
    return mpSchemaDef.keySet();
  }

  /**
   * Get the columns for a table
   * @param tableName
   * @return
   */
  public Map<String,Integer> getColumns(String tableName) {
    return mpSchemaDef.get(tableName);
  }
}

