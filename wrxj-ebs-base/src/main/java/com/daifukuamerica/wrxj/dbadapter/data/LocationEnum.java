package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Location Table fields.
 *
 *  @author A.D.
 *  @since  05-Jul-2007
 */
public enum LocationEnum implements TableEnum
{
  ADDRESS("SADDRESS"),
  AISLEGROUP("IAISLEGROUP"),
  ALLOWDELETION("IALLOWDELETION"),
  ASSIGNEDLENGTH("IASSIGNEDLENGTH"),
  DEVICEID("SDEVICEID"),
  EMPTYFLAG("IEMPTYFLAG"),
  HEIGHT("IHEIGHT"),
  LINKEDADDRESS("SLINKEDADDRESS"),
  LOCATIONDEPTH("ILOCATIONDEPTH"),
  LOCATIONSTATUS("ILOCATIONSTATUS"),
  LOCATIONTYPE("ILOCATIONTYPE"),
  MOVESEQUENCE("IMOVESEQUENCE"),
  SEARCHORDER("ISEARCHORDER"),
  SHELFPOSITION("SSHELFPOSITION"),
  SWAPZONE("ISWAPZONE"),
  WAREHOUSE("SWAREHOUSE"),
  WAREHOUSETYPE("IWAREHOUSETYPE"),
  ZONE("SZONE");

  private String msMessageName;

  LocationEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  @Override
  public String getName()
  {
    return(msMessageName);
  }
}
