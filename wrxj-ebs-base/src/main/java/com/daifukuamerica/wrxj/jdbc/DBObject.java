package com.daifukuamerica.wrxj.jdbc;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.oracle.OracleErrorCodes;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.util.StringObfuscator;
import com.daifukuamerica.wrxj.util.UnreachableCodeException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

/**
 * A class that is used to interface to the data base. It tries to make sure
 * that SQL statements are reasonably formatted, keeps track of the connection
 * we are using and also knows whether or not we are in a transaction.
 *
 * @author avt
 * @version 1.0
 */
final public class DBObject
{

  /**
   * Key to database connection property.
   *
   * <p><b>Details:</b> <code>DATABASE_KEY</code> is set to "<tt>database</tt>",
   * the name of the application property containing the name of the database
   * connection to use.</p>
   */
  static final String DATABASE_KEY = "database";
  /**
   * Key to web client property.
   *
   * <p><b>Details:</b> <code>WEB_CLIENT</code> is set to "<tt>webclient</tt>",
   * the name of the application property containing boolean values to configure
   * web-client specific logic. Namely in connection pooling of DBObject(s)
   *
   */
  static final String WEB_CLIENT = "webclient";

  /**
   * Default database connection.
   *
   * <p><b>Details:</b> <code>DEFAULT_DATABASE_VALUE</code> is set to
   * "<tt>OracleDB</tt>", the name of the default database connection to use.
   * The default database connection is used if the application property keyed
   * by <code>DATABASE_KEY</code> is not found.</p>
   */
  static final String DEFAULT_DATABASE_VALUE = "OracleDB";
  /**
   * DML key words.
   */
  Pattern mpDMLKeyWords = Pattern.compile("\\bINSERT\\b|\\bUPDATE\\b|\\bDELETE\\b|\\bUPDLOCK\\b|\\bFOR\\p{Blank}+UPDATE\\b");


  private boolean connected = false;
  private boolean connectionErrorLogged = false;
  private TransactionToken mpStartTranObject = null;
  private TransactionToken dummy = new TransactionToken() {};
  private long elapsedTime1InMillis;
  private long elapsedTime2InMillis;
  private long startTimeInMillis;
  private String timingData = "";
  private int tranIsolationLevel = -1;
  private boolean    _DEBUG   = false;
  private boolean    _TIMING  = false;
  private static final int DB_MAX_ROWS = 8000;
  private int mnMaxRows = DB_MAX_ROWS;
  private boolean mzTransactionDebugging = false;
  private boolean mzDefaultDB = false;
  private String msModule = "";

  String myDbName;

  long myTimeout = 1000;

  JDBCConnection myJDBCConnection = null;

  private SkDateTime dataDateTime = new SkDateTime();

  private String connectionFailureReason = null;

  private Logger logger = Logger.getLogger();

  private DBErrorCodes mpDBErrorCodes;

  /**
   * Create a database object. Activates debug and timing based on command line
   * parameters passed in to the JVM.
   */
  public DBObject(String db)
  {
    myDbName = db;
    _DEBUG = Application.getBoolean("DBG", false);
    _TIMING = Application.getBoolean("TIMING", false);
    mzTransactionDebugging = Application.getBoolean(myDbName+".TransactionDebugging", false);
    msModule = Thread.currentThread().getName();
    setMaxRows();
    /*
     * Set up the error codes for the DB vendor.
     */
    try
    {
      mpDBErrorCodes = Factory.create(DBErrorCodes.class);
    }
    catch(Exception e)
    {
      logger.logException("Using default of OracleErrorCodes due to error.", e);
      mpDBErrorCodes = Factory.create(OracleErrorCodes.class);
    }
  }

  /**
   *  Create a database object using default data base name.
   */
  public DBObject()
  {
    this(Application.getString(DATABASE_KEY, DEFAULT_DATABASE_VALUE));
    mzDefaultDB = true;
  }

  /**
   * Sets error codes we will use for this connection.
   *
   * @param isErrorCodesClass the package qualified error code class name.  The
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setDBErrorCodes(String isErrorCodesClass) throws Exception
  {
    try
    {
      Class vpClass = Class.forName(isErrorCodesClass);
      if (!DBErrorCodes.class.isAssignableFrom(vpClass))
      {
        throw new Exception("Code Error!! " + isErrorCodesClass + " is not an " +
                            "implementation of DBErrorCodes!");
      }
      mpDBErrorCodes = (DBErrorCodes)Factory.create(vpClass);
    }
    catch(Exception e)
    {
      logger.logException("Error code unchanged!  Keeping default setting as Oracle " +
                          "Error Code due to exception", e);
      throw e;
    }
  }

  /**
   * Is this the default database?
   * @return boolean
   */
  public boolean isDefaultDB()
  {
    return mzDefaultDB;
  }

  /**
   * Set the maximum number of rows to return (based upon properties file)
   */
  public void setMaxRows()
  {
    String vsEnvParam = Application.getString(myDbName+".MaxRows");
    if (vsEnvParam != null && vsEnvParam.trim().length() != 0)
    {
      mnMaxRows = Integer.valueOf(vsEnvParam).intValue();
    }
    else
    {
      mnMaxRows = DB_MAX_ROWS;
    }
  }

  /**
   * Set the maximum number of rows to return
   *
   * @param inMaxRows - The number of rows to return (must be > 0)
   */
  public void setMaxRows(int inMaxRows)
  {
    if (inMaxRows > 0)
    {
      mnMaxRows = inMaxRows;
    }
  }

  /**
   * Find out how many rows we've limited ourselves to
   */
  public int getMaxRows()
  {
    return (mnMaxRows);
  }

