package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;

/**
 * Purchase Order / Expected Receipt detail service
 *
 * @author mandrus
 */
public class PurchaseOrderDetailService
{
	protected AjaxResponse ajaxResponse; // For client response
	private final String metaId = "PurchaseOrderLine";

	@SuppressWarnings("rawtypes")
	public TableDataModel listDetail(String orderId) throws DBException, NoSuchFieldException
	{
		StandardPoReceivingServer poServer = Factory.create(StandardPoReceivingServer.class);
		List<Map> orderLineData = poServer.getPurchaseOrderLines(orderId);
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
