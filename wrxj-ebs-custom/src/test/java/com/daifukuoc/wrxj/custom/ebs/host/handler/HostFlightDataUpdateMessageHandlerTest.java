package com.daifukuoc.wrxj.custom.ebs.host.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.host.messages.delimited.DelimitedFormatter;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSHostServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.FlightDataUpdateResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.FlightDataUpdater;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.exception.FlightDataUpdateFailureException;

@ExtendWith(MockitoExtension.class)
public class HostFlightDataUpdateMessageHandlerTest {

    private static final String VALID_FLIGHT_NUMBER = "FL100";
    private static final String VALID_FLIGHT_SCHEDULED_DATETIME = "20221201001122";
    private static final String VALID_DEFAULT_RETRIEVAL_DATETIME = "20221202112233";
    private static final String VALID_FINAL_SORT_LOCATION = "1234";
    private static final String VALID_SEQUENCE_NUMBER = "32767";
    private static final String VALID_HEADER = "57," + VALID_SEQUENCE_NUMBER + ",57,2222,3,4,5,0,";
    private static final String VALID_MSG = VALID_HEADER + VALID_FLIGHT_NUMBER + ","
            + VALID_FLIGHT_SCHEDULED_DATETIME + "," + VALID_DEFAULT_RETRIEVAL_DATETIME + ","
            + VALID_FINAL_SORT_LOCATION;
    private static final int INVALID_EVENT_TYPE = Integer.MAX_VALUE;
    private static final String INVALID_FLIGHT_SCHEDULED_DATETIME = "20229999999999";
    private static final String INVALID_MSG = VALID_HEADER + VALID_FLIGHT_NUMBER + ","
            + INVALID_FLIGHT_SCHEDULED_DATETIME + "," + VALID_DEFAULT_RETRIEVAL_DATETIME + ","
            + VALID_FINAL_SORT_LOCATION;
    private static final String STATUS_VALUE = "0";

    MockedStatic<Factory> mockedFactory;
    MockedStatic<ProcessorFactory> mockedProcessorFactory;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    FlightDataUpdater flightDataUpdater;

    @Mock
    EBSHostServer ebsHostServer;

    @Mock
    ReadOnlyProperties controllerConfigs;

    @Mock
    IpcMessage ipcMessage;

    FlightDataUpdateResponseMessage flightDataUpdateResponseMessage;

    DelimitedFormatter delimitedFormatter;

    FlightDataUpdateMessageData flightDataUpdateMessageData = spy(new FlightDataUpdateMessageData());

