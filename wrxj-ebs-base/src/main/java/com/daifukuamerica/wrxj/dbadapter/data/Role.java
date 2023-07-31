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

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;
import java.util.Map;

/**
 * A lower level data base object that interfaces to the Role table.
 *
 * @author avt
 * @author A.D.
 * @version 1.0
 * @version 2.0    10/12/04
 */
public class Role extends BaseDBInterface
{
  private RoleData mpRoleData;
  private DBResultSet mpDBResultSet;

  public Role()
  {
    super("Role");
    mpRoleData = Factory.create(RoleData.class);
  }

  /**
   *  Method to delete a role.
   *
   *  @param isRole Role.
   *  @exception DBException
   */
  public void deleteRole(String isRole) throws DBException
  {
    // Delete Role
    mpRoleData.clear();
    mpRoleData.setKey(RoleData.ROLE_NAME, isRole);
    deleteElement(mpRoleData);
  }

  /**
   *  Method to add a role.
   *
   *  @param rold Filled in role data object.
   *  @exception DBException
   */
  public void addRole(RoleData rold) throws DBException
  {
    addElement(rold);
  }

  /**
   *  Method to get a list of matching role names.
   *
   *  @param srchRole Role name to match.
   *  @return String array of role names.
   *  @exception DBException
   */
  public String[] getRoleNameList(String srchRole) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sRole FROM Role ");

    if (!SKDCUserData.isSuperUser() && !SKDCUserData.isAdministrator())
    {
      vpSql.append("WHERE sRole = '").append(SKDCUserData.getRole()).append("' ");
    }
    else
    {
      if (srchRole.trim().length() != 0)
      {
        vpSql.append("WHERE sRole LIKE '").append(srchRole).append("%' ");


        if (!SKDCUserData.isSuperUser())
        {
          vpSql.append(" AND iRoleType = ").append(DBConstants.WORKER);
        }
      }
      else
      {
        if (!SKDCUserData.isSuperUser())
        {
          vpSql.append(" WHERE iRoleType = ").append(DBConstants.WORKER);
        }
      }
    }

    return getList(vpSql.toString(), "SROLE", SKDCConstants.NO_PREPENDER);
  }

  /**
   *  Method to get a list of multiple role data objects.
   *
   *  @param srchRole Role name to match.
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   */
  public List<Map> getRoleDataList(String srchRole, boolean includeAll,
      boolean includeSu) throws DBException
  {
    StringBuilder vpSql = new StringBuilder();
    if(includeSu == true)
    {
      vpSql.append("SELECT * ")
               .append("FROM Role WHERE sRole like \'")
               .append(srchRole)
               .append("%\' ")
               .append(" order by sRole");
    }
    else
    {
      if(includeAll)
      {
        vpSql.append("SELECT * ")
                 .append("FROM Role WHERE sRole like \'")
                 .append(srchRole)
                 .append("%\' and iRoleType != ")
                 .append(DBConstants.CREATOR)
                 .append(" order by sRole");
      }
      else
      { 
        if(srchRole.trim().length() > 0)
        {
          vpSql.append("SELECT * ")
                   .append("FROM Role WHERE sRole like \'")
                   .append(srchRole)
                   .append("%\' and iRoleType != ")
                   .append(DBConstants.CREATOR)
                   .append(" order by sRole");
        }
        else
        {
          return(null);
        }
      }
    }
    return fetchRecords(vpSql.toString());
  }

  /**
   *  Method to get a role data for specified role.
   *
   *  @param roleName Role number.
   *  @return RoleData object containing Role info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public RoleData getRoleData(String roleName) throws DBException
  {
    mpRoleData.clear();
    mpRoleData.setKey(RoleData.ROLE_NAME, roleName);
    return(getElement(mpRoleData, DBConstants.NOWRITELOCK));
  }

  /**
   *  Method to get a role object for specified role.
   *
   *  @param roleName Role number.
   *  @return Role object containing Role info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public Role getRoleObj(String roleName) throws DBException
  {
    Role thisRole = Factory.create(Role.class);

    mpDBResultSet = execute("SELECT * FROM role WHERE sRole = ?", roleName);
    switch (mpDBResultSet.getRowCount()){
      case 0:  // not found
        return (null);
      case 1:
        Map row;
        while (mpDBResultSet.hasNext())  // should be just one row
        {
          row = (Map) mpDBResultSet.next();
          thisRole.mpRoleData.setRole(DBHelper.getStringField(row,"sRole"));
          thisRole.mpRoleData.setRoleDescription(DBHelper.getStringField(row,"sRoleDescription"));
          thisRole.mpRoleData.setRoleType(DBHelper.getIntegerField(row,"iRoleType"));
        }
        break;
      default:  // Multiple matches
        DBHelper.dbThrow("Multiple matches on key: " + roleName);
        return (null);
    }  //switch
    return(thisRole);
  }

  /**
   *  Method to see if the specified role exists.
   *
   *  @param roleName Role name.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean exists(String roleName) throws DBException
  {
    mpRoleData.clear();
    mpRoleData.setKey(RoleData.ROLE_NAME, roleName);
    return(getCount(mpRoleData) > 0);
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpRoleData      = null;
    mpDBResultSet = null;
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
    int roleCount = -1;

    mpDBResultSet = execute(
        "SELECT COUNT (sRole) AS \"rowCount\" FROM role WHERE sRole like ?",
        srch + "%");
    Map row;
    while (mpDBResultSet.hasNext())  // should be just one
    {
      row = (Map) mpDBResultSet.next();
      roleCount = DBHelper.getIntegerField(row,"rowCount");
    }

    return(roleCount);
  }
}
