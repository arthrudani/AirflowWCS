package com.daifukuamerica.wrxj.device.agv;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleMoveData;
import com.daifukuamerica.wrxj.dbadapter.data.VehicleSystemCmdData;
import com.daifukuamerica.wrxj.device.agv.communication.TCPEventListener;
import com.daifukuamerica.wrxj.device.agv.communication.TCPIPClientComms;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageConstants;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageHelper;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFactoryException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFormatterException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFormatterFactory;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParseException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageParserFactory;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.AbstractMessageParser;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.ACKFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.ERRFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.HBTFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.LSSFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.formatters.NAKFormatter;
import com.daifukuamerica.wrxj.device.agv.messages.parsers.ACKParserImpl;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for handling AGV messages
 * @author A.D.
 * @since  25-May-2009
 */
public class AGVController extends Controller
{
  private AGVMessageFormatterFactory mpFormatFactory;
  private AGVMessageParserFactory mpParserFactory;

  private String[]          masStnDevCollaborators;
  private CMSStatusNotifier mpCMSStatusNotifier = null;
  private CMSConnectionBuilder mpConnectionBuilder;
  private AGVLogger         mpAGVLogger;
  private LinkedHashMap<String, TCPIPClientComms> mpTCPCommMap;
  private volatile TCPIPClientComms  mpTCPComm;
  private AGVDBInterface    mpDBInterface;
  private ConnectionMonitor mpConnectionMonitor;
  private Timer mpOldMessageMonitor;
  private volatile boolean mzStationStatusSynced = false;
  private volatile boolean mzLinkSynchronized = false;
  private int      HEARTBEAT_INTERVAL = 30000;
  private long     TIMER_START_DELAY = 10000L;
  private long     TIMER_PERIOD = 20000L;
  private long     MESSAGE_RESEND_TIME = 30000L;
  private int      TCPIP_PORT = 3100;
  private int      CONNECT_TIMEOUT = 30000;
  private final String ENVIRONMENT_FILE = Application.getString("connect_file_name");

  public AGVController()
  {
    super();
  }

  @Override
  public void startup()
  {
    super.startup();

    logger.logDebug("AGVController.startup() - Start");
    try
    {
      mpFormatFactory = AGVMessageFormatterFactory.getInstance();
      mpParserFactory = AGVMessageParserFactory.getInstance();

/*============================================================================
 * Create portable DB interface for use in *this* thread only.
 *============================================================================*/
      mpDBInterface = Factory.create(AGVDBUpdateWrxjImpl.class);

/*============================================================================
 * Create portable logger interface. This is necessary to isolate portable
 * package structure from non-standard loggers (like ours!).
 *============================================================================*/
      mpAGVLogger = Factory.create(AGVLoggerWrxjImpl.class, getName());
      logger.addEquipmentLogger();
      initDeviceCollaborators();

/*============================================================================
 *   Create TCP/IP interface reference and register for data receive events.
 *============================================================================*/
      mpTCPCommMap = new LinkedHashMap<String, TCPIPClientComms>();
      String[] vasHostNames = System.getProperty("HostName").split(",");
      for(int vnIdx = 0; vnIdx < vasHostNames.length; vnIdx++)
      {
        String vsPort = System.getProperty("Port");
        if (vsPort != null)
          TCPIP_PORT = Integer.parseInt(vsPort);

        String vsConnectTimeout = System.getProperty("ConnectTimeout");
        if (vsConnectTimeout != null)
          CONNECT_TIMEOUT = Integer.parseInt(vsConnectTimeout);

        TCPIPClientComms vpTCPComm = Factory.create(TCPIPClientComms.class,
                                                    vasHostNames[vnIdx],
                                                    TCPIP_PORT, CONNECT_TIMEOUT,
                                                    mpAGVLogger);
        vpTCPComm.registerTCPEventListener(new TCPEventListener()
        {
          @Override
          public void receivedData(String isData)
          {
            processReceivedData(isData);
          }
        });

        if (vnIdx == 0)
          mpTCPCommMap.put(CMSConnectionBuilder.PRIMARY_CONN, vpTCPComm);
        else
          mpTCPCommMap.put(CMSConnectionBuilder.SECONDARY_CONN, vpTCPComm);
      }

      mpConnectionBuilder = new CMSConnectionBuilder(mpTCPCommMap);

      if (HEARTBEAT_INTERVAL < 0)
      {
        logger.logDebug("Warning: HeartBeat messages are " +
                        "turned off for SAM communications.");
      }
      mpConnectionBuilder.createConnection(mpAGVLogger);
      mpTCPComm = mpTCPCommMap.get(CMSConnectionBuilder.PRIMARY_CONN);
      mpTCPComm.startReader();
      setDetailedControllerStatus("Connected to SAM " + mpTCPComm.getHostName());
//      else
//      {
//                                       // Start up the connection monitor.
      if (mpTCPComm.isConnectionEstablished())
      {
        mpConnectionMonitor = new ConnectionMonitor("AGVController.ConnectionMonitor");
      }
//      }

                                       // Start up old message checker.
      mpOldMessageMonitor = new Timer("OldMessageTask");
      mpOldMessageMonitor.schedule(new OldMessageCheckTask(), TIMER_START_DELAY,
                                   TIMER_PERIOD);

                                       // Mark this controller as running.
      super.setControllerStatus(ControllerConsts.STATUS_RUNNING);
    }
    catch(DBRuntimeException rte)
    {
      logger.logError(rte.getCause().getMessage());
    }
    catch(Throwable exc)
    {
      shutdown();
      logger.logException("Controller Shutting down!  Check configuration in " +
                 "WrxSequencer, or the AGVMessage tables tp start with!!", exc);
    }
    logger.logDebug("AGVController.startup() - End");
  }

