package com.daifukuamerica.wrxj.web.model.json.wrx;

public class RoleModel
{
	
	public String role; 
	public String roleDescription; 
	public String roleType; 
	public String modifyTime; 
	public String addMethod; 
	public String updateMethod;
	
	
	public RoleModel()
	{
		
	}
	
	public RoleModel(String role, String roleDescription){
		this.role = role; 
		this.roleDescription = roleDescription; 
	}
	
	public RoleModel(String role, String roleDescription, String roleType, String modifyTime, String addMethod, String updateMethod)
	{
		this.role = role; 
		this.roleDescription = roleDescription; 
		this.roleType = roleType; 
		this.modifyTime = modifyTime; 
		this.addMethod = addMethod; 
		this.updateMethod = updateMethod; 
	}
	
	public String getRole()
	{
		return role;
	}
	public void setRole(String role)
	{
		this.role = role;
	}
	public String getRoleDescription()
	{
		return roleDescription;
	}
	public void setRoleDescription(String roleDescription)
	{
		this.roleDescription = roleDescription;
	}
	public String getRoleType()
	{
		return roleType;
	}
	public void setRoleType(String roleType)
	{
		this.roleType = roleType;
	}
	public String getModifyTime()
	{
		return modifyTime;
	}
	public void setModifyTime(String modifyTime)
	{
		this.modifyTime = modifyTime;
	}
	public String getAddMethod()
	{
		return addMethod;
	}
	public void setAddMethod(String addMethod)
	{
		this.addMethod = addMethod;
	}
	public String getUpdateMethod()
	{
		return updateMethod;
	}
	public void setUpdateMethod(String updateMethod)
	{
		this.updateMethod = updateMethod;
	} 
	
	

}
