package com.daifukuamerica.wrxj.dbadapter.data;

import static com.daifukuamerica.wrxj.dbadapter.data.RoleOptionEnum.*;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.TableEnum;
import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:<BR>
 *   Class to handle Role Option Data operations.  This class treats columns, and
 *   Keys as Objects.
 *
 * @author       A.T.
 * @version      1.0
 */
public class RoleOptionData extends AbstractSKDCData
{
  public static final String  ADDALLOWED_NAME    = ADDALLOWED.getName();
  public static final String  BUTTONBAR_NAME     = BUTTONBAR.getName();
  public static final String  CATEGORY_NAME      = CATEGORY.getName();
  public static final String  CLASSNAME_NAME     = CLASSNAME.getName();
  public static final String  DELETEALLOWED_NAME = DELETEALLOWED.getName();
  public static final String  ICONNAME_NAME      = ICONNAME.getName();
  public static final String  MODIFYALLOWED_NAME = MODIFYALLOWED.getName();
  public static final String  OPTION_NAME        = OPTION.getName();
  public static final String  ROLE_NAME          = ROLE.getName();
  public static final String  VIEWALLOWED_NAME   = VIEWALLOWED.getName();

         // Private Data
  private String sRole = "";
  private String sCategory = "";
  private String sOption = "";
  private String sIconName = "";
  private String sClassName = "";
  private int iViewAllowed = DBConstants.NO;
  private int iButtonBar = DBConstants.NO;
  private int iAddAllowed = DBConstants.NO;
  private int iModifyAllowed = DBConstants.NO;
  private int iDeleteAllowed = DBConstants.NO;
  private static final Map<String, TableEnum> mpColumnMap = new ConcurrentHashMap<String, TableEnum>();

  public RoleOptionData()
  {
    super();
    initColumnMap(mpColumnMap, RoleOptionEnum.class);
  }


  /**
   * Sets all data values to defaults where appropriate (like translations) and
   * clear out the rest.
   */
  @Override
  public void clear()
  {
    super.clear();                     // Pull in the default behaviour.

    iViewAllowed = iButtonBar = iAddAllowed = DBConstants.NO;
    iModifyAllowed = iDeleteAllowed = DBConstants.NO;
    sRole = sCategory = sOption = sIconName = sClassName = "";
  }

  /**
   * This helps in debugging when we want to print the whole structure.
   */
  @Override
  public String toString()
  {
    String s = "sRole:" + sRole +
               "\nsCategory:" + sCategory +
               "\nsOption:" + sOption +
               "\nsIconName:" + sIconName +
               "\nsClassName:" + sClassName + "\n";
    try
    {
      s = s + "iButtonBar:" + DBTrans.getStringValue(BUTTONBAR_NAME, iButtonBar) +
               "\niAddAllowed:" + DBTrans.getStringValue(ADDALLOWED_NAME, iAddAllowed) +
               "\niModifyAllowed:" + DBTrans.getStringValue(MODIFYALLOWED_NAME, iModifyAllowed) +
               "\niDeleteAllowed:" + DBTrans.getStringValue(DELETEALLOWED_NAME, iDeleteAllowed) + "\n";

    }
    catch(NoSuchFieldException e)
    {
      s = s + "0";
    }
    s += super.toString();

    return(s);
  }

  /**
   * Defines equality between two RoleOptionData objects.
   *
   * @param  absRO <code>AbstractSKDCData</code> reference whose runtime type
   *         is expected to be <code>RoleOptionData</code>
   */
  @Override
  public boolean equals(AbstractSKDCData absRO)
  {
    RoleOptionData rl = (RoleOptionData)absRO;

    return((rl.sRole.equals(sRole)) && (rl.sCategory.equals(sCategory)));
  }

