package com.daifukuamerica.wrxj.clc.database;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.factory.Factory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatabaseControllerDefinition implements ControllerDefinition
{

  StandardDeviceServer mpDevServ = Factory.create(StandardDeviceServer.class);
  Map<String,String> mpLocalProperties = new HashMap<String,String>();
  private String msName, msType;
  
  public DatabaseControllerDefinition(String isName, String isType)
  {
    super();
    msName = isName;
    msType = isType;
    addGroupProperties(isType);
    addGroupProperties(isName);
  }
  
  public DatabaseControllerDefinition(String isName, String isType, Map<String,String> ipInitialProps)
  {
    super();
    msName = isName;
    msType = isType;
    mpLocalProperties.putAll(ipInitialProps);   
    addGroupProperties(isType);
    addGroupProperties(isName);
  }

  public String getName()
  {
    return msName;
  }

  public String getType() throws ControllerConfigurationException
  {
    return msType;
  }

  public String getProperty(String isName)
  {
    String vsProperty = mpLocalProperties.get(isName);
//    if (vsProperty == null)
//      throw new ControllerConfigurationException("Controller " + msName + "does not have a property: " + isName);
    return vsProperty;
  }

  /**
   * Adds controller properties from application properties.
   * 
   * <p><b>Details:</b> This method searches through the application properties 
   * for properties that can be applied to this controller, based on its name.  
   * If any are found, they are added to this controller's properties map.</p>
   */
  private void addGroupProperties(String isName)
  {
    String vsPrefix = "ControllerConfig." + isName + ".";
    int vnPrefixLength = vsPrefix.length();
    Set<String> vpSet = Application.getPropertyNames(vsPrefix);
    for (String vsPropertyName: vpSet)
    {
      String vsPropertyValue = Application.getString(vsPropertyName); 
      vsPropertyName = vsPropertyName.substring(vnPrefixLength);
      mpLocalProperties.put(vsPropertyName, vsPropertyValue);
    }
  }
  
  public Set<String> getPropertyNames(String isPrefix)
  {
    Set<String> vpMatches = new HashSet<String>();
    for (String vsName: mpLocalProperties.keySet())
    {
      if (vsName.startsWith(isPrefix))
        vpMatches.add(vsName);
    }
    return vpMatches;
  }
  
}

