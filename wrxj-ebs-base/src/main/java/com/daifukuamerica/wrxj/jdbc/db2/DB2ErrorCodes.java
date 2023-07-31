package com.daifukuamerica.wrxj.jdbc.db2;

import com.daifukuamerica.wrxj.jdbc.DBErrorCodes;
import java.sql.SQLException;

/**
 * <B>Description:</B> Class for handling out DB2 database-specific SQL
 * syntax.
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class DB2ErrorCodes implements DBErrorCodes
{
  /*========================================================================*/
  /*  DB2-specific SQL                                                      */
  /*========================================================================*/
  
  /**
   * Get the SQL for a connection query
   * 
   * @return
   */
  @Override
  public String getConnectionQuery()
  {
    return "SELECT 1 FROM sysibm.sysdummy1";
  }
  
  /*========================================================================*/
  /*  DB2-specific error handling                                           */
  /*========================================================================*/
  
  private final String CLOSED_CONNECTION = "The connection does not exist.";
  
  /**
   * Method tests if the error code it was sent is a vendor-specific code for a
   * duplicate record insert.
   * 
   * @param ipExc Generic JDBC exception
   * @return <code>true</code> if this is a Connection problem.
   */
  @Override
  public boolean isDuplicateRecordInsert(SQLException ipExc)
  {
    return (ipExc.getErrorCode() == -803);
  }

  /**
   * Method tests if the error code it was sent is a vendor-specific code for a
   * bad database connection.
   * 
   * @param ipExc Generic JDBC exception
   * @return <code>true</code> if this is a Connection problem.
   */
  @Override
  public boolean isConnectionProblem(SQLException ipExc)
  {
    return (ipExc.getMessage().equals(CLOSED_CONNECTION));
  }

  /**
   * Method tests if the error code it was sent is a vendor-specific code for a
   * invalid resource usage like closed statements, or cursors.
   * 
   * @param ipExc Generic JDBC exception
   * @return <code>true</code> if this is a invalid resource handle problem.
   */
  @Override
  public boolean isClosedResourceHandle(SQLException ipExc)
  {
    // TODO: Implement
    return false;
  }
  
  /**
   * Method tests if the error code it was sent is a vendor-specific code for a
   * data error like data too large or wrong type for column
   * 
   * @param ipExc Generic JDBC exception
   * @return <code>true</code> if this is a invalid resource handle problem.
   */
  @Override
  public boolean isDataError(SQLException ipExc)
  {
    return false;
  }
}
