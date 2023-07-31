package com.daifukuamerica.wrxj.jdbc.sqlserver;

import com.daifukuamerica.wrxj.jdbc.DBErrorCodes;
import com.daifukuamerica.wrxj.util.Reflected;
import java.sql.SQLException;

/**
 * SQL Server Error Codes.
 *
 * @author A.D.
 * @since  31-Jan-2013
 */
@Reflected
public class SQLServErrorCodes implements DBErrorCodes
{
  private final int CLOSED_CURSOR = -102;
  private final int DUPLICATE_INSERT = -119;

 /** Communication Link Error. */
  private final int COMMUNICATION_LINK = -461;
  /** Network is down */
  private final int NET_DOWN = -10050;
  /** Network is unreachable  */
  private final int NET_UNREACHABLE = -10051;
  /** Connection reset by peer */
  private final int CONN_PEER_RESET = -10054;
  /** Connection not established (Socket not connected) */
  private final int CONN_NOT_ESTABLISED = -10057;
  /** Connection Timeout */
  private final int CONN_TIMEOUT = -10060;
  /** Connection Refused */
  private final int CONN_REFUSED = -10061;
  /** No route to the Host */
  private final int NO_NET_ROUTE = -10065;
  /** Network subsystem is unavailable */
  private final int NET_SYS_UNAVAILABLE = -10091;
//  /** */
//  private final int TCP_DISABLED = -11001;
  /** Database is shutting down */
  private final int DB_SHUTTING_DOWN = 6005;
  /** String or binary data would be truncated. */
  private final int DB_WOULD_BE_TRUNCATED = 8152;
  
  /** The TCP/IP connection to the host xxxx, port yyyy has failed. */
  private final String CONNECTION_FAILED_1 = "The TCP/IP connection to the host";
  private final String CONNECTION_FAILED_2 = "has failed";
  /** Connection Closed */
  private final String CONNECTION_CLOSED = "The connection is closed.";
  
  @Override
  public String getConnectionQuery()
  {
    return("SELECT GETDATE()");
  }

  @Override
  public boolean isDuplicateRecordInsert(SQLException ipExc)
  {
    return(ipExc.getErrorCode() == DUPLICATE_INSERT);
  }

  @Override
  public boolean isConnectionProblem(SQLException ipExc)
  {
    boolean vzRtn;

    switch(ipExc.getErrorCode())
    {
      case 0:
        if (ipExc.getMessage().equals(CONNECTION_CLOSED))
        {
          vzRtn = true;
        }
        else if (ipExc.getMessage().startsWith(CONNECTION_FAILED_1)
            && (ipExc.getMessage().contains(CONNECTION_FAILED_2)))
        {
          vzRtn = true;
        }
        else
        {
          vzRtn = false;
        }
        break;
        
      case COMMUNICATION_LINK:
      case NET_DOWN:
      case NET_SYS_UNAVAILABLE:
      case NET_UNREACHABLE:
      case CONN_PEER_RESET:
      case CONN_NOT_ESTABLISED:
      case CONN_TIMEOUT:
      case CONN_REFUSED:
      case NO_NET_ROUTE:
      case DB_SHUTTING_DOWN:
        vzRtn = true;
        break;

      default:
        vzRtn = false;
    }

    return vzRtn;
  }

  @Override
  public boolean isClosedResourceHandle(SQLException ipExc)
  {
    return(ipExc.getErrorCode() == CLOSED_CURSOR);
  }
  
  /**
   * Method tests if the error code it was sent is a vendor-specific code for a
   * data error like data too large or wrong type for column
   * 
   * @param ipExc Generic JDBC exception
   * @return <code>true</code> if this is a invalid resource handle problem.
   */
  public boolean isDataError(SQLException ipExc)
  {
    boolean vzRtn;

    switch(ipExc.getErrorCode())
    {
      case DB_WOULD_BE_TRUNCATED:
        vzRtn = true;
        break;

      default:
        vzRtn = false;
    }

    return vzRtn;
  }
}