 /**
  *  {@inheritDoc}
  * @param uniqueControllerName
  */
  @Override
  public void initialize(String uniqueControllerName)
  {
    super.initialize(uniqueControllerName);
                                       // Subscribe for MOV messages received
                                       // from Host.
    subscribeHostMesgReceiveEvent(getName(), false);
    try
    {
      AGVMessageHelper.loadEnvironment(ENVIRONMENT_FILE);
      String vsReconnectChk = System.getProperty("ReconnectionCheck");
      if (vsReconnectChk != null)
        HEARTBEAT_INTERVAL = Integer.parseInt(vsReconnectChk)*1000;

      String vsResendInterval = System.getProperty("MessageResendTime");
      if (vsResendInterval != null)
        MESSAGE_RESEND_TIME = Integer.parseInt(vsResendInterval)*1000;
    }
    catch(FileNotFoundException ex)
    {
      logger.logError("File " + ENVIRONMENT_FILE + " was not " +
                           "found. AGVController environment not initialized!");
    }
    catch(IOException ex)
    {
      logger.logError("Properties file load error. " +
                                  "AGVController environment not initialized!");
    }
  }

 /**
  *  {@inheritDoc}
  */
  @Override
  public void shutdown()
  {
    logger.logDebug("AGVController.shutdown() -- Start");
    if (mpConnectionMonitor != null)
    {
      mpConnectionMonitor.stop();
    }

    if (mpTCPComm != null)
    {
      mpTCPComm.closeConnection();
      setDetailedControllerStatus("SAM connection closed.");
    }
    mpOldMessageMonitor.cancel();

    logger.logDebug("AGVController.shutdown() -- End");
    super.shutdown();
  }

