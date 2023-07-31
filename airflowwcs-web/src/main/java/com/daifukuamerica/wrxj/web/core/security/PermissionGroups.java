package com.daifukuamerica.wrxj.web.core.security;


/**
 * Default application user permission and authentication groups for 
 * server-side method protection, view rendering security, and user
 * segmentation. These will be persisted to database in WEBAUTHGROUP 
 * for later use in User management. 
 * 
 * Author: dystout
 * Created : Jun 2, 2018
 *
 */
public interface PermissionGroups
{
	final String MASTER = "ROLE_MASTER";
	final String ADMIN = "ROLE_ADMIN"; 
	final String ELEVATED = "ROLE_ELEVATED";
	final String USER = "ROLE_USER"; 
	final String READONLY = "ROLE_READONLY"; 
}
