package com.daifukuoc.wrxj.custom.ebs.host.communication;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPConnectionEvent;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.impl.TCPIPServerComms;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;

@ExtendWith(MockitoExtension.class)
class SACTCPServerTransportTest {

    private static final int NOW_HOUR = 10;
    private static final int NOW_MINUTE = 20;
    private static final int NOW_SECOND = 1;
    private static final LocalTime NOW = LocalTime.of(NOW_HOUR, NOW_MINUTE, NOW_SECOND);
    private static final int MESSAGE_SEQUENCE_NUMBER = 32767;
    private static final String HOST_CONTROLLER_NAME = "HOST_CONTROLLER_NAME";
    private static final String HOST_COMM_GROUP = "HOST_COMM_GROUP";
    private static final String HOST_IP = "localhost";
    private static final String HOST_NAME = "localhost";
    private static final int LISTEN_PORT = 12345;
    private static final boolean USE_ACKS = false;
    private static final boolean USE_HEART_BEATS = false;
    private static final String EXPECTED_RECEIPT_RESPONSE_MESSAGE = MESSAGE_SEQUENCE_NUMBER + ";ExpectedReceiptCompleteMessage;2;200200;2000;20002000;BAG2000;1001;1";
    private static final String RETRIEVAL_ORDER_RESPONSE_MESSAGE = MESSAGE_SEQUENCE_NUMBER + ";OrderCompleteMessage;5;111;1;0";
    private static final String FLIGHT_DATA_UPDATE_RESPONSE_MESSAGE = MESSAGE_SEQUENCE_NUMBER + ";FlightDataUpdateMessage;157;1";

    MockedStatic<Application> mockedApplication;
    MockedStatic<Factory> mockedFactory;
    MockedStatic<LocalTime> mockedLocalTime;

    @Mock
    Controller systemGateway;

    @Mock
    StandardHostServer standardHostServer;

    @Mock
    HostServerDelegate hostServerDelegate;

    @Mock
    WrxToHostData wrxToHostData;

    @Mock
    TCPIPServerComms tCPIPServerComms;

    @Mock
    SocketChannel socketChannel;

    @Mock
    Logger logger;

    @Mock
    SACTCPIPReadEventImpl sACTCPIPReadEventImpl;

    @Mock
    SACTCPIPSocketCloseEventImpl sACTCPIPSocketCloseEventImpl;

    @Mock
    Properties properties;

    SACTCPIPReaderWriterForTest sACTCPIPReaderWriterForTest;

    AtomicBoolean isSendMessageExecuted;

