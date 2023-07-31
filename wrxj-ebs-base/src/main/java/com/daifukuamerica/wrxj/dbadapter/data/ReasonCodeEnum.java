package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Role Option Table fields.
 *
 * @author A.D.
 * @since  18-Nov-2008
 */
public enum ReasonCodeEnum implements TableEnum
{
  REASONCODE("SREASONCODE"),
  DESCRIPTION("SDESCRIPTION"),
  REASONCATEGORY("IREASONCATEGORY");

  private String msMessageName;

  ReasonCodeEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
