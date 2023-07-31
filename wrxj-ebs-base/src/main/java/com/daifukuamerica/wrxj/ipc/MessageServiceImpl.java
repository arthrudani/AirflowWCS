package com.daifukuamerica.wrxj.ipc;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.InvalidDestinationException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Default MessageService using JBoss Messaging
 *
 * @author Stephen Kendorski
 */
public class MessageServiceImpl implements MessageService
{
  /**
   * Suffix of default topic name.
   *
   * <p><b>Details:</b> When the topic name (<code>topicName</code>) ends with
   * <code>USERNAME_TOPIC_SUFFIX</code>, <code>MessageServiceImpl</code>
   * attempts to replace this suffix with the users system login name, in order
   * to create a different topic name.  If the new topic name exists, it is used
   * instead of the original topic name that ended with this suffix.</p>
   *
   * <p>This is mostly a hack to support team development, so that we don't all
   * end up using the same default topic when we are all using the same
   * configuration source.</p>
   */
  private static final String USERNAME_TOPIC_SUFFIX = "SKDCTopic";

  private static Context jndiContext = null;

  private static Map<String, String> startupFailReasons = new ConcurrentHashMap<String, String>();

  private boolean startupOk = true;
  
  private boolean restarting = false;
  
  private MessageService firstFailedInstance = null;

  private Logger logger = null;
  
  private ExceptionListener connectionExceptionListener = null;

  private String msJmsConnectionFactoryName = null;
  
  private String msDestinationName = null;
  
  private String msInitialContextFactory = null;
  
  private String msProviderUrl = null;
  
  private String msUrlPkgPrefixes = null;

  /**
   * JMS Publisher and Subscriber (share a single Connection).
   *
   * We need separate publish and subscribe SESSIONS because the requirement
   * imposed on the JMS provider is that the sending of messages and the
   * asynchronous receiving of messages be processed serially. It is possible
   * to publish-and-subscribe using the same session, but only if the
   * application is publishing from within the onMessage() event handler.
   */
  private ConnectionFactory            mpConnectionFactory = null;

  private Connection                   mpConnection = null;
  
  private Destination                  mpDestination = null;

  /**
   * NOTE: If needed, we can have multiple MessageProducers (with different
   * EventTypes, for example) in the same Session.
   */
  private Session                      mpPublisherSession = null;
  
  private MessageProducer              mpPublisher = null;

  /**
   * NOTE: If needed, we can have multiple MessageConsumers (with different 
   * selectors, for example) in the same Session.
   */
  private Session                      mpSubscriberSession = null;
  
  private List<IpcMessageListener>     mpIpcMessageListenerList = new ArrayList<IpcMessageListener>();

  private MessageEventHandling         mpMessageEventHandler = null;
  
  private boolean                      mzConnectionStarted = false;
  
  private boolean                      mzShuttingDown = false;

  /**
   * Default Constructor
   */
  public MessageServiceImpl()
  {
    this(null);
  }
  
  /**
   * Constructor
   * 
   * @param isDestinationName - The name of the JMS destination to use
   */
  public MessageServiceImpl(String isDestinationName)
  {
    msDestinationName = isDestinationName;
  }

  @Override
  public String getStartupFailReason()
  {
    if (msDestinationName == null)
    {
      initializeDestination();
    }
    return startupFailReasons.get(msDestinationName);
  }
  public void setStartupFailReason(String isReason)
  {
    if (msDestinationName == null)
    {
      initializeDestination();
    }
    if (isReason == null)
    {
      startupFailReasons.remove(msDestinationName);
    }
    else
    {
      startupFailReasons.put(msDestinationName, isReason);
    }
  }

  @Override
  public String getProviderName()
  {
    return Application.getString("IpcMessageService.DeviceType");
  }

  /**
   * If null, set the first failed instance of the message service client to the 
   * passed in parameter.
   * 
   * @param ipFailedInstance a failed message service instance.
   * @return true if this is the first failed message service client
   */
  synchronized private boolean setFirstFailedInstance(MessageService ipFailedInstance)
  {
    boolean vbFirst = false;
    if (firstFailedInstance == null)
    {
      firstFailedInstance = ipFailedInstance;
      vbFirst = true;
    }
    else
    {
      if (firstFailedInstance == ipFailedInstance)
      {
        vbFirst = true;
      }
    }
    return vbFirst;
  }

