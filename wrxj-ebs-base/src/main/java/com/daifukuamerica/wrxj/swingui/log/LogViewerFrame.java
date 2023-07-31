package com.daifukuamerica.wrxj.swingui.log;

import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.LogDataModel;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.io.LogFileReaderWriter;
import com.daifukuamerica.wrxj.log.io.LogReaderWriter;
import com.daifukuamerica.wrxj.log.view.CommLogScrollPane;
import com.daifukuamerica.wrxj.log.view.EquipmentLogScrollPane;
import com.daifukuamerica.wrxj.log.view.ErrorLogScrollPane;
import com.daifukuamerica.wrxj.log.view.LogScrollPane;
import com.daifukuamerica.wrxj.log.view.OperationLogScrollPane;
import com.daifukuamerica.wrxj.log.view.SystemLogScrollPane;
import com.daifukuamerica.wrxj.swing.SKDCFileChooserFrame;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.InternalFrameEvent;

/**
 * Swing user interface for display of application logs.
 *
 * @author  Stephen Kendorski
 * @version 1.0
 */
public class LogViewerFrame extends SKDCInternalFrame
{
  private static final long serialVersionUID = 0L;
  
  private static String logPath = null;

  private LogReaderWriter logReaderWriter = new LogFileReaderWriter();
  private List logScrollPaneList = new ArrayList();
  private int systemLogCount = 0;
  private int operationLogCount = 0;
  private int errorLogCount = 0;
  private List fileNameList = null;

  private LogObserver logObserver = null;
  private SKDCInternalFrame thisFrame = this;

  private JPanel logViewerPane = new JPanel();
  private JTabbedPane mainTabbedPane = new JTabbedPane();
  private JTabbedPane commLogsTabbedPane = null;
  private JTabbedPane equipmentLogsTabbedPane = null;
  private BorderLayout borderLayout2 = new BorderLayout();

  public LogViewerFrame(String isTitle)
  {
    super(isTitle);
    setAllowDuplicateScreens(true);
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

  public LogViewerFrame()
  {
    this("");
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);
    String[] logs = new String[] {
        "Remote Logs",
        "Saved Logs",
        "Local Logs"
        };
    String input = (String)JOptionPane.showInputDialog(
                      LogViewerFrame.this,
                      "Please select logs to view",
                      "Log Viewer Selection", JOptionPane.INFORMATION_MESSAGE,
                      null, logs,
                      "Remote Logs");
    if (input == null)
    {
      close();
    }
    else if (input.equals("Remote Logs"))
    {
      attachRemoteLogView();
    }
    else if (input.equals("Saved Logs"))
    {
      SKDCFileChooserFrame fileChooser = new SKDCFileChooserFrame();
      this.getParent().add(fileChooser);
      fileChooser.setMultiSelectionEnabled(true);
      while (true)
      {
        if (logPath == null)
        {
          logPath = logReaderWriter.getLogPath();
        }
        fileNameList = fileChooser.getFileChoices(logPath);
        fileChooser.close();
        if (fileNameList != null)
        {
          int si = fileNameList.size();
          if (si == 0)
          {
            break;
          }
          String fileName = (String)fileNameList.get(0);
          String sFileName = fileName.toLowerCase(); 
          int startIndex = fileName.lastIndexOf('~');
          int endIndex = sFileName.lastIndexOf(LogConsts.LOG_FILE_EXTENSION.toLowerCase());
          if (endIndex == -1)
          {
            endIndex = sFileName.lastIndexOf(LogConsts.LOG_ZIP_FILE_EXTENSION.toLowerCase());
          }
          if ((startIndex > 0) && (endIndex > 0) && (startIndex != endIndex))
          {
            logPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
            this.setTitle("Log Viewer  -  " + fileName);
            break;
          }
          else
          {
            fileNameList = null;
            displayInfoAutoTimeOut("Not a Log File.", "Log Viewer");
          }
        }
        else
        {
          break;
        }
      }
      new Thread("LogViewerFrame-ReadLogs")
      {
        @Override
        public void run()
        {
          readLogs();
        }
      }.start();
    }
    else
    {
      attachLocalLogView();
    }
  }
  
