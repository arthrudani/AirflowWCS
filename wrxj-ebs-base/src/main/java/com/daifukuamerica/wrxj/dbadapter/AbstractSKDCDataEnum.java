package com.daifukuamerica.wrxj.dbadapter;

public enum AbstractSKDCDataEnum implements TableEnum
{ 
  ID("IID"),
  MODIFYTIME("DMODIFYTIME"),
  ADDMETHOD("SADDMETHOD"),
  UPDATEMETHOD("SUPDATEMETHOD");

  private String msMessageName;

  AbstractSKDCDataEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
  
  

}
