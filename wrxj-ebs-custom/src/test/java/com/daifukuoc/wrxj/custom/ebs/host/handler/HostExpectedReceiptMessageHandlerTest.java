package com.daifukuoc.wrxj.custom.ebs.host.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.host.messages.delimited.DelimitedFormatter;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.EmptyLocationFinder;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.AlreadyStoredLoadException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.InvalidExpectedReceiptException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadCreationOrUpdateFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LoadSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationReservationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.LocationSearchingFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.NoRemainingEmptyLocationException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.POCreationFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.exception.StationSearchingFailureException;

@ExtendWith(MockitoExtension.class)
class HostExpectedReceiptMessageHandlerTest {

    private static final String VALID_SEQUENCE_NUMBER = "32767";
    private static final String VALID_HEADER = "84," + VALID_SEQUENCE_NUMBER + ",52,2222,3,4,5,0";
    private static final String VALID_ORDER_ID = "1111";
    private static final String VALID_TRAY_ID = "2222";
    private static final String VALID_GLOBAL_ID = "3333";
    private static final String VALID_ITEM_ID = "44444";
    private static final String VALID_FLIGHT_NUMBER = "FL100";
    private static final String VALID_FLIGHT_SCHEDULED_DATETIME = "20221201001122";
    private static final String VALID_DEFAULT_RETRIEVAL_DATETIME = "20221202112233";
    private static final String VALID_FINAL_SORT_LOCATION = "5555";
    private static final String VALID_ITEM_TYPE = "1";
    private static final String VALID_REQUEST_TYPE = "1";
    private static final String VALID_RESPONSE = VALID_ORDER_ID + MessageUtil.HOST_OUTBOUND_MESSAGE_DELIMITER
            + VALID_TRAY_ID + MessageUtil.HOST_OUTBOUND_MESSAGE_DELIMITER + VALID_GLOBAL_ID
            + MessageUtil.HOST_OUTBOUND_MESSAGE_DELIMITER + VALID_ITEM_ID;
    private static final String VALID_ACK = VALID_SEQUENCE_NUMBER + MessageUtil.HOST_OUTBOUND_MESSAGE_DELIMITER
            + MessageOutNames.EXPECTED_RECEIPT_ACK.getValue() + MessageUtil.HOST_OUTBOUND_MESSAGE_DELIMITER
            + SACControlMessage.EXPECTED_RECIEPT_ACK_MSG_TYPE + MessageUtil.HOST_OUTBOUND_MESSAGE_DELIMITER;
    private static final String ENTRANCE_STATION_ID = "1002";
    private static final String INVALID_FLIGHT_SCHEDULED_DATETIME = "20229999001122";
    private static final String INVALID_REQUEST_TYPE = String.valueOf(Integer.MAX_VALUE);
    private static final int INVALID_EVENT_TYPE = Integer.MAX_VALUE;

    MockedStatic<Factory> mockedFactory;
    MockedStatic<ProcessorFactory> mockedProcessorFactory;

    @Mock
    HostConfig hostConfig;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    EBSInventoryServer inventoryServer;

    @Mock
    EmptyLocationFinder emptyLocationFinder;

    @Mock
    ReadOnlyProperties controllerConfigs;

    @Mock
    IpcMessage ipcMessage;

    ExpectedReceiptAckMessage expectedReceiptAckMessage;
    DelimitedFormatter formatterForAck;

    ExpectedReceiptResponseMessage expectedReceiptResponseMessage;
    DelimitedFormatter formatterForResponse;

    ExpectedReceiptMessageData expectedReceiptMessageData = spy(new ExpectedReceiptMessageData());

