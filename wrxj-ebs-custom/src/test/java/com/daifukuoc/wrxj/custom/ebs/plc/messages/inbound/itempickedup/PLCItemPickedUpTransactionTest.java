package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itempickedup;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuoc.wrxj.custom.ebs.plc.queue.CommandQueueManagerImpl;

@ExtendWith(MockitoExtension.class)
class PLCItemPickedUpTransactionTest {

    MockedStatic<Factory> mockedFactory;

    @Mock
    Load load;

    @Mock
    Location location;

    @Mock
    LoadData loadData;

    @Mock
    LocationData locationData;
    
    @Mock
    CommandQueueManagerImpl commandQueueManagerImpl;

    PLCItemPickedUpTransaction plcItemPickedUpTransaction;

    @BeforeEach
    public void setUp() {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(Load.class)).thenReturn(load);
        mockedFactory.when(() -> Factory.create(Location.class)).thenReturn(location);
        mockedFactory.when(() -> Factory.create(CommandQueueManagerImpl.class)).thenReturn(commandQueueManagerImpl);
        plcItemPickedUpTransaction = new PLCItemPickedUpTransaction();
    }

    @AfterEach
    public void tearDown() {
        mockedFactory.close();
        plcItemPickedUpTransaction = null;
    }

    @Test
    void execute_LoadAndLocationAreUpdated() throws DBException {
        PLCItemPickedUpTransaction spyPlcItemPickedUpTransaction = spy(plcItemPickedUpTransaction);

        doNothing().when(spyPlcItemPickedUpTransaction).startLocalTransaction();

        doReturn(loadData).when(load).getLoadData("123");
        doReturn(DBConstants.STORING).when(loadData).getLoadMoveStatus();

        doNothing().when(loadData).setCurrentAddress("TOLOC");
        doNothing().when(load).updateLoadInfo(loadData);

        doReturn("WAREHOUSE").when(loadData).getWarehouse();
        doReturn(locationData).when(location).getLocation("WAREHOUSE", "FROMLOCATION");
        doReturn(DBTrans.LCASRS).when(locationData).getLocationType();
        doNothing().when(location).setEmptyFlagValue("WAREHOUSE", "FROMLOCATION", DBTrans.UNOCCUPIED);

        doNothing().when(spyPlcItemPickedUpTransaction).commitLocalTransaction();
        doNothing().when(spyPlcItemPickedUpTransaction).endLocalTransaction();
        doNothing().when(commandQueueManagerImpl).update("123", DBConstants.CMD_PROCCESSING);

        spyPlcItemPickedUpTransaction.execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));

        verify(spyPlcItemPickedUpTransaction, times(1)).startLocalTransaction();

        verify(load, times(1)).getLoadData("123");
        verify(loadData, times(1)).getLoadMoveStatus();

        verify(loadData, times(1)).setCurrentAddress("TOLOC");
        verify(load, times(1)).updateLoadInfo(loadData);

        verify(loadData, times(1)).getWarehouse();
        verify(location, times(1)).getLocation("WAREHOUSE", "FROMLOCATION");
        verify(locationData, times(1)).getLocationType();
        verify(location, times(1)).setEmptyFlagValue("WAREHOUSE", "FROMLOCATION", DBTrans.UNOCCUPIED);

        verify(spyPlcItemPickedUpTransaction, times(1)).commitLocalTransaction();
        verify(spyPlcItemPickedUpTransaction, times(1)).endLocalTransaction();

        verify(spyPlcItemPickedUpTransaction, times(1)).execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));
    }

    @Test
    void execute_OnlyLoadIsUpdated() throws DBException {
        PLCItemPickedUpTransaction spyPlcItemPickedUpTransaction = spy(plcItemPickedUpTransaction);

        doNothing().when(spyPlcItemPickedUpTransaction).startLocalTransaction();

        doReturn(loadData).when(load).getLoadData("123");
        doNothing().when(loadData).setCurrentAddress("TOLOC");
        doNothing().when(load).updateLoadInfo(loadData);

        doReturn("WAREHOUSE").when(loadData).getWarehouse();
        doReturn(locationData).when(location).getLocation("WAREHOUSE", "FROMLOCATION");
        doReturn(DBConstants.STOREPENDING).when(loadData).getLoadMoveStatus();
        doNothing().when(loadData).setLoadMoveStatus(DBConstants.STORING);

        doReturn(DBTrans.LC_FRONT).when(locationData).getLocationType();

        doNothing().when(spyPlcItemPickedUpTransaction).commitLocalTransaction();
        doNothing().when(spyPlcItemPickedUpTransaction).endLocalTransaction();

        spyPlcItemPickedUpTransaction.execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));

        verify(spyPlcItemPickedUpTransaction, times(1)).startLocalTransaction();

        verify(load, times(1)).getLoadData("123");
        verify(loadData, times(1)).getLoadMoveStatus();
        verify(loadData, times(1)).setLoadMoveStatus(DBConstants.STORING);

        verify(loadData, times(1)).setCurrentAddress("TOLOC");
        verify(load, times(1)).updateLoadInfo(loadData);

        verify(loadData, times(1)).getWarehouse();
        verify(location, times(1)).getLocation("WAREHOUSE", "FROMLOCATION");
        verify(locationData, times(1)).getLocationType();

        verify(spyPlcItemPickedUpTransaction, times(1)).commitLocalTransaction();
        verify(spyPlcItemPickedUpTransaction, times(1)).endLocalTransaction();

        verify(spyPlcItemPickedUpTransaction, times(1)).execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));
    }

    @Test
    void execute_LocationDataToUpdateIsNotFoundInDB() throws DBException {
        PLCItemPickedUpTransaction spyPlcItemPickedUpTransaction = spy(plcItemPickedUpTransaction);

        doNothing().when(spyPlcItemPickedUpTransaction).startLocalTransaction();

        doReturn(loadData).when(load).getLoadData("123");
        doReturn(DBConstants.RETRIEVEPENDING).when(loadData).getLoadMoveStatus();
        doNothing().when(loadData).setLoadMoveStatus(DBConstants.RETRIEVING);
        doNothing().when(loadData).setCurrentAddress("TOLOC");
        doNothing().when(load).updateLoadInfo(loadData);

        doReturn("WAREHOUSE").when(loadData).getWarehouse();
        doReturn(null).when(location).getLocation("WAREHOUSE", "FROMLOCATION");

        doNothing().when(spyPlcItemPickedUpTransaction).commitLocalTransaction();
        doNothing().when(spyPlcItemPickedUpTransaction).endLocalTransaction();

        spyPlcItemPickedUpTransaction.execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));

        verify(spyPlcItemPickedUpTransaction, times(1)).startLocalTransaction();

        verify(load, times(1)).getLoadData("123");
        verify(loadData, times(1)).getLoadMoveStatus();
        verify(loadData, times(1)).setLoadMoveStatus(DBConstants.RETRIEVING);

        verify(loadData, times(1)).setCurrentAddress("TOLOC");
        verify(load, times(1)).updateLoadInfo(loadData);

        verify(loadData, times(1)).getWarehouse();
        verify(location, times(1)).getLocation("WAREHOUSE", "FROMLOCATION");

        verify(spyPlcItemPickedUpTransaction, times(1)).commitLocalTransaction();
        verify(spyPlcItemPickedUpTransaction, times(1)).endLocalTransaction();

        verify(spyPlcItemPickedUpTransaction, times(1)).execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));
    }

    @Test
    void execute_LoadDataIsNotFoundInDB() throws DBException {
        PLCItemPickedUpTransaction spyPlcItemPickedUpTransaction = spy(plcItemPickedUpTransaction);

        doNothing().when(spyPlcItemPickedUpTransaction).startLocalTransaction();

        doReturn(null).when(load).getLoadData("123");

        doNothing().when(spyPlcItemPickedUpTransaction).commitLocalTransaction();
        doNothing().when(spyPlcItemPickedUpTransaction).endLocalTransaction();

        spyPlcItemPickedUpTransaction.execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));

        verify(spyPlcItemPickedUpTransaction, times(1)).startLocalTransaction();

        verify(load, times(1)).getLoadData("123");

        verify(spyPlcItemPickedUpTransaction, times(1)).commitLocalTransaction();

        verify(spyPlcItemPickedUpTransaction, times(1)).endLocalTransaction();

        verify(spyPlcItemPickedUpTransaction, times(1)).execute(new ItemPickedUpContext("123", "FROMLOCATION", "TOLOC"));
    }
}
