package com.daifukuamerica.wrxj.web.core.security;

/**
 * Header auth codes used in authenticaiton check (integration with Spring Security) 
 * 
 * Author: dystout
 * Created : Feb 2, 2018
 *
 */
public interface AuthenticationCodes
{
	final String USERNAME_NOT_FOUND_URI_PARAM = "UNF"; 
	final String SESSION_AUTHENTICATION_EX_URI_PARAM = "SAE";
	final String INSUFFICIENT_AUTHENTICATION_EX_URI_PARAM = "IAE";
	final String BAD_CREDENTIALS_EX_URI_PARAM = "BC"; 
	final String ACCOUNT_STATUS_EX_URI_PARAM = "ASE"; 
	final String AUTHENTICATION_SERVICE_EX_URI_PARAM = "ATSE"; 

}
