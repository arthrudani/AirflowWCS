package com.daifukuamerica.wrxj.web.core.security;


import java.util.List;

import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.hibernate.UserAuthGroup;


/**
 * Service to access user details 
 * 
 * Author: dystout
 * Created : May 25, 2017
 *
 */
public class UserDetailsServiceImpl implements UserDetailsService
{
	/**
	* Log4j logger: UserDetailsServiceImpl
	*/
	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	/**
	 * Get the user details for the given username if exists. If no user exist throw a UsernameNotFoundException
	 * 
	 * @return user details - details of user
	 */
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException 
    {
    	StandardUserServer userServer = new StandardUserServer(); 
    	EmployeeData userData = null;
    	UserDetailsImpl userDetails = null; 
		try
		{
			userData = userServer.getEmployeeData(username);
			String role = userData.getRole();  
        	String displayName = userData.getUserName(); 
        	String pass = userData.getPassword();
        	userDetails = new UserDetailsImpl(username, displayName, role, null, getUserAuthGroups(username));
		} catch (DBException e)
		{
			logger.error("Error retrieving employee data: {}", e.getMessage());
			e.printStackTrace();
			

		}
     
       
        if (userDetails == null) {
            throw new UsernameNotFoundException("Username or password incorrect!");
        }
    
        return userDetails;
    }
    
    public List<UserAuthGroup> getUserAuthGroups(String userId)
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
	    Session session = factory.getCurrentSession();
	    List<UserAuthGroup> result = null; 
	    try {
            
	           // All the action with DB via Hibernate
	           // must be located in one transaction.
	           // Start Transaction.            
	    	   if(!session.getTransaction().isActive())
	    		   session.getTransaction().begin();
	    	   

	            
	           // Create an HQL statement, query Preferences object.
	           String hql = "from UserAuthGroup g where g.username = '" + userId + "'";
	           
	           
	           TypedQuery<UserAuthGroup> query = session.createQuery(hql);
	           result = query.getResultList(); 
	           session.getTransaction().commit();
	           session.close(); 
	       
	       } catch (Exception e) {
	           e.printStackTrace();
	           // Rollback in case of an error occurred.
	           session.getTransaction().rollback();
	       }
	    return result; 
	}

}
