package com.daifukuamerica.wrxj.web.core.connection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.application.DomainsPropertiesLayer;
import com.daifukuamerica.wrxj.application.EnvironmentPropertiesLayer;
import com.daifukuamerica.wrxj.application.PropertiesFileLayer;
import com.daifukuamerica.wrxj.application.PropertiesLayer;
import com.daifukuamerica.wrxj.application.ResourcePropertiesLayer;
import com.daifukuamerica.wrxj.application.SystemPropertiesLayer;
import com.daifukuamerica.wrxj.clc.ControllerConfigPropertiesLayer;
import com.daifukuamerica.wrxj.clc.ControllerConfigurationException;
import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ControllerListConfiguration;
import com.daifukuamerica.wrxj.clc.ControllerTypeDefinition;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerFactory;
import com.daifukuamerica.wrxj.controller.DaemonController;
import com.daifukuamerica.wrxj.dataserver.SysConfigPropertiesLayer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.factory.FactoryException;
import com.daifukuamerica.wrxj.factory.ImplementationsMap;
import com.daifukuamerica.wrxj.host.HostConfigPropertiesLayer;
import com.daifukuamerica.wrxj.jdbc.DBCommException;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBResultSet;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.swingui.main.StartupFailureException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.util.SkdRtException;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import com.daifukuamerica.wrxj.web.core.MainStartup;
import com.daifukuamerica.wrxj.web.core.listeners.WrxjConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author dystout
 * Date: Jul 20, 2016
 *
 * Description: Singelton encapsulation of WRX database connections created by WrxjConnectionListener
 * 
 * A wrxj.properties file must be placed in the src/main/resources folder in order to
 * read in WRX parameters. The WRX connection will utilize DBObject and look for the default
 * "OracleDB" object in the properties file for the connection parameters. 
 */
public class WrxjConnection 
{

	public static final String RUN_MODE = "com.skdaifuku.wrxj.mode";
	public static final String LoadControllers="com.skdaifuku.wrxj.loadControllers";
	public static final String LOAD_CONFIGS_FROM_RESOURCE="com.daifukuamerica.wrxj.loadConfigsFromResource";

	//Singleton instance
	private static WrxjConnection instance = null; 
	
	public static final String CLC_ARG="com.skdaifuku.wrxj.clc.arg";
	public static final String ALLOW_MULTIPLE_INSTANCES = "com.skdaifuku.wrxj.dup";
	public static DBObject dbObject = null;
	public static DBInfo dbInfo = new DBInfo(); 
	public static DBTrans  DBTrans = null;
	
	private static Logger logger = LoggerFactory.getLogger(WrxjConnection.class);
	private static ControllerListConfiguration gpClc;

	/**
	 * Only instantiate internally
	 */
	protected WrxjConnection()
	{
		init(); 
	}  
	
	/**
	 * Singleton pattern getInstance()
	 * 
	 * @return the only instance of WrxjConnection
	 */
	public static WrxjConnection getInstance()
	{
		if(instance == null) instance = new WrxjConnection();
        return instance;
	}
	  
