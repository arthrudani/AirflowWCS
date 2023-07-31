package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.nio.ByteBuffer;
import java.time.LocalTime;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.util.PLCMessageUtil;

/**
 * Handles messages as byte which receives or sends to the Host Port. Encode and Decode the required messages for SAC
 * interface
 * 
 * @author KR
 *
 */
public class SACMessageManager {
    private static final String msSeparator = ",";

    private Logger logger = Logger.getLogger();
    private short msgVersion = 1;// KR: configure this in db and get it in startup
    private String msMessageTxt = null;
    private SACMessageHeader mpSACMessageHeader = null;
    private StringBuffer ipAccumulationBuf = null;
    private byte[] vbMessage = null; // whole message
    private byte[] vbBody = null; // body of the message
    byte[] vbHeader = null; // header

    public SACMessageManager() {
    }

    /* public methods */
    public String getMessageTxt() {
        return (msMessageTxt != null) ? msMessageTxt : "Err";
    }

    public SACMessageHeader getMessageHeader() {
        return mpSACMessageHeader;
    }
    /*
     * Process message header received from SAC
     */

    /**
     * Creates Keep-Alive message
     * 
     * @return Keep-alive message as byte[]
     */
    public byte[] createKeepAliveMessage(int equimpmentId, int sSeqNum) {
        return MessageUtil.buildKeepAliveMessage(equimpmentId, sSeqNum, msgVersion);
    }
    public byte[] createSACLinkStartupAckMessage(short sequenceNumber, int equimpmentId) {
    	
    	short msgLength = SACControlMessage.MSG_HEADER_LEN + SACControlMessage.LINK_STARTUP_MSG_ACK_BODY_LEN;
        ByteBuffer buf = ByteBuffer
                .allocate(msgLength);
        
        buf.put(MessageUtil.buildMessageHeader(msgLength,sequenceNumber,SACControlMessage.LINK_STARTUP_ACK_MSG_TYPE_INT,equimpmentId,msgVersion));

        buf.putShort(SACControlMessage.LINK_STARTUP_MSG_ACK_STATUS);
        return buf.array();
    }
    public byte[] createSACLinkStartUpMessage(int equimpmentId, short currentOutboundSequenceNumber) {
       
    	short msgLength = SACControlMessage.MSG_HEADER_LEN + SACControlMessage.LINK_STARTUP_MSG_BODY_LEN;
    	return MessageUtil.buildMessageHeader(msgLength,currentOutboundSequenceNumber,SACControlMessage.LINK_STARTUP_MSG_TYPE_INT,equimpmentId,msgVersion);		    		
    }

    /**
     * Separate the message to header and body
     * 
     * @param vbMsg
     * @return
     */
    public boolean setMessage(byte[] vbMsg) {
        if (vbMsg == null || vbMsg.length == 0) {
            return false;
        }

        vbMessage = new byte[vbMsg.length];

        boolean returnVal = false;
        try {
            // copy to the message
            System.arraycopy(vbMsg, 0, vbMessage, 0, vbMsg.length);

            // get header
            vbHeader = new byte[SACControlMessage.MSG_HEADER_LEN];
            System.arraycopy(vbMsg, 0, vbHeader, 0, SACControlMessage.MSG_HEADER_LEN);

            // KR: get the header
            if (mpSACMessageHeader == null) {
                mpSACMessageHeader = processReceivedHeader(vbHeader);
            }
            vbBody = new byte[mpSACMessageHeader.getMsgLength() - SACControlMessage.MSG_HEADER_LEN];
            System.arraycopy(vbMsg, SACControlMessage.MSG_HEADER_LEN, vbBody, 0, vbBody.length);

            // set the buffer string and convert each byte to char and add to string buffer
            ipAccumulationBuf = new StringBuffer(vbMessage.length);
            for (Integer i = 0; i < vbMessage.length; i++) {
                ipAccumulationBuf.append((char) vbMessage[i]);
            }

            returnVal = true;

        } catch (Exception ex) {
            logger.logException("setMessage Error:", ex);
            returnVal = false;
        }

        return returnVal;

    }

    public String getReceivedDataFromHost() {

        return (ipAccumulationBuf != null) ? (ipAccumulationBuf.toString()) : "Err";
    }

