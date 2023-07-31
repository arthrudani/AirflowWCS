package com.daifukuamerica.wrxj.web.service.dao;

import java.util.HashMap;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.dbadapter.data.Employee;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.DBConstantsWeb;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.hibernate.UserPreference;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.wrx.UserPreferenceModel;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;

public class UserPreferenceService
{
	
	/**
	* Log4j logger: UserManagementService
	*/
	private static final Logger logger = LoggerFactory.getLogger(UserPreferenceService.class);
	private static final String YES = "1"; 
	private static final String NO = "0"; 
	

	public UserPreferenceModel getUserPreferences(String userId)
	{
		 
		UserPreferenceModel upm = new UserPreferenceModel(); 
		//TODO - figure out hibernate issue with concurrent accessors ??? 
		upm = getUserStatePrefences(userId, upm);
		upm = getConfigurablePreferences(userId, upm); 
		return upm; 
	}
	
	/**
	 * Get configurable user preferences stored as key,value in WEBUSERPREF. 
	 * Numeric keys & boolean values are persisted as string numbers to be parsed into Integers 
	 * If the key value 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public UserPreferenceModel getConfigurablePreferences(String userId, UserPreferenceModel upm)
	{ 

	    SessionFactory factory = HibernateUtils.getSessionFactory();
	    Session session = factory.getCurrentSession();
	    try {
            
	           // All the action with DB via Hibernate
	           // must be located in one transaction.
	           // Start Transaction.        
	    	  if(!session.getTransaction().isActive())
	    		  session.getTransaction().begin();

	            
	           // Create an HQL statement, query Preferences object.
	           String hql = "from UserPreference u where u.userId = '" + userId + "'";
	           
	           
	           TypedQuery<UserPreference> query = session.createQuery(hql);
	           List<UserPreference> result = query.getResultList(); 
	           HashMap<String,String> columnPreferences = new HashMap<String,String>(); 
	           for(UserPreference preference : result)
	           { 
//	        	   String user = preference.getUserId(); 
	        	   String key = preference.getPrefKey(); 
	        	   String value = preference.getPrefValue(); 
	        	   
	        	  
	        	   
	        	   Integer prefKey = Integer.valueOf(key);
					if (prefKey == DBConstantsWeb.UITHEME_PREF)
					{
						upm.setUiTheme(value);
					} else if (prefKey == DBConstantsWeb.MESSAGEBOX_PREF)
					{
						upm.setMessageBox(value.equals(YES));
						upm.setMessageBoxDescription(value.equals(YES)?"Enabled":"Disabled"); 
					} else if (prefKey == DBConstantsWeb.DEBUG_PREF)
					{
						upm.setHasDebug(value.equals(YES));
						upm.setHasDebugDescription(value.equals(YES)?"Enabled":"Disabled");
						
					} else if (prefKey == DBConstantsWeb.COLUMN_PREF)
					{ 
						String[] colPref = value.split("-"); 
						if(colPref.length>1)
							columnPreferences.put(colPref[0], colPref[1]); 
					}
	        	   
	           }
	           upm.setTableColumnVisibility(columnPreferences);
	           session.getTransaction().commit();
	           session.close(); 
	           if(result.size()<1)
	        	   initDefaultUserPreferences(userId); 
	       
	       } catch (Exception e) {
	           e.printStackTrace();
	           // Rollback in case of an error occurred.
	           session.getTransaction().rollback();
	       }
	    return upm; 
	}
	
	/**
	 * If this is the first time the user is accessing the application, store default user preferences
	 * in the database for later use. 
	 * 
	 * THIS SHOULD ONLY BE CALLED ONCE
	 * @param userId
	 */
	private void initDefaultUserPreferences(String userId)
	{
		 SessionFactory factory = HibernateUtils.getSessionFactory();
		 Session session = factory.getCurrentSession();
		 try{
			 // All the action with DB via Hibernate
	           // must be located in one transaction.
	           // Start Transaction.
			  if(!session.getTransaction().isActive())
				  session.getTransaction().begin();

	           /**
	            * Default theme
	            */
	           UserPreference upTheme = new UserPreference(); 
	           upTheme.setUserId(userId);
	           upTheme.setPrefKey(DBConstantsWeb.UITHEME_PREF.toString());
	           upTheme.setPrefValue("default"); 
	           session.saveOrUpdate(upTheme);
	           /**
	            * Default debug mode
	            */
	           UserPreference upDebug = new UserPreference(); 
	           upDebug.setUserId(userId);
	           upDebug.setPrefKey(DBConstantsWeb.DEBUG_PREF.toString());
	           upDebug.setPrefValue(NO);
	           session.saveOrUpdate(upDebug);
	           
	           /**
	            * Default Sidebar/messagebox mode
	            */
	           UserPreference upMessage = new UserPreference(); 
	           upMessage.setUserId(userId);
	           upMessage.setPrefKey(DBConstantsWeb.MESSAGEBOX_PREF.toString());
	           upMessage.setPrefValue(NO);
	           session.saveOrUpdate(upMessage);
	           
/*	           UserAuthGroup authGroup = new UserAuthGroup(); 
	           authGroup.setUsername(userId);
	           authGroup.setAuthGroup(DBConstantsWeb.DEFAULT_USER_ROLE);
	           session.save(authGroup); */
	            
	           session.getTransaction().commit();
	           session.close();
			 
		 }catch(Exception e)
		 {
			 logger.error("ERROR Inserting default user preferences: {}", e.getMessage());
			 e.printStackTrace();
			 session.getTransaction().rollback();
		 }
		
	}

