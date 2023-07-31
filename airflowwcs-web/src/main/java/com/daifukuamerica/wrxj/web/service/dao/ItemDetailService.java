package com.daifukuamerica.wrxj.web.service.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.ItemDetailModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;
import com.daifukuamerica.wrxj.web.service.DataTableable;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemDetailService implements DataTableable
{
	/**
	 * Log4j logger: ItemDetailService
	 */
	private static final Logger logger = LoggerFactory.getLogger(ItemDetailService.class);
	//private IkeaInventoryServer invServer = new IkeaInventoryServer();
	
	private final String metaId = "ItemDetail";

	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		LoadLineItemData vpLliKey =  new LoadLineItemData();
		LoadLineItem lli = new LoadLineItem();

		StandardLoadServer sLoadServer = new StandardLoadServer(DBConstantsWeb.DB_NAME);
		StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		List<Map> utItemDetailData = lli.getLoadLineItemDataList(vpLliKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utItemDetailData) // TODO this is ugly as hell but I am
											// tired come back to this
		{

//			row.replace(LoadLineItemData.HOLDTYPE_NAME, DBTrans.getStringValue(LoadLineItemData.HOLDTYPE_NAME,
//					(int) row.get(LoadLineItemData.HOLDTYPE_NAME)));

			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utItemDetailData);
		return results;
	}

	public TableDataModel listByLoadId(String loadId) throws DBException, NoSuchFieldException
	{
		LoadLineItemData vpLliKey =  new LoadLineItemData();
		LoadLineItem lli = new LoadLineItem();

		vpLliKey.setLoadID(loadId);

		StandardLoadServer sLoadServer = new StandardLoadServer(DBConstantsWeb.DB_NAME);
		StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		List<Map> utItemDetailData = invServer.getLoadLineItemDataListByLoadID(loadId);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utItemDetailData) // TODO this is ugly as hell but I am
											// tired come back to this
		{

//			row.replace(LoadLineItemData.HOLDTYPE_NAME, DBTrans.getStringValue(LoadLineItemData.HOLDTYPE_NAME,
//					(int) row.get(LoadLineItemData.HOLDTYPE_NAME)));

			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utItemDetailData);
		return results;
	}

	public TableDataModel listByLoadLineItem(String itemId) throws DBException, NoSuchFieldException
	{
		LoadLineItemData vpLliKey =  new LoadLineItemData();
		LoadLineItem lli = new LoadLineItem();

		vpLliKey.setItem(itemId);

		StandardLoadServer sLoadServer = new StandardLoadServer(DBConstantsWeb.DB_NAME);
		StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		List<Map> utItemDetailData = invServer.getLoadLineItemDataListByItemLot(itemId);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utItemDetailData) // TODO this is ugly as hell but I am
											// tired come back to this
		{

//			row.replace(LoadLineItemData.HOLDTYPE_NAME, DBTrans.getStringValue(LoadLineItemData.HOLDTYPE_NAME,
//					(int) row.get(LoadLineItemData.HOLDTYPE_NAME)));

			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utItemDetailData);
		return results;
	}
	
	public List<Map> getLoadLineItemByLoadId(String loadId) throws DBException, NoSuchFieldException
	{
		LoadLineItem lli = new LoadLineItem();

		List<Map> vpItemDetList = lli.getLoadLineItemDataListByLoadID(loadId);
		
		return vpItemDetList;
	}
	
	public AjaxResponse getLoadLineItemData(String toteId, String sscId, String itemId, String lotId,
											String orderId, String orderLot, String lineId, String positionId) 
							
	{
		AjaxResponse response = new AjaxResponse(); 
		LoadLineItem lli = new LoadLineItem();

		try
		{
			LoadLineItemData vpItemDetList = lli.getLoadLineItemData(itemId, lotId, sscId, orderId, orderLot, lineId, positionId);
			if(vpItemDetList!=null)
				response.setResponse(AjaxResponseCodes.SUCCESS, "Item[" + itemId + "] found in tote[" +  toteId + "]");
			else
				response.setResponse(AjaxResponseCodes.FAILURE, "Item[" + itemId + "] NOT FOUND in tote[" +  toteId + "]");			
		} 
		catch ( DBException e)
		{
			response.setResponse(AjaxResponseCodes.FAILURE, "Database Exception for tote[ " + toteId + "]  item[" + itemId
					              + "] | Exception: " + e.getMessage());
			logger.error("SHUTTLE QA ERROR: Unable to retrieve item detail for tote[ {}] item[ {}] | Exception: {}", toteId, itemId, e.getMessage());
		} 

		return response;
	}
	
	public ItemDetailModel findLoadLineItem(String load, String item)
	{
		StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		LoadLineItemData tempLLiData = new LoadLineItemData();
		tempLLiData.setKey(LoadLineItemData.LOADID_NAME, load);
		tempLLiData.setKey(LoadLineItemData.ITEM_NAME, item);
		ItemDetailModel itemDetailModel = null;
		try
		{
		  LoadLineItemData lliData = (LoadLineItemData)invServer.getLoadLineItem(tempLLiData);
		  itemDetailModel = new ItemDetailModel(lliData);
		}
		catch (Exception e) 
		{
			logger.error("Error Unable to retrieve item detail for load[ {}] item[ {}] | Exception: {}", load, item, e.getMessage());
		}
		return itemDetailModel;
	}
	
	/**
	 * Add new item detail {@see StandardInventoryServer}
	 * 
	 * @param req
	 * @param resp
	 * @return {@link AjaxResponse}
	 */
	public AjaxResponse add(ItemDetailModel itemDetailModel)

	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		try
		{
			invServer.addLoadLI(itemDetailModel.getLoadLineItemData());
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was a database exception: " + e.getMessage());
			e.printStackTrace();
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Added item " + itemDetailModel.getItem() + " to load " +
			itemDetailModel.getLoadId());
		return ajaxResponse;
	}
	/**
	 * Modify item detail {@see StandardInventoryServer}
	 * 
	 * @param req
	 * @param resp
	 * @return {@link AjaxResponse}
	 */
	public AjaxResponse modify(ItemDetailModel itemDetailModel)

	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		try
		{
			invServer.updateLoadLineItemInfo(itemDetailModel.getLoadLineItemData());
		}
		catch (Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was a database exception: " + e.getMessage());
			e.printStackTrace();
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Updated item " + itemDetailModel.getItem() + " on load " +
			itemDetailModel.getLoadId());
		return ajaxResponse;
	}
	
}
