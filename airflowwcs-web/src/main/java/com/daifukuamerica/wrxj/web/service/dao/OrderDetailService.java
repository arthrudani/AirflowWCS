package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDetailService
{
	private static final Logger logger = LoggerFactory.getLogger(OrderDetailService.class);
	protected AjaxResponse ajaxResponse; // For client response
	private final String metaId = "OrderLine"; 
	
	public TableDataModel listDetail(String orderId) throws DBException, NoSuchFieldException
	{
		OrderHeaderData ohdata = Factory.create(OrderHeaderData.class);
		OrderHeader mpOrderHeader = Factory.create(OrderHeader.class);
		StandardOrderServer orderServer = new StandardOrderServer(); 
		List<Map> orderLineData = orderServer.getOrderLineData(orderId);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId); 
		for(Map row : orderLineData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId); 
		}
		TableDataModel results = new TableDataModel(orderLineData); 
		return results; 
	}

}
