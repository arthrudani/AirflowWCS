package com.daifukuamerica.wrxj.device.gateway;

import com.daifukuamerica.wrxj.controller.NamedThread;
import com.daifukuamerica.wrxj.log.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ThreadSystemGateway
{

  static final Map<Thread, SystemGateway> gpMap;
  
  static
  {
    Map<Thread, SystemGateway> vpMap = new HashMap<Thread, SystemGateway>();
    vpMap = Collections.synchronizedMap(vpMap);
    gpMap = vpMap;
    new Cleaner().start();
  }
  
  public static SystemGateway get()
  {
    Thread vpThread = Thread.currentThread();
    return get(vpThread);
  }

  public static SystemGateway get(Thread vpThread)
  {
    if (vpThread.getState() == Thread.State.TERMINATED)
      return null;
    if (vpThread instanceof ControllerImplThread)
    {
      SystemGateway vpControllerImpl = ((ControllerImplThread) vpThread).mpControllerImpl;
      return vpControllerImpl;
    }
    SystemGateway vpSystemGateway = gpMap.get(vpThread);
    if (vpSystemGateway == null)
    {
      Logger vpLogger = Logger.getLogger();
      vpSystemGateway = SystemGateway.create(vpLogger);
      gpMap.put(vpThread, vpSystemGateway);
    }
    return vpSystemGateway;
  }
  
  private static class Cleaner extends NamedThread
  {

    private int mnSgCount;
    
    Cleaner()
    {
      setPriority(MIN_PRIORITY);
    }
    
    @Override
    public void run()
    {
      while (! Thread.interrupted())
      {
        clean();
        setName("SystemGateway Cleaner (" + mnSgCount + " watched)");
        try
        {
          Thread.sleep(1000);
        }
        catch (Exception ex)
        {
          ex.printStackTrace(System.err);
        }
      }
    }
    
    private void clean()
    {
      Map<Thread, SystemGateway> vpMap;
      synchronized (gpMap)
      {
        vpMap = new HashMap<Thread, SystemGateway>(gpMap);
        mnSgCount = vpMap.size();
      }
      for (Map.Entry<Thread, SystemGateway> vpEntry: vpMap.entrySet())
      {
        Thread.yield();
        Thread vpThread = vpEntry.getKey();
        if (vpThread.getState() == Thread.State.TERMINATED)
        {
          gpMap.remove(vpThread);
          SystemGateway vpSystemGateway = vpEntry.getValue();
          SystemGateway.destroy(vpSystemGateway);
          -- mnSgCount;
        }
      }
    }
    
  }
  
}