  /**
   * Method handles JMS events received by this controller.  Events will be
   * received from GUI screens as well as from the HostController.  The JMS
   * event is a <u>request</u> from the sender to look at a certain piece of
   * data that needs to be sent to SAM.
   *
   * The content of the message is expected to be a valid string understood
   * by the {@link com.daifukuamerica.wrxj.device.agv.messages.AGVMessageNameEnum AGVMessageNameEnum}
   * class.
   */
  @Override
  public void processIPCReceivedMessage()
  {
    super.processIPCReceivedMessage();

    if (!receivedMessageProcessed)
    {
      if (receivedEventType == MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE)
      {
        /*
         * receivedText = Message ID
         * receivedData = request sequence number
         */
        if (mzLinkSynchronized && receivedText.trim().length() > 0 &&
            receivedData > 0)
        {
          AGVMessageNameEnum vpMesgName = AGVMessageNameEnum.getEnumObject(receivedText);
          if (vpMesgName != null)
          {
            try
            {
              AbstractMessageFormatter vpFormatter = mpFormatFactory.getFormatter(vpMesgName);
              String vsSendStr = vpFormatter.format(receivedData, mpDBInterface);
              if (!vsSendStr.isEmpty())
              {
                mpTCPComm.sendMessage(vsSendStr);

                                       // Log formatted data in equipment log.
                String vsFormattedLogStr = vpFormatter.getFormattedLogFields().toString();
                if (!vsFormattedLogStr.isEmpty())
                  logger.logTxEquipmentMessage(vsSendStr, vsFormattedLogStr);
              }
              vpFormatter.postSendProcessing(mpDBInterface);
            }
            catch(AGVException exc)
            {
              logger.logException("Unable to process " + receivedText +
                                  " message!", exc);
            }
            catch(AGVMessageFactoryException exc)
            {
              logger.logError("Unable to create message formatter for " +
                              "message " + receivedText + ". " + exc.getMessage());
            }
            catch(AGVMessageFormatterException exc)
            {
              logger.logError("Formatting error! " + exc.getMessage());
            }
            catch(TCPIPCommException exc)
            {
              logger.logError("Unable to send message to SAM! " + exc.getMessage());
            }
          }
        }
        else if (!mzLinkSynchronized)
        {
          logger.logError("Link has not been synchronized.  AGV " +
                          "Message(s) not sent!");
        }
        else if (receivedText.trim().length() == 0)
        {
          logger.logError("No message id. received in JMS event to " +
                          "AGVController from " + receivedCKN +
                          ".  No message sent!");
        }
        else if (receivedData <= 0)
        {
          logger.logError("Invalid message sequence received in JMS event to " +
                          "AGVController from " + receivedCKN);
        }
      }
      receivedMessageProcessed = true;

//TODO: add message to AGCStationDevice (for when AGV system is taken offline).
//      add message to Host (MES) system when AGV delivers load to station.
    }
  }

 /**
  * Callback method to process received data from SAM. <b>Warning:</b> this
  * method is called by another thread. <i>Don't call this from multiple threads
  * unless you synchronize it.</i>
  *
  * @param isReceivedData the received data from SAM
  */
  public void processReceivedData(String isReceivedData)
  {
    AbstractMessageParser vpParser = null;
    AGVDBInterface vpDBInterface = Factory.create(AGVDBUpdateWrxjImpl.class);

    try
    {
                                       // Check received data message type and
                                       // build appropriate parser.
      vpParser = mpParserFactory.getParser(isReceivedData);
      vpParser.setAGCNotifier(mpCMSStatusNotifier);
      vpParser.setMessageLogger(mpAGVLogger);

                                       // Parse header to set sequence and
                                       // message length for validation.
      String vsHeaderStrippedMesg = vpParser.parseHeader(isReceivedData);
      if (vpParser.isValidMessageLength(vsHeaderStrippedMesg))
      {
        if (!vpParser.isAckNakMessage())
        {
          vpParser.parseMessage(vsHeaderStrippedMesg);
/*============================================================================
 * We've got the right message checksum and parsed message correctly so send
 * ACK.  If there are errors during message processing we send an ERR.
 *============================================================================*/
          sendACK(vpParser, isReceivedData, vpDBInterface);
          vpParser.processMessage(vpDBInterface);
                                       // Log parsed data in equipment log.
          String vsPrettyLogString = vpParser.getFormattedLogFields().toString();
          if (!vsPrettyLogString.isEmpty())
            logger.logRxEquipmentMessage(isReceivedData, vsPrettyLogString);

          if (vpParser.isLSSMessage())
          {                            // Tell them our starting sequence.
            sendLSS(vpDBInterface);
            mzLinkSynchronized = true;
          }
        }
        else                           // This is an ACK or NAK message to a
        {                              // Wrx to SAM message. Mark DB records
                                       // accordingly.
          vpParser.processMessage(vpDBInterface);

          if (!mzStationStatusSynced && mzLinkSynchronized)
          {                            // The first message they will ACK for
                                       // us is the LSS message. So we should only
                                       // come in here after the first LSS ACK.
            if (vpParser instanceof ACKParserImpl)
            {
              syncAGVStationStatus();
              mzStationStatusSynced = true;
            }
          }
        }
      }
      else
      {
        sendNAK(vpParser.getMessageSequence(),
                AGVMessageConstants.INVALID_MESSAGE_CODE, isReceivedData);
        logger.logError("NAK message sent due to invalid Message length " +
                        "received from SAM. Message: " + isReceivedData);
      }
    }
    catch(AGVMessageFactoryException ex)
    {
      if (vpParser != null)
      {
        logger.logException("Parser Factory error while trying to find " +
                            "parser for message: " + isReceivedData, ex);
        sendNAK(vpParser.getMessageSequence(),
                      AGVMessageConstants.INVALID_MESSAGE_CODE, isReceivedData);
      }
      else
      {
        logger.logError("Parser not found for message \'" + isReceivedData +
                        "\'! Sending NAK.");
        String[] vasHeader = AGVMessageHelper.peekMessageHeaderInfo(isReceivedData);
        sendNAK(Integer.parseInt(vasHeader[1]),
                      AGVMessageConstants.INVALID_MESSAGE_CODE, isReceivedData);
      }
    }
    catch(AGVMessageParseException ex)
    {
      sendNAK(vpParser.getMessageSequence(),
              AGVMessageConstants.INVALID_MESSAGE_CODE, isReceivedData);
      logger.logError("Unable to parse message: " + isReceivedData + ".  " +
                      ex.getMessage());
    }
    catch(AGVException ex)
    {
      sendERR(isReceivedData, vpDBInterface);
      logger.logException("Sending ERR message to SAM due to DB " +
                          "processing error!", ex);
    }
    finally
    {
      vpDBInterface.closeDatabaseConnection();
    }
  }

