package com.daifukuamerica.wrxj.swingui.customer;

import com.daifukuamerica.wrxj.dataserver.standard.StandardCustomerServer;
import com.daifukuamerica.wrxj.dbadapter.data.CustomerData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating containers.
 * <BR><B>NOTE: </B>Currently references to Zone are commented out.<BR>
 *
 * @author avt
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateCustomer extends DacInputFrame
{
  String msCustomer = "";
  CustomerData mpDefaultData = Factory.create(CustomerData.class);
  
  SKDCTextField mpCustomerText = new SKDCTextField(CustomerData.CUSTOMER_NAME);
  SKDCTextField mpDescription1Text = new SKDCTextField(CustomerData.DESCRIPTION1_NAME);
  SKDCTextField mpDescription2Text = new SKDCTextField(CustomerData.DESCRIPTION2_NAME);
  SKDCTextField mpStreetAddress1Text = new SKDCTextField(CustomerData.STREETADDRESS1_NAME);
  SKDCTextField mpStreetAddress2Text = new SKDCTextField(CustomerData.STREETADDRESS2_NAME);
  SKDCTextField mpCityText = new SKDCTextField(CustomerData.CITY_NAME);
  SKDCTextField mpStateText = new SKDCTextField(CustomerData.STATE_NAME);
  SKDCTextField mpZipCodeText = new SKDCTextField(CustomerData.ZIPCODE_NAME);
  SKDCTextField mpCountryText = new SKDCTextField(CustomerData.COUNTRY_NAME);
  SKDCTextField mpPhoneText = new SKDCTextField(CustomerData.PHONE_NAME);
  SKDCTextField mpAttentionText = new SKDCTextField(CustomerData.ATTENTION_NAME);
  SKDCTextField mpContactText = new SKDCTextField(CustomerData.CONTACT_NAME);
  SKDCTextField mpNoteText = new SKDCTextField(CustomerData.NOTE_NAME);

  SKDCCheckBox mpDeleteOnUseCheckBox = new SKDCCheckBox("Delete After Use");

  StandardCustomerServer mpCustomerServer = Factory.create(StandardCustomerServer.class);
  
  boolean mzAdding = true;

  /**
   *  Create container screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public UpdateCustomer(String isTitle)
  {
    super(isTitle, "Customer Information");

    try
    {
      jbInit();
      pack();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   *  Create default container screen class.
   */
  public UpdateCustomer()
  {
    this("");
  }

  /**
   *  Method to intialize screen components. This adds the components to the
   *  screen and adds listeners as needed.
   *
   *  @exception Exception
   */
  void jbInit() throws Exception
  {
    addInput("Customer:", mpCustomerText);
    addInput("Description:", mpDescription1Text);
    addInput("", mpDescription2Text);
    addInput("Address:", mpStreetAddress1Text);
    addInput("", mpStreetAddress2Text);
    addInput("City:", mpCityText);
    addInput("State:", mpStateText);
    addInput("Zip Code:", mpZipCodeText);
    addInput("Country:", mpCountryText);
    addInput("Phone:", mpPhoneText);
    addInput("Attention:", mpAttentionText);
    addInput("Contact:", mpContactText);
    addInput("Note:", mpNoteText);
    addInput("", mpDeleteOnUseCheckBox);

    useAddButtons();
  }
   
 /**
  *  Method to set screen for modifing.
  *
  *  @param isCustomer customer to be modified.
  */
  public void setModify(String isCustomer)
  {
    msCustomer = isCustomer;
    useModifyButtons();
    mzAdding = false;
  }

  /**
   * Overridden method so we can set up frame for either an add or modify
   *
   * @param ipEvent ignored
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent ipEvent)
  {
    super.internalFrameOpened(ipEvent);

    if (msCustomer.length() > 0) // we are modifying
    {
      try
      {
        mpDefaultData = mpCustomerServer.getCustomerRecord(msCustomer);
      }
      catch (DBException e2)
      {
        displayError("Unable to get Container data");
        return;
      }

      setData(mpDefaultData);
      mpCustomerText.setEnabled(false);
      this.setTimeout(90);
    }
    else
    {
      setData(mpDefaultData);
    }
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new container to the database.
   */
  @Override
  public void okButtonPressed()
  {
    boolean alreadyExists;

    CustomerData vpCustomerData = Factory.create(CustomerData.class);
    vpCustomerData.setKey(CustomerData.CUSTOMER_NAME, mpCustomerText.getText().trim());
    try
    {
      alreadyExists = mpCustomerServer.CustomerExists(vpCustomerData);
    }
    catch (DBException e2)
    {
      displayError("Unable to get Container data");
      return;
    }

    if (mzAdding)
    {
        if(mpCustomerText.getText().trim().length() < 1)
        {
            displayError("Customer Cannot Be Blank");
            mpCustomerText.requestFocus();
            return;
        }
    }
    if (mzAdding && alreadyExists)
    {
      displayError("Customer " + mpCustomerText.getText().trim() + " already exists");
      mpCustomerText.requestFocus();
      return;
    }

    if (!mzAdding && !alreadyExists)
    {
      displayError("Customer " + mpCustomerText.getText().trim() + " does not exist");
      mpCustomerText.requestFocus();
    }

    // fill in Container data
    if (!mzAdding)
    {
      try
      {
        vpCustomerData = mpCustomerServer.getCustomerRecord(mpCustomerText.getText().trim());
      }
      catch (DBException e2)
      {
        displayError("Unable to get Customer data");
        return;
      }
    }

    try
    {
      if (mzAdding)
      {
        vpCustomerData.setCustomer(mpCustomerText.getText().trim());
      }

      vpCustomerData.setDescription1(mpDescription1Text.getText().trim());
      vpCustomerData.setDescription2(mpDescription2Text.getText().trim());
      vpCustomerData.setStreetAddress1(mpStreetAddress1Text.getText().trim());
      vpCustomerData.setStreetAddress2(mpStreetAddress2Text.getText().trim());
      vpCustomerData.setCity(mpCityText.getText().trim());
      vpCustomerData.setState(mpStateText.getText().trim());
      vpCustomerData.setZipcode(mpZipCodeText.getText().trim());
      vpCustomerData.setCountry(mpCountryText.getText().trim());
      vpCustomerData.setPhone(mpPhoneText.getText().trim());
      vpCustomerData.setAttention(mpAttentionText.getText().trim());
      vpCustomerData.setContact(mpContactText.getText().trim());
      vpCustomerData.setNote(mpNoteText.getText().trim());
      vpCustomerData.setDeleteOnUse(mpDeleteOnUseCheckBox.isSelectedYesNo());
      if (mzAdding)
      {
        mpCustomerServer.addCustomer(vpCustomerData);
        this.changed();
        displayInfoAutoTimeOut("Customer " + mpCustomerText.getText().trim() + " added");
      }
      else
      {
        vpCustomerData.setKey(CustomerData.CUSTOMER_NAME, vpCustomerData.getCustomer());
        mpCustomerServer.modifyCustomer(vpCustomerData);
        this.changed();
        displayInfoAutoTimeOut("Customer " + mpCustomerText.getText().trim() + " updated");
      }
    }
    catch (DBException ve)
    {
      ve.printStackTrace(System.out);
      if (mzAdding)
      {
        displayError("Error adding Container " + mpCustomerText.getText().trim());
      }
      else
      {
        displayError("Error updating Container " + mpCustomerText.getText().trim());
      }
      return;
    }

    close();
  }

  /**
   *  Action method to handle Clear button..
   */
  @Override
  public void clearButtonPressed()
  {
    setData(mpDefaultData);
  }

  /**
   *  Method to refresh screen fields.
   *
   *  @param ipCTData Container data to use in refreshing.
   */
  private void setData(CustomerData ipCustomerData)
  {
    mpCustomerText.setText(ipCustomerData.getCustomer());
    mpDescription1Text.setText(ipCustomerData.getDescription1());
    mpDescription2Text.setText(ipCustomerData.getDescription2());
    mpStreetAddress1Text.setText(ipCustomerData.getStreetAddress1());
    mpStreetAddress2Text.setText(ipCustomerData.getStreetAddress2());
    mpCityText.setText(ipCustomerData.getCity());
    mpStateText.setText(ipCustomerData.getState());
    mpZipCodeText.setText(ipCustomerData.getZipcode());
    mpCountryText.setText(ipCustomerData.getCountry());
    mpPhoneText.setText(ipCustomerData.getPhone());
    mpAttentionText.setText(ipCustomerData.getAttention());
    mpContactText.setText(ipCustomerData.getContact());
    mpNoteText.setText(ipCustomerData.getNote());
    
    mpDeleteOnUseCheckBox.setSelected(ipCustomerData.getDeleteOnUse() == DBConstants.YES);
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  public void closeButtonPressed()
  {
    close();
  }
}