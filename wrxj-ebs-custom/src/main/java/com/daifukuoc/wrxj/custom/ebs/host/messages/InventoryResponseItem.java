package com.daifukuoc.wrxj.custom.ebs.host.messages;

import java.util.Date;

/**
 * 
 * @author BT
 *
 */
public class InventoryResponseItem {

    String loadId = ""; // Tray id = Container id
    String GlobalID = ""; // Global Id
    String lineId = ""; // Item id = bag id = barcode = sBarcode
    String flightNumber = "";
    Date flightSTD =  new Date();
    String locationID = ""; // locationID
    String WarehouseID = ""; // WH ID
    
    
    public String getLoadId() {
        return loadId;
    }

    public void setLoadId(String loadId) {
        this.loadId = loadId;
    }

    public String getGlobalID() {
        return GlobalID;
    }

    public void setGlobalID(String globalID) {
        this.GlobalID = globalID;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

	public String getFlightNumber() {
		return flightNumber;
	}

	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}

	public Date getFlightSTD() {
		return flightSTD;
	}

	public void setFlightSTD(Date flightSTD) {
		this.flightSTD = flightSTD;
	}

	public String getLocationID() {
		return locationID;
	}

	public void setLocationID(String locationID) {
		this.locationID = locationID;
	}
	public String getWarehouseID()
    {
    	return WarehouseID;
    }
    public void setWarehouseID(String warehouseID)
    {
    	this.WarehouseID = warehouseID;
    }


}
