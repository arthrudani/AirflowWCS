package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;

@ExtendWith(MockitoExtension.class)
class ACPPortTest {

    private static final String PORT_NAME = "ACP1-Port";
    
    private static final String MOVE_ORDER_MSG = "2,1,0,1,2,3,4,20230101123456,5,6,7,0";
    private static final String FLIGHT_DATA_UPDATE_MSG = "9,1,FL100,1234";

    MockedStatic<Factory> mockedFactory;

    @Mock
    ReadOnlyProperties config;

    @Mock
    PortData portData;

    @Mock
    StandardPortServer portServer;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    IpcMessage ipcMessage;

    @Mock
    ACPTransactionHandler transactionHandler;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
        mockedFactory.when(() -> Factory.create(StandardPortServer.class)).thenReturn(portServer);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();

    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        ACPPort acpPortCreatedByFactory = new ACPPort(config);
        try (MockedStatic<ACPPort> mockedACPPort = Mockito.mockStatic(ACPPort.class)) {
            mockedACPPort.when(() -> ACPPort.create(any(ReadOnlyProperties.class))).thenReturn(acpPortCreatedByFactory);

            try {
                ACPPort controller = (ACPPort) ACPPort.create(config);
                assertEquals(acpPortCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() throws DBException {
        when(config.getString(ControllerDefinition.CONTROLLER_NAME)).thenReturn(PORT_NAME);
        when(portServer.getPort(PORT_NAME)).thenReturn(portData);
        doNothing().when(portServer).cleanUp();

        // Mocking for new ACPServerSocketHandler(...) and new NamedThread(...)
        try (
                MockedConstruction<ACPServerSocketHandler> mockedServerSocketHandler = Mockito.mockConstruction(
                        ACPServerSocketHandler.class,
                        (serverSocketHandler, context) -> {
                            doNothing().when(serverSocketHandler).run();
                            doNothing().when(serverSocketHandler).shutdown();
                        });
                MockedConstruction<NamedThread> mockedNamedThread = Mockito.mockConstruction(NamedThread.class,
                        (namedThread, context) -> {
                            doNothing().when(namedThread).start();
                        })) {
            // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
            // actual behaviour seems too difficult for now.
            ACPPort acpPort = new ACPPort(config);
            acpPort.setMessageService(messageService);
            acpPort.setLogger(logger);
            acpPort.initialize(ACPPort.class.getName());
            acpPort.startup();

            // The handler should subscribe to the following 5 events
            ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
            verify(messageService, times(5)).addSubscriber(subscriptionSelectorCaptor.capture(), any());
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                    .anyMatch(selector -> selector.equals(MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE_TEXT)));
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream().anyMatch(
                    selector -> selector.contains(MessageEventConsts.REQUEST_EVENT_TYPE_TEXT)));
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream().anyMatch(
                    selector -> selector.contains(MessageEventConsts.PORT_EQPMNT_EVENT_TYPE_TEXT)));
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream().anyMatch(
                    selector -> selector.contains(MessageEventConsts.COMM_EVENT_TYPE_TEXT)));
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream().anyMatch(
                    selector -> selector.contains(MessageEventConsts.CONTROL_EVENT_TYPE_TEXT)));
        }
    }

    @Test
    void shouldProcessReceivedEventWhenCommEventIsPublished() throws DBException {
        when(config.getString(ControllerDefinition.CONTROLLER_NAME)).thenReturn(PORT_NAME);
        when(portServer.getPort(PORT_NAME)).thenReturn(portData);
        doNothing().when(portServer).cleanUp();

        // Mocking for new ACPServerSocketHandler(...) and new NamedThread(...)
        try (
                MockedConstruction<ACPServerSocketHandler> mockedServerSocketHandler = Mockito.mockConstruction(
                        ACPServerSocketHandler.class,
                        (serverSocketHandler, context) -> {
                            doNothing().when(serverSocketHandler).run();
                            doNothing().when(serverSocketHandler).shutdown();
                            when(serverSocketHandler.getTransactionHandler()).thenReturn(transactionHandler);
                        });
                MockedConstruction<NamedThread> mockedNamedThread = Mockito.mockConstruction(NamedThread.class,
                        (namedThread, context) -> {
                            doNothing().when(namedThread).start();
                        })) {

            // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
            // actual behaviour seems too difficult for now.
            ACPPort acpPort = new ACPPort(config);
            acpPort.setMessageService(messageService);
            acpPort.setLogger(logger);
            acpPort.initialize(ACPPort.class.getName());
            acpPort.startup();

            // This is to mimic how event is received
            // When running, running status is published
            when(ipcMessage.getEventType())
                    .thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData())
                    .thenReturn((long) ControllerConsts.STATUS_RUNNING);
            when(ipcMessage.getMessageText())
                    .thenReturn(ACPServerSocketHandler.COM_PORT_STATUS_CHANGE);
            acpPort.decodeIpcMessage(ipcMessage);
            acpPort.processIPCReceivedMessage();
            verify(messageService, times(1)).publishEvent(eq(ACPPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_RUNNING]),
                    eq((long) ControllerConsts.STATUS_RUNNING), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + ACPPort.class.getName()));

            // When stopping, nothing is published
            when(ipcMessage.getEventType())
                    .thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData())
                    .thenReturn((long) ControllerConsts.STATUS_STOPPING);
            when(ipcMessage.getMessageText())
                    .thenReturn(ACPServerSocketHandler.COM_PORT_STATUS_CHANGE);
            acpPort.decodeIpcMessage(ipcMessage);
            acpPort.processIPCReceivedMessage();
            verify(messageService, never()).publishEvent(eq(ACPPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_STOPPING]),
                    eq((long) ControllerConsts.STATUS_STOPPING), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + ACPPort.class.getName()));

            // When stopped, nothing is published
            when(ipcMessage.getEventType())
                    .thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData())
                    .thenReturn((long) ControllerConsts.STATUS_STOPPED);
            when(ipcMessage.getMessageText())
                    .thenReturn(ACPServerSocketHandler.COM_PORT_STATUS_CHANGE);
            acpPort.decodeIpcMessage(ipcMessage);
            acpPort.processIPCReceivedMessage();
            verify(messageService, never()).publishEvent(eq(ACPPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_STOPPED]),
                    eq((long) ControllerConsts.STATUS_STOPPED), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + ACPPort.class.getName()));

            // When error, error is published
            when(ipcMessage.getEventType())
                    .thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData())
                    .thenReturn((long) ControllerConsts.STATUS_ERROR);
            when(ipcMessage.getMessageText())
                    .thenReturn(ACPServerSocketHandler.COM_PORT_STATUS_CHANGE);
            acpPort.decodeIpcMessage(ipcMessage);
            acpPort.processIPCReceivedMessage();
            verify(messageService, times(1)).publishEvent(eq(ACPPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_ERROR]),
                    eq((long) ControllerConsts.STATUS_ERROR), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + ACPPort.class.getName()));
        }
    }

    private static Stream<Arguments> outboundMessageTypesThatACPShouldSendAck() {
        return Stream.of(
                Arguments.of("Move order", MOVE_ORDER_MSG),
                Arguments.of("Flight data update", FLIGHT_DATA_UPDATE_MSG)
                );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("outboundMessageTypesThatACPShouldSendAck")
    void shouldProcessReceivedEventWhenCustomAllocationEventIsPublished(String test, String outboundMessage) throws DBException {
        when(config.getString(ControllerDefinition.CONTROLLER_NAME)).thenReturn(PORT_NAME);
        when(portServer.getPort(PORT_NAME)).thenReturn(portData);
        doNothing().when(portServer).cleanUp();

        // Mocking for new ACPServerSocketHandler(...) and new NamedThread(...)
        try (
                MockedConstruction<ACPServerSocketHandler> mockedServerSocketHandler = Mockito.mockConstruction(
                        ACPServerSocketHandler.class,
                        (serverSocketHandler, context) -> {
                            doNothing().when(serverSocketHandler).run();
                            doNothing().when(serverSocketHandler).shutdown();
                            when(serverSocketHandler.getTransactionHandler()).thenReturn(transactionHandler);
                        });
                MockedConstruction<NamedThread> mockedNamedThread = Mockito.mockConstruction(NamedThread.class,
                        (namedThread, context) -> {
                            doNothing().when(namedThread).start();
                        })) {

            // Mock CUSTOM_ALLOCATION_EVENT_TYPE event
            when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.CUSTOM_ALLOCATION_EVENT_TYPE);
            when(ipcMessage.getMessageText()).thenReturn(outboundMessage);

            // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
            // actual behaviour seems too difficult for now.
            ACPPort acpPort = new ACPPort(config);
            acpPort.setMessageService(messageService);
            acpPort.setLogger(logger);
            acpPort.initialize(ACPPort.class.getName());
            acpPort.startup();

            // This is to mimic how event is received
            acpPort.decodeIpcMessage(ipcMessage);
            acpPort.processIPCReceivedMessage();

            // Should add the outbound message to the transaction handler
            ArgumentCaptor<String> outboundMessageCaptor = ArgumentCaptor.forClass(String.class);
            verify(transactionHandler, times(1)).addOutboundMessage(outboundMessageCaptor.capture());
            assertEquals(1, outboundMessageCaptor.getAllValues().size());
            assertEquals(outboundMessage, outboundMessageCaptor.getAllValues().get(0));
        }
    }
}
