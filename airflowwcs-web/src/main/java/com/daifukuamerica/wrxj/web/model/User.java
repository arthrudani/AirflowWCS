package com.daifukuamerica.wrxj.web.model;

import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.web.model.json.wrx.UserModel;

/**
 * @author dystout
 * Date: Jul 21, 2016
 *
 * Description: Application user view information. 
 * Display user information to client via jstl.
 * 
 * Used during WrxjAuthenticationProvider to keep track of
 * validation values.  
 * 
 */
public class User 
{
	
	public User(){}

	public User(String userId, String displayName, String role)
	{
		super();
		this.userId = userId;
		this.userName = displayName;
		this.role = role;
	}
	
	public User(String userId, String displayName, String role, String uiThemePath, boolean isShowMessageBox)
	{
		super(); 
		this.userId = userId; 
		this.userName = displayName; 
		this.role = role; 
		this.uiTheme = uiThemePath; 
		this.isShowMessageBox = isShowMessageBox; 
	}

	private String userId = ""; 
	/**
	 * User login name
	 */
	private String userName= ""; 
	
	/**
	 * User role level
	 */
	private String role = "";
	
	/**
	 * Client machine name
	 */
	private String machineName=""; 

	/**
	 * Client ip address
	 */
	private String ipAddress=""; 
	
	/**
	 * User preference - UI THEME path
	 */
	private String uiTheme; 
	
	/**
	 * User preference - Show/Hide message box 
	 */
	private boolean isShowMessageBox; 
	

	/**
	 * Validation status
	 */
	private boolean isValidated = false; 

	/**
	 * Displayable login error, placed here so login error is
	 * visible outside of internal application request scope
	 */
	private String loginError = ""; 
	

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

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public boolean isValidated() 
	{
		return isValidated;
	}

	public void setValidated(boolean isValidated) 
	{
		this.isValidated = isValidated;
	}

	public String getLoginError() 
	{
		return loginError;
	}

	public void setLoginError(String loginError) 
	{
		this.loginError = loginError;
	}

	public String getMachineName() 
	{
		return machineName;
	}

	public void setMachineName(String machineName) 
	{
		this.machineName = machineName;
	}

	public String getIpAddress() 
	{
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) 
	{
		this.ipAddress = ipAddress;
	}

	public String getUiTheme()
	{
		return uiTheme;
	}

	public void setUiTheme(String uiTheme)
	{
		this.uiTheme = uiTheme;
	}

	public boolean isShowMessageBox()
	{
		return isShowMessageBox;
	}

	public void setShowMessageBox(boolean isShowMessageBox)
	{
		this.isShowMessageBox = isShowMessageBox;
	}

	

}

