package com.daifukuamerica.wrxj.web.core.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;

/**
 * Global Access - Convenience hibernate property accessor.  //TODO - merge with duplicate class in com.daifuku.wrxj.web.model.hibernate
 * Author: dystout
 * Created : May 29, 2017
 *
 */
public class HibernateUtil
{
	   private static StandardServiceRegistry registry;
	   private static SessionFactory sessionFactory;
	   
	   static final String DATABASE_KEY = "database"; 
	   static final String DEFAULT_DATABASE_VALUE = "OracleDB"; 
	   
	   public static List<Object> list(Class persistentClass)
	   {
		   List<Object> result = null; 
			try{          
		    	   SessionFactory factory = HibernateUtils.getSessionFactory();
		    	   Session session = factory.getCurrentSession();
		    	   if(!session.getTransaction().isActive())
		    		   session.getTransaction().begin();
		    	   CriteriaBuilder builder = session.getCriteriaBuilder(); 
		    	   CriteriaQuery<Object> criteria = builder.createQuery(persistentClass); 
		    	   criteria.from(persistentClass); 
		    	   result = session.createQuery(criteria).getResultList(); 
		           session.getTransaction().commit();
		           session.close(); 
		    }catch(Exception e){ 
		    	
		    	e.printStackTrace(); //TODO remove
		    }
			return result;
	   }
	   
	   
		public List<Object> executeCustomSelectHql(String hql)
		{
			List<Object> result = null; 
			try{          
		    	   SessionFactory factory = HibernateUtils.getSessionFactory();
		    	   Session session = factory.getCurrentSession();
		    	   if(!session.getTransaction().isActive())
		    		   session.getTransaction().begin();
		           result =   session.createQuery(hql).list();
		           session.getTransaction().commit();
		           session.close(); 
		    }catch(Exception e){ 
		    	
		    	e.printStackTrace(); //TODO remove
		    }
			return result;
		}


	   public static SessionFactory getSessionFactory() {
	      if (sessionFactory == null) {
	         try {
	        	 
	        	String DB_KEY =  Application.getString(DATABASE_KEY, DEFAULT_DATABASE_VALUE);
	        	String jdbcDriver = Application.getString(DB_KEY + ".driver");
	        	String jdbcUrl = Application.getString(DB_KEY + ".url");
	        	String user = Application.getString(DB_KEY + ".user");
	        	String password = Application.getString(DB_KEY + ".password");
	        	String maxRows = Application.getString(DB_KEY + ".MaxRows"); 
	        	String maxConnection = Application.getString(DB_KEY + ".maximum"); 
	        	
	        	
	        	

	            // Create registry builder
	            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

	            // Hibernate settings equivalent to hibernate.cfg.xml's properties
	            Map<String, String> settings = new HashMap<>();
	            settings.put(Environment.DRIVER, jdbcDriver);
	            settings.put(Environment.URL, jdbcUrl);
	            settings.put(Environment.USER, user);
	            settings.put(Environment.PASS, password);


	            // Apply settings
	            registryBuilder.applySettings(settings);

	            // Create registry
	            registry = registryBuilder.build();

	            // Create MetadataSources
	            MetadataSources sources = new MetadataSources(registry);

	            // Create Metadata
	            Metadata metadata = sources.getMetadataBuilder().build();

	            // Create SessionFactory
	            sessionFactory = metadata.getSessionFactoryBuilder().build();

	         } catch (Exception e) {
	            e.printStackTrace();
	            if (registry != null) {
	               StandardServiceRegistryBuilder.destroy(registry);
	            }
	         }
	      }
	      return sessionFactory;
	   }

	   public static Session getNewSession()
	   {
		   Session session = getSessionFactory().openSession(); 
		   return session; 
	   }
	   
	   public static void shutdown() {
	      if (registry != null) {
	         StandardServiceRegistryBuilder.destroy(registry);
	      }
	   }
}