    /**
     * Processes received message from SAC (host) - This method is called from SACTCPIPReaderWriter.processChannelData
     * 
     * @param ipReadBuf
     * @return
     */
    public boolean processReceivedMessage() {

        boolean returnVal = false;

        try {
            if (mpSACMessageHeader != null) {
                short msgType = mpSACMessageHeader.getMsgType();
                switch (msgType) {
                case SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decode_Expected_Reciept_Msg(vbBody);
                    break;
                case SACControlMessage.EXPECTED_RECIEPT_RESPONSE_ACK_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeExpectedReceiptResponseAckMsg(vbBody);
                    break;
                case SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeRetrievalOrderResponseAckMsg(vbBody);
                    break;
                case SACControlMessage.RETRIEVAL_ITEM_RESPONSE_ACK_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeRetrievalItemResponseAckMsg(vbBody);
                    break;
                case SACControlMessage.INVENTORY_RESPONSE_ACK_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeInventoryResponseAckMsg(vbBody);
                    break;
                case SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeFlightDataUpdateMessage(vbBody);
                    break;
                case SACControlMessage.INVENTORY_UPDATE_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeInventoryUpdateMessage(vbBody);
                    break;
                case SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeStoredCompleteAckMessage(vbBody);
                    break;
                case SACControlMessage.ITEM_RELEASE_ACK_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decodeItemReleaseAckMessage(vbBody);
                    break;
                case SACControlMessage.KEEPALIVE_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator + decode_KeepALive_msg(vbBody);
                    break;
                case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator
                            + decode_Retrieval_Order_Request_Msg(vbBody);
                    break;
                case SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator
                            + decodeInventoryReqByWarehouseMsg(vbBody);
                    break;
                case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator
                            + decode_Inventory_Request_By_Flight_Request_Msg(vbBody);
                    break;
                case SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE:
                    msMessageTxt = mpSACMessageHeader.toString() + msSeparator
                            + decode_Retrieval_Item_Request_Msg(vbBody);
                    break;
                case SACControlMessage.LINK_STARTUP_MSG_TYPE_INT:
              	  	msMessageTxt = mpSACMessageHeader.toString(); // No body in this message
              	  	break;
                case SACControlMessage.LINK_STARTUP_ACK_MSG_TYPE_INT:
                	  msMessageTxt = mpSACMessageHeader.toString()+ msSeparator + decodeStandardAckMessage(vbBody);
                	  break;
                default:
                    logger.logError("Unknown message type received: " + msgType);
                    return false;
                }

                logger.logDebug("processReceivedMessage msgType:" + msgType + " - Msg:" + msMessageTxt);
                returnVal = true;
            }

        } catch (Exception ex) {
            logger.logException("processReceivedMessage Error", ex);
            returnVal = false;
        }

        return returnVal;
    }

