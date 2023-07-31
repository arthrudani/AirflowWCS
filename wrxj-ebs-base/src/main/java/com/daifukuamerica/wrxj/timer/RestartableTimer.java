package com.daifukuamerica.wrxj.timer;

import com.daifukuamerica.wrxj.log.Logger;

/**
 * A facility for threads to schedule tasks for future execution in a
 * background thread.  Tasks may be scheduled for one-time execution, or for
 * repeated execution at regular intervals.
 *
 * <p>Corresponding to each <tt>Timer</tt> object is a single background
 * thread that is used to execute all of the timer's tasks, sequentially.
 * Timer tasks should complete quickly.  If a timer task takes excessive time
 * to complete, it "hogs" the timer's task execution thread.  This can, in
 * turn, delay the execution of subsequent tasks, which may "bunch up" and
 * execute in rapid succession when (and if) the offending task finally
 * completes.
 *
 * <p>After the last live reference to a <tt>Timer</tt> object goes away
 * <i>and</i> all outstanding tasks have completed execution, the timer's task
 * execution thread terminates gracefully (and becomes subject to garbage
 * collection).  However, this can take arbitrarily long to occur.  By
 * default, the task execution thread does not run as a <i>daemon thread</i>,
 * so it is capable of keeping an application from terminating.  If a caller
 * wants to terminate a timer's task execution thread rapidly, the caller
 * should invoke the the timer's <tt>cancel</tt> method.
 *
 * <p>If the timer's task execution thread terminates unexpectedly, for
 * example, because its <tt>stop</tt> method is invoked, any further
 * attempt to schedule a task on the timer will result in an
 * <tt>IllegalStateException</tt>, as if the timer's <tt>cancel</tt>
 * method had been invoked.
 *
 * <p>This class is thread-safe: multiple threads can share a single
 * <tt>Timer</tt> object without the need for external synchronization.
 *
 * <p>This class does <i>not</i> offer real-time guarantees: it schedules
 * tasks using the <tt>Object.wait(long)</tt> method.
 *
 * @author Stephen Kendorski
 * @version 1.0
 *
 * @see     RestartableTimerTask
 * @see     Object#wait(long)
 * @since   1.3
 */

/*----------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------*/
public class RestartableTimer
{
  /**
   * The timer task queue.  This data structure is shared with the timer
   * thread.  The timer produces tasks, via its various schedule calls,
   * and the timer thread consumes, executing timer tasks as appropriate,
   * and removing them from the queue when they're obsolete.
   */
  private TaskQueue queue = new TaskQueue();

  /**
   * The timer thread.
   */
  private TimerThread thread = new TimerThread(queue);

  private Logger logger = null;

//  /*--------------------------------------------------------------------------*/
//  /**
//   * Creates a new timer.  The associated thread does <i>not</i> run as
//   * a daemon.
//   *
//   * @see Thread
//   * @see #cancel()
//   */
//  /*--------------------------------------------------------------------------*/
//  public RestartableTimer()
//  {
//    this("");
//  }

  /*--------------------------------------------------------------------------*/
  /**
   * Creates a new timer.  The associated thread does <i>not</i> run as
   * a daemon.
   *
   * @param timerName ?
   * @see Thread
   * @see #cancel()
   */
  /*--------------------------------------------------------------------------*/
  public RestartableTimer(String timerName)
  {
    if (timerName.length() == 0)
    {
//      logger = SkdContext.getLogger(this.getClass().getName());
    }
    else
    {
//      logger = SkdContext.getLogger(timerName);
/*      if (timerName.indexOf(':') != -1)
      {
        timerName = timerName.substring(0, timerName.indexOf(':'));
      }
      timerName = timerName + "- RestartableTimer";
*/      thread.setName(timerName);
    }
    queue.logger = logger;
    thread.logger = logger;
    thread.start();
  }

//  /*--------------------------------------------------------------------------*/
//  /**
//   * Creates a new timer whose associated thread may be specified to
//   * run as a daemon.  A deamon thread is called for if the timer will
//   * be used to schedule repeating "maintenance activities", which must
//   * be performed as long as the application is running, but should not
//   * prolong the lifetime of the application.
//   *
//   * @param isDaemon true if the associated thread should run as a daemon.
//   *
//   * @see Thread
//   * @see #cancel()
//   */
//  /*--------------------------------------------------------------------------*/
//  public RestartableTimer(boolean isDaemon)
//  {
//      thread.setDaemon(isDaemon);
//      thread.start();
//  }