 /**
  *  Method to throw a data base exception.
  *
  *  @param s Text to put in exception.
  *  @return int
  *  @exception DBException
  */
  private int dbThrow(String s) throws DBException
  {
     throw new DBException(s);
  }

 /**
  *  Method to throw a data base exception.
  *
  *  @param s Text to put in exception.
  *  @param duplicate True if caused by duplicate data.
  *  @return int
  *  @exception DBException
  */
  private int dbThrow(String s, boolean duplicate) throws DBException
  {
     throw new DBException(s, duplicate);
  }

 /**
  *  Method returns state variable indicating if this object is connected
  *  with the database.
  *
  *  @return boolean of <code>true</code> if connected.
  */
  // Shouldn't we call this "isConnected"?
  public boolean checkConnected()
  {
    return(connected);
  }

 /**
  * This is a test to see if our jdbc connection is usable.  The reason for this
  * test is to determine if our connection is still valid or if it is invalid
  * because the database went down unexpectedly.
  *
  * @return <code>false</code> if the connection is invalid.
  */
  public boolean isConnectionActive()
  {
    boolean vzValidConnection = false;
    Statement statement = null;

    try
    {
      if (_TIMING)
        startTimeInMillis = System.currentTimeMillis();
      statement = myJDBCConnection.myConnection().createStatement();
      statement.setMaxRows(1);
      // Simple select statement so we really go to the database
      statement.execute(mpDBErrorCodes.getConnectionQuery());
      connected = true;

      if (_TIMING)                     //For testing purposes
      {
        elapsedTime1InMillis = System.currentTimeMillis() - startTimeInMillis;
        logger.logDebug("isConnectionActive() - Select time (milli): " + elapsedTime1InMillis);
      }
      vzValidConnection = true;
    }
    catch(SQLException se)
    {
      connected = false;
      try
      {
        myJDBCConnection.closeConnection();
      }
      catch(SQLException e)
      {
        // okay to throw away since we can't close the connection
        // due to socket loss.  DB should clean up stray connections eventually.
      }
      // This is closed.  All this is for testing purposes.
    }
    finally
    {                                  // Make sure this close happens.
      if (statement != null) try { statement.close(); } catch(SQLException e) {}
    }

    return(vzValidConnection);
  }

  /**
   * Method for MonitorFrame, Login, and whomever else might want to check
   * the default WRx-J database status.
   *
   * @return
   */
  public static boolean isWRxJConnectionActive()
  {
    return isConnectionActive(Application.getString(DATABASE_KEY, DEFAULT_DATABASE_VALUE));
  }

 /**
  * This is a test to see if a jdbc connection is usable.  The reason for this
  * test is that the connection pool can have all of its connections rendered
  * invalid if the database goes down all of a sudden.  This test will at least
  * give a valid warning on whether the connection is usable.
  *
  * @param isDatabasePropertyGroup the property group name out of the wrxj-ctlrs
  *        property file that has connection related info.
  * @return <code>false</code> if the connection is invalid.
  */
  public static boolean isConnectionActive(String isDatabasePropertyGroup)
  {
    boolean vzValidConnection = false;
    Connection vpConnection = null;

    String vsURL = Application.getString(isDatabasePropertyGroup + ".url");
    String vsUserName = Application.getString(isDatabasePropertyGroup + ".user");
    String vsPassword = Application.getString(isDatabasePropertyGroup + ".password");
    if (vsPassword != null && vsPassword.startsWith("+"))
      vsPassword = StringObfuscator.decode(vsPassword.substring(1));

    try
    {
      vpConnection = DriverManager.getConnection(vsURL, vsUserName, vsPassword);
      vzValidConnection = true;
    }
    catch(SQLException se)
    {
      // This is okay since it's for testing purposes.
    }
    finally
    {                                  // Make sure this close takes place to
                                       // so that no resources are held onto.
      if (vpConnection != null)
        try { vpConnection.close(); } catch(SQLException e) {}
    }

    return(vzValidConnection);
  }

 /**
  *  Method to check if we are in a transaction.
  *
  *  @return boolean of <code>true</code> if in a transaction.
  *  @exception DBException
  */
  public boolean activeTransaction()
  {
    return (mpStartTranObject != null);
  }

 /**
  *  Method to check if transaction isolation level has changed. It should
  *  not change during the life of the connection, so we want to know if it does.
  *
  */
  private void checkTransactionLevel()
  {
    try
    {
      if (myJDBCConnection == null)
      {
        if (_DEBUG) logger.logDebug("DBObject - No JDBC Connection at execute time");  //For debug purposes
      }
      if (tranIsolationLevel != myJDBCConnection.myConnection().getTransactionIsolation())
      {
        if (tranIsolationLevel > -1)
        {
          if (_DEBUG) logger.logDebug("DBObject - Transaction Isolation changed from: " + tranIsolationLevel +
            " to: " + myJDBCConnection.myConnection().getTransactionIsolation());  //For debug purposes
        }
        tranIsolationLevel = myJDBCConnection.myConnection().getTransactionIsolation();
      }
      tranIsolationLevel = myJDBCConnection.myConnection().getTransactionIsolation();
    }
    catch (SQLException e)
    {
      logger.logError("DBObject - Transaction Isolation exception: " + e.getMessage());  //For debug purposes
    }
    catch (NullPointerException e)
    {
      logger.logError("DBObject - Transaction Isolation exception: " + e.getMessage());  //For debug purposes
    }
    return;
  }

