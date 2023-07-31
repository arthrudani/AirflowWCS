package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.Transporter;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * Outbound timer task to periodically check for outbound Host messages.  This
 * timer executes as long as the host is reachable, and the thread is not interrupted.
 *
 * @author A.D.
 * @since 08-Jan-2008
 */
public class OutboundTimerThread extends Thread
{
  protected static Logger      mpErrorLog;
  protected Transporter        mpTranporter;
  protected String             msHostName;
  protected int                mnInterval;
  protected int                ackTimeout;
  protected int                ackMaxRetry;
  protected volatile boolean   mzInterrupt = false;
  protected StandardHostServer mpHostServer = null;
  protected HostServerDelegate mpDelegate = null;
  
  protected static int 		   iCounter = 0;

  public OutboundTimerThread(Transporter ipTransporter, String isHostName, int inCheckInterval, int ackTimeout, int ackMaxRetry)
  {
    mpTranporter = ipTransporter;
    msHostName = isHostName;
    mnInterval = inCheckInterval;
    this.ackMaxRetry = ackMaxRetry;
    mpErrorLog = Logger.getLogger();
    setName(getClass().getSimpleName());
  }

  @Override
  public void run()
  {
	iCounter++;
	mpErrorLog.logOperation(msHostName + " : KR: OutboundTimerThread is called. iCounter is:" + iCounter);
	   
    DBObject vpDBObj = null;

    try
    {
      mpErrorLog.logOperation(msHostName + ".OutboundTimer is starting.");

      vpDBObj = new DBObjectTL().getDBObject();
      vpDBObj.connect();               // Make local DB connection

      initInstanceVars();
      while (!mzInterrupt && mpTranporter.isHostReachable())
      {
        mpDelegate.setInfo(msHostName);
        if (mpHostServer.unprocessedMessageAvailable(mpDelegate))
        {
          mpTranporter.sendMessages(mpDelegate, mpHostServer);
        }
        
        mpDelegate.setInfo(msHostName);
        if (mpHostServer.isPendingMessageFound(mpDelegate, ackTimeout))
        {
          mpTranporter.retransmitPendingMessages(mpDelegate, mpHostServer, ackMaxRetry);
        }
        
        Thread.sleep(mnInterval * 1000L);
      }
      if (!mzInterrupt)
      {
        mpErrorLog.logError(msHostName + ".OutboundTimer cannot reach the host.");
      }
    }
    catch(InterruptedException ie)
    {
      mpErrorLog.logOperation(msHostName + ".OutboundTimer was interrupted.");
    }
    catch (Exception e)
    {
      mpErrorLog.logException(msHostName + ".OutboundTimer", e);
    }
    finally
    {
      if (mpHostServer != null)
      {
        mpHostServer.cleanUp();
      }

      if (vpDBObj != null)
      {
        try { vpDBObj.disconnect(true); } catch(DBException ex) {}
      }
      mpErrorLog.logOperation(msHostName + ".OutboundTimer has terminated.");
    }

  }

  public void cancel()
  {
    mzInterrupt = true;
  }

  protected void initInstanceVars()
  {
    if (mpHostServer == null)
    {
      mpHostServer = Factory.create(StandardHostServer.class);
      mpDelegate = Factory.create(HostOutDelegate.class);
    }
  }
}
