package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the OrderHeader Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum OrderHeaderEnum implements TableEnum
{
  CARRIERID("SCARRIERID"),         
  DESCRIPTION("SDESCRIPTION"),
  DESTADDRESS("SDESTADDRESS"),
  DESTINATIONSTATION("SDESTINATIONSTATION"),
  DESTWAREHOUSE("SDESTWAREHOUSE"),
  HOSTLINECOUNT("IHOSTLINECOUNT"),
  NEXTSTATUS("INEXTSTATUS"),
  ORDEREDTIME("DORDEREDTIME"),
  ORDERID("SORDERID"),
  ORDERMESSAGE("SORDERMESSAGE"),
  ORDERSTATUS("IORDERSTATUS"),
  ORDERTYPE("IORDERTYPE"),
  PRIORITY("IPRIORITY"),
  RELEASETOCODE("SRELEASETOCODE"),
  SCHEDULEDDATE("DSCHEDULEDDATE"),
  SHIPCUSTOMER("SSHIPCUSTOMER"),
  SHORTORDERCHECKTIME("DSHORTORDERCHECKTIME");
  
  private String msMessageName;

  OrderHeaderEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