	/**
	 * Initialize a WRxJ Connection with factories, translations, and controllers. 
	 */
	protected void init() 
	{
		try{
			loadApplicationConfig();
			configureStandardPropertiesStack(WrxjConnectionListener.class.getClassLoader().getResource("wrxj.properties").getPath(),false);
			initializeFactory();
			//checkPreviousInstance();
			initDBTran();
			gatherMetaData(); 
			
//			initIkeaDBTran(); // CUSTOM translations
//			gatherIkeaMetaData(); 
			
			connect(); 
		    checkDBConnection();
			initializeAutoLogSaver();
			logPropertyValues();
			initializeExpertFactory(); 
			//initializeControllerListConfiguration(); 
			loadMiscStaticData();

	        
	        
	        /**
	         * TODO - Break off into configurable option. This is the WRXJ Server-side code - if we want to run this web server as a thick client leave 
	         * commented. If we want to run this web server as the WRXJ Server uncomment this code. 
	         */
//			autoStartControllers();
//			logger.debug("*****~ Starting WebMessageController ~*****");
			//ControllerFactory.startController(DatabaseControllerTypeDefinition.WEB_MESSAGE_TYPE);
//			SystemGateway vpSG = ThreadSystemGateway.get();
/*           vpSG.publishControlEvent(
	                  ControlEventDataFormat.getCommandTargetListMessage(
	                      ControlEventDataFormat.CONTROLLER_STARTUP, DatabaseControllerTypeDefinition.WEB_MESSAGE_TYPE),
	                  ControlEventDataFormat.TEXT_MESSAGE, SKDCConstants.CONTROLLER_SERVER);
**/	                 
	                  

		}
		catch (Exception e)
	    {
			e.printStackTrace();
	      // Pretty much any exception during startup is fatal.
	      // Don't leave Warehouse Rx in a coma.
	      logger.error("{}\n{}", e.getMessage(), e.getCause());
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

	/**
	 * Closes WRxJ connection
	 */
	public void close() 
	{
		if(dbObject.checkConnected())
		{
			try 
			{
				logger.info("Disconnecting from WRXJ database...");
				dbObject.disconnect();
			} 
			catch (DBException e) 
			{
				logger.error("***Error disconnecting WRXJ database ***");
				logger.error(e.getMessage());
			
			}

		}
		LogFileReaderWriter.shutdownLogs();

	}

	/**
	 * If user is already logged in return the machine name from first associated machine name with 
	 * username so that we can log them out. 
	 * 
	 * @param username
	 * @return machineName
	 */
	public String getPreviousMachineName(String username)
	{ 
		if(dbObject.isConnectionActive())
		{ 
			String sql = "SELECT SMACHINENAME FROM LOGIN WHERE SUSERID = '" + username + "'";
			try 
			{
				DBResultSet resultSet = dbObject.execute(sql, null);
				if(resultSet.hasNext())
				{ 
					Map resultMap = (HashMap)resultSet.next(); 
					return (String)resultMap.get("SMACHINENAME"); 
				}

			} 
			catch (DBCommException | DBException e) 
			{
				e.printStackTrace();
			} 
		}
		return null; 		
	}
	
	/**
	 * Load in properties file and set connection params
	 */
	private void loadApplicationConfig()
	{ 

		logger.info("Loading WRXJ properties file... ");
		Properties properties = new Properties(); 
		
		try 
		{
			properties.load(WrxjConnectionListener.class.getClassLoader().getResourceAsStream("wrxj.properties"));
			logger.info("Successfully loaded properties file.");
		} 
		catch (IOException e) 
		{
			logger.error("Failed to Find properties file");
			logger.error(e.getMessage());
		}

		for(String name : properties.stringPropertyNames())
		{ 
			
			String value = properties.getProperty(name); 
			logger.info("APPLICATION PROPERTIES -- [{}]=[{}]", name, value);
			Application.setString(name, value);
		}
	}
	
	/**
	 * Test connection to database using DBObject
	 */
	public void connect()
	{ 
		dbObject = new DBObjectTL().getDBObject();
		if (!dbObject.checkConnected())
		{
			try 
			{
				dbObject.connect();
				logger.info("Connected to WRXJ database");
			} 
			catch (DBException e) 
			{
				logger.error("Error connecting to database");
				logger.error(e.getMessage());
			}
		}else{
			logger.info("Connected to WRXJ database!");
		}
	}
	
	/**
	 * Initializes DB Information map and stores data for retrieval later.
	 */
	private static void gatherMetaData()
	{ 
		try 
		{
			logger.info("Gathering Database metadata.. ");
			DBInfo.init();
			com.daifukuamerica.wrxj.jdbc.DBTrans.init();
		} 
		catch (DBException e) 
		{
			logger.error("There was a problem gathering the database metadata: {}", e.getMessage());
		}
	}

	
	/**
	 * Initialize factory implementations from factory.properties
	 */
    private static void initializeFactory() throws FactoryException
    {
        String vsProperties = Application.getString("com.daifukuamerica.wrxj.factory");
    	if(vsProperties == null)
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
	          logger.error("RmiServerExpertFactory \"{}\" - initializeExpertFactory()", vsClassName, e);
	        }
	      }
	    }
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
	  
