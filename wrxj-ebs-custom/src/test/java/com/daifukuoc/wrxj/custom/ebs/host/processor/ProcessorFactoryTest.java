package com.daifukuoc.wrxj.custom.ebs.host.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.AisleBasedEmptyLocationFinderImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.EmptyLocationFinder;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.ConveyorBasedEmptyLocationFinderImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.DefaultFlightDataUpdaterImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.FlightDataUpdater;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.AisleBasedFlightLoadRetrieverImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.LoadRetriever;

@ExtendWith(MockitoExtension.class)
class ProcessorFactoryTest {
    private static final String HOST_EXPECTED_RECEIPT_CONTROLLER_NAME = "HostExpectedReceiptMessageHandler";
    private static final String HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME = "HostFlightDataUpdateMessageHandler";
    private static final String HOST_RETRIEVAL_ORDER_CONTROLLER_NAME = "HostRetrievalOrderMessageHandler";
    private static final String INVALID_PROCESSOR_NAME = "XXXXXX";

    MockedStatic<Factory> mockedFactory;

    @Mock
    HostConfig hostConfig;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(HostConfig.class)).thenReturn(hostConfig);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();

        // Reset the cached objects which interrupts the tests
        ProcessorFactory.reset();
    }

    @Test
    void shouldThrowAnIllegalArgumentExceptionWhenArgumentIsNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(null, EmptyLocationFinder.NAME);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(null, LoadRetriever.NAME);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(null, FlightDataUpdater.NAME);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(Strings.EMPTY, EmptyLocationFinder.NAME);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(Strings.EMPTY, LoadRetriever.NAME);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(Strings.EMPTY, FlightDataUpdater.NAME);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ProcessorFactory.get(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, Strings.EMPTY);
        });
    }

    @Test
    void shouldReturnAisleBasedEmptyLocationFinderImplWhenProcessorIsNotConfiguredInHostConfig() throws DBException {

        when(hostConfig.getProcessorClassName(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME))
                .thenReturn(Strings.EMPTY);
        Processor processor = ProcessorFactory.get(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME);
        assertTrue(processor instanceof AisleBasedEmptyLocationFinderImpl);
    }

    @Test
    void shouldReturnAisleBasedEmptyLocationFinderImplWhenInvalidProcessorIsConfiguredInHostConfig()
            throws DBException {

        when(hostConfig.getProcessorClassName(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME))
                .thenReturn(INVALID_PROCESSOR_NAME);
        Processor processor = ProcessorFactory.get(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME);
        assertTrue(processor instanceof AisleBasedEmptyLocationFinderImpl);
    }

    @Test
    void shouldReturnAisleBasedEmptyLocationFinderImplWhenReadingHostConfigFailed() throws DBException {

        when(hostConfig.getProcessorClassName(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME))
                .thenThrow(new DBException());
        Processor processor = ProcessorFactory.get(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME);
        assertTrue(processor instanceof AisleBasedEmptyLocationFinderImpl);
    }

    @Test
    void shouldReturnAisleBasedEmptyLocationFinderImplWhenAisleBasedEmptyLocationFinderImplIsProperlyConfiguredInHostConfig()
            throws DBException {

        when(hostConfig.getProcessorClassName(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME))
                .thenReturn(AisleBasedEmptyLocationFinderImpl.class.getName());
        Processor processor = ProcessorFactory.get(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME);
        assertTrue(processor instanceof AisleBasedEmptyLocationFinderImpl);
    }

    @Test
    void shouldReturnTimeSlotBasedEmptyLocationFinderImplWhenTimeSlotBasedEmptyLocationFinderImplIsProperlyConfiguredInHostConfig()
            throws DBException {

        when(hostConfig.getProcessorClassName(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME))
                .thenReturn(ConveyorBasedEmptyLocationFinderImpl.class.getName());
        Processor processor = ProcessorFactory.get(HOST_EXPECTED_RECEIPT_CONTROLLER_NAME, EmptyLocationFinder.NAME);
        assertTrue(processor instanceof ConveyorBasedEmptyLocationFinderImpl);
    }
    
    @Test
    void shouldReturnAisleBasedFlightLoadRetrieverImplWhenProcessorIsNotConfiguredInHostConfig() throws DBException {

        when(hostConfig.getProcessorClassName(HOST_RETRIEVAL_ORDER_CONTROLLER_NAME, LoadRetriever.NAME))
                .thenReturn(Strings.EMPTY);
        Processor processor = ProcessorFactory.get(HOST_RETRIEVAL_ORDER_CONTROLLER_NAME, LoadRetriever.NAME);
        assertTrue(processor instanceof AisleBasedFlightLoadRetrieverImpl);
    }

    @Test
    void shouldReturnAisleBasedFlightLoadRetrieverImplWhenReadingHostConfigFailed() throws DBException {

        when(hostConfig.getProcessorClassName(HOST_RETRIEVAL_ORDER_CONTROLLER_NAME, LoadRetriever.NAME))
                .thenThrow(new DBException());
        Processor processor = ProcessorFactory.get(HOST_RETRIEVAL_ORDER_CONTROLLER_NAME, LoadRetriever.NAME);
        assertTrue(processor instanceof AisleBasedFlightLoadRetrieverImpl);
    }

    @Test
    void shouldReturnAisleBasedFlightLoadRetrieverImplWhenAisleBasedFlightLoadRetrieverImplIsProperlyConfigured()
            throws DBException {

        when(hostConfig.getProcessorClassName(HOST_RETRIEVAL_ORDER_CONTROLLER_NAME, LoadRetriever.NAME))
                .thenReturn(AisleBasedFlightLoadRetrieverImpl.class.getName());
        Processor processor = ProcessorFactory.get(HOST_RETRIEVAL_ORDER_CONTROLLER_NAME, LoadRetriever.NAME);
        assertTrue(processor instanceof AisleBasedFlightLoadRetrieverImpl);
    }

    @Test
    void shouldReturnDefaultFlightDataUpdaterImplWhenProcessorIsNotConfiguredInHostConfig() throws DBException {

        when(hostConfig.getProcessorClassName(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, FlightDataUpdater.NAME))
                .thenReturn(Strings.EMPTY);
        Processor processor = ProcessorFactory.get(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, FlightDataUpdater.NAME);
        assertTrue(processor instanceof DefaultFlightDataUpdaterImpl);
    }

    @Test
    void shouldReturnDefaultFlightDataUpdaterImplWhenReadingHostConfigFailed() throws DBException {

        when(hostConfig.getProcessorClassName(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, FlightDataUpdater.NAME))
                .thenThrow(new DBException());
        Processor processor = ProcessorFactory.get(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, FlightDataUpdater.NAME);
        assertTrue(processor instanceof DefaultFlightDataUpdaterImpl);
    }

    @Test
    void shouldReturnDefaultFlightDataUpdaterImplWhenDefaultFlightDataUpdaterImplIsProperlyConfigured()
            throws DBException {

        when(hostConfig.getProcessorClassName(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, FlightDataUpdater.NAME))
                .thenReturn(DefaultFlightDataUpdaterImpl.class.getName());
        Processor processor = ProcessorFactory.get(HOST_FLIGHT_DATA_UPDATE_CONTROLLER_NAME, FlightDataUpdater.NAME);
        assertTrue(processor instanceof DefaultFlightDataUpdaterImpl);
    }
}
