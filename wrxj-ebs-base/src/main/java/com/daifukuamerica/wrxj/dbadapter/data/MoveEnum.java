package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Move Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum MoveEnum implements TableEnum
{
  ADDRESS("SADDRESS"),
  AISLEGROUP("IAISLEGROUP"),
  DESTADDRESS("SDESTADDRESS"),
  DESTWAREHOUSE("SDESTWAREHOUSE"),
  DEVICEID("SDEVICEID"),
  DISPLAYMESSAGE("SDISPLAYMESSAGE"),
  ITEM("SITEM"),
  LINEID("SLINEID"),
  LOADID("SLOADID"),
  MOVECATEGORY("IMOVECATEGORY"),
  MOVEDATE("DMOVEDATE"),
  MOVEID("IMOVEID"),
  MOVESEQUENCE("IMOVESEQUENCE"),
  MOVESTATUS("IMOVESTATUS"),
  MOVETYPE("IMOVETYPE"),
  NEXTADDRESS("SNEXTADDRESS"),
  NEXTWAREHOUSE("SNEXTWAREHOUSE"),
  ORDERID("SORDERID"),
  ORDERLOT("SORDERLOT"),
  PARENTLOAD("SPARENTLOAD"),
  PICKLOT("SPICKLOT"),
  PICKQUANTITY("FPICKQUANTITY"),
  PICKTOLOADID("SPICKTOLOADID"),
  POSITIONID("SPOSITIONID"),
  PRIORITY("IPRIORITY"),
  RELEASETOCODE("SRELEASETOCODE"),
  ROUTEID("SROUTEID"),
  SCHEDULERNAME("SSCHEDULERNAME"),
  WAREHOUSE("SWAREHOUSE");

  private String msMessageName;

  MoveEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
