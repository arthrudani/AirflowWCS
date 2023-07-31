package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Load Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum ZoneGroupEnum implements TableEnum
{
  ZONEGROUP("SZONEGROUP"),
  PRIORITY("IPRIORITY"),
  ZONE("SZONE");

  private String msMessageName;

  ZoneGroupEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
