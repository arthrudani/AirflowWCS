package com.daifukuamerica.wrxj.web.model.json.wrx;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.web.model.User;

public class UserModel
{
	private String userId;
	private String userName;
	private String role;
	private String password;
	private String confirmPassword; 
	private String releaseToCode;
	private String passwordExpiration;
	private int rememberLastLogin = DBConstants.NO; 
	protected String language = "English"; 
	private boolean updatePassword; 
	
	public UserModel()
	{
		//
	}
	
	public UserModel(EmployeeData wed)
	{
		this.userId = wed.getUserID(); 
		this.userName = wed.getUserName(); 
		this.role = wed.getRole(); 
		this.password = wed.getPassword(); 
		this.releaseToCode = wed.getReleaseToCode(); 
		if(wed.getPasswordExpiration()!=null)
			this.passwordExpiration = wed.getPasswordExpiration().toString(); 
		this.rememberLastLogin = wed.getRememberLastLogin(); 
		this.language = wed.getLanguage(); 
	}
	
	

	public UserModel(User user)
	{
		this.userId = user.getUserId(); 
		this.userName = user.getUserName(); 
		this.role = user.getRole(); 
		this.employeeData = new WebEmployeeData(user); 

	}



	private WebEmployeeData employeeData = null; 
	
	protected class WebEmployeeData extends EmployeeData
	{
		public WebEmployeeData(User user)
		{
			super(); 
			this.setUserName(user.getUserName());
			this.setUserID(user.getUserId());
		}
		
		public WebEmployeeData(UserModel userModel)
		{
			super(); 
			this.setUserName(userModel.getUserName());
			this.setUserID(userModel.getUserId());
			this.setRole(userModel.getRole());
			this.setPassword(userModel.getPassword());
			this.setLanguage(userModel.getLanguage());
		}
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

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getReleaseToCode()
	{
		return releaseToCode;
	}

	public void setReleaseToCode(String releaseToCode)
	{
		this.releaseToCode = releaseToCode;
	}

	

	public String getPasswordExpiration()
	{
		return passwordExpiration;
	}

	public void setPasswordExpiration(String passwordExpiration)
	{
		this.passwordExpiration = passwordExpiration;
	}

	public int getRememberLastLogin()
	{
		return rememberLastLogin;
	}

	public void setRememberLastLogin(int rememberLastLogin)
	{
		this.rememberLastLogin = rememberLastLogin;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * If we dont already have an instance of the 
	 * encapsulated User data, construct one 
	 * using the current state of the outer class
	 * 
	 * @return EmployeeData - the encapsulated employee data
	 * @throws NoSuchFieldException
	 */
	public EmployeeData getEmployeeData()
	{
		WebEmployeeData wed = null; 
		if(this.employeeData == null)
		{
			wed  = new WebEmployeeData(this); 
		}else{
			wed = this.employeeData; 
		}
		return wed; 
	}

	public void setEmployeeData(WebEmployeeData employeeData)
	{
		this.employeeData = employeeData;
	}

	public String getConfirmPassword()
	{
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword)
	{
		this.confirmPassword = confirmPassword;
	}

	public boolean isUpdatePassword()
	{
		return updatePassword;
	}

	public void setUpdatePassword(boolean updatePassword)
	{
		this.updatePassword = updatePassword;
	}
	
	
	
	
	
}
