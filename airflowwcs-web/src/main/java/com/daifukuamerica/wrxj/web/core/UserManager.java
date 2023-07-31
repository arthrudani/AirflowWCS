package com.daifukuamerica.wrxj.web.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpSession;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.Employee;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.web.core.security.WrxjAuthenticationProvider;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.model.json.wrx.UserModel;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dystout
 * Date: Aug 14, 2016
 *
 * Description: User login/logout manager. Writes login/logout values to wrxj LOGIN table.
 * Evalutates password against EMPLOYEE table. Used by {@link WrxjAuthenticationProvider}
 * to validate credentials against WRx database.
 *
 */
public class UserManager
{


	public Logger logger = LoggerFactory.getLogger("LOGIN");


	/**
	 * Validate username password for authentication against wrxj EMPLOYEE table. If user is already logged in, logout and
	 * remove user from UserSession. Uses wrxj StandardUserServer.validateLogin(user, password, machine name, IP Address)
	 * for authentication.
	 *
	 * @param username
	 * @param password
	 * @return User - populated User
	 */
	public User authenticateLogin(String userId, String password)
	{
		return authenticateLogin(userId, password, true);
	}

	/**
	 * Validate username password for authentication against wrxj EMPLOYEE table. If user is already logged in, logout and
	 * remove user from UserSession. Uses wrxj StandardUserServer.validateLogin(user, password, machine name, IP Address)
	 * for authentication.
	 *
	 * @param username
	 * @param password
	 * @return User - populated User
	 */
	public User authenticateLogin(String userId, String password, boolean autologout)
	{
		User user = new User();
		UserModel userModel = null;
		user.setUserId(userId);
		int loginCode = -1;
		StandardUserServer userServer = Factory.create(StandardUserServer.class);
		DBInfo dbInfo = new DBInfo();
		try
		{
			dbInfo.init();
		}
		catch (DBException e)
		{
			logger.error("*** (authenticateLogin) Database metadata collection failed | Exception : {}", e.getMessage());

		}

		InetAddress ip = null;
		String machineName = "";

		try
		{
			ip = InetAddress.getLocalHost();
			machineName = ip.getHostName();
		}
		catch (UnknownHostException e)
		{
			logger.error("*** (authenticateLogin) Could not determine host IP Address during login validation | Exception : {}", e.getMessage());
		}

		if(ip!=null)
		{
			user.setIpAddress(ip.getHostAddress());
			user.setMachineName(machineName);
		}
		else
		{
			logger.error("*** (authenticateLogin) Unable to retrieve IP Address ***");
		}


		try
		{
			logger.info("Attempting to Login User[{}] | IP [{}] | MachineName[{}]", userId, ip, machineName);

			if(userId!="" && password!= "")
				loginCode = userServer.validateLogin(userId, password, machineName, user.getIpAddress());
			else
				loginCode=-1;
		}
		catch (DBException e)
		{
			logger.error("*** (authenticateLogin) Error validating login, Database problem exists | Exception : {}", e.getMessage());
			e.printStackTrace();
		}

		if(loginCode == userServer.LOGIN_OKAY)
		{
			logger.info("User[{}] ID and Password validated.  Added to Login Table.", userId);
			SKDCUserData skdcUser = new SKDCUserData();
			skdcUser.login(user.getUserName(), user.getMachineName(), user.getIpAddress());
			Employee emp = Factory.create(Employee.class);
			EmployeeData empData = null;
			try
			{
				empData = emp.getEmployeeData(userId);
			} catch (DBException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(empData!=null)
			{
				user.setUserName(empData.getUserName());
				user.setRole(empData.getRole());
			}

			user.setValidated(true);
			userModel = new UserModel(user);
		}
		else
		{
			switch(loginCode)
			{
			case -1: // Wrong password | no user exiting
				user.setLoginError(UIConstants.LOGIN_INVALID);
				logger.info("### Wrong password submitted for User[{}] ###", userId);
				break;
			case -2:  // login expired
				user.setLoginError(UIConstants.LOGIN_EXPIRED);
				logger.info("### Login Expired for User[{}] ###", userId);
				break;
			case -3: // user already logged in
				try
				{
					if(autologout)
					{
						//userServer.logOut(userId, previousMachine);
						logger.info("User: {} - already logged in. Attempting to logout previous session", userId);
						userServer.logOut(userId, machineName);
						UserSession.removeUserSession(user);
						user = authenticateLogin(userId,password,false);
					}
					else
					{
						user.setLoginError("Login in use - Logout from terminal");
					}
				}
				catch (DBException e)
				{
					logger.error("User[{}] - Login already in use and could not logout previous session: {}", userId, e.getMessage());
					user.setLoginError(UIConstants.LOGIN_IN_USE);
					e.printStackTrace();
				}
				break;
			}
		}
		return user;

	}

	/**
	 * Change record in wrxj LOGIN table to reflect logged off status using
	 * StandardUserServer.logout(User Name, Machine Name)
	 *
	 * Remove user from UserSession and invalidate session.
	 *
	 * @param session
	 * @param user
	 */
	public void logoutUser(HttpSession session, User user)
	{

		StandardUserServer userServer = new StandardUserServer();
		DBInfo dbInfo = new DBInfo();

		//TODO Figure out why machine name and IP address not being saved in user object

		user = getIPandMachineName(user);

		try
		{
			dbInfo.init(); //TODO this is most likely not the correct way to access this
		}
		catch (DBException e)
		{
			logger.error("** (logoutUser) Database metadata collection failed. | Exception : {}", e.getMessage());
		}

		if(user==null)
			user=new User();

		logger.info("Attempting to logout User[{}] | IP Address[{}]", user.getUserId(), user.getIpAddress());
		try
		{
			 userServer.logOut(user.getUserName(), user.getMachineName());
			logger.info("Successfully logged out User[{}] | IP Address[{}]", user.getUserId(), user.getIpAddress());
		}
		catch (DBException e)
		{
			logger.error("Error using Standard User Service to log user out. User: {}Machine Name: {}", user.getUserName(), user.getMachineName());
			logger.error(e.getMessage());
		}
		UserSession.removeUserSession(user);
		session.removeAttribute("user");
		session.invalidate();

	}

	public User getIPandMachineName(User user)
	{
		InetAddress ip = null;
		String machineName = "";

		try
		{
			ip = InetAddress.getLocalHost();
			machineName = ip.getHostName();
		}
		catch (UnknownHostException e)
		{
			logger.error("*** Could not determine host IP Address during login validation ***");
			logger.error(e.getMessage());
		}

		if(ip!=null)
		{
			user.setIpAddress(ip.getHostAddress());
			user.setMachineName(machineName);
		}
		else
		{
			logger.error("*** Unable to retrieve IP Address ***");
		}

		return user;
	}


}
