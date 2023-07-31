package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Transaction History Table fields.
 *
 *  @author A.D.
 *  @since  13-Sep-2007
 */
public enum TransactionHistoryEnum implements TableEnum
{
  ACTIONDESCRIPTION("SACTIONDESCRIPTION"),
  ADJUSTEDQUANTITY("FADJUSTEDQUANTITY"),
  AGINGDATE("DAGINGDATE"),
  AISLEGROUP("IAISLEGROUP"),
  CARRIERID("SCARRIERID"),
  CURRENTQUANTITY("FCURRENTQUANTITY"),
  CUSTOMER("SCUSTOMER"),
  DEVICEID("SDEVICEID"),
  EXPECTEDQUANTITY("FEXPECTEDQUANTITY"),
  EXPIRATIONDATE("DEXPIRATIONDATE"),
  HOLDTYPE("IHOLDTYPE"),
  ITEM("SITEM"),
  LASTCCIDATE("DLASTCCIDATE"),
  LINEID("SLINEID"),
  LOADID("SLOADID"),
  LOCATION("SLOCATION"),
  LOT("SLOT"),
  MACHINENAME("SMACHINENAME"),
  ORDERID("SORDERID"),
  ORDERLOT("SORDERLOT"),
  ORDERTYPE("IORDERTYPE"),
  PICKQUANTITY("FPICKQUANTITY"),
  REASONCODE("SREASONCODE"),
  RECEIVEDQUANTITY("FRECEIVEDQUANTITY"),
  ROLE("SROLE"),
  ROUTEID("SROUTEID"),
  SHIPDATE("DSHIPDATE"),
  STATION("SSTATION"),
  TOLOAD("STOLOAD"),
  TOLOCATION("STOLOCATION"),
  TOSTATION("STOSTATION"),
  TRANCATEGORY("ITRANCATEGORY"),
  TRANSDATETIME("DTRANSDATETIME"),
  TRANTYPE("ITRANTYPE"),
  USERID("SUSERID");
  
  private String msMessageName;

  TransactionHistoryEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}

