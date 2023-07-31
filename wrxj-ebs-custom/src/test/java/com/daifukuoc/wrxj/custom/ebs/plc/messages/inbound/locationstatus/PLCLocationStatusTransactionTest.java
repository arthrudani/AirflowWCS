package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
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
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSDevice;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSStation;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSStationData;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.LocationStatusContext;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.PLCLocationStatusTransaction;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.PLCLocationStatusTransaction.LocationStatus;

@ExtendWith(MockitoExtension.class)
class PLCLocationStatusTransactionTest {

    MockedStatic<Factory> mockedFactory;

    @Mock
    EBSDevice device;

    @Mock
    EBSLocation location;

    @Mock
    EBSStation station;

    @Mock
    TransactionHistory transactionHistory;

    @Mock
    TransactionHistoryData transactionHistoryData;

    PLCLocationStatusTransaction plcLocationStatusTransaction;

    @BeforeEach
    public void setUp() {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(TransactionHistory.class)).thenReturn(transactionHistory);
        mockedFactory.when(() -> Factory.create(TransactionHistoryData.class)).thenReturn(transactionHistoryData);

        mockedFactory.when(() -> Factory.create(EBSDevice.class)).thenReturn(device);
        mockedFactory.when(() -> Factory.create(EBSLocation.class)).thenReturn(location);
        mockedFactory.when(() -> Factory.create(EBSStation.class)).thenReturn(station);

