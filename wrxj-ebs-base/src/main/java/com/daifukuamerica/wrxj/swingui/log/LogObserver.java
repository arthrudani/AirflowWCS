package com.daifukuamerica.wrxj.swingui.log;

import com.daifukuamerica.wrxj.controller.observer.ObservableControllerImpl;
import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.LogDataModel;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.view.CommLogScrollPane;
import com.daifukuamerica.wrxj.log.view.EquipmentLogScrollPane;
import com.daifukuamerica.wrxj.log.view.ErrorLogScrollPane;
import com.daifukuamerica.wrxj.log.view.LogScrollPane;
import com.daifukuamerica.wrxj.log.view.OperationLogScrollPane;
import com.daifukuamerica.wrxj.log.view.SystemLogScrollPane;
import com.daifukuamerica.wrxj.messageformat.controlevent.ControlEventDataFormat;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.monitor.MonitorFrame;
import com.daifukuamerica.wrxj.timer.RestartableTimer;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * LogObserver is a View into a remote logger that is in another running
 * instance of WRx-J. The LogObserver uses a RemoteLogClient to cache data
 * from the remote logger. Remote Log Entry counts are also periodically
 * requested so that the RemoteLogClient can show the log viewer that new logs
 * are available.
 *
 * @author Stephen Kendorski
 */
public class LogObserver implements Observer
{
  private int systemLogCount = 0;
  private int operationLogCount = 0;
  private int errorLogCount = 0;
  private List<LogScrollPane> logScrollPaneList = new ArrayList<LogScrollPane>();
  private List<JScrollPane> logJScrollPaneList = new ArrayList<JScrollPane>();
  private List<String> logList = new ArrayList<String>();
  private RestartableTimer timers = null;
  private SKDCInternalFrame parentFrame = null;
  private Observer observer = null;
  
  /**
   * The Logging implementation for this named subsystem to use.
   */
  private Logger logger = Logger.getLogger();
  /**
   * The name of the running instance of WRx-J whose log's we are viewing.
  */
  private String controllerGroupName = null;

  private SystemGateway getSystemGateway()
  {
    return ThreadSystemGateway.get();
  }

  /**
   * The RemoteLogClient for the Log Scroll Pane that is currently showing.
   */
  private RemoteLogClient activeRemoteLogClient = null;
  /**
   * Only request a new list of available loggers after requesting
   * this many Log Entry Counts.  Loggers can be added as Controllers
   * are started.
   */
  private int requestLogListInterval = 2;
  /**
   * A Map of the RemoteLogClients that handle the received remote
   * log cache. 
   */
  private Map<String, RemoteLogClient> remoteLogClients = new HashMap<String, RemoteLogClient>();
  private Map<String, LogScrollPane> logScrollPanes = new HashMap<String, LogScrollPane>();
  private LogUpdateTimeout logUpdateTimeout = new LogUpdateTimeout();
  private String parentFrameDefaultTitle = null;
  private Object activeClientLock = new Object();
  private LogScrollPane activeLogScrollPane = null;
  JTabbedPane mainTabbedPane = new JTabbedPane();
  JTabbedPane commLogsTabbedPane = null;
  JTabbedPane equipmentLogsTabbedPane = null;

  /*-------------------------------------------------------------------------*/
  public LogObserver()
  {
    try
    {
//      setSystemGateway((SystemGateway)SkdContext.getSystemGateway(logger.getLoggerInstanceName()));
//      if (getSystemGateway() == null)
//      {
//        throw new NullPointerException("Error obtaining System gateway...");
//      }
    }
    catch (Exception e)
    {
      logger.logException(e, "mpSystemGateway NOT Found - Namespace " + logger.getLoggerInstanceName());
    }
  }

  /**
   * Specify the frame that owns this LogObserver instance.
   * 
   * @param ipFrame the parent frame
   */
  public void setParentFrame(SKDCInternalFrame ipFrame)
  {
    parentFrame = ipFrame;
  }

