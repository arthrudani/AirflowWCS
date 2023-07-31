package com.daifukuoc.wrxj.custom.ebs.plc.messages.util;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;

@ExtendWith(MockitoExtension.class)
public class PLCMessageUtilTest {
    
    @Test
    public void shouldFailIfCannotBuildMessageHeader()
    {
    	//short mwMsgType, int mnMsgLength, int equimpmentId, short mnSeqNum , short mwMsgVer)
    	byte[] msgHeader = PLCMessageUtil.buildMessageHeader(PLCConstants.KEEPALIVE_MSG_TYPE,PLCConstants.KEEPALIVE_MSG_BODY_LEN,1111, (short)1,(short)1);
    	assertTrue(msgHeader != null );
		assertTrue(msgHeader.length == PLCConstants.MSG_HEADER_LEN );
    	
    }
    
    @Test
    public void isNumericTest()
    {
    	assertTrue(PLCMessageUtil.isNumeric("22"));
    	assertTrue(PLCMessageUtil.isNumeric("5.05"));
    	assertTrue(PLCMessageUtil.isNumeric("-200"));

    	assertFalse(PLCMessageUtil.isNumeric(null));
    	assertFalse(PLCMessageUtil.isNumeric("abc"));
    }
    
    @Test
    public void messageHeaderIsBuildAccoringToTheGivenValuesAndCalculatedValues() {
    	byte[] builtMessaheHeader = PLCMessageUtil.buildMessageHeader((short)3, 18, 32, (short)24, (short)1);

    	// Message Length (2 bytes)
    	assertEquals(0, builtMessaheHeader[0]); 
    	assertEquals(34, builtMessaheHeader[1]);
    	
    	// Message Sequence No (2 bytes)
    	assertEquals(0, builtMessaheHeader[2]);
    	assertEquals(24, builtMessaheHeader[3]);
    	
    	// Message Type (2 bytes)
    	assertEquals(0, builtMessaheHeader[4]);
    	assertEquals(3, builtMessaheHeader[5]);
    	
    	// Equipment ID (4 bytes)
    	assertEquals(0, builtMessaheHeader[6]);
    	assertEquals(0, builtMessaheHeader[7]);
    	assertEquals(0, builtMessaheHeader[8]);
    	assertEquals(32, builtMessaheHeader[9]);
    	
    	// index 10 - 13 is not checked as time stamp is stored.
    	
    	// Message Version (2 bytes)
    	assertEquals(0, builtMessaheHeader[14]);
    	assertEquals(1, builtMessaheHeader[15]);
    }
}