package com.daifukuamerica.wrxj.swing;

import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

 /**
 *   SK Daifuku Time spinner component for SKDCCalendar.
 *
 * @author       A.T.
 * @version      1.0
 */

public class SKDCTimeSpinner extends JSpinner
        implements SKDCGUIConstants
{
  private static final long serialVersionUID = 0L;

  private SimpleDateFormat sdf = new SimpleDateFormat();
  private String timeFormat = "HH:mm:ss";
  private SpinnerDateModel model;
  private Calendar cal = Calendar.getInstance();
  private JSpinner.DateEditor editor;
  protected boolean timingOn = false;

 /**
  *  Create default date time spinner.
  *
  */
  public SKDCTimeSpinner()
  {
    super();
    timingOn = false;
    model = new SpinnerDateModel();
    model.setCalendarField(Calendar.MINUTE);

    this.setModel(model);
    sdf.applyPattern(timeFormat);

    editor = new JSpinner.DateEditor(this, timeFormat);

    sdf.applyPattern(timeFormat);
    this.editor.getTextField().addFocusListener(new java.awt.event.FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent fe)
      {
        if (timingOn)
        {
          restartTimer();
        }
      }
    });
    this.editor.getTextField().addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent pe)
      {
        if ((timingOn) && (!pe.getPropertyName().toString().equalsIgnoreCase(FRAME_TIMER_RESTART)))
        {
          restartTimer();
        }
      }
    });

    this.setEditor(editor);
    sdf.applyPattern(timeFormat);
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
      firePropertyChange(FRAME_TIMER_RESTART,"Old","New");
  }

 /**
  *  Method to set the date.
  *
  *  @param date value to set spinner component to.
  */
  public void setDate(Date date)
  {
    if (date != null)
    {
      this.setValue(date);
    }
  }

 /**
  *  Method to get value as a date with time.
  *
  *  @return Date containing value from the spinner component.
  */
  public Calendar getCalendar()
  {
    Date retDate;
    try
    {
      this.commitEdit();
    }
    catch (ParseException pe){}

    retDate = model.getDate();
    if (retDate != null)
    {
      cal.setTime(retDate);
    }
    return(cal);
  }

 /**
  *  Method to reset spinner to current date.
  *
  */
  public void reset()
  {
    this.setValue(new Date());
  }


}