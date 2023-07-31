package com.daifukuamerica.wrxj.host;

import com.daifukuamerica.wrxj.host.communication.JDBCConnectionMonitor;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.communication.AbstractConnectionMonitor;
import com.daifukuamerica.wrxj.host.communication.OutboundTimerThread;
import com.daifukuamerica.wrxj.host.communication.TCPIPConnectionMonitor;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.messageformat.statusevent.StatusEventDataFormat;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:<BR>
 *   Class to direct Host messaging traffic. This class processes events from a
 *   client program wanting to send a message to the host.  It also publishes
 *   events to the {@link #HostMessageIntegrator HostMessageIntegrator} when a
 *   message is received from the host.  Note for multiple hosts we will likely
 *   have multiple HostControllers.
 *
 * @author       A.D.     03/07/05
 * @version      1.0
 */
public class HostController extends Controller
{
  protected StandardHostServer mpHostServer;
  protected HostServerDelegate mpHostDelegate;

  protected boolean          mzPublishStatus = false;
  protected boolean          mzErrorLogged = false;
  private int                mnHostOutCheckInterval;
  protected List<ConnectionInfo> mpCommInfo = new ArrayList<ConnectionInfo>();

  /**
   * public constructor
   */
  public HostController()
  {

  }

  /**
   * @see com.daifukuamerica.wrxj.controller.Controller#startup()
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("HostController.startup() - Start");
/*==========================================================================
   These are the objects that involve thread local.  In order to assure that
   this thread gets its own copy of a thread local object and then uses this
   same object subsequently throughout any BaseDBInterface object we need to
   define database connectivity here.
  ==========================================================================*/
    try
    {
                                       // Make local DB connection
      DBObject vpDBObj = new DBObjectTL().getDBObject();
      vpDBObj.connect();

      mpHostServer = Factory.create(StandardHostServer.class);
      mpHostDelegate = Factory.create(HostOutDelegate.class);

                                       // Mark this controller as running.
      super.setControllerStatus(ControllerConsts.STATUS_RUNNING);
                                       // Get Property info. for this controller.
      String vsCommGroup = getConfigProperty("CommType");
//      if (isUsingTCPIP(vsCommGroup))
//      {
//        if (isUsingDualPortTCPIP())
//        {
//          /*
//           * They want an IN and OUT TCP/IP port (usually for legacy systems -- therefore
//           * BOTH PORTS ARE ASSUMED TO BE CONNECTED TO THE SAME HOST)
//           * The Comm Group in this case is assumed to have a 1 and 2 suffix.  If
//           * it's a single port connection no suffix should be used.
//           */
//          vsHostName = Application.getString(Application.HOSTCFG_DOMAIN + vsCommGroup + "-1" + ".HostName");
//        }
//        else
//        {
//          vsHostName = Application.getString(Application.HOSTCFG_DOMAIN + vsCommGroup + ".HostName");
//        }
//      }
//      else
//      {
//        vsHostName = Application.getString(Application.HOSTCFG_DOMAIN + vsCommGroup + ".HostName");
//      }

      mnHostOutCheckInterval = getConfigPropertyAsInt("HostOutCheckInterval", 30);
      mzPublishStatus = getConfigPropertyAsBoolean("PublishStatus");

      setDetailedControllerStatus("   ");
      createAndStartTransporter(vsCommGroup);
    }
    catch(HostCommException hce)
    {
      logger.logError("****** Host communication failure! ******");
      setDetailedControllerStatus("Host connection failed...");
    }
    catch(DBException e)
    {
      logger.logException(e, "Error opening Database Connection");
      setDetailedControllerStatus("Host connection failed...");
    }
    catch(DBRuntimeException rte)
    {
      String vsNestedCauseMsg = "";
      if (rte.getCause() != null)
      {
        vsNestedCauseMsg = rte.getCause().getMessage() + " :: ";
      }
      logger.logError(vsNestedCauseMsg + rte.getMessage() + "::Transporter not loaded!");
      setDetailedControllerStatus("Host connection failed...");
    }
    logger.logDebug("HostController.startup() - End");
  }

 /**
  * @see com.daifukuamerica.wrxj.controller.Controller#shutdown()
  */
  @Override
  public void shutdown()
  {
    for(ConnectionInfo vpConnInfo : mpCommInfo)
    {
      if (vpConnInfo.mpConnectionMonitor != null)
        vpConnInfo.mpConnectionMonitor.stopMonitor();

      vpConnInfo.mpTransporter.stopTransporter();
      sendStatusMessage(vpConnInfo.msCommGroup, StatusEventDataFormat.STATUS_OFFLINE);
      if (vpConnInfo.mpOutbTimerThread != null)
      {
        vpConnInfo.mpOutbTimerThread.cancel();
      }
    }

    try
    {
      DBObject vpDBObj = new DBObjectTL().getDBObject();
      vpDBObj.disconnect();

      cleanUp();
      logger.logDebug("Closing DB Connection...");
    }
    catch (DBException e)
    {
      logger.logException(e, "Error closing Database Connection");
    }

    logger.logDebug("HostController.shutdown() -- End");
    setDetailedControllerStatus("Host connection closed.");
    super.shutdown();
  }

  /**
   * @see com.daifukuamerica.wrxj.controller.Controller#initialize(java.lang.String)
   */
  @Override
  public void initialize(String uniqueControllerName)
  {
    super.initialize(uniqueControllerName);
    subscribeHostMesgSendEvent("%", false);
  }

  /**
   * @see com.daifukuamerica.wrxj.controller.Controller#processHeartbeatRequestEvent()
   */
  @Override
  protected void processHeartbeatRequestEvent()
  {
    super.processHeartbeatRequestEvent();
    for(ConnectionInfo vpConnInfo : mpCommInfo)
    {
      checkAndPublishConnectionStatus(vpConnInfo.msCommGroup, vpConnInfo.mpTransporter);
//      if (vpConnInfo.mnConnType == Transporter.CLIENT_TRANSPORT)
//      {
      checkOutboundTimerThread(vpConnInfo);
//      }
    }
  }

  /**
   * In particular this method directs host related events this object receives.
   *
   * @see com.daifukuamerica.wrxj.ipc.AbstractIPCMessenger#processIPCReceivedMessage()
   */
  @Override
  public void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();
    if (!receivedMessageProcessed)
    {
      receivedMessageProcessed = true;
      ConnectionInfo[] vapCommInfo = getClientHostRefs();

      for(ConnectionInfo vpConnInfo : vapCommInfo)
      {
        if (vpConnInfo.mpTransporter.isConnectionEstablished())
        {
          mzErrorLogged = false;
          checkForMessageSendEvent(vpConnInfo.msHostName, vpConnInfo.mpTransporter);
        }
        else if (!mzErrorLogged)
        {
          logger.logError("++++++++ Host Outbound connection not established!" +
                          "No messages will be sent! ++++++++");
          mzErrorLogged = true;
        }
      }
    }
  }

  protected void checkForMessageSendEvent(String isHostName, Transporter ipTransporter)
  {
                               // Event to send host a message.
    if (receivedEventType == MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE)
    {
      try
      {
        mpHostDelegate.setInfo(isHostName);
        if (mpHostServer.unprocessedMessageAvailable(mpHostDelegate))
        {
          int vnSendCount = ipTransporter.sendMessages(mpHostDelegate,
              mpHostServer);
          logger.logDebug("Sent " + vnSendCount + " messages to Host '"
              + isHostName + "'");
        }
      }
      catch(DBException | HostCommException ce)
      {
        logger.logException(ce, "processIPCReceivedMessage.checkForMessageSendEvent()");
      }
    }
  }

  /**
   * Send a status message so we can display the state of the connection on the
   * Equipment Monitor
   *
   * @param isCommGroup The comm group (TCPIPHost-1, TCPIPHost-2).
   * @param isStatus
   */
  protected void sendStatusMessage(String isCommGroup, String isStatus)
  {
    if (mzPublishStatus)
    {
      StatusEventDataFormat vpSEDF = new StatusEventDataFormat(getClass().getSimpleName());
      vpSEDF.setType(ControllerConsts.EQUIPMENT_STATUS);
      vpSEDF.addEquipmentStatus(isCommGroup, "Host", isCommGroup, isStatus,
                                isStatus, StatusEventDataFormat.NONE, "NOW");
      publishStatusEvent(vpSEDF.createStringToSend());

      String vsContDetailStatus;
      if (isStatus.contains(StatusEventDataFormat.STATUS_OFFLINE))
        vsContDetailStatus = StatusEventDataFormat.STATUS_OFFLINE;
      else
        vsContDetailStatus = StatusEventDataFormat.STATUS_ONLINE;

      setDetailedControllerStatus(vsContDetailStatus);
    }
  }

  /**
   * Check the connection for a given transporter and send status updates
   */
  protected void checkAndPublishConnectionStatus(String isCommGroup, Transporter ipTransporter)
  {
    if (!mzPublishStatus)
    {
      return;
    }

    if (ipTransporter.isHostReachable())
    {                                // Socket on the other end is reachable.
      sendStatusMessage(isCommGroup, StatusEventDataFormat.STATUS_ONLINE);
    }
    else
    {                                // Comm. Thread hasn't started yet.
      sendStatusMessage(isCommGroup, StatusEventDataFormat.STATUS_OFFLINE);
    }
  }

  /**
  * Method loads all Transporter objects.
  * @param isCommGroup
  * @throws DBRuntimeException if the Transporter object can't be created.
  * @throws com.daifukuamerica.wrxj.host.HostCommException
  */
  protected void createAndStartTransporter(String isCommGroup)
            throws DBRuntimeException, HostCommException, DBException
  {
    if (isUsingTCPIP(isCommGroup))
    {
       /*
        * If they want an IN and OUT TCP/IP port (usually for legacy systems
        * both ports are to be connected to the same host but it may be different
        * ones also).
        * The Comm Group in this case is assumed to have a 1 and 2 suffix.  If
        * it's a single port connection no suffix should be used.
        */
      if (isUsingDualPortTCPIP())
      {
        String[] vasTransportNames =  mpHostServer.getActiveTransporters();
        for(String vsCommGroup : vasTransportNames)
        {
          int vnConnType = (mpHostServer.isTcpServer(vsCommGroup)) ? Transporter.SERVER_TRANSPORT : Transporter.CLIENT_TRANSPORT;
          createTransporter(vnConnType, vsCommGroup);
        }
        startAllTransports();
      }
      else                             // Single port TCP/IP case.
      {
        createTransporter(Transporter.BIDIRECTIONAL_CONN, isCommGroup);
        startConnectionMonitor(mpCommInfo.get(0));
      }
    }
    else                               // JDBC
    {
      createTransporter(Transporter.BIDIRECTIONAL_CONN, isCommGroup);
      startConnectionMonitor(mpCommInfo.get(0));
    }
  }

  protected void createTransporter(int inConnType, String isCommGroup) throws DBRuntimeException
  {
    int vnCommPort = 0;
    Transporter vpTransporter = null;

    try
    {
      String vsTransporterClassPath = Application.getString(Application.HOSTCFG_DOMAIN + isCommGroup + ".class");
      String vsHostName = Application.getString(Application.HOSTCFG_DOMAIN + isCommGroup + ".HostName");

      Class<? extends Transporter> vpClassMetaData = Class.forName(vsTransporterClassPath).asSubclass(Transporter.class);
      Constructor<? extends Transporter> mpConstructor = vpClassMetaData.getConstructor(Controller.class, String.class, String.class);
      vpTransporter = mpConstructor.newInstance(this, getName(), isCommGroup);
      vpTransporter.setLogger(logger);

      if (isUsingTCPIP(isCommGroup))
      {
        vnCommPort = Application.getInt(Application.HOSTCFG_DOMAIN + isCommGroup + "." + TCPIPConstants.LISTEN_PORT);
        vpTransporter.setCommPort(vnCommPort);
      }

      /*
       * Record the connection info. for later use.
       */
      ConnectionInfo vpConnInfo = new ConnectionInfo();
      vpConnInfo.mnConnType = inConnType;
      vpConnInfo.msCommGroup = isCommGroup;
      vpConnInfo.msHostName = vsHostName;
      vpConnInfo.mpTransporter = vpTransporter;
      vpConnInfo.mpConnectionMonitor = null;

      mpCommInfo.add(vpConnInfo);
    }
    catch(InvocationTargetException e)
    {
      setDetailedControllerStatus("Host connection failed...");
      throw new DBRuntimeException("Error invoking constructor for transporter...", e);
    }
    catch(NoSuchMethodException e)
    {
      setDetailedControllerStatus("Host connection failed...");
      throw new DBRuntimeException("Specified Constructor for Transporter " +
                                   "does not exist!", e);
    }
    catch(ClassNotFoundException e)
    {
      setDetailedControllerStatus("Host connection failed...");
      throw new DBRuntimeException("Failed to initialise Transporter class in " +
                                   "HostController!", e);
    }
    catch(InstantiationException e)
    {
      setDetailedControllerStatus("Host connection failed...");
      throw new DBRuntimeException("Failed to build Transporter in " +
                                   "HostController!", e);
    }
    catch(IllegalAccessException e)
    {
      setDetailedControllerStatus("Host connection failed...");
      throw new DBRuntimeException("Failed to build Transporter in " +
                                   "HostController!", e);
    }
  }

  /**
   * Method to start the connection monitor.  This monitor will start/restart the
   * Transporter as needed.  The connection monitor is normally used for a tcp/ip
   * client or jdbc connections.  It is not necessary for server sockets.
   * @param ipConnInfo
   * @throws com.daifukuamerica.wrxj.host.HostCommException
   */
  protected void startConnectionMonitor(ConnectionInfo ipConnInfo) throws HostCommException
  {
    int vnRetryInterval = Application.getInt(Application.HOSTCFG_DOMAIN + ipConnInfo.msCommGroup + ".ConnRetryInterval", 10);
    Transporter vpTranporter = ipConnInfo.mpTransporter;
    if (isUsingTCPIP(ipConnInfo.msCommGroup))
    {
      int keepAliveTimeout = Application.getInt(Application.HOSTCFG_DOMAIN + ipConnInfo.msCommGroup + ".KeepAliveTimeout", 60);
      ipConnInfo.mpConnectionMonitor = Factory.create(TCPIPConnectionMonitor.class, vpTranporter, vnRetryInterval, keepAliveTimeout);
    }
    else
    {
      ipConnInfo.mpConnectionMonitor = Factory.create(JDBCConnectionMonitor.class, vpTranporter, vnRetryInterval);
    }
  }

  /**
   * Method to start both sockets regardless of if they are Server or Client transports.
   * @throws HostCommException
   */
  protected void startAllTransports() throws HostCommException
  {
    for(ConnectionInfo vpConnInfo : mpCommInfo)
    {
      vpConnInfo.mpTransporter.startTransporter();
      startConnectionMonitor(vpConnInfo);
    }
  }

  protected void checkOutboundTimerThread(ConnectionInfo ipConnInfo)
  {
    if (ipConnInfo.mpOutbTimerThread == null || !ipConnInfo.mpOutbTimerThread.isAlive())
    {
      int ackTimeout = Application.getInt(Application.HOSTCFG_DOMAIN + ipConnInfo.msCommGroup + ".AckTimeout", 60);
      int ackMaxRetry = Application.getInt(Application.HOSTCFG_DOMAIN + ipConnInfo.msCommGroup + ".AckMaxRetry", 3);
      OutboundTimerThread vpThread = Factory.create(OutboundTimerThread.class,
       ipConnInfo.mpTransporter, ipConnInfo.msHostName, mnHostOutCheckInterval, ackTimeout, ackMaxRetry);
      vpThread.start();
      ipConnInfo.mpOutbTimerThread = vpThread;
    }
 
  }

  /**
   * Method gets a reference to a transporter using a Client socket.
   * For single port sockets or JDBC connections, there is no distinction between
   * inbound and outbound connections.
   *
   * @return An Array containing references to one or more Host Connection info
   *         objects (multiple if we are talking to multiple hosts).
   * @see Transporter
   */
  protected ConnectionInfo[] getClientHostRefs()
  {
    List<ConnectionInfo> vapConnList = new ArrayList<>();

    for(ConnectionInfo vpConnInfo : mpCommInfo)
    {
      if (vpConnInfo.mnConnType != Transporter.SERVER_TRANSPORT)
      {
        vapConnList.add(vpConnInfo);
      }
    }

    return(vapConnList.toArray(new ConnectionInfo[0]));
  }

 /**
  *  Sets Objects for garbage collection.
  */
  protected void cleanUp()
  {
    mpHostServer.cleanUp();
    mpHostServer = null;
  }

  /**
   * Checks if the <code>UseDualPortTcpComms</code> property is set in HostConfig.
   *
   * @return <code>false</code> if the property is not defined or is set to false.
   */
  protected boolean isUsingDualPortTCPIP()
  {
    return(getConfigPropertyAsBoolean("UseDualPortTcpComms"));
  }

  protected boolean isUsingTCPIP(String isCommGroup)
  {
    return(isCommGroup.toUpperCase().startsWith("TCPIP"));
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
    Controller thisController = Factory.create(HostController.class);
    thisController.setEquipmentPortCKN(controllerConfigs.getString(DEVICE_PORT));
    thisController.setCollaboratorCKN(controllerConfigs.getString(COLLABORATOR));
    return thisController;
  }

  public class ConnectionInfo
  {
    public int         mnConnType;
    public String      msCommGroup;
    public String      msHostName;
    public Transporter mpTransporter;
    public OutboundTimerThread mpOutbTimerThread;
    public AbstractConnectionMonitor mpConnectionMonitor;
  }
}

