package com.daifukuamerica.wrxj.dbadapter;

/*
                    Daifuku America Corporation
                       International Center
                   5202 Douglas Corrigan Way
                Salt Lake City, Utah  84116-3192
                        (801) 359-9900

   This software is furnished under a license and may be used and copied only
   in accordance with the terms of such license. This software or any other
   copies thereof in any form, may not be provided or otherwise made available,
   to any other person or company without written consent from Daifuku America
   Corporation.

   Daifuku America Corporation assumes no responsibility for the use or
   reliability of software which has been modified without approval.
*/

// +Start_Doc
// ***********************************************************************
// # DB Helper Class - Class for helping DB functionality
// ***********************************************************************
//
//
//  Copyright (c) 2001, By SKDC Corp.
//  All Rights Reserved
//
//
// Created on September 13, 2001
//
// ------------------------- Basic DB Helper Class -------------
//
// -End_Doc

import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A helper class that provides methods functionality that is used by the
 * servers and the data base objects.
 *
 * @author avt
 * @version 1.0
 */
public class DBHelper
{
  private static final ReentrantLock mpObjLock = new ReentrantLock();

  /**
   * Default constructor
   */
  private DBHelper()
  {
    super();
  }

  //   Public Methods for DB Helper Object

  /**
   *  Method to throw a data base exception.
   *
   *  @param s Exception data message.
   *  @exception DBException
   */
  public static int dbThrow(String s) throws DBException
  {
     throw new DBException(s);
  }

  /**
   * Retrieves string from map.
   *
   * <p><b>Details:</b> This method searches the given map for an entry that
   * matches the given key: first with the key unmodified, and then with the key
   * set to all upper and all lower case.  If any of these keys matches an entry
   * in the map, the entry's value is converted to a string and returned.  If no
   * matching entry is found, an empty string is returned.</p>
   *
   * @param hm the map
   * @param s the key
   * @return the string value
   */
  public static String getStringField(Map hm, String s)
  {
    String sTmp = "";
    Object ob = hm.get(s);
    if (ob == null)
    {
      ob = hm.get(s.toUpperCase());
    }
    if(ob == null)
    {
      ob = hm.get(s.toLowerCase());
    }
    if (ob != null)
    {
      sTmp = ob.toString();
    }
    return(sTmp);
  }

  /**
   *  Method to get a date object from a Map.
   *
   *  @param hm Map to look in.
   *  @param s Entry to match.
   *  @return Date containing data from matched object.
   */
  public static Date getDateField(Map hm, String s)
  {
    Date retDate;
    retDate = (Date)hm.get(s.toUpperCase());
    if (retDate == null)
    {
      retDate = (Date)hm.get(s);
    }
    if (retDate == null)
    {
      retDate = (Date)hm.get(s.toLowerCase());
    }

    return(retDate);
  }

  /**
   *  Method to get a int from a Map.
   *
   *  @param hm Map to look in.
   *  @param s Entry to match.
   *  @return int containing data from matched object.
   */
  public static int getIntegerField(Map hm, String s)
  {
    Integer retInt;
    retInt = (Integer)hm.get(s.toUpperCase());
    if (retInt == null)
    {
      retInt = (Integer)hm.get(s);
    }
    if (retInt == null)
    {
      retInt = (Integer)hm.get(s.toLowerCase());
    }

    return(retInt.intValue());
  }
  
  /**
   *  Method to get a long from a Map.
   *
   *  @param hm Map to look in.
   *  @param s Entry to match.
   *  @return long containing data from matched object.
   */
  public static long getLongField(Map hm, String s)
  {
    Long retInt;
    retInt = (Long)hm.get(s.toUpperCase());
    if (retInt == null)
    {
      retInt = (Long)hm.get(s);
    }
    if (retInt == null)
    {
      retInt = (Long)hm.get(s.toLowerCase());
    }

    return(retInt.intValue());
  }

