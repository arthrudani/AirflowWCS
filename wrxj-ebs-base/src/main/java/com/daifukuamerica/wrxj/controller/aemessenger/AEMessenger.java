/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.controller.aemessenger;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.controller.aemessenger.process.GlobalSettingsMessageProcessor;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMClientResponseHandler;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMServerSocketHandler;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMServerSocketThread;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMSocketCloseEvent;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMSocketConnectionEvent;
import com.daifukuamerica.wrxj.controller.aemessenger.tcp.AEMTcpipReaderWriter;
import com.daifukuamerica.wrxj.dbadapter.data.aed.CommunicationTypeData;
import com.daifukuamerica.wrxj.dbadapter.data.aed.Instance;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunications;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunicationsData;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.communication.TCPIPBaseLoggerImpl;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.messageformat.AEMessageEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.wynright.wrxj.app.Wynsoft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for AEMessenger, which is the core Wynsoft way for handling IPC.
 * 
 * To enable the AE Messenger controller:
 * 
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled) 
        VALUES (N'AEMessenger', N'class', N'com.daifukuamerica.wrxj.controller.aemessenger.AEMessenger', N'Wynsoft AEMessenger', 2, 1)
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'internal_port', N'1235', N'Wynsoft AEMessenger Internal Port', 2, 1)
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'port', N'1234', N'Wynsoft AEMessenger Port', 2, 1)
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'type', N'AEMessenger', NULL, 2, 1)
 * 
 * Each source must have a processor defined.  There is no default processor.
 * Here are some samples.  All of these use JsonAeMessageProcessor, but custom
 * processors may be defined for each source if desired.  See GES14 for an
 * example of a system with different processors.
 *  
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'CLE', N'com.daifukuamerica.wrxj.controller.aemessenger.process.json.JsonAeMessageProcessor', N'AEMessenger Processor for Convey', 2, 1)
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'CME', N'com.daifukuamerica.wrxj.controller.aemessenger.process.json.JsonAeMessageProcessor', N'AEMessenger Processor for Communicator', 2, 1)
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'PPE', N'com.daifukuamerica.wrxj.controller.aemessenger.process.json.JsonAeMessageProcessor', N'AEMessenger Processor for Order Fulfillment', 2, 1)
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'VIE_WEB', N'com.daifukuamerica.wrxj.controller.aemessenger.process.json.JsonAeMessageProcessor', N'AEMessenger Processor for Visibility', 2, 1)
    INSERT CONTROLLERCONFIG (sController, sPropertyName, sPropertyValue, sPropertyDesc, iScreenChangeAllowed, iEnabled)
        VALUES (N'AEMessenger', N'WGE', N'com.daifukuamerica.wrxj.controller.aemessenger.process.json.JsonAeMessageProcessor', N'AEMessenger Processor for Wynsoft Global', 2, 1)
 * 
 * @author mandrus
 */
public class AEMessenger extends Controller
{
  private Map<Integer, String> mpInstanceCommsMap;
  private Map<Long, AEMessageProcessor> mpIntProcMap = new HashMap<>();
  private Map<Long, AEMessageProcessor> mpExtProcMap = new HashMap<>();

  /*========================================================================*/
  /*  AEMessage Socket Stuff                                                */
  /*========================================================================*/
  /**
   * Get connection information
   */
  private class AEConnectionHandler implements AEMSocketConnectionEvent
  {
    @Override
    public void onSocketConnection(AEMTcpipReaderWriter ipAEMSocket)
    {
      mpServerConnections.add(ipAEMSocket);
      reportConnectionStatus();
    }
  }
  
  /**
   * Get disconnection information
   */
  private class AEDisconnectionHandler implements AEMSocketCloseEvent
  {
    @Override
    public void onSocketClose(AEMTcpipReaderWriter ipThread)
    {
      setDetailedControllerStatus("Disconnected");
      Set<AEMTcpipReaderWriter> vpConns = new HashSet<>();
      vpConns.addAll(mpServerConnections);
      for (AEMTcpipReaderWriter vpConnection : vpConns)
      {
        if (!vpConnection.isConnectionAlive())
        {
          mpServerConnections.remove(vpConnection);
        }
      }
      reportConnectionStatus();
    }
  }
  
