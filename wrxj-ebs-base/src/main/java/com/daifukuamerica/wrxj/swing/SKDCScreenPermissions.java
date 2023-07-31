package com.daifukuamerica.wrxj.swing;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

/**
 * A data class that contains users permissions.
 *
 * @author  A.T.
 * @version 1.0
 */
public class SKDCScreenPermissions
{
  public String sCategory = "";
  public String sOption = "";
  public boolean iAddAllowed = false; // default
  public boolean iModifyAllowed = false; // default
  public boolean iDeleteAllowed = false; // default

  public String getOption()
  {
    return sOption;
  }

  public String getCategory()
  {
      return sCategory;
  }
}