  /**
   * Specify the name of the frame that owns this LogObserver instance.
   * 
   * @param isParentFrameDefaultTitle the title
   */
  public void setParentFrameDefaultTitle(String isParentFrameDefaultTitle)
  {
    parentFrameDefaultTitle = isParentFrameDefaultTitle;
  }

  public void setMainTabbedPane(JTabbedPane ipTabbedPane)
  {
    mainTabbedPane = ipTabbedPane;
  }

  public void setCommLogsTabbedPane(JTabbedPane ipTabbedPane)
  {
    commLogsTabbedPane = ipTabbedPane;
  }

  public void setEquipmentLogsTabbedPane(JTabbedPane ipTabbedPane)
  {
    equipmentLogsTabbedPane = ipTabbedPane;
  }

  public void setObserver(Observer ipObserver)
  {
    observer = ipObserver;
  }
  public String getControllerGroupName()
  {
    return controllerGroupName;
  }
  
  public void initialize()
  {
    String selector = getSystemGateway().getLogEventSelector() 
        + getSystemGateway().getControllersKeyName();
    getSystemGateway().addObserver(MessageEventConsts.LOG_EVENT_TYPE, selector, 
        LogObserver.this);
    publishControlEvent(ControlEventDataFormat.TEXT_LOG_REQUEST_GROUPS_STR, 
        ControlEventDataFormat.SHM_REQUEST_GROUPS, 
        SKDCConstants.SYSTEM_HEALTH_MONITOR);
  }
  
  public void cleanUp()
  {  
    if (timers != null)
    {
      timers.cancel(logUpdateTimeout);
      logUpdateTimeout = null;
      timers.cancel();
      timers = null;
    }
    logUpdateTimeout = null;
    getSystemGateway().deleteObserver(MessageEventConsts.LOG_EVENT_TYPE, this);
    //
    cleanupLogViews();
    observer = null;
    parentFrame = null;
//    setSystemGateway(null);

    activeRemoteLogClient = null;
    cleanupRemoteLogClients();
    activeClientLock = null;
    parentFrameDefaultTitle = null;
    parentFrame = null;
    logger = null;
  }
  
  private void cleanupRemoteLogClients()
  {
    Iterator vpIter = remoteLogClients.values().iterator();
    while (vpIter.hasNext())
    {
      RemoteLogClient vpRemoteLogClient = (RemoteLogClient)vpIter.next();
      vpRemoteLogClient.cleanup();
    }
    remoteLogClients = null;
  }
  public void cleanupLogViews()
  {
    Iterator logIterator = logScrollPaneList.iterator();
    while (logIterator.hasNext())
    {
      LogScrollPane aPane = (LogScrollPane)logIterator.next();
      aPane.cleanUpOnClose();
    }
    logScrollPaneList.clear();
    logIterator = logJScrollPaneList.iterator();
    while (logIterator.hasNext())
    {
      JScrollPane aPane = (JScrollPane)logIterator.next();
      mainTabbedPane.remove(aPane);
    }
    logJScrollPaneList.clear();
    commLogsTabbedPane = null;
    equipmentLogsTabbedPane = null;
    logScrollPanes = null;
    mainTabbedPane = null;
  }

  public void update(Observable o, Object arg)
  {
    ObservableControllerImpl observableData = (ObservableControllerImpl)o;
    int logDataType = observableData.getIntData();
    String sText = observableData.getStringData();
    try
    {
      switch (logDataType)
      {
        case LogConsts.SEND_CONTROLLER_GROUPS:
          processControllerGroupList(sText);
          break;
        case LogConsts.SEND_LOG_LIST:
          processLogList(sText);
          break;
        case LogConsts.SEND_LOG_ENTRY_COUNT:
          processLogEntryCount(sText);
          break;
        case LogConsts.SEND_LOG_ENTRIES:
          processLogEntries(sText);
          break;
        case LogConsts.SEND_FILTERED_LOG_ENTRY_COUNT:
          processFilteredLogEntryCount(sText);
          break;
        case LogConsts.SEND_FILTERED_LOG_ENTRIES:
          processFilteredLogEntries(sText);
          break;
        case LogConsts.FIND_TEXT:
          processFindText(sText);
          break;
        default:
          logger.logError("UNKNOWN StatusType: " + logDataType + " - LogObserver.update()");
          break;
      }
    }
    catch (Exception e)
    {
      logger.logException(e, "\n\n" + sText + "\n\n");
    }
  }

