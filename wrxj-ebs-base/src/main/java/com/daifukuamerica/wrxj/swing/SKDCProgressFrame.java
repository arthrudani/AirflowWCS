package com.daifukuamerica.wrxj.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JProgressBar;
/**
 * Title:        WRx 8.xx (Java)
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      SK Daifuku Corporation
 * @author
 * @version 1.0
 */

public class SKDCProgressFrame extends SKDCInternalFrame
{
  private static final long serialVersionUID = 0L;

  protected int m_counter = 0;
  protected JProgressBar jpb;

  public SKDCProgressFrame()
  {
    this("");
  }

  public SKDCProgressFrame(String isTitle)
  {
    super(isTitle);
    setSize(300,50);
    setPreferredSize(new Dimension(300, 50));
    jpb = new JProgressBar(0,100);
    jpb.setIndeterminate(true);
    getContentPane().add(jpb, BorderLayout.CENTER);
    setVisible(true);
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