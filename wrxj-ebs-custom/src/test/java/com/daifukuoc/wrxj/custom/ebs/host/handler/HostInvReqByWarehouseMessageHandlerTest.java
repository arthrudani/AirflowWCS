package com.daifukuoc.wrxj.custom.ebs.host.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByWarehouseAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryReqByWarehouseMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.InventoryResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryrequest.exception.InventoryRequestFailureException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.invreqbywarehouse.InvReqByWarehouseProcessor;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;

@ExtendWith(MockitoExtension.class)
public class HostInvReqByWarehouseMessageHandlerTest {

    private static final String VALID_REQUEST_ID = "111";
    private static final String VALID_WAREHOUSE_ID = "EBS";
    private static final String VALID_SEQUENCE_NUMBER = "32767";
    private static final String VALID_HEADER = "42," + VALID_SEQUENCE_NUMBER + ",55,2222,3,4,5,0,";
    private static final String VALID_MSG_INVENTORY_REQUEST_BY_WAREHOUSE = VALID_HEADER + VALID_REQUEST_ID + ","
            + VALID_WAREHOUSE_ID;
    private static final int INVALID_EVENT_TYPE = Integer.MAX_VALUE;
    private static final String INVALID_REQUEST_ID = "20";
    private static final String INVALID_MSG = VALID_HEADER + INVALID_REQUEST_ID + "," + VALID_WAREHOUSE_ID;

    MockedStatic<Factory> mockedFactory;
    MockedStatic<ProcessorFactory> mockedProcessorFactory;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    InvReqByWarehouseProcessor invReqByWarehouseProcessor;

    @Mock
    EBSHostServer ebsHostServer;

    @Mock
    ReadOnlyProperties controllerConfigs;

    @Mock
    IpcMessage ipcMessage;


    DelimitedFormatter delimitedFormatter;
    
    InventoryReqByWarehouseAckMessage ackMessage;

    InventoryReqByWarehouseMessageData inventoryReqByWarehouseMessageData = spy(new InventoryReqByWarehouseMessageData());
    
    InventoryResponseMessage inventoryResMessage;

