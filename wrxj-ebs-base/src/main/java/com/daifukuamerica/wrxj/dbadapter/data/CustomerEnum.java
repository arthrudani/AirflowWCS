package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Customer Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum CustomerEnum implements TableEnum
{
  ATTENTION("SATTENTION"),
  CITY("SCITY"),
  CONTACT("SCONTACT"),
  COUNTRY("SCOUNTRY"),
  CUSTOMER("SCUSTOMER"),
  DELETEONUSE("IDELETEONUSE"),
  DESCRIPTION1("SDESCRIPTION1"),
  DESCRIPTION2("SDESCRIPTION2"),
  NOTE("SNOTE"),
  PHONE("SPHONE"),
  STATE("SSTATE"),
  STREETADDRESS1("SSTREETADDRESS1"),
  STREETADDRESS2("SSTREETADDRESS2"),
  ZIPCODE("SZIPCODE");

  private String msMessageName;

  CustomerEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
