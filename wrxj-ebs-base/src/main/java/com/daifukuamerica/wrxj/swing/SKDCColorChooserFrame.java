package com.daifukuamerica.wrxj.swing;

import java.awt.Color;
import javax.swing.JColorChooser;

public class SKDCColorChooserFrame extends SKDCInternalFrame
{
  private static final long serialVersionUID = 0L;

  private Color newColor = null;

  public SKDCColorChooserFrame()
  {
    super();
  }

  public Color getColorChoice(String s, Color initialColor)
  {
    newColor = JColorChooser.showDialog(this, s, initialColor);
    return newColor;
  }

  public Color getColorChoice(String s)
  {
    newColor = JColorChooser.showDialog(this, s, Color.white);
    return newColor;
  }

  /**
   * Indicates that no system gateway is needed.
   *
   * <p><b>Details:</b> <code>getSystemGatewayNeeded</code> returns
   * <code>false</code> to indicate that no system gateway is needed by this
   * frame.  This method is called by the superclass during initialization.</p>
   *
   * @return false
   */
  @Override
  protected boolean getSystemGatewayNeeded() {return false;}

}

