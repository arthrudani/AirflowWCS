package com.daifukuamerica.wrxj.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Reads properties from memory.
 *  
 * <p><b>Details:</b> This class implements {@link MutablePropertiesLayer} using
 * an in-<wbr>memory {@link Map}, which is initially empty.</p>
 *  
 * @author Sharky
 */
final class MapLayer implements MutablePropertiesLayer
{

  private final Map<String, String> mpMap = new HashMap<String, String>();
  
  @Override
  public String getProperty(String isName)
  {
    return mpMap.get(isName);
  }

  @Override
  public void setProperty(String isName, String isValue)
  {
    mpMap.put(isName, isValue);
  }

  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    Set<String> vpAllNames = mpMap.keySet();
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
}

