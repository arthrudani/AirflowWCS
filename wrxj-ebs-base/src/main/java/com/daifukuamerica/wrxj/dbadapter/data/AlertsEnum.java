package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Load Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum AlertsEnum implements TableEnum
{	
  ALERTID("SALERTID"),
  TIMESTAMP("DTIMESTAMP"),
  EVENTCODE("IEVENTCODE"),
  DESCRIPTION("DESCRIPTION"),
  ACTIVEFLAG("IACTIVEFLAG");
  

  private String msMessageName;

  AlertsEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }
  @Override
  public String getName()
  {
    return(msMessageName);
  }
}
