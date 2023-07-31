package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Container Type Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum ContainerTypeEnum implements TableEnum
{
  CONTAINERTYPE("SCONTAINERTYPE"),
  CONTHEIGHT("FCONTHEIGHT"),
  CONTLENGTH("FCONTLENGTH"),
  CONTWIDTH("FCONTWIDTH"),
  MAXWEIGHT("FMAXWEIGHT"),
  WEIGHT("FWEIGHT");

  private String msMessageName;

  ContainerTypeEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
