package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.util.Date;

import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

public class EBSStoreCompletionNotifyMessage extends MessageOut {
	
	  protected final String TRAN_DATE_NAME  			= "dTransactionTime";
	  protected final String ORDERID_NAME    			= "sOrderID"; //Request Order Id
	  protected final String LOADID_NAME     			= "sLoadID";//Tray / Container Id
	  protected final String GLOBALID_NAME				= "sGlobalID";//Global Id
	  protected final String LINEID_NAME     			= "sLineID";//Bag barcode
	  protected final String ZONEID_NAME     			= "sZoneID";//Zone Id
	  protected final String ENTRANCESTATIONID_NAME     = "sEntranceStationID";//Gate Id
	  protected final String STATUS_NAME     			= "iStatus";//Status
	  protected final String MSGID_NAME    				= "sMsgID"; //response msg id = 22
	  	
	  String sOrderID 				= "";	
	  String sLoadID				= "";	//Tray,Container Id
	  String sGlobalID				= "";	//Global Id
	  String sLineID				= "";   // Bag barcode
	  String sZoneID				= "";   //Zone Id
	  String sEntranceStationID		= "";   //Entrance Station ID ( 0= Not able to store for any reasons)     	
	  int iStatus = 1;					//Status (0=Not Available, 1=Succeed, 2= Error)
	  int iMsgID =	SACControlMessage.StoreCompletionNotify.MSG_TYPE ;	

	  public EBSStoreCompletionNotifyMessage()
	  {
	    messageFields = new ColumnObject[]
	    {
	        new ColumnObject(MSGID_NAME, SACControlMessage.StoreCompletionNotify.MSG_TYPE),
	        new ColumnObject(ORDERID_NAME, sOrderID),
	        new ColumnObject(LOADID_NAME, sLoadID),
	        new ColumnObject(GLOBALID_NAME, sGlobalID),
	        new ColumnObject(LINEID_NAME, sLineID),
	        new ColumnObject(ZONEID_NAME, sZoneID),
	        new ColumnObject(ENTRANCESTATIONID_NAME, sEntranceStationID),
	        new ColumnObject(STATUS_NAME,1)
	    };
	    msgfmt = MessageFormatterFactory.getInstance();
	    enumMessageName = MessageOutNames.STORE_COMPLETE;
	  }

	  public void setTransactionTime(Date dAddDateTime)
	  {
	    ColumnObject.modify(TRAN_DATE_NAME, dAddDateTime, messageFields);
	  }
	  public void setOrderID(String sOrderID)
	  {
	    ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
	  }
	  public void setLoadID(String sLoadID)
	  {
	    ColumnObject.modify(LOADID_NAME, sLoadID, messageFields);
	  }
	  
	  public void setGlobalID(String sGlobalID)
	  {
	    ColumnObject.modify(GLOBALID_NAME, sGlobalID, messageFields);
	  }
	  public void setLineID(String sLineID)
	  {
	    ColumnObject.modify(LINEID_NAME, sLineID, messageFields);
	  }	  
	  public void setZoneID(String sZoneID)
	  {
	    ColumnObject.modify(ZONEID_NAME, sZoneID, messageFields);
	  }
	  public void setLocation(String sEntranceLocationID)
	  {
	    ColumnObject.modify(ENTRANCESTATIONID_NAME, sEntranceLocationID, messageFields);
	  }
	  public void setStatus(int iStatus)
	  {
	    ColumnObject.modify(STATUS_NAME, iStatus, messageFields);
	  }

	public String getOrderID() {
		return sOrderID;
	}

	public String getLoadID() {
		return sLoadID;
	}
	public String getGlobalID() {
		return sGlobalID;
	}
	public String getLineID() {
		return sLineID;
	}

	public String getZoneID() {
		return sZoneID;
	}

	public String getEntranceStationID() {
		return sEntranceStationID;
	}

	public int getStatus() {
		return iStatus;
	}

	public int getiMsgID() {
		return iMsgID;
	}


}
