package com.daifukuamerica.wrxj.emulation.station;

import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.timer.RestartableTimer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class OrderSimulator
{
  private static Map<String, OrderSimulationTask> mpOrderTasks = new HashMap<String, OrderSimulationTask>();
  private static RestartableTimer mpTimer;

  private OrderSimulator()
  {
    // nothing to do here.
  }
  
  public static void updateValues(String isRoute, int inTime, boolean izFull, boolean izItemLot, boolean izMult)
  {
    OrderSimulationTask vpTask = mpOrderTasks.get(isRoute);
    if (vpTask == null)
    {
      startOrders(isRoute, inTime, izFull, izItemLot, izMult);
      return;
    }
    
    if (inTime != vpTask.getInterval())
      updateTaskInterval(vpTask, inTime);
    
    vpTask.updateValues(inTime, izFull, izItemLot, izMult);
  }
  
  private static void updateTaskInterval(OrderSimulationTask ipTask, int inValue)
  {
    if (mpTimer != null && mpTimer.isScheduled(ipTask))
    {
      stopTask(ipTask);
      startTask(ipTask, inValue);
    }
  }
  
  public static Set<String> getRoutes()
  {
    return mpOrderTasks.keySet();
  }
  
  private static void startOrders(String isRoute, int vnInterval, boolean izFull, boolean izItemLot, boolean izMult)
  {
    // If this is the first one start up the timer
    if (mpOrderTasks.size() == 0)
    {
      mpTimer = new RestartableTimer("Simulation - Order Timer");
    }
    OrderSimulationTask vpTask = Factory.create(OrderSimulationTask.class, isRoute, vnInterval, izFull, izItemLot, izMult);
    
    synchronized (mpOrderTasks)
    {
      mpOrderTasks.put(isRoute, vpTask);
    }
    startTask(vpTask, vnInterval);
  }

  public static void stopOrders(String isRoute)
  {
    synchronized (mpOrderTasks)
    {
      OrderSimulationTask vpTask = mpOrderTasks.remove(isRoute);
      stopTask(vpTask);
    }
    // If this is the last one destroy the timer
    if (mpOrderTasks.size() == 0)
    {
      mpTimer.cancel();
    }
  }
  
  private static void startTask(OrderSimulationTask ipTask, int inInterval)
  {
    System.out.println("Starting timer");
    mpTimer.setPeriodicTimerEvent(ipTask, inInterval*1000, 30000);
  }
  
  private static void stopTask(OrderSimulationTask ipTask)
  {
    System.out.println("Stopping timer");
    mpTimer.cancel(ipTask);
  } 
  
}
