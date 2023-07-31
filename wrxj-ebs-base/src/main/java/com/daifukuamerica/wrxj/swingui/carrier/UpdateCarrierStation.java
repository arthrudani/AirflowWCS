package com.daifukuamerica.wrxj.swingui.carrier;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCarrierServer;
import com.daifukuamerica.wrxj.dbadapter.data.CarrierData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;


/**
 * A screen class for updating carrier Stations.
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateCarrierStation extends DacInputFrame
{
  private SKDCComboBox    carrierComboBox;
  private StationComboBox stationComboBox;

  private String carrierName = "";
  private String stationName = "";

  private StandardCarrierServer mpCarServer = Factory.create(StandardCarrierServer.class);

  /**
   *  Create Carrier Station screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateCarrierStation(String isTitle)
  {
    super(isTitle, "Assignment Information");
    jbInit();
  }

  /**
   *  Create default Carrier Station screen class.
   */
  public UpdateCarrierStation()
  {
    this("");
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param carrierID Carrier ID.
   *  @param StationID Station ID.
   */
  public void setModify(String carrierID, String stationID)
  {
    carrierName = carrierID;
    stationName = stationID;
    
    carrierComboBox.setEnabled(false);
    
    useModifyButtons();
    clearButtonPressed();
  }

  /**
   *  Method to populate the carrier combo box.
   */
  private void carrierFill()
  {
    try
    {
      String[] carrierList = mpCarServer.getCarrierChoices();
      carrierComboBox.setComboBoxData(carrierList);
    }
    catch (DBException e)
    {
      displayWarning(e.getMessage(), "Carrier List Warning");
    }
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  private void jbInit()
  {
    carrierComboBox = new SKDCComboBox();
    stationComboBox = new StationComboBox();

    carrierComboBox.setEditable(false);
    stationComboBox.setEditable(false);

    carrierFill();
    try
    {
      stationComboBox.fillWithOutputs(SKDCConstants.EMPTY_VALUE);
    }
    catch (DBException dbe)
    {
      displayError(dbe.getMessage(), "Unable to get Stations");
    }
    
    addInput("Carrier ID", carrierComboBox);
    addInput("Station ID", stationComboBox);
  }    

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpCarServer.cleanUp();
    mpCarServer = null;
  }

  /**
   *  Action method to handle Clear button.
   *
   *  @param e Action event.
   *
   */
  @Override
  public void clearButtonPressed()
  {
    carrierComboBox.setSelectedItem(carrierName);
    stationComboBox.setSelectedStation(stationName);
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new Carrier Station to the database.
   *
   *  @param e Action event.
   */
  @Override
  public void okButtonPressed()
  {
    if (carrierComboBox.getText().length() <= 0)  // required
    {
      displayError("Carrier ID is required");
      return;
    }
    
    CarrierData csData = Factory.create(CarrierData.class);
    try
    {
      csData.setKey(CarrierData.CARRIERID_NAME, carrierComboBox.getText());
      csData = mpCarServer.getCarrierRecord(csData);
    }
    catch (DBException e2)
    {
      displayError("Unable to get Carrier Station data");
      return;
    }

    if (csData == null)
    {
      displayError("Carrier " + carrierComboBox.getText() + " not found.");
      return;
    }

    try
    {
      csData.clear();
      csData.setKey(CarrierData.CARRIERID_NAME, carrierComboBox.getText());
      csData.setStation(stationComboBox.getSelectedStation());
      mpCarServer.modifyCarrier(csData);
      this.changed();
      displayInfoAutoTimeOut("Carrier station for " + carrierComboBox.getText() 
         + " updated");
    }
    catch (DBException e2)
    {
      displayError("Error updating Carrier " + carrierComboBox.getText());
    }
    close();
  }

}
