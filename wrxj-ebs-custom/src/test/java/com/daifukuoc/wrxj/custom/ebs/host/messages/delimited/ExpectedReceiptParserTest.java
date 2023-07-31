package com.daifukuoc.wrxj.custom.ebs.host.messages.delimited;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSPoReceivingServer;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptMessageData;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageHeader;

@ExtendWith(MockitoExtension.class)
class ExpectedReceiptParserTest {
    
    private static final String HEADER = "84,1000,52,2222,3,4,5,0";
    private static final String ORDER_ID = "999";
    private static final String LOAD_ID = "1002";
    private static final String GLOBAL_ID = "10021002";
    private static final String LINE_ID = "BAG1002";
    private static final String FLIGHT_NUMBER = "FL100";
    private static final String FLIGHT_SCHEDULED_DATETIME = "20221213000000";
    private static final String DEFAULT_RETRIEVAL_DATETIME = "20221213000000";
    private static final String FINAL_SORT_LOCATION = "3600";
    private static final String ITEM_TYPE = "1"; // SACControlMessage.Bag_On_Tray
    private static final String REQUEST_TYPE = "1";
    
    private static final String INVALID_DATETIME_STRING = "20229999000000";
   
    MockedStatic<Factory> mockedFactory;
    MockedStatic<ThreadSystemGateway> mockedSystemGateway;
    MockedStatic<DBHelper> mockedDBHelper;
    
    @Mock
    HostToWrxData hostToWrxData;
    
    ExpectedReceiptMessageData expectedReceiptMessageData = new ExpectedReceiptMessageData();
    
    @Mock
    SACMessageHeader sACMessageHeader;    
    
    @Mock
    EBSPoReceivingServer eBSPoReceivingServer;
    
    @Mock
    EBSInventoryServer eBSInventoryServer;
    
    @Mock
    SystemGateway systemGateway;    
    
    ExpectedReceiptParser parser;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(ExpectedReceiptMessageData.class)).thenReturn(expectedReceiptMessageData);
        mockedFactory.when(() -> Factory.create(SACMessageHeader.class)).thenReturn(sACMessageHeader);
        mockedFactory.when(() -> Factory.create(EBSPoReceivingServer.class, ExpectedReceiptParser.PARSER_NAME)).thenReturn(eBSPoReceivingServer);
        mockedFactory.when(() -> Factory.create(EBSInventoryServer.class, ExpectedReceiptParser.PARSER_NAME)).thenReturn(eBSInventoryServer);
        
        mockedSystemGateway = Mockito.mockStatic(ThreadSystemGateway.class);
        mockedSystemGateway.when(() -> ThreadSystemGateway.get()).thenReturn(systemGateway);
        
        mockedDBHelper = Mockito.mockStatic(DBHelper.class);
        mockedSystemGateway.when(() -> DBHelper.getStringField(any(), eq(LoadLineItemData.LOT_NAME))).thenReturn(FLIGHT_NUMBER);
        mockedSystemGateway.when(() -> DBHelper.getStringField(any(), eq(LoadLineItemData.LINEID_NAME))).thenReturn(LINE_ID);
        
        parser = new ExpectedReceiptParser();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
        mockedSystemGateway.close();
        mockedDBHelper.close();
    }
    
    private String prepareMessageForTesting(String... fields) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            buf.append(fields[i]);
        }

        return buf.toString();
    }

    @Test
    void shouldThrowInvalidHostDataExceptionWhenReceivedMessageIsNull1() {
        assertThrows(InvalidHostDataException.class, () -> {
            parser.parse(null);
        });
    }
    
    @Test
    void shouldThrowInvalidHostDataExceptionWhenReceivedMessageIsNull2() {
        when(hostToWrxData.getMessage()).thenReturn(null);
        
        assertThrows(InvalidHostDataException.class, () -> {
            parser.parse(hostToWrxData);
        });
    }

    @Test
    void shouldThrowInvalidHostDataExceptionWhenReceivedMessageIsEmpty() {
        when(hostToWrxData.getMessage()).thenReturn(Strings.EMPTY);
        
        assertThrows(InvalidHostDataException.class, () -> {
            parser.parse(hostToWrxData);
        });
    }
    
    @Test
    void shouldPublishHostExpectedReceiptEventEvenWhenReceivedMessageIsInvalid() {
        String invalidMessage = prepareMessageForTesting(HEADER, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME, INVALID_DATETIME_STRING, FINAL_SORT_LOCATION, ITEM_TYPE, REQUEST_TYPE);
        when(hostToWrxData.getMessage()).thenReturn(invalidMessage);
        
        parser.parse(hostToWrxData);
        
        verify(systemGateway, times(1)).publishHostExpectedReceiptEvent(hostToWrxData.getMessage());
    }
    
    @Test
    void shouldIgnoreTheReceivedMessageWhenAlreadyProcessed() throws DBException, ParseException {
        String validMessage = prepareMessageForTesting(HEADER, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE, REQUEST_TYPE);
        when(hostToWrxData.getMessage()).thenReturn(validMessage);
                
        // No existing loadlineitem found by the load id
        List<Map> loadLineItems = new ArrayList<>();
        loadLineItems.add(new HashMap());
        when(eBSInventoryServer.getLoadLineItemDataListByLoadID(LOAD_ID)).thenReturn(loadLineItems);
       
        parser.parse(hostToWrxData);
        
        verify(eBSInventoryServer, times(1)).getLoadLineItemDataListByLoadID(LOAD_ID);
        verify(eBSPoReceivingServer, never()).addPOExpectedReceipt(expectedReceiptMessageData);
        verify(systemGateway, never()).publishHostExpectedReceiptEvent(hostToWrxData.getMessage());
    }
    
    @Test
    void shouldPublishHostExpectedReceiptEventWhenReceivedMessageIsValid() throws DBException, ParseException {
        String validMessage = prepareMessageForTesting(HEADER, ORDER_ID, LOAD_ID, GLOBAL_ID, LINE_ID, FLIGHT_NUMBER,
                FLIGHT_SCHEDULED_DATETIME, DEFAULT_RETRIEVAL_DATETIME, FINAL_SORT_LOCATION, ITEM_TYPE, REQUEST_TYPE);
        when(hostToWrxData.getMessage()).thenReturn(validMessage);
        
        // No existing loadlineitem found by the load id
        when(eBSInventoryServer.getLoadLineItemDataListByLoadID(LOAD_ID)).thenReturn(null);

        // New records created in purchaseorderheader and purchaseorderline tables
        doNothing().when(eBSPoReceivingServer).addPOExpectedReceipt(expectedReceiptMessageData);
        
        parser.parse(hostToWrxData);
        
        verify(eBSInventoryServer, times(1)).getLoadLineItemDataListByLoadID(LOAD_ID);
        verify(eBSPoReceivingServer, times(1)).addPOExpectedReceipt(expectedReceiptMessageData);
        verify(systemGateway, times(1)).publishHostExpectedReceiptEvent(hostToWrxData.getMessage());
    }
}
