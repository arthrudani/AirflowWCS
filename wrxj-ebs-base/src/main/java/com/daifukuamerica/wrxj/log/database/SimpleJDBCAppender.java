/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.log.database;

//import com.daifukuamerica.wrxj.application.Application;
//import com.daifukuamerica.wrxj.jdbc.sqlserver.SQLServErrorCodes;
//import com.daifukuamerica.wrxj.util.StringObfuscator;
//import java.sql.SQLException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import org.apache.log4j.EnhancedPatternLayout;
//import org.apache.log4j.LogManager;
//import org.apache.log4j.jdbc.JDBCAppender;
//import org.apache.log4j.spi.LoggingEvent;
//import org.apache.log4j.spi.ThrowableInformation;

/**
 * Simple database logger - still experimental
 * 
 * TODO: Use a prepared statement with parameters (speed/security)
 * TODO: Use a connection pool (speed)
 * 
 * @author mandrus
 */
public class SimpleJDBCAppender {}
//public class SimpleJDBCAppender extends JDBCAppender
//{
//  private static final int MESSAGE_LENGTH = (int)(3000 * 0.9);
//  
//  public SimpleJDBCAppender(String isSQL)
//  {
//    // Properties from wrxj.properties
//    String vsDBName = Application.getString("database", "OracleDB");
//    String vsDriver = Application.getString(vsDBName + ".driver");
//    String vsURL = Application.getString(vsDBName + ".url");
//    String vsUserName = Application.getString(vsDBName + ".user");
//    String vsPassword = Application.getString(vsDBName + ".password");
//    if (vsPassword != null && vsPassword.startsWith("+"))
//      vsPassword = StringObfuscator.decode(vsPassword.substring(1));
//
//    setLayout(new EnhancedPatternLayout(isSQL));
//    setDriver(vsDriver);
//    setLocationInfo(false);
//    setPassword(vsPassword);
//    setURL(vsURL);
//    setUser(vsUserName);
//  }
//  
//  /**
//   * Override to help track down problems
//   */
//  @Override
//  protected void execute(String sql) throws SQLException
//  {
//    try
//    {
//      super.execute(sql);
//    }
//    catch (SQLException e)
//    {
//      // SQL Server only...
//      if (new SQLServErrorCodes().isConnectionProblem(e))
//      {
//        System.err.println(formatDateTime(new Date()) + " Unable to connect to DB for logging");
//        resetAfterError();
//      }
//      else if (e.getMessage().contains("truncated"))
//      {
//        System.err.println("v===== " + getClass().getCanonicalName() + " FAILED! =====v");
//        System.err.println(sql);
//        e.printStackTrace(System.err);
//        System.err.println("^===== " + getClass().getCanonicalName() + " FAILED! =====^");
//        System.err.flush();
//      }
//      else
//      {
//        System.err.println(formatDateTime(new Date()) + " Unable to log to DB");
//        throw e;
//      }
//    }
//  }
//
//  /**
//   * Standard formatting for date+times in logs/messages
//   * @param ipTime
//   * @return
//   */
//  public String formatDateTime(Date ipTime)
//  {
//    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ipTime);
//  }
//
//  /**
//   * Disconnect after a reset to force a reconnection
//   */
//  private void resetAfterError()
//  {
//    try
//    {
//      if (connection != null && !connection.isClosed())
//      {
//        connection.close();
//      }
//    }
//    catch (SQLException arg1) {}
//    connection = null;
//  }
//  
//  /**
//   * Attempt to escape SQL until we rewrite this class to use prepared
//   * statements.
//   */
//  @Override
//  protected String getLogStatement(LoggingEvent event)
//  {
//    boolean needsClone = false;
//    Object newMessage = event.getMessage();
//    Throwable newThrowable = event.getThrowableInformation() != null
//        ? event.getThrowableInformation().getThrowable() : null;
//    if (newMessage != null && newMessage.toString().length() >= MESSAGE_LENGTH)
//    {
//      needsClone = true;
//      newMessage = newMessage.toString().substring(0, MESSAGE_LENGTH);
//    }
//    if (newMessage != null && newMessage.toString().indexOf("'") >= 0)
//    {
//      needsClone = true;
//      newMessage = newMessage.toString().replaceAll("'", "''");
//    }
//    if (newThrowable != null
//        && newThrowable.getMessage().indexOf("'") >= 0)
//    {
//      needsClone = true;
//      try
//      {
//        newThrowable = newThrowable.getClass().getDeclaredConstructor(
//            String.class).newInstance(
//                newThrowable.getMessage().replaceAll("'", "''"));
//        newThrowable.setStackTrace(
//            event.getThrowableInformation().getThrowable().getStackTrace());
//      }
//      catch (Exception e)
//      {
//        newThrowable = null;
//      }
//    }
//    if (needsClone) 
//    {
//      // A cloned event was required due to SQL escaping
//      LoggingEvent clone = new LoggingEvent(event.fqnOfCategoryClass,
//          LogManager.getLogger(event.getLoggerName()), event.timeStamp,
//          event.getLevel(), newMessage, event.getThreadName(),
//          new ThrowableInformation(newThrowable), event.getNDC(),
//          event.getLocationInformation(), event.getProperties());
//      return getLayout().format(clone);
//    }
//    return getLayout().format(event);
//  }
//}
