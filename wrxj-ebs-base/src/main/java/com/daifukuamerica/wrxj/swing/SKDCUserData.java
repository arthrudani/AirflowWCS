package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOptionData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A class that provides methods used to login and out of the GUI. It also
 * provides methods used to keep track of a users permissions.
 *
 * @author  A.T.
 * @version 1.0
 */
public class SKDCUserData
{
  protected static InheritableThreadLocal<String> loginName =
      new InheritableThreadLocal<String>();
  private static InheritableThreadLocal<String> role =
      new InheritableThreadLocal<String>();
  private static InheritableThreadLocal<String> mpMachineName =
    new InheritableThreadLocal<String>();
  private static InheritableThreadLocal<String> ipAddress =
    new InheritableThreadLocal<String>();
  private static InheritableThreadLocal<Map<String,Map<String,OptionInfo>>> optionList =
      new InheritableThreadLocal<Map<String,Map<String,OptionInfo>>>();
  private static InheritableThreadLocal<Map<String,String>> iconList = 
      new InheritableThreadLocal<Map<String,String>>();
  private static InheritableThreadLocal<String> superUser =
      new InheritableThreadLocal<String>();
  private static String msLanguage = "";

  protected StandardUserServer userServ;
  private Map<String,Map<String,OptionInfo>> categories;
  private Map<String,OptionInfo> optList;
  private Map<String,String> icons;
  private SKDCScreenPermissions epNone = new SKDCScreenPermissions();

  private class OptionInfo
  {
    String icon = "";
    String classname = "";
    boolean buttonBar = false;
    SKDCScreenPermissions ep = new SKDCScreenPermissions(); // default

    public boolean getButtonBar()
    {
      return buttonBar;
    }

    public String getIcon()
    {
      return icon;
    }

    public String getClassname()
    {
      return classname;
    }

  }

  public SKDCUserData()
  {
    userServ = Factory.create(StandardUserServer.class);
    epNone.sCategory = "";
    epNone.sOption = "";
    epNone.iAddAllowed = false;
    epNone.iModifyAllowed = false;
    epNone.iDeleteAllowed = false;
  }

 /**
  *  Method to determine if user is logged in.
  *
  *  @return boolean of <code>true</code> if user is logged in.
  */
  public static boolean isLoggedIn()
  {
    boolean loggedIn = true;

    if (loginName.get() == null)
    {
      loggedIn = false;
    }
    return (loggedIn);
  }

  /**
   *  Method to determine if user is a super user.
   *
   *  @return boolean of <code>true</code> if user is a super user.
   */
  public static boolean isSuperUser()
  {
    boolean rtnval = false;

    if (superUser.get() != null)
    {
      rtnval = true;
    }
    return (rtnval);
  }

  /**
   *  Method to determine if user is a an Administrator.
   *
   *  @return boolean of <code>true</code> if user is an Administrator.
   */
  public static boolean isAdministrator()
  {
    return getRole().equals(SKDCConstants.ROLE_ADMINISTRATOR);
  }

