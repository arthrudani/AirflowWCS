package com.daifukuamerica.wrxj.web.service;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPortServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableService
{

	/**
	* Log4j logger: TableService
	*/
	private static final Logger logger = LoggerFactory.getLogger(TableService.class);
	
	private static AsrsMetaDataData mddata = Factory.create(AsrsMetaDataData.class);

	private List<Map> metadata;
	private static String metaId; 
	private static TableDataModel results = new TableDataModel(); 

	public TableService()
	{
		//
	}

	
	public String getAllElementsJson(String screen) throws DBException
	{
		
		if(screen == null || screen.isEmpty()) return "[]"; 

		logger.debug("Retrieving DBAdapter data for {}", screen);
		switch (screen)
		{
		case "load":
			LoadData vpLoadKey = new LoadData(); 
			StandardLoadServer loadServer = new StandardLoadServer(DBConstantsWeb.DB_NAME);
			results.tableData = loadServer.getLoadDataList(vpLoadKey); 
			metaId = "Load";
			break;
		case "item":
			ItemMaster itmMaster = new ItemMaster();
			ItemMasterData vpIMKey = new ItemMasterData();
			vpIMKey.addOrderByColumn(ItemMasterData.ITEM_NAME);
			StandardInventoryServer itInvServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
			results.tableData = itmMaster.getAllElements(vpIMKey);
			metaId = "ItemMaster";
			break; 
		case "itemdetail":
			LoadLineItemData vpLliKey = new LoadLineItemData(); 
			LoadLineItem lli = Factory.create(LoadLineItem.class);

			StandardLoadServer sLoadServer = new StandardLoadServer(DBConstantsWeb.DB_NAME); 
			StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME); 
			results.tableData = lli.getLoadLineItemDataList(vpLliKey); 
			//results.tableData = invServer.getLoadLineItemDataList(vpLliKey); 
			metaId = "ItemDetail"; 
			break; 
		case "move":
			MoveData vpMoveKey = new MoveData();
			StandardMoveServer smServer = new StandardMoveServer(DBConstantsWeb.DB_NAME); 
			results.tableData = smServer.getMoveDataList(vpMoveKey);
			metaId = "Move";
			break; 
		case "location":
			LocationData locData = new LocationData();
			StandardLocationServer locServer = new StandardLocationServer(DBConstantsWeb.DB_NAME); 
			results.tableData = locServer.getLocationData(locData); 
			metaId = "Location";
			break; 
		case "container":
			StandardInventoryServer cInvServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME); 
			results.tableData = cInvServer.getContainerDataList(); 
			metaId = "Container"; 
			break; 
		case "device":
			StandardDeviceServer sDevServer = new StandardDeviceServer(DBConstantsWeb.DB_NAME);
			results.tableData = sDevServer.getDeviceSearchData(""); 
			metaId = "Device";
			break; 
		case "port":
			StandardPortServer sPortServer = new StandardPortServer(DBConstantsWeb.DB_NAME); 
			results.tableData = sPortServer.getPortlist(); 
			metaId = "Port"; 
			break; 
		case "route":
			RouteData rData = new RouteData();
			StandardRouteServer routeServer = new StandardRouteServer(DBConstantsWeb.DB_NAME); 
			results.tableData = routeServer.getRouteData(rData); 
			metaId = "Route";
			break; 
		case "warehouse":
			WarehouseData waData = new WarehouseData();
			StandardLocationServer wLocServer = new StandardLocationServer(DBConstantsWeb.DB_NAME); 
			results.tableData = wLocServer.getWarehouseData(waData); 
			metaId = "Warehouse";
			break; 
		default:
			break;
		}
		logger.debug("Retrieving {} column metadata.", metaId);
		metadata = AsrsMetaDataTransUtil.getInstance().getAsrsMetaData(metaId); 
		if(results.tableData.size()>0)
		{
			try
			{
				mddata.clear(); 
				String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); // Get Untranslated db columns
				String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId); //translate column names
				for(Map map : results.tableData)
				{ 
					//TODO figure out complexity with DB max rows
					
					// Determine columns to show
//					map = translateMapToHR(map,dbColumns,transColumns, metadata); 
					
					// Perform Translations
					map = AsrsMetaDataTransUtil.getInstance().databaseToUiTable(map, dbColumns, transColumns, metaId);
					
					// shy away from nested loop ??
				}
				if(results==null || results.tableData.isEmpty()) return "[]";

			}
			catch (DBException e) 
			{
				logger.error(e.getMessage()); 
				e.printStackTrace(); 
			}
		}
		Gson gson = new Gson(); 
		return gson.toJson(results);
	}
	
	
	public Map translateMapToHR(Map map, String[] dbColumns, String[] transColumns, List<Map> metadata)
	{
		for(int i=0; i<dbColumns.length-1; i++)
		{
			if(map.containsKey(dbColumns[i]))
			{
				Object objData = map.get(dbColumns[i]); // get data
				map.remove(dbColumns[i]); //remove untranslated value
				map.put(transColumns[i],objData); //put translated value in with data
			}
		}
		
		return map;
	}



}
