package com.daifukuamerica.wrxj.web.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Web Navigation Group for navigation menu
 *
 * @author mandrus
 */
@Entity
@Table(name="WEBNAVGROUP")
public class NavigationGroup
{
	public NavigationGroup()
	{
		this.name = "";
		this.icon = "";
		this.style = "";
		this.navOrder = 0;
	}

	public NavigationGroup(long id, String name, String icon, String style, int navOrder)
	{
		this.id = id;
		this.name = name;
		this.icon = icon;
		this.style = style;
		this.navOrder = navOrder;
	}

	/*======================================================================*/
	/* Fields 																*/
	/*======================================================================*/
	@Column(name="ID")
	private long id;

	@Id
	@Column(name="NAME")
	private String name;

	@Column(name="ICON")
	private String icon;

	@Column(name="STYLE")
	private String style;

	@Column(name="NAVORDER")
	private int navOrder;

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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getStyle()
	{
		return style;
	}

	public void setStyle(String style)
	{
		this.style = style;
	}

	public int getNavOrder()
	{
		return navOrder;
	}

	public void setNavOrder(int navOrder)
	{
		this.navOrder = navOrder;
	}
}