 /**
  *  Method to connect to the data base.
  *
  *  @return boolean of <code>true</code> if we got connected.
  *  @exception DBException
  */
  public boolean connect() throws DBException
  {
    if (!connected)
    {
      connected = false;
      try
      {
        if (_DEBUG) logger.logDebug("DBObject - Connecting to Database \"" + myDbName
          + "\"" + " Thread: " + Thread.currentThread().getName());
        dataDateTime.setStartDateTime();
        myJDBCConnection = new JDBCConnection(myDbName,myTimeout);
        if (_DEBUG) logger.logDebug("DBObject - Connected OK to Database \"" +
          myDbName + ", URL: " + this.myJDBCConnection.getUrl() + "\" - Connection Time: " + dataDateTime.getElapsedDateTimeAsString());
        connected = true;
        checkTransactionLevel();
        connectionErrorLogged = false;

        setCurrentAction("Connect", false);
      }
      catch (ClassNotFoundException e)
      {
        logger.logException(e, "DBObject - Error opening Database Connection");
        connected = false;
        dbThrow(e.getClass() + ": " + e.getMessage());
      }
      catch (SQLException e)
      {
        if (!connectionErrorLogged)
        {
          logger.logException(e, "DBObject - Error opening Database Connection");
          connectionErrorLogged = true;
        }
        connected = false;
        if (connectionFailureReason == null)
        {
          connectionFailureReason = "Database Connect Failure (" + myDbName + ") - " + e.getMessage();
        }
        try { Thread.sleep(1000); } catch (InterruptedException ie) {}
        dbThrow(e.getClass() + " (" + myDbName + ") : " + e.getMessage());
      }
    }
    return (connected);
  }

 /**
  *  Method to disconnect from the data base with thread checking turned on.
  *
  *  @return boolean of <code>true</code> if we got disconnected.
  *  @exception DBException
  */
  public boolean disconnect() throws DBException
  {
    return(disconnect(true));
  }

 /**
  *  Method to disconnect from the data base .
  *
  *  @param threadCheck True if need to check what thread we are in.
  *  @return boolean of <code>true</code> if we got disconnected.
  *  @exception DBException
  */
  public boolean disconnect(boolean threadCheck) throws DBException
  {
    if (connected)
    {
      try
      {
        if ((!threadCheck) ||
          (!SwingUtilities.isEventDispatchThread()))
        {
          if (!isConnectionActive())
                    // Close the connection (remove from pool) since this is a
                    // bad connection.
            myJDBCConnection.closeConnection();
          else
                    // Release connection back to pool.
            myJDBCConnection.releaseConnection();
          if (_DEBUG) logger.logDebug("Disconnected from: " + myDbName + " Thread: " +
            Thread.currentThread().getName());  // For debug purposes

          connected = false;
        }
      }
      catch (SQLException e)
      {
        dbThrow(e.getClass() + ": " + e.getMessage());
      }
    }
    return (connected);
  }

 /**
  *  Method to start a data base transaction.
  *
  *  Oracle does not have a start transaction, but we can set inTransaction
  *  in here so that other processes know that we are in a transaction and do not
  *  commit before we want to....Also...Other databases do have a start
  *  transaction so this will just be ready for them when we need it.
  *
  *  @return TransactionToken containing control token.
  *  @exception DBException
  */
  public TransactionToken startTransaction() throws DBException
  {
    throwIfNotConnected();
    if (activeTransaction())
    {
      return dummy;
    }

    Exception e = new Exception();
    StackTraceElement[] vapSTE = e.getStackTrace();
    if (vapSTE.length > 2)
    {
      setCurrentAction((vapSTE[2]).toString(), true);
    }
    else
    {
      setCurrentAction((vapSTE[vapSTE.length-1]).toString(), true);
    }
          // Check and/or Set inTransaction so we can all check so see if we are
          // in a transaction
    if (_DEBUG) logger.logDebug( "Transaction STARTED");  //For debug purposes
    mpStartTranObject = new TransactionToken() {};

    return(mpStartTranObject);
  }

 /**
  *  Method to commit a data base transaction.
  *
  *  @param tt TransactionToken containing token returned from startTransaction.
  *
  *  @return boolean of <code>true</code> if we succeeded.
  *  @exception DBException
  */
  public boolean commitTransaction(TransactionToken tt) throws DBException
  {
    throwIfNoTransaction();

    if (isStarter(tt))
    {
      try
      {
        setCurrentAction("", true);
        myJDBCConnection.myConnection().commit();
        checkTransactionLevel();
        if (_DEBUG) logger.logDebug( "Transaction COMMITTED");  //For debug purposes
      }
      catch (SQLException e)
      {
        dbThrow(e.getClass() + ": " + e.getMessage());
      }
      mpStartTranObject = null;
      cycleConnection();
    }
    return (true);
  }

 /**
  *  Method to end a data base transaction.
  *
  *  @param tt TransactionToken containing token returned from startTransaction.
  *  This method will rollback the transaction if it has not been committed.
  *
  *  @return boolean of <code>true</code> if we succeeded.
  */
  public boolean endTransaction(TransactionToken tt)
  {
    if (activeTransaction() && isStarter(tt))
    {
      try
      {
        myJDBCConnection.myConnection().rollback();
        checkTransactionLevel();
        if (_DEBUG) logger.logDebug( "Transaction ROLLBACK");  //For debug purposes
      }
      catch (SQLException e)
      {
        logger.logError("Transaction Rollback Exception: " + e.getMessage());
      }
      mpStartTranObject = null;
      setCurrentAction("", true);
      cycleConnection();
    }
    return (true);
  }

  /**
   * Throws exception if not connected.
   *
   * <p><b>Details:</b> <code>checkConnection</code> checks whether this
   * <code>DBObject</code> is connected and throws an exception if it is not.
   * Call this method from any method where a connection must already be
   * established.</p>
   *
   * <p><b>Implementation note:</b> If it is our expectation, with property
   * written code, that no method requiring a connection will ever be called
   * before a connection has been established, it may make more sense to throw
   * an <code>IllegalStateException</code> instead of a
   * <code>DBException</code>.  We should consider this.</p>
   *
   * @throws DBException if not connected to database
   */
  private void throwIfNotConnected() throws DBException
  {
    if (! connected)
      logger.logError("Not connected to the Database--attempting to reconnect ["
          + Thread.currentThread().getName() + "]");
      if (!connect())
      {
        dbThrow("Not connected to the Database (will attempt to reconnect)");
      }
  }

