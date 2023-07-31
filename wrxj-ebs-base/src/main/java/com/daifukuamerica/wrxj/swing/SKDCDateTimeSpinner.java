package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

 /**
 *   SK Daifuku Date Time spinner component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */

public class SKDCDateTimeSpinner extends JSpinner
        implements SKDCGUIConstants
{
  private static final long serialVersionUID = 0L;
  
  private SimpleDateFormat sdf = new SimpleDateFormat();
  private String dFormat = SKDCConstants.DateFormatString;
  private SpinnerDateModel model;
  private Calendar cal = Calendar.getInstance();
  private JSpinner.DateEditor editor;
  protected boolean timingOn = false;

 /**
  *  Create default date time spinner.
  *
  */
  public SKDCDateTimeSpinner()
  {
    super();
    timingOn = false;
    this.showDateOnly(false);
    sdf.applyPattern("EEEE MMM dd HH:mm:ss zzzz yyyy");
  }

 /**
  *  Method to set whether date and time is shown or just date.
  *
  *  @param dateOnly True if only date is to be displayed.
  */
  public void showDateOnly(boolean dateOnly)
  {
    model = new SpinnerDateModel();
    if (dateOnly)
    {
      model.setCalendarField(Calendar.WEEK_OF_MONTH);
    }
    else
    {
      model.setCalendarField(Calendar.MINUTE);
    }

    this.setModel(model);
    sdf.applyPattern(dFormat);

    if (dateOnly)
    {
      String s = "";
      if (dFormat.startsWith("yyyy"))
        s = dFormat.substring(0,dFormat.indexOf(' '));
      else if (dFormat.startsWith("HH"))
        s = dFormat.substring(dFormat.indexOf("MM"),dFormat.length());

      editor = new JSpinner.DateEditor(this, s);
    }
    else
    {
      editor = new JSpinner.DateEditor(this, dFormat);
    }

    sdf.applyPattern("EEEE MMM dd HH:mm:ss zzzz yyyy");
    this.editor.getTextField().addFocusListener(new java.awt.event.FocusAdapter()
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
    this.editor.getTextField().addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent pe)
      {
        if ((timingOn) && (!pe.getPropertyName().toString().equalsIgnoreCase(FRAME_TIMER_RESTART)))
        {
//          System.out.println("TEXTFIELD Property change: " + pe.getPropertyName().toString());
          restartTimer();
        }
      }
    });

    this.setEditor(editor);
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
  public Date getDateTime()
  {
    Date retDate;
    try
    {
      this.commitEdit();
    }
    catch (ParseException pe){}

    retDate = model.getDate();
    return(retDate);
  }

 /**
  *  Method to get value as a date only.
  *
  *  @return Date containing value from the spinner component.
  */
  public Date getDateOnly()
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
//      if (cal.get(cal.YEAR) > 9999) cal.set(cal.YEAR,9999);
      cal.set(Calendar.HOUR_OF_DAY,0);
      cal.set(Calendar.MINUTE,0);
      cal.set(Calendar.SECOND,0);
      retDate = cal.getTime();
    }
    return(retDate);
  }

 /**
  *  Method to reset spinner to current date.
  *
  */
  public void reset()
  {
    this.setValue(new Date());
  }

 /**
  *  Method to reset spinner to current date plus an offset amount.
  *
  *  @param field Specifies which part of date is to be changed
  *   (i.e. Month, day, hour, etc.).
  *  @param amount Amount to be changed.
  */
  public void reset(int field, int amount)
  {
    cal.setTime(new Date());
    cal.add(field, amount);

    this.setValue(cal.getTime());
  }

}