package com.daifukuoc.wrxj.custom.ebs.host.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.channels.SocketChannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostMessageIntegrator;
import com.daifukuamerica.wrxj.jdbc.DBException;

@ExtendWith(MockitoExtension.class)
class SACTCPIPReadEventImplTest {

    private static final String HOST_NAME = "HOST";
    private static final String GROUP_NAME = "GROUP";
    private static final String COLLABORATOR_NAME = HostMessageIntegrator.class.getSimpleName();
    private static final String NOT_CSV_FORMAT = "";
    private static final String HEART_BEAT_MESSAGE                        = "18,1000,1,2222,3,4,5,0,1";
    private static final String VALID_SEQUENCE_NUMBER = "32767";
    private static final String VALID_EXPECTED_RECEIPT_MESSAGE            = "84," + VALID_SEQUENCE_NUMBER + ",52,2222,3,4,5,0,999,1002,10021002,BAG1002,FL100,20221213000000,20221213000000,3600,1,1";
    private static final String VALID_RETRIEVAL_ORDER_MESSAGE             = "42," + VALID_SEQUENCE_NUMBER + ",55,2222,3,4,5,0,999,FL100,20221213000000,0";
    private static final String VALID_FLIGHT_DATA_UPDATE_MESSAGE          = "57," + VALID_SEQUENCE_NUMBER + ",57,2222,3,4,5,0,FL100,20221201001122,20221202112233,3700";
    private static final String INVALID_MESSAGE_WITH_UNKNOWN_MESSAGE_TYPE = "55," + VALID_SEQUENCE_NUMBER + ",999999,0,0,0,0,0,XXXX";
   
    MockedStatic<Factory> mockedFactory;

    @Mock
    TCPIPLogger logger;
    
    @Mock
    Controller systemGateway;
    
    @Mock
    SocketChannel socketChannel;

    @Mock
    HostToWrxData hostToWrxData;
    
    @Mock
    HostInDelegate hostInDelegate;
    
    @Mock
    StandardHostServer standardHostServer;
    
