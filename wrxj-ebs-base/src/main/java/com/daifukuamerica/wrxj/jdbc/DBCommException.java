package com.daifukuamerica.wrxj.jdbc;

import java.sql.SQLException;

/**
 * Database independent communication exception.  This exception should be
 * thrown any time a JDBC SQL Exception is thrown due to a connection problem
 * with the Database.
 * 
 * @author A.D.
 * @since  26-Nov-2007
 */
@SuppressWarnings("serial")
public class DBCommException extends RuntimeException
{
  String msDBIdentifier;
  
  public DBCommException()
  {
    super();
  }

  public DBCommException(String isMessage)
  {
    super(isMessage);
  }
  
  public DBCommException(String isMessage, String isDBIdentifier)
  {
    super(isMessage);
    msDBIdentifier = isDBIdentifier;
  }
  
  public DBCommException(SQLException ipExc)
  {
    this(ipExc.getMessage(), ipExc);
  }
  
  public DBCommException(String isMessage, SQLException ipExc)
  {
    super(isMessage, ipExc);
  }
  
  public String getDBIdentity()
  {
    return(msDBIdentifier);
  }
}
