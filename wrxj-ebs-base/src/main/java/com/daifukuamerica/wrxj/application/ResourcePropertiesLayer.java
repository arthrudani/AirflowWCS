package com.daifukuamerica.wrxj.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Reads properties from file.
 * 
 * <p><b>Details:</b> This class implements {@link PropertiesLayer} using 
 * properties read from a standard Java resource file.  The properties file is
 * read when the instance is created.</p>
 * 
 * @author mandrus
 */
public final class ResourcePropertiesLayer implements PropertiesLayer
{
  /**
   * Cached properties.
   *  
   * <p><b>Details:</b> This field caches the properties parsed from the 
   * properties file.</p>
   */
  private final Properties mpProperties = new Properties();
  
//  /**
//   * Reads and caches properties.
//   * 
//   * <p><b>Details:</b> This constructor opens, parses properties from, and 
//   * closes the given properties resource file.</p>
//   * 
//   * @param isFile the properties file path
//   * @param izReread true iff file should be reread on change
//   * @throws IOException if an I/O error occurs while processing the file
//   */
//  public ResourcePropertiesLayer() throws IOException
//  {
//  }

  /**
   * Reads and caches properties.
   * 
   * <p><b>Details:</b> This constructor opens, parses properties from, and 
   * closes the given properties resource file.</p>
   * 
   * @param isFile the properties file path
   * @throws IOException if an I/O error occurs while processing the file
   */
  public ResourcePropertiesLayer(String isFile) throws IOException
  {
    InputStream vpIS = ResourcePropertiesLayer.class.getResourceAsStream(isFile);
    if (vpIS == null)
    {
      throw new IOException("Resource \"" + isFile + "\" does not exist.");
    }
    mpProperties.load(vpIS);
    vpIS.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getProperty(String isName)
  {
    return mpProperties.getProperty(isName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    Set<String> vpAllNames = getKeySet(mpProperties);
    Set<String> vpMatchingNames = PropertiesStack.selectByPrefix(vpAllNames, isPrefix);
    return vpMatchingNames;
  }

  
  /**
   * {@inheritDoc}
   * 
   * <p><i>Currently unsupported by this implementation.</i></p>
   */ 
  @Override
  public void refresh()
  {
  }

  /**
   * Extracts name set from Properties.
   * 
   * <p><b>Details:</b> This method extracts the names of all properties from
   * the given {@link Properties} and returns them in an unordered set.</p>
   * 
   * @param ipProperties the properties
   * @return the names
   */
  static Set<String> getKeySet(Properties ipProperties)
  {
    int vnSize = ipProperties.size();
    Set<String> vpSet = new HashSet<String>(vnSize);
    for (Object vpKey: ipProperties.keySet())
      vpSet.add((String) vpKey);
    return vpSet;
  }
}

