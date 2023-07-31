package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.wrxj.host.Transporter;

/****************************************************************************
  $Workfile: ConnectionMonitor.java
  $Revision: IKEA

  Copyright 2017 Daifuku America Corporation All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/
/**
 * Abstract connection monitor.
 *
 * @author A.D.
 * @since  12-Dec-2017
 */
public abstract class AbstractConnectionMonitor extends Thread
{
  protected volatile boolean  mzStopConnMonitor = false;
  protected final Transporter mpTransport;
  protected final int         mnInterval;

  public AbstractConnectionMonitor(Transporter ipTransport, int inInterval)
  {
    mpTransport = ipTransport;
    mnInterval = inInterval;
    start();
  }

  @Override
  public void run()
  {
    checkConnection();
  }

  protected void sleep(int inSleepTime)
  {
    try { Thread.sleep(inSleepTime*1000L); }
    catch(InterruptedException ie) {}
  }

  public abstract void checkConnection();
  public abstract void stopMonitor();
}
