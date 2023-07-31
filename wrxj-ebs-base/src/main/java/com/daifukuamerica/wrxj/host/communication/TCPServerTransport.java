package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPConnectionEvent;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.TCPIPSocketCloseEvent;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.impl.TCPIPReaderWriter;
import com.daifukuamerica.impl.TCPIPServerComms;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.Transporter;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * TCP/IP transporter with the ability to handle two tcp/ip ports -- one
 * inbound and the other outbound.
 *
 * @author A.D.
 * @since  15-Jun-2013
 */
public class TCPServerTransport implements Transporter, Runnable
{
  protected Properties  mpConnProperties;

  /** Server Listen Port. */
  protected int         mnListenPort = 0;
  /** The name or IP of this host/Server socket */
  protected String      msHostName = null;
  /*
   * The name of the collaborator (HostMessageIntegrator) that will be notified
   * when we receive a message.
   */
  protected String      msCollaborator = "";
  protected String      msCommGroup = "";
  protected boolean     mzUseAcks;
  protected boolean     mzUseHeartBeats;
  protected volatile boolean  mzConnEstablished = false;
  protected Thread            mpMainTransportThread;

  protected TCPIPLogger       mpLogger;
  protected TCPIPServerComms  mpServComm;
  protected TCPIPReaderWriter mpReadWriteThread;
  protected Controller        mpSystemGateway;
                                       // Database related declarations.
  protected StandardHostServer mpHostServer;

  public TCPServerTransport(Controller ipSystemGateway, String isHostControllerName,
                            String isCommGroup) throws HostCommException
  {
    msCommGroup = isCommGroup;
    mpSystemGateway = ipSystemGateway;
    mpHostServer = Factory.create(StandardHostServer.class, "TCPServerTransport");

    msCollaborator = Application.getString(Application.HOSTCFG_DOMAIN + isHostControllerName + ".Collaborator");
    msHostName = Application.getString(Application.HOSTCFG_DOMAIN + isCommGroup + ".HostName");
    mzUseAcks = Application.getBoolean(Application.HOSTCFG_DOMAIN + isCommGroup + ".UseAcks", false);
    mzUseHeartBeats = Application.getBoolean(Application.HOSTCFG_DOMAIN + isCommGroup + ".UseHeartBeats", false);

    mpConnProperties = initConnectionProperties();
  }

  
  private Properties initConnectionProperties()
  {
    msHostName = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".HostName");
    mzUseAcks = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseAcks", false);
    mzUseHeartBeats = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseHeartBeats", false);

