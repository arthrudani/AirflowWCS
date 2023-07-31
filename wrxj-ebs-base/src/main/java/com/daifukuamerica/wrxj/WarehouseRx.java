package com.daifukuamerica.wrxj;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.application.DomainsPropertiesLayer;
import com.daifukuamerica.wrxj.application.EnvironmentPropertiesLayer;
import com.daifukuamerica.wrxj.application.PropertiesFileLayer;
import com.daifukuamerica.wrxj.application.PropertiesLayer;
import com.daifukuamerica.wrxj.application.ResourcePropertiesLayer;
import com.daifukuamerica.wrxj.application.SystemPropertiesLayer;
import com.daifukuamerica.wrxj.clc.ControllerConfigPropertiesLayer;
import com.daifukuamerica.wrxj.dataserver.GlobalSettingsPropertiesLayer;
import com.daifukuamerica.wrxj.dataserver.SysConfigPropertiesLayer;
import com.daifukuamerica.wrxj.host.HostConfigPropertiesLayer;
import com.daifukuamerica.wrxj.swingui.main.MainStartup;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.UnreachableCodeException;
import com.wynright.wrxj.app.Wynsoft;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Warehouse Rx main class.
 * 
 * <p><b>Details:</b> This is the main executable class for Warehouse Rx.</p>
 * 
 * @author Sharky
 */
public class WarehouseRx
{
  public static final String RUN_MODE = "com.skdaifuku.wrxj.mode";
  public static final String LoadControllers="com.skdaifuku.wrxj.loadControllers";
  public static final String LOAD_CONFIGS_FROM_RESOURCE="com.daifukuamerica.wrxj.loadConfigsFromResource";

  /**
   * Hides default constructor.
   */
  private WarehouseRx()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Program entry point.
   *  
   * <p><b>Details:</b> This is the program entry point for Warehouse Rx.  For
   * information about command line arguments, see 
   * <a href="doc-files/command_line_arguments.html">Warehouse Rx Command Line 
   * Arguments</a>.</p>
   * 
   * @param iasArgs command line arguments
   * @throws Exception if an unexpected exception occurs
   */
  public static void main(String[] iasArgs) throws Exception 
  {
    splashConsole();
    processCommandLineArguments(iasArgs);
    new WrxjServer().run();
  }

  /**
   * A splash screen on the console so you know stuff is working
   */
  private static void splashConsole()
  {
    try
    {
      InputStream vpIS = WarehouseRx.class.getResourceAsStream("/configs/splash.txt");
      if (vpIS != null)
      {
        byte vab[] = new byte[80];
        String vsSplash = "";
        int vnRead = vpIS.read(vab);
        while (vnRead > 0) 
        {
          vsSplash += new String(Arrays.copyOfRange(vab, 0, vnRead));
          vnRead = vpIS.read(vab);
        }
        System.err.println(vsSplash);
        vpIS.close();
      }
    }
    catch (IOException ex)
    {
      System.err.println("Exception reading splash screen: " + ex);
    }
  }
  
  /**
   * Processes command line arguments.
   * 
   * <p><b>Details:</b> This method processes the command line arguments passed
   * into {@link #main(String[])}.  The main objective of this method is to 
   * initialize the properties stack.</p>
   * 
   * @param iasArgs the arguments
   * @throws Exception if any unrecoverable exception occurs
   */
  private static void processCommandLineArguments(String[] iasArgs) throws Exception
  {
    Iterator<String> vpIterator = Arrays.asList(iasArgs).iterator();
    while(vpIterator.hasNext())
    {
      String vsArg = vpIterator.next();
      if (vsArg.equals("-pf"))
      {
        vsArg = vpIterator.next();
        addPropertiesFileLayer(vsArg);
        continue;
      }
      if (vsArg.equals("-ps"))
      {
        addSystemPropertiesLayer();
        continue;
      }
      if (vsArg.equals("-pe"))
      {
        addEnvironmentPropertiesLayer();
        continue;
      }
      if (vsArg.equals("-pr"))
      {
        vsArg = vpIterator.next();
        addPropertiesResourceLayer(vsArg);
        continue;
      }
      if (vsArg.equals("-pd"))
      {
        addDatabasePropertiesLayer();
        continue;
      }
      if (vsArg.equals("-pa"))
      {
        vsArg = vpIterator.next();
        configureStandardPropertiesStack(vsArg, false);
        continue;
      }
      if (vsArg.startsWith("-par"))
      {
        // For some reason, the web start arguments are combined
        if (vsArg.length() > 4)
        {
          vsArg = vsArg.substring(4);
        }
        else
        {
          vsArg = vpIterator.next();
        }
        configureStandardPropertiesStack(vsArg, true);
        continue;
      }
      throw new IllegalArgumentException(vsArg);
    }

    if (!Application.arePropertiesDefined())
      configureStandardPropertiesStack("configs/wrxj.properties", false);
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
    vpDomains.addDomain(Application.HOSTCFG_DOMAIN, vpLayer);
    vpLayer = new ControllerConfigPropertiesLayer();
    vpDomains.addDomain(Application.CONTROLLERCFG_DOMAIN, vpLayer);
    vpLayer = new SysConfigPropertiesLayer();
    vpDomains.addDomain(Application.SYSCFG_DOMAIN, vpLayer);
    
    Application.addPropertiesLayer(vpDomains);
    
    // Wynsoft Global Properties
    if (Wynsoft.isIntegrated())
    {
      vpLayer = new GlobalSettingsPropertiesLayer();
      Application.addPropertiesLayer(vpLayer);
    }
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
    // Must initialize the Factory BEFORE the database layer--contains DB-specific error codes, for example
    MainStartup.initializeFactory();
    addDatabasePropertiesLayer();
  }
}

