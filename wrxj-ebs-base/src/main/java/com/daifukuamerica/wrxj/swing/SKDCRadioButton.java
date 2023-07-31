package com.daifukuamerica.wrxj.swing;
/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

 /**
 *   SK Daifuku Radio button component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */

public class SKDCRadioButton extends JRadioButton
        implements SKDCGUIConstants
{
  private static final long serialVersionUID = 0L;

  protected boolean timingOn = false;

 /**
  *  Create radio button.
  *
  */
  public SKDCRadioButton()
  {
    super();

    this.addFocusListener(new java.awt.event.FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent fe)
      {
//        System.out.println("---- focusGained" + fe.getClass().getName());
        if (timingOn)
        {
          restartTimer();
        }
      }
    });
    this.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (timingOn)
        {
          restartTimer();
        }
      }
    });
  }

  public SKDCRadioButton(String labelText, char mnemonic)
  {
    this();
    setText(labelText);
    setMnemonic(mnemonic);
  }

  public SKDCRadioButton(String labelText, char mnemonic, boolean selectButton)
  {
    this(labelText, mnemonic);
    setSelected(selectButton);
  }

  public void eventListener(String actionID, ActionListener listener)
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

 /**
  *  Method to enable / disable the component time out.
  *
  *  @param on Enable value.
  */
  public void enableTimer(boolean on)
  {
    timingOn = on;
  }

 /**
  *  Method to restart the component time out.
  *
  */
  public void restartTimer ()
  {
//    System.out.println("Timer restarted SKDCRadioButton");
      firePropertyChange(FRAME_TIMER_RESTART,"Old","New");
  }

}