	/**
	 * Get user state properties from EMPLOYEE table
	 * @param userId
	 * @param upm
	 * @return
	 */
	public UserPreferenceModel getUserStatePrefences(String userId, UserPreferenceModel upm)
	{
		EmployeeData userResult = null; 
		Employee emp = Factory.create(Employee.class); 
		EmployeeData empData = Factory.create(EmployeeData.class);

		empData.setUserID(userId);
		if(userId!=null)
		{
			try
			{

				userResult = emp.getEmployeeData(userId); 
				
			} catch (DBException e)
			{
				e.printStackTrace();
			} 
		}
				

		if(userResult!=null )
		{
			upm.setUserName(userResult.getUserName());
			upm.setRole(userResult.getRole());

		}
		upm.setUserId(userId);

		return upm; 
	}
	
	
	public AjaxResponse updateDebugPreference(String userId, String value)
	{
		 AjaxResponse ajaxResponse = new AjaxResponse(); 

		 try{
	           /**
	            * Default theme
	            */
	           UserPreference upDebug = new UserPreference(); 
	           upDebug.setUserId(userId);
	           upDebug.setPrefKey(DBConstantsWeb.DEBUG_PREF.toString());
	           upDebug.setPrefValue(value);
	           updateUserPreference(upDebug); 
	           
	           

		 }catch(Exception e)
		 {
			 ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to update debug preferences:" +e.getMessage());
			 logger.error("ERROR Inserting default user preferences: {}", e.getMessage());
			 e.printStackTrace();
		 }
		 
		 if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully updated DEBUG preference!");
		 
		 return ajaxResponse; 
		
	}
	
	public AjaxResponse updateLockSidebarPreference(String userId, String value)
	{
		 AjaxResponse ajaxResponse = new AjaxResponse(); 

		 try{
	           /**
	            * Default theme
	            */
	           UserPreference upDebug = new UserPreference(); 
	           upDebug.setUserId(userId);
	           upDebug.setPrefKey(DBConstantsWeb.MESSAGEBOX_PREF.toString());
	           upDebug.setPrefValue(value.equalsIgnoreCase("yes")?"1":"0");
	           updateUserPreference(upDebug); 
	           
	           

		 }catch(Exception e)
		 {
			 ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to update lock sidebar preferences:" +e.getMessage());
			 logger.error("ERROR Inserting default user preferences: {}", e.getMessage());
			 e.printStackTrace();
		 }
		 
		 if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully updated lock sidebar preference!");
		 
		 return ajaxResponse; 
		
	}
	
	public AjaxResponse updateColumnVisibilityPreference(String userId, String visPref)
	{ 
		logger.debug("UPDATING COLUMN PREF | {} - PREF | {}", userId, visPref);
		AjaxResponse ajaxResponse = new AjaxResponse(); 

		 try{
			   String[] parsedPreference = visPref.split("-"); 
	           UserPreference colVisPref = new UserPreference(); 
	           colVisPref.setUserId(userId);
	           colVisPref.setPrefKey(DBConstantsWeb.COLUMN_PREF.toString());
	           colVisPref.setPrefValue(visPref);
	           colVisPref.setPrefDesc(parsedPreference[0]);
	           saveOrUpdateUserPreference(colVisPref);
	         

		 }catch(Exception e)
		 {
			 ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to update column display :" +e.getMessage());
			 logger.error("ERROR Inserting default column display : {}", e.getMessage());
			 e.printStackTrace();
		 }
		 
		 if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully updated column display preference!");
		 
		return ajaxResponse; 
	}
	
	public AjaxResponse updateUserThemePreference(String userId, String theme)	
	{
		AjaxResponse ajaxResponse = new AjaxResponse(); 

		 try{
	        
	           UserPreference upTheme = new UserPreference(); 
	           upTheme.setUserId(userId);
	           upTheme.setPrefKey(DBConstantsWeb.UITHEME_PREF.toString());
	           upTheme.setPrefValue(theme);
	           updateUserPreference(upTheme); 
	           
	         

		 }catch(Exception e)
		 {
			 ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "Unable to update theme preferences:" +e.getMessage());
			 logger.error("ERROR Inserting default theme preferences: {}", e.getMessage());
			 e.printStackTrace();
		 }
		 