        plcLocationStatusTransaction = new PLCLocationStatusTransaction();
    }

    @AfterEach
    public void tearDown() {
        plcLocationStatusTransaction = null;
        device = null;
        location = null;
        mockedFactory.close();
    }

    @ParameterizedTest(name = "{index} : Status = {0}, healthy = {1}, empty = {2}, enabled = {3}")
    @MethodSource("LocationStatus_GivingNewStatus_ParsedCorrectly_Parameter")
    void LocationStatus_GivingNewStatus_ParsedCorrectly(byte newStatus, boolean healthy, boolean empty,
            boolean enabled) {
        LocationStatus locationStatus = new LocationStatus("Address", newStatus);
        assertEquals(newStatus, locationStatus.getNewStatus());
        assertEquals("Address", locationStatus.getsAddress());
    }

    private static Stream<Arguments> LocationStatus_GivingNewStatus_ParsedCorrectly_Parameter() {
        return Stream.of(Arguments.of((byte) 0, false, false, false), Arguments.of((byte) 1, false, false, true),
                Arguments.of((byte) 2, false, true, false), Arguments.of((byte) 3, false, true, true),
                Arguments.of((byte) 4, true, false, false), Arguments.of((byte) 5, true, false, true),
                Arguments.of((byte) 6, true, true, false), Arguments.of((byte) 7, true, true, true));
    }

    @Test
    void execute_CheckNormalCase() throws DBException {
        LocationStatus locationStatus1 = new LocationStatus("ADDRESS1", (byte) 0);
        LocationStatus locationStatus2 = new LocationStatus("ADDRESS2", (byte) 0);
        LocationStatus locationStatus3 = new LocationStatus("ADDRESS3", (byte) 0);
        List<LocationStatus> locationStatusList = Arrays.asList(locationStatus1, locationStatus2, locationStatus3);
        String deviceID = "9001";

        LocationData locationData1 = mock(LocationData.class);
        LocationData locationData2 = mock(LocationData.class);
        LocationData locationData3 = mock(LocationData.class);

        PLCLocationStatusTransaction spiedPlcLocationStatusTransaction = spy(plcLocationStatusTransaction);
        doNothing().when(spiedPlcLocationStatusTransaction).startLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).commitLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).endLocalTransaction();

        doReturn(locationData1).when(location).getLocation("ABC", "ADDRESS1");
        doReturn(locationData2).when(location).getLocation("ABC", "ADDRESS2");
        doReturn(locationData3).when(location).getLocation("ABC", "ADDRESS3");
        doNothing().when(spiedPlcLocationStatusTransaction).processLocationData(locationData1, "ABC", locationStatus1);
        doNothing().when(spiedPlcLocationStatusTransaction).processLocationData(locationData2, "ABC", locationStatus2);
        doNothing().when(spiedPlcLocationStatusTransaction).processLocationData(locationData3, "ABC", locationStatus3);

        doReturn("ABC").when(device).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);

        spiedPlcLocationStatusTransaction.execute(new LocationStatusContext(deviceID, locationStatusList));

        verify(spiedPlcLocationStatusTransaction, times(1)).startLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).commitLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).endLocalTransaction();

        verify(location, times(1)).getLocation("ABC", "ADDRESS1");
        verify(location, times(1)).getLocation("ABC", "ADDRESS2");
        verify(location, times(1)).getLocation("ABC", "ADDRESS3");
        verify(spiedPlcLocationStatusTransaction, times(1)).processLocationData(locationData1, "ABC", locationStatus1);
        verify(spiedPlcLocationStatusTransaction, times(1)).processLocationData(locationData2, "ABC", locationStatus2);
        verify(spiedPlcLocationStatusTransaction, times(1)).processLocationData(locationData3, "ABC", locationStatus3);

        verify(device, times(1)).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);
    }

    @Test
    void execute_DefaultWareHouseIsUsedWhenWarehouseIsNull() throws DBException {
        LocationStatus locationStatus1 = new LocationStatus("ADDRESS1", (byte) 0);
        LocationStatus locationStatus2 = new LocationStatus("ADDRESS2", (byte) 0);
        LocationStatus locationStatus3 = new LocationStatus("ADDRESS3", (byte) 0);
        List<LocationStatus> locationStatusList = Arrays.asList(locationStatus1, locationStatus2, locationStatus3);
        String deviceID = "9001";

        LocationData locationData1 = mock(LocationData.class);
        LocationData locationData2 = mock(LocationData.class);
        LocationData locationData3 = mock(LocationData.class);

        PLCLocationStatusTransaction spiedPlcLocationStatusTransaction = spy(plcLocationStatusTransaction);
        doNothing().when(spiedPlcLocationStatusTransaction).startLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).commitLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).endLocalTransaction();

        doReturn(locationData1).when(location).getLocation("EBS", "ADDRESS1");
        doReturn(locationData2).when(location).getLocation("EBS", "ADDRESS2");
        doReturn(locationData3).when(location).getLocation("EBS", "ADDRESS3");
        doNothing().when(spiedPlcLocationStatusTransaction).processLocationData(locationData1, "EBS", locationStatus1);
        doNothing().when(spiedPlcLocationStatusTransaction).processLocationData(locationData2, "EBS", locationStatus2);
        doNothing().when(spiedPlcLocationStatusTransaction).processLocationData(locationData3, "EBS", locationStatus3);

        doReturn(null).when(device).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);

        spiedPlcLocationStatusTransaction.execute(new LocationStatusContext(deviceID, locationStatusList));

        verify(spiedPlcLocationStatusTransaction, times(1)).startLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).commitLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).endLocalTransaction();

        verify(location, times(1)).getLocation("EBS", "ADDRESS1");
        verify(location, times(1)).getLocation("EBS", "ADDRESS2");
        verify(location, times(1)).getLocation("EBS", "ADDRESS3");
        verify(spiedPlcLocationStatusTransaction, times(1)).processLocationData(locationData1, "EBS", locationStatus1);
        verify(spiedPlcLocationStatusTransaction, times(1)).processLocationData(locationData2, "EBS", locationStatus2);
        verify(spiedPlcLocationStatusTransaction, times(1)).processLocationData(locationData3, "EBS", locationStatus3);

        verify(device, times(1)).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);
    }

    @Test
    void execute_StationIsUpdatedWhenLocationIsNotFound() throws DBException {
        LocationStatus locationStatus1 = new LocationStatus("ADDRESS1", (byte) 0);
        LocationStatus locationStatus2 = new LocationStatus("ADDRESS2", (byte) 0);
        LocationStatus locationStatus3 = new LocationStatus("ADDRESS3", (byte) 0);

        List<LocationStatus> locationStatusList = Arrays.asList(locationStatus1, locationStatus2, locationStatus3);
        String deviceID = "9001";

        EBSStationData ebsStationData1 = mock(EBSStationData.class);
        EBSStationData ebsStationData2 = mock(EBSStationData.class);
        EBSStationData ebsStationData3 = mock(EBSStationData.class);

        PLCLocationStatusTransaction spiedPlcLocationStatusTransaction = spy(plcLocationStatusTransaction);
        doNothing().when(spiedPlcLocationStatusTransaction).startLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).commitLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).endLocalTransaction();
        doReturn(null).when(location).getLocation("EBS", "ADDRESS1");
        doReturn(null).when(location).getLocation("EBS", "ADDRESS2");
        doReturn(null).when(location).getLocation("EBS", "ADDRESS3");
        doReturn(ebsStationData1).when(station).getStationData("ADDRESS1", "EBS");
        doReturn(ebsStationData2).when(station).getStationData("ADDRESS2", "EBS");
        doReturn(ebsStationData3).when(station).getStationData("ADDRESS3", "EBS");
        doNothing().when(spiedPlcLocationStatusTransaction).processStationData(ebsStationData1, "EBS", locationStatus1);
        doNothing().when(spiedPlcLocationStatusTransaction).processStationData(ebsStationData2, "EBS", locationStatus2);
        doNothing().when(spiedPlcLocationStatusTransaction).processStationData(ebsStationData3, "EBS", locationStatus3);

        doReturn("EBS").when(device).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);

        spiedPlcLocationStatusTransaction.execute(new LocationStatusContext(deviceID, locationStatusList));

        verify(spiedPlcLocationStatusTransaction, times(1)).startLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).commitLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).endLocalTransaction();
        verify(location, times(1)).getLocation("EBS", "ADDRESS1");
        verify(location, times(1)).getLocation("EBS", "ADDRESS2");
        verify(location, times(1)).getLocation("EBS", "ADDRESS3");
        verify(station, times(1)).getStationData("ADDRESS1", "EBS");
        verify(station, times(1)).getStationData("ADDRESS2", "EBS");
        verify(station, times(1)).getStationData("ADDRESS3", "EBS");
        verify(spiedPlcLocationStatusTransaction, times(1)).processStationData(ebsStationData1, "EBS", locationStatus1);
        verify(spiedPlcLocationStatusTransaction, times(1)).processStationData(ebsStationData2, "EBS", locationStatus2);
        verify(spiedPlcLocationStatusTransaction, times(1)).processStationData(ebsStationData3, "EBS", locationStatus3);

        verify(device, times(1)).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);
    }

    @Test
    void execute_DoNothingWhenStationDataIsNotFound() throws DBException {
        LocationStatus locationStatus1 = new LocationStatus("ADDRESS1", (byte) 0);
        LocationStatus locationStatus2 = new LocationStatus("ADDRESS2", (byte) 0);
        LocationStatus locationStatus3 = new LocationStatus("ADDRESS3", (byte) 0);

        List<LocationStatus> locationStatusList = Arrays.asList(locationStatus1, locationStatus2, locationStatus3);
        String deviceID = "9001";

        PLCLocationStatusTransaction spiedPlcLocationStatusTransaction = spy(plcLocationStatusTransaction);
        doNothing().when(spiedPlcLocationStatusTransaction).startLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).commitLocalTransaction();
        doNothing().when(spiedPlcLocationStatusTransaction).endLocalTransaction();
        doReturn(null).when(location).getLocation("EBS", "ADDRESS1");
        doReturn(null).when(location).getLocation("EBS", "ADDRESS2");
        doReturn(null).when(location).getLocation("EBS", "ADDRESS3");
        doReturn(null).when(station).getStationData("ADDRESS1", "EBS");
        doReturn(null).when(station).getStationData("ADDRESS2", "EBS");
        doReturn(null).when(station).getStationData("ADDRESS3", "EBS");

        doReturn("EBS").when(device).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);

        spiedPlcLocationStatusTransaction.execute(new LocationStatusContext(deviceID, locationStatusList));

        verify(spiedPlcLocationStatusTransaction, times(1)).startLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).commitLocalTransaction();
        verify(spiedPlcLocationStatusTransaction, times(1)).endLocalTransaction();
        verify(location, times(1)).getLocation("EBS", "ADDRESS1");
        verify(location, times(1)).getLocation("EBS", "ADDRESS2");
        verify(location, times(1)).getLocation("EBS", "ADDRESS3");
        verify(station, times(1)).getStationData("ADDRESS1", "EBS");
        verify(station, times(1)).getStationData("ADDRESS2", "EBS");
        verify(station, times(1)).getStationData("ADDRESS3", "EBS");

        verify(device, times(1)).getSingleColumnValue("9001", DeviceData.WAREHOUSE_NAME);
    }

    @Test
    void execute_ArgumentCheck() throws DBException {
        LocationStatus locationStatus1 = new LocationStatus("ADDRESS1", (byte) 0);
        LocationStatus locationStatus2 = new LocationStatus("ADDRESS2", (byte) 0);
        LocationStatus locationStatus3 = new LocationStatus("ADDRESS3", (byte) 0);

        List<LocationStatus> locationStatusList = Arrays.asList(locationStatus1, locationStatus2, locationStatus3);
        String deviceID = "9001";

        plcLocationStatusTransaction.executeBody(new LocationStatusContext(null, locationStatusList));
        plcLocationStatusTransaction.executeBody(new LocationStatusContext(deviceID, null));
        plcLocationStatusTransaction.executeBody(new LocationStatusContext(deviceID, new ArrayList<>()));

    }

    @ParameterizedTest
    @MethodSource("processLocationData_updatesStationStatus_Param")
    void processLocationData_updatesStationStatus(int currentLocationStatus, int nextLocationStatus, short statusFlag)
            throws DBException {
        LocationData locationData = mock(LocationData.class);
        doReturn(currentLocationStatus).when(locationData).getLocationStatus();
        doNothing().when(location).setLocationStatusValue("WAREHOUSE", "ADDRESS", nextLocationStatus);

        plcLocationStatusTransaction.processLocationData(locationData, "WAREHOUSE",
                new LocationStatus("ADDRESS", statusFlag));

        verify(locationData, times(1)).getLocationStatus();
        verify(location, times(1)).setLocationStatusValue("WAREHOUSE", "ADDRESS", nextLocationStatus);
    }

    private static Stream<Arguments> processLocationData_updatesStationStatus_Param() {
        return Stream.of(Arguments.of(DBConstants.LCAVAIL, DBConstants.LCUNAVAIL, (short) 1),
                Arguments.of(DBConstants.LCUNAVAIL, DBConstants.LCAVAIL, (short) 0),
                Arguments.of(DBConstants.LCPROHIBIT, DBConstants.LCUNAVAIL, (short) 1));
    }

    @ParameterizedTest
    @MethodSource("processLocationData_notUpdatesStationStatus_Param")
    void processLocationData_notUpdatesStationStatus(int currentLocationStatus, int nextLocationStatus,
            short statusFlag) throws DBException {
        LocationData locationData = mock(LocationData.class);
        doReturn(currentLocationStatus).when(locationData).getLocationStatus();

        plcLocationStatusTransaction.processLocationData(locationData, "WAREHOUSE",
                new LocationStatus("ADDRESS", statusFlag));

        verify(locationData, times(1)).getLocationStatus();
    }

    private static Stream<Arguments> processLocationData_notUpdatesStationStatus_Param() {
        return Stream.of(Arguments.of(DBConstants.LCUNAVAIL, DBConstants.LCUNAVAIL, (short) 0),
                Arguments.of(DBConstants.LCAVAIL, DBConstants.LCAVAIL, (short) 1),
                Arguments.of(DBConstants.LCPROHIBIT, DBConstants.LCAVAIL, (short) 1));
    }

    @ParameterizedTest
    @MethodSource("processStationData_updatesStationStatus_Param")
    void processStationData_updatesStationStatus(int currentStationStatus, int nextStationStatus, short statusFlag)
            throws DBException {
        EBSStationData stationData = mock(EBSStationData.class);
        doReturn(currentStationStatus).when(stationData).getStatus();
        doNothing().when(station).setStatus("ADDRESS", "WAREHOUSE", nextStationStatus);

        plcLocationStatusTransaction.processStationData(stationData, "WAREHOUSE",
                new LocationStatus("ADDRESS", statusFlag));

        verify(stationData, times(1)).getStatus();
        verify(station, times(1)).setStatus("ADDRESS", "WAREHOUSE", nextStationStatus);
    }

    private static Stream<Arguments> processStationData_updatesStationStatus_Param() {
        return Stream.of(Arguments.of(DBConstants.STORERETRIEVE, DBConstants.STNOFFLINE, (short) 1),
                Arguments.of(DBConstants.STNOFFLINE, DBConstants.STORERETRIEVE, (short) 0));
    }

    @ParameterizedTest
    @MethodSource("processStationData_notUpdatesStationStatus_Param")
    void processStationData_notUpdatesStationStatus(int currentStationStatus, int nextStationStatus, short statusFlag)
            throws DBException {
        EBSStationData stationData = mock(EBSStationData.class);
        doReturn(currentStationStatus).when(stationData).getStatus();

        plcLocationStatusTransaction.processStationData(stationData, "WAREHOUSE",
                new LocationStatus("ADDRESS", statusFlag));

        verify(stationData, times(1)).getStatus();
    }

    private static Stream<Arguments> processStationData_notUpdatesStationStatus_Param() {
        return Stream.of(Arguments.of(DBConstants.STNOFFLINE, DBConstants.STNOFFLINE, (short) 0),
                Arguments.of(DBConstants.STORERETRIEVE, DBConstants.STORERETRIEVE, (short) 1));
    }

}
