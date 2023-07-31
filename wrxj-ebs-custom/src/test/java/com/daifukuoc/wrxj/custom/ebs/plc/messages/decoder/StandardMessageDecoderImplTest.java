package com.daifukuoc.wrxj.custom.ebs.plc.messages.decoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageHeader;

class StandardMessageDecoderImplTest extends StandardMessageDecoderImpl {
    private StandardMessageDecoderImpl standardMessageDecoderImpl;

    @BeforeEach
    public void setUp() {
        standardMessageDecoderImpl = new StandardMessageDecoderImpl();
    }

    @AfterEach
    public void tearDown() {
        standardMessageDecoderImpl = null;
    }

    private static Stream<Arguments> decodeItemPickedUpMessageShouldDecodeMessageAsExpected_forItemPickedUp() {
        return Stream.of(Arguments.of(header(50, 1, 53, 0x1234, 12, 34, 56789, 1), null, ""),
                Arguments.of(header(50, 1, 53, 0x1234, 12, 34, 56789, 1), new byte[15], ""),
                Arguments.of(header(50, 1, 53, 0x1234, 12, 34, 56789, 1),
                        bodyFor53(1, 2, 3, "123412341234", 123, 456, (short) 1), "53,1,1,2,3,123412341234,123,456,1"),
                Arguments.of(header(50, 1, 53, 0x1234, 12, 34, 56789, 1),
                        bodyFor53(0xffffffff, 0xffffffff, 0xffffffff, "123412341234", 0xffffffff, 0xffffffff,
                                (short) 0xffff),
                        "53,1,4294967295,4294967295,4294967295,123412341234,4294967295,4294967295,65535"));

    }

    private static PLCMessageHeader header(int msgLength, int seqNo, int msgType, int equipmentId, int hours,
            int minutes, int milliseconds, int msgVersion) {
        PLCMessageHeader header = new PLCMessageHeader();
        header.setEquipmentId(Integer.toUnsignedString(equipmentId));
        header.setMsgType(msgType);
        header.setSeqNo(seqNo);
        header.setHours(hours);
        header.setMinutes(minutes);
        header.setMilliSeconds(milliseconds);
        header.setMsgLength(msgLength);
        header.setMsgVersion(msgVersion);
        return header;
    }

    private static byte[] bodyFor53(int orderID, int trayId, int globalID, String itemID, int locationIDFrom,
            int locationIDTo, short statusFlags) {
        ByteBuffer buffer = ByteBuffer.allocate(50);
        buffer.put(new byte[16]);
        buffer.putInt(orderID);
        buffer.putInt(trayId);
        buffer.putInt(globalID);
        buffer.put(itemID.getBytes());
        buffer.putInt(locationIDFrom);
        buffer.putInt(locationIDTo);
        buffer.putShort(statusFlags);
        return buffer.array();
    }

    @Test
    void decodeShouldReturnAnEmptyStringForNullMessageHeader() {
        assertEquals("",
                standardMessageDecoderImpl.decode(null, bodyFor53(1, 2, 3, "123412341234", 123, 456, (short) 1)));
    }

    @Test
    void decodeShouldReturnForUnsupportedMessageType() {
        PLCMessageHeader plcMessageHeader = new PLCMessageHeader();
        plcMessageHeader.setMsgType(32768); // any value not supported
        assertEquals("", standardMessageDecoderImpl.decode(plcMessageHeader,
                bodyFor53(1, 2, 3, "123412341234", 123, 456, (short) 1)));
    }

    @ParameterizedTest(name = "{index} expecting [{2}] as decoded string")
    @MethodSource("decodeShouldDecodeMessageAsExpected_forNormalCases")
    void decodeShouldDecodeMessageAsExpected(PLCMessageHeader mpMessageHeader, byte[] bMsg, String expectation) {
        assertEquals(expectation, standardMessageDecoderImpl.decode(mpMessageHeader, bMsg));
    }

    @ParameterizedTest(name = "{index} expecting [{2}] as decoded string")
    @MethodSource("decodeItemPickedUpMessageShouldDecodeMessageAsExpected_forItemPickedUp")
    void decodeItemPickedUpMessageShouldDecodeMessageAsExpected(PLCMessageHeader mpMessageHeader, byte[] bMsg,
            String expectation) {
        assertEquals(expectation, standardMessageDecoderImpl.decodeItemPickedUpMessage(mpMessageHeader, bMsg));
    }

    @ParameterizedTest(name = "{index} expecting [{2}] as decoded string")
    @MethodSource("decodeItemPickedUpMessageShouldDecodeMessageAsExpected_forLocationStatus")
    void decodeLocationStatusMessageShouldDecodeMessageAsExpected(PLCMessageHeader mpMessageHeader, byte[] bMsg,
            String expectation) {
        assertEquals(expectation, standardMessageDecoderImpl.decodeLocationStatusMessage(mpMessageHeader, bMsg));
    }

    private static Stream<Arguments> decodeShouldDecodeMessageAsExpected_forNormalCases() {
        return Stream.of(
                Arguments.of(header(50, 1, 53, 0x1234, 12, 34, 56789, 1),
                        bodyFor53(1, 2, 3, "123412341234", 123, 456, (short) 1), "53,1,1,2,3,123412341234,123,456,1"),
                Arguments.of(header(34, 0, 60, 0, 12, 34, 56789, 1),
                        bodyFor60(new int[] { 1, 2, 3 }, new short[] { 4, 5, 6 }),
                        "60,0,0000000001,4,0000000002,5,0000000003,6"));

    }

    private static Stream<Arguments> decodeItemPickedUpMessageShouldDecodeMessageAsExpected_forLocationStatus() {
        return Stream.of(Arguments.of(header(50, 1, 53, 0x1234, 12, 34, 56789, 1), null, ""),
                Arguments.of(header(50, 1, 53, 0x1234, 12, 34, 56789, 1), new byte[15], ""),
                Arguments.of(header(34, 0, 60, 0, 12, 34, 56789, 1),
                        bodyFor60(new int[] { 1, 2, 3 }, new short[] { 4, 5, 6 }),
                        "60,0,0000000001,4,0000000002,5,0000000003,6"),
                Arguments.of(header(34, 0, 60, 0, 12, 34, 56789, 1),
                        bodyFor60(new int[] { 0xffffffff, 0xffffffff, 0xffffffff },
                                new short[] { (byte) 0xffff, (byte) 0xffff, (byte) 0xffff }),
                        "60,0,4294967295,65535,4294967295,65535,4294967295,65535"));

    }

    private static byte[] bodyFor60(int[] locationIDs, short[] statuses) {
        ByteBuffer buffer = ByteBuffer.allocate(6 * locationIDs.length + 16);
        buffer.put(new byte[16]);
        for (int i = 0; i < locationIDs.length; i++) {
            buffer.putInt(locationIDs[i]);
            buffer.putShort(statuses[i]);
        }
        return buffer.array();
    }

}
