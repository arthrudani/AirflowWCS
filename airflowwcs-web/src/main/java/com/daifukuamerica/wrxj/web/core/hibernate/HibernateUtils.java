package com.daifukuamerica.wrxj.web.core.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.daifukuamerica.wrxj.web.model.hibernate.AuthGroup;
import com.daifukuamerica.wrxj.web.model.hibernate.Device;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentGraphic;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentTab;
import com.daifukuamerica.wrxj.web.model.hibernate.EquipmentTracking;
import com.daifukuamerica.wrxj.web.model.hibernate.InductErrorCounts;
import com.daifukuamerica.wrxj.web.model.hibernate.InductErrorCountsPalletId;
import com.daifukuamerica.wrxj.web.model.hibernate.JvmConfig;
import com.daifukuamerica.wrxj.web.model.hibernate.NavigationGroup;
import com.daifukuamerica.wrxj.web.model.hibernate.NavigationOption;
import com.daifukuamerica.wrxj.web.model.hibernate.User;
import com.daifukuamerica.wrxj.web.model.hibernate.UserAuthGroup;
import com.daifukuamerica.wrxj.web.model.hibernate.UserPreference;
import com.daifukuamerica.wrxj.web.model.hibernate.UserSession;

/**
 * Hibernate utility used to access singleton of hibernate SessionFactory
 * Author: dystout
 * Created : Feb 2, 2018
 *
 */
public class HibernateUtils
{

	private static SessionFactory sessionFactory;

	 public static SessionFactory getSessionFactory() {
	        if (sessionFactory == null) {
	            // loads configuration and mappings
	            Configuration configuration = new Configuration().configure();
	            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
	            MetadataSources sources = new MetadataSources(serviceRegistry);

	            /**
	             * Add persistent entities
	             */
	            sources.addAnnotatedClass(EquipmentGraphic.class);
	            sources.addAnnotatedClass(EquipmentTab.class);
	            sources.addAnnotatedClass(EquipmentTracking.class);
	            sources.addAnnotatedClass(Device.class);
	            sources.addAnnotatedClass(JvmConfig.class);
	            sources.addAnnotatedClass(UserPreference.class);
	            sources.addAnnotatedClass(UserAuthGroup.class);
	            sources.addAnnotatedClass(UserSession.class);
	            sources.addAnnotatedClass(AuthGroup.class);
	            sources.addAnnotatedClass(User.class);
	            sources.addAnnotatedClass(NavigationGroup.class);
	            sources.addAnnotatedClass(NavigationOption.class);
	            sources.addAnnotatedClass(InductErrorCountsPalletId.class);
	            sources.addAnnotatedClass(InductErrorCounts.class);

	            Metadata metaData = sources.getMetadataBuilder().build();

	            // builds a session factory from the service registry (hibernate.cfg.xml) & metadata of added annotated classes
	            sessionFactory = metaData.getSessionFactoryBuilder().build();
	        }

	        return sessionFactory;
	    }

	 public static void shutdown() {
	      if(sessionFactory!=null)
	    	  sessionFactory.close();
	 }



}
