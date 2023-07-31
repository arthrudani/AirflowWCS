package com.daifukuoc.wrxj.custom.ebs.communication;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

public abstract class Transaction {

    private TransactionType transactionType;
    private Message inbound;
    private Message outbound;
    private LocalDateTime received;
    private List<LocalDateTime> sent = new ArrayList<>();

    public Transaction(Message inbound, Message outbound) {
        if (inbound != null && outbound != null) {
            throw new IllegalStateException(
                    "Transaction can be created for either inbound or outbound and inbound/outbound messages are both not null");
        } else if (inbound == null && outbound == null) {
            throw new IllegalStateException(
                    "Transaction can be created for either inbound or outbound and inbound/outbound messages are both null");
        } else if (inbound != null && outbound == null) {
            // Expected sequence of methods for a transaction started with inbound message
            // - A new inbound request message is received: new Transaction(inbound, null)
            // - A new ack for the received inbound request message is registered: setOutboundToSend(outbound)
            // - The ack is sent out: markOutboundAsSent()
            // - Now the transaction is completed, so process() of handler will remove it
            this.inbound = inbound;
            markAsReceived();
            transactionType = determineInboundTransactionType(inbound);
            if (transactionType.equals(TransactionType.UNKNOWN)) {
                throw new IllegalStateException(
                        "Transaction is unknown, so can't register it");
            }
        } else if (inbound == null && outbound != null) {
            // Expected sequence of methods for a transaction started with outbound message
            // - A new outbound request message is registered: new Transaction(null, outbound)
            // - The sequence number is determined and set before sending out: setOutboundSequenceNumber(int
            // sequenceNumber)
            // - The request is sent out: markOutboundAsSent()
            // - The ack for the outbound request message is received: setReceivedInbound(inbound)
            // - Now the transaction is completed, so process() of handler will remove it
            this.outbound = outbound;
            transactionType = determineOutboundTransactionType(outbound);
            if (transactionType.equals(TransactionType.UNKNOWN)) {
                throw new IllegalStateException(
                        "Transaction is unknown, so can't register it");
            }
        }
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Message getInbound() {
        return inbound;
    }

    public Message getOutbound() {
        return outbound;
    }

    public void setOutboundToSend(Message outbound) {
        if (inbound == null) {
            throw new IllegalStateException(
                    "No inbound message is registered to reply, so you can't set outbound message to reply");
        } else if (this.outbound != null) {
            throw new IllegalStateException("Outbound message is already registered");
        }
        this.outbound = outbound;
    }

    public void setOutboundSequenceNumber(short outboundSequenceNumber) {
        if (outbound == null) {
            throw new IllegalStateException("No outbound message is registered, so you can't set sequence number");
        }
        if (outbound.getSequenceNumber() != null) {
            throw new IllegalStateException("The outbound message already has the sequence number");
        }

        outbound.setSequenceNumber(Short.valueOf(outboundSequenceNumber));

        // Replace sequence number in outbound raw message
        // FIXME: Move this to somewhere as static method?
        ByteBuffer buf = ByteBuffer.allocate(PLCConstants.MSG_WORD_LEN);
        buf.putShort(outboundSequenceNumber);
        byte[] sequenceNumberArray = buf.array();
        System.arraycopy(sequenceNumberArray, 0, outbound.getRaw(), 2, sequenceNumberArray.length);
    }

    public void markOutboundAsSent() {
        if (outbound == null) {
            throw new IllegalStateException("No outbound message is registered, so you can't mark it as sent");
        }
        markAsSent();
    }

    public void setReceivedInbound(Message inbound) {
        if (this.inbound == null) {
            this.inbound = inbound;
            markAsReceived();
        } else {
            throw new IllegalStateException("The inbound message is already registered");
        }
    }

    /**
     * Check if the existing transaction is for the received inbound message type and sequence number
     * 
     * @param inboundMessageType the message type of the inbound message
     * @param inboundSequenceNumber the sequence number of the inbound message
     * @return true if matched, false if not
     */
    public boolean isMatchingSentOutbound(short inboundMessageType, short inboundSequenceNumber) {
        if (outbound != null &&
                outbound.getSequenceNumber() != null &&
                outbound.getSequenceNumber().shortValue() == inboundSequenceNumber &&
                isSent()) {
            if (isMatchingOutbound(inboundMessageType, outbound.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the existing transaction is for the sent outbound message type and sequence number
     * 
     * @param outboundMessageType the message type of the outbound message
     * @param outboundSequenceNumber the sequence number of the outbound message
     * @return true if matched, false if not
     */
    public boolean isMatchingReceivedInbound(short outboundMessageType, short outboundSequenceNumber) {
        if (inbound != null &&
                inbound.getSequenceNumber() != null &&
                inbound.getSequenceNumber().shortValue() == outboundSequenceNumber &&
                isReceived()) {
            if (isMatchingInbound(outboundMessageType, inbound.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current transaction is completed
     * 
     * @return true if completed, false if not
     */
    public boolean isCompleted() {
        return inbound != null &&
                outbound != null &&
                isSent() &&
                isReceived();
    }

    /**
     * Check if the current transaction require a reply to be sent
     * 
     * @return true if we should send a reply, false if not
     */
    public boolean shouldSendReply() {
        return transactionType.equals(TransactionType.RECEIVED_REQUEST_THAT_WCS_SHOULD_ACK) &&
                inbound != null &&
                outbound != null &&
                !isSent() &&
                isReceived();
    }

    /**
     * Check if the current transaction require sequence number validation
     * 
     * @return true if we should validate the received sequence number, false if not
     */
    public boolean shouldValidateInboundSequenceNumber() {
        return shouldSendReply() &&
                transactionType.equals(TransactionType.RECEIVED_REQUEST_THAT_WCS_SHOULD_ACK);
    }

    /**
     * Check if the current transaction is waiting for a reply
     * 
     * @return true if pending, false if not
     */
    public boolean isPending() {
        return transactionType.equals(TransactionType.REQUEST_TO_SEND_THAT_SHOULD_BE_ACKED) &&
                inbound == null &&
                outbound != null &&
                isSent() &&
                !isReceived();
    }

    /**
     * Check if ack is not received within timeout for the pending request
     * 
     * @param timeoutInSeconds ack timeout
     * @return true if it's timed out
     */
    public boolean isAckTimedOut(long timeoutInSeconds) {
        // Waiting for an ack
        // Ack is not received within timeout
        if (isPending()) {
            LocalDateTime latestSent = latestSent();
            if (latestSent != null) {
                LocalDateTime timeout = latestSent.plusSeconds(timeoutInSeconds);
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(timeout)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the pending request can continue to wait for ack
     * 
     * @param timeoutInSeconds ack timeout
     * @param maxRetry ack max retry
     * @return true if it's allowed to wait for ack, false if not
     */
    public boolean isAckTimedOutButCanRetry(long timeoutInSeconds, int maxRetry) {
        // Number of transmission <= max retry
        // Waiting for an ack
        // Ack is not received within timeout
        return numberOfSent() <= maxRetry && isAckTimedOut(timeoutInSeconds);
    }

    /**
     * Check if the pending request can't continue to wait for ack
     * 
     * @param timeoutInSeconds ack timeout
     * @param maxRetry ack max retry
     * @return true if it's not allowed to wait for ack, false if not
     */
    public boolean isAckTimedOutAndNoMoreRetryAllowed(long timeoutInSeconds, int maxRetry) {
        // Number of transmission > max retry
        // Waiting for an ack
        // Ack is not received within timeout
        return numberOfSent() > maxRetry && isAckTimedOut(timeoutInSeconds);
    }

    /**
     * Check if we can send the current transaction
     * 
     * @return true if we can send, false if not
     */
    public boolean canSendRequest() {
        return transactionType.equals(TransactionType.REQUEST_TO_SEND_THAT_SHOULD_BE_ACKED) &&
                inbound == null &&
                outbound != null &&
                !isSent() &&
                !isReceived();
    }

    public boolean isReceived() {
        return received != null;
    }

    public void markAsReceived() {
        received = LocalDateTime.now();
    }

    public boolean isSent() {
        return !sent.isEmpty();
    }

    public LocalDateTime latestSent() {
        return !sent.isEmpty() ? sent.get(sent.size() - 1) : null;
    }

    public int numberOfSent() {
        return sent.size();
    }

    public void markAsSent() {
        LocalDateTime now = LocalDateTime.now();
        sent.add(now);
    }

    /**
     * Determines inbound transaction type based on message type. This is only called when a new transaction object is
     * created.
     * 
     * @param message inbound message
     * @return transaction type determined with the given message
     */
    protected abstract TransactionType determineInboundTransactionType(Message message);

    /**
     * Check if the existing transaction is for the sent outbound message type
     * 
     * @param outboundMessageType outbound message type
     * @param inboundMessageType intbound message type
     * @return true when matching, false if not
     */
    protected abstract boolean isMatchingInbound(short outboundMessageType, short inboundMessageType);

    /**
     * Determines outbound transaction type based on message type. This is only called when a new transaction object is
     * created.
     * 
     * @param message outbound message
     * @return transaction type determined with the given message
     */
    protected abstract TransactionType determineOutboundTransactionType(Message message);

    /**
     * Check if the existing transaction is for the received inbound message type
     * 
     * @param inboundMessageType intbound message type
     * @param outboundMessageType outbound message type
     * @returnm true when matching, false if not
     */
    protected abstract boolean isMatchingOutbound(short inboundMessageType, short outboundMessageType);
    
    @Override
    public String toString() {
        return "Transaction [transactionType=" + transactionType + ", inbound=" + inbound + ", outbound=" + outbound
                + ", received=" + received + ", sent=" + sent + "]";
    }
}
