package com.daifukuoc.wrxj.custom.ebs.plc.messages.inbound.itemstored;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLocation;

/**
 * Level Based Empty location finder implementation 
 * @author MT
 *
 */
public class AisleEmptyLocationFinderImpl implements AisleEmptyLocationFinder{

	private EBSLocation ebsLocation = Factory.create(EBSLocation.class);
    protected Logger logger = Logger.getLogger();
    
    @Override
	public String find(LoadData loadData) throws DBException {
		if (loadData == null) {
			return null;
		}
		
		String address = loadData.getAddress();
		String warehouse = loadData.getWarehouse();
		String level = address.substring(8);
		String deviceId = loadData.getDeviceID();
		String availableAddress = ebsLocation.findUnoccupiedLocationOfLevelAndDeviceId(warehouse, level, deviceId);
		return availableAddress;
	}

}
