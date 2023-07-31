package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

@ExtendWith(MockitoExtension.class)
class PLCFlightDataUpdateMessageTest {

    private static final String SEQUENCE_NUMBER = "0";
    private static final String FLIGHT = "FL100";
    private static final String FINAL_SORT_LOCATION = "1234";
    private static final String MESSAGE_TO_SEND = PLCConstants.PLC_FLIGHT_DATA_UPDATE_MSG_TYPE
            + PLCConstants.PLC_MESSAGE_DELIM + SEQUENCE_NUMBER + PLCConstants.PLC_MESSAGE_DELIM + FLIGHT
            + PLCConstants.PLC_MESSAGE_DELIM + FINAL_SORT_LOCATION;

    MockedStatic<Factory> mockedFactory;

    @Mock
    PLCFlightDataUpdateMessage plcFlightDataUpdateMessage;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(eq(PLCFlightDataUpdateMessage.class), anyString(), anyString()))
                .thenReturn(plcFlightDataUpdateMessage);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }

    @Test
    void shouldReturnCSVFormattedMessageWhenConstructSendMessagetoPlcIsCalled() {
        when(plcFlightDataUpdateMessage.getSerialNum()).thenReturn(SEQUENCE_NUMBER);
        when(plcFlightDataUpdateMessage.getLot()).thenReturn(FLIGHT);
        when(plcFlightDataUpdateMessage.getFinalSortLocation()).thenReturn(FINAL_SORT_LOCATION);
        when(plcFlightDataUpdateMessage.constructSendMessagetoPlc()).thenCallRealMethod();

        PLCFlightDataUpdateMessage test = Factory.create(PLCFlightDataUpdateMessage.class, FLIGHT, FINAL_SORT_LOCATION);
        assertEquals(MESSAGE_TO_SEND, test.constructSendMessagetoPlc());
    }

}
