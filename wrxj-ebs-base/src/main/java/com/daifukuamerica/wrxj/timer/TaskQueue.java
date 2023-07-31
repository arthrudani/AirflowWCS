package com.daifukuamerica.wrxj.timer;

import com.daifukuamerica.wrxj.log.Logger;

/*----------------------------------------------------------------------------*/
/**
 * This class represents a timer task queue: a priority queue of RestartableTimerTasks,
 * ordered on nextExecutionTime.  Each Timer object has one of these, which it
 * shares with its TimerThread.  Internally this class uses a heap, which
 * offers log(n) performance for the add, removeMin and rescheduleMin
 * operations, and constant time performance for the the getMin operation.
 */
/*----------------------------------------------------------------------------*/
class TaskQueue
{
  /**
   * The priority queue is ordered (DESCENDING) using the task nextExecutionTime
   * field: The RestartableTimerTask with the lowest (next/earliest)
   * nextExecutionTime is in queue[size-1], the last valid task in the array,
   * (assuming the queue is nonempty).  Putting the next task to be executed
   * at the end of the list allows us to remove the task without resorting the
   * list.
   */
  private RestartableTimerTask[] queue = new RestartableTimerTask[128];
  /**
   * The number of tasks in the priority queue.  (The tasks are stored in
   * queue[0] to queue[size-1]).
   */
  int size = 0;

  Logger logger = null;

  /**
   * Adds a new task (if NOT already in list) to the priority queue.
   * 
   * @param task
   */
  void add(RestartableTimerTask task)
  {
    boolean needfix = true;
    if (!taskExists(task))
    {
      //
      // The Task to be added is NOT already in the TaskQueue.
      // Grow task array if necessary.
      //
      if (++size == queue.length)
      {
        RestartableTimerTask[] newQueue = new RestartableTimerTask[2*queue.length];
        System.arraycopy(queue, 0, newQueue, 0, size);
        queue = newQueue;
      }
      needfix = insert(task);
    }
    if (needfix)
    {
      fix();
    }
  }

  /**
   * Finds if a task is already in the priority queue.
   * 
   * @param task ?
   * @return ?
   */
  private boolean taskExists(RestartableTimerTask task)
  {
    boolean exists = false;
    for (int i = 0; i < size; i++)
    {
      if (queue[i] == task)
      {
        exists = true;
        break;
      }
    }
    return exists;
  }

  /**
   * Return the "next task" of the priority queue.  (The next task is the
   * task with the lowest/earliest nextExecutionTime (it's always the LAST
   * element in the list)).
   * 
   * @return ?
   */
  RestartableTimerTask getNext()
  {
    return queue[size-1];
  }

  /**
   * Remove the next/current task from the priority queue.
   */
  void removeCurrent()
  {
    size--;
    queue[size] = null;  // Drop extra reference to prevent memory leak.
  }

  /**
   * Sets the nextExecutionTime associated with the current task to the
   * specified value, and adjusts priority queue accordingly.
   * 
   * @param newTime
   */
  void rescheduleCurrent(long newTime)
  {
    queue[size - 1].nextExecutionTime = newTime;
    fix();
  }

  /**
   * Returns true if the priority queue contains no elements.
   * 
   * @return ?
   */
  boolean isEmpty()
  {
    return (size == 0);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Removes all elements from the priority queue.
   */
  /*--------------------------------------------------------------------------*/
  void clear()
  {
    // Null out task references to prevent memory leak.
    for (int i = size - 1; i >= 0; i--)
    {
      queue[i] = null;
    }
    size = 0;
  }

  /*------------------------------------------------------------------------*/
  /**
   * fix() -- BubbleSort the elements in the array.  There are faster sorting
   * methods, but we're probably not dealing with tens of thousands of timers
   * so we should be ok.  The Next timer task to execute is at the BOTTOM of
   * the list.
   */
  /*------------------------------------------------------------------------*/
  private void fix()
  {
    for (int i = size; --i >= 0; )
    {
      boolean sorted = true;
      for (int j = 0; j < i; j++)
      {
        if (queue[j+1].nextExecutionTime > queue[j].nextExecutionTime)
        {
          RestartableTimerTask tmp = queue[j];
          queue[j] = queue[j+1];
          queue[j+1] = tmp;
          sorted = false;
        }
      }
      if (sorted)
      {
        break; // No change in order - nothing further to do - we can exit.
      }
    }
  }
  /**
   * insert() -- Insert a new task into the queue.  If the new task execution
   * time is the same as another task in the list, the new task will be
   * inserted BEFORE the task already in the list (so that the task already
   * in the list will be executed first).  The Next timer task to execute is
   * always at the BOTTOM of the list.
   * 
   * @param task ?
   * @return ?
   */
  private boolean insert(RestartableTimerTask task)
  {
    boolean inserted = false;
    //
    // The queue size has been increased for the new task, but the new task has
    // NOT been added to the queue yet.
    //
    if (size > 1)
    {
      RestartableTimerTask tmp = null;
      RestartableTimerTask taskOnQueue = null;
      for (int i = 0; i < size - 1; i++ )
      {
        taskOnQueue = queue[i];
        if (task.nextExecutionTime <= taskOnQueue.nextExecutionTime)
        {
          /**
           * Rest of tasks in list are already sorted so we can insert the new
           * task without doing further compares.
           */
          for (int j = i; j < size; j++)
          {
            tmp = queue[j];
            queue[j] = task;
            task = tmp;
          }
          inserted = true;
          break;
        }
      }
    }
    if (!inserted)
    {
      queue[size-1] = task; // put at end of queue
    }
    return inserted;
  }
}

