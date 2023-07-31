package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.Employee;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.UserModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;

public class UserManagementService
{

	/**
	* Log4j logger: UserManagementService
	*/
	private static final Logger logger = LoggerFactory.getLogger("FILE");


	private final String metaId = "Employee";

	/**
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
    public TableDataModel list() throws DBException, NoSuchFieldException
	{
		EmployeeData empData = Factory.create(EmployeeData.class);
		Employee emp = Factory.create(Employee.class);
		List<Map> utEmpData = emp.getAllElements(empData);
		utEmpData = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(utEmpData, metaId);
		return new TableDataModel(utEmpData);

	}

	public boolean isDuplicateUser(String userId)
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		if (!session.getTransaction().isActive())
			session.getTransaction().begin();
		StringBuilder hql = new StringBuilder();
		hql.append("select u.id from User u where lower(u.id)='" + userId.toLowerCase() + "'");
		Query query = session.createQuery(hql.toString());
		return query.getResultList().size() > 0;
	}

	public AjaxResponse add(UserModel userModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();

		try
		{
			if(isDuplicateUser(userModel.getUserName()))
				return new AjaxResponse(AjaxResponseCodes.FAILURE, "This username is currently being used by another user. Please select another user name. ");
			StandardUserServer userServer = Factory.create(StandardUserServer.class);
			userServer.addEmployee(userModel.getEmployeeData());
		}
		catch(Exception e)
		{
			logger.error("Error adding user: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error adding user: " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully added user: " + userModel.getUserId());
		return ajaxResponse;
	}

	public AjaxResponse update(UserModel userModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();

		try
		{
			StandardUserServer userServer = new StandardUserServer(DBConstantsWeb.DB_NAME);
			userServer.updateEmployeeInfo(userModel.getEmployeeData());

		}
		catch(Exception e)
		{
			logger.error("Error updating user - {}: {}", userModel.getUserName(), e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error updating user: " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully updated user: " + userModel.getUserId());
		return ajaxResponse;
	}

	public AjaxResponse updatePassword(UserModel userModel)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();

		try
		{
			StandardUserServer userServer = new StandardUserServer(DBConstantsWeb.DB_NAME);
			userServer.updateEmployeeInfo(userModel.getEmployeeData()); //update

		}
		catch(Exception e)
		{
			logger.error("Error updating user password- {}: {}", userModel.getUserName(), e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error updating user password: " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully updated user password: " + userModel.getUserId());
		return ajaxResponse;
	}


	public AjaxResponse delete(String deleter, String userId)
	{
		AjaxResponse ajaxResponse = new AjaxResponse();
		try
		{
			StandardUserServer userServer = Factory.create(StandardUserServer.class);
			userServer.deleteEmployee(deleter, userId);
            logger.info("User [{}] deleted User=[{}]", deleter, userId);
		}
		catch(Exception e)
		{
			logger.error("Error deleting user: {}", e.getMessage());
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Error deleting user " + userId + ": " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode().equals(AjaxResponseCodes.DEFAULT))
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully deleted user: " + userId);
		return ajaxResponse;
	}

	public UserModel find(String userId)
	{
		UserModel userModel = new UserModel();
		EmployeeData empData = null;
		try
		{
			StandardUserServer userServer = Factory.create(StandardUserServer.class);
			empData = userServer.getEmployeeData(userId);
		}
		catch(Exception e)
		{
			logger.error("Error finding user record {}: {}", userId, e.getMessage());
		}
		if(empData!=null)
		{
			userModel = new UserModel(empData);
		}
		return userModel;

	}
}
