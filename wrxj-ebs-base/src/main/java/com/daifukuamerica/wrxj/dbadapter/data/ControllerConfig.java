package com.daifukuamerica.wrxj.dbadapter.data;

import com.daifukuamerica.wrxj.dbadapter.BaseDBInterface;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ControllerConfig table access.
 *
 * <p><b>Details:</b> This class provides methods to query and update the
 * {@code ControllerConfig} table.</p>
 *
 * @author Sharky
 */
public class ControllerConfig extends BaseDBInterface
{
  public static final String TIMEDEVENTSCHEDULER = "TimedEventScheduler";

  /**
   * Connects to database.
   *
   * <p><b>Details:</b> This constructor connects to the database, in
   * preparation for reading from and writing to the table.</p>
   */
  public ControllerConfig()
  {
    super("ControllerConfig");
  }

  /**
   * Retrieves property value.
   *
   * <p><b>Details:</b> This method looks up the value of the named property for
   * the named controller.  If a matching row exists, the {@code sPropertyValue}
   * field is returned.  Otherwise, this method returns <code>null</code>.</p>
   *
   * @param isController the controller name
   * @param isPropertyName the property name
   * @return the property value
   * @throws DBException if a database error occurs
   */
  public String getPropertyValue(String isController, String isPropertyName)
      throws DBException
  {
    String vsControllerName    = ControllerConfigData.CONTROLLER_NAME;
    String vsPropertyNameName  = ControllerConfigData.PROPERTYNAME_NAME;
    String vsPropertyValueName = ControllerConfigData.PROPERTYVALUE_NAME;
    String vsQuery = "SELECT " + vsPropertyValueName + " FROM " +
                     getWriteTableName() + " WHERE " +
                     vsControllerName + " = ? AND " +
                     vsPropertyNameName + " = ? AND " + 
                     ControllerConfigData.ENABLED_NAME + " = " + DBConstants.YES;

    List<Map> vpRecords = fetchRecords(vsQuery, isController, isPropertyName);
    if (vpRecords.isEmpty())
      return null;
    Map vpMap = vpRecords.get(0);
    String vsParameterValue = DBHelper.getStringField(vpMap, vsPropertyValueName);
    return vsParameterValue;
  }

  /**
   * Returns property names.
   *
   * <p><b>Details:</b> This method interprets the table as if it were an
   * ordinary (name, value) map and looks up the names of all properties
   * represented in the table.  Names are formed by concatenating
   * {@code sController} fields with {@code sPropertyName} fields, connecting
   * them with a dot.</p>
   *
   * @return the names
   * @throws DBException if a database error occurs
   */
  public Set<String> getDottedPropertyNames() throws DBException
  {
    String vsDottedPropertyName = "sDottedPropertyName";
    String vsControllerName = ControllerConfigData.CONTROLLER_NAME;
    String vsPropertyNameName = ControllerConfigData.PROPERTYNAME_NAME;
    String vsQuery = "SELECT CONCAT(CONCAT(" + vsControllerName + ", '.'), " +
                     vsPropertyNameName + ") AS " + vsDottedPropertyName + " FROM " +
                     getWriteTableName()
                     + " WHERE " + ControllerConfigData.ENABLED_NAME + " = " + DBConstants.YES;

    List<Map> vpRecords = fetchRecords(vsQuery);
    Set<String> vpSet = new HashSet<String>(vpRecords.size());
    for (Map vpMap: vpRecords)
    {
      String vsName = DBHelper.getStringField(vpMap, vsDottedPropertyName);
      vpSet.add(vsName);
    }
    return vpSet;
  }

  /**
   * Is this config enabled?
   * 
   * @param isController
   * @param isPropertyName
   * @return
   * @throws DBException
   */
  public boolean isEnabled(String isController, String isPropertyName)
      throws DBException
  {
    ControllerConfigData vpKey = Factory.create(ControllerConfigData.class);
    vpKey.setKey(ControllerConfigData.CONTROLLER_NAME, isController);
    vpKey.setKey(ControllerConfigData.PROPERTYNAME_NAME, isPropertyName);
    ControllerConfigData vpData = getElement(vpKey, DBConstants.NOWRITELOCK);
    if (vpData == null)
    {
      return false;
    }
    return vpData.getEnabled() == DBConstants.YES;
  }
}

