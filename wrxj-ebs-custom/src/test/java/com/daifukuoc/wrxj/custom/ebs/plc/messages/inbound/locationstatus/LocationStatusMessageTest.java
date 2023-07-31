package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants.MSG_ACKNOWLEDGEMENT_STATUS;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.LocationStatusContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.LocationStatusMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.PLCLocationStatusTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.PLCLocationStatusTransaction.LocationStatus;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;

@ExtendWith(MockitoExtension.class)
class LocationStatusMessageTest extends LocationStatusMessage {

    MockedStatic<Factory> mockedFactory;
    private LocationStatusMessage plcLocationStatusMessage;

    @Mock
    PLCLocationStatusTransaction plcLocationStatusTransaction;

    @Mock
    PLCStandardAckMessage plcStandardAckMessage;

    @BeforeEach
    public void setUp() {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(PLCLocationStatusTransaction.class))
                .thenReturn(plcLocationStatusTransaction);
        mockedFactory.when(() -> Factory.create(PLCStandardAckMessage.class)).thenReturn(plcStandardAckMessage);
        plcLocationStatusMessage = new LocationStatusMessage();
    }

    @AfterEach
    public void tearDown() {
        plcLocationStatusMessage = null;
        mockedFactory.close();
    }

    @Test
    void parse_ParsesString() {
        String message = "60,0,123,12,1234567890,34,111,56";
        plcLocationStatusMessage.parse(message);

        List<LocationStatus> locationStatusList = plcLocationStatusMessage.getLocationStatusList();

        assertEquals(3, locationStatusList.size());
        assertEquals("0000000123", locationStatusList.get(0).getsAddress());
        assertEquals(12, locationStatusList.get(0).getNewStatus());
        assertEquals("1234567890", locationStatusList.get(1).getsAddress());
        assertEquals(34, locationStatusList.get(1).getNewStatus());
        assertEquals("0000000111", locationStatusList.get(2).getsAddress());
        assertEquals(56, locationStatusList.get(2).getNewStatus());
    }

    @ParameterizedTest
    @MethodSource("parse_returnsFalseToWrongInput_params")
    @NullAndEmptySource
    void parse_returnsFalseToWrongInput(String message) {
        assertFalse(plcLocationStatusMessage.parse(message));
    }

    private static Stream<Arguments> parse_returnsFalseToWrongInput_params() {
        return Stream.of(Arguments.of(","), Arguments.of("1,2"));
    }

    @Test
    public void parse_cancelsParsingWhenWrongDataIsFound() {
        assertFalse(plcLocationStatusMessage.parse("1,3,4,2,A"));
        assertTrue(plcLocationStatusMessage.getLocationStatusList().isEmpty());
    }

    @Test
    public void process_CheckBehavior_ConfirmInvokesPLCLocationTransation() throws DBException {
        List<LocationStatus> locationStatusList = Arrays.asList(new LocationStatus("ADDR", (byte) 123));
        plcLocationStatusMessage.setDeviceId("DEVICE");
        plcLocationStatusMessage.setLocationStatusList(locationStatusList);

        doNothing().when(plcLocationStatusTransaction).execute(new LocationStatusContext("DEVICE", locationStatusList));

        plcLocationStatusMessage.process();

        verify(plcLocationStatusTransaction, times(1)).execute(new LocationStatusContext("DEVICE", locationStatusList));

    }

    @Test
    void processAck_CheckBehavior_ConfirmCalleMethods() {
        doNothing().when(plcStandardAckMessage).setDeviceId("DEVICE");
        doNothing().when(plcStandardAckMessage).setSerialNum("001");
        doNothing().when(plcStandardAckMessage).setMessageType(PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE);
        doNothing().when(plcStandardAckMessage).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
        doNothing().when(plcStandardAckMessage).sendMessageToPlc();

        plcLocationStatusMessage.setDeviceId("DEVICE");
        plcLocationStatusMessage.setSerialNumber("001");
        plcLocationStatusMessage.processAck();

        verify(plcStandardAckMessage, times(1)).setDeviceId("DEVICE");
        verify(plcStandardAckMessage, times(1)).setSerialNum("001");
        verify(plcStandardAckMessage, times(1)).setMessageType(PLCConstants.PLC_LOCATION_STATUS_ACK_MSG_TYPE);
        verify(plcStandardAckMessage, times(1)).setStatus(String.valueOf(MSG_ACKNOWLEDGEMENT_STATUS.OK));
        verify(plcStandardAckMessage, times(1)).sendMessageToPlc();

    }

}
