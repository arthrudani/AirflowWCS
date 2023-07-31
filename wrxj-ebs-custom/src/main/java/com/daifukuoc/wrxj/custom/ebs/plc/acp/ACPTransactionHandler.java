package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.communication.Message;
import com.daifukuoc.wrxj.custom.ebs.communication.TimeoutChecker;
import com.daifukuoc.wrxj.custom.ebs.communication.Transaction;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionHandler;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder.StandardMessageEncoder;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder.StandardMessageEncoderImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.processor.StandardMessageProcessor;

/**
 * A handler responsible for taking care of transactions with ACP *
 * 
 * @author LK
 *
 */
public class ACPTransactionHandler implements TransactionHandler {

    private static String WRX_RUN_MODE = Application.getString(WarehouseRx.RUN_MODE);

    private final int equipmentId;
    private final String portName;
    private final int ackTimeout;
    private final boolean useSTXETX;
    private final Logger logger;
    private final MessageService messageService;
    private final StandardMessageProcessor processor;
    private final StandardMessageEncoder encoder;
    private final TimeoutChecker timeoutChecker;

    private final Lock lock = new ReentrantLock();
    private final List<ACPTransaction> list = new ArrayList<>();
    private final ACPTransactionState transactionState;

    private short outboundSequenceNumber = 0;
    private short prevInboundSequenceNumber = 0;
    private OutputStream socketOutputStream = null;

    /**
     * A constructor
     * 
     * @param portName the name of port
     * @param useSTXETX true if STX/ETX should be used, false if not
     * @param logger the logger to use
     * @param messageService the message service to use
     * @param messageProcessor the message processor for decoding inbound messages
     * @param encoder the message encoder for encoding outbound messages
     * @param timeoutChecker the timeout checker instance
     * @throws Exception
     */
    public ACPTransactionHandler(String portName,int equipmentID, int ackTimeout, boolean useSTXETX,
            Logger logger, MessageService messageService, StandardMessageProcessor messageProcessor,
            StandardMessageEncoderImpl encoder, TimeoutChecker timeoutChecker) throws Exception {
    	this.equipmentId = equipmentID;
        this.portName = portName;
        this.ackTimeout = ackTimeout;
        this.useSTXETX = useSTXETX;
        this.logger = logger;
        this.messageService = messageService;
        this.processor = messageProcessor;
        this.encoder = encoder;
        this.timeoutChecker = timeoutChecker;
        this.transactionState = new ACPTransactionState(this.portName, this.logger);
    }

