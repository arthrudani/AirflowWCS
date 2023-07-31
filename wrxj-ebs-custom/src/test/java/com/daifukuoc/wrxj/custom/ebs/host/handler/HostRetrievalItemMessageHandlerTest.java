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
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalItemAckMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalItemResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.RetrievalOrderItemMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.processor.ProcessorFactory;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalitemorder.LoadItemRetriever;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.exception.RetrievalOrderFailureException;

@ExtendWith(MockitoExtension.class)
public class HostRetrievalItemMessageHandlerTest {

    private static final String VALID_ORDER_ID = "111";
    private static final String VALID_ARRAY_LENGTH = "1";
    private static final String VALID_TRAY_ID = "2222";
    private static final String VALID_GLOBAL_ID = "3333";
    private static final String VALID_ITEM_ID = "44444";
    private static final String VALID_FINAL_SORT_LOCATION = "1234";    
    private static final String VALID_NUMBER_OF_BAGS_TEN = "10";
    private static final String VALID_SEQUENCE_NUMBER = "32767";
    private static final String VALID_HEADER = "42," + VALID_SEQUENCE_NUMBER + ",54,2222,3,4,5,0,";
    private static final String VALID_MSG_TO_RETRIEVE_ALL = VALID_HEADER + VALID_ORDER_ID + ","
            + VALID_ARRAY_LENGTH + "," + VALID_TRAY_ID + "," + VALID_GLOBAL_ID + "," + VALID_ITEM_ID + "," + VALID_FINAL_SORT_LOCATION ;
	
	private static final Short NO_MISSING_BAGS = 0;
    private static final Short TEN_RETRIEVED_BAGS = Short.parseShort(VALID_NUMBER_OF_BAGS_TEN);
    private static final int INVALID_EVENT_TYPE = Integer.MAX_VALUE;
    private static final String INVALID_ARRAY_LENGTH = "0";
    private static final String INVALID_MSG = VALID_HEADER + VALID_ORDER_ID + ","
            + INVALID_ARRAY_LENGTH + "," + VALID_TRAY_ID + "," + VALID_GLOBAL_ID + "," + VALID_ITEM_ID + "," + VALID_FINAL_SORT_LOCATION ;
	
    MockedStatic<Factory> mockedFactory;
    MockedStatic<ProcessorFactory> mockedProcessorFactory;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    LoadItemRetriever loadRetriever;

    @Mock
    EBSHostServer ebsHostServer;

    @Mock
    ReadOnlyProperties controllerConfigs;

    @Mock
    IpcMessage ipcMessage;

    RetrievalItemResponseMessage retrievalItemResponseMessage;

    DelimitedFormatter delimitedFormatter;
    
    RetrievalItemAckMessage ackMessage;

    RetrievalOrderItemMessageData retrievalItemMessageData = spy(new RetrievalOrderItemMessageData());

