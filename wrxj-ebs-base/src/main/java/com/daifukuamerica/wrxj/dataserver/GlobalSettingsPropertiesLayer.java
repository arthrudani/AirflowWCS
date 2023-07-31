package com.daifukuamerica.wrxj.dataserver;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$
  
  Copyright (c) 2015 Wynright Corporation.  All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.application.PropertiesLayer;
import com.daifukuamerica.wrxj.application.PropertiesStack;
import com.daifukuamerica.wrxj.dataserver.standard.StandardGlobalSettingServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Global Settings Properties Layer impl.
 * @author mandrus
 * @since  22-Dec-2015
 */
public class GlobalSettingsPropertiesLayer implements PropertiesLayer
{
  private static Set<String> mpAllNames = null;
  private static Map<String, String> mpProperties = new TreeMap<>();

  public GlobalSettingsPropertiesLayer()
  {
    super();
    fillCache();
  }
  
  /**
   * {@inheritDoc}
   */ 
  @Override
  public String getProperty(String isName)
  {
    return mpProperties.get(isName);
  }

  /**
   * {@inheritDoc}
   */ 
  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    return PropertiesStack.selectByPrefix(mpAllNames, isPrefix);
  }
  
  /**
   * {@inheritDoc}
   */ 
  @Override
  public void refresh()
  {
    Logger.getLogger().logOperation("Reloading Global Settings");
    fillCache();
    
  }
  
  /**
   * Fill the cache
   */
  private synchronized void fillCache() {
    Map<String, String> vpProperties = new TreeMap<>();
    try
    {
      StandardGlobalSettingServer vpGSServer =
          Factory.create(StandardGlobalSettingServer.class);
      
      mpAllNames = vpGSServer.getDottedPropertyNames();
      int vnChangeCount = 0;
      for (String key : mpAllNames)
      {
        String vsValue = vpGSServer.getDottedPropertyValue(key);
        if (mpProperties.containsKey(key))
        {
          vnChangeCount += logChange(key, mpProperties.get(key), vsValue);
        }
        vpProperties.put(key, vsValue);
      }
      logChanges(vnChangeCount);
    }
    catch (DBException ex)
    {
      Logger.getLogger().logException(
          "Error filling Wynsoft Global Settings cache", ex);
      mpAllNames = new HashSet<>();
    }
    mpProperties = vpProperties;
  }
  
  /**
   * Log changes
   * 
   * @param isKey
   * @param isOld
   * @param isNew
   * @return 1 for change, 0 for no change
   */
  private int logChange(String isKey, String isOld, String isNew)
  {
    if ((isOld == null && isNew != null)
        || (isOld != null && !isOld.equals(isNew)))
    {
      Logger.getLogger().logOperation(
          String.format("Global Setting [%1$s] changed from [%2$s] to [%3$s]",
              isKey, isOld, isNew));
      return 1;
    }
    return 0;
  }
  
  /**
   * Log total change count
   * 
   * @param inCount
   */
  private void logChanges(int inCount)
  {
    Logger.getLogger().logOperation(
        String.format("Detected [%1$d] Global Settings change%2$s",
            inCount, inCount == 1 ? "" : "s"));
  }
}
