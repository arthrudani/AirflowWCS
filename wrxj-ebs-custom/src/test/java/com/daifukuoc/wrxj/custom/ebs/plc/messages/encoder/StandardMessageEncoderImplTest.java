package com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

class StandardMessageEncoderImplTest extends StandardMessageEncoderImpl {
    private StandardMessageEncoderImpl standardMessageEncoderImpl;

    @BeforeEach
    public void setUp() {
        standardMessageEncoderImpl = new StandardMessageEncoderImpl();
    }

    @AfterEach
    public void tearDown() {
        standardMessageEncoderImpl = null;
    }

    //
    // Test methods for encodeItemPickedUpAck
    //
    @Test
    void encodeItemPickedUpAckShouldReturnNullWhenCSVHasLessThan3Elements() {
        String sData = "1,2";

        assertNull(standardMessageEncoderImpl.encodeItemPickedUpAck(sData,
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT));
    }

    @Test
    void encodeItemPickedUpAckCallsCreatorMethodWithTheGivenCSV() {
        StandardMessageEncoderImpl spyStandardMessageEncoderImpl = spy(standardMessageEncoderImpl);

        doReturn(new byte[] { 1, 2, 3 }).when(spyStandardMessageEncoderImpl).createStandardAckMessage((short) 3,
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT, (short) 2, PLCConstants.MESSAGE_VER);

        assertArrayEquals(new byte[] { 1, 2, 3 }, spyStandardMessageEncoderImpl.encodeItemPickedUpAck("1,2,3",
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT));

        verify(spyStandardMessageEncoderImpl, times(1)).createStandardAckMessage((short) 3,
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT, (short) 2, PLCConstants.MESSAGE_VER);
    }

    @Test
    void encodeItemPickedUpAckIgnoresExtraElementsOfCSV() {
        StandardMessageEncoderImpl spyStandardMessageEncoderImpl = spy(standardMessageEncoderImpl);

        doReturn(new byte[] { 1, 2, 3 }).when(spyStandardMessageEncoderImpl).createStandardAckMessage((short) 3,
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT, (short) 2, PLCConstants.MESSAGE_VER);

        assertArrayEquals(new byte[] { 1, 2, 3 }, spyStandardMessageEncoderImpl.encodeItemPickedUpAck("1,2,3,4",
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT));

        verify(spyStandardMessageEncoderImpl, times(1)).createStandardAckMessage((short) 3,
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT, (short) 2, PLCConstants.MESSAGE_VER);
    }

    @Test
    void encodeItemPickedUpAckShouldReturnNullToEmptyInput() {
        assertNull(
                standardMessageEncoderImpl.encodeItemPickedUpAck("", PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT));
    }

    @Test
    void encodeItemPickedUpAckDoesNotCheckNullInput() {
        assertThrows(NullPointerException.class, () -> standardMessageEncoderImpl.encodeItemPickedUpAck(null,
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT));
    }

    //
    // Test methods for encode
    //
    @ParameterizedTest
    @NullAndEmptySource
    void encodeShouldReturnNullToNullOrEmptyInput(String input) {
        assertNull(standardMessageEncoderImpl.encode(input));
    }

    @Test
    void encodeCallsEncodeAckMessageWhenGivenMessageTypeIsItemPickedUpAckMessageType() {
        StandardMessageEncoderImpl spyStandardMessageEncoderImpl = spy(standardMessageEncoderImpl);
        doReturn(new byte[] { 1, 2, 3 }).when(spyStandardMessageEncoderImpl).encodeItemPickedUpAck("153,1,2",
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT);

        assertArrayEquals(new byte[] { 1, 2, 3 }, spyStandardMessageEncoderImpl.encode("153,1,2"));

        verify(spyStandardMessageEncoderImpl, times(1)).encodeItemPickedUpAck("153,1,2",
                PLCConstants.PLC_ITEM_PICKEDUP_ACK_MSG_TYPE_INT);
    }

    @Test
    void encodeCallsEncodeFlushMessageWhenGivenMessageTypeIsFlushResponseMessageType() {
        StandardMessageEncoderImpl spyStandardMessageEncoderImpl = spy(standardMessageEncoderImpl);
        doReturn(new byte[] { 1, 2, 3 }).when(spyStandardMessageEncoderImpl).encodeFlushMessage("3,1,2002,20,0,0");

        assertArrayEquals(new byte[] { 1, 2, 3 }, spyStandardMessageEncoderImpl.encode("3,1,2002,20,0,0"));

        verify(spyStandardMessageEncoderImpl, times(1)).encodeFlushMessage("3,1,2002,20,0,0");
    }
    @Test
    void encodeCallsEncodeBagDataUpdateMessageWhenGivenMessageTypeIsBagDataUpdateMessageType() {
        StandardMessageEncoderImpl spyStandardMessageEncoderImpl = spy(standardMessageEncoderImpl);
        doReturn(new byte[] { 1, 2, 3 }).when(spyStandardMessageEncoderImpl).encodeBagDataUpdateMessage("10,0,2222,5552,123456780,SQ123,3600,1200200001,1");

        assertArrayEquals(new byte[] { 1, 2, 3 }, spyStandardMessageEncoderImpl.encode("10,0,2222,5552,123456780,SQ123,3600,1200200001,1"));

        verify(spyStandardMessageEncoderImpl, times(1)).encodeBagDataUpdateMessage("10,0,2222,5552,123456780,SQ123,3600,1200200001,1");
    }
    
    @Test
    void encodeShouldReturnNullToCommaOnlyInput() {
        assertNull(standardMessageEncoderImpl.encode(","));
    }

    @Test
    void encodeLocationStatusAck_GeneratesMessage_WithCorrectParams() {
        StandardMessageEncoderImpl spyEncoderImpl = spy(standardMessageEncoderImpl);
        doReturn(new byte[] { 1, 2, 3 }).when(spyEncoderImpl).createStandardAckMessage((short) 333, (short) 160,
                (short) 222, PLCConstants.MESSAGE_VER);
        byte[] result = spyEncoderImpl.encodeLocationStatusAck("111,222,333", (short) 160);
        assertArrayEquals(new byte[] { 1, 2, 3 }, result);
        verify(spyEncoderImpl, times(1)).createStandardAckMessage((short) 333, (short) 160, (short) 222,
                PLCConstants.MESSAGE_VER);

    }
}
