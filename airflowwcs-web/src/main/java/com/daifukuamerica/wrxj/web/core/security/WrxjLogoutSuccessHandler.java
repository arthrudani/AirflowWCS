package com.daifukuamerica.wrxj.web.core.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.daifukuamerica.wrxj.web.core.UserSession;
import com.daifukuamerica.wrxj.web.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Post-logout success handler. onlogoutsuccess runs after the logout url is called through spring security. 
 * 
 * Author: dystout
 * Created : May 27, 2017
 *
 */
public class WrxjLogoutSuccessHandler implements LogoutSuccessHandler
{
	
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy(); 
	
	/**
	* Log4j logger: WrxjLogoutSuccessHandler
	*/
	private static final Logger logger = LoggerFactory.getLogger(WrxjLogoutSuccessHandler.class);


	/**
	 * Remove user & session from session pool. Invalidate current session. Destroy cookies by setting
	 * max age to 0. Redirect to /logout success page. 
	 */
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException
	{
		HttpSession session = request.getSession(); 
		User user = (User) session.getAttribute("user"); 
		logger.debug("Logging out user:{}", user.getUserName());
		UserSession.userManager.logoutUser(session, user);
		response.setStatus(HttpStatus.OK.value());
		SecurityContextHolder.clearContext();
		for(Cookie cookie : request.getCookies())
		{
			cookie.setMaxAge(0);
		}
		redirectStrategy.sendRedirect(request, response, "/logout");
	}

}