    String vsHBMesg = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.HEART_BEAT_MSG);
    String vsMesgPrefix = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_PREFIX);
    String vsMesgSuffix = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_SUFFIX);

    Properties vpProperty = new Properties();

    vpProperty.setProperty(TCPIPConstants.SERVER_IP, msHostName);
    vpProperty.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.SERVER.getValue());
    vpProperty.setProperty(TCPIPConstants.HEART_BEAT_MSG, (vsHBMesg == null) ? "" : vsHBMesg);
    vpProperty.setProperty(TCPIPConstants.MESSAGE_PREFIX, vsMesgPrefix);
    vpProperty.setProperty(TCPIPConstants.MESSAGE_SUFFIX, vsMesgSuffix);

    return(vpProperty);
  }
  
 /**
  * {@inheritDoc}
  * @return {@inheritDoc}
  */
  @Override
  public int getTransportModel()
  {
    return(Transporter.SERVER_TRANSPORT);
  }

  @Override
  public void setCommPort(int inPort)
  {
    mnListenPort = inPort;
  }

  @Override
  public void setLogger(Logger ipWrxjLogger)
  {
    mpLogger = new TCPIPBaseLoggerImpl(ipWrxjLogger);
  }

  @Override
  public void startTransporter()
  {
    mpMainTransportThread = new Thread(this);
    mpMainTransportThread.start();
  }

  @Override
  public boolean isTransporterAlive()
  {
    return mpMainTransportThread != null && mpMainTransportThread.isAlive() &&
           mpMainTransportThread.getState() != Thread.State.TERMINATED;
  }

  @Override
  public void run()
  {
    final Properties vpProperties = getConnectionProperties();
    mpServComm = new TCPIPServerComms(vpProperties, mpLogger);
    try
    {
                                       // What we do when a client connects to us.
      mpServComm.registerConnectionEvents(new TCPIPConnectionEvent()
      {
        @Override
        public void connectionHandler(SocketChannel ipClientChannel)
        {
          try
          {
            /*
             * Dispose of old connection thread if it's still there.
             */
            if (isHostReachable())
            {
              closeHostConnection();
            }


            TCPIPReadEventImpl vpReadEvent = Factory.create(TCPIPReadEventImpl.class, msHostName, msCommGroup, mpLogger);
            vpReadEvent.useAckNak(mzUseAcks);
                                         // Set up to let HostMessageIntegrator
                                         // know when we receive a message.
            vpReadEvent.setupCollaboratorNotification(mpSystemGateway, msCollaborator);

            TCPIPSocketCloseEvent vpCloseEvt = Factory.create(TCPIPSocketCloseEventImpl.class, msCommGroup, mpSystemGateway, mpLogger);

            /*
             * Setup the Client Thread and its Event handler.
             */
            mpReadWriteThread = new TCPIPReaderWriter(vpProperties, ipClientChannel, mpLogger);
            mpReadWriteThread.registerReadEvent(vpReadEvent);
            mpReadWriteThread.registerCloseEvent(vpCloseEvt);
            mpReadWriteThread.start();
            mzConnEstablished = true;
          }
          catch(TCPIPCommException exc)
          {
            mpLogger.logErrorMessage("Client Connection dropped due to Exception!", exc);
            closeHostConnection();
          }
        }
      });

      mpServComm.connectionWait();     // Loop and wait for each new connnection.
    }
    catch(TCPIPCommException ce)
    {
      mpLogger.logErrorMessage("TCP/IP Server Communication Error", ce);
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
          mpReadWriteThread.sendMessage(vpWrxToHostData.getMessageBytes());

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
  public void closeHostConnection()
  {
    mzConnEstablished = false;
    if (mpReadWriteThread != null)
      mpReadWriteThread.stopThread();
  }

  /**
   * Stop this thread.
   */
  @Override
  public void stopTransporter()
  {
    closeHostConnection();
    /*
     * It is possible that mpServComm is not initialised yet if they start the
     * HostController and then stop it immediately before running this thread.
     */
    if (mpServComm != null)
      mpServComm.stopServer();
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
    boolean vzRtn;

    if (mpReadWriteThread == null || !mpReadWriteThread.isAlive() || !mpReadWriteThread.isConnectionAlive())
      vzRtn = mzConnEstablished = false;
    else
      vzRtn = mzConnEstablished = true;

    return(vzRtn);
  }

  @Override
  public boolean isConnectionEstablished()
  {
    return(mzConnEstablished);
  }

  /**
   * Method loads configurations into a Property file class so that the tcp/ip
   * comms. library can be used.
   *
   * @return reference to Properties object.
   */
  protected Properties getConnectionProperties()
  {
    String vsHBMesg = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.HEART_BEAT_MSG);
    String vsMesgPrefix = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_PREFIX);
    String vsMesgSuffix = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_SUFFIX);
    mnListenPort = Application.getInt(Application.HOSTCFG_DOMAIN + msCommGroup + ".ListenPort");

    Properties vpProperty = new Properties();

    vpProperty.setProperty(TCPIPConstants.SERVER_IP, msHostName);
    vpProperty.setProperty(TCPIPConstants.LISTEN_PORT, Integer.toString(mnListenPort));
    vpProperty.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.SERVER.getValue());
    vpProperty.setProperty(TCPIPConstants.HEART_BEAT_MSG, vsHBMesg);
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
