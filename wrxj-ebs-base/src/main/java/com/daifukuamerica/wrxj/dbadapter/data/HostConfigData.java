package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.HostConfigEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Host Configuration data class.
 *
 * @author       A.D.  07/25/2006
 * @version      1.0
 */
public class HostConfigData extends AbstractSKDCData
{
  public static final String ACTIVE_DATA_TYPE = "THE_ACTIVE_DATA_TYPE";
  public static final String ACTIVE_TRANSPORT_TYPE = "THE_ACTIVE_TRANSPORT_TYPE";

  public static final String DATA_FORMAT_TRAN_NAME = "iHostDataFormat";
  public static final String ACTIVECONFIG_NAME   = ACTIVECONFIG.getName();
  public static final String DATAHANDLER_NAME    = DATAHANDLER.getName();
  public static final String GROUP_NAME          = GROUP.getName();
  public static final String PARAMETERNAME_NAME  = PARAMETERNAME.getName();
  public static final String PARAMETERVALUE_NAME = PARAMETERVALUE.getName();

  public static final String HOST_FORMATTER_GROUP_NAME  = "OutboundMessage";
  public static final String HOST_FORMAT_PROP_GROUP_NAME  = "MessageFormatting";
  public static final String HOST_TRANSPORT_GROUP_NAME  = "Transport";
  public static final String HOST_CONTROLLER_GROUP_NAME = "Controller";
  public static final String HOST_PARSER_GROUP_NAME     = "Parser";
  public static final String TCPIP_SERVER_TRANSPORT     = "TCPIP_SERVER";
  public static final String TCPIP_TRANSPORT            = "TCPIP";
  public static final String JDBC_ORACLE_TRANSPORT      = "JDBC-ORACLE";
  public static final String JDBC_DB2_TRANSPORT         = "JDBC-DB2-AS400";
  public static final String JDBC_SQLSERVER_TRANSPORT   = "JDBC-SQLSERVER";

  private String sDataHandler    = "";
  private String sGroup          = "";
  private String sParameterName  = "";
  private String sParameterValue = "";
  private int    iActiveConfig   = DBConstants.NO;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public HostConfigData()
  {
    super();
    initColumnMap(mpColumnMap, HostConfigEnum.class);
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String vsData = "sDataHandler: "      + sDataHandler +
                    "\nsGroup: "          + sGroup      +
                    "\nsParameterName: "  + sParameterName;

    try
    {
      vsData = vsData + "\niActiveConfig:" +
               DBTrans.getStringValue(ACTIVECONFIG_NAME, iActiveConfig);
    }
    catch(NoSuchFieldException e)
    {
      vsData = vsData + "0";
    }

    vsData += super.toString();

    return(vsData);
  }

 /**
  * Defines equality between two HostToWrxData objects.
  *
  * @param  ipData <code>AbstractSKDCData</code> reference whose runtime type
  *         is expected to be <code>HostConfigData</code>
  */
  @Override
  public boolean equals(AbstractSKDCData ipData)
  {
    if (ipData == null || !(ipData instanceof HostConfigData))
    {
      return(false);
    }
    HostConfigData vpHCData = (HostConfigData)ipData;

    return(vpHCData.sDataHandler.equals(sDataHandler) &&
           vpHCData.sGroup.equals(sGroup)             &&
           vpHCData.sParameterName == sParameterName  &&
           vpHCData.sParameterValue == sParameterValue &&
           vpHCData.iActiveConfig == iActiveConfig);
  }

 /**
  * Resets the data in this class to the default.
  */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.

    sDataHandler    = "";
    sGroup         = "";
    sParameterName = "";
    sParameterValue = "";
    iActiveConfig = DBConstants.NO;
  }

 /*===========================================================================
                           Getter methods section.
   ===========================================================================*/
  public String getDataHandler()
  {
    return sDataHandler;
  }

  public String getGroup()
  {
    return sGroup;
  }

  public String getParameterName()
  {
    return sParameterName;
  }

  public String getParameterValue()
  {
    return sParameterValue;
  }

 /**
  * Fetches Active Configuration flag.
  * @return Active Configuration flag as integer
  */
  public int getActiveConfig()
  {
    return(iActiveConfig);
  }

 /*===========================================================================
                           Setter methods section.
   ===========================================================================*/
  public void setDataHandler(String isDataHandler)
  {
    sDataHandler = isDataHandler;
    addColumnObject(new ColumnObject(DATAHANDLER_NAME, sDataHandler));
  }

  public void setGroup(String isGroup)
  {
    sGroup = isGroup;
    addColumnObject(new ColumnObject(GROUP_NAME, sGroup));
  }

  public void setParameterName(String isParameterName)
  {
    sParameterName = isParameterName;
    addColumnObject(new ColumnObject(PARAMETERNAME_NAME, sParameterName));
  }

  public void setParameterValue(String isParameterValue)
  {
    sParameterValue = isParameterValue;
    addColumnObject(new ColumnObject(PARAMETERVALUE_NAME, sParameterValue));
  }

  /**
   * Sets Active Configuration flag.
   */
   public void setActiveConfig(int inActiveConfig)
   {
     try
     {
       DBTrans.getStringValue("iActiveConfig", inActiveConfig);
     }
     catch(NoSuchFieldException e)
     {                                  // Passed value wasn't valid. Default it
       inActiveConfig = DBConstants.NO;
     }
     iActiveConfig = inActiveConfig;
     addColumnObject(new ColumnObject(ACTIVECONFIG_NAME,
       Integer.valueOf(inActiveConfig)));
   }

  /**
   *  Required set field method.  This method figures out what column was
   *  passed to it and sets the value.  This allows us to have a generic
   *  method for all DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return super.setField(isColName, ipColValue);
    }

    switch((HostConfigEnum)vpEnum)
    {
      case DATAHANDLER:
        setDataHandler((String)ipColValue);
        break;

      case GROUP:
        setGroup((String)ipColValue);
        break;

      case PARAMETERNAME:
        setParameterName((String)ipColValue);
        break;

      case PARAMETERVALUE:
        setParameterValue((String)ipColValue);
        break;

      case ACTIVECONFIG:
        setActiveConfig(((Integer)ipColValue).intValue());
    }

    return(0);
  }
}
