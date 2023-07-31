package com.daifukuamerica.wrxj.dbadapter.data;

/*
 *                  Daifuku America Corporation
 *                     International Center
 *                 5202 Douglas Corrigan Way
 *              Salt Lake City, Utah  84116-3192
 *                      (801) 359-9900
 *
 * Copyright 2008 Daifuku America Corporation.  All Rights Reserved.
 *
 * THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
 * NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED, COPIED, DISTRIBUTED,
 * REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
 * COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
 * CONSENT OF Daifuku America Corporation.  ANY USE OR EXPLOITATION OF THIS WORK
 * WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
 * LIABILITY.
 */   

import static com.daifukuamerica.wrxj.dbadapter.data.LoginEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Title:  Class to handle LoginData Object.
 *   Description : Handles all data for Login
 * @author       sbw
 * @version      1.0
 * <BR>Created:  17-Mar-05<BR>
 * Copyright (c) 2005<BR>
 * Company:  Daifuku America Corporation
 */
public class LoginData extends AbstractSKDCData
{
  public static final String IPADDRESS_NAME   = IPADDRESS.getName();
  public static final String LOGINTIME_NAME   = LOGINTIME.getName();
  public static final String MACHINENAME_NAME = MACHINENAME.getName();
  public static final String ROLE_NAME        = ROLE.getName();
  public static final String USERID_NAME      = USERID.getName();

  protected String sUserID      = "";
  protected String sRole        = "";
  protected String sMachineName = "";
  protected String sIPAddress   = "";
  protected Date dLoginTime;

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /**
   * Constructor
   */
  public LoginData()
  {
    super();
    initColumnMap(mpColumnMap, LoginEnum.class);
    clear();                      // set all values to default
  }

  /**
   * Sets all data values to defaults where appropriate (like translations) and
   * clear out the rest.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour.

    sMachineName = "";
    sIPAddress = "";
    dLoginTime = null;
    sUserID = "";
    sRole = "";
  }

  /**
   * Puts all data into a string
   */
  @Override
  public String toString()
  {
    String str = "\nsRole:"        + sRole      + 
                 "\nsUserID:"      + sUserID    +
                 "\nsMachineName:" + sMachineName  +
                 "\nsIPAddress:"   + sIPAddress +
                 "\ndLoginTime:"   + dLoginTime;

    str += super.toString();

    return(str);
  }

  @Override
  public boolean equals(AbstractSKDCData absLogin)
  {
    LoginData li = (LoginData)absLogin;
    return getUserID().equals(li.getUserID()) && 
           getMachineName().equals(li.getMachineName());
  }

/*---------------------------------------------------------------------------
               ******** Column Get methods go here. ********
  ---------------------------------------------------------------------------*/
  public String getUserID()         {    return(sUserID);       }
  public String getRole()           {    return(sRole);         }
  public String getMachineName()    {    return(sMachineName);  }
  public String getIPAddress()      {    return(sIPAddress);    }
  public Date   getLoginTime()      {    return(dLoginTime);    }


/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  public void setUserID(String isUserID)
  {
    sUserID = isUserID;
    addColumnObject(new ColumnObject(USERID_NAME, sUserID));
  }
  public void setRole(String isRole)
  {
    sRole = isRole;
    addColumnObject(new ColumnObject(ROLE_NAME, sRole));
  }
  public void setMachineName(String isMachineName)
  {
    sMachineName = isMachineName;
    addColumnObject(new ColumnObject(MACHINENAME_NAME, sMachineName));
  }
  public void setIPAddress (String isIPAddress)
  {
    sIPAddress = isIPAddress;
    addColumnObject(new ColumnObject(IPADDRESS_NAME, sIPAddress));
  }
  public void setLoginTime(Date idLoginTime)
  {
    dLoginTime = idLoginTime;
    addColumnObject(new ColumnObject(LOGINTIME_NAME, dLoginTime));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String colName, Object colValue)
  {
    TableEnum vpEnum = mpColumnMap.get(colName);
    if (vpEnum == null)
    {
      // Special case for when the user specified column is unknown.
      return(super.setField(colName, colValue));
    }
    
    switch ((LoginEnum)vpEnum)
    {
      case IPADDRESS:
        setIPAddress((String)colValue);
        break;
        
      case LOGINTIME:
        setLoginTime(((Date)colValue));
        break;
        
      case MACHINENAME:
        setMachineName((String)colValue);
        break;
        
      case ROLE:
        setRole((String)colValue);
        break;
        
      case USERID:
        setUserID((String)colValue);
        break;
    }
    return 0;
  }
}
