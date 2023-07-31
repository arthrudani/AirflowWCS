package com.daifukuoc.wrxj.custom.ebs.ipc;

import com.daifukuamerica.wrxj.ipc.MessageEventConsts;

/**
 * Interface used for all the Event, topic message constants required for the messages
 * @author Administrator
 *
 */
public interface EBSMessageEventConsts extends MessageEventConsts {
	  /**
	   * Event category for handling PLC message receipt notification.
	   */
	  public static final int PLC_MESG_RECV_EVENT_TYPE = 22;

	  /**
	   * Event category for handling PLC message send notification.
	   */
	  public static final int PLC_MESG_SEND_EVENT_TYPE = 23;
	  
	  /**
	   * Event text for PLC message receive event text
	   */
	  public static final String PLC_MESG_RECV_EVENT_TEXT = "PlcRecv";

	  /**
	   * Event text for PLC message send event text
	   */
	  public static final String PLC_MESG_SEND_EVENT_TEXT = "PlcSend";
	  
	  public static final String PLC_MESG_RECV_TEXT = 
			    PLC_MESG_RECV_EVENT_TEXT + SUB_EVENT_TEXT;
}
