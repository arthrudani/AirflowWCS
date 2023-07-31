package com.daifukuamerica.wrxj.jdbc;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.StringObfuscator;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class containing functionality for establishing and pooling data base
 * connections. Configuration of connections is defined in db.properties.
 *
 * @author avt
 * @version 1.0
 */
public final class JDBCConnectionImpl
{

  // database instance name, such as OracleDB, mySqlDB
  private final String dbName;
  private final String url;

  Connection mpDBConn; // the precious connection

  static String connectionFailureReason = null;

  static Logger logger = Logger.getLogger("JDBCConnectionImpl");


  /**
   *  Method to get whether connection failed.
   *
   *  @return boolean of <code>true</code> if it connection failed.
   */
  public static boolean getConnectionFailed()
  {
    return (connectionFailureReason != null);
  }

  /**
   *  Method to get reason connection failed.
   *
   *  @return String containing reason for failure.
   */
  public static String getConnectionFailureReason()
  {
    return connectionFailureReason;
  }

  /**
   * Private constructor
   *
   * @param isDBName
   * @param dbUrl
   * @param dbUser
   * @param dbPwd
   * @throws SQLException
   */
  JDBCConnectionImpl(String isDBName, String dbUrl, String dbUser,
      String dbPwd) throws SQLException
  {
    dbName = isDBName;
    url = dbUrl;
    mpDBConn = DriverManager.getConnection (dbUrl, dbUser, dbPwd);
    if (mpDBConn != null)
    {
      if (mpDBConn.getAutoCommit())
      {
        mpDBConn.setAutoCommit(false);
      }

      mpDBConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//      if (DBInfo.USING_ORACLE_DB)
//      {
//        mpDBConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//      }
//      else if (DBInfo.USING_SQL_SERVER)
//      {
//        /*
//         * This is a more portable way of using SQLServerConnection.TRANSACTION_SNAPSHOT.
//         * Doing it this way will keep us from having to include the SQL JDBC driver
//         * when building for an Oracle system.
//         */
//        mpDBConn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED + 4094);
//      }
    }
  }

  /**
   *  Method to get the data base name.
   *
   *  @return String containing the data base name
   */
  String getDatabaseName()
  {
    return dbName;
  }

  /**
   *  Method to get the connection URL.
   *
   *  @return String containing URL.
   *  @exception DBException
   */
  public String getUrl()
  {
    return url;
  }

  /**
   *  Method to get the data base connection.
   *
   *  @return Connection.
   */
  public Connection getConnection()
  {

    return mpDBConn;
  }

  /**
   *  Method to close a connection.
   *
   *  @exception SQLException
   */
  void close() throws SQLException
  {
    mpDBConn.close();
  }

  /**
   *  Method free resources when object is destroyed.
   *
   *  @exception SQLException
   */
  @Override
  protected void finalize() throws SQLException
  {
    close();
  }

  /**
   * A class containing functionality for pooling data base
   * connections. Configuration of connections is defined in db.properties.
   *
   * @author avt
   * @version 1.0
   */
  public static class JDBCPool
  {

    // dictionary of database names with corresponding vector of connections
    private final Map<String, List<JDBCConnectionImpl>> poolDictionary = new HashMap<String, List<JDBCConnectionImpl>>();

    // dictionary of database names with corresponding connection parameters
    private final Map<String, JDBCParms> parmsDictionary = new HashMap<String, JDBCParms>();

    // methods and attributes for Singleton pattern
    private JDBCPool() {} // private constructor
    private static JDBCPool _instance; // get class instance

    /**
     *  Method to get instance of pool. Singleton getter utilizing Double Checked Locking pattern.
     *
     *  @return JDBCPool of connections.
     */
    public static JDBCPool getInstance()
    {
      if (_instance == null)
      {
        synchronized(JDBCPool.class)
        {
           if (_instance == null)
           _instance = new JDBCPool();
        }
      }
      return _instance;
    }

    /**
     *  Method to get connection from pool with a timeout in milliseconds.
     *
     *  @param dbName Data base to get connection for.
     *  @param timeout value in milliseconds.
     *
     *  @return JDBCConnectionImpl containing connection or null if no
     *  connections are available.
     *  @exception SQLException
     *  @exception ClassNotFoundException
     *  @exception DBException
     */
    public synchronized JDBCConnectionImpl getImpl (String dbName, long timeout)
    throws SQLException, ClassNotFoundException, DBException
    {
      long localTimeout = timeout;
//      if (timeout > 0)
//      {
//        System.out.println("getImpl timeout: " + timeout);
//      }
      JDBCConnectionImpl myImpl;
      while ((myImpl = acquireImpl(dbName)) == null)
      {
        if (timeout <= 0)
        {
                // No Timeout
           throw new DBException("No available connections for " + dbName);
        }
        try
        {
            System.gc();
            wait(100);
            localTimeout = localTimeout - 100;
//            System.out.println("waiting: " + localTimeout);
        }
        catch (InterruptedException e) {}
        if (localTimeout <= 0)
        {
          // Timeout has expired
//            System.out.println("Timeout expired");
          if (connectionFailureReason == null)
          {
            connectionFailureReason = "NOT Connected to Database - NO Available Connections for \"" +
                  dbName + "\"";
          }
          throw new DBException("No available connections for " + dbName);
        }
      }
      return myImpl;
    }

