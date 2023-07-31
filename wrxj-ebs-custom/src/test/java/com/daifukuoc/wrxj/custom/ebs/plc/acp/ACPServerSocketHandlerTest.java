package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import java.net.SocketTimeoutException;
import java.util.Arrays;
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
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.processor.StandardMessageProcessor;

@ExtendWith(MockitoExtension.class)
class ACPServerSocketHandlerTest {

    private static final int ONE_VALID_MESSAGE_SAVED = 1;
    private static final int TWO_VALID_MESSAGES_SAVED = 2;
    private static final String HOST_ADDRESS = "localhost";
    private static final int REMOTE_PORT = 5678;
    private static final int LOCAL_PORT = 1234;
    private static final String PORT_NAME = "ACP1-Port";
    private static final String VALID_PORT_NUMBER = "4501";
    private static final String INVALID_PORT_NUMBER = "1";
    private static final int VALID_KEEP_ALIVE_INTERVAL = 1 * 1000;
    private static final int INVALID_KEEP_ALIVE_INTERVAL = 0;
    private static final int VALID_RETRY_INTERVAL = 1 * 1000;
    private static final boolean VALID_ENABLE_WRAPPING = true;

    private static final String VALID_KEEP_ALIVE_BINARY_MSG = "02 00 12 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 01 03";
    private static final String INVALID_MSG_WITH_INVALID_MSG_TYPE = "02 00 12 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 01 03";
    private static final String INVALID_MSG_WITH_INVALID_MSG_LENGTH = "02 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 01 03";
    private static final String INVALID_MSG_WITHOUT_STX = "00 12 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 01 03";
    private static final String INVALID_MSG_WITHOUT_ETX = "02 00 12 00 00 00 FF 00 00 00 00 00 00 00 00 00 00 00 01";

    MockedStatic<Factory> mockedFactory;

    @Mock
    PortData portData;

    @Mock
    Logger logger;

    @Mock
    MessageService messageService;

    @Mock
    ACPTransactionHandler transactionHandler;

    @Mock
    ServerSocket serverSocket2;

    @Mock
    Socket connectedSocket;

    @Mock
    InetAddress localInetAddress;

    @Mock
    InetAddress remoteInetAddress;

    @Mock
    InputStream socketInputStream;

    @Mock
    OutputStream socketOutputStream;

    StandardMessageProcessor messageProcessor;

    @Mock
    ExecutorService executor;

    ACPServerSocketHandler serverSocketHandler;

