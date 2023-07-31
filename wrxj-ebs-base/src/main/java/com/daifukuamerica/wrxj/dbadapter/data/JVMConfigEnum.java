package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the JVMConfig Table fields.
 *
 *  @author A.D.
 *  @since  10-Feb-2009
 */
public enum JVMConfigEnum implements TableEnum
{
  JVMIDENTIFIER("SJVMIDENTIFIER"),
  JVMTYPE("IJVMTYPE"),
  JVMSTATUS("IJVMSTATUS"),
  SERVERNAME("SSERVERNAME"),
  JMSTOPIC("SJMSTOPIC");

  private String msMessageName;

  JVMConfigEnum(String isMessageName)
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
