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
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.LoadRetriever;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;

/**
 * This host message handler processes only retrieval order event. RetrievalOrderParser will send the event with the
 * received message body.
 * 
 * @author LK
 *
 */
public class HostRetrievalOrderMessageHandler extends Controller {

    public static final short RETRIEVE_ALL = 0;

    private LoadRetriever loadRetriever;
    private EBSHostServer ebsHostServer;
    private RetrievalOrderMessageData retrievalOrderMessageData;

    /**
     * See {@link com.daifukuamerica.wrxj.controller.ControllerFactory#startController(String)} ControllerFactory
     * expects this method to be available in the controller class
     * 
     * @param controllerConfigs ReadOnlyProperties
     * @return Controller
     * @throws ControllerCreationException When failed to create a controller
     */
    public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException {
        Controller thisController = Factory.create(HostRetrievalOrderMessageHandler.class);
        return thisController;
    }

    @Override
    protected void initialize(String isControllerKeyName) {
        super.initialize(isControllerKeyName);

        logger.logDebug("HostRetrievalOrderMessageHandler.initialize() - Start");

        // Subscribe to the 'RetrievalOrder' event
        // The event is sent by RetrievalOrderParser
        super.subscribeHostRetrievalOrderEvent("%");

        logger.logDebug("HostRetrievalOrderMessageHandler.initialize() - End");
    }

    @Override
    protected void startup() {
        super.startup();

        logger.logDebug("HostRetrievalOrderMessageHandler.startup() - Start");

        try {
            ebsHostServer = Factory.create(EBSHostServer.class);
            retrievalOrderMessageData = Factory.create(RetrievalOrderMessageData.class);

            // Load a processor configured in host config table
            loadRetriever = (LoadRetriever) ProcessorFactory.get(controllersKeyName, LoadRetriever.NAME);

            // Mark this controller as running
            super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

        } catch (Exception e) {
            setControllerStatus(ControllerConsts.STATUS_ERROR);
            logger.logException(e, "Error in starting up HostRetrievalOrderMessageHandler");
        }

        logger.logDebug("HostRetrievalOrderMessageHandler.startup() - End");
        setDetailedControllerStatus("HostRetrievalOrderMessageHandler started up.");
    }

    @Override
    protected void shutdown() {
        logger.logDebug("HostRetrievalOrderMessageHandler.shutdown() - Start");

        if (ebsHostServer != null) {
            ebsHostServer.cleanUp();
        }

        logger.logDebug("HostRetrievalOrderMessageHandler.shutdown() - End");
        setDetailedControllerStatus("HostRetrievalOrderMessageHandler terminated.");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        short retrievedBags = 0;

        logger.logDebug("HostRetrievalOrderMessageHandler.processIPCReceivedMessage() - Start");
        if (!receivedMessageProcessed) {
            if (receivedEventType == MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TYPE) {
                logger.logDebug("HostRetrievalOrderMessageHandler received retrieval order event");

                if (retrievalOrderMessageData.parse(receivedText)) {
                    logger.logDebug("HostRetrievalOrderMessageHandler received retrieval order event: "
                            + retrievalOrderMessageData.toString());
                    // Send ack before processing
                    sendRetrievalOrderAckMsg((short) retrievalOrderMessageData.getHeader().getSeqNo(), false);
                    try {
                        retrievedBags = loadRetriever.retrieve(retrievalOrderMessageData);
                        logger.logDebug("HostRetrievalOrderMessageHandler retrieved order successfully");
                    } catch (RetrievalOrderFailureException e) {
                        retrievedBags = -1;
                        logger.logException("Retrieval order failed:", e);
                    } finally {
                        sendRetrievalOrderResponseMessage((short) retrievalOrderMessageData.getHeader().getSeqNo(),
                                Short.parseShort(retrievalOrderMessageData.getOrderId()),
                                determineStatus(Short.parseShort(retrievalOrderMessageData.getNumberOfBags()),
                                        retrievedBags),
                                determineMissingBags(Short.parseShort(retrievalOrderMessageData.getNumberOfBags()),
                                        retrievedBags));
                    }
                } else {
                    int originalSequenceNumber = 0;
                    if (retrievalOrderMessageData.getHeader() != null) {
                        originalSequenceNumber = retrievalOrderMessageData.getHeader().getSeqNo();
                    }
                    logger.logError(
                            "HostRetrievalOrderMessageHandler received a message that has an invalid retrieval order message:"
                                    + receivedText);
                    // Send the ack message
                    sendRetrievalOrderAckMsg((short) originalSequenceNumber, true);
                    // Send the response message
                    sendRetrievalOrderResponseMessage((short) originalSequenceNumber,
                            Short.parseShort(retrievalOrderMessageData.getOrderId()), SACControlMessage.STATUS_FAILED, (short) 0);
                }
            } else {
                logger.logError(
                        "HostRetrievalOrderMessageHandler received unexpected event type: " + receivedEventType);
            }
            // Now mark as it's been processed
            receivedMessageProcessed = true;
        }
        logger.logDebug("HostRetrievalOrderMessageHandler.processIPCReceivedMessage() - End");
    }

    private short determineStatus(short requestedNumberOfBagsToRetrieve, short retrievedBags) {
        if (requestedNumberOfBagsToRetrieve == RETRIEVE_ALL && retrievedBags > 0) {
            return SACControlMessage.STATUS_SUCCESS;
        } else if (requestedNumberOfBagsToRetrieve > 0 & retrievedBags > 0) {
            if (retrievedBags >= requestedNumberOfBagsToRetrieve) {
                return SACControlMessage.STATUS_SUCCESS;
            } else {
                return SACControlMessage.STATUS_COMPLETED_WITH_SHORTAGE;
            }
        } else if (retrievedBags == 0) {
            return SACControlMessage.STATUS_COMPLETED_WITH_SHORTAGE;
        }
        return SACControlMessage.STATUS_FAILED;
    }

    private short determineMissingBags(short requestedNumberOfBagsToRetrieve, short retrievedBags) {
        if (requestedNumberOfBagsToRetrieve > 0 && retrievedBags > 0
                && retrievedBags < requestedNumberOfBagsToRetrieve) {
            return (short) (requestedNumberOfBagsToRetrieve - retrievedBags);
        }

        return (short) 0;
    }

    /**
     * This method send the retrieval order message ack 
     * @param originalSequenceNumber 
     * @param isError flag represents error occurred or not
     */
    private void sendRetrievalOrderAckMsg(short originalSequenceNumber, boolean isError) {
    	RetrievalOrderAckMessage ackMessage = Factory.create(RetrievalOrderAckMessage.class);

        if (isError) {
            // When error is set
            ackMessage.setStatus(SACControlMessage.AckStatus.MESSAGE_ERROR.getValue());
        } else {
            ackMessage.setStatus(SACControlMessage.AckStatus.OK.getValue());
        }

        ackMessage.format();
        byte[] encoded = ackMessage.prepareMessageToSend((int) originalSequenceNumber);
        String messageToSend = new String(encoded);
        // FIXME Where to get "HostPort" ???
        publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }

    private void sendRetrievalOrderResponseMessage(short originalSequenceNumber, short orderId, short status,
            short numberOfShortages) {

        RetrievalOrderResponseMessage responseMessage = Factory.create(RetrievalOrderResponseMessage.class);
        responseMessage.setOrderID(orderId);
        responseMessage.setStatus(status);
        responseMessage.setNumberOfMissingBags(numberOfShortages);

        responseMessage.format();
        String messageToSend = new String(responseMessage.prepareMessageToSend((int) originalSequenceNumber));
        publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
}
