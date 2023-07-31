package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Device Table fields.
 *
 *  @author A.D.
 *  @since  25-Jun-2007
 */
public enum DeviceEnum implements TableEnum
{
  AISLEGROUP("IAISLEGROUP"),
  ALLOCATORNAME("SALLOCATORNAME"),
  COMMDEVICE("SCOMMDEVICE"),
  COMMREADPORT("SCOMMREADPORT"),
  COMMSENDPORT("SCOMMSENDPORT"),
  DEVICEID("SDEVICEID"),
  DEVICETOKEN("IDEVICETOKEN"),
  DEVICETYPE("IDEVICETYPE"),
  EMULATIONMODE("IEMULATIONMODE"),
  ERRORCODE("SERRORCODE"),
  JVMIDENTIFIER("SJVMIDENTIFIER"),
  NEXTDEVICE("SNEXTDEVICE"),
  OPERATIONALSTATUS("IOPERATIONALSTATUS"),
  PHYSICALSTATUS("IPHYSICALSTATUS"),
  PRINTER("SPRINTER"),
  SCHEDULERNAME("SSCHEDULERNAME"),
  STATIONNAME("SSTATIONNAME"),
  USERID("SUSERID"),
  WAREHOUSE("SWAREHOUSE");

  private String msMessageName;

  DeviceEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}