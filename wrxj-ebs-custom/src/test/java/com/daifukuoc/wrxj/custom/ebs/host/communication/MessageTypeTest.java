package com.daifukuoc.wrxj.custom.ebs.host.communication;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageTypeTest {

    private static final int NUMBER_OF_MESSAGE_TYPES = 8;

    @Test
    void shouldContainExpectedNumberOfEntries() {
        assertEquals(NUMBER_OF_MESSAGE_TYPES, MessageType.values().length);
    }
}
