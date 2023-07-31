package com.daifukuamerica.wrxj.swing;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * DoubleClickFrame
 * 
 * @Copyright Copyright (c) 2008 Daifuku America Corporation
 * @author Stephen Kendorski
 * @version 1.0
 */
@SuppressWarnings("serial")
public class DoubleClickFrame extends SKDCInternalFrame
{
  JScrollPane mpScrollPane = new JScrollPane();
  JEditorPane mpEditorPane = new JEditorPane();

  /**
   * Constructor
   * 
   * @param isTitle
   */
  public DoubleClickFrame(String isTitle)
  {
    super(isTitle);
    setAllowDuplicateScreens(true);
    jbInit();
  }

  /**
   * Set the frame data
   * 
   * @param ipUrl
   */
  public void setData(URL ipUrl)
  {
    try 
    {
      mpEditorPane.setPage(ipUrl);
    }
    catch (IOException e) 
    {
      logger.logException(e);
    }
  }

  /**
   * Set the frame data
   * 
   * @param isData
   * @param izIsHTML
   */
  public void setData(String isData, boolean izIsHTML)
  {
    if (izIsHTML)
    {
      mpEditorPane.setContentType("text/html");
    }
    else
    {
      mpEditorPane.setContentType("text/plain");
    }
    setData(isData);
  }

  /**
   * Set the frame data
   * 
   * @param isData
   */
  public void setData(String isData)
  {
    mpEditorPane.setText(isData);
    mpEditorPane.setCaretPosition(0);
  }

  /**
   * Build the screen
   */
  private void jbInit()
  {
    mpEditorPane.setEditable(false);  // This was true, but I couldn't see why.
    mpScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    mpScrollPane.getViewport().add(mpEditorPane, null);
    getContentPane().add(mpScrollPane, BorderLayout.CENTER);
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