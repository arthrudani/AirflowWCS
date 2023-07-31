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
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;
import java.util.Map;

/**
 * Title: Login Class
 * Description: Provides all functionality to the login object
 * Copyright:    Copyright (c) 2005
 * @author       sbw
 * @version      1.0
 * <BR>Created:  17-Mar-05<BR>
 * Copyright (c) 2005<BR>
 * Company:  Daifuku America Corporation
 */
public class Login extends BaseDBInterface
{
  private LoginData mpLoginData;

  public Login()
  {
    super("Login");
    mpLoginData = Factory.create(LoginData.class);
  }

  /**
   * Return true if the specified user/machine is already logged in.
   * 
   * @param isUser
   * @param isMachineName - the machine to look up
   * @return
   * @throws DBException
   */
  public boolean userLoggedIn(String isUser, String isMachineName)
      throws DBException
  {
    mpLoginData.clear();
    if (isUser.trim().length() > 0)
    {
      mpLoginData.setKey(LoginData.USERID_NAME, isUser);
    }
    if (isMachineName.trim().length() > 0)
    {
      mpLoginData.setKey(LoginData.MACHINENAME_NAME, isMachineName);
    }
    
    return getAllElements(mpLoginData).size() > 0;
  }
  
  /**
   * Method to get a list of multiple login data objects.
   * 
   * @param srchUser Employee name to match.
   * @param match - boolean as to whether we need to match exactly or not
   * @return List of <code>Map</code> objects.
   * @exception DBException
   */
  public List<Map> getLoginDataList(String srchUser, boolean match) throws DBException
  {
    // Clear out Sql String buffer.
    StringBuilder vpSql = new StringBuilder("SELECT * FROM Login ");
    if(match == true)
    {
      vpSql.append(" WHERE sUserID = '").append(srchUser).append("'");
    }
    else
    {
      vpSql.append(" WHERE sUserID like '").append(srchUser).append("%'");
    }

    if (SKDCUserData.isSuperUser())
    {
      // We are the super user or role of skdaifuku so show all data
    }
    else if (SKDCUserData.isAdministrator())
    {
      // we are the administrator so show everybody except daifuku role users
      vpSql.append(" AND sRole != '")
               .append(SKDCConstants.ROLE_DAC_SUPERROLE).append("' ");
    }
    else
    {
      // We are a regular employee so just display normal login data
      vpSql.append(" AND sRole != '")
               .append(SKDCConstants.ROLE_DAC_SUPERROLE).append("' ")
               .append(" AND sRole != '")
               .append(SKDCConstants.ROLE_ADMINISTRATOR).append("' ");
    }
    vpSql.append(" ORDER BY sUserID ");
    
    return fetchRecords(vpSql.toString());
  }
}

