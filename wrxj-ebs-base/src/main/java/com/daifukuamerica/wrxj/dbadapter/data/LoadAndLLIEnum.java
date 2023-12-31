package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Load Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum LoadAndLLIEnum implements TableEnum
{
  ADDRESS("SADDRESS"),
  AMOUNTFULL("IAMOUNTFULL"),
  BCRDATA("SBCRDATA"),
  CONTAINERTYPE("SCONTAINERTYPE"),
  DEVICEID("SDEVICEID"),
  FINALADDRESS("SFINALADDRESS"),
  FINALWAREHOUSE("SFINALWAREHOUSE"),
  GROUPNO("IGROUPNO"),
  HEIGHT("IHEIGHT"),
  LENGTH("ILENGTH"),
  LOADID("SLOADID"),
  LOADMESSAGE("SLOADMESSAGE"),
  LOADMOVESTATUS("ILOADMOVESTATUS"),
  LOADPRESENCECHECK("ILOADPRESENCECHECK"),
  MCKEY("SMCKEY"),
  MOVEDATE("DMOVEDATE"),
  NEXTADDRESS("SNEXTADDRESS"),
  NEXTSHELFPOSITION("SNEXTSHELFPOSITION"),
  NEXTWAREHOUSE("SNEXTWAREHOUSE"),
  PARENTLOAD("SPARENTLOAD"),
  RECOMMENDEDZONE("SRECOMMENDEDZONE"),
  ROUTEID("SROUTEID"),
  SHELFPOSITION("SSHELFPOSITION"),
  WAREHOUSE("SWAREHOUSE"),
  WEIGHT("FWEIGHT"),
  WIDTH("IWIDTH"),
  FINALSORTLOCATION("SFINALSORTLOCATIONID"),//US31512 - Send Move order request to PLC
  CURRENTADDRESS("SCURRENTADDRESS"),
  LOT("SLOT"),
  EXPECTEDDATE("DEXPECTEDDATE"),
  EXPECTEDRECEIPT("SEXPECTEDRECEIPT"),
  EXPIRATIONDATE("DEXPIRATIONDATE"),
  POSITIONID("SPOSITIONID"),
  HOLDREASONE("SHOLDREASON"),
  ORDERID("SORDERID"),
  ORDERLOT("SORDERLOT"),
  LINEID("SLINEID"),
  LASTCCIDATE("DLASTCCIDATE"),
  AGINGDATE("DAGINGDATE"),
  CURRENTQUANTITY("FCURRENTQUANTITY"),
  ALLOCATEDQUANTITY("FALLOCATEDQUANTITY"),
  PRIORITYALLOCATION("IPRIORITYALLOCATION"),
  HOLDTYPE("IHOLDTYPE"),
  GLOBALID("SGLOBALID");

  private String msMessageName;

  LoadAndLLIEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }
  @Override
  public String getName()
  {
    return(msMessageName);
  }
}
