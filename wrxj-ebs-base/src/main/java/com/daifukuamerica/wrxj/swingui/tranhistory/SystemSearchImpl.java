package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.swingui.station.StationComboBox;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 * Sets up the System History search internal frame.
 * 
 * @author A.D.
 * @version 1.0 <BR>
 *          Created: 12-May-03<BR>
 *          Copyright (c) 2003<BR>
 *          Company: SKDC Corporation
 */
@SuppressWarnings("serial")
public class SystemSearchImpl extends TransactionSearch
{
  private SKDCComboBox mpDeviceCombo;
  private LocationPanel mpTxtLocation;
  private SKDCTextField mpTxtRoute;
  private StationComboBox mpStationCombo;

  /**
   * Constructor
   * 
   * @param inInterfaceType
   * @param ianSystemActions
   */
  public SystemSearchImpl(int inInterfaceType, int[] ianSystemActions)
  {
    super("System History Search", ianSystemActions);
  }

  /**
   * Build the screen
   * 
   * @param ianActionTypes
   */
  @Override
  protected void buildScreen(int[] ianActionTypes) throws NoSuchFieldException
  {
    mpActionTypeCombo = new SKDCTranComboBox(
        TransactionHistoryData.TRANTYPE_NAME, ianActionTypes, true);
    mpBeginDateField = new SKDCDateField(false);
    mpBeginDateField.setDate(mpBegDate);
    mpEndingDateField = new SKDCDateField(false);
    mpDeviceCombo = new SKDCComboBox();
    mpTxtLocation = Factory.create(LocationPanel.class);
    mpTxtLocation.setWarehouseList(DBConstants.REGULAR, true);
    mpTxtRoute = new SKDCTextField(TransactionHistoryData.ROUTEID_NAME);
    mpStationCombo = new StationComboBox();
    fillComboBoxes();
    
    addInput("Action Type:", mpActionTypeCombo);
    addInput("Beginning Date:", mpBeginDateField);
    addInput("Ending Date:", mpEndingDateField);
    addInput("Device:", mpDeviceCombo);
    addInput("Location:", mpTxtLocation);
    addInput("Route:", mpTxtRoute);
    addInput("Station:", mpStationCombo);
  }

  /**
   * Fill the device combo box
   */
  private void fillComboBoxes()
  {
    StandardLocationServer vpLocServer = Factory.create(StandardLocationServer.class);
    
    mpDeviceCombo.setDisplayAllEnabled(true);
    mpStationCombo.setDisplayAllEnabled(true);
    try
    {
      String[] vasDevices = vpLocServer.getDeviceIDList(false);
      mpDeviceCombo.setComboBoxData(vasDevices);
      mpStationCombo.fill(new int[0], SKDCConstants.NO_PREPENDER);
    }
    catch (DBException exc)
    {
      logAndDisplayException("Error populating combo boxes", exc);
    }
  }
  
  /**
   * Processes Search Order request.
   */
  @Override
  protected void okButtonPressed()
  {
    mpTNSearchData.clear();

    try
    {
      mpTNSearchData.setTranTypeKey(mpActionTypeCombo.getIntegerValue());
    }
    catch (NoSuchFieldException e)
    {
      displayInfo(e.getMessage(), "Translation");
      return;
    }
    mpTNSearchData.setTranCategoryKey(DBConstants.SYSTEM_TRAN);
    mpTNSearchData.setDateRangeKey(mpBeginDateField.getDate(),
        mpEndingDateField.getDate());

    mpTNSearchData.setDeviceIDKey(mpDeviceCombo.getText());
    try
    {
      if (!mpTxtLocation.getLocationString().trim().equals(SKDCConstants.ALL_STRING))
      {
        mpTNSearchData.setLocationKey(mpTxtLocation.getLocationString());
      }
    }
    catch (DBException e)
    {
      displayError(e.getMessage());
      return;
    }
    if (mpTxtRoute.getText().trim().length() != 0)
    {
      mpTNSearchData.setRouteIDKey(mpTxtRoute.getText());
    }
    mpTNSearchData.setStationKey(mpStationCombo.getSelectedStation());

    mpTNSearchData.addOrderByColumn(TransactionHistoryData.TRANSDATETIME_NAME);
    mpTNSearchData.addOrderByColumn(AbstractSKDCDataEnum.ID.getName());

    checkForData();
  }

  /**
   * Clear Button handler.
   */
  @Override
  protected void clearButtonPressed()
  {
    mpActionTypeCombo.setSelectedIndex(0);
    mpBeginDateField.setDate(mpBegDate);
    mpEndingDateField.setDate();
    mpDeviceCombo.setSelectedIndex(0);
    mpTxtLocation.reset();
    mpTxtRoute.setText("");
    mpStationCombo.setSelectedIndex(0);
  }
}
