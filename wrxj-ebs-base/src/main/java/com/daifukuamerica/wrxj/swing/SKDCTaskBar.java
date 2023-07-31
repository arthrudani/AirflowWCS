package com.daifukuamerica.wrxj.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

/**
 * Title:        RTS JAVA GUI
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Eskay
 * @author
 * @version 1.0
 */

public class SKDCTaskBar
{
  private static Box gpTaskBar = null;
  private static ButtonGroup taskBarButtons = new ButtonGroup();

  /**
   * Internal class for tasks
   */
  private class TaskBarTask extends JToggleButton
  {
    private static final long serialVersionUID = 0L;

    private SKDCInternalFrame buttonFrame = null;
    private PopupMenu popup;

    public TaskBarTask(SKDCInternalFrame frame)
    {
      super(frame.getTitle(), frame.getFrameIcon());
      buttonFrame = frame;
      setActionCommand(frame.getTitle());
      addActionListener(new SKDCButtonListener());
      Insets margins = getMargin();
      setMargin(new Insets(margins.top, 2, margins.bottom, 2));
      Dimension dim = getPreferredSize();
      setMinimumSize(new Dimension(dim.width/5, dim.height));
      setToolTipText(getText());
      popup = new PopupMenu();
      popup.add("Minimize");
      popup.add("Restore");
      popup.add("Close");

      addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent mEvent)
        {
          showPopup(mEvent);
        }

        @Override
        public void mousePressed(MouseEvent mEvent)
        {
          showPopup(mEvent);
        }

        @Override
        public void mouseReleased(MouseEvent mEvent)
        {
          showPopup(mEvent);
        }
      });
      
      popup.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent ae)
        {
          processPopupEvent(ae);
        }
      });
      
      add(popup);
      taskBarButtons.add(this);
      taskBarButtons.setSelected(getModel(), true);
    }

    void showPopup(MouseEvent mEvent)
    {
      if (mEvent.isPopupTrigger())
      {
        popup.show(this, mEvent.getX(), mEvent.getY());
      }
    }

    private class SKDCButtonListener implements ActionListener
    {
      public void actionPerformed(ActionEvent ae)
      {
        // System.out.println("Task Bar Button: " + ae.getActionCommand());
        taskBarButtonSelected();
      }
    }

    /**
     * Select SKDCInternalFrame from the taskbar.
     */
    void taskBarButtonSelected()
    {
      if (buttonFrame.isIcon())
      {
        try
        {
          buttonFrame.setIcon(false);
        }
        catch (PropertyVetoException pve) {}

        buttonFrame.moveToFront();
      }
      else
      {
        if (buttonFrame.isSelected())
        {
          try
          {
            if (buttonFrame.isIconifiable())
            {
              buttonFrame.setIcon(true);
            }
          }
          catch (PropertyVetoException pve) {}
        }
        else
        {
          buttonFrame.moveToFront();
          try
          {
            buttonFrame.setSelected(true);
          }
          catch (PropertyVetoException pve) {}
        }
      }
    }

    void processPopupEvent(ActionEvent ae)
    {
      if (ae.getActionCommand().equalsIgnoreCase("Minimize"))
      {
        try
        {
          if (buttonFrame.isIconifiable() && !buttonFrame.isIcon())
          {
            buttonFrame.setIcon(true);
          }
        }
        catch (PropertyVetoException pve) {}
      }
      else if (ae.getActionCommand().equalsIgnoreCase("Restore"))
      {
        taskBarButtonSelected();
      }
      else if (ae.getActionCommand().equalsIgnoreCase("Close"))
      {
        try
        {
          buttonFrame.setClosed(true);
        }
        catch (PropertyVetoException pve) {}
      }
    }
    
    void cleanup()
    {
      taskBarButtons.remove(this);
      buttonFrame = null;
    }
  }
  
  /**
   * Get the taskbar.
   *
   * @return JPanel which is the task bar
   */
  public static Box getTaskBar()
  {
    if (gpTaskBar == null)
    {
      gpTaskBar = new Box(BoxLayout.X_AXIS);
      gpTaskBar.setBorder(BorderFactory.createEtchedBorder());
    }
    return gpTaskBar;
  }

  /**
   * Add SKDCInternalFrame to the taskbar.
   *
   * @param frame SKDCInternalFrame to be added
   */
  public void addToTaskBar(SKDCInternalFrame frame)
  {
    TaskBarTask task = new TaskBarTask(frame);
    gpTaskBar.add(task);
  }

  /**
   * Remove SKDCInternalFrame from the taskbar.
   *
   * @param frame SKDCInternalFrame to be removed
   */
  public void removeFromTaskBar(SKDCInternalFrame frame)
  {
    for (int i = 0; i < gpTaskBar.getComponentCount(); i++)
    {
      TaskBarTask vpTaskBarTask = (TaskBarTask)gpTaskBar.getComponent(i);
      if(vpTaskBarTask.buttonFrame.equals(frame))
      {
        vpTaskBarTask.cleanup();
        gpTaskBar.remove(vpTaskBarTask);
        gpTaskBar.repaint();
        break;
      }
    }
  }

  /**
   * Update title / tooltip on the taskbar.
   *
   * @param frame SKDCInternalFrame to be changed
   */
  public String updateTaskBarTaskTitle(SKDCInternalFrame frame, String newTitle)
  {
    String vsResult = null;
    for (int i = 0; i < gpTaskBar.getComponentCount(); i++)
    {
      TaskBarTask vpTaskBarTask = (TaskBarTask)gpTaskBar.getComponent(i);
      if(vpTaskBarTask.buttonFrame.equals(frame))
      {
        ((TaskBarTask)gpTaskBar.getComponent(i)).setText(newTitle);
        ((TaskBarTask)gpTaskBar.getComponent(i)).setToolTipText(newTitle);
        vsResult = vpTaskBarTask.getText();
        break;
      }
    }
    return vsResult;
  }

  /**
   * Select button on the taskbar.
   * 
   * <P>TODO: At some point this method should be changed to compare against
   * objects as the other methods do, instead of comparing against strings. This
   * method can currently lead to incorrect(though harmless) behavior when there
   * are two buttons with the same name.</P>
   * 
   * @param title String containing frames title
   */
  public void selectTaskBarButton(String title)
  {
    for (int i = 0; i < gpTaskBar.getComponentCount(); i++)
    {
      if (((TaskBarTask)gpTaskBar.getComponent(i)).getText().equalsIgnoreCase(title))
      {
        taskBarButtons.setSelected(((JToggleButton)gpTaskBar.getComponent(i)).getModel(), true);
        break;
      }
    }
  }


}