  void readLogs()
  {
    final Object vpWfHandle = openWaitFrame("Reading Logs");
    if (fileNameList != null)
    {
      attachSavedLogView(fileNameList);
    }
    closeWaitFrame(vpWfHandle);
  }

  private void attachRemoteLogView()
  {
    logObserver = new LogObserver();
    logObserver.setParentFrame(thisFrame);
    logObserver.setParentFrameDefaultTitle(defaultTitle);
    logObserver.setMainTabbedPane(mainTabbedPane);
    if (SKDCUserData.isSuperUser())
    {
      logObserver.setCommLogsTabbedPane(commLogsTabbedPane);
    }
    logObserver.setEquipmentLogsTabbedPane(equipmentLogsTabbedPane);
    logObserver.initialize();
  }

  private void jbInit() throws Exception
  {
    this.setTitle("Log Viewer");
    this.setPreferredSize(new Dimension(800, 400));
    logViewerPane.setLayout(borderLayout2);
    this.getContentPane().add(logViewerPane, BorderLayout.CENTER);
    logViewerPane.add(mainTabbedPane,  BorderLayout.CENTER);
  }

  /*--------------------------------------------------------------------------*/
  @Override
  protected void shutdownFrame()
  {
    logger.logDebug("removing logObserver - shutdownFrame()");
    if (logObserver != null)
    {
      logObserver.cleanUp();
      logObserver = null;
    }
    Iterator logIterator = logScrollPaneList.iterator();
    while (logIterator.hasNext())
    {
      LogScrollPane aPane = (LogScrollPane)logIterator.next();
      aPane.cleanUpOnClose();
    }
    logScrollPaneList.clear();
    logScrollPaneList = null;
    logReaderWriter = null;
    //
    fileNameList = null;
    thisFrame = null;
    //
    super.shutdownFrame();
  }

