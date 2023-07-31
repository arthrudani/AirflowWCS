package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuoc.wrxj.custom.ebs.communication.Message;
import com.daifukuoc.wrxj.custom.ebs.communication.Transaction;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionType;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

@ExtendWith(MockitoExtension.class)
class ACPTransactionTest {

    private static final short SEQUENCE_NUMBER = (short) 999;
    private static final byte[] ARBITRARY_MESSAGE_FOR_TESTING_ONLY = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00 };

    @Mock
    Message inbound;

    @Mock
    Message outbound;

    private static Stream<Arguments> inboundMessageTypesThatAirflowWCSShouldSendAck() {
        return Stream.of(
                Arguments.of("Keep alive", PLCConstants.KEEPALIVE_MSG_TYPE, PLCConstants.KEEPALIVE_MSG_TYPE),
                Arguments.of("Item arrived", PLCConstants.PLC_ITEM_ARRIVED_MSG_TYPE_INT,
                        PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE_INT),
                Arguments.of("Item arrived", PLCConstants.PLC_ITEM_RELEASED_MSG_TYPE_INT,
                        PLCConstants.PLC_ITEM_RELEASED_ACK_MSG_TYPE_INT),
                Arguments.of("Item stored", PLCConstants.PLC_ITEM_STORED_MSG_TYPE_INT,
                        PLCConstants.PLC_ITEM_STORED_ACK_MSG_TYPE_INT),
                Arguments.of("Item picked up", PLCConstants.PLC_ITEM_PICKEDUP_MSG_TYPE_INT,
                        PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT),
                Arguments.of("Location status", PLCConstants.PLC_LOCATION_STATUS_MSG_TYPE_INT,
                        PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE_INT));
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

    private static Stream<Arguments> outboundMessageTypesThatACPShouldSendAck() {
        return Stream.of(
                Arguments.of("Move order", PLCConstants.PLC_MOVE_ORDER_REQUEST_MSG_TYPE_INT,
                        PLCConstants.PLC_MOVE_ORDER_ACK_MSG_TYPE_INT),
                Arguments.of("Flight data update", PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE_INT,
                        PLCConstants.PLC_FLIGHT_DATA_UPDATE_ACK_MSG_TYPE_INT),
                Arguments.of("Flush request", PLCConstants.PLC_FLUSH_REQUEST_MSG_TYPE_INT,
                		PLCConstants.PLC_FLUSH_REQUEST_ACK_MSG_TYPE_INT),
                Arguments.of("Bag data update", PLCConstants.PLC_BAG_DATA_UPDATE_MSG_TYPE_INT,
                        PLCConstants.PLC_BAG_DATA_UPDATE_ACK_MSG_TYPE_INT));
    }

    private static short firstNonMatchingOutboundMessageType(short outboundMessageType) {
        Optional<Arguments> firstNonMatching = outboundMessageTypesThatACPShouldSendAck().filter(argument -> {
            return (short) argument.get()[1] != outboundMessageType;
        }).findFirst();

        if (firstNonMatching.isPresent()) {
            return (short) (firstNonMatching.get().get()[1]);
        }
        return (short) 0;
    }

    @Test
    void shouldThrowAnExceptionWhenInboundAndOutboundAreNullAtTheSameTime() {
        try {
            new ACPTransaction(null, null);
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    @Test
    void shouldThrowAnExceptionWhenInboundAndOutboundAreNotNullAtTheSameTime() {
        try {
            new ACPTransaction(
                    new Message(null, PLCConstants.KEEPALIVE_MSG_TYPE, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""),
                    new Message(PLCConstants.KEEPALIVE_MSG_TYPE, null, ""));
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    @Test
    void shouldThrowAnExceptionWhenInboundMessageTypeIsInvalid() {
        try {
            new ACPTransaction(new Message(null, Short.MAX_VALUE, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""),
                    null);
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    @Test
    void shouldThrowAnExceptionWhenOutboundMessageTypeIsInvalid() {
        try {
            new ACPTransaction(null, new Message(Short.MAX_VALUE, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
            fail("Should throw an exception");
        } catch (Exception e) {
        }
    }

    private static Stream<Arguments> inboundTransactionTypePerMessageType() {
        Stream<Arguments> mergedArguments = Stream.empty();

        for (Arguments arguments : inboundMessageTypesThatAirflowWCSShouldSendAck().collect(Collectors.toList())) {
            String test = (String) arguments.get()[0];
            short messageType = (short) arguments.get()[1];

            mergedArguments = Stream.concat(mergedArguments,
                    Stream.of(Arguments.of(test, messageType, TransactionType.RECEIVED_REQUEST_THAT_WCS_SHOULD_ACK)));
        }

        return mergedArguments;
    }

    private static Stream<Arguments> outboundTransactionTypePerMessageType() {
        Stream<Arguments> mergedArguments = Stream.empty();

        for (Arguments arguments : outboundMessageTypesThatACPShouldSendAck().collect(Collectors.toList())) {
            String test = (String) arguments.get()[0];
            short messageType = (short) arguments.get()[1];

            mergedArguments = Stream.concat(mergedArguments,
                    Stream.of(Arguments.of(test, messageType, TransactionType.REQUEST_TO_SEND_THAT_SHOULD_BE_ACKED)));
        }

        return mergedArguments;
    }

    @ParameterizedTest(name = "{index}: {0} -> {2}")
    @MethodSource("inboundTransactionTypePerMessageType")
    void determineInboundTransactionTypeShouldReturnsExpectedTransactionType(String test, short messageType,
            TransactionType expectedTransactionType) {
        Message message = mock(Message.class);
        doReturn(messageType).when(message).getType();

        ACPTransaction acpTransaction = new ACPTransaction(message, null);
        assertEquals(expectedTransactionType, acpTransaction.determineInboundTransactionType(message));
    }

    @ParameterizedTest(name = "{index}: {0} -> {2}")
    @MethodSource("outboundTransactionTypePerMessageType")
    void determineOutboundTransactionTypeShouldReturnsExpectedTransactionType(String test, short messageType,
            TransactionType expectedTransactionType) {
        Message message = mock(Message.class);
        doReturn(messageType).when(message).getType();

        ACPTransaction acpTransaction = new ACPTransaction(null, message);
        assertEquals(expectedTransactionType, acpTransaction.determineOutboundTransactionType(message));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void isCompletedShouldReturnTrueOnlyWhenInboundIsReceivedAndOutboundIsSent(String test, short inboundMessageType,
            short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new ACPTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // The transaction is not completed as outbound ack message is not sent yet
        assertFalse(receivedButAckIsNotPrepared.isCompleted());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new ACPTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // The transaction is not completed as outbound ack message is not sent yet
        assertFalse(receivedButNotSentYet.isCompleted());

        // Inbound request message is received
        Transaction receivedAndSent = new ACPTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // The transaction is now completed
        assertTrue(receivedAndSent.isCompleted());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void isCompletedShouldReturnTrueOnlyWhenOutboundIsSentAndInboundIsReceived(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // The transaction is not completed as outbound message is not sent yet
        assertFalse(newRequestButNotSent1.isCompleted());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // The transaction is not completed as outbound message is not sent yet
        assertFalse(newRequestButNotSent2.isCompleted());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // The transaction is not completed as ack message is not received yet
        assertFalse(sentButNotAckedYet.isCompleted());

        // Outbound request message is registered
        Transaction sentAndAcked = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentAndAcked.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentAndAcked.markOutboundAsSent();
        // Ack is received
        sentAndAcked.setReceivedInbound(new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // The transaction is now completed
        assertTrue(sentAndAcked.isCompleted());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void shouldSendReplyShouldReturnTrueOnlyWhenInboundIsReceivedButOutboundIsNotSent(String test,
            short inboundMessageType, short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new ACPTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // No outbound is registered
        assertFalse(receivedButAckIsNotPrepared.shouldSendReply());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new ACPTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet.setOutboundToSend(new Message(PLCConstants.KEEPALIVE_MSG_TYPE, null, ""));
        // The reply message should be sent out
        assertTrue(receivedButNotSentYet.shouldSendReply());

        // Inbound request message is received
        Transaction receivedAndSent = new ACPTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // The reply message was already sent out
        assertFalse(receivedAndSent.shouldSendReply());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void shouldSendReplyShouldReturnFalseAlwaysWhenRequestIsSentByAirflowWCS(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Always false as this is not an inbound message
        assertFalse(newRequestButNotSent1.shouldSendReply());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // Always false as this is not an inbound message
        assertFalse(newRequestButNotSent2.shouldSendReply());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // Always false as this is not an inbound message
        assertFalse(sentButNotAckedYet.shouldSendReply());

        // Outbound request message is registered
        Transaction sentAndAcked = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentAndAcked.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentAndAcked.markOutboundAsSent();
        // Ack is received
        sentAndAcked.setReceivedInbound(new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Always false as this is not an inbound message
        assertFalse(sentAndAcked.shouldSendReply());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void isPendingShouldReturnFalseAlwaysWhenRequestIsSentByACP(String test, short inboundMessageType,
            short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new ACPTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // This is not a valid case
        assertFalse(receivedButAckIsNotPrepared.isPending());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new ACPTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet
                .setOutboundToSend(new Message(outboundMessageType, null, ""));
        // This is not a valid case
        assertFalse(receivedButNotSentYet.isPending());

        // Inbound request message is received
        Transaction receivedAndSent = new ACPTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // This is not a valid case
        assertFalse(receivedAndSent.isPending());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void isPendingShouldReturnTrueOnlyWhenOutboundIsSentButAckIsNotReceived(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Not sent out yet
        assertFalse(newRequestButNotSent1.isPending());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // Still not sent out yet
        assertFalse(newRequestButNotSent2.isPending());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // Now this is a pending transaction
        assertTrue(sentButNotAckedYet.isPending());

        // Outbound request message is registered
        Transaction sentAndAcked = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentAndAcked.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentAndAcked.markOutboundAsSent();
        // Ack is received
        sentAndAcked.setReceivedInbound(new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // As ack is received, no longer pending
        assertFalse(sentAndAcked.isPending());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("inboundMessageTypesThatAirflowWCSShouldSendAck")
    void canSendRequestShouldReturnFalseAlwaysWhenRequestIsSentByACP(String test, short inboundMessageType,
            short outboundMessageType) {
        // Inbound request message is received
        Transaction receivedButAckIsNotPrepared = new ACPTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // This is not a valid case
        assertFalse(receivedButAckIsNotPrepared.canSendRequest());

        // Inbound request message is received
        Transaction receivedButNotSentYet = new ACPTransaction(new Message(null,
                inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedButNotSentYet
                .setOutboundToSend(new Message(outboundMessageType, null, ""));
        // This is not a valid case
        assertFalse(receivedButNotSentYet.canSendRequest());

        // Inbound request message is received
        Transaction receivedAndSent = new ACPTransaction(
                new Message(null, inboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null);
        // Outbound reply message is registered
        receivedAndSent.setOutboundToSend(new Message(outboundMessageType, null, ""));
        // Outbound reply message is sent out
        receivedAndSent.markOutboundAsSent();
        // This is not a valid case
        assertFalse(receivedAndSent.canSendRequest());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void canSendRequestShouldReturnTrueOnlyWhenOutboundIsRegisteredButNotSent(String test, short outboundMessageType,
            short inboundMessageType) {
        // Outbound request message is registered
        Transaction newRequestButNotSent1 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Now this transaction can be sent out
        assertTrue(newRequestButNotSent1.canSendRequest());

        // Outbound request message is registered
        Transaction newRequestButNotSent2 = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        newRequestButNotSent2.setOutboundSequenceNumber((short) 0);
        // Still not sent out yet
        assertTrue(newRequestButNotSent2.canSendRequest());

        // Outbound request message is registered
        Transaction sentButNotAckedYet = new ACPTransaction(null,
                new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""));
        // Before sending out, the sequence number is allocated
        sentButNotAckedYet.setOutboundSequenceNumber((short) 0);
        // Sent out
        sentButNotAckedYet.markOutboundAsSent();
        // Already sent out, so can't send this out again
        assertFalse(sentButNotAckedYet.canSendRequest());

        // Outbound request message is registered
        Transaction sentAndAcked = new ACPTransaction(null,
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
                            new ACPTransaction(new Message(SEQUENCE_NUMBER, inboundMessageType,
                                    ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null),
                            outboundMessageType, SEQUENCE_NUMBER, true),
                    Arguments.of(test + " - Only message type matches",
                            new ACPTransaction(new Message(SEQUENCE_NUMBER, inboundMessageType,
                                    ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null),
                            outboundMessageType, Short.MAX_VALUE, false),
                    Arguments.of(test + " - Only sequence number matches",
                            new ACPTransaction(new Message(SEQUENCE_NUMBER,
                                    firstNonMatchingInboundMessageType(inboundMessageType),
                                    ARBITRARY_MESSAGE_FOR_TESTING_ONLY, ""), null),
                            outboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test + " - Message type and sequence number doesn't match",
                            new ACPTransaction(new Message(SEQUENCE_NUMBER,
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

        List<Arguments> outboundMessageTypes = outboundMessageTypesThatACPShouldSendAck().collect(Collectors.toList());
        for (Arguments arguments : outboundMessageTypes) {
            String test = (String) arguments.get()[0];
            short outboundMessageType = (short) arguments.get()[1];
            short inboundMessageType = (short) arguments.get()[2];
            mergedArguments = Stream.concat(mergedArguments, Stream.of(
                    Arguments.of(
                            test + " - Sequence number not allocated: Both message type and sequence number matches",
                            new ACPTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            null, false, inboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test
                            + " - Sequence number allocated but not sent: Both message type and sequence number matches",
                            new ACPTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, false, inboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test
                            + " - Sequence number allocated and sent: Both message type and sequence number matches",
                            new ACPTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, true, inboundMessageType, SEQUENCE_NUMBER, true),
                    Arguments.of(test + " - Sequence number allocated and sent: Only message type matches",
                            new ACPTransaction(null,
                                    new Message(outboundMessageType, ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, true, inboundMessageType, Short.MAX_VALUE, false),
                    Arguments.of(test + " - Sequence number allocated and sent: Only sequence number matches",
                            new ACPTransaction(null,
                                    new Message(firstNonMatchingOutboundMessageType(outboundMessageType),
                                            ARBITRARY_MESSAGE_FOR_TESTING_ONLY, "")),
                            SEQUENCE_NUMBER, true, inboundMessageType, SEQUENCE_NUMBER, false),
                    Arguments.of(test
                            + " - Sequence number allocated and sent: Message type and sequence number doesn't match",
                            new ACPTransaction(null,
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
}