  /**
   *  Method to get a double from a Map.
   *
   *  @param hm Map to look in.
   *  @param s Entry to match.
   *  @return double containing data from matched object.
   */
  public static double getDoubleField(Map hm, String s)
  {
    Double retDoub;
    retDoub = (Double)hm.get(s.toUpperCase());
    if (retDoub == null)
    {
      retDoub = (Double)hm.get(s);
    }
    if(retDoub == null)
    {
      retDoub = (Double)hm.get(s.toLowerCase());
    }

    return(retDoub.doubleValue());
  }

  /**
   * Method to build the "where" clause portion of an SQL statement based on
   * the Key Array passed in.
   *
   * @param iapKeys Array of KeyObjects to be used in creating a where clause.
   * @return String containing the where clause.
   */
  public static String buildWhereClause(KeyObject[] iapKeys)
  {
    return buildWhereClause(iapKeys, "", false);
  }

  
  /**
   * Convenience method to build where clause for SQL statement.
   * @param iapKeys Array of KeyObjects to be used in creating a where clause.
   * @param izSuppressWhereKeyword boolean indicating if <b>WHERE</b> keyword should
   *        be excluded from the returned statement.
   * @return
   */
  public static String buildWhereClause(KeyObject[] iapKeys, boolean izSuppressWhereKeyword)
  {
    return buildWhereClause(iapKeys, "", izSuppressWhereKeyword);
  }

  /**
   * Method to build the "where" clause portion of an SQL statement based on the Key
   * Array passed in.  An option is provided to suppress the <b>WHERE</b> keyword
   * if desired.
   *
   * @param iapKeys Array of KeyObjects to be used in creating a where clause.
   * @param isTableAlias abbreviated alias for a table as used in an SQL query.
   * @param izSuppressWhereKeyword boolean indicating if <b>WHERE</b> keyword should
   *        be excluded from the returned statement.
   * @return String containing the where clause.
   */
  public static String buildWhereClause(KeyObject[] iapKeys, String isTableAlias,
                                        boolean izSuppressWhereKeyword)
  {
    StringBuffer vpSQLStr = new StringBuffer(" ");
                                       // They don't necessarily have to provide
                                       // a key.
    if (iapKeys == null || iapKeys.length == 0)
    {
      return("");                      // Return nothing case.
    }
    else
    {
      boolean vzAddWhereClause = !izSuppressWhereKeyword;

      for(int i = 0; i < iapKeys.length; i++)
      {
        if (iapKeys[i] == null ||
            iapKeys[i].equalsValue(SKDCConstants.ALL_STRING) ||
            iapKeys[i].equalsValue(Integer.valueOf(SKDCConstants.ALL_INT)))
        {
          continue;
        }

        if (vzAddWhereClause)
        {
          vzAddWhereClause = false;
          vpSQLStr.append(" WHERE ");
        }
        else if (i > 0)
        {                              // If it wasn't given, default it to "AND".
          if (iapKeys[i].getConjunctionString().length() == 0)
          {
            iapKeys[i].setConjunction(KeyObject.AND);
          }
          vpSQLStr.append(iapKeys[i].getConjunctionString());
        }

        if (isTableAlias.trim().length() == 0)
          vpSQLStr.append(iapKeys[i].getSQLString());
        else
          vpSQLStr.append(isTableAlias).append(".").append(iapKeys[i].getSQLString());
      }
    }

    return(vpSQLStr.toString());
  }

  
  /**
   * Method to build the "where" clause portion of an SQL statement based on
   * the Key Array passed in.
   *
   * @param iapKeys Array of KeyObjects to be used in creating a where clause.
   * @return String containing the where clause.
   */
  public static String buildWhereClauseFromRowID( int inRowID)
  {
    return buildWhereClauseFromRowID(inRowID, "", false);
  }

  
  /**
   * Convenience method to build where clause for SQL statement.
   * @param iapKeys Array of KeyObjects to be used in creating a where clause.
   * @param izSuppressWhereKeyword boolean indicating if <b>WHERE</b> keyword should
   *        be excluded from the returned statement.
   * @return
   */
  public static String buildWhereClauseFromRowID( long inRowID, boolean izSuppressWhereKeyword)
  {
    return buildWhereClauseFromRowID(inRowID, "", izSuppressWhereKeyword);
  }

