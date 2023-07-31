package com.daifukuamerica.wrxj.clc;

import com.daifukuamerica.wrxj.controller.Controller;

/**
 * Implementation information for individual controller type.
 *
 * <p><b>Details:</b> A {@link ControllerTypeDefinition} represents the
 * configuration information associated with a controller type identifier.
 * These configuration settings include a reference to the implementing class
 * and an optional set of default properties.  The individual controller
 * definition referencing this controller type may override the controller
 * type's default properties.</p>
 *
 * @see ControllerDefinition
 * @author Sharky
 */
public interface ControllerTypeDefinition
{

  /**
   * Returns controller type identifier.
   *
   * <p><b>Details:</b> {@link #getIdentifier} returns the controller type
   * identifier, or the name of this controller type.</p>
   *
   * @return the controller type identifier
   */
  String getIdentifier();

  /**
   * Queries implementing class.
   *
   * <p><b>Details:</b> {@link #getImplementingClass} returns the
   * implementing class for this controller type.  If no implementing class has
   * been defined, or if the implementing class cannot be found, this method
   * will throw a {@link ControllerConfigurationException}.</p>
   *
   * <p>The implementing class must implement the following static factory
   * method:</p>
   *
   *<blockquote><pre>
   *static Controller create(ReadOnlyProperties ipConfig) throws ControllerConfigurationException;
   *</pre></blockquote>
   *
   * <p>Note that the {@link Controller} interface cannot enforce this
   * requirement at compile time, but implementations may optionally enforce it
   * at runtime.</p>
   *
   * @return the implementing class
   * @throws ControllerConfigurationException if the class name cannot be
   *   resolved from the configuration source
   * @throws ClassNotFoundException if the class name was resolved, but the
   *   class could not be loaded
   */
  Class<? extends Controller> getImplementingClass() throws ControllerConfigurationException, ClassNotFoundException;

  /**
   * Queries default property.
   *
   * <p><b>Details:</b> {@link #getDefaultProperty} looks up and returns, in
   * string form, the named default property for this controller type.
   * <code>null</code> is returned if no default property exists for the given
   * name.  The referencing controller definition may honor or override these
   * values.</p>
   *
   * <p>If it cannot be determined whether the property is defined, or if the
   * property is defined but cannot be loaded due to some unusual cause, a
   * {@link ControllerConfigurationException} will be thrown.</p>
   *
   * @param isName the property's name
   * @return the property's value
   * @throws ControllerConfigurationException if an error occurred while loading
   *   the property
   */
  String getDefaultProperty(String isName) throws ControllerConfigurationException;

}

