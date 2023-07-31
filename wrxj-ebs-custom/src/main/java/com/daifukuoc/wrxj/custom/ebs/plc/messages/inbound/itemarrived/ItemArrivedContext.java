package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemarrived;

import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

/**
 * The context class for {@link PLCItemArrivedTransaction}. Should be instantiated by {@link ItemArrivedMessage}.
 * 
 * @author ST
 *
 */
public class ItemArrivedContext implements EBSTransactionContext {
    private final StationData stationData;
    private LoadData loadData;
    private LoadLineItemData loadLineItemData;
    private final String stationId;
    private final String orderId;
    private final String loadId;
    private final String globalId;
    private final String lineId;
    private final String serialNumber;
    private final String deviceId;
    private boolean success = false;

    /**
     * Constructor with values for {@link PLCItemArrivedTransaction}.
     * 
     * @param stationData
     * @param loadData
     * @param loadLineItemData
     * @param stationId
     * @param orderId
     * @param loadId
     * @param globalId
     * @param lineId
     * @param serialNumber
     * @param deviceId
     */
    public ItemArrivedContext(StationData stationData, LoadData loadData, LoadLineItemData loadLineItemData,
            String stationId, String orderId, String loadId, String globalId, String lineId, String serialNumber,
            String deviceId) {
        super();
        this.stationData = stationData;
        this.loadData = loadData;
        this.loadLineItemData = loadLineItemData;
        this.stationId = stationId;
        this.orderId = orderId;
        this.loadId = loadId;
        this.globalId = globalId;
        this.lineId = lineId;
        this.serialNumber = serialNumber;
        this.deviceId = deviceId;
    }

    public StationData getStationData() {
        return stationData;
    }

    public String getStationId() {
        return stationId;
    }

    public LoadData getLoadData() {
        return loadData;
    }

    public LoadLineItemData getLoadLineItemData() {
        return loadLineItemData;
    }

    public String getOrderId() {
        return orderId;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public void setLoadData(LoadData loadData) {
    	this.loadData = loadData;
    }
    
    public void setLoadLineItemData(LoadLineItemData loadLineItemData) {
    	this.loadLineItemData = loadLineItemData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, globalId, lineId, loadData, loadId, loadLineItemData, orderId, serialNumber,
                stationData, stationId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemArrivedContext other = (ItemArrivedContext) obj;
        return Objects.equals(deviceId, other.deviceId) && Objects.equals(globalId, other.globalId)
                && Objects.equals(lineId, other.lineId) && Objects.equals(loadData, other.loadData)
                && Objects.equals(loadId, other.loadId) && Objects.equals(loadLineItemData, other.loadLineItemData)
                && Objects.equals(orderId, other.orderId) && Objects.equals(serialNumber, other.serialNumber)
                && Objects.equals(stationData, other.stationData) && Objects.equals(stationId, other.stationId);
    }

    @Override
    public String toString() {
        return "PLCItemArrivedContext [stationData=" + stationData + ", loadData=" + loadData + ", loadLineItemData="
                + loadLineItemData + ", stationId=" + stationId + ", orderId=" + orderId + ", loadId=" + loadId
                + ", globalId=" + globalId + ", lineId=" + lineId + ", serialNumber=" + serialNumber + ", deviceId="
                + deviceId + "]";
    }
}
