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

package com.daifukuamerica.wrxj.swingui.test;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardStationServer;
import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.item.ItemNumberInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class SimplePurchaseOrderFrame extends DacInputFrame
{
  protected SKDCTextField    mpOrderID;
  protected SKDCComboBox     mpStation;
  protected ItemNumberInput     mpItem;
  protected SKDCTextField    mpLot;
  protected SKDCIntegerField mpQty;

  private   StandardInventoryServer   mpInvServer;
  protected StandardPoReceivingServer mpPOServer;
  private   StandardStationServer     mpStationServer;
  
  public SimplePurchaseOrderFrame()
  {
    super("Simple Expected Receipt", "Add Expected Receipt");
    
    mpInvServer     = Factory.create(StandardInventoryServer.class);
    mpPOServer      = Factory.create(StandardPoReceivingServer.class);
    mpStationServer = Factory.create(StandardStationServer.class);
    
    buildInputPanel();
  }

  /**
   * Build the input panel
   */
  private void buildInputPanel() 
  {
    mpOrderID = new SKDCTextField(PurchaseOrderLineData.ORDERID_NAME);
    mpStation = new SKDCComboBox();
    mpItem    = new ItemNumberInput(mpInvServer, true, false);
    mpLot     = new SKDCTextField(PurchaseOrderLineData.LOT_NAME);
    mpQty     = new SKDCIntegerField(1,8);

    mpOrderID.setText(createOrderIDByDateTime());
    fillStationComboBox();
    
    addInput("Order ID:", mpOrderID);
    addInput("Station:",  mpStation);
    addInput("Item:",     mpItem);
    addInput("Lot:",      mpLot);
    addInput("Quantity:", mpQty);
  }
  
  /**
   * Fill the station combo box
   */
  void fillStationComboBox()
  {
    try
    {
      int[] inputStations = {DBConstants.USHAPE_IN, DBConstants.PDSTAND,
                            DBConstants.INPUT, DBConstants.REVERSIBLE};
      Map vpStationsMap = mpStationServer.getStationsByStationType(inputStations);
      Object[] vapStations = vpStationsMap.keySet().toArray();
      mpStation.setComboBoxData(vapStations);
    }
    catch (DBException e)
    {
      displayError(e.getMessage(), "Unable to get Stations");
    }
  }

  
  /**
   * Clean up and close
   */
  @Override
  protected void closeButtonPressed()
  {
    mpInvServer.cleanUp();
    mpPOServer.cleanUp();
    mpStationServer.cleanUp();
    close();
  }
  
  /**
   * Reset to defaults
   */
  @Override
  protected void clearButtonPressed()
  {
    mpOrderID.setText(createOrderIDByDateTime());
    mpItem.reset();
    mpLot.setText("");
    mpQty.setValue(1);
    mpOrderID.requestFocus();
  }
  
  /**
   * New order
   */
  protected void addComplete()
  {
    mpOrderID.setText(createOrderIDByDateTime());
    mpOrderID.requestFocus();
  }

  /**
   * Create the order
   */
  @Override
  protected void okButtonPressed()
  {
    String vsOrderID = mpOrderID.getText().trim();
    String vsStation = mpStation.getSelectedItem().toString().substring(0,4);
    String vsItem    = mpItem.getSelectedItem().toString();
    String vsLot     = mpLot.getText().trim();
    int    vnQty     = mpQty.getValue();
    
    /*
     * Validation
     */
    if (vsOrderID.trim().length() == 0)
    {
      displayError("Order ID can not be blank.", "Validation Error");
      return;
    }

    if (vsStation.trim().length() == 0)
    {
      displayError("Station can not be blank.", "Validation Error");
      return;
    }
    
    if (vsItem.trim().length() == 0)
    {
      displayError("Item can not be blank.", "Validation Error");
      return;
    }
    
    if (vnQty < 1)
    {
      displayError("Quantity can not be less than 1.", "Validation Error");
      return;
    }
    
    /*
     * Add the purchase order
     */
    PurchaseOrderHeaderData vpPOHData = Factory.create(PurchaseOrderHeaderData.class);
    vpPOHData.setOrderID(vsOrderID);
    vpPOHData.setOrderStatus(DBConstants.EREXPECTED);

    PurchaseOrderLineData vpPOLData = Factory.create(PurchaseOrderLineData.class);
    vpPOLData.clear();
    vpPOLData.setOrderID(vsOrderID);
    vpPOLData.setRouteID(vsStation);
    vpPOLData.setLineID("1");
    vpPOLData.setItem(vsItem);
    vpPOLData.setLot(vsLot);
    vpPOLData.setExpectedQuantity(vnQty);

    List<PurchaseOrderLineData> vpPOLList = new ArrayList();
    vpPOLList.add(vpPOLData);
    try
    {
      mpPOServer.buildPO(vpPOHData, vpPOLList);
      displayInfoAutoTimeOut("Purchase Order " + vsOrderID + " added successfully.");
      addComplete();
//      close();
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage(), "Error adding order");
    }
  }
  
  /**
   * Create an "ER" order ID
   * @return
   */
  private String createOrderIDByDateTime()
  {
    return DBHelper.createOrderIDByDateTime("ER");
  }
}
