package com.daifukuamerica.wrxj.timer;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

/**
 * @author Stephen Kendorski
 * @version 1.0
 */
public abstract class RestartableTimerTask implements Runnable
{

  static final int VIRGIN = 0;
  static final int SCHEDULED = 1;
  static final int EXECUTED = 2;
  static final int CANCELLED = 3;

  final Object lock = new Object();
  int state = VIRGIN;
  long nextExecutionTime = 0;
  long period = 0;

  /**
   *
   */
  protected int iTag = 0;
  protected String sTag = "";
  protected boolean bTag = false;

  public RestartableTimerTask()
  {
  }

  void setIntTag(int aTag)
  {
    iTag = aTag;
  }

  void setStringTag(String aTag)
  {
    sTag = aTag;
  }

  int getIntTag()
  {
    return iTag;
  }

  String setStringTag()
  {
    return sTag;
  }

  /**
   * returns true if it prevents one or more scheduled executions from taking place.
   */
 public void cancel()
  {
    state = CANCELLED;
  }

  boolean isScheduled()
  {
    return (state == SCHEDULED);
  }

  /**
   * Returns:the time at which the most recent execution of this task was scheduled to occur,
   * in the format returned by Date.getTime(). The return value is undefined if the task has
   * yet to commence its first execution.
   * 
   * @return ?
   */
  long scheduledExecutionTime()
  {
    return nextExecutionTime;
  }

}

