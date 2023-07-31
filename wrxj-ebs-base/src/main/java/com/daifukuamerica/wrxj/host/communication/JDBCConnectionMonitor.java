package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.wrxj.host.Transporter;

/**
 * Connection Monitor Thread to monitor all connections.
 * <p>If the connection is deemed dead, then kill the Client Thread.  The TCPIP
 * Server implementation will then restart another client thread once a new
 * connection is established from the client.
 *
 * @author A.D.
 * @since  12-Feb-2013
 */
public class JDBCConnectionMonitor extends AbstractConnectionMonitor
{
  public JDBCConnectionMonitor(Transporter ipTransporter, int inInterval)
  {
    super(ipTransporter, inInterval);
  }

  @Override
  public void checkConnection()
  {
    boolean vzReachable = false;

    while(!mzStopConnMonitor)
    {
      try
      {
        if (!mpTransport.isHostReachable())
        {
          mpTransport.closeHostConnection();
          vzReachable = false;
          sleep(1);
        }
        else
        {
          vzReachable = true;
        }

        if (vzReachable)
        {
          if (!mpTransport.isTransporterAlive())
          {
            mpTransport.startTransporter();
          }
          mpTransport.sendHeartBeat();
        }
      }
      catch(Exception ce)
      {
        mpTransport.closeHostConnection();
      }
      sleep(mnInterval);
    }
  }

  @Override
  public void stopMonitor()
  {
    mzStopConnMonitor = true;
    interrupt();
  }
}
