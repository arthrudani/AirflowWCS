package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.jdbc.sqlserver.OffsetDateTimeUtil;

/**
 * Title:        Warehouse Rx
 * Description:
 * Copyright:    Copyright (c) 2002-2008
 * Company:      Daifuku America Corporation
 */

import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class that deals with data base result sets. This processes the result set
 * returned from the query and does whatever data transformations are needed.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class DBResultSet
{
  private int mnRowCount;
  private int mnDBStatus;
  // This could be an int, long, or String depending upon the database/table
  private Object mpGeneratedKey;
  private ResultSetMetaData mpRSMD;
  private List<Map> mpFields = new ArrayList<Map>();
  private Iterator<Map> mpFieldIter = mpFields.iterator();
  private List<String> mpColumns = new ArrayList<>();

  private boolean mzRefreshIterator = true;
  private SimpleDateFormat mpSDF = new SimpleDateFormat();   // Date
  private SimpleDateFormat mpTSSDF = new SimpleDateFormat(); // Timestamp

  /**
   * Create default data base result sets.
   */
  public DBResultSet()
  {
    mnRowCount = 0;
    mnDBStatus = 0;
    mpSDF.applyPattern(SKDCConstants.dbOutDateFormat);
    mpTSSDF.applyPattern(SKDCConstants.dbOutTimeStampFormat);
  }

  /**
   * Method to set the row count.
   *
   * @param inRowCount Value to set row count to.
   */
  public void setRowCount(int inRowCount)
  {
    if (inRowCount < 0)
    {
      System.out.println("Trying to set row count to negative value: "
          + inRowCount);
    }
    mnRowCount = inRowCount;
  }

  /**
   * Method to get the rows.
   *
   * @return List of fields in row.
   */
  public List<Map> getRows()
  {
    return mpFields;
  }

  /**
   * Method to get the row count.
   *
   * @return int containing number of rows affected.
   */
  public int getRowCount()
  {
    return mnRowCount;
  }

  /**
   * Method to get the status of query.
   *
   * @return int status of query.
   */
  public int getStatus()
  {
    return mnDBStatus;
  }

  /**
   * Method to clear the row count.
   */
  public void clearRowCount()
  {
    mnRowCount = 0;
  }

  /**
   * Method to increment the row count.
   */
  public void incrementRowCount()
  {
    mnRowCount = mnRowCount + 1;
  }

  /**
   * Get the key that was generated as part of an insert
   * 
   * @return
   */
  public Object getGeneratedKey()
  {
    return mpGeneratedKey;
  }

  /**
   * Set the key that was generated as part of an insert
   * 
   * @return
   */
  public void setGeneratedKey(Object ipGeneratedKey)
  {
    mpGeneratedKey = ipGeneratedKey;
  }

  /**
   * Method to see if there is another object.
   *
   * @return boolean of <code>true</code> if another object exists.
   */
  public boolean hasNext()
  {
    if (mzRefreshIterator)
    {
      mpFieldIter = mpFields.iterator();
      mzRefreshIterator = false;
    }
    return mpFieldIter.hasNext();
  }

  /**
   * Method to return next object.
   *
   * @return object Next object.
   */
  public Object next()
  {
    if (mzRefreshIterator)
    {
      mpFieldIter = mpFields.iterator();
      mzRefreshIterator = false;
    }
    return mpFieldIter.next();
  }

  /**
   * Method to add the next row from the result set. This processes the result
   * set returned from the query and does whatever data transformations are
   * needed.
   *
   * @param ipResultSet Result set from the query.
   * @exception DBException
   */
  public void addRow(ResultSet ipResultSet) throws DBException
  {
    try
    {
      if (mnRowCount == 0) // first time only
      {
        mpRSMD = ipResultSet.getMetaData();
        mpColumns.clear();
        for (int i = 1; i <= mpRSMD.getColumnCount(); i++)
        {
          mpColumns.add(mpRSMD.getColumnLabel(i));
        }
      }

      int vnFieldCount = mpRSMD.getColumnCount();
      Map<String, Object> vpFieldMap = new HashMap<String, Object>();
      for (int i = 1; i <= vnFieldCount; i++)
      {
        Object vpObj = ipResultSet.getObject(i);
        String vsColumnLabel;
        if (mpRSMD.getColumnLabel(i).length() > 0)
        {
          vsColumnLabel = mpRSMD.getColumnLabel(i).toUpperCase();
        }
        else
        {
          vsColumnLabel = mpRSMD.getColumnName(i).toUpperCase();
        }
//        System.out.println("Field(" + i + ") Col Label = " + vsColumnLabel
//            + ", object as string: " + vpObj.toString());
        // determine jdbc data type
        int vnJDBCType = mpRSMD.getColumnType(i);
        switch (vnJDBCType)
        {
          case Types.TINYINT:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,Integer.valueOf(0));
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,Integer.valueOf(vpObj.toString()));
            }
            break;
          case Types.SMALLINT:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,Integer.valueOf(0));
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,Integer.valueOf(vpObj.toString()));
            }
            break;
          case Types.INTEGER:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,Integer.valueOf(0));
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,Integer.valueOf(vpObj.toString()));
            }
            break;
          case Types.BIGINT:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,Long.valueOf(0));
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,Long.valueOf(vpObj.toString()));
            }
            break;
          case Types.FLOAT:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,Double.valueOf(0.0));
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,Double.valueOf(vpObj.toString()));
            }
            break;
          case Types.DOUBLE:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,Double.valueOf(0.0));
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,Double.valueOf(vpObj.toString()));
            }
            break;
          case Types.NUMERIC:
            if (ipResultSet.wasNull())
            {
              // need to check columnName also since things like SUM don't seem to
              // return any precision or scale
              if ((mpRSMD.getScale(i) > 0 ) || (mpRSMD.getColumnName(i).startsWith("f") ||
                  mpRSMD.getColumnName(i).startsWith("F")))
                vpFieldMap.put(vsColumnLabel,Double.valueOf(0.0));
              else
                vpFieldMap.put(vsColumnLabel,Integer.valueOf(0));
            }
            else
            {
//              if ((rsmd.getScale(i) == 0) && (rsmd.getPrecision(i) == 0))
//              {
//                System.out.println("rsmd.scale = " + rsmd.getScale(i) + ", precision = " + rsmd.getPrecision(i) );
//                System.out.println("rsmd.columnName = " + rsmd.getColumnName(i) +"ob.string = " + ob.toString() );
//              }
//              if (rsmd.getScale(i) > 0 )
              // need to check columnName also since things like SUM don't seem to
              // return any precision or scale
              if ((mpRSMD.getScale(i) > 0) ||
                  (mpRSMD.getColumnName(i).startsWith("f") ||
                   mpRSMD.getColumnName(i).startsWith("F")))
              {
                vpFieldMap.put(vsColumnLabel,Double.valueOf(vpObj.toString()));
              }
              else
              {
                vpFieldMap.put(vsColumnLabel,Integer.valueOf(vpObj.toString()));
              }
            }
            break;
          case Types.DECIMAL:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,Double.valueOf(0.0));
              System.out.println("Unsupported NULL Type: DECIMAL");
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,Double.valueOf(vpObj.toString()));
            }
            break;
          case Types.CHAR:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,"");
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,vpObj.toString());
            }
            break;
          case Types.NCHAR:
          case Types.VARCHAR:
          case Types.NVARCHAR:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,"");
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,vpObj.toString());
            }
            break;
          case Types.LONGVARCHAR:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,"");
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,vpObj.toString());
            }
            break;
          case Types.DATE:
            java.util.Date retDate;
            if (ipResultSet.wasNull())
            {
              retDate = null;
            }
            else
            {
              retDate = ipResultSet.getTimestamp(i);
            }
            vpFieldMap.put(vsColumnLabel,retDate);
            break;
           case Types.TIME:
            java.util.Date retTime;
            if (ipResultSet.wasNull())
            {
              retTime = null;
            }
            else
            {
              try
              {
                retTime = mpSDF.parse(vpObj.toString());
              }
              catch(ParseException e)
              {
                e.printStackTrace(System.out);
                System.out.println("Error " + e + " Getting Time field");
                retTime = null;
              }
            }
            vpFieldMap.put(vsColumnLabel,retTime);
            break;
          case Types.TIMESTAMP:
            java.util.Date retTSDate;
            if (ipResultSet.wasNull())
            {
              retTSDate = null;
            }
            else
            {
//              String temp = rs.getTimestamp(i).toString();
              retTSDate = ipResultSet.getTimestamp(i);
            }
            vpFieldMap.put(vsColumnLabel,retTSDate);
            break;
            // SQL Server datetimeoffset
          case DBInfo.MSSQL_DATETIMEOFFSET:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel,null);
            }
            else
            {
              vpFieldMap.put(vsColumnLabel,
                  OffsetDateTimeUtil.toDate(vpObj.toString()));
            }
            break;
          case Types.BLOB:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel, null);
            }
            else
            {                          // Put reference to BLOB Locator (reference)
              vpFieldMap.put(vsColumnLabel, vpObj);
            }
            break;
          case Types.CLOB:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel, null);
            }
            else
            {                          // Put reference to CLOB Locator (reference)
              vpFieldMap.put(vsColumnLabel, vpObj);
            }
            break;
          case Types.BIT:
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel, Boolean.FALSE);
            }
            else
            {
              vpFieldMap.put(vsColumnLabel, Boolean.valueOf(vpObj.toString()));
            }
            break;
          case Types.VARBINARY:
