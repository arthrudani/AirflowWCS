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
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBLargeResultSet;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <B>Description:</B> Abstract class for exporting table data
 * 
 * <br/>Configurations:
 * <ul>
 * <li>DBExporter.EmptyStringToNull</li>
 * <li>DBExporter.IncludeSchema</li>
 * <li>DBExporter.RowsPerCommit</li>
 * <li>DBExporter.RowsPerFile</li>
 * <li>DBExporter.RenameColumns</li>
 * </ul>
 *
 * @author       mandrus
 * @version      1.0
 */
public abstract class DBExporter
{
  protected static final int ROWS_PER_COMMIT = Application.getInt("DBExporter.RowsPerCommit", 1000);
  protected static final int ROWS_PER_FILE = Application.getInt("DBExporter.RowsPerFile", 100000);
  protected static final boolean EMPTY_STRING_TO_NULL = Application.getBoolean("DBExporter.EmptyStringToNull", true);
  protected static final boolean INCLUDE_SCHEMA = Application.getBoolean("DBExporter.IncludeSchema", true);
  protected static final Map<String, Map<String,String>> COLUMN_RENAMING;
  static  
  {
    // DBExporter.RenameColumns is comma separated groups of
    // TableName.OldColumnName:NewColumnName
    COLUMN_RENAMING = new HashMap<String, Map<String,String>>();
    try
    {
      String vsRenaming = Application.getString("DBExporter.RenameColumns");
      if (vsRenaming != null)
      {
        String[] vasColumns = vsRenaming.split(",");
        for (String vsRename : vasColumns)
        {
          String[] vasPieces = vsRename.split("\\.|:");
          String vsTable = vasPieces[0];
          String vsOld = vasPieces[1];
          String vsNew = vasPieces[2];
          Map<String,String> m = COLUMN_RENAMING.get(vsTable);
          if (m == null)
          {
            m = new HashMap<>();
            COLUMN_RENAMING.put(vsTable, m);
          }
          m.put(vsOld, vsNew);
        }
      }
    }
    catch (Exception e)
    {
      Logger.getLogger().logException(
          "Error processing DBExporter.RenameColumns", e);
    }
  }
  
  protected Logger mpLogger = Logger.getLogger();
  protected String msDatabaseConfig;
  protected String msPath;
  protected String msSchema = Application.getString("database.schema");
  
  /**
   * Constructor
   * @param isDatabaseConfig
   * @param isPath
   */
  protected DBExporter(String isDatabaseConfig, String isPath)
  {
    msDatabaseConfig = isDatabaseConfig;
    if (SKDCUtility.isBlank(msDatabaseConfig))
    {
      msDatabaseConfig = Application.getString("database");
    }
    msPath = isPath;
  }

  /**
   * Report on column names that will be changed
   * @return
   */
  public String getRenamingReport()
  {
    StringBuilder sb = new StringBuilder();
    for (String vsTable : COLUMN_RENAMING.keySet())
    {
      sb.append(vsTable).append(":").append(System.lineSeparator());
      Map<String, String> m = COLUMN_RENAMING.get(vsTable);
      for (String vsOld : m.keySet())
      {
        sb.append(" * From ").append(vsOld).append(" to ").append(m.get(vsOld)).append(System.lineSeparator());
      }
    }
    
    return sb.toString();
  }
  
  /**
   * Get text that should be inserted at the beginning of the file
   * @return
   */
  protected abstract String getFileHeader(String isTableName);
  
  /**
   * Get text that should be inserted at the end of the file
   * @return
   */
  protected abstract String getFileFooter(String isTableName);

  /**
   * Get text to start a transaction
   * @return
   */
  protected abstract String getStartTransaction();
  
  /**
   * Get text to commit a transaction
   * @return
   */
  protected abstract String getCommitTransaction();
  
  /**
   * Get the "insert into xxx (yyy, zzz)" portion of the insert statement.
   * 
   * <BR>Note: Removes debug fields from column set.
   * 
   * @param isTableName
   * @param iapColumns
   * @return
   */
  protected String getInsertColumnNames(String isTableName, List<String> ipColumns)
  {
    // We don't need to export debug data
    ipColumns.remove("DMODIFYTIME");
    ipColumns.remove("SADDMETHOD");
    ipColumns.remove("SUPDATEMETHOD");

    // Column renaming
    Map<String,String> vpRenaming = COLUMN_RENAMING.get(isTableName);
    
    // Build the insert Header
    StringBuilder vpSql = new StringBuilder("INSERT INTO ");
    if (INCLUDE_SCHEMA && SKDCUtility.isNotBlank(msSchema))
    {
      vpSql.append(msSchema).append(".");
    }
    vpSql.append(isTableName).append(" (");
    for (int i = 0; i < ipColumns.size() - 1; i++)
    {
      String vsColumn = ipColumns.get(i);
      if (vpRenaming != null)
      {
        String vsRenamed = vpRenaming.get(vsColumn);
        if (vsRenamed != null)
        {
          vsColumn = vsRenamed;
        }
      }
      vpSql.append(vsColumn).append(", ");
    }
    vpSql.append(ipColumns.get(ipColumns.size() - 1))
      .append(")").append(System.lineSeparator());
    return vpSql.toString();
  }
  
  /**
   * Get text for a value
   * @param isTable
   * @param isColumn
   * @param ipValue
   * @return
   */
  protected abstract String toSqlString(String isTable, String isColumn,
      Object ipValue);
  
  /**
   * Get the output path
   * @return
   */
  public String getOutputPath()
  {
    return new File(msPath).getAbsolutePath();
  }
  
  /**
   * Get the file name that will be used for the table
   * @param isTableName
   * @return
   */
  public String getFileName(String isTableName)
  {
    return msPath + "/" + isTableName + ".sql";
  }
  
