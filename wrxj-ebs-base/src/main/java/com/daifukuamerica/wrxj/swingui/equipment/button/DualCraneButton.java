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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Map;

/**
 * <B>Description:</B> This is a class designed to display a dual-shuttle
 * crane.
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class DualCraneButton extends CraneButton
{
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
  public DualCraneButton(EquipmentMonitorFrame ipEMF, int inPanelIndex,
      GroupPanel ipParent, String isGroupName, String isDeviceName,
      Polygon ipPolygon, Map<String, String> ipProperties, boolean izCanTrack,
      SKDCScreenPermissions ipPermissions)
  {
    super(ipEMF, inPanelIndex, ipParent, isGroupName, isDeviceName, ipPolygon,
        ipProperties, izCanTrack, ipPermissions);
  }

  /**
   * Build the crane.
   * <BR>The rail runs in the direction of the longest dimension (or horizontal
   * if the dimensions are equal).
   * 
   * @param r
   */
  @Override
  protected void buildCrane(Rectangle r)
  {
    int vanCraneX[] = null;
    int vanCraneY[] = null;
    int vnBetween = 2;
    int vnStub = 10;
    int vnGuide = 1;
    int vnHalfCrane = Math.min(r.height, r.width)/2;
    int vnCenterYOffset = 0;
    
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
          r.x + r.width,
          r.x + r.width,
          r.x + r.width/2 + getRailWidth()/2 + vnGuide,
          r.x + r.width/2 + getRailWidth()/2 + vnGuide,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x,
          r.x,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x + r.width/2 - getRailWidth()/2 - vnGuide,
          r.x
      };

      vanCraneY = new int[] {
          r.y + r.height/2 - vnHalfCrane*2 - vnBetween,
          r.y + r.height/2 - vnHalfCrane*2 - vnBetween,
          r.y + r.height/2 - vnHalfCrane*2 - vnBetween - vnStub,
          r.y + r.height/2 - vnHalfCrane*2 - vnBetween - vnStub,
          r.y + r.height/2 - vnHalfCrane*2 - vnBetween,
          r.y + r.height/2 - vnHalfCrane*2 - vnBetween,
          r.y + r.height/2 - vnBetween,
          r.y + r.height/2 - vnBetween,
          r.y + r.height/2 + vnBetween,
          r.y + r.height/2 + vnBetween,
          r.y + r.height/2 + vnHalfCrane*2 + vnBetween,
          r.y + r.height/2 + vnHalfCrane*2 + vnBetween,
          r.y + r.height/2 + vnHalfCrane*2 + vnBetween + vnStub,
          r.y + r.height/2 + vnHalfCrane*2 + vnBetween + vnStub,
          r.y + r.height/2 + vnHalfCrane*2 + vnBetween,
          r.y + r.height/2 + vnHalfCrane*2 + vnBetween,
          r.y + r.height/2 + vnBetween,
          r.y + r.height/2 + vnBetween,
          r.y + r.height/2 - vnBetween,
          r.y + r.height/2 - vnBetween
      };
      
      vnCenterYOffset = vnHalfCrane + vnBetween;
    }
    // Horizontal Crane
    else
    {
      vanCraneX = new int[] {
          r.x + r.width/2 - vnHalfCrane*2 - vnBetween - vnStub,
          r.x + r.width/2 - vnHalfCrane*2 - vnBetween,
          r.x + r.width/2 - vnHalfCrane*2 - vnBetween,
          r.x + r.width/2 - vnBetween,
          r.x + r.width/2 - vnBetween,
          r.x + r.width/2 + vnBetween,
          r.x + r.width/2 + vnBetween,
          r.x + r.width/2 + vnHalfCrane*2 + vnBetween,
          r.x + r.width/2 + vnHalfCrane*2 + vnBetween,
          r.x + r.width/2 + vnHalfCrane*2 + vnBetween + vnStub,
          r.x + r.width/2 + vnHalfCrane*2 + vnBetween + vnStub,
          r.x + r.width/2 + vnHalfCrane*2 + vnBetween,
          r.x + r.width/2 + vnHalfCrane*2 + vnBetween,
          r.x + r.width/2 + vnBetween,
          r.x + r.width/2 + vnBetween,
          r.x + r.width/2 - vnBetween,
          r.x + r.width/2 - vnBetween,
          r.x + r.width/2 - vnHalfCrane*2 - vnBetween,
          r.x + r.width/2 - vnHalfCrane*2 - vnBetween,
          r.x + r.width/2 - vnHalfCrane*2 - vnBetween - vnStub,
      };

      vanCraneY = new int[] {
          r.y + r.height/2 - getRailWidth()/2 - vnGuide,
          r.y + r.height/2 - getRailWidth()/2 - vnGuide,
          r.y,
          r.y,
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
          r.y + r.height/2 + getRailWidth()/2 + vnGuide,
          r.y + r.height,
          r.y + r.height,
          r.y + r.height/2 + getRailWidth()/2 + vnGuide,
          r.y + r.height/2 + getRailWidth()/2 + vnGuide
      };
    }
    
    mpCrane = new Polygon(vanCraneX, vanCraneY, vanCraneX.length);
    mpPolygon = mpCrane;  // To make the pop-up only show up on the actual crane
    mnCenterY -= vnCenterYOffset;  // So status text displays properly
  }

}
