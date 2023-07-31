package com.daifukuamerica.wrxj.application;

import java.util.Properties;
import java.util.Set;

/**
 * Reads properties from system properties.
 * 
 * <p><b>Details:</b> This class implements {@link PropertiesLayer} using 
 * properties read from the system properties.  The system properties is queried
 * every time a property is queried from this layer, so changes in the system
 * properties will result in changes in this layer.</p>
 * 
 * @author Sharky
 */
public class SystemPropertiesLayer implements PropertiesLayer
{

  @Override
  public String getProperty(String isName)
  {
    String vsAttribute = System.getProperty(isName);
    return vsAttribute;
  }

  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    Properties vpProperties = System.getProperties();
    Set<String> vpAllNames = PropertiesFileLayer.getKeySet(vpProperties);
    Set<String> vsMatchingNames = PropertiesStack.selectByPrefix(vpAllNames, isPrefix);
    return vsMatchingNames;
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
}

