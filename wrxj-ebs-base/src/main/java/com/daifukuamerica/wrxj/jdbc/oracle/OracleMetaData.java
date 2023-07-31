package com.daifukuamerica.wrxj.jdbc.oracle;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBMetaData;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.jdbc.JDBCMetaData;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Implementation for Oracle JDBC Meta Data extensions.
 *
 * @author A.D.
 * @since  24-Feb-2009
 */
public class OracleMetaData implements DBMetaData
{
  private Map<String, Object> mpRecord;
  private DBObject mpDBObj;
  private final SimpleDateFormat mpSDF = new SimpleDateFormat(SKDCConstants.DATETIME_FORMAT2);

  public OracleMetaData()
  {
    mpDBObj = new DBObjectTL().getDBObject();
    try
    {
      if (!mpDBObj.checkConnected())
      {
        mpDBObj.connect();               // Get connection from connection pool.
      }
      DBResultSet vpRslt = mpDBObj.execute("SELECT * FROM V$INSTANCE");
      List<Map> vpRows = vpRslt.getRows();
      if (!vpRows.isEmpty())
      {
        mpRecord = vpRows.get(0);
        JDBCMetaData.init();
      }
    }
    catch(DBException e)
    {
      throw new DBRuntimeException("Error opening Database Connection", e);
    }
  }

 /**
  * {@inheritDoc}
  * @return Vendor name string.
  */
  @Override
  public String getDatabaseVendorName()
  {
    return(JDBCMetaData.getDatabaseVendorName());
  }

 /**
  * {@inheritDoc}
  * @return The name of the machine hosting the Oracle instance.
  */
  @Override
  public String getDatabaseServerName()
  {
    return(((String)mpRecord.get("HOST_NAME")).toLowerCase());
  }

 /**
  * {@inheritDoc}
  * @return the database instance name.
  */
  @Override
  public String getDatabaseInstanceName()
  {
    return((String)mpRecord.get("INSTANCE_NAME"));
  }

 /**
  * {@inheritDoc}
  * @return the database version.
  */
  @Override
  public String getDatabaseVersion()
  {
    return((String)mpRecord.get("VERSION"));
  }

 /**
  * {@inheritDoc}
  * @return the database instance startup time.
  */
  @Override
  public String getDatabaseStartupTime()
  {
    return(mpSDF.format(mpRecord.get("STARTUP_TIME")));
  }
}
