package com.daifukuamerica.wrxj.host;

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

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.host.messages.MessageParser;
import com.daifukuamerica.wrxj.host.messages.MessageParserFactory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.util.SKDCUtility;

/**
 * Description:<BR>
 *   Class to Integrate host messages into the Wrx-j database.  This controller
 *   subscribes for HOST_MESG_RECV_EVENT_TYPE events, which prompts it to check
 *   the HostToWrx table for messages to process.
 *
 * @author       A.D.     03/28/05
 * @version      1.0
 * @see com.daifukuamerica.wrxj.common.ipc.MessageEventConsts#HOST_MESG_RECV_EVENT_TYPE
 */
public class HostMessageIntegrator extends Controller
{
  protected DBObject       dbobj;
  protected MessageParser  mesgParser;
  protected StandardHostServer     theHostServer;
  protected HostInDelegate hostInDelegate;
  protected HostToWrxData  hidata;
  
  public HostMessageIntegrator()
  {
  }

 /**
  *  {@inheritDoc}
  */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("HostMessageIntegrator.startup() - Start");

/*==========================================================================
   These are the objects that involve thread local.  In order to assure that
   this thread gets its own copy of a thread local object and then uses this
   same object subsequently throughout any BaseDBInterface object we need to
   them here.
  ==========================================================================*/
    dbobj = new DBObjectTL().getDBObject();
    
