package com.daifukuamerica.wrxj.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextPane;
import javax.swing.Timer;

/**
 * Title:        RTS JAVA GUI
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Eskay
 * @author
 * @version 1.0
 */

@SuppressWarnings("serial")
public class SKDCFlashScreen extends SKDCInternalFrame
{
  JTextPane messageDisplay = new JTextPane();
  Timer autoCloseTimer;

  public SKDCFlashScreen(String isTitle, String isMessage)
  {
    try
    {
      jbInit(isMessage);
      setTitle(isTitle);
      setAllowDuplicateScreens(true);
      pack();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  public SKDCFlashScreen()
  {
    this("Information","?");
  }

  private void jbInit(String message) throws Exception
  {
    messageDisplay.setEditable(false);
    messageDisplay.setBackground(new Color(212, 208, 200));
    messageDisplay.setText(message);
    int textLength = message.length();
    if (textLength > 20)
    {
      int x = textLength * 8;
      if (x > 500)
        x = 500;
      int y = (textLength / 25 + 1)*25;
      if (y < 60)
        y = 60;
      setPreferredSize(new Dimension(x, y));
    }
    else
    {
      setPreferredSize(new Dimension(200, 100));
    }
    getContentPane().add(messageDisplay, BorderLayout.CENTER);
    ActionListener autoClose = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            //...Perform a task...
          close();
        }
    };
    // try to lengthen the time out based on message length
    autoCloseTimer = new Timer(1000 + message.length()*12, autoClose);
    autoCloseTimer.start();
  }
 /**
  *  Method to do needed cleanup on close. May be overridden by classes that
  *  extend this class.
  */
  @Override
  public void cleanUpOnClose()
  {
    autoCloseTimer.stop();
  }

  @Override
  protected boolean getSystemGatewayNeeded()
  {
    return false;
  }

}