package com.daifukuoc.wrxj.custom.ebs.host.handler;

import java.util.List;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByWarehouseAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByWarehouseMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseItem;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.invreqbywarehouse.InvReqByWarehouseProcessor;

/**
 * This host message handler processes only inventory request by warehouse event. InventoryRequestByWarehouseProcessor will send the event with the
 * received message body.
 * 
 * @author MT
 *
 */
public class HostInvReqByWarehouseMessageHandler extends Controller {

    public static final short RETRIEVE_ALL = 0;

    private InvReqByWarehouseProcessor invReqByWarehouseProcessor;
    private EBSHostServer ebsHostServer;
    private InventoryReqByWarehouseMessageData inventoryReqByWarehouseMessageData;

    /**
     * See {@link com.daifukuamerica.wrxj.controller.ControllerFactory#startController(String)} ControllerFactory
     * expects this method to be available in the controller class
     * 
     * @param controllerConfigs ReadOnlyProperties
     * @return Controller
     * @throws ControllerCreationException When failed to create a controller
     */
    public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException {
        Controller thisController = Factory.create(HostInvReqByWarehouseMessageHandler.class);
        return thisController;
    }

    @Override
    protected void initialize(String isControllerKeyName) {
        super.initialize(isControllerKeyName);

        logger.logDebug("HostInvReqByWarehouseMessageHandler.initialize() - Start");

        // Subscribe to the 'RetrievalOrder' event
        // The event is sent by RetrievalOrderParser
        super.subscribeHostInventoryReqByWarehouseEvent("%");

        logger.logDebug("HostInvReqByWarehouseMessageHandler.initialize() - End");
    }

    @Override
    protected void startup() {
        super.startup();

        logger.logDebug("HostInvReqByWarehouseMessageHandler.startup() - Start");

        try {
            ebsHostServer = Factory.create(EBSHostServer.class);
            inventoryReqByWarehouseMessageData = Factory.create(InventoryReqByWarehouseMessageData.class);

            // Load a processor configured in host config table
            invReqByWarehouseProcessor = (InvReqByWarehouseProcessor) ProcessorFactory.get(controllersKeyName, InvReqByWarehouseProcessor.NAME);

            // Mark this controller as running
            super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

        } catch (Exception e) {
            setControllerStatus(ControllerConsts.STATUS_ERROR);
            logger.logException(e, "Error in starting up HostInvReqByWarehouseMessageHandler");
        }

        logger.logDebug("HostInvReqByWarehouseMessageHandler.startup() - End");
        setDetailedControllerStatus("HostInvReqByWarehouseMessageHandler started up.");
    }