  /*-------------------------------------------------------------------------*/
  /*-------------------------------------------------------------------------*/
  /**
   * Process a string containing the Controller Group Name and a tab delimited
   * list of log names and descriptions
   * 
   * @param sText
   */
   void processControllerGroupList(String sText)
  {
    try
    {
      StringTokenizer stringTokenizer = new StringTokenizer(sText, "\t\n");
      int controllerGroupNameCount = stringTokenizer.countTokens();
      if (controllerGroupNameCount > 0)
      {
        String[] controllerGroupNames = new String[controllerGroupNameCount];
        for (int i = 0; i < controllerGroupNameCount; i++)
        {
          controllerGroupNames[i] = stringTokenizer.nextToken();
        }
        parentFrame.setVisible(false);
//        String vsControllerGroupName = (String)JOptionPane.showInputDialog(
//                      parentFrame,
//                      "Please select view",
//                      "Controller Group Selection", JOptionPane.QUESTION_MESSAGE,
//                      null, controllerGroupNames,
//                      controllerGroupNames[0]);
        String vsControllerGroupName = "Ctlrs";
//        if (vsControllerGroupName == null)
//        {
//          return;
//        }
        controllerGroupName = vsControllerGroupName;
        if (observer != null)
        {
          //
          // If we have an observer it's the one associated with remoteText.
          // We need to replace the remoteText key with the actual controller
          // group name that we're viewing.
          //
          MonitorFrame vpMonitorFrame = MonitorFrame.systemMonitors.get(MonitorFrame.remoteText);
          vpMonitorFrame.setSystemMonitorsKeyName(controllerGroupName);
        }
        //
        // We have selected the Controller Group whose logs we want to view.
        // Create a cached view.
        //
        parentFrame.setTitle(parentFrameDefaultTitle + "  -  " + controllerGroupName);
        requestRemoteLogList();
      }
      parentFrame.setVisible(true);
    }
    catch (Exception e)
    {
      logger.logException(e, "\n\n" + sText + "\n\n");
    }
  }

  private void requestRemoteLogList()
  {
    publishLogEvent("Get Log List", LogConsts.SEND_LOG_LIST, 
        SKDCConstants.LOG_SERVER);
  }

