package com.daifukuamerica.wrxj.jdbc;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A class for a data base connection. A connection to the data base is
 * established when the class is instantiated.
 *
 * @author avt
 * @version 1.0
 */
public class JDBCConnection
{

  // database instance name, such as OracleDB
  private String dbName;

  // the connection pool for that database instance
  private JDBCConnectionImpl.JDBCPool connectionPool;

  // the connection
  private JDBCConnectionImpl impl;

  // JDBC statement and resultset
  private Statement stmt;
  private ResultSet rs;

  // flag: is there an open ResultSet in use?
  private boolean inUse;

 /**
  *  Method to get the data base connection.
  *
  *  @return Connection.
  */
  public Connection myConnection ()
  {
     return(impl.getConnection());
  }

 /**
  *  Create new connection to the data base.
  *
  *  @param dbName Data base to connect to.
  *
  *  @exception SQLException
  *  @exception ClassNotFoundException
  *  @exception DBException
  */
  public JDBCConnection (String isdbName)
  throws SQLException, ClassNotFoundException, DBException
  {
    dbName = isdbName;
    connectionPool = JDBCConnectionImpl.JDBCPool.getInstance();
    impl = connectionPool.getImpl(dbName, 0);
    inUse = false;
  }

 /**
  *  Create new connection to the data base with timeout.
  *
  *  @param dbName Data base to connect to.
  *  @param timeout in milliseconds
  *
  *  @exception SQLException
  *  @exception ClassNotFoundException
  *  @exception DBException
  */
  public JDBCConnection (String isdbName, long timeout)
  throws SQLException, ClassNotFoundException, DBException
  {
    dbName = isdbName;
    connectionPool = JDBCConnectionImpl.JDBCPool.getInstance();
    impl = connectionPool.getImpl(dbName, timeout);
    inUse = false;
  }

 /**
  *  Method to send a request to the database.
  *
  *  @param sqlString SQL text to send to the data base.
  *  @exception SQLException
  */
  public void sendRequest (String sqlString)
  throws SQLException
  {
    if (inUse)
    {
      closeRequest();
    }
//    impl = connectionPool.acquireImpl(dbName);
    stmt = impl.getConnection().createStatement();
    rs = stmt.executeQuery(sqlString);
    inUse = true;
  }

 /**
  *  Method to return the result set of the request.
  *
  *  @return ResultSet containing results of query.
  */
  public ResultSet getRs()
  {
    return rs;
  }

 /**
  *  Method to close request and return resources.
  *
  *  @exception SQLException
  */
  public void closeRequest() throws SQLException
  {
    rs.close();
    stmt.close();
//    connectionPool.releaseImpl(impl);
    inUse = false;
  }

 /**
  *  Method to release the connection.
  *
  *  @exception SQLException
  */
  public void releaseConnection() throws SQLException
  {
    if (inUse)
    {
      closeRequest();
    }
    connectionPool.releaseImpl(impl);
  }

 /**
  *  Method free resources when object is destroyed.
  *
  *  @exception SQLException
  */
  @Override
  protected void finalize() throws SQLException
  {
    if (inUse)
    {
      closeRequest();
    }
  }

 /**
  *  Method to get the connection URL.
  *
  *  @return String containing URL.
  *  @exception DBException
  */
  public String getUrl()
  {
    return(impl.getUrl());
  }

 /**
  *  Method to release the connection.
  *
  *  @exception SQLException
  */
  public void closeConnection() throws SQLException
  {
    if (inUse)
    {
      closeRequest();
    }

    connectionPool.closeImpl(impl);
  }

}

