package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.communication.ConnConfigKeys;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageManager;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPServerSocketHandler;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

@ExtendWith(MockitoExtension.class)
class SACClientSocketHandlerTest {

    private static final int REMOTE_PORT = 5678;
    private static final int LOCAL_PORT = 1234;
    private static final String IP_ADDRESS = "localhost";
    private static final String PORT_NUMBER = "8421";
    private static final int KEEP_ALIVE = 10;
    private static final int RETRY_INTERVAL = 1;
    private static final int ACK_MAX_RETRY = 3;
    private static final int ACK_TIMEOUT = 10;

    private static final String VALID_KEEP_ALIVE_BINARY_MSG = "02 00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01 03";
    private static final String INVALID_MSG_WITH_INVALID_MSG_TYPE = "02 00 12 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 01 03";
    private static final String INVALID_MSG_WITHOUT_STX = "00 12 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 01 03";
    private static final String INVALID_MSG_WITHOUT_ETX = "02 00 12 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 01";

    MockedStatic<Factory> mockedFactory;

    @Mock
    Properties connectionProperties;

    @Mock
    Logger logger;

    @Mock
    MessageService messageService;

    @Mock
    SACTransactionHandler transactionHandler;

    @Mock
    Socket connectedSocket1;

    @Mock
    InetAddress localInetAddress;

    @Mock
    InetAddress remoteInetAddress;

    @Mock
    InputStream socketInputStream;

    @Mock
    OutputStream socketOutputStream;

    SACMessageManager messageProcessor;

    @Mock
    ExecutorService executor;

    SACClientSocketHandler clientSocketHandler;