    @BeforeEach
    void setUp() throws Exception {
        delimitedFormatter = new DelimitedFormatter();
        retrievalItemResponseMessage = new RetrievalItemResponseMessage(delimitedFormatter);
        ackMessage = new RetrievalItemAckMessage(delimitedFormatter);

        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
        mockedFactory.when(() -> Factory.create(RetrievalOrderItemMessageData.class)).thenReturn(retrievalItemMessageData);
        mockedFactory.when(() -> Factory.create(RetrievalItemResponseMessage.class))
                .thenReturn(retrievalItemResponseMessage);
        mockedFactory.when(() -> Factory.create(EBSHostServer.class)).thenReturn(ebsHostServer);
        mockedFactory.when(() -> Factory.create(RetrievalItemAckMessage.class)).thenReturn(ackMessage);

        mockedProcessorFactory = Mockito.mockStatic(ProcessorFactory.class);
        mockedProcessorFactory.when(() -> ProcessorFactory.get(any(String.class), eq(LoadItemRetriever.NAME)))
                .thenReturn(loadRetriever);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedProcessorFactory.close();
    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        HostRetrievalOrderMessageHandler hostRetrievalOrderMessageHandlerCreatedByFactory = new HostRetrievalOrderMessageHandler();
        try (MockedStatic<HostRetrievalOrderMessageHandler> mockedHostRetrievalOrderMessageHandler = Mockito
                .mockStatic(HostRetrievalOrderMessageHandler.class)) {
            mockedHostRetrievalOrderMessageHandler
                    .when(() -> HostRetrievalOrderMessageHandler.create(any(ReadOnlyProperties.class)))
                    .thenReturn(hostRetrievalOrderMessageHandlerCreatedByFactory);

            try {
                HostRetrievalOrderMessageHandler controller = (HostRetrievalOrderMessageHandler) HostRetrievalOrderMessageHandler
                        .create(controllerConfigs);
                assertEquals(hostRetrievalOrderMessageHandlerCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() {

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostRetrievalItemMessageHandler hostRetrievalItemMessageHandler = new HostRetrievalItemMessageHandler();
        hostRetrievalItemMessageHandler.setMessageService(messageService);
        hostRetrievalItemMessageHandler.setLogger(logger);
        hostRetrievalItemMessageHandler.initialize(HostRetrievalItemMessageHandler.class.getName());
        hostRetrievalItemMessageHandler.startup();

        // The handler should subscribe to the following 2 events
        ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).addSubscriber(subscriptionSelectorCaptor.capture(), any(String.class));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.CONTROLLER_REQUEST_EVENT_TEXT
                        + MessageEventConsts.SUB_EVENT_TEXT + HostRetrievalItemMessageHandler.class.getName())));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TEXT + "%")));
    }

    @Test
    void shouldIgnoreTheEventWhenTheWrongEventIsReceived() throws RetrievalOrderFailureException, DBException {

        when(ipcMessage.getEventType()).thenReturn(INVALID_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG_TO_RETRIEVE_ALL);

        HostRetrievalItemMessageHandler hostRetrievalItemMessageHandler = new HostRetrievalItemMessageHandler();
        hostRetrievalItemMessageHandler.setMessageService(messageService);
        hostRetrievalItemMessageHandler.setLogger(logger);
        hostRetrievalItemMessageHandler.initialize(HostRetrievalOrderMessageHandler.class.getName());
        hostRetrievalItemMessageHandler.startup();

        hostRetrievalItemMessageHandler.decodeIpcMessage(ipcMessage);
        hostRetrievalItemMessageHandler.processIPCReceivedMessage();

        // RetrievalItemMessageData.parse() shouldn't be called
        verify(retrievalItemMessageData, never()).parse(any(String.class));

        // The processor's retrieve() shouldn't be called
        verify(loadRetriever, never()).retrieve(any());

        // Host message send event shouldn't be published
        verify(messageService, never()).publishEvent(anyString(), any(), anyString(), anyLong(),
                eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
    }

    @Test
    void shouldSendAResponseEvenWhenTheReceivedMessageIsInvalid() throws DBException, RetrievalOrderFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(INVALID_MSG);

        HostRetrievalItemMessageHandler hostRetrievalItemMessageHandler = new HostRetrievalItemMessageHandler();
        hostRetrievalItemMessageHandler.setMessageService(messageService);
        hostRetrievalItemMessageHandler.setLogger(logger);
        hostRetrievalItemMessageHandler.initialize(HostRetrievalOrderMessageHandler.class.getName());
        hostRetrievalItemMessageHandler.startup();

        // This is to mimic how host flight data update event is received
        hostRetrievalItemMessageHandler.decodeIpcMessage(ipcMessage);
        hostRetrievalItemMessageHandler.processIPCReceivedMessage();

        // RetrievalOrderMessageData.parse() should be called
        verify(retrievalItemMessageData, times(1)).parse(any(String.class));
        assertFalse(retrievalItemMessageData.parse(INVALID_MSG));

        // The processor's retrieve() shouldn't be called
        verify(loadRetriever, never()).retrieve(retrievalItemMessageData);

        // A new host send event with a response(failed) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.RETRIEVAL_ITEM_REQUEST_ACK.getValue() + ";"
        			+SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE+";1",
        			eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.ITEMS_ORDER_COMPLETE.getValue() + ";"
                		+SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE+";1;"
                        + SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE + ";" + VALID_ORDER_ID + ";"
                        + SACControlMessage.STATUS_FAILED + ";" + NO_MISSING_BAGS + ";",
                eventMessageTextCaptor.getAllValues().get(1));
    }

    @Test
    void shouldSendARetrievalFailureResponseEvenWhenRetrievingLoadFailed()
            throws DBException, RetrievalOrderFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG_TO_RETRIEVE_ALL);
        doThrow(RetrievalOrderFailureException.class).when(loadRetriever).retrieve(retrievalItemMessageData);

        HostRetrievalItemMessageHandler hostRetrievalItemMessageHandler = new HostRetrievalItemMessageHandler();
        hostRetrievalItemMessageHandler.setMessageService(messageService);
        hostRetrievalItemMessageHandler.setLogger(logger);
        hostRetrievalItemMessageHandler.initialize(HostRetrievalOrderMessageHandler.class.getName());
        hostRetrievalItemMessageHandler.startup();

        hostRetrievalItemMessageHandler.decodeIpcMessage(ipcMessage);
        hostRetrievalItemMessageHandler.processIPCReceivedMessage();

        // RetrievalOrderMessageData.parse() should be called
        verify(retrievalItemMessageData, times(1)).parse(eq(VALID_MSG_TO_RETRIEVE_ALL));
        assertTrue(retrievalItemMessageData.parse(VALID_MSG_TO_RETRIEVE_ALL));

        // The processor's retrieve() should be called
        verify(loadRetriever, times(1)).retrieve(retrievalItemMessageData);

        // A new host send event with a response(failed) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.RETRIEVAL_ITEM_REQUEST_ACK.getValue() + ";"
        			+SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE+";0",
        			eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.ITEMS_ORDER_COMPLETE.getValue() + ";"
                		+SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE+";0;"
                        + SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE + ";" + VALID_ORDER_ID + ";"
                        + SACControlMessage.STATUS_FAILED + ";" + NO_MISSING_BAGS+ ";" ,
                eventMessageTextCaptor.getAllValues().get(1));

    }
    
    @Test
    void shouldSendARetrievalOKResponseAfterProcessingTheReceivedMessageWhenTheHostRetrievalItemEventIsReceivedAndAllRetrievalIsCompletedOK()
            throws DBException, RetrievalOrderFailureException {

        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_RETRIEVAL_ITEM_EVENT_TYPE);
        // Retrieve all
        when(ipcMessage.getMessageText()).thenReturn(VALID_MSG_TO_RETRIEVE_ALL);
        // 10 retrieved
        when(loadRetriever.retrieve(retrievalItemMessageData)).thenReturn(TEN_RETRIEVED_BAGS);

        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the actual
        // behaviour seems too difficult for now.
        HostRetrievalItemMessageHandler hostRetrievalItemMessageHandler = new HostRetrievalItemMessageHandler();
        hostRetrievalItemMessageHandler.setMessageService(messageService);
        hostRetrievalItemMessageHandler.setLogger(logger);
        hostRetrievalItemMessageHandler.initialize(HostRetrievalOrderMessageHandler.class.getName());
        hostRetrievalItemMessageHandler.startup();

        // This is to mimic how host flight data update event is received
        hostRetrievalItemMessageHandler.decodeIpcMessage(ipcMessage);
        hostRetrievalItemMessageHandler.processIPCReceivedMessage();

        // RetrievalOrderMessageData.parse() should be called
        verify(retrievalItemMessageData, times(1)).parse(any(String.class));

        // The processor's retrieve() should be called with the received/parsed message
        verify(loadRetriever, times(1)).retrieve(retrievalItemMessageData);

        // A new host send event with a response(ok) should be published
        ArgumentCaptor<String> eventMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).publishEvent(anyString(), any(), eventMessageTextCaptor.capture(),
                anyLong(), eq(MessageEventConsts.HOST_MESG_SEND_EVENT_TYPE), anyString());
        assertFalse(eventMessageTextCaptor.getAllValues().isEmpty());
        assertEquals(VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.RETRIEVAL_ITEM_REQUEST_ACK.getValue() + ";"
        			+SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE+";0",
        			eventMessageTextCaptor.getAllValues().get(0));
        assertEquals(
                VALID_SEQUENCE_NUMBER + ";" + MessageOutNames.ITEMS_ORDER_COMPLETE.getValue() + ";"
                		+SACControlMessage.RETRIEVAL_ITEM_REQUEST_ACK_MSG_TYPE+";0;"
                        + SACControlMessage.RETRIEVAL_ITEM_RESPONSE_MSG_TYPE + ";" + VALID_ORDER_ID + ";"
                        + SACControlMessage.STATUS_SUCCESS + ";" + NO_MISSING_BAGS+ ";" ,
                eventMessageTextCaptor.getAllValues().get(1));
    }


}
