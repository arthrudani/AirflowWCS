package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="WEBUSERAUTHGROUP")
public class UserAuthGroup
{
	public UserAuthGroup()
	{
		//for hibernate
	}
	
	public UserAuthGroup(Integer id, User user, AuthGroup authGroup, String authGroupName)
	{
		this.id=id; 
		this.user=user; 
		this.authGroup=authGroup; 
		this.authGroupName=authGroupName; 
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name="AUTH_USER_GROUP_ID")
	private Integer id; 
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USERNAME")
	private User user; 
	
	@Formula("USERNAME")
	private String username;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AUTH_GROUP")
	private AuthGroup authGroup; 
	
	@Formula("AUTH_GROUP")
	private String authGroupName;


	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public AuthGroup getAuthGroup()
	{
		return authGroup;
	}

	public void setAuthGroup(AuthGroup authGroup)
	{
		this.authGroup = authGroup;
	}

	public String getAuthGroupName()
	{
		return authGroupName;
	}

	public void setAuthGroupName(String authGroupName)
	{
		this.authGroupName = authGroupName;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	

}