  /*-------------------------------------------------------------------------*/
  /*-------------------------------------------------------------------------*/
  private void processLogList(String sText)
  {
    try
    {
      synchronized (this)
      {
        if (timers == null)
          timers = new RestartableTimer("LogObserver Timer");
      }
      StringTokenizer stringTokenizer = new StringTokenizer(sText, "\n");
      controllerGroupName = stringTokenizer.nextToken(); // Name of Log Server
      while (stringTokenizer.hasMoreTokens())
      {
        String s = stringTokenizer.nextToken();
        int idx = s.indexOf("\t");
        String vsLogDescription = s.substring(0, idx);
        if (logList.contains(vsLogDescription))
        {
          continue;
        }

        logList.add(vsLogDescription);
        LogScrollPane logScrollPane = null;
        JScrollPane jScrollPane = null;
        if (vsLogDescription.indexOf(LogConsts.ERROR_LOG_NAME) != -1)
        {
          logScrollPane = new ErrorLogScrollPane();
          jScrollPane = logScrollPane.initialize(null, true, parentFrame);
          setViewRemoteData(logScrollPane, vsLogDescription);
          errorLogCount++;
          addLogScrollPaneToTabbedPane(vsLogDescription,
                           logScrollPane, "Error Logs " + controllerGroupName, jScrollPane, errorLogCount);
          getLogEntryCount(vsLogDescription);
        }
        else if (vsLogDescription.indexOf(LogConsts.OPERATION_LOG_NAME) != -1)
        {
          logScrollPane = new OperationLogScrollPane();
          jScrollPane = logScrollPane.initialize(null, true, parentFrame);
          setViewRemoteData(logScrollPane, vsLogDescription);
          operationLogCount++;
          addLogScrollPaneToTabbedPane(vsLogDescription,
                           logScrollPane, "Operation Logs " + controllerGroupName, jScrollPane, operationLogCount);
          getLogEntryCount(vsLogDescription);
        }
        else if (vsLogDescription.indexOf(LogConsts.SYSTEM_LOG_NAME) != -1)
        {
          logScrollPane = new SystemLogScrollPane();
          jScrollPane = logScrollPane.initialize(null, true, parentFrame);
          setViewRemoteData(logScrollPane, vsLogDescription);
          systemLogCount++;
          addLogScrollPaneToTabbedPane(vsLogDescription,
                           logScrollPane, "System Logs " + controllerGroupName, jScrollPane, systemLogCount);
          getLogEntryCount(vsLogDescription);
        }
        else if (s.indexOf("Equipment Logs") != -1)
        {
          logScrollPane = new EquipmentLogScrollPane();
          jScrollPane = logScrollPane.initialize(null, false, parentFrame);
          setViewRemoteData(logScrollPane, vsLogDescription + LogConsts.EQUIPMENT_LOG_NAME);
          addEquipmentLogScrollPaneToTabbedPane(vsLogDescription + LogConsts.EQUIPMENT_LOG_NAME,
                           logScrollPane, vsLogDescription, jScrollPane);
          vsLogDescription = vsLogDescription + LogConsts.EQUIPMENT_LOG_NAME;
          getLogEntryCount(vsLogDescription);
        }
        else if (s.indexOf("Comm Logs") != -1 && SKDCUserData.isSuperUser())
        {
          logScrollPane = new CommLogScrollPane();
          jScrollPane = logScrollPane.initialize(null, false, parentFrame);
          setViewRemoteData(logScrollPane, vsLogDescription + LogConsts.COMM_LOG_NAME);
          addCommLogScrollPaneToTabbedPane(vsLogDescription + LogConsts.COMM_LOG_NAME,
                           logScrollPane, vsLogDescription, jScrollPane);
          vsLogDescription = vsLogDescription + LogConsts.COMM_LOG_NAME;
          getLogEntryCount(vsLogDescription);
        }
        
        if (logScrollPane != null)
        {
          logScrollPane.setUserHandlingNeeded(true);
          logScrollPanes.put(vsLogDescription, logScrollPane);
        }
      }
    }
    catch (Exception e)
    {
      logger.logException(e, "\n\n" + sText + "\n\n");
    }
    parentFrame.setVisible(true);
  }

  /*-------------------------------------------------------------------------*/
  /*-------------------------------------------------------------------------*/
  /**
   * Process the number of available log entries.  The received message is a
   * tab-delimited string containing the log name, entry count, and current log
   * sink entry number.
   * 
   * @param sText
   */
  private void processLogEntryCount(String sText)
  {
    synchronized (activeClientLock)
    {
      updateLogEntryValues(sText);
    }
  }

  /*-------------------------------------------------------------------------*/
  /*-------------------------------------------------------------------------*/
  /**
   * Process the received range of log entries.  The received message is a
   * tab-delimited string containing the log name, entry count, current log
   * sink entry number and the log entries.
   * 
   * @param sText
   */
  private void processLogEntries(String sText)
  {
    synchronized (activeClientLock)
    {
      int idx = sText.indexOf("\n");
      String vsLogEntryValues = sText.substring(0, idx);
      String vsLogEntries = sText.substring(idx + 1);
      //
      updateLogEntryValues(vsLogEntryValues);
      //
      // updateLogEntryValues has updated activeRemoteLogClient.
      //
      // Let the activeRemoteLogClient actually process the entries by adding
      // them to its cache.
      //
      activeRemoteLogClient.readLogs(vsLogEntries);
      timers.setPeriodicTimerEvent(logUpdateTimeout, 2500);
    }
  }

