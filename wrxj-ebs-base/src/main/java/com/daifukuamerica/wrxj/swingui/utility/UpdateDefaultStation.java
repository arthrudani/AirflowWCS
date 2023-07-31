package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCComboBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;

import java.awt.event.ActionEvent;

public class UpdateDefaultStation extends UpdateSysConfig
{
  private static final long serialVersionUID = 0L;
  private static final String GUI_CFG_PREFIX = SysConfig.GUI_CFG_PREFIX;
  private static final int GUI_CFG_PREFIX_LENGTH = SysConfig.GUI_CFG_PREFIX.length();

  protected SKDCTextField mpTxtIPAddress;
  protected SKDCComboBox  mpCmbOpType;
  
  public UpdateDefaultStation()
  {
    this("");
  }
  
  public UpdateDefaultStation(String isFrameTitle)
  {
    super(isFrameTitle, "Default Station Parameter Information");
  }
  
  /**
   * Build the screen
   */
  @Override
  protected void jbInit()
  {
    // Create filed 
    mpTxtIPAddress = new SKDCTextField(50);
    String[] masOpTypeList = {SysConfig.OPTYPE_PICK, 
                              SysConfig.OPTYPE_STORE};
    mpCmbOpType = new SKDCComboBox(masOpTypeList);

    super.jbInit();
    
    mpTxtName.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (isStationNameValid(mpTxtName.getText().trim()))
        {
          mpTxtIPAddress.requestFocusInWindow();
          mpTxtIPAddress.selectAll();
        }
      }
    });
    
    mpTxtIPAddress.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        mpTxtIPAddress.setText(mpTxtIPAddress.getText().toLowerCase());
        mpCmbOpType.requestFocusInWindow();
      }
    });
  }
  
  /**
   * Build the screen
   */
  @Override
  protected void buildDataPanel()
  {
    /*
     * Build the data panel
     */
    addInput("Station Name:", mpTxtName);
    addInput("IP Address/Machine Name:", mpTxtIPAddress);
    addInput("Operation Category:", mpCmbOpType);
    
    useAddButtons();
  }
  
  /**
   * Method to verify if the given station name has been defined as a 
   * default station. 
   * @param isStnName <code>String</code> name of a station
   * @return <code>true</code> if station name is valid.
   *         <code>false</code> otherwise.
   */
  private boolean isStationNameValid(String isStnName)
  {
    if (isStnName.isEmpty())
    {
      displayError("Station Name can not be empty");
      return false;
    }
    
    // make sure the station is defined
    Station vpStation = Factory.create(Station.class);
    StationData vpStationData = Factory.create(StationData.class);
    vpStationData.setKey(StationData.STATIONNAME_NAME, isStnName);
    if (vpStation.exists(vpStationData) == false)
    {
      displayError("Station (" + isStnName + ") does NOT exists!", "Station Error");
      mpTxtName.requestFocusInWindow();
      mpTxtName.selectAll();
      return false;
    }

    return true;
  }
  
  /**
   * Method to return if the given default station configuration is valid.
   * The combination of Parameter Name and Screen Type should be unique.
   * 
   * @param isSCData <code>SysConfigData</code> object of configuration
   * @return <code>true</code> if configuration is valid.
   *         <code>false</code> otherwise.
   */
  protected boolean isEntryValid(SysConfigData isSCData)
  {
    String vsName = isSCData.getParameterName();
    String vsValue = isSCData.getParameterValue();
    String vsType = isSCData.getScreenType();
    
    // Make sure it doesn't already exist for exact match
    SysConfigData vpSCD = Factory.create(SysConfigData.class);
    vpSCD.setKey(SysConfigData.GROUP_NAME, SysConfig.DEFAULT_STATION);
    vpSCD.setKey(SysConfigData.PARAMETERNAME_NAME, vsName);
    vpSCD.setKey(SysConfigData.PARAMETERVALUE_NAME, vsValue);
    vpSCD.setKey(SysConfigData.SCREENTYPE_NAME, vsType);
    if (mpSC.exists(vpSCD))
    {
      displayError("Default station for Station (" + vsName +
                   ") Machine/IP Address (" + vsValue + 
                   ") for " + vsType + " already exists!", 
                    "Duplicate Error");
      mpTxtName.requestFocusInWindow();
      mpTxtName.selectAll();
      return false;
    }
   
    // Make sure it doesn't already exist for value and type combination
    vpSCD.clear();
    vpSCD.setKey(SysConfigData.GROUP_NAME, SysConfig.DEFAULT_STATION);
    vpSCD.setKey(SysConfigData.PARAMETERVALUE_NAME, vsValue);
    vpSCD.setKey(SysConfigData.SCREENTYPE_NAME, vsType);
    
    // Make sure it doesn't already exist
    if (mpSC.exists(vpSCD))
    {
      displayError("Default station for Machine/IP Address (" + vsValue + 
              ") for " + vsType + " already exists!", 
              "Duplicate Error");
      mpTxtIPAddress.requestFocusInWindow();
      mpTxtIPAddress.selectAll();
      return false;
    }

    return true;
  }

  /**
   * Method for the Submit button.
   */
  @Override
  protected SysConfigData getDataFromInputFields()
  {
    SysConfigData vpSCD = Factory.create(SysConfigData.class);
    vpSCD.setGroup(SysConfig.DEFAULT_STATION);
    vpSCD.setParameterName(mpTxtName.getText().trim());
    vpSCD.setParameterValue(getValue(mpTxtIPAddress.getText().toLowerCase()));
    vpSCD.setDescription(mpTxtDescArea.getText().trim());
    vpSCD.setEnabled(mpChkBxEnabled.isSelectedYesNo());
    vpSCD.setScreenChangeAllowed(mpChkBxChangeAllowed.isSelectedYesNo());
    vpSCD.setScreenType(mpCmbOpType.getText().trim());
    
    return vpSCD;
  }

  /**
   * Method for the Submit button.
   */
  @Override
  protected void okButtonPressed()
  {
    SysConfigData vpSCD = getDataFromInputFields();

    if (mzAdding && isStationNameValid(vpSCD.getParameterName()) == false)
    {
      return;
    }
    
    if (isEntryValid(vpSCD))
    {
      try
      {
        updateDatabase(vpSCD);
        changed();
        
        if (mzAdding)
        {
          displayInfoAutoTimeOut("Default Station (" + 
                                  vpSCD.getParameterName() + ") added");
          clearButtonPressed();
        }
        else
        {
          displayInfoAutoTimeOut("Default Station (" + 
                                  vpSCD.getParameterName() + ") modified");
          close();
        }
      }
      catch(DBException ex)
      {
        displayError(ex.getMessage(), "Database Error");
        mpTxtName.requestFocusInWindow();
        mpTxtName.selectAll();
      }
    }
  }
  
  /**
   * Method to set data
   * @param ipSCD <code>SysConfigData</code> of object
   */
  @Override
  protected void setData(SysConfigData ipSCD)
  {
    mpTxtGroup.setText(ipSCD.getGroup());
    mpTxtName.setText(getStationName(ipSCD));
    mpTxtIPAddress.setText(getIPAddress(ipSCD));
    mpTxtDescArea.setText(ipSCD.getDescription());
    mpChkBxEnabled.setSelected(ipSCD.getEnabled() == DBConstants.YES);
    mpChkBxChangeAllowed.setSelected(ipSCD.getScreenChangeAllowed() == DBConstants.YES);
    mpCmbOpType.setSelectedItem(ipSCD.getScreenType());
  
    setupInputFields();
  }
  
  /**
   * Method to enable/disable input fields base on the user's action.
   */
  @Override
  protected void setupInputFields()
  {
    if (mzAdding == true)
    {
      mpTxtName.setEditable(true);
      mpTxtName.requestFocusInWindow();
      mpTxtName.selectAll();
    }
    else
    {
      mpTxtName.setEditable(false);
      mpTxtIPAddress.setEditable(true);
      mpTxtIPAddress.requestFocusInWindow();
      mpTxtIPAddress.selectAll();
    }
  }
  
  /**
   * Method to return the IP Address. If the string begins with 'GUI@',
   * the method will strip it.
   * @param ipSCD <code>SysConfigData</code> Object of the record.
   * @return <code>String</code> the IP Address (or machine name)
   */
  private String getIPAddress(SysConfigData ipSCD)
  {
    String vsIPAddress = ipSCD.getParameterValue();
    if (vsIPAddress.startsWith(GUI_CFG_PREFIX))
    {
      vsIPAddress = vsIPAddress.substring(GUI_CFG_PREFIX_LENGTH);
    }
    return vsIPAddress;
  }
  
  /**
   * Method to return the station name.
   * @param ipSCD <code>SysConfigData</code> Object of the record.
   * @return <code>String</code> the name of a Station
   */
  private String getStationName(SysConfigData ipSCD)
  {
    return ipSCD.getParameterName();
  }
  
  /**
   * Method to return the group which is the IP Address (or machine name) with
   * 'GUI@' added at the beginning.
   * @param isIPAddress <code>String</code> to be converted into group
   * @return <code>String</code> the value of Group.
   */
  private String getValue(String isIPAddress)
  {
    String vsGroup = isIPAddress;
    if (vsGroup.startsWith(SysConfig.GUI_CFG_PREFIX) == false)
    {
      vsGroup = GUI_CFG_PREFIX + vsGroup;
    }
    return vsGroup;
  }

  /**
   * Method for update database records.
   * @param ipSCD <code>SysConfigData</code> Object of SysConfig record 
   * to be added/updated.
   */
  @Override
  protected void updateDatabase(SysConfigData ipSCD) throws DBException
  {
      // set keys
    ipSCD.setKey(SysConfigData.GROUP_NAME, mpCurSCD.getGroup());
    ipSCD.setKey(SysConfigData.PARAMETERNAME_NAME, mpCurSCD.getParameterName());
    if (mpCurSCD.getScreenType().isEmpty() == false)
    {
      ipSCD.setKey(SysConfigData.SCREENTYPE_NAME, mpCurSCD.getScreenType());
    }

    TransactionToken vpTT = null;
    
    vpTT = mpDBObject.startTransaction();
    if (mzAdding)
    {
      mpSC.addElement(ipSCD);
    }
    else
    {
      mpSC.modifyElement(ipSCD);
    }
    mpDBObject.commitTransaction(vpTT);
  }
}
