package com.daifukuamerica.wrxj.dbadapter;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2009 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation.  ANY USE OR EXPLOITATION OF THIS WORK
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBCommException;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBLargeResultSet;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.jdbc.StoredProcedureParameter;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Description:<BR>
 * Base class for the Database Interface classes. This class provides a standard
 * implementation for many of the required functions such as adding modifying
 * and deleting records in the database.
 *
 * @author A.D.
 * @version 1.0 05/28/02
 */
public class BaseDBInterface implements ModelInterface
{
  private final String msDBInterfacePackName = BaseDBInterface.class.getPackage().getName();
  public static final String ROW_COUNT_NAME = "ROWCOUNT";
  public static final String ROW_COUNT_SQL = "SELECT COUNT(*) AS \""
                                                + ROW_COUNT_NAME + "\" FROM ";

  // Database logging
  private static boolean mzFullDbLogging = Application.getBoolean("DatabaseLogging", false);
  private static String msCheckString = Application.getString("ClassComparisonString", "com.daifukuamerica");

  private Logger mpLogger;
  private String msWriteTableName;
  private String msReadTableName;
  private AbstractSKDCData mpDataObject;
  private DBObject mpDBObj;
  private SQLObject mpSQLObj;

  // Large result sets
  private DBLargeResultSet mpLargeResultSet = null;

  /**
   * Constructor with no specific table
   */
  public BaseDBInterface()
  {
    super();
    mpLogger = Logger.getLogger();
    mpDBObj = new DBObjectTL().getDBObject();
  }
  
  public BaseDBInterface(DBObject dbo)
  {
	  super(); 
	  mpLogger = Logger.getLogger(); 
	  mpDBObj = dbo; 
  }

  /**
   * Constructor with read/write table
   * <BR><B>NOTE:</B>The data class for this table is expected to be
   * isWriteTableName + Data.  If it is not, use a different constructor.
   *
   * @param isWriteTableName
   */
  public BaseDBInterface(String isWriteTableName)
  {
    this(isWriteTableName, isWriteTableName, null);
  }

  /**
   * Constructor with separate read and write tables (usually read view and
   * write table).
   * <BR><B>NOTE:</B>The data class for this table is expected to be
   * isWriteTableName + Data.  If it is not, use a different constructor.
   *
   * @param isWriteTableName
   * @param isReadViewName
   */
  public BaseDBInterface(String isWriteTableName, String isReadViewName)
  {
    this(isWriteTableName, isReadViewName, null);
  }

  /**
   * Constructor with separate read and write tables (usually read view and
   * write table) and a specified data object.
   *
   * @param isWriteTableName
   * @param isReadViewName
   */
  public BaseDBInterface(String isWriteTableName, String isReadViewName,
      AbstractSKDCData ipData)
  {
    this();

    msWriteTableName = isWriteTableName;
    msReadTableName = isReadViewName;
    if (ipData == null)
    {
      mpDataObject = getDataObject();
    }
    else
    {
      mpDataObject = ipData.clone();
    }
  }

  /*========================================================================*/
  /*  Public methods go in this section.                                    */
  /*========================================================================*/

  /**
   * Set the maximum number of rows to return (based upon properties file)
   */
  public void setMaxRows()
  {
    mpDBObj.setMaxRows();
  }

  /**
   * Set the maximum number of rows to return
   *
   * @param inMaxRows - The number of rows to return (must be > 0)
   */
  public void setMaxRows(int inMaxRows)
  {
    mpDBObj.setMaxRows(inMaxRows);
  }

