package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.DeviceEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 *   Class to handle DeviceData Object.  Handles all data for load.
 *
 * @author       REA
 * @author       A.D.        Extend off AbstractSKDCData class.  All<BR>
 *                           "setter" methods for translations will<BR>
 *                           default now if the passed in value is<BR>
 *                           incorrect.  Cleaned up code.<BR>
 * @author       A.D.        Eliminate inefficient string comparisons and if-tree.
 * @version      3.0
 * @since        04-Jan-02
 */
public class DeviceData extends AbstractSKDCData
{
  public static final String AISLEGROUP_NAME        = AISLEGROUP.getName();
  public static final String ALLOCATORNAME_NAME     = ALLOCATORNAME.getName();
  public static final String COMMDEVICE_NAME        = COMMDEVICE.getName();
  public static final String COMMREADPORT_NAME      = COMMREADPORT.getName();
  public static final String COMMSENDPORT_NAME      = COMMSENDPORT.getName();
  public static final String DEVICEID_NAME          = DEVICEID.getName();
  public static final String DEVICETOKEN_NAME       = DEVICETOKEN.getName();
  public static final String DEVICETYPE_NAME        = DEVICETYPE.getName();
  public static final String EMULATIONMODE_NAME     = EMULATIONMODE.getName();
  public static final String ERRORCODE_NAME         = ERRORCODE.getName();
  public static final String JVMIDENTIFIER_NAME     = JVMIDENTIFIER.getName();
  public static final String NEXTDEVICE_NAME        = NEXTDEVICE.getName();
  public static final String OPERATIONALSTATUS_NAME = OPERATIONALSTATUS.getName();
  public static final String PHYSICALSTATUS_NAME    = PHYSICALSTATUS.getName();
  public static final String PRINTER_NAME           = PRINTER.getName();
  public static final String SCHEDULERNAME_NAME     = SCHEDULERNAME.getName();
  public static final String STATIONNAME_NAME       = STATIONNAME.getName();
  public static final String USERID_NAME            = USERID.getName();
  public static final String WAREHOUSE_NAME         = WAREHOUSE.getName();

  private int    iDeviceType;
  private int    iAisleGroup;
  private int    iOperationalStatus;
  private int    iPhysicalStatus;
  private int    iEmulationMode;
  private int    iDeviceToken;
  private String sDeviceID      = "";
  private String sCommDevice    = "";
  private String sCommSendPort  = "";
  private String sCommReadPort  = "";
  private String sErrorCode     = "";
  private String sJVMIdentifier = "";
  private String sNextDevice    = "";
  private String sSchedulerName = "";
  private String sStationName   = "";
  protected String sPrinter     = "";
  protected String sUserID      = "";
  protected String sWarehouse   = "";
  private String sAllocatorName = "";
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public DeviceData()
  {
    super();
    initColumnMap(mpColumnMap, DeviceEnum.class);
    clear();                           // set all values to default
  }

