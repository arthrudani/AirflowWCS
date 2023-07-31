package com.daifukuamerica.wrxj.web.service.dao;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlockService
{
	
	/**
	* Log4j logger: PlockService
	*/
	private static final Logger logger = LoggerFactory.getLogger(PlockService.class);
	
	protected AjaxResponse ajaxResponse; 
	
	
	/**
	 * Get the load data object at the given station
	 * 
	 * @param station
	 * @return String - loadId at station
	 */
	public LoadData getLoadDataAtStation(String station)
	{
		StandardStationServer mpStationServ = Factory.create(StandardStationServer.class);
		StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);

		String vsStnWhs = mpStationServ.getStationWarehouse(station);
		LoadData vpLoadData = null;
		try
		{
			vpLoadData = (LoadData) vpLoadServ.getOldestLoad(vsStnWhs, station, DBConstants.ARRIVED);
		} catch (DBException e)
		{
			logger.error("Error getting load data at station: {} Message: {}", station, e.getMessage());
			e.printStackTrace();
		}

		return vpLoadData;
	}

}
