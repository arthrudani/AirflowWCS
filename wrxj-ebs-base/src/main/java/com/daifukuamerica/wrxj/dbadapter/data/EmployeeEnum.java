package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Employee Table fields.
 *
 *  @author A.D.
 *  @since  18-Nov-2007
 */
public enum EmployeeEnum  implements TableEnum
{
  LANGUAGE("SLANGUAGE"),
  PASSWORD("SPASSWORD"),
  PASSWORDEXPIRATION("DPASSWORDEXPIRATION"),
  RELEASETOCODE("SRELEASETOCODE"),
  REMEMBERLASTLOGIN("IREMEMBERLASTLOGIN"),
  ROLE("SROLE"),
  USERID("SUSERID"),
  USERNAME("SUSERNAME");

  private String msMessageName;

  EmployeeEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
