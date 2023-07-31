package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import com.daifukuoc.wrxj.custom.ebs.communication.Message;
import com.daifukuoc.wrxj.custom.ebs.communication.Transaction;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionType;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

/**
 * The transaction that takes care of a pair of inbound/outbound message
 * 
 * @author LK
 *
 */
public class ACPTransaction extends Transaction {
    public ACPTransaction(Message inbound, Message outbound) {
        super(inbound, outbound);
    }

    @Override
    protected TransactionType determineInboundTransactionType(Message message) {
        TransactionType transactionType = TransactionType.UNKNOWN;
        switch (message.getType()) {
        case PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT:
        case PLCConstants.KEEPALIVE_MSG_TYPE:
        case PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE_INT:
        case PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE_INT:
        case PLCConstants.PLC_ITEM_STORED_MSG_TYPE_INT:
        case PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE_INT:
        case PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE_INT:
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
        if (outboundMessageType == PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT &&
                inboundMessageType == PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT) {
            return true;
        } else if (outboundMessageType == PLCConstants.KEEPALIVE_MSG_TYPE &&
                inboundMessageType == PLCConstants.KEEPALIVE_MSG_TYPE) {
            return true;
        } else if (outboundMessageType == PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE_INT &&
                inboundMessageType == PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE_INT) {
            return true;
        } else if (outboundMessageType == PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE_INT &&
                inboundMessageType == PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE_INT) {
            return true;
        } else if (outboundMessageType == PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE_INT &&
                inboundMessageType == PLCConstants.PLC_ITEM_STORED_MSG_TYPE_INT) {
            return true;
        } else if (outboundMessageType == PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT &&
                inboundMessageType == PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE_INT) {
            return true;
        } else if (outboundMessageType == PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE_INT &&
                inboundMessageType == PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE_INT) {
            return true;
        }
        return false;
    }

    @Override
    protected TransactionType determineOutboundTransactionType(Message message) {
        TransactionType transactionType = TransactionType.UNKNOWN;
        switch (message.getType()) {
        case PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT:
        case PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE_INT:
        case PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE_INT:
        case PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE_INT:
        case PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE_INT:
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
        if (inboundMessageType == PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT &&
                outboundMessageType == PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT) {
            return true;
        } else if (inboundMessageType == PLCConstants.PLC_MOVE_ORDER_ACK_MSG_TYPE_INT &&
                outboundMessageType == PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE_INT) {
            return true;
        } else if (inboundMessageType == PLCConstants.PLC_FLIGHT_DATA_UPDATE_ACK_MSG_TYPE_INT &&
                outboundMessageType == PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE_INT) {
            return true;
        } else if (inboundMessageType == PLCConstants.PLC_FLUSH_REQUEST_ACK_MSG_TYPE_INT &&
                outboundMessageType == PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE_INT) {
            return true;
        } else if (inboundMessageType == PLCConstants.PLC_BAG_DATA_UPDATE_ACK_MSG_TYPE_INT &&
                outboundMessageType == PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE_INT) {
            return true;
        }
        return false;
    }
}
