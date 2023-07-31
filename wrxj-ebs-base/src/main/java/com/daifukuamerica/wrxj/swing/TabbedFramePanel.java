package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.util.SKDCUtility;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * A panel for use on a TabbedFrame
 * 
 * @author mandrus
 */
public class TabbedFramePanel extends JPanel
{
  private static final long serialVersionUID = 1579211119262620462L;
  protected TabbedFrame mpParent;
  
  /**
   * Constructor
   * @param ipParent
   * @param isTitle
   */
  public TabbedFramePanel(TabbedFrame ipParent, String isTitle)
  {
    super();
    mpParent = ipParent;
    if (SKDCUtility.isNotBlank(isTitle))
    {
      setBorder(new TitledBorder(new EtchedBorder(), isTitle));
    }
  }
  
  /**
   * Override if clean up is necessary
   */
  public void cleanup()
  {
  }
}
