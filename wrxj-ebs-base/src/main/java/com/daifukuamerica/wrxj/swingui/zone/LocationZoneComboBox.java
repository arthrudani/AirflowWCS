package com.daifukuamerica.wrxj.swingui.zone;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;

@SuppressWarnings("serial")
public class LocationZoneComboBox extends SKDCComboBox
{
  /**
   * Constructor
   * 
   * @param isPrepender - prepender for the list (null if none)
   */
  public LocationZoneComboBox(String isPrepender)
  {
    super();
    setPrototypeDisplayLength(DBInfo.getFieldLength(LocationData.ZONE_NAME));
    try
    {
      setComboBoxData(Factory.create(StandardLocationServer.class).getZoneChoiceList(isPrepender));
    }
    catch (DBException dbe)
    {
      dbe.printStackTrace();
    }
  }
}
