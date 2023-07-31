package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.host.messages.MessageHelper;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

public class MessageUtil {
    private static Logger mpLogger = Logger.getLogger();

    public static final String HOST_INBOUND_MESSAGE_DELIMITER = ",";
    public static final String HOST_OUTBOUND_MESSAGE_DELIMITER = ";";
    public static final int EQUIPMENT_ID = 8001;
    public static final int VERSION_NUMBER = 1;
    public static final String BAG_ID_FORMAT = "%-" + SACControlMessage.BAG_BARCODE_LEN + "s";
    public static final String DATE_FORMATE = "yyyyMMddHHmmss";
    
    public static String formatDate(Date date)
    {
    	String sFormattedDate = null;
    	try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMATE);
			sFormattedDate = dateFormat.format(date);

		} catch (Exception e) {
       
        }
    	
    	return sFormattedDate;
    }
    public static byte[] convertToByteArray(short sVal) {
        byte[] ret = new byte[2];
        ret[0] = (byte) ((sVal >> 8) & 0xff);
        ret[1] = (byte) (sVal & 0xff);
        return ret;
    }

    public static int buildShort(byte high, byte low) {
        return ((0xFF & (int) high) * 256) + ((0xFF & (int) low));
    }

    public static String encodeHexString(byte[] byteArray) {
        return encodeHexString(byteArray, 0, byteArray.length);
    }

    public static String encodeHexString(byte[] byteArray, int offset, int length) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = offset; i < offset + length; i++) {
            if (i > 0) {
                hexStringBuffer.append(" ");
            }
            hexStringBuffer.append("0x").append(byteToHex(byteArray[i]).toUpperCase());
        }
        return hexStringBuffer.toString();
    }

    public static String encodeHexString(byte value) {
        byte[] tmpByteArray = new byte[1];
        tmpByteArray[0] = value;
        return encodeHexString(tmpByteArray);
    }

    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    // https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    // s must be an even-length string
    // "00 01 02"
    // "000102"
    public static byte[] hexStringToByteArray(String s) {
        s = s.replaceAll("0x", "").replaceAll(" ", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] buildMessageHeader(short length, int sequenceNumber, short type, int equimpmentId,
            short version) {
        // Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.MSG_HEADER_LEN);

        buffer.putShort(length);

        buffer.putShort((short) sequenceNumber);

        buffer.putShort(type);

        buffer.putInt(equimpmentId);

        LocalTime localTime = LocalTime.now();

        buffer.put((byte) localTime.getHour());

        buffer.put((byte) localTime.getMinute());

        // FIXME signed short can't store 59999
        buffer.putShort((short) ((localTime.getSecond() * 1000) + localTime.get(ChronoField.MILLI_OF_SECOND)));

        buffer.putShort(version);

        return buffer.array();
    }

    public static byte[] buildKeepAliveMessage(int equimpmentId, int seqNum, short msgVersion) {
        // Create Keep Alive message
        byte[] mabKeepAliveResponse = new byte[SACControlMessage.KEEPALIVE_MSG_LEN];
        byte[] mabMsgHeader = new byte[SACControlMessage.MSG_HEADER_LEN];

        // Get the header TODO: Sort out Equipment ID for PLC1 - 3 .....
        mabMsgHeader = buildMessageHeader(SACControlMessage.KEEPALIVE_MSG_LEN, seqNum,
                SACControlMessage.KEEPALIVE_MSG_TYPE, equimpmentId, msgVersion);
        System.arraycopy(mabMsgHeader, 0, mabKeepAliveResponse, 0, mabMsgHeader.length);

        // Build the Message Body
        byte[] bytes = ByteBuffer.allocate(SACControlMessage.KEEPALIVE_MSG_BODY_LEN)
                .putShort(SACControlMessage.KEEPALIVE_MSG_ACTIVE_VAL).array();
        System.arraycopy(bytes, 0, mabKeepAliveResponse, mabMsgHeader.length, SACControlMessage.KEEPALIVE_MSG_BODY_LEN);

        return mabKeepAliveResponse;
    }

    public static byte[] buildExpectedReceiptResponseMessage(String converted) {
        // See ExpectedReceiptResponseMessage
        // 0: 32767
        // 1: ExpectedReceiptCompleteMessage
        // 2: 2, message type
        // 3: 1234, order id
        // 4: 115, tray id/load id
        // 5: 115115, global id
        // 6: ABCDEFG115, line id/bag id
        // 7: 6111, entrance station id
        // 8: 1, status
        byte[] mbMsg = new byte[SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_LEN];
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 9) {
            mpLogger.logError("Failed to build expected receipt response message because it's not valid: " + converted);
            return mbMsg;
        }

        // Initialize the byte array with 0x00
        Arrays.fill(mbMsg, (byte) 0x00);

        // header
        // TODO: get the msg version from a config
        byte[] header = buildMessageHeader(SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);

        int nextStartingPosition = 0;
        System.arraycopy(header, 0, mbMsg, nextStartingPosition, header.length);

        // order id
        nextStartingPosition += header.length;
        byte[] bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[3]))
                .array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);
        // tray id
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[4]))
                .array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);
        // global id
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[5]))
                .array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);
        // bag id(fixed 12 bytes)
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = String.format(BAG_ID_FORMAT, split[6]).getBytes();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);
        // entrance station id
        nextStartingPosition += SACControlMessage.BAG_BARCODE_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[7]))
                .array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);
        // status
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort(Short.parseShort(split[8]))
                .array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);

        return mbMsg;
    }

    // DK:28372
    /**
     * This method builds the store complete notify message to host
     * 
     * @param vpWrxToHostData
     * @return
     */
    public static byte[] buildStoreCompleteNotifyMessage(String converted) {
        byte[] mbMsg = new byte[SACControlMessage.StoreCompletionNotify.MSG_LEN];
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 10) {
            mpLogger.logError("Failed to build store complete notify message because it's not valid: " + converted);
            return mbMsg;
        }

        // Header
        // TODO: get the msg version from a config
        byte[] header = buildMessageHeader(SACControlMessage.StoreCompletionNotify.MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.StoreCompletionNotify.MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);

        int nextStartingPosition = 0;
        System.arraycopy(header, 0, mbMsg, nextStartingPosition, header.length);

        // Order id
        nextStartingPosition += header.length;
        byte[] bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[3])).array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);

        // Tray Id
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[4])).array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);

        // Global id
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[5])).array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);

        // iBagBarcode char[12]
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = String.format(BAG_ID_FORMAT, split[6]).getBytes();        
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);

        // Zone Id
        nextStartingPosition += bytes.length;
        if (split[7] != null && split[7].trim().length() > 0) {
            bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort(Short.parseShort(split[7])).array();
            System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);
        } else {
            bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort((short) 0).array();
            System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);
        }

        // Storage location id
        nextStartingPosition += SACControlMessage.MSG_WORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[8])).array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);

        // Status
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort(Short.parseShort(split[9])).array();
        System.arraycopy(bytes, 0, mbMsg, nextStartingPosition, bytes.length);

        return mbMsg;
    }

    /*
     * This method will create the SAC message from WCS side it content message header and body ex: (head :<6>,
     * body:<iGlobalId;iTrayId;iBagBarcode;iOutboundStationId>)
     * 
     * @return byte[] message to SAC
     */
    public static byte[] buildItemReleaseResponseMessage(String converted) {
        byte[] msg = new byte[SACControlMessage.ITEM_RELEASE_MSG_LEN];
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 9) {
            mpLogger.logError("Failed to build item released response message because it's not valid: " + converted);
            return msg;
        }

        byte[] msgHeader = new byte[SACControlMessage.MSG_HEADER_LEN];

        // Header
        // TODO: get the msg version from a config
        msgHeader = buildMessageHeader(SACControlMessage.ITEM_RELEASE_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.ITEM_RELEASE_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);

        // Build the header and add to the msg []
        int nextStartingPosition = 0;
        System.arraycopy(msgHeader, 0, msg, nextStartingPosition, msgHeader.length);

        // add iOrderId to the message
        nextStartingPosition = msgHeader.length;// 14
        byte[] bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN)
                .putInt(Integer.parseInt(split[3])).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);
        
        // add iTrayId to the message
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[4]))
                .array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);
        
        // add iGlobalId to the message
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;// 14
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN)
                .putInt(Integer.parseInt(split[5])).array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add iBagBarcode char[12]
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = String.format(BAG_ID_FORMAT, split[6]).getBytes();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // add iOutbound Station Id to the array
        nextStartingPosition += bytes.length;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN).putInt(Integer.parseInt(split[7]))
                .array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);
        
        // add status to the message
        nextStartingPosition += SACControlMessage.MSG_DWORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort(Short.parseShort(split[8]))
                .array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        return msg;
    }

    /*
     * http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-messages/retrieval-order-2-response.html
     * 
     * @return byte[] message to SAC
     */
    public static byte[] buildRetrievalOrderResponseMessage(String converted) {
        byte[] msg = new byte[SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_LEN];
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 6) {
            mpLogger.logError("Failed to build retrieval order response message because it's not valid: " + converted);
            return msg;
        }

        // TODO: get the msg version from a config
        byte[] header = buildMessageHeader(SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);

        // Initialize the byte array with 0x00
        Arrays.fill(msg, (byte) 0x00);

        // Header
        int nextStartingPosition = 0;
        System.arraycopy(header, 0, msg, nextStartingPosition, header.length);

        // Body

        // order id
        nextStartingPosition += header.length;
        byte[] bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort(Short.parseShort(split[3]))
                .array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // status
        nextStartingPosition += SACControlMessage.MSG_WORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort(Short.parseShort(split[4]))
                .array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        // numberOfMissingBags
        nextStartingPosition += SACControlMessage.MSG_WORD_LEN;
        bytes = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN).putShort(Short.parseShort(split[5]))
                .array();
        System.arraycopy(bytes, 0, msg, nextStartingPosition, bytes.length);

        return msg;
    }

	   /*
     * This method will create the SAC message from WCS side for Retrieval Order
     * Response, refer :
     * http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-
     * messages/retrieval-order-2.html header and body ex: (head
     * :<25>,body:<iOrderId;iStatus;iArryLenght;sArrayOfMissingBags>) *
     * 
     * @return byte[] message to SAC
     */
    static public byte[] buildRetrievalItemResponseMessage(String vpWrxToHostData) {

        int nextStartingPosition = 0;
        String missingBagsStr = "";
        String[] commaSeparated = vpWrxToHostData.split(";");
        byte[] msg = new byte[0];
        byte[] msgHeader = new byte[SACControlMessage.MSG_HEADER_LEN];

        if (commaSeparated.length > 5) {

            short responseType = Short.parseShort(commaSeparated[2]);
            if (responseType == SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE) {
                responseType = SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE;
            } else {
                responseType = SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE;
            }

            // add iOrderId to the message
            byte[] bytes1 = ByteBuffer.allocate(SACControlMessage.MSG_DWORD_LEN)
                    .putInt(Integer.parseInt(commaSeparated[3])).array();

            // add iStatus to the message
            byte[] bytes2 = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN)
                    .putShort(Short.parseShort(commaSeparated[4])).array();

            // add iLenghtOfarray
            byte[] bytes3 = ByteBuffer.allocate(SACControlMessage.MSG_WORD_LEN)
                    .putShort(Short.parseShort(commaSeparated[5])).array();

            // add iMissingbagArray Id to the array
            byte[] bytes4 = new byte[0];
            if (Integer.parseInt(commaSeparated[5]) > 0) {
                // get the length of the string and pass it as param to the next method
                missingBagsStr = commaSeparated[6];
                char[] missingBagsArray = convertSringToFixSizeCharArray(commaSeparated[6], missingBagsStr.length());
                bytes4 = new String(missingBagsArray).getBytes();
            } else {
                bytes4 = new String().getBytes();
            }
            short messageLength = (short) ((SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_LEN) + (Short.parseShort(commaSeparated[5])*20));

            msgHeader = buildMessageHeader(messageLength,Short.parseShort(commaSeparated[0])
                    , SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE, (short) EQUIPMENT_ID,(short) VERSION_NUMBER);// TODO: get the msg version
            
           int mmessageLength = (short) (msgHeader.length + bytes1.length
                    + bytes2.length + bytes3.length + bytes4.length);
            
            // from a config
            msg = new byte[mmessageLength];

            // Build the header and add to the msg []
            System.arraycopy(msgHeader, 0, msg, nextStartingPosition, msgHeader.length);

            nextStartingPosition = msgHeader.length;// 14
            System.arraycopy(bytes1, 0, msg, nextStartingPosition, bytes1.length);

            nextStartingPosition += SACControlMessage.MSG_WORD_LEN;
            System.arraycopy(bytes2, 0, msg, nextStartingPosition, bytes2.length);

            nextStartingPosition += SACControlMessage.MSG_WORD_LEN;
            System.arraycopy(bytes3, 0, msg, nextStartingPosition, bytes3.length);

            nextStartingPosition += SACControlMessage.MSG_WORD_LEN;
            System.arraycopy(bytes4, 0, msg, nextStartingPosition, bytes4.length);

        } else {
            mpLogger.logError("Invalid Retrieval ITEM Response notify msg:");
        }

        return msg;
    }
    
    static private byte[] convertInventoryResponseArrayItem( String sCommaSeparatedData,Integer arrayLen)
    {
    	if(sCommaSeparatedData != null && !sCommaSeparatedData.isBlank() && !sCommaSeparatedData.isEmpty() && arrayLen > 0)
    	{
    		String[] commaSeparated = sCommaSeparatedData.split(",");
    		byte[] mabMsg = new byte[arrayLen * SACControlMessage.INVENTORY_RESPONSE_ARRAY_ITEM_LEN];	
    		ByteBuffer buffer = ByteBuffer.wrap(mabMsg);
    		int index = 0;
    		for(int i=0;i < arrayLen ; i++)
    		{
				int trayId =  ( commaSeparated[index].isBlank() || commaSeparated[index].isEmpty() )? 0 :   Integer.parseInt(commaSeparated[index]); 
				buffer.putInt(trayId);
				//move index ahead
				index++;
				int globalId = ( commaSeparated[index].isBlank() || commaSeparated[index].isEmpty() )? 0 : Integer.parseInt(commaSeparated[index]); 
				buffer.putInt(globalId);
				index++;
				String itemId = commaSeparated[index];
				if ( commaSeparated[index].isBlank() || commaSeparated[index].isEmpty()  )
				{
					buffer.put(new String().getBytes());
				}else
				{
					buffer.put(String.format(BAG_ID_FORMAT,itemId).getBytes());
				}
				index++;
				String flightNum = commaSeparated[index];
				if ( commaSeparated[index].isBlank() || commaSeparated[index].isEmpty()  )
				{
					buffer.put(new String().getBytes());
				}else
				{
					buffer.put(flightNum.getBytes());
				}
				index++;
				String flightSTD = commaSeparated[index];
				if ( commaSeparated[index].isBlank() || commaSeparated[index].isEmpty()  )
				{
					buffer.put(new String().getBytes());
				}else
				{
					buffer.put(flightSTD.getBytes());
				}				
				index++;
				int locationId = ( commaSeparated[index].isBlank() || commaSeparated[index].isEmpty() )? 0 : Integer.parseInt(commaSeparated[index]); 
				buffer.putInt(locationId);
				index++;
				String warehouseID = commaSeparated[index];
				if ( commaSeparated[index].isBlank() || commaSeparated[index].isEmpty()  )
				{
					buffer.put(new String().getBytes());
				}else
				{
					buffer.put(warehouseID.getBytes());
				}
				index++;
    		}
    		return mabMsg;
    	}
    	
    	return new String().getBytes();
    }
    static public byte[] buildInventoryResponseMessage(String vpWrxToHostData) {

    	String[] commaSeparated = vpWrxToHostData.split(";");
    	byte[] mabMsg = null;
    	byte[] bagsArrayBytes = null;
    	 if (commaSeparated.length > 5) {
    		 
    		
    		 
    		 Integer arrayLen = Integer.parseInt(commaSeparated[5]);
    		 if (arrayLen > 0) {
                 bagsArrayBytes = convertInventoryResponseArrayItem(commaSeparated[6],arrayLen);
             } else {
            	 bagsArrayBytes = new String().getBytes();
             }

             short msgBodyLength = (short) ((SACControlMessage.INVENTORY_RESPONSE_MSG_LEN) + (arrayLen !=0 ? (arrayLen * SACControlMessage.INVENTORY_RESPONSE_ARRAY_ITEM_LEN) : (short)0));
             short msgLength = (short)(PLCConstants.MSG_HEADER_LEN + msgBodyLength + 8 );
             mabMsg = new byte[msgLength];	
             
             
             byte[] msgHeader = new byte[SACControlMessage.MSG_HEADER_LEN];
    		 msgHeader = buildMessageHeader(msgLength,0
                     , SACControlMessage.INVENTORY_RESPONSE_MSG_TYPE, (short) EQUIPMENT_ID,(short) VERSION_NUMBER);
             

             ByteBuffer buffer = ByteBuffer.wrap(mabMsg); 
             buffer.put(msgHeader); //header
    		 buffer.putInt(Integer.parseInt(commaSeparated[3])); //RequestID
    		 buffer.putShort(Short.parseShort(commaSeparated[4])); //Status flag
    		 buffer.putShort(Short.parseShort(commaSeparated[5])); //Array Length
             //array
             buffer.put(bagsArrayBytes);
    	 }
        return mabMsg;
    }
    
    /**
     * Converts provided string to fixed size char array.
     * 
     * @param st
     * @param requiredSize
     * @return Char array
     */
    private static char[] convertSringToFixSizeCharArray(String st, int requiredSize) {

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

    public static byte[] buildFlightDataUpdateResponseMessage(String converted) {
        // Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_LEN);
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 4) {
            mpLogger.logError("Failed to build flight data response message because it's not valid: " + converted);
            return buffer.array();
        }

        // Header
        byte[] header = buildMessageHeader(SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);
        buffer.put(header);

        // Body
        buffer.putShort(Short.parseShort(split[3]));

        return buffer.array();
    }
    
    public static byte[] buildInventoryUpdateResponseMessage(String converted) {
        // Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.INVENTORY_UPDATE_ACK_MSG_LEN);
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 4) {
            mpLogger.logError("Failed to build inventory update ack message because it's not valid: " + converted);
            return buffer.array();
        }

        // Header
        byte[] header = buildMessageHeader(SACControlMessage.INVENTORY_UPDATE_ACK_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.INVENTORY_UPDATE_ACK_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);
        buffer.put(header);

        // Body
        buffer.putShort(Short.parseShort(split[3]));

        return buffer.array();
    }

    public static byte[] buildExpectedReceiptAckMessage(String converted) {
        // Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_LEN);
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 4) {
            mpLogger.logError("Failed to build expected receipt acke message because it's not valid: " + converted);
            return buffer.array();
        }

        // Header
        byte[] header = buildMessageHeader(SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);
        buffer.put(header);

        // Body
        buffer.putShort(Short.parseShort(split[3]));

        return buffer.array();
    }
    
    public static byte[] buildRetrievalFlightAckMessage(String converted) {
        // Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_LEN);
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 4) {
            mpLogger.logError("Failed to build expected receipt acke message because it's not valid: " + converted);
            return buffer.array();
        }

        // Header
        byte[] header = buildMessageHeader(SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);
        buffer.put(header);

        // Body
        buffer.putShort(Short.parseShort(split[3]));

        return buffer.array();
    }
    
    public static byte[] buildInventoryRequestByWarehouseAckMessage(String converted) {
        // Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_LEN);
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 4) {
            mpLogger.logError("Failed to build inventory request by warehouse ack message because it's not valid: " + converted);
            return buffer.array();
        }

        // Header
        byte[] header = buildMessageHeader(SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);
        buffer.put(header);

        // Body
        buffer.putShort(Short.parseShort(split[3]));

        return buffer.array();
    }

    public static byte[] buildInventoryRequestByFlightAckMessage(String converted) {
        // Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_LEN);
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 4) {
            mpLogger.logError("Failed to build expected receipt acke message because it's not valid: " + converted);
            return buffer.array();
        }

        // Header
        byte[] header = buildMessageHeader(SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);
        buffer.put(header);

        // Body
        buffer.putShort(Short.parseShort(split[3]));

        return buffer.array();
    }
    
	public static byte[] buildRetrievalItemAckMessage(String converted) {
		// Create a byte buffer for header and body
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_LEN);
        String[] split = converted.split(HOST_OUTBOUND_MESSAGE_DELIMITER);
        if (split.length < 4) {
            mpLogger.logError("Failed to build expected receipt acke message because it's not valid: " + converted);
            return buffer.array();
        }

        // Header
        byte[] header = buildMessageHeader(SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_LEN,
                Short.parseShort(split[0]), SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE,
                EQUIPMENT_ID, (short) VERSION_NUMBER);
        buffer.put(header);

        // Body
        buffer.putShort(Short.parseShort(split[3]));

        return buffer.array();
	}
}
