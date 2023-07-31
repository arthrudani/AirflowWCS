package com.daifukuamerica.wrxj.web.service.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;
import com.daifukuamerica.wrxj.web.model.hibernate.AuthGroup;

public class UserService
{
	

	/**
	* Log4j logger: UserService
	*/
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	private static final String YES = "1"; 
	private static final String NO = "0"; 
	
	public List<AuthGroup> getUserAuthGroups(String userId)
	{ 
		SessionFactory factory = HibernateUtils.getSessionFactory();
 	   	Session session = factory.getCurrentSession();
 	   	if(!session.getTransaction().isActive())
 	   		session.getTransaction().begin();
 	   	StringBuilder hql = new StringBuilder(); 
 	   	hql.append("select u.authGroup from UserAuthGroup u where u.username='"+userId+"'"); 
        Query query = session.createQuery(hql.toString()); 
        List<AuthGroup> result = query.getResultList(); 
        return result; 
        
	}

}