  private TCPIPLogger mpCommLogger;
  private AEMServerSocketThread mpServComm1; // xxx_AED.dbo.AES_SYS_INSTANCES.PORT
  private AEMServerSocketThread mpServComm2; // xxx_AED.dbo.AES_SYS_INSTANCES.INTERNAL_PORT
  private AEMServerSocketHandler mpSocketHandler1, mpSocketHandler2;
  private Set<AEMTcpipReaderWriter> mpServerConnections;
  private Map<String, AEMTcpipReaderWriter> mpClientConnections;
  
  private InstanceData mpInstData;
  
  public AEMessenger()
  {
  }
  
  
  /*========================================================================*/
  /* Overridden methods                                                     */
  /*========================================================================*/
  
  /**
   * Initialize
   */
  @Override
  protected void initialize(String isControllerKeyName)
  {
    super.initialize(isControllerKeyName);
    
    try
    {
      mpInstData = Factory.create(Instance.class).getData(Wynsoft.getInstanceId());
    }
    catch (DBException e)
    {
      logger.logException(
          "Unable to read AE instance data [" + Wynsoft.getInstanceId() + "]",
          e);
    }
    
    startupTopicSubscriber(MessageEventConsts.AE_MESSENGER_EVENT_TYPE_TEXT, false);
  }
  
