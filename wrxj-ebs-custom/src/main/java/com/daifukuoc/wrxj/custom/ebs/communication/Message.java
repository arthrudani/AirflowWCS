package com.daifukuoc.wrxj.custom.ebs.communication;

import java.util.Arrays;

/**
 * This holds the detail of inbound or outbound message.
 * 
 * @author LK
 *
 */
public class Message {
    private Short sequenceNumber;
    // Only used for request messages for sequence number validation
    private Short prevSequenceNumber;
    private short type;
    private byte[] raw;
    private String converted;

    /**
     * A constructor for inbound messages without the previous sequence number
     * 
     * @param sequenceNumber The sequence number populated from the inbound message
     * @param type The message type
     * @param raw The byte array of the received inbound message
     * @param converted The string representation of outbound message passed from other part of AirflowWCS
     */
    public Message(Short sequenceNumber, short type, byte[] raw, String converted) {
        this(sequenceNumber, null, type, raw, converted);
    }
    
    /**
     * A constructor for inbound messages without the previous sequence number
     * 
     * @param sequenceNumber The sequence number populated from the inbound message
     * @param prevSequenceNumber The sequence number of the previous request message
     * @param type The message type
     * @param raw The byte array of the received inbound message
     * @param converted The string representation of outbound message passed from other part of AirflowWCS
     */
    public Message(Short sequenceNumber, Short prevSequenceNumber, short type, byte[] raw, String converted) {
        this.sequenceNumber = sequenceNumber;
        this.prevSequenceNumber = prevSequenceNumber;
        this.type = type;
        this.raw = raw;
        this.converted = converted;
    }

    /**
     * A constructor for outbound message
     * - Outbound message that doesn't have sequence number when registered as it should be allocated(including raw message) when sent out
     * 
     * @param type The message type
     * @param raw The byte array to be sent out encoded from converted
     * @param converted The string representation of outbound message passed from other part of AirflowWCS
     */
    public Message(short type, byte[] raw, String converted) {
        this(null, type, raw, converted);
    }

    public Short getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Short sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public Short getPrevSequenceNumber() {
        return prevSequenceNumber;
    }

    public short getType() {
        return type;
    }

    public byte[] getRaw() {
        return raw;
    }

    public String getConverted() {
        return converted;
    }

    @Override
    public String toString() {
        return "Message [sequenceNumber=" + sequenceNumber + ", prevSequenceNumber=" + prevSequenceNumber + ", type="
                + type + ", raw=" + Arrays.toString(raw) + ", converted=" + converted + "]";
    }
}
