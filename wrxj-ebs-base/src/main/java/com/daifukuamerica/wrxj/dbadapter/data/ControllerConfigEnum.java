package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the ControllerConfig Table fields.
 *
 *  @author A.D.
 *  @since  09-Sep-2007
 */
public enum ControllerConfigEnum implements TableEnum
{
  CONTROLLER("SCONTROLLER"),
  PROPERTYNAME("SPROPERTYNAME"),
  PROPERTYVALUE("SPROPERTYVALUE"),
  PROPERTYDESC("SPROPERTYDESC"),
  ENABLED("IENABLED"),
  SCREENCHANGEALLOWED("ISCREENCHANGEALLOWED");

  private String msMessageName;

  ControllerConfigEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

 /**
  * {@inheritDoc}
  */
  public String getName()
  {
    return(msMessageName);
  }
}
