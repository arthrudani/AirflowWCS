package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuoc.wrxj.custom.ebs.communication.Message;
import com.daifukuoc.wrxj.custom.ebs.communication.Transaction;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;

@ExtendWith(MockitoExtension.class)
class SACTransactionTest {

    private static final short SEQUENCE_NUMBER = (short) 999;
    private static final byte[] ARBITRARY_MESSAGE_FOR_TESTING_ONLY = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00 };

    private static final LocalDateTime WHEN_SENT = LocalDateTime.parse("2023-02-08T00:00:00");

    @Mock
    Message inbound;

    @Mock
    Message outbound;

    @Test
    void shouldThrowAnExceptionWhenInboundAndOutboundAreNullAtTheSameTime() {
        try {
            new SACTransaction(null, null);
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    @Test
    void shouldThrowAnExceptionWhenInboundAndOutboundAreNotNullAtTheSameTime() {
        try {
            new SACTransaction(
                    new Message(null, SACControlMessage.KEEPALIVE_MSG_TYPE, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""),
                    new Message(SACControlMessage.KEEPALIVE_MSG_TYPE, null, ""));
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    @Test
    void shouldThrowAnExceptionWhenInboundMessageTypeIsInvalid() {
        try {
            new SACTransaction(new Message(null, Short.MAX_VALUE, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""),
                    null);
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    @Test
    void shouldThrowAnExceptionWhenOutboundMessageTypeIsInvalid() {
        try {
            new SACTransaction(null, new Message(Short.MAX_VALUE, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    private static Stream<Arguments> inboundMessageTypesThatAirflowWCSShouldSendAck() {
        return Stream.of(
                Arguments.of("Keep alive", SACControlMessage.KEEPALIVE_MSG_TYPE, SACControlMessage.KEEPALIVE_MSG_TYPE),
                Arguments.of("Expected receipt", SACControlMessage.EXPECTED_RECIEPT_MSG_TYPE,
                        SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_TYPE),
                Arguments.of("Retrieval flight", SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE,
                        SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_ACK_MSG_TYPE),
                Arguments.of("Flight data update", SACControlMessage.FLIGHT_DATA_UPDATE_MSG_TYPE,
                        SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE),
                Arguments.of("Inventory request by Warehouse", SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_MSG_TYPE,
                		SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE));
    }

    private static short firstNonMatchingInboundMessageType(short inboundMessageType) {
        Optional<Arguments> firstNonMatching = inboundMessageTypesThatAirflowWCSShouldSendAck().filter(argument -> {
            return (short) argument.get()[1] != inboundMessageType;
        }).findFirst();

        if (firstNonMatching.isPresent()) {
            return (short) (firstNonMatching.get().get()[1]);
        }
        return (short) 0;
    }

    private static Stream<Arguments> outboundMessageTypesThatSACShouldSendAck() {
        return Stream.of(
                Arguments.of("Store complete", SACControlMessage.StoreCompletionNotify.MSG_TYPE,
                        SACControlMessage.STORED_COMPLETE_ACK_MSG_TYPE),
                Arguments.of("Expected receipt response", SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE,
                        SACControlMessage.EXPECTED_RECIEPT_RESPONSE_ACK_MSG_TYPE),
                Arguments.of("Retrieval order response", SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_MSG_TYPE,
                        SACControlMessage.RETRIEVAL_FLIGHT_RESPONSE_ACK_MSG_TYPE));
    }

    private static short firstNonMatchingOutboundMessageType(short outboundMessageType) {
        Optional<Arguments> firstNonMatching = outboundMessageTypesThatSACShouldSendAck().filter(argument -> {
            return (short) argument.get()[1] != outboundMessageType;
        }).findFirst();

        if (firstNonMatching.isPresent()) {
            return (short) (firstNonMatching.get().get()[1]);
        }
        return (short) 0;
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void isCompletedShouldReturnTrueOnlyWhenInboundIsReceivedAndOutboundIsSent(String test, short inboundMessageType,
            short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new SACTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // The transaction is not completed as outbound ack message is not sent yet
        assertFalse(receivedButAckIsNotPrepared.isCompleted());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new SACTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // The transaction is not completed as outbound ack message is not sent yet
        assertFalse(receivedButNotSentYet.isCompleted());

        // Inbound request message is received
        Transaction receivedAndSent = new SACTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // The transaction is now completed
        assertTrue(receivedAndSent.isCompleted());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void isCompletedShouldReturnTrueOnlyWhenOutboundIsSentAndInboundIsReceived(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // The transaction is not completed as outbound message is not sent yet
        assertFalse(newRequestButNotSent1.isCompleted());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // The transaction is not completed as outbound message is not sent yet
        assertFalse(newRequestButNotSent2.isCompleted());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // The transaction is not completed as ack message is not received yet
        assertFalse(sentButNotAckedYet.isCompleted());

        // Outbound request message is registered
        Transaction sentAndAcked = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentAndAcked.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentAndAcked.markOutboundAsSent();
        // Ack is received
        sentAndAcked.setReceivedInbound(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // The transaction is now completed
        assertTrue(sentAndAcked.isCompleted());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void shouldSendReplyShouldReturnTrueOnlyWhenInboundIsReceivedButOutboundIsNotSent(String test,
            short inboundMessageType, short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new SACTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // No outbound is registered
        assertFalse(receivedButAckIsNotPrepared.shouldSendReply());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new SACTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // The reply message should be sent out
        assertTrue(receivedButNotSentYet.shouldSendReply());

        // Inbound request message is received
        Transaction receivedAndSent = new SACTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // The reply message was already sent out
        assertFalse(receivedAndSent.shouldSendReply());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void shouldSendReplyShouldReturnFalseAlwaysWhenRequestIsSentByAirflowWCS(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Always false as this is not an inbound message
        assertFalse(newRequestButNotSent1.shouldSendReply());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // Always false as this is not an inbound message
        assertFalse(newRequestButNotSent2.shouldSendReply());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // Always false as this is not an inbound message
        assertFalse(sentButNotAckedYet.shouldSendReply());

        // Outbound request message is registered
        Transaction sentAndAcked = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentAndAcked.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentAndAcked.markOutboundAsSent();
        // Ack is received
        sentAndAcked.setReceivedInbound(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Always false as this is not an inbound message
        assertFalse(sentAndAcked.shouldSendReply());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void isPendingShouldReturnFalseAlwaysWhenRequestIsSentBySAC(String test, short inboundMessageType,
            short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new SACTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // This is not a valid case
        assertFalse(receivedButAckIsNotPrepared.isPending());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new SACTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet
                .setOutboundToSend(new Message(outboundMessageType, null, ""));
        // This is not a valid case
        assertFalse(receivedButNotSentYet.isPending());

        // Inbound request message is received
        Transaction receivedAndSent = new SACTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // This is not a valid case
        assertFalse(receivedAndSent.isPending());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void isPendingShouldReturnTrueOnlyWhenOutboundIsSentButAckIsNotReceived(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Not sent out yet
        assertFalse(newRequestButNotSent1.isPending());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // Still not sent out yet
        assertFalse(newRequestButNotSent2.isPending());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // Now this is a pending transaction
        assertTrue(sentButNotAckedYet.isPending());

        // Outbound request message is registered
        Transaction sentAndAcked = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentAndAcked.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentAndAcked.markOutboundAsSent();
        // Ack is received
        sentAndAcked.setReceivedInbound(new Message(null, inboundMessageType,
                ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // As ack is received, no longer pending
        assertFalse(sentAndAcked.isPending());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void canSendRequestShouldReturnFalseAlwaysWhenRequestIsSentBySAC(String test, short inboundMessageType,
            short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new SACTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // This is not a valid case
        assertFalse(receivedButAckIsNotPrepared.canSendRequest());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new SACTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet
                .setOutboundToSend(new Message(SACControlMessage.StoreCompletionNotify.MSG_TYPE, null, ""));
        // This is not a valid case
        assertFalse(receivedButNotSentYet.canSendRequest());

        // Inbound request message is received
        Transaction receivedAndSent = new SACTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // This is not a valid case
        assertFalse(receivedAndSent.canSendRequest());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatSACShouldSendAck")
    void canSendRequestShouldReturnTrueOnlyWhenOutboundIsRegisteredButNotSent(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Now this transaction can be sent out
        assertTrue(newRequestButNotSent1.canSendRequest());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // Still not sent out yet
        assertTrue(newRequestButNotSent2.canSendRequest());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // Already sent out, so can't send this out again
        assertFalse(sentButNotAckedYet.canSendRequest());

        // Outbound request message is registered
        Transaction sentAndAcked = new SACTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentAndAcked.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentAndAcked.markOutboundAsSent();
        // Ack is received
        sentAndAcked.setReceivedInbound(new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Already sent out, so can't send this out again
        assertFalse(sentAndAcked.canSendRequest());
    }

    // Prepare the messages for the following unit test method
    private static Stream<Arguments> isMatchingReceivedInboundList() {

        Stream<Arguments> mergedArguments = Stream.empty();

        List<Arguments> inboundMessageTypes = inboundMessageTypesThatAirflowWCSShouldSendAck()
                .collect(Collectors.toList());
        for (Arguments arguments : inboundMessageTypes) {
            String test = (String) arguments.get()[0];
            short inboundMessageType = (short) arguments.get()[1];
            short outboundMessageType = (short) arguments.get()[2];
            mergedArguments = Stream.concat(mergedArguments, Stream.of(
                    Arguments.of(test + " - Both message type and sequence number matches",
                            new SACTransaction(new Message(SEQUENCE_NUMBER, inboundMessageType,
                                    ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null),
                            outboundMessageType, SEQUENCE_NUMBER, true),
                    Arguments.of(test + " - Only message type matches",
                            new SACTransaction(new Message(SEQUENCE_NUMBER, inboundMessageType,
                                    ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null),
                            outboundMessageType, Short.MAX_VALUE, false),
                    Arguments.of(test + " - Only sequence number matches",
                            new SACTransaction(new Message(SEQUENCE_NUMBER,
                                    firstNonMatchingInboundMessageType(inboundMessageType),
                                    ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null),
                            outboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test + " - Message type and sequence number doesn't match",
                            new SACTransaction(new Message(SEQUENCE_NUMBER,
                                    firstNonMatchingInboundMessageType(inboundMessageType),
                                    ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null),
                            outboundMessageType, Short.MAX_VALUE, false)));
        }

        return mergedArguments;
    }

    @ParameterizedTest(name = "{index}: {0} -> {4}")
    @MethodSource("isMatchingReceivedInboundList")
    void isMatchingReceivedInboundShouldReturnTrueOnlyWhenMessageTypeAndSequenceNumberMatches(String test,
            Transaction transaction, short outboundMessageType, short outboundSequenceNumber, boolean expectedResult) {
        assertEquals(expectedResult,
                transaction.isMatchingReceivedInbound(outboundMessageType, outboundSequenceNumber));
    }

    private static Stream<Arguments> isMatchingSentOutboundList() {
        Stream<Arguments> mergedArguments = Stream.empty();

        List<Arguments> outboundMessageTypes = outboundMessageTypesThatSACShouldSendAck().collect(Collectors.toList());
        for (Arguments arguments : outboundMessageTypes) {
            String test = (String) arguments.get()[0];
            short outboundMessageType = (short) arguments.get()[1];
            short inboundMessageType = (short) arguments.get()[2];
            mergedArguments = Stream.concat(mergedArguments, Stream.of(
                    Arguments.of(
                            test + " - Sequence number not allocated: Both message type and sequence number matches",
                            new SACTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            null, false, inboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test
                            + " - Sequence number allocated but not sent: Both message type and sequence number matches",
                            new SACTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, false, inboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test
                            + " - Sequence number allocated and sent: Both message type and sequence number matches",
                            new SACTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, true, inboundMessageType, SEQUENCE_NUMBER, true),
                    Arguments.of(test + " - Sequence number allocated and sent: Only message type matches",
                            new SACTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, true, inboundMessageType, Short.MAX_VALUE, false),
                    Arguments.of(test + " - Sequence number allocated and sent: Only sequence number matches",
                            new SACTransaction(null,
                                    new Message(firstNonMatchingOutboundMessageType(outboundMessageType),
                                            ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, true, inboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test
                            + " - Sequence number allocated and sent: Message type and sequence number doesn't match",
                            new SACTransaction(null,
                                    new Message(firstNonMatchingOutboundMessageType(outboundMessageType),
                                            ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, true, inboundMessageType, Short.MAX_VALUE, false)));
        }

        return mergedArguments;
    }

    @ParameterizedTest(name = "{index}: {0} -> {6}")
    @MethodSource("isMatchingSentOutboundList")
    void isMatchingSentOutboundShouldReturnTrueOnlyWhenMessageTypeAndSequenceNumberMatches(String test,
            Transaction transaction, Short outboundSequenceNumber, boolean sent, short inboundMessageType,
            short inboundSequenceNumber, boolean expectedResult) {
        if (outboundSequenceNumber != null) {
            transaction.setOutboundSequenceNumber(outboundSequenceNumber);
        }
        if (sent) {
            transaction.markOutboundAsSent();
        }
        assertEquals(expectedResult,
                transaction.isMatchingSentOutbound(inboundMessageType, inboundSequenceNumber));
    }

    private static Stream<Arguments> ackTimeoutList() {
        return Stream.of(
                Arguments.of(
                        "The request is not sent yet",
                        new SACTransaction(null, new Message(SACControlMessage.StoreCompletionNotify.MSG_TYPE,
                                ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                        false, false, false, false),
                Arguments.of(
                        "The request is sent and ack is not received within timeout, but it's allowed to continue to wait",
                        new SACTransaction(null, new Message(SACControlMessage.StoreCompletionNotify.MSG_TYPE,
                                ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                        true, false, true, false),
                Arguments.of(
                        "The request is sent and ack is not received within timeout after max retry",
                        new SACTransaction(null, new Message(SACControlMessage.StoreCompletionNotify.MSG_TYPE,
                                ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                        true, true, false, true));
    }

    @ParameterizedTest(name = "{index}: {0} -> {4}")
    @MethodSource("ackTimeoutList")
    void isAckTimedOutButCanRetryShouldReturnTrueOnlyWhenItIsAllowedToContinueToWaitForAck(String test,
            Transaction transaction, boolean sent1, boolean sent2, boolean isAckTimedOutButCanRetry,
            boolean isAckTimedOutAndNoMoreRetryAllowed) {

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            long timeout = 10;
            long delay = 1;
            int maxRetry = 1;

            if (sent1 && !sent2) {
                transaction.setOutboundSequenceNumber((short) 111);

                mockedLocalDateTime.when(() -> LocalDateTime.now())
                        .thenReturn(WHEN_SENT) // markOutboundAsSent(..)
                        .thenReturn(WHEN_SENT.plus(timeout + delay, ChronoUnit.SECONDS)); // isAckTimedOutButCanRetry(...)

                transaction.markOutboundAsSent();
            } else if (sent1 && sent2) {
                transaction.setOutboundSequenceNumber((short) 111);

                mockedLocalDateTime.when(() -> LocalDateTime.now())
                        .thenReturn(WHEN_SENT) // markOutboundAsSent(..)
                        .thenReturn(WHEN_SENT.plus(timeout + delay + timeout, ChronoUnit.SECONDS)) // markOutboundAsSent(..)
                        .thenReturn(WHEN_SENT.plus(timeout + delay + timeout + delay + timeout, ChronoUnit.SECONDS)); // isAckTimedOutButCanRetry(...)

                transaction.markOutboundAsSent();
                transaction.markOutboundAsSent();
            }

            assertEquals(isAckTimedOutButCanRetry, transaction.isAckTimedOutButCanRetry(timeout, maxRetry));
        }
    }

    @ParameterizedTest(name = "{index}: {0} -> {5}")
    @MethodSource("ackTimeoutList")
    void isAckTimedOutAndNoMoreRetryAllowedShouldReturnTrueOnlyWhenItIsAllowedToContinueToWaitForAck(String test,
            Transaction transaction, boolean sent1, boolean sent2, boolean isAckTimedOutButCanRetry,
            boolean isAckTimedOutAndNoMoreRetryAllowed) {

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            long timeout = 10;
            long delay = 1;
            int maxRetry = 1;

            if (sent1 && !sent2) {
                transaction.setOutboundSequenceNumber((short) 111);

                mockedLocalDateTime.when(() -> LocalDateTime.now())
                        .thenReturn(WHEN_SENT) // markOutboundAsSent(..)
                        .thenReturn(WHEN_SENT.plus(timeout + delay, ChronoUnit.SECONDS)); // isAckTimedOutAndNoMoreRetryAllowed(...)

                transaction.markOutboundAsSent();
            } else if (sent1 && sent2) {
                transaction.setOutboundSequenceNumber((short) 111);

                mockedLocalDateTime.when(() -> LocalDateTime.now())
                        .thenReturn(WHEN_SENT) // markOutboundAsSent(..)
                        .thenReturn(WHEN_SENT.plus(timeout + delay + timeout, ChronoUnit.SECONDS)) // markOutboundAsSent(..)
                        .thenReturn(WHEN_SENT.plus(timeout + delay + timeout + delay + timeout, ChronoUnit.SECONDS)); // isAckTimedOutAndNoMoreRetryAllowed(...)

                transaction.markOutboundAsSent();
                transaction.markOutboundAsSent();
            }

            assertEquals(isAckTimedOutAndNoMoreRetryAllowed,
                    transaction.isAckTimedOutAndNoMoreRetryAllowed(timeout, maxRetry));
        }
    }
}
