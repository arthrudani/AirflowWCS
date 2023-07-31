package com.daifukuamerica.wrxj.dataserver;

import com.daifukuamerica.wrxj.jdbc.DBException;

/* Description:<BR>
 *  Interface for all Warehouse Rx package plugins.  All classes that are considered
 *  plugin packages should implement this interface so that they can be attached
 *  or detached from the system using a package configuration GUI.
 *
 * @author       A.D.
 * @version      1.0
 * @since        03-May-2007
 */
public interface PackagePlugin
{
 /**
  *  Method makes all changes necessary to attach this package to the system
  *  such as adding database fields, key modifications, or property file
  *  changes.
  *
  *  @throws DBException when there is a serious database error that prevents
  *          DML changes from occuring.
  *  @throws ConfigException if there is a problem with changing the property
  *          file.
  */
  public void packageAttach() throws DBException, ConfigException;
  
 /**
  *  Method makes all changes necessary to detach this package from the system
  *  such as deleting database fields, key modifications, or property file changes.
  *
  *  @throws DBException when there is a serious database error that prevents
  *          DML changes from occuring.
  *  @throws ConfigException if there is a problem with changing the property
  *          file.
  */
  public void packageDetach() throws DBException, ConfigException;

 /**
  *  Method retrieves package information for display purposes.  It should
  *  normally be the version number, fully qualified package name and class name
  *  and a package description.
  *  
  * @return String array of the format:
  * <pre>String[0] =  version #
  *      String[1] = package name + class name
  *      String[2] = "Brief description of package (i.e. what it's used for etc.)</pre>
  */
  public String[] getPackageInfo();
}
