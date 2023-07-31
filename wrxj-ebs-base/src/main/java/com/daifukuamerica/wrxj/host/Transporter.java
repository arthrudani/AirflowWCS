package com.daifukuamerica.wrxj.host;

import java.time.LocalDateTime;

/****************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright ? 2004 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;

public interface Transporter
{
  public static final int  CLIENT_TRANSPORT = 332;
  public static final int  SERVER_TRANSPORT = 333;
  public static final int  BIDIRECTIONAL_CONN = 400;
  public static final int  INBOUND_CONN = 441;
  public static final int  OUTBOUND_CONN = 442;

 /**
  * Identifies whether this Transporter is a Server or a Client service.
  * @return identification of service type.  Valid identifiers are:<p>
  * {@link #CLIENT_TRANSPORT CLIENT_TRANSPORT}, and
  * {@link #SERVER_TRANSPORT SERVER_TRANSPORT}
  */
  public int getTransportModel();

  public void setCommPort(int inPort);

 /**
  * Sends first available message(s) from the WrxToHost table to the host.
  * @param ipOutDelegate the delegate to send for out bound processing.
  * @param ipHostServer Host Transaction Mediator.
  * @return Number of messages sent successfully.
  * @throws DBException if database error occurs on
  *         either wrx-j or host system.
  * @throws HostCommException if there is an error communicating
  *         with the host system.
  */
  public int sendMessages(HostServerDelegate ipOutDelegate,
                          StandardHostServer ipHostServer)
         throws DBException, HostCommException;

  /**
   * Retransmit all of pending messages which wait for ack reply from SAC/Host
   * @param ipOutDelegate the delegate to send for out bound processing.
   * @param ipHostServer Host Transaction Mediator.
   * @param maxRetry the maximum retry
   * @return Number of messages sent successfully.
   * @throws DBException if database error occurs on
   *         either wrx-j or host system.
   * @throws HostCommException if there is an error communicating
   *         with the host system.
   */
  public int retransmitPendingMessages(HostServerDelegate hostOutDelegate,
                                       StandardHostServer hostServer,
                                       int maxRetry)
         throws DBException, HostCommException;
  
  /**
   * Method to send a test (or Heart Beat message) to the host.
   *
   * @throws HostCommException if there is a communication error.
   */
  public void sendHeartBeat() throws HostCommException;

 /**
  * Closes connection to the host system.  This method is to be called when (A)
  * the HostController is taken down, or (B) when this Transporter represents a
  * Client service and the connection needs to be reset.
  */
  public void closeHostConnection();

 /**
  *  Checks if the Host side is functional.
  *  @return <code>boolean</code> of <code>true</code> only if host is reachable.
  */
  public boolean isHostReachable();

  /**
   * Method to check if the transporter thread is still alive.
   * @return 
   */
  public boolean isTransporterAlive();
  
 /**
  * Check to see if connection to host has been established.
  * @return <code>boolean</code> of <code>true</code> only if host connection is
  *         established.
  */
  public boolean isConnectionEstablished();

 /**
  * Method associates logger with this thread of execution.
  * @param logger the logger reference.
  */
  public void setLogger(Logger logger);

  /**
   * Method to start transporter. This method allows the control of when a
   * Transporter is started.  This way we can initialise all necessary variables
   * before starting up.
   *
   * @throws HostCommException if there is a connection issue.  Normally this
   * exception will be thrown from Client type connection attempts if the Host
   * is the Server and we are the Client.
   */
  public void startTransporter() throws HostCommException;

  /**
   * Method to stop the transporter thread.
   */
  public void stopTransporter();
  
  public LocalDateTime lastKeepAliveReceived();

  public LocalDateTime connectionEstablished();
}
