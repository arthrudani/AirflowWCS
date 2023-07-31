package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 * Enum class representing the HostConfig Table fields.
 * s
 * @author A.D.
 * @since  19-Nov-20008
 */
public enum HostConfigEnum implements TableEnum
{
  ACTIVECONFIG("IACTIVECONFIG"),
  DATAHANDLER("SDATAHANDLER"),
  GROUP("SGROUP"),
  PARAMETERNAME("SPARAMETERNAME"),
  PARAMETERVALUE("SPARAMETERVALUE");

  private String msMessageName;

  HostConfigEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
