package com.daifukuoc.wrxj.custom.ebs.plc.messages;

import java.nio.ByteBuffer;

import com.daifukuamerica.wrxj.device.port.PortConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.util.PLCMessageUtil;

/**
 * Base Message builder class for in-bound and out-bound message. It contains all common functionalities.
 * 
 * @author KR
 *
 */
public class PLCBaseMsgBuilder {

    protected int mnTransmittedSequenceNumber = 0;
    protected int mnSequenceNumberLength = 4;
    protected int mnSequenceNumber = 1;
    protected int mnMaxSequenceNumber = (int) Math.pow(10, mnSequenceNumberLength) - 1;

    protected String msSeparator = ",";
    protected int mnExpectedReceivedSequenceNumber = 0;
    protected Logger logger = Logger.getLogger();
    protected boolean izEmulation = false; // determines if set to Emulation mode

    public PLCBaseMsgBuilder() {

    }

    /* protected methods */

    protected String getTransmittedSequenceNumber() {
        String sResult = "" + mnTransmittedSequenceNumber;
//      while (sResult.length() < mnSequenceNumberLength)
//      {
//        sResult = "0" + sResult;
//      }

        mnTransmittedSequenceNumber++;
        if (mnTransmittedSequenceNumber > mnMaxSequenceNumber + 1) {
            mnTransmittedSequenceNumber = mnSequenceNumber;
            // mnTransmittedSequenceNumber = 0;
            sResult = "" + mnTransmittedSequenceNumber;
            mnTransmittedSequenceNumber++;
        }
        return sResult;
    }

    /**
     * Set the length of the sequence number
     * 
     * @param inBytes (valid range is 1 - 6)
     */
    public void setSequenceLength(int inBytes, int maxSeqNum) {

        if (inBytes < 1 || inBytes > 6) {
            throw new IllegalArgumentException("Valid range is 1 - 6 (" + inBytes + ")");
        }
        mnSequenceNumberLength = inBytes;
        mnMaxSequenceNumber = maxSeqNum;
    }

    /**
     * Validate the message header
     * 
     * @param mpBCSMessageHeader
     * @return True if valid. false otherwise.
     */
    protected boolean isMsgHeaderValid(PLCMessageHeader mpBCSMessageHeader) {

        boolean vzRtnVal = true;

        if (!isHeaderMsgTypeValid(mpBCSMessageHeader.getMsgType())) {
            logger.logError("Invalid BCS Message Header Type - " + mpBCSMessageHeader.getMsgType());
            return false;
        }

        if (mpBCSMessageHeader.getMsgLength() <= 0) {
            logger.logError("Invalid BCS Message Header message length 0");
            return false;
        }

        // FOR TESTING WITH EMULATION
        // keepAlive's don't have a seqno to validate from emualtion
        // MCM TESTING
        if (izEmulation) {
            return vzRtnVal;
        }

        return vzRtnVal;
    }

    /**
     * Validating incoming messages types
     * 
     * @param inMsgType
     * @return
     */
    protected boolean isHeaderMsgTypeValid(int inMsgType) {

        switch (inMsgType) {
        case PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT:
        case PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT:
        case PLCConstants.KEEPALIVE_MSG_TYPE:
        case PLCConstants.PLC_ITEM_STORED_MSG_TYPE_INT:
        case PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE_INT:
        case PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE_INT:
        case PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE_INT:
        case PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE_INT:
        case PLCConstants.PLC_MOVE_ORDER_ACK_MSG_TYPE_INT:
        case PLCConstants.PLC_FLUSH_REQUEST_ACK_MSG_TYPE_INT:
        case PLCConstants.PLC_FLIGHT_DATA_UPDATE_ACK_MSG_TYPE_INT:
        case PLCConstants.PLC_BAG_DATA_UPDATE_ACK_MSG_TYPE_INT:
            return true;
        default:
            return false;
        }
    }

    protected boolean isHeaderSeqNotValid(int inMsgSeqNo) {
        // TODO: if need to sortout ....

        // if( inMsgSeqNo == mnExpectedReceivedSequenceNumber )
        // {
        // mnExpectedReceivedSequenceNumber++;
        // // check for rollover
        // if (mnExpectedReceivedSequenceNumber > mnMaxSequenceNumber)
        // {
        // mnExpectedReceivedSequenceNumber = 0;
        // }
        // return true;
        // }
        // return false;

        return true;
    }

