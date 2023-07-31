package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.archive.Archive;
import com.daifukuamerica.wrxj.archive.ArchiveException;
import com.daifukuamerica.wrxj.archive.tranhist.TransactionHistoryArchiver;
import com.daifukuamerica.wrxj.dataserver.standard.StandardConfigurationServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Description:<BR>
 *  This class is used to Archive various data in the Wrx-J system such as
 *  Transaction History, Reports etc.
 *
 * @author       A.D.    26-Aug-05
 * @version      1.0
 */
public class ArchiveTask extends TimedEventTask
{
  private StandardConfigurationServer mpConfigServ = null;
  private Archive[] mpArchivers;
  private boolean mzArchiverLoaded = false;
  private int     mnMaintainedFileCount;
  private String  msArchiveFilePath = "";

  public ArchiveTask(String isName)
  {
    super(isName);
    mpConfigServ = Factory.create(StandardConfigurationServer.class);
  }

  @Override
  public void run()
  {
    ensureDBConnection();
    if (!mzArchiverLoaded)
    {
      loadArchivers();
      mzArchiverLoaded = true;
    }
    
    if (mpArchivers == null)
    {
      mpLogger.logError("No archiver is configured.");
      return;
    }
    
    for(Archive vpArchiver : mpArchivers)
    {
      TransactionToken ttok = null;
      try
      {
        ttok = mpDBObject.startTransaction();
        Date vpBeginDate = vpArchiver.getOldestRecordedDate();
        Date vpEndDate = calculateArchivingEndDate(vpArchiver.getLastRecordedDate());
        
        vpArchiver.setBeginningDate(vpBeginDate);
        vpArchiver.setEndingDate(vpEndDate);
        vpArchiver.setArchivedFileCount(mnMaintainedFileCount);
        if (!msArchiveFilePath.isEmpty())
          vpArchiver.setArchiveOutputDirectory(msArchiveFilePath);
        vpArchiver.exportData();
        mpDBObject.commitTransaction(ttok);
        mpLogger.logOperation("****** Archiving data between " + vpBeginDate.toString() + 
                              " and " + vpEndDate.toString());
      }
      catch(ArchiveException e)
      {
        mpLogger.logError(e.getMessage());
      }
      catch(DBException e)
      {
        mpLogger.logError("Database error during archiving process..." + e.getMessage());
      }
      finally
      {
        mpDBObject.endTransaction(ttok);
      }
      if (mzInterrupted) break;
    }
    
    if (mzInterrupted)
    {
      try { mpDBObject.disconnect(false); }
      catch(DBException exc) {}
    }
  }

  private void loadArchivers()
  {
    Map<String, String> vpMap = mpConfigServ.getCachedSysNameValuePairs("Archiver.class");
    try
    {
      if (vpMap.isEmpty()) return;
      mpArchivers = new Archive[vpMap.size()];
      int idx = 0;
      
      for(Iterator<String> vpIter = vpMap.keySet().iterator(); vpIter.hasNext(); )
      {
                                       // The name-value pair.
        String vsParamName = vpIter.next();
        String vsClassName = vpMap.get(vsParamName);
        
        if (vsClassName.trim().length() == 0)
        {
          throw new DBException("Invalid archiving class for " + vsParamName);
        }

        try
        {
          Class vpClassMetaData = Class.forName(vsClassName);
          mpArchivers[idx++] = (Archive)vpClassMetaData.getDeclaredConstructor().newInstance();
        }
        catch(ClassNotFoundException e)
        {
          mpLogger.logException("Archiver class " + vsClassName + " not found...", e);
        }
        catch(InstantiationException e)
        {
          mpLogger.logException("Failed to instantiate object " + vsClassName, e);
        }
        catch(IllegalAccessException e)
        {
          mpLogger.logException("Failed to build " + vsClassName, e);
        }
        catch(Throwable e)
        {
          mpLogger.logException("Failed to build " + vsClassName, e);
        }
      }
    }
    catch (DBException e)
    {
      mpLogger.logException(e);
    }
  }

  @Override
  public String initTask()
  {
    String vsErrorStr = null;
    
    try
    {
      if (mpConfigServ.isSplitSystem() && mpConfigServ.isThisPrimaryJVM() == false)
      {
        mpLogger.logOperation("INVALID JVM (" + 
                Application.getString(SKDCConstants.JVM_IDENTIFIER_KEY) +
                ") - a Secondary JVM - ArchiveTask will not be started.");
        return " ";
      }
    }
    catch (DBException e)
    {
      return e.getMessage();
    }
    
    int vnDays = getConfigValue(INTERVAL);
    if (vnDays < 1)
    {
      vsErrorStr = "INVALID Archive interval - " + vnDays + " ArchiveTask will not be started.";
    }
    else
    {
      msIntervalString = vnDays + " days ";
      mnInterval = vnDays*ONE_DAY;
      
      String vsExecTime = Application.getString(msName + "." + "ExecutionTime");
      if (vsExecTime == null)
      {
        vsErrorStr = "Execution time not found for ArchiveTask";
      }
      else
      {
        mpInitialStartDate = calculateFirstExecDate(vsExecTime, vnDays);
        if (mpInitialStartDate == null)
          vsErrorStr = "Invalid Time specified for ArchiveTask.";
        else
          mzFixedDateTime = true;
      }
      mnMaintainedFileCount = Application.getInt(msName + "." + "ArchiveFileCount",
                                                 TransactionHistoryArchiver.DEFAULT_TRANSACTION_FILE_COUNT);
                                       // If archive directory is not specified 
                                       // in ControllerConfig the baselogpath
                                       // out of the wrxj.properties file is used.
      msArchiveFilePath = Application.getString("BaseArchiveDir", "");
    }
    
    return(vsErrorStr);
  }
  
  /**
   * Method to calculate start date for task execution.  The start date will be
   * the number of days incremented from current date by the Interval parameter.
   * The start time will be the value specified in the "ExecutionTime" parameter.
   * 
   * @param isTime the time when task should be executed.
   * @param inInterval the number of days from current date when execution will
   *        start.
   */
  private Date calculateFirstExecDate(String isTime, int inInterval)
  {
    String[] vasTime = isTime.split(":");
    if (vasTime == null || vasTime.length == 0)
      return(null);
    Calendar vpCurrCalDate = Calendar.getInstance();
    Calendar vpExecCalDate = (Calendar)vpCurrCalDate.clone();

    vpExecCalDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(vasTime[0]));
    vpExecCalDate.set(Calendar.MINUTE, Integer.parseInt(vasTime[1]));
    if (vasTime.length > 2)
      vpExecCalDate.set(Calendar.SECOND, Integer.parseInt(vasTime[2]));

/*============================================================================
 * If the execution time is scheduled after current time but within today,
 * return this as the first exec. date/time.  Otherwise the first exec. date
 * will be current date/time + interval days, at configured time.
 *============================================================================*/
    if (vpExecCalDate.before(vpCurrCalDate))
    {                                  // Exec. time is outside of today's scope.
      vpExecCalDate.add(Calendar.DAY_OF_MONTH, inInterval);
    }
    
    return(vpExecCalDate.getTime());
  }
  
  private Date calculateArchivingEndDate(Date ipLastRecordDate)
  {
    int inDaysToPreserve = getConfigValue("DaysToKeep");
    Calendar vpCal = Calendar.getInstance();
    vpCal.setTime(ipLastRecordDate);
    vpCal.add(Calendar.DAY_OF_MONTH, -inDaysToPreserve);
    
    return(vpCal.getTime());
  }
}

