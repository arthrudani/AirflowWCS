package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.communication.TimeoutChecker;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionHandler;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.decoder.StandardMessageDecoderImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder.StandardMessageEncoderImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.processor.StandardMessageProcessor;

@ExtendWith(MockitoExtension.class)
class ACPTransactionHandlerTest {
    private static final String PORT_NAME = "ACP-Port1";
    private static final int KEEP_ALIVE_TIMEOUT = 10;
    private static final int NONE = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;

    private static final boolean USE_STXETX = true;

    private static final String EMPTY_MSG = "";
    private static final String TOO_SHORT_MSG = "00 12 00 00";
    private static final String INVALID_MSG = "00 12 00 00 00 ff 00 00 00 00 00 00 00 00 00 00 00 01";

    private static final String VALID_KEEP_ALIVE_BINARY_MSG = "00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01";
    private static final String VALID_KEEP_ALIVE_CONVERTED_MSG = "1,123,1";

    private static final String VALID_ITEM_ARRIVED_BINARY_MSG = "00 2C 00 00 00 33 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 11 11 11 22 22 22 22 01 02 30 04 05 06 07 08 09 10 11 12 33 33 33 33";
    private static final String VALID_ITEM_ARRIVED_CONVERTED_MSG = "51,0,0,286331153,572662306,0,858993459";
    private static final String VALID_ITEM_ARRIVED_ACK_CONVERTED_MSG = "151,0,0";
    private static final String VALID_ITEM_STORED_BINARY_MSG = "00 2E 00 00 00 34 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 11 11 11 22 22 22 22 01 02 30 04 05 06 07 08 09 10 11 12 33 33 33 33 00 01";
    private static final String VALID_ITEM_STORED_CONVERTED_MSG = "52,0,0,286331153,572662306,0,858993459,1";
    private static final String VALID_ITEM_STORED_ACK_CONVERTED_MSG = "152,0,0";
    private static final String VALID_ITEM_PICKEDUP_BINARY_MSG = "00 32 00 00 00 35 00 00 00 00 00 00 00 00 00 00 00 00 00 68 00 00 00 68 00 00 00 68 31 30 34 20 20 20 20 20 20 20 20 20 1D D1 F8 7D 05 FA 74 DF 00 01";
    private static final String VALID_ITEM_PICKEDUP_CONVERTED_MSG = "53,0,104,104,104,104,500299901,100299999,1";
    private static final String VALID_ITEM_PICKEDUP_ACK_MSG = "153,0,0";
    private static final String VALID_LOCATION_STATUS_BINARY_MSG = "00 1C 00 00 00 3C 00 00 00 00 00 00 00 00 00 00 05 F8 EE 3F 00 00 0B EE CE DD 00 00";
    private static final String VALID_LOCATION_STATUS_CONVERTED_MSG = "60,0,0100199999,0,0200199901,0";
    private static final String VALID_LOCATION_STATUS_ACK_MSG = "160,0,0";

    private static final String VALID_MOVE_ORDER_MSG = "2,1,0,1,2,3,4,20230101123456,5,6,7,0";
    private static final String VALID_MOVE_ORDER_ACK_MSG = "00 12 00 01 00 66 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String VALID_FLIGHT_DATA_UPDATE_MSG = "9,1,FL100,1234";
    private static final String VALID_FLIGHT_DATA_UPDATE_ACK_MSG = "00 12 00 01 00 6D 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String VALID_BAG_DATA_UPDATE_MSG = "10,0,2222,5552,123456780,SQ123,3600,1200200001,1";
    private static final String VALID_BAG_DATA_UPDATE_ACK_MSG = "00 12 00 01 00 6E 00 00 00 00 00 00 00 00 00 00 00 00";

    private static final String VALID_LINK_START_UP_ACK_MSG_SEQ_0 = "00 12 00 00 00 C7 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String VALID_LINK_START_UP_ACK_MSG_SEQ_1 = "00 12 00 01 00 C7 00 00 00 00 00 00 00 00 00 00 00 00";

