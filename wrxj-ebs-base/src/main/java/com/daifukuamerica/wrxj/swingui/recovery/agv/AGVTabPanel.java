package com.daifukuamerica.wrxj.swingui.recovery.agv;

import javax.swing.JPanel;

/**
 * Interface for tabbed panel of AGV GUI.
 *
 * @author A.D.
 * @since  13-Jul-2009
 */
public abstract class AGVTabPanel extends JPanel
{
  public static final String SYS_PANEL_EVT = "PANEL_ENABLE_DISABLE";

  public abstract void execDataValidation();

  public void refreshTable()
  {
  }

  public void errorCleanUp()
  {
  }

  public void deleteCommand()
  {
  }

  public void cancelCommand()
  {
  }

  public void openTabOperations()
  {
  }

  public void closeTabOperations()
  {
  }
}
