package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Vehicle Data Table fields used to
 *  persist AGV messages.
 *
 *  @author A.D.
 *  @since  13-May-2009
 */
public enum VehicleMoveEnum implements TableEnum
{
  AGVLOADSTATUS("IAGVLOADSTATUS"),
  CURRSTATION("SCURRSTATION"),
  DESTSTATION("SDESTSTATION"),
  DUALLOADMOVESEQ("SDUALLOADMOVESEQ"),
  LOADID("SLOADID"),
  STATUSCHANGETIME("DSTATUSCHANGETIME"),
  NOTIFYHOST("INOTIFYHOST"),
  REQUESTID("SREQUESTID"),
  SEQUENCENUMBER("ISEQUENCENUMBER"),
  VEHICLEID("SVEHICLEID");

  private String msMessageName;

  VehicleMoveEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  @Override
  public String getName()
  {
    return(msMessageName);
  }
}
