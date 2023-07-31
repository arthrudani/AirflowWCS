package com.daifukuamerica.wrxj.swingui.rackusage;

import com.daifukuamerica.wrxj.swing.LegendItem;
import com.daifukuamerica.wrxj.swingui.equipment.EquipmentGraphic;
import java.awt.Color;
import java.awt.FlowLayout;
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
public class RackUsageLegend extends JPanel
{
  public static final Color RU_DDMOVE_RESERVED = Color.CYAN;
  public static final Color RU_ERROR = Color.RED;
  public static final Color RU_OCCUPIED = Color.GREEN;
  public static final Color RU_PROHIBIT = Color.YELLOW;
  public static final Color RU_SWAP = Color.ORANGE;
  public static final Color RU_UNAVAILABLE = Color.MAGENTA;
  public static final Color RU_UNKNOWN = EquipmentGraphic.DAIFUKU_LIGHT_PURPLE;
  public static final Color RU_UNOCCUPIED = Color.BLUE;
  
  /**
   * Constructor 
   */
  public RackUsageLegend(int inX, int inY, int inWidth, int inHeight)
  {
    super();
    setBounds(inX, inY, inWidth, inHeight);
    setBorder(new LineBorder(Color.BLACK, 1));
    setBackground(Color.WHITE);
    setLayout(new FlowLayout(FlowLayout.CENTER, 20, 6));

    add(new LegendItem(RU_OCCUPIED, "Occupied"));
    add(new LegendItem(RU_UNOCCUPIED, "Empty"));
    add(new LegendItem(RU_DDMOVE_RESERVED, "Reserved"));
    add(new LegendItem(RU_PROHIBIT, "Prohibited"));
    add(new LegendItem(RU_UNAVAILABLE, "Unavailable"));
    add(new LegendItem(RU_ERROR, "Database error"));
    add(new LegendItem(RU_UNKNOWN, "No location"));
  }
}
