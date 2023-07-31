package com.daifukuamerica.wrxj.jdbc.oracle;

import com.daifukuamerica.wrxj.jdbc.DBErrorCodes;
import java.sql.SQLException;

/**
 *  Class to define Oracle Error codes.
 *  
 *  @author   A.D.
 *  @version  1.0
 *  @since    28-Nov-2007
 */
public class OracleErrorCodes implements DBErrorCodes
{
  /*========================================================================*/
  /*  Oracle-specific SQL                                                   */
  /*========================================================================*/
  
  /**
   * Get the SQL for a connection query
   * 
   * @return
   */
  @Override
  public String getConnectionQuery()
  {
    return "SELECT 1 FROM DUAL";
  }

  /*========================================================================*/
  /*  Oracle-specific error handling                                        */
  /*========================================================================*/

 /** DB Session was killed */
  private final int CONNECTION_KILLED = 28;

  /** DB Session was marked for closure (waiting for I/O or some other operation
     to complete. */
  private final int CONNECTION_PENDING_CLOSE = 31;
  
 /** DB Connection is closed */
  private final int CLOSED_CONNECTION = 17008;
  
 /** Attempt to use closed statement */
  private final int STATEMENT_CLOSED = 17009;
  
 /** Attempt to use closed cursor */
  private final int CLOSED_CURSOR = 17010;
  
 /**
  * Method tests if the error code it was sent is a vendor-specific code for a
  * duplicate record insert.
  * @param ipExc Generic JDBC exception
  * @return <code>true</code> if this is a Connection problem.
  */
  @Override
  public boolean isDuplicateRecordInsert(SQLException ipExc)
  {
    return(ipExc.getErrorCode() == 1);
  }
  
 /**
  * Method tests if the error code it was sent is a vendor-specific code for a
  * bad database connection.
  * @param ipExc Generic JDBC exception
  * @return <code>true</code> if this is a Connection problem.
  */
  @Override
  public boolean isConnectionProblem(SQLException ipExc)
  {
    boolean vzRtn;
    
    switch(ipExc.getErrorCode())
    {
      case CONNECTION_KILLED:
      case CONNECTION_PENDING_CLOSE:
      case CLOSED_CONNECTION:
        vzRtn = true;
        break;
        
      default:
        vzRtn = false;
    }
    
    return(vzRtn);
  }
  
 /**
  * Method tests if the error code it was sent is a vendor-specific code for a
  * invalid resource usage like closed statements, or cursors.
  * @param ipExc Generic JDBC exception
  * @return <code>true</code> if this is a invalid resource handle problem.
  */
  @Override
  public boolean isClosedResourceHandle(SQLException ipExc)
  {
    boolean vzRtn;
    
    switch(ipExc.getErrorCode())
    {
      case STATEMENT_CLOSED:
      case CLOSED_CURSOR:
        vzRtn = true;
        break;
        
      default:
        vzRtn = false;
    }
    
    return(vzRtn);
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
