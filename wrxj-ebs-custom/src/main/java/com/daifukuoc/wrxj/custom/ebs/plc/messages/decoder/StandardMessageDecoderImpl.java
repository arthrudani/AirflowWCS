package com.daifukuoc.wrxj.custom.ebs.plc.messages.decoder;

import java.nio.ByteBuffer;

import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.util.PLCMessageUtil;

/*
 * Standard class which is used for decoding the PLC message send by the PLC
 * 
 */
public class StandardMessageDecoderImpl implements StandardMessageDecoder {

    private static final int LENGTH_OF_SUBARRAY = 6;
	protected Logger logger = Logger.getLogger();

    public StandardMessageDecoderImpl() {

    }

    public String decode(PLCMessageHeader mpMessageHeader, byte[] bMsg) {
        String sResult = "";
        if (mpMessageHeader == null) {
            return sResult;
        }
        int iMsgId = mpMessageHeader.getMsgType();

        switch (iMsgId) {
        case PLCConstants.KEEPALIVE_MSG_TYPE:
            // Not doing anything for keep alive message
            break;
        case PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE_INT:
            sResult = decodeItemArrivedMessage(mpMessageHeader, bMsg);
            break;
        case PLCConstants.PLC_ITEM_STORED_MSG_TYPE_INT:
            sResult = decodeItemStoredMessage(mpMessageHeader, bMsg);
            break;
        case PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE_INT:
            sResult = decodeItemReleasedMessage(mpMessageHeader, bMsg);
            break;
        case PLCConstants.PLC_MOVE_ORDER_ACK_MSG_TYPE_INT:
            // Not doing anything for move order ack message
            break;
        case PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE_INT:
            // Not doing anything for flight data ack message
            break;
        case PLCConstants.PLC_FLUSH_REQUEST_ACK_MSG_TYPE_INT:
        	// Not doing anything for flush response message
        	break;
        case PLCConstants.PLC_BAG_DATA_UPDATE_ACK_MSG_TYPE_INT:
        	// Not doing anything for bag data update ack message
        	break;
        case PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE_INT:
            sResult = decodeItemPickedUpMessage(mpMessageHeader, bMsg);
            break;
        case PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE_INT:
            sResult = decodeLocationStatusMessage(mpMessageHeader, bMsg);
            break;
        default:

            logger.logError("PLC Device Msg Type \"" + iMsgId + "\" NOT Processed - StandardMessageDecoderImpl()");
            break;
        }
        return sResult;
    }

	/* private methods */

