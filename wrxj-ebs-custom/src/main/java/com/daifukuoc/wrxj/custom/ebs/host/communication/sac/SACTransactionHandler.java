package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.communication.Message;
import com.daifukuoc.wrxj.custom.ebs.communication.TimeoutChecker;
import com.daifukuoc.wrxj.custom.ebs.communication.Transaction;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionHandler;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageManager;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * A handler responsible for taking care of transactions with SAC *
 * 
 * @author LK
 *
 */
public class SACTransactionHandler implements TransactionHandler {

    private static String WRX_RUN_MODE = Application.getString(WarehouseRx.RUN_MODE);

    private final String portName;
    private final String integratorName;
    private final int ackTimeout;
    private final int ackMaxRetry;
    private final boolean useSTXETX;
    private final Logger logger;
    private final MessageService messageService;
    private final SACMessageManager processor;
    private final TimeoutChecker timeoutChecker;

    private final Lock lock = new ReentrantLock();
    private final List<SACTransaction> list = new ArrayList<>();

    private short outboundSequenceNumber = Short.MAX_VALUE;
    private OutputStream socketOutputStream = null;
    
    private short prevInboundSequenceNumber = 0;
    private boolean isInitialized = false;
    

    /**
     * A constructor
     * 
     * @param portName the name of port, for example, "HostPort"
     * @param integratorName the name of integrator, for example, "HostIntegrator"
     * @param ackTimeout ack message timeout in second
     * @param ackMaxRetry ack max retry
     * @param useSTXETX true if STX/ETX is used, false if not
     * @param logger the logger
     * @param messageService the message service
     * @param messageProcessor the message processor
     * @param timeoutChecker tthe timeout checker
     */
    public SACTransactionHandler(String portName, String integratorName, int ackTimeout, int ackMaxRetry,
            boolean useSTXETX, Logger logger, MessageService messageService, SACMessageManager messageProcessor,
            TimeoutChecker timeoutChecker) {
        this.portName = portName;
        this.integratorName = integratorName;
        this.ackTimeout = ackTimeout;
        this.ackMaxRetry = ackMaxRetry;
        this.useSTXETX = useSTXETX;
        this.logger = logger;
        this.messageService = messageService;
        this.processor = messageProcessor;
        this.timeoutChecker = timeoutChecker;
        
        logger.addCommLogger();
    }

