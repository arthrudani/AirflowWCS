package com.daifukuamerica.wrxj.dbadapter;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.jdbc.sqlserver.OffsetDateTimeUtil;
import com.daifukuamerica.wrxj.log.Logger;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

/**
* Description:<BR>
*   Class for building SQL statements.
*
* @author       A.D.
* @version      1.0
* <BR>Created: 31-Oct-01<BR>
*     Copyright (c) 2001<BR>
*     Company:  Daifuku America Corporation
*/
public class SQLObject
{
  public static final int ADD = 1;     /** Type of SQL statement to build. */
  public static final int MODIFY = 2;  /** Type of SQL statement to build. */
  public static final int DELETE = 3;  /** Type of SQL statement to build. */
  public static final int READ = 4;    /** Type of SQL statement to build. */

  private StringBuffer   sqlstr = new StringBuffer(220);
  private String         tablename = "";
  private ColumnObject[] column = null;
  private KeyObject[]    key = null;
  private String         orderby = "";
  private int            operationtype;

  /**
   * @param inOpType integer specifying operation type (ADD, MODIFY or DELETE)
   * @param isTableName String containing table name.
   * @param iapColumns Array of column, and values.
   * @param iapKeys Array of key names and values.
   * @param isOrderBy order by clause
   * @exception SQLException
   */
  public SQLObject(int inOpType, String isTableName, ColumnObject[] iapColumns,
      KeyObject[] iapKeys, String isOrderBy)
  {
    this(inOpType, isTableName, iapColumns, iapKeys);
    orderby = isOrderBy;
  }
  
  /**
   * @param inOpType integer specifying operation type (ADD, MODIFY or DELETE)
   * @param isTableName String containing table name.
   * @param iapColumns Array of column, and values.
   * @param iapKeys Array of key names and values.
   * @exception SQLException
   */
  public SQLObject(int inOpType, String isTableName, ColumnObject[] iapColumns,
                   KeyObject[] iapKeys)
  {
    this(inOpType, isTableName, iapColumns);
    key = iapKeys;
    orderby = " ";
  }

  /**
   *  Same functionality as other constructor. This constructor is for
   *  convenience.
   */
  public SQLObject(int inOpType, String isTableName, ColumnObject[] iapColumns)
  {
    super();
    operationtype = inOpType;
    tablename = isTableName;
    column = iapColumns;
    key = null;
    orderby = " ";
  }

  /**
   * Figures out which type of SQL statement to build and calls the
   * appropriate method. <I> Note: if key is specified as null in the
   * constructor and the operation is DELETE, it is assumed the whole table
   * needs to be deleted!</I>
   *
   * @return String containing SQL statement.
   * @exception none
   */
  public StringBuffer buildSQL()
  {
    if (sqlstr.length() > 0) sqlstr.delete(0, sqlstr.length());
    
    switch(operationtype)
    {
      case SQLObject.ADD:
        buildInsertStatement();
        break;

      case SQLObject.MODIFY:
        buildUpdateStatement();
        break;

      case SQLObject.READ:
        buildSelectStatement();
        break;

      case SQLObject.DELETE:
        buildDeleteStatement();
    }

    return(sqlstr);
  }

  /**
   *  Builds Insert statement from Column Object array containing field
   *  specifications.
   */
  private void buildInsertStatement()
  {
    String       currentColumnName  = "";
    Object       currentColumnValue = null;
    StringBuffer colnames           = new StringBuffer(100);
    StringBuffer colvalues          = new StringBuffer(100);
    int          totalColumnCount   = column.length;

    sqlstr.append("INSERT INTO ").append(tablename).append(" ");

    colnames.append("(");              // StringBuffer for Column Names portion
    colvalues.append("(");             // StringBuffer for Column Values portion

                                       // Iterate through the column objects
                                       // array to gather SQL information.
    for(int i = 0; i < totalColumnCount; i++)
    {
                                       // The column name as a String type.
      currentColumnName = column[i].getColumnName();
                                       // The column value as an Object type.
      currentColumnValue = column[i].getColumnValue();

                                       // Accumulate column names and append a
                                       // comma if needed.
      colnames.append(currentColumnName);
      if (i != totalColumnCount - 1) colnames.append(", ");

/*---------------------------------------------------------------------------
   Check data type of the column.  Convert the Object currentColumnValue to the
   right string representation before building the SQL statement.  For a string
   column value we need quotes around it; for integer and date types we don't.
  ---------------------------------------------------------------------------*/
      int dataType = DBInfo.getFieldType(currentColumnName);
      if (dataType == DBInfo.UNKNOWN_TYPE)
      {
        // Guess based upon passed in data
        if (currentColumnValue == null) {
          dataType = Types.VARCHAR;
        }
        else if (currentColumnValue instanceof Date) {
          dataType = Types.DATE;
        }
        else if (currentColumnValue instanceof Integer) {
          dataType = Types.INTEGER;
        }
        else if (currentColumnValue instanceof String) {
          dataType = Types.VARCHAR;
        }
        else if (currentColumnValue instanceof Boolean) {
          dataType = Types.INTEGER;
          currentColumnValue = (boolean)currentColumnValue ? 1 : 0;
        }
      }
      String sCurrentColumnValue = getColumnValueAsString(dataType,
                                                          currentColumnName,
                                                          currentColumnValue);

                                       // Accumulate column values and append
                                       // a comma if needed.
      if (dataType != Types.VARCHAR)
      {
        colvalues.append(sCurrentColumnValue);
      }
      else
      {
        if (sCurrentColumnValue.equalsIgnoreCase("NULL"))
        {
          colvalues.append("NULL");
        }
        else
        {
          String varCharValue = sCurrentColumnValue.replaceAll("'", "''");
          colvalues.append("'")
                   .append(varCharValue)
                   .append("'");
        }
      }
      if (i != totalColumnCount-1) colvalues.append(", ");
    }

    colnames.append(")");
    colvalues.append(")");
                                    // Merge accumulated column names and
                                    // values to form SQL.
    sqlstr.append(colnames).append(" VALUES ").append(colvalues);

    currentColumnName  = null;
    currentColumnValue = null;
    colnames = colvalues = null;
    column = null;
  }