  /**
   *  Method to login a user. Gets the users role, menu categories and options,
   *  screen permissions and determines if the user is a super user.
   *
   *  @param newLoginName User name to logged in.
   *  @param newDevice - device user is loggin in on.
   */
  public void login(String newLoginName, String isMachineName,
      String isIPAddress)
  {
    if (newLoginName.length() > 0)
    {
      loginName.set(newLoginName);
      mpMachineName.set(isMachineName);
      ipAddress.set(isIPAddress);

      try
      {
        RoleOptionData rop = Factory.create(RoleOptionData.class);
        categories = new TreeMap<String,Map<String,OptionInfo>>();
        icons = new TreeMap<String,String>();
        role.set(userServ.getEmployeeRole(newLoginName));
        // get the categories / options for this role
        List<Map> roleOptionList = userServ.getRoleOptionsList(getRole());
        for (int x = 0; x < roleOptionList.size(); x++)
        {
          rop.dataToSKDCData(roleOptionList.get(x));

          if(rop.getViewAllowed() == DBConstants.NO)
          {
            continue;
          }
          // check if this category already in list

          if (!categories.containsKey(rop.getCategory()))
          {
            // if not, add it and create new option list map
//            System.out.println("New Category: " + (String)rop.get("sCategory").toString());
            optList = new TreeMap<String,OptionInfo>();
            categories.put(rop.getCategory(),optList);
          }
          OptionInfo oi = new OptionInfo();
          oi.icon = rop.getIconName().toString();
          oi.classname = rop.getClassName().toString();
          oi.buttonBar = (rop.getButtonBar() == DBConstants.YES);
          SKDCScreenPermissions ep = new SKDCScreenPermissions();

          ep.sCategory = rop.getCategory().toString();
          ep.sOption = rop.getOption().toString();
          ep.iAddAllowed = (rop.getAddAllowed() == DBConstants.YES);
          ep.iModifyAllowed = (rop.getModifyAllowed() == DBConstants.YES);
          ep.iDeleteAllowed = (rop.getDeleteAllowed() == DBConstants.YES);
          oi.ep = ep;

//          System.out.println("New Option: " + (String)rop.get("sOption").toString());
          optList.put(rop.getOption().toString(),oi);
          
          // Make sure we have a fully qualified class name
          String vsIconClass = oi.classname;
          if (! vsIconClass.startsWith("com."))
          {
            vsIconClass = SKDCGUIConstants.SWINGUI_BASE_PACKAGE + vsIconClass;
          }
          icons.put(vsIconClass, oi.icon);
        }
///        optionList.set(optList);
        optionList.set(categories);
        iconList.set(icons);
        superUser.set(null);
        if (getRole().equals(SKDCConstants.ROLE_DAC_SUPERROLE))
        {
          superUser.set("SUPERUSER");
        }
        
        // Get the Language
        EmployeeData vpED = userServ.getEmployeeData(newLoginName);
        msLanguage = vpED.getLanguage();
      }
      catch (DBException e)
      {
        System.out.println("Data base problem: " + e.getMessage());
      }
//    System.out.println("New Login: " + loginName.get() + " New Device:" + loginDevice.get());
    }
  }

  /**
   *  Method to set user data
   * 
   * @param isUser - User name 
   */
  public static void setLoginName(String isUser)
  {
    loginName.set(isUser);
  }

  /**
   *  Method to determine if a option is enabled for this user.
   *
   *  @param category Menu category for the option.
   *  @param option Menu option to check.
   *  @return boolean of <code>true</code> if option is enabled.
   */
  public boolean optionEnabled(String category, String option)
  {
    boolean enabled = false;  // default
    if ((categories = optionList.get()) != null)
    {
      optList = categories.get(category);
      if ((optList != null) && optList.containsKey(option))
      {
        enabled = true;
      }
    }
    return (enabled);
  }

  /**
   *  Method to determine if this option is to appear on the button bar.
   *
   *  @param category Menu category for the option.
   *  @param option Menu option to check.
   *  @return boolean of <code>true</code> if option is on button bar.
   */
  public boolean onButtonBar(String category, String option)
  {
    boolean enabled = false;  // default
    if ((categories = optionList.get()) != null)
    {
      if ((optList = categories.get(category)) != null)
      {
        OptionInfo oi = optList.get(option);
        enabled = oi.getButtonBar();
      }
    }
    return (enabled);
  }

  /**
   *  Method to get the icon used for this option.
   *
   *  @param category Menu category for the option.
   *  @param option Menu option to check.
   *  @return String containing the icon.
   */
  public String getIcon(String category, String option)
  {
    if ((categories = optionList.get()) != null)
    {
      if ((optList = categories.get(category)) != null)
      {
        OptionInfo oi = optList.get(option);
        return oi.getIcon();
      }
    }
    return null;
  }
   
  /**
   *  Method to get the icon for an internal frame.
   *
   *  @param className class name of internal frame.
   *  @return String containing the icon.
   */
  public String getIcon(String className)
  {
    icons = iconList.get();
    String vpIcon = null;
    if (icons != null)
    {
      vpIcon = icons.get(className);
    }
    return vpIcon;
  }

  /**
   *  Method to get the class used for this screen option.
   *
   *  @param category Menu category for the option.
   *  @param option Menu option to check.
   *  @return String containing the class to be used for this option.
   */
  public String getScreenClassName(String category, String option)
  {
    if ((categories = optionList.get()) != null)
    {
      if ((optList = categories.get(category)) != null)
      {
        OptionInfo oi = optList.get(option);
        return oi.getClassname();
      }
    }
    return null;
  }

  /**
   *  Method to get the permissions allowed for this option.
   *
   *  @param category Menu category for the option.
   *  @param option Menu option to check.
   *  @return SKDCScreenPermissions Class containing the users permissions.
   */
  public SKDCScreenPermissions getOptionPermissions(String category, String option)
  {
    categories = optionList.get();
    optList = categories.get(category);
    if (optList == null)
    {
       System.out.println("Permissions requested for unknown category: " +
         category.toString() + ", option: " + option.toString());
       return(epNone);
    }
    OptionInfo oi = optList.get(option);
    if (oi == null)
    {
       System.out.println("Permissions requested for unknown category: " +
         category.toString() + ", option: " + option.toString());
       return(epNone);
    }
    SKDCScreenPermissions ep = oi.ep;

    return (ep);
  }

