package com.daifukuamerica.wrxj.util;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.io.IoCloser;
import java.io.InputStream;
import java.util.Properties;

/**
 * Version information.
 *
 * <p><b>Details:</b> <code>WrxjVersion</code> provides information about the
 * currently running build of WRx-Java.  For this class to run correctly, the
 * properties file of the same name as this class must be stored in the same
 * package.  To modify the version string, edit the properties file.</p>
 *
 * @author Sharky
 */
public final class WrxjVersion
{
  private WrxjVersion() {}

  private static final String BASE_FILE = "/base_version.properties";
  private static final String DOUBLEDEEP_FILE = "/doubledeep_version.properties";
  private static final String CUSTOM_FILE = "/custom_version.properties";

  public static final String UNKNOWN = "?";

  private static final String gsBuildTime;
  private static final String gsVersionId;
  private static final String gsDDBuildTime;
  private static final String gsDDVersionId;
  private static final String gsCustomBuildTime;
  private static final String gsCustomVersionId;

  static
  {
    String vsBaseBuild = UNKNOWN;
    String vsBaseVersion = UNKNOWN;
    String vsDDBuild = UNKNOWN;
    String vsDDVersion = UNKNOWN;
    String vsCustomBuild = UNKNOWN;
    String vsCustomVersion = UNKNOWN;
    
    InputStream vpIn = null;
    try
    {
      // Base jar version information
      vpIn = WrxjVersion.class.getResourceAsStream(BASE_FILE);
      if (vpIn == null)
      {
        // Base jar must exist
        throw new Exception(BASE_FILE + " not found!");
      }
      String vasBaseInfo[] = getBuildTimeAndVersion(vpIn);
      vsBaseBuild = vasBaseInfo[0];
      vsBaseVersion = vasBaseInfo[1];
      
      // Double-Deep jar version information
      vpIn = WrxjVersion.class.getResourceAsStream(DOUBLEDEEP_FILE);
      if (vpIn != null)
      {
        String vasDDInfo[] = getBuildTimeAndVersion(vpIn);
        vsDDBuild = vasDDInfo[0];
        vsDDVersion = vasDDInfo[1];
      }
      
      // Custom jar version information
      vpIn = WrxjVersion.class.getResourceAsStream(CUSTOM_FILE);
      if (vpIn != null)
      {
        String vasCustomInfo[] = getBuildTimeAndVersion(vpIn);
        vsCustomBuild = vasCustomInfo[0];
        vsCustomVersion = vasCustomInfo[1];
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      IoCloser.close(vpIn);
    }

    gsBuildTime = vsBaseBuild;
    gsVersionId = vsBaseVersion;
    gsDDBuildTime = vsDDBuild;
    gsDDVersionId = vsDDVersion;
    gsCustomBuildTime = vsCustomBuild;
    gsCustomVersionId = vsCustomVersion;
  }

  /**
   * Extract build time and version from a resource file
   * 
   * @param ipResource
   * @return String[2] containing build and version information
   */
  private static String[] getBuildTimeAndVersion(InputStream ipResource)
  {
    String vsBuild = UNKNOWN;
    String vsVersion = UNKNOWN;
    
    try
    {
      if (ipResource == null)
      {
        throw new Exception("version.properties not found!");
      }
      Properties vpProperties = new Properties();
      vpProperties.load(ipResource);
      
      // Build ID is from the date
      vsBuild = vpProperties.getProperty("build.tstamp", "?");
      
      // Version ID is from the name/number of the build
      String vsName = vpProperties.getProperty("build.name", "?");
//      String vsNumber = vpProperties.getProperty("build.number", "?");
      vsVersion = vsName/* + " (build " + vsNumber + ")"*/;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      IoCloser.close(ipResource);
    }
    return new String[] {vsBuild, vsVersion};
  }
  
  /**
   * Returns build time.
   * 
   * <p><b>Details:</b> Returns the build time of this release of the Warehouse 
   * Rx baseline jar. The build ID takes the form <tt>yyyy.mm.dd.hhmm</tt>,
   * indicating the year, month, day, hour, and time of the build.</p>
   * 
   * @return build ID
   */
  public static String getBuildTime()
  {
    return gsBuildTime;
  }

  /**
   * Returns version ID.
   * 
   * <p><b>Details:</b> Returns a string representing the version ID of the
   * currently running build of the Warehouse Rx baseline jar. The string
   * returned is the value associated with the keys "<tt>name</tt>" and "
   * <tt>name</tt>" in the properties file for this class.</p>
   * 
   * @return version ID
   */
  public static String getVersionId()
  {
    return gsVersionId;
  }

  /**
   * Returns build time.
   * 
   * <p><b>Details:</b> Returns the build time of this release of the Warehouse 
   * Rx double-deep jar. The build ID takes the form <tt>yyyy.mm.dd.hhmm</tt>,
   * indicating the year, month, day, hour, and time of the build.</p>
   * 
   * @return build ID
   */
  public static String getDoubleDeepBuildTime()
  {
    return gsDDBuildTime;
  }

  /**
   * Returns version ID.
   * 
   * <p><b>Details:</b> Returns a string representing the version ID of the
   * currently running build of the Warehouse Rx double-deep jar. The string
   * returned is the value associated with the keys "<tt>name</tt>" and "
   * <tt>name</tt>" in the properties file for this class.</p>
   * 
   * @return version ID
   */
  public static String getDoubleDeepVersionId()
  {
    return gsDDVersionId;
  }

  /**
   * Returns build time.
   * 
   * <p><b>Details:</b> Returns the build time of this release of the Warehouse 
   * Rx custom jar. The build ID takes the form <tt>yyyy.mm.dd.hhmm</tt>,
   * indicating the year, month, day, hour, and time of the build.</p>
   * 
   * @return build ID
   */
  public static String getCustomBuildTime()
  {
    return gsCustomBuildTime;
  }

  /**
   * Returns version ID.
   * 
   * <p><b>Details:</b> Returns a string representing the version ID of the
   * currently running build of the Warehouse Rx custom jar. The string
   * returned is the value associated with the keys "<tt>name</tt>" and "
   * <tt>name</tt>" in the properties file for this class.</p>
   * 
   * @return version ID
   */
  public static String getCustomVersionId()
  {
    return gsCustomVersionId;
  }

  /**
   * Returns a copyright string for WarehouseRx
   * @return
   */
  public static String getCopyrightString()
  {
    String vsYear = getCustomBuildTime();
    if (vsYear.indexOf('.') < 1)
    {
      vsYear = getBuildTime();
    }
    if (vsYear.indexOf('.') < 1)
    {
      vsYear = "2009";
    }
    else
    {
      vsYear = vsYear.substring(0, vsYear.indexOf('.'));
    }
    return "Copyright © 1992-" + vsYear 
        + " Daifuku North America Holding Company.  All rights reserved.";
  }

  /**
   * Get the "official" software version
   * 
   * @return
   */
  public static String getSoftwareVersion()
  {
    String vsSoftwareVersion = "Warehouse Rx - ";
    // Add the project name
    String vsProjectName = Application.getString("ProjectName");
    if (vsProjectName != null)
    {
      vsSoftwareVersion += vsProjectName + " - ";
    }
    // Just get the version, not all the fields.
    String vsBuildTime = WrxjVersion.getCustomBuildTime();
    if (vsBuildTime.trim().length() < 4)
    {
      vsBuildTime = WrxjVersion.getBuildTime();
    }
    vsSoftwareVersion += vsBuildTime;
    
    return vsSoftwareVersion;
  }
}

