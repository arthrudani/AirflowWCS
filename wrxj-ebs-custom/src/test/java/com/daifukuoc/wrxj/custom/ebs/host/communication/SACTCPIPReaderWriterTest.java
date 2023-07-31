package com.daifukuoc.wrxj.custom.ebs.host.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.channels.SocketChannel;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;

class SACTCPIPReaderWriterTest {
    
    MockedStatic<Factory> mockedFactory;
    
    @Mock
    Properties config;
    
    @Mock
    SocketChannel socketChannel;
    
    @Mock
    TCPIPLogger logger;

    SACTCPIPReaderWriter tcpReaderWriter;

    @BeforeEach
    void setUp() throws Exception {
        mockedFactory = Mockito.mockStatic(Factory.class);
        tcpReaderWriter = new SACTCPIPReaderWriter();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedFactory.close();
    }
 
    @Test
    void shouldReturnHeartBeatMessageWhenGetHeartBeatMessageIsCalled() throws TCPIPCommException {
        byte[] keepAliveMessage = tcpReaderWriter.getHeartBeatMessage();
        
        assertEquals(SACControlMessage.KEEPALIVE_MSG_LEN, keepAliveMessage.length);
    }
}
