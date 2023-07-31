package com.daifukuamerica.wrxj.swingui.rackusage;

import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * <B>Description:</B> This is a class designed to display a single-shuttle
 * crane.  Shamelessly stolen from CraneButton. 
 *
 * @author       mandrus<BR>
 * @version      1.0
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class RackUsageCrane extends JComponent
{
  // Overall bounds
  protected Polygon mpPolygon;
  
  protected Polygon mpRail;
  protected Polygon mpCrane;
  
  /**
   * Constructor
   * 
   * @param inWidth
   * @param inHeight
   */
  public RackUsageCrane(int x, int y, int inWidth, int inHeight)
  {
    super();
    setBounds(x, y, inWidth, inHeight);
    
    inWidth -= 2;
    inHeight -= 2;
    
    mpPolygon = new Polygon(new int[] { 0, inWidth, inWidth, 0 }, new int[] {
        0, 0, inHeight, inHeight }, 4);

    // Build the crane
    buildRail(mpPolygon.getBounds());
    buildCrane(mpPolygon.getBounds());
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
    
    // Horizontal Crane
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
  }

  /**
   * Draw the crane rail
   * @param g
   */
  protected void drawRail(Graphics g)
  {
    // Color
    g.setColor(EquipmentGraphic.DAIFUKU_PURPLE);
    g.fillPolygon(mpRail);

    // Border
    g.setColor(Color.BLACK);
    g.drawPolygon(mpRail);
  }
  
  /**
   * Draw the actual crane
   * @param g
   */
  protected void drawCrane(Graphics g)
  {
    // Color
    g.setColor(EquipmentGraphic.DAIFUKU_MEDIUM_PURPLE);
    g.fillPolygon(mpCrane);

    // Border
    g.setColor(Color.BLACK);
    g.drawPolygon(mpCrane);
  }
}
