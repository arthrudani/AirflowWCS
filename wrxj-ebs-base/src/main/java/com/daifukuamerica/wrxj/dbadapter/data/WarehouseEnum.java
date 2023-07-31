package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Load Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum WarehouseEnum implements TableEnum
{
  DESCRIPTION("SDESCRIPTION"),
  EQUIPWAREHOUSE("SEQUIPWAREHOUSE"),
  ONELOADPERLOC("IONELOADPERLOC"),
  SUPERWAREHOUSE("SSUPERWAREHOUSE"),
  WAREHOUSE("SWAREHOUSE"),
  WAREHOUSESTATUS("IWAREHOUSESTATUS"),
  WAREHOUSETYPE("IWAREHOUSETYPE");

  private String msMessageName;

  WarehouseEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
