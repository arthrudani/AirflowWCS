package com.daifukuoc.wrxj.custom.ebs.host.messages;

/**
 * The list of bags included retrieval order list message
 * 
 * @author LK
 *
 */
public class RetrievalOrderItemList {

    String loadId = ""; // Tray id = Container id
    String orderID = ""; // Global Id
    String lineId = ""; // Item id = bag id = barcode = sBarcode
    String finalSortLocation = ""; // FinalSortLocationId

    public String getLoadId() {
        return loadId;
    }

    public void setLoadId(String loadId) {
        this.loadId = loadId;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getFinalSortLocation() {
        return finalSortLocation;
    }

    public void setFinalSortLocation(String finalSortLocation) {
        this.finalSortLocation = finalSortLocation;
    }

}
