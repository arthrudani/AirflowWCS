package com.daifukuoc.wrxj.custom.ebs.host.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

import java.nio.ByteBuffer;
import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SACMessageManagerTest {
    
    private static final int SEQ_NUM = 32767;
    private static final int NOW_HOUR = 10;
    private static final int NOW_MINUTE = 20;
    private static final int NOW_SECOND = 1;
    private static final LocalTime NOW = LocalTime.of(NOW_HOUR, NOW_MINUTE, NOW_SECOND);
    private static final short ACTIVE_FLAG = 1;
    
    private static final String VALID_EXPECTED_RECEIPT_MESSAGE     = "84,1000,52,2222,3,4,5,0,999,1002,10021002,BAG1002,FL100,20221213000000,20221213000000,3600,1,1";
    private static final String INVALID_EXPECTED_RECEIPT_MESSAGE   = "84,1000,52,2222,3,4,5,0,999,1002,10021002,BAG1002,FL100,20229999000000,20221213000000,3600,1,1";
    private static final String VALID_RETRIEVAL_ORDER_MESSAGE      = "44,1000,55,2222,3,4,5,0,999,FL100,20221213000000,0";
    private static final String INVALID_RETRIEVAL_ORDER_MESSAGE    = "44,1000,55,2222,3,4,5,0,999,FL100,20221213999999,0";
    private static final String VALID_FLIGHT_DATA_UPDATE_MESSAGE   = "56,1000,57,2222,3,4,5,0,FL100,20221201001122,20221202112233,3700";
    private static final String INVALID_FLIGHT_DATA_UPDATE_MESSAGE = "56,1000,57,2222,3,4,5,0,FL100,20229999001122,20221202112233,3700";
    private static final String VALID_INV_REQ_BY_WH_MESSAGE   = "23,0,59,0,0,0,0,0,11,EBS";
    private static final String INVALID_INV_REQ_BY_WH_MESSAGE = "23,0,59,0,0,0,0,0,13,EBS";

    MockedStatic<LocalTime> mockedLocalTime;
    
    SACMessageManager sACMessageManager;

    @BeforeEach
    void setUp() throws Exception {
        mockedLocalTime = Mockito.mockStatic(LocalTime.class);
        mockedLocalTime.when(() -> LocalTime.now()).thenReturn(NOW);
        
        sACMessageManager = new SACMessageManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedLocalTime.close();
    }
    
    @Test
    void shouldReturnBuiltKeepAliveMessageWhenCreateKeepAliveMessageIsCalled() {
        byte[] builtKeepAliveMessage = sACMessageManager.createKeepAliveMessage(MessageUtil.EQUIPMENT_ID, SEQ_NUM);
        
        ByteBuffer builtBuffer = ByteBuffer.wrap(builtKeepAliveMessage);        
        short messageLength = builtBuffer.getShort();
        int sequenceNumber = Short.toUnsignedInt(builtBuffer.getShort());
        short messageType = builtBuffer.getShort();
        int equipmentId = builtBuffer.getInt();
        byte hours = builtBuffer.get();
        byte minutes = builtBuffer.get();
        short mSeconds = builtBuffer.getShort();
        short versionNumber = builtBuffer.getShort();
        short activeFlag = builtBuffer.getShort();
        
        assertEquals(SACControlMessage.KEEPALIVE_MSG_LEN, messageLength);
        assertEquals(SEQ_NUM, sequenceNumber);
        assertEquals(SACControlMessage.KEEPALIVE_MSG_TYPE, messageType);
        assertEquals(MessageUtil.EQUIPMENT_ID, equipmentId);
        assertEquals(NOW_HOUR, hours);
        assertEquals(NOW_MINUTE, minutes);
        // FIXME signed short can't store 59999
        assertEquals(NOW_SECOND, mSeconds / 1000);
        assertEquals((short) MessageUtil.VERSION_NUMBER, versionNumber);
        assertEquals(ACTIVE_FLAG, activeFlag);
    }

    @Test
    void shouldReturnFalseWhenNoMessageIsSet() {
        assertFalse(sACMessageManager.setMessage(null));
        assertFalse(sACMessageManager.processReceivedMessage());
    }

    @Test
    void shouldReturnTrueWhenValidExpectedReceiptMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedExpectedReceiptMessageToByteArray(VALID_EXPECTED_RECEIPT_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());
        assertEquals(VALID_EXPECTED_RECEIPT_MESSAGE, sACMessageManager.getMessageTxt());
    }

    @Test
    void shouldReturnTrueWhenInvalidExpectedReceiptMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedExpectedReceiptMessageToByteArray(INVALID_EXPECTED_RECEIPT_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());        
    }
    
    @Test
    void shouldReturnTrueWhenValidRetrievalOrderMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedRetrievalOrderWithoutListMessageToByteArray(VALID_RETRIEVAL_ORDER_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());
        assertEquals(VALID_RETRIEVAL_ORDER_MESSAGE, sACMessageManager.getMessageTxt());
    }

    @Test
    void shouldReturnTrueWhenInvalidRetrievalOrderMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedRetrievalOrderWithoutListMessageToByteArray(INVALID_RETRIEVAL_ORDER_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());        
    }

    @Test
    void shouldReturnTrueWhenValidFlightDataUpdateMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedFlightDataUpdateMessageToByteArray(VALID_FLIGHT_DATA_UPDATE_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());
        assertEquals(VALID_FLIGHT_DATA_UPDATE_MESSAGE, sACMessageManager.getMessageTxt());
    }

    @Test
    void shouldReturnTrueEvenWhenInvalidFlightDataUpdateMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedFlightDataUpdateMessageToByteArray(INVALID_FLIGHT_DATA_UPDATE_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());
    }
    
    @Test
    void shouldReturnTrueWhenValidInvReqByWHMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedInvReqByWHMessageToByteArray(VALID_INV_REQ_BY_WH_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());
        assertEquals(VALID_INV_REQ_BY_WH_MESSAGE, sACMessageManager.getMessageTxt());
    }

    @Test
    void shouldReturnTrueEvenWhenInvalidInvReqByWHMessageIsSet() {
        assertTrue(sACMessageManager
                .setMessage(convertCSVFormattedInvReqByWHMessageToByteArray(INVALID_INV_REQ_BY_WH_MESSAGE)));
        assertTrue(sACMessageManager.processReceivedMessage());
    }

    

	private byte[] convertCSVFormattedExpectedReceiptMessageToByteArray(String csvFormattedMessage) {
        String[] split = csvFormattedMessage.split(",");
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.EXPECTED_RECIEPT_MSG_LEN);
        // Header
        buffer.putShort(Short.parseShort(split[0]));
        buffer.putShort(Short.parseShort(split[1]));
        buffer.putShort(Short.parseShort(split[2]));
        buffer.putInt(Integer.parseInt(split[3]));
        buffer.put(Byte.parseByte(split[4]));
        buffer.put(Byte.parseByte(split[5]));
        buffer.putShort(Short.parseShort(split[6]));
        buffer.putShort(Short.parseShort(split[7]));
        // Body
        buffer.putInt(Integer.parseInt(split[8]));
        buffer.putInt(Integer.parseInt(split[9]));
        buffer.putInt(Integer.parseInt(split[10]));
        buffer.put(String.format("%-12s", split[11]).getBytes());
        buffer.put(String.format("%-8s", split[12]).getBytes());
        buffer.put(String.format("%-14s", split[13]).getBytes());
        buffer.put(String.format("%-14s", split[14]).getBytes());
        buffer.putInt(Integer.parseInt(split[15]));
        buffer.putShort(Short.parseShort(split[16]));
        buffer.putShort(Short.parseShort(split[17]));

        return buffer.array();
    }
    
    private byte[] convertCSVFormattedRetrievalOrderWithoutListMessageToByteArray(String csvFormattedMessage) {
        String[] split = csvFormattedMessage.split(",");
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.RETRIEVAL_FLIGHT_WITHOUT_LIST_REQUEST_MSG_LEN);
        // Header
        buffer.putShort(Short.parseShort(split[0]));
        buffer.putShort(Short.parseShort(split[1]));
        buffer.putShort(Short.parseShort(split[2]));
        buffer.putInt(Integer.parseInt(split[3]));
        buffer.put(Byte.parseByte(split[4]));
        buffer.put(Byte.parseByte(split[5]));
        buffer.putShort(Short.parseShort(split[6]));
        buffer.putShort(Short.parseShort(split[7]));
        // Body
        buffer.putInt(Integer.parseInt(split[8]));
        buffer.put(String.format("%-8s", split[9]).getBytes());
        buffer.put(String.format("%-14s", split[10]).getBytes());
        buffer.putShort(Short.parseShort(split[11]));

        return buffer.array();
    }

    private byte[] convertCSVFormattedFlightDataUpdateMessageToByteArray(String csvFormattedMessage) {
        String[] split = csvFormattedMessage.split(",");
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.FLIGHT_DATA_UPDATE_MSG_LEN);
        // Header
        buffer.putShort(Short.parseShort(split[0]));
        buffer.putShort(Short.parseShort(split[1]));
        buffer.putShort(Short.parseShort(split[2]));
        buffer.putInt(Integer.parseInt(split[3]));
        buffer.put(Byte.parseByte(split[4]));
        buffer.put(Byte.parseByte(split[5]));
        buffer.putShort(Short.parseShort(split[6]));
        buffer.putShort(Short.parseShort(split[7]));
        // Body
        buffer.put(String.format("%-8s", split[8]).getBytes());
        buffer.put(String.format("%-14s", split[9]).getBytes());
        buffer.put(String.format("%-14s", split[10]).getBytes());
        buffer.putInt(Integer.parseInt(split[11]));
        return buffer.array();
    }
    
    private byte[] convertCSVFormattedInvReqByWHMessageToByteArray(String csvFormattedMessage) {
        String[] split = csvFormattedMessage.split(",");
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_LEN);
        // Header
        buffer.putShort(Short.parseShort(split[0]));
        buffer.putShort(Short.parseShort(split[1]));
        buffer.putShort(Short.parseShort(split[2]));
        buffer.putInt(Integer.parseInt(split[3]));
        buffer.put(Byte.parseByte(split[4]));
        buffer.put(Byte.parseByte(split[5]));
        buffer.putShort(Short.parseShort(split[6]));
        buffer.putShort(Short.parseShort(split[7]));
        // Body
        buffer.putInt(Integer.parseInt(split[8]));
        buffer.put(String.format("%-3s", split[9]).getBytes());
        return buffer.array();
    }
    
}
