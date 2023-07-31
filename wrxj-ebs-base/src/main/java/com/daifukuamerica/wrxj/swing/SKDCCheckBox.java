package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import javax.swing.JCheckBox;

 /**
 *   SK Daifuku check box component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */

public class SKDCCheckBox extends JCheckBox
        implements SKDCGUIConstants
{
  private static final long serialVersionUID = 0L;

  protected boolean timingOn = false;

 /**
  *  Create check box.
  *
  */
  public SKDCCheckBox()
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

 /**
  *  Create check box with default Text.
  *
  */
  public SKDCCheckBox(String boxText)
  {
    super(boxText);

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
//    System.out.println("Timer restarted SKDCCheckBox");
      firePropertyChange(FRAME_TIMER_RESTART,"Old","New");
  }

  /**
   * Returns selection info as YES/NO instead of true/false
   * @return DBConstants.YES if selected, DBConstants.NO otherwise
   */
  public int isSelectedYesNo()
  {
    return isSelected() ? DBConstants.YES : DBConstants.NO;
  }
  
  public void setSelected(int inYesNo)
  {
    setSelected(inYesNo == DBConstants.YES);
  }
}