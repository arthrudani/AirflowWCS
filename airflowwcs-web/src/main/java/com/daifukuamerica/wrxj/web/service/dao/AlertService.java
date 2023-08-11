package com.daifukuamerica.wrxj.web.service.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daifukuamerica.wrxj.dataserver.standard.StandardAlertServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPickServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.Alerts;
import com.daifukuamerica.wrxj.dbadapter.data.AlertsData;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadDataAndLLIData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.InKeyObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.LoadDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.loadAndLLIDataModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSInventoryServer;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSLoadServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSLoad;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

/**
 * Alert Service to handle any CRUD actions or business logic for Alerts.
 * Validation should happen as much as it can at the Model Validator level with
 * JSR 303 annotations on the models uses for form input, however, if all else
 * fails validation at this level can be accepted.
 *
 * This is an example of the most functional controller to date. Chances are
 * this will become the standard which most controller Services will be built.
 *
 * Author: dystout Created : May 4, 2017
 *
 */
public class AlertService
{
	/**
	 * Log4j logger: AlertService
	 */
	private static final Logger logger = LoggerFactory.getLogger("ALERT");


	private AjaxResponse ajaxResponse;
	private final String metaId = "Alert";

	

	/**
	 * Delete a load with the specified ID
	 *
	 * @param id
	 * @return {@link AjaxResponse}
	 * @throws DBException
	 */
//	public AjaxResponse delete(String loadId)
//	{
//		AjaxResponse ajaxResponse = new AjaxResponse();
//		//StandardInventoryServer mpInvServer = new StandardInventoryServer();
//		EBSInventoryServer mpInvServer = new EBSInventoryServer();
//		try
//		{
//			mpInvServer.deleteLoadWithAllData(loadId, "");
//		}
//		catch (DBException e)
//		{
//			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "A database exception has occured");
//			logger.error("LoadService (delete) Exception: {}", e.getMessage());
//		}
//		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
//		{
//			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted load: " + loadId);
//			logger.info("Deleted Load ID : {}", loadId);
//		}
//		return ajaxResponse;
//	}

	/**
	 * Modify a Alert with AlertDataModel {@see StandardAlertServer}
	 *
	 * @param req
	 * @param resp
	 * @return {@link AjaxResponse}
	 */
//	public AjaxResponse modify(AlertDataModel alertDataModel)
//
//	{
//		AjaxResponse ajaxResponse = new AjaxResponse();
//		StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
//		try
//		{
//			if (loadDataModel.getLoadDate() == null)
//				loadDataModel.setLoadDate(new Date());
//			// StandardLoadServer.updateLoadData is a TERRIBLE method that swallows exceptions
//			// Try to avoid some false successes
//			if (SKDCUtility.isBlank(loadDataModel.getWarehouse()))
//			{
//			  throw new Exception("Warehouse cannot be blank!");
//			}
//			if (SKDCUtility.isBlank(loadDataModel.getNextWarehouse()) && SKDCUtility.isNotBlank(loadDataModel.getNextAddress()))
//            {
//              throw new Exception("Next Warehouse cannot be blank when Next Address is not blank!");
//            }
//            if (SKDCUtility.isBlank(loadDataModel.getFinalWarehouse()) && SKDCUtility.isNotBlank(loadDataModel.getFinalAddress()))
//            {
//              throw new Exception("Final Warehouse cannot be blank when Final Address is not blank!");
//            }
//			mpLoadServer.updateLoadData(loadDataModel.getLoadData(), false);
//		}
//		catch (Exception e)
//		{
//			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error modifying load: " + e.getMessage());
//			logger.error("LoadService (modify) Exception: {}", e.getMessage());
//		}
//		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
//			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Updated load " + loadDataModel.getLoadId());
//		return ajaxResponse;
//	}

	/**
	 * Find a specific load by ID
	 *
	 * @param loadId
	 * @return LoadDataModel
	 */
//	public LoadDataModel findLoad(String loadId)
//	{
//		StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class);
//		LoadData loadData = mpLoadServer.getLoad(loadId);
//		LoadDataModel ldm = null;
//		try
//		{
//			ldm = new LoadDataModel(loadData);
//			ldm.setAmountFull(ldm.getAmountFull()); // this will set our string
//													// value for the amount full
//		}
//		catch (NoSuchFieldException e)
//		{
//			logger.error("LoadService (findLoad) Exception: {}", e.getMessage());
//		}
//
//		return ldm;
//	}

	
	/**
	 * List all Alerts
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		AlertsData vpAlertKey = new AlertsData();
		StandardAlertServer alertServer = new StandardAlertServer(DBConstantsWeb.DB_NAME);
		List<Map> utAlertData = alertServer.getAlertDataList(vpAlertKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utAlertData) // TODO this is ugly as hell but I am tired
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utAlertData);
		return results;

	}

	/**
	 * List alerts by search criteria
	 * @param description 
	 *
	 * @return JSON of AlertDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(AlertsData alertData) throws DBException, NoSuchFieldException
	{
		TableDataModel results = new TableDataModel();
		
		Alerts vpAlert = Factory.create(Alerts.class);
		//TODO - Implement search by item in baseline
        final EBSTableJoin vpAlertHandler = new EBSTableJoin();
        final List<Map> utAlertData = (List<Map>)vpAlertHandler.getAlertListWeb(alertData);
		////List<Map> utLoadData = vpLoadHandler.getAllElements(searchLDData);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utAlertData)
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		 results = new TableDataModel(utAlertData);
		 System.out.println(utAlertData);
		return results;
	}

//	public boolean isPicksRemainingOnLoad(String loadId) throws DBException
//	{
//		StandardMoveServer mpMoveServ = Factory.create(StandardMoveServer.class);
//		boolean picksRemaining = (mpMoveServ.getMoveCount("", loadId, "") > 0);
//
//		return (picksRemaining);
//	}

	
}