  /**
   * Throws exception if no active transaction.
   *
   * <p><b>Details:</b> <code>checkTransaction</code> checks whether this
   * <code>DBObject</code> is in a transaction and throws an exception if it is not.
   * Call this method from any method where a transaction must already be
   * started.</p>
   *
   * <p><b>Implementation note:</b> If it is our expectation, with property
   * written code, that no method requiring a transaction will ever be called
   * before a transaction has been started, it may make more sense to throw
   * an <code>IllegalStateException</code> instead of a
   * <code>DBException</code>.  We should consider this.</p>
   *
   * @throws DBException if not in transaction to database
   */
  private void throwIfNoTransaction() throws DBException
  {
    if (! activeTransaction())
    {
      logger.logException(new Exception("No Active Transaction, START is missing"));
      dbThrow("No Active Transaction, START is missing");
    }
  }

  /**
   * Method to set the values of a SQL prepared statement.
   * @author karmstrong 11/10/2005
   * @param ipStmt SQL statement
   * @param iapValues Array of objects to assign to statement.  Must be
   * SQL supported objects (String, Integer, etc.)  See <code>PreparedStatement</code>
   * for further info.
   * @throws DBException if an unsupported Object type is received
   */
  private void setValues(PreparedStatement ipStmt, Object[] iapValues) throws DBException
  {
    int vnLength = iapValues.length;
    for(int vnI = 0; vnI < vnLength; ++ vnI)
    {
      try
      {
        if (iapValues[vnI] instanceof DACCLOB)
        {
          DACCLOB vpClobData = (DACCLOB)iapValues[vnI];
          try
          {
          /*
           * Note: there is a newer version of this call that does not require
           * the message length to be specified (jdk 1.6). But not all JDBC
           * drivers can be depended on to have implemented this.
           */
            ipStmt.setAsciiStream(vnI + 1, vpClobData.getByteInputStream(),
                                  vpClobData.getDataSize());
          }
          finally
          {
            vpClobData.closeStream();
          }
        }
        else
        {
          if (iapValues[vnI] instanceof Date)
          {
            Date vpDate = (Date)iapValues[vnI];
            iapValues[vnI] = new Timestamp(vpDate.getTime());
          }
          ipStmt.setObject(vnI + 1, iapValues[vnI]);
        }
      }
      catch (SQLException ex)
      {
        // Just ignore it if we go beyond the available column indexes
        if (ex.getMessage().indexOf("Invalid column index") == -1)
          throw new DBException("DBStatement.setValues(): " + ex.getMessage());
      }
    }
  }

  /**
   * Returns Database identifier.
   * @return database identifier.
   */
  public String getDBIdentifier()
  {
    return(myDbName);
  }
  
  /**
   * Sets Oracle module and action information in V$session
   *
   * @param module
   * @param action
   */
  public void setOraModule(String module, String action)
  {
    try
    {
      CallableStatement pstmt = myJDBCConnection.myConnection().prepareCall("{call DBMS_APPLICATION_INFO.SET_MODULE(?,?)}");
      pstmt.setString(1, module);
      pstmt.setString(2, action);
      int rows = pstmt.executeUpdate();
      if (rows != 1)
      {
        throw new DBException("dbms_application_info.setModule(?,?) rows was not equal to 1");
      }
      if (!activeTransaction())
      {
        myJDBCConnection.myConnection().commit();
      }
      pstmt.close();
    }
    catch (Exception e)
    {
     System.out.println(e.getMessage());
    }
  }

  /**
   * Set the current action for debugging purposes.
   *
   * <P><I><B>NOTE:</B> Currently only implemented for Oracle.</I></P>
   *
   * @param isAction
   */
  private void setCurrentAction(String isAction, boolean izIsTransaction)
  {
    if (DBInfo.USING_ORACLE_DB && izIsTransaction && mzTransactionDebugging)
    {
      String vsAction = isAction;
      if (vsAction.length() > 20)
      {
        int inEnd = vsAction.lastIndexOf("(");
        if (inEnd > 0)
        {
          vsAction = vsAction.substring(inEnd+1, vsAction.length()-1);
        }
      }
      setOraModule(msModule, vsAction);
    }
  }

