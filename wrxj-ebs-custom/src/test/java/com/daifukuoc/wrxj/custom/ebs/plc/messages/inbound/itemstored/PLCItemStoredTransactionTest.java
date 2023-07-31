package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;

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
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommand;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerFailureException;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.queue.CommandQueueManagerImpl;

@ExtendWith(MockitoExtension.class)
class PLCItemStoredTransactionTest {
	
	private static final String DEVICEID = "9001";

    MockedStatic<Factory> mockedFactory;

    
    @Mock
    EBSInventoryServer ebsInventoryServer;
    
    @Mock
    EBSLocation mpEBSLocation;
    
    @Mock
    Load mpEBSLoad;
    
    @Mock
    LoadData loadData;
    
    @Mock
    LoadLineItemData loadLineItemData;
    
    @Mock
    EBSLoadServer mpEBSLoadServer;
    
    @Mock
    AisleEmptyLocationFinderImpl aisleEmptyLocationFinderImpl;
    
    @Mock
    RouteManagerImpl routeManagerImpl;
    
    @Mock
    MoveCommand moveCommand;
    
    @Mock
    MoveCommandData moveCommandData;
    
    @Mock
    CommandQueueManagerImpl commandQueueManagerImpl;
    
    PLCItemStoredTransaction plcItemStoredTransaction;
    
