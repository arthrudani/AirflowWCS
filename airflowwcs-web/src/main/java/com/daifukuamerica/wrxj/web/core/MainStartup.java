/**
 * 
 */
package com.daifukuamerica.wrxj.web.core;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerFactory;
import com.daifukuamerica.wrxj.controller.DaemonController;
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
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.SkdRtException;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: dystout
 * Created : Apr 9, 2017
 * 
 * Taken from wrxj base startup. Used onContextInitialized in DBConnectionService for 
 * startup values/connections.
 * 
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
	    catch (Exception ve)
	    {
	      ve.printStackTrace();
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
	  public static void initializeCore() 
	  {
	    try
	    {
	      logger = LoggerFactory.getLogger(MainStartup.class);
	      initializeFactory();
	      checkPreviousInstance();
	      initDBTran();
	      DBInfo.init();
	      checkDBConnection();
	      loadJVMParameters();
	      initializeAutoLogSaver();
	      logPropertyValues();          // MUST occur AFTER initializeAutoLogSaver()
	      initializeExpertFactory();
	      initializeControllerListConfiguration();
	      loadMiscStaticData();
	      //autoStartControllers();
	    }
	    catch (Exception e)
	    {
	      // Pretty much any exception during startup is fatal.
	      // Don't leave Warehouse Rx in a coma.
	      logger.error("{}\n{}", e.getMessage(), e.getCause());
	    }
	  }


	  static ServerSocket gpApplicationInstance;
	  
	  /*--------------------------------------------------------------------------*/
	  /**
	   * See if we are trying to create another instance of an already running
	   * client or server.  Try opening a Server/Listen socket and if we get an
	   * exception throw an exception and exit.
	   */
	  private static void checkPreviousInstance() 
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
	       e.printStackTrace();
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
	  private static void checkDBConnection() 
	  {
	    if (!DBObject.isWRxJConnectionActive())
	    {
	      logger.error("WRX CONNECTION NOT AVAILABLE");
	    }
	  }

	 /**
	  * Method to load JVM configuration parameters if this is a split system.
	  * @throws StartupFailureException if there are database errors, or invalid
	  *         JMS topic configuration in the startup property file.
	  */
	  private static void loadJVMParameters() 
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
	        	  logger.error("System is not configured correctly for multiple JVMs. No valid topic found in startup property file.");
	          }
	        }
	        else
	        {
	        	  logger.error("System is not configured correctly for multiple JVMs. No valid topic found in startup property file.");
	        }
	      }
	    }
	    catch(DBException ex)
	    {
	     logger.error("Database error trying to find configuration info. to run system as multiple JVMs: {}", ex.getMessage());
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
	        logger.error("Error determining expected Host data format.... Database access error for HostConfig table to read \"DataType\"{}", e.getMessage());
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
	    logger.error("===== APPLICATION START - {} =====", WrxjVersion.getSoftwareVersion());
	   // logger.info(LogConsts.OPR_USER, "");

	    String vsClcArg = Application.getString(MainStartup.CLC_ARG);
//	    logger.logOperation(LogConsts.OPR_USER, "Properties " + MainStartup.CLC_ARG + ": " + vsClcArg);
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
	     logger.error("{}\n{}", vsError, ve);
	    }
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
	          logger.error("{} RmiServerExpertFactory \"{}\" - initializeExpertFactory()", e.getMessage(), vsClassName);
	        }
	      }
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
	  private static List<String> listDaemonControllers() 
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
	          logger.error("Exception configuring {}", vsName, ex);
	          System.err.println(ex.getMessage());
	        }
	        catch (ControllerConfigurationException ex)
	        {
	          logger.error("Exception configuring {}", vsName, ex);
	          System.err.println(ex.getMessage());
	        }
	      }
	    }
	    catch (ControllerConfigurationException ex)
	    {
	      // If this happens, something is REALLY bad.  Fatal, even.
	     logger.error(ex.getMessage());
	    }
	    return vpDaemonControllers;
	  }
	  
	  /**
	   * Auto-start controllers if the config says to do so
	   */
	  private static void autoStartControllers() 
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
