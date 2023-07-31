package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A lower level data base object that interfaces to the Employee table.
 *
 * @author avt
 * @version 1.0
 */
public class Employee extends BaseDBInterface
{
  private DBResultSet mpDBResultSet;
  private EmployeeData mpEmpData;
  private LoginData mpLoginData;
  private StandardUserServer userServ;

  public Employee()
  {
    super("Employee");
    mpEmpData = Factory.create(EmployeeData.class);
    mpLoginData = Factory.create(LoginData.class);
    userServ = Factory.create(StandardUserServer.class);
  }

  /**
   *  Method to delete an employee.
   *
   *  @param isUserID User to be deleted.
   *  @exception DBException
   */
  public void deleteEmployee(String isUserID) throws DBException
  {

   List <Map> vpData; 

     vpData  = userServ.getLoginDataList(isUserID, true);
     if ( vpData.size() > 0)
     {  
       mpLoginData.clear();
       mpLoginData.setKey(LoginData.USERID_NAME, isUserID);
       Factory.create(Login.class).deleteElement(mpLoginData);
     }

    mpEmpData.clear();
    mpEmpData.setKey(EmployeeData.USERID_NAME, isUserID);
    deleteElement(mpEmpData);
  }

  /**
   *  Method to add an employee.
   *
   *  @param emd Filled in employee data object.
   *  @exception DBException
   */
  public void addEmployee(EmployeeData emd) throws DBException
  {
    if (SKDCUtility.isBlank(emd.getUserID()))
    {
      throw new DBException("User ID may not be blank!");
    }
    if (SKDCUtility.isBlank(emd.getUserName()))
    {
      throw new DBException("User Name may not be blank!");
    }
    
    /*
     * You have to do this instead of addElement() because addElement()
     * converts null dPasswordExpirations to SYSTIMESTAMP, and we don't
     * want to do that.
     */
    StringBuilder vpSql = new StringBuilder("INSERT INTO EMPLOYEE (")
               .append(EmployeeData.USERID_NAME).append(", ")
               .append(EmployeeData.USERNAME_NAME).append(", ")
               .append(EmployeeData.ROLE_NAME).append(", ")
               .append(EmployeeData.PASSWORD_NAME).append(", ")
               .append(EmployeeData.RELEASETOCODE_NAME).append(", ")
               .append(EmployeeData.PASSWORDEXPIRATION_NAME).append(", ")
               .append(EmployeeData.LANGUAGE_NAME).append(", ")
               .append(EmployeeData.REMEMBERLASTLOGIN_NAME)
               .append(") VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )");
    
    mpDBResultSet = execute(vpSql.toString(), emd.getUserID(),
        emd.getUserName(), emd.getRole(), emd.getPassword(),
        emd.getReleaseToCode(), emd.getPasswordExpiration(), emd.getLanguage(),
        emd.getRememberLastLogin());
  }

  /**
   *  Method to get a list of matching employee names.
   *
   *  @param srchUser Employee name to match.
   *  @return List of employee names.
   *  @exception DBException
   */
  public List<String> getEmployeeNameList(String srchUser) throws DBException
  {
    List<String> EmpList = new ArrayList<String>();

    mpDBResultSet = execute(
        "SELECT sUserID FROM employee WHERE sUserID like ? order by sUserID",
        srchUser + "%");
    Map row;
    while (mpDBResultSet.hasNext())  // may be multiple rows
    {
      row = (Map) mpDBResultSet.next();
      String nameStr = new String(DBHelper.getStringField(row,"sUserID"));
      EmpList.add(nameStr);
    }
    return(EmpList);
  }

  /**
   *  Method to get a list of multiple employee data objects.
   *
   *  @param srchUser Employee name to match.
   *  @param match - boolean as to whether we need to match exactly or not
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   */
  public List<Map> getEmployeeDataList(String isUserSearch, boolean izExactMatch)
      throws DBException
  {
    mpEmpData.clear();
    if (izExactMatch)
    {
      mpEmpData.setKey(EmployeeData.USERID_NAME, isUserSearch);
    }
    else if (isUserSearch.trim().length() > 0)
    {
      mpEmpData.setWildcardKey(EmployeeData.USERID_NAME, isUserSearch, false);
    }

    if (SKDCUserData.isAdministrator())
    {
                 // we are the administrator so show everybody except
                 // daifuku role users
      mpEmpData.setKey(EmployeeData.ROLE_NAME, SKDCConstants.ROLE_DAC_SUPERROLE,
                       KeyObject.NOT_EQUAL);
    }
    else if (!SKDCUserData.isSuperUser())
    {
                // We are a regular employee so just display normal user data
      mpEmpData.setKey(EmployeeData.ROLE_NAME, SKDCConstants.ROLE_DAC_SUPERROLE,
                       KeyObject.NOT_EQUAL);
      mpEmpData.setKey(EmployeeData.ROLE_NAME, SKDCConstants.ROLE_ADMINISTRATOR,
                       KeyObject.NOT_EQUAL);
    }
    mpEmpData.setOrderByColumns(EmployeeData.USERID_NAME);

    return(getAllElements(mpEmpData));
  }

