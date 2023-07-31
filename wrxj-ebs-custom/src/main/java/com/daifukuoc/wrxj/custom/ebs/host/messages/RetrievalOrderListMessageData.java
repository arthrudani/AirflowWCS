/**
 * 
 */
package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.log.Logger;

/**
 * @author Administrator
 *
 */
public class RetrievalOrderListMessageData extends AbstractSKDCData {
	
	protected Logger mpLogger = Logger.getLogger();
	SACMessageHeader oHeader = new SACMessageHeader();
	String sOrderID = ""; // Global Id
	String sLot = ""; // Flight#
	String sExpectedDate = ""; // sFlightSchedule
	int iNumberOfBags = 0; // Number of Bags to retrieve ( 0=All )
	RetrievalOrderItemMessageData retrievalOrderListData = null;
	public List<RetrievalOrderItemMessageData> listOrderData = null;			
	int iArrayLength = 0;
	String sStrArray = "";
	short iMsgType = 0;

	/**
	 * 
	 */
	public RetrievalOrderListMessageData() {
		super();
	}

	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		// TODO Auto-generated method stub
		return false;
	}

	// In the Retrieval Order there are two types of Retrieval Order
	// need to handle in the following methods
	// refer to :
	// http://softwaredoc.daifukuoc.com/airflowwcs/technical-guide/messages/sac-messages/retrieval-order.html

	// 1. parse the SAC message to data, example (Message header + Message body) :
	// <5,0,0,0,0,0,41,0,OrderId1,QFA 1234A,20221201134500,0 >
	// 2. parse the SAC message to data, example (Message header + Message
	// body{OrderId1,Array Length, Array of object [Tray ID, Global ID, Item ID,
	// Final Sort Location ID]}) :
	// <4,0,0,0,0,0,41,0,Ord01,1,20221201134500,>
	public boolean parse(String sMsg) {

		mpLogger.logDebug("RetrievalOrderMessage parseing :"+ sMsg);
		
		if (!StringUtils.isBlank(sMsg)) {
			String[] splitedMsg = sMsg.split(",");
			
			if (splitedMsg != null) {
				iMsgType = Short.parseShort(splitedMsg[0]);
				
				try {
					switch (iMsgType) {
					// if the msg type [4]
					case SACControlMessage.RETRIEVAL_FLIGHT_REQUEST_MSG_TYPE:
						setDatafromMessageHeader(splitedMsg);
						sOrderID = splitedMsg[8]; // Global Id
						sLot = splitedMsg[9]; // FlightNo
						sExpectedDate = splitedMsg[10]; // sFlightSchedule
						iNumberOfBags = Integer.parseInt(splitedMsg[11]);
						break;
						
					case SACControlMessage.INVENTORY_REQUEST_BY_FLIGHT_MSG_TYPE:
						setDatafromMessageHeader(splitedMsg);
						sOrderID = splitedMsg[8]; // Global Id
						sLot = splitedMsg[9]; // FlightNo
						sExpectedDate = splitedMsg[10]; // sFlightSchedule
						break;

					case SACControlMessage.RETRIEVAL_ITEM_REQUEST_MSG_TYPE:
						setDatafromMessageHeader(splitedMsg);

						sOrderID = splitedMsg[8]; // Global Id
						iArrayLength = Integer.parseInt(splitedMsg[9]); // iArrayLength
						int currentIndex = 9;						
						if (iArrayLength > 0) {
	
							listOrderData = new ArrayList<RetrievalOrderItemMessageData>();
							for (int x = 0; x < iArrayLength; x++) {
								retrievalOrderListData = new RetrievalOrderItemMessageData();
								currentIndex++;
								retrievalOrderListData.loadId = splitedMsg[currentIndex]; // tray-ContainerId
								currentIndex++;
								retrievalOrderListData.globalId = splitedMsg[currentIndex]; // Global Id
								currentIndex++;
								retrievalOrderListData.lineId = splitedMsg[currentIndex]; // sBarcode
								currentIndex++;
								retrievalOrderListData.finalSortLocation = splitedMsg[currentIndex]; // sFinalSortLocationId	
								listOrderData.add(retrievalOrderListData);
							}
						}
						break;
					}
				} catch (Exception e) {
					mpLogger.logError("error while Parsing the RetrievalOrderMessage, message :"+sMsg+", Error: "+e.getMessage());
					return false;					
				}
			} else {
				
				return false;
			}
		} else {
			mpLogger.logError("No Retrieval Order Message found for parse");
			return false;
		}

		return true;
	}

	// read the value from the splitedMsg string and set the data to message header
	// object
	private void setDatafromMessageHeader(String[] splitedMsg) {

	    oHeader.setMsgLength(Integer.parseInt(splitedMsg[0]));
	    oHeader.setSeqNo(Integer.parseInt(splitedMsg[1]));
		oHeader.setMsgType(Short.parseShort(splitedMsg[2]));
		oHeader.setEquipmentId(splitedMsg[3]);
		oHeader.setHours(Integer.parseInt(splitedMsg[4]));
		oHeader.setMinutes(Integer.parseInt(splitedMsg[5]));
		oHeader.setMilliSeconds(Integer.parseInt(splitedMsg[6]));
		oHeader.setMsgVersion(Integer.parseInt(splitedMsg[7]));
	}

	/* set */
	public void setNumberOfBags(int sN0OfBags) {
		iNumberOfBags = sN0OfBags;
	}

	public void setExpectedDate(String vsExpectedDate) throws ParseException {
		sExpectedDate = vsExpectedDate;
	}

	public void setOrderID(String vsOrderID) {
		sOrderID = vsOrderID;
	}

	public void setLot(String vsLot) {
		sLot = vsLot;
	}

	/* get */
	public int getsNumberOfBags() {
		return (iNumberOfBags);
	}

	public String getExpectedDate() {
		return (sExpectedDate);
	}

	public String getOrderID() {
		return (sOrderID);
	}

	public String getLot() {
		return (sLot);
	}

	public short getMsgType() {
		return iMsgType;
	}

}
