package com.daifukuamerica.wrxj.clc;

import com.daifukuamerica.wrxj.application.PropertiesLayer;
import com.daifukuamerica.wrxj.application.PropertiesStack;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Controller configuration properties.
 * 
 * <p><b>Details:</b> This properties layer serves properties from the 
 * controller configuration data.</p>
 *  
 * @author Sharky
 */
public final class ControllerConfigPropertiesLayer implements PropertiesLayer
{
  private static Set<String> mpAllNames = null;
  private static Map<String, String> mpProperties = new TreeMap<String, String>();
    
  /**
   * {@inheritDoc}
   * 
   * <p>If access to the controller configuration data fails with an exception, 
   * this implementation logs the exception and returns <code>null</code>, 
   * indicating that the property is not defined.</p>
   */
  @Override
  public String getProperty(String isName)
  {
    try
    {
      String vsValue = mpProperties.get(isName);
      if (vsValue == null)
      {
        StandardConfigurationServer vpConfigServer = 
          Factory.create(StandardConfigurationServer.class);
        vsValue = vpConfigServer.getControllerConfigDottedPropertyValue(isName);
        mpProperties.put(isName, vsValue);
      }
      return vsValue;
    }
    catch (DBException ex)
    {
      Logger.getLogger().logException(ex);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * <p>If access to the controller configuration data fails with an exception, 
   * this implementation logs the exception and returns <code>null</code>, 
   * indicating that the property is not defined.</p>
   */
  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    try
    {
      if (mpAllNames == null)
      {
        StandardConfigurationServer vpConfigServer = 
          Factory.create(StandardConfigurationServer.class);
        mpAllNames = vpConfigServer.getControllerConfigDottedPropertyNames();
      }
      Set<String> vpMatchingNames = PropertiesStack.selectByPrefix(mpAllNames, isPrefix);
      return vpMatchingNames;
    }
    catch (DBException ex)
    {
      Logger.getLogger().logException(ex);
      return new HashSet<String>(0);
    }
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

