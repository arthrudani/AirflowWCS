package com.daifukuoc.wrxj.custom.ebs.plc.messages.outbound;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

@ExtendWith(MockitoExtension.class)
public class PLCItemArrivedAckMessageTest {

    @Test
    public void msgParseTest()
    {
/*
    	String msg = "151,1,0";
    	PLCStandardAckMessage mbMsg = new PLCStandardAckMessage();
    	
    	mbMsg.setStatus("0");
    	mbMsg.setMessageType(PLCConstants.PLC_ITEM_ARRIVED_ACK_MSG_TYPE);
    	
    	mbMsg.constructSendMessagetoPlc();

    	assertTrue(!mbMsg.getSendMessage().isBlank() );
    	assertTrue(mbMsg.getSendMessage().equals(msg) );
		assertTrue(mbMsg.getStatus().equals("0"));
		
		*/
    }
}
