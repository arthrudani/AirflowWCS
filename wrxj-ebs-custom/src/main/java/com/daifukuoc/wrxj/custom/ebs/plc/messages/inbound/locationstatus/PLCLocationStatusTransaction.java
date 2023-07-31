package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.locationstatus;

import java.util.Objects;

import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSDevice;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSStation;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSStationData;
import com.daifukuoc.wrxj.custom.ebs.host.util.EBSHostMessageConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.StandardLocationStatusTransaction;

public class PLCLocationStatusTransaction extends StandardLocationStatusTransaction {
    public PLCLocationStatusTransaction() {
        super(true);
    }

    private EBSLocation location = Factory.create(EBSLocation.class);
    private EBSDevice device = Factory.create(EBSDevice.class);
    private EBSStation ebsStation = Factory.create(EBSStation.class);

    public static class LocationStatus {
        public static final short STATUS_FLAG_OFFLINE = 1;

        private String sAddress;
        private short newStatus;

        public LocationStatus(String sAddress, short newStatus) {
            this.sAddress = sAddress;
            this.newStatus = newStatus;
        }

        public String getsAddress() {
            return sAddress;
        }

        public short getNewStatus() {
            return newStatus;
        }

        @Override
        public int hashCode() {
            return Objects.hash(newStatus, sAddress);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LocationStatus other = (LocationStatus) obj;
            return newStatus == other.newStatus && Objects.equals(sAddress, other.sAddress);
        }

        @Override
        public String toString() {
            return "LocationStatus [sAddress=" + sAddress + ", newStatus=" + newStatus + "]";
        }
    }

    protected void processLocationData(LocationData locationData, String warehouse, LocationStatus locationStatus)
            throws DBException {
        int currentLocationStatus = locationData.getLocationStatus();
        int nextLocationStatus = locationStatus.getNewStatus() == LocationStatus.STATUS_FLAG_OFFLINE
                ? DBConstants.LCUNAVAIL
                : DBConstants.LCAVAIL;

        if (currentLocationStatus == DBConstants.LCPROHIBIT && nextLocationStatus == DBConstants.LCAVAIL
                || currentLocationStatus == nextLocationStatus) {
            logger.logDebug(String.format("Not applicable change for Location (%s, %s) status %d -> %d", warehouse,
                    locationStatus.getsAddress(), currentLocationStatus, nextLocationStatus));
            return;
        }
        location.setLocationStatusValue(warehouse, locationStatus.getsAddress(), nextLocationStatus);
        logger.logDebug(String.format("Updated Location (%s, %s) status %d -> %d", warehouse, locationStatus.getsAddress(),
                currentLocationStatus, nextLocationStatus));
    }

    protected void processStationData(EBSStationData ebsStationData, String warehouse, LocationStatus locationStatus)
            throws DBException {
        int currentStationStatus = ebsStationData.getStatus();
        int nextStationStatus = locationStatus.getNewStatus() == LocationStatus.STATUS_FLAG_OFFLINE
                ? DBConstants.STNOFFLINE
                : DBConstants.STORERETRIEVE;

        if (currentStationStatus != nextStationStatus) {
            ebsStation.setStatus(locationStatus.getsAddress(), warehouse, nextStationStatus);
            logger.logDebug(String.format ("Updated Station (%s, %s) status %d -> %d", warehouse, locationStatus.getsAddress(),
                    currentStationStatus, nextStationStatus));
        } else {
            logger.logDebug(String.format("Not applicable change for Station (%s, %s) status %d -> %d", warehouse,
                    locationStatus.getsAddress(), currentStationStatus, nextStationStatus));
        }

    }

    @Override
    protected void executeBody(LocationStatusContext plcLocationStatusContext) throws DBException {
        if (plcLocationStatusContext.getDeviceID() != null && (plcLocationStatusContext.getLocationStatusList() != null
                && plcLocationStatusContext.getLocationStatusList().size() > 0)) {
            String warehouse = (String) device.getSingleColumnValue(plcLocationStatusContext.getDeviceID(),
                    DeviceData.WAREHOUSE_NAME);

            if (warehouse == null) {
                logger.logDebug("Warehouse is NULL in DB, default is used: " + 
                        EBSHostMessageConstants.WAREHOUSE_NAME);
                warehouse = EBSHostMessageConstants.WAREHOUSE_NAME;
            }

            logger.logDebug("Warehouse :" + warehouse);

            for (LocationStatus locationStatus : plcLocationStatusContext.getLocationStatusList()) {
                // try to find data from LOCATION data
                LocationData locationData = location.getLocation(warehouse, locationStatus.getsAddress());
                if (locationData != null) {
                    processLocationData(locationData, warehouse, locationStatus);
                } else {
                    // When location data is not available, try to find station data
                    EBSStationData stationData = ebsStation.getStationData(locationStatus.getsAddress(), warehouse);
                    if (stationData != null) {
                        processStationData(stationData, warehouse, locationStatus);
                    } else {
                        logger.logError(String.format("Location nor Station is not found for warehouse - %s, address - %s", warehouse,
                                locationStatus.getsAddress()));
                    }
                }
            }
        }

        plcLocationStatusContext.setSuccess(true);
    }
}
