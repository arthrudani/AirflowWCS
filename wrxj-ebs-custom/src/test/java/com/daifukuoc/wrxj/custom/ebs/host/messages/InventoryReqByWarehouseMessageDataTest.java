package com.daifukuoc.wrxj.custom.ebs.host.messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryReqByWarehouseMessageDataTest {

    // 42,1000,55,2222,3,4,5,0,111,222,20221212010101,0
    private static final String MESSAGE_LEN = "23";
    private static final String SEQ_NUM = "1000";
    private static final String MESSAGE_TYPE = "59";
    private static final String EQUIPMENT_ID = "2222";
    private static final String HOUR = "3";
    private static final String MIN = "4";
    private static final String MSEC = "5";
    private static final String MESSAGE_VERSION = "1";
    private static final String HEADER1 = MESSAGE_LEN + "," + SEQ_NUM + "," + MESSAGE_TYPE + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String HEADER2 = MESSAGE_LEN + "," + SEQ_NUM + "," + (Integer.parseInt(MESSAGE_TYPE) + 1) + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String VALID_MSG_REQUEST_ID = "111";
    private static final String INVALID_MSG_REQUEST_ID = "XXX";
    private static final String VALID_MSG_WAREHOUSE_ID = "EBS";

    @Test
    void shouldParseValidCSVFormatString() {
        InventoryReqByWarehouseMessageData data = new InventoryReqByWarehouseMessageData();
        String messageToTest = HEADER1 + "," + VALID_MSG_REQUEST_ID + "," + VALID_MSG_WAREHOUSE_ID;
        assertTrue(data.parse(messageToTest));
        
        assertEquals(MESSAGE_LEN, String.valueOf(data.getHeader().getMsgLength()));
        assertEquals(SEQ_NUM, String.valueOf(data.getHeader().getSeqNo()));
        assertEquals(MESSAGE_TYPE, String.valueOf(data.getHeader().getMsgType()));
        assertEquals(EQUIPMENT_ID, String.valueOf(data.getHeader().getEquipmentID()));
        assertEquals(HOUR, String.valueOf(data.getHeader().getHours()));
        assertEquals(MIN, String.valueOf(data.getHeader().getMinutes()));
        assertEquals(MSEC, String.valueOf(data.getHeader().getMilliSeconds()));
        assertEquals(MESSAGE_VERSION, String.valueOf(data.getHeader().getMsgVersion()));
        
        assertEquals(VALID_MSG_REQUEST_ID, data.getRequestID());
        assertEquals(VALID_MSG_WAREHOUSE_ID, data.getWarehouseID());
        assertEquals(messageToTest, data.getReceivedMessage());
        assertTrue(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsNull() {
    	InventoryReqByWarehouseMessageData data = new InventoryReqByWarehouseMessageData();
        assertFalse(data.parse(null));
        assertEquals(Strings.EMPTY, data.getWarehouseID());
        assertEquals(Strings.EMPTY, data.getRequestID());
        assertEquals(null, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsEmpty() {
    	InventoryReqByWarehouseMessageData data = new InventoryReqByWarehouseMessageData();
        assertFalse(data.parse(Strings.EMPTY));
        assertEquals(Strings.EMPTY, data.getWarehouseID());
        assertEquals(Strings.EMPTY, data.getRequestID());
        assertEquals(Strings.EMPTY, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsTooShort() {
    	InventoryReqByWarehouseMessageData data = new InventoryReqByWarehouseMessageData();
        String messageToTest = HEADER1; // No fields in body
        assertFalse(data.parse(messageToTest));
        assertEquals(Strings.EMPTY, data.getWarehouseID());
        assertEquals(Strings.EMPTY, data.getRequestID());
        assertEquals(messageToTest, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfAnyFieldIsInvalid() {
    	InventoryReqByWarehouseMessageData data = new InventoryReqByWarehouseMessageData();

        // request id is empty
        assertFalse(data.parse(HEADER1 + "," + "," + "," + VALID_MSG_WAREHOUSE_ID));
        assertFalse(data.isValid());

        // warehouse id is empty
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_REQUEST_ID + ","));
        assertFalse(data.isValid());

        // request id is invalid
        assertFalse(data.parse(HEADER1 + "," + INVALID_MSG_REQUEST_ID + "," + VALID_MSG_WAREHOUSE_ID ));
        assertFalse(data.isValid());

    }

    @Test
    void shouldReturnTrueIfTheSameMessageIsParsedAndCompared() {
        String messageToTest = HEADER1 + "," + VALID_MSG_REQUEST_ID + "," + VALID_MSG_WAREHOUSE_ID;

        InventoryReqByWarehouseMessageData data1 = new InventoryReqByWarehouseMessageData();
        data1.parse(messageToTest);

        InventoryReqByWarehouseMessageData data2 = new InventoryReqByWarehouseMessageData();
        data2.parse(messageToTest);

        assertTrue(data1.equals(data2));
    }

}
