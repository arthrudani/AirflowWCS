package com.daifukuamerica.wrxj.controller;

/**
 * Automatically names thread.
 * 
 * <p><b>Details:</b> This class extends {@link Thread} by attaching a 
 * meaningful name to the thread when it is constructed.</p>
 * 
 * <p>This class was introduced as a patch for existing thread expressions in 
 * Wrxj.  On 8/16/2005, expressions constructing {@link Thread}s were refactored 
 * to construct NamedThreads instead.  This was done to facilitate the analysis
 * of several thread-related problems.</p>
 *
 * <p>Constructors involving thread groups and stack sizes were excluded, since
 * they are not used in Wrxj.</p>
 * 
 * @author Sharky
 */
public class NamedThread extends Thread
{

  private static int gnCount;
  
  private static synchronized String deriveName(Runnable ipRunnable)
  {
    return ipRunnable.getClass().getSimpleName() + "-" + gnCount ++;
  }
  
  public NamedThread()
  {
    super();
    String vsName = deriveName(this);
    setName(vsName);
  }

  public NamedThread(Runnable ipRunnable)
  {
    super(ipRunnable);
    String vsName = deriveName(ipRunnable);
    setName(vsName);
  }

  public NamedThread(String isName)
  {
    super(isName);
  }

  public NamedThread(Runnable ipRunnable, String isName)
  {
    super(ipRunnable, isName);
  }

}

