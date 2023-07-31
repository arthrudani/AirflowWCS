package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.JVMConfigEnum.*;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JVM data for the JVMConfig table.
 *
 * @author A.D.
 * @since  10-Feb-2009
 */
public class JVMConfigData extends AbstractSKDCData
{
  public static final String JVMIDENTIFIER_NAME = JVMIDENTIFIER.getName();
  public static final String JVMSTATUS_NAME     = JVMSTATUS.getName();
  public static final String JVMTYPE_NAME       = JVMTYPE.getName();
  public static final String SERVERNAME_NAME    = SERVERNAME.getName();
  public static final String JMSTOPIC_NAME      = JMSTOPIC.getName();

  public int    iJVMStatus     = DBConstants.JVM_UNUSED;
  public int    iJVMType       = DBConstants.SECONDARY_JVM;
  public String sJVMIdentifier = "";
  public String sServerName    = "";
  public String sJMSTopic  = "";

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();


  public JVMConfigData()
  {
    super();
    initColumnMap(mpColumnMap, JVMConfigEnum.class);
  }

 /**
  * This helps in debugging when we want to print the whole structure.
  */
  @Override
  public String toString()
  {
    String vsData = "sJVMIdentifier: "   + sJVMIdentifier +
                    "\nsJMSTopic: "    + sJMSTopic  +
                    "\nsServerName: "    + sServerName;

    try
    {
      vsData = vsData + "\niJVMStatus:" +
               DBTrans.getStringValue(JVMSTATUS_NAME, iJVMStatus);
      vsData = vsData + "\niJVMType:" +
               DBTrans.getStringValue(JVMTYPE_NAME, iJVMType);
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
  * @return 
  */
  @Override
  public boolean equals(AbstractSKDCData ipData)
  {
    if (ipData == null || !(ipData instanceof JVMConfigData))
    {
      return(false);
    }

    JVMConfigData vpJVMData = (JVMConfigData)ipData;

    return(vpJVMData.sJVMIdentifier.equals(sJVMIdentifier) &&
           vpJVMData.sServerName.equals(sServerName)       &&
           vpJVMData.sJMSTopic.equals(sJMSTopic)           &&
           vpJVMData.iJVMStatus == iJVMStatus              &&
           vpJVMData.iJVMType == iJVMType);
  }

 /**
  * Resets the data in this class to the default.
  */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in default behaviour.
    iJVMStatus     = DBConstants.JVM_UNUSED;
    iJVMType       = DBConstants.SECONDARY_JVM;
    sJVMIdentifier = "";
    sServerName    = "";
    sJMSTopic      = "";
  }

 /*===========================================================================
                           Getter methods section.
   ===========================================================================*/
  public int getJVMStatus()
  {
    return(iJVMStatus);
  }

  public int getJVMType()
  {
    return(iJVMType);
  }

  public String getJVMIdentifier()
  {
    return(sJVMIdentifier);
  }

  public String getServerName()
  {
    return(sServerName);
  }

  public String getJMSTopic()
  {
    return(sJMSTopic);
  }

 /*===========================================================================
                           Setter methods section.
   ===========================================================================*/
  public void setJVMStatus(int inJVMStatus)
  {
    iJVMStatus = inJVMStatus;
    addColumnObject(new ColumnObject(JVMSTATUS_NAME, iJVMStatus));
  }

  public void setJVMType(int inJVMType)
  {
    try
    {
     DBTrans.getStringValue(JVMTYPE_NAME, inJVMType);
    }
    catch(NoSuchFieldException e)
    {                                  // Passed value wasn't valid. Default it
     inJVMType = DBConstants.SECONDARY_JVM;
    }
    
    iJVMType = inJVMType;
    addColumnObject(new ColumnObject(JVMTYPE_NAME, Integer.valueOf(inJVMType)));
  }

  public void setJVMIdentifier(String isJVMIdentifier)
  {
    sJVMIdentifier = isJVMIdentifier;
    addColumnObject(new ColumnObject(JVMIDENTIFIER_NAME, sJVMIdentifier));
  }

  public void setServerName(String isServerName)
  {
    sServerName = isServerName;
    addColumnObject(new ColumnObject(SERVERNAME_NAME, sServerName));
  }

  public void setJMSTopic(String isJMSTopic)
  {
    sJMSTopic = isJMSTopic;
    addColumnObject(new ColumnObject(JMSTOPIC_NAME, sJMSTopic));
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

    switch((JVMConfigEnum)vpEnum)
    {
      case JVMIDENTIFIER:
        setJVMIdentifier((String)ipColValue);
        break;

      case JVMSTATUS:
        setJVMStatus((Integer)ipColValue);
        break;

      case JVMTYPE:
        setJVMType((Integer)ipColValue);
        break;

      case SERVERNAME:
        setServerName((String)ipColValue);
        break;

      case JMSTOPIC:
        setJMSTopic((String)ipColValue);
    }

    return(0);
  }
}
