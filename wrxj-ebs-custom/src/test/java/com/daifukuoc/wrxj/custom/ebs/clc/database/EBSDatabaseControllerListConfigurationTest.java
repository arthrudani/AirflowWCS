package com.daifukuoc.wrxj.custom.ebs.clc.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;

@ExtendWith(MockitoExtension.class)
class EBSDatabaseControllerListConfigurationTest {

    MockedStatic<Factory> mockedFactory;
    MockedStatic<DBObject> mockedDBObject;

    @Mock
    HostConfig hostConfig;

    @Mock
    StandardDeviceServer standardDeviceServer;

    @Mock
    StandardConfigurationServer standardConfigurationServer;

    EBSDatabaseControllerListConfiguration eBSDBCLC;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(HostConfig.class)).thenReturn(hostConfig);
        mockedFactory.when(() -> Factory.create(StandardDeviceServer.class)).thenReturn(standardDeviceServer);
        mockedFactory.when(() -> Factory.create(StandardConfigurationServer.class))
                .thenReturn(standardConfigurationServer);

        mockedDBObject = Mockito.mockStatic(DBObject.class);
        mockedDBObject.when(() -> DBObject.isWRxJConnectionActive()).thenReturn(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedDBObject.close();
    }
    
    // Prepare the list of handler types
    private static Stream<Arguments> handlers() {
        return Stream.of(
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_EXPECTED_RECEIPT_MESSAGE_HANDLER_TYPE),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_RETRIEVAL_ORDER_HANDLER_TYPE),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_FLIGHT_DATA_UPDATE_HANDLER_TYPE),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_PORT_TYPE),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_INTEGRATOR_TYPE),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_PLC_INTEGRATOR_TYPE),
                Arguments.of(EBSDatabaseControllerTypeDefinition.HOST_INVENTORY_REQUEST_BY_WAREHOUSE_HANDLER_TYPE)
                );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("handlers")
    void shouldHaveControllerConfiguredWhenExpectedReceiptHandlerIsConfiguredInHostConfig(String handler)
            throws DBException, ControllerConfigurationException {
        String[] controllerNames = new String[] { handler };
        when(hostConfig.getControllerNames()).thenReturn(controllerNames);

        Map<String, String> properties = new HashMap<>();
        when(hostConfig.getControllerConfigurations(handler)).thenReturn(properties);

        eBSDBCLC = new EBSDatabaseControllerListConfiguration(ControllerListConfiguration.CLC_SERVER);

        ControllerDefinition controllerDefinition = eBSDBCLC.getControllerDefinition(handler);
        assertEquals(handler,controllerDefinition.getName());

        ControllerTypeDefinition ControllerTypeDefinition = eBSDBCLC.getControllerTypeDefinition(handler);
        assertEquals(handler,ControllerTypeDefinition.getIdentifier());
    }
 }
