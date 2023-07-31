package com.daifukuoc.wrxj.custom.ebs.host.messages.delimited;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;

@ExtendWith(MockitoExtension.class)
class FlightDataUpdateParserTest {
   
    private static final String VALID_FLIGHT_DATA_UPDATE_MESSAGE = "57,1000,57,2222,3,4,5,0,FL100,20221201001122,20221202112233,3700";

    MockedStatic<ThreadSystemGateway> mockedSystemGateway;
    
    @Mock
    SystemGateway systemGateway;
    
    @Mock
    HostToWrxData hostToWrxData;
    
    FlightDataUpdateParser parser;

    @BeforeEach
    void setUp() throws Exception {
        mockedSystemGateway = Mockito.mockStatic(ThreadSystemGateway.class);
        mockedSystemGateway.when(() -> ThreadSystemGateway.get()).thenReturn(systemGateway);
        
        parser = new FlightDataUpdateParser();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedSystemGateway.close();
    }

    @Test
    void shouldThrowInvalidHostDataExceptionIfReceivedMessageIsNull1() {
        assertThrows(InvalidHostDataException.class, () -> {
            parser.parse(null);
        });
    }
    
    @Test
    void shouldThrowInvalidHostDataExceptionIfReceivedMessageIsNull2() {
        when(hostToWrxData.getMessage()).thenReturn(null);
        
        assertThrows(InvalidHostDataException.class, () -> {
            parser.parse(hostToWrxData);
        });
    }

    @Test
    void shouldThrowInvalidHostDataExceptionIfReceivedMessageIsEmpty() {
        when(hostToWrxData.getMessage()).thenReturn(Strings.EMPTY);
        
        assertThrows(InvalidHostDataException.class, () -> {
            parser.parse(hostToWrxData);
        });
    }
    
    @Test
    void shouldPublishHostFlightDataUpdateEventIfReceivedMessageIsValid() {
        when(hostToWrxData.getMessage()).thenReturn(VALID_FLIGHT_DATA_UPDATE_MESSAGE);
        
        try {
            parser.parse(hostToWrxData);
        }
        catch(Exception e) {
            fail("Shouldn't throw an exception");
        }
        
        verify(systemGateway, times(1)).publishHostFlightDataUpdateEvent(hostToWrxData.getMessage());
    }
}
