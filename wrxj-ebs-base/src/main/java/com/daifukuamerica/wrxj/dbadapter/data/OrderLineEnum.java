package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Order Line Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum OrderLineEnum implements TableEnum
{
  ALLOCATEDQUANTITY("FALLOCATEDQUANTITY"),
  BEGINADDRESS("SBEGINADDRESS"),
  BEGINLOCATION("SBEGINLOCATION"),
  BEGINWAREHOUSE("SBEGINWAREHOUSE"),
  CONTAINERTYPE("SCONTAINERTYPE"),
  DESCRIPTION("SDESCRIPTION"),
  ENDINGADDRESS("SENDINGADDRESS"),
  ENDINGLOCATION("SENDINGLOCATION"),
  ENDINGWAREHOUSE("SENDINGWAREHOUSE"),
  HEIGHT("IHEIGHT"),
  ITEM("SITEM"),
  LASTLINE("SLASTLINE"),
  LINEID("SLINEID"),
  LINESHY("ILINESHY"),
  LOADID("SLOADID"),
  ORDERID("SORDERID"),
  ORDERLOT("SORDERLOT"),
  ORDERQUANTITY("FORDERQUANTITY"),
  PICKQUANTITY("FPICKQUANTITY"),
  ROUTEID("SROUTEID"),
  SHIPQUANTITY("FSHIPQUANTITY"),
  WAREHOUSE("SWAREHOUSE");

  private String msMessageName;

  OrderLineEnum(String isMessageName)
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