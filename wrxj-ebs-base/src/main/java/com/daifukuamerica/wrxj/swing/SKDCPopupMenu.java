package com.daifukuamerica.wrxj.swing;

import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 * Convenience class to wrap popup menu functionality.
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 29-Nov-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class SKDCPopupMenu extends JPopupMenu
{
  /**
   * Creates a new menu item with translated text and appends
   * it to the end of this menu.
   *  
   * @param s the string for the menu item to be added
   */
  @Override
  public JMenuItem add(String s)
  {
    return add(new JMenuItem(DacTranslator.getTranslation(s)));
  }

  /**
   *  Method to add a menu item and and attach an action listener to it.
   *
   *  @param menuText displayed menu entry.
   *  @param addSeparator <code>boolean</code> indicating if a separator line is
   *         created after this menu entry.
   *  @param actionID Action command string.
   *  @param listener actionListener to be added.
   */
  public JMenuItem add(String menuText, boolean addSeparator, String actionID,
                  ActionListener listener)
  {
    if (actionID == null || actionID.length() == 0)
    {
      JOptionPane.showMessageDialog(null, "Invalid actionID passed.",
          "Event Add Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    JMenuItem menuItem = add(menuText);
    menuItem.setActionCommand(actionID);
    menuItem.addActionListener(listener);
    if (addSeparator)
    {
      addSeparator();
    }
    return menuItem;
  }

 /**
  *  Convenience method for not using menu separators.
  */
  public JMenuItem add(String menuText, String actionID, ActionListener listener)
  {
    return (add(menuText, false, actionID, listener));
  }

 /**
  *  Method to enable or disable menu items.
  *
  *  @param menuItemName <code>String</code> containing menu item name.
  *  @param enableAction <code>boolean</code> to enable or disable menu items.
  */
  public void setAuthorization(String menuItemName, boolean enableAction)
  {
    Component[] menuItems = getComponents();
    for(int mIdx = 0; mIdx < menuItems.length; mIdx++)
    {
      if (menuItems[mIdx] instanceof JMenuItem)
      {
        JMenuItem currMenuItem = (JMenuItem)menuItems[mIdx];
        String compName = currMenuItem.getText();
        if (compName.equalsIgnoreCase(menuItemName))
        {
          currMenuItem.setEnabled(enableAction);
          break;
        }
      }
    }
  }
  
  /**
   *  Method to remove
   *
   *  @param menuItemName <code>String</code> containing menu item name.
   */
  public void remove(String menuItemName)
  {
    Component[] menuItems = getComponents();
    for(int mIdx = 0; mIdx < menuItems.length; mIdx++)
    {
      if (menuItems[mIdx] instanceof JMenuItem)
      {
        JMenuItem currMenuItem = (JMenuItem)menuItems[mIdx];
        String compName = currMenuItem.getText();
        if (compName.equalsIgnoreCase(menuItemName))
        {
          remove(mIdx);
          break;
        }
      }
    }
    
    /*
     * If the first item is now a separator, remove it because it looks stupid.
     * TODO: Someday we may need to expand this to look for double-separators
     * and end-separators 
     */
    menuItems = getComponents();
    if (menuItems.length > 0)
    {
      if (menuItems[0] instanceof Separator)
      {
        remove(menuItems[0]);
      }
    }
  }
}
