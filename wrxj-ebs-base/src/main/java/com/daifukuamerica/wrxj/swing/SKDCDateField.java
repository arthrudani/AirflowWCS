package com.daifukuamerica.wrxj.swing;

/**
 * Title:        Java RTS
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SK Daifuku Corp.
 */

import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JInternalFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;

/**
 *   SK Daifuku Date field component for screens.
 *
 * @author       A.T.
 * @version      1.0
 */
@SuppressWarnings("serial")
public class SKDCDateField extends SKDCTextField
{
  private SimpleDateFormat sdf = new SimpleDateFormat();
  private String dFormat;
  private Date date = new Date();
  private SKDCCalendar cal = null;
  private PopupFactory factory = null;
  private Popup popup = null;
  private Dimension screenSize;
  private JInternalFrame parentFrame = null;

  /**
   *  Create date text field.
   */
  public SKDCDateField()
  {
    this(null, false);
  }

  /**
   *  Create date text field.
   */
  public SKDCDateField(boolean izDateOnly)
  {
    this(null, izDateOnly);
  }

  /**
   *  Create date text field.
   */
  public SKDCDateField(Date newDate, boolean dateOnly)
  {
    if (newDate != null)
    {
      date = newDate;
    }

    toolkit = Toolkit.getDefaultToolkit();
    screenSize  = toolkit.getScreenSize();
    
    setEditable(false);
    dFormat = SKDCConstants.DateFormatString;
    setBackground(Color.WHITE);
    addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          showCalendar();
        }
      });
    addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyPressed(KeyEvent e)
        {
          if (e.getKeyCode() == KeyEvent.VK_ENTER)
          {
            showCalendar();
          }
        }
      });

    if (dateOnly)
    {
      String s = "";
      if (dFormat.startsWith("yyyy"))
        s = dFormat.substring(0,dFormat.indexOf(' '));
      else if (dFormat.startsWith("HH"))
        s = dFormat.substring(dFormat.indexOf("MM"),dFormat.length());
      dFormat = s;
    }
    setColumns(dFormat.length());
    sdf.applyPattern(dFormat);
    setText(sdf.format(date));
    cal = new SKDCCalendar(!dateOnly);
    add(cal);
    cal.addPropertyChangeListener(
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
//          System.out.println("Property change: " + prop);
            if (prop.equals(FRAME_CLOSING))
            {
              if (e.getNewValue().toString().equalsIgnoreCase("OK"))
              {
                saveDate();
              }
              hideCalendar();
            }
          }
        });
    hideCalendar();
    factory = PopupFactory.getSharedInstance();
  }

  void showCalendar()
  {
    /*
     * Enable the calendar when it becomes visible
     */
    cal.setEnabled(true);

    if (isEnabled())
    {
      //    System.out.println("DateField:" + getFocusCycleRootAncestor().getClass().getName());
      if (parentFrame == null)
      {
        parentFrame = (JInternalFrame)getFocusCycleRootAncestor();
        cal.setParentFrame();
      }
      if (popup == null)
      {
        Point p = getLocationOnScreen();
        Calendar currentDate = cal.getCalendar();
        currentDate.setTime(date);
        cal.setCalendar(currentDate);

        if( ( p.y + cal.getHeight() + getHeight() ) < screenSize.height)
        {
          // will fit below input point
          popup = factory.getPopup( getParent(), cal,
              p.x, p.y + getHeight());
        } else
        {
          // need to fit it above input point
          popup = factory.getPopup( getParent(), cal,
              p.x, p.y - cal.getHeight());
        }
        //      System.out.println("DateField:" + getParent().toString());
        popup.show();
      }
      else
      {
        hideCalendar();
      }
    }
  }

  void hideCalendar()
  {
    /*
     * Don't tab through the calendar fields when it is not visible
     */
    cal.setEnabled(false);

    if (popup != null)
    {
      popup.hide();
      popup = null;
    }
  }

  void saveDate()
  {
    setDate(cal.getDateTime());
  }

  /**
   *  Method to get the field value.
   *
   *  @return Date containing current value from field
   */
  public Date getDate()
  {
    return(date);
  }

  /**
   *  Method to set Date field current date.
   */
  public void setDate()
  {
    setDate(new Date());
  }

  /**
   *  Method to set date field to new value.
   *
   *  @param newDate contains date to use in field.
   */
  public void setDate(Date newDate)
  {
    if (newDate != null)
    {
      date = new Date(newDate.getTime());
      setText(sdf.format(date));
      fireActionPerformed();
    }
  }

}
