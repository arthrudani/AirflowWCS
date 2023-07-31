package com.daifukuamerica.wrxj.emulation.station;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * A temporary, quick and dirty tool for simulation logging.
 * 
 * Hopefully this class won't be around for too long as it may lead to small
 * gatherings of snickering software engineers tarnishing my good name.
 * @author karmstrong
 *
 */
public class KipsSKDCSimulationLoggerErrorLoggingImpl
{
  
  private static PrintWriter mpOut;
  
  private KipsSKDCSimulationLoggerErrorLoggingImpl()
  {
    // Ha!  Now no one can instantiate this class!
  }

  public static void instantiateThisClass()
  {
    // Create the file and connection we will use for logging.
    try
    {
      File vpLogFile = new File("SimulationLogs.txt");
      mpOut = new PrintWriter(new FileOutputStream(vpLogFile));
    }
    catch (FileNotFoundException ex)
    {
      System.err.println("Error creating Simulation Log file!!");
    }
  }
  
  public static void println(String isText)
  {
    if (mpOut == null)
      instantiateThisClass();
    mpOut.println(isText);
  }
  
  public static void println(Throwable ex)
  {
    ex.printStackTrace(mpOut);
  }
  
  public static void closeEverything()
  {
    if (mpOut != null)
      mpOut.close();
  }
}
