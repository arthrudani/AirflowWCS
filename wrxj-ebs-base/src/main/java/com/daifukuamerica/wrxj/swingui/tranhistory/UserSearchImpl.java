package com.daifukuamerica.wrxj.swingui.tranhistory;

import com.daifukuamerica.wrxj.dataserver.standard.StandardUserServer;
import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCDataEnum;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCDateField;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;

/**
 * Description:<BR>
 * Sets up the User History search internal frame
 * 
 * @author A.D.
 * @version 1.0 <BR>
 *          Created: 12-May-03<BR>
 *          Copyright (c) 2003<BR>
 *          Company: SKDC Corporation
 */
@SuppressWarnings("serial")
public class UserSearchImpl extends TransactionSearch
{
  private SKDCComboBox mpComboUserID;
  private SKDCComboBox mpComboRole;
  private SKDCTextField mpTxtDeviceID;
  private SKDCTextField mpTxtStation;

  /**
   * Constructor
   * 
   * @param inInterfaceType
   * @param ianUserActions
   */
  public UserSearchImpl(int inInterfaceType, int[] ianUserActions)
  {
    super("User History Search", ianUserActions);
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
    StandardUserServer vpUserServ = Factory.create(StandardUserServer.class);
    try
    {
      mpComboUserID = new SKDCComboBox(vpUserServ.getEmployeeNameList(), true);
      mpComboRole = new SKDCComboBox(vpUserServ.getRoleNameList(""), true);
    }
    catch (DBException e)
    {
      displayError(e.getMessage());
      return;
    }
    mpTxtDeviceID = new SKDCTextField(TransactionHistoryData.DEVICEID_NAME);
    mpTxtStation = new SKDCTextField(TransactionHistoryData.STATION_NAME);

    addInput("Action Type:", mpActionTypeCombo);
    addInput("Beginning Date:", mpBeginDateField);
    addInput("Ending Date:", mpEndingDateField);
    addInput("User ID:", mpComboUserID);
    addInput("Role:", mpComboRole);
    addInput("Device:", mpTxtDeviceID);
    addInput("Station:", mpTxtStation);
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
    mpTNSearchData.setTranCategoryKey(DBConstants.USER_TRAN);
    mpTNSearchData.setDateRangeKey(mpBeginDateField.getDate(),
        mpEndingDateField.getDate());

    // Add User entry as Key.
    mpTNSearchData.setUserIDKey(mpComboUserID.getSelectedItem().toString());
    // Add Role entry as Key.
    mpTNSearchData.setRoleKey(mpComboRole.getSelectedItem().toString());
    // Add Device as Key.
    if (mpTxtDeviceID.getText().trim().length() != 0)
    {
      mpTNSearchData.setDeviceIDKey(mpTxtDeviceID.getText());
    }
    // Add Station as Key.
    if (mpTxtStation.getText().trim().length() != 0)
    {
      mpTNSearchData.setStationKey(mpTxtStation.getText());
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
    mpBeginDateField.setDate();
    mpBeginDateField.setDate(mpBegDate);
    mpEndingDateField.setDate();
    mpComboUserID.setSelectedIndex(0);
    mpComboRole.setSelectedIndex(0);
    mpTxtDeviceID.setText("");
    mpTxtStation.setText("");
  }
}
