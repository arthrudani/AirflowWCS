package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.HashMap;

import com.daifukuamerica.wrxj.web.ui.UIConstants;

public class UserPreferenceModel
{

	private String userId; 
	private String userName; 
	private String uiTheme = UIConstants.UI_THEME_DEFAULT; 
	private String role;
	private String hasDebugDescription; 
	private boolean hasDebug; 
	private boolean messageBox; 
	private HashMap<String,String> tableColumnVisibility; 
	private String messageBoxDescription; 
	
	public UserPreferenceModel()
	{
		//
	}
	
	public UserPreferenceModel(String userId, String userName, String uiTheme, String role)
	{
		this.userId = userId;
		this.userName = userName;
		this.uiTheme = uiTheme;
		this.role = role;
		this.tableColumnVisibility = new HashMap<String,String>();  
	}
	
	public UserPreferenceModel(String userId, String userName, String uiTheme, String role, boolean debug, boolean messageBox)
	{ 
		this.userId = userId;
		this.userName = userName;
		this.uiTheme = uiTheme;
		this.role = role;
		this.hasDebug = debug; 
		this.messageBox = messageBox; 
		this.tableColumnVisibility = new HashMap<String,String>();  
	}
	public String getUserId()
	{
		return userId;
	}
	public void setUserId(String userId)
	{
		this.userId = userId;
	}
	public String getUserName()
	{
		return userName;
	}
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	public String getUiTheme()
	{
		return uiTheme;
	}
	public void setUiTheme(String uiTheme)
	{
		this.uiTheme = uiTheme;
	}
	public String getRole()
	{
		return role;
	}
	public void setRole(String role)
	{
		this.role = role;
	}

	public String getMessageBoxDescription()
	{
		return messageBoxDescription;
	}

	public void setMessageBoxDescription(String messageBoxDescription)
	{
		this.messageBoxDescription = messageBoxDescription;
	}

	public String getHasDebugDescription()
	{
		return hasDebugDescription;
	}

	public void setHasDebugDescription(String hasDebugDescription)
	{
		this.hasDebugDescription = hasDebugDescription;
	}

	public boolean isHasDebug()
	{
		return hasDebug;
	}

	public void setHasDebug(boolean hasDebug)
	{
		this.hasDebug = hasDebug;
	}

	public boolean isMessageBox()
	{
		return messageBox;
	}

	public void setMessageBox(boolean messageBox)
	{
		this.messageBox = messageBox;
	}

	public HashMap<String, String> getTableColumnVisibility()
	{
		return tableColumnVisibility;
	}

	public void setTableColumnVisibility(HashMap<String, String> tableColumnVisibility)
	{
		this.tableColumnVisibility = tableColumnVisibility;
	} 
	
	
}
