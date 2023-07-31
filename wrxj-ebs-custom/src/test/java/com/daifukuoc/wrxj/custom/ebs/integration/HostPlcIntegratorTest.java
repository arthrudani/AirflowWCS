package com.daifukuoc.wrxj.custom.ebs.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.IpcMessage;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCFlightDataUpdateMessage;

@ExtendWith(MockitoExtension.class)
class HostPlcIntegratorTest {

    private static final String SEQUENCE_NUMBER = "32767";
    private static final String FLIGHT = "FL100";
    private static final String FINAL_SORT_LOCATION = "1234";
    private static final String FLIGHT_DATA_UPDATE_MESSAGE = "57," + SEQUENCE_NUMBER + ",57,2222,3,4,5,0," + FLIGHT
            + ",20221201001122,20221202112233," + FINAL_SORT_LOCATION;
    private static final String DEVICE_ID_TO_UPDATE = "9001";

    MockedStatic<Factory> mockedFactory;

    @Mock
    ReadOnlyProperties config;

    @Mock
    Properties connectionProperties;

    @Mock
    MessageService messageService;

    @Mock
    Logger logger;

    @Mock
    IpcMessage ipcMessage;
    
    @Mock
    EBSTableJoin tableJoin;

    @Mock
    PLCFlightDataUpdateMessage plcFlightDataUpdateMessage;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(MessageService.class)).thenReturn(messageService);
        mockedFactory.when(() -> Factory.create(EBSTableJoin.class)).thenReturn(tableJoin);
        mockedFactory.when(() -> Factory.create(eq(PLCFlightDataUpdateMessage.class), anyString(), anyString())).thenReturn(plcFlightDataUpdateMessage);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }

    @Test
    void shouldReturnAnObjectCreatedByFactoryWhenCreateIsCalled() {
        HostPlcIntegrator hostPlcIntegratorCreatedByFactory = new HostPlcIntegrator(config);
        try (MockedStatic<HostPlcIntegrator> mockedHostPlcIntegrator = Mockito.mockStatic(HostPlcIntegrator.class)) {
            mockedHostPlcIntegrator.when(() -> HostPlcIntegrator.create(any(ReadOnlyProperties.class)))
                    .thenReturn(hostPlcIntegratorCreatedByFactory);

            try {
                HostPlcIntegrator controller = (HostPlcIntegrator) HostPlcIntegrator.create(config);
                assertEquals(hostPlcIntegratorCreatedByFactory, controller);
            } catch (ControllerCreationException e) {
                fail("Shouldn't throw an exception");
            }
        }
    }

    @Test
    void shouldInitializeProperlyWhenInitializeAndThenStartUpIsCalled() throws DBException {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
        // actual behaviour seems too difficult for now.
        HostPlcIntegrator hostPlcIntegrator = new HostPlcIntegrator(config);
        hostPlcIntegrator.setMessageService(messageService);
        hostPlcIntegrator.setLogger(logger);
        hostPlcIntegrator.initialize(HostPlcIntegrator.class.getName());
        hostPlcIntegrator.startup();

        // The handler should subscribe to the following 4 events
        ArgumentCaptor<String> subscriptionSelectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(4)).addSubscriber(subscriptionSelectorCaptor.capture(), any());
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.equals(MessageEventConsts.HEARTBEAT_REQUEST_EVENT_TYPE_TEXT)));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.contains(MessageEventConsts.REQUEST_EVENT_TYPE_TEXT)));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.contains(MessageEventConsts.HOST_TO_PLC_EVENT_TEXT)));
        assertTrue(subscriptionSelectorCaptor.getAllValues().stream()
                .anyMatch(selector -> selector.contains(MessageEventConsts.PLC_TO_HOST_EVENT_TEXT)));
    }

    @Test
    void shouldPublishToPLCWithLoadsForTheUpdatedFlightWhenFlightDataUpdateIsPublishedFromHost() throws DBException {
        // This is just to mimic how ControllerFactory.startController() starts up controllers as mocking the
        // actual behaviour seems too difficult for now.
        HostPlcIntegrator hostPlcIntegrator = new HostPlcIntegrator(config);
        hostPlcIntegrator.setMessageService(messageService);
        hostPlcIntegrator.setLogger(logger);
        hostPlcIntegrator.initialize(HostPlcIntegrator.class.getName());
        hostPlcIntegrator.startup();

        // This is to mimic how event is received
        when(ipcMessage.getEventType()).thenReturn(MessageEventConsts.HOST_TO_PLC_EVENT_TYPE);
        when(ipcMessage.getMessageText()).thenReturn(FLIGHT_DATA_UPDATE_MESSAGE);

        List<String> devicesToUpdate = Collections.singletonList(DEVICE_ID_TO_UPDATE);
        when(tableJoin.getDevicesWithLoadsOfFlight(anyString(), eq(FLIGHT))).thenReturn(devicesToUpdate);
        
        hostPlcIntegrator.decodeIpcMessage(ipcMessage);
        hostPlcIntegrator.processIPCReceivedMessage();

        verify(tableJoin, times(1)).getDevicesWithLoadsOfFlight(anyString(), eq(FLIGHT));
        verify(plcFlightDataUpdateMessage, times(1)).setDeviceId(eq(DEVICE_ID_TO_UPDATE));
        verify(plcFlightDataUpdateMessage, times(1)).sendMessageToPlc();
    }
}
