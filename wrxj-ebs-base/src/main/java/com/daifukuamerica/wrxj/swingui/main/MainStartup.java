package com.daifukuamerica.wrxj.swingui.main;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerFactory;
import com.daifukuamerica.wrxj.controller.DaemonController;
import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.dataserver.standard.StandardAeSystemServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.JVMConfig;
import com.daifukuamerica.wrxj.dbadapter.data.JVMConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.factory.FactoryException;
import com.daifukuamerica.wrxj.factory.ImplementationsMap;
import com.daifukuamerica.wrxj.ipc.MessageServiceImpl;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.SkdRtException;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import com.wynright.wrxj.app.Wynsoft;
import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;

/**
 * Initializes application environment.
 *
 * <p><b>Details:</b> <code>MainStartup</code>, a singleton class, comprises
 * methods for initializing the WRx-J application environment.  This class may
 * be used to initialize the entire application or selectively initialize
 * individual fixtures for unit testing.</p>
 *
 * <p>To initialize the entire application environment prior to launching the
 * application, use {@link #initializeCoreOrFail() initializeCoreOrFail}.  To
 * initialize individual fixtures for unit testing, call the specific
 * initialization methods that are required by the test cases.</p>
 *
 * <p>Many of the initialization methods in this class throw
 * {@link StartupFailureException}s.  In most cases, these exceptions should
 * result in the immediate failure of the process.  To handle a thrown
 * <code>StartupFailureException</code> you have caught, consider using
 * {@link #fail(StartupFailureException) fail}.</p>
 *
 * @author Sharky
 * @author Steve
 */
public final class MainStartup
{
  private static Logger logger;
  
  private static ControllerListConfiguration gpClc;
  public static final String CLC_ARG="com.skdaifuku.wrxj.clc.arg";
  public static final String ALLOW_MULTIPLE_INSTANCES = "com.skdaifuku.wrxj.dup";
  
  /**
   * Inaccessible default constructor.
   */
  private MainStartup() {throw new InstantiationError();}

