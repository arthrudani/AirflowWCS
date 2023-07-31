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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

/**
 * Generic class to assist in code generation
 * 
 * @author mandrus
 */
public class CodeGenerator
{
  /**
   * Generate code based on a template and replacement parameters
   * 
   * @param templateName
   * @param outputName
   * @param parameters
   * @throws IOException
   */
  public void generate(String templateName, String outputName, Map<String,String> parameters) 
    throws IOException {
    
    String template = readResource(templateName);
    
    for (String key : parameters.keySet()) {
      template = template.replaceAll(key, parameters.get(key));
    }
    
    writeFile(outputName, template);
  }
  
  /*========================================================================*/
  /* Input methods                                                          */
  /*========================================================================*/

  /**
   * Reads a file into a String.  Ignores character encoding since it should 
   * match the system in this case.
   * 
   * @param templateName
   * @return
   * @throws IOException
   */
  protected String readResource(String templateName) 
      throws IOException
  {
    InputStream input = null;
    Scanner scan = null;
    
    try { 
      input = CodeGenerator.class.getResourceAsStream(
          "/template/" + templateName);
      scan = new Scanner(input);
      scan.useDelimiter("\\A");
      return scan.hasNext() ? scan.next() : "";
      
    } finally {
      if (input != null) input.close();
      if (scan != null) scan.close();
    }
  }

  /**
   * Reads a file into a String.  Ignores character encoding since it should 
   * match the system in this case.
   * 
   * @param templateName
   * @return
   * @throws IOException
   */
  protected String readFile(String templateName) 
      throws IOException
  {
    byte[] encoded = Files.readAllBytes(Paths.get(templateName));
    return new String(encoded);
  }
  
  /*========================================================================*/
  /* Output methods                                                         */
  /*========================================================================*/
  
  /**
   * Writes a String into a file.
   * 
   * @param outputName
   * @param content
   * @throws IOException
   */
  protected void writeFile(String outputName, String content)
      throws IOException
  {
    Files.write(Paths.get(outputName), content.getBytes());
  }

  /*========================================================================*/
  /* Helper methods                                                         */
  /*========================================================================*/
  /**
   * Space pad a String.
   * 
   * <p>Created because SKDCUtility.spaceFillTrailing() truncates strings that
   * are longer than length!</p>
   * 
   * @param s
   * @param length
   * @return
   */
  public String spacePad(String s, int length) {
    StringBuilder sb = new StringBuilder(s);
    while (sb.length() < length) {
      sb.append(" ");
    }
    return sb.toString();
  }
  
  /*========================================================================*/
  /* Typical replacement methods                                            */
  /*========================================================================*/

  /**
   * Typical date replacement
   * @return
   */
  protected String getDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
    return sdf.format(new Date());
  }
  
  /**
   * Typical year replacement
   * @return
   */
  protected String getYear() {
    return "" + Calendar.getInstance().get(Calendar.YEAR);
  }
}

