package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored;

import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

/**
 * The context class for {@link PLCItemStoredTransaction}
 * 
 * @author ST
 *
 */
public class ItemStoredContext implements EBSTransactionContext {
    private String orderId;
    private String loadId;
    private String addressId;
    private int status;
    private String globalId;
    private String lineId;
    private boolean success = false;
    private LoadData loadData;
    private LoadLineItemData loadLineItemData;
    private String deviceId;

    public ItemStoredContext(String orderId, String loadId, String addressId, int status, String globalId,
            String lineId, String deviceId) {
        super();
        this.orderId = orderId;
        this.loadId = loadId;
        this.addressId = addressId;
        this.status = status;
        this.globalId = globalId;
        this.lineId = lineId;
        this.deviceId = deviceId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getLoadId() {
        return loadId;
    }

    public String getAddressId() {
        return addressId;
    }

    public int getStatus() {
        return status;
    }

    public String getGlobalId() {
        return globalId;
    }

    public String getLineId() {
        return lineId;
    }

    public LoadData getLoadData() {
        return loadData;
    }

    public void setLoadData(LoadData loadData) {
        this.loadData = loadData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public LoadLineItemData getLoadLineItemData() {
		return loadLineItemData;
	}

	public void setLoadLineItemData(LoadLineItemData loadLineItemData) {
		this.loadLineItemData = loadLineItemData;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public void setAddressId(String addressId) {
		this.addressId = addressId;
	}

	@Override
    public int hashCode() {
        return Objects.hash(addressId, globalId, lineId, loadId, orderId, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemStoredContext other = (ItemStoredContext) obj;
        return Objects.equals(addressId, other.addressId) && Objects.equals(globalId, other.globalId)
                && Objects.equals(lineId, other.lineId) && Objects.equals(loadId, other.loadId)
                && Objects.equals(orderId, other.orderId) && status == other.status;
    }

    @Override
    public String toString() {
        return "PLCItemStoredContext [orderId=" + orderId + ", loadId=" + loadId + ", addressId=" + addressId
                + ", status=" + status + ", globalId=" + globalId + ", lineId=" + lineId + "]";
    }

}
