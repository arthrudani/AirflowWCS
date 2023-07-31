package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import java.util.Properties;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuoc.wrxj.custom.ebs.communication.ConnConfigKeys;

/**
 * This takes care of running socket communication for host/SAC interface
 * 
 * @author LK
 *
 */
public class SACPort extends Controller {
    public static final String COMM_TYPE = "CommType";
    private String controllerKeyName = "";
    private Thread handlerThread = null;
    private SACClientSocketHandler handler = null;
    private int prevControllerStatus = ControllerConsts.STATUS_UNKNOWN;

    public SACPort(ReadOnlyProperties config) {
        super();
        super.setProperties(config);
    }

    public static Controller create(ReadOnlyProperties config) throws ControllerCreationException {
        return new SACPort(config);
    }

    @Override
    public void initialize(String controllerKeyName) {
        super.initialize(controllerKeyName);

        logger.logDebug(getClass().getSimpleName() + ".initialize() - Start");

        this.controllerKeyName = controllerKeyName;
        
        logger.addCommLogger();

        // Socket handler publishes COM event whenever connection is made and terminated
        subscribeCommEvent();

        // Host message handlers like HostExpectedReceiptMessageHandler publish response message on this event
        subscribeHostMesgSendEvent("%", false);

        logger.logDebug(getClass().getSimpleName() + ".initialize() - End");
    }

    @Override
    public void startup() {
        // Not calling AbstractIPCMessenger.startup() as it starts an unnecessary timer thread by default.
        logger.logDebug(getClass().getSimpleName() + ".startup() - Start");
        
        String startMessage = "********** " + getName() + ": starting controller. **********";
        logger.logTxByteCommunication(startMessage.getBytes(), 0, startMessage.length());

        try {
			handler = new SACClientSocketHandler(loadConnectionConfigurations(), logger, getMessageService());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.logError("Failed to startup the SACPort" + e.getMessage());
		}
        handlerThread = new NamedThread(handler, controllerKeyName + "-SocketHandler");
        handlerThread.start();

        logger.logDebug(getClass().getSimpleName() + ".startup() - End");
    }

    @Override
    protected void shutdown() {
        logger.logDebug(getClass().getSimpleName() + ".shutdown() -- Start");

        handler.shutdown();

        logger.logDebug(getClass().getSimpleName() + ".shutdown() -- End");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

       // String s = "Msg:"+ receivedText ;
        //logger.logTxByteCommunication(s.getBytes(), 0, s.length());
        if (!receivedMessageProcessed) {
            switch (receivedEventType) {
            case MessageEventConsts.COMM_EVENT_TYPE:
                // Socket handler publishes this event as socket connection goes.
                // We use it for updating SACPort's controller status.
                processCommunicationEvent();
                receivedMessageProcessed = true;
                break;
            case MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE:
            	//out bound messages 
                handler.getTransactionHandler().addOutboundMessage(receivedText);
                receivedMessageProcessed = true;
                break;
            default:
                receivedMessageProcessed = false;
                break;
            }
        }
    }

    /**
     * Prepare a properties built from DB for the connectiton
     * 
     * @return The properties built from DB
     */
    private Properties loadConnectionConfigurations() {
        // SACHost
        String commType = getConfigProperty(COMM_TYPE);
        String entryName = Application.HOSTCFG_DOMAIN + commType + ".";

        Properties connectionProperties = new Properties();

        // Populate configuration values from hostconfig table
        connectionProperties.put(ConnConfigKeys.PORT_NAME.getValue(), Application.getString(
                entryName + ConnConfigKeys.PORT_NAME.getValue()));
        connectionProperties.put(ConnConfigKeys.INTEGRATOR_NAME.getValue(),
                Application.getString(entryName + ConnConfigKeys.INTEGRATOR_NAME.getValue()));
        connectionProperties.put(ConnConfigKeys.IP_ADDRESS.getValue(), Application.getString(
                entryName + ConnConfigKeys.IP_ADDRESS.getValue()));
        connectionProperties.put(ConnConfigKeys.PORT_NUMBER.getValue(), Application.getString(
                entryName + ConnConfigKeys.PORT_NUMBER.getValue()));
        connectionProperties.put(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue(),
                Application.getString(entryName + ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()));
        connectionProperties.put(ConnConfigKeys.RETRY_INTERVAL.getValue(), Application.getString(
                entryName + ConnConfigKeys.RETRY_INTERVAL.getValue()));
        connectionProperties.put(ConnConfigKeys.ACK_TIMEOUT.getValue(), Application.getString(
                entryName + ConnConfigKeys.ACK_TIMEOUT.getValue()));
        connectionProperties.put(ConnConfigKeys.ACK_MAX_RETRY.getValue(), Application.getString(
                entryName + ConnConfigKeys.ACK_MAX_RETRY.getValue()));
        connectionProperties.put(ConnConfigKeys.USE_STXETX.getValue(), Application.getString(
                entryName + ConnConfigKeys.USE_STXETX.getValue()));
        
        return connectionProperties;
    }

    /**
     * When socket handler reports its communication status, we need to update the current controller's status
     * accordingly.
     * 
     */
    private void processCommunicationEvent() {
        if (receivedText != null && !receivedText.isEmpty()) {
            char firstCharacter = receivedText.charAt(0);
            if (firstCharacter == 'S') {
                if (prevControllerStatus != receivedData) {
                    logger.logDebug(controllerKeyName + "- StatusChange - WAS: " +
                            ControllerConsts.STATUS_TEXT[prevControllerStatus] + " NOW: " +
                            ControllerConsts.STATUS_TEXT[receivedData]);

                    switch (receivedData) {
                    case ControllerConsts.STATUS_RUNNING:
                        setControllerStatus(ControllerConsts.STATUS_RUNNING);
                        break;
                    case ControllerConsts.STATUS_ERROR:
                        setControllerStatus(ControllerConsts.STATUS_ERROR);
                        break;
                    }

                    prevControllerStatus = receivedData;
                }
            } else {
                logger.logError(getClass().getSimpleName()
                        + ".processCommunicationEvent() -- UNKNOWN Event Type \""
                        + firstCharacter + "\" -- processCommunicationEvent()");
            }
        } else {
            logger.logError(getClass().getSimpleName()
                    + ".processCommunicationEvent() - Event Text is null or empty");
        }
    }

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
}
