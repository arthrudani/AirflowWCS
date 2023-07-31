package com.daifukuoc.wrxj.custom.ebs.host.messages;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpectedReceiptMessageDataTest {

    // 84,1000,52,2222,3,4,5,0,999,1002,10021002,BAG1002,FL100,20221213000000,20221213000000,3600,1,1
    private static final String MESSAGE_LEN = "84";
    private static final String SEQ_NUM = "1000";
    private static final String MESSAGE_TYPE = "52";
    private static final String EQUIPMENT_ID = "2222";
    private static final String HOUR = "3";
    private static final String MIN = "4";
    private static final String MSEC = "5";
    private static final String MESSAGE_VERSION = "1";
    private static final String HEADER1 = MESSAGE_LEN + "," + SEQ_NUM + "," + MESSAGE_TYPE + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String HEADER2 = MESSAGE_LEN + "," + SEQ_NUM + "," + (Integer.parseInt(MESSAGE_TYPE) + 1) + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String ORDER_ID = "999";
    private static final String LOAD_ID = "1002";
    private static final String GLOBAL_ID = "10021002";
    private static final String LINE_ID = "BAG1002";
    private static final String FLIGHT_NUMBER = "FL100";
    private static final String FLIGHT_SCHEDULED_DATETIME = "20221213000000";
    private static final String DEFAULT_RETRIEVAL_DATETIME = "20221213000000";
    private static final String FINAL_SORT_LOCATION = "3600";
    private static final String ITEM_TYPE = "1"; // SACControlMessage.Bag_On_Tray
    private static final String REQUEST_TYPE = "1";

    private static final String NON_NUMERIC_STRING = "ABC";
    private static final String INVALID_DATETIME_STRING = "20229999000000";

    private String prepareMessageForTesting(String... fields) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(fields[i]);
        }

        return buf.toString();
    }

    @Test
    void shouldParseWhenCSVFormatStringIsValid() {
        ExpectedReceiptMessageData data = new ExpectedReceiptMessageData();
        String messageToTest = prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE, REQUEST_TYPE);

        assertTrue(data.parse(messageToTest));
        
        assertEquals(MESSAGE_LEN, String.valueOf(data.getHeader().getMsgLength()));
        assertEquals(SEQ_NUM, String.valueOf(data.getHeader().getSeqNo()));
        assertEquals(MESSAGE_TYPE, String.valueOf(data.getHeader().getMsgType()));
        assertEquals(EQUIPMENT_ID, String.valueOf(data.getHeader().getEquipmentID()));
        assertEquals(HOUR, String.valueOf(data.getHeader().getHours()));
        assertEquals(MIN, String.valueOf(data.getHeader().getMinutes()));
        assertEquals(MSEC, String.valueOf(data.getHeader().getMilliSeconds()));
        assertEquals(MESSAGE_VERSION, String.valueOf(data.getHeader().getMsgVersion()));

        assertEquals(ORDER_ID, data.getOrderId());
        assertEquals(LOAD_ID, data.getLoadId());
        assertEquals(GLOBAL_ID, data.getGlobalId());
        assertEquals(LINE_ID, data.getLineId());
        assertEquals(FLIGHT_NUMBER, data.getLot());
        assertEquals(FLIGHT_SCHEDULED_DATETIME, data.getFlightScheduledDateTime());
        assertEquals(DEFAULT_RETRIEVAL_DATETIME, data.getDefaultRetrievalDateTime());
        assertEquals(FINAL_SORT_LOCATION, data.getFinalSortLocation());
        assertEquals(SACControlMessage.Bag_On_Tray, data.getItemType());
        assertEquals(Integer.parseInt(REQUEST_TYPE), data.getRequestType());
        assertEquals(messageToTest, data.getMessage());
        assertTrue(data.isValid());
    }

    @Test
    void shouldNotParseWhenCSVFormatStringIsNull() {
        ExpectedReceiptMessageData data = new ExpectedReceiptMessageData();

        assertFalse(data.parse(null));

        assertEquals(Strings.EMPTY, data.getOrderId());
        assertEquals(Strings.EMPTY, data.getLoadId());
        assertEquals(Strings.EMPTY, data.getGlobalId());
        assertEquals(Strings.EMPTY, data.getLineId());
        assertEquals(Strings.EMPTY, data.getLot());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(Strings.EMPTY, data.getDefaultRetrievalDateTime());
        assertEquals(Strings.EMPTY, data.getFinalSortLocation());
        assertEquals(Strings.EMPTY, data.getItemType());
        assertEquals(ExpectedReceiptMessageData.INVALID_REQUEST_TYPE, data.getRequestType());
        assertNull(data.getMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseWhenCSVFormatStringIsEmpty() {
        ExpectedReceiptMessageData data = new ExpectedReceiptMessageData();

        assertFalse(data.parse(Strings.EMPTY));

        assertEquals(Strings.EMPTY, data.getOrderId());
        assertEquals(Strings.EMPTY, data.getLoadId());
        assertEquals(Strings.EMPTY, data.getGlobalId());
        assertEquals(Strings.EMPTY, data.getLineId());
        assertEquals(Strings.EMPTY, data.getLot());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(Strings.EMPTY, data.getDefaultRetrievalDateTime());
        assertEquals(Strings.EMPTY, data.getFinalSortLocation());
        assertEquals(Strings.EMPTY, data.getItemType());
        assertEquals(ExpectedReceiptMessageData.INVALID_REQUEST_TYPE, data.getRequestType());
        assertEquals(Strings.EMPTY, data.getMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseWhenCSVFormatStringIsTooShort() {
        ExpectedReceiptMessageData data = new ExpectedReceiptMessageData();

        assertFalse(data.parse(HEADER1)); // No fields in message body

        assertEquals(Strings.EMPTY, data.getOrderId());
        assertEquals(Strings.EMPTY, data.getLoadId());
        assertEquals(Strings.EMPTY, data.getGlobalId());
        assertEquals(Strings.EMPTY, data.getLineId());
        assertEquals(Strings.EMPTY, data.getLot());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(Strings.EMPTY, data.getDefaultRetrievalDateTime());
        assertEquals(Strings.EMPTY, data.getFinalSortLocation());
        assertEquals(Strings.EMPTY, data.getItemType());
        assertEquals(ExpectedReceiptMessageData.INVALID_REQUEST_TYPE, data.getRequestType());
        assertEquals(HEADER1, data.getMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseWhenAnyFieldIsInvalid() {
        ExpectedReceiptMessageData data = new ExpectedReceiptMessageData();

        // Order id is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, Strings.EMPTY, LOAD_ID, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());

        // Order id is non-numeric
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, NON_NUMERIC_STRING, LOAD_ID, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());

        // Load id is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, Strings.EMPTY, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());

        // Load id is non-numeric
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, NON_NUMERIC_STRING, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());

        // Global id is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, Strings.EMPTY, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());

        // Global id is non-numeric
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, NON_NUMERIC_STRING, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());

        // Line id is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, Strings.EMPTY,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Flight number is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID,
                Strings.EMPTY, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Flight scheduled datetime is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, Strings.EMPTY, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Flight scheduled datetime is invalid
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, INVALID_DATETIME_STRING, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Default retrieval datetime is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, Strings.EMPTY, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Default retrieval datetime is invalid
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, INVALID_DATETIME_STRING, FINAL_SORT_LOCATION, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Final sort location is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, Strings.EMPTY, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, Strings.EMPTY, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());

        // Final sort location is non-numeric
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, NON_NUMERIC_STRING, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, NON_NUMERIC_STRING, ITEM_TYPE,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Item type is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, Strings.EMPTY, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, Strings.EMPTY,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Item type is non-numeric
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, Strings.EMPTY, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, NON_NUMERIC_STRING,
                REQUEST_TYPE)));
        assertFalse(data.isValid());
        
        // Request type is empty
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, Strings.EMPTY, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                Strings.EMPTY)));
        assertFalse(data.isValid());
        
        // Request type is non-numeric
        assertFalse(data.parse(prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, Strings.EMPTY, LINE_ID,
                FLIGHT_NUMBER, FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE,
                NON_NUMERIC_STRING)));
        assertFalse(data.isValid());
    }

    @Test
    void shouldReturnTrueIfTheSameMessageIsParsedAndCompared() {
        String messageToTest = prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE, REQUEST_TYPE);

        RetrievalOrderMessageData data1 = new RetrievalOrderMessageData();
        data1.parse(messageToTest);

        RetrievalOrderMessageData data2 = new RetrievalOrderMessageData();
        data2.parse(messageToTest);

        assertTrue(data1.equals(data2));
    }

    @Test
    void shouldReturnFalseIfTheDifferentMessagesAreParsedAndCompared() {
        String messageToTest1 = prepareMessageForTesting(HEADER1, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE, REQUEST_TYPE);
        String messageToTest2 = prepareMessageForTesting(HEADER2, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE, REQUEST_TYPE);

        RetrievalOrderMessageData data1 = new RetrievalOrderMessageData();
        data1.parse(messageToTest1);

        RetrievalOrderMessageData data2 = new RetrievalOrderMessageData();
        data2.parse(messageToTest2);

        assertFalse(data1.equals(data2));
    }
}
