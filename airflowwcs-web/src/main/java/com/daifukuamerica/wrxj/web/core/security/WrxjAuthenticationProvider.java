package com.daifukuamerica.wrxj.web.core.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.daifukuamerica.wrxj.jdbc.WebDBObjectHelper;
import com.daifukuamerica.wrxj.web.core.UserSession;
import com.daifukuamerica.wrxj.web.model.User;

/**
 * Authentication provider for authentication of user credentials submitted by the login page
 * form. This authentication provider relies on the usermanager to perform the authentication against
 * the wrxj Database. 
 * 
 * <b>This authentication provider can only authenticate user logins associated with WRXJ</b>
 * Author: dystout
 * Created : May 25, 2017
 *
 */
@Component
public class WrxjAuthenticationProvider implements AuthenticationProvider 
{
	
	@Autowired
	UserDetailsServiceImpl userDetailsService; 

	/**
	 * Build a UsernamePasswordAuthenticationToken to hand back to Spring Security if 
	 * we can validate the user's submitted credentials against WRxJ database. 
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException
	{

	    	String userName = authentication.getName().trim() + ""; // add trailing empty in case null
	        String password = authentication.getCredentials().toString().trim() + ""; //add trailing empty in case null
	        Authentication auth = null;
	        try (WebDBObjectHelper dboh = new WebDBObjectHelper())
		    {

	          User user = UserSession.userManager.authenticateLogin(userName, password); //validate the login credentials
			  if (user.isValidated())
	          {
	          	UserDetails userDetails = userDetailsService.loadUserByUsername(userName); 
	        	auth = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
	            return auth;
	          }
	          else 
	          {
	            throw new BadCredentialsException("1000");
	          }
		    }
	        catch (Exception e)
		    {
	        	throw new BadCredentialsException("1000");
		    }
	        

	}

	/**
	 * Define out authentication token type.
	 */
	@Override
	public boolean supports(Class<?> authentication)
	{
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
