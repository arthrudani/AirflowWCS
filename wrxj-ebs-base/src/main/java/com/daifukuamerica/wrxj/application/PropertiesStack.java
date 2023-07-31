package com.daifukuamerica.wrxj.application;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Combines multiple properties sources.
 * 
 * <p><b>Details:</b> This class represents a collection of "property layers", 
 * or individual collections of properties that are layered on top of each 
 * other.  In a properties stack, layers are added individually, in a specified
 * order.  The first layer added has the highest priority, while the last layer 
 * added has lowest priority.  When client code queries a property from this 
 * stack, the first layer is examined, and if the requested property is defined 
 * at that layer, then the property's value is returned.  Otherwise, the second 
 * layer is queried, and so on, until the property is finally found, or all 
 * layers have been queried unsuccessfully.</p>
 * 
 * <p>Helper methods are included in this class to convert string properties 
 * into other data types.  Each of the property "getter" methods has two forms:
 * one that only names the property, and one that names the property and 
 * supplies a default value.  In the first form, if the named property is found, 
 * it is simply converted and returned.  However, if the named property is not
 * found, then a "zero-ish" value (i.e., <code>null</code>, <code>0</code>, 
 * <code>false</code>, etc.) is returned.  The second form differs from the
 * first form in that the supplied default value is returned, instead of the
 * "zero-ish" value, if the property is not found.</p>
 * 
 * <p>Values may also be <em>written</em> to the properties stack.  However, 
 * because there is (currently) no way for a client to specify which layer a 
 * value will be written to, a hidden layer is used to record all writes.  This 
 * layer is always present and is always consulted first during reads.</p>  
 * 
 * @author Sharky
 */
public final class PropertiesStack
{
  public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  /**
   * Represents null in write layer.
   * 
   * <p><b>Details:</b> This constant is a "magic value" used only in the hidden 
   * write layer (see type documentation).  Its presense in the hidden write 
   * layer indicates that a <code>null</code> value has been explicitly written, 
   * with the intention of masking the value found in the other, ordinary 
   * layers.</p>
   * 
   * <p>When a property is queried from the properties stack, the properties 
   * stack first checks the write layer.  If the write layer returns this magic 
   * value, then the properties stack determines that the property has been 
   * overwritten with a <code>null</code> value, and hence, the query's result 
   * should be <code>null</code>.  (In this case, there is no need to consult 
   * the other layers.)  On the other hand, if the write layer returns a 
   * <em>real</em> <code>null</code>, then it simply means that no value has 
   * been explicitly written for this property, and the search will continue 
   * with the other layers.</p>
   * 
   * <p>Without this mechanism, it would not be possible to "turn off" any of 
   * the properties defined in the normal layers.</p>
   */
  private static final String gsNull = new String();

  /**
   * Write layer.
   * 
   * <p><b>Details:</b> This layer is the hidden write layer.  Initially empty,
   * this layer accumulates property entries only when a property setter is 
   * invoked.  See {@link #gsNull} to learn how <code>null</code> properties can 
   * be written into this layer.</p>
   */
  private MutablePropertiesLayer mpWrites = new MapLayer();
  
  /**
   * Ordered list of layers.
   * 
   * <p><b>Details:</b> This field stores the layers that have been added to 
   * this stack.</p>
   */
  private List<PropertiesLayer> gpScopes = new ArrayList<PropertiesLayer>();

  /**
   * Counts layers.
   * 
   * <p><b>Details:</b> This method returns the number of layers that have been
   * added to this stack.</p>
   * 
   * @return the count
   */
  public int countLayers()
  {
    return gpScopes.size();
  }
  
  /**
   * Appends layer.
   * 
   * <p><b>Details:</b> This method appends the given layer to this stack, with
   * lowest priority.</p>
   * 
   * @param ipLayer
   */
  public void appendLayer(PropertiesLayer ipLayer)
  {
    gpScopes.add(ipLayer);
  }

  /**
   * Refresh the properties layers if possible
   */
  public void refresh()
  {
    for (PropertiesLayer pl : gpScopes) {
      pl.refresh();
    }
  }
  
  /**
   * Returns string property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, returns it.  If no 
   * property by the given name is available, the provided default string is 
   * returned.</p>
   * 
   * @param isName the property name
   * @param isDefault the default string
   * @return the property's value
   */
  public String getString(String isName, String isDefault)
  {
    String vsValue = mpWrites.getProperty(isName);
    if (vsValue != null)
    {
      if (vsValue == PropertiesStack.gsNull)
        vsValue = null;
      return vsValue;
    }
    for (PropertiesLayer vpScope: gpScopes)
    {
      vsValue = vpScope.getProperty(isName);
      if (vsValue != null)
        return vsValue;
    }
    return isDefault;
  }
  
