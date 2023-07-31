package com.daifukuoc.wrxj.custom.ebs.plc.messages.decoder;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageHeader;

public interface StandardMessageDecoder {

	/**
	 * Loads the associated message handler using provided message Id parameter 
	 * which decodes / converts the byte[] message data to a comma separated string
	 * @param iMsgId
	 * @param iMsgLength
	 * @param bMsg
	 * @return
	 */
	String decode(PLCMessageHeader mpMessageHeader,byte[] bMsg);
}