    SACTCPIPReadEventImpl reader;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        mockedFactory.when(() -> Factory.create(HostToWrxData.class)).thenReturn(hostToWrxData);
        mockedFactory.when(() -> Factory.create(HostInDelegate.class)).thenReturn(hostInDelegate);
        mockedFactory.when(() -> Factory.create(StandardHostServer.class, SACTCPIPReadEventImpl.STANDARD_HOST_SERVER_KEY_NAME)).thenReturn(standardHostServer);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }

    @Test
    void shouldDoNothingWhenReceivedMessageIsNotCSV() {
        reader = new SACTCPIPReadEventImpl(HOST_NAME, GROUP_NAME, logger);
        reader.setupCollaboratorNotification(systemGateway, COLLABORATOR_NAME);
        reader.receivedData(socketChannel, NOT_CSV_FORMAT);
    }

    @Test
    void shouldDoNothingWhenReceivedMessageIsHeartBeatMessage() {
        reader = new SACTCPIPReadEventImpl(HOST_NAME, GROUP_NAME, logger);
        reader.setupCollaboratorNotification(systemGateway, COLLABORATOR_NAME);
        reader.receivedData(socketChannel, HEART_BEAT_MESSAGE);
    }
    
    @Test
    void shouldSaveTheReceivedMessageToDBAndThenPublishHostMessageReceivedEventEvenWhenInvalidMessageIsReceived() throws DBException {
        int sequenceNumber = Integer.parseInt(VALID_SEQUENCE_NUMBER);
        when(hostToWrxData.getOriginalMessageSequence()).thenReturn(Integer.parseInt(VALID_SEQUENCE_NUMBER));
        
        doAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                assertEquals(sequenceNumber, (short)invocation.getArguments()[0]);
                return (Integer) invocation.getArguments()[0];
            }
        }).when(hostToWrxData).setOriginalMessageSequence(anyInt());  
        
        reader = new SACTCPIPReadEventImpl(HOST_NAME, GROUP_NAME, logger);
        reader.setupCollaboratorNotification(systemGateway, COLLABORATOR_NAME);
        reader.receivedData(socketChannel, INVALID_MESSAGE_WITH_UNKNOWN_MESSAGE_TYPE);
        
        // TODO: Even if the received message is invalid, it's still stored in HostToWrx table. Is this what we want?
        verify(hostToWrxData, times(1)).setHostName(HOST_NAME);
        verify(hostToWrxData, times(1)).setOriginalMessageSequence(anyInt());
        verify(hostToWrxData, times(1)).getOriginalMessageSequence();
        verify(hostInDelegate, times(1)).setInfo(hostToWrxData);
        verify(standardHostServer, times(1)).addToDataQueue(eq(sequenceNumber), eq(hostInDelegate));
        verify(systemGateway, times(1)).publishHostMesgReceiveEvent(eq(""), eq(0), eq(COLLABORATOR_NAME));
    }
    
    @Test
    void shouldSaveTheReceivedMessageToDBAndThenPublishHostMessageReceivedEventWhenValidExpectedReceiptMessageIsReceived() throws DBException {
        int sequenceNumber = Integer.parseInt(VALID_SEQUENCE_NUMBER);
        when(hostToWrxData.getOriginalMessageSequence()).thenReturn(Integer.parseInt(VALID_SEQUENCE_NUMBER));
        
        doAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                assertEquals(sequenceNumber, (short)invocation.getArguments()[0]);
                return (Integer) invocation.getArguments()[0];
            }
        }).when(hostToWrxData).setOriginalMessageSequence(anyInt());        
        
        reader = new SACTCPIPReadEventImpl(HOST_NAME, GROUP_NAME, logger);
        reader.setupCollaboratorNotification(systemGateway, COLLABORATOR_NAME);
        reader.receivedData(socketChannel, VALID_EXPECTED_RECEIPT_MESSAGE);
        
        verify(hostToWrxData, times(1)).setHostName(HOST_NAME);
        verify(hostToWrxData, times(1)).setMessageIdentifier(MessageType.EXPECTEDRECEIPTMESSAGE.name());
        verify(hostToWrxData, times(1)).setMessage(VALID_EXPECTED_RECEIPT_MESSAGE);
        verify(hostToWrxData, times(1)).setMessageBytes(VALID_EXPECTED_RECEIPT_MESSAGE.getBytes());
        verify(hostToWrxData, times(1)).setOriginalMessageSequence(anyInt());
        verify(hostToWrxData, times(1)).getOriginalMessageSequence();
        verify(hostInDelegate, times(1)).setInfo(hostToWrxData);
        verify(standardHostServer, times(1)).addToDataQueue(eq(sequenceNumber), eq(hostInDelegate));
        verify(systemGateway, times(1)).publishHostMesgReceiveEvent(eq(""), eq(0), eq(COLLABORATOR_NAME));
    }
    
    @Test
    void shouldSaveTheReceivedMessageToDBAndThenPublishHostMessageReceivedEventWhenValidRetrievalOrderMessageIsReceived() throws DBException {
        int sequenceNumber = Integer.parseInt(VALID_SEQUENCE_NUMBER);
        when(hostToWrxData.getOriginalMessageSequence()).thenReturn(Integer.parseInt(VALID_SEQUENCE_NUMBER));
        
        doAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                assertEquals(sequenceNumber, (short)invocation.getArguments()[0]);
                return (Integer) invocation.getArguments()[0];
            }
        }).when(hostToWrxData).setOriginalMessageSequence(anyInt());    
        
        reader = new SACTCPIPReadEventImpl(HOST_NAME, GROUP_NAME, logger);
        reader.setupCollaboratorNotification(systemGateway, COLLABORATOR_NAME);
        reader.receivedData(socketChannel, VALID_RETRIEVAL_ORDER_MESSAGE);
        
        verify(hostToWrxData, times(1)).setHostName(HOST_NAME);
        verify(hostToWrxData, times(1)).setMessageIdentifier(MessageType.RETRIEVALORDERMESSAGE.name());
        verify(hostToWrxData, times(1)).setMessage(VALID_RETRIEVAL_ORDER_MESSAGE);
        verify(hostToWrxData, times(1)).setMessageBytes(VALID_RETRIEVAL_ORDER_MESSAGE.getBytes());
        verify(hostToWrxData, times(1)).setOriginalMessageSequence(anyInt());
        verify(hostToWrxData, times(1)).getOriginalMessageSequence();
        verify(hostInDelegate, times(1)).setInfo(hostToWrxData);
        verify(standardHostServer, times(1)).addToDataQueue(eq(sequenceNumber), eq(hostInDelegate));
        verify(systemGateway, times(1)).publishHostMesgReceiveEvent(eq(""), eq(0), eq(COLLABORATOR_NAME));
    }
    
    @Test
    void shouldSaveTheReceivedMessageToDBAndThenPublishHostMessageReceivedEventWhenValidFlightDateUpdateMessageIsReceived() throws DBException {
        int sequenceNumber = Integer.parseInt(VALID_SEQUENCE_NUMBER);
        when(hostToWrxData.getOriginalMessageSequence()).thenReturn(Integer.parseInt(VALID_SEQUENCE_NUMBER));
        
        doAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                assertEquals(sequenceNumber, (short)invocation.getArguments()[0]);
                return (Integer) invocation.getArguments()[0];
            }
        }).when(hostToWrxData).setOriginalMessageSequence(anyInt());    
        
        reader = new SACTCPIPReadEventImpl(HOST_NAME, GROUP_NAME, logger);
        reader.setupCollaboratorNotification(systemGateway, COLLABORATOR_NAME);
        reader.receivedData(socketChannel, VALID_FLIGHT_DATA_UPDATE_MESSAGE);
        
        verify(hostToWrxData, times(1)).setHostName(HOST_NAME);
        verify(hostToWrxData, times(1)).setMessageIdentifier(MessageType.FLIGHTDATAUPDATEMESSAGE.name());
        verify(hostToWrxData, times(1)).setMessage(VALID_FLIGHT_DATA_UPDATE_MESSAGE);
        verify(hostToWrxData, times(1)).setMessageBytes(VALID_FLIGHT_DATA_UPDATE_MESSAGE.getBytes());
        verify(hostToWrxData, times(1)).setOriginalMessageSequence(anyInt());
        verify(hostToWrxData, times(1)).getOriginalMessageSequence();
        verify(hostInDelegate, times(1)).setInfo(hostToWrxData);
        verify(standardHostServer, times(1)).addToDataQueue(eq(sequenceNumber), eq(hostInDelegate));
        verify(systemGateway, times(1)).publishHostMesgReceiveEvent(eq(""), eq(0), eq(COLLABORATOR_NAME));
    }
}