  /**
   * Method to build the "where" clause portion of an SQL statement based on the Key
   * Array passed in.  An option is provided to suppress the <b>WHERE</b> keyword
   * if desired.
   *
   * @param iapKeys Array of KeyObjects to be used in creating a where clause.
   * @param isTableAlias abbreviated alias for a table as used in an SQL query.
   * @param izSuppressWhereKeyword boolean indicating if <b>WHERE</b> keyword should
   *        be excluded from the returned statement.
   * @return String containing the where clause.
   */
  public static String buildWhereClauseFromRowID( long inRowID, String isTableAlias,
                                        boolean izSuppressWhereKeyword)
  {
    StringBuffer vpSQLStr = new StringBuffer(" ");
                                       
   
      boolean vzAddWhereClause = !izSuppressWhereKeyword;
      if (vzAddWhereClause)
      {
          vpSQLStr.append(" WHERE ");
      }
      vpSQLStr.append(AbstractSKDCDataEnum.ID.getName()).append(" = ").append(inRowID).append(" ");

    return(vpSQLStr.toString());
  }

  
  
  /**
   * Build an Order By clause, because sometimes I want to order my results
   * without writing my own SQL.
   * @param iasOrderBy
   * @return
   */
  public static String buildOrderByClause(String... iasOrderBy)
  {
    StringBuffer vsOrderBy = new StringBuffer();
    if (iasOrderBy.length > 0)
    {
      vsOrderBy.append(" ORDER BY ");
      int i = 0;
      while (i < iasOrderBy.length)
      {
        String s = iasOrderBy[i];
        vsOrderBy.append(s);
        i++;
        if (i < iasOrderBy.length)
          vsOrderBy.append(",");
      }
    }
    return vsOrderBy.toString();
  }

  /**
   * Method to validate and convert a Date object into a string that is
   * acceptable input to the database.
   *
   * @param ipDate Date to be converted.
   * @return String containing data to be input to the database.
   */
  public static String convertDateToDBString(Date ipDate)
  {
    String vsRtnDate = "NULL";
    if (ipDate != null)
    {
      mpObjLock.lock();
      try
      {
                                       // SimpleDateFormat is not thread-safe!

        if (DBInfo.USING_ORACLE_DB)
        {
          SimpleDateFormat vpSDF = new SimpleDateFormat(SKDCConstants.DateFormatString);
          vsRtnDate = "TO_TIMESTAMP('" + vpSDF.format(ipDate) + "', '" + SKDCConstants.dbInDateFormat + "')";
        }
        else if (DBInfo.USING_SQL_SERVER)
        {
          SimpleDateFormat vpSDF = new SimpleDateFormat(SKDCConstants.dbOutTimeStampFormat);
          vsRtnDate =  "CONVERT(DATETIME2, '" + vpSDF.format(ipDate) + "', 126)";
        }
      }
      finally
      {
        if (mpObjLock.isLocked())
          mpObjLock.unlock();
      }
    }

    return vsRtnDate;
  }