  /*--------------------------------------------------------------------------*/
  /** Set a Single-Shot Timer Event. Schedules the specified task for execution
   * <i>once</i> after the specified delay.
   *
   * @param task   task to be scheduled.
   * @param delay  delay in milliseconds before task is to be executed.
   * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
  public void setSSTimerEvent(RestartableTimerTask task, int delay)
  {
    task.setIntTag(0);
    schedule(task, delay);
  }

  /*--------------------------------------------------------------------------*/
  /** Set Auto-Repeating Timer Event.  Schedules the specified task for
   * <i>repeated fixed-interval execution</i>, beginning after the specified
   * interval.  Subsequent executions take place at approximately regular
   * intervals separated by the specified period.
   *
   * <p>In fixed-interval execution, each execution is scheduled relative to
   * the actual execution time of the previous execution.  If an execution
   * is delayed for any reason (such as garbage collection or other
   * background activity), subsequent executions will be delayed as well.
   * In the long run, the frequency of execution will generally be slightly
   * lower than the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-interval execution is appropriate for recurring activities
   * that require "smoothness."  In other words, it is appropriate for
   * activities where it is more important to keep the frequency accurate
   * in the short run than in the long run.  This includes most animation
   * tasks, such as blinking a cursor at regular intervals.  It also includes
   * tasks wherein regular activity is performed in response to human
   * input, such as automatically repeating a character as long as a key
   * is held down.
   *
   * @param task     task to be scheduled.
   * @param interval time in milliseconds between successive task executions.
   * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
  public void setPeriodicTimerEvent(RestartableTimerTask task, int interval)
  {
    task.setIntTag(0);
    schedule(task, interval, interval);
  }

  /*--------------------------------------------------------------------------*/
  /** Set Auto-Repeating Timer Event.  Schedules the specified task for
   * <i>repeated fixed-interval execution</i>, beginning after the specified delay.
   * Subsequent executions take place at approximately regular intervals
   * separated by the specified period.
   *
   * <p>In fixed-intervalay execution, each execution is scheduled relative to
   * the actual execution time of the previous execution.  If an execution
   * is delayed for any reason (such as garbage collection or other
   * background activity), subsequent executions will be delayed as well.
   * In the long run, the frequency of execution will generally be slightly
   * lower than the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-interval execution is appropriate for recurring activities
   * that require "smoothness."  In other words, it is appropriate for
   * activities where it is more important to keep the frequency accurate
   * in the short run than in the long run.  This includes most animation
   * tasks, such as blinking a cursor at regular intervals.  It also includes
   * tasks wherein regular activity is performed in response to human
   * input, such as automatically repeating a character as long as a key
   * is held down.
   *
   * @param task     task to be scheduled.
   * @param interval time in milliseconds between successive task executions.
   * @param delay    delay in milliseconds before task is to be executed.
   * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
  public void setPeriodicTimerEvent(RestartableTimerTask task,
                                    int interval, int delay)
  {
    task.setIntTag(0);
    schedule(task, delay, interval);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Schedules the specified task for execution after the specified delay.
   *
   * @param task  task to be scheduled.
   * @param delay delay in milliseconds before task is to be executed.
   * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
   *         <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled.
   */
  /*--------------------------------------------------------------------------*/
  private void schedule(RestartableTimerTask task, long delay) {
      if (delay < 0)
          throw new IllegalArgumentException("Negative delay.");
      sched(task, System.currentTimeMillis()+delay, 0);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Schedules the specified task for execution at the specified time.  If
   * the time is in the past, the task is scheduled for immediate execution.
   *
   * @param task task to be scheduled.
   * @param time time at which task is to be executed.
   * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
//  private void schedule(RestartableTimerTask task, Date time) {
//      sched(task, time.getTime(), 0);
//  }

  /*--------------------------------------------------------------------------*/
  /**
   * Schedules the specified task for repeated <i>fixed-delay execution</i>,
   * beginning after the specified delay.  Subsequent executions take place
   * at approximately regular intervals separated by the specified period.
   *
   * <p>In fixed-delay execution, each execution is scheduled relative to
   * the actual execution time of the previous execution.  If an execution
   * is delayed for any reason (such as garbage collection or other
   * background activity), subsequent executions will be delayed as well.
   * In the long run, the frequency of execution will generally be slightly
   * lower than the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-delay execution is appropriate for recurring activities
   * that require "smoothness."  In other words, it is appropriate for
   * activities where it is more important to keep the frequency accurate
   * in the short run than in the long run.  This includes most animation
   * tasks, such as blinking a cursor at regular intervals.  It also includes
   * tasks wherein regular activity is performed in response to human
   * input, such as automatically repeating a character as long as a key
   * is held down.
   *
   * @param task   task to be scheduled.
   * @param delay  delay in milliseconds before task is to be executed.
   * @param period time in milliseconds between successive task executions.
   * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
   *         <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread
   *         terminated.
   */
  /*--------------------------------------------------------------------------*/
  private void schedule(RestartableTimerTask task, long delay, long period) {
      if (delay < 0)
          throw new IllegalArgumentException("Negative delay.");
      if (period <= 0)
          throw new IllegalArgumentException("Non-positive period.");
      sched(task, System.currentTimeMillis()+delay, -period);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Schedules the specified task for repeated <i>fixed-delay execution</i>,
   * beginning at the specified time. Subsequent executions take place at
   * approximately regular intervals, separated by the specified period.
   *
   * <p>In fixed-delay execution, each execution is scheduled relative to
   * the actual execution time of the previous execution.  If an execution
   * is delayed for any reason (such as garbage collection or other
   * background activity), subsequent executions will be delayed as well.
   * In the long run, the frequency of execution will generally be slightly
   * lower than the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-delay execution is appropriate for recurring activities
   * that require "smoothness."  In other words, it is appropriate for
   * activities where it is more important to keep the frequency accurate
   * in the short run than in the long run.  This includes most animation
   * tasks, such as blinking a cursor at regular intervals.  It also includes
   * tasks wherein regular activity is performed in response to human
   * input, such as automatically repeating a character as long as a key
   * is held down.
   *
   * @param task   task to be scheduled.
   * @param firstTime First time at which task is to be executed.
   * @param period time in milliseconds between successive task executions.
   * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
//  private void schedule(RestartableTimerTask task, Date firstTime, long period) {
//      if (period <= 0)
//          throw new IllegalArgumentException("Non-positive period.");
//      sched(task, firstTime.getTime(), -period);
//  }

  /*--------------------------------------------------------------------------*/
  /**
   * Schedules the specified task for repeated <i>fixed-rate execution</i>,
   * beginning after the specified delay.  Subsequent executions take place
   * at approximately regular intervals, separated by the specified period.
   *
   * <p>In fixed-rate execution, each execution is scheduled relative to the
   * scheduled execution time of the initial execution.  If an execution is
   * delayed for any reason (such as garbage collection or other background
   * activity), two or more executions will occur in rapid succession to
   * "catch up."  In the long run, the frequency of execution will be
   * exactly the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-rate execution is appropriate for recurring activities that
   * are sensitive to <i>absolute</i> time, such as ringing a chime every
   * hour on the hour, or running scheduled maintenance every day at a
   * particular time.  It is also appropriate for for recurring activities
   * where the total time to perform a fixed number of executions is
   * important, such as a countdown timer that ticks once every second for
   * ten seconds.  Finally, fixed-rate execution is appropriate for
   * scheduling multiple repeating timer tasks that must remain synchronized
   * with respect to one another.
   *
   * @param task   task to be scheduled.
   * @param delay  delay in milliseconds before task is to be executed.
   * @param period time in milliseconds between successive task executions.
   * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
   *         <tt>delay + System.currentTimeMillis()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
//  private void scheduleAtFixedRate(RestartableTimerTask task, long delay, long period) {
//      if (delay < 0)
//          throw new IllegalArgumentException("Negative delay.");
//      if (period <= 0)
//          throw new IllegalArgumentException("Non-positive period.");
//      sched(task, System.currentTimeMillis()+delay, period);
//  }

  /*--------------------------------------------------------------------------*/
  /**
   * Schedules the specified task for repeated <i>fixed-rate execution</i>,
   * beginning at the specified time. Subsequent executions take place at
   * approximately regular intervals, separated by the specified period.
   *
   * <p>In fixed-rate execution, each execution is scheduled relative to the
   * scheduled execution time of the initial execution.  If an execution is
   * delayed for any reason (such as garbage collection or other background
   * activity), two or more executions will occur in rapid succession to
   * "catch up."  In the long run, the frequency of execution will be
   * exactly the reciprocal of the specified period (assuming the system
   * clock underlying <tt>Object.wait(long)</tt> is accurate).
   *
   * <p>Fixed-rate execution is appropriate for recurring activities that
   * are sensitive to <i>absolute</i> time, such as ringing a chime every
   * hour on the hour, or running scheduled maintenance every day at a
   * particular time.  It is also appropriate for for recurring activities
   * where the total time to perform a fixed number of executions is
   * important, such as a countdown timer that ticks once every second for
   * ten seconds.  Finally, fixed-rate execution is appropriate for
   * scheduling multiple repeating timer tasks that must remain synchronized
   * with respect to one another.
   *
   * @param task   task to be scheduled.
   * @param firstTime First time at which task is to be executed.
   * @param period time in milliseconds between successive task executions.
   * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
//  private void scheduleAtFixedRate(RestartableTimerTask task, Date firstTime,
//                                  long period) {
//      if (period <= 0)
//          throw new IllegalArgumentException("Non-positive period.");
//      sched(task, firstTime.getTime(), period);
//  }

  /*--------------------------------------------------------------------------*/
  /**
   * Schedule the specifed timer task for execution at the specified
   * time with the specified period, in milliseconds.  If period is
   * positive, the task is scheduled for repeated execution; if period is
   * zero, the task is scheduled for one-time execution. Time is specified
   * in Date.getTime() format.  This method checks timer state, task state,
   * and initial execution time, but not period.
   *
   * @param task ?
   * @param time ?
   * @param period ?
   * @throws IllegalArgumentException if <tt>time()</tt> is negative.
   * @throws IllegalStateException if timer was cancelled, or timer thread terminated.
   */
  /*--------------------------------------------------------------------------*/
  private void sched(RestartableTimerTask task, long time, long period)
  {
    if (time < 0)
    {
      throw new IllegalArgumentException("Illegal execution time.");
    }

    synchronized(queue)
    {
      if (!thread.newTasksMayBeScheduled)
      {
        throw new IllegalStateException("Timer already cancelled.");
      }

      synchronized(task.lock)
      {
        //
        // The current state of the timer task is not a concern. Rescheduling
        // an active or cancelled timer task is ok.
        //
//        if (task.state != RestartableTimerTask.VIRGIN)
//        {
//          throw new IllegalStateException("Task already scheduled or cancelled");
//        }
        task.nextExecutionTime = time;
        task.period = period;
        task.state = RestartableTimerTask.SCHEDULED;
      }
      queue.add(task);
//      logDebug("sched -- Add task: " + task.iTag);
      if (queue.getNext() == task)
      {
        /**
         * The task we added is the next to execute - let the task queue
         * update its wait time.
         */
        queue.notify();
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Terminates this timer, discarding any currently scheduled tasks.
   * Does not interfere with a currently executing task (if it exists).
   * Once a timer has been terminated, its execution thread terminates
   * gracefully, and no more tasks may be scheduled on it.
   *
   * <p>Note that calling this method from within the run method of a
   * timer task that was invoked by this timer absolutely guarantees that
   * the ongoing task execution is the last task execution that will ever
   * be performed by this timer.
   *
   * <p>This method may be called repeatedly; the second and subsequent
   * calls have no effect.
   */
  /*--------------------------------------------------------------------------*/
  public void cancel()
  {
    synchronized(queue)
    {
      thread.newTasksMayBeScheduled = false;
      queue.clear();
      queue.notify();  // In case queue was already empty.
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Terminates the specified timer task.  Does not interfere with currently
   * executing task.  Auto-repeating tasks will stop auto-repeating.
   *
   * <p>This method may be called repeatedly; the second and subsequent
   * calls have no effect.
   *
   * @param task   task to be cancelled.
   */
  /*--------------------------------------------------------------------------*/
  public void cancel(RestartableTimerTask task)
  {
    synchronized(queue)
    {
      task.cancel();
      queue.notify();
    }
  }
  
  public boolean isScheduled(RestartableTimerTask ipTask)
  {
    return ipTask.isScheduled();
  }
  
}

