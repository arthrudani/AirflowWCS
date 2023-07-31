package com.daifukuamerica.wrxj.io;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
public abstract class PropertyReader
{
  private List<String> tokenList = new ArrayList<String>();

  public PropertyReader()
  {
  }


  /*========================================================================*/
  /*  Mini-factory.                                                         */
  /*========================================================================*/
  
  /**
   * Get an appropriate PropertyReader
   * 
   * @return
   *    <code>PropertyResourceReader</code> if 
   *    <code>WarehouseRx.LOAD_CONFIGS_FROM_RESOURCE</code> is set to true,
   *    <code>PropertyFileReader</code> otherwise
   */
  public static final PropertyReader newInstance()
  {
    if (Application.getBoolean(WarehouseRx.LOAD_CONFIGS_FROM_RESOURCE, false))
    {
      return new PropertyResourceReader();
    }
    return new PropertyFileReader();
  }
  
  
  /*========================================================================*/
  /*  Abstract methods.                                                     */
  /*========================================================================*/
  
  /**
   * Convert a String path to a BufferedReader
   * 
   * @param isPath
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  protected abstract BufferedReader stringToReader(String isPath)
      throws FileNotFoundException, IOException;


  /*========================================================================*/
  /*  Concrete public methods.                                              */
  /*========================================================================*/
  
  /**
   * Fetch properties text from a file/resource/URL.
   *
   * @param isPropertiesPath the file path and name, URL, etc
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void readProperties(String isPropertiesPath)
      throws FileNotFoundException, IOException
  {
    readProperties(stringToReader(isPropertiesPath));
  }
  
  /**
   * Fetch a group of name/value data pairs from a file.
   *
   * @param isPropertiesPath the file path and name, URL, etc
   * @param isCollectionSeparator the group separator
   * @return the groups
   * @throws FileNotFoundException
   * @throws IOException
   */
  public List<List<String>> getAllPropertyCollections(
      String isPropertiesPath, String isCollectionSeparator)
      throws FileNotFoundException, IOException
  {
    return readAllPropertyCollections(stringToReader(isPropertiesPath),
        isCollectionSeparator);
  }
  
  /**
   * Get a property from a list of properties.
   * 
   * It is assumed that name/value pairs will be consecutive in the list.
   * 
   * @param list List of properties to search
   * @param key Property to search for.
   * @return Value of property, null if none is found.
   */
  public String getProperty(List<String> list, String key)
  {
    String sResult = null;
    Iterator<String> listIterator = list.iterator();
    while (listIterator.hasNext())
    {
      String sToken = listIterator.next();
      if (sToken.equalsIgnoreCase(key))
      {
        if (listIterator.hasNext())
        {
          sResult = listIterator.next();
          break;
        }
      }
    }
    return sResult;
  }

  /**
   * 
   * @param list
   * @param key
   * @return
   */
  private int getIntProperty(List<String> list, String key)
  {
    String s = getProperty(list, key);
    boolean intOK = true;
    int iValue = 0;
    if (s != null)
    {
      for (int i = 0; i < s.length(); i++)
      {
        char c = s.charAt(i);
        if ((c < '0') || (c > '9'))
        {
          intOK = false;
          break;
        }
        iValue = (iValue * 10) + c - 0x30;
      }
    }
    else
    {
      intOK = false;
    }
    if (intOK)
    {
      return iValue;
    }
    else
    {
      return -1;
    }
  }
  

  /**
   * Convert a string into a series of property name/value pairs.
   * 
   * The name/value pairs are stored in an internal list of strings.
   * This method is to be used in conjunction with <code>getProperty </code>
   * and <code>getIntProperty</code>.
   * @param The string to tokenize
   */
  public void tokenize(String s)
  {
    StringTokenizer st = new StringTokenizer(s, "\t\n =");
    while (st.hasMoreTokens())
    {
      tokenList.add(st.nextToken());
    }
  }

  /**
   * Read a property from the token list.
   * 
   * Note: it is assumed that the <code>readProperties</code> or <code>tokenize</code>
   * method will already have been called.
   * 
   * @param key The name of the property to get.
   * @return The value of the property, null if the property is not found.
   */
  public String getProperty(String key)
  {
    return getProperty(tokenList, key);
  }

  /**
   * Read a property from the token list.
   * 
   * Note: it is assumed that the <code>readProperties</code> or <code>tokenize</code>
   * method will already have been called.
   * 
   * @param key The name of the property to get.
   * @return The value of the property, -1 if the property is not found.
   */
  public int getIntProperty(String key)
  {
    return getIntProperty(tokenList, key);
  }

  /**
   * Read a property from the token list.
   * 
   * Note: it is assumed that the <code>readProperties</code> or <code>tokenize</code>
   * method will already have been called.
   * 
   * @param key The name of the property to get.
   * @param value The default value to return if no property is found
   * @return The value of the property, <code>value</code> if the property is not found.
   */
  public int getIntProperty(String key, int value)
  {
    int result = getIntProperty(tokenList, key);
    if (result == -1)
    {
      result = value;
    }
    return result;
  }

  
  /*========================================================================*/
  /*  Private concrete methods                                              */
  /*========================================================================*/

  /**
   * Read in a series of name/value pairs from a properties file.
   * 
   * The name/value pairs are stored in an internal list of strings.
   * This method is to be used in conjunction with <code>getProperty </code>
   * and <code>getIntProperty</code>.
   * 
   * @param ipReader Reader to use to get the properties.  The reader will be
   *        closed upon completion.
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void readProperties(BufferedReader ipReader)
      throws FileNotFoundException, IOException
  {
    tokenList = new ArrayList<String>();
    while (true)
    {
      String vsLine = ipReader.readLine();
      if (vsLine == null)
      {
        break;
      }
      if (vsLine.startsWith("//"))
      {
        continue;
      }
      StringTokenizer vpST = new StringTokenizer(vsLine, "\t\n =");
      while (vpST.hasMoreTokens())
      {
        tokenList.add(vpST.nextToken());
      }
    }
    ipReader.close();
  }
  
  /**
   * A more efficient, cleaner, method for initializing the data from the
   * equipment file.
   * 
   * This method takes the place of the combination of
   * <code>readProperties</code> and <code>findAllPropertyCollections</code>.
   * It performs the same function using only one pass through the file data
   * instead of several.
   * 
   * @param ipReader Reader to use to get the properties.  The reader will be
   *        closed upon completion.
   * @param isCollectionSeparator Text that is used in the file to separate
   *            difference equipment collections.
   *            
   * @return <code>List of Lists of equipment properties.
   * @throws FileNotFoundException
   * @throws IOException
   */
  protected List<List<String>> readAllPropertyCollections(
      BufferedReader ipReader, String isCollectionSeparator)
      throws FileNotFoundException, IOException
  {
    List<List<String>> vpCollections = new ArrayList<List<String>>();
    List<String> vpCurrent = new ArrayList<String>();
    
    while (true)
    {
      String vsLine = ipReader.readLine();
      if (vsLine == null)
      {
        break;
      }
      if (vsLine.startsWith("//"))
      {
        continue;
      }
      StringTokenizer vpST = new StringTokenizer(vsLine, "\t\n =");
      while (vpST.hasMoreTokens())
      {
        String vsNext = vpST.nextToken();
        if (vsNext.equalsIgnoreCase(isCollectionSeparator))
        {
          // Start a new collection
          vpCurrent = new ArrayList<String>();
          vpCollections.add(vpCurrent);
        }
        vpCurrent.add(vsNext);
      }
    }
    ipReader.close();
    return vpCollections;
  }
}