  /**
   * Helper method for building starting and ending date conditional SQL. This
   * method will ignore the milliseconds field in the given beginning and ending
   * date.
   *
   * @param sDateFieldName <code>String</code> containing Date Field name.
   * @param begDate <code>Date</code> containing beginning date of date range.
   * @param endDate <code>Date</code> containing ending date of date range.
   *
   * @return <code>StringBuffer</code> containing SQL with date range.
   */
  public static StringBuffer getDateRangeSQL(String sDateFieldName, Date begDate,
                                             Date endDate)
          throws ParseException
  {
    StringBuffer dateCondition = new StringBuffer(80);
    Calendar beginCalDateTime = Calendar.getInstance();
    Calendar endingCalDateTime = Calendar.getInstance();

    beginCalDateTime.setTime(begDate);
    endingCalDateTime.setTime(endDate);
    beginCalDateTime.clear(Calendar.MILLISECOND);
    endingCalDateTime.clear(Calendar.MILLISECOND);

    begDate = beginCalDateTime.getTime();
    endDate = endingCalDateTime.getTime();

    if (begDate == null && endDate == null)
    {
      throw new ParseException("Both Begin Date and End Date missing!", 2);
    }
    else if (begDate == null)
    {
      String sEndDate = DBHelper.convertDateToDBString(endDate);
      dateCondition.append(sDateFieldName).append(" <= ").append(sEndDate);
    }
    else if (endDate == null)
    {
      String sBeginDate = DBHelper.convertDateToDBString(begDate);
      dateCondition.append(sDateFieldName).append(" >= ").append(sBeginDate);
    }
    else if (begDate.equals(endDate))
    {
      String sBeginDate = DBHelper.convertDateToDBString(begDate);
      dateCondition.append(sDateFieldName).append(" = ").append(sBeginDate);
    }
    else
    {
      String sBeginDate = DBHelper.convertDateToDBString(begDate);
      String sEndDate = DBHelper.convertDateToDBString(endDate);
      dateCondition.append(sDateFieldName).append(" BETWEEN ");

      if (begDate.after(endDate))
        dateCondition.append(sEndDate).append(" AND ").append(sBeginDate);
      else
        dateCondition.append(sBeginDate).append(" AND ").append(sEndDate);
    }

    return(dateCondition);
  }

  /**
   *  Method to convert an List of Maps to an List of AbstractSKDCData objects.
   *
   * @param ipList <code>List</code> of Map's.
   * @param ipClass Class literal reference of type <code>AbstractSKDCData</code>.
   *         This tells us how to convert the data.
   *
   * <strong>Note:</strong> For large data-sets this method is clearly not
   * very efficient and will be noticeably slow since we are doing two levels
   * of conversion before handing the data back to the requester
   * (JDBC-->List[Map]-->List[AbstractSKDCData]).
   * @return List of Type data.
   */
  public static <Type extends AbstractSKDCData> List<Type> convertData(
                                         List<Map> ipList, Class<Type> ipClass)
  {
    if (ipList == null || ipList.isEmpty())
    {
      return(new ArrayList<Type>());
    }

    int vnListSize = ipList.size();
    List<Type> vpNewList  = new ArrayList<Type>(vnListSize);
    Type vpDataObj = Factory.create(ipClass);
                                       // Extract the Map from each row and
                                       // build a new List.
    for(int vnIdx = 0; vnIdx < vnListSize; vnIdx++)
    {
      vpDataObj.dataToSKDCData(ipList.get(vnIdx));
      vpNewList.add((Type)vpDataObj.clone());
    }

    return(vpNewList);
  }

  /**
   * Type-safe method for getting an object array of columns from a
   * List&lt;AbstractSKDCData&gt;
   * @param ipList  Data list.
   * @param ipArrayClass Array type meta-class (eg. String, Date, Integer etc.)
   * @param isColumnName Data class type that extends AbstractSKDCData
   * @return Array of type specified.
   */
  public static <T extends Object, E extends AbstractSKDCData> T[] getArray(List<E> ipList,
                                                     Class<T> ipArrayClass,
                                                     String isColumnName)
  {
    int vnIdx = 0;
    T[] vapArr = (T[])Array.newInstance(ipArrayClass, ipList.size());

    for(E vpData : ipList)
    {
      vapArr[vnIdx++] = (T)ColumnObject.getValueByName(isColumnName, vpData.getColumnArray());
    }

    return vapArr;
  }

