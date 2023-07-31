package com.daifukuamerica.wrxj.clc;

import com.daifukuamerica.wrxj.io.PropertyFileReader;
import com.daifukuamerica.wrxj.io.PropertyReader;
import java.util.Set;

/**
 * Immutable property hash.
 *
 * <p><b>Details:</b> {@link ReadOnlyProperties} encapsulates a generic,
 * read-<wbr>only set of named properties.  In concept this class is similar to
 * {@link java.util.Properties}, but an interface is used to support
 * various implementations and data sources, and only a limited set of getters
 * is defined.</p>
 *
 * <p>In most cases, you can fully implement this class simply by extending
 * {@link ReadOnlyPropertiesImpl}.  This will save you considerable
 * effort.</p>
 *
 * <p>This class will obsolete {@link PropertyFileReader} and
 * {@link PropertyReader}.</p>
 *
 * @see java.util.Properties
 * @see PropertyReader
 * @see PropertyFileReader
 * @author Sharky
 */
public interface ReadOnlyProperties
{

  /**
   * Returns string property.
   *
   * <p><b>Details:</b> {@link #getString(String)} returns the string value
   * associated with the named property.  If the named property does not exist,
   * <code>null</code> is returned.</p>
   *
   * @param isName the property name
   * @return the property value
   *
   * @see #getString(String, String)
   */
  String getString(String isName);

  /**
   * Returns string property or default value.
   *
   * <p><b>Details:</b> {@link #getString(String, String)} does the same
   * thing as {@link #getString(String)}, but instead of returning
   * <code>null</code> if the named property cannot be found, returns the
   * supplied default value.</p>
   *
   * @param isName the property name
   * @param isDefault default property value
   * @return the property value
   *
   * @see #getString(String)
   */
  String getString(String isName, String isDefault);

  /**
   * Returns int property.
   *
   * <p><b>Details:</b> {@link #getInt(String)} returns the <code>int</code> value
   * associated with the named property.  If the named property does not exist,
   * 0 is returned.</p>
   *
   * @param isName the property name
   * @return the property value as an int
   *
   * @see #getInt(String, int)
   */
  int getInt(String isName);

  /**
   * Returns int property or default value.
   *
   * <p><b>Details:</b> {@link #getInt(String, int)} does the same thing as
   * {@link #getInt(String)}, but instead of returning 0 if the named
   * property cannot be found, returns the supplied default value.</p>
   *
   * @param isName the property name
   * @param inDefault default property value
   * @return the property value as an int
   */
  int getInt(String isName, int inDefault);

  /**
   * Lists names beginning with prefix.
   * 
   * <p><b>Details:</b> This method returns all of the property names that begin 
   * with the supplied prefix.</p>
   * 
   * @param isPrefix the prefix
   * @return the names
   */
  Set<String> getNames(String isPrefix);
  
}

