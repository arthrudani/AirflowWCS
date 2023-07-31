package com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder;

public interface StandardMessageEncoder {

	/**
	 * Encodes / converts the string message to the byte 
	 * @param sData
	 * @return
	 */
	public byte[] encode(String sData);
}
