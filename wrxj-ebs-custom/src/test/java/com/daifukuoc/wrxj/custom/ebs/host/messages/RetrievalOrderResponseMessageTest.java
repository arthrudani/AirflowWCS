package com.daifukuoc.wrxj.custom.ebs.host.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageFormatter;
import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

@ExtendWith(MockitoExtension.class)
class RetrievalOrderResponseMessageTest {

    private static final int TOTAL_NUMBER_OF_FIELDS_INCLUDING_MESSAGE_ID = 4;
    private static final short ORDER_ID = 1234;
    private static final short STATUS = 1;
    private static final short NUMBER_OF_MISSING_BAGS = 0;

    MockedStatic<Factory> mockedFactory;
    MockedStatic<MessageFormatterFactory> mockedMessageFormatterFactory;

    @Mock
    MessageFormatter messageFormatter;

    @Mock
    HostConfig hostConfig;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(HostConfig.class)).thenReturn(hostConfig);

        mockedMessageFormatterFactory = Mockito.mockStatic(MessageFormatterFactory.class);
        mockedMessageFormatterFactory.when(() -> MessageFormatterFactory.getInstance()).thenReturn(messageFormatter);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedMessageFormatterFactory.close();
    }

    @Test
    void shouldContainTheListOfColumnObjectWhenCreated() {
        RetrievalOrderResponseMessage retrievalOrderResponseMessage = new RetrievalOrderResponseMessage();
        ColumnObject[] columnObjects = retrievalOrderResponseMessage.getMessageFields();
        assertEquals(TOTAL_NUMBER_OF_FIELDS_INCLUDING_MESSAGE_ID, columnObjects.length);
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(RetrievalOrderResponseMessage.MSGID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(RetrievalOrderResponseMessage.ORDERID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(RetrievalOrderResponseMessage.STATUS_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(RetrievalOrderResponseMessage.NUMBER_OF_MISSING_BAGS_NAME)));
        assertEquals(MessageOutNames.ORDER_COMPLETE.getValue(), retrievalOrderResponseMessage.getMessageIdentifier());
    }

    @Test
    void shouldSetValueInTheListOfColumnObjectWhenSetMethodIsCalled() {
        RetrievalOrderResponseMessage retrievalOrderResponseMessage = new RetrievalOrderResponseMessage();
                
        retrievalOrderResponseMessage.setOrderID(ORDER_ID);
        Optional<ColumnObject> orderIdColumnObject = Arrays.stream(retrievalOrderResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(RetrievalOrderResponseMessage.ORDERID_NAME)).findAny();
        assertTrue(orderIdColumnObject.isPresent());
        assertEquals(ORDER_ID, orderIdColumnObject.get().getColumnValue());
        
        retrievalOrderResponseMessage.setStatus(STATUS);
        Optional<ColumnObject> statusColumnObject = Arrays.stream(retrievalOrderResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(RetrievalOrderResponseMessage.STATUS_NAME)).findAny();
        assertTrue(statusColumnObject.isPresent());
        assertEquals(STATUS, statusColumnObject.get().getColumnValue());
        
        retrievalOrderResponseMessage.setNumberOfMissingBags(NUMBER_OF_MISSING_BAGS);
        Optional<ColumnObject> numberOfMissingBagsColumnObject = Arrays.stream(retrievalOrderResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(RetrievalOrderResponseMessage.NUMBER_OF_MISSING_BAGS_NAME)).findAny();
        assertTrue(numberOfMissingBagsColumnObject.isPresent());
        assertEquals(NUMBER_OF_MISSING_BAGS, numberOfMissingBagsColumnObject.get().getColumnValue());
    }
}
