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
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;
import java.util.Map;

/**
 * A lower level data base object that interfaces to the RoleOption table.
 *
 * @author avt
 * @version 1.0
 */
public class RoleOption extends BaseDBInterface
{
  private RoleOptionData mpOptionData;
  private DBResultSet mpDBResultSet;

  public RoleOption()
  {
    super("RoleOption");
    mpOptionData = Factory.create(RoleOptionData.class);
  }

  /**
   *  Method to delete a role option.
   *
   *  @param isRole Role of role option to be deleted.
   *  @param isCategory Category of role option to be deleted.
   *  @param isOption Option to be deleted.
   *  @exception DBException
   */
  public void deleteRoleOption(String isRole, String isCategory, String isOption)
         throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setKey(RoleOptionData.ROLE_NAME, isRole);
    mpOptionData.setKey(RoleOptionData.CATEGORY_NAME, isCategory);
    mpOptionData.setKey(RoleOptionData.OPTION_NAME, isOption);
    deleteElement(mpOptionData);
  }

  /**
   *  Method to delete a role option.
   *
   *  @param isOption Option to be deleted from all roles.
   *  @param isCategory
   *  @param izPreserveMaster if set to <code>true</code> don't delete option in
   *         Master and DAC super roles.
   *  @exception DBException
   */
  public void deleteRoleOption(String isOption, String isCategory, boolean izPreserveMaster)
      throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setKey(RoleOptionData.OPTION_NAME, isOption);
    mpOptionData.setKey(RoleOptionData.CATEGORY_NAME, isCategory);

    if (izPreserveMaster)
    {
      mpOptionData.setKey(RoleOptionData.ROLE_NAME, SKDCConstants.ROLE_MASTER, KeyObject.NOT_EQUAL, KeyObject.AND);
      mpOptionData.setKey(RoleOptionData.ROLE_NAME, SKDCConstants.ROLE_DAC_SUPERROLE, KeyObject.NOT_EQUAL, KeyObject.AND);
    }
    deleteElement(mpOptionData);
  }

  /**
   * Method to delete a role.
   *
   * @param isRole Role for which options are to be deleted.
   * @exception DBException
   */
  public void deleteRoleOptions(String isRole) throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setKey(RoleOptionData.ROLE_NAME, isRole);
    deleteElement(mpOptionData);
  }

 /**
  *  Method to add a role option.
  *
  *  @param rod Filled in role option data object.
  *  @exception DBException
  */
  public void addRoleOption(RoleOptionData rod) throws DBException
  {
    addElement(rod);
  }

  /**
   *  Method to get a list of multiple role option data objects.
   *
   *  @param srchRole Role name to match.
   *  @return List of <code>Map</code> objects.
   *  @exception DBException
   */
  // TODO: This method has broken code.
  public List<Map> getRoleOptionsList(String srchRole) throws DBException
  {
    List<Map> roleOptList = null;
    StringBuilder vpSql = new StringBuilder();
    if((srchRole.trim().length() <= 0) )
    {
      vpSql.append("SELECT sRole, sCategory, sOption, sIconName, sClassName, ")
               .append(DBConstants.NO).append(" AS iViewAllowed, ")
               .append(DBConstants.NO).append(" AS iButtonBar, ")
               .append(DBConstants.NO).append(" AS iAddAllowed, ")
               .append(DBConstants.NO).append(" AS iModifyAllowed, ")
               .append(DBConstants.NO).append(" AS iDeleteAllowed ")
               .append(" FROM roleoption WHERE sRole = '")
               .append(SKDCConstants.ROLE_MASTER)
               .append("' ORDER BY SCATEGORY, SOPTION");

      roleOptList = fetchRecords(vpSql.toString());
    }
    else
    {
      vpSql.append("SELECT sRole, sCategory, sOption, sIconName, sClassName, ")
               .append("iViewAllowed, iButtonBar, iAddAllowed, iModifyAllowed, iDeleteAllowed ")
               .append(" FROM ROLEOPTION WHERE SROLE = '")
               .append(srchRole)
               .append("' ORDER BY SCATEGORY, SOPTION");
      roleOptList = fetchRecords(vpSql.toString());

      vpSql.setLength(0);
      vpSql.append("SELECT ")
               .append("sRole, sCategory, sOption, sIconName, sClassName, ")
               .append(DBConstants.NO).append(" AS iViewAllowed, ")
               .append(DBConstants.NO).append(" AS iButtonBar, ")
               .append(DBConstants.NO).append(" AS iAddAllowed, ")
               .append(DBConstants.NO).append(" AS iModifyAllowed, ")
               .append(DBConstants.NO).append(" AS iDeleteAllowed ")
               .append("FROM ROLEOPTION WHERE SROLE = '")
               .append(SKDCConstants.ROLE_MASTER)
               .append("' AND SOPTION NOT IN ")
               .append("(SELECT SOPTION FROM ROLEOPTION WHERE SROLE = '")
               .append(srchRole)
               .append("') ORDER BY SCATEGORY, SOPTION");

      roleOptList.addAll(fetchRecords(vpSql.toString()));
    }

    return roleOptList;
  }

  /**
   *  Method to get a role option data for specified role option.
   *
   *  @param isRole Role name.
   *  @param isCategory Category name.
   *  @param isOption Option name.
   *  @return RoleOptionData object containing Role option info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public RoleOptionData getRoleOptionData(String isRole, String isCategory,
      String isOption) throws DBException
  {
    mpOptionData.setKey(RoleOptionData.ROLE_NAME, isRole);
    mpOptionData.setKey(RoleOptionData.CATEGORY_NAME, isCategory);
    mpOptionData.setKey(RoleOptionData.OPTION_NAME, isOption);
    return(getElement(mpOptionData, DBConstants.NOWRITELOCK));
  }

  /**
   *  Method to get a role option object for specified role option.
   *
   *  @param isRole Role name.
   *  @param isCategory Category name.
   *  @param isOption Option name.
   *  @return RoleOption object containing Role option info. matching our
   *          search criteria.
   *  @exception DBException
   */
  public RoleOption getRoleObj(String isRole, String isCategory, String isOption)
      throws DBException
  {
    RoleOption thisRoleOption = Factory.create(RoleOption.class);
    RoleOptionData thisRoleOptionData = thisRoleOption.mpOptionData;

    mpDBResultSet = execute("SELECT * FROM roleoption WHERE sRole = ?"
        + " and sCategory = ? AND sOption = ?", isRole, isCategory, isOption);
    switch (mpDBResultSet.getRowCount())
    {
      case 0: // not found
        return (null);
      case 1:
        Map row;
        while (mpDBResultSet.hasNext()) // should be just one row
        {
          row = (Map) mpDBResultSet.next();
          thisRoleOptionData.setRole(DBHelper.getStringField(row,
              RoleOptionData.ROLE_NAME));
          thisRoleOptionData.setCategory(DBHelper.getStringField(row,
              RoleOptionData.CATEGORY_NAME));
          thisRoleOptionData.setOption(DBHelper.getStringField(row,
              RoleOptionData.OPTION_NAME));
          thisRoleOptionData.setIconName(DBHelper.getStringField(row,
              RoleOptionData.ICONNAME_NAME));
          thisRoleOptionData.setClassName(DBHelper.getStringField(row,
              RoleOptionData.CLASSNAME_NAME));
          thisRoleOptionData.setButtonBar(DBHelper.getIntegerField(row,
              RoleOptionData.BUTTONBAR_NAME));
          thisRoleOptionData.setAddAllowed(DBHelper.getIntegerField(row,
              RoleOptionData.ADDALLOWED_NAME));
          thisRoleOptionData.setModifyAllowed(DBHelper.getIntegerField(row,
              RoleOptionData.MODIFYALLOWED_NAME));
          thisRoleOptionData.setDeleteAllowed(DBHelper.getIntegerField(row,
              RoleOptionData.DELETEALLOWED_NAME));
        }
        break;
      default: // Multiple matches
        DBHelper.dbThrow("Multiple matches on key: " + isRole + "+"
            + isCategory + "+" + isOption);
        return (null);
    } // switch
    return (thisRoleOption);
  }

  /**
   * Method to see if the specified role option exists.
   *
   * @param isRole Role name.
   * @param isCategory Category name.
   * @param isOption Option namee.
   * @return boolean of <code>true</code> if it exists.
   * @exception DBException
   */
  public boolean exists(String isRole, String isCategory, String isOption) throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setKey(RoleOptionData.ROLE_NAME, isRole);
    mpOptionData.setKey(RoleOptionData.CATEGORY_NAME, isCategory);
    mpOptionData.setKey(RoleOptionData.OPTION_NAME, isOption);
    return(getCount(mpOptionData) > 0);
  }

  /**
   *  Method to see if the specified role has options.
   *
   *  @param isRole Role name.
   *  @return boolean of <code>true</code> if it exists.
   *  @exception DBException
   */
  public boolean roleOptionsExist(String isRole) throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setKey(RoleOptionData.ROLE_NAME, isRole);
    return(getCount(mpOptionData) > 0);
  }

  /**
   *  Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpDBResultSet = null;
  }

  /**
   *  Method to update a role option.
   *
   *  @param rod Filled in role option data object.
   *  @exception DBException
   */
  public void updateRoleOptionInfo(RoleOptionData rod) throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setIconName(rod.getIconName());
    mpOptionData.setClassName(rod.getClassName());
    mpOptionData.setButtonBar(rod.getButtonBar());
    mpOptionData.setAddAllowed(rod.getAddAllowed());
    mpOptionData.setModifyAllowed(rod.getModifyAllowed());
    mpOptionData.setDeleteAllowed(rod.getDeleteAllowed());
    mpOptionData.setViewAllowed(rod.getViewAllowed());

    mpOptionData.setKey(RoleOptionData.ROLE_NAME, rod.getRole());
    mpOptionData.setKey(RoleOptionData.CATEGORY_NAME, rod.getCategory());
    mpOptionData.setKey(RoleOptionData.OPTION_NAME, rod.getOption());
    modifyElement(mpOptionData);
  }

  /**
   *  Method to update a role option for all users that have it.
   *
   *  @param ipOrigData data object containing original data (before change).
   *  @param ipModData data object containing changed data.
   *  @exception DBException
   */
  public void updateRoleOptionInfoForAll(RoleOptionData ipOrigData, RoleOptionData ipModData)
         throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setIconName(ipModData.getIconName());
    mpOptionData.setClassName(ipModData.getClassName());
    mpOptionData.setCategory(ipModData.getCategory());

    mpOptionData.setKey(RoleOptionData.ROLE_NAME, ipOrigData.getRole());
    mpOptionData.setKey(RoleOptionData.CATEGORY_NAME, ipOrigData.getCategory());
    mpOptionData.setKey(RoleOptionData.OPTION_NAME, ipOrigData.getOption());

    modifyElement(mpOptionData);
  }

  /**
   *  Method to copy role options from a specified role to a specified role.
   *
   *  @param newRole the role to copy into.
   *  @param copiedRole the role to copy from.
   *  @exception DBException
   */
  public void copyRoleOptions(String newRole, String copiedRole)
      throws DBException
  {
    RoleOptionData mpRoleData = Factory.create(RoleOptionData.class);
    mpRoleData.setKey(RoleOptionData.ROLE_NAME, copiedRole);
    List<Map> mpList = getAllElements(mpRoleData);
    for(Map mpmap :mpList)
    {
      mpRoleData.clear();
      mpRoleData.dataToSKDCData(mpmap);
      mpRoleData.setRole(newRole);
      addElement(mpRoleData);
    }
  }

  /**
   *  Method to get a count of matching roles options.
   *
   *  @param srch Role name.
   *  @return integer count of matching role options.
   *  @exception DBException
   */
  public int getRoleOptionCountByRole(String srch) throws DBException
  {
    mpOptionData.clear();
    mpOptionData.setKey(RoleOptionData.ROLE_NAME, srch, KeyObject.LIKE);
    return(getCount(mpOptionData));
  }
}