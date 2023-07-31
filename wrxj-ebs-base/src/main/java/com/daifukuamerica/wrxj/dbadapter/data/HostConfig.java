package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:<BR>
 *   Class for handling HostConfig table interactions.
 *
 * @author       A.D.
 * @version      1.0     25-Jul-06
 */
public class HostConfig extends BaseDBInterface
{
  public static final int JDBCORACLE  = 1;
  public static final int TCPIP = 2;
  public static final int JDBCDB2 = 3;
  public static final int JDBCSQLSERVER = 4;

  protected HostConfigData mpHCData;

  public HostConfig()
  {
    super("HostConfig");
    mpHCData = Factory.create(HostConfigData.class);
  }

  /**
   * Fetches a list of Controller names associated with the Host interface.
   * @return String array of Host Controller Names.
   * @throws DBException if there is a database access error.
   */
  public String[] getControllerNames() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT DISTINCT sDataHandler FROM HostConfig WHERE ")
             .append("sGroup = 'Controller' AND ")
             .append("sParameterName = 'ControllerType' AND ")
             .append("iActiveConfig = ").append(DBConstants.YES).append(" ");

    return(getList(vpSql.toString(), HostConfigData.DATAHANDLER_NAME,
                   SKDCConstants.NO_PREPENDER));
  }

  /**
   *  Gets a list of all inbound messages defined in the system.
   *  @return String array of message names.
   *  @throws DBException for database access errors.
   */
  public String[] getInBoundMessageNames() throws DBException
  {
    String vsSubQuery = "(SELECT hc2.sParameterValue FROM HostConfig hc2 WHERE " +
                        "hc2.sGroup = \'Host\' AND hc2.sParameterName = \'DataType\' AND " +
                        "iActiveConfig = " + DBConstants.YES + ")";
    StringBuilder vpSql = new StringBuilder("SELECT hc1.sParameterName FROM HostConfig hc1 WHERE ")
             .append("hc1.sGroup = CONCAT(").append(vsSubQuery).append(", 'Parser')");

    return(getList(vpSql.toString(), HostConfigData.PARAMETERNAME_NAME,
                   SKDCConstants.NO_PREPENDER));
  }
  public boolean isOutboundMsgActive(String isMessageName) throws DBException
  {
    mpHCData.clear();
    mpHCData.setKey(HostConfigData.GROUP_NAME, "OutboundMessage");
    mpHCData.setKey(HostConfigData.PARAMETERNAME_NAME, isMessageName);
    String[] vasActiveVal = getSingleColumnValues(HostConfigData.ACTIVECONFIG_NAME, true,
            mpHCData, SKDCConstants.NO_PREPENDER);

    boolean vzRtn = false;
    if (vasActiveVal.length > 0)
    {     // There really should be just one value.
      vzRtn = (Integer.valueOf(vasActiveVal[0]) == DBConstants.YES);
    }

    return(vzRtn);
  }

  /**
   * Gets the names of all defined outbound messages names whether they are Active or not.
   * @return  array of defined message names.
   */
  public String[] getAllDefinedOutboundMesgNames()
  {
    return MessageOutNames.getNames();
  }

 /**
  *  Gets a list of all active outbound messages defined in the system.
  *  @return String array of message names.
  *  @throws DBException for database access errors.
  */
  public String[] getOutBoundMessageNames() throws DBException
  {
    String vsColumnNameSQL;
    if (getDataFormat() == DBConstants.XML)
      vsColumnNameSQL = "CONCAT(sParameterName, \'Message\') AS SPARAMETERNAME";
    else
      vsColumnNameSQL = "sParameterName";

    StringBuilder vpSql = new StringBuilder("SELECT ").append(vsColumnNameSQL)
               .append(" FROM HostConfig WHERE ")
               .append("iActiveConfig = ").append(DBConstants.YES).append(" AND ")
               .append("sGroup = 'OutboundMessage'");


    return(getList(vpSql.toString(), HostConfigData.PARAMETERNAME_NAME,
                   SKDCConstants.NO_PREPENDER));
  }

 /**
  * Method gets a Parser class name for a given Controller or Data Handler, and
  * message.
  * @param isDataHandler the Controller Name.
  * @param isMessageIdentifier The message name.
  * @return String containing package and class name.  An empty string is returned
  *         if no matching data is found.
  * @throws DBException if there is a database access error.
  */
  public String getParserClassName(String isDataHandler, String isMessageIdentifier)
         throws DBException
  {
    String vsSubQuery = "(SELECT hc2.sParameterValue FROM HostConfig hc2 WHERE " +
                        "hc2.sParameterName = \'DataType\' AND " +
                        "hc2.sDataHandler = \'" + isDataHandler + "\' AND " +
                        "hc2.iActiveConfig = " + DBConstants.YES + ")";
    StringBuilder vpSql = new StringBuilder("SELECT sParameterValue FROM HostConfig WHERE ")
               .append("sGroup = CONCAT(").append(vsSubQuery).append(", 'Parser') AND ")
               .append("sParameterName = '").append(isMessageIdentifier).append("'");

    return(getStringColumn(HostConfigData.PARAMETERVALUE_NAME, vpSql.toString()));
  }
  
  /**
   * Method gets a Processor class name for a given Controller or Data Handler, and
   * message.
   * @param dataHandlerName the Controller Name.
   * @param isMessageIdentifier The message name.
   * @return String containing package and class name.  An empty string is returned
   *         if no matching data is found.
   * @throws DBException if there is a database access error.
   */
   public String getProcessorClassName(String dataHandlerName, String processorName)
          throws DBException
   {
     StringBuilder vpSql = new StringBuilder("SELECT sParameterValue FROM HostConfig WHERE ")
                .append("sDataHandler = \'").append(dataHandlerName).append("\' AND ")
                .append("sGroup = \'Processor\' AND ")
                .append("sParameterName = '").append(processorName).append("' AND ")
                .append("iActiveConfig = ").append(DBConstants.YES).append(" ");
     
     return(getStringColumn(HostConfigData.PARAMETERVALUE_NAME, vpSql.toString()));
   }

 /**
  * Method gets a Formatter class name for a given Controller or Data Handler, and
  * message.  <i>If no class name can be found, an empty string is returned.</i>
  * @return String containing package and class name.
  * @throws DBException if there is a database access error.
  */
  public String getFormatterClassName() throws DBException
  {
    // FIXME Hmm hardcoded controller name
    // This query is used only in MessageFormatter for populating required formatter which extends MessageOutt
    String vsSubQuery = "(SELECT hc2.sParameterValue FROM HostConfig hc2 WHERE " +
                        "hc2.sParameterName = \'DataType\' AND " +
                        "hc2.sDataHandler = \'HostIntegrator\' AND " +
                        "hc2.iActiveConfig = " + DBConstants.YES + ")";
    StringBuilder vpSql = new StringBuilder("SELECT sParameterValue FROM HostConfig WHERE ")
               .append("sGroup = 'Formatter' AND ")
               .append("sParameterName = CONCAT(").append(vsSubQuery).append(", 'Formatter')");
    return(getStringColumn(HostConfigData.PARAMETERVALUE_NAME, vpSql.toString()));
  }

 /**
  * Method gets a class name for an outbound message.
  * message.  <i>If no class name can be found, an empty string is returned.</i>
  * @param isMessageName String containing the message name.
  * @return String containing package and class name.
  * @throws DBException if there is a database access error.
  */
  public String getMessageOutClassName(String isMessageName) throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sParameterValue FROM HostConfig WHERE ")
               .append("sGroup = 'OutboundMessage' AND ")
               .append("sParameterName = '").append(isMessageName).append("'");
    return(getStringColumn(HostConfigData.PARAMETERVALUE_NAME, vpSql.toString()));
  }

 /**
  * Method gets the effective communication protocol.  The assumption made here
  * is that there is only one protocol available at a time regardless of how many
  * HostControllers are configured.  This may be changed in the future depending on
  * customer needs.
  * This method will return one of three constants:
  *   <ul>
  *     <li> HostConfig.TCPIP      if the protocol is TCP/IP.</li>
  *     <li> HostConfig.JDBCORACLE if the protocol is Oracle JDBC.</li>
  *     <li> HostConfig.JDBCDB2    if the protocol is DB2 JDBC.</li>
  *     <li> -1                    if the protocol is unspecified.</li>
  *   </ul>
  *
  * @return integer value specifying protocol.
  * @throws DBException if there is a database access error.
  */
  public int getActiveTransportMethod() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sParameterValue FROM HostConfig WHERE ")
               .append("sParameterName = 'CommType' AND ")
               .append("iActiveConfig = ").append(DBConstants.YES);
    String vsProtocol = getStringColumn(HostConfigData.PARAMETERVALUE_NAME, vpSql.toString());

    return((vsProtocol.startsWith(HostConfigData.JDBC_ORACLE_TRANSPORT)) ? JDBCORACLE :
           (vsProtocol.startsWith(HostConfigData.JDBC_SQLSERVER_TRANSPORT)) ? JDBCSQLSERVER :
           (vsProtocol.startsWith(HostConfigData.TCPIP_TRANSPORT)) ? TCPIP :
           (vsProtocol.startsWith(HostConfigData.JDBC_DB2_TRANSPORT)) ? JDBCDB2 :  -1 );
  }

 /**
  * Method gets the effective data format for all host messaging.  The return
  * value will be an integer value of:
  * <pre>
  *    DBConstants.XML for XML formatted Host messages.
  *    DBConstants.DELIMITED for delimiter formatted data.
  *    DBConstants.FIXEDLENGTH for fixed length (space-padded)
  *    formatted data.
  * </pre>
  * @return integer representation of the data format being used for the Host
  *         Interface. -1 if no active data format is specified.
  * @throws DBException if there is a database access error.
  * @see HostConfigData
  */
  public int getDataFormat() throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sParameterValue FROM HostConfig WHERE ")
               .append("sParameterName = 'DataType' AND ")
               .append("sGroup = 'Host' AND ")
               .append("iActiveConfig = ").append(DBConstants.YES);
    String vsFormatType = getStringColumn(HostConfigData.PARAMETERVALUE_NAME, vpSql.toString());

    int vnTran;

    if (vsFormatType.isEmpty())
    {
      vnTran = -1;
    }
    else
    {
      try
      {
        vnTran = DBTrans.getIntegerValue(HostConfigData.DATA_FORMAT_TRAN_NAME, vsFormatType);
      }
      catch(NoSuchFieldException e)
      {
        throw new DBException("Unknown data format for Host message...", e);
      }
    }

    return(vnTran);
  }

 /**
  * Method gets all configurations for a given controller.  This method returns
  * a Map with entries which have as a key the name of the property, and value which
  * is the value of the property.   So for example a returned map for a
  * HostController would have the following data:<pre>
  *         Map.entry&lt;"Collaborator", "HostMessageIntegrator"&gt;
  *         Map.entry&lt;"CommType", "JDBCHost-1"&gt;
  *         Map.entry&lt;"ControllerType", "HostController-1"&gt;
  *                            .
  *                            .
  *                            .</pre>
  * @param isControllerName the controller name for which configurations are being
  *        retrieved.
  * @return A Map of parameter names and values for the controller.
  * @throws DBException if there is a database access error.
  */
  public Map<String, String> getControllerConfigurations(String isControllerName)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT sDataHandler, sParameterName, sParameterValue FROM HostConfig WHERE ")
             .append("sGroup = 'Controller' AND ")
             .append("sDataHandler = '").append(isControllerName).append("' AND ")
             .append("iActiveConfig = ").append(DBConstants.YES);
    List<Map> vpControllerCfg = fetchRecords(vpSql.toString());

    HashMap<String, String> vpNewMap = new HashMap<String, String>();
    for(Map vpMap : vpControllerCfg)
    {
      String vsKey = DBHelper.getStringField(vpMap, HostConfigData.PARAMETERNAME_NAME);
      String vsValue = DBHelper.getStringField(vpMap, HostConfigData.PARAMETERVALUE_NAME);
      if (!vpNewMap.containsKey(vsKey))
        vpNewMap.put(vsKey, vsValue);
    }

    return(vpNewMap);
  }

 /**
  * Method gets all configurations for a given controller.  This method returns
  * a Map with entries which have as a key the name of the property, and value which
  * is the value of the property.  If no matching data exists a <code>null</code>
  * value is returned.
  * @param isControllerName the controller name for which data trasport info.
  *        is being retrieved.
  * @param inActive indicates if the lookup is for active transports.
  * @return A list of transporter types.
  * @throws DBException if there is a database access error.
  */
  public List<Map> getTransportDefinitions(String isControllerName, int inActive)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT hc1.sDataHandler, hc1.sParameterName, hc1.sParameterValue ")
             .append("FROM hostconfig hc1, hostconfig hc2 WHERE ")
             .append("hc1.sGroup = 'Transport' AND ")
             .append("hc1.sDataHandler = hc2.sParameterValue AND ")
             .append("hc2.sDataHandler = '").append(isControllerName).append("'");

    return fetchRecords(vpSql.toString());
  }

 /**
  *  Method finds all available transports for a Host Interface.
  *  @return <code>String[]</code> of Active transport names. An empty array
  *          if no matches are found.
  */
  public String[] getActiveTransportChoices() throws DBException
  {
    StringBuilder vpSQL = new StringBuilder();
    vpSQL.append("SELECT DISTINCT sDataHandler FROM HostConfig WHERE ")
         .append("sGroup = '").append(HostConfigData.HOST_TRANSPORT_GROUP_NAME).append("' AND ")
         .append("iActiveConfig = ").append(DBConstants.YES).append(" ")
         .append("ORDER BY sDataHandler");
    return(getList(vpSQL.toString(), HostConfigData.DATAHANDLER_NAME,
                   SKDCConstants.NO_PREPENDER));
  }

  public List<Map> getControllersWithInActiveTransporter(String isCommType)
         throws DBException
  {
    StringBuilder vpSql = new StringBuilder("SELECT * FROM HostConfig hc1 WHERE ")
             .append("hc1.sGroup = 'Controller' AND ")
             .append("hc1.sParameterName != 'CommType' UNION ")
             .append("SELECT * FROM HostConfig hc2 WHERE ")
             .append("hc2.sGroup = 'Controller' AND ")
             .append("hc2.sParameterName = 'CommType' AND ")
             .append("hc2.sParameterValue LIKE '").append(isCommType).append("%'");

    return fetchRecords(vpSql.toString());
  }

 /**
  *  Sets Objects for garbage collection.
  */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
    mpHCData    = null;
  }

  /**
   * Retrieves property value.
   *
   * <p><b>Details:</b> This method interprets the table as if it were an
   * ordinary (name, value) map and looks up the value of the named property.
   * To perform the query, the name is first scanned for a dot ('.') character.
   * If one is found, everything before the dot is taken as the
   * {@code sDataHandler} field, and everything after the dot is taken as
   * the {@code sParameterName} field.  If a matching row exists, the
   * {@code sParameterValue} field is returned.  If the name does not include a
   * dot, or if a matching row cannot be found, this method returns
   * <code>null</code>.</p>
   *
   * @param isName the property name
   * @return the property value
   * @throws DBException if a database error occurs
   */
  public String getPropertyValue(String isName) throws DBException
  {
    int vnDot = isName.indexOf('.');
    if (vnDot < 0) return null;
    String vsDataHandler = isName.substring(0, vnDot);
    String vsParameterName = isName.substring(vnDot + 1);

    StringBuilder vpSql = new StringBuilder("SELECT ").append(HostConfigData.PARAMETERVALUE_NAME).append(" ")
               .append("FROM HostConfig WHERE ")
               .append("sDataHandler = '").append(vsDataHandler).append("' AND ")
               .append("sParameterName = '").append(vsParameterName).append("' AND ")
               .append("iActiveConfig = ").append(DBConstants.YES);

    List<Map> dataList = fetchRecords(vpSql.toString());
    if (dataList.isEmpty())
      return null;

    return DBHelper.getStringField(dataList.get(0),
        HostConfigData.PARAMETERVALUE_NAME);
  }

  /**
   * Returns property names.
   *
   * <p><b>Details:</b> This method interprets the table as if it were an
   * ordinary (name, value) map and looks up the names of all properties
   * represented in the table.  Names are formed by concatenating
   * {@code sDataHandler} fields with {@code sParameterName} fields, connecting
   * them with a dot.</p>
   *
   * @return the names
   * @throws DBException if a database error occurs
   */
  public Set<String> getPropertyNames() throws DBException
  {
    final String SNAME = "sName";
    String vsConcatColumn = "CONCAT(sDataHandler, CONCAT(\'.\', sParameterName))";

    StringBuilder vpSql = new StringBuilder("SELECT ").append(vsConcatColumn).append(" AS ").append(SNAME)
               .append(" FROM HostConfig WHERE ")
               .append("iActiveConfig = ").append(DBConstants.YES);
    List<Map> vpRecords = fetchRecords(vpSql.toString());

    int vnSize = vpRecords.size();
    Set<String> vpSet = new HashSet<String>(vnSize);
    for (Map vpMap: vpRecords)
    {
      String vsName = DBHelper.getStringField(vpMap, SNAME);
      vpSet.add(vsName);
    }
    return vpSet;
  }
}

