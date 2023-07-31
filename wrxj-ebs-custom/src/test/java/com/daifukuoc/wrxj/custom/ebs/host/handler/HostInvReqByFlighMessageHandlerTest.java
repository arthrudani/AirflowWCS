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

import java.util.ArrayList;
import java.util.List;

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
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByFlightAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByFlightMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseItem;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryreqbyflight.InventoryReqByFlight;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.util.ConversionUtil;

@ExtendWith(MockitoExtension.class)
public class HostInvReqByFlighMessageHandlerTest {

    private static final String VALID_ORDER_ID = "111";
    private static final String VALID_FLIGHT_NUMBER = "FL100";
    private static final String VALID_FLIGHT_SCHEDULED_DATETIME = "20221231010101";
    private static final String VALID_TRAY_ID = "2222";
    private static final String VALID_ITEM_ID = "44444";
    private static final String VALID_FINAL_SORT_LOCATION = "1234";    
    private static final String VALID_SEQUENCE_NUMBER = "32767";
    private static final String VALID_HEADER = "42," + VALID_SEQUENCE_NUMBER + ",58,2222,3,4,5,0,";
    private static final String VALID_MSG_TO_RETRIEVE_ALL = VALID_HEADER + VALID_ORDER_ID + ","
            + VALID_FLIGHT_NUMBER + "," + VALID_FLIGHT_SCHEDULED_DATETIME;
  
	private static final Short NO_MISSING_BAGS = 0;
    private static final int INVALID_EVENT_TYPE = Integer.MAX_VALUE;
    private static final String INVALID_FLIGHT_SCHEDULED_DATETIME = "20221231999999";
    private static final String INVALID_MSG = VALID_HEADER + VALID_ORDER_ID + "," + VALID_FLIGHT_NUMBER + ","
            + INVALID_FLIGHT_SCHEDULED_DATETIME;
    
    MockedStatic<Factory> mockedFactory;
    MockedStatic<ProcessorFactory> mockedProcessorFactory;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    InventoryReqByFlight inventoryReqByFlight;

    @Mock
    EBSHostServer ebsHostServer;

    @Mock
    ReadOnlyProperties controllerConfigs;

    @Mock
    IpcMessage ipcMessage;

    InventoryResponseMessage inventoryResponseMessage;

    DelimitedFormatter delimitedFormatter;
    
    InventoryReqByFlightAckMessage ackMessage;

    InventoryReqByFlightMessageData inventoryReqByFlightMessageData = spy(new InventoryReqByFlightMessageData());

