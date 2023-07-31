package com.daifukuamerica.wrxj.swingui.developer;

import com.daifukuamerica.wrxj.jdbc.DBInfo;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$
  
  Copyright (c) 2015 Wynright Corporation.  All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CodeGeneratorDbData extends CodeGeneratorDB
{
  private static final String TABLENAME    = "ZZZTABLENAMEZZZ";
  private static final String OBJECTNAME   = "ZZZOBJECTNAMEZZZ";
  private static final String COLNAMES     = "ZZZCOLUMNNAMESZZZ";
  private static final String COLVARS      = "ZZZCOLUMNVARSZZZ";
  private static final String COLGETTERS   = "ZZZGETTERSZZZ";
  private static final String COLSETTERS   = "ZZZSETTERSZZZ";
  private static final String COLSETSWITCH = "ZZZSETTERSWITCHZZZ";
  private static final String DATE         = "ZZZDATEZZZ";
  private static final String YEAR         = "ZZZYEARZZZ";
  
  private static final int GETTER_PAD_LENGTH = 25;
  private static final int NAME_PAD_LENGTH   = 25;
  private static final int TYPE_PAD_LENGTH   =  6;

  /**
   * Constructor
   * @param ipDbTableInfo
   */
  public CodeGeneratorDbData(DBTableInfo ipDbTableInfo)
  {
    super(ipDbTableInfo);
  }

  /**
   * Generate the DB Data object for the table
   * 
   * @param tableName - database table name
   * @param objectName - Java object name
   * @throws IOException
   */
  @Override
  public String generateCode(String tableName, String objectName)
      throws IOException
  {
    Map<String, String> parameters = createParameters(tableName, objectName,
        mpDbTableInfo.getColumns(tableName.toUpperCase()));
    String output = OUTPUT_PATH + objectName + "Data.java";
    generate("TemplateDbData.txt", output, parameters);
    return output;
  }
  
  /**
   * @see CodeGeneratorDB#getDescription()
   */
  @Override
  public String getDescription()
  {
    return "DB Data";
  }
  
  /**
   * Create the parameters for the enum generation
   * 
   * @param tableName - database table name
   * @param objectName - Java object name
   * @param columnInfo - database column information
   */
  private Map<String, String> createParameters(String tableName,
      String objectName, Map<String, Integer> columnInfo)
  {
    
    Map<String,String> parameters = new TreeMap<>();
    
    // Table Name
    parameters.put(TABLENAME, tableName);
    
    // Object Name
    parameters.put(OBJECTNAME, objectName);
    
    // Column-related code
    List<String> columnList = new ArrayList<>(columnInfo.keySet());
    Collections.sort(columnList, new DbColumnComparator(getUsesHungarianNotationColumnNames()));
    parameters.put(COLNAMES,     getColumnNames(columnList));
    parameters.put(COLVARS,      getColumnVariables(columnList, columnInfo));
    parameters.put(COLGETTERS,   getColumnGetters(columnList, columnInfo));
    parameters.put(COLSETTERS,   getColumnSetters(columnList, columnInfo));
    parameters.put(COLSETSWITCH, getColumnSetterSwitch(columnList, columnInfo));

    // Date
    parameters.put(DATE, getDate());
    
    // Year
    parameters.put(YEAR, getYear());

    return parameters;
  }

  /**
   * Get Column Name Constants
   * 
   * @param columnInfo
   * @return
   */
  private String getColumnNames(List<String> columnList) {
    //
    //  public static final String COLUMN1_NAME               = COLUMN1.getName();
    //  public static final String COLUMN2_NAME               = COLUMN2.getName();
    //  public static final String COLUMNN_NAME               = COLUMNN.getName();
    //
    StringBuilder sb = new StringBuilder();
    for (String column : columnList) {
      if (skipColumn(column)) continue;

      String columnName = spacePad(getColumnNameName(column), NAME_PAD_LENGTH);
      
      column = DBTableInfo.getColumnName(column, getUsesHungarianNotationColumnNames(), true);
      
      sb.append("  public static final String ").append(columnName).append("= ").append(column).append(".getName();").append(System.lineSeparator());
    }
    return sb.toString();
  }

  /**
   * Get Column Name Constants
   * 
   * @param columnInfo
   * @return
   */
  private String getColumnNameName(String column)
  {
    column = DBTableInfo.getColumnName(column, getUsesHungarianNotationColumnNames(), true);
    return DBTableInfo.suggestObjName(column).toUpperCase() + "_NAME";
  }

  /**
   * Get the Column class member fields
   * 
   * @param columnList
   * @param columnInfo
   * @return
   */
  private String getColumnVariables(List<String> columnList,
      Map<String, Integer> columnInfo)
  {
    //
    //  private xxx xColumn1;
    //  private xxx xColumn2;
    //  private xxx xColumnn;
    //
    StringBuilder sb = new StringBuilder();
    for (String column : columnList) {
      if (skipColumn(column)) continue;

      
      String javaType = getJavaPrimitiveType(columnInfo.get(column), TYPE_PAD_LENGTH);
      sb.append("  private ").append(javaType).append(" ")
        .append(getMemberVariableName(column, columnInfo)).append(";")
        .append(System.lineSeparator());
    }
    return sb.toString();
  }

  /**
   * Get the Java object member name for a database column
   * 
   * @param column
   * @param columnInfo
   * @return
   */
  private String getMemberVariableName(String column, Map<String, Integer> columnInfo) {
    
    String columnFixedCase = column;
    if (column.contains("_") || column.equals(column.toUpperCase())) {
      columnFixedCase = DBTableInfo.suggestObjName(columnFixedCase);
    }
    if (column.equalsIgnoreCase(DBTableInfo.ID_NAME))
    {
      columnFixedCase = "Id";
    }
    if (!getUsesHungarianNotationColumnNames()
        || column.equalsIgnoreCase(DBTableInfo.ID_NAME))
    {
      switch (columnInfo.get(column)) {
        case Types.DATE:
          columnFixedCase = "d" + columnFixedCase;
          break;
        case Types.FLOAT:
          columnFixedCase = "f" + columnFixedCase;
          break;
        case Types.INTEGER:
          columnFixedCase = "n" + columnFixedCase;
          break;
        case Types.VARCHAR:
          columnFixedCase = "s" + columnFixedCase;
          break;
        case Types.BOOLEAN:
          columnFixedCase = "z" + columnFixedCase;
          break;
        default:
          columnFixedCase = "x" + columnFixedCase;
      }
    }
    return "m" + columnFixedCase;
  }
  
  /**
   * Get the getters
   * 
   * @param columnList
   * @param columnInfo
   * @return
   */
  private String getColumnGetters(List<String> columnList,
      Map<String, Integer> columnInfo)
  {
    //
    // public xxx getColumn1() { return xColumn1; }
    // public xxx getColumn2() { return xColumn2; }
    // public xxx getColumn3() { return xColumnn; }
    //
    StringBuilder sb = new StringBuilder();
    for (String column : columnList)
    {
      if (skipColumn(column)) continue;

      String javaType = getJavaPrimitiveType(columnInfo.get(column), TYPE_PAD_LENGTH);
      String getMethod = getGetterMethodName(column);
      String rtnVal = spacePad(getMemberVariableName(column, columnInfo) + ";", GETTER_PAD_LENGTH);
      sb.append("  public ").append(javaType).append(" ").append(
          getMethod).append("{  return ").append(rtnVal).append("}").append(
              System.lineSeparator());
    }
    return sb.toString();
  }

  /**
   * Get the getter name
   * 
   * @param column
   * @return
   */
  private String getGetterMethodName(String column)
  {
    return spacePad("get" + getGSetterName(column) + "()", GETTER_PAD_LENGTH);
  }
  
  /**
   * Get the setters
   * 
   * @param columnList
   * @param columnInfo
   * @return
   */
  private String getColumnSetters(List<String> columnList,
      Map<String, Integer> columnInfo)
  {
    //
    //  public void setColumn1(int ixColumn1)
    //  {
    //    xColumn1 = ixColumn1;
    //    addColumnObject(new ColumnObject(COLUMN_NAME, xColumn1));
    //  }
    //
    StringBuilder sb = new StringBuilder();
    for (String column : columnList) {
      if (skipColumn(column)) continue;

      String columnNoHN = getGSetterName(column);

      String javaType = getJavaPrimitiveType(columnInfo.get(column), 0);
      String setMethod = "set" + columnNoHN;
      String colName = getColumnNameName(column);
      String varName = getMemberVariableName(column, columnInfo);
      String inputVarName = getMemberVariableName(column, columnInfo).substring(1);
      sb.append("  public void ").append(setMethod).append("(").append(javaType).append(" i").append(inputVarName).append(")").append(System.lineSeparator());
      sb.append("  {").append(System.lineSeparator());
      sb.append("    ").append(varName).append(" = i").append(inputVarName).append(";").append(System.lineSeparator());
      sb.append("    addColumnObject(new ColumnObject(").append(colName).append(", ").append(varName).append("));").append(System.lineSeparator());
      sb.append("  }").append(System.lineSeparator());
    }
    return sb.toString();
  }

  /**
   * Get the setter switch cases
   * 
   * @param columnList
   * @param columnInfo
   * @return
   */
  private String getColumnSetterSwitch(List<String> columnList,
      Map<String, Integer> columnInfo)
  {
    //
    //    case COLUMN1:
    //      setColumn1((xxx)ipColValue);
    //      break;
    //
    CodeGeneratorDbEnum dbEnum = new CodeGeneratorDbEnum(mpDbTableInfo);
    dbEnum.setUsesHungarianNotationColumnNames(getUsesHungarianNotationColumnNames());
    
    StringBuilder sb = new StringBuilder();
    for (String column : columnList) {
      if (skipColumn(column)) continue;

      String setMethodName = "set" + getGSetterName(column);
      
      String enumName = dbEnum.getEnumNameForColumn(column);
      
      String javaType = getJavaObjectType(columnInfo.get(column), 0);
      
      sb.append("      case ").append(enumName).append(":").append(System.lineSeparator());
      sb.append("        ").append(setMethodName).append("((").append(javaType).append(")ipColValue);").append(System.lineSeparator());
      sb.append("        break;").append(System.lineSeparator());
    }
    return sb.toString();
  }

  /**
   * Get the variable part of getter/setter names
   * 
   * @param column
   * @return
   */
  private String getGSetterName(String column)
  {
    column = DBTableInfo.getColumnName(column,
        getUsesHungarianNotationColumnNames(), false);
    if (column.contains("_") || !column.equals(column.toUpperCase()))
    {
      column = DBTableInfo.suggestObjName(column);
    }
    return column;
  }
  
  /**
   * Convert DB type to Java type
   * 
   * @param iiDbType
   * @param iiPadToLength
   */
  private String getJavaPrimitiveType(int iiDbType, int iiPadToLength) {
    String javaType;
    switch (iiDbType) {
      case DBInfo.MSSQL_DATETIMEOFFSET:
      case Types.DATE:
        javaType = "Date";
        break;
      case Types.FLOAT:
        javaType = "double";
        break;
      case Types.INTEGER:
        javaType = "int";
        break;
      case Types.VARCHAR:
        javaType = "String";
        break;
      default:
        javaType = "Unsupported:" + iiDbType;
    }
    javaType = spacePad(javaType, iiPadToLength);
    return javaType;
  }
  
  /**
   * Convert DB type to Java type
   * 
   * @param iiDbType
   * @param iiPadToLength
   */
  private String getJavaObjectType(int iiDbType, int iiPadToLength) {
    String javaType;
    switch (iiDbType) {
      case DBInfo.MSSQL_DATETIMEOFFSET:
      case Types.DATE:
        javaType = "Date";
        break;
      case Types.FLOAT:
        javaType = "Double";
        break;
      case Types.INTEGER:
        javaType = "Integer";
        break;
      case Types.VARCHAR:
        javaType = "String";
        break;
      default:
        javaType = "Unsupported:" + iiDbType;
    }
    javaType = spacePad(javaType, iiPadToLength);
    return javaType;
  }
}
