package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Load Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum SynonymEnum implements TableEnum
{
  ITEM("SITEM"),
  SYNONYM("SSYNONYM");

  private String msMessageName;

  SynonymEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
