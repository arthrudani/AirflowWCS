package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.RoleEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 * Class to handle Role Data operations. This class treats columns, and Keys as
 * Objects.
 * 
 * @author A.T.
 * @version 1.0
 */

public class RoleData extends AbstractSKDCData
{
  // Column names
  public static final String ROLE_NAME = ROLE.getName();
  public static final String ROLEDESCRIPTION_NAME = ROLEDESCRIPTION.getName();
  public static final String ROLETYPE_NAME = ROLETYPE.getName();

  // Private Data
  private String sRole = "";
  private String sRoleDescription = "";
  private int iRoleType = 0;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  /**
   * Constructor
   */
  public RoleData()
  {
    super();
    initColumnMap(mpColumnMap, RoleEnum.class);
  }

  /*========================================================================*/
  /* Overridden methods                                                     */
  /*========================================================================*/
  
  /**
   * @see com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData#clear()
   */
  @Override
  public void clear()
  {
    super.clear();
    
    sRole = "";
    sRoleDescription = "";
    iRoleType = 0;
  }
  
  /**
   * Defines equality between two RoleData objects.
   * 
   * @param absROLE <code>AbstractSKDCData</code> reference whose runtime type
   *          is expected to be <code>RoleData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absROLE)
  {
    RoleData rl = (RoleData)absROLE;
    return (rl.sRole.equals(sRole));
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sRole:" + sRole 
             + "\nsRoleDescription:" + sRoleDescription;

    try
    {
      s += "\niRoleType:" + DBTrans.getStringValue(ROLETYPE_NAME, iRoleType)
          + "\n";
    }
    catch (NoSuchFieldException e)
    {
      s += "\niRoleType:" + iRoleType + "\n";
    }
    s += super.toString();

    return (s);
  }

  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/

  /**
   * Fetches Role
   * 
   * @return Role as string
   */
  public String getRole()
  {
    return sRole;
  }

  /**
   * Fetches Role Description
   * 
   * @return Role Description as string
   */
  public String getRoleDescription()
  {
    return sRoleDescription;
  }

  /**
   * Fetches Role Type
   * 
   * @return Role Type as integer
   */
  public int getRoleType()
  {
    return iRoleType;
  }

  /*========================================================================*/
  /*  Setters                                                               */
  /*========================================================================*/
  
  /**
   * Sets Role value.
   */
  public void setRole(String isRole)
  {
    sRole = checkForNull(isRole);
    addColumnObject(new ColumnObject(ROLE_NAME, isRole));
  }

  /**
   * Sets Role Description value.
   */
  public void setRoleDescription(String isDesc)
  {
    if (isDesc == null || isDesc.trim().length() < 1)
    {
      sRoleDescription = " ";
    }
    else
    {
      sRoleDescription = isDesc;
    }
    addColumnObject(new ColumnObject(ROLEDESCRIPTION_NAME, sRoleDescription));
  }

  /**
   * Sets Role Type value.
   */
  public void setRoleType(int inRoleType)
  {
    iRoleType = inRoleType;
    addColumnObject(new ColumnObject(ROLETYPE_NAME, Integer.valueOf(inRoleType)));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return super.setField(isColName, ipColValue);
    }

    switch ((RoleEnum)vpEnum)
    {
      case ROLE:
        setRole(ipColValue.toString());
        break;
      case ROLEDESCRIPTION:
        setRoleDescription(ipColValue.toString());
        break;
      case ROLETYPE:
        setRoleType((Integer)ipColValue);
        break;
    }
    
    return (0);
  }
}
