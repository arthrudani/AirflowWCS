package com.daifukuoc.wrxj.custom.ebs.host.handler;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.FlightDataUpdater;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.exception.FlightDataUpdateFailureException;

/**
 * This host message handler processes only flight data update event. FlightDataUpdateParser will send the event with
 * the received message body.
 * 
 * @author LK
 *
 */
public class HostFlightDataUpdateMessageHandler extends Controller {

    private FlightDataUpdater flightDataUpdater;
    private EBSHostServer ebsHostServer;
    private FlightDataUpdateMessageData flightDataUpdateMessageData;

    /**
     * See {@link com.daifukuamerica.wrxj.controller.ControllerFactory#startController(String)} ControllerFactory
     * expects this method to be available in the controller class
     * 
     * @param controllerConfigs ReadOnlyProperties
     * @return Controller
     * @throws ControllerCreationException When failed to create a controller
     */
    public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException {
        Controller thisController = Factory.create(HostFlightDataUpdateMessageHandler.class);
        return thisController;
    }

    @Override
    protected void initialize(String isControllerKeyName) {
        super.initialize(isControllerKeyName);

        logger.logDebug("HostFlightDataUpdateMessageHandler.initialize() - Start");

        // Subscribe to the 'FlightDataUpdate' event
        // The event is sent by FlightDataUpdateParser
        super.subscribeHostFlightDataUpdateEvent("%");

        logger.logDebug("HostFlightDataUpdateMessageHandler.initialize() - End");
    }

    @Override
    protected void startup() {
        super.startup();

        logger.logDebug("HostFlightDataUpdateMessageHandler.startup() - Start");

        try {
            ebsHostServer = Factory.create(EBSHostServer.class);
            flightDataUpdateMessageData = Factory.create(FlightDataUpdateMessageData.class);

            // Load a processor configured in host config table
            flightDataUpdater = (FlightDataUpdater) ProcessorFactory.get(controllersKeyName, FlightDataUpdater.NAME);

            // Mark this controller as running
            super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

        } catch (Exception e) {
            setControllerStatus(ControllerConsts.STATUS_ERROR);
            logger.logException(e, "Error in starting up HostFlightDataUpdateMessageHandler");
        }

        logger.logDebug("HostFlightDataUpdateMessageHandler.startup() - End");
        setDetailedControllerStatus("HostFlightDataUpdateMessageHandler started up.");
    }

    @Override
    protected void shutdown() {
        logger.logDebug("HostFlightDataUpdateMessageHandler.shutdown() - Start");

        if (ebsHostServer != null) {
            ebsHostServer.cleanUp();
        }

        logger.logDebug("HostFlightDataUpdateMessageHandler.shutdown() - End");
        setDetailedControllerStatus("HostFlightDataUpdateMessageHandler terminated.");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        logger.logDebug("HostFlightDataUpdateMessageHandler.processIPCReceivedMessage() - Start");
        if (!receivedMessageProcessed) {
            if (receivedEventType == MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE) {
                logger.logDebug("HostFlightDataUpdateMessageHandler received flight data update event");

                if (flightDataUpdateMessageData.parse(receivedText)) {
                    logger.logDebug("HostFlightDataUpdateMessageHandler received flight data update event: "
                            + flightDataUpdateMessageData.toString());
                    try {
                        flightDataUpdater.update(flightDataUpdateMessageData);
                        logger.logDebug("HostFlightDataUpdateMessageHandler updated flight data successfully");
                        sendFlightDataUpdateResponseMessage((short)flightDataUpdateMessageData.getHeader().getSeqNo(), (short)0);
                    } catch (FlightDataUpdateFailureException e) {
                        logger.logException("Flight data update failed:", e);
                        sendFlightDataUpdateResponseMessage((short)flightDataUpdateMessageData.getHeader().getSeqNo(), (short)1);
                    }
                } else {
                    int originalSequenceNumber = 0;
                    if (flightDataUpdateMessageData.getHeader() != null) {
                        originalSequenceNumber = flightDataUpdateMessageData.getHeader().getSeqNo();
                    }
                    logger.logError(
                            "HostFlightDataUpdateMessageHandler received a message that has an invalid flight data update message:"
                                    + receivedText);
                    sendFlightDataUpdateResponseMessage((short)originalSequenceNumber, (short)1);
                }
            } else {
                logger.logError(
                        "HostFlightDataUpdateMessageHandler received unexpected event type: " + receivedEventType);
            }
            // Now mark as it's been processed
            receivedMessageProcessed = true;
        }
        logger.logDebug("HostFlightDataUpdateMessageHandler.processIPCReceivedMessage() - End");
    }

    private void sendFlightDataUpdateResponseMessage(short originalSequenceNumber, short status) {

        FlightDataUpdateResponseMessage responseMessage = Factory.create(FlightDataUpdateResponseMessage.class);
        responseMessage.setStatus(status);
        
        responseMessage.format();
        String messageToSend = new String(responseMessage.prepareMessageToSend((int)originalSequenceNumber));
        publishHostMesgSendEvent(messageToSend, 0,SACControlMessage.HOST_PORT_EVENT);
    }

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
}
