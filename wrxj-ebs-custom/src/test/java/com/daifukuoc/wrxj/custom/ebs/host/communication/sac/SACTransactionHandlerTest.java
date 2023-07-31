package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.communication.TimeoutChecker;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionHandler;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageManager;

@ExtendWith(MockitoExtension.class)
class SACTransactionHandlerTest {
    private static final int ACK_MAXRETRY = 3;
    private static final int ACK_TIMEOUT = 10;
    private static final String PORT_NAME = SACClientSocketHandler.DEFAULT_PORT_NAME;
    private static final String INTEGRATOR_NAME = SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME;
    private static final int KEEP_ALIVE_TIMEOUT = 10;
    private static final int NONE = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final String EMPTY_MSG = "";
    private static final String TOO_SHORT_MSG = "00 12 00 00";
    private static final String INVALID_MSG = "00 12 00 00 00 ff 00 00 00 00 00 00 00 00 00 00 00 01";
    private static final String KEEP_ALIVE_BINARY_MSG = "00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01";
    private static final String KEEP_ALIVE_CONVERTED_MSG = "1,123,1";
    private static final String SEQUENCE_NUMBER = "32767";
    private static final String EXPECTED_RECEIPT_REQUEST_MSG = "84," + SEQUENCE_NUMBER
            + ",52,2222,3,4,5,0,999,1002,10021002,BAG1002,FL100,20221213000000,20221213000000,3600,1,1";
    private static final String EXPECTED_RECEIPT_ACK_MSG = SEQUENCE_NUMBER + ";"
            + MessageOutNames.EXPECTED_RECEIPT_ACK.getValue() + ";" + SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_TYPE
            + ";" + SACControlMessage.AckStatus.OK.getValue();
    private static final String EXPECTED_RECEIPT_RESPONSE_MSG = SEQUENCE_NUMBER + ";"
            + MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue() + ";"
            + SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE + ";999;1002;10021002;BAG1002;2020010201;1";
    private static final String EXPECTED_RECEIPT_RESPONSE_ACK_BINARY_MSG = "00 12 "
            + MessageUtil.encodeHexString(MessageUtil.convertToByteArray((short) 2))
            + " 00 66 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String EXPECTED_RECEIPT_RESPONSE_ACK_CONVERTED_MSG = "18,2,102,0,0,0,0,0,0";
    private static final String RETRIEVAL_FLIGHT_REQUEST_MESSAGE = "44," + SEQUENCE_NUMBER
            + ",55,2222,3,4,5,0,999,FL100,20221213000000,0";
    private static final String RETRIEVAL_FLIGHT_REQUEST_ACK_MSG = SEQUENCE_NUMBER + ";"
            + MessageOutNames.RETRIEVAL_FLIGHT_REQUEST_ACK.getValue() + ";" + SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_TYPE
            + ";" + SACControlMessage.AckStatus.OK.getValue();
    private static final String RETRIEVAL_FLIGHT_RESPONSE_MSG = SEQUENCE_NUMBER + ";"
    		+ MessageOutNames.ORDER_COMPLETE.getValue() + ";5;111;1;0";
    private static final String RETRIEVAL_FLIGHT_RESPONSE_ACK_BINARY_MSG = "00 12 "
            + MessageUtil.encodeHexString(MessageUtil.convertToByteArray((short) 2))
            + " 00 69 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String RETRIEVAL_FLIGHT_RESPONSE_ACK_CONVERTED_MSG = "18,2,105,0,0,0,0,0,0";
    private static final String FLIGHT_DATA_UPDATE_MESSAGE = "56," + SEQUENCE_NUMBER
            + ",57,2222,3,4,5,0,FL100,20221201001122,20221202112233,3700";
    private static final String FLIGHT_DATA_UPDATE_RESPONSE_MSG = SEQUENCE_NUMBER + ";"
            + MessageOutNames.FLIGHT_DATA_UPDATE.getValue() + ";157;1";

    private static final String STORED_COMPLETE_REQUEST_MSG = "1;"
            + MessageOutNames.STORE_COMPLETE.getValue() + ";" + SACControlMessage.StoreCompletionNotify.MSG_TYPE
            + ";0;1;2;3;4;5;6";
    private static final String STORED_COMPLETE_ACK_BINARY_MSG = "00 12 "
            + MessageUtil.encodeHexString(MessageUtil.convertToByteArray((short) 2))
            + " 00 67 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String STORED_COMPLETE_ACK_CONVERTED_MSG = "18,2,103,0,0,0,0,0,0";

