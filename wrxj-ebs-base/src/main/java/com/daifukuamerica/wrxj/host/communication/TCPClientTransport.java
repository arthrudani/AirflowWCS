package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.TCPIPSocketCloseEvent;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.impl.TCPIPReaderWriter;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.Transporter;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Client Transport for when WRx is the Client Socket.
 *
 * @author A.D.
 * @since  15-Jun-2013
 */
public class TCPClientTransport implements Transporter, Runnable
{
  /** Connection Port. */
  protected int      mnConnectionPort = 0;
  /** The name or IP of this host/Server socket */
  protected String      msHostName = null;
  /**
   * The name of the collaborator (HostMessageIntegrator) that will be notified
   * when we receive a message.
   */
  protected String       msCommGroup;
  protected String      msCollaborator = "";
  protected boolean     mzUseAcks;
  protected boolean     mzUseHeartBeats;
  protected volatile boolean     mzConnEstablished = false;

  protected Thread            mpClientThread;
  protected TCPIPLogger       mpLogger;
  protected TCPIPReaderWriter mpReadWriteThread;
  protected Controller        mpSystemGateway;
  protected Properties        mpConnProperties;

                                      // Database related declarations.
  protected StandardHostServer   mpHostServer;

  public TCPClientTransport(Controller ipSystemGateway, String isHostControllerName, String isCommGroup)
         throws HostCommException
  {
    mpSystemGateway = ipSystemGateway;
    msCommGroup = isCommGroup;
    mpConnProperties = initConnectionProperties();

    mpHostServer = Factory.create(StandardHostServer.class, "TCPClientTransport");

    msCollaborator = Application.getString(Application.HOSTCFG_DOMAIN + isHostControllerName + ".Collaborator");
  }

  @Override
  public void setCommPort(int inPort)
  {
    mnConnectionPort = inPort;
  }

 /**
  * {@inheritDoc}
  * @return {@inheritDoc}
  */
  @Override
  public int getTransportModel()
  {
    return(Transporter.CLIENT_TRANSPORT);
  }

  @Override
  public void setLogger(Logger ipWrxJLogger)
  {
    mpLogger = new TCPIPBaseLoggerImpl(ipWrxJLogger);
  }

  /**
   * The main purpose of this run is to connect to the host and start up a
   * client thread to manage that connection.  <b>Note:</b> If there is already
   * a connection by chance, and someone tries to start another thread, do nothing!
   */
  @Override
  public void run()
  {
    if (mzConnEstablished)
    {
      return;
    }

    try
    {
      /*
       * Handle any data that comes off the socket as a Read Event.
       */
      TCPIPReadEventImpl vpReadEvent = Factory.create(TCPIPReadEventImpl.class, msHostName, msCommGroup, mpLogger);
      vpReadEvent.useAckNak(mzUseAcks);
                                       // Configure notification for HostMessageIntegrator
                                       // for when messages arrive.
      vpReadEvent.setupCollaboratorNotification(mpSystemGateway, msCollaborator);
                                       // Event handler for when host connection is closed.
      TCPIPSocketCloseEvent vpCloseEvt = Factory.create(TCPIPSocketCloseEventImpl.class, msCommGroup, mpSystemGateway, mpLogger);
      /*
       * Register Event Handler with the Reader Thread and Start it.
       */
      mpReadWriteThread = new TCPIPReaderWriter(mpConnProperties, mpLogger);
      mpReadWriteThread.registerReadEvent(vpReadEvent);
      mpReadWriteThread.registerCloseEvent(vpCloseEvt);
      mpReadWriteThread.connToServer();
      mpReadWriteThread.start();
      mzConnEstablished = true;

      try
      {
        mpReadWriteThread.join();
      }
      catch(InterruptedException ie)
      {
        mzConnEstablished = false;
      }
    }
    catch(TCPIPCommException exc)
    {
      mpLogger.logErrorMessage("Client Connection Error to " + msHostName, exc);
      mzConnEstablished = false;
    }
  }

  @Override
  public void sendHeartBeat() throws HostCommException
  {
    if (mzUseHeartBeats)
    {
      String vsHBMesg = mpConnProperties.getProperty(TCPIPConstants.HEART_BEAT_MSG);
      try
      {
        mpReadWriteThread.sendMessage(vsHBMesg);
      }
      catch(TCPIPCommException ex)
      {
        throw new HostCommException("Error establishing connection to Server " +
                                    "Socket from Client.", ex);
      }
    }
  }

