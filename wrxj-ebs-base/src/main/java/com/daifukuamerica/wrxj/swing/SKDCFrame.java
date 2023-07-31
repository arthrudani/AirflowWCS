package com.daifukuamerica.wrxj.swing;

import java.awt.Point;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * Description:<BR>
 *   Class to center any type of frame.  The first method is mainly
 *   for centering an internal frame.
 *
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 08-Feb-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  SKDC Corporation
 */
public class SKDCFrame
{
  private static int x, y;
  private static int main_width, main_height;

  public static void positionFrame(JDesktopPane desktop_pane,
      JInternalFrame child_frame)
  {
    main_width = desktop_pane.getSize().width;
    main_height = desktop_pane.getSize().height;

    //      centerFrame(desktop_pane, child_frame);
    cascadeNewFrame(desktop_pane, child_frame);
  }

  public static void centerFrame(JDesktopPane desktop_pane,
      JInternalFrame child_frame)
  {
    // Center the internal frame relative
    // to Main Frame.
    x = (main_width - child_frame.getWidth()) / 2;
    y = (main_height - child_frame.getHeight()) / 2;
    child_frame.setLocation(new Point(x, y));

    return;
  }

  private static int lastX = 5;

  public static void cascadeNewFrame(JDesktopPane desktop_pane,
      JInternalFrame child_frame)
  {
    // Cascade the internal frame relative
    // to the last Main Frame.
    x = lastX + 25;
    if (x > main_width / 3 || x > main_height / 4)
    {
      x = 30;
    }
    // Make sure that the frame is completely visible
    while (x > 5
        && (x + child_frame.getWidth() > main_width || x
            + child_frame.getHeight() > (main_height-4))) // the height may change
    {
      x -= 25;
    }
    y = x;
    child_frame.setLocation(x, y);
    lastX = x;
  }

}
