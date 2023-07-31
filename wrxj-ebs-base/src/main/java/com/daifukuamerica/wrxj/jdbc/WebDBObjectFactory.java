package com.daifukuamerica.wrxj.jdbc;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class WebDBObjectFactory extends BasePooledObjectFactory<DBObject>
{
  String msDbConfig;
  
  public WebDBObjectFactory(String isDbConfig)
  {
    msDbConfig = isDbConfig;
  }

  @Override
  public void activateObject(PooledObject<DBObject> p) throws Exception
  {
    p.getObject().connect();
    new DBObjectTL().putDBObject(msDbConfig, p.getObject());
  }
  
  @Override
  public DBObject create() throws Exception
  {
    return new DBObject(msDbConfig);
  }

  @Override
  public void destroyObject(PooledObject<DBObject> p) throws Exception
  {
    DBObject dbo = p.getObject();
    if (dbo.checkConnected())
    {
      dbo.disconnect(false);
    }
  }
  
  @Override
  public void passivateObject(PooledObject<DBObject> p) throws Exception
  {
    new DBObjectTL().removeDBObject(msDbConfig);
    super.passivateObject(p);
  }
  
  @Override
  public boolean validateObject(PooledObject<DBObject> p)
  {
    return p.getObject().isConnectionActive();
  }
  
  @Override
  public PooledObject<DBObject> wrap(DBObject obj)
  {
    return new DefaultPooledObject<DBObject>(obj);
  }
}
