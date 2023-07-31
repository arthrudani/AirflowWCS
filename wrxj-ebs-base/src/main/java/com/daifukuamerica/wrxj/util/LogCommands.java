package com.daifukuamerica.wrxj.util;

public class LogCommands extends Commands
{
  private String[] switches = 
  {
    "logf",
    "logp",
    "logl",
    "env",
  };

  public LogCommands(String[] args)
  {
    super(args, null);    
    setSwitches(switches);

    try
    {
      parseCommandLine();
    } catch (IllegalArgumentException e)
    {
      help();
      System.exit(-1);
    }
    if (switchPresent("help"))
    {
      help();
      System.exit(-1);
    }
  }

  public void help()
  {
    System.out.println("\njava <class> [commands]");
    System.out.println("where commands are:\n");
    System.out.println(" -help"  + "\t\tDisplay this.");
    System.out.println(" -logf:" + "<file>\tlog file name");
    System.out.println(" -logp:" + "<path>\tpath to log file");
    System.out.println(" -logl:" + "<FATAL, ERROR, WARN, INFO, DEBUG, ALL, OFF>");
    System.out.println("\nall switches are case sensitive");
  }
}