  private void sendLSS(AGVDBInterface ipDBInterface)
  {
    try
    {
      LSSFormatter vpLSSFormatter = mpFormatFactory.getFormatter(
                                       AGVMessageNameEnum.LSS_REQUEST_RESPONSE);
      vpLSSFormatter.setDateTime(new Date());
                                       // Format the message and send it.
      String vsFormattedMessage = vpLSSFormatter.format(0, ipDBInterface);
      mpTCPComm.sendMessage(vsFormattedMessage);
                                       // Log formatted data in equipment log.
      StringBuffer vpLogBuf = vpLSSFormatter.getFormattedLogFields();
      if (vpLogBuf.length() > 0)
        logger.logTxEquipmentMessage(vsFormattedMessage, vpLogBuf.toString());
    }
    catch(AGVException exc)
    {
      logger.logException("Unable to process LSS message! " +
                          "Sequence number error...", exc);
    }
    catch(AGVMessageFactoryException exc)
    {
      logger.logError("Unable to create ACK message formatter! " +
                      exc.getMessage());
    }
    catch(AGVMessageFormatterException exc)
    {
      logger.logError("Unable to format ACK message! " + exc.getMessage());
    }
    catch(TCPIPCommException exc)
    {
      logger.logError("Unable to send ACK! " + exc.getMessage());
    }
  }

 /**
  * Send the ACK to a message we just received.
  * @param ipParser the parser associated with the received message.
  * @param isOriginalMessage the full original message.  This will be used in a
  *        NAK response here in case they sent a skipped sequence number.
  * @param ipDBInterface the database interface.
  */
  private void sendACK(AbstractMessageParser ipParser, String isOriginalMessage,
                       AGVDBInterface ipDBInterface)
  {
    try
    {
      if (!ipParser.isLSSMessage())
      {
        ipDBInterface.checkSkippedInboundSequence(ipParser.getMessageSequence());
      }
      ACKFormatter vpACKFormatter = mpFormatFactory.getFormatter(
                                       AGVMessageNameEnum.ACK_REQUEST_RESPONSE);
      mpTCPComm.sendMessage(vpACKFormatter.format(ipParser.getMessageSequence(),
                                                  ipDBInterface));
    }
    catch(AGVMessageFactoryException exc)
    {
      logger.logError("Unable to create ACK message formatter! " +
                      exc.getMessage());
    }
    catch(AGVMessageFormatterException exc)
    {
      logger.logError("Unable to format ACK message! " + exc.getMessage());
    }
    catch(AGVException exc)
    {
      if (exc.getErrorCode() == AGVDBInterface.RECEIVED_SKIPPED_SEQ)
      {
        logger.logError(exc.getMessage() + " Message: " +
                        ipParser.getMessageIdentifier());
        sendNAK(ipParser.getMessageSequence(),
                AGVMessageConstants.SKIPPED_MESSAGE_CODE, isOriginalMessage);
      }
      else
      {
        logger.logException(exc);
      }
    }
    catch(TCPIPCommException exc)
    {
      logger.logError("Unable to send ACK! " + exc.getMessage());
    }
    catch(Exception e)
    {
      logger.logException("Unable to send ACK! ", e);
    }
  }