    /**
     *  Method that tries to get a connection from pool.
     *
     *  @param dbName Data base to get connection for.
     *
     *  @return JDBCConnectionImpl containing connection or null if no
     *  connections are available.
     *  @exception SQLException
     *  @exception ClassNotFoundException
     *  @exception DBException
     */
    public JDBCConnectionImpl acquireImpl (String dbName)
    throws SQLException, ClassNotFoundException, DBException
    {
       // get connection parameters matching database name
      JDBCParms p = parmsDictionary.get(dbName);

      // first call to database
      if (p == null)
      {
        // process properties
        p = new JDBCParms();
        p.name = dbName;
        p.driver = Application.getString(dbName + ".driver");

        if (p.driver == null)
        {
          throw new DBException("parameters not found for " + dbName);
        }

        p.url = Application.getString(dbName + ".url");

        // if an alternate data base server was specified at run time, use it
        String dbURL = Application.getString(dbName);
        if (dbURL != null && dbURL.length() > 0)
        {
          String newURL = p.url.substring(0,p.url.indexOf('@')) + dbURL.substring(dbURL.indexOf('@'));
//          System.out.println("Changing URL from " + p.url + " to " + newURL);
          p.url = null;
          p.url = newURL;
//          System.out.println("Changed URL is " + p.url);
        }

        p.user = Application.getString(dbName + ".user");
        {
          String vsPassword = Application.getString(dbName + ".password");
          if (vsPassword != null && vsPassword.startsWith("+"))
            vsPassword = StringObfuscator.decode(vsPassword.substring(1));
          p.password = vsPassword;
        }
        String pMax = Application.getString(dbName + ".maximum");
        p.maxConn = Integer.valueOf(pMax).intValue();
        p.curConn = 0;

        // load database driver
        Class.forName (p.driver);

        // save parms
        parmsDictionary.put(dbName, p);
      }

      // get connection pool matching database name
      List<JDBCConnectionImpl> pool = poolDictionary.get(dbName);
      if (pool != null)
      {
        while (pool.size() > 0)
        {
          JDBCConnectionImpl impl = null;
          // retrieve existing unused connection
          impl = pool.get(pool.size() - 1);
          // remove connection from pool
          pool.remove(pool.size() - 1);
          // try to open a statement.
          // If session has been killed, you won't succeed so throw away this connection
          // and keep it out of the pool
          Statement statement = null;
          try
          {
            statement = impl.mpDBConn.createStatement();
            statement.setMaxRows(1);
            // Simple select statement so we really go to the database
//            if (Application.getString(DBObject.DATABASE_KEY).equals("OracleDB"))
            if (DBInfo.USING_ORACLE_DB)
            {
              statement.execute("SELECT 1 from DUAL");
              statement.close();
            }
            else if (DBInfo.USING_SQL_SERVER)
            {
              statement.execute("SELECT GETDATE()");
              statement.close();
            }
            // return connection
            return impl;
          }
          catch (SQLException e)
          {
            if (statement != null)
            {
              statement.close();
            }
            logger.logDebug("Connection for " + dbName + " is bad, throwing it away.");
            impl.close();
            impl = null;
            p.curConn--;
          }

        }
      }

      // pool is empty so create new connection
      // unless we are using a maximum
      if ((p.maxConn == 0) || (p.curConn < p.maxConn))
      {
        JDBCConnectionImpl impl = new JDBCConnectionImpl(dbName, p.url, p.user,
            p.password);
        p.curConn++;

        return impl;
      }

      // pool is empty and max connections reached, or no connections are available
       return null;
    }

    /**
     *  Method to return connection impl to pool.
     *
     *  @param impl JDBC Connection Implementation to be released.
     */
    public synchronized void releaseImpl (JDBCConnectionImpl impl)
    {
      String dbName = impl.getDatabaseName();
      List<JDBCConnectionImpl> pool = poolDictionary.get(dbName);
      if (pool == null)
      {
        pool = new ArrayList<JDBCConnectionImpl>();
        poolDictionary.put(dbName, pool);
      }
      pool.add(impl);
    }

    /**
     *  Method to return connection impl to pool.
     *
     *  @param impl JDBC Connection Implementation to be released.
     */
    public synchronized void closeImpl (JDBCConnectionImpl impl) throws SQLException
    {
      // get connection parameters matching database name
      JDBCParms p = parmsDictionary.get(impl.getDatabaseName());
      impl.close();
      p.curConn--;
    }

    /**
     *  Method to get the paramaters for a data base.
     *
     *  @param dbName Specifies which data base to get parameters for.
     *
     *  @return JDBCParms Containing the parameters for the data base
     */
    public JDBCParms getParms(String dbName)
    {
      return parmsDictionary.get(dbName);
    }

    /**
     *  Method to close out all unused pool connections for a data base.
     *
     *  @param dbName Specifies which data base to close.
     *  @exception SQLException
     */
    public void close(String dbName) throws SQLException
    {
      // get connection pool matching database name
      List<JDBCConnectionImpl> pool = poolDictionary.get(dbName);
      if (pool != null)
      {
        int size = pool.size();
//      if (size > 0)
        while (size > 0)
        {
          JDBCConnectionImpl impl = null;
          // retrieve existing unused connection
          impl = pool.get(size-1);
          // remove connection from pool
          pool.remove(size-1);
          // close connection
          impl.close();
//        System.out.println("size= " + size);
          size--;
        }
        JDBCParms p = parmsDictionary.get(dbName);
        p.curConn = 0;
      }
    }

    /**
     *  Method to close out all connections.
     *
     *  @exception SQLException
     */
    public void close() throws SQLException
    {
      Iterator<JDBCParms> parmsDictionaryIterator = parmsDictionary.values().iterator();
      while (parmsDictionaryIterator.hasNext())
      {
        JDBCParms p = parmsDictionaryIterator.next();
        close(p.name);
      }
    }
  }
}
