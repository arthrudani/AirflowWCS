package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Load Line Item Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum LoadLineItemEnum implements TableEnum
{
  AGINGDATE("DAGINGDATE"),
  ALLOCATEDQUANTITY("FALLOCATEDQUANTITY"),
  CURRENTQUANTITY("FCURRENTQUANTITY"),
  EXPECTEDRECEIPT("SEXPECTEDRECEIPT"),
  EXPIRATIONDATE("DEXPIRATIONDATE"),
  HOLDREASON("SHOLDREASON"),
  HOLDTYPE("IHOLDTYPE"),
  ITEM("SITEM"),
  LASTCCIDATE("DLASTCCIDATE"),
  LINEID("SLINEID"),
  LOADID("SLOADID"),
  LOT("SLOT"),
  ORDERID("SORDERID"),
  ORDERLOT("SORDERLOT"),
  POSITIONID("SPOSITIONID"),
  PRIORITYALLOCATION("IPRIORITYALLOCATION"),
  GLOBALID("SGLOBALID"),
  EXPECTEDDATE("DEXPECTEDDATE");
  
  private String msMessageName;

  LoadLineItemEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}

