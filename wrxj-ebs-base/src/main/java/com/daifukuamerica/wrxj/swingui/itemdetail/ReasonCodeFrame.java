package com.daifukuamerica.wrxj.swingui.itemdetail;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryAdjustServer;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$
  
  Copyright ? 2007 Daifuku America Corporation  All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
  TREATIES. NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED
  COPIED, DISTRIBUTED, REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED,
  EXPANDED, COLLECTED, COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED
  WITHOUT THE PRIOR WRITTEN CONSENT OF Daifuku America Corporation ANY
  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
  PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
****************************************************************************/

/**
 * Title:        Screen Class to handle Reason Code Maintenance
 * Description:  Handles all data for Reason Code
 * Copyright:    Copyright (c) 2004
 * Company:      Daifuku America Corp.
 *
 * @author       jan
 * @version      1.0
 *               Created 15-July-05
 * @file     ReasonCodeFrame.java
 */
@SuppressWarnings("serial")
public class ReasonCodeFrame extends DacInputFrame
{
  protected SKDCComboBox reasonBox;
  protected SKDCButton okButton;
  
  /**
   * Choices property.
   * 
   * <p><b>Details:</b> This property contains the choices avaliable
   * as they are defined in the ReasonCode table. 
   * 
   * @see #getSteeringWheel()
   */
  protected String[] msChoices = null;
  
  public ReasonCodeFrame(int iReasonCategory, int inTimeOut)
  {
    super("ReasonCode", "");

    try
    {
      buildScreen(iReasonCategory);
      if(msChoices.length > 1)
      {
        setTimeout(inTimeOut);
      }

      pack();
    }
    catch(Exception exp)
    {
      exp.printStackTrace();
    }
  }

  public ReasonCodeFrame(int iReasonCategory)
  {
    this(iReasonCategory, 0);
  }

  protected void buildScreen(int iReasonCategory)
  {
    // Set the title and size.
    setTitle("Please select a reason code");
    
    // Only allow this window to be closed by the OK button.
    setClosable(false);
    
    // Get the choice list and fill in the choice box.
    StandardInventoryAdjustServer invtAdjServ = Factory.create(StandardInventoryAdjustServer.class);
    try
    {
      msChoices = invtAdjServ.getReasonCodeChoiceList(iReasonCategory);
    }
    catch(DBException exp)
    {
      exp.printStackTrace();
    }
    
    reasonBox = new SKDCComboBox(msChoices);
    addInput("", reasonBox);
    
    mpBtnClear.setVisible(false);
    mpBtnClose.setVisible(false);
  }

  @Override
  protected void okButtonPressed()
  {
    String selected = (String) reasonBox.getSelectedItem();
    if(selected == null || selected.length() == 0)
    {
      firePropertyChange(FRAME_CHANGE, null, null);
      close();
      return;
    }
    
    int index = selected.indexOf(":");
    if(index == -1)
    {
      index = selected.length();
    }
    String reasonCode = selected.substring(0, index);
    firePropertyChange(FRAME_CHANGE, null, reasonCode);
    close();
  }

  public String[] getMsChoices()
  {
    return msChoices;
  }
}
