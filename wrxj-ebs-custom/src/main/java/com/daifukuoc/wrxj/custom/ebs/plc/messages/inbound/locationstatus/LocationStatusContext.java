package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus;

import java.util.List;
import java.util.Objects;

import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus.PLCLocationStatusTransaction.LocationStatus;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.transaction.EBSTransactionContext;

public class LocationStatusContext implements EBSTransactionContext {
    private boolean success = false;;
    private String deviceID;
    private List<PLCLocationStatusTransaction.LocationStatus> locationStatusList;

    public LocationStatusContext(String deviceID, List<LocationStatus> locationStatusList) {
        super();
        this.deviceID = deviceID;
        this.locationStatusList = locationStatusList;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public List<PLCLocationStatusTransaction.LocationStatus> getLocationStatusList() {
        return locationStatusList;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceID, locationStatusList);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocationStatusContext other = (LocationStatusContext) obj;
        return Objects.equals(deviceID, other.deviceID) && Objects.equals(locationStatusList, other.locationStatusList);
    }

    @Override
    public String toString() {
        return "PLCLocationStatusContext [deviceID=" + deviceID + ", locationStatusList=" + locationStatusList + "]";
    }
}
