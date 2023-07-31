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

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCPopupMenu;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ItemLotQuantityFrame extends DacInputFrame
{
  private String msItemID;

  private StandardInventoryServer mpInvServer = null;
  private boolean mzAllowHold;
  
  private String dataName = "ItemLotQuantities"; 

  private SKDCLabel mpLTotalQty;
  private SKDCLabel mpLAllocQty;
  private SKDCLabel mpLExpectQty;

  public ItemLotQuantityFrame(String isItemID, 
      StandardInventoryServer ipInvServer, boolean izAllowHold)
  {
    super("Item Quantities", null);
    
    msItemID = isItemID;
    mpInvServer = ipInvServer;
    mzAllowHold = izAllowHold;
    
    buildScreen();
  }
  
  /**
   * Build the screen
   */
  private void buildScreen()
  {
    /*
     * Build the screen header
     */
    SKDCLabel vpItem = new SKDCLabel(msItemID);
    mpLTotalQty = new SKDCLabel();
    mpLAllocQty = new SKDCLabel();
    mpLExpectQty = new SKDCLabel();
    
    addInput("Item ID", vpItem);
    addInput("Total Current Quantity", mpLTotalQty);
    addInput("Allocated Quantity", mpLAllocQty);
    addInput("Expected Quantity", mpLExpectQty);
    
    /*
     * Move the input panel to the top and add an SKDCTable in the middle
     */
    showTableWithDefaultButtons(dataName);
    mpTable.allowOneRowSelection(true);
    setTableMouseListener();
    
    /*
     * Set up the buttons
     */
    mpBtnAddLine.setVisible(false);
    mpBtnModLine.setVisible(false);
    mpBtnDelLine.setVisible(false);
    
    mpBtnSubmit.setText("Modify Lot Hold");
    mpBtnSubmit.setToolTipText("Modify Lot Hold");
    mpBtnSubmit.setMnemonic('M');
    mpBtnSubmit.setVisible(mzAllowHold);
    mpBtnClear.setVisible(false);

    /*
     * Set the size
     */
    setPreferredSize(new Dimension(600,400));

    /*
     * Get latest quantities
     */
    refreshQuantities();
  }
  
  /**
   * Refresh the item/lot quantities
   */
  private void refreshQuantities()
  {
    try
    {
      double[] vadQty = mpInvServer.getItemQuantities(msItemID);
      double vdTotalQty = vadQty[0];
      double vdAllocQty = vadQty[1];
      double vdExpectQty = vadQty[2];
      
      mpLTotalQty.setText("" + vdTotalQty);
      mpLAllocQty.setText("" + vdAllocQty);
      mpLExpectQty.setText("" + vdExpectQty);

      List<Map> vpLotList = mpInvServer.getItemQuantitiesByLot(msItemID);
      refreshTable(vpLotList);
      mpTable.selectFirstRow();
      mpBtnSubmit.setEnabled(vpLotList.size() > 0);
    }
    catch (DBException dbe)
    {
      logger.logException(dbe);
      displayError(dbe.getMessage());
    }
  }
  
  /**
   *  Hold a lot 
   */
  @Override
  protected void okButtonPressed()
  {
    String vsLot = "";
    Object cObj = mpTable.getCurrentRowDataField(LoadLineItemData.LOT_NAME);
    if (cObj != null)  //we have one
    {
      vsLot = cObj.toString();
    }
    
    UpdateItemLotHold vpUILH = Factory.create(UpdateItemLotHold.class, msItemID, vsLot);

    addSKDCInternalFrameModal(vpUILH, new JPanel[] {mpButtonPanel},
        new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop.equals(FRAME_CHANGE))
            {
              refreshQuantities();
            }
        }
      });
  }
  
  /**
   *  Set up the mouse listener for the table
   */
  @Override
  protected void setTableMouseListener()
  {
    mpPopupMenu = new SKDCPopupMenu();
    mpTable.addMouseListener(new DacTableMouseListener(mpTable)
    {
      /**
       *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
       *  to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {
        mpPopupMenu.add("Modify Lot Hold", MODIFY_BTN, new ActionListener()
          {
            public void actionPerformed(ActionEvent arg0)
            {
              okButtonPressed();
            }
          });
        mpPopupMenu.setAuthorization("Modify Lot Hold", mzAllowHold);

        return(mpPopupMenu);
      }
      
      /**
       *  Display the screen.
       */
      @Override
      public void displayDetail()
      {
      }
    });
  }
}
