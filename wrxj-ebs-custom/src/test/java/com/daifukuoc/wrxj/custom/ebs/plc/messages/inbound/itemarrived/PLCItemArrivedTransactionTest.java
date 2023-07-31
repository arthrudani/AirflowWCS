package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Date;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommand;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLocationServer;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSDBConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerFailureException;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCMoveOrderMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound.PLCStandardAckMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.queue.CommandQueueManagerImpl;

@ExtendWith(MockitoExtension.class)
class PLCItemArrivedTransactionTest {
    MockedStatic<Factory> mockedFactory;

    @Mock
    StandardStationServer stnServer;

    @Mock
    Load mpLoad;

    @Mock
    EBSLoadServer mpEBSLoadServer;

    @Mock
    EBSInventoryServer mpEBSInventory;

    @Mock
    PLCStandardAckMessage itemAckMsg;

    @Mock
    PLCMoveOrderMessage moveOrder;

    @Mock
    EBSLocationServer ebsLocationServer;

    @Mock
    StationData stationData;

    @Mock
    LoadData loadData;

    @Mock
    LoadLineItemData loadLineData;
    
    @Mock
    LoadLineItem loadLine;

    @Mock
    LocationData locationData;

    @Mock
    PLCMoveOrderMessage plcMoveOrderMessage;

    @Mock
    TransactionHistory transactionHistory;

    @Mock
    TransactionHistoryData transactionHistoryData;

    @Mock
    RouteManagerImpl routeManagerImpl;
    
    @Mock
    CommandQueueManagerImpl commandQueueManagerImpl;
    
    @Mock
    MoveCommandData moveCommandData;
    
    @Mock
    MoveCommand moveCommand;

    private PLCItemArrivedTransaction plcItemArrivedTransaction;

    @BeforeEach
    public void setUp() {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(StandardStationServer.class)).thenReturn(stnServer);
        mockedFactory.when(() -> Factory.create(Load.class)).thenReturn(mpLoad);
        mockedFactory.when(() -> Factory.create(EBSLoadServer.class)).thenReturn(mpEBSLoadServer);
        mockedFactory.when(() -> Factory.create(EBSInventoryServer.class)).thenReturn(mpEBSInventory);
        mockedFactory.when(() -> Factory.create(PLCStandardAckMessage.class)).thenReturn(itemAckMsg);
        mockedFactory.when(() -> Factory.create(PLCMoveOrderMessage.class)).thenReturn(moveOrder);
        mockedFactory.when(() -> Factory.create(EBSLocationServer.class)).thenReturn(ebsLocationServer);
        mockedFactory.when(() -> Factory.create(StationData.class)).thenReturn(stationData);
        mockedFactory.when(() -> Factory.create(LoadData.class)).thenReturn(loadData);
        mockedFactory.when(() -> Factory.create(LoadLineItemData.class)).thenReturn(loadLineData);
        mockedFactory.when(() -> Factory.create(LoadLineItem.class)).thenReturn(loadLine);
        mockedFactory.when(() -> Factory.create(TransactionHistory.class)).thenReturn(transactionHistory);
        mockedFactory.when(() -> Factory.create(TransactionHistoryData.class)).thenReturn(transactionHistoryData);
        mockedFactory.when(() -> Factory.create(RouteManagerImpl.class)).thenReturn(routeManagerImpl);
        mockedFactory.when(() -> Factory.create(CommandQueueManagerImpl.class)).thenReturn(commandQueueManagerImpl);
        mockedFactory.when(() -> Factory.create(MoveCommand.class)).thenReturn(moveCommand);
        mockedFactory.when(() -> Factory.create(MoveCommandData.class)).thenReturn(moveCommandData);
        