  /**
   * Returns string property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, returns it.  If no 
   * property by the given name is available, <code>null</code> is returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public String getString(String isName)
  {
    return getString(isName, null);
  }

  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param isValue the property value
   */
  public void setString(String isName, String isValue)
  {
    if (isValue == null)
      isValue = PropertiesStack.gsNull;
    mpWrites.setProperty(isName, isValue);
  }
  
  /**
   * Returns char property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, returns the first 
   * character.  If no property by the given name is available, or if the string
   * property is an empty string, then the provided default {@code char} is 
   * returned.</p>
   * 
   * @param isName the property name
   * @param icDefault the default char
   * @return the property's value
   */
  public char getChar(String isName, char icDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null || vsAttribute.length() == 0)
      return icDefault;
    return vsAttribute.charAt(0);
  }
  
  /**
   * Returns char property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, returns the first 
   * character.  If no property by the given name is available, or if the string
   * property is an empty string, '\u0000' is returned.</p>
   * 
   * @param isName the property name
   * @param icDefault the default char
   * @return the property's value
   */
  public char getChar(String isName)
  {
    return getChar(isName, '\u0000');
  }

  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param icValue the property value
   */
  public void setChar(String isName, char icValue)
  {
    setString(isName, Character.toString(icValue));
  }
  
  /**
   * Returns boolean property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code boolean} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code boolean}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param izDefault the default value
   * @return the property's value
   */
  public boolean getBoolean(String isName, boolean izDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return izDefault;
    vsAttribute = vsAttribute.trim().toLowerCase();
    if (vsAttribute.equals(Boolean.TRUE.toString()))
      return true;
    if (vsAttribute.equals(Boolean.FALSE.toString()))
      return false;
    return izDefault;
  }
  
  /**
   * Returns boolean property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code boolean} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code boolean}, 
   * <code>false</code> is returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public boolean getBoolean(String isName)
  {
    return getBoolean(isName, false);
  }
  
  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param izValue the property value
   */
  public void setBoolean(String isName, boolean izValue)
  {
    setString(isName, Boolean.toString(izValue));
  }
  
  /**
   * Returns byte property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code byte} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code byte}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param ibDefault the default value
   * @return the property's value
   */
  public byte getByte(String isName, byte ibDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return ibDefault;
    try
    {
      return Byte.parseByte(vsAttribute);
    }
    catch (NumberFormatException ex)
    {
      return ibDefault;
    }
  }
  
  /**
   * Returns byte property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code byte} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code byte}, 0 is 
   * returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public byte getByte(String isName)
  {
    return getByte(isName, (byte) 0);
  }
  
  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param ibValue the property value
   */
  public void setByte(String isName, byte ibValue)
  {
    setString(isName, Byte.toString(ibValue));
  }
  
  /**
   * Returns short property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code short} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code short}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param iwDefault the default value
   * @return the property's value
   */
  public short getShort(String isName, short iwDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return iwDefault;
    try
    {
      return Short.parseShort(vsAttribute);
    }
    catch (NumberFormatException ex)
    {
      return iwDefault;
    }
  }
  
  /**
   * Returns short property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code short} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code short}, 0 is 
   * returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public short getShort(String isName)
  {
    return getShort(isName, (short) 0);
  }

  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param iwValue the property value
   */
  public void setShort(String isName, short iwValue)
  {
    setString(isName, Short.toString(iwValue));
  }

  /**
   * Returns int property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code int} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code int}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param inDefault the default value
   * @return the property's value
   */
  public int getInt(String isName, int inDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return inDefault;
    try
    {
      return Integer.parseInt(vsAttribute);
    }
    catch (NumberFormatException ex)
    {
      return inDefault;
    }
  }
  
  /**
   * Returns int property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code int} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code int}, 0 is 
   * returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public int getInt(String isName)
  {
    return getInt(isName, 0);
  }
  
  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param inValue the property value
   */
  public void setInt(String isName, int inValue)
  {
    setString(isName, Integer.toString(inValue));
  }
  
  /**
   * Returns long property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code long} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code long}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param ilDefault the default value
   * @return the property's value
   */
  public long getLong(String isName, long ilDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return ilDefault;
    try
    {
      return Long.parseLong(vsAttribute);
    }
    catch (NumberFormatException ex)
    {
      return ilDefault;
    }
  }
  
  /**
   * Returns long property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code long} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code long}, 0 is 
   * returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public long getLong(String isName)
  {
    return getLong(isName, 0);
  }
  
  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param ilValue the property value
   */
  public void setLong(String isName, long ilValue)
  {
    setString(isName, Long.toString(ilValue));
  }
  
  /**
   * Returns float property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code float} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code float}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param ifDefault the default value
   * @return the property's value
   */
  public float getFloat(String isName, float ifDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return ifDefault;
    try
    {
      return Float.parseFloat(vsAttribute);
    }
    catch (NumberFormatException ex)
    {
      return ifDefault;
    }
  }
  
  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param ifValue the property value
   */
  public void setFloat(String isName, float ifValue)
  {
    setString(isName, Float.toString(ifValue));
  }
  
  /**
   * Returns float property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code float} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code float}, 0 is 
   * returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public float getFloat(String isName)
  {
    return getFloat(isName, 0);
  }
  
  /**
   * Returns double property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code double} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code double}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param idDefault the default value
   * @return the property's value
   */
  public double getDouble(String isName, double idDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return idDefault;
    try
    {
      return Double.parseDouble(vsAttribute);
    }
    catch (NumberFormatException ex)
    {
      return idDefault;
    }
  }
  
  /**
   * Returns double property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code double} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code double}, 0 is 
   * returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public double getDouble(String isName)
  {
    return getDouble(isName, 0);
  }

  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param idValue the property value
   */
  public void setDouble(String isName, double idValue)
  {
    setString(isName, Double.toString(idValue));
  }
  
  /**
   * Returns Date property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code Date} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code Date}, the 
   * provided default value is returned.</p>
   * 
   * @param isName the property name
   * @param idDefault the default value
   * @return the property's value
   */
  public Date getDate(String isName, Date idDefault)
  {
    String vsAttribute = getString(isName);
    if (vsAttribute == null)
      return idDefault;
    try
    {
      SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
      return df.parse(vsAttribute);
    }
    catch (ParseException ex)
    {
      return idDefault;
    }
  }
  
  /**
   * Returns Date property.
   * 
   * <p><b>Details:</b> This method searches this properties stack for a string 
   * property with the given name, and if one is found, converts it to a 
   * {@code Date} value and returns it.  If no property by the given name is 
   * available, or if the string cannot be converted to a {@code Date}, 0 is 
   * returned.</p>
   * 
   * @param isName the property name
   * @return the property's value
   */
  public Date getDate(String isName)
  {
    return getDate(isName, null);
  }

  /**
   * Writes property.
   *  
   * <p><b>Details:</b> This method writes the given property name and value to
   * the hidden write layer.  Subsequent reads of the same property will produce
   * the last value written.</p>
   * 
   * @param isName the property name
   * @param idValue the property value
   */
  public void setDate(String isName, Date idValue)
  {
    SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    setString(isName, df.format(idValue));
  }

  /**
   * Returns all property names.
   * 
   * <p><b>Details:</b> This method combines all of property names beginning 
   * with the given search prefix, from all the layers, and returns them in an 
   * unordered set.</p>
   * 
   * @param isPrefix the search prefix
   * @return the names
   */
  public Set<String> getNames(String isPrefix)
  {
    Set<String> vpCombinedSet = new HashSet<String>();
    Set<String> vpLayerSet = mpWrites.getPropertyNames(isPrefix);
    vpCombinedSet.addAll(vpLayerSet);
    for (PropertiesLayer vpLayer: gpScopes)
    {
      vpLayerSet = vpLayer.getPropertyNames(isPrefix);
      vpCombinedSet.addAll(vpLayerSet);
    }
    return vpCombinedSet;
  }

  /**
   * Selects strings beginning with prefix.
   * 
   * <p><b>Details:</b> This utility method selects the strings from the given 
   * set of strings that begin with the given search prefix, and returns them in 
   * a new set.</p>
   * 
   * <p>This method may be useful in implementations of 
   * {@link PropertiesLayer#getPropertyNames(String)}.</p>
   * 
   * @param ipAllNames the set of strings to search through
   * @param isPrefix the search prefix
   * @return the set of matching strings
   */
  // TODO Move to general-purpose location.
  public static Set<String> selectByPrefix(Set<String> ipAllNames, String isPrefix)
  {
    Set<String> vpMatchingNames = new HashSet<String>();
    for (String vsName: ipAllNames)
      if (vsName.startsWith(isPrefix))
        vpMatchingNames.add(vsName);
    return vpMatchingNames;
  }
  
}

