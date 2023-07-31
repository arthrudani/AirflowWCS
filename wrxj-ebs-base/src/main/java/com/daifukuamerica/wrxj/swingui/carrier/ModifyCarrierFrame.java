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
 *    Sets up the Carrier modify internal frame.  It fills in the contents of
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
public class ModifyCarrierFrame extends DacInputFrame
{
  private CarrierData    mpCurrentData    = Factory.create(CarrierData.class);
  
  private StandardCarrierServer mpCarServer= null;
  
  protected SKDCTextField   mpTxtCarrierID;
  protected SKDCTextField   mpTxtCarrierName;
  protected SKDCTextField   mpTxtCarrierContact;
  protected SKDCTextField   mpTxtCarrierPhone;
  protected StationComboBox mpCmbStation;

  public ModifyCarrierFrame(StandardCarrierServer ipCarServer)
  {
    super("Modify Carrier", "Carrier Information");
    mpCarServer = ipCarServer;
    addInputs();
  }

  /**
   *   Load the modify screen with the current selected row of data from the
   *   table.  It is assumed that this frame has already been built when this
   *   method is called.
   */
  public void setCurrentData(CarrierData ipCurrentData)
  {
    mpCurrentData = ipCurrentData;

    mpTxtCarrierID.setText(ipCurrentData.getCarrierID());
    mpTxtCarrierName.setText(ipCurrentData.getCarrierName());
    mpTxtCarrierContact.setText(ipCurrentData.getCarrierContact());
    mpTxtCarrierPhone.setText(ipCurrentData.getCarrierPhone());
    mpCmbStation.setSelectedStation(ipCurrentData.getStation());
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

    mpTxtCarrierID.setEnabled(false);
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
    
    useModifyButtons();
  }

  /**
   *  Processes Carrier modify request.
   */
  @Override
  protected void okButtonPressed()
  {
    CarrierData newCAdata = Factory.create(CarrierData.class);
    newCAdata.clear();      // Make sure everything is defaulted to begin with.

    newCAdata.setCarrierName(mpTxtCarrierName.getText());
    newCAdata.setCarrierContact(mpTxtCarrierContact.getText());
    newCAdata.setCarrierPhone(mpTxtCarrierPhone.getText());
    newCAdata.setStation(mpCmbStation.getSelectedStation());

    try
    {                                  // Set the key for the modify.
      newCAdata.setKey(CarrierData.CARRIERID_NAME, mpTxtCarrierID.getText());
      String mesg = mpCarServer.modifyCarrier(newCAdata);
                                       // Get fresh data for screen update.
      CarrierData newdata = mpCarServer.getCarrierRecord(newCAdata);
      changed(null, newdata);
      displayInfoAutoTimeOut(mesg, "Modify Confirmation");
      Thread.sleep(30);
      close();
    }
    catch(DBException e)
    {
      displayError(e.getMessage(), "Carrier Modify Error");
    }
    catch(InterruptedException e)
    {  // ignore it!.
    }
    return;
  }

  /**
   *  Clear button event handler.  This method resets all fields in the
   *  Modify Carrier dialog.
   */
  @Override
  protected void clearButtonPressed()
  {
    setCurrentData(mpCurrentData);
    mpTxtCarrierName.requestFocus();
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
