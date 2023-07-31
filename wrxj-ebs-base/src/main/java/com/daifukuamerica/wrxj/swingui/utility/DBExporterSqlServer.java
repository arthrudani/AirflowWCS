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
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBMetaData;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.sql.Date;
import java.util.List;

/**
 * <B>Description:</B> Class for exporting table data for SQL Server
 * <br/>Configurations:
 * <ul>
 * <li>DBExporter.SQLServer.ExportPath</li>
 * <li>DBExporter.SQLServer.UseDatabaseName</li>
 * </ul>
 * 
 * @author       mandrus
 * @version      1.0
 */
public class DBExporterSqlServer extends DBExporter
{
  private static final String EXPORT_PATH = Application.getString("DBExporter.SQLServer.ExportPath", "./sql/sqlserver/data");
  
  private static final String GO = "GO" + System.lineSeparator();
  private static final String START = "BEGIN TRANSACTION;" + System.lineSeparator();
  private static final String COMMIT = "COMMIT TRANSACTION;" + System.lineSeparator();
  private static final String USE_DATABASE;
  static
  {
    String vsDBName = Application.getString("DBExporter.SQLServer.UseDatabaseName");
    if (SKDCUtility.isBlank(vsDBName))
    {
      DBMetaData vpDBMetaData = Factory.create(DBMetaData.class);
      vsDBName = vpDBMetaData.getDatabaseInstanceName();
    }
    USE_DATABASE = "use  [" + vsDBName + "]" + System.lineSeparator() + GO;
  }

  private boolean mzIdentityInsert = false;
  
  /**
   * Constructor
   * @param isDatabaseConfig
   * @param isPath
   */
  public DBExporterSqlServer(String isDatabaseConfig, String isPath)
  {
    super(isDatabaseConfig, isPath);
  }
  
  /**
   * Constructor
   * @param isDatabaseConfig
   */
  public DBExporterSqlServer(String isDatabaseConfig)
  {
    this(isDatabaseConfig, EXPORT_PATH);
  }
  
  /**
   * Constructor (default database)
   */
  public DBExporterSqlServer()
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
    vpSQL.append(USE_DATABASE);
    vpSQL.append("PRINT '++++++++++++++++++++++++++++++++++++++++++++++'").append(System.lineSeparator());
    vpSQL.append("PRINT '+  Populating ").append(String.format("%-30.30s", isTableName)).append(" +'").append(System.lineSeparator());
    vpSQL.append("PRINT '++++++++++++++++++++++++++++++++++++++++++++++'").append(System.lineSeparator());
    vpSQL.append(GO);
    if (mzIdentityInsert)
    {
      vpSQL.append("SET IDENTITY_INSERT ").append(isTableName).append(" ON").append(System.lineSeparator());
      vpSQL.append(GO);
    }
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
    if (mzIdentityInsert)
    {
      vpSQL.append("SET IDENTITY_INSERT ").append(isTableName).append(" OFF").append(System.lineSeparator());
      vpSQL.append(GO);
      vpSQL.append("PRINT '++++++++++++++++++++++++++++++++++++++++++++++'").append(System.lineSeparator());
      vpSQL.append("PRINT '+  Completed ").append(String.format("%-30.30s", isTableName)).append("  +'").append(System.lineSeparator());
      vpSQL.append("PRINT '++++++++++++++++++++++++++++++++++++++++++++++'").append(System.lineSeparator());
      vpSQL.append(GO);
    }
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
   * Get the "insert into xxx (yyy, zzz)" portion of the insert statement.
   * 
   * <BR>Note: Removes debug fields from column set.
   * 
   * @param isTableName
   * @param iapColumns
   * @return
   */
  @Override
  protected String getInsertColumnNames(String isTableName, List<String> ipColumns)
  {   
    // If the column list contains IID, then we need to insert identity values
    mzIdentityInsert = ipColumns.contains("IID");
        
    // the super method does everything else
    return super.getInsertColumnNames(isTableName, ipColumns);
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
    // D = Date. SQL Server is pretty good with date conversions
    if (isColumn.startsWith("D") || ipValue instanceof Date)
      return "'" + ipValue.toString() + "'";
    
    System.out.println(isTable + "." + isColumn + " -> " + ipValue.getClass()
        + " :: " + ipValue.toString());
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
}
