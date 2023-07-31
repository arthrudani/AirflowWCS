package com.daifukuamerica.wrxj.web.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.core.security.PermissionGroups;
import com.daifukuamerica.wrxj.web.model.hibernate.AuthGroup;
import com.daifukuamerica.wrxj.web.model.hibernate.User;
import com.daifukuamerica.wrxj.web.model.hibernate.UserAuthGroup;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;

/**
 * User permission services. Used for managing and determining user permissions group(s) 
 * and determination of Permission Group access authorization. 
 * 
 * Author: dystout
 * Created : Jun 2, 2018
 *
 */
public class UserPermissionService
{
	private static final Logger logger = LoggerFactory.getLogger(UserPermissionService.class);

	/**
	 * Get a list of all AuthGroup objects that exist in WEBAUTHGROUP table. 
	 * 
	 * @return List of all AuthGroup objects in WEBAUTHGROUP table. 
	 */
	@SuppressWarnings("unchecked")
    public List<AuthGroup> getAuthGroups()
	{
		List<AuthGroup> result = null; 
	    try {          
	    	   SessionFactory factory = HibernateUtils.getSessionFactory();
	    	   Session session = factory.getCurrentSession();
	    	   if(!session.getTransaction().isActive())
	    		   session.getTransaction().begin();
	           result =   (List<AuthGroup>) session.createQuery("from AuthGroup").list();
	           session.getTransaction().commit();
	           session.close(); 
	    }catch(Exception e){ 
	    	logger.error("Unable to get AuthGroup list | ERROR: {}", e.getMessage());
	    	e.printStackTrace(); //TODO remove
	    }
		return result;
	}
	
	/**
	 * Determine if given group is a group that is first 
	 * required to be a base role of ROLE_USER. In most 
	 * cases this will return true with the exception of 
	 * 			-ROLE_READONLY
	 * 			-ROLE_USER 
	 * 
	 * @param group
	 * @return is the group dependent on ROLE_USER
	 */
	public boolean isGroupUserDependent(String group){ // only user group that can access application without ROLE_USER is ROLE_READONLY
		if(!group.equalsIgnoreCase(PermissionGroups.READONLY) && !group.equalsIgnoreCase(PermissionGroups.USER))
			return true; 
		return false; 
	}
	
	/**
	 * Check if in ROLE_USER permissions group
	 * @param user
	 * @return
	 */
	public boolean isInUserGroup(String user) 
	{
		return isInGroup(user,PermissionGroups.USER); 
	}
	
	/**
	 * Determine if user name is in specified group name
	 * 
	 * @param user
	 * @param group
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public boolean isInGroup(String user, String group)
	{
		List<Object[]> result=null; 
		try{
			SessionFactory factory = HibernateUtils.getSessionFactory(); 
			Session session = factory.getCurrentSession(); 
			if(!session.getTransaction().isActive())
				session.getTransaction().begin(); 
			result = session.createQuery("from UserAuthGroup where username='"+user+"' and authGroupName ='"+group+"'").list(); 
			session.getTransaction().commit();
			session.close();		
		}catch(Exception e){ 
	    	logger.error("Unable to determine whether in user/group for {}/{} | ERROR: {}", user, group, e.getMessage());
	    	e.printStackTrace(); //TODO remove
	    }
		if(result!=null&&result.size()>0)
			return true; 
		return false; 
	}
	
	/**
	 * Execute a given HQL statement against available Hibernate Annotated Objects.
	 * Return a List of rows. 
	 * 
	 * @param hql
	 * @return List of Rows for given ResultSet
	 */
	@SuppressWarnings("unchecked")
    public List<Object[]> listCustomHql(String hql)
	{
		List<Object[]> result = null; 
		try{          
	    	   SessionFactory factory = HibernateUtils.getSessionFactory();
	    	   Session session = factory.getCurrentSession();
	    	   if(!session.getTransaction().isActive())
	    		   session.getTransaction().begin();
	           result =   session.createQuery(hql).list();
	           session.getTransaction().commit();
	           session.close(); 
	    }catch(Exception e){ 
	    	logger.error("Unable to get AuthGroup list | ERROR: {}", e.getMessage());
	    	e.printStackTrace(); //TODO remove
	    }
		return result;
	}

	/**
	 * Get the number of users in each of the AuthGroups formatted for 
	 * use in a DataTables response. 
	 * 
	 * COLUMNS - [0]=Name of the group
	 * 			 [1]=Number of users in the 
	 * 
	 * @param columns
	 * @return List<Map<String,Object> - essentially a List of Rows in a 
	 * table that contains Map<Column/Value>
	 */
	public List<Map<String,Object>> getUserAuthGroupCount(String[] columns)
	{
		List<Map<String,Object>> jsonTableData = new ArrayList<Map<String,Object>>(); 
		List<Object[]> result = null; 
		result = listCustomHql("select ag.name, cast(count(distinct a.username) as string) from AuthGroup ag left join a where 1=1 group by a.authGroupName"); 
		for(int i = 0; i<result.size();i++)
		{ 
			if(columns.length>=i)
			{
				Object[] rowData = result.get(i); 
				Map<String,Object> rowJson = new HashMap<String,Object>();
				rowJson.put(columns[0],rowData[0]); 
				rowJson.put(columns[1],rowData[1]); 
				jsonTableData.add(rowJson); 
			}
				
		}
		return jsonTableData;
	}
	
