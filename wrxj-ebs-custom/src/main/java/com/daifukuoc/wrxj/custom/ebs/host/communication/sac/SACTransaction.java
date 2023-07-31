package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import com.daifukuoc.wrxj.custom.ebs.communication.Message;
import com.daifukuoc.wrxj.custom.ebs.communication.Transaction;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionType;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * The transaction that takes care of a pair of inbound/outbound message
 * 
 * @author LK
 *
 */
public class SACTransaction extends Transaction {
    public SACTransaction(Message inbound, Message outbound) {
        super(inbound, outbound);
    }

    @Override
    protected TransactionType determineInboundTransactionType(Message message) {
        TransactionType transactionType = TransactionType.UNKNOWN;
        switch (message.getType()) {
        case SACControlMessage.KEEPALIVE_MSG_TYPE:
        case SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE:
        case SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE:
        case SACControlMessage.INVENTORY_UPDATE_MSG_TYPE:
        case SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE:
        case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE:
        case SACControlMessage.LINK_STARTUP_MSG_TYPE_INT:
            transactionType = TransactionType.RECEIVED_REQUEST_THAT_WCS_SHOULD_ACK;
            break;
        default:
            transactionType = TransactionType.UNKNOWN;
            break;
        }
        return transactionType;
    }

    @Override
    protected boolean isMatchingInbound(short outboundMessageType, short inboundMessageType) {
        if (outboundMessageType == PLCConstants.KEEPALIVE_MSG_TYPE &&
                inboundMessageType == PLCConstants.KEEPALIVE_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_TYPE &&
                inboundMessageType == SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_TYPE &&
                inboundMessageType == SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE &&
                inboundMessageType == SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE &&
                inboundMessageType == SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == SACControlMessage.INVENTORY_UPDATE_ACK_MSG_TYPE &&
                inboundMessageType == SACControlMessage.INVENTORY_UPDATE_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE &&
                inboundMessageType == SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE &&
                inboundMessageType == SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE) {
        	return true;
        }else if (outboundMessageType == SACControlMessage.LINK_STARTUP_ACK_MSG_TYPE_INT &&
                inboundMessageType == SACControlMessage.LINK_STARTUP_MSG_TYPE_INT) {
            return true;
        }
        

        return false;
    }

    @Override
    protected TransactionType determineOutboundTransactionType(Message message) {
        TransactionType transactionType = TransactionType.UNKNOWN;
        switch (message.getType()) {
        case SACControlMessage.StoreCompletionNotify.MSG_TYPE:
        case SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE:
        case SACControlMessage.INVENTORY_RESPONSE_MSG_TYPE:
        case SACControlMessage.ITEM_RELEASE_MSG_TYPE:
        case SACControlMessage.LINK_STARTUP_MSG_TYPE_INT:
            transactionType = TransactionType.REQUEST_TO_SEND_THAT_SHOULD_BE_ACKED;
            break;
        default:
            transactionType = TransactionType.UNKNOWN;
            break;
        }
        return transactionType;
    }

    @Override
    protected boolean isMatchingOutbound(short inboundMessageType, short outboundMessageType) {
        if (inboundMessageType == SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE &&
                outboundMessageType == SACControlMessage.StoreCompletionNotify.MSG_TYPE) {
            return true;
        } else if (inboundMessageType == SACControlMessage.EXPECTED_RECIEPT_RESPONSE_ACK_MSG_TYPE &&
                outboundMessageType == SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE) {
            return true;
        } else if (inboundMessageType == SACControlMessage.ITEM_RELEASE_ACK_MSG_TYPE &&
                outboundMessageType == SACControlMessage.ITEM_RELEASE_MSG_TYPE) {
            return true;
        } else if (inboundMessageType == SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_TYPE &&
                outboundMessageType == SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE) {
            return true;
        } else if (inboundMessageType == SACControlMessage.RETRIEVAL_ITEM_RESPONSE_ACK_MSG_TYPE &&
            outboundMessageType == SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE) {
        	return true;
        } else if (inboundMessageType == SACControlMessage.INVENTORY_RESPONSE_ACK_MSG_TYPE &&
                outboundMessageType == SACControlMessage.INVENTORY_RESPONSE_MSG_TYPE) {
            	return true;
        }else if (inboundMessageType == SACControlMessage.LINK_STARTUP_ACK_MSG_TYPE_INT &&
        		outboundMessageType == SACControlMessage.LINK_STARTUP_MSG_TYPE_INT) {
            return true;
        }
        return false;
    }

}
