package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Purchase Order Line Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum PurchaseOrderLineEnum implements TableEnum
{
  CASEQUANTITY("FCASEQUANTITY"),
  EXPECTEDQUANTITY("FEXPECTEDQUANTITY"),
  EXPIRATIONDATE("DEXPIRATIONDATE"),
  HOLDREASON("SHOLDREASON"),
  INSPECTION("IINSPECTION"),
  ITEM("SITEM"),
  LASTLINE("SLASTLINE"),
  LINEID("SLINEID"),
  LOADID("SLOADID"),
  LOT("SLOT"),
  ORDERID("SORDERID"),
  RECEIVEDQUANTITY("FRECEIVEDQUANTITY"),
  ROUTEID("SROUTEID"),
  GLOBALID("SGLOBALID");    

  private String msMessageName;

  PurchaseOrderLineEnum(String isMessageName)
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
