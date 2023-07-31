package com.daifukuamerica.wrxj.swingui.zone;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.ZoneGroupData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;

@SuppressWarnings("serial")
public class RecommendedZoneComboBox extends SKDCComboBox
{
  public RecommendedZoneComboBox()
  {
    super();
    setPrototypeDisplayLength(DBInfo.getFieldLength(ZoneGroupData.ZONEGROUP_NAME));
    try
    {
      setComboBoxData(Factory.create(StandardLocationServer.class).getRecommendedZones());
    }
    catch (DBException dbe)
    {
      dbe.printStackTrace();
    }
  }
}
