package com.daifukuamerica.wrxj.dbadapter.data;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2004-2008 Daifuku America Corporation  All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Description:<BR>
 *   Class for handling  table interactions.
 *
 * @author       sbw
 * @version      1.0
 * <BR>Created: 16-Feb-05<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
public class SysConfig extends BaseDBInterface
{
  public static final String DEFAULT_RESTRICTED_ACCESS_WARNING = 
    "Unauthorized access to or use of this system is prohibited.\n" +  
    "All access and use may be monitored and recorded.";
  protected static final String RAW_GROUP = "Login";
  protected static final String RAW_PARAMETER = "RestrictedAccessWarning%";

  public static final String ALLOCATION_STRATEGY = "AllocationStrategy";
  public static final String LAST_LOGIN = "LastLogin";
  
  public static final String GUI_CFG_PREFIX = "GUI@";
  public static final String DEFAULT_STATION = "DefaultStation";
  
  public static final String GLOBAL_CONFIGURATION = "GlobalConfiguration";
  public static final String EMULATION_MODE = "EmulationMode";
  public static final String DEVICETYPE_SCALE = "Scale";
  
  private static final int DEFAULT_TABS_PER_GROUP = 8;
  
  public static final String OPTYPE_PICK = "Pick";
  public static final String OPTYPE_STORE = "Store";

  private SysConfigData mpSysCfgData;

  public SysConfig()
  {
    super("SysConfig");
    mpSysCfgData = Factory.create(SysConfigData.class);
  }

