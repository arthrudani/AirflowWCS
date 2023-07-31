package com.daifukuamerica.wrxj.log.io;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.log.Log;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.LogDataModel;
import com.daifukuamerica.wrxj.log.LogTableModel;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.LoggingDataAccess;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.timer.RestartableTimer;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;

/**
 * Load/Save logs from/to a system file.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
 public class LogFileReaderWriter implements LogReaderWriter
{
  private static int SAVED_LOG_EXTENSION_SIZE = 1000;
  private Log savedLogs = null;

  private static String logPath = null;
  private static String logGroupRootPath = null;
  private static String systemLogPath = null;
  private static String operationLogPath = null;
  private static String errorLogPath = null;
  private static String commLogPath = null;
  private static String equipmentLogPath = null;

  private File oldestFile = null;
  private String oldestFileName = null;
  private long oldestFileDateTime = 0;
  private long earliestDateTimeToKeep = 0; // 0 says do NOT delete by age.
  private long maxLogFilesSize = LogConsts.MAX_LOG_FILES_SIZE_MB;
  private long maxLogZipFilesSize = LogConsts.MAX_LOG_ZIP_FILES_SIZE_MB;
  
  private LogDataModel saveLogsDataModel = null;
  private RestartableTimer saveLogsTimer = null;
  private SaveLogsTimeout saveLogsTimeout = null;
  private int saveLogsTimerCounter = LogConsts.AUTO_CLEANUP_LOGS_INTERVAL;
  private int saveFileMaxSize = LogConsts.AUTO_SAVE_LOG_MAX_FILE_SIZE;
  private String saveLogsFilePath = null;
  private String saveLogsFileName = null;
  private String saveLogsFilePathName = null;
  private boolean saveLogsIOException = false;
  private int nextLogEntryNumberToSave = 0;
  private String currentLogFilePathName = null;
  private boolean archiveOldestFiles = true;
  private byte[] archiveLogFileBuffer = new byte[0x10000];
  private boolean directorySizeExceeded = false;
  private String writeLogFilePath = null;

  private static List<LogFileReaderWriter> logAutoSavers = new ArrayList<>();
  private static List<LogDataModel> logAutoSaverDataModels = new ArrayList<>();
  
  private Boolean mzRunning = true;
  
  /**
   * The Logging implementation for this named subsystem to use.
   */
  private Logger logger = Logger.getLogger();

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  static
  {
    logPath = Application.getString("LogFileReaderWriter.LogPath");
    if (logPath == null)
    {
      logPath = Application.getString(ControllerConsts.ROOT_PATH_PROPERTY, null);
      if (logPath != null)
      {
        if ((! logPath.endsWith("/")) && (! logPath.endsWith("\\")))
        {
          logPath = logPath + File.separator;
        }
        logPath = logPath + LogConsts.LOG_PATH;
      }
      else if (Application.getBoolean(WarehouseRx.LOAD_CONFIGS_FROM_RESOURCE, false))
      {
        // If no log path is defined and we're loading from resource (probably
        // because of Java Web Start), use [UserHomeDirectory]/WarehouseRxLogs
        logPath = new JFileChooser().getFileSystemView().getDefaultDirectory()
            + File.separator + "WarehouseRxLogs";
      }
    }
    if (logPath == null)
    {
      System.err.println("LogFileReaderWriter - Property \""
          + ControllerConsts.ROOT_PATH_PROPERTY + "\" - MISSING");
    }

    String vsJVMID = Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY);
    String vpRunMode = Application.getString(WarehouseRx.RUN_MODE, null);
    if (vsJVMID == null)
      vpRunMode = vpRunMode != null ? vpRunMode  : "Client";
    else
      vpRunMode = (vpRunMode != null ? vpRunMode  : "Client") + "_" + vsJVMID;
    
    logGroupRootPath = logPath + vpRunMode + File.separator;
    systemLogPath = logGroupRootPath + LogConsts.SYSTEM_LOG_PATH;
    operationLogPath = logGroupRootPath + LogConsts.OPERATION_LOG_PATH;
    errorLogPath = logGroupRootPath + LogConsts.ERROR_LOG_PATH;
    commLogPath = logGroupRootPath + LogConsts.COMM_LOG_PATH;
    equipmentLogPath = logGroupRootPath + LogConsts.EQUIPMENT_LOG_PATH;
    //
    LogDataModel logDataModel = new LogTableModel();
    LoggingDataAccess aLogger = (LoggingDataAccess)Logger.getSystemLogger();
    logDataModel.setData(Logger.getSystemLogger());
    logDataModel.setDataName(LogConsts.SYSTEM_LOG_NAME);
    logDataModel.setColumnToFieldMap(LogConsts.SYS_LOG_COLUMN_FIELDS);
    logDataModel.setNormalToTable(false); // NOT using JTable view!
    logAutoSaverDataModels.add(logDataModel);
    LogFileReaderWriter logAutoSaver = new LogFileReaderWriter();
    logAutoSavers.add(logAutoSaver);
    Logger vpLogging = Logger.getLogger("SystemLogAutoSaver");
    vpLogging.logDebug("Logs Root Path - \"" + logGroupRootPath + "\"");
    logAutoSaver.setLogger(vpLogging);
    //
    // 3rd parameter "null" says do NOT load this logger from existing log files.
    //
    logAutoSaver.startAutoLogSaver(logDataModel, systemLogPath, aLogger);
    //
    logDataModel = new LogTableModel();
    aLogger = (LoggingDataAccess)Logger.getOperationLogger();
    logDataModel.setData(aLogger);
    logDataModel.setDataName(LogConsts.OPERATION_LOG_NAME);
    logDataModel.setColumnToFieldMap(LogConsts.OPR_LOG_COLUMN_FIELDS);
    logDataModel.setNormalToTable(false); // NOT using JTable view!
    logAutoSaverDataModels.add(logDataModel);
    logAutoSaver = new LogFileReaderWriter();
    logAutoSavers.add(logAutoSaver);
    logAutoSaver.setLogger(Logger.getLogger("OperationLogAutoSaver"));
    //
    // 3rd parameter "!null" says DO load this logger from existing log files.
    //
    logAutoSaver.startAutoLogSaver(logDataModel, operationLogPath, aLogger);
    //
    logDataModel = new LogTableModel();
    aLogger = (LoggingDataAccess)Logger.getErrorLogger();
    logDataModel.setData(aLogger);
    logDataModel.setDataName(LogConsts.ERROR_LOG_NAME);
    logDataModel.setColumnToFieldMap(LogConsts.ERR_LOG_COLUMN_FIELDS);
    logDataModel.setNormalToTable(false); // NOT using JTable view!
    logAutoSaverDataModels.add(logDataModel);
    logAutoSaver = new LogFileReaderWriter();
    logAutoSavers.add(logAutoSaver);
    logAutoSaver.setLogger(Logger.getLogger("ErrorLogAutoSaver"));
    //
    // 3rd parameter "!null" says DO load this logger from existing log files.
    //
    logAutoSaver.startAutoLogSaver(logDataModel, errorLogPath, aLogger);
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  public LogFileReaderWriter()
  {
  }

  /*--------------------------------------------------------------------------*/
  public static void shutdownLogs()
  {
    Iterator<LogFileReaderWriter> logIterator = logAutoSavers.iterator();
    while (logIterator.hasNext())
    {
      LogFileReaderWriter aLogAutoSaver = logIterator.next();
      aLogAutoSaver.shutdown();
    }
    logAutoSavers.clear();
    logAutoSavers = null;
    //
    logAutoSaverDataModels.clear();
    logAutoSaverDataModels = null;
    //
    logPath = null;
    logGroupRootPath = null;
    systemLogPath = null;
    operationLogPath = null;
    errorLogPath = null;
  }

  /*--------------------------------------------------------------------------*/
  private void shutdown()
  {
    synchronized (mzRunning)
    {
      //
      // Write out final logs.
      //
      try
      {
        autoWriteLogs();
      }
      catch (Exception e)
      {
        // If this happens, the logger is broken.
        // Allow mzRunning to change to false
        e.printStackTrace();
      }
      
      mzRunning = false;
      if (saveLogsTimer != null)
      {
        saveLogsTimer.cancel();
        saveLogsTimer = null;
      }
      saveLogsTimeout = null;
      saveLogsFilePath = null;
      saveLogsFileName = null;
      saveLogsFilePathName = null;
      currentLogFilePathName = null;
      oldestFile = null;
      savedLogs = null;
      saveLogsDataModel.setData(null);
      saveLogsDataModel = null;
      saveLogsTimeout = null;
      archiveLogFileBuffer = null;
      logger = null;
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Accessing this method will load the class and execute its static construction clause.
   *  The static construction clause (static block) creates one set of auto-log-savers
   *  for the application.
   */
  public static void autoSaveLogs()
  {
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Create auto-log-savers for the Comm & Equipment logs
   *  for the application.
   */
  public static void addEquipmentLogAutoSaver(Logger baseLogger)
  {
    LogDataModel logDataModel = new LogTableModel();
    logDataModel.setData(baseLogger.getEquipmentLogger());
    String logName = baseLogger.getLoggerInstanceName();
    logName = logName.substring(0, logName.indexOf(':'));
    logName = logName.replace('-', '_');
    logDataModel.setDataName(LogConsts.EQUIPMENT_LOG_NAME + "~" + logName);
    logDataModel.setColumnToFieldMap(LogConsts.COM_LOG_COLUMN_FIELDS);
    logDataModel.setNormalToTable(false); // NOT using JTable view!
    logAutoSaverDataModels.add(logDataModel);
    LogFileReaderWriter logAutoSaver = new LogFileReaderWriter();
    logAutoSavers.add(logAutoSaver);
    logAutoSaver.setLogger(Logger.getLogger(baseLogger.getLoggerInstanceName() +"LogAutoSaver"));
    //
    // 3rd parameter "null" says do NOT load this logger from existing log files.
    //
    logAutoSaver.startAutoLogSaver(logDataModel, equipmentLogPath, baseLogger.getEquipmentLogger());
  }
  
  public static void addCommLogAutoSaver(Logger baseLogger)
  {
    LogDataModel logDataModel = new LogTableModel();
    logDataModel.setData(baseLogger.getCommLogger());
    String logName = baseLogger.getLoggerInstanceName();
    logName = logName.substring(0, logName.indexOf(':'));
    logName = logName.replace('-', '_');
    logDataModel.setDataName(LogConsts.COMM_LOG_NAME + "~" + logName);
    logDataModel.setColumnToFieldMap(LogConsts.COM_LOG_COLUMN_FIELDS);
    logDataModel.setNormalToTable(false); // NOT using JTable view!
    logAutoSaverDataModels.add(logDataModel);
    LogFileReaderWriter logAutoSaver = new LogFileReaderWriter();
    logAutoSavers.add(logAutoSaver);
    logAutoSaver.setLogger(Logger.getLogger(baseLogger.getLoggerInstanceName() +"LogAutoSaver"));
    //
    // 3rd parameter "null" says do NOT load this logger from existing log files.
    //
    logAutoSaver.startAutoLogSaver(logDataModel, commLogPath, baseLogger.getCommLogger());
  }

  /*------------------------------------------------------------------------*/
  @Override
  public String getLogPath()
  {
    return logGroupRootPath;
  }

  private void setLogger(Object ipLogger)
  {
    logger = (Logger)ipLogger;
  }
  /*--------------------------------------------------------------------------*/
  //
  // 3rd parameter "!null" says DO load this logger from existing log files.
  //
  @Override
  public void startAutoLogSaver(Object logDataModel, String logsFilePath, Object loggerToLoad)
  {
    if (Application.getBoolean("reset_logs", false))
      loggerToLoad = null;
    if (logPath == null)
    {
      logger.logError("startAutoLogSaver() - LogPath is NULL");
    }
    //
    saveLogsDataModel = (LogDataModel)logDataModel;
    saveLogsFilePath = logsFilePath;
    saveLogsFileName = saveLogsDataModel.getDataName();
    saveLogsFilePathName = saveLogsFilePath + saveLogsFileName;
    //
    currentLogFilePathName = getCurrentLogFilePathName();
    if (currentLogFilePathName == null)
    {
      setNewCurrentLogFilePathName();
    }
    if (loggerToLoad != null)
    {
      readLogs(currentLogFilePathName, saveLogsDataModel.getColumnToFieldMap(), loggerToLoad);
    }
    saveLogsTimer = new RestartableTimer(logger.getLoggerInstanceName());
    saveLogsTimeout = new SaveLogsTimeout();
    Random random = new Random();
    int initialDelay = random.nextInt(2000) + 5000;
    saveLogsTimer.setPeriodicTimerEvent(saveLogsTimeout, LogConsts.AUTO_SAVE_LOGS_INTERVAL, initialDelay);
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public Object readLogs(String logName, int[] dataMap, Object vLogger)
  {
    Object result = null;
    BufferedReader fileReader = null;
    String tmpLogName = null;
    String zipLogName = null;
    String sLine = null;
    Log existingLogger = (Log)vLogger;
    int savedLogsSize = 0;
    int rc = 0;
    if (existingLogger == null)
    {
      savedLogsSize = SAVED_LOG_EXTENSION_SIZE;
      savedLogs = Logger.getLoggerInstance(savedLogsSize);
      rc++;
    }
    try
    {
      String sLogName = logName.toLowerCase(); 
      if (sLogName.endsWith(LogConsts.LOG_ZIP_FILE_EXTENSION))
      {
        //
        // This is a zipped log file - extract it.
        //
        zipLogName = logName;
        logName = logName.substring(0, logName.length() - LogConsts.LOG_ZIP_FILE_EXTENSION.length());
        tmpLogName = logName + LogConsts.LOG_TMP_FILE_EXTENSION;
        logName = tmpLogName;
        //
        // Create the ZIP input file.
        //
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipLogName));
        //
        // Access the first zipped entry in the file so we can read it.
        //
        zipInputStream.getNextEntry();
        //
        FileOutputStream fileOutputStream = new FileOutputStream(tmpLogName);
        //
        // Transfer bytes from the file to the ZIP file
        //
        int len;
        while ((len = zipInputStream.read(archiveLogFileBuffer)) > 0)
        {
          fileOutputStream.write(archiveLogFileBuffer, 0, len);
        }
        //
        // Complete the entry.
        //
        zipInputStream.close();
        fileOutputStream.close();
      }
      fileReader = new BufferedReader(new FileReader(logName));
      while (true)
      {
        sLine = fileReader.readLine();
        if (sLine == null)
        {
          break;
        }
        StringTokenizer st = new StringTokenizer(sLine, "\t");
        int cc = 0;
        Object[] logObjects = null;
        if (existingLogger == null)
        {
          if (rc >= savedLogsSize)
          {
            savedLogsSize += SAVED_LOG_EXTENSION_SIZE;
            savedLogs.setMaxLogEntries(savedLogsSize);
          }
        }
        logObjects = new Object[LogConsts.LOG_ENTRY_MAX_FIELDS];
        while (st.hasMoreTokens())
        {
          if (dataMap[cc] != LogConsts.LOG_ENTRY_CTLRKEY_IDX)
          {
            String sField = st.nextToken();
            if (sField.indexOf(0x01) != -1)
            {
              sField = sField.replace((char)0x01, (char)0x0d);
            }
            if (sField.indexOf(0x02) != -1)
            {
              sField = sField.replace((char)0x02, (char)0x0a);
            }
            if (sField.indexOf(0x03) != -1)
            {
              sField = sField.replace((char)0x03, (char)0x09);
            }
            logObjects[dataMap[cc]] = sField;
          }
          else
          {
            String s = st.nextToken();
            int ic = 0;
            for (int i = 0; i < s.length(); i++)
            {
              ic = (ic * 10) + s.charAt(i) - 0x30;
            }
            logObjects[dataMap[cc]] = Integer.valueOf(ic);
          }
          cc++;
        }
        if (existingLogger != null)
        {
          existingLogger.addLogEntry(logObjects);
        }
        else
        {
          savedLogs.addLogEntry(logObjects);
        }
        rc++;
      }
      fileReader.close();
      if (tmpLogName != null)
      {
        (new File(tmpLogName)).delete();
      }
    }
    catch (Exception e)
    {
      try
      {
        fileReader.close();
      }
      catch (Exception e1)
      {
      }
    }
    if (existingLogger == null)
    {
      result = savedLogs;
    }
    else
    {
      result = existingLogger;
    }
    nextLogEntryNumberToSave = rc;
    return result;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void writeLogs(LogDataModel logData, boolean append)
  {
    LogDataModel logDataModel = logData;
    PrintWriter printWriter = null;
    int colCount = logDataModel.getColumnCount();
    String logName = null;
    SkDateTime datetime = null;
    if (!append)
    {
      //
      // We are writing these logs to a new file.
      //
      String vpRunMode = Application.getString(WarehouseRx.RUN_MODE);
      if (vpRunMode == null)
      {
        vpRunMode = Logger.getSystemName();
      }
      String dataName = logDataModel.getDataName();
      if (dataName.indexOf(':') != -1)
      {
        dataName = dataName.substring(0, dataName.indexOf(':'));
      }
      datetime = new SkDateTime("yyyy-MM-dd~HHmmss");
      logName = writeLogFilePath + vpRunMode + "~" +
                datetime.getCurrentDateTimeAsString() + "~" +
                dataName +
                LogConsts.LOG_FILE_EXTENSION;
    }
    else
    {
      logName = currentLogFilePathName;
    }
    try
    {
      printWriter = new PrintWriter(new BufferedWriter(new FileWriter(logName, append)));
      saveLogsIOException = false;
    }
    catch (IOException e)
    {
      String eText = e.toString();
      if (eText.indexOf("being used by another process") != -1)
      {
        //
        // This can happen when we try to auto-save logs that are being read.
        // This is not catastrophic, so just return, we'll try again later.
        //
        return;
      }
      if (!saveLogsIOException)
      {
        saveLogsIOException = true;
        System.err.println(e);
      }
    }
    if (!saveLogsIOException)
    {
      if (!append)
      {
        //
        // Fetch the number of the earliest entry that is still in the logger.  This
        // number can be greater than the logger buffer capacity.
        //
        nextLogEntryNumberToSave = logDataModel.getEarliestEntryNumber();
      }
      String sLine = "";
      int viLogCounter = 0;
      while (true)
      {
        int latestEntryNumber = logDataModel.getLatestEntryNumber();
        if (nextLogEntryNumberToSave >= latestEntryNumber)
        {
          break;
        }
        //
        // Don't get caught on a loop writing huge numbers of logs without
        // checking that we've exceeded max log file sizes.
        //
        if (viLogCounter > 2000)
        {
          break;
        }
        while (nextLogEntryNumberToSave < latestEntryNumber)
        {
          for (int cc = 0; cc < colCount; cc++)
          {
            Object co = logDataModel.getValueAt(nextLogEntryNumberToSave, cc);
            if (co == null) continue;
            
            String sField = co.toString();
            if (sField.length() == 0)
            {
              sField = " ";
            }
            else
            {
              if (sField.indexOf(0x0d) != -1)
              {
                sField = sField.replace((char)0x0d, (char)0x01);
              }
              if (sField.indexOf(0x0a) != -1)
              {
                sField = sField.replace((char)0x0a, (char)0x02);
              }
              if (sField.indexOf(0x09) != -1)
              {
                sField = sField.replace((char)0x09, (char)0x03);
              }
            }
            sLine = sLine + sField + "\t";
          }
          sLine = sLine.substring(0, sLine.length() - 1);
          printWriter.println(sLine);
          sLine = "";
          nextLogEntryNumberToSave++;
          viLogCounter++;
        }
      }
    }
    try
    {
      printWriter.close();
    }
    catch (Exception e1)
    {
    }
    if (append)
    {
      updateCurrentLogFilePathName();
    }
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Create a unique directory when manually saving the logs to files
   * (by using the MonitorFrame "Save Logs" button).
   * 
   * @return the directory path of the saved files
   */
  @Override
  public String writeLogs()
  {
    SkDateTime datetime = new SkDateTime("yyyy-MM-dd~HHmmss");
    String vpRunMode = Application.getString(WarehouseRx.RUN_MODE);
    if (vpRunMode == null)
    {
      vpRunMode = Logger.getSystemName();
    }
    writeLogFilePath = logPath + vpRunMode + "~" +
                datetime.getCurrentDateTimeAsString();
    if (new File(writeLogFilePath).mkdirs())
    {
//      System.err.println("Created Log Directory \"" + writeLogFilePath + "\"");
    }
    else
    {
      System.err.println("UNABLE To Create Log Directory \"" + writeLogFilePath + "\"");
    }
    writeLogFilePath = writeLogFilePath + File.separator;
    //
    Iterator<LogDataModel> logAutoSaverDataModelsIterator = logAutoSaverDataModels.iterator();
    while (logAutoSaverDataModelsIterator.hasNext())
    {
      LogDataModel vpLogDataModel = logAutoSaverDataModelsIterator.next();
      writeLogs(vpLogDataModel, false);
    }
    return writeLogFilePath;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  private void handleOldestLogFiles()
  {
    while ((getLogFilesTotalSize(LogConsts.LOG_FILE_EXTENSION) >= maxLogFilesSize) ||
           ((earliestDateTimeToKeep != 0) && (oldestFileDateTime != 0) &&
            (oldestFileDateTime < earliestDateTimeToKeep)))
    {
      //
      // We have exceeeded the max size of all of the log files (*.txt) in this
      // directory.  Delete the oldest log file(s) ntil we're below the max.
      //
      boolean okToDeleteFile = true;
      if (archiveOldestFiles)
      {
        //
        // We need to archive (zip up) the log file before we delete it.
        //
//        logger.logOperation(LogConsts.OPR_LOG, "Archiving OLDEST Log File \"" +
//                              oldestFileName + "\"");
        okToDeleteFile = archiveLogFile(oldestFileName);
      }
      if (okToDeleteFile)
      {
//        logger.logOperation(LogConsts.OPR_LOG, "Deleting OLDEST Log File \"" +
//                              oldestFileName + "\"");
        oldestFile.delete();
      }
      else
      {
        break;
      }
    }
    //
    // Now, do the same max size check for the archived logs and delete the
    // oldest (zipped) archice file(s) to get us below the max for archived files
    // in this directory.
    //
    while ((getLogFilesTotalSize(LogConsts.LOG_ZIP_FILE_EXTENSION) >= maxLogFilesSize) ||
           ((earliestDateTimeToKeep != 0) && (oldestFileDateTime != 0) &&
            (oldestFileDateTime < earliestDateTimeToKeep)))
    {
      //logger.logOperation(LogConsts.OPR_LOG, "Deleting OLDEST ARCHIVE (zipped) Log File \"" +
//                            oldestFileName + "\"");
      oldestFile.delete();
    }
    checkDirectoryTotalSize();
  }

  /*--------------------------------------------------------------------------*/
  private long getLogFilesTotalSize(String fileExtension)
  {
    long totalLogFilesSize = 0;
    oldestFile = null;
    oldestFileDateTime = 0;
    File file = new File(saveLogsFilePath);
    File[] files = file.listFiles();
    if (files != null)
    {
      if (files.length > 0)
      {
        //
        // Start with the first file as the oldest.
        //
        int index = 0;
        File aFile = null;
        String fileName = "";
        while (index < files.length)
        {
          aFile = files[index];
          fileName = aFile.getName();
          if (validLogFile(fileName, fileExtension))
          {
            break;
          }
          index++;
        }
        if  (index < files.length)
        {
          //
          // Use the first file we found as oldest and total size.
          //
          totalLogFilesSize = aFile.length();
          oldestFile = aFile;
          oldestFileName = fileName;
          oldestFileDateTime = aFile.lastModified();
          //
          // Cycle through the rest of the files in the directory to find the oldest.
          //
          for (int i = index + 1; i < files.length; i++)
          {
            aFile = files[i];
            fileName = aFile.getName();
            if (!validLogFile(fileName, fileExtension))
            {
              continue;
            }
            totalLogFilesSize += aFile.length();
            long fileDateTime = aFile.lastModified();
            if (fileName.compareTo(oldestFileName) < 0)
            {
              oldestFile = aFile;
              oldestFileName = fileName;
              oldestFileDateTime = fileDateTime;
            }
          }
          //
          // We have found the oldest log file - and the total size of the log files.
          //
        }
      }
    }
    return totalLogFilesSize;
  }

  /*--------------------------------------------------------------------------*/
  private long checkDirectoryTotalSize()
  {
    long totalDirectorySize = 0;
    File file = new File(saveLogsFilePath);
    File[] files = file.listFiles();
    file = null;
    if (files != null)
    {
      for (int i = 0; i < files.length; i++)
      {
        file = files[i];
        totalDirectorySize += file.length();
      }
    }
    //
    // We have found the total size of ALL files in the Directory. Check to
    // see if (for some unknown reason) we have exceeded our allowable
    // directory size (less a little cushion).
    //
    if (totalDirectorySize > (maxLogFilesSize + maxLogZipFilesSize + 0x100000))
    {
      if (!directorySizeExceeded)
      {
        directorySizeExceeded = true;
        long maxSizeMB = (maxLogFilesSize + maxLogZipFilesSize) / 1000000;
        logger.logOperation(LogConsts.OPR_LOG, "Directory \"" + saveLogsFilePath + "\" Maximum Specified Size (" +
        maxSizeMB + "MB) EXCEEDED! - LogFileReaderWriter");
      }
    }
    else
    {
      if (directorySizeExceeded)
      {
        //
        // NEED TO... Take some action to show size is now under maximum.
        //
        directorySizeExceeded = false;
      }
    }
    return totalDirectorySize;
  }

  /*--------------------------------------------------------------------------*/
  private boolean archiveLogFile(String fileName)
  {
    boolean result = true;
    int endIdx = fileName.lastIndexOf(LogConsts.LOG_FILE_EXTENSION);
    String zipFileName = fileName.substring(0, endIdx) + LogConsts.LOG_ZIP_FILE_EXTENSION;
    String filePathName = null;
    String zipFilePathName = null;
    if (fileName.indexOf(File.separatorChar) == -1)
    {
      filePathName = saveLogsFilePath + fileName;
      zipFilePathName = saveLogsFilePath + zipFileName;
    }
    else
    {
      filePathName = fileName;
      zipFilePathName = zipFileName;
    }
    // Create a buffer for reading the files
    try
    {
      //
      // Create the ZIP file.
      //
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePathName));
      //
      // Compress the file.
      //
      FileInputStream in = new FileInputStream(filePathName);
      //
      // Add ZIP entry to output stream.
      //
      out.setLevel(9);
      out.putNextEntry(new ZipEntry(fileName));
      //
      // Transfer bytes from the file to the ZIP file
      //
      int len;
      while ((len = in.read(archiveLogFileBuffer)) > 0)
      {
        out.write(archiveLogFileBuffer, 0, len);
      }
      //
      // Complete the entry.
      //
      in.close();
      //
      // Complete the ZIP file
      //
      out.closeEntry();
      out.close();
      //
    }
    catch (IOException e)
    {
      result = false;
      logger.logSparseException(e, fileName);
    }
    return result;
  }

  /*--------------------------------------------------------------------------*/
  private String getCurrentLogFilePathName()
  {
    String result = null;
    if (!new File(saveLogsFilePath).exists())
    {
      String logDirectory = saveLogsFilePath.substring(0, saveLogsFilePath.length()-1); // lose file separator
      if (new File(logDirectory).mkdirs())
      {
//        System.err.println("Created Log Directory \"" + logDirectory + "\"");
      }
      else
      {
        System.err.println("UNABLE To Create Log Directory \"" + logDirectory + "\"");
      }
    }
    File file = new File(saveLogsFilePath);
    File[] files = file.listFiles();
    if (files != null)
    {
      List<String> fileNames = new ArrayList<>();
      if (files.length > 0)
      {
        for (int i = 0; i < files.length; i++)
        {
          File aFile = files[i];
          String fileName = aFile.getName();
          if (validLogFile(fileName, LogConsts.LOG_FILE_EXTENSION))
          {
            fileNames.add(fileName);
          }
        }
        int fileCount = fileNames.size();
        if (fileCount > 0)
        {
          for (int i = 0; i < fileCount - 1; i++)
          {
            for (int j = i + 1; j < fileCount; j++)
            {
              String si = fileNames.get(i);
              if (si.compareTo(fileNames.get(j)) < 0)
              {
                String temp = fileNames.get(j);
                fileNames.set(j, si);
                fileNames.set(i, temp);
              }
            }
          }
          //
          // We now have a DESCENDING sorted file name list (Most recent entry at 0).
          //
        }
      }
      if (!fileNames.isEmpty())
      {
        result = fileNames.get(0); // Return the LATEST entry.
        if (validLogFile(result, LogConsts.LOG_FILE_EXTENSION))
        {
          result = saveLogsFilePath + result;
        }
        else
        {
          result = null;
        }
      }
    }
    return result;
  }


  /*--------------------------------------------------------------------------*/
  //            1         2
  //  0123456789012345678901234567890
  // "MsgLogs~yyyy-mm-dd-hhmmss.txt"
  /*--------------------------------------------------------------------------*/
  private boolean validLogFile(String fileName, String fileExtension)
  {
    boolean result = true;
    while (true)
    {
      if (fileName.length() < 29) { result = false; break; }
      if (!fileName.startsWith(saveLogsFileName)) { result = false; break; }
      if (!fileName.endsWith(fileExtension)) { result = false; break; }
      if (!fileName.startsWith("~",  7)) { result = false; break; }
      //
      // Check for '~' separator after LogTypeName
      //
      int firstHyphenIndex = fileName.indexOf('-');
      if (!fileName.startsWith("-", firstHyphenIndex)) { result = false; break; }
      if (!fileName.startsWith("-", firstHyphenIndex+3)) { result = false; break; }
      if (!fileName.startsWith("-", firstHyphenIndex+6)) { result = false; break; }
      break;
    }
    return result;
  }


  /*--------------------------------------------------------------------------*/
  private void setNewCurrentLogFilePathName()
  {
   SkDateTime datetime = new SkDateTime("~yyyy-MM-dd-HHmmss");
   currentLogFilePathName = saveLogsFilePathName + datetime.getCurrentDateTimeAsString() +
                LogConsts.LOG_FILE_EXTENSION;
  }

  /*--------------------------------------------------------------------------*/
  private void updateCurrentLogFilePathName()
  {
    File currentFile = new File(currentLogFilePathName);
    // Get the number of bytes in the file
    long length = currentFile.length();
    if (length > saveFileMaxSize)
    {
      setNewCurrentLogFilePathName();
    }
  }

  /*--------------------------------------------------------------------------*/
  void autoWriteLogs()
  {
    synchronized (mzRunning)
    {
      while (true && mzRunning)
      {
        writeAllLogs();
        int latest = saveLogsDataModel.getLatestEntryNumber();
        if (latest == nextLogEntryNumberToSave)
        {
          break;
        }
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  void writeAllLogs()
  {
   try
   {
    saveLogsTimerCounter--;
    if (!directorySizeExceeded)
    {
      if (saveLogsTimerCounter <= 0)
      {
        saveLogsTimerCounter = LogConsts.AUTO_CLEANUP_LOGS_INTERVAL;
        if (!saveLogsIOException)
        {
          handleOldestLogFiles();
        }
      }
    }
    else
    {
      if (!saveLogsIOException)
      {
        handleOldestLogFiles();
      }
    }
    //
    // saveLogsView.getLatestEntryNumber() starts at zero and shows where the
    // next log entry will be written (does NOT wrap to zero).
    //
    int latest = saveLogsDataModel.getLatestEntryNumber();
    if (latest != nextLogEntryNumberToSave)
    {
      writeLogs(saveLogsDataModel, true);
    }
   }
   catch (Exception e)
   {
    if (logger != null)
    {
      logger.logException(e, "autoWriteLogs");
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException ie) {}
    }
   }
  }

  /*--------------------------------------------------------------------------*/
  private class SaveLogsTimeout extends RestartableTimerTask
  {
    /*------------------------------------------------------------------------*/
    /**
     * run ???
     */
    public void run()
    {
      autoWriteLogs();
    }
  }
}