    private static final LocalDateTime WHEN_SENT = LocalDateTime.parse("2023-02-08T00:00:00");

    @Mock
    Logger logger;

    @Mock
    MessageService messageService;

    SACMessageManager processor;

    @Mock
    TimeoutChecker timeoutChecker;

    @Mock
    OutputStream outputStream;

    TransactionHandler transactionHandler;

    @BeforeEach
    void setUp() throws Exception {
        processor = new SACMessageManager();
        transactionHandler = new SACTransactionHandler(PORT_NAME, INTEGRATOR_NAME, ACK_TIMEOUT, ACK_MAXRETRY, true,
                logger, messageService,
                processor, timeoutChecker);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    private static Stream<Arguments> inboundMessageTypesThatAirflowWCSShouldSendAck() {
        return Stream.of(
                Arguments.of("Expected receipt",
                        convertCSVFormattedExpectedReceiptMessageToByteArray(EXPECTED_RECEIPT_REQUEST_MSG),
                        EXPECTED_RECEIPT_REQUEST_MSG, EXPECTED_RECEIPT_ACK_MSG),
                Arguments.of("Retrieval order without list",
                        convertCSVFormattedRetrievalOrderWithoutListMessageToByteArray(
                                RETRIEVAL_FLIGHT_REQUEST_MESSAGE),
                        RETRIEVAL_FLIGHT_REQUEST_MESSAGE, RETRIEVAL_FLIGHT_REQUEST_ACK_MSG),
                Arguments.of("Flight data update",
                        convertCSVFormattedFlightDataUpdateMessageToByteArray(FLIGHT_DATA_UPDATE_MESSAGE),
                        FLIGHT_DATA_UPDATE_MESSAGE, FLIGHT_DATA_UPDATE_RESPONSE_MSG));
    }

    private static Stream<Arguments> outboundMessageTypesThatSACShouldSendAck() {
        return Stream.of(
                Arguments.of("Store complete", STORED_COMPLETE_REQUEST_MSG, STORED_COMPLETE_ACK_BINARY_MSG,
                        STORED_COMPLETE_ACK_CONVERTED_MSG),
                Arguments.of("Expected receipt response", EXPECTED_RECEIPT_RESPONSE_MSG,
                        EXPECTED_RECEIPT_RESPONSE_ACK_BINARY_MSG, EXPECTED_RECEIPT_RESPONSE_ACK_CONVERTED_MSG),
                Arguments.of("Retrieval order response", RETRIEVAL_FLIGHT_RESPONSE_MSG,
                		RETRIEVAL_FLIGHT_RESPONSE_ACK_BINARY_MSG, RETRIEVAL_FLIGHT_RESPONSE_ACK_CONVERTED_MSG));
    }

    @Test
    void shouldSetTheTimeoutValueToTimeoutCheckerkWhenConnectedIsCalled() throws IOException {
        // connected() is called when a connection is established
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        // The internal timeout checker's connected() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnFalseWhenANullByteArrayIsAddedAsInboundMessage() throws IOException {
        assertFalse(transactionHandler.addInboundMessage(null));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnFalseWhenAnEmptyMessageIsAddedAsInboundMessage() throws IOException {
        assertFalse(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(EMPTY_MSG)));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnFalseWhenAShortMessageIsAddedAsInboundMessage() throws IOException {
        assertFalse(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(TOO_SHORT_MSG)));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnFalseWhenAnInvalidMessageIsAddedAsInboundMessage() throws IOException {
        assertFalse(transactionHandler.addInboundMessage(MessageUtil
                .hexStringToByteArray(INVALID_MSG)));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnTrueWhenKeepAliveIsAddedAsInboundMessage() throws IOException {
        // Keep alive outbound is registered by transaction handler when keep alive inbound is registered
        assertTrue(transactionHandler.addInboundMessage(MessageUtil
                .hexStringToByteArray(KEEP_ALIVE_BINARY_MSG)));

        // The transaction list should have 1 entry
        assertEquals(ONE, transactionHandler.size());

        // timeout checker's received() should be called when keep alive is received
        verify(timeoutChecker, times(1)).ticked();

        // The received message shouldn't be published
        verify(messageService, never()).publishEvent(eq(PORT_NAME), isNull(), anyString(), eq(0),
                eq(MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE),
                eq(MessageEventConsts.HOST_MESG_RECV_TEXT + INTEGRATOR_NAME));

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void shouldReturnTrueWhenExpectedReceiptIsAddedAsInboundMessage(String test, byte[] receivedMessage,
            String convertedMessage, String responseMessage) throws IOException {
        assertTrue(transactionHandler.addInboundMessage(receivedMessage));

        // The transaction list should have 1 entry
        assertEquals(ONE, transactionHandler.size());

        // The received message should be published
        verify(messageService, times(1)).publishEvent(PORT_NAME, null, convertedMessage, 0,
                MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE, MessageEventConsts.HOST_MESG_RECV_TEXT + INTEGRATOR_NAME);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void shouldReturnFalseWhenAckIsAddedAsInboundMessageButThereIsNoRequestSentOut(String test, String outboundMessage,
            String inboundBinaryMessage, String inboundConvertedMessage) throws IOException {
        assertFalse(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(inboundBinaryMessage)));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnFalseWhenANullByteArrayIsAddedAsOutboundMessage() throws IOException {
        assertFalse(transactionHandler.addOutboundMessage(null));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnFalseWhenAnEmptyMessageIsAddedAsOutboundMessage() throws IOException {
        assertFalse(transactionHandler.addOutboundMessage(EMPTY_MSG));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    @Test
    void shouldReturnFalseWhenAKeepAliveMessageIsAddedAsOutboundMessage() throws IOException {
        // Keep alive reply is registered by transaction handler, so it shouldn't be added by addOutboundMessage().
        // This is just for checking it out just in case.
        assertFalse(transactionHandler.addOutboundMessage(KEEP_ALIVE_CONVERTED_MSG));

        // The transaction list should be empty
        assertEquals(NONE, transactionHandler.size());

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // Keep alive transaction
    // - Connected
    // - AirflowWCS <--(Keep alive)--- SAC
    // - AirflowWCS ---(Keep alive)--> SAC
    @Test
    void shouldProcessKeepAliveTransactionWhenARequestIsSentAndAnAckIsReceived() throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Register the received message to transaction handler
        // - Keep alive reply should be registered by transactionHandler internally
        assertTrue(transactionHandler.addInboundMessage(MessageUtil
                .hexStringToByteArray(KEEP_ALIVE_BINARY_MSG)));

        // The transaction list should have 1 entry
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - The keep alive should be removed from the internal transaction list
        assertTrue(transactionHandler.process());

        // The transaction list should be empty
        assertEquals(ONE, transactionHandler.size());

        // The internal timeout checker's connected() and received() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT); // When connected
        verify(timeoutChecker, times(1)).ticked(); // When keep alive is received

        // The keep alive message should be sent out
        verify(outputStream, times(2)).write(any(byte[].class));
        verify(outputStream, times(2)).flush();

        // The received message shouldn't be published
        verify(messageService, never()).publishEvent(eq(PORT_NAME), isNull(), anyString(), eq(0),
                eq(MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE),
                eq(MessageEventConsts.HOST_MESG_RECV_TEXT + INTEGRATOR_NAME));

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // Request received and ack sent out
    // - Connected
    // - AirflowWCS <--(req)--- SAC
    // - AirflowWCS ---(ack)--> SAC
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void shouldProcessTransactionWhenARequestIsReceivedAndAnAckIsSent(String test, byte[] receivedMessage,
            String convertedMessage, String ackMessageToSend) throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Register the received message to transaction handler
        assertTrue(transactionHandler.addInboundMessage(receivedMessage));

        // The transaction list should have 1 entry created for the inbound message
        assertEquals(ONE, transactionHandler.size());

        // Simulate a new response messaget
        assertTrue(transactionHandler.addOutboundMessage(ackMessageToSend));

        // The transaction list should have 1 entry created for the pair of inbound/outbound messages
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - The req/res should be removed from the internal transaction list
        assertTrue(transactionHandler.process());

        // The transaction list should be empty
        assertEquals(ONE, transactionHandler.size());

        // The internal timeout checker's connected() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT);

        // The response message should be sent out
        verify(outputStream, times(2)).write(any(byte[].class));
        verify(outputStream, times(2)).flush();

        // The received message should be published
        verify(messageService, times(1)).publishEvent(PORT_NAME, null, convertedMessage, 0,
                MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE, MessageEventConsts.HOST_MESG_RECV_TEXT + INTEGRATOR_NAME);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // Request sent and ack received transaction
    // - Connected
    // - AirflowWCS ---(req)--> SAC
    // - AirflowWCS <--(ack)--- SAC
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void shouldProcessTransactionWhenARequestIsSentAndAnAckIsReceived(String test, String outboundMessage,
            String inboundBinaryMessage, String inboundConvertedMessage) throws IOException {
        // Simulate the established connection
        transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

        // Register the received message to transaction handler
        assertTrue(transactionHandler.addOutboundMessage(outboundMessage));

        // The transaction list should have 1 entry created for the inbound message
        assertEquals(ONE, transactionHandler.size());

        // Run process() to simulate 1 cycle
        // - The req should be sent out
        assertTrue(transactionHandler.process());

        // Simulate a new ack message received for the request
        // - ack is received
        assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(inboundBinaryMessage)));

        // Run process() to simulate 1 cycle
        // - The req/res should be removed from the internal transaction list
        assertTrue(transactionHandler.process());

        // The transaction list should be empty
        assertEquals(ONE, transactionHandler.size());

        // The internal timeout checker's connected() should be called
        verify(timeoutChecker, times(1)).started(KEEP_ALIVE_TIMEOUT);

        // The request message should be sent out
        verify(outputStream, times(2)).write(any(byte[].class));
        verify(outputStream, times(2)).flush();

        // The received message should be published
        verify(messageService, times(1)).publishEvent(PORT_NAME, null, inboundConvertedMessage, 0,
                MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE, MessageEventConsts.HOST_MESG_RECV_TEXT + INTEGRATOR_NAME);

        verifyNoMoreInteractions(messageService, timeoutChecker, outputStream);
    }

    // Retransmitting a pending request
    // - Connected
    // - AirflowWCS ---(req)--> SAC
    // - Ack timeout
    // - AirflowWCS ---(req)--> SAC
    // - AirflowWCS <--(ack)--- SAC
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void shouldRetransmitAPendingRequestWhenAckIsNotReceivedWithinTimeout(String test, String outboundMessage,
            String inboundBinaryMessage, String inboundConvertedMessage) throws IOException {

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(() -> LocalDateTime.now())
                    // connected()
                    .thenReturn(WHEN_SENT)
                    // markAsSent() after the 1st transmission
                    .thenReturn(WHEN_SENT.plus(1, ChronoUnit.SECONDS))
                    // isAckTimedOutButCanRetry()
                    .thenReturn(WHEN_SENT.plus(1 + 1 + ACK_TIMEOUT, ChronoUnit.SECONDS))
                    // the 2nd transmission
                    .thenReturn(WHEN_SENT.plus(1 + 1 + ACK_TIMEOUT + 1, ChronoUnit.SECONDS))
                    // markAsReceived() after the ack message was registered
                    .thenReturn(WHEN_SENT.plus(1 + 1 + ACK_TIMEOUT + 1 + 1, ChronoUnit.SECONDS));

            doCallRealMethod().when(timeoutChecker).started(anyLong());

            // Simulate the established connection
            transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

            // Add a new request message
            assertTrue(transactionHandler.addOutboundMessage(outboundMessage));

            // The transaction list should have 1 entry created for the outbound message
            assertEquals(ONE, transactionHandler.size());

            // Run process() to simulate the 1st cycle
            // - Move order request is sent out
            assertTrue(transactionHandler.process());

            // The transaction list should still have the entry created for the outbound message
            assertEquals(TWO, transactionHandler.size());

            // Run process() to simulate the 2nd cycle
            // - The request is sent out again
            assertTrue(transactionHandler.process());

            // The transaction list should still have the entry created for the outbound message
            assertEquals(TWO, transactionHandler.size());

            // Now add a matching inbound message
            assertTrue(transactionHandler.addInboundMessage(MessageUtil.hexStringToByteArray(inboundBinaryMessage)));

            // Run process() to simulate the 3rd cycle
            assertTrue(transactionHandler.process());

            // The transaction list should be empty as the matching inbound message was received
            assertEquals(ONE, transactionHandler.size());

            // The request message should be sent out only 2 times
            // - the 1st cycle: the initial transmission
            // - the 2nd cycle: the 1st retransmission
            // - the 3rd cycle: ack was received, so no need of retransmission
            verify(outputStream, times(3)).write(any(byte[].class));
            verify(outputStream, times(3)).flush();
        }
    }

    // Retransmitting a pending request only up to 3 times
    // - Connected
    // - AirflowWCS ---(req)--> SAC (initial transmission)
    // - Ack timeout
    // - AirflowWCS ---(req)--> SAC
    // - Ack timeout
    // - AirflowWCS ---(req)--> SAC
    // - Ack timeout
    // - AirflowWCS ---(req)--> SAC
    // - Ack timeout
    // - No more retransmission
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void shouldRetransmitAPendingRequestWhenAckIsNotReceivedWithinTimeoutButOnlyUpToMaxRetry(String test,
            String outboundMessage, String inboundBinaryMessage, String inboundConvertedMessage) throws IOException {
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(() -> LocalDateTime.now())
                    // connected()
                    .thenReturn(WHEN_SENT)
                    // the initial transmission
                    .thenReturn(WHEN_SENT.plus(0, ChronoUnit.SECONDS))
                    // isAckTimedOutButCanRetry()
                    .thenReturn(WHEN_SENT.plus(ACK_TIMEOUT + 1, ChronoUnit.SECONDS))
                    // the 1st retransmission
                    .thenReturn(WHEN_SENT.plus(ACK_TIMEOUT + 1, ChronoUnit.SECONDS))
                    // isAckTimedOutButCanRetry()
                    .thenReturn(WHEN_SENT.plus(ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1, ChronoUnit.SECONDS))
                    // the 2nd retransmission
                    .thenReturn(WHEN_SENT.plus(ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1, ChronoUnit.SECONDS))
                    // isAckTimedOutButCanRetry()
                    .thenReturn(WHEN_SENT.plus(ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1, ChronoUnit.SECONDS))
                    // the 3rd retransmission
                    .thenReturn(WHEN_SENT.plus(ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1, ChronoUnit.SECONDS))
                    // isAckTimedOutAndNoMoreRetryAllowed()
                    .thenReturn(WHEN_SENT.plus(ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1 + ACK_TIMEOUT + 1,
                            ChronoUnit.SECONDS));

            doCallRealMethod().when(timeoutChecker).started(anyLong());

            // Simulate the established connection
            transactionHandler.connected(KEEP_ALIVE_TIMEOUT, outputStream);

            // Add a new request message
            assertTrue(transactionHandler.addOutboundMessage(outboundMessage));

            // The transaction list should have 1 entry created for the outbound message
            assertEquals(ONE, transactionHandler.size());

            // Run process() to simulate the 1st cycle
            // - request is sent out
            assertTrue(transactionHandler.process());

            // The transaction list should still have the entry created for the outbound message
            assertEquals(TWO, transactionHandler.size());

            // Run process() to simulate the 2nd cycle
            // - The request is sent out again
            assertTrue(transactionHandler.process());

            // The transaction list should still have the entry created for the outbound message
            assertEquals(TWO, transactionHandler.size());

            // Run process() to simulate the 3rd cycle
            // - The request is sent out again
            assertTrue(transactionHandler.process());

            // Run process() to simulate the 4th cycle
            // - The request is sent out again
            assertTrue(transactionHandler.process());

            // The transaction list should be empty as the matching inbound message was received
            assertEquals(TWO, transactionHandler.size());

            // Run process() to simulate the 3rd cycle
            assertTrue(transactionHandler.process());

            // The transaction list should be empty as the matching inbound message was received
            assertEquals(TWO, transactionHandler.size());

            // The request message should be sent out 4 times
            // - the 1st cycle: the initial transmission
            // - the 2nd ~ 4th cycle: retransmission up to 3 times
            verify(outputStream, times(6)).write(any(byte[].class));
            verify(outputStream, times(6)).flush();
        }
    }

    private static byte[] convertCSVFormattedExpectedReceiptMessageToByteArray(String csvFormattedMessage) {
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

    private static byte[] convertCSVFormattedExpectedReceiptAckMessageToByteArray(String csvFormattedMessage) {
        String[] split = csvFormattedMessage.split(",");
        ByteBuffer buffer = ByteBuffer.allocate(SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_LEN);
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
        buffer.putShort(Short.parseShort(split[8]));
        return buffer.array();
    }

    private static byte[] convertCSVFormattedRetrievalOrderWithoutListMessageToByteArray(String csvFormattedMessage) {
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

    private static byte[] convertCSVFormattedFlightDataUpdateMessageToByteArray(String csvFormattedMessage) {
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
}