    @BeforeEach
    public void setUp() {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(EBSInventoryServer.class)).thenReturn(ebsInventoryServer);
        mockedFactory.when(() -> Factory.create(EBSLoadServer.class)).thenReturn(mpEBSLoadServer);
        mockedFactory.when(() -> Factory.create(Load.class)).thenReturn(mpEBSLoad);
        mockedFactory.when(() -> Factory.create(AisleEmptyLocationFinderImpl.class)).thenReturn(aisleEmptyLocationFinderImpl);
        mockedFactory.when(() -> Factory.create(RouteManagerImpl.class)).thenReturn(routeManagerImpl);
        mockedFactory.when(() -> Factory.create(EBSLocation.class)).thenReturn(mpEBSLocation);
        mockedFactory.when(() -> Factory.create(MoveCommand.class)).thenReturn(moveCommand);
        mockedFactory.when(() -> Factory.create(CommandQueueManagerImpl.class)).thenReturn(commandQueueManagerImpl);
        plcItemStoredTransaction = new PLCItemStoredTransaction();

    }

    @AfterEach
    public void tearDown() {
        plcItemStoredTransaction = null;
        mockedFactory.close();
    }

    @Test
    void executeBody_normalCase() throws DBException {
        ItemStoredContext plcItemStoredContext = new ItemStoredContext("ORDER", "LOAD", "ADDRESS", 0, "GLOBAL",
                "LINE", DEVICEID);

        doNothing().when(ebsInventoryServer).processStoreComplete(plcItemStoredContext);

        plcItemStoredTransaction.executeBody(plcItemStoredContext);

        verify(ebsInventoryServer, times(1)).processStoreComplete(plcItemStoredContext);

    }

    @Test
    void executeBody_abnormalCase_EndsWithDBException() throws DBException {
        ItemStoredContext plcItemStoredContext = new ItemStoredContext("ORDER", "LOAD", "ADDRESS", PLCConstants.PLC_ITEM_STORED_BIN_FULL_ERROR, "GLOBAL",
                "LINE", DEVICEID);

        doReturn(loadData).when(mpEBSLoadServer).getLoad("LOAD");
        doThrow(new DBException()).when(aisleEmptyLocationFinderImpl).find(loadData);

        plcItemStoredTransaction.executeBody(plcItemStoredContext);

        verify(ebsInventoryServer, times(0)).processStoreComplete(plcItemStoredContext);

    }
    /*
    @Test
    void executeBody_Bin_Full_Error() throws DBException {
    	plcItemStoredTransaction = spy(plcItemStoredTransaction);
    	
    	PLCItemStoredContext plcItemStoredContext = new PLCItemStoredContext("ORDER", "LOAD", "ADDRESS", PLCConstants.PLC_ITEM_STORED_BIN_FULL_ERROR, "GLOBAL", "LINE", DEVICEID);
    	doReturn(loadData).when(mpEBSLoadServer).getLoad("LOAD");
    	doReturn("ADDRESS").when(aisleEmptyLocationFinderImpl).find(loadData);
    	doReturn("LOADID").when(loadData).getLoadID();
    	doReturn(loadLineItemData).when(mpEBSLoadServer).getLoadLineByLoadId("LOADID");
    	doReturn("ADDRESS").when(loadData).getAddress();
    	doReturn("ADDRESS").when(loadData).getCurrentAddress();
    	doReturn("EBS").when(loadData).getWarehouse();
    	doNothing().when(mpEBSLocation).setEmptyFlagValue("EBS", "ADDRESS", DBConstants.OCCUPIED);
    	doNothing().when(mpEBSLocation).setLocationStatusValue("EBS", "ADDRESS", DBConstants.LCPROHIBIT);
    	doNothing().when(loadData).setAddress("ADDRESS");
    	doNothing().when(mpEBSLoad).updateLoadInfo(loadData);
    	doNothing().when(mpEBSLocation).setEmptyFlagValue("EBS", "ADDRESS", DBConstants.LCRESERVED);
    	doNothing().when(commandQueueManagerImpl).enqueue(moveCommandData);
    	doNothing().when(moveCommand).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
    	doReturn(moveCommandData).when(plcItemStoredTransaction).constructMoveCommandData(plcItemStoredContext, "ADDRESS", String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.INTER_LOCATION), DBConstants.CMD_RACK);
    	doNothing().when(ebsInventoryServer).processStoreComplete(plcItemStoredContext);
    	
    	plcItemStoredTransaction.executeBody(plcItemStoredContext);
    	
    	verify(ebsInventoryServer, times(1)).processStoreComplete(plcItemStoredContext);
    }
    
    @Test
    void executeBody_Bin_Full_Error_And_Location_Not_Available() throws DBException, RouteManagerFailureException {
    	plcItemStoredTransaction = spy(plcItemStoredTransaction);
    	
    	PLCItemStoredContext plcItemStoredContext = new PLCItemStoredContext("ORDER", "LOAD", "ADDRESS", PLCConstants.PLC_ITEM_STORED_BIN_FULL_ERROR, "GLOBAL", "LINE" , DEVICEID);
    	doReturn(loadData).when(mpEBSLoadServer).getLoad("LOAD");
    	doReturn(null).when(aisleEmptyLocationFinderImpl).find(loadData);
    	doReturn("LOADID").when(loadData).getLoadID();
    	doReturn(loadLineItemData).when(mpEBSLoadServer).getLoadLineByLoadId("LOADID");
    	doReturn("ADDRESS").when(loadData).getAddress();
    	doReturn("EBS").when(loadData).getWarehouse();
    	doNothing().when(mpEBSLocation).setEmptyFlagValue("EBS", "ADDRESS", DBConstants.OCCUPIED);
    	doNothing().when(mpEBSLocation).setLocationStatusValue("EBS", "ADDRESS", DBConstants.LCPROHIBIT);
    	doNothing().when(commandQueueManagerImpl).enqueue(moveCommandData);
    	doNothing().when(moveCommand).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
    	doNothing().when(loadData).setLoadMoveStatus(DBConstants.RETRIEVING);
    	doNothing().when(mpEBSLoad).updateLoadInfo(loadData);
    	doReturn("ADDRESS").when(routeManagerImpl).findNextDestination(loadData, "ADDRESS");
    	doReturn(String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.RETRIEVAL)).when(routeManagerImpl).getMoveType();
    	doReturn(moveCommandData).when(plcItemStoredTransaction).constructMoveCommandData(plcItemStoredContext, "ADDRESS", String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.RETRIEVAL), DBConstants.CMD_RETRIEVAL);
    	doNothing().when(ebsInventoryServer).processStoreComplete(plcItemStoredContext);
    	
    	plcItemStoredTransaction.executeBody(plcItemStoredContext);
    	
    	verify(ebsInventoryServer, times(1)).processStoreComplete(plcItemStoredContext);
    }
*/

}
