package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.daifukuamerica.wrxj.dataserver.standard.StandardRouteServer;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteService 
{
	/**
	* Log4j logger: RouteService
	*/
	private static final Logger logger = LoggerFactory.getLogger(RouteService.class);
	private static final String metaId = "Route"; 
	
	public TableDataModel list(ServletContext context) throws DBException, NoSuchFieldException
	{

		RouteData vpRouteKey = Factory.create(RouteData.class);
		StandardRouteServer routeServer = new StandardRouteServer(DBConstantsWeb.DB_NAME);
		List<Map> utRouteData = routeServer.getRouteData(vpRouteKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utRouteData) // TODO this is ugly as hell but I am tired
									// come back to this
		{
//			row.replace(RouteData.FROMTYPE_NAME, WrxjConnection.getInstance().DBTrans
//					.getStringValue(RouteData.FROMTYPE_NAME, (int) row.get(RouteData.FROMTYPE_NAME)));
//			row.replace(RouteData.DESTTYPE_NAME, WrxjConnection.getInstance().DBTrans
//					.getStringValue(RouteData.DESTTYPE_NAME, (int) row.get(RouteData.DESTTYPE_NAME)));
//			row.replace(RouteData.ROUTEONOFF_NAME, WrxjConnection.getInstance().DBTrans
//					.getStringValue(RouteData.ROUTEONOFF_NAME, (int) row.get(RouteData.ROUTEONOFF_NAME)));
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utRouteData);
	    /*DBObjectPoolUtil.returnDBObject(context, dbo);*/
		return results;

	}
}
