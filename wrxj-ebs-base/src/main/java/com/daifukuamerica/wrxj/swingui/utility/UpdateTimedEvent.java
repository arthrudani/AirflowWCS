package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfig;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;

import javax.swing.JScrollPane;

public class UpdateTimedEvent extends UpdateControllerConfig
{
  private static final long serialVersionUID = 0L;

  public UpdateTimedEvent()
  {
    this("");
  }
  
  public UpdateTimedEvent(String isFrameTitle)
  {
    super(isFrameTitle, "Timed Event Parameter Information");
  }
  
  /**
   * Build the screen
   */
  @Override
  protected void buildDataPanel()
  {
    addInput("Name:", mpTxtName);
    addInput("Value:", new JScrollPane(mpTxtValueArea));
    addInput("Description:", new JScrollPane(mpTextDescArea));
    
    useAddButtons();
  }

  /**
   * Method for the Submit button.
   */
  @Override
  protected ControllerConfigData getDataFromInputFields()
  {
    // get data from screen fields
    ControllerConfigData vpCCD = Factory.create(ControllerConfigData.class);
    vpCCD.setController(ControllerConfig.TIMEDEVENTSCHEDULER);
    vpCCD.setPropertyName(mpTxtName.getText().trim());
    vpCCD.setPropertyValue(mpTxtValueArea.getText().trim());
    vpCCD.setPropertyDesc(mpTextDescArea.getText().trim());
    vpCCD.setEnabled(mpChkBxEnabled.isSelectedYesNo());
    vpCCD.setScreenChangeAllowed(mpChkBxChangeAllowed.isSelectedYesNo());
    return vpCCD;
  }

  /**
   * Method for the Submit button.
   */
  @Override
  protected void okButtonPressed()
  {
    // get data from screen fields
    ControllerConfigData vpCCD = getDataFromInputFields();
    
    try
    {
      updateDatabase(vpCCD);
      changed();
      
      if (mzAdding)
      {
        displayInfoAutoTimeOut("TimedEventScheduler record added");
        clearButtonPressed();
      }
      else
      {
        displayInfoAutoTimeOut("TimedEventScheduler record modified");
        close();
      }
    }
    catch(DBException ex)
    {
      displayError(ex.getMessage(), "Database Error");
      setupInputFields();
    }
  }
  
  /**
   * Method for setting up Input Fields
   */
  @Override
  protected void setupInputFields()
  {
    if (mzAdding == true)
    {
      mpTxtName.requestFocusInWindow();
      mpTxtName.selectAll();
    }
    else
    {
      mpTxtName.setEditable(false);
      mpTxtValueArea.requestFocusInWindow();
      mpTxtValueArea.selectAll();
    }
  }
}
