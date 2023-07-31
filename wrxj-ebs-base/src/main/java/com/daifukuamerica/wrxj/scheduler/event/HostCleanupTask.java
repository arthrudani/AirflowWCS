package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostInDelegate;
import com.daifukuamerica.wrxj.host.HostOutDelegate;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Date;
import java.util.NoSuchElementException;

public class HostCleanupTask extends TimedEventTask
{
  int mnCutoff;
  
  public HostCleanupTask(String isName)
  {
    super(isName);
  }

  @Override
  public void run()
  {
    long vnDays = mnCutoff * ONE_DAY;
    StandardHostServer vpHostServ = Factory.create(StandardHostServer.class);
    Date vpCutoffDate = new Date(new Date().getTime() - vnDays);
    mpLogger.logOperation("HostCleanUpTask - Deleting all host messages before date: " + vpCutoffDate.toString());
    try
    {
      String[] vpHostNames = vpHostServ.getHostNames();

      HostInDelegate vpHostInDelegate = new HostInDelegate();
      HostOutDelegate vpHostOutDelegate = new HostOutDelegate();
      for (int k = 0; k < vpHostNames.length; k++)
      {
        try
        {
          vpHostInDelegate.setInfo(vpHostNames[k], vpCutoffDate);
          vpHostServ.deleteProcessedMessage(vpHostInDelegate);
        }
        catch (NoSuchElementException nsee) { /* This is okay */ }
        
        try
        {
          vpHostOutDelegate.setInfo(vpHostNames[k], vpCutoffDate);
          vpHostServ.deleteProcessedMessage(vpHostOutDelegate);
        }
        catch (NoSuchElementException nsee) { /* This is okay */ }
      }
    }
    catch (DBException ex)
    {
      mpLogger.logError("Error cleaning up host messages: " + ex.getMessage());
    }
    catch (Exception e)
    {
      mpLogger.logError("Error cleaning up host messages: " + e.getMessage());
    }
    
  }

  @Override
  public String initTask()
  {
    try
    {
      StandardConfigurationServer mpConfigSrvr = Factory.create(StandardConfigurationServer.class);
      if (mpConfigSrvr.isSplitSystem() && mpConfigSrvr.isThisPrimaryJVM() == false)
      {
        mpLogger.logOperation("INVALID JVM (" + 
                Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY) +
                ") - a Secondary JVM - HostCleanupTask will not be started.");
        return " ";
      }
    }
    catch (DBException e)
    {
      return e.getMessage();
    }
    
    mzFixed = true;
    mnInitialInterval = ONE_DAY;
    int vnDays = getConfigValue(INTERVAL);
    msIntervalString = vnDays + " days ";
    mnInterval = vnDays * ONE_DAY;
    if (vnDays < 1)
      return "INVALID HostCleanup interval - " + vnDays + " HostCleanupTask will not be started.";

    mnCutoff = getConfigValue("DaysToKeep");
    if (mnCutoff < 1)
      return "INVALID HostCleanup DaysToKeep - " + mnCutoff + " HostCleanupTask will not be started.";
    
    run();
    
    return null;
  }
}