  @Override
  public synchronized int sendMessages(HostServerDelegate ipOutDelegate,
                                       StandardHostServer ipHostServer)
         throws DBException, HostCommException
  {
    int vnMessageCount = 0;
    WrxToHostData vpWrxToHostData = null;

                                       // Make local DB connection
    DBObject vpDBObj = new DBObjectTL().getDBObject();
    if (!vpDBObj.checkConnected()) vpDBObj.connect();

    do
    {
      TransactionToken vpTransaction = null;
      try
      {
        vpTransaction = vpDBObj.startTransaction();
                                       // Give delegate information related to
                                       // the task it will perform.
        ipOutDelegate.setInfo(msHostName);
        ipOutDelegate.setReadLockInfo(true);
                                       // Read outbound message from WrxToHost
                                       // table.
        vpWrxToHostData = (WrxToHostData)mpHostServer.getOldestDataQueueMessage(ipOutDelegate);
        if (vpWrxToHostData != null)
        {
          String vsSeqNo = SKDCUtility.preZeroFill(vpWrxToHostData.getMessageSequence(), MessageHelper.MESSAGE_SEQUENCE_LENGTH);
          byte[] vabSeqNo = vsSeqNo.getBytes();
          byte[] vabMesgBytes = vpWrxToHostData.getMessageBytes();

          ByteBuffer vpBaseMesg = ByteBuffer.allocate(vabSeqNo.length + vabMesgBytes.length);
          vpBaseMesg.put(vabSeqNo)
                    .put(vabMesgBytes);
          ByteBuffer vpSentMesg = mpReadWriteThread.sendMessage(vpBaseMesg.array());

          System.out.println("Sent: " + new String(vpSentMesg.array()));

                                       // Mark WRx-J message as processed.
          ipOutDelegate.setInfo(vpWrxToHostData);
          ipHostServer.markMessageAsProcessed(ipOutDelegate);
          vpDBObj.commitTransaction(vpTransaction);
          vnMessageCount++;
        }
      }
      catch(TCPIPCommException hce)
      {
        mpLogger.logErrorMessage("Error Sending message to host!", hce);
      }
      finally
      {
        vpDBObj.endTransaction(vpTransaction);
      }

    } while(mzConnEstablished && vpWrxToHostData != null);

    return(vnMessageCount);
  }

  @Override
  public void startTransporter() throws HostCommException
  {
    mpClientThread = new Thread(this);
    mpClientThread.start();
  }

  @Override
  public void closeHostConnection()
  {
    mzConnEstablished = false;
    if (mpReadWriteThread != null)
      mpReadWriteThread.stopThread();
  }

  /**
   * This is a rehash of the closeHostConnection method. This method is however
   * necessary for compatibility with the TCP/IP Server Transporter.
   */
  @Override
  public void stopTransporter()
  {
    closeHostConnection();
  }

  @Override
  public boolean isTransporterAlive()
  {
    return mpClientThread != null && mpClientThread.isAlive() &&
           mpClientThread.getState() != Thread.State.TERMINATED;
  }

 /**
  *  {@inheritDoc}.  This method tests the remote connection to see if it's valid.
  *  This method is only called to check the outbound socket.  If the configuration
  *  parameter <code>HostConfig.UseHeartBeats</code> is set to <code>false</code>
  *  then this method always returns <code>true.</code>
  *  @return <code>boolean</code> of <code>true</code> only if remote sockets are
  *          still accessible.
  */
  @Override
  public boolean isHostReachable()
  {
    boolean vzRtn = true;

    if (mzUseHeartBeats)
    {
      if (mpReadWriteThread == null || !mpReadWriteThread.isAlive() || !mpReadWriteThread.isConnectionAlive())
        vzRtn = mzConnEstablished = false;
      else
        vzRtn = mzConnEstablished = true;
    }

    return(vzRtn);
  }

  @Override
  public boolean isConnectionEstablished()
  {
    return(mzConnEstablished);
  }

  private Properties initConnectionProperties()
  {
    msHostName = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".HostName");
    mzUseAcks = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseAcks", false);
    mzUseHeartBeats = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseHeartBeats", false);
    mnConnectionPort = Application.getInt(Application.HOSTCFG_DOMAIN + msCommGroup + ".ListenPort");

    String vsHBMesg = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.HEART_BEAT_MSG);
    String vsMesgPrefix = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_PREFIX);
    String vsMesgSuffix = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_SUFFIX);

    Properties vpProperty = new Properties();

    vpProperty.setProperty(TCPIPConstants.SERVER_IP, msHostName);
    vpProperty.setProperty(TCPIPConstants.LISTEN_PORT, Integer.toString(mnConnectionPort));
    vpProperty.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.CLIENT.getValue());
    vpProperty.setProperty(TCPIPConstants.HEART_BEAT_MSG, (vsHBMesg == null) ? "" : vsHBMesg);
    vpProperty.setProperty(TCPIPConstants.MESSAGE_PREFIX, vsMesgPrefix);
    vpProperty.setProperty(TCPIPConstants.MESSAGE_SUFFIX, vsMesgSuffix);

    return(vpProperty);
  }

  @Override
  public int retransmitPendingMessages(HostServerDelegate hostOutDelegate, StandardHostServer hostServer, int maxRetry)
        throws DBException, HostCommException {
    return 0;
  }

  @Override
  public LocalDateTime lastKeepAliveReceived() {
    return null;
  }

  @Override
  public LocalDateTime connectionEstablished() {
    return null;
  }
}
