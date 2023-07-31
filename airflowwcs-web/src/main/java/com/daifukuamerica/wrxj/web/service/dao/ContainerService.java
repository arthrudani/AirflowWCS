package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.DataTableable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContainerService implements DataTableable
{
	
	/**
	* Log4j logger: ContainerService
	*/
	private static final Logger logger = LoggerFactory.getLogger(ContainerService.class);
	private AjaxResponse ajaxResponse; 
	private final String metaId = "Container";
	


	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		StandardInventoryServer cInvServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME); 
		List<Map> utContainerData = cInvServer.getContainerDataList(); 
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId); 
		for(Map row : utContainerData) // TODO this is ugly as hell but I am tired come back to this
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId); 
		}
		TableDataModel results = new TableDataModel(utContainerData); 
		return results;
	}
	
	

}
