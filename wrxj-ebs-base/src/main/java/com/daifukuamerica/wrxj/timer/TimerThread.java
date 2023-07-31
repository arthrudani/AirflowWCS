package com.daifukuamerica.wrxj.timer;

import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.log.Logger;

/*----------------------------------------------------------------------------*/
/**
 * This "helper class" implements the timer's task execution thread, which
 * waits for tasks on the timer queue, executions them when they fire,
 * reschedules repeating tasks, and removes cancelled tasks and spent
 * non-repeating tasks from the queue.
 */
/*----------------------------------------------------------------------------*/
class TimerThread extends NamedThread
{
  /**
   * This flag is set to false by the reaper to inform us that there
   * are no more live references to our Timer object.  Once this flag
   * is true and there are no more tasks in our queue, there is no
   * work left for us to do, so we terminate gracefully.  Note that
   * this field is protected by queue's monitor!
   */
  boolean newTasksMayBeScheduled = true;

  /**
   * Our Timer's queue.  We store this reference in preference to
   * a reference to the Timer so the reference graph remains acyclic.
   * Otherwise, the Timer would never be garbage-collected and this
   * thread would never go away.
   */
  private TaskQueue queue;

  Logger logger = null;

  TimerThread(TaskQueue ipQueue)
  {
    queue = ipQueue;
  }

  /*--------------------------------------------------------------------------*/
  @Override
  public void run()
  {
    try
    {
      mainLoop();
    }
    finally
    {
      // Somone killed this Thread, behave as if Timer cancelled
      synchronized(queue)
      {
        newTasksMayBeScheduled = false;
        queue.clear();  // Eliminate obsolete references
        queue.logger = null;
        logger = null;
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * The main timer loop.  (See class comment.)
   */
  /*--------------------------------------------------------------------------*/
  private void mainLoop()
  {
    while (true)
    {
      try
      {
        RestartableTimerTask task;
        boolean taskFired;
        synchronized(queue)
        {
          // Wait for queue to become non-empty
          while ((queue.isEmpty()) && (newTasksMayBeScheduled))
          {
           queue.wait();
          }
          if (queue.isEmpty() && (!newTasksMayBeScheduled))
          {
            break;
          }
          // Queue nonempty; look at "Next" event and do the right thing
          long currentTime, executionTime;
          task = queue.getNext();
          synchronized(task.lock)
          {
            if (task.state == RestartableTimerTask.CANCELLED)
            {
              queue.removeCurrent();
              continue;  // No action required, poll queue again
            }
            currentTime = System.currentTimeMillis();
            executionTime = task.nextExecutionTime;
            taskFired = (executionTime <= currentTime);
            if (taskFired)
            {
              if (task.period == 0)
              {
                // Non-repeating, remove
                queue.removeCurrent();
                task.state = RestartableTimerTask.EXECUTED;
              }
              else
              {
                // Repeating task, reschedule
                queue.rescheduleCurrent(
                        task.period<0 ? currentTime   - task.period
                                      : executionTime + task.period);
              }
            }
          }
          if (!taskFired)
          {
            // Task hasn't yet fired; wait
            queue.wait(executionTime - currentTime);
          }
        }
        if (taskFired)
        {
          //
          // Task fired; run it, holding no locks
          //
          task.run();
        }
      }
      catch(InterruptedException e)
      {
      }
    }
  }
}