    @BeforeEach
    void setUp() throws Exception {
        delimitedFormatter = new DelimitedFormatter();
        inventoryResponseMessage = new InventoryResponseMessage(delimitedFormatter);
        ackMessage = new InventoryReqByFlightAckMessage(delimitedFormatter);

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
        mockedFactory.when(() -> Factory.create(InventoryReqByFlightMessageData.class)).thenReturn(inventoryReqByFlightMessageData);
        mockedFactory.when(() -> Factory.create(InventoryResponseMessage.class))
                .thenReturn(inventoryResponseMessage);
        mockedFactory.when(() -> Factory.create(EBSHostServer.class)).thenReturn(ebsHostServer);
        mockedFactory.when(() -> Factory.create(InventoryReqByFlightAckMessage.class)).thenReturn(ackMessage);

        mockedProcessorFactory = Mockito.mockStatic(ProcessorFactory.class);
        mockedProcessorFactory.when(() -> ProcessorFactory.get(any(String.class), eq(InventoryReqByFlight.NAME)))
                .thenReturn(inventoryReqByFlight);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedProcessorFactory.close();
    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        HostInventoryReqByFlightMessageHandler inventoryReqByFlightMessageHandler = new HostInventoryReqByFlightMessageHandler();
        try (MockedStatic<HostInventoryReqByFlightMessageHandler> mockedHostInventoryReqByFlightMessageHandler = Mockito
                .mockStatic(HostInventoryReqByFlightMessageHandler.class)) {
            mockedHostInventoryReqByFlightMessageHandler
                    .when(() -> HostInventoryReqByFlightMessageHandler.create(any(ReadOnlyProperties.class)))
                    .thenReturn(inventoryReqByFlightMessageHandler);

            try {
                HostInventoryReqByFlightMessageHandler controller = (HostInventoryReqByFlightMessageHandler) HostInventoryReqByFlightMessageHandler
                        .create(controllerConfigs);
                assertEquals(inventoryReqByFlightMessageHandler, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() {

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostInventoryReqByFlightMessageHandler hostInventoryRequestByFlightMessageHandler = new HostInventoryReqByFlightMessageHandler();
        hostInventoryRequestByFlightMessageHandler.setMessageService(messageService);
        hostInventoryRequestByFlightMessageHandler.setLogger(logger);
        hostInventoryRequestByFlightMessageHandler.initialize(HostInventoryReqByFlightMessageHandler.class.getName());
        hostInventoryRequestByFlightMessageHandler.startup();

        // The handler should subscribe to the following 2 events
        ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).addSubscriber(subscriptionSelectorCaptor.capture(), any(String.class));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.CONTROLLER_REQUEST_EVENT_TEXT
                        + MessageEventConsts.SUB_EVENT_TEXT + HostInventoryReqByFlightMessageHandler.class.getName())));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.HOST_INVENTORY_REQUEST_EVENT_TEXT + "%")));
    }

    @Test
    void shouldIgnoreTheEventWhenTheWrongEventIsReceived() throws InventoryRequestFailureException, DBException {

        when(ipcMessage.getEventType()).thenReturn(INVALID_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG_TO_RETRIEVE_ALL);

        HostInventoryReqByFlightMessageHandler hostInventoryRequestByFlightMessageHandler = new HostInventoryReqByFlightMessageHandler();
        hostInventoryRequestByFlightMessageHandler.setMessageService(messageService);
        hostInventoryRequestByFlightMessageHandler.setLogger(logger);
        hostInventoryRequestByFlightMessageHandler.initialize(HostInventoryReqByFlightMessageHandler.class.getName());
        hostInventoryRequestByFlightMessageHandler.startup();

        hostInventoryRequestByFlightMessageHandler.decodeIpcMessage(ipcMessage);
        hostInventoryRequestByFlightMessageHandler.processIPCReceivedMessage();

        verify(inventoryReqByFlightMessageData, never()).parse(any(String.class));

        verify(inventoryReqByFlight, never()).getResponseList(any());

        // Host message send event shouldn't be published
        verify(messageService, never()).publishEvent(anyString(), any(), anyString(), anyLong(),
                eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
    }

    @Test
    void shouldSendAResponseEvenWhenTheReceivedMessageIsInvalid() throws DBException, InventoryRequestFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_INVENTORY_REQUEST_BY_FLIGHT_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(INVALID_MSG);

        HostInventoryReqByFlightMessageHandler hostInventoryRequestByFlightMessageHandler = new HostInventoryReqByFlightMessageHandler();
        hostInventoryRequestByFlightMessageHandler.setMessageService(messageService);
        hostInventoryRequestByFlightMessageHandler.setLogger(logger);
        hostInventoryRequestByFlightMessageHandler.initialize(HostInventoryReqByFlightMessageHandler.class.getName());
        hostInventoryRequestByFlightMessageHandler.startup();

        hostInventoryRequestByFlightMessageHandler.decodeIpcMessage(ipcMessage);
        hostInventoryRequestByFlightMessageHandler.processIPCReceivedMessage();

        verify(inventoryReqByFlightMessageData, times(1)).parse(any(String.class));
        assertFalse(inventoryReqByFlightMessageData.parse(INVALID_MSG));

        verify(inventoryReqByFlight, never()).getResponseList(inventoryReqByFlightMessageData);

        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT_ACK.getValue() + ";"
        			+SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE+";1",
        			eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT.getValue() + ";"
                		+SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE+";1;"
                        + SACControlMessage.INVENTORY_RESPONSE_MSG_TYPE + ";" + VALID_ORDER_ID + ";"
                        + SACControlMessage.STATUS_FAILED + ";" + NO_MISSING_BAGS + ";",
                eventMessageTextCaptor.getAllValues().get(1));
    }
    
    @Test
    void shouldSendAInventoryOKResponseAfterProcessingTheReceivedMessageWhenTheHostInventoryByFlightEventIsReceived()
            throws DBException, InventoryRequestFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_INVENTORY_REQUEST_BY_FLIGHT_EVENT_TYPE);
        // Retrieve all
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG_TO_RETRIEVE_ALL);
        
        List<InventoryResponseItem> list = new ArrayList<>();
        InventoryResponseItem itemList = new InventoryResponseItem();
        itemList.setFlightNumber(VALID_FLIGHT_NUMBER);
        itemList.setFlightSTD(ConversionUtil.convertDateStringToDate(VALID_FLIGHT_SCHEDULED_DATETIME));
        itemList.setLineId(VALID_ITEM_ID);
        itemList.setLoadId(VALID_TRAY_ID);
        itemList.setLocationID(VALID_FINAL_SORT_LOCATION);
        itemList.setGlobalID(VALID_ORDER_ID);
        list.add(itemList);
        when(inventoryReqByFlight.getResponseList(inventoryReqByFlightMessageData)).thenReturn(list);

        HostInventoryReqByFlightMessageHandler hostInventoryRequestByFlightMessageHandler = new HostInventoryReqByFlightMessageHandler();
        hostInventoryRequestByFlightMessageHandler.setMessageService(messageService);
        hostInventoryRequestByFlightMessageHandler.setLogger(logger);
        hostInventoryRequestByFlightMessageHandler.initialize(HostInventoryReqByFlightMessageHandler.class.getName());
        hostInventoryRequestByFlightMessageHandler.startup();

        hostInventoryRequestByFlightMessageHandler.decodeIpcMessage(ipcMessage);
        hostInventoryRequestByFlightMessageHandler.processIPCReceivedMessage();

        verify(inventoryReqByFlightMessageData, times(1)).parse(any(String.class));

        verify(inventoryReqByFlight, times(1)).getResponseList(inventoryReqByFlightMessageData);

        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.INVENTORY_REQUEST_BY_FLIGHT_ACK.getValue() + ";"
        			+SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_ACK_MSG_TYPE+";0",
        			eventMessageTextCaptor.getAllValues().get(0));
    }


}
