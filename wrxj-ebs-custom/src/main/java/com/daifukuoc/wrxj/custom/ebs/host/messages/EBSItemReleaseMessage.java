/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.util.Date;

import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

/**
 * Class to setup an Item Release (similar to Load Arrival) notification to the
 * host when an Item Release at a station.
 *
 * @author N.H.C
 * @since 18-Aug-2022
 */
public class EBSItemReleaseMessage extends MessageOut {

	private final String TRAN_DATE_NAME = "dTransactionTime";
	private final String ORDERID_NAME = "sOrderID";
	private final String LOADID_NAME = "sLoadID";
	private final String LINEID_NAME = "sLineID";
	private final String STATION_NAME = "sStationName";
	private final String STATUS_NAME = "sStatus";
	private final String GLOBALID_NAME = "sGlobalID";

	public EBSItemReleaseMessage() {
		messageFields = new ColumnObject[] { 
				new ColumnObject(TRAN_DATE_NAME, new Date()),
				new ColumnObject(ORDERID_NAME, ""),
				new ColumnObject(LOADID_NAME, ""),
				new ColumnObject(GLOBALID_NAME, ""),
				new ColumnObject(LINEID_NAME, ""),
				new ColumnObject(STATION_NAME, ""),
				new ColumnObject(STATUS_NAME, 1)//1=Normal retrieval(SAC), 2=Operator retrieval, 3=No room to store,4=Unknown tray, 5=Error recovery(Manual)
		};

		msgfmt = MessageFormatterFactory.getInstance();
		enumMessageName = MessageOutNames.ITEM_RELEASE;
	}

	public void setTransactionTime(Date ipTranDate) {
		ColumnObject.modify(TRAN_DATE_NAME, ipTranDate, messageFields);
	}
	
	public void setLoadID(String isLoadID) {
		ColumnObject.modify(LOADID_NAME, isLoadID, messageFields);
	}
	
	public void setOrderID(String sOrderID) {
		ColumnObject.modify(ORDERID_NAME, sOrderID, messageFields);
	}	

	public void setLineID(String sLineID) {
		ColumnObject.modify(LINEID_NAME, sLineID, messageFields);
	}

	public void setArrivalStation(String isArrivalStation) {
		ColumnObject.modify(STATION_NAME, isArrivalStation, messageFields);
	}
	
	public void setStatus(int sStatus) {
		ColumnObject.modify(STATUS_NAME, sStatus, messageFields);
	}
	
	public void setGlobalID(String sGlobalID) {
		ColumnObject.modify(GLOBALID_NAME, sGlobalID, messageFields);
	}

}