  /**
   *  Method to get a list of multiple employee data objects by role.
   *
   *  @param srchRole Role name to match.
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   */
  public List<Map> getEmployeeDataListByRole(String isRoleSearch) throws DBException
  {
    mpEmpData.clear();
    mpEmpData.setWildcardKey(EmployeeData.ROLE_NAME, isRoleSearch, false);

    // Only SuperUser gets to see everything
    if (!SKDCUserData.isSuperUser())
    {
      if (SKDCUserData.isAdministrator())
      {
        // We are the administrator so show all roles except daifuku
        mpEmpData.setKey(EmployeeData.ROLE_NAME,
            SKDCConstants.ROLE_DAC_SUPERROLE, KeyObject.NOT_EQUAL);
      }
      else
      {
        mpEmpData.setKey(EmployeeData.ROLE_NAME,
            SKDCConstants.ROLE_DAC_SUPERROLE, KeyObject.NOT_EQUAL);
        mpEmpData.setKey(EmployeeData.ROLE_NAME,
            SKDCConstants.ROLE_ADMINISTRATOR, KeyObject.NOT_EQUAL);
      }
    }
    mpEmpData.setOrderByColumns(EmployeeData.USERID_NAME);
    
    return(getAllElements(mpEmpData));
  }

  /**
   *  Method to get a employee data for specified user.
   *
   *  @param userName Employee name to get.
   *  @return EmployeeData object containing Employee info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public EmployeeData getEmployeeData(String isUserName) throws DBException
  {
    mpEmpData.clear();
    mpEmpData.setKey(EmployeeData.USERID_NAME, isUserName);

    return(getElement(mpEmpData, DBConstants.NOWRITELOCK));
  }

  /**
   *  Method to see if the specified employee exists.
   *
   *  @param empName Employee name.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean exists(String empName) throws DBException
  {
    mpEmpData.clear();
    mpEmpData.setKey(EmployeeData.USERID_NAME, empName);
    return(exists(mpEmpData));
  }

  /**
   *  Method to see if any employees with the specified role exist.
   *
   *  @param sRole Role name.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean employeeRoleExists(String sRole) throws DBException
  {
    mpEmpData.clear();
    mpEmpData.setKey(EmployeeData.ROLE_NAME, sRole);
    return(exists(mpEmpData));
  }

  /**
   *  Method to update a employee.
   *
   *  @param emData Filled in employee data object.
   *  @throws DBException
   */
  public void updateEmployeeInfo(EmployeeData emData) throws DBException
  {
    emData.setKey(EmployeeData.USERID_NAME, emData.getUserID());
    modifyElement(emData);
  }

  /**
   *  Method to update a employees role.
   *
   *  @param userID to update.
   *  @param newRole to update to.
   *  @exception DBException
   */
  public void updateEmployeeRole(String userID, String newRole) throws DBException
  {
    mpEmpData.clear();
    mpEmpData.setKey(EmployeeData.USERID_NAME, userID);
    mpEmpData.setRole(newRole);
    modifyElement(mpEmpData);
  }

  /**
   *  Method to get an employee's role.
   *
   *  @param isEmpName Employee name.
   *  @return String containing the role of the employee.
   *  @throws DBException
   */
  public String getEmployeeRole(String isEmpName) throws DBException
  {
    mpEmpData.clear();
    mpEmpData.setKey(EmployeeData.USERID_NAME, isEmpName);

    String[] vasRoles = getSingleColumnValues(EmployeeData.ROLE_NAME, true, 
                                              mpEmpData, SKDCConstants.NO_PREPENDER);

    return(vasRoles.length > 0 ? vasRoles[0] : null);
  }

  /**
   *  Method to get an employee's release-to-code.
   *
   *  @param empName Employee name.
   *  @return String containing the release-to-code of the employee.
   *  @exception DBException
   */
  public String getEmployeeReleaseToCode(String empName) throws DBException
  {
    mpDBResultSet = execute(
        "SELECT sReleaseToCode FROM employee WHERE sUserID = ?", empName);
    Map row;
    while (mpDBResultSet.hasNext())
    {
      row = (Map) mpDBResultSet.next();
      return(DBHelper.getStringField(row, EmployeeData.RELEASETOCODE_NAME));
    }

    return(null);
  }
}