  /**
   * Retrieves one record using unique key.
   *
   * @param ipData <code>AbstractSKDCData</code> object.  This object should
   *        have Key information in it already.
   * @param inLockFlag integer whose value is WRITELOCK or NOWRITELOCK.
   *
   * @return reference to object containing current record. <code>null</code>
   *         reference if no records found.
   * @exception DBException
   */
  @Override
  public <Type extends AbstractSKDCData> Type getElement(Type ipData,
      int inLockFlag) throws DBException
  {
    String vsTable = (inLockFlag == DBConstants.WRITELOCK) ? msWriteTableName
                                                           : msReadTableName;
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(vsTable);
    if (inLockFlag == DBConstants.WRITELOCK)
    {
      if (DBInfo.USING_ORACLE_DB)
      {
        vpSql.append(DBHelper.buildWhereClause(ipData.getKeyArray()));
        vpSql.append(" FOR UPDATE");
      }
      else if (DBInfo.USING_SQL_SERVER)
      {
    	
    	  long mnRowID = getRowIdForElement(ipData);
    	  if( mnRowID == 0)
    	  {
    		  return null;
    	  }
    	  else if( mnRowID > 0 )  
    	  {
    		  vpSql.append(" WITH (UPDLOCK) WHERE ")
                   .append(DBHelper.buildWhereClauseFromRowID(mnRowID, true));
    	  }
    	  else
    	  {
    		  vpSql.append(" WITH (UPDLOCK) WHERE ")
    	 	  	   .append(DBHelper.buildWhereClause(ipData.getKeyArray(), true));
    	  }
      }
      else
      {
        vpSql.append(DBHelper.buildWhereClause(ipData.getKeyArray()));
        vpSql.append(" FOR UPDATE");
      }
    }
    else
    {
      vpSql.append(DBHelper.buildWhereClause(ipData.getKeyArray()));
    }

    Type vpResultData = null;
    List<Map> vpResultList = fetchRecords(vpSql.toString());

    if (vpResultList.size() > 0)
    {
      if (vpResultList.size() > 1)
      {
        mpDataObject.dataToSKDCData(vpResultList.get(0));
        String vsRecord0 = mpDataObject.toString();

        mpDataObject.dataToSKDCData(vpResultList.get(1));
        String vsRecord1 = mpDataObject.toString();

        throw new DBException(vpResultList.size() + " records returned for a "
            + "single line query!  Query is:\n" + vpSql
            + "\n\nRecord 0:\n" + vsRecord0 + "\n\nRecord 1:\n" + vsRecord1
            + "\n\n(may be more records)\n\n");
      }

      // Turn DB Data into Data class format.
      mpDataObject.dataToSKDCData(vpResultList.get(0));
      vpResultData = (Type)mpDataObject.clone();
      vpResultList = null;
    }

    return vpResultData;
  }

  
  /**
   * Retrieves RowID of record using unique key.
   *
   * @param ipData <code>AbstractSKDCData</code> object.  This object should
   *        have Key information in it already.
   *
   * @return rowID to object containing current record. <code>null</code>
   *         reference if no records found.
   * @exception DBException
   */
  
 
  public <Type extends AbstractSKDCData> long getRowIdForElement(Type ipData) throws DBException
  {
	    String vsTable = msReadTableName;
	    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(vsTable);
	    
	    	vpSql.append(DBHelper.buildWhereClause(ipData.getKeyArray()));
	    

	    Type vpResultData = null;
	    List<Map> vpResultList = fetchRecords(vpSql.toString());
	    
	    if (vpResultList.size() > 0)
	    {
	    	if (vpResultList.size() > 1)
	    	{
	    		mpDataObject.dataToSKDCData(vpResultList.get(0));
	    		String vsRecord0 = mpDataObject.toString();

	    		mpDataObject.dataToSKDCData(vpResultList.get(1));
	    		String vsRecord1 = mpDataObject.toString();

	    		throw new DBException(vpResultList.size() + " records returned for a "
	    				+ "single line query!  Query is:\n" + vpSql
	    				+ "\n\nRecord 0:\n" + vsRecord0 + "\n\nRecord 1:\n" + vsRecord1
	    				+ "\n\n(may be more records)\n\n");
	    	}

	    	long mnID = DBHelper.getLongField(vpResultList.get(0), AbstractSKDCDataEnum.ID.getName());
	    	return mnID;
	    }

	    return 0;
  }
  
  
  /**
   * Retrieves List of all records matching some criteria.
   *
   * @param ipKey <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   *
   * @return List of data.
   */
  @Override
  public List<Map> getAllElements(AbstractSKDCData ipKey) throws DBException
  {
    // Clear out SQL String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT * FROM ").append(msReadTableName)
               .append(DBHelper.buildWhereClause(ipKey.getKeyArray()))
               .append(DBHelper.buildOrderByClause(ipKey.getOrderByColumns()));

    return fetchRecords(vpSql.toString());
  }

  /**
   * Method gets records with selected columns specified in ColumnObjects
   * contained in {@link AbstractSKDCData AbstractSKDCData}
   * @param ipData Data object representing columns and search keys.
   * @return List of Maps with each Map being the DB Row that was read.
   * @throws DBException if there is a database access error.
   */
  public List<Map> getSelectedColumnElements(AbstractSKDCData ipData)
         throws DBException
  {
    List<Map> vpRtnList;
    ColumnObject[] vpSelectColumns = ipData.getColumnArray();
    KeyObject[] vpKeys = ipData.getKeyArray();

    if (vpSelectColumns.length == 0)
      vpRtnList = getAllElements(ipData);
    else
      vpRtnList = readAllData(vpSelectColumns, vpKeys, msReadTableName,
          DBHelper.buildOrderByClause(ipData.getOrderByColumns()));

    return vpRtnList;
  }