    MockedStatic<Logger> mockedLogger;
    MockedStatic<Factory> mockedFactory;

    @Mock
    Logger logger;

    @Mock
    MessageService messageService;

    StandardMessageProcessor processor;

    StandardMessageEncoderImpl encoder;

    StandardMessageDecoderImpl decoder;

    @Mock
    TimeoutChecker timeoutChecker;

    @Mock
    OutputStream outputStream;

    TransactionHandler transactionHandler;

    @BeforeEach
    void setUp() throws Exception {
        mockedLogger = Mockito.mockStatic(Logger.class);
        mockedLogger.when(() -> Logger.getLogger()).thenReturn(logger);

        decoder = new StandardMessageDecoderImpl();

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(StandardMessageDecoderImpl.class)).thenReturn(decoder);

        processor = new StandardMessageProcessor();
        encoder = new StandardMessageEncoderImpl();

        transactionHandler = new ACPTransactionHandler(PORT_NAME,9001, 30, USE_STXETX, logger, messageService, processor,
                encoder, timeoutChecker);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedLogger.close();
        mockedFactory.close();
    }

    private static Stream<Arguments> invalidInboundMessages() {
        return Stream.of(
                Arguments.of("Null", null),
                Arguments.of("Empty", MessageUtil.hexStringToByteArray(EMPTY_MSG)),
                Arguments.of("Too short", MessageUtil.hexStringToByteArray(TOO_SHORT_MSG)),
                Arguments.of("Invalid", MessageUtil.hexStringToByteArray(INVALID_MSG)));
    }

    private static Stream<Arguments> invalidOutboundMessages() {
        return Stream.of(
                Arguments.of("Null", null),
                Arguments.of("Empty", EMPTY_MSG),
                Arguments.of("Keep alive is already registered", VALID_KEEP_ALIVE_CONVERTED_MSG));
    }

    private static Stream<Arguments> inboundMessageTypesThatAirflowWCSShouldSendAck() {
        return Stream.of(
                Arguments.of("Item arrived", VALID_ITEM_ARRIVED_BINARY_MSG, VALID_ITEM_ARRIVED_CONVERTED_MSG,
                        VALID_ITEM_ARRIVED_ACK_CONVERTED_MSG),
                Arguments.of("Item stored", VALID_ITEM_STORED_BINARY_MSG, VALID_ITEM_STORED_CONVERTED_MSG,
                        VALID_ITEM_STORED_ACK_CONVERTED_MSG),
                Arguments.of("Item picked up", VALID_ITEM_PICKEDUP_BINARY_MSG, VALID_ITEM_PICKEDUP_CONVERTED_MSG,
                        VALID_ITEM_PICKEDUP_ACK_MSG),
                Arguments.of("Location status", VALID_LOCATION_STATUS_BINARY_MSG, VALID_LOCATION_STATUS_CONVERTED_MSG,
                        VALID_LOCATION_STATUS_ACK_MSG));
    }

    private static Stream<Arguments> outboundMessageTypesThatACPShouldSendAck() {
        return Stream.of(
                Arguments.of("Move order", VALID_MOVE_ORDER_MSG, VALID_MOVE_ORDER_ACK_MSG),
                Arguments.of("Flight data update", VALID_FLIGHT_DATA_UPDATE_MSG, VALID_FLIGHT_DATA_UPDATE_ACK_MSG),
                Arguments.of("Bag data update", VALID_BAG_DATA_UPDATE_MSG, VALID_BAG_DATA_UPDATE_ACK_MSG));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidInboundMessages")
    void shouldReturnFalseWhenAnInvalidMessageIsAddedAsInboundMessage(String test, byte[] inboundMessage)
            throws IOException {
        assertFalse(transactionHandler.addInboundMessage(inboundMessage));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidOutboundMessages")
    void shouldReturnFalseWhenAnInvalidMessageIsAddedAsOutboundMessage(String test, String outboundMessage)
            throws IOException {
        assertFalse(transactionHandler.addOutboundMessage(outboundMessage));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void shouldReturnTrueWhenExpectedMessageIsAddedAsInboundMessage(String test, String receivedRawMessage,
            String convertedMessage, String ackMessageToSend) throws IOException {
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(receivedRawMessage)));

        // The transaction list should have 1 entry
        assertEquals(ONE, transactionHandler.size());

        // The received message should be published
        verify(messageService, times(1)).publishEvent(PORT_NAME, null, convertedMessage, 0,
                MessageEventConsts.EQUIPMENT_EVENT_TYPE, MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + PORT_NAME);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void shouldReturnFalseWhenAckIsAddedAsInboundMessageButThereIsNoRequestSentOut(String test, String outboundMessage,
            String inboundMessage) throws IOException {
        assertFalse(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(inboundMessage)));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldSetTheTimeoutValueToTimeoutCheckerkWhenConnectedIsCalled() throws IOException {
        // connected() is called when a connection is established
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        // The internal timeout checker's started() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldSetTheTimeToTimeoutCheckerWhenKeepAliveIsAddedAsInboundMessage() throws IOException {
        // Keep alive outbound is registered by transaction handler when keep alive inbound is registered
        assertTrue(transactionHandler.addInboundMessage(MessageUtil
                .hexStringToByteArray(VALID_KEEP_ALIVE_BINARY_MSG)));

        // The transaction list should have 1 entry
        assertEquals(ONE, transactionHandler.size());

        // timeout checker's ticked() should be called when keep alive is received
        verify(timeoutChecker, times(1)).ticked();

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // Keep alive transaction
    // - Connected
    // - AirflowWCS <--(Keep alive)--- ACP
    // - AirflowWCS ---(Keep alive)--> ACP
    @Test
    void shouldProcessKeepAliveTransactionWhenARequestIsSentAndAnAckIsReceived() throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Register the received message to transaction handler
        // - Keep alive reply should be registered by transaction handler internally
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(VALID_KEEP_ALIVE_BINARY_MSG)));

        // The transaction list should have 1 entry
        // - Keep alive message
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - The outbound link start up should be registered
        // - Those 2 messages should be sent out
        assertTrue(transactionHandler.process());

        // The transaction list should have 1 entry as the outbound link start up is not acked yet
        assertEquals(ONE, transactionHandler.size());

        // The internal timeout checker's started() and ticked() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT); // When connected
        verify(timeoutChecker, times(1)).ticked(); // When keep alive is received

        // The outbound link start up and keep alive message should be sent out
        verify(outputStream, times(2)).write(any(byte[].class));
        verify(outputStream, times(2)).flush();

        // The received message shouldn't be published
        verify(messageService, never()).publishEvent(eq(PORT_NAME), isNull(), anyString(), eq(0),
                eq(MessageEventConsts.EQUIPMENT_EVENT_TYPE),
                eq(MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + PORT_NAME));

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // Request received and ack sent out
    // - Connected
    // - AirflowWCS <--(req)--- ACP
    // - AirflowWCS ---(ack)--> ACP
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void shouldProcessTransactionWhenARequestIsReceivedAndAnAckIsSent(String test, String receivedRawMessage,
            String convertedMessage, String ackMessageToSend) throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Register the received message to transaction handler
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(receivedRawMessage)));

        // The transaction list should have 1 entry
        // - The received request message
        assertEquals(ONE, transactionHandler.size());

        // Simulate a new ack reply registered for the request
        assertTrue(transactionHandler.addOutboundMessage(ackMessageToSend));

        // The transaction list should have 1 entry
        // - The received request including ack reply to be sent out
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - The outbound link start up should be sent out
        // - The req/ack should be removed from the internal transaction list
        assertTrue(transactionHandler.process());

        // The transaction list should have 1 entry
        assertEquals(ONE, transactionHandler.size());

        // The internal timeout checker's started() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT);

        // The ack message should be sent out
        verify(outputStream, times(2)).write(any(byte[].class));
        verify(outputStream, times(2)).flush();

        // The received message should be published
        verify(messageService, times(1)).publishEvent(PORT_NAME, null, convertedMessage, 0,
                MessageEventConsts.EQUIPMENT_EVENT_TYPE, MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + PORT_NAME);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // The outbound link start up should be sent whenever connected
    // - Connected
    // - AirflowWCS ---(Link start up req with sequence number 0)--> ACP
    // - AirflowWCS <--(Link start up ack)--- ACP
    // - AirflowWCS ---(Move order req with sequence number 1)--> ACP
    // - AirflowWCS <--(Move order ack)--- ACP
    // - Disconnected
    // - Connected
    // - AirflowWCS ---(Link start up req with sequence number 1)--> ACP
    @Test
    void shouldSendLinkStartUpWheneverAConnectionIsEstablished() throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Add a new request message
        assertTrue(transactionHandler.addOutboundMessage(VALID_MOVE_ORDER_MSG));

        // The transaction list should have 1 entry
        // - Move order request
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate the 1st cycle
        // - Outbound link start up should be registered and sent
        // - Move order request shouldn't be sent as link start up ack is not received yet
        assertTrue(transactionHandler.process());

        // The transaction list should have 2 entries
        assertEquals(TWO, transactionHandler.size());

        // Simulate link start up ack message
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(VALID_LINK_START_UP_ACK_MSG_SEQ_0)));

        // Run process() to simulate the 2nd cycle
        // - The outbound link start up ack is received, so it should be removed
        assertTrue(transactionHandler.process());

        // The transaction list should have 1 entry
        // - Move order
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate the 3rd cycle
        // - Finally move order can be sent out
        assertTrue(transactionHandler.process());

        // Simulate move order ack message
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(VALID_MOVE_ORDER_ACK_MSG)));

        // Run process() to simulate the 4th cycle
        // - move order ack is received, so it should be removed
        assertTrue(transactionHandler.process());

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        // Simulate the established connection after disconnection
        transactionHandler.disconnected();
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        // Run process() to simulate the 1st cycle
        // - outbound link start up is registered and sent out
        assertTrue(transactionHandler.process());

        // The internal timeout checker's started() should be called 2 times
        verify(timeoutChecker, times(2)).started(KEEP_ALIVE_TIMEOUT);

        // The request messages should be sent out
        ArgumentCaptor<byte[]> outboundMessageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(3)).write(outboundMessageCaptor.capture());
        assertEquals(3, outboundMessageCaptor.getAllValues().size());

        // - The 1st link starts up with sequence number 0
        ByteBuffer buf1 = ByteBuffer.wrap(outboundMessageCaptor.getAllValues().get(0));
        if (USE_STXETX) {
            buf1.get();
        }
        buf1.getShort();
        assertEquals(0, buf1.getShort());
        assertEquals(PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT, buf1.getShort());

        // - The move order request has sequence number 1
        ByteBuffer buf2 = ByteBuffer.wrap(outboundMessageCaptor.getAllValues().get(1));
        if (USE_STXETX) {
            buf2.get();
        }
        buf2.getShort();
        assertEquals(1, buf2.getShort());
        assertEquals(PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE_INT, buf2.getShort());

        // - The 2nd link starts up with sequence number 1
        ByteBuffer buf3 = ByteBuffer.wrap(outboundMessageCaptor.getAllValues().get(2));
        if (USE_STXETX) {
            buf3.get();
        }
        buf3.getShort();
        assertEquals(1, buf3.getShort());
        assertEquals(PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT, buf3.getShort());

        verify(outputStream, times(3)).flush();
    }

    // Prepare the test cases for inbound sequence number validation tests
    private static Stream<Arguments> inboundSequenceNumberCases() {
        return Stream.of(
                Arguments.of("Initial:0, Request:1, so it's valid", (short) 0, (short) 1,
                        PLCConstants.AckStatus.OK.getValue()),
                Arguments.of("Initial:1, Request:2, so it's valid", (short) 1, (short) 2,
                        PLCConstants.AckStatus.OK.getValue()),
                Arguments.of("Initial:32767, Request:1, so it's valid", Short.MAX_VALUE, (short) 1,
                        PLCConstants.AckStatus.OK.getValue()),
                Arguments.of("Initial:32766, Request:32767, so it's valid", (short) (Short.MAX_VALUE - 1),
                        Short.MAX_VALUE, PLCConstants.AckStatus.OK.getValue()),
                Arguments.of("Initial:0, Request:2, so it's invalid", (short) 0, (short) 2,
                        PLCConstants.AckStatus.SEQUENCE_NUMBER_ERROR.getValue()),
                Arguments.of("Initial:32767, Request:2, so it's invalid", (short) Short.MAX_VALUE,
                        (short) 2, PLCConstants.AckStatus.SEQUENCE_NUMBER_ERROR.getValue()),
                Arguments.of("Initial:1, Request:3, so it's invalid", (short) 1, (short) 3,
                        PLCConstants.AckStatus.SEQUENCE_NUMBER_ERROR.getValue()),
                Arguments.of("Initial:32766, Request:1, so it's invalid", (short) (Short.MAX_VALUE - 1),
                        (short) 1, PLCConstants.AckStatus.SEQUENCE_NUMBER_ERROR.getValue()));
    }

    // - Connected
    // - AirflowWCS <--(Inbound link start up with sequence number {1})--- ACP
    // - AirflowWCS ---(Outbound link start up with sequence number 0)--> ACP
    // - AirflowWCS ---(Inbound link start up ack)---> ACP
    // - AirflowWCS <--(Outbound link start up ack)--- ACP
    // - AirflowWCS <--(Item arrived req with sequence number {2})--- ACP
    // - AirflowWCS ---(Item arrived ack with sequence number {2} / ack status flag {3})--> ACP
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundSequenceNumberCases")
    void shouldValidateSequenceNumberToTheInitialSequenceNumberWhenInboundLinkStartUpIsReceived(String test,
            short prevSeqNum, short seqNum, short ackStatusFlag) throws IOException {
        // Prepare test messages to use
        ByteBuffer inboundLinkStartUpMessage = ByteBuffer.allocate(PLCConstants.MSG_HEADER_LEN + PLCConstants.PLC_LINK_STARTUP_MSG_BODY_LEN);
        inboundLinkStartUpMessage.putShort((short) (PLCConstants.MSG_HEADER_LEN + PLCConstants.PLC_LINK_STARTUP_MSG_BODY_LEN));
        inboundLinkStartUpMessage.putShort(prevSeqNum);
        inboundLinkStartUpMessage.putShort(PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT);
        inboundLinkStartUpMessage.putInt(0);
        inboundLinkStartUpMessage.put((byte) 0);
        inboundLinkStartUpMessage.put((byte) 0);
        inboundLinkStartUpMessage.putShort((short) 0);
        inboundLinkStartUpMessage.putShort((short) 0);

        ByteBuffer itemArriveMessage = ByteBuffer.allocate(PLCConstants.MSG_HEADER_LEN + PLCConstants.PLC_ITEM_ARRIVED_MSG_BODY_LEN);
        itemArriveMessage.putShort((short) (PLCConstants.MSG_HEADER_LEN + PLCConstants.PLC_ITEM_ARRIVED_MSG_BODY_LEN));
        itemArriveMessage.putShort(seqNum);
        itemArriveMessage.putShort(PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE_INT);
        itemArriveMessage.putInt(0);
        itemArriveMessage.put((byte) 0);
        itemArriveMessage.put((byte) 0);
        itemArriveMessage.putShort((short) 0);
        itemArriveMessage.putShort((short) 0);
        itemArriveMessage.putInt(0);
        itemArriveMessage.putInt(0);
        itemArriveMessage.putInt(0);
        itemArriveMessage.put("000000000000".getBytes());
        itemArriveMessage.putInt(0);
        
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Register the received message
        // - inbound link start up message with the given sequence number
        assertTrue(transactionHandler.addInboundMessage(inboundLinkStartUpMessage.array()));

        // The transaction list should have 1 entry
        // - inbound link start up
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - Outbound link start up should be sent out
        // - Inbound link start up ack should be sent out and then removed from transaction list
        assertTrue(transactionHandler.process());

        // The transaction list should have 1 entry
        // - Outbound link start up is pending for an ack
        assertEquals(ONE, transactionHandler.size());
        
        // Register the received message
        // - outbound link start up ack message
        // - item arrived message with the given sequence number
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(VALID_LINK_START_UP_ACK_MSG_SEQ_0)));
        assertTrue(transactionHandler.addInboundMessage(itemArriveMessage.array()));
        
        // Run process() to simulate 1 cycle
        // - Outbound link start up is acked, so should be removed from transaction list
        assertTrue(transactionHandler.process());
        
        // The transaction list should have 1 entry
        // - Item arrive needs to be acked
        assertEquals(ONE, transactionHandler.size());

        // Simulate a new ack message registered for item arrive request
        String itemArrivedAckMessage = PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE_INT + "," + seqNum + ",0";
        assertTrue(transactionHandler.addOutboundMessage(itemArrivedAckMessage));

        // Run process() to simulate 1 cycle
        // - item arrived ack should be sent out, so the transaction should be removed
        assertTrue(transactionHandler.process());

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        // The 3 messages should be sent out
        // - outbound link start up
        // - inbound link start up ack
        // - item arrived ack
        ArgumentCaptor<byte[]> outboundMessageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(3)).write(outboundMessageCaptor.capture());
        verify(outputStream, times(3)).flush();

        assertEquals(3, outboundMessageCaptor.getAllValues().size());
        
        // Outbound link start up
        ByteBuffer buf1 = ByteBuffer.wrap(outboundMessageCaptor.getAllValues().get(0));
        if (USE_STXETX) {
            buf1.get();
        }
        buf1.getShort(); // length
        assertEquals(0, buf1.getShort()); // sequence number
        assertEquals(PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT, buf1.getShort()); // type
        
        // Inbound link start up ack
        ByteBuffer buf2 = ByteBuffer.wrap(outboundMessageCaptor.getAllValues().get(1));
        if (USE_STXETX) {
            buf2.get();
        }
        buf2.getShort(); // length
        assertEquals(prevSeqNum, buf2.getShort()); // sequence number
        assertEquals(PLCConstants.PLC_LINK_STARTUP_ACK_MSG_TYPE_INT, buf2.getShort()); // type

        // Item arrived ack
        ByteBuffer buf3 = ByteBuffer.wrap(outboundMessageCaptor.getAllValues().get(2));
        if (USE_STXETX) {
            buf3.get();
        }
        buf3.getShort(); // length
        assertEquals(seqNum, buf3.getShort()); // sequence number of item arrive request message
        assertEquals(PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE_INT, buf3.getShort()); // type
        buf3.getInt();
        buf3.get();
        buf3.get();
        buf3.getShort();
        buf3.getShort();
        assertEquals(ackStatusFlag, buf3.getShort()); // ack status flag
    }

    // Request sent and ack received transaction with initialisation
    // - Connected
    // - AirflowWCS ---(outbound link start up)--> ACP
    // - AirflowWCS <--(outbound link start up ack)--- ACP
    // - AirflowWCS ---(req)--> ACP
    // - AirflowWCS <--(ack)--- ACP
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void shouldProcessTransactionWhenARequestIsSentAndAnAckIsReceived(String test, String outboundMessage,
            String inboundMessage) throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Add a new request message
        assertTrue(transactionHandler.addOutboundMessage(outboundMessage));
        
        // Run process() to simulate 1 cycle
        // - outbound link start up should be sent out
        assertTrue(transactionHandler.process());

        // The transaction list should have 2 entries created for the outbound message
        // - outbound link start up
        // - request
        assertEquals(TWO, transactionHandler.size());
        
        // Simulate outbound link start up ack message
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(VALID_LINK_START_UP_ACK_MSG_SEQ_0)));

        // Run process() to simulate 1 cycle
        // - outbound link start up should be removed
        // - the request should be sent out
        assertTrue(transactionHandler.process());

        // The transaction list should have 1 entry created for the outbound message that is sent out
        assertEquals(ONE, transactionHandler.size());

        // Simulate a new ack message received for the request
        // - ack is received
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(inboundMessage)));

        // The transaction list should have 1 entry created for the pair of inbound/outbound messages
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - The req/ack should be removed from the internal transaction list
        assertTrue(transactionHandler.process());

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        // The internal timeout checker's started() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT);

        // The request message should be sent out
        verify(outputStream, times(2)).write(any(byte[].class));
        verify(outputStream, times(2)).flush();

        // The received messages should be published
        // - Outbound link start up ack
        // - ack for the request
        verify(messageService, times(2)).publishEvent(PORT_NAME, null, EMPTY_MSG, 0,
                MessageEventConsts.EQUIPMENT_EVENT_TYPE,
                MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + PORT_NAME);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // Retransmitting a pending request after initialisation
    // - Connected
    // - AirflowWCS ---(outbound link start up)--> ACP
    // - AirflowWCS <--(outbound link start up ack)--- ACP
    // - AirflowWCS ---(req)--> ACP
    // - Connected after disconnection
    // - AirflowWCS ---(outbound link start up)--> ACP
    // - AirflowWCS <--(outbound link start up ack)--- ACP
    // - AirflowWCS ---(req)--> ACP
    // - AirflowWCS <--(ack)--- ACP
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void shouldRetransmitAPendingRequestWhenAConnectionIsEstablishedAgain(String test, String outboundMessage,
            String inboundMessage) throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Add a new request message
        assertTrue(transactionHandler.addOutboundMessage(outboundMessage));

        // The transaction list should have 1 entry
        // - request
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate the 1st cycle
        // - outbound link start up is registered and sent out
        assertTrue(transactionHandler.process());
        
        // The transaction list should have 2 entries
        // - outbound link start up
        // - request
        assertEquals(TWO, transactionHandler.size());
        
        // Simulate outbound link start up ack message
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(VALID_LINK_START_UP_ACK_MSG_SEQ_0)));
        
        // Run process() to simulate the 2nd cycle
        // - outbound link start up is acked, so should be removed from transaction list
        // - request should be sent out
        assertTrue(transactionHandler.process());

        // Simulate the established connection after disconnection
        transactionHandler.disconnected();
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Run process() to simulate the 3rd cycle
        // - new outbound link start up should be sent out
        assertTrue(transactionHandler.process());

        // The transaction list should have 2 entries
        // - outbound link start up
        // - pending request
        assertEquals(TWO, transactionHandler.size());
        
        // Simulate outbound link start up ack message
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(VALID_LINK_START_UP_ACK_MSG_SEQ_1)));

        // Run process() to simulate the 4th cycle
        // - Outbound link start up is acked, so should be removed from transaction list
        // - The pending request should be sent out again
        assertTrue(transactionHandler.process());

        // The transaction list should have 1 entry
        assertEquals(ONE, transactionHandler.size());

        // The internal timeout checker's started() should be called 2 times
        verify(timeoutChecker, times(2)).started(KEEP_ALIVE_TIMEOUT);

        // The request message should be sent out
        // - The 1st cycle: Outbound link start up(sequence number 0)
        // - The 2nd cycle: Request(the 1st transmission)
        // - The 3rd cycle: Outbound link start up(sequence number 1)
        // - The 4th cycle: Request(the 2nd transmission)
        verify(outputStream, times(4)).write(any(byte[].class));
        verify(outputStream, times(4)).flush();
    }

    // When writing fails, process() immediately returns false without doing any more things
    // - Connected
    // - AirflowWCS <--(req)--- ACP
    // - AirflowWCS ---(ack)--> ACP but writing failed
    // - Connected after disconnection
    // - AirflowWCS ---(ack)--> ACP and this time writing ok
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void shouldContinueToSendAMessageWhenWritingIsFailed(String test, String receivedRawMessage,
            String convertedMessage, String ackMessageToSend) throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Register the received message to transaction handler
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(receivedRawMessage)));

        // Simulate a new ack message registered for request
        assertTrue(transactionHandler.addOutboundMessage(ackMessageToSend));

        // The transaction list should have 1 entry
        // - received message
        assertEquals(ONE, transactionHandler.size());

        // In the 1st call of outputStream.write(...): do nothing to simulate the successful writing for outbound link start up
        // In the 2nd call of outputStream.write(...): throw an IOException to simulate writing failure for the request
        // In the 3rd call of outputStream.write(...): do nothing to simulate the successful writing for outbound link start up
        // In the 4th call of outputStream.write(...): do nothing to simulate the successful writing for the request
        doNothing().doThrow(IOException.class).doNothing().doNothing().when(outputStream).write(any(byte[].class));

        // Run process() to simulate 1 cycle
        // - Outbound link start up should be sent
        // - The req/ack should be still in the transaction list as writing ack is failed
        // - When process() returns false, ServerSocketHandler disconnects the connection and waits for a new connection
        assertFalse(transactionHandler.process());

        // The transaction list should have 2 entries
        // - Outbound link start up
        // - Ack message not sent yet
        assertEquals(TWO, transactionHandler.size());
        
        // Simulate the reconnection
        transactionHandler.disconnected();
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Run process() to simulate 1 cycle
        // - The item arrived req/ack should be removed from the internal transaction list as sending ack is done
        assertTrue(transactionHandler.process());

        // The transaction list should be empty
        assertEquals(ONE, transactionHandler.size());

        // 2 times connected, so started() should be called accordingly
        verify(timeoutChecker, times(2)).started(anyLong());
        
        // The outbound link start up and ack message should be sent out
        verify(outputStream, times(4)).write(any(byte[].class)); // called 4 times
        verify(outputStream, times(3)).flush(); // sent 3 times
        verify(outputStream, times(1)).close(); // closed once

        // The received message should be published
        verify(messageService, times(1)).publishEvent(PORT_NAME, null, convertedMessage, 0,
                MessageEventConsts.EQUIPMENT_EVENT_TYPE, MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + PORT_NAME);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // When an unexpected ack message is received, it should be ignored
    // - Connected
    // - AirflowWCS <--(ack)--- ACP
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void shouldIgnoreUnexpectedAckMessageWhenNoMatchingRequestMessageIsFound(String test, String outboundMessage,
            String inboundMessage) throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Simulate a new ack message received while there's no matching request message sent out
        assertFalse(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(inboundMessage)));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - The ack shouldn't be registered in the internal transaction list
        assertTrue(transactionHandler.process());

        // The transaction list should have outbound link start up message registered by process()
        assertEquals(ONE, transactionHandler.size());

        // The internal timeout checker's started() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT);

        // The outbound link start up message should be sent out
        verify(outputStream, times(1)).write(any(byte[].class));
        verify(outputStream, times(1)).flush();

        // The received message shouldn't be published
        verify(messageService, never()).publishEvent(eq(PORT_NAME), isNull(), anyString(), eq(0),
                eq(MessageEventConsts.EQUIPMENT_EVENT_TYPE),
                eq(MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT + PORT_NAME));

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }
}