        plcItemArrivedTransaction = new PLCItemArrivedTransaction();
    }

    @AfterEach
    public void tearDown() {
        plcItemArrivedTransaction = null;
        mockedFactory.close();
    }
  
    @Test
    void constructMoveOrderData_setsValues() throws RouteManagerFailureException, DBException {
        doReturn("ORDER").when(loadLineData).getOrderID();
        doReturn("LOAD").when(loadData).getLoadID();
        doReturn("GLOBAL").when(loadLineData).getGlobalID();
        doReturn("LINE").when(loadLineData).getLineID();
        doReturn("LOT").when(loadLineData).getLot();
        Date date = new Date();
        doReturn(date).when(loadLineData).getExpirationDate();
        doReturn("FINALSORTLOCATION").when(loadData).getFinalSortLocationID();

        doNothing().when(moveOrder).setSerialNum("SERIAL"); // TODO: This will be our generated serial number
        doNothing().when(moveOrder).setOrderId("ORDER");
        doNothing().when(moveOrder).setLoadId("LOAD");
        doNothing().when(moveOrder).setGlobalId("GLOBAL");
        doNothing().when(moveOrder).setLineId("LINE");
        doNothing().when(moveOrder).setLot("LOT");
        doNothing().when(moveOrder).setFlightSchduledDateTime(date);
        doNothing().when(moveOrder).setFinalSortLocation("FINALSORTLOCATION");
        doNothing().when(moveOrder).setFromLocation("FROM_STATION");
        doNothing().when(moveOrder).setToLocation("NEXT");
        doNothing().when(moveOrder).setMoveType("MOVE_TYPE");
        doNothing().when(moveOrder).setDeviceId("DEVICE");

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "FROM_STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        assertEquals(moveOrder,
                plcItemArrivedTransaction.constructMoveOrderData(plcItemArrivedContext, "NEXT", "MOVE_TYPE"));

        verify(moveOrder, times(1)).setSerialNum("SERIAL"); // TODO: This will be our generated serial number
        verify(moveOrder, times(1)).setOrderId("ORDER");
        verify(moveOrder, times(1)).setLoadId("LOAD");
        verify(moveOrder, times(1)).setGlobalId("GLOBAL");
        verify(moveOrder, times(1)).setLineId("LINE");
        verify(moveOrder, times(1)).setLot("LOT");
        verify(moveOrder, times(1)).setFlightSchduledDateTime(date);
        verify(moveOrder, times(1)).setFinalSortLocation("FINALSORTLOCATION");
        verify(moveOrder, times(1)).setFromLocation("FROM_STATION");
        verify(moveOrder, times(1)).setToLocation("NEXT");
        verify(moveOrder, times(1)).setMoveType("MOVE_TYPE");
        verify(moveOrder, times(1)).setDeviceId("DEVICE");

    }
    
    @Test
    void process_normalCase_Output() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedMessage = spy(plcItemArrivedTransaction);

        doReturn(DBConstants.OUTPUT).when(stationData).getStationType();
        doNothing().when(moveCommand).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        doNothing().when(mpEBSInventory).processReleasedLoadData(plcItemArrivedContext);
        spiedPlcItemArrivedMessage.executeBody(plcItemArrivedContext);

        verify(mpEBSInventory, times(1)).processReleasedLoadData(plcItemArrivedContext);
    }
    
    @ParameterizedTest
    @MethodSource("process_normalCase_OtherThanOutput_Param")
    @Disabled
    void process_normalCase_OtherThanOutput(int stationType, int numOfCallingGetStationType) throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedMessage = spy(plcItemArrivedTransaction);
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "", "", "", "", "0", "");

        doReturn(stationType).when(stationData).getStationType();

        spiedPlcItemArrivedMessage.executeBody(plcItemArrivedContext);

        verify(spiedPlcItemArrivedMessage, times(1)).constructMoveOrderData(plcItemArrivedContext, "NEXT",
                String.valueOf(PLCConstants.MoveOrder.MOVE_TYPE.STORAGE));
    }
    private static Stream<Arguments> process_normalCase_OtherThanOutput_Param() {
        return Stream.of(Arguments.of(DBConstants.INPUT, 2), Arguments.of(DBConstants.REVERSIBLE, 3));
    }

    @Test
    void process_abnormalCase_StationIsNotFound() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedMessage = spy(plcItemArrivedTransaction);

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        doReturn(moveCommandData).when(spiedPlcItemArrivedMessage).constructMoveCommandData(plcItemArrivedContext, null, null, 1);
        spiedPlcItemArrivedMessage.executeBody(plcItemArrivedContext);
        
    }

   
    @Test
    void process_abnormalCase_OutputButEndsWithException() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedMessage = spy(plcItemArrivedTransaction);

        doReturn(DBConstants.OUTPUT).when(stationData).getStationType();
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
        		"STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        doNothing().when(moveCommand).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
        doThrow(new DBException()).when(mpEBSInventory).processReleasedLoadData(plcItemArrivedContext);

        spiedPlcItemArrivedMessage.executeBody(plcItemArrivedContext);

        verify(stationData, times(1)).getStationType();
        verify(mpEBSInventory, times(1)).processReleasedLoadData(plcItemArrivedContext);
    }
    
    @Test
    void process_abnormalCase_UnknownStationType() throws DBException, RouteManagerFailureException {
        PLCItemArrivedTransaction spiedPlcItemArrivedMessage = spy(plcItemArrivedTransaction);
        doReturn("NEXT").when(routeManagerImpl).findNextDestination(loadData, "STATION");
        doReturn("2").when(routeManagerImpl).getMoveType();
        doReturn(200).when(stationData).getStationType(); // none of OUTPUT(223),INPUT(224),REVERSIBLE(225)
        
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        spiedPlcItemArrivedMessage.executeBody(plcItemArrivedContext);

        verify(stationData, times(6)).getStationType();

    }
    
    @Test
    void executeBody_doNothing_WhenArgumentDoesNotHaveStationData() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(null, null, null, null, null, null,
                null, null, null, null);
        spiedPlcItemArrivedTransaction.executeBody(plcItemArrivedContext);
        verify(spiedPlcItemArrivedTransaction, times(1)).executeBody(plcItemArrivedContext);
        verifyNoMoreInteractions(spiedPlcItemArrivedTransaction);
    }
    
    @Test
    void executeBody_checkLogicFlow_WhenReversibleAndStorePending() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");

        doReturn(DBConstants.REVERSIBLE).when(stationData).getStationType();
        doReturn(DBConstants.STOREPENDING).when(loadData).getLoadMoveStatus();

        doNothing().when(spiedPlcItemArrivedTransaction).doInProgressProcess(plcItemArrivedContext);

        spiedPlcItemArrivedTransaction.executeBody(plcItemArrivedContext);

        verify(stationData, times(1)).getStationType();
        verify(loadData, times(1)).getLoadMoveStatus();

        verify(spiedPlcItemArrivedTransaction, times(1)).doInProgressProcess(plcItemArrivedContext);

    }
    
    @Test
    void executeBody_checkLogicFlow_WhenReversibleAndRetrievePending() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");

        doReturn(DBConstants.REVERSIBLE).when(stationData).getStationType();
        doReturn(DBConstants.RETRIEVEPENDING).when(loadData).getLoadMoveStatus();

        doNothing().when(spiedPlcItemArrivedTransaction).completeReleaseProcess(plcItemArrivedContext);

        spiedPlcItemArrivedTransaction.executeBody(plcItemArrivedContext);

        verify(stationData, times(1)).getStationType();
        verify(loadData, times(1)).getLoadMoveStatus();

        verify(spiedPlcItemArrivedTransaction, times(1)).completeReleaseProcess(plcItemArrivedContext);

    }
    
    @Test
    void executeBody_checkLogicFlow_WhenOutput() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");

        doReturn(DBConstants.OUTPUT).when(stationData).getStationType();

        doNothing().when(spiedPlcItemArrivedTransaction).completeReleaseProcess(plcItemArrivedContext);

        spiedPlcItemArrivedTransaction.executeBody(plcItemArrivedContext);

        verify(stationData, times(1)).getStationType();

        verify(spiedPlcItemArrivedTransaction, times(1)).completeReleaseProcess(plcItemArrivedContext);

    }

    @Test
    void executeBody_checkLogicFlow_WhenInput() throws DBException {
        PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");

        doReturn(DBConstants.INPUT).when(stationData).getStationType();

        doNothing().when(spiedPlcItemArrivedTransaction).doInProgressProcess(plcItemArrivedContext);

        spiedPlcItemArrivedTransaction.executeBody(plcItemArrivedContext);

        verify(stationData, times(1)).getStationType();

        verify(spiedPlcItemArrivedTransaction, times(1)).doInProgressProcess(plcItemArrivedContext);

    }

    @Test
    void doInProgressProcess_checkLocationIsReservedOrEmpty() throws DBException, RouteManagerFailureException {
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);

        doReturn("NEXT").when(routeManagerImpl).findNextDestination(loadData, "STATION");
        doReturn("2").when(routeManagerImpl).getMoveType();
        doReturn(moveCommandData).when(spiedPlcItemArrivedTransaction).constructMoveCommandData(plcItemArrivedContext, "NEXT",
                "2",1);
        doNothing().when(commandQueueManagerImpl).enqueue(moveCommandData);
        doNothing().when(loadData).setCurrentAddress("STATION");

        spiedPlcItemArrivedTransaction.doInProgressProcess(plcItemArrivedContext);

        verify(routeManagerImpl, times(1)).findNextDestination(loadData, "STATION");
        verify(spiedPlcItemArrivedTransaction, times(1)).constructMoveCommandData(plcItemArrivedContext, "NEXT",
                "2",1);
    }

    @Test
    void doInProgressProcess_checkLocationIsOccupied() throws DBException, RouteManagerFailureException {
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);
        doReturn("0").when(routeManagerImpl).getMoveType();
        doReturn(moveCommandData).when(spiedPlcItemArrivedTransaction).constructMoveCommandData(plcItemArrivedContext, null,
                "0",1);
        doNothing().when(commandQueueManagerImpl).enqueue(moveCommandData);
        doNothing().when(loadData).setCurrentAddress("STATION");

        spiedPlcItemArrivedTransaction.doInProgressProcess(plcItemArrivedContext);
        verify(spiedPlcItemArrivedTransaction, times(1)).constructMoveCommandData(plcItemArrivedContext, null,
                "0",1);
        verify(commandQueueManagerImpl, times(1)).enqueue(moveCommandData);
        verifyNoMoreInteractions(moveOrder);
    }

    @Test
    void doTerminalProcess_checkFlow_normalCase() throws DBException {

        doNothing().when(moveCommand).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        doNothing().when(mpEBSInventory).processReleasedLoadData(plcItemArrivedContext);
        plcItemArrivedTransaction.completeReleaseProcess(plcItemArrivedContext);
        verify(moveCommand, times(1)).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
        verify(mpEBSInventory, times(1)).processReleasedLoadData(plcItemArrivedContext);

    }

    @Test
    void executeBody_checkFlow_normalCase_nullLoad() throws DBException, RouteManagerFailureException {
    	PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);
        doReturn("NEXT").when(routeManagerImpl).findNextDestination(loadData, "ADDRESS");
        doReturn("2").when(routeManagerImpl).getMoveType();
        doNothing().when(mpLoad).addElement(loadData);
        doNothing().when(loadLine).addElement(loadLineData);
        doNothing().when(commandQueueManagerImpl).enqueue(moveCommandData);
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, null, loadLineData,
                "ADDRESS", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        doReturn(moveCommandData).when(spiedPlcItemArrivedTransaction).constructMoveCommandData(plcItemArrivedContext, "NEXT",
        		"2",1);
        spiedPlcItemArrivedTransaction.executeBody(plcItemArrivedContext);
        verify(mpLoad, times(1)).addElement(loadData);
        verify(loadLine, times(1)).addElement(loadLineData);
        
    }

    @Test
    void doTerminalProcess_checkFlow_exceptionCase() throws DBException {
    	ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "STATION", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        doThrow(new DBException()).when(mpEBSInventory).processReleasedLoadData(plcItemArrivedContext);

        doNothing().when(moveCommand).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
        plcItemArrivedTransaction.completeReleaseProcess(plcItemArrivedContext);
        verify(moveCommand, times(1)).updateMoveCommandStatusByLoadId("LOAD", DBConstants.CMD_DELETED);
        verify(mpEBSInventory, times(1)).processReleasedLoadData(plcItemArrivedContext);

    }

    @ParameterizedTest
    @MethodSource("isOutBoundStation_returnsValueWIthNoCondition_ForSpecificType_Param")
    void isOutBoundStation_returnsValueWIthNoCondition_ForSpecificType(int stationType, boolean expectation) {
        doReturn(stationType).when(stationData).getStationType();

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, null, null, null,
                null, null, null, null, null);

        assertEquals(expectation, plcItemArrivedTransaction.isOutBoundStation(plcItemArrivedContext));

        verify(stationData, times(1)).getStationType();

    }

    private static Stream<Arguments> isOutBoundStation_returnsValueWIthNoCondition_ForSpecificType_Param() {
        return Stream.of(Arguments.of(DBConstants.OUTPUT, true), Arguments.of(DBConstants.INPUT, false));
    }

    @ParameterizedTest
    @MethodSource("isOutpundStation_returnsValueBycondition_forReversible_Param")
    void isOutpundStation_returnsValueBycondition_forReversible(int loadMoveStatus, boolean expectation) {
        doReturn(DBConstants.REVERSIBLE).when(stationData).getStationType();
        doReturn(loadMoveStatus).when(loadData).getLoadMoveStatus();

        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, null, null, null,
                null, null, null, null, null);

        assertEquals(expectation, plcItemArrivedTransaction.isOutBoundStation(plcItemArrivedContext));

        verify(stationData, times(1)).getStationType();
        verify(loadData, times(1)).getLoadMoveStatus();
    }

    private static Stream<Arguments> isOutpundStation_returnsValueBycondition_forReversible_Param() {
        return Stream.of(Arguments.of(DBConstants.RETRIEVEPENDING, true), Arguments.of(DBConstants.RETRIEVING, true),
                Arguments.of(DBConstants.SHIPPING, false), Arguments.of(0, false));
    }

    @Test
    void doInProgressProcess_changesLoadStatusToStorePendingIfItIsArrivePending()
            throws RouteManagerFailureException, DBException {
    	PLCItemArrivedTransaction spiedPlcItemArrivedTransaction = spy(plcItemArrivedTransaction);
        doReturn("NEXT").when(routeManagerImpl).findNextDestination(loadData, "ADDRESS");
        doReturn("2").when(routeManagerImpl).getMoveType();
        doReturn(DBConstants.ARRIVEPENDING).when(loadData).getLoadMoveStatus();
        doNothing().when(loadData).setLoadMoveStatus(DBConstants.STOREPENDING);
        doNothing().when(mpLoad).updateLoadInfo(loadData);
        doNothing().when(commandQueueManagerImpl).enqueue(moveCommandData);
        ItemArrivedContext plcItemArrivedContext = new ItemArrivedContext(stationData, loadData, loadLineData,
                "ADDRESS", "ORDER", "LOAD", "GLOBAL", "LINE", "SERIAL", "DEVICE");
        doReturn(moveCommandData).when(spiedPlcItemArrivedTransaction).constructMoveCommandData(plcItemArrivedContext, "NEXT",
        		"2",1);
        spiedPlcItemArrivedTransaction.doInProgressProcess(plcItemArrivedContext);

        verify(routeManagerImpl, times(1)).findNextDestination(loadData, "ADDRESS");
        verify(loadData, times(4)).getLoadMoveStatus();
        verify(loadData, times(1)).setLoadMoveStatus(DBConstants.STOREPENDING);
        verify(mpLoad, times(1)).updateLoadInfo(loadData);
        verify(commandQueueManagerImpl, times(1)).enqueue(moveCommandData);

    }

}
