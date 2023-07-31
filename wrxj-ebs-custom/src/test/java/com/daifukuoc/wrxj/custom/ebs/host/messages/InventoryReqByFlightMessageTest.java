package com.daifukuoc.wrxj.custom.ebs.host.messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryReqByFlightMessageTest {

    // 42,1000,55,2222,3,4,5,0,111,222,20221212010101,0
    private static final String MESSAGE_LEN = "42";
    private static final String SEQ_NUM = "1000";
    private static final String MESSAGE_TYPE = "55";
    private static final String EQUIPMENT_ID = "2222";
    private static final String HOUR = "3";
    private static final String MIN = "4";
    private static final String MSEC = "5";
    private static final String MESSAGE_VERSION = "1";
    private static final String HEADER1 = MESSAGE_LEN + "," + SEQ_NUM + "," + MESSAGE_TYPE + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String HEADER2 = MESSAGE_LEN + "," + SEQ_NUM + "," + (Integer.parseInt(MESSAGE_TYPE) + 1) + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String VALID_MSG_ORDER_ID = "111";
    private static final String INVALID_MSG_ORDER_ID = "XXX";
    private static final String VALID_MSG_FLIGHT_NUMBER = "FL100";
    private static final String VALID_MSG_FLIGHT_SCHEDULED_DATETIME = "20221201001122";
    private static final String INVALID_MSG_FLIGHT_SCHEDULED_DATETIME = "20221201999999";

    @Test
    void shouldParseValidCSVFormatString() {
        InventoryReqByFlightMessageData data = new InventoryReqByFlightMessageData();
        String messageToTest = HEADER1 + "," + VALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + ","
                + VALID_MSG_FLIGHT_SCHEDULED_DATETIME;
        assertTrue(data.parse(messageToTest));
        
        assertEquals(MESSAGE_LEN, String.valueOf(data.getHeader().getMsgLength()));
        assertEquals(SEQ_NUM, String.valueOf(data.getHeader().getSeqNo()));
        assertEquals(MESSAGE_TYPE, String.valueOf(data.getHeader().getMsgType()));
        assertEquals(EQUIPMENT_ID, String.valueOf(data.getHeader().getEquipmentID()));
        assertEquals(HOUR, String.valueOf(data.getHeader().getHours()));
        assertEquals(MIN, String.valueOf(data.getHeader().getMinutes()));
        assertEquals(MSEC, String.valueOf(data.getHeader().getMilliSeconds()));
        assertEquals(MESSAGE_VERSION, String.valueOf(data.getHeader().getMsgVersion()));
        
        assertEquals(VALID_MSG_ORDER_ID, data.getRequestId());
        assertEquals(VALID_MSG_FLIGHT_NUMBER, data.getLot());
        assertEquals(VALID_MSG_FLIGHT_SCHEDULED_DATETIME, data.getFlightScheduledDateTime());
        assertEquals(messageToTest, data.getReceivedMessage());
        assertTrue(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsNull() {
    	InventoryReqByFlightMessageData data = new InventoryReqByFlightMessageData();
        assertFalse(data.parse(null));
        assertEquals(Strings.EMPTY, data.getRequestId());
        assertEquals(Strings.EMPTY, data.getLot());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(null, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsEmpty() {
    	InventoryReqByFlightMessageData data = new InventoryReqByFlightMessageData();
        assertFalse(data.parse(Strings.EMPTY));
        assertEquals(Strings.EMPTY, data.getRequestId());
        assertEquals(Strings.EMPTY, data.getLot());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(Strings.EMPTY, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsTooShort() {
    	InventoryReqByFlightMessageData data = new InventoryReqByFlightMessageData();
        String messageToTest = HEADER1; // No fields in body
        assertFalse(data.parse(messageToTest));
        assertEquals(Strings.EMPTY, data.getRequestId());
        assertEquals(Strings.EMPTY, data.getLot());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(messageToTest, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfAnyFieldIsInvalid() {
    	InventoryReqByFlightMessageData data = new InventoryReqByFlightMessageData();

        // Order id is empty
        assertFalse(data.parse(HEADER1 + "," + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME
               ));
        assertFalse(data.isValid());

        // Flight number is empty
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_ORDER_ID + "," + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME
                ));
        assertFalse(data.isValid());

        // Flight scheduled datetime is empty
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + "," + ","
                ));
        assertFalse(data.isValid());

        // Number of bags to retrieve is empty
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + ","
               ));
        assertFalse(data.isValid());

        // Order id is invalid
        assertFalse(data.parse(HEADER1 + "," + INVALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + ","
                + VALID_MSG_FLIGHT_SCHEDULED_DATETIME ));
        assertFalse(data.isValid());

        // Flight scheduled datetime id is invalid
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + ","
                + INVALID_MSG_FLIGHT_SCHEDULED_DATETIME ));
        assertFalse(data.isValid());

    }

    @Test
    void shouldReturnTrueIfTheSameMessageIsParsedAndCompared() {
        String messageToTest = HEADER1 + "," + VALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + ","
                + VALID_MSG_FLIGHT_SCHEDULED_DATETIME;

        InventoryReqByFlightMessageData data1 = new InventoryReqByFlightMessageData();
        data1.parse(messageToTest);

        InventoryReqByFlightMessageData data2 = new InventoryReqByFlightMessageData();
        data2.parse(messageToTest);

        assertTrue(data1.equals(data2));
    }

    @Test
    void shouldReturnFalseIfTheDifferentMessagesAreParsedAndCompared() {
        String messageToTest1 = HEADER1 + "," + VALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + ","
                + VALID_MSG_FLIGHT_SCHEDULED_DATETIME ;
        String messageToTest2 = HEADER2 + "," + VALID_MSG_ORDER_ID + "," + VALID_MSG_FLIGHT_NUMBER + ","
                + VALID_MSG_FLIGHT_SCHEDULED_DATETIME ;

        InventoryReqByFlightMessageData data1 = new InventoryReqByFlightMessageData();
        data1.parse(messageToTest1);

        InventoryReqByFlightMessageData data2 = new InventoryReqByFlightMessageData();
        data2.parse(messageToTest2);

        assertFalse(data1.equals(data2));
    }
}
