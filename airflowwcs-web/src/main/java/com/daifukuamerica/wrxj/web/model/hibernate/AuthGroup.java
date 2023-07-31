package com.daifukuamerica.wrxj.web.model.hibernate;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity
@Table(name="WEBAUTHGROUP")
public class AuthGroup
{
	public AuthGroup()
	{
		this.name=""; 
		this.description=""; 
	}
	
	public AuthGroup(long id, String name, String description)
	{
		this.id=id; 
		this.name=name; 
		this.description=description; 
	}


	@Column(name="ID")
	@Expose(serialize = false)
	private long id; 
	
	@Id
	@SerializedName("Group Name")
	@Column(name="NAME")
	@Expose
	private String name; 
	
	@SerializedName("Description")
	@Column(name="DESCRIPTION")
	@Expose
	private String description;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	
}