  /**
   * Sets all data values to defaults where appropriate (like translations) and
   * clear out the rest.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour.

    iDeviceType        = DBConstants.SRC5;
    iAisleGroup        = 0;
    iOperationalStatus = DBConstants.APPOFFLINE;
    iPhysicalStatus    = DBConstants.OFFLINE;
    iEmulationMode     = DBConstants.NOEMU;
    iDeviceToken       = DBConstants.FALSE;
    sDeviceID          = "";
    sCommDevice        = "";
    sCommSendPort      = "";
    sCommReadPort      = "";
    sErrorCode         = "";
    sNextDevice        = "";
    sSchedulerName     = "";
    sStationName       = "";
    sPrinter           = "";
    sUserID            = "";
    sWarehouse         = "";
    sAllocatorName     = "";
    sJVMIdentifier     = "";
  }

  /**
   * Puts all data into a string
   */
  @Override
  public String toString()
  {
    String str = "sDeviceID:"        + sDeviceID      +
                 "\nsCommDevice:"    + sCommDevice    +
                 "\nsCommSendPort:"  + sCommSendPort  +
                 "\nsCommReadPort:"  + sCommReadPort  +
                 "\nsErrorCode:"     + sErrorCode     +
                 "\nsJVMIdentifier:" + sJVMIdentifier +
                 "\nsNextDevice:"    + sNextDevice    +
                 "\nsSchedulerName:" + sSchedulerName +
                 "\nsStationName:"   + sStationName   +
                 "\nsPrinter:"       + sPrinter       +
                 "\nsUserID:"        + sUserID        +
                 "\nsWarehouse:"     + sWarehouse     +
                 "\nsAllocator:"     + sAllocatorName + "\n";

    try
    {
      str = str + "iDeviceType:" +
         DBTrans.getStringValue(DEVICETYPE.getName(), iDeviceType) +
           "\niOperationalStatus:" +
         DBTrans.getStringValue(OPERATIONALSTATUS.getName(), iOperationalStatus) +
         "\niPhysicalStatus:" +
         DBTrans.getStringValue(PHYSICALSTATUS.getName(), iPhysicalStatus) +
         "\niEmulationMode:" +
         DBTrans.getStringValue(EMULATIONMODE.getName(), iEmulationMode) +
         "\niDeviceToken:" +
         DBTrans.getStringValue(DEVICETOKEN.getName(), iDeviceToken) +
         "\n";
    }
    catch(NoSuchFieldException e)
    {
      str += "0\n";
    }

    str += ("iAisleGroup:" + Integer.toString(iAisleGroup) + "\n");
    str += super.toString();

    return(str);
  }

  /**
   * 
   */
  @Override
  public boolean equals(AbstractSKDCData absDev)
  {
    DeviceData dv = (DeviceData)absDev;
    return(getDeviceID().equals(dv.getDeviceID()));
  }

  /**
   * This generates the string for the field that is changed.
   */
  @Override
  public String getActionDesc(String isColName, Object ipOld, Object ipNew)
  {
    String s = "Field [" + isColName + "]:";

    try
    {
      String vsOld = "";
      String vsNew = "";

      // Construct Action Description string
      switch ((DeviceEnum)mpColumnMap.get(isColName))
      {
        case DEVICETYPE:
        case AISLEGROUP:
        case OPERATIONALSTATUS:
        case PHYSICALSTATUS:
        case EMULATIONMODE:
        case DEVICETOKEN:
          vsOld = DBTrans.getStringValue(isColName, ((Integer)ipOld).intValue());
          vsNew = DBTrans.getStringValue(isColName, ((Integer)ipNew).intValue());
          break;

        default:
          vsOld = ipOld.toString();
          vsNew = ipNew.toString();
      }
      s = s + " Old [" + vsOld + "] New [" + vsNew + "]";
    }
    catch (NoSuchFieldException e)
    {
      s = s + " #### NoSuchFieldException #### ";
    }
    return s;
  }

  /*========================================================================*/
  /*  Calculated data that is not stored in the database                    */
  /*========================================================================*/
  
  /**
   * Get the MOS device for an SRC5 device.  This is not a real database column.
   * 
   * @return
   */
  public String getMosDevice()
  {
    if (iDeviceType != DBConstants.SRC5)
    {
      return "";
    }
    return sDeviceID + "-Mos";
  }

  /**
   * Get the MOS port for an SRC5 device.  This is not a real database column.
   * 
   * @return
   */
  public String getMosPortName()
  {
    if (iDeviceType != DBConstants.SRC5)
    {
      return "";
    }
    return sDeviceID + "-MosPort";
  }

  /*========================================================================*/
  /*  Column Get methods go here                                            */
  /*========================================================================*/

  /**
   * returns the current sDeviceID value
   */
  public String getDeviceID()
  {
    return(sDeviceID);
  }

  /**
   * returns the DeviceType
   */
  public int getDeviceType()
  {
    return(iDeviceType);
  }

  /**
   * returns the AisleGroup
   */
  public int getAisleGroup()
  {
    return(iAisleGroup);
  }

  /**
   *  Returns the current Control Device
   */
  public String getCommDevice()
  {
    return(sCommDevice);
  }

  /**
   *  Returns the Application Device Status
   */
  public int getOperationalStatus()
  {
    return(iOperationalStatus);
  }

  /**
   *  returns the Physical Device Status
   */
  public int getPhysicalStatus()
  {
    return(iPhysicalStatus);
  }

  public String getErrorCode()
  {
    return(sErrorCode);
  }