    /*
     * Process message header received from PLC
     */
    public PLCMessageHeader processReceivedPLCHeader(byte[] inputProtocolByteBuffer, int receivedByteCount) {

        PLCMessageHeader mpBCSMessageHeader = new PLCMessageHeader();
        // parse received Data String to validate and get full message length
        mpBCSMessageHeader.clear();

        if (receivedByteCount != PLCConstants.MSG_HEADER_LEN
                && receivedByteCount != PLCConstants.MSG_HEADER_LEN_WITH_STX)
//            if( receivedByteCount != PLCConstants.MSG_HEADER_LEN)
        {
            // error
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap(inputProtocolByteBuffer, 0, receivedByteCount);
//            Need to comment out the following if block for ACPPort              
        if (receivedByteCount == PLCConstants.MSG_HEADER_LEN_WITH_STX && inputProtocolByteBuffer[0] == PortConsts.STX) {
            buf.get(); // just skip STX
        }

        mpBCSMessageHeader.setMsgLength(Short.toUnsignedInt(buf.getShort()));
        mpBCSMessageHeader.setSeqNo(Short.toUnsignedInt(buf.getShort()));
        mpBCSMessageHeader.setMsgType(Short.toUnsignedInt(buf.getShort()));
        mpBCSMessageHeader.setEquipmentId(Long.toString(Integer.toUnsignedLong(buf.getInt())));
        mpBCSMessageHeader.setHours(Byte.toUnsignedInt(buf.get()));
        mpBCSMessageHeader.setMinutes(Byte.toUnsignedInt(buf.get()));
        mpBCSMessageHeader.setMilliSeconds(Short.toUnsignedInt(buf.getShort()));
        mpBCSMessageHeader.setMsgVersion(Short.toUnsignedInt(buf.getShort()));

        if (!isMsgHeaderValid(mpBCSMessageHeader)) {
            return null;
        }

        return mpBCSMessageHeader;
    }

    /**
     * Creates Link Startup Sync response message
     * 
     * @return Link Startup Sync message
     */
    public byte[] createPLCLinkStartupAckMessage(short sequenceNumber, int equimpmentId) {
        ByteBuffer buf = ByteBuffer
                .allocate(PLCConstants.MSG_HEADER_LEN + PLCConstants.PLC_LINK_STARTUP_MSG_ACK_BODY_LEN);
        buf.put(PLCMessageUtil.buildMessageHeader(PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT,
                PLCConstants.PLC_LINK_STARTUP_MSG_ACK_BODY_LEN, equimpmentId, sequenceNumber,
                PLCConstants.MESSAGE_VER));
        buf.putShort(PLCConstants.PLC_LINK_STARTUP_MSG_ACK_STATUS);
        return buf.array();
    }

    /**
     * Creates Keep-Alive response message
     * 
     * @return Keep-alive message
     */
    public byte[] createPLCKeepAliveResponseMessage(int equimpmentId) {
        // Create Keep Alive msg
        byte[] mabKeepAliveResponse = new byte[PLCConstants.MSG_HEADER_LEN + PLCConstants.KEEPALIVE_MSG_BODY_LEN];
        byte[] mabMsgHeader = new byte[PLCConstants.MSG_HEADER_LEN];

        short vwMsgVersion = PLCConstants.MESSAGE_VER;
        short mnSeqNum = 0;
        // Get the header TODO: Sort out Equipment ID for PLC1 - 3 .....
        mabMsgHeader = PLCMessageUtil.buildMessageHeader(PLCConstants.KEEPALIVE_MSG_TYPE,
                PLCConstants.KEEPALIVE_MSG_BODY_LEN, equimpmentId, mnSeqNum, vwMsgVersion);
        System.arraycopy(mabMsgHeader, 0, mabKeepAliveResponse, 0,
                mabMsgHeader.length);

        // Build the Message Body
        byte[] bytes = ByteBuffer.allocate(PLCConstants.KEEPALIVE_MSG_BODY_LEN)
                .putShort(PLCConstants.KEEPALIVE_MSG_ACTIVE_VAL).array();
        System.arraycopy(bytes, 0, mabKeepAliveResponse, mabMsgHeader.length, PLCConstants.KEEPALIVE_MSG_BODY_LEN);

        return (mabKeepAliveResponse);
    }

    public byte[] createPLCLinkStartUpMessage(int equimpmentId, short currentOutboundSequenceNumber) {
        return PLCMessageUtil.buildMessageHeader(PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT,
                (int) PLCConstants.PLC_LINK_STARTUP_MSG_BODY_LEN, equimpmentId,
                currentOutboundSequenceNumber, PLCConstants.MESSAGE_VER);
    }
}