    @Override
    public void connected(long keepAliveTimeoutInSeconds, OutputStream socketOutputStream) {
        lock.lock();
        try {
        	isInitialized = false;//Connected again!
            setSocketOutputStream(socketOutputStream);
            timeoutChecker.started(keepAliveTimeoutInSeconds);
          
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void disconnected() {
        lock.lock();
        try {
            // Not doing anything at the moment
        	isInitialized = false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addInboundMessage(byte[] rawInbound) {
        lock.lock();
        try {
            if (rawInbound == null) {
                logger.logError(
                        "An inbound message is null");
                return false;
            } else if (rawInbound != null && rawInbound.length < PLCConstants.MSG_HEADER_LEN) {
                logger.logError(
                        "An inbound message seems to be too short: " + MessageUtil.encodeHexString(rawInbound));
                return false;
            }

            byte[] headerBytesArray = new byte[PLCConstants.MSG_HEADER_LEN];
            System.arraycopy(rawInbound, 0, headerBytesArray, 0, headerBytesArray.length);
            SACMessageHeader header = processor.processReceivedHeader(headerBytesArray);
            if (header == null) {
                // Socket handler's processReceivedBytes() already validates header before passing it to upper layer.
                // However, we are validating it here again.
                logger.logError("An inbound message has an invalid header: " + MessageUtil.encodeHexString(rawInbound));
                return false;
            }

            processor.setMessage(rawInbound);
            if (!processor.processReceivedMessage()) {
                logger.logError(
                        "Failed to process the received inbound message: " + MessageUtil.encodeHexString(rawInbound));
                return false;
            }

            boolean result = false;
            SACTransaction transaction;
            short inboundMessageType = (short) header.getMsgType();
            short inboundSequenceNumber = (short) header.getSeqNo();
            switch (inboundMessageType) {
            case SACControlMessage.LINK_STARTUP_MSG_TYPE_INT:
            
            	result = processInboundLinkStartUpMessage(rawInbound, header);
            	 break;
            case SACControlMessage.KEEPALIVE_MSG_TYPE:
            	String keepAliveMsg = processor.getMessageTxt();
            	 logger.logDebug("Inbound KeepAlive msg: " + keepAliveMsg);
                // If keep alive message is received, keep alive reply needs to be registered here now.
                transaction = new SACTransaction(new Message(Short.valueOf((short) header.getSeqNo()),
                        inboundMessageType, rawInbound, processor.getMessageTxt()), null);

                // Keep alive message will not use sequence number, so sending 0 always here.
                byte[] rawOutbound = processor.createKeepAliveMessage(MessageUtil.EQUIPMENT_ID, (short) 0);
                transaction.setOutboundToSend(
                        new Message(Short.valueOf((short) 0), PLCConstants.KEEPALIVE_MSG_TYPE, rawOutbound, null));

                // Keep alive is not published

                result = list.add(transaction);
                /*KR:      
                 if (result) {
                    logger.logDebug("A new inbound message is registered: " + transaction);
                } */

                // Save the keep alive received time
                timeoutChecker.ticked();
                break;
            case SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE:
            case SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE:
            case SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE:
            case SACControlMessage.INVENTORY_UPDATE_MSG_TYPE:
            case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE:
           
            	String msg = processor.getMessageTxt();
                // SAC sent a request message and ack reply will be set later
                transaction = new SACTransaction(new Message(Short.valueOf((short) header.getSeqNo()),
                        inboundMessageType, rawInbound, msg), null);
                logToCOM("Incoming converted Msg: " + msg);
                prevInboundSequenceNumber = (short) header.getSeqNo();
                // Now publish the event for the inbound message
                publishInboundMessage(transaction.getInbound().getConverted());

                // Register to the list
                result = list.add(transaction);
             /*   if (result) {
                    logger.logDebug("A new inbound message is registered: " + transaction);
                } */
                break;
            case SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE:
            case SACControlMessage.EXPECTED_RECIEPT_RESPONSE_ACK_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_ITEM_RESPONSE_ACK_MSG_TYPE:
            case SACControlMessage.INVENTORY_RESPONSE_ACK_MSG_TYPE:
            case SACControlMessage.ITEM_RELEASE_ACK_MSG_TYPE:
            case SACControlMessage.LINK_STARTUP_ACK_MSG_TYPE_INT:
                // SAC sent an ack, so we have to find a matching sent outbound message sent by AirflowWCS
                Iterator<SACTransaction> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Transaction existingTransaction = iterator.next();
                    String ackMsg =processor.getMessageTxt();
                    if (existingTransaction.isMatchingSentOutbound(inboundMessageType, inboundSequenceNumber)) {
                        existingTransaction.setReceivedInbound(
                                new Message(Short.valueOf((short) header.getSeqNo()), inboundMessageType, rawInbound,
                                        ackMsg));
                     
                        logToCOM("Incoming Ack Msg:" + ackMsg);
                        /*KR
                        logger.logDebug(
                                "A new inbound message is saved to the existing matching sent outbound message: "
                                        + existingTransaction);
                        */
                        // Now publish the event for the inbound message
                        publishInboundMessage(existingTransaction.getInbound().getConverted());

                        result = true;
                        break;
                    }
                }
                if (!result) {
                    logger.logError(
                            "The existing outbound message that required the received ack message is not found, so will be ignored: "
                                    + MessageUtil.encodeHexString(rawInbound));
                }
                break;
            default:
                result = true;
                logger.logError(
                        "Unexpected inbound message, so will be ignored: " + MessageUtil.encodeHexString(rawInbound));
                break;
            }

            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addOutboundMessage(String convertedOutbound) {
        lock.lock();
        try {
        	this.logToCOM("Outgoing converted msg:"+ convertedOutbound);
        	logger.logDebug("Host Outgoing msg:" + convertedOutbound);
            byte[] raw = encode(convertedOutbound);
            if (raw == null) {
                logger.logError("Failed to encode the outbound message to send: " + convertedOutbound);
                return false;
            }

            boolean result = false;
            SACTransaction transaction;
            short outboundMessageType = getMessageType(raw);
            short outboundSequenceNumber = getSequenceNumber(raw);
            switch (outboundMessageType) {
            case SACControlMessage.KEEPALIVE_MSG_TYPE:
                // Keep alive reply is already registered here when keep alive request is received.
               // logger.logDebug( "Keep alive message reply shouldn't be registered by other part from now on, so will be ignored");
                result = true;
                break;
            case SACControlMessage.StoreCompletionNotify.MSG_TYPE:
            case SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE:
            case SACControlMessage.INVENTORY_RESPONSE_MSG_TYPE:
            case SACControlMessage.ITEM_RELEASE_MSG_TYPE:
                // A new request message sent by AirflowWCS, so we don't need to find an existing request message
                transaction = new SACTransaction(null, new Message(outboundMessageType, raw, convertedOutbound));
                result = list.add(transaction);
                /*KR
                if (result) {
                    logger.logDebug("New outbound message to send out is saved: " + transaction.toString());
                }*/
                break;
            case SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE:
            case SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_TYPE:
            case SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE:	
            case SACControlMessage.INVENTORY_UPDATE_ACK_MSG_TYPE:
            case SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE:
            case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE:
                // Ack reply, so we have to find a matching request message sent by SAC
                Iterator<SACTransaction> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Transaction existingTransaction = iterator.next();
                    if (existingTransaction.isMatchingReceivedInbound(outboundMessageType, outboundSequenceNumber)) {
                        existingTransaction.setOutboundToSend(new Message(outboundMessageType, raw, convertedOutbound));
                       /* KR
                        logger.logDebug("New outbound message is saved to the existing inbound message: "
                                + existingTransaction.toString()); */
                        result = true;
                        break;
                    }
                }
                if (!result) {
                    logger.logError(
                            "The existing inbound message that required the received ack message is not found, so will be ignored: "
                                    + convertedOutbound);
                }
                break;
            default:
                result = true;
                logger.logError("Unexpected outbound message, so will be ignored: " + convertedOutbound);
                break;
            }

            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean process() {
        lock.lock();
        try {
        	clear();
            if( !isInitialized )
            {
            	//add to the list
            	prepareOutboundLinkStartUpMessage();
            	isInitialized = true;
            }
            // Process replies to be sent
            Iterator<SACTransaction> iterator = list.iterator();
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                if (transaction.shouldSendReply()) {
                    // Sequence shouldn't be modified as this is for ack or keep alive messages
                    if (transmit(transaction)) {
                        transaction.markOutboundAsSent();
                    } else {
                        return false;
                    }
                }
            }
            clear();

            // Get the pending transactions that are waiting for ack, but not acked in time
            // - sent and not acked for timeout
            // - number of transmission < max retry
            List<SACTransaction> ackTimedOutButCanRetryList = list.stream()
                    .filter(transaction -> transaction.isAckTimedOutButCanRetry(ackTimeout, ackMaxRetry))
                    .collect(Collectors.toList());
            for (SACTransaction transaction : ackTimedOutButCanRetryList) {
                // As this request was already sent out, don't change sequence number
                if (transmit(transaction)) {
                    transaction.markOutboundAsSent();
                } else {
                    return false;
                }
            }
            clear();

            // Get the requests to transmit
            List<SACTransaction> newRequestList = list.stream()
                    .filter(transaction -> transaction.canSendRequest()).collect(Collectors.toList());
            for (SACTransaction transaction : newRequestList) {
              
            	  // Set a new sequence number just before sending out a new request only if not the LLS msg
            	if( transaction.getOutbound().getType() != SACControlMessage.LINK_STARTUP_MSG_TYPE_INT )
            	{
            		transaction.setOutboundSequenceNumber(nextOutboundSequenceNumber());
            	}
                if (transmit(transaction)) {
                    transaction.markOutboundAsSent();
                } else {
                    return false;
                }
            }
            clear();
            
            return true;
        } finally {
            lock.unlock();
        }
        
    }

    @Override
    public boolean isKeepAliveTimedOut() {
        lock.lock();
        try {
            return timeoutChecker.check();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Convert semicolon delimiter string which is generated from MessageOut.format() to byte array, for example, have a
     * look at ExpectedReceiptResponseMessage.
     * 
     * @param convertedOutbound semicolon delimiter string
     * @return byte array converted from the given string
     */

    private byte[] encode(String convertedOutbound) {
        if (convertedOutbound == null || convertedOutbound.isEmpty()) {
            logger.logDebug("Null or empty string can't be encoded to byte array");
            return null;
        }

        // The string message is formatted by DelimitedFormatter when format() of MessageOut is called
        String[] split = convertedOutbound.split(MessageUtil.HOST_OUTBOUND_MESSAGE_DELIMITER);
        // At least, sequence number, message identifier should be included
        if (split.length < 3) {
            logger.logDebug(
                    "The outbound message must have 3 values at least - sequence number, message identifier and message type");
            return null;
        }

        String messageIdentifier = split[1];
        byte[] raw = null;
        if (MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildExpectedReceiptResponseMessage(convertedOutbound);
        } else if (MessageOutNames.EXPECTED_RECEIPT_ACK.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildExpectedReceiptAckMessage(convertedOutbound);
        } else if (MessageOutNames.STORE_COMPLETE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildStoreCompleteNotifyMessage(convertedOutbound);
        } else if (MessageOutNames.FLIGHT_DATA_UPDATE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildFlightDataUpdateResponseMessage(convertedOutbound);
        } else if (MessageOutNames.ORDER_COMPLETE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildRetrievalOrderResponseMessage(convertedOutbound);
        } else if (MessageOutNames.ITEMS_ORDER_COMPLETE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildRetrievalItemResponseMessage(convertedOutbound);
        } else if (MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildInventoryResponseMessage(convertedOutbound);
        } else if (MessageOutNames.INVENTORY_REQUEST_BY_WAREHOUSE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildInventoryResponseMessage(convertedOutbound);
        } else if (MessageOutNames.RETRIEVAL_FLIGHT_REQUEST_ACK.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildRetrievalFlightAckMessage(convertedOutbound);
        } else if (MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT_ACK.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildInventoryRequestByFlightAckMessage(convertedOutbound);
        } else if (MessageOutNames.RETRIEVAL_ITEM_REQUEST_ACK.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildRetrievalItemAckMessage(convertedOutbound);
        } else if (MessageOutNames.INVENTORY_UPDATE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildInventoryUpdateResponseMessage(convertedOutbound);
        } else if (MessageOutNames.ITEM_RELEASE.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildItemReleaseResponseMessage(convertedOutbound);
        } else if (MessageOutNames.INVENTORY_REQUEST_BY_WAREHOUSE_ACK.getValue().equals(messageIdentifier)) {
            raw = MessageUtil.buildInventoryRequestByWarehouseAckMessage(convertedOutbound);
        }
        else {
            logger.logError("Unimplemented outgoing message: " + convertedOutbound);
        }

        return raw;
    }

    /**
     * Set the output stream
     * 
     * @param socketOutputStream the output stream from the established socket
     */
    private void setSocketOutputStream(OutputStream socketOutputStream) {
        try {
            if (this.socketOutputStream != null) {
                this.socketOutputStream.close();
            }
        } catch (Exception e) {

        } finally {
            this.socketOutputStream = null;
        }
        this.socketOutputStream = socketOutputStream;
    }

    /**
     * Extract message type from byte array
     * 
     * @param raw the byte array
     * @return the message type
     */
    private short getMessageType(byte[] raw) {
        ByteBuffer buf = ByteBuffer.wrap(raw);
        buf.getShort();
        buf.getShort();
        return buf.getShort(); // the 3rd field in header
    }

    /**
     * Extract sequence number from byte array
     * 
     * @param raw the byte array
     * @return the sequence number
     */
    private short getSequenceNumber(byte[] raw) {
        ByteBuffer buf = ByteBuffer.wrap(raw);
        buf.getShort();
        return buf.getShort(); // the 2nd field in header
    }

    /**
     * Publish the received inbound message as a host message receive event
     * 
     * @param converted the CSV string converted from the received byte array
     */
    private void publishInboundMessage(String converted) {

        messageService.publishEvent(portName, WRX_RUN_MODE, converted, 0,
                MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE, MessageEventConsts.HOST_MESG_RECV_TEXT + integratorName);
    }

    /**
     * Get the next sequence number for a new request message
     * 
     * @return the populated sequence number to use
     */
    private short nextOutboundSequenceNumber() {
        if (outboundSequenceNumber >= Short.MAX_VALUE || outboundSequenceNumber < 0) {
            outboundSequenceNumber = 0;
        }
        this.outboundSequenceNumber = (short) (outboundSequenceNumber + 1);
        return outboundSequenceNumber;
    }

    /**
     * Convert the outbound byte array for STX/ETX wrapping if needed
     * 
     * @param rawOutbound the original byte array
     * @param useSTXETX true if wrapping is required
     * @return the modified byte array
     */
    private byte[] wrapOutboundMessageIfRequired(byte[] rawOutbound, boolean useSTXETX) {
        if (useSTXETX) {
            byte[] wrappedByteArray = new byte[rawOutbound.length + 2];
            wrappedByteArray[0] = SACControlMessage.STX;
            System.arraycopy(rawOutbound, 0, wrappedByteArray, 1, rawOutbound.length);
            wrappedByteArray[wrappedByteArray.length - 1] = SACControlMessage.ETX;
            return wrappedByteArray;
        }
        return rawOutbound;
    }

    /**
     * Transmit the transaction to SAC
     * 
     * @param transaction the transaction to transmit
     * @return true if transmission is successfully done, false if not
     */
    private boolean transmit(Transaction transaction) {
        boolean sent = false;

        try {
            byte[] byteArrayToWrite = wrapOutboundMessageIfRequired(transaction.getOutbound().getRaw(),
                    useSTXETX);
            logger.logDebug("Outbound message will be sent out for " + transaction);
            logger.logDebug("Outgoing message written to " + portName + ":"
                    + MessageUtil.encodeHexString(byteArrayToWrite));
            logToCOM("Outgoing Message:" + MessageUtil.encodeHexString(byteArrayToWrite, 0, byteArrayToWrite.length));
            socketOutputStream.write(byteArrayToWrite);
            socketOutputStream.flush();

            sent = true;
        } catch (IOException e) {
            logger.logError("Failed to write to " + portName + " due to " + e.getMessage());
            sent = false;
        }

        return sent;
    }

    private void clear() {
        list.removeIf(transaction -> transaction.isCompleted()
                || transaction.isAckTimedOutAndNoMoreRetryAllowed(ackTimeout, ackMaxRetry));
    }
    private boolean processInboundLinkStartUpMessage(byte[] rawInbound, SACMessageHeader header) {
        // If link start up is received, just update the saved previous inbound sequence number
        boolean result = false;
        SACTransaction transaction = new SACTransaction(new Message(Short.valueOf((short) header.getSeqNo()),
        		SACControlMessage.LINK_STARTUP_MSG_TYPE_INT, rawInbound,
                processor.getMessageTxt()), null);

        prevInboundSequenceNumber = (short) header.getSeqNo();

        // Link start up ack should use the sequence number of inbound link start up message just like other ack
        // messages
        byte[] rawOutbound = processor.createSACLinkStartupAckMessage((short) header.getSeqNo(),
                Integer.valueOf(header.getEquipmentID()));
        transaction.setOutboundToSend(
                new Message(Short.valueOf((short) 0), SACControlMessage.LINK_STARTUP_ACK_MSG_TYPE_INT, rawOutbound,
                        null));
        result = list.add(transaction);
        if (result) {
            logger.logDebug("Inbound link start up message is received, so saved sequence number is updated to "
                    + prevInboundSequenceNumber);
        }
        return result;
    }
    private void prepareOutboundLinkStartUpMessage() {
        boolean result = false;

        // Remove existing outbound link start up message regardless of its status if found
        list.removeIf(transaction -> transaction.getOutbound() != null
                && transaction.getOutbound().getType() == SACControlMessage.LINK_STARTUP_MSG_TYPE_INT);
  
        // Create a new outbound link start up message
        SACTransaction transaction = new SACTransaction(null, new Message(
                Short.valueOf(nextOutboundSequenceNumber()), SACControlMessage.LINK_STARTUP_MSG_TYPE_INT,
                processor.createSACLinkStartUpMessage(MessageUtil.EQUIPMENT_ID, currentOutboundSequenceNumber()), ""));
        
        result = list.add(transaction);

        if (result) {
        	logger.logDebug("A new outbound link start up message is registered: " + transaction);
        }
    }
    
    private short currentOutboundSequenceNumber() {
        return outboundSequenceNumber;
    }
    
    private void logToCOM(String s)
    {
    	 logger.logTxByteCommunication(s.getBytes(), 0, s.length());
    }
}
