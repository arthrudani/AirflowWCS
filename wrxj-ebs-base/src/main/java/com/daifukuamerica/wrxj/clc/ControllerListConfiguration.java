package com.daifukuamerica.wrxj.clc;

import com.daifukuamerica.wrxj.controller.Controller;
import java.util.List;

/**
 * Multi-controller configuration.
 *
 * <p><b>Details:</b> A {@link ControllerListConfiguration} represents a
 * list of controllers and their configurations, as interpreted from a
 * configuration source, such as a file or database.  The purpose of this
 * interface is to permit the deployment of various configuration sources or
 * formats with minimal impact on {@link ControllerImplFactory}'s design or
 * operation.</p>
 *
 * <p>A {@link ControllerListConfiguration} is a set of
 * {@link ControllerDefinition}s and
 * {@link ControllerTypeDefinition}s.  Every
 * {@link ControllerDefinition} has a controller name and a controller type
 * identifier, both strings.  No two controller definitions can have the same
 * controller name, but several controller definitions can have a common
 * controller type identifier.  The controller type identifier unambiguously
 * references a {@link ControllerTypeDefinition}, which encapsulates
 * information about the controller's type, implementation, and default
 * behavior.  Depending on the selected controller type, a controller definition
 * may also include configuration parameters that affect the controller's
 * behavior.</p>
 *
 * <p>Although several interesting interface methods could be defined in this
 * interface, the current strategy is to keep this interface as minimal as
 * possible, until the need for additional functionality becomes critical.</p>
 *
 * <p>None of the query methods defined in this interface return failure values.
 * Instead, all failures are reported by throwing
 * {@link ControllerConfigurationException}s.</p>
 *
 * <p><b>Constructor:</b> Although it is not possible to enforce constructor
 * interfaces in Java interface declarations, it is recommended that all
 * implementations of this interface include a constructor that accepts a
 * {@link String} as a single parameter.  This will facilitate the future
 * plan of allowing the configuration interface to be fully specified on the
 * application command line.</p>
 *
 * <p><b>Data path:</b> The data path for
 * {@link ControllerListConfiguration}, {@link ControllerDefinition},
 * and {@link ControllerTypeDefinition} is illustrated in the diagram
 * below:</p>
 *
 * <p align=center><img src=doc-files/clc_data_path.png></p>
 *
 * <p>Data flows as follows:</p>
 *
 * <ol>
 *   <li>A {@link ControllerListConfiguration} implementation is
 *     instantiated.</li>
 *   <li>{@link #listControllerNames} returns a {@link List} of
 *     {@link String}s, where each {@link String} is the name of a
 *     controller.</li>
 *   <li>A controller name is passed to {@link #getControllerDefinition},
 *     which returns a {@link ControllerDefinition}.</li>
 *   <li>The {@link ControllerDefinition}'s {@link ControllerDefinition#getType} returns a
 *     controller type identifier.</li>
 *   <li>The controller type identifier is passed into
 *     {@link #getControllerTypeDefinition} to obtain a
 *     {@link ControllerTypeDefinition}.</li>
 *   <li>The {@link ControllerTypeDefinition}'s
 *     {@link ControllerTypeDefinition#getImplementingClass} method returns a {@link Class}
 *     object representing the Java class for the controller.</li>
 * </ol>
 *
 * <p>Addition steps required for controller creation, not shown in the diagram
 * above, include:</p>
 *
 * <ol>
 *   <li>
 *     <p>Reflection is used on the resulting {@link Class} object to
 *     discover a static factory method matching the following signature:</p>
 *     <blockquote><code>
 *       Controller create(ReadOnlyProperties) throws
 *       ControllerConfigurationException
 *     </code></blockquote>
 *     <p>All implementations of {@link Controller} are required to
 *     implement this method.</p>
 *   </li>
 *   <li>The "properties" of the {@link ControllerDefinition} (available
 *     from {@link ControllerDefinition#getProperty}) and the "default properties" of the
 *     {@link ControllerTypeDefinition} (available from
 *     {@link ControllerTypeDefinition#getDefaultProperty}) are combined into a
 *     {@link ReadOnlyProperties} object, which is passed when invoking the
 *     factory.</li>
 * </ol>
 *
 * @author Sharky
 */
public interface ControllerListConfiguration
{
  public static final String CLC_SERVER = "Ctlrs";
  public static final String CLC_CLIENT = "client";
  public static final String CLC_EMULATORS = "emulators";
  
  /**
   * Lists all controller names.
   *
   * <p><b>Details:</b> {@link #listControllerNames} returns a list of the
   * names of all controllers defined in the configuration source.  Each element
   * of the returned list is a {@link String}.  Use
   * {@link #getControllerDefinition} to convert the controller name into a
   * controller definition.</p>
   *
   * <p>This method may return an empty list, but it will never return
   * <code>null</code>.</p>
   *
   * @return list of controller names
   * @throws ControllerConfigurationException if the list cannot be processed
   * @see #getControllerDefinition(String)
   */
  List<String> listControllerNames() throws ControllerConfigurationException;

  /**
   * Reads controller definiition.
   *
   * <p><b>Details:</b> {@link #getControllerDefinition} returns the
   * controller definition for the controller with the given name.  If no
   * controller has been defined with the given name, <code>null</code> is
   * returned.  This may happen if:</p>
   *
   * <ul>
   *   <li><var>isName</var> is set to a name not included in the
   *     {@link List} returned by {@link #listControllerNames}.</li>
   *   <li>The configuration source is incomplete or incorrect.</li>
   *   <li>I/O errors are preventing access to the configuration source.</li>
   * </ul>
   *
   * <p>This method will never return <code>null</code>.</p>
   *
   * @param isName the controller name
   * @return the controller definition
   * @throws ControllerConfigurationException if the controller definition
   *   cannot be obtained
   */
  ControllerDefinition getControllerDefinition(String isName) throws ControllerConfigurationException;

  /**
   * Reads controller type definition.
   *
   * <p><b>Details:</b> {@link #getControllerTypeDefinition} returns the
   * controller type definition associated with the given controller type
   * identifier.  If no such controller type exists, <code>null</code> is
   * returned.</p>
   *
   * <p>This method will never return <code>null</code>.</p>
   *
   * @param isIdentifier the controller type identifier
   * @return the controller type definition
   * @throws ControllerConfigurationException if the controller type definition
   *   cannot be obtained
   */
  ControllerTypeDefinition getControllerTypeDefinition(String isIdentifier) throws ControllerConfigurationException;

}

