package com.daifukuamerica.wrxj.swingui.monitor;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerFactory;
import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.JDBCConnectionImpl;
import com.daifukuamerica.wrxj.log.Log;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import com.daifukuamerica.wrxj.log.io.LogReaderWriter;
import com.daifukuamerica.wrxj.log.view.CommLogScrollPane;
import com.daifukuamerica.wrxj.log.view.EquipmentLogScrollPane;
import com.daifukuamerica.wrxj.log.view.ErrorLogScrollPane;
import com.daifukuamerica.wrxj.log.view.LogScrollPane;
import com.daifukuamerica.wrxj.log.view.OperationLogScrollPane;
import com.daifukuamerica.wrxj.log.view.SystemLogScrollPane;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.swing.MonitorScrollPane;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentGroupProperty;
import com.daifukuamerica.wrxj.swingui.equipment.properties.EquipmentMonitorProperties;
import com.daifukuamerica.wrxj.swingui.log.LogObserver;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuamerica.wrxj.util.UnreachableCodeException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.InternalFrameEvent;

/**
 * The main view into the application's Controllers' statuses and logs.  The
 * Controllers in the application can be activated, deactivated and monitored
 * from this frame.  Any Devices controlled by the application can be monitored
 * and controlled (for maintenance functions).
 *
 * @author Stephen Kendorski
 */
@SuppressWarnings("serial")
public class MonitorFrame extends SKDCInternalFrame
{
  private static final String POPUP_STARTUP = "Start";
  private static final String POPUP_SHUTDOWN = "Shutdown";

  public static final String remoteText = "Remote Controllers"; 
  public static final String localText = "Local Controllers"; 

  private static boolean allowControllerControl = false;
  private static boolean remoteControllers = false;

  /**
   * A Map containing keys (Controller Group Names) and values
   * (active MonitorFrames). The value is null if no Monitor
   * is currently active for the key's Controller Group Name.
   */
  public static Map<String, MonitorFrame> systemMonitors = new TreeMap<String, MonitorFrame>();
  
  /**
   * The name of the Controller Group that the instantiated Monitor Frame
   * is Monitoring.
   */
  private String systemMonitorsKeyName = null;
  
  /**
   * The name of the Controller Group that an instantiated Monitor Frame
   * is monitoring. 
   */
  private String controllerNamesGroup = null;
  
  /**
   * An Observer used to update the controllerNames received from the
   * active Controller Group's Controller Server..
   */
  Observer controllerServerObserver = new ControllerServerObserver();
  
  /**
   * An Observer used to update the Controller Status display.
   */
  Observer controllerStatusObserver = new ControllerStatusObserver();
  private Observer exceptionObserver = new ExceptionObserver();
  
  LogReaderWriter logReaderWriter = new LogFileReaderWriter();
  private Timer timers = null;
  ControllerStatusScrollPane controllerStatusScrollPane = null;
  MonitorScrollPane mpMosAnomalyScrollPane = null;

  protected SKDCInternalFrame thisFrame = this;
  protected List<String> logScrollPaneNameList = new ArrayList<String>();
  private List<LogScrollPane> logScrollPaneList = new ArrayList<LogScrollPane>();
  private List<JScrollPane> logJScrollPaneList = new ArrayList<JScrollPane>();
  private LogObserver logObserver = null;

  protected JTabbedPane mainTabbedPane = new JTabbedPane();
  JPanel statusPanel = new JPanel();
  protected DacLogMonitorTabbedPane mpCommLogsTab = null;
  DacLogMonitorTabbedPane mpEquipmentLogsTab = null;

  JScrollPane statusScrollPane = null;
  
  SKDCButton SaveLogsButton = new SKDCButton();
  SKDCButton addLogEntryButton = new SKDCButton();
  SKDCButton startupControllersButton = new SKDCButton();
  SKDCButton shutdownControllersButton = new SKDCButton();
  
  private SKDCPopupMenu controllerPopupMenu = new SKDCPopupMenu();
  
  Dimension mpButtonSize = new Dimension(160, 27);
  
  // Log groups for equipment and communications logs
  protected String[] masTabOrder;
  protected Map<String,String> mpSpecifiedTabGroups;
  protected int mnLogsPerGroup = 8;
    
  static
  {
    // Initialize the Remote and Local Controllers text that we
    // use to select the ControllerGroup to Monitor.
    systemMonitors.put(remoteText, null);
    systemMonitors.put(localText, null);
    String loadControllers = Application.getString(WarehouseRx.LoadControllers);
    loadControllers = loadControllers.substring(0,1);
    if (loadControllers != null)
    {
      if (loadControllers.equalsIgnoreCase("A"))
      {
        allowControllerControl = true;
      } else if ((loadControllers.equalsIgnoreCase("Y")) ||
                 (loadControllers.equalsIgnoreCase("T")))
      {
        allowControllerControl = true;
      } else if (loadControllers.equalsIgnoreCase("R"))
      {
        allowControllerControl = true;
        remoteControllers = true;
      }
    }
  }

