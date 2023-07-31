package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

/**
 * Interface for the PLC Inbound message type.
 * 
 * @author Administrator
 *
 */
public abstract class StandardInboundMessage {

    protected Logger logger = Logger.getLogger();
    private String sDeviceID = "";
    private String sSerialNumber = "0";

    /**
     * Parse and populates each value to the instance field with the given receivedText
     * 
     * @param receivedText CSV formed encoded inboud message
     * @return true : success / false : failure
     */
    public abstract boolean parse(String receivedText);

    /**
     * This method is used to do the business process for the incoming message received.
     * 
     * @return The context instance used in the method, it may contain new values for specific records. Null in case of
     *         error.
     */
    public abstract EBSTransactionContext process();

    /**
     * Sends back Ack message to client.
     */
    public abstract void processAck();

    /**
     * Write out transaction log. As it depends on message if writes out transaction log, this class keeps an empty
     * implementation to avoid unnecessary implementation.
     * 
     * @param ebsTransactionContext context instance for log message. Extra information cangather from other places.
     */
    public void outputTransactionLog(EBSTransactionContext ebsTransactionContext) {
    }

    /**
     * This method is used to validate the parsing is successfully and converted to the actual inbound message object.
     * 
     * @return
     * @throws DBException
     */
    public abstract boolean isValid() throws DBException;

    public void setDeviceId(String deviceId) {
        sDeviceID = deviceId;
    }

    public String getDeviceId() {
        return sDeviceID;
    }

    public void setSerialNumber(String serialNumber) {
        sSerialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return sSerialNumber;
    }
}
