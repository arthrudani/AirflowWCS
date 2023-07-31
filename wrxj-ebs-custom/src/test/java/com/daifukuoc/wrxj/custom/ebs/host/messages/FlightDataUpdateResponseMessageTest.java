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
class FlightDataUpdateResponseMessageTest {

    private static final int TOTAL_NUMBER_OF_FIELDS_INCLUDING_MESSAGE_ID = 2;
    private static final short STATUS = 1;

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
        FlightDataUpdateResponseMessage flightDataUpdateResponseMessage = new FlightDataUpdateResponseMessage();
        ColumnObject[] columnObjects = flightDataUpdateResponseMessage.getMessageFields();
        assertEquals(TOTAL_NUMBER_OF_FIELDS_INCLUDING_MESSAGE_ID, columnObjects.length);
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(FlightDataUpdateResponseMessage.MSGID_NAME)));
        assertTrue(Arrays.stream(columnObjects)
                .anyMatch(co -> co.getColumnName().equals(FlightDataUpdateResponseMessage.STATUS_NAME)));
    }

    @Test
    void shouldSetValueInTheListOfColumnObjectWhenSetMethodIsCalled() {
        FlightDataUpdateResponseMessage flightDataUpdateResponseMessage = new FlightDataUpdateResponseMessage();

        flightDataUpdateResponseMessage.setStatus(STATUS);
        Optional<ColumnObject> statusColumnObject = Arrays
                .stream(flightDataUpdateResponseMessage.getMessageFields())
                .filter(co -> co.getColumnName().equals(FlightDataUpdateResponseMessage.STATUS_NAME)).findAny();
        assertTrue(statusColumnObject.isPresent());
        assertEquals(STATUS, statusColumnObject.get().getColumnValue());
    }
}