  /**
   * Method retrieves a BLOB object into memory for a database system.
   *
   * @param sHostDBIdentifier <code>String</code> containing database
   *          identifier. This is mainly useful when reading a CLOB from a Host
   *          database system. If left empty, the wrxj database is used.
   * @param sBlobColumnName String name of the column.
   * @param sqlCmd contains SQL string.
   * @return byte array of BLOB data.
   * @throws com.daifukuamerica.wrxj.common.jdbc.DBException
   */
  public static byte[] readBlob(String sHostDBIdentifier, String sBlobColumnName,
                         String sqlCmd) throws DBException
  {
    DBObject dbobj;
    if (sHostDBIdentifier.trim().length() == 0)
      dbobj = new DBObjectTL().getDBObject();
    else
      dbobj = new DBObjectTL().getDBObject(sHostDBIdentifier);

    byte[] vabBlobData = null;
    Statement jdbcStatement = null;
    ResultSet rset = null;
    BufferedInputStream istream = null;

    try
    {
      jdbcStatement = dbobj.getJDBCStatement();
      rset = jdbcStatement.executeQuery(sqlCmd);
/*--------------------------------------------------------------------------
               Get the BLOB Locator from which to read data.
  --------------------------------------------------------------------------*/
      if (!rset.next()) throw new DBException("No rows found...");
      Blob vpBlob = rset.getBlob(sBlobColumnName);
      vabBlobData = new byte[(int)vpBlob.length()];
                                   // Get the input stream for reading the BLOB
      istream = new BufferedInputStream(vpBlob.getBinaryStream());
      istream.read(vabBlobData, 0, vabBlobData.length);
    }
    catch(SQLException exc)
    {
      throw new DBException("Error retrieving BLOB data...", exc);
    }
    catch(IOException ioexc)
    {
      throw new DBException("Error reading from BLOB stream...", ioexc);
    }
    finally
    {
      if (istream != null) try { istream.close(); } catch(IOException ioexc) {}
      if (rset != null) try { rset.close(); } catch(SQLException rse) {}
      if (jdbcStatement != null) try { jdbcStatement.close(); } catch(SQLException sqExc) {}
      istream = null;
      rset = null;
      jdbcStatement = null;
    }

    return(vabBlobData);
  }

  /**
   * Method retrieves a CLOB object into memory for the wrxj database.
   *
   * @param isClobColumnName String name of the column.
   * @param isSQL contains SQL string.
   * @return byte array of CLOB data.
   * @throws DBException
   */
  public static byte[] readClob(String isClobColumnName, String isSQL)
         throws DBException
  {
    return(readClob("", isClobColumnName, isSQL));
  }

  /**
   * Method retrieves a CLOB object into memory for a database system.
   *
   * @param isHostDBIdentifier <code>String</code> containing database
   *          identifier. This is mainly useful when reading a CLOB from a Host
   *          database system. If left empty, the wrxj database is used.
   * @param isClobColumnName String name of the column.
   * @param isSQL contains SQL string.
   * @return byte array of CLOB data.
   * @throws DBException
   */
  public static byte[] readClob(String isHostDBIdentifier, String isClobColumnName,
                                String isSQL) throws DBException
  {
    DBObject vpDBObj;
    if (isHostDBIdentifier.trim().length() == 0)
      vpDBObj = new DBObjectTL().getDBObject();
    else
      vpDBObj = new DBObjectTL().getDBObject(isHostDBIdentifier);

    byte[] vabXMLData = null;
    Statement vpJDBCStmt = null;
    ResultSet vpResultSet = null;
    BufferedInputStream vpBufStream = null;

    try
    {
      vpJDBCStmt = vpDBObj.getJDBCStatement();
      vpResultSet = vpJDBCStmt.executeQuery(isSQL);
/*--------------------------------------------------------------------------
               Get the CLOB Locator from which to read data.
  --------------------------------------------------------------------------*/
      if (!vpResultSet.next()) throw new DBException("No rows found...");
      Clob vpClobRef = vpResultSet.getClob(isClobColumnName);
      vabXMLData = new byte[(int)vpClobRef.length()];
                                   // Get the input stream for reading the CLOB
      vpBufStream = new BufferedInputStream(vpClobRef.getAsciiStream());

      /*
       * Note: we could have used the read(buf, offset, length) call from the
       * BufferedInputStream.  But then we have to manage the offset and length
       * parameters for each successive read.  Using the read() call from the
       * BufferedInputStream is just as efficient since we are transferring from
       * one byte array to another, and BufferedInputStream manages the stream
       * data and its read offsets internally as it sees fit.
       */
      int vnByte = 0;
      int vnIdx  = 0;
      while((vnByte = vpBufStream.read()) != -1)
      {
        vabXMLData[vnIdx++] = (byte)vnByte;
      }
    }
    catch(SQLException exc)
    {
      throw new DBException("Error retrieving CLOB data...", exc);
    }
    catch(IOException ioexc)
    {
      throw new DBException("Error reading from CLOB stream...", ioexc);
    }
    finally
    {
      if (vpBufStream != null)
        try { vpBufStream.close(); } catch(IOException ioexc) {}
      if (vpResultSet != null)
        try { vpResultSet.close(); } catch(SQLException rse) {}
      if (vpJDBCStmt != null)
        try { vpJDBCStmt.close(); } catch(SQLException sqExc) {}
      vpBufStream = null;
      vpResultSet = null;
      vpJDBCStmt = null;
    }

    return(vabXMLData);
  }

