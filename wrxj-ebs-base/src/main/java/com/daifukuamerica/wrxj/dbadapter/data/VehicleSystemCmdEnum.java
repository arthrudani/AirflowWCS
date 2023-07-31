package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 * Enum class for Vehicle system command table.
 * @author A.D.
 * @since  15-Jun-2009
 */
public enum VehicleSystemCmdEnum implements TableEnum
{
  COMMANDSTATUS("ICOMMANDSTATUS"),
  COMMANDVALUE("SCOMMANDVALUE"),
  STATUSCHANGETIME("DSTATUSCHANGETIME"),
  SEQUENCENUMBER("ISEQUENCENUMBER"),
  SYSTEMMESSAGEID("SSYSTEMMESSAGEID");

  private String msMessageName;

  VehicleSystemCmdEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  @Override
  public String getName()
  {
    return(msMessageName);
  }
}
