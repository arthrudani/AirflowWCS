package com.daifukuamerica.wrxj.swingui.developer;

/**
 * Generic class to assist in code generation
 * 
 * @author mandrus
 */
public abstract class CodeGeneratorDB extends CodeGenerator
{
  private static final String[] EXCLUDED_COLUMNS = {"sAddMethod", "dModifyTime", "sUpdateMethod"};
  
  protected static final String OUTPUT_PATH = "C:\\Temp\\";
  
  protected DBTableInfo mpDbTableInfo;
  
  /**
   * Constructor
   * @param ipDbTableInfo
   */
  public CodeGeneratorDB(DBTableInfo ipDbTableInfo)
  {
    mpDbTableInfo = ipDbTableInfo;
  }
  
  /**
   * Generate code
   * @param tableName - database table name
   * @param objectName - Java object name
   * @throws Exception
   * @return
   */
  public abstract String generateCode(String tableName, String objectName) throws Exception;

  /**
   * Describe the file to be generated
   * 
   * @return
   */
  public abstract String getDescription();

  /**
   * Ignore certain database columns
   * 
   * @param column
   * @return
   */
  protected boolean skipColumn(String column) {
    for (String skipMe : EXCLUDED_COLUMNS) {
      if (skipMe.equalsIgnoreCase(column)) return true;
    }
    return false;
  }
  
  // Handle differences in column naming
  private boolean mzUsesHungarianNotationColumnNames = false;
  public boolean getUsesHungarianNotationColumnNames()
  {
    return mzUsesHungarianNotationColumnNames;
  }
  public void setUsesHungarianNotationColumnNames(boolean izUses)
  {
    this.mzUsesHungarianNotationColumnNames = izUses;
  }
}
