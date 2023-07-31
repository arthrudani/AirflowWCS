/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.dbadapter.data.aed;

import static com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.dbadapter.WynsoftData;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to hold AES_SYS_INSTANCES data
 */
public class InstanceData extends WynsoftData
{
  public static final String ID_NAME                  = ID.getName();
  public static final String COMPUTER_NAME_NAME       = COMPUTER_NAME.getName();
  public static final String DB_CONN_STR_PARAMS_NAME  = DB_CONN_STR_PARAMS.getName();
  public static final String DB_NAME_NAME             = DB_NAME.getName();
  public static final String DB_PASSWORD_NAME         = DB_PASSWORD.getName();
  public static final String DB_SERVER_NAME           = DB_SERVER.getName();
  public static final String DB_TYPE_NAME             = DB_TYPE.getName();
  public static final String DB_USER_NAME             = DB_USER.getName();
  public static final String IDENTITY_NAME_NAME       = IDENTITY_NAME.getName();
  public static final String INTERNAL_PORT_NAME       = INTERNAL_PORT.getName();
  public static final String IP_ADDRESS_NAME          = IP_ADDRESS.getName();
  public static final String LOG_PORT_NAME            = LOG_PORT.getName();
  public static final String PORT_NAME                = PORT.getName();
  public static final String PROCESS_ID_NAME          = PROCESS_ID.getName();
  public static final String PROCESS_NAME_NAME        = PROCESS_NAME.getName();
  public static final String PRODUCT_ID_NAME          = PRODUCT_ID.getName();
  public static final String RUN_PATH_NAME            = RUN_PATH.getName();
  public static final String STATS_MEASURE_INTERVAL_NAME= STATS_MEASURE_INTERVAL.getName();
  public static final String STATS_MEASURE_READING_DELAY_NAME= STATS_MEASURE_READING_DELAY.getName();
  public static final String STATS_MEASURE_TIME_NAME  = STATS_MEASURE_TIME.getName();
  public static final String THREAD_SLEEP_TIME_NAME   = THREAD_SLEEP_TIME.getName();
  public static final String TIME_REGISTERED_NAME     = TIME_REGISTERED.getName();
  public static final String TOTAL_DISK_SPACE_NAME    = TOTAL_DISK_SPACE.getName();
  public static final String TOTAL_MEMORY_NAME        = TOTAL_MEMORY.getName();
  public static final String USER_NAME_NAME           = USER_NAME.getName();
  public static final String VERSION_NAME             = VERSION.getName();

  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<>();

  // ------------------- Instance table data -----------------------------
  private int    mnId;
  private String msComputerName;
  private String msDbConnStrParams;
  private String msDbName;
  private String msDbPassword;
  private String msDbServer;
  private String msDbType;
  private String msDbUser;
  private String msIdentityName;
  private int    mnInternalPort;
  private String msIpAddress;
  private int    mnLogPort;
  private int    mnPort;
  private int    mnProcessId;
  private String msProcessName;
  private int    mnProductID;
  private String msRunPath;
  private int    mnStatsMeasureInterval;
  private int    mnStatsMeasureReadingDelay;
  private int    mnStatsMeasureTime;
  private int    mnThreadSleepTime;
  private Date   mdTimeRegistered;
  private double mdTotalDiskSpace;
  private double mdTotalMemory;
  private String msUserName;
  private String msVersion;

  //-------------------- Instance default data ---------------------------
  public InstanceData()
  {
    super();
    clear();
    initColumnMap(mpColumnMap, InstanceEnum.class);
  }

  @Override
  public String toString()
  {
    StringBuffer myString = new StringBuffer(getClass().getCanonicalName()).append("\n");
    String[] vasKeys = mpColumnMap.keySet().toArray(new String[0]);
    Arrays.sort(vasKeys);
    for (String sKey : vasKeys) {
      ColumnObject vpVal = getColumnObject(sKey);
      String vsVal = vpVal == null ? null : 
        vpVal.getColumnValue() == null ? null : vpVal.getColumnValue().toString();
      myString.append(" * ").append(sKey).append(" = ").append(vsVal).append(";\n");
    }
    return myString.toString();
  }

