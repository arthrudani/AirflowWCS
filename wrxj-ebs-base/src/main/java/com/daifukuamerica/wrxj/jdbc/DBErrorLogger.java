/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.jdbc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Class to log SQL when a data error occurs
 * 
 * @author mandrus
 */
public class DBErrorLogger
{
  private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  /** private constructor - do not instantiate */
  private DBErrorLogger() {}

  /**
   * Print additional info to the console when there is a data error so we have
   * some hope of diagnosing and correcting the problem.
   * 
   * @param ipError - cannot be null
   * @param isSql - cannot be null
   * @param iapParams
   */
  public static void log(Throwable ipError, String isSql, Object... iapParams)
  {
    if (ipError == null) return;
    if (isSql == null) return;

    StringBuilder vpMsg = new StringBuilder();
    
    // Timestamp - Thread Name
    String vsNow = DT_FORMAT.format(LocalDateTime.now());
    vpMsg.append(vsNow).append(" - ")
         .append(Thread.currentThread().getName())
         .append(" - Database Data Error").append(System.lineSeparator());

    // Exception class/message
    vpMsg.append(ipError.getClass().getCanonicalName())
      .append(": ").append(ipError.getMessage()).append(System.lineSeparator());
    
    // SQL information
    vpMsg.append(" * SQL: ").append(isSql).append(System.lineSeparator());
    if (iapParams != null)
    {
      vpMsg.append(" * Prm: ").append(Arrays.toString(iapParams)).append(System.lineSeparator());
    }
    
    // Filtered stack trace
    Pattern vpPattern = Pattern.compile("daifuku?|wynright?");
    StackTraceElement[] vapStacktrace = ipError.getStackTrace();
    if (vapStacktrace != null && vapStacktrace.length > 0)
    {
      for (StackTraceElement vpElement : vapStacktrace)
      {
        if (vpElement != null)
        {
          String vsSTE = vpElement.toString();
          if (vpPattern.matcher(vsSTE).find())
          {
            vpMsg.append("\tat ").append(vsSTE).append(System.lineSeparator());
          }
        }
      }
    }
    
    // I'm reluctant to put actual SQL in the logs for production servers.
    // This probably doesn't matter for GES, but it could matter elsewhere.
    System.err.print(vpMsg.toString());
  }
}
