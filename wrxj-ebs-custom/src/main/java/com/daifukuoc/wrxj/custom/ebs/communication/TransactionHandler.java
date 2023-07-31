package com.daifukuoc.wrxj.custom.ebs.communication;

import java.io.OutputStream;

public interface TransactionHandler {

    /**
     * Do a few things when a connection is established
     * 
     * @param keepAliveTimeoutInSeconds the keep alive timeout in second
     * @param socketOutputStream the output stream from the established socket connection
     */
    void connected(long keepAliveTimeoutInSeconds, OutputStream socketOutputStream);
    
    /**
     * Do a few things when a connection is destroyed
     */
    void disconnected();

    /**
     * Add a new inbound message into the transaction list
     * 
     * @param rawInbound the byte array of the received message
     * @return true if adding is done, false if not
     */
    boolean addInboundMessage(byte[] rawInbound);

    /**
     * Add a new outbound message into the transaction list
     * 
     * @param convertedOutbound the CSV string built by other part of AirflowWCs
     * @return true if adding is done, false if not
     */
    boolean addOutboundMessage(String convertedOutbound);

    /**
     * Process the current transaction list
     * 
     * @return true if processing is done successfully, false if not
     */
    boolean process();

    /**
     * Check if keep alive is timed out
     * 
     * @return true if timed out, false if not
     */
    boolean isKeepAliveTimedOut();

    /**
     * Return the number of transactions
     * 
     * @return the size of current transaction list
     */
    int size();
}