  /**
   * Method to submit a SQL query to the data base. This cleans up the statement
   * if needed, determines if we are starting a transaction, does timing (if
   * enabled), convert the JDBC results of the query to the DB Result Set.
   *
   * Makes use of prepared statements - allowing use of special characters.
   * Modified 11/10/2005
   *
   * @param sqlStatement SQL statement to be submitted.
   *  @param izMaxRows max rows set for query.
   * @return DBResultSet containing the results of the query.
   * @exception DBException
   *
   */
  public DBResultSet execute(boolean izMaxRows, String sqlStatement,  Object... iapParams)
         throws DBException, DBCommException
  {
    if (sqlStatement.length() <= 0)
    {
       dbThrow("Blank statement");
    }

    throwIfNotConnected();

    String upperCaseStr = sqlStatement.toUpperCase();
    //  **A.D** String fixedString = sqlStatement.replace('"','\'');

    if (_TIMING) timingData = "";

    DBResultSet result_set = new DBResultSet();
    PreparedStatement vpStatement = null;

    StringBuilder sqlStr = new StringBuilder(sqlStatement);

    try
    {
      checkTransactionLevel();

      // clean up the end of the string
      while ((sqlStr.charAt(sqlStr.length() - 1) == ' ') ||
        (sqlStr.charAt(sqlStr.length() - 1) == ';') ||
        (sqlStr.charAt(sqlStr.length() - 1) == '\n') ||
        (sqlStr.charAt(sqlStr.length() - 1) == '\r') )
      {
        sqlStr.deleteCharAt(sqlStr.length() - 1);
      }

      // Commented out by A.D. 06-Jul-2018.  This block gets confused when
      // a column contains one of these key words such as "DLASTUPDATE "!

//      if ((upperCaseStr.indexOf("DELETE ") >= 0) ||
//         (upperCaseStr.indexOf("UPDATE ") >= 0) ||
//         (upperCaseStr.indexOf("FOR UPDATE") >= 0) ||
//         (upperCaseStr.indexOf("INSERT ") >= 0) ||
//         (upperCaseStr.indexOf("UPDLOCK" ) >= 0))
      Matcher vpMatcher = mpDMLKeyWords.matcher(upperCaseStr);
      if (vpMatcher.find())
      {
        throwIfNoTransaction();
      }

      boolean vzReturnId = false;
      if (upperCaseStr.startsWith("INSERT ")) {
        vzReturnId = true;
      }

      if (_DEBUG || _TIMING)
      {
        String vsParams =" ";
        if(iapParams != null && iapParams.length > 0)
        {
          vsParams = " Params: " + Arrays.toString(iapParams);
        }
        logger.logDebug("DBObject - SQL: " + sqlStr.toString() + vsParams);
      }
      if (_TIMING) startTimeInMillis = System.currentTimeMillis();
      if (vzReturnId)
      {
        vpStatement = myJDBCConnection.myConnection().prepareStatement(
            sqlStr.toString(), Statement.RETURN_GENERATED_KEYS);
      }
      else
      {
        vpStatement = myJDBCConnection.myConnection().prepareStatement(
            sqlStr.toString());
      }
      if(izMaxRows)
      {
        vpStatement.setMaxRows(mnMaxRows);
      }

      if(iapParams != null)
        setValues(vpStatement, iapParams);

      if (vpStatement.execute())
      {
        if (_TIMING) elapsedTime1InMillis = System.currentTimeMillis() - startTimeInMillis;
        ResultSet rs = vpStatement.getResultSet();

        while (rs.next())
        {
          result_set.addRow(rs);
        }
        rs.close();
        if (_TIMING)
        {
          elapsedTime2InMillis = System.currentTimeMillis() - startTimeInMillis;
          timingData = timingData + "Total time (milli): " + elapsedTime2InMillis +
                  ", SQL time (milli): " + elapsedTime1InMillis +
                  ", Row Count ( " + result_set.getRowCount() + "):";
        }
      }
      else
      {                                // If the DML statement fails, let
                                       // caller know something went wrong! --A.D.
        int updateCount = vpStatement.getUpdateCount();
        if (updateCount == 0)
        {
          vpStatement.close();
          throw new NoSuchElementException("SQL Update/Delete statement: No rows affected failure!");
        }

        if (vzReturnId)
        {
          ResultSet vpGenKeys = vpStatement.getGeneratedKeys();
          // Not all inserts have a generated key
          if (vpGenKeys.next())
          {
            result_set.setGeneratedKey(vpGenKeys.getObject(1));
          }
          else
          {
            result_set.setGeneratedKey(-1); // TODO: Change to constant
          }
        }
        
        if (_TIMING) elapsedTime1InMillis = System.currentTimeMillis() - startTimeInMillis;
        result_set.setRowCount(updateCount);
        if (_TIMING)
        {
          timingData = timingData +"SQL time (milli): " + elapsedTime1InMillis +
                       ", Row Count ( " + result_set.getRowCount() + "):";
        }
      }
      vpStatement.close();
      if (_TIMING) logger.logDebug(
        "Timing Stats - " + timingData);  //For debug purposes

    }
    catch (SQLException e)
    {
      String errStr = e.getMessage();
      try
      {
        if (vpStatement != null)
        {
          vpStatement.close();
        }
      }
      catch (SQLException e3)
      {
        throw new DBException(e3.getMessage(), e3);
      }
      catch (NullPointerException e3)
      {
        if (connectionFailureReason == null)
        {
          connectionFailureReason = "Database Connection FAILURE - " + e3.getMessage();
        }
        dbThrow(e3.getClass() + ": " + e3.getMessage(), false);
      }

      if (mpDBErrorCodes.isDuplicateRecordInsert(e))
      {
        dbThrow(e.getClass() + ": " + e.getMessage(), true);
      }
      else if (mpDBErrorCodes.isConnectionProblem(e))
      {
        logger.logError("Database connection is invalid. " + errStr);
        disconnect(false);
        throw new DBCommException("Database connection is invalid. " + errStr, getDBIdentifier());
      }
      else if (mpDBErrorCodes.isDataError(e))
      {
        DBErrorLogger.log(e, sqlStatement, iapParams);
        throw new DBException(e.getMessage() + "  See database error log.", e);
      }
      else
      {
//        System.err.println(sqlStatement);
//        e.printStackTrace();
        throw new DBException(e.getMessage(), e);
      }
    }
    catch (NullPointerException e)
    {
      logger.logException(e, "DBObject - execute exception");  //For debug purposes
    }
    cycleConnection();
    return (result_set);
  }
  /**
   * Method to submit a SQL query to the data base. This cleans up the statement
   * if needed, determines if we are starting a transaction, does timing (if
   * enabled), convert the JDBC results of the query to the DB Result Set.
   *
   * Makes use of prepared statements - allowing use of special characters.
   * Modified 11/10/2005
   *
   * @param sqlStatement SQL statement to be submitted.
   * @return DBResultSet containing the results of the query.
   * @exception DBException
   *
   */
  public DBResultSet execute(String sqlStatement, Object... iapParams)
         throws DBException, DBCommException
  {
     return execute(true, sqlStatement, iapParams);
  }
  
