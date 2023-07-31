package com.daifukuamerica.wrxj.host;

import com.daifukuamerica.wrxj.application.PropertiesLayer;
import com.daifukuamerica.wrxj.application.PropertiesStack;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.HashSet;
import java.util.Set;

/**
 * Host configuration properties.
 * 
 * <p><b>Details:</b> This properties layer serves properties from the host
 * configuration data.</p>
 *  
 * @author Sharky
 */
public final class HostConfigPropertiesLayer implements PropertiesLayer
{

  /**
   * {@inheritDoc}
   * 
   * <p>If access to the host configuration data fails with an exception, this 
   * implementation logs the exception and returns <code>null</code>, indicating 
   * that the property is not defined.</p>
   */
  @Override
  public String getProperty(String isName)
  {
    try
    {
      StandardConfigurationServer vpConfigurationServer = 
        Factory.create(StandardConfigurationServer.class);
      String vsValue = vpConfigurationServer.getHostConfigPropertyValue(isName);
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
   * <p>If access to the host configuration data fails with an exception, this 
   * implementation logs the exception and returns <code>null</code>, indicating 
   * that the property is not defined.</p>
   */
  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    try
    {
      StandardConfigurationServer vpConfigurationServer = 
        Factory.create(StandardConfigurationServer.class);
      Set<String> vpAllNames = vpConfigurationServer.getHostConfigPropertyNames();
      Set<String> vpMatchingNames = PropertiesStack.selectByPrefix(vpAllNames, isPrefix);
      return vpMatchingNames;
    }
    catch (DBException ex)
    {
      Logger.getLogger().logException(ex);
      return new HashSet<String>();
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

