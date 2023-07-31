package com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder;

import java.nio.ByteBuffer;

import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.util.PLCMessageUtil;
import com.daifukuoc.wrxj.custom.ebs.util.MiscUtility;

public class StandardMessageEncoderImpl implements StandardMessageEncoder {

    protected Logger logger = Logger.getLogger();

    public StandardMessageEncoderImpl() {

    }

    @Override
    public byte[] encode(String sData) {

        if (sData == null || sData.isBlank()) {
            logger.logError("Encode-> data is null or blank. Data:" + sData);
            return null;
        }
        String[] sVars = sData.split(PLCConstants.DELIM_COMMA);

        if (sVars == null || sVars.length == 0) {
            logger.logError("Encode-> data is null or len is zero. Data:" + sData);
            return null;
        }

        byte[] baData = null;
        switch (sVars[0]) {
        case PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE:
            baData = encodeMoveOrderRequestMessage(sData);
            break;
        case PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE:
            baData = encodeFlightDataUpdateMessage(sData);
            break;
        case PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE:
             baData = encodeFlushMessage(sData);
            break;
        case PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE:
            baData = encodeItemArrivedAck(sData);
            break;
        case PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE:
            baData = encodeItemReleasedAck(sData);
            break;
        case PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE:
            baData = encodeItemPickedUpAck(sData, PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT);
            break;
        case PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE:
            baData = encodeItemStoredAck(sData);
            break;
        case PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE:
            baData = encodeLocationStatusAck(sData, PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE_INT);
            break;
        case PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE:
            baData = encodeBagDataUpdateMessage(sData);
            break;
        }
        return baData;
    }

