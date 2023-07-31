package com.daifukuamerica.wrxj.swingui.shipment;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.Location;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author Pete Madsen
 *
 */
@SuppressWarnings("serial")
public class ShipLoad extends DacInputFrame
{
  protected OrderHeaderData mpLoadOrderHeader;
  
  private static String MULTIPLE_ORDERS = "<Multiple>";

  SKDCTextField mpLoadText = new SKDCTextField(LoadData.LOADID_NAME);
  SKDCTextField mpOrderText = new SKDCTextField(OrderHeaderData.ORDERID_NAME);
  SKDCTextField mpCustomerText = new SKDCTextField(OrderHeaderData.SHIPCUSTOMER_NAME);
  SKDCCheckBox  mpAllowMultipleCust = new SKDCCheckBox("Allow Multiple");
  int vnLocTextLength = DBInfo.getFieldLength(LocationData.WAREHOUSE_NAME) + 1
      + DBInfo.getFieldLength(LocationData.ADDRESS_NAME);
  SKDCTextField mpCurrentLocText = new SKDCTextField(vnLocTextLength);
  SKDCTextField mpShippingLocText = new SKDCTextField(vnLocTextLength);
  SKDCTextField mpConfirmLocText = new SKDCTextField(vnLocTextLength);

  SKDCButton mpStageButton = new SKDCButton("Stage");

  StandardInventoryServer mpInventoryServer = Factory.create(StandardInventoryServer.class, keyName);
  StandardLoadServer mpLoadServer = Factory.create(StandardLoadServer.class, keyName);
  StandardLocationServer mpLocationServer = Factory.create(StandardLocationServer.class, keyName);
  StandardOrderServer mpOrderServer = Factory.create(StandardOrderServer.class, keyName);
  StationData stationData = null;
  OrderLineData oldata = Factory.create(OrderLineData.class);

  
  /**
   * @param isTitle
   */
  public ShipLoad(String isTitle)
  {
    super(isTitle + " Load", "Load Shipment Information");
    jbInit();
  }