  /**
   * Cycle (close/connect) the database connection for DB Server health
   */
  private void cycleConnection()
  {
    // Cycle connections to relieve MSSQL server tempdb
    if (DBInfo.USING_SQL_SERVER)
    {

      if (!activeTransaction())
      {
        try
        {
          myJDBCConnection.myConnection().rollback();
        }
        catch (SQLException dbe)
        {
          Logger.getLogger().logException("Cycling DB connection", dbe);
          dbe.printStackTrace(System.err);
        }
      }
    }
  }

  /**
   * Method to submit a SQL query to the data base. This cleans up the statement
   * if needed, determines if we are starting a transaction, does timing (if
   * enabled), convert the JDBC results of the query to the DB Result Set.
   *
   * Makes use of prepared statements - allowing use of special characters.
   * Modified 11/10/2005
   *
   * @param sqlStatement SQL statement to be submitted.
   * @return DBLargeResultSet containing the results of the query.
   * @exception DBException
   *
   */
  public DBLargeResultSet executeLargeSelect(String sqlStatement,
      Object... iapParams) throws DBException, DBCommException
  {
    if (sqlStatement.length() <= 0)
    {
       dbThrow("Blank statement");
    }

    throwIfNotConnected();

    String upperCaseStr = sqlStatement.toUpperCase();

    // Commented out by A.D. 06-Jul-2018.  This block gets confused when
    // a column contains one of these key words such as "DLASTUPDATE "!

//    if ((upperCaseStr.indexOf("DELETE ") >= 0) ||
//        (upperCaseStr.indexOf("UPDATE ") >= 0) ||
//        (upperCaseStr.indexOf("FOR UPDATE") >= 0) ||
//        (upperCaseStr.indexOf("INSERT ") >= 0))

    Matcher vpMatcher = mpDMLKeyWords.matcher(upperCaseStr);
    if (vpMatcher.find())
    {
      dbThrow("Transactions not allowed for executeLargeSelect()");
    }

    DBLargeResultSet vpLargeResults = null;

    if (_TIMING) timingData = "";

    StringBuilder sqlStr = new StringBuilder(sqlStatement);

    PreparedStatement vpStatement = null;
    try
    {
      checkTransactionLevel();

      // clean up the end of the string
      while ((sqlStr.charAt(sqlStr.length() - 1) == ' ') ||
             (sqlStr.charAt(sqlStr.length() - 1) == ';') ||
             (sqlStr.charAt(sqlStr.length() - 1) == '\n') ||
             (sqlStr.charAt(sqlStr.length() - 1) == '\r') )
      {
        sqlStr.deleteCharAt(sqlStr.length() - 1);
      }

      if (_DEBUG || _TIMING)
        logger.logDebug("DBObject - SQL: " + sqlStr.toString());

      if (_TIMING) startTimeInMillis = System.currentTimeMillis();
      vpStatement = myJDBCConnection.myConnection().prepareStatement(sqlStr.toString());

      if(iapParams != null)
        setValues(vpStatement, iapParams);

      if (vpStatement.execute())
      {
        if (_TIMING) elapsedTime1InMillis = System.currentTimeMillis() - startTimeInMillis;
        ResultSet vpResultSet = vpStatement.getResultSet();
       // vpResultSet.setFetchDirection(ResultSet.FETCH_FORWARD);
        vpLargeResults = new DBLargeResultSet(vpStatement, vpResultSet);

        if (_TIMING)
        {
          elapsedTime2InMillis = System.currentTimeMillis() - startTimeInMillis;
          timingData = timingData
              + "Total time (milli): " + elapsedTime2InMillis
              + ", SQL time (milli): " + elapsedTime1InMillis;
//              + ", Row Count ( " + rs.getRowCount() + "):";
        }
      }
      else
      {
        throw new UnreachableCodeException("execute() did not return a ResultSet");
      }
      if (_TIMING) logger.logDebug(
        "Timing Stats - " + timingData);  //For debug purposes
    }
    catch (SQLException e)
    {
      String errStr = e.getMessage();
      try
      {
        if (vpStatement != null)
        {
          vpStatement.close();
        }
      }
      catch (SQLException e3)
      {
        throw new DBException(e3.getMessage(), e3);
      }
      catch (NullPointerException e3)
      {
        if (connectionFailureReason == null)
        {
          connectionFailureReason = "Database Connection FAILURE - " + e3.getMessage();
        }
        dbThrow(e3.getClass() + ": " + e3.getMessage(), false);
      }
      if (mpDBErrorCodes.isConnectionProblem(e))
      {
        logger.logError("Database connection is invalid. " + errStr);
        disconnect(false);
        throw new DBCommException("Database connection is invalid. " + errStr, getDBIdentifier());
      }
      else
      {
        throw new DBException(e.getMessage(), e);
      }
    }
    catch (NullPointerException e)
    {
      logger.logException(e, "DBObject - execute exception");  //For debug purposes
    }
    cycleConnection();
    return vpLargeResults;
  }

