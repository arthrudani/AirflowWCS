package com.daifukuamerica.wrxj.ipc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageEventConstsTest {

    private static final int NUMBER_OF_MESSAGE_EVENTS = 36	;

    @Test
    void shouldContainExpectedNumberOfEntries() {
        assertEquals(NUMBER_OF_MESSAGE_EVENTS, MessageEventConsts.UniqueSelectors.length);
    }
}
