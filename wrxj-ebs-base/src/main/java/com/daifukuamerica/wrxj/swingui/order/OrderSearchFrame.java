package com.daifukuamerica.wrxj.swingui.order;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardOrderServer;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBInfo;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.ReleaseToCodeField;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description:<BR>
 *    Sets up the Order Search internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       A.D.    Version 1.0.  Original version.
 * @author       RKM     Version 2.0   Added Tabbed Panes.
 * @author       mandrus Version 3.0   DacInputFrame, only for searching again
 * @version      3.0
 * <BR>Created: 04-Jun-02<BR>
 *     Copyright (c) 2002<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class OrderSearchFrame extends DacInputFrame
{
  private OrderHeaderData  searchFields;
  private OrderLineData    searchLines;

  private StandardCarrierServer mpCarServer;
  private StandardOrderServer   mpOrderServer;

  private static long ONE_YEAR_SECONDS = 31536000;
  private static long ONE_MONTH_SECONDS= 2592000;

  private OrderHeaderData  ohdata      = Factory.create(OrderHeaderData.class);
  private OrderLineData    oldata      = Factory.create(OrderLineData.class);
  private List<Map>        datalist;

  protected SKDCTextField      txtOrderID;
  private SKDCTextField      txtDescription;
  protected SKDCTextField      txtItem;
  protected SKDCTextField      txtLot;
  private SKDCTranComboBox   orderTypeCombo;
  private SKDCTranComboBox   orderStatusCombo;
  private SKDCDateField      txtBegDate;
  private SKDCDateField      txtEndDate;
  private SKDCComboBox       cmbCarrierID;
  private Date               begDate;
  private Date               endDate;
  private SKDCIntegerField   txtPriority;
  private ReleaseToCodeField releaseToCode;
  protected StationComboBox    cmbItemDestStation;
  private LocationPanel      mpDestPanel;
  private SKDCTextField      txtOrderMessage;

  /**
   * Constructor
   */
  public OrderSearchFrame()
  {
    super("Order Search", "Order Information");
    mpOrderServer = Factory.create(StandardOrderServer.class);
    mpCarServer = Factory.create(StandardCarrierServer.class);

    buildScreen();
  }

  /**
   * Build the screen
   */
  protected void buildScreen()
  {
    txtBegDate      = new SKDCDateField(true);
    txtEndDate      = new SKDCDateField(true);
    begDate         = new Date();
    endDate         = new Date();
    txtOrderID      = new SKDCTextField(OrderHeaderData.ORDERID_NAME);
    txtDescription  = new SKDCTextField(OrderHeaderData.DESCRIPTION_NAME);
    txtItem         = new SKDCTextField(OrderLineData.ITEM_NAME);
    txtLot          = new SKDCTextField(OrderLineData.ORDERLOT_NAME);
    txtPriority     = new SKDCIntegerField(7,1);
    txtOrderMessage = new SKDCTextField(40);
    releaseToCode   = new ReleaseToCodeField();

    // Build Order Type Combo Box.
    // Build Order Type Combo Box.
//    int[] orderStatus = { DBConstants.ALLOCATENOW, DBConstants.HOLD,
//                          DBConstants.KILLED,      DBConstants.PICKCOMP,
//                          DBConstants.READY,       DBConstants.REALLOC,
//                          DBConstants.SCHEDULED,   DBConstants.SHORT,
//                          DBConstants.DONE };
    try
    {
      orderTypeCombo = new SKDCTranComboBox(OrderHeaderData.ORDERTYPE_NAME, true);
      orderStatusCombo = new SKDCTranComboBox(OrderHeaderData.ORDERSTATUS_NAME, true);
//      orderStatusCombo = new SKDCTranComboBox(OrderHeaderData.ORDERSTATUS_NAME,
//                                              orderStatus, true);
    }
    catch(java.lang.NoSuchFieldException e)
    {
      displayWarning("Translation Error");
    }

    begDate.setTime((begDate.getTime() - (ONE_YEAR_SECONDS* 1000)));
    endDate.setTime((endDate.getTime() + (ONE_MONTH_SECONDS*1000)));
    txtBegDate.setDate(begDate);
    txtEndDate.setDate(endDate);

    try
    {
      /*
       * Stations
       */
      cmbItemDestStation = new StationComboBox();
      cmbItemDestStation.fillWithAllOutputs(SKDCConstants.ALL_STRING);
      cmbItemDestStation.setSelectedIndex(0);  // ALL

      /*
       * Carriers
       */
      String[] carrierList = mpCarServer.getCarrierChoices();
      cmbCarrierID = new SKDCComboBox(carrierList, true);
    }
    catch (DBException e)
    {
      displayWarning(e.getMessage());
    }

    txtOrderMessage.setMaxColumns(DBInfo.getFieldLength(OrderHeaderData.ORDERMESSAGE_NAME));

    mpDestPanel = Factory.create(LocationPanel.class);
    mpDestPanel.setWarehouseList(LocationPanel.WTYPE_ALL, true);
    mpDestPanel.setRackFormat(false);

    txtPriority.setText("");

    addInput("Order ID", txtOrderID);
    addInput("Description", txtDescription);
    addInput("Item", txtItem);
    addInput("Lot", txtLot);
    addInput("Type", orderTypeCombo);
    addInput("Status", orderStatusCombo);
    addInput("Beginning Date", txtBegDate);
    addInput("Ending Date", txtEndDate);
    addInput("Priority", txtPriority);
    if (ReleaseToCodeField.useReleaseToCode())
      addInput("Release-to Code", releaseToCode);
    addInput("Destination Station", cmbItemDestStation);
    addInput("Destination Location", mpDestPanel);
    if (mpCarServer.hasCarriersDefined())
      addInput("Carrier", cmbCarrierID);
    addInput("Message", txtOrderMessage);

    /*
     * Hide these for now...
     */
    setInputVisible(txtOrderMessage, false);
    setInputVisible(mpDestPanel, false);

    useSearchButtons();
  }

  public OrderHeaderData getSearchCriteria()
  {
    return(searchFields);
  }

  public OrderLineData getSearchLine()
  {
    return(searchLines);
  }

  /*===========================================================================
                Methods for event handling go in this section.
  ===========================================================================*/
  /**
   * Processes Search Order request.
   */
  @Override
  protected void okButtonPressed()
  {
    boolean useol = false;
    oldata.clear();
    ohdata.clear();                    // Make sure everything is defaulted
                                       // to begin with.

    // Add Order entry as Key.
    if (txtOrderID.getText().trim().length() != 0)
    {
      ohdata.setKey(OrderHeaderData.ORDERID_NAME, txtOrderID.getText(),
          KeyObject.LIKE);
    }

    // Add Order description as Key.
    if (txtDescription.getText().trim().length() != 0)
    {
      ohdata.setKey(OrderHeaderData.DESCRIPTION_NAME, txtDescription.getText(),
          KeyObject.LIKE);
    }

    // get Item entry
    if (txtItem.getText().trim().length() != 0)
    {
      useol = true;
      oldata.setKey(OrderLineData.ITEM_NAME, txtItem.getText(), KeyObject.LIKE);
    }

    //  get Lot entry
    if (txtLot.getText().trim().length() != 0)
    {
      useol = true;
      oldata.setKey(OrderLineData.ORDERLOT_NAME, txtLot.getText(), KeyObject.LIKE);
    }

    // Add Order priority as Key.
    if (txtPriority.getText().trim().length() != 0)
    {
      ohdata.setKey(OrderHeaderData.PRIORITY_NAME, txtPriority.getText());
    }

    // Add Order Message as Key.
    if (txtOrderMessage.getText().trim().length() != 0)
    {
      ohdata.setKey(OrderHeaderData.ORDERMESSAGE_NAME, txtOrderMessage.getText(),
          KeyObject.LIKE);
    }

    // Add Order Release-to Code as Key.
    if (releaseToCode.getText().trim().length() != 0)
    {
      ohdata.setKey(OrderHeaderData.RELEASETOCODE_NAME, releaseToCode.getText(),
          KeyObject.LIKE);
    }

    // Add Order Destination Warehouse as Key.
    if (!mpDestPanel.getWarehouseString().equals(SKDCConstants.ALL_STRING))
    {
      ohdata.setKey(OrderHeaderData.DESTWAREHOUSE_NAME, mpDestPanel
          .getWarehouseString(), KeyObject.LIKE);
    }

    // Add Order Destination Address as Key.
    try
    {
      if (mpDestPanel.getAddressString().trim().length() != 0)
      {
        ohdata.setKey(OrderHeaderData.DESTADDRESS_NAME, mpDestPanel
            .getAddressString(), KeyObject.LIKE);
      }
    }
    catch (DBException e2)
    {
      displayError(e2.getMessage(), "Getting Destination Address");
    }

    // Carrier
    if (!cmbCarrierID.getSelectedItem().toString().equals(
        SKDCConstants.ALL_STRING))
    {
      ohdata.setKey(OrderHeaderData.CARRIERID_NAME, cmbCarrierID.getSelectedItem());
    }

    // Destination Station
    if (!cmbItemDestStation.getSelectedStation().equals(SKDCConstants.ALL_STRING) &&
         cmbItemDestStation.getSelectedStation().length() > 0)
    {
      ohdata.setKey(OrderHeaderData.DESTINATIONSTATION_NAME,
                    cmbItemDestStation.getSelectedStation());
    }

    // Order Type
    if (!orderTypeCombo.getSelectedItem().toString().equals(SKDCConstants.ALL_STRING))
    {
      try
      {
        ohdata.setKey(OrderHeaderData.ORDERTYPE_NAME, orderTypeCombo.getIntegerObject());
      }
      catch (NoSuchFieldException e1)
      {
        displayWarning(e1.getMessage(), "Translation Error");
      }
    }

    // Order Status
    if (!orderStatusCombo.getSelectedItem().toString().equals(SKDCConstants.ALL_STRING))
    {
      try
      {
        ohdata.setKey(OrderHeaderData.ORDERSTATUS_NAME, orderStatusCombo.getIntegerObject());
      }
      catch (NoSuchFieldException e1)
      {
        displayWarning(e1.getMessage(), "Translation Error");
      }
    }

    ohdata.setBetweenKey(OrderHeaderData.SCHEDULEDDATE_NAME, txtBegDate.getDate(), txtEndDate.getDate());

    // Save off the search criteria.
    searchFields = ohdata.clone();
    searchLines = (OrderLineData)oldata.clone();

    // Call the order server and fetch the data based on the search criteria.
    try
    {
      if(useol)
      {
        datalist = mpOrderServer.getOrderSearchList(ohdata, oldata);
        if (datalist != null && datalist.size() != 0)
        {
          // If something comes back, notify main frame of the change.
          changed(null, datalist);
          close();
        }
        else
        {
          displayInfoAutoTimeOut("No Data Found.", "Search Information");
          txtOrderID.requestFocus();
        }
      }
      else
      {
        datalist = mpOrderServer.getOrderHeaderData(ohdata);
        if (datalist != null && datalist.size() != 0)
        {
          // If something comes back, notify main frame of the change.
          changed(null, datalist);
          close();
        }
        else
        {
          displayInfoAutoTimeOut("No Data Found.", "Search Information");
          txtOrderID.requestFocus();
        }
      }
    }
    catch(Exception e)
    {
      displayError(e.getMessage(), "Search Information");
    }
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    txtOrderID.setText("");
    txtDescription.setText("");
    txtItem.setText("");
    txtLot.setText("");
    orderTypeCombo.resetDefaultSelection();
    orderStatusCombo.resetDefaultSelection();
    txtBegDate.setDate(begDate);
    txtEndDate.setDate(endDate);
    txtPriority.setText("");
    releaseToCode.setText("");
    cmbItemDestStation.setSelectedIndex(0);
    txtOrderMessage.setText("");
    mpDestPanel.reset(null, "");
    cmbCarrierID.setSelectedIndex(0);

    txtOrderID.requestFocus();
  }
}
