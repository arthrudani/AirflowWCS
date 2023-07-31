package com.daifukuamerica.wrxj.swingui.reasoncode;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ReasonCodeData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swing.SKDCTranComboBox;
import javax.swing.event.InternalFrameEvent;

/**
 * A screen class for updating Reason Codes
 *
 * @author jan
 * @version 1.0
 */
@SuppressWarnings("serial")
public class UpdateReasonCode extends DacInputFrame
{
  StandardInventoryServer mpInvServer = Factory.create(StandardInventoryServer.class);
  
  SKDCTranComboBox mpTCReasonCategory;
  SKDCTextField mpTxtReasonCode = new SKDCTextField(ReasonCodeData.REASONCODE_NAME);
  SKDCTextField mpTxtReasonCodeDescription = new SKDCTextField(ReasonCodeData.DESCRIPTION_NAME);
  
  protected String msReasonCode  = "";
  protected String msReasonCodeDescription = "";
  protected int miReasonCategory = DBConstants.REASONHOLD;

  boolean mzAdding = true;
  
  /**
   * Create Reason Code screen class.
   * 
   * @param isTitle Title to be displayed.
   */
  public UpdateReasonCode(String isTitle)
  {
    super(isTitle, "Reason Code Information");
    try
    {
      buildScreen();
    }
    catch (NoSuchFieldException nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
  }

  public UpdateReasonCode(String isTitle, int iItem)
  {
    this(isTitle);
    miReasonCategory = iItem;
  }

  /**
   *  Method to set screen for modifying.
   *
   *  @param iiReasonCategory Reason Category.
   *  @param isReasonCode Reason Code.
   *  @param isReasonCodeDescription Reason Description.
   */
  public void setModify(int iiReasonCategory, String isReasonCode, String isReasonCodeDescription)
  {
    miReasonCategory = iiReasonCategory;
    msReasonCode = isReasonCode;
    msReasonCodeDescription = isReasonCodeDescription;
    mzAdding = false;
    useModifyButtons();
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

    if (!mzAdding)
    {
      mpTxtReasonCode.setEnabled(false);
      
      this.setTimeout(60);
    }
    clearButtonPressed();
  }

  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the update form
   * 
   * @throws NoSuchFieldException
   */
  private void buildScreen() throws NoSuchFieldException
  {
    mpTCReasonCategory = new SKDCTranComboBox(ReasonCodeData.REASONCATEGORY_NAME);

    addInput("Reason Category:", mpTCReasonCategory);
    addInput("Reason Code:", mpTxtReasonCode);
    addInput("Description:", mpTxtReasonCodeDescription);
    
    useAddButtons();
  }

  /**
   *  Method to clean up as needed at closing.
   */
  @Override
  public void cleanUpOnClose()
  {
    mpInvServer.cleanUp();
    mpInvServer = null;
  }

  /**
   *  Action method to handle Clear button..
   */
  @Override
  protected void clearButtonPressed()
  {
    try
    {
      mpTCReasonCategory.setSelectedElement(miReasonCategory);
    }
    catch (NoSuchFieldException nsfe)
    {
      displayError(nsfe.getMessage());
    }
    mpTxtReasonCode.setText(msReasonCode);
    mpTxtReasonCodeDescription.setText(msReasonCodeDescription);
  }

  /**
   *  Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }

  /**
   *  Action method to handle OK button. Verifies that entered data is valid,
   *  then adds a new container to the database.
   */
  @Override
  protected void okButtonPressed()
  {
    ReasonCodeData rcdata = Factory.create(ReasonCodeData.class);
    int iCategory = 0;
    try
    {
      iCategory = DBTrans.getIntegerValue(ReasonCodeData.REASONCATEGORY_NAME,
          mpTCReasonCategory.getSelectedItem().toString());
    }
    catch(NoSuchFieldException exp)
    {
      logger.logError("Unable to get translation value for Reason Category");
      return;
    }
    if (mpTxtReasonCode.getText().trim().length() == 0)
    {
      displayInfoAutoTimeOut("Reason Code cannot be blank.");
      return;
    }
    rcdata.setReasonCategory(iCategory);
    rcdata.setDescription(mpTxtReasonCodeDescription.getText());
    rcdata.setReasonCode(mpTxtReasonCode.getText());
    try
    {
      if(mzAdding)
      {
        mpInvServer.addReasonCode(rcdata.getReasonCategory(),
            rcdata.getReasonCode(), rcdata.getReasonCodeDescription());
      }
      else
      {
        mpInvServer.updateReasonCodeInfo(rcdata);
      }
    }
    catch(DBException exp)
    {
      exp.printStackTrace();
      logger.logException(exp);
    }
    close();
  }
}
