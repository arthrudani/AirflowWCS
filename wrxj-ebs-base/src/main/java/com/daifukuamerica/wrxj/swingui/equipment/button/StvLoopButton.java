package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Map;

/**
 * <B>Description:</B> This is a class designed to display the STV rail.
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class StvLoopButton extends PolygonButton
{
  int mnExtX, mnExtY, mnExtWidth, mnExtHeight, mnExtArc;
  int mnIntX, mnIntY, mnIntWidth, mnIntHeight, mnIntArc;
  
  /**
   * Public constructor for Factory
   * 
   * @param ipEMF - Our Equipment Monitor Frame
   * @param inPanelIndex - The index of our panel
   * @param ipParent - Our panel
   * @param isGroupName - Our group name
   * @param isDeviceName - Our device name
   * @param ipPolygon - Our shape
   * @param ipProperties - Our properties
   * @param izCanTrack - Can we display tracking?
   * @param ipPermissions
   */
  public StvLoopButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
    
    // External shape
    Rectangle r = ipPolygon.getBounds();
    mnExtX = r.x;
    mnExtY = r.y;
    mnExtWidth = r.width+1;
    mnExtHeight = r.height+1;
    mnExtArc = Math.min(mnExtWidth, mnExtHeight); 

    // Internal shape
    int vnLoopWidth = getLoopWidth();
    mnIntX = mnExtX + vnLoopWidth;
    mnIntY = mnExtY + vnLoopWidth;
    mnIntWidth = mnExtWidth - vnLoopWidth*2;
    mnIntHeight = mnExtHeight - vnLoopWidth*2;
    mnIntArc = Math.min(mnIntWidth, mnIntHeight);
  }
  
  /**
   * Build this graphic's pop-up menu
   * 
   * @param ipEMF
   * @param inPanelIndex
   * @param isGroupName
   * @param izCanTrack
   * @param ipPermissions
   */
  @Override
  protected void buildPopup(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      String isGroupName, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    // No pop-up
  }

  /**
   * Get the width of the STV loop.  Provided for extensibility.
   * <P>There are lots of different possibilities to return here.  Some of the
   * more obvious are:
   * <UL>
   *   <LI>a constant number (may cause issues if the polygon is too small)</LI>
   *   <LI>mnExtArc/x (width equals 1/x the minimum distance)</LI>
   *   <LI>Application.getInt("LoopWidth")</LI>
   * </UL></P>
   * 
   * @return 10
   */
  protected int getLoopWidth()
  {
    return 10;
  }

  /**
   * Draw the actual shape
   * @param g
   */
  @Override
  protected void drawPolygon(Graphics g)
  {
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      // There is only a border, because the rest is background color
      g.setColor(getBorderColor(BORDER_COLOR));
      g.drawRoundRect(mnExtX, mnExtY, mnExtWidth-1, mnExtHeight-1, mnExtArc, mnExtArc);
      
      g.setColor(getBorderColor(BORDER_COLOR));
      g.drawRoundRect(mnIntX, mnIntY, mnIntWidth, mnIntHeight, mnIntArc, mnIntArc);
    }
    else
    {
      // There is no border, because the border wouldn't draw correctly.
      g.setColor(getFillColor(DAIFUKU_PURPLE));
      g.fillRoundRect(mnExtX, mnExtY, mnExtWidth, mnExtHeight, mnExtArc, mnExtArc);
      
      g.setColor(mpParent.getBackground());
      g.fillRoundRect(mnIntX, mnIntY, mnIntWidth, mnIntHeight, mnIntArc, mnIntArc);
    }
  }

  /**
   * STV Loops are always online for equipment monitor
   */
  @Override
  public int getStatus()
  {
    // Future and non-JVM are always "no status"
    if (mzIsFuture || !mzIsAssignedJVM)
    {
      return NO_STATUS;
    }
    
    // Everything else is "online"
    return ONLINE;
  }
}