  @Override
  public void setMessageEventHandler(MessageEventHandling o)
  {
    mpMessageEventHandler = o;
  }

  @Override
  public void initialize(String keyName)
  {
    logger = Logger.getLogger(keyName);
    connectionExceptionListener = new ConnectionExceptionListener();

    logger.logDebug("JMS Configuring \"" + getProviderName() + "\" - initialize");
    initializeDestination();
    
    msJmsConnectionFactoryName = getConfig("IpcMessageService.JmsTopicConnectionFactoryName");
    msInitialContextFactory = getConfig("IpcMessageService.JmsInitialContextFactory");
    msProviderUrl = getConfig("IpcMessageService.JmsProviderUrl");
    msUrlPkgPrefixes = getConfig("IpcMessageService.JmsUrlPkgPrefixes");
    
    // Try to close any connections on exit
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run()
      {
        shutdown();
      }
    });
  }

  /**
   * Initialize the destination for this Message Service
   */
  private void initializeDestination()
  {
    if (msDestinationName == null)
    {
      if (logger == null)
      {
        logger = Logger.getLogger();
      }
      // Message service with default destination
      msDestinationName = getConfig("IpcMessageService.JmsTopicName");
      
      // Helper to redirect topics by user for development
      if (msDestinationName.endsWith(USERNAME_TOPIC_SUFFIX))
      {
        boolean vbUserNameTopicFound = true;
        String vsUserNameTopic = msDestinationName.substring(0,
            msDestinationName.lastIndexOf(USERNAME_TOPIC_SUFFIX))
            + System.getProperty("user.name");
        jndiLookup(vsUserNameTopic, "initializeDestination");
        if (!startupOk)
        {
          vbUserNameTopicFound = false;
        }
        if (vbUserNameTopicFound)
        {
          logger.logDebug("Using topic \"" + vsUserNameTopic
              + "\" instead of \"" + msDestinationName + "\"");
          msDestinationName = vsUserNameTopic;
        }
      }
    }
  }

  /**
   * Get an application property with logging
   * 
   * @param name
   * @return
   */
  private String getConfig(String name) {
    String value = Application.getString(name);
    if (value == null)
    {
      logger.logError("JMS Configuration: \"" + name + "\" is MISSING");
      value = "*MISSING Config!*";
    }
    else
    {
      logger.logDebug("JMS Configuration: \"" + name + "\" = \"" + value + "\"");
    }
    return value;
  }
  
  /**
   * Publish a (Text) Message to the Destination.
   * 
   * @param messageSender the sender's (String) controllersKeyName.
   * @param controllerGroup the message sender's controller group
   * @param messageText the message TEXT string to be sent as the message body.
   * @param messageData an int with data defined by the message type.
   * @param eventType the int EVENT TYPE of the Message.
   * @param event the event selector String used by Subscribers to get this
   *          message.
   */
  @Override
  public synchronized void publishEvent(String messageSender,
      String controllerGroup, String messageText, long messageData,
      int eventType, String event)
  {
    try
    {
      if (startupOk)
      {
        TextMessage publishedMessage = mpPublisherSession.createTextMessage();
        publishedMessage.setText(messageText);
        publishedMessage.setLongProperty(MESSAGE_DATA, messageData);
        publishedMessage.setStringProperty(MESSAGE_SENDER, messageSender);
        publishedMessage.setStringProperty(MESSAGE_SENDER_GROUP, controllerGroup);
        publishedMessage.setIntProperty(MESSAGE_EVENT_TYPE, eventType);

        // Don't let these messages queue up forever
        int vnTimeToLive = DEFAULT_TIME_TO_LIVE; 
        switch (eventType)
        {
          case MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE:
          case MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE:
          case MessageEventConsts.STATUS_EVENT_TYPE:
          case MessageEventConsts.UPDATE_EVENT_TYPE:
            vnTimeToLive = DEFAULT_STATUS_TIME_TO_LIVE;
        }

        // The "event" string is used by the subscriber event selector.
        publishedMessage.setStringProperty(MESSAGE_EVENT, event);
        publishedMessage.setLongProperty(MESSAGE_TIME, new Date().getTime());
        
        // Log the message we are about to publish (but not log or heart beats)
        if ((eventType != MessageEventConsts.LOG_EVENT_TYPE)
            && (eventType != MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE)
            && (eventType != MessageEventConsts.HEARTBEAT_RESPONSE_EVENT_TYPE))
        {
          logTransmittedMessage(messageText, event);
        }
        
        // publish the message
        mpPublisher.send(publishedMessage, DeliveryMode.NON_PERSISTENT,
            DEFAULT_JMS_PRIORITY, vnTimeToLive);
      }
      else
      {
        logger.logDebug("## MessageService NOT Connected/Running -- publishEvent() ##");
      }
    }
    catch (JMSException e)
    {
      startupFail(e, "## MessageService STOPPED Running -- publishEvent() ##");
    }
    catch (NullPointerException e)
    {
      // Message Service NOT running.  The failure has already been logged as
      // an Error, so just log the publish failure as a debug message.
      logger.logDebug("## publishEvent -- Message Service NOT running! ##");
      // If Message Service IS running and mpPublisher IS null, then another
      // controller has restarted the MessageService. Now, reconnect this
      // controller.
      if ((startupOk) && (!restarting) && (mpPublisher == null))
      {
        setupToRestoreConnection();
      }
    }
  }

   /**
   * Casts the message to a TextMessage and gives it to the controller. Our
   * Asynchronous message event handler.
    *
    * @param ipMessage the incoming message
    * @param isSelector ?
    */
   @Override
   public void onMessage(Message ipMessage, String isSelector)
   {
     if (!mzShuttingDown)
     {
       try
       {
         long vlReceivedMsgRxTime = new Date().getTime();
         String vsReceivedText = null; 
         long vlReceivedMsgData = 0;
         String vsReceivedMsgSender = null;
         String vsReceivedMsgControllerGroup = null;
         int viReceivedEventType = 0;
         String vsReceivedEvent = null;
         long vlReceivedMsgTxTime = 0;
         
        if (ipMessage instanceof TextMessage)
        {
        	 TextMessage receivedMessage =  (TextMessage)ipMessage;
	          vsReceivedText =  receivedMessage.getText();

	         try
	         {
	           vlReceivedMsgData = ipMessage.getLongProperty(MESSAGE_DATA);
	           vsReceivedMsgSender = ipMessage.getStringProperty(MESSAGE_SENDER);
	           vsReceivedMsgControllerGroup = ipMessage.getStringProperty(MESSAGE_SENDER_GROUP);
	           viReceivedEventType = ipMessage.getIntProperty(MESSAGE_EVENT_TYPE);
	           vsReceivedEvent = ipMessage.getStringProperty(MESSAGE_EVENT);
	           vlReceivedMsgTxTime = ipMessage.getLongProperty(MESSAGE_TIME);
	         }
	         catch (MessageFormatException e)
	         {
            logger.logException("JMS onMessage -- [" + vsReceivedText + "]", e);
	           return;
	         }
	         catch (JMSException e)
	         {
            logger.logException("JMS onMessage -- [" + vsReceivedText + "]", e);
	           return;
	         }
         }
        else
        {
          logger.logError("JMS onMessage -- Unable to process message class ["
              + ipMessage.getClass().getCanonicalName() + "]");
          return;
        }
        	 
        IpcMessage controllermsg = new IpcMessage(vsReceivedMsgSender,
            vsReceivedMsgControllerGroup, vsReceivedText, vlReceivedMsgData,
            viReceivedEventType, vsReceivedEvent, vlReceivedMsgTxTime,
            vlReceivedMsgRxTime, isSelector);
         if (mpMessageEventHandler != null)
         {
           mpMessageEventHandler.onTextMessage(controllermsg);
         }
         else
         {
          logger.logDebug(
              "controllerMessagingEventHandler == null - Discarding Message - onMessage()");
         }
       }
       catch (JMSException e)
       {
         logger.logError("JMS onMessage -- JMSException: " + e.toString());
       }
     }
   }

  @Override
  public void startup()
  {
    /**
     * Startup our Publisher and then start the Connection (one Connection for
     * all Subscribers and Publishers).
     */
    startupPublisher();
    startupConnection();
  }

  @Override
  public void shutdown()
  {
    shutdownConnection();
    if (mpIpcMessageListenerList != null) {
      mpIpcMessageListenerList.clear();
      mpIpcMessageListenerList = null;
    }
    connectionExceptionListener = null;
    mpMessageEventHandler = null;
    if (this == firstFailedInstance)
    {
      firstFailedInstance = null;
    }
//    logger = null;
  }
  
  private void createConnection()
  {
    if (startupOk)
    {
      if (mpConnectionFactory == null)
      {
        /*
         * Use JNDI to look up a JMS connection factory.
         */
        mpConnectionFactory = (ConnectionFactory)jndiLookup(
                    msJmsConnectionFactoryName, "createConnection()");
      }
      if ((startupOk) && (mpConnectionFactory != null) &&
          (mpConnection == null))
      {
        try
        {
          mpConnection = mpConnectionFactory.createConnection();
          logger.logDebug("JMS createConnection() -- Created");
          mpConnection.setExceptionListener(connectionExceptionListener);
        }
        catch (JMSException e)
        {
          String vsReason = "JMS MessageService STOPPED Running -- createConnection()";
          startupFail(e, vsReason);
        }
      }
    }
  }

  /**
   * A destination (also known as a subject, group, or channel) is located
   * through JNDI. The destination identifies the messages being sent or
   * received. In pub/sub systems, the subscribers subscribe to a given
   * destination, while the publishers associate destinations with the messages
   * they publish.
   */
  @Override
  public void addSubscriber(String selector, String noLocal)
  {
    /*
     * The boolean noLocal was changed to String noLocal for the EventBrokerImpl
     * which needs to know the name of the Listener doing the subscribe for
     * NoLocal The publish will then have the same name in the
     * IpcMessage.messageSender field so the EventBrokerImpl can match these up
     * and ignore the needed ones.
     */
    IpcMessageListener ipcMessageListener = new IpcMessageListener(this, logger);
    ipcMessageListener.setMessageConsumer(null);
    ipcMessageListener.setMessageSelector(selector);
    ipcMessageListener.setNoLocal(noLocal);
    // Keep the message listeners around so that if the connection fails
    // we can restore all the subscribers.
    mpIpcMessageListenerList.add(ipcMessageListener);
    if (startupOk)
    {
      createConnection(); // if not already created
    }
    if (startupOk)
    {
      if (mpDestination == null)
      {
        mpDestination = (Destination)jndiLookup(msDestinationName, "addSubscriber");
      }
      if (startupOk)
      {
        try
        {
          if (mpSubscriberSession == null)
          {
            mpSubscriberSession = mpConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          }
          /*
           * Messages filtered out by a subscriber's message selector will never
           * be delivered to the subscriber. From the subscriber's perspective they
           * simply don't exist.
           *
           * In some cases, a connection may both publish and subscribe to a destination.
           * The subscriber NoLocal attribute allows a subscriber to inhibit the
           * delivery of messages published by its own connection.
           *
           * Parameters:
           *    messageSelector - only messages with properties matching the
           *                      message selector expression are delivered. This
           *                      value may be null.
           *    noLocal - if set, inhibits the delivery of messages published by
           *              its own connection.
           *
           * Throws:
           *    JMSException - if a session fails to create a subscriber due to
           *                   some JMS error or invalid selector.
           *    InvalidDestinationException - if invalid destination specified.
           *    InvalidSelectorException - if the message selector is invalid.
           */
          MessageConsumer vpConsumer = null;
          if (selector.length() >= 1)
          {
            try
            {
              // noLocal - if set, inhibits the delivery of messages published
              // by its own connection.
              vpConsumer = mpSubscriberSession.createConsumer(mpDestination, "Event LIKE '" + selector + "'", noLocal == null ? false : true);
              logger.logDebug("JMS Created OK - addSubscriber()");
            }
            catch (InvalidDestinationException e)
            {
              logger.logError("JMS addSubscriber -- InvalidDestinationException: " + e.toString());
              return;
            }
            catch (InvalidSelectorException e)
            {
              logger.logError("JMS addSubscriber -- InvalidSelectorException: " + e.toString());
              return;
            }
          }
          else
          {
            vpConsumer = mpSubscriberSession.createConsumer(mpDestination);
            logger.logDebug("JMS Created OK - addSubscriber()");
          }
          ipcMessageListener.setMessageConsumer(vpConsumer);
          //
          vpConsumer.setMessageListener(ipcMessageListener);
        }
        catch (JMSException e)
        {
          logger.logError("JMS addSubscriber -- JMSException: " + e.toString());
        }
        catch (NullPointerException e)
        {
          logger.logError("JMS addSubscriber -- NullPointerException: " + e.toString());
        }
      }
    }
  }

  /**
   * A destination (also known as a subject, group, or channel) is located
   * through JNDI. The destination identifies the messages being sent or
   * received. In pub/sub systems, the subscribers subscribe to a given
   * destination, while the publishers associate destinations with the messages
   * they publish.
   */
  private void startupPublisher()
  {
    if (startupOk)
    {
      logger.logDebug("JMS startupPublisher -- Start -- Destination Name \"" + msDestinationName + "\"");
      createConnection(); // if not already created
    }
    if (startupOk)
    {
      if (mpDestination == null)
      {
        mpDestination = (Destination)jndiLookup(msDestinationName, "startupPublisher");
      }
      if (startupOk)
      {
        try
        {
          // false says the Session is NOT transacted.
          mpPublisherSession = mpConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          mpPublisher = mpPublisherSession.createProducer(mpDestination);
          if (mpPublisher == null)
          {
            logger.logDebug("JMS startupPublisher -- mpPublisher == null");
          }
          else
          {
            // By default (JBossMQ) delivery mode IS PERSISTENT, so we need to
            // explicitly make it NON_PERSISTENT.
            mpPublisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
          }
          logger.logDebug("JMS startupPublisher -- Started");
        }
        catch (JMSException e)
        {
          logger.logError("JMS startupPublisher -- JMSException: " + e.toString());
        }
        catch (NullPointerException e)
        {
          logger.logError("JMS startupPublisher -- NullPointerException: " + e.toString());
        }
        logger.logDebug("JMS startupPublisher -- End -- Destination Name \"" + msDestinationName + "\"");
      }
    }
  }

  private void startupConnection()
  {
    if ((startupOk) && (mpConnection != null) && (!mzConnectionStarted))
    {
      try
      {
        mpConnection.start();
        mzConnectionStarted = true;
        logger.logDebug("JMS startupConnection -- Connection Started");
      }
      catch (JMSException e)
      {
        startupFail(e,  "JMS MessageService STOPPED Running -- startupConnection");
      }
    }
  }

  private synchronized void shutdownConnection()
  {
    if (mzShuttingDown) {
      return;
    }
    startupOk = false;
//    logger.logDebug("ControllerMessageService.shutdownConnection() - Start");
    mzShuttingDown = true;
    // First, close the Publisher.
    if (mpPublisher != null)
    {
      try
      {
        synchronized(mpPublisher)
        {
          mpPublisher.wait(40);
          mpPublisher.close();
        }
      }
      catch (Exception e)
      {
      }
      mpPublisher = null;
    }
    // Next, close the Publisher Session.
    if (mpPublisherSession != null)
    {
      try
      {
        synchronized(mpPublisherSession)
        {
          mpPublisherSession.wait(40);
          mpPublisherSession.setMessageListener(null);
          mpPublisherSession.close();
        }
      }
      catch (Exception e)
      {
      }
      mpPublisherSession = null;
    }
    // Next, close the Subscribers.
    Iterator<IpcMessageListener> ipcMessageListenerListIterator = mpIpcMessageListenerList.iterator();
    while (ipcMessageListenerListIterator.hasNext())
    {
      IpcMessageListener ipcMessageListener = ipcMessageListenerListIterator.next();
      MessageConsumer vpMsgConsumer = ipcMessageListener.getMessageConsumer();
      if (vpMsgConsumer != null)
      {
        closeConsumer(vpMsgConsumer);
        ipcMessageListener.setMessageConsumer(null);
      }
      ipcMessageListener = null;
    }
    // Next, close the Consumer Session.
    if (mpSubscriberSession != null)
    {
      synchronized(mpSubscriberSession)
      {
        try
        {
          mpSubscriberSession.wait(40);
        }
        catch (InterruptedException e)
        {
        }
        try
        {
          mpSubscriberSession.setMessageListener(null);
        }
        catch (Exception e)
        {
        }
        try
        {
          mpSubscriberSession.close();
        }
        catch (javax.jms.IllegalStateException e)
        {
        }
        catch (Exception e)
        {
        }
      }
      mpSubscriberSession = null;
    }
    // Finally, close the Connection.
    if (mpConnection != null)
    {
      try
      {
        mpConnection.setExceptionListener(null);
        mpConnection.close();
      }
      catch (JMSException e)
      {
      }
      catch(Exception e)
      {
      }
      mpConnection = null;
    }
    mpConnectionFactory = null;
    mpDestination = null;
    mzConnectionStarted = false;
//    logger.logDebug("ControllerMessageService.shutdownConnection() - End");
  }

  @Override
  public void shutdownSubscriber(String selector)
  {
    if (mpIpcMessageListenerList != null) {
      Iterator<IpcMessageListener> ipcMessageListenerListIterator = mpIpcMessageListenerList.iterator();
      while (ipcMessageListenerListIterator.hasNext())
      {
        IpcMessageListener ipcMessageListener = ipcMessageListenerListIterator.next();
        String s = ipcMessageListener.getMessageSelector();
        if (selector.equals(s))
        {
          ipcMessageListenerListIterator.remove();
          closeConsumer(ipcMessageListener.getMessageConsumer());
          ipcMessageListener.setMessageConsumer(null);
          ipcMessageListener = null;
          break;
        }
      }
    }
  }

  private void closeConsumer(MessageConsumer ipMsgConsumer)
  {
    if (ipMsgConsumer != null)
    {
      synchronized(ipMsgConsumer)
      {
        try
        {
          // If we don't set the message listener to null receiveNoWait() will throw the
          // JMSException: "A message listener is already registered"
          ipMsgConsumer.setMessageListener(null);
        }
        catch (Exception e)
        {
        }
        try
        {
          while (ipMsgConsumer.receiveNoWait() != null) ;
          ipMsgConsumer.close();
        }
        catch (javax.jms.IllegalStateException e)
        {
        }
        catch (Exception e)
        {
        }
      }
      ipMsgConsumer = null;
    }
  }

  private void logTransmittedMessage(String logText, String msgType)
  {
    logger.logTransmittedMessage(logText, msgType);
  }

  /**
   * Creates a JNDI InitialContext object if none exists yet.  Then looks up
   * the string argument and returns the associated object.
   *
   * @param name  the name of the object to be looked up
   * @param caller  the name of the method's caller (for logging)
   *
   * @return		the object bound to <code>name</code>
   */
  private Object jndiLookup(String name, String caller)
  {
    Object obj = null;
    if (jndiContext == null)
    {
      try
      {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, msProviderUrl);
        env.put(Context.INITIAL_CONTEXT_FACTORY, msInitialContextFactory);
        if (msUrlPkgPrefixes != null)
        {
          env.put(Context.URL_PKG_PREFIXES, msUrlPkgPrefixes);
        }
        logger.logDebug("jndiLookup() of \"" + name +
                        "\" - caller \"" + caller +
                        "\" - providerUrl \"" + msProviderUrl +
                        "\" - urlPkgPrefixes \"" + msUrlPkgPrefixes +
                        "\" - initialContextFactory \"" + msInitialContextFactory + "\"");
        jndiContext = new InitialContext(env);
      }
      catch (Exception e)
      {
        String vsReason = "JMS jndiLookup Exception -- \"" +
                          name + "\" - providerUrl \"" + msProviderUrl + "\" " + caller;
        startupFail(e, vsReason);
      }
    }
    if (startupOk)
    {
      try
      {
        obj = jndiContext.lookup(name);
      }
      catch (Exception e)
      {
        String vsReason = "JMS jndiLookup Exception -- \"" +
                          name + "\" - providerUrl \"" + msProviderUrl + "\" " + caller;
        startupFail(e, vsReason);
      }
    }
    return obj;
  }

  /**
   * Log the exception that caused the message service failure and set the
   * startup fail reason.
   * 
   * @param e the exception that caused the failure
   * @param isReason the cause of the failure
   */
  private void startupFail(Exception e, String isReason)
  {
    if (getStartupFailReason() == null)
    {
      setStartupFailReason("MessageService NOT Connected/Running");
      logger.logSparseException(e, isReason);
    }
    startupOk = false;
  }

  /**
   * After a message service failure (JMSException) shutdown the failed
   * connection and then try to start a new message service connection.
   * 
   * @return true if the restoration FAILED, false otherwise
   */
  private boolean restoreConnection()
  {
    logger.logDebug("JMS Attempt restoreConnection()");
    shutdownConnection();
    //
    // Make sure the old connection goes away.
    //
    System.gc();
    mzShuttingDown = false;
    //
    setStartupFailReason(null);
    startupOk = true;
    /**
     * Startup our Publisher and then start the Connection (one Connection for all
     * Subscribers and Publishers).
     */
    startup();
    if (startupOk)
    {
      List<IpcMessageListener> vpIpcMessageListenerList = mpIpcMessageListenerList;
      // Make a new ipcMessageListenerList since addSubscriber()
      // will re-add new ipcMessageListeners.
      mpIpcMessageListenerList = new ArrayList<IpcMessageListener>();
      // Restore all subscribers.
      Iterator<IpcMessageListener> vpIpcMessageListenerListIterator = vpIpcMessageListenerList.iterator();
      while (vpIpcMessageListenerListIterator.hasNext())
      {
        IpcMessageListener vpIpcMessageListener = vpIpcMessageListenerListIterator.next();
        String vsSelector = vpIpcMessageListener.getMessageSelector();
        String vbNoLocal = vpIpcMessageListener.getNoLocal();
        vpIpcMessageListener.setMessageSelector(null);
        vpIpcMessageListener.setMessageConsumer(null);
        addSubscriber(vsSelector, vbNoLocal);
      }
      vpIpcMessageListenerList.clear();    
    }
    boolean vbRetry = false;
    if (! startupOk)
    {
      // The message service did NOT restart its Connection.  Now what...?
      // 1. The Message Service will come back on its own shortly.
      //    If so, how "shortly."
      // 2. The Message Service will be down for an extended time.
      //    While it is down, the entire system is down.
      if (setFirstFailedInstance(this))
      {
        // This is the first message service instance to NOT recover from
        // a message service failure.  Let this instance be the one to attempt
        // periodic restarts.
        vbRetry = true;
      }
      logger.logDebug("JMS Failed restoreConnection()");
    }
    else
    {
      logger.logDebug("JMS Successful restoreConnection()");
    }
    return vbRetry;
  }

  private void setupToRestoreConnection()
  {
    setFirstFailedInstance(this);
    try
    {
      if (mpConnection != null)
      {
        mpConnection.setExceptionListener(null);
      }
      new Thread("ConnectionExceptionListener")
      {
        @Override
        public void run()
        {
          try
          {
            // Give the actual exception some time to finish.
            sleep(300);
          }
          catch (InterruptedException e)
          {
          }
          // restoreConnection() will return true if the connection did NOT
          // restart and this message service instance needs to retry to
          // reconnect.
          while (restoreConnection())
          {
            try
            {
              // Connection NOT restored - Delay and then try again.
              sleep(60000);
            }
            catch (InterruptedException e)
            {
            }
          }
          if (startupOk)
          {
            //
            // We HAVE restarted.
            //
            if (MessageServiceImpl.this == firstFailedInstance)
            {
              firstFailedInstance = null;
            }
            restarting = false;
          }
        }
      }.start();
    }
    catch (JMSException e2)
    {
    }
  }

  private class ConnectionExceptionListener implements ExceptionListener
  {
    @Override
    public void onException(JMSException e)
    {
      logger.logSparseException(e, "ConnectionExceptionListener");
      restarting = true;
      setupToRestoreConnection();
    }
  }
  
}