  /**
   * Return the values from a single column
   *
   * @param isColumnName
   * @param ipKey
   * @param isAllOrNone
   * @return
   * @throws DBException
   */
  public String[] getSingleColumnValues(String isColumnName, boolean izDistinct,
      AbstractSKDCData ipKey, String isAllOrNone) throws DBException
  {
    String vsDistinct = izDistinct ? "DISTINCT " : "";

    StringBuilder vpSql = new StringBuilder("SELECT ").append(vsDistinct).append(isColumnName)
               .append(" FROM ").append(msReadTableName)
               .append(DBHelper.buildWhereClause(ipKey.getKeyArray()))
               .append(DBHelper.buildOrderByClause(ipKey.getOrderByColumns()));

    return getList(vpSql.toString(), isColumnName, isAllOrNone);
  }

  /**
   * Method returns an array of column values. No search criteria is currently
   * applicable.
   *
   * @param isColumnName the column name for which to return values. <b>Note:</b>
   *            The values will be sorted by this column also.
   * @param isAllNonePrepender Constant string value of
   *            {@link com.daifukuamerica.wrxj.util.SKDCConstants#ALL_STRING ALL_STRING},
   *            {@link com.daifukuamerica.wrxj.util.SKDCConstants#NONE_STRING NONE_STRING},
   *            {@link com.daifukuamerica.wrxj.util.SKDCConstants#EMPTY_VALUE EMPTY_VALUE}
   * @throws DBException if there is a database access error.
   * @return Array of distinct column values.
   */
  public String[] getDistinctColumnValues(String isColumnName,
      final String isAllNonePrepender) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(isColumnName).append(" FROM ")
               .append(msReadTableName).append(" ORDER BY ").append(isColumnName);