    @BeforeEach
    void setUp() throws Exception {
        messageProcessor = new StandardMessageProcessor();

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(StandardMessageProcessor.class)).thenReturn(messageProcessor);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }

    @Test
    void shouldRetryToStartAServerSocketEvenWhenThePortNumberIsInvalid()
            throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(INVALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Run run() of the socket handler 2 times
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(2));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            assertEquals(0, mocked.constructed().size());

            // disconnected() of transaction handler is called
            verify(transactionHandler, times(1)).disconnected();

            // Controller status should be published: Error --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_ERROR, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket);
        }
    }

    @Test
    void shouldRetryToStartAServerSocketEvenWhenTheKeepAliveTimeoutIsInvalid()
            throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(INVALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Run run() of the socket handler 2 times
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(2));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            assertEquals(0, mocked.constructed().size());

            // disconnected() of transaction handler is called
            verify(transactionHandler, times(1)).disconnected();

            // Controller status should be published: Error --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_ERROR, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket);
        }
    }

    @Test
    void shouldRetryWhenConnectionIsNotEstablished() throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    when(serverSocket1.accept())
                            .thenThrow(new SocketTimeoutException()) // Connection timeout
                            .thenThrow(new IOException()) // Unexpected exception
                            .thenReturn(connectedSocket); // Connection established
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(1234);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(5678);

            // Let the transaction returns true
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the socket handler 3 times
            // - the 1st execution: connection timeout
            // - the 2nd execution: unexpected exception
            // - the 3rd execution: connection established
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(3));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            assertEquals(1, mocked.constructed().size());
            verify(mocked.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            // - Should be called 2 times as there was a connection timeout
            verify(mocked.constructed().get(0), times(3)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(1)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(1)).getInputStream();
            verify(connectedSocket, times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(1)).getLocalAddress();
            verify(connectedSocket, times(1)).getLocalPort();
            verify(connectedSocket, times(1)).getInetAddress();
            verify(connectedSocket, times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler's process(), isKeepAliveTimedOut() and disconnected() should be called
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(1)).isKeepAliveTimedOut();
            verify(transactionHandler, times(1)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(connectedSocket, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldCloseConnectionWhenConnectionIsClosedByClient() throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(1234);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(5678);

            // Simulate closed socket
            when(socketInputStream.read(any())).thenReturn(-1);

            // Run run() of the socket handler only once
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(1));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mocked.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mocked.constructed().get(0), times(1)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(1)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(1)).getInputStream();
            verify(connectedSocket, times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(1)).getLocalAddress();
            verify(connectedSocket, times(1)).getLocalPort();
            verify(connectedSocket, times(1)).getInetAddress();
            verify(connectedSocket, times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler's process() and isKeepAliveTimedOut() shouldn't be called
            verify(transactionHandler, never()).process();
            verify(transactionHandler, never()).isKeepAliveTimedOut();

            // transactionHandler's disconnected() should be called 2 times
            // - Closed socket
            // - When it reaches the maximum execution cycle
            verify(transactionHandler, times(2)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(connectedSocket, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket,
                    mocked.constructed().get(0));
        }
    }

    @Test
    void shouldCloseConnectionWhenTransactionProcessingFails() throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(1234);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(5678);

            // Simulate the case when nothing is received
            when(socketInputStream.read(any())).thenReturn(0);

            // Let the transaction returns false to simulate the case when transaction processing fails
            when(transactionHandler.process()).thenReturn(false);

            // Run run() of the socket handler 2 times
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(2));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mocked.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mocked.constructed().get(0), times(2)).accept(); // As disconnected once

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(2)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(2)).getInputStream();
            verify(connectedSocket, times(2)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(2)).getLocalAddress();
            verify(connectedSocket, times(2)).getLocalPort();
            verify(connectedSocket, times(2)).getInetAddress();
            verify(connectedSocket, times(2)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(2)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() should be called 2 times
            verify(transactionHandler, times(2)).process();

            // transactionHandler.isKeepAliveTimedOut() shouldn't be called
            verify(transactionHandler, never()).isKeepAliveTimedOut();

            // transactionHandler.disconnected() should be called 3 times
            verify(transactionHandler, times(3)).disconnected();

            // The 1st close: When transaction handler's process() returned false
            // The 2nd close: as it reaches the max execution, input stream, connected socket and server socket should
            // be closed
            verify(socketInputStream, times(2)).close();
            verify(socketOutputStream, times(2)).close();
            verify(connectedSocket, times(2)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(3)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(1));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(2));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket,
                    mocked.constructed().get(0));
        }
    }

    @Test
    void shouldCloseConnectionWhenKeepAliveIsNotReceivedWithinTimeout() throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(1234);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(5678);

            // Simulate the case when nothing is received
            when(socketInputStream.read(any())).thenReturn(0);

            // Let the transaction.isKeepAliveTimedOut() returns false to simulate the case when keep alive is timed out
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(true);

            // Run run() of the socket handler 2 times
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(2));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mocked.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mocked.constructed().get(0), times(2)).accept(); // As disconnected once

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(2)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(2)).getInputStream();
            verify(connectedSocket, times(2)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(2)).getLocalAddress();
            verify(connectedSocket, times(2)).getLocalPort();
            verify(connectedSocket, times(2)).getInetAddress();
            verify(connectedSocket, times(2)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(2)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler's process() and isKeepAliveTimedOut() should be called 2 times
            verify(transactionHandler, times(2)).process();
            verify(transactionHandler, times(2)).isKeepAliveTimedOut();

            // transactionHandler.disconnected() should be called 3 times
            verify(transactionHandler, times(3)).disconnected();

            // The 1st close: When transaction handler's process() returned false
            // The 2nd close: as it reaches the max execution, input stream, connected socket and server socket should
            // be closed
            verify(socketInputStream, times(2)).close();
            verify(socketOutputStream, times(2)).close();
            verify(connectedSocket, times(2)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(3)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(1));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(2));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldStopItsExecutionWhenShutdownIsCalled() throws Exception {
        // Configurations
        when(portData.getPortName()).thenReturn(PORT_NAME);
        when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
        when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
        when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
        when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

        // Return the mock input/output stream
        when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
        when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

        // Run run() of the socket handler, practically indefinitely
        serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                serverSocket2, connectedSocket, Integer.MAX_VALUE);

        // To call shutdown(), it's necessary to run ServerSocketHandler as a thread
        ExecutorService es = Executors.newCachedThreadPool();
        es.execute(serverSocketHandler);
        serverSocketHandler.shutdown();
        es.awaitTermination(1, TimeUnit.SECONDS);

        // As it reaches the max execution, input stream, connected socket and server socket should be closed
        verify(socketInputStream, times(1)).close();
        verify(socketOutputStream, times(1)).close();
        verify(connectedSocket, times(1)).close();
        verify(serverSocket2, times(1)).close();
        verify(transactionHandler, times(1)).disconnected();

        // Controller status should be published: Running --> Stopped
        ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
        verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                statusCaptor.capture(),
                eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
        assertEquals((long) ControllerConsts.STATUS_STOPPING, statusCaptor.getAllValues().get(0));
        assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

        verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                connectedSocket, serverSocket2);
    }

    @Test
    void shouldContinueItsExecutionWhenNothingIsReceivedWithinTimeout() throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(LOCAL_PORT);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(REMOTE_PORT);

            // Simulate the case when nothing is received
            when(socketInputStream.read(any())).thenReturn(0);

            // Let the transaction returns true
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the socket handler 2 times
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(2));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mocked.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mocked.constructed().get(0), times(1)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(1)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(1)).getInputStream();
            verify(connectedSocket, times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(1)).getLocalAddress();
            verify(connectedSocket, times(1)).getLocalPort();
            verify(connectedSocket, times(1)).getInetAddress();
            verify(connectedSocket, times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() and isKeepAliveTimedOut() should be called 2 times
            verify(transactionHandler, times(2)).process();
            verify(transactionHandler, times(2)).isKeepAliveTimedOut();

            // transactionHandler.disconnected() should be called
            verify(transactionHandler, times(1)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(connectedSocket, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldContinueItsExecutionWhenSocketTimeoutExceptionIsThrown() throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(LOCAL_PORT);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(REMOTE_PORT);

            // Simulate SocketTimeoutException
            when(socketInputStream.read(any())).thenThrow(new SocketTimeoutException());

            // Let the transaction returns true
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the socket handler only once
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(1));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mocked.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mocked.constructed().get(0), times(1)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(1)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(1)).getInputStream();
            verify(connectedSocket, times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(1)).getLocalAddress();
            verify(connectedSocket, times(1)).getLocalPort();
            verify(connectedSocket, times(1)).getInetAddress();
            verify(connectedSocket, times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler.process() and disconnected() should be called
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(1)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(connectedSocket, times(1)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mocked.constructed().get(0));
        }
    }

    @Test
    void shouldDisconnectWhenUnexpectedExceptionIsThrown() throws Exception {

        try (MockedConstruction<ServerSocket> mocked = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(LOCAL_PORT);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(REMOTE_PORT);

            // Simulate unexpected exception and then nothing is received
            when(socketInputStream.read(any()))
                    .thenThrow(new IOException()) // Unexpected exception
                    .thenReturn(0); // Nothing is received

            // Let the transaction returns true
            when(transactionHandler.process()).thenReturn(true);
            when(transactionHandler.isKeepAliveTimedOut()).thenReturn(false);

            // Run run() of the socket handler 2 times
            // - the 1st execution: unexpected exception
            // - the 2nd execution: nothing is received
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(2));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mocked.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mocked.constructed().get(0), times(1)).setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mocked.constructed().get(0), times(2)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(2)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(2)).getInputStream();
            verify(connectedSocket, times(2)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(2)).getLocalAddress();
            verify(connectedSocket, times(2)).getLocalPort();
            verify(connectedSocket, times(2)).getInetAddress();
            verify(connectedSocket, times(2)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(2)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(2)).read(any());

            // transactionHandler.process() and disconnected() should be called
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(2)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(2)).close();
            verify(socketOutputStream, times(2)).close();
            verify(connectedSocket, times(2)).close();
            verify(mocked.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(3)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(1));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(2));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mocked.constructed().get(0));
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
    void shouldProcessTheReceivedMessageWhenAValidMessageIsReceived(String test, String... parts) throws Exception {

        try (MockedConstruction<ServerSocket> mockedServerSocket = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(LOCAL_PORT);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(REMOTE_PORT);

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

            // Run run() of the socket handler by the number of parts
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(parts.length));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mockedServerSocket.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mockedServerSocket.constructed().get(0), times(1))
                    .setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mockedServerSocket.constructed().get(0), times(1)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(1)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(1)).getInputStream();
            verify(connectedSocket, times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(1)).getLocalAddress();
            verify(connectedSocket, times(1)).getLocalPort();
            verify(connectedSocket, times(1)).getInetAddress();
            verify(connectedSocket, times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called by the number of parts
            verify(socketInputStream, times(parts.length)).read(any());

            // transactionHandler's addInboundMessage(), process() and isKeepAliveTimedOut() should be called
            ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(transactionHandler, times(1)).addInboundMessage(byteArrayCaptor.capture());
            assertEquals(ONE_VALID_MESSAGE_SAVED, byteArrayCaptor.getAllValues().size());
            assertTrue(Arrays.equals(receivedMessageWithoutSTXETX, byteArrayCaptor.getAllValues().get(0)));
            verify(transactionHandler, times(parts.length)).process();
            verify(transactionHandler, times(parts.length)).isKeepAliveTimedOut();

            // transactionHandler.disconnected() should be called
            verify(transactionHandler, times(1)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(connectedSocket, times(1)).close();
            verify(mockedServerSocket.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mockedServerSocket.constructed().get(0));
        }
    }

    @Test
    void shouldProcessTheReceivedMessagesWhenMultipleValidMessagesAreReceivedAtTheSameTime() throws Exception {

        try (MockedConstruction<ServerSocket> mockedServerSocket = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(LOCAL_PORT);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(REMOTE_PORT);

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

            // Run run() of the socket handler only once
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(1));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mockedServerSocket.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mockedServerSocket.constructed().get(0), times(1))
                    .setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mockedServerSocket.constructed().get(0), times(1)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(1)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(1)).getInputStream();
            verify(connectedSocket, times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(1)).getLocalAddress();
            verify(connectedSocket, times(1)).getLocalPort();
            verify(connectedSocket, times(1)).getInetAddress();
            verify(connectedSocket, times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler's addInboundMessage(), process(), isKeepAliveTimedOut() and disconnected() should be
            // called
            ArgumentCaptor<byte[]> byteArrayCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(transactionHandler, times(2)).addInboundMessage(byteArrayCaptor.capture()); // 2 messages received
            assertEquals(TWO_VALID_MESSAGES_SAVED, byteArrayCaptor.getAllValues().size());
            assertTrue(Arrays.equals(receivedMessageWithoutSTXETX, byteArrayCaptor.getAllValues().get(0)));
            assertTrue(Arrays.equals(receivedMessageWithoutSTXETX, byteArrayCaptor.getAllValues().get(1)));
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(1)).isKeepAliveTimedOut();
            verify(transactionHandler, times(1)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(connectedSocket, times(1)).close();
            verify(mockedServerSocket.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(),
                    eq(MessageEventConsts.COMM_EVENT_TYPE), eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mockedServerSocket.constructed().get(0));
        }
    }

    // Prepare the list of invalid message for the following unit test method
    private static Stream<Arguments> invalidMessages() {
        return Stream.of(
                Arguments.of("Invalid message type", INVALID_MSG_WITH_INVALID_MSG_TYPE),
                Arguments.of("Invalid message length(0)", INVALID_MSG_WITH_INVALID_MSG_LENGTH),
                Arguments.of("No STX", INVALID_MSG_WITHOUT_STX),
                Arguments.of("No ETX", INVALID_MSG_WITHOUT_ETX));
    }

    @ParameterizedTest(name = "{index}: {0} - {1}")
    @MethodSource("invalidMessages")
    void shouldIgnoreTheReceivedMessageWhenAMessageWithInvalidMessageIsReceived(String reason, String invalidMessage)
            throws Exception {

        try (MockedConstruction<ServerSocket> mockedServerSocket = Mockito.mockConstruction(ServerSocket.class,
                (serverSocket1, context) -> {
                    // Return the mock connected socket
                    when(serverSocket1.accept()).thenReturn(connectedSocket);
                })) {

            // Configurations
            when(portData.getPortName()).thenReturn(PORT_NAME);
            when(portData.getSocketNumber()).thenReturn(VALID_PORT_NUMBER);
            when(portData.getRcvKeepAliveInterval()).thenReturn(VALID_KEEP_ALIVE_INTERVAL);
            when(portData.getRetryInterval()).thenReturn(VALID_RETRY_INTERVAL);
            when(portData.getEnableWrapping()).thenReturn(VALID_ENABLE_WRAPPING);

            // Return the mock input/output stream
            when(connectedSocket.getInputStream()).thenReturn(socketInputStream);
            when(connectedSocket.getOutputStream()).thenReturn(socketOutputStream);

            // Return the mock local/remote address of the connected socket
            when(connectedSocket.getLocalAddress()).thenReturn(localInetAddress);
            when(connectedSocket.getLocalPort()).thenReturn(LOCAL_PORT);
            when(connectedSocket.getInetAddress()).thenReturn(remoteInetAddress);
            when(remoteInetAddress.getHostAddress()).thenReturn(HOST_ADDRESS);
            when(connectedSocket.getPort()).thenReturn(REMOTE_PORT);

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

            // Run run() of the socket handler only once
            serverSocketHandler = new ACPServerSocketHandler(portData, logger, messageService, transactionHandler,
                    null, null, Integer.valueOf(1));

            // Constructor mocking works only in the current thread, so have to call run() directly
            serverSocketHandler.run();

            // Server socket
            verify(mockedServerSocket.constructed().get(0), times(1)).setReuseAddress(true);
            verify(mockedServerSocket.constructed().get(0), times(1))
                    .setSoTimeout(ACPServerSocketHandler.CONNECTION_TIMEOUT);
            verify(mockedServerSocket.constructed().get(0), times(1)).accept();

            // Connected socket
            // - Read timeout should be set
            verify(connectedSocket, times(1)).setSoTimeout(ACPServerSocketHandler.READ_TIMEOUT);
            // - Input/Output stream should be set
            verify(connectedSocket, times(1)).getInputStream();
            verify(connectedSocket, times(1)).getOutputStream();
            // - local/remote address of the connected socket should be called
            verify(connectedSocket, times(1)).getLocalAddress();
            verify(connectedSocket, times(1)).getLocalPort();
            verify(connectedSocket, times(1)).getInetAddress();
            verify(connectedSocket, times(1)).getPort();

            // Transaction handler.connected(...) should be called
            verify(transactionHandler, times(1)).connected((VALID_KEEP_ALIVE_INTERVAL * 2) / 1000,
                    socketOutputStream);

            // socketInputStream.read() should be called
            verify(socketInputStream, times(1)).read(any());

            // transactionHandler's and process(), isKeepAliveTimedOut() and disconnected() should be called
            verify(transactionHandler, never()).addInboundMessage(any(byte[].class)); // Shouldn't be registered
            verify(transactionHandler, times(1)).process();
            verify(transactionHandler, times(1)).isKeepAliveTimedOut();
            verify(transactionHandler, times(1)).disconnected();

            // As it reaches the max execution, input stream, connected socket and server socket should be closed
            verify(socketInputStream, times(1)).close();
            verify(socketOutputStream, times(1)).close();
            verify(connectedSocket, times(1)).close();
            verify(mockedServerSocket.constructed().get(0), times(1)).close();

            // Controller status should be published: Running --> Stopped
            ArgumentCaptor<Long> statusCaptor = ArgumentCaptor.forClass(Long.class);
            verify(messageService, times(2)).publishEvent(eq(PORT_NAME), isNull(), eq("StatusChange"),
                    statusCaptor.capture(), eq(MessageEventConsts.COMM_EVENT_TYPE),
                    eq(MessageEventConsts.COMM_EVENT_TYPE_TEXT + PORT_NAME));
            assertEquals((long) ControllerConsts.STATUS_RUNNING, statusCaptor.getAllValues().get(0));
            assertEquals((long) ControllerConsts.STATUS_STOPPED, statusCaptor.getAllValues().get(1));

            verifyNoMoreInteractions(messageService, transactionHandler, socketInputStream, socketOutputStream,
                    connectedSocket, mockedServerSocket.constructed().get(0));
        }
    }
}
