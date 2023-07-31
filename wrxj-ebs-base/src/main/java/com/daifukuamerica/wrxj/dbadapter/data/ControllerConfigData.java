package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigEnum.*;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ControllerConfig row.
 * 
 * <p><b>Details:</b> This class represents a row from the 
 * {@code ControllerConfig} table.</p>
 * 
 * @author Sharky
 */
public class ControllerConfigData extends AbstractSKDCData
{
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

/*============================================================================
 *                            COLUMN NAMES
 *============================================================================*/
  public static final String CONTROLLER_NAME    = CONTROLLER.getName();
  public static final String PROPERTYNAME_NAME  = PROPERTYNAME.getName();
  public static final String PROPERTYVALUE_NAME = PROPERTYVALUE.getName();
  public static final String PROPERTYDESC_NAME = PROPERTYDESC.getName();
  public static final String ENABLED_NAME = ENABLED.getName();
  public static final String SCREENCHANGEALLOWED_NAME = SCREENCHANGEALLOWED.getName();

  /**
   * sController value.
   *
   * <p><b>Details:</b> This field is the current value of the
   * {@code sController} field.  It is never <code>null</code>.</p>
   */
  private String msController    = "";
  /**
   * sPropertyName value.
   *
   * <p><b>Details:</b> This field is the current value of the
   * {@code sPropertyName} field.  It is never <code>null</code>.</p>
   */
  private String msPropertyName  = "";
  /**
   * sPropertyValue value.
   *
   * <p><b>Details:</b> This field is the current value of the
   * {@code sPropertyValue} field.  It is never <code>null</code>.</p>
   */
  private String msPropertyValue = "";

  private String msPropertyDesc = "";

  private int mnEnabled = 1;

  private int mnScreenChangeAllowed = 1;

  public ControllerConfigData()
  {
    super();
    initColumnMap(mpColumnMap, ControllerConfigEnum.class);
  }

  /**
   * Summarizes properties in string.
   *
   * <p><b>Details:</b> This method returns a string representation of all the
   * properties in this instance.</p>
   */
  @Override
  public String toString()
  {
    String vsString =
      CONTROLLER_NAME          + ":" + msController      + SKDCConstants.EOL_CHAR +
      PROPERTYNAME_NAME        + ":" + msPropertyName    + SKDCConstants.EOL_CHAR +
      PROPERTYVALUE_NAME       + ":" + msPropertyValue   + SKDCConstants.EOL_CHAR +
      PROPERTYDESC_NAME        + ":" + msPropertyDesc    + SKDCConstants.EOL_CHAR +
      ENABLED_NAME             + ":" + mnEnabled         + SKDCConstants.EOL_CHAR +
      SCREENCHANGEALLOWED_NAME + ":" + mnScreenChangeAllowed;

    vsString += super.toString();

    return vsString;
  }

  /**
   * Resets properties.
   *
   * <p><b>Details:</b> This method resets this instance's properties to their
   * default values.</p>
   */
   @Override
   public void clear()
   {
     super.clear();
     msController      = msPropertyName     = "";
     msPropertyValue   = msPropertyDesc     = "";
     mnEnabled = mnScreenChangeAllowed = 1;
   }

  /**
   * Compares to other instance.
   *
   * <p><b>Details:</b> This method compares this {@link ControllerConfigData}
   * to the given object, which should be an instance of this class.  If the
   * given object is not an instance of this class, or if any of the given
   * instance's properties are not exactly equal to the properties of this
   * instance, this method returns <code>false</code>.  Otherwise, this method
   * returns <code>true</code>.</p>
   *
   * @param ipThat the instance to compare to this
   * @return true if the two instances are equivalent
   */
  @Override
  public boolean equals(AbstractSKDCData ipThat)
  {
    if (!(ipThat instanceof ControllerConfigData))
      return false;
    ControllerConfigData vpThat = (ControllerConfigData)ipThat;

    return(msController.equals(vpThat.msController)       &&
           msPropertyName .equals(vpThat.msPropertyName)  &&
           msPropertyValue.equals(vpThat.msPropertyValue) &&
           msPropertyDesc.equals(vpThat.msPropertyDesc));
  }

  /**
   * Retrieves sController field.
   * 
   * <p><b>Details:</b> This method returns the current value of the 
   * {@code sController} field.  The returned value is never 
   * <code>null</code>.</p>
   * 
   * @return the field value
   */
  public String getController()
  {
    return msController;
  }

  /**
   * Retrieves sPropertyName field.
   *
   * <p><b>Details:</b> This method returns the current value of the
   * {@code sPropertyName} field.  The returned value is never
   * <code>null</code>.</p>
   *
   * @return the value
   */
  public String getPropertyName()
  {
    return msPropertyName;
  }

  /**
   * Retrieves sPropertyValue field.
   *
   * <p><b>Details:</b> This method returns the current value of the
   * {@code sPropertyValue} field.  The returned value is never
   * <code>null</code>.</p>
   *
   * @return the field value
   */
  public String getPropertyValue()
  {
    return msPropertyValue;
  }

  /**
   * Retrieves sPropertyDesc field.
   *
   * <p><b>Details:</b> This method returns the current value of the
   * {@code sPropertyDesc} field.  The returned value is never
   * <code>null</code>.</p>
   *
   * @return the field value
   */
  public String getPropertyDesc()
  {
    return msPropertyDesc;
  }

