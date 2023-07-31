package com.daifukuamerica.wrxj.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class to read properties in from a file.  The properties are read in and
 * stored in a token list.  It is assumed that name/value pairs will be
 * consecutive in the token list once the file is parsed.
 * 
 * @author Stephen Kendorski (1.0)
 * @author karmstrong (2.0)
 * @author mandrus
 * @version 3.0
 */
public class PropertyFileReader extends PropertyReader
{
  public PropertyFileReader()
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
    return new BufferedReader(new FileReader(isPath));
  }
}