	/**
	 * List users that are contained in specified AuthGroup name
	 * 
	 * @param group
	 * @return List<Map<String,Object> - essentially a List of Rows in a 
	 * table that contains Map<Column/Value>
	 */
	public List<Map<String,Object>> getUsersByGroup(String group)
	{
		List<Map<String,Object>> jsonTableData = new ArrayList<Map<String,Object>>(); 
		List<Object[]> result = null; 
		result = listCustomHql("select u.username from UserAuthGroup u where u.authGroup ='"+group+"'"); 
		for(int i = 0; i<result.size();i++)
		{ 
			
//				Object[] rowData = result.get(i); 
				Map<String,Object> rowJson = new HashMap<String,Object>();
				rowJson.put("User",result.get(i)); 

				jsonTableData.add(rowJson); 
			
				
		}
		return jsonTableData;
	}
	
	/**
	 * List all users that are NOT contained in specified AuthGroup name 
	 * 
	 * @param group
	 * @return List<Map<String,Object> - essentially a List of Rows in a 
	 * table that contains Map<Column/Value>
	 */
	public List<Map<String,Object>> getUsersNotInGroup(String group)
	{
		List<Map<String,Object>> jsonTableData = new ArrayList<Map<String,Object>>(); 
		List<Object[]> result = null; 
		result = listCustomHql("select u.id from User u where u.id not in (select uag.username from UserAuthGroup uag where uag.authGroupName ='"+group+"')"); 
		for(int i = 0; i<result.size();i++)
		{ 
			
//				Object[] rowData = result.get(i); 
				Map<String,Object> rowJson = new HashMap<String,Object>();
				rowJson.put("User",result.get(i)); 

				jsonTableData.add(rowJson); 
			
				
		}
		return jsonTableData; 
	}
	
	/**
	 * List all users
	 * 
	 * @return List<Map<String,Object> - essentially a List of Rows in a 
	 * table that contains Map<Column/Value>
	 */
	public List<Map<String,Object>>  getUsers()
	{
		List<Map<String,Object>> jsonTableData = new ArrayList<Map<String,Object>>(); 
		List<Object[]> result = null; 
		result = listCustomHql("select u.id from User u where 1=1"); 
		for(int i = 0; i<result.size();i++)
		{ 
			
				Object rowData = result.get(i); 
				String sRow = (String) rowData; 
				Map<String,Object> rowJson = new HashMap<String,Object>();
				rowJson.put("User",sRow);  
				jsonTableData.add(rowJson); 
			
				
		}
		return jsonTableData;
	}
	
	
	/**
	 * List all users
	 * 
	 * @return List<Map<String,Object> - essentially a List of Rows in a 
	 * table that contains Map<Column/Value>
	 */
	public List<Map<String,Object>>  getUsersAndGroups()
	{
		List<Map<String,Object>> jsonTableData = new ArrayList<Map<String,Object>>(); 
		List<Object[]> result = null; 
		result = listCustomHql("select u.id, u.authGroups from User u where 1=1"); 
		for(int i = 0; i<result.size();i++)
		{ 
			
			Object[] rowData = result.get(i); 
			Map<String,Object> rowJson = new HashMap<String,Object>();
			rowJson.put("User",rowData[0]); 
			rowJson.put("Granted Access",rowData[1]); 
			jsonTableData.add(rowJson); 
			
				
		}
		return jsonTableData;
	}
	
	
	
	/**
	 * Get a user object for the specified user
	 * 
	 * @return User object with the given user name, returns
	 * null if no match
	 */
	public User getUserByName(String username)
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
        CriteriaBuilder cb = session.getCriteriaBuilder(); 
        CriteriaQuery<User> cq = cb.createQuery(User.class); 
        Root<User> from = cq.from(User.class); 
        Predicate condition = cb.equal(from.get("username"), username); 
        cq.where(condition); 
        Query query = session.createQuery(cq); 
        
