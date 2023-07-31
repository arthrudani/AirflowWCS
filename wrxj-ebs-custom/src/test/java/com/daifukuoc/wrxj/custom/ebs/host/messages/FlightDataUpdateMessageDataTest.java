package com.daifukuoc.wrxj.custom.ebs.host.messages;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlightDataUpdateMessageDataTest {

    private static final String MESSAGE_LEN = "56";
    private static final String SEQ_NUM = "1000";
    private static final String MESSAGE_TYPE = "57";
    private static final String EQUIPMENT_ID = "2222";
    private static final String HOUR = "3";
    private static final String MIN = "4";
    private static final String MSEC = "5";
    private static final String MESSAGE_VERSION = "1";
    private static final String HEADER1 = MESSAGE_LEN + "," + SEQ_NUM + "," + MESSAGE_TYPE + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String HEADER2 = MESSAGE_LEN + "," + SEQ_NUM + "," + (Integer.parseInt(MESSAGE_TYPE) + 1) + "," + EQUIPMENT_ID + "," + HOUR + "," + MIN + "," + MSEC + "," + MESSAGE_VERSION;
    private static final String VALID_MSG_FLIGHT_NUMBER = "FL100";
    private static final String VALID_MSG_FLIGHT_SCHEDULED_DATETIME = "20221201001122";
    private static final String INVALID_MSG_FLIGHT_SCHEDULED_DATETIME = "20229988776655";
    private static final String VALID_MSG_DEFAULT_RETRIEVAL_DATETIME = "20221202112233";
    private static final String INVALID_MSG_DEFAULT_RETRIEVAL_DATETIME = "20229988776655";
    private static final String VALID_MSG_FINAL_SORT_LOCATION = "3700";
    private static final String INVALID_MSG_FINAL_SORT_LOCATION = "ABC";

    @Test
    void shouldParseValidCSVFormatString() {
        FlightDataUpdateMessageData data = new FlightDataUpdateMessageData();
        String messageToTest = HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME + ","
                + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION;
        assertTrue(data.parse(messageToTest));
        
        assertEquals(MESSAGE_LEN, String.valueOf(data.getHeader().getMsgLength()));
        assertEquals(SEQ_NUM, String.valueOf(data.getHeader().getSeqNo()));
        assertEquals(MESSAGE_TYPE, String.valueOf(data.getHeader().getMsgType()));
        assertEquals(EQUIPMENT_ID, String.valueOf(data.getHeader().getEquipmentID()));
        assertEquals(HOUR, String.valueOf(data.getHeader().getHours()));
        assertEquals(MIN, String.valueOf(data.getHeader().getMinutes()));
        assertEquals(MSEC, String.valueOf(data.getHeader().getMilliSeconds()));
        assertEquals(MESSAGE_VERSION, String.valueOf(data.getHeader().getMsgVersion()));
        
        assertEquals(VALID_MSG_FLIGHT_NUMBER, data.getFlightNumber());
        assertEquals(VALID_MSG_FLIGHT_SCHEDULED_DATETIME, data.getFlightScheduledDateTime());
        assertEquals(VALID_MSG_DEFAULT_RETRIEVAL_DATETIME, data.getDefaultRetrievalDateTime());
        assertEquals(VALID_MSG_FINAL_SORT_LOCATION, data.getFinalSortLocation());
        assertEquals(messageToTest, data.getReceivedMessage());
        assertTrue(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsNull() {
        FlightDataUpdateMessageData data = new FlightDataUpdateMessageData();
        assertFalse(data.parse(null));
        assertEquals(Strings.EMPTY, data.getFlightNumber());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(Strings.EMPTY, data.getDefaultRetrievalDateTime());
        assertEquals(Strings.EMPTY, data.getFinalSortLocation());
        assertEquals(null, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsEmpty() {
        FlightDataUpdateMessageData data = new FlightDataUpdateMessageData();
        assertFalse(data.parse(Strings.EMPTY));
        assertEquals(Strings.EMPTY, data.getFlightNumber());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(Strings.EMPTY, data.getDefaultRetrievalDateTime());
        assertEquals(Strings.EMPTY, data.getFinalSortLocation());
        assertEquals(Strings.EMPTY, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfCSVFormatStringIsTooShort() {
        FlightDataUpdateMessageData data = new FlightDataUpdateMessageData();
        String messageToTest = HEADER1; // No fields in body
        assertFalse(data.parse(messageToTest));
        assertEquals(Strings.EMPTY, data.getFlightNumber());
        assertEquals(Strings.EMPTY, data.getFlightScheduledDateTime());
        assertEquals(Strings.EMPTY, data.getDefaultRetrievalDateTime());
        assertEquals(Strings.EMPTY, data.getFinalSortLocation());
        assertEquals(messageToTest, data.getReceivedMessage());
        assertFalse(data.isValid());
    }

    @Test
    void shouldNotParseIfAnyFieldIsInvalid() {
        FlightDataUpdateMessageData data = new FlightDataUpdateMessageData();

        // Flight number is empty
        assertFalse(data.parse(HEADER1 + "," + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME + ","
                + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION));
        assertFalse(data.isValid());

        // Flight scheduled datetime is empty
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + ","
                + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION));
        assertFalse(data.isValid());

        // Flight scheduled datetime is invalid
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + INVALID_MSG_FLIGHT_SCHEDULED_DATETIME
                + "," + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION));
        assertFalse(data.isValid());

        // Default retrieval datetime is empty
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME + ","
                + "," + VALID_MSG_FINAL_SORT_LOCATION));
        assertFalse(data.isValid());

        // Default retrieval datetime is invalid
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME + ","
                + INVALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION));
        assertFalse(data.isValid());

        // Final sort location is empty
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME + ","
                + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + ","));
        assertFalse(data.isValid());
        
        // Final sort location is invalid
        assertFalse(data.parse(HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME + ","
                + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + INVALID_MSG_FINAL_SORT_LOCATION));
        assertFalse(data.isValid());
    }

    @Test
    void shouldReturnTrueIfTheSameMessageIsParsedAndCompared() {
        String messageToTest = HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME + ","
                + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION;

        FlightDataUpdateMessageData data1 = new FlightDataUpdateMessageData();
        data1.parse(messageToTest);

        FlightDataUpdateMessageData data2 = new FlightDataUpdateMessageData();
        data2.parse(messageToTest);

        assertTrue(data1.equals(data2));
    }

    @Test
    void shouldReturnFalseIfTheDifferentMessagesAreParsedAndCompared() {
        String messageToTest1 = HEADER1 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME
                + "," + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION;
        String messageToTest2 = HEADER2 + "," + VALID_MSG_FLIGHT_NUMBER + "," + VALID_MSG_FLIGHT_SCHEDULED_DATETIME
                + "," + VALID_MSG_DEFAULT_RETRIEVAL_DATETIME + "," + VALID_MSG_FINAL_SORT_LOCATION;

        FlightDataUpdateMessageData data1 = new FlightDataUpdateMessageData();
        data1.parse(messageToTest1);

        FlightDataUpdateMessageData data2 = new FlightDataUpdateMessageData();
        data2.parse(messageToTest2);

        assertFalse(data1.equals(data2));
    }
}
