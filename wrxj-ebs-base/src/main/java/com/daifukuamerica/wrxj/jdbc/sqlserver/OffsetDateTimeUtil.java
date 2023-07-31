package com.daifukuamerica.wrxj.jdbc.sqlserver;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * Class for easing use of MS SQL Server datetimeoffset
 * 
 * @author mandrus
 */
public class OffsetDateTimeUtil
{
  private static DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
      .appendPattern("yyyy-MM-dd HH:mm:ss")
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .appendPattern(" ZZZZZ").toFormatter();

  public static OffsetDateTime getTime()
  {
    return OffsetDateTime.now();
  }
  
  public static OffsetDateTime toOffsetDateTime(CharSequence isTime)
  {
    if (isTime == null || isTime.length() == 0)
    {
      return null;
    }
    return OffsetDateTime.parse(isTime, FORMATTER);
  }
  
  public static Date toDate(CharSequence isTime)
  {
    if (isTime == null || isTime.length() == 0)
    {
      return null;
    }
    return Date.from(OffsetDateTime.parse(isTime, FORMATTER).toInstant());
  }
  
  public static String toString(Date ipTime)
  {
    if (ipTime == null)
    {
      return "NULL";
    }
    String dbString = "'" + OffsetDateTime.ofInstant(ipTime.toInstant(), ZoneId.systemDefault()).format(FORMATTER) + "'";
//    System.out.println(dbString);
    return dbString;
  }

  public static String toString(OffsetDateTime ipTime)
  {
    if (ipTime == null)
    {
      return null;
    }
    String dbString = ipTime.format(FORMATTER);
//    System.out.println(dbString);
    return dbString;
  }
}
