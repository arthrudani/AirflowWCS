package com.daifukuamerica.wrxj.dataserver;

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
 * SysConfig Properties Layer impl.
 * @author A.D.
 * @since  26-May-2010
 */
public class SysConfigPropertiesLayer implements PropertiesLayer
{
  private static Set<String> mpAllNames = null;
  private static Map<String, String> mpProperties = new TreeMap<String, String>();

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
        vsValue = vpConfigServer.getSysConfigDottedPropertyValue(isName);
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

  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    try
    {
      if (mpAllNames == null)
      {
        StandardConfigurationServer vpConfigServer =
          Factory.create(StandardConfigurationServer.class);
        mpAllNames = vpConfigServer.getSysConfigDottedPropertyNames();
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
   */ 
  @Override
  public void refresh()
  {
    mpProperties.clear();
  }
}
