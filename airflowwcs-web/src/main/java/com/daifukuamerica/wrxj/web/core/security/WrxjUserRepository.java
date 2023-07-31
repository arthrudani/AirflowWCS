package com.daifukuamerica.wrxj.web.core.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.hibernate.UserAuthGroup;
import com.daifukuamerica.wrxj.web.service.dao.UserAuthGroupService;

/**
 * Singleton class with Map of users and their details in the wrxj database to 
 * use when assigning roles and permissions. 
 * 
 * Author: dystout
 * Created : May 25, 2017
 *
 */
public class WrxjUserRepository
{
	
	private static WrxjUserRepository instance = null; 
	
	private Map<String, UserDetailsImpl> userRepository = new HashMap<String, UserDetailsImpl>();
	
	@Autowired
	UserAuthGroupService authGroupService; 
	/**
	* Log4j logger: WrxjUserRepository
	*/
	private static final Logger logger = LoggerFactory.getLogger(WrxjUserRepository.class);

	/**
	 * Available Application-level roles. This will define the access control for the user 
	 * within the web application context. These authorities will be mapped to the WRXJ roles 
	 * to allow spring security to grant/restrict access to areas of applicaiton based on 
	 * user's role level. 
	 */
	private final GrantedAuthority authorityMaster = new GrantedAuthorityImpl("ROLE_MASTER");
    private final GrantedAuthority authorityAdmin = new GrantedAuthorityImpl("ROLE_ADMIN");
    private final GrantedAuthority authorityUser = new GrantedAuthorityImpl("ROLE_USER");
    private final GrantedAuthority authorityReadOnly = new GrantedAuthorityImpl("ROLE_READONLY");
	
    /**
     * Protected for use with singleton instantiation. 
     */
	protected WrxjUserRepository(){
		 WrxjConnection.getInstance();
	        StandardUserServer sUserServer = Factory.create(StandardUserServer.class);
	        List<String> usernames = null;
			try
			{
				usernames = sUserServer.getEmployeeNameList();
			} catch (DBException e)
			{
				logger.error("Error retrieving employee name list: {}", e.getMessage());
				e.printStackTrace();
			}
	        for(String user : usernames)
	        {
	        
	        	EmployeeData userData = null;
				try
				{
					userData = sUserServer.getEmployeeData(user);
				} catch (DBException e)
				{
					logger.error("Error retrieving employee data: {}", e.getMessage());
					e.printStackTrace();
				}
	        	String role = userData.getRole();  
	        	String displayName = userData.getUserName(); 
//	        	Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>(); 

//	        	switch (role) // Map WRXJ role to a spring security role. 
//					{
//					case "Administrator":
//						authorities.add(authorityAdmin);
//						break;
//					case "SKDaifuku":
//						authorities.add(authorityUser);
//						break;
//					case "ReadOnly":
//						authorities.add(authorityReadOnly); 
//						break;
//					case "Master":
//						authorities.add(authorityMaster);
//						break;
//					default:
//						authorities.add(authorityReadOnly);
//					}
	        	UserDetailsImpl userDetails = new UserDetailsImpl(user, displayName, role, null, getUserAuthGroups(user));
	        	userRepository.put(user, userDetails);
	        }
	}
	

	/**
	 * Singleton instance grabber. Will grab the only existing instance of this user repo 
	 * in this thread. 
	 * 
	 * @return WrxjUserRepository - instance class
	 */
	public static WrxjUserRepository getInstance()
	{
		//if(instance==null)
			instance = new WrxjUserRepository(); 
		return instance;
	}

	/**
	 * Get the user details for wrxj employee database table. 
	 * @return
	 */
	public Map<String, UserDetailsImpl> getUserRepository()
	{
		return userRepository;
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