    @Override
    protected void shutdown() {
        logger.logDebug("HostInvReqByWarehouseMessageHandler.shutdown() - Start");

        if (ebsHostServer != null) {
            ebsHostServer.cleanUp();
        }

        logger.logDebug("HostInvReqByWarehouseMessageHandler.shutdown() - End");
        setDetailedControllerStatus("HostInvReqByWarehouseMessageHandler terminated.");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();
        short processedBags = 0;
        List<InventoryResponseItem> list = null;
        
        logger.logDebug("HostInvReqByWarehouseMessageHandler.processIPCReceivedMessage() - Start");
        if (!receivedMessageProcessed) {
            if (receivedEventType == MessageEventConsts.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TYPE) {
                logger.logDebug("HostInvReqByWarehouseMessageHandler received retrieval order event");

                if (inventoryReqByWarehouseMessageData.parse(receivedText)) {
                    logger.logDebug("HostInvReqByWarehouseMessageHandler received retrieval order event: "
                            + inventoryReqByWarehouseMessageData.toString());
                    // Send ack before processing
                    sendInventoryReqByWarehouseAckMsg((short) inventoryReqByWarehouseMessageData.getHeader().getSeqNo(), false);
                      
                    try {
                        list = invReqByWarehouseProcessor.getResponseList(inventoryReqByWarehouseMessageData);
                        if (list != null && !list.isEmpty()) {
                        	processedBags = (short) (list.size());
                        }
                        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java retrieved order successfully");
                    } catch (InventoryRequestFailureException e) {
                    	processedBags = -1;
                        logger.logException("Inventory Request By Flight failed:", e);
                    } finally {	
                    	 sendInventoryResponseMessage((short) inventoryReqByWarehouseMessageData.getHeader().getSeqNo(),
                                 Short.parseShort(inventoryReqByWarehouseMessageData.getRequestID()), SACControlMessage.STATUS_SUCCESS,processedBags,list);
                                        
                    }
                } else {
                    int originalSequenceNumber = 0;
                    if (inventoryReqByWarehouseMessageData.getHeader() != null) {
                        originalSequenceNumber = inventoryReqByWarehouseMessageData.getHeader().getSeqNo();
                    }
                    logger.logError(
                            "HostInvReqByWarehouseMessageHandler received a message that has an invalid inventory request by warehouse message:"
                                    + receivedText);
                    // Send the ack message
                    sendInventoryReqByWarehouseAckMsg((short) originalSequenceNumber, true);
                    sendInventoryResponseMessage((short) originalSequenceNumber,
                            Short.parseShort(inventoryReqByWarehouseMessageData.getRequestID()), SACControlMessage.STATUS_FAILED, processedBags,null);
                }
            } else {
                logger.logError(
                        "HostInvReqByWarehouseMessageHandler received unexpected event type: " + receivedEventType);
            }
            // Now mark as it's been processed
            receivedMessageProcessed = true;
        }
        logger.logDebug("HostInvReqByWarehouseMessageHandler.processIPCReceivedMessage() - End");
    }


    private void sendInventoryResponseMessage(short originalSequenceNumber, short orderId, short status,
            short numberOfBags,  List<InventoryResponseItem> list) {

    	InventoryResponseMessage responseMessage = Factory.create(InventoryResponseMessage.class);
        responseMessage.setOrderID(orderId);
        responseMessage.setStatus(status);
        
        if(numberOfBags>0 && list != null)
        {
        	responseMessage.setArrayOfBags( getConvertListToString ( list ));
        }
        responseMessage.setArrayLength(numberOfBags); 
        responseMessage.format();
        String messageToSend = new String(responseMessage.prepareMessageToSend((int) originalSequenceNumber));
        publishHostMesgSendEvent(messageToSend, 0, SACControlMessage.HOST_PORT_EVENT);
    }
    private String getConvertListToString( List<InventoryResponseItem> list)
    {
    	String comma = SACControlMessage.COMMA;
    	StringBuilder strBuilder = new StringBuilder();
    	Integer index = 0;
    	for(InventoryResponseItem item: list)
    	{
    		strBuilder.append(item.getLoadId()).append(comma)
    		.append(item.getGlobalID()).append(comma)
    		.append(item.getLineId()).append(comma)
    		.append(item.getFlightNumber()).append(comma);
    		 String sFlightNum =MessageUtil.formatDate( item.getFlightSTD() );
    		 strBuilder.append(sFlightNum ).append(comma)
    		.append(item.getLocationID()).append(comma)
    		.append(item.getWarehouseID());
    		index++;
    		if(index <list.size())
    		{
    			//to avoid adding a comma to the end
    			strBuilder.append(comma);
    		}
    	}
    	return strBuilder.toString();
    }
    /**
     * This method send the inventory request by warehouse message ack 
     * @param originalSequenceNumber 
     * @param isError flag represents error occurred or not
     */
    private void sendInventoryReqByWarehouseAckMsg(short originalSequenceNumber, boolean isError) {
    	InventoryReqByWarehouseAckMessage ackMessage = Factory.create(InventoryReqByWarehouseAckMessage.class);

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

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
}
