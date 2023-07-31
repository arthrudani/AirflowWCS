package com.daifukuamerica.wrxj.ipc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractIPCMessengerTest {
    private static final String EVENT_DELIMITER = "%";

    private static final String MESSENGER_NAME = MockIPCMessenger.class.getName();

    private static final String ARBITRARY_MESSAGE = "TEST"; // As we don't test with message, it's ok to use any string
    
    @Mock
    MessageService messageService;

    @Test
    void shouldPublishAnEventWhenPublishHostFlightDataUpdateEventMethodIsCalled() {
        MockIPCMessenger mockIPCMessenger = new MockIPCMessenger();
        mockIPCMessenger.setMessageService(messageService);
        mockIPCMessenger.setName(MESSENGER_NAME);
        mockIPCMessenger.publishHostFlightDataUpdateEvent(ARBITRARY_MESSAGE);

        verify(messageService).publishEvent(eq(MESSENGER_NAME), eq(AbstractIPCMessenger.WRX_RUN_MODE), eq(ARBITRARY_MESSAGE), eq(0L), eq(MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TYPE), eq(MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT));
    }
    
    @Test
    void shouldSubscribeToHostFlightDataUpdateEventWhenSubscribeHostFlightDataUpdateEventIsCalled() {
        MockIPCMessenger mockIPCMessenger = new MockIPCMessenger();
        mockIPCMessenger.setMessageService(messageService);
        mockIPCMessenger.setName(MESSENGER_NAME);        
        mockIPCMessenger.subscribeHostFlightDataUpdateEvent(EVENT_DELIMITER);

        verify(messageService).addSubscriber(eq(MessageEventConsts.HOST_FLIGHT_DATA_UPDATE_EVENT_TEXT + EVENT_DELIMITER), eq(MESSENGER_NAME));
    }
    
    @Test
    void shouldPublishAnEventWhenPublishHostRetrievalOrderEventMethodIsCalled() {
        MockIPCMessenger mockIPCMessenger = new MockIPCMessenger();
        mockIPCMessenger.setMessageService(messageService);
        mockIPCMessenger.setName(MESSENGER_NAME);
        mockIPCMessenger.publishHostRetrievalOrderEvent(ARBITRARY_MESSAGE);

        verify(messageService).publishEvent(eq(MESSENGER_NAME), eq(AbstractIPCMessenger.WRX_RUN_MODE), eq(ARBITRARY_MESSAGE), eq(0L), eq(MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TYPE), eq(MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TEXT));
    }
    
    @Test
    void shouldSubscribeToHostRetrievalOrderEventWhenSubscribeHostRetrievalOrderEventIsCalled() {
        MockIPCMessenger mockIPCMessenger = new MockIPCMessenger();
        mockIPCMessenger.setMessageService(messageService);
        mockIPCMessenger.setName(MESSENGER_NAME);        
        mockIPCMessenger.subscribeHostRetrievalOrderEvent(EVENT_DELIMITER);

        verify(messageService).addSubscriber(eq(MessageEventConsts.HOST_RETRIEVAL_ORDER_EVENT_TEXT + EVENT_DELIMITER), eq(MESSENGER_NAME));
    }

    // Internal concrete class to test AbstractIPCMessenger
    public class MockIPCMessenger extends AbstractIPCMessenger {        
    }
}