  @Override
  public boolean equals(AbstractSKDCData absOther)
  {
    InstanceData other = (InstanceData)absOther;
    return other.getID() == getID();
  }


/*---------------------------------------------------------------------------
   Getters
 ---------------------------------------------------------------------------*/
  public int    getId()                 {  return mnId;                   }
  public String getComputerName()       {  return msComputerName;         }
  public String getDbConnStrParams()    {  return msDbConnStrParams;      }
  public String getDbName()             {  return msDbName;               }
  public String getDbPassword()         {  return msDbPassword;           }
  public String getDbServer()           {  return msDbServer;             }
  public String getDbType()             {  return msDbType;               }
  public String getDbUser()             {  return msDbUser;               }
  public String getIdentityName()       {  return msIdentityName;         }
  public int    getInternalPort()       {  return mnInternalPort;         }
  public String getIpAddress()          {  return msIpAddress;            }
  public int    getLogPort()            {  return mnLogPort;              }
  public int    getPort()               {  return mnPort;                 }
  public int    getProcessId()          {  return mnProcessId;            }
  public String getProcessName()        {  return msProcessName;          }
  public int    getProductID()          {  return mnProductID;            }
  public String getRunPath()            {  return msRunPath;              }
  public int    getStatsMeasureInterval(){  return mnStatsMeasureInterval;}
  public int    getStatsMeasureReadingDelay(){  return mnStatsMeasureReadingDelay;}
  public int    getStatsMeasureTime()   {  return mnStatsMeasureTime;     }
  public int    getThreadSleepTime()    {  return mnThreadSleepTime;      }
  public Date   getTimeRegistered()     {  return mdTimeRegistered;       }
  public double getTotalDiskSpace()     {  return mdTotalDiskSpace;       }
  public double getTotalMemory()        {  return mdTotalMemory;          }
  public String getUserName()           {  return msUserName;             }
  public String getVersion()            {  return msVersion;              }


/*---------------------------------------------------------------------------
   Setters
 ---------------------------------------------------------------------------*/
  public void setId(int inId)
  {
    mnId = inId;
    addColumnObject(new ColumnObject(ID_NAME, mnId));
  }
  public void setComputerName(String isComputerName)
  {
    msComputerName = isComputerName;
    addColumnObject(new ColumnObject(COMPUTER_NAME_NAME, msComputerName));
  }
  public void setDbConnStrParams(String isDbConnStrParams)
  {
    msDbConnStrParams = isDbConnStrParams;
    addColumnObject(new ColumnObject(DB_CONN_STR_PARAMS_NAME, msDbConnStrParams));
  }
  public void setDbName(String isDbName)
  {
    msDbName = isDbName;
    addColumnObject(new ColumnObject(DB_NAME_NAME, msDbName));
  }
  public void setDbPassword(String isDbPassword)
  {
    msDbPassword = isDbPassword;
    addColumnObject(new ColumnObject(DB_PASSWORD_NAME, msDbPassword));
  }
  public void setDbServer(String isDbServer)
  {
    msDbServer = isDbServer;
    addColumnObject(new ColumnObject(DB_SERVER_NAME, msDbServer));
  }
  public void setDbType(String isDbType)
  {
    msDbType = isDbType;
    addColumnObject(new ColumnObject(DB_TYPE_NAME, msDbType));
  }
  public void setDbUser(String isDbUser)
  {
    msDbUser = isDbUser;
    addColumnObject(new ColumnObject(DB_USER_NAME, msDbUser));
  }
  public void setIdentityName(String isIdentityName)
  {
    msIdentityName = isIdentityName;
    addColumnObject(new ColumnObject(IDENTITY_NAME_NAME, msIdentityName));
  }
  public void setInternalPort(int inInternalPort)
  {
    mnInternalPort = inInternalPort;
    addColumnObject(new ColumnObject(INTERNAL_PORT_NAME, mnInternalPort));
  }
  public void setIpAddress(String isIpAddress)
  {
    msIpAddress = isIpAddress;
    addColumnObject(new ColumnObject(IP_ADDRESS_NAME, msIpAddress));
  }
  public void setLogPort(int inLogPort)
  {
    mnLogPort = inLogPort;
    addColumnObject(new ColumnObject(LOG_PORT_NAME, mnLogPort));
  }
  public void setPort(int inPort)
  {
    mnPort = inPort;
    addColumnObject(new ColumnObject(PORT_NAME, mnPort));
  }
  public void setProcessId(int inProcessId)
  {
    mnProcessId = inProcessId;
    addColumnObject(new ColumnObject(PROCESS_ID_NAME, mnProcessId));
  }
  public void setProcessName(String isProcessName)
  {
    msProcessName = isProcessName;
    addColumnObject(new ColumnObject(PROCESS_NAME_NAME, msProcessName));
  }
  public void setProductID(int inProductID)
  {
    mnProductID = inProductID;
    addColumnObject(new ColumnObject(PRODUCT_ID_NAME, mnProductID));
  }
  public void setRunPath(String isRunPath)
  {
    msRunPath = isRunPath;
    addColumnObject(new ColumnObject(RUN_PATH_NAME, msRunPath));
  }
  public void setStatsMeasureInterval(int inSTATS_MEASURE_INTERVAL)
  {
    mnStatsMeasureInterval = inSTATS_MEASURE_INTERVAL;
    addColumnObject(new ColumnObject(STATS_MEASURE_INTERVAL_NAME, mnStatsMeasureInterval));
  }
  public void setStatsMeasureReadingDelay(int inSTATS_MEASURE_READING_DELAY)
  {
    mnStatsMeasureReadingDelay = inSTATS_MEASURE_READING_DELAY;
    addColumnObject(new ColumnObject(STATS_MEASURE_READING_DELAY_NAME, mnStatsMeasureReadingDelay));
  }
  public void setStatsMeasureTime(int inStatsMeasureTime)
  {
    mnStatsMeasureTime = inStatsMeasureTime;
    addColumnObject(new ColumnObject(STATS_MEASURE_TIME_NAME, mnStatsMeasureTime));
  }
  public void setThreadSleepTime(int inThreadSleepTime)
  {
    mnThreadSleepTime = inThreadSleepTime;
    addColumnObject(new ColumnObject(THREAD_SLEEP_TIME_NAME, mnThreadSleepTime));
  }
  public void setTimeRegistered(Date idTimeRegistered)
  {
    mdTimeRegistered = idTimeRegistered;
    addColumnObject(new ColumnObject(TIME_REGISTERED_NAME, mdTimeRegistered));
  }
  public void setTotalDiskSpace(double idTotalDiskSpace)
  {
    mdTotalDiskSpace = idTotalDiskSpace;
    addColumnObject(new ColumnObject(TOTAL_DISK_SPACE_NAME, mdTotalDiskSpace));
  }
  public void setTotalMemory(double idTotalMemory)
  {
    mdTotalMemory = idTotalMemory;
    addColumnObject(new ColumnObject(TOTAL_MEMORY_NAME, mdTotalMemory));
  }
  public void setUserName(String isUserName)
  {
    msUserName = isUserName;
    addColumnObject(new ColumnObject(USER_NAME_NAME, msUserName));
  }
  public void setVersion(String isVersion)
  {
    msVersion = isVersion;
    addColumnObject(new ColumnObject(VERSION_NAME, msVersion));
  }

