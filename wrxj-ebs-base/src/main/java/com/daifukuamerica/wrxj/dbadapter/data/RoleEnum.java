package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Role Option Table fields.
 *
 * @author A.D.
 * @since  18-Nov-2008
 */
public enum RoleEnum implements TableEnum
{
  ROLE("SROLE"),
  ROLEDESCRIPTION("SROLEDESCRIPTION"),
  ROLETYPE("IROLETYPE");

  private String msMessageName;

  RoleEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
