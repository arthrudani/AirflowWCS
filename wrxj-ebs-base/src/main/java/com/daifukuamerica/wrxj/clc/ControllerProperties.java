package com.daifukuamerica.wrxj.clc;

import java.util.Set;

/**
 * Exposes properties from controller definition.
 *
 * <p><b>Details:</b> {@link ControllerProperties} reads properties for a
 * specific controller using the "property" methods from its
 * {@link ControllerDefinition} and its
 * {@link ControllerTypeDefinition}.</p>
 *
 * <p><b>Reserved property names:</b> Certain property names are reserved and
 * will not result in calls to the generic "property" methods.  For more
 * information, consult {@link #getString(String, String)}.  The
 * restrictions described there apply to all of the property query methods in
 * this class.</p>
 *
 * @see #getString(String, String)
 *
 * @author Sharky
 */
public class ControllerProperties extends ReadOnlyPropertiesImpl
{

  /**
   * Controller type definition.
   *
   * <p><b>Details:</b> {@link #mpType} is the controller type definition
   * set by the constructor.</p>
   */
  private final ControllerTypeDefinition mpType;

  /**
   * Controller definition.
   *
   * <p><b>Details:</b> {@link #mpController} is the controller definition
   * set by the constructor.</p>
   */
  private final ControllerDefinition mpController;

  /**
   * Sets controller definition.
   *
   * <p><b>Details:</b> This constructor sets the
   * {@link ControllerTypeDefinition} and
   * {@link ControllerDefinition} that will be used to look up
   * controller properties.</p>
   *
   * @param ipType the controller type definition
   * @param ipController the controller definition
   */
  public ControllerProperties(ControllerTypeDefinition ipType, ControllerDefinition ipController)
  {
    if (ipType == null)
      throw new NullPointerException("ipType");
    if (ipController == null)
      throw new NullPointerException("ipController");
    mpType = ipType;
    mpController = ipController;
  }

  /**
   * Determines the value of the named property.
   *
   * <p><b>Details:</b> {@link #getString} queries the controller definition
   * to obtain a property named <var>isName</var> and returns it.  The
   * controller definition is queried first, and if the property is found it is
   * returned.  Otherwise, a default property is sought from the controller type
   * definition, which is returned if found.  As a last resort, the supplied
   * default value (<var>isDefault</var>) is returned.</p>
   *
   * <p>If, however, <var>isName</var> is set to one of the reserved property
   * names, the appropriate getter method for that reserved property will be
   * called instead, and its return value will be unconditionally returned.  All
   * of the reserved property names are defined in
   * {@link ControllerDefinition}.</p>
   *
   * @param isName the property name
   * @param isDefault default value to return if the property is not found
   * @return the property value
   *
   * @see ControllerDefinition
   */
  public String getString(String isName, String isDefault)
  {
    if (isName.equals(ControllerDefinition.CONTROLLER_NAME))
      return mpController.getName();
    if (isName.equals(ControllerDefinition.CONTROLLER_TYPE))
      return mpType.getIdentifier();
    try
    {
      String vsValue = mpController.getProperty(isName);
      if (vsValue != null)
        return vsValue;
      vsValue = mpType.getDefaultProperty(isName);
      if (vsValue != null)
        return vsValue;
    }
    catch (ControllerConfigurationException ve)
    {
    }
    return isDefault;
  }

  public Set<String> getNames(String isPrefix)
  {
    Set<String> vpNames = mpController.getPropertyNames(isPrefix);
    if (ControllerDefinition.CONTROLLER_NAME.startsWith(isPrefix))
      vpNames.add(ControllerDefinition.CONTROLLER_NAME);
    if (ControllerDefinition.CONTROLLER_TYPE.startsWith(isPrefix))
      vpNames.add(ControllerDefinition.CONTROLLER_TYPE);
    return vpNames;
  }

}

