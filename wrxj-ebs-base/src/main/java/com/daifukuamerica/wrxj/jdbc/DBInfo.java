package com.daifukuamerica.wrxj.jdbc;

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
import com.daifukuamerica.wrxj.log.Logger;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description:<BR>
 *   Class for getting Database meta data information.
 *
 * @author       A.T.
 * @version      1.0
 * <BR>Created: 23-Oct-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class DBInfo
{
  public static boolean USING_ORACLE_DB;
  public static boolean USING_SQL_SERVER;

  /*
   * The following database name strings should be changed with care.  If you
   * change the name here, make sure to change the corresponding string in
   * wrxj.properties also.
   */
  private static String ORACLE_DB_NAME = "OracleDB";
  private static String SQL_SERVER_NAME = "SQLServer";
  
  private static DBObject dbobj    = null;
  private static Map<String,Info> fieldData   = new HashMap<String,Info>(250);
  private static String schema = Application.getString("database.schema");
  private static int DEFAULTFIELDLENGTH               = 1;
  private static int DEFAULTQUANTITYLENGTH            = 8;
  private static int DEFAULTTIMESTAMPLENGTH           = 30;
  private static String[] length1fields = {"iPriority"};
  private static String[] length2fields = {"iHeight", "iAisleGroup"};
  private static Logger  logger = Logger.getLogger();

  public static final int MSSQL_DATETIMEOFFSET = -155;
  public static final int UNKNOWN_TYPE = -999;
  
  private static class Info
  {
    private int length;
    private int dataType;

    public Info(int fieldLength, int fieldType)
    {
      length = fieldLength;
      dataType = fieldType;
    }

    public int getLength()
    {
      return length;
    }

    public int getType()
    {
      return dataType;
    }
    
    @Override
    public String toString()
    {
      return getClass().getSimpleName() + "(Type=[" + dataType + "], Length=[" + length + "])";
    }
  }

  /**
   * Initializes DB Information map and stores data for retrieval later.
   */
  public static void init()  throws DBException
  {
    if (fieldData.isEmpty())
    {
      String dbtype = Application.getString(DBObject.DATABASE_KEY);
      USING_ORACLE_DB = dbtype.equals(ORACLE_DB_NAME);
      USING_SQL_SERVER = dbtype.equals(SQL_SERVER_NAME);
      if (!USING_ORACLE_DB && !USING_SQL_SERVER) {
        throw new DBException("Unknown database: \"" + dbtype + "\". Valid values are " + ORACLE_DB_NAME + ", " + SQL_SERVER_NAME);
      }
      loadFieldInfo();
    }
  }

 /**
  * Retrieves Array List of MetaData data.
  *
  */
  private static void loadFieldInfo() throws DBException
  {
    if (dbobj == null)
    {
      dbobj = new DBObjectTL().getDBObject();

      try
      {
        dbobj.connect();                 // Get connection from connection pool.
      }
      catch (DBException e)
      {
        dbobj = null;
        throw new DBException("Unable to connect to database.", e);
      }
    }
    logger.logDebug("DBInfo.loadFieldInfo() - Reloading Database JDBC MetaData");

    // Ignore tables that we don't manage that may be in our table space 
    Set<String> vpIgnoreTables = new HashSet<>();
    String vsIgnoreTables = Application.getString(
        Application.getString(DBObject.DATABASE_KEY) + ".IgnoreTables");
    if (vsIgnoreTables != null && vsIgnoreTables.trim().length() > 0)
    {
      Collections.addAll(vpIgnoreTables, vsIgnoreTables.split(","));
    }
    
    DatabaseMetaData metaData = null;
    if (dbobj != null)
    {
      try
      {
        metaData = dbobj.getDataBaseMetaData();

        if (metaData != null)
        {
          // Default the schema to ASRS if it's not defined.
          if(schema == null)
          {
            schema = "ASRS";
          }
          ResultSet columns = metaData.getColumns(null,schema,null,null);

          while(columns.next() )
          {
            String tableName = columns.getString("TABLE_NAME");
            String colName = columns.getString("COLUMN_NAME");
            String typeName = columns.getString("TYPE_NAME");
            int decimalDigits = columns.getInt("DECIMAL_DIGITS");
            int colSize = columns.getInt("COLUMN_SIZE");
            int colType = columns.getInt("DATA_TYPE");
            
            // Ignore tables that we don't manage that may be in our table space
            if (vpIgnoreTables.contains(tableName))
            {
//              System.out.println("DBInfo: Ignoring " + tableName);
              continue;
            }
            
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
              int skSize = determineLengthToUse(tableName, colName, colType, typeName, colSize);
              String key = colName.toUpperCase();
              if (!fieldData.containsKey(key))
              {
                fieldData.put(key, new Info(skSize,
                  determineTypeToUse(colName, colType, decimalDigits, typeName)));
              }
            }
          }
        }
      }
      catch (DBException e)
      {
        System.out.println("DBInfo - Unable to get database information - " + e.getMessage());
      }
      catch (SQLException e)
      {
        System.out.println("DBInfo - Unable to get database information - " + e.getMessage());
      }
    }
  }

 /**
  *  Method determines if the passed in field name is a known field name in the
  *  database.
  *  @param isFieldName the field name in question.
  *  @return <code>true</code> if the field is a valid Database field name,
  *          <code>false</code> otherwise.
  */
  public static boolean isValidField(String isFieldName)
  {
    return fieldData.containsKey(toKeyName(isFieldName));
  }

  /**
   * Retrieves length of data base field.
   *
   * @return integer containing field length.
   */
  public static int getFieldLength(String fieldName)
  {
    int fieldLength = DEFAULTFIELDLENGTH; // default length
    String vsKey = toKeyName(fieldName);
    if (fieldData.containsKey(vsKey))
    {
      Info info = fieldData.get(vsKey);
      fieldLength = info.getLength();
    }

    return fieldLength;
  }

  /**
   * Retrieves type of data base field.
   *
   * @return integer containing field type.
   */
  public static int getFieldType(String fieldName)
  {
    int fieldType = DBInfo.UNKNOWN_TYPE; // no default type
    String vsKey = toKeyName(fieldName);
    if (fieldData.containsKey(vsKey))
    {
      Info info = fieldData.get(vsKey);
      fieldType = info.getType();
    }
    return fieldType;
  }

   /**
    * Method to set the type of a field.  Primarily for use with foreign 
    * databases that DBInfo doesn't scan.
    * 
    * @param fieldName
    * @param jdbcType
    * @param length
    */
   public static void setFieldType(String fieldName, int jdbcType, int length)
   {
     if (getFieldType(fieldName) == -1)
     {
       fieldData.put(fieldName.toUpperCase(), new Info(length, jdbcType));
     }
   }
   
  /**
   * Determines what length should be used for the field.
   *
   * @param fldName String containing field name.
   * @param jdbcType Integer containing jdbc type.
   * @param typeName String containing jdbc type.
   * @param jdbcSize Integer containing field size from the jdbc meta data.
   *
   * @return integer containing field length.
   */
  private static int determineLengthToUse(String tableName, String fldName, int jdbcType, String typeName, int jdbcSize)
  {
    int length = -1;

    // determine jdbc data type
    switch (jdbcType)
    {
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.DECIMAL:

        // take care of some special integer fields that are limited in screen length
        for (int ix = 0; ((length == -1 ) && (ix < length2fields.length)); ix++)
        {
          if (length2fields[ix].equalsIgnoreCase(fldName))
          {
            length = 2;
          }
        }
        for (int ix = 0; ((length == -1 ) && (ix < length1fields.length)); ix++)
        {
          if (length1fields[ix].equalsIgnoreCase(fldName))
          {
            length = 1;
          }
        }

        if (length < 0)
        {
          length = (DBTrans.isTranslation(fldName)) ? DBTrans.getMaxTranslationLength(fldName)
                                                    : DEFAULTQUANTITYLENGTH;
        }
        break;
      case Types.CHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
        length = jdbcSize;
        break;
      case Types.BLOB:
      case Types.CLOB:
        length = Integer.MAX_VALUE;
        break;
      case Types.TIMESTAMP:
        length = DEFAULTTIMESTAMPLENGTH;
        break;
      case Types.OTHER:
        if (typeName.startsWith("TIMESTAMP"))
        {
          length = DEFAULTTIMESTAMPLENGTH;
          break;
        }
      case Types.BIT:
        length = 1;
        break;
      case Types.VARBINARY:
        length = jdbcSize;
        break;
      // SQL Server datetimeoffset
      case MSSQL_DATETIMEOFFSET:
        length = jdbcSize;
        break;
      case Types.TINYINT:
        length = jdbcSize;
        break;
      case Types.SMALLINT:
        length = jdbcSize;
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
        System.out.println("DBInfo - UNSUPPORTED JDBC Type: " + typeName +
          " - Table: " + tableName + " - Field: " + fldName);
        try
        {
          throw new Exception("DBInfo");
        }
        catch (Exception e)
        {
          logger.logException(e, "DBInfo - Table: " + tableName + " - Field: " + fldName +
                                 " - Unsupported JDBC Type: " + typeName);
        }
    }  //switch
    if (length < 0)
    {
      System.out.println("DBInfo - UNDETERMINED Length for field: " + fldName);
    }

    return (length);

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
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
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
      case MSSQL_DATETIMEOFFSET:
        type = MSSQL_DATETIMEOFFSET;
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
        System.out.println("DBInfo - UNSUPPORTED JDBC Type: " + typeName + "("
            + jdbcType + ")" + ", field name: " + fldName);
    }  //switch
    if (type < 0)
    {
      System.out.println("DBInfo - UNDETERMINED Type for field: " + fldName);
    }

    return (type);
  }

  /**
   * Convert a column name to a key name
   * 
   * @param isColumnName in table.column or column form
   * @return
   */
  private static final String toKeyName(String isColumnName)
  {
    String vsKeyName = isColumnName.toUpperCase().trim();
    int dot = isColumnName.indexOf('.');
    if (dot != -1)
    {
      vsKeyName = vsKeyName.substring(dot + 1);
    }
    return vsKeyName;
  }
}
