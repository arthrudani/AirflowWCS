package com.daifukuamerica.wrxj.jdbc;

/**
 * Description:<BR>
 *  Class to create runtime exceptions (not required to be caught).
 * @author       A.D.
 * @version      1.0
 *     Copyright (c) 2004<BR>
 *     Company:  DaifukuAmerica Corporation
 */
public class DBRuntimeException extends RuntimeException
{
  private static final long serialVersionUID = 0L;

  public DBRuntimeException()
  {
    super();
  }

  public DBRuntimeException(String mesg)
  {
    super(mesg);
  }

  public DBRuntimeException(Throwable exc)
  {
    super(exc);
  }
  
  public DBRuntimeException(String mesg, Throwable exc)
  {
    super(mesg, exc);
  }
}