		return (User) query.getSingleResult();
	}
	

	/**
	 * Get a user object for the specified user
	 * 
	 * @return User object with the given user name, returns
	 * null if no match
	 */
	public User getUser(String id)
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
        CriteriaBuilder cb = session.getCriteriaBuilder(); 
        CriteriaQuery<User> cq = cb.createQuery(User.class); 
        Root<User> from = cq.from(User.class); 
        Predicate condition = cb.equal(from.get("id"), id); 
        cq.where(condition); 
        Query query = session.createQuery(cq); 
        
		return (User) query.getSingleResult();
	}
	
	/**
	 * Get a single AuthGroup object for the group name 
	 * 
	 * @return AuthGroup object with the given name, returns
	 * null if no match
	 */
	public AuthGroup getAuthGroupByName(String groupname)
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
        CriteriaBuilder cb = session.getCriteriaBuilder(); 
        CriteriaQuery<AuthGroup> cq = cb.createQuery(AuthGroup.class); 
        Root<AuthGroup> from = cq.from(AuthGroup.class); 
        Predicate condition = cb.equal(from.get("name"), groupname); 
        cq.where(condition); 
        Query query = session.createQuery(cq); 
        
		return (AuthGroup) query.getSingleResult();
	}
	
	/**
	 * Get a single UserAuthGroup object for the user name/group name composite reference
	 * representing an AuthGroup that has been granted to the User. 
	 * 
	 * @param username - name of user | group - name of group 
	 * @return AuthGroup object with the given name, returns
	 * null if no match
	 */
	public UserAuthGroup getUserAuthGroupEntry(String username,String group)
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
        CriteriaBuilder cb = session.getCriteriaBuilder(); 
        CriteriaQuery<UserAuthGroup> cq = cb.createQuery(UserAuthGroup.class); 
        Root<UserAuthGroup> from = cq.from(UserAuthGroup.class); 
        Predicate condition = cb.equal(from.get("username"), username); 
        Predicate condition2 = cb.equal(from.get("authGroupName"), group); 
        cq.where(cb.and(condition,condition2)); 
        Query query = session.createQuery(cq); 
        
		return (UserAuthGroup) query.getSingleResult();
	}
		
	/*
	 * Get an aggregate count of how many users are contained within all of the existing 
	 * AuthGroups. 
	 * 
	 * COLUMNS - [0]=Number of users in the AuthGroup
	 * 			 [1]=Name of the AuthGroup
	 * 
	 * @return List<Map<String,Object> - essentially a List of Rows in a 
	 * table that contains Map<Column/Value>
	 */
	public List<Map<String,Object>>  getUserCount()
	{
		List<Map<String,Object>> jsonTableData = new ArrayList<Map<String,Object>>(); 
		List<Object[]> result = null; 
		result = listCustomHql("select cast(count(distinct uag.username) as string), uag.authGroupName from UserAuthGroup uag group by uag.authGroupName"); 
		for(int i = 0; i<result.size();i++)
		{ 
			
				Object[] rowData = result.get(i); 
				Map<String,Object> rowJson = new HashMap<String,Object>();
				rowJson.put("Count",rowData[0]); 
				rowJson.put("Group Name",rowData[1]); 
				jsonTableData.add(rowJson); 
			
				
		}
		return jsonTableData;
	}

	/**
	 * Add the array of usernames to the specified group
	 * @param users
	 * @param group
	 * @return
	 */
	public AjaxResponse addUsersToGroup(String[] users, String group)
	{
		AjaxResponse ajaxResponse = new AjaxResponse(); 
		try{
			 SessionFactory factory = HibernateUtils.getSessionFactory();
	  	   	
	         AuthGroup authGroup = getAuthGroupByName(group); 
	         for(String user : users)
	         { 
	        	 UserAuthGroup auth = new UserAuthGroup(); 
	        	 auth.setUser(getUser(user));
	        	 auth.setAuthGroup(authGroup);
	        	 Session session = factory.getCurrentSession();
	        	 if(!session.getTransaction().isActive())
	        		 session.getTransaction().begin();
	        	 session.save(auth); 
	        	 session.getTransaction().commit();
		         session.close();
	         }
	         
		}catch(Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to add users to group " + group + " | " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Added users to group "  + group + "!");
		return ajaxResponse;
	}

	/**
	 * Delete all UserAuthGroups for a user
	 * @param userId
	 */
    @SuppressWarnings("unchecked")
    public void deleteUser(String userId)
    {
        try (Session session = HibernateUtils.getSessionFactory().getCurrentSession())
        {
            if (!session.getTransaction().isActive())
                session.getTransaction().begin();
      
            CriteriaBuilder cb = session.getCriteriaBuilder(); 
            CriteriaQuery<UserAuthGroup> cq = cb.createQuery(UserAuthGroup.class); 
            Root<UserAuthGroup> from = cq.from(UserAuthGroup.class); 
            Predicate condition = cb.equal(from.get("username"), userId); 
            cq.where(condition); 
            Query query = session.createQuery(cq); 
            
            List<UserAuthGroup> results = query.getResultList();
            for (UserAuthGroup ag : results)
              session.remove(ag);
            
            session.getTransaction().commit();
        }
    }
	
	public AjaxResponse deleteUsersFromGroup(String[] users, String group)
	{
		AjaxResponse ajaxResponse = new AjaxResponse(); 
		try{
			 SessionFactory factory = HibernateUtils.getSessionFactory();
	  	   	
	         for(String user : users)
	         { 
	        	 UserAuthGroup auth = new UserAuthGroup(); 
	        	 auth = getUserAuthGroupEntry(user, group);
	        	 Session session = factory.getCurrentSession();
	        	 if(!session.getTransaction().isActive())
	        		 session.getTransaction().begin();
	        	 session.delete(auth);
	        	 session.getTransaction().commit();
		         session.close();
	         }
	         
		}catch(Exception e)
		{
			ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to delete users from group " + group + " | " + e.getMessage());
		}
		if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Removed users from group "  + group + "!");
		return ajaxResponse;
	}

}
