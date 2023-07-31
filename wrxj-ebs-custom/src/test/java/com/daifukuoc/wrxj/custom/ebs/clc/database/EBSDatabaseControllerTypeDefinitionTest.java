package com.daifukuoc.wrxj.custom.ebs.clc.database;

import static org.mockito.Mockito.times;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuoc.wrxj.custom.ebs.host.communication.sac.SACIntegrator;
import com.daifukuoc.wrxj.custom.ebs.host.communication.sac.SACPort;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostExpectedReceiptMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostFlightDataUpdateMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostInvReqByWarehouseMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.host.handler.HostRetrievalOrderMessageHandler;
import com.daifukuoc.wrxj.custom.ebs.integration.HostPlcIntegrator;

@ExtendWith(MockitoExtension.class)
class EBSDatabaseControllerTypeDefinitionTest {
    
    MockedStatic<Factory> mockedFactory;
   
    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        // FIXEME At the moment, it's not possible to mock Factory.getImplementation() due to its wildcard return type.
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }
    
    // Prepare the list of handler types
    private static Stream<Arguments> handlers() {
        return Stream.of(
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_EXPECTED_RECEIPT_MESSAGE_HANDLER_TYPE, HostExpectedReceiptMessageHandler.class),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_RETRIEVAL_ORDER_HANDLER_TYPE, HostRetrievalOrderMessageHandler.class),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_FLIGHT_DATA_UPDATE_HANDLER_TYPE, HostFlightDataUpdateMessageHandler.class),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_PORT_TYPE, SACPort.class),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_INTEGRATOR_TYPE, SACIntegrator.class),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_PLC_INTEGRATOR_TYPE, HostPlcIntegrator.class),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_HANDLER_TYPE, HostInvReqByWarehouseMessageHandler.class)
                );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("handlers")
    void shouldCallFactoryGetImplementationWhenGetDefaultClassIsCalled(String handler, Class handlerClass) {
        EBSDatabaseControllerTypeDefinition.getDefaultClass(handler);
        mockedFactory.verify(() -> Factory.getImplementation(handlerClass), times(1));
    }
}
