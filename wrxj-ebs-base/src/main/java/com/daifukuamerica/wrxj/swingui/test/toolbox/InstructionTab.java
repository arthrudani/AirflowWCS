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
package com.daifukuamerica.wrxj.swingui.test.toolbox;

import com.daifukuamerica.wrxj.swing.TabbedFramePanel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.InputStream;
import java.util.Scanner;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Instruction tab
 * 
 * @author mandrus
 */
public class InstructionTab extends TabbedFramePanel
{
  private static final long serialVersionUID = -798704620564586709L;

  /**
   * Constructor
   * 
   * @param ipParent
   * @param isTitle
   */
  public InstructionTab(TestToolbox ipParent)
  {
    super(ipParent, null);
    setPreferredSize(new Dimension(600, 300));
    setLayout(new GridLayout(1, 1));
    
    JTextPane area = new JTextPane();
    area.setEditable(false);

    String vsResource = "InstructionTab.html";
    String vsInstructions = "";
    try (InputStream is = getClass().getResourceAsStream(vsResource))
    {
      if (is == null)
      {
        throw new Exception(vsResource + " not found!");
      }
      try (Scanner s = new Scanner(is))
      {
        vsInstructions = s.useDelimiter("\\A").hasNext() ? s.next() : "";
      }
      area.setContentType("text/html");
    }
    catch (Exception e)
    {
      vsInstructions = "Exception loading " + vsResource + "\n";
//      vsInstructions += ExceptionUtils.getStackTrace(e);
      e.printStackTrace();
    }
    area.setText(vsInstructions);
    
    add(new JScrollPane(area));
    
    area.setCaretPosition(0);
  }
}