  /**
   * Method to reconnect to a database.
   *
   * @param isDBIdentifier the identity of the database for which a connection
   *          will be retried.
   * @param inMaxRetries the number of times the connection should be retried.
   * @param ipLogger Logger to use to record errors
   *
   * @return boolean <code>true</code> if the reconnection was successful.
   */
  public static boolean dbReconnect(String isDBIdentifier, int inMaxRetries,
      Logger ipLogger)
  {
    DBObject vpDBObj;
    boolean vzReconnect = false;

    vpDBObj = new DBObjectTL().getDBObject(isDBIdentifier);
    for (int vnTries = 0; vnTries < inMaxRetries; vnTries++)
    {
      try
      {
        vpDBObj.disconnect();
        vpDBObj = new DBObjectTL().getDBObject(isDBIdentifier);

        if (vpDBObj != null)
        {
          vpDBObj.connect();
          vzReconnect = vpDBObj.isConnectionActive();
          if (vzReconnect) break;
        }
        else
        {
          break;
        }
      }
      catch (DBException ex)
      {
        ipLogger.logException(ex, "Error connecting to " + isDBIdentifier
            + " (Attempt #" + (vnTries + 1) + " of " + inMaxRetries + ")");
      }

      try { Thread.sleep(8000L); } catch(InterruptedException ie) {}
    }

    return(vzReconnect);
  }

  /**
   * Create an order ID based upon the current date/time
   *
   * @return
   */
  public static String createOrderIDByDateTime(String isPrefix)
  {
    Calendar vpCalendar = Calendar.getInstance();
    String vsYear  = SKDCUtility.preZeroFill(vpCalendar.get(Calendar.YEAR) - 2000, 2);
    String vsMonth = SKDCUtility.preZeroFill(vpCalendar.get(Calendar.MONTH) + 1, 2);
    String vsDay   = SKDCUtility.preZeroFill(vpCalendar.get(Calendar.DAY_OF_MONTH), 2);
    String vsHour  = SKDCUtility.preZeroFill(vpCalendar.get(Calendar.HOUR_OF_DAY), 2);
    String vsMin   = SKDCUtility.preZeroFill(vpCalendar.get(Calendar.MINUTE), 2);
    String vsSec   = SKDCUtility.preZeroFill(vpCalendar.get(Calendar.SECOND), 2);
    String vsMSec  = SKDCUtility.preZeroFill(vpCalendar.get(Calendar.MILLISECOND), 3);

    String vsOrderID = isPrefix + vsYear + vsMonth + vsDay + vsHour + vsMin + vsSec + vsMSec;

    int vnOrderLength = DBInfo.getFieldLength(OrderHeaderData.ORDERID_NAME);
    if (vsOrderID.length() > vnOrderLength)
    {
      vsOrderID = vsOrderID.substring(0, vnOrderLength);
    }
    return vsOrderID;
  }
}
