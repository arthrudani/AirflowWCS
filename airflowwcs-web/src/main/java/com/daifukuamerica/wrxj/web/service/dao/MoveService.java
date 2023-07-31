package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PickMoveData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveService 
{
	/**
	* Log4j logger: MoveService
	*/
	private static final Logger logger = LoggerFactory.getLogger("FILE");
	private static final String metaId = "Move"; 
	
	public TableDataModel getMoveDataListByLoad(String loadId) throws DBException, NoSuchFieldException
	{
		TableDataModel tdm = new TableDataModel(); 
		StandardMoveServer sMoveServer = new StandardMoveServer();
		List<Map> utMoveDataList = null;
		try
		{
			utMoveDataList = sMoveServer.getMoveDataList(loadId);
		} catch (DBException e)
		{
			logger.error("ERROR: Error getting move data list by load: {} | Exception : {}", loadId, e.getMessage());
		} 
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId); 
		for(Map row : utMoveDataList) 
		{	
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId); 
		}
		
		if(utMoveDataList.size()>0)
		{
			
			tdm.setTableData(utMoveDataList);
		}
		return tdm; 
	}
	
	public TableDataModel getMoveDataListByOrder(String orderId) throws DBException, NoSuchFieldException
	{
		TableDataModel tdm = new TableDataModel(); 
		StandardMoveServer sMoveServer = new StandardMoveServer();
		MoveData mvData = new MoveData();
		List<Map> utMoveDataList = null;
		try
		{
			mvData.setKey(MoveData.ORDERID_NAME, orderId);
			utMoveDataList = sMoveServer.getMoveDataList(mvData);
		} catch (DBException e)
		{
			logger.error("ERROR: Error getting move data list by order: {} | Exception : {}", orderId, e.getMessage());
		} 
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId); 
		for(Map row : utMoveDataList) 
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId); 
		}
		
		if(utMoveDataList.size()>0)
		{
			
			tdm.setTableData(utMoveDataList);
		}
		return tdm; 
	}
	
	
	
	public PickMoveData getNextMoveForLoad(String loadId) throws DBException
	{
		StandardMoveServer sMoveServer = new StandardMoveServer(); 
		MoveData moveData = new MoveData();
		try
		{
			moveData = sMoveServer.getNextMoveRecord(loadId); 
		}
		catch(DBException e)
		{
			logger.error("Error fetching next move for load: {}", loadId);
		}
		StandardInventoryServer inventoryServer = new StandardInventoryServer();
		if(moveData!=null) // if we dont have a move record for the load we won't return anything.
		{
			String itemDescription = inventoryServer.getItemMasterDescription(moveData.getItem()); //null 
			StandardOrderServer orderServer = new StandardOrderServer(); 
			OrderLineData old = new OrderLineData();
			old.setKey(OrderLineData.ORDERID_NAME, moveData.getOrderID());
			old.setKey(OrderLineData.ITEM_NAME, moveData.getItem());
			old.setKey(OrderLineData.LINEID_NAME, moveData.getLineID());
			OrderLineData orderLinePick = orderServer.getOrderLineRecord(old);
			/*OrderHeaderData ohr = orderServer.getOrderHeaderRecord(moveData.getOrderID()); */
			
			
			return new PickMoveData(moveData, itemDescription, orderLinePick.getAllocatedQuantity(), orderLinePick.getPickQuantity());
		}
		else{
			return null; 
		}
		 
	}
	
	public PickMoveData getCurrentMoveForLoad(String loadId)
	{
		return null; 
	}
	
		
	/**
	 * List all moves
	 * 
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException
	{

		MoveData vpLoadKey = new MoveData();
		StandardMoveServer moveServer = new StandardMoveServer(DBConstantsWeb.DB_NAME);
		List<Map> utLoadData = moveServer.getMoveDataList(vpLoadKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
	
		for (Map row : utLoadData) // TODO this is ugly as hell but I am tired
	   							// come back to this
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		
		
		// A check for invalid characters before handing the
		TableDataModel results = new TableDataModel(utLoadData);
		return results;

	}

}