  public int getEmulationMode()
  {
    return(iEmulationMode);
  }

  public String getCommSendPort()
  {
    return(sCommSendPort);
  }

  public String getCommReadPort()
  {
    return(sCommReadPort);
  }

  public String getNextDevice()
  {
    return(sNextDevice);
  }

  public int getDeviceToken()
  {
    return(iDeviceToken);
  }

  public String getSchedulerName()
  {
    return(sSchedulerName);
  }

  public String getStationName()
  {
    return(sStationName);
  }

  public String getPrinter()
  {
    return(sPrinter);
  }

  public String getUserID()
  {
    return(sUserID);
  }
  
  public String getWarehouse()
  {
  	return(sWarehouse);
  }
  
  public String getAllocatorName()
  {
    return(sAllocatorName);
  }


  public String getJVMIdentifier()
  {
    return sJVMIdentifier;
  }

/*---------------------------------------------------------------------------
               ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
 /**
  * Set sDeviceID with the value passed
  */
  public void setDeviceID(String isDeviceID)
  {
    sDeviceID = isDeviceID;
    addColumnObject(new ColumnObject(DEVICEID.getName(), sDeviceID));
  }

 /**
  * Set the Control Device
  */
  public void setCommDevice (String isCommDevice)
  {
    sCommDevice = isCommDevice;
    addColumnObject(new ColumnObject(COMMDEVICE.getName(), sCommDevice));
  }

  public void setCommSendPort(String isCommSendPort)
  {
    sCommSendPort = isCommSendPort;
    addColumnObject(new ColumnObject(COMMSENDPORT.getName(), sCommSendPort));
  }

  public void setCommReadPort(String isCommReadPort)
  {
    sCommReadPort = isCommReadPort;
    addColumnObject(new ColumnObject(COMMREADPORT.getName(), sCommReadPort));
  }

  public void setErrorCode(String isErrorCode)
  {
    sErrorCode = isErrorCode;
    addColumnObject(new ColumnObject(ERRORCODE.getName(), sErrorCode));
  }

  public void setJVMIdentifier(String isJVMIdentifier)
  {
    sJVMIdentifier = checkForNull(isJVMIdentifier);
    addColumnObject(new ColumnObject(JVMIDENTIFIER_NAME, sJVMIdentifier));
  }

  public void setNextDevice(String isNextDevice)
  {
    sNextDevice = isNextDevice;
    addColumnObject(new ColumnObject(NEXTDEVICE.getName(), sNextDevice));
  }

  public void setSchedulerName(String isSchedulerName)
  {
    sSchedulerName = isSchedulerName;
    addColumnObject(new ColumnObject(SCHEDULERNAME.getName(), sSchedulerName));
  }

  public void setStationName(String isStationName)
  {
    sStationName = isStationName;
    addColumnObject(new ColumnObject(STATIONNAME.getName(), sStationName));
  }

  /**
   * Set the Printer
   */
  public void setPrinter(String isPrinter)
  {
    sPrinter = isPrinter;
    addColumnObject(new ColumnObject(PRINTER.getName(), sPrinter));
  }
  
  /**
   * Set the UserID
   */
  public void setUserID(String isUserID)
  {
    sUserID = isUserID;
    addColumnObject(new ColumnObject(USERID.getName(), sUserID));
  }
  
  /** Set the Warehouse
   * 
   * @param sWarehouse
   */
  public void setWarehouse(String isWarehouse)
  {
    sWarehouse = isWarehouse;
    addColumnObject(new ColumnObject(WAREHOUSE.getName(), sWarehouse));
  }
  
  /**
   * Set the allocator name
   * @param isName
   */
  public void setAllocatorName(String isAllocName)
  {
    sAllocatorName = isAllocName;
    addColumnObject(new ColumnObject(ALLOCATORNAME.getName(), sAllocatorName));
  }


  /**
   * Set the DeviceType
   */
  public void setDeviceType(int inDeviceType)
  {
    iDeviceType = inDeviceType;
    addColumnObject(new ColumnObject(DEVICETYPE.getName(), iDeviceType));
  }

/**
 * Set the AisleGroup
 */
  public void setAisleGroup(int inAisleGroup)
  {
    iAisleGroup = inAisleGroup;
    addColumnObject(new ColumnObject(AISLEGROUP.getName(), iAisleGroup));
  }

