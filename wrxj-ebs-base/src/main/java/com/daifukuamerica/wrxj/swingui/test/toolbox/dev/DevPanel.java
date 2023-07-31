/* ***************************************************************************
  Copyright (c) 2018 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swingui.test.toolbox.dev;

import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.TabbedFramePanel;
import com.daifukuamerica.wrxj.swingui.test.toolbox.TestToolbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
INSERT INTO dbo.SYSCONFIG (sGroup,sParameterName,sParameterValue,sDescription)
VALUES ('TestToolbox:Panel','Developer','com.daifukuamerica.wrxj.swingui.test.toolbox.dev.DevPanel','TestToolbox Panel')
*/

/**
 * Skeleton screen for faster testing
 * 
 * @author mandrus
 */
public class DevPanel extends TabbedFramePanel implements ActionListener
{
  private static final long serialVersionUID = -6766860697137854490L;
  
  private Logger mpLogger = Logger.getLogger();

  /**
   * Constructor
   * 
   * @param ipParent
   */
  public DevPanel(TestToolbox ipParent)
  {
    super(ipParent, null);

    SKDCButton vpButton = new SKDCButton("TEST");
    vpButton.addActionListener(this);
    add(vpButton);
  }

  /**
   * Action method for test button
   */
  @Override
  public void actionPerformed(ActionEvent e)
  {
    mpLogger.logError("Test button pressed");
  }
}