    @BeforeEach
    void setUp() throws Exception {
        delimitedFormatter = new DelimitedFormatter();
        ackMessage = new InventoryReqByWarehouseAckMessage(delimitedFormatter);
        
        inventoryResMessage = new InventoryResponseMessage(delimitedFormatter);

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
        mockedFactory.when(() -> Factory.create(InventoryReqByWarehouseMessageData.class)).thenReturn(inventoryReqByWarehouseMessageData);
        mockedFactory.when(() -> Factory.create(EBSHostServer.class)).thenReturn(ebsHostServer);
        mockedFactory.when(() -> Factory.create(InventoryResponseMessage.class)).thenReturn(inventoryResMessage);
        mockedFactory.when(() -> Factory.create(InventoryReqByWarehouseAckMessage.class)).thenReturn(ackMessage);

        mockedProcessorFactory = Mockito.mockStatic(ProcessorFactory.class);
        mockedProcessorFactory.when(() -> ProcessorFactory.get(any(String.class), eq(InvReqByWarehouseProcessor.NAME)))
                .thenReturn(invReqByWarehouseProcessor);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedProcessorFactory.close();
    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        HostInvReqByWarehouseMessageHandler hostInvReqByWarehouseMessageHandlerCreatedByFactory = new HostInvReqByWarehouseMessageHandler();
        try (MockedStatic<HostInvReqByWarehouseMessageHandler> mockedHostInvReqByWarehouseMessageHandler = Mockito
                .mockStatic(HostInvReqByWarehouseMessageHandler.class)) {
            mockedHostInvReqByWarehouseMessageHandler
                    .when(() -> HostInvReqByWarehouseMessageHandler.create(any(ReadOnlyProperties.class)))
                    .thenReturn(hostInvReqByWarehouseMessageHandlerCreatedByFactory);

            try {
                HostInvReqByWarehouseMessageHandler controller = (HostInvReqByWarehouseMessageHandler) HostInvReqByWarehouseMessageHandler
                        .create(controllerConfigs);
                assertEquals(hostInvReqByWarehouseMessageHandlerCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() {

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
    	HostInvReqByWarehouseMessageHandler hostInvReqByWarehouseMessageHandler = new HostInvReqByWarehouseMessageHandler();
        hostInvReqByWarehouseMessageHandler.setMessageService(messageService);
        hostInvReqByWarehouseMessageHandler.setLogger(logger);
        hostInvReqByWarehouseMessageHandler.initialize(HostInvReqByWarehouseMessageHandler.class.getName());
        hostInvReqByWarehouseMessageHandler.startup();

        // The handler should subscribe to the following 2 events
        ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).addSubscriber(subscriptionSelectorCaptor.capture(), any(String.class));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.CONTROLLER_REQUEST_EVENT_TEXT
                        + MessageEventConsts.SUB_EVENT_TEXT + HostInvReqByWarehouseMessageHandler.class.getName())));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TEXT + "%")));
    }

    @Test
    void shouldIgnoreTheEventWhenTheWrongEventIsReceived() {

        when(ipcMessage.getEventType()).thenReturn(INVALID_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG_INVENTORY_REQUEST_BY_WAREHOUSE);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostInvReqByWarehouseMessageHandler hostInvReqByWarehouseMessageHandler = new HostInvReqByWarehouseMessageHandler();
        hostInvReqByWarehouseMessageHandler.setMessageService(messageService);
        hostInvReqByWarehouseMessageHandler.setLogger(logger);
        hostInvReqByWarehouseMessageHandler.initialize(HostInvReqByWarehouseMessageHandler.class.getName());
        hostInvReqByWarehouseMessageHandler.startup();

        // This is to mimic how host inv req by warehouse event is received
        hostInvReqByWarehouseMessageHandler.decodeIpcMessage(ipcMessage);
        hostInvReqByWarehouseMessageHandler.processIPCReceivedMessage();

        // InventoryReqByWarehouseMessageData.parse() shouldn't be called
        verify(inventoryReqByWarehouseMessageData, never()).parse(any(String.class));

        // The processor's retrieve() shouldn't be called
        try {
			verify(invReqByWarehouseProcessor, never()).getResponseList(inventoryReqByWarehouseMessageData);
		} catch (InventoryRequestFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Host message send event shouldn't be published
        verify(messageService, never()).publishEvent(anyString(), any(), anyString(), anyLong(),
                eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
    }

    @Test
    void shouldSendAResponseEvenWhenTheReceivedMessageIsInvalid() throws DBException, RetrievalOrderFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(INVALID_MSG);
        when(inventoryReqByWarehouseMessageData.parse(any(String.class))).thenReturn(false);
        when(inventoryReqByWarehouseMessageData.getRequestID()).thenReturn(INVALID_REQUEST_ID);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostInvReqByWarehouseMessageHandler hostInvReqByWarehouseMessageHandler = new HostInvReqByWarehouseMessageHandler();
        hostInvReqByWarehouseMessageHandler.setMessageService(messageService);
        hostInvReqByWarehouseMessageHandler.setLogger(logger);
        hostInvReqByWarehouseMessageHandler.initialize(HostInvReqByWarehouseMessageHandler.class.getName());
        hostInvReqByWarehouseMessageHandler.startup();

        // This is to mimic how host flight data update event is received
        hostInvReqByWarehouseMessageHandler.decodeIpcMessage(ipcMessage);
        hostInvReqByWarehouseMessageHandler.processIPCReceivedMessage();

        // RetrievalOrderMessageData.parse() should be called
        verify(inventoryReqByWarehouseMessageData, times(1)).parse(any(String.class));
        assertFalse(inventoryReqByWarehouseMessageData.parse(INVALID_MSG));

        // The processor's retrieve() shouldn't be called
        try {
			verify(invReqByWarehouseProcessor, never()).getResponseList(inventoryReqByWarehouseMessageData);
		} catch (InventoryRequestFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // A new host send event with a response(failed) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals("0;" + MessageOutNames.INVENTORY_REQUEST_BY_WAREHOUSE_ACK.getValue() + ";"
        			+SACControlMessage.INVENTORY_REQUEST_BY_WAREHOUSE_ACK_MSG_TYPE+";1",
        			eventMessageTextCaptor.getAllValues().get(0));
    }

}
