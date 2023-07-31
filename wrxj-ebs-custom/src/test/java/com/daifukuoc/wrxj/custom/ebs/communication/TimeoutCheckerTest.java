package com.daifukuoc.wrxj.custom.ebs.communication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuoc.wrxj.custom.ebs.communication.TimeoutChecker;

@ExtendWith(MockitoExtension.class)
class TimeoutCheckerTest {

    private static final int WITHIN_TIMEOUT_1 = 10;
    private static final int WITHIN_TIMEOUT_2 = 15;
    private static final int TIMEOUT = 20;
    private static final int AFTER_TIMEOUT = 30;

    private static final LocalDateTime now = LocalDateTime.parse("2023-02-08T00:00:00");
    private static final LocalDateTime withinTimeout1 = now.plus(WITHIN_TIMEOUT_1, ChronoUnit.SECONDS);
    private static final LocalDateTime withinTimeout2 = now.plus(WITHIN_TIMEOUT_2, ChronoUnit.SECONDS);
    private static final LocalDateTime afterTimeout = now.plus(AFTER_TIMEOUT, ChronoUnit.SECONDS);

    MockedStatic<LocalDateTime> mockedLocalDateTime;

    TimeoutChecker timeoutChecker;

    @BeforeEach
    void setUp() throws Exception {
        mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class);

        timeoutChecker = new TimeoutChecker();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedLocalDateTime.close();
    }

    @Test
    void shouldReturnTrueWhenConnectedIsNotCalled() {
        mockedLocalDateTime.when(() -> LocalDateTime.now())
                .thenReturn(now)
                .thenReturn(withinTimeout1)
                .thenReturn(withinTimeout2);

        assertTrue(timeoutChecker.check());
        assertTrue(timeoutChecker.check());
        assertTrue(timeoutChecker.check());
    }

    @Test
    void shouldReturnFalseWhenReceivedIsNotCalledWithinTimeout() {
        mockedLocalDateTime.when(() -> LocalDateTime.now())
                .thenReturn(now)
                .thenReturn(withinTimeout1);

        timeoutChecker.started(TIMEOUT);
        assertFalse(timeoutChecker.check());
    }

    @Test
    void shouldReturnTrueWhenReceivedIsNotCalledAfterTimeout() {
        mockedLocalDateTime.when(() -> LocalDateTime.now())
                .thenReturn(now)
                .thenReturn(afterTimeout);

        timeoutChecker.started(TIMEOUT);
        assertTrue(timeoutChecker.check());
    }

    @Test
    void shouldReturnFalseWhenReceivedIsCalledWithinTimeout() {
        mockedLocalDateTime.when(() -> LocalDateTime.now())
                .thenReturn(now)
                .thenReturn(withinTimeout1)
                .thenReturn(withinTimeout2);

        timeoutChecker.started(TIMEOUT);
        timeoutChecker.ticked();
        assertFalse(timeoutChecker.check());
    }

    @Test
    void shouldReturnTrueWhenReceivedIsCalledAfterTimeout() {
        mockedLocalDateTime.when(() -> LocalDateTime.now())
                .thenReturn(now)
                .thenReturn(withinTimeout1)
                .thenReturn(afterTimeout);

        timeoutChecker.started(TIMEOUT);
        timeoutChecker.ticked();
        assertFalse(timeoutChecker.check());
    }
}
