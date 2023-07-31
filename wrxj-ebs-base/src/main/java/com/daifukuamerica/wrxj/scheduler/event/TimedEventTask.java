package com.daifukuamerica.wrxj.scheduler.event;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Date;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

/**
 * Description:<BR>
 *  
 *
 * @author       A.D.    26-Aug-05
 * @version      1.0
 */
public abstract class TimedEventTask extends TimerTask
{
  protected static final String INTERVAL = "interval";
  protected final long  ONE_DAY = javax.management.timer.Timer.ONE_DAY;
  protected boolean   mzInterrupted;
  protected DBObject  mpDBObject;
  protected Logger    mpLogger = Logger.getLogger();
  protected long    mnInterval, mnInitialInterval;
  protected Date    mpInitialStartDate;
  protected String  msName, msIntervalString;
  protected boolean mzFixed = false;
  protected boolean mzFixedDateTime = false;

  protected TimedEventTask(String isName)
  {
    msName = isName;
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
      catch(DBException e)
      {
        Logger.getLogger().logException(e);
      }
    }
  }  
  
  /**
   * Method to close database connection
   */
  protected void removeDBConnection()
  {
    boolean threadCheckingOn = true;
    if(mpDBObject != null)
    {
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
          mpLogger.logException(e, "Error closing Database Connection");
        }
      }
    }
  }
  
  /**
   * Cancel this task
   */
  @Override
  public boolean cancel()
  {
    boolean rtnval = super.cancel();
    mzInterrupted = true;
    removeDBConnection();
    return(rtnval);
  }
  
  /**
   * 
   * @return The interval (in ms) at which the task should be run
   */
  public long getInterval()
  {
    return mnInterval;
  }
  
  /**
   * 
   * @return The initial time (in ms) to wait before the first execution
   * of the task.
   */
  public long getInitialInterval()
  {
    return mnInitialInterval;
  }
  
  /**
   * 
   * @return A <code>String</code> representation of the interval for debugging
   * purposes.
   */
  public String getIntervalString()
  {
    return msIntervalString;
  }
  
  public Date getIntialStartDate()
  {
    return mpInitialStartDate;
  }
  
  /**
   * 
   * @return The <code>String</code> representation of this task for debugging
   * purposes.
   */
  public String getName()
  {
    return msName;
  }
  
  /**
   * 
   * @return true if the task should be run on a fixed interval
   */
  public boolean isFixedInterval()
  {
    return mzFixed;
  }
  
  /**
   * Tells us if it's a Fixed date time tasker.  Meaning the task is scheduled
   * initially at a fixed date/time.  Subsequent execution is determined by the
   * Interval property.
   * 
   * @return <code>true</code> if the task is run initially with a fixed date/time.
   */
  public boolean isFixedDateTime()
  {
    return(mzFixedDateTime);
  }
  
  /**
   * Get an integer property for this task
   * 
   * @param isName
   * @return
   */
  protected int getConfigValue(String isName)
  {
    return Application.getInt(msName + "." + isName);
  }

  /**
   * Get a String property for this task
   * 
   * @param isName
   * @return
   */
  protected String getConfigString(String isName)
  {
    return Application.getString(msName + "." + isName);
  }
  
  /**
   * Get the error string for a bad/missing property.
   * 
   * @param isProperty
   * @param isValue
   * @return
   */
  protected String getPropertyError(String isProperty, String isValue)
  {
    return "INVALID " + getClass().getSimpleName() + " property " + isProperty
        + " (" + isValue + "). " + getClass().getSimpleName()
        + " will not be started.";
  }

  /**
   * Initialize all the variables for this task (interval, name, etc.)
   * @return <code>String</code> error message if initialization fails, 
   * <code>null</code> if it succeeds.
   */
  public abstract String initTask();
  
  /**
   * The task's behavior should be implemented in this method.
   */
  @Override
  public abstract void run();
}