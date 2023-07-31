package com.daifukuamerica.wrxj.swingui.main;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.DacTranslator;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCFrame;
import com.daifukuamerica.wrxj.swing.SKDCGUIConstants;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTaskBar;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.WrxjVersion;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * The primary frame in the application. The menu bar and its selections allow
 * the user access to all of the application's functionality (as determined by
 * the user's role. The static construction clause (static block) accesses the
 * "LogFileReaderWriter.autoSaveLogs()" static method which loads the
 * "LogFileReaderWriter" class which then executes its static construction
 * clause. The LogFileReaderWriter static construction clause (static block)
 * creates the application's System, Error & Transaction loggers and the
 * auto-log-savers for the application.
 * 
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements SKDCGUIConstants
{
  private static final String SCREEN_SIZE = "com.daifukuamerica.wrxj.ScreenSize";
  
  protected static String APPLICATION_TITLE = WrxjVersion.getSoftwareVersion();

  private static final String MENU_WINDOW = "Window";

  private static Logger logger = Logger.getLogger("MainFrame");
  private static String controllerGroupName = null;
  private static String upSince = null;

  // Main window components
  protected JMenuBar mpMainMenuBar = new JMenuBar();
  private JToolBar mpToolBar = new JToolBar();
  private JDesktopPane mpDesktop = new JDesktopPane();
  private SKDCTaskBar mpTaskBar = new SKDCTaskBar();
  private JTextField mpTimeText = new JTextField();
  
  // Internal Frames
  private JInternalFrame[] mapInternalFrames = null;
  
  // User data
  protected SKDCUserData mpUserData = null;

  // Clock
  private SimpleDateFormat mpSDF = null;
  private Calendar mpCal = null;
  private Timer mpClockTimer = null;
  private int mpGarbageCollectionCounter = 0;

  // System monitor
  // I don't know why these are protected and non-static.  Did some once change
  // these for a project?
  protected String msToolsCategory = "Tools";
  protected String msSystemMonitorOption = "System Monitor";

  // Button size
  int mnToolBarButtonSize = 32;

  // Menu icon size
  protected int mnMenuIconSize = 16;
  
  /**
   * Static block
   */
  static
  {
    SkDateTime tempTime = new SkDateTime("HH:mm:ss  EEE  dd-MMM-yy");
    upSince = "Up since:  " + tempTime.getCurrentDateTimeAsString();
    controllerGroupName = Application.getString(WarehouseRx.RUN_MODE);
    logger.logDebug("Controller Group Name \"" + controllerGroupName + "\"");
    controllerGroupName = controllerGroupName != null ? controllerGroupName : "Client";
    //
    // This changes the name of the "main" thread.
    //
    Thread.currentThread().setName(logger.getLoggerInstanceName());
  }

  /**
   * Create the main screen frame.
   */
  public MainFrame()
  {
    mnToolBarButtonSize = Application.getInt("ToolBarButtonSize", mnToolBarButtonSize);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    mpUserData = new SKDCUserData();
    try
    {
      jbInit();
      adjustScreenSize();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Add extra support for very small screens (less than 1024x768)
   */
  protected void adjustScreenSize()
  {
    Dimension vpScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension vpFrameSize = getPreferredSize();

    // Start at 90% of the screen
    vpFrameSize.height = vpScreenSize.height * 9 / 10;
    vpFrameSize.width = vpScreenSize.width * 9 / 10;

    // Force a certain size of screen (primarily for testing)
    String vsScreenSize = Application.getString(SCREEN_SIZE);
    if (vsScreenSize != null)
    {
      try
      {
        String[] vsDims = vsScreenSize.split(",");
        vpFrameSize.width = Integer.parseInt(vsDims[0]);
        vpFrameSize.height = Integer.parseInt(vsDims[1]);
      }
      catch (Exception e)
      {
        logger.logException("Invalid parameter: " + SCREEN_SIZE + "="
            + vsScreenSize, e);
      }
    }

    // Set the size
    setPreferredSize(vpFrameSize);
    setSize(vpFrameSize);
    pack();
    
    // Center the screen (or maximize on a small screen)
    if (vpScreenSize.width < 1000)
    {
      setExtendedState(MAXIMIZED_BOTH);
    }
    else
    {
      setLocation((vpScreenSize.width - vpFrameSize.width) / 2,
          (vpScreenSize.height - vpFrameSize.height) / 2);
    }
  }
  
  /**
   * Get the default "File" menu
   * @return
   */
  protected String[] getDefaultMenusFirst()
  {
    String[] vsDefaultMenus = {
      "File:Logout:/graphics/logout.png",
      "File:Exit:/graphics/exit.png"};
    
    return vsDefaultMenus;
  }

  /**
   * Get the default "Window" and "Help" menus 
   * @return
   */
  protected String[] getDefaultMenusLast()
  {
    String[] vsDefaultMenus = {
        MENU_WINDOW + ":Tile:/graphics/tile.png",
        MENU_WINDOW + ":Cascade:/graphics/cascade.png",
        MENU_WINDOW + ":Close Current:/graphics/closeone.png",
        MENU_WINDOW + ":Close All:/graphics/closeall.png",
        "Help:Help:/graphics/help.png",
        "Help:About Warehouse Rx:/graphics/idea.png"};

    return vsDefaultMenus;
  }

  /**
   * Method to initialize screen components. This adds the components to the
   * screen and adds listeners as needed.
   * 
   * @exception Exception
   */
  private void jbInit() throws Exception
  {
    mpToolBar.setFloatable(false);
    mpToolBar.setBorder(new EtchedBorder());
    
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setJMenuBar(mpMainMenuBar);
    setSize(new Dimension(861, 571));
    mpDesktop.setLayout(null);
    mpDesktop.setBackground(SystemColor.desktop);

    // Desktop
    Container vpDesktopPanel = getContentPane();
    vpDesktopPanel.add(mpToolBar, BorderLayout.NORTH);
    vpDesktopPanel.add(mpDesktop, BorderLayout.CENTER);
    addStatusBarToContentPane();

    setTitle(controllerGroupName);
    String vsMainIcon = Application.getString("ApplicationIcon", "/graphics/wrxj.png");
    Image vpImage = new ImageIcon(MainFrame.class.getResource(vsMainIcon)).getImage();
    setIconImage(vpImage);
    
    // set up default actions
    addDefaultMenus(getDefaultMenusFirst());

    mpSDF = new SimpleDateFormat();
    mpSDF.applyPattern(" HH:mm:ss ");
    mpCal = Calendar.getInstance();
    Action clockAction = new AbstractAction() {
      private static final long serialVersionUID = 0L;
      
      public void actionPerformed(ActionEvent e) {
        reclock();
      }
    };

    mpClockTimer = new javax.swing.Timer(1000, clockAction);
    mpClockTimer.stop();
    reclock();
  }
  
  /**
   * Add the status bar to the bottom of the screen
   * 
   * @return
   */
  protected void addStatusBarToContentPane() throws Exception
  {
    // Status panel
    SKDCLabel vpStatusInfo = new SKDCLabel();
    vpStatusInfo.setText(upSince);
    
    JPanel vpUpTimePanel = new JPanel();
    vpUpTimePanel.setBorder(BorderFactory.createEtchedBorder());
    vpUpTimePanel.add(vpStatusInfo);
    
    mpTimeText.setBorder(BorderFactory.createEtchedBorder());
    mpTimeText.setEditable(false);
    mpTimeText.setText("hh:mm");
    
    // East Side panel
    JPanel vpEastPanel = new JPanel();
    vpEastPanel.setLayout(new BorderLayout());
    
    StandardConfigurationServer mpConfigSrvr = Factory.create(StandardConfigurationServer.class);
    if (mpConfigSrvr.isSplitSystem())
    {
      // Setup to show JVM Id because we split the system
      String vsJVMId = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
      JTextField vpJVMText = new JTextField();
      vpJVMText.setBorder(BorderFactory.createEtchedBorder());
      vpJVMText.setEditable(false);
      vpJVMText.setText(vsJVMId);
      vpEastPanel.add(vpJVMText, BorderLayout.WEST);
      if (mpConfigSrvr.isThisPrimaryJVM())
      {
        vpJVMText.setBackground(Color.GREEN);
      }
      else
      {
        vpJVMText.setBackground(Color.LIGHT_GRAY);
      }
    }
    vpEastPanel.add(mpTimeText, BorderLayout.EAST);
    
    JPanel vpStatusPanel = new JPanel(new BorderLayout());
    vpStatusPanel.add(vpUpTimePanel, BorderLayout.WEST);
    vpStatusPanel.add(SKDCTaskBar.getTaskBar(), BorderLayout.CENTER);
    vpStatusPanel.add(vpEastPanel,  BorderLayout.EAST);
    
    getContentPane().add(vpStatusPanel, BorderLayout.SOUTH);
  }
  
  /**
   * Method to update the time display.
   */
  private void reclock()
  {
    mpCal.setTime(new Date());
    mpTimeText.setText(mpSDF.format(mpCal.getTime()));
    mpGarbageCollectionCounter++;
    if (mpGarbageCollectionCounter >= 1800)
    {
      //
      // Run the garbage collector every 1/2 hour.
      //
      mpGarbageCollectionCounter = 0;
      System.gc();
    }
  }

  /**
   * Overridden method so we can exit when window is closed.
   * 
   * @param e Window event
   */
  @Override
  protected void processWindowEvent(WindowEvent e)
  {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      confirmExit();
    }
  }
  
  /**
   * Method that tries to instantiate the screen class then get it to display.
   * 
   * @param menu Text for menu.
   * @param key Option.
   * @param icon Containing icon to be used.
   * @param screen Containing class.
   */
  protected void actionASRS(String menu, String key, String isTitle, 
      Image image, String screen)
  {
    if (key.equals("Exit"))
    {
      confirmExit();
    }
    else if (key.equals("Help"))
    {
      displayHelp();
    }
    else if (key.equals("About Warehouse Rx"))
    {
      about();
    }
    else if (key.equals("Login") || key.equals("Logout"))
    {
            // call logOut() because it closes everything and then call logIn()
      logOut();
      logIn();
    }
    else if (key.equals("Cascade"))
    {
      cascadeFrames();
    }
    else if (key.equals("Tile"))
    {
      tileFrames();
    }
    else if (key.equals("Close All"))
    {
      closeAllFrames();
    }
    else if (key.equals("Close Current"))
    {
      closeCurrentFrame();
    }
    else if (menu.equals(MENU_WINDOW))
    {
      // if we get here and are in the "Window" menu
      // then we are trying to bring a frame to the front
      moveFrametoFront(key);
    }
    else
    {
      if (screen.length() > 0)
      {
        try
        {
            // Take the class
            Class myScreenClass = (Class.forName(screen));
            // Instantiate the Class
            SKDCInternalFrame myScreen = (SKDCInternalFrame) Factory.create(myScreenClass);
            myScreen.setCategoryAndOption(menu, key);
            myScreen.setTitle(isTitle);
            // Now display the screen
            displayFrame(myScreen, image);
        }
        catch (ClassNotFoundException cnfe)
        {
          System.out.println("Could not find class: " + screen + " to instantiate.");
          JOptionPane.showMessageDialog(this, "Cannot open " + key 
              + " screen: Screen not found", menu + "::" + key, 
              JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception e)
        {
          System.out.println("Error with " + menu + "::" + key + " screen: " 
              + e.getMessage());
          e.printStackTrace(System.out);
          JOptionPane.showMessageDialog(this, "Error with " + menu + "::" 
              + key + " screen: " + e.getMessage(), menu + "::" + key,
              JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
          // go on with something ?????
        }
      }
      else
      {
        System.out.println("Key ["+ menu + ":" + key +"] not yet implemented.");
      }
    }
  }

  /**
   * Confirm exit and quit
   */
  private void confirmExit()
  {
    int exit = JOptionPane.showConfirmDialog(this, 
        DacTranslator.getTranslation("Are you sure you want to Shutdown/Exit Warehouse Rx") 
        + " \"" + controllerGroupName + "\"?", 
        DacTranslator.getTranslation("Confirm Warehouse Rx Shutdown/Exit"), 
        JOptionPane.YES_NO_OPTION);
    if (exit == JOptionPane.YES_OPTION) 
    {
      addLogoutTransaction();
      mpUserData.logout();
      closeAllFrames();
      dispose();
      System.exit(0);
    }
  }

  /**
   * Method to bring up the About ASRS screen.
   */
  private void about()
  {
    JDialog dlg = getAboutDialog();
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setVisible(true);
  }
  
  /**
   * Get the about box
   * @return
   */
  protected JDialog getAboutDialog()
  {
    JDialog dlg = new AboutASRS(this, "About Warehouse Rx", true);
    return dlg;
  }

  /**
   * Method to log out a user. This logs the current user off of the system,
   * removes the users menus and buttons, then puts up the login screen.
   */
  private void logOut()
  {
    adjustScreenSize();
    closeAllFrames();
    addLogoutTransaction();
    mpClockTimer.stop();
    mpUserData.logout();
    removeMenus();
  }

  /**
   * Do the actual logout 
   */
  public void addLogoutTransaction()
  {
    StandardUserServer vpUserServer = Factory.create(StandardUserServer.class, "Logout");
    try
    {
      // Log the logout transaction.
      vpUserServer.logOut(SKDCUserData.getLoginName(), SKDCUserData.getMachineName());
    }
    catch (Exception e)
    {
      // A DBCommException can occur if we lose the connection to the database
      logger.logException(e);
    }
  }

  /**
   * Method to bring up the login screen.
   */
  public void logIn()
  {
    mpClockTimer.stop();
    if (Application.getBoolean("ShowRestrictedAccessWarning", true))
    {
      JOptionPane.showMessageDialog(this, 
          Factory.create(SysConfig.class).getRestrictedAccessWarning(), 
          "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    final Login dlg = createLoginDialog();

    dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.pack();
    dlg.toFront();
    dlg.addPropertyChangeListener(
        new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
//          System.out.println("Property change: " + prop);
          if (prop.equals(FRAME_CLOSING))
          {
            MainFrame pf = (MainFrame)dlg.getParent();
            if (dlg.isExiting())  // user wants to exit program
            {
              if (SKDCUserData.isLoggedIn())
              {
                mpUserData.logout();
              }
              closeAllFrames();
              System.exit(0);
            }
            else if (dlg.loggedIn())  // login was successful reset user permissions
            {
              closeAllFrames();
              loginUser(dlg, pf);
              mpClockTimer.start();
              autoStartUserScreens(pf);
            }
               // user escaped from login dialog,
               // if there was a user name then leave permissions based on it
               // is there was no user then disable all permissions
            else if (SKDCUserData.getLoginName().length() <= 0)
            {
              if (!pf.isVisible())  // first login before main screen visible
              {
                closeAllFrames();
                System.exit(0);
              }
              pf.setTitle(controllerGroupName + " - " + APPLICATION_TITLE);
            }

            dlg.dispose(); // always close dialog
          }
      }
    });

    dlg.setVisible(true);

  }
  
  /**
   * Auto-magically start screens for a user
   * 
   * @param ipMainFrame
   */
  protected void autoStartUserScreens(MainFrame ipMainFrame)
  {
    // System Monitor
    String vsLoadControllers = Application.getString(WarehouseRx.LoadControllers);
    if (vsLoadControllers != null)
    {
      vsLoadControllers = vsLoadControllers.substring(0,1);
      if ((vsLoadControllers.equalsIgnoreCase("A")) ||
          (vsLoadControllers.equalsIgnoreCase("Y")))
      {
        if (mpUserData.optionEnabled(msToolsCategory,msSystemMonitorOption))
        {
        	// Added check to allow for custom extending MonitorFrame and still be able to autostart
        	String sMonitorFrameClass = mpUserData.getScreenClassName(msToolsCategory,
                    msSystemMonitorOption);
        	if( !sMonitorFrameClass.startsWith(SKDCGUIConstants.WRX_CUSTOM_PACKAGE))
        	{
        		sMonitorFrameClass = SKDCGUIConstants.SWINGUI_BASE_PACKAGE + sMonitorFrameClass;
        	}
          actionASRS(msToolsCategory, msSystemMonitorOption,
              DacTranslator.getTranslation(msSystemMonitorOption),
              new ImageIcon(MainFrame.class.getResource(mpUserData.getIcon(
                  msToolsCategory, msSystemMonitorOption))).getImage(),
              sMonitorFrameClass);
        }
      }
    }
  }

  /**
   * Create the login dialog
   * @return
   */
  protected Login createLoginDialog()
  {
    final Login dlg = new Login(this, "Login - " + controllerGroupName
        + " - " + APPLICATION_TITLE, true);
    return dlg;
  }

  /**
   * Log in to Warehouse Rx
   * @param dlg
   * @param pf
   */
  protected void loginUser(final Login dlg, MainFrame pf)
  {
    pf.mpUserData.login(dlg.getUserName(), dlg.getMachineName(),
        dlg.getIPAddress());
    pf.setTitle(controllerGroupName +
       " - User: " + SKDCUserData.getLoginName() +
       ",  Role: " + SKDCUserData.getRole() +
       " - " + APPLICATION_TITLE);
    logger.logOperation(LogConsts.OPR_USER,
        "Login - User \"" + SKDCUserData.getLoginName() +
        "\"   Role \"" + SKDCUserData.getRole() + "\" at Machine \"" +
        SKDCUserData.getMachineName() + ":" + SKDCUserData.getIPAddress() + "\"");

    removeMenus();
    addDefaultMenus(getDefaultMenusFirst());
    addUserMenus();
    addDefaultMenus(getDefaultMenusLast());
    resetMnemonics();
    pack();
    mpMainMenuBar.repaint();
  }
  
  /**
   * Make sure there are no duplicate mnemonics in the main menu
   */
  private void resetMnemonics()
  {
    /*
     * First go through the mnemonics for the main menu bar.
     * 
     * Don't change these
     * 0      = File
     * Last-1 = Window
     * Last   = Help 
     */
    Set<Character> vpMnemonics = new TreeSet<Character>();
    vpMnemonics.add('F');
    vpMnemonics.add('W');
    vpMnemonics.add('H');
    for (int i = 1; i < (mpMainMenuBar.getComponentCount() - 2); i++)
    {
      JMenu vpMenu = mpMainMenuBar.getMenu(i);
      Character c = (char)vpMenu.getMnemonic();
      boolean vzNeedsNewMnemonic = vpMnemonics.contains(c);
      if (vzNeedsNewMnemonic)
      {
        String vsMenuText = vpMenu.getText().toUpperCase();
        for (int j = 0; j < vsMenuText.length() && vzNeedsNewMnemonic; j++)
        {
          c = vsMenuText.charAt(j);
          if (c != ' ')
          {
            vzNeedsNewMnemonic = vpMnemonics.contains(c);
          }
        }
      }
      if (!vzNeedsNewMnemonic)
      {
        vpMnemonics.add(c);
        vpMenu.setMnemonic(c);
      }
      else
      {
        logger.logError("Unable to find unused mnemonic for " + vpMenu.getText());
      }
    }
    
    /*
     * Now do the same thing for each sub-menu
     */
    for (int i = 0; i < mpMainMenuBar.getComponentCount(); i++)
    {
      JMenu vpMenu = mpMainMenuBar.getMenu(i);
      resetSubMenuMnemonics(vpMenu);
    }
  }
  
  /**
   * Make sure there are no duplicate mnemonics in the main menu
   */
  private void resetSubMenuMnemonics(JMenu ipSubMenu)
  {
    Set<Character> vpMnemonics = new TreeSet<Character>();
    for (int i = 0; i < ipSubMenu.getMenuComponentCount(); i++)
    {
      if (ipSubMenu.getMenuComponent(i) instanceof JMenuItem)
      {
        JMenuItem vpMenu = (JMenuItem)ipSubMenu.getMenuComponent(i);
        Character c = (char)vpMenu.getMnemonic();
        boolean vzNeedsNewMnemonic = vpMnemonics.contains(c);
        if (vzNeedsNewMnemonic)
        {
          String vsMenuText = vpMenu.getText().toUpperCase();
          for (int j = 0; j < vsMenuText.length() && vzNeedsNewMnemonic; j++)
          {
            c = vsMenuText.charAt(j);
            if (c != ' ')
            {
              vzNeedsNewMnemonic = vpMnemonics.contains(c);
            }
          }
        }
        if (!vzNeedsNewMnemonic)
        {
          vpMnemonics.add(c);
          vpMenu.setMnemonic(c);
        }
        else
        {
          if (!ipSubMenu.getText().equals("Window"))
          {
            logger.logError("Unable to find unused mnemonic for " 
                + ipSubMenu.getText() + " - " + vpMenu.getText());
          }
        }
      }
    }
  }  
  
  /**
   * Method to display the SKDCInternalFrame. If an existing copy of this frame
   * exists then it will be moved to the screen front. If it does not exist it
   * will be added.
   * 
   * @param ipFrame Frame to be displayed.
   * @param icon Containing icon to be used.
   */
  private void displayFrame(SKDCInternalFrame ipFrame, Image image)
  {
    // check if already active and duplicates is not allowed, bring it to the front

    for (int x = 0; x < mpDesktop.getComponentCount() ; x++)
    {
      Component comp = mpDesktop.getComponent(x);
      if (comp instanceof JInternalFrame.JDesktopIcon)
      {
        // this is an iconified window
        JInternalFrame vpFrame = ((JInternalFrame.JDesktopIcon)comp).getInternalFrame();
        if (vpFrame.getClass().getName().equals(ipFrame.getClass().getName()))
        {
          if (vpFrame.isIcon())
          {
            if (ipFrame.getAllowDuplicateScreens())
            {
              break; // Display newly created frame.
            }
            try
            {
              vpFrame.setIcon(false);
            }
            catch (PropertyVetoException pve) {}
          }
          //
          // This IS an iconified frame and duplicates are NOT allowed.  Discard
          // the newly created frame.
          //
          String vsOriginalTitle = ipFrame.getOriginalTitle();
          if (vsOriginalTitle != null)
          {
            //
            // Restore the title in the TaskBar that the newly created frame
            // replaced.
            //
            new SKDCTaskBar().updateTaskBarTaskTitle(ipFrame, vsOriginalTitle);
          }
          ipFrame.uncreate();
          ipFrame = null;
          //
          // Show the existing Frame.
          //
          vpFrame.moveToFront();
          return;
        }
      }
      else if (comp instanceof JInternalFrame)
      {
        JInternalFrame vpFrame = (JInternalFrame)comp;
        if (vpFrame.getClass().getName().equals(ipFrame.getClass().getName()))
        {
          if (comp instanceof SKDCInternalFrame)
          {
            if ((ipFrame).getAllowDuplicateScreens())
            {
              break; // Display newly created frame.
            }
          }
          //
          // This frame is NOT at the front and duplicates are NOT allowed.  Discard
          // the newly created frame.
          //
          String vsOriginalTitle = ipFrame.getOriginalTitle();
          if (vsOriginalTitle != null)
          {
            //
            // Restore the title in the TaskBar that the newly created frame
            // replaced.
            //
            new SKDCTaskBar().updateTaskBarTaskTitle(ipFrame, vsOriginalTitle);
          }
          ipFrame.uncreate();
          ipFrame = null;
          //
          // Show the existing Frame.
          //
          try
          {
            vpFrame.setSelected(true);
          }
          catch (PropertyVetoException pve) {}
          vpFrame.moveToFront();
          return;
        }
      }
    }

    // is not already displayed so let's put it up on screen
    ipFrame.setIconifiable(true);
    ipFrame.setFrameIcon(image);

    // if this frame is wider than the screen, shrink it.
    if (ipFrame.getWidth() > mpDesktop.getWidth())
    {
      ipFrame.setSize(mpDesktop.getWidth(), ipFrame.getHeight());
    }

    ipFrame.pack();
    SKDCFrame.positionFrame(mpDesktop,ipFrame);
                                   // Add frame to the main desktop
    mpDesktop.add(ipFrame);
    mpTaskBar.addToTaskBar(ipFrame);

                                    // Make the frame visible.
//    iFrame.pack();
    ipFrame.setVisible(true);
    ipFrame.toFront();
  }

  /**
   * Method to add the default menu options.
   * 
   * @param df List of strings containing default SKDCActions
   */
  protected void addDefaultMenus(String[] df)
  {
    for(int idx = 0; idx < df.length; idx++)
    {
      StringTokenizer st = new StringTokenizer(df[idx],":");
      String cat = st.nextToken();
      String opt = st.nextToken();
      String icon = st.nextToken();

      JMenu menu = getCategory(cat);
      addOption(menu, cat, opt, MainFrame.class.getResource(icon), "");
    }
  }

  /**
   * Method to add the user menu options. The user menu options are defined by
   * the employee, role, and roleoption tables in the database.
   */
  protected void addUserMenus()
  {
    TreeSet c = mpUserData.getCategories();
    for(Iterator it = c.iterator(); it.hasNext();)
    {
      String cat = it.next().toString();
//      System.out.println("Cat: " + cat);
      JMenu userMenu = getCategory(cat);

      TreeSet opts = mpUserData.getCategoryOptions(cat);
      if (opts != null)
      {
        for(Iterator it2 = opts.iterator(); it2.hasNext();)
        {
          String opt = it2.next().toString();
          addOption(userMenu, cat, opt,
              MainFrame.class.getResource(mpUserData.getIcon(cat, opt)),
              mpUserData.getScreenClassName(cat, opt));
//          System.out.println("Option: " + opt);
        }
      }
      /*
       * Since they may not be in order after translation...
       */
      sortUserMenu(userMenu);
    }
  }

  /**
   * Sort the menu
   * @param ipMenu
   */
  private void sortUserMenu(JMenu ipMenu)
  {
    JMenu vpSortedMenu = new JMenu();
    
    while (ipMenu.getItemCount() > 0)
    {
      boolean vzAdded = false;
      JMenuItem m = ipMenu.getItem(0);
      for (int j = 0; j < vpSortedMenu.getItemCount() && !vzAdded; j++)
      {
        if (vpSortedMenu.getItem(j).getText().compareTo(m.getText()) > 0)
        {
          vpSortedMenu.insert(m, j);
          vzAdded = true;
        }
      }
      if (!vzAdded) vpSortedMenu.add(m);
    }
    while (vpSortedMenu.getItemCount() > 0)
      ipMenu.add(vpSortedMenu.getItem(0));
  }

  /**
   * Method to get a JMenu object for the specified category. It looks through
   * the existing JMenus to find the one corresponding to the category. If
   * found, it returns the JMenu otherwise it creates a new JMenu for this
   * category.
   * 
   * @param category Menu category.
   * 
   * @return JMenu object for this category.
   */
  protected JMenu getCategory(String category)
  {
    String vsTransCategory = DacTranslator.getTranslation(category);
    
      // if menu header not there then add it
    boolean exists = false;
    JMenu menu = null;
    int menuCount = mpMainMenuBar.getMenuCount();
    for (int idx2 = 0 ; idx2 < menuCount ; idx2++)
    {
      menu = (JMenu)mpMainMenuBar.getComponent(idx2);
      if (menu.getText().equals(vsTransCategory))
      {
        exists = true;
        break;
      }
    }
    if (!exists)
    {
      menu = new JMenu(vsTransCategory);
      menu.setMnemonic(vsTransCategory.charAt(0));
      mpMainMenuBar.add(menu);
      // is this is the 'Window' menu add menu listener to it
      if (category.equalsIgnoreCase(MENU_WINDOW))
      {
        menu.addMenuListener(new windowMenuListener());
      }
    }
    menu.setVisible(true);
    return menu;
  }

  /**
   * Method to find all current frames and add them as a menu option under the
   * "Window" category. This is invoked when the menu is displayed.
   */
  void addWindowsToWindowMenu()
  {
    // find all our frames and adds them to the "Window" menu
    mapInternalFrames = null;
    JMenu menu = getCategory(MENU_WINDOW);
    Map  windows = new TreeMap();
    mapInternalFrames = this.mpDesktop.getAllFrames();
    for (int i = 0; i < mapInternalFrames.length; i++)
    {
      StringBuffer name = new StringBuffer(mapInternalFrames[i].getTitle());
      if (!windows.containsKey(name.toString()))
      {
        windows.put(name.toString(), mapInternalFrames[i].getFrameIcon());
      }
      else
      {
        name.append('-');
        int sizeWithOutInt = name.length();
        int copyNbr = 2;
        name.append(copyNbr);
        while(windows.containsKey(name.toString()))
        {
          copyNbr++;
          name.delete(sizeWithOutInt, name.length());
          name.append(copyNbr);
        }
        windows.put(name.toString(), mapInternalFrames[i].getFrameIcon());
      }
    }
    if (windows.size() > 0)
    {
      menu.addSeparator();
    }
    for(Iterator it = windows.keySet().iterator(); it.hasNext();)
    {
      String name = (String)it.next();
      addOption(menu, MENU_WINDOW, name, (ImageIcon)windows.get(name), "");
    }
    resetSubMenuMnemonics(menu);
  }

  /**
   * Method to reset the "Window" category back to it defaults when the menu
   * closes.
   */
  void removeWindowsFromWindowMenu()
  {
    JMenu menu = getCategory(MENU_WINDOW);
    for (int x = menu.getItemCount(); x > 4; x--)
    {
      menu.remove(x - 1);
    }
  }

  /**
   * Menu Listener class for the "Window" menu.
   */
  private class windowMenuListener implements MenuListener
  {
    public void menuSelected(MenuEvent me)
    {
      addWindowsToWindowMenu();
    }
    public void menuDeselected(MenuEvent me)
    {
      removeWindowsFromWindowMenu();
    }
    public void menuCanceled(MenuEvent me)
    {
      // don't know when this occurs
    }
  }
  
  /**
   * Method to find a frame by it's title and bring it to the front.
   * 
   * @param title Title of frame to be displayed.
   */
  private void moveFrametoFront(String title)
  {
     // find frame and bring it to the front
    boolean exactMatch = (title.indexOf('-') < 0);
    StringBuffer startsWith = null;
    Integer copyNbr = null;
    int count = 1;
    if (!exactMatch)
    {
      startsWith = new StringBuffer(title.substring(0,title.lastIndexOf('-')));
      try
      {
        copyNbr = Integer.valueOf(title.substring(title.lastIndexOf('-') + 1, title.length()));
      }
      catch (NumberFormatException eN)
      {
        //
        // The frame title may have hyphens in it that are not related to a copy number.
        // If so, assume the frame title is unique (because if we're here we are trying
        // to convert text that is not an integer.
        //
        exactMatch = true;
        copyNbr = null;
      }
    }
    try
    {
      for (int i = 0; i < mapInternalFrames.length; i++)
      {
        if (exactMatch && (mapInternalFrames[i].getTitle().equals(title)))
        {
          if (mapInternalFrames[i].isIcon())
          {
            mapInternalFrames[i].setIcon(false);
          }
          mapInternalFrames[i].setSelected(true);
          break;
        }
        else if (!exactMatch && mapInternalFrames[i].getTitle().startsWith(startsWith.toString()))
        {
          if (copyNbr.intValue() == count)
          {
            if (mapInternalFrames[i].isIcon())
            {
              mapInternalFrames[i].setIcon(false);
            }
            mapInternalFrames[i].setSelected(true);
            break;
          }
          count++;
        }
      }
    }
    catch (PropertyVetoException pve) {}
    mapInternalFrames = null;
  }

  /**
   * Method to add an option to a menu. Creates an SKDCAction for this option,
   * adds the option to the menu, sets up mnemonics, tool tips and buttons as
   * needed.
   * 
   * @param menu JMenu object.
   * @param category Menu category.
   * @param option Menu option.
   * @param icon URL of the icon.
   * @param pScreenClass String containing class for this option.
   */
  protected void addOption(JMenu menu, String category, String option,
      URL icon, String pScreenClass)
  {
    try
    {
     addOption(menu, category, option, new ImageIcon(icon), pScreenClass);
    }
    catch (NullPointerException e)
    {
      System.out.println("MainFrame.addOption category: " + category +
                                          " - option: " + option +
                                          " - icon: " + icon +
                                          " - ScreenClass: " + pScreenClass);
    }
  }

  /**
   * Method to add an option to a menu. Creates an SKDCAction for this option,
   * adds the option to the menu, sets up mnemonics, tool tips and buttons as
   * needed.
   * 
   * @param menu JMenu object.
   * @param category Menu category.
   * @param option Menu option.
   * @param icon Icon to use.
   * @param pScreenClass String containing class for this option.
   */
  private void addOption(JMenu menu, String category, String option, ImageIcon icon, String pScreenClass)
  {
    try
    {
      String vsTransOption = DacTranslator.getTranslation(option);
      
      ImageIcon vpScaledIcon = new ImageIcon(icon.getImage().getScaledInstance(
          mnToolBarButtonSize, mnToolBarButtonSize, Image.SCALE_SMOOTH));
      SKDCAction ea = new SKDCAction(category, option, vsTransOption, 
          vpScaledIcon, pScreenClass);
      JMenuItem menuItem = new JMenuItem(ea);
      menuItem.setMnemonic(vsTransOption.charAt(0));
      // lets resize the icon so it will fit in the frame
      ImageIcon vpIcon = new ImageIcon(icon.getImage().getScaledInstance(
          mnMenuIconSize, mnMenuIconSize, Image.SCALE_SMOOTH));
      menuItem.setIcon(vpIcon);
      menu.add(menuItem);
      if (mpUserData.onButtonBar(category, option))
      {
        mpToolBar.setVisible(true);
        SKDCButton button = new SKDCButton(ea);
        button.setMargin(new Insets(0,0,0,0));
        button.setText("");
        button.setToolTipText(vsTransOption);
        mpToolBar.add(button, null);
      }
    }
    catch (NullPointerException e)
    {
      System.out.println("MainFrame.addOption category: " + category +
                                          " - option: " + option +
                                          " - icon: " + icon +
                                          " - ScreenClass: " + pScreenClass);
    }
  }

  /**
   * Method to remove all menu options, buttons, etc.
   */
  private void removeMenus()
  {
    int menuCount = mpMainMenuBar.getMenuCount();
    for (int idx = menuCount - 1; idx >= 0; idx--)
    {
      JMenu jm = (JMenu)mpMainMenuBar.getComponent(idx);
      jm.removeAll();
      jm.setVisible(false);
      mpMainMenuBar.remove(idx);
    }

    int buttonCount = mpToolBar.getComponentCount();
    for (int idx = buttonCount - 1; idx >= 0; idx--)
    {
      mpToolBar.remove(idx);
    }
    mpToolBar.setVisible(false);
  }

  /**
   * Method to close all frames. It finds all SKDCInternalFrames and closes
   * them.
   */
  public void closeAllFrames()
  {
    // find all our frames and close them
    JInternalFrame[] intFrames = this.mpDesktop.getAllFrames();
    for (int i = intFrames.length; i > 0; i--)
    {
      try
      {
        intFrames[i-1].setClosed(true);
      }
      catch (PropertyVetoException pve) {}
    }
  }

  /**
   * Method to close current frame. It finds the current SKDCInternalFrame and
   * closes it.
   */
  private void closeCurrentFrame()
  {
    // find current frame and close it
    if (mpDesktop.getSelectedFrame() != null)
    {
      try
      {
        mpDesktop.getSelectedFrame().setClosed(true);
      }
      catch (PropertyVetoException pve)
      {
      }
    }
  }

  /**
   * Method to cascade all frames. It finds all SKDCInternalFrames and tries to
   * reposition them starting at the upper left corner.
   */
  private void cascadeFrames()
  {
    int xOff = 0;
    int yOff = 0;
    int main_width, main_height;
    int child_width, child_height;

    main_width = this.getSize().width;
    main_height = this.getSize().height;

    // find all our frames and cascade them
    JInternalFrame[] intFrames = this.mpDesktop.getAllFrames();
    for (int i = intFrames.length; i > 0; i--)
    {
      try
      {
        if (intFrames[i-1].isIcon())
        {
          // this is an iconified window
            intFrames[i-1].setIcon(false);
        }

        child_width = intFrames[i-1].getSize().width;
        child_height = intFrames[i-1].getSize().height;
        if ((xOff > (main_width - child_width)) || (yOff > (main_height - child_height)))
        {
          xOff = 0;
          yOff = 0;
        }
        intFrames[i-1].setLocation( xOff, yOff);
        intFrames[i-1].toFront();
        intFrames[i-1].setSelected(true);
        xOff = xOff + 20;
        yOff = yOff + 20;
      }
      catch (PropertyVetoException pve) {}
    }
  }

  /**
   * Method to tile all frames. It finds all JInternalFrames and tries to
   * reposition them so they eack get part of the screen.
   */
  private void tileFrames()
  {
    // How many frames do we have?
    JInternalFrame[] allframes = mpDesktop.getAllFrames();
    int count = allframes.length;
    if (count == 0) return;

    // Determine the necessary grid size
    int sqrt = (int)Math.sqrt(count);
    int rows = sqrt;
    int cols = sqrt;
    if (rows*cols < count)
    {
      cols++;
      if (rows*cols < count)
      {
        rows++;
      }
    }

    // Define some initial values for size & location
    Dimension size = mpDesktop.getSize();

    int w = size.width/cols;
    int h = size.height/rows;
    int x = 0;
    int y = 0;

    // Iterate over the frames, deiconifying any iconified frames and then
    // relocating & resizing each
    for (int i=0; i<rows; i++)
    {
      for (int j=0; j<cols && ((i*cols)+j<count); j++)
      {
        JInternalFrame f = allframes[(i*cols)+j];

        if ((f.isClosed() == false) && (f.isIcon() == true))
        {
          try
          {
            f.setIcon(false);
          }
          catch (PropertyVetoException ex) {}
        }

        mpDesktop.getDesktopManager().resizeFrame(f, x, y, w, h);
        x += w;
      }
      y += h; // start the next row
      x = 0;
    }
  }

  /**
   * Do this when "Help" is selected from the "Help" menu
   */
  protected void displayHelp()
  {
    HelpLauncher.launchHelp(this);
  }
  
  /*========================================================================*/
  /* Internal Classes                                                       */
  /*========================================================================*/
  
  /**
   * An internal class to help manage screens. This class provides a common
   * definition for each screen, one place for both menu and button.
   */
  private class SKDCAction extends javax.swing.AbstractAction
  {
    private String menuTitle;
    private String key;
    private String msOption;
    private Image theImage;
    private String screenClass;

    /**
     * Create SKDC Action class.
     * 
     * @param menuText Text showing which menu this option is for.
     * @param optionText Text to define the menu option.
     * @param isDisplayOptionText Text to show up on menu option.
     * @param icon True Icon to use for menu and button.
     */
    public SKDCAction(String menuText, String optionText,
        String isDisplayOptionText, Icon icon)
    {
      super(isDisplayOptionText, icon);
      msOption = isDisplayOptionText;
      key = optionText;
      menuTitle = menuText;
      screenClass = "";
    }

    /**
     * Create SKDC Action class.
     * 
     * @param menuText Text showing which menu this option is for.
     * @param optionText Text to define the menu option.
     * @param isDisplayOptionText Text to show up on menu option.
     * @param image True Icon to use for menu and button.
     * @param screen Text defining what screen class to invoke.
     */
    public SKDCAction(String menuText, String optionText,
        String isDisplayOptionText, ImageIcon image, String screen)
    {
      this(menuText, optionText, isDisplayOptionText, image);
      theImage = image.getImage();
      if ((screen != null) && (screen.length() > 0))
      {
        if (!screen.startsWith("com."))
          screenClass = SKDCGUIConstants.SWINGUI_BASE_PACKAGE;
        screenClass = screenClass + screen;
      }
    }

    /**
     * Method that gets invoked to actually bring up the screen. It can be
     * triggered by selecting a menu option or by pressing a button on the
     * button bar.
     * 
     * @param e Action event.
     */
    public void actionPerformed(ActionEvent e)
    {
      actionASRS(menuTitle, key, msOption, theImage, screenClass);
    }
  }
}
