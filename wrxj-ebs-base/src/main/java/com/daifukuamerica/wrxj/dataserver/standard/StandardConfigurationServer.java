package com.daifukuamerica.wrxj.dataserver.standard;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.database.DatabaseControllerTypeDefinition;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfig;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccess;
import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccessData;
import com.daifukuamerica.wrxj.dbadapter.data.JVMConfig;
import com.daifukuamerica.wrxj.dbadapter.data.JVMConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.dbadapter.data.WrxSequencer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

/**
 * Baseline system configuration methods.
 *  
 * <p><b>Details:</b> This class comprises the baseline implementations of
 * configuration-<wbr>related methods.</p>
 * 
 * @author Sharky
 * @author A.D.
 */
public class StandardConfigurationServer extends StandardServer 
{
  private HostConfig     mpHostConfig;
  private SysConfig      mpSysConfig;
  private JVMConfig      mpJVMConfig;
  private Device         mpDevice;
  private HostOutAccess  mpHostOutAccess;
  private final HostConfigData mpHostConfigData = Factory.create(HostConfigData.class);
  private final JVMConfigData  mpJVMConfigData = Factory.create(JVMConfigData.class);
  private final DeviceData     mpDevData = Factory.create(DeviceData.class);

  public StandardConfigurationServer(String isKeyName)
  {
    super(isKeyName);
  }

  public StandardConfigurationServer()
  {
    this(null);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardConfigurationServer(String keyName, DBObject dbo)
  {
	  super(keyName, dbo);
  }

  /**
   * {@inheritDoc}
   */
  public String getHostConfigPropertyValue(String isName) throws DBException
  {
    initHostConfig();
    String vsValue = mpHostConfig.getPropertyValue(isName);
    return vsValue;
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getHostConfigPropertyNames() throws DBException
  {
    initHostConfig();
    Set<String> vpNames = mpHostConfig.getPropertyNames();
    return vpNames;
  }

  /**
   * {@inheritDoc}
   * @param ipConfigData reference to HostConfig Data object.  This object is
   *        expected to contain all data to be added to HostConfig record.
   * @throws DBException if there is a database access error.
   */
  public void addHostConfig(HostConfigData ipConfigData) throws DBException
  {
    initHostConfig();
    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      mpHostConfig.addElement(ipConfigData);
      commitTransaction(vpToken);
    }
    catch(DBException e)
    {
      logException(e, "Adding HostConfig data.");
    }
    finally
    {
      endTransaction(vpToken);
    }
  }

 /**
  * Method to get the active Transport Data handler.  <b>Note:</b> if the system
  * is configured incorrectly, there will be more than one active data transport
  * for a given host name.
  * @param isHostName the host name.
  * @return the Transport data handler
  * @throws DBException if there is a database access error.
  */  
  public String getTransportDataHandlerForHost(String isHostName)
         throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    
    mpHostConfigData.setKey(HostConfigData.GROUP_NAME, 
                            HostConfigData.HOST_TRANSPORT_GROUP_NAME);
    mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, "HostName");
    mpHostConfigData.setKey(HostConfigData.PARAMETERVALUE_NAME, isHostName);
    mpHostConfigData.setKey(HostConfigData.ACTIVECONFIG_NAME, DBConstants.YES);
    HostConfigData vpHCData = mpHostConfig.getElement(mpHostConfigData,
                                                      DBConstants.NOWRITELOCK);
    
    String vsRtn = "";
    if (vpHCData != null)
    {
      vsRtn = vpHCData.getDataHandler();
    }
    