  /**
   * Sets Objects for garbage collection.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpSysCfgData = null;
  }

 /**
  * Get a parameter value from SysConfig table.
  * @param isGroup the group id.
  * @param isParameterName the parameter name to lookup.
  * @return parameter value if found or else empty string.
  * @throws DBException if there is a DB error.
  */
  public String getSysConfigValue(String isGroup, String isParameterName)
         throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, isGroup);
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, isParameterName);

    SysConfigData vpSysData = getElement(mpSysCfgData, DBConstants.NOWRITELOCK);
    return((vpSysData == null) ? "" : vpSysData.getParameterValue());
  }

  /**
   * Method to get List of SysConfigs..
   * 
   * @param allOrNone <code>boolean</code> containing indicator of string to
   *            start list with. The values are SKDCConstants.ALL_STRING or
   *            SKDCConstants.NONE_STRING
   * 
   * @return StringBuffer SysConfigs.
   */
  public String[] getSysConfigChoices(boolean allOrNone) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT ").append(SysConfigData.GROUP_NAME)
             .append(" || '|' ||").append(SysConfigData.PARAMETERNAME_NAME)
             .append(" \"").append(SysConfigData.PARAMETERNAME_NAME).append("\"")
             .append(" FROM ").append(getWriteTableName())
             .append(" ORDER BY ").append( SysConfigData.GROUP_NAME).append(", ")
             .append(SysConfigData.PARAMETERNAME_NAME);
    
    return getList(vpSql.toString(), SysConfigData.PARAMETERNAME_NAME,
           (allOrNone) ? SKDCConstants.ALL_STRING : SKDCConstants.NO_PREPENDER);
  }

  /**
   * Method finds the parameter names for a specific group.
   * 
   * @param sGroup <code>String</code> containing group name.
   * 
   * @return <code>String[]</code> of parameter names. An empty array if no
   *         matches are found.
   * @throws DBException
   */
  public String[] getParameterNames(String sGroup) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sParameterName FROM SysConfig WHERE ")
             .append("sGroup = ? ");
    List<Map> vpArrList = fetchRecords(vpSql.toString(), sGroup);
    
    return(SKDCUtility.toStringArray(vpArrList, SysConfigData.PARAMETERNAME_NAME));
  }
  
  /**
   * Method gets all the parameter values for a group with the ".class" modifier
   * appended to the parameter name.
   * @param isGroup
   * @return Array of String representation of class names.
   */
  public String[] getClassNameParameterValues(String isGroup) throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, isGroup);
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, "%.class", KeyObject.LIKE);

    return SKDCUtility.toStringArray(getAllElements(mpSysCfgData), SysConfigData.PARAMETERVALUE_NAME);
  }
  
  /**
   * Get the name/value pairs for a given group
   * 
   * @param isGroup
   * @return
   * @throws DBException
   */
  public Map<String, String> getNameValuePairs(String isGroup) throws DBException
  {
    Map<String,String> vpPairs = new HashMap<String,String>();
    StringBuilder vpSql = new StringBuilder("SELECT " + SysConfigData.PARAMETERNAME_NAME + ", ")
             .append(SysConfigData.PARAMETERVALUE_NAME + " FROM SysConfig WHERE ")
             .append(SysConfigData.GROUP_NAME + " = ?");
    List<Map> vpList = fetchRecords(vpSql.toString(), isGroup);
    for (Map vpMap : vpList)
    {
      String vsName = DBHelper.getStringField(vpMap, SysConfigData.PARAMETERNAME_NAME);
      String vsValue = DBHelper.getStringField(vpMap, SysConfigData.PARAMETERVALUE_NAME);
      vpPairs.put(vsName, vsValue);
    }
    return vpPairs;
  }

  /**
   * Get the enabled name/value pairs for a given group
   * 
   * @param isGroup
   * @return
   * @throws DBException
   */
  @SuppressWarnings("rawtypes")
  public Map<String, String> getEnabledNameValuePairs(String isGroup) throws DBException
  {
    Map<String,String> vpPairs = new HashMap<>();
    StringBuilder vpSql = new StringBuilder("SELECT ").append(SysConfigData.PARAMETERNAME_NAME)
        .append(", ").append(SysConfigData.PARAMETERVALUE_NAME)
        .append(" FROM ").append(getReadTableName())
        .append(" WHERE ").append(SysConfigData.GROUP_NAME)
        .append(" = ? AND ").append(SysConfigData.ENABLED_NAME)
        .append("=").append(DBConstants.YES);
    
    List<Map> vpList = fetchRecords(vpSql.toString(), isGroup);
    for (Map vpMap : vpList)
    {
      String vsName = DBHelper.getStringField(vpMap, SysConfigData.PARAMETERNAME_NAME);
      String vsValue = DBHelper.getStringField(vpMap, SysConfigData.PARAMETERVALUE_NAME);
      vpPairs.put(vsName, vsValue);
    }
    return vpPairs;
  }
  
  /*========================================================================*/
  /* Restricted Access Warning                                              */
  /*========================================================================*/
  
  /**
   * This builds the restricted access warning for the database.
   * 
   * <P>This method reads the SysConfig table with sGroup="Login" and 
   * sParameterName like "RestrictedAccessWarning%", sorted by sParameterName.
   * It appends each successive sParameterValue and converts <tt>\n</tt> to  
   * new-lines.</P>
   * 
   * <P>If no database values are set, this returns 
   * <code>DEFAULT_RESTRICTED_ACCESS_WARNING</code>.</P>
   * 
   * @return The restricted access warning
   */
  public String getRestrictedAccessWarning()
  {
    String vsWarning = "";
    
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, RAW_GROUP);
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, RAW_PARAMETER, KeyObject.LIKE);
    mpSysCfgData.addOrderByColumn(SysConfigData.PARAMETERNAME_NAME);
    try
    {
      List<Map> vpWarnings = getAllElements(mpSysCfgData);
      for (Map m : vpWarnings)
      {
        mpSysCfgData.dataToSKDCData(m);
        vsWarning += mpSysCfgData.getParameterValue();
      }
      // Repair lost newline characters
      vsWarning = vsWarning.replace("\\n", "\n");
    }
    catch (DBException dbe)
    {
      dbe.printStackTrace();
    }
    if (vsWarning.trim().length() == 0)
    {
      vsWarning = DEFAULT_RESTRICTED_ACCESS_WARNING;
    }
    return vsWarning;
  }

  /*========================================================================*/
  /*  Workstation Defaults (public)                                         */
  /*                                                                        */
  /*  The public methods should take two key parameters, machine name and   */
  /*  IP address.  For getters, precedences should be given to the machine  */
  /*  name.                                                                 */
  /*========================================================================*/
  
  /**
   * Get the default station list for a given screen type and machine name, 
   * IP address or local host if user is a superuser.
   *  
   * @param isMachineName
   * @param isIPAddress
   * @return String if configured, null otherwise
   * @throws DBException
   */
  public String[] getDefaultStationList(String isMachineName, String isIPAddress, 
          String isScreenType, boolean izSuperUser)
      throws DBException
  {
    // construct values - convert machine name to lower case
    String vsMachineName = GUI_CFG_PREFIX + isMachineName.toLowerCase();
    String vsIPAddress = GUI_CFG_PREFIX + isIPAddress;
    String vsLocalHost = GUI_CFG_PREFIX + "localhost";

    String vsHostList = "'" + vsMachineName + "', '" + vsIPAddress + "'";
    if (izSuperUser)
    {
      vsHostList += ", '" + vsLocalHost + "'";
    }
    
    String vsTypeList = "'" + isScreenType + "'";
    if (isScreenType.equals(SKDCConstants.ALL_STRING) == false)
    {
      vsTypeList += ", '" + SKDCConstants.ALL_STRING + "'";
    }
    
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT ").append(SysConfigData.PARAMETERNAME_NAME)
        .append(" FROM ").append(getWriteTableName())
        .append(" WHERE ").append(SysConfigData.PARAMETERVALUE_NAME)
                .append(" IN (").append(vsHostList).append(")")
        .append("   AND ").append(SysConfigData.SCREENTYPE_NAME)
                .append(" IN (").append(vsTypeList).append(")")
        .append(" ORDER BY ").append(SysConfigData.PARAMETERNAME_NAME);
    
    return getList(vpSql.toString(), SysConfigData.PARAMETERNAME_NAME,
                   SKDCConstants.NO_PREPENDER);
  }

  /*========================================================================*/
  /*  Workstation Defaults (public)                                         */
  /*                                                                        */
  /*  The public methods should take two key parameters, machine name and   */
  /*  IP address.  For getters, precedences should be given to the machine  */
  /*  name.                                                                 */
  /*========================================================================*/
  
  /**
   * Get the default station for a given machine name <B>-OR-</B> IP address.
   * The search by IP address is only performed if the search by name fails.
   *  
   * @param isMachineName
   * @param isIPAddress
   * @return String if configured, null otherwise
   * @throws DBException
   */
  public String getDefaultStation(String isMachineName, String isIPAddress, 
          String isScreenType, boolean izSuperUser)
      throws DBException
  {
    // construct values - convert machine name to lower case
    String vsMachineName = GUI_CFG_PREFIX + isMachineName.toLowerCase();
    String vsIPAddress = GUI_CFG_PREFIX + isIPAddress;
    String vsLocalHost = GUI_CFG_PREFIX + "localhost";
    
    // check for given screen type by Machine Name
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, DEFAULT_STATION);
    mpSysCfgData.setKey(SysConfigData.PARAMETERVALUE_NAME, vsMachineName);
    if (isScreenType != null && isScreenType.isEmpty() == false)
    {
      mpSysCfgData.setKey(SysConfigData.SCREENTYPE_NAME, isScreenType);
    }
    List<Map> vpSCDataList = getAllElements(mpSysCfgData);

    if (vpSCDataList != null && vpSCDataList.size() <= 0)
    {
      // check for given screen type by IP Address
      mpSysCfgData.clear();
      mpSysCfgData.setKey(SysConfigData.GROUP_NAME, DEFAULT_STATION);
      mpSysCfgData.setKey(SysConfigData.PARAMETERVALUE_NAME, vsIPAddress);
      if (isScreenType != null && isScreenType.isEmpty() == false)
      {
        mpSysCfgData.setKey(SysConfigData.SCREENTYPE_NAME, isScreenType);
      }
      vpSCDataList = getAllElements(mpSysCfgData);
    }

    if (vpSCDataList != null && vpSCDataList.size() <= 0 && izSuperUser)
    {
      // check for ALL screen type by Machine Name
      mpSysCfgData.clear();
      mpSysCfgData.setKey(SysConfigData.GROUP_NAME, DEFAULT_STATION);
      mpSysCfgData.setKey(SysConfigData.PARAMETERVALUE_NAME, vsLocalHost);
      mpSysCfgData.setKey(SysConfigData.SCREENTYPE_NAME, isScreenType);
      vpSCDataList = getAllElements(mpSysCfgData);
    }


    if (vpSCDataList != null && vpSCDataList.size() <= 0)
    {
      // check for ALL screen type by Machine Name
      mpSysCfgData.clear();
      mpSysCfgData.setKey(SysConfigData.GROUP_NAME, DEFAULT_STATION);
      mpSysCfgData.setKey(SysConfigData.PARAMETERVALUE_NAME, vsMachineName);
      mpSysCfgData.setKey(SysConfigData.SCREENTYPE_NAME, SKDCConstants.ALL_STRING);
      vpSCDataList = getAllElements(mpSysCfgData);
    }

    if (vpSCDataList != null && vpSCDataList.size() <= 0)
    {
      // check for ALL screen type by IP Address
      mpSysCfgData.clear();
      mpSysCfgData.setKey(SysConfigData.GROUP_NAME, DEFAULT_STATION);
      mpSysCfgData.setKey(SysConfigData.PARAMETERVALUE_NAME, vsIPAddress);
      mpSysCfgData.setKey(SysConfigData.SCREENTYPE_NAME, SKDCConstants.ALL_STRING);
      vpSCDataList = getAllElements(mpSysCfgData);
    }

    if (vpSCDataList != null && vpSCDataList.size() <= 0 && izSuperUser)
    {
      // check for ALL screen type by IP Address
      mpSysCfgData.clear();
      mpSysCfgData.setKey(SysConfigData.GROUP_NAME, DEFAULT_STATION);
      mpSysCfgData.setKey(SysConfigData.PARAMETERVALUE_NAME, vsLocalHost);
      mpSysCfgData.setKey(SysConfigData.SCREENTYPE_NAME, SKDCConstants.ALL_STRING);
      vpSCDataList = getAllElements(mpSysCfgData);
    }

    if (vpSCDataList != null && vpSCDataList.size() > 0)
    {
      SysConfigData vpSCData = Factory.create(SysConfigData.class);
      vpSCData.dataToSKDCData(vpSCDataList.get(0));
      return vpSCData.getParameterName();
    }
    else
    {
      return "";
    }
  }
  
  
  /**
   * Method to modify a sys config row.  This method must be called from within a
   * transaction.
   *
   * @param isGroup
   * @param isParameterName
   * @param isParameterValue
   * @throws DBException
   */
  public void updateSysConfigValue(String isGroup, String isParameterName, String isParameterValue)
         throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, isGroup);
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, isParameterName);
    if (exists(mpSysCfgData))
    {
      mpSysCfgData.setParameterValue(isParameterValue);
      modifyElement(mpSysCfgData);
    }
  }

  /**
   * Set the default station
   * 
   * @param isMachineName
   * @param isIPAddress
   * @param isStation
   * @throws DBException
   */
  public void setDefaultStation(String isMachineName, String isIPAddress,
      String isStation) throws DBException
  {
    if (isStation == null || isStation.length() == 0)
    {
      deleteDefaultStation(isMachineName);
      deleteDefaultStation(isIPAddress);
    }
    else
    {
      if (isMachineName != null && isMachineName.length() > 0)
      {
        if (existsDefaultStation(isMachineName))
          modifyDefaultStation(isMachineName, isStation);
        else
          addDefaultStation(isMachineName, isStation);
      }
      if (isIPAddress != null && isIPAddress.length() > 0)
      {
        if (existsDefaultStation(isIPAddress))
          modifyDefaultStation(isIPAddress, isStation);
        else
          addDefaultStation(isIPAddress, isStation);
      }
    }
  }
  
  public static boolean isScaleEmulationEnabled()
  {
    boolean vzEnabled = false;
    try
    {
      SysConfigData vpSysCfgData = Factory.create(SysConfigData.class);
      vpSysCfgData.setKey(SysConfigData.GROUP_NAME, GLOBAL_CONFIGURATION);
      vpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, EMULATION_MODE);
      vpSysCfgData.setKey(SysConfigData.PARAMETERVALUE_NAME, DEVICETYPE_SCALE);
      // Check SysConfig first
      SysConfig mpSC = Factory.create(SysConfig.class);
      SysConfigData vpSysData = mpSC.getElement(vpSysCfgData, DBConstants.NOWRITELOCK);
      // There is no SysConfig entry--use LocalWorkStation
      if (vpSysData != null && vpSysData.getEnabled() == DBConstants.YES)
      {
        vzEnabled = true;
      }
    }
    catch (DBException dbe)
    {
      Logger.getLogger().logException(dbe);
    }
    return vzEnabled;
  }

  /*========================================================================*/
  /*  Workstation Defaults (private)                                        */
  /*========================================================================*/
  
  /**
   * Add the default station
   * 
   * @param isGroupKey
   * @param isStation
   * @throws DBException
   */
  private void addDefaultStation(String isGroupKey, String isStation)
      throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setGroup(GUI_CFG_PREFIX + isGroupKey);
    mpSysCfgData.setParameterName(DEFAULT_STATION);
    mpSysCfgData.setParameterValue(isStation);
    addElement(mpSysCfgData);
  }

  /**
   * Delete the default station
   * 
   * @param isGroupKey
   * @throws DBException
   */
  private void deleteDefaultStation(String isGroupKey) throws DBException
  {
    try
    {
      mpSysCfgData.clear();
      mpSysCfgData.setKey(SysConfigData.GROUP_NAME, GUI_CFG_PREFIX + isGroupKey);
      mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, DEFAULT_STATION);
      deleteElement(mpSysCfgData);
    }
    catch (NoSuchElementException nsee)
    {
      // This is okay
    }
  }
  
  /**
   * Does a default station exist for a given key?
   *  
   * @param isGroupKey
   * @return boolean
   * @throws DBException
   */
  public boolean existsDefaultStation(String isGroupKey) throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, GUI_CFG_PREFIX + isGroupKey);
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, DEFAULT_STATION);
    return exists(mpSysCfgData);
  }
  
  /**
   * Modify the default station
   * 
   * @param isGroupKey
   * @param isStation
   * @throws DBException
   */
  private void modifyDefaultStation(String isGroupKey, String isStation)
      throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, GUI_CFG_PREFIX + isGroupKey);
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, DEFAULT_STATION);
    mpSysCfgData.setParameterValue(isStation);
    modifyElement(mpSysCfgData);
  }

  /*========================================================================*/
  /*  MonitorFrame configurations                                           */
  /*========================================================================*/

  /**
   * Get the number of tabs per group on the MonitorFrame
   * 
   * @return
   * @throws DBException
   */
  public int getMonitorTabsPerGroup() throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, "MonitorFrame");
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, "TabsPerGroup");
    SysConfigData vpSCData = getElement(mpSysCfgData, DBConstants.NOWRITELOCK);
    if (vpSCData == null)
    {
      return DEFAULT_TABS_PER_GROUP;
    }
    else
    {
      return Integer.parseInt(vpSCData.getParameterValue());
    }
  }

  /**
   * Get the tab order of system-specified tabs
   * 
   * @return
   * @throws DBException
   */
  public String[] getMonitorTabOrder() throws DBException
  {
    mpSysCfgData.clear();
    mpSysCfgData.setKey(SysConfigData.GROUP_NAME, "MonitorFrame");
    mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, "TabOrder");
    SysConfigData vpSCData = getElement(mpSysCfgData, DBConstants.NOWRITELOCK);
    if (vpSCData == null)
    {
      return null;
    }
    else
    {
      StringTokenizer vpST = new StringTokenizer(vpSCData.getParameterValue(),",");
      String[] vasTabs = new String[vpST.countTokens()];
      for (int i = 0; i < vasTabs.length; i++)
      {
        vasTabs[i] = vpST.nextToken();
      }
      return vasTabs;
    }
  } 
  
  /**
   * Get the contents of system-specified tabs
   * 
   * @return
   * @throws DBException
   */
  public Map<String,String> getMonitorTabGroups() throws DBException
  {
    String[] vasTabs = getMonitorTabOrder();
    if (vasTabs == null)
    {
      return null;
    }
    
    Map<String,String> vpGroupMap = new TreeMap<String, String>();
    for (String s : vasTabs)
    {
      mpSysCfgData.clear();
      mpSysCfgData.setKey(SysConfigData.GROUP_NAME, "MonitorFrame");
      mpSysCfgData.setKey(SysConfigData.PARAMETERNAME_NAME, s);
      SysConfigData vpSCData = getElement(mpSysCfgData, DBConstants.NOWRITELOCK);
      if (vpSCData != null)
      {
        StringTokenizer vpST = new StringTokenizer(vpSCData.getParameterValue(),",");
        while (vpST.hasMoreElements())
        {
          vpGroupMap.put(vpST.nextToken(), s);
        }
      }
    }
    return vpGroupMap;
  }
  
  
  /*========================================================================*/
  /*  Last User                                                             */
  /*========================================================================*/
  /**
   * Store the last user for later retrieval
   * 
   * @param isMachine
   * @param isUserID
   * @throws DBException
   */
  public void memorizeLastLogin(String isMachine, String isUserID)
      throws DBException
  {
    SysConfigData vpSCData = Factory.create(SysConfigData.class);
    vpSCData.setKey(SysConfigData.GROUP_NAME, LAST_LOGIN);
    vpSCData.setKey(SysConfigData.PARAMETERNAME_NAME, isMachine);
    vpSCData = getElement(vpSCData, DBConstants.WRITELOCK);
    if (vpSCData == null)
    {
      vpSCData = Factory.create(SysConfigData.class);
      vpSCData.setGroup(LAST_LOGIN);
      vpSCData.setParameterName(isMachine);
      vpSCData.setParameterValue(isUserID);
      addElement(vpSCData);
    }
    else
    {
      vpSCData.setKey(SysConfigData.GROUP_NAME, SysConfig.LAST_LOGIN);
      vpSCData.setKey(SysConfigData.PARAMETERNAME_NAME, isMachine);
      vpSCData.setParameterValue(isUserID);
      modifyElement(vpSCData);
    }
  }
  
  /**
   * Remember the last login at this machine
   * 
   * @param isMachine
   * @return
   * @throws DBException
   */
  public String rememberLastLogin(String isMachine) throws DBException
  {
    SysConfigData vpSCData = Factory.create(SysConfigData.class);
    vpSCData.setKey(SysConfigData.GROUP_NAME, SysConfig.LAST_LOGIN);
    vpSCData.setKey(SysConfigData.PARAMETERNAME_NAME, isMachine);
    vpSCData = getElement(vpSCData, DBConstants.NOWRITELOCK);
    if (vpSCData != null)
    {
      return vpSCData.getParameterValue();
    }
    return "";
  }
}
