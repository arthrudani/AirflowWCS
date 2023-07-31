package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Role Option Table fields.
 *
 * @author A.D.
 * @since  18-Nov-2008
 */
public enum RoleOptionEnum implements TableEnum
{
  ADDALLOWED("IADDALLOWED"),
  BUTTONBAR("IBUTTONBAR"),
  CATEGORY("SCATEGORY"),
  CLASSNAME("SCLASSNAME"),
  DELETEALLOWED("IDELETEALLOWED"),
  ICONNAME("SICONNAME"),
  MODIFYALLOWED("IMODIFYALLOWED"),
  OPTION("SOPTION"),
  ROLE("SROLE"),
  VIEWALLOWED("IVIEWALLOWED");

  private String msMessageName;

  RoleOptionEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