  /*-------------------------------------------------------------------------*/
  private void updateLogEntryValues(String sText)
  {
    StringTokenizer stringTokenizer = new StringTokenizer(sText, "\t");
    String vsLogDescription = stringTokenizer.nextToken();
    activeRemoteLogClient = remoteLogClients.get(vsLogDescription);
    //
    String vsLogEntryCount = stringTokenizer.nextToken();
    Integer logEntryCountInteger = Integer.valueOf(vsLogEntryCount);
    int logEntryCount = logEntryCountInteger.intValue();
    activeRemoteLogClient.setEntryCount(logEntryCount);
    //
    String vsLogEntrySink = stringTokenizer.nextToken();
    logEntryCountInteger = Integer.valueOf(vsLogEntrySink);
    int logEntrySink = logEntryCountInteger.intValue();
    activeRemoteLogClient.setLogSinkEntryNumber(logEntrySink);
    //
    String vsLatestLogEntryNumber = stringTokenizer.nextToken();
    logEntryCountInteger = Integer.valueOf(vsLatestLogEntryNumber);
    int latestLogEntryNumber = logEntryCountInteger.intValue();
    activeRemoteLogClient.setLatestLogEntryNumber(latestLogEntryNumber);
  }

  /*-------------------------------------------------------------------------*/
  /*-------------------------------------------------------------------------*/
  /**
   * Process the number of available log entries.  The received message is a
   * tab-delimited string containing the log name, entry count, and current log
   * sink entry number.
   * 
   * @param sText
   */
  private void processFilteredLogEntryCount(String sText)
  {
    synchronized (activeClientLock)
    {
      updateFilteredLogEntryValues(sText);
    }
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Process the received range of log entries.  The received message is a
   * tab-delimited string containing the log name, entry count, current log
   * sink entry number and the log entries.
   * 
   * @param sText
   */
  private void processFilteredLogEntries(String sText)
  {
    synchronized (activeClientLock)
    {
      int idx = sText.indexOf("\n");
      String vsLogEntryValues = sText.substring(0, idx);
      String vsLogEntries = sText.substring(idx + 1);
      //
      updateFilteredLogEntryValues(vsLogEntryValues);
      //
      // updateLogEntryValues has updated activeRemoteLogClient.
      //
      // Let the activeRemoteLogClient actually process the entries by adding
      // them to its cache.
      //
      activeRemoteLogClient.readLogs(vsLogEntries);
      timers.setPeriodicTimerEvent(logUpdateTimeout, 2500);
    }
  }
    
  /*-------------------------------------------------------------------------*/
  private void updateFilteredLogEntryValues(String sText)
  {
    StringTokenizer stringTokenizer = new StringTokenizer(sText, "\t");
    String vsLogDescription = stringTokenizer.nextToken();
    activeRemoteLogClient = remoteLogClients.get(vsLogDescription);
    //
    String vsLogEntryCount = stringTokenizer.nextToken();
    Integer logEntryCountInteger = Integer.valueOf(vsLogEntryCount);
    int logEntryCount = logEntryCountInteger.intValue();
    //
    String vsLogEntrySink = stringTokenizer.nextToken();
    logEntryCountInteger = Integer.valueOf(vsLogEntrySink);
    int logEntrySink = logEntryCountInteger.intValue();
    //
    String vsLatestLogEntryNumber = stringTokenizer.nextToken();
    logEntryCountInteger = Integer.valueOf(vsLatestLogEntryNumber);
    int latestLogEntryNumber = logEntryCountInteger.intValue();
    //
    String filterText = stringTokenizer.nextToken();
    //
    String vsFilterIndex = stringTokenizer.nextToken();
    logEntryCountInteger = Integer.valueOf(vsFilterIndex);
    int filterIndex = logEntryCountInteger.intValue();
    //
    activeRemoteLogClient.setEntryCount(logEntryCount, filterText, filterIndex);
    activeRemoteLogClient.setLogSinkEntryNumber(logEntrySink, filterText, filterIndex);
    activeRemoteLogClient.setLatestLogEntryNumber(latestLogEntryNumber, filterText, filterIndex);
 }

 /*-------------------------------------------------------------------------*/
 /**
  * Process the received range of log entries.  The received message is a
  * tab-delimited string containing the log name and found entry number
  * (-1 if not found).
  * 
  * @param sText
  */
 private void processFindText(String sText)
 {
   synchronized (activeClientLock)
   {
     StringTokenizer stringTokenizer = new StringTokenizer(sText, "\t");
     String vsLogDescription = stringTokenizer.nextToken();
     activeRemoteLogClient = remoteLogClients.get(vsLogDescription);
     //
     String vsFoundEntry = stringTokenizer.nextToken();
     Integer vnFoundEntry = Integer.valueOf(vsFoundEntry);
     int viFoundEntry = vnFoundEntry.intValue();
     //
     activeLogScrollPane.setFoundEntry(viFoundEntry);
   }
 }
    
  /*-------------------------------------------------------------------------*/
  /*-------------------------------------------------------------------------*/
  void getLogEntryCount(String vpLogDescription)
  {
    publishLogEvent(vpLogDescription + "\tGet Log Entry Count", 
        LogConsts.SEND_LOG_ENTRY_COUNT, 
        SKDCConstants.LOG_SERVER);
  }

  void getLogEntryCount(String vpLogDescription, String filterText, int filterIndex)
  {
    publishLogEvent(vpLogDescription + "\t" + filterText + "\t" + filterIndex
        + "\tGet Log Entry Count", LogConsts.SEND_FILTERED_LOG_ENTRY_COUNT,
        SKDCConstants.LOG_SERVER);
  }

  /*------------------------------------------------------------------------*/
  /**
   * Create a RemoteLogClient, give it our logger and system gateway, set
   * its field indexes for the log data it has.
   *
   * @param logDataModel ?
   * @param dataName the log name/description
   */
  private void setViewRemoteData(LogDataModel logDataModel, String dataName)
  {
    RemoteLogClient remoteLogClient = new RemoteLogClient(dataName,
        controllerGroupName, logger.getLoggerInstanceName());
    remoteLogClient.initializeCacheBlocks(0x10, 0x100);
    remoteLogClient.setColumnToFieldMap(logDataModel.getColumnToFieldMap());
    remoteLogClients.put(dataName, remoteLogClient);
    logDataModel.setData(remoteLogClient);
  }

  /*------------------------------------------------------------------------*/
  /**
   * Attach a log view to this frame by creating a LoggerScrollPane and adding
   * it to the mainTabbedPane.
   *
   * @param dataName
   * @param logScrollPane
   * @param logPaneName the name of the tab
   * @param jScrollPane
   * @param logCount the count of this type of log being added
   */
  private void addLogScrollPaneToTabbedPane(String dataName, LogScrollPane logScrollPane, String logPaneName,
                                            JScrollPane jScrollPane, int logCount)
  {
    logScrollPane.setDataName(dataName);
    logScrollPaneList.add(logScrollPane);
    logJScrollPaneList.add(jScrollPane);
    //Add the log scroll pane to the Tabbed Panel.
    if (logCount > 1)
    {
      logPaneName = logPaneName + logCount;
    }
    mainTabbedPane.add(logPaneName, jScrollPane);
  }

  /*------------------------------------------------------------------------*/
  void addEquipmentLogScrollPaneToTabbedPane(String logName, LogScrollPane logScrollPane, String logPaneName,
                                            JScrollPane jScrollPane)
  {
    if (equipmentLogsTabbedPane == null)
    {
      equipmentLogsTabbedPane = new JTabbedPane();
      mainTabbedPane.add("Equipment Logs", equipmentLogsTabbedPane);
    }
    logScrollPane.setDataName(logName);
    logScrollPaneList.add(logScrollPane);
    logJScrollPaneList.add(jScrollPane);
    //Add the log scroll pane to the Tabbed Panel.
    equipmentLogsTabbedPane.add(logPaneName, jScrollPane);
  }

  /*------------------------------------------------------------------------*/
  void addCommLogScrollPaneToTabbedPane(String logName, LogScrollPane logScrollPane, String logPaneName,
                                            JScrollPane jScrollPane)
  {
    if (commLogsTabbedPane == null)
    {
      commLogsTabbedPane = new JTabbedPane();
      mainTabbedPane.add("Comm Logs", commLogsTabbedPane);
    }
    logScrollPane.setDataName(logName);
    logScrollPaneList.add(logScrollPane);
    logJScrollPaneList.add(jScrollPane);
    //Add the log scroll pane to the Tabbed Panel.
    commLogsTabbedPane.add(logPaneName, jScrollPane);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Publish a "Control" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   *
   * @param sEvent the String data content to be sent
   * @param iEvent the int data content to be sent
   * @param sCKN the message destination
   */
  private void publishControlEvent(String sEvent, int iEvent, String sCKN)
  {
    getSystemGateway().publishControlEvent(sEvent, iEvent, sCKN);
  }

  /*--------------------------------------------------------------------------*/
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

  /*--------------------------------------------------------------------------*/
  void getActiveRemoteLogClientLogEntryCount()
  {
    synchronized (activeClientLock)
    {
      //
      // Find the Remote Log Viewer Scroll Pane that is showing and request
      // its log entry count so we can update the view, if needed.
      //
      Iterator clientsKeyIterator = remoteLogClients.keySet().iterator();
      while (clientsKeyIterator.hasNext())
      {
        String vsLogName = (String)clientsKeyIterator.next();
        LogScrollPane logScrollPane = logScrollPanes.get(vsLogName);
        try
        {
        if (logScrollPane.isShowing())
        {
          //
          // We have a Log View that is showing.  Does the user want it stopped?
          //
          if (! logScrollPane.getUserUsingLogGrid())
          {
            //
            // Request its entry count.
            //
            activeLogScrollPane = logScrollPane;
            activeRemoteLogClient = remoteLogClients.get(vsLogName);
            if (!activeRemoteLogClient.getFilterActive())
            {
              getLogEntryCount(vsLogName);
            }
            else
            {
              String filterText = activeRemoteLogClient.getFilterText();
              int filterIndex = activeRemoteLogClient.getFilterIndex();
              getLogEntryCount(vsLogName, filterText, filterIndex);
            }
          }
          break;
        }
        }
        catch (Exception e)
        {
          logger.logDebug(vsLogName + "help");
        }
      }
      requestLogListInterval--;
      if (requestLogListInterval == 0)
      {
        requestRemoteLogList();
        requestLogListInterval = 4;
      }
    }
  }
  
  private class LogUpdateTimeout extends RestartableTimerTask
  {
    
    private Thread mpThread = Thread.currentThread();
    
    /*------------------------------------------------------------------------*/
    /**
     * run -- the LocalTimerTask's run() needs to be synchronized on activeClientLock
     * so that any work we do here is not interrupted by any incoming messages
     * or events.  We want to complete anything we do here without being pre-empted.
     */
    public void run()
    {
      if (mpThread == null)
        return;
      SystemGateway vpSystemGateway = ThreadSystemGateway.get(mpThread);
      if (vpSystemGateway == null)
      {
        mpThread = null;
        return;
      }
      (vpSystemGateway).invokeLater
      (
        new Runnable()
        {
          public void run()
          {
            getActiveRemoteLogClientLogEntryCount();
          }
        }
      );
    }
  }
}

