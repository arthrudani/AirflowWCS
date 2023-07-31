package com.daifukuamerica.wrxj.swingui.carrier;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dbadapter.data.CarrierData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 *    Sets up the Carrier add internal frame.  It fills in the contents of
 *    a JPanel before adding it to the Internal frame.  This method then
 *    returns the Internal Frame reference to the caller so that it can be
 *    added to the desktop when appropriate.
 *
 * @author       R.M.
 * @version      1.0
 * <BR>Created: 24-Nov-04<BR>
 *     Copyright (c) 2004<BR>
 *     Company:  Daifuku America Corporation
 */
@SuppressWarnings("serial")
public class AddCarrierFrame extends DacInputFrame
{
  protected StandardCarrierServer mpCarServer;

  protected SKDCTextField   mpTxtCarrierID;
  protected SKDCTextField   mpTxtCarrierName;
  protected SKDCTextField   mpTxtCarrierContact;
  protected SKDCTextField   mpTxtCarrierPhone;
  protected StationComboBox mpCmbStation;
  
  public AddCarrierFrame(StandardCarrierServer ipCarServer)
  {
    super("Add Carrier", "Carrier Information");
    mpCarServer = ipCarServer;
    addInputs();
  }

  /**
   * Add the input labels/fields
   */
  protected void addInputs() 
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
      displayError(dbe.getMessage(), "Unable to get Stations");
    }
    
    addInput("Carrier ID", mpTxtCarrierID);
    addInput("Carrier Name", mpTxtCarrierName);
    addInput("Carrier Contact", mpTxtCarrierContact);
    addInput("Carrier Phone", mpTxtCarrierPhone);
    addInput("Assigned Station", mpCmbStation);

    useAddButtons();
  }

  /**
   * Handle the submit button
   */
  @Override
  protected void okButtonPressed()
  {
    CarrierData cadata = Factory.create(CarrierData.class);
    cadata.clear();       // Make sure everything is defaulted to begin with.
                                       
    //  Carrier ID is a required field.
    if (mpTxtCarrierID.getText().trim().length() != 0)
    {
      cadata.setCarrierID(mpTxtCarrierID.getText());
      
      // Attach Key object to the data class also for later use.
      cadata.setKey(CarrierData.CARRIERID_NAME, mpTxtCarrierID.getText());
    }
    else
    {
      displayInfoAutoTimeOut("Carrier ID is required", "Submit Error");
      return;
    }

    if (mpTxtCarrierName.getText().trim().length() != 0)
    {
      cadata.setCarrierName(mpTxtCarrierName.getText());
    }
    if (mpTxtCarrierContact.getText().trim().length() != 0)
    {
      cadata.setCarrierContact(mpTxtCarrierContact.getText());
    }
    if (mpTxtCarrierPhone.getText().trim().length() != 0)
    {
      cadata.setCarrierPhone(mpTxtCarrierPhone.getText());
    }
    if (mpCmbStation.getSelectedStation().trim().length() != 0)
    {
      cadata.setStation(mpCmbStation.getSelectedStation());
    }
    
    /*
     * Make sure it doesn't exist
     */
    try
    {
      if (mpCarServer.CarrierExists(cadata) == true)
      {
        displayInfoAutoTimeOut("Carrier " + cadata.getCarrierID()
            + " already exists", "Add Carrier Error");
      }
    }
    catch(Exception exc)
    {
      exc.printStackTrace(System.out);
      displayError(exc.getMessage(), "DB Error");
    }
    
    /*
     * Add it
     */
    try
    {
      // Send it to the Carrier Server
      mpCarServer.addCarrier(cadata);
      displayInfoAutoTimeOut("Carrier " + cadata.getCarrierID() + " Added");
      
      // Get fresh data with all fields filled in.
      try
      {
        CarrierData newdata = mpCarServer.getCarrierRecord(cadata);
        changed(null, newdata);  // Send it to the Carrier Frame.
        clearButtonPressed();
      }
      catch(Exception e)
      {
        displayError(e.getMessage());
      }
    }
    catch(DBException exc)
    {
      displayError(exc.getMessage(), "Add Error");
    }
    return;
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Add Carrier Frame.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpTxtCarrierID.setText("");
    mpTxtCarrierName.setText("");
    mpTxtCarrierContact.setText("");
    mpTxtCarrierPhone.setText("");
    mpCmbStation.setSelectedIndex(0);
    mpTxtCarrierID.requestFocus();
  }

  /**
   *  Cancel Button handler.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}