  /**
   * Set the Application Device Status
   */
  public void setOperationalStatus(int inOperationalStatus)
  {
    try
    {
      DBTrans.getStringValue(OPERATIONALSTATUS.getName(), inOperationalStatus);
    }
    catch(NoSuchFieldException e)
    {
      iOperationalStatus = DBConstants.APPOFFLINE;
    }
    iOperationalStatus = inOperationalStatus;
    addColumnObject(new ColumnObject(OPERATIONALSTATUS.getName(), inOperationalStatus));
  }

 /**
  * Set the Physical Device Status
  */
  public void setPhysicalStatus(int inPhysicalStatus)
  {
    try
    {
      DBTrans.getStringValue(PHYSICALSTATUS.getName(), inPhysicalStatus);
    }
    catch(NoSuchFieldException e)
    {
      inPhysicalStatus = DBConstants.OFFLINE;
    }

    iPhysicalStatus = inPhysicalStatus;
    addColumnObject(new ColumnObject(PHYSICALSTATUS.getName(), iPhysicalStatus));
  }

  /**
   * Set the Emulation Mode
   */
  public void setEmulationMode(int inEmulationMode)
  {
    try
    {
      DBTrans.getStringValue(EMULATIONMODE.getName(), inEmulationMode); 
    }
    catch(NoSuchFieldException e)
    {
      inEmulationMode = DBConstants.NOEMU;
    }
    iEmulationMode = inEmulationMode;
    addColumnObject(new ColumnObject(EMULATIONMODE.getName(), iEmulationMode));
  }

  public void setDeviceToken(int inDeviceToken)
  {
    try
    {
      DBTrans.getStringValue(DEVICETOKEN.getName(), inDeviceToken);
    }
    catch(NoSuchFieldException e)
    {
      inDeviceToken = DBConstants.FALSE;
    }
    iDeviceToken = inDeviceToken;
    addColumnObject(new ColumnObject(DEVICETOKEN.getName(), iDeviceToken));
  }

 /**
  * {@inheritDoc}
  * @param isColName {@inheritDoc}
  * @param ipColValue {@inheritDoc}
  * @return {@inheritDoc}
  */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
                                       // Special case for when the user specified
    if (vpEnum == null)               // column is unknown.
    {
      return(super.setField(isColName, ipColValue));
    }
    switch((DeviceEnum)vpEnum)
    {
      case AISLEGROUP:
        setAisleGroup((Integer)ipColValue);
        break;
        
      case ALLOCATORNAME:
        setAllocatorName((String)ipColValue);
        break;
        
      case COMMDEVICE:
        setCommDevice((String)ipColValue);
        break;
        
      case COMMREADPORT:
        setCommReadPort((String)ipColValue);
        break;
        
      case COMMSENDPORT:
        setCommSendPort((String)ipColValue);
        break;
        
      case DEVICEID:
        setDeviceID((String)ipColValue);
        break;
        
      case DEVICETOKEN:
        setDeviceToken((Integer)ipColValue);
        break;
        
      case DEVICETYPE:
        setDeviceType((Integer)ipColValue);
        break;
        
      case EMULATIONMODE:
        setEmulationMode((Integer)ipColValue);
        break;
        
      case ERRORCODE:
        setErrorCode((String)ipColValue);
        break;
        
      case NEXTDEVICE:
        setNextDevice((String)ipColValue);
        break;
        
      case JVMIDENTIFIER:
        setJVMIdentifier((String)ipColValue);
        break;

      case OPERATIONALSTATUS:
        setOperationalStatus((Integer)ipColValue);
        break;
        
      case PHYSICALSTATUS:
        setPhysicalStatus((Integer)ipColValue);
        break;
        
      case PRINTER:
        setPrinter((String)ipColValue);
        break;
        
      case SCHEDULERNAME:
        setSchedulerName((String)ipColValue);
        break;
        
      case STATIONNAME:
        setStationName((String)ipColValue);
        break;
        
      case USERID:
        setUserID((String)ipColValue);
        break;
        
      case WAREHOUSE:
        setWarehouse((String)ipColValue);
    }
    
    return(0);
  }
}
