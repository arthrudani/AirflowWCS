package com.daifukuoc.wrxj.custom.ebs.host.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;

@ExtendWith(MockitoExtension.class)
class MessageUtilTest {
    private static final int NOW_HOUR = 10;
    private static final int NOW_MINUTE = 20;
    private static final int NOW_SECOND = 1;
    private static final LocalTime NOW = LocalTime.of(NOW_HOUR, NOW_MINUTE, NOW_SECOND);
    private static final int SEQUENCE_NUMBER = 32767;
    private static final short ACTIVE_FLAG_OF_KEEP_ALIVE = 1;
    private static final String ORDER_ID_OF_EXPECTED_RECEIPT = "200200";
    private static final String TRAY_ID_OF_EXPECTED_RECEIPT = "2000";
    private static final String GLOBAL_ID_OF_EXPECTED_RECEIPT = "20002000";
    private static final String BAG_ID_OF_EXPECTED_RECEIPT = "BAG2000";
    private static final String ENTRANCE_STATION_OF_EXPECTED_RECEIPT = "1001";
    private static final String STATUS_OF_EXPECTED_RECEIPT = "1";
    private static final String ORDER_ID_OF_STORE_COMPLETE = "200200";
    private static final String TRAY_ID_OF_STORE_COMPLETE = "2000";
    private static final String GLOBAL_ID_OF_STORE_COMPLETE = "20002000";
    private static final String BAG_ID_OF_STORE_COMPLETE = "BAG2000";
    private static final String EMPTY_ZONE_ID_OF_STORE_COMPLETE = "";
    private static final short NUMERIC_EMPTY_ZONE_ID_OF_STORE_COMPLETE = (short) 0;
    private static final String STORAGE_LOCATION_OF_STORE_COMPLETE = "2000100101";
    private static final String STATUS_OF_STORE_COMPLETE = "1";
    private static final String STATUS_FLIGHT_DATA_UPDATE = "2";
    private static final String STATUS_INV_REQ_BY_WH_ACK = "0";
    private static final String ORDER_ID_OF_RETRIEVAL_ORDER = "1234";
    private static final String STATUS_OF_RETRIEVAL_ORDER = STATUS_OF_EXPECTED_RECEIPT;
    private static final String MISSING_BAG_OF_RETRIEVAL_ORDER = "0";
    private static final String EXPECTED_RECEIPT_RESPONSE_MESSAGE = SEQUENCE_NUMBER + ";"
            + MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue() + ";"
            + SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE + ";"
            + ORDER_ID_OF_EXPECTED_RECEIPT + ";" + TRAY_ID_OF_EXPECTED_RECEIPT + ";" + GLOBAL_ID_OF_EXPECTED_RECEIPT
            + ";" + BAG_ID_OF_EXPECTED_RECEIPT + ";" + ENTRANCE_STATION_OF_EXPECTED_RECEIPT + ";"
            + STATUS_OF_EXPECTED_RECEIPT;
    private static final String STORE_COMPLETE_NOTIFY_MESSAGE = SEQUENCE_NUMBER + ";"
            + MessageOutNames.STORE_COMPLETE.getValue() + ";" + SACControlMessage.StoreCompletionNotify.MSG_TYPE + ";"
            + ORDER_ID_OF_STORE_COMPLETE + ";" + TRAY_ID_OF_STORE_COMPLETE + ";" + GLOBAL_ID_OF_STORE_COMPLETE
            + ";" + BAG_ID_OF_STORE_COMPLETE + ";" + EMPTY_ZONE_ID_OF_STORE_COMPLETE + ";"
            + STORAGE_LOCATION_OF_STORE_COMPLETE + ";" + STATUS_OF_STORE_COMPLETE;
    private static final String FLIGHT_DATA_UPDATE_RESPONSE_MESSAGE = SEQUENCE_NUMBER + ";"
            + MessageOutNames.FLIGHT_DATA_UPDATE.getValue() + ";" + SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE
            + ";" + STATUS_FLIGHT_DATA_UPDATE;
    
    private static final String INV_REQ_BY_WH_ACK_MSG = SEQUENCE_NUMBER + ";"
            + MessageOutNames.INVENTORY_REQUEST_BY_WAREHOUSE.getValue() + ";" + SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE
            + ";" + STATUS_INV_REQ_BY_WH_ACK;
    private static final String RETRIEVAL_ORDER_RESPONSE_MESSAGE = SEQUENCE_NUMBER + ";"
            + MessageOutNames.FLIGHT_DATA_UPDATE.getValue() + ";" + SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE
            + ";" + ORDER_ID_OF_RETRIEVAL_ORDER + ";" + STATUS_OF_RETRIEVAL_ORDER + ";"
            + MISSING_BAG_OF_RETRIEVAL_ORDER;

