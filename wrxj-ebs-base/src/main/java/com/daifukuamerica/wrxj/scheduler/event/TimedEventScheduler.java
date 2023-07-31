package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.clc.ControllerDefinition;
import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import javax.swing.SwingUtilities;

/**
 *  Controller Class for miscellaneous timer based activities such as
 *  Item Master Auto Deletion, Completed Expected Receipt cleanup etc.  Each
 *  task is purposely scheduled with a separate timer so that it gets its own
 *  thread, and does not interfere with the schedule of any other task.
 *
 * @author       A.T.    23-Mar-05    Original version
 * @author       A.D.    24-Aug-05    Added short order check task and archiving
 *                                    task. What used to be defined as inner classes
 *                                    are now defined as normal outer classes that
 *                                    can be extended on a project basis if
 *                                    necessary.
 * @author karmstrong   27-Apr-06     Modified for easier extensibility - No changes need to be made here
 *                                    to add new tasks - just add a new entry to the configuration
 * @version      2.0
 */
public class TimedEventScheduler extends Controller
{
  private String msName;
  private List<TimedEventTask> mpTaskList;
  private List<Timer> mpTimerList;
  
  private DBObject mpDBObject = null;

 /**
  *  Default constructor for this scheduler.
  */
  private TimedEventScheduler(String isName)
  {
    msName = isName;
    mpTaskList = new ArrayList<TimedEventTask>();
    mpTimerList = new ArrayList<Timer>();
  }

  /**
   *  Method simply ensures database connectivity.  This is useful
   */
   protected void ensureDBConnection()
   {
	 if (mpDBObject == null || !mpDBObject.isConnectionActive())
	 {
       mpDBObject = new DBObjectTL().getDBObject();
       try { mpDBObject.connect(); }
       catch(DBException e) { return; }
     }
   }

  /**
   *  Sets the the status of this controller to RUNNING.
   */
  @Override
  public void startup()
  {
    super.startup();
    logger.logDebug("TimedEventScheduler.startup() - Start");
                                       // Mark this controller as running.
    super.setControllerStatus(ControllerConsts.STATUS_RUNNING);

    // Here is where the task list is created  
    List<TimedEventTask> vpTasks = getTasksToSchedule();
    
    for (TimedEventTask vpTask : vpTasks)
    {
      String vsErrorMsg = vpTask.initTask();
      if (vsErrorMsg == null)
      {
        scheduleTask(vpTask);
      }
      else
      {
        if (vsErrorMsg.trim().length() > 0)
        {
          logger.logError(vsErrorMsg);
        }
      }
    }
    
    logger.logDebug("TimedEventScheduler.startup() - End");
  }

  @Override
  public void shutdown()
  {
    logger.logDebug("TimedEventScheduler.shutdown() -- Start");
    
    // Cancel all the tasks
    for (TimedEventTask vpTask : mpTaskList)
    {
      if (vpTask != null) vpTask.cancel();
      vpTask = null;
    }
    
  /*-------------------------------------------------------------------------
  Now cancel all the timers for this scheduler.
  -------------------------------------------------------------------------*/  
    for (Timer vpTimer : mpTimerList)
    {
      if (vpTimer != null)
      {
        vpTimer.cancel();
        vpTimer = null;
      }
    }
    boolean threadCheckingOn = true;
    if(mpDBObject.checkConnected())
    {
      try
      {
        mpDBObject.disconnect(threadCheckingOn);
        if ((!threadCheckingOn) ||
          (!SwingUtilities.isEventDispatchThread()))
        {
          mpDBObject = null;
        }
      }
      catch (DBException e)
      {
        logger.logException(e, "Error closing Database Connection");
      }
    }

    logger.logDebug("TimedEventScheduler.shutdown() -- End");
    super.shutdown();
  }
  
  /**
   * Factory for ControllerImplFactory.
   *
   * <p><b>Details:</b> <code>create</code> is a factory method used exclusively
   * by <code>ControllerImplFactory</code>.  Configurable properties of a new
   * controller created using this method are initialized using data in the
   * supplied properties object.  If the controller cannot be created, a
   * <code>ControllerCreationException</code> is thrown.</p>
   *
   * <p>This factory initializes the device port and collaborator.</p>
   *
   * @param ipConfig configurable property definitions
   * @return the created controller
   * @throws ControllerCreationException if an error occurred while creating the controller
   */
  public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException
  {
    Controller vpController = new TimedEventScheduler(ipConfig.getString(ControllerDefinition.CONTROLLER_NAME));
    return vpController;
  }
  
