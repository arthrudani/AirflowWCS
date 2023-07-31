package com.daifukuamerica.wrxj.jdbc.sqlserver;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBMetaData;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Gets SQL Server instance Metadata information.
 * @author A.D.
 * @since  04-Sep-2014
 */
public class SQLServerMetaData implements DBMetaData
{
  private DBObject mpDBObj;
  private final SimpleDateFormat mpSDF = new SimpleDateFormat();


  public SQLServerMetaData()
  {
    mpDBObj = new DBObjectTL().getDBObject();
    try
    {
      if (!mpDBObj.checkConnected())
      {
        mpDBObj.connect();               // Get connection from connection pool.
      }
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("Error opening Database Connection", exc);
    }
  }

  @Override
  public String getDatabaseVendorName()
  {
    String vsDBVendor = "";
    try
    {
      DBResultSet vpRslt = mpDBObj.execute("SELECT CONVERT(VARCHAR(50), SUBSTRING(@@VERSION, 1, 25)) AS \"DBVENDOR\"");
      List<Map> vpRows = vpRslt.getRows();
      if (!vpRows.isEmpty())
      {
        vsDBVendor = DBHelper.getStringField(vpRows.get(0), "DBVENDOR");
      }
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("DB Error retrieving Database Vendor Name!", exc);
    }

    return vsDBVendor;
  }

  @Override
  public String getDatabaseServerName()
  {
    String vsDBVendor = "";
    try
    {
      DBResultSet vpRslt = mpDBObj.execute("SELECT CONVERT(VARCHAR(50), SERVERPROPERTY('MachineName')) AS \"HOST_NAME\"");
      List<Map> vpRows = vpRslt.getRows();
      if (!vpRows.isEmpty())
      {
        vsDBVendor = DBHelper.getStringField(vpRows.get(0), "HOST_NAME");
      }
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("DB Error retrieving Database Host Machine Name!", exc);
    }

    return vsDBVendor;
  }

  @Override
  public String getDatabaseInstanceName()
  {
    String vsInstanceName = "";
    try
    {
      DBResultSet vpRslt = mpDBObj.execute("SELECT CONVERT(VARCHAR(50), DB_NAME()) AS \"INSTANCE_NAME\"");
      List<Map> vpRows = vpRslt.getRows();
      if (!vpRows.isEmpty())
      {
        vsInstanceName = DBHelper.getStringField(vpRows.get(0), "INSTANCE_NAME");
      }
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("DB Error retrieving Database Instance Name!", exc);
    }

    return vsInstanceName;
  }

  @Override
  public String getDatabaseVersion()
  {
    String vsVersion = "";
    try
    {
      DBResultSet vpRslt = mpDBObj.execute("SELECT CONVERT(VARCHAR(50), SERVERPROPERTY('ProductVersion')) AS \"VERSION\"");
      List<Map> vpRows = vpRslt.getRows();
      if (!vpRows.isEmpty())
      {
        vsVersion = DBHelper.getStringField(vpRows.get(0), "VERSION");
      }
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("DB Error retrieving Database Version Info.!", exc);
    }

    return vsVersion;
  }

  @Override
  public String getDatabaseStartupTime()
  {
    String vsStartTime = "";
    try
    {
      DBResultSet vpRslt = mpDBObj.execute("SELECT MIN(login_time) AS \"STARTUP_TIME\" FROM sys.sysprocesses");
      List<Map> vpRows = vpRslt.getRows();
      if (!vpRows.isEmpty())
      {
        vsStartTime = DBHelper.getStringField(vpRows.get(0), "STARTUP_TIME");
        try
        {
          mpSDF.applyPattern(SKDCConstants.HOST_DATE_FORMAT);
          Date vpStartDate = mpSDF.parse(vsStartTime);
          mpSDF.applyPattern(SKDCConstants.DATETIME_FORMAT2);
          vsStartTime = mpSDF.format(vpStartDate);
        }
        catch(ParseException pe)
        {
        }
      }
    }
    catch(DBException exc)
    {
      throw new DBRuntimeException("DB Error retrieving Database Start Time Info.!", exc);
    }

    return vsStartTime;
  }
}
