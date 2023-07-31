package com.daifukuamerica.wrxj.application;

/**
 * Mutable layer in properties stack.
 *  
 * <p><b>Details:</b> This interface represents a 
 * {@link PropertiesLayer properties layer} whose properties can be 
 * modified.</p>
 *  
 * @author Sharky
 */
public interface MutablePropertiesLayer extends PropertiesLayer
{

  /**
   * Sets property.
   *  
   * <p><b>Details:</b> This method sets the named property to the given value.  
   * The name may indicate either an existing property whose value is to be 
   * modified, or a new property to be created.  Setting the value to 
   * <code>null</code> has the effect of removing the property from the layer, 
   * making the property visible, if available, in lower layers.</p>
   *  
   * @param isName the property's name
   * @param isValue the property's new value
   */
  void setProperty(String isName, String isValue);
  
}