  /*------------------------------------------------------------------------*/
  /*------------------------------------------------------------------------*/
  private void attachSavedLogView(List vpFileNameList)
  {
    //
    // We are viewing logs that have been saved to a file.
    //
    Iterator fileListIterator = vpFileNameList.iterator();
    while (fileListIterator.hasNext())
    {
      String fileName = (String)fileListIterator.next();
      String sFileName = fileName.toLowerCase(); 
      int startIndex = fileName.lastIndexOf('~');
      int endIndex = sFileName.lastIndexOf(LogConsts.LOG_FILE_EXTENSION.toLowerCase());
      if (endIndex == -1)
      {
        endIndex = sFileName.lastIndexOf(LogConsts.LOG_ZIP_FILE_EXTENSION.toLowerCase());
      }
      if ((startIndex > 0) && (endIndex > 0) && (startIndex != endIndex))
      {
        startIndex = fileName.lastIndexOf(File.separator);
        String dataName = fileName.substring(startIndex, fileName.length());
        endIndex = dataName.indexOf('~');
        dataName = dataName.substring(1, endIndex);
        startIndex = dataName.lastIndexOf(' ');
        if (startIndex > -1)
        {
          dataName = dataName.substring(startIndex + 1);
        }
        if (fileName.indexOf(LogConsts.ERROR_LOG_NAME) != -1)
        {
          LogScrollPane logScrollPane = new ErrorLogScrollPane();
          JScrollPane jScrollPane = logScrollPane.initialize(Logger.getErrorLogger(), true, thisFrame);
          setViewData(logScrollPane, fileName, LogConsts.ERROR_LOG_NAME);
          errorLogCount++;
          addLogScrollPaneToTabbedPane(LogConsts.ERROR_LOG_NAME,
                           logScrollPane, "Error Logs", jScrollPane, errorLogCount);
        }
        else if (fileName.indexOf(LogConsts.OPERATION_LOG_NAME) != -1)
        {
          LogScrollPane logScrollPane = new OperationLogScrollPane();
          JScrollPane jScrollPane = logScrollPane.initialize(Logger.getOperationLogger(), true, thisFrame);
          setViewData(logScrollPane, fileName, LogConsts.OPERATION_LOG_NAME);
          operationLogCount++;
          addLogScrollPaneToTabbedPane(LogConsts.OPERATION_LOG_NAME,
                           logScrollPane, "Operation Logs", jScrollPane, operationLogCount);
        }
        else if (fileName.indexOf(LogConsts.SYSTEM_LOG_NAME) != -1)
        {
          LogScrollPane logScrollPane = new SystemLogScrollPane();
          JScrollPane jScrollPane = logScrollPane.initialize(null, true, thisFrame);
          setViewData(logScrollPane, fileName, LogConsts.SYSTEM_LOG_NAME);
          systemLogCount++;
          addLogScrollPaneToTabbedPane(LogConsts.SYSTEM_LOG_NAME,
                           logScrollPane, "System Logs", jScrollPane, systemLogCount);
        }
        else if (fileName.indexOf(LogConsts.EQUIPMENT_LOG_NAME) != -1)
        {
          LogScrollPane logScrollPane = new EquipmentLogScrollPane();
          JScrollPane jScrollPane = logScrollPane.initialize(null, false, thisFrame);
          if (setViewData(logScrollPane, fileName, dataName))
          {
            String s = fileName.substring(fileName.indexOf(LogConsts.EQUIPMENT_LOG_NAME) +
                       LogConsts.EQUIPMENT_LOG_NAME.length(), fileName.length() - 4);
            addEquipmentLogScrollPaneToTabbedPane(dataName,
                             logScrollPane, dataName + "-" + s, jScrollPane);
          }
        }
        else if (fileName.indexOf(LogConsts.COMM_LOG_NAME) != -1 &&
                 SKDCUserData.isSuperUser())
        {
          LogScrollPane logScrollPane = new CommLogScrollPane();
          JScrollPane jScrollPane = logScrollPane.initialize(null, false, thisFrame);
          if (setViewData(logScrollPane, fileName, dataName))
          {
            String s = fileName.substring(fileName.indexOf(LogConsts.COMM_LOG_NAME) +
                       LogConsts.COMM_LOG_NAME.length(), fileName.length() - 4);
            addCommLogScrollPaneToTabbedPane(dataName,
                             logScrollPane, dataName + "-" + s, jScrollPane);
          }
        }
      }
    }
  }

  private void attachLocalLogView()
  {
    LogScrollPane logScrollPane = new ErrorLogScrollPane();
    JScrollPane jScrollPane = logScrollPane.initialize(Logger.getErrorLogger(), true, thisFrame);
    errorLogCount++;
    addLogScrollPaneToTabbedPane(LogConsts.ERROR_LOG_NAME,
                     logScrollPane, "Error Logs", jScrollPane, errorLogCount);
    //
    logScrollPane = new OperationLogScrollPane();
    jScrollPane = logScrollPane.initialize(Logger.getOperationLogger(), true, thisFrame);
    operationLogCount++;
    addLogScrollPaneToTabbedPane(LogConsts.OPERATION_LOG_NAME,
                     logScrollPane, "Operation Logs", jScrollPane, operationLogCount);
    //
    logScrollPane = new SystemLogScrollPane();
    jScrollPane = logScrollPane.initialize(Logger.getSystemLogger(), true, thisFrame);
    systemLogCount++;
    addLogScrollPaneToTabbedPane(LogConsts.SYSTEM_LOG_NAME,
                     logScrollPane, "System Logs", jScrollPane, systemLogCount);
    //
    List loggers = Logger.getLoggerInstances();
    List c = null;
    synchronized(loggers)
    {
      c = new ArrayList(loggers);
    }
    Iterator logIterator = c.iterator();
    while (logIterator.hasNext())
    {
      Logger aLogger = (Logger)logIterator.next();
      if (aLogger.getEquipmentLogger() != null)
      {
        logScrollPane = new EquipmentLogScrollPane();
        jScrollPane = logScrollPane.initialize(aLogger.getEquipmentLogger(), false, thisFrame);
        addEquipmentLogScrollPaneToTabbedPane(aLogger.getLoggerInstanceName(),
                         logScrollPane, aLogger.getLoggerInstanceName() + " EquipmentLogs", jScrollPane);
      }
    }

    /*
     * Don't show comm logs except to super-user
     */
    if (!SKDCUserData.isSuperUser())
    {
      return;
    }
    logIterator = c.iterator();
    while (logIterator.hasNext())
    {
      Logger aLogger = (Logger)logIterator.next();
      if (aLogger.getCommLogger() != null)
      {
        logScrollPane = new CommLogScrollPane();
        jScrollPane = logScrollPane.initialize(aLogger.getCommLogger(), false, thisFrame);
        addCommLogScrollPaneToTabbedPane(aLogger.getLoggerInstanceName(),
                         logScrollPane, aLogger.getLoggerInstanceName() + " CommLogs", jScrollPane);
      }
    }
  }

