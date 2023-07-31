package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

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

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPPort;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.ACPServerSocketHandler;

@ExtendWith(MockitoExtension.class)
class SACPortTest {

    private static final Object object = new Object();
    private static final String STORE_COMPLETE_MSG = "32767;" + MessageOutNames.STORE_COMPLETE.getValue() + ";"
            + SACControlMessage.StoreCompletionNotify.MSG_TYPE + "111" + ";" + "222" + ";" + "333" + ";" + "BAG444"
            + ";" + "0" + ";" + "555" + ";" + "1";

    MockedStatic<Factory> mockedFactory;

    @Mock
    ReadOnlyProperties config;

    @Mock
    Properties connectionProperties;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    IpcMessage ipcMessage;

    @Mock
    SACTransactionHandler transactionHandler;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        SACPort sacPortCreatedByFactory = new SACPort(config);
        try (MockedStatic<SACPort> mockedSACPort = Mockito
                .mockStatic(SACPort.class)) {
            mockedSACPort
                    .when(() -> SACPort.create(any(ReadOnlyProperties.class)))
                    .thenReturn(sacPortCreatedByFactory);

            try {
                SACPort controller = (SACPort) SACPort
                        .create(config);
                assertEquals(sacPortCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() throws DBException {
        when(config.getString(eq(SACPort.COMM_TYPE))).thenReturn("SACHost");

        // Mocking for new SACClientSocketHandler(...) and new NamedThread(...)
        try (
                MockedConstruction<SACClientSocketHandler> mockedClientSocketHandler = Mockito.mockConstruction(
                        SACClientSocketHandler.class,
                        (clientSocketHandler, context) -> {
                            doNothing().when(clientSocketHandler).run();
                            doNothing().when(clientSocketHandler).shutdown();
                        });
                MockedConstruction<NamedThread> mockedNamedThread = Mockito.mockConstruction(NamedThread.class,
                        (namedThread, context) -> {
                            doNothing().when(namedThread).start();
                        });
                MockedConstruction<Properties> mockedConnectionProperties = Mockito.mockConstruction(
                        Properties.class,
                        (connectionProperties, context) -> {
                            when(connectionProperties.put(any(), any())).thenReturn(object);
                        });) {

            // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
            // actual behaviour seems too difficult for now.
            SACPort sacPort = new SACPort(config);
            sacPort.setMessageService(messageService);
            sacPort.setLogger(logger);
            sacPort.initialize(SACPort.class.getName());
            sacPort.startup();

            // The handler should subscribe to the following 5 events
            ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
            verify(messageService, times(4)).addSubscriber(subscriptionSelectorCaptor.capture(), any());
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                    .anyMatch(selector -> selector.equals(MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE_TEXT)));
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream().anyMatch(
                    selector -> selector.equals(MessageEventConsts.COMM_EVENT_TYPE_TEXT + SACPort.class.getName())));
            assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                    .anyMatch(selector -> selector.equals(MessageEventConsts.HOST_MESG_SEND_TEXT + "%")));
        }
    }

