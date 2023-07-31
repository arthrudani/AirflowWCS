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
class ExpectedReceiptResponseMessageTest {

    private static final int TOTAL_NUMBER_OF_FIELDS_INCLUDING_MESSAGE_ID = 7;
    private static final String ORDER_ID = "999";
    private static final String LOAD_ID = "1234";
    private static final String GLOBAL_ID = "1111";
    private static final String LINE_ID = "2222";
    private static final String ENTRANCE_STATION_ID = "3600";
    private static final int STATUS = 1;

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
        ExpectedReceiptResponseMessage expectedReceiptResponseMessage = new ExpectedReceiptResponseMessage();
        ColumnObject[] columnObjects = expectedReceiptResponseMessage.getMessageFields();
        assertEquals(TOTAL_NUMBER_OF_FIELDS_INCLUDING_MESSAGE_ID, columnObjects.length);
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.MSGID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.ORDER_ID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.LOAD_ID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.GLOBAL_ID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.LINE_ID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.ENTRANCE_STATION_ID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.STATUS_NAME)));
        assertEquals(MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue(),
                expectedReceiptResponseMessage.getMessageIdentifier());
    }

    @Test
    void shouldSetValueInTheListOfColumnObjectWhenSetMethodIsCalled() {
        ExpectedReceiptResponseMessage expectedReceiptResponseMessage = new ExpectedReceiptResponseMessage();

        expectedReceiptResponseMessage.setOrderId(ORDER_ID);
        Optional<ColumnObject> orderIdColumnObject = Arrays
                .stream(expectedReceiptResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.ORDER_ID_NAME)).findAny();
        assertTrue(orderIdColumnObject.isPresent());
        assertEquals(ORDER_ID, orderIdColumnObject.get().getColumnValue());
        
        expectedReceiptResponseMessage.setLoadId(LOAD_ID);
        Optional<ColumnObject> loadIdColumnObject = Arrays
                .stream(expectedReceiptResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.LOAD_ID_NAME)).findAny();
        assertTrue(orderIdColumnObject.isPresent());
        assertEquals(LOAD_ID, loadIdColumnObject.get().getColumnValue());
        
        expectedReceiptResponseMessage.setGlobalId(GLOBAL_ID);
        Optional<ColumnObject> globalIdColumnObject = Arrays
                .stream(expectedReceiptResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.GLOBAL_ID_NAME)).findAny();
        assertTrue(orderIdColumnObject.isPresent());
        assertEquals(GLOBAL_ID, globalIdColumnObject.get().getColumnValue());
        
        expectedReceiptResponseMessage.setLineId(LINE_ID);
        Optional<ColumnObject> lineIdColumnObject = Arrays
                .stream(expectedReceiptResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.LINE_ID_NAME)).findAny();
        assertTrue(orderIdColumnObject.isPresent());
        assertEquals(LINE_ID, lineIdColumnObject.get().getColumnValue());
        
        expectedReceiptResponseMessage.setEntranceStationId(ENTRANCE_STATION_ID);
        Optional<ColumnObject> entranceStationIdColumnObject = Arrays
                .stream(expectedReceiptResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.ENTRANCE_STATION_ID_NAME)).findAny();
        assertTrue(orderIdColumnObject.isPresent());
        assertEquals(ENTRANCE_STATION_ID, entranceStationIdColumnObject.get().getColumnValue());
        
        expectedReceiptResponseMessage.setStatus(STATUS);
        Optional<ColumnObject> statusColumnObject = Arrays
                .stream(expectedReceiptResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(ExpectedReceiptResponseMessage.STATUS_NAME)).findAny();
        assertTrue(orderIdColumnObject.isPresent());
        assertEquals(STATUS, statusColumnObject.get().getColumnValue());
    }
}
