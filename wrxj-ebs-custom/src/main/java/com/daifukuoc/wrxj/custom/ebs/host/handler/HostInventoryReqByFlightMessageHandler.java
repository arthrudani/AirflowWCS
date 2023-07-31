package com.daifukuoc.wrxj.custom.ebs.host.handler;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByFlightAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByFlightMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseItem;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryreqbyflight.InventoryReqByFlight;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;

/**
 * 
 * @author BT
 *
 */
public class HostInventoryReqByFlightMessageHandler extends Controller {

    public static final short RETRIEVE_ALL = 0;

    private InventoryReqByFlight inventoryRequest;
    private EBSHostServer ebsHostServer;
    private InventoryReqByFlightMessageData inventoryRequestByFlightMessageData;

    /**
     * See {@link com.daifukuamerica.wrxj.controller.ControllerFactory#startController(String)} ControllerFactory
     * expects this method to be available in the controller class
     * 
     * @param controllerConfigs ReadOnlyProperties
     * @return Controller
     * @throws ControllerCreationException When failed to create a controller
     */
    public static Controller create(ReadOnlyProperties controllerConfigs) throws ControllerCreationException {
        Controller thisController = Factory.create(HostInventoryReqByFlightMessageHandler.class);
        return thisController;
    }

    @Override
    protected void initialize(String isControllerKeyName) {
        super.initialize(isControllerKeyName);

        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.initialize() - Start");

        super.subscribeHostInventoryRequestByFlightEvent("%");

        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.initialize() - End");
    }

    @Override
    protected void startup() {
        super.startup();

        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.startup() - Start");

        try {
            ebsHostServer = Factory.create(EBSHostServer.class);
            inventoryRequestByFlightMessageData = Factory.create(InventoryReqByFlightMessageData.class);

            // Load a processor configured in host config table
            inventoryRequest = (InventoryReqByFlight) ProcessorFactory.get(controllersKeyName, InventoryReqByFlight.NAME);

            // Mark this controller as running
            super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

        } catch (Exception e) {
            setControllerStatus(ControllerConsts.STATUS_ERROR);
            logger.logException(e, "Error in starting up HostInventoryRequestByFlightMessageHandler.java");
        }

        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.startup() - End");
        setDetailedControllerStatus("HostInventoryRequestByFlightMessageHandler.java started up.");
    }

    @Override
    protected void shutdown() {
        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.shutdown() - Start");

        if (ebsHostServer != null) {
            ebsHostServer.cleanUp();
        }

        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.shutdown() - End");
        setDetailedControllerStatus("HostInventoryRequestByFlightMessageHandler.java terminated.");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        short processedBags = 0;
        List<InventoryResponseItem> list = null;

        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.processIPCReceivedMessage() - Start");
        if (!receivedMessageProcessed) {
            if (receivedEventType == MessageEventConsts.HOST_INVENTORY_REQUEST_BY_FLIGHT_EVENT_TYPE) {
                logger.logDebug("HostInventoryRequestByFlightMessageHandler.java received inventory request by flight event");

                if (inventoryRequestByFlightMessageData.parse(receivedText)) {
                    logger.logDebug("HostInventoryRequestByFlightMessageHandler.java received inventory request by flight event: "
                            + inventoryRequestByFlightMessageData.toString());
                    // Send ack before processing
                    sendInventoryRequestByFlightAckMsg((short) inventoryRequestByFlightMessageData.getHeader().getSeqNo(), false);
                    try {
                        list = inventoryRequest.getResponseList(inventoryRequestByFlightMessageData);
                        if (list != null && !list.isEmpty()) {
                        	processedBags = (short) (list.size());
                        }
                        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java retrieved order successfully");
                    } catch (InventoryRequestFailureException e) {
                    	processedBags = -1;
                        logger.logException("Inventory Request By Flight failed:", e);
                    } finally {	
                    	 sendInventoryResponseMessage((short) inventoryRequestByFlightMessageData.getHeader().getSeqNo(),
                                 Short.parseShort(inventoryRequestByFlightMessageData.getRequestId()), SACControlMessage.STATUS_SUCCESS,processedBags,list);
                                        
                    }
                } else {
                    int originalSequenceNumber = 0;
                    if (inventoryRequestByFlightMessageData.getHeader() != null) {
                        originalSequenceNumber = inventoryRequestByFlightMessageData.getHeader().getSeqNo();
                    }
                    logger.logError(
                            "HostInventoryRequestByFlightMessageHandler.java received a message that has an invalid Inventory message:"
                                    + receivedText);
                    // Send the ack message
                    sendInventoryRequestByFlightAckMsg((short) originalSequenceNumber, true);
                    // Send the response message
                    sendInventoryResponseMessage((short) originalSequenceNumber,
                            Short.parseShort(inventoryRequestByFlightMessageData.getRequestId()), SACControlMessage.STATUS_FAILED, processedBags,null);
                }
            } else {
                logger.logError(
                        "HostInventoryRequestByFlightMessageHandler.java received unexpected event type: " + receivedEventType);
            }
            // Now mark as it's been processed
            receivedMessageProcessed = true;
        }
        logger.logDebug("HostInventoryRequestByFlightMessageHandler.java.processIPCReceivedMessage() - End");
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
     * This method send the retrieval order message ack 
     * @param originalSequenceNumber 
     * @param isError flag represents error occurred or not
     */
    private void sendInventoryRequestByFlightAckMsg(short originalSequenceNumber, boolean isError) {
    	InventoryReqByFlightAckMessage ackMessage = Factory.create(InventoryReqByFlightAckMessage.class);

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