	protected byte[] encodeItemPickedUpAck(String sData, short messageType) {
        byte[] baData = null;
        String[] sVars = MiscUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length > 0) {
            try {
                // the first element in index [0] is type and second will be the header serial number
                short serialNum = MiscUtility.getShortValue(sVars[1]);
                // body of the message from here
                short status = MiscUtility.getShortValue(sVars[2]);

                baData = createStandardAckMessage(status, messageType, serialNum, PLCConstants.MESSAGE_VER);

            } catch (Exception ex) {
                logger.logException("Failed to build the Item Arrived Ack message to PLC:" + sData, ex);
            }
        }
        return baData;
    }

    protected byte[] encodeLocationStatusAck(String sData, short messageType) {
        byte[] baData = null;
        String[] sVars = MiscUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length > 0) {
            try {
                // the first element in index [0] is type and second will be the header serial number
                short serialNum = MiscUtility.getShortValue(sVars[1]);
                // body of the message from here
                short status = MiscUtility.getShortValue(sVars[2]);

                baData = createStandardAckMessage(status, messageType, serialNum, PLCConstants.MESSAGE_VER);

            } catch (Exception ex) {
                logger.logException("Failed to build the Item Arrived Ack message to PLC:" + sData, ex);
            }
        }
        return baData;
    }

    protected byte[] encodeItemArrivedAck(String sData) {
        byte[] baData = null;
        String[] sVars = MiscUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length > 0) {
            try {
                // the first element in index [0] is type and second will be the header serial number
                short serialNum = MiscUtility.getShortValue(sVars[1]);
                // body of the message from here
                short status = MiscUtility.getShortValue(sVars[2]);

                short iMsgVer = 1;
                baData = createStandardAckMessage(status, PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE_INT, serialNum,
                        iMsgVer);

            } catch (Exception ex) {
                logger.logException("Failed to build the Item Arrived Ack message to PLC:" + sData, ex);
            }
        }
        return baData;
    }
    protected byte[] encodeItemReleasedAck(String sData) {
        byte[] baData = null;
        String[] sVars = MiscUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length > 0) {
            try {
                // the first element in index [0] is type and second will be the header serial number
                short serialNum = MiscUtility.getShortValue(sVars[1]);
                // body of the message from here
                short status = MiscUtility.getShortValue(sVars[2]);

                short iMsgVer = 1;
                baData = createStandardAckMessage(status, PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE_INT, serialNum,
                        iMsgVer);

            } catch (Exception ex) {
                logger.logException("Failed to build the Item Arrived Ack message to PLC:" + sData, ex);
            }
        }
        return baData;
    }
    protected byte[] encodeItemStoredAck(String sData) {
        byte[] baData = null;
        String[] sVars = MiscUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length > 0) {
            try {
                // the first element in index [0] is type and second will be the header serial number
                short serialNum = MiscUtility.getShortValue(sVars[1]);
                // body of the message from here
                short status = MiscUtility.getShortValue(sVars[2]);
                short iMsgVer = 1;
                baData = createStandardAckMessage(status, PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE_INT, serialNum,
                        iMsgVer);

            } catch (Exception ex) {
                logger.logException("Failed to build the Item stored Ack message to PLC:" + sData, ex);
            }
        }
        return baData;
    }

    protected byte[] encodeMoveOrderRequestMessage(String sData) {
        byte[] baData = null;
        String[] sVars = MiscUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length > 0) {
            try {

                // the first element in index [0] is type and second will be the header serial number
                short serialNum = MiscUtility.getShortValue(sVars[1]);
                // body of the message from here
                int orderId = MiscUtility.getIntegerValue(sVars[2]);
                int trayId = MiscUtility.getIntegerValue(sVars[3]);
                int globalId = MiscUtility.getIntegerValue(sVars[4]);
                String itemId = sVars[5];
                String lot = sVars[6];
                String flightScheduledDate = sVars[7];
                int finalSortLocation = MiscUtility.getIntegerValue(sVars[8]);
                int fromLocation = MiscUtility.getIntegerValue(sVars[9]);
                int toLocation = MiscUtility.getIntegerValue(sVars[10]);
                short moveType = MiscUtility.getShortValue(sVars[11]);

                baData = createPLCMoveOrderRequestMessage(serialNum, orderId, trayId, globalId, itemId, lot,
                        flightScheduledDate, finalSortLocation, fromLocation, toLocation, moveType);
            } catch (Exception ex) {
                logger.logException("Failed to build the move order request message to PLC:" + sData, ex);
            }
        }
        return baData;
    }

    protected byte[] encodeFlushMessage(String sData) {
        byte[] baData = null;
        String[] sVars = SKDCUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length == 6) {
            try {
                // the first element in index [0] is type
            	int orderId = MiscUtility.getIntegerValue(sVars[2]);
                int laneId = MiscUtility.getIntegerValue(sVars[3]);
                short qty = Short.parseShort(sVars[4]);
                short requestType = Short.parseShort(sVars[5]);

                baData = createPLCFlushMessage(orderId, laneId, qty, requestType);
            } catch (Exception ex) {
                logger.logException("Failed to parse Flush Message:" + sData, ex);
            }
        }
        return baData;
    }
    
    private byte[] encodeFlightDataUpdateMessage(String message) {
        if (message != null) {
            String[] split = message.split(PLCConstants.DELIM_COMMA);
            if (split.length == PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_SPLIT) {
                String lot = split[2];
                int finalSortLocation;
                try {
                    finalSortLocation = Integer.parseInt(split[3]);
                } catch (Exception e) {
                    finalSortLocation = 0;
                }
                
                ByteBuffer buf = ByteBuffer.allocate(PLCConstants.MSG_HEADER_LEN + PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_BODY_LEN);
                buf.put(PLCMessageUtil.buildMessageHeader(PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE_INT,
                        PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_BODY_LEN, PLCConstants.EBSC_EQUIPID, (short)0, (short)1));            
                buf.put(String.format("%-8s", lot).getBytes());
                buf.putInt(finalSortLocation);
                
                return buf.array();
            }
        }
        return null;
    }

    /**
     * Protected for the testing purtpose.
     */
    protected byte[] createStandardAckMessage(short status, Short iMsgType, Short iMsgSequenceNum, short iMsgVer) {
        int nextStartingPosition = 0;
        byte[] msgHeader = new byte[PLCConstants.MSG_HEADER_LEN];
        byte[] msg = new byte[PLCConstants.PLC_STANDARD_ACK_BODY_LEN + PLCConstants.MSG_HEADER_LEN];
        // Build the header and add to the msg[]
        msgHeader = PLCMessageUtil.buildMessageHeader(iMsgType, PLCConstants.PLC_STANDARD_ACK_BODY_LEN,
                PLCConstants.EBSC_EQUIPID, iMsgSequenceNum, iMsgVer);
        System.arraycopy(msgHeader, 0, msg, nextStartingPosition, msgHeader.length);
        // Build the body
        // add TrayId to the message
        nextStartingPosition = msgHeader.length;
        byte[] bytes = ByteBuffer.allocate(PLCConstants.MSG_WORD_LEN).putShort(status).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        return msg;
    }

    /* private methods */

    private byte[] createPLCMoveOrderRequestMessage(short mwSeqNum, int orderId, int trayId, int globalId,
            String itemId, String lot, String flightScheduledDate, int finalSortLocation, int fromLocation,
            int toLocation, short moveType) {
        int nextStartingPosition = 0;
        byte[] msgHeader = new byte[PLCConstants.MSG_HEADER_LEN];
        byte[] msg = new byte[PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_BODY_LEN + PLCConstants.MSG_HEADER_LEN];

        short mwMsgVer = 1; // KR: TODO
        // Build the header and add to the msg[]
        msgHeader = PLCMessageUtil.buildMessageHeader(PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE_INT,
                PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_BODY_LEN, PLCConstants.EBSC_EQUIPID, mwSeqNum, mwMsgVer);
        System.arraycopy(msgHeader, 0, msg, nextStartingPosition, msgHeader.length);

        // Build the body

        // add Order Id
        nextStartingPosition = msgHeader.length;
        byte[] bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(orderId).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add TrayId to the message
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(trayId).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add globalId to the message
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(globalId).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add itemId char[12]
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        char[] itemIdArray = PLCMessageUtil.convertSringToFixSizeCharArray(itemId, PLCConstants.BAG_BARCODE_LEN);
        bytes = new String(itemIdArray).getBytes();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add flightNumber char[9]
        nextStartingPosition += itemIdArray.length;
        char[] flightNumberArray = PLCMessageUtil.convertSringToFixSizeCharArray(lot, PLCConstants.FLIGHT_NUMBER_LEN);
        bytes = new String(flightNumberArray).getBytes();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add flight scheduled date time char[14]
        nextStartingPosition += flightNumberArray.length;
        char[] flightScheduledDataTimeArray = PLCMessageUtil.convertSringToFixSizeCharArray(flightScheduledDate,
                PLCConstants.FLIGHT_SCHEDULED_DATE_LEN);
        bytes = new String(flightScheduledDataTimeArray).getBytes();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add Final sort location
        nextStartingPosition += flightScheduledDataTimeArray.length;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(finalSortLocation).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add from location
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(fromLocation).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add to location
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(toLocation).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add move type
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_WORD_LEN).putShort(moveType).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);
        return msg;
    }

    private byte[] createPLCFlushMessage(int orderId, int landId, short qty, short releaseInterval) {
        int nextStartingPosition = 0;
        byte[] msgHeader = new byte[PLCConstants.MSG_HEADER_LEN];
        byte[] msg = new byte[PLCConstants.PLC_FLUSH_REQUEST_MSG_BODY_LEN + PLCConstants.MSG_HEADER_LEN];
        short mwSeqNum = 0; // this will change
        short mwMsgVer = 1; // KR: TODO
        // Build the header and add to the msg[]
        msgHeader = PLCMessageUtil.buildMessageHeader(PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE_INT,
                PLCConstants.PLC_FLUSH_REQUEST_MSG_BODY_LEN, PLCConstants.EBSC_EQUIPID, mwSeqNum, mwMsgVer);
        System.arraycopy(msgHeader, 0, msg, nextStartingPosition, msgHeader.length);
        
        // add Order ID
        nextStartingPosition = msgHeader.length;// 14
        byte[] bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(orderId).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);
        
        // add Land ID
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(landId).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add Qty
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_WORD_LEN).putShort(qty).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add Request Type
        nextStartingPosition += PLCConstants.MSG_WORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_WORD_LEN).putShort(releaseInterval).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);
        
        return msg;
    }

    protected byte[] encodeBagDataUpdateMessage(String sData) {

    	byte[] baData = null;
        String[] sVars = SKDCUtility.getTokens(sData, PLCConstants.DELIM_COMMA);
        if (sVars.length == 9) {
            try {
            	// the first element in index [0] is type and second will be the header serial number
                short serialNum = MiscUtility.getShortValue(sVars[1]);
                // body of the message from here
                int trayId = MiscUtility.getIntegerValue(sVars[2]);
                int globalId = MiscUtility.getIntegerValue(sVars[3]);
                String itemId = sVars[4];
                String lot = sVars[5];
                int finalSortLocation = MiscUtility.getIntegerValue(sVars[6]);
                int locationID = MiscUtility.getIntegerValue(sVars[7]);
                short updateType = MiscUtility.getShortValue(sVars[8]);

                baData = createPLCBagDataUpdateMessage(trayId, globalId, itemId, lot, finalSortLocation, locationID, updateType);
            } catch (Exception ex) {
                logger.logException("Failed to parse Flush Message:" + sData, ex);
            }
        }
        return baData;
	}

	private byte[] createPLCBagDataUpdateMessage(int trayId, int globalId, String itemId, String lot,
			int finalSortLocation, int locationID, short updateType) {
		int nextStartingPosition = 0;
        byte[] msgHeader = new byte[PLCConstants.MSG_HEADER_LEN];
        byte[] msg = new byte[PLCConstants.PLC_BAG_DATA_UPDATE_MSG_BODY_LEN + PLCConstants.MSG_HEADER_LEN];
        
        short mwSeqNum = 0;
        short mwMsgVer = 1; 
        // Build the header and add to the msg[]
        msgHeader = PLCMessageUtil.buildMessageHeader(PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE_INT,
                PLCConstants.PLC_BAG_DATA_UPDATE_MSG_BODY_LEN, PLCConstants.EBSC_EQUIPID, mwSeqNum, mwMsgVer);
        System.arraycopy(msgHeader, 0, msg, nextStartingPosition, msgHeader.length);

        // Build the body

        // add TrayId to the message
        nextStartingPosition = msgHeader.length;
        byte[] bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(trayId).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add globalId to the message
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(globalId).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add itemId char[12]
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        char[] itemIdArray = PLCMessageUtil.convertSringToFixSizeCharArray(itemId, PLCConstants.BAG_BARCODE_LEN);
        bytes = new String(itemIdArray).getBytes();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add flightNumber char[9]
        nextStartingPosition += itemIdArray.length;
        char[] flightNumberArray = PLCMessageUtil.convertSringToFixSizeCharArray(lot, PLCConstants.FLIGHT_NUMBER_LEN);
        bytes = new String(flightNumberArray).getBytes();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add Final sort location
        nextStartingPosition += flightNumberArray.length;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(finalSortLocation).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add location ID
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_DWORD_LEN).putInt(locationID).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add update type
        nextStartingPosition += PLCConstants.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(PLCConstants.MSG_WORD_LEN).putShort(updateType).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);
        return msg;
	}
}
