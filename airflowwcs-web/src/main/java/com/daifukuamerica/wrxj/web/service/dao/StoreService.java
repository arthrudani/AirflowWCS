package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreService
{
	/**
	* Log4j logger: StoreService
	*/
	private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

	protected AjaxResponse ajaxResponse; 
	private final String metaId = "Store Items"; 

	/**
	 * Get the load id at the given station
	 * 
	 * @param station
	 * @return String - loadId at station
	 */
	public String getLoadAtStation(String station)
	{
		StandardStationServer mpStationServ = Factory.create(StandardStationServer.class);
		StandardLoadServer vpLoadServ = Factory.create(StandardLoadServer.class);
		StandardPoReceivingServer vpPOServ = Factory.create(StandardPoReceivingServer.class);
		String vsStationLoad = null;
		String vsStnWhs = mpStationServ.getStationWarehouse(station);
        LoadData vpLoadData = null;
		try
		{
			vpLoadData = vpLoadServ.getOldestLoad(vsStnWhs, station, DBConstants.ARRIVED);
		} catch (DBException e)
		{
			logger.error("Error getting load at station: {} Message: {}", station, e.getMessage());
			e.printStackTrace();
		}
        if (vpLoadData != null)
        {
          vsStationLoad = vpLoadData.getLoadID();
        }

		return vsStationLoad;
	}
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
		StandardPoReceivingServer vpPOServ = Factory.create(StandardPoReceivingServer.class);
		String vsStationLoad = null;
		String vsStnWhs = mpStationServ.getStationWarehouse(station);
        LoadData vpLoadData = null;
		try
		{
			vpLoadData = vpLoadServ.getOldestLoad(vsStnWhs, station, DBConstants.ARRIVED);
		} catch (DBException e)
		{
			logger.error("Error getting load data at station: {} Message: {}", station, e.getMessage());
			e.printStackTrace();
		}
		return vpLoadData;
	}
	
	/**
	 * Get the list of load line items in a given load 
	 * formatted for the store screen. Differs from item details
	 * with the respect to server calls and fields exposed to client. 
	 * 
	 * @param - String - loadId to search
	 * @return TableDataModel - DataTables JSON formatted 
	 * @throws DBException - problems with database
	 * @throws NoSuchFieldException - field in storeScreenDataList not exist
	 */
	public TableDataModel getStoreLoadTableData(String loadId) throws DBException, NoSuchFieldException
	{
		TableDataModel tdm = new TableDataModel(); 
        StandardInventoryServer vpInvServ = Factory.create(StandardInventoryServer.class);
        List<Map> results = null ;
        if(vpInvServ.getLoadLineCount(loadId)>0)
        	results = vpInvServ.getLoadLineItemDataListByLoadID(loadId); 
        	
        String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId); 
        for(Map row: results)
        {
/*        	row.replace(LoadLineItemData.HOLDTYPE_NAME, 
					DBTrans.getStringValue(LoadLineItemData.HOLDTYPE_NAME, (int) row.get(LoadLineItemData.HOLDTYPE_NAME))); 
        	row.replace(LoadLineItemData.PRIORITYALLOCATION_NAME, 
					DBTrans.getStringValue(LoadLineItemData.PRIORITYALLOCATION_NAME, (int) row.get(LoadLineItemData.PRIORITYALLOCATION_NAME))); */

			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId); 
        }
        tdm.setTableData(results);
        return tdm; 	
	}
	


}
