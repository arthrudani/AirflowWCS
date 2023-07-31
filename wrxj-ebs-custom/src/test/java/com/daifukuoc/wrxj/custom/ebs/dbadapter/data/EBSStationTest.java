package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

@ExtendWith(MockitoExtension.class)
class EBSStationTest extends EBSStation {

    static MockedStatic<Factory> mockedFactory;

    @Mock
    EBSStationData mpEBSSTData;

    @Mock
    StationData stationData;

    EBSStation ebsStation;

    @BeforeAll
    static void setUpAll() {
        mockedFactory = Mockito.mockStatic(Factory.class);
    }

    @AfterAll
    static void tearDownAll() {
        mockedFactory.close();
    }

    @BeforeEach
    void setUp() {
        mockedFactory.when(() -> Factory.create(EBSStationData.class)).thenReturn(mpEBSSTData);
        mockedFactory.when(() -> Factory.create(StationData.class)).thenReturn(stationData);
        ebsStation = new EBSStation();
    }

    @AfterEach
    void tearDown() {
        ebsStation = null;
    }

    @Test
    void getStationData_TestBehavior_CheckInteraction() throws DBException {
        EBSStationData ebsStationData = new EBSStationData();
        EBSStation spyEBSStation = spy(ebsStation);
        doReturn(ebsStationData).when(spyEBSStation).getElement(mpEBSSTData, DBConstants.NOWRITELOCK);

        doNothing().when(mpEBSSTData).clear();
        doNothing().when(mpEBSSTData).setKey(StationData.STATIONNAME_NAME, "STATION");
        doNothing().when(mpEBSSTData).setKey(StationData.WAREHOUSE_NAME, "WAREHOUSE");

        assertEquals(ebsStationData, spyEBSStation.getStationData("STATION", "WAREHOUSE"));

        verify(mpEBSSTData, times(1)).clear();
        verify(mpEBSSTData, times(1)).setKey(StationData.STATIONNAME_NAME, "STATION");
        verify(mpEBSSTData, times(1)).setKey(StationData.WAREHOUSE_NAME, "WAREHOUSE");

        verify(spyEBSStation, times(1)).getElement(mpEBSSTData, DBConstants.NOWRITELOCK);
    }

    @Test
    void setStatus_TestBehavior_CheckInteraction() throws DBException {
        EBSStation spyEBSStation = spy(ebsStation);
        doNothing().when(spyEBSStation).modifyElement(mpEBSSTData);

        doNothing().when(mpEBSSTData).clear();
        doNothing().when(mpEBSSTData).setKey(StationData.STATIONNAME_NAME, "STATION");
        doNothing().when(mpEBSSTData).setKey(StationData.WAREHOUSE_NAME, "WAREHOUSE");
        doNothing().when(mpEBSSTData).setStatus(123);

        spyEBSStation.setStatus("STATION", "WAREHOUSE", 123);

        verify(mpEBSSTData, times(1)).clear();
        verify(mpEBSSTData, times(1)).setKey(StationData.STATIONNAME_NAME, "STATION");
        verify(mpEBSSTData, times(1)).setKey(StationData.WAREHOUSE_NAME, "WAREHOUSE");
        verify(mpEBSSTData, times(1)).setStatus(123);
        verify(spyEBSStation, times(1)).modifyElement(mpEBSSTData);
    }

}