    try
    {
      dbobj.connect();
      theHostServer = Factory.create(StandardHostServer.class);
      hostInDelegate = Factory.create(HostInDelegate.class);
                                       // Mark this controller as running.
      super.setControllerStatus(ControllerConsts.STATUS_RUNNING);
    }
    catch (DBException e)
    {
      logger.logException(e, "Error opening Database Connection");
    }
    catch(DBRuntimeException rte)
    {
      logger.logError(rte.getCause().getMessage() + "::Transporter not loaded!");
    }
    logger.logDebug("HostMessageIntegrator.startup() - End");
    checkForUnprocessedMessages();
  }

 /**
  *  {@inheritDoc}
  */
  @Override
  public void shutdown()
  {
    logger.logDebug("HostMessageIntegrator.shutdown() -- Start");
    try
    {
      dbobj.disconnect();
      cleanUp();
      logger.logDebug("Closing DB Connection...");
      
//      MessageParserFactory.closeFactory();
      logger.logDebug("Clearing MessageParser...");
    }
    catch (DBException e)
    {
      logger.logException(e, "Error closing Database Connection");
    }
    logger.logDebug("HostMessageIntegrator.shutdown() -- End");
    super.shutdown();
  }

 /**
  *  {@inheritDoc}
  */
  @Override
  public void initialize(String uniqueControllerName)
  {
    super.initialize(uniqueControllerName);
    subscribeHostMesgReceiveEvent(getName(), false);
  }

 /**
  *  {@inheritDoc} In particular this method directs host related events this
  *  object receives.
  */
  @Override
  public void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();

    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      if (receivedEventType == MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE)
      {
/*---------------------------------------------------------------------------
    For now this is configured to read the oldest host message in the inbound
    data queue and process it with no regard to which host the message came from.
    In the future we may want to tie this controller to a host explicitely
    using a properties configuration setting.
  ---------------------------------------------------------------------------*/
        do
        {
          try
          {
            hostInDelegate.setInfo((Object)null);
            hostInDelegate.setReadLockInfo(true);
            hidata = (HostToWrxData)theHostServer.getOldestDataQueueMessage(hostInDelegate);
            if (hidata != null)
            {
              if (hidata.getMessageIdentifier().trim().length() == 0)
              {
                String vsErr = "Message Identifier field must be correctly " +
                               "specified in the HostToWrx table record!";
                InvalidHostDataException vpIHE = new InvalidHostDataException(vsErr);
                vpIHE.setOriginalSequence(hidata.getOriginalMessageSequence());
                throw vpIHE;
              }
              mesgParser = MessageParserFactory.getParser(getName(),
                                                          hidata.getMessageIdentifier());
              mesgParser.parse(hidata);
                                       // Mark the message as processed.
              hostInDelegate.setInfo(hidata);
              theHostServer.markMessageAsProcessed(hostInDelegate);
            }
          }
          catch(DBException e)
          {
            execErrorHandler(e);
          }
          catch(DBRuntimeException re)
          {
            execErrorHandler(re);
          }
          catch(NullPointerException exc)
          {
            execErrorHandler(exc);
          }
          catch(InvalidHostDataException exc)
          {
            notifyHost(exc);
          }
          catch(Exception exc)
          {
            execErrorHandler(exc);
          }
        } while(hidata != null);
      }
      else
      {
        receivedMessageProcessed = false;
      }
    }
  }

 /**
  *  Mark the message as processed, and log error. <b>This method is for 
  *  exceptions that are internal to WRx and the host does not need to be notified.</b>
  * @param excep the exception to report.
  */
  protected void execErrorHandler(Exception excep)
  {
    try
    {
      theHostServer.markMessageInError(hidata);
    }
    catch(DBException exc)
    {
      logger.logException(exc, "HostMessageIntegrator-->processIPCReceivedMessage");
    }
    finally
    {
      logger.logException(excep,
                          "HostMessageIntegrator-->processIPCReceivedMessage:: " +
                          "Message Sequence = " + hidata.getMessageSequence() +
                          ", Message Identifier = \"" + hidata.getMessageIdentifier() + "\".");
    }
  }

 /**
  * Method to notify host of an error processing a message.
  * @param ipDataException reference to an InvalidHostDataException.  This exception
  *        will contain info. that will be sent to the host, such as the error code,
  *        and the error message, and optonally the specific host the message is
  *        directed at.
  */
  protected void notifyHost(InvalidHostDataException ipDataException)
  {
    if (ipDataException.getInboundMessageErrorFlag())
    {
      execErrorHandler(ipDataException);
    }
    else
    {
      try
      {
        hostInDelegate.setInfo(hidata);
        theHostServer.markMessageAsProcessed(hostInDelegate);
      }
      catch(DBException exc)
      {
        logger.logError("DB update error for inbound host message: " + exc.getMessage());
      }
    }

    int vnErrorCode = (ipDataException.getErrorCode() == 0)? HostError.INVALID_DATA
                                                            : ipDataException.getErrorCode();
    int vnOrigSeqNumber = ipDataException.getOriginalSequence();
    String vsHostName = ipDataException.getHostName();
    String vsErrorMessage = ipDataException.getErrorMessage() + " " +
                         SKDCUtility.appendNestedExceptionMessages(ipDataException);

    try
    {
      theHostServer.writeHostError(vnErrorCode, vnOrigSeqNumber, vsHostName, vsErrorMessage);        
    }
    catch(DBException exc)
    {
      logger.logException(exc, "HostMessageIntegrator-->processIPCReceivedMessage. " +
                          "Host error message failed to add to data queue for sending...");
    }
  }
  
 /**
  *  Method defines a timer task that will check for unprocessed messages in the
  *  
  */
  protected void checkForUnprocessedMessages()
  {
    try
    {
      if (hostInDelegate.unprocessedMessageAvailable())
        publishHostMesgReceiveEvent("", 0, getName());
    }
    catch(DBException exc)
    {
      logger.logError("Error accessing HostToWrx.");
    }
  }

 /**
  *  Sets Objects for garbage collection.
  */
  protected void cleanUp()
  {
    dbobj = null;
    theHostServer.cleanUp();
  }

  /**
   * Factory for ControllerImplFactory.
   *
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>.  Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object.  If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   *
   * <p>This factory initializes the device port and collaborator.</p>
   *
   * @param controllerConfigs configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException
  {
    Controller thisController = new HostMessageIntegrator();
    thisController.setEquipmentPortCKN(controllerConfigs.getString(DEVICE_PORT));
    thisController.setCollaboratorCKN(controllerConfigs.getString(COLLABORATOR));
    return thisController;
  }
}
