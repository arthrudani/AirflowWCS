/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2007 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.item;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryAdjustServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class UpdateItemLotHold extends DacInputFrame
{
  protected String msItemID = "";
  protected String msLot    = "";

  protected StandardInventoryAdjustServer mpInvAdjServer;
  protected StandardInventoryServer mpInvServer;
  
  protected SKDCTextField mpTxtItem;
  protected SKDCTextField mpTxtLot;
  protected SKDCTranComboBox mpCBHold;
  protected SKDCComboBox mpCBReason;
  
  public UpdateItemLotHold(String isItemID, String isLot)
  {
    super("Hold Item/Lot", "Hold Information");
    
    msItemID = isItemID;
    msLot = isLot;
    
    mpInvAdjServer = Factory.create(StandardInventoryAdjustServer.class);
    mpInvServer = Factory.create(StandardInventoryServer.class);
    
    buildScreen();
  }
  
  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    mpTxtItem = new SKDCTextField(DBInfo.getFieldLength(LoadLineItemData.ITEM_NAME));
    mpTxtItem.setEnabled(false);
    mpTxtLot  = new SKDCTextField(DBInfo.getFieldLength(LoadLineItemData.LOT_NAME));
    try
    {
      int[] vanValidHold = // Don't allow Shipping Hold
        new int[] { DBConstants.ITMAVAIL, DBConstants.ITMHOLD, DBConstants.QCHOLD };
      mpCBHold = new SKDCTranComboBox(LoadLineItemData.HOLDTYPE_NAME, vanValidHold, false);
      mpCBHold.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              try
              {
                int vnSelected = mpCBHold.getIntegerValue();
                if (mpCBReason != null)
                  mpCBReason.setEnabled(!(vnSelected == DBConstants.ITMAVAIL));
              }
              catch (NoSuchFieldException nsfe) {}
            }
          });
      
      
      String[] vasHoldReasons = mpInvAdjServer.getReasonCodeChoiceList(DBConstants.REASONHOLD);
      if (vasHoldReasons.length > 0)
        mpCBReason = new SKDCComboBox(vasHoldReasons);
    }
    catch (Exception e)
    {
      logger.logException(e);
    }

    clearButtonPressed();
    
    addInput("Item", mpTxtItem);
    addInput("Lot", mpTxtLot);
    addInput("Status", mpCBHold);
    if (mpCBReason != null)
    {
      addInput("Reason", mpCBReason);
    }
    useModifyButtons();
    
    mpCBHold.requestFocus();
  }

  /**
   * Change the lot hold type
   */
  @Override
  protected void okButtonPressed()
  {
    // LOT
    String vsLot = mpTxtLot.getText();
    
    // HOLD TYPE
    int vnHold = -1;
    try
    {
      vnHold = mpCBHold.getIntegerValue();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
      return;
    }
    
    // REASON
    String vsReason = "";
    if (mpCBReason != null && vnHold != DBConstants.ITMAVAIL)
    {
      vsReason = mpCBReason.getText();
      int vnIndex = vsReason.indexOf(":");
      if (vnIndex == -1)
      {
        vnIndex = vsReason.length();
      }
      vsReason = vsReason.substring(0, vnIndex);
    }

    try
    {
      /*
       * Change item details
       */
      mpInvServer.setItemHoldValue(msItemID, vsLot, vsReason, vnHold);
      
      /*
       * TODO: Change expected receipts, if we decide to do that someday
       */
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
      return;
    }

    firePropertyChange(FRAME_CHANGE, null, null);
    close();
  }
  
  /**
   * Reset the screen
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTxtItem.setText(msItemID);
    mpTxtLot.setText(msLot);
    mpCBHold.setSelectedIndex(0);
    if (mpCBReason != null)
      mpCBReason.setSelectedIndex(0);
    
    mpCBHold.requestFocus();
  }
}