  /*---------------------------------------------------------------------------
                     Column value get methods go here.
  ---------------------------------------------------------------------------*/
  /**
   * Fetches Role
   * @return Role as string
   */
  public String getRole()
  {
    return(sRole);
  }
  /**
   * Fetches Category
   * @return Category as string
   */
  public String getCategory()
  {
    return(sCategory);
  }
  /**
   * Fetches Option
   * @return Option as string
   */
  public String getOption()
  {
    return(sOption);
  }
  /**
   * Fetches Icon Name
   * @return Icon Name as string
   */
  public String getIconName()
  {
    return(sIconName);
  }
  /**
   * Fetches ClassName
   * @return Class Name as string
   */
  public String getClassName()
  {
    return(sClassName);
  }
  /**
   * Fetches ButtonBar
   * @return Button Bar as integer
   */
  public int getButtonBar()
  {
    return(iButtonBar);
  }
  /**
   * Fetches Add Allowed
   * @return Add Allowed as integer
   */
  public int getAddAllowed()
  {
    return(iAddAllowed);
  }
  /**
   * Fetches Modify Allowed
   * @return Modify Allowed as integer
   */
  public int getModifyAllowed()
  {
    return(iModifyAllowed);
  }
  /**
   * Fetches Delete Allowed
   * @return Delete Allowed as integer
   */
  public int getDeleteAllowed()
  {
    return(iDeleteAllowed);
  }
  /**
   * Fetches View Allowed
   * @return View Allowed as integer
   */
  public int getViewAllowed()
  {
    return(iViewAllowed);
  }

  /*---------------------------------------------------------------------------
   ******** Column Setting methods go here. ********
  ---------------------------------------------------------------------------*/
  /**
   * Sets Role value.
   */
  public void setRole(String siRole)
  {
    sRole = checkForNull(siRole);
    addColumnObject(new ColumnObject(ROLE_NAME, siRole));
  }
  /**
   * Sets Category value.
   */
  public void setCategory(String siCategory)
  {
    sCategory = checkForNull(siCategory);
    addColumnObject(new ColumnObject(CATEGORY_NAME, siCategory));
  }
  /**
   * Sets Option value.
   */
  public void setOption(String siOption)
  {
    sOption = checkForNull(siOption);
    addColumnObject(new ColumnObject(OPTION_NAME, siOption));
  }
  /**
   * Sets Icon Name value.
   */
  public void setIconName(String siIconName)
  {
    sIconName = checkForNull(siIconName);
    addColumnObject(new ColumnObject(ICONNAME_NAME, siIconName));
  }
  /**
   * Sets Class Name value.
   */
  public void setClassName(String siClassName)
  {
    sClassName = checkForNull(siClassName);
    addColumnObject(new ColumnObject(CLASSNAME_NAME, siClassName));
  }
  /**
   * Sets Button Bar value.
   */
  public void setButtonBar(int iiButtonBar)
  {
    iButtonBar = iiButtonBar;
    addColumnObject(new ColumnObject(BUTTONBAR_NAME, Integer.valueOf(iiButtonBar)));
  }
  /**
   * Sets Add Allowed value.
   */
  public void setAddAllowed(int iiAddAllowed)
  {
    iAddAllowed = iiAddAllowed;
    addColumnObject(new ColumnObject(ADDALLOWED_NAME, Integer.valueOf(iiAddAllowed)));
  }
  /**
   * Sets Modify Allowed value.
   */
  public void setModifyAllowed(int iiModifyAllowed)
  {
    iModifyAllowed = iiModifyAllowed;
    addColumnObject(new ColumnObject(MODIFYALLOWED_NAME, Integer.valueOf(iiModifyAllowed)));
  }
  /**
   * Sets Delete Allowed value.
   */
  public void setDeleteAllowed(int iiDeleteAllowed)
  {
    iDeleteAllowed = iiDeleteAllowed;
    addColumnObject(new ColumnObject(DELETEALLOWED_NAME, Integer.valueOf(iiDeleteAllowed)));
  }
  /**
   * Sets View Allowed value.
   */
  public void setViewAllowed(int iiViewAllowed)
  {
    iViewAllowed = iiViewAllowed;
    addColumnObject(new ColumnObject(VIEWALLOWED_NAME, Integer.valueOf(iiViewAllowed)));
  }

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
      return super.setField(isColName, ipColValue);
    }

    switch((RoleOptionEnum)vpEnum)
    {
      case ADDALLOWED:
        setAddAllowed((Integer)ipColValue);
        break;

      case BUTTONBAR:
        setButtonBar((Integer)ipColValue);
        break;

      case CATEGORY:
        setCategory((String)ipColValue);
        break;

      case CLASSNAME:
        setClassName((String)ipColValue);
        break;

      case DELETEALLOWED:
        setDeleteAllowed((Integer)ipColValue);
        break;

      case ICONNAME:
        setIconName((String)ipColValue);
        break;

      case MODIFYALLOWED:
        setModifyAllowed((Integer)ipColValue);
        break;

      case OPTION:
        setOption((String)ipColValue);
        break;

      case ROLE:
        setRole((String)ipColValue);
        break;

      case VIEWALLOWED:
        setViewAllowed((Integer)ipColValue);
    }

    return(0);
  }
}