	public SACMessageHeader processReceivedHeader(byte[] inputProtocolByteBuffer) {

        // set header
        mpSACMessageHeader = new SACMessageHeader();
        // parse received Data String to validate and get full message length
        mpSACMessageHeader.clear();

        if (inputProtocolByteBuffer.length != SACControlMessage.MSG_HEADER_LEN) {
            // error
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap(inputProtocolByteBuffer, 0, SACControlMessage.MSG_HEADER_LEN);
        
        mpSACMessageHeader.setMsgLength(buf.getShort());
        mpSACMessageHeader.setSeqNo(Short.toUnsignedInt(buf.getShort()));
        mpSACMessageHeader.setMsgType(buf.getShort());
        mpSACMessageHeader.setEquipmentId(Integer.toString(buf.getInt()));
        mpSACMessageHeader.setHours(buf.get() & 0xFFFF);
        mpSACMessageHeader.setMinutes(buf.get() & 0xFFFF);
        mpSACMessageHeader.setMilliSeconds(buf.getShort() & 0xFFFF);
        mpSACMessageHeader.setMsgVersion(buf.getShort());

        if (!isMsgHeaderValid(mpSACMessageHeader)) {
            return null;
        }

        return mpSACMessageHeader;
    }

    /**
     * Validating incoming messages types
     * 
     * @param inMsgType
     * @return
     */
    public boolean isHeaderMsgTypeValid(int inMsgType) {

        switch (inMsgType) {
        case SACControlMessage.KEEPALIVE_MSG_TYPE:
        case SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE:
        case SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_TYPE:
        case SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE:
        case SACControlMessage.EXPECTED_RECIEPT_RESPONSE_ACK_MSG_TYPE:
        case SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE:
        case SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE:
        case SACControlMessage.INVENTORY_UPDATE_MSG_TYPE:
        case SACControlMessage.INVENTORY_UPDATE_ACK_MSG_TYPE:
        case SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE:
        case SACControlMessage.RETRIEVAL_ITEM_RESPONSE_ACK_MSG_TYPE:
        case SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE:
        case SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE:
        case SACControlMessage.ITEM_RELEASE_MSG_TYPE:
        case SACControlMessage.ITEM_RELEASE_ACK_MSG_TYPE:
        case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE:
        case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE:
        case SACControlMessage.INVENTORY_RESPONSE_MSG_TYPE:
        case SACControlMessage.INVENTORY_RESPONSE_ACK_MSG_TYPE:
        	/*
             * KR: TODO:
             * 
             * case BCSPLCMessage.PLC_STORAGE_ORDER_MSG_TYPE_INT: case BCSPLCMessage.PLC_STORAGE_COMPLETE_MSG_TYPE_INT:
             * case BCSPLCMessage.PLC_FLUSH_REQUEST_MSG_TYPE_INT: case BCSPLCMessage.PLC_FLUSH_RESPONSE_MSG_TYPE_INT:
             * case BCSPLCMessage.PLC_ITEM_RELEAZED_MSG_TYPE_INT: case BCSPLCMessage.PLC_LOCATION_STATUS_MSG_TYPE_INT:
             */
        case SACControlMessage.LINK_STARTUP_MSG_TYPE_INT:
        case SACControlMessage.LINK_STARTUP_ACK_MSG_TYPE_INT:
            
            return true;
        default:
            return false;
        }
    }

    public void clear() {
        vbMessage = null; // whole message
        vbBody = null; // body of the message
        vbHeader = null; // header
        msMessageTxt = null;
        ipAccumulationBuf = null;
    }

 
    /* Private methods */

    /**
     * Validate the message header
     * 
     * @param mpSACMessageHeader
     * @return True if valid. false otherwise.
     */
    private boolean isMsgHeaderValid(SACMessageHeader mpSACMessageHeader) {

        boolean vzRtnVal = true;

        if (!isHeaderMsgTypeValid(mpSACMessageHeader.getMsgType())) {
            logger.logError("Invalid BCS Message Header Type - " + mpSACMessageHeader.getMsgType());
            return false;
        }

        // logError("BCS Message Header Seqno received - " +
        // mpSACMessageHeader.getSeqNo() + ", Expecting Seqno " +
        // mnExpectedReceivedSequenceNumber );
        if (!isHeaderSeqNoValid(mpSACMessageHeader.getSeqNo())) {
            // TODO:
            // logError("Invalid BCS Message Header Seqno received - " +
            // mpSACMessageHeader.getSeqNo() + ", Expecting Seqno " +
            // mnExpectedReceivedSequenceNumber );
            return false;
        }
        return vzRtnVal;
    }

    private boolean isHeaderSeqNoValid(int inMsgSeqNo) {
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

    private String getStringFromBuffer(byte[] receivedBuffer, int iStartPosition, int iStringLen) {
        return new String(receivedBuffer, iStartPosition, iStringLen).trim();
    }

    /**
     * Converts provided string to fixed size char array.
     * 
     * @param st
     * @param requiredSize
     * @return Char array
     */
    private char[] convertSringToFixSizeCharArray(String st, int requiredSize) {

        if (st.isEmpty() || requiredSize == 0) {
            return null;
        }
        int stringLen = st.length();
        if (stringLen == requiredSize) {
            return st.toCharArray();
        } else if (stringLen > requiredSize) {
            // truncate to required size
            return st.substring(0, requiredSize).toCharArray();

        } else if (stringLen < requiredSize) {
            // add null value to end of string
            int diff = requiredSize - stringLen;
            for (int i = 0; i < diff; i++) {
                st += "\0";
            }
            return st.toCharArray();
        }
        return null;
    }

    /* de_coder methods */
    private String decode_Expected_Reciept_Msg(byte[] receivedBuffer) {
        StringBuffer sMsgStringBuf = new StringBuffer();
        int iStartPosition = 0;
        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);
        
        // Get order id(1st field, DWord = 4 bytes)
        int orderId = buf.getInt(iStartPosition);
        sMsgStringBuf.append(orderId);
        iStartPosition += SACControlMessage.MSG_DWORD_LEN;

        // Get tray id(2nd field, DWord = 4 bytes)
        int trayId = buf.getInt(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(trayId);
        iStartPosition += SACControlMessage.MSG_DWORD_LEN;

        // Get global id(3rd field, DWord = 4 bytes)
        int globalId = buf.getInt(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(globalId);
        iStartPosition += SACControlMessage.MSG_DWORD_LEN;

        // Get bag id(4th field, IATA code, barcode, Char[12] = 12 bytes)
        sMsgStringBuf.append(msSeparator)
                .append(getStringFromBuffer(buf.array(), iStartPosition, SACControlMessage.BAG_BARCODE_LEN));
        iStartPosition += SACControlMessage.BAG_BARCODE_LEN;

        // Get flight number(5th field, Char[8] = 8 bytes)
        sMsgStringBuf.append(msSeparator)
                .append(getStringFromBuffer(buf.array(), iStartPosition, SACControlMessage.FLIGHT_NUM_LEN));
        iStartPosition += SACControlMessage.FLIGHT_NUM_LEN;

        // Get flight scheduled datetime(6th field, STD, Char[14] = 14 bytes, YYYYMMDDHHMMSS)
        sMsgStringBuf.append(msSeparator).append(
                getStringFromBuffer(buf.array(), iStartPosition, SACControlMessage.FLIGHT_SCHEDULED_DATETIEM_LEN));
        iStartPosition += SACControlMessage.FLIGHT_SCHEDULED_DATETIEM_LEN;

        // Get default retrieval datetime(7th field, Char[14] = 14 bytes, YYYYMMDDHHMMSS)
        sMsgStringBuf.append(msSeparator).append(
                getStringFromBuffer(buf.array(), iStartPosition, SACControlMessage.DEFAULT_RETRIEVAL_DATETIEM_LEN));
        iStartPosition += SACControlMessage.DEFAULT_RETRIEVAL_DATETIEM_LEN;

        // Get final sort location id(8th field, DWord = 4 bytes)
        int finalSortLocation = buf.getInt(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(finalSortLocation);
        iStartPosition += SACControlMessage.MSG_DWORD_LEN;

        // Get item type(9th field, Word = 2 bytes)
        int itemType = buf.getShort(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(itemType);
        iStartPosition += SACControlMessage.MSG_WORD_LEN;

        // Get request type(10th field, Word = 2 bytes)
        int requestType = buf.getShort(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(requestType);

        return sMsgStringBuf.toString();
    }
    
    private String decodeExpectedReceiptResponseAckMsg(byte[] rawMessage) {
        StringBuffer strBuf = new StringBuffer();

        ByteBuffer buf = ByteBuffer.wrap(rawMessage, 0, rawMessage.length);

        // Get status value
        short status = buf.getShort();
        strBuf.append(status);

        return strBuf.toString();
    }
    
    private String decodeRetrievalOrderResponseAckMsg(byte[] rawMessage) {
        StringBuffer strBuf = new StringBuffer();

        ByteBuffer buf = ByteBuffer.wrap(rawMessage, 0, rawMessage.length);

        // Get status value
        short status = buf.getShort();
        strBuf.append(status);

        return strBuf.toString();
    }
    
    private String decodeRetrievalItemResponseAckMsg(byte[] rawMessage) {
        StringBuffer strBuf = new StringBuffer();

        ByteBuffer buf = ByteBuffer.wrap(rawMessage, 0, rawMessage.length);

        // Get status value
        short status = buf.getShort();
        strBuf.append(status);

        return strBuf.toString();
    }
    
    private String decodeInventoryResponseAckMsg(byte[] rawMessage) {
        StringBuffer strBuf = new StringBuffer();

        ByteBuffer buf = ByteBuffer.wrap(rawMessage, 0, rawMessage.length);

        // Get status value
        short status = buf.getShort();
        strBuf.append(status);

        return strBuf.toString();
    }

    private String decodeFlightDataUpdateMessage(byte[] rawMessage) {
        StringBuffer strBuf = new StringBuffer();
        int startPos = 0;
        ByteBuffer byteBuf = ByteBuffer.wrap(rawMessage, 0, rawMessage.length);

        // Get flight number(1st field, Char[9] = 9 bytes)
        strBuf.append(getStringFromBuffer(byteBuf.array(), startPos, SACControlMessage.FLIGHT_NUM_LEN));
        startPos += SACControlMessage.FLIGHT_NUM_LEN;

        // Get flight scheduled datetime(2nd field, STD, Char[14] = 14 bytes, YYYYMMDDHHMMSS)
        strBuf.append(msSeparator).append(
                getStringFromBuffer(byteBuf.array(), startPos, SACControlMessage.FLIGHT_SCHEDULED_DATETIEM_LEN));
        startPos += SACControlMessage.FLIGHT_SCHEDULED_DATETIEM_LEN;

        // Get default retrieval datetime(3rd field, Char[14] = 14 bytes, YYYYMMDDHHMMSS)
        strBuf.append(msSeparator).append(
                getStringFromBuffer(byteBuf.array(), startPos, SACControlMessage.DEFAULT_RETRIEVAL_DATETIEM_LEN));
        startPos += SACControlMessage.DEFAULT_RETRIEVAL_DATETIEM_LEN;

        // Get final sort location id(4th field, DWord = 4 bytes)
        int finalSortLocation = byteBuf.getInt(startPos);
        strBuf.append(msSeparator).append(finalSortLocation);
        
        return strBuf.toString();
    }
    
    private String decodeInventoryUpdateMessage(byte[] rawMessage) {
    	StringBuffer sMsgStringBuf = new StringBuffer();
        int iStartPosition = 0;
        ByteBuffer byteBuf = ByteBuffer.wrap(rawMessage, 0, rawMessage.length);

        // Get tray id(1st field, DWord = 4 bytes)
        int trayId = byteBuf.getInt(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(trayId);
        iStartPosition += SACControlMessage.MSG_DWORD_LEN;

        // Get global id(2nd field, DWord = 4 bytes)
        int globalId = byteBuf.getInt(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(globalId);
        iStartPosition += SACControlMessage.MSG_DWORD_LEN;

        // Get bag id(3rd field, IATA code, barcode, Char[12] = 12 bytes)
        sMsgStringBuf.append(msSeparator)
                .append(getStringFromBuffer(byteBuf.array(), iStartPosition, SACControlMessage.BAG_BARCODE_LEN));
        iStartPosition += SACControlMessage.BAG_BARCODE_LEN;
        
        // Get storage location id(4th field, DWord = 4 bytes)	
        int storageLocationID = byteBuf.getInt(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(storageLocationID);
        iStartPosition += SACControlMessage.MSG_DWORD_LEN;

        // Get final sort location id(5th field, DWord = 4 bytes)
        short status = byteBuf.getShort(iStartPosition);
        sMsgStringBuf.append(msSeparator).append(status);

        return sMsgStringBuf.toString();
    }

    
    private String decodeStandardAckMessage(byte[] receivedBuffer) {
        StringBuffer sMsgStringBuf = new StringBuffer();

        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);

        // Get status value
        short status = buf.getShort();
        sMsgStringBuf.append(status);

        return sMsgStringBuf.toString();
    } 
    
    private String decodeStoredCompleteAckMessage(byte[] receivedBuffer) {
        StringBuffer sMsgStringBuf = new StringBuffer();

        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);

        // Get status value
        short status = buf.getShort();
        sMsgStringBuf.append(status);

        return sMsgStringBuf.toString();
    }
    
    private String decodeItemReleaseAckMessage(byte[] receivedBuffer) {
        StringBuffer sMsgStringBuf = new StringBuffer();

        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);

        // Get status value
        short status = buf.getShort();
        sMsgStringBuf.append(status);

        return sMsgStringBuf.toString();
    }

    private String decode_KeepALive_msg(byte[] receivedBuffer) {
        // String sMsgString = Integer.toString(nMsgID);
        Integer iStartPosition = 0;// this body and start position
        ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);

        Short iActiveFlag = buf.getShort(iStartPosition);// after header which is 16 bytes
        // sMsgString += msSeparator + iActiveFlag;
        String sMsgString = Short.toString(iActiveFlag);
        return sMsgString;
    }

    /*
     * This method will decode the SAC message to bytes as follow
     * http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac- messages/retrieval-order-2.html the
     * message expected a comma separated : Body Example: {5556,QFA 1234A,20221201134500,0} -
     * {<OrdrNo>,<FlightNo>,<Flight Scheduled Date Time>,<Number of Bags to retrieve (0=All)>}
     */
    private String decode_Retrieval_Order_Request_Msg(byte[] receivedBuffer) {

        Integer iStartPosition = 0;
        String sMsgString = "";

        if (receivedBuffer != null && receivedBuffer.length == SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_BODY_LEN) {
            ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);

            // start with global Id/ Order Id
            // after header which is 14 length
            Integer orderId = buf.getInt(iStartPosition);
            sMsgString = Integer.toString(orderId);

            // get flight No
            iStartPosition += SACControlMessage.MSG_DWORD_LEN;
            sMsgString += msSeparator
                    + getStringFromBuffer(receivedBuffer, iStartPosition, SACControlMessage.FLIGHT_NUM_LEN);

            // get flight schedule date time
            iStartPosition += SACControlMessage.FLIGHT_NUM_LEN;
            sMsgString += msSeparator + getStringFromBuffer(receivedBuffer, iStartPosition,
                    SACControlMessage.FLIGHT_SCHEDULED_DATETIEM_LEN);

            // get noOfBags (All = 0)
            iStartPosition += SACControlMessage.FLIGHT_SCHEDULED_DATETIEM_LEN;
            short noOfBags = buf.getShort(iStartPosition);
            sMsgString += msSeparator + noOfBags;
        }
        return (sMsgString);

    }
    
    private String decode_Inventory_Request_By_Flight_Request_Msg(byte[] receivedBuffer) {

        Integer iStartPosition = 0;
        String sMsgString = "";

        if (receivedBuffer != null) {
            ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);

            // start with global Id/ Order Id
            // after header which is 14 length
            Integer orderId = buf.getInt(iStartPosition);
            sMsgString = Integer.toString(orderId);

            // get flight No
            iStartPosition += SACControlMessage.MSG_DWORD_LEN;
            sMsgString += msSeparator
                    + getStringFromBuffer(receivedBuffer, iStartPosition, SACControlMessage.FLIGHT_NUM_LEN);

            // get flight schedule date time
            iStartPosition += SACControlMessage.FLIGHT_NUM_LEN;
            sMsgString += msSeparator + getStringFromBuffer(receivedBuffer, iStartPosition,
                    SACControlMessage.FLIGHT_SCHEDULED_DATETIEM_LEN);
        }
        return (sMsgString);

    }

    private String decode_Retrieval_Item_Request_Msg(byte[] receivedBuffer) {
        Integer iStartPosition = 0;
        String sMsgString = "";

        if (receivedBuffer != null) {
            // there is no limit in size for this message
            ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);
            // start with Order Id
            // after header which is 14 length
            int iOrderId = buf.getInt(iStartPosition);
            sMsgString = Integer.toString(iOrderId);

            // get Array Length
            iStartPosition += SACControlMessage.MSG_DWORD_LEN;
            short arraylength = buf.getShort(iStartPosition);
            sMsgString += msSeparator + arraylength;

            iStartPosition += SACControlMessage.MSG_WORD_LEN;
            // get Arry of the trays/bags based on the array length in the message
            if (arraylength > 0) {
                String itemsData = "";
                short index = 1;
                for (int x = 0; x < arraylength; x++) {
                    // move to first position to read the TrayId
                    Integer iTrayId = buf.getInt(iStartPosition);
                    if (index == 1) {
                        itemsData += Integer.toString(iTrayId); // will add the separator in end
                    } else {
                        itemsData += msSeparator + Integer.toString(iTrayId);
                    }
                    // move to next position and read GlobalId
                    iStartPosition += SACControlMessage.MSG_DWORD_LEN;
                    Integer iGlobalId = buf.getInt(iStartPosition);
                    itemsData += msSeparator + Integer.toString(iGlobalId); // read global id

                    // move to next position and read item ID / barcode
                    iStartPosition += SACControlMessage.MSG_DWORD_LEN;
                    itemsData += msSeparator
                            + getStringFromBuffer(receivedBuffer, iStartPosition, SACControlMessage.BAG_BARCODE_LEN);

                    iStartPosition += SACControlMessage.BAG_BARCODE_LEN;
                    Integer iFinalSortLoacation = buf.getInt(iStartPosition);
                    itemsData += msSeparator + iFinalSortLoacation;
                    iStartPosition += SACControlMessage.MSG_DWORD_LEN;

                    index++;
                    
                }

                sMsgString += msSeparator + itemsData;
            }
        }
        return (sMsgString);

    }
    
	private String decodeInventoryReqByWarehouseMsg(byte[] receivedBuffer) {
		Integer iStartPosition = 0;
		String sMsgString = "";

		if (receivedBuffer != null
				&& receivedBuffer.length == SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_BODY_LEN) {
			ByteBuffer buf = ByteBuffer.wrap(receivedBuffer, 0, receivedBuffer.length);

			// get request Id
			// after header which is 14 length
			Integer requestId = buf.getInt(iStartPosition);
			sMsgString = Integer.toString(requestId);

			// get warehouse Id
			iStartPosition += SACControlMessage.MSG_DWORD_LEN;
			sMsgString += msSeparator
					+ getStringFromBuffer(receivedBuffer, iStartPosition, SACControlMessage.WAREHOUSE_NAME_LEN);
		}
		return (sMsgString);

	}

}
