package com.daifukuamerica.wrxj.clc;

/**
 * Generic ReadOnlyProperties implementation.
 *
 * <p><b>Details:</b> <code>ReadOnlyPropertiesImpl</code> features generic
 * implementations of several of <code>ReadOnlyProperties</code>'s methods.  To
 * implement a fully functional <code>ReadOnlyProperties</code> subclass, it is
 * sufficient to extend this class and implement
 * <code>getString(String,String)</code>.  Be sure to heed the polymorphism
 * warnings included below in each method detail.</p>
 *
 * @author Sharky
 */
public abstract class ReadOnlyPropertiesImpl implements ReadOnlyProperties
{

  /**
   * Returns string property.
   *
   * <p><b>Details:</b> <code>getString</code> returns the string value
   * associated with the named property.  If the named property does not exist,
   * <code>null</code> is returned.</p>
   *
   * <p><b>Polymorphism warning:</b> This method calls
   * <code>getString(String,String)</code>.</p>
   *
   * @param isName the property name
   * @return the property value
   *
   * @see #getString(String, String)
   */
  public String getString(String isName)
  {
    return getString(isName, null);
  }

  /**
   * Returns int property.
   *
   * <p><b>Details:</b> <code>getInt</code> returns the <code>int</code> value
   * associated with the named property.  If the named property does not exist,
   * 0 is returned.</p>
   *
   * <p><b>Polymorphism warning:</b> This method calls
   * <code>getInt(String)</code>.</p>
   *
   * @param isName the property name
   * @return the property value as an int
   *
   * @see #getInt(String, int)
   */
  public int getInt(String isName)
  {
    return getInt(isName, 0);
  }

  /**
   * Returns int property or default value.
   *
   * <p><b>Details:</b> <code>getInt(String,int)</code> does the same thing as
   * <code>getInt(String)</code>, but instead of returning 0 if the named
   * property cannot be found, returns the supplied default value.</p>
   *
   * <p><b>Polymorphism warning:</b> This method calls
   * <code>getInt(String,int)</code>.</p>
   *
   * @param isName the property name
   * @param inDefault default property value
   * @return the property value as an int
   */
  public int getInt(String isName, int inDefault)
  {
    try
    {
      return Integer.parseInt(getString(isName));
    }
    catch (NullPointerException ve)
    {
    }
    catch (NumberFormatException ve)
    {
    }
    return inDefault;
  }

}