  /**
   *  Builds Update SQL statement based on column, and key, value
   *  specification.
   */
  private void buildUpdateStatement()
  {
    int    totalColumnCount   = column.length;
    String currentColumnName  = "";
    Object currentColumnValue = null;

    sqlstr.append("UPDATE ").append(tablename).append(" SET ");

    for(int i = 0; i < totalColumnCount; i++)
    {
      currentColumnName = column[i].getColumnName();
      currentColumnValue = column[i].getColumnValue();

      sqlstr.append(currentColumnName).append(" = ");

                                       // Build Column value as a string.
      int dataType = DBInfo.getFieldType(currentColumnName);
      if (dataType == DBInfo.UNKNOWN_TYPE)
      {
        // Guess based upon passed in data
        if (currentColumnValue == null) {
          dataType = Types.VARCHAR;
        }
        else if (currentColumnValue instanceof Date) {
          dataType = Types.DATE;
        }
        else if (currentColumnValue instanceof Integer) {
          dataType = Types.INTEGER;
        }
        else if (currentColumnValue instanceof String) {
          dataType = Types.VARCHAR;
        }
        else if (currentColumnValue instanceof Boolean) {
          dataType = Types.INTEGER;
          currentColumnValue = (boolean)currentColumnValue ? 1 : 0;
        }
      }
      String sCurrentColumnValue = getColumnValueAsString(dataType,
                                                          currentColumnName,
                                                          currentColumnValue);

      if (dataType != Types.VARCHAR)
      {
        sqlstr.append(sCurrentColumnValue);
      }
      else
      {
        if (sCurrentColumnValue.equalsIgnoreCase("NULL"))
        {
          sqlstr.append("NULL");
        }
        else
        {
          String varCharValue = sCurrentColumnValue.replaceAll("'", "''");
          sqlstr.append("'").append(varCharValue).append("'");
        }
      }
      if (i != totalColumnCount-1) sqlstr.append(", ");
    }

    sqlstr.append(DBHelper.buildWhereClause(key));
    currentColumnName = null;
    currentColumnValue = null;
  }


  /**
   *  Builds Select SQL statement based on column, and key, value
   *  specification.
   */
  private void buildSelectStatement()
  {
    
    String currentColumnName  = "";

    sqlstr.append("SELECT ");

    if (column == null || column.length == 0)
    {
        sqlstr.append("* ");
    }
    else
    {
      int    totalColumnCount   = column.length;
      for(int i = 0; i < totalColumnCount; i++)
      {
        currentColumnName = column[i].getColumnName();

        sqlstr.append(currentColumnName);

        if (i != totalColumnCount-1)
        {
            sqlstr.append(", ");
        }
      }
    }

    sqlstr.append(" FROM ").append(tablename);
    if (key != null && key.length != 0)
    {
        sqlstr.append(DBHelper.buildWhereClause(key));
    }

    if( orderby.trim().length() > 0)
    {
        sqlstr.append(" ORDER BY ").append(orderby);
    }

    currentColumnName = null;
  }

  /**
   *  Builds Delete SQL statement based on key, and value specification
   */
  private void buildDeleteStatement()
  {
    sqlstr.append("DELETE FROM ").append(tablename)
          .append(DBHelper.buildWhereClause(key));
  }

  /**
   *  Returns the columnValue object from a ColumnObject object as a string.
   */
  private String getColumnValueAsString(int dataType, String columnName,
                                        Object columnValue)
  {
    String sColumnValue;

    if (columnValue == null || columnValue.toString().trim().length() == 0)
    {
      sColumnValue = "NULL";
    }
    else if (dataType == Types.VARCHAR)
    {
      sColumnValue = columnValue.toString();
    }
    else if (dataType == Types.DATE)
    {
      try
      {
        sColumnValue = DBHelper.convertDateToDBString((Date)columnValue);
      }
      catch(ClassCastException exc)    // Oops! Bad data passed, try to convert
      {                                // object to regular string and go on.
        if (columnValue instanceof String && ((String)columnValue).length() == 0)
        {
          // an example of this is OrderHeaderData.setOrderedTime(null) will end
          // up over here as an empty string.  NULL is a valid SQL value for this
          // case - if the column they are setting does not accept null, it will
          // throw an exception - so this should be ok
          sColumnValue = "NULL";
        }else
        {
          // consider throwing an exception here instead of just logging this case
          sColumnValue = columnValue.toString();
          Logger.getLogger().logError("UNEXPECTED date value setting to: \"" 
              + sColumnValue +"\" - SQLObject.getColumnValueAsString()");
        }
      }
    }
    else if (dataType == DBInfo.MSSQL_DATETIMEOFFSET)
    {
      sColumnValue = OffsetDateTimeUtil.toString((Date)columnValue);
    }
    else
    {
      sColumnValue = columnValue.toString();
    }

    return(sColumnValue);
  }

  @Override
  public void finalize()
  {
    tablename =  null;
    column = null;
    key =  null;
  }
}
