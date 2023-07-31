package com.daifukuamerica.wrxj.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Application parameters.
 *  
 * <p><b>Details:</b> This singleton class offers application-level features,
 * such as the ability to query application parameters (also known as 
 * "properties").</p>
 * 
 * <p>This class retrieves properties by querying a properties stack.  Thus,
 * bootstrapping code should add at least one properties layer to the stack
 * before querying properties.  For more information, see 
 * {@link PropertiesStack}.</p> 
 * 
 * @author Sharky
 */
public final class Application
{
  public static final String CONTROLLERCFG_DOMAIN = "ControllerConfig.";
  public static final String SYSCFG_DOMAIN = "SysConfig.";
  public static final String HOSTCFG_DOMAIN = "HostConfig.";
  public static final String GLOBALSETTING_DOMAIN = "GlobalSetting.";

  /**
   * Global properties stack.
   * 
   * <p><b>Details:</b> This field is the global properties stack referenced by
   * all the property getters in this class.</p>
   */
  private static final PropertiesStack gpProperties = new PropertiesStack();

  /**
   * Are properties defined?
   * 
   * <p><b>Details:</b> This method reports true iff the properties stack has 
   * any layers in it.</p>
   * 
   * @return true iff properties stack is non-empty
   */
  public static boolean arePropertiesDefined()
  {
    return gpProperties.countLayers() > 0;
  }
  
  /**
   * Appends layer to properties stack.
   *  
   * <p><b>Details:</b> This method adds the given properties layer to the 
   * existing properties stack (which may be empty to begin with).  Properties 
   * defined in the given layer have lower priority than properties found in 
   * layers that have already been added.</p>
   * 
   * @param ipLayer
   */
  public static void addPropertiesLayer(PropertiesLayer ipLayer)
  {
    gpProperties.appendLayer(ipLayer);
  }
  
  /**
   * Refresh the properties layers if possible
   */
  public static void refreshPropertiesLayers()
  {
    gpProperties.refresh();
  }
  
  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param isDefault passed to delegate
   * @return returned by delegate
   */
  public static String getString(String isName, String isDefault)
  {
    return gpProperties.getString(isName, isDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static String getString(String isName)
  {
    return gpProperties.getString(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param isValue passed to delegate
   */
  public static void setString(String isName, String isValue)
  {
    gpProperties.setString(isName, isValue);
  }
  
  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param icDefault passed to delegate
   * @return returned by delegate
   */
  public static char getChar(String isName, char icDefault)
  {
    return gpProperties.getChar(isName, icDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static char getChar(String isName)
  {
    return gpProperties.getChar(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param icValue passed to delegate
   */
  public static void setChar(String isName, char icValue)
  {
    gpProperties.setChar(isName, icValue);
  }
  
  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param izDefault passed to delegate
   * @return returned by delegate
   */
  public static boolean getBoolean(String isName, boolean izDefault)
  {
    return gpProperties.getBoolean(isName, izDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static boolean getBoolean(String isName)
  {
    return gpProperties.getBoolean(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param izValue passed to delegate
   */
  public static void setBoolean(String isName, boolean izValue)
  {
    gpProperties.setBoolean(isName, izValue);
  }
  
  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param ibDefault passed to delegate
   * @return returned by delegate
   */
  public static byte getByte(String isName, byte ibDefault)
  {
    return gpProperties.getByte(isName, ibDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static byte getByte(String isName)
  {
    return gpProperties.getByte(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param ibValue passed to delegate
   */
  public static void setByte(String isName, byte ibValue)
  {
    gpProperties.setByte(isName, ibValue);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param iwDefault passed to delegate
   * @return returned by delegate
   */
  public static short getShort(String isName, short iwDefault)
  {
    return gpProperties.getShort(isName, iwDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static short getShort(String isName)
  {
    return gpProperties.getShort(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param iwValue passed to delegate
   */
  public static void setShort(String isName, short iwValue)
  {
    gpProperties.setShort(isName, iwValue);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param inDefault passed to delegate
   * @return returned by delegate
   */
  public static int getInt(String isName, int inDefault)
  {
    return gpProperties.getInt(isName, inDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static int getInt(String isName)
  {
    return gpProperties.getInt(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param inValue passed to delegate
   */
  public static void setInt(String isName, int inValue)
  {
    gpProperties.setInt(isName, inValue);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param ilDefault passed to delegate
   * @return returned by delegate
   */
  public static long getLong(String isName, long ilDefault)
  {
    return gpProperties.getLong(isName, ilDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static long getLong(String isName)
  {
    return gpProperties.getLong(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param ilValue passed to delegate
   */
  public static void setLong(String isName, long ilValue)
  {
    gpProperties.setLong(isName, ilValue);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param ifDefault passed to delegate
   * @return returned by delegate
   */
  public static float getFloat(String isName, float ifDefault)
  {
    return gpProperties.getFloat(isName, ifDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static float getFloat(String isName)
  {
    return gpProperties.getFloat(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param ifValue passed to delegate
   */
  public static void setFloat(String isName, float ifValue)
  {
    gpProperties.setFloat(isName, ifValue);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param idDefault passed to delegate
   * @return returned by delegate
   */
  public static double getDouble(String isName, double idDefault)
  {
    return gpProperties.getDouble(isName, idDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static double getDouble(String isName)
  {
    return gpProperties.getDouble(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param idValue passed to delegate
   */
  public static void setDouble(String isName, double idValue)
  {
    gpProperties.setDouble(isName, idValue);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param idDefault passed to delegate
   * @return returned by delegate
   */
  public static Date getDate(String isName, Date idDefault)
  {
    return gpProperties.getDate(isName, idDefault);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-named method in 
   * {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @return returned by delegate
   */
  public static Date getDate(String isName)
  {
    return gpProperties.getDate(isName);
  }

  /**
   * Delegates to global properties stack.
   * 
   * <p><b>Details:</b> This method delegates to the similarly-<wbr>named method
   * in {@link PropertiesStack}, using the global properties stack.</p>
   * 
   * @param isName passed to delegate
   * @param idValue passed to delegate
   */
  public static void setDate(String isName, Date idValue)
  {
    gpProperties.setDate(isName, idValue);
  }

  /**
   * Returns all property names.
   * 
   * <p><b>Details:</b> This method returns an unordered set of property names 
   * beginning with the given search prefix that are available in the properties 
   * stack.</p>
   * 
   * @param isPrefix the search prefix
   * @return the names
   */
  public static Set<String> getPropertyNames(String isPrefix)
  {
    return gpProperties.getNames(isPrefix);
  }

  /**
   * Prints selected application properties.
   * 
   * <p><b>Details:</b> This method, provided for debugging purposes, prints to
   * the console the names and values of all available application properties 
   * that begin with the supplied prefix.</p>
   * 
   * @param isPrefix the search prefix
   */
  public static void printProperties(String isPrefix)
  {
    Set<String> vpNamesSet = getPropertyNames(isPrefix);
    List<String> vpNamesList = new ArrayList<String>(vpNamesSet);
    Collections.sort(vpNamesList);
    int vnWidestNameWidth = 1;
    for (String vsName: vpNamesList)
    {
      int vnNameWidth = vsName.length();
      if (vnNameWidth > vnWidestNameWidth)
        vnWidestNameWidth = vnNameWidth;
    }
    String vsFormat = "%-" + vnWidestNameWidth + "s=%s\n";
    for (String vsName: vpNamesList)
    {
      String vsValue = getString(vsName);
      System.out.printf(vsFormat, vsName, vsValue);
    }
  }
  
}