  /*--------------------------------------------------------------------------*/
  /**
   * Initializes core application fixtures and kills the application on failure.
   *
   * <p><b>Details:</b> <code>initializeCoreOrFail</code> attempts to initialize
   * the core application environment.  If it fails, the application exits.
   * This is done by calling <code>initializeCore</code> and, if there is a
   * <code>StartupFailureException</code>, passing the exception to
   * {@link #fail(StartupFailureException) fail}.</p>
   *
   * @see #initializeCore()
   * @see #fail(StartupFailureException)
   */
  public static void initializeCoreOrFail()
  {
    try
    {
      initializeCore();
    }
    catch (final StartupFailureException ve)
    {
      fail(ve);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Initializes core application and GUI fixtures and kills the application on
   * failure.
   *
   * <p><b>Details:</b> <code>initializeCoreOrFail</code> does the same thing as
   * {@link #initializeCoreOrFail() initializeCoreOrFail} with the addition of
   * also attempting to initialize the GUI.</p>
   *
   * @see #initializeCoreOrFail()
   */
  public static void initializeCoreWithGuiOrFail()
  {
    try
    {
      initializeCoreWithGui();
    }
    catch (final StartupFailureException ve)
    {
      fail(ve);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Initializes core application fixtures.
   *
   * <p><b>Details:</b> <code>initializeCore</code> attempts to initialize the
   * core application environment.  The current implementation of this method
   * loads the application properties, controller list configuration, and server
   * factory.  It also initializes the logger and registers the shutdown
   * routine.</p>
   *
   * <p>No GUI-<wbr>related initialization is performed here because it may be
   * possible, some day, to run the application without the GUI.</p>
   *
   * @throws StartupFailureException cause of failure
   */
  public static void initializeCore() throws StartupFailureException
  {
    try
    {
      logger = Logger.getLogger();
      initializeFactory();
      checkPreviousInstance();
      initDBTran();
      DBInfo.init();
      checkDBConnection();
      checkMessageService();
      initializeExceptionHandler();
      loadJVMParameters();
      initializeAutoLogSaver();
      logPropertyValues();          // MUST occur AFTER initializeAutoLogSaver()
      initializeExpertFactory();
      initializeControllerListConfiguration();
      initializeShutdownHook();
      loadMiscStaticData();
      updateAESystem();
      doCustomInitialization();
      autoStartControllers();
    }
    catch (Exception e)
    {
      // Pretty much any exception during startup is fatal.
      // Don't leave Warehouse Rx in a coma.
      throw new StartupFailureException(e.getMessage(), e);
    }
  }

  /**
   * Initializes core application and GUI fixtures.
   *
   * <p><b>Details:</b> <code>initializeCoreWithGui</code> does the same thing
   * as {@link #initializeCore() initializeCore} with the addition of also
   * attempting to initialize the GUI.</p>
   *
   * @see #initializeCore()
   * @throws StartupFailureException cause of failure
   */
  public static void initializeCoreWithGui() throws StartupFailureException
  {
    initializeCore();
    initializeLookAndFeel();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Outputs failure reason and terminates process.
   *
   * <p><b>Details:</b> <code>fail</code> outputs information about the given
   * <code>StartupFailureException</code> and terminates the process with an
   * exit code of 1.</p>
   *
   * @param ipCause failure reason
   */
  public static void fail(StartupFailureException ipCause)
  {
    System.err.println("Fatal startup error:");
    ipCause.printStackTrace(System.err);
    
    Throwable vpCause = ipCause.getCause();
    String vsDetails = ""; 
    if (vpCause != null)
    {
      vpCause.printStackTrace();
      vsDetails = "\n\nDetails:\n" + vpCause.getClass().getCanonicalName()
          + "\n" + vpCause.getMessage();
    }
    vsDetails += "\n\nWarehouse Rx will now exit.";
    JOptionPane.showMessageDialog(null, ipCause.getMessage() + vsDetails,
        "Warehouse Rx - Fatal Startup Error", JOptionPane.ERROR_MESSAGE);
    System.exit(1);
  }

  static ServerSocket gpApplicationInstance;
  
  /*--------------------------------------------------------------------------*/
  /**
   * See if we are trying to create another instance of an already running
   * client or server.  Try opening a Server/Listen socket and if we get an
   * exception throw an exception and exit.
   */
  private static void checkPreviousInstance() throws StartupFailureException
  {
    boolean vzAllowMultipleInstances = Application.getBoolean(MainStartup.ALLOW_MULTIPLE_INSTANCES, false);
    if ( !vzAllowMultipleInstances)
    {
      int vnAppInstancePort = Application.getInt("AppInstancePort", 5882);
      try
      {
        gpApplicationInstance = new ServerSocket(vnAppInstancePort);
      }
      catch(Exception e)
      {
        throw new StartupFailureException(
            "Previous application instance running on server socket port: "
                + vnAppInstancePort, e);
      }
    }
  }

 /**
  * Loads the translation class.
  */
  private static void initDBTran()
  {
    Class<?> myClass = Factory.create(DBTrans.class).getClass();
    try
    {
      myClass.getMethod("init", new Class[] {}).invoke(null, new Object[] {});
    }
    catch(NoSuchMethodException nm)
    {
      nm.printStackTrace();
    }
    catch(InvocationTargetException it)
    {
      it.printStackTrace();
    }
    catch(IllegalAccessException ia)
    {
      ia.printStackTrace();
    }
  }
  
  /**
   * Make sure we have a database connection
   */
  private static void checkDBConnection() throws StartupFailureException
  {
    if (!DBObject.isWRxJConnectionActive())
    {
      throw new StartupFailureException("No database connection available for Warehouse Rx");
    }
  }

  /**
   * Check the Message Service.
   */
  private static void checkMessageService() throws StartupFailureException
  {
    String vsArg = Application.getString(MainStartup.CLC_ARG);
    if (!vsArg.equalsIgnoreCase(ControllerListConfiguration.CLC_CLIENT))
    {
      MessageServiceImpl vpMsgSrvTst = Factory.create(MessageServiceImpl.class);
      vpMsgSrvTst.initialize(MainStartup.class.getSimpleName());
      vpMsgSrvTst.startup();
      String vsFailure = vpMsgSrvTst.getStartupFailReason();
      vpMsgSrvTst.shutdown();
      if (SKDCUtility.isFilledIn(vsFailure))
      {
        throw new StartupFailureException(vsFailure);
      }
    }
  }

 /**
  * Method to load JVM configuration parameters if this is a split system.
  * @throws StartupFailureException if there are database errors, or invalid
  *         JMS topic configuration in the startup property file.
  */
  private static void loadJVMParameters() throws StartupFailureException
  {
    JVMConfig vpJVMConf = Factory.create(JVMConfig.class);
    try
    {
      if (vpJVMConf.isAnyJVMConfigured())
      {
        String vsJMSKeyValue = Application.getString("IpcMessageService.JmsTopicName");
        String[] vsJMSTopic = vsJMSKeyValue.split("/");
        if (vsJMSTopic.length > 1)
        {
          JVMConfigData vpJVMData = vpJVMConf.getJVMRecord(vsJMSTopic[1]);
          if (vpJVMData != null)
          {
            Application.setString(SKDCConstants.JVM_IDENTIFIER_KEY, vpJVMData.getJVMIdentifier());
            Application.setString(SKDCConstants.JVM_SERVER_KEY, vpJVMData.getServerName());
            Application.setString(SKDCConstants.JVM_JMSTOPIC_KEY, vpJVMData.getJMSTopic());
          }
          else
          {
            throw new StartupFailureException("System is not configured correctly " +
             "for multiple JVMs. No valid topic found in startup property file.");
          }
        }
        else
        {
          throw new StartupFailureException("System is not configured correctly " +
           "for multiple JVMs. No valid topic found in startup property file.");
        }
      }
    }
    catch(DBException ex)
    {
      throw new StartupFailureException("Database error trying to find " +
                      "configuration info. to run system as multiple JVMs.", ex);
    }
  }

  private static void loadMiscStaticData()
  {
    LoadData.initAmountFullTransMap();

    if (Application.getBoolean("HostSystemEnabled"))
    {
      HostConfig vpHostCfg = Factory.create(HostConfig.class);
      try
      {
                                         // Figure out what type of Host Messaging
                                         // is to be in effect.
        int vnDataFormat = vpHostCfg.getDataFormat();
        Application.setInt(HostConfigData.ACTIVE_DATA_TYPE, vnDataFormat);

        int vnActiveTransp = vpHostCfg.getActiveTransportMethod();
        Application.setInt(HostConfigData.ACTIVE_TRANSPORT_TYPE, vnActiveTransp);
                                         // Get a list of active outbound Host messages.
        String[] vasHostMesgs = vpHostCfg.getAllDefinedOutboundMesgNames();
        for(String vsMesgName : vasHostMesgs)
        {
          int vnEnableFlag = (vpHostCfg.isOutboundMsgActive(vsMesgName)) ?
                              SKDCConstants.ACTIVE_MESSAGE : SKDCConstants.INACTIVE_MESSAGE;
          Application.setInt(vsMesgName, vnEnableFlag);
        }
      }
      catch(DBException e)
      {
        Logger.getLogger().logError("Error determining expected Host data format.... " +
                        "Database access error for HostConfig table to read " +
                        "\"DataType\"" + e.getMessage());
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Initializes log auto saver.
   *
   * <p><b>Details:</b> <code>initializeLogger</code> prepares the logger for
   * use by loading <code>LogFileReaderWriter</code>'s static initializer, which
   * creates one set of auto-log-savers for the application.</p>
   */
  public static void initializeAutoLogSaver()
  {
    LogFileReaderWriter.autoSaveLogs();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * 
   */
  private static void logPropertyValues()
  {
    //
    // ALL Packages ? ...sk
    //
    logger.logError("===== APPLICATION START - " + WrxjVersion.getSoftwareVersion() + " =====");
    logger.logOperation(LogConsts.OPR_USER, "");

    String vsClcArg = Application.getString(MainStartup.CLC_ARG);
    logger.logOperation(LogConsts.OPR_USER, "Properties " + MainStartup.CLC_ARG + ": " + vsClcArg);
  }


  /*--------------------------------------------------------------------------*/
  /**
   * Loads controller list configuration.
   *
   * <p><b>Details:</b> <code>initializeControllerListConfiguration</code> loads
   * the controller list configuration (CLC) specified by application properties
   * <code>clc.class</code> and <code>clc.arg</code>.  This method fails if
   * these properties are not defined or if they specify an invalid or
   * unloadable configuration.  Refer to the comments for these properties in
   * the application properties file for more information.</p>
   */
  public static void initializeControllerListConfiguration() 
      throws StartupFailureException
  {
    try
    {
      // CLC_ARG = "ControllerListConfiguration argument"
      String vsArg = Application.getString(MainStartup.CLC_ARG);
      if (vsArg == null)
        throw new SkdRtException("Property " + MainStartup.CLC_ARG + " undefined.");
      ControllerListConfiguration vpInstance = Factory.create(ControllerListConfiguration.class, vsArg);
      ControllerFactory.setClc(vpInstance);
      gpClc = vpInstance;
    }
    // ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException:
    catch(Throwable ve)
    {
      String vsError = "Error loading CLC. " + SKDCUtility.appendNestedExceptionMessages(ve);
      throw new StartupFailureException(vsError, ve);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Sets Swing look and feel to resemble host.
   *
   * <p><b>Details:</b> <code>initializeLookAndFeel</code> sets the Swing look
   * and feel to mimick that of the host graphical environment.  It is unclear
   * what conditions must exist for this method to fail, but it is believed that
   * failure is unlikely.</p>
   */
  public static void initializeLookAndFeel()
  {
//    final String vsLookAndFeelClass = UIManager.getSystemLookAndFeelClassName();
//    try
//    {
//      UIManager.setLookAndFeel(vsLookAndFeelClass);
//    }
//    catch (final Exception ve)
//    {
//      throw new SkdRtException("Unable to load look and feel:" + vsLookAndFeelClass, ve);
//    }
    if (UIManager.getLookAndFeel().getName().equals("Metal"))
      modifyMetalLookAndFeel();
  }
  
  /**
   * Mike decided that he didn't like some inconsistencies in the Metal LAF.
   * This is intended to correct those annoyances. 
   */
  public static void modifyMetalLookAndFeel()
  {
    UIManager.getDefaults().remove("ComboBox.disabledForeground");
    UIManager.getDefaults().put("ComboBox.disabledForeground", new Color(153, 153, 153));

    UIManager.getDefaults().remove("ComboBox.disabledBackground");
    UIManager.getDefaults().put("ComboBox.disabledBackground", Color.white);

    UIManager.getDefaults().remove("List.background");
    UIManager.getDefaults().put("List.background", Color.white);
    
    UIManager.getDefaults().remove("TextField.inactiveForeground");
    UIManager.getDefaults().put("TextField.inactiveForeground", new Color(153, 153, 153));

    UIManager.getDefaults().remove("TextField.font");
    UIManager.getDefaults().put("TextField.font", new FontUIResource("Dialog", Font.BOLD, 12));

    UIManager.getDefaults().remove("TextField.margin");
    UIManager.getDefaults().put("TextField.margin", new InsetsUIResource(2, 0, 3, 0));

    UIManager.getDefaults().remove("PasswordField.margin");
    UIManager.getDefaults().put("PasswordField.margin", new InsetsUIResource(2, 0, 3, 0));
  }
  
  /*--------------------------------------------------------------------------*/
  public static void initializeExceptionHandler()
  {
    Thread.UncaughtExceptionHandler mmpExcpHndlr = new WrxjUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(mmpExcpHndlr);
  }

  /**
   * Loads expert factory.
   *
   * <p><b>Details:</b> <code>initializeExpertFactory</code> loads the expert
   * factory specified by application properties
   * <code>expert_factory.class</code> and <code>expert_factory.arg</code>.
   * This method fails if these properties are not defined or if they specify an
   * invalid or unloadable configuration.  Refer to the comments for these
   * properties in the application properties file for more information.</p>
   */
  public static void initializeExpertFactory()
  {
    String vsArg = Application.getString(MainStartup.CLC_ARG);
    if (!vsArg.equalsIgnoreCase(ControllerListConfiguration.CLC_CLIENT))
    {
      String vsClassName = null;
        
      vsClassName = Application.getString("RmiServerExpertFactory");
      if (vsClassName != null)
      {
        try
        {
          // The constructor will startup the RMI Server for us.
          Class<?> vpExpertFactoryClass = (Class.forName(vsClassName));
          vpExpertFactoryClass.getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
          logger.logException(e, "RmiServerExpertFactory \"" + vsClassName
              + "\" - initializeExpertFactory()");
        }
      }
    }
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   * Registers shutdown hook.
   *
   * <p><b>Details:</b> <code>initializeShutdownHook</code> registers a new
   * instance of <code>MainShutdown</code> to be executed when the application
   * begins shutting down.</p>
   */
  public static void initializeShutdownHook()
  {
    MainShutdown mainShutdown = new MainShutdown();
    Thread mainFrameShutdownThread = new NamedThread(mainShutdown, "MainShutdown");
    Runtime.getRuntime().addShutdownHook(mainFrameShutdownThread);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Connects database from AWT event thread.
   *
   * <p><b>Details:</b> This method creates and initializes the
   * thread-<wbr>local database connection that will be used in the AWT event
   * thread.  This method has been implemented as a testing fixture for certain
   * unit tests, and in regular application execution its use is not
   * required.</p>
   *
   * <p>If a <code>StartupFailureException</code> is thrown, the cause is most
   * likely a <code>DBException</code> that was thrown during the connect
   * attempt.  Call the exception's <code>getCause</code> method to check.</p>
   *
   * @throws StartupFailureException if an exception occurs while connecting the
   *     database
   */
  public static void initializedDatabaseForAwtEventThread() throws StartupFailureException
  {
    try
    {
      SwingUtilities.invokeAndWait
      ( new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              DBInfo.init();
              new DBObjectTL().getDBObject().connect();
            }
            catch (final DBException ve)
            {
              throw new UndeclaredThrowableException(ve);
            }
          }
        }
      );
    }
    catch (Exception ve)
    {
      if (ve instanceof UndeclaredThrowableException)
      {
        final Throwable veCause = ve.getCause();
        if (veCause instanceof DBException)
          ve = (Exception) veCause;
      }
      throw new StartupFailureException("Database initialization for event thread failed: ", ve);
    }
  }

  /**
   * Lists daemon controllers.
   * 
   * <p><b>Details:</b> This method returns a list of the names of all 
   * controllers in the CLC that are 
   * {@linkplain DaemonController daemon controllers}.</p>
   * 
   * <p><b>Implementation note:</b> It might be better to make this method part
   * of the {@link ControllerListConfiguration} interface.  However, because the
   * implementation of this method would probably be the same for all 
   * implementations of {@link ControllerListConfiguration}, it seemed better 
   * not to burden its implementers.  Also, this compilation unit is the only 
   * place where this feature is ever required, so to promote locality, the 
   * method is kept here.</p>
   *  
   * @return the list
   */
  private static List<String> listDaemonControllers() throws StartupFailureException
  {
    List<String> vpDaemonControllers = new ArrayList<String>();
    ControllerListConfiguration vpClc = ControllerFactory.getClc();
    try
    {
      List<String> vpNames = vpClc.listControllerNames();
      for (String vsName: vpNames)
      {
        try
        {
          ControllerDefinition vpDef = vpClc.getControllerDefinition(vsName);
          String vsTypeDef = vpDef.getType();
          ControllerTypeDefinition vpTypeDef = vpClc.getControllerTypeDefinition(vsTypeDef);
          Class<? extends Controller> vtClass = vpTypeDef.getImplementingClass();
          if (DaemonController.class.isAssignableFrom(vtClass))
            vpDaemonControllers.add(vsName);
        }
        // If these happen, DON'T leave WarehouseRx comatose.  Skip this device.
        catch (ClassNotFoundException ex)
        {
          logger.logException("Exception configuring " + vsName, ex);
          System.err.println(ex.getMessage());
        }
        catch (ControllerConfigurationException ex)
        {
          logger.logException("Exception configuring " + vsName, ex);
          System.err.println(ex.getMessage());
        }
      }
    }
    catch (ControllerConfigurationException ex)
    {
      // If this happens, something is REALLY bad.  Fatal, even.
      throw new StartupFailureException(ex.getMessage());
    }
    return vpDaemonControllers;
  }
  
  /**
   * Update the AE database if this is a server and part of an integrated
   * Wynsoft solution
   */
  private static void updateAESystem()
  {
    if (Wynsoft.isIntegrated() && Wynsoft.updateAESystem())
    {
      if (ControllerListConfiguration.CLC_SERVER.equalsIgnoreCase(
          Application.getString(WarehouseRx.RUN_MODE)))
      {
        Factory.create(StandardAeSystemServer.class).updateInstanceForServerStart();
      }
    }
  }
  
  /**
   * Method for adding custom initialization
   * 
   * @throws StartupFailureException
   */
  private static void doCustomInitialization() throws StartupFailureException
  {
    Factory.create(BaseInitializer.class).initialize();
  }

  /**
   * Auto-start controllers if the config says to do so
   */
  private static void autoStartControllers() throws StartupFailureException
  {
    // Start daemon controllers:
    List<String> vpDaemons = listDaemonControllers();
    ControllerFactory.startControllers(vpDaemons);
    // Load other controllers:
    String vsLoadControllers = Application.getString(WarehouseRx.LoadControllers);
    if (vsLoadControllers != null)
    {
      vsLoadControllers = vsLoadControllers.substring(0,1);
      if (vsLoadControllers.equalsIgnoreCase("A"))
      {
        try
        {
          List<String> vpControllerList = gpClc.listControllerNames();
          if (vpControllerList != null)
          {
            SystemGateway vpSG = ThreadSystemGateway.get();
            for (String s: vpControllerList)
            {
              vpSG.publishControlEvent(
                  ControlEventDataFormat.getCommandTargetListMessage(
                      ControlEventDataFormat.CONTROLLER_STARTUP, s),
                  ControlEventDataFormat.TEXT_MESSAGE, SKDCConstants.CONTROLLER_SERVER);
            }
          }
        }
        catch (ControllerConfigurationException ve)
        {
          System.out.println("Failed Creating Controllers: " + ve);
          return;
        }
      }
    }
  }
  
  /**
   * Initializes factory.
   * 
   * <p><b>Details:</b> This method initializes the factory by registering the
   * interface/implementation associations listed in a properties file.  The
   * properties file is named by the application property:</p>
   * 
   * <blockquote>
   *   <tt>com.daifukuamerica.wrxj.factory</tt>
   * </blockquote>
   */
  public static void initializeFactory() throws FactoryException
  {
    String vsProperties = Application.getString("com.daifukuamerica.wrxj.factory");
    if (vsProperties == null)
      return;
    try
    {
      ImplementationsMap vpMap = ImplementationsMap.createFromProperties(vsProperties);
      Factory.setImplementations(vpMap);
    }
    catch (Exception ee)
    {
      // This is fatal for Warehouse Rx
      ee.printStackTrace();
      throw new FactoryException("Error initializing Factory", ee);
    }
  }
}

