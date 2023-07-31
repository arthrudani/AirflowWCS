package com.daifukuamerica.wrxj.swing;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

 /**
 *   SK Daifuku Toggle button component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */
public class SKDCToggleButton extends JToggleButton
{
  private static final long serialVersionUID = 0L;

 /**
  *  Create toggle button.
  *
  */
  public SKDCToggleButton()
  {
    super();
  }

 /**
  *  Create toggle button.
  *
  * @param btnlabel Contains label for the button.
  */
  public SKDCToggleButton(String btnlabel)
  {
      this();
      this.setText(btnlabel);
  }

 /**
  *  Create toggle button.
  *
  * @param btnlabel Contains label for the button.
  * @param helptext Contains tool tip text for the button.
  */
  public SKDCToggleButton(String btnlabel, String helptext)
  {
      this(btnlabel);
      this.setToolTipText(helptext);
  }

 /**
  *  Create toggle button.
  *
  * @param btnlabel Contains label for the button.
  * @param helptext Contains tool tip text for the button.
  * @param hotkey Character to be used as menu hotkey.
  */
  public SKDCToggleButton(String btnlabel, String helptext, char hotkey)
  {
      this(btnlabel, helptext);
      this.setMnemonic(hotkey);
  }

 /**
  *  Method to delete a load line item.
  *
  *  @param actionID Action command.
  *  @param listener Action listener to add to button.
  */
  public void addEvent(String actionID, ActionListener listener)
  {
      if (actionID == null || actionID.length() == 0)
      {
          JOptionPane.showMessageDialog(null, "Invalid actionID passed.",
                                        "Event Add Error",
                                        JOptionPane.ERROR_MESSAGE);
          return;
      }

      this.setActionCommand(actionID);
      this.addActionListener(listener);
  }

}
