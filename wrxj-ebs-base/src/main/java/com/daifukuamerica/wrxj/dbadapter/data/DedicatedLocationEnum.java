package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Customer Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum DedicatedLocationEnum implements TableEnum
{
  ADDRESS("SADDRESS"),
  CURRENTQUANTITY("FCURRENTQUANTITY"),
  DEDICATEDTYPE("IDEDICATEDTYPE"),
  ENROUTEQUANTITY("FENROUTEQUANTITY"),
  ITEM("SITEM"),
  MAXIMUMQUANTITY("FMAXIMUMQUANTITY"),
  MINIMUMQUANTITY("FMINIMUMQUANTITY"),
  REPLENISHNOW("IREPLENISHNOW"),
  REPLENISHTYPE("IREPLENISHTYPE"),
  WAREHOUSE("SWAREHOUSE");

  private String msMessageName;

  DedicatedLocationEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
