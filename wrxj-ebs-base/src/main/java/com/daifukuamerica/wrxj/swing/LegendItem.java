package com.daifukuamerica.wrxj.swing;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * <B>Description:</B> Class for items on legend panels
 *
 * <P>Copyright (c) 2009 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class LegendItem extends JPanel
{
  /**
   * Constructor
   */
  public LegendItem(Color ipColor, String isText)
  {
    super();
    setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    setOpaque(false);
    
    JPanel vpPanel = new JPanel();
    vpPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    vpPanel.setBackground(ipColor);
    add(vpPanel);
    
    add(new SKDCLabel(isText));
  }
}
