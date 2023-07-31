/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.equipment.button;

import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentMonitorFrame;
import com.daifukuamerica.wrxj.swingui.equipment.GroupPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Map;

/**
 * <B>Description:</B> This is a class designed to display a single-shuttle
 * crane.
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class CraneButton extends PolygonButton
{
  protected static final String SHOW_CRANE="SHOW_DEVICE_NUMBER";
  
  protected Polygon mpRail;
  protected Polygon mpCrane;
  protected String msCraneNumber;
  
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
  public CraneButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
    
    // Build the crane
    buildRail(ipPolygon.getBounds());
    buildCrane(ipPolygon.getBounds());
  }

  /**
   * Set the graphics properties
   * 
   * @param ipProperties
   */
  @Override
  protected void setProperties(Map<String, String> ipProperties)
  {
    msCraneNumber = "Undefined";
    
    super.setProperties(ipProperties);
    
    setStatusText("");
  }
  
  /**
   * Build the rail.
   * <BR>The rail runs in the direction of the longest dimension (or horizontal
   * if the dimensions are equal).
   * 
   * @param r
   */
  protected void buildRail(Rectangle r)
  {
    int vanRailX[] = null;
    int vanRailY[] = null;

    if (r.height > r.width)
    {
      vanRailX = new int[] {
          r.x + r.width/2 - getRailWidth()/2,
          r.x + r.width/2 + getRailWidth()/2,
          r.x + r.width/2 + getRailWidth()/2,
          r.x + r.width/2 - getRailWidth()/2
      };
      vanRailY = new int[] {
          r.y,
          r.y,
          r.height,
          r.height
      };
    }
    // Horizontal Crane
    else
    {
      vanRailX = new int[] {
          r.x,
          r.x,
          r.width,
          r.width
      };
      vanRailY = new int[] {
          r.y + r.height/2 - getRailWidth()/2,
          r.y + r.height/2 + getRailWidth()/2,
          r.y + r.height/2 + getRailWidth()/2,
          r.y + r.height/2 - getRailWidth()/2
      };
    }
    mpRail  = new Polygon(vanRailX,  vanRailY,  vanRailX.length);
  }
  
  /**
   * Build the crane.
   * <BR>The rail runs in the direction of the longest dimension (or horizontal
   * if the dimensions are equal).
   * 
   * @param r
   */
  protected void buildCrane(Rectangle r)
  {
    int vanCraneX[] = null;
    int vanCraneY[] = null;
    int vnStub = 10;
    int vnGuide = 1;
    int vnHalfCrane = Math.min(r.height, r.width)/2;
    
    // Vertical crane
    if (r.height > r.width)
    {
      vanCraneX = new int[] {
          r.x,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x + r.width/2 + getRailWidth()/2 + vnGuide,
          r.x + r.width/2 + getRailWidth()/2 + vnGuide,
          r.x + r.width,
          r.x + r.width,
          r.x + r.width/2 + getRailWidth()/2 + vnGuide,
          r.x + r.width/2 + getRailWidth()/2 + vnGuide,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x
      };

      vanCraneY = new int[] {
          r.y + r.height/2 - vnHalfCrane,
          r.y + r.height/2 - vnHalfCrane,
          r.y + r.height/2 - vnHalfCrane - vnStub,
          r.y + r.height/2 - vnHalfCrane - vnStub,
          r.y + r.height/2 - vnHalfCrane,
          r.y + r.height/2 - vnHalfCrane,
          r.y + r.height/2 + vnHalfCrane,
          r.y + r.height/2 + vnHalfCrane,
          r.y + r.height/2 + vnHalfCrane + vnStub,
          r.y + r.height/2 + vnHalfCrane + vnStub,
          r.y + r.height/2 + vnHalfCrane,
          r.y + r.height/2 + vnHalfCrane
      };
    }
    // Horizontal Crane
    else
    {
      vanCraneX = new int[] {
          r.x + r.width/2 - vnHalfCrane - vnStub,
          r.x + r.width/2 - vnHalfCrane,
          r.x + r.width/2 - vnHalfCrane,
          r.x + r.width/2 + vnHalfCrane,
          r.x + r.width/2 + vnHalfCrane,
          r.x + r.width/2 + vnHalfCrane + vnStub,
          r.x + r.width/2 + vnHalfCrane + vnStub,
          r.x + r.width/2 + vnHalfCrane,
          r.x + r.width/2 + vnHalfCrane,
          r.x + r.width/2 - vnHalfCrane,
          r.x + r.width/2 - vnHalfCrane,
          r.x + r.width/2 - vnHalfCrane - vnStub
      };

      vanCraneY = new int[] {
          r.y + r.height/2 - getRailWidth()/2 - vnGuide,
          r.y + r.height/2 - getRailWidth()/2 - vnGuide,
          r.y,
          r.y,
          r.y + r.height/2 - getRailWidth()/2 - vnGuide,
          r.y + r.height/2 - getRailWidth()/2 - vnGuide,
          r.y + r.height/2 + getRailWidth()/2 + vnGuide,
          r.y + r.height/2 + getRailWidth()/2 + vnGuide,
          r.y + r.height,
          r.y + r.height,
          r.y + r.height/2 + getRailWidth()/2 + vnGuide,
          r.y + r.height/2 + getRailWidth()/2 + vnGuide
      };
    }
    
    mpCrane = new Polygon(vanCraneX, vanCraneY, vanCraneX.length);
    mpPolygon = mpCrane;  // To make the pop-up only show up on the actual crane
  }

  /**
   * Get the width of the crane rail.  Provided for extensibility.
   * <P>There are lots of different possibilities to return here.  Some of the
   * more obvious are:
   * <UL>
   *   <LI>a constant number (may cause issues if the polygon is too small)</LI>
   *   <LI>mnExtArc/x (width equals 1/x the minimum distance)</LI>
   *   <LI>Application.getInt("RailWidth")</LI>
   * </UL></P>
   * 
   * @return 10
   */
  protected int getRailWidth()
  {
    return Math.min(mpPolygon.getBounds().height, mpPolygon.getBounds().width)/3;
  }
  
  /**
   * Draw the graphic/text
   */
  @Override
  public void paintComponent(Graphics g)
  {
    drawRail(g);
    drawCrane(g);
    if (mzShowTracking)
    {
      drawText(g, "" + getTrackingCount(), null);
    }
    else
    {
      drawText(g, msStatusText, msStatusText2);
    }
  }

  /**
   * @see com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton#getFillColor(java.awt.Color)
   */
  @Override
  protected Color getFillColor(Color ipFillColor)
  {
    if (ipFillColor != DAIFUKU_PURPLE)
      return super.getFillColor(ipFillColor);
    else
      return ipFillColor;
  }
  
  /**
   * Draw the crane rail
   * @param g
   */
  protected void drawRail(Graphics g)
  {
    // Color
    if (mzIsFuture || !mzIsAssignedJVM)
      g.setColor(getBackground());
    else
      g.setColor(DAIFUKU_PURPLE);
    g.fillPolygon(mpRail);

    // Border
    g.setColor(getBorderColor(BORDER_COLOR));
    g.drawPolygon(mpRail);
  }
  
  /**
   * Draw the actual crane
   * @param g
   */
  protected void drawCrane(Graphics g)
  {
    // Color
    g.setColor(getFillColor(mpStatusColor));
    g.fillPolygon(mpCrane);

    // Border
    g.setColor(getBorderColor(BORDER_COLOR));
    g.drawPolygon(mpCrane);
  }
  
  /**
   * Let someone NOT show the crane number
   * 
   * @see com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton#assignParameter(java.lang.String, java.lang.String)
   */
  @Override
  protected boolean assignParameter(String isParameter, String isValue)
  {
    if (isParameter.equals(SHOW_CRANE))
    {
      if (Boolean.parseBoolean(isValue) == false)
      {
        msCraneNumber = null;
      }
    }
    else
    {
      return super.assignParameter(isParameter, isValue);
    }
    return true;
  }
  
  /**
   * Also set our crane number
   * 
   * @see com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton#setDeviceID(java.lang.String)
   */
  @Override
  public void setDeviceID(String isDeviceID)
  {
    if (msCraneNumber != null)
    {
      super.setDeviceID(isDeviceID);
      int vnIndex = 0;
      while (vnIndex < isDeviceID.length()
          && (!Character.isDigit(isDeviceID.charAt(vnIndex)) ||
              (isDeviceID.charAt(vnIndex) == '0'))) // Skip leading 0s
      {
        vnIndex++;
      }
      msCraneNumber = isDeviceID.substring(vnIndex);
    }
  }
  
  /**
   * Also set out crane number
   * 
   * @see com.daifukuamerica.wrxj.swingui.equipment.button.PolygonButton#setStatusText(java.lang.String)
   */
  @Override
  public void setStatusText(String isStatusText)
  {
    if (mzIsFuture)
    {
      if (msCraneNumber != null)
      {
        isStatusText = msCraneNumber + "|Future";
      }
      else
      {
        isStatusText = msCraneNumber;
      }
    }
    else if (!mzIsAssignedJVM)
    {
      if (msCraneNumber != null)
      {
        isStatusText = msCraneNumber + "|" + msJVMNote;
      }
      else
      {
        isStatusText = msCraneNumber;
      }
    }
    else if (msCraneNumber != null && !isStatusText.startsWith(msCraneNumber))
    {
      if (isStatusText.trim().length() == 0)
      {
        isStatusText = msCraneNumber;
      }
      else
      {
        isStatusText = msCraneNumber + "|" + isStatusText;
      }
    }
    super.setStatusText(isStatusText);
  }
}
