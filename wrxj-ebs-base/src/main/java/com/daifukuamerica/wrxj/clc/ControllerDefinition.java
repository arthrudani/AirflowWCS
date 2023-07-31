package com.daifukuamerica.wrxj.clc;

import java.util.Set;

/**
 * Configuration information for individual controller.
 *
 * <p><b>Details:</b> A {@link ControllerDefinition} conveys the
 * configuration parameters for a single controller.  Every controller has a
 * name, a type, and an implementing class.  Getters are offered to discover
 * each of these properties.  Depending on the controller type, additional
 * controller properties may be available.  These additional properties are
 * accessed using a generic property getter.</p>
 *
 * @author Sharky
 */
public interface ControllerDefinition
{

  /**
   * Property identifier for controller name.
   *
   * <p><b>Details:</b> {@link #CONTROLLER_NAME} is set to
   * "<tt>ControllerName</tt>", the name of the property  whose value is (duh!)
   * the controller's name.  This constant is defined here for convenience and
   * to promote safe coding by discouraging the use of string literals in
   * related code.</p>
   *
   * @see #getName()
   */
  public static final String CONTROLLER_NAME = "ControllerName";

  /**
   * Property identifier for controller type.
   *
   * <p><b>Details:</b> {@link #CONTROLLER_TYPE} is set to
   * "<tt>ControllerType</tt>", the name of the property  whose value is (duh!)
   * the controller's type identifier.  This constant is defined here for
   * convenience and to promote safe coding by discouraging the use of string
   * literals in related code.</p>
   *
   * @see #getType()
   */
  public static final String CONTROLLER_TYPE = "ControllerType";

  /**
   * Property identifier for controller group.
   *
   * <p><b>Details:</b> {@link #CONTROLLER_GROUP} is set to
   * "<tt>ControllerGroup</tt>", the name of the property  whose value is (duh!)
   * the controller's group identifier.  This constant is defined here for
   * convenience and to promote safe coding by discouraging the use of string
   * literals in related code.</p>
   */
  public static final String CONTROLLER_GROUP = "ControllerGroup";

  /**
   * Returns controller name.
   *
   * <p><b>Details:</b> {@link #getName} returns the name associated with
   * the defined controller.  Every controller definition must include a unique
   * controller name in order to be a complete definition.  Hence,
   * {@link #getName} will never return <code>null</code>.</p>
   *
   * @return the name
   */
  String getName();

  /**
   * Returns controller type identifier.
   *
   * <p><b>Details:</b> {@link #getType} returns the type identifier
   * associated with the defined controller.  Every controller definition must
   * include a controller type identifier in order to be a complete definition.
   * Hence, {@link #getType} will never return <code>null</code>.</p>
   *
   * <p>The returned type identifier can be converted into a
   * {@link ControllerTypeDefinition} by calling
   * {@link ControllerListConfiguration#getControllerTypeDefinition}.
   *
   * @return the type identifier
   * @throws ControllerConfigurationException if the type cannot be determined
   * @see ControllerListConfiguration#getControllerTypeDefinition(String)
   */
  String getType() throws ControllerConfigurationException;

  /**
   * Retrieves named property by name.
   *
   * <p><b>Details:</b> {@link #getProperty} returns the value of the named
   * property as a string, or <code>null</code> if the property is not
   * defined.</p>
   *
   * <p>If it cannot be determined whether the property is defined, or if the
   * property is defined but cannot be loaded due to some unusual cause, a
   * {@link ControllerConfigurationException} will be thrown.</p>
   *
   * @param isName the property name
   * @return the property value
   * @throws ControllerConfigurationException if an error occurred while loading
   *   the property
   */
  String getProperty(String isName) throws ControllerConfigurationException;

  /**
   * Returns property names beginning with prefix.
   * 
   * <p><b>Details:</b> This method returns the set of property names for this
   * instance that start with the given prefix.</p>
   * 
   * @param isPrefix the prefix
   * @return the names
   */
  Set<String> getPropertyNames(String isPrefix);
  
}

