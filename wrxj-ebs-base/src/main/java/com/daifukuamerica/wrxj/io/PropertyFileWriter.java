package com.daifukuamerica.wrxj.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class PropertyFileWriter
{
  
  /**
   * Method writes out a properties file
   * @param isPath the path and filename
   * @param ipMap a map containing the properties to write
   * @throws IOException
   */
  public void writeProperties(String isPath, Map ipMap) throws IOException
  {
    BufferedWriter vpWriter = new BufferedWriter(new FileWriter(isPath));
    Iterator<String> iter = ipMap.keySet().iterator();
    while(iter.hasNext())
    {
      String sKey = iter.next();
      Object theValue = ipMap.get(sKey);
      vpWriter.write(sKey + "=" + theValue.toString());
      vpWriter.newLine();
    }
    vpWriter.close();
  }

}
