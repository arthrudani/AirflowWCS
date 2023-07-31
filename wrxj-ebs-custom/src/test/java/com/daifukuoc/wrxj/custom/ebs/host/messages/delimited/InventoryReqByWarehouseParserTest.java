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
class InventoryReqByWarehouseParserTest {
   
    private static final String VALID_INV_REQ_BY_WH_MESSAGE = "23,0,59,0,0,0,0,0,11,EBS";

    MockedStatic<ThreadSystemGateway> mockedSystemGateway;
    
    @Mock
    SystemGateway systemGateway;
    
    @Mock
    HostToWrxData hostToWrxData;
    
    InventoryRequestByWarehouseParser parser;

    @BeforeEach
    void setUp() throws Exception {
        mockedSystemGateway = Mockito.mockStatic(ThreadSystemGateway.class);
        mockedSystemGateway.when(() -> ThreadSystemGateway.get()).thenReturn(systemGateway);
        
        parser = new InventoryRequestByWarehouseParser();
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
    void shouldPublishInventoryReqByWarehouseEventIfReceivedMessageIsValid() {
        when(hostToWrxData.getMessage()).thenReturn(VALID_INV_REQ_BY_WH_MESSAGE);
        
        try {
            parser.parse(hostToWrxData);
        }
        catch(Exception e) {
            fail("Shouldn't throw an exception");
        }
        
        verify(systemGateway, times(1)).publishHostInventoryReqByWarehouseEvent(hostToWrxData.getMessage());
    }
}
