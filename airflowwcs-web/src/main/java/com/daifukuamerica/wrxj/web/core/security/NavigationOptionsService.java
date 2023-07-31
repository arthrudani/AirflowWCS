package com.daifukuamerica.wrxj.web.core.security;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daifukuamerica.wrxj.web.core.hibernate.HibernateUtils;

/**
 * Service to access navigation options
 *
 * Author: mandrus
 */
public class NavigationOptionsService
{
	/**
	* Log4j logger: NavigationOptionsService
	*/
	private static final Logger logger = LoggerFactory.getLogger(NavigationOptionsService.class);

	/**
	 * Get options applicable to the specified roles
	 *
	 * @param roles
	 * @return List<Object[]>, where Object[0] is a NavigationGroup and Object[1] is a NavigationOption
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getOptions(List<String> roles)
	{
		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		List<Object[]> result = null;
		try
		{
			// All the action with DB via Hibernate must be located in one transaction.
			// Start Transaction.
			if (!session.getTransaction().isActive())
				session.getTransaction().begin();

			// Create an HQL statement, query Preferences object.
			String hql = "from NavigationGroup g, NavigationOption o where g.name=o.navGroupName and o.authGroupName in (:agn) order by g.navOrder, o.orderNo, o.name";
			Query<Object[]> q = session.createQuery(hql);
			q.setParameterList("agn", roles);
			result = q.getResultList();

			session.getTransaction().commit();
		}
		catch (Exception e)
		{
			logger.error("Error reading nav options.  Roles=[{}]", roles, e);
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}
		return result;
	}

	/**
	 * Get favorite options applicable to the specified roles
	 *
	 * @param roles
	 * @return List<Object[]>, where Object[0] is a NavigationGroup and Object[1] is a NavigationOption
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getDefaultFavoriteOptions(List<String> roles)
	{
	    if (roles == null || roles.isEmpty())
	    {
	      return new ArrayList<Object[]>();
	    }
		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		List<Object[]> result = null;
		try
		{
			// All the action with DB via Hibernate must be located in one transaction.
			// Start Transaction.
			if (!session.getTransaction().isActive())
				session.getTransaction().begin();

			// Create an HQL statement, query Preferences object.
			String hql = "from NavigationGroup g, NavigationOption o where g.name=o.navGroupName and o.authGroupName in (:agn) and o.favorite=1 order by o.orderNo, g.navOrder, o.name";
			Query<Object[]> q = session.createQuery(hql);
			q.setParameterList("agn", roles);
			result = q.getResultList();

			session.getTransaction().commit();
		}
		catch (Exception e)
		{
			logger.error("Error reading nav options.  Roles=[{}]", roles, e);
			session.getTransaction().rollback();
		}
		finally
		{
			session.close();
		}
		return result;
	}
}
