package com.daifukuamerica.wrxj.host.communication;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.DefaultHostDBDelegate;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.Transporter;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.DBCommException;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.Reflected;

import java.time.LocalDateTime;
import java.util.HashMap;


/**
 *  JDBC implementation of a data transporter.  This class does all of the work
 *  of sending and receiving messages from a host database.
 *  @author       A.D.
 *  @version      1.0   02/21/2005
 */
@Reflected
public class JDBCTransporter implements Transporter, Runnable
{
  private final int                mnHostInCheckInterval;
  private final String             msHostName;
  private final String             msCollaborator;
  private final String             msCommGroup;
  private Logger             mpMessageLogger;
  private final Controller         mpSystemGateway;
  private volatile boolean   mzInterrupted = false;
  private boolean            mzLogMessages = false;
  private boolean            mzErrorLoggedOnce = false;
  private boolean            mzExceptionLoggedOnce = false;
  private volatile boolean   mzConnectionEstablished = false;
  private Thread             mpJDBCThread;

 /**
  *  Constructor allows for specifying which host we are talking to.
  * @param ipSystemGateway a reference to the system gateway.
  * @param isInvokerName the name of the calling object.
  * @param isCommGroup  The connection configuration info.
  */
  public JDBCTransporter(Controller ipSystemGateway, String isInvokerName, String isCommGroup)
  {
    msCommGroup = isCommGroup;
    mpSystemGateway = ipSystemGateway;
    msCollaborator = Application.getString(Application.HOSTCFG_DOMAIN + isInvokerName + ".Collaborator");

    msHostName = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".HostName");
    mzLogMessages = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".LogHostMessages", true);

    mnHostInCheckInterval = Application.getInt(Application.HOSTCFG_DOMAIN + isInvokerName + ".HostInCheckInterval", 15);
    HostCommHelper.init();
   }

  @Override
  public void setCommPort(int inPort)
  {
    // Method stub to satisfy interface.
  }

  @Override
  public void startTransporter() throws HostCommException
  {
    mzInterrupted = false;
    mpJDBCThread = new Thread(this);
    mpJDBCThread.setName("JDBCTransporter_" + mpJDBCThread.getId());
    mpJDBCThread.start();
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
  public void setLogger(Logger ipWrxjLogger)
  {
    mpMessageLogger = ipWrxjLogger;
    if (mzLogMessages)
    {
      if (mpMessageLogger.getCommLogger() == null)
      {
        mpMessageLogger.addCommLogger();
      }
    }
  }

 @Override
  public void run()
  {
   DBObject vpHostDBObj = null;
/*===========================================================================
       Reserve our connections to the host and local database.
  ===========================================================================*/
    try
    {
                                       // Make local DB connection
      DBObject vpDBObj = new DBObjectTL().getDBObject();
      if (!vpDBObj.checkConnected()) vpDBObj.connect();
      vpHostDBObj = connectToHost();
      mzErrorLoggedOnce = false;
    }
    catch(DBException e)
    {
      if (!mzErrorLoggedOnce)
      {
        mpMessageLogger.logError("Error connecting to Warehouse Rx database.");
        mzErrorLoggedOnce = true;
      }
    }
    catch(HostCommException e)
    {
      if (!mzErrorLoggedOnce)
      {
        mpMessageLogger.logError("Error connecting to remote host database.");
        mzErrorLoggedOnce = true;
      }
    }
                                       // If still nothing established, leave!
    if (!mzConnectionEstablished) return;

    HostInDelegate vpHostInDelegate = Factory.create(HostInDelegate.class);
    DefaultHostDBDelegate vpHostDBDelegate = Factory.create(DefaultHostDBDelegate.class, null, vpHostDBObj);
    StandardHostServer vpHostServer = Factory.create(StandardHostServer.class, "JDBCTransporter");

    while(!mzInterrupted)
    {
      try
      {
        vpHostDBDelegate.setInfo(msHostName);
        if (vpHostServer.unprocessedMessageAvailable(vpHostDBDelegate))
        {
          if (recvMessages(vpHostInDelegate, vpHostDBDelegate, vpHostServer) > 0)
          {
            mpSystemGateway.publishHostMesgReceiveEvent("", 0, msCollaborator);
          }
        }
        else if (vpHostServer.unprocessedMessageAvailable(new HostInDelegate(msHostName)))
        {
          mpSystemGateway.publishHostMesgReceiveEvent("", 0, msCollaborator);
        }

        try { Thread.sleep(mnHostInCheckInterval*1000); }
        catch(InterruptedException e) {}
        mzExceptionLoggedOnce = false;
      }
      catch(DBCommException vpCE)
      {
        mzConnectionEstablished = false;
        if (!DBHelper.dbReconnect(vpCE.getDBIdentity(), 1, mpMessageLogger))
        {
          mpMessageLogger.logError("Database reconnect failed!");
          closeHostConnection();
        }
        else
        {
          mzConnectionEstablished = true;
          mpMessageLogger.logOperation("++++ Database reconnect to " +
                                       vpCE.getDBIdentity() + " successful!");
        }
      }
      catch(Exception vpExc)
      {
        if (!mzExceptionLoggedOnce)
        {
          mpMessageLogger.logException("Error accessing Warehouse Rx/Host database..." +
                                       " Stopping Host communication thread", vpExc);
          mzExceptionLoggedOnce = true;
        }
        closeHostConnection();
      }
    } // End While
  }

 @Override
  public void sendHeartBeat() throws HostCommException
  {
    // Stub to satisfy interface.  For a JDBC connection there is no heartbeat
    // message.
  }

 /**
  * {@inheritDoc}
  * @param ipHostOutDelegate the delegate to send for outbound processing.
  * @return Number of message sent successfully.
  * @throws com.daifukuamerica.wrxj.jdbc.DBException if database error occurs on
  *         either wrx-j or host system.
  * @throws HostCommException if there is an error communicating
  *         with the host system.
  */
  @Override
  public synchronized int sendMessages(HostServerDelegate ipHostOutDelegate,
                                       StandardHostServer ipHostServer)
         throws DBException, HostCommException
  {
    int vnMessagesSent = 0;

    try
    {
                                       // Make local DB connection
      DBObject vpDBObj = new DBObjectTL().getDBObject();
      if (!vpDBObj.checkConnected()) vpDBObj.connect();

      connectToHost();

      DefaultHostDBDelegate vpHostDBDelegate = Factory.create(DefaultHostDBDelegate.class,
                                   Application.HOSTCFG_DOMAIN + msCommGroup);
      WrxToHostData mpHodata = null;
      WrxToHostData mpCurrentWrxjRecord = null;

      do
      {
        HashMap<String, TransactionToken> vpTransactionMap = null;
        try
        {
          vpTransactionMap = vpHostDBDelegate.startTransactions();
                                       // Give delegate information related to
                                       // the task it will perform.
          ipHostOutDelegate.setInfo(msHostName);
          ipHostOutDelegate.setReadLockInfo(true);
                                       // Read outbound message from WrxToHost
                                       // table.
          mpHodata = (WrxToHostData)ipHostServer.getOldestDataQueueMessage(ipHostOutDelegate);
          if (mpHodata != null)
          {                            // Save this off for later use.
            mpCurrentWrxjRecord = mpHodata.clone();
                                       // Write data to the Host database.
            vpHostDBDelegate.setInfo(mpHodata);
            ipHostServer.addToDataQueue(vpHostDBDelegate);
                                       // Mark WRx-J message as processed.
            ipHostOutDelegate.setInfo(mpCurrentWrxjRecord);
            ipHostServer.markMessageAsProcessed(ipHostOutDelegate);
            vpHostDBDelegate.commitTransactions(vpTransactionMap);
            vnMessagesSent++;
          }
        }
        catch(DBException e)
        {
          mpMessageLogger.logException(e, "Inside JDBCTransporter-->writeOutboundMessages");
          vpHostDBDelegate.endTransactions(vpTransactionMap);
          ipHostServer.markMessageInError(mpCurrentWrxjRecord);
        }
        finally
        {
          vpHostDBDelegate.endTransactions(vpTransactionMap);
        }
      } while(mpHodata != null);
    }
    catch(DBException exc)
    {
      Throwable e = exc.getCause();
      if (e != null && e instanceof java.sql.SQLException)
      {
        String vsErrorMessage = e.getMessage();
        if (vsErrorMessage.contains("Connection") || vsErrorMessage.contains("socket"))
        {
          throw new HostCommException("Connection to host has been dropped");
        }
      }
      else
      {
        throw exc;
      }
    }
    return(vnMessagesSent);
  }

 /**
  * {@inheritDoc}
  */
  @Override
  public void closeHostConnection()
  {
    if (mpJDBCThread != null && mpJDBCThread.isAlive())
    {
      mzInterrupted = true;
      mpJDBCThread.interrupt();
    }
    else
    {
      mzInterrupted = false;
    }

    mzConnectionEstablished = false;
    try
    {
                                       // Retrieve our host connection from the pool.
      DBObject vpHostDBObj = new DBObjectTL().getDBObject(Application.HOSTCFG_DOMAIN + msCommGroup);
      if (vpHostDBObj != null && vpHostDBObj.checkConnected())
        vpHostDBObj.disconnect(false);
                                       // Close local DB connection too.
      DBObject vpDBObj = new DBObjectTL().getDBObject();
      vpDBObj.disconnect();
    }
    catch(DBException exc)
    {
      mpMessageLogger.logException(exc);
    }
    catch(Exception exc)
    {
      mpMessageLogger.logException(exc);
    }
  }

  /**
   * Stop this thread.
   */
  @Override
public void stopTransporter()
  {
    closeHostConnection();
  }

  @Override
  public boolean isTransporterAlive()
  {
    return mpJDBCThread != null && mpJDBCThread.isAlive() &&
           mpJDBCThread.getState() != Thread.State.TERMINATED;
  }

 /**
  * Method checks if the host connection is valid.
  * @return <code>true</code> if host is accepting connections.
  */
  @Override
  public boolean isHostReachable()
  {
    DBObject vpDBObj = new DBObjectTL().getDBObject(Application.HOSTCFG_DOMAIN + msCommGroup);
    try
    {
      if (!vpDBObj.checkConnected())
      {
        vpDBObj.connect();
        setErrorCodes(vpDBObj);
      }

      mzConnectionEstablished = vpDBObj.isConnectionActive();
    }
    catch(DBException ex)
    {
      mpMessageLogger.logException(ex);
    }
    catch(HostCommException ex)
    {
      mpMessageLogger.logException(ex);
    }

    return(mzConnectionEstablished);
  }

  /**
   * Is the connection established?
   * @return boolean
   */
  @Override
  public boolean isConnectionEstablished()
  {
    return(mzConnectionEstablished);
  }

/*----------------------------------------------------------------------------
                          Private Methods Section.
  ----------------------------------------------------------------------------*/
 /**
  * Method reads from the host table all inbound messages it finds and adds
  * them to the HostToWrx data queue.
  * @param ipHostInDelegate the delegate to send for Inbound operations.
  * @param ipHostDBDelegate the delegate to send for Host DB operations
  *        (this will vary by target DB type)..
  * @param ipHostServer the Host Transaction Mediator.
  * @throws DBException if there is a error receiving the Host messages.
  * @return Number of successfully received messages.
  */
  protected int recvMessages(HostInDelegate ipHostInDelegate,
                             DefaultHostDBDelegate ipHostDBDelegate,
                             StandardHostServer ipHostServer) throws DBException
  {
    int vnSuccessfulRecvCount = 0;
    HostToWrxData vpHiData = null;
    HashMap<String, TransactionToken> vpTransactionMap = null;

    do
    {
      try
      {
        vpTransactionMap = ipHostDBDelegate.startTransactions();
                                       // Tell the host delegate that it needs
                                       // to know to read the oldest message
                                       // with a lock and nothing else.
        ipHostDBDelegate.setInfo((Object)null);
        ipHostDBDelegate.setReadLockInfo(true);

        vpHiData = (HostToWrxData)ipHostDBDelegate.getOldestUnprocessedMessage();
        if (vpHiData != null)
        {
                                       // Write data into the HostToWrx Table.
          vpHiData.setHostName(msHostName);
          ipHostInDelegate.setInfo(vpHiData);
          ipHostServer.addToDataQueue(ipHostInDelegate);

                                       // Delete processed message from host DB.
          ipHostDBDelegate.setInfo(Integer.valueOf(vpHiData.getOriginalMessageSequence()));
          ipHostServer.deleteProcessedMessage(ipHostDBDelegate);
          vnSuccessfulRecvCount++;
          logHostInCommMessage(vpHiData);
        }
        ipHostDBDelegate.commitTransactions(vpTransactionMap);
      }
      catch(DBException e)
      {
        mpMessageLogger.logException(e, "Inside JDBCTransporter-->readInboundMessages");
        if (e.getErrorCode() == HostError.DATA_QUEUE_FULL)
        {
          ipHostDBDelegate.endTransactions(vpTransactionMap);
                                       // We know of these types of errors immediately
                                       // so let Host know about it now.
          try
          {
            ipHostServer.writeHostError(HostError.DATA_QUEUE_FULL, vpHiData.getOriginalMessageSequence(),
                                        msHostName, e.getMessage() +
                                        "Please clear Warehouse Rx data queue of " +
                                        "all processed messages before attempting to " +
                                        "send more messages!");
          }
          catch(DBException exc)
          {
            mpMessageLogger.logException(exc, "Writing message to host!");
          }
        }
        throw e;
      }
      finally
      {
        ipHostDBDelegate.endTransactions(vpTransactionMap);
      }
    } while(vpHiData != null);

    return(vnSuccessfulRecvCount);
  }

  /**
   * Log the Host In message as a comm logger message
   * @param ipMessageIn data object containing message being logged.
   */
  private void logHostInCommMessage(HostToWrxData ipMessageIn)
  {
    if (mzLogMessages)
    {
      String vsMessageString = "[" + ipMessageIn.getMessageSequence() + "] "
          + ipMessageIn.getMessageIdentifier() + ": "
          + new String(ipMessageIn.getMessageBytes());
      byte[] vabMessage = vsMessageString.getBytes();
      mpMessageLogger.logRxByteCommunication(vabMessage, 0, vabMessage.length);
    }
  }

  /**
   * Method to connect to the Host database.
   *
   * @throws HostCommException when cannot connect to host.
   */
  private DBObject connectToHost() throws HostCommException
  {
    DBObject vpHostDBObj = new DBObjectTL().getDBObject(Application.HOSTCFG_DOMAIN + msCommGroup);
    setErrorCodes(vpHostDBObj);

    try
    {
      if (!vpHostDBObj.checkConnected())
      {
        vpHostDBObj.connect();
        mzConnectionEstablished = true;
      }
      else if (!vpHostDBObj.isConnectionActive())
      {
        if (!DBHelper.dbReconnect(vpHostDBObj.getDBIdentifier(), 1, mpMessageLogger))
        {
          mzConnectionEstablished = false;
          mpMessageLogger.logError("Error establishing host connection");
          throw new HostCommException("Error establishing host connection");
        }
        else
        {
          mzConnectionEstablished = true;
        }
      }
    }
    catch(DBCommException e)
    {
      if (!DBHelper.dbReconnect(e.getDBIdentity(), 1, mpMessageLogger))
      {
        mzConnectionEstablished = false;
        mpMessageLogger.logError("Error establishing host connection!");
        throw new HostCommException("Error establishing host connection!");
      }
      else
      {
        mzConnectionEstablished = true;
      }
    }
    catch(DBException e)
    {
      mzConnectionEstablished = false;
      mpMessageLogger.logError("Error establishing host connection....");
      throw new HostCommException("Error establishing host connection...", e);
    }
    catch(Exception e)
    {
      mzConnectionEstablished = false;
      throw new HostCommException("Error creating host connection...", e);
    }

    return vpHostDBObj;
  }

  /**
   * Check for Error code setting in HostConfig to see if there is a DB vendor specific
   * setting there.  If not, assume the error code setting to be the same as
   *
   * @param ipDBOBJ the database connection object.
   */
  private void setErrorCodes(DBObject ipDBOBJ) throws HostCommException
  {
    String vsErrorCodeClassName = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".ErrorCodes");
    if (vsErrorCodeClassName != null)
    {
      try
      {
        ipDBOBJ.setDBErrorCodes(vsErrorCodeClassName);
      }
      catch(Exception exc)
      {
        throw new HostCommException(exc);
      }
    }
  }

  @Override
  public int retransmitPendingMessages(HostServerDelegate hostOutDelegate, StandardHostServer hostServer, int maxRetry)
        throws DBException, HostCommException {
    // TODO Auto-generated method stub
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
