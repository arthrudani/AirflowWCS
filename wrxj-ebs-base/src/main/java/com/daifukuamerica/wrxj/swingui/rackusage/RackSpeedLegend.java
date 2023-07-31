package com.daifukuamerica.wrxj.swingui.rackusage;

import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.swing.LegendItem;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * <B>Description:</B> Legend panel for RackUsage screen
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class RackSpeedLegend extends JPanel
{
  /**
   * Constructor 
   */
  public RackSpeedLegend(int inX, int inY, int inWidth, int inHeight)
  {
    super();
    setBounds(inX, inY, inWidth, inHeight);
    setBorder(new LineBorder(Color.BLACK, 1));
    setBackground(Color.WHITE);
    setLayout(new FlowLayout(FlowLayout.CENTER, 0, 6));

    add(new SKDCLabel("First"));
    add(new LegendItem(getSpeedColor(0, 0, 1), ""));
    add(new LegendItem(getSpeedColor(1, 0, 10), ""));
    add(new LegendItem(getSpeedColor(2, 0, 10), ""));
    add(new LegendItem(getSpeedColor(3, 0, 10), ""));
    add(new LegendItem(getSpeedColor(4, 0, 10), ""));
    add(new LegendItem(getSpeedColor(5, 0, 10), ""));
    add(new LegendItem(getSpeedColor(6, 0, 10), ""));
    add(new LegendItem(getSpeedColor(7, 0, 10), ""));
    add(new LegendItem(getSpeedColor(8, 0, 10), ""));
    add(new LegendItem(getSpeedColor(9, 0, 10), ""));
    add(new LegendItem(getSpeedColor(1, 0, 1), ""));
    add(new SKDCLabel("Last"));
    add(Box.createHorizontalStrut(50));
    add(new LegendItem(RackUsageLegend.RU_PROHIBIT, "Prohibited"));
    add(Box.createHorizontalStrut(20));
    add(new LegendItem(RackUsageLegend.RU_UNAVAILABLE, "Unavailable"));
    add(Box.createHorizontalStrut(20));
    add(new LegendItem(RackUsageLegend.RU_ERROR, "Database error"));
    add(Box.createHorizontalStrut(20));
    add(new LegendItem(RackUsageLegend.RU_UNKNOWN, "No location"));
  }
  
  /**
   * Get a color that corresponds to the rack speed.
   * 
   * @param inSearchOrder
   * @param inMinSearch
   * @param inMaxSearch
   * @return
   */
  public static Color getSpeedColor(float inSearchOrder, float inMinSearch,
      float inMaxSearch)
  {
    // These locations wont be used
    if (inSearchOrder == -DBConstants.LCPROHIBIT)
    {
      return RackUsageLegend.RU_PROHIBIT;
    }
    else if (inSearchOrder == -DBConstants.LCUNAVAIL)
    {
      return RackUsageLegend.RU_UNAVAILABLE;
    }
    else if (inSearchOrder < 0)
    {
      return RackUsageLegend.RU_ERROR;
    }
    
    // These will
    inSearchOrder -= inMinSearch;
    inMaxSearch -= inMinSearch;
    if (inSearchOrder > inMaxSearch/2)
    {
      return new Color((inMaxSearch-inSearchOrder)/inMaxSearch, (inMaxSearch-inSearchOrder)/(inMaxSearch/2), 1);
    }
    
    return new Color(inSearchOrder/inMaxSearch, 1, inSearchOrder/(inMaxSearch/2));
  }
}
