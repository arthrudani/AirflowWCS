package com.daifukuamerica.wrxj.web.model.hibernate;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="V_USER")
public class User
{
	
	public User()
	{
		//for hibernate
	}
	
	public User(String id, String username, String wrxRole, String authGroups)
	{
		this.id=id; 
		this.username=username; 
		this.wrxRole=wrxRole; 
		this.authGroups=authGroups;
	}
	@Id
	@Column(name="ID")
	private String id; 
	
	@Column(name="USERNAME")
	private String username; 
	
	@Column(name="WRXROLE")
	private String wrxRole;
	
	@Column(name="GROUPNAMES")
	private String authGroups; 

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getWrxRole()
	{
		return wrxRole;
	}

	public void setWrxRole(String wrxRole)
	{
		this.wrxRole = wrxRole;
	}

	public String getAuthGroups()
	{
		return authGroups;
	}

	public void setAuthGroups(String authGroups)
	{
		this.authGroups = authGroups;
	}

	
	
	


}