    @BeforeEach
    void setUp() throws Exception {
        messageProcessor = new SACMessageManager();

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(SACMessageManager.class)).thenReturn(messageProcessor);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }

    // Prepare the list of invalid configurations
    private static Stream<Arguments> invalidConfigurations() {
        return Stream.of(Arguments.of("No IP address", "", "8421", "10", "10", "3"),
                Arguments.of("No port", "localhost", "", "10", "10", "3"),
                Arguments.of("Too low port number", "localhost", "1", "10", "10", "3"),
                Arguments.of("Negative port number", "localhost", "-1", "10", "10", "3"),
                Arguments.of("No keep alive timeout", "localhost", "8421", "", "10", "3"),
                Arguments.of("Negative keep alive timeout", "localhost", "8421", "-1", "10", "3"),
                Arguments.of("No ack timeout", "localhost", "8421", "10", "", "3"),
                Arguments.of("Negative ack timeout", "localhost", "8421", "10", "-1", "3"),
                Arguments.of("No ack max retry", "localhost", "8421", "10", "10", ""),
                Arguments.of("Negative ack max retry", "localhost", "8421", "10", "10", "-1"));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidConfigurations")
    void shouldPublishAnErrorEventWhenAnyConfigurationIsInvalid(String test, String ipAddress,
            String portNumber, String keepAliveTimeout, String ackTimeout, String ackMaxRetry)
            throws InterruptedException, IOException {
        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(ipAddress);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(portNumber);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(keepAliveTimeout));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(null);
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ackTimeout));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ackMaxRetry));

            // Run run() of the socket handler once
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // client socket
            assertEquals(0, mocked.constructed().size());

            // Controller status should be published: Error --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"),
                    statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_ERROR, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1);
        }
    }

    @Test
    void shouldPublishAnErrorEventWhenConnectionIsNotEstablished() throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {
                    // Don't use any(). Instead, use a specific type one like any(XXX.class), anyInt(), anyString(), ...
                    doThrow(new SocketException()).when(connectedSocket2).setSoTimeout(anyInt());
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            // Run run() of the socket handler once
            clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
                    transactionHandler, null, Integer.valueOf(2));

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // client socket
            assertEquals(2, mocked.constructed().size());

            // Controller status should be published: Error --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(3)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_ERROR, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_ERROR, statusCaptor.getAllValues().get(1));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(2));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1);
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Test
    void shouldCloseConnectionWhenConnectionIsClosedByServer() throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate closed socket
            when(socketInputStream.read(any())).thenReturn(-1);

            // Run run() of the socket handler once
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler.process() shouldn't be called
            verify(transactionHandler, never()).process();

            // transactionHandler.isKeepAliveTimedOut() shouldn't be called
            verify(transactionHandler, never()).isKeepAliveTimedOut();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldCloseConnectionWhenTransactionProcessingFails() throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate the case when nothing is received
            when(socketInputStream.read(any())).thenReturn(0);

            // Let the transaction returns false to simulate the case when transaction processing fails
            when(transactionHandler.process()).thenReturn(false);

            // Run run() of the socket handler 2 times
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            // - connected 2 times as disconnected when transaction handler's process() returns false
            assertEquals(2, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();
            // In the 2nd connection
            // - Read timeout should be set
            verify(mocked.constructed().get(1), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(1), times(1)).getInputStream();
            verify(mocked.constructed().get(1), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(1), times(1)).getLocalAddress();
            verify(mocked.constructed().get(1), times(1)).getLocalPort();
            verify(mocked.constructed().get(1), times(1)).getInetAddress();
            verify(mocked.constructed().get(1), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(2)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() should be called 2 times
            verify(transactionHandler, times(2)).process();

            // transactionHandler.isKeepAliveTimedOut() shouldn't be called
            verify(transactionHandler, never()).isKeepAliveTimedOut();

            // The 1st close: When transaction handler's process() returned false
            // The 2nd close: as it reaches the max execution, input stream, connected socket and server socket should
            // be closed
            verify(socketInputStream, times(2)).close();
            verify(socketOutputStream, times(2)).close();
            verify(mocked.constructed().get(0), times(1)).close();
            verify(mocked.constructed().get(1), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(3)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(1));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(2));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0), mocked.constructed().get(1));
        }
    }

    @Test
    void shouldCloseConnectionWhenKeepAliveIsNotReceivedWithinTimeout() throws IOException {
        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate the case when nothing is received
            when(socketInputStream.read(any())).thenReturn(0);

            // Let the transaction.isKeepAliveTimedOut() returns false to simulate the case when keep alive is timed out
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(true);

            // Run run() of the socket handler 2 times
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            // - connected 2 times as disconnected when transaction handler's process() returns false
            assertEquals(2, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();
            // In the 2nd connection
            // - Read timeout should be set
            verify(mocked.constructed().get(1), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(1), times(1)).getInputStream();
            verify(mocked.constructed().get(1), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(1), times(1)).getLocalAddress();
            verify(mocked.constructed().get(1), times(1)).getLocalPort();
            verify(mocked.constructed().get(1), times(1)).getInetAddress();
            verify(mocked.constructed().get(1), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(2)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() and isKeepAliveTimedOut should be called 2 times
            verify(transactionHandler, times(2)).process();
            verify(transactionHandler, times(2)).isKeepAliveTimedOut();

            // The 1st close: When transaction handler's process() returned false
            // The 2nd close: as it reaches the max execution, input stream, connected socket and server socket should
            // be closed
            verify(socketInputStream, times(2)).close();
            verify(socketOutputStream, times(2)).close();
            verify(mocked.constructed().get(0), times(1)).close();
            verify(mocked.constructed().get(1), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(3)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(1));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(2));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0), mocked.constructed().get(1));
        }
    }

    @Test
    void shouldStopItsExecutionWhenShutdownIsCalled() throws IOException, InterruptedException {
        // Configurations
        when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
        when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
        when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                .thenReturn(IP_ADDRESS);
        when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                .thenReturn(PORT_NUMBER);
        when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                .thenReturn(String.valueOf(KEEP_ALIVE));
        when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                .thenReturn(String.valueOf(RETRY_INTERVAL));
        when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                .thenReturn(String.valueOf(ACK_TIMEOUT));
        when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                .thenReturn(String.valueOf(ACK_MAX_RETRY));

        // Return the mock input/output stream
        when(connectedSocket1.getInputStream()).thenReturn(socketInputStream);
        when(connectedSocket1.getOutputStream()).thenReturn(socketOutputStream);

        // Run run() of the socket handler, practically indefinitely
        try {
			clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
			        transactionHandler, connectedSocket1, Integer.MAX_VALUE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // To call shutdown(), it's necessary to run the socket handler as a thread
        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(clientSocketHandler);
        clientSocketHandler.shutdown();
        es.awaitTermination(1, TimeUnit.SECONDS);

        // Input stream and connected socket should be closed
        verify(socketInputStream, times(1)).close();
        verify(socketOutputStream, times(1)).close();
        verify(connectedSocket1, times(1)).close();

        // Controller status should be published: Running --> Stopped
        ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
        verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
        assertEquals((long) ControllerConsts.STATUS_STOPPING, statusCaptor.getAllValues().get(0));
        assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

        verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                connectedSocket1);
    }

    @Test
    void shouldContinueItsExecutionWhenNothingIsReceivedWithinTimeout() throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate the case when nothing is received
            when(socketInputStream.read(any())).thenReturn(0);

            // Let the transaction continue
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the socket handler 2 times
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            assertEquals(1, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() and isKeepAliveTimedOut() should be called 2 times
            verify(transactionHandler, times(2)).process();
            verify(transactionHandler, times(2)).isKeepAliveTimedOut();

            // As it reaches the max execution, input/output stream and socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldContinueItsExecutionWhenSocketTimeoutExceptionIsThrown() throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate SocketTimeoutException
            when(socketInputStream.read(any())).thenThrow(new SocketTimeoutException());

            // Let the transaction continue
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the socket handler 2 times
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            assertEquals(1, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() and isKeepAliveTimedOut() should be called 2 times
            verify(transactionHandler, times(2)).process();
            verify(transactionHandler, times(2)).isKeepAliveTimedOut();

            // As it reaches the max execution, input/output stream and socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldDisconnectWhenUnexpectedExceptionIsThrown() throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate unexpected exception and then nothing is received
            when(socketInputStream.read(any()))
                    .thenThrow(new IOException()) // Unexpected exception
                    .thenReturn(0); // Nothing is received

            // Let the transaction continue
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the socket handler 2 times
            // - the 1st execution: unexpected exception
            // - the 2nd execution: nothing is received
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            assertEquals(2, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();

            // In the 2nd connection
            // - Read timeout should be set
            verify(mocked.constructed().get(1), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(1), times(1)).getInputStream();
            verify(mocked.constructed().get(1), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(1), times(1)).getLocalAddress();
            verify(mocked.constructed().get(1), times(1)).getLocalPort();
            verify(mocked.constructed().get(1), times(1)).getInetAddress();
            verify(mocked.constructed().get(1), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(2)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() and isKeepAliveTimedOut() should be called 2 times
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(1)).isKeepAliveTimedOut();

            // As it reaches the max execution, input/output stream and socket should be closed
            verify(socketInputStream, times(2)).close();
            verify(socketOutputStream, times(2)).close();
            verify(mocked.constructed().get(0), times(1)).close();
            verify(mocked.constructed().get(1), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(3)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(1));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(2));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0), mocked.constructed().get(1));
        }
    }

    // Prepare the list of parts of 1 valid message for the following unit test method
    private static Stream<Arguments> parts() {
        return Stream.of(
                Arguments.of("STX + header + body + ETX received in 1 read",
                        (Object) new String[] { "02 00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01 03" }),
                Arguments.of("STX -> header + body + ETX received in 2 reads",
                        (Object) new String[] { "02", "00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01 03" }),
                Arguments.of("STX + the half of header -> the half of header + body + ETX received in 2 reads",
                        (Object) new String[] { "02 00 12", "00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01 03" }),
                Arguments.of("STX + header -> body + ETX received in 2 reads",
                        (Object) new String[] { "02 00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00", "00 01 03" }),
                Arguments.of("STX + header + the half of body -> the half of body + ETX received in 2 reads",
                        (Object) new String[] { "02 00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00", "01 03" }),
                Arguments.of("STX + header + body -> ETX received in 2 reads",
                        (Object) new String[] { "02 00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01", "03" }),
                Arguments.of("STX -> header + body -> ETX received in 3 reads",
                        (Object) new String[] { "02", "00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01", "03" }),
                Arguments.of("STX -> header -> body -> ETX received in 4 reads",
                        (Object) new String[] { "02", "00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00", "00 01",
                                "03" }));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("parts")
    void shouldProcessTheReceivedMessageWhenAValidMessageIsReceived(String test, String... parts) throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate the case when a message is received
            final byte[] receivedMessage = MessageUtil.hexStringToByteArray(String.join("", parts));
            byte[] receivedMessageWithoutSTXETX = new byte[PLCConstants.MSG_HEADER_LEN
                    + PLCConstants.KEEPALIVE_MSG_BODY_LEN];
            System.arraycopy(receivedMessage, 1, receivedMessageWithoutSTXETX, 0, receivedMessageWithoutSTXETX.length);

            // Dynamic stubbing to use the provided parameters from parts()
            OngoingStubbing<Integer> socketInputStreamReadStubbing;
            socketInputStreamReadStubbing = when(socketInputStream.read(any()));
            for (String part : parts) {
                // The reference should be replaced. If not, mockito will fail.
                socketInputStreamReadStubbing = socketInputStreamReadStubbing
                        .thenAnswer(new Answer<Integer>() {
                            public Integer answer(InvocationOnMock invocation) throws Throwable {
                                // Copy the message into the byte array argument
                                byte[] receivedByteArray = (byte[]) invocation.getArguments()[0];
                                byte[] message = MessageUtil.hexStringToByteArray(part);
                                System.arraycopy(message, 0, receivedByteArray, 0, message.length);
                                // Return the length of the message
                                return (Integer) message.length;
                            }
                        });
            }

            // Let the transaction returns true
            when(transactionHandler.addInboundMessage(receivedMessageWithoutSTXETX)).thenReturn(true);
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the server socket handler by the number of parts
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(parts.length));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            assertEquals(1, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called by the number of parts
            verify(socketInputStream, times(parts.length)).read(any());

            // transactionHandler's addInboundMessage(), process() and isKeepAliveTimedOut() should be called
            ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(transactionHandler, times(1)).addInboundMessage(byteArrayCaptor.capture());
            assertEquals(1, byteArrayCaptor.getAllValues().size());
            assertTrue(Arrays.equals(receivedMessageWithoutSTXETX, byteArrayCaptor.getAllValues().get(0)));
            verify(transactionHandler, times(parts.length)).process();
            verify(transactionHandler, times(parts.length)).isKeepAliveTimedOut();

            // As it reaches the max execution, input/output stream and socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldProcessTheReceivedMessagesWhenMultipleValidMessagesAreReceivedAtTheSameTime() throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate the case when 2 same messages are received at the same time
            final byte[] receivedMessages = MessageUtil
                    .hexStringToByteArray(VALID_KEEP_ALIVE_BINARY_MSG + VALID_KEEP_ALIVE_BINARY_MSG);
            byte[] receivedMessageWithoutSTXETX = new byte[PLCConstants.MSG_HEADER_LEN
                    + PLCConstants.KEEPALIVE_MSG_BODY_LEN];
            System.arraycopy(receivedMessages, 1, receivedMessageWithoutSTXETX, 0, receivedMessageWithoutSTXETX.length);
            when(socketInputStream.read(any())).thenAnswer(new Answer<Integer>() {
                public Integer answer(InvocationOnMock invocation) throws Throwable {
                    // Copy the message into the byte array argument
                    byte[] receivedByteArray = (byte[]) invocation.getArguments()[0];
                    byte[] message = receivedMessages;
                    System.arraycopy(message, 0, receivedByteArray, 0, message.length);
                    // Return the length of the message
                    return (Integer) message.length;
                }
            });

            // Let the transaction returns true
            when(transactionHandler.addInboundMessage(receivedMessageWithoutSTXETX)).thenReturn(true);
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the server socket handler once
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            assertEquals(1, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called once
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler's addInboundMessage(), process() and isKeepAliveTimedOut() should be called
            ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(transactionHandler, times(2)).addInboundMessage(byteArrayCaptor.capture()); // 2 messages received
            assertEquals(2, byteArrayCaptor.getAllValues().size());
            assertTrue(Arrays.equals(receivedMessageWithoutSTXETX, byteArrayCaptor.getAllValues().get(0)));
            assertTrue(Arrays.equals(receivedMessageWithoutSTXETX, byteArrayCaptor.getAllValues().get(1)));
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(1)).isKeepAliveTimedOut();

            // As it reaches the max execution, input/output stream and socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0));
        }
    }

    // Prepare the list of invalid message for the following unit test method
    private static Stream<Arguments> invalidMessages() {
        return Stream.of(
                Arguments.of("Invalid message type", INVALID_MSG_WITH_INVALID_MSG_TYPE),
                Arguments.of("No STX", INVALID_MSG_WITHOUT_STX),
                Arguments.of("No ETX", INVALID_MSG_WITHOUT_ETX));
    }

    @ParameterizedTest(name = "{index}: {0} - {1}")
    @MethodSource("invalidMessages")
    void shouldIgnoreTheReceivedMessageWhenAMessageWithInvalidMessageIsReceived(String reason, String invalidMessage)
            throws IOException {

        try (MockedConstruction<Socket> mocked = Mockito.mockConstruction(Socket.class,
                (connectedSocket2, context) -> {

                    doNothing().when(connectedSocket2).setSoTimeout(anyInt());

                    when(connectedSocket2.getInputStream()).thenReturn(socketInputStream);
                    when(connectedSocket2.getOutputStream()).thenReturn(socketOutputStream);

                    // Return the mock local/remote address of the connected socket
                    when(connectedSocket2.getLocalAddress()).thenReturn(localInetAddress);
                    when(connectedSocket2.getLocalPort()).thenReturn(LOCAL_PORT);
                    when(connectedSocket2.getInetAddress()).thenReturn(remoteInetAddress);
                    when(connectedSocket2.getPort()).thenReturn(REMOTE_PORT);
                })) {

            // Configurations
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_PORT_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue()))
                    .thenReturn(SACClientSocketHandler.DEFAULT_INTEGRATOR_NAME);
            when(connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue()))
                    .thenReturn(IP_ADDRESS);
            when(connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue()))
                    .thenReturn(PORT_NUMBER);
            when(connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(KEEP_ALIVE));
            when(connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue()))
                    .thenReturn(String.valueOf(RETRY_INTERVAL));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue()))
                    .thenReturn(String.valueOf(ACK_TIMEOUT));
            when(connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue()))
                    .thenReturn(String.valueOf(ACK_MAX_RETRY));

            when(remoteInetAddress.getHostAddress()).thenReturn(IP_ADDRESS);

            // Simulate the case when a message is received
            final byte[] receivedMessage = MessageUtil.hexStringToByteArray(invalidMessage);
            when(socketInputStream.read(any())).thenAnswer(new Answer<Integer>() {
                public Integer answer(InvocationOnMock invocation) throws Throwable {
                    // Copy the message into the byte array argument
                    byte[] receivedByteArray = (byte[]) invocation.getArguments()[0];
                    byte[] message = receivedMessage;
                    System.arraycopy(message, 0, receivedByteArray, 0, message.length);
                    // Return the length of the message
                    return (Integer) message.length;
                }
            });

            // Let the transaction returns true
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the server socket handler once
            try {
				clientSocketHandler = new SACClientSocketHandler(connectionProperties, logger, messageService,
				        transactionHandler, null, Integer.valueOf(1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Constructor mocking works only in the current thread, so have to call run() directly
            clientSocketHandler.run();

            // Connected socket
            assertEquals(1, mocked.constructed().size());

            // In the 1st connection
            // - Read timeout should be set
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(SACClientSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(mocked.constructed().get(0), times(1)).getInputStream();
            verify(mocked.constructed().get(0), times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(mocked.constructed().get(0), times(1)).getLocalAddress();
            verify(mocked.constructed().get(0), times(1)).getLocalPort();
            verify(mocked.constructed().get(0), times(1)).getInetAddress();
            verify(mocked.constructed().get(0), times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected(KEEP_ALIVE * 2, socketOutputStream);

            // socketInputStream.read() should be called once
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler's process() and isKeepAliveTimedOut() should be called
            verify(transactionHandler, never()).addInboundMessage(any(byte[].class)); // Shouldn't be registered
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(1)).isKeepAliveTimedOut();

            // As it reaches the max execution, input/output stream and socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(SACClientSocketHandler.DEFAULT_PORT_NAME), isNull(),
                    eq("StatusChange"), statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACClientSocketHandler.DEFAULT_PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket1, mocked.constructed().get(0));
        }
    }
}
