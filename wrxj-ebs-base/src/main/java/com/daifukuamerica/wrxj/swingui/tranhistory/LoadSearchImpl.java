package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import com.daifukuamerica.wrxj.swingui.location.LocationPanel;
import com.daifukuamerica.wrxj.util.SKDCConstants;

/**
 * Description:<BR>
 * Sets up the Load History search internal frame.
 * 
 * @author A.D.
 * @version 1.0 <BR>
 *          Created: 12-May-03<BR>
 *          Copyright (c) 2003<BR>
 *          Company: SKDC Corporation
 */
@SuppressWarnings("serial")
public class LoadSearchImpl extends TransactionSearch
{
  private SKDCTextField mpTxtLoad;
  private LocationPanel mpTxtLocation;
  private LocationPanel mpTxtToLocation;
  private SKDCTextField mpTxtRoute;

  /**
   * Constructor
   * 
   * @param inInterfaceType
   * @param ianLoadActions
   */
  public LoadSearchImpl(int inInterfaceType, int[] ianLoadActions)
  {
    super("Load History Search", ianLoadActions);
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
    mpTxtLoad = new SKDCTextField(TransactionHistoryData.LOADID_NAME);
    mpTxtRoute = new SKDCTextField(TransactionHistoryData.ROUTEID_NAME);
    mpTxtLocation = Factory.create(LocationPanel.class);
    mpTxtLocation.setWarehouseList(DBConstants.REGULAR, true);
    mpTxtToLocation = Factory.create(LocationPanel.class);
    mpTxtToLocation.setWarehouseList(DBConstants.REGULAR, true);

    addInput("Action Type:", mpActionTypeCombo);
    addInput("Beginning Date:", mpBeginDateField);
    addInput("Ending Date:", mpEndingDateField);
    addInput("Load:", mpTxtLoad);
    addInput("Location:", mpTxtLocation);
    addInput("To-Location:", mpTxtToLocation);
    addInput("Route:", mpTxtRoute);
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
    mpTNSearchData.setTranCategoryKey(DBConstants.LOAD_TRAN);
    mpTNSearchData.setDateRangeKey(mpBeginDateField.getDate(),
        mpEndingDateField.getDate());

    // Add From Load entry as Key.
    if (mpTxtLoad.getText().trim().length() != 0)
    {
      mpTNSearchData.setLoadKey(mpTxtLoad.getText());
    }
    // Add Location as Key.
    try
    {
      if (!mpTxtLocation.getLocationString().trim().equals(SKDCConstants.ALL_STRING))
      {
        mpTNSearchData.setLocationKey(mpTxtLocation.getLocationString());
      }
      if (!mpTxtToLocation.getLocationString().trim().equals(SKDCConstants.ALL_STRING))
      {
        mpTNSearchData.setToLocationKey(mpTxtToLocation.getLocationString());
      }
    }
    catch (DBException e)
    {
      displayError(e.getMessage());
      return;
    }
    // Add Route as Key.
    if (mpTxtRoute.getText().trim().length() != 0)
    {
      mpTNSearchData.setRouteIDKey(mpTxtRoute.getText());
    }

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
    mpTxtLoad.setText("");
    mpTxtLocation.reset();
    mpTxtRoute.setText("");
  }
}
