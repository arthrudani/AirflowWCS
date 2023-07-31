package com.daifukuamerica.wrxj.swingui.carrier;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dbadapter.data.CarrierData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.KeyObject;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.List;

/**
 * Description:<BR>
 *    Sets up the Carrier Search internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       R.M.
 * @version      1.0
 * <BR>Created: 29-Nov-04<BR>
 *     Copyright (c) 2004<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class CarrierSearchFrame extends DacInputFrame
{
  private CarrierData  searchFields;
  private StandardCarrierServer    mpCarServer;
  private List            datalist;
  private CarrierData     cadata      = Factory.create(CarrierData.class);
  
  private SKDCTextField   mpTxtCarrierID;
  private SKDCTextField   mpTxtCarrierName;
  private SKDCTextField   mpTxtCarrierContact;
  private SKDCTextField   mpTxtCarrierPhone;
  private StationComboBox mpCmbStation; 

  public CarrierSearchFrame(StandardCarrierServer ipCarServer)
  {
    super("Carrier Search", "Carrier Information");
    mpCarServer = ipCarServer;
    buildScreen();
  }

  public CarrierData getSearchCriteria()
  {
    return(searchFields);
  }

  /**
   * Add the input labels/fields
   */
  protected void buildScreen() 
  {
    mpTxtCarrierID      = new SKDCTextField(CarrierData.CARRIERID_NAME);
    mpTxtCarrierName    = new SKDCTextField(CarrierData.CARRIERNAME_NAME);
    mpTxtCarrierContact = new SKDCTextField(CarrierData.CARRIERCONTACT_NAME);
    mpTxtCarrierPhone   = new SKDCTextField(CarrierData.CARRIERPHONE_NAME);
    mpCmbStation        = new StationComboBox();
    
    try
    {
      mpCmbStation.fillWithOutputs(SKDCConstants.EMPTY_VALUE);
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage());
    }

    addInput("Carrier ID", mpTxtCarrierID);
    addInput("Carrier Name", mpTxtCarrierName);
    addInput("Carrier Contact", mpTxtCarrierContact);
    addInput("Carrier Phone", mpTxtCarrierPhone);
    addInput("Assigned Station", mpCmbStation);
    
    useSearchButtons();
  }

  /**
   * Processes Search Carrier request.
   *
   */
  @Override
  protected void okButtonPressed()
  {
    cadata.clear();         // Make sure everything is defaulted to begin with.

    // Add Carrier entry as Key.
    if (mpTxtCarrierID.getText().trim().length() != 0)
    {
      cadata.setKey(CarrierData.CARRIERID_NAME, mpTxtCarrierID.getText(),
          KeyObject.LIKE);
    }

    // get Carrier Name entry
    if (mpTxtCarrierName.getText().trim().length() != 0)
    {
      cadata.setKey(CarrierData.CARRIERNAME_NAME, mpTxtCarrierName.getText(),
          KeyObject.LIKE);
    }

    // get Carrier Contact entry
    if (mpTxtCarrierContact.getText().trim().length() != 0)
    {
      cadata.setKey(CarrierData.CARRIERCONTACT_NAME, mpTxtCarrierContact.getText(),
          KeyObject.LIKE);
    }

    // get Carrier Phone entry
    if (mpTxtCarrierPhone.getText().trim().length() != 0)
    {
      cadata.setKey(CarrierData.CARRIERPHONE_NAME, mpTxtCarrierPhone.getText(),
          KeyObject.LIKE);
    }
    
    // get Station entry
    if (mpCmbStation.getSelectedStation().length() != 0)
    {
      cadata.setKey(CarrierData.STATIONNAME_NAME, mpCmbStation.getSelectedStation(),
          KeyObject.LIKE);
    }
    
    // Save off the search criteria.
    searchFields = cadata.clone();
    
    // Call the order server and fetch the data based on the search criteria.
    try
    {
      datalist = mpCarServer.getCarrierList(cadata);
      if (datalist != null && datalist.size() != 0)
      {
        // If something comes back, notify main frame of the change.
        changed(null, datalist);
        closeButtonPressed();
      }
      else
      {
        displayInfoAutoTimeOut("No Data Found.", "Search Information");
        mpTxtCarrierID.requestFocus();
      }
    }
    catch(Exception e)
    {
      displayError(e.getMessage(), "Search Information");
    }
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTxtCarrierID.setText("");
    mpTxtCarrierName.setText("");
    mpTxtCarrierContact.setText("");
    mpTxtCarrierPhone.setText("");
    mpCmbStation.setSelectedIndex(0);
  }
}