  /**
   * Required set field method. This method figures out what column was passed
   * to it and sets the value. This allows us to have a generic method for all
   * DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null) 
    {
      return super.setField(isColName, ipColValue);
    }
    
    switch ((InstanceEnum)vpEnum)
    {
      case ID:
        setId((Integer)ipColValue);
        break;
      case COMPUTER_NAME:
        setComputerName((String)ipColValue);
        break;
      case DB_CONN_STR_PARAMS:
        setDbConnStrParams((String)ipColValue);
        break;
      case DB_NAME:
        setDbName((String)ipColValue);
        break;
      case DB_PASSWORD:
        setDbPassword((String)ipColValue);
        break;
      case DB_SERVER:
        setDbServer((String)ipColValue);
        break;
      case DB_TYPE:
        setDbType((String)ipColValue);
        break;
      case DB_USER:
        setDbUser((String)ipColValue);
        break;
      case IDENTITY_NAME:
        setIdentityName((String)ipColValue);
        break;
      case INTERNAL_PORT:
        setInternalPort((Integer)ipColValue);
        break;
      case IP_ADDRESS:
        setIpAddress((String)ipColValue);
        break;
      case LOG_PORT:
        setLogPort((Integer)ipColValue);
        break;
      case PORT:
        setPort((Integer)ipColValue);
        break;
      case PROCESS_ID:
        setProcessId((Integer)ipColValue);
        break;
      case PROCESS_NAME:
        setProcessName((String)ipColValue);
        break;
      case PRODUCT_ID:
        setProductID((Integer)ipColValue);
        break;
      case RUN_PATH:
        setRunPath((String)ipColValue);
        break;
      case STATS_MEASURE_INTERVAL:
        setStatsMeasureInterval((Integer)ipColValue);
        break;
      case STATS_MEASURE_READING_DELAY:
        setStatsMeasureReadingDelay((Integer)ipColValue);
        break;
      case STATS_MEASURE_TIME:
        setStatsMeasureTime((Integer)ipColValue);
        break;
      case THREAD_SLEEP_TIME:
        setThreadSleepTime((Integer)ipColValue);
        break;
      case TIME_REGISTERED:
        setTimeRegistered((Date)ipColValue);
        break;
      case TOTAL_DISK_SPACE:
        setTotalDiskSpace((Double)ipColValue);
        break;
      case TOTAL_MEMORY:
        setTotalMemory((Double)ipColValue);
        break;
      case USER_NAME:
        setUserName((String)ipColValue);
        break;
      case VERSION:
        setVersion((String)ipColValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown field " + isColName);
    }
    return 0;
  }
}
