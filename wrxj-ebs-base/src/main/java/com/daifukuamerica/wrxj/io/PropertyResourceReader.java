package com.daifukuamerica.wrxj.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class to read properties in from a resource file. The properties are read in
 * and stored in a token list. It is assumed that name/value pairs will be
 * consecutive in the token list once the file is parsed.
 * 
 * @author mandrus
 * @version 1.0
 */
public class PropertyResourceReader extends PropertyReader
{
  public PropertyResourceReader()
  {
  }

  /*========================================================================*/
  /*  Implement abstract methods                                            */
  /*========================================================================*/

  /**
   * @see com.daifukuamerica.wrxj.io.PropertyReader#stringToReader(java.lang.String)
   */
  @Override
  protected BufferedReader stringToReader(String isPath)
      throws FileNotFoundException, IOException
  {
    return new BufferedReader(new InputStreamReader(
        getClass().getResourceAsStream(isPath)));
  }
}