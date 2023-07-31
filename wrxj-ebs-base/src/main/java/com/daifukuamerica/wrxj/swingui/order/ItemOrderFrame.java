/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2004 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swingui.order;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCScreenPermissions;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCUserData;
import com.daifukuamerica.wrxj.swingui.move.MoveListFrame;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

@SuppressWarnings("serial")
public abstract class ItemOrderFrame extends DacInputFrame
{
  protected OrderHeaderData mpOHData;
  
  protected SKDCTextField    mpOrderID;
  protected StationComboBox  mpDestStation;

  protected StandardInventoryServer mpInvServer;
  protected StandardOrderServer     mpOrderServer;
  protected StandardStationServer   mpStationServer;
  
  protected SKDCButton mpBtnMoves = new SKDCButton("Moves");
  
  protected boolean mzAdding = false;
  protected boolean mzDisplayOnly = false;
  
  protected SKDCScreenPermissions ePerms;
  protected SKDCUserData userData;

  public ItemOrderFrame(String isFrameTitle, String isBorderTitle)
  {
    super(isFrameTitle, isBorderTitle);
    setResizable(true);
    mpInvServer     = Factory.create(StandardInventoryServer.class);
    mpOrderServer   = Factory.create(StandardOrderServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);
    
    mpOrderID = new SKDCTextField(DBInfo.getFieldLength(OrderHeaderData.ORDERID_NAME));
    
    mpDestStation = new StationComboBox();
    try
    {
      mpDestStation.fillWithOutputs(SKDCConstants.NO_PREPENDER);
    }
    catch (DBException e)
    {
      displayError(e.getMessage(), "Unable to get Stations");
    }
    
    userData = new SKDCUserData();
    ePerms = userData.getOptionPermissionsByClass(ItemOrderFrame.class);
  }

  protected abstract void buildScreen();
  protected abstract void setAddMode();
  protected abstract void setModifyMode(OrderHeaderData ipOHData);
  protected abstract void setDisplayMode(OrderHeaderData ipOHData);
  protected abstract void setPermissions();
    
  /**
   * Clean up and close
   */
  @Override
  protected void closeButtonPressed()
  {
    mpInvServer.cleanUp();
    mpOrderServer.cleanUp();
    mpStationServer.cleanUp();
    close();
  }
  
  /**
   * Reset to defaults
   */
  @Override
  protected void clearButtonPressed()
  {
    if (mzAdding)
    {
      mpOrderID.setText(DBHelper.createOrderIDByDateTime("OR"));
      mpOrderID.requestFocus();
    }
  }
  
  /**
   * New order
   */
  protected void addComplete()
  {
    changed(null, mpOHData);
    clearButtonPressed();
  }

  /**
   * Actually add the item order to the database.
   * 
   * @param ipOHData
   * @param iapLines
   */  
  protected void buildItemOrder(OrderHeaderData ipOHData, OrderLineData... iapLines)
  {
    try
    {
      String vsBuildMessage = mpOrderServer.buildOrder(ipOHData, iapLines);
      logger.logDebug(vsBuildMessage);
      displayInfoAutoTimeOut("Order " + ipOHData.getOrderID() 
          + " added successfully.");
//      close();
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage(), "Error adding order");
      return;
    }
    
    /*
     * Tell the allocator that we just added a new order
     */
//    try
//    {
//      AllocationMessageDataFormat vpAllocatorMessage = new AllocationMessageDataFormat();
//      vpAllocatorMessage.clear();
//      vpAllocatorMessage.setOutputStation(ipOHData.getDestinationStation());
//      vpAllocatorMessage.createDataString();
//  
//      getSystemGateway().publishAllocateEvent(
//          vpAllocatorMessage.createStringToSend(), 0,
//          mpStationServer.getStationsScheduler(ipOHData.getDestinationStation()));
//    }
//    catch (Exception e)
//    {
//      displayError("Error waking allocator: " + e.getMessage());
//      logger.logException(e);
//    }
    
    /*
     * Get ready for another order
     */
    addComplete();
  }
  
  protected void addMovesButton()
  {
    mpButtonPanel.add(mpBtnMoves);
    mpBtnMoves.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          movesButtonPressed();
        }
      });
    }
  
  protected abstract void movesButtonPressed();
  
  protected void showMoves(List<KeyObject> searchData)
  {
    MoveListFrame vpMoveFrame = Factory.create(MoveListFrame.class);
    vpMoveFrame.setAllowDuplicateScreens(true);
    vpMoveFrame.setFilter(KeyObject.toKeyArray(searchData));
    addSKDCInternalFrameModal(vpMoveFrame, new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent pcevt)
        {
          String prop_name = pcevt.getPropertyName();
          if (prop_name.equals(FRAME_CLOSING))
          {
            correctFieldEnabledValues();
          }
        }
      });
  }
  
  protected abstract void correctFieldEnabledValues();
}
