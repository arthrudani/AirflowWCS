package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.UnderConstructionException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.OrderDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.PurchaseOrderDataModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Purchase Order / Expected Receipt Service
 *
 * @author mandrus
 */
public class PurchaseOrderService
{
	private static final Logger logger = LoggerFactory.getLogger("PURCHASEORDER");
	private static final String metaId = "PurchaseOrderHeader";
	protected AjaxResponse ajaxResponse;

	/**
	 * Does this PO exist?
	 *
	 * @param poNumber
	 * @return
	 */
	public AjaxResponse exists(String poNumber)
	{
		StandardPoReceivingServer poReceivingServer = Factory.create(StandardPoReceivingServer.class);

		ajaxResponse = new AjaxResponse();
		if(poReceivingServer.exists(poNumber))
		{
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Found PO#:"+poNumber);
		}
		else
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Does not exist!");
		}
		return ajaxResponse;
	}

	/**
	 * Add
	 *
	 * @param data
	 * @return
	 */
	public AjaxResponse add(PurchaseOrderDataModel data)
	{
//		StandardPoReceivingServer poServer = Factory.create(StandardPoReceivingServer.class);
		ajaxResponse = new AjaxResponse();
//		OrderHeaderData orderHeaderData = orderDataModel.getOrderHeaderData();
		try
		{
//			poServer.addOrderHeader(orderHeaderData);
			// TODO - implement
			throw new UnderConstructionException("Please implement");
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to add: " + data.getOrderId());
			logger.error("ERROR occured trying to add PO: {}", data.getOrderId(), e);
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully added " + data.getOrderId() + "!\n");
		return ajaxResponse;
	}

	/**
	 * Delete
	 *
	 * @param orderId
	 * @return
	 */
	public AjaxResponse delete(String orderId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardPoReceivingServer poServer = Factory.create(StandardPoReceivingServer.class);
		try
		{
			poServer.deletePO(orderId);
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to delete: " + orderId);
			logger.error("ERROR occured trying to delete: {}", orderId, e);
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted: " + orderId + "!\n");
		return ajaxResponse;
	}

	/**
	 * Modify an existing order using form entries & ids
	 *
	 * @param data
	 * @return
	 */
	public AjaxResponse modify(PurchaseOrderDataModel data)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
//		StandardPoReceivingServer poServer = Factory.create(StandardPoReceivingServer.class);
		try
		{
//			orderDataModel.modifyDate = new Date();
//			poServer.modifyOrderHeader(orderDataModel.getOrderHeaderData());
			// TODO - implement
			throw new UnderConstructionException("Please implement");
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to modify: " + data.getOrderId());
			logger.error("ERROR occured trying to modify PO: {}", data.getOrderId(), e);
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Modified order " + data.getOrderId());
		return ajaxResponse;
	}

	/**
	 *
	 * @param dataModel
	 * @return
	 */
	public OrderDataModel searchOrders(OrderDataModel dataModel)
	{
		logger.error("ERROR occured trying to search PO: Not implemented", new UnderConstructionException("Please implement"));
		return null; //TODO - implement
	}

	/**
	 * List all purchase orders / expected receipts
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		return listSearch(Factory.create(PurchaseOrderHeaderData.class), Factory.create(PurchaseOrderLineData.class));
	}

	/**
	 * List matching purchase orders / expected receipts
	 *
	 * @param searchHeaderData
	 * @param searchLineData
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(PurchaseOrderHeaderData searchHeaderData, PurchaseOrderLineData searchLineData)
			throws DBException, NoSuchFieldException
	{
		StandardPoReceivingServer poServer = Factory.create(StandardPoReceivingServer.class);
		List<Map> orderData = poServer.getPOSearchList(searchHeaderData, searchLineData);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for(Map row : orderData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(orderData);
		return results;
	}

	/**
	 * Delete multiple
	 *
	 * @param orderIds
	 * @return
	 */
	public AjaxResponse deleteOrders(String[] orderIds)
	{
		ajaxResponse = new AjaxResponse(); // store a global response for all order Ids passed
		for(String id : orderIds)
		{
			AjaxResponse tempResp = delete(id);
			if(tempResp.getResponseCode()==AjaxResponseCodes.SUCCESS)//if successful append response message for full listing of actions
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage()+ tempResp.getResponseMessage());
			if(tempResp.getResponseCode()==AjaxResponseCodes.FAILURE)
			{ //if an instance fails, record the failure so we can model alert for the error
				ajaxResponse.setResponseCode(AjaxResponseCodes.FAILURE);
				ajaxResponse.setResponseMessage(ajaxResponse.getResponseMessage() + tempResp.getResponseMessage());

			}
		}
		if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponseCode(AjaxResponseCodes.SUCCESS);
		return ajaxResponse;
	}

	/**
	 * Find
	 *
	 * @param orderId
	 * @return
	 */
	public PurchaseOrderDataModel find(String orderId)
	{
		PurchaseOrderDataModel odm = new PurchaseOrderDataModel();

		try
		{
			StandardPoReceivingServer poServer = Factory.create(StandardPoReceivingServer.class);
			PurchaseOrderHeaderData ohd = poServer.getPoHeaderRecord(orderId);
			if (ohd == null)
			{
				odm = null;
			}
			else
			{
				odm = new PurchaseOrderDataModel(ohd);
			}
		}
		catch (DBException | NoSuchFieldException | ClassCastException e)
		{
			logger.error("ERROR GETTNG ORDER HEADER DATA for:{}REASON: {}", orderId, e.getMessage(), e);
		}
		return odm;
	}
}
