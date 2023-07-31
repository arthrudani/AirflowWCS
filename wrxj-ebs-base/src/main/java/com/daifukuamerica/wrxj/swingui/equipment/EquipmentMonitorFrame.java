package com.daifukuamerica.wrxj.swingui.equipment;

import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.controller.status.StatusModel;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.device.agc.AGCDeviceConstants;
import com.daifukuamerica.wrxj.device.agc.AGCMOSMessage;
import com.daifukuamerica.wrxj.device.scale.ScaleMessage;
import com.daifukuamerica.wrxj.errorcodes.api.ErrorDescriptions;
import com.daifukuamerica.wrxj.errorcodes.api.ErrorGuide;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.messageformat.loadevent.LoadEventDataFormat;
import com.daifukuamerica.wrxj.swing.DoubleClickFrame;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentGroupProperty;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentMonitorProperties;
import com.daifukuamerica.wrxj.swingui.equipment.properties.LibraryGroupProperty;
import com.daifukuamerica.wrxj.swingui.recovery.LoadRecovery;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;

/**
 * The main view into the application's Controllers' statuses and logs.  The
 * Controllers in the application can be activated, deactivated and monitored
 * from this frame.  Any Devices controlled by the application can be monitored
 * and controlled (for maintenance functions).
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EquipmentMonitorFrame extends SKDCInternalFrame
{
  // Main tab name
  public static final String MAIN_TAB = "Control Room";
  
  // Permissions data
  protected SKDCScreenPermissions mpPermissions;

  // All of our MC and MOS devices for System commands
  protected List<String> mpMCControllerList;
  protected List<String> mpMOSControllerList;

  // Handle status updates
  protected ControllerStatusObserver mpStatusObserver;
  protected List<GraphicDeviceView> mpGraphicDeviceViews;
  protected StatusModel mpEquipmentStatusModel;
  
  // SRC Tracking
  protected List<TrackingPanel> mpTrackingPanels;
  protected LoadRecovery mpLoadRecovery;
  protected String activeMOSController;
  protected String activeGroupName;
  
  // Error descriptions
  protected Map<String,ErrorDescriptions> mpErrorDescriptions;
  
  // Configurations
  protected EquipmentMonitorProperties mpEquipProperties;
  
  // The actual display 
  protected JTabbedPane mpMainTabbedPane;
  protected List<EquipmentTabPanel> mpGraphicStatusPanels;


  /**
   * Constructor
   *  
   * @param isTitle
   */
  public EquipmentMonitorFrame()
  {
    super();
    
    // Build the screen
    try
    {
      initializeClassFields();

      buildScreen();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /*========================================================================*
   *  The following are overridden methods                                  *
   *========================================================================*/
  
  /**
   *  Method invoked when frame is opened.  Construct and activate any
   *  Controllers needed by this frame.
   *
   *  @param e Internal frame event.
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);

    /*
     * Add the StatusObserver and get an update
     */
    if (getSystemGateway() != null)
    {
      logger.logDebug("Adding status observer - internalFrameOpened()");
      getSystemGateway().addObserver(MessageEventConsts.UPDATE_EVENT_TYPE,
          getSystemGateway().getAllUpdateEventSelector(), mpStatusObserver,
          false);
      /*
       * Get a full Status Update
       */
      publishControlEvent(ControlEventDataFormat.TEXT_SHM_ALL_STATUSES, 
          ControlEventDataFormat.SHM_STATUS_REQUEST, 
          SKDCConstants.SYSTEM_HEALTH_MONITOR);
    }
    else
    {
      logger.logError("UNABLE to Add status observer - System Gateway is null");
    }
  }

  /**
   * @see com.daifukuamerica.wrxj.swing.SKDCInternalFrame#shutdownFrame()
   */
  @Override
  protected void shutdownFrame()
  {
    mpEquipProperties = null;

    // Stop tracking updates
    for (TrackingPanel vpTP : mpTrackingPanels)
    {
      vpTP.shutdown();
    }
    
    logger.logDebug("Removing status observer");
    if (getSystemGateway() != null)
    {
      if (activeMOSController.length() > 0)
      {
        publishControlEvent(ControlEventDataFormat.TEXT_MOS_STOP_POLLING, 
            ControlEventDataFormat.MOS_STOP_POLLING, activeMOSController);
      }
      getSystemGateway().deleteObserver(MessageEventConsts.UPDATE_EVENT_TYPE,
          mpStatusObserver);
    }
    
    mpStatusObserver = null;
    mpEquipmentStatusModel = null;
    super.shutdownFrame();
  }

  /*========================================================================*
   *  The preceding are overridden methods                                  *
   *========================================================================*/
  /*========================================================================*
   *  The following are initialization methods                              *
   *========================================================================*/
  
  /**
   * Initialize class fields 
   */
  protected void initializeClassFields()
  {
    mpPermissions = new SKDCUserData().getOptionPermissionsByClass(
        EquipmentMonitorFrame.class);
    
    mpMCControllerList = new ArrayList<String>();
    mpMOSControllerList = new ArrayList<String>();

    // Handle status updates
    mpStatusObserver = new ControllerStatusObserver(this);
    mpGraphicDeviceViews = new ArrayList<GraphicDeviceView>();
    
    // SRC Tracking
    mpTrackingPanels = new ArrayList<TrackingPanel>();
    mpLoadRecovery = null;
    activeMOSController = "";
    activeGroupName = null;

    mpLoadRecovery = new LoadRecovery();
    mpLoadRecovery.setParentFrame(this);
    mpLoadRecovery.initialize();

    // Error descriptions
    mpErrorDescriptions = new TreeMap<String,ErrorDescriptions>();
    
    // Configurations
    mpEquipProperties = new EquipmentMonitorProperties(logger);
    
    // The actual display 
    mpGraphicStatusPanels = new ArrayList<EquipmentTabPanel>();
  }
  
  /*========================================================================*
   *  The preceding are initialization methods                              *
   *========================================================================*/
  /*========================================================================*
   *  The following are screen builder methods                              *
   *========================================================================*/
  
  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    int vnFrameHeight = mpEquipProperties.getFrameHeight();
    int vnFrameWidth = mpEquipProperties.getFrameWidth();
    setMinimumSize(new Dimension(vnFrameWidth, vnFrameHeight));
    setPreferredSize(new Dimension(vnFrameWidth, vnFrameHeight));
    setMaximizable(true);

    // Add the System menu bar if there is more than one group
    JMenuBar vpSystemMenuBar = getSystemMenuBar();
    if (vpSystemMenuBar != null)
    {
      getContentPane().add(vpSystemMenuBar, BorderLayout.NORTH);
    }
    
    // Add the TabbedPane
    mpMainTabbedPane = new JTabbedPane();
    getContentPane().add(mpMainTabbedPane, BorderLayout.CENTER);

    // Initialize our StatusModel
    mpEquipmentStatusModel = Factory.create(StatusModel.class);
    mpEquipmentStatusModel.addSiteEquipment();

    // If we are specifying tabs, draw the specified tabs
    if (mpEquipProperties.hasSpecifiedTabs())
    {
      // Initialize the specified tabs
      String[] vasTabGroups = mpEquipProperties.getTabGroups();
      for (int i = 0; i < vasTabGroups.length; i++)
      {
        String vsTabGroup = vasTabGroups[i];
        logger.logDebug("Tab Group " + i + " is " + vsTabGroup);
        
        EquipmentTabPanel vpStatusPanel = createTabPanel(vsTabGroup, i);
        mpGraphicStatusPanels.add(vpStatusPanel);
        mpMainTabbedPane.add(vsTabGroup.replace('|',' '), vpStatusPanel);
      }
    }
    else
    {
      // Initialize the main "Control Room" tab
      EquipmentTabPanel vpMainPanel = createTabPanel(MAIN_TAB, 0);
      mpGraphicStatusPanels.add(vpMainPanel);
      mpMainTabbedPane.add(MAIN_TAB, vpMainPanel);
  
      // Initialize the extra tabs
      String[] vasTabGroups = mpEquipProperties.getTabGroups();
      for (int i = 0; i < vasTabGroups.length; i++)
      {
        String vsTabGroup = vasTabGroups[i];
        logger.logDebug("Tab Group " + i + " is " + vsTabGroup);
        
        EquipmentTabPanel vpStatusPanel = createTabPanel(vsTabGroup, i + 1);
        mpGraphicStatusPanels.add(vpStatusPanel);
        mpMainTabbedPane.add(vsTabGroup, vpStatusPanel);
      }
    }
    // Add a listener for handling resize events
    addComponentListener(new TrackingPanelResizer(getPreferredSize()));

    // Special handling for completely future/non-JVM tabs
    int vnStartTab = -1;
    for (int i = 0; i < mpGraphicStatusPanels.size(); i++)
    {
      boolean vzTabIsActive = mpGraphicStatusPanels.get(i).isActive();
      if (vzTabIsActive && vnStartTab == -1)
      {
        vnStartTab = i;
      }
      
      // Hide tracking panels for any tabs that are completely future/non-JVM
      // but don't show any hidden ones.
      if (!vzTabIsActive)
      { 
          mpTrackingPanels.get(i).setVisible(false);
      }
    }
    if (vnStartTab < 0) vnStartTab = 0;
    
    // Start with the main tab (or first tab with active buttons) selected
    mpMainTabbedPane.setSelectedComponent(mpGraphicStatusPanels.get(vnStartTab));
    
    // If there is only one tab, hide the tab
    if (mpGraphicStatusPanels.size() == 1)
    {
      getContentPane().remove(mpMainTabbedPane);
      getContentPane().add(mpGraphicStatusPanels.get(0), BorderLayout.CENTER);
    }
  }
  
  /**
   * Create a tab
   * 
   * @param isTabName
   * @param inTabIndex
   * @return
   */
  protected EquipmentTabPanel createTabPanel(String isTabName, int inTabIndex)
  {
    // Create the tab
    EquipmentTabPanel vpTabPanel = Factory.create(EquipmentTabPanel.class,
        isTabName);

    // Add the groups to the tab
    Map<String, EquipmentGraphic> vpGraphicsMap = addGraphicPanelsToTab(
        vpTabPanel, inTabIndex, isTabName);
    
    // Keep track of the graphics
    GraphicDeviceView vpGDV = new GraphicDeviceView();
    vpGDV.setEquipmentGroup(isTabName);
    vpGDV.setMissingGraphic(Factory.create(PolygonButton.class));
    vpGDV.setGraphicsMap(vpGraphicsMap);
    mpGraphicDeviceViews.add(vpGDV);

    return vpTabPanel;
  }

  /**
   * Add graphic panels for a tab
   * 
   * @param inTabIndex
   * @param isGroup
   * @return
   */
  protected Map<String, EquipmentGraphic> addGraphicPanelsToTab(
      JPanel ipTabPanel, int inTabIndex, String isGroup)
  {
    logger.logDebug("Added graphic panels to " + isGroup);

    Map<String, EquipmentGraphic> vpGraphicMap = new HashMap<String, EquipmentGraphic>();
    
    /*
     * Add title, logo, and legends as necessary
     */
    addTitleOrLogoToPanel(ipTabPanel, inTabIndex); 
    addLegends(ipTabPanel, inTabIndex);

    //
    // Now add the equipment groups.
    // * all groups on overview panel
    // * only specified group on extra tab group sheets
    // See ExtraTabGroups in properties file
    //
    boolean vzLoadStatusPanelAdded = false;
    
    List<EquipmentGroupProperty> vpEquipmentList;
    
    if (mpEquipProperties.hasSpecifiedTabs())
    {
      vpEquipmentList = mpEquipProperties.getTabEquipmentGroupList(inTabIndex);
    }
    else
    {
      vpEquipmentList = mpEquipProperties.getEquipmentGroupList(inTabIndex);
    }
    if (vpEquipmentList == null)
    {
      return vpGraphicMap;
    }

    // For each Group property in the EquipmentGroup properties
    for (EquipmentGroupProperty vpEGP : vpEquipmentList)
    {
      // Only add this on the main tab or on its own tab
      if (!mpEquipProperties.hasSpecifiedTabs() && inTabIndex > 0
          && !vpEGP.msName.equalsIgnoreCase(isGroup))
      {
        continue;
      }
      
      /*
       * Add the tracking panel if necessary
       */
      if (!vzLoadStatusPanelAdded)
      {
        addTrackingPanel(ipTabPanel, inTabIndex, vpEGP.msName,
            vpEGP.manLocation[0], vpEGP.manLocation[1] + vpEGP.manSize[1]);
        vzLoadStatusPanelAdded = true;
      }
      
      /*
       * Determine whether this group can display tracking
       */
      boolean vzHasTracking = 
         (mpEquipProperties.hasMOSConnection(vpEGP.msName) &&
          mpTrackingPanels.get(inTabIndex).isVisible());
      
      /*
       * Create Panel for a graphics group.
       */
      GroupPanel vpGroupPanel = new GroupPanel(vpEGP.msName, vpEGP.manSize[0],
          vpEGP.manSize[1], vpEGP.manLocation[0], vpEGP.manLocation[1]);
      vpGroupPanel.addMouseListener(new GroupMouseAdapter());
      ipTabPanel.add(vpGroupPanel);
      
      /*
       * Find the LIBRARY graphics group to use in the panel.
       * 
       * Get a List whose entries are collections of properties for the
       * collection name: "LibraryGroup".
       */
      List<LibraryGroupProperty> vpLibraryDeviceList = 
        mpEquipProperties.getLibraryGroupList(vpEGP.msLibraryGroup, inTabIndex);
      if (vpLibraryDeviceList == null)
      {
        logger.logError("MISSING LibraryGroup \"" + vpEGP.msLibraryGroup + "\"");
        break;
      }
      
      //
      // We now have the LIBRARY graphics group to use in the panel.  Iterate
      // through its library devices to build all the graphics in the group.
      //
      for (LibraryGroupProperty vpLGP : vpLibraryDeviceList)
      {
        String vsDeviceName = vpEGP.msName + ":" + vpLGP.msName;
        EquipmentGraphic vpDeviceGraphic = addGraphicDevice(vpGroupPanel,
            vpLGP.manLocation, mpEquipProperties.getPolygon(
                vpLGP.msLibraryDevice, inTabIndex, vpLGP.manLocation),
            vsDeviceName, inTabIndex, vpEGP.msName, vzHasTracking);
        
        // Keep track of this graphic
        vpGraphicMap.put(vsDeviceName, vpDeviceGraphic);
      }
    }
    
    return vpGraphicMap;
  }

  /**
   * Add the logo to the tab panel
   * 
   * @param inPanelIndex
   */
  private void addTitleOrLogoToPanel(JPanel ipTabPanel, int inPanelIndex)
  {
    SKDCLabel vpTitleLabel = new SKDCLabel(mpEquipProperties.getTitle());
    vpTitleLabel.setFont(new Font("Helvetica", Font.BOLD, 24));
    vpTitleLabel.setOpaque(true);
    ipTabPanel.add(vpTitleLabel);
    int vnTitleX = mpEquipProperties.getTitleX();
    int vnTitleY = mpEquipProperties.getTitleY();
    setWidgetBounds(vpTitleLabel, vnTitleX, vnTitleY);
    
    URL vpLogoURL = mpEquipProperties.getLogoURL();
    if (vpLogoURL != null)
    {
      JLabel vpLogoLabel = new JLabel(new ImageIcon(vpLogoURL));
      ipTabPanel.add(vpLogoLabel);
      int vnLogoX = mpEquipProperties.getLogoX(inPanelIndex);
      int vnLogoY = mpEquipProperties.getLogoY(inPanelIndex);
      setWidgetBounds(vpLogoLabel, vnLogoX, vnLogoY);
    }
  }

  /**
   * Add the Light Tower and Equipment Monitor legends to the tab panel
   * 
   * @param ipTabPanel
   * @param inTabIndex
   */
  private void addLegends(JPanel ipTabPanel, int inTabIndex)
  {
    /*
     * Now add the Light Tower Panel (if needed).
     */
    if (mpEquipProperties.needsLightTower(inTabIndex))
    {    
      JPanel vpLightTowerPanel = Factory.create(EquipmentLightTowerPanel.class,
          mpEquipProperties);

      int vnX = mpEquipProperties.getLightTowerX(inTabIndex);
      int vnY = mpEquipProperties.getLightTowerY(inTabIndex);

      ipTabPanel.add(vpLightTowerPanel);
      setWidgetBounds(vpLightTowerPanel, vnX, vnY);
    }
    
    /*
     * Now add the Legend Panel (if needed).
     */
    if (mpEquipProperties.needsLegend(inTabIndex))
    {    
      JPanel vpLegendPanel = Factory.create(EquipmentLegendPanel.class, 
          mpEquipProperties);
      
      int vnX = mpEquipProperties.getLegendX(inTabIndex);
      int vnY = mpEquipProperties.getLegendY(inTabIndex);
      
      ipTabPanel.add(vpLegendPanel);
      setWidgetBounds(vpLegendPanel, vnX, vnY);
    }
  }

  /**
   * Resize the TrackingPanel(s) when we resize the frame
   * 
   * @see java.awt.event.ComponentListener
   */
  public class TrackingPanelResizer extends ComponentAdapter
  {
    Dimension mpInitialSize;
    
    public TrackingPanelResizer(Dimension ipInitialSize)
    {
      mpInitialSize = ipInitialSize;
    }
    
    @Override
    public void componentResized(ComponentEvent e)
    {
      if (mpEquipProperties == null)
      {
        return;
      }

      Dimension vpNewSize = getSize();
      int vnWidthChange = Math.max(vpNewSize.width - mpInitialSize.width, 0);
      int vnHeightChange = Math.max(vpNewSize.height - mpInitialSize.height, 0);
      
      int vnTrackingWidth = mpEquipProperties.getTrackingPanelWidth()
          + vnWidthChange;
      int vnTrackingHeight = mpEquipProperties.getTrackingPanelHeight()
          + vnHeightChange;
      
      for (TrackingPanel vpTP : mpTrackingPanels)
      {
        vpTP.resizeScrollPane(new Dimension(vnTrackingWidth, vnTrackingHeight));
      }
    }
  }
  
  /**
   * Add the tracking panel
   *   
   * @param ipTabPanel
   * @param inTabIndex
   * @param isGroup
   * @param inTrackingPanelX - suggested X position for extra tabs
   * @param inTrackingPanelY - suggested Y position for extra tabs
   * @return
   */
  private void addTrackingPanel(JPanel ipTabPanel, int inTabIndex,
      String isGroup, int inTrackingPanelX, int inTrackingPanelY)
  {
    if (doesTabHaveMOSConnection(inTabIndex, isGroup))
    {
      if (inTabIndex == 0 || mpEquipProperties.hasSpecifiedTabs())
      {
        inTrackingPanelX = mpEquipProperties.getTrackingPanelX();
        inTrackingPanelY = mpEquipProperties.getTrackingPanelY();
      }
      TrackingPanel vpTrackingPanel = new TrackingPanel(this, isGroup,
          mpEquipProperties, mpPermissions, mpEquipmentStatusModel, logger);
      mpTrackingPanels.add(vpTrackingPanel);
      ipTabPanel.add(vpTrackingPanel);
      setWidgetBounds(vpTrackingPanel, inTrackingPanelX, inTrackingPanelY);
      if (mpEquipProperties.hasTabGroups() && (inTabIndex == 0) && 
          !mpEquipProperties.hasSpecifiedTabs())
      {
        vpTrackingPanel.setVisible(false);
        vpTrackingPanel.setEnabled(false);
      }
    }
    else if (inTabIndex > 0)
    {
      TrackingPanel vpLoadStatusPanel = new TrackingPanel(this, "Filler"
            + inTabIndex, mpEquipProperties, mpPermissions,
            mpEquipmentStatusModel, logger);
      mpTrackingPanels.add(vpLoadStatusPanel);
    }
  }
  
  /**
   * Determine whether a tab has a MOS connection
   * @param isGroup
   * @return
   */
  protected boolean doesTabHaveMOSConnection(int inTabIndex, String isGroup)
  {
    if (mpEquipProperties.hasSpecifiedTabs())
    {
      if (inTabIndex == 0)
        return mpEquipProperties.anyMOSConnections();
      else
        return mpEquipProperties.hasMOSConnection(mpEquipProperties.getTabGroups()[inTabIndex]);
    }
    else
    {
      if (inTabIndex == 0)
        return mpEquipProperties.anyMOSConnections();
      else
        return mpEquipProperties.hasMOSConnection(isGroup);
    }
  }

  /**
   * Add a graphic to a group panel
   * 
   * @param ipGroupPanel
   * @param xyOrigin
   * @param ipEquipmentShape
   * @param isDeviceName
   * @param inStatusPanelIndex
   * @param isGroupName
   * @param izHasTracking
   * @return
   */
  private EquipmentGraphic addGraphicDevice(GroupPanel ipGroupPanel,
      int[] xyOrigin, Polygon ipEquipmentShape, String isDeviceName,
      int inStatusPanelIndex, String isGroupName, boolean izHasTracking)
  {
    try
    {
      // Get the other attributes for the graphic
      Map<String,String> vpProperties = mpEquipmentStatusModel.get(isDeviceName);
      if (vpProperties == null)
      {
        throw new RuntimeException("Missing properties for " + isDeviceName);
      }
      // Get the appropriate class for the error set
      String vsErrorSet = vpProperties.get(StatusModel.ERROR_SET);
      String vsErrorClass = mpEquipProperties.getErrorClass(vsErrorSet);
      initializeErrorSet(vsErrorClass);
      vpProperties.put(PolygonButton.ERROR_CLASS, vsErrorClass);
      
      // Build the graphic
      EquipmentGraphic vpDeviceGraphic = Factory.create(
          mpEquipProperties.getEquipmentGraphicClass(isDeviceName), this,
          inStatusPanelIndex, ipGroupPanel, isGroupName, isDeviceName,
          ipEquipmentShape, vpProperties, izHasTracking, mpPermissions);
      
      // Add it to the screen
      ipGroupPanel.add(vpDeviceGraphic);
      setWidgetBounds((JComponent)vpDeviceGraphic, xyOrigin[0], xyOrigin[1]);

      // Keep track of MC and MOS devices
      if (!vpDeviceGraphic.isFutureExpansion() && vpDeviceGraphic.isAssignedJVM())
      {
        String vsMcController = vpProperties.get(StatusModel.MC_CONTROLLER);
        String vsMosController = vpProperties.get(StatusModel.MOS_CONTROLLER);
        if (!vsMosController.equals(StatusModel.NO_VALUE))
        {
          if (!mpMOSControllerList.contains(vsMosController))
          {
            mpMOSControllerList.add(vsMosController);
          }
        }
        // Only add the MC controller if we didn't add the MOS controller
        else if (!vsMcController.equals(StatusModel.NO_VALUE))
        {
          if (!mpMCControllerList.contains(vsMcController))
          {
            mpMCControllerList.add(vsMcController);
          }
        }
      }
      return vpDeviceGraphic;
    }
    catch (Exception e)
    {
      logger.logException("Error adding graphic: " + isDeviceName, e);
      return null;
    }
  }

  /**
   * Initialize an error set
   * @param isErrorClass
   */
  private void initializeErrorSet(String isErrorClass)
  {
    if (isErrorClass != null)
    {
      if (!mpErrorDescriptions.containsKey(isErrorClass))
      {
        try
        {
          ErrorDescriptions vpErrorDescriptions = 
            (ErrorDescriptions)Class.forName(isErrorClass).getDeclaredConstructor().newInstance();
          mpErrorDescriptions.put(isErrorClass, vpErrorDescriptions);
        }
        catch (Exception e)
        {
          logger.logException("No Error Set for " + isErrorClass, e);
        }
      }
    }
  }
  
  /*========================================================================*
   *  The following methods are for XYLayout replacement                    *
   *========================================================================*/
  
  /**
   * Set location and bounds for placement on a JPanel with a null Layout
   * 
   * @param ipWidget
   * @param inX
   * @param inY
   */
  protected void setWidgetBounds(JComponent ipWidget, int inX, int inY)
  {
    ipWidget.setBounds(new Rectangle(ipWidget.getPreferredSize()));
    ipWidget.setLocation(inX, inY);
  }
  
  /*========================================================================*
   *  The following methods are for XYLayout replacement                    *
   *========================================================================*/
  /*========================================================================*
   *  The following methods/classes are for status/tracking updates         *
   *========================================================================*/
  
  /**
   * Observer class to catch update events
   */
  public class ControllerStatusObserver implements Observer
  {
    SKDCInternalFrame mpParentFrame;
    
    public ControllerStatusObserver(SKDCInternalFrame ipFrame)
    {
      mpParentFrame = ipFrame;
    }

    @Override
    public void update(Observable o, Object arg)
    {
      ObservableControllerImpl observableData = (ObservableControllerImpl)o;
      int statusType = observableData.getIntData();
      switch (statusType)
      {
        case ControllerConsts.CONTROLLER_STATUS:
          break;
        case ControllerConsts.EQUIPMENT_STATUS:
          for (GraphicDeviceView vpGDV : mpGraphicDeviceViews)
          {
            vpGDV.update(o, arg);
          }
          updateTabStatus();
          break;
        case ControllerConsts.TRACKING_STATUS:
          synchronized (mpTrackingPanels)
          {
            if (mpTrackingPanels.size() > 1)
              mpTrackingPanels.get(mpMainTabbedPane.getSelectedIndex()).update(o, arg);
            else
              mpTrackingPanels.get(0).update(o, arg);
          }
          break;
        default:
          logger.logError("UNKNOWN StatusType: " + (char) statusType
              + " - ControllerStatusObserver.update()");
          break;
      }
      mpParentFrame.repaint();
    }
  }
  
  /*========================================================================*
   *  The preceding methods/classes are for status/tracking updates         *
   *========================================================================*/
  /*========================================================================*
   *  The following methods/classes are for tab updates                     *
   *========================================================================*/

  /**
   * Mouse listener for GroupPanel
   */
  protected class GroupMouseAdapter extends MouseAdapter
  {
    @Override
    public void mouseClicked(MouseEvent e)
    {    	
      if (!SwingUtilities.isRightMouseButton(e))
      {
        // Check for mouse double-click.
        if (e.getClickCount() == 2)
        {
          String vsGroupPanelName = ((JPanel)e.getSource()).getName();
          JPanel vpTabPanel = ((JPanel)((JPanel)e.getSource()).getParent());
          String vsTabPanelName = vpTabPanel.getName();
          /*
           * If we are on the overview / Main panel, bring corresponding group
           * panel to front otherwise bring the overview /main panel to front
           */
          if (vsGroupPanelName.equalsIgnoreCase(vsTabPanelName))
          {
            mpMainTabbedPane.setSelectedIndex(0);
          }
          else
          {
            for (int i = 1; i < mpGraphicStatusPanels.size(); i++)
            {
              if (mpGraphicStatusPanels.get(i).getName().equalsIgnoreCase(
                  vsGroupPanelName))
              {
                mpMainTabbedPane.setSelectedIndex(i);
              }
            }
          }
        }
      }
    }
  }
  
  /**
   * Update the text color on the tab to reflect the status of its graphics
   */
  protected void updateTabStatus()
  {
    // The tabbed pane may have been removed if there was only one tab
    if (mpMainTabbedPane.getComponentCount() > 0)
    {
      for (int i = 0; i < mpGraphicStatusPanels.size(); i++)
      {
        mpMainTabbedPane.setForegroundAt(i,
            mpGraphicStatusPanels.get(i).getTabStatusColor());
      }
    }
  }
  
  /*========================================================================*
   *  The preceding methods/classes are for tab updates                     *
   *========================================================================*/
  /*========================================================================*
   *  The following methods are for the tracking pop-up                     *
   *========================================================================*/
  
  /**
   * Recover a load in Warehouse Rx
   * 
   * @param isLoadID
   * @param barCode
   * @param fromStation
   * @param iHeight
   */
  public void recoverDBLoad(String loadId, String barCode, String fromStation, int iHeight)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, "Load ID \"" + loadId + "\" Recover");

    if ((loadId.length() == 0) && (barCode.equals(AGCDeviceConstants.AGCDUMMYLOAD)))
    {
      //
      // We have missed the "Dummy" load arrival.
      //
      if (displayYesNoPrompt("Create \"Dummy Arrival\" for Load ID \"" + barCode + "\""))
      {
        if (fromStation != null)
        {
          mpLoadRecovery.recoverArrival(loadId, barCode, fromStation, iHeight);
        }
        else
        {
          logger.logOperation(LogConsts.OPR_USER, "Load ID \"" + loadId
              + "\" \"Dummy\" arrival NOT created - NO From Station");
          displayInfoAutoTimeOut("Load ID \"" + loadId
              + "\" \"Dummy\" arrival NOT created - NO From Station");
        }
        return;
      }
    }
    mpLoadRecovery.recoverLoad(loadId);
  }
  
  /**
   * Delete a load in Warehouse Rx
   * 
   * @param isLoadID
   */
  public void deleteDBLoad(String isLoadID)
  {
    if (displayYesNoPrompt("Delete Load ID \"" + isLoadID + "\" from WRx"))
    {
      logger.logOperation(LogConsts.OPR_DEVICE, "Load ID \"" + isLoadID
          + "\" Delete DB Load");
      mpLoadRecovery.deleteLoad(isLoadID);
      logger.logOperation(LogConsts.OPR_USER, "Load ID \"" + isLoadID
          + "\" Deleted from DB");
      displayInfoAutoTimeOut("Load ID \"" + isLoadID + "\" deleted");
    }
  }

  /**
   * Delete SRC tracking
   * 
   * @param loadId
   * @param machineId
   * @param eqLoadStatus
   */
  public void deleteTracking(String loadId, String machineId, String eqLoadStatus)
  {
    if (eqLoadStatus.equals("None"))
    {
      displayInfo("LoadId \"" + loadId
              + "\" CANNOT be deleted from SRC\n(No SRC Load Status)",
              "Delete Load From SRC");
      return;
    }

    logger.logOperation(LogConsts.OPR_DEVICE, "LoadId \"" + loadId + "\" Delete SRC Load Y/N");
    int reply = JOptionPane.showConfirmDialog(null,
                                              "Delete LoadId: \"" + loadId + "\" ?",
                                              "Delete Equipment Load Tracking Data",
                                              JOptionPane.YES_NO_OPTION);
    if (reply == JOptionPane.YES_OPTION)
    {
      //
      // Delete the Load Tracking Record at this row
      //
      logger.logOperation(LogConsts.OPR_DEVICE, "LoadId \"" + loadId + "\" " + " Delete Load Tracking " +
                            activeGroupName + " - " + machineId);
      /*...sk Patch this out in "Baseline" until we test it
      String vsBarcode = (String)getLoadStatusScrollPane().getValueAt(loadStatusViewSelectedRow, LoadStatusScrollPane.BAR_CODE_COLUMN);
      if ((loadId.length() > vsBarcode.length()) &&
          (vsBarcode.length() > 0))
      {
        //
        // loadId longer than TrackingId, Device needs the shorter TrackingId.
        //
        loadId = vsBarcode;
      }*/
      publishControlEvent(
          ControlEventDataFormat.getMosTrackingDeleteCommand(machineId, loadId), 
          ControlEventDataFormat.MOS_DELETE_TRACK, activeMOSController);
    }
  }
  
  /*========================================================================*
   *  The preceding methods are for the tracking pop-up                     *
   *========================================================================*/
  /*========================================================================*
   *  The following methods are for the menu bar                            *
   *========================================================================*/

  /**
   * Get the system menu bar
   * 
   * @return
   */
  protected JMenuBar getSystemMenuBar()
  {
    // Only show the menu bar if there are multiple equipment groups
    if (!mpEquipProperties.hasMultipleEquipmentGroups())
    {
      return null;
    }
    
    JMenuBar vpSystemMenuBar = new JMenuBar();
    JMenu vpMenuSystem = new JMenu("System");
    
    JMenuItem vpMenuItem = new JMenuItem("Start System", 'S');
    vpMenuItem.addActionListener(new SystemStart());
    vpMenuSystem.add(vpMenuItem);
    
    vpMenuItem = new JMenuItem("Stop System", 't');
    vpMenuItem.addActionListener(new SystemStop());
    vpMenuSystem.add(vpMenuItem);
    
    if (mpEquipProperties.anyMOSConnections())
    {
      vpMenuSystem.addSeparator();
      
      vpMenuItem = new JMenuItem("Reset Errors", 'E');
      vpMenuItem.addActionListener(new SystemReset());
      vpMenuSystem.add(vpMenuItem);

      vpMenuItem = new JMenuItem("Silence Alarms", 'A');
      vpMenuItem.addActionListener(new SystemSilence());
      vpMenuSystem.add(vpMenuItem);
    }
    vpMenuSystem.setEnabled(mpPermissions.iModifyAllowed);
    vpSystemMenuBar.add(vpMenuSystem);

    return vpSystemMenuBar;
  }

  /**
   * Start the whole system
   */
  private class SystemStart implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      logger.logOperation(LogConsts.OPR_DEVICE, SKDCUserData.getLoginName()
          + " ====>>> Start System");

      // MC Port
      for (String vsMCController : mpMCControllerList)
      {
        publishControlEvent("" + ControlEventDataFormat.CHAR_START_DEVICE, 
            ControlEventDataFormat.TEXT_MESSAGE, vsMCController);
      }
      
      // MOS Port
      for (String vsMOSController : mpMOSControllerList)
      {
        publishControlEvent(ControlEventDataFormat.TEXT_MOS_START_EQUIPMENT, 
            ControlEventDataFormat.MOS_START_AISLE, vsMOSController);
      }
    }
  }
  
  /**
   * Stop the whole system
   */
  protected class SystemStop implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (displayYesNoPrompt("Are you sure you want to stop the entire system"))
      {
        logger.logOperation(LogConsts.OPR_DEVICE, SKDCUserData.getLoginName()
            + " ====>>> Stop System");

        // MOS Port
        for (String vsMOSController : mpMOSControllerList)
        {
          publishControlEvent(ControlEventDataFormat.TEXT_MOS_STOP_EQUIPMENT, 
              ControlEventDataFormat.MOS_STOP_AISLE, vsMOSController);
        }

        // MC Port
        for (String vsMCController : mpMCControllerList)
        {
          publishControlEvent("" + ControlEventDataFormat.CHAR_STOP_DEVICE, 
              ControlEventDataFormat.TEXT_MESSAGE, vsMCController);
        }
      }
    }
  }

  /**
   * Reset errors throughout the whole system
   */
  protected class SystemReset implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      logger.logOperation(LogConsts.OPR_DEVICE, SKDCUserData.getLoginName()
          + " ====>>> Reset ALL Errors");
      for (String vsMosController : mpMOSControllerList)
      {
        publishControlEvent(ControlEventDataFormat.TEXT_MOS_RESET_ERROR, 
            ControlEventDataFormat.MOS_RESET_ERROR, vsMosController);
      }
    }
  }

  /**
   * Reset errors throughout the whole system
   */
  private class SystemSilence implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
      logger.logOperation(LogConsts.OPR_DEVICE, SKDCUserData.getLoginName()
          + " ====>>> Silence ALL Alarms");
      for (String vsMosController : mpMOSControllerList)
      {
        publishControlEvent(ControlEventDataFormat.TEXT_MOS_SILENCE_ALARM, 
            ControlEventDataFormat.MOS_SILENCE_ERROR, vsMosController);
      }
    }
  }
  
  /*========================================================================*
   *  The preceding methods are for the menu bar                            *
   *========================================================================*/
  /*========================================================================*
   *  The following methods are for the graphic pop-up options              *
   *========================================================================*/
  
  /**
   * Can an error class display error help?
   * 
   * @param isErrorClass
   * @return
   */
  public boolean doesErrorClassHaveHelp(String isErrorClass)
  {
    if (isErrorClass == null)
    {
      return false;
    }
    return (mpErrorDescriptions.get(isErrorClass) instanceof ErrorGuide);
  }

  /**
   * Get the Device Type for a given group.  Default is "AGC".
   * @param isGroupName
   * @return
   */
  public String getDeviceTypeForGroup(String isGroupName)
  {
    return mpEquipProperties.getDeviceTypeForGroup(isGroupName);
  }
  
  /*========================================================================*
   *  The preceding methods are for the graphic pop-up options              *
   *========================================================================*/
  /*========================================================================*
   *  The following methods are for the graphic pop-up actions              *
   *========================================================================*/

  /**
   * Turn on tracking
   * 
   * @param inPanelIndex
   * @param isGroupName
   * @param isMOSController
   * @param ipDevicePanel
   */
  public void trackingOn(int inPanelIndex, String isGroupName, 
      String isMOSController, JComponent ipDevicePanel)
  {
    // if we are trying to turn the same group on again, just return
    if (activeGroupName != null && activeGroupName.equalsIgnoreCase(isGroupName))
    {
      return;
    }
    
    //
    // Stop status polling for the current active device group.
    //
    if (activeMOSController.length() > 0)
    {
      publishControlEvent(ControlEventDataFormat.TEXT_MOS_STOP_POLLING, 
          ControlEventDataFormat.MOS_STOP_POLLING, activeMOSController);
      
      for (TrackingPanel vpTP : mpTrackingPanels)
      {
        vpTP.deactivate();
      }
    }
    
    // Re-Selecting the same one to turn on again
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName + " Start Status Polling");
    activeMOSController = isMOSController;
    activeGroupName = isGroupName;
    mpTrackingPanels.get(inPanelIndex).activate(isGroupName, ipDevicePanel, inPanelIndex);

    //
    //  Get a full Status Update for the newly selected device group.
    //
    publishControlEvent(ControlEventDataFormat.TEXT_SHM_ALL_STATUSES, 
        ControlEventDataFormat.SHM_STATUS_REQUEST, 
        SKDCConstants.SYSTEM_HEALTH_MONITOR);
    publishControlEvent(ControlEventDataFormat.TEXT_MOS_START_POLLING, 
        ControlEventDataFormat.MOS_START_POLLING, isMOSController);
  }
  
  /**
   * Turn off tracking
   * 
   * @param isGroupName
   * @param isMOSController
   */
  public void trackingOff(String isGroupName, String isMOSController)
  {
    /*
     * If we're not tracking this one anyway, just return
     */
    if (!activeMOSController.equals(isMOSController)) return;

    //
    // Stop status polling for the current active device group.
    //
    if (activeMOSController.length() > 0)
    {
      publishControlEvent(ControlEventDataFormat.TEXT_MOS_STOP_POLLING, 
          ControlEventDataFormat.MOS_STOP_POLLING, activeMOSController);
    }
    
    //
    // Re-Selecting the current active group deselects it and stops
    // status polling.
    //
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName + " Stop Status Polling");
    activeMOSController = "";
    activeGroupName = null;
    for (TrackingPanel vpTP : mpTrackingPanels)
    {
      vpTP.deactivate();
    }
  }

  /**
   * Display error text
   * 
   * @param isDeviceName
   * @param isErrorClass
   * @param isErrorCode
   */
  public void displayErrorText(String isDeviceName, String isErrorClass,
      String isErrorCode)
  {
    isErrorCode = (String) JOptionPane.showInputDialog(this,
        "Enter Error Code Number", "Error Code Text Selection",
        JOptionPane.QUESTION_MESSAGE, null, null, isErrorCode);
    if (isErrorCode != null)
    {
      isErrorCode = isErrorCode.trim();
      if (isErrorCode.length() > 0)
      {
        String vsErrorText = 
          mpErrorDescriptions.get(isErrorClass).getDescription(isErrorCode);
        if (vsErrorText != null)
        {
          JOptionPane.showMessageDialog(this, "Error Code: " + isErrorCode
              + "\nDescription: " + vsErrorText, isDeviceName
              + "  -  Error Text", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
          JOptionPane.showMessageDialog(this,
              "No Text Description Is Available For Error Code: " + isErrorCode,
              isDeviceName + "  -  Error Text", JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }
  }

  /**
   * Display error help
   * 
   * @param isDeviceName
   * @param isErrorClass
   * @param isErrorCode
   */
  public void displayErrorHelp(String isDeviceName, String isErrorClass,
      String isErrorCode)
  {
    isErrorCode = (String) JOptionPane.showInputDialog(this,
        "Enter Error Code Number", "Error Code Help Selection",
        JOptionPane.QUESTION_MESSAGE, null, null, isErrorCode);
    if (isErrorCode != null)
    {
      isErrorCode = isErrorCode.trim();
      if (isErrorCode.length() > 0)
      {
        String errorHelp = null;
        URL errorUrl = null;
        ErrorDescriptions vpErrorDescriptions = mpErrorDescriptions.get(isErrorClass);
        if (vpErrorDescriptions instanceof ErrorGuide)
          errorUrl = ((ErrorGuide) vpErrorDescriptions).getGuide(isErrorCode);
        else
          errorHelp = vpErrorDescriptions.getDescription(isErrorCode);
        if ((errorHelp != null) || (errorUrl != null))
        {
          DoubleClickFrame doubleClickFrame = new DoubleClickFrame(
              isDeviceName + "  -  Help For Error Code: " + isErrorCode);
          if (errorUrl != null)
          {
            doubleClickFrame.setData(errorUrl);
          }
          else
          {
            doubleClickFrame.setData(errorHelp, true);
          }
          Dimension dimension = new Dimension(900, 500);
          doubleClickFrame.setPreferredSize(dimension);
          addSKDCInternalFrame(doubleClickFrame);
        }
        else
        {
          JOptionPane.showMessageDialog(this,
              "No Help Is Available For Error Code: " + isErrorCode,
              isDeviceName + "  -  Error Text", JOptionPane.INFORMATION_MESSAGE);
        }
      }
    }
  }

  /**
   * Start an aisle
   * 
   * @param isGroupName
   * @param isMOSController
   */
  public void startAisle(String isGroupName, String isMOSController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Start Aisle");
    publishControlEvent(ControlEventDataFormat.TEXT_MOS_START_EQUIPMENT, 
        ControlEventDataFormat.MOS_START_AISLE, isMOSController);
  }

  /**
   * Stop an aisle
   * 
   * @param isGroupName
   * @param isMOSController
   */
  public void stopAisle(String isGroupName, String isMOSController,
      String isMCController)
  {
    if (displayYesNoPrompt("Are you sure you want to stop " + isGroupName + "-"
        + isMCController))  // MC Controller is more human-readable
    {
      logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
          + " ====>>> Stop Aisle");
      publishControlEvent(ControlEventDataFormat.TEXT_MOS_STOP_EQUIPMENT, 
          ControlEventDataFormat.MOS_STOP_AISLE, isMOSController);
    }
  }

  /**
   * Start equipment
   * 
   * @param isGroupName
   * @param isMOSController
   * @param isMachineId
   */
  public void startEquipment(String isGroupName, String isMOSController,
      String isMachineId)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Start Equipment");
    isMachineId = isMachineId.substring(isMachineId.indexOf(':') + 1);
    publishControlEvent(isMachineId, ControlEventDataFormat.MOS_START_EQUIP,
        isMOSController);
  }

  /**
   * Stop equipment
   * 
   * @param isGroupName
   * @param isMOSController
   * @param isMachineId
   */
  public void stopEquipment(String isGroupName, String isMOSController,
      String isMachineId, String isHumanReadable)
  {
    if (displayYesNoPrompt("Are you sure you want to stop " + isHumanReadable))
    {
      logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
          + " ====>>> Stop Equipment");
      isMachineId = isMachineId.substring(isMachineId.indexOf(':') + 1);
      publishControlEvent(isMachineId, ControlEventDataFormat.MOS_STOP_EQUIP,
          isMOSController);
    }
  }

  /**
   * Reset errors for a device
   * 
   * @param isGroupName
   * @param isMachineId
   * @param isMOSController
   */
  public void resetError(String isGroupName, String isMachineId, String isMOSController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Reset Error");
    isMachineId = isMachineId.substring(isMachineId.indexOf(':') + 1);
    publishControlEvent(isMachineId, 
        ControlEventDataFormat.MOS_RESET_ERROR, isMOSController);
  }

  /**
   * Silence alarms for a device
   * 
   * @param isGroupName
   * @param isMOSController
   */
  public void silenceAlarms(String isGroupName, String isMOSController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Silence Alarm");
    publishControlEvent(ControlEventDataFormat.TEXT_MOS_SILENCE_ALARM,
        ControlEventDataFormat.MOS_SILENCE_ERROR, isMOSController);
  }

  /**
   * Request a change to retrieve mode
   * 
   * @param isDeviceName
   * @param isStation
   * @param isMCController
   */
  public void requestRetrieveMode(String isDeviceName, String isStation,
      String isMCController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, groupName
        + " ====>>> Retrieve Mode Request");
    publishControlEvent(ControlEventDataFormat.getModeChangeCommand(
        ControlEventDataFormat.CHAR_RETRIEVE_MODE, isStation),
        ControlEventDataFormat.TEXT_MESSAGE, isMCController);
  }

  /**
   * Request a change to store mode
   * 
   * @param isDeviceName
   * @param isStation
   * @param isMCController
   */
  public void requestStoreMode(String isDeviceName, String isStation,
      String isMCController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, groupName
        + " ====>>> Store Mode Request");
    publishControlEvent(ControlEventDataFormat.getModeChangeCommand(
        ControlEventDataFormat.CHAR_STORE_MODE, isStation),
        ControlEventDataFormat.TEXT_MESSAGE, isMCController);
  }

  /**
   * Disconnect a piece of equipment
   * 
   * @param isGroupName
   * @param isMachineId
   * @param isMOSController
   */
  public void disconnectEquipment(String isGroupName, String isMachineId,
      String isMOSController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Disconnect Data Request");
    isMachineId = isMachineId.substring(isMachineId.indexOf(':') + 1);
    publishControlEvent(ControlEventDataFormat.getMosMachineCommand(isMachineId),
        ControlEventDataFormat.MOS_DISCONNECT, isMOSController);

  }

  /**
   * Recover/reconnect a piece of equipment
   * 
   * @param isGroupName
   * @param isMachineId
   * @param isMOSController
   */
  public void reconnectEquipment(String isGroupName, String isMachineId,
      String isMOSController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Recovery Data Request");
    isMachineId = isMachineId.substring(isMachineId.indexOf(':') + 1);
    publishControlEvent(ControlEventDataFormat.getMosMachineCommand(isMachineId),
        ControlEventDataFormat.MOS_RECOVER_DATA, isMOSController);
  }

  /**
   * Latch clear
   * 
   * @param isMachineId
   * @param isMOSController
   */
  public void latchClear(String isMachineId, String isMOSController)
  {
    if (isMachineId.equals(AGCMOSMessage.LATCH_CLEAR_ALL_MOSID))
    {
      if (!displayYesNoPrompt("Really clear ALL latches"))
      {
        return;
      }
    }
    logger.logOperation(LogConsts.OPR_DEVICE, isMOSController 
        + " ====>>> Latch clear");
    isMachineId = isMachineId.substring(isMachineId.indexOf(':') + 1);
    publishControlEvent(
        ControlEventDataFormat.getMosMachineCommand(isMachineId),
        ControlEventDataFormat.MOS_LATCH_CLEAR, isMOSController);
  }

  /**
   * Start a device
   * 
   * @param isGroupName
   * @param isMCController
   */
  public void startDevice(String isGroupName, String isMCController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> AGC Online");
    publishControlEvent("" + ControlEventDataFormat.CHAR_START_DEVICE, 
        ControlEventDataFormat.TEXT_MESSAGE, isMCController);
  }

  /**
   * Stop a device
   * 
   * @param isGroupName
   * @param isMCController
   */
  public void stopDevice(String isGroupName, String isMCController)
  {
    if (displayYesNoPrompt("Are you sure you want to stop " + isGroupName + "-"
        + isMCController))
    {
      logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
          + " ====>>> AGC Offline");
      if (!mpEquipProperties.hasMOSConnection(isGroupName))
      {
        publishControlEvent("" + ControlEventDataFormat.CHAR_STOP_EQUIPMENT, 
            ControlEventDataFormat.TEXT_MESSAGE, isMCController); // send simultaneous stop first
      }
      publishControlEvent("" + ControlEventDataFormat.CHAR_STOP_DEVICE, 
          ControlEventDataFormat.TEXT_MESSAGE, isMCController);
    }
  }

  /**
   * Send a barcode command
   * 
   * @param isGroupName
   * @param isStationID - Generally speaking, there isn't one, but there might be.
   * @param isMachineId
   * @param isMOSController
   */
  public void sendBarCode(String isGroupName, String isStationID,
      String isMachineId, String isMOSController)
  {
    String vsBarCode = (String) JOptionPane.showInputDialog(this,
        "Enter Bar Code", "Bar Code Entry", JOptionPane.QUESTION_MESSAGE, null,
        null, "");
    if (vsBarCode != null)
    {
      vsBarCode = vsBarCode.trim();
      int vnBarCode = DBInfo.getFieldLength(LoadData.BCRDATA_NAME);
      if (vsBarCode.length() > 0 && vsBarCode.length() <= vnBarCode)
      {
        isMachineId = isMachineId.substring(isMachineId.indexOf(':') + 1);
        logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
            + " ====>>> Send Bar Code From Equipment Monitor Screen for Barcode: \"" 
            + vsBarCode + "\" vsMachineId");
        publishControlEvent(ControlEventDataFormat.getMosBcrDataCommand(
            isMachineId, vsBarCode), ControlEventDataFormat.MOS_SEND_BAR_CODE,
            isMOSController);
      }
      else
      {
        JOptionPane.showMessageDialog(this,
            "Barcode can't be spaces or longer than " + vnBarCode
                + " characters", "Error", JOptionPane.ERROR_MESSAGE, null);
      }
    }
  }

  /**
   * Tell the MOS to save its logs
   * 
   * @param isGroupName
   * @param isMOSController
   */
  public void saveLogs(String isGroupName, String isMOSController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Save Logs");
    publishControlEvent(ControlEventDataFormat.TEXT_MOS_SAVE_LOGS, 
        ControlEventDataFormat.MOS_SAVE_ALL_LOGS, isMOSController);
  }

  /**
   * Send a communications test message over the MC port
   * 
   * @param isGroupName
   * @param isMCController
   */
  public void sendMcCommTest(String isGroupName, String isMCController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> MC Comm Test");
    publishControlEvent("" + ControlEventDataFormat.CHAR_COMM_TEST,
        ControlEventDataFormat.TEXT_MESSAGE, isMCController);
  }

  /**
   * Send a status request message over the MC port
   * 
   * @param isGroupName
   * @param isMCController
   */
  public void sendStatusRequest(String isGroupName, String isMCController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> MC Status Request");
    publishControlEvent("" + ControlEventDataFormat.CHAR_REQUEST_STATUS, 
        ControlEventDataFormat.TEXT_MESSAGE, isMCController);
  }

  /**
   * Send a communications test message over the MOS port
   * 
   * @param isGroupName
   * @param isMOSController
   */
  public void sendMosCommTest(String isGroupName, String isMOSController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> MOS Comm Test");
    publishControlEvent(ControlEventDataFormat.TEXT_MOS_COMM_TEST, 
        ControlEventDataFormat.MOS_COMM_TEST, isMOSController);
  }

  /**
   * Method to handle the weight command action sends the scale a message to get
   * the weight
   * 
   * @param isStationID
   */
  public void sendWeightCommand(String isStationID)
  {
    StandardStationServer vpStationServer = Factory.create(StandardStationServer.class);
    StationData vpStationData = vpStationServer.getStation(isStationID);
    if (vpStationData != null && vpStationData.getStationScale().length() > 0)
    {
      LoadEventDataFormat vpLoadEvent = Factory.create(
          LoadEventDataFormat.class, "Equipment Monitor");
      vpLoadEvent.setLoadID(AGCDeviceConstants.AGCDUMMYLOAD);
      vpLoadEvent.setSourceStation(vpStationData.getStationName());
      getSystemGateway().publishLoadEvent(vpLoadEvent.createStringToSend(), 0,
          vpStationData.getStationScale());
    }
    else
    {
      JOptionPane.showMessageDialog(this, "Station does not have a scale",
          "Error", JOptionPane.ERROR_MESSAGE, null);
    }
  }
  
  /**
   * Method sends a message to the scale controller containing the weight
   * 
   * @param isStationID
   */
  public void enterWeightCommand(String isStationID)
  {
    StandardStationServer vpStationServer = Factory.create(StandardStationServer.class);
    StationData vpStationData = vpStationServer.getStation(isStationID);

    if (vpStationData != null && vpStationData.getStationScale().length() > 0)
    {
      String vsWeight = (String) JOptionPane.showInputDialog(this,
          "Enter Weight", "Weight Entry", JOptionPane.QUESTION_MESSAGE, null,
          null, "");
      if (vsWeight != null)
      {
        if (vsWeight.length() > 10)
        {
          JOptionPane.showMessageDialog(this,
              "Number can't be longer than ten characters", "Error",
              JOptionPane.ERROR_MESSAGE, null);
          return;
        }
        // check to see if the value is an actual number
        try
        {
          Double.parseDouble(vsWeight);
        }
        catch (NumberFormatException e)
        {
          JOptionPane.showMessageDialog(this, "Not a Number", "Error",
              JOptionPane.ERROR_MESSAGE, null);
          return;
        }
        ScaleMessage vpScaleMessage = Factory.create(ScaleMessage.class);
        publishControlEvent(vpScaleMessage.getWeightDataMessage(vsWeight),
            ControlEventDataFormat.TEXT_MESSAGE,
            vpStationData.getStationScale());
      }
    }
    else
    {
      JOptionPane.showMessageDialog(this, "Station does not have scale",
          "Error", JOptionPane.ERROR_MESSAGE, null);
    }
  }

  /**
   * Send a PLC Comm Test
   * 
   * @param isGroupName
   * @param isController
   */
  public void sendControlsCommTest(String isGroupName, String isController)
  {
    logger.logOperation(LogConsts.OPR_DEVICE, isGroupName 
        + " ====>>> Comm Test");
    publishControlEvent(ControlEventDataFormat.TEXT_PLC_COMM_TEST, 
        ControlEventDataFormat.PLC_COMM_TEST, isController);
  }
  
  /**
   * This should never happen, but you never know.
   * 
   * @param isDeviceName
   * @param isCommand
   */
  public void unknownCommand(String isDeviceName, String isCommand)
  {
    displayError(isDeviceName + ": Unknown command \"" + isCommand + "\"");
  }
  
  /*========================================================================*
   *  The preceding methods are for the graphic pop-up actions              *
   *========================================================================*/
  
  /**
   * Helper method to publish a Control Event
   * 
   * @param sEvent
   * @param iEvent
   * @param sCKN
   */
  protected void publishControlEvent(String sEvent, int iEvent, String sCKN)
  {
    getSystemGateway().publishControlEvent(sEvent, iEvent, sCKN);
  }
}
