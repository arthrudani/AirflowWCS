package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Container Type Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum AsrsMetaDataEnum implements TableEnum
{
  COLUMNNAME("SCOLUMNNAME"),
  DATAVIEWNAME("SDATAVIEWNAME"),
  DISPLAYORDER("IDISPLAYORDER"),
  FULLNAME("SFULLNAME"),
  ISTRANSLATION("SISTRANSLATION");

  private String msMessageName;

  AsrsMetaDataEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
