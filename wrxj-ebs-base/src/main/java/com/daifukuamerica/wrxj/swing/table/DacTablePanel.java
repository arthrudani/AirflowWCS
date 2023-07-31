package com.daifukuamerica.wrxj.swing.table;

import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 *  Class to attach a mouse listener to a component that resides in a
 *  panel (JPanel).
 *
 * @author gmuhlest
   * @version      1.0
   * <BR>Created: 02-Apr-04<BR>
   *     Copyright (c) 2004<BR>
   *     Company:  Daifuku America Corporation
 *
 */
public class DacTablePanel extends JPanel
{
  private static final long serialVersionUID = 0L;

  DacTable     skTable     = null;
  SKDCPopupMenu skPopupMenu = null;
  private MouseListener[] mouseListeners = null;

  public DacTablePanel()
  {
    super();
  }

  public DacTablePanel(boolean isDoubleBuffered)
  {
    super(isDoubleBuffered);
  }

  public DacTablePanel(LayoutManager layout)
  {
    super(layout);
  }

  public DacTablePanel(LayoutManager layout, boolean isDoubleBuffered)
  {
    super(layout, isDoubleBuffered);
  }

  public void addMouseEvent(DacTable ipTable, SKDCPopupMenu ipPopupMenu) //public so popups can be attached to panels
  {
    skTable = ipTable;
    skPopupMenu = ipPopupMenu;
    ipTable.addMouseListener(new tableMouseListener());
  }

 /**
  *  Method to enable / disable the SKDCTable. Method also adds and removes
  *  mouseListeners as needed.
  *
  *  @param enabled True if enabled, false if not.
  */
  public void setTableEnabled(boolean enabled)
  {
    if (skTable != null)
    {
      skTable.setEnabled(enabled);
      this.setEnabled(enabled);
      if (enabled)
      {
        if (mouseListeners != null)
        {
          for (int i = 0; i < mouseListeners.length; i++)
          {
            skTable.addMouseListener(mouseListeners[i]);
          }
          mouseListeners = null;
        }
/**************************
        else
        {
          System.out.println("No mouse listeners to reset ????????????");
        }
**************************/
      }
      else
      {
        mouseListeners = skTable.getMouseListeners();
        for (int i = 0; i < mouseListeners.length; i++)
        {
          skTable.removeMouseListener(mouseListeners[i]);
        }
      }

    }
  }

//the frame's gonna have to take care of this  public abstract void displayDetail(); //what to do when double-clicking table

 /**
  *  Inner class to handle table based mouse events.  In order for the code
  *  to be portable between Unix and Windows for popup menus, three mouse event
  *  methods had to be overridden: <code>mousePressed</code>,
  *  <code>mouseReleased</code> and <code>mouseClicked</code>.
  *
  * @author       A.D.
  * @version      1.0
  * <BR>Created: 21-Oct-02<BR>
  *     Copyright (c) 2002<BR>
  *     Company:  Daifuku America Corporation
  */
  private class tableMouseListener extends MouseAdapter
  {
    @Override
    public void mouseClicked(MouseEvent mEvent)
    {
      switch(mEvent.getClickCount())
      {
        case 1:
          showPopup(mEvent);
          break;

        /*case 2:
          if (skTable.columnAtPoint(mEvent.getPoint()) != -1)
          {
            displayDetail();
          }*/
      }
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

    private void showPopup(MouseEvent mEvent)
    {
      if (skTable.columnAtPoint(mEvent.getPoint()) != -1)
      {
        if (mEvent.isPopupTrigger())
        {
          skPopupMenu.show(skTable, mEvent.getX(), mEvent.getY());
        }
      }
    }
  }
}

