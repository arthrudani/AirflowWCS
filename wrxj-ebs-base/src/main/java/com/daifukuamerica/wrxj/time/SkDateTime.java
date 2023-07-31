package com.daifukuamerica.wrxj.time;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class SkDateTime represents a specific instant in time, with millisecond precision.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public class SkDateTime
{
  private Date dateTime = new Date();
  private String dateTimeFormat = "dd-MMM-yyyy HH:mm:ss.SSS";
  private SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
  private SimpleDateFormat elapsedTimeFormat = new SimpleDateFormat();
  private long startDateTime = 0;

  /**
   * Allocates a SkDateTime object.
   */
  public SkDateTime()
  {
  }

  /**
   * Allocates a SkDateTime object.
   *
   * @param sDateTimeFormat format string to apply when time is fetched
   */
  public SkDateTime(String sDateTimeFormat)
  {
      dateTimeFormat = sDateTimeFormat;
      simpleDateFormat.applyPattern(dateTimeFormat);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the current time, measured to the nearest millisecond.
   *
   * @return current time, measured to the nearest millisecond
   */
  public long getCurrentDateTime()
  {
    dateTime = new Date();
    return dateTime.getTime();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the current time, measured to the nearest millisecond.  The returned
   * time is a string formatted according to <i>dateTimeFormat</i>.
   *
   * @return current time, measured to the nearest millisecond
   */
  public String getCurrentDateTimeAsString()
  {
    dateTime = new Date();
    return simpleDateFormat.format(dateTime);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the current time, measured to the nearest millisecond.  The returned
   * time is a string formatted according to the caller's passed in format
   * string.
   *
   * @param sDateTimeFormat a format specification
   *
   * @return current time, measured to the nearest millisecond
   */
  public String getCurrentDateTimeAsString(String sDateTimeFormat)
  {
    dateTime = new Date();
    simpleDateFormat.applyPattern(sDateTimeFormat);
    return simpleDateFormat.format(dateTime);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the current time, measured to the nearest millisecond.  The returned
   * time is a <i>Long</i> object (not a <i>long</i>).
   *
   * @return current time, measured to the nearest millisecond
   */
  public Long getCurrentDateTimeAsLong() // Long is Object!
  {
    dateTime = new Date();
    return Long.valueOf(dateTime.getTime());
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the caller's time parameter as a string formatted according to
   * this object's internal <i>dateTimeFormat</i>.
   *
   * @param lDateTime a Date/Time (in milliseconds)
   * @return formated time
   */
  public String getlongDateTimeAsString(long lDateTime)
  {
    dateTime.setTime(lDateTime);
    return simpleDateFormat.format(dateTime);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the caller's time parameter as a string formatted according to
   * the caller's format parameter.
   *
   * @param lDateTime a Date/Time (in milliseconds)
   * @param sDateTimeFormat a format specification
   *
   * @return formated time
   */
  public String getlongDateTimeAsString(long lDateTime, String sDateTimeFormat)
  {
    dateTime.setTime(lDateTime);
    simpleDateFormat.applyPattern(sDateTimeFormat);
    return simpleDateFormat.format(dateTime);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify a beginning time using the current time, measured to the nearest
   * millisecond.  This beginning time value is later used to find an elapsed
   * time.
   */
  public void setStartDateTime()
  {
    dateTime = new Date();
    startDateTime = dateTime.getTime();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify a beginning time using the caller's time parameter.  This beginning
   * time value is later used to find an elapsed time.
   *
   * @param value a beginning time
   */
  public void setStartDateTime(long value)
  {
    startDateTime = value;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the interval time, measured to the nearest millisecond.  This
   * object's internal <i>startDateTime is used as the interval beginning time.
   *  The returned time is a <i>Long</i> object (not a <i>long</i>).
   *
   * @return time interval, measured to the nearest millisecond
   */
  public long getElapsedDateTimeAsLong()
  {
    dateTime = new Date();
    return dateTime.getTime() - startDateTime;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the interval time, measured to the nearest millisecond.  This
   * object's internal <i>startDateTime is used as the interval beginning time.
   *  The returned time is formatted according to this objects internal
   *  <i>dateTimeFormat</i>.
   *
   * @return time interval, measured to the nearest millisecond
   */
  public String getElapsedDateTimeAsString()
  {
    dateTime = new Date();
    long elapsedDateTime = dateTime.getTime() - startDateTime;
    return getElapsedDateTimeAsString(elapsedDateTime);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the interval time, measured to the nearest millisecond.  The caller's
   * parameter is used as the interval time to format.
   *
   * @return time interval, measured to the nearest millisecond
   */
  public String getElapsedDateTimeAsString(long elapsedDateTime)
  {
    dateTime = new Date();
    dateTime.setTime(elapsedDateTime);
    String formatPattern = "HH:mm:ss.SSS";
    if (elapsedDateTime < 1000)
    {
      formatPattern = "SSS 'msecs'";
    }
    else
    {
      if (elapsedDateTime < 60*1000)
      {
        formatPattern = "ss.SSS 'secs'";
      }
      else
      {
        if (elapsedDateTime < 60*60*1000)
        {
          formatPattern = "mm:ss.SSS";
        }
      }
    }
    elapsedTimeFormat.applyPattern(formatPattern);
    String dateString = elapsedTimeFormat.format(dateTime);
    if (dateString.charAt(0) == '0')
    {
      dateString = dateString.substring(1); // lose leading zero
    }
    return dateString;
  }
}