    @BeforeEach
    void setUp() throws Exception {
        // Please note that formatter should be shared and each message needs its own formatter instance.
        formatterForAck = new DelimitedFormatter();
        expectedReceiptAckMessage = new ExpectedReceiptAckMessage(formatterForAck);
        formatterForResponse = new DelimitedFormatter();
        expectedReceiptResponseMessage = new ExpectedReceiptResponseMessage(formatterForResponse);

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
        mockedFactory.when(() -> Factory.create(EBSInventoryServer.class)).thenReturn(inventoryServer);
        mockedFactory.when(() -> Factory.create(HostConfig.class)).thenReturn(hostConfig);
        mockedFactory.when(() -> Factory.create(ExpectedReceiptMessageData.class))
                .thenReturn(expectedReceiptMessageData);
        mockedFactory.when(() -> Factory.create(ExpectedReceiptAckMessage.class))
                .thenReturn(expectedReceiptAckMessage);
        mockedFactory.when(() -> Factory.create(ExpectedReceiptResponseMessage.class))
                .thenReturn(expectedReceiptResponseMessage);

        mockedProcessorFactory = Mockito.mockStatic(ProcessorFactory.class);
        mockedProcessorFactory.when(() -> ProcessorFactory.get(any(String.class), eq(EmptyLocationFinder.NAME)))
                .thenReturn(emptyLocationFinder);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedProcessorFactory.close();
    }