  /**
   * Retrieves iEnabled field.
   *
   * <p><b>Details:</b> This method returns the current value of the
   * {@code iEnabled} field.  The returned value is never
   * <code>null</code>.</p>
   *
   * @return the field value
   */
  public int getEnabled()
  {
    return mnEnabled;
  }

  /**
   * Retrieves iScreenChangeAllowed field.
   *
   * <p><b>Details:</b> This method returns the current value of the
   * {@code iScreenChangeAllowed} field.  The returned value is never
   * <code>null</code>.</p>
   *
   * @return the field value
   */
  public int getScreenChangeAllowed()
  {
    return mnScreenChangeAllowed;
  }

  /**
   * Sets sController field.
   * 
   * <p><b>Details:</b> This method sets the {@code sController} field to the 
   * given value.  If the provided value is <code>null</code>, it will be 
   * automatically replaced with the default value for this field.</p>
   * 
   * @param isController the new value
   */
  public void setController(String isController)
  {
    if (isController == null)
      isController = "";
    msController = isController;
    addColumnObject(new ColumnObject(CONTROLLER_NAME, isController));
  }

  /**
   * Sets sPropertyName field.
   * 
   * <p><b>Details:</b> This method sets the {@code sPropertyName} field to the 
   * given value.  If the provided value is <code>null</code>, it will be 
   * automatically replaced with the default value for this field.</p>
   * 
   * @param isPropertyName the new value 
   */
  public void setPropertyName(String isPropertyName)
  {
    msPropertyName = isPropertyName;
    addColumnObject(new ColumnObject(PROPERTYNAME_NAME, msPropertyName));
  }

  /**
   * Sets sPropertyValue field.
   * 
   * <p><b>Details:</b> This method sets the {@code sPropertyValue} field to the 
   * given value.  If the provided value is <code>null</code>, it will be 
   * automatically replaced with the default value for this field.</p>
   * 
   * @param isPropertyValue the new value
   */
  public void setPropertyValue(String isPropertyValue)
  {
    msPropertyValue = isPropertyValue;
    addColumnObject(new ColumnObject(PROPERTYVALUE_NAME, msPropertyValue));
  }
  
  /**
   * Sets PropertyDesc field.
   * 
   * <p><b>Details:</b> This method sets the {@code sPropertyDesc} field to the 
   * given value.  If the provided value is <code>null</code>, it will be 
   * automatically replaced with the default value for this field.</p>
   * 
   * @param isPropertyValue the new value
   */
  public void setPropertyDesc(String isPropertyDesc)
  {
    msPropertyDesc = isPropertyDesc;
    addColumnObject(new ColumnObject(PROPERTYDESC_NAME, msPropertyDesc));
  }
  
  /**
   * Sets Enabled field.
   * 
   * <p><b>Details:</b> This method sets the {@code sPropertyDesc} field to the 
   * given value.  If the provided value is <code>null</code>, it will be 
   * automatically replaced with the default value for this field.</p>
   * 
   * @param inEnabled the new value
   */
  public void setEnabled(int inPropertyEnabled)
  {
    mnEnabled = inPropertyEnabled;
    addColumnObject(new ColumnObject(ENABLED_NAME, mnEnabled));
  }
  
  /**
   * Sets ScreenChangeAllowed field.
   * 
   * <p><b>Details:</b> This method sets the {@code iScreenChangeAllowed} field to the 
   * given value.  If the provided value is <code>null</code>, it will be 
   * automatically replaced with the default value for this field.</p>
   * 
   * @param inScreenChangeAllowed the new value
   */
  public void setScreenChangeAllowed(int inScreenChangeAllowed)
  {
    mnScreenChangeAllowed = inScreenChangeAllowed;
    addColumnObject(new ColumnObject(SCREENCHANGEALLOWED_NAME, mnScreenChangeAllowed));
  }

  /**
   * Sets named field.
   * 
   * <p><b>Details:</b> This method sets the named field to the given value.  
   * The field name is case-insensitive.  If the operation is successful, this 
   * method returns 0.  Otherwise, -1 is returned.</p>
   * 
   * @param isName the field name
   * @param ipValue the field value
   * @return 0 iff successful; -1 otherwise
   */
  /**
   *  Required set field method.  This method figures out what column was
   *  passed to it and sets the value.  This allows us to have a generic
   *  method for all DB interfaces.
   */
  @Override
  public int setField(String isColName, Object ipColValue)
  {
    TableEnum vpEnum = mpColumnMap.get(isColName);
    if (vpEnum == null)
    {
      return(super.setField(isColName, ipColValue));
    }

    switch ((ControllerConfigEnum)vpEnum)
    {
      case CONTROLLER:
        setController((String)ipColValue);
        break;

      case PROPERTYNAME:
        setPropertyName((String)ipColValue);
        break;

      case PROPERTYVALUE:
        setPropertyValue((String)ipColValue);
        break;

      case PROPERTYDESC:
        setPropertyDesc((String)ipColValue);
        break;

      case ENABLED:
        setEnabled((Integer)ipColValue);
        break;

      case SCREENCHANGEALLOWED:
        setScreenChangeAllowed((Integer)ipColValue);
    }

    return(0);
  }
}

