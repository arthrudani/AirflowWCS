package com.daifukuoc.wrxj.custom.ebs.host.handler;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalItemAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalItemResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderItemMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalitemorder.LoadItemRetriever;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.LoadRetriever;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;

/**
 * This host message handler processes only retrieval Item event. 
 * 
 * @author BT
 *
 */
public class HostRetrievalItemMessageHandler extends Controller {

	public static final short RETRIEVE_ALL = 0;

    private LoadItemRetriever loadItemRetriever;
    private EBSHostServer ebsHostServer;
    private RetrievalOrderItemMessageData retrievalItemrMessageData;

    /**
     * See {@link com.daifukuamerica.wrxj.controller.ControllerFactory#startController(String)} ControllerFactory
     * expects this method to be available in the controller class
     * 
     * @param controllerConfigs ReadOnlyProperties
     * @return Controller
     * @throws ControllerCreationException When failed to create a controller
     */
    public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException {
        Controller thisController = Factory.create(HostRetrievalItemMessageHandler.class);
        return thisController;
    }

    @Override
    protected void initialize(String isControllerKeyName) {
        super.initialize(isControllerKeyName);

        logger.logDebug("HostRetrievalItemMessageHandler.initialize() - Start");

        super.subscribeHostRetrievalItemEvent("%");

        logger.logDebug("HostRetrievalItemMessageHandler.initialize() - End");
    }

    @Override
    protected void startup() {
        super.startup();

        logger.logDebug("HostRetrievalItemMessageHandler.startup() - Start");

        try {
            ebsHostServer = Factory.create(EBSHostServer.class);
            retrievalItemrMessageData = Factory.create(RetrievalOrderItemMessageData.class);

            // Load a processor configured in host config table
            loadItemRetriever = (LoadItemRetriever) ProcessorFactory.get(controllersKeyName, LoadItemRetriever.NAME);

            // Mark this controller as running
            super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

        } catch (Exception e) {
            setControllerStatus(ControllerConsts.STATUS_ERROR);
            logger.logException(e, "Error in starting up HostRetrievalItemMessageHandler");
        }

        logger.logDebug("HostRetrievalItemMessageHandler.startup() - End");
        setDetailedControllerStatus("HostRetrievalItemMessageHandler started up.");
    }

    @Override
    protected void shutdown() {
        logger.logDebug("HostRetrievalItemMessageHandler.shutdown() - Start");

        if (ebsHostServer != null) {
            ebsHostServer.cleanUp();
        }

        logger.logDebug("HostRetrievalItemMessageHandler.shutdown() - End");
        setDetailedControllerStatus("HostRetrievalItemMessageHandler terminated.");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        short retrievedBags = 0;

        logger.logDebug("HostRetrievalItemMessageHandler.processIPCReceivedMessage() - Start");
        if (!receivedMessageProcessed) {
            if (receivedEventType == MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TYPE) {
                logger.logDebug("HostRetrievalItemMessageHandler received retrieval order event");

                if (retrievalItemrMessageData.parse(receivedText)) {
                    logger.logDebug("HostRetrievalItemMessageHandler received retrieval order event: "
                            + retrievalItemrMessageData.toString());
                    // Send ack before processing
                    sendRetrievalItemAckMsg((short) retrievalItemrMessageData.getHeader().getSeqNo(), false);
                    try {
                        retrievedBags = loadItemRetriever.retrieve(retrievalItemrMessageData);
                        logger.logDebug("HostRetrievalItemMessageHandler retrieved order successfully");
                    } catch (RetrievalOrderFailureException e) {
                        retrievedBags = -1;
                        logger.logException("Retrieval Item failed:", e);
                    } finally {
                        sendRetrievalItemResponseMessage((short) retrievalItemrMessageData.getHeader().getSeqNo(),
                                Short.parseShort(retrievalItemrMessageData.getOrderId()),
                                determineStatus(Short.parseShort(retrievalItemrMessageData.getNumberOfBags()),
                                        retrievedBags),
                                determineMissingBags(Short.parseShort(retrievalItemrMessageData.getNumberOfBags()),
                                        retrievedBags),retrievalItemrMessageData.getList().toArray());
                    }
                } else {
                    int originalSequenceNumber = 0;
                    if (retrievalItemrMessageData.getHeader() != null) {
                        originalSequenceNumber = retrievalItemrMessageData.getHeader().getSeqNo();
                    }
                    logger.logError(
                            "HostRetrievalItemMessageHandler received a message that has an invalid retrieval order message:"
                                    + receivedText);
                    // Send the ack message
                    sendRetrievalItemAckMsg((short) originalSequenceNumber, true);
                    // Send the response message
                    sendRetrievalItemResponseMessage((short) originalSequenceNumber,
                            Short.parseShort(retrievalItemrMessageData.getOrderId()), SACControlMessage.STATUS_FAILED, (short) 0,retrievalItemrMessageData.getList().toArray());
                }
            } else {
                logger.logError(
                        "HostRetrievalItemMessageHandler received unexpected event type: " + receivedEventType);
            }
            // Now mark as it's been processed
            receivedMessageProcessed = true;
        }
        logger.logDebug("HostRetrievalItemMessageHandler.processIPCReceivedMessage() - End");
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
                && retrievedBags < requestedNumberOfBagsToRetrieve){
            return (short) (requestedNumberOfBagsToRetrieve - retrievedBags);
        }

        return (short) 0;
    }

    /**
     * This method send the retrieval Item message ack 
     * @param originalSequenceNumber 
     * @param isError flag represents error occurred or not
     */
    private void sendRetrievalItemAckMsg(short originalSequenceNumber, boolean isError) {
    	RetrievalItemAckMessage ackMessage = Factory.create(RetrievalItemAckMessage.class);

        if (isError) {
            // When error is set
            ackMessage.setStatus(SACControlMessage.AckStatus.MESSAGE_ERROR.getValue());
        } else {
            ackMessage.setStatus(SACControlMessage.AckStatus.OK.getValue());
        }

        ackMessage.format();
        byte[] encoded = ackMessage.prepareMessageToSend((int) originalSequenceNumber);
        String messageToSend = new String(encoded);
        publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }

    private void sendRetrievalItemResponseMessage(short originalSequenceNumber, short orderId, short status,
            short numberOfShortages, Object[] objects) {

    	RetrievalItemResponseMessage responseMessage = Factory.create(RetrievalItemResponseMessage.class);
        responseMessage.setOrderID(orderId);
        responseMessage.setStatus(status);
        
        if(numberOfShortages>0)
        	responseMessage.setArrayOfMissingBags(objects.toString());
        
        responseMessage.setArrayLength(numberOfShortages); 
        responseMessage.format();
        String messageToSend = new String(responseMessage.prepareMessageToSend((int) originalSequenceNumber));

        publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        super.decodeIpcMessage(receivedMessage);
    }
}
