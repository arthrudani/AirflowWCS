package com.daifukuamerica.wrxj.application;

import java.util.Map;
import java.util.Set;

/**
 * Renders environment variables as properties.
 * 
 * <p><b>Details:</b> This properties layer derives properties from the system
 * environment (environment variables).  Note that there may be some 
 * case-<wbr>sensitivity issues with environment variable names.  See the method
 * documentation for details.</p>
 * 
 * @author Sharky
 */
public class EnvironmentPropertiesLayer implements PropertiesLayer
{

  /**
   * {@inheritDoc}
   * 
   * <p>Note that on some platforms, such as Windows, the supplied property name 
   * may be interpreted without sensitivity to case.  Thus, if there is an 
   * environment variable called "ALPHA", requesting a property called "alpha" 
   * may in fact access the same property.</p>
   */
  @Override
  public String getProperty(String isName)
  {
    String vsEnv = System.getenv(isName);
    return vsEnv;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    Map<String, String> vpEnv = System.getenv();
    Set<String> vpAllNames = vpEnv.keySet();
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

