package com.daifukuamerica.wrxj.jdbc;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

// JDBCParms.java

/**
 * A data class that contains parameters used in connecting to the data base.
 *
 * @author avt
 * @version 1.0
 */
public class JDBCParms
{
  String name; // pool identifier
  String driver; // JDBC driver class name
  String url; // JDBC database URL
  String user; // user name for the pool
  String password; // user name's password
  int maxConn; // maximum number of connections
  int curConn; // current number of connections in use

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String s = "name = " + name +
               ": driver = " + driver +
               ": url = " + url +
               ": user = " + user +
               ": password = " + password +
               ": maxConn = " + maxConn +
               ": curConn = " + curConn;
    return s;
  }
}