  /**
   *  Method to get the permissions allowed for this option.
   *
   *  @param optionName String containing both the category and the option.
   *  @return SKDCScreenPermissions Class containing the users permissions.
   */
  public SKDCScreenPermissions getOptionPermissions(String optionName)
  {
    return getOptionPermissions(optionName.substring(0,optionName.indexOf(":")),
      optionName.substring(optionName.indexOf(":")+1,optionName.length()));
  }

  /**
   *  Method to get the permissions allowed for this option.
   *
   *  @param callingClass Menu option to check.
   *  @return SKDCScreenPermissions Class containing the users permissions.
   */
  public SKDCScreenPermissions getOptionPermissionsByClass(Class callingClass)
  {
    String fullName;
   // String shortName;

    fullName = callingClass.getName();

    categories = optionList.get();
    if (categories == null)
    {
      /*
       * If this is true, then something BAD happened. Tell the user to log out,
       * because this session is dead.
       */
      throw new NullPointerException("\nError checking login information.  Please log out.");
    }
    categories.keySet().toString();
    Object[] categorylist = categories.keySet().toArray();
    for(int y = 0; y < categorylist.length; y++)
    {
      optList = categories.get(categorylist[y]);
      Object[] optionslist = optList.keySet().toArray();


      for(int x = 0; x < optionslist.length; x++)
      {

        OptionInfo oi = optList.get(optionslist[x]);

          if (oi == null)
          {
             System.out.println("Error Reading Option Info: null");
             return(epNone);
          }

          if(fullName.endsWith(oi.getClassname()))
          {
              SKDCScreenPermissions ep = oi.ep;
              return ep;
          }
//        }
      }
    }
    return(epNone);
  }

  /**
   *  Method to logout a user.
   */
  public void logout()
  {
    if (loginName.get() != null)
    {
      loginName.set(null);
      role.set(null);
      
      if (optList != null)
      {
        optList.clear();
      }
      if (icons != null)
      {
        icons.clear();
      }
      optionList.set(null);
      superUser.set(null);
    }
  }

  /**
   *  Method to get users login name.
   *
   *  @return String containing the users login name
   */
  public static String getLoginName()
  {
    if (loginName.get() == null)
    {

        String vsLogin = Application.getString("DefaultSystemUser");
        if( vsLogin == null )
        {
        	return "";
        }
        else
        {
            return vsLogin;
        }
    }
    else
    {
      return (loginName.get());
    }
  }
  
  /**
   *  Method to get users role name.
   *
   *  @return String containing the users role name
   */
  public static String getRole()
  {
    if (role.get() == null)
    {
      return ("");
    }
    else
    {
      return (role.get());
    }
  }

  /**
   *  Method to get user's language.
   *
   *  @return int containing the user's language
   */
  public static String getLanguage()
  {
    return msLanguage;
  }

  /**
   *  Method to get user's Machine Name.
   *
   *  @return String containing the user's Machine Name
   */
  public static String getMachineName()
  {
    return mpMachineName.get() == null ? "" : mpMachineName.get();
  }

  /**
   *  Method to get user's Machine Name.
   *
   *  @return String containing the user's Machine Name
   */
  public static String getIPAddress()
  {
    return ipAddress.get() == null ? "" : ipAddress.get();
  }

  /**
   *  Method to get the users menu options for a menu category.
   *
   *  @param category Menu category.
   *  @return TreeSet containing menus options.
   */
  public TreeSet<String> getCategoryOptions(String category)
  {
    TreeSet<String> options = new TreeSet<String>();
    categories = optionList.get();
    optList = categories.get(category);

    for(Iterator<String> it = optList.keySet().iterator(); it.hasNext();)
    {
      options.add(it.next());
    }
    return(options);
  }

  /**
   *  Method to get the users menu categories.
   *
   *  @return TreeSet containing menu categories.
   */
  public TreeSet<String> getCategories()
  {
    TreeSet<String> cats = new TreeSet<String>();
    categories = optionList.get();

    for(Iterator<String> it = categories.keySet().iterator(); it.hasNext();)
    {
      cats.add(it.next());
    }
    return(cats);
  }
}
