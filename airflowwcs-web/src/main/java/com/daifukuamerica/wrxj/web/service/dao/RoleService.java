package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.RoleData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.RoleModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;

public class RoleService
{
	private static final String metaId = "Role"; 
	private static final String optionMetaId = "RoleOption"; 
	/**
	 * List all Roles
	 * 
	 * @return JSON of LoadDataModel Objects
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException 
	{

		StandardUserServer userServer = new StandardUserServer();
		List<Map> roleData = userServer.getRoleDataList(true, false); 
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true); 
	//	String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : roleData) 
		{
	//		row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(roleData);
		return results;

	}
	
	public TableDataModel listRoleOption(String role) throws DBException, NoSuchFieldException 
	{

		StandardUserServer userServer = new StandardUserServer();
		List<Map> roleOptionData = userServer.getRoleOptionsList(role);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(optionMetaId, true); 
	//	String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, optionMetaId);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : roleOptionData) 
		{
	//		row = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(row, dbColumns, transColumns, optionMetaId);
		}
		TableDataModel results = new TableDataModel(roleOptionData);
		return results;

	}
	
	public AjaxResponse addRole(RoleModel roleModel){
		AjaxResponse ajaxResponse = new AjaxResponse(); 
		StandardUserServer userServer = new StandardUserServer();
		RoleData newRoleData = Factory.create(RoleData.class);
		newRoleData.setRole(roleModel.getRole());
		newRoleData.setRoleDescription(roleModel.getRoleDescription());
		newRoleData.setRoleType(DBConstants.WORKER);
		try{
			if(!userServer.roleExists(roleModel.getRole())){
				userServer.addRole(newRoleData);
			}else{
				ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to add Role [" + roleModel.getRole() + "] - this role already exists.");
			}
		}catch(Exception e){
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to add Role " + roleModel.getRole() + " | ERROR: " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Sucessfully added Role " + roleModel.getRole() + "!");
		return ajaxResponse; 
	}
	
	public AjaxResponse deleteRole(RoleModel roleModel) {
		AjaxResponse ajaxResponse = new AjaxResponse(); 
		StandardUserServer userServer = new StandardUserServer(); 
		try{
			if (!userServer.roleEmployeeExists(roleModel.getRole()))
		      {
				userServer.deleteRole(roleModel.getRole());
		      }else{
		    	  ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Users(s) exist for this Role - Cannot Delete | Delete users to continue");
		      }
			
		}catch(Exception e){ 
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to delete Role " + roleModel.getRole() + " | ERROR: " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted role " + roleModel.getRole() + "!"); 
		return ajaxResponse; 
	}
	
	public AjaxResponse modifyRole(RoleModel roleModel){
		AjaxResponse ajaxResponse = new AjaxResponse(); 
		StandardUserServer userServer = new StandardUserServer(); 
		RoleData updateRoleData = Factory.create(RoleData.class); 
		updateRoleData.setRole(roleModel.getRole());
		updateRoleData.setRoleDescription(roleModel.getRoleDescription());
		updateRoleData.setKey(RoleData.ROLE_NAME, roleModel.getRole());
		try{
			userServer.updateRoleInfo(updateRoleData);
		}catch(Exception e ){ 
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to modify role " + roleModel.getRole() + " | ERROR: " + e.getMessage());
		}
		return ajaxResponse; 
	}
}