  private void sendNAK(int inLastSequenceNum, int inErrorCode,
                              String isErrorText)
  {
    try
    {
      NAKFormatter vpNAKFormatter = mpFormatFactory.getFormatter(
                                       AGVMessageNameEnum.NAK_REQUEST_RESPONSE);
      vpNAKFormatter.setErrorCode(inErrorCode);
      vpNAKFormatter.setMessageText(isErrorText);

      String vsFormattedMessage = vpNAKFormatter.format(inLastSequenceNum);
      mpTCPComm.sendMessage(vsFormattedMessage);

                                       // Log formatted data in equipment log.
      StringBuffer vpLogBuf = vpNAKFormatter.getFormattedLogFields();
      logger.logTxEquipmentMessage(vsFormattedMessage, vpLogBuf.toString());
    }
    catch(AGVMessageFactoryException exc)
    {
      logger.logError("Unable to create NAK message formatter! " +
                      exc.getMessage());
    }
    catch(AGVMessageFormatterException fe)
    {
      logger.logError("Unable to format NAK message! " + fe.getMessage());
    }
    catch(TCPIPCommException exc)
    {
      logger.logError("Unable to send NAK! " + exc.getMessage());
    }
  }

  private void sendERR(String isMessage, AGVDBInterface ipDBInterface)
  {
    try
    {
      ERRFormatter vpERRFormatter = mpFormatFactory.getFormatter(
                                       AGVMessageNameEnum.ERR_REQUEST_RESPONSE);
      vpERRFormatter.setReasonCode(ERRFormatter.DB_PROCESSING_ERROR);
      vpERRFormatter.setReasonText(isMessage);

      int vnSeq = ipDBInterface.getFormatSequenceNumber();
      String vsFormattedMessage = vpERRFormatter.format(vnSeq, ipDBInterface);
      mpTCPComm.sendMessage(vsFormattedMessage);

                                       // Log formatted data in equipment log.
      StringBuffer vpLogBuf = vpERRFormatter.getFormattedLogFields();
      logger.logTxEquipmentMessage(vsFormattedMessage, vpLogBuf.toString());

    }
    catch(AGVException ae)
    {
      logger.logException("Unable to process ERR message! " +
                          "Sequence number error...", ae);
    }
    catch(AGVMessageFactoryException exc)
    {
      logger.logError("Unable to create ERR message formatter! " +
                      exc.getMessage());
    }
    catch(AGVMessageFormatterException fe)
    {
      logger.logError("Unable to format ERR message! " + fe.getMessage());
    }
    catch(TCPIPCommException exc)
    {
      logger.logError("Unable to send ERR! " + exc.getMessage());
    }
  }

  private void syncAGVStationStatus()
  {
    VehicleSystemCmdData vpVSData = Factory.create(VehicleSystemCmdData.class);
    StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);