    private String prepareMessageForTesting(String... fields) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(fields[i]);
        }

        return buf.toString();
    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandlerCreatedByFactory = new HostExpectedReceiptMessageHandler();
        try (MockedStatic<HostExpectedReceiptMessageHandler> mockedHostExpectedReceiptMessageHandler = Mockito
                .mockStatic(HostExpectedReceiptMessageHandler.class)) {
            mockedHostExpectedReceiptMessageHandler
                    .when(() -> HostExpectedReceiptMessageHandler.create(any(ReadOnlyProperties.class)))
                    .thenReturn(hostExpectedReceiptMessageHandlerCreatedByFactory);
            try {
                HostExpectedReceiptMessageHandler controller = (HostExpectedReceiptMessageHandler) HostExpectedReceiptMessageHandler
                        .create(controllerConfigs);
                assertEquals(hostExpectedReceiptMessageHandlerCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandler = new HostExpectedReceiptMessageHandler();
        hostExpectedReceiptMessageHandler.setMessageService(messageService);
        hostExpectedReceiptMessageHandler.setLogger(logger);
        hostExpectedReceiptMessageHandler.initialize(HostExpectedReceiptMessageHandler.class.getName());
        hostExpectedReceiptMessageHandler.startup();

        // The handler should subscribe to the following 2 events
        ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).addSubscriber(subscriptionSelectorCaptor.capture(), any(String.class));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.CONTROLLER_REQUEST_EVENT_TEXT
                        + MessageEventConsts.SUB_EVENT_TEXT + HostExpectedReceiptMessageHandler.class.getName())));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TEXT + "%")));
    }

    @Test
    void shouldIgnoreTheEventWhenTheWrongEventIsReceived() throws NoRemainingEmptyLocationException,
            LocationSearchingFailureException, InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException,
            DBException, LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
            LocationReservationFailureException, POCreationFailureException {
        when(ipcMessage.getEventType()).thenReturn(INVALID_EVENT_TYPE);
        String validMessage = prepareMessageForTesting(VALID_HEADER, VALID_ORDER_ID, VALID_TRAY_ID, VALID_GLOBAL_ID,
                VALID_ITEM_ID, VALID_FLIGHT_NUMBER, VALID_FLIGHT_SCHEDULED_DATETIME, VALID_DEFAULT_RETRIEVAL_DATETIME,
                VALID_FINAL_SORT_LOCATION, VALID_ITEM_TYPE, VALID_REQUEST_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(validMessage);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandler = new HostExpectedReceiptMessageHandler();
        hostExpectedReceiptMessageHandler.setMessageService(messageService);
        hostExpectedReceiptMessageHandler.setLogger(logger);
        hostExpectedReceiptMessageHandler.initialize(HostExpectedReceiptMessageHandler.class.getName());
        hostExpectedReceiptMessageHandler.startup();

        // This is to mimic how host expected receipt event is received
        hostExpectedReceiptMessageHandler.decodeIpcMessage(ipcMessage);
        hostExpectedReceiptMessageHandler.processIPCReceivedMessage();

        // ExpectedReceiptMessageData.parse() shouldn't be called
        verify(expectedReceiptMessageData, never()).parse(any(String.class));

        // The processor's find() shouldn't be called
        verify(emptyLocationFinder, never()).find(expectedReceiptMessageData);

        // Host message send event shouldn't be published
        verify(messageService, never()).publishEvent(anyString(), any(), anyString(), anyLong(),
                eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
    }

//    @Test
//    void shouldIgnoreTheEventWhenTheMessageWasAlreadyProcessed() throws NoRemainingEmptyLocationException,
//            LocationSearchingFailureException, InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException,
//            DBException, LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException, POCreationFailureException {
//        String validMessage = prepareMessageForTesting(VALID_HEADER, VALID_ORDER_ID, VALID_TRAY_ID, VALID_GLOBAL_ID,
//                VALID_ITEM_ID, VALID_FLIGHT_NUMBER, VALID_FLIGHT_SCHEDULED_DATETIME, VALID_DEFAULT_RETRIEVAL_DATETIME,
//                VALID_FINAL_SORT_LOCATION, VALID_ITEM_TYPE, VALID_REQUEST_TYPE);
//        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE);
//        when(ipcMessage.getMessageText()).thenReturn(validMessage);
//
//        List<Map> loadLineItemDataList = new ArrayList<>();
//        Map loadLineItemData = new HashMap();
//        loadLineItemData.put(LoadLineItemData.LOT_NAME, VALID_FLIGHT_NUMBER);
//        loadLineItemData.put(LoadLineItemData.LINEID_NAME, VALID_ITEM_ID);
//        loadLineItemDataList.add(loadLineItemData);
//
//        when(inventoryServer.getLoadLineItemDataListByLoadID(eq(VALID_TRAY_ID))).thenReturn(loadLineItemDataList);
//
//        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
//        // behaviour seems too difficult for now.
//        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandler = new HostExpectedReceiptMessageHandler();
//        hostExpectedReceiptMessageHandler.setMessageService(messageService);
//        hostExpectedReceiptMessageHandler.setLogger(logger);
//        hostExpectedReceiptMessageHandler.initialize(HostExpectedReceiptMessageHandler.class.getName());
//        hostExpectedReceiptMessageHandler.startup();
//
//        // This is to mimic how host expected receipt event is received
//        hostExpectedReceiptMessageHandler.decodeIpcMessage(ipcMessage);
//        hostExpectedReceiptMessageHandler.processIPCReceivedMessage();
//
//        // ExpectedReceiptMessageData.parse() should be called
//        verify(expectedReceiptMessageData, times(1)).parse(any(String.class));
//
//        // The processor's find() shouldn't be called
//        // - The message was already processed
//        verify(emptyLocationFinder, never()).find(expectedReceiptMessageData);
//
//        // Host message send event should be published for an ack message
//        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
//        verify(messageService, times(1)).publishEvent(anyString(), any(), messageCaptor.capture(), anyLong(),
//                eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
//        assertEquals(1, messageCaptor.getAllValues().size());
//        assertEquals(VALID_ACK + SACControlMessage.AckStatus.OK.getValue(), messageCaptor.getAllValues().get(0));
//    }

    @Test
    void shouldSendAResponseEvenWhenTheReceivedMessageIsInvalid()
            throws NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, DBException,
            LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
            LocationReservationFailureException, POCreationFailureException {
        String invalidMessage = prepareMessageForTesting(VALID_HEADER, VALID_ORDER_ID, VALID_TRAY_ID, VALID_GLOBAL_ID,
                VALID_ITEM_ID, VALID_FLIGHT_NUMBER, INVALID_FLIGHT_SCHEDULED_DATETIME, VALID_DEFAULT_RETRIEVAL_DATETIME,
                VALID_FINAL_SORT_LOCATION, VALID_ITEM_TYPE, VALID_REQUEST_TYPE);
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(invalidMessage);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandler = new HostExpectedReceiptMessageHandler();
        hostExpectedReceiptMessageHandler.setMessageService(messageService);
        hostExpectedReceiptMessageHandler.setLogger(logger);
        hostExpectedReceiptMessageHandler.initialize(HostExpectedReceiptMessageHandler.class.getName());
        hostExpectedReceiptMessageHandler.startup();

        // This is to mimic how host expected receipt event is received
        hostExpectedReceiptMessageHandler.decodeIpcMessage(ipcMessage);
        hostExpectedReceiptMessageHandler.processIPCReceivedMessage();

        // ExpectedReceiptMessageData.parse() should be called
        verify(expectedReceiptMessageData, times(1)).parse(any(String.class));

        // The processor's find() shouldn't be called
        verify(emptyLocationFinder, never()).find(any());

        // 2 new host send events for an ack and a response(error, no fields in body) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertEquals(2, eventMessageTextCaptor.getAllValues().size());
        assertEquals(VALID_ACK + SACControlMessage.AckStatus.MESSAGE_ERROR.getValue(),
                eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue() + ";"
                        + SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE + ";;;;;0;"
                        + SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR,
                eventMessageTextCaptor.getAllValues().get(1));
    }

    @Test
    void shouldSendAResponseWithAnErrorStatusWhenExpectedReceiptMessageHasInvalidRequestType()
            throws NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, DBException,
            LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
            LocationReservationFailureException, POCreationFailureException {
        String validMessage = prepareMessageForTesting(VALID_HEADER, VALID_ORDER_ID, VALID_TRAY_ID, VALID_GLOBAL_ID,
                VALID_ITEM_ID, VALID_FLIGHT_NUMBER, VALID_FLIGHT_SCHEDULED_DATETIME, VALID_DEFAULT_RETRIEVAL_DATETIME,
                VALID_FINAL_SORT_LOCATION, VALID_ITEM_TYPE, INVALID_REQUEST_TYPE);
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(validMessage);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandler = new HostExpectedReceiptMessageHandler();
        hostExpectedReceiptMessageHandler.setMessageService(messageService);
        hostExpectedReceiptMessageHandler.setLogger(logger);
        hostExpectedReceiptMessageHandler.initialize(HostExpectedReceiptMessageHandler.class.getName());
        hostExpectedReceiptMessageHandler.startup();

        // This is to mimic how host expected receipt event is received
        hostExpectedReceiptMessageHandler.decodeIpcMessage(ipcMessage);
        hostExpectedReceiptMessageHandler.processIPCReceivedMessage();

        // ExpectedReceiptMessageData.parse() should be called
        verify(expectedReceiptMessageData, times(1)).parse(any(String.class));

        // The processor's find() shouldn't be called
        verify(emptyLocationFinder, never()).find(any());

        // 2 new host send events for an ack and a response(error, no fields in body) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertEquals(2, eventMessageTextCaptor.getAllValues().size());
        assertEquals(VALID_ACK + SACControlMessage.AckStatus.MESSAGE_ERROR.getValue(),
                eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue() + ";"
                        + SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE + ";;;;;0;"
                        + SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR,
                eventMessageTextCaptor.getAllValues().get(1));
    }

    // Prepare the list of failures that could be thrown by EmptyLocationFinder
    private static Stream<Arguments> failuresList() {
        return Stream.of(
                Arguments.of("Failed to search a load", new LoadSearchingFailureException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR),
                Arguments.of("Already stored load", new AlreadyStoredLoadException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR),
                Arguments.of("Failed to search a station of location", new StationSearchingFailureException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR),
                Arguments.of("Failed to reserve a location", new LocationReservationFailureException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR),
                Arguments.of("Failed to create a new PO", new POCreationFailureException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR),
                Arguments.of("No remaining empty location", new NoRemainingEmptyLocationException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.NOT_AVAILABLE),
                Arguments.of("Location searching failed", new LocationSearchingFailureException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR),
                Arguments.of("Invalid expected receipt", new InvalidExpectedReceiptException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR),
                Arguments.of("Failed to create or update a load", new LoadCreationOrUpdateFailureException(),
                        SACControlMessage.ExpectedReceiptsResponse.STATUS.ERROR));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("failuresList")
    void shouldSendAResponseWithAnErrorOrANotAvailableStatusAfterProcessingTheReceivedMessageWhenEmptyLocationSearchingThrowsAnException(
            String test, Exception exception, int expectedStatusValue)
            throws NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException, LoadCreationOrUpdateFailureException, DBException,
            LoadSearchingFailureException, AlreadyStoredLoadException, StationSearchingFailureException,
            LocationReservationFailureException, POCreationFailureException {
        String validMessage = prepareMessageForTesting(VALID_HEADER, VALID_ORDER_ID, VALID_TRAY_ID, VALID_GLOBAL_ID,
                VALID_ITEM_ID, VALID_FLIGHT_NUMBER, VALID_FLIGHT_SCHEDULED_DATETIME, VALID_DEFAULT_RETRIEVAL_DATETIME,
                VALID_FINAL_SORT_LOCATION, VALID_ITEM_TYPE, VALID_REQUEST_TYPE);
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(validMessage);

        when(emptyLocationFinder.find(expectedReceiptMessageData)).thenThrow(exception);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandler = new HostExpectedReceiptMessageHandler();
        hostExpectedReceiptMessageHandler.setMessageService(messageService);
        hostExpectedReceiptMessageHandler.setLogger(logger);
        hostExpectedReceiptMessageHandler.initialize(HostExpectedReceiptMessageHandler.class.getName());
        hostExpectedReceiptMessageHandler.startup();

        // This is to mimic how host expected receipt event is received
        hostExpectedReceiptMessageHandler.decodeIpcMessage(ipcMessage);
        hostExpectedReceiptMessageHandler.processIPCReceivedMessage();

        // ExpectedReceiptMessageData.parse() should be called
        verify(expectedReceiptMessageData, times(1)).parse(any(String.class));

        // The processor's find() should be called with the received/parsed message
        verify(emptyLocationFinder, times(1)).find(expectedReceiptMessageData);

        // 2 new host send events for an ack and a response(error, fields in body) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertEquals(2, eventMessageTextCaptor.getAllValues().size());
        assertEquals(VALID_ACK + SACControlMessage.AckStatus.OK.getValue(),
                eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue() + ";"
                        + SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE + ";" + VALID_RESPONSE + ";0;"
                        + expectedStatusValue,
                eventMessageTextCaptor.getAllValues().get(1));
    }

    @Test
    void shouldSendAResponseWithASuccessfulStatusAfterProcessingTheReceivedMessageWhenAEmptyLocationIsFound()
            throws NoRemainingEmptyLocationException, LocationSearchingFailureException,
            InvalidExpectedReceiptException,
            LoadCreationOrUpdateFailureException, DBException, LoadSearchingFailureException,
            AlreadyStoredLoadException, StationSearchingFailureException, LocationReservationFailureException,
            POCreationFailureException {
        String validMessage = prepareMessageForTesting(VALID_HEADER, VALID_ORDER_ID, VALID_TRAY_ID, VALID_GLOBAL_ID,
                VALID_ITEM_ID, VALID_FLIGHT_NUMBER, VALID_FLIGHT_SCHEDULED_DATETIME, VALID_DEFAULT_RETRIEVAL_DATETIME,
                VALID_FINAL_SORT_LOCATION, VALID_ITEM_TYPE, VALID_REQUEST_TYPE);
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(validMessage);

        when(emptyLocationFinder.find(expectedReceiptMessageData)).thenReturn(ENTRANCE_STATION_ID);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostExpectedReceiptMessageHandler hostExpectedReceiptMessageHandler = new HostExpectedReceiptMessageHandler();
        hostExpectedReceiptMessageHandler.setMessageService(messageService);
        hostExpectedReceiptMessageHandler.setLogger(logger);
        hostExpectedReceiptMessageHandler.initialize(HostExpectedReceiptMessageHandler.class.getName());
        hostExpectedReceiptMessageHandler.startup();

        // This is to mimic how host expected receipt event is received
        hostExpectedReceiptMessageHandler.decodeIpcMessage(ipcMessage);
        hostExpectedReceiptMessageHandler.processIPCReceivedMessage();

        // ExpectedReceiptMessageData.parse() should be called
        verify(expectedReceiptMessageData, times(1)).parse(any(String.class));

        // The processor's find() should be called with the received/parsed message
        verify(emptyLocationFinder, times(1)).find(expectedReceiptMessageData);

        // 2 new host send events for an ack and a response(ok including entrance station id) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertEquals(2, eventMessageTextCaptor.getAllValues().size());
        assertEquals(VALID_ACK + SACControlMessage.AckStatus.OK.getValue(),
                eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue() + ";"
                        + SACControlMessage.EXPECTED_RECIEPT_RESPONSE_MSG_TYPE + ";" + VALID_RESPONSE + ";"
                        + ENTRANCE_STATION_ID + ";"
                        + SACControlMessage.ExpectedReceiptsResponse.STATUS.SUCCESS,
                eventMessageTextCaptor.getAllValues().get(1));
    }
}
