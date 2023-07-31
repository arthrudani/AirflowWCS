package com.daifukuamerica.wrxj.swingui.rackusage;

import com.daifukuamerica.wrxj.swing.SKDCComboBox;

/**
 * <B>Description:</B> Specialized combo to help various components on the 
 * Rack Information screen (aka Rack Usage) know what data to display.
 *
 * <P>Copyright (c) 2010 by Daifuku America Corporation</P>
 *
 * @author       mandrus
 * @version      1.0
 */
public class RackInfoCombo extends SKDCComboBox
{
  public static final String SHOW_STATUS = "Status";
  public static final String SHOW_ORDER = "Search Order";

  /**
   * Constructor
   */
  public RackInfoCombo()
  {
    super(new String[] { SHOW_ORDER, SHOW_STATUS });
    setSelectedItem(SHOW_STATUS);
  }
  
  /**
   * Is the speed option selected?
   * 
   * @return
   */
  public boolean isSpeedSelected()
  {
    return (getSelectedItem().equals(SHOW_ORDER));
  }

  /**
   * Is the status option selected?
   * @return
   */
  public boolean isStatusSelected()
  {
    return (getSelectedItem().equals(SHOW_STATUS));
  }
}
