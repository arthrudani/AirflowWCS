package com.daifukuoc.wrxj.custom.ebs.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.log.Logger;

@ExtendWith(MockitoExtension.class)
class EBSLoggerTest{
    @Mock
    Logger logger;
    
    private EBSLogger ebsLogger;
    
    @BeforeEach
    public void setUp() {
        ebsLogger = new EBSLogger(logger);
    }
    
    @AfterEach
    public void tearDown() {
        ebsLogger = null;
    }

    @Test
    void logDebug_returnsOutputMessageAfterCallingLogger() {
        doNothing().when(logger).logDebug("Log message - String = ABC, Integer = 123");
        assertEquals("Log message - String = ABC, Integer = 123", ebsLogger.logDebug("Log message - String = %s, Integer = %d", "ABC", 123));
        verify(logger, times(1)).logDebug("Log message - String = ABC, Integer = 123");
    }

    @Test
    void logError__returnsOutputMessageAfterCallingLogger() {
        doNothing().when(logger).logError("Log message - String = ABC, Integer = 123");
        assertEquals("Log message - String = ABC, Integer = 123", ebsLogger.logError("Log message - String = %s, Integer = %d", "ABC", 123));
        verify(logger, times(1)).logError("Log message - String = ABC, Integer = 123");
    }
    
    @Test
    void logException__returnsExceptionMessageAfterCallingLogger() {
        Exception ex = new Exception("Test Exception");
        doNothing().when(logger).logException(ex);
        assertEquals("Test Exception", ebsLogger.logException(ex));
        verify(logger, times(1)).logException(ex);
    }

}