  /**
   * Method to submit call to a function
   *
   * @param itReturnType - return type for the function
   * @param isFunctionName - function to be called.
   * @param iapParams - function parameters
   */
  @SuppressWarnings("unchecked")
  public <Type>Type executeFunction(Class<Type> itReturnType, String isFunctionName, Object... iapParams)
         throws DBException, DBCommException
  {
    if (isFunctionName.length() <= 0)
    {
       dbThrow("Blank function name");
    }

    throwIfNotConnected();

    Type t = null;
    
    if (_TIMING) timingData = "";

    DBResultSet result_set = new DBResultSet();
    CallableStatement vpStatement = null;
    
    StringBuilder sqlStr = new StringBuilder("{? = call ")
        .append(isFunctionName).append("(");
    if (iapParams != null)
    {
      for (int i = 0; i < iapParams.length -1; i++)
      {
        sqlStr.append("?,");
      }
      if (iapParams.length > 0)
      {
        sqlStr.append("?");
      }
    }
    sqlStr.append(")}");
    
    try
    {
      checkTransactionLevel();

      if (_DEBUG || _TIMING)
        logger.logDebug("DBObject - SQL: " + sqlStr.toString());

      if (_TIMING) startTimeInMillis = System.currentTimeMillis();

      vpStatement = myJDBCConnection.myConnection().prepareCall(
          sqlStr.toString());
      vpStatement.setMaxRows(mnMaxRows);

      // Output/Result (parameter #1, 1-based)
      if (String.class.isAssignableFrom(itReturnType))
      {
        vpStatement.registerOutParameter(1, Types.VARCHAR);
      }
      else if (Integer.class.isAssignableFrom(itReturnType))
      {
        vpStatement.registerOutParameter(1, Types.INTEGER);
      }
      else if (Double.class.isAssignableFrom(itReturnType))
      {
        vpStatement.registerOutParameter(1, Types.DOUBLE);
      }
      else if (Date.class.isAssignableFrom(itReturnType))
      {
        vpStatement.registerOutParameter(1, Types.DATE);
      }

      // Input parameters (parameters 2+, 1-based)
      if (iapParams != null)
      {
        for (int i = 0; i < iapParams.length; i++)
        {
          Object fp = iapParams[i];
          if (fp instanceof String)
          {
            vpStatement.setString(i+2, (String)fp);
          }
          else if (fp instanceof Integer)
          {
            vpStatement.setInt(i+2, (Integer)fp);
          }
          else if (fp instanceof Double)
          {
            vpStatement.setDouble(i+2, (Double)fp);
          }
          else if (fp instanceof Date)
          {
            vpStatement.setTimestamp(i+2, new Timestamp(((Date)fp).getTime()));
          }
        }
      }

      vpStatement.execute();

      if (_TIMING) elapsedTime1InMillis = System.currentTimeMillis() - startTimeInMillis;
      
      t = (Type)vpStatement.getObject(1);

      if (_TIMING)
      {
        elapsedTime2InMillis = System.currentTimeMillis() - startTimeInMillis;
        timingData = timingData + "Total time (milli): " + elapsedTime2InMillis +
            ", SQL time (milli): " + elapsedTime1InMillis +
            ", Row Count ( " + result_set.getRowCount() + "):";
      }
      vpStatement.close();
      if (_TIMING) logger.logDebug(
        "Timing Stats - " + timingData);  //For debug purposes

    }
    catch (SQLException e)
    {
      String errStr = e.getMessage();
      try
      {
        if (vpStatement != null)
        {
          vpStatement.close();
        }
      }
      catch (SQLException e3)
      {
        throw new DBException(e3.getMessage(), e3);
      }
      catch (NullPointerException e3)
      {
        if (connectionFailureReason == null)
        {
          connectionFailureReason = "Database Connection FAILURE - " + e3.getMessage();
        }
        dbThrow(e3.getClass() + ": " + e3.getMessage(), false);
      }

      if (mpDBErrorCodes.isDuplicateRecordInsert(e))
      {
        dbThrow(e.getClass() + ": " + e.getMessage(), true);
      }
      else if (mpDBErrorCodes.isConnectionProblem(e))
      {
        logger.logError("Database connection is invalid. " + errStr);
        disconnect(false);
        throw new DBCommException("Database connection is invalid. " + errStr, getDBIdentifier());
      }
      else
      {
//        System.err.println(sqlStatement);
//        e.printStackTrace();
        throw new DBException(e.getMessage(), e);
      }
    }
    catch (NullPointerException e)
    {
      logger.logException(e, "DBObject - execute exception");  //For debug purposes
    }

    return t;
  }
  
