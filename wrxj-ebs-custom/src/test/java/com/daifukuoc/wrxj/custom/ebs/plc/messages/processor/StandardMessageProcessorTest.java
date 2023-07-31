package com.daifukuoc.wrxj.custom.ebs.plc.messages.processor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageData;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardInboundMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.processor.StandardMessageProcessor.MSG_PROCESS_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

@ExtendWith(MockitoExtension.class)
public class StandardMessageProcessorTest {

    MockedStatic<Factory> mockedFactory;

    @Mock
    StandardStationServer standardStationServer;

    @Mock
    Load load;

    @Mock
    PLCMessageData plcMessageData;

    StandardMessageProcessor standardMessageProcessor;

    @BeforeEach
    void setUp() {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(StandardStationServer.class)).thenReturn(standardStationServer);
        mockedFactory.when(() -> Factory.create(Load.class)).thenReturn(load);
        standardMessageProcessor = new StandardMessageProcessor();
    }

    @AfterEach
    void tearDown() {
        standardMessageProcessor = null;
        mockedFactory.close();
    }

    @Test
    void ShouldFailIfCannotCreatePLCKeepAliveResponseMessage() {
        byte[] keepAliveBytes = standardMessageProcessor.createPLCKeepAliveResponseMessage(1111);

        assertNotNull(keepAliveBytes);
        assertEquals(keepAliveBytes.length, PLCConstants.MSG_HEADER_LEN + PLCConstants.KEEPALIVE_MSG_BODY_LEN);

        ByteBuffer buf = ByteBuffer.wrap(keepAliveBytes);
        // Message length
        assertEquals(PLCConstants.MSG_HEADER_LEN + PLCConstants.KEEPALIVE_MSG_BODY_LEN, buf.getShort());
        // Sequence number 0
        assertEquals(0, buf.getShort());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getMessageHandler_ResultCheck_ReturnsNullForNullOrEmptyMessageType(String messageType) {
        PLCMessageData plcMessageData = new PLCMessageData();
        plcMessageData.toDataValues(messageType);
        standardMessageProcessor.mpMessageData = plcMessageData;
        assertNull(standardMessageProcessor.getMessageHandler());
    }

    @Test
    void getMessageHandler_ResultCheck_ReturnsNullForUnsupportedMessageType() {
        PLCMessageData plcMessageData = new PLCMessageData();
        plcMessageData.toDataValues("NON");
        standardMessageProcessor.mpMessageData = plcMessageData;
        assertNull(standardMessageProcessor.getMessageHandler());
    }

    @Test
    void process_normalCase() throws DBException {
        StandardMessageProcessor spiedStandardMessageProcessor = spy(standardMessageProcessor);

        StandardInboundMessage standardInboundMessage = mock(StandardInboundMessage.class);
        EBSTransactionContext ebsTransactionContext = mock(EBSTransactionContext.class);

        doReturn(standardInboundMessage).when(spiedStandardMessageProcessor).getMessageHandler();
        doReturn("RECEIVED").when(spiedStandardMessageProcessor).getReceivedText();
        doReturn("DEVICE").when(spiedStandardMessageProcessor).getDeviceId();

        doReturn(true).when(standardInboundMessage).parse("RECEIVED");
        doNothing().when(standardInboundMessage).setDeviceId("DEVICE");
        doNothing().when(standardInboundMessage).processAck();
        doReturn(true).when(standardInboundMessage).isValid();
        doReturn(ebsTransactionContext).when(standardInboundMessage).process();
        doReturn(true).when(ebsTransactionContext).isSuccess();
        doNothing().when(standardInboundMessage).outputTransactionLog(ebsTransactionContext);

        assertEquals(MSG_PROCESS_STATUS.SUCCESS, spiedStandardMessageProcessor.process());

        verify(spiedStandardMessageProcessor, times(1)).getMessageHandler();
        verify(spiedStandardMessageProcessor, times(1)).getReceivedText();
        verify(spiedStandardMessageProcessor, times(1)).getDeviceId();

        verify(standardInboundMessage, times(1)).parse("RECEIVED");
        verify(standardInboundMessage, times(1)).setDeviceId("DEVICE");
        verify(standardInboundMessage, times(1)).processAck();
        verify(standardInboundMessage, times(1)).isValid();
        verify(standardInboundMessage, times(1)).process();
        verify(ebsTransactionContext, times(1)).isSuccess();
        verify(standardInboundMessage, times(1)).outputTransactionLog(ebsTransactionContext);
    }

    @Test
    void process_abnormalCase_noInboundMEssageIsFound() throws DBException {
        StandardMessageProcessor spiedStandardMessageProcessor = spy(standardMessageProcessor);

        doReturn(null).when(spiedStandardMessageProcessor).getMessageHandler();
        doReturn("RECEIVED").when(spiedStandardMessageProcessor).getReceivedText();
        doReturn("DEVICE").when(plcMessageData).getDeviceId();

        spiedStandardMessageProcessor.setMessageData(plcMessageData);
        assertEquals(MSG_PROCESS_STATUS.FAILURE, spiedStandardMessageProcessor.process());

        verify(spiedStandardMessageProcessor, times(1)).getMessageHandler();
        verify(spiedStandardMessageProcessor, times(1)).getReceivedText();
        verify(plcMessageData, times(1)).getDeviceId();

    }

    @Test
    void process_abnormalCase_messageIsInvalid() throws DBException {
        StandardMessageProcessor spiedStandardMessageProcessor = spy(standardMessageProcessor);

        StandardInboundMessage standardInboundMessage = mock(StandardInboundMessage.class);

        doReturn(standardInboundMessage).when(spiedStandardMessageProcessor).getMessageHandler();
        doReturn("RECEIVED").when(spiedStandardMessageProcessor).getReceivedText();
        doReturn("DEVICE").when(spiedStandardMessageProcessor).getDeviceId();

        doReturn(true).when(standardInboundMessage).parse("RECEIVED");
        doNothing().when(standardInboundMessage).setDeviceId("DEVICE");
        doNothing().when(standardInboundMessage).processAck();
        doReturn(false).when(standardInboundMessage).isValid();

        assertEquals(MSG_PROCESS_STATUS.FAILURE, spiedStandardMessageProcessor.process());

        verify(spiedStandardMessageProcessor, times(1)).getMessageHandler();
        verify(spiedStandardMessageProcessor, times(1)).getReceivedText();
        verify(spiedStandardMessageProcessor, times(1)).getDeviceId();

        verify(standardInboundMessage, times(1)).parse("RECEIVED");
        verify(standardInboundMessage, times(1)).setDeviceId("DEVICE");
        verify(standardInboundMessage, times(1)).processAck();
        verify(standardInboundMessage, times(1)).isValid();
    }

    @Test
    void process_abnormalCase_nullIsReturnedFromProcess() throws DBException {
        StandardMessageProcessor spiedStandardMessageProcessor = spy(standardMessageProcessor);

        StandardInboundMessage standardInboundMessage = mock(StandardInboundMessage.class);

        doReturn(standardInboundMessage).when(spiedStandardMessageProcessor).getMessageHandler();
        doReturn("RECEIVED").when(spiedStandardMessageProcessor).getReceivedText();
        doReturn("DEVICE").when(spiedStandardMessageProcessor).getDeviceId();

        doReturn(true).when(standardInboundMessage).parse("RECEIVED");
        doNothing().when(standardInboundMessage).setDeviceId("DEVICE");
        doNothing().when(standardInboundMessage).processAck();
        doReturn(true).when(standardInboundMessage).isValid();
        doReturn(null).when(standardInboundMessage).process();

        assertEquals(MSG_PROCESS_STATUS.FAILURE, spiedStandardMessageProcessor.process());

        verify(spiedStandardMessageProcessor, times(1)).getMessageHandler();
        verify(spiedStandardMessageProcessor, times(1)).getReceivedText();
        verify(spiedStandardMessageProcessor, times(1)).getDeviceId();

        verify(standardInboundMessage, times(1)).parse("RECEIVED");
        verify(standardInboundMessage, times(1)).setDeviceId("DEVICE");
        verify(standardInboundMessage, times(1)).processAck();
        verify(standardInboundMessage, times(1)).isValid();
        verify(standardInboundMessage, times(1)).process();

    }

    @Test
    void process_abnormalCase_validationThrowsAnException() throws DBException {
        StandardMessageProcessor spiedStandardMessageProcessor = spy(standardMessageProcessor);

        StandardInboundMessage standardInboundMessage = mock(StandardInboundMessage.class);

        doReturn(standardInboundMessage).when(spiedStandardMessageProcessor).getMessageHandler();
        doReturn("RECEIVED").when(spiedStandardMessageProcessor).getReceivedText();
        doReturn("DEVICE").when(spiedStandardMessageProcessor).getDeviceId();
        doReturn("DEVICE").when(plcMessageData).getDeviceId();

        doReturn(true).when(standardInboundMessage).parse("RECEIVED");
        doNothing().when(standardInboundMessage).setDeviceId("DEVICE");
        doNothing().when(standardInboundMessage).processAck();
        doThrow(new DBException()).when(standardInboundMessage).isValid();

        spiedStandardMessageProcessor.setMessageData(plcMessageData);
        assertEquals(MSG_PROCESS_STATUS.FAILURE, spiedStandardMessageProcessor.process());

        verify(spiedStandardMessageProcessor, times(1)).getMessageHandler();
        verify(spiedStandardMessageProcessor, times(4)).getReceivedText();
        verify(spiedStandardMessageProcessor, times(1)).getDeviceId();

        verify(standardInboundMessage, times(1)).parse("RECEIVED");
        verify(standardInboundMessage, times(1)).setDeviceId("DEVICE");
        verify(standardInboundMessage, times(1)).processAck();
        verify(standardInboundMessage, times(1)).isValid();

    }
}
