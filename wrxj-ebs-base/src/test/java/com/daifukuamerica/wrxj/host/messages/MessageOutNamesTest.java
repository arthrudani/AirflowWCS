package com.daifukuamerica.wrxj.host.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageOutNamesTest {

    private static final int NUMBER_OF_OUT_MESSAGES = 17;
    
    @Test
    void shouldContainExpectedNumberOfEntries() {
        assertEquals(NUMBER_OF_OUT_MESSAGES, MessageOutNames.getNames().length);
        assertTrue(Arrays.stream(MessageOutNames.getNames()).anyMatch(name -> name.equals(MessageOutNames.EXPECTED_RECEIPT_COMPLETE.getValue())));
        assertTrue(Arrays.stream(MessageOutNames.getNames()).anyMatch(name -> name.equals(MessageOutNames.EXPECTED_RECEIPT_ACK.getValue())));
        assertTrue(Arrays.stream(MessageOutNames.getNames()).anyMatch(name -> name.equals(MessageOutNames.STORE_COMPLETE.getValue())));
        assertTrue(Arrays.stream(MessageOutNames.getNames()).anyMatch(name -> name.equals(MessageOutNames.ORDER_COMPLETE.getValue())));
        assertTrue(Arrays.stream(MessageOutNames.getNames()).anyMatch(name -> name.equals(MessageOutNames.FLIGHT_DATA_UPDATE.getValue())));
        assertTrue(Arrays.stream(MessageOutNames.getNames()).anyMatch(name -> name.equals(MessageOutNames.INVENTORY_UPDATE.getValue())));
    }
}