  /**
   * Method to submit call to a stored procedure
   *
   * @param isProcName stored proc to be called.
   * @param iapParams 
   */
  public StoredProcedureParameter[] executeStoreProcedure(String isProcName, StoredProcedureParameter... iapParams)
         throws DBException, DBCommException
  {
    if (isProcName.length() <= 0)
    {
       dbThrow("Blank stored procedure name");
    }

    throwIfNotConnected();

    if (_TIMING) timingData = "";

    DBResultSet result_set = new DBResultSet();
    CallableStatement vpStatement = null;
    
    StringBuilder sqlStr = new StringBuilder("{call ")
        .append(isProcName).append("(");
    if (iapParams != null)
    {
      for (int i = 0; i < iapParams.length -1; i++)
      {
        sqlStr.append("?,");
      }
      if (iapParams.length > 0)
      {
        sqlStr.append("?");
      }
    }
    sqlStr.append(")}");
    
    try
    {
      checkTransactionLevel();

      // clean up the end of the string
      while ((sqlStr.charAt(sqlStr.length() - 1) == ' ') ||
        (sqlStr.charAt(sqlStr.length() - 1) == ';') ||
        (sqlStr.charAt(sqlStr.length() - 1) == '\n') ||
        (sqlStr.charAt(sqlStr.length() - 1) == '\r') )
      {
        sqlStr.deleteCharAt(sqlStr.length() - 1);
      }

      if (_DEBUG || _TIMING)
        logger.logDebug("DBObject - SQL: " + sqlStr.toString());

      if (_TIMING) startTimeInMillis = System.currentTimeMillis();

      vpStatement = myJDBCConnection.myConnection().prepareCall(
          sqlStr.toString());
      vpStatement.setMaxRows(mnMaxRows);

      if (iapParams != null)
      {
        for (int i = 0; i < iapParams.length; i++)
        {
          StoredProcedureParameter spp = iapParams[i];
          if (spp.getOutParam() != null)
          {
            if (spp.getOutParam() instanceof String)
            {
              vpStatement.registerOutParameter(i+1, Types.VARCHAR);
            }
            else if (spp.getOutParam() instanceof Integer)
            {
              vpStatement.registerOutParameter(i+1, Types.INTEGER);
            }
            else if (spp.getOutParam() instanceof Double)
            {
              vpStatement.registerOutParameter(i+1, Types.DOUBLE);
            }
            else if (spp.getOutParam() instanceof Date)
            {
              vpStatement.registerOutParameter(i+1, Types.DATE);
            }
          }
          if (spp.getInParam() != null)
          {
            if (spp.getInParam() instanceof String)
            {
              vpStatement.setString(i+1, (String)spp.getInParam());
            }
            else if (spp.getInParam() instanceof Integer)
            {
              vpStatement.setInt(i+1, (Integer)spp.getInParam());
            }
            else if (spp.getInParam() instanceof Double)
            {
              vpStatement.setDouble(i+1, (Double)spp.getInParam());
            }
            else if (spp.getInParam() instanceof Date)
            {
              vpStatement.setTimestamp(i+1, new Timestamp(((Date)spp.getInParam()).getTime()));
            }
          }
        }
      }

      vpStatement.execute();

      if (_TIMING) elapsedTime1InMillis = System.currentTimeMillis() - startTimeInMillis;
      if (iapParams != null)
      {
        for (int i = 0; i < iapParams.length; i++)
        {
          StoredProcedureParameter spp = iapParams[i];
          if (spp.getOutParam() != null)
          {
            spp.setOutParam(vpStatement.getObject(i + 1));
          }
        }
      }

      if (_TIMING)
      {
        elapsedTime2InMillis = System.currentTimeMillis() - startTimeInMillis;
        timingData = timingData + "Total time (milli): " + elapsedTime2InMillis +
            ", SQL time (milli): " + elapsedTime1InMillis +
            ", Row Count ( " + result_set.getRowCount() + "):";
      }
      vpStatement.close();
      if (_TIMING) logger.logDebug(
        "Timing Stats - " + timingData);  //For debug purposes

    }
    catch (SQLException e)
    {
      String errStr = e.getMessage();
      try
      {
        if (vpStatement != null)
        {
          vpStatement.close();
        }
      }
      catch (SQLException e3)
      {
        throw new DBException(e3.getMessage(), e3);
      }
      catch (NullPointerException e3)
      {
        if (connectionFailureReason == null)
        {
          connectionFailureReason = "Database Connection FAILURE - " + e3.getMessage();
        }
        dbThrow(e3.getClass() + ": " + e3.getMessage(), false);
      }

      if (mpDBErrorCodes.isDuplicateRecordInsert(e))
      {
        dbThrow(e.getClass() + ": " + e.getMessage(), true);
      }
      else if (mpDBErrorCodes.isConnectionProblem(e))
      {
        logger.logError("Database connection is invalid. " + errStr);
        disconnect(false);
        throw new DBCommException("Database connection is invalid. " + errStr, getDBIdentifier());
      }
      else
      {
//        System.err.println(sqlStatement);
//        e.printStackTrace();
        throw new DBException(e.getMessage(), e);
      }
    }
    catch (NullPointerException e)
    {
      logger.logException(e, "DBObject - execute exception");  //For debug purposes
    }

    return iapParams;
  }

  /**
   * Get the JDBC statement
   * @return Statement
   * @throws DBException
   */
  public Statement getJDBCStatement() throws DBException
  {
    Statement theStatement = null;
    try
    {
      theStatement = myJDBCConnection.myConnection().createStatement();
    }
    catch (SQLException exc)
    {
      throw new DBException("Executing JDBC statement...", exc);
    }

    return theStatement;
  }

  /**
   *  Method to get the data base meta data information.
   *
   *  @return DatabaseMetaData containing the data base meta data.
   *  @exception DBException
   */
  public DatabaseMetaData getDataBaseMetaData() throws DBException
  {
    DatabaseMetaData md = null;
    try
    {
      md = myJDBCConnection.myConnection().getMetaData();
    }
    catch (SQLException e)
    {
      dbThrow(e.getClass() + ": " + e.getMessage(), false);
    }
    return (md);
  }

 /**
  *  Method to get the connection URL.
  *
  *  @return String containing URL.
  *  @exception DBException
  */
  public String getUrl()
  {
    if (!connected)
    {
      return null;
    }
    return(this.myJDBCConnection.getUrl());
  }

  public boolean isStarter(TransactionToken tt)
  {
    if (mpStartTranObject == null) return false;
    return (mpStartTranObject.equals(tt));
  }

 /**
  *  Method to disconnect from the data base .
  *
  *  @param threadCheck True if need to check what thread we are in.
  *  @return boolean of <code>true</code> if we got disconnected.
  *  @exception DBException
  */
  public boolean totalDisconnect() throws DBException
  {
    if (connected)
    {
      try
      {
          myJDBCConnection.closeConnection();
          if (_DEBUG) logger.logDebug("Totally disconnecting from: " + myDbName + " Thread: " +
            Thread.currentThread().getName());  //For debug purposes
          connected = false;
      }
      catch (SQLException e)
      {
        dbThrow(e.getClass() + ": " + e.getMessage());
      }
    }
    return (connected);
  }

  /**
   * Restores connection to pool.
   *
   * <p><b>Details:</b> This finalizer ensures that the pooled database
   * connection associated with this instance gets restored to the pool.</p>
   */
  @Override
  protected void finalize() throws Throwable
  {
    disconnect();
  }
}

