package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;

/**
 * This takes care of running socket communication with 1 ACP port. 
 * - Starts up server socket handler to initiate communication with ACP 
 * - Receives communication events to update controller status 
 * - Receives received inbound messages to update to system HB server
 * 
 * @author LK
 *
 */
public class ACPPort extends Controller {
    private final ReadOnlyProperties config;
    private String controllerKeyName = "";
    private Thread handlerThread = null;
    private ACPServerSocketHandler handler = null;
    private int prevControllerStatus = ControllerConsts.STATUS_UNKNOWN;

    public ACPPort(ReadOnlyProperties config) {
        super();
        this.config = config;
    }

    public static Controller create(ReadOnlyProperties config) throws ControllerCreationException {
        return new ACPPort(config);
    }

    @Override
    public void initialize(String controllerKeyName) {
        super.initialize(controllerKeyName);

        logger.logDebug(getClass().getSimpleName() + ".initialize() - Start");

        this.controllerKeyName = controllerKeyName;

        logger.addCommLogger();

        setEquipmentPortCKN(controllerKeyName);
        subscribeCommEvent();
        // CUSTOM_ALLOCATION_EVENT_TYPE is one of equipment event.
        // See AbstractIPCMessenger.publishCustomEquipmentEvent().
        subscribeEquipmentEvent();
        subscribeControlEvent();

        logger.logDebug(getClass().getSimpleName() + ".initialize() - End");
    }

    @Override
    public void startup() {
        // Not calling AbstractIPCMessenger.startup() as it starts an unnecessary timer thread by default.
        logger.logDebug(getClass().getSimpleName() + ".startup() - Start");

        String startMessage = "********** " + getName() + ": starting controller. **********";
        logger.logTxByteCommunication(startMessage.getBytes(), 0, startMessage.length());

        // TODO Use TCPConnectionConfiguration
        PortData portData = readPortData(config.getString(ControllerDefinition.CONTROLLER_NAME));
        if (portData != null) {
            try {
                handler = new ACPServerSocketHandler(portData, logger, getMessageService());
                handlerThread = new NamedThread(handler, controllerKeyName + "-SocketHandler");
                handlerThread.start();
            } catch (Exception e) {
                logger.logException("Failed to create a server socket handler", e);
            }
        }

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

        if (!receivedMessageProcessed) {
            switch (receivedEventType) {
            case MessageEventConsts.COMM_EVENT_TYPE:
                // ServerSocketHandler publishes this event as socket connection goes.
                // We use it for updating ACPPort's controller status.
                processCommunicationEvent();
                receivedMessageProcessed = true;
                break;
            case MessageEventConsts.CUSTOM_ALLOCATION_EVENT_TYPE:
                // PLCDevice publishes this event when a message needs to be sent out to ACP
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
     * Populate PortData of the current port name
     * 
     * @param portName The port name = sPortName of port table
     * @return The populated PortData
     */
    private PortData readPortData(String portName) {
        PortData portData = null;

        StandardPortServer portServer = Factory.create(StandardPortServer.class);
        try {
            portData = portServer.getPort(portName);
            if (portData == null) {
                throw new DBException("Port [" + portName + "] not found!");
            }
        } catch (DBException ex) {
            logger.logException("Failed to get a port data of " + portName, ex);
        } finally {
            portServer.cleanUp();
        }

        return portData;
    }

    /**
     * When server socket handler reports its communication status, we need to update the current controller's status
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