		 if(ajaxResponse.getResponseCode()==AjaxResponseCodes.DEFAULT)
			ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Successfully updated THEME preference to "+theme+"!");
		 
		return ajaxResponse; 
	}
	
	public void saveOrUpdateUserPreference(UserPreference up) 
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
		
		Session session = factory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		 
		try{
			// See if there is a record already existing for this composite reference 
			CriteriaQuery<UserPreference> cQuery = builder.createQuery(UserPreference.class); //create select statment
			Root<UserPreference> root = cQuery.from(UserPreference.class);  //set table root
			cQuery.select(root); // make it a select query 
			cQuery.where(builder.equal(root.get("userId"), up.getUserId()))
				.where(builder.equal(root.get("prefKey"), up.getPrefKey()))
				.where(builder.equal(root.get("prefDesc") ,up.getPrefDesc())); // set where conditions for select 
			Query<UserPreference> query = session.createQuery(cQuery); // create query 
			List<UserPreference> prefList = query.list(); //list out any previous preference
			transaction.commit(); // close transaction
			session.close(); // close select session
			
			/** create separate session for the update/insert **/
			Session updateSession = factory.getCurrentSession(); // grab session for update 
			Transaction updateTransaction = updateSession.beginTransaction(); // new transaction for update
			CriteriaBuilder updateBuilder = updateSession.getCriteriaBuilder(); // build a criteria for the update
			if(prefList.size()>0) // there are matching preferences already, updated them
			{
				CriteriaUpdate<UserPreference> updateQuery = updateBuilder.createCriteriaUpdate(UserPreference.class);
				Root<UserPreference> updateRoot = updateQuery.from(UserPreference.class); 
				
				updateQuery.set(updateRoot.get("prefKey"), up.getPrefKey()); 
				updateQuery.set(updateRoot.get("prefValue"), up.getPrefValue());
				updateQuery.set(updateRoot.get("userId"), up.getUserId());
				updateQuery.set(updateRoot.get("prefDesc"), up.getPrefDesc()); // set values for update 
				if(prefList!=null&&prefList.size()>=1){
					UserPreference oldRecord = (UserPreference) prefList.get(0); 
					updateQuery.set(updateRoot.get("id"), oldRecord.getId()); 
					updateQuery.where(builder.equal(updateRoot.get("id"), oldRecord.getId()));
					updateQuery.where(builder.equal(updateRoot.get("prefKey"), oldRecord.getPrefKey()));
					updateQuery.where(builder.equal(updateRoot.get("userId"), oldRecord.getUserId()));
					updateQuery.where(builder.equal(updateRoot.get("prefDesc"), oldRecord.getPrefDesc())); // set where for update 
					
				}
				
				updateSession.createQuery(updateQuery).executeUpdate(); // update the record
				updateTransaction.commit(); // close the update transaction
			}else{ // there are no matchin preferences, insert them
				updateSession.save(up); // insert the new preference
				updateTransaction.commit(); // close the insert transaction
			}
			
			updateSession.close();
		}catch(Exception e){
			logger.error("CRITICAL ERROR| unable to update user preferences.");
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	
	}
	
	/**
	 * Construct update statment  for user preference table without need for id field.
	 * 
	 * @param up
	 */
	@SuppressWarnings("rawtypes")
    public void updateUserPreference(UserPreference up) throws Exception
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		try
		{
			// All the action with DB via Hibernate
			// must be located in one transaction.
			// Start Transaction.
			if(!session.getTransaction().isActive())
				session.getTransaction().begin();
			Query query = session.createQuery("UPDATE UserPreference u SET prefValue=:pVal where prefKey=:pKey and userId=:userId");
			query.setParameter("pVal", up.getPrefValue());
			query.setParameter("pKey", up.getPrefKey());
			query.setParameter("userId", up.getUserId());
			query.executeUpdate();
			session.getTransaction().commit();
		} catch (Exception e)
		{
			 session.getTransaction().rollback(); 
			 logger.error("Fatal error updating user preference: {}", e.getMessage()); 
			 e.printStackTrace();
			 throw new Exception("Fatal error updating user preference:" + e.getMessage()); 
		 }
		
        session.close();
	}
	
    /**
     * Delete all UserAuthGroups for a user
     * @param user
     */
    public void deleteUser(String user)
    {
        try (Session session = HibernateUtils.getSessionFactory().getCurrentSession())
        {
            if (!session.getTransaction().isActive())
                session.getTransaction().begin();
      
            session.createQuery("delete from UserPreference where userId = :user")
                .setParameter("user", user)
                .executeUpdate();
            
            session.getTransaction().commit();
        }
    }
}
