package com.daifukuamerica.wrxj.swingui.developer;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CodeGeneratorDbEnum extends CodeGeneratorDB
{
  private static final String TABLENAME   = "ZZZTABLENAMEZZZ";
  private static final String OBJECTNAME  = "ZZZOBJECTNAMEZZZ";
  private static final String COLUMNSENUM = "ZZZCOLUMNSENUMZZZ";
  private static final String DATE        = "ZZZDATEZZZ";
  private static final String YEAR        = "ZZZYEARZZZ";

  /**
   * Constructor
   * @param ipDbTableInfo
   */
  public CodeGeneratorDbEnum(DBTableInfo ipDbTableInfo)
  {
    super(ipDbTableInfo);
  }
  
  /**
   * Generate the enum for the DB columns
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
        mpDbTableInfo.getColumns(tableName.toUpperCase()).keySet());
    String output = OUTPUT_PATH + objectName + "Enum.java";
    generate("TemplateDbEnum.txt", output, parameters);
    return output;
  }
  
  /**
   * @see CodeGeneratorDB#getDescription()
   */
  @Override
  public String getDescription()
  {
    return "DB Column Enum";
  }

  /**
   * Create the parameters for the enum generation
   * 
   * @param tableName - database table name
   * @param objectName - Java object name
   * @param columns - database column names 
   */
  private Map<String, String> createParameters(String tableName, String objectName,
      Set<String> columns)
  {
    Map<String,String> parameters = new TreeMap<>();
    
    // Table Name
    parameters.put(TABLENAME, tableName);

    // Object Name
    parameters.put(OBJECTNAME, objectName);

    // Enum values.  Results should look like this:
    //
    //    COLUMN1("XCOLUMN1"),
    //    COLUMN2("XCOLUMN2"),
    //    COLUMNN("XCOLUMNN");
    //
    StringBuilder sb = new StringBuilder();
    List<String> columnList = new ArrayList<>(columns);
    Collections.sort(columnList, new DbColumnComparator(getUsesHungarianNotationColumnNames()));
    for (String column : columnList) {
      if (skipColumn(column)) continue;

      String columnName = getEnumNameForColumn(column);
      
      column = column.toUpperCase();
      if (sb.length() > 0) {
        sb.append(",").append(System.lineSeparator());
      }
      sb.append("  ").append(columnName).append("(\"").append(column).append("\")");
    }
    parameters.put(COLUMNSENUM, sb.toString());

    // Date
    parameters.put(DATE, getDate());
    
    // Year
    parameters.put(YEAR, getYear());
    
    return parameters;
  }
  
  /**
   * Get the enum name for a column
   * 
   * @param isColumn
   * @return
   */
  public String getEnumNameForColumn(String isColumn)
  {
    return DBTableInfo.getColumnName(isColumn,
        getUsesHungarianNotationColumnNames(), true);
  }
}
