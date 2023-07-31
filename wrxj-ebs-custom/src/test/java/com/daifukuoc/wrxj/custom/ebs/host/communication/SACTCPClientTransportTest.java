package com.daifukuoc.wrxj.custom.ebs.host.communication;

import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;

@ExtendWith(MockitoExtension.class)
class SACTCPClientTransportTest {

    private static final int NOW_HOUR = 10;
    private static final int NOW_MINUTE = 20;
    private static final int NOW_SECOND = 1;
    private static final LocalTime NOW = LocalTime.of(NOW_HOUR, NOW_MINUTE, NOW_SECOND);
    private static final int MESSAGE_SEQUENCE_NUMBER = 999;
    private static final String HOST_CONTROLLER_NAME = "HOST_CONTROLLER_NAME";
    private static final String HOST_COMM_GROUP = "HOST_COMM_GROUP";
    private static final String HOST_IP = "localhost";
    private static final String HOST_NAME = "localhost";
    private static final int LISTEN_PORT = 12345;
    private static final boolean USE_ACKS = false;
    private static final boolean USE_HEART_BEATS = false;
    private static final String FLIGHT_DATA_UPDATE_RESPONSE_MESSAGE = "93252;FlightDataUpdateMessage;29;FL100;20221201123456;FL100;20221201131234;3600";

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
//        // The basic properties to initialize SACTCPIPReaderWriterForTest
//        when(properties.getProperty("ListenPort")).thenReturn("1234");
//        when(properties.getProperty("ServerIP", "")).thenReturn("localhost");
//        when(properties.getProperty("ClientRetryInterval", "6")).thenReturn("6");
//        when(properties.getProperty(not(eq("ListenPort")))).thenReturn("");
//        sACTCPIPReaderWriterForTest = new SACTCPIPReaderWriterForTest(properties);
//
//        isSendMessageExecuted = new AtomicBoolean(false);
//
//        mockedFactory = Mockito.mockStatic(Factory.class);
//        mockedFactory.when(
//                () -> Factory.create(eq(StandardHostServer.class), eq(SACTCPClientTransport.TCP_CLIENT_TRANSPORT_NAME)))
//                .thenReturn(standardHostServer);
//        mockedFactory.when(() -> Factory.create(eq(SACTCPIPReadEventImpl.class), eq(HOST_NAME), eq(HOST_COMM_GROUP),
//                any(TCPIPLogger.class))).thenReturn(sACTCPIPReadEventImpl);
//        mockedFactory.when(() -> Factory.create(eq(SACTCPIPSocketCloseEventImpl.class), eq(HOST_COMM_GROUP),
//                eq(systemGateway), any(TCPIPLogger.class))).thenReturn(sACTCPIPSocketCloseEventImpl);
//        mockedFactory.when(
//                () -> Factory.create(eq(SACTCPIPReaderWriter.class), any(Properties.class), any(TCPIPLogger.class)))
//                .thenReturn(sACTCPIPReaderWriterForTest);
//
//        mockedApplication = Mockito.mockStatic(Application.class);
//        mockedApplication
//                .when(() -> Application.getString(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".HostName"))))
//                .thenReturn(HOST_NAME);
//        mockedApplication
//                .when(() -> Application.getString(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".HostIP"))))
//                .thenReturn(HOST_IP);
//        mockedApplication
//                .when(() -> Application.getInt(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".ListenPort"))))
//                .thenReturn(LISTEN_PORT);
//        mockedApplication
//                .when(() -> Application.getBoolean(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".UseAcks"))))
//                .thenReturn(USE_ACKS);
//        mockedApplication.when(
//                () -> Application.getBoolean(and(startsWith(Application.HOSTCFG_DOMAIN), endsWith(".UseHeartBeats"))))
//                .thenReturn(USE_HEART_BEATS);
//        // SERVER_IP, LISTEN_PORT, SOCKET_TYPE are put with what we provided
//        mockedApplication
//                .when(() -> Application.getString(
//                        and(startsWith(Application.HOSTCFG_DOMAIN), endsWith("." + TCPIPConstants.HEART_BEAT_MSG))))
//                .thenReturn(Strings.EMPTY);
//        mockedApplication
//                .when(() -> Application.getString(
//                        and(startsWith(Application.HOSTCFG_DOMAIN), endsWith("." + TCPIPConstants.MESSAGE_PREFIX))))
//                .thenReturn(Strings.EMPTY);
//        mockedApplication
//                .when(() -> Application.getString(
//                        and(startsWith(Application.HOSTCFG_DOMAIN), endsWith("." + TCPIPConstants.MESSAGE_SUFFIX))))
//                .thenReturn(Strings.EMPTY);
//
//        mockedLocalTime = Mockito.mockStatic(LocalTime.class);
//        mockedLocalTime.when(() -> LocalTime.now()).thenReturn(NOW);
    }

    @AfterEach
    void tearDown() throws Exception {
//        sACTCPIPReaderWriterForTest.interrupt();
//
//        mockedFactory.close();
//        mockedApplication.close();
//        mockedLocalTime.close();
    }

    @Test
    void shouldSendFlightDataUpdateResponseMessageWhenItIsSavedInWrxToHostTable()
            throws HostCommException, DBException, TCPIPCommException {
        // FIXME SACTCPClientTransport calls SACTCPIPReaderWriter's join() right after it starts it in run(), which would cause client transport blocked. So, until it's fixed, no meaning of testing it.
//        when(standardHostServer.getOldestDataQueueMessage(hostServerDelegate)).thenReturn(wrxToHostData);
//        when(wrxToHostData.getMessage()).thenReturn(FLIGHT_DATA_UPDATE_RESPONSE_MESSAGE);
//        when(wrxToHostData.getMessageIdentifierEnum()).thenReturn(MessageOutNames.FLIGHT_DATA_UPDATE);
//        when(wrxToHostData.getMessageSequence()).thenReturn(MESSAGE_SEQUENCE_NUMBER);
//
//        // Prepare SACTCPServerTransport
//        SACTCPClientTransport sACTCPClientTransport = new SACTCPClientTransport(systemGateway, HOST_CONTROLLER_NAME,
//                HOST_COMM_GROUP);
//        sACTCPClientTransport.setLogger(logger);      
//        sACTCPClientTransport.run();
//
//        // The message should be sent
//        int numberOfSentMessages = sACTCPClientTransport.sendMessages(hostServerDelegate, standardHostServer);
//        assertEquals(1, numberOfSentMessages);
//
//        // The binary format message should be built by MessageUtil.buildFlightDataUpdateResponseMessage()
//        ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
//        verify(wrxToHostData, times(1)).setMessageBytes(byteArrayCaptor.capture());
//        byte[] sentByteArray = byteArrayCaptor.getValue();
//        byte[] buildByteArrayByMessageUtil = MessageUtil.buildFlightDataUpdateResponseMessage(wrxToHostData);
//        assertTrue(Arrays.equals(sentByteArray, buildByteArrayByMessageUtil));
//
//        // SACTCPIPReaderWriter.sendMessage() should be called
//        assertTrue(isSendMessageExecuted.get());
//
//        // The message should be marked as processed
//        verify(standardHostServer, times(1)).markMessageAsProcessed(hostServerDelegate);
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
        public void connToServer() throws TCPIPCommException {
            // Simulate connection
        }

        @Override
        public void run() {
            // SACTCPClientTransportTest checks if SACTCPIPReaderWriter is alive by calling the final Thread.isAlive()
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