  /**
   * Schedules a task for execution on a regular interval specified by
   * the task object.
   * @param ipTask
   */
  private void scheduleTask(TimedEventTask ipTask)
  {
    long vnInterval = ipTask.getInterval();
    long vnInitialInterval = ipTask.getInitialInterval();
    String vsIntervalString = ipTask.getIntervalString();
    String vsName = ipTask.getName();
    
    if (vnInterval > 0)
    {
      mpTaskList.add(ipTask);
          // Schedule this task on a regular interval
          // from current date.
      Timer vpTimer = new Timer(vsName + "Timer");
      mpTimerList.add(vpTimer);
      
      if (ipTask.isFixedInterval())
      {
        vpTimer.scheduleAtFixedRate(ipTask, vnInitialInterval, vnInterval);
        logger.logDebug("Running " + vsName + " Timer Task. Interval = " + ipTask.getIntervalString());
      }
      else if (ipTask.isFixedDateTime())
      {
        vpTimer.scheduleAtFixedRate(ipTask, ipTask.getIntialStartDate(), vnInterval);
        logger.logOperation("Running " + vsName + " Timer Task. Interval = " +
                            ipTask.getIntervalString() + ". First exec. date-time " +
                            ipTask.getIntialStartDate().toString());
      }
      else
      {
        vpTimer.schedule(ipTask, vnInitialInterval, vnInterval);
        logger.logDebug("Running " + vsName + " Timer Task. Interval = " + ipTask.getIntervalString());
      }
    }
    else
    {
      logger.logDebug("INVALID Interval \"" + vsIntervalString +
      "\" - startup(). " + vsName + " TASK NOT LOADED.");
    }
  }
  
  private List<TimedEventTask> getTasksToSchedule()
  {
    ensureDBConnection();
    
    
    List<TimedEventTask> vpList = new ArrayList<TimedEventTask>();
      
    for(String vsTaskName : getTaskNames())
    {
      String vsClassName = "";
      try
      {
        vsClassName = getConfigProperty("Task." + vsTaskName + ".class");
        if (!vsClassName.startsWith("com"))
        {
          vsClassName = TimedEventTask.class.getPackage().getName() + '.' + vsClassName;
        }
        Class<? extends TimedEventTask> vpClass = Class.forName(vsClassName).asSubclass(TimedEventTask.class);
        Constructor<? extends TimedEventTask> vpCon = vpClass.getConstructor(String.class);
        String vsFullName = "ControllerConfig." + msName + ".Task." + vsTaskName;
        TimedEventTask vpTask = vpCon.newInstance(vsFullName);
        vpList.add(vpTask);
      }
      catch(InstantiationException ex)
      {
        logger.logException("========= " + vsClassName + " creation failure! =========", ex);
      }
      catch(IllegalAccessException ex)
      {
        logger.logException("========= " + vsClassName + " creation failure! =========", ex);
      }
      catch(IllegalArgumentException ex)
      {
        logger.logException("========= " + vsClassName + " creation failure! =========", ex);
      }
      catch(InvocationTargetException ex)
      {
        logger.logException("========= " + vsClassName + " creation failure! =========", ex);
      }
      catch(NoSuchMethodException ex)
      {
        logger.logException("========= " + vsClassName + " creation failure! =========", ex);
      }
      catch(SecurityException ex)
      {
        logger.logException("========= " + vsClassName + " creation failure! =========", ex);
      }
      catch(ClassNotFoundException ex)
      {
        logger.logException("========= " + vsClassName + " creation failure! =========", ex);
      }
    }
    
    return vpList;
  }
  
  /**
   * Returns task names.
   * 
   * <p><b>Details:</b> This method determines the names of all tasks that are 
   * defined as subproperties for this {@link TimedEventScheduler} and returns
   * them in a {@link Set}.</p>
   * 
   * <p>In other words, if a property of this controller has the form 
   * "<code>Task.<var>Name</var>.</code>*", <var>Name</var> will be included in 
   * the returned set.</p>
   * 
   * <p>Task details can be retrieved by querying specific task properties, 
   * as demonstrated below:</p>
   * 
   * <blockquote><pre>
      *String vsClass = {@link #getConfigProperty(String) getConfigProperty}("Task." + vsTaskName + ".class");
      *Class vtClass = Class.forName(vsClass);
   * </pre></blockquote>
   * 
   * <p>All tasks defined as properties of this {@link TimedEventScheduler} are 
   * expected to include, at the very least, a ".class" subproperty.  Other
   * subproperties may also be expected, depending on the task type.</p>
   * 
   * @return the task names
   */
  private Set<String> getTaskNames()
  {
    String vsPrefix = "Task.";
    int vnPrefixLength = vsPrefix.length();
    Set<String> vpRawNames = mpProperties.getNames(vsPrefix);
    Set<String> vpRefinedNames = new HashSet<String>();
    for (String vsRawName: vpRawNames)
    {
      int vnDot2 = vsRawName.indexOf('.', vnPrefixLength);
      if (vnDot2 < 0)
        continue;
      String vsRefinedName = vsRawName.substring(vnPrefixLength, vnDot2);
      vpRefinedNames.add(vsRefinedName);
    }
    return vpRefinedNames;
  }
  
}


