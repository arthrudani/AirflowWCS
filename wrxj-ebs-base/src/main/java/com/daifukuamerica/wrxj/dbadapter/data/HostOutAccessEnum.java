package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the HostOutAccess Table fields.
 *  @author A.D.
 *  @since  19-Nov-2008
 */
public enum HostOutAccessEnum implements TableEnum
{
  ENABLED("IENABLED"),
  HOSTNAME("SHOSTNAME"),
  MESSAGEIDENTIFIER("SMESSAGEIDENTIFIER");

  private String msMessageName;

  HostOutAccessEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }

}