    return(vsRtn);
  }
  
 /**
  * Updates the selected Host Data Type to the active configuration.
  * @param isDataType the data type that was selected.
  * @throws DBException if there is a database access or update error.
  */
  public void updateActiveHostDataType(String isDataType)
         throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();

    TransactionToken vpToken = null;
    try
    {
      int vnPassedInDataType = DBTrans.getIntegerValue(HostConfigData.DATA_FORMAT_TRAN_NAME, isDataType);
      
      vpToken = startTransaction();
      if (vnPassedInDataType == DBConstants.XML       ||
          vnPassedInDataType == DBConstants.DELIMITED ||
          vnPassedInDataType == DBConstants.FIXEDLENGTH)
      {
        mpHostConfigData.setKey(HostConfigData.ACTIVECONFIG_NAME, Integer.valueOf(DBConstants.YES));
        mpHostConfigData.setKey(HostConfigData.GROUP_NAME, HostConfigData.HOST_TRANSPORT_GROUP_NAME,
                                KeyObject.NOT_EQUAL);
        mpHostConfigData.setKey(HostConfigData.GROUP_NAME, HostConfigData.HOST_FORMATTER_GROUP_NAME,
                                KeyObject.NOT_EQUAL);
        mpHostConfigData.setKey(HostConfigData.GROUP_NAME, HostConfigData.HOST_CONTROLLER_GROUP_NAME,
                                KeyObject.NOT_EQUAL);
        mpHostConfigData.setActiveConfig(DBConstants.NO);
        mpHostConfig.modifyElement(mpHostConfigData);
      
                                       // Enable the selected data type.
        mpHostConfigData.clear();
        mpHostConfigData.setKey(HostConfigData.GROUP_NAME,
                                isDataType + HostConfigData.HOST_PARSER_GROUP_NAME);
        mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isDataType + "Formatter",
                                KeyObject.EQUALITY, KeyObject.OR);
        mpHostConfigData.setKey(HostConfigData.GROUP_NAME, "Host", KeyObject.EQUALITY, KeyObject.OR);
        mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, "DataType");
        mpHostConfigData.setKey(HostConfigData.PARAMETERVALUE_NAME, isDataType);
        mpHostConfigData.setKey(HostConfigData.GROUP_NAME, "Formatter",
                                KeyObject.EQUALITY, KeyObject.OR);
        mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, isDataType + "Formatter",
                                KeyObject.EQUALITY);
        

        mpHostConfigData.setActiveConfig(DBConstants.YES);
        mpHostConfig.modifyElement(mpHostConfigData);
        Application.setInt(HostConfigData.ACTIVE_DATA_TYPE, vnPassedInDataType);
      }
      else
      {
        throw new DBException("No Data Type configuration changes specified...");
      }
      commitTransaction(vpToken);
    }
    catch(NoSuchFieldException nsf)
    {
      throw new DBException("Error updating configuration...", nsf);
    }
    catch(NoSuchElementException nse)
    {
      throw new DBException("Error updating configuration...", nse);
    }
    finally
    {
      endTransaction(vpToken);
    }
  }
  
 /**
  * Updates the selected Host Comm. Type to the active configuration.
  * @param isDataType the data type that was selected.
  * @param isCommType the communication type that was selected.
  * @throws DBException if there is a database access or update error.
  */
  public void updateActiveHostCommType(String isCommType) throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();

    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      mpHostConfigData.setKey(HostConfigData.GROUP_NAME, HostConfigData.HOST_TRANSPORT_GROUP_NAME);
      mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, "CommType", KeyObject.EQUALITY,
                              KeyObject.OR);
      mpHostConfigData.setActiveConfig(DBConstants.NO);
      mpHostConfig.modifyElement(mpHostConfigData);
 
      if (isCommType.equals(HostConfigData.TCPIP_TRANSPORT) ||
          isCommType.equals(HostConfigData.JDBC_ORACLE_TRANSPORT) ||
          isCommType.equals(HostConfigData.JDBC_SQLSERVER_TRANSPORT) ||
          isCommType.equals(HostConfigData.JDBC_DB2_TRANSPORT))
      {
        mpHostConfigData.clear();
        mpHostConfigData.setKey(HostConfigData.GROUP_NAME, HostConfigData.HOST_TRANSPORT_GROUP_NAME);
        mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isCommType + "Host", KeyObject.LIKE);
        mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, "CommType", KeyObject.EQUALITY,
                                KeyObject.OR);
        mpHostConfigData.setKey(HostConfigData.PARAMETERVALUE_NAME, isCommType, KeyObject.LIKE);

        
        mpHostConfigData.setActiveConfig(DBConstants.YES);
        mpHostConfig.modifyElement(mpHostConfigData);
        int vnActiveTransport = getActiveTransportMethod();
        if (vnActiveTransport != -1)
          Application.setInt(HostConfigData.ACTIVE_TRANSPORT_TYPE, vnActiveTransport);
      }
      else
      {
        throw new DBException("No Communication Type configuration changes specified...");
      }
      commitTransaction(vpToken);
    }
    finally
    {
      endTransaction(vpToken);
    }
  }
  
  /**
   * {@inheritDoc} 
   * @param isDataHandler the data handler name. 
   * @param isGroup the group name for a configuration.
   * @param isParameterName the parameter name for a configuration(s).
   * @param isNewParameterValue the parameter's value to be modified.
   * @throws DBException if there is a database access error.
   */
  public void updateHostConfigValue(String isDataHandler, String isGroup,
                                    String isParameterName, String isNewParameterValue)
              throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    
    mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isDataHandler);
    mpHostConfigData.setKey(HostConfigData.GROUP_NAME, isGroup);
    mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, isParameterName);
    mpHostConfigData.setParameterValue(isNewParameterValue);
    
    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      mpHostConfig.modifyElement(mpHostConfigData);
      commitTransaction(vpToken);
    }
    catch(DBException e)
    {
      logException(e, "Modifying HostConfig data.");
      throw e;
    }
    finally
    {
      endTransaction(vpToken);
    }
  }
  
  /**
   * Method changes the host name in HOSTCONFIG, HOSTOUTACCESS, and WRXSEQUENCER
   * @param isDataHandler the data handler
   * @param isGroup the group name for a configuration.
   * @param isOldHostName the old host name
   * @param isNewHostName the new host name
   * @throws DBException
   */
  public void changeHostName(String isDataHandler, String isGroup, String isOldHostName, String isNewHostName)throws DBException
  {
    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      updateHostConfigValue(isDataHandler,isGroup, "HostName", isNewHostName);
      HostOutAccess vpHostOut = Factory.create(HostOutAccess.class);
      vpHostOut.changeHostName(isOldHostName, isNewHostName);
      WrxSequencer vpSequencer = Factory.create(WrxSequencer.class);
      vpSequencer.updateEndDevice(isOldHostName, isNewHostName);
      commitTransaction(vpToken);      
    }
    catch(DBException e)
    {
      logException(e, "Modifying Host Name");
      throw e;
    }
    finally
    {
      endTransaction(vpToken);
    }
  }
  
  /**
   * {@inheritDoc} 
   * @param isDataHandler the data handler name. 
   * @param isGroup the group name for a configuration.
   * @param isParameterName the parameter name for a configuration(s).
   * @throws DBException if there is a database access error.
   */
  public void deleteHostConfigValue(String isDataHandler, String isGroup,
                                    String isParameterName) throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    
    mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isDataHandler);
    mpHostConfigData.setKey(HostConfigData.GROUP_NAME, isGroup);
    mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, isParameterName);
    
    TransactionToken vpToken = null;
    try
    {
      vpToken = startTransaction();
      mpHostConfig.deleteElement(mpHostConfigData);
      commitTransaction(vpToken);
    }
    catch(DBException e)
    {
      logException(e, "Deleting HostConfig data.");
    }
    finally
    {
      endTransaction(vpToken);
    }
  }

  /**
   * {@inheritDoc} Method requires that all three arguments be filled in.
   * @param isDataHandler the data handler name. 
   * @param isGroup the group name for a configuration.
   * @param isParameterName the parameter name for a configuration(s).
   * @return fetches one host record based on unique search criteria. <code>null</code>
   *         is returned when the data can't be found.
   * @throws DBException if there is a database access error.
   */
  public HostConfigData getHostConfigRecord(String isDataHandler, String isGroup,
                                            String isParameterName) throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    if (isDataHandler == null || isGroup == null || isParameterName == null)
    {
      return(null);
    }
    mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isDataHandler);
    mpHostConfigData.setKey(HostConfigData.GROUP_NAME, isGroup);
    mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, isParameterName);
    
    return(mpHostConfig.getElement(mpHostConfigData, DBConstants.NOWRITELOCK));
  }

  /**
   * {@inheritDoc} Partial words may be passed for this method's arguments.  The
   * arguments should not contain the '%' character.
   * @param isDataHandler the data handler name.  A <code>null</code> may be
   *        passed for all/any data handler.
   * @param isGroup the group name for a configuration. A <code>null</code> may be
   *        passed for all/any group name.
   * @param isParameterName the parameter name for a configuration(s).  If all/any
   *        parameter value is acceptable a <code>null</code> may be passed.
   * @return a List of Maps containing data matching search criteria.  The columns
   * represented in the returned maps are:
   * <p> <ol>
   *       <li>sDataHandler </li>
   *       <li>sGroup </li>
   *       <li>sParameterName </li>
   *       <li>sParameterValue </li>
   *     </ol>
   * </p>
   * @throws DBException if there is a database access error.
   */
  public List<Map> getHostConfigList(String isDataHandler, String isGroup,
                                     String isParameterName) throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    if (isDataHandler != null && isDataHandler.trim().length() != 0)
    {
      mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isDataHandler + "%",
                              KeyObject.LIKE);
    }
    
    if (isGroup != null && isGroup.trim().length() != 0)
    {
      mpHostConfigData.setKey(HostConfigData.GROUP_NAME, isGroup + "%",
                              KeyObject.LIKE);
    }

    if (isParameterName != null && isParameterName.trim().length() != 0)
    {
      mpHostConfigData.setKey(HostConfigData.PARAMETERNAME_NAME, isParameterName + "%",
                              KeyObject.LIKE);
    }
    
    return(mpHostConfig.getAllElements(mpHostConfigData));
  }

 /**
  * Convenience method.
  * @param isGroup the group name for a configuration. A <code>null</code> may be
  *        passed for all/any group name.
  * @param inActive parameter to indicate if only Active configurations should be
  *        found.
  * @return a List of Maps containing data matching search criteria.  The columns
  * represented in the returned maps are:
  * <p> <ol>
  *       <li>sDataHandler </li>
  *       <li>sGroup </li>
  *       <li>sParameterName </li>
  *       <li>sParameterValue </li>
  *     </ol>
  * </p>
  * @throws DBException if there is a database access error.
  */
  public List<Map> getHostConfigList(String isGroup, int inActive) throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    if (isGroup != null && isGroup.trim().length() != 0)
    {
      mpHostConfigData.setWildcardKey(HostConfigData.GROUP_NAME, isGroup, true);
      mpHostConfigData.addOrderByColumn(HostConfigData.GROUP_NAME);
    }
    mpHostConfigData.setKey(HostConfigData.ACTIVECONFIG_NAME, Integer.valueOf(inActive));

    return(mpHostConfig.getAllElements(mpHostConfigData));
  }

  public List<Map> getHostConfigList(String isDataHandler, String isGroup, int inActive)
         throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    if (isGroup != null && isGroup.trim().length() != 0)
    {
      mpHostConfigData.setWildcardKey(HostConfigData.GROUP_NAME, isGroup, true);
      mpHostConfigData.addOrderByColumn(HostConfigData.GROUP_NAME);
    }
    mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isDataHandler);
    mpHostConfigData.setKey(HostConfigData.ACTIVECONFIG_NAME, Integer.valueOf(inActive));

    return(mpHostConfig.getAllElements(mpHostConfigData));
  }

 /**
  * Convenience method.
  * @param isGroup the group name for a configuration. A <code>null</code> may be
  *        passed for all/any group name.
  * @return a List of Maps containing data matching search criteria.  The columns
  * represented in the returned maps are:
  * <p> <ol>
  *       <li>sDataHandler </li>
  *       <li>sGroup </li>
  *       <li>sParameterName </li>
  *       <li>sParameterValue </li>
  *     </ol>
  * </p>
  * @throws DBException if there is a database access error.
  */
  public List<Map> getHostConfigList(String isGroup) throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    if (isGroup != null && isGroup.trim().length() != 0)
    {
      mpHostConfigData.setWildcardKey(HostConfigData.GROUP_NAME, isGroup, true);
      mpHostConfigData.addOrderByColumn(HostConfigData.GROUP_NAME);
    }
    
    return(mpHostConfig.getAllElements(mpHostConfigData));
  }
  
  /**
   * Gets a list of controller configurations, but makes sure the list contains
   * a specified protocol.
   * @param isProtocol The protocol the controller should be using.
   * @throws com.daifukuamerica.wrxj.jdbc.DBException if there is a database access error.
   * @return List of Host Controllers.
   */
  public List<Map> getControllerWithProtocol(String isProtocol)
         throws DBException
  {
    initHostConfig();
    return(mpHostConfig.getControllersWithInActiveTransporter(isProtocol));
  }
  
  public List<Map> getTransportDefinitions(String isGroup, String isCommType, int inActive)
         throws DBException
  {
    initHostConfig();
    mpHostConfigData.clear();
    mpHostConfigData.setKey(HostConfigData.GROUP_NAME, isGroup);
    mpHostConfigData.setKey(HostConfigData.DATAHANDLER_NAME, isCommType + "Host", KeyObject.LIKE);
    mpHostConfigData.addOrderByColumn(HostConfigData.DATAHANDLER_NAME);

    return(mpHostConfig.getAllElements(mpHostConfigData));
  }
  
 /**
  * Method gets currently active data format for the Host.
  * <pre>
  *    DBConstants.XML for XML formatted Host messages.
  *    DBConstants.DELIMITED for delimiter formatted data.
  *    DBConstants.FIXEDLENGTH for fixed length (space-padded)
  *    formatted data.
  * </pre>
  * @return integer representation of the data format being used for the Host
  *         Interface.
  * @throws DBException if there is a database access error.
  */
  public int getActiveDataFormat() throws DBException
  {
    initHostConfig();
    return(mpHostConfig.getDataFormat());
  }

 /**
  * Method to get the currently active data transport protocol.
  * This method will return one of three constants:
  *   <ul>
  *     <li> HostConfig.TCPIP      if the transport method is TCP/IP.</li>
  *     <li> HostConfig.JDBCORACLE if the transport method is Oracle JDBC.</li>
  *     <li> HostConfig.JDBCDB2    if the transport method is DB2 JDBC.</li>
  *     <li> -1                    if the transport method is unspecified.</li>
  *   </ul>
  * 
  * @return integer value specifying protocol.
  * @throws DBException if there is a database access error.
  */
  public int getActiveTransportMethod() throws DBException
  {
    initHostConfig();
    return(mpHostConfig.getActiveTransportMethod());
  }

  /*========================================================================*/
  /*  ControllerConfig Methods                                              */
  /*========================================================================*/
  
  /**
   * Get a Controller.PropertyName property value from the Controller
   * Configuration
   * 
   * @param isDottedPropertyName
   * @return
   * @throws DBException
   */
  public String getControllerConfigDottedPropertyValue(String isDottedPropertyName) throws DBException
  {
    int vnDot = isDottedPropertyName.indexOf('.');
    if (vnDot < 0)
      return null;
    String vsController = isDottedPropertyName.substring(0, vnDot);
    String vsPropertyName = isDottedPropertyName.substring(vnDot + 1);
    String vsPropertyValue = getControllerConfigPropertyValue(vsController, vsPropertyName);
    return vsPropertyValue;
  }

  /**
   * Get a property value from the Controller Configuration
   * 
   * @param isController
   * @param isPropertyName
   * @return
   * @throws DBException
   */
  public String getControllerConfigPropertyValue(String isController, String isPropertyName) throws DBException
  {
    ControllerConfig vpControllerConfig = Factory.create(ControllerConfig.class);
    String vsPropertyValue = vpControllerConfig.getPropertyValue(isController, isPropertyName);
    return vsPropertyValue;
  }

  /**
   * Get a list of Controller.PropertyName property names
   * 
   * @return
   * @throws DBException
   */
  public Set<String> getControllerConfigDottedPropertyNames() throws DBException
  {
    ControllerConfig vpControllerConfig = Factory.create(ControllerConfig.class);
    Set<String> vpNames = vpControllerConfig.getDottedPropertyNames();
    return vpNames;
  }
  
  /**
   * Add a ControllerConfig row
   * @param ipCCData
   * @throws DBException
   */
  public void add(ControllerConfigData ipCCData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(ControllerConfig.class).addElement(ipCCData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }
  
  /**
   * Modify a ControllerConfig row
   * @param ipCCData
   * @throws DBException
   */
  public void modify(ControllerConfigData ipCCData) throws DBException
  {
    TransactionToken tt = null;
    try
    {
      tt = startTransaction();
      Factory.create(ControllerConfig.class).modifyElement(ipCCData);
      commitTransaction(tt);
    }
    finally
    {
      endTransaction(tt);
    }
  }

  /*========================================================================*/
  /*  SysConfig Methods                                                     */
  /*========================================================================*/
  /**
   * Get a list of SysConfig.PropertyName property names
   *
   * @return a Set containing strings of the form:
   * <pre><code>sGroup.sParameterName</code></pre>
   * @throws DBException
   */
  public Set<String> getSysConfigDottedPropertyNames() throws DBException
  {
    SysConfigData vpSysData = Factory.create(SysConfigData.class);
    initializeSysConfig();
                                       // These are the selected Columns.
    vpSysData.setGroup(SysConfigData.GROUP_NAME);
    vpSysData.setParameterName(SysConfigData.PARAMETERNAME_NAME);
    List<Map> vpList = mpSysConfig.getSelectedColumnElements(vpSysData);

    Set<String> vpSet = new HashSet<String>(vpList.size());
    if (!vpList.isEmpty())
    {
      for(Map vpMap : vpList)
      {
        String vsGroup = DBHelper.getStringField(vpMap, SysConfigData.GROUP_NAME);
        String vsName = DBHelper.getStringField(vpMap, SysConfigData.PARAMETERNAME_NAME);
        vpSet.add(vsGroup + "." + vsName);
      }
    }

    return(vpSet);
  }


  /**
   * Get a Controller.PropertyName property value from the Controller
   * Configuration
   *
   * @param isDottedPropertyName a string of the form: <pre><b>sGroup.sParameterName</b></pre>
   * @return the property value
   * @throws DBException
   */
  public String getSysConfigDottedPropertyValue(String isDottedPropertyName) throws DBException
  {
    int vnDot = isDottedPropertyName.indexOf('.');
    if (vnDot < 0)
      return null;
    String vsController = isDottedPropertyName.substring(0, vnDot);
    String vsPropertyName = isDottedPropertyName.substring(vnDot + 1);

    return getSysConfigPropertyValue(vsController, vsPropertyName);
  }

  /**
   * Get a property value from the System Configuration table.
   *
   * @param isGroup id. in the sysconfig table.
   * @param isPropertyName the parametername in the sysconfig table.
   * @return property value
   * @throws DBException
   */
  public String getSysConfigPropertyValue(String isGroup, String isPropertyName)
         throws DBException
  {
    initializeSysConfig();
    return(mpSysConfig.getSysConfigValue(isGroup, isPropertyName));
  }

 /**
  * Method to return a name-value pair of all matched name patterns.
  * @param isNamePatternString partial of full name of name/value pair.
  * @return sorted Map of name-value pairs.
  */
  public Map<String, String> getCachedSysNameValuePairs(String isNamePatternString)
  {
    if (isNamePatternString.trim().length() == 0)
    {
      isNamePatternString = Application.SYSCFG_DOMAIN;
    }
    else
    {
      isNamePatternString = Application.SYSCFG_DOMAIN + isNamePatternString;
    }
    
    Set<String> vpNamesSet = Application.getPropertyNames(isNamePatternString);
    List<String> vpNamesList = new ArrayList<String>(vpNamesSet);
    TreeMap<String, String> vpNameValueMap = new TreeMap<String, String>();

    for (String vsFullName: vpNamesList)
    {
      String[] vasDottedNames = vsFullName.split("\\x2E");
      if (vasDottedNames != null && vasDottedNames.length > 2)
        vpNameValueMap.put(vasDottedNames[2], Application.getString(vsFullName));
    }

    return(vpNameValueMap);
  }

 /**
  * Convenience method to get a SysConfig value that has been used previously
  * and thereafter cached.  This method saves on DB hits.
  * @param isGroup the SysConfig group
  * @param isPropertyName the SysConfig Property Name
  * @param isDefault the default value to return if the isGroup + isPropertyName
  *        combination is not found.
  * @return String SysConfig property value
  */
  public String getCachedSysConfigString(String isGroup, String isPropertyName,
                                         String isDefault)
  {
    String vsKey = Application.SYSCFG_DOMAIN + isGroup + "." + isPropertyName;
    return(Application.getString(vsKey, isDefault));
  }

 /**
  * Convenience method to get a SysConfig value that has been used previously
  * and thereafter cached.  This method saves on DB hits.
  * @param isGroup the SysConfig group
  * @param isPropertyName the SysConfig Property Name
  * @param inDefault the default value to return if the isGroup + isPropertyName
  *        combination is not found.
  * @return integer SysConfig property value.
  */
  public int getCachedSysConfigInt(String isGroup, String isPropertyName, int inDefault)
  {
    String vsKey = Application.SYSCFG_DOMAIN + isGroup + "." + isPropertyName;
    return(Application.getInt(vsKey, inDefault));
  }

 /**
  * Convenience method to get a SysConfig value that has been used previously
  * and thereafter cached.  This method saves on DB hits.
  * @param isGroup the SysConfig group
  * @param isPropertyName the SysConfig Property Name
  * @param izDefault the default value to return if the isGroup + isPropertyName
  *        combination is not found.
  * @return boolean SysConfig property value
  */
  public boolean getCachedSysConfigBoolean(String isGroup, String isPropertyName,
                                           boolean izDefault)
  {
    String vsKey = Application.SYSCFG_DOMAIN + isGroup + "." + isPropertyName;
    return(Application.getBoolean(vsKey, izDefault));
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
    TransactionToken vpTT = null;
    try
    {
      vpTT = startTransaction();
      
      initializeSysConfig();
      mpSysConfig.setDefaultStation(isMachineName, isIPAddress, isStation);
      
      commitTransaction(vpTT);
    }
    finally
    {
      endTransaction(vpTT);
    }
  }

  /*========================================================================*/
  /*  JVMConfig Methods                                                     */
  /*========================================================================*/
 /**
  * Method to get list of JVM configurations.
  * @return List&lt;Map&gt; of JVM records.
  * @throws DBException if there is a DB access error.
  */
  public List<Map> getJVMList() throws DBException
  {
    initJVMConfig();
    mpJVMConfigData.clear();
    return(mpJVMConfig.getAllElements(mpJVMConfigData));
  }

 /**
  * Method to get the current split systems configuration.
  * @param isWarehouse The logical warehouse that will have different JVMs running.
  * @return List&lt;Map&gt; of configuration records for a split system.  Each
  * Map has the following columns and their values:<p><pre>
  * sJVMIdentifier
  * sJMSTopic
  * sDeviceID
  * sServerName
  * sSchedulerName
  * sAllocatorName
  * @throws DBException if there is a database access error.
  */
  public List<Map> getCurrentSplitSystemConfig(String isWarehouse) throws DBException
  {
    TableJoin vpTJ = Factory.create(TableJoin.class);
    return(vpTJ.getCurrentSplitSystemConfig(isWarehouse));
  }

  public String[] getJVMIDChoices() throws DBException
  {
    initJVMConfig();
    mpJVMConfigData.clear();
    String[] vasJVMIds = mpJVMConfig.getSingleColumnValues(JVMConfigData.JVMIDENTIFIER_NAME,
                                                           true, mpJVMConfigData,
                                                           SKDCConstants.NO_PREPENDER);
    return(vasJVMIds);
  }

 /**
  * Method to get the count of JVM's assigned to a warehouse.
  * @param isWarehouse the warehouse (may be super warehouse).
  * @return count of the number of JVMs in the given warehouse
  * @throws DBException if there is a database error.
  */
  public int getJVMCountPerWarehouse(String isWarehouse) throws DBException
  {
    TableJoin vpTJ = Factory.create(TableJoin.class);
    return(vpTJ.getJVMCountPerWarehouse(isWarehouse));
  }
  
 /**
  * Method to get a JVM record by JVM ID. without a record lock.
  * @param isJVMId the JVM Identifier.
  * @return returns a JVM record if found or <code>null</code> otherwise.
  * @throws DBException if there is a DB access error.
  */
  public JVMConfigData getJVMRecord(String isJVMId) throws DBException
  {
    initJVMConfig();
    mpJVMConfigData.clear();
    mpJVMConfigData.setKey(JVMConfigData.JVMIDENTIFIER_NAME, isJVMId);

    return(mpJVMConfig.getElement(mpJVMConfigData, DBConstants.NOWRITELOCK));
  }

  /**
   * Method to get the primary JVM's identifier.
   * @return the primary jvm id. Empty string if no primary JVM exists.  This
   * last condition is really a configuration error.  If multi-system configuration
   * is performed using the provided GUI this configuration error will not
   * occur.
   * @throws DBException if there is a database access error.
   */
  public String getPrimaryJVMIdentifier() throws DBException
  {
    initJVMConfig();
    mpJVMConfigData.clear();
    mpJVMConfigData.setKey(JVMConfigData.JVMTYPE_NAME,
      Integer.valueOf(DBConstants.PRIMARY_JVM));
    String[] vasJVMIds = mpJVMConfig.getSingleColumnValues(JVMConfigData.JVMIDENTIFIER_NAME,
                                                           true, mpJVMConfigData,
                                                           SKDCConstants.NO_PREPENDER);
    if (vasJVMIds.length > 1)
    {
      throw new DBException("More than one Primary JVM present! " +
                            "Invalid configuration found.");
    }
    
    return((vasJVMIds.length == 1) ? vasJVMIds[0] : "");
  }

 /**
  * Method to create a split system configuration.
  * @param isSuperWarehouse the super warehouse where system is being split.
  * @param ipConfigTemplate template of the configuration to create.
  * @throws DBException if there is a database error.
  */
  public void createSplitSystemConfig(String isSuperWarehouse, List<Map> ipConfigTemplate)
         throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
                                       // Completely remove any old configuration
                                       // for this super warehouse before beginning.
      removeSplitSystemConfig(isSuperWarehouse);
      for(Map vpMap : ipConfigTemplate)
      {
                                         // JVM Config.-Specific fields.
        String vsJVMID = DBHelper.getStringField(vpMap, DeviceData.JVMIDENTIFIER_NAME);
        String vsJMSTopic = DBHelper.getStringField(vpMap, JVMConfigData.JMSTOPIC_NAME);
        String vsServerName = DBHelper.getStringField(vpMap, JVMConfigData.SERVERNAME_NAME);
        if (!mpJVMConfig.exists(vsJVMID))
        {
          mpJVMConfigData.clear();
          mpJVMConfigData.setJVMIdentifier(vsJVMID);
          mpJVMConfigData.setJMSTopic(vsJMSTopic);
          mpJVMConfigData.setServerName(vsServerName);
          mpJVMConfig.addElement(mpJVMConfigData);
        }
                                         // Device-Specific fields.
        String vsDeviceID = DBHelper.getStringField(vpMap, DeviceData.DEVICEID_NAME);
        String vsSchedName = DBHelper.getStringField(vpMap, DeviceData.SCHEDULERNAME_NAME);
        String vsAllocName = DBHelper.getStringField(vpMap, DeviceData.ALLOCATORNAME_NAME);
        mpDevData.clear();
        mpDevData.setKey(DeviceData.DEVICEID_NAME, vsDeviceID);
        mpDevData.setSchedulerName(vsSchedName);
        mpDevData.setAllocatorName(vsAllocName);
        mpDevData.setJVMIdentifier(vsJVMID);
        mpDevice.modifyElement(mpDevData);
      }
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

 /**
  * Method to remove the split system configuration and make it into one JVM. as
  * is normal with most systems.
  * @param isSuperWarehouse the super warehouse under which all split system
  * configuration must be removed.  If this is set to SKDCConstants.ALL_
  * @throws DBException if there is a database error, or if the split system
  *         configuration couldn't be completely removed.
  */
  public void removeSplitSystemConfig(String isSuperWarehouse) throws DBException
  {
    String[] vasJVMIDs;
    if (isSuperWarehouse.equals(SKDCConstants.ALL_STRING))
    {
      vasJVMIDs = mpJVMConfig.getDistinctColumnValues(JVMConfigData.JVMIDENTIFIER_NAME,
                                                      SKDCConstants.NO_PREPENDER);
    }
    else
    {
      vasJVMIDs = mpJVMConfig.getJVMIDsPerSuperWarehouse(isSuperWarehouse);
    }

    boolean vzSplitSysFullyRemoved;
    TransactionToken vpTok = null;
    try
    {
      initDevice();
      initJVMConfig();
      vpTok = startTransaction();

      for(int vnIdx = 0; vnIdx < vasJVMIDs.length; vnIdx++)
      {
        String vsJVMId = vasJVMIDs[vnIdx];
        mpDevData.clear();
        mpDevData.setKey(DeviceData.JVMIDENTIFIER_NAME, vsJVMId);
        if (mpDevice.exists(mpDevData))
        {
          mpDevData.setJVMIdentifier(null);
                                       // Default scheduler and Allocator Names.
          mpDevData.setAllocatorName(DatabaseControllerTypeDefinition.ALLOCATOR_TYPE + "-1");
          mpDevData.setSchedulerName(DatabaseControllerTypeDefinition.SCHEDULER_TYPE + "-" +
                                    (vnIdx + 1));
          mpDevice.modifyElement(mpDevData);
        }

                                       // Remove the JVM record too.
        mpJVMConfigData.clear();
        mpJVMConfigData.setKey(JVMConfigData.JVMIDENTIFIER_NAME, vsJVMId);
        if (mpJVMConfig.exists(vsJVMId))
          mpJVMConfig.deleteElement(mpJVMConfigData);
      }
      commitTransaction(vpTok);
      vzSplitSysFullyRemoved = true;
    }
    catch(DBException exc)
    {
      vzSplitSysFullyRemoved = false;
      logError("Error removing split system! " + exc.getMessage());
    }
    finally
    {
      endTransaction(vpTok);
    }

    if (!vzSplitSysFullyRemoved)
    {
      throw new DBException("Warning! Split system not removed.  " +
                            "Please check Error log.");
    }
  }

 /**
  * Method to determine if this system is part of a split system.
  * @return <code>true</code> if this jvm is part of a split system.
  * @throws DBException if there is a database access error.
  */
  public boolean isSplitSystem() throws DBException
  {
    initJVMConfig();
    return(mpJVMConfig.isAnyJVMConfigured());
  }

 /**
  * Method to determine if this jvm is the primary one in a split system.
  * @return <code>true</code> if this jvm is the primary one in a split system.
  * @throws DBException if there is a database access error.
  */
  public boolean isThisPrimaryJVM() throws DBException
  {
    initJVMConfig();
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
    return(mpJVMConfig.isPrimaryJVM(vsJVMId));
  }

 /**
  * Method checks if the passed in JVM ID. is
  * @param isJVMId the JVM Identifier.
  * @return <code>true</code> if this is the primary JVM, <code>false</code>
  * otherwise.
  * @throws DBException if there is a database operation error.
  */
  public boolean isPrimaryJVM(String isJVMId) throws DBException
  {
    initJVMConfig();
    return(mpJVMConfig.isPrimaryJVM(isJVMId));
  }

  public boolean isAnyDeviceConfiguredForThisJVM() throws DBException
  {
    initDevice();
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
    return(mpDevice.isAnyDeviceJVMEnabled(vsJVMId));
  }

 /**
  * Method to reserve JVM.
  * @return record of the JVM id. that was reserved. <code>null</code> if
  *         nothing could be reserved.
  * @throws DBException for DB access errors.
  */
  public JVMConfigData reserveJVM() throws DBException
  {
    initJVMConfig();
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
    
    return(mpJVMConfig.reserveJVM(vsJVMId));
  }

 /**
  *  Method unreserves a JVM record so that it can be assigned to another app.
  *  instance.
  */
  public void unreserveJVM()
  {
    initJVMConfig();
    String vsJVMID = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
    TransactionToken vpTok = null;
    try
    {
      if (mpJVMConfig.isAnyJVMConfigured())
      {
        vpTok = startTransaction();
        mpJVMConfig.unreserveJVM(vsJVMID);
        commitTransaction(vpTok);
      }
    }
    catch(Exception nse)
    {
      logError("Error unreserving JVM " + vsJVMID + ". " + nse.getMessage());
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
  
  /**
   * Method to modify a sys config row.
   *
   * @param isGroup
   * @param isParameterName
   * @param isParameterValue
   * @throws DBException
   */
  public void modifySysConfig(String isGroup, String isParameterName, String isParameterValue)
         throws DBException
  {
    
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
      initializeSysConfig();
      mpSysConfig.updateSysConfigValue(isGroup, isParameterName, isParameterValue);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }

  /*========================================================================*/
  /*  HostOutAccess Methods                                                 */
  /*========================================================================*/
  /**
   *  Gets a list of order host out access data based on keys passed in inHOAData.
   *  @param ipHOAData <code>HostOutAccessData</code> object containing search
   *         information.
   *  @return <code>List</code> of host out access data.
   *  @exception DBException when DB exception is detected
   */
  public List<Map> getHostOutAccessData(HostOutAccessData ipHOAData)
      throws DBException
  {
    initHostOutAccess();
    return mpHostOutAccess.getHostOutAccessData(ipHOAData);
  }

  /**
   *  Method retrieves a host out access record using host name and message ID as keys.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *  @param withLock <code>int</code> flag indicating if record should be locked.
   *
   *  @return <code>HostOutAccessData</code> object. <code>null</code> if no record found.
   *  @exception DBException when DB exception is detected
   */
  public HostOutAccessData getHostOutAccessData(String isHostName, String isMsgId, int withLock)
      throws DBException
  {
    initHostOutAccess();
    return mpHostOutAccess.getHostOutAccessData(isHostName, isMsgId, withLock);
  }

  /**
   *  Method adds host out access record.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *  @param inEnabled <code>int</code> containing the enabled flag.
   *
   *  @exception DBException when DB exception is detected
   */
  public void addHostOutAccess(String isHostName, String isMsgId, int inEnabled)
      throws DBException
  {
    initHostOutAccess();
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
      mpHostOutAccess.addHostOutAccess(isHostName, isMsgId, inEnabled);
      commitTransaction(ttok);
    }
    catch (DBException e)
    {
      logException(e, "Inside StandardHostServer-->addHostOutAccess");
      throw e;
    }
    finally
    {
      endTransaction(ttok);
    }
  }

  /**
   *  Method deletes a host out access record using host name and message ID as keys.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *
   *  @exception DBException when DB exception is detected
   */
  public void deleteHostOutAccess(String isHostName, String isMsgId)
      throws DBException
  {
    initHostOutAccess();
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();

      mpHostOutAccess.deleteHostOutAccess(isHostName, isMsgId);
      commitTransaction(ttok);
    }
    catch (DBException e)
    {
      logException(e, "Inside StandardHostServer-->deleteHostOutAccess");
      throw e;
    }
    finally
    {
      endTransaction(ttok);
    }
  }

  /**
   *  Method modifies the enabled field of a given host name and message ID.
   *
   *  @param isHostName <code>String</code> containing the host name.
   *  @param isMsgId <code>String</code> containing the message ID.
   *  @param inEnabled <code>int</code> containing the enabled flag.
   *
   *  @exception DBException when DB exception is detected
   */
  public void modifyEnabledFlag(String isHostName, String isMsgId, int inEnabled)
      throws DBException
  {
    initHostOutAccess();
    TransactionToken ttok = null;
    try
    {
      ttok = startTransaction();
      mpHostOutAccess.modifyEnabledFlag(isHostName, isMsgId, inEnabled);
      commitTransaction(ttok);
    }
    catch (DBException e)
    {
      logException(e, "Inside StandardHostServer-->modifyEnabledFlag");
      throw e;
    }
    finally
    {
      endTransaction(ttok);
    }
  }

  /*========================================================================*/
  /*  Class Field Initialization Methods                                    */
  /*========================================================================*/
  private void initDevice()
  {
    if (mpDevice == null)
      mpDevice = Factory.create(Device.class);
  }

  private void initJVMConfig()
  {
    if (mpJVMConfig == null)
      mpJVMConfig = Factory.create(JVMConfig.class);
  }

  private void initHostConfig()
  {
    if (mpHostConfig == null)
      mpHostConfig = Factory.create(HostConfig.class);
  }

  private void initializeSysConfig()
  {
    if (mpSysConfig == null)
    {
      mpSysConfig = Factory.create(SysConfig.class);
    }
  }

  private void initHostOutAccess()
  {
    if (mpHostOutAccess == null)
      mpHostOutAccess = Factory.create(HostOutAccess.class);
  }

}

