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
import java.util.Map;
import java.util.TreeMap;

public class CodeGeneratorDbInt extends CodeGeneratorDB
{
  private static final String TABLENAME  = "ZZZTABLENAMEZZZ";
  private static final String OBJECTNAME = "ZZZOBJECTNAMEZZZ";
  private static final String DATE       = "ZZZDATEZZZ";
  private static final String YEAR       = "ZZZYEARZZZ";
  
  /**
   * Constructor
   * @param ipDbTableInfo
   */
  public CodeGeneratorDbInt(DBTableInfo ipDbTableInfo)
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
    Map<String, String> parameters = createParameters(tableName, objectName);
    String output = OUTPUT_PATH  + objectName + ".java";
    generate("TemplateDbInt.txt", output, parameters);
    return output;
  }
  
  /**
   * @see CodeGeneratorDB#getDescription()
   */
  @Override
  public String getDescription()
  {
    return "DB Interface";
  }

  /**
   * Create the parameters for the enum generation
   * 
   * @param tableName - database table name
   * @param objectName - Java object name
   */
  private Map<String, String> createParameters(String tableName,
      String objectName)
  {
    
    Map<String,String> parameters = new TreeMap<String, String>();
    parameters.put(TABLENAME, tableName);
    parameters.put(OBJECTNAME, objectName);
    parameters.put(DATE, getDate());
    parameters.put(YEAR, getYear());
    
    return parameters;
  }
}