    @Test
    void shouldProcessReceivedEventWhenCommEventIsPublished() throws DBException {
        when(config.getString(eq(SACPort.COMM_TYPE))).thenReturn("SACHost");

        // Mocking for new SACClientSocketHandler(...) and new NamedThread(...)
        try (
                MockedConstruction<SACClientSocketHandler> mockedClientSocketHandler = Mockito.mockConstruction(
                        SACClientSocketHandler.class,
                        (clientSocketHandler, context) -> {
                            doNothing().when(clientSocketHandler).run();
                            doNothing().when(clientSocketHandler).shutdown();
                        });
                MockedConstruction<NamedThread> mockedNamedThread = Mockito.mockConstruction(NamedThread.class,
                        (namedThread, context) -> {
                            doNothing().when(namedThread).start();
                        });
                MockedConstruction<Properties> mockedConnectionProperties = Mockito.mockConstruction(
                        Properties.class,
                        (connectionProperties, context) -> {
                            when(connectionProperties.put(any(), any())).thenReturn(object);
                        });) {

            // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
            // actual behaviour seems too difficult for now.
            SACPort sacPort = new SACPort(config);
            sacPort.setMessageService(messageService);
            sacPort.setLogger(logger);
            sacPort.initialize(SACPort.class.getName());
            sacPort.startup();

            // This is to mimic how event is received
            // When running, running status is published
            when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData()).thenReturn((long) ControllerConsts.STATUS_RUNNING);
            when(ipcMessage.getMessageText()).thenReturn(SACClientSocketHandler.COM_PORT_STATUS_CHANGE);
            sacPort.decodeIpcMessage(ipcMessage);
            sacPort.processIPCReceivedMessage();
            verify(messageService, times(1)).publishEvent(eq(SACPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_RUNNING]),
                    eq((long) ControllerConsts.STATUS_RUNNING), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + SACPort.class.getName()));

            // When stopping, nothing is published
            when(ipcMessage.getEventType())
                    .thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData())
                    .thenReturn((long) ControllerConsts.STATUS_STOPPING);
            when(ipcMessage.getMessageText())
                    .thenReturn(SACClientSocketHandler.COM_PORT_STATUS_CHANGE);
            sacPort.decodeIpcMessage(ipcMessage);
            sacPort.processIPCReceivedMessage();
            verify(messageService, never()).publishEvent(eq(SACPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_STOPPING]),
                    eq((long) ControllerConsts.STATUS_STOPPING), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + SACPort.class.getName()));

            // When stopped, nothing is published
            when(ipcMessage.getEventType())
                    .thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData())
                    .thenReturn((long) ControllerConsts.STATUS_STOPPED);
            when(ipcMessage.getMessageText())
                    .thenReturn(SACClientSocketHandler.COM_PORT_STATUS_CHANGE);
            sacPort.decodeIpcMessage(ipcMessage);
            sacPort.processIPCReceivedMessage();
            verify(messageService, never()).publishEvent(eq(SACPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_STOPPED]),
                    eq((long) ControllerConsts.STATUS_STOPPED), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + SACPort.class.getName()));

            // When error, error is published
            when(ipcMessage.getEventType())
                    .thenReturn(MessageEventConsts.COMM_EVENT_TYPE);
            when(ipcMessage.getMessageData())
                    .thenReturn((long) ControllerConsts.STATUS_ERROR);
            when(ipcMessage.getMessageText())
                    .thenReturn(SACClientSocketHandler.COM_PORT_STATUS_CHANGE);
            sacPort.decodeIpcMessage(ipcMessage);
            sacPort.processIPCReceivedMessage();
            verify(messageService, times(1)).publishEvent(eq(SACPort.class.getName()), any(),
                    eq(ControllerConsts.CONTROLLER_STATUS + " "
                            + ControllerConsts.STATUS_TEXT[ControllerConsts.STATUS_ERROR]),
                    eq((long) ControllerConsts.STATUS_ERROR), eq(MessageEventConsts.STATUS_EVENT_TYPE),
                    eq(MessageEventConsts.CONTROLLER_STATUS_EVENT_TYPE_TEXT + SACPort.class.getName()));
        }
    }

    @Test
    void shouldProcessReceivedEventWhenHostSendEventIsPublished() throws DBException {
        when(config.getString(eq(SACPort.COMM_TYPE))).thenReturn("SACHost");

        // Mocking for new SACClientSocketHandler(...) and new NamedThread(...)
        try (
                MockedConstruction<SACClientSocketHandler> mockedClientSocketHandler = Mockito.mockConstruction(
                        SACClientSocketHandler.class,
                        (clientSocketHandler, context) -> {
                            doNothing().when(clientSocketHandler).run();
                            doNothing().when(clientSocketHandler).shutdown();
                            when(clientSocketHandler.getTransactionHandler()).thenReturn(transactionHandler);
                        });
                MockedConstruction<NamedThread> mockedNamedThread = Mockito.mockConstruction(NamedThread.class,
                        (namedThread, context) -> {
                            doNothing().when(namedThread).start();
                        });
                MockedConstruction<Properties> mockedConnectionProperties = Mockito.mockConstruction(
                        Properties.class,
                        (connectionProperties, context) -> {
                            when(connectionProperties.put(any(), any())).thenReturn(object);
                        });) {

            // Mock Host message send event for stored complete message
            when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE);
            when(ipcMessage.getMessageText()).thenReturn(STORE_COMPLETE_MSG);

            // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
            // actual behaviour seems too difficult for now.
            SACPort sacPort = new SACPort(config);
            sacPort.setMessageService(messageService);
            sacPort.setLogger(logger);
            sacPort.initialize(SACPort.class.getName());
            sacPort.startup();

            // This is to mimic how event is received
            sacPort.decodeIpcMessage(ipcMessage);
            sacPort.processIPCReceivedMessage();

            // Should add the outbound message to the transaction handler
            ArgumentCaptor<String> outboundMessageCaptor = ArgumentCaptor.forClass(String.class);
            verify(transactionHandler, times(1)).addOutboundMessage(outboundMessageCaptor.capture());
            assertEquals(1, outboundMessageCaptor.getAllValues().size());
            assertEquals(STORE_COMPLETE_MSG, outboundMessageCaptor.getAllValues().get(0));
        }
    }
}