    try
    {
      vpVSData.setSystemMessageID(AGVMessageNameEnum.XMT_REQUEST.getValue());
      vpVSData.setCommandValue(AGVMessageNameEnum.SSR_RESPONSE.getValue());
      vpDevServ.addAGVCommandRecord(vpVSData);
                                       // Send message to self.
      publishHostMesgReceiveEvent(vpVSData.getSystemMessageID(),
                                  vpVSData.getSequenceNumber(), getName());
    }
    catch(DBException ex)
    {
      logger.logException(ex);
    }
  }

 /**
  * Method to resend any sent messages that have not been responded to.  Any
  * move message that has not been acknowledged from the other side for more
  * than 30 seconds will be resent.
  *
  * @param ipDBInterface the database interface.
  * @param ipDevServ reference to the device server.
  * @throws DBException for database access errors.
  * @throws AGVMessageFactoryException if no formatter could be created.
  * @throws AGVMessageFormatterException if there is a formatting error.
  * @throws TCPIPCommException if there is a communication error.
  * @throws AGVException
  */
  private void resendAGVMoves(AGVDBInterface ipDBInterface,
           StandardDeviceServer ipDevServ) throws DBException,
                                                  AGVMessageFactoryException,
                                                  AGVMessageFormatterException,
                                                  TCPIPCommException,
                                                  AGVException
  {
    List<Map> vpMoves = ipDevServ.getAllAGVMoveRecordsByStatus(
                              DBConstants.AGV_NOMOVE, DBConstants.AGV_MOVESENT,
                              DBConstants.AGV_MOVECANCELSENT, DBConstants.AGV_RECOVERABLE);
    if (!vpMoves.isEmpty())
    {
      for(Map vpMoveMap : vpMoves)
      {
        Date vpMesgDate = DBHelper.getDateField(vpMoveMap, VehicleMoveData.STATUSCHANGETIME_NAME);
        long vlOriginalTime = vpMesgDate.getTime();
        long vlCurrTime = System.currentTimeMillis();

        if (vlCurrTime - vlOriginalTime >= MESSAGE_RESEND_TIME)
        {
//          String vsLoadID = DBHelper.getStringField(vpMoveMap,
//                                                    VehicleMoveData.LOADID_NAME);
          int vnSeqNum = DBHelper.getIntegerField(vpMoveMap, VehicleMoveData.SEQUENCENUMBER_NAME);
//          int vnSeqNum = ipDevServ.updateAGVMoveSequence(vsLoadID);
                                       // Send message to self.
          publishHostMesgReceiveEvent(AGVMessageNameEnum.MOV_REQUEST.getValue(),
                                      vnSeqNum, getName());
        }
      }
    }
  }

 /**
  * Method to resend any sent messages that have not been responded to.  Any
  * system command that has not been acknowledged from the other side for more
  * than 30 seconds will be resent.
  *
  * @param ipDBInterface the database interface.
  * @param ipDevServ reference to the device server.
  * @throws DBException for database access errors.
  * @throws AGVMessageFactoryException if no formatter could be created.
  * @throws AGVMessageFormatterException if there is a formatting error.
  * @throws TCPIPCommException if there is a communication error.
  * @throws AGVException
  */
  public void resendAGVSystemCmd(AGVDBInterface ipDBInterface,
           StandardDeviceServer ipDevServ) throws DBException,
                                                  AGVMessageFactoryException,
                                                  AGVMessageFormatterException,
                                                  TCPIPCommException,
                                                  AGVException
  {
    List<Map> vpMoves = ipDevServ.getAllAGVMoveRecordsByStatus(
                                               DBConstants.AGV_SYSCMD_REQUEST,
                                               DBConstants.AGV_SYSCMD_SENT);
    if (!vpMoves.isEmpty())
    {
      for(Map vpCmdMap : vpMoves)
      {
        Date vpMesgDate = DBHelper.getDateField(vpCmdMap,
                                    VehicleSystemCmdData.STATUSCHANGETIME_NAME);
        long vlOriginalTime = vpMesgDate.getTime();
        long vlCurrTime = System.currentTimeMillis();

        if (vlCurrTime - vlOriginalTime >= MESSAGE_RESEND_TIME)
        {
          String vsMessageID = DBHelper.getStringField(vpCmdMap,
                                     VehicleSystemCmdData.SYSTEMMESSAGEID_NAME);

          int vnOldSeq = DBHelper.getIntegerField(vpCmdMap,
                                          VehicleMoveData.SEQUENCENUMBER_NAME);
          int vnNewSeq = ipDevServ.updateAGVCmdSequence(vnOldSeq);
          publishHostMesgReceiveEvent(vsMessageID, vnNewSeq, getName());
        }
      }
    }
  }

  private void initDeviceCollaborators()
  {
    StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
    try
    {
      List<Map> vpList = vpDevServ.getCtlrDevices();
      if (vpList.isEmpty())
      {
        masStnDevCollaborators = new String[0];
      }
      else
      {
        masStnDevCollaborators = new String[vpList.size()];
        int vnIdx = 0;
        for(Map vpDevMap : vpList)
        {
          masStnDevCollaborators[vnIdx++] = DBHelper.getStringField(vpDevMap,
                                                     DeviceData.DEVICEID_NAME);
        }
      }
    }
    catch(DBException ex)
    {
      masStnDevCollaborators = new String[0];
    }
    mpCMSStatusNotifier = new WrxCMSStatusNotifier(masStnDevCollaborators);
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
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig)
         throws ControllerCreationException
  {
    Controller vpController = Factory.create(AGVController.class);
    vpController.setCollaboratorCKN(ipConfig.getString(COLLABORATOR));

    return(vpController);
  }

 /**
  * Class to resend any message stuck as SENT since we never received an
  * ACK/NAK for them.
  */
  private class OldMessageCheckTask extends TimerTask
  {
    @Override
    public void run()
    {
      if (mzLinkSynchronized)
      {
        logger.logDebug("Checking for unsent MOV messages.");
        StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);
        AGVDBInterface vpAGVDBInterface = Factory.create(AGVDBUpdateWrxjImpl.class);

        try
        {
          resendAGVMoves(vpAGVDBInterface, vpDevServ);
          resendAGVSystemCmd(vpAGVDBInterface, vpDevServ);
        }
        catch(AGVException ae)
        {
          logger.logException("Unable to process message! " +
                              "Sequence number error...", ae);
        }
        catch(AGVMessageFactoryException exc)
        {
          logger.logException("Unable to create message formatter! ", exc);
        }
        catch(AGVMessageFormatterException fe)
        {
          logger.logError("Unable to format MOV message! " + fe.getMessage());
        }
        catch(TCPIPCommException exc)
        {
          logger.logError("Unable to resend message! " + exc.getMessage());
        }
        catch(DBException exc)
        {
          logger.logError("Error finding Vehicle Move records! " + exc.getMessage());
        }
        catch(Exception e)
        {
          logger.logError("Error finding Vehicle Move records! " + e.getMessage());
        }
        finally
        {
          vpDevServ.cleanUp();
        }
      }
      else
      {
        logger.logError("Link has not been synchronized.  Messages not checked " +
                        "to be sent.");
      }
    }
  }

  /**
   * Class maintains a connection to a TCP/IP server.
   */
  private class ConnectionMonitor implements Runnable
  {
    private HBTFormatter  impHBTFormatter;
    private volatile boolean imzStopThread = false;
    private Thread        impThread;

    public ConnectionMonitor(String vsThreadName)
           throws AGVMessageFactoryException
    {
      impHBTFormatter = mpFormatFactory.getFormatter(AGVMessageNameEnum.HBT_REQUEST);
      impThread = new Thread(this, vsThreadName);
      impThread.start();
    }

    @Override
    public void run()
    {
      AGVDBInterface impAGVDBInterface = Factory.create(AGVDBUpdateWrxjImpl.class);
      SKDCUtility.sleep(10000L);

      do
      {
        try
        {
          if (mpTCPComm != null && mpTCPComm.isConnectionEstablished() &&
              mpTCPComm.isSocketValid())
          {
            mpTCPComm.sendMessage(impHBTFormatter.format(0, impAGVDBInterface));
          }
          else
          {
            setDetailedControllerStatus("SAM connection closed.");
            logger.logDebug("AGV Connection Monitor attempting new socket connection.");
            mpConnectionBuilder.createConnection(mpAGVLogger);
            mpTCPComm = mpTCPCommMap.get(CMSConnectionBuilder.PRIMARY_CONN);
            mpTCPComm.startReader();
            SKDCUtility.sleep(2000L);
            setDetailedControllerStatus("Connected to SAM " + mpTCPComm.getHostName());
          }
        }
        catch(AGVException exc)
        {
          logger.logError("Error getting Sequence number for HeartBeat " +
                          "Message.  Stopping ConnectionMonitor Thread! " +
                          exc.getMessage());
          mzLinkSynchronized = false;  // Link will need to be synchronized again.
          mzStationStatusSynced = false;
        }
        catch(AGVMessageFormatterException exc)
        {
          logger.logError("Heartbeat message creation error. " + exc.getMessage());
          imzStopThread = true;
          mzLinkSynchronized = false;  // Link will need to be synchronized again.
          mzStationStatusSynced = false;
          setDetailedControllerStatus("SAM connection closed.");
        }
        catch(TCPIPCommException te)
        {
          logger.logException("AGVController Connection Monitor received " +
                              "Socket Error! Closing Socket.", te);
          if (mpTCPComm != null)
            mpTCPComm.closeConnection();
          setDetailedControllerStatus("SAM connection failed.");

          mzLinkSynchronized = false;  // Link will need to be synchronized again.
          mzStationStatusSynced = false;
        }
        catch(Exception exc)
        {
          mzLinkSynchronized = false;  // Link will need to be synchronized again.
          mzStationStatusSynced = false;
          logger.logException("AGVController Connection Monitor received " +
                              "general exception! Closing Socket.", exc);
          if (mpTCPComm != null)
            mpTCPComm.closeConnection();
          setDetailedControllerStatus("SAM connection stopped.");
        }
        SKDCUtility.sleep(HEARTBEAT_INTERVAL);
      } while(!imzStopThread);
    }

    public void stop()
    {
      imzStopThread = true;
      impThread.interrupt();
      setDetailedControllerStatus("Connection to SAM closed.");
    }
  }
}
