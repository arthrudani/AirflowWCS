package com.daifukuoc.wrxj.custom.ebs.host.handler;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryUpdateResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.InventoryUpdater;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.exception.InventoryUpdateFailureException;


/**
 * This host message handler processes only inventory update event. InventoryUpdateParser will send the event with
 * the received message body.
 * 
 * @author MT
 *
 */

public class HostInventoryUpdateMessageHandler extends Controller {
	
	private InventoryUpdater inventoryUpdater;
	private EBSHostServer ebsHostServer;
	private InventoryUpdateMessageData inventoryUpdateMessageData;
	
	
	/**
     * See {@link com.daifukuamerica.wrxj.controller.ControllerFactory#startController(String)} ControllerFactory
     * expects this method to be available in the controller class
     * 
     * @param controllerConfigs ReadOnlyProperties
     * @return Controller
     * @throws ControllerCreationException When failed to create a controller
     */
    public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException {
        Controller thisController = Factory.create(HostInventoryUpdateMessageHandler.class);
        return thisController;
    }

    @Override
    protected void initialize(String isControllerKeyName) {
        super.initialize(isControllerKeyName);

        logger.logDebug("HostInventoryUpdateMessageHandler.initialize() - Start");

        // Subscribe to the 'InventoryUpdate' event
        // The event is sent by InventoryUpdateParser
        super.subscribeHostInventoryUpdateEvent("%");

        logger.logDebug("HostInventoryUpdateMessageHandler.initialize() - End");
    }
    
    @Override
    protected void startup() {
        super.startup();

        logger.logDebug("HostInventoryUpdateMessageHandler.startup() - Start");

        try {
            ebsHostServer = Factory.create(EBSHostServer.class);
            inventoryUpdateMessageData = Factory.create(InventoryUpdateMessageData.class);

            // Load a processor configured in host config table
            inventoryUpdater = (InventoryUpdater) ProcessorFactory.get(controllersKeyName, InventoryUpdater.NAME);

            // Mark this controller as running
            super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

        } catch (Exception e) {
            setControllerStatus(ControllerConsts.STATUS_ERROR);
            logger.logException(e, "Error in starting up HostInventoryUpdateMessageHandler");
        }

        logger.logDebug("HostInventoryUpdateMessageHandler.startup() - End");
        setDetailedControllerStatus("HostInventoryUpdateMessageHandler started up.");
    }
    
    @Override
    protected void shutdown() {
        logger.logDebug("HostInventoryUpdateMessageHandler.shutdown() - Start");

        if (ebsHostServer != null) {
            ebsHostServer.cleanUp();
        }

        logger.logDebug("HostInventoryUpdateMessageHandler.shutdown() - End");
        setDetailedControllerStatus("HostInventoryUpdateMessageHandler terminated.");

        super.shutdown();
    }
    
    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        logger.logDebug("HostInventoryUpdateMessageHandler.processIPCReceivedMessage() - Start");
        if (!receivedMessageProcessed) {
            if (receivedEventType == MessageEventConsts.HOST_INVENTORY_UPDATE_EVENT_TYPE) {
                logger.logDebug("HostInventoryUpdateMessageHandler received inventory update event");

                if (inventoryUpdateMessageData.parse(receivedText)) {
                    logger.logDebug("HostInventoryUpdateMessageHandler received inventory update event: "
                            + inventoryUpdateMessageData.toString());
                    try {
                        inventoryUpdater.update(inventoryUpdateMessageData);
                        logger.logDebug("HostInventoryUpdateMessageHandler updated inventory successfully");
                        sendInventoryUpdateResponseMessage((short)inventoryUpdateMessageData.getHeader().getSeqNo(), (short)0);
                    } catch (InventoryUpdateFailureException e) {
                        logger.logException("Inventory update failed:", e);
                        sendInventoryUpdateResponseMessage((short)inventoryUpdateMessageData.getHeader().getSeqNo(), (short)1);
                    }
                } else {
                    int originalSequenceNumber = 0;
                    if (inventoryUpdateMessageData.getHeader() != null) {
                        originalSequenceNumber = inventoryUpdateMessageData.getHeader().getSeqNo();
                    }
                    logger.logError(
                            "HostInventoryUpdateMessageHandler received a message that has an invalid inventory update message:"
                                    + receivedText);
                    sendInventoryUpdateResponseMessage((short)originalSequenceNumber, (short)1);
                }
            } else {
                logger.logError(
                        "HostInventoryUpdateMessageHandler received unexpected event type: " + receivedEventType);
            }
            // Now mark as it's been processed
            receivedMessageProcessed = true;
        }
        logger.logDebug("HostInventoryUpdateMessageHandler.processIPCReceivedMessage() - End");
    }

    private void sendInventoryUpdateResponseMessage(short originalSequenceNumber, short status) {

        InventoryUpdateResponseMessage responseMessage = Factory.create(InventoryUpdateResponseMessage.class);
        responseMessage.setStatus(status);
        
        responseMessage.format();
        String messageToSend = new String(responseMessage.prepareMessageToSend((int)originalSequenceNumber));
        publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }
    
    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
	
}
