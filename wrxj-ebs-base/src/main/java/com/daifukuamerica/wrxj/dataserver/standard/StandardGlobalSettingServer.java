package com.daifukuamerica.wrxj.dataserver.standard;

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

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.aed.GlobalSetting;
import com.daifukuamerica.wrxj.dbadapter.data.aed.GlobalSettingData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A server for interacting with Wynsoft Global Settings
 */
public class StandardGlobalSettingServer extends StandardServer
{
  public static final String GS_CACHED_VALUE_KEY = "GS_CACHED_VALUE_KEY";

  private GlobalSetting mpGSHandler = Factory.create(GlobalSetting.class);
  
  /*====================================================================*/
  /*  Constructors                                                      */
  /*====================================================================*/

  /**
   * Constructor
   */
  public StandardGlobalSettingServer()
  {
    this(null);
  }

  /**
   * Constructor
   *
   * @param keyName
   */
  public StandardGlobalSettingServer(String keyName)
  {
//    super(keyName, GlobalSetting.GS_DATABASE);
    super(keyName);
    logDebug("Creating " + getClass().getSimpleName());
  }

  /*====================================================================*/
  /*  Overridden methods                                                */
  /*====================================================================*/

  /**
   * Shuts down this controller by cancelling any timers and shutting down the
   * Equipment.
   */
  @Override
  public void cleanUp()
  {
    mpGSHandler.cleanUp();
    super.cleanUp();
  }
  
  /*====================================================================*/
  /* Methods                                                            */
  /*====================================================================*/

  /**
   * Get the key for a Global Setting
   * 
   * @param isArea
   * @param isName
   * @return
   */
  public static String getGSKey(String isArea, String isName)
  {
    if (SKDCUtility.isFilledIn(isArea))
      return isArea + "." + isName;
    else
      return isName;
  }

  /**
   * Get a Global Setting property value
   *
   * @param isDottedPropertyName a string of the form:
   * <pre><b>Area.Name</b></pre>
   * @return the property value
   * @throws DBException
   */
  public String getDottedPropertyValue(String isDottedPropertyName) throws DBException
  {
    int vnDot = isDottedPropertyName.indexOf('.');
    if (vnDot < 0)
      return getString(null, isDottedPropertyName);

    String vsArea = isDottedPropertyName.substring(0, vnDot);
    String vsName = isDottedPropertyName.substring(vnDot + 1);
    return getString(vsArea, vsName);
  }

  /**
   * Get a list of Global Setting property names
   *
   * @return a Set containing strings of the form:
   * <pre><code>Area.Name</code></pre>
   * @throws DBException
   */
  public Set<String> getDottedPropertyNames() throws DBException
  {
    List<GlobalSettingData> vpGSList = mpGSHandler.getDataList();
    Set<String> vpSet = new HashSet<>(vpGSList.size());
    for (GlobalSettingData vpGSData : vpGSList)
    {
      vpSet.add(getGSKey(vpGSData.getArea(), vpGSData.getName()));
    }
    return vpSet;
  }

  /**
   * Get a String value
   * 
   * @param isArea
   * @param isName
   * @return
   * @throws DBException
   */
  public String getString(String isArea, String isName)
      throws DBException
  {
    GlobalSettingData vpGSData = mpGSHandler.getData(isArea, isName);
    if (vpGSData == null)
    {
      return null;
    }
    return vpGSData.getValue();
  }

  /**
   * Get a list of all global settings including associated cached values
   * 
   * @return
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<Map> getList(String isName) throws DBException {
    
    List<Map> vpGSList = mpGSHandler.getList(isName);
    for (Map m : vpGSList) {
      GlobalSettingData vpGsData = new GlobalSettingData();
      vpGsData.dataToSKDCData(m);
      String key = getGSKey(vpGsData.getArea(), vpGsData.getName());
      m.put(GS_CACHED_VALUE_KEY, Application.getString(key));
    }
    return vpGSList;
  }
  
  /*====================================================================*/
  /* Cache check                                                        */
  /*====================================================================*/
  /**
   * Check the global settings cache
   * 
   * @return true if the cache is okay, false otherwise
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public boolean checkCache() throws DBException
  {
    List<Map> vpCheckData = getList(null);
    for (Map m : vpCheckData)
    {
      String db = (String)m.get(GlobalSettingData.VALUE_NAME);
      String cache = (String)m.get(GS_CACHED_VALUE_KEY);
      if(!db.equals(cache))
      {
        return false;
      }
    }
    return true;
  }
}
