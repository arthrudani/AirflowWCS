package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Station Table fields.
 *
 *  @author A.D.
 *  @since  25-Jun-2007
 */
public enum RouteEnum implements TableEnum
{
  ROUTEID("SROUTEID"),
  FROMID("SFROMID"),
  DESTID("SDESTID"),
  FROMTYPE("IFROMTYPE"),
  DESTTYPE("IDESTTYPE"),
  ROUTEONOFF("IROUTEONOFF");

  private String msMessageName;

  RouteEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
