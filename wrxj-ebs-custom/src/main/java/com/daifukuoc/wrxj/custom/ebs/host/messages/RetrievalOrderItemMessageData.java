package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;

public class RetrievalOrderItemMessageData extends AbstractSKDCData {
	// The received whole message
	private String receivedMessage = "";
	// Header
	private SACMessageHeader header = new SACMessageHeader();
	String OrderId = "";
	String numberOfBags = ""; 
	String loadId = ""; // Tray id = Container id
	String globalId = ""; // Global Id
	String lineId = ""; // Item id = bag id = barcode = sBarcode
	String finalSortLocation = ""; // FinalSortLocationId
	List<RetrievalOrderItemList> list = null;
	
	public boolean parse(String receivedMessage) {
		this.receivedMessage = receivedMessage;

		if (receivedMessage != null && !receivedMessage.isEmpty()) {
			String[] splitedMsg = receivedMessage.split(",");
			if (splitedMsg != null) {

				// Split the initial 8 fields into the header
				header.setMsgLength(Integer.parseInt(splitedMsg[0]));
				header.setSeqNo(Integer.parseInt(splitedMsg[1]));
				header.setMsgType(Short.parseShort(splitedMsg[2]));
				header.setEquipmentId(splitedMsg[3]);
				header.setHours(Integer.parseInt(splitedMsg[4]));
				header.setMinutes(Integer.parseInt(splitedMsg[5]));
				header.setMilliSeconds(Integer.parseInt(splitedMsg[6]));
				header.setMsgVersion(Integer.parseInt(splitedMsg[7]));

				OrderId = splitedMsg[8];
				numberOfBags = splitedMsg[9];
				list = new ArrayList<>(Integer.parseInt(numberOfBags));
				int i=0,j=10;
				while(i<Integer.parseInt(numberOfBags) && j< splitedMsg.length) {
					RetrievalOrderItemList li = new RetrievalOrderItemList();
					loadId = splitedMsg[j];
					li.setLoadId(splitedMsg[j]);
					li.setOrderID(splitedMsg[j+1]);
					li.setLineId(splitedMsg[j+2]);
					li.setFinalSortLocation(splitedMsg[j+3]);
					globalId = splitedMsg[j+1];
					lineId = splitedMsg[j+2];
					finalSortLocation = splitedMsg[j+3];
					i++;
					j+=4;
					list.add(li);
				}

				return isValid();
			}
		}

		return false;
	}

	public boolean isValid() {
		if(Integer.parseInt(numberOfBags)>0 && !list.isEmpty()) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "RetrievalOrderItemMessageData [receivedMessage=" + receivedMessage + ", header=" + header + ", OrderId="
				+ OrderId + ", numberOfBags=" + numberOfBags + ", loadId=" + loadId + ", globalId=" + globalId
				+ ", lineId=" + lineId + ", finalSortLocation=" + finalSortLocation + ", list=" + list + "]";
	}

	public String getReceivedMessage() {
		return receivedMessage;
	}

	public SACMessageHeader getHeader() {
		return header;
	}

	public String getLoadId() {
		return loadId;
	}

	public String getGlobalId() {
		return globalId;
	}

	public String getLineId() {
		return lineId;
	}

	public String getFinalSortLocation() {
		return finalSortLocation;
	}
	
	public String getOrderId() {
		return OrderId;
	}

	public String getNumberOfBags() {
		return numberOfBags;
	}

	public List<RetrievalOrderItemList> getList() {
		return list;
	}

	@Override
	public boolean equals(AbstractSKDCData eskdata) {
		return equals((Object) eskdata);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RetrievalOrderItemMessageData other = (RetrievalOrderItemMessageData) obj;
		return Objects.equals(loadId, other.loadId) && Objects.equals(header, other.header)
				&& Objects.equals(finalSortLocation, other.finalSortLocation)
				&& Objects.equals(globalId, other.globalId) && Objects.equals(lineId, other.lineId)
				&& Objects.equals(receivedMessage, other.receivedMessage)
				&& Objects.equals(OrderId, other.OrderId) && Objects.equals(list, other.list);
	}

}
