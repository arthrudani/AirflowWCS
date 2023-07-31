package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Web Navigation Option for navigation menu
 *
 * @author mandrus
 */
@Entity
@Table(name="WEBNAVOPTION")
public class NavigationOption
{
	public NavigationOption()
	{
		this.authGroupName = "";
		this.navGroupName = "";
		this.name = "";
		this.link = "";
		this.description = "";
		this.icon = "";
		this.favorite = 0;
		this.orderNo = 0;
	}

	public NavigationOption(long id, String authGroupName, String navGroupName, String name, String link,
			String description, String icon)
	{
		this(id, authGroupName, navGroupName, name, link, description, icon, 0, 0);
	}

	public NavigationOption(long id, String authGroupName, String navGroupName, String name, String link,
			String description, String icon, int favorite, int orderNo)
	{
		this.id = id;
		this.navGroupName = navGroupName;
		this.authGroupName = authGroupName;
		this.name = name;
		this.link = link;
		this.description = description;
		this.icon = icon;
		this.favorite = favorite;
		this.orderNo = orderNo;
	}

	/*======================================================================*/
	/* Fields 																*/
	/*======================================================================*/
	@Column(name="ID")
	private long id;

	@Column(name="AUTHGROUPNAME")
	private String authGroupName;

	@Column(name="NAVGROUPNAME")
	private String navGroupName;

	@Id
	@Column(name="NAME")
	private String name;

	@Column(name="LINK")
	private String link;

	@Column(name="DESCRIPTION")
	private String description;

	@Column(name="ICON")
	private String icon;

	@Column(name="FAVORITE")
	private int favorite;
	
	@Column(name="ORDERNO")
	private int orderNo;

	/*======================================================================*/
	/* Accessors															*/
	/*======================================================================*/
	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getAuthGroupName()
	{
		return authGroupName;
	}

	public void setAuthGroupName(String authGroupName)
	{
		this.authGroupName = authGroupName;
	}

	public String getNavGroupName()
	{
		return navGroupName;
	}

	public void setNavGroupName(String navGroupName)
	{
		this.navGroupName = navGroupName;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public int getFavorite()
	{
		return favorite;
	}

	public void setFavorite(int favorite)
	{
		this.favorite = favorite;
	}
	
	public int setOrderNo()
	{
		return orderNo;
	}
	
	public void setOrderNo(int orderNo)
	{
		this.orderNo = orderNo;
	}
	
}
