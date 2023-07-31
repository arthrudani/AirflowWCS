package com.daifukuoc.wrxj.custom.ebs.communication;

/**
 * A list of transaction types
 * 
 * @author LK
 *
 */
public enum TransactionType {
    RECEIVED_REQUEST_THAT_WCS_SHOULD_ACK,
    REQUEST_TO_SEND_THAT_SHOULD_BE_ACKED,
    UNKNOWN
}