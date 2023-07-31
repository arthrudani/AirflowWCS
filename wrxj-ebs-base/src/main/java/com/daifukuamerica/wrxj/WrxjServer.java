package com.daifukuamerica.wrxj;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swingui.main.MainFrame;
import com.daifukuamerica.wrxj.swingui.main.MainStartup;

public class WrxjServer
{
  // Is this running as a service?
  public static final String IS_SERVICE = "com.skdaifuku.wrxj.service";
  // Print the properties to the console on start up
  public static final String PRINT_PROPERTIES = "PrintProperties";

  /**
   * Default constructor
   */
  public WrxjServer()
  {
  }

  /**
   * Runner implementation
   */
  public void run()
  {
    Logger logger = Logger.getLogger(getClass().getSimpleName());
    logger.logDebug(getClass().getSimpleName() + " - Logging Attached");
    boolean vzIsAService = Application.getBoolean(IS_SERVICE, false);
    if (vzIsAService)
    {
      MainStartup.initializeCoreOrFail();
    }
    else
    {
      MainStartup.initializeCoreWithGuiOrFail();
      String vsPrintProperties = Application.getString(PRINT_PROPERTIES);
      if (vsPrintProperties != null)
      {
        if (vsPrintProperties.equals("*"))
          vsPrintProperties = "";
        Application.printProperties(vsPrintProperties);
      }

      // Create the main frame
      MainFrame vpMainFrame = Factory.create(MainFrame.class);
      vpMainFrame.validate();
      vpMainFrame.setVisible(true);
      vpMainFrame.logIn();
    }
    
    // We don't need the initial DB connection anymore
    new DBObjectTL().close();
    
    logger.logDebug(getClass().getSimpleName() + " is now Running");
  }
}
