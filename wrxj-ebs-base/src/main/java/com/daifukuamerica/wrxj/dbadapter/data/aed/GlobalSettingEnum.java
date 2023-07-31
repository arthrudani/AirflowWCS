package com.daifukuamerica.wrxj.dbadapter.data.aed;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 * Enum class representing the Wynsoft Global Setting table fields.
 * 
 * @author mandrus
 */
public enum GlobalSettingEnum implements TableEnum
{
  AREA("AREA"),
  DESCRIPTION("DESCRIPTION"),
  ID("ID"),
  INSTANCEID("INSTANCEID"),
  ISAUTOREFRESHREQUIRED("ISAUTOREFRESHREQUIRED"),
  ISEDITABLE("ISEDITABLE"),
  ISHIDDEN("ISHIDDEN"),
  MAXVALUE("MAXVALUE"),
  MINVALUE("MINVALUE"),
  NAME("NAME"),
  PRODUCTID("PRODUCTID"),
  RECOMMENDEDVALUE("RECOMENDEDVALUE"),
  REGEX("REGEX"),
  TYPEID("TYPEID"),
  VALUE("VALUE");

  private String msColumnName;

  GlobalSettingEnum(String isColumnName)
  {
    msColumnName = isColumnName;
  }

  @Override
  public String getName()
  {
    return(msColumnName);
  }
}
