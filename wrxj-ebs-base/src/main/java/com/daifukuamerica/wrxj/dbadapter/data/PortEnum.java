package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.TableEnum;

/**
 *  Enum class representing the Port Table fields.
 *
 *  @author A.D.
 *  @since  18-Nov-2008
 */
public enum PortEnum implements TableEnum
{
  COMMUNICATIONMODE("ICOMMUNICATIONMODE"),
  DEVICEID("SDEVICEID"),
  DIRECTION("IDIRECTION"),
  LASTSEQUENCE("ILASTSEQUENCE"),
  PORTNAME("SPORTNAME"),
  RCVKEEPALIVEINTERVAL("IRCVKEEPALIVEINTERVAL"),
  RETRYINTERVAL("IRETRYINTERVAL"),
  SERVERNAME("SSERVERNAME"),
  SNDKEEPALIVEINTERVAL("ISNDKEEPALIVEINTERVAL"),
  SOCKETNUMBER("SSOCKETNUMBER"),
  ENABLEWRAPPING("IENABLEWRAPPING");
  
  private String msMessageName;

  PortEnum(String isMessageName)
  {
    msMessageName = isMessageName;
  }

  public String getName()
  {
    return(msMessageName);
  }
}