  /**
   * Export
   * @param isTableName
   * @return number of rows exported
   * @throws DBException, IOException
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public int export(String isTableName) throws DBException, IOException
  {
    List<String> vpColumnNames = new ArrayList<>();
    List<Map> vpRows = null;
    int vnRowCount = 0;
    
    String vsFileName = getFileName(isTableName);
    String vsSelectSQL = "select * from " + isTableName + " order by 1, 2";

    // Run the query to get all of the data
    DBObject vpDBO = new DBObjectTL().getDBObject(msDatabaseConfig);
    try
    {
      // Connect
      vpDBO.connect();
      
      // Just get one row to get column info
      vpDBO.setMaxRows(1);
      DBResultSet rs = vpDBO.execute(vsSelectSQL);
      vpRows = rs.getRows();
      // Column names may not all be capitalized, but they are as map keys
      for (String vsKey : rs.getColumns())
      {
        vpColumnNames.add(vsKey.toUpperCase());
      }
    }
    finally
    {
      vpDBO.setMaxRows();
    }

    // Return if there is nothing to export
    if (vpRows.size() == 0)
    {
      return 0;
    }

    // Build the insert Header
    String vsInsertColumns = getInsertColumnNames(isTableName, vpColumnNames);

    try
    {
      deleteFile(isTableName + "*");
      createExportDirectory();
      writeTextFile(vsFileName, getFileHeader(isTableName));

      // Page through the table (avoid max rows limits)
      DBLargeResultSet vpLRS = vpDBO.executeLargeSelect(vsSelectSQL);
      vpLRS.setResultsPerPage(ROWS_PER_COMMIT);
      vpRows = vpLRS.fetchNextLargeRecordListEntries();
      while (vpRows.size() > 0)
      {
        writeTextFile(vsFileName, getStartTransaction());
        String vsSMessageClob = null;
        // Write the data
        for (Map vpMap : vpRows)
        {
          if ((isTableName.equals("HOSTTOWRX")) || (isTableName.equals("WRXTOHOST")) )
          {
            vsSMessageClob = retrieveClob(vpMap, isTableName);
          }
        	
          StringBuilder vpInsertData = new StringBuilder(" VALUES (");
          for (String vsKey : vpColumnNames)
          {
            if (vsKey.equals("SMESSAGE"))
            {
              vpInsertData.append(
                  toSqlString(isTableName, vsKey, vsSMessageClob)).append(", ");
            }
            else
            {
        	  vpInsertData.append(
        	      toSqlString(isTableName, vsKey, vpMap.get(vsKey))).append(", ");
        	}
          }
          // Trim last ", "
          vpInsertData.setLength(vpInsertData.length() - 2);
          vpInsertData.append(");");
          vpInsertData.append(System.lineSeparator());
  
          writeTextFile(vsFileName, vsInsertColumns);
          writeTextFile(vsFileName, vpInsertData.toString());
          vnRowCount++;
        }
        
        // Commit occasionally
        writeTextFile(vsFileName, getCommitTransaction());
        
        // Make new files occasionally
        if (vnRowCount % ROWS_PER_FILE == 0)
        {
          vsFileName = getFileName(isTableName + "-" + (vnRowCount / ROWS_PER_FILE));
          writeTextFile(vsFileName, getFileHeader(isTableName));
        }
        
        // Get the next batch
        vpRows = vpLRS.fetchNextLargeRecordListEntries();
      }
      writeTextFile(vsFileName, getFileFooter(isTableName));
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error processing " + isTableName, e);
    }
    
    return vnRowCount;
  }

  protected String retrieveClob(Map<Object, Object> ipRowMap, String isTableName) throws CharacterCodingException, DBException
  {
    String vsSQLCommand = "Select SMESSAGE from " + isTableName + " where SHOSTNAME = '" 
    + ipRowMap.get("SHOSTNAME") + "' AND IMESSAGESEQUENCE = " + ipRowMap.get("IMESSAGESEQUENCE");
    
    byte[] vabClob  = DBHelper.readClob("SMESSAGE", vsSQLCommand);
    
    //wrap byte[] into buffer, decode it and and construct the new charbuffer into a Stringbuilder 
    Charset charset = Charset.forName("ISO-8859-1");
    CharsetDecoder decoder = charset.newDecoder();
    ByteBuffer srcBuffer = ByteBuffer.wrap(vabClob);
    CharBuffer resBuffer = decoder.decode(srcBuffer);
    StringBuilder vsbClob = new StringBuilder(resBuffer);
    return vsbClob.toString();
  }
  
  
  /**
   * Delete an existing export file
   * @param isTableName
   */
  private void deleteFile(String isFileName) throws IOException
  {
    File folder = new File(msPath);
    if (folder != null)
    {
      File[] files = folder.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name)
        {
          return name.matches(isFileName + "(-[0-9]+)?\\.sql");
        }
      });
      if (files != null)
      {
        for (File file : files)
        {
          if (!file.delete())
          {
            throw new IOException(
                "Unable to delete [" + file.getAbsolutePath() + "]");
          }
        }
      }
    }
  }

  /**
   * Create the export directory
   * @throws IOException
   */
  private void createExportDirectory() throws IOException
  {
    if (!new File(msPath).exists())
    {
      if (!new File(msPath).mkdirs())
      {
        throw new IOException(
            "UNABLE to create export directory \"" + msPath + "\"");
      }
    }
  }

  /**
   * Write to a text files
   * @param isFileName
   * @param s
   * @throws IOException
   */
  private void writeTextFile(String isFileName, String s) throws IOException
  {
    try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(isFileName, true)))
    {
      writer.write(s);
      writer.flush();
    }
  }
}
