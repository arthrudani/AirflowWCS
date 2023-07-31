package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Item Master Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum ItemMasterEnum implements TableEnum
{
  CASEHEIGHT("FCASEHEIGHT"),
  CASELENGTH("FCASELENGTH"),
  CASEWEIGHT("FCASEWEIGHT"),
  CASEWIDTH("FCASEWIDTH"),
  CCIPOINTQUANTITY("FCCIPOINTQUANTITY"),
  DEFAULTLOADQUANTITY("FDEFAULTLOADQUANTITY"),
  DELETEATZEROQUANTITY("IDELETEATZEROQUANTITY"),
  DESCRIPTION("SDESCRIPTION"),
  EXPIRATIONREQUIRED("IEXPIRATIONREQUIRED"),
  HOLDTYPE("IHOLDTYPE"),
  ITEM("SITEM"),
  ITEMHEIGHT("FITEMHEIGHT"),
  ITEMLENGTH("FITEMLENGTH"),
  ITEMWEIGHT("FITEMWEIGHT"),
  ITEMWIDTH("FITEMWIDTH"),
  LASTCCIDATE("DLASTCCIDATE"),
  ORDERROUTE("SORDERROUTE"),
  PIECESPERUNIT("IPIECESPERUNIT"),
  RECOMMENDEDWAREHOUSE("SRECOMMENDEDWAREHOUSE"),
  RECOMMENDEDZONE("SRECOMMENDEDZONE"),
  STORAGEFLAG("ISTORAGEFLAG");

  private String msMessageName;

  ItemMasterEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
