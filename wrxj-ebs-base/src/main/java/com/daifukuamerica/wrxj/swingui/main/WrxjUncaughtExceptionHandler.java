package com.daifukuamerica.wrxj.swingui.main;

import com.daifukuamerica.wrxj.log.Logger;

public class WrxjUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
  public WrxjUncaughtExceptionHandler()
  {
  }
  @Override
  public void uncaughtException(Thread arg0, Throwable arg1)
  {
    Logger vplogger = Logger.getLogger();
    vplogger.logException(arg1, "Warehouse Rx Uncaught Exception Handler");
    System.err.println("Exception in Thread: " + arg0.getName());
    arg1.printStackTrace(System.err);
  }
}