	  /*--------------------------------------------------------------------------*/
	  /**
	   * 
	   */
	  private static void logPropertyValues()
	  {
	    //
	    // ALL Packages ? ...sk
	    //
	    logger.info("===== WRXJ APPLICATION START - {} =====", WrxjVersion.getSoftwareVersion());
	    String vsClcArg = Application.getString(MainStartup.CLC_ARG);
	    logger.info("Properties {}: {}", MainStartup.CLC_ARG, vsClcArg);
	  }
	  
	  
	  /**
	   * Uses properties from file.
	   * 
	   * <p><b>Details:</b> This method adds a properties layer to the properties
	   * stack containing properties read from the given file.</p>
	   * 
	   * <p>This method will throw an {@link IOException} if the properties file 
	   * cannot be read.</p>
	   * 
	   * @param isFile
	   * @throws IOException
	   */
	  private static void addPropertiesFileLayer(String isFile) throws IOException
	  {
	    PropertiesFileLayer vpLayer = new PropertiesFileLayer(isFile);
	    Application.addPropertiesLayer(vpLayer);
	  }
	  
	  /**
	   * Uses properties from a resource file.
	   * 
	   * <p><b>Details:</b> This method adds a properties layer to the properties
	   * stack containing properties read from the given resource file.</p>
	   * 
	   * <p>This method will throw an {@link IOException} if the properties file 
	   * cannot be read.</p>
	   * 
	   * @param isFile
	   * @throws IOException
	   */
	  private static void addPropertiesResourceLayer(String isFile) throws IOException
	  {
	    PropertiesLayer vpLayer = new ResourcePropertiesLayer(isFile);
	    Application.addPropertiesLayer(vpLayer);
	  }

	  /**
	   * Uses system properties.
	   * 
	   * <p><b>Details:</b> This method adds a properties layer to the properties
	   * stack containing properties read from the system properties.</p>
	   */
	  private static void addSystemPropertiesLayer()
	  {
	    SystemPropertiesLayer vpLayer = new SystemPropertiesLayer();
	    Application.addPropertiesLayer(vpLayer);
	  }

	  private static void addEnvironmentPropertiesLayer()
	  {
	    EnvironmentPropertiesLayer vpLayer = new EnvironmentPropertiesLayer();
	    Application.addPropertiesLayer(vpLayer);
	  }

	  private static void addDatabasePropertiesLayer()
	  {
	    DomainsPropertiesLayer vpDomains = new DomainsPropertiesLayer();
	    PropertiesLayer vpLayer;
	    vpLayer = new HostConfigPropertiesLayer();
	    vpDomains.addDomain("HostConfig.", vpLayer);
	    vpLayer = new ControllerConfigPropertiesLayer();
	    vpDomains.addDomain("ControllerConfig.", vpLayer);
	    vpLayer = new SysConfigPropertiesLayer();
	    vpDomains.addDomain("SysConfig.", vpLayer);

	    Application.addPropertiesLayer(vpDomains);
	  }

	  /**
	   * Load the standard properties stack
	   * @param isFile
	   * @param izLoadFromResource true for resource, false for file
	   * @throws IOException
	   */
	  private static void configureStandardPropertiesStack(String isFile,
	      boolean izLoadFromResource) throws IOException
	  {
	    addEnvironmentPropertiesLayer();
	    try
	    {
	      InetAddress vpInetAddr = InetAddress.getLocalHost();
	                                       // Sharky would hate me for this! --A.D.
	      Application.setString(SKDCConstants.MACHINE_NAME, vpInetAddr.getHostName().toLowerCase());
	      Application.setString(SKDCConstants.IPADDRESS_NAME, vpInetAddr.getHostAddress());
	    }
	    catch(UnknownHostException uhe)
	    {
	      throw new IOException("Error determining current machine information!", uhe);
	    }

	    addSystemPropertiesLayer();
	    if (izLoadFromResource)
	    {
	      addPropertiesResourceLayer(isFile);
	    }
	    else
	    {
	      addPropertiesFileLayer(isFile);
	    }
	    addDatabasePropertiesLayer();
	  }

}