    @BeforeEach
    void setUp() throws Exception {
        delimitedFormatter = new DelimitedFormatter();
        flightDataUpdateResponseMessage = new FlightDataUpdateResponseMessage(delimitedFormatter);

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
        mockedFactory.when(() -> Factory.create(FlightDataUpdateMessageData.class))
                .thenReturn(flightDataUpdateMessageData);
        mockedFactory.when(() -> Factory.create(FlightDataUpdateResponseMessage.class))
                .thenReturn(flightDataUpdateResponseMessage);
        mockedFactory.when(() -> Factory.create(EBSHostServer.class)).thenReturn(ebsHostServer);

        mockedProcessorFactory = Mockito.mockStatic(ProcessorFactory.class);
        mockedProcessorFactory.when(() -> ProcessorFactory.get(any(String.class), eq(FlightDataUpdater.NAME)))
                .thenReturn(flightDataUpdater);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedProcessorFactory.close();
    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        HostFlightDataUpdateMessageHandler hostFlightDataUpdateMessageHandlerCreatedByFactory = new HostFlightDataUpdateMessageHandler();
        try (MockedStatic<HostFlightDataUpdateMessageHandler> mockedHostFlightDataUpdateMessageHandler = Mockito
                .mockStatic(HostFlightDataUpdateMessageHandler.class)) {
            mockedHostFlightDataUpdateMessageHandler
                    .when(() -> HostFlightDataUpdateMessageHandler.create(any(ReadOnlyProperties.class)))
                    .thenReturn(hostFlightDataUpdateMessageHandlerCreatedByFactory);

            try {
                HostFlightDataUpdateMessageHandler controller = (HostFlightDataUpdateMessageHandler) HostFlightDataUpdateMessageHandler
                        .create(controllerConfigs);
                assertEquals(hostFlightDataUpdateMessageHandlerCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() {

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostFlightDataUpdateMessageHandler hostFlightDataUpdateMessageHandler = new HostFlightDataUpdateMessageHandler();
        hostFlightDataUpdateMessageHandler.setMessageService(messageService);
        hostFlightDataUpdateMessageHandler.setLogger(logger);
        hostFlightDataUpdateMessageHandler.initialize(HostFlightDataUpdateMessageHandler.class.getName());
        hostFlightDataUpdateMessageHandler.startup();

        // The handler should subscribe to the following 2 events
        ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).addSubscriber(subscriptionSelectorCaptor.capture(), any(String.class));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.CONTROLLER_REQUEST_EVENT_TEXT
                        + MessageEventConsts.SUB_EVENT_TEXT + HostFlightDataUpdateMessageHandler.class.getName())));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT + "%")));
    }

    @Test
    void shouldIgnoreTheEventWhenTheWrongEventIsReceived() throws FlightDataUpdateFailureException, DBException {

        when(ipcMessage.getEventType()).thenReturn(INVALID_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostFlightDataUpdateMessageHandler hostFlightDataUpdateMessageHandler = new HostFlightDataUpdateMessageHandler();
        hostFlightDataUpdateMessageHandler.setMessageService(messageService);
        hostFlightDataUpdateMessageHandler.setLogger(logger);
        hostFlightDataUpdateMessageHandler.initialize(HostFlightDataUpdateMessageHandler.class.getName());
        hostFlightDataUpdateMessageHandler.startup();

        // This is to mimic how host flight data update event is received
        hostFlightDataUpdateMessageHandler.decodeIpcMessage(ipcMessage);
        hostFlightDataUpdateMessageHandler.processIPCReceivedMessage();

        // FlightDataUpdateMessageData.parse() shouldn't be called
        verify(flightDataUpdateMessageData, never()).parse(any(String.class));

        // The processor's update() shouldn't be called
        verify(flightDataUpdater, never()).update(any());

        // Host message send event shouldn't be published
        verify(messageService, never()).publishEvent(anyString(), any(), anyString(), anyLong(),
                eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
    }

    @Test
    void shouldSendAResponseEvenWhenTheReceivedMessageIsInvalid() throws DBException, FlightDataUpdateFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(INVALID_MSG);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostFlightDataUpdateMessageHandler hostFlightDataUpdateMessageHandler = new HostFlightDataUpdateMessageHandler();
        hostFlightDataUpdateMessageHandler.setMessageService(messageService);
        hostFlightDataUpdateMessageHandler.setLogger(logger);
        hostFlightDataUpdateMessageHandler.initialize(HostFlightDataUpdateMessageHandler.class.getName());
        hostFlightDataUpdateMessageHandler.startup();

        // This is to mimic how host flight data update event is received
        hostFlightDataUpdateMessageHandler.decodeIpcMessage(ipcMessage);
        hostFlightDataUpdateMessageHandler.processIPCReceivedMessage();

        // FlightDataUpdateMessageData.parse() should be called
        verify(flightDataUpdateMessageData, times(1)).parse(any(String.class));
        assertFalse(flightDataUpdateMessageData.parse(INVALID_MSG));

        // The processor's update() shouldn't be called
        verify(flightDataUpdater, never()).update(flightDataUpdateMessageData);

        // A new host send event with a response(error) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(1)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.FLIGHT_DATA_UPDATE.getValue() + ";"
                        + SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE + ";1",
                eventMessageTextCaptor.getAllValues().get(0));
    }

    @Test
    void shouldSendAResponseEvenWhenUpdatingFlightDataFailed() throws DBException, FlightDataUpdateFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG);
        doThrow(FlightDataUpdateFailureException.class).when(flightDataUpdater).update(flightDataUpdateMessageData);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostFlightDataUpdateMessageHandler hostFlightDataUpdateMessageHandler = new HostFlightDataUpdateMessageHandler();
        hostFlightDataUpdateMessageHandler.setMessageService(messageService);
        hostFlightDataUpdateMessageHandler.setLogger(logger);
        hostFlightDataUpdateMessageHandler.initialize(HostFlightDataUpdateMessageHandler.class.getName());
        hostFlightDataUpdateMessageHandler.startup();

        // This is to mimic how host flight data update event is received
        hostFlightDataUpdateMessageHandler.decodeIpcMessage(ipcMessage);
        hostFlightDataUpdateMessageHandler.processIPCReceivedMessage();

        // FlightDataUpdateMessageData.parse() should be called
        verify(flightDataUpdateMessageData, times(1)).parse(eq(VALID_MSG));
        assertTrue(flightDataUpdateMessageData.parse(VALID_MSG));

        // The processor's update() should be called
        verify(flightDataUpdater, times(1)).update(flightDataUpdateMessageData);

        // A new host send event with a response(error) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(1)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.FLIGHT_DATA_UPDATE.getValue() + ";"
                        + SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE + ";1",
                eventMessageTextCaptor.getAllValues().get(0));
    }

    @Test
    void shouldSendAResponseAfterProcessingTheReceivedMessageWhenTheHostFlightDataUpdateEventIsReceived()
            throws DBException, FlightDataUpdateFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostFlightDataUpdateMessageHandler hostFlightDataUpdateMessageHandler = new HostFlightDataUpdateMessageHandler();
        hostFlightDataUpdateMessageHandler.setMessageService(messageService);
        hostFlightDataUpdateMessageHandler.setLogger(logger);
        hostFlightDataUpdateMessageHandler.initialize(HostFlightDataUpdateMessageHandler.class.getName());
        hostFlightDataUpdateMessageHandler.startup();

        // This is to mimic how host flight data update event is received
        hostFlightDataUpdateMessageHandler.decodeIpcMessage(ipcMessage);
        hostFlightDataUpdateMessageHandler.processIPCReceivedMessage();

        // FlightDataUpdateMessageData.parse() should be called
        verify(flightDataUpdateMessageData, times(1)).parse(any(String.class));

        // The processor's update() should be called with the received/parsed message
        verify(flightDataUpdater, times(1)).update(flightDataUpdateMessageData);

        // A new host send event with a response(error) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(1)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.FLIGHT_DATA_UPDATE.getValue() + ";"
                        + SACControlMessage.FLIGHT_DATA_UPDATE_ACK_MSG_TYPE + ";0",
                eventMessageTextCaptor.getAllValues().get(0));
    }
}
