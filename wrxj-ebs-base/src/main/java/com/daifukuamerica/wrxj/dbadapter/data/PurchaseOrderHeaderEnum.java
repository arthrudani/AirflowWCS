package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Purchase Order Line Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum PurchaseOrderHeaderEnum implements TableEnum
{
  EXPECTEDDATE("DEXPECTEDDATE"),
  HOSTLINECOUNT("IHOSTLINECOUNT"),
  LASTACTIVITYTIME("DLASTACTIVITYTIME"),
  ORDERID("SORDERID"),
  STORESTATION("SSTORESTATION"),
  PURCHASEORDERSTATUS("IPURCHASEORDERSTATUS"),
  VENDORID("SVENDORID"),
  FINALSORTLOCATIONID("SFINALSORTLOCATIONID");

  private String msMessageName;

  PurchaseOrderHeaderEnum(String isMessageName)
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