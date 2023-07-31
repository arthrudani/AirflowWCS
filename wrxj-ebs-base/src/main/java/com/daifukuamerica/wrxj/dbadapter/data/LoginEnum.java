package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Login Table fields.
 *
 *  @author mike
 *  @since  24-Sep-2008
 */
public enum LoginEnum implements TableEnum
{
  IPADDRESS("SIPADDRESS"),
  LOGINTIME("DLOGINTIME"),
  MACHINENAME("SMACHINENAME"),
  ROLE("SROLE"),
  USERID("SUSERID");

  private String msMessageName;

  LoginEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}