package com.daifukuamerica.wrxj.swingui.equipment.button;

/* ***************************************************************************
   Copyright © 2009 Daifuku America Corporation  All Rights Reserved.
    
   THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
   NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
   REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
   COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
   CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
   WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
   CIVIL LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import java.awt.Color;
import java.awt.Polygon;

/**
 * <B>Description:</B> Helper class for the DetailedRackButton<BR>
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2008 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class DetailedRackLocation extends Polygon
{
  public static final Color SWAP_COLOR = new Color(00,0xaa,0xaa);
  
  int mnStatus;
  
  /**
   * Create a Rack Location
   *  
   * @param inX
   * @param inY
   * @param inWidth
   * @param inHeight
   */
  public DetailedRackLocation(int inX, int inY, int inWidth, int inHeight)
  {
    super(new int[] { inX, inX+inWidth, inX+inWidth, inX }, 
          new int[] { inY, inY, inY+inHeight, inY+inHeight }, 
          4);
  }

  /**
   * Get the status color
   * @return
   */
  public Color getStatusFillColor()
  {
    switch (mnStatus)
    {
      case DBConstants.LC_DDMOVE:
      case DBConstants.LCRESERVED:
        return Color.CYAN;
        
      case DBConstants.UNOCCUPIED:
        return Color.WHITE;
        
      case DBConstants.OCCUPIED:
        return Color.GREEN;
      
      case DBConstants.LC_SWAP:
        return SWAP_COLOR; 
        
      case DBConstants.LCUNAVAIL:
      default:
        return Color.RED;
    }
  }

  /**
   * Get the status color
   * @return
   */
  public Color getStatusTextColor()
  {
    switch (mnStatus)
    {
      case DBConstants.LCUNAVAIL:
      case DBConstants.LCRESERVED:
      case DBConstants.UNOCCUPIED:
      case DBConstants.OCCUPIED:
        return Color.BLACK;
        
      default:
        return Color.WHITE;
    }
  }

  /**
   * Get text for the location.
   * @return
   */
  public String getStatusText()
  {
    return "";
  }
  
  /**
   * Set the location status/empty flag
   * @param inStatus
   * @param inEmptyFlag
   */
  public void setStatus(int inStatus, int inEmptyFlag)
  {
    if (inStatus == DBConstants.LCUNAVAIL)
      mnStatus = inStatus;
    else
      mnStatus = inEmptyFlag;
  }
}