//            System.out.println("Unsupported JDBC Type: VARBINARY");
            // This should probably be a byte[] instead, but this should work for now
            if (ipResultSet.wasNull())
            {
              vpFieldMap.put(vsColumnLabel, null);
            }
            else
            {
              vpFieldMap.put(vsColumnLabel, vpObj.toString());
            }
            break;
          case Types.REAL:
            System.out.println("Unsupported JDBC Type: REAL, Field=" + vsColumnLabel);
            break;
          case Types.BINARY:
            System.out.println("Unsupported JDBC Type: BINARY, Field=" + vsColumnLabel);
            break;
          case Types.LONGVARBINARY:
            System.out.println("Unsupported JDBC Type: LONGVARBINARY, Field=" + vsColumnLabel);
            break;
          case Types.NULL:
            System.out.println("Unsupported JDBC Type: NULL, Field=" + vsColumnLabel);
            break;
          case Types.OTHER:
            System.out.println("Unsupported JDBC Type: OTHER, Field=" + vsColumnLabel);
            break;
          case Types.JAVA_OBJECT:
            System.out.println("Unsupported JDBC Type: JAVA_OBJECT, Field=" + vsColumnLabel);
            break;
          case Types.DISTINCT:
            System.out.println("Unsupported JDBC Type: DISTINCT, Field=" + vsColumnLabel);
            break;
          case Types.STRUCT:
            System.out.println("Unsupported JDBC Type: STRUCT, Field=" + vsColumnLabel);
            break;
          case Types.ARRAY:
            System.out.println("Unsupported JDBC Type: ARRAY, Field=" + vsColumnLabel);
            break;
          case Types.REF:
            System.out.println("Unsupported JDBC Type: REF, Field=" + vsColumnLabel);
            break;
          default :
            System.out.println("Unsupported JDBC Type: Type=" + vnJDBCType + ", Field=" + vsColumnLabel);
        }  //switch

      } // for
      incrementRowCount();

      mpFields.add(vpFieldMap);
      mzRefreshIterator = true;
    }
    catch (SQLException e)
    {
      throw new DBException(e.getClass() + ": " + e.getMessage());
    }
  }
  
  /**
   * Get the column names
   * @return
   */
  public List<String> getColumns() throws DBException
  {
    return mpColumns;
  }
}
