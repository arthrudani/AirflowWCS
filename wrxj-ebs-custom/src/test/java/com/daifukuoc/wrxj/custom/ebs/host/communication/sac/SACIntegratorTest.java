package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;

@ExtendWith(MockitoExtension.class)
class SACIntegratorTest {

    private static final String SEQUENCE_NUMBER = "32767";
    private static final String EXPECTED_RECEIPT_REQUEST_MSG = "84," + SEQUENCE_NUMBER
            + ",52,2222,3,4,5,0,999,1002,10021002,BAG1002,FL100,20221213000000,20221213000000,3600,1,1";
    private static final String RETRIEVAL_FLIGHT_MESSAGE = "42," + SEQUENCE_NUMBER
            + ",55,2222,3,4,5,0,111,222,20221212010101,0";
    private static final String FLIGHT_DATA_UPDATE_MESSAGE = "57," + SEQUENCE_NUMBER
            + ",57,2222,3,4,5,0,FL100,20221201001122,20221202112233,3700";
    private static final String INV_REQ_BY_WH_MESSAGE = "23," + SEQUENCE_NUMBER + ",59,0,0,0,0,0,11,EBS";

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
        SACIntegrator sacIntegratorCreatedByFactory = new SACIntegrator(config);
        try (MockedStatic<SACIntegrator> mockedSACIntegrator = Mockito.mockStatic(SACIntegrator.class)) {
            mockedSACIntegrator.when(() -> SACIntegrator.create(any(ReadOnlyProperties.class)))
                    .thenReturn(sacIntegratorCreatedByFactory);

            try {
                SACIntegrator controller = (SACIntegrator) SACIntegrator.create(config);
                assertEquals(sacIntegratorCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() throws DBException {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
        // actual behaviour seems too difficult for now.
        SACIntegrator sacIntegrator = new SACIntegrator(config);
        sacIntegrator.setMessageService(messageService);
        sacIntegrator.setLogger(logger);
        sacIntegrator.initialize(SACIntegrator.class.getName());
        sacIntegrator.startup();

        // The handler should subscribe to the following 3 events
        ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(3)).addSubscriber(subscriptionSelectorCaptor.capture(), any());
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE_TEXT)));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.contains(MessageEventConsts.REQUEST_EVENT_TYPE_TEXT)));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.contains(MessageEventConsts.HOST_MESG_RECV_TEXT)));
    }

    @Test
    void shouldProcessReceivedEventWhenExpectedReceiptIsPublished() throws DBException {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
        // actual behaviour seems too difficult for now.
        SACIntegrator sacIntegrator = new SACIntegrator(config);
        sacIntegrator.setMessageService(messageService);
        sacIntegrator.setLogger(logger);
        sacIntegrator.initialize(SACIntegrator.class.getName());
        sacIntegrator.startup();

        // This is to mimic how event is received
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(EXPECTED_RECEIPT_REQUEST_MSG);

        sacIntegrator.decodeIpcMessage(ipcMessage);
        sacIntegrator.processIPCReceivedMessage();

        ArgumentCaptor<String> publishedMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> publishedMessageTypeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(messageService, times(3)).publishEvent(anyString(), isNull(), publishedMessageTextCaptor.capture(),
                anyLong(), publishedMessageTypeCaptor.capture(), anyString());

        assertTrue(publishedMessageTextCaptor.getAllValues().stream()
                .anyMatch(message -> message.equals(EXPECTED_RECEIPT_REQUEST_MSG)));
        assertTrue(publishedMessageTypeCaptor.getAllValues().stream()
                .anyMatch(type -> type == MessageEventConsts.HOST_EXPECTED_RECEIPT_EVENT_TYPE));
    }

    @Test
    void shouldProcessReceivedEventWhenRetrievalOrderIsPublished() throws DBException {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
        // actual behaviour seems too difficult for now.
        SACIntegrator sacIntegrator = new SACIntegrator(config);
        sacIntegrator.setMessageService(messageService);
        sacIntegrator.setLogger(logger);
        sacIntegrator.initialize(SACIntegrator.class.getName());
        sacIntegrator.startup();

        // This is to mimic how event is received
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(RETRIEVAL_FLIGHT_MESSAGE);

        sacIntegrator.decodeIpcMessage(ipcMessage);
        sacIntegrator.processIPCReceivedMessage();

        ArgumentCaptor<String> publishedMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> publishedMessageTypeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(messageService, times(3)).publishEvent(anyString(), isNull(), publishedMessageTextCaptor.capture(),
                anyLong(), publishedMessageTypeCaptor.capture(), anyString());

        assertTrue(publishedMessageTextCaptor.getAllValues().stream()
                .anyMatch(message -> message.equals(RETRIEVAL_FLIGHT_MESSAGE)));
        assertTrue(publishedMessageTypeCaptor.getAllValues().stream()
                .anyMatch(type -> type == MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TYPE));
    }

    @Test
    void shouldProcessReceivedEventWhenFlightDataUpdateIsPublished() throws DBException {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
        // actual behaviour seems too difficult for now.
        SACIntegrator sacIntegrator = new SACIntegrator(config);
        sacIntegrator.setMessageService(messageService);
        sacIntegrator.setLogger(logger);
        sacIntegrator.initialize(SACIntegrator.class.getName());
        sacIntegrator.startup();

        // This is to mimic how event is received
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(FLIGHT_DATA_UPDATE_MESSAGE);

        sacIntegrator.decodeIpcMessage(ipcMessage);
        sacIntegrator.processIPCReceivedMessage();

        // 4 events published
        // - CONTROL_EVENT_TYPE = 0
        // - STATUS_EVENT_TYPE = 1
        // - HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE = 23
        // - HOST_TO_PLC_EVENT_TYPE = 25
        ArgumentCaptor<String> publishedMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> publishedMessageTypeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(messageService, times(4)).publishEvent(anyString(), isNull(), publishedMessageTextCaptor.capture(),
                anyLong(), publishedMessageTypeCaptor.capture(), anyString());

        assertEquals(publishedMessageTextCaptor.getAllValues().get(2), FLIGHT_DATA_UPDATE_MESSAGE);
        assertEquals(publishedMessageTypeCaptor.getAllValues().get(2),  MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE);
        assertEquals(publishedMessageTextCaptor.getAllValues().get(3), FLIGHT_DATA_UPDATE_MESSAGE);
        assertEquals(publishedMessageTypeCaptor.getAllValues().get(3),  MessageEventConsts.HOST_TO_PLC_EVENT_TYPE);
    }
    
    @Test
    void shouldProcessReceivedEventWhenInvReqByWHIsPublished() throws DBException {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
        // actual behaviour seems too difficult for now.
        SACIntegrator sacIntegrator = new SACIntegrator(config);
        sacIntegrator.setMessageService(messageService);
        sacIntegrator.setLogger(logger);
        sacIntegrator.initialize(SACIntegrator.class.getName());
        sacIntegrator.startup();

        // This is to mimic how event is received
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_MESG_RECV_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(INV_REQ_BY_WH_MESSAGE);

        sacIntegrator.decodeIpcMessage(ipcMessage);
        sacIntegrator.processIPCReceivedMessage();

        ArgumentCaptor<String> publishedMessageTextCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> publishedMessageTypeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(messageService, times(3)).publishEvent(anyString(), isNull(), publishedMessageTextCaptor.capture(),
                anyLong(), publishedMessageTypeCaptor.capture(), anyString());

        assertTrue(publishedMessageTextCaptor.getAllValues().stream()
                .anyMatch(message -> message.equals(INV_REQ_BY_WH_MESSAGE)));
        assertTrue(publishedMessageTypeCaptor.getAllValues().stream()
                .anyMatch(type -> type == MessageEventConsts.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_EVENT_TYPE));
    }
}
