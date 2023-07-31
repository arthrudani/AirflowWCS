package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the SysConfig Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum SysConfigEnum implements TableEnum
{
  GROUP("SGROUP"),
  PARAMETERNAME("SPARAMETERNAME"),
  PARAMETERVALUE("SPARAMETERVALUE"),
  DESCRIPTION("SDESCRIPTION"),
  ENABLED("IENABLED"),
  SCREENCHANGEALLOWED("ISCREENCHANGEALLOWED"),
  SCREENTYPE("SSCREENTYPE");

  private String msMessageName;

  SysConfigEnum(String isMessageName)
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