  /**
   * Process JMS messages
   */
  @Override
  protected void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      switch (receivedEventType)
      {
        case MessageEventConsts.AE_MESSENGER_EVENT_TYPE:
          receivedMessageProcessed = processAEMessage();
          break;
        default:
          receivedMessageProcessed = false;
      }
    }
  }
  
  /**
   * Heartbeat
   */
  @Override
  protected void processHeartbeatRequestEvent()
  {
    super.processHeartbeatRequestEvent();
    
    // Check up on our clients
    for (String s : mpClientConnections.keySet())
    {
      try
      {
        refreshClientSocket(s, mpClientConnections.get(s));
      }
      catch (TCPIPCommException e)
      {
        logger.logException("Unable to refresh connection for " + s, e);
      }
    }
  }
  
  /**
   * Start up
   */
  @Override
  protected void startup()
  {
    super.startup();

    // Loggers
    mpCommLogger = new TCPIPBaseLoggerImpl(logger);

    // Initialize configuration
    initializeConfiguration();

    // Wynsoft -> Warehouse Rx
    initializeServers();
    
    // Warehouse Rx -> Wynsoft
    initializeClients();

    // Controller status
    reportConnectionStatus();
    setControllerStatus(ControllerConsts.STATUS_RUNNING);
  }
  
  /**
   * Shut down
   */
  @Override
  protected synchronized void shutdown()
  {
    setDetailedControllerStatus("Shutting down...");

    // Servers
    try
    {
      mpServComm1.interrupt();
      mpServComm1.join();
    }
    catch (Exception e) {}

    try
    {
      mpServComm2.interrupt();
      mpServComm2.join();
    }
    catch (Exception e) {}

    List<AEMTcpipReaderWriter> vpConnList = new ArrayList<>(mpServerConnections);
    for (AEMTcpipReaderWriter vpConn : vpConnList)
    {
      vpConn.stopThread();
    }
    mpServerConnections.clear();

    // Clients
    vpConnList = new ArrayList<>(mpClientConnections.values());
    for (AEMTcpipReaderWriter vpConn : vpConnList)
    {
      vpConn.stopThread();
    }
    mpServerConnections.clear();
    
    setDetailedControllerStatus(" ");
    
    super.shutdown();
  }

  /*========================================================================*/
  /*  JMS Message processing                                                */
  /*========================================================================*/

  /**
   * Process Custom Messages
   * 
   * @param isReceivedText
   * @return
   */
  protected boolean processAEMessage()
  {
    try
    {
      // Send messages via AEMessenger to other Wynsoft products
      AEMessageEventDataFormat vpAEMEDF = Factory.create(
          AEMessageEventDataFormat.class, getClass().getSimpleName());
      vpAEMEDF.setDataString(receivedText);
      if (vpAEMEDF.decodeDataString())
      {
        sendMessage(vpAEMEDF.getTargetInstanceName(), vpAEMEDF.getMessageText(),
            vpAEMEDF.getMessageData(), receivedDataLong);
      }
      else
      {
        throw new Exception("Unable to decode message");
      }
    }
    catch (Exception e)
    {
      logger.logException("Error processing message: " + receivedText, e);
      setDetailedControllerStatus("Error processing message (see log)");
    }
    return true;
  }

  /**
   * Refresh a client socket
   * 
   * @param isName
   * @param ipSocket
   * @return
   * @throws TCPIPCommException
   */
  private AEMTcpipReaderWriter refreshClientSocket(String isName,
      AEMTcpipReaderWriter ipSocket) throws TCPIPCommException
  {
    if (!ipSocket.isConnecting() && !ipSocket.isConnectionAlive())
    {
      logger.logError(String.format("Reconnecting to Target=[%1$s]", isName));
      ipSocket.stopThread();
      ipSocket = new AEMTcpipReaderWriter(ipSocket);
      mpClientConnections.put(isName, ipSocket);
      ipSocket.start();
    }
    return ipSocket;
  }
  
  /**
   * Send an AE Message to another Wynsoft instance
   * 
   * @param isTarget
   * @param isMessageText
   * @param isMessageData
   * @param ilTransactionID
   */
  private void sendMessage(String isTarget, String isMessageText,
      String isMessageData, long ilTransactionID)
  {
    try
    {
      AEMTcpipReaderWriter vpSocket = mpClientConnections.get(isTarget);
      if (vpSocket == null)
      {
        logger.logError(String.format(
            "No connection found for AE Message. Target=[%1$s], Message=[%2$s]",
            isTarget, isMessageData));
      }
      else
      {
        vpSocket = refreshClientSocket(isTarget, vpSocket);
        if (ilTransactionID <= 0)
        {
          ilTransactionID = Math.abs(System.nanoTime());
        }
        if (!vpSocket.queueMessage(ilTransactionID, isMessageData))
        {
          logger.logError(String.format(
              "Unable to buffer message to send (buffer full). Target=[%1$s], Message=[%2$s]",
              isTarget, isMessageData));
        }
        else
        {
          // Log equipment log transmission here.  Comm log will show actual
          // transmission time
          getAEMessageEqLogger(isTarget).logTxEquipmentMessage(isMessageData,
              isMessageText);
        }
      }
    }
    catch (TCPIPCommException e)
    {
      logger.logException(String.format(
          "Error sending AE Message. Target=[%1$s], Message=[%2$s]", isTarget,
          isMessageData), e);
    }
  }

  /*========================================================================*/
  /*  Configuration                                                         */
  /*========================================================================*/

  /**
   * Properties for server socket
   *
   * @return reference to Properties object.
   */
  private Properties getServerProps(boolean izInternal)
  {
    Properties vpProperty = new Properties();

    int vnPort = izInternal ? mpInstData.getInternalPort() : mpInstData.getPort();
    
    vpProperty.setProperty(TCPIPConstants.SERVER_IP, "");
    vpProperty.setProperty(TCPIPConstants.LISTEN_PORT, Integer.toString(vnPort));
    vpProperty.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.SERVER.getValue());
    vpProperty.setProperty(AEMTcpipReaderWriter.MSG_SOURCE, Integer.toString(Wynsoft.getInstanceId()));

    return vpProperty;
  }

  /**
   * Properties for client socket
   *
   * @return reference to Properties object.
   */
  private Properties getClientProps(String isHostName, int inPort, String isTargetName)
  {
    Properties vpProperty = new Properties();

    vpProperty.setProperty(TCPIPConstants.SERVER_IP, isHostName);
    vpProperty.setProperty(TCPIPConstants.LISTEN_PORT, Integer.toString(inPort));
    vpProperty.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.CLIENT.getValue());
    vpProperty.setProperty(AEMTcpipReaderWriter.MSG_SOURCE, Integer.toString(Wynsoft.getInstanceId()));
    vpProperty.setProperty(AEMTcpipReaderWriter.MSG_TARGET, isTargetName);
    vpProperty.setProperty(TCPIPConstants.CLIENT_RETRY_INTERVAL, "3");

    return vpProperty;
  }

  /**
   * Set up processors
   */
  @SuppressWarnings("unchecked")
  private void initializeConfiguration()
  {
    // Global Settings changes
    addMessageProcessor(true, 1101, new GlobalSettingsMessageProcessor("AED"));
    
    try
    {
      // Connect to the DB
      new DBObjectTL().getDBObject().connect();
      
      // Load processors from database
      InstanceCommunications vpIC = Factory.create(InstanceCommunications.class);
      mpInstanceCommsMap = vpIC.getAeMessengerConnections();
      for (Integer i : mpInstanceCommsMap.keySet())
      {
        String vsProcessorClassName = getProcessorClassName(mpInstanceCommsMap.get(i));
        if (SKDCUtility.isFilledIn(vsProcessorClassName))
        {
          try
          {
            Class<AEMessageProcessor> vpClass = 
                (Class<AEMessageProcessor>)Class.forName(vsProcessorClassName);
            addMessageProcessor(false, i, Factory.create(vpClass, mpInstanceCommsMap.get(i)));
          }
          catch (ClassNotFoundException e)
          {
            logger.logException(e,
                "Error adding processor for " + mpInstanceCommsMap.get(i));
          }
        }
        else
        {
          logger.logError("No processor defined for " + mpInstanceCommsMap.get(i));
        }
      }
    }
    catch (DBException dbe)
    {
      logger.logException("Error getting AEMessenger connections", dbe);
    }
  }

  /**
   * Get the class name for a product
   * @param isProduct
   * @return
   */
  private String getProcessorClassName(String isProduct)
  {
    return Application.getString(
        Application.CONTROLLERCFG_DOMAIN + getName() + "." + isProduct);
  }
  
  /**
   * Add a message processor
   * 
   * @param izInternal
   * @param inSender
   * @param ipMsgProc
   */
  private void addMessageProcessor(boolean izInternal, long inSender,
      AEMessageProcessor ipMsgProc)
  {
    if (izInternal)
    {
      mpIntProcMap.put(inSender, ipMsgProc);
    }
    else
    {
      mpExtProcMap.put(inSender, ipMsgProc);
    }
    logger.logOperation(
        "Adding " + (izInternal ? "internal" : "external") + " processor "
            + ipMsgProc.getClass().getSimpleName() + " for " + inSender);
  }

  
  /*========================================================================*/
  /*  Server (Wynsoft -> Warehouse Rx)                                      */
  /*========================================================================*/

  /**
   * Initialize the server for inbound AEMessenger messages.
   */
  private void initializeServers()
  {
    mpServerConnections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    if (mpInstData == null)
    {
      // Impossible to derive connection info
      return;
    }
    
    // Server for xxx_AED.dbo.AES_SYS_INSTANCES.PORT
    Properties vpProps1 = getServerProps(false);
    mpSocketHandler1 = new AEMServerSocketHandler(getName(), vpProps1,
        mpExtProcMap, new AEConnectionHandler(), new AEDisconnectionHandler());
    mpServComm1 = new AEMServerSocketThread(vpProps1, mpCommLogger, mpSocketHandler1);
    
    // Server for xxx_AED.dbo.AES_SYS_INSTANCES.INTERNAL_PORT
    Properties vpProps2 = getServerProps(true);
    mpSocketHandler2 = new AEMServerSocketHandler(getName(), vpProps2,
        mpIntProcMap, new AEConnectionHandler(), new AEDisconnectionHandler());
    mpServComm2 = new AEMServerSocketThread(vpProps2, mpCommLogger, mpSocketHandler2);
    
    mpServComm1.start();
    mpServComm2.start();
  }
  

  /*========================================================================*/
  /*  Client (Warehouse Rx -> Wynsoft)                                      */
  /*========================================================================*/

  /**
   * Initialize the client for outbound AEMessenger messages.
   */
  private void initializeClients()
  {
    mpClientConnections = new HashMap<>();
    try
    {
      InstanceCommunicationsData vpICKey = Factory.create(InstanceCommunicationsData.class);
      vpICKey.setKey(InstanceCommunicationsData.SENDER_ID_NAME, Wynsoft.getInstanceId());
      vpICKey.setKey(InstanceCommunicationsData.COMMUNICATION_TYPE_ID_NAME, CommunicationTypeData.COMM_TYPE_TCP);
      List<InstanceCommunicationsData> vpInstances = Factory.create(InstanceCommunications.class).getAllElementsAsData(vpICKey);
      for (InstanceCommunicationsData vpICData : vpInstances)
      {
        InstanceData vpInstData = Factory.create(Instance.class).getData(vpICData.getReceiverId());
        logger.logOperation("Adding client connection to "
            + vpInstData.getIdentityName() + " on "
            + vpInstData.getComputerName() + ":" + vpInstData.getPort());
        
        Properties vpProps = getClientProps(vpInstData.getComputerName(), vpInstData.getPort(), vpInstData.getIdentityName());
        AEMTcpipReaderWriter vpClient = new AEMTcpipReaderWriter(vpProps,
            new TCPIPBaseLoggerImpl(getAEMessageCommLogger(vpInstData.getIdentityName())));
        mpClientConnections.put(vpInstData.getIdentityName(), vpClient);
        
        vpClient.registerReadEvent(
            new AEMClientResponseHandler(vpInstData.getIdentityName(),
                mpExtProcMap.get((long)vpInstData.getProductID()), logger));
        vpClient.start();
      }
    }
    catch (DBException dbe)
    {
      logger.logException("DB Error setting up AE Senders", dbe);
    }
    catch (Exception e)
    {
      logger.logException("Error setting up AE Senders", e);
    }
  }
  

  /*========================================================================*/
  /* Utilities                                                              */
  /*========================================================================*/
  
  /**
   * Report the status of our connections
   */
  private void reportConnectionStatus()
  {
    if (mpServerConnections.size() == 0)
    {
      if (mpInstData != null)
      {
        setDetailedControllerStatus("Listening on ports " + mpInstData.getPort()
            + " & " + mpInstData.getInternalPort());
      }
      else
      {
        setDetailedControllerStatus("Not listening; instance "
            + Wynsoft.getInstanceId() + " undefined.");
      }
    }
    else
    {
      setDetailedControllerStatus(
          mpServerConnections.size() + " inbound connection"
              + (mpServerConnections.size() == 1 ? "" : "s"));
    }
    
    // TODO: Wynsoft-Persist AE connection info somewhere so status can be seen
  }
  
  /**
   * Transaction ID
   */
  private int mnLastTransactionID = (int)new Date().getTime();
  @SuppressWarnings("unused")
  private synchronized int getTransactionID()
  {
    return ++mnLastTransactionID;
  }
  
  /**
   * Get the logger for WRx <-> Wynsoft AE Communications
   * @param isProduct
   * @return
   */
  public static Logger getAEMessageEqLogger(String isProduct)
  {
    Logger vpLogger = Logger.getLogger("AEMessenger_" + isProduct);
    vpLogger.addEquipmentLogger();
    return vpLogger;
  }

  /**
   * Get the logger for WRx <-> Wynsoft AE Communications
   * @param isProduct
   * @return
   */
  public static Logger getAEMessageCommLogger(String isProduct)
  {
    Logger vpLogger = Logger.getLogger("AEMessengerPort_" + isProduct);
    vpLogger.addCommLogger();
    return vpLogger;
  }

  /*========================================================================*/
  /* Required Factory Method                                                */
  /*========================================================================*/
  
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
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    AEMessenger vpController = Factory.create(AEMessenger.class);
    vpController.setCollaboratorCKN(ipConfig.getString(COLLABORATOR));
    return vpController;
  }
}