    @BeforeEach
    void setUp() throws Exception {
        // The basic properties to initialize SACTCPIPReaderWriterForTest
        when(properties.getProperty("ListenPort")).thenReturn("1234");
        when(properties.getProperty("ServerIP", "")).thenReturn("localhost");
        when(properties.getProperty("ClientRetryInterval", "6")).thenReturn("6");
        when(properties.getProperty(not(eq("ListenPort")))).thenReturn("");
        sACTCPIPReaderWriterForTest = new SACTCPIPReaderWriterForTest(properties);

        isSendMessageExecuted = new AtomicBoolean(false);

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(
                () -> Factory.create(eq(StandardHostServer.class), eq(SACTCPServerTransport.TCP_SERVER_TRANSPORT_NAME)))
                .thenReturn(standardHostServer);
        mockedFactory.when(() -> Factory.create(eq(TCPIPServerComms.class), any(), any())).thenReturn(tCPIPServerComms);
        mockedFactory.when(() -> Factory.create(eq(SACTCPIPReadEventImpl.class), eq(HOST_NAME), eq(HOST_COMM_GROUP),
                any(TCPIPLogger.class))).thenReturn(sACTCPIPReadEventImpl);
        mockedFactory.when(() -> Factory.create(eq(SACTCPIPSocketCloseEventImpl.class), eq(HOST_COMM_GROUP),
                eq(systemGateway), any(TCPIPLogger.class))).thenReturn(sACTCPIPSocketCloseEventImpl);
        mockedFactory.when(() -> Factory.create(eq(SACTCPIPReaderWriter.class), any(Properties.class),
                any(SocketChannel.class), any(TCPIPLogger.class))).thenReturn(sACTCPIPReaderWriterForTest);

        mockedApplication = Mockito.mockStatic(Application.class);
        mockedApplication
                .when(() -> Application.getString(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".HostName"))))
                .thenReturn(HOST_NAME);
        mockedApplication
                .when(() -> Application.getString(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".HostIP"))))
                .thenReturn(HOST_IP);
        mockedApplication
                .when(() -> Application.getInt(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".ListenPort"))))
                .thenReturn(LISTEN_PORT);
        mockedApplication
                .when(() -> Application.getBoolean(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".UseAcks"))))
                .thenReturn(USE_ACKS);
        mockedApplication.when(
                () -> Application.getBoolean(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".UseHeartBeats"))))
                .thenReturn(USE_HEART_BEATS);
        // SERVER_IP, LISTEN_PORT, SOCKET_TYPE are put with what we provided
        mockedApplication
                .when(() -> Application.getString(
                        and(startsWith(Application.HOSTCFG_DOMAIN), endsWith("." + TCPIPConstants.HEART_BEAT_MSG))))
                .thenReturn(Strings.EMPTY);
        mockedApplication
                .when(() -> Application.getString(
                        and(startsWith(Application.HOSTCFG_DOMAIN), endsWith("." + TCPIPConstants.MESSAGE_PREFIX))))
                .thenReturn(Strings.EMPTY);
        mockedApplication
                .when(() -> Application.getString(
                        and(startsWith(Application.HOSTCFG_DOMAIN), endsWith("." + TCPIPConstants.MESSAGE_SUFFIX))))
                .thenReturn(Strings.EMPTY);

        mockedLocalTime = Mockito.mockStatic(LocalTime.class);
        mockedLocalTime.when(() -> LocalTime.now()).thenReturn(NOW);
    }

    @AfterEach
    void tearDown() throws Exception {
        sACTCPIPReaderWriterForTest.interrupt();

        mockedFactory.close();
        mockedApplication.close();
        mockedLocalTime.close();
    }

    @Test
    void shouldExpectedReceiptResponseMessageWhenItIsSavedInWrxToHostTable()
            throws HostCommException, DBException, TCPIPCommException {
        when(standardHostServer.getOldestDataQueueMessage(hostServerDelegate)).thenReturn(wrxToHostData);
        when(wrxToHostData.getMessage()).thenReturn(EXPECTED_RECEIPT_RESPONSE_MESSAGE);
        when(wrxToHostData.getMessageIdentifierEnum()).thenReturn(MessageOutNames.EXPECTED_RECEIPT_COMPLETE);

        ArgumentCaptor<TCPIPConnectionEvent> connectionEventHandlerCaptor = ArgumentCaptor
                .forClass(TCPIPConnectionEvent.class);
        doNothing().when(tCPIPServerComms).registerConnectionEvents(connectionEventHandlerCaptor.capture());

        // Prepare SACTCPServerTransport
        SACTCPServerTransport sACTCPServerTransport = new SACTCPServerTransport(systemGateway, HOST_CONTROLLER_NAME,
                HOST_COMM_GROUP);
        sACTCPServerTransport.setLogger(logger);
        sACTCPServerTransport.run();

        // Simulate connection event
        TCPIPConnectionEvent tCPIPConnectionEvent = connectionEventHandlerCaptor.getValue();
        assertNotNull(tCPIPConnectionEvent);
        tCPIPConnectionEvent.connectionHandler(socketChannel);

        // The message should be sent
        int numberOfSentMessages = sACTCPServerTransport.sendMessages(hostServerDelegate, standardHostServer);
        assertEquals(1, numberOfSentMessages);

        // The binary format message should be built by MessageUtil.buildExpectedReceiptResponseMessage()
        ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(wrxToHostData, times(1)).setMessageBytes(byteArrayCaptor.capture());
        byte[] sentByteArray = byteArrayCaptor.getValue();
        byte[] buildByteArrayByMessageUtil = MessageUtil.buildExpectedReceiptResponseMessage(wrxToHostData.getMessage());
        assertTrue(Arrays.equals(sentByteArray, buildByteArrayByMessageUtil));

        // SACTCPIPReaderWriter.sendMessage() should be called
        assertTrue(isSendMessageExecuted.get());

        // The message should be marked as processed
        verify(standardHostServer, times(1)).markMessageAsProcessed(hostServerDelegate);
    }
    
    @Test
    void shouldSendRetrievalOrderResponseMessageWhenItIsSavedInWrxToHostTable()
            throws HostCommException, DBException, TCPIPCommException {
        when(standardHostServer.getOldestDataQueueMessage(hostServerDelegate)).thenReturn(wrxToHostData);
        when(wrxToHostData.getMessage()).thenReturn(RETRIEVAL_ORDER_RESPONSE_MESSAGE);
        when(wrxToHostData.getMessageIdentifierEnum()).thenReturn(MessageOutNames.ORDER_COMPLETE);

        ArgumentCaptor<TCPIPConnectionEvent> connectionEventHandlerCaptor = ArgumentCaptor
                .forClass(TCPIPConnectionEvent.class);
        doNothing().when(tCPIPServerComms).registerConnectionEvents(connectionEventHandlerCaptor.capture());

        // Prepare SACTCPServerTransport
        SACTCPServerTransport sACTCPServerTransport = new SACTCPServerTransport(systemGateway, HOST_CONTROLLER_NAME,
                HOST_COMM_GROUP);
        sACTCPServerTransport.setLogger(logger);
        sACTCPServerTransport.run();

        // Simulate connection event
        TCPIPConnectionEvent tCPIPConnectionEvent = connectionEventHandlerCaptor.getValue();
        assertNotNull(tCPIPConnectionEvent);
        tCPIPConnectionEvent.connectionHandler(socketChannel);

        // The message should be sent
        int numberOfSentMessages = sACTCPServerTransport.sendMessages(hostServerDelegate, standardHostServer);
        assertEquals(1, numberOfSentMessages);

        // The binary format message should be built by MessageUtil.buildRetrievalOrderResponseMessage()
        ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(wrxToHostData, times(1)).setMessageBytes(byteArrayCaptor.capture());
        byte[] sentByteArray = byteArrayCaptor.getValue();
        byte[] buildByteArrayByMessageUtil = MessageUtil.buildRetrievalOrderResponseMessage(wrxToHostData.getMessage());
        assertTrue(Arrays.equals(sentByteArray, buildByteArrayByMessageUtil));

        // SACTCPIPReaderWriter.sendMessage() should be called
        assertTrue(isSendMessageExecuted.get());

        // The message should be marked as processed
        verify(standardHostServer, times(1)).markMessageAsProcessed(hostServerDelegate);
    }

    @Test
    void shouldSendFlightDataUpdateResponseMessageWhenItIsSavedInWrxToHostTable()
            throws HostCommException, DBException, TCPIPCommException {
        when(standardHostServer.getOldestDataQueueMessage(hostServerDelegate)).thenReturn(wrxToHostData);
        when(wrxToHostData.getMessage()).thenReturn(FLIGHT_DATA_UPDATE_RESPONSE_MESSAGE);
        when(wrxToHostData.getMessageIdentifierEnum()).thenReturn(MessageOutNames.FLIGHT_DATA_UPDATE);

        ArgumentCaptor<TCPIPConnectionEvent> connectionEventHandlerCaptor = ArgumentCaptor
                .forClass(TCPIPConnectionEvent.class);
        doNothing().when(tCPIPServerComms).registerConnectionEvents(connectionEventHandlerCaptor.capture());

        // Prepare SACTCPServerTransport
        SACTCPServerTransport sACTCPServerTransport = new SACTCPServerTransport(systemGateway, HOST_CONTROLLER_NAME,
                HOST_COMM_GROUP);
        sACTCPServerTransport.setLogger(logger);
        sACTCPServerTransport.run();

        // Simulate connection event
        TCPIPConnectionEvent tCPIPConnectionEvent = connectionEventHandlerCaptor.getValue();
        assertNotNull(tCPIPConnectionEvent);
        tCPIPConnectionEvent.connectionHandler(socketChannel);

        // The message should be sent
        int numberOfSentMessages = sACTCPServerTransport.sendMessages(hostServerDelegate, standardHostServer);
        assertEquals(1, numberOfSentMessages);

        // The binary format message should be built by MessageUtil.buildFlightDataUpdateResponseMessage()
        ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(wrxToHostData, times(1)).setMessageBytes(byteArrayCaptor.capture());
        byte[] sentByteArray = byteArrayCaptor.getValue();
        byte[] buildByteArrayByMessageUtil = MessageUtil.buildFlightDataUpdateResponseMessage(wrxToHostData.getMessage());
        assertTrue(Arrays.equals(sentByteArray, buildByteArrayByMessageUtil));

        // SACTCPIPReaderWriter.sendMessage() should be called
        assertTrue(isSendMessageExecuted.get());

        // The message should be marked as processed
        verify(standardHostServer, times(1)).markMessageAsProcessed(hostServerDelegate);
    }

    /**
     * This is to simulator the behaviour of SACTCPIPReaderWriter as it's used by SACTCPServerTransport
     * 
     * @author LK
     *
     */
    private class SACTCPIPReaderWriterForTest extends SACTCPIPReaderWriter {

        private static final int MOCK_THREAD_SLEEP_DURATION = 2000;

        public SACTCPIPReaderWriterForTest(Properties ipConfigProp) throws TCPIPCommException {
            super(ipConfigProp);
        }

        @Override
        public boolean isConnectionAlive() {
            // Return true to simulator when the connection is established
            return true;
        }

        @Override
        public void run() {
            // SACTCPServerTransport checks if SACTCPIPReaderWriter is alive by calling the final Thread.isAlive()
            // The thing is Mockito can't mock final method
            // So here, I just leave this thread running to simulate running status
            synchronized (this) {
                while (true) {
                    try {
                        sleep(MOCK_THREAD_SLEEP_DURATION);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        @Override
        public ByteBuffer sendMessage(byte[] mabMessage) throws TCPIPCommException {
            // Mark that this method is called and then return null to simulate sendMessage()
            isSendMessageExecuted.set(true);
            return null;
        }
    }
}