  public MonitorFrame(String isTitle)
  {
    super(isTitle);
    setAllowDuplicateScreens(false);
    setMaximizable(true);
    try
    {
      jbInit();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public MonitorFrame()
  {
    this("");
  }

  /**
   * Specify the name of the Controller Group that this instantiated Monitor
   * Frame should monitor. Also, add the name & this instance to the
   * systemMonitors Map and set the "Remote Controllers" entry value to
   * null.  Startup SystemGateway event handling, attach observers, and
   * request a status update and a list of controllers in the group.
   * 
   * @param isSystemMonitorsKeyName the name of the Controller Group this
   * frame is to monitor
   */
  public void setSystemMonitorsKeyName(String isSystemMonitorsKeyName)
  {
    systemMonitorsKeyName = isSystemMonitorsKeyName;
    controllerStatusScrollPane.setGroupName(isSystemMonitorsKeyName);
    systemMonitors.put(MonitorFrame.remoteText, null);
    systemMonitors.put(systemMonitorsKeyName, this);
    startupSystemGatewayEventHandling();
    attachObservers();
    getSystemGateway().deleteObserver(MessageEventConsts.STATUS_EVENT_TYPE, controllerStatusObserver);
    String selector = getSystemGateway().getAllUpdateEventSelector() + ":" + systemMonitorsKeyName;
    getSystemGateway().addObserver(MessageEventConsts.UPDATE_EVENT_TYPE, selector, controllerStatusObserver);
    publishControlEvent(ControlEventDataFormat.getStatusMessage(systemMonitorsKeyName), 
        ControlEventDataFormat.SHM_STATUS_REQUEST, 
        SKDCConstants.SYSTEM_HEALTH_MONITOR);
  }
  
  private void jbInit() throws Exception
  {
    setPreferredSize(new Dimension(900, 480));
    SaveLogsButton.setPreferredSize(mpButtonSize);
    SaveLogsButton.setText("Save Logs");
    SaveLogsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        SaveLogsButton_actionPerformed();
      }
    });
    statusPanel.setLayout(new BorderLayout());
    statusPanel.setMinimumSize(new Dimension(600, 476));
    statusPanel.setPreferredSize(new Dimension(900, 476));
    addLogEntryButton.setPreferredSize(mpButtonSize);
    addLogEntryButton.setText("Add Log Entry");
    addLogEntryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        addLogEntryButton_actionPerformed();
      }
    });
    getContentPane().add(mainTabbedPane, BorderLayout.CENTER);
    mainTabbedPane.add("Status/Control", statusPanel);
    // Add the Status Tables to the status scroll pane.
    loadLogGroupingConfiguration();
    initializeStatusScrollPanes();
    lookForNewLogs();
    setMinimumSize(new Dimension(700, 225));
  }
  
  /**
   * Load equipment & communications log grouping information from the SysConfig
   * table and the equipment properties file.
   */
  protected void loadLogGroupingConfiguration()
  {
    try
    {
      SysConfig vpSC = new SysConfig();
      mnLogsPerGroup = vpSC.getMonitorTabsPerGroup();
      
      /*
       * Using the equipment properties file for grouping requires that the
       * groups have the same name as the devices.
       * 
       * If you'd rather specify tab grouping elsewhere, you can override this
       * method and use the SysConfig methods getMonitorTabOrder() and
       * getMonitorTabGroups() to get this configuration instead.
       */
      EquipmentMonitorProperties vpEMP = new EquipmentMonitorProperties(logger);
      if (vpEMP.hasSpecifiedTabs())
      {
        masTabOrder = vpEMP.getTabGroups();
        mpSpecifiedTabGroups = new TreeMap<String, String>();
        for (int i = 0; i < masTabOrder.length; i++)
        {
          List<EquipmentGroupProperty> vpList = vpEMP.getTabEquipmentGroupList(i);
          for (EquipmentGroupProperty vpEGP : vpList)
          {
            mpSpecifiedTabGroups.put(vpEGP.msName, masTabOrder[i]);
          }
        }
      }
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
    }
  }
  
  /**
   * Performs initialization before displaying frame.
   *
   * <p><b>Details:</b> <code>internalFrameOpened</code> is called by Swing just
   * before displaying this frame.  This method responds by performing various
   * initialization activities that could not be performed in the
   * constructor.</p>
   *
   * <p>This implementation calls the super-<wbr>method and then attaches its
   * views to the currently active <code>ControllerImplFactory</code>, if one is
   * indeed active.</p>
   *
   * @param ipEvent the frame event
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);
    if (!remoteControllers)
    {
      // We need to monitor this JVM's local controller group.
      monitorLocalControllers(localText);
      return;
    }
    String vsGroupName = remoteText;
    logger.logDebug("Show \"" + vsGroupName + "\" MonitorFrame");
    if (vsGroupName.equals(remoteText))
    {
      // We need to select a controller group to view in a NEW Monitor Frame.
      systemMonitors.put(vsGroupName, this);
      systemMonitorsKeyName = vsGroupName;
      attachNewRemoteLogView();
    } else if (vsGroupName.equals(localText))
    {
      // We need to monitor this JVM's local controller group.
      monitorLocalControllers(vsGroupName);
    }
    else
    {
      // Need to view an existing MonitorFrame
      MonitorFrame existingFrame = systemMonitors.get(vsGroupName);
      showExistingFrame(existingFrame);
    }
  }

  @Override
  protected void shutdownFrame()
  {
    if (timers != null)
    {
      timers.cancel();
      timers = null;
    }
    if (systemMonitorsKeyName != null)
    {
      if ((systemMonitorsKeyName.equals(localText)) ||
          (systemMonitorsKeyName.equals(remoteText)))
      {
        systemMonitors.put(systemMonitorsKeyName, null);
      }
      else
      {
        systemMonitors.remove(systemMonitorsKeyName);
      }
    }
    if (logObserver != null)
    {
      logger.logDebug("removing logObserver - shutdownFrame()");
      logObserver.cleanUp();
      logObserver = null;
    }
    logger.logDebug("removing controllerStatusObserver - shutdownFrame()");
    getSystemGateway().deleteObserver(MessageEventConsts.STATUS_EVENT_TYPE, controllerStatusObserver);
    getSystemGateway().deleteObserver(MessageEventConsts.UPDATE_EVENT_TYPE, controllerStatusObserver);
    controllerStatusObserver = null;
    exceptionObserver = null;
    //
    logger.logDebug("removing controllerServerObserver - shutdownFrame()");
    getSystemGateway().deleteObserver(MessageEventConsts.CONTROL_EVENT_TYPE, controllerServerObserver);
    controllerServerObserver = null;
    //
    cleanupLogViews();
    //
    if (controllerStatusScrollPane != null)
    {
      controllerStatusScrollPane.cleanUpOnClose();
    }
    if (mpMosAnomalyScrollPane != null)
    {
      mpMosAnomalyScrollPane.cleanUpOnClose();
    }
    logScrollPaneNameList = null;
    logScrollPaneList = null;
    logJScrollPaneList = null;
    logReaderWriter = null;
    thisFrame = null;
    super.shutdownFrame();
  }

  private void initializeStatusScrollPanes()
  {
    // Add the Status Table to the status scroll pane, then add the scroll pane
    // to the status panel.
    controllerStatusScrollPane = new ControllerStatusScrollPane();
    statusScrollPane = controllerStatusScrollPane.initialize(thisFrame);
    // Add the Startup/Shutdown popup to controllerStatusScrollPane.
    JTable controllerStatusScrollPaneJTable = controllerStatusScrollPane.getTable();
    defineControllerPopup(controllerPopupMenu, new ControllerPopupMenuListener());
    controllerStatusScrollPaneJTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          showControllerPopupMenu(e); // if this is a popupTrigger
        }
        
        @Override
        public void mouseClicked(MouseEvent e)
        {
          showControllerPopupMenu(e); // if this is a popupTrigger
        }
        
        @Override
        public void mouseReleased(MouseEvent e)
        {
          showControllerPopupMenu(e); // if this is a popupTrigger
        }
      });
    statusScrollPane.setSize(100,230);
    statusScrollPane.setPreferredSize(new Dimension(controllerStatusScrollPane.getTableWidth(), 230));
    statusPanel.add(Box.createRigidArea(new Dimension(20, 20)), BorderLayout.LINE_START);
    statusPanel.add(statusScrollPane, BorderLayout.CENTER);
    statusPanel.add(Box.createRigidArea(new Dimension(20, 20)), BorderLayout.LINE_END);
    
    // Put the anomaly and button panels on a panel at the bottom of the screen
    mpMosAnomalyScrollPane = new MosAnomalyScrollPane();
    JScrollPane vpAnomalyScrollPane = null;
    vpAnomalyScrollPane = mpMosAnomalyScrollPane.initialize(thisFrame);
    vpAnomalyScrollPane.setSize(100,130);
    vpAnomalyScrollPane.setPreferredSize(new Dimension(mpMosAnomalyScrollPane.getTableWidth(), 60));

    JPanel vpAnomolyPanel = new JPanel();
    vpAnomolyPanel.add(vpAnomalyScrollPane);

    JPanel vpStatusButtonPanel = new JPanel();
    vpStatusButtonPanel.setMinimumSize(new Dimension(650, 40));
    vpStatusButtonPanel.setPreferredSize(new Dimension(700, 40));
    
    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(vpAnomolyPanel, BorderLayout.CENTER);
    vpSouthPanel.add(vpStatusButtonPanel, BorderLayout.SOUTH);

    statusPanel.add(vpSouthPanel, BorderLayout.PAGE_END);
    vpStatusButtonPanel.add(addLogEntryButton);
    vpStatusButtonPanel.add(SaveLogsButton);
    
    // Only show the startup (Controllers) Button if there is an explicit
    // property to do so.
    if (allowControllerControl)
    {
      startupControllersButton.setPreferredSize(mpButtonSize);
      startupControllersButton.setText("Startup Controllers");
      startupControllersButton.addActionListener( new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            startupControllersButton_actionPerformed();
          }
        });
      vpStatusButtonPanel.add(startupControllersButton);
      shutdownControllersButton.setPreferredSize(mpButtonSize);
      shutdownControllersButton.setText("Shutdown Controllers");
      shutdownControllersButton.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            shutdownControllersButton_actionPerformed();
          }
        });
      vpStatusButtonPanel.add(shutdownControllersButton);
    }
  }

  private void monitorLocalControllers(String isGroupName)
  {
    MonitorFrame existingFrame = systemMonitors.get(isGroupName);
    if (existingFrame != null)
    {
      // Need to view an existing MonitorFrame.
      showExistingFrame(existingFrame);
    }
    else
    {
      systemMonitors.put(isGroupName, this);
      systemMonitorsKeyName = isGroupName;
      setTitle(defaultTitle + "   -  " + groupName + "  (Local Controllers)");
      attachBasicLogViewers();
      startupSystemGatewayEventHandling();
      attachObservers();
      attachEquipmentLogViewers();
      attachCommLogViewers();
      publishControlEvent(ControlEventDataFormat.getStatusMessage(groupName),
          ControlEventDataFormat.SHM_STATUS_REQUEST, 
          SKDCConstants.SYSTEM_HEALTH_MONITOR);
    }
  }

  private void showExistingFrame(MonitorFrame existingFrame)
  {
    if (existingFrame.isIcon())
    {
      try
      {
        existingFrame.setIcon(false);
      }
      catch (PropertyVetoException pve) {}
    }
    else
    {
      existingFrame.moveToFront();
      try
      {
        existingFrame.setSelected(true);
      }
      catch (PropertyVetoException pve) {}
    }
    close(); // Close this frame instance since we're using an existing frame.
  }

  private void attachNewRemoteLogView()
  {
    logObserver = new LogObserver();
    logObserver.setParentFrame(thisFrame);
    logObserver.setParentFrameDefaultTitle(defaultTitle);
    logObserver.setMainTabbedPane(mainTabbedPane);
    logObserver.setCommLogsTabbedPane(mpCommLogsTab);
    logObserver.setEquipmentLogsTabbedPane(mpEquipmentLogsTab);
    logObserver.setObserver(controllerStatusObserver);
    logObserver.initialize();
  }

  /**
   * Add a Status Event Observer.  The observer is added using the
   * {@link #controllerFactory controllerFactory}.
   */
  private void startupSystemGatewayEventHandling()
  {
    // If controllerFactory is null then this is a client with only
    // a SystemGateway.
    controllerNamesGroup = groupName;
    if (!allowControllerControl)
    {
      startupControllersButton.setVisible(false);
      shutdownControllersButton.setVisible(false);
    }
    logger.logDebug("Adding controllerStatusObserver for Group: " + groupName 
        + " - startupSystemGatewayEventHandling()");
    String selector = getSystemGateway().getAllUpdateEventSelector() + ":" 
        + groupName;
    getSystemGateway().addObserver(MessageEventConsts.UPDATE_EVENT_TYPE, 
        selector, controllerStatusObserver);
    publishControlEvent(ControlEventDataFormat.getStatusMessage(groupName), 
        ControlEventDataFormat.SHM_STATUS_REQUEST, 
        SKDCConstants.SYSTEM_HEALTH_MONITOR);
    //
    logger.logDebug("Adding controllerServerObserver for Group: " + groupName 
        + " - startupSystemGatewayEventHandling()");
    selector = getSystemGateway().getControlEventSelector(controllerNamesGroup 
        + instanceKey);
    getSystemGateway().addObserver(MessageEventConsts.CONTROL_EVENT_TYPE, 
        selector, controllerServerObserver);
  }

  /**
   * Attaches observers to controllers.
   * 
   * <p><b>Details:</b> <code>attachObservers</code> attaches its log viewers
   * and event observers to the currently running set of controllers.</p>
   */
  void attachObservers()
  {
    // Controllers Initialized & Started -- Now, check for any exceptions that
    // may have prevented our MessageService from starting (if so, NO Exception
    // Events will ever get published!).
    ObservableControllerImpl observableControllerImpl = new ObservableControllerImpl();
    String s = Factory.create(MessageService.class).getStartupFailReason();
    if (s != null)
    {
      observableControllerImpl.setStringData(s);
      exceptionObserver.update(observableControllerImpl, null);
    }
    if (!DBObject.isWRxJConnectionActive())
    {
      s = "Unable to connect to database";
      observableControllerImpl.setStringData(s);
      exceptionObserver.update(observableControllerImpl, null);
    }
    if (JDBCConnectionImpl.getConnectionFailed())
    {
      s = JDBCConnectionImpl.getConnectionFailureReason();
      observableControllerImpl.setStringData(s);
      exceptionObserver.update(observableControllerImpl, null);
    }
  }

  void controllerPopupMenu_actionPerformed(ActionEvent e)
  {
    JTable controllerStatusScrollPaneJTable = (JTable)controllerPopupMenu.getInvoker();
    if (controllerStatusScrollPaneJTable != null)
    {
      String actionCommand = e.getActionCommand();
      int[] rows = controllerStatusScrollPaneJTable.getSelectedRows();
      int rowCount = rows.length;
      List<String> selectedControllersList = new ArrayList<String>();
      for (int i = 0; i < rowCount; i++)
      {
        int row = rows[i];
        String controllerName = controllerStatusScrollPane.getControllerNameAtRow(row);
        selectedControllersList.add(controllerName);
      }
      String[] vpSelectedControllers = selectedControllersList.toArray(new String[0]);
      String vsAction = null;
      if (actionCommand.equals(POPUP_STARTUP))
      {
        vsAction = ControlEventDataFormat.CONTROLLER_STARTUP;
      }
      else if (actionCommand.equals(POPUP_SHUTDOWN))
      {
        vsAction = ControlEventDataFormat.CONTROLLER_SHUTDOWN;
      }
      startupOrShutdownControllers(vsAction, vpSelectedControllers);
    }
  }

 private void defineControllerPopup(SKDCPopupMenu popupMenu, ActionListener actionListener)
 {
   popupMenu.add("Startup Controllers", POPUP_STARTUP, actionListener);
   popupMenu.add("Shutdown Controllers", POPUP_SHUTDOWN, actionListener);
 }

  private class ControllerPopupMenuListener implements ActionListener
  {

    @Override
    public void actionPerformed(ActionEvent e)
    {
      controllerPopupMenu_actionPerformed(e);
    }
    
  }

  void showControllerPopupMenu(MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      if (allowControllerControl)
      {
        controllerPopupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
      else
      {
        displayInfoAutoTimeOut("Startup/Shutdown of Controllers NOT Permitted from this Client");
      }
    }
  }

  /**
   * Publish a "Control" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   *
   * @param sEvent the String data content to be sent
   * @param iEvent the int data content to be sent
   * @param sCKN the message destination
   */
  void publishControlEvent(String sEvent, int iEvent, String sCKN)
  {
    getSystemGateway().publishControlEvent(sEvent, iEvent, sCKN);
  }

  /**
   * Publish a "Log" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   *
   * @param sEvent the String data content to be sent
   * @param iEvent the int data content to be sent
   * @param sCKN the message destination
   */
  void publishLogEvent(String sEvent, int iEvent, String sCKN)
  {
    getSystemGateway().publishLogEvent(sEvent, iEvent, sCKN);
  }

  void monitorFrame_closeWaitFrame(final Object ipHandle)
  {
    closeWaitFrame(ipHandle);
  }

  protected Object monitorFrame_openWaitFrame(final String isTitle)
  {
    return openWaitFrame(isTitle);
  }

  private void cleanupLogViews()
  {
    Iterator logIterator = logScrollPaneList.iterator();
    while (logIterator.hasNext())
    {
      LogScrollPane aPane = (LogScrollPane)logIterator.next();
      aPane.cleanUpOnClose();
    }
    logScrollPaneList.clear();
    //
    logIterator = logJScrollPaneList.iterator();
    while (logIterator.hasNext())
    {
      JScrollPane aPane = (JScrollPane)logIterator.next();
      mainTabbedPane.remove(aPane);
    }
    logJScrollPaneList.clear();
  }

  /**
   * Attach the Error, Operation, and System logs
   */
  private void attachBasicLogViewers()
  {
    logger.logDebug("attachBasicLogViewers");
    LogScrollPane logScrollPane = new ErrorLogScrollPane();
    JScrollPane jScrollPane = logScrollPane.initialize(Logger.getErrorLogger(),
        true, thisFrame);
    addLogScrollPaneToMainPane(LogConsts.ERROR_LOG_NAME, logScrollPane,
        "Error Logs", jScrollPane);
    logScrollPane = new OperationLogScrollPane();
    jScrollPane = logScrollPane.initialize(Logger.getOperationLogger(), true,
        thisFrame);
    addLogScrollPaneToMainPane(LogConsts.OPERATION_LOG_NAME, logScrollPane,
        "Operation Logs", jScrollPane);
    logScrollPane = new SystemLogScrollPane();
    jScrollPane = logScrollPane.initialize(Logger.getSystemLogger(), true,
        thisFrame);
    addLogScrollPaneToMainPane(LogConsts.SYSTEM_LOG_NAME, logScrollPane,
        "System Logs", jScrollPane);
  }

  /**
   * Add a log to the main pane
   * 
   * @param dataName
   * @param logScrollPane
   * @param logPaneName
   * @param jScrollPane
   */
  private void addLogScrollPaneToMainPane(String dataName,
      LogScrollPane logScrollPane, String logPaneName, JScrollPane jScrollPane)
  {
    logScrollPane.setDataName(dataName);
    logScrollPaneList.add(logScrollPane);
    logJScrollPaneList.add(jScrollPane);
    //Add the log scroll pane to the Tabbed Panel.
    mainTabbedPane.add(logPaneName, jScrollPane);
  }

  /**
   * Add a log to a target Log tab
   * 
   * @param isLogName
   * @param ipLogScrollPane
   * @param isLogPaneName
   * @param ipScrollPane
   * @param ipTargetTabbedPane
   */
  protected void addLogScrollPaneToTabbedPane(String isLogName,
      LogScrollPane ipLogScrollPane, String isLogPaneName,
      JScrollPane ipScrollPane, JTabbedPane ipTargetTabbedPane)
  {
    ipLogScrollPane.setDataName(isLogName);
    logScrollPaneList.add(ipLogScrollPane);
    logJScrollPaneList.add(ipScrollPane);

    /*
     * Our tabs don't need the ":Ctlrs" on them
     */
    String vsTabName = isLogPaneName;
    if (vsTabName.contains(":"))
    {
      vsTabName = vsTabName.substring(0, vsTabName.indexOf(":"));
    }
    
    ipTargetTabbedPane.addTab(vsTabName, ipScrollPane);
  }

  /**
   * Add the Comm Logs
   */
  protected void attachCommLogViewers()
  {
    if (!SKDCUserData.isSuperUser() && !SKDCUserData.isAdministrator())
    {
      return;
    }
    logger.logDebug("Attaching CommLogViewers");
    List<Log> loggers = Logger.getLogInstances();
    List<Log> c = null;
    synchronized(loggers)
    {
      c = new ArrayList<Log>(loggers);
    }
    Iterator logIterator = c.iterator();
    while (logIterator.hasNext())
    {
      Log vpLog = (Log)logIterator.next();

      if (vpLog.getLogType().equals(LogConsts.COMM_LOG_NAME))
      {
        if (mpCommLogsTab == null)
        {
          mpCommLogsTab = Factory.create(DacLogMonitorTabbedPane.class,
              mnLogsPerGroup, masTabOrder, mpSpecifiedTabGroups);
          mainTabbedPane.add("Comm Logs", mpCommLogsTab);
        }
        String vsLogName = LogConsts.COMM_LOG_NAME + vpLog.getLogName();
        if (! logScrollPaneNameList.contains(vsLogName))
        {
          logScrollPaneNameList.add(vsLogName);
          LogScrollPane logScrollPane = new CommLogScrollPane();
          JScrollPane jScrollPane = logScrollPane.initialize(vpLog, false, thisFrame);
          addLogScrollPaneToTabbedPane(vsLogName, logScrollPane,
              vpLog.getLogName(), jScrollPane, mpCommLogsTab);
        }
      }
    }
  }

  /**
   * Add the Equipment logs
   */
  private void attachEquipmentLogViewers()
  {
//    if (mainTabbedPane.indexOfTab("Equipment Logs") == -1)
    {
      logger.logDebug("Attaching EquipmentLogViewers");
      List<Log> loggers = Logger.getLogInstances();
      List<Log> c = null;
      synchronized(loggers)
      {
        c = new ArrayList<Log>(loggers);
      }
      Iterator logIterator = c.iterator();
      while (logIterator.hasNext())
      {
        Log vpLog = (Log)logIterator.next();

        if (vpLog.getLogType().equals(LogConsts.EQUIPMENT_LOG_NAME))
        {
          if (mpEquipmentLogsTab == null)
          {
            mpEquipmentLogsTab = Factory.create(DacLogMonitorTabbedPane.class,
                mnLogsPerGroup, masTabOrder, mpSpecifiedTabGroups);
            mainTabbedPane.add("Equipment Logs", mpEquipmentLogsTab);
          }
          String vsLogName = LogConsts.EQUIPMENT_LOG_NAME + vpLog.getLogName();
          if (! logScrollPaneNameList.contains(vsLogName))
          {
            logScrollPaneNameList.add(vsLogName);
            LogScrollPane logScrollPane = new EquipmentLogScrollPane();
            JScrollPane jScrollPane = logScrollPane.initialize(vpLog, false, thisFrame);
            addLogScrollPaneToTabbedPane(vsLogName, logScrollPane,
                vpLog.getLogName(), jScrollPane, mpEquipmentLogsTab);
          }
        }
      }
    }
  }

  private class ControllerStatusObserver implements Observer
  {
    @Override
    public void update(Observable o, Object arg)
    {
      controllerStatusObserverUpdate(o, arg);
    }
  }

  void controllerStatusObserverUpdate(Observable o, Object arg)
  {
    ObservableControllerImpl observableData = (ObservableControllerImpl)o;
    int statusType = observableData.getIntData();
    switch (statusType)
    {
      case ControllerConsts.CONTROLLER_STATUS:
      case ControllerConsts.DETAILED_STATUS:
        controllerStatusScrollPane.update(o, arg);
        break;
      case ControllerConsts.EQUIPMENT_STATUS:
        break;
      case ControllerConsts.TRACKING_STATUS:
        break;
      default:
        logger.logError("UNKNOWN StatusType: " + (char)statusType + " - ControllerStatusObserver.update()");
        break;
    }
  }
  
  private class ExceptionObserver implements Observer
  {
    @Override
    public void update(Observable o, Object arg)
    {
      mpMosAnomalyScrollPane.update(o, arg);
      int index = mpMosAnomalyScrollPane.getUpdatedIndex();
      if (index >= 0)
      {
        mpMosAnomalyScrollPane.addSelectedKey(index, Color.yellow);
      }
      else
      {
        mpMosAnomalyScrollPane.addSelectedKey(index, Color.white);
      }
      mpMosAnomalyScrollPane.fireDataChanged();
    }
  }

  private class ControllerServerObserver implements Observer
  {
    @Override
    public void update(Observable o, Object arg)
    {
      throw new UnreachableCodeException("Mike was wrong!");
    }
  }

  void startupControllersButton_actionPerformed()
  {
    startupOrShutdownControllers(ControlEventDataFormat.CONTROLLER_STARTUP);
  }

  void shutdownControllersButton_actionPerformed()
  {
    startupOrShutdownControllers(ControlEventDataFormat.CONTROLLER_SHUTDOWN);
  }

  public static final String ALL_CONTROLLERS = "ALL CONTROLLERS";
  
  private void startupOrShutdownControllers(String isAction)
  {
    List<String> controllerNamesList = ControllerFactory.getControllerNames();
    if (controllerNamesList != null)
    {
      int listSize = controllerNamesList.size() + 1;
      Vector<String> controllerNames = new Vector<String>(listSize);
      controllerNames.add(ALL_CONTROLLERS);
      controllerNames.addAll(controllerNamesList);
      JList controllerNamesChooser = new JList(controllerNames);
      controllerNamesChooser.setSelectedIndex(0);
      if (listSize > 20)
        listSize = 20;
      controllerNamesChooser.setVisibleRowCount(listSize);
      JScrollPane listScrollPane = new JScrollPane(controllerNamesChooser);
      Object[] options = new Object[]{listScrollPane};
      int input = JOptionPane.showOptionDialog(MonitorFrame.this, options,
                   "Select Controllers To " + isAction, JOptionPane.OK_CANCEL_OPTION,
                   JOptionPane.QUESTION_MESSAGE, null, null, null);
      if (input == JOptionPane.OK_OPTION)
      {
        Object[] vapSelectedControllers = controllerNamesChooser.getSelectedValues();
        String[] vasSelectedControllers = new String[vapSelectedControllers.length];
        for (int i = 0; i < vapSelectedControllers.length; i++)
        {
          vasSelectedControllers[i] = vapSelectedControllers[i].toString();
        }
        startupOrShutdownControllers(isAction, vasSelectedControllers);
      }
    }
  }

  /**
   * Start up or shut down the selected controllers
   * 
   * @param isAction
   * @param iasSelectedControllers
   */
  private void startupOrShutdownControllers(String isAction, 
      String[] iasSelectedControllers)
  {
    if (iasSelectedControllers.length == 0)
    {
      displayInfoAutoTimeOut("NO Controllers Selected", isAction + " Controllers");
      return;
    }
    JList controllerNamesChooser = new JList(iasSelectedControllers);
    JScrollPane listScrollPane = new JScrollPane(controllerNamesChooser);
    int listSize = iasSelectedControllers.length;
    if (listSize > 20)
    {
      listSize = 20;
    }
    controllerNamesChooser.setVisibleRowCount(listSize);
    SKDCLabel label = new SKDCLabel("Are You Sure You Want To " + isAction 
        + " These Controllers ?");
    Object[] options = new Object[]{label, listScrollPane};
    int input = JOptionPane.showOptionDialog(MonitorFrame.this, options,
                 "Confirm Controller " + isAction, JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE, null, null, null);
    if (input == JOptionPane.OK_OPTION)
    {
      if (iasSelectedControllers[0].equals(ALL_CONTROLLERS))
      {
        logger.logOperation(LogConsts.OPR_USER, isAction + " ALL Controllers");
      }
      else
      {
        logger.logOperation(LogConsts.OPR_USER, isAction + " "
            + iasSelectedControllers.length + " Controllers");
      }
      
      publishControlEvent(
          ControlEventDataFormat.getCommandTargetListMessage(
              isAction, iasSelectedControllers), 
          ControlEventDataFormat.TEXT_MESSAGE, SKDCConstants.CONTROLLER_SERVER);
      if (systemMonitorsKeyName.equals(localText))
      {
        lookForNewLogs();
      }
    }
  }

  /**
   * Start up the controllers
   */
  void startupControllers()
  {
    new Thread("MonitorFrame-ControllerStartup")
    {
      @Override
      public void run()
      {
        final Object vpWfHandle = monitorFrame_openWaitFrame("Starting Up Controllers");
        ControllerFactory.startAllControllers();
        attachObservers();
        // Controllers Initialized & Started -- Attach our Viewer to the
        // Equipment & Comm Logs.
        attachEquipmentLogViewers();
        attachCommLogViewers();
        monitorFrame_closeWaitFrame(vpWfHandle);
       }
      
    }.start();
  }

  /**
   * Save the logs
   */
  private void SaveLogsButton_actionPerformed()
  {
    SaveLogsButton.setEnabled(false);
    addLogEntryButton_actionPerformed();
    if (remoteControllers)
    {
      publishLogEvent("SaveLogs", LogConsts.SAVE_LOGS, SKDCConstants.LOG_SERVER);
    }
    new Thread("MonitorFrame-SaveLogs")
    {
      
      @Override
      public void run()
      {
        saveLogs();
      }
      
    }.start();
  }
  
  /**
   * Save the logs
   */
  private void saveLogs()
  {
    final Object vpWfHandle = monitorFrame_openWaitFrame("Saving Logs");
    String vsSavedPath = logReaderWriter.writeLogs();
    logger.logOperation(LogConsts.OPR_USER, "Logs Saved to: " + vsSavedPath);
    monitorFrame_closeWaitFrame(vpWfHandle);
    SKDCButton.setEnabledTs(SaveLogsButton, true);
  }

  /**
   * Add a log entry
   */
  private void addLogEntryButton_actionPerformed()
  {
    String logText = JOptionPane.showInputDialog("Enter text to be logged");
    if (logText != null)
    {
      if (logText.length() == 0)
      {
        logText = ">>======>> User Added Log Entry <<======<<";
      }
      logger.logDebug(" ");
      logger.logOperation(LogConsts.OPR_USER, logText);
      logger.logDebug(" ");
      if (!systemMonitorsKeyName.equals(localText))
      {
        publishLogEvent(logText, LogConsts.ADD_LOG_ENTRY,
            SKDCConstants.LOG_SERVER);
      }
    }
  }

  /**
   * Logs don't get added right away.  After new controllers are started, we'll
   * check a few times to see what we can find.
   */
  private void lookForNewLogs()
  {
    if (timers == null)
    {
      timers = new Timer("ButtonStartupTimeout");
    }
    timers.schedule(new ButtonStartupTimeout(), 10000);
    timers.schedule(new ButtonStartupTimeout(), 30000);
    timers.schedule(new ButtonStartupTimeout(), 60000);
  }
  
  /**
   * Task to add new equipement and comm logs  
   */
  private class ButtonStartupTimeout extends TimerTask
  {
    @Override
    public void run()
    {
      // Controllers Initialized & Started -- Attach our Viewer to the
      // Equipment & Comm Logs.
      attachEquipmentLogViewers();
      attachCommLogViewers();
    }
  }
}

