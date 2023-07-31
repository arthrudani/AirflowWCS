package com.daifukuoc.wrxj.custom.ebs.dataserver;


import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.SystemHB;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.SystemHBData;
import com.daifukuamerica.wrxj.dataserver.standard.StandardServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;

import com.daifukuamerica.wrxj.factory.Factory;

import com.daifukuamerica.wrxj.jdbc.DBException;

public class SystemHBServer extends StandardServer
{


	  protected final SystemHB mpSysHB = Factory.create(SystemHB.class);
	  protected final SystemHBData mpSysHBData = Factory.create(SystemHBData.class);
	  protected String msMyClass = null;

  /**
   * Constructor w/o key name
   */
  public SystemHBServer()
  {
    this(null);
  }

  /**
   * Constructor with key name
   *
   * @param isKeyName
   */
  public SystemHBServer(String isKeyName)
  {
    super(isKeyName);
  }

  /**
   * Shuts down this controller by canceling any timers and shutting down the
   * Equipment.
   */
  @Override
  public void cleanUp()
  {
    super.cleanUp();
  }
  
  
  public void updateSystemHBTime(String sSystem) throws DBException
  {
    TransactionToken vpTok = null;
    try
    {
      vpTok = startTransaction();
     
      mpSysHB.updateSystemHB(sSystem);
      commitTransaction(vpTok);
    }
    finally
    {
      endTransaction(vpTok);
    }
  }
  
  
}
