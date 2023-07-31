package com.daifukuamerica.wrxj.swing;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.toedter.calendar.JCalendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;

 /**
 *   SK Daifuku Calendar component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */
public class SKDCCalendar extends JCalendar
    implements SKDCGUIConstants
{
  private static final long serialVersionUID = 0L;

  private SimpleDateFormat sdf = new SimpleDateFormat();
  private String dFormat = SKDCConstants.DateFormatString;
  protected boolean timingOn = false;
  TitledBorder titledBorder1;
  JPanel buttonPanel = new JPanel();
  SKDCButton okButton = new SKDCButton();
  SKDCButton cancelButton = new SKDCButton();
  SKDCTimeSpinner timeSpinner;
  private JInternalFrame parentFrame= null;
  private boolean addTime = false;

  /**
  *  Create date text field.
  *
  */
  public SKDCCalendar()
  {
    super();
    setDefaults();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public SKDCCalendar( boolean showTime )
  {
    super();
    addTime = showTime;
    setDefaults();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void setDefaults()
  {
    timingOn = false;
    sdf.applyPattern(dFormat);
    this.addFocusListener(new java.awt.event.FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent fe)
      {
//        System.out.println("TEXTFIELD---- focusGained" + fe.getClass().getName());
        if (timingOn)
        {
          restartTimer();
        }
      }
    });
    this.addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent pe)
      {
        if ((timingOn) && (!pe.getPropertyName().toString().equalsIgnoreCase(FRAME_TIMER_RESTART)))
        {
//          System.out.println("TEXTFIELD Property change: " + pe.getPropertyName().toString());
          restartTimer();
        }
      }
    });
  }

  /**
   *  Method to get value as a date with time.
   *
   *  @return Date containing value from the spinner component.
   */
   public Date getDateTime()
   {
     Calendar c = this.getCalendar();
     if (addTime)
     {
       c.set(Calendar.HOUR_OF_DAY, timeSpinner.getCalendar().get(Calendar.HOUR_OF_DAY));
       c.set(Calendar.MINUTE, timeSpinner.getCalendar().get(Calendar.MINUTE));
       c.set(Calendar.SECOND, timeSpinner.getCalendar().get(Calendar.SECOND));
     }
     return(c.getTime());
   }

  /**
   *  Method to get value as a date only.
   *
   *  @return Date containing value from the spinner component.
   */
   public Date getDateOnly()
   {
     Date retDate = null;
     Calendar cal = (Calendar)this.getCalendar().clone();
     if (cal != null)
     {
       cal.set(Calendar.HOUR_OF_DAY,0);
       cal.set(Calendar.MINUTE,0);
       cal.set(Calendar.SECOND,0);
       retDate = cal.getTime();
     }
     return(retDate);
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
  //    System.out.println("Timer restarted SKDCDateTimeSpinner");
       firePropertyChange(FRAME_TIMER_RESTART,"Old","New");
   }

  /**
   * Returns "SKDC Calendar".
   */
  @Override
  public String getName()
  {
      return "SKDC Calendar";
  }

  /**
   * Sets the root focus container and adds internal frame listener.
   */
  public void setParentFrame()
  {
    parentFrame = (JInternalFrame)this.getFocusCycleRootAncestor();
    parentFrame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
    {
      @Override
      public void internalFrameClosing(InternalFrameEvent ife)
      {
        button_pushed("CANCEL");
      }
      @Override
      public void internalFrameIconified(InternalFrameEvent ife)
      {
        button_pushed("CANCEL");
      }
    });
    parentFrame.addComponentListener(new java.awt.event.ComponentAdapter()
    {
      @Override
      public void componentMoved(java.awt.event.ComponentEvent e)
      {
        button_pushed("CANCEL");
      }
    });
    return;
  }

  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(103, 101, 98),new Color(148, 145, 140)),"Pick a Date");
    this.setBorder(titledBorder1);
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        button_pushed("OK");
      }
    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        button_pushed("CANCEL");
      }
    });
    if (addTime)
    {
      timeSpinner = new SKDCTimeSpinner();
      buttonPanel.add(timeSpinner, null);
    }
    buttonPanel.setBorder(BorderFactory.createEtchedBorder());
    this.add(buttonPanel,  BorderLayout.SOUTH);
    buttonPanel.add(okButton, null);
    buttonPanel.add(cancelButton, null);
  }

 /**
  *  Action method to handle Cancel button.
  *
  *  @param button
  */
  void button_pushed(String button)
  {
    firePropertyChange(FRAME_CLOSING,null,button);
  }

  /**
   * Sets the calendar property.
   * This is a bound property.
   *
   * @see #getCalendar
   * @param c the new calendar
   */
  @Override
  public void setCalendar(Calendar c)
  {
    super.setCalendar(c);
    if (addTime)
    {
      timeSpinner.setDate(c.getTime());
    }
  }

  /**
   * Extra stuff to fix tabbing when the calendar in not enabled
   * 
   * @param izEnabled 
   */
  @Override
  public void setEnabled(boolean izEnabled)
  {
    super.setEnabled(izEnabled);
    okButton.setEnabled(izEnabled);
    cancelButton.setEnabled(izEnabled);
  }
}
