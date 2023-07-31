package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.MessageParserFactory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuoc.wrxj.custom.ebs.host.communication.MessageType;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;

/**
 * This takes care of integrating the port with business logics
 * 
 * @author LK
 *
 */
public class SACIntegrator extends Controller {
    private final ReadOnlyProperties config;
    private String controllerKeyName = "";

    public SACIntegrator(ReadOnlyProperties config) {
        super();
        this.config = config;
    }

    public static Controller create(ReadOnlyProperties config) throws ControllerCreationException {
        return new SACIntegrator(config);
    }

    @Override
    public void initialize(String controllerKeyName) {
        super.initialize(controllerKeyName);

        logger.logDebug(getClass().getSimpleName() + ".initialize() - Start");

        // controllerKeyName will be HostIntegrator which is the name of this controller
        this.controllerKeyName = controllerKeyName;

        // Host message receive event is sent from SACTransactionHandler whenever a message is received from SAC
        subscribeHostMesgReceiveEvent(this.controllerKeyName, false);

        logger.logDebug(getClass().getSimpleName() + ".initialize() - End");
    }

    @Override
    public void startup() {
        // Not calling AbstractIPCMessenger.startup() as it starts an unnecessary timer thread by default.
        logger.logDebug(getClass().getSimpleName() + ".startup() - Start");

        setControllerStatus(ControllerConsts.STATUS_RUNNING);

        logger.logDebug(getClass().getSimpleName() + ".startup() - End");
    }

    @Override
    protected void shutdown() {
        logger.logDebug(getClass().getSimpleName() + ".shutdown() -- Start");

        logger.logDebug(getClass().getSimpleName() + ".shutdown() -- End");

        super.shutdown();
    }

    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        if (!receivedMessageProcessed) {
            switch (receivedEventType) {
            case MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE:
                processReceivedHostMessage(receivedText);
                receivedMessageProcessed = true;
                break;
            default:
                receivedMessageProcessed = false;
                break;
            }
        }
    }

    private void processReceivedHostMessage(String receivedMessage) {
        if (receivedMessage == null || receivedMessage.isEmpty()) {
            logger.logDebug("Null or empty message can't be processed");
            return;
        }

        // For example, expected receipt message:
        // 84,999,52,0,0,0,0,0,104,104,104,BAG104,FL1000,20221201134500,20230101123456,3333,1,1
        // - 84 = message length
        // - 999 = sequence number
        // - 52 = message type
        // - other fields in the header and body
        String[] split = receivedMessage.split(MessageUtil.HOST_INBOUND_MESSAGE_DELIMITER);
        if (split.length < 3) {
            logger.logError(
                    "The received converted message should have 3 values at least - message length, sequence number and message type");
            return;
        }

        short messageType = Short.parseShort(split[2]);
        switch (messageType) {
        case SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE:
            publishHostExpectedReceiptEvent(receivedMessage);
            break;
        case SACControlMessage.EXPECTED_RECIEPT_RESPONSE_ACK_MSG_TYPE:
            // No business logic required yet for ER response ack
            break;
        case SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_TYPE:
        	// No business logic required yet for retrieval flight response ack
        	break;
        case SACControlMessage.RETRIEVAL_ITEM_RESPONSE_ACK_MSG_TYPE:
        	// No business logic required yet for retrieval Item response ack
        	break;
        case SACControlMessage.INVENTORY_RESPONSE_ACK_MSG_TYPE:
        	// No business logic required yet for retrieval Item response ack
        	break;
        case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE:
            publishHostRetrievalOrderEvent(receivedMessage);
            break;
        case SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE:
            publishHostInventoryReqByWarehouseEvent(receivedMessage);
            break;
        case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE:
            publishHostInventoryRequestEvent(receivedMessage);
            break;
        case SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE:
        	publishHostRetrievalItemEvent(receivedMessage);
            break;
        case SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE:
            if (split.length == SACControlMessage.FLIGHT_DATA_UPDATE_SPLIT_LEN) {
                publishHostFlightDataUpdateEvent(receivedMessage);                
                publishHostToPlcEvent(receivedMessage);
            }
            break; 
        case SACControlMessage.INVENTORY_UPDATE_MSG_TYPE:
        	if (split.length == SACControlMessage.INVENTORY_UPDATE_SPLIT_LEN) {
        		publishHostInventoryUpdateEvent(receivedMessage);
        		publishHostToPlcEvent(receivedMessage);
        	}
        	break;
        case SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE:
            break;
        case SACControlMessage.ITEM_RELEASE_ACK_MSG_TYPE:
            break;
        default:
            logger.logError("The received converted message is for unimplemented message type: " + messageType + ", so will be ignored");
            break;
        }
    }

    @Override
    protected void decodeIpcMessage(IpcMessage receivedMessage) {
        // FIXME: This is only required for unit testing
        super.decodeIpcMessage(receivedMessage);
    }
}
