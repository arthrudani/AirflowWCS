package com.daifukuamerica.wrxj.host.messages;

import com.daifukuamerica.wrxj.host.messages.MessageFormatterFactory;
import com.daifukuamerica.wrxj.host.messages.MessageOut;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;

  public class DeviceStatus extends MessageOut
  {
    private final String DEVICEID_NAME  = "sDeviceID";
    private final String DEVICE_STATUS_NAME    = "sStatus";

    /**
     * Default constructor. This constructor finds the correct message formatter
     * to use for this message.
     */
    public DeviceStatus()
    {
      messageFields = new ColumnObject[]
      {
        new ColumnObject(DEVICEID_NAME, ""),
        new ColumnObject(DEVICE_STATUS_NAME, "")
      };
      enumMessageName = MessageOutNames.DEVICE_STATUS;
      msgfmt = MessageFormatterFactory.getInstance();
    }
   
    public void setDeviceName(String isDeviceName)
    {
      ColumnObject.modify(DEVICEID_NAME, isDeviceName, messageFields);
    }
    
    public void setDeviceStatus(String isStatus)
    {
      ColumnObject.modify(DEVICE_STATUS_NAME, isStatus, messageFields);
    }
  }

