package com.daifukuamerica.wrxj.web.service.dao;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfig;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfigData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.SysConfigModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import com.daifukuoc.wrxj.custom.ebs.dataserver.EBSTimeServer;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.TimeSlotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SysConfigService {
	private static final Logger logger = LoggerFactory.getLogger("FILE");
	private static final String ccMetaId = "ControllerConfig";
	private static final String scMetaId = "SysConfig";
	private static final String tsMetaId = "TimeSlot";
	protected AjaxResponse ajaxResponse; // For client response

	public TableDataModel listControllerConfig() throws DBException, NoSuchFieldException {
		TableDataModel results = new TableDataModel();

		ControllerConfig mpCC = new ControllerConfig();
		ControllerConfigData mpCCD = new ControllerConfigData();

		mpCCD.setKey(ControllerConfigData.SCREENCHANGEALLOWED_NAME, DBConstants.YES);
		mpCCD.addOrderByColumn(ControllerConfigData.PROPERTYNAME_NAME);

		List<Map> utCCList = mpCC.getAllElements(mpCCD);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(ccMetaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, ccMetaId);
		for (Map row : utCCList) {
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, ccMetaId);
		}
		results = new TableDataModel(utCCList);
		return results;

	}

	public TableDataModel listSysConfig() throws DBException, NoSuchFieldException {
		TableDataModel results = new TableDataModel();

		SysConfig mpSC = new SysConfig();
		SysConfigData mpSCD = new SysConfigData();

		mpSCD.setKey(SysConfigData.SCREENCHANGEALLOWED_NAME, DBConstants.YES);
		mpSCD.addOrderByColumn(SysConfigData.GROUP_NAME);
		mpSCD.addOrderByColumn(SysConfigData.PARAMETERNAME_NAME);

		List<Map> utSCList = mpSC.getAllElements(mpSCD);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(scMetaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, scMetaId);
		for (Map row : utSCList) {
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, scMetaId);
		}
		results = new TableDataModel(utSCList);
		return results;

	}

	/**
	 * Method to find specific Controller Config record
	 * 
	 * @param paramId
	 * @return
	 */
	public SysConfigModel findControllerConfig(String paramId) {
		SysConfigModel sysConfigData = new SysConfigModel();
		ControllerConfig mpCC = new ControllerConfig();
		ControllerConfigData mpCCD = new ControllerConfigData();

		mpCCD.setKey(ControllerConfigData.PROPERTYNAME_NAME, paramId);

		try {
			mpCCD = mpCC.getElement(mpCCD, DBConstants.NOWRITELOCK);

			if (mpCCD != null) {
				sysConfigData.setControllerId(mpCCD.getController());
				sysConfigData.setPropertyName(mpCCD.getPropertyName());
				sysConfigData.setPropertyDesc(mpCCD.getPropertyDesc());
				sysConfigData.setPropertyValue(mpCCD.getPropertyValue());
			}
		} catch (DBException e) {
			logger.error("IkeaSysConfigService (find) DBException : {}", e.getMessage());
		}
		return sysConfigData;

	}

	public AjaxResponse modifyControllerConfig(String paramId, String paramValue, String userId) {
		AjaxResponse ajaxResponse = new AjaxResponse();
		boolean success = false;

		// TODO: Port StandardConfigurationServer.updateControllerConfigParamValue()
		// from Ikea to Base
//		StandardConfigurationServer ccServer = new StandardConfigurationServer();

		try {

//			success = ccServer.updateControllerConfigParamValue(paramId, paramValue);

			if (success) {
				logger.info("SysConfig (Modify) - UserId [{}] \n | Parameter[{} \n | Value[{}]", userId, paramId, paramValue);

				ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Parameter[" + paramId + "] Updated");
			} else {
				logger.error("Failed to update SysConfig (Modify) - UserId [{}] \n | Parameter[{} \n | Value[{}]", userId, paramId, paramValue);

				ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Failed to Update Parameter[" + paramId + "]");
			}
		} catch (Exception e) {
			logger.error("Failed to update SysConfig (Modify) - UserId [{}] \n | Parameter[{} \n | Value[{}] \n | Exception: {}", userId, paramId, paramValue, e.getMessage());

			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Failed to Update Parameter[" + paramId + "]");
		}

		return ajaxResponse;

	}

	/**
	 * List all Time slot by schema ID
	 *
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listTimeSlotBySchemaId(String schemaId) throws DBException, NoSuchFieldException {
		TableDataModel results = new TableDataModel();
		final TimeSlotConfig vpTJLoadHandler = new TimeSlotConfig();
		final List<Map> timeSlotData = (List<Map>) vpTJLoadHandler.getTimeSlotBySchemaId(schemaId);

		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(tsMetaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, tsMetaId);
		for (Map row : timeSlotData) {
			row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, tsMetaId);
		}
		results = new TableDataModel(timeSlotData);
		return results;
	}

	/**
	 * Add an OrderHeader
	 *
	 * @param orderDataModel
	 * @return
	 * @throws NoSuchFieldException
	 */
	public AjaxResponse addTimeSlot(String timeSlot, String schemaId) throws NoSuchFieldException {
		ajaxResponse = new AjaxResponse();
		EBSTimeServer mpEBSTimeSlotServer = new EBSTimeServer(DBConstantsWeb.DB_NAME);
		try {
			mpEBSTimeSlotServer.addTimeSlot(timeSlot, schemaId);
		} catch (DBException e) {
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Failed to add New Time Slot: " + timeSlot);
			logger.error("DB ERROR occured trying to add New Time Slot: {}", timeSlot);
		}

		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully added New Time Slot " + timeSlot + "!\n");
		return ajaxResponse;
	}

	/**
	 * Delete time slot
	 *
	 * @param timeSlot
	 * @param schemaId
	 * @return AjaxResponse
	 * @throws NoSuchFieldException
	 * @throws ParseException
	 */
	public AjaxResponse deleteTimeSlot(String timeSlot, String schemaId) throws NoSuchFieldException, ParseException {
		ajaxResponse = new AjaxResponse();
		EBSTimeServer mpEBSTimeSlotServer = new EBSTimeServer(DBConstantsWeb.DB_NAME);
		try {		

			mpEBSTimeSlotServer.deleteTimeSlot(timeSlot, schemaId);
		} catch (DBException e) {
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, " A database exception has occured");
			logger.error("LoadService (delete) Exception: {}", e.getMessage());
		}

		if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted Time Slot " + timeSlot + "!\n");

		return ajaxResponse;
	}
}
