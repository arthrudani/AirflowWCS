package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;

public class EBSStation extends Station {
    private EBSStationData mpEBSSTData;

    public EBSStation() {
        super();
        mpEBSSTData = Factory.create(EBSStationData.class);
    }

    /**
     * Sets Objects for garbage collection.
     */
    @Override
    public void cleanUp() {
        super.cleanUp();
        mpEBSSTData = null;
    }

    public EBSStationData getStationData(String stationName, String warehouse) throws DBException {
        mpEBSSTData.clear();
        mpEBSSTData.setKey(StationData.STATIONNAME_NAME, stationName);
        mpEBSSTData.setKey(StationData.WAREHOUSE_NAME, warehouse);

        return getElement(mpEBSSTData, DBConstants.NOWRITELOCK);
    }

    public void setStatus(String stationId, String warehouse, int status) throws DBException {
        mpEBSSTData.clear();
        mpEBSSTData.setKey(StationData.STATIONNAME_NAME, stationId);
        mpEBSSTData.setKey(StationData.WAREHOUSE_NAME, warehouse);
        mpEBSSTData.setStatus(status);
        modifyElement(mpEBSSTData);
    }
}
