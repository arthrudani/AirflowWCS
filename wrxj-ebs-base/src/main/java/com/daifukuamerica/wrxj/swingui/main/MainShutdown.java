package com.daifukuamerica.wrxj.swingui.main;

import com.daifukuamerica.wrxj.controller.ControllerFactory;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import java.net.ServerSocket;

public class MainShutdown  implements Runnable
{
  private static String upSince = null;

  /*--------------------------------------------------------------------------*/
  static
  {
    SkDateTime tempTime = new SkDateTime("HH:mm:ss   EEE  dd-MMM-yy");
    upSince = "Up since: " + tempTime.getCurrentDateTimeAsString();
  }

  public MainShutdown()
  {
  }

  public void run()
  {
    Logger logger = Logger.getLogger("MainShutdown");
    logger.logDebug("MainShutdown.run() - Start");
    String swVersion = WrxjVersion.getSoftwareVersion() + " - " + upSince;
    logger.logDebug("MainShutdown.run() - shutdownAllControllers()");
    try
    {
      ControllerFactory.stopAllControllers();
      ServerSocket vpApplicationInstance = MainStartup.gpApplicationInstance;
      if (vpApplicationInstance != null)
      {
        logger.logDebug("MainShutdown.run() - vpApplicationInstance.close()");
        vpApplicationInstance.close();
      }
    }
    catch(Throwable t)
    {
      logger.logException(t, "vpApplicationInstance.close()");
    }
    finally
    {
      StandardConfigurationServer vpConfigServ = Factory.create(StandardConfigurationServer.class);
      vpConfigServ.unreserveJVM();
      vpConfigServ.cleanUp();
    }
    logger.logOperation(LogConsts.OPR_USER, "===== APPLICATION SHUTDOWN - " + swVersion + " =====");
    logger.logError("===== APPLICATION SHUTDOWN - " + swVersion + " =====");
    LogFileReaderWriter.shutdownLogs();
    logger = null;
  }
}

