package com.daifukuamerica.wrxj.host.communication;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.daifukuamerica.wrxj.host.Transporter;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * Connection Monitor Thread to monitor all connections.
 * <p>If the connection is deemed dead, then kill the Client Thread; wait x number
 * of seconds and retry.</p>
 *
 * @author A.D.
 * @since  12-Feb-2013
 */
public class TCPIPConnectionMonitor extends AbstractConnectionMonitor
{
  private static final Logger logger = Logger.getLogger();
  
  private int keepAliveTimeout;
  
  public TCPIPConnectionMonitor(Transporter ipTransporter, int inInterval, int keepAliveTimeout)
  {
    super(ipTransporter, inInterval);
    this.keepAliveTimeout = keepAliveTimeout;
  }

  @Override
  public void checkConnection()
  {
    boolean vzConnClosed = true;
    LocalDateTime connectionEstablished = null;
    LocalDateTime lastKeepAliveReceived = null;

    while(!mzStopConnMonitor)
    {
      try
      {
        if (vzConnClosed)
        {
          if (!mpTransport.isTransporterAlive())
          {
            logger.logDebug("Starting a transporter");
            connectionEstablished = null;
            lastKeepAliveReceived = null;
            mpTransport.startTransporter();
            sleep(3);     // Sleep for 3 sec. to allow remote connection to take effect.
          }
        }
        
        mpTransport.sendHeartBeat();

        if (!mpTransport.isHostReachable())
        {
//          mpTransport.closeHostConnection();
          vzConnClosed = true;
        }
        else
        {
          vzConnClosed = false;
        }

        // Checking only while connected
        if (!vzConnClosed && connectionEstablished != null) {
          if (lastKeepAliveReceived == null) {
            // check keep alive timeout from the connection establishment
            if (isTimedOut(connectionEstablished, keepAliveTimeout)) {
              // Mark the connection to be closed below
              logger.logDebug(">>> SAC <<< Keep alive wasn't received within " + keepAliveTimeout + " seconds since when the connection was established, so closing the connection now");
              vzConnClosed = true;
            }
          } else {
            // check keep alive timeout from the last keep alive received
            if (isTimedOut(lastKeepAliveReceived, keepAliveTimeout)) {
              // Mark the connection to be closed below
              logger.logDebug(">>> SAC <<< Keep alive wasn't received within " + keepAliveTimeout + " seconds since when the last keep alive was received, so closing the connection now");
              vzConnClosed = true;
            }
          }
        }
        
        connectionEstablished = mpTransport.connectionEstablished();
        lastKeepAliveReceived = mpTransport.lastKeepAliveReceived();
      }
      catch(Exception ce)
      {
        vzConnClosed = true;
      }
      finally
      {
        if (vzConnClosed)
        {
          mpTransport.closeHostConnection();
        }
      }
      sleep(mnInterval);
    }
  }

  /**
   * Checks if it has timeout
   * 
   * @param timeToCheck the time to check
   * @param timeoutInSeconds timeout in seconds
   * @return true if timed out, false if not
   */
  private boolean isTimedOut(LocalDateTime timeToCheck, int timeoutInSeconds) {
    LocalDateTime timeout = timeToCheck.plusSeconds(timeoutInSeconds);
    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(timeout)) {
        logger.logDebug(">>> SAC <<< Now = " + now + ", time to check: " + timeToCheck + ", timeoutInSecond: " + timeoutInSeconds + ", time to check + timeoutInSecond: " + timeout + ", diff = " + ChronoUnit.SECONDS.between(timeout, now));
        return true;
    }
    return false;
}

  @Override
  public void stopMonitor()
  {
    mzStopConnMonitor = true;
    interrupt();
  }
}
