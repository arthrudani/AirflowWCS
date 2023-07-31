package com.daifukuamerica.wrxj.application;

import java.util.Set;

/**
 * Layer in properties stack.
 * 
 * <p><b>Details:</b> This interface represents a generic layer of properties,
 * to be added to a {@link PropertiesStack}.  All properties in a layer are 
 * reported as strings, but methods in {@link PropertiesStack} provide 
 * opportunities to interpret these string properties as other data types.</p>
 * 
 * @author Sharky
 */
public interface PropertiesLayer
{

  /**
   * Returns string property.
   * 
   * <p><b>Details:</b> This method returns the string property corresponding to
   * the given name, or <code>null</code> if the property is undefined or 
   * unavailable.</p>
   * 
   * @param isName the name
   * @return the property
   */
  String getProperty(String isName);
 
  /**
   * Returns all property names.
   * 
   * <p><b>Details:</b> This method returns the names of all properties defined
   * in this layer, as an unordered set, whose names begin with the given 
   * prefix.  If there are no matching properties, this method returns an empty 
   * set (not <code>null</code>!).  The supplied prefix may not be 
   * <code>null</code>.</p>
   * 
   * @param isPrefix the prefix
   * @return the names
   */
  Set<String> getPropertyNames(String isPrefix);
  
  /**
   * Clear cached values and reload (if supported).  It is not required for an 
   * implementation to actually do anything with this.
   */
  void refresh();
}

