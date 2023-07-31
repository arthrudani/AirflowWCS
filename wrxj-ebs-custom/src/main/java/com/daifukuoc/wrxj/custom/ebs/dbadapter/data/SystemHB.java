package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.Date;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;

public class SystemHB extends BaseDBInterface
{
	  private DBResultSet  myDBResultSet;
	  protected  SystemHBData mpSysHBData;

	  public SystemHB()
	  {
	    super("SystemHB");
	    mpSysHBData = Factory.create(SystemHBData.class);
	  }

	
	
	
	  public void updateSystemHB(String sSystemID) throws DBException
	  {
		  mpSysHBData.clear();
		  Date newDate = new Date();
		  mpSysHBData.setHBTime(newDate);
		  mpSysHBData.setKey(SystemHBData.SYSTEM_NAME, sSystemID);
	      modifyElement(mpSysHBData);
	  }
}
