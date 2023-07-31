/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * <B>Description:</B> Class for exporting table data for Oracle
 * <br/>Configurations:
 * <ul>
 * <li>DBExporter.Oracle.ExportPath</li>
 * </ul>
 *
 * @author       mandrus
 * @version      1.0
 */
public class DBExporterOracle extends DBExporter
{
  private static final String EXPORT_PATH = Application.getString("DBExporter.Oracle.ExportPath", "./sql/oracle/data");
  
  private static final String START = System.lineSeparator();
  private static final String COMMIT = "COMMIT;" + System.lineSeparator();

  private SimpleDateFormat mpSDF = new SimpleDateFormat(SKDCConstants.DateFormatString);

  /**
   * Constructor
   * @param isDatabaseConfig
   * @param isPath
   */
  public DBExporterOracle(String isDatabaseConfig, String isPath)
  {
    super(isDatabaseConfig, isPath);
  }
  
  /**
   * Constructor
   * @param isDatabaseConfig
   */
  public DBExporterOracle(String isDatabaseConfig)
  {
    this(isDatabaseConfig, EXPORT_PATH);
  }
  
  /**
   * Constructor (default database)
   */
  public DBExporterOracle()
  {
    this("", EXPORT_PATH);
  }

  /**
   * Get text that should be inserted at the beginning of the file
   * @return
   */
  @Override
  protected String getFileHeader(String isTableName)
  {
    StringBuilder vpSQL = new StringBuilder();
    vpSQL.append("SET SERVEROUTPUT ON SIZE 30000").append(System.lineSeparator());
    vpSQL.append("SET DEFINE OFF").append(System.lineSeparator());
    vpSQL.append("PROMPT ++++++++++++++++++++++++++++++++++++++++++++++").append(System.lineSeparator());
    vpSQL.append("PROMPT +  Populating ").append(String.format("%-30.30s", isTableName)).append(" +").append(System.lineSeparator());
    vpSQL.append("PROMPT ++++++++++++++++++++++++++++++++++++++++++++++").append(System.lineSeparator());
    return vpSQL.toString();
  }
  
  /**
   * Get text that should be inserted at the end of the file
   * @return
   */
  @Override
  protected String getFileFooter(String isTableName)
  {
    StringBuilder vpSQL = new StringBuilder();
    vpSQL.append("PROMPT ++++++++++++++++++++++++++++++++++++++++++++++").append(System.lineSeparator());
    vpSQL.append("PROMPT +  Completed ").append(String.format("%-30.30s", isTableName)).append("  +").append(System.lineSeparator());
    vpSQL.append("PROMPT ++++++++++++++++++++++++++++++++++++++++++++++").append(System.lineSeparator());
    vpSQL.append("quit").append(System.lineSeparator());
    return vpSQL.toString();
  }
  
  /**
   * Get text to start a transaction
   * @return
   */
  @Override
  protected String getStartTransaction()
  {
    return START;
  }
  
  /**
   * Get text to commit a transaction
   * @return
   */
  @Override
  protected String getCommitTransaction()
  {
    return COMMIT;
  }
  
  /**
   * Get text for a value
   * @param isTable
   * @param isColumn
   * @param ipValue
   * @return
   */
  @Override
  protected String toSqlString(String isTable, String isColumn, Object ipValue)
  {
    // NOTE: This is only safe for WRx databases, and only because WRx uses 
    // Hungarian notation. The CORRECT way to do this would be to read the 
    // result set's metadata.
    if (ipValue == null)
    {
      return "null";
    }
    // S = String
    if (isColumn.startsWith("S") || ipValue instanceof String)
    {
      if (EMPTY_STRING_TO_NULL && SKDCUtility.isBlank(ipValue.toString()))
        return "null";
      else
        return "'" + escapeString(ipValue.toString()) + "'";
    }
    // I = Integer
    if (isColumn.startsWith("I") || ipValue instanceof Integer)
      return ipValue.toString();
    // F = Float (or double)
    if (isColumn.startsWith("F") || ipValue instanceof Float || ipValue instanceof Double)
      return ipValue.toString();
    // D = Date. TODO: use TO_DATE - the code below will fail
    if (isColumn.startsWith("D") || ipValue instanceof Date)
      return "TO_TIMESTAMP('" + mpSDF.format(ipValue) + "', '" + SKDCConstants.dbInDateFormat + "')";;

    System.out.println(
        isColumn + " -> " + ipValue.getClass() + " :: " + ipValue.toString());
    return ipValue.toString();
  }
  
  /**
   * Escape a string for SQL.  Should probably just use Apache Commons Lang3.
   * If this method ends up being the same for all implementations, then it 
   * should be refactored into DBExporter.
   * @param isOriginal
   * @return
   */
  protected String escapeString(String isOriginal)
  {
    return isOriginal.replace("'", "''");
  }
  
  /**
   * Get the file name that will be used for the table
   * @param isTableName
   * @return
   */
  @Override
  public String getFileName(String isTableName)
  {
    // sqlplus loads LOGIN.sql by default, so rename that file
    if (isTableName.equalsIgnoreCase("LOGIN"))
      return msPath + "/" + isTableName + "TABLE.sql";
    else
      return msPath + "/" + isTableName + ".sql";
  }
}
