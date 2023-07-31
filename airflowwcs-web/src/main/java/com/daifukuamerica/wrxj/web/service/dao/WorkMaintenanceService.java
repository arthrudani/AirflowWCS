package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardMoveCommandServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItem;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.WorkDataModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;
import com.daifukuoc.wrxj.custom.ebs.plc.acp.route.RouteManagerConstants;

public class WorkMaintenanceService {
	private static final Logger logger = LoggerFactory.getLogger("WORKMAINT");
	private final String metaId = "WorkMaintenance";
	private AjaxResponse ajaxResponse;

	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(MoveCommandData searchData, String isItem, String lot)
			throws DBException, NoSuchFieldException {
		TableDataModel results = new TableDataModel();
		// TODO - Implement search by item in baseline
		if(searchData == null) {
			searchData = Factory.create(MoveCommandData.class);
			searchData.setDeviceID("ALL");
			searchData.setKey(MoveCommandData.DEVICEID_NAME, "ALL");
		}
		final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		final List<Map> utWorkData = (List<Map>) vpTJLoadHandler.getWorkListWeb(searchData, isItem, lot);
		// List<Map> utLoadData = vpLoadHandler.getAllElements(searchLDData);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utWorkData) {
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		results = new TableDataModel(utWorkData);
		return results;
	}

	public AjaxResponse add(WorkDataModel workDataModel) throws NoSuchFieldException {
		StandardMoveCommandServer mpMoveCommandServer = Factory.create(StandardMoveCommandServer.class);
		ajaxResponse = new AjaxResponse();
		MoveCommandData moveCommandData = workDataModel.getMoveCommnadData();
		try {
			// Rudimentary validation
			if (SKDCUtility.isBlank(workDataModel.getLoadId())) {
				throw new Exception("Load ID cannot be blank!");
			}

			if (SKDCUtility.isBlank(workDataModel.getFrom())) {
				throw new Exception("From cannot be blank!");
			}

			if (SKDCUtility.isBlank(workDataModel.getToDest())) {
				throw new Exception("To Dest cannot be blank!");
			}
			String fromDest = moveCommandData.getFrom();
			String toDest = moveCommandData.getToDest();
			String fromLevel = fromDest.substring(8);
			String toLevel =  toDest.substring(8);
			if(fromDest.equals(toDest)) {
				throw new Exception("To Dest cannot be same as from!");
			}
			if(fromDest.substring(0, 2).equals(RouteManagerConstants.LCSTORAGE_LOCATION_TYPE) && !fromLevel.equals(toLevel)) {
				throw new Exception("Bag can only move in same level!");
			}
			mpMoveCommandServer.addMoveCommand(moveCommandData);
		} catch (Exception e) {
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to add work data: " + e.getMessage());
			e.printStackTrace();
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT) {
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully added work data");
		}

		return ajaxResponse;

	}
	
	public TableDataModel listByLoadId(String loadId) throws DBException, NoSuchFieldException
	{
		LoadLineItemData vpLliKey =  new LoadLineItemData();
		LoadLineItem lli = new LoadLineItem();
		LoadData ld = new LoadData();

		vpLliKey.setLoadID(loadId);

		StandardLoadServer sLoadServer = new StandardLoadServer(DBConstantsWeb.DB_NAME);
		StandardInventoryServer invServer = new StandardInventoryServer(DBConstantsWeb.DB_NAME);
		ld = sLoadServer.getLoad(loadId);
		List<Map> utItemDetailData = invServer.getLoadLineItemDataListByLoadID(loadId);
		if(ld!=null) {
		utItemDetailData.get(0).put("finalShortLocationId", ld.getFinalSortLocationID());
		utItemDetailData.get(0).put("currentAddress", ld.getCurrentAddress());
		}
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : utItemDetailData) 
		{
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(utItemDetailData);
		return results;
	}
	
	public AjaxResponse delete(String id)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		StandardMoveCommandServer mpServer = new StandardMoveCommandServer();
		try
		{
			int iCmdstatus = mpServer.getStatusForId(id);
			if(iCmdstatus != -1)
				mpServer.deleteMoveCommandForId(id);
			else
				ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "The process has been already started and cannot be deleted");
		}
		catch (DBException e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "A database exception has occured");
			logger.error("WorkMaintenance Service (delete) Exception: {}", e.getMessage());
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted Command ");
			logger.info("Deleted ID : {}", id);
		}
		return ajaxResponse;
	}
	
	public WorkDataModel getMoveCommandData(String id) throws DBException
	{
		StandardMoveCommandServer mpMoveCommandServer = Factory.create(StandardMoveCommandServer.class);
		MoveCommandData moveCommnadData = mpMoveCommandServer.getMoveCommnadById(id);
		WorkDataModel wdm = null;
		try
		{
			wdm = new WorkDataModel(moveCommnadData);
		}
		catch (NoSuchFieldException e)
		{
			logger.error("LoadService (findLoad) Exception: {}", e.getMessage());
		}

		return wdm;
	}

	public AjaxResponse modify(WorkDataModel workDataModel, int iId) {
		StandardMoveCommandServer mpMoveCommandServer = Factory.create(StandardMoveCommandServer.class);
		ajaxResponse = new AjaxResponse();
		try {
			mpMoveCommandServer.updateMoveCommandData(iId,DBTrans.getIntegerValue(MoveCommandData.STATUS_NAME, workDataModel.getStatus()),workDataModel.getFrom(),workDataModel.getToDest());
		} catch (Exception e) {
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to Modify work data: " + e.getMessage());
			e.printStackTrace();
		}
		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT) {
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully Modified work data");
		}

		return ajaxResponse;
		
	}

}
