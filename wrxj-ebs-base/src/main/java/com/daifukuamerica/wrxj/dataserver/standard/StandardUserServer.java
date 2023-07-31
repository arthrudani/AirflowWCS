package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.dbadapter.data.Employee;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.dbadapter.data.Login;
import com.daifukuamerica.wrxj.dbadapter.data.LoginData;
import com.daifukuamerica.wrxj.dbadapter.data.Role;
import com.daifukuamerica.wrxj.dbadapter.data.RoleData;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOption;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOptionData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;

import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.xerces.impl.dv.util.Base64;

/**
 * A server that provides methods and transactions for keeping track of
 * employees, roles, and role options. Transactions are wrapped around calls
 * to the lower level data base objects.
 *
 * @author avt
 * @version 1.0
 */
public class StandardUserServer extends StandardServer
{
  // TODO: Increase HASH_ITERATIONS to 1000+
  public static final int HASH_ITERATIONS = 1;

  public static final int LOGIN_OKAY    =  0;
  public static final int LOGIN_INVALID = -1;
  public static final int LOGIN_EXPIRED = -2;
  public static final int LOGIN_IN_USE  = -3;

  public StandardUserServer()
  {
    this(null);
  }

  public StandardUserServer(String keyName)
  {
    super(keyName);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardUserServer(String isKeyName, DBObject dbo)
  {
	  super(isKeyName, dbo); 
  }

  /**
   * Get the validation string (to be stored in the database) for a given
   * password.
   *
   * TODO: Add salt
   * TODO: Change "SHA" to "SHA-512" (requires VARCHAR2(89) for storage)
   *
   * @param isPassword
   * @return
   */
  public String getHashedPassword(String isPassword)
  {
    if (isPassword.equals(""))
    {
      return isPassword;
    }
    try
    {
      MessageDigest vpMessageDigest = null;
      vpMessageDigest = MessageDigest.getInstance("SHA");
      byte vabRaw[] = isPassword.getBytes("UTF-8");
      for (int i = 0; i < HASH_ITERATIONS; i++)
      {
        vpMessageDigest.update(vabRaw);
        vabRaw = vpMessageDigest.digest();
      }
      String vsHashedPassword = Base64.encode(vabRaw).replace("\n", "");
      return vsHashedPassword;
    }
    catch (Exception e)
    {
      logException("Error hashing password", e);
      return isPassword;
    }
  }

  /**
   * Check customer-specific password requirements (baseline has none).
   *
   * @param isPassword
   * @throws Exception
   */
  public void checkPasswordRequirements(String isPassword) throws Exception
  {
    // Sample:
//    if (isPassword.trim().length() == 0)
//      throw new Exception("Password is required.");
  }

  /**
   * Is this the correct password?
   *
   * @param isPassword
   * @param isHashedPassword
   * @return
   */
  public boolean isCorrectPassword(String isPassword, String isHashedPassword)
  {
    if (isHashedPassword.equals(""))
    {
      return isPassword.equals(isHashedPassword);
    }
    return getHashedPassword(isPassword).equals(isHashedPassword);
  }

  /**
   *  Method to delete an employee.
   *
   *  @param siEmployee Employee.
   *  @exception DBException
   *  @Deprecated Use deleteEmployee(String, String) instead
   */
  @Deprecated
  public void deleteEmployee(String siEmployee) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Employee.class).deleteEmployee(siEmployee);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to delete an employee.
   *
   * @param isDeleter - Employee performing the deletion
   * @param isEmployee - Employee to be deleted
   * @exception DBException
   */
  public void deleteEmployee(String isDeleter, String isEmployee) throws DBException
  {
    if (isEmployeeSuperUser(isEmployee) && !isEmployeeSuperUser(isDeleter))
    {
      throw new DBException("User [" + isDeleter + "] is not authorized to delete superuser [" + isEmployee + "]");
    }
    
    if ((isEmployee.equals(SKDCConstants.USER_DAC_SUPERUSER)) || 
        (isEmployee.equals(SKDCConstants.USER_DAC_SUPPORT)))
    {
      throw new DBException("User [" + isDeleter + "] is not authorized to delete embedded user [" + isEmployee + "]");
    }
    
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Employee.class).deleteEmployee(isEmployee);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to add an employee.
   *
   *  @param ipEmployeeData Filled in employee data object.
   *  @exception DBException
   */
  public void addEmployee(EmployeeData ipEmployeeData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      try
      {
        checkPasswordRequirements(ipEmployeeData.getPassword());
        ipEmployeeData.setPassword(getHashedPassword(ipEmployeeData.getPassword()));
      }
      catch (Exception e)
      {
        throw new DBException(e.getMessage());
      }
      Factory.create(Employee.class).addEmployee(ipEmployeeData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to get a list of all employee names.
   *
   *  @return List of employee names.
   *  @exception DBException
   */
  public List<String> getEmployeeNameList() throws DBException
  {
    return(this.getEmployeeNameList (""));
  }

  /**
   *  Method to get a list of matching employee names.
   *
   *  @param srchEmployee Employee name to match.
   *  @return List of employee names.
   *  @exception DBException
   */
  public List<String> getEmployeeNameList(String srchEmployee) throws DBException
  {
    return Factory.create(Employee.class).getEmployeeNameList(srchEmployee);
  }

  /**
   *  Method to get a list of all employee data objects.
   *
   *  @return List of <code>EmployeeData</code> objects.
   *  @exception DBException
   */
  public List<Map> getEmployeeDataList() throws DBException
  {
    return getEmployeeDataList("", false);
  }

  /**
   *  Method to get a list of matching employee data objects.
   *
   *  @param srchEmployee Employee name to match.
   *  @param match booean as to whether we need an exact match or not
   *  @return List of <code>EmployeeData</code> objects.
   *  @exception DBException
   */
  public List<Map> getEmployeeDataList(String srchEmployee, boolean match) throws DBException
  {
    return Factory.create(Employee.class).getEmployeeDataList(srchEmployee, match);
  }

  /**
   *  Method to get a list of matching employee data objects by role.
   *
   *  @param srchRole Role name to match.
   *  @return List of <code>EmployeeData</code> objects.
   *  @exception DBException
   */
  public List<Map> getEmployeeDataListByRole(String srchRole) throws DBException
  {
    return Factory.create(Employee.class).getEmployeeDataListByRole(srchRole);
  }

  /**
   *  Method to get an employee data for specified employee.
   *
   *  @param employeeName Employee number.
   *  @return EmployeeData object containing Employee info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public EmployeeData getEmployeeData(String employeeName) throws DBException
  {
    return Factory.create(Employee.class).getEmployeeData (employeeName);
  }

  /**
  *  Method to see if the specified employee exists.
  *
  *  @param employeeName Employee name.
  *  @return boolean of <code>true</code> if it exists.
  *  @exception DBException
  */
  public boolean employeeExists(String employeeName) throws DBException
  {
    return Factory.create(Employee.class).exists(employeeName);
  }

  /**
   *  Method to update an employee.
   *
   *  @param ipEmployeeData Filled in employee data object.
   *  @exception DBException
   */
  public void updateEmployeeInfo(EmployeeData ipEmployeeData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      if (ipEmployeeData.getColumnObject(EmployeeData.PASSWORD_NAME) != null)
      {
        try
        {
          checkPasswordRequirements(ipEmployeeData.getPassword());
          ipEmployeeData.setPassword(getHashedPassword(ipEmployeeData.getPassword()));
        }
        catch(Exception e)
        {
          throw new DBException(e.getMessage());
        }
      }
      Factory.create(Employee.class).updateEmployeeInfo(ipEmployeeData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to update an employees role.
   *
   *  @param userID to update.
   *  @param newRole to update to
   *  @exception DBException
   */
  public void updateEmployeeRole(String userID, String newRole) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Employee emp = Factory.create(Employee.class);
      emp.updateEmployeeRole(userID, newRole);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to get an employee's role.
   *
   *  @param employeeName Employee name.
   *  @return String containing the role of the employee.
   *  @exception DBException
   */
  public String getEmployeeRole(String employeeName) throws DBException
  {
    return Factory.create(Employee.class).getEmployeeRole(employeeName);
  }

  /**
   *  Method to get an employee's role.
   *
   *  @param employeeName Employee name.
   *  @return String containing the role of the employee.
   *  @exception DBException
   */
  public String getEmployeeReleaseToCode(String employeeName) throws DBException
  {
    return Factory.create(Employee.class).getEmployeeReleaseToCode(employeeName);
  }

  /**
   *  Method to validate that a password is correct for an employee and log the
   *  employee into the system.
   *
   *  @param isEmployeeName Employee to verify.
   *  @param isPassword Password string.
   *  @param isMachineName
   *  @param isIPAddress
   *  @param isDeviceID
   *  @param izDeviceRqd
   *  @return int containing login status.
   *  @exception DBException
   */
  public int validateLogin(String isEmployeeName, String isPassword,
      String isMachineName, String isIPAddress) throws DBException
  {
    int vnLoginStatus = LOGIN_INVALID;

    // If the device is in use, return -3 so they can decide if they want
    // to replace that logged in device
    if (isMachineName.length() == 0)
    {
      isMachineName = isIPAddress;
    }
    Login vpLogin = Factory.create(Login.class);

    EmployeeData vpEmpData = Factory.create(Employee.class).getEmployeeData(isEmployeeName);
    if (vpEmpData != null)  // employee exists
    {
      // validate password
      String vsValidationString = vpEmpData.getPassword();
      if (vsValidationString.equalsIgnoreCase("NULL"))
      {
        vsValidationString = "";
      }
      if (isCorrectPassword(isPassword, vsValidationString))
      {
        // check expiration date
        Date vpDate = vpEmpData.getPasswordExpiration();
        if (vpDate == null)  // password never expires
        {
          vnLoginStatus = LOGIN_OKAY;
        }
        else if (vpDate.after(new Date()))
        {
          vnLoginStatus = LOGIN_OKAY;
        }
        else if (vpDate.before(new Date()))
        {
          vnLoginStatus = LOGIN_EXPIRED;
        }
      }

      if (vnLoginStatus == LOGIN_OKAY)
      {
        // Don't check whether the user is logged in until AFTER checking the
        // password
        if (vpLogin.userLoggedIn(isEmployeeName, isMachineName))
        {
          return LOGIN_IN_USE;
        }

        // Record user login into history table.
        tnData.clear();
        tnData.setTranCategory(DBConstants.USER_TRAN);
        tnData.setTranType(DBConstants.LOGIN);
        tnData.setUserID(vpEmpData.getUserID());
        tnData.setRole(vpEmpData.getRole());
        tnData.setMachineName(isMachineName);
                                       // add the user to the login table
        LoginData vpLoginData = Factory.create(LoginData.class);
        vpLoginData.setUserID(isEmployeeName);
        vpLoginData.setMachineName(isMachineName);
        vpLoginData.setIPAddress(isIPAddress);
        vpLoginData.setRole(vpEmpData.getRole());

        TransactionToken ttok = null;
        try
        {
          ttok = startTransaction();
          if (vpEmpData.getRememberLastLogin() == DBConstants.YES)
          {
            Factory.create(SysConfig.class).memorizeLastLogin(isMachineName,
                isEmployeeName);
          }
          vpLogin.addElement(vpLoginData);
          logTransaction(tnData);
          commitTransaction(ttok);
        }
        catch(DBException dbe)
        {
          logException(dbe, "Error Adding Login Transaction");
        }
        finally
        {
          endTransaction(ttok);
        }
      }
    }
    return vnLoginStatus;
  }

  /**
   * Method handles user logout.
   *
   * @param isEmployeeName Employee to log out.
   * @param isMachineName to log out.
   */
  public void logOut(String isEmployeeName, String isMachineName)
      throws DBException
  {
    String vsUserMessage = "";
    String vsMachineMessage = "";
    String vsDeviceMessage = "";

    // delete the user from the login table
    LoginData vpLoginData = Factory.create(LoginData.class);
    Login vpLogin = Factory.create(Login.class);
    if (isEmployeeName.trim().length() > 0)
    {
      vpLoginData.setKey(LoginData.USERID_NAME, isEmployeeName);
      vsUserMessage = "User: " + isEmployeeName + " ";
    }
    if (isMachineName.trim().length() > 0)
    {
      vpLoginData.setKey(LoginData.MACHINENAME_NAME, isMachineName);
      vsMachineMessage = "Machine: " + isMachineName + " ";
    }
    if (vpLoginData.getKeyCount() == 0)
    {
      throw new DBException("No key specified for log out operation");
    }
    vpLoginData = vpLogin.getElement(vpLoginData, DBConstants.NOWRITELOCK);

    tnData.clear();
    tnData.setTranCategory(DBConstants.USER_TRAN);
    tnData.setTranType(DBConstants.LOGOUT);
    tnData.setUserID(isEmployeeName);
    tnData.setMachineName(isMachineName);

    if(vpLoginData != null)
    {
      tnData.setRole(vpLoginData.getRole());
      logOperation(vsUserMessage + vsMachineMessage + vsDeviceMessage
          + "Logged Off");
    }
    else
    {
      tnData.setRole(" ");
      logOperation(vsUserMessage + vsMachineMessage + vsDeviceMessage
          + "Logged Off (login information had already been replaced)");
    }

    /*
     * Delete login information
     */
    TransactionToken vpTT = null;
    try

    {
      vpTT = startTransaction();
      if(vpLoginData != null)
      {
        vpLoginData.setKey(LoginData.USERID_NAME, vpLoginData.getUserID());
        vpLoginData.setKey(LoginData.MACHINENAME_NAME, vpLoginData.getMachineName());
        vpLogin.deleteElement(vpLoginData);
      }
      logTransaction(tnData);
      commitTransaction(vpTT);
    }
    finally
    {
      endTransaction(vpTT);
    }
  }

  /**
   *  Method user login information.
   *  @param isMachineName Device for query.
   *
   *  @return <code>String</code> representing User ID of employee
   *  logged into that device - null if there is none.
   */
  public String getLoginByMachine(String isMachineName) throws DBException
  {
    LoginData loginData = Factory.create(LoginData.class);
    Login login = Factory.create(Login.class);
    loginData.setKey(LoginData.MACHINENAME_NAME, isMachineName);
    loginData = login.getElement(loginData, DBConstants.NOWRITELOCK);

    if (loginData == null)
      return null;

    return loginData.getUserID();
  }

  /**
   *  Method to get current user logins.
   */
  public List<Map> getLoginList() throws DBException
  {
    LoginData lgdata = Factory.create(LoginData.class);
    Login login = Factory.create(Login.class);

    return login.getAllElements(lgdata);
  }

  /**
   *  Method to get current user logins by role.
   */
  public List<Map> getLoginListByRole(String sRole) throws DBException
  {
    LoginData lgdata = Factory.create(LoginData.class);
    lgdata.setKey(LoginData.ROLE_NAME, sRole);

    Login login = Factory.create(Login.class);
    return login.getAllElements(lgdata);
  }

  /**
   *  Method to get a list of matching employee data objects.
   *
   *  @param srchEmployee Employee name to match.
   *  @param match booean as to whether we need an exact match or not
   *  @return List of <code>Maps of EmployeeData</code>.
   *  @exception DBException
   */
  public List<Map> getLoginDataList(String srchEmployee, boolean match) throws DBException
  {
    return Factory.create(Login.class).getLoginDataList(srchEmployee, match);
  }

  /**
   *  Method to get an role data for specified role.
   *
   *  @param roleName Role number.
   *  @return RoleData object containing Role info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public RoleData getRoleData(String roleName) throws DBException
  {
    return Factory.create(Role.class).getRoleData(roleName);
  }

  /**
   *  Method to delete a role.
   *
   *  @param isRole Role.
   *  @exception DBException
   */
  public void deleteRole(String isRole) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      if (roleOptionsExist(isRole))
      {
        deleteRoleOptions(isRole);

        // TODO: Log Transaction?
      }
      Factory.create(Role.class).deleteRole(isRole);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to add a role.
   *
   *  @param rold Filled in role data object.
   *  @exception DBException
   */
  public void addRole(RoleData rold) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Role.class).addRole(rold);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to get a list of matching role names.
   *
   *  @param srchRole Role name to match.
   *  @return List of role names.
   *  @exception DBException
   */
  public String[] getRoleNameList(String srchRole) throws DBException
  {
    return Factory.create(Role.class).getRoleNameList(srchRole);
  }

  /**
   *  Method to get a list of all role data objects.
   *
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   */
  public List<Map> getRoleDataList(boolean includeAll, boolean includeSu) throws DBException
  {
    return getRoleDataList("", includeAll, includeSu);
  }

  /**
   *  Method to get a list of matching role data objects.
   *
   *  @param srchRole Role name to match.
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   */
  public List<Map> getRoleDataList(String srchRole, boolean includeAll, boolean includeSu) throws DBException
  {
    return Factory.create(Role.class).getRoleDataList(srchRole, includeAll, includeSu);
  }

  /**
  *  Method to see if the specified role exists.
  *
  *  @param roleName Role name.
  *  @return boolean of <code>true</code> if it exists.
  *  @exception DBException
  */
  public boolean roleExists(String roleName) throws DBException
  {
    return Factory.create(Role.class).exists(roleName);
  }

  /**
  *  Method to see if the specified role exists.
  *
  *  @param roleName Role name.
  *  @return boolean of <code>true</code> if it exists.
  *  @exception DBException
  */
  public boolean roleEmployeeExists(String roleName) throws DBException
  {
    return Factory.create(Employee.class).employeeRoleExists(roleName);
  }

  /**
   * Method to update a role.
   *
   * @param ipRoleData RoleData with keys and data set
   * @exception DBException
   */
  public void updateRoleInfo(RoleData ipRoleData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(Role.class).modifyElement(ipRoleData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /**
   *  Method to update a role.
   *
   *  @param newRole Filled in role data object.
   *  @param copiedRole
   *  @exception DBException
   */
  public void copyRoleOptions(String newRole, String copiedRole) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      if(this.roleOptionsExist(newRole))
      {
         this.deleteRoleOptions(newRole);
      }
      Factory.create(RoleOption.class).copyRoleOptions(newRole, copiedRole);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }
  /**
   *  Method to get a count of matching roles.
   *
   *  @param srch Role name.
   *  @return integer count of matching roles.
   *  @exception DBException
   */
  public int getRoleCountByName(String srch) throws DBException
  {
    return Factory.create(Role.class).getRoleCountByName(srch);
  }

  /**
   *  Method to get a role option data for specified role option.
   *
   *  @param roleName Role name.
   *  @param category Category name.
   *  @param option Option name.
   *  @return RoleOptionData object containing Role option info. matching our
   *          search criteria.
   *  @exception DBException
   */
   public RoleOptionData getRoleOptionData(String roleName, String category, String option) throws DBException
  {
    return Factory.create(RoleOption.class).getRoleOptionData(roleName, category, option);
  }

  /**
   *  Method to delete a role option.
   *
   *  @param roleName Role.
   *  @param category Category.
   *  @param option Option.
   *  @exception DBException
   */
  public void deleteRoleOption(String roleName, String category, String option) throws DBException
  {
    TransactionToken tt = null;

    tt = startTransaction();

    try
    {
      Factory.create(RoleOption.class).deleteRoleOption(roleName, category, option);
      commitTransaction(tt);
    }
    catch(DBException dbe)
    {
      logException(dbe, "UserServer - deleteRoleOption" + dbe.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

  }

  /**
   *  Method to delete a role option from all roles.
   *
   *  @param isOption - role option to delete.
   *  @param isCategory  - the category from which to delete this option.
   *  @param izPreserveMaster - boolean indicating to delete even from master.
   *  @exception DBException
   */
  public void deleteRoleOption(String isOption, String isCategory, boolean izPreserveMaster) throws DBException
  {
    TransactionToken tt = null;

    tt = startTransaction();

    try
    {
      Factory.create(RoleOption.class).deleteRoleOption(isOption, isCategory, izPreserveMaster);
      commitTransaction(tt);
    }
    catch(DBException dbe)
    {
      logException(dbe, "UserServer - deleteRoleOption" + dbe.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

  }
  /**
   *  Method to delete a role option.
   *
   *  @param roleName Role for which role options should be deleted.
   *  @exception DBException
   */
  public void deleteRoleOptions(String roleName) throws DBException
  {
    TransactionToken tt = null;

    tt = startTransaction();

    try
    {
      Factory.create(RoleOption.class).deleteRoleOptions(roleName);
      commitTransaction(tt);
    }
    catch(DBException dbe)
    {
      logException(dbe, "UserServer - deleteRoleOption" + dbe.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

  }
  /**
   *  Method to add a role option.
   *
   *  @param rold Filled in role option data object.
   *  @exception DBException
   */
  public void addRoleOption(RoleOptionData rold) throws DBException
  {
    TransactionToken tt = null;

    tt = startTransaction();

    try
    {
      Factory.create(RoleOption.class).addRoleOption(rold);
      commitTransaction(tt);
    }
    catch(DBException dbe)
    {
      logException(dbe, "UserServer - addRoleOption" + dbe.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

  }

  /**
   *  Method to get Map objects for all Master role options.
   *
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   *  @UnusedMethod
   */
  public List<Map> getRoleOptionsList() throws DBException
  {
    return Factory.create(RoleOption.class).getRoleOptionsList("");
  }

  /**
   *  Method to get Map objects for matching role options.
   *
   *  @param srchRole Role name to match.
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   */
  public List<Map> getRoleOptionsList(String srchRole) throws DBException
  {
            // Always set srchRole to blank string if a new role is being added
    if( (srchRole.trim().length() <= 0) ||
        (getRoleOptionCountByRole(srchRole) <= 0) )
    {
       srchRole = "";
    }
    return Factory.create(RoleOption.class).getRoleOptionsList (srchRole);
  }

  /**
   *  Method to update a role option.
   *
   *  @param rold Filled in role option data object.
   *  @exception DBException
   */
  public void updateRoleOptionDataInfo(RoleOptionData rold) throws DBException
  {
    TransactionToken tt = null;

    tt = startTransaction();

    try
    {
      Factory.create(RoleOption.class).updateRoleOptionInfo(rold);
      commitTransaction(tt);
    }
    catch(DBException dbe)
    {
      logException(dbe, "UserServer - updateRoleOptionDataInfo" + dbe.getMessage());
    }
    finally
    {
      endTransaction(tt);
    }

  }

  /**
   * Method to update a role option.
   *
   * @param ipOrigData data object containing original data (before change).
   * @param ipModData data object containing changed data.
   * @throws DBException  when there is a database access or modify error.
   */
  public void updateRoleOptionDataInfoForAll(RoleOptionData ipOrigData, RoleOptionData ipModData)
         throws DBException
  {
    TransactionToken tt = null;

    tt = startTransaction();

    try
    {
      Factory.create(RoleOption.class).updateRoleOptionInfoForAll(ipOrigData, ipModData);
      commitTransaction(tt);
    }
    catch(DBException dbe)
    {
      logException(dbe, "UserServer - updateRoleOptionDataInfo" + dbe.getMessage());
      throw dbe;
    }
    finally
    {
      endTransaction(tt);
    }

  }
  /**
   *  Method to verify if a role option exists.
   *
   *  @param roleName Role name.
   *  @param sCategory - Category to Search.
   *  @param sOption - Option to search for.
   *  @return integer count of matching role options.
   *  @exception DBException
   */
  public boolean roleOptionExists(String roleName, String sCategory,
                            String sOption)
  {
    try
    {
      return Factory.create(RoleOption.class).exists(roleName, sCategory, sOption);
    }
    catch(DBException dbe)
    {
       logException(dbe, "roleOption.exists - " + dbe.getMessage());
       return(false);
    }
  }

  /**
   *  Method to verify if a role option exists.
   *
   *  @param roleName Role name.
   *  @return integer count of matching role options.
   *  @exception DBException
   */
  public boolean roleOptionsExist(String roleName)
  {
    try
    {
      return Factory.create(RoleOption.class).roleOptionsExist(roleName);
    }
    catch(DBException dbe)
    {
       logException(dbe, "roleOption.exists - " + dbe.getMessage());
       return(false);
    }
  }

  /**
   *  Method to get a count of role options for this role.
   *
   *  @param roleName Role name.
   *  @return integer count of matching role options.
   *  @exception DBException
   */
  public int getRoleOptionCountByRole(String roleName) throws DBException
  {
    return Factory.create(RoleOption.class).getRoleOptionCountByRole(roleName);
  }

  /**
   *  Method determines if employee is a SuperUser
   *
   *  @param empID <code>String</code> containing employee ID
   *
   *  @return <code>boolean</code> True id Super User
   */
  public boolean isEmployeeSuperUser(String empID) throws DBException
  {
    return Factory.create(TableJoin.class).isEmployeeSuperUser(empID);
  }

  /**
   *  Method determines if an employee is logged in with a given release-to code
   *
   *  @param isReleaseToCode <code>String</code> containing release-to code
   *
   *  @return <code>boolean</code> True employee with release-to code logged in.
   */
  public boolean isRTCLoggedIn(String isReleaseToCode) throws DBException
  {
    EmployeeData vpEmpData = Factory.create(EmployeeData.class);
    vpEmpData.setKey(EmployeeData.RELEASETOCODE_NAME, isReleaseToCode);
    Employee vpEmployee = Factory.create(Employee.class);
    LoginData lgdata = Factory.create(LoginData.class);
    Login login = Factory.create(Login.class);

    List<Map> vpEmpList = vpEmployee.getAllElements(vpEmpData);
    for(Map vpMap: vpEmpList)
    {
      vpEmpData.dataToSKDCData(vpMap);
      lgdata.setKey(LoginData.USERID_NAME, vpEmpData.getUserID());
      if (login.exists(lgdata))
      {
        return true;
      }
    }

    return false;
  }
}

