package com.daifukuamerica.wrxj.dataserver.standard;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dbadapter.data.Load;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;
import java.util.Map;

/**
 * A server that provides methods and transactions for use in recovery
 * management. Methods are used to recover loads in various states of movement.
 * Transactions are wrapped around calls to the lower level data base objects.
 *
 * @author avt
 * @version 1.0
 */

public class StandardRecoveryServer extends StandardServer
{
  protected String keyName;
  protected Load mpLoad = null;
  protected TableJoin mpTJ = null;
  protected StandardConfigurationServer mpConfigServ = null;

    //   Public Methods for Recovery Server
  public StandardRecoveryServer()
  {
    this(null);
  }

  public StandardRecoveryServer(String isKeyName)
  {
    super(isKeyName);
    keyName = isKeyName;
    mpLoad = Factory.create(Load.class);
    mpTJ = Factory.create(TableJoin.class);
    mpConfigServ = Factory.create(StandardConfigurationServer.class);
  }
  
  /**
   * Web application constructor for per user connection pooling
   * @param keyName
   * @param dbo
   */
  public StandardRecoveryServer(String isKeyName, DBObject dbo)
  {
	  super(isKeyName, dbo); 
	  keyName = isKeyName;
	  mpLoad = Factory.create(Load.class);
	  mpTJ = Factory.create(TableJoin.class);
	  mpConfigServ = Factory.create(StandardConfigurationServer.class);
  }

 /**
  *  Method to get data objects for matching loads to be recovered.
  *
  *  @param iapColData key data
  *  @return List of <code>LoadData</code> objects.
  *  @exception DBException
  */
  public List<Map> getRecoveryLoadDataList(KeyObject[] iapColData)
         throws DBException
  {
    String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);

    List<Map> vpList;
    if (mpConfigServ.isSplitSystem())
    {
      vpList = mpTJ.getLoadDataListByJVM(vsJVMId, iapColData);
    }
    else
    {
      vpList = mpLoad.getLoadDataList(iapColData);
    }

    return(vpList);
  }

  /**
   *  Method to get data objects for a device's loads to be recovered.
   *
   *  @param deviceId device ID to search for.
   *  @return List of <code>LoadData</code> objects.
   *  @exception DBException
   */
  public List<Map> getDevicesRecoveryLoadDataList(String deviceId) throws DBException
  {
    Load load = Factory.create(Load.class);
    LoadData lddataSearch = Factory.create(LoadData.class);
    lddataSearch.clear();
    lddataSearch.setKey(LoadData.LOADMOVESTATUS_NAME, Integer.valueOf(DBConstants.NOMOVE),
                        KeyObject.NOT_EQUAL);

    lddataSearch.setKey(LoadData.DEVICEID_NAME, deviceId);
    return(load.getAllElements(lddataSearch));
  }
}