    MockedStatic<LocalTime> mockedLocalTime;

    @BeforeEach
    void setUp() throws Exception {
        mockedLocalTime = Mockito.mockStatic(LocalTime.class);
        mockedLocalTime.when(() -> LocalTime.now()).thenReturn(NOW);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedLocalTime.close();
    }

    // Prepare the list of sequence number values for the following unit test method
    private static Stream<Arguments> sequenceNumberTestCases() {
        return Stream.of(
                Arguments.of("Sequence number 0", "0x00 0x00", (short) 2, 0, (short) 0, (short) 0, (short) 1),
                Arguments.of("Sequence number 32767", "0x7F 0xFF", (short) 2, 32767, (short) 0, (short) 0, (short) 1),
                Arguments.of("Sequence number -32768", "0x80 0x00", (short) 2, -32768, (short) 0, (short) 0, (short) 1),
                Arguments.of("Sequence number -1", "0xFF 0xFF", (short) 2, -1, (short) 0, (short) 0, (short) 1),
                Arguments.of("Sequence number 65535", "0xFF 0xFF", (short) 2, 65535, (short) 0, (short) 0, (short) 1));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("sequenceNumberTestCases")
    void sequenceNumberShouldHaveUnsignedShortwhenBuildMessageHeaderBuildsAHeader(String test, String converted,
            short length, int sequenceNumber, short type, int equimpmentId, short version) {
        byte[] builtByteArray = MessageUtil.buildMessageHeader(length, sequenceNumber, type, equimpmentId, version);
        String[] hexString = MessageUtil.encodeHexString(builtByteArray).split(" ");
        assertEquals(converted, hexString[2] + " " + hexString[3]);
    }

    // TODO Unit tests for other existing messages!

    @Test
    void shouldReturnBuiltByteArrayofKeepAliveMessageWhenBuildKeepAliveMessageIsCalled() {
        byte[] builtByteArray = MessageUtil.buildKeepAliveMessage(MessageUtil.EQUIPMENT_ID, (short) SEQUENCE_NUMBER,
                (short) MessageUtil.VERSION_NUMBER);

        ByteBuffer builtBuffer = ByteBuffer.wrap(builtByteArray);
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
        assertEquals(SEQUENCE_NUMBER, sequenceNumber);
        assertEquals(SACControlMessage.KEEPALIVE_MSG_TYPE, messageType);
        assertEquals(MessageUtil.EQUIPMENT_ID, equipmentId);
        assertEquals(NOW_HOUR, hours);
        assertEquals(NOW_MINUTE, minutes);
        // FIXME signed short can't store 59999
        assertEquals(NOW_SECOND, mSeconds / 1000);
        assertEquals((short) MessageUtil.VERSION_NUMBER, versionNumber);
        assertEquals(ACTIVE_FLAG_OF_KEEP_ALIVE, activeFlag);
    }

    @Test
    void shouldReturnBuiltByteArrayofExpectedReceiptResponseMessageWhenBuildExpectedReceiptResponseMessageIsCalled() {
        byte[] builtByteArray = MessageUtil.buildExpectedReceiptResponseMessage(EXPECTED_RECEIPT_RESPONSE_MESSAGE);
        assertEquals(SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_LEN, builtByteArray.length);
        ByteBuffer builtBuffer = ByteBuffer.wrap(builtByteArray);

        short messageLength = builtBuffer.getShort();
        int sequenceNumber = Short.toUnsignedInt(builtBuffer.getShort());
        short messageType = builtBuffer.getShort();
        int equipmentId = builtBuffer.getInt();
        byte hours = builtBuffer.get();
        byte minutes = builtBuffer.get();
        short mSeconds = builtBuffer.getShort();
        short versionNumber = builtBuffer.getShort();

        int orderId = builtBuffer.getInt();
        int trayId = builtBuffer.getInt();
        int globalId = builtBuffer.getInt();
        byte[] bagIdArray = new byte[12];
        builtBuffer.get(bagIdArray, 0, bagIdArray.length);
        int entranceStationId = builtBuffer.getInt();
        short status = builtBuffer.getShort();

        assertEquals(SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_LEN, messageLength);
        assertEquals(SEQUENCE_NUMBER, sequenceNumber);
        assertEquals(SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE, messageType);
        assertEquals(MessageUtil.EQUIPMENT_ID, equipmentId);
        assertEquals(NOW_HOUR, hours);
        assertEquals(NOW_MINUTE, minutes);
        // FIXME signed short can't store 59999
        assertEquals(NOW_SECOND, mSeconds / 1000);
        assertEquals((short) MessageUtil.VERSION_NUMBER, versionNumber);
        assertEquals(Integer.parseInt(ORDER_ID_OF_EXPECTED_RECEIPT), orderId);
        assertEquals(Integer.parseInt(TRAY_ID_OF_EXPECTED_RECEIPT), trayId);
        assertEquals(Integer.parseInt(GLOBAL_ID_OF_EXPECTED_RECEIPT), globalId);
        assertEquals(String.format(MessageUtil.BAG_ID_FORMAT, BAG_ID_OF_EXPECTED_RECEIPT), new String(bagIdArray)); // 12 bytes
        assertEquals(Integer.parseInt(ENTRANCE_STATION_OF_EXPECTED_RECEIPT), entranceStationId);
        assertEquals(Short.parseShort(STATUS_OF_EXPECTED_RECEIPT), status);
    }

    @Test
    void shouldReturnBuiltByteArrayofStoreCompleteMessageWhenBuildStoreCompleteNotifyMessageIsCalled() {
        byte[] builtByteArray = MessageUtil.buildStoreCompleteNotifyMessage(STORE_COMPLETE_NOTIFY_MESSAGE);
        assertEquals(SACControlMessage.StoreCompletionNotify.MSG_LEN, builtByteArray.length);
        ByteBuffer builtBuffer = ByteBuffer.wrap(builtByteArray);

        short messageLength = builtBuffer.getShort();
        int sequenceNumber = Short.toUnsignedInt(builtBuffer.getShort());
        short messageType = builtBuffer.getShort();
        int equipmentId = builtBuffer.getInt();
        byte hours = builtBuffer.get();
        byte minutes = builtBuffer.get();
        short mSeconds = builtBuffer.getShort();
        short versionNumber = builtBuffer.getShort();

        int orderId = builtBuffer.getInt();
        int trayId = builtBuffer.getInt();
        int globalId = builtBuffer.getInt();
        byte[] bagIdArray = new byte[12];
        builtBuffer.get(bagIdArray, 0, bagIdArray.length);
        short zoneId = builtBuffer.getShort();
        int storageLocationId = builtBuffer.getInt();
        short status = builtBuffer.getShort();

        assertEquals(SACControlMessage.StoreCompletionNotify.MSG_LEN, messageLength);
        assertEquals(SEQUENCE_NUMBER, sequenceNumber);
        assertEquals(SACControlMessage.StoreCompletionNotify.MSG_TYPE, messageType);
        assertEquals(MessageUtil.EQUIPMENT_ID, equipmentId);
        assertEquals(NOW_HOUR, hours);
        assertEquals(NOW_MINUTE, minutes);
        // FIXME signed short can't store 59999
        assertEquals(NOW_SECOND, mSeconds / 1000);
        assertEquals((short) MessageUtil.VERSION_NUMBER, versionNumber);
        assertEquals(Integer.parseInt(ORDER_ID_OF_STORE_COMPLETE), orderId);
        assertEquals(Integer.parseInt(TRAY_ID_OF_STORE_COMPLETE), trayId);
        assertEquals(Integer.parseInt(GLOBAL_ID_OF_STORE_COMPLETE), globalId);
        assertEquals(String.format(MessageUtil.BAG_ID_FORMAT, BAG_ID_OF_STORE_COMPLETE), new String(bagIdArray)); // 12 bytes
        // When zone id is empty, 0 should be sent out
        assertEquals(NUMERIC_EMPTY_ZONE_ID_OF_STORE_COMPLETE, zoneId);
        assertEquals(Integer.parseInt(STORAGE_LOCATION_OF_STORE_COMPLETE), storageLocationId);
        assertEquals(Short.parseShort(STATUS_OF_EXPECTED_RECEIPT), status);
    }

    @Test
    void shouldReturnBuiltByteArrayofRetrievalOrderResponseMessageWhenBuildRetrievalOrderResponseMessageIsCalled() {
        byte[] builtByteArray = MessageUtil.buildRetrievalOrderResponseMessage(RETRIEVAL_ORDER_RESPONSE_MESSAGE);
        assertEquals(SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_LEN, builtByteArray.length);
        ByteBuffer builtBuffer = ByteBuffer.wrap(builtByteArray);

        short messageLength = builtBuffer.getShort();
        int sequenceNumber = Short.toUnsignedInt(builtBuffer.getShort());
        short messageType = builtBuffer.getShort();
        int equipmentId = builtBuffer.getInt();
        byte hours = builtBuffer.get();
        byte minutes = builtBuffer.get();
        short mSeconds = builtBuffer.getShort();
        short versionNumber = builtBuffer.getShort();
        short orderId = builtBuffer.getShort();
        short status = builtBuffer.getShort();
        short missingBags = builtBuffer.getShort();

        assertEquals(SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_LEN, messageLength);
        assertEquals(SEQUENCE_NUMBER, sequenceNumber);
        assertEquals(SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE, messageType);
        assertEquals(MessageUtil.EQUIPMENT_ID, equipmentId);
        assertEquals(NOW_HOUR, hours);
        assertEquals(NOW_MINUTE, minutes);
        // FIXME signed short can't store 59999
        assertEquals(NOW_SECOND, mSeconds / 1000);
        assertEquals((short) MessageUtil.VERSION_NUMBER, versionNumber);
        assertEquals(Short.parseShort(ORDER_ID_OF_RETRIEVAL_ORDER), orderId);
        assertEquals(Short.parseShort(STATUS_OF_RETRIEVAL_ORDER), status);
        assertEquals(Short.parseShort(MISSING_BAG_OF_RETRIEVAL_ORDER), missingBags);
    }

    @Test
    void shouldReturnBuiltByteArrayofFlightDataUpdateResponseMessageWhenBuildFlightDataUpdateResponseMessageIsCalled() {
        byte[] builtByteArray = MessageUtil.buildFlightDataUpdateResponseMessage(FLIGHT_DATA_UPDATE_RESPONSE_MESSAGE);
        assertEquals(SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_LEN, builtByteArray.length);
        ByteBuffer builtBuffer = ByteBuffer.wrap(builtByteArray);

        short messageLength = builtBuffer.getShort();
        int sequenceNumber = Short.toUnsignedInt(builtBuffer.getShort());
        short messageType = builtBuffer.getShort();
        int equipmentId = builtBuffer.getInt();
        byte hours = builtBuffer.get();
        byte minutes = builtBuffer.get();
        short mSeconds = builtBuffer.getShort();
        short versionNumber = builtBuffer.getShort();
        int status = builtBuffer.getShort();

        assertEquals(SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_LEN, messageLength);
        assertEquals(SEQUENCE_NUMBER, sequenceNumber);
        assertEquals(SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE, messageType);
        assertEquals(MessageUtil.EQUIPMENT_ID, equipmentId);
        assertEquals(NOW_HOUR, hours);
        assertEquals(NOW_MINUTE, minutes);
        // FIXME signed short can't store 59999
        assertEquals(NOW_SECOND, mSeconds / 1000);
        assertEquals((short) MessageUtil.VERSION_NUMBER, versionNumber);
        assertEquals(Short.parseShort(STATUS_FLIGHT_DATA_UPDATE), status);
    }
    
    @Test
    void shouldReturnBuiltByteArrayofInvReqByWHMessageWhenBuildInvReqByWHMessageIsCalled() {
        byte[] builtByteArray = MessageUtil.buildInventoryRequestByWarehouseAckMessage(INV_REQ_BY_WH_ACK_MSG);
        assertEquals(SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_LEN, builtByteArray.length);
        ByteBuffer builtBuffer = ByteBuffer.wrap(builtByteArray);

        short messageLength = builtBuffer.getShort();
        int sequenceNumber = Short.toUnsignedInt(builtBuffer.getShort());
        short messageType = builtBuffer.getShort();
        int equipmentId = builtBuffer.getInt();
        byte hours = builtBuffer.get();
        byte minutes = builtBuffer.get();
        short mSeconds = builtBuffer.getShort();
        short versionNumber = builtBuffer.getShort();
        int status = builtBuffer.getShort();

        assertEquals(SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_LEN, messageLength);
        assertEquals(SEQUENCE_NUMBER, sequenceNumber);
        assertEquals(SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE, messageType);
        assertEquals(MessageUtil.EQUIPMENT_ID, equipmentId);
        assertEquals(NOW_HOUR, hours);
        assertEquals(NOW_MINUTE, minutes);
        // FIXME signed short can't store 59999
        assertEquals(NOW_SECOND, mSeconds / 1000);
        assertEquals((short) MessageUtil.VERSION_NUMBER, versionNumber);
        assertEquals(Short.parseShort(STATUS_INV_REQ_BY_WH_ACK), status);
    }

    @Test
    void shouldReturnAByteArrayConvertedFromTheGivenHexStringWhenHexStringToByteArrayIsCalled() {
        byte[] result = MessageUtil.hexStringToByteArray("00 01 0A 0F A1 FE");
        assertEquals(result[0], (byte) 0x00);
        assertEquals(result[1], (byte) 0x01);
        assertEquals(result[2], (byte) 0x0A);
        assertEquals(result[3], (byte) 0x0F);
        assertEquals(result[4], (byte) 0xA1);
        assertEquals(result[5], (byte) 0xFE);
    }
}
