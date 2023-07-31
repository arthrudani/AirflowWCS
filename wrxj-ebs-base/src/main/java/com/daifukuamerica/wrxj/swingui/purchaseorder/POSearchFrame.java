package com.daifukuamerica.wrxj.swingui.purchaseorder;

import com.daifukuamerica.wrxj.dataserver.standard.StandardPoReceivingServer;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description: Sets up the PO Search internal frame
 */
@SuppressWarnings("serial")
public class POSearchFrame extends DacInputFrame
{
  protected POHLSearch posearch = new POHLSearch();
  protected StandardPoReceivingServer mpPOServer;

  protected SKDCTextField mpTxtPOID;
  protected SKDCTranComboBox mpPOStatusCombo;
  protected SKDCTextField mpTxtItem;
  protected SKDCTextField mpTxtLot;
  protected SKDCTextField mpTxtVendor;

  /**
   * Constructor
   * 
   * @param ipPOServer
   */
  public POSearchFrame(StandardPoReceivingServer ipPOServer)
  {
    super("Expected Receipt Search", "Expected Receipt Information");
    mpPOServer = ipPOServer;

    buildScreen();
  }

  /**
   * Builds the screen
   */
  protected void buildScreen()
  {
    /*
     * Initialize
     */
    mpTxtPOID   = new SKDCTextField(PurchaseOrderLineData.ORDERID_NAME);
    try
    {
      mpPOStatusCombo = new SKDCTranComboBox(
          PurchaseOrderHeaderData.PURCHASEORDERSTATUS_NAME,
          DBConstants.EREXPECTED, true);
    }
    catch (NoSuchFieldException nsfe)
    {
      logAndDisplayException(nsfe);
      mpPOStatusCombo = new SKDCTranComboBox();
    }
    mpTxtItem   = new SKDCTextField(PurchaseOrderLineData.ITEM_NAME);
    mpTxtLot    = new SKDCTextField(PurchaseOrderLineData.LOT_NAME);
    mpTxtVendor = new SKDCTextField(PurchaseOrderHeaderData.VENDORID_NAME);
    
    /*
     * Add to screen
     */
    addInput("Expected Receipt ID:", mpTxtPOID);
    addInput("Expected Receipt Status:", mpPOStatusCombo);
    addInput("Item:", mpTxtItem);
    addInput("Lot:", mpTxtLot);
    addInput("Vendor:", mpTxtVendor);
  }

  /**
   * Processes Search PO request.
   * 
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#okButtonPressed()
   */
  @Override
  protected void okButtonPressed()
  {
    posearch.clear();           // Make sure everything is defaulted
                                // to begin with.
    String sPOID = mpTxtPOID.getText();
    String sItem = mpTxtItem.getText();
    String sLot = mpTxtLot.getText();
    String sVendor = mpTxtVendor.getText();

    if (sPOID.length() > 0)
    {
      posearch.podata.setKey(PurchaseOrderHeaderData.ORDERID_NAME, sPOID,
          KeyObject.LIKE);
    }

    if (sVendor.length() > 0)
    {
      posearch.podata.setKey(PurchaseOrderHeaderData.VENDORID_NAME, sVendor,
          KeyObject.LIKE);
    }

    if (sItem.length() > 0)
    {
      posearch.poldata.setKey(PurchaseOrderLineData.ITEM_NAME, sItem,
          KeyObject.LIKE);
    }

    if (sLot.length() > 0)
    {
      posearch.poldata.setKey(PurchaseOrderLineData.LOT_NAME, sLot,
          KeyObject.LIKE);
    }

    int iPOStatus = SKDCConstants.ALL_INT;
    try
    {
      iPOStatus = mpPOStatusCombo.getIntegerValue();
      posearch.podata.setKey(PurchaseOrderHeaderData.PURCHASEORDERSTATUS_NAME,
          mpPOStatusCombo.getIntegerObject(), KeyObject.EQUALITY);
    }
    catch (NoSuchFieldException e)
    {
      displayWarning(e.getMessage(), "Translation Error");
      return;
    }
    
                 // indicate the change and search parameters so the screen
                 // can get new data based on the search criteria.
    posearch.podata.setOrderID(sPOID);
    posearch.poldata.setItem(sItem);
    posearch.poldata.setLot(sLot);
    posearch.podata.setOrderStatus(iPOStatus);
    posearch.podata.setVendorID(sVendor);

    changed(null, posearch);
    mpTxtPOID.requestFocus();
  }

  /**
   * Clear Button handler.
   * 
   * @see com.daifukuamerica.wrxj.swing.DacInputFrame#clearButtonPressed()
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTxtPOID.setText("");
    mpTxtItem.setText("");
    mpTxtLot.setText("");
    mpTxtVendor.setText("");
    mpPOStatusCombo.resetDefaultSelection();
    mpTxtPOID.requestFocus();
  }
}