    @Override
    public void connected(long keepAliveTimeoutInSeconds, OutputStream socketOutputStream) {
        lock.lock();
        try {
            // Re-populate output socket stream when connected
            setSocketOutputStream(socketOutputStream);
            // Record the connection time
            timeoutChecker.started(keepAliveTimeoutInSeconds);
            // Notify connection is established
            transactionState.connect();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void disconnected() {
        lock.lock();
        try {
            // Notify connection is destroyed
            transactionState.disconnect();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addInboundMessage(byte[] rawInbound) {
        lock.lock();
        try {
            if (rawInbound == null) {
                logError(
                        "An inbound message is null");
                return false;
            } else if (rawInbound != null && rawInbound.length < PLCConstants.MSG_HEADER_LEN) {
                logError(
                        "An inbound message seems to be too short: " + MessageUtil.encodeHexString(rawInbound));
                return false;
            }
            PLCMessageHeader header = processor.processReceivedPLCHeader(rawInbound, PLCConstants.MSG_HEADER_LEN);
            if (header == null) {
                // Socket handler's processReceivedBytes() already validates header before passing it to upper layer.
                // However, we are validating it here again.
                logError("An inbound message has an invalid header: " + MessageUtil.encodeHexString(rawInbound));
                return false;
            }

            boolean result = false;
            short inboundMessageType = (short) header.getMsgType();
            short inboundSequenceNumber = (short) header.getSeqNo();
            switch (inboundMessageType) {
            case PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT:
                // Process the received link start up message
                // Link start up ack is registered here
                result = processInboundLinkStartUpMessage(rawInbound, header);
                break;
            case PLCConstants.KEEPALIVE_MSG_TYPE:
                // Process the received keep alive message
                // Keep alive reply is registered here
                result = processInboundKeepAliveMessage(rawInbound, header);
                // Save the keep alive received time
                timeoutChecker.ticked();
                break;
            case PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE_INT:
            case PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE_INT:
            case PLCConstants.PLC_ITEM_STORED_MSG_TYPE_INT:
            case PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE_INT:
            case PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE_INT:
                // Process the received inbound request message
                result = processInboundRequestMessage(rawInbound, header, inboundMessageType);
                break;
            case PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_MOVE_ORDER_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_FLIGHT_DATA_UPDATE_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_FLUSH_REQUEST_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_BAG_DATA_UPDATE_ACK_MSG_TYPE_INT:
                // Process the received inbound ack message
                result = processInboundAckMessage(rawInbound, header, inboundMessageType, inboundSequenceNumber);
                break;
            default:
                result = true;
                logError("Unexpected inbound message, so will be ignored: " + MessageUtil.encodeHexString(rawInbound));
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
            byte[] raw = encoder.encode(convertedOutbound);
            if (raw == null) {
                logAllDebug("Failed to encode the outbound message to send: " + convertedOutbound);
                return false;
            }

            boolean result = false;
            ACPTransaction transaction;
            short outboundMessageType = getMessageType(raw);
            short outboundSequenceNumber = getSequenceNumber(raw);
            switch (outboundMessageType) {
            case PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT:
            case PLCConstants.KEEPALIVE_MSG_TYPE:
                // Special messages internally registered by transaction handler
                logAllDebug(
                        "This shouldn't be added here as it's already registered by transaction handler internally");
                result = true;
                break;
            case PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE_INT:
            case PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE_INT:
            case PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE_INT:
            case PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE_INT:
                // A new request message sent by AirflowWCS, so we don't need to find an existing request message
                transaction = new ACPTransaction(null, new Message(outboundMessageType, raw, convertedOutbound));
                result = list.add(transaction);
                if (result) {
                    logAllDebug("New outbound message to send out is saved: " + transaction.toString());
                }
                break;
            case PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT:
            case PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE_INT:
                // Ack reply, so we have to find a matching request message sent by ACP
                Iterator<ACPTransaction> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Transaction existingTransaction = iterator.next();
                    if (existingTransaction.isMatchingReceivedInbound(outboundMessageType, outboundSequenceNumber)) {
                        existingTransaction.setOutboundToSend(new Message(outboundMessageType, raw, convertedOutbound));
                        logAllDebug("New outbound message is saved to the existing inbound message: "
                                + existingTransaction.toString());
                        result = true;
                        break;
                    }
                }
                if (!result) {
                    logError(
                            "The existing inbound message that required the received ack message is not found, so will be ignored: "
                                    + convertedOutbound);
                }
                break;
            default:
                result = true;
                logError("Unexpected outbound message, so will be ignored: " + convertedOutbound);
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

            Iterator<ACPTransaction> iterator;

            // Clear completed transactions
            list.removeIf(transaction -> transaction.isCompleted());

            if (transactionState.shouldStartInitialisation()) {
                // Connected, so link start up should be sent to ACP

                // Prepare a new link start up message
                prepareOutboundLinkStartUpMessage();

                // Now send it out
                Optional<ACPTransaction> outboundLinkStartUp = list.stream()
                        .filter(transaction -> transaction.canSendRequest()
                                && transaction.getOutbound().getType() == PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT)
                        .findFirst();
                if (outboundLinkStartUp.isPresent()) {
                    Transaction transaction = outboundLinkStartUp.get();
                    // Sequence shouldn't be modified as this is for sending out a link start up message
                    if (transmit(transaction)) {
                        transaction.markOutboundAsSent();
                        transactionState.startInitialisation();
                    } else {
                        return false;
                    }
                }
            } else if (transactionState.canSendRequest()) {
                // Now it's allowed to process requests
                if (isPendingTransactionFound()) {
                    // If there's a pending request, send it again
                    Optional<ACPTransaction> pendingTransactionSearch = list.stream()
                            .filter(transaction -> transaction.isPending()).findFirst();
                    if (pendingTransactionSearch.isPresent()) {
                        Transaction transaction = pendingTransactionSearch.get();
                        // As this request was already sent out, don't change sequence number now
                        if (transmit(transaction)) {
                            transaction.markOutboundAsSent();
                            transactionState.startRequest();
                        } else {
                            return false;
                        }
                    }
                } else {
                    // If no pending request is found, send a new request for the first time
                    Optional<ACPTransaction> newTransactionSearch = list.stream()
                            .filter(transaction -> transaction.canSendRequest()).findFirst();
                    if (newTransactionSearch.isPresent()) {
                        Transaction transaction = newTransactionSearch.get();
                        // Set a new sequence number just before sending out a new request
                        transaction.setOutboundSequenceNumber(nextOutboundSequenceNumber());
                        if (transmit(transaction)) {
                            transaction.markOutboundAsSent();
                            transactionState.startRequest();
                        } else {
                            return false;
                        }
                    }
                }
            } else if (transactionState.shouldWaitForAck()) {
                // Outbound link start up or a request was sent out, so let's check if ack is timed out
                Optional<ACPTransaction> theLastPendingTransaction = getLastPendingRequest();
                if (theLastPendingTransaction.isPresent()) {
                    boolean ackTimedOut = theLastPendingTransaction.get().isAckTimedOut(ackTimeout);
                    if (ackTimedOut) {
                        return false;
                    }
                }
            }

            // Send out ack reply if allowed
            if (transactionState.canSendReply()) {
                iterator = list.iterator();
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
            }

            // Clear completed transactions
            list.removeIf(transaction -> transaction.isCompleted());

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
     * Search the last pending request
     * 
     * @return The last ACPTransaction pending for ack
     */
    private Optional<ACPTransaction> getLastPendingRequest() {
        return list.stream()
                .filter(transaction -> transaction.isPending() && transaction.latestSent() != null)
                .sorted(new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        if (t1.latestSent() != null && t2.latestSent() != null) {
                            return t1.latestSent().compareTo(t2.latestSent());
                        } else if (t1.latestSent() == null && t2.latestSent() != null) {
                            return -1;
                        } else if (t1.latestSent() != null && t2.latestSent() == null) {
                            return 1;
                        }
                        return 0;
                    }
                }.reversed()).findFirst();
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
     * Extract equipment id from byte array
     * 
     * @param raw the byte array
     * @return the equipment id
     */
    private int getEquipmentId(byte[] raw) {
        ByteBuffer buf = ByteBuffer.wrap(raw);
        buf.getShort();
        buf.getShort();
        buf.getShort();
        return buf.getInt(); // the 4th field in header
    }

    /**
     * Add a outbound link start up message with the current outbound sequence number to transaction list
     */
    private void prepareOutboundLinkStartUpMessage() {
        boolean result = false;

        // Remove existing outbound link start up message regardless of its status if found
        list.removeIf(transaction -> transaction.getOutbound() != null
                && transaction.getOutbound().getType() == PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT);

        // Create a new outbound link start up message
        ACPTransaction transaction = new ACPTransaction(null, new Message(
                Short.valueOf(currentOutboundSequenceNumber()), PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT,
                processor.createPLCLinkStartUpMessage(this.equipmentId, currentOutboundSequenceNumber()), ""));
        result = list.add(transaction);
        if (result) {
            logAllDebug("A new outbound link start up message is registered: " + transaction);
        }
    }

    /**
     * Process the inbound link start up message
     * 
     * @param rawInbound the raw message
     * @param header the decoded header
     * @return true if ok, false if not
     */
    private boolean processInboundLinkStartUpMessage(byte[] rawInbound, PLCMessageHeader header) {
        // If link start up is received, just update the saved previous inbound sequence number
        boolean result = false;
        ACPTransaction transaction = new ACPTransaction(new Message(Short.valueOf((short) header.getSeqNo()),
                PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT, rawInbound,
                processor.decodeReceivedData(rawInbound, header)), null);

        prevInboundSequenceNumber = (short) header.getSeqNo();

        // Link start up ack should use the sequence number of inbound link start up message just like other ack
        // messages
        byte[] rawOutbound = processor.createPLCLinkStartupAckMessage((short) header.getSeqNo(),
                Integer.valueOf(header.getEquipmentID()));
        transaction.setOutboundToSend(
                new Message(Short.valueOf((short) 0), PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT, rawOutbound,
                        null));
        result = list.add(transaction);
        if (result) {
            logAllDebug("Inbound link start up message is received, so saved sequence number is updated to "
                    + prevInboundSequenceNumber);
        }
        return result;
    }

    /**
     * Process the inbound keep alive message
     * 
     * @param rawInbound the raw message
     * @param header the decoded header
     * @return true if ok, false if not
     */
    private boolean processInboundKeepAliveMessage(byte[] rawInbound, PLCMessageHeader header) {
        // If keep alive message is received, keep alive reply needs to be registered here now.
        // Other reply will be registered somewhere else.
        boolean result = false;
        ACPTransaction transaction = new ACPTransaction(new Message(Short.valueOf((short) header.getSeqNo()),
                PLCConstants.KEEPALIVE_MSG_TYPE, rawInbound, processor.decodeReceivedData(rawInbound, header)), null);

        // Keep alive message will not use sequence number, so sending 0 always here.
        byte[] rawOutbound = processor.createPLCKeepAliveResponseMessage(this.equipmentId);
        transaction.setOutboundToSend(
                new Message(Short.valueOf((short) 0), PLCConstants.KEEPALIVE_MSG_TYPE, rawOutbound, null));
        result = list.add(transaction);
        if (result) {
            logAllDebug("A new inbound message is registered: " + transaction);
        }
        return result;
    }

    /**
     * Process the inbound request message
     * 
     * @param rawInbound the raw message
     * @param header the decoded header
     * @param inboundMessageType the message type of the inbound message
     * @return true if ok, false if not
     */
    private boolean processInboundRequestMessage(byte[] rawInbound, PLCMessageHeader header, short inboundMessageType) {
        // ACP sent a request message and ack reply will be set later
        boolean result = false;
        ACPTransaction transaction = new ACPTransaction(
                new Message(Short.valueOf((short) header.getSeqNo()), Short.valueOf(prevInboundSequenceNumber),
                        inboundMessageType, rawInbound, processor.decodeReceivedData(rawInbound, header)),
                null);

        // Now publish the event for the inbound message
        publishInboundMessage(transaction.getInbound().getConverted());

        // Register to the list
        result = list.add(transaction);
        if (result) {
            logAllDebug("A new inbound message is registered: " + transaction);
        }

        // Save the sequence number of the received request message
        prevInboundSequenceNumber = (short) header.getSeqNo();

        return result;
    }

    /**
     * Process the inbound ack message
     * 
     * @param rawInbound the raw message
     * @param header the decoded header
     * @param inboundMessageType the message type of the inbound message
     * @param inboundSequenceNumber the sequence number of the inbound message
     * @return true if ok, false if not
     */
    private boolean processInboundAckMessage(byte[] rawInbound, PLCMessageHeader header, short inboundMessageType,
            short inboundSequenceNumber) {
        // ACP sent an ack, so we have to find a matching sent outbound message sent by AirflowWCS
        boolean result = false;
        
        Iterator<ACPTransaction> iterator = list.iterator();
        while (iterator.hasNext()) {
            Transaction existingTransaction = iterator.next();
            if (existingTransaction.isMatchingSentOutbound(inboundMessageType, inboundSequenceNumber)) {
                existingTransaction.setReceivedInbound(
                        new Message(Short.valueOf((short) header.getSeqNo()), inboundMessageType, rawInbound,
                                processor.decodeReceivedData(rawInbound, header)));

                logAllDebug("A new inbound message is saved to the existing matching sent outbound message: "
                        + existingTransaction);

                // Now publish the event for the inbound message
                publishInboundMessage(existingTransaction.getInbound().getConverted());

                if (inboundMessageType == PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT) {
                    // Notify outbound link start up is acked
                    transactionState.finishInitialisation();
                } else {
                    // Notify outbound request is acked
                    transactionState.finishRequest();
                }

                result = true;
                break;
            }
        }
        if (!result) {
            logError(
                    "The existing outbound message that required the received ack message is not found, so will be ignored: "
                            + MessageUtil.encodeHexString(rawInbound));
        }
        return result;
    }

    /**
     * Publish the received inbound message as an equipment event
     * 
     * @param converted the CSV string converted from the received byte array
     */
    private void publishInboundMessage(String converted) {
        messageService.publishEvent(portName, WRX_RUN_MODE, converted, 0, MessageEventConsts.EQUIPMENT_EVENT_TYPE,
                MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + portName);
    }

    private short nextOutboundSequenceNumber() {
        if (outboundSequenceNumber >= Short.MAX_VALUE || outboundSequenceNumber < 0) {
            outboundSequenceNumber = 0;
        }
        this.outboundSequenceNumber = (short) (outboundSequenceNumber + 1);
        return outboundSequenceNumber;
    }

    /**
     * Get the current sequence number for a link start up message
     * 
     * @return the current sequence number
     */
    private short currentOutboundSequenceNumber() {
        return outboundSequenceNumber;
    }

    /**
     * Check if there's a pending transaction in the transaction list
     * 
     * @return true if found, false if not
     */
    private boolean isPendingTransactionFound() {
        return list.stream().anyMatch(transaction -> transaction.isPending());
    }

    /**
     * Transmit the transaction to ACP
     * 
     * @param transaction the transaction to transmit
     * @return true if transmission is successfully done, false if not
     */
    private boolean transmit(Transaction transaction) {
        boolean sent = false;

        try {
            byte[] outboundMessage = transaction.getOutbound().getRaw();
           
            if (transaction.shouldValidateInboundSequenceNumber() 
            		&& transaction.getOutbound().getType() != PLCConstants.KEEPALIVE_MSG_TYPE  ) {
            	
                Short previousSequenceNumber = transaction.getInbound().getPrevSequenceNumber();
                Short currentSequenceNumber = transaction.getInbound().getSequenceNumber();
                // Set the ack status flag value
                // - if the sequence number is valid
                setAckStatusFlag(outboundMessage, isValidSequenceNumber(previousSequenceNumber, currentSequenceNumber));
            }

            byte[] messageToSend = wrapOutboundMessageIfRequired(outboundMessage, useSTXETX);
            logAllDebug("Outbound message will be sent out for " + transaction);
            logAllDebug("Outgoing message written to " + portName + ":"
                    + MessageUtil.encodeHexString(messageToSend));
            logger.logTxByteCommunication(messageToSend, 0, messageToSend.length);
            socketOutputStream.write(messageToSend);
            socketOutputStream.flush();

            sent = true;
        } catch (IOException e) {
            logError("Failed to write to " + portName + " due to " + e.getMessage());
            sent = false;
        }

        return sent;
    }

    /**
     * Validate the received inbound sequence number
     * 
     * @param previousSequenceNumber the previously received sequence number
     * @param currentSequenceNumber the current sequence number to validate
     * @return true if valid, false if not
     */
    private boolean isValidSequenceNumber(Short previousSequenceNumber, Short currentSequenceNumber) {
        if (previousSequenceNumber != null) {
            // If there was a previous request message, let's validate the current sequence number to it
            short nextSeqNumCalculatedFromPreviousSeqNum = previousSequenceNumber;
            if (nextSeqNumCalculatedFromPreviousSeqNum >= Short.MAX_VALUE
                    || nextSeqNumCalculatedFromPreviousSeqNum < 0) {
                nextSeqNumCalculatedFromPreviousSeqNum = 0;
            }
            nextSeqNumCalculatedFromPreviousSeqNum = (short) (nextSeqNumCalculatedFromPreviousSeqNum + 1);
            // Check if the current sequence number == next value of prev sequence number( N + 1 or 1)
            if (nextSeqNumCalculatedFromPreviousSeqNum == currentSequenceNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the ack status flag in the message to be sent
     * 
     * @param outboundRawMessage the raw outbound message to be modified
     * @param validSequenceNumber true if sequence number is valid, false if not
     */
    private void setAckStatusFlag(byte[] outboundRawMessage, boolean validSequenceNumber) {
        short ackStatusFlagValue = PLCConstants.AckStatus.OK.getValue();
        if (!validSequenceNumber) {
            ackStatusFlagValue = PLCConstants.AckStatus.SEQUENCE_NUMBER_ERROR.getValue();
        }
        ByteBuffer buf = ByteBuffer.allocate(PLCConstants.MSG_WORD_LEN);
        buf.putShort(ackStatusFlagValue);
        byte[] ackStatusFlagValueArray = buf.array();
        System.arraycopy(ackStatusFlagValueArray, 0, outboundRawMessage, PLCConstants.MSG_HEADER_LEN,
                ackStatusFlagValueArray.length);
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

    /*
     * The below 5 methods are copied from SocketComPort.java to provide the similar logging
     */
    private void logError(String s) {
        logger.logError(s);
        logTxDebug("**** " + s + " ****");
    }

    private void logTxDebug(String s) {
        logger.logTxByteCommunication(s.getBytes(), 0, s.length());
    }

    private void logSocketError(String s) {
        logError("  # SOCKET #  " + s);
    }

    private void logSocketDebug(String s) {
        logger.logDebug("  # SOCKET #  " + s);
    }

    private void logAllDebug(String s) {
        logSocketDebug(s);
        logTxDebug(s);
    }
}