    return getList(vpSql.toString(), isColumnName, isAllNonePrepender);
  }

  /**
   * Method to add a row of data.
   *
   * @param ipAddData <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @exception DBException if Database Add error.
   */
  @Override
  public void addElement(AbstractSKDCData ipAddData) throws DBException
  {
    if (mzFullDbLogging)
    {
      Throwable t = new Throwable();
      String vsString  = stackTraceArrayToString(t.getStackTrace());
      ipAddData.setAddMethod(vsString);
      ipAddData.setModifyTime(new Date());
    }
    addData(ipAddData.getColumnArray(), msWriteTableName);
  }

  /**
   * Method to add a row of data to a table with a db-generated key.
   *
   * @param ipAddData <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @exception DBException if Database Add error.
   */
  public Object addElementWithKey(AbstractSKDCData ipAddData) throws DBException
  {
    if (mzFullDbLogging)
    {
      Throwable t = new Throwable();
      String vsString  = stackTraceArrayToString(t.getStackTrace());
      ipAddData.setAddMethod(vsString);
      ipAddData.setModifyTime(new Date());
    }
    return addData(ipAddData.getColumnArray(), msWriteTableName).getGeneratedKey();
  }

  /**
   * Method to modify a row of data. Normally used if there are multiple columns
   * that need to be changed.
   *
   * @param ipModData <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @exception DBException
   */
  @Override
  public void modifyElement(AbstractSKDCData ipModData) throws DBException
  {
    if (mzFullDbLogging)
    {
      Throwable t = new Throwable();
      String vsString = stackTraceArrayToString(t.getStackTrace());
      ipModData.setUpdMethod(vsString);
      ipModData.setModifyTime(new Date());
    }
    
    if (DBInfo.USING_SQL_SERVER)
    {
    	int mnCount = getCount(ipModData);
    	if( mnCount > 1 )
    	{
    		modifyData(ipModData.getColumnArray(), ipModData.getKeyArray(), msWriteTableName);
    	}
    	else
    	{
    	    long mnRowID = getRowIdForElement(ipModData);
  	        if( mnRowID == 0)
  	        {
  	        	return;
  	        }
  	        else if( mnRowID > 0 )
  	        {
  	    	    ipModData.clearKeys();
  		        ipModData.setKey(AbstractSKDCDataEnum.ID.getName(), mnRowID);
  	    	    modifyData(ipModData.getColumnArray(), ipModData.getKeyArray(), msWriteTableName);
  	        }
  	        else
  	        {
  	    	    modifyData(ipModData.getColumnArray(), ipModData.getKeyArray(), msWriteTableName);
  	        }
    	}
    }
    else if (DBInfo.USING_ORACLE_DB)
    {
        modifyData(ipModData.getColumnArray(), ipModData.getKeyArray(), msWriteTableName);
    }
    else
    {
        modifyData(ipModData.getColumnArray(), ipModData.getKeyArray(), msWriteTableName);	
    }
  }

  /**
   * Method to delete a row of data.
   *
   * @param ipDelKey <code>AbstractSKDCData</code> object containing key
   *            information to do the lookup. If there is no key info. then we
   *            do a wild-card search.
   * @return number of rows deleted.
   * @exception DBException
   */
  @Override
  public void deleteElement(AbstractSKDCData ipDelKey) throws DBException
  {
	  if (DBInfo.USING_SQL_SERVER)
	  {
		  int mnCount = getCount(ipDelKey);
		  if( mnCount > 1 )
		  {
			  deleteData(ipDelKey.getKeyArray(), msWriteTableName);
		  }
		  else
		  {
			  long mnRowID = getRowIdForElement(ipDelKey);
			  if( mnRowID == 0 )
			  {
				  return;
			  }
			  else if( mnRowID > 0 )
			  {
				  ipDelKey.clearKeys();
				  ipDelKey.setKey(AbstractSKDCDataEnum.ID.getName(), mnRowID);
				  deleteData(ipDelKey.getKeyArray(), msWriteTableName);
			  }
			  else
			  {
				  deleteData(ipDelKey.getKeyArray(), msWriteTableName);
			  }
		  }
	  }
	  else if (DBInfo.USING_ORACLE_DB)
	  {
		  deleteData(ipDelKey.getKeyArray(), msWriteTableName);
	  }
	  else
	  {
		  deleteData(ipDelKey.getKeyArray(), msWriteTableName);	
	  }
	  
    return;
  }

  /**
   * Method to check for record existence.
   *
   * @return boolean of true if it exists, false otherwise.
   */
  @Override
  public boolean exists(AbstractSKDCData ipKey)
  {
    boolean rtn = false;
    try
    {
      rtn = getCount(ipKey) > 0;
    }
    catch (DBException e)
    {
      mpLogger.logException(e, "Record Exists");
    }

    return rtn;
  }

  /**
   * Method to get Record count.
   *
   * @param ipKey <code>AbstractSKDCData</code> containing Key
   *            specification(s).
   * @return int containing count of the records matching Key specification.
   */
  @Override
  public int getCount(AbstractSKDCData ipKey) throws DBException
  {
    StringBuilder vpSql = new StringBuilder(ROW_COUNT_SQL).append(msReadTableName)
             .append(DBHelper.buildWhereClause(ipKey.getKeyArray()));

    return getRecordCount(vpSql.toString(), ROW_COUNT_NAME);
  }

  /**
   * Method to clean up Object references. This method will <b>not</b> close
   * the DBObject connection to the database and clean up the reference; this is
   * the responsibility of the Object that initiated the connection. This method
   * will clean up all other objects it created.
   */
  @Override
  public void cleanUp()
  {
//    dbhelp       = null;
//    dbobj = null;
//    sql_stmt  = null;
//    sqlstring = null;
//    dataObject = null;
//    sTableName = null;
  }

  /*========================================================================*/
  /*  Protected methods go in this section.                                 */
  /*========================================================================*/

  /**
   * Get a logger for error reporting.
   * <BR><B>NOTE:</B> Generally speaking, errors should be thrown and handled
   * by the caller.
   */
  protected Logger getLogger()
  {
    return mpLogger;
  }

  /**
   * Get the name of our database table for writes
   * @return
   */
  protected String getWriteTableName()
  {
    return msWriteTableName;
  }

  /**
   * Get the name of our database table for reads (to support views)
   * @return
   */
  public String getReadTableName()
  {
    return msReadTableName;
  }

  /**
   * Helper method for when we have to cheat and make our own SQL.
   *
   * @param isSQL
   * @param iapParams
   * @return
   * @throws DBException
   */
  protected DBResultSet execute(String isSQL, Object... iapParams)
      throws DBException
  {
    return mpDBObj.execute(isSQL, iapParams);
  }

  /**
   * Retrieves a Date column from one of the tables.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isSql SQL string to execute
   *
   * @returns Date column as Date Object.  <code>null</code> if nothing is found.
   */
  protected Date getDateColumn(String isColumnName, String isSql) throws DBException
  {
    List<Map> dataList = fetchRecords(isSql);
    if (dataList.isEmpty())
      return (null);

    return DBHelper.getDateField(dataList.get(0), isColumnName);
  }

  /**
   * Retrieves an double column from one of the tables.  This method is meant
   * to be used to retrieve quantity values.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isSql SQL string to execute
   */
  protected double getDoubleColumn(String isColumnName, String isSql) throws DBException
  {
    List<Map> dataList = fetchRecords(isSql);
    if (dataList.size() == 0)
    {
      return(-1);
    }
    return(DBHelper.getDoubleField(dataList.get(0), isColumnName));
  }

  /**
   * Retrieves an double column from one of the tables. This method is meant to
   * be used to retrieve quantity values.
   * 
   * @param isColumnName String containing the DataBase name for the column.
   * @param isSql SQL string to execute
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected double getDoubleColumn(String isColumnName, String isSql,
      Object... iapParams) throws DBException
  {
    List<Map> dataList = fetchRecords(isSql, iapParams);
    if (dataList.size() == 0)
    {
      return (-1);
    }
    return (DBHelper.getDoubleField(dataList.get(0), isColumnName));
  }

  /**
   * Retrieves an integer column from one of the tables.  This method is meant
   * to be used to retrieve translation values like order statuses.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isSql SQL string to execute
   */
  protected int getIntegerColumn(String isColumnName, String isSql) throws DBException
  {
    return getIntegerColumn(isColumnName, isSql, (Object[])null);
  }

  /**
   * Retrieves an integer column from one of the tables.  This method is meant
   * to be used to retrieve translation values like order statuses.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isSql SQL string to execute
   * @param iapParams prepared statement parameters.
   * @return
   * @throws DBException
   */
  protected int getIntegerColumn(String isColumnName, String isSql, Object... iapParams)
      throws DBException
  {
    List<Map> dataList = fetchRecords(isSql, iapParams);
    if (dataList.size() == 0)
    {
      return -1;
    }
    return DBHelper.getIntegerField(dataList.get(0), isColumnName);
  }
  
  /**
   * Method was added to get a sequencer value
   * @param isColumnName
   * @param isSql
   * @return
   * @throws DBException
   */
  protected int getIntegerColumnWithoutMaxRows(String isColumnName, String isSql) throws DBException
  {
    return getIntegerColumnWithoutMaxRows(isColumnName, isSql, (Object[])null);
  }
  
  /**
   * Method was added to get a sequencer value using a parameter value
   * @param isColumnName
   * @param isSql
   * @param iapParams
   * @return
   * @throws DBException
   */
  protected int getIntegerColumnWithoutMaxRows(String isColumnName, String isSql,Object... iapParams) throws DBException
  {
    List<Map> dataList = fetchRecordsWithoutMaxRows(isSql, iapParams);
    if (dataList.size() == 0)
    {
      return -1;
    }
    return DBHelper.getIntegerField(dataList.get(0), isColumnName);
  }
  
  /**
   * Retrieves an String column from one of the tables.  This method is meant
   * to be used to retrieve String values like OrderID's.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isSql SQL string to execute
   *
   * @return
   *    Returns the string column as a string on success.
   *    If nothing is found it returns an empty string
   * @throws DBException
   */
  protected String getStringColumn(String isColumnName, String isSQL) throws DBException
  {
    List<Map> dataList = fetchRecords(isSQL);
    if (dataList.isEmpty())
      return ("");

    return DBHelper.getStringField(dataList.get(0), isColumnName);
  }

  /**
   * Retrieves an String column from one of the tables.  This method is meant
   * to be used to retrieve String values like OrderID's.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isSQL SQL string to execute
   * @param iapParams parameters
   *
   * @return
   *    Returns the string column as a string on success.
   *    If nothing is found it returns an empty string
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  protected String getStringColumn(String isColumnName, String isSQL,
      Object... iapParams) throws DBException
  {
    List<Map> dataList = fetchRecords(isSQL, iapParams);
    if (dataList.isEmpty())
      return ("");

    return DBHelper.getStringField(dataList.get(0), isColumnName);
  }

  /**
   *  Fills a String array with a column of data for choice lists.
   *
   * @param isSQL  SQL string.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isAllOrNone <code>String</code> specifying if the "ALL" or "NONE"
   *        string should be included in the choice list.
   * @return array of string values for a choice list.
   * @throws DBException
   */
  protected String[] getList(String isSQL, String isColumnName,
      String isAllOrNone) throws DBException
  {
    return getList(isSQL, isColumnName, isAllOrNone, (Object[])null);
  }

  /**
   * Fills a String array with a column of data for choice lists.
   *
   * @param isSQL SQL string.
   * @param isColumnName String containing the DataBase name for the column.
   * @param isAllNonePrepender One of the following constants:<ul>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#ALL_STRING
   *              SKDCConstants.ALL_STRING}
   *            which prepends the string "ALL" to the array.</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NONE_STRING
   *              SKDCConstants.NONE_STRING},
   *            which prepends the string "NONE" to the array</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#EMPTY_VALUE
   *              SKDCConstants.EMPTY_VALUE},
   *            which prepends a blank string to the array.</li>
   *        <li>{@link com.daifukuamerica.wrxj.util.SKDCConstants#NO_PREPENDER
   *              SKDCConstants.NO_PREPENDER},
   *            which means there is no prepender (no pre-defined first element)
   *            to the list.</li></ul>
   * @param iapParams array of key values.
   * @return array of values associated with isColumnName.
   * @throws DBException for DB access errors.
   */
  protected String[] getList(String isSQL, String isColumnName,
                             String isAllNonePrepender, Object... iapParams)
            throws DBException
  {
    List<Map> vpDBList = null;
    String[] vasResults = null;

    try
    {
      vpDBList = mpDBObj.execute(isSQL, iapParams).getRows();
    }
    catch(DBException exc)
    {
      System.out.println("BaseDBInterface - " + exc.getMessage());
      System.out.println(isSQL);
      exc.printStackTrace();
      throw exc;
    }
    if (vpDBList == null || vpDBList.size() == 0)
    {
      if (isAllNonePrepender.equals(SKDCConstants.ALL_STRING))
      {
        vasResults = new String[1];
        vasResults[0] = SKDCConstants.ALL_STRING;
      }
      else if (isAllNonePrepender.equals(SKDCConstants.NONE_STRING))
      {
        vasResults = new String[1];
        vasResults[0] = SKDCConstants.NONE_STRING;
      }
      else if (isAllNonePrepender.equals(SKDCConstants.EMPTY_VALUE))
      {
        vasResults = new String[1];
        vasResults[0] = "";
      }
      else
      {
        vasResults = new String[0];
      }
    }
    else
    {
      if (isAllNonePrepender.equals(SKDCConstants.ALL_STRING)  ||
          isAllNonePrepender.equals(SKDCConstants.NONE_STRING) ||
          isAllNonePrepender.equals(SKDCConstants.EMPTY_VALUE))
      {
        vasResults = new String[vpDBList.size()+1];
        vasResults[0] = (isAllNonePrepender.equals(SKDCConstants.EMPTY_VALUE))
            ? "" : isAllNonePrepender;
        for(int idx = 1; idx < vpDBList.size()+1; idx++)
        {
          vasResults[idx] = ((vpDBList.get(idx-1)).get(isColumnName)).toString();
        }
      }
      else
      {
        vasResults = new String[vpDBList.size()];

        for(int idx = 0; idx < vpDBList.size(); idx++)
        {
          vasResults[idx] = ((vpDBList.get(idx)).get(isColumnName)).toString();
        }
      }
    }

    return(vasResults);
  }

  /**
   * Backwards-compatible convenience method
   * @return
   * @throws DBException
   */
  protected List<Map> fetchRecords(String isSQL) throws DBException
  {
    return fetchRecords(isSQL, (Object[])null);
  }

  /**
   * Fetch a list of records
   * @return
   * @throws DBException
   */
  protected List<Map> fetchRecords(String isSQL, Object... iapParams)
      throws DBException
  {
    return mpDBObj.execute(isSQL, iapParams).getRows();
  }
  
  /**
   * Fetch a list of records
   * @return
   * @throws DBException
   */
  protected List<Map> fetchRecordsWithoutMaxRows(String isSQL, Object... iapParams)
      throws DBException
  {
    return mpDBObj.execute(false, isSQL, iapParams).getRows();
  }

  /**
   * Fetch a list of records as data objects
   * 
   * @param ipDatatype
   * @param isSQL
   * @param iapParams
   * @return
   * @throws DBException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected <T extends AbstractSKDCData> List<T> fetchData(T ipDatatype,
      String isSQL, Object... iapParams) throws DBException
  {
    List<Map> rawData = fetchRecords(isSQL, iapParams);
    List<T> objData = new ArrayList<>(rawData.size());
    for (Map data : rawData)
    {
      T d = (T)(ipDatatype.clone());
      d.dataToSKDCData(data);
      objData.add(d);
    }
    return objData;
  }
  
  /**
   * Fetch a list of records as data objects
   * 
   * @param ipDatatype
   * @param isSQL
   * @param iapParams
   * @return
   * @throws DBException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T extends AbstractSKDCData> List<T> getAllElementsAsData(T ipKey)
      throws DBException
  {
    List<Map> rawData = getAllElements(ipKey);
    List<T> objData = new ArrayList<>(rawData.size());
    while (!rawData.isEmpty())
    {
      Map data = rawData.remove(0);
      T d = (T)(ipKey.clone());
      d.dataToSKDCData(data);
      objData.add(d);
    }
    return objData;
  }
  
  /**
   *  Returns the count of records using the Key criteria.
   *
   * @param isSQL select statement.
   * @param isColumnName String containing the name for the column.
   * @param iapParams
   * @return
   * @throws DBException
   */
  protected int getRecordCount(String isSQL, String isColumnName, Object... iapParams)
            throws DBException
  {
    int vnRowCount = 0;

    List<Map> vpResultList = mpDBObj.execute(isSQL, iapParams).getRows();
    if (vpResultList != null && vpResultList.size() != 0)
    {
      String vsRowCount = ((vpResultList.get(0)).get(isColumnName.toUpperCase())).toString();
      vnRowCount = Integer.parseInt(vsRowCount);
    }

    return vnRowCount;
  }

  /**
   * Method to add a row of data.
   *
   * @param iapColumns Array of columns to add.
   * @param isTableName String containing the table name.
   * @exception DBException if Database Add error.
   */
  @SuppressWarnings("rawtypes")
  protected DBResultSet addData(ColumnObject[] iapColumns, String isTableName)
            throws DBException
  {
    if (iapColumns == null || iapColumns.length == 0)
    {
      throw new DBException("No columns specified.");
    }

    // Go build the SQL statement.
    mpSQLObj = new SQLObject(SQLObject.ADD, isTableName, iapColumns);
    String isInsertSQL = mpSQLObj.buildSQL().toString();
    return mpDBObj.execute(isInsertSQL, (Object[])null);
  }

  /**
   * Method to modify a row of data.
   *
   * @param iapColumns Array of columns to modify.
   * @param iapKeys Array specifying keys to find record for modification.
   * @param isTableName String containing the table name.
   * @exception DBException
   */
  protected void modifyData(ColumnObject[] iapColumns, KeyObject[] iapKeys,
      String isTableName) throws DBException
  {
    // Make sure there is data to build our SQL statement!
    if (iapKeys == null || iapKeys.length == 0)
    {
      throw new DBException("No Key columns specified.");
    }
    else if (iapColumns == null || iapColumns.length == 0)
    {
      throw new DBException("No columns specified.");
    }

    mpSQLObj = new SQLObject(SQLObject.MODIFY, isTableName, iapColumns, iapKeys);
    String vsUpdateSQL = mpSQLObj.buildSQL().toString();
    mpDBObj.execute(vsUpdateSQL, (Object[])null);

  }

  /**
   * Method to delete a row of data.
   *
   * @param iapKeys Array containing key to identify row(s) to delete.
   * @param isTableName Array containing key to identify row(s) to delete.
   * @exception DBException if there is a Delete error.
   */
  protected void deleteData(KeyObject[] iapKeys, String isTableName)
      throws DBException
  {
    if (iapKeys.length == 0)
    {
      throw new DBException("Internal Error... No key provided for delete!");
    }
    mpSQLObj = new SQLObject(SQLObject.DELETE, isTableName, null, iapKeys);
    String vsDeleteSQL = mpSQLObj.buildSQL().toString();
    mpDBObj.execute(vsDeleteSQL, (Object[])null);
  }

  /*========================================================================*/
  /* Private methods go in this section.                                    */
  /*========================================================================*/

  /**
   *  Method assumes that the data object name is [table name] + "Data".
   *
   *  @return <code>AbstractSKDCData</code> reference.
   */
  private <Type extends AbstractSKDCData> Type getDataObject()
  {
    Type vpDataObj = null;
    String vsInvokerPackName = getClass().getPackage().getName();
    String vsInvokersSuperPackName = getClass().getSuperclass().getPackage().getName();

    try
    {
      String vsDataClassName;

      if (vsInvokersSuperPackName.equals(msDBInterfacePackName))
      {                                // Class that directly extends BaseDBInterface.
        vsDataClassName = vsInvokerPackName + "." + msWriteTableName + "Data";
      }
      else
      {                                // Class that extends a baseline data class.
        vsDataClassName = msDBInterfacePackName + ".data." + msWriteTableName + "Data";
      }

      Class vpDataClass = Class.forName(vsDataClassName);
      vpDataObj = (Type)Factory.create(vpDataClass);
    }
    catch(ClassNotFoundException cnfe)
    {
      mpLogger.logException(cnfe);
      cnfe.printStackTrace();
    }

    return(vpDataObj);
  }

  /**
   * Method reads all data according to selected columns.
   *
   * @param iapCols Column Objects of the columns to read.
   * @param iapKeys Key objects of the Keys to read by.
   * @param isTableName the table name.
   * @return List of Maps with each Map being the DB Row that was read.
   */
  protected List<Map> readAllData(ColumnObject[] iapCols, KeyObject[] iapKeys,
      String isTableName, String isOrderBy) throws DBException
  {
    mpSQLObj = new SQLObject(SQLObject.READ, isTableName, iapCols, iapKeys);
    String vsSelectSQL = mpSQLObj.buildSQL().toString() + isOrderBy;
    return mpDBObj.execute(vsSelectSQL).getRows();
  }

  /**
   * Method Converts an array of StackTraceElement into a new line delimited
   * string. The new line is inserted between each StackTraceElement in the
   * array after it is converted to a string.
   *
   * @param iapStacktrace the array to be converted to a byte array
   * @return String containing the stack trace.
   */
  private String stackTraceArrayToString(StackTraceElement[] iapStacktrace)
  {
    StringBuilder vsStringBuilder = new StringBuilder();
    String vsString = null;
    if (iapStacktrace != null && iapStacktrace.length > 0)
    {
      for (StackTraceElement vpElement : iapStacktrace)
      {
        if (vpElement != null)
        {
          String vsClass = vpElement.getClassName();
          String vsMethod = vpElement.getMethodName();
          int vnLine = vpElement.getLineNumber();

          if (vsClass.contains(msCheckString))
          {
            int vntemp = vsClass.lastIndexOf(".");
            String vsTemp = vsClass.substring(vntemp + 1, vsClass.length());
            vsStringBuilder.append(vsTemp);
            vsStringBuilder.append(".");
            vsStringBuilder.append(vsMethod);
            vsStringBuilder.append("():");
            vsStringBuilder.append(vnLine);
            vsStringBuilder.append(SKDCConstants.EOL_CHAR);
          }
        }
      }
      vsString = vsStringBuilder.toString();
    }
    return vsString;
  }

  /*========================================================================*/
  /*  Large List support                                                    */
  /*========================================================================*/

  /**
   * Initialize a large record list (the results are probably greater than
   * DB_MAX_ROWS).
   *
   * @param inRowsPerPage
   * @param ipKey
   * @throws DBException
   */
  public void initializeLargeRecordList(int inRowsPerPage,
      AbstractSKDCData ipKey) throws DBException
  {
	  StringBuilder vpSql = new StringBuilder(" ");
	  if( msReadTableName.equals("Load"))
	  {
		// Clear out SQL String buffer.
		     vpSql.append("SELECT Load.*, LoadLineItem.sItem, LoadLineItem.sLot,LoadLineItem.sLineID FROM Load LEFT OUTER JOIN LoadLineItem ON load.sLoadid = loadlineitem.sLoadid ");
		     if( ipKey.getKeyArray().length > 0 )
		     {
		    	 if( ipKey.getKeyObject(LoadData.LOADID_NAME) != null)
		    	 {
		    		 vpSql.append( " WHERE Load.");
		             vpSql.append(DBHelper.buildWhereClause(ipKey.getKeyArray(), "", true));
		    	 }
		    	 else
		    	 {
		             vpSql.append(DBHelper.buildWhereClause(ipKey.getKeyArray()));
		    	 }
		     }
		     else
		     {
		    	 vpSql.append(" WHERE Load.sLoadid = LoadLineItem.sLoadid " );
		     }
		     vpSql.append(DBHelper.buildOrderByClause(ipKey.getOrderByColumns()));
	  }
	  else
	  {
		// Clear out SQL String buffer.
		     vpSql.append("SELECT * FROM ").append(msReadTableName)
		               .append(DBHelper.buildWhereClause(ipKey.getKeyArray()))
		               .append(DBHelper.buildOrderByClause(ipKey.getOrderByColumns()));
	  }
    

    initializeLargeRecordList(inRowsPerPage, vpSql.toString(), (Object[])null);
  }

  /**
   * Initialize a large record list (the results are probably greater than
   * DB_MAX_ROWS).
   *
   * @param inRowsPerPage
   * @param isSQL
   * @param iapParams
   * @throws DBException
   */
  protected void initializeLargeRecordList(int inRowsPerPage, String isSQL,
      Object... iapParams) throws DBException
  {
    if (mpLargeResultSet != null)
    {
      mpLargeResultSet.cleanUp();
      mpLargeResultSet = null;
    }
    mpLargeResultSet = mpDBObj.executeLargeSelect(isSQL, iapParams);
    mpLargeResultSet.setResultsPerPage(inRowsPerPage);
  }

  /**
   * Get the next page of results from a large result set
   *
   * @return
   * @throws DBException
   */
  public List<Map> fetchNextLargeRecordListEntries()
      throws DBException
  {
    return mpLargeResultSet.fetchNextLargeRecordListEntries();
  }

  /**
   * Close the large result set
   *
   * @return
   * @throws DBException
   */
  public void closeLargeRecordList()
  {
    try
    {
      if (mpLargeResultSet != null)
      {
        mpLargeResultSet.cleanUp();
      }
    }
    catch (DBException dbe)
    {
      mpLogger.logException("Exception closing large list", dbe);
    }
  }

  /**
   * Execute a stored procedure
   * 
   * @param isProcName
   * @param iapParams
   * @throws DBException
   */
  public StoredProcedureParameter[] executeStoredProcedure(String isProcName,
      StoredProcedureParameter... iapParams) throws DBException
  {
    return mpDBObj.executeStoreProcedure(isProcName, iapParams);
  }

  /**
   * Method to submit call to a function
   *
   * @param itReturnType return type for the function
   * @param isFunctionName stored proc to be called.
   * @param iapParams 
   */
  public <Type>Type executeFunction(Class<Type> itReturnType, String isFunctionName, Object... iapParams)
         throws DBException, DBCommException
  {
    return mpDBObj.executeFunction(itReturnType, isFunctionName, iapParams);
  }
}
