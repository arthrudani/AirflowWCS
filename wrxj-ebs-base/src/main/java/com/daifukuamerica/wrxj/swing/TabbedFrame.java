package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.swing.DacInputFrame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;

/**
 * A frame with multiple tabs, for when making a screen for every little thing
 * seems silly.
 * 
 * @author mandrus
 */
@SuppressWarnings("serial")
public abstract class TabbedFrame extends DacInputFrame
{
  private List<TabbedFramePanel> mpPanels = new ArrayList<>();
  private JTabbedPane mpTabs = new JTabbedPane();

  /**
   * Constructor
   */
  public TabbedFrame()
  {
    this("");
  }

  /**
   * Constructor
   * 
   * @param isTitle
   */
  public TabbedFrame(String isTitle)
  {
    super(isTitle, "");
    buildScreen();
    setResizable(true);
  }
  
  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    // Unused buttons
    mpBtnSubmit.setVisible(false);
    mpBtnClear.setVisible(false);

    // Add tabs to the tabbed pane
    addTabs();
    
    // Add the tabbed pane to the frame
    getContentPane().add(mpTabs);
    pack();
  }

  /**
   * Add all tabs
   */
  protected abstract void addTabs();
  
  /**
   * Add a test
   * 
   * @param isTabName
   * @param ipTest
   */
  protected void addTab(String isTabName, TabbedFramePanel ipTest) {
    mpPanels.add(ipTest);
    mpTabs.add(isTabName, ipTest);
  }
  
  /*========================================================================*/
  /* Make log options available to child frames                             */
  /*========================================================================*/
  @Override
  public void logAndDisplayError(String isError)
  {
    super.logAndDisplayError(isError);
  }
  
  @Override
  public void logAndDisplayException(String isUserNote, Exception e)
  {
    super.logAndDisplayException(isUserNote, e);
  }
  
  /**
   * Clean up when we close the screen
   */
  @Override
  public void cleanUpOnClose()
  {
    super.cleanUpOnClose();
    for (TabbedFramePanel j : mpPanels) {
      j.cleanup();
    }
  }
}