  /*------------------------------------------------------------------------*/
  private boolean setViewData(LogDataModel logDataModel, String fileName, String dataName)
  {
    boolean result = true;
    Object logData = logReaderWriter.readLogs(fileName, logDataModel.getColumnToFieldMap() , null);
    if (logData != null)
    {
      logDataModel.setData(logData);
    }
    else
    {
      result = false;
    }
    logDataModel.setDataName(dataName);
    return result;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Attach a log view to this frame by creating a LoggerScrollPane and adding
   * it to the mainTabbedPane.
   *
   * @param aLogger the LogFrameView to add
   * @param logPaneName the name of the tab
   * @param resizeFrame if true, resize the logScrollPane for the log view
   * @param logCount the count of this type of log being added
   *
   * @return the LoggerScrollPane that was added to the mainTabbedPane
   */
  private void addLogScrollPaneToTabbedPane(String dataName,
      LogScrollPane logScrollPane, String logPaneName, JScrollPane jScrollPane,
      int logCount)
  {
    logScrollPane.setDataName(dataName);
    logScrollPaneList.add(logScrollPane);
    //Add the log scroll pane to the Tabbed Panel.
    if (logCount > 1)
    {
      logPaneName = logPaneName + logCount;
    }
    mainTabbedPane.add(logPaneName, jScrollPane);
  }

  /*------------------------------------------------------------------------*/
  private void addEquipmentLogScrollPaneToTabbedPane(String logName,
      LogScrollPane logScrollPane, String logPaneName, JScrollPane jScrollPane)
  {
    if (equipmentLogsTabbedPane == null)
    {
      equipmentLogsTabbedPane = new JTabbedPane();
      mainTabbedPane.add("Equipment Logs", equipmentLogsTabbedPane);
    }
    logScrollPane.setDataName(logName);
    logScrollPaneList.add(logScrollPane);
    //Add the log scroll pane to the Tabbed Panel.
    equipmentLogsTabbedPane.add(logPaneName, jScrollPane);
  }

  /*------------------------------------------------------------------------*/
  private void addCommLogScrollPaneToTabbedPane(String logName,
      LogScrollPane logScrollPane, String logPaneName, JScrollPane jScrollPane)
  {
    if (!SKDCUserData.isSuperUser())
    {
      return;
    }
    if (commLogsTabbedPane == null)
    {
      commLogsTabbedPane = new JTabbedPane();
      mainTabbedPane.add("Comm Logs", commLogsTabbedPane);
    }
    logScrollPane.setDataName(logName);
    logScrollPaneList.add(logScrollPane);
    //Add the log scroll pane to the Tabbed Panel.
    commLogsTabbedPane.add(logPaneName, jScrollPane);
  }
}