    private String decodeItemArrivedMessage(PLCMessageHeader mpMessageHeader, byte[] receivedBuffer) {
        if (receivedBuffer == null || receivedBuffer.length < PLCConstants.MSG_HEADER_LEN)
            return "";

        // start with MsgId and Serial number in the header
        String sMsgString = Integer.toString(mpMessageHeader.getMsgType());
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + mpMessageHeader.getSeqNo();

        int iStartPosition = PLCConstants.MSG_HEADER_LEN;
        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, mpMessageHeader.getMsgLength());
        // get Order Id
        Integer orderId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + orderId;
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        // Getting TrayId
        Integer trayId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + trayId;
        // Getting GlobalId
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        Integer globalId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + globalId;
        // Getting bar-code / ItemId
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM
                + PLCMessageUtil.getStringFromBuffer(receivedBuffer, iStartPosition, PLCConstants.BAG_BARCODE_LEN);
        // Getting LaneId (location of the item)
        iStartPosition += PLCConstants.BAG_BARCODE_LEN;
        Integer laneId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + laneId;
        return (sMsgString);
    }
    private String decodeItemReleasedMessage(PLCMessageHeader mpMessageHeader, byte[] receivedBuffer) {
        if (receivedBuffer == null || receivedBuffer.length < PLCConstants.MSG_HEADER_LEN)
            return "";

        // start with MsgId and Serial number in the header
        String sMsgString = Integer.toString(mpMessageHeader.getMsgType());
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + mpMessageHeader.getSeqNo();

        int iStartPosition = PLCConstants.MSG_HEADER_LEN;
        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, mpMessageHeader.getMsgLength());
        // get Order Id
        Integer orderId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + orderId;
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        // Getting TrayId
        Integer trayId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + trayId;
        // Getting GlobalId
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        Integer globalId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + globalId;
        // Getting bar-code / ItemId
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM
                + PLCMessageUtil.getStringFromBuffer(receivedBuffer, iStartPosition, PLCConstants.BAG_BARCODE_LEN);
        // Getting LaneId (location of the item)
        iStartPosition += PLCConstants.BAG_BARCODE_LEN;
        Integer laneId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + laneId;
        return (sMsgString);
    }

    private String decodeItemStoredMessage(PLCMessageHeader mpMessageHeader, byte[] receivedBuffer) {
        if (receivedBuffer == null || receivedBuffer.length < PLCConstants.MSG_HEADER_LEN)
            return "";

        // start with MsgId and Serial number in the header
        String sMsgString = Integer.toString(mpMessageHeader.getMsgType());
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + mpMessageHeader.getSeqNo();

        int iStartPosition = PLCConstants.MSG_HEADER_LEN;

        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, mpMessageHeader.getMsgLength());
        // get Order Id
        Integer orderId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + orderId;
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        // Getting TrayId
        Integer trayId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + trayId;
        // Getting GlobalId
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        Integer globalId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + globalId;
        // Getting bar-code / ItemId
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM
                + PLCMessageUtil.getStringFromBuffer(receivedBuffer, iStartPosition, PLCConstants.BAG_BARCODE_LEN);
        // Getting LaneId (location of the item)
        iStartPosition += PLCConstants.BAG_BARCODE_LEN;
        Integer laneId = buf.getInt(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + laneId;
        // Getting status of the storage completion
        iStartPosition += PLCConstants.MSG_DWORD_LEN;
        short status = buf.getShort(iStartPosition);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + status;
        return (sMsgString);
    }

    // KR: TODO Complete this message and test it
    protected String decodeItemPickedUpMessage(PLCMessageHeader mpMessageHeader, byte[] receivedBuffer) {

        if (receivedBuffer == null
                || receivedBuffer.length < PLCConstants.MSG_HEADER_LEN + PLCConstants.PLC_ITEM_PICKEDUP_MSG_BODY_LEN)
            return "";

        // start with MsgId and Serial number in the header
        String sMsgString = Integer.toString(mpMessageHeader.getMsgType());
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + mpMessageHeader.getSeqNo();

        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, PLCConstants.MSG_HEADER_LEN,
                PLCConstants.PLC_ITEM_PICKEDUP_MSG_BODY_LEN);

        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + Integer.toUnsignedLong(buf.getInt()); // Move order request ID
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + Integer.toUnsignedLong(buf.getInt()); // Tray ID
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + Integer.toUnsignedLong(buf.getInt()); // Global ID

        // Item ID (barcode)
        byte[] itemID = new byte[12];
        buf.get(itemID);
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + PLCMessageUtil.getStringFromBuffer(itemID, 0, 12);

        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + Integer.toUnsignedLong(buf.getInt()); // Location ID - From
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + Integer.toUnsignedLong(buf.getInt()); // Location ID - To
        sMsgString += PLCConstants.PLC_MESSAGE_DELIM + Short.toUnsignedInt(buf.getShort()); // Status Flags

        return (sMsgString);
    }

    protected String decodeLocationStatusMessage(PLCMessageHeader plcMessageHeader, byte[] receivedBuffer) {
		int subArrayLen = LENGTH_OF_SUBARRAY;// 6 bytes in each sub array ( Location Id =4 bytes AND Status Flag = 2 bytes)
        if (receivedBuffer == null || receivedBuffer.length < PLCConstants.MSG_HEADER_LEN)
            return "";

        String sMsgString = Integer.toString(plcMessageHeader.getMsgType()) +
        		PLCConstants.PLC_MESSAGE_DELIM + Integer.toString(plcMessageHeader.getSeqNo());
        // get length of body
        int bodyLength = plcMessageHeader.getMsgLength() - PLCConstants.MSG_HEADER_LEN; // without header
        // init body byte array
        byte[] bodyBytes = new byte[bodyLength];
        // copy the body to the new array
        System.arraycopy(receivedBuffer, PLCConstants.MSG_HEADER_LEN, bodyBytes, 0, bodyLength);

        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyBytes, 0, bodyLength);

        int end = bodyLength / subArrayLen;
        for (int index = 0; index < end; index++) {
            long locationId = Integer.toUnsignedLong(bodyBuffer.getInt()); // get the location id
            int status = Short.toUnsignedInt(bodyBuffer.getShort());// get the status
            sMsgString += PLCConstants.PLC_MESSAGE_DELIM + String.format("%010d", locationId)
                    + PLCConstants.PLC_MESSAGE_DELIM + status;
        }
        return (sMsgString);
    }

}
