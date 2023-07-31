package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.EmployeeEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Employee Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.T.
 * @version      1.0
 */
public class EmployeeData extends AbstractSKDCData
{
  public static final String LANGUAGE_NAME           = LANGUAGE.getName();
  public static final String PASSWORD_NAME           = PASSWORD.getName();
  public static final String PASSWORDEXPIRATION_NAME = PASSWORDEXPIRATION.getName(); 
  public static final String RELEASETOCODE_NAME      = RELEASETOCODE.getName();
  public static final String REMEMBERLASTLOGIN_NAME  = REMEMBERLASTLOGIN.getName();
  public static final String ROLE_NAME               = ROLE.getName();
  public static final String USERID_NAME             = USERID.getName();
  public static final String USERNAME_NAME           = USERNAME.getName();

          // Private Data
  protected String sUserID = "";
  protected String sUserName = "";
  protected String sRole = "";
  protected String sPassword = "";
  protected String sReleaseToCode = "";
  protected Date   dPasswordExpiration = null;
  protected int    iRememberLastLogin = DBConstants.NO;
  protected String sLanguage = "";
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public EmployeeData()
  {
    super();
    initColumnMap(mpColumnMap, EmployeeEnum.class);
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sUserID:" + sUserID +
               "\nsUserName:" + sUserName +
               "\nsRole:" + sRole +
               "\nsPassword:" + sPassword +
               "\nsReleaseToCode:" + sReleaseToCode +
               "\ndPasswordExpiration:" + sdf.format(dPasswordExpiration).toString() + 
               "\niRememberLastLogin:" + iRememberLastLogin +
               "\nsLanguage:"+ sLanguage + "\n";

    s += super.toString();

    return(s);
  }

  /**
   *  Method to make a deep copy of this object.
   *
   *  @return copy of <code>EmployeeData</code>.
   */
  @Override
  public EmployeeData clone()
  {
    EmployeeData vpClonedData = (EmployeeData)super.clone();
    if (dPasswordExpiration != null)
    {
      vpClonedData.dPasswordExpiration = (Date)dPasswordExpiration.clone();
    }
    return vpClonedData;
  }

  /**
   * Defines equality between two EmployeeData objects.
   *
   * @param  absEMP <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>EmployeeData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absEMP)
  {
    EmployeeData em = (EmployeeData)absEMP;
    return(em.sUserID.equals(this.sUserID));
  }

  /*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches User ID
   * @return User ID as string
   */
  public String getUserID()
  {
    return(sUserID);
  }
  /**
   * Fetches Password Expiration Date
   * @return Password Expiration as Date
   */
  public Date getPasswordExpiration()
  {
    return(dPasswordExpiration);
  }
  /**
   * Fetches Role
   * @return Role as string
   */
  public String getRole()
  {
    return(sRole);
  }
  /**
   * Fetches Password
   * @return Password as string
   */
  public String getPassword()
  {
    return(sPassword);
  }
  /**
   * Fetches User Name
   * @return User Name as string
   */
  public String getUserName()
  {
    return(sUserName);
  }

  /**
   * Fetches ReleaseToCode
   * @return ReleaseToCode as string
   */
  public String getReleaseToCode()
  {
    return(sReleaseToCode);
  }
  
  /**
   * Get the RememberLastLogin flag
   * @return
   */
  public int getRememberLastLogin()
  {
    return iRememberLastLogin;
  }
  
  /**
   * Get the language
   * @return
   */
  public String getLanguage()
  {
    return sLanguage;
  }

  /*---------------------------------------------------------------------------
   ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets User ID value.
   */
  public void setUserID(String siUserID)
  {
    this.sUserID = checkForNull(siUserID);
    addColumnObject(new ColumnObject(USERID_NAME, siUserID));
  }
  /**
   * Sets User Name value.
   */
  public void setUserName(String siUserName)
  {
    this.sUserName = checkForNull(siUserName);
    addColumnObject(new ColumnObject(USERNAME_NAME, siUserName));
  }
  /**
   * Sets Role value.
   */
  public void setRole(String siRole)
  {
    this.sRole = checkForNull(siRole);
    addColumnObject(new ColumnObject(ROLE_NAME, siRole));
  }
  /**
   * Sets Password value.
   */
  public void setPassword(String ssPassword)
  {
    this.sPassword = checkForNull(ssPassword);
    addColumnObject(new ColumnObject(PASSWORD_NAME, ssPassword));
  }
  /**
   * Sets ReleaseToCode value.
   */
  public void setReleaseToCode(String siReleaseToCode)
  {
    this.sReleaseToCode = checkForNull(siReleaseToCode);
    addColumnObject(new ColumnObject(RELEASETOCODE_NAME, siReleaseToCode));
  }
  /**
   * Sets Password Expiration value.
   */
  public void setPasswordExpiration(Date diPasswordExpiration)
  {
    dPasswordExpiration = diPasswordExpiration;
    addColumnObject(new ColumnObject(PASSWORDEXPIRATION_NAME, diPasswordExpiration));
  }
  
  /**
   * Set the RememberLastLogin flag
   * @param inRememberLastLogin
   */
  public void setRememberLastLogin(int inRememberLastLogin)
  {
    iRememberLastLogin = inRememberLastLogin;
    addColumnObject(new ColumnObject(REMEMBERLASTLOGIN_NAME, inRememberLastLogin));
  }

  /**
   * Set the language
   * @param isLanguage
   */
  public void setLanguage(String isLanguage)
  {
    sLanguage = checkForNull(isLanguage);
    addColumnObject(new ColumnObject(LANGUAGE_NAME, isLanguage));
  }

  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return super.setField(isColName, ipColValue);
    }

    switch((EmployeeEnum)vpEnum)
    {
      case LANGUAGE:
        setLanguage((String)ipColValue);
        break;
        
      case PASSWORD:
        setPassword((String)ipColValue);
        break;

      case PASSWORDEXPIRATION:
        setPasswordExpiration(((Date)ipColValue));
        break;

      case RELEASETOCODE:
        setReleaseToCode((String)ipColValue);
        break;

      case REMEMBERLASTLOGIN:
        setRememberLastLogin((Integer)ipColValue);
        break;
        
      case ROLE:
        setRole((String)ipColValue);
        break;

      case USERID:
        setUserID((String)ipColValue);
        break;

      case USERNAME:
        setUserName((String)ipColValue);
    }

    return(0);
  }
}
