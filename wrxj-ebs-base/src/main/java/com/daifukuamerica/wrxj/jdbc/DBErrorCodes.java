package com.daifukuamerica.wrxj.jdbc;

import java.sql.SQLException;

/**
 * Interface to Database vendor error codes.  This allows us to insulate the 
 * application from vendor-specific error codes.
 * 
 * <P>This interface now includes database-specific SQL getters.</P>
 *
 * @author A.D.
 * @version 1.0
 * @since 28-Nov-2007
 * 
 * TODO: Give this class a new name to reflect its new responsibilities
 */
public interface DBErrorCodes
{
  /**
   * Get the SQL for a connection query
   * 
   * @return
   */
  public String getConnectionQuery();
  
 /**
  * Method tests if the error code it was sent is a vendor-specific code for a
  * duplicate record insert.
  * @param ipExc Generic JDBC exception
  * @return <code>true</code> if this is a Connection problem.
  */
  public boolean isDuplicateRecordInsert(SQLException ipExc);
  
 /**
  * Method tests if the error code it was sent is a vendor-specific code for a
  * bad database connection.
  * @param ipExc Generic JDBC exception
  * @return <code>true</code> if this is a Connection problem.
  */
  public boolean isConnectionProblem(SQLException ipExc);
  
 /**
  * Method tests if the error code it was sent is a vendor-specific code for a
  * invalid resource usage like closed statements, or cursors.
  * @param ipExc Generic JDBC exception
  * @return <code>true</code> if this is a invalid resource handle problem.
  */
  public boolean isClosedResourceHandle(SQLException ipExc);

  /**
   * Method tests if the error code it was sent is a vendor-specific code for a
   * data error like data too large or wrong type for column
   * 
   * @param ipExc Generic JDBC exception
   * @return <code>true</code> if this is a invalid resource handle problem.
   */
   public boolean isDataError(SQLException ipExc);
}
