package com.daifukuamerica.wrxj.web.service;

import javax.servlet.http.HttpSession;

import com.daifukuamerica.wrxj.web.core.UserSession;
import com.daifukuamerica.wrxj.web.model.Login;
import com.daifukuamerica.wrxj.web.model.User;

public class LoginService
{
	public User attemptLogin(Login login){ 
		User user = UserSession.userManager.authenticateLogin(login.getUsername(), login.getPassword()); //validate the login credentials
		return user; 
	}
	
	public void logout(User user, HttpSession session){ 
		UserSession.userManager.logoutUser(session, user);
	}
	

}