  /**
   * 
   */
  public ShipLoad()
  {
    this("Stage / Ship Load");
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  protected void jbInit()
  {
    mpLoadText.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ieAction)
        {
          String vsLoadID = mpLoadText.getText().trim();
          if (vsLoadID.length() > 0)
          {
            displayLoadData();
          }
          else
          {
            clearLoadData();
          }
        }
      });
    mpOrderText.setEnabled(false);
    mpCustomerText.setEnabled(false);
    mpCurrentLocText.setEnabled(false);
    mpShippingLocText.setEnabled(false);
    mpConfirmLocText.setEnabled(false);

    JPanel vpCustomerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    vpCustomerPanel.add(mpCustomerText);
    vpCustomerPanel.add(Box.createHorizontalStrut(20));
    vpCustomerPanel.add(mpAllowMultipleCust);

    /*
     * Add the input fields to the panel
     */
    addInput("Load ID:", mpLoadText);
    addInput("Order ID:", mpOrderText);
    addInput("Customer ID:", vpCustomerPanel);
    addInput("Current Location:", mpCurrentLocText);
    addInput("Shipping Location:", mpShippingLocText);
    addInput("Confirmed Location:", mpConfirmLocText);

    /*
     *  Button Panel
     */
    mpStageButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          stageButtonPressed();
        }
      });
    mpBtnSubmit.setText("Ship");

    mpButtonPanel.add(mpStageButton);
    mpButtonPanel.add(mpBtnSubmit);    // Re-add the rest to preserve order
    mpButtonPanel.add(mpBtnClear);
    mpButtonPanel.add(mpBtnClose);

    mpStageButton.setEnabled(false);
    mpBtnSubmit.setEnabled(false);
    validate();
  }

  /**
   * Display the load data on the screen
   */
  private void displayLoadData()
  {
    mpLoadOrderHeader = null;
    String vsLoadID = mpLoadText.getText();

    mpStageButton.setEnabled(false);
    mpBtnSubmit.setEnabled(false);
    
    try
    {
      // if the Load does not exist, tell the user
      LoadData vpLoadData = mpLoadServer.getLoad(vsLoadID);
      if(vpLoadData == null)
      {
        displayInfoAutoTimeOut("Load " + vsLoadID + " not found.");
        clearButtonPressed();
        return;
      }
      
      /*
       * Make sure that the load is part of an order
       */
      if (mpInventoryServer.getLoadOrder(vsLoadID).trim().length() == 0)
      {
        displayInfo("Load " + vsLoadID + " is not part of an order", "Invalid Load");
        clearButtonPressed();
        return;
      }

      // Verify that all load line items have an order
      // Set a flag if all orders are not for the same customer
      Set<String> vpOrderSet = new HashSet<String>();
      Set<String> vpCustomerSet = new HashSet<String>();

      List<Map> vpLineItemList = mpInventoryServer.getLoadLineItemDataListByLoadID(vsLoadID);

      // Create a set of unique order ID's contained in the load line item list
      for(Map vpLineHash: vpLineItemList)
      {
        LoadLineItemData vpLineItemData = Factory.create(LoadLineItemData.class);
        vpLineItemData.dataToSKDCData(vpLineHash);
        String vsOrderID = vpLineItemData.getOrderID();
        if (vsOrderID.length() != 0)
        {
          vpOrderSet.add(vsOrderID);
        }
      }
      
      // Create a set of unique customers contained in the set of order ID's
      if (vpOrderSet.size() > 0)
      {
        OrderHeaderData vpOrderHeader;
        for(String vsOrder: vpOrderSet)
        {
          vpOrderHeader = mpOrderServer.getOrderHeaderRecord(vsOrder);
          // Save the first Order Header for display purposes
          if (mpLoadOrderHeader == null)
          {
            mpLoadOrderHeader = vpOrderHeader;
            
            // Display the appropriate order information and disable the combo
            if (vpOrderSet.size() > 1)
            {
              mpOrderText.setText(MULTIPLE_ORDERS);
            }
            else
            {
              mpOrderText.setText(vpOrderHeader.getOrderID());
            }
          }
          vpCustomerSet.add(vpOrderHeader.getShipCustomer());
        }
      }

      if (mpLoadOrderHeader == null)
      {
        mpCustomerText.setText("<Overpack>");
        mpCurrentLocText.setText(vpLoadData.getWarehouse() + vpLoadData.getAddress());
        mpShippingLocText.setText(vpLoadData.getWarehouse() + vpLoadData.getAddress());
      }
      else
      {
        if (vpCustomerSet.size() > 1)
        {
          mpCustomerText.setText("<Multiple>");
        }
        else
        {
          mpCustomerText.setText(mpLoadOrderHeader.getShipCustomer());
        }
        mpCurrentLocText.setText(vpLoadData.getWarehouse() + vpLoadData.getAddress());
        mpShippingLocText.setText(mpLoadOrderHeader.getDestWarehouse() + mpLoadOrderHeader.getDestAddress());
      }
    }
    catch(Exception ve)
    {
      ve.printStackTrace();
    }
    mpConfirmLocText.setEnabled(true);

    mpStageButton.setEnabled(true);
    mpBtnSubmit.setEnabled(true);
    mpConfirmLocText.requestFocus();
  }

  /**
   * Validate the location
   * @return
   */
  boolean validLocation()
  {
    try
    {
      LocationData vpLocationData = mpLocationServer.getLocationRecord(mpConfirmLocText.getText());
      if (vpLocationData != null)
      {
        if ((vpLocationData.getLocationType() == DBConstants.LCSTAGING) ||
            (vpLocationData.getLocationType() == DBConstants.LCCONSOLIDATION) ||
            (vpLocationData.getLocationType() == DBConstants.LCSHIPPING) ||
            (vpLocationData.getLocationType() == DBConstants.LCSTATION))
        {
          return true;
        }
      }
    }
    catch(DBException ve)
    {
      ve.printStackTrace();
    }
    mpConfirmLocText.requestFocus();
    return false;
  }
  
  /**
   * Validate the confirmation location
   * @return
   */
  boolean validConfirmationLocation()
  {
    String vsConfirmationLocation = mpConfirmLocText.getText();
//    if (!vsConfirmationLocation.contains("-"))
//    {
//      String[] vasLocation = Location.parseLocation(mpConfirmLocText.getText());
//      vsConfirmationLocation = vasLocation[0] + "-" + vasLocation[1];
//    }
    if (mpShippingLocText.getText().compareTo(vsConfirmationLocation) == 0)
    {
      return true;
    }
    else
    {
      if (JOptionPane.showInternalConfirmDialog(this, 
          "The validation location does not match the shipping location.\n" +
          "The load will ship from the validation location.",
          "Validation Location Mismatch", JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
      {
        return true;
      }
      else
      {
        return false;
      }
    }
  }

  //----------------------------------------------------------------------//
  //  Action methods                                                      //
  //----------------------------------------------------------------------//
  
  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Action method to handle Clear button.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpConfirmLocText.setText("");
    clearLoadData();

    mpLoadText.setText("");
    validate();
    mpLoadText.requestFocus();
  }

  /**
   * Clear the load data
   */
  private void clearLoadData()
  {
    mpOrderText.setText("");
    mpCustomerText.setText("");
    mpCurrentLocText.setText("");
    mpShippingLocText.setText("");
    
    mpConfirmLocText.setEnabled(false);
    mpStageButton.setEnabled(false);
    mpBtnSubmit.setEnabled(false);
  }

  /**
   *  Action method to handle Stage button.
   */
  private void stageButtonPressed()
  {
    if (mpStageButton.isEnabled())
    {
      if (validLocation())
      {
        try
        {
          String vsConfirmationLocation = mpConfirmLocText.getText();
          if (!vsConfirmationLocation.contains("-"))
          {
            String[] vasLocation = Location.parseLocation(mpConfirmLocText.getText());
            vsConfirmationLocation = vasLocation[0] + "-" + vasLocation[1];
          }
          mpLoadServer.stageLoad(mpLoadText.getText(), vsConfirmationLocation,
              mpAllowMultipleCust.isSelected());
          displayInfoAutoTimeOut(mpLoadText.getText() + " successfully staged");
          clearButtonPressed();
        }
        catch(DBException ve)
        {
          displayError(ve.getMessage());
        }
      }
      else
      {
        if (mpConfirmLocText.getText().length() == 0)
        {
          displayError("Confirmed Location must be entered");
        }
        else
        {
          displayError(mpConfirmLocText.getText() + " is not a valid staging location");
        }
      }
    }
  }

  /**
   *  Action method to handle Ship button.
   */
  @Override
  protected void okButtonPressed()
  {
    String vsLoadID = mpLoadText.getText();
    if (mpBtnSubmit.isEnabled())
    {
      if (validLocation())
      {
        if (validConfirmationLocation())
        {
          try
          {
            String vsConfirmationLocation = mpConfirmLocText.getText();
            if (!vsConfirmationLocation.contains("-"))
            {
              String[] vasLocation = Location.parseLocation(mpConfirmLocText.getText());
              vsConfirmationLocation = vasLocation[0] + "-" + vasLocation[1];
            }
            if (!mpLoadServer.isLoadMoveStatus(vsLoadID, DBConstants.STAGED))
            {
              mpLoadServer.stageLoad(vsLoadID, vsConfirmationLocation,
                  mpAllowMultipleCust.isSelected());
            }

            mpLoadServer.shipLoad(vsLoadID, vsConfirmationLocation,
                mpAllowMultipleCust.isSelected());
            displayInfoAutoTimeOut(vsLoadID + " successfully marked for shipping");
            clearButtonPressed();
          }
          catch(DBException ve)
          {
            displayError(ve.getMessage());
          }
        }
      }
      else
      {
        displayError(mpConfirmLocText.getText() + " is not a valid shipping location");
      }
    }
  }
}
