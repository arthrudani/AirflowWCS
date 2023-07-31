package com.daifukuamerica.wrxj.web.service.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.hibernate.UserSession;

public class UserSessionService
{

	/**
	 * User Session Service Logger 
	 */
	private static final Logger logger = LoggerFactory.getLogger(UserSessionService.class);
	
	/**
	 * Get all sessions with the associated user detailed information
	 * @return
	 */
	public List<UserSession> getUserSessions()
	{ 
		
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
 	   	StringBuilder hql = new StringBuilder(); 
 	   	hql.append("from UserSession"); 
        Query query = session.createQuery(hql.toString()); 
        List<UserSession> result = query.getResultList(); 
        
       
        return result;        
	}
	
	public Long getActiveSessionCount()
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
 	   	StringBuilder hql = new StringBuilder(); 
 	   	hql.append("select count(*) from UserSession"); 
        Query query = session.createQuery(hql.toString()); 
        List<Long> result = query.getResultList(); 
		return (Long) result.get(0);
	}
	
	public Long getDuplicateSessionCount()
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
 	   	StringBuilder hql = new StringBuilder(); 
 	   	hql.append("select userId, count(*) from UserSession group by userId having count(*) > 1"); 
        Query query = session.createQuery(hql.toString()); 
        List<Object[]> result = query.getResultList(); 
        Long dupSessions = 0L; 
        for(Object[] row : result){
        	dupSessions+= (Long)row[1]; 
        }
		return dupSessions;
	}
	
	public Long getUniqueLoginCount()
	{
		
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
 	   	StringBuilder hql = new StringBuilder(); 
 	   	hql.append("select count(distinct userId) from UserSession"); 
        Query query = session.createQuery(hql.toString()); 
        List<Long> result = query.getResultList(); 
        Long uniqueSessions = 0L; 
        for(Long row : result){
        	uniqueSessions+= row; 
        }
		return uniqueSessions;
	}
	
	public Long getAdminSessionCount()
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
 	   	StringBuilder hql = new StringBuilder(); 
 	   	hql.append("select sum(case when (u.grantedUserAccess like '%ROLE_MASTER%' OR u.grantedUserAccess like '%ROLE_ADMIN%') then 1 else 0 end)"
 	   			+ " from UserSession u group by u.userId, u.grantedUserAccess"); 
        Query query = session.createQuery(hql.toString()); 
        List<Long> result = query.getResultList(); 
        Long adminSessions = 0L; 
        for(Long row : result){
        	adminSessions+= row; 
        }
		return adminSessions; 
	}



	public Map<String,Long> getUserSessionCounts()
	{
		HashMap<String,Long> userSessions = new HashMap<String,Long>(); 
		Long activeSessions = getActiveSessionCount(); 
		Long duplicateSessions = getDuplicateSessionCount(); 
		Long uniqueSessions = getUniqueLoginCount(); 
		Long adminSessions = getAdminSessionCount(); 
		userSessions.put("Active", activeSessions); 
		userSessions.put("Duplicate", duplicateSessions); 
		userSessions.put("Unique", uniqueSessions); 
		userSessions.put("Admin" , adminSessions); 
		return userSessions; 
	}

}
